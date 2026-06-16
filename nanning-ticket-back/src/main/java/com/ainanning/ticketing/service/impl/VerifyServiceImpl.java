package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.VerifyQueryDTO;
import com.ainanning.ticketing.dto.VerifyRequestDTO;
import com.ainanning.ticketing.entity.Order;
import com.ainanning.ticketing.entity.OrderItem;
import com.ainanning.ticketing.entity.Sale;
import com.ainanning.ticketing.entity.SaleItem;
import com.ainanning.ticketing.entity.Scenic;
import com.ainanning.ticketing.entity.Ticket;
import com.ainanning.ticketing.entity.VerifyRecord;
import com.ainanning.ticketing.mapper.OrderItemMapper;
import com.ainanning.ticketing.mapper.OrderMapper;
import com.ainanning.ticketing.mapper.SaleItemMapper;
import com.ainanning.ticketing.mapper.SaleMapper;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.mapper.TicketMapper;
import com.ainanning.ticketing.mapper.VerifyRecordMapper;
import com.ainanning.ticketing.service.VerifyService;
import com.ainanning.ticketing.service.VoucherService;
import com.ainanning.ticketing.vo.VerifyRecordVO;
import com.ainanning.ticketing.vo.VerifyResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 检票业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>核心流程：根据 {@code voucherCode} 反查 {@code sale_item} 或 {@code order_item}
 *       → 校验主单状态 → 校验日期 → 并发门控（条件更新 voucher）→ 写记录</li>
 *   <li>支持双源票据：先查 sale_item（窗口售票），未命中再查 order_item（在线订单）</li>
 *   <li>无论成功失败都写 {@code verify_record}（审计需求），闸机/前端根据 {@code result} 字段判断</li>
 *   <li>并发安全：使用 {@code UPDATE voucher SET status='已使用' WHERE status='待使用'} 条件更新
 *       作为原子门控，避免两闸机同时扫描同一票码都返回成功</li>
 *   <li>有效期：{@code today ∈ [inventoryDate, inventoryDate + validDays - 1]}，否则视为未生效/已过期</li>
 *   <li>票种已下架但已售票仍允许核销（软警告，不阻断）</li>
 *   <li>N+1 防护：分页后批量注入 scenicName / saleNo / ticketName</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final VerifyRecordMapper verifyRecordMapper;
    private final SaleItemMapper saleItemMapper;
    private final SaleMapper saleMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final TicketMapper ticketMapper;
    private final ScenicMapper scenicMapper;
    private final VoucherService voucherService;

    /* 检票结果 */
    private static final String RESULT_SUCCESS = "成功";
    private static final String RESULT_FAIL    = "失败";

    /* 失败原因 */
    private static final String FAIL_REASON_NOT_FOUND    = "无效码";
    private static final String FAIL_REASON_USED         = "已使用";
    private static final String FAIL_REASON_NOT_VALID    = "未生效";
    private static final String FAIL_REASON_EXPIRED      = "已过期";
    private static final String FAIL_REASON_SOLD_INVALID = "销售单已退";
    private static final String FAIL_REASON_TICKET_GONE  = "票种已下架";

    /* 检票方式白名单 */
    private static final List<String> METHOD_WHITELIST =
            Arrays.asList("扫码", "手输", "刷脸");

    /* 默认检票方式 */
    private static final String DEFAULT_METHOD = "扫码";

    /* 主单状态白名单（可检） */
    private static final String SALE_STATUS_PAID    = "已支付";
    private static final String SALE_STATUS_PARTIAL = "部分退票";
    /* Order 状态：已出票 / 部分退款 均可核销；退款中 / 已退款 / 已取消 不可核销 */
    private static final Set<String> ORDER_VERIFIABLE_STATUSES = Set.of(
            Order.STATUS_FULFILLED, Order.STATUS_PARTIAL);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyResultVO verify(VerifyRequestDTO dto) {
        log.info("[检票] 收到请求 voucherCode={}, method={}, staff={}, device={}",
                dto.getVoucherCode(), dto.getVerifyMethod(),
                dto.getVerifyStaffName(), dto.getDeviceName());

        // 1. 校验检票方式
        String method = StringUtils.hasText(dto.getVerifyMethod())
                ? dto.getVerifyMethod() : DEFAULT_METHOD;
        if (!METHOD_WHITELIST.contains(method)) {
            throw new BusinessException(ResultCode.VERIFY_METHOD_INVALID);
        }

        // 2. 反查票据所属明细（先 SaleItem，再 OrderItem）
        Source source = resolveSource(dto.getVoucherCode());
        if (source == null) {
            log.warn("[检票] 票据码无效 voucherCode={}", dto.getVoucherCode());
            return saveAndBuildFail(dto, method, null, FAIL_REASON_NOT_FOUND);
        }

        // 3. 校验主单状态
        if (!source.isVerifiable()) {
            return saveAndBuildFail(dto, method, source, FAIL_REASON_SOLD_INVALID);
        }

        // 4. 校验票种：可能已下架但已售票仍可核销（与注释一致，仅 warn 不阻断）
        Ticket ticket = ticketMapper.selectById(source.getTicketId());
        if (ticket == null || ticket.getDeletedAt() != null) {
            log.warn("[检票] 票种已下架但允许已售票核销 ticketId={}", source.getTicketId());
            // 不阻断：保留原逻辑"已售票仍可核销"
        }

        // 5. 校验入场日期：[inventoryDate, inventoryDate + validDays - 1]
        LocalDate today = LocalDate.now();
        LocalDate validFrom = source.getInventoryDate();
        int validDays = ticket != null && ticket.getValidDays() != null ? ticket.getValidDays() : 1;
        LocalDate validTo   = validFrom.plusDays(Math.max(0, validDays - 1));
        if (today.isBefore(validFrom)) {
            return saveAndBuildFail(dto, method, source, FAIL_REASON_NOT_VALID);
        }
        if (today.isAfter(validTo)) {
            return saveAndBuildFail(dto, method, source, FAIL_REASON_EXPIRED);
        }

        // 6. 查重（前置快速失败，可被 7 的条件更新覆盖）
        long used = verifyRecordMapper.countSuccessByVoucherCode(dto.getVoucherCode());
        if (used > 0) {
            log.info("[检票] 票据已被使用 voucherCode={}", dto.getVoucherCode());
            return saveAndBuildFail(dto, method, source, FAIL_REASON_USED);
        }

        // 7. 用 voucher 的条件更新作为并发门控（原子操作）：
        //    markUsedIfUnused 仅在 status='待使用' 时才改 status='已使用'，
        //    并发两次核销同一票码只有一个能成功（affected rows=1），另一个会拿到 0
        boolean marked = voucherService.markUsed(dto.getVoucherCode(),
                dto.getVerifyStaffId(), dto.getVerifyStaffName(),
                dto.getDeviceId(), dto.getDeviceName());
        if (!marked) {
            log.info("[检票] 并发核销失败 voucherCode={}", dto.getVoucherCode());
            return saveAndBuildFail(dto, method, source, FAIL_REASON_USED);
        }

        // 8. 写入成功记录（在 markUsed 成功之后，确保不会留下"未核销却有记录"的不一致状态）
        Scenic scenic = scenicMapper.selectById(source.getScenicId());
        VerifyRecord record = new VerifyRecord();
        record.setVoucherCode(dto.getVoucherCode());
        record.setSaleId(source.getBusinessId());
        record.setSaleItemId(source.getBusinessItemId());
        record.setTicketId(source.getTicketId());
        record.setTicketName(source.getTicketName());
        record.setScenicId(source.getScenicId());
        record.setInventoryId(source.getInventoryId());
        record.setInventoryDate(source.getInventoryDate());
        record.setVerifyTime(dto.getVerifyTime() != null ? dto.getVerifyTime() : LocalDateTime.now());
        record.setVerifyMethod(method);
        record.setVerifyStaffId(dto.getVerifyStaffId());
        record.setVerifyStaffName(dto.getVerifyStaffName());
        record.setDeviceId(dto.getDeviceId());
        record.setDeviceName(dto.getDeviceName());
        record.setResult(RESULT_SUCCESS);
        record.setVisitorName(source.getVisitorName());
        int rows = verifyRecordMapper.insert(record);
        if (rows == 0) {
            log.error("[检票] 写入成功记录失败 voucherCode={}", dto.getVoucherCode());
            throw new BusinessException(ResultCode.VERIFY_SAVE_FAILED);
        }

        log.info("[检票] 核销成功 voucherCode={}, source={}, recordId={}",
                dto.getVoucherCode(), source.getSourceType(), record.getId());

        VerifyResultVO.VerifyResultVOBuilder builder = VerifyResultVO.builder()
                .recordId(record.getId())
                .result(RESULT_SUCCESS)
                .voucherCode(dto.getVoucherCode())
                .sourceType(source.getSourceType())
                .ticketId(source.getTicketId())
                .ticketName(source.getTicketName())
                .scenicId(source.getScenicId())
                .scenicName(scenic == null ? null : scenic.getName())
                .inventoryDate(source.getInventoryDate())
                .unitPrice(source.getUnitPrice())
                .visitorName(source.getVisitorName())
                .verifyTime(record.getVerifyTime())
                .verifyMethod(method)
                .verifyStaffName(dto.getVerifyStaffName())
                .deviceName(dto.getDeviceName());
        source.fillBusinessNo(builder);
        return builder.build();
    }

    @Override
    public PageVO<VerifyRecordVO> page(VerifyQueryDTO query) {
        log.info("[检票] 分页查询 scenicId={}, result={}, method={}, keyword={}, [{} ~ {}]",
                query.getScenicId(), query.getResult(), query.getVerifyMethod(),
                query.getKeyword(), query.getDateFrom(), query.getDateTo());

        // 1. 分页对象
        Page<VerifyRecord> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 条件
        LambdaQueryWrapper<VerifyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(VerifyRecord::getDeletedAt);
        if (query.getScenicId() != null) {
            wrapper.eq(VerifyRecord::getScenicId, query.getScenicId());
        }
        if (query.getSaleId() != null) {
            wrapper.eq(VerifyRecord::getSaleId, query.getSaleId());
        }
        if (StringUtils.hasText(query.getResult())) {
            wrapper.eq(VerifyRecord::getResult, query.getResult());
        }
        if (StringUtils.hasText(query.getVerifyMethod())) {
            wrapper.eq(VerifyRecord::getVerifyMethod, query.getVerifyMethod());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(VerifyRecord::getVoucherCode, kw)
                    .or().like(VerifyRecord::getVisitorName, kw)
                    .or().like(VerifyRecord::getVerifyStaffName, kw));
        }
        if (query.getDateFrom() != null) {
            wrapper.ge(VerifyRecord::getVerifyTime, query.getDateFrom().atStartOfDay());
        }
        if (query.getDateTo() != null) {
            wrapper.le(VerifyRecord::getVerifyTime, query.getDateTo().atTime(23, 59, 59));
        }
        wrapper.orderByDesc(VerifyRecord::getVerifyTime).orderByDesc(VerifyRecord::getId);

        // 3. 查询
        Page<VerifyRecord> result = verifyRecordMapper.selectPage(page, wrapper);

        // 4. 注入 scenicName / saleNo
        List<VerifyRecordVO> records = enrichRecords(result.getRecords());

        return PageVO.of(result, records);
    }

    @Override
    public VerifyRecordVO getById(Long id) {
        log.info("[检票] 查询详情 id={}", id);
        VerifyRecord entity = verifyRecordMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.VERIFY_RECORD_NOT_FOUND);
        }
        return enrichRecords(Collections.singletonList(entity)).get(0);
    }

    @Override
    public List<VerifyRecordVO> listByVoucherCode(String voucherCode) {
        log.info("[检票] 按票据码查历史 voucherCode={}", voucherCode);
        if (!StringUtils.hasText(voucherCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<VerifyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(VerifyRecord::getDeletedAt)
               .eq(VerifyRecord::getVoucherCode, voucherCode)
               .orderByDesc(VerifyRecord::getVerifyTime)
               .orderByDesc(VerifyRecord::getId);
        List<VerifyRecord> records = verifyRecordMapper.selectList(wrapper);
        return enrichRecords(records);
    }

    @Override
    public long countTodaySuccess(Long scenicId) {
        LocalDate today = LocalDate.now();
        return countSuccessByDate(scenicId, today);
    }

    @Override
    public long countSuccessByDate(Long scenicId, LocalDate date) {
        if (scenicId == null || date == null) {
            return 0L;
        }
        return verifyRecordMapper.countSuccessByScenicAndTime(
                scenicId, date.atStartOfDay(), date.atTime(LocalTime.MAX));
    }

    /* ====================== 私有方法 ====================== */

    /**
     * 写一条失败记录并返回失败结果 VO（适配 SaleItem / OrderItem 双源）
     */
    private VerifyResultVO saveAndBuildFail(VerifyRequestDTO dto, String method,
                                            Source source, String failReason) {
        VerifyRecord record = new VerifyRecord();
        record.setVoucherCode(dto.getVoucherCode());
        if (source != null) {
            record.setSaleId(source.getBusinessId());
            record.setSaleItemId(source.getBusinessItemId());
            record.setTicketId(source.getTicketId());
            record.setTicketName(source.getTicketName());
            record.setScenicId(source.getScenicId());
            record.setInventoryId(source.getInventoryId());
            record.setInventoryDate(source.getInventoryDate());
        }
        record.setVerifyTime(dto.getVerifyTime() != null ? dto.getVerifyTime() : LocalDateTime.now());
        record.setVerifyMethod(method);
        record.setVerifyStaffId(dto.getVerifyStaffId());
        record.setVerifyStaffName(dto.getVerifyStaffName());
        record.setDeviceId(dto.getDeviceId());
        record.setDeviceName(dto.getDeviceName());
        record.setResult(RESULT_FAIL);
        record.setFailReason(failReason);
        verifyRecordMapper.insert(record);

        VerifyResultVO.VerifyResultVOBuilder builder = VerifyResultVO.builder()
                .recordId(record.getId())
                .result(RESULT_FAIL)
                .failReason(failReason)
                .voucherCode(dto.getVoucherCode())
                .verifyTime(record.getVerifyTime())
                .verifyMethod(method)
                .verifyStaffName(dto.getVerifyStaffName())
                .deviceName(dto.getDeviceName());
        if (source != null) {
            Scenic scenic = scenicMapper.selectById(source.getScenicId());
            builder.sourceType(source.getSourceType())
                    .ticketId(source.getTicketId())
                    .ticketName(source.getTicketName())
                    .scenicId(source.getScenicId())
                    .scenicName(scenic == null ? null : scenic.getName())
                    .inventoryDate(source.getInventoryDate());
            source.fillBusinessNo(builder);
            if (VerifyResultVO.SOURCE_SALE.equals(source.getSourceType())) {
                builder.saleItemId(source.getBusinessItemId());
            } else {
                builder.orderItemId(source.getBusinessItemId());
            }
        }
        return builder.build();
    }

    /**
     * 票据来源统一抽象：同时承载 Sale 与 Order 两种来源的字段，
     * 由 {@link #resolveSource(String)} 在查表后填充并返回。
     */
    private class Source {
        private final String sourceType;       // SALE / ORDER
        private final Long businessId;         // Sale.id 或 Order.id
        private final Long businessItemId;     // SaleItem.id 或 OrderItem.id
        private final String businessNo;       // Sale.saleNo 或 Order.orderNo
        private final Long ticketId;
        private final String ticketName;
        private final Long scenicId;
        private final Long inventoryId;
        private final LocalDate inventoryDate;
        private final BigDecimal unitPrice;
        private final String visitorName;
        private final boolean verifiable;      // 主单状态是否允许核销

        private Source(String sourceType, Long businessId, Long businessItemId,
                       String businessNo, Long ticketId, String ticketName,
                       Long scenicId, Long inventoryId, LocalDate inventoryDate,
                       BigDecimal unitPrice, String visitorName, boolean verifiable) {
            this.sourceType = sourceType;
            this.businessId = businessId;
            this.businessItemId = businessItemId;
            this.businessNo = businessNo;
            this.ticketId = ticketId;
            this.ticketName = ticketName;
            this.scenicId = scenicId;
            this.inventoryId = inventoryId;
            this.inventoryDate = inventoryDate;
            this.unitPrice = unitPrice;
            this.visitorName = visitorName;
            this.verifiable = verifiable;
        }

        String getSourceType() { return sourceType; }
        Long getBusinessId() { return businessId; }
        Long getBusinessItemId() { return businessItemId; }
        Long getTicketId() { return ticketId; }
        String getTicketName() { return ticketName; }
        Long getScenicId() { return scenicId; }
        Long getInventoryId() { return inventoryId; }
        LocalDate getInventoryDate() { return inventoryDate; }
        BigDecimal getUnitPrice() { return unitPrice; }
        String getVisitorName() { return visitorName; }
        boolean isVerifiable() { return verifiable; }

        void fillBusinessNo(VerifyResultVO.VerifyResultVOBuilder builder) {
            if (VerifyResultVO.SOURCE_SALE.equals(sourceType)) {
                builder.saleId(businessId).saleNo(businessNo);
            } else {
                builder.orderId(businessId).orderNo(businessNo);
            }
        }
    }

    /**
     * 按 voucherCode 反查票据归属：
     * <ol>
     *   <li>先查 sale_item.voucher_codes（窗口售票）</li>
     *   <li>再查 order_item.voucher_codes（在线订单）</li>
     * </ol>
     * 命中后加载主单并校验状态，返回统一抽象的 {@link Source}；都未命中返回 null。
     */
    private Source resolveSource(String voucherCode) {
        // 1. sale_item
        SaleItem saleItem = saleItemMapper.selectByVoucherCode(voucherCode);
        if (saleItem != null) {
            Sale sale = saleMapper.selectById(saleItem.getSaleId());
            if (sale == null || sale.getDeletedAt() != null) {
                return null;
            }
            boolean verifiable = SALE_STATUS_PAID.equals(sale.getStatus())
                    || SALE_STATUS_PARTIAL.equals(sale.getStatus());
            return new Source(
                    VerifyResultVO.SOURCE_SALE,
                    sale.getId(), saleItem.getId(), sale.getSaleNo(),
                    saleItem.getTicketId(), saleItem.getTicketName(),
                    saleItem.getScenicId(), saleItem.getInventoryId(),
                    saleItem.getInventoryDate(), saleItem.getUnitPrice(),
                    sale.getVisitorName(), verifiable);
        }
        // 2. order_item
        OrderItem orderItem = orderItemMapper.selectByVoucherCode(voucherCode);
        if (orderItem != null) {
            Order order = orderMapper.selectById(orderItem.getOrderId());
            if (order == null || order.getDeletedAt() != null) {
                return null;
            }
            boolean verifiable = ORDER_VERIFIABLE_STATUSES.contains(order.getStatus());
            return new Source(
                    VerifyResultVO.SOURCE_ORDER,
                    order.getId(), orderItem.getId(), order.getOrderNo(),
                    orderItem.getTicketId(), orderItem.getTicketName(),
                    orderItem.getScenicId(), orderItem.getInventoryId(),
                    orderItem.getInventoryDate(), orderItem.getUnitPrice(),
                    order.getContactName(), verifiable);
        }
        return null;
    }

    /**
     * Entity 列表 → VO 列表，注入 scenicName / saleNo
     */
    private List<VerifyRecordVO> enrichRecords(List<VerifyRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. scenicName
        Set<Long> scenicIds = records.stream()
                .map(VerifyRecord::getScenicId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> scenicNameMap = new HashMap<>();
        if (!scenicIds.isEmpty()) {
            LambdaQueryWrapper<Scenic> sw = new LambdaQueryWrapper<>();
            sw.in(Scenic::getId, scenicIds)
              .isNull(Scenic::getDeletedAt)
              .select(Scenic::getId, Scenic::getName);
            scenicMapper.selectList(sw).forEach(s -> scenicNameMap.put(s.getId(), s.getName()));
        }
        // 2. saleNo
        Set<Long> saleIds = records.stream()
                .map(VerifyRecord::getSaleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> saleNoMap = new HashMap<>();
        if (!saleIds.isEmpty()) {
            LambdaQueryWrapper<Sale> saleWrapper = new LambdaQueryWrapper<>();
            saleWrapper.in(Sale::getId, saleIds)
                       .isNull(Sale::getDeletedAt)
                       .select(Sale::getId, Sale::getSaleNo);
            saleMapper.selectList(saleWrapper).forEach(s -> saleNoMap.put(s.getId(), s.getSaleNo()));
        }
        // 3. 装配
        return records.stream().map(r -> {
            VerifyRecordVO vo = VerifyRecordVO.from(r);
            vo.setScenicName(scenicNameMap.get(r.getScenicId()));
            vo.setSaleNo(saleNoMap.get(r.getSaleId()));
            return vo;
        }).collect(Collectors.toList());
    }
}

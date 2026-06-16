package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.util.NoGenerator;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.VoucherQueryDTO;
import com.ainanning.ticketing.dto.VoucherReissueDTO;
import com.ainanning.ticketing.dto.VoucherRevokeDTO;
import com.ainanning.ticketing.entity.Sale;
import com.ainanning.ticketing.entity.SaleItem;
import com.ainanning.ticketing.entity.Order;
import com.ainanning.ticketing.entity.OrderItem;
import com.ainanning.ticketing.entity.Scenic;
import com.ainanning.ticketing.entity.Ticket;
import com.ainanning.ticketing.entity.Voucher;
import com.ainanning.ticketing.mapper.OrderMapper;
import com.ainanning.ticketing.mapper.SaleMapper;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.mapper.VoucherMapper;
import com.ainanning.ticketing.service.VoucherService;
import com.ainanning.ticketing.vo.VoucherStatsVO;
import com.ainanning.ticketing.vo.VoucherVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 票据业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>每售出 1 张就生成 1 条 voucher 记录（区别于 sale_item.voucherCodes 逗号串），
 *       单张作废 / 补发 / 核销都按 ID 精准定位</li>
 *   <li>状态单向迁移：{@code 待使用 → 已使用 / 已退 / 已作废}；退票/作废必须基于"待使用"状态</li>
 *   <li>{@code validFrom/validTo} 在出票时由票种 validDays 计算填入，检票模块直接读取不再二次计算</li>
 *   <li>补发：原票标记不变，新增"待使用"新码；新旧码不重复（voucherCode 全表唯一）</li>
 *   <li>打印计数：使用 SQL 自增避免并发 ABA</li>
 *   <li>提供 {@link #issue} / {@link #markRefunded} / {@link #markUsed} 三个内部接口供
 *       {@code SaleService} / {@code VerifyService} 调用</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherMapper voucherMapper;
    private final SaleMapper saleMapper;
    private final OrderMapper orderMapper;
    private final ScenicMapper scenicMapper;

    @Override
    public PageVO<VoucherVO> page(VoucherQueryDTO query) {
        log.info("[票据] 分页查询 scenicId={}, ticketId={}, status={}, [{} ~ {}]",
                query.getScenicId(), query.getTicketId(), query.getStatus(),
                query.getDateFrom(), query.getDateTo());

        // 1. 分页
        Page<Voucher> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 条件
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Voucher::getDeletedAt);
        if (query.getScenicId() != null) {
            wrapper.eq(Voucher::getScenicId, query.getScenicId());
        }
        if (query.getTicketId() != null) {
            wrapper.eq(Voucher::getTicketId, query.getTicketId());
        }
        if (query.getSaleId() != null) {
            wrapper.eq(Voucher::getSaleId, query.getSaleId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Voucher::getStatus, query.getStatus());
        }
        if (query.getInventoryDate() != null) {
            wrapper.eq(Voucher::getInventoryDate, query.getInventoryDate());
        }
        if (query.getDateFrom() != null) {
            wrapper.ge(Voucher::getInventoryDate, query.getDateFrom());
        }
        if (query.getDateTo() != null) {
            wrapper.le(Voucher::getInventoryDate, query.getDateTo());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Voucher::getVoucherCode, kw)
                    .or().like(Voucher::getVisitorName, kw)
                    .or().like(Voucher::getVisitorPhone, kw));
        }
        wrapper.orderByDesc(Voucher::getId);

        // 3. 查询
        Page<Voucher> result = voucherMapper.selectPage(page, wrapper);

        // 4. 注入 saleNo
        List<VoucherVO> records = enrichRecords(result.getRecords());

        return PageVO.of(result, records);
    }

    @Override
    public VoucherVO getById(Long id) {
        log.info("[票据] 查询详情 id={}", id);
        Voucher entity = voucherMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.VOUCHER_NOT_FOUND);
        }
        return enrichRecords(Collections.singletonList(entity)).get(0);
    }

    @Override
    public VoucherVO getByCode(String voucherCode) {
        log.info("[票据] 按码查询 code={}", voucherCode);
        if (!StringUtils.hasText(voucherCode)) {
            throw new BusinessException(ResultCode.VOUCHER_NOT_FOUND);
        }
        Voucher entity = voucherMapper.selectByVoucherCode(voucherCode);
        if (entity == null) {
            throw new BusinessException(ResultCode.VOUCHER_NOT_FOUND);
        }
        return enrichRecords(Collections.singletonList(entity)).get(0);
    }

    @Override
    public List<VoucherVO> listBySaleItemId(Long saleItemId) {
        log.info("[票据] 按销售明细查 saleItemId={}", saleItemId);
        if (saleItemId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Voucher::getDeletedAt)
               .eq(Voucher::getSaleItemId, saleItemId)
               .orderByAsc(Voucher::getId);
        return enrichRecords(voucherMapper.selectList(wrapper));
    }

    @Override
    public List<VoucherVO> listBySaleId(Long saleId) {
        log.info("[票据] 按销售单查 saleId={}", saleId);
        if (saleId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Voucher::getDeletedAt)
               .eq(Voucher::getSaleId, saleId)
               .orderByAsc(Voucher::getSaleItemId)
               .orderByAsc(Voucher::getId);
        return enrichRecords(voucherMapper.selectList(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revoke(VoucherRevokeDTO dto) {
        log.info("[票据] 批量作废 ids={}, reason={}, staff={}",
                dto.getIds(), dto.getReason(), dto.getStaffName());

        int successCount = 0;
        for (Long id : dto.getIds()) {
            Voucher v = voucherMapper.selectById(id);
            if (v == null || v.getDeletedAt() != null) {
                log.warn("[票据] 作废跳过 - 不存在 id={}", id);
                continue;
            }
            if (!Voucher.STATUS_UNUSED.equals(v.getStatus())) {
                // 已使用 / 已退 / 已作废 都跳过，不抛错（保证批量幂等）
                log.warn("[票据] 作废跳过 - 状态非法 id={}, status={}", id, v.getStatus());
                continue;
            }
            Voucher upd = new Voucher();
            upd.setId(id);
            upd.setStatus(Voucher.STATUS_REVOKED);
            upd.setRevokeTime(LocalDateTime.now());
            upd.setRevokeReason(dto.getReason());
            upd.setRevokeStaff(dto.getStaffName());
            int rows = voucherMapper.updateById(upd);
            if (rows > 0) {
                successCount++;
            }
        }
        log.info("[票据] 批量作废完成 请求 {} 张, 成功 {} 张", dto.getIds().size(), successCount);
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<VoucherVO> reissue(VoucherReissueDTO dto) {
        log.info("[票据] 批量补发 sourceIds={}, reason={}, staff={}",
                dto.getSourceIds(), dto.getReason(), dto.getStaffName());

        List<VoucherVO> newVouchers = new ArrayList<>();
        for (Long sourceId : dto.getSourceIds()) {
            Voucher src = voucherMapper.selectById(sourceId);
            if (src == null || src.getDeletedAt() != null) {
                throw new BusinessException(ResultCode.VOUCHER_NOT_FOUND,
                        "原票据不存在: " + sourceId);
            }
            // 仅允许"已退 / 已作废"补发；待使用 / 已使用 不应补发
            if (!Voucher.STATUS_REFUND.equals(src.getStatus())
                    && !Voucher.STATUS_REVOKED.equals(src.getStatus())) {
                throw new BusinessException(ResultCode.VOUCHER_STATUS_INVALID,
                        "原票据状态不可补发: " + src.getStatus());
            }
            // 已过期也不能补发（避免补发后立刻又过期）
            if (src.getValidTo() != null && src.getValidTo().isBefore(LocalDate.now())) {
                throw new BusinessException(ResultCode.VOUCHER_EXPIRED,
                        "原票据已过期，不能补发: " + sourceId);
            }

            // 补发：复制字段 + 生成新码
            Voucher newV = new Voucher();
            newV.setVoucherCode(generateVoucherCode());
            newV.setQrCode(newV.getVoucherCode());
            newV.setStatus(Voucher.STATUS_UNUSED);
            newV.setSaleId(src.getSaleId());
            newV.setSaleItemId(src.getSaleItemId());
            newV.setTicketId(src.getTicketId());
            newV.setTicketName(src.getTicketName());
            newV.setScenicId(src.getScenicId());
            newV.setScenicName(src.getScenicName());
            newV.setInventoryId(src.getInventoryId());
            newV.setInventoryDate(src.getInventoryDate());
            newV.setValidFrom(src.getValidFrom());
            newV.setValidTo(src.getValidTo());
            newV.setVisitorName(src.getVisitorName());
            newV.setVisitorPhone(src.getVisitorPhone());
            newV.setVisitorIdCard(src.getVisitorIdCard());
            newV.setPrintCount(0);
            newV.setIssueTime(LocalDateTime.now());
            newV.setRemark("补发自原票据 " + src.getVoucherCode()
                    + (StringUtils.hasText(dto.getReason()) ? " - " + dto.getReason() : ""));
            int rows = voucherMapper.insert(newV);
            if (rows == 0) {
                log.error("[票据] 补发插入失败 sourceId={}", sourceId);
                throw new BusinessException(ResultCode.VOUCHER_REISSUE_FAILED);
            }
            newVouchers.add(VoucherVO.from(newV));
        }
        log.info("[票据] 补发完成 {} 张", newVouchers.size());
        return newVouchers;
    }

    @Override
    public int markPrinted(List<Long> ids) {
        log.info("[票据] 标记打印 ids={}", ids);
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long id : ids) {
            if (voucherMapper.incrementPrintCount(id) > 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public VoucherStatsVO stats(Long scenicId, Long ticketId, Long saleId) {
        log.info("[票据] 统计 scenicId={}, ticketId={}, saleId={}", scenicId, ticketId, saleId);
        long unused = countByStatusWith(scenicId, ticketId, saleId, Voucher.STATUS_UNUSED);
        long used = countByStatusWith(scenicId, ticketId, saleId, Voucher.STATUS_USED);
        long refund = countByStatusWith(scenicId, ticketId, saleId, Voucher.STATUS_REFUND);
        long revoked = countByStatusWith(scenicId, ticketId, saleId, Voucher.STATUS_REVOKED);
        long total = unused + used + refund + revoked;
        double rate = total == 0 ? 0.0
                : Math.round((double) used * 10000 / total) / 100.0;
        return VoucherStatsVO.builder()
                .scenicId(scenicId)
                .ticketId(ticketId)
                .saleId(saleId)
                .unusedCount(unused)
                .usedCount(used)
                .refundCount(refund)
                .revokedCount(revoked)
                .totalCount(total)
                .usageRate(rate)
                .build();
    }

    /* ====================== 供 SaleService / VerifyService 调用的内部接口 ====================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> issue(Sale sale, SaleItem item, Ticket ticket, int quantity) {
        log.info("[票据] 出票 saleId={}, saleItemId={}, quantity={}",
                sale.getId(), item.getId(), quantity);
        if (quantity <= 0) {
            return Collections.emptyList();
        }
        // 园区名（用于冗余）
        Scenic scenic = scenicMapper.selectById(sale.getScenicId());
        String scenicName = scenic == null ? null : scenic.getName();

        // 有效期：[inventoryDate, inventoryDate + validDays - 1]
        LocalDate validFrom = item.getInventoryDate();
        LocalDate validTo = validFrom.plusDays(Math.max(0, ticket.getValidDays() - 1));

        List<String> codes = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            Voucher v = new Voucher();
            v.setVoucherCode(generateVoucherCode());
            v.setQrCode(v.getVoucherCode());
            v.setStatus(Voucher.STATUS_UNUSED);
            v.setSourceType(Voucher.SOURCE_SALE);
            v.setSaleId(sale.getId());
            v.setSaleItemId(item.getId());
            v.setTicketId(ticket.getId());
            v.setTicketName(ticket.getName());
            v.setScenicId(sale.getScenicId());
            v.setScenicName(scenicName);
            v.setInventoryId(item.getInventoryId());
            v.setInventoryDate(item.getInventoryDate());
            v.setValidFrom(validFrom);
            v.setValidTo(validTo);
            v.setVisitorName(sale.getVisitorName());
            v.setVisitorPhone(sale.getVisitorPhone());
            v.setVisitorIdCard(sale.getVisitorIdCard());
            v.setPrintCount(0);
            v.setIssueTime(sale.getSaleTime() != null ? sale.getSaleTime() : LocalDateTime.now());
            v.setRemark(sale.getRemark());
            int rows = voucherMapper.insert(v);
            if (rows == 0) {
                log.error("[票据] 出票失败 saleId={}, ticketId={}", sale.getId(), ticket.getId());
                throw new BusinessException(ResultCode.VOUCHER_SAVE_FAILED);
            }
            codes.add(v.getVoucherCode());
        }
        log.info("[票据] 出票成功 saleId={}, 票数={}", sale.getId(), codes.size());
        return codes;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> issueForOrder(Order order, OrderItem item, Ticket ticket, int quantity) {
        log.info("[票据] 出票（订单）orderId={}, orderItemId={}, quantity={}",
                order.getId(), item.getId(), quantity);
        if (quantity <= 0) {
            return Collections.emptyList();
        }
        // 园区名（用于冗余）
        Scenic scenic = scenicMapper.selectById(order.getScenicId());
        String scenicName = scenic == null ? null : scenic.getName();

        // 有效期：[inventoryDate, inventoryDate + validDays - 1]
        LocalDate validFrom = item.getInventoryDate();
        LocalDate validTo = validFrom.plusDays(Math.max(0, ticket.getValidDays() - 1));

        List<String> codes = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            Voucher v = new Voucher();
            v.setVoucherCode(generateVoucherCode());
            v.setQrCode(v.getVoucherCode());
            v.setStatus(Voucher.STATUS_UNUSED);
            v.setSourceType(Voucher.SOURCE_ORDER);
            // 订单票据使用专属字段，避免与 Sale 语义混淆
            v.setOrderId(order.getId());
            v.setOrderItemId(item.getId());
            // 兼容：sale_item_id 字段保留作为 Item.id 的兼容映射，
            // 这样旧版按 sale_item_id 查询的 SQL（如 markRefunded、countNonUnusedBySaleItemIds）
            // 在 Order 来源下也能正确命中，无需立即改造所有查询
            v.setSaleItemId(item.getId());
            v.setTicketId(ticket.getId());
            v.setTicketName(ticket.getName());
            v.setScenicId(order.getScenicId());
            v.setScenicName(scenicName);
            v.setInventoryId(item.getInventoryId());
            v.setInventoryDate(item.getInventoryDate());
            v.setValidFrom(validFrom);
            v.setValidTo(validTo);
            v.setVisitorName(order.getContactName());
            v.setVisitorPhone(order.getContactPhone());
            v.setVisitorIdCard(order.getContactIdCard());
            v.setPrintCount(0);
            v.setIssueTime(order.getOrderTime() != null ? order.getOrderTime() : LocalDateTime.now());
            v.setRemark(order.getRemark());
            int rows = voucherMapper.insert(v);
            if (rows == 0) {
                log.error("[票据] 出票（订单）失败 orderId={}, ticketId={}", order.getId(), ticket.getId());
                throw new BusinessException(ResultCode.VOUCHER_SAVE_FAILED);
            }
            codes.add(v.getVoucherCode());
        }
        log.info("[票据] 出票（订单）成功 orderId={}, 票数={}", order.getId(), codes.size());
        return codes;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markRefunded(Long saleItemId, int quantity) {
        log.info("[票据] 标记退票 saleItemId={}, quantity={}", saleItemId, quantity);
        if (saleItemId == null || quantity <= 0) {
            return 0;
        }
        // 取该明细下所有"待使用"的票
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Voucher::getDeletedAt)
               .eq(Voucher::getSaleItemId, saleItemId)
               .eq(Voucher::getStatus, Voucher.STATUS_UNUSED)
               .orderByAsc(Voucher::getId);
        List<Voucher> candidates = voucherMapper.selectList(wrapper);
        if (candidates.size() < quantity) {
            throw new BusinessException(ResultCode.SALE_REFUND_EXCEED,
                        "明细 " + saleItemId + " 可退票据仅剩 " + candidates.size()
                                + " 张，需求 " + quantity + " 张");
        }
        int count = 0;
        for (int i = 0; i < quantity; i++) {
            Voucher v = candidates.get(i);
            Voucher upd = new Voucher();
            upd.setId(v.getId());
            upd.setStatus(Voucher.STATUS_REFUND);
            upd.setRemark(appendRemark(v.getRemark(), "已退票"));
            if (voucherMapper.updateById(upd) > 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markUsed(String voucherCode, Long staffId, String staffName,
                            Long deviceId, String deviceName) {
        log.info("[票据] 标记已使用 code={}, staff={}", voucherCode, staffName);
        // 条件更新：仅当 status='待使用' 时才改为 '已使用'，
        // 避免 select→judge→update 模式在并发下两次核销都返回 true
        int affected = voucherMapper.markUsedIfUnused(voucherCode, staffId, staffName, deviceId, deviceName);
        if (affected == 0) {
            // 票据不存在 / 已删除 / 状态不是"待使用" 都视为核销失败
            log.info("[票据] 标记已使用失败 code={}（不存在/已核销/非待使用）", voucherCode);
            return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllRefundedBySaleId(Long saleId) {
        log.info("[票据] 全单退票 saleId={}", saleId);
        if (saleId == null) {
            return 0;
        }
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Voucher::getDeletedAt)
               .eq(Voucher::getSaleId, saleId)
               .eq(Voucher::getStatus, Voucher.STATUS_UNUSED);
        List<Voucher> candidates = voucherMapper.selectList(wrapper);
        int count = 0;
        for (Voucher v : candidates) {
            Voucher upd = new Voucher();
            upd.setId(v.getId());
            upd.setStatus(Voucher.STATUS_REFUND);
            upd.setRemark(appendRemark(v.getRemark(), "已退票"));
            if (voucherMapper.updateById(upd) > 0) {
                count++;
            }
        }
        return count;
    }

    /* ====================== 私有方法 ====================== */

    /**
     * 按状态统计（可叠加 scenic/ticket/sale 维度过滤）
     */
    private long countByStatusWith(Long scenicId, Long ticketId, Long saleId, String status) {
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Voucher::getDeletedAt).eq(Voucher::getStatus, status);
        if (scenicId != null) {
            wrapper.eq(Voucher::getScenicId, scenicId);
        }
        if (ticketId != null) {
            wrapper.eq(Voucher::getTicketId, ticketId);
        }
        if (saleId != null) {
            wrapper.eq(Voucher::getSaleId, saleId);
        }
        return voucherMapper.selectCount(wrapper);
    }

    /**
     * Entity 列表 → VO 列表，注入 saleNo / orderNo
     *
     * <p>根据 {@code sourceType} 分发：SALE 来源查 sale 表，ORDER 来源查 order 表。</p>
     */
    private List<VoucherVO> enrichRecords(List<Voucher> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 批量查 saleNo（SALE 来源）
        Set<Long> saleIds = records.stream()
                .filter(r -> Voucher.SOURCE_SALE.equals(r.getSourceType()))
                .map(Voucher::getSaleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> saleNoMap = new HashMap<>();
        if (!saleIds.isEmpty()) {
            LambdaQueryWrapper<Sale> sw = new LambdaQueryWrapper<>();
            sw.in(Sale::getId, saleIds)
              .isNull(Sale::getDeletedAt)
              .select(Sale::getId, Sale::getSaleNo);
            saleMapper.selectList(sw).forEach(s -> saleNoMap.put(s.getId(), s.getSaleNo()));
        }
        // 2. 批量查 orderNo（ORDER 来源）
        Set<Long> orderIds = records.stream()
                .filter(r -> Voucher.SOURCE_ORDER.equals(r.getSourceType()))
                .map(Voucher::getOrderId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> orderNoMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<Order> ow = new LambdaQueryWrapper<>();
            ow.in(Order::getId, orderIds)
              .isNull(Order::getDeletedAt)
              .select(Order::getId, Order::getOrderNo);
            orderMapper.selectList(ow).forEach(o -> orderNoMap.put(o.getId(), o.getOrderNo()));
        }
        // 3. 装配
        return records.stream().map(r -> {
            VoucherVO vo = VoucherVO.from(r);
            vo.setSaleNo(saleNoMap.get(r.getSaleId()));
            vo.setOrderNo(orderNoMap.get(r.getOrderId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 生成单个票据码：{@code V + yyyyMMddHHmmss + 4 位随机}
     *
     * <p>使用 NoGenerator 自动重试兜底，DB 唯一键冲突时不再让整事务回滚。</p>
     */
    private String generateVoucherCode() {
        return NoGenerator.generateWithRetry("V", NoGenerator.nowCompact(), 4,
                code -> voucherMapper.selectByVoucherCode(code) != null);
    }

    /**
     * 在原 remark 末尾追加一段（避免覆盖原信息）
     */
    private String appendRemark(String original, String addition) {
        if (!StringUtils.hasText(original)) {
            return addition;
        }
        return original + " | " + addition;
    }
}

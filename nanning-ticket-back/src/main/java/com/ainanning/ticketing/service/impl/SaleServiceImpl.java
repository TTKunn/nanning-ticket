package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.util.NoGenerator;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SaleCreateDTO;
import com.ainanning.ticketing.dto.SaleItemCreateDTO;
import com.ainanning.ticketing.dto.SaleQueryDTO;
import com.ainanning.ticketing.dto.SaleRefundDTO;
import com.ainanning.ticketing.entity.Inventory;
import com.ainanning.ticketing.entity.Sale;
import com.ainanning.ticketing.entity.SaleItem;
import com.ainanning.ticketing.entity.Scenic;
import com.ainanning.ticketing.entity.Ticket;
import com.ainanning.ticketing.mapper.InventoryMapper;
import com.ainanning.ticketing.mapper.SaleItemMapper;
import com.ainanning.ticketing.mapper.SaleMapper;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.mapper.TicketMapper;
import com.ainanning.ticketing.mapper.VoucherMapper;
import com.ainanning.ticketing.service.SaleService;
import com.ainanning.ticketing.service.VoucherService;
import com.ainanning.ticketing.vo.SaleItemVO;
import com.ainanning.ticketing.vo.SaleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 窗口售票业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>所有写操作使用 {@code @Transactional} 保证事务（主单 + 明细 + 库存的"三表同改"）</li>
 *   <li>{@code saleNo} = {@code S + yyyyMMdd + 4 位流水}；使用 {@code yyyyMMdd} 前缀 + 时间戳后缀避免同日重复</li>
 *   <li>创建时按明细逐条校验：票种在售、对应日期库存开放、可用库存充足</li>
 *   <li>创建后扣减库存：{@code sold += quantity; available = total - sold - reserved}（DB 字段持久化）</li>
 *   <li>退票：按明细累加 {@code refundQuantity}，恢复库存，更新主单 {@code refundAmount} 与状态</li>
 *   <li>票据码（{@code voucherCodes}）用 {@code V + yyyyMMddHHmmss + 4 位随机}，由 Service 自动生成</li>
 *   <li>N+1 防护：分页后批量加载明细与园区名/票种名</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleMapper saleMapper;
    private final SaleItemMapper saleItemMapper;
    private final TicketMapper ticketMapper;
    private final InventoryMapper inventoryMapper;
    private final ScenicMapper scenicMapper;
    private final VoucherMapper voucherMapper;
    private final VoucherService voucherService;

    /* 状态常量 */
    private static final String STATUS_PAID     = "已支付";
    private static final String STATUS_PARTIAL  = "部分退票";
    private static final String STATUS_REFUNDED = "已退票";
    private static final String STATUS_CANCEL   = "已取消";

    /* 业务类型 */
    private static final String TYPE_SALE = "售票";

    /* 支付方式白名单 */
    private static final List<String> PAYMENT_WHITELIST =
            Arrays.asList("现金", "微信", "支付宝", "银行卡", "余额");

    /* 库存状态白名单（可售：开放；不可售：关闭/售罄） */
    private static final String INV_STATUS_OPEN = "开放";

    @Override
    public PageVO<SaleVO> page(SaleQueryDTO query) {
        log.info("[销售] 分页查询 scenicId={}, status={}, paymentMethod={}, keyword={}, [{} ~ {}]",
                query.getScenicId(), query.getStatus(), query.getPaymentMethod(),
                query.getKeyword(), query.getDateFrom(), query.getDateTo());

        // 1. 构造分页对象
        Page<Sale> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Sale> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Sale::getDeletedAt);
        if (query.getScenicId() != null) {
            wrapper.eq(Sale::getScenicId, query.getScenicId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Sale::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getPaymentMethod())) {
            wrapper.eq(Sale::getPaymentMethod, query.getPaymentMethod());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Sale::getSaleNo, kw)
                    .or().like(Sale::getVisitorName, kw)
                    .or().like(Sale::getVisitorPhone, kw));
        }
        if (query.getDateFrom() != null) {
            // saleTime >= dateFrom 00:00:00
            wrapper.ge(Sale::getSaleTime, query.getDateFrom().atStartOfDay());
        }
        if (query.getDateTo() != null) {
            // saleTime <= dateTo 23:59:59
            wrapper.le(Sale::getSaleTime, query.getDateTo().atTime(23, 59, 59));
        }
        wrapper.orderByDesc(Sale::getSaleTime).orderByDesc(Sale::getId);

        // 3. 执行分页查询
        Page<Sale> result = saleMapper.selectPage(page, wrapper);

        // 4. Entity → VO（含明细 + scenicName）
        List<SaleVO> records = enrichRecords(result.getRecords());

        return PageVO.of(result, records);
    }

    @Override
    public SaleVO getById(Long id) {
        log.info("[销售] 查询详情 id={}", id);
        Sale entity = getActiveById(id);
        return enrichRecords(Collections.singletonList(entity)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SaleCreateDTO dto) {
        log.info("[销售] 新建 scenicId={}, paymentMethod={}, items={}",
                dto.getScenicId(), dto.getPaymentMethod(), dto.getItems().size());

        // 1. 校验园区
        validateScenicActive(dto.getScenicId());

        // 2. 校验支付方式
        validatePaymentMethod(dto.getPaymentMethod());

        // 3. 校验明细条数
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException(ResultCode.SALE_NO_ITEMS);
        }

        // 4. 生成 saleNo
        String saleNo = generateSaleNo();

        // 5. 处理明细
        List<SaleItem> items = new ArrayList<>(dto.getItems().size());
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        int itemCount = 0;

        for (SaleItemCreateDTO itemDto : dto.getItems()) {
            // 5.1 校验票种
            Ticket ticket = validateTicketActive(itemDto.getTicketId());
            if (!ticket.getScenicId().equals(dto.getScenicId())) {
                throw new BusinessException(ResultCode.SALE_TICKET_MISMATCH,
                        "票种 [" + ticket.getName() + "] 不属于园区 " + dto.getScenicId());
            }

            // 5.2 校验日期
            if (itemDto.getInventoryDate().isBefore(LocalDate.now())) {
                throw new BusinessException(ResultCode.SALE_INVENTORY_DATE_INVALID,
                        "入场日期不能早于今天: " + itemDto.getInventoryDate());
            }

            // 5.3 校验并锁定库存
            Inventory inventory = validateAndLockInventory(itemDto.getTicketId(),
                    itemDto.getInventoryDate(), itemDto.getQuantity());

            // 5.4 计算金额
            BigDecimal unitPrice = itemDto.getUnitPrice() != null
                    ? itemDto.getUnitPrice() : ticket.getPrice();
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ResultCode.SALE_AMOUNT_INVALID, "销售单价不能为负");
            }
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = itemDto.getDiscountAmount() != null
                    ? itemDto.getDiscountAmount() : BigDecimal.ZERO;
            if (discount.compareTo(BigDecimal.ZERO) < 0
                    || discount.compareTo(subtotal) > 0) {
                throw new BusinessException(ResultCode.SALE_AMOUNT_INVALID,
                        "优惠金额必须在 [0, " + subtotal + "] 之间");
            }
            BigDecimal finalAmount = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

            // 5.5 构造 SaleItem
            SaleItem item = new SaleItem();
            item.setTicketId(ticket.getId());
            item.setTicketName(ticket.getName());
            item.setScenicId(ticket.getScenicId());
            item.setInventoryId(inventory.getId());
            item.setInventoryDate(itemDto.getInventoryDate());
            item.setUnitPrice(unitPrice);
            item.setQuantity(itemDto.getQuantity());
            item.setSubtotalAmount(subtotal);
            item.setRuleIds(joinList(itemDto.getRuleIds() != null
                    ? itemDto.getRuleIds() : parseCsvLongs(ticket.getRuleIds())));
            item.setDiscountAmount(discount);
            item.setFinalAmount(finalAmount);
            // 票据码：售出后再生成（需要 saleId/itemId，由 VoucherService 统一管理）
            item.setVoucherCodes(null);
            item.setRefundQuantity(0);
            item.setRefundAmount(BigDecimal.ZERO);
            item.setRemark(itemDto.getRemark());
            items.add(item);

            totalAmount = totalAmount.add(subtotal);
            discountAmount = discountAmount.add(discount);
            itemCount += itemDto.getQuantity();

            // 5.6 扣减库存
            decrementInventory(inventory, itemDto.getQuantity());
        }

        BigDecimal paidAmount = totalAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        // 6. 构造 Sale 主单
        Sale sale = new Sale();
        sale.setSaleNo(saleNo);
        sale.setScenicId(dto.getScenicId());
        sale.setWindowId(dto.getWindowId());
        sale.setWindowName(dto.getWindowName());
        sale.setSalespersonId(dto.getSalespersonId());
        sale.setSalespersonName(dto.getSalespersonName());
        sale.setVisitorName(dto.getVisitorName());
        sale.setVisitorPhone(dto.getVisitorPhone());
        sale.setVisitorIdCard(dto.getVisitorIdCard());
        sale.setSaleType(TYPE_SALE);
        sale.setPaymentMethod(dto.getPaymentMethod());
        sale.setTotalAmount(totalAmount);
        sale.setDiscountAmount(discountAmount);
        sale.setPaidAmount(paidAmount);
        sale.setRefundAmount(BigDecimal.ZERO);
        sale.setItemCount(itemCount);
        sale.setStatus(STATUS_PAID);
        sale.setSaleTime(dto.getSaleTime() != null ? dto.getSaleTime() : LocalDateTime.now());
        sale.setRemark(dto.getRemark());

        // 7. 插入主单
        int rows = saleMapper.insert(sale);
        if (rows == 0) {
            log.error("[销售] 插入主单失败 saleNo={}", saleNo);
            throw new BusinessException(ResultCode.SALE_SAVE_FAILED);
        }
        // 8. 批量插入明细（设置 saleId），并出票
        for (SaleItem item : items) {
            item.setSaleId(sale.getId());
            saleItemMapper.insert(item);
            // 8.1 出票：每张票生成 voucher 记录（与本事务保持一致，回滚一并回滚）
            //     ticket 已在创建明细时校验过，这里重新 select 取最新数据传给 voucher 模块
            Ticket ticket = ticketMapper.selectById(item.getTicketId());
            if (ticket == null) {
                throw new BusinessException(ResultCode.TICKET_NOT_FOUND,
                        "出票时票种已被删除: " + item.getTicketId());
            }
            List<String> codes = voucherService.issue(sale, item, ticket, item.getQuantity());
            // 8.2 回写 voucherCodes（保留冗余，方便后续检票 FIND_IN_SET 反查）
            item.setVoucherCodes(joinList(codes));
            saleItemMapper.updateById(item);
        }

        log.info("[销售] 新建成功 id={}, saleNo={}, totalAmount={}, paidAmount={}, itemCount={}",
                sale.getId(), saleNo, totalAmount, paidAmount, itemCount);
        return sale.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id, SaleRefundDTO dto) {
        log.info("[销售] 退票 id={}, 模式={}", id,
                (dto.getItems() == null || dto.getItems().isEmpty()) ? "整单" : "部分");

        Sale sale = getActiveById(id);

        // 1. 校验主单状态
        if (STATUS_REFUNDED.equals(sale.getStatus())) {
            throw new BusinessException(ResultCode.SALE_ALREADY_REFUNDED);
        }
        if (STATUS_CANCEL.equals(sale.getStatus())) {
            throw new BusinessException(ResultCode.SALE_STATUS_INVALID, "已取消订单无法退票");
        }

        // 2. 加载所有有效明细
        List<SaleItem> allItems = saleItemMapper.selectActiveBySaleId(id);
        if (allItems.isEmpty()) {
            throw new BusinessException(ResultCode.SALE_ITEM_NOT_FOUND);
        }
        Map<Long, SaleItem> itemMap = allItems.stream()
                .collect(Collectors.toMap(SaleItem::getId, i -> i));

        // 3. 计算本次退票明细
        List<SaleItem> toRefund;
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            // 整单退：所有未全退的明细
            toRefund = allItems.stream()
                    .filter(i -> i.getQuantity() != null
                            && i.getRefundQuantity() != null
                            && i.getRefundQuantity() < i.getQuantity())
                    .collect(Collectors.toList());
        } else {
            toRefund = new ArrayList<>();
            for (SaleRefundDTO.RefundItem ri : dto.getItems()) {
                SaleItem item = itemMap.get(ri.getSaleItemId());
                if (item == null) {
                    throw new BusinessException(ResultCode.SALE_ITEM_NOT_FOUND,
                            "明细不存在: " + ri.getSaleItemId());
                }
                int left = item.getQuantity() - item.getRefundQuantity();
                if (ri.getQuantity() > left) {
                    throw new BusinessException(ResultCode.SALE_REFUND_EXCEED,
                            "明细 " + item.getId() + " 可退数量仅剩 " + left);
                }
                toRefund.add(item);
            }
        }
        if (toRefund.isEmpty()) {
            log.warn("[销售] 退票无可退明细 id={}", id);
            return;
        }

        // 4. 校验：所有待退的 voucher 必须仍为"待使用"，否则不允许退
        //    （部分退时只校验本次将要退的票）
        List<Long> toRefundItemIds = toRefund.stream().map(SaleItem::getId).collect(Collectors.toList());
        long usedOrRefunded = voucherMapper.countNonUnusedBySaleItemIds(toRefundItemIds);
        if (usedOrRefunded > 0) {
            log.warn("[销售] 退票失败，存在已使用/已退 voucher saleId={}, itemIds={}", id, toRefundItemIds);
            throw new BusinessException(ResultCode.SALE_STATUS_INVALID,
                    "存在已被核销或已退的票据，不能退票");
        }

        // 4. 逐条退票：恢复库存 + 累加 refundQuantity/refundAmount
        BigDecimal addRefund = BigDecimal.ZERO;
        for (SaleItem item : toRefund) {
            int qty = (dto.getItems() != null && !dto.getItems().isEmpty())
                    ? findRefundQty(dto.getItems(), item.getId())
                    : item.getQuantity() - item.getRefundQuantity();
            if (qty <= 0) {
                continue;
            }
            // 4.1 恢复库存
            restoreInventory(item.getInventoryId(), qty);
            // 4.2 累加明细退票数与金额（按单价）
            BigDecimal refundAmt = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(qty))
                    .setScale(2, RoundingMode.HALF_UP);
            item.setRefundQuantity(item.getRefundQuantity() + qty);
            item.setRefundAmount(
                    (item.getRefundAmount() == null ? BigDecimal.ZERO : item.getRefundAmount())
                            .add(refundAmt));
            saleItemMapper.updateById(item);
            // 4.3 标记对应 voucher 为"已退"（与本事务同进退）
            voucherService.markRefunded(item.getId(), qty);

            addRefund = addRefund.add(refundAmt);
        }

        // 5. 更新主单
        sale.setRefundAmount(
                (sale.getRefundAmount() == null ? BigDecimal.ZERO : sale.getRefundAmount())
                        .add(addRefund));
        // 状态：所有明细均已全退 → 已退票；否则 → 部分退票
        //    注意：toRefund 中的 item 已被就地更新（refundQuantity 已加），与 allItems 共用引用
        boolean allRefunded = allItems.stream()
                .allMatch(i -> i.getRefundQuantity() >= i.getQuantity());
        sale.setStatus(allRefunded ? STATUS_REFUNDED : STATUS_PARTIAL);
        saleMapper.updateById(sale);

        log.info("[销售] 退票完成 id={}, 退 {} 张, 退金额={}, 新状态={}",
                id, qtyOf(toRefund, dto), addRefund, sale.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, String reason) {
        log.info("[销售] 取消 id={}, 原因={}", id, reason);

        Sale sale = getActiveById(id);
        if (STATUS_CANCEL.equals(sale.getStatus())) {
            log.info("[销售] 已是已取消状态，跳过 id={}", id);
            return;
        }
        if (STATUS_REFUNDED.equals(sale.getStatus())) {
            throw new BusinessException(ResultCode.SALE_STATUS_INVALID, "已退票订单无法取消");
        }
        if (STATUS_PARTIAL.equals(sale.getStatus())) {
            throw new BusinessException(ResultCode.SALE_STATUS_INVALID, "部分退票订单无法取消");
        }

        // 1. 加载明细并校验：所有 voucher 必须仍为"待使用"
        List<SaleItem> items = saleItemMapper.selectActiveBySaleId(id);
        if (!items.isEmpty()) {
            List<Long> itemIds = items.stream().map(SaleItem::getId).collect(Collectors.toList());
            long usedOrRefunded = voucherMapper.countNonUnusedBySaleItemIds(itemIds);
            if (usedOrRefunded > 0) {
                log.warn("[销售] 取消失败，存在已使用/已退 voucher saleId={}", id);
                throw new BusinessException(ResultCode.SALE_STATUS_INVALID,
                        "存在已被核销或已退的票据，不能取消");
            }
        }

        // 2. 恢复所有未退明细的库存 + 标记 voucher 已退
        for (SaleItem item : items) {
            int left = item.getQuantity() - item.getRefundQuantity();
            if (left > 0) {
                restoreInventory(item.getInventoryId(), left);
                item.setRefundQuantity(item.getQuantity());
                item.setRefundAmount(item.getFinalAmount());
                saleItemMapper.updateById(item);
                voucherService.markRefunded(item.getId(), left);
            }
        }

        // 3. 更新主单
        sale.setStatus(STATUS_CANCEL);
        sale.setRefundAmount(sale.getPaidAmount());
        if (StringUtils.hasText(reason)) {
            sale.setRemark((sale.getRemark() == null ? "" : sale.getRemark() + " | ") + "取消：" + reason);
        }
        int rows = saleMapper.updateById(sale);
        if (rows == 0) {
            log.error("[销售] 取消失败 id={}", id);
            throw new BusinessException(ResultCode.SALE_CANCEL_FAILED);
        }
        log.info("[销售] 取消成功 id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info("[销售] 删除 id={}", id);
        Sale exist = getActiveById(id);
        // 业务规则：仅允许删除"已取消"状态的订单
        if (!STATUS_CANCEL.equals(exist.getStatus())) {
            throw new BusinessException(ResultCode.SALE_STATUS_INVALID,
                    "仅允许删除已取消订单，当前状态：" + exist.getStatus());
        }
        // 软删除主单
        Sale upd = new Sale();
        upd.setId(id);
        upd.setDeletedAt(LocalDateTime.now());
        int rows = saleMapper.updateById(upd);
        if (rows == 0) {
            log.error("[销售] 删除失败 id={}", id);
            throw new BusinessException(ResultCode.SALE_DELETE_FAILED);
        }
        log.info("[销售] 删除成功 id={}", id);
    }

    /* ====================== 私有方法 ====================== */

    /**
     * 获取有效销售单（不存在或已删除时抛业务异常）
     */
    private Sale getActiveById(Long id) {
        Sale entity = saleMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SALE_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 校验园区存在且未暂停
     */
    private void validateScenicActive(Long scenicId) {
        Scenic scenic = scenicMapper.selectById(scenicId);
        if (scenic == null || scenic.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SCENIC_NOT_FOUND);
        }
        if ("暂停运营".equals(scenic.getStatus())) {
            throw new BusinessException("园区已暂停运营，无法售票");
        }
    }

    /**
     * 校验支付方式白名单
     */
    private void validatePaymentMethod(String method) {
        if (!StringUtils.hasText(method) || !PAYMENT_WHITELIST.contains(method)) {
            throw new BusinessException(ResultCode.SALE_PAYMENT_INVALID);
        }
    }

    /**
     * 校验票种存在、在售、属于对应园区
     */
    private Ticket validateTicketActive(Long ticketId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || ticket.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.TICKET_NOT_FOUND);
        }
        if (!"在售".equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.SALE_TICKET_OFFLINE);
        }
        return ticket;
    }

    /**
     * 校验并"锁定"库存：使用 SELECT ... FOR UPDATE 行锁防止并发超卖
     *
     * <p>调用方必须处于事务中，锁会在事务提交/回滚时释放。
     * 返回有效库存记录，调用方负责扣减。</p>
     */
    private Inventory validateAndLockInventory(Long ticketId, LocalDate date, int quantity) {
        Inventory inv = inventoryMapper.selectForUpdate(ticketId, date);
        if (inv == null) {
            throw new BusinessException(ResultCode.SALE_INVENTORY_NOT_FOUND,
                    "票种 " + ticketId + " 在 " + date + " 无库存");
        }
        if (!INV_STATUS_OPEN.equals(inv.getStatus())) {
            throw new BusinessException(ResultCode.SALE_INVENTORY_CLOSED,
                    "库存当前状态：" + inv.getStatus());
        }
        int available = inv.getAvailable() == null ? 0 : inv.getAvailable();
        if (available < quantity) {
            throw new BusinessException(ResultCode.SALE_STOCK_NOT_ENOUGH,
                    "可用库存 " + available + " 张，需求 " + quantity + " 张");
        }
        return inv;
    }

    /**
     * 扣减库存：{@code sold += qty; available -= qty}
     */
    private void decrementInventory(Inventory inv, int qty) {
        int newSold = (inv.getSold() == null ? 0 : inv.getSold()) + qty;
        int newAvailable = Math.max(0, (inv.getTotal() == null ? 0 : inv.getTotal()) - newSold
                - (inv.getReserved() == null ? 0 : inv.getReserved()));
        inv.setSold(newSold);
        inv.setAvailable(newAvailable);
        // 售罄：自动切换 status
        if (newAvailable == 0 && INV_STATUS_OPEN.equals(inv.getStatus())) {
            inv.setStatus("售罄");
        }
        inventoryMapper.updateById(inv);
        log.debug("[销售] 扣减库存 id={}, sold={} -> {}, available={}",
                inv.getId(), newSold - qty, newSold, newAvailable);
    }

    /**
     * 恢复库存：{@code sold -= qty; available += qty}
     */
    private void restoreInventory(Long inventoryId, int qty) {
        if (inventoryId == null || qty <= 0) {
            return;
        }
        Inventory inv = inventoryMapper.selectById(inventoryId);
        if (inv == null || inv.getDeletedAt() != null) {
            log.warn("[销售] 退票时库存不存在 inventoryId={}", inventoryId);
            return;
        }
        int oldSold = inv.getSold() == null ? 0 : inv.getSold();
        int newSold = Math.max(0, oldSold - qty);
        int newAvailable = Math.max(0, (inv.getTotal() == null ? 0 : inv.getTotal()) - newSold
                - (inv.getReserved() == null ? 0 : inv.getReserved()));
        inv.setSold(newSold);
        inv.setAvailable(newAvailable);
        // 售罄 → 开放
        if ("售罄".equals(inv.getStatus()) && newAvailable > 0) {
            inv.setStatus(INV_STATUS_OPEN);
        }
        inventoryMapper.updateById(inv);
        log.debug("[销售] 恢复库存 id={}, sold={} -> {}, available={}",
                inv.getId(), oldSold, newSold, newAvailable);
    }

    /**
     * 解析 SaleRefundDTO 中指定明细的退票数量
     */
    private int findRefundQty(List<SaleRefundDTO.RefundItem> items, Long saleItemId) {
        return items.stream()
                .filter(i -> i.getSaleItemId().equals(saleItemId))
                .mapToInt(SaleRefundDTO.RefundItem::getQuantity)
                .findFirst()
                .orElse(0);
    }

    /**
     * 退票总张数（用于日志）
     */
    private int qtyOf(List<SaleItem> items, SaleRefundDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return items.stream()
                    .mapToInt(i -> i.getQuantity() - (i.getRefundQuantity() == null ? 0 : i.getRefundQuantity()))
                    .sum();
        }
        return dto.getItems().stream().mapToInt(SaleRefundDTO.RefundItem::getQuantity).sum();
    }

    /**
     * Entity 列表 → VO 列表，注入 scenicName + 明细列表
     */
    private List<SaleVO> enrichRecords(List<Sale> sales) {
        if (sales == null || sales.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 批量查 scenicName
        Set<Long> scenicIds = sales.stream()
                .map(Sale::getScenicId)
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
        // 2. 批量查明细
        List<Long> saleIds = sales.stream().map(Sale::getId).collect(Collectors.toList());
        Map<Long, List<SaleItemVO>> itemMap = new HashMap<>();
        if (!saleIds.isEmpty()) {
            List<SaleItem> allItems = saleItemMapper.selectActiveBySaleIds(saleIds);
            for (SaleItem item : allItems) {
                itemMap.computeIfAbsent(item.getSaleId(), k -> new ArrayList<>())
                        .add(SaleItemVO.from(item));
            }
        }
        // 3. 装配
        return sales.stream().map(s -> {
            SaleVO vo = SaleVO.from(s);
            vo.setScenicName(scenicNameMap.get(s.getScenicId()));
            vo.setItems(itemMap.getOrDefault(s.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 生成销售流水号：{@code S + yyyyMMddHHmmss + 3 位随机}
     *
     * <p>DB 唯一键冲突时由 NoGenerator 自动重试（最多 3 次）。</p>
     */
    private String generateSaleNo() {
        return NoGenerator.generateWithRetry("S", NoGenerator.nowCompact(), 3,
                no -> saleMapper.countBySaleNo(no) > 0);
    }

    /** 列表 → 逗号分隔字符串（过滤 null/空白） */
    private String joinList(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(java.util.Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    /** 逗号分隔字符串 → List&lt;Long&gt; */
    private List<Long> parseCsvLongs(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}

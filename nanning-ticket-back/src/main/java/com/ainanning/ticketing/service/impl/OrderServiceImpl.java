package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.util.NoGenerator;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.OrderCancelDTO;
import com.ainanning.ticketing.dto.OrderCreateDTO;
import com.ainanning.ticketing.dto.OrderItemCreateDTO;
import com.ainanning.ticketing.dto.OrderPayDTO;
import com.ainanning.ticketing.dto.OrderQueryDTO;
import com.ainanning.ticketing.dto.OrderRefundDTO;
import com.ainanning.ticketing.entity.Channel;
import com.ainanning.ticketing.entity.Inventory;
import com.ainanning.ticketing.entity.Order;
import com.ainanning.ticketing.entity.OrderItem;
import com.ainanning.ticketing.entity.Scenic;
import com.ainanning.ticketing.entity.Ticket;
import com.ainanning.ticketing.entity.Voucher;
import com.ainanning.ticketing.mapper.InventoryMapper;
import com.ainanning.ticketing.mapper.OrderItemMapper;
import com.ainanning.ticketing.mapper.OrderMapper;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.mapper.TicketMapper;
import com.ainanning.ticketing.mapper.VoucherMapper;
import com.ainanning.ticketing.service.OrderService;
import com.ainanning.ticketing.service.VoucherService;
import com.ainanning.ticketing.vo.OrderItemVO;
import com.ainanning.ticketing.vo.OrderStatsVO;
import com.ainanning.ticketing.vo.OrderVO;
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
 * 在线订单业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>本原型"创建即支付"：{@code create()} 内部走完下单 → 支付 → 出票全流程，
 *       客户在管理后台看到的就是"已出票"状态。生产环境要拆成 create/pay 两步异步</li>
 *   <li>支付/取消/退款/删除都使用 {@code @Transactional} 写操作（订单 + 明细 + voucher + 库存 同进退）</li>
 *   <li>状态机单向迁移：{@code 待支付 → 已出票 / 已取消}，{@code 已出票 → 已退款}（部分退款预留）</li>
 *   <li>退票校验：voucher 必须全部"待使用"（未被核销），否则报 {@code ORDER_HAS_VOUCHER_USED}</li>
 *   <li>出票复用 {@code VoucherService.issue}（与窗口售票共用同一出票逻辑）</li>
 *   <li>N+1 防护：分页后批量加载明细</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final TicketMapper ticketMapper;
    private final InventoryMapper inventoryMapper;
    private final ScenicMapper scenicMapper;
    private final VoucherMapper voucherMapper;
    private final VoucherService voucherService;

    /* 渠道白名单（与 Channel 模块保持一致，复用 Channel 实体常量） */
    private static final List<String> CHANNEL_WHITELIST = Arrays.asList(
            Channel.TYPE_OTA, Channel.TYPE_OFFICIAL, Channel.TYPE_MINI,
            Channel.TYPE_APP, Channel.TYPE_SHORT, Channel.TYPE_WECHAT, Channel.TYPE_OTHER);

    /* 支付方式白名单 */
    private static final List<String> PAYMENT_WHITELIST =
            Arrays.asList("现金", "微信", "支付宝", "银行卡", "余额");

    /* 库存状态白名单（可售） */
    private static final String INV_STATUS_OPEN = "开放";

    @Override
    public PageVO<OrderVO> page(OrderQueryDTO query) {
        log.info("[订单] 分页查询 scenicId={}, channel={}, status={}, keyword={}, [{} ~ {}]",
                query.getScenicId(), query.getChannelCode(), query.getStatus(),
                query.getKeyword(), query.getOrderDateFrom(), query.getOrderDateTo());

        Page<Order> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Order::getDeletedAt);
        if (query.getScenicId() != null) {
            wrapper.eq(Order::getScenicId, query.getScenicId());
        }
        if (StringUtils.hasText(query.getChannelCode())) {
            wrapper.eq(Order::getChannelCode, query.getChannelCode());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Order::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getPayMethod())) {
            wrapper.eq(Order::getPayMethod, query.getPayMethod());
        }
        if (StringUtils.hasText(query.getContactPhone())) {
            wrapper.eq(Order::getContactPhone, query.getContactPhone());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Order::getOrderNo, kw)
                    .or().like(Order::getContactName, kw)
                    .or().like(Order::getUserName, kw));
        }
        if (query.getOrderDateFrom() != null) {
            wrapper.ge(Order::getOrderTime, query.getOrderDateFrom().atStartOfDay());
        }
        if (query.getOrderDateTo() != null) {
            wrapper.le(Order::getOrderTime, query.getOrderDateTo().atTime(23, 59, 59));
        }
        if (query.getUseDateFrom() != null) {
            wrapper.ge(Order::getUseStartDate, query.getUseDateFrom());
        }
        if (query.getUseDateTo() != null) {
            wrapper.le(Order::getUseEndDate, query.getUseDateTo());
        }
        wrapper.orderByDesc(Order::getOrderTime).orderByDesc(Order::getId);

        Page<Order> result = orderMapper.selectPage(page, wrapper);
        List<OrderVO> records = enrichRecords(result.getRecords());
        return PageVO.of(result, records);
    }

    @Override
    public OrderVO getById(Long id) {
        log.info("[订单] 查询详情 id={}", id);
        Order entity = getActiveById(id);
        return enrichRecords(Collections.singletonList(entity)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderCreateDTO dto) {
        log.info("[订单] 创建并支付 scenicId={}, channel={}, items={}",
                dto.getScenicId(), dto.getChannelCode(),
                dto.getItems() == null ? 0 : dto.getItems().size());

        // 1. 校验园区
        Scenic scenic = validateScenicActive(dto.getScenicId());

        // 2. 校验渠道 + 支付方式
        validateChannel(dto.getChannelCode());
        validatePayMethod(dto.getPayMethod());

        // 3. 校验明细条数
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException(ResultCode.ORDER_NO_ITEMS);
        }

        // 4. 处理明细（同时扣库存）
        List<OrderItem> items = new ArrayList<>(dto.getItems().size());
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        int itemCount = 0;
        LocalDate useStart = null;
        LocalDate useEnd = null;
        String lastTicketName = null;

        for (OrderItemCreateDTO itemDto : dto.getItems()) {
            // 4.1 校验票种
            Ticket ticket = validateTicketActive(itemDto.getTicketId());
            if (!ticket.getScenicId().equals(dto.getScenicId())) {
                throw new BusinessException(ResultCode.ORDER_TICKET_MISMATCH,
                        "票种 [" + ticket.getName() + "] 不属于园区 " + dto.getScenicId());
            }
            // 4.2 校验日期
            if (itemDto.getInventoryDate().isBefore(LocalDate.now())) {
                throw new BusinessException(ResultCode.ORDER_INVENTORY_DATE_INVALID,
                        "入场日期不能早于今天: " + itemDto.getInventoryDate());
            }
            // 4.3 校验并锁定库存
            Inventory inventory = validateAndLockInventory(itemDto.getTicketId(),
                    itemDto.getInventoryDate(), itemDto.getQuantity());
            // 4.4 计算金额
            BigDecimal unitPrice = itemDto.getUnitPrice() != null
                    ? itemDto.getUnitPrice() : ticket.getPrice();
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ResultCode.ORDER_AMOUNT_INVALID, "销售单价不能为负");
            }
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = itemDto.getDiscountAmount() != null
                    ? itemDto.getDiscountAmount() : BigDecimal.ZERO;
            if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(subtotal) > 0) {
                throw new BusinessException(ResultCode.ORDER_AMOUNT_INVALID,
                        "优惠金额必须在 [0, " + subtotal + "] 之间");
            }
            BigDecimal finalAmount = subtotal.subtract(discount)
                    .setScale(2, RoundingMode.HALF_UP);

            // 4.5 构造 OrderItem
            OrderItem item = new OrderItem();
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
            item.setVoucherCodes(null); // 售出后再回填
            item.setRefundQuantity(0);
            item.setRefundAmount(BigDecimal.ZERO);
            item.setRemark(itemDto.getRemark());
            items.add(item);

            totalAmount = totalAmount.add(subtotal);
            discountAmount = discountAmount.add(discount);
            itemCount += itemDto.getQuantity();
            // 4.6 累计入场日期范围
            if (useStart == null || itemDto.getInventoryDate().isBefore(useStart)) {
                useStart = itemDto.getInventoryDate();
            }
            if (useEnd == null || itemDto.getInventoryDate().isAfter(useEnd)) {
                useEnd = itemDto.getInventoryDate();
            }
            lastTicketName = ticket.getName();
            // 4.7 扣库存
            decrementInventory(inventory, itemDto.getQuantity());
        }

        BigDecimal paidAmount = totalAmount.subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // 5. 构造 Order
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setChannelCode(dto.getChannelCode());
        order.setChannelName(dto.getChannelName());
        order.setScenicId(dto.getScenicId());
        order.setScenicName(scenic.getName());
        order.setUserId(dto.getUserId());
        order.setUserName(dto.getUserName());
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setContactIdCard(dto.getContactIdCard());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPaidAmount(paidAmount);
        order.setRefundAmount(BigDecimal.ZERO);
        order.setItemCount(itemCount);
        order.setPayMethod(dto.getPayMethod());
        order.setPayTime(LocalDateTime.now());
        order.setPayTransactionId(StringUtils.hasText(dto.getPayTransactionId())
                ? dto.getPayTransactionId()
                : "TEST-" + System.currentTimeMillis());
        order.setStatus(Order.STATUS_FULFILLED);
        order.setFulfillTime(LocalDateTime.now());
        order.setOrderTime(LocalDateTime.now());
        order.setUseStartDate(useStart);
        order.setUseEndDate(useEnd);
        order.setRemark(buildRemark(dto.getRemark(), lastTicketName));

        // 6. 插入主单
        int rows = orderMapper.insert(order);
        if (rows == 0) {
            log.error("[订单] 插入主单失败 orderNo={}", order.getOrderNo());
            throw new BusinessException(ResultCode.ORDER_SAVE_FAILED);
        }
        // 7. 插入明细 + 出票
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            // 7.1 出票：每张生成 1 张 voucher（订单专用方法）
            List<String> codes = voucherService.issueForOrder(
                    order, item, item.getTicketId() == null ? null
                            : ticketMapper.selectById(item.getTicketId()),
                    item.getQuantity());
            // 7.2 回写 voucherCodes
            item.setVoucherCodes(joinList(codes));
            orderItemMapper.updateById(item);
        }

        log.info("[订单] 创建+支付+出票完成 id={}, orderNo={}, paidAmount={}, itemCount={}",
                order.getId(), order.getOrderNo(), paidAmount, itemCount);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long id, OrderPayDTO dto) {
        log.info("[订单] 支付 id={}, method={}", id, dto.getPayMethod());

        Order order = getActiveById(id);
        if (!Order.STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID,
                    "当前状态 " + order.getStatus() + " 不允许支付");
        }
        validatePayMethod(dto.getPayMethod());

        // 模拟支付成功：写支付信息 + 出票
        order.setPayMethod(dto.getPayMethod());
        order.setPayTime(LocalDateTime.now());
        order.setPayTransactionId(StringUtils.hasText(dto.getPayTransactionId())
                ? dto.getPayTransactionId()
                : "TEST-" + System.currentTimeMillis());

        // 出票 + 扣库存（先校验/锁定库存，再生成 voucher）
        List<OrderItem> items = orderItemMapper.selectActiveByOrderId(id);
        if (items.isEmpty()) {
            throw new BusinessException(ResultCode.ORDER_ITEM_NOT_FOUND);
        }
        for (OrderItem item : items) {
            Ticket ticket = ticketMapper.selectById(item.getTicketId());
            if (ticket == null) {
                throw new BusinessException(ResultCode.ORDER_TICKET_OFFLINE,
                        "票种已下架: " + item.getTicketId());
            }
            // 行锁校验库存 + 扣减（与 create() 路径保持一致，防止超卖）
            Inventory inv = validateAndLockInventory(item.getTicketId(),
                    item.getInventoryDate(), item.getQuantity());
            decrementInventory(inv, item.getQuantity());

            List<String> codes = voucherService.issueForOrder(
                    order, item, ticket, item.getQuantity());
            item.setVoucherCodes(joinList(codes));
            orderItemMapper.updateById(item);
        }

        order.setStatus(Order.STATUS_FULFILLED);
        order.setFulfillTime(LocalDateTime.now());
        int rows = orderMapper.updateById(order);
        if (rows == 0) {
            throw new BusinessException(ResultCode.ORDER_PAY_FAILED);
        }
        log.info("[订单] 支付成功 id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, OrderCancelDTO dto) {
        log.info("[订单] 取消 id={}, reason={}", id, dto.getReason());

        Order order = getActiveById(id);
        if (Order.STATUS_CANCEL.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_CANCELLED);
        }
        if (!Order.STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID,
                    "当前状态 " + order.getStatus() + " 不允许取消（已出票请走 refund）");
        }

        // 1. 恢复所有未退明细的库存（本原型"待支付"应该还没扣过库存？见下方注释）
        //    说明：本原型 create() = 创建即支付，会立刻出票 → 不会出现"待支付 + 有 voucher"的状态。
        //    但若未来引入"超时不支付"自动关单，则需补回库存。这里保留代码路径。
        List<OrderItem> items = orderItemMapper.selectActiveByOrderId(id);
        for (OrderItem item : items) {
            int left = item.getQuantity() - item.getRefundQuantity();
            if (left > 0) {
                restoreInventory(item.getInventoryId(), left);
            }
        }

        // 2. 更新主单
        order.setStatus(Order.STATUS_CANCEL);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(dto.getReason());
        int rows = orderMapper.updateById(order);
        if (rows == 0) {
            throw new BusinessException(ResultCode.ORDER_CANCEL_FAILED);
        }
        log.info("[订单] 取消成功 id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id, OrderRefundDTO dto) {
        log.info("[订单] 全单退款 id={}, reason={}", id, dto.getReason());

        Order order = getActiveById(id);
        if (Order.STATUS_REFUNDED.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_REFUNDED);
        }
        if (!Order.STATUS_FULFILLED.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID,
                    "当前状态 " + order.getStatus() + " 不允许退款");
        }
        // 校验：voucher 必须全部"待使用"
        LambdaQueryWrapper<Voucher> w = new LambdaQueryWrapper<>();
        w.isNull(Voucher::getDeletedAt)
         .eq(Voucher::getSaleId, id)  // 注意：voucher.sale_id 存的是 orderId（VoucherService.issue 复用 SaleLike 字段）
         .ne(Voucher::getStatus, Voucher.STATUS_UNUSED);
        long usedOrRefunded = voucherMapper.selectCount(w);
        if (usedOrRefunded > 0) {
            throw new BusinessException(ResultCode.ORDER_HAS_VOUCHER_USED,
                    "订单下存在已被核销或已退的票据，不能整单退");
        }

        // 1. 恢复库存
        List<OrderItem> items = orderItemMapper.selectActiveByOrderId(id);
        for (OrderItem item : items) {
            int left = item.getQuantity() - item.getRefundQuantity();
            if (left > 0) {
                restoreInventory(item.getInventoryId(), left);
                item.setRefundQuantity(item.getQuantity());
                item.setRefundAmount(item.getFinalAmount());
                orderItemMapper.updateById(item);
                // 2. 联动 voucher → 已退
                voucherService.markRefunded(item.getId(), left);
            }
        }

        // 3. 更新主单
        order.setStatus(Order.STATUS_REFUNDED);
        order.setRefundTime(LocalDateTime.now());
        order.setRefundAmount(order.getPaidAmount());
        if (StringUtils.hasText(dto.getReason())) {
            order.setRemark(appendRemark(order.getRemark(), "退款：" + dto.getReason()));
        }
        int rows = orderMapper.updateById(order);
        if (rows == 0) {
            throw new BusinessException(ResultCode.ORDER_REFUND_FAILED);
        }
        log.info("[订单] 退款成功 id={}, refundAmount={}", id, order.getRefundAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info("[订单] 删除 id={}", id);
        Order order = getActiveById(id);
        // 仅允许删除"已取消"或"已退款"状态
        if (!Order.STATUS_CANCEL.equals(order.getStatus())
                && !Order.STATUS_REFUNDED.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID,
                    "仅允许删除已取消或已退款订单，当前状态：" + order.getStatus());
        }
        Order upd = new Order();
        upd.setId(id);
        upd.setDeletedAt(LocalDateTime.now());
        int rows = orderMapper.updateById(upd);
        if (rows == 0) {
            throw new BusinessException(ResultCode.ORDER_DELETE_FAILED);
        }
        log.info("[订单] 删除成功 id={}", id);
    }

    @Override
    public OrderStatsVO stats(Long scenicId, String channelCode) {
        log.info("[订单] 统计 scenicId={}, channel={}", scenicId, channelCode);
        long pending   = countByStatus(scenicId, channelCode, Order.STATUS_PENDING);
        long fulfilled = countByStatus(scenicId, channelCode, Order.STATUS_FULFILLED);
        long cancelled = countByStatus(scenicId, channelCode, Order.STATUS_CANCEL);
        long refunding = countByStatus(scenicId, channelCode, Order.STATUS_REFUNDING);
        long refunded  = countByStatus(scenicId, channelCode, Order.STATUS_REFUNDED);
        long partial   = countByStatus(scenicId, channelCode, Order.STATUS_PARTIAL);
        long total = pending + fulfilled + cancelled + refunding + refunded + partial;

        // GMV = 已出票 + 退款中 + 已退款 + 部分退款 的实付金额
        BigDecimal gmv = sumPaidAmount(scenicId, channelCode, Order.STATUS_FULFILLED, Order.STATUS_REFUNDED,
                Order.STATUS_PARTIAL, Order.STATUS_REFUNDING);
        BigDecimal refundAmt = sumPaidAmount(scenicId, channelCode,
                Order.STATUS_REFUNDED, Order.STATUS_PARTIAL);
        // 出票率 = 已出票 / 合计
        double rate = total == 0 ? 0.0
                : Math.round((double) fulfilled * 10000 / total) / 100.0;

        return OrderStatsVO.builder()
                .scenicId(scenicId)
                .channelCode(channelCode)
                .pendingCount(pending)
                .fulfilledCount(fulfilled)
                .cancelledCount(cancelled)
                .refundingCount(refunding)
                .refundedCount(refunded)
                .partialCount(partial)
                .totalCount(total)
                .gmvAmount(gmv)
                .refundAmount(refundAmt)
                .fulfillRate(rate)
                .build();
    }

    @Override
    public List<OrderVO> listByChannelCode(String channelCode) {
        log.info("[订单] 按渠道列 channel={}", channelCode);
        if (!StringUtils.hasText(channelCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Order::getDeletedAt)
               .eq(Order::getChannelCode, channelCode)
               .orderByDesc(Order::getOrderTime);
        return enrichRecords(orderMapper.selectList(wrapper));
    }

    /* ====================== 私有方法 ====================== */

    private Order getActiveById(Long id) {
        Order entity = orderMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return entity;
    }

    private Scenic validateScenicActive(Long scenicId) {
        Scenic scenic = scenicMapper.selectById(scenicId);
        if (scenic == null || scenic.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SCENIC_NOT_FOUND);
        }
        if ("暂停运营".equals(scenic.getStatus())) {
            throw new BusinessException("园区已暂停运营，无法下单");
        }
        return scenic;
    }

    private void validateChannel(String channelCode) {
        if (!StringUtils.hasText(channelCode) || !CHANNEL_WHITELIST.contains(channelCode)) {
            throw new BusinessException(ResultCode.ORDER_CHANNEL_INVALID);
        }
    }

    private void validatePayMethod(String method) {
        if (!StringUtils.hasText(method) || !PAYMENT_WHITELIST.contains(method)) {
            throw new BusinessException(ResultCode.ORDER_PAY_METHOD_INVALID);
        }
    }

    private Ticket validateTicketActive(Long ticketId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || ticket.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.TICKET_NOT_FOUND);
        }
        if (!"在售".equals(ticket.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_TICKET_OFFLINE);
        }
        return ticket;
    }

    private Inventory validateAndLockInventory(Long ticketId, LocalDate date, int quantity) {
        // 行锁：SELECT ... FOR UPDATE —— 防止并发超卖
        Inventory inv = inventoryMapper.selectForUpdate(ticketId, date);
        if (inv == null) {
            throw new BusinessException(ResultCode.ORDER_INVENTORY_NOT_FOUND,
                    "票种 " + ticketId + " 在 " + date + " 无库存");
        }
        if (!INV_STATUS_OPEN.equals(inv.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_INVENTORY_CLOSED,
                    "库存当前状态：" + inv.getStatus());
        }
        int available = inv.getAvailable() == null ? 0 : inv.getAvailable();
        if (available < quantity) {
            throw new BusinessException(ResultCode.ORDER_STOCK_NOT_ENOUGH,
                    "可用库存 " + available + " 张，需求 " + quantity + " 张");
        }
        return inv;
    }

    private void decrementInventory(Inventory inv, int qty) {
        int newSold = (inv.getSold() == null ? 0 : inv.getSold()) + qty;
        int newAvailable = Math.max(0, (inv.getTotal() == null ? 0 : inv.getTotal()) - newSold
                - (inv.getReserved() == null ? 0 : inv.getReserved()));
        inv.setSold(newSold);
        inv.setAvailable(newAvailable);
        if (newAvailable == 0 && INV_STATUS_OPEN.equals(inv.getStatus())) {
            inv.setStatus("售罄");
        }
        inventoryMapper.updateById(inv);
    }

    private void restoreInventory(Long inventoryId, int qty) {
        if (inventoryId == null || qty <= 0) {
            return;
        }
        Inventory inv = inventoryMapper.selectById(inventoryId);
        if (inv == null || inv.getDeletedAt() != null) {
            return;
        }
        int oldSold = inv.getSold() == null ? 0 : inv.getSold();
        int newSold = Math.max(0, oldSold - qty);
        int newAvailable = Math.max(0, (inv.getTotal() == null ? 0 : inv.getTotal()) - newSold
                - (inv.getReserved() == null ? 0 : inv.getReserved()));
        inv.setSold(newSold);
        inv.setAvailable(newAvailable);
        if ("售罄".equals(inv.getStatus()) && newAvailable > 0) {
            inv.setStatus(INV_STATUS_OPEN);
        }
        inventoryMapper.updateById(inv);
    }

    private long countByStatus(Long scenicId, String channelCode, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Order::getDeletedAt).eq(Order::getStatus, status);
        if (scenicId != null) {
            wrapper.eq(Order::getScenicId, scenicId);
        }
        if (StringUtils.hasText(channelCode)) {
            wrapper.eq(Order::getChannelCode, channelCode);
        }
        return orderMapper.selectCount(wrapper);
    }

    private BigDecimal sumPaidAmount(Long scenicId, String channelCode, String... statuses) {
        if (statuses == null || statuses.length == 0) {
            return BigDecimal.ZERO;
        }
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Order::getDeletedAt)
               .in(Order::getStatus, Arrays.asList(statuses));
        if (scenicId != null) {
            wrapper.eq(Order::getScenicId, scenicId);
        }
        if (StringUtils.hasText(channelCode)) {
            wrapper.eq(Order::getChannelCode, channelCode);
        }
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                .map(Order::getPaidAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNo() {
        // O + yyyyMMddHHmmss + 3 位随机，DB 唯一键冲突时自动重试（最多 3 次）
        return NoGenerator.generateWithRetry("O", NoGenerator.nowCompact(), 3,
                no -> orderMapper.countByOrderNo(no) > 0);
    }

    private String buildRemark(String dtoRemark, String lastTicketName) {
        if (!StringUtils.hasText(dtoRemark) && !StringUtils.hasText(lastTicketName)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(lastTicketName)) {
            sb.append("票种：").append(lastTicketName);
        }
        if (StringUtils.hasText(dtoRemark)) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(dtoRemark);
        }
        return sb.toString();
    }

    private String appendRemark(String original, String addition) {
        if (!StringUtils.hasText(original)) {
            return addition;
        }
        return original + " | " + addition;
    }

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

    /**
     * Entity 列表 → VO 列表，注入明细
     */
    private List<OrderVO> enrichRecords(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 批量查明细
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderItemVO>> itemMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<OrderItem> allItems = orderItemMapper.selectActiveByOrderIds(orderIds);
            for (OrderItem item : allItems) {
                itemMap.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>())
                        .add(OrderItemVO.from(item));
            }
        }
        // 2. 装配
        return orders.stream().map(o -> {
            OrderVO vo = OrderVO.from(o);
            vo.setItems(itemMap.getOrDefault(o.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }
}

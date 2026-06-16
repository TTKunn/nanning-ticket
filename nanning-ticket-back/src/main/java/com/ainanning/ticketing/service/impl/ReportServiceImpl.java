package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.dto.ReportQueryDTO;
import com.ainanning.ticketing.entity.Inventory;
import com.ainanning.ticketing.entity.Order;
import com.ainanning.ticketing.entity.Sale;
import com.ainanning.ticketing.entity.VerifyRecord;
import com.ainanning.ticketing.entity.Voucher;
import com.ainanning.ticketing.mapper.InventoryMapper;
import com.ainanning.ticketing.mapper.OrderMapper;
import com.ainanning.ticketing.mapper.SaleItemMapper;
import com.ainanning.ticketing.mapper.SaleMapper;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.mapper.TicketMapper;
import com.ainanning.ticketing.mapper.VerifyRecordMapper;
import com.ainanning.ticketing.mapper.VoucherMapper;
import com.ainanning.ticketing.service.ReportService;
import com.ainanning.ticketing.vo.ReportInventoryVO;
import com.ainanning.ticketing.vo.ReportOverviewVO;
import com.ainanning.ticketing.vo.ReportPaymentVO;
import com.ainanning.ticketing.vo.ReportRankingVO;
import com.ainanning.ticketing.vo.ReportTrendVO;
import com.ainanning.ticketing.vo.ReportVisitFunnelVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据报表业务实现
 *
 * <p>本模块 = 纯读聚合层，不建新表。从 6 张核心表（sale / order / voucher / verify_record /
 * inventory / channel_settlement）聚合指标。
 * <br>核心实现策略：
 * <ul>
 *   <li>用 MyBatis-Plus {@code selectList} 加载范围内原始数据 + Java 内存聚合，
 *       适合原型数据量（&lt;10w 行 / 周期）；生产环境可改为分维度 SQL 聚合</li>
 *   <li>时间窗口限制 366 天（{@code REPORT_DATE_RANGE_TOO_LARGE}）防止单次扫表过久</li>
 *   <li>所有金额字段 NULL-safe 视为 0；所有时间字段 NULL 视为越界</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SaleMapper saleMapper;
    private final OrderMapper orderMapper;
    private final VoucherMapper voucherMapper;
    private final VerifyRecordMapper verifyRecordMapper;
    private final InventoryMapper inventoryMapper;
    private final TicketMapper ticketMapper;
    private final ScenicMapper scenicMapper;
    private final SaleItemMapper saleItemMapper;

    /* 计入"已售"销售状态（排除已取消） */
    private static final Set<String> SALE_PAID_STATUSES = Set.of("已支付", "部分退票", "已退票");
    private static final Set<String> ORDER_PAID_STATUSES = Set.of(
            Order.STATUS_FULFILLED, Order.STATUS_REFUNDED, Order.STATUS_PARTIAL, Order.STATUS_REFUNDING);
    private static final Set<String> ORDER_REFUND_STATUSES = Set.of(
            Order.STATUS_REFUNDED, Order.STATUS_PARTIAL);

    /* 报表面向开发统计时间窗口的最大跨度 */
    private static final long MAX_DATE_RANGE_DAYS = 366L;

    @Override
    public ReportOverviewVO overview(ReportQueryDTO query) {
        validateRange(query);

        LocalDateTime from = query.getDateFrom().atStartOfDay();
        LocalDateTime to   = query.getDateTo().atTime(23, 59, 59);

        // ===== 1. 销售（窗口售票）=====
        List<Sale> sales = saleMapper.selectList(buildSaleWrapper(query, from, to));
        long saleCount = sales.size();
        BigDecimal saleGmv = sum(sales.stream().map(Sale::getPaidAmount).collect(Collectors.toList()));
        BigDecimal saleRefund = sum(sales.stream()
                .filter(s -> "已退票".equals(s.getStatus()) || "部分退票".equals(s.getStatus()))
                .map(Sale::getRefundAmount).collect(Collectors.toList()));

        // ===== 2. 订单（在线下单）=====
        List<Order> orders = orderMapper.selectList(buildOrderWrapper(query, from, to));
        long orderCount = orders.size();
        BigDecimal orderGmv = sum(orders.stream().map(Order::getPaidAmount).collect(Collectors.toList()));
        BigDecimal orderRefund = sum(orders.stream()
                .filter(o -> ORDER_REFUND_STATUSES.contains(o.getStatus()))
                .map(Order::getRefundAmount).collect(Collectors.toList()));

        BigDecimal totalGmv = nz(saleGmv).add(nz(orderGmv));
        BigDecimal refundAmount = nz(saleRefund).add(nz(orderRefund));
        BigDecimal netRevenue = totalGmv.subtract(refundAmount);

        // ===== 3. Voucher 状态聚合 =====
        // voucher 按出票时间 issueTime 过滤；issueTime 在 order/sale 创建时填入 voucher.issue_time
        // 为简化，原型按 voucher.created_at 范围（出票即创建）
        List<Voucher> vouchers = voucherMapper.selectList(
                new LambdaQueryWrapper<Voucher>()
                        .isNull(Voucher::getDeletedAt)
                        .between(Voucher::getCreatedAt, from, to));
        long voucherIssued  = vouchers.size();
        long voucherUsed    = vouchers.stream().filter(v -> Voucher.STATUS_USED.equals(v.getStatus())).count();
        long voucherRefunded = vouchers.stream().filter(v -> Voucher.STATUS_REFUND.equals(v.getStatus())).count();
        long voucherRevoked  = vouchers.stream().filter(v -> Voucher.STATUS_REVOKED.equals(v.getStatus())).count();

        Double useRate = voucherIssued == 0 ? 0d
                : roundHalfUp(voucherUsed * 10000.0 / voucherIssued) / 100d;

        // ===== 4. 库存聚合 =====
        List<Inventory> invs = inventoryMapper.selectList(buildInventoryWrapper(query));
        long invTotal = invs.stream().mapToLong(i -> i.getTotal() == null ? 0L : i.getTotal().longValue()).sum();
        long invSold  = invs.stream().mapToLong(i -> i.getSold() == null ? 0L : i.getSold().longValue()).sum();
        Double invRate = invTotal == 0 ? 0d : roundHalfUp(invSold * 10000.0 / invTotal) / 100d;

        return ReportOverviewVO.builder()
                .dateFrom(query.getDateFrom().toString())
                .dateTo(query.getDateTo().toString())
                .scenicId(query.getScenicId())
                .channelCode(query.getChannelCode())
                .saleCount(saleCount)
                .orderCount(orderCount)
                .totalTicketCount(saleCount + orderCount)
                .saleGmv(nz(saleGmv))
                .orderGmv(nz(orderGmv))
                .totalGmv(totalGmv)
                .refundAmount(refundAmount)
                .netRevenue(netRevenue)
                .voucherIssued(voucherIssued)
                .voucherUsed(voucherUsed)
                .voucherRefunded(voucherRefunded)
                .voucherRevoked(voucherRevoked)
                .useRate(useRate)
                .inventoryTotal(invTotal)
                .inventorySold(invSold)
                .inventorySellRate(invRate)
                .build();
    }

    @Override
    public ReportTrendVO trend(ReportQueryDTO query) {
        validateRange(query);
        String interval = StringUtils.hasText(query.getInterval())
                ? query.getInterval() : ReportQueryDTO.INTERVAL_DAY;
        if (!ReportQueryDTO.INTERVAL_DAY.equals(interval)
                && !ReportQueryDTO.INTERVAL_WEEK.equals(interval)
                && !ReportQueryDTO.INTERVAL_MONTH.equals(interval)) {
            throw new BusinessException(ResultCode.REPORT_INTERVAL_INVALID);
        }

        LocalDateTime from = query.getDateFrom().atStartOfDay();
        LocalDateTime to   = query.getDateTo().atTime(23, 59, 59);

        // 加载原始数据
        List<Sale> sales = saleMapper.selectList(buildSaleWrapper(query, from, to));
        List<Order> orders = orderMapper.selectList(buildOrderWrapper(query, from, to));
        List<VerifyRecord> verifies = verifyRecordMapper.selectList(
                new LambdaQueryWrapper<VerifyRecord>()
                        .isNull(VerifyRecord::getDeletedAt)
                        .eq(VerifyRecord::getResult, "成功")
                        .between(VerifyRecord::getVerifyTime, from, to));

        // 按桶聚合
        Map<String, TrendAccumulator> buckets = new LinkedHashMap<>();
        List<LocalDate> bucketStarts = computeBucketStarts(query.getDateFrom(), query.getDateTo(), interval);

        for (LocalDate bs : bucketStarts) {
            String key = bs.format(DateTimeFormatter.ISO_LOCAL_DATE);
            buckets.put(key, new TrendAccumulator());
        }

        for (Sale s : sales) {
            String key = bucketize(s.getSaleTime(), query.getDateFrom(), interval);
            if (key == null) continue;
            TrendAccumulator a = buckets.get(key);
            if (a != null) {
                a.saleCount++;
                a.saleGmv = a.saleGmv.add(nz(s.getPaidAmount()));
            }
        }
        for (Order o : orders) {
            String key = bucketize(o.getOrderTime(), query.getDateFrom(), interval);
            if (key == null) continue;
            TrendAccumulator a = buckets.get(key);
            if (a != null) {
                a.orderCount++;
                a.orderGmv = a.orderGmv.add(nz(o.getPaidAmount()));
                if (ORDER_REFUND_STATUSES.contains(o.getStatus())) {
                    a.refund = a.refund.add(nz(o.getRefundAmount()));
                }
            }
        }
        for (VerifyRecord v : verifies) {
            String key = bucketize(v.getVerifyTime(), query.getDateFrom(), interval);
            if (key == null) continue;
            TrendAccumulator a = buckets.get(key);
            if (a != null) a.useCount++;
        }

        List<ReportTrendVO.TrendBucket> result = new ArrayList<>();
        for (Map.Entry<String, TrendAccumulator> e : buckets.entrySet()) {
            TrendAccumulator a = e.getValue();
            LocalDate bucketStart = LocalDate.parse(e.getKey());
            LocalDate bucketEnd = bucketEndOf(bucketStart, interval);
            result.add(ReportTrendVO.TrendBucket.builder()
                    .bucket(e.getKey())
                    .bucketEnd(bucketEnd.toString())
                    .saleCount(a.saleCount)
                    .saleGmv(a.saleGmv)
                    .orderCount(a.orderCount)
                    .orderGmv(a.orderGmv)
                    .totalCount(a.saleCount + a.orderCount)
                    .totalGmv(a.saleGmv.add(a.orderGmv))
                    .refundAmount(a.refund)
                    .useCount(a.useCount)
                    .build());
        }

        return ReportTrendVO.builder()
                .interval(interval)
                .dateFrom(query.getDateFrom().toString())
                .dateTo(query.getDateTo().toString())
                .scenicId(query.getScenicId())
                .channelCode(query.getChannelCode())
                .buckets(result)
                .build();
    }

    @Override
    public ReportRankingVO ranking(ReportQueryDTO query) {
        validateRange(query);
        String groupBy = StringUtils.hasText(query.getGroupBy())
                ? query.getGroupBy() : ReportQueryDTO.GROUP_BY_CHANNEL;
        if (!ReportQueryDTO.GROUP_BY_CHANNEL.equals(groupBy)
                && !ReportQueryDTO.GROUP_BY_SCENIC.equals(groupBy)
                && !ReportQueryDTO.GROUP_BY_TICKET.equals(groupBy)
                && !ReportQueryDTO.GROUP_BY_PAY_METHOD.equals(groupBy)
                && !ReportQueryDTO.GROUP_BY_WINDOW.equals(groupBy)) {
            throw new BusinessException(ResultCode.REPORT_GROUP_BY_INVALID);
        }

        LocalDateTime from = query.getDateFrom().atStartOfDay();
        LocalDateTime to   = query.getDateTo().atTime(23, 59, 59);

        List<ReportRankingVO.RankingRow> rows = new ArrayList<>();
        if (ReportQueryDTO.GROUP_BY_CHANNEL.equals(groupBy)) {
            rows = rankByChannel(query, from, to);
        } else if (ReportQueryDTO.GROUP_BY_SCENIC.equals(groupBy)) {
            rows = rankByScenic(query, from, to);
        } else if (ReportQueryDTO.GROUP_BY_TICKET.equals(groupBy)) {
            rows = rankByTicket(query, from, to);
        } else if (ReportQueryDTO.GROUP_BY_PAY_METHOD.equals(groupBy)) {
            rows = rankByPayMethod(query, from, to);
        } else if (ReportQueryDTO.GROUP_BY_WINDOW.equals(groupBy)) {
            rows = rankByWindow(query, from, to);
        }

        // 计算总额 & 占比
        BigDecimal totalGmv = rows.stream()
                .map(ReportRankingVO.RankingRow::getGmv)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        for (ReportRankingVO.RankingRow r : rows) {
            Double pct = totalGmv.compareTo(BigDecimal.ZERO) == 0 ? 0d
                    : roundHalfUp(r.getGmv().multiply(new BigDecimal("10000"))
                            .divide(totalGmv, 0, RoundingMode.HALF_UP).doubleValue()) / 100d;
            r.setPercentage(pct);
        }
        // 排名
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setRank(i + 1);
        }

        return ReportRankingVO.builder()
                .groupBy(groupBy)
                .dateFrom(query.getDateFrom().toString())
                .dateTo(query.getDateTo().toString())
                .scenicId(query.getScenicId())
                .totalRows(rows.size())
                .rows(rows)
                .build();
    }

    @Override
    public ReportVisitFunnelVO visitFunnel(ReportQueryDTO query) {
        validateRange(query);
        LocalDateTime from = query.getDateFrom().atStartOfDay();
        LocalDateTime to   = query.getDateTo().atTime(23, 59, 59);

        // 下单 = 周期内"已出票/已退款/部分退款/退款中"订单
        long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .isNull(Order::getDeletedAt)
                        .in(Order::getStatus, ORDER_PAID_STATUSES)
                        .between(Order::getOrderTime, from, to)
                        .eq(query.getScenicId() != null, Order::getScenicId, query.getScenicId())
                        .eq(StringUtils.hasText(query.getChannelCode()), Order::getChannelCode, query.getChannelCode()));

        // 出票 = 周期内 voucher（createdAt 视为出票时间）
        long issuedCount = voucherMapper.selectCount(
                new LambdaQueryWrapper<Voucher>()
                        .isNull(Voucher::getDeletedAt)
                        .between(Voucher::getCreatedAt, from, to)
                        .eq(query.getScenicId() != null, Voucher::getScenicId, query.getScenicId()));

        // 核销 = 周期内核销成功的 verify_record
        long usedCount = verifyRecordMapper.selectCount(
                new LambdaQueryWrapper<VerifyRecord>()
                        .isNull(VerifyRecord::getDeletedAt)
                        .eq(VerifyRecord::getResult, "成功")
                        .between(VerifyRecord::getVerifyTime, from, to)
                        .eq(query.getScenicId() != null, VerifyRecord::getScenicId, query.getScenicId()));

        Double orderToIssue = rate(issuedCount, orderCount);
        Double issueToUse   = rate(usedCount, issuedCount);
        Double orderToUse   = rate(usedCount, orderCount);

        List<ReportVisitFunnelVO.FunnelStep> steps = new ArrayList<>();
        steps.add(ReportVisitFunnelVO.FunnelStep.builder()
                .step(1).name("下单").count(orderCount).conversionRate(100d).build());
        steps.add(ReportVisitFunnelVO.FunnelStep.builder()
                .step(2).name("出票").count(issuedCount).conversionRate(orderToIssue).build());
        steps.add(ReportVisitFunnelVO.FunnelStep.builder()
                .step(3).name("核销").count(usedCount).conversionRate(issueToUse).build());

        return ReportVisitFunnelVO.builder()
                .dateFrom(query.getDateFrom().toString())
                .dateTo(query.getDateTo().toString())
                .scenicId(query.getScenicId())
                .channelCode(query.getChannelCode())
                .steps(steps)
                .orderToIssueRate(orderToIssue)
                .issueToUseRate(issueToUse)
                .orderToUseRate(orderToUse)
                .build();
    }

    @Override
    public ReportInventoryVO inventory(ReportQueryDTO query) {
        validateRange(query);
        List<Inventory> invs = inventoryMapper.selectList(buildInventoryWrapper(query));

        // 加载票种和园区名
        Set<Long> ticketIds = invs.stream().map(Inventory::getTicketId).collect(Collectors.toSet());
        Map<Long, String> ticketNameMap = ticketIds.isEmpty()
                ? Map.of()
                : ticketMapper.selectBatchIds(ticketIds).stream()
                        .collect(Collectors.toMap(com.ainanning.ticketing.entity.Ticket::getId,
                                com.ainanning.ticketing.entity.Ticket::getName, (a, b) -> a));

        Set<Long> scenicIds = invs.stream().map(Inventory::getScenicId).collect(Collectors.toSet());
        Map<Long, String> scenicNameMap = scenicIds.isEmpty()
                ? Map.of()
                : scenicMapper.selectBatchIds(scenicIds).stream()
                        .collect(Collectors.toMap(com.ainanning.ticketing.entity.Scenic::getId,
                                com.ainanning.ticketing.entity.Scenic::getName, (a, b) -> a));

        long totalAvailable = 0L;
        long totalSold = 0L;
        List<ReportInventoryVO.InventoryRow> rows = new ArrayList<>();
        for (Inventory inv : invs) {
            int total = inv.getTotal() == null ? 0 : inv.getTotal();
            int sold = inv.getSold() == null ? 0 : inv.getSold();
            int available = Math.max(0, total - sold);
            Double sellRate = total == 0 ? 0d
                    : roundHalfUp(sold * 10000.0 / total) / 100d;
            totalAvailable += total;
            totalSold += sold;
            rows.add(ReportInventoryVO.InventoryRow.builder()
                    .inventoryDate(inv.getInventoryDate() == null ? null : inv.getInventoryDate().toString())
                    .ticketId(inv.getTicketId())
                    .ticketName(ticketNameMap.getOrDefault(inv.getTicketId(), ""))
                    .scenicId(inv.getScenicId())
                    .scenicName(scenicNameMap.getOrDefault(inv.getScenicId(), ""))
                    .total(total)
                    .sold(sold)
                    .available(available)
                    .status(inv.getStatus())
                    .sellRate(sellRate)
                    .build());
        }
        rows.sort(Comparator.comparing(ReportInventoryVO.InventoryRow::getInventoryDate,
                Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ReportInventoryVO.InventoryRow::getTicketId,
                        Comparator.nullsLast(Comparator.naturalOrder())));

        Double overall = totalAvailable == 0 ? 0d
                : roundHalfUp(totalSold * 10000.0 / totalAvailable) / 100d;

        return ReportInventoryVO.builder()
                .dateFrom(query.getDateFrom().toString())
                .dateTo(query.getDateTo().toString())
                .scenicId(query.getScenicId())
                .totalRows(rows.size())
                .totalAvailable(totalAvailable)
                .totalSold(totalSold)
                .overallSellRate(overall)
                .rows(rows)
                .build();
    }

    @Override
    public ReportPaymentVO payment(ReportQueryDTO query) {
        validateRange(query);
        LocalDateTime from = query.getDateFrom().atStartOfDay();
        LocalDateTime to   = query.getDateTo().atTime(23, 59, 59);

        // 窗口售票
        List<Sale> sales = saleMapper.selectList(buildSaleWrapper(query, from, to));
        // 在线订单
        List<Order> orders = orderMapper.selectList(buildOrderWrapper(query, from, to));

        Map<String, long[]> acc = new LinkedHashMap<>(); // method -> {count}
        Map<String, BigDecimal> gmvMap = new LinkedHashMap<>();

        for (Sale s : sales) {
            String m = s.getPaymentMethod() == null ? "未知" : s.getPaymentMethod();
            acc.computeIfAbsent(m, k -> new long[1])[0]++;
            gmvMap.merge(m, nz(s.getPaidAmount()), BigDecimal::add);
        }
        for (Order o : orders) {
            String m = o.getPayMethod() == null ? "未知" : o.getPayMethod();
            acc.computeIfAbsent(m, k -> new long[1])[0]++;
            gmvMap.merge(m, nz(o.getPaidAmount()), BigDecimal::add);
        }

        BigDecimal totalGmv = gmvMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalCount = acc.values().stream().mapToLong(a -> a[0]).sum();

        List<ReportPaymentVO.PaymentRow> rows = new ArrayList<>();
        for (Map.Entry<String, long[]> e : acc.entrySet()) {
            BigDecimal gmv = gmvMap.getOrDefault(e.getKey(), BigDecimal.ZERO);
            Double pct = totalGmv.compareTo(BigDecimal.ZERO) == 0 ? 0d
                    : roundHalfUp(gmv.multiply(new BigDecimal("10000"))
                            .divide(totalGmv, 0, RoundingMode.HALF_UP).doubleValue()) / 100d;
            rows.add(ReportPaymentVO.PaymentRow.builder()
                    .payMethod(e.getKey())
                    .count(e.getValue()[0])
                    .gmv(gmv)
                    .percentage(pct)
                    .build());
        }
        rows.sort(Comparator.comparing(ReportPaymentVO.PaymentRow::getGmv).reversed());

        return ReportPaymentVO.builder()
                .dateFrom(query.getDateFrom().toString())
                .dateTo(query.getDateTo().toString())
                .scenicId(query.getScenicId())
                .totalCount(totalCount)
                .totalGmv(totalGmv)
                .rows(rows)
                .build();
    }

    /* ================== 私有方法 ================== */

    /** 通用日期范围校验 */
    private void validateRange(ReportQueryDTO query) {
        if (query.getDateFrom() == null || query.getDateTo() == null) {
            throw new BusinessException(ResultCode.REPORT_DATE_RANGE_INVALID);
        }
        if (query.getDateTo().isBefore(query.getDateFrom())) {
            throw new BusinessException(ResultCode.REPORT_DATE_RANGE_INVALID);
        }
        long days = query.getDateFrom().toEpochDay() - query.getDateTo().toEpochDay();
        if (-days > MAX_DATE_RANGE_DAYS) {
            throw new BusinessException(ResultCode.REPORT_DATE_RANGE_TOO_LARGE);
        }
    }

    private LambdaQueryWrapper<Sale> buildSaleWrapper(ReportQueryDTO q, LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<Sale> w = new LambdaQueryWrapper<>();
        w.isNull(Sale::getDeletedAt)
                .in(Sale::getStatus, SALE_PAID_STATUSES)
                .between(Sale::getSaleTime, from, to);
        if (q.getScenicId() != null) w.eq(Sale::getScenicId, q.getScenicId());
        // sale 没有 channelCode，跳过
        return w;
    }

    private LambdaQueryWrapper<Order> buildOrderWrapper(ReportQueryDTO q, LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
        w.isNull(Order::getDeletedAt)
                .in(Order::getStatus, ORDER_PAID_STATUSES)
                .between(Order::getOrderTime, from, to);
        if (q.getScenicId() != null) w.eq(Order::getScenicId, q.getScenicId());
        if (StringUtils.hasText(q.getChannelCode())) w.eq(Order::getChannelCode, q.getChannelCode());
        return w;
    }

    private LambdaQueryWrapper<Inventory> buildInventoryWrapper(ReportQueryDTO q) {
        LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<>();
        w.isNull(Inventory::getDeletedAt)
                .between(Inventory::getInventoryDate, q.getDateFrom(), q.getDateTo());
        if (q.getScenicId() != null) w.eq(Inventory::getScenicId, q.getScenicId());
        return w;
    }

    private BigDecimal sum(List<BigDecimal> list) {
        return list.stream().filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private Double rate(long numerator, long denominator) {
        if (denominator == 0) return 0d;
        return roundHalfUp(numerator * 10000.0 / denominator) / 100d;
    }

    private long roundHalfUp(double v) {
        return Math.round(v);
    }

    /** 计算区间内所有桶的起点（按 interval） */
    private List<LocalDate> computeBucketStarts(LocalDate from, LocalDate to, String interval) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate cur = from;
        while (!cur.isAfter(to)) {
            result.add(cur);
            if (ReportQueryDTO.INTERVAL_DAY.equals(interval)) {
                cur = cur.plusDays(1);
            } else if (ReportQueryDTO.INTERVAL_WEEK.equals(interval)) {
                cur = cur.plusWeeks(1);
            } else {
                cur = cur.plusMonths(1);
            }
        }
        return result;
    }

    /** 给定时间戳所在桶的起点 key（yyyy-MM-dd） */
    private String bucketize(LocalDateTime t, LocalDate rangeStart, String interval) {
        if (t == null) return null;
        LocalDate d = t.toLocalDate();
        if (ReportQueryDTO.INTERVAL_DAY.equals(interval)) {
            return d.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (ReportQueryDTO.INTERVAL_WEEK.equals(interval)) {
            LocalDate weekStart = d.with(DayOfWeek.MONDAY);
            return weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        // MONTH: 取每月 1 号
        LocalDate monthStart = d.withDayOfMonth(1);
        return monthStart.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /** 桶的结束日（含） */
    private LocalDate bucketEndOf(LocalDate bucketStart, String interval) {
        if (ReportQueryDTO.INTERVAL_DAY.equals(interval)) {
            return bucketStart;
        }
        if (ReportQueryDTO.INTERVAL_WEEK.equals(interval)) {
            return bucketStart.with(DayOfWeek.SUNDAY);
        }
        return bucketStart.with(TemporalAdjusters.lastDayOfMonth());
    }

    /** 趋势桶累加器 */
    private static class TrendAccumulator {
        long saleCount = 0L;
        long orderCount = 0L;
        long useCount = 0L;
        BigDecimal saleGmv = BigDecimal.ZERO;
        BigDecimal orderGmv = BigDecimal.ZERO;
        BigDecimal refund = BigDecimal.ZERO;
    }

    /* ============== 排名 5 个维度的私有方法 ============== */

    private List<ReportRankingVO.RankingRow> rankByChannel(ReportQueryDTO q, LocalDateTime from, LocalDateTime to) {
        // 渠道维度：order.channel_code 即渠道（sale 无渠道字段）
        List<Order> orders = orderMapper.selectList(buildOrderWrapper(q, from, to));
        Map<String, BigDecimal[]> acc = new LinkedHashMap<>();
        Map<String, String> nameMap = new LinkedHashMap<>();
        for (Order o : orders) {
            String key = o.getChannelCode() == null ? "未归类" : o.getChannelCode();
            acc.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acc.get(key)[0] = acc.get(key)[0].add(nz(o.getPaidAmount()));
            if (ORDER_REFUND_STATUSES.contains(o.getStatus())) {
                acc.get(key)[1] = acc.get(key)[1].add(nz(o.getRefundAmount()));
            }
            nameMap.putIfAbsent(key, o.getChannelName() == null ? key : o.getChannelName());
        }
        List<ReportRankingVO.RankingRow> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> e : acc.entrySet()) {
            BigDecimal gmv = e.getValue()[0];
            BigDecimal refund = e.getValue()[1];
            rows.add(ReportRankingVO.RankingRow.builder()
                    .dimKey(e.getKey())
                    .dimName(nameMap.getOrDefault(e.getKey(), e.getKey()))
                    .dimType("渠道")
                    .count(0)
                    .gmv(gmv)
                    .refundAmount(refund)
                    .netRevenue(gmv.subtract(refund))
                    .build());
        }
        rows.sort(Comparator.comparing(ReportRankingVO.RankingRow::getGmv).reversed());
        return rows;
    }

    private List<ReportRankingVO.RankingRow> rankByScenic(ReportQueryDTO q, LocalDateTime from, LocalDateTime to) {
        Map<Long, BigDecimal[]> acc = new LinkedHashMap<>();
        for (Sale s : saleMapper.selectList(buildSaleWrapper(q, from, to))) {
            acc.computeIfAbsent(s.getScenicId(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acc.get(s.getScenicId())[0] = acc.get(s.getScenicId())[0].add(nz(s.getPaidAmount()));
            if ("已退票".equals(s.getStatus()) || "部分退票".equals(s.getStatus())) {
                acc.get(s.getScenicId())[1] = acc.get(s.getScenicId())[1].add(nz(s.getRefundAmount()));
            }
        }
        for (Order o : orderMapper.selectList(buildOrderWrapper(q, from, to))) {
            acc.computeIfAbsent(o.getScenicId(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acc.get(o.getScenicId())[0] = acc.get(o.getScenicId())[0].add(nz(o.getPaidAmount()));
            if (ORDER_REFUND_STATUSES.contains(o.getStatus())) {
                acc.get(o.getScenicId())[1] = acc.get(o.getScenicId())[1].add(nz(o.getRefundAmount()));
            }
        }
        // 园区名
        Set<Long> ids = acc.keySet();
        Map<Long, String> nameMap = ids.isEmpty() ? Map.of()
                : scenicMapper.selectBatchIds(ids).stream()
                        .collect(Collectors.toMap(com.ainanning.ticketing.entity.Scenic::getId,
                                com.ainanning.ticketing.entity.Scenic::getName, (a, b) -> a));
        List<ReportRankingVO.RankingRow> rows = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal[]> e : acc.entrySet()) {
            BigDecimal gmv = e.getValue()[0];
            BigDecimal refund = e.getValue()[1];
            rows.add(ReportRankingVO.RankingRow.builder()
                    .dimKey(String.valueOf(e.getKey()))
                    .dimName(nameMap.getOrDefault(e.getKey(), ""))
                    .dimType("园区")
                    .count(0)
                    .gmv(gmv)
                    .refundAmount(refund)
                    .netRevenue(gmv.subtract(refund))
                    .build());
        }
        rows.sort(Comparator.comparing(ReportRankingVO.RankingRow::getGmv).reversed());
        return rows;
    }

    private List<ReportRankingVO.RankingRow> rankByTicket(ReportQueryDTO q, LocalDateTime from, LocalDateTime to) {
        // 票种：saleItem / orderItem，需要更细的明细；原型阶段基于 sale_item 聚合
        // 此处简化：只对 sale_item 处理（窗口售票天然按票种拆分）
        LambdaQueryWrapper<com.ainanning.ticketing.entity.SaleItem> siw = new LambdaQueryWrapper<>();
        siw.isNull(com.ainanning.ticketing.entity.SaleItem::getDeletedAt);
        if (q.getScenicId() != null) {
            siw.eq(com.ainanning.ticketing.entity.SaleItem::getScenicId, q.getScenicId());
        }
        List<com.ainanning.ticketing.entity.SaleItem> items =
                injectSaleItems(siw);
        Map<Long, BigDecimal[]> acc = new LinkedHashMap<>();
        for (com.ainanning.ticketing.entity.SaleItem item : items) {
            acc.computeIfAbsent(item.getTicketId(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal lineGmv = nz(item.getUnitPrice()).multiply(new BigDecimal(item.getQuantity() == null ? 0 : item.getQuantity()));
            acc.get(item.getTicketId())[0] = acc.get(item.getTicketId())[0].add(lineGmv);
            BigDecimal refundQty = new BigDecimal(item.getRefundQuantity() == null ? 0 : item.getRefundQuantity());
            if (refundQty.signum() > 0) {
                acc.get(item.getTicketId())[1] = acc.get(item.getTicketId())[1]
                        .add(nz(item.getRefundAmount()));
            }
        }
        Set<Long> ids = acc.keySet();
        Map<Long, String> nameMap = ids.isEmpty() ? Map.of()
                : ticketMapper.selectBatchIds(ids).stream()
                        .collect(Collectors.toMap(com.ainanning.ticketing.entity.Ticket::getId,
                                com.ainanning.ticketing.entity.Ticket::getName, (a, b) -> a));
        List<ReportRankingVO.RankingRow> rows = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal[]> e : acc.entrySet()) {
            BigDecimal gmv = e.getValue()[0];
            BigDecimal refund = e.getValue()[1];
            rows.add(ReportRankingVO.RankingRow.builder()
                    .dimKey(String.valueOf(e.getKey()))
                    .dimName(nameMap.getOrDefault(e.getKey(), ""))
                    .dimType("票种")
                    .count(0)
                    .gmv(gmv)
                    .refundAmount(refund)
                    .netRevenue(gmv.subtract(refund))
                    .build());
        }
        rows.sort(Comparator.comparing(ReportRankingVO.RankingRow::getGmv).reversed());
        return rows;
    }

    private List<ReportRankingVO.RankingRow> rankByPayMethod(ReportQueryDTO q, LocalDateTime from, LocalDateTime to) {
        Map<String, BigDecimal[]> acc = new LinkedHashMap<>();
        for (Sale s : saleMapper.selectList(buildSaleWrapper(q, from, to))) {
            String key = s.getPaymentMethod() == null ? "未知" : s.getPaymentMethod();
            acc.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acc.get(key)[0] = acc.get(key)[0].add(nz(s.getPaidAmount()));
        }
        for (Order o : orderMapper.selectList(buildOrderWrapper(q, from, to))) {
            String key = o.getPayMethod() == null ? "未知" : o.getPayMethod();
            acc.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acc.get(key)[0] = acc.get(key)[0].add(nz(o.getPaidAmount()));
        }
        List<ReportRankingVO.RankingRow> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> e : acc.entrySet()) {
            rows.add(ReportRankingVO.RankingRow.builder()
                    .dimKey(e.getKey())
                    .dimName(e.getKey())
                    .dimType("支付方式")
                    .count(0)
                    .gmv(e.getValue()[0])
                    .refundAmount(BigDecimal.ZERO)
                    .netRevenue(e.getValue()[0])
                    .build());
        }
        rows.sort(Comparator.comparing(ReportRankingVO.RankingRow::getGmv).reversed());
        return rows;
    }

    private List<ReportRankingVO.RankingRow> rankByWindow(ReportQueryDTO q, LocalDateTime from, LocalDateTime to) {
        // 窗口维度（窗口售票特有）
        Map<String, BigDecimal[]> acc = new LinkedHashMap<>();
        for (Sale s : saleMapper.selectList(buildSaleWrapper(q, from, to))) {
            String key = s.getWindowName() == null ? "未指派窗口" : s.getWindowName();
            acc.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acc.get(key)[0] = acc.get(key)[0].add(nz(s.getPaidAmount()));
            if ("已退票".equals(s.getStatus()) || "部分退票".equals(s.getStatus())) {
                acc.get(key)[1] = acc.get(key)[1].add(nz(s.getRefundAmount()));
            }
        }
        List<ReportRankingVO.RankingRow> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> e : acc.entrySet()) {
            BigDecimal gmv = e.getValue()[0];
            BigDecimal refund = e.getValue()[1];
            rows.add(ReportRankingVO.RankingRow.builder()
                    .dimKey(e.getKey())
                    .dimName(e.getKey())
                    .dimType("窗口")
                    .count(0)
                    .gmv(gmv)
                    .refundAmount(refund)
                    .netRevenue(gmv.subtract(refund))
                    .build());
        }
        rows.sort(Comparator.comparing(ReportRankingVO.RankingRow::getGmv).reversed());
        return rows;
    }

    /** 注入 sale_item 列表（占位解耦，简化原型） */
    private List<com.ainanning.ticketing.entity.SaleItem> injectSaleItems(
            LambdaQueryWrapper<com.ainanning.ticketing.entity.SaleItem> wrapper) {
        // 原型用：直接 selectList，prototype 用 service 注入此处由 controller 之外的 mapper 兜底
        // 为了避免再加 SaleItemMapper，临时直接用 service 内的限定范围
        return saleItemMapper.selectList(wrapper);
    }
}

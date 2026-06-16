package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.util.NoGenerator;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SettlementActionDTO;
import com.ainanning.ticketing.dto.SettlementCreateDTO;
import com.ainanning.ticketing.dto.SettlementQueryDTO;
import com.ainanning.ticketing.entity.Channel;
import com.ainanning.ticketing.entity.ChannelSettlement;
import com.ainanning.ticketing.entity.Order;
import com.ainanning.ticketing.mapper.ChannelMapper;
import com.ainanning.ticketing.mapper.ChannelSettlementMapper;
import com.ainanning.ticketing.mapper.OrderMapper;
import com.ainanning.ticketing.service.ChannelSettlementService;
import com.ainanning.ticketing.vo.ChannelSettlementVO;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 渠道结算单业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>状态机：{@code 待确认 → 已确认 → 已打款}（任一状态都可作废）</li>
 *   <li>金额计算：(GMV - 退款) × 比例 / 100 = 佣金；应付 = (GMV-退款) - 佣金</li>
 *   <li>{@code commission_rate} 字段在创建时快照渠道当时的比例，比例调整不影响历史结算单</li>
 *   <li>同一渠道同周期内不允许多张"有效（待确认/已确认/已打款）"结算单</li>
 *   <li>结算单的订单 ID 用 CSV 存储（{@code orderIds}）</li>
 *   <li>结算单号格式：{@code CS + yyyyMM + 3 位流水}</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelSettlementServiceImpl implements ChannelSettlementService {

    private final ChannelSettlementMapper settlementMapper;
    private final ChannelMapper channelMapper;
    private final OrderMapper orderMapper;

    /** 计入结算的订单状态集合（已出票 + 已退款 + 部分退款 + 退款中） */
    private static final Set<String> PAID_STATUSES = Set.of(
            Order.STATUS_FULFILLED, Order.STATUS_REFUNDED, Order.STATUS_PARTIAL, Order.STATUS_REFUNDING);

    /** 计入退款金额的状态（已退款 / 部分退款） */
    private static final Set<String> REFUND_STATUSES = Set.of(
            Order.STATUS_REFUNDED, Order.STATUS_PARTIAL);

    /** 视为"有效"的结算单状态（不可重复生成） */
    private static final Set<String> ACTIVE_STATUSES = Set.of(
            ChannelSettlement.STATUS_PENDING,
            ChannelSettlement.STATUS_CONFIRMED,
            ChannelSettlement.STATUS_PAID);

    @Override
    public PageVO<ChannelSettlementVO> page(SettlementQueryDTO query) {
        log.info("[结算] 分页查询 channelId={}, status={}, [{}~{}], keyword={}",
                query.getChannelId(), query.getStatus(),
                query.getPeriodFrom(), query.getPeriodTo(), query.getKeyword());

        Page<ChannelSettlement> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<ChannelSettlement> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(ChannelSettlement::getDeletedAt);
        if (query.getChannelId() != null) {
            wrapper.eq(ChannelSettlement::getChannelId, query.getChannelId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(ChannelSettlement::getStatus, query.getStatus());
        }
        if (query.getPeriodFrom() != null) {
            wrapper.ge(ChannelSettlement::getPeriodStart, query.getPeriodFrom());
        }
        if (query.getPeriodTo() != null) {
            wrapper.le(ChannelSettlement::getPeriodEnd, query.getPeriodTo());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(ChannelSettlement::getSettlementNo, kw)
                    .or().like(ChannelSettlement::getChannelName, kw));
        }
        wrapper.orderByDesc(ChannelSettlement::getPeriodStart)
                .orderByDesc(ChannelSettlement::getId);

        Page<ChannelSettlement> result = settlementMapper.selectPage(page, wrapper);
        List<ChannelSettlementVO> voList = result.getRecords().stream()
                .map(ChannelSettlementVO::from)
                .collect(Collectors.toList());
        return PageVO.of(result, voList);
    }

    @Override
    public ChannelSettlementVO getById(Long id) {
        ChannelSettlement settlement = settlementMapper.selectById(id);
        if (settlement == null || settlement.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_NOT_FOUND);
        }
        return ChannelSettlementVO.from(settlement);
    }

    @Override
    public ChannelSettlementVO getByNo(String settlementNo) {
        ChannelSettlement settlement = settlementMapper.selectOne(
                new LambdaQueryWrapper<ChannelSettlement>()
                        .eq(ChannelSettlement::getSettlementNo, settlementNo)
                        .isNull(ChannelSettlement::getDeletedAt));
        if (settlement == null) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_NOT_FOUND);
        }
        return ChannelSettlementVO.from(settlement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSettlement(SettlementCreateDTO dto) {
        // 1. 渠道校验
        if (dto.getPeriodStart() == null || dto.getPeriodEnd() == null) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_PERIOD_INVALID);
        }
        if (dto.getPeriodEnd().isBefore(dto.getPeriodStart())) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_PERIOD_INVALID);
        }

        Channel channel = channelMapper.selectById(dto.getChannelId());
        if (channel == null || channel.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.CHANNEL_NOT_FOUND);
        }

        // 2. 同渠道同周期不可重复
        Long existsCount = settlementMapper.selectCount(
                new LambdaQueryWrapper<ChannelSettlement>()
                        .eq(ChannelSettlement::getChannelId, channel.getId())
                        .eq(ChannelSettlement::getPeriodStart, dto.getPeriodStart())
                        .eq(ChannelSettlement::getPeriodEnd, dto.getPeriodEnd())
                        .in(ChannelSettlement::getStatus, ACTIVE_STATUSES)
                        .isNull(ChannelSettlement::getDeletedAt));
        if (existsCount > 0) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_PERIOD_INVALID,
                    "该渠道同周期已有有效结算单");
        }

        // 3. 聚合订单
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getChannelCode, channel.getChannelCode())
                        .ge(Order::getOrderTime, dto.getPeriodStart().atStartOfDay())
                        .le(Order::getOrderTime, dto.getPeriodEnd().atTime(23, 59, 59))
                        .in(Order::getStatus, PAID_STATUSES)
                        .isNull(Order::getDeletedAt));

        if (orders.isEmpty()) {
            throw new BusinessException(ResultCode.CHANNEL_ORDERS_EMPTY);
        }

        // 4. 金额计算
        BigDecimal gmv = orders.stream()
                .map(Order::getPaidAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refund = orders.stream()
                .filter(o -> REFUND_STATUSES.contains(o.getStatus()))
                .map(Order::getRefundAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rate = channel.getCommissionRate() == null
                ? BigDecimal.ZERO : channel.getCommissionRate();
        BigDecimal commission = gmv.subtract(refund)
                .multiply(rate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal payable = gmv.subtract(refund).subtract(commission);

        // 5. 订单 ID CSV
        String orderIdsCsv = orders.stream()
                .map(o -> o.getId().toString())
                .collect(Collectors.joining(","));

        // 6. 落库
        ChannelSettlement entity = new ChannelSettlement();
        entity.setSettlementNo(generateSettlementNo());
        entity.setChannelId(channel.getId());
        entity.setChannelCode(channel.getChannelCode());
        entity.setChannelName(channel.getChannelName());
        entity.setPeriodStart(dto.getPeriodStart());
        entity.setPeriodEnd(dto.getPeriodEnd());
        entity.setOrderCount(orders.size());
        entity.setOrderIds(orderIdsCsv);
        entity.setGmvAmount(gmv);
        entity.setRefundAmount(refund);
        entity.setCommissionRate(rate);
        entity.setCommissionAmount(commission);
        entity.setPayableAmount(payable);
        entity.setPaidAmount(BigDecimal.ZERO);
        entity.setStatus(ChannelSettlement.STATUS_PENDING);
        entity.setRemark(dto.getRemark());

        settlementMapper.insert(entity);
        log.info("[结算] 生成结算单 id={}, no={}, channel={}, [{}~{}], 订单 {} 单, GMV={}, 佣金={}, 应付={}",
                entity.getId(), entity.getSettlementNo(), channel.getChannelName(),
                dto.getPeriodStart(), dto.getPeriodEnd(), orders.size(), gmv, commission, payable);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id, SettlementActionDTO dto) {
        ChannelSettlement settlement = mustGet(id);
        if (!ChannelSettlement.STATUS_PENDING.equals(settlement.getStatus())) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_STATUS_INVALID,
                    "当前状态 " + settlement.getStatus() + "，无法确认");
        }
        String staff = dto == null ? null : dto.getConfirmStaff();
        if (!StringUtils.hasText(staff)) {
            staff = "系统";
        }
        settlement.setStatus(ChannelSettlement.STATUS_CONFIRMED);
        settlement.setConfirmTime(LocalDateTime.now());
        settlement.setConfirmStaff(staff);
        settlementMapper.updateById(settlement);
        log.info("[结算] 确认 id={}, staff={}", id, staff);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long id, SettlementActionDTO dto) {
        ChannelSettlement settlement = mustGet(id);
        if (!ChannelSettlement.STATUS_CONFIRMED.equals(settlement.getStatus())) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_STATUS_INVALID,
                    "当前状态 " + settlement.getStatus() + "，无法打款");
        }
        BigDecimal paid = (dto == null || dto.getPaidAmount() == null)
                ? settlement.getPayableAmount() : dto.getPaidAmount();
        if (paid == null || paid.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_AMOUNT_INVALID);
        }
        // 实付金额上限校验：不允许超过应付金额
        BigDecimal payable = settlement.getPayableAmount() == null
                ? BigDecimal.ZERO : settlement.getPayableAmount();
        if (paid.compareTo(payable) > 0) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_AMOUNT_INVALID,
                    "实付金额不能超过应付金额 (应付=" + payable + " 实付=" + paid + ")");
        }
        String tx = dto == null ? null : dto.getPayTransaction();
        if (!StringUtils.hasText(tx)) {
            // 使用 UUID 短串，避免同一毫秒并发产生相同流水号
            tx = "PAY" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }
        settlement.setStatus(ChannelSettlement.STATUS_PAID);
        settlement.setPayTime(LocalDateTime.now());
        settlement.setPayTransaction(tx);
        settlement.setPaidAmount(paid);
        settlementMapper.updateById(settlement);
        log.info("[结算] 打款 id={}, paidAmount={}, tx={}", id, paid, tx);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, SettlementActionDTO dto) {
        ChannelSettlement settlement = mustGet(id);
        if (ChannelSettlement.STATUS_PAID.equals(settlement.getStatus())) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_STATUS_INVALID,
                    "已打款结算单不能作废，请走退款流程");
        }
        String reason = dto == null ? null : dto.getReason();
        settlement.setStatus(ChannelSettlement.STATUS_CANCEL);
        String old = settlement.getRemark();
        settlement.setRemark(old == null ? reason : (old + " | 作废:" + reason));
        settlementMapper.updateById(settlement);
        log.info("[结算] 作废 id={}, reason={}", id, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        ChannelSettlement settlement = mustGet(id);
        if (!ChannelSettlement.STATUS_CANCEL.equals(settlement.getStatus())) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_STATUS_INVALID,
                    "仅 '已作废' 状态的结算单可删除");
        }
        // 软删除：与其他模块保持一致
        ChannelSettlement upd = new ChannelSettlement();
        upd.setId(id);
        upd.setDeletedAt(LocalDateTime.now());
        settlementMapper.updateById(upd);
        log.info("[结算] 删除 id={}", id);
    }

    /* ===== 内部方法 ===== */

    private ChannelSettlement mustGet(Long id) {
        ChannelSettlement settlement = settlementMapper.selectById(id);
        if (settlement == null || settlement.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.CHANNEL_SETTLEMENT_NOT_FOUND);
        }
        return settlement;
    }

    /** 生成结算单号：CS + yyyyMM + 3 位流水（使用 NoGenerator 自动重试兜底） */
    private String generateSettlementNo() {
        String prefix = "CS" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return NoGenerator.generateWithRetry(prefix, "", 3,
                no -> settlementMapper.countBySettlementNo(no) > 0);
    }
}

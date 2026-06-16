package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.ChannelCommissionDTO;
import com.ainanning.ticketing.dto.ChannelQueryDTO;
import com.ainanning.ticketing.dto.ChannelSaveDTO;
import com.ainanning.ticketing.dto.ChannelStatusDTO;
import com.ainanning.ticketing.entity.Channel;
import com.ainanning.ticketing.entity.Order;
import com.ainanning.ticketing.mapper.ChannelMapper;
import com.ainanning.ticketing.mapper.OrderMapper;
import com.ainanning.ticketing.service.ChannelService;
import com.ainanning.ticketing.vo.ChannelOptionVO;
import com.ainanning.ticketing.vo.ChannelStatsVO;
import com.ainanning.ticketing.vo.ChannelVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 渠道业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>主数据管理（CRUD + 状态/佣金调整）</li>
 *   <li>删除前校验该渠道是否有关联订单（有则拒绝，避免历史订单的 channel_code 变孤儿）</li>
 *   <li>佣金比例调整不影响历史结算单——结算单有 commission_rate 快照字段</li>
 *   <li>下拉选项：仅"启用"渠道，按 sort 倒序</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelMapper channelMapper;
    private final OrderMapper orderMapper;

    /* 合法类型集合（与 Order 模块共用同一来源；复用 Channel 实体常量，避免双份维护） */
    private static final Set<String> VALID_TYPES = Set.of(
            Channel.TYPE_OTA, Channel.TYPE_OFFICIAL, Channel.TYPE_MINI,
            Channel.TYPE_APP, Channel.TYPE_SHORT, Channel.TYPE_WECHAT, Channel.TYPE_OTHER);

    @Override
    public PageVO<ChannelVO> page(ChannelQueryDTO query) {
        log.info("[渠道] 分页查询 keyword={}, type={}, status={}",
                query.getKeyword(), query.getChannelType(), query.getStatus());

        Page<Channel> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Channel::getDeletedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Channel::getChannelCode, kw)
                    .or().like(Channel::getChannelName, kw));
        }
        if (StringUtils.hasText(query.getChannelType())) {
            wrapper.eq(Channel::getChannelType, query.getChannelType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Channel::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Channel::getSort)
                .orderByDesc(Channel::getId);

        Page<Channel> result = channelMapper.selectPage(page, wrapper);
        List<ChannelVO> voList = result.getRecords().stream()
                .map(ChannelVO::from)
                .collect(Collectors.toList());
        return PageVO.of(result, voList);
    }

    @Override
    public ChannelVO getById(Long id) {
        Channel channel = channelMapper.selectById(id);
        if (channel == null || channel.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.CHANNEL_NOT_FOUND);
        }
        return ChannelVO.from(channel);
    }

    @Override
    public ChannelVO getByCode(String channelCode) {
        Channel channel = channelMapper.selectOne(
                new LambdaQueryWrapper<Channel>()
                        .eq(Channel::getChannelCode, channelCode)
                        .isNull(Channel::getDeletedAt));
        if (channel == null) {
            throw new BusinessException(ResultCode.CHANNEL_NOT_FOUND);
        }
        return ChannelVO.from(channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(ChannelSaveDTO dto) {
        // 类型校验
        if (!VALID_TYPES.contains(dto.getChannelType())) {
            throw new BusinessException(ResultCode.CHANNEL_TYPE_INVALID);
        }
        // 状态校验
        if (!Channel.STATUS_ENABLED.equals(dto.getStatus())
                && !Channel.STATUS_DISABLED.equals(dto.getStatus())) {
            throw new BusinessException(ResultCode.CHANNEL_STATUS_INVALID);
        }
        // 佣金比例兜底
        if (dto.getCommissionRate() == null) {
            dto.setCommissionRate(BigDecimal.ZERO);
        }
        if (dto.getCommissionRate().compareTo(BigDecimal.ZERO) < 0
                || dto.getCommissionRate().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ResultCode.CHANNEL_COMMISSION_INVALID);
        }

        if (dto.getId() == null) {
            // 新增：编码唯一性
            Long count = channelMapper.selectCount(
                    new LambdaQueryWrapper<Channel>()
                            .eq(Channel::getChannelCode, dto.getChannelCode())
                            .isNull(Channel::getDeletedAt));
            if (count > 0) {
                throw new BusinessException(ResultCode.CHANNEL_CODE_DUPLICATE);
            }
            Channel entity = new Channel();
            BeanUtils.copyProperties(dto, entity);
            channelMapper.insert(entity);
            log.info("[渠道] 新增 channelCode={}, name={}", entity.getChannelCode(), entity.getChannelName());
            return entity.getId();
        } else {
            // 修改
            Channel existing = channelMapper.selectById(dto.getId());
            if (existing == null || existing.getDeletedAt() != null) {
                throw new BusinessException(ResultCode.CHANNEL_NOT_FOUND);
            }
            // 编码若变了，新编码不能冲突
            if (!existing.getChannelCode().equals(dto.getChannelCode())) {
                Long count = channelMapper.selectCount(
                        new LambdaQueryWrapper<Channel>()
                                .eq(Channel::getChannelCode, dto.getChannelCode())
                                .ne(Channel::getId, dto.getId())
                                .isNull(Channel::getDeletedAt));
                if (count > 0) {
                    throw new BusinessException(ResultCode.CHANNEL_CODE_DUPLICATE);
                }
            }
            // 仅 set 允许修改的字段，避免 BeanUtils.copyProperties 把
            // orderCount/totalGmv 等冗余聚合字段清成 null
            existing.setChannelCode(dto.getChannelCode());
            existing.setChannelName(dto.getChannelName());
            existing.setChannelType(dto.getChannelType());
            existing.setIcon(dto.getIcon());
            existing.setIconBg(dto.getIconBg());
            existing.setCommissionRate(dto.getCommissionRate());
            existing.setContactName(dto.getContactName());
            existing.setContactPhone(dto.getContactPhone());
            existing.setSettleAccount(dto.getSettleAccount());
            existing.setSettleBank(dto.getSettleBank());
            existing.setSettleAccountNo(dto.getSettleAccountNo());
            existing.setApiKey(dto.getApiKey());
            existing.setApiEndpoint(dto.getApiEndpoint());
            existing.setDescription(dto.getDescription());
            existing.setStatus(dto.getStatus());
            existing.setSort(dto.getSort());
            channelMapper.updateById(existing);
            log.info("[渠道] 修改 id={}, code={}", existing.getId(), existing.getChannelCode());
            return existing.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Channel channel = channelMapper.selectById(id);
        if (channel == null || channel.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.CHANNEL_NOT_FOUND);
        }
        // 校验：是否有历史订单
        Long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getChannelCode, channel.getChannelCode())
                        .isNull(Order::getDeletedAt));
        if (orderCount > 0) {
            throw new BusinessException(ResultCode.CHANNEL_HAS_ORDERS);
        }
        // 软删除：与其他模块保持一致
        Channel upd = new Channel();
        upd.setId(id);
        upd.setDeletedAt(java.time.LocalDateTime.now());
        channelMapper.updateById(upd);
        log.info("[渠道] 删除 id={}, code={}", id, channel.getChannelCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, ChannelStatusDTO dto) {
        if (!Channel.STATUS_ENABLED.equals(dto.getStatus())
                && !Channel.STATUS_DISABLED.equals(dto.getStatus())) {
            throw new BusinessException(ResultCode.CHANNEL_STATUS_INVALID);
        }
        Channel channel = channelMapper.selectById(id);
        if (channel == null || channel.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.CHANNEL_NOT_FOUND);
        }
        channel.setStatus(dto.getStatus());
        channelMapper.updateById(channel);
        log.info("[渠道] 切状态 id={}, status={}", id, dto.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommission(Long id, ChannelCommissionDTO dto) {
        Channel channel = channelMapper.selectById(id);
        if (channel == null || channel.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.CHANNEL_NOT_FOUND);
        }
        BigDecimal newRate = dto.getCommissionRate();
        if (newRate == null
                || newRate.compareTo(BigDecimal.ZERO) < 0
                || newRate.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ResultCode.CHANNEL_COMMISSION_INVALID);
        }
        BigDecimal oldRate = channel.getCommissionRate();
        channel.setCommissionRate(newRate);
        channelMapper.updateById(channel);
        log.info("[渠道] 调佣 id={}, {} -> {}, 原因={}",
                id, oldRate, newRate, dto.getReason());
    }

    @Override
    public List<ChannelOptionVO> listOptions() {
        List<Channel> list = channelMapper.selectList(
                new LambdaQueryWrapper<Channel>()
                        .eq(Channel::getStatus, Channel.STATUS_ENABLED)
                        .isNull(Channel::getDeletedAt)
                        .orderByDesc(Channel::getSort)
                        .orderByDesc(Channel::getId));
        return list.stream().map(c -> ChannelOptionVO.builder()
                        .id(c.getId())
                        .channelCode(c.getChannelCode())
                        .channelName(c.getChannelName())
                        .channelType(c.getChannelType())
                        .icon(c.getIcon())
                        .iconBg(c.getIconBg())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ChannelStatsVO stats() {
        // 1. 全部未软删渠道
        List<Channel> all = channelMapper.selectList(
                new LambdaQueryWrapper<Channel>().isNull(Channel::getDeletedAt));

        long enabled  = all.stream()
                .filter(c -> Channel.STATUS_ENABLED.equals(c.getStatus())).count();
        long disabled = all.size() - enabled;

        // 2. 单渠道汇总
        List<ChannelStatsVO.ChannelSummary> summaries = all.stream()
                .map(c -> ChannelStatsVO.ChannelSummary.builder()
                        .channelId(c.getId())
                        .channelCode(c.getChannelCode())
                        .channelName(c.getChannelName())
                        .channelType(c.getChannelType())
                        .commissionRate(c.getCommissionRate())
                        .status(c.getStatus())
                        .orderCount(c.getOrderCount() == null ? 0 : c.getOrderCount())
                        .totalGmv(c.getTotalGmv() == null ? BigDecimal.ZERO : c.getTotalGmv())
                        .build())
                .sorted((a, b) -> b.getTotalGmv().compareTo(a.getTotalGmv()))
                .collect(Collectors.toList());

        // 3. 合计
        long totalOrders = summaries.stream().mapToLong(ChannelStatsVO.ChannelSummary::getOrderCount).sum();
        BigDecimal totalGmv = summaries.stream()
                .map(ChannelStatsVO.ChannelSummary::getTotalGmv)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ChannelStatsVO.builder()
                .totalCount(all.size())
                .enabledCount(enabled)
                .disabledCount(disabled)
                .channelList(summaries)
                .totalOrderCount(totalOrders)
                .totalGmv(totalGmv)
                .build();
    }
}

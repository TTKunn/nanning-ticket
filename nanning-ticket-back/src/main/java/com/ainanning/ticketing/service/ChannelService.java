package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.ChannelCommissionDTO;
import com.ainanning.ticketing.dto.ChannelQueryDTO;
import com.ainanning.ticketing.dto.ChannelSaveDTO;
import com.ainanning.ticketing.dto.ChannelStatusDTO;
import com.ainanning.ticketing.vo.ChannelOptionVO;
import com.ainanning.ticketing.vo.ChannelStatsVO;
import com.ainanning.ticketing.vo.ChannelVO;

import java.util.List;

/**
 * 渠道业务接口
 *
 * <p>渠道 = 在线分销通道主数据（OTA / 官网 / 小程序 / APP / 抖音 / 微信 等）。
 * 与订单的关系：{@code order.channel_code} 是对 {@code channel.channel_code} 的弱引用，
 * 本模块负责管理渠道主数据。</p>
 *
 * <p>结算（{@code ChannelSettlementService}）单独拆分，避免本接口过度膨胀。</p>
 *
 * @author nanning-ticket
 */
public interface ChannelService {

    /** 分页查询渠道 */
    PageVO<ChannelVO> page(ChannelQueryDTO query);

    /** 渠道详情 */
    ChannelVO getById(Long id);

    /** 按编码查询 */
    ChannelVO getByCode(String channelCode);

    /** 新增 / 修改渠道（id 为空 = 新增） */
    Long save(ChannelSaveDTO dto);

    /** 删除渠道（有历史订单时拒绝） */
    void deleteById(Long id);

    /** 切换状态 */
    void updateStatus(Long id, ChannelStatusDTO dto);

    /** 调整佣金比例（修改主数据，旧结算单不受影响——已快照） */
    void updateCommission(Long id, ChannelCommissionDTO dto);

    /** 渠道下拉选项（仅启用项） */
    List<ChannelOptionVO> listOptions();

    /** 渠道维度统计（每个渠道一行 + 启用/停用计数） */
    ChannelStatsVO stats();
}

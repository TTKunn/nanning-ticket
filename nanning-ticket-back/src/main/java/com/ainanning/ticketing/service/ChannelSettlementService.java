package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SettlementActionDTO;
import com.ainanning.ticketing.dto.SettlementCreateDTO;
import com.ainanning.ticketing.dto.SettlementQueryDTO;
import com.ainanning.ticketing.vo.ChannelSettlementVO;

/**
 * 渠道结算单业务接口
 *
 * <p>结算单 = 一段时间内（通常一个月）某渠道产生订单的"应付对账单"。
 * 状态机：{@code 待确认 → 已确认 → 已打款}（任一状态都可被作废）</p>
 *
 * <p>与 {@code ChannelService} 的关系：渠道主数据（佣金比例、结算账户）由 ChannelService 管理，
 * 结算单按"快照"原则记录生成时刻的佣金比例——主数据变更不影响已生成结算单。</p>
 *
 * @author nanning-ticket
 */
public interface ChannelSettlementService {

    /** 分页查询结算单 */
    PageVO<ChannelSettlementVO> page(SettlementQueryDTO query);

    /** 结算单详情 */
    ChannelSettlementVO getById(Long id);

    /** 结算单详情（按结算单号） */
    ChannelSettlementVO getByNo(String settlementNo);

    /**
     * 生成结算单（"待确认"状态）
     *
     * <p>按"渠道 ID + 周期"从 order 表聚合 GMV / 退款 / 订单数，扣减佣金比例算出应付金额。
     * 同一渠道同周期内若已存在"待确认 / 已确认 / 已打款"任一状态的结算单则拒绝重复生成。</p>
     */
    Long createSettlement(SettlementCreateDTO dto);

    /** 确认结算单（"待确认" → "已确认"） */
    void confirm(Long id, SettlementActionDTO dto);

    /** 打款（"已确认" → "已打款"，实付金额默认 = payableAmount） */
    void pay(Long id, SettlementActionDTO dto);

    /** 作废（任一状态 → "已作废"，不可逆） */
    void cancel(Long id, SettlementActionDTO dto);

    /** 删除（仅"已作废"状态可硬软删） */
    void deleteById(Long id);
}

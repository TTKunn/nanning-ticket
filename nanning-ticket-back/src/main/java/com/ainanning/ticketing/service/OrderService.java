package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.OrderCancelDTO;
import com.ainanning.ticketing.dto.OrderCreateDTO;
import com.ainanning.ticketing.dto.OrderPayDTO;
import com.ainanning.ticketing.dto.OrderQueryDTO;
import com.ainanning.ticketing.dto.OrderRefundDTO;
import com.ainanning.ticketing.vo.OrderStatsVO;
import com.ainanning.ticketing.vo.OrderVO;

import java.util.List;

/**
 * 在线订单业务接口
 *
 * @author nanning-ticket
 */
public interface OrderService {

    /** 分页查询订单 */
    PageVO<OrderVO> page(OrderQueryDTO query);

    /** 订单详情 */
    OrderVO getById(Long id);

    /** 创建订单（创建即支付，一气呵成） */
    Long create(OrderCreateDTO dto);

    /** 支付（仅"待支付"可调） */
    void pay(Long id, OrderPayDTO dto);

    /** 取消订单（仅"待支付"可调，已出票走 refund） */
    void cancel(Long id, OrderCancelDTO dto);

    /** 全单退款（仅"已出票"可调，voucher 必须全部未使用） */
    void refund(Long id, OrderRefundDTO dto);

    /** 删除订单（仅"已取消"或"已退款"状态） */
    void deleteById(Long id);

    /** 按状态统计（园区 / 渠道维度） */
    OrderStatsVO stats(Long scenicId, String channelCode);

    /** 列出某渠道的所有订单（不分页） */
    List<OrderVO> listByChannelCode(String channelCode);
}

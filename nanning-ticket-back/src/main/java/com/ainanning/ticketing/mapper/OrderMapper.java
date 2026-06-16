package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 在线订单 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 检查订单号是否已存在（用于编号生成去重）
     */
    @Select("SELECT COUNT(*) FROM `order` WHERE order_no = #{orderNo}")
    long countByOrderNo(String orderNo);
}

package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 在线订单明细 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 按订单 ID 查询明细（未软删，按 id 升序）
     */
    @Select("SELECT * FROM order_item " +
            "WHERE order_id = #{orderId} AND deleted_at IS NULL " +
            "ORDER BY id ASC")
    List<OrderItem> selectActiveByOrderId(Long orderId);

    /**
     * 按订单 ID 列表批量查询明细（用于分页后的注入）
     */
    @Select("<script>" +
            "SELECT * FROM order_item WHERE deleted_at IS NULL " +
            "AND order_id IN " +
            "<foreach collection='orderIds' item='oid' open='(' separator=',' close=')'>" +
            "  #{oid}" +
            "</foreach> " +
            "ORDER BY order_id, id" +
            "</script>")
    List<OrderItem> selectActiveByOrderIds(List<Long> orderIds);

    /**
     * 按票据码模糊查找所属明细（用于检票时的反查）
     *
     * <p>注意：voucherCodes 字段以逗号分隔存储，LIKE 模糊匹配。</p>
     * <p>与 {@code SaleItemMapper.selectByVoucherCode} 配对使用：先查 sale_item，
     * 命中失败再查 order_item，从而同时支持窗口售票与在线订单两类票据。</p>
     */
    @Select("SELECT * FROM order_item " +
            "WHERE deleted_at IS NULL " +
            "AND FIND_IN_SET(#{voucherCode}, voucher_codes) > 0 " +
            "ORDER BY id ASC LIMIT 1")
    OrderItem selectByVoucherCode(String voucherCode);
}

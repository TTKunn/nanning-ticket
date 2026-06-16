package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.Voucher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 票据 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {

    /**
     * 按票据码精确查找（用于核销 / 详情）
     */
    @Select("SELECT * FROM voucher " +
            "WHERE voucher_code = #{voucherCode} AND deleted_at IS NULL " +
            "LIMIT 1")
    Voucher selectByVoucherCode(String voucherCode);

    /**
     * 按销售明细 ID 列表查询（用于 Service 在出票时回填 / 退票时批量改状态）
     */
    @Select("<script>" +
            "SELECT * FROM voucher WHERE deleted_at IS NULL " +
            "AND sale_item_id IN " +
            "<foreach collection='saleItemIds' item='sid' open='(' separator=',' close=')'>" +
            "  #{sid}" +
            "</foreach> " +
            "ORDER BY sale_item_id, id" +
            "</script>")
    List<Voucher> selectBySaleItemIds(List<Long> saleItemIds);

    /**
     * 按销售单 ID 列表查询（用于分页后注入 saleNo / 报表聚合）
     */
    @Select("<script>" +
            "SELECT * FROM voucher WHERE deleted_at IS NULL " +
            "AND sale_id IN " +
            "<foreach collection='saleIds' item='sid' open='(' separator=',' close=')'>" +
            "  #{sid}" +
            "</foreach> " +
            "ORDER BY sale_id, id" +
            "</script>")
    List<Voucher> selectBySaleIds(List<Long> saleIds);

    /**
     * 统计指定状态票据数量（首页 dashboard / 库存概览用）
     */
    @Select("SELECT COUNT(*) FROM voucher " +
            "WHERE deleted_at IS NULL AND status = #{status}")
    long countByStatus(String status);

    /**
     * 原子"自增打印次数 + 更新最近打印时间"
     *
     * <p>使用 SQL 自增避免并发下读-改-写的 ABA 问题</p>
     */
    @Update("UPDATE voucher SET " +
            "print_count = print_count + 1, " +
            "last_print_time = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int incrementPrintCount(Long id);

    /**
     * 原子条件更新：仅当 voucher 状态为"待使用"时将其改为"已使用"
     *
     * <p>用于核销场景的并发安全。两次并发核销同一票码：
     * <ul>
     *   <li>第一次：affected rows = 1，状态变更为"已使用"</li>
     *   <li>第二次：affected rows = 0（状态已不再是"待使用"），调用方据此判定为重复核销</li>
     * </ul>
     * </p>
     */
    @Update("UPDATE voucher SET " +
            "status = '已使用', " +
            "use_time = NOW(), " +
            "use_staff_id = #{staffId}, " +
            "use_staff_name = #{staffName}, " +
            "device_id = #{deviceId}, " +
            "device_name = #{deviceName} " +
            "WHERE voucher_code = #{voucherCode} " +
            "AND deleted_at IS NULL " +
            "AND status = '待使用'")
    int markUsedIfUnused(@org.apache.ibatis.annotations.Param("voucherCode") String voucherCode,
                         @org.apache.ibatis.annotations.Param("staffId") Long staffId,
                         @org.apache.ibatis.annotations.Param("staffName") String staffName,
                         @org.apache.ibatis.annotations.Param("deviceId") Long deviceId,
                         @org.apache.ibatis.annotations.Param("deviceName") String deviceName);

    /**
     * 按 sale_item_id 列表统计状态非"待使用"的 voucher 数量
     *
     * <p>用于退款前置校验：若待退明细下存在已核销/已退/已作废的 voucher，整单/部分退应拒绝。</p>
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM voucher WHERE deleted_at IS NULL " +
            "AND sale_item_id IN " +
            "<foreach collection='saleItemIds' item='sid' open='(' separator=',' close=')'>" +
            "  #{sid}" +
            "</foreach> " +
            "AND status != '待使用'" +
            "</script>")
    long countNonUnusedBySaleItemIds(@org.apache.ibatis.annotations.Param("saleItemIds") List<Long> saleItemIds);
}

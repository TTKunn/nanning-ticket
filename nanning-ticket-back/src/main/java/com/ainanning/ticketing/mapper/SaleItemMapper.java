package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.SaleItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 销售明细 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface SaleItemMapper extends BaseMapper<SaleItem> {

    /**
     * 按销售主单 ID 查询明细（未软删）
     */
    @Select("SELECT * FROM sale_item WHERE sale_id = #{saleId} AND deleted_at IS NULL ORDER BY id ASC")
    List<SaleItem> selectActiveBySaleId(Long saleId);

    /**
     * 按销售主单 ID 列表批量查询明细（用于分页后的注入）
     */
    @Select("<script>" +
            "SELECT * FROM sale_item WHERE deleted_at IS NULL " +
            "AND sale_id IN " +
            "<foreach collection='saleIds' item='sid' open='(' separator=',' close=')'>" +
            "  #{sid}" +
            "</foreach> " +
            "ORDER BY sale_id, id" +
            "</script>")
    List<SaleItem> selectActiveBySaleIds(List<Long> saleIds);

    /**
     * 按票据码模糊查找所属明细（用于检票时的反查）
     *
     * <p>注意：voucherCodes 字段以逗号分隔存储，LIKE 模糊匹配</p>
     */
    @Select("SELECT * FROM sale_item " +
            "WHERE deleted_at IS NULL " +
            "AND FIND_IN_SET(#{voucherCode}, voucher_codes) > 0 " +
            "ORDER BY id ASC LIMIT 1")
    SaleItem selectByVoucherCode(String voucherCode);
}

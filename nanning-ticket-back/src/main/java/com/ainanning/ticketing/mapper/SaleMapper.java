package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.Sale;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 销售主单 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface SaleMapper extends BaseMapper<Sale> {

    /**
     * 检查销售流水号是否已存在（用于编号生成去重）
     */
    @Select("SELECT COUNT(*) FROM sale WHERE sale_no = #{saleNo}")
    long countBySaleNo(String saleNo);
}

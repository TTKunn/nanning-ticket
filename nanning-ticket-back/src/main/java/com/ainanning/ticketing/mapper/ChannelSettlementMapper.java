package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.ChannelSettlement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 渠道结算单 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface ChannelSettlementMapper extends BaseMapper<ChannelSettlement> {

    /**
     * 按结算单号前缀统计当日数量（用于生成"CS + yyyyMM + 3 位流水"）
     */
    @Select("SELECT COUNT(*) FROM channel_settlement WHERE settlement_no LIKE CONCAT(#{prefix}, '%')")
    long countBySettlementNoPrefix(@Param("prefix") String prefix);

    /**
     * 检查结算单号是否已存在（用于编号生成去重）
     */
    @Select("SELECT COUNT(*) FROM channel_settlement WHERE settlement_no = #{settlementNo}")
    long countBySettlementNo(@Param("settlementNo") String settlementNo);
}

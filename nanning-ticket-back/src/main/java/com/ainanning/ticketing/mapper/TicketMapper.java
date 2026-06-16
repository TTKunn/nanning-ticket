package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.Ticket;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 票种 Mapper
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，已包含单表 CRUD 的全部方法。
 * 复杂查询通过 {@link com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper} 在 Service 层构造。</p>
 *
 * @author nanning-ticket
 */
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

    /**
     * 按园区 + 编码统计有效记录数（新增时使用）
     */
    @Select("SELECT COUNT(*) FROM ticket WHERE scenic_id = #{scenicId} AND code = #{code} AND deleted_at IS NULL")
    long countByCode(Long scenicId, String code);

    /**
     * 按园区 + 编码统计排除指定 ID 后的有效记录数（修改时使用）
     */
    @Select("SELECT COUNT(*) FROM ticket " +
            "WHERE scenic_id = #{scenicId} AND code = #{code} AND id <> #{excludeId} AND deleted_at IS NULL")
    long countByCodeExcludeId(Long scenicId, String code, Long excludeId);

    /**
     * 统计指定园区下的有效票种数（用于回写 scenic.ticket_count）
     */
    @Select("SELECT COUNT(*) FROM ticket WHERE scenic_id = #{scenicId} AND deleted_at IS NULL")
    long countActiveByScenicId(Long scenicId);
}

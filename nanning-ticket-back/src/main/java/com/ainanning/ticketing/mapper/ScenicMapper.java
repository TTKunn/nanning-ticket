package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.Scenic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 园区 Mapper
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，已包含单表 CRUD 的全部方法。
 * 复杂查询通过 {@link com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper} 在 Service 层构造。</p>
 *
 * @author nanning-ticket
 */
@Mapper
public interface ScenicMapper extends BaseMapper<Scenic> {

    /**
     * 按名称统计有效记录数（不包含已删除）
     *
     * @param name 园区名称
     * @return 有效记录数
     */
    @Select("SELECT COUNT(*) FROM scenic WHERE name = #{name} AND deleted_at IS NULL")
    long countByName(String name);

    /**
     * 按名称统计排除指定 ID 后的有效记录数（修改时使用）
     *
     * @param name      园区名称
     * @param excludeId 排除的园区 ID
     * @return 有效记录数
     */
    @Select("SELECT COUNT(*) FROM scenic WHERE name = #{name} AND id <> #{excludeId} AND deleted_at IS NULL")
    long countByNameExcludeId(String name, Long excludeId);
}

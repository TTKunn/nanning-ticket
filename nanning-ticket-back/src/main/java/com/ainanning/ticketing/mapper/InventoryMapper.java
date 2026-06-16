package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.Inventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存 Mapper
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，已包含单表 CRUD 的全部方法。
 * 复杂查询通过 {@link com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper} 在 Service 层构造。</p>
 *
 * @author nanning-ticket
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 按票种 + 日期统计有效记录数（新增时使用）
     */
    @Select("SELECT COUNT(*) FROM inventory " +
            "WHERE ticket_id = #{ticketId} AND inventory_date = #{date} AND deleted_at IS NULL")
    long countByTicketAndDate(Long ticketId, java.time.LocalDate date);

    /**
     * 按票种 + 日期统计排除指定 ID 后的有效记录数（修改时使用）
     */
    @Select("SELECT COUNT(*) FROM inventory " +
            "WHERE ticket_id = #{ticketId} AND inventory_date = #{date} " +
            "AND id <> #{excludeId} AND deleted_at IS NULL")
    long countByTicketAndDateExcludeId(Long ticketId, java.time.LocalDate date, Long excludeId);

    /**
     * 统计某票种的已售数量（用于删除前校验是否有销售记录）
     */
    @Select("SELECT IFNULL(SUM(sold), 0) FROM inventory " +
            "WHERE ticket_id = #{ticketId} AND deleted_at IS NULL")
    long sumSoldByTicketId(Long ticketId);

    /**
     * 按票种 + 日期加行锁查询（用于下单/售票时并发安全扣减）
     *
     * <p>使用 {@code SELECT ... FOR UPDATE} 在事务内对目标行加排他锁，
     * 避免两个并发请求同时读到相同 {@code available} 后都通过校验导致超卖。
     * 调用方必须处于事务中，锁会在事务提交/回滚时释放。</p>
     */
    @Select("SELECT * FROM inventory " +
            "WHERE deleted_at IS NULL " +
            "AND ticket_id = #{ticketId} AND inventory_date = #{inventoryDate} " +
            "LIMIT 1 FOR UPDATE")
    Inventory selectForUpdate(@Param("ticketId") Long ticketId,
                              @Param("inventoryDate") java.time.LocalDate inventoryDate);
}

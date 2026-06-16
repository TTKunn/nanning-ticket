package com.ainanning.ticketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 库存实体
 *
 * <p>对应数据库表：inventory</p>
 *
 * <p>设计要点：
 * <ul>
 *   <li>按"票种 × 日期"粒度记录库存，遵循票务行业"日期即 SKU"惯例</li>
 *   <li>{@code scenicId} 从 {@code ticket} 冗余过来，便于按园区查询库存，免去连表</li>
 *   <li>{@code sold} / {@code reserved} / {@code available} 在销售/退款/取消预占等流程中维护，
 *       本模块只负责 CRUD，不参与实际扣减逻辑（后续由订单/售票模块负责）</li>
 *   <li>{@code status} 手动控制：开放 / 关闭；售罄在写入时由 service 根据库存自动判断</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
@Schema(description = "库存")
public class Inventory extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "所属园区 ID（冗余自 ticket.scenic_id）")
    private Long scenicId;

    @Schema(description = "库存日期（票的入场日期）")
    private LocalDate inventoryDate;

    @Schema(description = "总库存")
    private Integer total;

    @Schema(description = "已售数量")
    private Integer sold;

    @Schema(description = "预占数量（未付款）")
    private Integer reserved;

    @Schema(description = "可用库存（= total - sold - reserved）")
    private Integer available;

    @Schema(description = "状态：开放/关闭/售罄")
    private String status;

    @Schema(description = "备注")
    private String remark;
}

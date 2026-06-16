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
import java.math.BigDecimal;

/**
 * 票种实体
 *
 * <p>对应数据库表：ticket</p>
 *
 * <p>字段设计要点：
 * <ul>
 *   <li>{@code category} 取值：单票 / 套票 / 联票</li>
 *   <li>{@code price} 为销售价，{@code costPrice} 为成本价（用于对账，可选）</li>
 *   <li>{@code ruleIds} 与 {@code tags} 在数据库以逗号分隔字符串存储（{@code "1,2,3"}），
 *       在 Service 层与 {@code List} 互转，避免再多一张关联表带来的复杂度</li>
 *   <li>{@code refundable} 用 TINYINT(1) 存储 0/1，对应不可退 / 可退</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("ticket")
@Schema(description = "票种")
public class Ticket extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属园区 ID")
    private Long scenicId;

    @Schema(description = "票种名称")
    private String name;

    @Schema(description = "票种编码（同一园区下唯一）")
    private String code;

    @Schema(description = "分类：单票/套票/联票")
    private String category;

    @Schema(description = "票面价（销售价）")
    private BigDecimal price;

    @Schema(description = "成本价（用于对账）")
    private BigDecimal costPrice;

    @Schema(description = "票种说明")
    private String description;

    @Schema(description = "封面图 URL")
    private String cover;

    @Schema(description = "标签（逗号分隔：热销/推荐/限时）")
    private String tags;

    @Schema(description = "入场有效天数（购票后 N 天内有效）")
    private Integer validDays;

    @Schema(description = "是否可退：1=可退 0=不可退")
    private Integer refundable;

    @Schema(description = "关联规则 ID（逗号分隔）")
    private String ruleIds;

    @Schema(description = "状态：在售/停售")
    private String status;

    @Schema(description = "排序值，越大越靠前")
    private Integer sort;
}

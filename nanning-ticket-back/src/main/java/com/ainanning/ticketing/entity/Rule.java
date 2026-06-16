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
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 项目规则实体
 *
 * <p>对应数据库表：rule</p>
 *
 * <p>规则类型（type 字段）支持以下枚举值：
 * <ul>
 *   <li>折扣：费率折扣类规则（如儿童 8 折）</li>
 *   <li>免票：满足条件免票（如老人免票）</li>
 *   <li>团体：团体优惠（满 N 人打折）</li>
 *   <li>时段：时间段差异化定价</li>
 *   <li>限流：数量上限控制</li>
 * </ul>
 *
 * <p>config 字段为 JSON 字符串，保存各类规则的具体参数，业务侧按需解析。
 * 之所以使用 JSON 而非拆表，是为了让不同类型规则共享同一张表、降低后续扩展成本。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("rule")
@Schema(description = "项目规则")
public class Rule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属园区 ID")
    private Long scenicId;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则编码（同一园区下唯一）")
    private String code;

    @Schema(description = "类型：折扣/免票/团体/时段/限流")
    private String type;

    @Schema(description = "规则说明")
    private String description;

    @Schema(description = "规则参数 JSON（按 type 解析）")
    private String config;

    @Schema(description = "优先级，数值越大越优先")
    private Integer priority;

    @Schema(description = "状态：启用/禁用")
    private String status;

    @Schema(description = "生效开始日期")
    private LocalDate effectiveFrom;

    @Schema(description = "生效结束日期")
    private LocalDate effectiveTo;
}

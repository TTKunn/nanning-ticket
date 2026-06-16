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
import java.math.BigDecimal;

/**
 * 园区实体
 *
 * <p>对应数据库表：scenic</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("scenic")
@Schema(description = "园区")
public class Scenic extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "园区名称")
    private String name;

    @Schema(description = "园区图标（单字或 emoji）")
    private String icon;

    @Schema(description = "园区图标背景色 HEX")
    private String iconBg;

    @Schema(description = "景区等级（国家5A级/城市公园等）")
    private String level;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "开放时间（例：08:00-18:00）")
    private String openTime;

    @Schema(description = "园区说明")
    private String description;

    @Schema(description = "收费项目数（冗余，由系统聚合）")
    private Integer projectCount;

    @Schema(description = "规则数（冗余，由系统聚合）")
    private Integer ruleCount;

    @Schema(description = "票种数（冗余，由系统聚合）")
    private Integer ticketCount;

    @Schema(description = "本月销售额（冗余，由系统聚合）")
    private BigDecimal monthSales;

    @Schema(description = "状态：运营中 / 暂停运营")
    private String status;

    @Schema(description = "排序值，越大越靠前")
    private Integer sort;
}

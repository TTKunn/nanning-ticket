package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Scenic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 园区视图对象（用于 API 响应）
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "园区视图")
public class ScenicVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "园区名称")
    private String name;

    @Schema(description = "园区图标")
    private String icon;

    @Schema(description = "园区图标背景色")
    private String iconBg;

    @Schema(description = "景区等级")
    private String level;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "开放时间")
    private String openTime;

    @Schema(description = "园区说明")
    private String description;

    @Schema(description = "收费项目数")
    private Integer projectCount;

    @Schema(description = "规则数")
    private Integer ruleCount;

    @Schema(description = "票种数")
    private Integer ticketCount;

    @Schema(description = "本月销售额")
    private BigDecimal monthSales;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序值")
    private Integer sort;

    /** Entity → VO 转换 */
    public static ScenicVO from(Scenic entity) {
        if (entity == null) {
            return null;
        }
        return ScenicVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .iconBg(entity.getIconBg())
                .level(entity.getLevel())
                .address(entity.getAddress())
                .openTime(entity.getOpenTime())
                .description(entity.getDescription())
                .projectCount(entity.getProjectCount())
                .ruleCount(entity.getRuleCount())
                .ticketCount(entity.getTicketCount())
                .monthSales(entity.getMonthSales())
                .status(entity.getStatus())
                .sort(entity.getSort())
                .build();
    }
}

package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Rule;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目规则视图对象（用于 API 响应）
 *
 * <p>与 Entity 相比，VO 不会暴露 deleted_at 等内部字段，且日期字段使用统一格式化。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "规则视图")
public class RuleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属园区 ID")
    private Long scenicId;

    @Schema(description = "所属园区名称")
    private String scenicName;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则编码")
    private String code;

    @Schema(description = "类型：折扣/免票/团体/时段/限流")
    private String type;

    @Schema(description = "规则说明")
    private String description;

    @Schema(description = "规则参数 JSON")
    private String config;

    @Schema(description = "优先级，数值越大越优先")
    private Integer priority;

    @Schema(description = "状态：启用/禁用")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "生效开始日期")
    private LocalDate effectiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "生效结束日期")
    private LocalDate effectiveTo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /** Entity → VO 转换（默认不填 scenicName，由 Service 注入） */
    public static RuleVO from(Rule entity) {
        if (entity == null) {
            return null;
        }
        return RuleVO.builder()
                .id(entity.getId())
                .scenicId(entity.getScenicId())
                .name(entity.getName())
                .code(entity.getCode())
                .type(entity.getType())
                .description(entity.getDescription())
                .config(entity.getConfig())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 项目规则新增 / 修改参数
 *
 * <p>id 为空时表示新增，非空时表示修改。
 * config 字段为字符串形式的 JSON，保存各类型规则的具体参数。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "规则新增/修改参数")
public class RuleSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（修改时必填）")
    private Long id;

    @NotNull(message = "所属园区不能为空")
    @Schema(description = "所属园区 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long scenicId;

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 64, message = "规则名称不能超过 64 个字符")
    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "规则编码不能为空")
    @Size(max = 32, message = "规则编码不能超过 32 个字符")
    @Schema(description = "规则编码（同一园区下唯一，建议大写英文加下划线）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "规则类型不能为空")
    @Schema(description = "类型：折扣/免票/团体/时段/限流",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Size(max = 255, message = "规则说明不能超过 255 个字符")
    @Schema(description = "规则说明")
    private String description;

    @Schema(description = "规则参数 JSON 字符串")
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

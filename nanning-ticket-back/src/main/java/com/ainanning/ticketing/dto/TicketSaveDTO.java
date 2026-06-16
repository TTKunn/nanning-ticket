package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 票种新增 / 修改参数
 *
 * <p>id 为空时表示新增，非空时表示修改。
 * ruleIds 与 tags 以前端数组方式传入，Service 层负责与逗号分隔字符串互转。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "票种新增/修改参数")
public class TicketSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（修改时必填）")
    private Long id;

    @NotNull(message = "所属园区不能为空")
    @Schema(description = "所属园区 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long scenicId;

    @NotBlank(message = "票种名称不能为空")
    @Size(max = 64, message = "票种名称不能超过 64 个字符")
    @Schema(description = "票种名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "票种编码不能为空")
    @Size(max = 32, message = "票种编码不能超过 32 个字符")
    @Schema(description = "票种编码（同一园区下唯一，建议大写英文加下划线）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "票种分类不能为空")
    @Schema(description = "分类：单票/套票/联票", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @NotNull(message = "票面价不能为空")
    @DecimalMin(value = "0.00", inclusive = true, message = "票面价不能为负数")
    @Schema(description = "票面价（销售价），允许 0 表示免费票",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "成本价（用于对账，可选）")
    private BigDecimal costPrice;

    @Size(max = 255, message = "票种说明不能超过 255 个字符")
    @Schema(description = "票种说明")
    private String description;

    @Schema(description = "封面图 URL")
    private String cover;

    @Schema(description = "标签列表（如 热销/推荐/限时）")
    private List<String> tags;

    @Min(value = 1, message = "入场有效天数至少为 1")
    @Max(value = 365, message = "入场有效天数不能超过 365")
    @Schema(description = "入场有效天数（购票后 N 天内有效）")
    private Integer validDays;

    @Schema(description = "是否可退：true=可退 false=不可退")
    private Boolean refundable;

    @Schema(description = "关联规则 ID 列表")
    private List<Long> ruleIds;

    @Schema(description = "状态：在售/停售")
    private String status;

    @Schema(description = "排序值，越大越靠前")
    private Integer sort;
}

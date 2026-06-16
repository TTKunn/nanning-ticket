package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 销售明细创建 DTO（嵌入 SaleCreateDTO）
 *
 * <p>表示一种票 × 一个入场日期的购买明细；可多个明细合并为一次窗口出票。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "销售明细创建参数")
public class SaleItemCreateDTO {

    @NotNull(message = "票种 ID 不能为空")
    @Schema(description = "票种 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ticketId;

    @NotNull(message = "入场日期不能为空")
    @Schema(description = "入场日期", example = "2026-06-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate inventoryDate;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须 ≥ 1")
    @Schema(description = "购买数量", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Schema(description = "销售单价（不传则取票种价）", example = "60.00")
    private BigDecimal unitPrice;

    @Schema(description = "优惠金额（不传则按规则计算或 0）", example = "0.00")
    private BigDecimal discountAmount;

    @Schema(description = "应用的规则 ID（不传则取票种关联规则）")
    private List<Long> ruleIds;

    @Schema(description = "备注")
    private String remark;
}

package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 订单明细创建 DTO
 *
 * <p>嵌入在 {@link OrderCreateDTO#items} 中。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "订单明细创建参数")
public class OrderItemCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "票种 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "票种 ID 不能为空")
    private Long ticketId;

    @Schema(description = "入场日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入场日期不能为空")
    private LocalDate inventoryDate;

    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须 ≥ 1")
    private Integer quantity;

    @Schema(description = "销售单价（可空 = 取票种价）")
    private BigDecimal unitPrice;

    @Schema(description = "优惠金额（可空 = 0）")
    private BigDecimal discountAmount;

    @Schema(description = "应用规则 ID 列表（可空）")
    private List<Long> ruleIds;

    @Schema(description = "备注")
    private String remark;
}

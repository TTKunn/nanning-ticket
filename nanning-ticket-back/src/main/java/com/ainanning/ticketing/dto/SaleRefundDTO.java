package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 退票 DTO
 *
 * <p>支持"整单退"与"部分退"：
 * <ul>
 *   <li>整单退：{@code items} 为空或 null，服务端按主单全部明细全退</li>
 *   <li>部分退：{@code items} 列出要退的明细及数量</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "退票参数")
public class SaleRefundDTO {

    @Schema(description = "退票原因 / 备注")
    private String reason;

    @Schema(description = "部分退明细（空/缺省=全单退）")
    @Valid
    private List<RefundItem> items;

    @Data
    @Schema(description = "单条明细退票数量")
    public static class RefundItem {

        @NotNull(message = "明细 ID 不能为空")
        @Schema(description = "销售明细 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long saleItemId;

        @NotNull(message = "退票数量不能为空")
        @Min(value = 1, message = "退票数量必须 ≥ 1")
        @Schema(description = "本次退票数量", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer quantity;

        @Schema(description = "单条退票备注")
        private String remark;
    }
}

package com.ainanning.ticketing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 库存新增 / 修改参数（单日）
 *
 * <p>id 为空时表示新增，非空时表示修改。
 * 修改时允许调整 total / status / remark，sold / reserved / available 由销售流程维护，本接口不接收。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "库存新增/修改参数")
public class InventorySaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（修改时必填）")
    private Long id;

    @NotNull(message = "票种不能为空")
    @Schema(description = "票种 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ticketId;

    @NotNull(message = "库存日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "库存日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate inventoryDate;

    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能为负数")
    @Schema(description = "总库存", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer total;

    @Schema(description = "状态：开放/关闭/售罄")
    private String status;

    @Schema(description = "备注")
    private String remark;
}

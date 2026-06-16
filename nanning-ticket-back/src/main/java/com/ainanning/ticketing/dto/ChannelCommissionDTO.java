package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 渠道佣金比例调整 DTO
 *
 * <p>专用于"批量调佣"场景：调整比例会记录在结算单生成时（快照字段 commission_rate），
 * 因此历史结算单不受影响——这是"主数据修改"与"快照"分离的标准模式。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "渠道佣金比例调整参数")
public class ChannelCommissionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "佣金比例不能为空")
    @DecimalMin(value = "0.00", message = "佣金比例不能小于 0")
    @DecimalMax(value = "100.00", message = "佣金比例不能大于 100")
    @Schema(description = "新的佣金比例（百分比 0-100）", example = "10.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal commissionRate;

    @Schema(description = "调整原因 / 备注")
    private String reason;
}

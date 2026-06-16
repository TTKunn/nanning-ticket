package com.ainanning.ticketing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 渠道结算单生成 DTO
 *
 * <p>指定渠道 + 结算起止日期，系统将自动从 order 表聚合该段时间内的"已出票/已退款"订单，
 * 计算 GMV、退款、佣金、应付金额，生成一条"待确认"状态的结算单。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "渠道结算单生成参数")
public class SettlementCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "渠道 ID 不能为空")
    @Schema(description = "渠道 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long channelId;

    @NotNull(message = "结算起始日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结算起始日（含）", example = "2026-06-01",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate periodStart;

    @NotNull(message = "结算截止日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结算截止日（含）", example = "2026-06-30",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate periodEnd;

    @Schema(description = "备注")
    private String remark;
}

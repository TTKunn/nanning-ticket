package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 渠道结算单动作 DTO
 *
 * <p>复用 DTO：确认（confirmStaff）/ 打款（payTransaction, paidAmount）/ 作废（reason）三处共用。
 * 字段按需传入，未填字段忽略。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "渠道结算单动作参数")
public class SettlementActionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "确认人（confirm 用）")
    private String confirmStaff;

    @Schema(description = "打款流水号（pay 用）")
    private String payTransaction;

    @Schema(description = "实付金额（pay 用；不传则默认按 payableAmount）")
    private BigDecimal paidAmount;

    @Schema(description = "作废原因（cancel 用）")
    private String reason;
}

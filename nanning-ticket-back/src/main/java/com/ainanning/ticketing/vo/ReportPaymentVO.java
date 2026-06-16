package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 支付方式分布
 *
 * <p>聚合窗口售票（sale.payment_method）+ 在线订单（order.pay_method）的支付方式占比。
 * 用于"哪种支付最受用户欢迎"的决策。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "支付方式分布")
public class ReportPaymentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "统计起始日")
    private String dateFrom;

    @Schema(description = "统计截止日")
    private String dateTo;

    @Schema(description = "园区 ID（可空）")
    private Long scenicId;

    @Schema(description = "总单数（窗口 + 在线）")
    private long totalCount;

    @Schema(description = "总 GMV")
    private BigDecimal totalGmv;

    @Schema(description = "支付方式汇总行（按 GMV 倒序）")
    private List<PaymentRow> rows;

    /**
     * 单行
     */
    @Data
    @Builder
    @Schema(description = "支付方式单行")
    public static class PaymentRow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "支付方式（现金/微信/支付宝/银行卡/余额）")
        private String payMethod;

        @Schema(description = "单数")
        private long count;

        @Schema(description = "GMV")
        private BigDecimal gmv;

        @Schema(description = "占比 = gmv / totalGmv × 100%")
        private Double percentage;
    }
}

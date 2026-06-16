package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 报表总览（Dashboard 顶部 4-6 个数字卡片）
 *
 * <p>由 ReportService 一次性聚合所有核心指标，避免前端多次请求。
 * 周期内的"窗口售票 + 在线订单"统一折算为 GMV；退款金额为周期内"已退款 + 部分退款"订单之和。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "报表总览（核心指标卡）")
public class ReportOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "统计起始日")
    private String dateFrom;

    @Schema(description = "统计截止日")
    private String dateTo;

    @Schema(description = "园区 ID（可空）")
    private Long scenicId;

    @Schema(description = "渠道编码（可空）")
    private String channelCode;

    /* ===== 销售指标 ===== */
    @Schema(description = "窗口销售单数")
    private long saleCount;

    @Schema(description = "在线订单数")
    private long orderCount;

    @Schema(description = "售票总单数 = saleCount + orderCount")
    private long totalTicketCount;

    @Schema(description = "窗口销售 GMV")
    private BigDecimal saleGmv;

    @Schema(description = "在线订单 GMV")
    private BigDecimal orderGmv;

    @Schema(description = "总 GMV（销售 + 订单）")
    private BigDecimal totalGmv;

    @Schema(description = "退款金额（已退 + 部分退）")
    private BigDecimal refundAmount;

    @Schema(description = "净收入 = totalGmv - refundAmount")
    private BigDecimal netRevenue;

    /* ===== 票据 / 核销指标 ===== */
    @Schema(description = "出票数（voucher 总数）")
    private long voucherIssued;

    @Schema(description = "已使用 voucher 数")
    private long voucherUsed;

    @Schema(description = "已退 voucher 数")
    private long voucherRefunded;

    @Schema(description = "已作废 voucher 数")
    private long voucherRevoked;

    @Schema(description = "核销率 = voucherUsed / voucherIssued × 100%")
    private Double useRate;

    /* ===== 库存指标 ===== */
    @Schema(description = "可售库存（所有开放日 total 之和）")
    private long inventoryTotal;

    @Schema(description = "已售库存（所有开放日 sold 之和）")
    private long inventorySold;

    @Schema(description = "库存售出率 = sold / total × 100%")
    private Double inventorySellRate;
}

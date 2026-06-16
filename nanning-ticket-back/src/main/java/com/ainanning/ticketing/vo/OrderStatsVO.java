package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单状态统计 VO
 *
 * <p>用于管理首页 / 园区详情 / 渠道详情等场景，
 * 一次性返回 6 个状态的数量 + 4 个关键金额，避免前端多次请求。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "订单状态统计")
public class OrderStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "园区 ID（可空）")
    private Long scenicId;

    @Schema(description = "渠道编码（可空）")
    private String channelCode;

    @Schema(description = "待支付")
    private long pendingCount;

    @Schema(description = "已出票")
    private long fulfilledCount;

    @Schema(description = "已取消")
    private long cancelledCount;

    @Schema(description = "退款中")
    private long refundingCount;

    @Schema(description = "已退款")
    private long refundedCount;

    @Schema(description = "部分退款")
    private long partialCount;

    @Schema(description = "订单合计（未软删）")
    private long totalCount;

    @Schema(description = "GMV 合计（实付金额 = paidAmount）")
    private BigDecimal gmvAmount;

    @Schema(description = "已退款金额合计")
    private BigDecimal refundAmount;

    @Schema(description = "出票率 = 已出票 / 合计 × 100%")
    private Double fulfillRate;
}

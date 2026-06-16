package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 报表趋势（折线图 / 柱状图）
 *
 * <p>按时间粒度（DAY/WEEK/MONTH）聚合，x 轴是 {@code bucket}，y 轴有 4 个序列：
 * saleCount / orderCount / saleGmv / orderGmv。
 * 同一接口的形状固定，前端用一个图表组件即可渲染（双 Y 轴或多图表切换）。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "报表趋势（按时间序列）")
public class ReportTrendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "时间粒度：DAY/WEEK/MONTH")
    private String interval;

    @Schema(description = "统计起始日")
    private String dateFrom;

    @Schema(description = "统计截止日")
    private String dateTo;

    @Schema(description = "园区 ID（可空）")
    private Long scenicId;

    @Schema(description = "渠道编码（可空）")
    private String channelCode;

    @Schema(description = "时间桶（升序）")
    private List<TrendBucket> buckets;

    /**
     * 单个时间桶
     */
    @Data
    @Builder
    @Schema(description = "趋势单点")
    public static class TrendBucket implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "时间桶起点（如 2026-06-01）")
        private String bucket;

        @Schema(description = "时间桶结束（包含）")
        private String bucketEnd;

        @Schema(description = "窗口售票单数")
        private long saleCount;

        @Schema(description = "窗口售票 GMV")
        private BigDecimal saleGmv;

        @Schema(description = "在线订单数")
        private long orderCount;

        @Schema(description = "在线订单 GMV")
        private BigDecimal orderGmv;

        @Schema(description = "总单数")
        private long totalCount;

        @Schema(description = "总 GMV")
        private BigDecimal totalGmv;

        @Schema(description = "退款金额")
        private BigDecimal refundAmount;

        @Schema(description = "核销 voucher 数")
        private long useCount;
    }
}

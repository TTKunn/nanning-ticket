package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 渠道维度统计 VO
 *
 * <p>给"渠道总览"页使用：每个渠道一行汇总（订单数 / GMV / 退款 / 佣金 / 应付），
 * 同时按"启用 / 停用"维度计数。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "渠道维度统计")
public class ChannelStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道总数（未软删）")
    private long totalCount;

    @Schema(description = "启用渠道数")
    private long enabledCount;

    @Schema(description = "停用渠道数")
    private long disabledCount;

    @Schema(description = "各渠道汇总行（按 GMV 倒序）")
    private List<ChannelSummary> channelList;

    @Schema(description = "历史 GMV 合计（来自 channel.totalGmv 冗余）")
    private BigDecimal totalGmv;

    @Schema(description = "历史订单合计（来自 channel.orderCount 冗余）")
    private long totalOrderCount;

    /**
     * 单渠道汇总行
     */
    @Data
    @Builder
    @Schema(description = "单渠道汇总")
    public static class ChannelSummary implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "渠道 ID")
        private Long channelId;

        @Schema(description = "渠道编码")
        private String channelCode;

        @Schema(description = "渠道名称")
        private String channelName;

        @Schema(description = "渠道类型")
        private String channelType;

        @Schema(description = "佣金比例")
        private BigDecimal commissionRate;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "历史订单数")
        private Integer orderCount;

        @Schema(description = "历史 GMV")
        private BigDecimal totalGmv;
    }
}

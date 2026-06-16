package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 检票漏斗（订单 → 核销转化）
 *
 * <p>在线下单（pay / create） → 出票（voucher） → 入园（核销） 各级转化率。
 * 这是订单模块的关键运营指标："售出 / 核销" 反映用户实际到场率。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "检票转化漏斗")
public class ReportVisitFunnelVO implements Serializable {

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

    @Schema(description = "漏斗各级（按顺序：下单 → 出票 → 核销）")
    private List<FunnelStep> steps;

    @Schema(description = "下单→出票 出票率 = issuedCount / orderCount × 100%")
    private Double orderToIssueRate;

    @Schema(description = "出票→核销 核销率 = usedCount / issuedCount × 100%")
    private Double issueToUseRate;

    @Schema(description = "下单→核销 综合转化 = usedCount / orderCount × 100%")
    private Double orderToUseRate;

    /**
     * 漏斗单级
     */
    @Data
    @Builder
    @Schema(description = "漏斗单级")
    public static class FunnelStep implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "步骤序号（1 开始）")
        private Integer step;

        @Schema(description = "步骤名")
        private String name;

        @Schema(description = "该级数量")
        private long count;

        @Schema(description = "相对上一步的转化率（百分比，0~100）")
        private Double conversionRate;
    }
}

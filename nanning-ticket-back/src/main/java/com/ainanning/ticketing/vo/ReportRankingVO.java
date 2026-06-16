package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 报表排名（按某维度分组 + 排序）
 *
 * <p>通用排名 VO：根据 groupBy 参数（CHANNEL/SCENIC/TICKET/PAY_METHOD/WINDOW）动态返回不同维度的排名。
 * 每行包含 dimKey / dimName + 4 个核心指标（count / gmv / refund / netRevenue）。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "报表排名（按维度）")
public class ReportRankingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分组维度：CHANNEL/SCENIC/TICKET/PAY_METHOD/WINDOW")
    private String groupBy;

    @Schema(description = "统计起始日")
    private String dateFrom;

    @Schema(description = "统计截止日")
    private String dateTo;

    @Schema(description = "园区 ID（可空）")
    private Long scenicId;

    @Schema(description = "总行数")
    private long totalRows;

    @Schema(description = "排名行（按 totalGmv 倒序）")
    private List<RankingRow> rows;

    /**
     * 单行排名
     */
    @Data
    @Builder
    @Schema(description = "排名单行")
    public static class RankingRow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "排名（1 开始）")
        private Integer rank;

        @Schema(description = "维度主键（按 groupBy 不同含义不同：CHANNEL=code, SCENIC/TICKET=id, WINDOW=name, PAY_METHOD=key）")
        private String dimKey;

        @Schema(description = "维度名称")
        private String dimName;

        @Schema(description = "辅助类型（如 CHANNEL=OTA/SCENIC=国家5A级）")
        private String dimType;

        @Schema(description = "单数（销售/订单/票数，含义按 groupBy 走）")
        private long count;

        @Schema(description = "GMV")
        private BigDecimal gmv;

        @Schema(description = "退款金额")
        private BigDecimal refundAmount;

        @Schema(description = "净收入 = gmv - refundAmount")
        private BigDecimal netRevenue;

        @Schema(description = "占比 = gmv / 总 GMV × 100%")
        private Double percentage;
    }
}

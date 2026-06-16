package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 库存与售票日报
 *
 * <p>按日期 + 票种展开：每行一个 (date × ticket) 组合，含 total / sold / available / useRate。
 * 用于运营视角："今天哪些票种快卖完了？"</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "库存与售票日报")
public class ReportInventoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "统计起始日")
    private String dateFrom;

    @Schema(description = "统计截止日")
    private String dateTo;

    @Schema(description = "园区 ID（可空）")
    private Long scenicId;

    @Schema(description = "总行数")
    private long totalRows;

    @Schema(description = "可售总量合计")
    private long totalAvailable;

    @Schema(description = "已售总量合计")
    private long totalSold;

    @Schema(description = "整体售出率")
    private Double overallSellRate;

    @Schema(description = "日报行（按日期、票种聚合）")
    private List<InventoryRow> rows;

    /**
     * 单行：日期 + 票种 + 库存 + 售出
     */
    @Data
    @Builder
    @Schema(description = "库存日报单行")
    public static class InventoryRow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "日期")
        private String inventoryDate;

        @Schema(description = "票种 ID")
        private Long ticketId;

        @Schema(description = "票种名称")
        private String ticketName;

        @Schema(description = "园区 ID")
        private Long scenicId;

        @Schema(description = "园区名称")
        private String scenicName;

        @Schema(description = "可售总量")
        private Integer total;

        @Schema(description = "已售")
        private Integer sold;

        @Schema(description = "剩余 = total - sold")
        private Integer available;

        @Schema(description = "库存状态：开放/关闭/售罄")
        private String status;

        @Schema(description = "售出率 = sold / total × 100%")
        private Double sellRate;
    }
}

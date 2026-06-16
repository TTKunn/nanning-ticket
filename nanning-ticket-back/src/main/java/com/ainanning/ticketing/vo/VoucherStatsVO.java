package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 票据状态统计 VO
 *
 * <p>用于管理首页 / 园区详情 / 单票种概览等场景，
 * 一次性返回 4 个状态的数量，避免前端 4 次请求。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "票据状态统计")
public class VoucherStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "园区 ID（可空，代表全园区）")
    private Long scenicId;

    @Schema(description = "票种 ID（可空，代表全票种）")
    private Long ticketId;

    @Schema(description = "销售单 ID（可空，代表全销售单）")
    private Long saleId;

    @Schema(description = "待使用")
    private long unusedCount;

    @Schema(description = "已使用")
    private long usedCount;

    @Schema(description = "已退")
    private long refundCount;

    @Schema(description = "已作废")
    private long revokedCount;

    @Schema(description = "合计（未软删）")
    private long totalCount;

    /** 使用率 = 已使用 / 合计 × 100%（保留 2 位小数） */
    @Schema(description = "核销率（百分比，0-100）")
    private Double usageRate;
}

package com.ainanning.ticketing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 报表通用查询条件
 *
 * <p>所有报表接口的入参都基于这个 DTO，附加具体业务字段用 extends 扩展。
 * 报表查询有时间窗口限制（{@code REPORT_DATE_RANGE_TOO_LARGE} = 366 天），
 * 避免一次性扫表过久导致接口超时。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "报表通用查询条件")
public class ReportQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "统计起始日（含）", example = "2026-05-01")
    private LocalDate dateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "统计截止日（含）", example = "2026-06-14")
    private LocalDate dateTo;

    @Schema(description = "园区 ID（可空，全园区）")
    private Long scenicId;

    @Schema(description = "渠道编码（可空，全渠道）")
    private String channelCode;

    /* ===== 时间粒度（trend 接口使用） ===== */
    public static final String INTERVAL_DAY   = "DAY";
    public static final String INTERVAL_WEEK  = "WEEK";
    public static final String INTERVAL_MONTH = "MONTH";

    @Schema(description = "时间粒度：DAY/WEEK/MONTH（trend 接口使用）", example = "DAY")
    private String interval;

    /* ===== 分组维度（ranking 接口使用） ===== */
    public static final String GROUP_BY_CHANNEL   = "CHANNEL";
    public static final String GROUP_BY_SCENIC    = "SCENIC";
    public static final String GROUP_BY_TICKET    = "TICKET";
    public static final String GROUP_BY_PAY_METHOD = "PAY_METHOD";
    public static final String GROUP_BY_WINDOW     = "WINDOW";

    @Schema(description = "分组维度：CHANNEL/SCENIC/TICKET/PAY_METHOD/WINDOW", example = "CHANNEL")
    private String groupBy;
}

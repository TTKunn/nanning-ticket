package com.ainanning.ticketing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 库存批量更新参数
 *
 * <p>支持两种"目标范围"指定方式（可同时使用并取并集）：
 * <ul>
 *   <li>{@code ticketId} + {@code dateFrom} / {@code dateTo}：按票种和日期区间过滤</li>
 *   <li>{@code dates}：显式列出要操作的日期（与上面规则可叠加）</li>
 * </ul>
 *
 * <p>{@code operation} 决定写入哪些字段：
 * <ul>
 *   <li>{@code SET_TOTAL}：用 {@code total} 覆盖（自动校验 ≥ sold）</li>
 *   <li>{@code INCREMENT}：在原 total 上加 {@code delta}（可负）</li>
 *   <li>{@code DECREMENT}：在原 total 上减 {@code delta}（不允许 < sold）</li>
 *   <li>{@code SET_STATUS}：写入 {@code status}（开放/关闭）</li>
 *   <li>{@code SET_REMARK}：写入 {@code remark}</li>
 * </ul>
 *
 * <p>返回结果由 {@code BatchOpResultVO} 告知"成功 N 条 / 跳过 N 条 / 跳过原因"。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "库存批量更新参数")
public class InventoryBatchUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "票种 ID（不传则作用于所有命中票种）")
    private Long ticketId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "起始日期（含）")
    private LocalDate dateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期（含）")
    private LocalDate dateTo;

    @Schema(description = "显式指定的日期列表（与 dateFrom~dateTo 取并集）")
    private List<LocalDate> dates;

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型：SET_TOTAL / INCREMENT / DECREMENT / SET_STATUS / SET_REMARK",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String operation;

    @Schema(description = "目标 total（SET_TOTAL 使用）")
    private Integer total;

    @Schema(description = "增减量（INCREMENT / DECREMENT 使用，可负）")
    private Integer delta;

    @Schema(description = "目标状态（SET_STATUS 使用：开放/关闭）")
    private String status;

    @Schema(description = "备注（SET_REMARK 使用）")
    private String remark;

    @Schema(description = "是否仅操作未售罄记录（默认 true，跳过 sold>0 的日期）")
    private Boolean skipSold = true;
}

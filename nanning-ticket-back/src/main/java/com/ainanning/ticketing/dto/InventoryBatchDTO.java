package com.ainanning.ticketing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 库存批量新增参数
 *
 * <p>为指定票种在 [startDate, endDate] 区间内每一天创建一条库存记录。
 * 若该日期已存在有效记录，则跳过（不会覆盖），并把跳过数量返回给前端。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "库存批量新增参数")
public class InventoryBatchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "票种不能为空")
    @Schema(description = "票种 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ticketId;

    @NotNull(message = "起始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "起始日期（含）", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期（含）", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @NotNull(message = "总库存不能为空")
    @Min(value = 1, message = "总库存必须大于 0")
    @Schema(description = "每日总库存（区间内每天一致）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer total;

    @Schema(description = "备注（会写入每条记录）")
    private String remark;
}

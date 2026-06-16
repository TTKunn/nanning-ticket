package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 票据分页查询 DTO
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "票据分页查询参数")
public class VoucherQueryDTO extends PageQuery {

    @Schema(description = "园区 ID", example = "1")
    private Long scenicId;

    @Schema(description = "票种 ID", example = "1")
    private Long ticketId;

    @Schema(description = "销售单 ID", example = "1")
    private Long saleId;

    @Schema(description = "状态：待使用/已使用/已退/已作废")
    private String status;

    @Schema(description = "入场日期（精确）", example = "2026-06-14")
    private LocalDate inventoryDate;

    @Schema(description = "入场日期范围起（含）")
    private LocalDate dateFrom;

    @Schema(description = "入场日期范围止（含）")
    private LocalDate dateTo;

    @Schema(description = "关键字（票据码/购票人/手机）")
    private String keyword;
}

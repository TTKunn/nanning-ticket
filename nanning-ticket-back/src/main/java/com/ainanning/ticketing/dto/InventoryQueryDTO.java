package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 库存分页查询参数
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "库存查询参数")
public class InventoryQueryDTO extends PageQuery {

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "起始日期（含）")
    private LocalDate dateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期（含）")
    private LocalDate dateTo;

    @Schema(description = "状态：开放/关闭/售罄")
    private String status;
}

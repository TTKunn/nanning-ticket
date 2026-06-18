package com.ainanning.ticketing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 库存批量删除参数
 *
 * <p>目标范围指定方式同 {@link InventoryBatchUpdateDTO}。
 * 默认只删 {@code sold=0} 的记录，{@code onlyUnsold=false} 时强制删除已售记录
 * （需配合业务二次确认）。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "库存批量删除参数")
public class InventoryBatchDeleteDTO implements Serializable {

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

    @Schema(description = "是否仅删未售记录（默认 true；为 false 时强制删除）")
    private Boolean onlyUnsold = true;
}

package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 检票记录分页查询 DTO
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "检票记录分页查询参数")
public class VerifyQueryDTO extends PageQuery {

    @Schema(description = "园区 ID", example = "1")
    private Long scenicId;

    @Schema(description = "销售单 ID", example = "1")
    private Long saleId;

    @Schema(description = "检票结果：成功/失败")
    private String result;

    @Schema(description = "检票方式：扫码/手输/刷脸")
    private String verifyMethod;

    @Schema(description = "关键字（票据码 / 购票人 / 检票员）")
    private String keyword;

    @Schema(description = "检票起始日期（含）")
    private LocalDate dateFrom;

    @Schema(description = "检票结束日期（含）")
    private LocalDate dateTo;
}

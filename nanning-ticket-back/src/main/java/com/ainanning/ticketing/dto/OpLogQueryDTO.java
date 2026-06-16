package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 操作日志分页查询
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "操作日志分页查询")
public class OpLogQueryDTO extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关键字（按 bizNo / operatorName / requestUrl 模糊）")
    private String keyword;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作动作")
    private String action;

    @Schema(description = "状态：成功/失败")
    private String status;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "业务 ID")
    private Long bizId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "操作起始日（opTime &gt;= 此值）")
    private LocalDate opDateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "操作截止日（opTime &lt;= 此值）")
    private LocalDate opDateTo;
}

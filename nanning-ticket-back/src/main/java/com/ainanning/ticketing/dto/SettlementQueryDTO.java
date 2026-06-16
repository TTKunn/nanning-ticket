package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 渠道结算单分页查询
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "渠道结算单分页查询")
public class SettlementQueryDTO extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道 ID")
    private Long channelId;

    @Schema(description = "结算单状态：待确认/已确认/已打款/已作废")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结算起始日（过滤 periodStart &gt;= 此值）")
    private LocalDate periodFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结算截止日（过滤 periodEnd &lt;= 此值）")
    private LocalDate periodTo;

    @Schema(description = "关键字（按结算单号 / 渠道名称 模糊）")
    private String keyword;
}

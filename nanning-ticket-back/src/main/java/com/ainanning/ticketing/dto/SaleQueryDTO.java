package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 销售分页查询 DTO
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "销售分页查询参数")
public class SaleQueryDTO extends PageQuery {

    @Schema(description = "园区 ID", example = "1")
    private Long scenicId;

    @Schema(description = "销售状态：已支付/部分退票/已退票/已取消")
    private String status;

    @Schema(description = "支付方式：现金/微信/支付宝/银行卡/余额")
    private String paymentMethod;

    @Schema(description = "关键字（销售流水号 / 购票人姓名 / 电话）")
    private String keyword;

    @Schema(description = "交易起始日期（含）")
    private LocalDate dateFrom;

    @Schema(description = "交易结束日期（含）")
    private LocalDate dateTo;
}

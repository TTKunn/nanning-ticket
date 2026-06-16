package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 订单分页查询 DTO
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单分页查询参数")
public class OrderQueryDTO extends PageQuery {

    @Schema(description = "园区 ID", example = "1")
    private Long scenicId;

    @Schema(description = "渠道编码：OTA/官网/小程序/APP/抖音/微信")
    private String channelCode;

    @Schema(description = "订单状态：待支付/已出票/已取消/退款中/已退款/部分退款")
    private String status;

    @Schema(description = "支付方式：微信/支付宝/银行卡/余额")
    private String payMethod;

    @Schema(description = "联系人手机（精确）")
    private String contactPhone;

    @Schema(description = "关键字（订单号 / 联系人 / 用户名）")
    private String keyword;

    @Schema(description = "下单起始日期（含）")
    private LocalDate orderDateFrom;

    @Schema(description = "下单结束日期（含）")
    private LocalDate orderDateTo;

    @Schema(description = "入场日期范围起（含）")
    private LocalDate useDateFrom;

    @Schema(description = "入场日期范围止（含）")
    private LocalDate useDateTo;
}

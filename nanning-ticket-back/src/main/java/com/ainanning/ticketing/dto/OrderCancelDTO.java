package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单取消 DTO
 *
 * <p>仅允许"待支付"状态的订单取消，"已出票"必须走 {@link OrderRefundDTO}。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "订单取消参数")
public class OrderCancelDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "取消原因", example = "改主意了")
    private String reason;
}

package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单退款 DTO
 *
 * <p>本原型仅支持"全单退款"：必须先校验所有 voucher 均为"待使用"（未被核销），
 * 然后批量改 voucher 状态为"已退"、改 order 状态为"已退款"。</p>
 *
 * <p>如果未来要支持"部分退"（按张退），需扩展本 DTO 加 {@code voucherIds} 列表，
 * 并新增 Service 方法。当前保留 {@link ResultCode#ORDER_PARTIAL_REFUND_NOT_SUPPORTED}。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "订单退款参数（全单退）")
public class OrderRefundDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "退款原因", example = "客户取消")
    private String reason;
}

package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单支付 DTO
 *
 * <p>用于将"待支付"订单推进到"已出票"：
 * <ul>
 *   <li>校验支付方式</li>
 *   <li>模拟支付成功（写 pay_time / pay_transaction_id）</li>
 *   <li>调 {@code VoucherService.issue} 出票</li>
 *   <li>改 status = 已出票</li>
 * </ul>
 *
 * <p>本原型支付是同步的（不需要异步轮询支付平台），生产环境可改为异步 + 回调。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "订单支付参数")
public class OrderPayDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "支付方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "微信")
    @NotBlank(message = "支付方式不能为空")
    private String payMethod;

    @Schema(description = "支付平台流水号（可空 = 测试模式由系统生成）")
    private String payTransactionId;
}

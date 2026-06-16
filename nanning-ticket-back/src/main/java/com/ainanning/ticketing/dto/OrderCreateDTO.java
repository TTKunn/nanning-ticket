package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 订单创建 DTO
 *
 * <p>本原型下单即付：{@link #payMethod} 必填，{@code Service} 在创建后立即走 pay 流程。
 * 真正的"待支付→超时关单"留待后续接入支付网关时再拆分。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "订单创建参数（创建即支付）")
public class OrderCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "园区 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "园区 ID 不能为空")
    private Long scenicId;

    @Schema(description = "渠道编码", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "小程序")
    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;

    @Schema(description = "渠道名称（冗余）", example = "微信小程序")
    @NotBlank(message = "渠道名称不能为空")
    private String channelName;

    @Schema(description = "下单用户 ID（可空 = 匿名下单）")
    private Long userId;

    @Schema(description = "下单用户名（冗余）", example = "小王")
    private String userName;

    @Schema(description = "联系人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @Schema(description = "联系人手机", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "联系人手机不能为空")
    private String contactPhone;

    @Schema(description = "联系人身份证（部分票种/活动需要）")
    private String contactIdCard;

    @Schema(description = "支付方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "微信")
    @NotBlank(message = "支付方式不能为空")
    private String payMethod;

    @Schema(description = "支付平台流水号（可空 = 测试模式由系统生成）")
    private String payTransactionId;

    @Schema(description = "订单明细", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "订单明细不能为空")
    @Valid
    private List<OrderItemCreateDTO> items;

    @Schema(description = "备注")
    private String remark;
}

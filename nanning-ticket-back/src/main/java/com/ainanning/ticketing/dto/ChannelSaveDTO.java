package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 渠道新增 / 修改参数
 *
 * <p>id 为空时表示新增，非空时表示修改。
 * 结算账户 / API 密钥 / 历史计数（orderCount / totalGmv）不在此处暴露，由系统维护。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "渠道新增/修改参数")
public class ChannelSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（修改时必填）")
    private Long id;

    @NotBlank(message = "渠道编码不能为空")
    @Size(max = 32, message = "渠道编码不能超过 32 个字符")
    @Schema(description = "渠道编码（业务主键）", example = "OTA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelCode;

    @NotBlank(message = "渠道名称不能为空")
    @Size(max = 64, message = "渠道名称不能超过 64 个字符")
    @Schema(description = "渠道名称", example = "携程旅行", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelName;

    @NotBlank(message = "渠道类型不能为空")
    @Schema(description = "渠道类型：OTA/官网/小程序/APP/短视频/微信/其他",
            example = "OTA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelType;

    @Schema(description = "渠道图标（emoji 或单字）")
    private String icon;

    @Schema(description = "渠道图标背景色 HEX")
    private String iconBg;

    @DecimalMin(value = "0.00", message = "佣金比例不能小于 0")
    @DecimalMax(value = "100.00", message = "佣金比例不能大于 100")
    @Schema(description = "佣金比例（百分比 0-100）", example = "12.00")
    private BigDecimal commissionRate;

    @Schema(description = "商务联系人")
    private String contactName;

    @Schema(description = "商务联系电话")
    private String contactPhone;

    @Schema(description = "结算账户名")
    private String settleAccount;

    @Schema(description = "结算开户行")
    private String settleBank;

    @Schema(description = "结算账号")
    private String settleAccountNo;

    @Schema(description = "API 密钥")
    private String apiKey;

    @Schema(description = "API 接入地址")
    private String apiEndpoint;

    @Schema(description = "渠道说明")
    private String description;

    @Schema(description = "状态：启用/停用", example = "启用")
    private String status;

    @Schema(description = "排序值（越大越靠前）")
    private Integer sort;
}

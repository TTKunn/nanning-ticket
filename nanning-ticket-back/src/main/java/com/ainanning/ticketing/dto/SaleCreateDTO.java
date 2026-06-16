package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售单创建 DTO
 *
 * <p>一次窗口出票：包含 1..N 个销售明细。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "窗口售票创建参数")
public class SaleCreateDTO {

    @NotNull(message = "园区 ID 不能为空")
    @Schema(description = "园区 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long scenicId;

    @Schema(description = "窗口 ID（占位）")
    private Long windowId;

    @Schema(description = "窗口名称（冗余）", example = "1号窗口")
    private String windowName;

    @Schema(description = "售票员 ID（占位）")
    private Long salespersonId;

    @Schema(description = "售票员姓名（冗余）", example = "李华")
    private String salespersonName;

    @Schema(description = "购票人姓名", example = "王芳")
    @Size(max = 64)
    private String visitorName;

    @Schema(description = "联系电话", example = "13800000001")
    @Size(max = 20)
    private String visitorPhone;

    @Schema(description = "身份证号", example = "450101199001011234")
    @Size(max = 32)
    private String visitorIdCard;

    @NotNull(message = "支付方式不能为空")
    @Schema(description = "支付方式：现金/微信/支付宝/银行卡/余额", example = "微信",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentMethod;

    @Schema(description = "交易时间（不传则取服务器当前时间）")
    private LocalDateTime saleTime;

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "销售明细不能为空")
    @Valid
    @Schema(description = "销售明细（至少 1 条）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SaleItemCreateDTO> items;
}

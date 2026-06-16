package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检票请求 DTO
 *
 * <p>用于闸机/手持终端调用核心检票接口，输出 {@link com.ainanning.ticketing.vo.VerifyResultVO}。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "检票请求参数")
public class VerifyRequestDTO {

    @NotBlank(message = "票据码不能为空")
    @Size(max = 64)
    @Schema(description = "票据码（Voucher 编码）", example = "V202606140001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String voucherCode;

    @Schema(description = "检票方式：扫码/手输/刷脸（不传默认 扫码）", example = "扫码")
    private String verifyMethod;

    @Schema(description = "检票员 ID（占位）")
    private Long verifyStaffId;

    @Schema(description = "检票员姓名（冗余）", example = "张检")
    @Size(max = 64)
    private String verifyStaffName;

    @Schema(description = "闸机/设备 ID（占位）")
    private Long deviceId;

    @Schema(description = "设备名称（冗余）", example = "1号闸机")
    @Size(max = 64)
    private String deviceName;

    @Schema(description = "检票时间（不传则取服务器当前时间）")
    private LocalDateTime verifyTime;
}

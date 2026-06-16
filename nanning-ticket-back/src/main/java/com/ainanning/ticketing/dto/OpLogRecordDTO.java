package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 操作日志记录 DTO（供业务 Service 显式调用）
 *
 * <p>由 {@code OpLogService.record(dto)} 写入 op_log 表。
 * 通常在业务 Service 的"成功" / "失败"出口处调用，避免引入 AOP 复杂度。</p>
 *
 * <p>{@code requestParams} / {@code responseResult} 由 Service 在内部按
 * {@code OP_LOG_PARAMS_MAX_LENGTH} 截断，DTO 传入原始值即可。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "操作日志记录参数")
public class OpLogRecordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "操作模块不能为空")
    @Schema(description = "操作模块（园区/票种/库存/...）", example = "订单",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String module;

    @NotBlank(message = "操作动作不能为空")
    @Schema(description = "操作动作（新增/修改/删除/状态/退款/作废/...）", example = "退款",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Schema(description = "业务实体 ID")
    private Long bizId;

    @Schema(description = "业务流水号", example = "O202606140003")
    private String bizNo;

    @Schema(description = "操作人 ID")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "财务小李")
    private String operatorName;

    @Schema(description = "操作人角色", example = "财务")
    private String operatorRole;

    @Schema(description = "请求 URL", example = "/api/orders/3/refund")
    private String requestUrl;

    @Schema(description = "HTTP 方法", example = "POST")
    private String requestMethod;

    @Schema(description = "请求参数（JSON 字符串，按长度截断）")
    private String requestParams;

    @Schema(description = "响应结果（JSON 字符串，按长度截断）")
    private String responseResult;

    @Schema(description = "客户端 IP", example = "127.0.0.1")
    private String ip;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "状态：成功/失败", example = "成功")
    private String status;

    @Schema(description = "错误信息（status=失败 时填）")
    private String errorMsg;

    @Schema(description = "耗时（毫秒）", example = "32")
    private Long durationMs;

    @Schema(description = "备注")
    private String remark;
}

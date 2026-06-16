package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.OpLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志视图
 *
 * <p>对 Entity 字段做"前端友好"输出（时间格式化等），
 * 字段顺序与 {@code OpLog} 一致，方便检索。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "操作日志视图")
public class OpLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作动作")
    private String action;

    @Schema(description = "业务实体 ID")
    private Long bizId;

    @Schema(description = "业务流水号")
    private String bizNo;

    @Schema(description = "操作人 ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作人角色")
    private String operatorRole;

    @Schema(description = "请求 URL")
    private String requestUrl;

    @Schema(description = "HTTP 方法")
    private String requestMethod;

    @Schema(description = "请求参数（截断存储）")
    private String requestParams;

    @Schema(description = "响应结果（截断存储）")
    private String responseResult;

    @Schema(description = "客户端 IP")
    private String ip;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "状态：成功/失败")
    private String status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "耗时（毫秒）")
    private Long durationMs;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "操作时间")
    private LocalDateTime opTime;

    @Schema(description = "备注")
    private String remark;

    /**
     * Entity → VO
     */
    public static OpLogVO from(OpLog entity) {
        if (entity == null) return null;
        return OpLogVO.builder()
                .id(entity.getId())
                .module(entity.getModule())
                .action(entity.getAction())
                .bizId(entity.getBizId())
                .bizNo(entity.getBizNo())
                .operatorId(entity.getOperatorId())
                .operatorName(entity.getOperatorName())
                .operatorRole(entity.getOperatorRole())
                .requestUrl(entity.getRequestUrl())
                .requestMethod(entity.getRequestMethod())
                .requestParams(entity.getRequestParams())
                .responseResult(entity.getResponseResult())
                .ip(entity.getIp())
                .userAgent(entity.getUserAgent())
                .status(entity.getStatus())
                .errorMsg(entity.getErrorMsg())
                .durationMs(entity.getDurationMs())
                .opTime(entity.getOpTime())
                .remark(entity.getRemark())
                .build();
    }
}

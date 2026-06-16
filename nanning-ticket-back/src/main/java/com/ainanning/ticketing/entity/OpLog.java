package com.ainanning.ticketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 操作日志实体（管理后台审计追踪）
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code module + action} 描述具体操作（如：{@code 订单/退款}、{@code 票据/作废}）</li>
 *   <li>{@code bizId + bizNo} 关联业务实体（可空）</li>
 *   <li>{@code requestParams} / {@code responseResult} 按长度截断存储（避免大对象撑爆）</li>
 *   <li>{@code durationMs} 记录耗时——排查慢操作</li>
 *   <li>{@code status} 成功 / 失败，失败时 {@code errorMsg} 必填</li>
 *   <li>本原型不接 AOP，由业务 Service 显式调用 {@code OpLogService.record()} 写入</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("op_log")
public class OpLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /* ===== 状态常量 ===== */
    public static final String STATUS_SUCCESS = "成功";
    public static final String STATUS_FAILURE = "失败";

    /* ===== 常见模块 ===== */
    public static final String MODULE_SCENIC   = "园区";
    public static final String MODULE_TICKET   = "票种";
    public static final String MODULE_INVENTORY = "库存";
    public static final String MODULE_SALE     = "销售";
    public static final String MODULE_VERIFY   = "检票";
    public static final String MODULE_VOUCHER  = "票据";
    public static final String MODULE_ORDER    = "订单";
    public static final String MODULE_CHANNEL  = "渠道";
    public static final String MODULE_SETTING  = "系统";

    /* ===== 常见动作 ===== */
    public static final String ACTION_CREATE  = "新增";
    public static final String ACTION_UPDATE  = "修改";
    public static final String ACTION_DELETE  = "删除";
    public static final String ACTION_STATUS  = "状态";
    public static final String ACTION_REFUND  = "退款";
    public static final String ACTION_REVOKE  = "作废";
    public static final String ACTION_CONFIRM = "确认";
    public static final String ACTION_PAY     = "打款";

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
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
}

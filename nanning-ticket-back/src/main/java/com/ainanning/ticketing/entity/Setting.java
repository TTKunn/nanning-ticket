package com.ainanning.ticketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统参数实体（key-value 全局配置）
 *
 * <p>设计要点：
 * <ul>
 *   <li>setting_key 业务主键（uk_setting_key），前端用 {@code SYS_ORDER_TIMEOUT_MIN} 这类常量引用</li>
 *   <li>valueType 决定 setting_value 如何被反序列化（STRING/NUMBER/BOOLEAN/JSON）</li>
 *   <li>isReadonly=true 表示系统内置，{@code save()} 时拒绝修改（{@code SETTING_READONLY}）</li>
 *   <li>groupName 用于管理后台按分组渲染（通用/订单/支付/票务/渠道/园区）</li>
 *   <li>业务侧读取推荐用 {@code SettingService.getValue(key, defaultValue)}，避免直接查库</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("setting")
public class Setting extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /* ===== 值类型常量 ===== */
    public static final String TYPE_STRING  = "STRING";
    public static final String TYPE_NUMBER  = "NUMBER";
    public static final String TYPE_BOOLEAN = "BOOLEAN";
    public static final String TYPE_JSON    = "JSON";

    /* ===== 状态常量 ===== */
    public static final String STATUS_ENABLED  = "启用";
    public static final String STATUS_DISABLED = "停用";

    /* ===== 常见分组 ===== */
    public static final String GROUP_GENERIC = "通用";
    public static final String GROUP_ORDER   = "订单";
    public static final String GROUP_PAYMENT = "支付";
    public static final String GROUP_TICKET  = "票务";
    public static final String GROUP_CHANNEL = "渠道";
    public static final String GROUP_SCENIC  = "园区";

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "参数键（业务主键）", example = "ORDER_TIMEOUT_MIN")
    private String settingKey;

    @Schema(description = "参数值（原始字符串）")
    private String settingValue;

    @Schema(description = "值类型：STRING/NUMBER/BOOLEAN/JSON", example = "NUMBER")
    private String valueType;

    @Schema(description = "参数分组", example = "订单")
    private String groupName;

    @Schema(description = "参数说明")
    private String description;

    @Schema(description = "是否只读：1=是（系统内置），0=否")
    private Integer isReadonly;

    @Schema(description = "状态：启用/停用")
    private String status;
}

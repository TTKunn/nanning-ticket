package com.ainanning.ticketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 后台系统用户
 *
 * <p>对应数据库表：sys_user
 * <ul>
 *   <li>{@code passwordHash} 永不暴露到 API（{@link JsonIgnore}）</li>
 *   <li>{@code role} 是主角色；{@code roleCodes} 是所有角色编码（逗号分隔），用于多角色鉴权</li>
 *   <li>{@code scenicIds} 为空表示可访问全部园区，否则只能访问指定园区</li>
 *   <li>{@code lockedUntil} 非空表示当前处于锁定状态，需要在登录时校验</li>
 * </ul>
 * </p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "后台系统用户")
public class SysUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /* ===== 状态常量 ===== */
    public static final String STATUS_ENABLED  = "启用";
    public static final String STATUS_DISABLED = "停用";

    /* ===== 角色编码常量 ===== */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN       = "ADMIN";
    public static final String ROLE_OPERATOR    = "OPERATOR";
    public static final String ROLE_SELLER      = "SELLER";
    public static final String ROLE_VERIFIER    = "VERIFIER";
    public static final String ROLE_FINANCE     = "FINANCE";
    public static final String ROLE_STAFF       = "STAFF";

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "登录账号", example = "admin")
    private String username;

    @Schema(description = "BCrypt 密码哈希（仅持久化使用，不外露）", hidden = true)
    @JsonIgnore
    private String passwordHash;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "主角色编码：SUPER_ADMIN/ADMIN/OPERATOR/SELLER/VERIFIER/FINANCE/STAFF")
    private String role;

    @Schema(description = "角色编码集合（逗号分隔，含主角色）")
    private String roleCodes;

    @Schema(description = "可管辖园区 ID（逗号分隔），null/空 表示全部园区")
    private String scenicIds;

    @Schema(description = "状态：启用/停用")
    private String status;

    @Schema(description = "最近登录时间")
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @Schema(description = "最近登录 IP")
    @TableField("last_login_ip")
    private String lastLoginIp;

    @Schema(description = "连续登录失败次数", hidden = true)
    @JsonIgnore
    @TableField("login_fail_count")
    private Integer loginFailCount;

    @Schema(description = "账号锁定截止时间（NULL 表示未锁定）", hidden = true)
    @JsonIgnore
    @TableField("locked_until")
    private LocalDateTime lockedUntil;

    @Schema(description = "备注")
    private String remark;
}

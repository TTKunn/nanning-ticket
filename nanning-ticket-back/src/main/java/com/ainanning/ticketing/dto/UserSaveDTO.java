package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户新增 / 修改参数
 *
 * <p>id 为空时表示新增，非空时表示修改。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "用户新增/修改参数")
public class UserSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（修改时必填）")
    private Long id;

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 3, max = 32, message = "登录账号长度必须在 3-32 之间")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "登录账号必须以字母开头，仅含字母数字下划线")
    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /** 新增时必填；修改时为空表示不修改密码 */
    @Size(min = 6, max = 64, message = "密码长度必须在 6-64 之间")
    @Schema(description = "登录密码（新增必填，修改时为空表示不修改）")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 32, message = "姓名不能超过 32 个字符")
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "主角色编码：SUPER_ADMIN/ADMIN/OPERATOR/SELLER/VERIFIER/FINANCE/STAFF",
            example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "额外角色编码（逗号分隔，可选，与主角色合并后写入 roleCodes）")
    private String extraRoles;

    @Schema(description = "可管辖园区 ID（逗号分隔），空表示全部园区")
    private String scenicIds;

    @Schema(description = "状态：启用/停用")
    private String status;

    @Schema(description = "备注")
    private String remark;
}

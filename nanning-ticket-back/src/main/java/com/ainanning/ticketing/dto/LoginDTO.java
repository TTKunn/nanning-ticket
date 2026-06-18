package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求参数
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "登录请求参数")
public class LoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 3, max = 32, message = "登录账号长度必须在 3-32 之间")
    @Schema(description = "登录账号", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "登录密码不能为空")
    @Size(min = 6, max = 64, message = "登录密码长度必须在 6-64 之间")
    @Schema(description = "登录密码（明文传输，HTTPS 保护）", example = "admin123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "图形验证码（演示用，可选）")
    private String captcha;

    @Schema(description = "图形验证码 key（演示用，可选）")
    private String captchaKey;
}

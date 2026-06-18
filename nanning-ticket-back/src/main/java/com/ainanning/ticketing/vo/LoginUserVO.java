package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录成功响应
 *
 * <p>包含访问令牌、当前登录用户信息以及可管辖的园区 ID 列表（前端用于按园区过滤）。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录成功响应")
public class LoginUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌（JWT）")
    private String token;

    @Schema(description = "令牌过期时间（毫秒时间戳）")
    private Long tokenExpireAt;

    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(description = "可管辖园区 ID 列表（仅在 scenicIds 字段非空时返回）")
    private List<Long> scenicIdList;

    @Schema(description = "可管辖角色编码列表（从 roleCodes 拆分）")
    private List<String> roleList;
}

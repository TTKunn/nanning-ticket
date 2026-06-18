package com.ainanning.ticketing.common.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 已登录用户上下文
 *
 * <p>由 {@code AuthInterceptor} 在请求进入时解析 JWT 并写入 {@link SecurityContextHolder}；
 * 业务代码可通过 {@link SecurityContextHolder#get()} 在当前线程内取出当前用户。</p>
 *
 * <p>该对象刻意与 {@code SysUser} 实体解耦，避免无意中泄露密码哈希等敏感字段。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "已登录用户上下文")
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "主角色")
    private String role;

    @Schema(description = "角色编码集合（逗号分隔）")
    private String roleCodes;

    @Schema(description = "可管辖园区 ID（逗号分隔），空 = 全部")
    private String scenicIds;

    /** 角色编码数组（懒加载） */
    public List<String> getRoleList() {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(roleCodes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /** 是否拥有任一指定角色 */
    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }
        List<String> mine = getRoleList();
        for (String r : roles) {
            if (mine.contains(r)) {
                return true;
            }
        }
        return false;
    }

    /** 是否超级管理员 */
    public boolean isSuperAdmin() {
        return getRoleList().contains("SUPER_ADMIN");
    }
}

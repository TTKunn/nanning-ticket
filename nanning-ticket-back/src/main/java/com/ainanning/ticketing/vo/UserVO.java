package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户视图对象（用于 API 响应）
 *
 * <p>相比实体，剥离了敏感字段（passwordHash / loginFailCount / lockedUntil）。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "用户视图")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "主角色编码")
    private String role;

    @Schema(description = "角色编码集合（逗号分隔）")
    private String roleCodes;

    @Schema(description = "角色编码集合（数组）")
    private List<String> roleList;

    @Schema(description = "可管辖园区 ID（逗号分隔）")
    private String scenicIds;

    @Schema(description = "可管辖园区 ID（数组）")
    private List<Long> scenicIdList;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "最近登录 IP")
    private String lastLoginIp;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** Entity → VO 转换 */
    public static UserVO from(SysUser entity) {
        if (entity == null) {
            return null;
        }
        String roleCodes = entity.getRoleCodes() == null
                ? entity.getRole()
                : entity.getRoleCodes();
        return UserVO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .realName(entity.getRealName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .avatar(entity.getAvatar())
                .role(entity.getRole())
                .roleCodes(roleCodes)
                .roleList(splitToList(roleCodes))
                .scenicIds(entity.getScenicIds())
                .scenicIdList(splitToLongList(entity.getScenicIds()))
                .status(entity.getStatus())
                .lastLoginAt(entity.getLastLoginAt())
                .lastLoginIp(entity.getLastLoginIp())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private static List<String> splitToList(String csv) {
        if (csv == null || csv.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static List<Long> splitToLongList(String csv) {
        if (csv == null || csv.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}

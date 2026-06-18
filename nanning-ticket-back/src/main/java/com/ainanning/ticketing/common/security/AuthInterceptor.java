package com.ainanning.ticketing.common.security;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.entity.SysUser;
import com.ainanning.ticketing.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * 认证拦截器
 *
 * <p>职责：
 * <ol>
 *   <li>从请求头 {@code Authorization: Bearer xxx} 解析 JWT</li>
 *   <li>校验令牌有效性，提取 userId / username / role</li>
 *   <li>从数据库加载最新的用户信息（用于实时反映"停用 / 角色变更"）</li>
 *   <li>将 {@link LoginUser} 写入 {@link SecurityContextHolder}</li>
 *   <li>支持 {@link PublicEndpoint} / {@link RequireRoles} 注解：
 *       <ul>
 *         <li>{@link PublicEndpoint}：跳过鉴权（用于登录、Swagger 等）</li>
 *         <li>{@link RequireRoles}：必须拥有任一指定角色，超级管理员始终放行</li>
 *       </ul>
 *   </li>
 *   <li>请求结束时清理 ThreadLocal</li>
 * </ol>
 * </p>
 *
 * @author nanning-ticket
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // 只拦截 Controller 方法
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        Method method = hm.getMethod();
        Class<?> controller = hm.getBeanType();

        // 1. 公开接口直接放行
        if (method.isAnnotationPresent(PublicEndpoint.class)
                || controller.isAnnotationPresent(PublicEndpoint.class)) {
            return true;
        }

        // 2. 解析 JWT
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ResultCode.LOGIN_REQUIRED);
        }
        Claims claims = jwtUtil.parse(token);

        Long userId = claims.get(JwtUtil.CLAIM_USER_ID, Long.class);
        if (userId == null) {
            throw new BusinessException(ResultCode.LOGIN_TOKEN_INVALID);
        }

        // 3. 加载用户最新状态（实时反映停用、角色变更、删除等）
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
                        .isNull(SysUser::getDeletedAt));
        if (user == null) {
            log.warn("[Auth] 用户不存在或已删除 userId={}", userId);
            throw new BusinessException(ResultCode.LOGIN_TOKEN_INVALID);
        }
        if (SysUser.STATUS_DISABLED.equals(user.getStatus())) {
            log.warn("[Auth] 用户已停用 userId={}", userId);
            throw new BusinessException(ResultCode.LOGIN_DISABLED);
        }

        // 4. 写入上下文
        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .roleCodes(user.getRoleCodes() != null ? user.getRoleCodes() : user.getRole())
                .scenicIds(user.getScenicIds())
                .build();
        SecurityContextHolder.set(loginUser);

        // 5. 角色鉴权
        RequireRoles requireRoles = method.getAnnotation(RequireRoles.class);
        if (requireRoles == null) {
            requireRoles = controller.getAnnotation(RequireRoles.class);
        }
        if (requireRoles != null) {
            if (!loginUser.isSuperAdmin() && !loginUser.hasAnyRole(requireRoles.value())) {
                log.warn("[Auth] 角色不足 userId={}, need={}, has={}",
                        userId, String.join(",", requireRoles.value()),
                        loginUser.getRoleCodes());
                throw new BusinessException(ResultCode.USER_NOT_ALLOWED);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        SecurityContextHolder.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        String prefix = "Bearer ";
        if (header.startsWith(prefix)) {
            return header.substring(prefix.length()).trim();
        }
        return header.trim();
    }
}

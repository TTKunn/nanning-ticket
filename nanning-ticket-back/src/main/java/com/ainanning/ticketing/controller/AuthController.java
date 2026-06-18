package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.security.PublicEndpoint;
import com.ainanning.ticketing.dto.ChangePasswordDTO;
import com.ainanning.ticketing.dto.LoginDTO;
import com.ainanning.ticketing.service.AuthService;
import com.ainanning.ticketing.vo.LoginUserVO;
import com.ainanning.ticketing.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller
 *
 * <p>路由前缀：/api/auth
 * <ul>
 *   <li>{@code POST /api/auth/login}      登录（公开）</li>
 *   <li>{@code POST /api/auth/logout}     登出（需登录）</li>
 *   <li>{@code GET  /api/auth/me}         当前登录用户（需登录）</li>
 *   <li>{@code PUT  /api/auth/password}   修改自己的密码（需登录）</li>
 * </ul>
 * </p>
 *
 * @author nanning-ticket
 */
@Tag(name = "00. 登录 / 认证", description = "账号登录、当前用户信息、修改密码")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PublicEndpoint
    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginUserVO> login(@Valid @RequestBody LoginDTO dto,
                                     HttpServletRequest request) {
        String ip = resolveClientIp(request);
        return Result.success("登录成功", authService.login(dto, ip));
    }

    @Operation(summary = "登出（前端清除 token 即可，服务端无状态）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("已退出登录", null);
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(authService.me());
    }

    @Operation(summary = "修改自己的密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success("密码修改成功", null);
    }

    /** 解析客户端 IP（兼容反向代理 X-Forwarded-For） */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isEmpty()) {
            return real;
        }
        return request.getRemoteAddr();
    }
}

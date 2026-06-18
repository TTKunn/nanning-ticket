package com.ainanning.ticketing.common.security;

/**
 * 基于 ThreadLocal 的轻量级安全上下文
 *
 * <p>项目自研实现，替代 Spring Security 容器以保持代码轻量。
 * 由 {@code AuthInterceptor} 在请求开始时写入，在请求结束时清空（{@code finally}）。</p>
 *
 * @author nanning-ticket
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    private SecurityContextHolder() {}

    /** 写入当前请求的登录用户 */
    public static void set(LoginUser user) {
        CONTEXT.set(user);
    }

    /** 取出当前请求的登录用户（可能为 null，表示未登录） */
    public static LoginUser get() {
        return CONTEXT.get();
    }

    /** 取出当前用户，若未登录抛出业务异常 */
    public static LoginUser required() {
        LoginUser u = CONTEXT.get();
        if (u == null) {
            throw new com.ainanning.ticketing.common.exception.BusinessException(
                    com.ainanning.ticketing.common.result.ResultCode.LOGIN_REQUIRED);
        }
        return u;
    }

    /** 清理 ThreadLocal（必须由拦截器或过滤器在 finally 中调用） */
    public static void clear() {
        CONTEXT.remove();
    }
}

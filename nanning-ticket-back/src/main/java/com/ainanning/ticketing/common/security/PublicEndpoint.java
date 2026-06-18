package com.ainanning.ticketing.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 公开接口标记
 *
 * <p>标注在 Controller 类或方法上，{@code AuthInterceptor} 将跳过 JWT 校验直接放行。
 * 适用于登录、刷新令牌、Swagger、静态资源、健康检查等。</p>
 *
 * @author nanning-ticket
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicEndpoint {
}

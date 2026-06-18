package com.ainanning.ticketing.config;

import com.ainanning.ticketing.common.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置
 *
 * <ul>
 *   <li>注册 {@code AuthInterceptor} 到 {@code /api/**}，排除登录与 Swagger</li>
 *   <li>补充 CORS 映射（与 {@code CorsConfig} 二选一即可；本配置同时开放自定义头，便于前端 axios 携带 Authorization）</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                // 排除：登录、Swagger、API 文档、错误页
                .excludePathPatterns(List.of(
                        "/api/auth/login",
                        "/api/auth/captcha",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/error"
                ));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 与 CorsConfig 配合；这里允许任意来源/方法/头，满足 axios + Bearer Token
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

package com.ainanning.ticketing.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色鉴权注解
 *
 * <p>标注在 Controller 方法上，配合 {@code RoleInterceptor} 校验当前登录用户是否拥有任一指定角色。
 * 默认不强制超级管理员放行（超级管理员由 {@link LoginUser#isSuperAdmin()} 自动放行）。</p>
 *
 * <pre>
 *   {@code
 *   @RequireRoles({"SUPER_ADMIN", "ADMIN"})
 *   public Result<Void> deleteUser(@PathVariable Long id) { ... }
 *   }
 * </pre>
 *
 * @author nanning-ticket
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRoles {

    /** 允许访问的角色编码（任一匹配即可） */
    String[] value() default {};

    /** 鉴权失败时的业务消息 */
    String message() default "当前账号无权访问该资源";
}

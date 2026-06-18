package com.ainanning.ticketing.common.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * BCrypt 密码工具
 *
 * <p>封装 {@code at.favre.lib:bcrypt}，对外暴露 encode / matches 静态方法风格接口。</p>
 *
 * @author nanning-ticket
 */
@Component
public class PasswordEncoder {

    /** 哈希强度（4-31，10 是安全与性能的折中） */
    private static final int COST = 10;

    /**
     * 加密明文密码
     */
    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }

    /**
     * 校验明文密码与哈希值是否匹配
     */
    public boolean matches(String rawPassword, String hashed) {
        if (rawPassword == null || hashed == null || hashed.isEmpty()) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), hashed);
        return result.verified;
    }
}

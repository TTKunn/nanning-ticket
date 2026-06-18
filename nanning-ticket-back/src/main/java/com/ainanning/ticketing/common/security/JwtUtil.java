package com.ainanning.ticketing.common.security;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * <p>负责生成、解析、校验 JWT 访问令牌。
 * 密钥来自配置 {@code auth.jwt.secret}（HS256，至少 32 字节）；
 * 默认有效期来自 {@code auth.jwt.expireHours}（默认 8 小时）。
 * 演示用密钥硬编码到 application.yml，生产环境务必从环境变量/密钥管理服务注入。</p>
 *
 * @author nanning-ticket
 */
@Slf4j
@Component
public class JwtUtil {

    /** 存放在 JWT claims 中的字段：用户 ID / 用户名 / 角色编码 */
    public static final String CLAIM_USER_ID   = "uid";
    public static final String CLAIM_USERNAME  = "uname";
    public static final String CLAIM_ROLE      = "role";
    public static final String CLAIM_ROLE_CODES = "roles";

    @Value("${auth.jwt.secret:nanning-ticket-demo-secret-key-please-change-me-32bytes-min}")
    private String secret;

    @Value("${auth.jwt.expireHours:8}")
    private long expireHours;

    @Value("${auth.jwt.issuer:nanning-ticket}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // HS256 要求至少 32 字节（256 位）的密钥
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
            log.warn("[JWT] 密钥长度不足 32 字节，已自动补零；生产环境请使用更长密钥");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        log.info("[JWT] 初始化完成，过期时间 {} 小时，签发方 {}", expireHours, issuer);
    }

    /**
     * 生成访问令牌
     *
     * @param userId     用户 ID
     * @param username   登录账号
     * @param role       主角色
     * @param roleCodes  全部角色编码（逗号分隔）
     * @return [token, expireAt(ms)]
     */
    public TokenInfo generate(Long userId, String username, String role, String roleCodes) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireHours * 3600);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_ROLE_CODES, roleCodes);

        String token = Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(claims)
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        return new TokenInfo(token, exp.toEpochMilli());
    }

    /**
     * 解析并校验 JWT；任何异常都映射为业务异常（由 GlobalExceptionHandler 统一处理）
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BusinessException(ResultCode.LOGIN_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("[JWT] 解析失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.LOGIN_TOKEN_INVALID);
        }
    }

    /**
     * 计算过期时间（LocalDateTime，Asia/Shanghai）
     */
    public LocalDateTime expireAtLocal() {
        return LocalDateTime.ofInstant(
                Instant.now().plusSeconds(expireHours * 3600),
                ZoneId.of("Asia/Shanghai"));
    }

    /** 令牌 + 过期时间（毫秒）的不可变包装 */
    public record TokenInfo(String token, long expireAt) {}
}

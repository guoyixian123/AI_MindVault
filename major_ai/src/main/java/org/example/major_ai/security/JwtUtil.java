package org.example.major_ai.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 负责JWT Token的生成、解析和验证
 *
 * JWT结构：Header.Payload.Signature
 * - Header: {"alg": "HS256", "typ": "JWT"}
 * - Payload: {"sub": "username", "userId": 1, "role": "USER", "iat": ..., "exp": ...}
 * - Signature: HMAC-SHA256签名
 *
 * 配置：
 * - jwt.secret: 签名密钥（在application.yml中配置）
 * - jwt.expiration: 过期时间（毫秒，默认24小时）
 */
@Component
public class JwtUtil {

    /** JWT签名密钥（从配置文件读取） */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT过期时间（毫秒，从配置文件读取） */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 获取签名密钥
     * 将字符串密钥转换为HMAC-SHA256密钥
     *
     * @return SecretKey 签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色（ROOT_ADMIN/DOCTOR/USER）
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)                                          // 主题（用户名）
                .claim("userId", userId)                                    // 自定义声明：用户ID
                .claim("role", role)                                        // 自定义声明：用户角色
                .issuedAt(new Date())                                       // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 过期时间
                .signWith(getSigningKey())                                  // 签名
                .compact();
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token字符串
     * @return Claims 负载（包含所有声明）
     * @throws JwtException Token无效或过期
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // 验证签名
                .build()
                .parseSignedClaims(token)     // 解析Token
                .getPayload();                // 获取负载
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从Token中获取用户角色
     *
     * @param token JWT Token字符串
     * @return 用户角色（ROOT_ADMIN/DOCTOR/USER）
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token字符串
     * @return true=有效，false=无效或过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

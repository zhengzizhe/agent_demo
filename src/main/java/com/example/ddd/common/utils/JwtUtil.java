package com.example.ddd.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 提供 JWT Token 的生成、解析、验证等功能
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:your-secret-key-should-be-at-least-256-bits-long-for-hs256-algorithm}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认 24 小时
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 默认 7 天
    private Long refreshExpiration;

    /**
     * 从 Authorization header 中提取 Token
     * 格式：Bearer {token}
     *
     * @param authHeader Authorization header 值
     * @return JWT Token，如果格式不正确返回 null
     */
    public String extractTokenFromHeader(String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }
        
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return authHeader;
    }

    /**
     * 生成 JWT Token（使用默认过期时间）
     *
     * @param userUuid 用户UUID（对外标识）
     * @param email    用户邮箱
     * @return JWT Token
     */
    public String generateToken(String userUuid, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userUuid", userUuid);
        claims.put("email", email);
        return generateToken(claims, expiration);
    }

    /**
     * 生成 JWT Token（自定义 claims）
     *
     * @param userUuid 用户UUID
     * @param email    用户邮箱
     * @param displayName 用户显示名称
     * @return JWT Token
     */
    public String generateToken(String userUuid, String email, String displayName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userUuid", userUuid);
        claims.put("email", email);
        if (StringUtils.hasText(displayName)) {
            claims.put("displayName", displayName);
        }
        return generateToken(claims, expiration);
    }

    /**
     * 生成 JWT Token（完全自定义）
     *
     * @param claims 自定义声明信息
     * @return JWT Token
     */
    public String generateToken(Map<String, Object> claims) {
        return generateToken(claims, expiration);
    }

    /**
     * 生成 JWT Token（指定过期时间）
     *
     * @param claims    声明信息
     * @param expirationMillis 过期时间（毫秒）
     * @return JWT Token
     */
    public String generateToken(Map<String, Object> claims, Long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新 Token
     * 刷新 Token 通常有更长的过期时间
     *
     * @param userUuid 用户UUID
     * @return 刷新 Token
     */
    public String generateRefreshToken(String userUuid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userUuid", userUuid);
        claims.put("type", "refresh");
        return generateToken(claims, refreshExpiration);
    }

    /**
     * 从 Token 中获取用户UUID
     *
     * @param token JWT Token
     * @return 用户UUID
     */
    public String getUserUuidFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("userUuid", String.class);
        } catch (Exception e) {
            log.error("从 Token 中获取用户UUID失败", e);
            return null;
        }
    }

    /**
     * 从 Token 中获取邮箱
     *
     * @param token JWT Token
     * @return 用户邮箱
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("从 Token 中获取邮箱失败", e);
            return null;
        }
    }

    /**
     * 从 Token 中获取显示名称
     *
     * @param token JWT Token
     * @return 用户显示名称
     */
    public String getDisplayNameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("displayName", String.class);
        } catch (Exception e) {
            log.error("从 Token 中获取显示名称失败", e);
            return null;
        }
    }

    /**
     * 从 Token 中获取指定 claim
     *
     * @param token JWT Token
     * @param claimName claim 名称
     * @param clazz 返回类型
     * @return claim 值
     */
    public <T> T getClaimFromToken(String token, String claimName, Class<T> clazz) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get(claimName, clazz);
        } catch (Exception e) {
            log.error("从 Token 中获取 claim 失败: {}", claimName, e);
            return null;
        }
    }

    /**
     * 从 Token 中获取所有声明
     *
     * @param token JWT Token
     * @return Claims
     * @throws JwtException 如果 Token 无效
     */
    public Claims getClaimsFromToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token 不能为空");
        }
        
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("解析 Token 失败", e);
            throw e;
        }
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Token 已过期");
            return false;
        } catch (Exception e) {
            log.debug("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证 Token 是否即将过期
     * 用于判断是否需要刷新 Token
     *
     * @param token JWT Token
     * @param thresholdMinutes 阈值（分钟），默认 30 分钟
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, int thresholdMinutes) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remainingMillis = expiration.getTime() - now.getTime();
            long thresholdMillis = thresholdMinutes * 60 * 1000L;
            return remainingMillis > 0 && remainingMillis < thresholdMillis;
        } catch (Exception e) {
            log.error("检查 Token 过期时间失败", e);
            return false;
        }
    }

    /**
     * 验证 Token 是否即将过期（默认 30 分钟）
     *
     * @param token JWT Token
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        return isTokenExpiringSoon(token, 30);
    }

    /**
     * 获取 Token 的过期时间
     *
     * @param token JWT Token
     * @return 过期时间（毫秒时间戳），如果解析失败返回 null
     */
    public Long getExpirationFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().getTime();
        } catch (Exception e) {
            log.error("获取 Token 过期时间失败", e);
            return null;
        }
    }

    /**
     * 获取 Token 的剩余有效时间
     *
     * @param token JWT Token
     * @return 剩余时间（毫秒），如果已过期或解析失败返回 0
     */
    public long getRemainingTimeFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remaining = expiration.getTime() - now.getTime();
            return Math.max(0, remaining);
        } catch (Exception e) {
            log.error("获取 Token 剩余时间失败", e);
            return 0;
        }
    }

    /**
     * 刷新 Token（基于现有 Token 生成新 Token）
     *
     * @param token 现有 Token
     * @return 新的 Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            // 移除过期相关的 claim，重新生成
            Map<String, Object> newClaims = new HashMap<>(claims);
            newClaims.remove("iat");
            newClaims.remove("exp");
            return generateToken(newClaims, expiration);
        } catch (ExpiredJwtException e) {
            // 即使 Token 已过期，也允许刷新（使用过期 Token 中的 claims）
            Map<String, Object> newClaims = new HashMap<>(e.getClaims());
            newClaims.remove("iat");
            newClaims.remove("exp");
            return generateToken(newClaims, expiration);
        } catch (Exception e) {
            log.error("刷新 Token 失败", e);
            throw new RuntimeException("刷新 Token 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


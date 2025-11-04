package com.kite.authenticator.util;

import com.kite.authenticator.context.LoginUser;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.common.util.JsonUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类
 * 使用 HMAC-SHA256 算法
 * 
 * @author yourname
 */
public class JwtUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    /**
     * 生成 Token
     * 
     * @param loginUser 登录用户信息
     * @param secret JWT 密钥
     * @param expireTime 过期时间（毫秒）
     * @return Token
     */
    public static String generateToken(LoginUser loginUser, String secret, long expireTime) {
        if (loginUser == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "登录用户信息不能为空");
        }
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireTime);
        
        // 将用户信息转为 JSON 存入 claims
        String userJson = JsonUtils.toJsonString(loginUser);
        
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(String.valueOf(loginUser.getUserId()))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .claim("user", userJson)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析 Token
     * 
     * @param token Token
     * @param secret JWT 密钥
     * @return 登录用户信息
     */
    public static LoginUser parseToken(String token, String secret) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 不能为空");
        }
        
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // 从 claims 中获取用户信息
            String userJson = claims.get("user", String.class);
            if (userJson == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 格式错误");
            }
            
            LoginUser loginUser = JsonUtils.parseObject(userJson, LoginUser.class);
            if (loginUser == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 解析失败");
            }
            
            return loginUser;
            
        } catch (ExpiredJwtException e) {
            logger.warn("Token 已过期: {}", token);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 已过期");
        } catch (JwtException e) {
            logger.warn("Token 解析失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 无效");
        } catch (Exception e) {
            logger.error("Token 解析异常", e);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 解析异常");
        }
    }
    
    /**
     * 验证 Token 是否有效
     * 
     * @param token Token
     * @param secret JWT 密钥
     * @return 是否有效
     */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取 Token 过期时间
     * 
     * @param token Token
     * @param secret JWT 密钥
     * @return 过期时间
     */
    public static Date getExpirationDate(String token, String secret) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("获取 Token 过期时间失败", e);
            return null;
        }
    }
}


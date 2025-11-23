package com.kite.authenticator.service;

import com.kite.authenticator.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 * 用于撤销已泄露或需要立即失效的 Token
 * 
 * @author yourname
 */
public class TokenBlacklistService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    private static final String BLACKLIST_KEY_PREFIX = "authc:blacklist:";
    private static final String BLACKLIST_REASON_PREFIX = "authc:blacklist:reason:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final String jwtSecret;
    
    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate, String jwtSecret) {
        this.redisTemplate = redisTemplate;
        this.jwtSecret = jwtSecret;
    }
    
    /**
     * 将 Token 加入黑名单（安全事件）
     * 
     * @param token Token 字符串
     * @param reason 加入黑名单的原因（如：Token泄露、异常行为等），用于安全审计
     * @param expireTimeMillis 过期时间（毫秒），如果为 null，则使用 Token 本身的过期时间
     */
    public void blacklistToken(String token, String reason, Long expireTimeMillis) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        try {
            // 使用 Token 的哈希值作为 key（避免存储完整 Token）
            String tokenHash = generateTokenHash(token);
            String blacklistKey = BLACKLIST_KEY_PREFIX + tokenHash;
            
            // 如果没有指定过期时间，尝试从 Token 中获取
            if (expireTimeMillis == null) {
                Date expirationDate = JwtUtils.getExpirationDate(token, jwtSecret);
                if (expirationDate != null) {
                    long remainingTime = expirationDate.getTime() - System.currentTimeMillis();
                    if (remainingTime > 0) {
                        expireTimeMillis = remainingTime;
                    } else {
                        // Token 已过期，不需要加入黑名单
                        logger.debug("Token 已过期，无需加入黑名单");
                        return;
                    }
                } else {
                    // 无法获取过期时间，使用默认值（24小时）
                    expireTimeMillis = 24 * 60 * 60 * 1000L;
                }
            }
            
            // 存储到 Redis，设置过期时间
            redisTemplate.opsForValue().set(blacklistKey, "1", expireTimeMillis, TimeUnit.MILLISECONDS);
            
            // 如果有原因，存储原因（用于安全审计）
            if (reason != null && !reason.isEmpty()) {
                String reasonKey = BLACKLIST_REASON_PREFIX + tokenHash;
                redisTemplate.opsForValue().set(reasonKey, reason, expireTimeMillis, TimeUnit.MILLISECONDS);
            }
            
            logger.warn("Token 已加入黑名单，原因: {}, 过期时间: {} 毫秒", reason != null ? reason : "未指定", expireTimeMillis);
            
        } catch (Exception e) {
            logger.error("将 Token 加入黑名单失败", e);
        }
    }
    
    /**
     * 将 Token 加入黑名单（使用 Token 本身的过期时间）
     * 
     * @param token Token 字符串
     */
    public void blacklistToken(String token) {
        blacklistToken(token, null, null);
    }
    
    /**
     * 将 Token 加入黑名单（安全事件，带原因）
     * 
     * @param token Token 字符串
     * @param reason 加入黑名单的原因
     */
    public void blacklistToken(String token, String reason) {
        blacklistToken(token, reason, null);
    }
    
    /**
     * 获取 Token 加入黑名单的原因
     * 
     * @param token Token 字符串
     * @return 原因，如果不存在则返回 null
     */
    public String getBlacklistReason(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            String tokenHash = generateTokenHash(token);
            String reasonKey = BLACKLIST_REASON_PREFIX + tokenHash;
            Object reason = redisTemplate.opsForValue().get(reasonKey);
            return reason != null ? reason.toString() : null;
        } catch (Exception e) {
            logger.error("获取 Token 黑名单原因失败", e);
            return null;
        }
    }
    
    /**
     * 检查 Token 是否在黑名单中
     * 
     * @param token Token 字符串
     * @return true 如果在黑名单中，false 否则
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        try {
            String tokenHash = generateTokenHash(token);
            String blacklistKey = BLACKLIST_KEY_PREFIX + tokenHash;
            
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            return Boolean.TRUE.equals(exists);
            
        } catch (Exception e) {
            logger.error("检查 Token 黑名单失败", e);
            // 发生异常时，为了安全起见，返回 true（拒绝访问）
            return true;
        }
    }
    
    /**
     * 从黑名单中移除 Token（提前解除黑名单）
     * 
     * @param token Token 字符串
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        try {
            String tokenHash = generateTokenHash(token);
            String blacklistKey = BLACKLIST_KEY_PREFIX + tokenHash;
            String reasonKey = BLACKLIST_REASON_PREFIX + tokenHash;
            
            // 删除黑名单记录和原因记录
            redisTemplate.delete(blacklistKey);
            redisTemplate.delete(reasonKey);
            
            logger.info("Token 已从黑名单中移除");
            
        } catch (Exception e) {
            logger.error("从黑名单中移除 Token 失败", e);
        }
    }
    
    /**
     * 生成 Token 的哈希值
     * 使用 SHA-256 哈希，避免在 Redis 中存储完整 Token
     */
    private String generateTokenHash(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (Exception e) {
            logger.error("生成 Token 哈希失败", e);
            // 如果哈希失败，使用 Token 本身（不推荐，但作为后备方案）
            return token.substring(0, Math.min(token.length(), 64));
        }
    }
    
    /**
     * 批量撤销用户的所有 Token（通过 Session 实现）
     * 这个方法主要用于强制用户下线，实际撤销通过 Session 状态管理
     * 
     * @param userId 用户ID
     */
    public void blacklistUserTokens(Long userId) {
        // 注意：这个方法主要用于记录日志
        // 实际的 Token 撤销通过 Session 状态管理实现
        logger.info("用户 {} 的所有 Token 将被撤销（通过 Session 状态管理）", userId);
    }
}


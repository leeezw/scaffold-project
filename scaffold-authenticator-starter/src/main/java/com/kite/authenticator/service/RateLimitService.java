package com.kite.authenticator.service;

import com.kite.authenticator.enums.RateLimitAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 限流服务
 * 基于 Redis 实现的分布式限流服务
 * 
 * @author yourname
 */
public class RateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String SLIDING_WINDOW_PREFIX = RATE_LIMIT_PREFIX + "sliding:";
    private static final String FIXED_WINDOW_PREFIX = RATE_LIMIT_PREFIX + "fixed:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RateLimitService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 检查是否允许请求
     * 
     * @param key 限流Key（如：IP、用户ID、Token等）
     * @param window 时间窗口（秒）
     * @param maxRequests 最大请求数
     * @param algorithm 限流算法
     * @return true 允许，false 拒绝
     */
    public boolean allowRequest(String key, int window, int maxRequests, RateLimitAlgorithm algorithm) {
        if (key == null || key.isEmpty()) {
            return true; // key为空，不限制
        }
        
        try {
            if (algorithm == RateLimitAlgorithm.SLIDING_WINDOW) {
                return slidingWindowAllow(key, window, maxRequests);
            } else {
                return fixedWindowAllow(key, window, maxRequests);
            }
        } catch (Exception e) {
            logger.error("限流检查失败，key: {}", key, e);
            // 发生异常时，为了不影响业务，允许请求
            return true;
        }
    }
    
    /**
     * 滑动窗口算法
     * 使用 Redis Sorted Set 实现
     */
    private boolean slidingWindowAllow(String key, int window, int maxRequests) {
        String redisKey = SLIDING_WINDOW_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (window * 1000L);
        
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        
        // 1. 删除窗口外的记录（score < windowStart）
        zSetOps.removeRangeByScore(redisKey, 0, windowStart);
        
        // 2. 统计窗口内的记录数
        Long count = zSetOps.count(redisKey, windowStart, currentTime);
        if (count == null) {
            count = 0L;
        }
        
        // 3. 如果未超过限制，添加新记录
        if (count < maxRequests) {
            // 添加当前请求记录（score为当前时间戳，member为UUID）
            zSetOps.add(redisKey, UUID.randomUUID().toString(), currentTime);
            // 设置过期时间（窗口时间 + 1秒，确保数据清理）
            redisTemplate.expire(redisKey, window + 1, TimeUnit.SECONDS);
            return true;
        }
        
        return false;
    }
    
    /**
     * 固定窗口算法
     * 使用 Redis String + TTL 实现
     */
    private boolean fixedWindowAllow(String key, int window, int maxRequests) {
        // 计算当前窗口开始时间（秒级时间戳）
        long currentTime = System.currentTimeMillis() / 1000;
        long windowStart = (currentTime / window) * window;
        
        String redisKey = FIXED_WINDOW_PREFIX + key + ":" + windowStart;
        
        // 1. 获取或创建计数器
        Long count = redisTemplate.opsForValue().increment(redisKey);
        
        // 2. 如果是第一次请求（count == 1），设置过期时间
        if (count == 1) {
            redisTemplate.expire(redisKey, window, TimeUnit.SECONDS);
        }
        
        // 3. 检查是否超过限制
        return count <= maxRequests;
    }
    
    /**
     * 获取剩余请求数
     * 
     * @param key 限流Key
     * @param window 时间窗口（秒）
     * @param maxRequests 最大请求数
     * @return 剩余请求数
     */
    public long getRemainingRequests(String key, int window, int maxRequests) {
        if (key == null || key.isEmpty()) {
            return maxRequests;
        }
        
        try {
            long currentCount = getCurrentCount(key, window);
            long remaining = maxRequests - currentCount;
            return Math.max(0, remaining);
        } catch (Exception e) {
            logger.error("获取剩余请求数失败，key: {}", key, e);
            return maxRequests;
        }
    }
    
    /**
     * 获取当前窗口内的请求数
     */
    private long getCurrentCount(String key, int window) {
        // 使用滑动窗口算法统计（更准确）
        String redisKey = SLIDING_WINDOW_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (window * 1000L);
        
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Long count = zSetOps.count(redisKey, windowStart, currentTime);
        return count != null ? count : 0L;
    }
    
    /**
     * 获取重置时间（秒）
     * 返回距离下一个时间窗口开始的时间
     * 
     * @param key 限流Key
     * @param window 时间窗口（秒）
     * @return 重置时间（秒）
     */
    public long getResetTime(String key, int window) {
        if (key == null || key.isEmpty()) {
            return 0;
        }
        
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            long windowStart = (currentTime / window) * window;
            long nextWindowStart = windowStart + window;
            return nextWindowStart - currentTime;
        } catch (Exception e) {
            logger.error("获取重置时间失败，key: {}", key, e);
            return window;
        }
    }
    
    /**
     * 清除限流记录
     * 用于测试或手动重置限流
     * 
     * @param key 限流Key
     */
    public void clearRateLimit(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        
        try {
            String slidingKey = SLIDING_WINDOW_PREFIX + key;
            
            redisTemplate.delete(slidingKey);
            // 注意：固定窗口的key会在过期后自动删除，无需手动清理
        } catch (Exception e) {
            logger.error("清除限流记录失败，key: {}", key, e);
        }
    }
}


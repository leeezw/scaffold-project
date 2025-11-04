package com.kite.authenticator.session.dao;

import com.kite.authenticator.session.DefaultSession;
import com.kite.authenticator.session.Session;
import com.kite.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis Session Dao 实现
 * 
 * @author yourname
 */
public class RedisSessionDao implements SessionDao {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisSessionDao.class);
    
    private static final String SESSION_KEY_PREFIX = "authc:session:";
    private static final String USER_SESSIONS_KEY_PREFIX = "authc:user:sessions:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisSessionDao(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    private String getSessionKey(String sessionKey) {
        return SESSION_KEY_PREFIX + sessionKey;
    }
    
    private String getUserSessionsKey(Long userId) {
        return USER_SESSIONS_KEY_PREFIX + userId;
    }
    
    @Override
    public void create(Session session) {
        String key = getSessionKey(session.getSessionKey());
        String sessionJson = JsonUtils.toJsonString(session);
        
        // 存储 Session
        redisTemplate.opsForValue().set(key, sessionJson);
        
        // 设置过期时间
        if (session.getExpireAt() != null) {
            long ttl = session.getExpireAt() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.expire(key, ttl, TimeUnit.MILLISECONDS);
            }
        }
        
        // 将 Session Key 添加到用户的 Session 集合中
        String userSessionsKey = getUserSessionsKey(session.getUserId());
        redisTemplate.opsForSet().add(userSessionsKey, session.getSessionKey());
        
        // 设置用户 Session 集合的过期时间（取最长的过期时间）
        Long expireAt = session.getExpireAt();
        if (expireAt != null) {
            long ttl = expireAt - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.expire(userSessionsKey, ttl, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    @Override
    public void update(Session session) {
        create(session); // Redis 的 set 操作会覆盖，所以直接调用 create
    }
    
    @Override
    public void delete(Session session) {
        String key = getSessionKey(session.getSessionKey());
        redisTemplate.delete(key);
        
        // 从用户的 Session 集合中移除
        String userSessionsKey = getUserSessionsKey(session.getUserId());
        redisTemplate.opsForSet().remove(userSessionsKey, session.getSessionKey());
        
        // 如果用户没有其他 Session，删除集合
        Long size = redisTemplate.opsForSet().size(userSessionsKey);
        if (size == null || size == 0) {
            redisTemplate.delete(userSessionsKey);
        }
    }
    
    @Override
    public Session get(String sessionKey) {
        String key = getSessionKey(sessionKey);
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof String) {
            return JsonUtils.parseObject((String) value, DefaultSession.class);
        }
        
        // 如果 Redis 返回的是对象，直接转换
        return JsonUtils.parseObject(JsonUtils.toJsonString(value), DefaultSession.class);
    }
    
    @Override
    public Set<String> getUserSessionKeys(Long userId) {
        String userSessionsKey = getUserSessionsKey(userId);
        Set<Object> sessionKeys = redisTemplate.opsForSet().members(userSessionsKey);
        
        if (sessionKeys == null || sessionKeys.isEmpty()) {
            return new java.util.HashSet<>();
        }
        
        Set<String> result = new java.util.HashSet<>();
        for (Object sessionKey : sessionKeys) {
            if (sessionKey != null) {
                result.add(sessionKey.toString());
            }
        }
        return result;
    }
    
    @Override
    public void deleteUserSessions(Long userId) {
        Set<String> sessionKeys = getUserSessionKeys(userId);
        for (String sessionKey : sessionKeys) {
            Session session = get(sessionKey);
            if (session != null) {
                delete(session);
            }
        }
    }
}


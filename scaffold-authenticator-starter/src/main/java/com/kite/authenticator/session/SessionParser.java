package com.kite.authenticator.session;

import com.kite.authenticator.context.LoginUser;

import java.util.UUID;

/**
 * Session 解析器
 * 用于生成和解析 Session Key
 * 
 * @author yourname
 */
public class SessionParser {
    
    /**
     * 生成 Session Key
     * 格式: userId:deviceId:uuid
     */
    public String generateSessionKey(LoginUser loginUser, String deviceId) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new IllegalArgumentException("LoginUser 和 UserId 不能为空");
        }
        
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return loginUser.getUserId() + ":" + (deviceId != null ? deviceId : "default") + ":" + uuid;
    }
    
    /**
     * 从 Session Key 中提取用户ID
     */
    public Long extractUserId(String sessionKey) {
        if (sessionKey == null || sessionKey.isEmpty()) {
            return null;
        }
        
        String[] parts = sessionKey.split(":");
        if (parts.length > 0) {
            try {
                return Long.parseLong(parts[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 从 Session Key 中提取设备ID
     */
    public String extractDeviceId(String sessionKey) {
        if (sessionKey == null || sessionKey.isEmpty()) {
            return null;
        }
        
        String[] parts = sessionKey.split(":");
        if (parts.length > 1) {
            return parts[1];
        }
        return null;
    }
}


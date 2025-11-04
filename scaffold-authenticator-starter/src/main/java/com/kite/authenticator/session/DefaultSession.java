package com.kite.authenticator.session;

import com.kite.authenticator.session.enums.UserStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * Session 默认实现
 * 
 * @author yourname
 */
@Data
public class DefaultSession implements Session {
    
    private static final long serialVersionUID = 1L;
    
    private String sessionKey;
    private Long userId;
    private String deviceId;
    private Long expireAt;
    private Long lastAccessTime;
    private Integer status;
    private Long startTime;
    
    @Override
    public boolean isExpired() {
        if (expireAt == null) {
            return false;
        }
        return System.currentTimeMillis() > expireAt;
    }
    
    @Override
    public void touch() {
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    @Override
    public void renewal(Long renewalInterval) {
        if (renewalInterval != null && renewalInterval > 0) {
            this.expireAt = System.currentTimeMillis() + renewalInterval;
        }
    }
    
    @Override
    public boolean exceedSessionTimeout(Long sessionTimeout) {
        if (sessionTimeout == null || sessionTimeout <= 0) {
            return false;
        }
        if (lastAccessTime == null) {
            return false;
        }
        return (System.currentTimeMillis() - lastAccessTime) > sessionTimeout;
    }
    
    @Override
    public boolean matchDevice(String deviceId) {
        if (deviceId == null || this.deviceId == null) {
            return false;
        }
        return this.deviceId.equals(deviceId);
    }
    
    @Override
    public void modifyStatus(UserStatus status) {
        if (status != null) {
            this.status = status.getCode();
        }
    }
}


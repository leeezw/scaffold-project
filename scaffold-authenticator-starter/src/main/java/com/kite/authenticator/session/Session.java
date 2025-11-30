package com.kite.authenticator.session;

import com.kite.authenticator.session.enums.UserStatus;

import java.io.Serializable;

/**
 * Session 接口
 * 
 * @author yourname
 */
public interface Session extends SessionKey, SessionStatus, Serializable {
    
    /**
     * 设置 Session Key
     */
    void setSessionKey(String sessionKey);
    
    /**
     * 设置用户ID
     */
    void setUserId(Long userId);
    
    /**
     * 设置设备ID
     */
    void setDeviceId(String deviceId);
    
    /**
     * 设置过期时间
     */
    void setExpireAt(Long expireAt);
    
    /**
     * 设置最后访问时间
     */
    void setLastAccessTime(Long lastAccessTime);
    
    /**
     * 设置状态
     */
    void setStatus(Integer status);
    
    /**
     * 设置开始时间
     */
    void setStartTime(Long startTime);
    
    /**
     * 设置最近操作时间
     */
    void setOperateAt(Long operateAt);
    
    /**
     * 判断是否过期
     */
    boolean isExpired();
    
    /**
     * 更新最后访问时间（touch）
     */
    void touch();
    
    /**
     * 续期
     */
    void renewal(Long renewalInterval);
    
    /**
     * 判断是否超过会话超时时间
     */
    boolean exceedSessionTimeout(Long sessionTimeout);
    
    /**
     * 判断设备是否匹配
     */
    boolean matchDevice(String deviceId);
    
    /**
     * 修改状态
     */
    void modifyStatus(UserStatus status);
}

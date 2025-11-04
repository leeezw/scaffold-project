package com.kite.authenticator.session;

import java.io.Serializable;

/**
 * Session 状态接口
 * 
 * @author yourname
 */
public interface SessionStatus extends Serializable {
    
    /**
     * 获取 Session Key
     */
    String getSessionKey();
    
    /**
     * 获取用户ID
     */
    Long getUserId();
    
    /**
     * 获取设备ID
     */
    String getDeviceId();
    
    /**
     * 获取过期时间（时间戳）
     */
    Long getExpireAt();
    
    /**
     * 获取最后访问时间（时间戳）
     */
    Long getLastAccessTime();
    
    /**
     * 获取状态码
     */
    Integer getStatus();
    
    /**
     * 获取开始时间（时间戳）
     */
    Long getStartTime();
}


package com.kite.authenticator.session;

import java.io.Serializable;

/**
 * Session Key 接口
 * 用于标识 Session 的唯一键
 * 
 * @author yourname
 */
public interface SessionKey extends Serializable {
    
    /**
     * 获取 Session Key 字符串
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
}


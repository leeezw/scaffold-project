package com.kite.authenticator;

import com.kite.authenticator.config.AuthenticatorProperties;

/**
 * 认证配置读取器接口
 * 
 * @author yourname
 */
public interface AuthenticatorConfigReader {
    
    /**
     * 获取是否验证设备
     */
    Boolean getValidateHost();
    
    /**
     * 是否启用 Session 续期
     */
    Boolean getRenewal();
    
    /**
     * 是否验证用户状态
     */
    Boolean getValidateStatus();
    
    /**
     * 获取 Session 超时时间
     */
    Long getSessionTimeout();
    
    /**
     * 获取 Session 续期间隔
     */
    Long getRenewalInterval();
}


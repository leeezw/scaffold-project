package com.kite.authenticator;

import java.io.Serializable;

/**
 * 认证令牌接口
 * 表示用户提交的认证信息（如 Token、用户名密码等）
 * 
 * @author yourname
 */
public interface AuthenticationToken extends Serializable {
    
    /**
     * 获取主体（如用户名、用户ID）
     */
    Object getPrincipal();
    
    /**
     * 获取凭证（如密码、Token）
     */
    String getCredential();
}


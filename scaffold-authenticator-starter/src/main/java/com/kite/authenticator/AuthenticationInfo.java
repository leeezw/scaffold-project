package com.kite.authenticator;

import com.kite.authenticator.context.LoginUser;

import java.io.Serializable;

/**
 * 认证信息接口
 * 表示从系统中获取的已认证用户信息
 * 
 * @author yourname
 */
public interface AuthenticationInfo extends Serializable {
    
    /**
     * 获取登录用户信息
     */
    LoginUser getUser();
    
    /**
     * 获取凭证（用于签名验证）
     */
    String getCredential();
}


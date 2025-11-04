package com.kite.authenticator;

import com.kite.authenticator.context.LoginUser;

/**
 * 认证域接口
 * 负责从系统中获取用户认证信息
 * 
 * @author yourname
 */
public interface Realm {
    
    /**
     * 判断是否支持该类型的 Token
     * 
     * @param token 认证令牌
     * @return 是否支持
     */
    boolean support(AuthenticationToken token);
    
    /**
     * 获取认证信息
     * 
     * @param token 认证令牌
     * @return 认证信息
     */
    AuthenticationInfo getAuthenticationInfo(AuthenticationToken token);
}


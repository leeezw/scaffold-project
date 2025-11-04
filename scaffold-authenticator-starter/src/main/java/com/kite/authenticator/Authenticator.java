package com.kite.authenticator;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.token.HostAuthenticationToken;

/**
 * 认证器接口
 * 
 * @author yourname
 */
public interface Authenticator {
    
    /**
     * 登录（生成 Token）
     * 
     * @param authenticationInfo 认证信息
     * @return Token 字符串
     */
    String login(AuthenticationInfo authenticationInfo);
    
    /**
     * 认证（验证 Token）
     * 
     * @param token 认证令牌（包含 Token 和设备信息）
     * @return 登录用户信息
     */
    LoginUser authenticate(HostAuthenticationToken token);
}


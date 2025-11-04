package com.kite.authenticator.realm;

import com.kite.authenticator.AuthenticationInfo;
import com.kite.authenticator.AuthenticationToken;
import com.kite.authenticator.Realm;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.service.AuthenticationService;
import com.kite.authenticator.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * 用户认证域实现
 * 从业务层获取用户认证信息
 * 
 * @author yourname
 */
@Slf4j
public class UserRealm implements Realm {
    
    @Autowired(required = false)
    private AuthenticationService authenticationService;
    
    @Value("${kite.auth.secret:}")
    private String jwtSecret;
    
    public UserRealm() {
    }
    
    public UserRealm(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    
    @Override
    public boolean support(AuthenticationToken token) {
        return token instanceof com.kite.authenticator.token.HostAuthenticationToken;
    }
    
    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) {
        if (!(token instanceof com.kite.authenticator.token.HostAuthenticationToken)) {
            throw new IllegalArgumentException("不支持的 Token 类型");
        }
        
        com.kite.authenticator.token.HostAuthenticationToken hostToken = 
            (com.kite.authenticator.token.HostAuthenticationToken) token;
        String tokenStr = hostToken.getCredential();
        
        if (StringUtils.isEmpty(tokenStr)) {
            return null;
        }
        
        try {
            // 从 Token 中解析用户信息
            LoginUser loginUser = JwtUtils.parseToken(tokenStr, jwtSecret);
            
            return new AuthenticationInfo() {
                @Override
                public LoginUser getUser() {
                    return loginUser;
                }
                
                @Override
                public String getCredential() {
                    return tokenStr;
                }
            };
        } catch (Exception e) {
            log.warn("解析 Token 失败: {}", e.getMessage());
            return null;
        }
    }
}


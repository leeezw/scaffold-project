package com.kite.authenticator.realm;

import com.kite.authenticator.AuthenticationInfo;
import com.kite.authenticator.AuthenticationToken;
import com.kite.authenticator.Realm;
import com.kite.authenticator.context.LoginUser;

/**
 * 空 Realm 实现（默认实现）
 * 当没有提供 AuthenticationService 时使用
 * 
 * @author yourname
 */
public class EmptyRealm implements Realm {
    
    @Override
    public boolean support(AuthenticationToken token) {
        return true;
    }
    
    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) {
        // 返回空的认证信息
        return new AuthenticationInfo() {
            @Override
            public LoginUser getUser() {
                return null;
            }
            
            @Override
            public String getCredential() {
                return token.getCredential();
            }
        };
    }
}


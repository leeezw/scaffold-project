package com.kite.authenticator.token;

import com.kite.authenticator.AuthenticationToken;
import lombok.Data;

/**
 * 带主机/设备信息的认证令牌
 * 
 * @author yourname
 */
@Data
public class HostAuthenticationToken implements AuthenticationToken {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Token 凭证
     */
    private String credential;
    
    /**
     * 设备ID/主机ID
     */
    private String host;
    
    public HostAuthenticationToken() {
    }
    
    public HostAuthenticationToken(String credential, String host) {
        this.credential = credential;
        this.host = host;
    }
    
    @Override
    public Object getPrincipal() {
        return host;  // 使用 host 作为 principal
    }
    
    @Override
    public String getCredential() {
        return credential;
    }
}


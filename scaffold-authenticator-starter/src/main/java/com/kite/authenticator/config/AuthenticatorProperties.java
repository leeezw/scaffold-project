package com.kite.authenticator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证配置属性
 * 
 * @author yourname
 */
@Data
@ConfigurationProperties(prefix = "kite.auth")
public class AuthenticatorProperties {
    
    /**
     * Token 在 Header 中的 Key（默认：Authorization）
     */
    private String tokenHeader = "Authorization";
    
    /**
     * Token 前缀（默认：Bearer ）
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * JWT 密钥（必填）
     */
    private String secret;
    
    /**
     * Token 过期时间（毫秒，默认：7天）
     */
    private Long expireTime = 7 * 24 * 60 * 60 * 1000L;
    
    /**
     * 是否启用认证（默认：true）
     */
    private Boolean enabled = true;
    
    /**
     * 是否启用 Mock 用户（开发环境使用，默认：false）
     */
    private Boolean mockEnabled = false;
    
    /**
     * Mock 用户ID（仅在 mockEnabled=true 时生效）
     */
    private Long mockUserId = 1L;
    
    /**
     * Mock 用户名（仅在 mockEnabled=true 时生效）
     */
    private String mockUsername = "mock-user";
    
    /**
     * 排除路径列表（不需要认证的路径）
     */
    private List<String> excludePaths = new ArrayList<>();
    
    /**
     * 是否验证权限（默认：true）
     */
    private Boolean checkPermission = true;
}


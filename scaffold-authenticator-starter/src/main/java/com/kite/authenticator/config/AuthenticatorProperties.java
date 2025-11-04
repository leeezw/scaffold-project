package com.kite.authenticator.config;

import com.kite.authenticator.AuthenticatorConfigReader;
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
public class AuthenticatorProperties implements AuthenticatorConfigReader {
    
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
    
    /**
     * Session 配置
     */
    private Session session = new Session();
    
    @Data
    public static class Session {
        /**
         * 是否启用 Session 管理（默认：true）
         */
        private Boolean enabled = true;
        
        /**
         * 是否验证设备（默认：true）
         */
        private Boolean validateDevice = true;
        
        /**
         * 是否验证用户状态（默认：true）
         */
        private Boolean validateStatus = true;
        
        /**
         * 是否启用 Session 续期（默认：true）
         */
        private Boolean renewal = true;
        
        /**
         * Session 超时时间（毫秒，默认：30分钟）
         * 超过此时间未访问，需要重新登录
         */
        private Long timeout = 30 * 60 * 1000L;
        
        /**
         * Session 续期间隔（毫秒，默认：7天）
         * 每次访问时，如果未超过超时时间，则续期
         */
        private Long renewalInterval = 7 * 24 * 60 * 60 * 1000L;
    }
    
    // AuthenticatorConfigReader 接口实现
    
    @Override
    public Boolean getValidateHost() {
        return session != null && session.getValidateDevice() != null ? session.getValidateDevice() : true;
    }
    
    @Override
    public Boolean getRenewal() {
        return session != null && session.getRenewal() != null ? session.getRenewal() : true;
    }
    
    @Override
    public Boolean getValidateStatus() {
        return session != null && session.getValidateStatus() != null ? session.getValidateStatus() : true;
    }
    
    @Override
    public Long getSessionTimeout() {
        return session != null ? session.getTimeout() : null;
    }
    
    @Override
    public Long getRenewalInterval() {
        return session != null ? session.getRenewalInterval() : null;
    }
}


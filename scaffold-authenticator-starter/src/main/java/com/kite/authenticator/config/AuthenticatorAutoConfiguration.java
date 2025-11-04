package com.kite.authenticator.config;

import com.kite.authenticator.filter.AuthenticationFilter;
import com.kite.authenticator.session.SessionManager;
import com.kite.authenticator.session.SessionParser;
import com.kite.authenticator.session.dao.RedisSessionDao;
import com.kite.authenticator.session.dao.SessionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 认证自动配置类
 * 
 * @author yourname
 */
@Configuration
@EnableConfigurationProperties(AuthenticatorProperties.class)
@ConditionalOnProperty(prefix = "kite.auth", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticatorAutoConfiguration {
    
    @Bean
    public SessionParser sessionParser() {
        return new SessionParser();
    }
    
    /**
     * Redis Session Dao（当存在 RedisTemplate 时自动配置）
     */
    @Bean
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnBean(RedisTemplate.class)
    public SessionDao redisSessionDao(RedisTemplate<String, Object> redisTemplate) {
        return new RedisSessionDao(redisTemplate);
    }
    
    /**
     * Session Manager
     */
    @Bean
    @ConditionalOnBean(SessionDao.class)
    public SessionManager sessionManager(SessionDao sessionDao, SessionParser sessionParser) {
        return new SessionManager(sessionDao, sessionParser);
    }
    
    /**
     * Authentication Filter（支持 Session 和纯 JWT 两种模式）
     */
    @Bean
    public AuthenticationFilter authenticationFilter(AuthenticatorProperties properties,
                                                     @Autowired(required = false) SessionManager sessionManager) {
        // 如果 Session 被禁用或 SessionManager 不存在，则传入 null
        if (properties.getSession() != null 
            && properties.getSession().getEnabled() != null 
            && !properties.getSession().getEnabled()) {
            return new AuthenticationFilter(properties, null);
        }
        return new AuthenticationFilter(properties, sessionManager);
    }
    
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistration(
            AuthenticationFilter authenticationFilter) {
        FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authenticationFilter);
        registration.addUrlPatterns("/*");
        registration.setName("authenticationFilter");
        registration.setOrder(1); // 设置优先级
        return registration;
    }
}


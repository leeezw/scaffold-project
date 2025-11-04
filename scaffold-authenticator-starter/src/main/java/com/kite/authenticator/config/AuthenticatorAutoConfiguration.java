package com.kite.authenticator.config;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.filter.AuthenticationFilter;
import com.kite.authenticator.util.JwtUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public AuthenticationFilter authenticationFilter(AuthenticatorProperties properties) {
        return new AuthenticationFilter(properties);
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


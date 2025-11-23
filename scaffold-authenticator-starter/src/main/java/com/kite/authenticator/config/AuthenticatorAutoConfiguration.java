package com.kite.authenticator.config;

import com.kite.authenticator.*;
import com.kite.authenticator.filter.AuthenticationFilter;
import com.kite.authenticator.context.LoginUserContextCustomizer;
import com.kite.authenticator.realm.EmptyRealm;
import com.kite.authenticator.realm.UserRealm;
import com.kite.authenticator.service.AuthenticationService;
import com.kite.authenticator.session.SessionManager;
import com.kite.authenticator.session.SessionParser;
import com.kite.authenticator.session.dao.RedisSessionDao;
import com.kite.authenticator.session.dao.SessionDao;
import com.kite.authenticator.resolvers.LoginUserArgumentResolver;
import com.kite.authenticator.signature.JwtHmacSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

/**
 * 认证自动配置类
 * 
 * @author yourname
 */
@Configuration
@EnableConfigurationProperties(AuthenticatorProperties.class)
@ConditionalOnProperty(prefix = "kite.auth", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticatorAutoConfiguration implements WebMvcConfigurer {
    
    @Bean
    public SessionParser sessionParser() {
        return new SessionParser();
    }

    @Bean
    @ConditionalOnClass(RequestMappingHandlerMapping.class)
    public AllowAnonymousRegistry allowAnonymousRegistry(
            ObjectProvider<List<RequestMappingHandlerMapping>> handlerMappingsProvider) {
        return new AllowAnonymousRegistry(handlerMappingsProvider);
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
     * Session Manager（当存在 SessionDao 时自动配置）
     */
    @Bean
    @ConditionalOnBean(SessionDao.class)
    public SessionManager sessionManager(SessionDao sessionDao, SessionParser sessionParser) {
        return new SessionManager(sessionDao, sessionParser);
    }
    
    /**
     * Realm（优先使用 UserRealm，如果没有 AuthenticationService 则使用 EmptyRealm）
     */
    @Bean
    @ConditionalOnMissingBean(Realm.class)
    public Realm realm(@Autowired(required = false) AuthenticationService authenticationService,
                       AuthenticatorProperties properties,
                       org.springframework.beans.factory.config.AutowireCapableBeanFactory beanFactory) {
        if (authenticationService != null) {
            // 创建 UserRealm 并通过 AutowireCapableBeanFactory 注入依赖
            UserRealm userRealm = new UserRealm();
            userRealm.setJwtSecret(properties.getSecret());
            // 使用 AutowireCapableBeanFactory 注入 AuthenticationService
            beanFactory.autowireBean(userRealm);
            return userRealm;
        }
        return new EmptyRealm();
    }
    
    /**
     * Signature（JWT HMAC 实现）
     */
    @Bean
    @ConditionalOnMissingBean(Signature.class)
    public Signature signature(AuthenticatorProperties properties) {
        return new JwtHmacSignature(properties.getSecret());
    }
    
    /**
     * Authenticator（默认安全管理器）
     */
    @Bean
    @ConditionalOnMissingBean(Authenticator.class)
    public Authenticator authenticator(
            Realm realm,
            Signature signature,
            AuthenticatorProperties properties,
            SessionParser sessionParser,
            @Autowired(required = false) SessionManager sessionManager,
            @Autowired(required = false) SessionDao sessionDao) {
        return new DefaultSecurityManager(
            realm, 
            signature, 
            sessionManager, 
            sessionDao, 
            sessionParser, 
            properties);
    }
    
    /**
     * Authentication Filter（使用 Authenticator）
     */
    @Bean
    public AuthenticationFilter authenticationFilter(
            AuthenticatorProperties properties,
            Authenticator authenticator,
            @Autowired(required = false) AllowAnonymousRegistry allowAnonymousRegistry,
            @Autowired(required = false) List<LoginUserContextCustomizer> contextCustomizers) {
        return new AuthenticationFilter(properties, authenticator, allowAnonymousRegistry, contextCustomizers);
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
    
    /**
     * LoginUser 参数解析器
     */
    @Bean
    @ConditionalOnMissingBean(LoginUserArgumentResolver.class)
    public LoginUserArgumentResolver loginUserArgumentResolver() {
        return new LoginUserArgumentResolver();
    }
    
    /**
     * 注册参数解析器到 Spring MVC
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // LoginUserArgumentResolver 是无状态的，直接创建实例避免循环依赖
        // 如果需要使用 Bean，可以通过 ApplicationContext 获取
        resolvers.add(new LoginUserArgumentResolver());
    }
}

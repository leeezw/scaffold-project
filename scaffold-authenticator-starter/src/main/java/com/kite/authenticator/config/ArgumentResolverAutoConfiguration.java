package com.kite.authenticator.config;

import com.kite.authenticator.resolvers.LoginUserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 参数解析器自动配置
 * 注册 LoginUserArgumentResolver 到 Spring MVC
 * 
 * @author yourname
 */
@Configuration
public class ArgumentResolverAutoConfiguration implements WebMvcConfigurer {
    
    @Bean
    public LoginUserArgumentResolver loginUserArgumentResolver() {
        return new LoginUserArgumentResolver();
    }
    
    @Autowired
    private LoginUserArgumentResolver loginUserArgumentResolver;
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}


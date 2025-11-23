package com.kite.authenticator.annotation;

import com.kite.authenticator.enums.RateLimitAlgorithm;
import com.kite.authenticator.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * 用于方法级别或类级别的限流配置
 * 
 * @author yourname
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 限流维度
     * 
     * @return 限流类型
     */
    RateLimitType type() default RateLimitType.IP;
    
    /**
     * 时间窗口（秒）
     * 在此时间窗口内限制请求数
     * 
     * @return 时间窗口（秒）
     */
    int window() default 60;
    
    /**
     * 最大请求数
     * 在时间窗口内允许的最大请求数
     * 
     * @return 最大请求数
     */
    int maxRequests() default 100;
    
    /**
     * 限流算法
     * 
     * @return 限流算法
     */
    RateLimitAlgorithm algorithm() default RateLimitAlgorithm.SLIDING_WINDOW;
    
    /**
     * 限流提示信息
     * 当触发限流时返回的提示信息
     * 
     * @return 提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 是否启用限流
     * 可用于动态控制限流功能
     * 
     * @return 是否启用
     */
    boolean enabled() default true;
}


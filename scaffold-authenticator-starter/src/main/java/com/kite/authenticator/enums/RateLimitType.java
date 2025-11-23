package com.kite.authenticator.enums;

/**
 * 限流类型枚举
 * 
 * @author yourname
 */
public enum RateLimitType {
    
    /**
     * 基于IP限流
     * 适用于防止单个IP的恶意请求
     */
    IP,
    
    /**
     * 基于用户限流（需要登录）
     * 适用于限制单个用户的操作频率
     */
    USER,
    
    /**
     * 基于Token限流
     * 适用于检测Token泄露或异常使用
     */
    TOKEN,
    
    /**
     * 全局限流（所有请求）
     * 适用于保护整个接口或服务
     */
    GLOBAL
}


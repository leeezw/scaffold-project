package com.kite.authenticator.enums;

/**
 * 限流算法枚举
 * 
 * @author yourname
 */
public enum RateLimitAlgorithm {
    
    /**
     * 滑动窗口算法（推荐）
     * 优点：更平滑，避免突发流量，限流更精确
     * 缺点：实现稍复杂，需要更多Redis操作
     * 适用场景：大多数场景，特别是需要精确限流的场景
     */
    SLIDING_WINDOW,
    
    /**
     * 固定窗口算法
     * 优点：实现简单，Redis操作少，性能好
     * 缺点：窗口边界可能有突发流量
     * 适用场景：对精度要求不高的场景，高并发场景
     */
    FIXED_WINDOW
}


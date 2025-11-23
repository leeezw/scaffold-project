package com.kite.authenticator.config;

import com.kite.authenticator.annotation.RateLimit;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.authenticator.enums.RateLimitType;
import com.kite.authenticator.service.RateLimitService;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 限流切面
 * 拦截带有 @RateLimit 注解的方法，进行限流检查
 * 
 * @author yourname
 */
@Aspect
@Component
@Order(1) // 在权限检查之前执行
public class RateLimitAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    
    private final RateLimitService rateLimitService;
    
    public RateLimitAspect(@Autowired(required = false) RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }
    
    @Around("@annotation(com.kite.authenticator.annotation.RateLimit) || " +
            "@within(com.kite.authenticator.annotation.RateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取方法级别的注解
        RateLimit methodAnnotation = method.getAnnotation(RateLimit.class);
        
        // 获取类级别的注解
        Class<?> clazz = joinPoint.getTarget().getClass();
        RateLimit classAnnotation = clazz.getAnnotation(RateLimit.class);
        
        // 方法级别注解优先
        RateLimit rateLimit = methodAnnotation != null ? methodAnnotation : classAnnotation;
        
        if (rateLimit == null || !rateLimit.enabled()) {
            // 未配置限流或已禁用，直接放行
            return joinPoint.proceed();
        }
        
        // 如果限流服务不可用，直接放行（避免影响业务）
        if (rateLimitService == null) {
            logger.warn("限流服务不可用，跳过限流检查");
            return joinPoint.proceed();
        }
        
        // 提取限流Key
        String rateLimitKey = extractRateLimitKey(rateLimit.type());
        if (rateLimitKey == null || rateLimitKey.isEmpty()) {
            // 无法提取key，直接放行（避免影响业务）
            logger.warn("无法提取限流Key，类型: {}", rateLimit.type());
            return joinPoint.proceed();
        }
        
        // 检查限流
        boolean allowed = rateLimitService.allowRequest(
            rateLimitKey,
            rateLimit.window(),
            rateLimit.maxRequests(),
            rateLimit.algorithm()
        );
        
        if (!allowed) {
            // 触发限流
            long remaining = rateLimitService.getRemainingRequests(
                rateLimitKey,
                rateLimit.window(),
                rateLimit.maxRequests()
            );
            long resetTime = rateLimitService.getResetTime(rateLimitKey, rateLimit.window());
            
            logger.warn("触发限流，key: {}, 类型: {}, 剩余请求数: {}, 重置时间: {}秒",
                rateLimitKey, rateLimit.type(), remaining, resetTime);
            
            throw new BusinessException(
                ResultCode.TOO_MANY_REQUESTS.getCode(),
                rateLimit.message() + String.format("（剩余请求数: %d，重置时间: %d秒后）", remaining, resetTime)
            );
        }
        
        // 允许请求，继续执行
        return joinPoint.proceed();
    }
    
    /**
     * 根据限流类型提取限流Key
     */
    private String extractRateLimitKey(RateLimitType type) {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        switch (type) {
            case IP:
                return extractIp(request) + ":" + method + ":" + path;
                
            case USER:
                LoginUser loginUser = LoginUserContext.getLoginUser();
                if (loginUser == null) {
                    // 用户未登录，使用IP作为fallback
                    return extractIp(request) + ":" + method + ":" + path;
                }
                return "user:" + loginUser.getUserId() + ":" + method + ":" + path;
                
            case TOKEN:
                String token = extractToken(request);
                if (token == null || token.isEmpty()) {
                    // Token为空，使用IP作为fallback
                    return extractIp(request) + ":" + method + ":" + path;
                }
                // 使用Token的哈希值（避免存储完整Token）
                return "token:" + hashToken(token) + ":" + method + ":" + path;
                
            case GLOBAL:
                return "global:" + method + ":" + path;
                
            default:
                return extractIp(request) + ":" + method + ":" + path;
        }
    }
    
    /**
     * 提取IP地址
     */
    private String extractIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果IP包含多个值，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
    
    /**
     * 提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
    
    /**
     * Token哈希（避免存储完整Token）
     */
    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Token哈希失败", e);
            // 如果哈希失败，使用Token的前16个字符
            return token.length() > 16 ? token.substring(0, 16) : token;
        }
    }
}


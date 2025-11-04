package com.kite.usercenter.aspect;

import com.kite.common.util.JsonUtils;
import com.kite.common.annotation.OperationLog;
import com.kite.common.log.OperationLogContext;
import com.kite.usercenter.entity.OperationLogEntity;
import com.kite.usercenter.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 
 * @author yourname
 */
@Aspect
@Component
public class OperationLogAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);
    
    @Autowired(required = false)
    private OperationLogService operationLogService;
    
    @Pointcut("@annotation(com.kite.common.annotation.OperationLog)")
    public void operationLogPointcut() {
    }
    
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        
        OperationLogEntity logEntity = new OperationLogEntity();
        logEntity.setCreateTime(LocalDateTime.now());
        
        OperationLogContext context = OperationLogContext.getContext();
        if (context != null) {
            logEntity.setUserId(context.getUserId());
            logEntity.setUsername(context.getUsername());
        }
        
        if (request != null) {
            logEntity.setMethod(request.getMethod());
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setIpAddress(getIpAddress(request));
            logEntity.setUserAgent(request.getHeader("User-Agent"));
        }
        
        logEntity.setModule(operationLog.module());
        logEntity.setOperationType(operationLog.operationType());
        logEntity.setDescription(operationLog.description());
        
        if (operationLog.recordParams()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = JsonUtils.toJsonString(filterSensitiveParams(args));
                if (params != null && params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                logEntity.setRequestParams(params);
            }
        }
        
        Object result = null;
        Throwable throwable = null;
        
        try {
            result = joinPoint.proceed();
            
            if (operationLog.recordResult() && result != null) {
                String resultStr = JsonUtils.toJsonString(result);
                if (resultStr != null && resultStr.length() > 2000) {
                    resultStr = resultStr.substring(0, 2000) + "...";
                }
                logEntity.setResponseResult(resultStr);
            }
            
            logEntity.setStatus(1);
        } catch (Throwable e) {
            throwable = e;
            logEntity.setStatus(0);
            logEntity.setErrorMsg(e.getMessage());
            if (e.getMessage() != null && e.getMessage().length() > 1000) {
                logEntity.setErrorMsg(e.getMessage().substring(0, 1000) + "...");
            }
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            logEntity.setExecutionTime(executionTime);
            
            if (operationLogService != null) {
                try {
                    operationLogService.saveAsync(logEntity);
                } catch (Exception e) {
                    logger.error("保存操作日志失败", e);
                }
            }
        }
        
        if (throwable != null) {
            throw throwable;
        }
        
        return result;
    }
    
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    private Object[] filterSensitiveParams(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }
        
        Object[] filtered = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg != null) {
                String className = arg.getClass().getName().toLowerCase();
                if (className.contains("password") || className.contains("pwd")) {
                    filtered[i] = "***";
                } else {
                    filtered[i] = arg;
                }
            } else {
                filtered[i] = arg;
            }
        }
        return filtered;
    }
}


package com.kite.authenticator.config;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.authenticator.annotation.RequiresRoles;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 权限检查切面
 * 
 * @author yourname
 */
@Aspect
@Component
@Order(2)
public class PermissionAspect {
    
    @Before("@annotation(com.kite.authenticator.annotation.RequiresRoles) || " +
            "@annotation(com.kite.authenticator.annotation.RequiresPermissions)")
    public void checkPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 检查类级别的注解
        Class<?> clazz = joinPoint.getTarget().getClass();
        RequiresRoles classRoles = clazz.getAnnotation(RequiresRoles.class);
        RequiresPermissions classPermissions = clazz.getAnnotation(RequiresPermissions.class);
        
        // 检查方法级别的注解
        RequiresRoles methodRoles = method.getAnnotation(RequiresRoles.class);
        RequiresPermissions methodPermissions = method.getAnnotation(RequiresPermissions.class);
        
        // 方法级别注解优先
        RequiresRoles roles = methodRoles != null ? methodRoles : classRoles;
        RequiresPermissions permissions = methodPermissions != null ? methodPermissions : classPermissions;
        
        LoginUser loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        
        // 检查角色
        if (roles != null) {
            checkRoles(loginUser, roles);
        }
        
        // 检查权限
        if (permissions != null) {
            checkPermissions(loginUser, permissions);
        }
    }
    
    /**
     * 检查角色
     */
    private void checkRoles(LoginUser loginUser, RequiresRoles roles) {
        String[] requiredRoles = roles.value();
        if (requiredRoles == null || requiredRoles.length == 0) {
            return;
        }
        
        List<String> userRoles = loginUser.getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "用户没有角色");
        }
        
        if (roles.logical()) {
            // 需要所有角色
            for (String role : requiredRoles) {
                if (!userRoles.contains(role)) {
                    throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "缺少角色: " + role);
                }
            }
        } else {
            // 只需其中一个角色
            boolean hasRole = false;
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), 
                    "需要以下角色之一: " + Arrays.toString(requiredRoles));
            }
        }
    }
    
    /**
     * 检查权限
     */
    private void checkPermissions(LoginUser loginUser, RequiresPermissions permissions) {
        String[] requiredPermissions = permissions.value();
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return;
        }
        
        List<String> userPermissions = loginUser.getPermissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "用户没有权限");
        }
        
        if (permissions.logical()) {
            // 需要所有权限
            for (String permission : requiredPermissions) {
                if (!userPermissions.contains(permission)) {
                    throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "缺少权限: " + permission);
                }
            }
        } else {
            // 只需其中一个权限
            boolean hasPermission = false;
            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), 
                    "需要以下权限之一: " + Arrays.toString(requiredPermissions));
            }
        }
    }
}


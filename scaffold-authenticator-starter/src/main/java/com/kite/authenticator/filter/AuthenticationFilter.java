package com.kite.authenticator.filter;

import com.kite.authenticator.Authenticator;
import com.kite.authenticator.config.AuthenticatorProperties;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.authenticator.token.HostAuthenticationToken;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 认证过滤器
 * 拦截请求，验证 Token，设置登录用户上下文
 * 
 * @author yourname
 */
public class AuthenticationFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    private final AuthenticatorProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Authenticator authenticator;
    
    public AuthenticationFilter(AuthenticatorProperties properties, Authenticator authenticator) {
        this.properties = properties;
        this.authenticator = authenticator;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            requestPath = requestPath.substring(contextPath.length());
        }
        
        // 检查是否需要排除的路径
        if (isExcludePath(requestPath)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Mock 模式（开发环境）
        if (properties.getMockEnabled() != null && properties.getMockEnabled()) {
            LoginUser mockUser = createMockUser();
            LoginUserContext.setLoginUser(mockUser);
            try {
                chain.doFilter(request, response);
            } finally {
                LoginUserContext.clear();
            }
            return;
        }
        
        // 获取 Token
        String token = extractToken(httpRequest);
        
        if (token == null || token.isEmpty()) {
            handleUnauthorized(httpResponse, "未提供 Token");
            return;
        }
        
        try {
            // 提取设备ID
            String deviceId = extractDeviceId(httpRequest);
            
            // 创建认证令牌
            HostAuthenticationToken authToken = new HostAuthenticationToken(token, deviceId);
            
            // 调用 Authenticator 进行认证
            LoginUser loginUser = authenticator.authenticate(authToken);
            
            // 设置登录用户上下文
            LoginUserContext.setLoginUser(loginUser);
            
            try {
                chain.doFilter(request, response);
            } finally {
                // 清除上下文
                LoginUserContext.clear();
            }
            
        } catch (BusinessException e) {
            logger.warn("Token 验证失败: {}", e.getMessage());
            handleUnauthorized(httpResponse, e.getMessage());
        } catch (Exception e) {
            logger.error("认证处理异常", e);
            handleUnauthorized(httpResponse, "认证失败");
        }
    }
    
    /**
     * 提取设备ID
     */
    private String extractDeviceId(HttpServletRequest request) {
        // 优先从 Header 中获取
        String deviceId = request.getHeader("X-Device-Id");
        if (!StringUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        
        // 从 User-Agent 中提取（简单实现）
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.isEmpty(userAgent)) {
            return userAgent.hashCode() + "";
        }
        
        // 使用 IP 地址作为设备ID（简单实现）
        return request.getRemoteAddr();
    }
    
    /**
     * 提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String headerName = properties.getTokenHeader();
        String tokenPrefix = properties.getTokenPrefix();
        
        // 从 Header 中获取
        String token = request.getHeader(headerName);
        if (token != null && !token.isEmpty()) {
            if (token.startsWith(tokenPrefix)) {
                return token.substring(tokenPrefix.length());
            }
            return token;
        }
        
        // 从 Cookie 中获取
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                if (headerName.equals(cookie.getName())) {
                    token = cookie.getValue();
                    if (token != null && token.startsWith(tokenPrefix)) {
                        return token.substring(tokenPrefix.length());
                    }
                    return token;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 检查是否为排除路径
     */
    private boolean isExcludePath(String path) {
        List<String> excludePaths = properties.getExcludePaths();
        if (excludePaths == null || excludePaths.isEmpty()) {
            return false;
        }
        
        for (String excludePath : excludePaths) {
            if (pathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 创建 Mock 用户
     */
    private LoginUser createMockUser() {
        LoginUser mockUser = new LoginUser();
        mockUser.setUserId(properties.getMockUserId());
        mockUser.setUsername(properties.getMockUsername());
        mockUser.setNickname("Mock 用户");
        mockUser.setExpireAt(System.currentTimeMillis() + properties.getExpireTime());
        return mockUser;
    }
    
    /**
     * 处理未授权响应
     */
    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
            "{\"code\":%d,\"message\":\"%s\",\"timestamp\":%d}",
            ResultCode.UNAUTHORIZED.getCode(),
            message,
            System.currentTimeMillis()
        ));
    }
}


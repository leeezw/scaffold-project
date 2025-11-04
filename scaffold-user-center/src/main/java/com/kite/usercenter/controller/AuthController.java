package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.AllowAnonymous;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.authenticator.service.AuthenticationService;
import com.kite.authenticator.session.Session;
import com.kite.authenticator.session.SessionManager;
import com.kite.authenticator.util.JwtUtils;
import com.kite.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * @author yourname
 */
@Tag(name = "认证", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
@AllowAnonymous
public class AuthController {
    
    @Autowired(required = false)
    private AuthenticationService authenticationService;
    
    @Autowired(required = false)
    private SessionManager sessionManager;
    
    @Value("${kite.auth.secret}")
    private String jwtSecret;
    
    @Value("${kite.auth.expire-time:604800000}")
    private Long expireTime;
    
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password,
            HttpServletRequest request) {
        
        if (authenticationService == null) {
            // 如果没有实现 AuthenticationService，返回错误提示
            return Result.fail("认证服务未实现，请实现 AuthenticationService 接口");
        }
        
        // 验证用户名密码
        LoginUser loginUser = authenticationService.authenticate(username, password);
        if (loginUser == null) {
            return Result.fail("用户名或密码错误");
        }
        
        // 设置过期时间
        loginUser.setExpireAt(System.currentTimeMillis() + expireTime);
        
        // 提取设备ID
        String deviceId = extractDeviceId(request);
        
        String sessionKey = null;
        
        // 如果启用了 Session，创建 Session
        if (sessionManager != null) {
            Session session = sessionManager.createSession(loginUser, deviceId, expireTime);
            sessionKey = session.getSessionKey();
        }
        
        // 生成 Token（包含 sessionKey）
        String token = JwtUtils.generateToken(loginUser, jwtSecret, expireTime, sessionKey);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expireTime", loginUser.getExpireAt());
        result.put("user", loginUser);
        if (sessionKey != null) {
            result.put("sessionKey", sessionKey);
        }
        
        return Result.success(result);
    }
    
    @Operation(summary = "获取当前用户信息", description = "根据 Token 获取当前登录用户信息")
    @GetMapping("/current")
    public Result<LoginUser> getCurrentUser() {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null) {
            return Result.fail("用户未登录");
        }
        return Result.success(loginUser);
    }
    
    @Operation(summary = "用户登出", description = "退出登录")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        if (loginUser != null && sessionManager != null) {
            // 从 Token 中提取 sessionKey
            String token = extractToken(request);
            if (token != null) {
                String sessionKey = JwtUtils.extractSessionKey(token, jwtSecret);
                if (sessionKey != null) {
                    Session session = sessionManager.getSession(sessionKey);
                    if (session != null) {
                        sessionManager.deleteSession(session);
                    }
                }
            }
        }
        return Result.success("登出成功");
    }
    
    /**
     * 提取设备ID
     */
    private String extractDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isEmpty()) {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && !userAgent.isEmpty()) {
                deviceId = userAgent.hashCode() + "";
            } else {
                deviceId = request.getRemoteAddr();
            }
        }
        return deviceId;
    }
    
    /**
     * 提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}


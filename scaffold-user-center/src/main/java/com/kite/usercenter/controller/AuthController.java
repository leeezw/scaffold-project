package com.kite.usercenter.controller;

import com.kite.authenticator.AuthenticationInfo;
import com.kite.authenticator.Authenticator;
import com.kite.authenticator.annotation.AllowAnonymous;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.service.AuthenticationService;
import com.kite.authenticator.service.SessionManagementService;
import com.kite.authenticator.service.TokenBlacklistService;
import com.kite.authenticator.util.JwtUtils;
import com.kite.common.response.Result;
import com.kite.usercenter.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
    private Authenticator authenticator;
    
    @Autowired(required = false)
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired(required = false)
    private SessionManagementService sessionManagementService;
    
    @Autowired(required = false)
    private com.kite.authenticator.config.AuthenticatorProperties authenticatorProperties;
    
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest,
                                             HttpServletRequest request) {
        
        if (authenticationService == null) {
            return Result.fail("认证服务未实现，请实现 AuthenticationService 接口");
        }
        
        if (authenticator == null) {
            return Result.fail("认证器未配置");
        }
        
        // 验证用户名密码
        LoginUser loginUser = authenticationService.authenticate(
                loginRequest.getUsername(), loginRequest.getPassword());
        if (loginUser == null) {
            return Result.fail("用户名或密码错误");
        }
        
        // 提取设备ID
        String deviceId = extractDeviceId(request);
        
        // 创建认证信息
        AuthenticationInfo authenticationInfo = new AuthenticationInfo() {
            @Override
            public LoginUser getUser() {
                return loginUser;
            }
            
            @Override
            public String getCredential() {
                return deviceId;  // 使用 deviceId 作为 credential
            }
        };
        
        // 调用 Authenticator 进行登录
        String token = authenticator.login(authenticationInfo);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expireTime", loginUser.getExpireAt());
        result.put("user", loginUser);
        
        return Result.success(result);
    }
    
    @Operation(summary = "获取当前用户信息", description = "根据 Token 获取当前登录用户信息")
    @GetMapping("/current")
    public Result<LoginUser> getCurrentUser(LoginUser loginUser) {
        // 使用参数解析器自动注入 LoginUser
        return Result.success(loginUser);
    }
    
    @Operation(summary = "用户登出", description = "退出登录，删除当前 Session（正常退出）")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        LoginUser loginUser = com.kite.authenticator.context.LoginUserContext.getLoginUser();
        if (loginUser != null) {
            // 提取当前 Token
            String token = extractToken(request);
            if (token != null && sessionManagementService != null && authenticatorProperties != null) {
                // 从 Token 中提取 sessionKey
                String sessionKey = JwtUtils.extractSessionKey(token, authenticatorProperties.getSecret());
                if (sessionKey != null && !sessionKey.isEmpty()) {
                    // 正常退出：删除 Session（不加入黑名单）
                    sessionManagementService.deleteSession(sessionKey);
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

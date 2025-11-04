package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.AllowAnonymous;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.authenticator.service.AuthenticationService;
import com.kite.authenticator.util.JwtUtils;
import com.kite.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

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
    
    @Value("${kite.auth.secret}")
    private String jwtSecret;
    
    @Value("${kite.auth.expire-time:604800000}")
    private Long expireTime;
    
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {
        
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
        
        // 生成 Token
        String token = JwtUtils.generateToken(loginUser, jwtSecret, expireTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expireTime", loginUser.getExpireAt());
        result.put("user", loginUser);
        
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
    public Result<String> logout() {
        // 登出逻辑（可以在这里实现 Token 黑名单等）
        return Result.success("登出成功");
    }
}


package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresRoles;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.authenticator.service.SessionManagementService;
import com.kite.authenticator.service.TokenBlacklistService;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Session 管理控制器
 * 
 * @author yourname
 */
@Tag(name = "Session 管理", description = "用户 Session 管理接口")
@RestController
@RequestMapping("/api/auth/session")
@RequiresRoles("admin")
public class SessionManagementController {
    
    @Autowired(required = false)
    private SessionManagementService sessionManagementService;
    
    @Autowired(required = false)
    private TokenBlacklistService tokenBlacklistService;
    
    @Operation(summary = "获取当前用户的 Session 列表", description = "获取当前登录用户的所有 Session")
    @GetMapping("/my-sessions")
    @OperationLog(module = "Session管理", operationType = "查询", description = "查询我的Session列表")
    public Result<Set<String>> getMySessions() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        Set<String> sessionKeys = sessionManagementService.getUserSessionKeys(userId);
        return Result.success(sessionKeys);
    }
    
    @Operation(summary = "获取指定用户的 Session 列表", description = "管理员查询指定用户的所有 Session")
    @GetMapping("/user/{userId}")
    @OperationLog(module = "Session管理", operationType = "查询", description = "查询用户Session列表")
    public Result<Set<String>> getUserSessions(@Parameter(description = "用户ID") @PathVariable Long userId) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        Set<String> sessionKeys = sessionManagementService.getUserSessionKeys(userId);
        return Result.success(sessionKeys);
    }
    
    @Operation(summary = "强制用户下线", description = "管理员强制指定用户下线（踢出所有设备）")
    @PostMapping("/kick-out/{userId}")
    @OperationLog(module = "Session管理", operationType = "删除", description = "强制用户下线")
    public Result<String> kickOutUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        sessionManagementService.kickOutUser(userId);
        return Result.success("用户已强制下线");
    }
    
    @Operation(summary = "踢出指定设备", description = "管理员踢出用户的指定设备")
    @PostMapping("/kick-out-device")
    @OperationLog(module = "Session管理", operationType = "删除", description = "踢出指定设备")
    public Result<String> kickOutDevice(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam String deviceId) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        sessionManagementService.kickOutDevice(userId, deviceId);
        return Result.success("设备已踢出");
    }
    
    @Operation(summary = "禁用用户", description = "管理员禁用用户（禁用所有 Session）")
    @PostMapping("/disable/{userId}")
    @OperationLog(module = "Session管理", operationType = "修改", description = "禁用用户")
    public Result<String> disableUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        sessionManagementService.disableUser(userId);
        return Result.success("用户已禁用");
    }
    
    @Operation(summary = "撤销 Token", description = "管理员撤销指定的 Token（安全事件，将 Token 加入黑名单）")
    @PostMapping("/revoke-token")
    @OperationLog(module = "Session管理", operationType = "删除", description = "撤销Token（安全事件）")
    public Result<String> revokeToken(
            @Parameter(description = "Token", required = true) @RequestParam String token,
            @Parameter(description = "撤销原因", required = false) @RequestParam(required = false) String reason) {
        if (tokenBlacklistService == null) {
            return Result.fail("Token 黑名单服务未启用");
        }
        
        // 安全事件：将 Token 加入黑名单，并记录原因
        String finalReason = reason != null && !reason.isEmpty() 
            ? reason 
            : "管理员撤销（未指定原因）";
        
        tokenBlacklistService.blacklistToken(token, finalReason);
        return Result.success("Token 已撤销（安全事件）");
    }
}


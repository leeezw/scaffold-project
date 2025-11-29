package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.common.util.PageResult;
import com.kite.usercenter.dto.*;
import com.kite.usercenter.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @Operation(summary = "用户分页查询")
    @RequiresPermissions({"user:list", "*:*:*"})
    @GetMapping("/page")
    public Result<UserPageResponse> pageUsers(UserPageRequest request) {
        return Result.success(userService.pageUsers(request));
    }
    
    @Operation(summary = "用户详情")
    @RequiresPermissions({"user:list", "*:*:*"})
    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }
    
    @Operation(summary = "创建用户")
    @RequiresPermissions({"user:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "用户管理", operationType = "新增", description = "创建用户")
    public Result<Void> createUser(@RequestBody @Validated UserCreateRequest request) {
        userService.createUser(request);
        return Result.success();
    }
    
    @Operation(summary = "更新用户")
    @RequiresPermissions({"user:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "用户管理", operationType = "修改", description = "更新用户")
    public Result<Void> updateUser(
            @PathVariable Long id,
            @RequestBody @Validated UserUpdateRequest request) {
        request.setId(id);
        userService.updateUser(request);
        return Result.success();
    }
    
    @Operation(summary = "修改用户状态")
    @RequiresPermissions({"user:update", "*:*:*"})
    @PutMapping("/status")
    @OperationLog(module = "用户管理", operationType = "修改", description = "修改用户状态")
    public Result<Void> changeStatus(@RequestBody @Validated ChangeStatusRequest request) {
        userService.changeStatus(request);
        return Result.success();
    }
    
    @Operation(summary = "重置密码")
    @RequiresPermissions({"user:reset-password", "*:*:*"})
    @PutMapping("/password")
    @OperationLog(module = "用户管理", operationType = "修改", description = "重置用户密码")
    public Result<Void> resetPassword(@RequestBody @Validated ResetPasswordRequest request) {
        userService.resetPassword(request);
        return Result.success();
    }
}

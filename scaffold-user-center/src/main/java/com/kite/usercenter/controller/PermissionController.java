package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.usercenter.dto.PermissionDTO;
import com.kite.usercenter.dto.PermissionRequest;
import com.kite.usercenter.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/permissions")
@Validated
public class PermissionController {
    
    private final PermissionService permissionService;
    
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
    @Operation(summary = "权限树")
    @RequiresPermissions({"permission:list", "*:*:*"})
    @GetMapping("/tree")
    public Result<List<PermissionDTO>> tree() {
        return Result.success(permissionService.listGrantedTreeForCurrentUser());
    }

    @Operation(summary = "创建权限/菜单")
    @RequiresPermissions({"permission:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "权限管理", operationType = "新增", description = "创建权限或菜单")
    public Result<Void> create(@RequestBody @Validated PermissionRequest request) {
        permissionService.create(request);
        return Result.success();
    }

    @Operation(summary = "更新权限/菜单")
    @RequiresPermissions({"permission:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "权限管理", operationType = "修改", description = "更新权限或菜单")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Validated PermissionRequest request) {
        permissionService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "删除权限/菜单")
    @RequiresPermissions({"permission:delete", "*:*:*"})
    @DeleteMapping("/{id}")
    @OperationLog(module = "权限管理", operationType = "删除", description = "删除权限或菜单")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }
}

package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.usercenter.dto.RoleDTO;
import com.kite.usercenter.dto.RoleRequest;
import com.kite.usercenter.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/roles")
@Validated
public class RoleController {
    
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @Operation(summary = "角色列表")
    @RequiresPermissions({"role:list", "*:*:*"})
    @GetMapping
    public Result<List<RoleDTO>> listRoles() {
        return Result.success(roleService.listAll());
    }
    
    @Operation(summary = "角色详情")
    @RequiresPermissions({"role:list", "*:*:*"})
    @GetMapping("/{id}")
    public Result<RoleDTO> getRole(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }
    
    @Operation(summary = "创建角色")
    @RequiresPermissions({"role:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "角色管理", operationType = "新增", description = "创建角色")
    public Result<Void> createRole(@RequestBody @Validated RoleRequest request) {
        roleService.create(request);
        return Result.success();
    }
    
    @Operation(summary = "更新角色")
    @RequiresPermissions({"role:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "角色管理", operationType = "修改", description = "更新角色")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody @Validated RoleRequest request) {
        roleService.update(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除角色")
    @RequiresPermissions({"role:delete", "*:*:*"})
    @DeleteMapping("/{id}")
    @OperationLog(module = "角色管理", operationType = "删除", description = "删除角色")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }
}

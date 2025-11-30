package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.usercenter.dto.TenantDTO;
import com.kite.usercenter.dto.TenantRequest;
import com.kite.usercenter.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "租户管理")
@RestController
@RequestMapping("/api/tenants")
@Validated
public class TenantController {
    
    private final TenantService tenantService;
    
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }
    
    @Operation(summary = "租户列表")
    @RequiresPermissions({"tenant:list", "*:*:*"})
    @GetMapping
    public Result<List<TenantDTO>> listTenants(
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(tenantService.list(keyword, status));
    }
    
    @Operation(summary = "租户详情")
    @RequiresPermissions({"tenant:list", "*:*:*"})
    @GetMapping("/{id}")
    public Result<TenantDTO> getTenant(@PathVariable Long id) {
        return Result.success(tenantService.getById(id));
    }
    
    @Operation(summary = "创建租户")
    @RequiresPermissions({"tenant:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "租户管理", operationType = "新增", description = "创建租户")
    public Result<Void> createTenant(@RequestBody @Validated TenantRequest request) {
        tenantService.create(request);
        return Result.success();
    }
    
    @Operation(summary = "更新租户")
    @RequiresPermissions({"tenant:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "租户管理", operationType = "修改", description = "更新租户")
    public Result<Void> updateTenant(@PathVariable Long id, @RequestBody @Validated TenantRequest request) {
        tenantService.update(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除租户")
    @RequiresPermissions({"tenant:delete", "*:*:*"})
    @DeleteMapping("/{id}")
    @OperationLog(module = "租户管理", operationType = "删除", description = "删除租户")
    public Result<Void> deleteTenant(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.success();
    }
}

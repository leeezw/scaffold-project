package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.usercenter.dto.OrganizationDTO;
import com.kite.usercenter.dto.OrganizationRequest;
import com.kite.usercenter.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "组织管理")
@RestController
@RequestMapping("/api/orgs")
@Validated
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }
    
    @Operation(summary = "组织树")
    @RequiresPermissions({"org:list", "*:*:*"})
    @GetMapping("/tree")
    public Result<List<OrganizationDTO>> tree(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        return Result.success(organizationService.tree(tenantId));
    }
    
    @Operation(summary = "新增组织/部门")
    @RequiresPermissions({"org:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "组织管理", operationType = "新增", description = "新增组织/部门")
    public Result<Void> create(@RequestBody @Validated OrganizationRequest request) {
        organizationService.create(request);
        return Result.success();
    }
    
    @Operation(summary = "修改组织/部门")
    @RequiresPermissions({"org:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "组织管理", operationType = "修改", description = "修改组织/部门")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated OrganizationRequest request) {
        organizationService.update(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除组织/部门")
    @RequiresPermissions({"org:delete", "*:*:*"})
    @DeleteMapping("/{id}")
    @OperationLog(module = "组织管理", operationType = "删除", description = "删除组织/部门")
    public Result<Void> delete(@PathVariable Long id) {
        organizationService.delete(id);
        return Result.success();
    }
}

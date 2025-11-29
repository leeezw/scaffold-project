package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.response.Result;
import com.kite.usercenter.dto.PermissionDTO;
import com.kite.usercenter.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
    @Operation(summary = "权限树")
    @RequiresPermissions({"permission:list", "*:*:*"})
    @GetMapping("/tree")
    public Result<List<PermissionDTO>> tree() {
        return Result.success(permissionService.listAll());
    }
}

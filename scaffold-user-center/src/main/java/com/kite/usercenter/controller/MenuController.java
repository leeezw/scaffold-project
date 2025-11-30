package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.usercenter.dto.MenuDTO;
import com.kite.usercenter.dto.MenuRequest;
import com.kite.usercenter.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/menus")
@Validated
public class MenuController {
    
    private final MenuService menuService;
    
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    @Operation(summary = "菜单树")
    @RequiresPermissions({"menu:list", "*:*:*"})
    @GetMapping("/tree")
    public Result<List<MenuDTO>> tree(@Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(menuService.tree(status));
    }
    
    @Operation(summary = "菜单详情")
    @RequiresPermissions({"menu:list", "*:*:*"})
    @GetMapping("/{id}")
    public Result<MenuDTO> detail(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }
    
    @Operation(summary = "新增菜单/按钮")
    @RequiresPermissions({"menu:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "菜单管理", operationType = "新增", description = "新增菜单/按钮")
    public Result<Void> create(@RequestBody @Validated MenuRequest request) {
        menuService.create(request);
        return Result.success();
    }
    
    @Operation(summary = "修改菜单/按钮")
    @RequiresPermissions({"menu:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "菜单管理", operationType = "修改", description = "修改菜单/按钮")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated MenuRequest request) {
        menuService.update(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除菜单/按钮")
    @RequiresPermissions({"menu:delete", "*:*:*"})
    @DeleteMapping("/{id}")
    @OperationLog(module = "菜单管理", operationType = "删除", description = "删除菜单/按钮")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}

package com.kite.organization.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.common.util.PageResult;
import com.kite.organization.dto.TenantDTO;
import com.kite.organization.dto.TenantRequest;
import com.kite.organization.service.TenantService;
import com.kite.organization.vo.TenantOptionVO;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "租户分页列表")
    @RequiresPermissions({"tenant:list", "*:*:*"})
    @GetMapping
    public Result<PageResult<TenantDTO>> page(@RequestParam(required = false) String keyword,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(tenantService.pageTenants(keyword, pageNum, pageSize));
    }

    @Operation(summary = "租户下拉")
    @GetMapping("/options")
    public Result<List<TenantOptionVO>> options() {
        return Result.success(tenantService.listOptions());
    }

    @Operation(summary = "当前用户可选租户")
    @GetMapping("/my-options")
    public Result<List<TenantOptionVO>> myOptions() {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null) {
            return Result.fail("未登录");
        }
        return Result.success(tenantService.listOptionsByUser(loginUser.getUserId(), loginUser.getTenantId()));
    }

    @Operation(summary = "租户详情")
    @RequiresPermissions({"tenant:list", "*:*:*"})
    @GetMapping("/{id}")
    public Result<TenantDTO> detail(@PathVariable Long id) {
        return Result.success(tenantService.getDetail(id));
    }

    @Operation(summary = "创建租户")
    @RequiresPermissions({"tenant:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "租户管理", operationType = "新增", description = "创建租户")
    public Result<Void> create(@RequestBody @Validated TenantRequest request) {
        tenantService.create(request);
        return Result.success();
    }

    @Operation(summary = "更新租户")
    @RequiresPermissions({"tenant:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "租户管理", operationType = "修改", description = "更新租户")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated TenantRequest request) {
        tenantService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "启用/停用租户")
    @RequiresPermissions({"tenant:update", "*:*:*"})
    @PatchMapping("/{id}/status")
    @OperationLog(module = "租户管理", operationType = "修改", description = "更新租户状态")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        tenantService.changeStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除租户")
    @RequiresPermissions({"tenant:delete", "*:*:*"})
    @DeleteMapping("/{id}")
    @OperationLog(module = "租户管理", operationType = "删除", description = "删除租户")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.success();
    }
}

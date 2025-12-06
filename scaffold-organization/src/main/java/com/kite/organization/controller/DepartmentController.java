package com.kite.organization.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.organization.dto.DepartmentDTO;
import com.kite.organization.dto.DepartmentRequest;
import com.kite.organization.service.DepartmentService;
import com.kite.organization.vo.DepartmentTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/departments")
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "部门树")
    @RequiresPermissions({"department:list", "*:*:*"})
    @GetMapping("/tree")
    public Result<List<DepartmentTreeVO>> tree(@RequestParam Long tenantId) {
        return Result.success(departmentService.listTree(tenantId));
    }

    @Operation(summary = "部门详情")
    @RequiresPermissions({"department:list", "*:*:*"})
    @GetMapping("/{id}")
    public Result<DepartmentDTO> detail(@PathVariable Long id) {
        return Result.success(departmentService.detail(id));
    }

    @Operation(summary = "新建部门")
    @RequiresPermissions({"department:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "部门管理", operationType = "新增", description = "新建部门")
    public Result<Void> create(@RequestBody @Validated DepartmentRequest request) {
        departmentService.create(request);
        return Result.success();
    }

    @Operation(summary = "更新部门")
    @RequiresPermissions({"department:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "部门管理", operationType = "修改", description = "更新部门")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated DepartmentRequest request) {
        departmentService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @RequiresPermissions({"department:delete", "*:*:*"})
    @DeleteMapping("/{id}")
    @OperationLog(module = "部门管理", operationType = "删除", description = "删除部门")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}

package com.kite.organization.controller;

import com.kite.authenticator.annotation.RequiresPermissions;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.organization.dto.PositionDTO;
import com.kite.organization.dto.PositionRequest;
import com.kite.organization.service.PositionService;
import com.kite.organization.vo.PositionOptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "岗位管理")
@RestController
@RequestMapping("/api/positions")
@Validated
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @Operation(summary = "岗位列表")
    @RequiresPermissions({"position:list", "*:*:*"})
    @GetMapping
    public Result<List<PositionDTO>> list(@RequestParam Long tenantId) {
        return Result.success(positionService.listByTenant(tenantId));
    }

    @Operation(summary = "岗位下拉")
    @RequiresPermissions({"position:list", "*:*:*"})
    @GetMapping("/options")
    public Result<List<PositionOptionVO>> options(@RequestParam Long tenantId) {
        return Result.success(positionService.listOptions(tenantId));
    }

    @Operation(summary = "岗位详情")
    @RequiresPermissions({"position:list", "*:*:*"})
    @GetMapping("/{id}")
    public Result<PositionDTO> detail(@PathVariable Long id) {
        return Result.success(positionService.detail(id));
    }

    @Operation(summary = "创建岗位")
    @RequiresPermissions({"position:create", "*:*:*"})
    @PostMapping
    @OperationLog(module = "岗位管理", operationType = "新增", description = "创建岗位")
    public Result<Void> create(@RequestBody @Validated PositionRequest request) {
        positionService.create(request);
        return Result.success();
    }

    @Operation(summary = "更新岗位")
    @RequiresPermissions({"position:update", "*:*:*"})
    @PutMapping("/{id}")
    @OperationLog(module = "岗位管理", operationType = "修改", description = "更新岗位")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated PositionRequest request) {
        positionService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "删除岗位")
    @RequiresPermissions({"position:delete", "*:*:*"})
    @DeleteMapping("/{id}")
    @OperationLog(module = "岗位管理", operationType = "删除", description = "删除岗位")
    public Result<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return Result.success();
    }
}

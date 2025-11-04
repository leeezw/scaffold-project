package com.kite.usercenter.controller;

import com.kite.common.annotation.OperationLog;
import com.kite.usercenter.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志查询接口
 * 
 * @author yourname
 */
@Tag(name = "操作日志", description = "操作日志查询和管理接口")
@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Operation(summary = "查询操作日志", description = "分页查询操作日志")
    @GetMapping("/page")
    @OperationLog(module = "操作日志", operationType = "查询", description = "分页查询操作日志")
    public Map<String, Object> getPage(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "操作模块") @RequestParam(required = false) String module,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", required = true) @RequestParam(defaultValue = "10") Integer pageSize) {
        
        List<com.kite.usercenter.entity.OperationLog> list = operationLogService.getPage(userId, module, operationType, startTime, endTime, pageNum, pageSize);
        long total = operationLogService.getCount(userId, module, operationType, startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return result;
    }
    
    @Operation(summary = "查询操作日志详情", description = "根据ID查询操作日志详情")
    @GetMapping("/{id}")
    @OperationLog(module = "操作日志", operationType = "查询", description = "查询操作日志详情")
    public Map<String, Object> getById(@Parameter(description = "日志ID") @PathVariable Long id) {
        com.kite.usercenter.entity.OperationLog log = operationLogService.getById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", log);
        
        return result;
    }
    
    @Operation(summary = "删除过期日志", description = "删除指定时间之前的日志")
    @DeleteMapping("/clean")
    @OperationLog(module = "操作日志", operationType = "删除", description = "删除过期日志")
    public Map<String, Object> clean(
            @Parameter(description = "删除此时间之前的日志", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime time) {
        
        int count = operationLogService.deleteBeforeTime(time);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", count);
        
        return result;
    }
}


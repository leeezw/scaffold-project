package com.kite.usercenter.controller;

import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello 测试控制器
 * 用于测试 Swagger 文档
 * 
 * @author yourname
 */
@Tag(name = "Hello", description = "Hello 测试接口")
@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @Operation(summary = "Hello 接口", description = "返回一个简单的 Hello 消息")
    @OperationLog(module = "Hello", operationType = "查询", description = "Hello 接口测试")
    @GetMapping
    public Result<String> hello() {
        return Result.success("欢迎使用开发脚手架");
    }

    @Operation(summary = "获取用户信息", description = "返回示例用户信息")
    @GetMapping("/user")
    public Result<Map<String, Object>> getUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", "admin");
        user.put("email", "admin@example.com");
        
        return Result.success(user);
    }
}


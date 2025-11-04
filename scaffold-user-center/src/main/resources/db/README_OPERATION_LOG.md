# 操作日志系统使用说明

## 一、系统概述

操作日志系统用于自动记录系统中的所有操作，包括：
- 操作人信息（用户ID、用户名）
- 操作信息（模块、操作类型、描述）
- 请求信息（URL、方法、参数、IP地址）
- 响应信息（结果、执行时间、状态）
- 错误信息（失败时的错误消息）

## 二、数据库表

执行 SQL 文件创建日志表：

```sql
-- 执行以下文件中的 SQL
scaffold-user-center/src/main/resources/db/operation_log.sql
```

## 三、使用方式

### 1. 在 Controller 方法上添加注解

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @OperationLog(module = "用户管理", operationType = "新增", description = "创建用户")
    @PostMapping
    public Result createUser(@RequestBody UserCreateDTO dto) {
        // 业务逻辑
    }
    
    @OperationLog(module = "用户管理", operationType = "查询", description = "查询用户列表", 
                  recordParams = false, recordResult = false)
    @GetMapping("/list")
    public Result getUserList() {
        // 业务逻辑
    }
}
```

### 2. 注解参数说明

- `module`: 操作模块（如：用户管理、订单管理）
- `operationType`: 操作类型（如：新增、删除、修改、查询）
- `description`: 操作描述
- `recordParams`: 是否记录请求参数（默认 true）
- `recordResult`: 是否记录响应结果（默认 true）

### 3. 设置用户上下文

在拦截器或过滤器中设置用户信息：

```java
// 在认证成功后设置
OperationLogContext context = OperationLogContext.create(userId, username);
OperationLogContext.setContext(context);

// 在请求结束后清理
try {
    // 处理请求
} finally {
    OperationLogContext.clear();
}
```

## 四、查询接口

### 1. 分页查询日志

```
GET /api/operation-log/page
参数：
- userId: 用户ID（可选）
- module: 操作模块（可选）
- operationType: 操作类型（可选）
- startTime: 开始时间（可选，格式：yyyy-MM-dd HH:mm:ss）
- endTime: 结束时间（可选，格式：yyyy-MM-dd HH:mm:ss）
- pageNum: 页码（默认 1）
- pageSize: 每页数量（默认 10）
```

### 2. 查询日志详情

```
GET /api/operation-log/{id}
参数：
- id: 日志ID
```

### 3. 清理过期日志

```
DELETE /api/operation-log/clean?time=2024-01-01 00:00:00
参数：
- time: 删除此时间之前的日志
```

## 五、特性

1. **自动记录**：通过 AOP 自动拦截，无需手动编写日志代码
2. **异步保存**：日志异步保存，不影响主业务流程性能
3. **敏感信息过滤**：自动过滤密码等敏感参数
4. **数据长度限制**：自动截断过长的参数和结果
5. **完整信息记录**：记录 IP、User-Agent、执行时间等完整信息

## 六、注意事项

1. 需要在数据库执行建表 SQL
2. 需要在拦截器中设置用户上下文（认证后）
3. 日志会自动异步保存，不会阻塞业务
4. 建议定期清理过期日志，避免数据表过大


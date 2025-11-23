# Token 黑名单功能说明

## 📋 概述

Token 黑名单功能用于**立即撤销已泄露或需要失效的 Token**，即使 Token 本身还未过期，也能通过黑名单机制使其立即失效。

## ✨ 功能特性

- ✅ **立即撤销**：Token 加入黑名单后立即失效
- ✅ **自动过期**：黑名单中的 Token 会自动过期（使用 Token 本身的过期时间）
- ✅ **分布式支持**：基于 Redis，支持分布式部署
- ✅ **安全存储**：使用 Token 的 SHA-256 哈希值存储，不存储完整 Token
- ✅ **自动集成**：已集成到认证流程中，无需额外配置

## 🔧 工作原理

### 1. Token 哈希

为了避免在 Redis 中存储完整的 Token（可能很长），系统使用 SHA-256 算法对 Token 进行哈希：

```
Token → SHA-256 → 哈希值 → Redis Key: "authc:blacklist:{hash}"
```

### 2. 黑名单检查流程

```
请求到达 → 提取 Token → 计算哈希值 → 查询 Redis → 
存在 → 拒绝请求（Token 已被撤销）
不存在 → 继续认证流程
```

### 3. 自动过期

黑名单中的 Token 会自动过期：
- 如果指定了过期时间，使用指定时间
- 如果未指定，自动从 Token 中提取过期时间
- 过期后自动从 Redis 中删除

## 📝 使用方法

### 1. 用户登出（自动撤销 Token）

用户登出时，当前 Token 会自动加入黑名单：

```java
POST /api/auth/logout
Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "message": "登出成功"
}
```

### 2. 管理员撤销指定 Token

管理员可以撤销任何 Token：

```java
POST /api/auth/session/revoke-token?token={token}
Authorization: Bearer {admin_token}

// 响应
{
  "code": 200,
  "message": "Token 已撤销"
}
```

### 3. 代码中使用

#### 撤销 Token

```java
@Autowired
private TokenBlacklistService tokenBlacklistService;

// 撤销 Token（使用 Token 本身的过期时间）
tokenBlacklistService.blacklistToken(token);

// 撤销 Token（指定过期时间，单位：毫秒）
tokenBlacklistService.blacklistToken(token, 3600000L); // 1小时后过期
```

#### 检查 Token 是否在黑名单中

```java
boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
if (isBlacklisted) {
    // Token 已被撤销
}
```

#### 从黑名单中移除（提前解除）

```java
tokenBlacklistService.removeFromBlacklist(token);
```

## 🔒 安全场景示例

### 场景 1：Token 泄露

**问题**：用户发现自己的 Token 被泄露了

**解决方案**：
```java
// 1. 用户立即登出（撤销当前 Token）
POST /api/auth/logout

// 2. 管理员撤销泄露的 Token（如果知道 Token）
POST /api/auth/session/revoke-token?token={泄露的token}

// 3. 强制用户下线（撤销所有 Token）
POST /api/auth/session/kick-out/{userId}
```

### 场景 2：异常行为检测

**问题**：检测到异常登录行为

**解决方案**：
```java
// 检测到异常行为后，立即撤销 Token
if (detectAbnormalBehavior(token, ip)) {
    tokenBlacklistService.blacklistToken(token);
    logSecurityEvent("异常行为检测", token, ip);
}
```

### 场景 3：密码修改

**问题**：用户修改密码后，应该撤销所有旧 Token

**解决方案**：
```java
// 修改密码后
public void changePassword(Long userId, String newPassword) {
    // 1. 更新密码
    userService.updatePassword(userId, newPassword);
    
    // 2. 强制用户下线（撤销所有 Session）
    sessionManagementService.kickOutUser(userId);
    
    // 注意：Session 状态管理会自动处理 Token 失效
    // 但也可以额外加入黑名单，双重保障
}
```

## 📊 Redis 存储结构

### Key 格式

```
authc:blacklist:{token_hash}
```

### Value

```
"1"  // 固定值，表示已加入黑名单
```

### TTL

自动设置，等于 Token 的剩余过期时间

### 示例

```
Key: authc:blacklist:a1b2c3d4e5f6...
Value: "1"
TTL: 604800000 (7天)
```

## ⚙️ 配置说明

Token 黑名单功能**无需额外配置**，只要满足以下条件即可自动启用：

1. ✅ Redis 已配置并可用
2. ✅ `scaffold-authenticator-starter` 已引入
3. ✅ 认证功能已启用（`kite.auth.enabled=true`）

## 🔍 认证流程中的位置

Token 黑名单检查在认证流程的**最前面**执行：

```
1. 提取 Token
2. 【黑名单检查】← 在这里检查
3. Realm 获取认证信息
4. Signature 验证 Token
5. Session 验证
6. 设置用户上下文
```

这样可以：
- ✅ 避免无效的 Token 验证（节省资源）
- ✅ 立即拒绝已撤销的 Token
- ✅ 提高安全性

## 📈 性能考虑

### Redis 查询性能

- **查询速度**：O(1) 时间复杂度
- **存储大小**：每个黑名单条目约 50-60 字节
- **内存占用**：非常小（只存储哈希值）

### 优化建议

1. **合理设置过期时间**：不要设置过长的过期时间
2. **定期清理**：Redis 会自动清理过期的 key
3. **监控 Redis 内存**：定期检查黑名单数量

## 🛠️ 故障排查

### 问题 1：黑名单不生效

**可能原因**：
- Redis 未配置或连接失败
- TokenBlacklistService 未正确注入

**解决方案**：
```java
// 检查 Redis 连接
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// 检查 TokenBlacklistService
@Autowired(required = false)
private TokenBlacklistService tokenBlacklistService;
```

### 问题 2：Token 撤销后仍能使用

**可能原因**：
- Token 哈希计算错误
- Redis 中的 key 已过期

**解决方案**：
- 检查 Redis 中是否存在对应的 key
- 检查 Token 是否已过期（黑名单会自动过期）

### 问题 3：内存占用过高

**可能原因**：
- 大量 Token 被加入黑名单
- 过期时间设置过长

**解决方案**：
- 缩短 Token 过期时间
- 定期清理 Redis
- 监控黑名单数量

## 📚 相关接口

### 用户接口

- `POST /api/auth/logout` - 登出（自动撤销当前 Token）

### 管理员接口

- `POST /api/auth/session/revoke-token` - 撤销指定 Token
- `POST /api/auth/session/kick-out/{userId}` - 强制用户下线（撤销所有 Token）
- `POST /api/auth/session/kick-out-device` - 踢出指定设备

## 💡 最佳实践

1. **用户登出时自动撤销**：已在 `AuthController.logout()` 中实现
2. **密码修改后撤销所有 Token**：通过 Session 管理实现
3. **异常检测后立即撤销**：在安全监控模块中实现
4. **定期清理**：依赖 Redis 的自动过期机制
5. **监控告警**：监控黑名单数量，异常时告警

## 🔐 安全建议

1. **HTTPS 传输**：确保 Token 在传输过程中加密
2. **合理过期时间**：不要设置过长的 Token 过期时间
3. **及时撤销**：发现泄露后立即撤销
4. **监控异常**：监控黑名单使用情况，发现异常及时处理
5. **日志记录**：记录所有 Token 撤销操作，便于审计

## 📝 总结

Token 黑名单功能提供了**立即撤销 Token**的能力，是对 Session 状态管理的有效补充：

- **Session 状态管理**：适用于批量操作（如强制下线）
- **Token 黑名单**：适用于精确控制（如撤销单个 Token）

两者结合使用，可以提供更强大的安全控制能力。


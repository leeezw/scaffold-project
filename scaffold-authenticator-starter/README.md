# Kite 认证框架使用手册

## 目录

- [概述](#概述)
- [架构设计](#架构设计)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [核心组件](#核心组件)
- [使用指南](#使用指南)
- [事件通知机制](#事件通知机制)
- [参数解析器](#参数解析器)
- [自定义扩展](#自定义扩展)
- [Session 管理](#session-管理)
- [常见问题](#常见问题)

---

## 概述

`scaffold-authenticator-starter` 是一个基于 Spring Boot 的企业级认证框架 Starter，提供了完整的认证、授权、Session 管理功能。

### 特性

- ✅ **JWT Token 认证**：基于 JWT 的无状态/有状态认证
- ✅ **Session 管理**：支持 Session 存储、续期、超时、状态管理（可选）
- ✅ **登录用户上下文**：通过 `LoginUserContext` 或参数解析器获取当前登录用户
- ✅ **权限控制**：基于角色的访问控制（RBAC）
- ✅ **事件通知机制**：支持登录、登出等事件通知
- ✅ **参数解析器**：自动注入 `LoginUser` 到 Controller 方法参数
- ✅ **优雅的架构**：基于 Realm、Signature、Authenticator 的抽象设计
- ✅ **自动配置**：Spring Boot Starter，开箱即用
- ✅ **灵活扩展**：支持自定义 Realm、Signature、通知器等

### 架构设计

本框架采用了分层抽象的设计理念，参考了 `sparrow-authenticator` 的优雅架构：

```
┌─────────────────────────────────────────────────────────┐
│                    AuthenticationFilter                  │
│              (请求拦截和 Token 提取)                      │
└────────────────────┬──────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│                    Authenticator                          │
│              (认证器核心接口)                              │
└──────┬──────────────────────────────┬───────────────────┘
       │                              │
       ▼                              ▼
┌──────────────┐            ┌──────────────────┐
│    Realm     │            │    Signature     │
│ (认证域)     │            │   (签名器)        │
└──────────────┘            └──────────────────┘
       │                              │
       ▼                              ▼
┌──────────────┐            ┌──────────────────┐
│ UserRealm    │            │ JwtHmacSignature │
│ EmptyRealm   │            │                  │
└──────────────┘            └──────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│              AuthenticationService (业务层)                │
│         (用户需要实现的业务认证逻辑)                        │
└─────────────────────────────────────────────────────────┘
```

### 核心设计理念

1. **分层抽象**：Filter → Authenticator → Realm/Signature → Business Service
2. **职责分离**：认证、授权、Session 管理各自独立
3. **事件驱动**：通过事件通知机制解耦业务逻辑
4. **可扩展性**：所有核心组件都支持自定义实现

---

## 快速开始

### 1. 添加依赖

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.kite</groupId>
    <artifactId>scaffold-authenticator-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**注意**：如果使用 Session 管理功能，还需要确保项目中包含 Redis 依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中配置：

```yaml
kite:
  auth:
    enabled: true                    # 是否启用认证（默认：true）
    token-header: Authorization       # Token Header 名称（默认：Authorization）
    token-prefix: Bearer              # Token 前缀（默认：Bearer ）
    secret: your-secret-key-here      # JWT 密钥（必填，请修改为安全的密钥，至少32位）
    expire-time: 604800000            # Token 过期时间（毫秒，默认：7天）
    mock-enabled: false               # 是否启用 Mock 用户（开发环境使用，默认：false）
    mock-user-id: 1                   # Mock 用户ID（仅在 mockEnabled=true 时生效）
    mock-username: mock-user          # Mock 用户名（仅在 mockEnabled=true 时生效）
    check-permission: true           # 是否验证权限（默认：true）
    exclude-paths:                    # 排除路径（不需要认证的路径，支持 Ant 模式）
      - /api/auth/**
      - /api/public/**
      - /doc.html
      - /swagger-ui/**
    session:                          # Session 配置（可选）
      enabled: true                   # 是否启用 Session 管理（默认：true）
      validate-device: true           # 是否验证设备（默认：true）
      validate-status: true           # 是否验证用户状态（默认：true）
      renewal: true                   # 是否启用 Session 续期（默认：true）
      timeout: 1800000                # Session 超时时间（毫秒，默认：30分钟）
      renewal-interval: 604800000     # Session 续期间隔（毫秒，默认：7天）
```

### 3. 实现 AuthenticationService

创建一个实现类，实现 `AuthenticationService` 接口：

```java
package com.example.service;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthenticationService implements AuthenticationService {
    
    @Autowired
    private UserService userService;
    
    @Override
    public LoginUser authenticate(String username, String password) {
        // 1. 查询用户
        User user = userService.findByUsername(username);
        if (user == null) {
            return null;
        }
        
        // 2. 验证密码（请使用安全的密码验证方式，如 BCrypt）
        if (!passwordMatches(password, user.getPassword())) {
            return null;
        }
        
        // 3. 检查用户状态（如是否被禁用）
        if (user.getStatus() != UserStatus.ACTIVE) {
            return null;
        }
        
        // 4. 构建 LoginUser
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        
        // 5. 设置角色和权限（从数据库或缓存中获取）
        loginUser.setRoles(userService.getRoles(user.getId()));
        loginUser.setPermissions(userService.getPermissions(user.getId()));
        
        return loginUser;
    }
    
    @Override
    public LoginUser getUserById(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return null;
        }
        
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setRoles(userService.getRoles(user.getId()));
        loginUser.setPermissions(userService.getPermissions(user.getId()));
        
        return loginUser;
    }
    
    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        // 使用 BCrypt 或其他加密方式验证密码
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
```

### 4. 创建登录接口

```java
package com.example.controller;

import com.kite.authenticator.AuthenticationInfo;
import com.kite.authenticator.Authenticator;
import com.kite.authenticator.annotation.AllowAnonymous;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.service.AuthenticationService;
import com.kite.common.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllowAnonymous
public class AuthController {
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private Authenticator authenticator;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request) {
        
        // 1. 验证用户名密码
        LoginUser loginUser = authenticationService.authenticate(username, password);
        if (loginUser == null) {
            return Result.fail("用户名或密码错误");
        }
        
        // 2. 提取设备ID
        String deviceId = extractDeviceId(request);
        
        // 3. 创建认证信息
        AuthenticationInfo authenticationInfo = new AuthenticationInfo() {
            @Override
            public LoginUser getUser() {
                return loginUser;
            }
            
            @Override
            public String getCredential() {
                return deviceId;  // 将 deviceId 存储在 credential 中
            }
        };
        
        // 4. 调用 Authenticator 进行登录（生成 Token 和 Session）
        String token = authenticator.login(authenticationInfo);
        
        // 5. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expireTime", loginUser.getExpireAt());
        result.put("user", loginUser);
        
        return Result.success(result);
    }
    
    /**
     * 获取当前用户信息（使用参数解析器）
     */
    @GetMapping("/current")
    public Result<LoginUser> getCurrentUser(LoginUser loginUser) {
        // LoginUser 会自动通过参数解析器注入
        return Result.success(loginUser);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        // TODO: 可以通过事件通知机制实现登出逻辑
        return Result.success("登出成功");
    }
    
    /**
     * 提取设备ID
     */
    private String extractDeviceId(HttpServletRequest request) {
        // 优先从 Header 中获取
        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId != null && !deviceId.isEmpty()) {
            return deviceId;
        }
        
        // 从 User-Agent 中提取
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.isEmpty()) {
            return String.valueOf(userAgent.hashCode());
        }
        
        // 使用 IP 地址作为设备ID
        return request.getRemoteAddr();
    }
}
```

### 5. （可选）同步自定义上下文

若你的项目还需要将登录信息同步到其他上下文（例如审计、操作日志、租户信息等），可以实现 `LoginUserContextCustomizer` 接口：

```java
@Component
public class AuditContextCustomizer implements LoginUserContextCustomizer {

    @Override
    public void onLoginUserSet(LoginUser loginUser) {
        AuditContext.set(loginUser.getUserId(), loginUser.getUsername());
    }

    @Override
    public void onLoginUserCleared() {
        AuditContext.clear();
    }
}
```

`AuthenticationFilter` 会在每次请求认证成功/结束后调用这些自定义器，第三方系统无需修改框架源码即可扩展。

### 5. 使用认证功能

在 Controller 中使用：

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    /**
     * 方式1：使用参数解析器（推荐）
     */
    @GetMapping("/profile")
    public Result<LoginUser> getProfile(LoginUser loginUser) {
        // loginUser 会自动注入，无需手动获取
        return Result.success(loginUser);
    }
    
    /**
     * 方式2：使用 LoginUserContext
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null) {
            return Result.fail("用户未登录");
        }
        
        Map<String, Object> info = new HashMap<>();
        info.put("userId", loginUser.getUserId());
        info.put("username", loginUser.getUsername());
        return Result.success(info);
    }
}
```

---

## 配置说明

### 完整配置项

```yaml
kite:
  auth:
    # 基础配置
    enabled: true                      # 是否启用认证（默认：true）
    token-header: Authorization         # Token Header 名称（默认：Authorization）
    token-prefix: Bearer               # Token 前缀（默认：Bearer ）
    secret: your-secret-key            # JWT 密钥（必填）
    expire-time: 604800000             # Token 过期时间（毫秒，默认：7天）
    
    # Mock 配置（开发环境使用）
    mock-enabled: false                # 是否启用 Mock 用户（默认：false）
    mock-user-id: 1                    # Mock 用户ID
    mock-username: mock-user           # Mock 用户名
    
    # 权限配置
    check-permission: true             # 是否验证权限（默认：true）
    
    # 排除路径配置
    exclude-paths:                     # 排除路径列表（支持 Ant 模式）
      - /api/auth/**
      - /api/public/**
      - /doc.html
      - /swagger-ui/**
    
    # Session 配置
    session:
      enabled: true                    # 是否启用 Session 管理（默认：true）
      validate-device: true            # 是否验证设备（默认：true）
      validate-status: true           # 是否验证用户状态（默认：true）
      renewal: true                    # 是否启用 Session 续期（默认：true）
      timeout: 1800000                 # Session 超时时间（毫秒，默认：30分钟）
      renewal-interval: 604800000      # Session 续期间隔（毫秒，默认：7天）
```

### 配置说明

#### 基础配置

- **enabled**：是否启用认证框架。设置为 `false` 时，框架将不会拦截请求。
- **token-header**：Token 在 HTTP Header 中的名称，默认为 `Authorization`。
- **token-prefix**：Token 的前缀，默认为 `Bearer `（注意有空格）。
- **secret**：JWT 签名密钥，**必须修改为安全的密钥**（建议至少 32 位随机字符串）。
- **expire-time**：Token 的过期时间（毫秒），默认 7 天。

#### Mock 配置

在开发环境中，可以启用 Mock 模式，跳过认证直接使用 Mock 用户：

```yaml
kite:
  auth:
    mock-enabled: true
    mock-user-id: 1
    mock-username: admin
```

启用后，所有请求都会使用 Mock 用户，无需提供 Token。

#### 排除路径配置

配置不需要认证的路径，支持 Ant 模式：

```yaml
kite:
  auth:
    exclude-paths:
      - /api/auth/**      # 认证相关接口
      - /api/public/**    # 公开接口
      - /doc.html        # Swagger 文档
      - /swagger-ui/**    # Swagger UI
```

#### Session 配置

- **enabled**：是否启用 Session 管理。设置为 `false` 时，将使用纯 JWT 模式。
- **validate-device**：是否验证设备ID。启用后，会验证请求的设备ID是否与 Session 中的设备ID匹配。
- **validate-status**：是否验证用户状态。启用后，会检查 Session 状态（如被踢出、被禁用等）。
- **renewal**：是否启用 Session 续期。启用后，每次访问时会自动续期。
- **timeout**：Session 超时时间。超过此时间未访问，需要重新登录。
- **renewal-interval**：Session 续期间隔。每次续期时，会将过期时间延长至此值。

---

## 核心组件

### 1. Authenticator（认证器）

认证器的核心接口，负责登录和认证：

```java
public interface Authenticator {
    /**
     * 登录（生成 Token）
     */
    String login(AuthenticationInfo authenticationInfo);
    
    /**
     * 认证（验证 Token）
     */
    LoginUser authenticate(HostAuthenticationToken token);
}
```

**默认实现**：`DefaultSecurityManager`

### 2. Realm（认证域）

负责从系统中获取用户认证信息：

```java
public interface Realm {
    /**
     * 判断是否支持该类型的 Token
     */
    boolean support(AuthenticationToken token);
    
    /**
     * 获取认证信息
     */
    AuthenticationInfo getAuthenticationInfo(AuthenticationToken token);
}
```

**默认实现**：
- `UserRealm`：从业务层获取用户信息
- `EmptyRealm`：空实现（当没有 AuthenticationService 时使用）

### 3. Signature（签名器）

负责 Token 的生成和验证：

```java
public interface Signature {
    /**
     * 生成签名（Token）
     */
    String sign(LoginUser loginUser, String key);
    
    /**
     * 验证签名并解析用户信息
     */
    LoginUser verify(String token, String key);
}
```

**默认实现**：`JwtHmacSignature`（使用 HMAC-SHA256 算法）

### 4. AuthenticationToken（认证令牌）

表示用户提交的认证信息：

```java
public interface AuthenticationToken {
    Object getPrincipal();  // 主体（如用户名、用户ID）
    String getCredential(); // 凭证（如密码、Token）
}
```

**实现类**：`HostAuthenticationToken`（包含设备信息）

### 5. AuthenticationInfo（认证信息）

表示从系统中获取的已认证用户信息：

```java
public interface AuthenticationInfo {
    LoginUser getUser();      // 登录用户信息
    String getCredential();   // 凭证（用于签名验证）
}
```

### 6. LoginUser（登录用户）

登录用户信息类：

```java
public class LoginUser {
    private Long userId;           // 用户ID
    private String username;       // 用户名
    private String nickname;       // 昵称
    private List<String> roles;    // 角色列表
    private List<String> permissions; // 权限列表
    private Long expireAt;         // Token 过期时间
}
```

### 7. LoginUserContext（登录用户上下文）

提供静态方法获取当前登录用户：

```java
public class LoginUserContext {
    public static LoginUser getLoginUser();
    public static Long getUserId();
    public static String getUsername();
    public static boolean isLogin();
    public static void clear();
}
```

---

## 使用指南

### 1. 获取当前登录用户

#### 方式1：使用参数解析器（推荐）

```java
@GetMapping("/profile")
public Result<LoginUser> getProfile(LoginUser loginUser) {
    // loginUser 会自动注入
    return Result.success(loginUser);
}
```

#### 方式2：使用 LoginUserContext

```java
@GetMapping("/profile")
public Result<LoginUser> getProfile() {
    LoginUser loginUser = LoginUserContext.getLoginUser();
    return Result.success(loginUser);
}
```

### 2. 权限控制

#### 使用角色注解

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    /**
     * 需要 admin 角色
     */
    @RequiresRoles("admin")
    @GetMapping("/users")
    public Result<List<User>> getUsers() {
        // ...
    }
    
    /**
     * 需要 admin 或 manager 角色（任一即可）
     */
    @RequiresRoles(value = {"admin", "manager"}, logical = false)
    @GetMapping("/managers")
    public Result<List<User>> getManagers() {
        // ...
    }
    
    /**
     * 需要 admin 和 manager 角色（同时满足）
     */
    @RequiresRoles(value = {"admin", "manager"}, logical = true)
    @GetMapping("/super-admins")
    public Result<List<User>> getSuperAdmins() {
        // ...
    }
}
```

#### 使用权限注解

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    /**
     * 需要 user:delete 权限
     */
    @RequiresPermissions("user:delete")
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        // ...
    }
    
    /**
     * 需要 user:read 或 user:write 权限（任一即可）
     */
    @RequiresPermissions(value = {"user:read", "user:write"}, logical = false)
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        // ...
    }
}
```

#### 组合使用

```java
/**
 * 需要 admin 角色或 user:read 权限（其中一个即可）
 */
@RequiresRoles(value = {"admin"}, logical = false)
@RequiresPermissions("user:read")
@GetMapping("/{id}")
public Result<User> getUser(@PathVariable Long id) {
    // ...
}
```

### 3. 允许匿名访问

使用 `@AllowAnonymous` 注解标记不需要认证的接口：

```java
@RestController
@RequestMapping("/api/public")
public class PublicController {
    
    @AllowAnonymous
    @GetMapping("/info")
    public Result<String> getInfo() {
        return Result.success("公开信息");
    }
}
```

### 4. API 调用示例

#### 登录

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=123456"
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJleHAiOjE3MDQwNjcyMDB9...",
    "expireTime": 1704067200000,
    "user": {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "roles": ["admin"],
      "permissions": ["user:read", "user:write"]
    }
  }
}
```

#### 使用 Token 访问受保护接口

```bash
curl -X GET "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### 传递设备ID

```bash
curl -X GET "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "X-Device-Id: device-123456"
```

---

## 事件通知机制

框架提供了事件通知机制，可以在登录、登出等操作时触发事件，实现业务逻辑的解耦。

### 事件类型

```java
public enum AuthcEventType implements EventType {
    LOGIN(1),      // 登录事件
    LOGOUT(2),     // 登出事件
    AUTHENTICATE(3); // 认证事件
}
```

### 实现通知器

继承 `AbstractNotifier` 并实现 `notify` 方法：

```java
package com.example.notifier;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.notifier.AbstractNotifier;
import com.kite.authenticator.notifier.AuthcEventType;
import com.kite.authenticator.notifier.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 登录通知器
 */
@Slf4j
@Component
public class LoginAuditNotifier extends AbstractNotifier<LoginUser> {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private UserService userService;
    
    @Override
    public String getType() {
        return AuthcEventType.LOGIN.name();
    }
    
    @Override
    public void notify(Event<LoginUser> event) {
        LoginUser loginUser = event.getBody();
        if (loginUser != null) {
            // 1. 记录登录日志
            auditLogService.recordLogin(loginUser.getUserId(), loginUser.getUsername());
            
            // 2. 更新最后登录时间
            userService.updateLastLoginTime(loginUser.getUserId());
            
            // 3. 发送登录通知（如邮件、短信等）
            notificationService.sendLoginNotification(loginUser);
            
            log.info("用户登录成功: userId={}, username={}", 
                loginUser.getUserId(), loginUser.getUsername());
        }
    }
}
```

### 注册通知器

通知器会自动注册到 `NotifyRegistry`，无需手动配置。只需要：

1. 实现 `AbstractNotifier` 接口
2. 使用 `@Component` 注解标记为 Spring Bean
3. 实现 `getType()` 方法返回事件类型名称
4. 实现 `notify()` 方法处理事件

### 手动触发事件

如果需要手动触发事件：

```java
@Autowired
private NotifyRegistry notifyRegistry;

public void triggerCustomEvent() {
    Notifier<LoginUser> notifier = notifyRegistry.get(AuthcEventType.LOGIN.name());
    if (notifier != null) {
        LoginEvent event = new LoginEvent(loginUser);
        notifier.notify(event);
    }
}
```

---

## 参数解析器

框架提供了 `LoginUserArgumentResolver`，可以自动将 `LoginUser` 注入到 Controller 方法参数中。

### 使用方式

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    /**
     * 方式1：直接注入 LoginUser 参数（推荐）
     */
    @GetMapping("/profile")
    public Result<LoginUser> getProfile(LoginUser loginUser) {
        // loginUser 会自动注入，无需手动获取
        return Result.success(loginUser);
    }
    
    /**
     * 方式2：与其他参数一起使用
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(
            LoginUser loginUser,
            @RequestParam(required = false) String format) {
        
        Map<String, Object> info = new HashMap<>();
        info.put("userId", loginUser.getUserId());
        info.put("username", loginUser.getUsername());
        return Result.success(info);
    }
}
```

### 工作原理

参数解析器会：
1. 检查方法参数类型是否为 `LoginUser`
2. 从 `LoginUserContext` 中获取当前登录用户
3. 如果用户未登录，抛出 `BusinessException`

### 注意事项

- 如果用户未登录，参数解析器会抛出异常，返回 401 状态码
- 参数解析器已经自动配置，无需手动注册

---

## 自定义扩展

### 1. 自定义 Realm

如果需要自定义认证逻辑，可以实现 `Realm` 接口：

```java
@Component
public class CustomRealm implements Realm {
    
    @Override
    public boolean support(AuthenticationToken token) {
        return token instanceof HostAuthenticationToken;
    }
    
    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) {
        HostAuthenticationToken hostToken = (HostAuthenticationToken) token;
        String tokenStr = hostToken.getCredential();
        
        // 自定义解析逻辑
        LoginUser loginUser = parseToken(tokenStr);
        
        return new AuthenticationInfo() {
            @Override
            public LoginUser getUser() {
                return loginUser;
            }
            
            @Override
            public String getCredential() {
                return tokenStr;
            }
        };
    }
}
```

### 2. 自定义 Signature

如果需要使用其他签名算法，可以实现 `Signature` 接口：

```java
@Component
public class CustomSignature implements Signature {
    
    @Override
    public String sign(LoginUser loginUser, String key) {
        // 自定义签名逻辑
        return generateCustomToken(loginUser, key);
    }
    
    @Override
    public LoginUser verify(String token, String key) {
        // 自定义验证逻辑
        return parseCustomToken(token, key);
    }
}
```

### 3. 自定义 Authenticator

如果需要完全自定义认证逻辑，可以实现 `Authenticator` 接口：

```java
@Component
public class CustomAuthenticator implements Authenticator {
    
    @Override
    public String login(AuthenticationInfo authenticationInfo) {
        // 自定义登录逻辑
        return generateToken(authenticationInfo);
    }
    
    @Override
    public LoginUser authenticate(HostAuthenticationToken token) {
        // 自定义认证逻辑
        return validateToken(token);
    }
}
```

---

## Session 管理

### Session 模式 vs 纯 JWT 模式

#### Session 模式（`session.enabled: true`）

**优点：**
- ✅ 支持强制下线（踢出用户）
- ✅ 支持设备管理（踢出指定设备）
- ✅ 支持用户状态控制（禁用、踢出等）
- ✅ 支持会话超时和续期
- ✅ 支持多设备登录管理

**缺点：**
- ⚠️ 需要 Redis 存储
- ⚠️ 性能稍差（需要访问 Redis）

**适用场景：**
- 企业级应用
- 需要用户管理功能的应用
- 需要设备管理的应用

#### 纯 JWT 模式（`session.enabled: false`）

**优点：**
- ✅ 无状态，性能好
- ✅ 不需要 Redis
- ✅ 配置简单

**缺点：**
- ❌ 无法强制下线
- ❌ 无法管理设备
- ❌ 无法控制用户状态

**适用场景：**
- 中小型应用
- 简单的 API 服务
- 不需要用户管理功能的应用

### Session 状态

Session 支持以下状态：

- `NORMAL(1)`：正常状态
- `DISABLED(0)`：用户被禁用
- `KICK_OUT(-1)`：用户被踢出（所有设备）
- `DEVICE_KICK_OUT(-2)`：设备被踢出
- `REPLACED(-3)`：用户在其他地方登录，当前 Session 被替换

### Session 管理服务

```java
@Autowired
private SessionManagementService sessionManagementService;

// 强制用户下线（踢出所有设备）
sessionManagementService.kickOutUser(userId);

// 踢出指定设备
sessionManagementService.kickOutDevice(userId, deviceId);

// 禁用用户（禁用所有 Session）
sessionManagementService.disableUser(userId);

// 获取用户的所有 Session Key
Set<String> sessionKeys = sessionManagementService.getUserSessionKeys(userId);
```

### Session 超时和续期

- **超时时间**：如果用户在指定时间内（默认 30 分钟）没有访问，Session 会超时，需要重新登录
- **续期机制**：如果用户在超时时间内访问，Session 会自动续期（延长过期时间）

### 设备验证

如果启用了设备验证（`validate-device: true`），系统会验证请求的设备ID是否与 Session 中存储的设备ID匹配。

设备ID的提取顺序：
1. 从 Header `X-Device-Id` 中获取
2. 从 User-Agent 中提取（hashCode）
3. 使用 IP 地址作为设备ID

**建议**：客户端在请求 Header 中传递 `X-Device-Id`，以便进行设备管理。

---

## 常见问题

### 1. JWT 密钥安全

**Q: 如何设置安全的 JWT 密钥？**

A: 建议使用至少 32 位的随机字符串，可以通过以下方式生成：

```bash
# Linux/Mac
openssl rand -base64 32

# Java
String secret = UUID.randomUUID().toString().replace("-", "") + 
                UUID.randomUUID().toString().replace("-", "");
```

### 2. Token 过期时间设置

**Q: Token 过期时间应该设置多长？**

A: 根据业务需求设置：
- 移动应用：可以设置较长时间（如 30 天）
- Web 应用：建议设置较短时间（如 7 天）
- 高安全要求：建议设置更短时间（如 1 天），配合 Refresh Token 使用

### 3. Session vs 纯 JWT

**Q: 什么时候使用 Session 模式，什么时候使用纯 JWT 模式？**

A: 
- **Session 模式**：需要用户管理功能（强制下线、设备管理等）时使用
- **纯 JWT 模式**：简单的 API 服务，不需要用户管理功能时使用

### 4. 排除路径配置

**Q: 如何配置不需要认证的路径？**

A: 在 `exclude-paths` 中配置，支持 Ant 模式：

```yaml
kite:
  auth:
    exclude-paths:
      - /api/auth/**      # 认证相关接口
      - /api/public/**    # 公开接口
      - /doc.html        # Swagger 文档
      - /swagger-ui/**    # Swagger UI
```

### 5. 权限检查失败

**Q: 权限检查失败时如何返回自定义错误信息？**

A: 权限检查失败时会抛出 `BusinessException`，可以通过全局异常处理器自定义错误信息：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.fail(e.getMessage());
    }
}
```

### 6. Mock 模式使用

**Q: Mock 模式如何使用？**

A: 在开发环境中启用 Mock 模式：

```yaml
kite:
  auth:
    mock-enabled: true
    mock-user-id: 1
    mock-username: admin
```

启用后，所有请求都会使用 Mock 用户，无需提供 Token。

### 7. 多设备登录

**Q: 如何支持多设备登录？**

A: 启用 Session 模式后，默认支持多设备登录。如果需要限制单设备登录，可以在登录时删除其他设备的 Session：

```java
// 登录时删除其他设备的 Session
sessionManagementService.kickOutUser(userId);
// 然后创建新的 Session
```

### 8. 自定义 Realm

**Q: 如何自定义 Realm？**

A: 实现 `Realm` 接口并标记为 Spring Bean：

```java
@Component
public class CustomRealm implements Realm {
    // 实现接口方法
}
```

框架会自动使用自定义的 Realm。

---

## 总结

本框架提供了完整的认证、授权、Session 管理功能，采用了优雅的分层抽象设计，支持灵活扩展。

### 核心优势

1. **优雅的架构**：分层抽象，职责清晰
2. **事件驱动**：通过事件通知机制解耦业务逻辑
3. **便捷使用**：参数解析器自动注入，简化代码
4. **灵活扩展**：所有核心组件都支持自定义实现
5. **开箱即用**：Spring Boot Starter，自动配置

### 快速开始

1. 添加依赖
2. 配置 `application.yml`
3. 实现 `AuthenticationService`
4. 创建登录接口
5. 使用认证功能

### 更多信息

如有问题，请参考：
- 源码：`scaffold-authenticator-starter`
- 示例：`scaffold-user-center`

---

**版本**: 1.0.0-SNAPSHOT  
**更新日期**: 2025-11-04

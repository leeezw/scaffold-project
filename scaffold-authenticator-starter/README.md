# 认证框架使用说明

## 概述

`scaffold-authenticator-starter` 是一个基于 JWT 的认证框架 Starter，提供了：

1. **JWT Token 认证**：自动拦截请求，验证 Token
2. **登录用户上下文**：通过 `LoginUserContext` 获取当前登录用户
3. **权限控制**：通过 `@RequiresRoles` 和 `@RequiresPermissions` 注解控制访问权限
4. **自动配置**：通过 Spring Boot 自动配置，开箱即用

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

### 2. 配置

在 `application.yml` 中配置：

```yaml
kite:
  auth:
    enabled: true                    # 是否启用认证
    token-header: Authorization      # Token Header 名称
    token-prefix: Bearer             # Token 前缀
    secret: your-secret-key          # JWT 密钥（必填，请修改为安全的密钥）
    expire-time: 604800000           # Token 过期时间（毫秒），默认 7 天
    mock-enabled: false              # 是否启用 Mock 用户（开发环境使用）
    check-permission: true           # 是否验证权限
    exclude-paths:                   # 排除路径（不需要认证的路径）
      - /api/hello
      - /api/auth/login
      - /doc.html
```

### 3. 实现 AuthenticationService

创建一个实现类，实现 `AuthenticationService` 接口：

```java
@Service
public class UserAuthenticationService implements AuthenticationService {
    
    @Autowired
    private UserService userService;
    
    @Override
    public LoginUser authenticate(String username, String password) {
        User user = userService.findByUsername(username);
        if (user == null || !passwordMatches(password, user.getPassword())) {
            return null;
        }
        
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setRoles(user.getRoles());
        loginUser.setPermissions(user.getPermissions());
        
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
        loginUser.setRoles(user.getRoles());
        loginUser.setPermissions(user.getPermissions());
        
        return loginUser;
    }
}
```

### 4. 使用登录接口

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Value("${kite.auth.secret}")
    private String jwtSecret;
    
    @Value("${kite.auth.expire-time}")
    private Long expireTime;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String username, 
                                             @RequestParam String password) {
        LoginUser loginUser = authenticationService.authenticate(username, password);
        if (loginUser == null) {
            return Result.fail("用户名或密码错误");
        }
        
        loginUser.setExpireAt(System.currentTimeMillis() + expireTime);
        String token = JwtUtils.generateToken(loginUser, jwtSecret, expireTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", loginUser);
        return Result.success(result);
    }
}
```

### 5. 在 Controller 中使用

#### 获取当前登录用户

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/profile")
    public Result<LoginUser> getProfile() {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        return Result.success(loginUser);
    }
}
```

#### 使用权限注解

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    // 需要 admin 角色
    @RequiresRoles("admin")
    @GetMapping("/users")
    public Result<List<User>> getUsers() {
        // ...
    }
    
    // 需要 user:delete 权限
    @RequiresPermissions("user:delete")
    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        // ...
    }
    
    // 需要 admin 角色或 user:read 权限（其中一个即可）
    @RequiresRoles(value = {"admin", "manager"}, logical = false)
    @RequiresPermissions("user:read")
    @GetMapping("/user/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        // ...
    }
}
```

#### 允许匿名访问

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

## API 调用示例

### 登录

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -d "username=admin&password=123456"
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expireTime": 1704067200000,
    "user": {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员"
    }
  }
}
```

### 使用 Token 访问受保护接口

```bash
curl -X GET "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## 核心组件

### LoginUser

登录用户信息类，包含：
- `userId`: 用户ID
- `username`: 用户名
- `nickname`: 昵称
- `roles`: 角色列表
- `permissions`: 权限列表
- `expireAt`: Token 过期时间

### LoginUserContext

登录用户上下文，提供静态方法：
- `getLoginUser()`: 获取当前登录用户
- `getUserId()`: 获取当前用户ID
- `getUsername()`: 获取当前用户名
- `isLogin()`: 判断是否已登录
- `clear()`: 清除上下文

### JwtUtils

JWT 工具类，提供：
- `generateToken()`: 生成 Token
- `parseToken()`: 解析 Token
- `validateToken()`: 验证 Token

### 注解

- `@RequiresRoles`: 需要角色
- `@RequiresPermissions`: 需要权限
- `@AllowAnonymous`: 允许匿名访问

## 注意事项

1. **JWT 密钥**：请务必修改 `kite.auth.secret` 为安全的密钥（至少 32 位）
2. **Token 过期时间**：根据业务需求设置合理的过期时间
3. **排除路径**：将不需要认证的路径（如登录、注册、Swagger 文档）添加到 `exclude-paths`
4. **权限检查**：默认启用权限检查，可以通过 `check-permission: false` 关闭

## 在其他项目中使用

如果要在其他项目中使用此认证框架，只需要：

1. 将 `scaffold-authenticator-starter` 模块打包安装到本地 Maven 仓库
2. 在其他项目中添加依赖
3. 实现 `AuthenticationService` 接口
4. 配置 `application.yml`

即可快速集成认证功能！


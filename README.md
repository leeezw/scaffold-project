# Spring Boot 开发脚手架

基于 Spring Boot 的开发脚手架，包含登录认证、权限认证和用户中台功能。

## 项目结构

```
scaffold-project/
├── scaffold-common/          # 公共模块
│   └── src/main/java/com/kite/common/
│       └── config/
│           └── RedisConfig.java
├── scaffold-user-center/     # 用户中台模块
│   └── src/main/java/com/kite/usercenter/
│       ├── entity/          # 实体类
│       ├── mapper/          # Mapper 接口
│       ├── service/         # 服务层
│       └── controller/      # 控制器
└── scaffold-app/            # 启动模块
    ├── src/main/java/com/kite/app/
    │   └── Application.java
    └── src/main/resources/
        ├── application.yml
        └── mybatis-config.xml
```

## 技术栈

- **Spring Boot 2.7.14**
- **MyBatis 2.3.1**
- **MySQL 8.0**
- **Redis**
- **Druid** (数据库连接池)
- **JWT** (认证)
- **Lombok**
- **Knife4j** (API 文档，基于 Springdoc OpenAPI)

## 快速开始

### 1. 前置要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis

### 2. 数据库配置

创建数据库：

```sql
CREATE DATABASE scaffold_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `scaffold-app/src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scaffold_db?...
    username: root
    password: your_password
```

### 3. Redis 配置

确保 Redis 服务已启动（默认端口 6379）。

如果 Redis 有密码，在 `application.yml` 中配置：

```yaml
spring:
  redis:
    password: your_redis_password
```

### 4. 编译和运行

```bash
# 编译项目
mvn clean install

# 运行项目
cd scaffold-app
mvn spring-boot:run
```

### 5. 访问地址

- 应用地址：http://localhost:8080
- Druid 监控：http://localhost:8080/druid/index.html (用户名/密码: admin/admin)
- **Knife4j API 文档**：http://localhost:8080/doc.html (重点！)
- Swagger UI：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs

## Swagger 接口文档

### 访问地址

- **Knife4j 文档**：http://localhost:8080/doc.html（推荐，界面更美观）
- **Swagger UI**：http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**：http://localhost:8080/v3/api-docs

### 使用 Swagger 注解

在 Controller 中使用 Swagger 注解来生成接口文档：

```java
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Operation(summary = "获取用户列表", description = "分页查询用户列表")
    @GetMapping("/list")
    public Result<UserListVO> list(@Parameter(description = "页码") @RequestParam int page) {
        // ...
    }
}
```

### 常用注解

- `@Tag`: 标注 Controller 的标签和描述
- `@Operation`: 标注接口方法的说明
- `@Parameter`: 标注请求参数说明
- `@Schema`: 标注实体类字段说明

### 配置说明

Swagger 配置在 `application.yml` 中：

```yaml
springdoc:
  api-docs:
    enabled: true              # 是否启用 API 文档
  swagger-ui:
    enabled: true              # 是否启用 Swagger UI
  group-configs:
    - group: 'default'
      packages-to-scan: com.kite.usercenter.controller

knife4j:
  enable: true                 # 是否启用 Knife4j
  production: false            # 生产环境建议设置为 true 隐藏文档
```

## 下一步

1. ✅ 整合 Swagger 接口文档（已完成）
2. 创建用户表和相关数据库表
3. 实现登录认证功能
4. 实现权限认证功能
5. 实现用户管理功能

## 注意事项

- 首次运行前请先创建数据库
- 确保 MySQL 和 Redis 服务已启动
- 修改配置文件中的数据库连接信息


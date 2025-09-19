# 控制器统计API路由总结

## 基础配置
- **应用上下文路径**: `/api` (在 `application.properties` 中配置)
- **所有API请求都需要 `/api` 前缀**

## 统计接口路由列表

### 1. 系统总体统计 (HomeController)
- **控制器路径**: `/api/home`
- **统计接口**:
  - `GET /api/home/quick-stats` - 快速统计信息
  - `GET /api/home/system-stats` - 系统详细统计
  - `GET /api/home/dashboard` - 仪表板数据
  - `GET /api/home/health` - 系统健康检查
  - `POST /api/home/refresh-cache` - 刷新缓存 (需要ADMIN权限)

### 2. 用户管理统计 (UserController)
- **控制器路径**: `/api/users`
- **统计接口**:
  - `GET /api/users/stats` - 用户管理统计信息
- **权限要求**: ADMIN 或 SUPER_ADMIN

### 3. 媒体资源统计 (MediaAssetController)
- **控制器路径**: `/api/media`
- **统计接口**:
  - `GET /api/media/stats` - 媒体资源统计信息
- **权限要求**: ADMIN 或 SUPER_ADMIN

### 4. 管理员用户统计 (AdminUserController)
- **控制器路径**: `/api/admin`
- **统计接口**:
  - `GET /api/admin/stats` - 管理员用户统计信息
- **权限要求**: SUPER_ADMIN

### 5. 词汇管理统计 (AvVocabController)
- **控制器路径**: `/api/vocab`
- **统计接口**:
  - `GET /api/vocab/stats` - 词汇管理统计信息 (待实现)
- **权限要求**: ADMIN 或 SUPER_ADMIN

### 6. 术语管理统计 (AvTermController)
- **控制器路径**: `/api/terms`
- **统计接口**:
  - `GET /api/terms/stats` - 术语管理统计信息 (待实现)
- **权限要求**: ADMIN 或 SUPER_ADMIN

## 错误分析

### 当前错误
```
URL: http://localhost:8080/api/home/users/stats
错误: No static resource home/users/stats
```

### 问题原因
客户端请求的路径 `/api/home/users/stats` 是**错误的**。

### 正确路径
应该请求: `http://localhost:8080/api/users/stats`

## 安全配置

所有统计接口都在 `SecurityConfig.java` 中配置了权限控制：

```java
// 用户管理统计 - 管理员及以上可查看
.requestMatchers(HttpMethod.GET, "/users/stats").hasAnyRole("ADMIN", "SUPER_ADMIN")
// 媒体资源统计 - 管理员及以上可查看
.requestMatchers(HttpMethod.GET, "/media/stats").hasAnyRole("ADMIN", "SUPER_ADMIN")
// 管理员用户统计 - 只有超级管理员可查看
.requestMatchers(HttpMethod.GET, "/admin/stats").hasRole("SUPER_ADMIN")
// 词汇和术语统计 - 管理员及以上可查看
.requestMatchers(HttpMethod.GET, "/vocab/stats").hasAnyRole("ADMIN", "SUPER_ADMIN")
.requestMatchers(HttpMethod.GET, "/terms/stats").hasAnyRole("ADMIN", "SUPER_ADMIN")
```

## 缓存配置

所有统计接口都配置了Redis缓存，TTL为15分钟：

```java
// 控制器统计缓存15分钟
configMap.put("controllerStats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
configMap.put("userStats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
configMap.put("mediaStats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
configMap.put("adminStats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
```

## 测试建议

使用正确的URL进行测试：

```bash
# 用户统计 (需要ADMIN权限)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/users/stats

# 媒体统计 (需要ADMIN权限)  
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/media/stats

# 管理员统计 (需要SUPER_ADMIN权限)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/admin/stats

# 系统总体统计 (公开访问)
curl http://localhost:8080/api/home/quick-stats
```

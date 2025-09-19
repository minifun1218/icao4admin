# EQAS Backend API 文档

## 概述

EQAS (English Qualification Assessment System) 是一个航空英语水平评估系统，提供多种英语测试模块，包括听力理解、口语面试、故事复述、LSA听说评估、ATC模拟等功能。

**Base URL**: `http://localhost:8080/api`

## 认证方式

系统支持两种认证方式：
1. **JWT Token认证**: 用于管理员用户
2. **微信登录**: 用于普通用户

认证header格式：
```
Authorization: Bearer <your-jwt-token>
```

## 角色权限

- **USER**: 普通用户，可以查看内容和参与考试
- **ADMIN**: 管理员，可以管理内容但不能管理其他管理员
- **SUPER_ADMIN**: 超级管理员，拥有所有权限

---

## 1. 用户管理 (/users)

### 1.1 微信用户登录
```http
POST /api/users/wechat/login
```

**权限**: 公开接口

**请求体**:
```json
{
  "code": "string",          // 微信授权码，必填
  "nickname": "string",      // 用户昵称，可选
  "avatar": "string",        // 用户头像URL，可选
  "gender": 1,                // 性别：0-未知，1-男，2-女，可选
  "location": "string",      // 用户位置信息，可选
  "rawData": "string",       // 原始数据（用于签名验证），可选
  "signature": "string"      // 签名（用于验证数据完整性），可选
}
```

**响应**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,          // 令牌过期时间（秒）
  "userInfo": {
    "id": 1,
    "openid": "oGZUI0egBJY1rkKllLdcBRN2q_Xu",
    "username": "张三",
    "phone": "13800138000",
    "avatar": "https://avatar.example.com/user.jpg",
    "gender": 1,               // 性别：0-未知，1-男，2-女
    "birthday": "1990-01-01",  // 生日
    "level": 1,                // 用户等级
    "location": "北京市",      // 位置信息
    "joinDate": "2024-01-01",  // 加入日期
    "status": 1,               // 状态：1-正常，0-禁用
    "roles": ["USER"],         // 角色列表
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 1.2 根据ID获取用户信息
```http
GET /api/users/{id}
```

**权限**: ADMIN 或 用户本人

**路径参数**:
- `id`: 用户ID（Long类型）

**响应**:
```json
{
  "id": 1,
  "openid": "oGZUI0egBJY1rkKllLdcBRN2q_Xu",
  "username": "张三",
  "phone": "13800138000",
  "avatar": "https://avatar.example.com/user.jpg",
  "gender": 1,
  "birthday": "1990-01-01",
  "level": 1,
  "location": "北京市",
  "joinDate": "2024-01-01",
  "status": 1,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00",
  "roles": [
    {
      "id": 1,
      "name": "普通用户",
      "code": "USER",
      "description": "普通用户角色"
    }
  ]
}
```

### 1.3 根据OpenID获取用户信息
```http
GET /api/users/openid/{openid}
```

**权限**: ADMIN 或 用户本人

**路径参数**:
- `openid`: 微信OpenID（String类型）

**响应**: 与1.2相同的用户信息格式

### 1.4 更新用户信息
```http
PUT /api/users/{id}
```

**权限**: ADMIN 或 用户本人

**路径参数**:
- `id`: 用户ID（Long类型）

**请求体**:
```json
{
  "username": "新用户名",      // 可选，用户名
  "phone": "13900139000",     // 可选，手机号
  "avatar": "new_avatar_url", // 可选，头像URL
  "gender": 2,                // 可选，性别
  "birthday": "1991-02-02",   // 可选，生日
  "location": "上海市"         // 可选，位置信息
}
```

**响应**: 与1.2相同的用户信息格式

### 1.5 删除用户（软删除）
```http
DELETE /api/users/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 用户ID（Long类型）

**响应**:
```json
{
  "success": true,
  "message": "用户删除成功"
}
```

### 1.6 物理删除用户
```http
DELETE /api/users/{id}/hard
```

**权限**: ADMIN

**路径参数**:
- `id`: 用户ID（Long类型）

**响应**:
```json
{
  "success": true,
  "message": "用户永久删除成功"
}
```

### 1.7 获取当前用户信息
```http
GET /api/users/me
```

**权限**: 已认证用户

**响应**: 与1.2相同的用户信息格式

---

## 2. 管理员用户管理 (/admin)

### 2.1 管理员注册
```http
POST /api/admin/register
```

**权限**: SUPER_ADMIN

**请求体**:
```json
{
  "username": "admin001",         // 用户名，3-50个字符，必填
  "password": "Password123!",     // 密码，6-100个字符，必填
  "confirmPassword": "Password123!", // 确认密码，必填
  "email": "admin@example.com",   // 邮箱，必填
  "phone": "13800138000",         // 手机号，可选
  "avatar": "avatar_url"          // 头像URL，可选
}
```

**响应**:
```json
{
  "success": true,
  "message": "管理员用户注册成功",
  "data": {
    "id": 1,
    "username": "admin001",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": "avatar_url",
    "status": 1,
    "roles": ["ADMIN"],
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 2.2 管理员登录
```http
POST /api/admin/login
```

**权限**: 公开接口

**请求体**:
```json
{
  "username": "admin001",        // 用户名，必填
  "password": "Password123!"     // 密码，必填
}
```

**响应**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,           // 令牌过期时间（24小时）
  "adminUserInfo": {
    "id": 1,
    "username": "admin001",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": "avatar_url",
    "status": 1,               // 状态：1-正常，0-禁用
    "roles": ["ADMIN"],        // 角色列表
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 2.3 获取当前管理员信息
```http
GET /api/admin/me
```

**权限**: 已认证管理员

**响应**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "admin001",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": "avatar_url",
    "status": 1,
    "roles": ["ADMIN"],
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 2.4 获取管理员详情
```http
GET /api/admin/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 管理员ID（Long类型）

**响应**: 与2.3相同的管理员信息格式

### 2.5 更新当前管理员信息
```http
PUT /api/admin/me
```

**权限**: 已认证管理员

**请求体**:
```json
{
  "email": "new_email@example.com",  // 可选，新邮箱
  "phone": "13900139000",            // 可选，新手机号
  "avatar": "new_avatar_url"         // 可选，新头像URL
}
```

**响应**: 与2.3相同的管理员信息格式

### 2.6 修改密码
```http
PUT /api/admin/password
```

**权限**: 已认证管理员

**请求体**:
```json
{
  "oldPassword": "OldPassword123!",     // 旧密码，必填
  "newPassword": "NewPassword123!",     // 新密码，必填
  "confirmNewPassword": "NewPassword123!" // 确认新密码，必填
}
```

**响应**:
```json
{
  "success": true,
  "message": "密码修改成功"
}
```

### 2.7 登出
```http
POST /api/admin/logout
```

**权限**: 已认证管理员

**响应**:
```json
{
  "success": true,
  "message": "注销成功"
}
```

---

## 3. 角色管理 (/roles)

### 3.1 获取所有角色
```http
GET /api/roles
```

**权限**: ADMIN

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)
- `sortBy`: 排序字段 (默认: "createdAt")
- `sortDirection`: 排序方向 (默认: "desc")
- `status`: 状态过滤 (可选: 1-已启用, 0-已禁用)

**响应**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "超级管理员",
        "code": "SUPER_ADMIN",
        "description": "系统超级管理员角色",
        "status": 1,
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      },
      {
        "id": 2,
        "name": "管理员",
        "code": "ADMIN",
        "description": "系统管理员角色",
        "status": 1,
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "direction": "DESC",
        "field": "createdAt"
      }
    },
    "totalElements": 3,
    "totalPages": 1,
    "first": true,
    "last": true,
    "numberOfElements": 3
  }
}
```

### 3.2 根据ID获取角色
```http
GET /api/roles/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 角色ID（Long类型）

**响应**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "超级管理员",
    "code": "SUPER_ADMIN",
    "description": "系统超级管理员角色",
    "status": 1,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 3.3 创建角色
```http
POST /api/roles
```

**权限**: SUPER_ADMIN

**请求体**:
```json
{
  "name": "新角色名称",       // 角色名称，必填
  "code": "NEW_ROLE",          // 角色编码，必填
  "description": "新角色描述" // 角色描述，可选
}
```

**响应**: 与3.2相同的角色信息格式

### 3.4 更新角色
```http
PUT /api/roles/{id}
```

**权限**: SUPER_ADMIN

**路径参数**:
- `id`: 角色ID（Long类型）

**请求体**:
```json
{
  "name": "更新后的角色名称",  // 可选，角色名称
  "code": "UPDATED_ROLE",       // 可选，角色编码
  "description": "更新后的描述" // 可选，角色描述
}
```

**响应**: 与3.2相同的角色信息格式

### 3.5 删除角色
```http
DELETE /api/roles/{id}
```

**权限**: SUPER_ADMIN

**路径参数**:
- `id`: 角色ID（Long类型）

**响应**:
```json
{
  "success": true,
  "message": "删除成功"
}
```

### 3.6 更新角色状态
```http
PUT /api/roles/{id}/status
```

**权限**: SUPER_ADMIN

**路径参数**:
- `id`: 角色ID（Long类型）

**查询参数**:
- `status`: 状态值 (1-启用, 0-禁用)

**响应**: 与3.2相同的角色信息格式

---

## 4. 航空词汇管理 (/vocab)

### 4.1 创建词汇
```http
POST /api/vocab
```

**权限**: ADMIN

**请求体**:
```json
{
  "headword": "cleared for takeoff",                 // 词条/术语，必填
  "lemma": "clear",                              // 词形还原，可选
  "pos": "phrase",                               // 词性，可选
  "ipa": "/klɪəd fər ˈteɪkˌɔf/",               // 国际音标，可选
  "definitionEn": "Permission granted to begin takeoff roll", // 英文释义，可选
  "definitionCn": "许可起飞",                      // 中文释义，可选
  "exampleSentence": "Tower: Speedbird 123, cleared for takeoff runway 09L", // 例句，可选
  "exampleTranslation": "塔台：英航123，跑道09L许可起飞", // 例句翻译，可选
  "cefrLevel": "B1",                            // CEFR等级，可选
  "frequencyLevel": 1,                           // 频率等级，可选
  "difficultyLevel": 3,                          // 难度等级，可选
  "audioPath": "/audio/cleared-for-takeoff.mp3", // 语音文件路径，可选
  "imagePath": "/images/takeoff.jpg",            // 图片文件路径，可选
  "tags": "ATC,takeoff,clearance",               // 标签，可选
  "notes": "用于许可飞机起飞",                  // 备注，可选
  "isEnabled": true,                             // 是否启用，可选
  "displayOrder": 1                              // 显示顺序，可选
}
```

**响应**:
```json
{
  "id": 1,
  "headword": "cleared for takeoff",
  "lemma": "clear",
  "pos": "phrase",
  "ipa": "/klɪəd fər ˈteɪkˌɔf/",
  "definitionEn": "Permission granted to begin takeoff roll",
  "definitionCn": "许可起飞",
  "exampleSentence": "Tower: Speedbird 123, cleared for takeoff runway 09L",
  "exampleTranslation": "塔台：英航123，跑道09L许可起飞",
  "cefrLevel": "B1",
  "frequencyLevel": 1,
  "difficultyLevel": 3,
  "audioPath": "/audio/cleared-for-takeoff.mp3",
  "imagePath": "/images/takeoff.jpg",
  "tags": "ATC,takeoff,clearance",
  "notes": "用于许可飞机起飞",
  "isEnabled": true,
  "displayOrder": 1,
  "createdAt": "2024-01-01T00:00:00"
}
```

### 4.2 获取词汇详情
```http
GET /api/vocab/{id}
```

**权限**: USER

**路径参数**:
- `id`: 词汇ID（Long类型）

**查询参数**:
- `includeTopics`: 是否包含主题信息 (默认: false)

**响应**: 与4.1相同的词汇信息格式

### 4.3 获取词汇列表
```http
GET /api/vocab
```

**权限**: USER

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)
- `sort`: 排序字段 (默认: "id")
- `direction`: 排序方向 (默认: "desc")
- `keyword`: 关键词搜索 (支持模糊匹配)
- `cefrLevel`: CEFR等级过滤
- `difficultyLevel`: 难度等级过滤
- `frequencyLevel`: 频率等级过滤
- `pos`: 词性过滤
- `hasAudio`: 是否有语音 (true/false)

**响应**:
```json
{
  "content": [
    {
      "id": 1,
      "headword": "cleared for takeoff",
      "pos": "phrase",
      "ipa": "/klɪəd fər ˈteɪkˌɔf/",
      "definitionCn": "许可起飞",
      "cefrLevel": "B1",
      "frequencyLevel": 1,
      "difficultyLevel": 3,
      "displayHeadword": "cleared for takeoff [/klɪəd fər ˈteɪkˌɔf/]",
      "hasAudioAsset": true,
      "topicCount": 2
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "direction": "DESC",
      "field": "id"
    }
  },
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false,
  "numberOfElements": 20
}
```

### 4.4 更新词汇
```http
PUT /api/vocab/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 词汇ID（Long类型）

**请求体**: 与4.1相同的词汇信息格式（所有字段可选）

**响应**: 与4.1相同的词汇信息格式

### 4.5 删除词汇
```http
DELETE /api/vocab/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 词汇ID（Long类型）

**响应**: 204 No Content

### 4.6 根据词条查询
```http
GET /api/vocab/by-word/{word}
```

**权限**: USER

**路径参数**:
- `word`: 词条内容（String类型）

**响应**: 返回匹配的词汇列表（数组格式）

### 4.7 模糊搜索词汇
```http
GET /api/vocab/search
```

**权限**: USER

**查询参数**:
- `keyword`: 搜索关键词（必填）
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)

**响应**: 与4.3相同的分页格式

### 4.8 综合搜索词汇
```http
GET /api/vocab/comprehensive-search
```

**权限**: USER

**查询参数**:
- `word`: 词条搜索 (可选)
- `definition`: 释义搜索 (可选)
- `example`: 例句搜索 (可选)
- `pos`: 词性过滤 (可选)
- `cefrLevel`: CEFR等级过滤 (可选)
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)

**响应**: 与4.3相同的分页格式

### 4.9 批量创建词汇
```http
POST /api/vocab/batch
```

**权限**: ADMIN

**请求体**: 词汇对象数组（格式与4.1相同）

**响应**: 成功创建的词汇列表

### 4.10 批量删除词汇
```http
DELETE /api/vocab/batch
```

**权限**: ADMIN

**请求体**: 词汇ID数组
```json
[1, 2, 3, 4, 5]
```

**响应**: 204 No Content

### 4.11 获取词汇统计信息
```http
GET /api/vocab/statistics
```

**权限**: USER

**响应**:
```json
{
  "totalCount": 1500,
  "cefrLevelStats": {
    "A1": 150,
    "A2": 200,
    "B1": 300,
    "B2": 400,
    "C1": 300,
    "C2": 150
  },
  "posStats": {
    "noun": 600,
    "verb": 400,
    "adjective": 300,
    "phrase": 200
  },
  "completenessStats": {
    "withAudio": 800,
    "withImage": 500,
    "withIpa": 1200,
    "withExample": 1000
  }
}
```

---

## 5. 航空术语管理 (/terms)

### 5.1 创建术语
```http
POST /api/terms
```

**权限**: ADMIN

**请求体**:
```json
{
  "headword": "ATIS",                  // 术语名称，必填
  "lemma": "ATIS",                     // 词形还原，可选
  "pos": "noun",                       // 词性，可选
  "ipa": "/ˈeɪ tɪ s/",                  // 国际音标，可选
  "definitionEn": "Automatic Terminal Information Service", // 英文释义，可选
  "definitionZh": "自动终端情报服务",     // 中文释义，可选
  "exampleEn": "Contact ATIS on 118.1",  // 英文例句，可选
  "exampleZh": "联系ATIS频率118.1",      // 中文例句，可选
  "cefrLevel": "B2",                     // CEFR等级，可选
  "freqRank": 50,                       // 频率排名，可选
  "source": "ICAO Annex 11",             // 来源，可选
  "audioAssetId": 25,                   // 音频资源ID，可选
  "extraJson": {                        // 扩展信息，可选
    "category": "communication",
    "subcategory": "information_service"
  }
}
```

**响应**: 与请求相同格式，加上id和createdAt字段

### 5.2 获取术语详情
```http
GET /api/terms/{id}
```

**权限**: USER

**路径参数**:
- `id`: 术语ID（Long类型）

**查询参数**:
- `includeTopics`: 是否包含主题信息 (默认: false)

**响应**: 与5.1相同的术语信息格式

### 5.3 获取术语列表
```http
GET /api/terms
```

**权限**: USER

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)
- `sort`: 排序字段 (默认: "id")
- `direction`: 排序方向 (默认: "desc")
- `headword`: 术语名称过滤
- `cefrLevel`: CEFR等级过滤
- `source`: 来源过滤
- `pos`: 词性过滤

**响应**: 与词汇类似的分页格式

### 5.4 更新术语
```http
PUT /api/terms/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 术语ID（Long类型）

**请求体**: 与5.1相同的术语信息格式（所有字段可选）

**响应**: 与5.1相同的术语信息格式

### 5.5 删除术语
```http
DELETE /api/terms/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 术语ID（Long类型）

**响应**: 204 No Content

### 5.6 根据词条获取术语
```http
GET /api/terms/by-headword/{headword}
```

**权限**: USER

**路径参数**:
- `headword`: 术语名称（String类型）

**响应**: 返回匹配的术语列表

### 5.7 根据CEFR等级获取术语
```http
GET /api/terms/by-cefr-level/{cefrLevel}
```

**权限**: USER

**路径参数**:
- `cefrLevel`: CEFR等级 (A1/A2/B1/B2/C1/C2)

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)

**响应**: 与5.3相同的分页格式

### 5.8 检查词条可用性
```http
GET /api/terms/check-headword/{headword}
```

**权限**: USER

**路径参数**:
- `headword`: 术语名称（String类型）

**响应**:
```json
{
  "headword": "ATIS",
  "available": false,
  "exists": true,
  "existingTerms": [
    {
      "id": 1,
      "headword": "ATIS",
      "definitionZh": "自动终端情报服务",
      "cefrLevel": "B2"
    }
  ]
}
```

---

## 6. 听力选择题 (/listening-mcq)

### 6.1 获取所有题目
```http
GET /api/listening-mcq/questions
```

**权限**: USER

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)

**响应**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "moduleId": 1,
        "audioId": 1,
        "textStem": "What clearance is the pilot requesting?",
        "playOnce": true,
        "answerSeconds": 15,
        "choices": [
          {
            "id": 1,
            "label": "A",
            "content": "Takeoff clearance",
            "isCorrect": true
          },
          {
            "id": 2,
            "label": "B",
            "content": "Landing clearance",
            "isCorrect": false
          }
        ]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### 6.2 根据ID获取题目
```http
GET /api/listening-mcq/questions/{id}
```

**权限**: USER

**路径参数**:
- `id`: 题目ID（Long类型）

**响应**: 与6.1中单个题目相同的格式

### 6.3 创建题目
```http
POST /api/listening-mcq/questions
```

**权限**: ADMIN

**请求体**:
```json
{
  "moduleId": 1,                    // 模块ID，必填
  "audioId": 1,                     // 音频资源ID，必填
  "textStem": "What clearance is the pilot requesting?", // 题目题干，必填
  "playOnce": true,                 // 是否只播放一次，可选
  "answerSeconds": 15,              // 答题时间（秒），可选
  "choices": [                      // 选项列表，必填
    {
      "label": "A",                // 选项标签，必填
      "content": "Takeoff clearance", // 选项内容，必填
      "isCorrect": true            // 是否为正确答案，必填
    },
    {
      "label": "B",
      "content": "Landing clearance",
      "isCorrect": false
    }
  ]
}
```

**响应**: 与6.1中单个题目相同的格式

### 6.4 更新题目
```http
PUT /api/listening-mcq/questions/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 题目ID（Long类型）

**请求体**: 与6.3相同的题目信息格式

**响应**: 与6.1中单个题目相同的格式

### 6.5 删除题目
```http
DELETE /api/listening-mcq/questions/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 题目ID（Long类型）

**响应**:
```json
{
  "success": true,
  "data": "题目删除成功"
}
```

### 6.6 提交答案
```http
POST /api/listening-mcq/responses/submit
```

**权限**: USER

**请求体**:
```json
{
  "questionId": 1,              // 题目ID，必填
  "selectedChoiceId": 1,        // 选择的选项ID，必填
  "elapsedMs": 10500            // 答题耗时（毫秒），可选
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "questionId": 1,
    "selectedChoiceId": 1,
    "isCorrect": true,
    "score": 1.0,
    "elapsedMs": 10500,
    "submittedAt": "2024-01-01T00:00:00"
  }
}
```

### 6.7 获取题目统计信息
```http
GET /api/listening-mcq/questions/{questionId}/stats
```

**权限**: USER

**路径参数**:
- `questionId`: 题目ID（Long类型）

**响应**:
```json
{
  "success": true,
  "data": {
    "totalResponses": 100,
    "correctResponses": 75,
    "incorrectResponses": 20,
    "ungradedResponses": 5,
    "averageResponseTime": 12.5,
    "difficulty": "medium",
    "choiceSelections": {
      "A": 75,
      "B": 15,
      "C": 8,
      "D": 2
    },
    "timestamp": "2024-01-01T00:00:00"
  }
}
```

---

## 7. OPI口语面试 (/opi)

### 7.1 获取所有话题
```http
GET /api/opi/topics
```
**权限**: USER

### 7.2 根据ID获取话题
```http
GET /api/opi/topics/{id}
```
**权限**: USER

### 7.3 根据模块ID获取话题
```http
GET /api/opi/topics/module/{moduleId}
```
**权限**: USER

### 7.4 创建话题
```http
POST /api/opi/topics
```
**权限**: ADMIN

### 7.5 更新话题
```http
PUT /api/opi/topics/{id}
```
**权限**: ADMIN

### 7.6 删除话题
```http
DELETE /api/opi/topics/{id}
```
**权限**: ADMIN

### 7.7 获取问题列表
```http
GET /api/opi/questions
```
**权限**: USER

### 7.8 提交回答
```http
POST /api/opi/submit
```
**权限**: USER

---

## 8. 故事复述 (/story-retell)

### 8.1 获取所有复述题目
```http
GET /api/story-retell/items
```
**权限**: USER

### 8.2 根据ID获取复述题目
```http
GET /api/story-retell/items/{id}
```
**权限**: USER

### 8.3 创建复述题目
```http
POST /api/story-retell/items
```
**权限**: ADMIN

### 8.4 更新复述题目
```http
PUT /api/story-retell/items/{id}
```
**权限**: ADMIN

### 8.5 删除复述题目
```http
DELETE /api/story-retell/items/{id}
```
**权限**: ADMIN

### 8.6 提交复述回答
```http
POST /api/story-retell/submit
```
**权限**: USER

---

## 9. LSA听说评估 (/lsa-dialogs)

### 9.1 获取所有对话
```http
GET /api/lsa-dialogs
```
**权限**: USER

### 9.2 根据ID获取对话
```http
GET /api/lsa-dialogs/{id}
```
**权限**: USER

### 9.3 创建对话
```http
POST /api/lsa-dialogs
```
**权限**: ADMIN

### 9.4 更新对话
```http
PUT /api/lsa-dialogs/{id}
```
**权限**: ADMIN

### 9.5 删除对话
```http
DELETE /api/lsa-dialogs/{id}
```
**权限**: ADMIN

### 9.6 提交回答
```http
POST /api/lsa-dialogs/submit
```
**权限**: USER

---

## 10. ATC模拟 (/atc-sim)

### 10.1 机场管理

#### 10.1.1 获取所有机场
```http
GET /api/atc-sim/airports
```
**权限**: USER

#### 10.1.2 根据ID获取机场
```http
GET /api/atc-sim/airports/{id}
```
**权限**: USER

#### 10.1.3 搜索机场
```http
GET /api/atc-sim/airports/search
```
**权限**: USER

**查询参数**:
- `keyword`: 搜索关键词

#### 10.1.4 创建机场
```http
POST /api/atc-sim/airports
```
**权限**: ADMIN

#### 10.1.5 更新机场
```http
PUT /api/atc-sim/airports/{id}
```
**权限**: ADMIN

#### 10.1.6 删除机场
```http
DELETE /api/atc-sim/airports/{id}
```
**权限**: ADMIN

### 10.2 场景管理

#### 10.2.1 获取所有场景
```http
GET /api/atc-sim/scenarios
```
**权限**: USER

#### 10.2.2 根据ID获取场景
```http
GET /api/atc-sim/scenarios/{id}
```
**权限**: USER

#### 10.2.3 创建场景
```http
POST /api/atc-sim/scenarios
```
**权限**: ADMIN

#### 10.2.4 更新场景
```http
PUT /api/atc-sim/scenarios/{id}
```
**权限**: ADMIN

#### 10.2.5 删除场景
```http
DELETE /api/atc-sim/scenarios/{id}
```
**权限**: ADMIN

### 10.3 提交ATC回答
```http
POST /api/atc-sim/submit
```
**权限**: USER

---

## 11. 媒体资源管理 (/media)

### 11.1 上传媒体文件
```http
POST /api/media/upload
```

**权限**: ADMIN

**请求方式**: multipart/form-data

**请求参数**:
- `file`: 媒体文件 (必填)
- `type`: 媒体类型 (audio/image/video/document) (必填)
- `title`: 标题 (可选)
- `description`: 描述 (可选)

**响应**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "mediaType": "audio",
    "uri": "/media/audio/sample.mp3",
    "durationMs": 30000,
    "transcript": "Audio transcription",
    "metadata": {
      "title": "Sample Audio",
      "description": "Sample audio file",
      "fileSize": 1024000,
      "format": "mp3"
    },
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

### 11.2 获取所有媒体资源
```http
GET /api/media
```

**权限**: USER

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 10)
- `sort`: 排序字段 (默认: "id,asc")

**响应**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "mediaType": "audio",
        "uri": "/media/audio/sample.mp3",
        "durationMs": 30000,
        "transcript": "Audio transcription",
        "metadata": {
          "title": "Sample Audio",
          "description": "Sample audio file"
        },
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 50,
    "totalPages": 5
  }
}
```

### 11.3 根据ID获取媒体资源
```http
GET /api/media/{id}
```

**权限**: USER

**路径参数**:
- `id`: 媒体资源ID（Long类型）

**响应**: 与11.1相同的媒体资源信息格式

### 11.4 更新媒体资源信息
```http
PUT /api/media/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 媒体资源ID（Long类型）

**请求体**:
```json
{
  "transcript": "更新后的转写内容",   // 可选
  "metadata": {                       // 可选
    "title": "更新后的标题",
    "description": "更新后的描述"
  }
}
```

**响应**: 与11.1相同的媒体资源信息格式

### 11.5 删除媒体资源
```http
DELETE /api/media/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: 媒体资源ID（Long类型）

**响应**:
```json
{
  "success": true,
  "data": "删除媒体资源成功"
}
```

### 11.6 根据类型获取媒体资源
```http
GET /api/media/type/{type}
```

**权限**: USER

**路径参数**:
- `type`: 媒体类型 (audio/image/video/document)

**响应**: 与11.2相同的列表格式

### 11.7 搜索媒体资源
```http
GET /api/media/search
```

**权限**: USER

**查询参数**:
- `keyword`: 搜索关键词 (在转写内容中搜索)

**响应**: 与11.2相同的列表格式

---

## 12. Banner管理 (/banners)

### 12.1 获取所有Banner组
```http
GET /api/banners
```

**权限**: USER

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)
- `sortBy`: 排序字段 (默认: "sortOrder")
- `sortDirection`: 排序方向 (默认: "desc")
- `includeItems`: 是否包含Banner项目 (默认: false)

**响应**:
```json
{
  "content": [
    {
      "id": 1,
      "name": "主页轮播图",
      "description": "应用主页展示的轮播图组",
      "sortOrder": 1,
      "isActive": true,
      "startTime": "2024-01-01T00:00:00",
      "endTime": "2024-12-31T23:59:59",
      "status": "active",
      "itemCount": 5,
      "validItemCount": 4,
      "activeItemCount": 3,
      "shouldDisplay": true,
      "inDisplayTime": true,
      "remainingDisplayMinutes": 525600,
      "expiringSoon": false,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00",
      "items": [
        {
          "id": 1,
          "title": "欢迎使用EQAS系统",
          "description": "系统介绍",
          "imageUrl": "/images/banner1.jpg",
          "linkUrl": "/intro",
          "sortWeight": 1,
          "isEnabled": true,
          "createdAt": "2024-01-01T00:00:00",
          "updatedAt": "2024-01-01T00:00:00"
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 3,
  "totalPages": 1
}
```

### 12.2 根据ID获取Banner组
```http
GET /api/banners/{id}
```

**权限**: USER

**路径参数**:
- `id`: Banner组ID（Long类型）

**查询参数**:
- `includeItems`: 是否包含Banner项目 (默认: false)

**响应**: 与12.1中单个Banner相同的格式

### 12.3 创建Banner组
```http
POST /api/banners
```

**权限**: ADMIN

**请求体**:
```json
{
  "name": "主页轮播图",              // Banner组名称，必填
  "description": "应用主页展示的轮播图组", // 描述，可选
  "sortOrder": 1,                     // 排序权重，可选
  "isActive": true,                   // 是否激活，可选
  "startTime": "2024-01-01T00:00:00", // 开始时间，可选
  "endTime": "2024-12-31T23:59:59"   // 结束时间，可选
}
```

**响应**: 与12.1中单个Banner相同的格式

### 12.4 更新Banner组
```http
PUT /api/banners/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: Banner组ID（Long类型）

**请求体**: 与12.3相同的Banner信息格式（所有字段可选）

**响应**: 与12.1中单个Banner相同的格式

### 12.5 删除Banner组
```http
DELETE /api/banners/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: Banner组ID（Long类型）

**查询参数**:
- `forceDelete`: 是否强制删除 (默认: false)

**响应**: 204 No Content

### 12.6 获取活跃Banner组
```http
GET /api/banners/active
```

**权限**: USER

**响应**: 返回当前活跃的Banner组列表（格式与12.1相同）

### 12.7 批量操作Banner
```http
POST /api/banners/batch
```

**权限**: ADMIN

**请求体**:
```json
{
  "operation": "enable",      // 操作类型: enable/disable/delete/updateSortOrder
  "ids": [1, 2, 3],           // Banner ID列表
  "sortOrder": 10,            // 仅updateSortOrder操作需要
  "forceDelete": false        // 仅delete操作可用
}
```

**响应**:
```json
{
  "operation": "enable",
  "affectedCount": 3,
  "success": true
}
```

### 12.8 获取Banner统计信息
```http
GET /api/banners/stats
```

**权限**: ADMIN

**响应**:
```json
{
  "totalCount": 10,
  "activeCount": 7,
  "inactiveCount": 3
}
```

### 12.9 检查Banner名称可用性
```http
GET /api/banners/check-name
```

**权限**: ADMIN

**查询参数**:
- `name`: 要检查的名称
- `excludeId`: 排除的Banner ID (可选)

**响应**:
```json
{
  "available": true
}
```

---

## 13. Banner项目管理 (/banner-items)

### 13.1 获取Banner项目列表
```http
GET /api/banner-items
```

**权限**: USER

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)
- `bannerId`: Banner组ID过滤 (可选)

**响应**: 与12.1中的items字段相同的分页格式

### 13.2 根据Banner组ID获取项目
```http
GET /api/banner-items/banner/{bannerId}
```

**权限**: USER

**路径参数**:
- `bannerId`: Banner组ID（Long类型）

**响应**: 返回指定Banner组下的所有项目列表

### 13.3 创建Banner项目
```http
POST /api/banner-items
```

**权限**: ADMIN

**请求体**:
```json
{
  "bannerId": 1,                      // Banner组ID，必填
  "title": "欢迎使用EQAS系统",    // 标题，必填
  "description": "系统介绍",         // 描述，可选
  "imageUrl": "/images/banner1.jpg",   // 图片URL，可选
  "linkUrl": "/intro",                 // 链接URL，可选
  "sortWeight": 1,                    // 排序权重，可选
  "isEnabled": true                   // 是否启用，可选
}
```

**响应**: 与12.1中单个item相同的格式

### 13.4 批量创建Banner项目
```http
POST /api/banner-items/batch
```

**权限**: ADMIN

**请求体**: Banner项目对象数组（格式与13.3相同）

**响应**: 成功创建的Banner项目列表

### 13.5 更新Banner项目
```http
PUT /api/banner-items/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: Banner项目ID（Long类型）

**请求体**: 与13.3相同的Banner项目信息格式（所有字段可选）

**响应**: 与12.1中单个item相同的格式

### 13.6 删除Banner项目
```http
DELETE /api/banner-items/{id}
```

**权限**: ADMIN

**路径参数**:
- `id`: Banner项目ID（Long类型）

**响应**: 204 No Content

---

## 14. 用户标记管理 (/user-flags)

### 14.1 获取用户词汇标记
```http
GET /api/user-flags/vocab
```

**权限**: USER（仅自己的标记）

**查询参数**:
- `page`: 页码 (默认: 0)
- `size`: 每页大小 (默认: 20)
- `flagType`: 标记类型过滤 (BOOKMARKED/LEARNED/DIFFICULT)

**响应**:
```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "vocabId": 1,
      "flagType": "BOOKMARKED",
      "createdAt": "2024-01-01T00:00:00",
      "vocabInfo": {
        "id": 1,
        "headword": "cleared for takeoff",
        "definitionCn": "许可起飞",
        "cefrLevel": "B1"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 15,
  "totalPages": 1
}
```

### 14.2 添加词汇标记
```http
POST /api/user-flags/vocab
```

**权限**: USER

**请求体**:
```json
{
  "vocabId": 1,                // 词汇ID，必填
  "flagType": "BOOKMARKED"     // 标记类型，必填 (BOOKMARKED/LEARNED/DIFFICULT)
}
```

**响应**: 与14.1中单个标记相同的格式

### 14.3 删除词汇标记
```http
DELETE /api/user-flags/vocab/{vocabId}/flag/{flagType}
```

**权限**: USER

**路径参数**:
- `vocabId`: 词汇ID（Long类型）
- `flagType`: 标记类型 (BOOKMARKED/LEARNED/DIFFICULT)

**响应**: 204 No Content

### 14.4 获取用户术语标记
```http
GET /api/user-flags/terms
```

**权限**: USER

**查询参数**: 与14.1相同

**响应**: 与14.1类似，但包含termInfo而非vocabInfo

### 14.5 添加术语标记
```http
POST /api/user-flags/terms
```

**权限**: USER

**请求体**:
```json
{
  "termId": 1,                 // 术语ID，必填
  "flagType": "BOOKMARKED"     // 标记类型，必填
}
```

**响应**: 与14.4中单个标记相同的格式

### 14.6 删除术语标记
```http
DELETE /api/user-flags/terms/{termId}/flag/{flagType}
```

**权限**: USER

**路径参数**:
- `termId`: 术语ID（Long类型）
- `flagType`: 标记类型 (BOOKMARKED/LEARNED/DIFFICULT)

**响应**: 204 No Content

---

## 错误响应格式

所有API的错误响应都遵循统一格式：

### 统一错误响应格式
```json
{
  "success": false,
  "message": "错误描述信息",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null,
  "details": {
    "field": "username",
    "rejectedValue": "ab",
    "reason": "用户名长度必须在3-50个字符之间"
  }
}
```

### 常见错误类型

#### 输入验证错误 (400 Bad Request)
```json
{
  "success": false,
  "message": "输入参数验证失败",
  "code": "VALIDATION_ERROR",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null,
  "details": {
    "errors": [
      {
        "field": "username",
        "message": "用户名不能为空"
      },
      {
        "field": "password",
        "message": "密码长度必须在6-100个字符之间"
      }
    ]
  }
}
```

#### 认证失败 (401 Unauthorized)
```json
{
  "success": false,
  "message": "认证失败，请重新登录",
  "code": "AUTHENTICATION_FAILED",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null
}
```

#### 权限不足 (403 Forbidden)
```json
{
  "success": false,
  "message": "权限不足，无法访问该资源",
  "code": "ACCESS_DENIED",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null
}
```

#### 资源不存在 (404 Not Found)
```json
{
  "success": false,
  "message": "请求的资源不存在",
  "code": "RESOURCE_NOT_FOUND",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null
}
```

#### 数据冲突 (409 Conflict)
```json
{
  "success": false,
  "message": "数据冲突，用户名已存在",
  "code": "DATA_CONFLICT",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null,
  "details": {
    "conflictField": "username",
    "conflictValue": "admin001"
  }
}
```

#### 服务器内部错误 (500 Internal Server Error)
```json
{
  "success": false,
  "message": "服务器内部错误，请稍后重试",
  "code": "INTERNAL_SERVER_ERROR",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null,
  "details": {
    "errorId": "ERR-2024-001-12345",
    "suggestion": "请联系系统管理员或稍后重试"
  }
}
```

### 常见HTTP状态码

| 状态码 | 说明 | 使用场景 |
|---------|------|----------|
| 200 OK | 请求成功 | 成功获取数据、成功更新资源 |
| 201 Created | 资源创建成功 | 成功创建新的用户、词汇、题目等 |
| 204 No Content | 成功但无内容 | 成功删除资源 |
| 400 Bad Request | 请求参数错误 | 输入验证失败、格式错误 |
| 401 Unauthorized | 未认证 | Token过期、未登录 |
| 403 Forbidden | 权限不足 | 普通用户访问管理员接口 |
| 404 Not Found | 资源不存在 | 访问不存在的用户、词汇等 |
| 409 Conflict | 资源冲突 | 用户名重复、数据依赖关系冲突 |
| 422 Unprocessable Entity | 业务逻辑错误 | 业务规则不符合 |
| 429 Too Many Requests | 请求频率过高 | API限流 |
| 500 Internal Server Error | 服务器内部错误 | 系统异常、数据库连接失败 |
| 502 Bad Gateway | 网关错误 | 上游服务不可用 |
| 503 Service Unavailable | 服务不可用 | 系统维护、资源不足 |

---

## 成功响应格式

### 统一成功响应格式
```json
{
  "success": true,
  "message": "操作成功",
  "code": "SUCCESS",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    // 具体数据内容
  }
}
```

### 分页响应格式
分页接口的响应格式：

```json
{
  "success": true,
  "message": "获取数据成功",
  "code": "SUCCESS",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "示例数据",
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,      // 当前页码（从0开始）
      "pageSize": 20,       // 每页大小
      "sort": {
        "sorted": true,     // 是否排序
        "direction": "ASC", // 排序方向
        "field": "id"       // 排序字段
      },
      "offset": 0,          // 偏移量
      "unpaged": false,     // 是否未分页
      "paged": true         // 是否已分页
    },
    "totalElements": 100,   // 总记录数
    "totalPages": 5,        // 总页数
    "first": true,          // 是否为首页
    "last": false,          // 是否为末页
    "numberOfElements": 20, // 当前页元素数
    "size": 20,             // 页大小
    "number": 0,            // 当前页码
    "empty": false          // 是否为空页
  }
}
```

### 列表响应格式（非分页）
```json
{
  "success": true,
  "message": "获取列表成功",
  "code": "SUCCESS",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": [
    {
      "id": 1,
      "name": "示例数据1"
    },
    {
      "id": 2,
      "name": "示例数据2"
    }
  ]
}
```

### 单个对象响应格式
```json
{
  "success": true,
  "message": "获取详情成功",
  "code": "SUCCESS",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    "id": 1,
    "name": "示例数据",
    "description": "详细描述",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

---

## 认证示例

### 管理员登录流程

#### 1. 管理员登录获取Token
```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin001",
    "password": "AdminPassword123!"
  }'
```

**响应示例**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbjAwMSIsInVzZXJJZCI6MSwidXNlclR5cGUiOiJhZG1pbiIsImlhdCI6MTYzNzY1NDQwMCwiZXhwIjoxNjM3NzQwODAwfQ.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_payload.signature",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "adminUserInfo": {
    "id": 1,
    "username": "admin001",
    "email": "admin@example.com",
    "roles": ["ADMIN"]
  }
}
```

#### 2. 使用Token访问受保护接口
```bash
curl -X GET http://localhost:8080/api/admin/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbjAwMSIsInVzZXJJZCI6MSwidXNlclR5cGUiOiJhZG1pbiIsImlhdCI6MTYzNzY1NDQwMCwiZXhwIjoxNjM3NzQwODAwfQ.signature" \
  -H "Content-Type: application/json"
```

#### 3. 创建新的词汇（需要ADMIN权限）
```bash
curl -X POST http://localhost:8080/api/vocab \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "headword": "cleared for takeoff",
    "definitionEn": "Permission granted to begin takeoff roll",
    "definitionCn": "许可起飞",
    "cefrLevel": "B1"
  }'
```

### 微信用户登录流程

#### 1. 微信登录获取Token
```bash
curl -X POST http://localhost:8080/api/users/wechat/login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "061Dh1Ga1ByQO00y5HGa1T2",
    "nickname": "张三",
    "avatar": "https://avatar.example.com/user.jpg",
    "gender": 1,
    "location": "北京市"
  }'
```

**响应示例**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvR1pVSTBlZ0JKWTFya0tsbExkY0JSTjJxX1h1IiwidXNlcklkIjoxLCJ1c2VyVHlwZSI6InVzZXIiLCJpYXQiOjE2Mzc2NTQ0MDAsImV4cCI6MTYzNzY1ODAwMH0.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_payload.signature",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userInfo": {
    "id": 1,
    "openid": "oGZUI0egBJY1rkKllLdcBRN2q_Xu",
    "username": "张三",
    "avatar": "https://avatar.example.com/user.jpg",
    "roles": ["USER"]
  }
}
```

#### 2. 使用Token访问用户接口
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvR1pVSTBlZ0JKWTFya0tsbExkY0JSTjJxX1h1IiwidXNlcklkIjoxLCJ1c2VyVHlwZSI6InVzZXIiLCJpYXQiOjE2Mzc2NTQ0MDAsImV4cCI6MTYzNzY1ODAwMH0.signature" \
  -H "Content-Type: application/json"
```

#### 3. 查询词汇列表（USER权限）
```bash
curl -X GET "http://localhost:8080/api/vocab?page=0&size=10&cefrLevel=B1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

#### 4. 添加词汇标记（USER权限）
```bash
curl -X POST http://localhost:8080/api/user-flags/vocab \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "vocabId": 1,
    "flagType": "BOOKMARKED"
  }'
```

### Token刷新流程

#### 使用RefreshToken获取新的AccessToken
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_payload.signature"
  }'
```

### 错误处理示例

#### Token过期错误
```bash
# 请求
 curl -X GET http://localhost:8080/api/admin/me \
  -H "Authorization: Bearer expired_token"

# 响应 (401 Unauthorized)
{
  "success": false,
  "message": "Token已过期，请重新登录",
  "code": "TOKEN_EXPIRED",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null
}
```

#### 权限不足错误
```bash
# 普通用户访问管理员接口
curl -X POST http://localhost:8080/api/vocab \
  -H "Authorization: Bearer user_token" \
  -H "Content-Type: application/json" \
  -d '{"headword": "test"}'

# 响应 (403 Forbidden)
{
  "success": false,
  "message": "权限不足，需要ADMIN权限",
  "code": "ACCESS_DENIED",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": null
}
```

---

## 开发说明

### 基本规范

1. **所有时间格式**: ISO 8601 格式 (`2024-01-01T00:00:00Z`)
2. **字符编码**: 统一使用 UTF-8 编码
3. **请求头**: 所有POST/PUT请求必须包含 `Content-Type: application/json`
4. **响应格式**: 所有响应都是JSON格式，包含success、message、data字段

### 分页规范

- **分页参数**: 使用 `page`(从0开始) 和 `size` 参数
- **默认分页**: `page=0, size=20`
- **最大分页**: `size` 最大为 100
- **排序参数**: 使用 `sort` 和 `direction` 参数
- **排序方向**: `asc`(升序) 或 `desc`(降序)

### 过滤规范

- **模糊搜索**: 使用 `keyword` 参数，支持多字段模糊匹配
- **精确过滤**: 使用具体字段名作为参数
- **日期范围**: 使用 `startDate` 和 `endDate` 参数
- **数值范围**: 使用 `min{Field}` 和 `max{Field}` 参数

### 缓存策略

- **读取接口**: 部分读取接口支持缓存
- **缓存TTL**: 根据数据类型不同，从30秒到60分钟不等
- **缓存清理**: 写入操作会自动清理相关缓存
- **手动清理**: 部分模块提供缓存清理接口

### 文件上传规范

- **支持格式**: 
  - 音频: mp3, wav, m4a (最大 50MB)
  - 图片: jpg, jpeg, png, gif (最大 10MB)
  - 视频: mp4, avi, mov (最大 100MB)
  - 文档: pdf, doc, docx, txt (最大 20MB)
- **上传方式**: multipart/form-data
- **文件命名**: 系统自动生成UUID名称，保留原始扩展名

### 安全规范

- **HTTPS**: 生产环境必须使用HTTPS
- **CORS**: 支持跨域请求，可配置允许的域名
- **限流**: API调用频率限制（每分钟100次）
- **数据验证**: 所有输入参数都进行严格验证
- **SQL注入防护**: 使用参数化查询和ORM框架

### 异常处理

- **统一异常处理**: 所有异常都由GlobalExceptionHandler处理
- **日志记录**: 所有异常都会记录详细日志
- **错误追踪**: 包含errorId用于问题追踪
- **用户友好**: 错误信息对用户友好，不暴露系统内部信息

### 性能优化

- **数据库索引**: 对常用查询字段建立索引
- **懒加载**: 关联数据使用懒加载策略
- **批量操作**: 支持批量创建、更新、删除操作
- **分页限制**: 避免返回过大数据集

### 版本管理

- **API版本**: 当前版本 v1.0
- **向后兼容**: 新版本保持对旧版本的兼容
- **弃用通知**: 弃用的API会提剆30天通知
- **文档更新**: API文档与代码同步更新

### 环境配置

- **开发环境**: `http://localhost:8080/api`
- **测试环境**: `https://test-api.eqas.example.com/api`
- **生产环境**: `https://api.eqas.example.com/api`

### 调试工具

- **Swagger UI**: `/swagger-ui.html` (仅开发环境)
- **API文档**: `/api-docs` (仅开发环境)
- **健康检查**: `/actuator/health`
- **指标监控**: `/actuator/metrics` (仅内网可访问)

---

---

## 更新日志

### v1.0.0 (2024-09-18)
- 初始版本发布
- 完成所有核心模块API开发
- 支持用户管理、词汇管理、听力测试等功能
- 完成微信登录集成
- 完成JWT认证系统

---

## 联系信息

- **项目名称**: EQAS Backend API
- **版本**: v1.0.0
- **最后更新**: 2024年9月18日
- **技术支持**: backend-support@eqas.example.com
- **文档维护**: dev-team@eqas.example.com

---

**注意**: 本文档为最新版本，如有问题请及时联系技术支持团队。

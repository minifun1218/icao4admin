# ICAO4 后台管理系统

基于 Vue 3 + Element Plus 开发的 ICAO 英语培训后台管理系统前端。

## 🚀 功能特性

### 核心模块
- **用户管理** - 用户信息管理、状态控制、密码重置
- **词汇管理** - 词汇创建、编辑、分类、导入导出
- **术语管理** - 航空术语管理、分类管理、批量操作
- **听力选择题管理** - 完整的题目管理、选项管理、统计分析

### 系统功能
- **认证系统** - JWT Token 认证、角色权限控制
- **文件管理** - 媒体资源上传、管理
- **数据分析** - 统计图表、数据可视化
- **响应式设计** - 支持移动端和桌面端

## 🛠️ 技术栈

- **前端框架**: Vue 3 (Composition API)
- **UI 组件库**: Element Plus
- **路由管理**: Vue Router 4
- **状态管理**: Pinia
- **HTTP 客户端**: Axios
- **构建工具**: Vite
- **开发语言**: JavaScript

## 📦 安装和运行

### 环境要求
- Node.js >= 20.19.0 或 >= 22.12.0
- npm 或 yarn

### 安装依赖
\`\`\`bash
npm install
\`\`\`

### 开发环境运行
\`\`\`bash
npm run dev
\`\`\`

### 生产环境构建
\`\`\`bash
npm run build
\`\`\`

### 预览构建结果
\`\`\`bash
npm run preview
\`\`\`

## 🔧 配置说明

### API 配置
后端 API 地址配置在 \`src/api/index.js\` 中：
\`\`\`javascript
baseURL: 'http://localhost:8080/api'
\`\`\`

### 代理配置
开发环境的代理配置在 \`vite.config.js\` 中：
\`\`\`javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
\`\`\`

## 📁 项目结构

\`\`\`
src/
├── api/                # API 接口定义
│   ├── index.js       # Axios 配置和拦截器
│   ├── user.js        # 用户相关 API
│   ├── vocab.js       # 词汇相关 API
│   ├── term.js        # 术语相关 API
│   └── exercise.js    # 练习相关 API
├── assets/            # 静态资源
├── components/        # 公共组件
├── layout/            # 布局组件
│   └── MainLayout.vue # 主布局
├── router/            # 路由配置
│   └── index.js       # 路由定义
├── stores/            # Pinia 状态管理
│   └── auth.js        # 认证状态
├── views/             # 页面组件
│   ├── Login.vue      # 登录页
│   ├── Dashboard.vue  # 仪表盘
│   ├── user/          # 用户管理
│   ├── vocab/         # 词汇管理
│   ├── term/          # 术语管理
│   ├── exercise/      # 练习管理
│   ├── banner/        # 轮播图管理
│   ├── media/         # 媒体管理
│   ├── atc/           # ATC模拟管理
│   └── role/          # 角色权限管理
├── App.vue            # 根组件
└── main.js            # 入口文件
\`\`\`

## 🔐 认证和权限

### 登录
- 默认管理员账号: \`admin\`
- 使用 JWT Token 进行身份验证
- Token 自动续期和过期处理

### 权限控制
- 基于角色的权限控制 (RBAC)
- 路由级别的权限验证
- 菜单和功能按钮的权限控制

## 📊 已实现的功能模块

### ✅ 完全实现
1. **用户管理**
   - 用户列表查看和搜索
   - 用户信息编辑
   - 用户状态切换
   - 密码重置
   - 批量删除
   - 数据导出

2. **词汇管理**
   - 词汇创建和编辑
   - 多条件搜索和筛选
   - 批量导入导出
   - 音频文件关联
   - 难度等级和分类管理

3. **术语管理**
   - 术语创建和编辑
   - 分类和来源管理
   - 批量操作
   - 数据导入导出

4. **听力选择题管理**
   - 题目创建和编辑
   - 多选项管理
   - 音频文件关联
   - 统计分析
   - 批量操作

### 🚧 待完善模块
- 听力理解管理
- 口语模仿管理
- 故事复述管理
- 轮播图管理
- ATC模拟管理
- 媒体资源管理
- 角色权限管理

## 🌐 API 接口

项目使用的 API 接口文档请参考 \`EQS_API.md\` 文件。

主要接口包括：
- 认证管理 API
- 用户管理 API
- 词汇管理 API
- 术语管理 API
- 练习管理 API
- 媒体资源 API

## 🎨 界面预览

### 登录页面
- 简洁的登录界面
- 响应式设计
- 表单验证

### 仪表盘
- 数据统计卡片
- 图表展示（待集成）
- 快捷操作
- 最近活动

### 管理页面
- 统一的表格布局
- 高级搜索功能
- 批量操作支持
- 分页和排序

## 🔄 开发说明

### 代码规范
- 使用 Vue 3 Composition API
- 采用 \`<script setup>\` 语法
- 响应式数据使用 \`ref\` 和 \`reactive\`
- 统一的错误处理机制

### 样式规范
- 使用 Element Plus 组件库
- 自定义样式使用 scoped CSS
- 响应式设计支持移动端

### API 调用规范
- 统一的 API 封装
- 自动 Token 处理
- 错误统一处理
- Loading 状态管理

## 📝 注意事项

1. **后端依赖**: 此项目需要配合后端 API 服务使用
2. **端口配置**: 默认前端端口 5173，后端端口 8080
3. **数据格式**: 严格按照 API 文档的数据格式进行交互
4. **权限验证**: 所有页面都需要登录后访问（除登录页外）

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来帮助改进项目。

## 📄 许可证

Copyright © 2024 ICAO4 English Training System. All rights reserved.
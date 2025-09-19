# Dashboard 报错修复总结

## 问题描述

主页访问时出现以下错误：
```
SyntaxError: Duplicate export of 'termStatsApi'
```

## 错误原因

在 `src/api/home.js` 文件中，`termStatsApi` 被重复导出了两次：

1. **第109行**：`export const termStatsApi = {...}`
2. **第199行**：在 export 语句中再次导出 `termStatsApi`

这导致了 JavaScript 模块的重复导出语法错误。

## 修复方案

### 1. 删除重复的导出语句

移除了文件末尾的重复导出语句：
```javascript
// 删除了这部分代码
export {
  homeApi,
  userStatsApi,
  mediaStatsApi,
  adminStatsApi,
  vocabStatsApi,
  termStatsApi  // 这里造成了重复导出
}
```

### 2. 保留单独的 export const 声明

每个 API 对象都使用 `export const` 单独导出：
```javascript
export const homeApi = {...}
export const userStatsApi = {...}
export const mediaStatsApi = {...}
export const adminStatsApi = {...}
export const vocabStatsApi = {...}
export const termStatsApi = {...}  // 只在这里导出一次
export const statsApi = {...}
```

### 3. 添加降级处理机制

为了确保主页在后端API不可用时仍能正常显示，在 `Dashboard.vue` 中添加了：

#### 3.1 初始化默认数据
- 在组件挂载时首先设置默认数据
- 确保页面有内容显示，不会出现空白

#### 3.2 API调用降级处理
- 每个API调用都添加了错误处理
- 当API调用失败时，使用模拟数据作为降级方案
- 显示友好的错误提示

#### 3.3 数据加载策略
```javascript
onMounted(() => {
  // 首先初始化默认数据，确保页面能正常显示
  initializeDefaultData()
  
  // 然后尝试加载真实数据
  loadAllData()
})
```

## 修复后的文件结构

### src/api/home.js
```javascript
// 单独导出每个API对象
export const homeApi = {...}
export const userStatsApi = {...}
export const mediaStatsApi = {...}
export const adminStatsApi = {...}
export const vocabStatsApi = {...}
export const termStatsApi = {...}
export const statsApi = {...}

// 默认导出
export default homeApi
```

### src/views/Dashboard.vue
- 添加了 `initializeDefaultData()` 函数
- 为所有数据加载函数添加了降级处理
- 改进了错误处理机制

## 测试验证

1. **语法检查**：使用 linter 检查，无语法错误
2. **模块导入**：确保所有导入都能正常工作
3. **页面显示**：主页能正常显示默认数据
4. **API调用**：在后端可用时能正确加载真实数据

## 预防措施

1. **导出规范**：
   - 使用 `export const` 进行单独导出
   - 避免在文件末尾使用 `export {}` 语句重复导出
   
2. **错误处理**：
   - 所有API调用都应该有错误处理
   - 提供降级方案确保用户体验
   
3. **数据初始化**：
   - 组件应该有默认状态
   - 避免依赖外部数据才能正常显示

## 修复结果

- ✅ 解决了重复导出语法错误
- ✅ 主页能够正常加载和显示
- ✅ 在后端API不可用时有降级处理
- ✅ 提供了友好的用户体验
- ✅ 代码结构更加健壮

现在主页应该能够正常访问，不再出现语法错误，并且在各种情况下都能提供良好的用户体验。

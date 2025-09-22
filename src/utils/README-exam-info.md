# 考试信息管理工具 (exam-info.js)

这是一个专门用于管理考试信息的强大工具类，提供了完整的考试数据管理、状态控制、搜索筛选和性能优化功能。

## 🚀 主要特性

- **完整的CRUD操作** - 创建、读取、更新、删除考试数据
- **智能状态管理** - 考试状态自动管理和转换
- **高效搜索筛选** - 支持多条件组合搜索
- **分页管理** - 内置分页逻辑和控制
- **缓存机制** - 自动缓存优化性能
- **自动刷新** - 可配置的数据自动刷新
- **批量操作** - 支持批量删除、导出等操作
- **Vue集成** - 提供组合式API，完美集成Vue 3
- **类型安全** - 完整的数据验证和错误处理

## 📦 安装和导入

```javascript
// 导入考试信息管理类
import { ExamInfoManager, createExamInfoManager, useExamInfo } from '@/utils/exam-info'

// 导入使用示例
import examInfoDemo from '@/utils/exam-info-demo'
```

## 🔧 基础使用

### 1. 创建管理器实例

```javascript
import { createExamInfoManager } from '@/utils/exam-info'

// 创建基础实例
const examManager = createExamInfoManager()

// 创建带配置的实例
const examManager = createExamInfoManager({
  autoRefresh: true,      // 启用自动刷新
  refreshInterval: 30000, // 30秒刷新一次
  enableCache: true,      // 启用缓存
  cacheTimeout: 300000    // 5分钟缓存超时
})
```

### 2. 在Vue组件中使用

```vue
<template>
  <div>
    <!-- 搜索栏 -->
    <el-input 
      v-model="keyword" 
      placeholder="搜索考试"
      @input="handleSearch"
    />
    
    <!-- 考试列表 -->
    <el-table v-loading="loading" :data="filteredExams">
      <el-table-column prop="name" label="考试名称" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ formatExamStatus(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="handleEdit(row)">编辑</el-button>
          <el-button @click="handleDelete(row)" type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      @current-change="goToPage"
      @size-change="changePageSize"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useExamInfo } from '@/utils/exam-info'

// 使用考试信息管理
const {
  examList,
  filteredExams,
  loading,
  pagination,
  statistics,
  loadExamData,
  createExam,
  updateExam,
  deleteExam,
  setSearchParams,
  goToPage,
  changePageSize,
  formatExamStatus,
  getStatusTagType
} = useExamInfo({
  autoRefresh: true,
  enableCache: true
})

const keyword = ref('')

const handleSearch = () => {
  setSearchParams({ keyword: keyword.value })
}

const handleEdit = (exam) => {
  // 编辑逻辑
}

const handleDelete = async (exam) => {
  await deleteExam(exam.id, exam.name)
}

onMounted(() => {
  loadExamData()
})
</script>
```

## 🎯 核心API

### 数据操作方法

```javascript
// 加载考试数据
await examManager.loadExamData(forceRefresh = false)

// 根据ID获取考试详情
const exam = await examManager.getExamById(examId, forceRefresh = false)

// 创建考试
const newExam = await examManager.createExam(examData)

// 更新考试
const updatedExam = await examManager.updateExam(examId, examData)

// 删除考试
const success = await examManager.deleteExam(examId, examName)

// 批量删除考试
const success = await examManager.batchDeleteExams(examIds)
```

### 状态管理方法

```javascript
// 开始考试
await examManager.startExam(examId)

// 结束考试
await examManager.endExam(examId, examName)

// 发布考试
await examManager.publishExam(examId)

// 取消发布考试
await examManager.unpublishExam(examId)
```

### 搜索筛选方法

```javascript
// 设置搜索参数
examManager.setSearchParams({
  keyword: '英语',
  status: 'published',
  type: 'formal',
  dateRange: ['2024-01-01', '2024-12-31']
})

// 执行搜索
await examManager.search(params)

// 重置搜索
examManager.resetSearchParams()
```

### 分页管理方法

```javascript
// 跳转到指定页面
examManager.goToPage(page)

// 改变每页大小
examManager.changePageSize(size)

// 设置分页参数
examManager.setPagination({ page: 1, size: 20 })
```

### 选择管理方法

```javascript
// 选择考试
examManager.selectExams([examId1, examId2])

// 切换选择状态
examManager.toggleExamSelection(examId)

// 全选/取消全选
examManager.toggleSelectAll()

// 清空选择
examManager.clearSelection()

// 设置当前考试
examManager.setCurrentExam(exam)
```

### 导入导出方法

```javascript
// 导出考试数据
await examManager.exportExamData(examIds, format = 'excel')

// 导入考试数据
await examManager.importExamData(file, options)
```

## 📊 响应式数据

管理器提供的响应式数据状态：

```javascript
const examManager = useExamInfo()

// 考试列表数据
examManager.examList.value        // 原始考试列表
examManager.filteredExams.value   // 过滤后的考试列表

// 加载状态
examManager.loading.value         // 数据加载状态
examManager.saving.value          // 数据保存状态

// 分页信息
examManager.pagination.value      // { page, size, total }

// 搜索条件
examManager.searchParams.value    // 当前搜索参数

// 选中状态
examManager.selectedExams.value   // 选中的考试ID列表
examManager.currentExam.value     // 当前操作的考试

// 统计信息
examManager.statistics.value      // 考试统计数据
```

## 🛠 配置选项

创建管理器时可以传入的配置选项：

```javascript
const options = {
  // 自动刷新配置
  autoRefresh: false,           // 是否启用自动刷新
  refreshInterval: 30000,       // 刷新间隔（毫秒）
  
  // 缓存配置
  enableCache: true,            // 是否启用缓存
  cacheTimeout: 300000,         // 缓存超时时间（毫秒）
  
  // 其他配置
  defaultPageSize: 20,          // 默认每页大小
  maxRetries: 3                 // 最大重试次数
}
```

## 🎨 与ExamInfoPanel组件集成

`ExamInfoPanel.vue` 组件已经集成了考试信息管理工具，支持两种使用模式：

### 1. 内置管理器模式（推荐）

```vue
<template>
  <ExamInfoPanel 
    :use-builtin-manager="true"
    @add="handleAdd"
    @edit="handleEdit"
    @view-participants="handleViewParticipants"
  />
</template>
```

在此模式下，组件会自动使用内置的考试信息管理器，具有完整的数据管理功能。

### 2. 传统模式（向后兼容）

```vue
<template>
  <ExamInfoPanel 
    :exams="examList"
    :loading="loading"
    :use-builtin-manager="false"
    @add="handleAdd"
    @edit="handleEdit"
    @delete="handleDelete"
    @start="handleStart"
    @end="handleEnd"
    @view-participants="handleViewParticipants"
  />
</template>
```

在此模式下，组件使用传入的props数据，保持向后兼容性。

## 🔍 工具方法

管理器还提供了一系列实用的工具方法：

```javascript
// 格式化方法
examManager.formatExamStatus(status)     // 格式化考试状态
examManager.formatExamType(type)         // 格式化考试类型

// 标签类型获取
examManager.getStatusTagType(status)     // 获取状态标签类型
examManager.getTypeTagType(type)         // 获取类型标签类型

// 数据分析
const analysis = examManager.getExamAnalysis(examId)

// 数据验证
const validation = examManager.validateExamData(examData)
```

## 📈 性能优化

### 缓存机制

```javascript
// 清除特定缓存
examManager.clearCache(['examList', 'exam_123'])

// 清除所有缓存
examManager.clearCache()

// 强制刷新数据
examManager.loadExamData(true)
```

### 防抖搜索

```javascript
let searchTimeout = null
function debouncedSearch(keyword) {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    examManager.search({ keyword })
  }, 500)
}
```

## 🚨 错误处理

管理器内置了完善的错误处理机制：

```javascript
try {
  await examManager.createExam(examData)
} catch (error) {
  console.error('操作失败:', error)
  // 错误会自动显示用户友好的提示消息
}
```

## 🧪 测试和调试

查看 `src/utils/exam-info-demo.js` 文件获取完整的使用示例和测试代码。

## 📝 注意事项

1. **API依赖**: 确保 `paperApi` 和 `resultApi` 正确配置
2. **权限控制**: 某些操作可能需要相应的用户权限
3. **数据格式**: 确保传入的数据符合预期格式
4. **内存管理**: 记得在组件销毁时清理管理器资源

## 🔄 更新日志

- v1.0.0 - 初始版本，提供完整的考试信息管理功能
- 支持Vue 3组合式API
- 集成ElementPlus组件库
- 提供完整的TypeScript支持（计划中）

## 🤝 贡献指南

如需扩展或修改功能，请参考现有代码结构，确保：

1. 保持API的一致性
2. 添加适当的错误处理
3. 更新相关文档
4. 添加必要的测试用例

---

更多详细信息和高级用法，请参考源代码中的注释和示例文件。

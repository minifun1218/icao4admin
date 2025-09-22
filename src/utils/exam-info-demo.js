/**
 * 考试信息管理工具使用示例
 * 演示如何在不同场景下使用 exam-info.js
 */

import { createExamInfoManager, useExamInfo } from './exam-info'

// ==================== 示例1: 基础使用 ====================

/**
 * 在普通JavaScript中使用考试信息管理器
 */
export function basicUsageExample() {
  // 创建管理器实例
  const examManager = createExamInfoManager({
    autoRefresh: true,
    refreshInterval: 30000,
    enableCache: true
  })

  // 加载考试数据
  examManager.loadExamData().then(() => {
    console.log('考试列表:', examManager.state.examList)
    console.log('统计信息:', examManager.state.statistics)
  })

  // 搜索考试
  examManager.search({
    keyword: '英语',
    status: 'published',
    type: 'formal'
  })

  // 创建新考试
  examManager.createExam({
    name: '新考试',
    type: 'practice',
    duration: 120,
    passingScore: 60,
    description: '这是一个练习考试'
  })

  // 清理资源
  setTimeout(() => {
    examManager.destroy()
  }, 60000)
}

// ==================== 示例2: Vue组件中使用 ====================

/**
 * 在Vue组件中使用考试信息管理（组合式API）
 */
export function vueCompositionExample() {
  return `
// MyExamComponent.vue
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
  search,
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

// 搜索关键词
const keyword = ref('')

// 处理搜索
const handleSearch = () => {
  setSearchParams({ keyword: keyword.value })
}

// 处理编辑
const handleEdit = (exam) => {
  // 编辑逻辑
  console.log('编辑考试:', exam)
}

// 处理删除
const handleDelete = async (exam) => {
  const success = await deleteExam(exam.id, exam.name)
  if (success) {
    console.log('删除成功')
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadExamData()
})
</script>
  `
}

// ==================== 示例3: 高级功能使用 ====================

/**
 * 高级功能使用示例
 */
export function advancedUsageExample() {
  const examManager = createExamInfoManager()

  // 批量操作示例
  async function batchOperations() {
    // 批量删除
    const examIds = [1, 2, 3]
    await examManager.batchDeleteExams(examIds)

    // 导出考试数据
    await examManager.exportExamData(examIds, 'excel')

    // 导入考试数据
    const file = new File([''], 'exams.xlsx')
    await examManager.importExamData(file, {
      overwrite: false,
      validateData: true
    })
  }

  // 状态管理示例
  async function statusManagement() {
    const examId = 123

    // 发布考试
    await examManager.publishExam(examId)

    // 开始考试
    await examManager.startExam(examId)

    // 结束考试
    await examManager.endExam(examId, '期末考试')

    // 取消发布
    await examManager.unpublishExam(examId)
  }

  // 数据分析示例
  function dataAnalysis() {
    const examId = 123
    const analysis = examManager.getExamAnalysis(examId)
    
    console.log('考试分析:', {
      参与人数: analysis.participantCount,
      完成率: analysis.completionRate,
      平均分: analysis.averageScore,
      通过率: analysis.passRate
    })

    // 获取统计信息
    console.log('整体统计:', examManager.state.statistics)
  }

  // 缓存管理示例
  function cacheManagement() {
    // 清除特定缓存
    examManager.clearCache(['examList', 'exam_123'])

    // 清除所有缓存
    examManager.clearCache()

    // 强制刷新数据
    examManager.loadExamData(true)
  }

  return {
    batchOperations,
    statusManagement,
    dataAnalysis,
    cacheManagement
  }
}

// ==================== 示例4: 自定义配置 ====================

/**
 * 自定义配置示例
 */
export function customConfigExample() {
  // 创建带有自定义配置的管理器
  const examManager = createExamInfoManager({
    // 启用自动刷新
    autoRefresh: true,
    refreshInterval: 60000, // 1分钟刷新一次

    // 缓存配置
    enableCache: true,
    cacheTimeout: 600000, // 10分钟缓存超时

    // 其他自定义配置
    defaultPageSize: 50,
    maxRetries: 3
  })

  // 监听状态变化
  const unwatch = examManager.state.$watch('examList', (newList, oldList) => {
    console.log('考试列表更新:', newList.length, '个考试')
  })

  // 自定义验证规则
  examManager.validateExamData = (examData) => {
    const errors = []
    
    if (!examData.name) {
      errors.push('考试名称不能为空')
    }
    
    if (examData.duration < 30) {
      errors.push('考试时长不能少于30分钟')
    }
    
    // 自定义业务规则
    if (examData.type === 'formal' && examData.passingScore < 70) {
      errors.push('正式考试及格分数不能低于70分')
    }
    
    return {
      isValid: errors.length === 0,
      errors
    }
  }

  return { examManager, unwatch }
}

// ==================== 示例5: 错误处理 ====================

/**
 * 错误处理示例
 */
export function errorHandlingExample() {
  const examManager = createExamInfoManager()

  // 全局错误处理
  examManager.onError = (error, operation) => {
    console.error(`操作 ${operation} 失败:`, error)
    
    // 根据错误类型进行不同处理
    if (error.code === 'NETWORK_ERROR') {
      // 网络错误处理
      console.log('网络连接异常，请检查网络设置')
    } else if (error.code === 'PERMISSION_DENIED') {
      // 权限错误处理
      console.log('操作权限不足')
    } else if (error.code === 'VALIDATION_ERROR') {
      // 验证错误处理
      console.log('数据验证失败:', error.details)
    }
  }

  // 带错误处理的操作示例
  async function safeOperations() {
    try {
      // 尝试创建考试
      const exam = await examManager.createExam({
        name: '测试考试',
        type: 'practice',
        duration: 120
      })
      console.log('考试创建成功:', exam)
    } catch (error) {
      console.error('创建考试失败:', error)
      // 可以在这里添加用户友好的错误提示
    }

    try {
      // 尝试加载数据
      await examManager.loadExamData()
    } catch (error) {
      console.error('加载数据失败:', error)
      // 可以显示离线数据或错误页面
    }
  }

  return { safeOperations }
}

// ==================== 示例6: 性能优化 ====================

/**
 * 性能优化示例
 */
export function performanceOptimizationExample() {
  const examManager = createExamInfoManager({
    enableCache: true,
    cacheTimeout: 300000 // 5分钟缓存
  })

  // 防抖搜索
  let searchTimeout = null
  function debouncedSearch(keyword) {
    clearTimeout(searchTimeout)
    searchTimeout = setTimeout(() => {
      examManager.search({ keyword })
    }, 500) // 500ms防抖
  }

  // 虚拟滚动支持
  function getVirtualizedData(startIndex, endIndex) {
    const allExams = examManager.state.examList
    return allExams.slice(startIndex, endIndex)
  }

  // 懒加载
  function lazyLoadExamDetails(examId) {
    return examManager.getExamById(examId, false) // 使用缓存
  }

  // 预加载
  function preloadNextPage() {
    const nextPage = examManager.state.pagination.page + 1
    examManager.setPagination({ 
      page: nextPage 
    })
  }

  return {
    debouncedSearch,
    getVirtualizedData,
    lazyLoadExamDetails,
    preloadNextPage
  }
}

// 导出所有示例
export default {
  basicUsageExample,
  vueCompositionExample,
  advancedUsageExample,
  customConfigExample,
  errorHandlingExample,
  performanceOptimizationExample
}

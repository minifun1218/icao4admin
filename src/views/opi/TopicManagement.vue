<template>
  <div class="topic-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>话题管理</h2>
      <p>管理口语能力面试的话题库</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog" :icon="Plus">
          添加话题
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedTopics.length === 0"
          @click="handleBatchDelete"
          :icon="Delete"
        >
          批量删除
        </el-button>
        <el-button @click="handleImport" :icon="Upload">
          批量导入
        </el-button>
        <el-button @click="handleExport" :icon="Download">
          导出数据
        </el-button>
        <el-button @click="showStatsDialog" :icon="DataAnalysis">
          统计信息
        </el-button>
      </div>
      
      <div class="toolbar-right">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索话题标题或描述..."
          style="width: 300px; margin-right: 10px;"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="showFilterDialog" :icon="Filter">
          高级筛选
        </el-button>
        <el-button @click="loadTopics" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选条件显示 -->
    <div v-if="hasActiveFilters" class="filter-tags">
      <span class="filter-label">当前筛选：</span>
      <el-tag
        v-if="searchForm.moduleId"
        closable
        @close="clearFilter('moduleId')"
        type="info"
      >
        模块ID: {{ searchForm.moduleId }}
      </el-tag>
      <el-tag
        v-if="searchForm.keyword"
        closable
        @close="clearFilter('keyword')"
        type="info"
      >
        关键词: {{ searchForm.keyword }}
      </el-tag>
      <el-button 
        type="text" 
        @click="clearAllFilters"
        style="margin-left: 10px;"
      >
        清除所有筛选
      </el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="topicList"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column
          prop="id"
          label="ID"
          width="80"
          sortable="custom"
        />
        
        <el-table-column
          prop="moduleId"
          label="模块ID"
          width="100"
          sortable="custom"
        >
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ row.moduleId }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="order"
          label="话题顺序"
          width="100"
          sortable="custom"
        >
          <template #default="{ row }">
            <el-tag size="small" type="success">第{{ row.order }}个</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="title"
          label="话题标题"
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <el-link 
              type="primary" 
              @click="viewTopicDetails(row)"
              :underline="false"
            >
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="description"
          label="话题描述"
          min-width="250"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span v-if="row.description">{{ row.description }}</span>
            <el-tag v-else size="small" type="info">无描述</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="createdAt"
          label="创建时间"
          width="160"
          sortable="custom"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column
          label="问题数量"
          width="100"
        >
          <template #default="{ row }">
            <el-link 
              type="primary" 
              @click="viewQuestions(row.id)"
              :underline="false"
            >
              {{ row.questionCount || 0 }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column
          label="操作"
          width="240"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="showEditDialog(row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="copyTopic(row.id)"
              :icon="DocumentCopy"
            >
              复制
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="viewStats(row.id)"
              :icon="DataLine"
            >
              统计
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteTopic(row.id)"
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑话题' : '添加话题'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="topicForm"
        :model="topicForm"
        :rules="topicRules"
        label-width="120px"
      >
        <el-form-item label="模块ID" prop="moduleId">
          <el-select
            v-model="topicForm.moduleId"
            placeholder="请选择模块"
            style="width: 100%"
            filterable
            clearable
            @change="handleModuleChange"
          >
            <el-option
              v-for="module in moduleList"
              :key="module.id"
              :label="`${module.id} - ${module.name || '未命名模块'}`"
              :value="module.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="话题顺序" prop="order">
          <el-input-number
            v-model="topicForm.order"
            :min="1"
            :max="100"
            placeholder="请输入话题顺序"
            style="width: 100%"
          />
          <div class="form-help">同一模块内的话题顺序必须唯一</div>
        </el-form-item>
        
        <el-form-item label="话题标题" prop="title">
          <el-input
            v-model="topicForm.title"
            placeholder="请输入话题标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="话题描述" prop="description">
          <el-input
            v-model="topicForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入话题描述（可选）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTopic" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 高级筛选对话框 -->
    <el-dialog
      v-model="filterDialogVisible"
      title="高级筛选"
      width="500px"
    >
      <el-form label-width="120px">
        <el-form-item label="模块">
          <el-select
            v-model="filterForm.moduleId"
            placeholder="请选择模块"
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="module in moduleList"
              :key="module.id"
              :label="`${module.id} - ${module.name || '未命名模块'}`"
              :value="module.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="resetFilter">重置</el-button>
        <el-button type="primary" @click="applyFilter">应用筛选</el-button>
      </template>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog
      v-model="statsDialogVisible"
      title="统计信息"
      width="700px"
    >
      <div v-loading="statsLoading" class="stats-container">
        <el-row :gutter="20" v-if="statsData">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ statsData.totalTopics || 0 }}</div>
              <div class="stat-label">总话题数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ statsData.totalQuestions || 0 }}</div>
              <div class="stat-label">总问题数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ statsData.avgQuestionsPerTopic || 0 }}</div>
              <div class="stat-label">平均每话题问题数</div>
            </div>
          </el-col>
        </el-row>

        <el-divider>模块分布</el-divider>
        
        <el-table
          :data="statsData?.moduleStats || []"
          style="width: 100%"
          size="small"
        >
          <el-table-column prop="moduleId" label="模块ID" width="100" />
          <el-table-column prop="moduleName" label="模块名称" />
          <el-table-column prop="topicCount" label="话题数量" width="100" />
          <el-table-column prop="questionCount" label="问题数量" width="100" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 话题详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="话题详情"
      width="800px"
    >
      <div v-if="currentTopic" class="topic-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="话题ID">{{ currentTopic.id }}</el-descriptions-item>
          <el-descriptions-item label="模块ID">{{ currentTopic.moduleId }}</el-descriptions-item>
          <el-descriptions-item label="话题顺序">第{{ currentTopic.order }}个</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentTopic.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="话题标题" :span="2">{{ currentTopic.title }}</el-descriptions-item>
          <el-descriptions-item label="话题描述" :span="2">
            {{ currentTopic.description || '无描述' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>相关问题</el-divider>
        
        <div class="topic-questions">
          <el-table
            v-loading="questionsLoading"
            :data="topicQuestions"
            size="small"
            style="width: 100%"
          >
            <el-table-column prop="QOrder" label="顺序" width="80" />
            <el-table-column prop="promptAudioId" label="音频ID" width="100" />
            <el-table-column prop="answerSeconds" label="回答时间" width="100">
              <template #default="{ row }">{{ row.answerSeconds }}秒</template>
            </el-table-column>
            <el-table-column prop="promptText" label="屏显文本" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  Edit,
  Search,
  Filter,
  Refresh,
  Upload,
  Download,
  DocumentCopy,
  DataAnalysis,
  DataLine
} from '@element-plus/icons-vue'
import {
  getTopics,
  getTopicById,
  getTopicsByModuleId,
  createTopic,
  updateTopic,
  deleteTopic as deleteTopicApi,
  copyTopic as copyTopicApi,
  batchDeleteTopics,
  searchTopics,
  getTopicStats,
  getOverallStats,
  exportTopicsByModule,
  getQuestionsByTopicId
} from '@/api/opi-topic'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const statsLoading = ref(false)
const questionsLoading = ref(false)
const dialogVisible = ref(false)
const filterDialogVisible = ref(false)
const statsDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)

const topicList = ref([])
const moduleList = ref([])
const selectedTopics = ref([])
const statsData = ref(null)
const currentTopic = ref(null)
const topicQuestions = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  moduleId: null
})

// 筛选表单
const filterForm = reactive({
  moduleId: null,
  dateRange: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 排序数据
const sortData = reactive({
  prop: '',
  order: ''
})

// 话题表单
const topicForm = reactive({
  id: null,
  moduleId: null,
  order: 1,
  title: '',
  description: ''
})

// 表单引用
const topicFormRef = ref(null)

// 计算属性
const hasActiveFilters = computed(() => {
  return searchForm.moduleId || searchForm.keyword
})

// 表单验证规则
const topicRules = {
  moduleId: [
    { required: true, message: '请选择模块', trigger: 'change' }
  ],
  order: [
    { required: true, message: '请输入话题顺序', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '话题顺序必须在1-100之间', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入话题标题', trigger: 'blur' },
    { min: 1, max: 200, message: '话题标题长度为1-200个字符', trigger: 'blur' }
  ],
  description: [
    { max: 1000, message: '话题描述不能超过1000个字符', trigger: 'blur' }
  ]
}

// 页面初始化
onMounted(() => {
  loadTopics()
  loadModules()
})

// 获取话题列表
const loadTopics = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    
    // 添加排序
    if (sortData.prop) {
      params.sort = [`${sortData.prop},${sortData.order === 'ascending' ? 'asc' : 'desc'}`]
    }
    
    let response
    
    // 根据搜索条件选择不同的API
    if (searchForm.keyword) {
      response = await searchTopics(searchForm.keyword, params)
    } else if (searchForm.moduleId) {
      response = await getTopicsByModuleId(searchForm.moduleId, params)
    } else {
      response = await getTopics(params)
    }
    
    topicList.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
    // 加载每个话题的问题数量
    await loadQuestionCounts()
  } catch (error) {
    ElMessage.error('获取话题列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 加载问题数量
const loadQuestionCounts = async () => {
  try {
    const promises = topicList.value.map(async (topic) => {
      try {
        const stats = await getTopicStats(topic.id)
        topic.questionCount = stats.data.totalQuestions || 0
      } catch (error) {
        topic.questionCount = 0
      }
    })
    await Promise.all(promises)
  } catch (error) {
    console.error('加载问题数量失败：', error)
  }
}

// 获取模块列表
const loadModules = async () => {
  try {
    // 模拟模块数据，实际应该从后端获取
    moduleList.value = [
      { id: 1, name: '基础模块' },
      { id: 2, name: '中级模块' },
      { id: 3, name: '高级模块' }
    ]
  } catch (error) {
    console.error('获取模块列表失败：', error)
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  resetTopicForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(topicForm, { ...row })
  dialogVisible.value = true
}

// 重置话题表单
const resetTopicForm = () => {
  Object.assign(topicForm, {
    id: null,
    moduleId: null,
    order: 1,
    title: '',
    description: ''
  })
  if (topicFormRef.value) {
    topicFormRef.value.resetFields()
  }
}

// 模块变化处理
const handleModuleChange = (moduleId) => {
  if (moduleId) {
    // 可以根据模块ID获取下一个可用的顺序号
    getNextOrderForModule(moduleId)
  }
}

// 获取模块的下一个可用顺序号
const getNextOrderForModule = async (moduleId) => {
  try {
    const response = await getTopicsByModuleId(moduleId, { size: 1000 })
    const topics = response.data.content || []
    const maxOrder = topics.length > 0 ? Math.max(...topics.map(t => t.order)) : 0
    topicForm.order = maxOrder + 1
  } catch (error) {
    console.error('获取模块话题失败：', error)
  }
}

// 提交话题
const submitTopic = async () => {
  if (!topicFormRef.value) return
  
  try {
    await topicFormRef.value.validate()
    submitting.value = true
    
    const data = { ...topicForm }
    delete data.id
    
    if (isEdit.value) {
      await updateTopic(topicForm.id, data)
      ElMessage.success('话题更新成功')
    } else {
      await createTopic(data)
      ElMessage.success('话题创建成功')
    }
    
    dialogVisible.value = false
    loadTopics()
  } catch (error) {
    if (error.message) {
      ElMessage.error('操作失败：' + error.message)
    }
  } finally {
    submitting.value = false
  }
}

// 删除话题
const deleteTopic = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个话题吗？删除后相关问题也会被删除！', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteTopicApi(id)
    ElMessage.success('话题删除成功')
    loadTopics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 复制话题
const copyTopic = async (id) => {
  try {
    await copyTopicApi(id)
    ElMessage.success('话题复制成功')
    loadTopics()
  } catch (error) {
    ElMessage.error('复制失败：' + (error.message || '未知错误'))
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedTopics.value.length === 0) {
    ElMessage.warning('请选择要删除的话题')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedTopics.value.length} 个话题吗？删除后相关问题也会被删除！`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedTopics.value.map(item => item.id)
    await batchDeleteTopics(ids)
    ElMessage.success('批量删除成功')
    loadTopics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 查看话题详情
const viewTopicDetails = async (topic) => {
  try {
    currentTopic.value = topic
    detailDialogVisible.value = true
    
    // 加载话题相关问题
    questionsLoading.value = true
    const response = await getQuestionsByTopicId(topic.id, { size: 1000 })
    topicQuestions.value = response.data.content || []
  } catch (error) {
    ElMessage.error('获取话题详情失败：' + (error.message || '未知错误'))
  } finally {
    questionsLoading.value = false
  }
}

// 查看问题
const viewQuestions = (topicId) => {
  ElMessage.info(`跳转到话题 ${topicId} 的问题管理页面`)
  // 这里可以路由跳转到问题管理页面并传递topicId参数
}

// 查看统计
const viewStats = async (topicId) => {
  try {
    const response = await getTopicStats(topicId)
    ElMessage.success(`话题统计信息：${JSON.stringify(response.data)}`)
  } catch (error) {
    ElMessage.error('获取统计信息失败：' + (error.message || '未知错误'))
  }
}

// 显示统计对话框
const showStatsDialog = async () => {
  try {
    statsLoading.value = true
    statsDialogVisible.value = true
    
    const response = await getOverallStats()
    statsData.value = response.data
  } catch (error) {
    ElMessage.error('获取统计信息失败：' + (error.message || '未知错误'))
  } finally {
    statsLoading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadTopics()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedTopics.value = selection
}

// 排序变化
const handleSortChange = ({ prop, order }) => {
  sortData.prop = prop
  sortData.order = order
  loadTopics()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadTopics()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.page = page
  loadTopics()
}

// 显示筛选对话框
const showFilterDialog = () => {
  Object.assign(filterForm, searchForm)
  filterDialogVisible.value = true
}

// 应用筛选
const applyFilter = () => {
  Object.assign(searchForm, {
    moduleId: filterForm.moduleId,
    keyword: searchForm.keyword // 保持关键词搜索
  })
  pagination.page = 1
  filterDialogVisible.value = false
  loadTopics()
}

// 重置筛选
const resetFilter = () => {
  Object.assign(filterForm, {
    moduleId: null,
    dateRange: null
  })
}

// 清除单个筛选
const clearFilter = (key) => {
  searchForm[key] = key === 'moduleId' ? null : ''
  loadTopics()
}

// 清除所有筛选
const clearAllFilters = () => {
  Object.assign(searchForm, {
    keyword: '',
    moduleId: null
  })
  loadTopics()
}

// 导入处理
const handleImport = () => {
  ElMessage.info('导入功能正在开发中...')
}

// 导出处理
const handleExport = async () => {
  try {
    if (searchForm.moduleId) {
      await exportTopicsByModule(searchForm.moduleId)
      ElMessage.success('导出成功')
    } else {
      ElMessage.info('导出功能正在开发中...')
    }
  } catch (error) {
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.topic-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.filter-tags {
  margin-bottom: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.table-container {
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.stats-container {
  min-height: 200px;
}

.stat-card {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.topic-detail {
  margin-top: 10px;
}

.topic-questions {
  margin-top: 15px;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}
</style>

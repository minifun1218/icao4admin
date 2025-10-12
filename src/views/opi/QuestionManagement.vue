<template>
  <div class="question-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>口语问题管理</h2>
      <p>管理口语能力面试的问题库</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog" :icon="Plus">
          添加问题
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedQuestions.length === 0"
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
      </div>
      
      <div class="toolbar-right">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索问题..."
          style="width: 250px; margin-right: 10px;"
          clearable
          @keyup.enter="loadQuestions"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="showFilterDialog" :icon="Filter">
          高级筛选
        </el-button>
        <el-button @click="loadQuestions" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选条件显示 -->
    <div v-if="hasActiveFilters" class="filter-tags">
      <span class="filter-label">当前筛选：</span>
      <el-tag
        v-if="searchForm.topicId"
        closable
        @close="clearFilter('topicId')"
        type="info"
      >
        话题ID: {{ searchForm.topicId }}
      </el-tag>
      <el-tag
        v-if="searchForm.hasPromptText !== null"
        closable
        @close="clearFilter('hasPromptText')"
        type="info"
      >
        {{ searchForm.hasPromptText ? '有屏显文本' : '无屏显文本' }}
      </el-tag>
      <el-tag
        v-if="searchForm.minAnswerSeconds"
        closable
        @close="clearFilter('answerTime')"
        type="info"
      >
        回答时间: {{ searchForm.minAnswerSeconds }}-{{ searchForm.maxAnswerSeconds }}秒
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
        :data="questionList"
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
          prop="topicId"
          label="话题ID"
          width="100"
          sortable="custom"
        >
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.topicId }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="QOrder"
          label="问题顺序"
          width="100"
          sortable="custom"
        >
          <template #default="{ row }">
            <el-tag size="small" type="success">第{{ row.QOrder }}题</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="promptAudioId"
          label="音频ID"
          width="100"
        >
          <template #default="{ row }">
            <el-link 
              type="primary" 
              @click="playAudio(row.promptAudioId)"
              :underline="false"
            >
              {{ row.promptAudioId }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="answerSeconds"
          label="回答时间"
          width="100"
          sortable="custom"
        >
          <template #default="{ row }">
            <el-tag 
              size="small" 
              :type="getAnswerTimeTagType(row.answerSeconds)"
            >
              {{ row.answerSeconds }}秒
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="promptText"
          label="屏显文本"
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span v-if="row.promptText">{{ row.promptText }}</span>
            <el-tag v-else size="small" type="info">无</el-tag>
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
          label="操作"
          width="200"
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
              @click="copyQuestion(row.id)"
              :icon="DocumentCopy"
            >
              复制
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteQuestion(row.id)"
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
      :title="isEdit ? '编辑问题' : '添加问题'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="questionForm"
        :model="questionForm"
        :rules="questionRules"
        label-width="120px"
      >
        <el-form-item label="话题ID" prop="topicId">
          <el-select
            v-model="questionForm.topicId"
            placeholder="请选择话题"
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="topic in topicList"
              :key="topic.id"
              :label="`${topic.id} - ${topic.title || '未命名话题'}`"
              :value="topic.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="问题顺序" prop="QOrder">
          <el-input-number
            v-model="questionForm.QOrder"
            :min="1"
            :max="100"
            placeholder="请输入问题顺序"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="音频ID" prop="promptAudioId">
          <el-input-number
            v-model="questionForm.promptAudioId"
            :min="1"
            placeholder="请输入音频ID"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="回答时间(秒)" prop="answerSeconds">
          <el-input-number
            v-model="questionForm.answerSeconds"
            :min="10"
            :max="300"
            placeholder="请输入回答时间"
            style="width: 100%"
          />
          <div class="form-help">推荐：短时间30秒以下，标准30-120秒，长时间120秒以上</div>
        </el-form-item>
        
        <el-form-item label="屏显文本" prop="promptText">
          <el-input
            v-model="questionForm.promptText"
            type="textarea"
            :rows="4"
            placeholder="请输入屏显文本（可选）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion" :loading="submitting">
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
        <el-form-item label="话题ID">
          <el-select
            v-model="filterForm.topicId"
            placeholder="请选择话题"
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="topic in topicList"
              :key="topic.id"
              :label="`${topic.id} - ${topic.title || '未命名话题'}`"
              :value="topic.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="屏显文本">
          <el-select
            v-model="filterForm.hasPromptText"
            placeholder="请选择"
            style="width: 100%"
            clearable
          >
            <el-option label="有屏显文本" :value="true" />
            <el-option label="无屏显文本" :value="false" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="回答时间范围">
          <div style="display: flex; align-items: center; gap: 10px;">
            <el-input-number
              v-model="filterForm.minAnswerSeconds"
              :min="1"
              :max="300"
              placeholder="最小值"
            />
            <span>至</span>
            <el-input-number
              v-model="filterForm.maxAnswerSeconds"
              :min="1"
              :max="300"
              placeholder="最大值"
            />
            <span>秒</span>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="resetFilter">重置</el-button>
        <el-button type="primary" @click="applyFilter">应用筛选</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入问题"
      width="500px"
    >
      <el-upload
        ref="uploadRef"
        :action="uploadAction"
        :auto-upload="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        accept=".xlsx,.xls,.csv"
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <div class="el-upload__tip">
          只能上传 xlsx/xls/csv 文件
        </div>
      </el-upload>
      
      <div class="import-options">
        <el-checkbox v-model="importOptions.skipDuplicates">跳过重复数据</el-checkbox>
        <el-checkbox v-model="importOptions.updateExisting">更新已存在数据</el-checkbox>
        <el-checkbox v-model="importOptions.validateData">验证数据完整性</el-checkbox>
      </div>
      
      <template #footer>
        <el-button @click="downloadTemplate">下载模板</el-button>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading">
          开始导入
        </el-button>
      </template>
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
  UploadFilled
} from '@element-plus/icons-vue'
import {
  getQuestions,
  getQuestionsByTopicId,
  createQuestion,
  updateQuestion,
  deleteQuestion as deleteQuestionApi,
  copyQuestion as copyQuestionApi,
  batchDeleteQuestions,
  getTopics,
  getQuestionsWithPromptText,
  getQuestionsWithoutPromptText,
  getQuestionsByAnswerTime,
  exportOPIQuestionTemplate,
  importOPIQuestions
} from '@/api/opi-question'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const dialogVisible = ref(false)
const filterDialogVisible = ref(false)
const importDialogVisible = ref(false)
const isEdit = ref(false)

const questionList = ref([])
const topicList = ref([])
const selectedQuestions = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  topicId: null,
  hasPromptText: null,
  minAnswerSeconds: null,
  maxAnswerSeconds: null
})

// 筛选表单
const filterForm = reactive({
  topicId: null,
  hasPromptText: null,
  minAnswerSeconds: null,
  maxAnswerSeconds: null
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

// 问题表单
const questionForm = reactive({
  id: null,
  topicId: null,
  QOrder: 1,
  promptAudioId: null,
  answerSeconds: 60,
  promptText: ''
})

// 导入选项
const importOptions = reactive({
  skipDuplicates: false,
  updateExisting: false,
  validateData: true
})

// 表单引用
const questionFormRef = ref(null)
const uploadRef = ref(null)

// 上传地址
const uploadAction = ref('')

// 计算属性
const hasActiveFilters = computed(() => {
  return searchForm.topicId || 
         searchForm.hasPromptText !== null || 
         searchForm.minAnswerSeconds || 
         searchForm.maxAnswerSeconds
})

// 表单验证规则
const questionRules = {
  topicId: [
    { required: true, message: '请选择话题', trigger: 'change' }
  ],
  QOrder: [
    { required: true, message: '请输入问题顺序', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '问题顺序必须在1-100之间', trigger: 'blur' }
  ],
  promptAudioId: [
    { required: true, message: '请输入音频ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '音频ID必须大于0', trigger: 'blur' }
  ],
  answerSeconds: [
    { required: true, message: '请输入回答时间', trigger: 'blur' },
    { type: 'number', min: 10, max: 300, message: '回答时间必须在10-300秒之间', trigger: 'blur' }
  ]
}

// 页面初始化
onMounted(() => {
  loadQuestions()
  loadTopics()
})

// 获取问题列表
const loadQuestions = async () => {
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
    
    // 添加搜索条件
    if (searchForm.topicId) {
      params.topicId = searchForm.topicId
    }
    
    let response
    
    // 根据筛选条件选择不同的API
    if (searchForm.hasPromptText === true) {
      response = await getQuestionsWithPromptText(params)
    } else if (searchForm.hasPromptText === false) {
      response = await getQuestionsWithoutPromptText(params)
    } else if (searchForm.minAnswerSeconds && searchForm.maxAnswerSeconds) {
      response = await getQuestionsByAnswerTime(
        searchForm.minAnswerSeconds,
        searchForm.maxAnswerSeconds,
        params
      )
    } else if (searchForm.topicId) {
      response = await getQuestionsByTopicId(searchForm.topicId, params)
    } else {
      response = await getQuestions(params)
    }
    
    questionList.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('获取问题列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 获取话题列表
const loadTopics = async () => {
  try {
    const response = await getTopics({ size: 1000 })
    topicList.value = response.data.content || []
  } catch (error) {
    console.error('获取话题列表失败：', error)
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  resetQuestionForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(questionForm, { ...row })
  dialogVisible.value = true
}

// 重置问题表单
const resetQuestionForm = () => {
  Object.assign(questionForm, {
    id: null,
    topicId: null,
    QOrder: 1,
    promptAudioId: null,
    answerSeconds: 60,
    promptText: ''
  })
  if (questionFormRef.value) {
    questionFormRef.value.resetFields()
  }
}

// 提交问题
const submitQuestion = async () => {
  if (!questionFormRef.value) return
  
  try {
    await questionFormRef.value.validate()
    submitting.value = true
    
    const data = { ...questionForm }
    delete data.id
    
    if (isEdit.value) {
      await updateQuestion(questionForm.id, data)
      ElMessage.success('问题更新成功')
    } else {
      await createQuestion(data)
      ElMessage.success('问题创建成功')
    }
    
    dialogVisible.value = false
    loadQuestions()
  } catch (error) {
    if (error.message) {
      ElMessage.error('操作失败：' + error.message)
    }
  } finally {
    submitting.value = false
  }
}

// 删除问题
const deleteQuestion = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个问题吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteQuestionApi(id)
    ElMessage.success('问题删除成功')
    loadQuestions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 复制问题
const copyQuestion = async (id) => {
  try {
    await copyQuestionApi(id)
    ElMessage.success('问题复制成功')
    loadQuestions()
  } catch (error) {
    ElMessage.error('复制失败：' + (error.message || '未知错误'))
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要删除的问题')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedQuestions.value.length} 个问题吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedQuestions.value.map(item => item.id)
    await batchDeleteQuestions(ids)
    ElMessage.success('批量删除成功')
    loadQuestions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

// 排序变化
const handleSortChange = ({ prop, order }) => {
  sortData.prop = prop
  sortData.order = order
  loadQuestions()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadQuestions()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.page = page
  loadQuestions()
}

// 显示筛选对话框
const showFilterDialog = () => {
  Object.assign(filterForm, searchForm)
  filterDialogVisible.value = true
}

// 应用筛选
const applyFilter = () => {
  Object.assign(searchForm, filterForm)
  pagination.page = 1
  filterDialogVisible.value = false
  loadQuestions()
}

// 重置筛选
const resetFilter = () => {
  Object.assign(filterForm, {
    topicId: null,
    hasPromptText: null,
    minAnswerSeconds: null,
    maxAnswerSeconds: null
  })
}

// 清除单个筛选
const clearFilter = (key) => {
  if (key === 'answerTime') {
    searchForm.minAnswerSeconds = null
    searchForm.maxAnswerSeconds = null
  } else {
    searchForm[key] = key === 'hasPromptText' ? null : null
  }
  loadQuestions()
}

// 清除所有筛选
const clearAllFilters = () => {
  Object.assign(searchForm, {
    keyword: '',
    topicId: null,
    hasPromptText: null,
    minAnswerSeconds: null,
    maxAnswerSeconds: null
  })
  loadQuestions()
}

// 导入对话框
const handleImport = () => {
  importDialogVisible.value = true
}

// 导出数据
const handleExport = async () => {
  try {
    ElMessage.info('导出功能正在开发中...')
  } catch (error) {
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  }
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await exportOPIQuestionTemplate()
    const blob = new Blob([response], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'opi_question_template.csv'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败：' + (error.message || '未知错误'))
  }
}

// 文件选择变化
const handleFileChange = (file) => {
  // 文件处理逻辑
}

// 上传前检查
const beforeUpload = (file) => {
  const isValidType = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 
                      'application/vnd.ms-excel', 
                      'text/csv'].includes(file.type)
  if (!isValidType) {
    ElMessage.error('只能上传 Excel 或 CSV 文件!')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  
  return true
}

// 开始上传
const handleUpload = async () => {
  // 上传逻辑
  ElMessage.info('上传功能正在开发中...')
}

// 播放音频
const playAudio = (audioId) => {
  ElMessage.info(`播放音频 ID: ${audioId}`)
}

// 获取回答时间标签类型
const getAnswerTimeTagType = (seconds) => {
  if (seconds < 30) return 'warning'
  if (seconds >= 120) return 'danger'
  return 'success'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.question-management {
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

.import-options {
  margin: 20px 0;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.import-options .el-checkbox {
  display: block;
  margin-bottom: 10px;
}

.import-options .el-checkbox:last-child {
  margin-bottom: 0;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}
</style>
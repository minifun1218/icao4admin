<template>
  <div class="question-management">


    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加题目
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedQuestions.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="refreshList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="showImportDialog">
          <el-icon><Upload /></el-icon>
          导入题目
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出题目
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-select
          v-model="filterModule"
          placeholder="选择模块"
          clearable
          style="width: 150px; margin-right: 12px"
          @change="handleFilterChange"
        >
          <el-option label="全部模块" value="" />
          <el-option 
            v-for="module in modules" 
            :key="module.id" 
            :label="module.name" 
            :value="module.id" 
          />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索题目内容..."
          style="width: 250px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>



    <!-- 题目列表 -->
    <div class="question-list">
      <el-table
        v-loading="loading"
        :data="questionList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="textStem" label="题干" min-width="200">
          <template #default="scope">
            <div class="text-stem">
              {{ scope.row.textStem || '暂无题干' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="audioId" label="音频" width="120" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.audioId" 
              type="primary" 
              size="small" 
              @click="playAudio(scope.row.audioId)"
            >
              <el-icon><VideoPlay /></el-icon>
              播放
            </el-button>
            <span v-else class="text-muted">无音频</span>
          </template>
        </el-table-column>
        <el-table-column prop="playOnce" label="播放模式" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.playOnce ? 'danger' : 'success'">
              {{ formatPlayMode(scope.row.playOnce) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="answerSeconds" label="答题时间" width="120" align="center">
          <template #default="scope">
            <span>{{ formatAnswerTime(scope.row.answerSeconds) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="moduleId" label="所属模块" width="150">
          <template #default="scope">
            <span>{{ getModuleName(scope.row.moduleId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" @click="handleCopy(scope.row)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button size="small" @click="handleChoices(scope.row)">
              <el-icon><List /></el-icon>
              选项
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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
    </div>

    <!-- 创建/编辑题目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑题目' : '添加题目'"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="questionFormRef"
        :model="currentQuestion"
        :rules="questionRules"
        label-width="120px"
      >
        <el-form-item label="所属模块" prop="moduleId">
          <el-select v-model="currentQuestion.moduleId" placeholder="请选择模块" style="width: 100%">
            <el-option 
              v-for="module in modules" 
              :key="module.id" 
              :label="module.name" 
              :value="module.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="音频文件" prop="audioId">
          <div class="audio-upload-section">
            <el-upload
              ref="audioUploadRef"
              :file-list="audioFileList"
              :before-upload="beforeAudioUpload"
              :on-change="handleAudioChange"
              :auto-upload="false"
              accept="audio/*"
              :limit="1"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择音频文件
              </el-button>
            </el-upload>
            <div v-if="currentAudioUrl" class="audio-preview">
              <audio :src="currentAudioUrl" controls class="audio-player" />
              <el-button size="small" type="danger" @click="removeAudio">
                <el-icon><Delete /></el-icon>
                移除
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="题干内容" prop="textStem">
          <el-input
            v-model="currentQuestion.textStem"
            type="textarea"
            :rows="4"
            placeholder="请输入题干内容"
          />
        </el-form-item>

        <el-form-item label="播放模式" prop="playOnce">
          <el-radio-group v-model="currentQuestion.playOnce">
            <el-radio :label="true">仅播放一遍</el-radio>
            <el-radio :label="false">可重复播放</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="答题时间" prop="answerSeconds">
          <el-input-number
            v-model="currentQuestion.answerSeconds"
            :min="1"
            :max="300"
            :step="5"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 10px">秒</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveQuestion" :loading="saveLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看题目详情对话框 -->
    <el-dialog v-model="detailVisible" title="题目详情" width="700px">
      <div v-if="currentQuestion" class="question-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目ID">{{ currentQuestion.id }}</el-descriptions-item>
          <el-descriptions-item label="所属模块">{{ getModuleName(currentQuestion.moduleId) }}</el-descriptions-item>
          <el-descriptions-item label="音频ID">{{ currentQuestion.audioId || '无' }}</el-descriptions-item>
          <el-descriptions-item label="播放模式">
            <el-tag :type="currentQuestion.playOnce ? 'danger' : 'success'">
              {{ formatPlayMode(currentQuestion.playOnce) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="答题时间">{{ formatAnswerTime(currentQuestion.answerSeconds) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentQuestion.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="题干内容" :span="2">
            <div class="text-content">{{ currentQuestion.textStem || '暂无题干' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 音频播放 -->
        <div v-if="currentQuestion.audioId" class="audio-section">
          <h4>音频文件</h4>
          <audio v-if="currentAudioUrl" :src="currentAudioUrl" controls class="detail-audio" />
        </div>
      </div>
    </el-dialog>

    <!-- 导入题目对话框 -->
    <el-dialog v-model="importVisible" title="导入题目" width="600px">
      <div class="import-section">
        <el-upload
          ref="importUploadRef"
          :file-list="importFileList"
          :before-upload="beforeImportUpload"
          :on-change="handleImportChange"
          :auto-upload="false"
          accept=".xlsx,.xls,.csv"
          :limit="1"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持 Excel (.xlsx, .xls) 和 CSV 文件格式
            </div>
          </template>
        </el-upload>

        <div class="import-options">
          <el-checkbox v-model="importOptions.skipDuplicates">跳过重复项</el-checkbox>
          <el-checkbox v-model="importOptions.updateExisting">更新已存在的题目</el-checkbox>
          <el-checkbox v-model="importOptions.validateData">验证数据</el-checkbox>
        </div>
      </div>

      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importLoading">
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  Refresh,
  Upload,
  Download,
  Search,
  VideoPlay,
  View,
  Edit,
  CopyDocument,
  List
} from '@element-plus/icons-vue'
import {
  getQuestions,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  batchDeleteQuestions,
  copyQuestion,
  searchQuestions,
  getMCQStatistics,
  uploadAudioFile,
  importQuestions,
  exportQuestions,
  formatAnswerTime,
  formatPlayMode,
  validateQuestionData
} from '@/api/listening-mcq'
import { getMediaList } from '@/api/media'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const importLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const importVisible = ref(false)
const isEditing = ref(false)

const questionList = ref([])
const selectedQuestions = ref([])
const searchKeyword = ref('')
const filterModule = ref('')
const modules = ref([])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const currentQuestion = reactive({
  id: null,
  moduleId: null,
  audioId: null,
  textStem: '',
  playOnce: true,
  answerSeconds: 15
})

// 表单引用
const questionFormRef = ref(null)
const audioUploadRef = ref(null)
const importUploadRef = ref(null)

// 文件相关
const audioFileList = ref([])
const currentAudioUrl = ref('')
const currentAudioFile = ref(null)
const importFileList = ref([])

// 导入选项
const importOptions = reactive({
  skipDuplicates: true,
  updateExisting: false,
  validateData: true
})

// 表单验证规则
const questionRules = {
  moduleId: [
    { required: true, message: '请选择所属模块', trigger: 'change' }
  ],
  audioId: [
    { required: true, message: '请选择音频文件', trigger: 'change' }
  ],
  textStem: [
    { required: true, message: '请输入题干内容', trigger: 'blur' },
    { min: 5, message: '题干内容至少5个字符', trigger: 'blur' }
  ],
  answerSeconds: [
    { required: true, message: '请设置答题时间', trigger: 'blur' },
    { type: 'number', min: 1, max: 300, message: '答题时间必须在1-300秒之间', trigger: 'blur' }
  ]
}

// 计算属性
const formatDateTime = computed(() => {
  return (dateTime) => {
    if (!dateTime) return '-'
    return new Date(dateTime).toLocaleString('zh-CN')
  }
})

// 方法
const loadQuestionList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    
    if (filterModule.value) {
      params.moduleId = filterModule.value
    }
    
    const response = await getQuestions(params)
    
    if (response && response.data) {
      questionList.value = response.data.data.content || [];

      pagination.total = response.data.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载题目列表失败')
    console.error('加载题目列表错误:', error)
  } finally {
    loading.value = false
  }
}

const loadModules = async () => {
  try {
    // TODO: 实现模块列表加载
    modules.value = [
      { id: 1, name: '基础听力理解' },
      { id: 2, name: '中级听力理解' },
      { id: 3, name: '高级听力理解' }
    ]
  } catch (error) {
    console.error('加载模块列表错误:', error)
  }
}

const getModuleName = (moduleId) => {
  const module = modules.value.find(m => m.id === moduleId)
  return module ? module.name : '未知模块'
}

const showCreateDialog = () => {
  isEditing.value = false
  resetCurrentQuestion()
  dialogVisible.value = true
}

const handleView = (question) => {
  Object.assign(currentQuestion, question)
  loadAudioForQuestion(question)
  detailVisible.value = true
}

const handleEdit = (question) => {
  isEditing.value = true
  Object.assign(currentQuestion, question)
  loadAudioForQuestion(question)
  dialogVisible.value = true
}

const handleCopy = async (question) => {
  try {
    await copyQuestion(question.id)
    ElMessage.success('题目复制成功')
    refreshList()
  } catch (error) {
    ElMessage.error('题目复制失败')
    console.error('复制题目错误:', error)
  }
}

const handleChoices = (question) => {
  // TODO: 跳转到选项管理页面
  ElMessage.info('选项管理功能待实现')
}

const handleDelete = (question) => {
  ElMessageBox.confirm(
    `确定要删除题目"${question.textStem || question.id}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteQuestion(question.id)
      ElMessage.success('题目删除成功')
      refreshList()
    } catch (error) {
      ElMessage.error('题目删除失败')
      console.error('删除题目错误:', error)
    }
  })
}

const handleBatchDelete = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要删除的题目')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedQuestions.value.length} 个题目吗？`,
    '批量删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const questionIds = selectedQuestions.value.map(q => q.id)
      await batchDeleteQuestions(questionIds)
      ElMessage.success('批量删除成功')
      refreshList()
    } catch (error) {
      ElMessage.error('批量删除失败')
      console.error('批量删除错误:', error)
    }
  })
}

const handleSaveQuestion = async () => {
  if (!questionFormRef.value) return
  
  try {
    await questionFormRef.value.validate()
    
    // 验证题目数据
    const errors = validateQuestionData(currentQuestion)
    if (errors.length > 0) {
      ElMessage.error(errors[0])
      return
    }
    
    saveLoading.value = true
    
    if (isEditing.value) {
      await updateQuestion(currentQuestion.id, currentQuestion)
      ElMessage.success('题目更新成功')
    } else {
      await createQuestion(currentQuestion)
      ElMessage.success('题目创建成功')
    }
    
    dialogVisible.value = false
    refreshList()
  } catch (error) {
    ElMessage.error(isEditing.value ? '题目更新失败' : '题目创建失败')
    console.error('保存题目错误:', error)
  } finally {
    saveLoading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    refreshList()
    return
  }
  
  try {
    loading.value = true
    const params = {
      page: 0,
      size: pagination.size
    }
    
    const response = await searchQuestions(searchKeyword.value, params)
    
    if (response && response.data) {
      questionList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
      pagination.page = 1
    }
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error('搜索错误:', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.page = 1
  loadQuestionList()
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadQuestionList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadQuestionList()
}

const refreshList = () => {
  loadQuestionList()

}

const resetForm = () => {
  if (questionFormRef.value) {
    questionFormRef.value.resetFields()
  }
  resetCurrentQuestion()
  resetAudio()
}

const resetCurrentQuestion = () => {
  Object.assign(currentQuestion, {
    id: null,
    moduleId: null,
    audioId: null,
    textStem: '',
    playOnce: true,
    answerSeconds: 15
  })
}

// 音频相关方法
const handleAudioChange = (file) => {
  currentAudioFile.value = file.raw
  const url = URL.createObjectURL(file.raw)
  currentAudioUrl.value = url
  audioFileList.value = [file]
}

const beforeAudioUpload = (file) => {
  const isAudio = file.type.startsWith('audio/')
  if (!isAudio) {
    ElMessage.error('只能上传音频文件')
    return false
  }
  
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('音频文件大小不能超过50MB')
    return false
  }
  
  return false // 阻止自动上传
}

const removeAudio = () => {
  currentAudioFile.value = null
  currentAudioUrl.value = ''
  audioFileList.value = []
  currentQuestion.audioId = null
}

const loadAudioForQuestion = async (question) => {
  if (question.audioId) {
    try {
      // TODO: 根据audioId加载音频文件URL
      currentAudioUrl.value = `/api/media/preview/audio_${question.audioId}.mp3`
    } catch (error) {
      console.error('加载音频错误:', error)
    }
  }
}

const playAudio = (audioId) => {
  // TODO: 实现音频播放逻辑
  ElMessage.info(`播放音频 ID: ${audioId}`)
}

// 导入相关方法
const showImportDialog = () => {
  importVisible.value = true
}

const handleImportChange = (file) => {
  importFileList.value = [file]
}

const beforeImportUpload = (file) => {
  const validTypes = [
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-excel',
    'text/csv'
  ]
  
  if (!validTypes.includes(file.type)) {
    ElMessage.error('只支持Excel和CSV文件')
    return false
  }
  
  return false // 阻止自动上传
}

const handleImport = async () => {
  if (importFileList.value.length === 0) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  try {
    importLoading.value = true
    const formData = new FormData()
    formData.append('file', importFileList.value[0].raw)
    formData.append('skipDuplicates', importOptions.skipDuplicates)
    formData.append('updateExisting', importOptions.updateExisting)
    formData.append('validateData', importOptions.validateData)
    
    const response = await importQuestions(formData)
    
    if (response && response.data) {
      const result = response.data
      ElMessage.success(`导入完成：成功${result.successCount}条，失败${result.failureCount}条`)
      importVisible.value = false
      refreshList()
    }
  } catch (error) {
    ElMessage.error('导入失败')
    console.error('导入错误:', error)
  } finally {
    importLoading.value = false
  }
}

const handleExport = async () => {
  try {
    const params = {}
    if (filterModule.value) {
      params.moduleId = filterModule.value
    }
    
    const response = await exportQuestions(params)
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `listening_mcq_questions_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error('导出错误:', error)
  }
}

// 生命周期
onMounted(() => {
  loadQuestionList()
  loadModules()
})
</script>

<style scoped>
.question-management {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.toolbar {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.statistics {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.question-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.text-stem {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.audio-upload-section {
  width: 100%;
}

.audio-preview {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.audio-player {
  width: 300px;
}

.detail-audio {
  width: 100%;
  margin-top: 12px;
}

.question-detail {
  padding: 16px 0;
}

.text-content {
  line-height: 1.6;
  color: #303133;
}

.audio-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.audio-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.import-section {
  padding: 16px 0;
}

.import-options {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.import-options .el-checkbox {
  display: block;
  margin-bottom: 12px;
}
</style>

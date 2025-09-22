<template>
  <div class="listening-dialog-panel">
    <!-- 操作栏 -->
    <div class="panel-header">
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索对话标题或内容"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.difficulty" placeholder="难度等级" clearable>
              <el-option
                v-for="level in difficultyLevels"
                :key="level.value"
                :label="level.label"
                :value="level.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="action-section">
        <el-button type="success" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加听力对话
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="!hasSelection">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 对话列表 -->
    <div class="dialog-list">
      <el-table
        v-loading="loading"
        :data="filteredDialogs"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        stripe
        class="dialog-table"
        empty-text="暂无听力对话数据"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="expand" width="30">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="dialog-details">
                <div class="detail-item">
                  <span class="label">音频文件:</span>
                  <span class="value">
                    <el-tag v-if="row.audioUrl" type="success" size="small">
                      <el-icon><Service /></el-icon>
                      有音频
                    </el-tag>
                    <el-tag v-else type="danger" size="small">无音频</el-tag>
                  </span>
                </div>
                <div class="detail-item">
                  <span class="label">对话内容:</span>
                  <span class="value">{{ row.content || '暂无内容' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">问题数量:</span>
                  <span class="value">{{ row.questionCount || 0 }}道</span>
                </div>
              </div>
              
              <!-- 问题列表 -->
              <div class="questions-section" v-if="row.questions && row.questions.length > 0">
                <h4>单选题题干 ({{ row.questions.length }}道)</h4>
                <el-table :data="row.questions" size="small">
                  <el-table-column prop="questionText" label="题干" min-width="200" />
                  <el-table-column prop="choiceCount" label="选项数" width="80" align="center" />
                  <el-table-column label="操作" width="200" align="center">
                    <template #default="{ row: question }">
                      <el-button type="primary" size="small" @click="editQuestion(question)">
                        编辑题干
                      </el-button>
                      <el-button type="success" size="small" @click="manageChoices(question)">
                        管理选项
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="对话信息" min-width="300">
          <template #default="{ row }">
            <div class="dialog-info">
              <div class="dialog-title">{{ row.title || '未命名对话' }}</div>
              <div class="dialog-meta">
                <el-tag v-if="row.audioUrl" size="small" type="success">
                  <el-icon><Service /></el-icon>
                  音频
                </el-tag>
                <el-tag 
                  :type="getDifficultyTagType(row.difficulty)"
                  size="small"
                >
                  {{ formatDifficultyLevel(row.difficulty) }}
                </el-tag>
                <span class="question-count">{{ row.questionCount || 0 }}道题</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="toggleStatus(row)"
              active-color="#67C23A"
              inactive-color="#F56C6C"
            />
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="editDialog(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="success" size="small" @click="addQuestion(row)">
                <el-icon><Plus /></el-icon>
                添加题干
              </el-button>
              <el-button type="info" size="small" @click="previewDialog(row)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button type="danger" size="small" @click="deleteDialog(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/编辑对话对话框 -->
    <el-dialog
      v-model="dialogFormVisible"
      :title="dialogForm.id ? '编辑听力对话' : '创建听力对话'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="120px"
      >
        <el-form-item label="对话标题" prop="title">
          <el-input v-model="dialogForm.title" placeholder="请输入对话标题" />
        </el-form-item>
        
        <el-form-item label="对话内容" prop="content">
          <el-input
            v-model="dialogForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入对话内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="dialogForm.difficulty" placeholder="选择难度等级">
            <el-option
              v-for="level in difficultyLevels"
              :key="level.value"
              :label="level.label"
              :value="level.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="显示顺序">
          <el-input-number
            v-model="dialogForm.displayOrder"
            :min="1"
            :max="9999"
            placeholder="显示顺序"
          />
        </el-form-item>
        
        <el-form-item label="音频文件">
          <div class="audio-upload-section">
            <el-upload
              ref="audioUploadRef"
              :action="uploadUrl"
              :before-upload="beforeAudioUpload"
              :on-success="handleAudioUploadSuccess"
              :on-error="handleAudioUploadError"
              :show-file-list="false"
              accept="audio/*"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                上传音频
              </el-button>
            </el-upload>
            
            <div v-if="dialogForm.audioUrl" class="audio-preview">
              <audio :src="dialogForm.audioUrl" controls style="width: 100%; margin-top: 10px;" />
              <el-button type="danger" size="small" @click="removeAudio" style="margin-top: 10px;">
                移除音频
              </el-button>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-switch
            v-model="dialogForm.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="dialogForm.remark"
            type="textarea"
            :rows="2"
            placeholder="备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDialog" :loading="saving">
          {{ dialogForm.id ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 问题管理对话框 -->
    <ListeningQuestionPanel
      v-if="questionPanelVisible"
      :visible="questionPanelVisible"
      :dialog-id="currentDialogId"
      :questions="currentDialogQuestions"
      @close="questionPanelVisible = false"
      @refresh="loadDialogQuestions"
    />

    <!-- 选项管理对话框 -->
    <ListeningChoicePanel
      v-if="choicePanelVisible"
      :visible="choicePanelVisible"
      :question-id="currentQuestionId"
      :choices="currentQuestionChoices"
      @close="choicePanelVisible = false"
      @refresh="loadQuestionChoices"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionBankApi } from '@/api/questionBank'
import ListeningQuestionPanel from './ListeningQuestionPanel.vue'
import ListeningChoicePanel from './ListeningChoicePanel.vue'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  View,
  Upload,
  Service
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['selection-change', 'refresh'])

// 响应式数据
const dialogFormVisible = ref(false)
const questionPanelVisible = ref(false)
const choicePanelVisible = ref(false)
const saving = ref(false)

const dialogFormRef = ref(null)
const audioUploadRef = ref(null)

const currentDialogId = ref(null)
const currentQuestionId = ref(null)
const currentDialogQuestions = ref([])
const currentQuestionChoices = ref([])

const dialogs = ref([])
const selectedDialogs = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  difficulty: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 对话表单
const dialogForm = reactive({
  id: null,
  title: '',
  content: '',
  audioUrl: '',
  difficulty: 3,
  displayOrder: 1,
  enabled: true,
  remark: ''
})

// 计算属性
const difficultyLevels = computed(() => questionBankApi.getDifficultyLevels())

const filteredDialogs = computed(() => {
  let filtered = dialogs.value

  // 关键词搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filtered = filtered.filter(dialog => 
      dialog.title?.toLowerCase().includes(keyword) ||
      dialog.content?.toLowerCase().includes(keyword)
    )
  }

  // 难度筛选
  if (searchForm.difficulty !== null) {
    filtered = filtered.filter(dialog => dialog.difficulty === searchForm.difficulty)
  }

  // 更新分页总数
  pagination.total = filtered.length

  // 分页
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filtered.slice(start, end)
})

const hasSelection = computed(() => selectedDialogs.value.length > 0)

const uploadUrl = computed(() => '/api/listening-mcq/question-bank/upload/audio')

// 表单验证规则
const dialogRules = {
  title: [
    { required: true, message: '请输入对话标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度为2-200个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入对话内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度为10-2000个字符', trigger: 'blur' }
  ],
  difficulty: [
    { required: true, message: '请选择难度等级', trigger: 'change' }
  ]
}

// 方法
const loadDialogs = async () => {
  try {
    const response = await questionBankApi.getListeningDialogs({
      page: pagination.page - 1,
      size: pagination.size
    })
    
    dialogs.value = response.data?.content || []
    pagination.total = response.data?.totalElements || 0
    
    // 加载每个对话的问题数量
    await Promise.all(dialogs.value.map(async (dialog) => {
      try {
        const questionsResponse = await questionBankApi.getListeningDialogQuestions(dialog.id)
        dialog.questions = questionsResponse.data?.content || []
        dialog.questionCount = dialog.questions.length
      } catch (error) {
        dialog.questionCount = 0
      }
    }))
    
  } catch (error) {
    console.error('加载对话列表失败:', error)
    ElMessage.error('加载对话列表失败')
  }
}

const showCreateDialog = () => {
  resetDialogForm()
  dialogFormVisible.value = true
}

const editDialog = (dialog) => {
  Object.assign(dialogForm, {
    ...dialog,
    enabled: dialog.enabled !== false
  })
  dialogFormVisible.value = true
}

const saveDialog = async () => {
  if (!dialogFormRef.value) return
  
  try {
    await dialogFormRef.value.validate()
    saving.value = true
    
    const data = { ...dialogForm }
    
    if (dialogForm.id) {
      await questionBankApi.updateListeningDialog(dialogForm.id, data)
      ElMessage.success('对话更新成功')
    } else {
      await questionBankApi.createListeningDialog(data)
      ElMessage.success('对话创建成功')
    }
    
    dialogFormVisible.value = false
    await loadDialogs()
    emit('refresh')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const deleteDialog = async (dialog) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除对话"${dialog.title}"吗？此操作将同时删除相关的所有问题和选项。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await questionBankApi.deleteListeningDialog(dialog.id)
    ElMessage.success('删除成功')
    await loadDialogs()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const addQuestion = (dialog) => {
  currentDialogId.value = dialog.id
  currentDialogQuestions.value = dialog.questions || []
  questionPanelVisible.value = true
}

const editQuestion = (question) => {
  // 编辑问题逻辑
  ElMessage.info('编辑问题功能开发中...')
}

const manageChoices = (question) => {
  currentQuestionId.value = question.id
  loadQuestionChoices(question.id)
  choicePanelVisible.value = true
}

const loadDialogQuestions = async (dialogId) => {
  try {
    const response = await questionBankApi.getListeningDialogQuestions(dialogId)
    currentDialogQuestions.value = response.data?.content || []
    
    // 更新对话中的问题数据
    const dialog = dialogs.value.find(d => d.id === dialogId)
    if (dialog) {
      dialog.questions = currentDialogQuestions.value
      dialog.questionCount = currentDialogQuestions.value.length
    }
  } catch (error) {
    console.error('加载问题失败:', error)
    ElMessage.error('加载问题失败')
  }
}

const loadQuestionChoices = async (questionId) => {
  try {
    const response = await questionBankApi.getListeningDialogQuestionChoices(questionId)
    currentQuestionChoices.value = response.data || []
  } catch (error) {
    console.error('加载选项失败:', error)
    ElMessage.error('加载选项失败')
  }
}

const toggleStatus = async (dialog) => {
  try {
    await questionBankApi.updateListeningDialog(dialog.id, { enabled: dialog.enabled })
    ElMessage.success(`已${dialog.enabled ? '启用' : '禁用'}对话`)
  } catch (error) {
    dialog.enabled = !dialog.enabled // 回滚状态
    ElMessage.error('状态更新失败')
  }
}

const previewDialog = (dialog) => {
  ElMessage.info('预览功能开发中...')
}

const batchDelete = async () => {
  if (selectedDialogs.value.length === 0) {
    ElMessage.warning('请选择要删除的对话')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedDialogs.value.length} 个对话吗？`,
      '批量删除确认',
      { type: 'warning' }
    )
    
    const dialogIds = selectedDialogs.value.map(dialog => dialog.id)
    await questionBankApi.batchDeleteListeningDialogs(dialogIds)
    
    ElMessage.success(`成功删除 ${selectedDialogs.value.length} 个对话`)
    selectedDialogs.value = []
    await loadDialogs()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleSearch = () => {
  pagination.page = 1
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    difficulty: null
  })
  pagination.page = 1
}

const handleSelectionChange = (selection) => {
  selectedDialogs.value = selection
  emit('selection-change', selection)
}

const handleRowClick = (row) => {
  // 可以在这里添加行点击逻辑
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadDialogs()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadDialogs()
}

const resetDialogForm = () => {
  Object.assign(dialogForm, {
    id: null,
    title: '',
    content: '',
    audioUrl: '',
    difficulty: 3,
    displayOrder: 1,
    enabled: true,
    remark: ''
  })
  
  if (dialogFormRef.value) {
    dialogFormRef.value.clearValidate()
  }
}

// 音频上传相关方法
const beforeAudioUpload = (file) => {
  const validation = questionBankApi.validateAudioFile(file)
  if (!validation.valid) {
    ElMessage.error(validation.message)
    return false
  }
  return true
}

const handleAudioUploadSuccess = (response) => {
  if (response.success) {
    dialogForm.audioUrl = response.data.url
    ElMessage.success('音频上传成功')
  } else {
    ElMessage.error('音频上传失败')
  }
}

const handleAudioUploadError = () => {
  ElMessage.error('音频上传失败')
}

const removeAudio = () => {
  dialogForm.audioUrl = ''
  ElMessage.success('音频已移除')
}

// 工具方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatDifficultyLevel = (level) => {
  return questionBankApi.formatDifficultyLevel(level)
}

const getDifficultyTagType = (level) => {
  const typeMap = {
    1: 'success',
    2: 'success',
    3: 'warning',
    4: 'warning',
    5: 'danger',
    6: 'info'
  }
  return typeMap[level] || 'info'
}

// 生命周期
onMounted(() => {
  loadDialogs()
})
</script>

<style scoped>
.listening-dialog-panel {
  padding: 0;
}

.panel-header {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.search-section {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.action-section {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.dialog-table {
  border-radius: 8px;
  overflow: hidden;
}

.dialog-info {
  padding: 8px 0;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.dialog-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.question-count {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.expand-content {
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.dialog-details {
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  margin-bottom: 8px;
}

.detail-item .label {
  font-weight: 600;
  width: 100px;
  color: #606266;
}

.detail-item .value {
  flex: 1;
  color: #303133;
}

.questions-section h4 {
  margin: 16px 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.audio-upload-section {
  width: 100%;
}

.audio-preview {
  margin-top: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .action-section {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .dialog-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>

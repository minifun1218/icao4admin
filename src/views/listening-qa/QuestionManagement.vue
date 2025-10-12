<template>
  <div class="question-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>
            <el-button text @click="goBackToDialogs">
              <el-icon><ArrowLeft /></el-icon>
              返回对话列表
            </el-button>
          </el-breadcrumb-item>
        </el-breadcrumb>
        <h1>问题管理</h1>
        <el-tag v-if="currentDialogTitle" type="info" class="ml-2">
          {{ currentDialogTitle }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateQuestion = true">
          <el-icon><Plus /></el-icon>
          新建问题
        </el-button>
        <el-button @click="showImportQuestion = true">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="exportQuestions">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索问题内容..."
            @input="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.questionType" placeholder="问题类型" clearable @change="handleFilter">
            <el-option
              v-for="type in questionTypeOptions"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.difficultyLevel" placeholder="难度等级" clearable @change="handleFilter">
            <el-option
              v-for="level in difficultyLevelOptions"
              :key="level.value"
              :label="level.label"
              :value="level.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.isActive" placeholder="状态" clearable @change="handleFilter">
            <el-option label="激活" :value="true" />
            <el-option label="未激活" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedQuestions.length > 0">
      <el-alert
        :title="`已选择 ${selectedQuestions.length} 项`"
        type="info"
        show-icon
        :closable="false"
      >
        <template #default>
          <el-button size="small" @click="batchActivate">批量激活</el-button>
          <el-button size="small" @click="batchDeactivate">批量停用</el-button>
          <el-button size="small" @click="batchUpdateDifficulty">批量设置难度</el-button>
          <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
        </template>
      </el-alert>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="questionList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        
        <el-table-column label="问题内容" min-width="300" sortable="custom">
          <template #default="scope">
            <div class="question-content">
              <el-text class="question-text" @click="viewQuestion(scope.row)" style="cursor: pointer; color: #409eff;">
                {{ scope.row.questionText }}
              </el-text>
              <div class="question-meta mt-1">
                <el-tag size="small" :type="getQuestionTypeTagType(scope.row.questionType)">
                  {{ getQuestionTypeDescription(scope.row.questionType) }}
                </el-tag>
                <el-tag v-if="scope.row.tags" size="small" type="info" class="ml-1">
                  {{ scope.row.tags }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="选项" width="200" v-if="hasMultipleChoiceQuestions">
          <template #default="scope">
            <div v-if="isMultipleChoice(scope.row)" class="options-preview">
              <div v-if="scope.row.optionA" class="option-item">
                <span class="option-label">A.</span>
                <span class="option-text">{{ scope.row.optionA.substring(0, 20) }}...</span>
              </div>
              <div v-if="scope.row.optionB" class="option-item">
                <span class="option-label">B.</span>
                <span class="option-text">{{ scope.row.optionB.substring(0, 20) }}...</span>
              </div>
              <el-text v-if="scope.row.optionC || scope.row.optionD" type="info" size="small">
                +{{ getOptionCount(scope.row) - 2 }}个选项
              </el-text>
            </div>
            <el-text v-else type="info">-</el-text>
          </template>
        </el-table-column>

        <el-table-column prop="correctAnswer" label="正确答案" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.correctAnswer" type="success" size="small">
              {{ scope.row.correctAnswer }}
            </el-tag>
            <el-text v-else type="info">-</el-text>
          </template>
        </el-table-column>

        <el-table-column prop="points" label="分值" width="80" align="center" sortable="custom">
          <template #default="scope">
            <el-text>{{ formatPoints(scope.row.points) }}</el-text>
          </template>
        </el-table-column>

        <el-table-column prop="difficultyLevel" label="难度" width="100" align="center" sortable="custom">
          <template #default="scope">
            <el-tag :type="getDifficultyTagType(scope.row.difficultyLevel)" size="small">
              {{ getDifficultyDescription(scope.row.difficultyLevel) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="displayOrder" label="顺序" width="80" align="center" sortable="custom" />

        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isActive"
              @change="toggleQuestionStatus(scope.row)"
              :disabled="!hasPermission('ADMIN')"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewQuestion(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="editQuestion(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              @click="copyQuestion(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteQuestionAction(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
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

    <!-- 创建/编辑问题对话框 -->
    <el-dialog
      v-model="showCreateQuestion"
      :title="isEdit ? '编辑问题' : '新建问题'"
      width="900px"
      @close="resetQuestionForm"
    >
      <el-form
        ref="questionFormRef"
        :model="questionForm"
        :rules="questionRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="问题类型" prop="questionType">
              <el-select v-model="questionForm.questionType" placeholder="请选择问题类型" style="width: 100%">
                <el-option
                  v-for="type in questionTypeOptions"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-select v-model="questionForm.difficultyLevel" placeholder="请选择难度等级" style="width: 100%">
                <el-option
                  v-for="level in difficultyLevelOptions"
                  :key="level.value"
                  :label="level.label"
                  :value="level.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="问题文本" prop="questionText">
          <el-input
            v-model="questionForm.questionText"
            type="textarea"
            :rows="4"
            placeholder="请输入问题内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <!-- 选择题选项 -->
        <div v-if="isMultipleChoiceType(questionForm.questionType)">
          <el-form-item label="选项A" prop="optionA">
            <el-input
              v-model="questionForm.optionA"
              placeholder="请输入选项A内容"
              maxlength="500"
            />
          </el-form-item>
          <el-form-item label="选项B" prop="optionB">
            <el-input
              v-model="questionForm.optionB"
              placeholder="请输入选项B内容"
              maxlength="500"
            />
          </el-form-item>
          <el-form-item label="选项C">
            <el-input
              v-model="questionForm.optionC"
              placeholder="请输入选项C内容（可选）"
              maxlength="500"
            />
          </el-form-item>
          <el-form-item label="选项D">
            <el-input
              v-model="questionForm.optionD"
              placeholder="请输入选项D内容（可选）"
              maxlength="500"
            />
          </el-form-item>
        </div>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="正确答案" prop="correctAnswer">
              <el-input
                v-model="questionForm.correctAnswer"
                placeholder="正确答案"
                maxlength="10"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分值" prop="points">
              <el-input-number
                v-model="questionForm.points"
                :min="0"
                :max="100"
                :precision="1"
                placeholder="分值"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="显示顺序" prop="displayOrder">
              <el-input-number
                v-model="questionForm.displayOrder"
                :min="1"
                placeholder="显示顺序"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="答案解析">
          <el-input
            v-model="questionForm.answerExplanation"
            type="textarea"
            :rows="3"
            placeholder="请输入答案解析（可选）"
            maxlength="1000"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="questionForm.tags" placeholder="多个标签用逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="questionForm.isActive"
                active-text="激活"
                inactive-text="停用"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateQuestion = false">取消</el-button>
          <el-button type="primary" @click="submitQuestion" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看问题详情对话框 -->
    <el-dialog
      v-model="showViewQuestion"
      title="问题详情"
      width="800px"
    >
      <div v-if="currentQuestion" class="question-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="问题类型">
            {{ getQuestionTypeDescription(currentQuestion.questionType) }}
          </el-descriptions-item>
          <el-descriptions-item label="难度等级">
            {{ getDifficultyDescription(currentQuestion.difficultyLevel) }}
          </el-descriptions-item>
          <el-descriptions-item label="分值">
            {{ formatPoints(currentQuestion.points) }}
          </el-descriptions-item>
          <el-descriptions-item label="显示顺序">
            {{ currentQuestion.displayOrder }}
          </el-descriptions-item>
          <el-descriptions-item label="正确答案" v-if="currentQuestion.correctAnswer">
            {{ currentQuestion.correctAnswer }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentQuestion.isActive ? 'success' : 'danger'">
              {{ currentQuestion.isActive ? '激活' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentQuestion.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(currentQuestion.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="mt-4">
          <h4>问题内容</h4>
          <el-text class="question-text">{{ currentQuestion.questionText }}</el-text>
        </div>

        <div v-if="isMultipleChoice(currentQuestion)" class="mt-4">
          <h4>选项</h4>
          <div class="options-list">
            <div v-if="currentQuestion.optionA" class="option-item">
              <span class="option-label">A.</span>
              <span class="option-text">{{ currentQuestion.optionA }}</span>
            </div>
            <div v-if="currentQuestion.optionB" class="option-item">
              <span class="option-label">B.</span>
              <span class="option-text">{{ currentQuestion.optionB }}</span>
            </div>
            <div v-if="currentQuestion.optionC" class="option-item">
              <span class="option-label">C.</span>
              <span class="option-text">{{ currentQuestion.optionC }}</span>
            </div>
            <div v-if="currentQuestion.optionD" class="option-item">
              <span class="option-label">D.</span>
              <span class="option-text">{{ currentQuestion.optionD }}</span>
            </div>
          </div>
        </div>

        <div v-if="currentQuestion.answerExplanation" class="mt-4">
          <h4>答案解析</h4>
          <el-text class="answer-explanation">{{ currentQuestion.answerExplanation }}</el-text>
        </div>

        <div v-if="currentQuestion.tags" class="mt-4">
          <h4>标签</h4>
          <el-tag v-for="tag in currentQuestion.tags.split(',')" :key="tag" class="mr-1">
            {{ tag.trim() }}
          </el-tag>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showViewQuestion = false">关闭</el-button>
          <el-button type="primary" @click="editQuestion(currentQuestion)" v-if="hasPermission('ADMIN')">
            编辑问题
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量设置难度对话框 -->
    <el-dialog
      v-model="showBatchDifficulty"
      title="批量设置难度"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="难度等级">
          <el-select v-model="batchDifficultyLevel" placeholder="请选择难度等级" style="width: 100%">
            <el-option
              v-for="level in difficultyLevelOptions"
              :key="level.value"
              :label="level.label"
              :value="level.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchDifficulty = false">取消</el-button>
          <el-button type="primary" @click="submitBatchDifficulty" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  ArrowLeft,
  Plus,
  Search,
  Refresh,
  Upload,
  Download,
  View,
  Edit,
  Delete,
  DocumentCopy
} from '@element-plus/icons-vue'

// API导入
import {
  getQuestionsByDialogId,
  getQuestionById,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  batchDeleteQuestions,
  getDifficultyDescription,
  getQuestionTypeDescription,
  getQuestionTypeOptions,
  getDifficultyLevelOptions,
  validateQuestionData,
  generateQuestionTemplate
} from '@/api/lsa-dialogs'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 获取对话ID
const dialogId = computed(() => route.params.dialogId)
const currentDialogTitle = computed(() => route.query.dialogTitle)

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const questionList = ref([])
const selectedQuestions = ref([])
const currentQuestion = ref(null)

// 搜索和筛选
const searchKeyword = ref('')
const filterParams = reactive({
  questionType: null,
  difficultyLevel: null,
  isActive: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 排序
const sortParams = reactive({
  prop: 'displayOrder',
  order: 'ascending'
})

// 对话框状态
const showCreateQuestion = ref(false)
const showViewQuestion = ref(false)
const showImportQuestion = ref(false)
const showBatchDifficulty = ref(false)
const isEdit = ref(false)

// 表单
const questionFormRef = ref(null)
const questionForm = reactive(generateQuestionTemplate())
const questionRules = {
  questionType: [
    { required: true, message: '请选择问题类型', trigger: 'change' }
  ],
  questionText: [
    { required: true, message: '请输入问题内容', trigger: 'blur' },
    { max: 2000, message: '问题内容长度不能超过2000个字符', trigger: 'blur' }
  ],
  optionA: [
    { 
      validator: (rule, value, callback) => {
        if (isMultipleChoiceType(questionForm.questionType) && !value) {
          callback(new Error('选择题必须填写选项A'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  optionB: [
    { 
      validator: (rule, value, callback) => {
        if (isMultipleChoiceType(questionForm.questionType) && !value) {
          callback(new Error('选择题必须填写选项B'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  correctAnswer: [
    { required: true, message: '请输入正确答案', trigger: 'blur' }
  ],
  points: [
    { required: true, message: '请设置分值', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '分值必须在0-100之间', trigger: 'blur' }
  ],
  displayOrder: [
    { required: true, message: '请设置显示顺序', trigger: 'blur' },
    { type: 'number', min: 1, message: '显示顺序必须为正数', trigger: 'blur' }
  ]
}

// 批量难度设置
const batchDifficultyLevel = ref(null)

// 选项数据
const questionTypeOptions = ref(getQuestionTypeOptions())
const difficultyLevelOptions = ref(getDifficultyLevelOptions())

// 计算属性
const hasMultipleChoiceQuestions = computed(() => {
  return questionList.value.some(q => isMultipleChoice(q))
})

// 权限检查
const hasPermission = (role) => {
  return authStore.hasRole(role)
}

// 工具函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const formatPoints = (points) => {
  if (!points) return '0分'
  return points % 1 === 0 ? `${points}分` : `${points.toFixed(1)}分`
}

const isMultipleChoice = (question) => {
  return question.questionType === 'MULTIPLE_CHOICE' || question.questionType === 'MC'
}

const isMultipleChoiceType = (questionType) => {
  return questionType === 'MULTIPLE_CHOICE' || questionType === 'MC'
}

const getOptionCount = (question) => {
  let count = 0
  if (question.optionA) count++
  if (question.optionB) count++
  if (question.optionC) count++
  if (question.optionD) count++
  return count
}

const getQuestionTypeTagType = (questionType) => {
  switch (questionType) {
    case 'MULTIPLE_CHOICE':
    case 'MC':
      return 'primary'
    case 'FILL_IN_BLANK':
    case 'FIB':
      return 'success'
    case 'SHORT_ANSWER':
    case 'SA':
      return 'warning'
    case 'TRUE_FALSE':
    case 'TF':
      return 'info'
    default:
      return 'default'
  }
}

const getDifficultyTagType = (difficultyLevel) => {
  switch (difficultyLevel) {
    case 1:
    case 2:
      return 'success'
    case 3:
      return 'warning'
    case 4:
    case 5:
      return 'danger'
    default:
      return 'info'
  }
}

// 返回对话列表
const goBackToDialogs = () => {
  router.push('/listening-qa/dialogs')
}

// 加载问题列表
const loadQuestions = async () => {
  if (!dialogId.value) return
  
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: [sortParams.prop, sortParams.order === 'ascending' ? 'asc' : 'desc']
    }

    // 添加筛选参数
    if (filterParams.questionType) params.questionType = filterParams.questionType
    if (filterParams.difficultyLevel) params.difficultyLevel = filterParams.difficultyLevel
    if (filterParams.isActive !== null) params.isActive = filterParams.isActive

    const response = await getQuestionsByDialogId(dialogId.value, params)
    questionList.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载问题列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadQuestions()
}

// 筛选处理
const handleFilter = () => {
  pagination.page = 1
  loadQuestions()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  Object.assign(filterParams, {
    questionType: null,
    difficultyLevel: null,
    isActive: null
  })
  pagination.page = 1
  loadQuestions()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadQuestions()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadQuestions()
}

// 排序处理
const handleSortChange = ({ prop, order }) => {
  sortParams.prop = prop || 'displayOrder'
  sortParams.order = order || 'ascending'
  loadQuestions()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

// 查看问题
const viewQuestion = async (question) => {
  try {
    const response = await getQuestionById(question.id)
    currentQuestion.value = response.data
    showViewQuestion.value = true
  } catch (error) {
    ElMessage.error('获取问题详情失败：' + error.message)
  }
}

// 编辑问题
const editQuestion = (question) => {
  isEdit.value = true
  Object.assign(questionForm, question)
  showCreateQuestion.value = true
}

// 复制问题
const copyQuestion = (question) => {
  const newQuestion = { ...question }
  delete newQuestion.id
  newQuestion.displayOrder = Math.max(...questionList.value.map(q => q.displayOrder)) + 1
  Object.assign(questionForm, newQuestion)
  isEdit.value = false
  showCreateQuestion.value = true
}

// 删除问题
const deleteQuestionAction = (question) => {
  ElMessageBox.confirm(
    `确定要删除问题"${question.questionText.substring(0, 50)}..."吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteQuestion(question.id)
      ElMessage.success('删除成功')
      loadQuestions()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

// 切换问题状态
const toggleQuestionStatus = async (question) => {
  try {
    await updateQuestion(question.id, { ...question, isActive: question.isActive })
    ElMessage.success(`问题已${question.isActive ? '激活' : '停用'}`)
  } catch (error) {
    question.isActive = !question.isActive // 恢复状态
    ElMessage.error('状态切换失败：' + error.message)
  }
}

// 批量操作
const batchActivate = async () => {
  try {
    const promises = selectedQuestions.value.map(q => 
      updateQuestion(q.id, { ...q, isActive: true })
    )
    await Promise.all(promises)
    ElMessage.success('批量激活成功')
    loadQuestions()
  } catch (error) {
    ElMessage.error('批量激活失败：' + error.message)
  }
}

const batchDeactivate = async () => {
  try {
    const promises = selectedQuestions.value.map(q => 
      updateQuestion(q.id, { ...q, isActive: false })
    )
    await Promise.all(promises)
    ElMessage.success('批量停用成功')
    loadQuestions()
  } catch (error) {
    ElMessage.error('批量停用失败：' + error.message)
  }
}

const batchUpdateDifficulty = () => {
  batchDifficultyLevel.value = null
  showBatchDifficulty.value = true
}

const submitBatchDifficulty = async () => {
  if (!batchDifficultyLevel.value) {
    ElMessage.warning('请选择难度等级')
    return
  }

  try {
    submitting.value = true
    const promises = selectedQuestions.value.map(q => 
      updateQuestion(q.id, { ...q, difficultyLevel: batchDifficultyLevel.value })
    )
    await Promise.all(promises)
    ElMessage.success('批量设置难度成功')
    showBatchDifficulty.value = false
    loadQuestions()
  } catch (error) {
    ElMessage.error('批量设置难度失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

const batchDelete = () => {
  const count = selectedQuestions.value.length
  ElMessageBox.confirm(
    `确定要删除选中的 ${count} 个问题吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedQuestions.value.map(q => q.id)
      await batchDeleteQuestions(ids)
      ElMessage.success('批量删除成功')
      selectedQuestions.value = []
      loadQuestions()
    } catch (error) {
      ElMessage.error('批量删除失败：' + error.message)
    }
  })
}

// 提交问题表单
const submitQuestion = async () => {
  if (!questionFormRef.value) return

  try {
    await questionFormRef.value.validate()
    
    // 设置对话ID
    questionForm.dialogId = dialogId.value
    
    // 验证数据
    const errors = validateQuestionData(questionForm)
    if (errors.length > 0) {
      ElMessage.error('表单验证失败：' + errors[0])
      return
    }

    submitting.value = true

    if (isEdit.value) {
      await updateQuestion(questionForm.id, questionForm)
      ElMessage.success('问题更新成功')
    } else {
      await createQuestion(questionForm)
      ElMessage.success('问题创建成功')
    }

    showCreateQuestion.value = false
    loadQuestions()
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetQuestionForm = () => {
  if (questionFormRef.value) {
    questionFormRef.value.resetFields()
  }
  Object.assign(questionForm, generateQuestionTemplate())
  isEdit.value = false
}

// 导出数据
const exportQuestions = async () => {
  try {
    ElMessage.info('导出功能正在开发中')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  if (!dialogId.value) {
    ElMessage.error('缺少对话ID参数')
    router.push('/listening-qa/dialogs')
    return
  }
  loadQuestions()
})
</script>

<style scoped>
.question-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.header-left h1 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.batch-actions {
  margin-bottom: 20px;
}

.table-section {
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.question-content .question-text {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
}

.question-meta {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.options-preview .option-item {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
  font-size: 12px;
}

.options-preview .option-label {
  font-weight: bold;
  margin-right: 4px;
  min-width: 20px;
}

.options-preview .option-text {
  color: #666;
}

.question-detail .question-text {
  white-space: pre-wrap;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  display: block;
  margin-top: 8px;
}

.question-detail .options-list {
  margin-top: 8px;
}

.question-detail .options-list .option-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.question-detail .options-list .option-label {
  font-weight: bold;
  margin-right: 8px;
  min-width: 25px;
}

.question-detail .answer-explanation {
  white-space: pre-wrap;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  display: block;
  margin-top: 8px;
}

.ml-1 {
  margin-left: 4px;
}

.ml-2 {
  margin-left: 8px;
}

.mr-1 {
  margin-right: 4px;
}

.mt-1 {
  margin-top: 4px;
}

.mt-4 {
  margin-top: 16px;
}
</style>

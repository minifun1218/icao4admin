<template>
  <div class="module-management">
    <el-card class="box-card">
      <!-- 页面标题和操作按钮 -->
      <template #header>
        <div class="card-header">
          <span class="card-title">考试模块管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建模块
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="模块类型">
          <el-select v-model="searchForm.moduleType" clearable placeholder="请选择模块类型" style="width: 200px">
            <el-option
              v-for="type in moduleTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isActivate" clearable placeholder="请选择状态" style="width: 150px">
            <el-option label="已激活" :value="true" />
            <el-option label="已停用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 模块列表 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="examPaperId" label="试卷ID" width="100" align="center" />
        <el-table-column prop="moduleType" label="模块类型" width="180" align="center">
          <template #default="{ row }">
            <el-tag :type="getModuleTypeTagType(row.moduleType)">
              {{ getModuleTypeName(row.moduleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="displayOrder" label="显示顺序" width="100" align="center" />
        <el-table-column prop="score" label="分数" width="100" align="center" />
        <el-table-column prop="isActivate" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActivate"
              @change="handleToggleActivation(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewQuestions(row)">
              <el-icon><View /></el-icon>
              预览题目
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="handleCopy(row)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 创建/编辑模块对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="moduleFormRef"
        :model="moduleForm"
        :rules="moduleRules"
        label-width="120px"
      >
        <el-form-item label="选择试卷" prop="examPaperId">
          <el-select
            v-model="moduleForm.examPaperId"
            placeholder="请输入试卷名称或代码进行搜索"
            filterable
            clearable
            remote
            reserve-keyword
            :remote-method="remoteSearchPapers"
            :loading="paperSearchLoading"
            style="width: 100%"
            @focus="handlePaperSelectFocus"
          >
            <template #empty>
              <div class="select-empty">
                <el-empty 
                  :image-size="80" 
                  :description="examPaperList.length === 0 ? '暂无试卷数据，请先创建试卷' : '未找到匹配的试卷'"
                />
              </div>
            </template>
            <el-option
              v-for="paper in examPaperList"
              :key="paper.id"
              :label="`${paper.name} (${paper.code})`"
              :value="paper.id"
            >
              <div class="paper-option">
                <span class="paper-name">{{ paper.name }}</span>
                <el-tag size="small" type="info" class="paper-code">{{ paper.code }}</el-tag>
              </div>
            </el-option>
          </el-select>
          <div class="form-item-tip">
            <el-icon><InfoFilled /></el-icon>
            支持输入试卷名称或代码搜索，当前共有 {{ totalPaperCount }} 份试卷
          </div>
        </el-form-item>
        <el-form-item label="模块类型" prop="moduleType">
          <el-select
            v-model="moduleForm.moduleType"
            placeholder="请选择模块类型"
            style="width: 100%"
            @change="handleModuleTypeChange"
          >
            <el-option
              v-for="type in moduleTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            >
              <span style="float: left">{{ type.label }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ type.description }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="显示顺序" prop="displayOrder">
          <el-input-number
            v-model="moduleForm.displayOrder"
            :min="1"
            placeholder="请输入显示顺序"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分数" prop="score">
          <el-input-number
            v-model="moduleForm.score"
            :min="0"
            placeholder="请输入分数"
            style="width: 100%"
          />
        </el-form-item>
        
        <!-- 配置JSON编辑器 -->
        <el-form-item label="模块配置" prop="configJson">
          <el-input
            v-model="moduleForm.configJson"
            type="textarea"
            :rows="6"
            placeholder='请输入JSON格式配置，例如：{"questionIds": [1,2,3]}'
          />
          <div class="form-item-tip">
            支持配置题目ID、时间限制等参数，格式为JSON
          </div>
        </el-form-item>

        <!-- 题目选择器 -->
        <el-form-item v-if="moduleForm.moduleType" label="选择题目">
          <el-button @click="showQuestionSelector">
            <el-icon><List /></el-icon>
            从题库选择题目
          </el-button>
          <div v-if="selectedQuestions.length > 0" class="selected-questions-info">
            已选择 {{ selectedQuestions.length }} 道题目
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 题目选择器对话框 -->
    <el-dialog
      v-model="questionSelectorVisible"
      title="选择题目"
      width="900px"
      append-to-body
    >
      <el-table
        v-loading="questionLoading"
        :data="questionList"
        border
        @selection-change="handleQuestionSelectionChange"
        max-height="500"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column :label="getQuestionTableLabel()" min-width="200">
          <template #default="{ row }">
            {{ getQuestionTitle(row) }}
          </template>
        </el-table-column>
        <el-table-column label="详情" min-width="150">
          <template #default="{ row }">
            {{ getQuestionDetail(row) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="questionPagination.page"
        v-model:page-size="questionPagination.size"
        :total="questionPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadQuestions"
        @current-change="loadQuestions"
        class="pagination"
      />

      <template #footer>
        <el-button @click="questionSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmQuestions">
          确定（已选{{ selectedQuestions.length }}道）
        </el-button>
      </template>
    </el-dialog>

    <!-- 题目预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="题目预览"
      width="900px"
    >
      <div v-loading="previewLoading" class="question-preview">
        <el-empty v-if="previewQuestions.length === 0" description="暂无题目数据" />
        <div v-else>
          <el-card
            v-for="(question, index) in previewQuestions"
            :key="question.id"
            class="question-card"
            shadow="hover"
          >
            <template #header>
              <div class="question-header">
                <span>题目 {{ index + 1 }}</span>
                <el-tag size="small">{{ getModuleTypeName(currentModule?.moduleType) }}</el-tag>
              </div>
            </template>
            <div class="question-content">
              <div class="question-detail" v-html="formatQuestionContent(question)"></div>
            </div>
          </el-card>
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
  Edit,
  Delete,
  View,
  CopyDocument,
  List,
  InfoFilled
} from '@element-plus/icons-vue'
import {
  getExamModules,
  createExamModule,
  updateModuleConfig,
  updateDisplayOrder,
  updateModuleScore,
  deleteExamModule,
  copyExamModule,
  toggleModuleActivation
} from '@/api/exam-module'
import { getExamPapers, searchExamPapers } from '@/api/exam-paper'

// 引入题库API
import * as listeningMcqApi from '@/api/listening-mcq'
import * as storyRetellApi from '@/api/story-retell'
import * as lsaDialogsApi from '@/api/lsa-dialogs'
import * as opiQuestionApi from '@/api/opi-question'
import * as atcScenarioApi from '@/api/atc-scenario'

// 模块类型定义
const moduleTypes = [
  { 
    value: 'LISTENING_MCQ', 
    label: '听力理解（多选题）', 
    description: '听音频选择正确答案',
    api: 'listeningMcq'
  },
  { 
    value: 'STORY_RETELL', 
    label: '故事复述', 
    description: '听音频后复述故事内容',
    api: 'storyRetell'
  },
  { 
    value: 'LISTENING_SA', 
    label: '听力简答题', 
    description: '听对话后回答问题',
    api: 'lsaDialogs'
  },
  { 
    value: 'ATC_SIM', 
    label: '模拟通话', 
    description: '空中交通管制模拟场景',
    api: 'atcScenario'
  },
  { 
    value: 'OPI', 
    label: '口语能力面试', 
    description: '口语问题回答',
    api: 'opiQuestion'
  }
]

// 数据状态
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const examPaperList = ref([])
const paperSearchLoading = ref(false)
const totalPaperCount = ref(0)

// 搜索表单
const searchForm = reactive({
  moduleType: '',
  isActivate: null
})

// 分页
const pagination = reactive({
  page: 0,
  size: 20,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑模块' : '新建模块')
const isEdit = ref(false)
const submitLoading = ref(false)
const moduleFormRef = ref(null)

// 表单数据
const moduleForm = reactive({
  id: null,
  examPaperId: null,
  moduleType: '',
  displayOrder: 1,
  configJson: '',
  score: 0
})

// 表单验证规则
const moduleRules = {
  examPaperId: [
    { required: true, message: '请选择试卷', trigger: 'change' }
  ],
  moduleType: [
    { required: true, message: '请选择模块类型', trigger: 'change' }
  ],
  displayOrder: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' }
  ]
}

// 题目选择器
const questionSelectorVisible = ref(false)
const questionLoading = ref(false)
const questionList = ref([])
const selectedQuestions = ref([])
const questionPagination = reactive({
  page: 0,
  size: 20,
  total: 0
})

// 题目预览
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewQuestions = ref([])
const currentModule = ref(null)

// ==================== 生命周期 ====================
onMounted(() => {
  loadModules()
  loadExamPapers()
})

// ==================== 数据加载 ====================
const loadModules = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      sortBy: 'displayOrder',
      sortDir: 'asc'
    }

    const response = await getExamModules(params)
    if (response.code === 200 && response.data) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载模块列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载试卷列表
const loadExamPapers = async (keyword = '') => {
  try {
    const params = {
      page: 0,
      size: 100, // 限制返回数量，避免数据过多
      sortBy: 'createdAt',
      sortDir: 'desc'
    }
    
    let response
    if (keyword && keyword.trim() !== '') {
      // 如果有关键词，使用搜索接口
      response = await searchExamPapers(keyword, params)
    } else {
      // 否则获取所有试卷
      response = await getExamPapers(params)
    }
    
    if (response.data.code === 200 && response.data) {
      examPaperList.value = response.data.data.content || []
      totalPaperCount.value = response.data.totalElements || examPaperList.value.length
    }
  } catch (error) {
    ElMessage.error('加载试卷列表失败：' + error.message)
  }
}

// 远程搜索试卷
const remoteSearchPapers = async (query) => {
  if (query === '') {
    await loadExamPapers()
    return
  }
  
  paperSearchLoading.value = true
  try {
    await loadExamPapers(query)
  } catch (error) {
    console.error('搜索试卷失败:', error)
  } finally {
    paperSearchLoading.value = false
  }
}

// 下拉框获得焦点时加载试卷列表
const handlePaperSelectFocus = () => {
  if (examPaperList.value.length === 0) {
    loadExamPapers()
  }
}

// 加载题目列表
const loadQuestions = async () => {
  if (!moduleForm.moduleType) return

  questionLoading.value = true
  try {
    const params = {
      page: questionPagination.page,
      size: questionPagination.size
    }

    let response
    switch (moduleForm.moduleType) {
      case 'LISTENING_MCQ':
        response = await listeningMcqApi.getQuestions(params)
        break
      case 'STORY_RETELL':
        response = await storyRetellApi.getRetellItems(params)
        break
      case 'LISTENING_SA':
        response = await lsaDialogsApi.getDialogs(params)
        break
      case 'ATC_SIM':
        response = await atcScenarioApi.getScenarios(params)
        break
      case 'OPI':
        response = await opiQuestionApi.getQuestions(params)
        break
    }

    if (response && response.code === 200 && response.data) {
      questionList.value = response.data.content || response.data || []
      questionPagination.total = response.data.totalElements || questionList.value.length
    }
  } catch (error) {
    ElMessage.error('加载题目列表失败：' + error.message)
  } finally {
    questionLoading.value = false
  }
}

// ==================== 事件处理 ====================
const handleSearch = () => {
  pagination.page = 0
  loadModules()
}

const handleReset = () => {
  searchForm.moduleType = ''
  searchForm.isActivate = null
  handleSearch()
}

const handleSizeChange = () => {
  loadModules()
}

const handleCurrentChange = () => {
  loadModules()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const showCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(moduleForm, {
    id: row.id,
    examPaperId: row.examPaperId,
    moduleType: row.moduleType,
    displayOrder: row.displayOrder,
    configJson: row.configJson || '',
    score: row.score || 0
  })
  
  // 解析已选择的题目
  try {
    if (row.configJson) {
      const config = JSON.parse(row.configJson)
      if (config.questionIds && Array.isArray(config.questionIds)) {
        selectedQuestions.value = config.questionIds.map(id => ({ id }))
      }
    }
  } catch (e) {
    console.error('解析配置JSON失败', e)
  }
  
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个模块吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteExamModule(row.id)
    ElMessage.success('删除成功')
    loadModules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

const handleCopy = async (row) => {
  try {
    await copyExamModule(row.id)
    ElMessage.success('复制成功')
    loadModules()
  } catch (error) {
    ElMessage.error('复制失败：' + error.message)
  }
}

const handleToggleActivation = async (row) => {
  try {
    await toggleModuleActivation(row.id, row.isActivate)
    ElMessage.success(row.isActivate ? '已激活' : '已停用')
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
    row.isActivate = !row.isActivate
  }
}

const handleViewQuestions = async (row) => {
  currentModule.value = row
  previewVisible.value = true
  previewLoading.value = true
  previewQuestions.value = []

  try {
    // 解析配置获取题目ID列表
    let questionIds = []
    if (row.configJson) {
      const config = JSON.parse(row.configJson)
      questionIds = config.questionIds || []
    }

    if (questionIds.length === 0) {
      ElMessage.warning('该模块暂无题目')
      previewLoading.value = false
      return
    }

    // 根据模块类型加载对应的题目详情
    const questions = []
    for (const id of questionIds) {
      try {
        let response
        switch (row.moduleType) {
          case 'LISTENING_MCQ':
            response = await listeningMcqApi.getQuestionById(id)
            break
          case 'STORY_RETELL':
            response = await storyRetellApi.getRetellItemById(id)
            break
          case 'LISTENING_SA':
            response = await lsaDialogsApi.getDialogById(id)
            break
          case 'ATC_SIM':
            response = await atcScenarioApi.getScenarioById(id)
            break
          case 'OPI':
            response = await opiQuestionApi.getQuestionById(id)
            break
        }
        if (response && response.code === 200 && response.data) {
          questions.push(response.data)
        }
      } catch (e) {
        console.error(`加载题目${id}失败`, e)
      }
    }

    previewQuestions.value = questions
  } catch (error) {
    ElMessage.error('加载题目详情失败：' + error.message)
  } finally {
    previewLoading.value = false
  }
}

const handleModuleTypeChange = () => {
  selectedQuestions.value = []
  moduleForm.configJson = ''
}

const showQuestionSelector = () => {
  questionSelectorVisible.value = true
  loadQuestions()
}

const handleQuestionSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const handleConfirmQuestions = () => {
  // 更新配置JSON
  const questionIds = selectedQuestions.value.map(q => q.id)
  const config = { questionIds }
  moduleForm.configJson = JSON.stringify(config, null, 2)
  questionSelectorVisible.value = false
  ElMessage.success(`已选择 ${questionIds.length} 道题目`)
}

const handleSubmit = async () => {
  if (!moduleFormRef.value) return

  try {
    await moduleFormRef.value.validate()
    submitLoading.value = true

    // 验证JSON格式
    if (moduleForm.configJson) {
      try {
        JSON.parse(moduleForm.configJson)
      } catch (e) {
        ElMessage.error('配置JSON格式不正确')
        submitLoading.value = false
        return
      }
    }

    if (isEdit.value) {
      // 更新模块
      await updateModuleConfig(moduleForm.id, moduleForm.configJson)
      await updateDisplayOrder(moduleForm.id, moduleForm.displayOrder)
      await updateModuleScore(moduleForm.id, moduleForm.score)
      ElMessage.success('更新成功')
    } else {
      // 创建模块
      await createExamModule({
        examPaperId: moduleForm.examPaperId,
        moduleType: moduleForm.moduleType,
        displayOrder: moduleForm.displayOrder,
        configJson: moduleForm.configJson,
        score: moduleForm.score
      })
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadModules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  Object.assign(moduleForm, {
    id: null,
    examPaperId: null,
    moduleType: '',
    displayOrder: 1,
    configJson: '',
    score: 0
  })
  selectedQuestions.value = []
  if (moduleFormRef.value) {
    moduleFormRef.value.resetFields()
  }
}

// ==================== 辅助方法 ====================
const getModuleTypeName = (type) => {
  const found = moduleTypes.find(t => t.value === type)
  return found ? found.label : type
}

const getModuleTypeTagType = (type) => {
  const typeMap = {
    'LISTENING_MCQ': 'primary',
    'STORY_RETELL': 'success',
    'LISTENING_SA': 'warning',
    'ATC_SIM': 'danger',
    'OPI': 'info'
  }
  return typeMap[type] || ''
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const getQuestionTableLabel = () => {
  if (!moduleForm.moduleType) return '题目'
  
  const labelMap = {
    'LISTENING_MCQ': '题干',
    'STORY_RETELL': '标题',
    'LISTENING_SA': '对话标题',
    'ATC_SIM': '场景名称',
    'OPI': '问题文本'
  }
  return labelMap[moduleForm.moduleType] || '题目'
}

const getQuestionTitle = (question) => {
  if (!question) return ''
  return question.textStem || question.title || question.questionText || question.name || `题目${question.id}`
}

const getQuestionDetail = (question) => {
  if (!question) return ''
  
  const details = []
  if (question.answerSeconds) details.push(`答题时间: ${question.answerSeconds}秒`)
  if (question.audioDurationSec) details.push(`音频时长: ${question.audioDurationSec}秒`)
  if (question.difficultyLevel) details.push(`难度: ${question.difficultyLevel}`)
  
  return details.join(' | ') || '-'
}

const formatQuestionContent = (question) => {
  if (!question) return ''
  
  let html = '<div class="question-item">'
  
  // 根据不同类型格式化内容
  if (currentModule.value?.moduleType === 'LISTENING_MCQ') {
    html += `<p><strong>题干：</strong>${question.textStem || ''}</p>`
    if (question.choices && question.choices.length > 0) {
      html += '<p><strong>选项：</strong></p><ul>'
      question.choices.forEach(choice => {
        const correct = choice.isCorrect ? ' ✓' : ''
        html += `<li>${choice.label}. ${choice.content}${correct}</li>`
      })
      html += '</ul>'
    }
  } else if (currentModule.value?.moduleType === 'STORY_RETELL') {
    html += `<p><strong>标题：</strong>${question.title || ''}</p>`
    html += `<p><strong>音频时长：</strong>${question.audioDurationSec || 0}秒</p>`
    html += `<p><strong>答题时间：</strong>${question.answerSeconds || 0}秒</p>`
    if (question.referenceText) {
      html += `<p><strong>参考文本：</strong>${question.referenceText}</p>`
    }
  } else if (currentModule.value?.moduleType === 'LISTENING_SA') {
    html += `<p><strong>对话：</strong>${question.title || ''}</p>`
    if (question.dialogText) {
      html += `<p><strong>对话内容：</strong>${question.dialogText}</p>`
    }
  } else if (currentModule.value?.moduleType === 'ATC_SIM') {
    html += `<p><strong>场景：</strong>${question.title || ''}</p>`
    html += `<p><strong>描述：</strong>${question.description || ''}</p>`
    html += `<p><strong>场景类型：</strong>${question.scenarioType || ''}</p>`
    html += `<p><strong>难度：</strong>${question.difficultyLevel || ''}</p>`
  } else if (currentModule.value?.moduleType === 'OPI') {
    html += `<p><strong>问题：</strong>${question.questionText || ''}</p>`
    html += `<p><strong>答题时间：</strong>${question.answerSeconds || 0}秒</p>`
    if (question.promptText) {
      html += `<p><strong>提示文本：</strong>${question.promptText}</p>`
    }
  }
  
  html += '</div>'
  return html
}
</script>

<style scoped>
.module-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.selected-questions-info {
  margin-top: 10px;
  padding: 10px;
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
  color: #409eff;
  font-size: 14px;
}

.question-preview {
  max-height: 600px;
  overflow-y: auto;
}

.question-card {
  margin-bottom: 16px;
}

.question-card:last-child {
  margin-bottom: 0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.question-content {
  padding: 10px 0;
}

.question-detail :deep(p) {
  margin: 8px 0;
  line-height: 1.6;
}

.question-detail :deep(ul) {
  margin: 8px 0;
  padding-left: 20px;
}

.question-detail :deep(li) {
  margin: 4px 0;
  line-height: 1.6;
}

.question-detail :deep(strong) {
  color: #303133;
  font-weight: 600;
}

/* 试卷选择器样式 */
.paper-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.paper-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 10px;
}

.paper-code {
  flex-shrink: 0;
}

.select-empty {
  padding: 20px 0;
}

.form-item-tip {
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-item-tip .el-icon {
  color: #409eff;
}
</style>


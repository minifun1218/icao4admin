<template>
  <div class="question-bank-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon" size="24" color="#409EFF"><Collection /></el-icon>
            <span class="header-title">题库管理</span>
            <el-tag type="info" size="small">{{ totalQuestions }}道题目</el-tag>
          </div>
          <div class="header-actions">
            <el-button type="success" @click="showImportDialog">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button type="primary" @click="exportQuestions">
              <el-icon><Download /></el-icon>
              导出题库
            </el-button>
            <el-button type="danger" @click="batchDelete" :disabled="!hasSelection">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :span="6" v-for="stat in questionStats" :key="stat.type">
            <el-card class="stat-card" :class="`stat-${stat.type}`" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon :size="32" :color="stat.color">
                    <component :is="stat.icon" />
                  </el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ stat.count }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="question-tabs">
        <!-- 听力理解 -->
        <el-tab-pane name="listening">
          <template #label>
            <span class="tab-label">
              <el-icon><Headset /></el-icon>
              听力理解
            </span>
          </template>
          <QuestionTypePanel 
            type="listening"
            :questions="listeningQuestions"
            :loading="loading"
            @add="addQuestion"
            @edit="editQuestion"
            @delete="deleteQuestion"
            @selection-change="handleSelectionChange"
          />
        </el-tab-pane>

        <!-- 听力选择题 -->
        <el-tab-pane name="mcq">
          <template #label>
            <span class="tab-label">
              <el-icon><List /></el-icon>
              听力选择题
            </span>
          </template>
          <QuestionTypePanel 
            type="mcq"
            :questions="mcqQuestions"
            :loading="loading"
            @add="addQuestion"
            @edit="editQuestion"
            @delete="deleteQuestion"
            @selection-change="handleSelectionChange"
          />
        </el-tab-pane>

        <!-- 口语模仿 -->
        <el-tab-pane name="opi">
          <template #label>
            <span class="tab-label">
              <el-icon><Microphone /></el-icon>
              口语模仿
            </span>
          </template>
          <QuestionTypePanel 
            type="opi"
            :questions="opiQuestions"
            :loading="loading"
            @add="addQuestion"
            @edit="editQuestion"
            @delete="deleteQuestion"
            @selection-change="handleSelectionChange"
          />
        </el-tab-pane>

        <!-- 故事复述 -->
        <el-tab-pane name="retell">
          <template #label>
            <span class="tab-label">
              <el-icon><ChatDotRound /></el-icon>
              故事复述
            </span>
          </template>
          <QuestionTypePanel 
            type="retell"
            :questions="retellQuestions"
            :loading="loading"
            @add="addQuestion"
            @edit="editQuestion"
            @delete="deleteQuestion"
            @selection-change="handleSelectionChange"
          />
        </el-tab-pane>

        <!-- ATC模拟 -->
        <el-tab-pane name="atc">
          <template #label>
            <span class="tab-label">
              <el-icon><Connection /></el-icon>
              ATC模拟
            </span>
          </template>
          <ATCQuestionPanel 
            :questions="atcQuestions"
            :scenarios="atcScenarios"
            :loading="loading"
            @add="addATCQuestion"
            @edit="editATCQuestion"
            @delete="deleteATCQuestion"
            @add-scenario="addATCScenario"
            @edit-scenario="editATCScenario"
            @delete-scenario="deleteATCScenario"
            @selection-change="handleSelectionChange"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 添加/编辑题目对话框 -->
    <el-dialog
      v-model="questionDialogVisible"
      :title="questionForm.id ? '编辑题目' : '添加题目'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="questionFormRef"
        :model="questionForm"
        :rules="questionRules"
        label-width="100px"
        class="question-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题目类型" prop="type">
              <el-select v-model="questionForm.type" placeholder="选择题目类型" @change="handleTypeChange">
                <el-option
                  v-for="type in questionTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                >
                  <span class="option-item">
                    <el-icon :color="type.color"><component :is="type.icon" /></el-icon>
                    {{ type.label }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-select v-model="questionForm.difficultyLevel" placeholder="选择难度等级">
                <el-option
                  v-for="level in difficultyLevels"
                  :key="level.value"
                  :label="level.label"
                  :value="level.value"
                >
                  <el-tag :color="level.color" size="small">{{ level.label }}</el-tag>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
            show-word-limit
            maxlength="1000"
          />
        </el-form-item>

        <!-- 选择题选项 -->
        <div v-if="questionForm.type === 'mcq'" class="choices-section">
          <el-form-item label="选项设置">
            <div class="choices-container">
              <div
                v-for="(choice, index) in questionForm.choices"
                :key="index"
                class="choice-item"
              >
                <el-input
                  v-model="choice.text"
                  :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
                  class="choice-input"
                />
                <el-radio
                  v-model="questionForm.correctAnswer"
                  :label="index"
                  class="choice-radio"
                >
                  正确答案
                </el-radio>
                <el-button
                  v-if="questionForm.choices.length > 2"
                  type="danger"
                  size="small"
                  circle
                  @click="removeChoice(index)"
                >
                  <el-icon><Minus /></el-icon>
                </el-button>
              </div>
              <el-button
                v-if="questionForm.choices.length < 6"
                type="primary"
                size="small"
                @click="addChoice"
                class="add-choice-btn"
              >
                <el-icon><Plus /></el-icon>
                添加选项
              </el-button>
            </div>
          </el-form-item>
        </div>

        <!-- 音频文件 -->
        <el-form-item 
          v-if="['listening', 'mcq', 'opi'].includes(questionForm.type)"
          label="音频文件"
          prop="audioFile"
        >
          <div class="audio-upload">
            <el-upload
              ref="audioUploadRef"
              :file-list="audioFileList"
              :before-upload="beforeAudioUpload"
              :on-success="handleAudioSuccess"
              :on-remove="handleAudioRemove"
              accept="audio/*"
              :limit="1"
              action="/api/upload/audio"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                上传音频
              </el-button>
            </el-upload>
            <div v-if="questionForm.audioUrl" class="audio-preview">
              <audio :src="questionForm.audioUrl" controls></audio>
            </div>
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="分值" prop="points">
              <el-input-number
                v-model="questionForm.points"
                :min="0.5"
                :max="50"
                :step="0.5"
                :precision="1"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预计用时" prop="estimatedTime">
              <el-input-number
                v-model="questionForm.estimatedTime"
                :min="30"
                :max="1800"
                :step="30"
                placeholder="秒"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="isActive">
              <el-switch
                v-model="questionForm.isActive"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="标签">
          <el-tag
            v-for="tag in questionForm.tags"
            :key="tag"
            closable
            @close="removeTag(tag)"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            size="small"
            @keyup.enter="addTag"
            @blur="addTag"
            class="tag-input"
          />
          <el-button v-else size="small" @click="showTagInput" class="add-tag-btn">
            <el-icon><Plus /></el-icon>
            添加标签
          </el-button>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="questionForm.remark"
            type="textarea"
            :rows="2"
            placeholder="题目备注或说明"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入题目"
      width="600px"
    >
      <div class="import-content">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          class="import-alert"
        >
          <template #default>
            <div>支持Excel格式文件，请按照模板格式准备数据</div>
            <div>
              <el-link type="primary" @click="downloadTemplate">
                <el-icon><Download /></el-icon>
                下载导入模板
              </el-link>
            </div>
          </template>
        </el-alert>

        <el-upload
          ref="importUploadRef"
          :file-list="importFileList"
          :before-upload="beforeImportUpload"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          accept=".xlsx,.xls"
          :limit="1"
          action="/api/question-bank/questions/import"
          class="import-upload"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              只能上传xlsx/xls文件，且不超过10MB
            </div>
          </template>
        </el-upload>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startImport" :loading="importing">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionBankApi } from '@/api/questionBank'
import QuestionTypePanel from './components/QuestionTypePanel.vue'
import ATCQuestionPanel from './components/ATCQuestionPanel.vue'
import {
  Collection,
  Upload,
  Download,
  Delete,
  Headset,
  List,
  Microphone,
  ChatDotRound,
  Connection,
  Plus,
  Minus
} from '@element-plus/icons-vue'

// 响应式数据
const activeTab = ref('listening')
const loading = ref(false)
const saving = ref(false)
const importing = ref(false)

const questionDialogVisible = ref(false)
const importDialogVisible = ref(false)
const tagInputVisible = ref(false)

const questionFormRef = ref(null)
const audioUploadRef = ref(null)
const importUploadRef = ref(null)
const tagInputRef = ref(null)

const tagInputValue = ref('')
const audioFileList = ref([])
const importFileList = ref([])

// 题目数据
const listeningQuestions = ref([])
const mcqQuestions = ref([])
const opiQuestions = ref([])
const retellQuestions = ref([])
const atcQuestions = ref([])
const atcScenarios = ref([])

const selectedQuestions = ref([])

// 表单数据
const questionForm = reactive({
  id: null,
  type: '',
  content: '',
  difficultyLevel: 3,
  points: 1.0,
  estimatedTime: 120,
  audioUrl: '',
  choices: [
    { text: '', correct: false },
    { text: '', correct: false }
  ],
  correctAnswer: 0,
  tags: [],
  remark: '',
  isActive: true
})

// 获取选项数据
const questionTypes = computed(() => questionBankApi.getQuestionTypes())
const difficultyLevels = computed(() => questionBankApi.getDifficultyLevels())

// 统计数据
const questionStats = ref([
  { type: 'listening', label: '听力理解', count: 0, icon: 'Headset', color: '#409EFF' },
  { type: 'mcq', label: '听力选择题', count: 0, icon: 'List', color: '#67C23A' },
  { type: 'opi', label: '口语模仿', count: 0, icon: 'Microphone', color: '#E6A23C' },
  { type: 'retell', label: '故事复述', count: 0, icon: 'ChatDotRound', color: '#F56C6C' },
  { type: 'atc', label: 'ATC模拟', count: 0, icon: 'Connection', color: '#909399' }
])

// 计算属性
const totalQuestions = computed(() => {
  return questionStats.value.reduce((total, stat) => total + stat.count, 0)
})

const hasSelection = computed(() => selectedQuestions.value.length > 0)

// 表单验证规则
const questionRules = {
  type: [
    { required: true, message: '请选择题目类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' },
    { min: 10, message: '题目内容至少10个字符', trigger: 'blur' }
  ],
  difficultyLevel: [
    { required: true, message: '请选择难度等级', trigger: 'change' }
  ],
  points: [
    { required: true, message: '请设置题目分值', trigger: 'blur' }
  ]
}

// 方法
const loadQuestions = async (type) => {
  try {
    loading.value = true
    let response
    
    switch (type) {
      case 'listening':
        response = await questionBankApi.getListeningQuestions()
        listeningQuestions.value = response.content || []
        break
      case 'mcq':
        response = await questionBankApi.getMCQQuestions()
        mcqQuestions.value = response.content || []
        break
      case 'opi':
        response = await questionBankApi.getOPIQuestions()
        opiQuestions.value = response.content || []
        break
      case 'retell':
        response = await questionBankApi.getRetellQuestions()
        retellQuestions.value = response.content || []
        break
      case 'atc':
        response = await questionBankApi.getATCQuestions()
        atcQuestions.value = response.content || []
        
        const scenariosResponse = await questionBankApi.getATCScenarios()
        atcScenarios.value = scenariosResponse.content || []
        break
    }
    
    updateStats()
  } catch (error) {
    ElMessage.error('加载题目失败')
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  questionStats.value[0].count = listeningQuestions.value.length
  questionStats.value[1].count = mcqQuestions.value.length
  questionStats.value[2].count = opiQuestions.value.length
  questionStats.value[3].count = retellQuestions.value.length
  questionStats.value[4].count = atcQuestions.value.length
}

const handleTabClick = (tab) => {
  activeTab.value = tab.name
  loadQuestions(tab.name)
}

const addQuestion = (type) => {
  resetQuestionForm()
  questionForm.type = type
  questionDialogVisible.value = true
}

const editQuestion = (question) => {
  Object.assign(questionForm, {
    ...question,
    choices: question.choices || [
      { text: '', correct: false },
      { text: '', correct: false }
    ]
  })
  questionDialogVisible.value = true
}

const deleteQuestion = async (question) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除题目"${question.content.substring(0, 20)}..."吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    switch (question.type) {
      case 'listening':
        await questionBankApi.deleteListeningQuestion(question.id)
        break
      case 'mcq':
        await questionBankApi.deleteMCQQuestion(question.id)
        break
      case 'opi':
        await questionBankApi.deleteOPIQuestion(question.id)
        break
      case 'retell':
        await questionBankApi.deleteRetellQuestion(question.id)
        break
      case 'atc':
        await questionBankApi.deleteATCQuestion(question.id)
        break
    }
    
    ElMessage.success('删除成功')
    loadQuestions(activeTab.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const saveQuestion = async () => {
  if (!questionFormRef.value) return
  
  try {
    await questionFormRef.value.validate()
    saving.value = true
    
    const questionData = { ...questionForm }
    
    if (questionForm.id) {
      // 更新题目
      switch (questionForm.type) {
        case 'listening':
          await questionBankApi.updateListeningQuestion(questionForm.id, questionData)
          break
        case 'mcq':
          await questionBankApi.updateMCQQuestion(questionForm.id, questionData)
          break
        case 'opi':
          await questionBankApi.updateOPIQuestion(questionForm.id, questionData)
          break
        case 'retell':
          await questionBankApi.updateRetellQuestion(questionForm.id, questionData)
          break
        case 'atc':
          await questionBankApi.updateATCQuestion(questionForm.id, questionData)
          break
      }
      ElMessage.success('更新成功')
    } else {
      // 创建题目
      switch (questionForm.type) {
        case 'listening':
          await questionBankApi.createListeningQuestion(questionData)
          break
        case 'mcq':
          await questionBankApi.createMCQQuestion(questionData)
          break
        case 'opi':
          await questionBankApi.createOPIQuestion(questionData)
          break
        case 'retell':
          await questionBankApi.createRetellQuestion(questionData)
          break
        case 'atc':
          await questionBankApi.createATCQuestion(questionData)
          break
      }
      ElMessage.success('创建成功')
    }
    
    questionDialogVisible.value = false
    loadQuestions(activeTab.value)
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const resetQuestionForm = () => {
  Object.assign(questionForm, {
    id: null,
    type: '',
    content: '',
    difficultyLevel: 3,
    points: 1.0,
    estimatedTime: 120,
    audioUrl: '',
    choices: [
      { text: '', correct: false },
      { text: '', correct: false }
    ],
    correctAnswer: 0,
    tags: [],
    remark: '',
    isActive: true
  })
  
  audioFileList.value = []
  
  if (questionFormRef.value) {
    questionFormRef.value.clearValidate()
  }
}

const handleTypeChange = (type) => {
  if (type === 'mcq' && questionForm.choices.length < 2) {
    questionForm.choices = [
      { text: '', correct: false },
      { text: '', correct: false }
    ]
  }
}

const addChoice = () => {
  questionForm.choices.push({
    text: '',
    correct: false
  })
}

const removeChoice = (index) => {
  questionForm.choices.splice(index, 1)
  if (questionForm.correctAnswer >= questionForm.choices.length) {
    questionForm.correctAnswer = 0
  }
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const batchDelete = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要删除的题目')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedQuestions.value.length} 道题目吗？`,
      '确认批量删除',
      { type: 'warning' }
    )
    
    const questionIds = selectedQuestions.value.map(q => q.id)
    await questionBankApi.batchDeleteQuestions(questionIds)
    
    ElMessage.success('批量删除成功')
    loadQuestions(activeTab.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const exportQuestions = async () => {
  try {
    const blob = await questionBankApi.exportQuestions()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `题库_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const showImportDialog = () => {
  importDialogVisible.value = true
  importFileList.value = []
}

const downloadTemplate = () => {
  // 下载导入模板
  const link = document.createElement('a')
  link.href = '/templates/question-import-template.xlsx'
  link.download = '题目导入模板.xlsx'
  link.click()
}

const beforeImportUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                  file.type === 'application/vnd.ms-excel'
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!')
    return false
  }
  return true
}

const startImport = async () => {
  if (importFileList.value.length === 0) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  try {
    importing.value = true
    await questionBankApi.importQuestions(importFileList.value[0])
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    loadQuestions(activeTab.value)
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

const handleImportSuccess = (response) => {
  ElMessage.success('文件上传成功')
}

const handleImportError = (error) => {
  ElMessage.error('文件上传失败')
}

// 音频上传相关
const beforeAudioUpload = (file) => {
  const isAudio = file.type.startsWith('audio/')
  const isLt50M = file.size / 1024 / 1024 < 50
  
  if (!isAudio) {
    ElMessage.error('只能上传音频文件!')
    return false
  }
  if (!isLt50M) {
    ElMessage.error('音频文件大小不能超过50MB!')
    return false
  }
  return true
}

const handleAudioSuccess = (response, file) => {
  questionForm.audioUrl = response.url
  ElMessage.success('音频上传成功')
}

const handleAudioRemove = () => {
  questionForm.audioUrl = ''
}

// 标签相关
const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

const addTag = () => {
  const tag = tagInputValue.value.trim()
  if (tag && !questionForm.tags.includes(tag)) {
    questionForm.tags.push(tag)
  }
  tagInputValue.value = ''
  tagInputVisible.value = false
}

const removeTag = (tag) => {
  const index = questionForm.tags.indexOf(tag)
  if (index > -1) {
    questionForm.tags.splice(index, 1)
  }
}

// ATC相关方法
const addATCQuestion = () => {
  addQuestion('atc')
}

const editATCQuestion = (question) => {
  editQuestion(question)
}

const deleteATCQuestion = async (question) => {
  await deleteQuestion(question)
}

const addATCScenario = () => {
  // 添加ATC场景的逻辑
}

const editATCScenario = (scenario) => {
  // 编辑ATC场景的逻辑
}

const deleteATCScenario = async (scenario) => {
  // 删除ATC场景的逻辑
}

// 生命周期
onMounted(() => {
  loadQuestions('listening')
})

// 监听标签页切换
watch(activeTab, (newTab) => {
  loadQuestions(newTab)
})
</script>

<style scoped>
.question-bank-management {
  padding: 0;
}

.main-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  background: linear-gradient(135deg, #409EFF, #67C23A);
  border-radius: 8px;
  padding: 8px;
  color: white;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-cards {
  margin: 20px 0;
}

.stat-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.1));
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.question-tabs {
  margin-top: 20px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.question-form {
  max-height: 70vh;
  overflow-y: auto;
}

.choices-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
}

.choices-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.choice-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.choice-input {
  flex: 1;
}

.choice-radio {
  margin: 0;
}

.add-choice-btn {
  align-self: flex-start;
  margin-top: 8px;
}

.audio-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.audio-preview {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.tag-input {
  width: 100px;
  margin-right: 8px;
}

.add-tag-btn {
  height: 24px;
  line-height: 22px;
  border-style: dashed;
}

.import-content {
  padding: 20px 0;
}

.import-alert {
  margin-bottom: 20px;
}

.import-upload {
  text-align: center;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 40px 20px;
  background: #fafafa;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .stats-cards .el-col {
    margin-bottom: 16px;
  }
  
  .choice-item {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .choice-radio {
    align-self: flex-start;
  }
}
</style>

<template>
  <div class="result-management">
    <el-card class="box-card">
      <!-- 页面标题和统计信息 -->
      <template #header>
        <div class="card-header">
          <span class="card-title">成绩管理</span>
          <div class="header-stats">
            <el-statistic
              title="总考试数"
              :value="statistics.totalRecords || 0"
              class="stat-item"
            />
            <el-statistic
              title="今日考试"
              :value="statistics.todayRecords || 0"
              class="stat-item"
            />
            <el-statistic
              title="本周考试"
              :value="statistics.weekRecords || 0"
              class="stat-item"
            />
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="用户ID">
          <el-input
            v-model.number="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="试卷ID">
          <el-input
            v-model.number="searchForm.examPaperId"
            placeholder="请输入试卷ID"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="完成状态">
          <el-select
            v-model="searchForm.isFinished"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="已完成" :value="true" />
            <el-option label="进行中" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 考试记录列表 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="记录ID" width="100" align="center" />
        <el-table-column prop="examPaperId" label="试卷ID" width="100" align="center" />
        <el-table-column label="试卷名称" width="200">
          <template #default="{ row }">
            {{ getPaperName(row.examPaperId) }}
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column label="用户名" width="150">
          <template #default="{ row }">
            {{ getUserName(row.userId) }}
          </template>
        </el-table-column>
        <el-table-column prop="createAt" label="考试时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="isFinished" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getExamStatusType(row.isFinished)">
              {{ formatExamStatus(row.isFinished) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleScore(row)"
              :disabled="!row.isFinished"
            >
              <el-icon><Edit /></el-icon>
              评分
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

    <!-- 考试详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="考试详情"
      width="90%"
      top="5vh"
    >
      <div v-loading="detailLoading" class="detail-container">
        <!-- 考试基本信息 -->
        <el-descriptions :column="2" border class="exam-info">
          <el-descriptions-item label="记录ID">
            {{ currentRecord?.id }}
          </el-descriptions-item>
          <el-descriptions-item label="试卷名称">
            {{ currentPaperInfo?.name || '加载中...' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户">
            用户ID: {{ currentRecord?.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="考试时间">
            {{ formatDateTime(currentRecord?.createAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getExamStatusType(currentRecord?.isFinished)">
              {{ formatExamStatus(currentRecord?.isFinished) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="试卷时长">
            {{ currentPaperInfo?.totalDurationMin || 0 }}分钟
          </el-descriptions-item>
        </el-descriptions>

        <!-- 试卷模块和答案 -->
        <el-divider content-position="left">
          <span class="divider-title">考试模块（共 {{ currentModules.length }} 个）</span>
        </el-divider>

        <el-empty v-if="currentModules.length === 0" description="该考试暂无模块数据" />

        <div v-else class="modules-container">
          <el-card
            v-for="(module, index) in currentModules"
            :key="module.id"
            class="module-card"
            shadow="hover"
          >
            <template #header>
              <div class="module-header">
                <span class="module-title">
                  <el-tag type="primary" class="mr-10">模块 {{ index + 1 }}</el-tag>
                  {{ getModuleTypeName(module.moduleType) }}
                </span>
                <div class="module-meta">
                  <el-tag size="small" type="info" class="mr-10">分数: {{ module.score || 0 }}</el-tag>
                  <el-tag size="small" type="success">实得: {{ getModuleActualScore(module.id) || 0 }}</el-tag>
                </div>
              </div>
            </template>

            <!-- 模块题目和回答 -->
            <div class="module-content">
              <div v-if="getModuleQuestions(module).length === 0" class="no-content">
                暂无题目数据
              </div>
              <div v-else>
                <div
                  v-for="(question, qIndex) in getModuleQuestions(module)"
                  :key="question.id"
                  class="question-answer-item"
                >
                  <div class="question-section">
                    <div class="section-title">题目 {{ qIndex + 1 }}</div>
                    <div class="question-content" v-html="formatQuestionContent(question, module.moduleType)"></div>
                  </div>
                  
                  <div class="answer-section">
                    <div class="section-title">学生答案</div>
                    <div class="answer-content">
                      {{ getStudentAnswer(module.id, question.id) || '暂无作答' }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 评分对话框 -->
    <el-dialog
      v-model="scoreVisible"
      title="模块评分"
      width="900px"
    >
      <div v-loading="scoreLoading" class="score-container">
        <el-descriptions :column="2" border class="mb-20">
          <el-descriptions-item label="试卷">
            {{ currentPaperInfo?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="用户">
            用户ID: {{ currentRecord?.userId }}
          </el-descriptions-item>
        </el-descriptions>

        <el-form
          ref="scoreFormRef"
          :model="scoreForm"
          label-width="120px"
        >
          <div
            v-for="(module, index) in currentModules"
            :key="module.id"
            class="module-score-section"
          >
            <el-divider content-position="left">
              <strong>{{ getModuleTypeName(module.moduleType) }}</strong>
              （满分: {{ module.score || 0 }}）
            </el-divider>

            <el-form-item
              :label="`模块${index + 1}得分`"
              :prop="`moduleScores.${module.id}`"
              :rules="[
                { required: true, message: '请输入得分', trigger: 'blur' },
                { type: 'number', min: 0, max: module.score || 100, message: `得分必须在0-${module.score || 100}之间`, trigger: 'blur' }
              ]"
            >
              <el-input-number
                v-model="scoreForm.moduleScores[module.id]"
                :min="0"
                :max="module.score || 100"
                :precision="1"
                :step="0.5"
                style="width: 200px"
              />
              <span class="ml-10">分</span>
            </el-form-item>

            <el-form-item :label="`模块${index + 1}评语`">
              <el-input
                v-model="scoreForm.moduleComments[module.id]"
                type="textarea"
                :rows="3"
                placeholder="请输入评语（可选）"
              />
            </el-form-item>
          </div>

          <el-divider />

          <el-form-item label="总分">
            <el-statistic
              :value="calculateTotalScore()"
              :precision="1"
              suffix="分"
            />
          </el-form-item>

          <el-form-item label="总评">
            <el-input
              v-model="scoreForm.overallComment"
              type="textarea"
              :rows="4"
              placeholder="请输入总评（可选）"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="scoreVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitScoreLoading" @click="handleSubmitScore">
          提交评分
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  View,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import {
  getAllExamRecords,
  getExamRecordsByUserId,
  getExamRecordsByExamPaperId,
  getUserExamRecordsByPaper,
  getExamRecordsByDateRange,
  deleteExamRecord,
  formatExamStatus,
  getExamStatusType,
  formatDateTime,
  getBasicStatistics
} from '@/api/exam-record'
import { getExamPaperById } from '@/api/exam-paper'
import { getModulesByExamPaperId } from '@/api/exam-module'

// 引入题库API
import * as listeningMcqApi from '@/api/listening-mcq'
import * as storyRetellApi from '@/api/story-retell'
import * as lsaDialogsApi from '@/api/lsa-dialogs'
import * as opiQuestionApi from '@/api/opi-question'
import * as atcScenarioApi from '@/api/atc-scenario'

// 模块类型映射
const moduleTypes = {
  'LISTENING_MCQ': { label: '听力理解（多选题）' },
  'STORY_RETELL': { label: '故事复述' },
  'LISTENING_SA': { label: '听力简答题' },
  'ATC_SIM': { label: '模拟通话' },
  'OPI': { label: '口语能力面试' }
}

// 数据状态
const loading = ref(false)
const tableData = ref([])
const dateRange = ref([])

// 统计信息
const statistics = reactive({
  totalRecords: 0,
  todayRecords: 0,
  weekRecords: 0,
  monthRecords: 0
})

// 搜索表单
const searchForm = reactive({
  userId: null,
  examPaperId: null,
  isFinished: null
})

// 分页
const pagination = reactive({
  page: 0,
  size: 20,
  total: 0
})

// 详情对话框
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentRecord = ref(null)
const currentPaperInfo = ref(null)
const currentModules = ref([])
const currentQuestionsMap = ref({})

// 评分对话框
const scoreVisible = ref(false)
const scoreLoading = ref(false)
const submitScoreLoading = ref(false)
const scoreFormRef = ref(null)
const scoreForm = reactive({
  moduleScores: {},
  moduleComments: {},
  overallComment: ''
})

// 缓存数据
const papersCache = ref({})
const usersCache = ref({})
const moduleScoresCache = ref({}) // 模块得分缓存
const studentAnswersCache = ref({}) // 学生答案缓存

// ==================== 生命周期 ====================
onMounted(() => {
  loadRecords()
  loadStatistics()
})

// ==================== 数据加载 ====================
const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      sortBy: 'createAt',
      sortDir: 'desc'
    }

    let response
    if (searchForm.userId && searchForm.examPaperId) {
      response = await getUserExamRecordsByPaper(searchForm.userId, searchForm.examPaperId, params)
    } else if (searchForm.userId) {
      response = await getExamRecordsByUserId(searchForm.userId, params)
    } else if (searchForm.examPaperId) {
      response = await getExamRecordsByExamPaperId(searchForm.examPaperId, params)
    } else if (dateRange.value && dateRange.value.length === 2) {
      response = await getExamRecordsByDateRange(
        dateRange.value[0].toISOString(),
        dateRange.value[1].toISOString(),
        params
      )
    } else {
      response = await getAllExamRecords(params)
    }

    if (response.code === 200 && response.data) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0

      // 预加载试卷信息
      await loadPapersInfo()
    }
  } catch (error) {
    ElMessage.error('加载考试记录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await getBasicStatistics()
    if (response.code === 200 && response.data) {
      Object.assign(statistics, response.data)
    }
  } catch (error) {
    console.error('加载统计信息失败', error)
  }
}

const loadPapersInfo = async () => {
  const paperIds = [...new Set(tableData.value.map(r => r.examPaperId))]
  
  for (const paperId of paperIds) {
    if (!papersCache.value[paperId]) {
      try {
        const response = await getExamPaperById(paperId)
        if (response.code === 200 && response.data) {
          papersCache.value[paperId] = response.data
        }
      } catch (error) {
        console.error(`加载试卷${paperId}信息失败`, error)
      }
    }
  }
}

const loadRecordDetail = async (record) => {
  detailLoading.value = true
  currentRecord.value = record
  currentModules.value = []
  currentQuestionsMap.value = {}

  try {
    // 加载试卷信息
    const paperResponse = await getExamPaperById(record.examPaperId)
    if (paperResponse.code === 200 && paperResponse.data) {
      currentPaperInfo.value = paperResponse.data
    }

    // 加载模块列表
    const modulesResponse = await getModulesByExamPaperId(record.examPaperId, true)
    if (modulesResponse.code === 200 && modulesResponse.data) {
      currentModules.value = modulesResponse.data || []

      // 加载每个模块的题目
      for (const module of currentModules.value) {
        await loadModuleQuestions(module)
      }

      // 模拟加载学生答案（实际应该从后端获取）
      loadStudentAnswers(record.id)
    }
  } catch (error) {
    ElMessage.error('加载详情失败：' + error.message)
  } finally {
    detailLoading.value = false
  }
}

const loadModuleQuestions = async (module) => {
  try {
    let questionIds = []
    if (module.configJson) {
      const config = JSON.parse(module.configJson)
      questionIds = config.questionIds || []
    }

    if (questionIds.length === 0) {
      currentQuestionsMap.value[module.id] = []
      return
    }

    const questions = []
    for (const id of questionIds) {
      try {
        let response
        switch (module.moduleType) {
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

    currentQuestionsMap.value[module.id] = questions
  } catch (error) {
    console.error('解析模块配置失败', error)
    currentQuestionsMap.value[module.id] = []
  }
}

// 模拟加载学生答案（实际应从后端获取）
const loadStudentAnswers = (recordId) => {
  // 这里应该调用实际的API获取学生答案
  // 目前使用模拟数据
  studentAnswersCache.value[recordId] = {
    // moduleId_questionId: answer
  }
  
  // 模拟一些答案数据
  currentModules.value.forEach(module => {
    const questions = currentQuestionsMap.value[module.id] || []
    questions.forEach((question, index) => {
      const key = `${module.id}_${question.id}`
      studentAnswersCache.value[recordId][key] = `这是学生对题目${index + 1}的回答内容（模拟数据）`
    })
  })
}

// ==================== 事件处理 ====================
const handleSearch = () => {
  pagination.page = 0
  loadRecords()
}

const handleReset = () => {
  searchForm.userId = null
  searchForm.examPaperId = null
  searchForm.isFinished = null
  dateRange.value = []
  handleSearch()
}

const handleSizeChange = () => {
  loadRecords()
}

const handleCurrentChange = () => {
  loadRecords()
}

const handleViewDetail = async (row) => {
  detailVisible.value = true
  await loadRecordDetail(row)
}

const handleScore = async (row) => {
  currentRecord.value = row
  scoreVisible.value = true
  scoreLoading.value = true

  // 重置评分表单
  scoreForm.moduleScores = {}
  scoreForm.moduleComments = {}
  scoreForm.overallComment = ''

  try {
    // 加载试卷信息
    const paperResponse = await getExamPaperById(row.examPaperId)
    if (paperResponse.code === 200 && paperResponse.data) {
      currentPaperInfo.value = paperResponse.data
    }

    // 加载模块列表
    const modulesResponse = await getModulesByExamPaperId(row.examPaperId, true)
    if (modulesResponse.code === 200 && modulesResponse.data) {
      currentModules.value = modulesResponse.data || []

      // 初始化分数为0
      currentModules.value.forEach(module => {
        scoreForm.moduleScores[module.id] = 0
        scoreForm.moduleComments[module.id] = ''
      })

      // 如果已有评分，加载现有评分
      if (moduleScoresCache.value[row.id]) {
        Object.assign(scoreForm.moduleScores, moduleScoresCache.value[row.id])
      }
    }
  } catch (error) {
    ElMessage.error('加载评分信息失败：' + error.message)
  } finally {
    scoreLoading.value = false
  }
}

const handleSubmitScore = async () => {
  if (!scoreFormRef.value) return

  try {
    await scoreFormRef.value.validate()
    submitScoreLoading.value = true

    // 这里应该调用实际的评分API
    // 目前模拟保存到缓存
    moduleScoresCache.value[currentRecord.value.id] = { ...scoreForm.moduleScores }

    ElMessage.success('评分提交成功')
    scoreVisible.value = false
    
    // 刷新列表
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交评分失败：' + error.message)
    }
  } finally {
    submitScoreLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条考试记录吗？删除后将无法恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteExamRecord(row.id)
    ElMessage.success('删除成功')
    loadRecords()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// ==================== 辅助方法 ====================
const getPaperName = (paperId) => {
  return papersCache.value[paperId]?.name || `试卷${paperId}`
}

const getUserName = (userId) => {
  return usersCache.value[userId]?.name || `用户${userId}`
}

const getModuleTypeName = (type) => {
  return moduleTypes[type]?.label || type
}

const getModuleQuestions = (module) => {
  return currentQuestionsMap.value[module.id] || []
}

const getModuleActualScore = (moduleId) => {
  if (!currentRecord.value) return 0
  return moduleScoresCache.value[currentRecord.value.id]?.[moduleId] || 0
}

const getStudentAnswer = (moduleId, questionId) => {
  if (!currentRecord.value) return ''
  const key = `${moduleId}_${questionId}`
  return studentAnswersCache.value[currentRecord.value.id]?.[key] || ''
}

const calculateTotalScore = () => {
  return Object.values(scoreForm.moduleScores).reduce((sum, score) => sum + (score || 0), 0)
}

const formatQuestionContent = (question, moduleType) => {
  if (!question) return ''
  
  let html = '<div class="question-detail">'
  
  if (moduleType === 'LISTENING_MCQ') {
    html += `<p><strong>题干：</strong>${question.textStem || ''}</p>`
    if (question.choices && question.choices.length > 0) {
      html += '<p><strong>选项：</strong></p><ul>'
      question.choices.forEach(choice => {
        const correct = choice.isCorrect ? ' <span class="correct-mark">✓</span>' : ''
        html += `<li>${choice.label}. ${choice.content}${correct}</li>`
      })
      html += '</ul>'
    }
  } else if (moduleType === 'STORY_RETELL') {
    html += `<p><strong>标题：</strong>${question.title || ''}</p>`
    if (question.referenceText) {
      html += `<p><strong>参考文本：</strong>${question.referenceText}</p>`
    }
  } else if (moduleType === 'LISTENING_SA') {
    html += `<p><strong>对话：</strong>${question.title || ''}</p>`
    if (question.dialogText) {
      html += `<div class="dialog-text">${question.dialogText}</div>`
    }
  } else if (moduleType === 'ATC_SIM') {
    html += `<p><strong>场景：</strong>${question.title || ''}</p>`
    html += `<p>${question.description || ''}</p>`
  } else if (moduleType === 'OPI') {
    html += `<p><strong>问题：</strong>${question.questionText || ''}</p>`
    if (question.promptText) {
      html += `<p><strong>提示：</strong>${question.promptText}</p>`
    }
  }
  
  html += '</div>'
  return html
}
</script>

<style scoped>
.result-management {
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

.header-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mr-10 {
  margin-right: 10px;
}

.ml-10 {
  margin-left: 10px;
}

.mb-20 {
  margin-bottom: 20px;
}

/* 详情样式 */
.detail-container {
  min-height: 400px;
}

.exam-info {
  margin-bottom: 20px;
}

.divider-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.modules-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.module-card {
  border: 1px solid #e4e7ed;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.module-meta {
  display: flex;
  align-items: center;
}

.module-content {
  padding: 10px 0;
}

.no-content {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.question-answer-item {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.question-answer-item:last-child {
  margin-bottom: 0;
}

.question-section,
.answer-section {
  margin-bottom: 15px;
}

.answer-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
  font-size: 14px;
}

.question-content,
.answer-content {
  padding: 10px;
  background-color: white;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
}

.question-detail :deep(p) {
  margin: 8px 0;
}

.question-detail :deep(ul) {
  margin: 8px 0;
  padding-left: 20px;
}

.question-detail :deep(li) {
  margin: 4px 0;
}

.question-detail :deep(.correct-mark) {
  color: #67c23a;
  font-weight: bold;
  margin-left: 8px;
}

.question-detail :deep(.dialog-text) {
  padding: 10px;
  background-color: #f9f9f9;
  border-left: 3px solid #409eff;
  margin: 8px 0;
  white-space: pre-wrap;
}

/* 评分样式 */
.score-container {
  min-height: 300px;
}

.module-score-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>

<template>
  <div class="exam-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon" size="24" color="#409EFF"><Medal /></el-icon>
            <span class="header-title">考试管理</span>
            <el-tag type="info" size="small">{{ totalExams }}场考试</el-tag>
          </div>
          <div class="header-actions">
            <el-button type="success" @click="createExam">
              <el-icon><Plus /></el-icon>
              创建考试
            </el-button>
            <el-button type="primary" @click="exportExamData">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :span="6" v-for="stat in examStats" :key="stat.type">
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
      <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="exam-tabs">
        <!-- 试卷管理 -->
        <el-tab-pane name="papers">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              试卷管理
            </span>
          </template>
          <ExamPaperPanel 
            :papers="examPapers"
            :loading="loading"
            @add="addPaper"
            @edit="editPaper"
            @delete="deletePaper"
            @copy="copyPaper"
            @publish="publishPaper"
            @preview="previewPaper"
          />
        </el-tab-pane>

        <!-- 考试信息 -->
        <el-tab-pane name="exams">
          <template #label>
            <span class="tab-label">
              <el-icon><Clock /></el-icon>
              考试信息
            </span>
          </template>
          <ExamInfoPanel 
            :exams="examList"
            :loading="loading"
            :use-builtin-manager="true"
            @add="addExam"
            @edit="editExam"
            @delete="deleteExam"
            @start="startExam"
            @end="endExam"
            @view-participants="viewParticipants"
          />
        </el-tab-pane>

        <!-- 考试结果 -->
        <el-tab-pane name="results">
          <template #label>
            <span class="tab-label">
              <el-icon><TrendCharts /></el-icon>
              考试结果
            </span>
          </template>
          <ExamResultPanel 
            :results="examResults"
            :loading="loading"
            @view-detail="viewResultDetail"
            @re-score="reScoreResult"
            @export="exportResults"
            @view-statistics="viewStatistics"
          />
        </el-tab-pane>

        <!-- 听力简答考试 -->
        <el-tab-pane name="listening-comprehension">
          <template #label>
            <span class="tab-label">
              <el-icon><Service /></el-icon>
              听力简答考试
            </span>
          </template>
          <ListeningComprehensionExam 
            :exam-data="listeningComprehensionData"
            :show-admin-preview="true"
            @exam-started="handleExamStarted"
            @exam-completed="handleExamCompleted"
            @answer-recorded="handleAnswerRecorded"
          />
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <!-- 创建/编辑考试对话框 -->
    <el-dialog
      v-model="examDialogVisible"
      :title="examForm.id ? '编辑考试' : '创建考试'"
      width="900px"
      :close-on-click-modal="false"
      class="exam-dialog"
    >
      <el-form
        ref="examFormRef"
        :model="examForm"
        :rules="examRules"
        label-width="120px"
        class="exam-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试名称" prop="name">
              <el-input v-model="examForm.name" placeholder="请输入考试名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试类型" prop="type">
              <el-select v-model="examForm.type" placeholder="选择考试类型">
                <el-option
                  v-for="type in examTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                >
                  <span class="option-item">
                    <el-icon><component :is="type.icon" /></el-icon>
                    {{ type.label }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="考试描述" prop="description">
          <el-input
            v-model="examForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入考试描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="选择试卷" prop="paperId">
              <el-select v-model="examForm.paperId" placeholder="选择试卷" filterable>
                <el-option
                  v-for="paper in examPapers"
                  :key="paper.id"
                  :label="paper.title"
                  :value="paper.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number
                v-model="examForm.duration"
                :min="10"
                :max="480"
                :step="10"
                placeholder="分钟"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分数" prop="passingScore">
              <el-input-number
                v-model="examForm.passingScore"
                :min="0"
                :max="100"
                :step="1"
                placeholder="分"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="examForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="examForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最大参与人数">
              <el-input-number
                v-model="examForm.maxParticipants"
                :min="1"
                :max="10000"
                :step="1"
                placeholder="人"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="重考次数">
              <el-input-number
                v-model="examForm.retakeLimit"
                :min="0"
                :max="10"
                :step="1"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评分方式">
              <el-select v-model="examForm.scoringMethod" placeholder="选择评分方式">
                <el-option
                  v-for="method in scoringMethods"
                  :key="method.value"
                  :label="method.label"
                  :value="method.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="考试设置">
          <div class="exam-settings">
            <el-checkbox v-model="examForm.settings.randomQuestions">题目乱序</el-checkbox>
            <el-checkbox v-model="examForm.settings.randomOptions">选项乱序</el-checkbox>
            <el-checkbox v-model="examForm.settings.showScore">显示成绩</el-checkbox>
            <el-checkbox v-model="examForm.settings.allowReview">允许查看答案</el-checkbox>
            <el-checkbox v-model="examForm.settings.preventCheating">防作弊模式</el-checkbox>
            <el-checkbox v-model="examForm.settings.autoSubmit">时间到自动提交</el-checkbox>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="examForm.remark"
            type="textarea"
            :rows="2"
            placeholder="考试备注或特殊说明"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="examDialogVisible = false">取消</el-button>
        <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="saveExam" :loading="saving">
          {{ examForm.id ? '更新考试' : '创建考试' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 参与者管理对话框 -->
    <el-dialog
      v-model="participantsDialogVisible"
      title="参与者管理"
      width="800px"
      :close-on-click-modal="false"
    >
      <ParticipantsManagement
        v-if="currentExam"
        :exam="currentExam"
        @update="loadExamData"
      />
    </el-dialog>

    <!-- 统计分析对话框 -->
    <el-dialog
      v-model="statisticsDialogVisible"
      title="统计分析"
      width="1000px"
      :close-on-click-modal="false"
    >
      <ExamStatistics
        v-if="currentExam"
        :exam="currentExam"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { examApi } from '@/api/exam'
import ExamPaperPanel from './components/ExamPaperPanel.vue'
import ExamInfoPanel from './components/ExamInfoPanel.vue'
import ExamResultPanel from './components/ExamResultPanel.vue'
import ParticipantsManagement from './components/ParticipantsManagement.vue'
import ExamStatistics from './components/ExamStatistics.vue'
import ListeningComprehensionExam from './components/ListeningComprehensionExam.vue'
import {
  Medal,
  Plus,
  Download,
  Document,
  Clock,
  TrendCharts,
  Edit,
  Service
} from '@element-plus/icons-vue'

// 响应式数据
const activeTab = ref('papers')
const loading = ref(false)
const saving = ref(false)

const examDialogVisible = ref(false)
const participantsDialogVisible = ref(false)
const statisticsDialogVisible = ref(false)

const examFormRef = ref(null)
const currentExam = ref(null)

// 数据
const examPapers = ref([])
const examList = ref([])
const examResults = ref([])

// 听力简答考试数据
const listeningComprehensionData = ref({
  id: 'listening-comprehension-demo',
  title: '听力简答部分演示',
  dialogs: [
    {
      id: 'dialog-1',
      title: '机场延误情况',
      content: 'Air traffic control to Flight 123, due to severe weather conditions at your destination airport, we need to hold your flight for approximately 30 minutes. There are strong winds and heavy rain making it unsafe for landing operations. We will update you as soon as conditions improve and provide further instructions.',
      audioUrl: '/demo/audio/dialog1.mp3',
      audioDuration: 45,
      questions: [
        {
          id: 'q1-1',
          questionText: 'What is the main reason for the flight delay?',
          order: 1
        },
        {
          id: 'q1-2', 
          questionText: 'How long is the expected delay time?',
          order: 2
        },
        {
          id: 'q1-3',
          questionText: 'What weather conditions are mentioned?',
          order: 3
        }
      ]
    },
    {
      id: 'dialog-2',
      title: '紧急医疗情况',
      content: 'Mayday, mayday, mayday. This is Flight 456. We have a medical emergency on board. A passenger is experiencing severe chest pain and difficulty breathing. We request immediate medical assistance upon landing and priority approach to the nearest airport. We have 180 passengers and 8 crew members on board.',
      audioUrl: '/demo/audio/dialog2.mp3',
      audioDuration: 50,
      questions: [
        {
          id: 'q2-1',
          questionText: 'What type of emergency is reported?',
          order: 1
        },
        {
          id: 'q2-2',
          questionText: 'What assistance is requested?',
          order: 2
        },
        {
          id: 'q2-3',
          questionText: 'How many people are on board?',
          order: 3
        }
      ]
    }
  ]
})

// 表单数据
const examForm = reactive({
  id: null,
  name: '',
  type: '',
  description: '',
  paperId: null,
  duration: 120,
  passingScore: 60,
  startTime: '',
  endTime: '',
  maxParticipants: 100,
  retakeLimit: 0,
  scoringMethod: 'auto',
  settings: {
    randomQuestions: false,
    randomOptions: false,
    showScore: true,
    allowReview: false,
    preventCheating: false,
    autoSubmit: true
  },
  remark: ''
})

// 统计数据
const examStats = ref([
  { type: 'papers', label: '试卷总数', count: 0, icon: 'Document', color: '#409EFF' },
  { type: 'exams', label: '考试总数', count: 0, icon: 'Clock', color: '#67C23A' },
  { type: 'results', label: '考试结果', count: 0, icon: 'TrendCharts', color: '#E6A23C' },
  { type: 'completed', label: '已完成', count: 0, icon: 'Medal', color: '#909399' }
])

// 选项数据
const examTypes = computed(() => examApi.getPaperTypeOptions())
const scoringMethods = computed(() => examApi.getScoringMethodOptions())

// 计算属性
const totalExams = computed(() => {
  return examStats.value.reduce((total, stat) => total + stat.count, 0)
})

// 表单验证规则
const examRules = {
  name: [
    { required: true, message: '请输入考试名称', trigger: 'blur' },
    { min: 2, max: 100, message: '考试名称长度为2-100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择考试类型', trigger: 'change' }
  ],
  paperId: [
    { required: true, message: '请选择试卷', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请设置考试时长', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

// 方法
const loadExamData = async () => {
  try {
    loading.value = true
    
    // 并行加载各种数据
    const [papersRes, examsRes, resultsRes] = await Promise.all([
      examApi.getExamPapers(),
      examApi.getExams(),
      examApi.getAllExamResults()
    ])
    
    examPapers.value = papersRes.data?.content || []
    examList.value = examsRes.data?.content || []
    examResults.value = resultsRes.data?.content || []
    
    updateStats()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  examStats.value[0].count = examPapers.value.length
  examStats.value[1].count = examList.value.length
  examStats.value[2].count = examResults.value.length
  examStats.value[3].count = examList.value.filter(exam => exam.status === 'ended').length
}

const handleTabClick = (tab) => {
  activeTab.value = tab.name
  // 根据标签页加载相应数据
}

// 考试管理方法
const createExam = () => {
  resetExamForm()
  examDialogVisible.value = true
}

const addExam = () => {
  createExam()
}

const editExam = (exam) => {
  Object.assign(examForm, {
    ...exam,
    settings: exam.settings || {
      randomQuestions: false,
      randomOptions: false,
      showScore: true,
      allowReview: false,
      preventCheating: false,
      autoSubmit: true
    }
  })
  examDialogVisible.value = true
}

const saveExam = async () => {
  if (!examFormRef.value) return
  
  try {
    await examFormRef.value.validate()
    saving.value = true
    
    if (examForm.id) {
      await examApi.updateExam(examForm.id, examForm)
      ElMessage.success('考试更新成功')
    } else {
      await examApi.createExam(examForm)
      ElMessage.success('考试创建成功')
    }
    
    examDialogVisible.value = false
    loadExamData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const saveDraft = async () => {
  try {
    saving.value = true
    const draftData = { ...examForm, status: 'draft' }
    
    if (examForm.id) {
      await examApi.updateExam(examForm.id, draftData)
    } else {
      await examApi.createExam(draftData)
    }
    
    ElMessage.success('草稿保存成功')
    examDialogVisible.value = false
    loadExamData()
  } catch (error) {
    ElMessage.error('保存草稿失败')
  } finally {
    saving.value = false
  }
}

const deleteExam = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除考试"${exam.name}"吗？此操作不可恢复。`,
      '确认删除',
      { type: 'warning' }
    )
    
    await examApi.deleteExam(exam.id)
    ElMessage.success('删除成功')
    loadExamData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const startExam = async (exam) => {
  try {
    await examApi.startExam(exam.id)
    ElMessage.success('考试已开始')
    loadExamData()
  } catch (error) {
    ElMessage.error('启动考试失败')
  }
}

const endExam = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要结束考试"${exam.name}"吗？`,
      '确认结束考试',
      { type: 'warning' }
    )
    
    await examApi.endExam(exam.id)
    ElMessage.success('考试已结束')
    loadExamData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('结束考试失败')
    }
  }
}

// 试卷管理方法
const addPaper = () => {
  // 跳转到试卷创建页面或打开试卷创建对话框
  ElMessage.info('试卷创建功能开发中...')
}

const editPaper = (paper) => {
  ElMessage.info('试卷编辑功能开发中...')
}

const deletePaper = async (paper) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除试卷"${paper.title}"吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await examApi.deleteExamPaper(paper.id)
    ElMessage.success('删除成功')
    loadExamData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const copyPaper = async (paper) => {
  try {
    await examApi.copyExamPaper(paper.id)
    ElMessage.success('试卷复制成功')
    loadExamData()
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const publishPaper = async (paper) => {
  try {
    await examApi.publishExamPaper(paper.id)
    ElMessage.success('试卷发布成功')
    loadExamData()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const previewPaper = (paper) => {
  // 预览试卷
  ElMessage.info('试卷预览功能开发中...')
}

// 结果管理方法
const viewResultDetail = (result) => {
  ElMessage.info('结果详情功能开发中...')
}

const reScoreResult = async (result) => {
  try {
    await examApi.reScoreExamResult(result.id)
    ElMessage.success('重新评分成功')
    loadExamData()
  } catch (error) {
    ElMessage.error('重新评分失败')
  }
}

const exportResults = async (exam) => {
  try {
    const blob = await examApi.exportExamResults(exam.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${exam.name}_结果_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const viewStatistics = (exam) => {
  currentExam.value = exam
  statisticsDialogVisible.value = true
}


const viewParticipants = (exam) => {
  currentExam.value = exam
  participantsDialogVisible.value = true
}

const exportExamData = async () => {
  try {
    const blob = await examApi.exportExamResults()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `考试数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 听力简答考试事件处理
const handleExamStarted = (examData) => {
  console.log('听力简答考试开始:', examData)
  ElMessage.success('听力简答考试已开始')
}

const handleExamCompleted = (examResult) => {
  console.log('听力简答考试完成:', examResult)
  ElMessage.success('听力简答考试已完成')
  
  // 保存考试结果
  // 这里可以调用API保存考试结果
  // await examApi.saveListeningComprehensionResult(examResult)
}

const handleAnswerRecorded = (answerData) => {
  console.log('录音答案:', answerData)
  
  if (answerData.action === 'start') {
    console.log('开始录音:', answerData.questionId)
  } else if (answerData.action === 'stop') {
    console.log('结束录音:', answerData.questionId)
    // 这里可以处理录音数据的保存
  }
}

const resetExamForm = () => {
  Object.assign(examForm, {
    id: null,
    name: '',
    type: '',
    description: '',
    paperId: null,
    duration: 120,
    passingScore: 60,
    startTime: '',
    endTime: '',
    maxParticipants: 100,
    retakeLimit: 0,
    scoringMethod: 'auto',
    settings: {
      randomQuestions: false,
      randomOptions: false,
      showScore: true,
      allowReview: false,
      preventCheating: false,
      autoSubmit: true
    },
    remark: ''
  })
  
  if (examFormRef.value) {
    examFormRef.value.clearValidate()
  }
}

// 生命周期
onMounted(() => {
  loadExamData()
})
</script>

<style scoped>
.exam-management {
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

.exam-tabs {
  margin-top: 20px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.exam-dialog :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}

.exam-form {
  padding: 0 20px;
}

.exam-settings {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
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
  
  .exam-settings {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .exam-form {
    padding: 0 10px;
  }
}
</style>

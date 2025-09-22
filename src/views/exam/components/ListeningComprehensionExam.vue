<template>
  <div class="listening-comprehension-exam">
    <!-- 考试头部信息 -->
    <div class="exam-header">
      <div class="exam-info">
        <h3>听力简答部分</h3>
        <div class="exam-meta">
          <el-tag type="primary" size="small">
            <el-icon><Clock /></el-icon>
            总时长: {{ formatTime(totalDuration) }}
          </el-tag>
          <el-tag type="info" size="small">
            {{ config.totalDialogs }}段对话 · {{ config.totalQuestions }}道题目
          </el-tag>
        </div>
      </div>
      
      <div class="exam-controls" v-if="!isExamStarted">
        <el-button type="success" size="large" @click="startExam" :loading="loading">
          <el-icon><VideoPlay /></el-icon>
          开始考试
        </el-button>
      </div>
    </div>

    <!-- 考试说明 -->
    <el-card class="instruction-card" v-if="!isExamStarted">
      <template #header>
        <div class="instruction-header">
          <el-icon><InfoFilled /></el-icon>
          考试说明
        </div>
      </template>
      
      <div class="instructions">
        <el-alert
          title="重要提示"
          type="warning"
          :closable="false"
          style="margin-bottom: 16px;"
        >
          <template #default>
            <ul class="instruction-list">
              <li>本部分包含<strong>{{ config.totalDialogs }}段对话</strong>，每段对话后设<strong>{{ config.questionsPerDialog }}个问题</strong>，总共<strong>{{ config.totalQuestions }}道题目</strong></li>
              <li>每段对话长度约为<strong>{{ config.dialogWordCount.min }}-{{ config.dialogWordCount.max }}词</strong>，<strong>仅播放一遍</strong></li>
              <li>每个问题的回答时间为<strong>{{ config.answerTimePerQuestion }}秒</strong>，超时将自动跳转下一题</li>
              <li>请以<strong>口头形式</strong>作答，系统将进行录音</li>
              <li>总考试时间约<strong>{{ Math.ceil(totalDuration / 60) }}分钟</strong></li>
            </ul>
          </template>
        </el-alert>
        
        <div class="exam-flow">
          <h4>考试流程</h4>
          <el-steps :active="0" finish-status="success" align-center>
            <el-step title="播放对话" description="仔细听取对话内容" />
            <el-step title="回答问题" description="依次回答3个问题" />
            <el-step title="下一对话" description="重复以上流程" />
            <el-step title="完成考试" description="提交答案" />
          </el-steps>
        </div>
      </div>
    </el-card>

    <!-- 考试进行中 -->
    <div class="exam-content" v-if="isExamStarted">
      <!-- 整体进度 -->
      <div class="exam-progress">
        <div class="progress-header">
          <span>考试进度</span>
          <span>{{ currentDialogIndex + 1 }} / {{ config.totalDialogs }}段对话</span>
        </div>
        <el-progress 
          :percentage="overallProgress" 
          :color="getProgressColor()"
          :stroke-width="6"
        />
      </div>

      <!-- 当前对话卡片 -->
      <el-card class="dialog-card" v-if="currentDialog">
        <template #header>
          <div class="dialog-header">
            <div class="dialog-title">
              <el-icon><Service /></el-icon>
              第{{ currentDialogIndex + 1 }}段对话
            </div>
            <div class="dialog-status">
              <el-tag :type="getStageTagType(currentStage)">
                {{ getStageText(currentStage) }}
              </el-tag>
            </div>
          </div>
        </template>

        <!-- 音频播放阶段 -->
        <div class="audio-stage" v-if="currentStage === 'playing'">
          <div class="audio-info">
            <el-icon><Service /></el-icon>
            <span>正在播放对话，请仔细听取内容...</span>
          </div>
          
          <div class="audio-player">
            <audio 
              ref="audioRef"
              :src="currentDialog.audioUrl"
              @loadedmetadata="onAudioLoaded"
              @ended="onAudioEnded"
              @timeupdate="onAudioTimeUpdate"
            />
            
            <div class="audio-controls">
              <el-progress 
                :percentage="audioProgress" 
                color="#409EFF"
                :stroke-width="4"
                :show-text="false"
              />
              <div class="audio-time">
                {{ formatTime(audioCurrentTime) }} / {{ formatTime(audioDuration) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 问题回答阶段 -->
        <div class="question-stage" v-if="currentStage === 'answering'">
          <div class="question-info">
            <div class="question-header">
              <span class="question-number">问题 {{ currentQuestionIndex + 1 }} / {{ config.questionsPerDialog }}</span>
              <div class="timer-section">
                <el-icon><Clock /></el-icon>
                <span class="timer-text">{{ formatTime(questionTimeRemaining) }}</span>
                <el-progress 
                  type="circle" 
                  :percentage="questionTimeProgress"
                  :color="getTimerColor()"
                  :width="40"
                  :stroke-width="4"
                  :show-text="false"
                />
              </div>
            </div>
            
            <div class="question-content">
              <div class="question-text">
                {{ currentQuestion?.questionText }}
              </div>
              
              <div class="word-count-info">
                <el-tag size="small" type="info">
                  问题词数: {{ getWordCount(currentQuestion?.questionText) }}词
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 录音控制 -->
          <div class="recording-section">
            <div class="recording-info">
              <el-icon :class="{ 'recording': isRecording }" color="#F56C6C"><Microphone /></el-icon>
              <span>{{ isRecording ? '正在录音...' : '准备录音' }}</span>
              <el-tag :type="isRecording ? 'danger' : 'info'" size="small">
                {{ isRecording ? '录音中' : '待录音' }}
              </el-tag>
            </div>
            
            <div class="recording-controls">
              <el-button 
                type="danger" 
                :disabled="!canStartRecording"
                @click="toggleRecording"
                size="large"
              >
                <el-icon><Microphone /></el-icon>
                {{ isRecording ? '停止录音' : '开始录音' }}
              </el-button>
              
              <el-button 
                type="primary" 
                @click="nextQuestion"
                :disabled="!canProceed"
              >
                <el-icon><Right /></el-icon>
                {{ isLastQuestion ? '下一对话' : '下一问题' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 对话完成阶段 -->
        <div class="dialog-complete" v-if="currentStage === 'completed'">
          <div class="complete-info">
            <el-icon color="#67C23A" size="32"><SuccessFilled /></el-icon>
            <span>第{{ currentDialogIndex + 1 }}段对话已完成</span>
          </div>
          
          <div class="complete-actions" v-if="!isLastDialog">
            <el-button type="primary" @click="nextDialog" size="large">
              <el-icon><Right /></el-icon>
              开始第{{ currentDialogIndex + 2 }}段对话
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 考试完成 -->
      <el-card class="exam-complete-card" v-if="isExamCompleted">
        <div class="exam-complete">
          <div class="complete-header">
            <el-icon color="#67C23A" size="48"><Medal /></el-icon>
            <h3>听力简答部分已完成</h3>
          </div>
          
          <div class="exam-summary">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label">完成对话</div>
                  <div class="summary-value">{{ completedDialogs }} / {{ config.totalDialogs }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label">回答问题</div>
                  <div class="summary-value">{{ answeredQuestions }} / {{ config.totalQuestions }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label">用时</div>
                  <div class="summary-value">{{ formatTime(examDuration) }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
          
          <div class="complete-actions">
            <el-button type="success" size="large" @click="submitExam">
              <el-icon><Check /></el-icon>
              提交答案
            </el-button>
            <el-button @click="reviewAnswers">
              <el-icon><View /></el-icon>
              查看回答
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 考试数据预览（管理员模式） -->
    <el-card class="admin-preview" v-if="showAdminPreview && examData">
      <template #header>
        <div class="admin-header">
          <el-icon><Setting /></el-icon>
          考试数据预览（管理员模式）
        </div>
      </template>
      
      <div class="exam-data-preview">
        <el-tabs v-model="previewTab">
          <el-tab-pane label="对话内容" name="dialogs">
            <div class="dialogs-preview">
              <div 
                v-for="(dialog, index) in examData.dialogs" 
                :key="dialog.id"
                class="dialog-preview"
              >
                <h4>第{{ index + 1 }}段对话 ({{ getWordCount(dialog.content) }}词)</h4>
                <div class="dialog-content">{{ dialog.content }}</div>
                
                <div class="dialog-questions">
                  <h5>问题列表</h5>
                  <ol>
                    <li 
                      v-for="question in dialog.questions" 
                      :key="question.id"
                      class="question-item"
                    >
                      {{ question.questionText }}
                      <el-tag size="small" type="info">{{ getWordCount(question.questionText) }}词</el-tag>
                    </li>
                  </ol>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="考试配置" name="config">
            <ListeningComprehensionConfig :config="config" />
          </el-tab-pane>
          
          <el-tab-pane label="评分标准" name="scoring">
            <ListeningComprehensionScoring :criteria="scoringCriteria" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionBankApi } from '@/api/questionBank'
import ListeningComprehensionConfig from './ListeningComprehensionConfig.vue'
import ListeningComprehensionScoring from './ListeningComprehensionScoring.vue'
import {
  Clock,
  VideoPlay,
  InfoFilled,
  Service,
  Microphone,
  Right,
  SuccessFilled,
  Medal,
  Check,
  View,
  Setting
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  examData: {
    type: Object,
    required: true
  },
  showAdminPreview: {
    type: Boolean,
    default: false
  },
  autoStart: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['exam-completed', 'answer-recorded', 'exam-started'])

// 响应式数据
const loading = ref(false)
const isExamStarted = ref(false)
const isExamCompleted = ref(false)
const currentDialogIndex = ref(0)
const currentQuestionIndex = ref(0)
const currentStage = ref('playing') // 'playing', 'answering', 'completed'

const audioRef = ref(null)
const audioDuration = ref(0)
const audioCurrentTime = ref(0)
const audioProgress = ref(0)

const questionTimeRemaining = ref(0)
const questionTimeProgress = ref(100)
const isRecording = ref(false)
const canStartRecording = ref(false)
const canProceed = ref(false)

const examStartTime = ref(null)
const examDuration = ref(0)
const previewTab = ref('dialogs')

const answers = ref([])
const completedDialogs = ref(0)
const answeredQuestions = ref(0)

let questionTimer = null
let examTimer = null

// 计算属性
const config = computed(() => questionBankApi.getListeningComprehensionExamConfig())
const scoringCriteria = computed(() => questionBankApi.getListeningComprehensionScoringCriteria())

const currentDialog = computed(() => {
  return props.examData?.dialogs?.[currentDialogIndex.value]
})

const currentQuestion = computed(() => {
  return currentDialog.value?.questions?.[currentQuestionIndex.value]
})

const totalDuration = computed(() => {
  if (!props.examData?.dialogs) return 0
  return questionBankApi.calculateTotalExamDuration(props.examData.dialogs, config.value)
})

const overallProgress = computed(() => {
  const totalQuestions = config.value.totalQuestions
  const currentProgress = (currentDialogIndex.value * config.value.questionsPerDialog) + currentQuestionIndex.value
  return Math.floor((currentProgress / totalQuestions) * 100)
})

const isLastDialog = computed(() => {
  return currentDialogIndex.value >= config.value.totalDialogs - 1
})

const isLastQuestion = computed(() => {
  return currentQuestionIndex.value >= config.value.questionsPerDialog - 1
})

// 监听器
watch(() => props.autoStart, (autoStart) => {
  if (autoStart && !isExamStarted.value) {
    startExam()
  }
})

// 方法
const startExam = async () => {
  try {
    // 验证考试数据
    const validation = questionBankApi.validateListeningComprehensionExam(props.examData)
    if (!validation.isValid) {
      ElMessage.error(validation.errors[0])
      return
    }
    
    loading.value = true
    
    // 初始化考试状态
    isExamStarted.value = true
    examStartTime.value = Date.now()
    currentDialogIndex.value = 0
    currentQuestionIndex.value = 0
    currentStage.value = 'playing'
    answers.value = []
    
    // 开始第一段对话
    await playCurrentDialog()
    
    emit('exam-started', {
      startTime: examStartTime.value,
      examData: props.examData
    })
    
  } catch (error) {
    ElMessage.error('考试启动失败')
  } finally {
    loading.value = false
  }
}

const playCurrentDialog = async () => {
  if (!currentDialog.value?.audioUrl) {
    ElMessage.error('音频文件不存在')
    return
  }
  
  currentStage.value = 'playing'
  canStartRecording.value = false
  canProceed.value = false
  
  // 播放音频
  if (audioRef.value) {
    audioRef.value.src = currentDialog.value.audioUrl
    audioRef.value.load()
    
    try {
      await audioRef.value.play()
    } catch (error) {
      ElMessage.error('音频播放失败，请检查浏览器设置')
    }
  }
}

const onAudioLoaded = () => {
  if (audioRef.value) {
    audioDuration.value = audioRef.value.duration
  }
}

const onAudioTimeUpdate = () => {
  if (audioRef.value) {
    audioCurrentTime.value = audioRef.value.currentTime
    audioProgress.value = (audioCurrentTime.value / audioDuration.value) * 100
  }
}

const onAudioEnded = () => {
  // 音频播放结束，开始回答问题
  currentStage.value = 'answering'
  currentQuestionIndex.value = 0
  startQuestionTimer()
}

const startQuestionTimer = () => {
  questionTimeRemaining.value = config.value.answerTimePerQuestion
  questionTimeProgress.value = 100
  canStartRecording.value = true
  canProceed.value = false
  
  questionTimer = setInterval(() => {
    questionTimeRemaining.value--
    questionTimeProgress.value = (questionTimeRemaining.value / config.value.answerTimePerQuestion) * 100
    
    if (questionTimeRemaining.value <= 0) {
      clearInterval(questionTimer)
      // 自动跳转下一题
      if (isRecording.value) {
        stopRecording()
      }
      nextQuestion()
    }
  }, 1000)
}

const toggleRecording = () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

const startRecording = () => {
  if (!canStartRecording.value) return
  
  isRecording.value = true
  canProceed.value = true
  
  // 这里应该调用实际的录音API
  // navigator.mediaDevices.getUserMedia({ audio: true })
  
  emit('answer-recorded', {
    dialogIndex: currentDialogIndex.value,
    questionIndex: currentQuestionIndex.value,
    questionId: currentQuestion.value?.id,
    action: 'start'
  })
}

const stopRecording = () => {
  if (!isRecording.value) return
  
  isRecording.value = false
  
  // 保存录音答案
  const answer = {
    dialogIndex: currentDialogIndex.value,
    questionIndex: currentQuestionIndex.value,
    questionId: currentQuestion.value?.id,
    questionText: currentQuestion.value?.questionText,
    recordingUrl: '', // 实际录音文件URL
    answeredAt: Date.now(),
    duration: config.value.answerTimePerQuestion - questionTimeRemaining.value
  }
  
  answers.value.push(answer)
  answeredQuestions.value++
  
  emit('answer-recorded', {
    ...answer,
    action: 'stop'
  })
}

const nextQuestion = () => {
  if (questionTimer) {
    clearInterval(questionTimer)
  }
  
  if (isRecording.value) {
    stopRecording()
  }
  
  if (isLastQuestion.value) {
    // 当前对话的所有问题已完成
    currentStage.value = 'completed'
    completedDialogs.value++
    
    if (isLastDialog.value) {
      // 所有对话都已完成
      completeExam()
    }
  } else {
    // 下一个问题
    currentQuestionIndex.value++
    startQuestionTimer()
  }
}

const nextDialog = () => {
  if (!isLastDialog.value) {
    currentDialogIndex.value++
    currentQuestionIndex.value = 0
    currentStage.value = 'playing'
    playCurrentDialog()
  }
}

const completeExam = () => {
  isExamCompleted.value = true
  examDuration.value = Math.floor((Date.now() - examStartTime.value) / 1000)
  
  if (examTimer) {
    clearInterval(examTimer)
  }
}

const submitExam = async () => {
  try {
    const examResult = {
      examId: props.examData.id,
      startTime: examStartTime.value,
      endTime: Date.now(),
      duration: examDuration.value,
      answers: answers.value,
      completedDialogs: completedDialogs.value,
      answeredQuestions: answeredQuestions.value,
      totalQuestions: config.value.totalQuestions
    }
    
    emit('exam-completed', examResult)
    ElMessage.success('考试提交成功')
    
  } catch (error) {
    ElMessage.error('考试提交失败')
  }
}

const reviewAnswers = () => {
  ElMessage.info('答案回顾功能开发中...')
}

// 工具方法
const formatTime = (seconds) => {
  if (!seconds) return '0:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const getWordCount = (text) => {
  return questionBankApi.getWordCount(text)
}

const getProgressColor = () => {
  if (overallProgress.value < 30) return '#F56C6C'
  if (overallProgress.value < 70) return '#E6A23C'
  return '#67C23A'
}

const getTimerColor = () => {
  if (questionTimeProgress.value < 25) return '#F56C6C'
  if (questionTimeProgress.value < 50) return '#E6A23C'
  return '#67C23A'
}

const getStageTagType = (stage) => {
  const typeMap = {
    'playing': 'primary',
    'answering': 'warning',
    'completed': 'success'
  }
  return typeMap[stage] || 'info'
}

const getStageText = (stage) => {
  const textMap = {
    'playing': '播放中',
    'answering': '答题中',
    'completed': '已完成'
  }
  return textMap[stage] || '未知'
}

// 生命周期
onMounted(() => {
  // 启动考试计时器
  examTimer = setInterval(() => {
    if (examStartTime.value) {
      examDuration.value = Math.floor((Date.now() - examStartTime.value) / 1000)
    }
  }, 1000)
})

onUnmounted(() => {
  if (questionTimer) {
    clearInterval(questionTimer)
  }
  if (examTimer) {
    clearInterval(examTimer)
  }
})
</script>

<style scoped>
.listening-comprehension-exam {
  padding: 0;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #409EFF, #67C23A);
  border-radius: 12px;
  color: white;
}

.exam-info h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.exam-meta {
  display: flex;
  gap: 12px;
}

.instruction-card {
  margin-bottom: 24px;
}

.instruction-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.instruction-list {
  margin: 0;
  padding-left: 20px;
  line-height: 1.8;
}

.instruction-list li {
  margin-bottom: 8px;
}

.exam-flow {
  margin-top: 20px;
}

.exam-flow h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.exam-progress {
  margin-bottom: 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.dialog-card {
  margin-bottom: 20px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.audio-stage {
  text-align: center;
  padding: 40px 20px;
}

.audio-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 16px;
  color: #409EFF;
}

.audio-player {
  max-width: 400px;
  margin: 0 auto;
}

.audio-controls {
  margin-top: 16px;
}

.audio-time {
  text-align: center;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.question-stage {
  padding: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-number {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.timer-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timer-text {
  font-size: 16px;
  font-weight: 600;
  color: #E6A23C;
}

.question-content {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.question-text {
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 12px;
}

.word-count-info {
  text-align: right;
}

.recording-section {
  text-align: center;
}

.recording-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 16px;
}

.recording-info .el-icon.recording {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.recording-controls {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.dialog-complete {
  text-align: center;
  padding: 40px 20px;
}

.complete-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;
  font-size: 18px;
  color: #67C23A;
}

.exam-complete-card {
  margin-top: 20px;
}

.exam-complete {
  text-align: center;
  padding: 40px 20px;
}

.complete-header {
  margin-bottom: 30px;
}

.complete-header h3 {
  margin: 12px 0 0 0;
  color: #303133;
}

.exam-summary {
  margin: 30px 0;
}

.summary-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.complete-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.admin-preview {
  margin-top: 24px;
  border: 2px dashed #409EFF;
}

.admin-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409EFF;
  font-weight: 600;
}

.dialogs-preview {
  max-height: 400px;
  overflow-y: auto;
}

.dialog-preview {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.dialog-preview h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.dialog-content {
  background: white;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
  line-height: 1.6;
}

.dialog-questions h5 {
  margin: 0 0 8px 0;
  color: #606266;
}

.question-item {
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .question-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .recording-controls {
    flex-direction: column;
    gap: 12px;
  }
  
  .complete-actions {
    flex-direction: column;
    gap: 12px;
  }
}
</style>

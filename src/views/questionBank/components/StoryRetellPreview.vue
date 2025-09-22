<template>
  <el-dialog
    v-model="visible"
    title="故事复述题预览"
    width="800px"
    :close-on-click-modal="false"
    @close="$emit('close')"
  >
    <div class="story-preview">
      <div class="preview-header">
        <h3>{{ story?.title || '故事复述题' }}</h3>
        <div class="story-meta">
          <el-tag :type="getDifficultyTagType(story?.difficulty)" size="small">
            {{ formatDifficultyLevel(story?.difficulty) }}
          </el-tag>
          <span class="word-count">{{ getWordCount(story?.content) }}词</span>
        </div>
      </div>

      <div class="preview-content">
        <!-- 故事内容 -->
        <div class="content-section">
          <h4>
            <el-icon><Document /></el-icon>
            故事内容
          </h4>
          <div class="story-content">
            {{ story?.content || '暂无内容' }}
          </div>
        </div>

        <!-- 音频播放 -->
        <div class="audio-section" v-if="story?.audioUrl">
          <h4>
            <el-icon><Service /></el-icon>
            音频文件
          </h4>
          <div class="audio-player">
            <audio 
              ref="audioRef"
              :src="story.audioUrl" 
              controls 
              style="width: 100%;"
              @loadedmetadata="onAudioLoaded"
            />
            <div class="audio-info">
              <span>时长: {{ formatTime(audioDuration) }}</span>
              <span>播放次数: {{ story?.playCount || 2 }}次</span>
              <span>播放间隔: {{ story?.playInterval || 5 }}秒</span>
            </div>
          </div>
        </div>

        <!-- 考试流程演示 -->
        <div class="exam-flow-section">
          <h4>
            <el-icon><Clock /></el-icon>
            考试流程演示
          </h4>
          
          <div class="flow-steps">
            <el-steps :active="currentStep" finish-status="success" align-center>
              <el-step title="播放音频" description="听取故事内容" />
              <el-step title="准备时间" description="理解并准备复述" />
              <el-step title="开始复述" description="口头复述故事" />
              <el-step title="完成" description="自动跳转下题" />
            </el-steps>
          </div>

          <div class="flow-controls">
            <el-button type="primary" @click="startDemo" :disabled="isRunning">
              <el-icon><VideoPlay /></el-icon>
              开始演示
            </el-button>
            <el-button @click="resetDemo" :disabled="isRunning">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>

          <!-- 当前阶段信息 -->
          <div class="current-stage" v-if="isRunning">
            <el-card class="stage-card">
              <template #header>
                <div class="stage-header">
                  <span>{{ getCurrentStageTitle() }}</span>
                  <el-tag :type="getStageTagType()">{{ getCurrentStageStatus() }}</el-tag>
                </div>
              </template>
              
              <div class="stage-content">
                <div class="stage-description">
                  {{ getCurrentStageDescription() }}
                </div>
                
                <div class="stage-timer" v-if="showTimer">
                  <el-progress 
                    :percentage="timerProgress" 
                    :color="getProgressColor()"
                    :stroke-width="8"
                  />
                  <div class="timer-text">
                    剩余时间: {{ formatTime(remainingTime) }}
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 考试配置 -->
        <div class="config-section">
          <h4>
            <el-icon><Setting /></el-icon>
            考试配置
          </h4>
          
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="config-item">
                <div class="config-label">准备时间</div>
                <div class="config-value">{{ formatTime(story?.preparationTime || 300) }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="config-item">
                <div class="config-label">回答时间</div>
                <div class="config-value">{{ formatTime(story?.responseTime || 300) }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="config-item">
                <div class="config-label">播放次数</div>
                <div class="config-value">{{ story?.playCount || 2 }}次</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="config-item">
                <div class="config-label">播放间隔</div>
                <div class="config-value">{{ story?.playInterval || 5 }}秒</div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: 16px;">
            <el-col :span="8">
              <div class="config-item">
                <div class="config-label">自动跳转</div>
                <div class="config-value">
                  <el-tag :type="story?.autoNext ? 'success' : 'danger'" size="small">
                    {{ story?.autoNext ? '是' : '否' }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="config-item">
                <div class="config-label">显示计时器</div>
                <div class="config-value">
                  <el-tag :type="story?.showTimer ? 'success' : 'danger'" size="small">
                    {{ story?.showTimer ? '是' : '否' }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="config-item">
                <div class="config-label">状态</div>
                <div class="config-value">
                  <el-tag :type="story?.enabled ? 'success' : 'danger'" size="small">
                    {{ story?.enabled ? '启用' : '禁用' }}
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 评分标准 -->
        <div class="scoring-section">
          <h4>
            <el-icon><Star /></el-icon>
            评分标准
          </h4>
          
          <el-table :data="scoringCriteria" size="small">
            <el-table-column prop="label" label="评分项目" width="120" />
            <el-table-column prop="weight" label="权重" width="80" align="center">
              <template #default="{ row }">
                {{ row.weight }}%
              </template>
            </el-table-column>
            <el-table-column prop="description" label="评分描述" />
          </el-table>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="$emit('close')">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onUnmounted } from 'vue'
import { questionBankApi } from '@/api/questionBank'
import {
  Document,
  Service,
  Clock,
  Setting,
  Star,
  VideoPlay,
  Refresh
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  story: {
    type: Object,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits(['close'])

// 响应式数据
const audioRef = ref(null)
const audioDuration = ref(0)
const currentStep = ref(0)
const isRunning = ref(false)
const showTimer = ref(false)
const remainingTime = ref(0)
const timerProgress = ref(0)

let demoTimer = null
let stageTimer = null

// 计算属性
const scoringCriteria = computed(() => questionBankApi.getStoryRetellScoringCriteria())

// 监听器
watch(() => props.visible, (visible) => {
  if (!visible) {
    resetDemo()
  }
})

// 方法
const onAudioLoaded = () => {
  if (audioRef.value) {
    audioDuration.value = audioRef.value.duration || 0
  }
}

const startDemo = async () => {
  if (isRunning.value) return
  
  isRunning.value = true
  currentStep.value = 0
  
  // 第一步：播放音频
  await playAudioStage()
  
  // 第二步：准备时间
  await preparationStage()
  
  // 第三步：回答时间
  await responseStage()
  
  // 第四步：完成
  completeStage()
}

const playAudioStage = () => {
  return new Promise((resolve) => {
    currentStep.value = 0
    const playCount = props.story?.playCount || 2
    const playInterval = (props.story?.playInterval || 5) * 1000
    let currentPlay = 0
    
    const playNext = () => {
      if (currentPlay < playCount) {
        currentPlay++
        
        // 模拟音频播放时间（使用音频实际时长或默认10秒）
        const playDuration = (audioDuration.value || 10) * 1000
        
        setTimeout(() => {
          if (currentPlay < playCount) {
            // 播放间隔
            setTimeout(playNext, playInterval)
          } else {
            resolve()
          }
        }, playDuration)
      }
    }
    
    playNext()
  })
}

const preparationStage = () => {
  return new Promise((resolve) => {
    currentStep.value = 1
    const prepTime = props.story?.preparationTime || 300
    
    if (props.story?.showTimer) {
      startTimer(prepTime, resolve)
    } else {
      setTimeout(resolve, prepTime * 1000)
    }
  })
}

const responseStage = () => {
  return new Promise((resolve) => {
    currentStep.value = 2
    const respTime = props.story?.responseTime || 300
    
    if (props.story?.showTimer) {
      startTimer(respTime, resolve)
    } else {
      setTimeout(resolve, respTime * 1000)
    }
  })
}

const completeStage = () => {
  currentStep.value = 3
  showTimer.value = false
  
  setTimeout(() => {
    isRunning.value = false
    currentStep.value = 0
  }, 2000)
}

const startTimer = (duration, callback) => {
  showTimer.value = true
  remainingTime.value = duration
  timerProgress.value = 100
  
  const startTime = Date.now()
  const endTime = startTime + duration * 1000
  
  stageTimer = setInterval(() => {
    const now = Date.now()
    const remaining = Math.max(0, Math.ceil((endTime - now) / 1000))
    
    remainingTime.value = remaining
    timerProgress.value = (remaining / duration) * 100
    
    if (remaining <= 0) {
      clearInterval(stageTimer)
      showTimer.value = false
      callback()
    }
  }, 100)
}

const resetDemo = () => {
  if (demoTimer) {
    clearTimeout(demoTimer)
    demoTimer = null
  }
  
  if (stageTimer) {
    clearInterval(stageTimer)
    stageTimer = null
  }
  
  isRunning.value = false
  currentStep.value = 0
  showTimer.value = false
  remainingTime.value = 0
  timerProgress.value = 0
}

const getCurrentStageTitle = () => {
  const titles = [
    '播放音频阶段',
    '准备时间阶段',
    '复述回答阶段',
    '完成阶段'
  ]
  return titles[currentStep.value] || ''
}

const getCurrentStageStatus = () => {
  const statuses = [
    '听取中',
    '准备中',
    '回答中',
    '已完成'
  ]
  return statuses[currentStep.value] || ''
}

const getCurrentStageDescription = () => {
  const descriptions = [
    `音频将播放 ${props.story?.playCount || 2} 次，每次播放间隔 ${props.story?.playInterval || 5} 秒`,
    `考生有 ${formatTime(props.story?.preparationTime || 300)} 的时间理解故事内容并准备复述`,
    `考生需要在 ${formatTime(props.story?.responseTime || 300)} 内完成口头复述`,
    '复述完成，系统将自动跳转到下一题'
  ]
  return descriptions[currentStep.value] || ''
}

const getStageTagType = () => {
  const types = ['primary', 'warning', 'success', 'info']
  return types[currentStep.value] || 'info'
}

const getProgressColor = () => {
  if (remainingTime.value <= 30) return '#F56C6C'
  if (remainingTime.value <= 60) return '#E6A23C'
  return '#67C23A'
}

// 工具方法
const formatTime = (seconds) => {
  if (!seconds) return '0秒'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return minutes > 0 ? `${minutes}分${remainingSeconds}秒` : `${remainingSeconds}秒`
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

const getWordCount = (text) => {
  if (!text) return 0
  return text.trim().split(/\s+/).length
}

// 清理定时器
onUnmounted(() => {
  resetDemo()
})
</script>

<style scoped>
.story-preview {
  padding: 0;
}

.preview-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.preview-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.story-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.word-count {
  background: #f0f2f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.preview-content {
  max-height: 600px;
  overflow-y: auto;
}

.content-section,
.audio-section,
.exam-flow-section,
.config-section,
.scoring-section {
  margin-bottom: 24px;
}

.content-section h4,
.audio-section h4,
.exam-flow-section h4,
.config-section h4,
.scoring-section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.story-content {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  line-height: 1.8;
  color: #303133;
}

.audio-player {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.audio-info {
  display: flex;
  gap: 20px;
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
}

.flow-steps {
  margin: 16px 0;
}

.flow-controls {
  text-align: center;
  margin: 16px 0;
}

.current-stage {
  margin-top: 20px;
}

.stage-card {
  border: 2px solid #409EFF;
}

.stage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stage-content {
  padding: 16px 0;
}

.stage-description {
  margin-bottom: 16px;
  color: #606266;
  line-height: 1.6;
}

.stage-timer {
  text-align: center;
}

.timer-text {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.config-item {
  text-align: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.config-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.config-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .story-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .audio-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .flow-controls {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}
</style>

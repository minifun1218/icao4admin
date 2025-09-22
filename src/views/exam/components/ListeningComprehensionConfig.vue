<template>
  <div class="listening-comprehension-config">
    <div class="config-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="config-card">
            <div class="config-item">
              <div class="config-icon">
                <el-icon color="#409EFF" size="24"><ChatDotRound /></el-icon>
              </div>
              <div class="config-content">
                <div class="config-label">对话数量</div>
                <div class="config-value">{{ config.totalDialogs }}段</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="config-card">
            <div class="config-item">
              <div class="config-icon">
                <el-icon color="#67C23A" size="24"><QuestionFilled /></el-icon>
              </div>
              <div class="config-content">
                <div class="config-label">总题目数</div>
                <div class="config-value">{{ config.totalQuestions }}道</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="config-card">
            <div class="config-item">
              <div class="config-icon">
                <el-icon color="#E6A23C" size="24"><Clock /></el-icon>
              </div>
              <div class="config-content">
                <div class="config-label">考试时长</div>
                <div class="config-value">{{ Math.ceil(config.totalExamTime / 60) }}分钟</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="config-card">
            <div class="config-item">
              <div class="config-icon">
                <el-icon color="#F56C6C" size="24"><Timer /></el-icon>
              </div>
              <div class="config-content">
                <div class="config-label">答题时间</div>
                <div class="config-value">{{ config.answerTimePerQuestion }}秒/题</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="config-details">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <el-icon><Document /></el-icon>
                <span>内容要求</span>
              </div>
            </template>
            
            <div class="config-section">
              <div class="config-row">
                <span class="label">对话长度:</span>
                <span class="value">{{ config.dialogWordCount.min }}-{{ config.dialogWordCount.max }}词</span>
              </div>
              
              <div class="config-row">
                <span class="label">问题字数限制:</span>
                <span class="value">≤{{ config.questionWordLimit }}词</span>
              </div>
              
              <div class="config-row">
                <span class="label">每段对话问题数:</span>
                <span class="value">{{ config.questionsPerDialog }}个</span>
              </div>
              
              <div class="config-row">
                <span class="label">回答方式:</span>
                <span class="value">口头作答</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <el-icon><Service /></el-icon>
                <span>播放设置</span>
              </div>
            </template>
            
            <div class="config-section">
              <div class="config-row">
                <span class="label">播放次数:</span>
                <span class="value">{{ config.playCount }}次</span>
                <el-tag size="small" type="warning">仅播放一遍</el-tag>
              </div>
              
              <div class="config-row">
                <span class="label">自动跳转:</span>
                <span class="value">
                  <el-tag :type="config.autoNext ? 'success' : 'danger'" size="small">
                    {{ config.autoNext ? '启用' : '禁用' }}
                  </el-tag>
                </span>
              </div>
              
              <div class="config-row">
                <span class="label">录音功能:</span>
                <span class="value">
                  <el-tag :type="config.recordResponse ? 'success' : 'danger'" size="small">
                    {{ config.recordResponse ? '启用' : '禁用' }}
                  </el-tag>
                </span>
              </div>
              
              <div class="config-row">
                <span class="label">显示计时器:</span>
                <span class="value">
                  <el-tag :type="config.showTimer ? 'success' : 'danger'" size="small">
                    {{ config.showTimer ? '显示' : '隐藏' }}
                  </el-tag>
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="exam-flow">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><Connection /></el-icon>
            <span>考试流程</span>
          </div>
        </template>
        
        <div class="flow-diagram">
          <el-steps :active="4" finish-status="success" align-center>
            <el-step title="开始考试" description="进入考试界面" />
            <el-step title="播放对话" description="播放第1段对话（仅1次）" />
            <el-step title="回答问题" description="依次回答3个问题（20秒/题）" />
            <el-step title="下一对话" description="播放第2段对话" />
            <el-step title="完成考试" description="提交所有答案" />
          </el-steps>
        </div>
        
        <div class="flow-details">
          <el-alert
            title="详细流程说明"
            type="info"
            :closable="false"
          >
            <template #default>
              <ol class="flow-list">
                <li>
                  <strong>对话播放阶段</strong>：系统播放对话音频，考生仔细听取内容
                  <ul>
                    <li>每段对话长度约{{ config.dialogWordCount.min }}-{{ config.dialogWordCount.max }}词</li>
                    <li>音频<strong>仅播放一遍</strong>，无法重复播放</li>
                    <li>播放结束后自动进入答题阶段</li>
                  </ul>
                </li>
                <li>
                  <strong>问题回答阶段</strong>：依次回答3个问题
                  <ul>
                    <li>每个问题回答时间限制为<strong>{{ config.answerTimePerQuestion }}秒</strong></li>
                    <li>问题字数控制在<strong>{{ config.questionWordLimit }}词以内</strong></li>
                    <li>考生需<strong>口头作答</strong>，系统进行录音</li>
                    <li>超时将自动跳转下一题</li>
                  </ul>
                </li>
                <li>
                  <strong>流程重复</strong>：完成第1段对话后，重复以上流程处理第2段对话
                </li>
                <li>
                  <strong>考试完成</strong>：完成所有{{ config.totalQuestions }}道题目后提交答案
                </li>
              </ol>
            </template>
          </el-alert>
        </div>
      </el-card>
    </div>

    <div class="time-breakdown">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>时间分配</span>
          </div>
        </template>
        
        <div class="time-chart">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="time-item">
                <div class="time-category">音频播放</div>
                <div class="time-duration">约2-3分钟</div>
                <div class="time-description">两段对话播放时间</div>
              </div>
            </el-col>
            
            <el-col :span="8">
              <div class="time-item">
                <div class="time-category">问题回答</div>
                <div class="time-duration">{{ config.totalQuestions * config.answerTimePerQuestion }}秒</div>
                <div class="time-description">{{ config.totalQuestions }}题 × {{ config.answerTimePerQuestion }}秒</div>
              </div>
            </el-col>
            
            <el-col :span="8">
              <div class="time-item">
                <div class="time-category">总计时间</div>
                <div class="time-duration">约{{ Math.ceil(config.totalExamTime / 60) }}分钟</div>
                <div class="time-description">完整考试时长</div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <div class="time-tips">
          <el-alert
            title="时间管理提示"
            type="warning"
            :closable="false"
          >
            <template #default>
              <ul>
                <li>音频播放期间请专注听取，无法暂停或重播</li>
                <li>每题回答时间固定{{ config.answerTimePerQuestion }}秒，合理安排回答内容</li>
                <li>超时将自动跳转，建议简洁明了地表达要点</li>
                <li>整个考试过程系统自动控制，无需手动操作</li>
              </ul>
            </template>
          </el-alert>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import {
  ChatDotRound,
  QuestionFilled,
  Clock,
  Timer,
  Document,
  Service,
  Connection
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  config: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.listening-comprehension-config {
  padding: 0;
}

.config-overview {
  margin-bottom: 24px;
}

.config-card {
  height: 100%;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.config-icon {
  flex-shrink: 0;
}

.config-content {
  flex: 1;
}

.config-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.config-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.config-details {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.config-section {
  padding: 0;
}

.config-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.config-row:last-child {
  border-bottom: none;
}

.config-row .label {
  font-weight: 500;
  color: #606266;
}

.config-row .value {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.exam-flow {
  margin-bottom: 24px;
}

.flow-diagram {
  margin: 20px 0;
}

.flow-details {
  margin-top: 20px;
}

.flow-list {
  margin: 0;
  padding-left: 20px;
  line-height: 1.8;
}

.flow-list li {
  margin-bottom: 12px;
}

.flow-list ul {
  margin-top: 8px;
  margin-bottom: 0;
}

.flow-list ul li {
  margin-bottom: 4px;
}

.time-breakdown {
  margin-bottom: 24px;
}

.time-chart {
  margin: 20px 0;
}

.time-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  height: 100%;
}

.time-category {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.time-duration {
  font-size: 24px;
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 4px;
}

.time-description {
  font-size: 12px;
  color: #606266;
}

.time-tips {
  margin-top: 20px;
}

.time-tips ul {
  margin: 0;
  padding-left: 20px;
  line-height: 1.6;
}

.time-tips li {
  margin-bottom: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .config-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .time-item {
    margin-bottom: 16px;
  }
}
</style>

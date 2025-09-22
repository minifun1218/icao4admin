<template>
  <div class="listening-comprehension-scoring">
    <div class="scoring-overview">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><Star /></el-icon>
            <span>评分标准概览</span>
          </div>
        </template>
        
        <div class="overview-content">
          <el-alert
            title="听力简答部分评分说明"
            type="info"
            :closable="false"
            style="margin-bottom: 16px;"
          >
            <template #default>
              <p>听力简答部分采用多维度评分体系，综合考察考生的听力理解能力、口语表达能力和语言运用能力。</p>
              <p>评分基于以下5个维度，各维度权重不同，总分100分。</p>
            </template>
          </el-alert>
          
          <div class="weight-distribution">
            <el-row :gutter="20">
              <el-col :span="24">
                <div class="weight-chart">
                  <div 
                    v-for="criterion in criteria" 
                    :key="criterion.criteria"
                    class="weight-bar"
                  >
                    <div class="weight-info">
                      <span class="weight-label">{{ criterion.label }}</span>
                      <span class="weight-value">{{ criterion.weight }}%</span>
                    </div>
                    <div class="weight-progress">
                      <el-progress 
                        :percentage="criterion.weight" 
                        :color="getWeightColor(criterion.weight)"
                        :stroke-width="12"
                        :show-text="false"
                      />
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-card>
    </div>

    <div class="scoring-criteria">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <el-icon><Document /></el-icon>
                <span>详细评分标准</span>
              </div>
            </template>
            
            <div class="criteria-table">
              <el-table :data="criteria" stripe>
                <el-table-column label="评分维度" width="150">
                  <template #default="{ row }">
                    <div class="criterion-name">
                      <el-icon :color="getCriterionColor(row.criteria)">
                        <component :is="getCriterionIcon(row.criteria)" />
                      </el-icon>
                      <span>{{ row.label }}</span>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="权重" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :color="getWeightColor(row.weight)" size="small">
                      {{ row.weight }}%
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="评分描述" min-width="300">
                  <template #default="{ row }">
                    <div class="criterion-description">
                      {{ row.description }}
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="评分要点" min-width="400">
                  <template #default="{ row }">
                    <div class="scoring-points">
                      <ul>
                        <li v-for="point in getScoringPoints(row.criteria)" :key="point">
                          {{ point }}
                        </li>
                      </ul>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="scoring-levels">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><TrendCharts /></el-icon>
            <span>分数等级划分</span>
          </div>
        </template>
        
        <div class="levels-content">
          <el-row :gutter="20">
            <el-col :span="6" v-for="level in scoringLevels" :key="level.level">
              <div class="level-card" :class="`level-${level.level}`">
                <div class="level-score">{{ level.scoreRange }}</div>
                <div class="level-name">{{ level.name }}</div>
                <div class="level-description">{{ level.description }}</div>
                <div class="level-characteristics">
                  <ul>
                    <li v-for="char in level.characteristics" :key="char">
                      {{ char }}
                    </li>
                  </ul>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <div class="scoring-examples">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>评分示例</span>
          </div>
        </template>
        
        <div class="examples-content">
          <el-tabs v-model="activeExampleTab">
            <el-tab-pane 
              v-for="example in scoringExamples" 
              :key="example.level"
              :label="`${example.levelName}示例`" 
              :name="example.level"
            >
              <div class="example-content">
                <div class="example-header">
                  <el-tag :type="example.tagType" size="large">
                    {{ example.levelName }} ({{ example.scoreRange }}分)
                  </el-tag>
                </div>
                
                <div class="example-question">
                  <h4>示例问题</h4>
                  <div class="question-text">{{ example.question }}</div>
                </div>
                
                <div class="example-answer">
                  <h4>回答示例</h4>
                  <div class="answer-text">{{ example.answer }}</div>
                </div>
                
                <div class="example-analysis">
                  <h4>评分分析</h4>
                  <el-table :data="example.analysis" size="small">
                    <el-table-column prop="criterion" label="评分维度" width="120" />
                    <el-table-column prop="score" label="得分" width="80" align="center">
                      <template #default="{ row }">
                        <el-tag size="small" :type="getScoreTagType(row.score)">
                          {{ row.score }}分
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="comment" label="评分说明" />
                  </el-table>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-card>
    </div>

    <div class="scoring-tips">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span>评分建议</span>
          </div>
        </template>
        
        <div class="tips-content">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="tips-section">
                <h4>
                  <el-icon color="#67C23A"><Check /></el-icon>
                  提高分数的要点
                </h4>
                <ul class="tips-list good-tips">
                  <li>仔细听取对话内容，抓住关键信息</li>
                  <li>回答要点明确，逻辑清晰</li>
                  <li>使用准确的词汇和语法结构</li>
                  <li>发音清晰，语调自然</li>
                  <li>在规定时间内完整回答问题</li>
                  <li>避免长时间停顿或重复</li>
                </ul>
              </div>
            </el-col>
            
            <el-col :span="12">
              <div class="tips-section">
                <h4>
                  <el-icon color="#F56C6C"><Close /></el-icon>
                  常见失分点
                </h4>
                <ul class="tips-list bad-tips">
                  <li>回答与问题不相关或偏离主题</li>
                  <li>语法错误较多，影响理解</li>
                  <li>发音不清晰，难以理解</li>
                  <li>回答过于简短，缺乏必要信息</li>
                  <li>超时未能完成回答</li>
                  <li>过度使用填充词（如"嗯"、"那个"）</li>
                </ul>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Star,
  Document,
  TrendCharts,
  ChatDotRound,
  InfoFilled,
  Check,
  Close,
  Service,
  Clock,
  Microphone,
  EditPen,
  Timer
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  criteria: {
    type: Array,
    required: true
  }
})

// 响应式数据
const activeExampleTab = ref('excellent')

// 计算属性
const scoringLevels = computed(() => [
  {
    level: 'excellent',
    name: '优秀',
    scoreRange: '90-100',
    description: '全面掌握听力内容，回答准确完整',
    characteristics: [
      '完全理解对话内容',
      '回答准确且完整',
      '语言流利自然',
      '发音清晰标准'
    ]
  },
  {
    level: 'good',
    name: '良好',
    scoreRange: '80-89',
    description: '较好理解听力内容，回答基本准确',
    characteristics: [
      '理解大部分内容',
      '回答基本准确',
      '表达较为流利',
      '发音基本清晰'
    ]
  },
  {
    level: 'fair',
    name: '中等',
    scoreRange: '70-79',
    description: '部分理解听力内容，回答有所缺失',
    characteristics: [
      '理解部分内容',
      '回答不够完整',
      '表达有些停顿',
      '发音有待提高'
    ]
  },
  {
    level: 'poor',
    name: '需改进',
    scoreRange: '60-69',
    description: '理解有限，回答不够准确',
    characteristics: [
      '理解能力有限',
      '回答不够准确',
      '表达不够流利',
      '发音需要改进'
    ]
  }
])

const scoringExamples = computed(() => [
  {
    level: 'excellent',
    levelName: '优秀',
    scoreRange: '90-100',
    tagType: 'success',
    question: 'What was the main reason for the flight delay mentioned in the conversation?',
    answer: 'The main reason for the flight delay was severe weather conditions at the destination airport. The conversation mentioned that there were strong winds and heavy rain, which made it unsafe for aircraft to land. The air traffic control decided to hold all incoming flights until the weather improved.',
    analysis: [
      { criterion: '内容准确性', score: 33, comment: '完全理解对话内容，回答准确完整' },
      { criterion: '语言流利性', score: 23, comment: '表达流利自然，逻辑清晰' },
      { criterion: '发音清晰度', score: 19, comment: '发音清晰标准，易于理解' },
      { criterion: '语法正确性', score: 11, comment: '语法使用正确，结构完整' },
      { criterion: '回答时效性', score: 7, comment: '在规定时间内完整回答' }
    ]
  },
  {
    level: 'good',
    levelName: '良好',
    scoreRange: '80-89',
    tagType: 'primary',
    question: 'What was the main reason for the flight delay mentioned in the conversation?',
    answer: 'The flight was delayed because of bad weather. There was strong wind and rain at the airport, so planes could not land safely. Air traffic control had to wait until weather was better.',
    analysis: [
      { criterion: '内容准确性', score: 28, comment: '理解主要内容，回答基本准确' },
      { criterion: '语言流利性', score: 20, comment: '表达较为流利，有轻微停顿' },
      { criterion: '发音清晰度', score: 17, comment: '发音基本清晰，偶有不准确' },
      { criterion: '语法正确性', score: 10, comment: '语法基本正确，结构简单' },
      { criterion: '回答时效性', score: 6, comment: '及时完成回答' }
    ]
  },
  {
    level: 'fair',
    levelName: '中等',
    scoreRange: '70-79',
    tagType: 'warning',
    question: 'What was the main reason for the flight delay mentioned in the conversation?',
    answer: 'Uh... the flight delay... because weather is bad. Wind and rain... airport not safe for landing. Control tower... wait for weather good.',
    analysis: [
      { criterion: '内容准确性', score: 24, comment: '理解部分内容，回答不够完整' },
      { criterion: '语言流利性', score: 17, comment: '表达有停顿，不够流利' },
      { criterion: '发音清晰度', score: 14, comment: '发音基本可理解，有改进空间' },
      { criterion: '语法正确性', score: 8, comment: '语法错误较多，影响表达' },
      { criterion: '回答时效性', score: 5, comment: '回答较慢，时间利用不充分' }
    ]
  },
  {
    level: 'poor',
    levelName: '需改进',
    scoreRange: '60-69',
    tagType: 'danger',
    question: 'What was the main reason for the flight delay mentioned in the conversation?',
    answer: 'Weather... bad weather... uh... plane cannot... airport... wait...',
    analysis: [
      { criterion: '内容准确性', score: 18, comment: '理解有限，回答不够准确' },
      { criterion: '语言流利性', score: 12, comment: '表达不流利，多次停顿' },
      { criterion: '发音清晰度', score: 11, comment: '发音不够清晰，影响理解' },
      { criterion: '语法正确性', score: 6, comment: '语法错误多，结构不完整' },
      { criterion: '回答时效性', score: 3, comment: '未能在规定时间内完整回答' }
    ]
  }
])

// 方法
const getWeightColor = (weight) => {
  if (weight >= 30) return '#F56C6C'
  if (weight >= 20) return '#E6A23C'
  if (weight >= 15) return '#409EFF'
  return '#67C23A'
}

const getCriterionColor = (criteria) => {
  const colorMap = {
    'content_accuracy': '#F56C6C',
    'language_fluency': '#E6A23C',
    'pronunciation': '#409EFF',
    'grammar_usage': '#67C23A',
    'response_time': '#909399'
  }
  return colorMap[criteria] || '#409EFF'
}

const getCriterionIcon = (criteria) => {
  const iconMap = {
    'content_accuracy': 'Check',
    'language_fluency': 'Service',
    'pronunciation': 'Microphone',
    'grammar_usage': 'EditPen',
    'response_time': 'Timer'
  }
  return iconMap[criteria] || 'Star'
}

const getScoringPoints = (criteria) => {
  const pointsMap = {
    'content_accuracy': [
      '回答内容与问题高度相关',
      '准确理解对话中的关键信息',
      '回答完整，涵盖问题要点',
      '逻辑清晰，条理分明'
    ],
    'language_fluency': [
      '表达流利，语速适中',
      '语言连贯，过渡自然',
      '避免过多停顿和重复',
      '使用适当的连接词'
    ],
    'pronunciation': [
      '发音清晰准确',
      '语调自然得体',
      '重音和节奏恰当',
      '易于听懂和理解'
    ],
    'grammar_usage': [
      '语法结构正确',
      '时态使用恰当',
      '句式变化丰富',
      '避免基础语法错误'
    ],
    'response_time': [
      '在规定时间内完成回答',
      '时间分配合理',
      '回答简洁有效',
      '避免超时或过短'
    ]
  }
  return pointsMap[criteria] || []
}

const getScoreTagType = (score) => {
  if (score >= 25) return 'success'
  if (score >= 20) return 'primary'
  if (score >= 15) return 'warning'
  return 'danger'
}
</script>

<style scoped>
.listening-comprehension-scoring {
  padding: 0;
}

.scoring-overview {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.overview-content {
  padding: 0;
}

.weight-distribution {
  margin-top: 20px;
}

.weight-chart {
  padding: 0;
}

.weight-bar {
  margin-bottom: 16px;
}

.weight-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.weight-label {
  font-weight: 500;
  color: #303133;
}

.weight-value {
  font-weight: 600;
  color: #409EFF;
}

.weight-progress {
  width: 100%;
}

.scoring-criteria {
  margin-bottom: 24px;
}

.criteria-table {
  padding: 0;
}

.criterion-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.criterion-description {
  line-height: 1.6;
  color: #606266;
}

.scoring-points ul {
  margin: 0;
  padding-left: 16px;
  line-height: 1.6;
}

.scoring-points li {
  margin-bottom: 4px;
  color: #606266;
}

.scoring-levels {
  margin-bottom: 24px;
}

.levels-content {
  padding: 20px 0;
}

.level-card {
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  height: 100%;
  transition: transform 0.2s;
}

.level-card:hover {
  transform: translateY(-2px);
}

.level-card.level-excellent {
  background: linear-gradient(135deg, #67C23A20, #67C23A40);
  border: 2px solid #67C23A;
}

.level-card.level-good {
  background: linear-gradient(135deg, #409EFF20, #409EFF40);
  border: 2px solid #409EFF;
}

.level-card.level-fair {
  background: linear-gradient(135deg, #E6A23C20, #E6A23C40);
  border: 2px solid #E6A23C;
}

.level-card.level-poor {
  background: linear-gradient(135deg, #F56C6C20, #F56C6C40);
  border: 2px solid #F56C6C;
}

.level-score {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.level-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.level-description {
  font-size: 12px;
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.4;
}

.level-characteristics ul {
  margin: 0;
  padding-left: 16px;
  text-align: left;
  font-size: 12px;
  line-height: 1.4;
}

.level-characteristics li {
  margin-bottom: 4px;
  color: #606266;
}

.scoring-examples {
  margin-bottom: 24px;
}

.examples-content {
  padding: 20px 0;
}

.example-content {
  padding: 0;
}

.example-header {
  margin-bottom: 20px;
  text-align: center;
}

.example-question,
.example-answer,
.example-analysis {
  margin-bottom: 20px;
}

.example-question h4,
.example-answer h4,
.example-analysis h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.question-text,
.answer-text {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  line-height: 1.6;
  color: #303133;
}

.question-text {
  border-left: 4px solid #409EFF;
}

.answer-text {
  border-left: 4px solid #67C23A;
}

.scoring-tips {
  margin-bottom: 24px;
}

.tips-content {
  padding: 20px 0;
}

.tips-section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  font-size: 16px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  line-height: 1.8;
}

.tips-list li {
  margin-bottom: 8px;
}

.good-tips li {
  color: #67C23A;
}

.bad-tips li {
  color: #F56C6C;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .weight-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .levels-content .el-col {
    margin-bottom: 16px;
  }
  
  .tips-section {
    margin-bottom: 24px;
  }
}
</style>

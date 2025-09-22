<template>
  <div class="exam-result-panel">
    <!-- 操作栏 -->
    <div class="panel-header">
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索考试名称或考生姓名"
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
            <el-select v-model="searchForm.examType" placeholder="考试类型" clearable>
              <el-option
                v-for="type in examTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.status" placeholder="考试状态" clearable>
              <el-option label="已完成" value="completed" />
              <el-option label="进行中" value="ongoing" />
              <el-option label="未开始" value="pending" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
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
        <el-button type="success" @click="exportAllResults" :disabled="!hasResults">
          <el-icon><Download /></el-icon>
          批量导出
        </el-button>
        <el-button type="warning" @click="showStatistics" :disabled="!hasResults">
          <el-icon><TrendCharts /></el-icon>
          统计分析
        </el-button>
        <el-button type="danger" @click="batchReScore" :disabled="!hasSelection">
          <el-icon><RefreshRight /></el-icon>
          批量重评
        </el-button>
      </div>
    </div>

    <!-- 考试结果列表 -->
    <div class="result-list">
      <el-table
        v-loading="loading"
        :data="filteredResults"
        @selection-change="handleSelectionChange"
        stripe
        class="result-table"
        empty-text="暂无考试结果"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="考试信息" min-width="200">
          <template #default="{ row }">
            <div class="exam-info">
              <div class="exam-name">{{ row.examName }}</div>
              <div class="exam-meta">
                <el-tag size="small" :type="getExamTypeTagType(row.examType)">
                  {{ formatExamType(row.examType) }}
                </el-tag>
                <span class="exam-date">{{ formatDate(row.examDate) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="考生信息" min-width="150">
          <template #default="{ row }">
            <div class="student-info">
              <div class="student-name">{{ row.studentName }}</div>
              <div class="student-meta">
                <span class="student-id">{{ row.studentId }}</span>
                <el-tag v-if="row.studentLevel" size="small" type="info">
                  {{ row.studentLevel }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="成绩" width="120" align="center">
          <template #default="{ row }">
            <div class="score-info">
              <div class="score-value" :class="getScoreClass(row.score, row.passingScore)">
                {{ row.score }}分
              </div>
              <div class="score-status">
                <el-tag 
                  size="small"
                  :type="row.score >= row.passingScore ? 'success' : 'danger'"
                >
                  {{ row.score >= row.passingScore ? '通过' : '未通过' }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="用时" width="100" align="center">
          <template #default="{ row }">
            <div class="time-info">
              <div class="time-used">{{ formatDuration(row.timeUsed) }}</div>
              <div class="time-limit">/ {{ formatDuration(row.timeLimit) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="答题详情" width="120" align="center">
          <template #default="{ row }">
            <div class="answer-stats">
              <div class="correct-count">
                正确: {{ row.correctCount }}/{{ row.totalQuestions }}
              </div>
              <div class="accuracy-rate">
                准确率: {{ calculateAccuracy(row.correctCount, row.totalQuestions) }}%
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              size="small"
              :type="getStatusTagType(row.status)"
            >
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewDetail(row)"
                link
              >
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="downloadCertificate(row)"
                :disabled="row.score < row.passingScore"
                link
              >
                <el-icon><Medal /></el-icon>
                证书
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                @click="reScore(row)"
                link
              >
                <el-icon><RefreshRight /></el-icon>
                重评
              </el-button>
              <el-button 
                type="info" 
                size="small" 
                @click="exportSingle(row)"
                link
              >
                <el-icon><Download /></el-icon>
                导出
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

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
    </div>

    <!-- 结果详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="考试结果详情"
      width="1000px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedResult" class="result-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">考试名称:</span>
                <span class="value">{{ selectedResult.examName }}</span>
              </div>
              <div class="info-item">
                <span class="label">考生姓名:</span>
                <span class="value">{{ selectedResult.studentName }}</span>
              </div>
              <div class="info-item">
                <span class="label">学员ID:</span>
                <span class="value">{{ selectedResult.studentId }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">考试类型:</span>
                <span class="value">{{ formatExamType(selectedResult.examType) }}</span>
              </div>
              <div class="info-item">
                <span class="label">考试时间:</span>
                <span class="value">{{ formatDateTime(selectedResult.examDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">提交时间:</span>
                <span class="value">{{ formatDateTime(selectedResult.submitTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 成绩信息 -->
        <div class="detail-section">
          <h4>成绩信息</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="score-card">
                <div class="score-title">总分</div>
                <div class="score-value large" :class="getScoreClass(selectedResult.score, selectedResult.passingScore)">
                  {{ selectedResult.score }}
                </div>
                <div class="score-subtitle">满分 {{ selectedResult.maxScore }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="score-card">
                <div class="score-title">正确率</div>
                <div class="score-value large">
                  {{ calculateAccuracy(selectedResult.correctCount, selectedResult.totalQuestions) }}%
                </div>
                <div class="score-subtitle">{{ selectedResult.correctCount }}/{{ selectedResult.totalQuestions }} 题</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="score-card">
                <div class="score-title">用时</div>
                <div class="score-value large">
                  {{ formatDuration(selectedResult.timeUsed) }}
                </div>
                <div class="score-subtitle">限时 {{ formatDuration(selectedResult.timeLimit) }}</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 答题详情 -->
        <div class="detail-section">
          <h4>答题详情</h4>
          <el-table
            :data="selectedResult.answerDetails || []"
            stripe
            class="answer-detail-table"
          >
            <el-table-column prop="questionNo" label="题号" width="80" align="center" />
            <el-table-column prop="questionType" label="题型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ formatQuestionType(row.questionType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="question" label="题目" min-width="200" />
            <el-table-column prop="studentAnswer" label="学生答案" width="120" />
            <el-table-column prop="correctAnswer" label="正确答案" width="120" />
            <el-table-column prop="score" label="得分" width="80" align="center">
              <template #default="{ row }">
                <span :class="{ 'correct': row.isCorrect, 'incorrect': !row.isCorrect }">
                  {{ row.score }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="isCorrect" label="结果" width="80" align="center">
              <template #default="{ row }">
                <el-tag 
                  size="small"
                  :type="row.isCorrect ? 'success' : 'danger'"
                >
                  {{ row.isCorrect ? '正确' : '错误' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 统计分析对话框 -->
    <el-dialog
      v-model="statisticsDialogVisible"
      title="统计分析"
      width="1200px"
      :close-on-click-modal="false"
    >
      <div class="statistics-content">
        <!-- 总体统计 -->
        <div class="stats-overview">
          <el-row :gutter="20">
            <el-col :span="6" v-for="stat in overallStats" :key="stat.key">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-icon">
                    <el-icon :size="24" :color="stat.color">
                      <component :is="stat.icon" />
                    </el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stat.value }}</div>
                    <div class="stat-label">{{ stat.label }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 图表区域 -->
        <div class="charts-section">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>成绩分布</span>
                </template>
                <div class="chart-placeholder">
                  <el-icon size="40" color="#dcdfe6"><TrendCharts /></el-icon>
                  <p>成绩分布图表</p>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>通过率统计</span>
                </template>
                <div class="chart-placeholder">
                  <el-icon size="40" color="#dcdfe6"><PieChart /></el-icon>
                  <p>通过率饼图</p>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { examApi } from '@/api/exam'
import {
  Search,
  Refresh,
  Download,
  TrendCharts,
  RefreshRight,
  View,
  Medal,
  PieChart
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  results: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['view-detail', 're-score', 'export', 'view-statistics'])

// 响应式数据
const detailDialogVisible = ref(false)
const statisticsDialogVisible = ref(false)
const selectedResult = ref(null)
const selectedResults = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  examType: '',
  status: '',
  dateRange: []
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 计算属性
const examTypes = computed(() => examApi.getPaperTypeOptions())

const filteredResults = computed(() => {
  let filtered = props.results

  // 关键词搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filtered = filtered.filter(result => 
      result.examName.toLowerCase().includes(keyword) ||
      result.studentName.toLowerCase().includes(keyword) ||
      result.studentId.toLowerCase().includes(keyword)
    )
  }

  // 考试类型筛选
  if (searchForm.examType) {
    filtered = filtered.filter(result => result.examType === searchForm.examType)
  }

  // 状态筛选
  if (searchForm.status) {
    filtered = filtered.filter(result => result.status === searchForm.status)
  }

  // 日期范围筛选
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const [startDate, endDate] = searchForm.dateRange
    filtered = filtered.filter(result => {
      const examDate = new Date(result.examDate)
      return examDate >= new Date(startDate) && examDate <= new Date(endDate)
    })
  }

  // 更新分页总数
  pagination.total = filtered.length

  // 分页
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filtered.slice(start, end)
})

const hasResults = computed(() => props.results.length > 0)
const hasSelection = computed(() => selectedResults.value.length > 0)

// 总体统计数据
const overallStats = computed(() => {
  const results = props.results
  const totalCount = results.length
  const passedCount = results.filter(r => r.score >= r.passingScore).length
  const avgScore = totalCount > 0 ? Math.round(results.reduce((sum, r) => sum + r.score, 0) / totalCount) : 0
  const passRate = totalCount > 0 ? Math.round((passedCount / totalCount) * 100) : 0

  return [
    { key: 'total', label: '总考试数', value: totalCount, icon: 'Document', color: '#409EFF' },
    { key: 'passed', label: '通过人数', value: passedCount, icon: 'Check', color: '#67C23A' },
    { key: 'avgScore', label: '平均分', value: avgScore, icon: 'TrendCharts', color: '#E6A23C' },
    { key: 'passRate', label: '通过率', value: `${passRate}%`, icon: 'PieChart', color: '#F56C6C' }
  ]
})

// 方法
const handleSearch = () => {
  pagination.page = 1
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    examType: '',
    status: '',
    dateRange: []
  })
  pagination.page = 1
}

const handleSelectionChange = (selection) => {
  selectedResults.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
}

const handleCurrentChange = (page) => {
  pagination.page = page
}

const viewDetail = (result) => {
  selectedResult.value = result
  detailDialogVisible.value = true
  emit('view-detail', result)
}

const reScore = async (result) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新评分考试"${result.examName}"的结果吗？`,
      '确认重新评分',
      { type: 'warning' }
    )
    
    emit('re-score', result)
  } catch (error) {
    // 用户取消
  }
}

const batchReScore = async () => {
  if (selectedResults.value.length === 0) {
    ElMessage.warning('请选择要重新评分的结果')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要重新评分选中的 ${selectedResults.value.length} 个结果吗？`,
      '确认批量重新评分',
      { type: 'warning' }
    )
    
    for (const result of selectedResults.value) {
      emit('re-score', result)
    }
    ElMessage.success('批量重新评分完成')
  } catch (error) {
    // 用户取消
  }
}

const exportSingle = (result) => {
  emit('export', result)
}

const exportAllResults = () => {
  if (props.results.length === 0) {
    ElMessage.warning('没有可导出的结果')
    return
  }
  emit('export', { type: 'all', results: props.results })
}

const downloadCertificate = async (result) => {
  if (result.score < result.passingScore) {
    ElMessage.warning('该考生未通过考试，无法下载证书')
    return
  }
  
  try {
    const blob = await examApi.downloadCertificate(result.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${result.studentName}_${result.examName}_证书.pdf`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('证书下载成功')
  } catch (error) {
    ElMessage.error('证书下载失败')
  }
}

const showStatistics = () => {
  statisticsDialogVisible.value = true
  emit('view-statistics')
}

// 工具方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatDuration = (seconds) => {
  if (!seconds) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = seconds % 60
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟${remainingSeconds}秒`
  } else {
    return `${remainingSeconds}秒`
  }
}

const formatExamType = (type) => {
  return examApi.formatPaperType(type)
}

const formatStatus = (status) => {
  const statusMap = {
    'completed': '已完成',
    'ongoing': '进行中',
    'pending': '未开始',
    'cancelled': '已取消'
  }
  return statusMap[status] || status
}

const formatQuestionType = (type) => {
  const typeMap = {
    'mcq': '选择题',
    'fill_blank': '填空题',
    'essay': '问答题',
    'listening': '听力题',
    'speaking': '口语题'
  }
  return typeMap[type] || type
}

const getExamTypeTagType = (type) => {
  const typeMap = {
    'listening': 'primary',
    'speaking': 'success',
    'reading': 'warning',
    'writing': 'danger',
    'comprehensive': 'info'
  }
  return typeMap[type] || 'info'
}

const getStatusTagType = (status) => {
  const statusMap = {
    'completed': 'success',
    'ongoing': 'warning',
    'pending': 'info',
    'cancelled': 'danger'
  }
  return statusMap[status] || 'info'
}

const getScoreClass = (score, passingScore) => {
  if (score >= passingScore) {
    return 'score-pass'
  } else {
    return 'score-fail'
  }
}

const calculateAccuracy = (correctCount, totalQuestions) => {
  if (totalQuestions === 0) return 0
  return Math.round((correctCount / totalQuestions) * 100)
}

// 监听结果列表变化，重置分页
watch(() => props.results, () => {
  pagination.page = 1
}, { deep: true })
</script>

<style scoped>
.exam-result-panel {
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

.result-table {
  border-radius: 8px;
  overflow: hidden;
}

.exam-info {
  padding: 8px 0;
}

.exam-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.exam-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.student-info {
  padding: 8px 0;
}

.student-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.student-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.score-info {
  text-align: center;
}

.score-value {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.score-value.score-pass {
  color: #67C23A;
}

.score-value.score-fail {
  color: #F56C6C;
}

.time-info {
  text-align: center;
  font-size: 12px;
}

.time-used {
  font-weight: 600;
  color: #303133;
}

.time-limit {
  color: #909399;
}

.answer-stats {
  text-align: center;
  font-size: 12px;
}

.correct-count {
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.accuracy-rate {
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

/* 详情对话框样式 */
.result-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  min-width: 80px;
  color: #909399;
  font-weight: 500;
}

.info-item .value {
  color: #303133;
  flex: 1;
}

.score-card {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.score-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.score-value.large {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 4px;
}

.score-subtitle {
  font-size: 12px;
  color: #909399;
}

.answer-detail-table {
  margin-top: 16px;
}

.correct {
  color: #67C23A;
  font-weight: 600;
}

.incorrect {
  color: #F56C6C;
  font-weight: 600;
}

/* 统计分析样式 */
.statistics-content {
  max-height: 70vh;
  overflow-y: auto;
}

.stats-overview {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  background: rgba(64, 158, 255, 0.1);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.charts-section {
  margin-top: 24px;
}

.chart-placeholder {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.chart-placeholder p {
  margin-top: 8px;
  font-size: 14px;
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
  
  .stats-overview .el-col {
    margin-bottom: 16px;
  }
  
  .charts-section .el-col {
    margin-bottom: 16px;
  }
}
</style>
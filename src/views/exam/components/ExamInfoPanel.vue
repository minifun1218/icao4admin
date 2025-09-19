<template>
  <div class="exam-info-panel">
    <!-- 搜索和操作栏 -->
    <div class="panel-header">
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索考试名称"
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
            <el-select v-model="searchForm.status" placeholder="考试状态" clearable>
              <el-option
                v-for="status in examStatuses"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
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
        <el-button type="success" @click="$emit('add')">
          <el-icon><Plus /></el-icon>
          创建考试
        </el-button>
      </div>
    </div>

    <!-- 考试列表 -->
    <el-table
      v-loading="loading"
      :data="filteredExams"
      stripe
      class="exam-table"
      empty-text="暂无考试数据"
    >
      <el-table-column prop="id" label="ID" width="80" />
      
      <el-table-column label="考试信息" min-width="250">
        <template #default="{ row }">
          <div class="exam-info">
            <div class="exam-name">{{ row.name }}</div>
            <div class="exam-description">{{ row.description || '暂无描述' }}</div>
            <div class="exam-meta">
              <el-tag size="small" :type="getExamTypeTagType(row.type)">
                {{ formatExamType(row.type) }}
              </el-tag>
              <span class="exam-duration">{{ row.duration }}分钟</span>
              <span class="passing-score">及格: {{ row.passingScore }}分</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag 
            :type="getStatusTagType(row.status)"
            size="small"
            effect="dark"
          >
            {{ formatExamStatus(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="时间安排" width="200">
        <template #default="{ row }">
          <div class="time-info">
            <div class="time-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(row.startTime) }}</span>
            </div>
            <div class="time-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(row.endTime) }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="参与情况" width="150" align="center">
        <template #default="{ row }">
          <div class="participation-info">
            <div class="participant-count">
              <span class="count">{{ row.participantCount || 0 }}</span>
              <span class="total">/{{ row.maxParticipants }}</span>
            </div>
            <div class="completion-rate">
              完成率: {{ calculateCompletionRate(row) }}%
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="small" 
              @click="$emit('edit', row)"
              :disabled="row.status === 'ongoing'"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            
            <el-button 
              v-if="row.status === 'draft' || row.status === 'published'"
              type="success" 
              size="small" 
              @click="$emit('start', row)"
            >
              <el-icon><VideoPlay /></el-icon>
              开始
            </el-button>
            
            <el-button 
              v-if="row.status === 'ongoing'"
              type="warning" 
              size="small" 
              @click="$emit('end', row)"
            >
              <el-icon><VideoPause /></el-icon>
              结束
            </el-button>
            
            <el-button 
              type="info" 
              size="small" 
              @click="$emit('view-participants', row)"
            >
              <el-icon><User /></el-icon>
              参与者
            </el-button>
            
            <el-button 
              type="danger" 
              size="small" 
              @click="$emit('delete', row)"
              :disabled="row.status === 'ongoing'"
            >
              <el-icon><Delete /></el-icon>
              删除
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
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { examApi } from '@/api/exam'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  VideoPlay,
  VideoPause,
  User,
  Delete,
  Clock
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  exams: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['add', 'edit', 'delete', 'start', 'end', 'view-participants'])

// 搜索表单
const searchForm = reactive({
  keyword: '',
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
const examStatuses = computed(() => examApi.getExamStatusOptions())

const filteredExams = computed(() => {
  let filtered = props.exams

  // 关键词搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filtered = filtered.filter(exam => 
      exam.name.toLowerCase().includes(keyword) ||
      (exam.description && exam.description.toLowerCase().includes(keyword))
    )
  }

  // 状态筛选
  if (searchForm.status) {
    filtered = filtered.filter(exam => exam.status === searchForm.status)
  }

  // 日期范围筛选
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const startDate = new Date(searchForm.dateRange[0])
    const endDate = new Date(searchForm.dateRange[1])
    filtered = filtered.filter(exam => {
      const examDate = new Date(exam.startTime)
      return examDate >= startDate && examDate <= endDate
    })
  }

  // 更新分页总数
  pagination.total = filtered.length

  // 分页
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filtered.slice(start, end)
})

// 方法
const getStatusTagType = (status) => {
  const tagTypes = {
    draft: 'info',
    published: 'success',
    ongoing: 'warning',
    ended: 'primary',
    cancelled: 'danger'
  }
  return tagTypes[status] || 'info'
}

const getExamTypeTagType = (type) => {
  const tagTypes = {
    practice: 'success',
    mock: 'warning',
    formal: 'danger',
    placement: 'primary'
  }
  return tagTypes[type] || 'info'
}

const formatExamStatus = (status) => {
  return examApi.formatExamStatus(status)
}

const formatExamType = (type) => {
  return examApi.formatPaperType(type)
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const calculateCompletionRate = (exam) => {
  if (!exam.participantCount || exam.participantCount === 0) return 0
  const completedCount = exam.completedCount || 0
  return Math.round((completedCount / exam.participantCount) * 100)
}

const handleSearch = () => {
  pagination.page = 1
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    dateRange: []
  })
  pagination.page = 1
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
}

const handleCurrentChange = (page) => {
  pagination.page = page
}
</script>

<style scoped>
.exam-info-panel {
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

.exam-table {
  border-radius: 8px;
  overflow: hidden;
}

.exam-info {
  padding: 8px 0;
}

.exam-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.exam-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.exam-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.exam-duration,
.passing-score {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.participation-info {
  text-align: center;
}

.participant-count {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.participant-count .count {
  color: #409EFF;
}

.participant-count .total {
  color: #909399;
}

.completion-rate {
  font-size: 12px;
  color: #606266;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
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
  
  .exam-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .time-info {
    gap: 2px;
  }
}
</style>

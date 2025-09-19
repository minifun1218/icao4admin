<template>
  <div class="exam-paper-panel">
    <!-- 搜索和操作栏 -->
    <div class="panel-header">
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索试卷标题"
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
            <el-select v-model="searchForm.type" placeholder="试卷类型" clearable>
              <el-option
                v-for="type in paperTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.status" placeholder="发布状态" clearable>
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
            </el-select>
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
          创建试卷
        </el-button>
      </div>
    </div>

    <!-- 试卷卡片列表 -->
    <div class="papers-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="paper in filteredPapers" :key="paper.id">
          <el-card class="paper-card" shadow="hover">
            <template #header>
              <div class="paper-header">
                <div class="paper-title">
                  <el-icon :color="getPaperTypeColor(paper.type)">
                    <component :is="getPaperTypeIcon(paper.type)" />
                  </el-icon>
                  <span>{{ paper.title }}</span>
                </div>
                <el-tag 
                  :type="paper.status === 'published' ? 'success' : 'info'"
                  size="small"
                >
                  {{ paper.status === 'published' ? '已发布' : '草稿' }}
                </el-tag>
              </div>
            </template>
            
            <div class="paper-content">
              <div class="paper-description">
                {{ paper.description || '暂无描述' }}
              </div>
              
              <div class="paper-stats">
                <div class="stat-item">
                  <span class="stat-label">题目数量:</span>
                  <span class="stat-value">{{ paper.questionCount || 0 }}题</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">总分:</span>
                  <span class="stat-value">{{ paper.totalScore || 0 }}分</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">建议时长:</span>
                  <span class="stat-value">{{ paper.suggestedDuration || 0 }}分钟</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">使用次数:</span>
                  <span class="stat-value">{{ paper.usageCount || 0 }}次</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">创建时间:</span>
                  <span class="stat-value">{{ formatDate(paper.createdAt) }}</span>
                </div>
              </div>
            </div>
            
            <template #footer>
              <div class="paper-actions">
                <el-button 
                  type="info" 
                  size="small" 
                  @click="$emit('preview', paper)"
                >
                  <el-icon><View /></el-icon>
                  预览
                </el-button>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="$emit('edit', paper)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button 
                  type="warning" 
                  size="small" 
                  @click="$emit('copy', paper)"
                >
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
                <el-button 
                  v-if="paper.status === 'draft'"
                  type="success" 
                  size="small" 
                  @click="$emit('publish', paper)"
                >
                  <el-icon><Upload /></el-icon>
                  发布
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="$emit('delete', paper)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 空状态 -->
      <div v-if="filteredPapers.length === 0" class="empty-state">
        <el-empty description="暂无试卷数据">
          <el-button type="primary" @click="$emit('add')">创建第一个试卷</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="filteredPapers.length > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[6, 12, 18, 24]"
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
  View,
  Edit,
  CopyDocument,
  Upload,
  Delete
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  papers: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['add', 'edit', 'delete', 'copy', 'publish', 'preview'])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

// 计算属性
const paperTypes = computed(() => examApi.getPaperTypeOptions())

const filteredPapers = computed(() => {
  let filtered = props.papers

  // 关键词搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filtered = filtered.filter(paper => 
      paper.title.toLowerCase().includes(keyword) ||
      (paper.description && paper.description.toLowerCase().includes(keyword))
    )
  }

  // 类型筛选
  if (searchForm.type) {
    filtered = filtered.filter(paper => paper.type === searchForm.type)
  }

  // 状态筛选
  if (searchForm.status) {
    filtered = filtered.filter(paper => paper.status === searchForm.status)
  }

  // 更新分页总数
  pagination.total = filtered.length

  // 分页
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filtered.slice(start, end)
})

// 方法
const getPaperTypeColor = (type) => {
  const colors = {
    practice: '#67C23A',
    mock: '#E6A23C',
    formal: '#F56C6C',
    placement: '#409EFF'
  }
  return colors[type] || '#909399'
}

const getPaperTypeIcon = (type) => {
  const icons = {
    practice: 'Edit',
    mock: 'Clock',
    formal: 'Medal',
    placement: 'TrendCharts'
  }
  return icons[type] || 'Document'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const handleSearch = () => {
  pagination.page = 1
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    type: '',
    status: ''
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
.exam-paper-panel {
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

.papers-grid {
  margin-bottom: 20px;
}

.paper-card {
  margin-bottom: 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.paper-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.paper-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.paper-content {
  padding: 16px 0;
}

.paper-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 48px;
}

.paper-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  padding: 4px 0;
}

.stat-label {
  color: #909399;
}

.stat-value {
  color: #303133;
  font-weight: 500;
}

.paper-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.pagination-container {
  text-align: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .action-section {
    flex-direction: column;
  }
  
  .papers-grid .el-col {
    margin-bottom: 16px;
  }
  
  .paper-actions {
    flex-direction: column;
  }
  
  .paper-stats {
    grid-template-columns: 1fr;
  }
}
</style>

<template>
  <div class="banner-group-panel">
    <!-- 搜索和操作栏 -->
    <div class="panel-header">
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索轮播图组名称"
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
            <el-select v-model="searchForm.type" placeholder="类型" clearable>
              <el-option
                v-for="type in bannerTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.status" placeholder="状态" clearable>
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
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
          创建轮播图组
        </el-button>
      </div>
    </div>

    <!-- 轮播图组卡片列表 -->
    <div class="groups-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="group in filteredGroups" :key="group.id">
          <el-card class="group-card" shadow="hover">
            <template #header>
              <div class="group-header">
                <div class="group-title">
                  <el-icon :color="getTypeColor(group.type)">
                    <component :is="getTypeIcon(group.type)" />
                  </el-icon>
                  <span>{{ group.name }}</span>
                </div>
                <div class="group-status">
                  <el-tag 
                    :type="group.isActive ? 'success' : 'danger'"
                    size="small"
                  >
                    {{ group.isActive ? '启用' : '禁用' }}
                  </el-tag>
                  <el-checkbox 
                    v-model="selectedGroups"
                    :label="group.id"
                    @change="handleSelectionChange"
                  />
                </div>
              </div>
            </template>
            
            <div class="group-content">
              <div class="group-type">
                <el-tag :type="getTypeTagType(group.type)" size="small">
                  {{ formatType(group.type) }}
                </el-tag>
              </div>
              
              <div class="group-description">
                {{ group.description || '暂无描述' }}
              </div>
              
              <div class="group-stats">
                <div class="stat-item">
                  <span class="stat-label">位置:</span>
                  <span class="stat-value">{{ formatPosition(group.displayPosition) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">排序:</span>
                  <span class="stat-value">{{ group.displayOrder }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">项目数:</span>
                  <span class="stat-value">{{ group.itemCount || 0 }}个</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">创建时间:</span>
                  <span class="stat-value">{{ formatDate(group.createdAt) }}</span>
                </div>
              </div>

              <!-- 自动播放设置 -->
              <div v-if="group.autoPlay" class="auto-play-info">
                <el-icon color="#67C23A"><VideoPlay /></el-icon>
                <span>自动播放 {{ group.playInterval / 1000 }}秒</span>
              </div>
            </div>
            
            <template #footer>
              <div class="group-actions">
                <el-button 
                  type="info" 
                  size="small" 
                  @click="$emit('preview', group)"
                >
                  <el-icon><View /></el-icon>
                  预览
                </el-button>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="$emit('edit', group)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button 
                  v-if="!group.isPublished"
                  type="success" 
                  size="small" 
                  @click="$emit('publish', group)"
                >
                  <el-icon><Upload /></el-icon>
                  发布
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="$emit('delete', group)"
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
      <div v-if="filteredGroups.length === 0" class="empty-state">
        <el-empty description="暂无轮播图组数据">
          <el-button type="primary" @click="$emit('add')">创建第一个轮播图组</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="filteredGroups.length > 0">
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
import { bannerApi } from '@/api/banner'
import {
  Search,
  Refresh,
  Plus,
  View,
  Edit,
  Upload,
  Delete,
  VideoPlay
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  groups: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['add', 'edit', 'delete', 'preview', 'publish', 'selection-change'])

// 响应式数据
const selectedGroups = ref([])

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
const bannerTypes = computed(() => bannerApi.getBannerTypes())

const filteredGroups = computed(() => {
  let filtered = props.groups

  // 关键词搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filtered = filtered.filter(group => 
      group.name.toLowerCase().includes(keyword) ||
      (group.description && group.description.toLowerCase().includes(keyword))
    )
  }

  // 类型筛选
  if (searchForm.type) {
    filtered = filtered.filter(group => group.type === searchForm.type)
  }

  // 状态筛选
  if (searchForm.status !== '') {
    filtered = filtered.filter(group => group.isActive === searchForm.status)
  }

  // 更新分页总数
  pagination.total = filtered.length

  // 分页
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filtered.slice(start, end)
})

// 方法
const getTypeColor = (type) => {
  const colors = {
    carousel: '#409EFF',
    banner: '#67C23A',
    popup: '#E6A23C',
    notice: '#F56C6C'
  }
  return colors[type] || '#909399'
}

const getTypeIcon = (type) => {
  const icons = {
    carousel: 'Picture',
    banner: 'Postcard',
    popup: 'MessageBox',
    notice: 'Bell'
  }
  return icons[type] || 'Picture'
}

const getTypeTagType = (type) => {
  const tagTypes = {
    carousel: 'primary',
    banner: 'success',
    popup: 'warning',
    notice: 'danger'
  }
  return tagTypes[type] || 'info'
}

const formatType = (type) => {
  return bannerApi.formatBannerType(type)
}

const formatPosition = (position) => {
  return bannerApi.formatDisplayPosition(position)
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

const handleSelectionChange = () => {
  emit('selection-change', selectedGroups.value)
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
.banner-group-panel {
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

.groups-grid {
  margin-bottom: 20px;
}

.group-card {
  margin-bottom: 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.group-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.group-status {
  display: flex;
  align-items: center;
  gap: 12px;
}

.group-content {
  padding: 16px 0;
}

.group-type {
  margin-bottom: 12px;
}

.group-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 48px;
}

.group-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 12px;
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

.auto-play-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #67C23A;
  font-size: 14px;
  padding: 8px 12px;
  background: rgba(103, 194, 58, 0.1);
  border-radius: 6px;
}

.group-actions {
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
  
  .groups-grid .el-col {
    margin-bottom: 16px;
  }
  
  .group-actions {
    flex-direction: column;
  }
  
  .group-stats {
    grid-template-columns: 1fr;
  }
  
  .group-status {
    flex-direction: column;
    gap: 8px;
  }
}
</style>

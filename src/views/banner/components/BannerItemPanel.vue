<template>
  <div class="banner-item-panel">
    <!-- 搜索和操作栏 -->
    <div class="panel-header">
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索标题"
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
            <el-select v-model="searchForm.groupId" placeholder="所属组" clearable>
              <el-option
                v-for="group in groups"
                :key="group.id"
                :label="group.name"
                :value="group.id"
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
          添加轮播图项目
        </el-button>
      </div>
    </div>

    <!-- 轮播图项目列表 -->
    <el-table
      v-loading="loading"
      :data="filteredItems"
      @selection-change="handleSelectionChange"
      stripe
      class="item-table"
      empty-text="暂无轮播图项目"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      
      <el-table-column label="预览图" width="120">
        <template #default="{ row }">
          <div class="image-preview">
            <el-image
              :src="generateThumbnail(row.imageUrl)"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              class="preview-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="项目信息" min-width="250">
        <template #default="{ row }">
          <div class="item-info">
            <div class="item-title">{{ row.title }}</div>
            <div class="item-description">{{ row.description || '暂无描述' }}</div>
            <div class="item-meta">
              <el-tag 
                v-if="row.group"
                size="small" 
                type="info"
              >
                {{ row.group.name }}
              </el-tag>
              <el-tag 
                v-if="row.linkType !== 'none'"
                size="small" 
                type="warning"
              >
                {{ formatLinkType(row.linkType) }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="链接地址" width="200">
        <template #default="{ row }">
          <div v-if="row.linkType !== 'none'" class="link-info">
            <el-link 
              :href="row.linkUrl" 
              :target="row.openInNewWindow ? '_blank' : '_self'"
              type="primary"
              class="link-url"
            >
              {{ truncateUrl(row.linkUrl) }}
            </el-link>
            <div class="link-meta">
              <el-icon v-if="row.openInNewWindow"><TopRight /></el-icon>
              <span>{{ formatLinkType(row.linkType) }}</span>
            </div>
          </div>
          <span v-else class="no-link">无链接</span>
        </template>
      </el-table-column>

      <el-table-column label="排序" width="80" align="center">
        <template #default="{ row }">
          <span class="order-text">{{ row.displayOrder }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.isActive"
            @change="handleStatusChange(row)"
            size="small"
          />
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="small" 
              @click="$emit('edit', row)"
              link
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="previewItem(row)"
              link
            >
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="$emit('delete', row)"
              link
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

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="轮播图项目预览"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="previewItem" class="preview-content">
        <div class="preview-image-container">
          <el-image
            :src="previewItemData.imageUrl"
            fit="contain"
            class="preview-full-image"
          >
            <template #error>
              <div class="image-error-large">
                <el-icon size="60"><Picture /></el-icon>
                <p>图片加载失败</p>
              </div>
            </template>
          </el-image>
        </div>
        
        <div class="preview-info">
          <h3>{{ previewItemData.title }}</h3>
          <p class="preview-description">{{ previewItemData.description }}</p>
          
          <div class="preview-details">
            <div class="detail-item">
              <span class="label">所属组:</span>
              <span class="value">{{ previewItemData.group?.name || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">链接类型:</span>
              <span class="value">{{ formatLinkType(previewItemData.linkType) }}</span>
            </div>
            <div v-if="previewItemData.linkUrl" class="detail-item">
              <span class="label">链接地址:</span>
              <el-link :href="previewItemData.linkUrl" type="primary" target="_blank">
                {{ previewItemData.linkUrl }}
              </el-link>
            </div>
            <div class="detail-item">
              <span class="label">排序:</span>
              <span class="value">{{ previewItemData.displayOrder }}</span>
            </div>
            <div class="detail-item">
              <span class="label">状态:</span>
              <el-tag :type="previewItemData.isActive ? 'success' : 'danger'" size="small">
                {{ previewItemData.isActive ? '启用' : '禁用' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { bannerApi } from '@/api/banner'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  View,
  Delete,
  Picture,
  TopRight
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
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
const emit = defineEmits(['add', 'edit', 'delete', 'upload', 'selection-change'])

// 响应式数据
const previewDialogVisible = ref(false)
const previewItemData = ref(null)
const selectedItems = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  groupId: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 计算属性
const filteredItems = computed(() => {
  let filtered = props.items

  // 关键词搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filtered = filtered.filter(item => 
      item.title.toLowerCase().includes(keyword) ||
      (item.description && item.description.toLowerCase().includes(keyword))
    )
  }

  // 组筛选
  if (searchForm.groupId) {
    filtered = filtered.filter(item => item.groupId === searchForm.groupId)
  }

  // 状态筛选
  if (searchForm.status !== '') {
    filtered = filtered.filter(item => item.isActive === searchForm.status)
  }

  // 更新分页总数
  pagination.total = filtered.length

  // 分页
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filtered.slice(start, end)
})

// 方法
const generateThumbnail = (imageUrl) => {
  return bannerApi.generateThumbnailUrl(imageUrl, 100, 75)
}

const formatLinkType = (type) => {
  return bannerApi.formatLinkType(type)
}

const truncateUrl = (url) => {
  if (!url) return ''
  return url.length > 30 ? url.substring(0, 30) + '...' : url
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const handleSearch = () => {
  pagination.page = 1
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    groupId: '',
    status: ''
  })
  pagination.page = 1
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection
  emit('selection-change', selection)
}

const handleStatusChange = async (item) => {
  try {
    // 这里应该调用API更新状态
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 恢复原状态
    item.isActive = !item.isActive
    ElMessage.error('状态更新失败')
  }
}

const previewItem = (item) => {
  previewItemData.value = item
  previewDialogVisible.value = true
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
.banner-item-panel {
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

.item-table {
  border-radius: 8px;
  overflow: hidden;
}

.image-preview {
  width: 80px;
  height: 60px;
}

.preview-image {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  cursor: pointer;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  border-radius: 4px;
}

.item-info {
  padding: 8px 0;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.item-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.link-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.link-url {
  font-size: 14px;
}

.link-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.no-link {
  color: #909399;
  font-size: 14px;
}

.order-text {
  font-weight: 600;
  color: #E6A23C;
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

/* 预览对话框样式 */
.preview-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-image-container {
  text-align: center;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.preview-full-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
}

.image-error-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.image-error-large p {
  margin-top: 10px;
  font-size: 14px;
}

.preview-info h3 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 20px;
}

.preview-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 20px;
}

.preview-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-item .label {
  min-width: 80px;
  color: #909399;
  font-weight: 500;
}

.detail-item .value {
  color: #303133;
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
  
  .item-meta {
    flex-direction: column;
    gap: 4px;
  }
  
  .preview-content {
    gap: 15px;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .detail-item .label {
    min-width: auto;
  }
}
</style>

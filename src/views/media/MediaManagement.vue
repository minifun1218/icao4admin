<template>
  <div class="media-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>媒体资源管理</span>
          <div class="header-actions">
            <el-upload
              ref="uploadRef"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-progress="handleUploadProgress"
              multiple
              accept="image/*,audio/*,video/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx"
              style="display: inline-block; margin-right: 10px;"
            >
              <el-button type="primary" :loading="uploading">
                <el-icon><Upload /></el-icon>
                {{ uploading ? '上传中...' : '上传文件' }}
              </el-button>
            </el-upload>
            <el-button type="danger" @click="batchDelete" :disabled="!hasSelection">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索和过滤 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="文件名">
            <el-input
              v-model="searchForm.fileName"
              placeholder="搜索文件名"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="文件类型">
            <el-select v-model="searchForm.category" placeholder="选择类型" clearable>
              <el-option
                v-for="category in mediaCategories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="文件大小">
            <el-select v-model="searchForm.sizeRange" placeholder="选择大小范围" clearable>
              <el-option label="小于1MB" value="0-1" />
              <el-option label="1MB-10MB" value="1-10" />
              <el-option label="10MB-50MB" value="10-50" />
              <el-option label="大于50MB" value="50-" />
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
      
      <!-- 视图切换 -->
      <div class="view-controls">
        <el-radio-group v-model="viewMode" @change="handleViewModeChange">
          <el-radio-button label="grid">
            <el-icon><Grid /></el-icon>
            网格视图
          </el-radio-button>
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
            列表视图
          </el-radio-button>
        </el-radio-group>
        
        <div class="file-stats">
          共 {{ pagination.total }} 个文件，总大小 {{ totalSize }}
        </div>
      </div>
      
      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="grid-view">
        <div class="media-grid">
          <div
            v-for="file in mediaList"
            :key="file.id"
            class="media-item"
            :class="{ selected: selectedFiles.includes(file.id) }"
            @click="toggleSelection(file.id)"
          >
            <div class="media-preview">
              <el-image
                v-if="file.category === 'IMAGE'"
                :src="getMediaUrl(file.id)"
                :preview-src-list="[getMediaUrl(file.id)]"
                fit="cover"
                class="preview-image"
                lazy
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              
              <div v-else class="file-icon">
                <el-icon size="40" :color="getFileIconColor(file.category)">
                  <component :is="getFileIcon(file.category)" />
                </el-icon>
              </div>
            </div>
            
            <div class="media-info">
              <div class="file-name" :title="file.fileName">{{ file.fileName }}</div>
              <div class="file-meta">
                <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
                <span class="file-type">{{ getCategoryName(file.category) }}</span>
              </div>
              <div class="file-actions">
                <el-button type="text" size="small" @click.stop="previewFile(file)">
                  <el-icon><View /></el-icon>
                </el-button>
                <el-button type="text" size="small" @click.stop="downloadFile(file)">
                  <el-icon><Download /></el-icon>
                </el-button>
                <el-button type="text" size="small" @click.stop="editFile(file)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button type="text" size="small" @click.stop="deleteFile(file)" class="danger">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 列表视图 -->
      <div v-else class="list-view">
        <el-table
          v-loading="loading"
          :data="mediaList"
          @selection-change="handleSelectionChange"
          stripe
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="预览" width="80">
            <template #default="{ row }">
              <el-image
                v-if="row.category === 'IMAGE'"
                :src="getMediaUrl(row.id)"
                :preview-src-list="[getMediaUrl(row.id)]"
                fit="cover"
                style="width: 50px; height: 50px; border-radius: 4px;"
                lazy
              />
              <div v-else class="table-file-icon">
                <el-icon :color="getFileIconColor(row.category)">
                  <component :is="getFileIcon(row.category)" />
                </el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
          <el-table-column prop="originalName" label="原始名称" min-width="150" show-overflow-tooltip />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getCategoryType(row.category)" size="small">
                {{ getCategoryName(row.category) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fileSize" label="大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
          <el-table-column prop="tags" label="标签" width="120" show-overflow-tooltip />
          <el-table-column label="上传时间" width="160">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="previewFile(row)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button type="info" size="small" @click="downloadFile(row)">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
              <el-button type="warning" size="small" @click="editFile(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteFile(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 编辑文件对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑文件信息"
      width="600px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="文件名" prop="fileName">
          <el-input v-model="editForm.fileName" placeholder="请输入文件名" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入文件描述"
          />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="editForm.category" placeholder="选择分类">
            <el-option
              v-for="category in mediaCategories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFile" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="currentFile?.fileName"
      width="80%"
      top="5vh"
    >
      <div class="file-preview">
        <!-- 图片预览 -->
        <el-image
          v-if="currentFile?.category === 'IMAGE'"
          :src="getMediaUrl(currentFile.id)"
          fit="contain"
          style="width: 100%; max-height: 70vh;"
        />
        
        <!-- 音频预览 -->
        <audio
          v-else-if="currentFile?.category === 'AUDIO'"
          :src="getMediaUrl(currentFile.id)"
          controls
          style="width: 100%;"
        />
        
        <!-- 视频预览 -->
        <video
          v-else-if="currentFile?.category === 'VIDEO'"
          :src="getMediaUrl(currentFile.id)"
          controls
          style="width: 100%; max-height: 70vh;"
        />
        
        <!-- 其他文件 -->
        <div v-else class="unsupported-preview">
          <el-icon size="60" color="#dcdfe6">
            <component :is="getFileIcon(currentFile?.category)" />
          </el-icon>
          <p>此文件类型不支持预览</p>
          <el-button type="primary" @click="downloadFile(currentFile)">
            <el-icon><Download /></el-icon>
            下载文件
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 上传进度对话框 -->
    <el-dialog
      v-model="uploadProgressVisible"
      title="文件上传"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="upload-progress">
        <div class="upload-file-info">
          <span>{{ currentUploadFile?.name }}</span>
          <span>{{ formatFileSize(currentUploadFile?.size) }}</span>
        </div>
        <el-progress :percentage="uploadProgress" :status="uploadStatus" />
        <div class="upload-speed">
          上传速度: {{ uploadSpeed }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mediaApi } from '@/api/media'
import {
  Search,
  Refresh,
  Upload,
  Delete,
  Edit,
  View,
  Download,
  Grid,
  List,
  Picture,
  VideoPlay,
  Headset,
  Document,
  Folder
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const editDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const uploadProgressVisible = ref(false)
const editFormRef = ref(null)
const uploadRef = ref(null)

const mediaList = ref([])
const selectedFiles = ref([])
const currentFile = ref(null)
const currentUploadFile = ref(null)
const uploadProgress = ref(0)
const uploadStatus = ref('')
const uploadSpeed = ref('0 KB/s')

const viewMode = ref('grid') // 'grid' | 'list'

// 媒体分类
const mediaCategories = mediaApi.getMediaCategories()

// 搜索表单
const searchForm = reactive({
  fileName: '',
  category: '',
  sizeRange: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 24,
  total: 0
})

// 编辑表单
const editForm = reactive({
  id: null,
  fileName: '',
  description: '',
  category: '',
  tags: ''
})

// 表单验证规则
const editRules = {
  fileName: [
    { required: true, message: '请输入文件名', trigger: 'blur' }
  ]
}

// 计算属性
const hasSelection = computed(() => selectedFiles.value.length > 0)

const totalSize = computed(() => {
  const total = mediaList.value.reduce((sum, file) => sum + (file.fileSize || 0), 0)
  return formatFileSize(total)
})

// 获取媒体文件URL
const getMediaUrl = (id) => {
  return mediaApi.getMediaUrl(id)
}

// 获取文件图标
const getFileIcon = (category) => {
  const icons = {
    IMAGE: Picture,
    AUDIO: Headset,
    VIDEO: VideoPlay,
    DOCUMENT: Document,
    OTHER: Folder
  }
  return icons[category] || Folder
}

// 获取文件图标颜色
const getFileIconColor = (category) => {
  const colors = {
    IMAGE: '#67c23a',
    AUDIO: '#e6a23c',
    VIDEO: '#409eff',
    DOCUMENT: '#f56c6c',
    OTHER: '#909399'
  }
  return colors[category] || '#909399'
}

// 获取分类名称
const getCategoryName = (category) => {
  const categoryItem = mediaCategories.find(c => c.id === category)
  return categoryItem?.name || '其他'
}

// 获取分类标签类型
const getCategoryType = (category) => {
  const types = {
    IMAGE: 'success',
    AUDIO: 'warning',
    VIDEO: 'primary',
    DOCUMENT: 'danger',
    OTHER: 'info'
  }
  return types[category] || 'info'
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  return mediaApi.formatFileSize(bytes)
}

// 加载媒体文件列表
const loadMediaFiles = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: 'createdAt,desc',
      ...searchForm
    }
    
    const response = await mediaApi.getMediaFiles(params)
    
    mediaList.value = response.content || []
    pagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载媒体文件失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadMediaFiles()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    fileName: '',
    category: '',
    sizeRange: ''
  })
  handleSearch()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadMediaFiles()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadMediaFiles()
}

// 视图模式切换
const handleViewModeChange = () => {
  selectedFiles.value = []
  // 调整分页大小
  if (viewMode.value === 'grid') {
    pagination.size = 24
  } else {
    pagination.size = 20
  }
  pagination.page = 1
  loadMediaFiles()
}

// 选择处理（网格视图）
const toggleSelection = (fileId) => {
  const index = selectedFiles.value.indexOf(fileId)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  } else {
    selectedFiles.value.push(fileId)
  }
}

// 选择处理（列表视图）
const handleSelectionChange = (selection) => {
  selectedFiles.value = selection.map(file => file.id)
}

// 文件上传前处理
const beforeUpload = (file) => {
  // 验证文件大小（50MB限制）
  const maxSize = 50 * 1024 * 1024
  if (!mediaApi.validateFileSize(file, maxSize)) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }
  
  currentUploadFile.value = file
  uploading.value = true
  uploadProgressVisible.value = true
  uploadProgress.value = 0
  uploadStatus.value = ''
  
  return true
}

// 上传进度处理
const handleUploadProgress = (progressEvent) => {
  const { loaded, total } = progressEvent
  uploadProgress.value = Math.round((loaded / total) * 100)
  
  // 计算上传速度
  const speed = loaded / (Date.now() - uploadStartTime) * 1000
  uploadSpeed.value = formatFileSize(speed) + '/s'
}

let uploadStartTime = 0

// 文件上传
const handleFileUpload = async (file) => {
  try {
    uploadStartTime = Date.now()
    
    const formData = new FormData()
    formData.append('file', file)
    formData.append('description', `上传的${getCategoryName(mediaApi.getFileType(file.name))}文件`)
    formData.append('category', mediaApi.getFileType(file.name))
    
    await mediaApi.uploadMedia(formData, {
      onUploadProgress: handleUploadProgress
    })
    
    uploadStatus.value = 'success'
    ElMessage.success('文件上传成功')
    
    setTimeout(() => {
      uploadProgressVisible.value = false
      uploading.value = false
      loadMediaFiles()
    }, 1000)
  } catch (error) {
    uploadStatus.value = 'exception'
    ElMessage.error('文件上传失败')
    uploading.value = false
    uploadProgressVisible.value = false
  }
}

// 编辑文件
const editFile = (file) => {
  Object.assign(editForm, {
    id: file.id,
    fileName: file.fileName,
    description: file.description || '',
    category: file.category,
    tags: file.tags || ''
  })
  editDialogVisible.value = true
}

// 保存文件信息
const saveFile = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    await mediaApi.updateMedia(editForm.id, editForm)
    
    ElMessage.success('文件信息更新成功')
    editDialogVisible.value = false
    loadMediaFiles()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 预览文件
const previewFile = (file) => {
  currentFile.value = file
  previewDialogVisible.value = true
}

// 下载文件
const downloadFile = async (file) => {
  try {
    await mediaApi.downloadMedia(file.id, file.originalName || file.fileName)
    ElMessage.success('文件下载成功')
  } catch (error) {
    ElMessage.error('文件下载失败')
  }
}

// 删除文件
const deleteFile = (file) => {
  ElMessageBox.confirm(
    `确定要删除文件 "${file.fileName}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await mediaApi.deleteMedia(file.id)
      ElMessage.success('删除成功')
      loadMediaFiles()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const batchDelete = () => {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning('请选择要删除的文件')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedFiles.value.length} 个文件吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await mediaApi.batchDeleteMedia(selectedFiles.value)
      ElMessage.success('批量删除成功')
      selectedFiles.value = []
      loadMediaFiles()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadMediaFiles()
})
</script>

<style scoped>
.media-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.view-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;
}

.file-stats {
  color: #606266;
  font-size: 14px;
}

.grid-view {
  min-height: 400px;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  padding: 20px;
}

.media-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.media-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.media-item.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.media-preview {
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #c0c4cc;
}

.file-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.media-info {
  padding: 12px;
}

.file-name {
  font-weight: 500;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.file-actions {
  display: flex;
  justify-content: space-around;
}

.file-actions .el-button {
  padding: 4px;
}

.file-actions .danger {
  color: #f56c6c;
}

.table-file-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 4px;
  background-color: #f8f9fa;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.file-preview {
  text-align: center;
}

.unsupported-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.unsupported-preview p {
  margin: 20px 0;
  font-size: 16px;
}

.upload-progress {
  text-align: center;
}

.upload-file-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  font-size: 14px;
  color: #606266;
}

.upload-speed {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .media-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
    padding: 15px;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 5px;
  }
  
  .view-controls {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .search-form .el-form--inline .el-form-item {
    display: block;
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>

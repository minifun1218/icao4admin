<template>
  <div class="media-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>媒体资源管理</h2>
      <p>管理系统中的音频、图片、视频和文档资源</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showUploadDialog">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedMedia.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="refreshList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-select
          v-model="filterType"
          placeholder="选择类型"
          clearable
          style="width: 120px; margin-right: 12px"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="音频" value="audio" />
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
          <el-option label="文档" value="doc" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文件名或转录内容..."
          style="width: 250px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="showAdvancedSearch = !showAdvancedSearch">
          高级搜索
        </el-button>
      </div>
    </div>

    <!-- 高级搜索面板 -->
    <el-collapse-transition>
      <div v-show="showAdvancedSearch" class="advanced-search">
        <el-form :model="advancedSearchForm" inline>
          <el-form-item label="媒体类型">
            <el-select v-model="advancedSearchForm.mediaType" placeholder="选择类型" clearable>
              <el-option label="音频" value="audio" />
              <el-option label="图片" value="image" />
              <el-option label="视频" value="video" />
              <el-option label="文档" value="doc" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="advancedSearchForm.keyword" placeholder="输入关键词" />
          </el-form-item>
          <el-form-item label="有转录内容">
            <el-select v-model="advancedSearchForm.hasTranscript" placeholder="选择" clearable>
              <el-option label="是" :value="true" />
              <el-option label="否" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item label="时长范围">
            <el-input-number 
              v-model="advancedSearchForm.minDuration" 
              :min="0" 
              placeholder="最小时长(秒)"
              style="width: 120px"
            />
            <span style="margin: 0 8px">-</span>
            <el-input-number 
              v-model="advancedSearchForm.maxDuration" 
              :min="0" 
              placeholder="最大时长(秒)"
              style="width: 120px"
            />
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="advancedSearchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdvancedSearch">搜索</el-button>
            <el-button @click="resetAdvancedSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-collapse-transition>

    <!-- 统计信息 -->
    <div class="statistics">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="总文件数" :value="statistics.totalCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="音频文件" :value="statistics.audioCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="图片文件" :value="statistics.imageCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="视频文件" :value="statistics.videoCount || 0" />
        </el-col>
      </el-row>
    </div>

    <!-- 调试信息面板 -->
    <div v-if="isDevelopment" class="debug-panel">
      <el-collapse>
        <el-collapse-item title="🔍 调试信息" name="debug">
          <div class="debug-content">
            <p><strong>媒体列表长度:</strong> {{ mediaList.length }}</p>
            <p><strong>分页信息:</strong> 第{{ pagination.page }}页，共{{ pagination.total }}条</p>
            <p><strong>加载状态:</strong> {{ loading ? '加载中' : '已完成' }}</p>
            <p><strong>筛选类型:</strong> {{ filterType || '无' }}</p>
            <div v-if="mediaList.length > 0">
              <p><strong>第一个项目:</strong></p>
              <pre class="debug-json">{{ JSON.stringify(mediaList[0], null, 2) }}</pre>
            </div>
            <div v-else>
              <p><strong>⚠️ 媒体列表为空</strong></p>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 媒体文件网格视图 -->
    <div class="media-grid">
      <div class="view-controls">
        <el-radio-group v-model="viewMode" @change="handleViewModeChange">
          <el-radio-button label="grid">网格视图</el-radio-button>
          <el-radio-button label="list">列表视图</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="grid-view">
        <!-- 空状态 -->
        <div v-if="!loading && mediaList.length === 0" class="empty-state">
          <el-empty description="暂无媒体文件">
            <el-button type="primary" @click="showUploadDialog">
              <el-icon><Upload /></el-icon>
              上传第一个文件
            </el-button>
          </el-empty>
        </div>
        
        <!-- 媒体卡片 -->
        <div 
          v-for="media in mediaList" 
          :key="media.id" 
          class="media-card"
          :class="{ selected: selectedMedia.includes(media.id) }"
          @click="handleCardClick(media)"
        >
          <div class="card-header">
            <el-checkbox 
              :model-value="selectedMedia.includes(media.id)"
              @change="(checked) => handleSelectChange(media.id, checked)"
              @click.stop
            />
            <el-dropdown @command="(command) => handleCardAction(command, media)" @click.stop>
              <el-button type="text" size="small">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view">查看详情</el-dropdown-item>
                  <el-dropdown-item command="download">下载</el-dropdown-item>
                  <el-dropdown-item command="edit">编辑信息</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <div class="card-content">
            <!-- 媒体预览 -->
            <div class="media-preview">
              <div v-if="media.isImage" class="image-preview">
                <el-image 
                  :src="getCachedMediaUrl(media, 'preview')"
                  fit="cover"
                  lazy
                  class="preview-image"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
              
              <div v-else-if="media.isAudio" class="audio-preview">
                <div class="media-icon">
                  <el-icon size="48" :color="getMediaTypeColor(media)"><VideoPlay /></el-icon>
                </div>
                <audio v-if="media.previewUrl" :src="getCachedMediaUrl(media, 'preview')" controls class="audio-player" />
              </div>
              
              <div v-else-if="media.isVideo" class="video-preview">
                <div class="media-icon">
                  <el-icon size="48" :color="getMediaTypeColor(media)"><VideoCamera /></el-icon>
                </div>
                <video v-if="media.previewUrl" :src="getCachedMediaUrl(media, 'preview')" controls class="video-player" />
              </div>
              
              <div v-else class="doc-preview">
                <div class="media-icon">
                  <el-icon size="48" :color="getMediaTypeColor(media)"><Document /></el-icon>
                </div>
              </div>
            </div>
            
            <!-- 文件信息 -->
            <div class="file-info">
              <div class="file-name" :title="media.filename || media.originalFilename">
                {{ media.filename || media.originalFilename }}
              </div>
              <div class="file-meta">
                <el-tag :type="getMediaTypeTagType(media.type)" size="small">
                  {{ getMediaTypeLabel(media.type) }}
                </el-tag>
                <span v-if="media.duration || media.durationDescription" class="duration">
                  {{ media.durationDescription || formatDuration(media.duration) }}
                </span>
              </div>
              <div class="file-date">
                {{ formatDateTime(media.createdAt) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'" class="list-view">
        <el-table
          v-loading="loading"
          :data="mediaList"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          stripe
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="预览" width="80" align="center">
            <template #default="scope">
              <div class="table-preview">
                <el-image
                  v-if="scope.row.isImage"
                  :src="getCachedMediaUrl(scope.row, 'preview')"
                  fit="cover"
                  class="table-thumbnail"
                  lazy
                >
                  <template #error>
                    <el-icon><Picture /></el-icon>
                  </template>
                </el-image>
                <el-icon v-else :color="getMediaTypeColor(scope.row)" size="24">
                  <component :is="getMediaTypeIcon(scope.row)" />
                </el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column 
            prop="filename" 
            label="文件名" 
            min-width="200"
            show-overflow-tooltip
          >
            <template #default="scope">
              {{ scope.row.filename || scope.row.originalFilename }}
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="80">
            <template #default="scope">
              <el-tag :type="getMediaTypeTagType(scope.row.type)" size="small">
                {{ getMediaTypeLabel(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="时长" width="100">
            <template #default="scope">
              {{ scope.row.durationDescription || (scope.row.duration ? formatDuration(scope.row.duration) : '-') }}
            </template>
          </el-table-column>
          <el-table-column 
            prop="description" 
            label="描述" 
            min-width="150"
            show-overflow-tooltip
          >
            <template #default="scope">
              {{ scope.row.description || '-' }}
            </template>
          </el-table-column>
          <el-table-column 
            prop="fileSizeDescription" 
            label="文件大小" 
            width="100"
          >
            <template #default="scope">
              {{ scope.row.fileSizeDescription || '-' }}
            </template>
          </el-table-column>
          <el-table-column 
            prop="createdAt" 
            label="创建时间" 
            width="180"
            sortable="custom"
          >
            <template #default="scope">
              {{ formatDateTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleView(scope.row)">
                查看
              </el-button>
              <el-button size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button size="small" @click="handleDownload(scope.row)">
                下载
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 上传文件对话框 -->
    <el-dialog v-model="uploadVisible" title="上传媒体文件" width="600px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        :file-list="fileList"
        multiple
        drag
        class="upload-demo"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持音频、图片、视频、文档格式，单个文件不超过 100MB
          </div>
        </template>
      </el-upload>
      
      <div class="upload-options">
        <el-form :model="uploadForm" label-width="80px">
          <el-form-item label="文件类型">
            <el-select v-model="uploadForm.type" placeholder="自动检测">
              <el-option label="自动检测" value="auto" />
              <el-option label="音频" value="audio" />
              <el-option label="图片" value="image" />
              <el-option label="视频" value="video" />
              <el-option label="文档" value="doc" />
            </el-select>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="uploadForm.title" placeholder="可选" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input 
              v-model="uploadForm.description" 
              type="textarea" 
              :rows="2"
              placeholder="可选"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleUpload"
          :loading="uploadLoading"
        >
          上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 媒体详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentMedia ? getFileName(currentMedia) : '媒体详情'"
      width="800px"
    >
      <div v-if="currentMedia" class="media-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 媒体预览 -->
            <div class="detail-preview">
              <div v-if="currentMedia.isImage" class="image-detail">
                <el-image 
                  :src="getPreviewUrl(currentMedia)" 
                  fit="contain"
                  class="detail-image"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon size="48"><Picture /></el-icon>
                      <p>图片加载失败</p>
                    </div>
                  </template>
                </el-image>
              </div>
              
              <div v-else-if="currentMedia.isAudio" class="audio-detail">
                <div class="media-icon-large">
                  <el-icon size="64" :color="getMediaTypeColor(currentMedia)"><VideoPlay /></el-icon>
                </div>
                <audio 
                  v-if="currentMedia.previewUrl" 
                  :src="getCachedMediaUrl(currentMedia, 'preview')" 
                  controls 
                  class="detail-audio"
                />
              </div>
              
              <div v-else-if="currentMedia.isVideo" class="video-detail">
                <video 
                  v-if="currentMedia.previewUrl" 
                  :src="getCachedMediaUrl(currentMedia, 'preview')" 
                  controls 
                  class="detail-video"
                />
              </div>
              
              <div v-else class="doc-detail">
                <div class="media-icon-large">
                  <el-icon size="64" :color="getMediaTypeColor(currentMedia)"><Document /></el-icon>
                </div>
                <p>{{ currentMedia.filename || currentMedia.originalFilename }}</p>
              </div>
            </div>
          </el-col>
          
          <el-col :span="12">
            <!-- 媒体信息 -->
            <div class="detail-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="文件名">
                  {{ currentMedia.filename || currentMedia.originalFilename }}
                </el-descriptions-item>
                <el-descriptions-item label="类型">
                  <el-tag :type="getMediaTypeTagType(currentMedia.type)">
                    {{ getMediaTypeLabel(currentMedia.type) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="文件大小">
                  {{ currentMedia.fileSizeDescription || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="时长" v-if="currentMedia.duration || currentMedia.durationDescription">
                  {{ currentMedia.durationDescription || formatDuration(currentMedia.duration) }}
                </el-descriptions-item>
                <el-descriptions-item label="质量">
                  {{ currentMedia.quality || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="来源">
                  {{ currentMedia.source || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ formatDateTime(currentMedia.createdAt) }}
                </el-descriptions-item>
                <el-descriptions-item label="文件路径">
                  {{ currentMedia.filePath }}
                </el-descriptions-item>
              </el-descriptions>
              
              <!-- 描述 -->
              <div v-if="currentMedia.description" class="description-section">
                <h4>描述</h4>
                <div class="description-content">
                  {{ currentMedia.description }}
                </div>
              </div>
              
              <!-- 标签 -->
              <div v-if="currentMedia.tags" class="tags-section">
                <h4>标签</h4>
                <div class="tags-content">
                  <el-tag v-for="tag in currentMedia.tags" :key="tag" style="margin-right: 8px;">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              
              <!-- 元数据 -->
              <div v-if="currentMedia.metadata" class="metadata-section">
                <h4>元数据</h4>
                <pre class="metadata-content">{{ formatMetadata(currentMedia.metadata) }}</pre>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleDownload(currentMedia)">
          下载文件
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑媒体信息对话框 -->
    <el-dialog
      v-model="editVisible"
      title="编辑媒体信息"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        label-width="100px"
      >
        <el-form-item label="标题">
          <el-input
            v-model="editForm.title"
            placeholder="输入文件标题"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="输入文件描述"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input
            v-model="editForm.tagsText"
            placeholder="输入标签，用逗号分隔"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSaveEdit"
          :loading="editLoading"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload,
  Delete,
  Refresh,
  Search,
  More,
  Picture,
  VideoPlay,
  VideoCamera,
  Document,
  UploadFilled
} from '@element-plus/icons-vue'
import {
  getMediaList,
  getMediaById,
  deleteMedia,
  deleteMediaBatch,
  uploadMediaFile,
  searchMedia,
  getMediaStats,
  advancedSearchMedia,
  updateMediaInfo,
  getPreviewUrl,
  getDownloadUrl,
  getThumbnailUrl,
  formatDuration,
  getMediaTypeIcon,
  getMediaTypeColor,
  formatFileSize
} from '@/api/media'
import { 
  MEDIA_CONFIG, 
  validateFileType, 
  validateFileSize, 
  getMaxFileSizeText, 
  getSupportedTypesText 
} from '@/utils/media-config'

// 响应式数据
const loading = ref(false)
const uploadLoading = ref(false)
const editLoading = ref(false)
const uploadVisible = ref(false)
const detailVisible = ref(false)
const editVisible = ref(false)
const showAdvancedSearch = ref(false)

const mediaList = ref([])
const selectedMedia = ref([])
const statistics = ref({})
const currentMedia = ref(null)

// 视图模式
const viewMode = ref('grid')

// 搜索和筛选
const searchKeyword = ref('')
const filterType = ref('')

// 分页
const pagination = reactive({
  page: 1,
  size: 24,
  total: 0
})

// 高级搜索表单
const advancedSearchForm = reactive({
  mediaType: '',
  keyword: '',
  hasTranscript: null,
  minDuration: null,
  maxDuration: null,
  dateRange: []
})

// 上传表单
const uploadForm = reactive({
  type: 'auto',
  title: '',
  description: ''
})

const fileList = ref([])

// 编辑表单
const editForm = reactive({
  title: '',
  description: '',
  tagsText: ''
})

// 计算属性
const formatDateTime = computed(() => {
  return (dateTime) => {
    if (!dateTime) return '-'
    return new Date(dateTime).toLocaleString('zh-CN')
  }
})

// 开发环境判断
const isDevelopment = computed(() => {
  return import.meta.env.DEV
})

// 媒体URL刷新机制
const mediaUrlCache = reactive(new Map())

// 获取带缓存的媒体URL
const getCachedMediaUrl = (mediaAsset, type = 'preview') => {
  if (!mediaAsset) return null
  
  const cacheKey = `${mediaAsset.id || mediaAsset.filename}_${type}`
  
  if (mediaUrlCache.has(cacheKey)) {
    return mediaUrlCache.get(cacheKey)
  }
  
  let url = null
  switch (type) {
    case 'preview':
      url = getPreviewUrl(mediaAsset)
      break
    case 'thumbnail':
      url = getPreviewUrl(mediaAsset)
      break
    case 'download':
      url = getDownloadUrl(mediaAsset)
      break
    default:
      url = getPreviewUrl(mediaAsset)
  }
  
  if (url) {
    mediaUrlCache.set(cacheKey, url)
  }
  
  return url
}

// 清除URL缓存
const clearMediaUrlCache = () => {
  mediaUrlCache.clear()
}

// 方法
const loadMediaList = async () => {
  try {
    loading.value = true
    // 清除URL缓存，确保使用最新的token
    clearMediaUrlCache()
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    
    if (filterType.value) {
      params.type = filterType.value
    }
    
    console.log('📡 发送媒体列表请求，参数:', params)
    const response = await getMediaList(params)
    console.log('📦 收到媒体列表响应:', response)
    console.log('🔍 响应数据详情:', response.data)
    
    if (response && response.data) {
      // 检查多种可能的数据结构
      let mediaData = []
      let totalCount = 0
      
      if (response.data.data) {
        // 格式: { data: { content: [...], totalElements: ... } }
        console.log('📊 检测到嵌套data结构')
        mediaData = response.data.data.content || []
        totalCount = response.data.data.totalElements || 0
      } else if (response.data.content) {
        // 格式: { content: [...], totalElements: ... }
        console.log('📊 检测到直接content结构')
        mediaData = response.data.content || []
        totalCount = response.data.totalElements || 0
      } else if (Array.isArray(response.data)) {
        // 格式: [...] 直接数组
        console.log('📊 检测到直接数组结构')
        mediaData = response.data
        totalCount = response.data.length
      } else {
        console.log('📊 未知数据结构，尝试解析')
        console.log('📊 响应数据键:', Object.keys(response.data))
        mediaData = response.data.content || response.data.data?.content || []
        totalCount = response.data.totalElements || response.data.data?.totalElements || 0
      }
      
      console.log('✅ 解析后的数据结构:', {
        mediaDataLength: mediaData.length,
        totalCount: totalCount,
        firstItem: mediaData[0],
        dataKeys: mediaData[0] ? Object.keys(mediaData[0]) : []
      })
      
      mediaList.value = mediaData
      pagination.total = totalCount
      
      console.log('📋 设置媒体列表:', mediaList.value.length, '个项目')
    } else {
      console.warn('⚠️ 响应数据格式异常:', response)
      ElMessage.warning('响应数据格式异常')
    }
  } catch (error) {
    ElMessage.error('加载媒体列表失败')
    console.error('❌ 加载媒体列表错误:', error)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await getMediaStats()
    if (response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const refreshList = () => {
  loadMediaList()
  loadStatistics()
}

const handleFilterChange = () => {
  pagination.page = 1
  loadMediaList()
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadMediaList()
    return
  }
  
  try {
    loading.value = true
    const response = await searchMedia(searchKeyword.value)
    if (response.data) {
      mediaList.value = response.data
      pagination.total = response.data.length
    }
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdvancedSearch = async () => {
  try {
    loading.value = true
    const searchParams = {
      ...advancedSearchForm,
      page: 0,
      size: pagination.size
    }
    
    if (advancedSearchForm.dateRange && advancedSearchForm.dateRange.length === 2) {
      searchParams.startDate = advancedSearchForm.dateRange[0]
      searchParams.endDate = advancedSearchForm.dateRange[1]
    }
    
    const response = await advancedSearchMedia(searchParams)
    if (response.data) {
      mediaList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
      pagination.page = 1
    }
  } catch (error) {
    ElMessage.error('高级搜索失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetAdvancedSearch = () => {
  Object.assign(advancedSearchForm, {
    mediaType: '',
    keyword: '',
    hasTranscript: null,
    minDuration: null,
    maxDuration: null,
    dateRange: []
  })
}

const handleViewModeChange = () => {
  // 网格视图和列表视图切换时可能需要调整分页大小
  if (viewMode.value === 'grid') {
    pagination.size = 24
  } else {
    pagination.size = 20
  }
  pagination.page = 1
  loadMediaList()
}

const handleCardClick = (media) => {
  handleView(media)
}

const handleSelectChange = (mediaId, checked) => {
  if (checked) {
    if (!selectedMedia.value.includes(mediaId)) {
      selectedMedia.value.push(mediaId)
    }
  } else {
    const index = selectedMedia.value.indexOf(mediaId)
    if (index > -1) {
      selectedMedia.value.splice(index, 1)
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedMedia.value = selection.map(item => item.id)
}

const handleCardAction = (command, media) => {
  switch (command) {
    case 'view':
      handleView(media)
      break
    case 'download':
      handleDownload(media)
      break
    case 'edit':
      handleEdit(media)
      break
    case 'delete':
      handleDelete(media)
      break
  }
}

const handleView = (media) => {
  currentMedia.value = media
  detailVisible.value = true
}

const handleEdit = (media) => {
  currentMedia.value = media
  editForm.title = media.title || ''
  editForm.description = media.description || ''
  editForm.tagsText = media.tags ? media.tags.join(', ') : ''
  editVisible.value = true
}

const handleDownload = (media) => {
  const downloadUrl = getDownloadUrl(media)
  if (downloadUrl) {
    window.open(downloadUrl, '_blank')
  } else {
    ElMessage.error('无法获取下载链接')
  }
}

const handleDelete = async (media) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件"${media.filename || media.originalFilename}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteMedia(media.id)
    ElMessage.success('删除成功')
    refreshList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedMedia.value.length} 个文件吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteMediaBatch(selectedMedia.value)
    ElMessage.success('批量删除成功')
    selectedMedia.value = []
    refreshList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error(error)
    }
  }
}

const handleSortChange = ({ prop, order }) => {
  // 处理排序逻辑
  console.log('排序:', prop, order)
  loadMediaList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadMediaList()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadMediaList()
}

const showUploadDialog = () => {
  uploadVisible.value = true
  fileList.value = []
  Object.assign(uploadForm, {
    type: 'auto',
    title: '',
    description: ''
  })
}

const handleFileChange = (file) => {
  // 文件选择处理
}

const beforeUpload = (file) => {
  // 验证文件类型
  if (!validateFileType(file)) {
    ElMessage.error(`不支持的文件类型，支持的类型：${getSupportedTypesText()}`)
    return false
  }
  
  // 验证文件大小
  if (!validateFileSize(file)) {
    ElMessage.error(`文件大小不能超过 ${getMaxFileSizeText()}`)
    return false
  }
  
  return true
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  
  try {
    uploadLoading.value = true
    
    for (const file of fileList.value) {
      const formData = new FormData()
      formData.append('file', file.raw)
      formData.append('type', uploadForm.type)
      
      if (uploadForm.title) {
        formData.append('title', uploadForm.title)
      }
      
      if (uploadForm.description) {
        formData.append('description', uploadForm.description)
      }
      
      await uploadMediaFile(formData)
    }
    
    ElMessage.success('上传成功')
    uploadVisible.value = false
    refreshList()
  } catch (error) {
    ElMessage.error('上传失败')
    console.error(error)
  } finally {
    uploadLoading.value = false
  }
}

const handleSaveEdit = async () => {
  try {
    editLoading.value = true
    
    const updateData = {
      title: editForm.title,
      description: editForm.description,
      tags: editForm.tagsText ? editForm.tagsText.split(',').map(tag => tag.trim()).filter(tag => tag) : null
    }
    
    await updateMediaInfo(currentMedia.value.id, updateData)
    ElMessage.success('更新成功')
    editVisible.value = false
    refreshList()
  } catch (error) {
    ElMessage.error('更新失败')
    console.error(error)
  } finally {
    editLoading.value = false
  }
}

// 辅助方法
const getFileName = (media) => {
  if (!media) return '未知文件'
  return media.filename || media.originalFilename || '未知文件'
}

const getMediaTypeLabel = (type) => {
  const labels = {
    audio: '音频',
    image: '图片', 
    video: '视频',
    doc: '文档'
  }
  return labels[type] || '未知'
}

const getMediaTypeTagType = (type) => {
  const types = {
    audio: 'success',
    image: 'primary',
    video: 'warning',
    doc: 'info'
  }
  return types[type] || 'info'
}

const formatMetadata = (jsonString) => {
  try {
    return JSON.stringify(JSON.parse(jsonString), null, 2)
  } catch {
    return jsonString
  }
}

// 生命周期
onMounted(() => {
  loadMediaList()
  loadStatistics()
})

// 模板引用
const uploadRef = ref(null)
const editFormRef = ref(null)
</script>

<style scoped>
.media-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.advanced-search {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.statistics {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 调试面板样式 */
.debug-panel {
  margin-bottom: 20px;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
}

.debug-content {
  padding: 16px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.debug-json {
  background: #f8f9fa;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #e9ecef;
  font-size: 11px;
  overflow-x: auto;
  max-height: 200px;
}

/* 空状态样式 */
.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
  text-align: center;
}

.media-grid {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.view-controls {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 网格视图样式 */
.grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.media-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.media-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.media-card.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.card-content {
  padding: 16px;
}

.media-preview {
  margin-bottom: 12px;
}

.image-preview {
  width: 100%;
  height: 160px;
  border-radius: 4px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f5f5;
  color: #909399;
}

.audio-preview,
.video-preview,
.doc-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.media-icon {
  margin-bottom: 12px;
}

.audio-player,
.video-player {
  width: 100%;
  max-height: 120px;
}

.file-info {
  text-align: center;
}

.file-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.duration {
  font-size: 12px;
  color: #909399;
}

.file-date {
  font-size: 12px;
  color: #c0c4cc;
}

/* 列表视图样式 */
.list-view {
  margin-bottom: 20px;
}

.table-preview {
  display: flex;
  justify-content: center;
  align-items: center;
}

.table-thumbnail {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}

/* 分页样式 */
.pagination {
  padding: 20px;
  text-align: center;
  border-top: 1px solid #e9ecef;
}

/* 对话框样式 */
.upload-demo {
  margin-bottom: 20px;
}

.upload-options {
  margin-top: 20px;
}

.media-detail {
  min-height: 400px;
}

.detail-preview {
  text-align: center;
}

.detail-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
}

.media-icon-large {
  margin-bottom: 16px;
}

.detail-audio,
.detail-video {
  width: 100%;
  margin-top: 16px;
}

.detail-info {
  height: 100%;
}

.description-section,
.tags-section,
.metadata-section {
  margin-top: 20px;
}

.description-section h4,
.tags-section h4,
.metadata-section h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.description-content {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
  line-height: 1.6;
}

.tags-content {
  padding: 8px 0;
}

.metadata-content {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
  font-size: 12px;
  overflow-x: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .toolbar-left,
  .toolbar-right {
    justify-content: center;
  }

  .grid-view {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }

  .advanced-search .el-form {
    flex-direction: column;
  }
}
</style>

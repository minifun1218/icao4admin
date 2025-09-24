import request from '@/utils/request'
import { createApiMethod } from '@/utils/request'

/**
 * 媒体资源管理 API
 * 基于 MediaAssetController.java
 */

// ==================== 基础 CRUD 操作 ====================

/**
 * 上传媒体文件
 * POST /media/upload
 */
export const uploadMediaFile = (formData, config = {}) => {
  return request.post('/media/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    ...config
  })
}

/**
 * 根据ID获取媒体资源
 * GET /media/{id}
 */
export const getMediaById = createApiMethod('get', '/media/{id}')

/**
 * 删除媒体资源
 * DELETE /media/{id}
 */
export const deleteMedia = (id) => {
  return request.delete(`/media/${id}`)
}

/**
 * 批量删除媒体资源
 */
export const deleteMediaBatch = (ids) => {
  const deletePromises = ids.map(id => deleteMedia(id))
  return Promise.all(deletePromises)
}

// ==================== 查询操作 ====================

/**
 * 分页获取所有媒体资源
 * GET /media
 */
export const getMediaList = (params = {}) => {
  return request.get('/media', { params })
}

/**
 * 根据类型获取媒体资源
 * GET /media/type/{type}
 */
export const getMediaByType = (type) => {
  return request.get(`/media/type/${type}`)
}

/**
 * 搜索媒体资源（根据转录内容）
 * GET /media/search
 */
export const searchMedia = (keyword) => {
  return request.get('/media/search', { params: { keyword } })
}

/**
 * 获取媒体资源统计信息
 * GET /media/stats
 */
export const getMediaStats = () => {
  return request.get('/media/stats')
}

// ==================== 辅助方法 ====================

/**
 * 获取媒体类型选项
 */
export const getMediaTypes = () => {
  return Promise.resolve({
    data: [
      { value: 'audio', label: '音频', icon: 'VideoPlay' },
      { value: 'image', label: '图片', icon: 'Picture' },
      { value: 'video', label: '视频', icon: 'VideoCamera' },
      { value: 'doc', label: '文档', icon: 'Document' }
    ]
  })
}

/**
 * 根据媒体类型获取图标
 */
export const getMediaTypeIcon = (mediaAsset) => {
  if (!mediaAsset) return 'Document'
  
  // 优先使用布尔字段判断
  if (mediaAsset.isAudio) return 'VideoPlay'
  if (mediaAsset.isImage) return 'Picture'
  if (mediaAsset.isVideo) return 'VideoCamera'
  if (mediaAsset.isDocument) return 'Document'
  
  // 备选：使用type字段
  const iconMap = {
    audio: 'VideoPlay',
    image: 'Picture', 
    video: 'VideoCamera',
    doc: 'Document'
  }
  return iconMap[mediaAsset.type] || 'Document'
}

/**
 * 根据媒体类型获取颜色
 */
export const getMediaTypeColor = (mediaAsset) => {
  if (!mediaAsset) return '#909399'
  
  // 优先使用布尔字段判断
  if (mediaAsset.isAudio) return '#67c23a'
  if (mediaAsset.isImage) return '#409eff'
  if (mediaAsset.isVideo) return '#e6a23c'
  if (mediaAsset.isDocument) return '#909399'
  
  // 备选：使用type字段
  const colorMap = {
    audio: '#67c23a',
    image: '#409eff',
    video: '#e6a23c',
    doc: '#909399'
  }
  return colorMap[mediaAsset.type] || '#909399'
}

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  
  const sizes = ['B', 'KB', 'MB', 'GB']
  let i = 0
  
  while (bytes >= 1024 && i < sizes.length - 1) {
    bytes /= 1024
    i++
  }
  
  return `${bytes.toFixed(i === 0 ? 0 : 1)} ${sizes[i]}`
}

/**
 * 格式化播放时长
 */
export const formatDuration = (duration) => {
  if (!duration) return '-'
  
  // 如果是已格式化的字符串，直接返回
  if (typeof duration === 'string') {
    return duration
  }
  
  // 如果是秒数，进行格式化
  const seconds = Math.floor(duration)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}:${String(minutes % 60).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`
  }
  
  return `${minutes}:${String(seconds % 60).padStart(2, '0')}`
}

/**
 * 验证文件类型
 */
export const validateFileType = (file, allowedTypes = []) => {
  if (allowedTypes.length === 0) return true
  
  const fileType = file.type.split('/')[0]
  return allowedTypes.includes(fileType)
}

/**
 * 获取文件预览URL
 */
export const getPreviewUrl = (mediaAsset) => {
  if (!mediaAsset) return null
  
  // 如果有预览URL，检查是否为完整URL
  if (mediaAsset.previewUrl) {
    if (mediaAsset.previewUrl.startsWith('http')) {
      return mediaAsset.previewUrl
    }
    // 相对路径，添加API前缀
    return mediaAsset.previewUrl.startsWith('/api') ? mediaAsset.previewUrl : `/api${mediaAsset.previewUrl}`
  }
  
  // 如果有文件URL，检查是否为完整URL
  if (mediaAsset.fileUrl) {
    if (mediaAsset.fileUrl.startsWith('http')) {
      return mediaAsset.fileUrl
    }
    // 相对路径，添加API前缀
    return mediaAsset.fileUrl.startsWith('/api') ? mediaAsset.fileUrl : `/api${mediaAsset.fileUrl}`
  }
  
  // 构建预览URL
  const filename = mediaAsset.filename || mediaAsset.originalFilename
  if (filename) {
    const basePath = import.meta.env.VITE_MEDIA_PREVIEW_PATH || '/api/media/preview'
    return `${basePath}/${filename}`
  }
  
  return null
}

/**
 * 获取下载URL
 */
export const getDownloadUrl = (mediaAsset) => {
  if (!mediaAsset) return null
  
  // 如果有下载URL，检查是否为完整URL
  if (mediaAsset.downloadUrl) {
    if (mediaAsset.downloadUrl.startsWith('http')) {
      return mediaAsset.downloadUrl
    }
    // 相对路径，添加API前缀
    return mediaAsset.downloadUrl.startsWith('/api') ? mediaAsset.downloadUrl : `/api${mediaAsset.downloadUrl}`
  }
  
  // 构建下载URL
  const filename = mediaAsset.filename || mediaAsset.originalFilename
  if (filename) {
    return `/api/media/download/${filename}`
  }
  
  return null
}

/**
 * 获取缩略图URL
 */
export const getThumbnailUrl = (mediaAsset) => {
  if (!mediaAsset) return null
  
  // 如果有缩略图URL，检查是否为完整URL
  if (mediaAsset.thumbnailUrl) {
    if (mediaAsset.thumbnailUrl.startsWith('http')) {
      return mediaAsset.thumbnailUrl
    }
    // 相对路径，添加API前缀
    return mediaAsset.thumbnailUrl.startsWith('/api') ? mediaAsset.thumbnailUrl : `/api${mediaAsset.thumbnailUrl}`
  }
  
  // 对于图片类型，使用预览URL作为缩略图
  if (mediaAsset.isImage && mediaAsset.previewUrl) {
    return getPreviewUrl(mediaAsset)
  }
  
  // 构建缩略图URL
  const filename = mediaAsset.filename || mediaAsset.originalFilename
  if (filename && mediaAsset.isImage) {
    return `/api/media/thumbnail/${filename}`
  }
  
  return null
}

// ==================== 高级搜索 ====================

/**
 * 高级搜索媒体资源
 */
export const advancedSearchMedia = (searchParams) => {
  const params = {
    page: searchParams.page || 0,
    size: searchParams.size || 20,
    sort: searchParams.sort || 'createdAt,desc'
  }
  
  // 添加搜索条件
  if (searchParams.mediaType) {
    params.mediaType = searchParams.mediaType
  }
  
  if (searchParams.keyword) {
    params.keyword = searchParams.keyword
  }
  
  if (searchParams.hasTranscript !== undefined) {
    params.hasTranscript = searchParams.hasTranscript
  }
  
  if (searchParams.minDuration) {
    params.minDuration = searchParams.minDuration
  }
  
  if (searchParams.maxDuration) {
    params.maxDuration = searchParams.maxDuration
  }
  
  if (searchParams.startDate) {
    params.startDate = searchParams.startDate
  }
  
  if (searchParams.endDate) {
    params.endDate = searchParams.endDate
  }
  
  return request.get('/media/search/advanced', { params })
}

// ==================== 批量操作 ====================

/**
 * 批量上传媒体文件
 */
export const batchUploadMedia = (files, options = {}) => {
  const uploadPromises = files.map(file => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', options.type || 'auto')
    
    if (options.title) {
      formData.append('title', options.title)
    }
    
    if (options.description) {
      formData.append('description', options.description)
    }
    
    return uploadMediaFile(formData)
  })
  
  return Promise.all(uploadPromises)
}

/**
 * 更新媒体资源信息（如转录内容、元数据等）
 */
export const updateMediaInfo = (id, updateData) => {
  return request.put(`/media/${id}`, updateData)
}

/**
 * 批量更新媒体资源
 */
export const batchUpdateMedia = (updates) => {
  const updatePromises = updates.map(update => 
    updateMediaInfo(update.id, update.data)
  )
  return Promise.all(updatePromises)
}

export default {
  // 基础操作
  uploadMediaFile,
  getMediaById,
  deleteMedia,
  deleteMediaBatch,
  
  // 查询操作
  getMediaList,
  getMediaByType,
  searchMedia,
  getMediaStats,
  
  // 辅助方法
  getMediaTypes,
  getMediaTypeIcon,
  getMediaTypeColor,
  formatFileSize,
  formatDuration,
  validateFileType,
  getPreviewUrl,
  getDownloadUrl,
  getThumbnailUrl,
  
  // 高级功能
  advancedSearchMedia,
  batchUploadMedia,
  updateMediaInfo,
  batchUpdateMedia
}
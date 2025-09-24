/**
 * 媒体文件配置工具类
 * 统一管理媒体文件相关的配置和路径
 */

/**
 * 媒体文件配置
 */
export const MEDIA_CONFIG = {
  // API路径
  PREVIEW_PATH: import.meta.env.VITE_MEDIA_PREVIEW_PATH || '/api/media/preview',
  DOWNLOAD_PATH: import.meta.env.VITE_MEDIA_DOWNLOAD_PATH || '/api/media/download',
  THUMBNAIL_PATH: import.meta.env.VITE_MEDIA_THUMBNAIL_PATH || '/api/media/thumbnail',
  UPLOAD_PATH: import.meta.env.VITE_MEDIA_UPLOAD_PATH || '/api/media/upload',
  
  // 文件大小限制（字节）
  MAX_FILE_SIZE: parseInt(import.meta.env.VITE_MAX_FILE_SIZE) || 104857600, // 100MB
  
  // 支持的文件类型
  ALLOWED_TYPES: (import.meta.env.VITE_ALLOWED_MEDIA_TYPES || 'image,audio,video,doc').split(','),
  
  // MIME类型映射
  MIME_TYPES: {
    image: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml'],
    audio: ['audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/mp3', 'audio/mp4', 'audio/aac'],
    video: ['video/mp4', 'video/avi', 'video/mov', 'video/wmv', 'video/flv', 'video/webm'],
    doc: ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
          'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
          'text/plain', 'text/csv']
  },
  
  // 文件扩展名映射
  FILE_EXTENSIONS: {
    image: ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.svg'],
    audio: ['.mp3', '.wav', '.ogg', '.aac', '.m4a'],
    video: ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm'],
    doc: ['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.txt', '.csv']
  }
}

/**
 * 根据文件类型获取图标
 */
export const getFileTypeIcon = (type) => {
  const iconMap = {
    image: 'Picture',
    audio: 'VideoPlay',
    video: 'VideoCamera',
    doc: 'Document'
  }
  return iconMap[type] || 'Document'
}

/**
 * 根据文件类型获取颜色
 */
export const getFileTypeColor = (type) => {
  const colorMap = {
    image: '#409eff',
    audio: '#67c23a',
    video: '#e6a23c',
    doc: '#909399'
  }
  return colorMap[type] || '#909399'
}

/**
 * 验证文件类型
 */
export const validateFileType = (file) => {
  if (!file || !file.type) return false
  
  const fileType = file.type.toLowerCase()
  return Object.values(MEDIA_CONFIG.MIME_TYPES).flat().includes(fileType)
}

/**
 * 验证文件大小
 */
export const validateFileSize = (file) => {
  if (!file || !file.size) return false
  return file.size <= MEDIA_CONFIG.MAX_FILE_SIZE
}

/**
 * 根据MIME类型获取文件类别
 */
export const getFileCategory = (mimeType) => {
  if (!mimeType) return 'doc'
  
  const type = mimeType.toLowerCase()
  
  if (MEDIA_CONFIG.MIME_TYPES.image.includes(type)) return 'image'
  if (MEDIA_CONFIG.MIME_TYPES.audio.includes(type)) return 'audio'
  if (MEDIA_CONFIG.MIME_TYPES.video.includes(type)) return 'video'
  
  return 'doc'
}

/**
 * 根据文件扩展名获取文件类别
 */
export const getFileCategoryByExtension = (filename) => {
  if (!filename) return 'doc'
  
  const ext = filename.toLowerCase().substring(filename.lastIndexOf('.'))
  
  for (const [category, extensions] of Object.entries(MEDIA_CONFIG.FILE_EXTENSIONS)) {
    if (extensions.includes(ext)) {
      return category
    }
  }
  
  return 'doc'
}

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  
  return `${(bytes / Math.pow(1024, i)).toFixed(i === 0 ? 0 : 1)} ${sizes[i]}`
}

/**
 * 格式化最大文件大小提示
 */
export const getMaxFileSizeText = () => {
  return formatFileSize(MEDIA_CONFIG.MAX_FILE_SIZE)
}

/**
 * 获取支持的文件类型提示文本
 */
export const getSupportedTypesText = () => {
  const typeLabels = {
    image: '图片',
    audio: '音频',
    video: '视频',
    doc: '文档'
  }
  
  return MEDIA_CONFIG.ALLOWED_TYPES.map(type => typeLabels[type] || type).join('、')
}

/**
 * 构建完整的媒体URL
 */
export const buildMediaUrl = (path, filename) => {
  if (!filename) return null
  
  const baseUrl = import.meta.env.VITE_MEDIA_BASE_URL || ''
  return `${baseUrl}${path}/${filename}`
}

export default MEDIA_CONFIG

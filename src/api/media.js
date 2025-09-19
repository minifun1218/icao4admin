import api from './index'

// 媒体资源管理相关API
export const mediaApi = {
  // 获取媒体文件列表
  getMediaFiles(params = {}) {
    return api.get('/media', { params })
  },

  // 获取媒体文件详情
  getMediaById(id) {
    return api.get(`/media/${id}`)
  },

  // 上传媒体文件
  uploadMedia(formData, config = {}) {
    return api.post('/media/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      ...config,
      // 上传进度回调
      onUploadProgress: config.onUploadProgress
    })
  },

  // 批量上传媒体文件
  batchUploadMedia(files, config = {}) {
    const formData = new FormData()
    files.forEach((file, index) => {
      formData.append(`files[${index}]`, file)
    })
    
    return api.post('/media/batch-upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      ...config
    })
  },

  // 更新媒体文件信息
  updateMedia(id, data) {
    return api.put(`/media/${id}`, data)
  },

  // 删除媒体文件
  deleteMedia(id) {
    return api.delete(`/media/${id}`)
  },

  // 批量删除媒体文件
  batchDeleteMedia(mediaIds) {
    return api.delete('/media/batch', { data: { mediaIds } })
  },

  // 获取媒体文件URL
  getMediaUrl(id) {
    return `${api.defaults.baseURL}/media/files/${id}`
  },

  // 下载媒体文件
  downloadMedia(id, filename) {
    return api.get(`/media/${id}/download`, {
      responseType: 'blob',
      headers: {
        'Accept': 'application/octet-stream'
      }
    }).then(response => {
      // 创建下载链接
      const url = window.URL.createObjectURL(response)
      const link = document.createElement('a')
      link.href = url
      link.download = filename || `media_${id}`
      link.click()
      window.URL.revokeObjectURL(url)
    })
  },

  // 获取媒体分类
  getMediaCategories() {
    return [
      { id: 'AUDIO', name: '音频文件', extensions: ['.mp3', '.wav', '.m4a', '.ogg'] },
      { id: 'IMAGE', name: '图片文件', extensions: ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'] },
      { id: 'VIDEO', name: '视频文件', extensions: ['.mp4', '.avi', '.mov', '.wmv', '.flv'] },
      { id: 'DOCUMENT', name: '文档文件', extensions: ['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx'] },
      { id: 'OTHER', name: '其他文件', extensions: [] }
    ]
  },

  // 获取文件类型
  getFileType(filename) {
    if (!filename) return 'OTHER'
    
    const ext = filename.toLowerCase().substr(filename.lastIndexOf('.'))
    const categories = this.getMediaCategories()
    
    for (const category of categories) {
      if (category.extensions.includes(ext)) {
        return category.id
      }
    }
    
    return 'OTHER'
  },

  // 格式化文件大小
  formatFileSize(bytes) {
    if (!bytes || bytes === 0) return '0 B'
    
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  },

  // 验证文件类型
  validateFileType(file, allowedTypes = []) {
    if (allowedTypes.length === 0) return true
    
    const fileType = this.getFileType(file.name)
    return allowedTypes.includes(fileType)
  },

  // 验证文件大小
  validateFileSize(file, maxSize = 50 * 1024 * 1024) { // 默认50MB
    return file.size <= maxSize
  }
}

export default mediaApi

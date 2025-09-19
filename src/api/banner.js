import api from './index'

// 轮播图管理相关API
export const bannerApi = {
  // ==================== 轮播图组管理 ====================
  
  // 获取轮播图组列表
  getBannerGroups(params = {}) {
    return api.get('/banners/groups', { params })
  },

  // 根据ID获取轮播图组详情
  getBannerGroupById(id) {
    return api.get(`/banners/groups/${id}`)
  },

  // 创建轮播图组
  createBannerGroup(data) {
    return api.post('/banners/groups', data)
  },

  // 更新轮播图组
  updateBannerGroup(id, data) {
    return api.put(`/banners/groups/${id}`, data)
  },

  // 删除轮播图组
  deleteBannerGroup(id) {
    return api.delete(`/banners/groups/${id}`)
  },

  // 批量删除轮播图组
  batchDeleteBannerGroups(groupIds) {
    return api.delete('/banners/groups/batch', { data: { groupIds } })
  },

  // 更新轮播图组状态
  updateBannerGroupStatus(id, isActive) {
    return api.put(`/banners/groups/${id}/status`, { isActive })
  },

  // 更新轮播图组排序
  updateBannerGroupOrder(id, displayOrder) {
    return api.put(`/banners/groups/${id}/order`, { displayOrder })
  },

  // ==================== 轮播图项目管理 ====================
  
  // 获取轮播图项目列表
  getBannerItems(params = {}) {
    return api.get('/banners/items', { params })
  },

  // 根据轮播图组ID获取项目列表
  getBannerItemsByGroupId(groupId, params = {}) {
    return api.get(`/banners/groups/${groupId}/items`, { params })
  },

  // 根据ID获取轮播图项目详情
  getBannerItemById(id) {
    return api.get(`/banners/items/${id}`)
  },

  // 创建轮播图项目
  createBannerItem(data) {
    return api.post('/banners/items', data)
  },

  // 更新轮播图项目
  updateBannerItem(id, data) {
    return api.put(`/banners/items/${id}`, data)
  },

  // 删除轮播图项目
  deleteBannerItem(id) {
    return api.delete(`/banners/items/${id}`)
  },

  // 批量删除轮播图项目
  batchDeleteBannerItems(itemIds) {
    return api.delete('/banners/items/batch', { data: { itemIds } })
  },

  // 更新轮播图项目状态
  updateBannerItemStatus(id, isActive) {
    return api.put(`/banners/items/${id}/status`, { isActive })
  },

  // 更新轮播图项目排序
  updateBannerItemOrder(id, displayOrder) {
    return api.put(`/banners/items/${id}/order`, { displayOrder })
  },

  // 批量更新轮播图项目排序
  batchUpdateItemOrder(orderData) {
    return api.put('/banners/items/batch-order', { orderData })
  },

  // ==================== 文件上传 ====================
  
  // 上传轮播图图片
  uploadBannerImage(file, groupId = null) {
    const formData = new FormData()
    formData.append('file', file)
    if (groupId) {
      formData.append('groupId', groupId)
    }
    return api.post('/banners/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 删除轮播图图片
  deleteBannerImage(imageUrl) {
    return api.delete('/banners/upload/image', { data: { imageUrl } })
  },

  // ==================== 数据导入导出 ====================
  
  // 导出轮播图数据
  exportBanners(params = {}) {
    return api.get('/banners/export', {
      params,
      responseType: 'blob'
    })
  },

  // 导入轮播图数据
  importBanners(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/banners/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // ==================== 统计分析 ====================
  
  // 获取轮播图统计信息
  getBannerStats() {
    return api.get('/banners/stats')
  },

  // 获取轮播图点击统计
  getBannerClickStats(params = {}) {
    return api.get('/banners/stats/clicks', { params })
  },

  // 获取轮播图展示统计
  getBannerViewStats(params = {}) {
    return api.get('/banners/stats/views', { params })
  },

  // ==================== 预览和发布 ====================
  
  // 预览轮播图组
  previewBannerGroup(id) {
    return api.get(`/banners/groups/${id}/preview`)
  },

  // 发布轮播图组
  publishBannerGroup(id) {
    return api.put(`/banners/groups/${id}/publish`)
  },

  // 取消发布轮播图组
  unpublishBannerGroup(id) {
    return api.put(`/banners/groups/${id}/unpublish`)
  },

  // ==================== 工具方法 ====================
  
  // 获取轮播图类型选项
  getBannerTypes() {
    return [
      { value: 'carousel', label: '轮播图', icon: 'Picture', color: '#409EFF' },
      { value: 'banner', label: '横幅广告', icon: 'Postcard', color: '#67C23A' },
      { value: 'popup', label: '弹窗广告', icon: 'MessageBox', color: '#E6A23C' },
      { value: 'notice', label: '通知公告', icon: 'Bell', color: '#F56C6C' }
    ]
  },

  // 获取链接类型选项
  getLinkTypes() {
    return [
      { value: 'none', label: '无链接' },
      { value: 'internal', label: '内部链接' },
      { value: 'external', label: '外部链接' },
      { value: 'download', label: '下载文件' }
    ]
  },

  // 获取显示位置选项
  getDisplayPositions() {
    return [
      { value: 'home_top', label: '首页顶部' },
      { value: 'home_middle', label: '首页中部' },
      { value: 'home_bottom', label: '首页底部' },
      { value: 'course_top', label: '课程页顶部' },
      { value: 'course_side', label: '课程页侧边' },
      { value: 'exam_top', label: '考试页顶部' }
    ]
  },

  // 获取状态选项
  getStatusOptions() {
    return [
      { value: true, label: '启用', color: 'success' },
      { value: false, label: '禁用', color: 'danger' }
    ]
  },

  // 验证图片文件
  validateImageFile(file) {
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
    const maxSize = 5 * 1024 * 1024 // 5MB

    if (!allowedTypes.includes(file.type)) {
      return { valid: false, message: '只支持 JPG、PNG、GIF、WebP 格式的图片' }
    }

    if (file.size > maxSize) {
      return { valid: false, message: '图片大小不能超过 5MB' }
    }

    return { valid: true }
  },

  // 格式化轮播图类型
  formatBannerType(type) {
    const typeMap = {
      'carousel': '轮播图',
      'banner': '横幅广告',
      'popup': '弹窗广告',
      'notice': '通知公告'
    }
    return typeMap[type] || type
  },

  // 格式化链接类型
  formatLinkType(type) {
    const typeMap = {
      'none': '无链接',
      'internal': '内部链接',
      'external': '外部链接',
      'download': '下载文件'
    }
    return typeMap[type] || type
  },

  // 格式化显示位置
  formatDisplayPosition(position) {
    const positionMap = {
      'home_top': '首页顶部',
      'home_middle': '首页中部',
      'home_bottom': '首页底部',
      'course_top': '课程页顶部',
      'course_side': '课程页侧边',
      'exam_top': '考试页顶部'
    }
    return positionMap[position] || position
  },

  // 生成缩略图URL
  generateThumbnailUrl(imageUrl, width = 200, height = 150) {
    if (!imageUrl) return ''
    return `${imageUrl}?w=${width}&h=${height}&fit=crop`
  },

  // 计算图片尺寸比例
  calculateAspectRatio(width, height) {
    if (!width || !height) return '16:9'
    const gcd = (a, b) => b === 0 ? a : gcd(b, a % b)
    const divisor = gcd(width, height)
    return `${width / divisor}:${height / divisor}`
  }
}

export default bannerApi
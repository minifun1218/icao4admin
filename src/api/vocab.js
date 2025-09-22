import api from './index'

// 航空词汇管理相关API (全面更新版本)
export const vocabApi = {
  // ==================== 基础CRUD操作 ====================
  
  // 创建词汇 (需要ADMIN权限)
  createVocab(data) {
    return api.post('/vocab', data)
  },

  // 获取词汇详情
  getVocabById(id, includeTopics = false, includeRelated = false) {
    return api.get(`/vocab/${id}`, { 
      params: { 
        includeTopics, 
        includeRelated 
      } 
    })
  },

  // 获取词汇列表（分页）
  getVocabs(params = {}) {
    return api.get('/vocab', { params })
  },

  // 更新词汇 (需要ADMIN权限)
  updateVocab(id, data) {
    return api.put(`/vocab/${id}`, data)
  },

  // 删除词汇 (需要ADMIN权限)
  deleteVocab(id) {
    return api.delete(`/vocab/${id}`)
  },

  // 软删除词汇 (需要ADMIN权限)
  softDeleteVocab(id) {
    return api.put(`/vocab/${id}/soft-delete`)
  },

  // 恢复已删除的词汇 (需要ADMIN权限)
  restoreVocab(id) {
    return api.put(`/vocab/${id}/restore`)
  },

  // ==================== 查询相关接口 ====================
  
  // 根据词条查询
  getVocabByWord(word) {
    return api.get(`/vocab/by-word/${encodeURIComponent(word)}`)
  },

  // 模糊搜索词汇
  searchVocabs(keyword, params = {}) {
    return api.get('/vocab/search', { 
      params: { keyword, ...params }
    })
  },

  // 综合搜索词汇
  comprehensiveSearch(params = {}) {
    return api.get('/vocab/comprehensive-search', { params })
  },

  // 高级搜索词汇
  advancedSearch(searchCriteria) {
    return api.post('/vocab/advanced-search', searchCriteria)
  },

  // 根据CEFR等级获取词汇
  getVocabsByCEFR(level, params = {}) {
    return api.get(`/vocab/cefr/${level}`, { params })
  },

  // 根据难度等级获取词汇
  getVocabsByDifficulty(level, params = {}) {
    return api.get(`/vocab/difficulty/${level}`, { params })
  },

  // 根据词性获取词汇
  getVocabsByPOS(pos, params = {}) {
    return api.get(`/vocab/pos/${pos}`, { params })
  },

  // 获取热门词汇
  getPopularVocabs(params = {}) {
    return api.get('/vocab/popular', { params })
  },

  // 获取最新添加的词汇
  getLatestVocabs(params = {}) {
    return api.get('/vocab/latest', { params })
  },

  // ==================== 批量操作 ====================
  
  // 批量创建词汇 (需要ADMIN权限)
  batchCreateVocabs(data) {
    return api.post('/vocab/batch', data)
  },

  // 批量更新词汇 (需要ADMIN权限)
  batchUpdateVocabs(updates) {
    return api.put('/vocab/batch', updates)
  },

  // 批量删除词汇 (需要ADMIN权限)
  batchDeleteVocabs(vocabIds) {
    return api.delete('/vocab/batch', { data: { vocabIds } })
  },

  // 批量软删除词汇 (需要ADMIN权限)
  batchSoftDeleteVocabs(vocabIds) {
    return api.put('/vocab/batch/soft-delete', { vocabIds })
  },

  // 批量恢复词汇 (需要ADMIN权限)
  batchRestoreVocabs(vocabIds) {
    return api.put('/vocab/batch/restore', { vocabIds })
  },

  // 批量更新词汇状态 (需要ADMIN权限)
  batchUpdateVocabStatus(vocabIds, isEnabled) {
    return api.put('/vocab/batch/status', { vocabIds, isEnabled })
  },

  // 批量设置词汇标签 (需要ADMIN权限)
  batchSetVocabTags(vocabIds, tags) {
    return api.put('/vocab/batch/tags', { vocabIds, tags })
  },

  // ==================== 统计信息 ====================
  
  // 获取词汇统计信息
  getVocabStatistics() {
    return api.get('/vocab/statistics')
  },

  // 获取词汇统计详情
  getVocabStatisticsDetail(params = {}) {
    return api.get('/vocab/statistics/detail', { params })
  },

  // 获取用户词汇学习统计
  getUserVocabStats(userId, params = {}) {
    return api.get(`/vocab/user-stats/${userId}`, { params })
  },

  // 获取词汇使用频率统计
  getVocabUsageStats(params = {}) {
    return api.get('/vocab/usage-stats', { params })
  },

  // ==================== 文件上传 ====================
  
  // 上传词汇音频文件
  uploadVocabAudio(file, vocabId = null, options = {}) {
    const formData = new FormData()
    formData.append('file', file)
    if (vocabId) {
      formData.append('vocabId', vocabId)
    }
    if (options.quality) {
      formData.append('quality', options.quality)
    }
    if (options.format) {
      formData.append('format', options.format)
    }
    return api.post('/vocab/upload/audio', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 30000 // 30秒超时
    })
  },

  // 删除词汇音频文件
  deleteVocabAudio(audioUrl) {
    return api.delete('/vocab/upload/audio', { data: { audioUrl } })
  },

  // 批量上传音频文件
  batchUploadVocabAudio(files, vocabIds = []) {
    const formData = new FormData()
    files.forEach((file, index) => {
      formData.append(`files`, file)
      if (vocabIds[index]) {
        formData.append(`vocabIds`, vocabIds[index])
      }
    })
    return api.post('/vocab/upload/audio/batch', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000 // 60秒超时
    })
  },

  // 上传词汇图片文件
  uploadVocabImage(file, vocabId = null, options = {}) {
    const formData = new FormData()
    formData.append('file', file)
    if (vocabId) {
      formData.append('vocabId', vocabId)
    }
    if (options.width) {
      formData.append('width', options.width)
    }
    if (options.height) {
      formData.append('height', options.height)
    }
    if (options.quality) {
      formData.append('quality', options.quality)
    }
    return api.post('/vocab/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 30000
    })
  },

  // 删除词汇图片文件
  deleteVocabImage(imageUrl) {
    return api.delete('/vocab/upload/image', { data: { imageUrl } })
  },

  // 批量上传图片文件
  batchUploadVocabImage(files, vocabIds = []) {
    const formData = new FormData()
    files.forEach((file, index) => {
      formData.append(`files`, file)
      if (vocabIds[index]) {
        formData.append(`vocabIds`, vocabIds[index])
      }
    })
    return api.post('/vocab/upload/image/batch', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    })
  },

  // ==================== 数据导入导出 ====================

  // 导入词汇数据
  importVocabs(file, options = {}) {
    const formData = new FormData()
    formData.append('file', file)
    if (options.skipDuplicates !== undefined) {
      formData.append('skipDuplicates', options.skipDuplicates)
    }
    if (options.updateExisting !== undefined) {
      formData.append('updateExisting', options.updateExisting)
    }
    if (options.validateData !== undefined) {
      formData.append('validateData', options.validateData)
    }
    return api.post('/vocab/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000 // 2分钟超时
    })
  },

  // 导出词汇数据
  exportVocabs(params = {}) {
    return api.get('/vocab/export', {
      params,
      responseType: 'blob',
      timeout: 60000
    })
  },

  // 导出词汇模板
  exportVocabTemplate() {
    return api.get('/vocab/export/template', {
      responseType: 'blob'
    })
  },

  // 验证导入文件
  validateImportFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/vocab/import/validate', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 获取导入历史记录
  getImportHistory(params = {}) {
    return api.get('/vocab/import/history', { params })
  },

  // ==================== 标签管理 ====================

  // 获取所有标签
  getAllTags() {
    return api.get('/vocab/tags')
  },

  // 创建标签
  createTag(tagData) {
    return api.post('/vocab/tags', tagData)
  },

  // 更新标签
  updateTag(tagId, tagData) {
    return api.put(`/vocab/tags/${tagId}`, tagData)
  },

  // 删除标签
  deleteTag(tagId) {
    return api.delete(`/vocab/tags/${tagId}`)
  },

  // 获取热门标签
  getPopularTags(limit = 20) {
    return api.get('/vocab/tags/popular', { params: { limit } })
  },

  // ==================== 词汇关系管理 ====================

  // 获取词汇的同义词
  getVocabSynonyms(vocabId) {
    return api.get(`/vocab/${vocabId}/synonyms`)
  },

  // 添加同义词关系
  addSynonymRelation(vocabId, synonymId) {
    return api.post(`/vocab/${vocabId}/synonyms`, { synonymId })
  },

  // 删除同义词关系
  removeSynonymRelation(vocabId, synonymId) {
    return api.delete(`/vocab/${vocabId}/synonyms/${synonymId}`)
  },

  // 获取词汇的反义词
  getVocabAntonyms(vocabId) {
    return api.get(`/vocab/${vocabId}/antonyms`)
  },

  // 添加反义词关系
  addAntonymRelation(vocabId, antonymId) {
    return api.post(`/vocab/${vocabId}/antonyms`, { antonymId })
  },

  // 删除反义词关系
  removeAntonymRelation(vocabId, antonymId) {
    return api.delete(`/vocab/${vocabId}/antonyms/${antonymId}`)
  },

  // 获取相关词汇
  getRelatedVocabs(vocabId, params = {}) {
    return api.get(`/vocab/${vocabId}/related`, { params })
  },

  // ==================== 学习记录管理 ====================

  // 记录词汇学习
  recordVocabLearning(vocabId, learningData) {
    return api.post(`/vocab/${vocabId}/learn`, learningData)
  },

  // 获取用户学习记录
  getUserLearningRecords(userId, params = {}) {
    return api.get(`/vocab/learning-records/${userId}`, { params })
  },

  // 获取词汇学习进度
  getVocabLearningProgress(vocabId, userId) {
    return api.get(`/vocab/${vocabId}/progress/${userId}`)
  },

  // 标记词汇为已掌握
  markVocabAsMastered(vocabId, userId) {
    return api.post(`/vocab/${vocabId}/master`, { userId })
  },

  // 标记词汇为收藏
  favoriteVocab(vocabId, userId) {
    return api.post(`/vocab/${vocabId}/favorite`, { userId })
  },

  // 取消收藏词汇
  unfavoriteVocab(vocabId, userId) {
    return api.delete(`/vocab/${vocabId}/favorite/${userId}`)
  },

  // 获取用户收藏的词汇
  getUserFavoriteVocabs(userId, params = {}) {
    return api.get(`/vocab/favorites/${userId}`, { params })
  },

  // ==================== 缓存管理 ====================

  // 清除词汇缓存
  clearVocabCache() {
    return api.delete('/vocab/cache')
  },

  // 预热词汇缓存
  warmupVocabCache() {
    return api.post('/vocab/cache/warmup')
  },

  // ==================== 工具方法 ====================
  
  // 验证音频文件
  validateAudioFile(file) {
    const allowedTypes = [
      'audio/mpeg', 
      'audio/mp3', 
      'audio/wav', 
      'audio/ogg', 
      'audio/m4a',
      'audio/aac',
      'audio/flac'
    ]
    const maxSize = 10 * 1024 * 1024 // 10MB

    if (!file) {
      return { valid: false, message: '请选择音频文件' }
    }

    if (!allowedTypes.includes(file.type)) {
      return { 
        valid: false, 
        message: '只支持 MP3、WAV、OGG、M4A、AAC、FLAC 格式的音频文件' 
      }
    }

    if (file.size > maxSize) {
      return { 
        valid: false, 
        message: '音频文件大小不能超过 10MB' 
      }
    }

    return { valid: true }
  },

  // 验证图片文件
  validateImageFile(file) {
    const allowedTypes = [
      'image/jpeg', 
      'image/jpg', 
      'image/png', 
      'image/gif', 
      'image/webp',
      'image/svg+xml'
    ]
    const maxSize = 5 * 1024 * 1024 // 5MB

    if (!file) {
      return { valid: false, message: '请选择图片文件' }
    }

    if (!allowedTypes.includes(file.type)) {
      return { 
        valid: false, 
        message: '只支持 JPG、PNG、GIF、WebP、SVG 格式的图片文件' 
      }
    }

    if (file.size > maxSize) {
      return { 
        valid: false, 
        message: '图片文件大小不能超过 5MB' 
      }
    }

    return { valid: true }
  },

  // 验证导入文件
  validateImportFileFormat(file) {
    const allowedTypes = [
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
      'application/vnd.ms-excel', // .xls
      'text/csv', // .csv
      'application/json' // .json
    ]
    const maxSize = 50 * 1024 * 1024 // 50MB

    if (!file) {
      return { valid: false, message: '请选择要导入的文件' }
    }

    if (!allowedTypes.includes(file.type)) {
      return { 
        valid: false, 
        message: '只支持 Excel (.xlsx, .xls)、CSV 和 JSON 格式的文件' 
      }
    }

    if (file.size > maxSize) {
      return { 
        valid: false, 
        message: '导入文件大小不能超过 50MB' 
      }
    }

    return { valid: true }
  },

  // 获取CEFR等级选项
  getCEFRLevels() {
    return [
      { value: 'A1', label: 'A1 - 入门级', color: '#67C23A', description: '基础词汇，日常用语' },
      { value: 'A2', label: 'A2 - 基础级', color: '#E6A23C', description: '常用词汇，简单交流' },
      { value: 'B1', label: 'B1 - 进阶级', color: '#409EFF', description: '中级词汇，工作学习' },
      { value: 'B2', label: 'B2 - 中高级', color: '#909399', description: '复杂词汇，专业领域' },
      { value: 'C1', label: 'C1 - 高级', color: '#F56C6C', description: '高级词汇，学术写作' },
      { value: 'C2', label: 'C2 - 精通级', color: '#303133', description: '母语级别，专业术语' }
    ]
  },

  // 获取词性选项
  getPOSOptions() {
    return [
      { value: 'noun', label: '名词 (n.)', color: '#409EFF', icon: 'Document' },
      { value: 'verb', label: '动词 (v.)', color: '#67C23A', icon: 'VideoPlay' },
      { value: 'adjective', label: '形容词 (adj.)', color: '#E6A23C', icon: 'Star' },
      { value: 'adverb', label: '副词 (adv.)', color: '#F56C6C', icon: 'Lightning' },
      { value: 'pronoun', label: '代词 (pron.)', color: '#909399', icon: 'User' },
      { value: 'preposition', label: '介词 (prep.)', color: '#606266', icon: 'Connection' },
      { value: 'conjunction', label: '连词 (conj.)', color: '#303133', icon: 'Link' },
      { value: 'interjection', label: '感叹词 (int.)', color: '#E6A23C', icon: 'ChatDotRound' },
      { value: 'article', label: '冠词 (art.)', color: '#409EFF', icon: 'Flag' },
      { value: 'phrase', label: '短语 (phr.)', color: '#67C23A', icon: 'Collection' }
    ]
  },

  // 获取频率等级选项
  getFrequencyLevels() {
    return [
      { value: 1, label: '极高频', color: '#67C23A', description: '最常用的1000个词' },
      { value: 2, label: '高频', color: '#E6A23C', description: '常用的2000-5000个词' },
      { value: 3, label: '中频', color: '#409EFF', description: '中等使用频率的词汇' },
      { value: 4, label: '低频', color: '#909399', description: '较少使用的词汇' },
      { value: 5, label: '罕见', color: '#F56C6C', description: '专业或罕见词汇' }
    ]
  },

  // 获取难度等级选项
  getDifficultyLevels() {
    return [
      { value: 1, label: '非常简单', color: '#67C23A', description: '小学水平' },
      { value: 2, label: '简单', color: '#95D475', description: '初中水平' },
      { value: 3, label: '一般', color: '#E6A23C', description: '高中水平' },
      { value: 4, label: '中等', color: '#409EFF', description: '大学水平' },
      { value: 5, label: '困难', color: '#F78989', description: '专业水平' },
      { value: 6, label: '很困难', color: '#F56C6C', description: '学术水平' },
      { value: 7, label: '极困难', color: '#C45656', description: '专家水平' }
    ]
  },

  // 获取学习状态选项
  getLearningStatusOptions() {
    return [
      { value: 'new', label: '未学习', color: '#909399' },
      { value: 'learning', label: '学习中', color: '#E6A23C' },
      { value: 'reviewing', label: '复习中', color: '#409EFF' },
      { value: 'mastered', label: '已掌握', color: '#67C23A' },
      { value: 'forgotten', label: '已遗忘', color: '#F56C6C' }
    ]
  },

  // 获取音频质量选项
  getAudioQualityOptions() {
    return [
      { value: 'low', label: '低质量 (64kbps)', size: '小文件' },
      { value: 'medium', label: '中等质量 (128kbps)', size: '平衡' },
      { value: 'high', label: '高质量 (256kbps)', size: '大文件' },
      { value: 'lossless', label: '无损质量', size: '最大文件' }
    ]
  },

  // 格式化文件大小
  formatFileSize(bytes) {
    if (!bytes) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  },

  // 格式化CEFR等级
  formatCEFRLevel(level) {
    const levels = this.getCEFRLevels()
    const found = levels.find(l => l.value === level)
    return found ? found.label : level
  },

  // 格式化词性
  formatPOS(pos) {
    const posOptions = this.getPOSOptions()
    const found = posOptions.find(p => p.value === pos)
    return found ? found.label : pos
  },

  // 格式化难度等级
  formatDifficultyLevel(level) {
    const levels = this.getDifficultyLevels()
    const found = levels.find(l => l.value === level)
    return found ? found.label : `${level}级`
  },

  // 格式化频率等级
  formatFrequencyLevel(level) {
    const levels = this.getFrequencyLevels()
    const found = levels.find(l => l.value === level)
    return found ? found.label : `${level}级`
  },

  // 生成词汇URL
  generateVocabUrl(vocabId) {
    return `/vocab/${vocabId}`
  },

  // 生成音频URL
  generateAudioUrl(audioPath, quality = 'medium') {
    if (!audioPath) return ''
    return `${audioPath}?quality=${quality}`
  },

  // 生成图片缩略图URL
  generateThumbnailUrl(imagePath, width = 200, height = 150) {
    if (!imagePath) return ''
    return `${imagePath}?w=${width}&h=${height}&fit=crop`
  },

  // 检查词汇是否存在
  async checkVocabExists(headword) {
    try {
      const response = await this.getVocabByWord(headword)
      return { exists: true, vocab: response }
    } catch (error) {
      if (error.response?.status === 404) {
        return { exists: false }
      }
      throw error
    }
  },

  // 获取推荐学习词汇
  getRecommendedVocabs(userId, params = {}) {
    return api.get(`/vocab/recommend/${userId}`, { params })
  },

  // 获取词汇学习计划
  getVocabLearningPlan(userId, params = {}) {
    return api.get(`/vocab/learning-plan/${userId}`, { params })
  },

  // 创建词汇学习计划
  createVocabLearningPlan(userId, planData) {
    return api.post(`/vocab/learning-plan/${userId}`, planData)
  },

  // 更新词汇学习计划
  updateVocabLearningPlan(userId, planId, planData) {
    return api.put(`/vocab/learning-plan/${userId}/${planId}`, planData)
  }
}

export default vocabApi
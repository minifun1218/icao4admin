import api from './index'

// 航空词汇管理相关API (根据API文档更新)
export const vocabApi = {
  // ==================== 基础CRUD操作 ====================
  
  // 创建词汇 (需要ADMIN权限)
  createVocab(data) {
    return api.post('/vocab', data)
  },

  // 获取词汇详情
  getVocabById(id, includeTopics = false) {
    return api.get(`/vocab/${id}`, { params: { includeTopics } })
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

  // ==================== 批量操作 ====================
  
  // 批量创建词汇 (需要ADMIN权限)
  batchCreateVocabs(data) {
    return api.post('/vocab/batch', data)
  },

  // 批量删除词汇 (需要ADMIN权限)
  batchDeleteVocabs(vocabIds) {
    return api.delete('/vocab/batch', { data: vocabIds })
  },

  // ==================== 统计信息 ====================
  
  // 获取词汇统计信息
  getVocabStatistics() {
    return api.get('/vocab/statistics')
  },

  // ==================== 工具方法 ====================
  
  // 获取CEFR等级选项
  getCEFRLevels() {
    return ['A1', 'A2', 'B1', 'B2', 'C1', 'C2']
  },

  // 获取词性选项
  getPOSOptions() {
    return [
      'noun', 'verb', 'adjective', 'adverb', 
      'pronoun', 'preposition', 'conjunction', 
      'interjection', 'article', 'phrase'
    ]
  },

  // 获取频率等级选项
  getFrequencyLevels() {
    return [
      { value: 1, label: '高频' },
      { value: 2, label: '中频' },
      { value: 3, label: '低频' }
    ]
  },

  // 获取难度等级选项
  getDifficultyLevels() {
    return [
      { value: 1, label: '非常简单' },
      { value: 2, label: '简单' },
      { value: 3, label: '中等' },
      { value: 4, label: '困难' },
      { value: 5, label: '非常困难' }
    ]
  }
}

export default vocabApi

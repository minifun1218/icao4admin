import api from './index'

// 试卷管理相关API
export const paperApi = {
  // ==================== 试卷基础管理 ====================
  
  // 获取试卷列表
  getPapers(params = {}) {
    return api.get('/app/paper', { params })
  },

  // 根据ID获取试卷详情
  getPaperById(id) {
    return api.get(`/app/paper/${id}`)
  },

  // 创建试卷
  createPaper(data) {
    return api.post('/app/paper', data)
  },

  // 更新试卷
  updatePaper(id, data) {
    return api.put(`/app/paper/${id}`, data)
  },

  // 删除试卷
  deletePaper(id) {
    return api.delete(`/app/paper/${id}`)
  },

  // 批量删除试卷
  batchDeletePapers(paperIds) {
    return api.delete('/app/paper/batch', { data: { paperIds } })
  },

  // 复制试卷
  copyPaper(id) {
    return api.post(`/app/paper/${id}/copy`)
  },

  // ==================== 试卷状态管理 ====================
  
  // 发布试卷
  publishPaper(id) {
    return api.put(`/app/paper/${id}/publish`)
  },

  // 取消发布试卷
  unpublishPaper(id) {
    return api.put(`/app/paper/${id}/unpublish`)
  },

  // 启用试卷
  enablePaper(id) {
    return api.put(`/app/paper/${id}/enable`)
  },

  // 禁用试卷
  disablePaper(id) {
    return api.put(`/app/paper/${id}/disable`)
  },

  // 归档试卷
  archivePaper(id) {
    return api.put(`/app/paper/${id}/archive`)
  },

  // 恢复试卷
  restorePaper(id) {
    return api.put(`/app/paper/${id}/restore`)
  },

  // ==================== 试卷题目管理 ====================
  
  // 获取试卷题目列表
  getPaperQuestions(paperId, params = {}) {
    return api.get(`/app/paper/${paperId}/questions`, { params })
  },

  // 添加题目到试卷
  addQuestionToPaper(paperId, questionData) {
    return api.post(`/app/paper/${paperId}/questions`, questionData)
  },

  // 批量添加题目到试卷
  batchAddQuestionsToPaper(paperId, questionIds) {
    return api.post(`/app/paper/${paperId}/questions/batch`, { questionIds })
  },

  // 从试卷移除题目
  removeQuestionFromPaper(paperId, questionId) {
    return api.delete(`/app/paper/${paperId}/questions/${questionId}`)
  },

  // 批量移除题目
  batchRemoveQuestionsFromPaper(paperId, questionIds) {
    return api.delete(`/app/paper/${paperId}/questions/batch`, { data: { questionIds } })
  },

  // 更新试卷中题目的顺序和分值
  updatePaperQuestion(paperId, questionId, data) {
    return api.put(`/app/paper/${paperId}/questions/${questionId}`, data)
  },

  // 批量更新题目顺序
  batchUpdateQuestionOrder(paperId, questionOrders) {
    return api.put(`/app/paper/${paperId}/questions/order`, { questionOrders })
  },

  // 自动组卷（根据条件自动选择题目）
  autoGeneratePaper(paperId, criteria) {
    return api.post(`/app/paper/${paperId}/auto-generate`, criteria)
  },

  // ==================== 试卷模板管理 ====================
  
  // 获取试卷模板列表
  getPaperTemplates(params = {}) {
    return api.get('/app/paper/templates', { params })
  },

  // 创建试卷模板
  createPaperTemplate(data) {
    return api.post('/app/paper/templates', data)
  },

  // 从模板创建试卷
  createPaperFromTemplate(templateId, data) {
    return api.post(`/app/paper/templates/${templateId}/create-paper`, data)
  },

  // 保存为模板
  savePaperAsTemplate(paperId, templateData) {
    return api.post(`/app/paper/${paperId}/save-as-template`, templateData)
  },

  // ==================== 试卷预览和验证 ====================
  
  // 预览试卷
  previewPaper(id) {
    return api.get(`/app/paper/${id}/preview`)
  },

  // 验证试卷
  validatePaper(id) {
    return api.get(`/app/paper/${id}/validate`)
  },

  // 获取试卷统计信息
  getPaperStatistics(id) {
    return api.get(`/app/paper/${id}/statistics`)
  },

  // ==================== 试卷分类管理 ====================
  
  // 获取试卷分类
  getPaperCategories() {
    return api.get('/app/paper/categories')
  },

  // 创建试卷分类
  createPaperCategory(data) {
    return api.post('/app/paper/categories', data)
  },

  // 更新试卷分类
  updatePaperCategory(id, data) {
    return api.put(`/app/paper/categories/${id}`, data)
  },

  // 删除试卷分类
  deletePaperCategory(id) {
    return api.delete(`/app/paper/categories/${id}`)
  },

  // 根据分类获取试卷
  getPapersByCategory(categoryId, params = {}) {
    return api.get(`/app/paper/categories/${categoryId}/papers`, { params })
  },

  // ==================== 试卷搜索和筛选 ====================
  
  // 搜索试卷
  searchPapers(params = {}) {
    return api.get('/app/paper/search', { params })
  },

  // 高级搜索
  advancedSearchPapers(criteria) {
    return api.post('/app/paper/search/advanced', criteria)
  },

  // 根据标签搜索
  searchPapersByTags(tags, params = {}) {
    return api.get('/app/paper/search/tags', { 
      params: { tags: tags.join(','), ...params }
    })
  },

  // 获取热门试卷
  getPopularPapers(params = {}) {
    return api.get('/app/paper/popular', { params })
  },

  // 获取最新试卷
  getLatestPapers(params = {}) {
    return api.get('/app/paper/latest', { params })
  },

  // ==================== 试卷版本管理 ====================
  
  // 获取试卷版本历史
  getPaperVersions(paperId) {
    return api.get(`/app/paper/${paperId}/versions`)
  },

  // 创建试卷版本
  createPaperVersion(paperId, versionData) {
    return api.post(`/app/paper/${paperId}/versions`, versionData)
  },

  // 恢复到指定版本
  restorePaperVersion(paperId, versionId) {
    return api.post(`/app/paper/${paperId}/versions/${versionId}/restore`)
  },

  // 比较版本差异
  comparePaperVersions(paperId, version1, version2) {
    return api.get(`/app/paper/${paperId}/versions/compare`, {
      params: { v1: version1, v2: version2 }
    })
  },

  // ==================== 试卷导入导出 ====================
  
  // 导出试卷
  exportPaper(id, format = 'pdf') {
    return api.get(`/app/paper/${id}/export`, {
      params: { format },
      responseType: 'blob'
    })
  },

  // 批量导出试卷
  batchExportPapers(paperIds, format = 'pdf') {
    return api.post('/app/paper/export/batch', {
      paperIds,
      format
    }, {
      responseType: 'blob'
    })
  },

  // 导入试卷
  importPaper(file, options = {}) {
    const formData = new FormData()
    formData.append('file', file)
    Object.keys(options).forEach(key => {
      formData.append(key, options[key])
    })
    return api.post('/app/paper/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 导出试卷模板
  exportPaperTemplate(format = 'excel') {
    return api.get('/app/paper/export/template', {
      params: { format },
      responseType: 'blob'
    })
  },

  // ==================== 试卷权限管理 ====================
  
  // 获取试卷权限
  getPaperPermissions(paperId) {
    return api.get(`/app/paper/${paperId}/permissions`)
  },

  // 设置试卷权限
  setPaperPermissions(paperId, permissions) {
    return api.put(`/app/paper/${paperId}/permissions`, { permissions })
  },

  // 分享试卷
  sharePaper(paperId, shareData) {
    return api.post(`/app/paper/${paperId}/share`, shareData)
  },

  // 取消分享
  unsharePaper(paperId, shareId) {
    return api.delete(`/app/paper/${paperId}/share/${shareId}`)
  },

  // ==================== 试卷标签管理 ====================
  
  // 获取所有标签
  getAllPaperTags() {
    return api.get('/app/paper/tags')
  },

  // 创建标签
  createPaperTag(data) {
    return api.post('/app/paper/tags', data)
  },

  // 更新标签
  updatePaperTag(id, data) {
    return api.put(`/app/paper/tags/${id}`, data)
  },

  // 删除标签
  deletePaperTag(id) {
    return api.delete(`/app/paper/tags/${id}`)
  },

  // 为试卷添加标签
  addTagToPaper(paperId, tagIds) {
    return api.post(`/app/paper/${paperId}/tags`, { tagIds })
  },

  // 从试卷移除标签
  removeTagFromPaper(paperId, tagId) {
    return api.delete(`/app/paper/${paperId}/tags/${tagId}`)
  },

  // ==================== 试卷使用统计 ====================
  
  // 获取试卷使用统计
  getPaperUsageStats(paperId) {
    return api.get(`/app/paper/${paperId}/usage-stats`)
  },

  // 获取试卷热度排行
  getPaperHotRanking(params = {}) {
    return api.get('/app/paper/hot-ranking', { params })
  },

  // 记录试卷访问
  recordPaperAccess(paperId) {
    return api.post(`/app/paper/${paperId}/access`)
  },

  // ==================== 工具方法 ====================
  
  // 获取试卷类型选项
  getPaperTypeOptions() {
    return [
      { value: 'practice', label: '练习试卷', icon: 'Edit', color: '#409EFF' },
      { value: 'mock', label: '模拟考试', icon: 'Clock', color: '#67C23A' },
      { value: 'formal', label: '正式考试', icon: 'Medal', color: '#E6A23C' },
      { value: 'placement', label: '分级考试', icon: 'TrendCharts', color: '#F56C6C' },
      { value: 'diagnostic', label: '诊断测试', icon: 'Monitor', color: '#909399' }
    ]
  },

  // 获取试卷状态选项
  getPaperStatusOptions() {
    return [
      { value: 'draft', label: '草稿', color: '#909399' },
      { value: 'published', label: '已发布', color: '#67C23A' },
      { value: 'archived', label: '已归档', color: '#606266' },
      { value: 'disabled', label: '已禁用', color: '#F56C6C' }
    ]
  },

  // 获取难度等级选项
  getDifficultyLevelOptions() {
    return [
      { value: 1, label: '入门', color: '#67C23A' },
      { value: 2, label: '基础', color: '#95D475' },
      { value: 3, label: '中级', color: '#E6A23C' },
      { value: 4, label: '中高级', color: '#F78989' },
      { value: 5, label: '高级', color: '#F56C6C' },
      { value: 6, label: '专家级', color: '#909399' }
    ]
  },

  // 获取题型选项
  getQuestionTypeOptions() {
    return [
      { value: 'listening_comprehension', label: '听力理解' },
      { value: 'listening_mcq', label: '听力选择题' },
      { value: 'story_retell', label: '故事复述' },
      { value: 'atc_simulation', label: '模拟通话' },
      { value: 'opi', label: '口语能力面试' }
    ]
  },

  // 验证试卷数据
  validatePaperData(paperData) {
    const errors = []
    
    if (!paperData.title || paperData.title.trim().length === 0) {
      errors.push('试卷标题不能为空')
    }
    
    if (!paperData.type) {
      errors.push('请选择试卷类型')
    }
    
    if (!paperData.duration || paperData.duration <= 0) {
      errors.push('考试时长必须大于0')
    }
    
    if (!paperData.questions || paperData.questions.length === 0) {
      errors.push('试卷必须包含至少一道题目')
    }
    
    return {
      isValid: errors.length === 0,
      errors
    }
  },

  // 格式化试卷类型
  formatPaperType(type) {
    const typeMap = {
      'practice': '练习试卷',
      'mock': '模拟考试',
      'formal': '正式考试',
      'placement': '分级考试',
      'diagnostic': '诊断测试'
    }
    return typeMap[type] || type
  },

  // 格式化试卷状态
  formatPaperStatus(status) {
    const statusMap = {
      'draft': '草稿',
      'published': '已发布',
      'archived': '已归档',
      'disabled': '已禁用'
    }
    return statusMap[status] || status
  },

  // 格式化难度等级
  formatDifficultyLevel(level) {
    const levelMap = {
      1: '入门',
      2: '基础',
      3: '中级',
      4: '中高级',
      5: '高级',
      6: '专家级'
    }
    return levelMap[level] || `等级${level}`
  },

  // 计算试卷总分
  calculateTotalScore(questions) {
    if (!questions || questions.length === 0) return 0
    return questions.reduce((total, question) => total + (question.score || 0), 0)
  },

  // 计算试卷预计用时
  calculateEstimatedTime(questions) {
    if (!questions || questions.length === 0) return 0
    return questions.reduce((total, question) => total + (question.estimatedTime || 60), 0)
  },

  // 生成试卷摘要
  generatePaperSummary(paper) {
    const questionCount = paper.questions?.length || 0
    const totalScore = this.calculateTotalScore(paper.questions)
    const estimatedTime = Math.round(this.calculateEstimatedTime(paper.questions) / 60)
    
    return {
      questionCount,
      totalScore,
      estimatedTime: `${estimatedTime}分钟`,
      difficulty: this.formatDifficultyLevel(paper.difficultyLevel),
      type: this.formatPaperType(paper.type)
    }
  }
}

export default paperApi

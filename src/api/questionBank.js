import api from './index'

// 题库管理相关API
export const questionBankApi = {
  // ==================== 题目分类管理 ====================
  
  // 获取题目分类列表
  getCategories(params = {}) {
    return api.get('/question-bank/categories', { params })
  },

  // 创建题目分类
  createCategory(data) {
    return api.post('/question-bank/categories', data)
  },

  // 更新题目分类
  updateCategory(id, data) {
    return api.put(`/question-bank/categories/${id}`, data)
  },

  // 删除题目分类
  deleteCategory(id) {
    return api.delete(`/question-bank/categories/${id}`)
  },

  // ==================== 题目管理 ====================
  
  // 获取题目列表
  getQuestions(params = {}) {
    return api.get('/question-bank/questions', { params })
  },

  // 根据ID获取题目详情
  getQuestionById(id) {
    return api.get(`/question-bank/questions/${id}`)
  },

  // 创建题目
  createQuestion(data) {
    return api.post('/question-bank/questions', data)
  },

  // 更新题目
  updateQuestion(id, data) {
    return api.put(`/question-bank/questions/${id}`, data)
  },

  // 删除题目
  deleteQuestion(id) {
    return api.delete(`/question-bank/questions/${id}`)
  },

  // 批量删除题目
  batchDeleteQuestions(questionIds) {
    return api.delete('/question-bank/questions/batch', { data: { questionIds } })
  },

  // 批量导入题目
  importQuestions(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/question-bank/questions/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 导出题目
  exportQuestions(params = {}) {
    return api.get('/question-bank/questions/export', {
      params,
      responseType: 'blob'
    })
  },

  // ==================== 听力理解题管理 ====================
  
  // 获取听力理解题列表
  getListeningQuestions(params = {}) {
    return api.get('/question-bank/listening', { params })
  },

  // 创建听力理解题
  createListeningQuestion(data) {
    return api.post('/question-bank/listening', data)
  },

  // 更新听力理解题
  updateListeningQuestion(id, data) {
    return api.put(`/question-bank/listening/${id}`, data)
  },

  // 删除听力理解题
  deleteListeningQuestion(id) {
    return api.delete(`/question-bank/listening/${id}`)
  },

  // ==================== 听力选择题管理 ====================
  
  // 获取听力选择题列表
  getMCQQuestions(params = {}) {
    return api.get('/question-bank/mcq', { params })
  },

  // 创建听力选择题
  createMCQQuestion(data) {
    return api.post('/question-bank/mcq', data)
  },

  // 更新听力选择题
  updateMCQQuestion(id, data) {
    return api.put(`/question-bank/mcq/${id}`, data)
  },

  // 删除听力选择题
  deleteMCQQuestion(id) {
    return api.delete(`/question-bank/mcq/${id}`)
  },

  // ==================== 口语模仿题管理 ====================
  
  // 获取口语模仿题列表
  getOPIQuestions(params = {}) {
    return api.get('/question-bank/opi', { params })
  },

  // 创建口语模仿题
  createOPIQuestion(data) {
    return api.post('/question-bank/opi', data)
  },

  // 更新口语模仿题
  updateOPIQuestion(id, data) {
    return api.put(`/question-bank/opi/${id}`, data)
  },

  // 删除口语模仿题
  deleteOPIQuestion(id) {
    return api.delete(`/question-bank/opi/${id}`)
  },

  // ==================== 故事复述题管理 ====================
  
  // 获取故事复述题列表
  getRetellQuestions(params = {}) {
    return api.get('/question-bank/retell', { params })
  },

  // 创建故事复述题
  createRetellQuestion(data) {
    return api.post('/question-bank/retell', data)
  },

  // 更新故事复述题
  updateRetellQuestion(id, data) {
    return api.put(`/question-bank/retell/${id}`, data)
  },

  // 删除故事复述题
  deleteRetellQuestion(id) {
    return api.delete(`/question-bank/retell/${id}`)
  },

  // ==================== ATC模拟题管理 ====================
  
  // 获取ATC模拟题列表
  getATCQuestions(params = {}) {
    return api.get('/question-bank/atc', { params })
  },

  // 创建ATC模拟题
  createATCQuestion(data) {
    return api.post('/question-bank/atc', data)
  },

  // 更新ATC模拟题
  updateATCQuestion(id, data) {
    return api.put(`/question-bank/atc/${id}`, data)
  },

  // 删除ATC模拟题
  deleteATCQuestion(id) {
    return api.delete(`/question-bank/atc/${id}`)
  },

  // 获取ATC场景列表
  getATCScenarios(params = {}) {
    return api.get('/question-bank/atc/scenarios', { params })
  },

  // 创建ATC场景
  createATCScenario(data) {
    return api.post('/question-bank/atc/scenarios', data)
  },

  // 更新ATC场景
  updateATCScenario(id, data) {
    return api.put(`/question-bank/atc/scenarios/${id}`, data)
  },

  // 删除ATC场景
  deleteATCScenario(id) {
    return api.delete(`/question-bank/atc/scenarios/${id}`)
  },

  // ==================== 统计分析 ====================
  
  // 获取题库统计信息
  getQuestionBankStats() {
    return api.get('/question-bank/stats')
  },

  // 获取题目难度分布
  getDifficultyDistribution(categoryId = null) {
    return api.get('/question-bank/stats/difficulty', {
      params: { categoryId }
    })
  },

  // 获取题目类型分布
  getTypeDistribution(categoryId = null) {
    return api.get('/question-bank/stats/type', {
      params: { categoryId }
    })
  },

  // ==================== 工具方法 ====================
  
  // 获取题目类型选项
  getQuestionTypes() {
    return [
      { value: 'listening_comprehension', label: '听力理解', icon: 'Headset', color: '#409EFF' },
      { value: 'multiple_choice', label: '听力选择题', icon: 'List', color: '#67C23A' },
      { value: 'oral_proficiency', label: '口语模仿', icon: 'Microphone', color: '#E6A23C' },
      { value: 'story_retelling', label: '故事复述', icon: 'ChatDotRound', color: '#F56C6C' },
      { value: 'atc_simulation', label: 'ATC模拟', icon: 'Connection', color: '#909399' }
    ]
  },

  // 获取难度等级选项
  getDifficultyLevels() {
    return [
      { value: 1, label: '初级', color: '#67C23A' },
      { value: 2, label: '初中级', color: '#95D475' },
      { value: 3, label: '中级', color: '#E6A23C' },
      { value: 4, label: '中高级', color: '#F78989' },
      { value: 5, label: '高级', color: '#F56C6C' },
      { value: 6, label: '专家级', color: '#909399' }
    ]
  },

  // 获取ATC场景类型
  getATCScenarioTypes() {
    return [
      { value: 'departure', label: '离场', icon: 'Top' },
      { value: 'approach', label: '进场', icon: 'Bottom' },
      { value: 'ground', label: '地面', icon: 'MapLocation' },
      { value: 'tower', label: '塔台', icon: 'OfficeBuilding' },
      { value: 'emergency', label: '紧急情况', icon: 'Warning' },
      { value: 'weather', label: '天气相关', icon: 'Cloudy' }
    ]
  },

  // 格式化题目类型
  formatQuestionType(type) {
    const typeMap = {
      'listening_comprehension': '听力理解',
      'multiple_choice': '听力选择题',
      'oral_proficiency': '口语模仿',
      'story_retelling': '故事复述',
      'atc_simulation': 'ATC模拟'
    }
    return typeMap[type] || type
  },

  // 格式化难度等级
  formatDifficultyLevel(level) {
    const levelMap = {
      1: '初级',
      2: '初中级',
      3: '中级',
      4: '中高级',
      5: '高级',
      6: '专家级'
    }
    return levelMap[level] || `等级${level}`
  }
}

export default questionBankApi

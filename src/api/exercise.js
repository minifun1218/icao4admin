import api from './index'

// 练习相关API (根据API文档更新)
export const exerciseApi = {
  // ==================== 听力选择题 MCQ ====================
  
  // 获取所有题目
  getMCQQuestions(params = {}) {
    return api.get('/listening-mcq/questions', { params })
  },

  // 根据ID获取题目
  getMCQQuestionById(id) {
    return api.get(`/listening-mcq/questions/${id}`)
  },

  // 创建题目 (需要ADMIN权限)
  createMCQQuestion(data) {
    return api.post('/listening-mcq/questions', data)
  },

  // 更新题目 (需要ADMIN权限)
  updateMCQQuestion(id, data) {
    return api.put(`/listening-mcq/questions/${id}`, data)
  },

  // 删除题目 (需要ADMIN权限)
  deleteMCQQuestion(id) {
    return api.delete(`/listening-mcq/questions/${id}`)
  },

  // 提交答案
  submitMCQResponse(data) {
    return api.post('/listening-mcq/responses/submit', data)
  },

  // 获取题目统计信息
  getMCQStats(questionId) {
    return api.get(`/listening-mcq/questions/${questionId}/stats`)
  },

  // ==================== LSA听说评估 ====================
  
  // 获取所有对话
  getLSADialogs(params = {}) {
    return api.get('/lsa-dialogs', { params })
  },

  // 根据ID获取对话
  getLSADialogById(id) {
    return api.get(`/lsa-dialogs/${id}`)
  },

  // 创建对话 (需要ADMIN权限)
  createLSADialog(data) {
    return api.post('/lsa-dialogs', data)
  },

  // 更新对话 (需要ADMIN权限)
  updateLSADialog(id, data) {
    return api.put(`/lsa-dialogs/${id}`, data)
  },

  // 删除对话 (需要ADMIN权限)
  deleteLSADialog(id) {
    return api.delete(`/lsa-dialogs/${id}`)
  },

  // 提交回答
  submitLSAResponse(data) {
    return api.post('/lsa-dialogs/submit', data)
  },

  // ==================== OPI口语面试 ====================
  
  // 获取所有话题
  getOPITopics(params = {}) {
    return api.get('/opi/topics', { params })
  },

  // 根据ID获取话题
  getOPITopicById(id) {
    return api.get(`/opi/topics/${id}`)
  },

  // 根据模块ID获取话题
  getOPITopicsByModule(moduleId) {
    return api.get(`/opi/topics/module/${moduleId}`)
  },

  // 创建话题 (需要ADMIN权限)
  createOPITopic(data) {
    return api.post('/opi/topics', data)
  },

  // 更新话题 (需要ADMIN权限)
  updateOPITopic(id, data) {
    return api.put(`/opi/topics/${id}`, data)
  },

  // 删除话题 (需要ADMIN权限)
  deleteOPITopic(id) {
    return api.delete(`/opi/topics/${id}`)
  },

  // 获取问题列表
  getOPIQuestions(params = {}) {
    return api.get('/opi/questions', { params })
  },

  // 提交回答
  submitOPIResponse(data) {
    return api.post('/opi/submit', data)
  },

  // ==================== 故事复述 ====================
  
  // 获取所有复述题目
  getRetellItems(params = {}) {
    return api.get('/story-retell/items', { params })
  },

  // 根据ID获取复述题目
  getRetellItemById(id) {
    return api.get(`/story-retell/items/${id}`)
  },

  // 创建复述题目 (需要ADMIN权限)
  createRetellItem(data) {
    return api.post('/story-retell/items', data)
  },

  // 更新复述题目 (需要ADMIN权限)
  updateRetellItem(id, data) {
    return api.put(`/story-retell/items/${id}`, data)
  },

  // 删除复述题目 (需要ADMIN权限)
  deleteRetellItem(id) {
    return api.delete(`/story-retell/items/${id}`)
  },

  // 提交复述回答
  submitRetellResponse(data) {
    return api.post('/story-retell/submit', data)
  },

  // ==================== ATC模拟 ====================
  
  // 机场管理
  getAirports(params = {}) {
    return api.get('/atc-sim/airports', { params })
  },

  getAirportById(id) {
    return api.get(`/atc-sim/airports/${id}`)
  },

  searchAirports(keyword) {
    return api.get('/atc-sim/airports/search', { params: { keyword } })
  },

  createAirport(data) {
    return api.post('/atc-sim/airports', data)
  },

  updateAirport(id, data) {
    return api.put(`/atc-sim/airports/${id}`, data)
  },

  deleteAirport(id) {
    return api.delete(`/atc-sim/airports/${id}`)
  },

  // 场景管理
  getScenarios(params = {}) {
    return api.get('/atc-sim/scenarios', { params })
  },

  getScenarioById(id) {
    return api.get(`/atc-sim/scenarios/${id}`)
  },

  createScenario(data) {
    return api.post('/atc-sim/scenarios', data)
  },

  updateScenario(id, data) {
    return api.put(`/atc-sim/scenarios/${id}`, data)
  },

  deleteScenario(id) {
    return api.delete(`/atc-sim/scenarios/${id}`)
  },

  // 提交ATC回答
  submitATCResponse(data) {
    return api.post('/atc-sim/submit', data)
  },

  // ==================== 通用方法 ====================
  
  // 获取题目类型选项
  getQuestionTypes() {
    return [
      'single_choice',    // 单选题
      'multiple_choice',  // 多选题
      'true_false',       // 判断题
      'fill_blank',       // 填空题
      'short_answer'      // 简答题
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
  },

  // 获取评估标准选项
  getEvaluationCriteria() {
    return [
      'pronunciation',    // 发音
      'fluency',         // 流利度
      'accuracy',        // 准确性
      'grammar',         // 语法
      'vocabulary',      // 词汇
      'content',         // 内容
      'coherence'        // 连贯性
    ]
  }
}

export default exerciseApi

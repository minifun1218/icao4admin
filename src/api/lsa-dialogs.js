import request from '@/utils/request'

/**
 * 听力理解对话管理API
 * 基于 LsaDialogsController.java
 */

// ==================== 对话相关接口 ====================

/**
 * 获取所有对话（分页）
 * GET /lsa-dialogs/dialogs
 */
export const getDialogs = (params = {}) => {
  return request.get('/lsa-dialogs/dialogs', { params })
}

/**
 * 根据ID获取对话
 * GET /lsa-dialogs/dialogs/{id}
 */
export const getDialogById = (id) => {
  return request.get(`/lsa-dialogs/dialogs/${id}`)
}

/**
 * 根据模块ID获取对话
 * GET /lsa-dialogs/dialogs/module/{moduleId}
 */
export const getDialogsByModuleId = (moduleId, params = {}) => {
  return request.get(`/lsa-dialogs/dialogs/module/${moduleId}`, { params })
}

/**
 * 创建对话
 * POST /lsa-dialogs/dialogs
 */
export const createDialog = (dialogData) => {
  return request.post('/lsa-dialogs/dialogs', dialogData)
}

/**
 * 更新对话
 * PUT /lsa-dialogs/dialogs/{id}
 */
export const updateDialog = (id, dialogData) => {
  return request.put(`/lsa-dialogs/dialogs/${id}`, dialogData)
}

/**
 * 删除对话
 * DELETE /lsa-dialogs/dialogs/{id}
 */
export const deleteDialog = (id) => {
  return request.delete(`/lsa-dialogs/dialogs/${id}`)
}

/**
 * 激活/停用对话
 * PUT /lsa-dialogs/dialogs/{id}/toggle-active
 */
export const toggleDialogActive = (id) => {
  return request.put(`/lsa-dialogs/dialogs/${id}/toggle-active`)
}

/**
 * 复制对话
 * POST /lsa-dialogs/dialogs/{id}/copy
 */
export const copyDialog = (id) => {
  return request.post(`/lsa-dialogs/dialogs/${id}/copy`)
}

/**
 * 搜索对话
 * GET /lsa-dialogs/dialogs/search
 */
export const searchDialogs = (keyword, params = {}) => {
  return request.get('/lsa-dialogs/dialogs/search', { 
    params: { keyword, ...params } 
  })
}

// ==================== 问题相关接口 ====================

/**
 * 获取对话的问题列表
 * GET /lsa-dialogs/dialogs/{dialogId}/questions
 */
export const getQuestionsByDialogId = (dialogId, params = {}) => {
  return request.get(`/lsa-dialogs/dialogs/${dialogId}/questions`, { params })
}

/**
 * 根据ID获取问题
 * GET /lsa-dialogs/questions/{id}
 */
export const getQuestionById = (id) => {
  return request.get(`/lsa-dialogs/questions/${id}`)
}

/**
 * 创建问题
 * POST /lsa-dialogs/questions
 */
export const createQuestion = (questionData) => {
  return request.post('/lsa-dialogs/questions', questionData)
}

/**
 * 更新问题
 * PUT /lsa-dialogs/questions/{id}
 */
export const updateQuestion = (id, questionData) => {
  return request.put(`/lsa-dialogs/questions/${id}`, questionData)
}

/**
 * 删除问题
 * DELETE /lsa-dialogs/questions/{id}
 */
export const deleteQuestion = (id) => {
  return request.delete(`/lsa-dialogs/questions/${id}`)
}

/**
 * 根据问题类型获取问题
 * GET /lsa-dialogs/questions/type/{questionType}
 */
export const getQuestionsByType = (questionType, params = {}) => {
  return request.get(`/lsa-dialogs/questions/type/${questionType}`, { params })
}

// ==================== 回答相关接口 ====================

/**
 * 获取问题的回答列表
 * GET /lsa-dialogs/questions/{questionId}/responses
 */
export const getResponsesByQuestionId = (questionId, params = {}) => {
  return request.get(`/lsa-dialogs/questions/${questionId}/responses`, { params })
}

/**
 * 根据ID获取回答
 * GET /lsa-dialogs/responses/{id}
 */
export const getResponseById = (id) => {
  return request.get(`/lsa-dialogs/responses/${id}`)
}

/**
 * 提交回答
 * POST /lsa-dialogs/responses/submit
 */
export const submitResponse = (responseData) => {
  return request.post('/lsa-dialogs/responses/submit', responseData)
}

/**
 * 创建回答记录
 * POST /lsa-dialogs/responses
 */
export const createResponse = (responseData) => {
  return request.post('/lsa-dialogs/responses', responseData)
}

/**
 * 评分回答
 * PUT /lsa-dialogs/responses/{id}/score
 */
export const scoreResponse = (id, scoreData) => {
  return request.put(`/lsa-dialogs/responses/${id}/score`, scoreData)
}

/**
 * 自动评分回答
 * PUT /lsa-dialogs/responses/{id}/auto-score
 */
export const autoScoreResponse = (id) => {
  return request.put(`/lsa-dialogs/responses/${id}/auto-score`)
}

/**
 * 删除回答
 * DELETE /lsa-dialogs/responses/{id}
 */
export const deleteResponse = (id) => {
  return request.delete(`/lsa-dialogs/responses/${id}`)
}

// ==================== 统计相关接口 ====================

/**
 * 获取对话统计信息
 * GET /lsa-dialogs/dialogs/{dialogId}/stats
 */
export const getDialogStats = (dialogId) => {
  return request.get(`/lsa-dialogs/dialogs/${dialogId}/stats`)
}

/**
 * 获取模块统计信息
 * GET /lsa-dialogs/modules/{moduleId}/stats
 */
export const getModuleStats = (moduleId) => {
  return request.get(`/lsa-dialogs/modules/${moduleId}/stats`)
}

/**
 * 获取整体统计信息
 * GET /lsa-dialogs/stats/overall
 */
export const getOverallStats = () => {
  return request.get('/lsa-dialogs/stats/overall')
}

// ==================== 批量操作接口 ====================

/**
 * 批量删除对话
 * DELETE /lsa-dialogs/dialogs/batch
 */
export const batchDeleteDialogs = (dialogIds) => {
  return request.delete('/lsa-dialogs/dialogs/batch', { data: dialogIds })
}

/**
 * 批量激活对话
 * PUT /lsa-dialogs/dialogs/batch-activate
 */
export const batchActivateDialogs = (dialogIds) => {
  return request.put('/lsa-dialogs/dialogs/batch-activate', dialogIds)
}

/**
 * 批量停用对话
 * PUT /lsa-dialogs/dialogs/batch-deactivate
 */
export const batchDeactivateDialogs = (dialogIds) => {
  return request.put('/lsa-dialogs/dialogs/batch-deactivate', dialogIds)
}

/**
 * 批量删除问题
 * DELETE /lsa-dialogs/questions/batch
 */
export const batchDeleteQuestions = (questionIds) => {
  return request.delete('/lsa-dialogs/questions/batch', { data: questionIds })
}

/**
 * 批量删除回答
 * DELETE /lsa-dialogs/responses/batch
 */
export const batchDeleteResponses = (responseIds) => {
  return request.delete('/lsa-dialogs/responses/batch', { data: responseIds })
}

// ==================== 导入导出接口 ====================

/**
 * 导出模块对话
 * GET /lsa-dialogs/modules/{moduleId}/export
 */
export const exportDialogsByModule = (moduleId) => {
  return request.get(`/lsa-dialogs/modules/${moduleId}/export`)
}

/**
 * 导入对话到模块
 * POST /lsa-dialogs/modules/{moduleId}/import
 */
export const importDialogsToModule = (moduleId, dialogs) => {
  return request.post(`/lsa-dialogs/modules/${moduleId}/import`, dialogs)
}

// ==================== 题库管理系统相关接口 ====================

/**
 * 获取听力理解题列表
 * GET /lsa-dialogs/question-bank/listening
 */
export const getListeningQuestions = (params = {}) => {
  return request.get('/lsa-dialogs/question-bank/listening', { params })
}

/**
 * 创建听力理解题
 * POST /lsa-dialogs/question-bank/listening
 */
export const createListeningQuestion = (dialogData) => {
  return request.post('/lsa-dialogs/question-bank/listening', dialogData)
}

/**
 * 更新听力理解题
 * PUT /lsa-dialogs/question-bank/listening/{id}
 */
export const updateListeningQuestion = (id, dialogData) => {
  return request.put(`/lsa-dialogs/question-bank/listening/${id}`, dialogData)
}

/**
 * 删除听力理解题
 * DELETE /lsa-dialogs/question-bank/listening/{id}
 */
export const deleteListeningQuestion = (id) => {
  return request.delete(`/lsa-dialogs/question-bank/listening/${id}`)
}

/**
 * 批量操作听力理解题
 * POST /lsa-dialogs/question-bank/listening/batch
 */
export const batchOperateListeningQuestions = (operation, questionIds, data = {}) => {
  return request.post('/lsa-dialogs/question-bank/listening/batch', {
    operation,
    questionIds,
    data
  })
}

/**
 * 上传听力题目音频
 * POST /lsa-dialogs/question-bank/upload/audio
 */
export const uploadQuestionAudio = (file, questionId, quality) => {
  const formData = new FormData()
  formData.append('file', file)
  if (questionId) formData.append('questionId', questionId)
  if (quality) formData.append('quality', quality)
  
  return request.post('/lsa-dialogs/question-bank/upload/audio', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除听力题目音频
 * DELETE /lsa-dialogs/question-bank/upload/audio
 */
export const deleteQuestionAudio = (audioUrl) => {
  return request.delete('/lsa-dialogs/question-bank/upload/audio', {
    data: { audioUrl }
  })
}

/**
 * 批量导入听力理解题
 * POST /lsa-dialogs/question-bank/listening/import
 */
export const importListeningQuestions = (file, options = {}) => {
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
  
  return request.post('/lsa-dialogs/question-bank/listening/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出听力理解题
 * GET /lsa-dialogs/question-bank/listening/export
 */
export const exportListeningQuestions = (params = {}) => {
  return request.get('/lsa-dialogs/question-bank/listening/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导出听力理解题模板
 * GET /lsa-dialogs/question-bank/listening/export/template
 */
export const exportListeningQuestionTemplate = () => {
  return request.get('/lsa-dialogs/question-bank/listening/export/template', {
    responseType: 'blob'
  })
}

/**
 * 验证导入文件
 * POST /lsa-dialogs/question-bank/listening/import/validate
 */
export const validateImportFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/lsa-dialogs/question-bank/listening/import/validate', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 高级搜索听力理解题
 * POST /lsa-dialogs/question-bank/listening/search/advanced
 */
export const advancedSearchListeningQuestions = (criteria) => {
  return request.post('/lsa-dialogs/question-bank/listening/search/advanced', criteria)
}

/**
 * 全文搜索听力理解题
 * GET /lsa-dialogs/question-bank/listening/search/fulltext
 */
export const fullTextSearchListeningQuestions = (query, params = {}) => {
  return request.get('/lsa-dialogs/question-bank/listening/search/fulltext', {
    params: { query, ...params }
  })
}

/**
 * 获取相似题目推荐
 * GET /lsa-dialogs/question-bank/listening/{questionId}/similar
 */
export const getSimilarListeningQuestions = (questionId, limit = 10) => {
  return request.get(`/lsa-dialogs/question-bank/listening/${questionId}/similar`, {
    params: { limit }
  })
}

// ==================== 工具函数 ====================

/**
 * 格式化音频时长显示
 */
export const formatAudioDuration = (seconds) => {
  if (!seconds) return '未知'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

/**
 * 格式化时间限制显示
 */
export const formatTimeLimit = (seconds) => {
  if (!seconds) return '无限制'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

/**
 * 获取对话类型
 */
export const getDialogType = (audioDurationSeconds) => {
  if (!audioDurationSeconds) return '未知'
  
  if (audioDurationSeconds <= 60) {
    return '短对话'
  } else if (audioDurationSeconds <= 300) {
    return '中等对话'
  } else {
    return '长对话'
  }
}

/**
 * 获取难度等级描述
 */
export const getDifficultyDescription = (difficultyLevel) => {
  if (!difficultyLevel) return '未设置'
  
  switch (difficultyLevel) {
    case 1: return '非常简单'
    case 2: return '简单'
    case 3: return '中等'
    case 4: return '困难'
    case 5: return '非常困难'
    default: return '未知'
  }
}

/**
 * 获取问题类型描述
 */
export const getQuestionTypeDescription = (questionType) => {
  if (!questionType) return '未知类型'
  
  switch (questionType.toUpperCase()) {
    case 'MULTIPLE_CHOICE':
    case 'MC':
      return '选择题'
    case 'FILL_IN_BLANK':
    case 'FIB':
      return '填空题'
    case 'SHORT_ANSWER':
    case 'SA':
      return '简答题'
    case 'TRUE_FALSE':
    case 'TF':
      return '判断题'
    case 'ESSAY':
      return '论述题'
    case 'LISTENING':
      return '听力题'
    default:
      return questionType
  }
}

/**
 * 验证对话数据
 */
export const validateDialogData = (dialog) => {
  const errors = []
  
  if (!dialog.title || dialog.title.trim() === '') {
    errors.push('对话标题不能为空')
  }
  
  if (dialog.title && dialog.title.length > 255) {
    errors.push('对话标题长度不能超过255个字符')
  }
  
  if (dialog.timeLimitSeconds && dialog.timeLimitSeconds < 0) {
    errors.push('答题时长限制不能为负数')
  }
  
  if (!dialog.displayOrder || dialog.displayOrder < 1) {
    errors.push('显示顺序必须为正数')
  }
  
  return errors
}

/**
 * 验证问题数据
 */
export const validateQuestionData = (question) => {
  const errors = []
  
  if (!question.dialogId) {
    errors.push('必须选择所属对话')
  }
  
  if (!question.questionType || question.questionType.trim() === '') {
    errors.push('问题类型不能为空')
  }
  
  if (!question.questionText || question.questionText.trim() === '') {
    errors.push('问题文本不能为空')
  }
  
  if (question.questionText && question.questionText.length > 2000) {
    errors.push('问题文本长度不能超过2000个字符')
  }
  
  if (question.points && (question.points < 0 || question.points > 100)) {
    errors.push('分值必须在0-100之间')
  }
  
  if (question.difficultyLevel && (question.difficultyLevel < 1 || question.difficultyLevel > 5)) {
    errors.push('难度等级必须在1-5之间')
  }
  
  // 选择题特殊验证
  if (question.questionType === 'MULTIPLE_CHOICE' || question.questionType === 'MC') {
    const hasOptions = question.optionA || question.optionB || question.optionC || question.optionD
    if (!hasOptions) {
      errors.push('选择题必须至少有一个选项')
    }
    
    if (!question.correctAnswer) {
      errors.push('选择题必须设置正确答案')
    }
  }
  
  return errors
}

/**
 * 生成对话模板
 */
export const generateDialogTemplate = () => {
  return {
    id: null,
    moduleId: null,
    title: '',
    description: '',
    audioId: null,
    dialogText: '',
    audioDurationSeconds: null,
    timeLimitSeconds: null,
    displayOrder: 1,
    isActive: true,
    tags: '',
    metadata: ''
  }
}

/**
 * 生成问题模板
 */
export const generateQuestionTemplate = () => {
  return {
    id: null,
    dialogId: null,
    questionType: 'MULTIPLE_CHOICE',
    questionText: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    correctAnswer: '',
    answerExplanation: '',
    points: 1.0,
    displayOrder: 1,
    difficultyLevel: 3,
    tags: '',
    isActive: true
  }
}

/**
 * 获取问题类型选项
 */
export const getQuestionTypeOptions = () => {
  return [
    { value: 'MULTIPLE_CHOICE', label: '选择题' },
    { value: 'FILL_IN_BLANK', label: '填空题' },
    { value: 'SHORT_ANSWER', label: '简答题' },
    { value: 'TRUE_FALSE', label: '判断题' },
    { value: 'ESSAY', label: '论述题' },
    { value: 'LISTENING', label: '听力题' }
  ]
}

/**
 * 获取难度等级选项
 */
export const getDifficultyLevelOptions = () => {
  return [
    { value: 1, label: '非常简单' },
    { value: 2, label: '简单' },
    { value: 3, label: '中等' },
    { value: 4, label: '困难' },
    { value: 5, label: '非常困难' }
  ]
}
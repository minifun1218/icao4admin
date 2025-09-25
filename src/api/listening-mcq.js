import request from '@/utils/request'
import { createApiMethod } from '@/utils/request'

/**
 * 听力理解选择题管理 API
 * 基于 ListeningMcqController.java
 */

// ==================== 题目相关接口 ====================

/**
 * 获取所有题目（分页）
 * GET /listening-mcq/questions
 */
export const getQuestions = (params = {}) => {
  return request.get('/listening-mcq/questions', { params })
}

/**
 * 根据ID获取题目
 * GET /listening-mcq/questions/{id}
 */
export const getQuestionById = (id) => {
  return request.get(`/listening-mcq/questions/${id}`)
}

/**
 * 根据模块ID获取题目
 * GET /listening-mcq/questions/module/{moduleId}
 */
export const getQuestionsByModuleId = (moduleId, params = {}) => {
  return request.get(`/listening-mcq/questions/module/${moduleId}`, { params })
}

/**
 * 根据音频ID获取题目
 * GET /listening-mcq/questions/audio/{audioId}
 */
export const getQuestionsByAudioId = (audioId, params = {}) => {
  return request.get(`/listening-mcq/questions/audio/${audioId}`, { params })
}

/**
 * 创建题目模块
 * POST /listening-mcq/questions_moudle
 */
export const createQuestionModule = (moduleData) => {
  return request.post('/listening-mcq/questions_moudle', moduleData)
}

/**
 * 创建题目
 * POST /listening-mcq/questions
 */
export const createQuestion = (questionData) => {
  return request.post('/listening-mcq/questions', questionData)
}

/**
 * 更新题目
 * PUT /listening-mcq/questions/{id}
 */
export const updateQuestion = (id, questionData) => {
  return request.put(`/listening-mcq/questions/${id}`, questionData)
}

/**
 * 删除题目
 * DELETE /listening-mcq/questions/{id}
 */
export const deleteQuestion = (id) => {
  return request.delete(`/listening-mcq/questions/${id}`)
}

/**
 * 搜索题目
 * GET /listening-mcq/questions/search
 */
export const searchQuestions = (keyword, params = {}) => {
  return request.get('/listening-mcq/questions/search', { 
    params: { keyword, ...params } 
  })
}

/**
 * 复制题目
 * POST /listening-mcq/questions/{id}/copy
 */
export const copyQuestion = (id) => {
  return request.post(`/listening-mcq/questions/${id}/copy`)
}

// ==================== 选项相关接口 ====================

/**
 * 获取题目的所有选项
 * GET /listening-mcq/questions/{questionId}/choices
 */
export const getChoicesByQuestionId = (questionId) => {
  return request.get(`/listening-mcq/questions/${questionId}/choices`)
}

/**
 * 根据ID获取选项
 * GET /listening-mcq/choices/{id}
 */
export const getChoiceById = (id) => {
  return request.get(`/listening-mcq/choices/${id}`)
}

/**
 * 创建选项
 * POST /listening-mcq/choices
 */
export const createChoice = (choiceData) => {
  return request.post('/listening-mcq/choices', choiceData)
}

/**
 * 为题目创建标准选项（A、B、C、D）
 * POST /listening-mcq/questions/{questionId}/choices/standard
 */
export const createStandardChoices = (questionId, contents, correctLabel) => {
  return request.post(`/listening-mcq/questions/${questionId}/choices/standard`, {
    contents,
    correctLabel
  })
}

/**
 * 更新选项
 * PUT /listening-mcq/choices/{id}
 */
export const updateChoice = (id, choiceData) => {
  return request.put(`/listening-mcq/choices/${id}`, choiceData)
}

/**
 * 删除选项
 * DELETE /listening-mcq/choices/{id}
 */
export const deleteChoice = (id) => {
  return request.delete(`/listening-mcq/choices/${id}`)
}

/**
 * 设置选项为正确答案
 * PUT /listening-mcq/choices/{id}/correct
 */
export const setChoiceAsCorrect = (id) => {
  return request.put(`/listening-mcq/choices/${id}/correct`)
}

/**
 * 切换选项正确性
 * PUT /listening-mcq/choices/{id}/toggle
 */
export const toggleChoiceCorrectness = (id) => {
  return request.put(`/listening-mcq/choices/${id}/toggle`)
}

// ==================== 统计相关接口 ====================

/**
 * 获取题目统计信息
 * GET /listening-mcq/questions/{questionId}/stats
 */
export const getQuestionStats = (questionId) => {
  return request.get(`/listening-mcq/questions/${questionId}/stats`)
}

/**
 * 获取选择题统计
 * GET /listening-mcq/question-bank/mcq/statistics
 */
export const getMCQStatistics = (params = {}) => {
  return request.get('/listening-mcq/question-bank/mcq/statistics', { params })
}

// ==================== 批量操作接口 ====================

/**
 * 批量删除题目
 * DELETE /listening-mcq/questions/batch
 */
export const batchDeleteQuestions = (questionIds) => {
  return request.delete('/listening-mcq/questions/batch', { data: questionIds })
}

/**
 * 批量删除选项
 * DELETE /listening-mcq/choices/batch
 */
export const batchDeleteChoices = (choiceIds) => {
  return request.delete('/listening-mcq/choices/batch', { data: choiceIds })
}

/**
 * 批量操作听力选择题
 * POST /listening-mcq/question-bank/mcq/batch
 */
export const batchOperateQuestions = (operation, questionIds) => {
  return request.post('/listening-mcq/question-bank/mcq/batch', {
    operation,
    questionIds
  })
}

// ==================== 文件上传接口 ====================

/**
 * 上传听力选择题音频文件
 * POST /listening-mcq/question-bank/upload/audio
 */
export const uploadAudioFile = (formData, config = {}) => {
  return request.post('/listening-mcq/question-bank/upload/audio', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000,
    ...config
  })
}

/**
 * 批量上传听力选择题音频文件
 * POST /listening-mcq/question-bank/upload/audio/batch
 */
export const batchUploadAudioFiles = (formData, config = {}) => {
  return request.post('/listening-mcq/question-bank/upload/audio/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 120000,
    ...config
  })
}

/**
 * 上传题目图片文件
 * POST /listening-mcq/question-bank/upload/image
 */
export const uploadImageFile = (formData, config = {}) => {
  return request.post('/listening-mcq/question-bank/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000,
    ...config
  })
}

/**
 * 删除题目图片文件
 * DELETE /listening-mcq/question-bank/upload/image
 */
export const deleteImageFile = (imageUrl) => {
  return request.delete('/listening-mcq/question-bank/upload/image', {
    data: { imageUrl }
  })
}

// ==================== 导入导出接口 ====================

/**
 * 导入题目
 * POST /listening-mcq/question-bank/questions/import
 */
export const importQuestions = (formData, config = {}) => {
  return request.post('/listening-mcq/question-bank/questions/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 120000,
    ...config
  })
}

/**
 * 导出题目
 * GET /listening-mcq/question-bank/questions/export
 */
export const exportQuestions = (params = {}) => {
  return request.get('/listening-mcq/question-bank/questions/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导出模块题目
 * GET /listening-mcq/modules/{moduleId}/export
 */
export const exportQuestionsByModule = (moduleId) => {
  return request.get(`/listening-mcq/modules/${moduleId}/export`)
}

/**
 * 导入题目到模块
 * POST /listening-mcq/modules/{moduleId}/import
 */
export const importQuestionsToModule = (moduleId, questions) => {
  return request.post(`/listening-mcq/modules/${moduleId}/import`, questions)
}

/**
 * 获取导入历史
 * GET /listening-mcq/question-bank/questions/import/history
 */
export const getImportHistory = (params = {}) => {
  return request.get('/listening-mcq/question-bank/questions/import/history', { params })
}

// ==================== 题库管理系统接口 ====================

/**
 * 获取听力选择题列表（题库管理系统）
 * GET /listening-mcq/question-bank/mcq
 */
export const getMCQQuestions = (params = {}) => {
  return request.get('/listening-mcq/question-bank/mcq', { params })
}

/**
 * 创建听力选择题（题库管理系统）
 * POST /listening-mcq/question-bank/mcq
 */
export const createMCQQuestion = (questionData) => {
  return request.post('/listening-mcq/question-bank/mcq', questionData)
}

/**
 * 更新听力选择题（题库管理系统）
 * PUT /listening-mcq/question-bank/mcq/{id}
 */
export const updateMCQQuestion = (id, questionData) => {
  return request.put(`/listening-mcq/question-bank/mcq/${id}`, questionData)
}

/**
 * 删除听力选择题（题库管理系统）
 * DELETE /listening-mcq/question-bank/mcq/{id}
 */
export const deleteMCQQuestion = (id) => {
  return request.delete(`/listening-mcq/question-bank/mcq/${id}`)
}

/**
 * 验证选择题答案
 * POST /listening-mcq/question-bank/mcq/{questionId}/validate
 */
export const validateMCQAnswer = (questionId, answer) => {
  return request.post(`/listening-mcq/question-bank/mcq/${questionId}/validate`, { answer })
}

// ==================== 辅助方法 ====================

/**
 * 格式化答题时间
 */
export const formatAnswerTime = (seconds) => {
  if (!seconds) return '-'
  
  if (seconds < 60) {
    return `${seconds}秒`
  }
  
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  
  if (remainingSeconds === 0) {
    return `${minutes}分钟`
  }
  
  return `${minutes}分${remainingSeconds}秒`
}

/**
 * 格式化播放模式
 */
export const formatPlayMode = (playOnce) => {
  return playOnce ? '仅播放一遍' : '可重复播放'
}

/**
 * 获取难度等级选项
 */
export const getDifficultyLevels = () => {
  return [
    { value: 1, label: '初级', color: '#67c23a' },
    { value: 2, label: '中级', color: '#e6a23c' },
    { value: 3, label: '高级', color: '#f56c6c' },
    { value: 4, label: '专家', color: '#909399' }
  ]
}

/**
 * 获取难度等级标签
 */
export const getDifficultyLabel = (level) => {
  const levels = getDifficultyLevels()
  const found = levels.find(item => item.value === level)
  return found ? found.label : '未知'
}

/**
 * 获取难度等级颜色
 */
export const getDifficultyColor = (level) => {
  const levels = getDifficultyLevels()
  const found = levels.find(item => item.value === level)
  return found ? found.color : '#909399'
}

/**
 * 验证题目数据
 */
export const validateQuestionData = (questionData) => {
  const errors = []
  
  if (!questionData.audioId) {
    errors.push('请选择音频文件')
  }
  
  if (!questionData.textStem || questionData.textStem.trim() === '') {
    errors.push('请输入题干内容')
  }
  
  if (!questionData.answerSeconds || questionData.answerSeconds < 1) {
    errors.push('答题时间必须大于0秒')
  }
  
  if (questionData.answerSeconds > 300) {
    errors.push('答题时间不能超过300秒')
  }
  
  return errors
}

// ==================== 选项管理专用接口 ====================

/**
 * 获取所有选项（分页，用于选项管理页面）
 */
export const getAllChoices = (params = {}) => {
  return request.get('/listening-mcq/choices', { params })
}

/**
 * 搜索选项
 */
export const searchChoices = (keyword, params = {}) => {
  return request.get('/listening-mcq/choices/search', { 
    params: { keyword, ...params } 
  })
}

/**
 * 批量创建选项
 */
export const batchCreateChoices = (choices) => {
  return request.post('/listening-mcq/choices/batch', choices)
}

/**
 * 批量更新选项
 */
export const batchUpdateChoices = (choices) => {
  return request.put('/listening-mcq/choices/batch', choices)
}

/**
 * 复制选项到其他题目
 */
export const copyChoicesToQuestion = (fromQuestionId, toQuestionId) => {
  return request.post(`/listening-mcq/questions/${fromQuestionId}/choices/copy-to/${toQuestionId}`)
}

/**
 * 重新排序选项
 */
export const reorderChoices = (questionId, choiceOrders) => {
  return request.put(`/listening-mcq/questions/${questionId}/choices/reorder`, choiceOrders)
}

/**
 * 获取选项统计信息
 */
export const getChoiceStatistics = (params = {}) => {
  return request.get('/listening-mcq/choices/statistics', { params })
}

// ==================== 选项辅助方法 ====================

/**
 * 获取选项标签选项
 */
export const getChoiceLabels = () => {
  return [
    { value: 'A', label: 'A', color: '#409eff' },
    { value: 'B', label: 'B', color: '#67c23a' },
    { value: 'C', label: 'C', color: '#e6a23c' },
    { value: 'D', label: 'D', color: '#f56c6c' }
  ]
}

/**
 * 获取选项标签颜色
 */
export const getChoiceLabelColor = (label) => {
  const labels = getChoiceLabels()
  const found = labels.find(item => item.value === label)
  return found ? found.color : '#909399'
}

/**
 * 验证选项数据
 */
export const validateChoiceData = (choiceData) => {
  const errors = []
  
  if (!choiceData.questionId) {
    errors.push('请选择关联的题目')
  }
  
  if (!choiceData.label || !['A', 'B', 'C', 'D'].includes(choiceData.label)) {
    errors.push('选项标签必须是A、B、C、D中的一个')
  }
  
  if (!choiceData.content || choiceData.content.trim() === '') {
    errors.push('请输入选项内容')
  }
  
  if (choiceData.content && choiceData.content.length > 1000) {
    errors.push('选项内容不能超过1000个字符')
  }
  
  return errors
}

/**
 * 生成标准选项模板
 */
export const generateStandardChoiceTemplate = (questionId) => {
  return [
    { questionId, label: 'A', content: '', isCorrect: false },
    { questionId, label: 'B', content: '', isCorrect: false },
    { questionId, label: 'C', content: '', isCorrect: false },
    { questionId, label: 'D', content: '', isCorrect: false }
  ]
}

/**
 * 格式化选项显示文本
 */
export const formatChoiceDisplay = (choice) => {
  if (!choice) return ''
  return `${choice.label}. ${choice.content}`
}

/**
 * 检查选项配置是否完整
 */
export const validateChoiceConfiguration = (choices) => {
  const errors = []
  
  if (!choices || choices.length === 0) {
    errors.push('至少需要一个选项')
    return errors
  }
  
  // 检查是否有重复标签
  const labels = choices.map(c => c.label)
  const uniqueLabels = [...new Set(labels)]
  if (labels.length !== uniqueLabels.length) {
    errors.push('选项标签不能重复')
  }
  
  // 检查是否有正确答案
  const correctChoices = choices.filter(c => c.isCorrect)
  if (correctChoices.length === 0) {
    errors.push('必须设置至少一个正确答案')
  }
  
  if (correctChoices.length > 1) {
    errors.push('只能设置一个正确答案')
  }
  
  // 检查内容是否为空
  const emptyChoices = choices.filter(c => !c.content || c.content.trim() === '')
  if (emptyChoices.length > 0) {
    errors.push(`选项 ${emptyChoices.map(c => c.label).join(', ')} 的内容不能为空`)
  }
  
  return errors
}

export default {
  // 题目相关
  getQuestions,
  getQuestionById,
  getQuestionsByModuleId,
  getQuestionsByAudioId,
  createQuestionModule,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  searchQuestions,
  copyQuestion,
  
  // 选项相关
  getChoicesByQuestionId,
  getChoiceById,
  createChoice,
  createStandardChoices,
  updateChoice,
  deleteChoice,
  setChoiceAsCorrect,
  toggleChoiceCorrectness,
  
  // 选项管理专用
  getAllChoices,
  searchChoices,
  batchCreateChoices,
  batchUpdateChoices,
  copyChoicesToQuestion,
  reorderChoices,
  getChoiceStatistics,
  
  // 统计相关
  getQuestionStats,
  getMCQStatistics,
  
  // 批量操作
  batchDeleteQuestions,
  batchDeleteChoices,
  batchOperateQuestions,
  
  // 文件上传
  uploadAudioFile,
  batchUploadAudioFiles,
  uploadImageFile,
  deleteImageFile,
  
  // 导入导出
  importQuestions,
  exportQuestions,
  exportQuestionsByModule,
  importQuestionsToModule,
  getImportHistory,
  
  // 题库管理
  getMCQQuestions,
  createMCQQuestion,
  updateMCQQuestion,
  deleteMCQQuestion,
  validateMCQAnswer,
  
  // 辅助方法
  formatAnswerTime,
  formatPlayMode,
  getDifficultyLevels,
  getDifficultyLabel,
  getDifficultyColor,
  validateQuestionData,
  
  // 选项辅助方法
  getChoiceLabels,
  getChoiceLabelColor,
  validateChoiceData,
  generateStandardChoiceTemplate,
  formatChoiceDisplay,
  validateChoiceConfiguration
}

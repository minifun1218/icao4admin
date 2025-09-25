import request from '@/utils/request'

/**
 * 故事复述API
 * 基于 StoryRetellController.java
 */

// ==================== 复述题目相关接口 ====================

/**
 * 获取所有复述题目（分页）
 * GET /story-retell/items
 */
export const getRetellItems = (params = {}) => {
  return request.get('/story-retell/items', { params })
}

/**
 * 根据ID获取复述题目
 * GET /story-retell/items/{id}
 */
export const getRetellItemById = (id) => {
  return request.get(`/story-retell/items/${id}`)
}

/**
 * 根据模块ID获取复述题目
 * GET /story-retell/items/module/{moduleId}
 */
export const getRetellItemsByModuleId = (moduleId, params = {}) => {
  return request.get(`/story-retell/items/module/${moduleId}`, { params })
}

/**
 * 创建复述题目
 * POST /story-retell/items
 */
export const createRetellItem = (itemData) => {
  return request.post('/story-retell/items', itemData)
}

/**
 * 更新复述题目
 * PUT /story-retell/items/{id}
 */
export const updateRetellItem = (id, itemData) => {
  return request.put(`/story-retell/items/${id}`, itemData)
}

/**
 * 删除复述题目
 * DELETE /story-retell/items/{id}
 */
export const deleteRetellItem = (id) => {
  return request.delete(`/story-retell/items/${id}`)
}

/**
 * 复制复述题目
 * POST /story-retell/items/{id}/copy
 */
export const copyRetellItem = (id) => {
  return request.post(`/story-retell/items/${id}/copy`)
}

/**
 * 搜索复述题目
 * GET /story-retell/items/search
 */
export const searchRetellItems = (keyword, params = {}) => {
  return request.get('/story-retell/items/search', { 
    params: { keyword, ...params } 
  })
}

/**
 * 获取有参考文本的复述题目
 * GET /story-retell/items/with-reference
 */
export const getItemsWithReferenceText = (params = {}) => {
  return request.get('/story-retell/items/with-reference', { params })
}

/**
 * 获取没有参考文本的复述题目
 * GET /story-retell/items/without-reference
 */
export const getItemsWithoutReferenceText = (params = {}) => {
  return request.get('/story-retell/items/without-reference', { params })
}

// ==================== 复述回答相关接口 ====================

/**
 * 获取复述题目的回答列表
 * GET /story-retell/items/{itemId}/responses
 */
export const getResponsesByItemId = (itemId, params = {}) => {
  return request.get(`/story-retell/items/${itemId}/responses`, { params })
}

/**
 * 根据ID获取复述回答
 * GET /story-retell/responses/{id}
 */
export const getResponseById = (id) => {
  return request.get(`/story-retell/responses/${id}`)
}

/**
 * 提交复述回答
 * POST /story-retell/responses/submit
 */
export const submitResponse = (responseData) => {
  return request.post('/story-retell/responses/submit', responseData)
}

/**
 * 创建复述回答记录
 * POST /story-retell/responses
 */
export const createResponse = (responseData) => {
  return request.post('/story-retell/responses', responseData)
}

/**
 * 评分复述回答
 * PUT /story-retell/responses/{id}/score
 */
export const scoreResponse = (id, scoreData) => {
  return request.put(`/story-retell/responses/${id}/score`, scoreData)
}

/**
 * 自动评分复述回答
 * PUT /story-retell/responses/{id}/auto-score
 */
export const autoScoreResponse = (id) => {
  return request.put(`/story-retell/responses/${id}/auto-score`)
}

/**
 * 删除复述回答
 * DELETE /story-retell/responses/{id}
 */
export const deleteResponse = (id) => {
  return request.delete(`/story-retell/responses/${id}`)
}

/**
 * 获取已评分的回答
 * GET /story-retell/responses/scored
 */
export const getScoredResponses = (params = {}) => {
  return request.get('/story-retell/responses/scored', { params })
}

/**
 * 获取未评分的回答
 * GET /story-retell/responses/unscored
 */
export const getUnscoredResponses = (params = {}) => {
  return request.get('/story-retell/responses/unscored', { params })
}

/**
 * 获取及格的回答
 * GET /story-retell/responses/passed
 */
export const getPassedResponses = (params = {}) => {
  return request.get('/story-retell/responses/passed', { params })
}

/**
 * 获取不及格的回答
 * GET /story-retell/responses/failed
 */
export const getFailedResponses = (params = {}) => {
  return request.get('/story-retell/responses/failed', { params })
}

// ==================== 统计相关接口 ====================

/**
 * 获取复述题目统计信息
 * GET /story-retell/items/{itemId}/stats
 */
export const getItemStats = (itemId) => {
  return request.get(`/story-retell/items/${itemId}/stats`)
}

/**
 * 获取模块统计信息
 * GET /story-retell/modules/{moduleId}/stats
 */
export const getModuleStats = (moduleId) => {
  return request.get(`/story-retell/modules/${moduleId}/stats`)
}

/**
 * 获取整体统计信息
 * GET /story-retell/stats/overall
 */
export const getOverallStats = () => {
  return request.get('/story-retell/stats/overall')
}

// ==================== 批量操作接口 ====================

/**
 * 批量删除复述题目
 * DELETE /story-retell/items/batch
 */
export const batchDeleteItems = (itemIds) => {
  return request.delete('/story-retell/items/batch', { data: itemIds })
}

/**
 * 批量删除复述回答
 * DELETE /story-retell/responses/batch
 */
export const batchDeleteResponses = (responseIds) => {
  return request.delete('/story-retell/responses/batch', { data: responseIds })
}

/**
 * 批量移动复述题目到其他模块
 * PUT /story-retell/items/batch-move
 */
export const batchMoveItemsToModule = (itemIds, targetModuleId) => {
  return request.put('/story-retell/items/batch-move', {
    itemIds,
    targetModuleId
  })
}

// ==================== 导入导出接口 ====================

/**
 * 导出模块复述题目
 * GET /story-retell/modules/{moduleId}/export
 */
export const exportItemsByModule = (moduleId) => {
  return request.get(`/story-retell/modules/${moduleId}/export`)
}

/**
 * 导入复述题目到模块
 * POST /story-retell/modules/{moduleId}/import
 */
export const importItemsToModule = (moduleId, items) => {
  return request.post(`/story-retell/modules/${moduleId}/import`, items)
}

/**
 * 导出复述题目的所有回答
 * GET /story-retell/items/{itemId}/responses/export
 */
export const exportResponsesByItem = (itemId) => {
  return request.get(`/story-retell/items/${itemId}/responses/export`)
}

// ==================== 高级查询接口 ====================

/**
 * 根据音频时长范围查找复述题目
 * GET /story-retell/items/audio-duration
 */
export const getItemsByAudioDuration = (minDuration, maxDuration, params = {}) => {
  return request.get('/story-retell/items/audio-duration', {
    params: {
      minDuration,
      maxDuration,
      ...params
    }
  })
}

/**
 * 根据答题时长范围查找复述题目
 * GET /story-retell/items/answer-time
 */
export const getItemsByAnswerTime = (minSeconds, maxSeconds, params = {}) => {
  return request.get('/story-retell/items/answer-time', {
    params: {
      minSeconds,
      maxSeconds,
      ...params
    }
  })
}

/**
 * 根据得分范围查找复述回答
 * GET /story-retell/responses/score-range
 */
export const getResponsesByScoreRange = (minScore, maxScore, params = {}) => {
  return request.get('/story-retell/responses/score-range', {
    params: {
      minScore,
      maxScore,
      ...params
    }
  })
}

// ==================== 题库管理系统相关接口 ====================

/**
 * 获取故事复述题列表
 * GET /story-retell/question-bank/retell
 */
export const getRetellQuestions = (params = {}) => {
  return request.get('/story-retell/question-bank/retell', { params })
}

/**
 * 创建故事复述题
 * POST /story-retell/question-bank/retell
 */
export const createRetellQuestion = (itemData) => {
  return request.post('/story-retell/question-bank/retell', itemData)
}

/**
 * 更新故事复述题
 * PUT /story-retell/question-bank/retell/{id}
 */
export const updateRetellQuestion = (id, itemData) => {
  return request.put(`/story-retell/question-bank/retell/${id}`, itemData)
}

/**
 * 删除故事复述题
 * DELETE /story-retell/question-bank/retell/{id}
 */
export const deleteRetellQuestion = (id) => {
  return request.delete(`/story-retell/question-bank/retell/${id}`)
}

/**
 * 获取故事模板
 * GET /story-retell/question-bank/retell/templates
 */
export const getStoryTemplates = () => {
  return request.get('/story-retell/question-bank/retell/templates')
}

/**
 * 创建故事模板
 * POST /story-retell/question-bank/retell/templates
 */
export const createStoryTemplate = (templateData) => {
  return request.post('/story-retell/question-bank/retell/templates', templateData)
}

/**
 * 批量操作故事复述题
 * POST /story-retell/question-bank/retell/batch
 */
export const batchOperateRetellQuestions = (operation, questionIds, data = {}) => {
  return request.post('/story-retell/question-bank/retell/batch', {
    operation,
    questionIds,
    data
  })
}

/**
 * 上传复述题目音频
 * POST /story-retell/question-bank/upload/audio
 */
export const uploadQuestionAudio = (file, questionId, quality) => {
  const formData = new FormData()
  formData.append('file', file)
  if (questionId) formData.append('questionId', questionId)
  if (quality) formData.append('quality', quality)
  
  return request.post('/story-retell/question-bank/upload/audio', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除复述题目音频
 * DELETE /story-retell/question-bank/upload/audio
 */
export const deleteQuestionAudio = (audioUrl) => {
  return request.delete('/story-retell/question-bank/upload/audio', {
    data: { audioUrl }
  })
}

/**
 * 批量导入故事复述题
 * POST /story-retell/question-bank/retell/import
 */
export const importRetellQuestions = (file, options = {}) => {
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
  
  return request.post('/story-retell/question-bank/retell/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出故事复述题
 * GET /story-retell/question-bank/retell/export
 */
export const exportRetellQuestions = (params = {}) => {
  return request.get('/story-retell/question-bank/retell/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导出故事复述题模板
 * GET /story-retell/question-bank/retell/export/template
 */
export const exportRetellQuestionTemplate = () => {
  return request.get('/story-retell/question-bank/retell/export/template', {
    responseType: 'blob'
  })
}

/**
 * 高级搜索故事复述题
 * POST /story-retell/question-bank/retell/search/advanced
 */
export const advancedSearchRetellQuestions = (criteria) => {
  return request.post('/story-retell/question-bank/retell/search/advanced', criteria)
}

/**
 * 全文搜索故事复述题
 * GET /story-retell/question-bank/retell/search/fulltext
 */
export const fullTextSearchRetellQuestions = (query, params = {}) => {
  return request.get('/story-retell/question-bank/retell/search/fulltext', {
    params: { query, ...params }
  })
}

/**
 * 获取相似题目推荐
 * GET /story-retell/question-bank/retell/{questionId}/similar
 */
export const getSimilarRetellQuestions = (questionId, limit = 10) => {
  return request.get(`/story-retell/question-bank/retell/${questionId}/similar`, {
    params: { limit }
  })
}

/**
 * 智能推荐题目
 * GET /story-retell/question-bank/retell/recommend
 */
export const getRecommendedRetellQuestions = (userId, limit = 10, difficultyLevel = null) => {
  return request.get('/story-retell/question-bank/retell/recommend', {
    params: {
      userId,
      limit,
      difficultyLevel
    }
  })
}

// ==================== 工具函数 ====================

/**
 * 格式化音频时长显示
 */
export const formatAudioDuration = (seconds) => {
  if (!seconds) return '0:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

/**
 * 格式化答题时长显示
 */
export const formatAnswerTime = (seconds) => {
  if (!seconds) return '0:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

/**
 * 验证复述题目数据
 */
export const validateRetellItemData = (item) => {
  const errors = []
  
  if (!item.title || item.title.trim() === '') {
    errors.push('题目标题不能为空')
  }
  
  if (item.title && item.title.length > 200) {
    errors.push('题目标题长度不能超过200个字符')
  }
  
  if (!item.moduleId) {
    errors.push('必须选择所属模块')
  }
  
  if (!item.audioAssetId) {
    errors.push('必须上传音频文件')
  }
  
  if (!item.audioDurationSec || item.audioDurationSec <= 0) {
    errors.push('音频时长必须大于0')
  }
  
  if (!item.answerSeconds || item.answerSeconds <= 0) {
    errors.push('答题时长必须大于0')
  }
  
  return errors
}

/**
 * 生成复述题目模板
 */
export const generateRetellItemTemplate = () => {
  return {
    id: null,
    moduleId: null,
    title: '',
    audioAssetId: null,
    audioDurationSec: 60,
    answerSeconds: 90,
    referenceText: ''
  }
}

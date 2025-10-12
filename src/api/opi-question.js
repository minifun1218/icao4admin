import request from '@/utils/request'

/**
 * OPI 问题管理 API
 */

// ==================== 问题相关接口 ====================

/**
 * 获取所有问题列表
 */
export function getQuestions(params = {}) {
  return request({
    url: '/opi/questions',
    method: 'get',
    params
  })
}

/**
 * 根据话题ID获取问题列表
 */
export function getQuestionsByTopicId(topicId, params = {}) {
  return request({
    url: `/opi/topics/${topicId}/questions`,
    method: 'get',
    params
  })
}

/**
 * 根据ID获取问题详情
 */
export function getQuestionById(id) {
  return request({
    url: `/opi/questions/${id}`,
    method: 'get'
  })
}

/**
 * 创建问题
 */
export function createQuestion(data) {
  return request({
    url: '/opi/questions',
    method: 'post',
    data
  })
}

/**
 * 更新问题
 */
export function updateQuestion(id, data) {
  return request({
    url: `/opi/questions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除问题
 */
export function deleteQuestion(id) {
  return request({
    url: `/opi/questions/${id}`,
    method: 'delete'
  })
}

/**
 * 复制问题
 */
export function copyQuestion(id) {
  return request({
    url: `/opi/questions/${id}/copy`,
    method: 'post'
  })
}

/**
 * 根据回答时间范围查找问题
 */
export function getQuestionsByAnswerTime(minSeconds, maxSeconds, params = {}) {
  return request({
    url: '/opi/questions/answer-time',
    method: 'get',
    params: {
      minSeconds,
      maxSeconds,
      ...params
    }
  })
}

/**
 * 获取有屏显文本的问题
 */
export function getQuestionsWithPromptText(params = {}) {
  return request({
    url: '/opi/questions/with-prompt-text',
    method: 'get',
    params
  })
}

/**
 * 获取没有屏显文本的问题
 */
export function getQuestionsWithoutPromptText(params = {}) {
  return request({
    url: '/opi/questions/without-prompt-text',
    method: 'get',
    params
  })
}

/**
 * 批量删除问题
 */
export function batchDeleteQuestions(questionIds) {
  return request({
    url: '/opi/questions/batch',
    method: 'delete',
    data: questionIds
  })
}

/**
 * 导出话题问题
 */
export function exportQuestionsByTopic(topicId) {
  return request({
    url: `/opi/topics/${topicId}/questions/export`,
    method: 'get'
  })
}

/**
 * 获取问题统计信息
 */
export function getQuestionStats(questionId) {
  return request({
    url: `/opi/questions/${questionId}/stats`,
    method: 'get'
  })
}

// ==================== 话题相关接口 ====================

/**
 * 获取所有话题
 */
export function getTopics(params = {}) {
  return request({
    url: '/opi/topics',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取话题
 */
export function getTopicById(id) {
  return request({
    url: `/opi/topics/${id}`,
    method: 'get'
  })
}

/**
 * 根据模块ID获取话题
 */
export function getTopicsByModuleId(moduleId, params = {}) {
  return request({
    url: `/opi/topics/module/${moduleId}`,
    method: 'get',
    params
  })
}

/**
 * 创建话题
 */
export function createTopic(data) {
  return request({
    url: '/opi/topics',
    method: 'post',
    data
  })
}

/**
 * 更新话题
 */
export function updateTopic(id, data) {
  return request({
    url: `/opi/topics/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除话题
 */
export function deleteTopic(id) {
  return request({
    url: `/opi/topics/${id}`,
    method: 'delete'
  })
}

/**
 * 复制话题
 */
export function copyTopic(id) {
  return request({
    url: `/opi/topics/${id}/copy`,
    method: 'post'
  })
}

/**
 * 搜索话题
 */
export function searchTopics(keyword, params = {}) {
  return request({
    url: '/opi/topics/search',
    method: 'get',
    params: {
      keyword,
      ...params
    }
  })
}

/**
 * 批量删除话题
 */
export function batchDeleteTopics(topicIds) {
  return request({
    url: '/opi/topics/batch',
    method: 'delete',
    data: topicIds
  })
}

/**
 * 导出模块话题
 */
export function exportTopicsByModule(moduleId) {
  return request({
    url: `/opi/modules/${moduleId}/topics/export`,
    method: 'get'
  })
}

/**
 * 获取话题统计信息
 */
export function getTopicStats(topicId) {
  return request({
    url: `/opi/topics/${topicId}/stats`,
    method: 'get'
  })
}

/**
 * 获取模块统计信息
 */
export function getModuleStats(moduleId) {
  return request({
    url: `/opi/modules/${moduleId}/stats`,
    method: 'get'
  })
}

/**
 * 获取整体统计信息
 */
export function getOverallStats() {
  return request({
    url: '/opi/stats/overall',
    method: 'get'
  })
}

// ==================== 题库管理系统相关接口 ====================

/**
 * 获取口语模仿题列表
 */
export function getOPIQuestions(params = {}) {
  return request({
    url: '/opi/question-bank/opi',
    method: 'get',
    params
  })
}

/**
 * 创建口语模仿题
 */
export function createOPIQuestion(data) {
  return request({
    url: '/opi/question-bank/opi',
    method: 'post',
    data
  })
}

/**
 * 更新口语模仿题
 */
export function updateOPIQuestion(id, data) {
  return request({
    url: `/opi/question-bank/opi/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除口语模仿题
 */
export function deleteOPIQuestion(id) {
  return request({
    url: `/opi/question-bank/opi/${id}`,
    method: 'delete'
  })
}

/**
 * 获取口语评分标准
 */
export function getOPIScoringCriteria() {
  return request({
    url: '/opi/question-bank/opi/scoring-criteria',
    method: 'get'
  })
}

/**
 * 更新口语评分标准
 */
export function updateOPIScoringCriteria(criteria) {
  return request({
    url: '/opi/question-bank/opi/scoring-criteria',
    method: 'put',
    data: criteria
  })
}

/**
 * 批量操作口语模仿题
 */
export function batchOperateOPIQuestions(operation, questionIds, data = {}) {
  return request({
    url: '/opi/question-bank/opi/batch',
    method: 'post',
    data: {
      operation,
      questionIds,
      data
    }
  })
}

/**
 * 上传口语题目音频
 */
export function uploadQuestionAudio(formData) {
  return request({
    url: '/opi/question-bank/upload/audio',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除口语题目音频
 */
export function deleteQuestionAudio(audioUrl) {
  return request({
    url: '/opi/question-bank/upload/audio',
    method: 'delete',
    data: { audioUrl }
  })
}

/**
 * 批量导入口语模仿题
 */
export function importOPIQuestions(formData) {
  return request({
    url: '/opi/question-bank/opi/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出口语模仿题
 */
export function exportOPIQuestions(params = {}) {
  return request({
    url: '/opi/question-bank/opi/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 导出口语模仿题模板
 */
export function exportOPIQuestionTemplate() {
  return request({
    url: '/opi/question-bank/opi/export/template',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 高级搜索口语模仿题
 */
export function advancedSearchOPIQuestions(criteria) {
  return request({
    url: '/opi/question-bank/opi/search/advanced',
    method: 'post',
    data: criteria
  })
}

/**
 * 全文搜索口语模仿题
 */
export function fullTextSearchOPIQuestions(query, params = {}) {
  return request({
    url: '/opi/question-bank/opi/search/fulltext',
    method: 'get',
    params: {
      query,
      ...params
    }
  })
}

/**
 * 获取相似题目推荐
 */
export function getSimilarOPIQuestions(questionId, limit = 10) {
  return request({
    url: `/opi/question-bank/opi/${questionId}/similar`,
    method: 'get',
    params: { limit }
  })
}

/**
 * 智能推荐题目
 */
export function getRecommendedOPIQuestions(userId, params = {}) {
  return request({
    url: '/opi/question-bank/opi/recommend',
    method: 'get',
    params: {
      userId,
      ...params
    }
  })
}

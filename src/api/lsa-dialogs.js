import request from '@/utils/request'

/**
 * 听力简答对话管理 API
 */

// ==================== 对话相关接口 ====================

/**
 * 获取所有对话
 */
export function getAllDialogs(params = {}) {
  return request({
    url: '/lsa-dialogs/dialogs',
    method: 'get',
    params: {
      page: 0,
      size: 20,
      sort: ['displayOrder,asc'],
      ...params
    }
  })
}

/**
 * 根据ID获取对话
 */
export function getDialogById(id) {
  return request({
    url: `/lsa-dialogs/dialogs/${id}`,
    method: 'get'
  })
}

/**
 * 根据模块ID获取对话
 */
export function getDialogsByModuleId(moduleId, params = {}) {
  return request({
    url: `/lsa-dialogs/dialogs/module/${moduleId}`,
    method: 'get',
    params: {
      page: 0,
      size: 20,
      sort: ['displayOrder,asc'],
      ...params
    }
  })
}

/**
 * 创建对话
 */
export function createDialog(data) {
  return request({
    url: '/lsa-dialogs/dialogs',
    method: 'post',
    data
  })
}

/**
 * 更新对话
 */
export function updateDialog(id, data) {
  return request({
    url: `/lsa-dialogs/dialogs/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除对话
 */
export function deleteDialog(id) {
  return request({
    url: `/lsa-dialogs/dialogs/${id}`,
    method: 'delete'
  })
}

/**
 * 激活/停用对话
 */
export function toggleDialogActive(id) {
  return request({
    url: `/lsa-dialogs/dialogs/${id}/toggle-active`,
    method: 'put'
  })
}

/**
 * 复制对话
 */
export function copyDialog(id) {
  return request({
    url: `/lsa-dialogs/dialogs/${id}/copy`,
    method: 'post'
  })
}

/**
 * 搜索对话
 */
export function searchDialogs(keyword, params = {}) {
  return request({
    url: '/lsa-dialogs/dialogs/search',
    method: 'get',
    params: {
      keyword,
      page: 0,
      size: 20,
      ...params
    }
  })
}

// ==================== 问题相关接口 ====================

/**
 * 获取对话的问题列表
 */
export function getQuestionsByDialogId(dialogId, params = {}) {
  return request({
    url: `/lsa-dialogs/dialogs/${dialogId}/questions`,
    method: 'get',
    params: {
      page: 0,
      size: 20,
      sort: ['displayOrder,asc'],
      ...params
    }
  })
}

/**
 * 根据ID获取问题
 */
export function getQuestionById(id) {
  return request({
    url: `/lsa-dialogs/questions/${id}`,
    method: 'get'
  })
}

/**
 * 创建问题
 */
export function createQuestion(data) {
  return request({
    url: '/lsa-dialogs/questions',
    method: 'post',
    data
  })
}

/**
 * 更新问题
 */
export function updateQuestion(id, data) {
  return request({
    url: `/lsa-dialogs/questions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除问题
 */
export function deleteQuestion(id) {
  return request({
    url: `/lsa-dialogs/questions/${id}`,
    method: 'delete'
  })
}

/**
 * 根据问题类型获取问题
 */
export function getQuestionsByType(questionType, params = {}) {
  return request({
    url: `/lsa-dialogs/questions/type/${questionType}`,
    method: 'get',
    params: {
      page: 0,
      size: 20,
      ...params
    }
  })
}

// ==================== 回答相关接口 ====================

/**
 * 获取问题的回答列表
 */
export function getResponsesByQuestionId(questionId, params = {}) {
  return request({
    url: `/lsa-dialogs/questions/${questionId}/responses`,
    method: 'get',
    params: {
      page: 0,
      size: 20,
      sort: ['answeredAt,desc'],
      ...params
    }
  })
}

/**
 * 根据ID获取回答
 */
export function getResponseById(id) {
  return request({
    url: `/lsa-dialogs/responses/${id}`,
    method: 'get'
  })
}

/**
 * 提交回答
 */
export function submitResponse(data) {
  return request({
    url: '/lsa-dialogs/responses/submit',
    method: 'post',
    data
  })
}

/**
 * 创建回答记录
 */
export function createResponse(data) {
  return request({
    url: '/lsa-dialogs/responses',
    method: 'post',
    data
  })
}

/**
 * 评分回答
 */
export function scoreResponse(id, data) {
  return request({
    url: `/lsa-dialogs/responses/${id}/score`,
    method: 'put',
    data
  })
}

/**
 * 自动评分回答
 */
export function autoScoreResponse(id) {
  return request({
    url: `/lsa-dialogs/responses/${id}/auto-score`,
    method: 'put'
  })
}

/**
 * 删除回答
 */
export function deleteResponse(id) {
  return request({
    url: `/lsa-dialogs/responses/${id}`,
    method: 'delete'
  })
}

// ==================== 统计相关接口 ====================

/**
 * 获取对话统计信息
 */
export function getDialogStats(dialogId) {
  return request({
    url: `/lsa-dialogs/dialogs/${dialogId}/stats`,
    method: 'get'
  })
}

/**
 * 获取模块统计信息
 */
export function getModuleStats(moduleId) {
  return request({
    url: `/lsa-dialogs/modules/${moduleId}/stats`,
    method: 'get'
  })
}

/**
 * 获取整体统计信息
 */
export function getOverallStats() {
  return request({
    url: '/lsa-dialogs/stats/overall',
    method: 'get'
  })
}

// ==================== 批量操作接口 ====================

/**
 * 批量删除对话
 */
export function batchDeleteDialogs(dialogIds) {
  return request({
    url: '/lsa-dialogs/dialogs/batch',
    method: 'delete',
    data: dialogIds
  })
}

/**
 * 批量激活对话
 */
export function batchActivateDialogs(dialogIds) {
  return request({
    url: '/lsa-dialogs/dialogs/batch-activate',
    method: 'put',
    data: dialogIds
  })
}

/**
 * 批量停用对话
 */
export function batchDeactivateDialogs(dialogIds) {
  return request({
    url: '/lsa-dialogs/dialogs/batch-deactivate',
    method: 'put',
    data: dialogIds
  })
}

/**
 * 批量删除问题
 */
export function batchDeleteQuestions(questionIds) {
  return request({
    url: '/lsa-dialogs/questions/batch',
    method: 'delete',
    data: questionIds
  })
}

/**
 * 批量删除回答
 */
export function batchDeleteResponses(responseIds) {
  return request({
    url: '/lsa-dialogs/responses/batch',
    method: 'delete',
    data: responseIds
  })
}

// ==================== 导入导出接口 ====================

/**
 * 导出模块对话
 */
export function exportDialogsByModule(moduleId) {
  return request({
    url: `/lsa-dialogs/modules/${moduleId}/export`,
    method: 'get'
  })
}

/**
 * 导入对话到模块
 */
export function importDialogsToModule(moduleId, dialogs) {
  return request({
    url: `/lsa-dialogs/modules/${moduleId}/import`,
    method: 'post',
    data: dialogs
  })
}

// ==================== 题库管理系统相关接口 ====================

/**
 * 获取听力理解题列表
 */
export function getListeningQuestions(params = {}) {
  return request({
    url: '/lsa-dialogs/question-bank/listening',
    method: 'get',
    params: {
      page: 0,
      size: 20,
      ...params
    }
  })
}

/**
 * 创建听力理解题
 */
export function createListeningQuestion(data) {
  return request({
    url: '/lsa-dialogs/question-bank/listening',
    method: 'post',
    data
  })
}

/**
 * 更新听力理解题
 */
export function updateListeningQuestion(id, data) {
  return request({
    url: `/lsa-dialogs/question-bank/listening/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除听力理解题
 */
export function deleteListeningQuestion(id) {
  return request({
    url: `/lsa-dialogs/question-bank/listening/${id}`,
    method: 'delete'
  })
}

/**
 * 批量操作听力理解题
 */
export function batchOperateListeningQuestions(data) {
  return request({
    url: '/lsa-dialogs/question-bank/listening/batch',
    method: 'post',
    data
  })
}

/**
 * 上传听力题目音频
 */
export function uploadQuestionAudio(file, questionId, quality) {
  const formData = new FormData()
  formData.append('file', file)
  if (questionId) formData.append('questionId', questionId)
  if (quality) formData.append('quality', quality)
  
  return request({
    url: '/lsa-dialogs/question-bank/upload/audio',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除听力题目音频
 */
export function deleteQuestionAudio(audioUrl) {
  return request({
    url: '/lsa-dialogs/question-bank/upload/audio',
    method: 'delete',
    data: { audioUrl }
  })
}

/**
 * 批量导入听力理解题
 */
export function importListeningQuestions(file, options = {}) {
  const formData = new FormData()
  formData.append('file', file)
  
  Object.keys(options).forEach(key => {
    formData.append(key, options[key])
  })
  
  return request({
    url: '/lsa-dialogs/question-bank/listening/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出听力理解题
 */
export function exportListeningQuestions(params = {}) {
  return request({
    url: '/lsa-dialogs/question-bank/listening/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 导出听力理解题模板
 */
export function exportListeningQuestionTemplate() {
  return request({
    url: '/lsa-dialogs/question-bank/listening/export/template',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 验证导入文件
 */
export function validateImportFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/lsa-dialogs/question-bank/listening/import/validate',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 高级搜索听力理解题
 */
export function advancedSearchListeningQuestions(criteria) {
  return request({
    url: '/lsa-dialogs/question-bank/listening/search/advanced',
    method: 'post',
    data: criteria
  })
}

/**
 * 全文搜索听力理解题
 */
export function fullTextSearchListeningQuestions(query, params = {}) {
  return request({
    url: '/lsa-dialogs/question-bank/listening/search/fulltext',
    method: 'get',
    params: {
      query,
      page: 0,
      size: 20,
      ...params
    }
  })
}

/**
 * 获取相似题目推荐
 */
export function getSimilarListeningQuestions(questionId, limit = 10) {
  return request({
    url: `/lsa-dialogs/question-bank/listening/${questionId}/similar`,
    method: 'get',
    params: { limit }
  })
}

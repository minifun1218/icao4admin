import request from '@/utils/request'

/**
 * OPI 话题管理 API
 */

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

// ==================== 问题相关接口（话题管理页面可能需要） ====================

/**
 * 获取话题的问题列表
 */
export function getQuestionsByTopicId(topicId, params = {}) {
  return request({
    url: `/opi/topics/${topicId}/questions`,
    method: 'get',
    params
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

// ==================== 模块相关接口（用于下拉选择） ====================

/**
 * 获取考试模块列表（简化版，用于下拉选择）
 */
export function getExamModules(params = {}) {
  return request({
    url: '/exam/modules',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取考试模块
 */
export function getExamModuleById(id) {
  return request({
    url: `/exam/modules/${id}`,
    method: 'get'
  })
}

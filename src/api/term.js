import request from '@/utils/request'

// ==================== 术语管理 API ====================

/**
 * 创建术语
 */
export function createTerm(data) {
  return request({
    url: '/terms',
    method: 'post',
    data
  })
}

/**
 * 获取术语详情
 */
export function getTermById(id, includeTopics = false) {
  return request({
    url: `/terms/${id}`,
    method: 'get',
    params: { includeTopics }
  })
}

/**
 * 分页查询术语
 */
export function getTerms(params) {
  return request({
    url: '/terms',
    method: 'get',
    params
  })
}

/**
 * 更新术语
 */
export function updateTerm(id, data) {
  return request({
    url: `/terms/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除术语
 */
export function deleteTerm(id) {
  return request({
    url: `/terms/${id}`,
    method: 'delete'
  })
}

/**
 * 根据词条获取术语
 */
export function getTermsByHeadword(headword) {
  return request({
    url: `/terms/by-headword/${headword}`,
    method: 'get'
  })
}

/**
 * 根据CEFR等级获取术语列表
 */
export function getTermsByCefrLevel(cefrLevel, params) {
  return request({
    url: `/terms/by-cefr-level/${cefrLevel}`,
    method: 'get',
    params
  })
}

/**
 * 根据频次排名范围获取术语列表
 */
export function getTermsByFreqRank(minRank, maxRank, params) {
  return request({
    url: '/terms/by-freq-rank',
    method: 'get',
    params: { minRank, maxRank, ...params }
  })
}

/**
 * 检查词条是否可用
 */
export function checkHeadwordAvailability(headword) {
  return request({
    url: `/terms/check-headword/${headword}`,
    method: 'get'
  })
}

/**
 * 复制术语
 */
export function copyTerm(id) {
  return request({
    url: `/terms/${id}/copy`,
    method: 'post'
  })
}

// ==================== 术语主题映射管理 API ====================

/**
 * 为术语添加主题映射
 */
export function addTopicMapping(termId, data) {
  return request({
    url: `/terms/${termId}/topics`,
    method: 'post',
    data
  })
}

/**
 * 获取术语的主题映射列表
 */
export function getTermTopicMappings(termId) {
  return request({
    url: `/terms/${termId}/topics`,
    method: 'get'
  })
}

/**
 * 更新术语的主题映射
 */
export function updateTopicMapping(termId, topicId, data) {
  return request({
    url: `/terms/${termId}/topics/${topicId}`,
    method: 'put',
    data
  })
}

/**
 * 删除术语的主题映射
 */
export function removeTopicMapping(termId, topicId) {
  return request({
    url: `/terms/${termId}/topics/${topicId}`,
    method: 'delete'
  })
}

// ==================== 术语主题管理 API ====================

/**
 * 创建术语主题
 */
export function createTermTopic(data) {
  return request({
    url: '/terms/topics',
    method: 'post',
    data
  })
}

/**
 * 获取术语主题详情
 */
export function getTermTopicById(id) {
  return request({
    url: `/terms/topics/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询术语主题
 */
export function getTermTopics(params) {
  return request({
    url: '/terms/topics',
    method: 'get',
    params
  })
}

/**
 * 更新术语主题
 */
export function updateTermTopic(id, data) {
  return request({
    url: `/terms/topics/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除术语主题
 */
export function deleteTermTopic(id) {
  return request({
    url: `/terms/topics/${id}`,
    method: 'delete'
  })
}

// ==================== 高级查询和统计 API ====================

/**
 * 根据主题ID获取术语列表
 */
export function getTermsByTopicId(topicId, params) {
  return request({
    url: `/terms/by-topic/${topicId}`,
    method: 'get',
    params
  })
}

/**
 * 设置术语的主归属主题
 */
export function setPrimaryTopic(termId, topicId) {
  return request({
    url: `/terms/${termId}/primary-topic/${topicId}`,
    method: 'put'
  })
}

/**
 * 批量添加术语到主题
 */
export function batchAddToTopic(termIds, topicId, isPrimary = false) {
  return request({
    url: '/terms/batch-add-to-topic',
    method: 'post',
    params: { termIds, topicId, isPrimary }
  })
}

/**
 * 批量从主题移除术语
 */
export function batchRemoveFromTopic(termIds, topicId) {
  return request({
    url: '/terms/batch-remove-from-topic',
    method: 'delete',
    params: { termIds, topicId }
  })
}

/**
 * 高级查询术语
 */
export function advancedSearchTerms(data, params) {
  return request({
    url: '/terms/advanced-search',
    method: 'post',
    data,
    params
  })
}

/**
 * 获取术语统计信息
 */
export function getTermStatistics() {
  return request({
    url: '/terms/statistics',
    method: 'get'
  })
}

/**
 * 获取术语统计信息（按条件）
 */
export function getConditionalStatistics(data) {
  return request({
    url: '/terms/statistics/conditional',
    method: 'post',
    data
  })
}

/**
 * 批量操作术语
 */
export function batchOperation(data) {
  return request({
    url: '/terms/batch-operation',
    method: 'post',
    data
  })
}

/**
 * 获取最大频次排名
 */
export function getMaxFreqRank() {
  return request({
    url: '/terms/max-freq-rank',
    method: 'get'
  })
}

/**
 * 导出术语数据
 */
export function exportTerms(data) {
  return request({
    url: '/terms/export',
    method: 'post',
    data
  })
}

/**
 * 随机获取术语
 */
export function getRandomTerms(params) {
  return request({
    url: '/terms/random',
    method: 'get',
    params
  })
}

// ==================== 数据导入导出 API ====================

/**
 * 导出术语数据
 */
export function exportTermsData(params) {
  return request({
    url: '/terms/export',
    method: 'get',
    params
  })
}

/**
 * 导入术语数据
 */
export function importTerms(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/terms/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ==================== 工具方法 API ====================

/**
 * 获取术语分类选项
 */
export function getTermCategories() {
  return request({
    url: '/terms/categories',
    method: 'get'
  })
}

/**
 * 获取术语来源选项
 */
export function getTermSources() {
  return request({
    url: '/terms/sources',
    method: 'get'
  })
}

/**
 * 获取CEFR等级选项
 */
export function getCEFRLevels() {
  return request({
    url: '/vocab/cefr-levels', // 复用词汇的CEFR等级接口
    method: 'get'
  })
}

/**
 * 获取词性选项
 */
export function getPOSOptions() {
  return request({
    url: '/vocab/pos-options', // 复用词汇的词性选项接口
    method: 'get'
  })
}

/**
 * 获取频率等级选项
 */
export function getFrequencyLevels() {
  return request({
    url: '/vocab/frequency-levels', // 复用词汇的频率等级接口
    method: 'get'
  })
}

/**
 * 获取难度等级选项
 */
export function getDifficultyLevels() {
  return request({
    url: '/vocab/difficulty-levels', // 复用词汇的难度等级接口
    method: 'get'
  })
}

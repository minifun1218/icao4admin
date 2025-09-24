import request from '@/utils/request'

// ==================== 词汇管理 API ====================

/**
 * 创建词汇
 */
export function createVocab(data) {
  return request({
    url: '/vocab',
    method: 'post',
    data
  })
}

/**
 * 获取词汇详情
 */
export function getVocabById(id, includeTopics = false) {
  return request({
    url: `/vocab/${id}`,
    method: 'get',
    params: { includeTopics }
  })
}

/**
 * 分页查询词汇
 */
export function getVocabs(params) {
  return request({
    url: '/vocab',
    method: 'get',
    params
  })
}

/**
 * 更新词汇
 */
export function updateVocab(id, data) {
  return request({
    url: `/vocab/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除词汇
 */
export function deleteVocab(id) {
  return request({
    url: `/vocab/${id}`,
    method: 'delete'
  })
}

/**
 * 根据单词查找词汇
 */
export function getVocabsByWord(word) {
  return request({
    url: `/vocab/by-word/${word}`,
    method: 'get'
  })
}

/**
 * 模糊搜索词汇
 */
export function searchVocabs(params) {
  return request({
    url: '/vocab/search',
    method: 'get',
    params
  })
}

/**
 * 综合搜索词汇
 */
export function comprehensiveSearchVocabs(params) {
  return request({
    url: '/vocab/comprehensive-search',
    method: 'get',
    params
  })
}

/**
 * 批量创建词汇
 */
export function createVocabsBatch(data) {
  return request({
    url: '/vocab/batch',
    method: 'post',
    data
  })
}

/**
 * 批量删除词汇
 */
export function deleteVocabsBatch(ids) {
  return request({
    url: '/vocab/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取词汇统计信息
 */
export function getVocabStatistics() {
  return request({
    url: '/vocab/statistics',
    method: 'get'
  })
}

// ==================== 词汇主题管理 API ====================

/**
 * 创建词汇主题
 */
export function createVocabTopic(data) {
  return request({
    url: '/vocab/topics',
    method: 'post',
    data
  })
}

/**
 * 获取词汇主题详情
 */
export function getVocabTopicById(id) {
  return request({
    url: `/vocab/topics/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询词汇主题
 */
export function getVocabTopics(params) {
  return request({
    url: '/vocab/topics',
    method: 'get',
    params
  })
}

/**
 * 更新词汇主题
 */
export function updateVocabTopic(id, data) {
  return request({
    url: `/vocab/topics/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除词汇主题
 */
export function deleteVocabTopic(id) {
  return request({
    url: `/vocab/topics/${id}`,
    method: 'delete'
  })
}

/**
 * 根据代码查找主题
 */
export function getVocabTopicByCode(code) {
  return request({
    url: `/vocab/topics/by-code/${code}`,
    method: 'get'
  })
}

/**
 * 根据父主题ID获取子主题
 */
export function getVocabTopicsByParentId(parentId) {
  return request({
    url: `/vocab/topics/by-parent/${parentId}`,
    method: 'get'
  })
}

/**
 * 获取根主题列表
 */
export function getRootVocabTopics() {
  return request({
    url: '/vocab/topics/roots',
    method: 'get'
  })
}

/**
 * 获取主题层级结构
 */
export function getVocabTopicHierarchy() {
  return request({
    url: '/vocab/topics/hierarchy',
    method: 'get'
  })
}

/**
 * 搜索主题
 */
export function searchVocabTopics(params) {
  return request({
    url: '/vocab/topics/search',
    method: 'get',
    params
  })
}

/**
 * 批量创建主题
 */
export function createVocabTopicsBatch(data) {
  return request({
    url: '/vocab/topics/batch',
    method: 'post',
    data
  })
}

/**
 * 批量删除主题
 */
export function deleteVocabTopicsBatch(ids) {
  return request({
    url: '/vocab/topics/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取主题统计信息
 */
export function getVocabTopicStatistics() {
  return request({
    url: '/vocab/topics/statistics',
    method: 'get'
  })
}

// ==================== 词汇主题映射管理 API ====================

/**
 * 创建词汇主题映射
 */
export function createVocabTopicMapping(data) {
  return request({
    url: '/vocab/mappings',
    method: 'post',
    data
  })
}

/**
 * 获取映射详情
 */
export function getVocabTopicMappingById(id) {
  return request({
    url: `/vocab/mappings/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询映射
 */
export function getVocabTopicMappings(params) {
  return request({
    url: '/vocab/mappings',
    method: 'get',
    params
  })
}

/**
 * 更新映射
 */
export function updateVocabTopicMapping(id, data) {
  return request({
    url: `/vocab/mappings/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除映射
 */
export function deleteVocabTopicMapping(id) {
  return request({
    url: `/vocab/mappings/${id}`,
    method: 'delete'
  })
}

/**
 * 根据词汇ID获取映射
 */
export function getVocabTopicMappingsByVocabId(vocabId) {
  return request({
    url: `/vocab/mappings/by-vocab/${vocabId}`,
    method: 'get'
  })
}

/**
 * 根据主题ID获取映射
 */
export function getVocabTopicMappingsByTopicId(topicId) {
  return request({
    url: `/vocab/mappings/by-topic/${topicId}`,
    method: 'get'
  })
}

/**
 * 获取词汇的主归属主题
 */
export function getPrimaryTopicByVocabId(vocabId) {
  return request({
    url: `/vocab/mappings/primary-topic/${vocabId}`,
    method: 'get'
  })
}

/**
 * 设置词汇的主归属主题
 */
export function setPrimaryTopic(vocabId, topicId) {
  return request({
    url: `/vocab/mappings/set-primary/${vocabId}/${topicId}`,
    method: 'post'
  })
}

/**
 * 批量添加词汇到主题
 */
export function addVocabsToTopic(topicId, vocabIds, isPrimary = false) {
  return request({
    url: `/vocab/mappings/add-vocabs-to-topic/${topicId}`,
    method: 'post',
    params: { isPrimary },
    data: vocabIds
  })
}

/**
 * 批量从主题移除词汇
 */
export function removeVocabsFromTopic(topicId, vocabIds) {
  return request({
    url: `/vocab/mappings/remove-vocabs-from-topic/${topicId}`,
    method: 'delete',
    data: vocabIds
  })
}

/**
 * 批量添加主题到词汇
 */
export function addTopicsToVocab(vocabId, topicIds, primaryTopicId = null) {
  return request({
    url: `/vocab/mappings/add-topics-to-vocab/${vocabId}`,
    method: 'post',
    params: { primaryTopicId },
    data: topicIds
  })
}

/**
 * 批量从词汇移除主题
 */
export function removeTopicsFromVocab(vocabId, topicIds) {
  return request({
    url: `/vocab/mappings/remove-topics-from-vocab/${vocabId}`,
    method: 'delete',
    data: topicIds
  })
}

/**
 * 获取映射统计信息
 */
export function getVocabTopicMappingStatistics() {
  return request({
    url: '/vocab/mappings/statistics',
    method: 'get'
  })
}

/**
 * 检查数据完整性
 */
export function checkVocabDataIntegrity() {
  return request({
    url: '/vocab/mappings/integrity-check',
    method: 'get'
  })
}

/**
 * 修复数据完整性
 */
export function repairVocabDataIntegrity() {
  return request({
    url: '/vocab/mappings/repair-integrity',
    method: 'post'
  })
}

// ==================== 缓存管理 API ====================

/**
 * 清除所有缓存
 */
export function clearAllVocabCache() {
  return request({
    url: '/vocab/cache/clear-all',
    method: 'post'
  })
}

/**
 * 清除词汇缓存
 */
export function clearVocabCache() {
  return request({
    url: '/vocab/cache/clear-vocabs',
    method: 'post'
  })
}

/**
 * 清除主题缓存
 */
export function clearVocabTopicCache() {
  return request({
    url: '/vocab/cache/clear-topics',
    method: 'post'
  })
}

/**
 * 清除映射缓存
 */
export function clearVocabMappingCache() {
  return request({
    url: '/vocab/cache/clear-mappings',
    method: 'post'
  })
}

// ==================== 工具方法 API ====================

/**
 * 获取CEFR等级选项
 */
export function getCEFRLevels() {
  return request({
    url: '/vocab/cefr-levels',
    method: 'get'
  })
}

/**
 * 获取词性选项
 */
export function getPOSOptions() {
  return request({
    url: '/vocab/pos-options',
    method: 'get'
  })
}

/**
 * 获取频率等级选项
 */
export function getFrequencyLevels() {
  return request({
    url: '/vocab/frequency-levels',
    method: 'get'
  })
}

/**
 * 获取难度等级选项
 */
export function getDifficultyLevels() {
  return request({
    url: '/vocab/difficulty-levels',
    method: 'get'
  })
}

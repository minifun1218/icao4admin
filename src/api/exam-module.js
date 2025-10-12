import request from '@/utils/request'

/**
 * 考试模块管理API
 */

// ==================== 基础CRUD操作 ====================

/**
 * 创建考试模块
 * @param {Object} data - 模块数据
 * @param {number} data.examPaperId - 试卷ID
 * @param {string} data.moduleType - 模块类型
 * @param {number} data.displayOrder - 显示顺序
 * @param {string} data.configJson - 配置JSON
 * @param {number} data.score - 分数
 */
export function createExamModule(data) {
  return request({
    url: '/exam-modules',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取考试模块
 * @param {number} id - 模块ID
 */
export function getExamModuleById(id) {
  return request({
    url: `/exam-modules/${id}`,
    method: 'get'
  })
}

/**
 * 分页获取所有考试模块
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.sortBy - 排序字段
 * @param {string} params.sortDir - 排序方向
 */
export function getExamModules(params) {
  return request({
    url: '/exam-modules',
    method: 'get',
    params
  })
}

/**
 * 根据试卷ID获取模块列表
 * @param {number} examPaperId - 试卷ID
 * @param {boolean} ordered - 是否按顺序排列
 */
export function getModulesByExamPaperId(examPaperId, ordered = true) {
  return request({
    url: `/exam-modules/exam-paper/${examPaperId}`,
    method: 'get',
    params: { ordered }
  })
}

/**
 * 根据模块类型获取模块列表
 * @param {string} moduleType - 模块类型
 * @param {Object} params - 分页参数
 */
export function getModulesByType(moduleType, params) {
  return request({
    url: `/exam-modules/type/${moduleType}`,
    method: 'get',
    params
  })
}

/**
 * 更新考试模块配置
 * @param {number} id - 模块ID
 * @param {string} configJson - 配置JSON
 */
export function updateModuleConfig(id, configJson) {
  return request({
    url: `/exam-modules/${id}/config`,
    method: 'put',
    data: { configJson }
  })
}

/**
 * 更新模块显示顺序
 * @param {number} id - 模块ID
 * @param {number} displayOrder - 显示顺序
 */
export function updateDisplayOrder(id, displayOrder) {
  return request({
    url: `/exam-modules/${id}/display-order`,
    method: 'put',
    data: { displayOrder }
  })
}

/**
 * 更新模块分数
 * @param {number} id - 模块ID
 * @param {number} score - 分数
 */
export function updateModuleScore(id, score) {
  return request({
    url: `/exam-modules/${id}/score`,
    method: 'put',
    data: { score }
  })
}

/**
 * 删除考试模块
 * @param {number} id - 模块ID
 */
export function deleteExamModule(id) {
  return request({
    url: `/exam-modules/${id}`,
    method: 'delete'
  })
}

// ==================== 高级功能 ====================

/**
 * 复制考试模块
 * @param {number} id - 模块ID
 */
export function copyExamModule(id) {
  return request({
    url: `/exam-modules/${id}/copy`,
    method: 'post'
  })
}

/**
 * 复制模块到其他试卷
 * @param {number} id - 模块ID
 * @param {number} targetExamPaperId - 目标试卷ID
 */
export function copyModuleToExamPaper(id, targetExamPaperId) {
  return request({
    url: `/exam-modules/${id}/copy-to-paper`,
    method: 'post',
    data: { targetExamPaperId }
  })
}

/**
 * 重新排序模块
 * @param {number} examPaperId - 试卷ID
 * @param {Array<number>} moduleIds - 模块ID列表
 */
export function reorderModules(examPaperId, moduleIds) {
  return request({
    url: `/exam-modules/exam-paper/${examPaperId}/reorder`,
    method: 'put',
    data: moduleIds
  })
}

/**
 * 激活/停用模块
 * @param {number} id - 模块ID
 * @param {boolean} isActivate - 是否激活
 */
export function toggleModuleActivation(id, isActivate) {
  return request({
    url: `/exam-modules/${id}/toggle-activation`,
    method: 'put',
    data: { isActivate }
  })
}

// ==================== 查询和统计 ====================

/**
 * 获取试卷的活跃模块
 * @param {number} examPaperId - 试卷ID
 */
export function getActiveModules(examPaperId) {
  return request({
    url: `/exam-modules/exam-paper/${examPaperId}/active`,
    method: 'get'
  })
}

/**
 * 获取试卷的非活跃模块
 * @param {number} examPaperId - 试卷ID
 */
export function getInactiveModules(examPaperId) {
  return request({
    url: `/exam-modules/exam-paper/${examPaperId}/inactive`,
    method: 'get'
  })
}

/**
 * 获取模块类型分布统计
 */
export function getModuleTypeDistribution() {
  return request({
    url: '/exam-modules/statistics/type-distribution',
    method: 'get'
  })
}

/**
 * 获取模块统计信息
 */
export function getModuleStatistics() {
  return request({
    url: '/exam-modules/statistics',
    method: 'get'
  })
}


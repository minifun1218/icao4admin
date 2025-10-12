import request from '@/utils/request'

/**
 * 考试记录管理API
 */

// ==================== 基础CRUD操作 ====================

/**
 * 创建考试记录
 * @param {Object} data - 记录数据
 * @param {number} data.examPaperId - 试卷ID
 * @param {number} data.userId - 用户ID
 */
export function createExamRecord(data) {
  return request({
    url: '/exam-records',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取考试记录
 * @param {number} id - 记录ID
 */
export function getExamRecordById(id) {
  return request({
    url: `/exam-records/${id}`,
    method: 'get'
  })
}

/**
 * 根据用户ID获取考试记录
 * @param {number} userId - 用户ID
 * @param {Object} params - 分页参数
 */
export function getExamRecordsByUserId(userId, params) {
  return request({
    url: `/exam-records/user/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 根据试卷ID获取考试记录
 * @param {number} examPaperId - 试卷ID
 * @param {Object} params - 分页参数
 */
export function getExamRecordsByExamPaperId(examPaperId, params) {
  return request({
    url: `/exam-records/exam-paper/${examPaperId}`,
    method: 'get',
    params
  })
}

/**
 * 获取用户在指定试卷的考试记录
 * @param {number} userId - 用户ID
 * @param {number} examPaperId - 试卷ID
 * @param {Object} params - 分页参数
 */
export function getUserExamRecordsByPaper(userId, examPaperId, params) {
  return request({
    url: `/exam-records/user/${userId}/exam-paper/${examPaperId}`,
    method: 'get',
    params
  })
}

/**
 * 分页获取所有考试记录
 * @param {Object} params - 查询参数
 */
export function getAllExamRecords(params) {
  return request({
    url: '/exam-records',
    method: 'get',
    params
  })
}

/**
 * 根据时间范围获取考试记录
 * @param {string} startTime - 开始时间
 * @param {string} endTime - 结束时间
 * @param {Object} params - 分页参数
 */
export function getExamRecordsByDateRange(startTime, endTime, params) {
  return request({
    url: '/exam-records/date-range',
    method: 'get',
    params: {
      startTime,
      endTime,
      ...params
    }
  })
}

/**
 * 删除考试记录
 * @param {number} id - 记录ID
 */
export function deleteExamRecord(id) {
  return request({
    url: `/exam-records/${id}`,
    method: 'delete'
  })
}

// ==================== 考试流程管理 ====================

/**
 * 开始考试
 * @param {number} id - 记录ID
 */
export function startExam(id) {
  return request({
    url: `/exam-records/${id}/start`,
    method: 'post'
  })
}

/**
 * 完成考试
 * @param {number} id - 记录ID
 */
export function finishExam(id) {
  return request({
    url: `/exam-records/${id}/finish`,
    method: 'post'
  })
}

/**
 * 更新考试状态
 * @param {number} id - 记录ID
 * @param {boolean} isFinished - 是否完成
 */
export function updateExamStatus(id, isFinished) {
  return request({
    url: `/exam-records/${id}/status`,
    method: 'put',
    data: { isFinished }
  })
}

// ==================== 查询和统计 ====================

/**
 * 获取用户最近的考试记录
 * @param {number} userId - 用户ID
 * @param {number} limit - 数量限制
 */
export function getRecentExamRecords(userId, limit = 10) {
  return request({
    url: `/exam-records/user/${userId}/recent`,
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取用户指定试卷的最新考试记录
 * @param {number} userId - 用户ID
 * @param {number} examPaperId - 试卷ID
 */
export function getLatestExamRecord(userId, examPaperId) {
  return request({
    url: `/exam-records/user/${userId}/exam-paper/${examPaperId}/latest`,
    method: 'get'
  })
}

/**
 * 获取最热门的试卷统计
 * @param {number} limit - 数量限制
 */
export function getMostPopularExamPapers(limit = 10) {
  return request({
    url: '/exam-records/statistics/popular-papers',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取最活跃的用户统计
 * @param {number} limit - 数量限制
 */
export function getMostActiveUsers(limit = 10) {
  return request({
    url: '/exam-records/statistics/active-users',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取每日考试统计
 * @param {string} startDate - 开始日期
 * @param {string} endDate - 结束日期
 */
export function getDailyExamStats(startDate, endDate) {
  return request({
    url: '/exam-records/statistics/daily',
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 获取每月考试统计
 */
export function getMonthlyExamStats() {
  return request({
    url: '/exam-records/statistics/monthly',
    method: 'get'
  })
}

/**
 * 获取考试统计报告
 * @param {string} startDate - 开始日期
 * @param {string} endDate - 结束日期
 */
export function getExamStatisticsReport(startDate, endDate) {
  return request({
    url: '/exam-records/statistics/report',
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 获取基础统计信息
 */
export function getBasicStatistics() {
  return request({
    url: '/exam-records/statistics/basic',
    method: 'get'
  })
}

// ==================== 辅助方法 ====================

/**
 * 格式化考试状态
 * @param {boolean} isFinished - 是否完成
 */
export function formatExamStatus(isFinished) {
  return isFinished ? '已完成' : '进行中'
}

/**
 * 获取考试状态类型（用于el-tag的type属性）
 * @param {boolean} isFinished - 是否完成
 */
export function getExamStatusType(isFinished) {
  return isFinished ? 'success' : 'warning'
}

/**
 * 格式化日期时间
 * @param {string} dateString - 日期字符串
 */
export function formatDateTime(dateString) {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

/**
 * 计算考试时长
 * @param {string} startTime - 开始时间
 * @param {string} endTime - 结束时间
 */
export function calculateExamDuration(startTime, endTime) {
  if (!startTime || !endTime) return '-'
  
  const start = new Date(startTime)
  const end = new Date(endTime)
  const diffMs = end - start
  
  if (diffMs < 0) return '-'
  
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const hours = Math.floor(diffMinutes / 60)
  const minutes = diffMinutes % 60
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

/**
 * 验证考试记录数据
 * @param {Object} record - 考试记录数据
 */
export function validateExamRecordData(record) {
  const errors = []
  
  if (!record.examPaperId) {
    errors.push('试卷ID不能为空')
  }
  
  if (!record.userId) {
    errors.push('用户ID不能为空')
  }
  
  return errors
}


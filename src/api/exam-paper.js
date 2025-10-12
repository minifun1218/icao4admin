import request from '@/utils/request'

/**
 * 考试试卷管理API
 */

// ==================== 基础CRUD操作 ====================

/**
 * 创建考试试卷
 * @param {Object} data - 试卷数据
 * @param {string} data.code - 试卷代码
 * @param {string} data.name - 试卷名称
 * @param {number} data.totalDurationMin - 总时长（分钟）
 * @param {string} data.description - 描述
 */
export function createExamPaper(data) {
  return request({
    url: '/exam-papers',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取考试试卷
 * @param {number} id - 试卷ID
 */
export function getExamPaperById(id) {
  return request({
    url: `/exam-papers/${id}`,
    method: 'get'
  })
}

/**
 * 根据代码获取考试试卷
 * @param {string} code - 试卷代码
 */
export function getExamPaperByCode(code) {
  return request({
    url: `/exam-papers/code/${code}`,
    method: 'get'
  })
}

/**
 * 分页获取所有考试试卷
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.sortBy - 排序字段
 * @param {string} params.sortDir - 排序方向
 */
export function getExamPapers(params) {
  return request({
    url: '/exam-papers',
    method: 'get',
    params
  })
}

/**
 * 搜索考试试卷
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 分页参数
 */
export function searchExamPapers(keyword, params) {
  return request({
    url: '/exam-papers/search',
    method: 'get',
    params: {
      keyword,
      ...params
    }
  })
}

/**
 * 更新考试试卷
 * @param {number} id - 试卷ID
 * @param {Object} data - 更新数据
 */
export function updateExamPaper(id, data) {
  return request({
    url: `/exam-papers/${id}`,
    method: 'put',
    data
  })
}

/**
 * 更新试卷代码
 * @param {number} id - 试卷ID
 * @param {string} code - 新代码
 */
export function updateExamPaperCode(id, code) {
  return request({
    url: `/exam-papers/${id}/code`,
    method: 'put',
    data: { code }
  })
}

/**
 * 删除考试试卷
 * @param {number} id - 试卷ID
 */
export function deleteExamPaper(id) {
  return request({
    url: `/exam-papers/${id}`,
    method: 'delete'
  })
}

// ==================== 高级功能 ====================

/**
 * 复制考试试卷
 * @param {number} id - 试卷ID
 * @param {string} code - 新试卷代码
 * @param {string} name - 新试卷名称
 */
export function copyExamPaper(id, code, name) {
  return request({
    url: `/exam-papers/${id}/copy`,
    method: 'post',
    data: { code, name }
  })
}

/**
 * 生成试卷代码
 */
export function generateExamPaperCode() {
  return request({
    url: '/exam-papers/generate-code',
    method: 'get'
  })
}

/**
 * 发布试卷
 * @param {number} id - 试卷ID
 */
export function publishExamPaper(id) {
  return request({
    url: `/exam-papers/${id}/publish`,
    method: 'post'
  })
}

/**
 * 下架试卷
 * @param {number} id - 试卷ID
 */
export function unpublishExamPaper(id) {
  return request({
    url: `/exam-papers/${id}/unpublish`,
    method: 'post'
  })
}

// ==================== 统计和报告 ====================

/**
 * 获取试卷统计信息
 * @param {number} id - 试卷ID
 */
export function getExamPaperStatistics(id) {
  return request({
    url: `/exam-papers/${id}/statistics`,
    method: 'get'
  })
}

/**
 * 获取最近创建的试卷
 * @param {number} limit - 数量限制
 */
export function getRecentExamPapers(limit = 10) {
  return request({
    url: '/exam-papers/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取最受欢迎的试卷
 * @param {number} limit - 数量限制
 */
export function getPopularExamPapers(limit = 10) {
  return request({
    url: '/exam-papers/popular',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取推荐试卷
 * @param {number} userId - 用户ID
 * @param {number} limit - 数量限制
 */
export function getRecommendedExamPapers(userId, limit = 5) {
  return request({
    url: '/exam-papers/recommended',
    method: 'get',
    params: { userId, limit }
  })
}

// ==================== 辅助方法 ====================

/**
 * 格式化时长显示
 * @param {number} minutes - 分钟数
 */
export function formatDuration(minutes) {
  if (!minutes) return '-'
  
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  
  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60
  
  if (remainingMinutes === 0) {
    return `${hours}小时`
  }
  
  return `${hours}小时${remainingMinutes}分钟`
}

/**
 * 验证试卷数据
 * @param {Object} paper - 试卷数据
 */
export function validateExamPaperData(paper) {
  const errors = []
  
  if (!paper.code || paper.code.trim() === '') {
    errors.push('试卷代码不能为空')
  }
  
  if (paper.code && paper.code.length > 100) {
    errors.push('试卷代码长度不能超过100个字符')
  }
  
  if (!paper.name || paper.name.trim() === '') {
    errors.push('试卷名称不能为空')
  }
  
  if (paper.name && paper.name.length > 200) {
    errors.push('试卷名称长度不能超过200个字符')
  }
  
  if (!paper.totalDurationMin || paper.totalDurationMin <= 0) {
    errors.push('考试时长必须大于0')
  }
  
  if (paper.totalDurationMin && paper.totalDurationMin > 1440) {
    errors.push('考试时长不能超过1440分钟（24小时）')
  }
  
  return errors
}

/**
 * 生成试卷模板
 */
export function generateExamPaperTemplate() {
  return {
    id: null,
    code: '',
    name: '',
    totalDurationMin: 120,
    description: ''
  }
}

/**
 * 获取时长预设选项
 */
export function getDurationPresets() {
  return [
    { value: 30, label: '30分钟' },
    { value: 60, label: '1小时' },
    { value: 90, label: '1.5小时' },
    { value: 120, label: '2小时' },
    { value: 150, label: '2.5小时' },
    { value: 180, label: '3小时' },
    { value: 240, label: '4小时' }
  ]
}


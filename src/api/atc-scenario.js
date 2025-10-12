import request from '@/utils/request'

/**
 * 空中交通管制场景管理API
 * 基于 AtcScenario.java 实体类
 */

// ==================== 场景基础CRUD操作 ====================

/**
 * 获取所有场景（分页）
 * GET /atc-scenarios
 */
export const getScenarios = (params = {}) => {
  return request.get('/atc-scenarios', { params })
}

/**
 * 根据ID获取场景详情
 * GET /atc-scenarios/{id}
 */
export const getScenarioById = (id) => {
  return request.get(`/atc-scenarios/${id}`)
}

/**
 * 创建场景
 * POST /atc-scenarios
 */
export const createScenario = (scenarioData) => {
  return request.post('/atc-scenarios', scenarioData)
}

/**
 * 更新场景
 * PUT /atc-scenarios/{id}
 */
export const updateScenario = (id, scenarioData) => {
  return request.put(`/atc-scenarios/${id}`, scenarioData)
}

/**
 * 删除场景
 * DELETE /atc-scenarios/{id}
 */
export const deleteScenario = (id) => {
  return request.delete(`/atc-scenarios/${id}`)
}

// ==================== 场景扩展功能 ====================

/**
 * 根据机场ID获取场景
 * GET /atc-scenarios/by-airport/{airportId}
 */
export const getScenariosByAirportId = (airportId, params = {}) => {
  return request.get(`/atc-scenarios/by-airport/${airportId}`, { params })
}

/**
 * 根据模块ID获取场景
 * GET /atc-scenarios/by-module/{moduleId}
 */
export const getScenariosByModuleId = (moduleId, params = {}) => {
  return request.get(`/atc-scenarios/by-module/${moduleId}`, { params })
}

/**
 * 根据场景类型获取场景列表
 * GET /atc-scenarios/by-type/{scenarioType}
 */
export const getScenariosByType = (scenarioType, params = {}) => {
  return request.get(`/atc-scenarios/by-type/${scenarioType}`, { params })
}

/**
 * 根据难度等级获取场景列表
 * GET /atc-scenarios/by-difficulty/{difficultyLevel}
 */
export const getScenariosByDifficulty = (difficultyLevel, params = {}) => {
  return request.get(`/atc-scenarios/by-difficulty/${difficultyLevel}`, { params })
}

/**
 * 搜索场景
 * GET /atc-scenarios/search
 */
export const searchScenarios = (keyword, params = {}) => {
  return request.get('/atc-scenarios/search', {
    params: { keyword, ...params }
  })
}

/**
 * 激活/停用场景
 * PUT /atc-scenarios/{id}/toggle-active
 */
export const toggleScenarioActive = (id) => {
  return request.put(`/atc-scenarios/${id}/toggle-active`)
}

/**
 * 复制场景
 * POST /atc-scenarios/{id}/copy
 */
export const copyScenario = (id) => {
  return request.post(`/atc-scenarios/${id}/copy`)
}

// ==================== 轮次管理相关接口 ====================

/**
 * 获取场景的轮次列表
 * GET /atc-scenarios/{scenarioId}/turns
 */
export const getTurnsByScenarioId = (scenarioId, params = {}) => {
  return request.get(`/atc-scenarios/${scenarioId}/turns`, { params })
}

/**
 * 为场景添加轮次
 * POST /atc-scenarios/{scenarioId}/turns
 */
export const addTurnToScenario = (scenarioId, turnData) => {
  return request.post(`/atc-scenarios/${scenarioId}/turns`, turnData)
}

/**
 * 更新场景轮次
 * PUT /atc-scenarios/{scenarioId}/turns/{turnId}
 */
export const updateScenarioTurn = (scenarioId, turnId, turnData) => {
  return request.put(`/atc-scenarios/${scenarioId}/turns/${turnId}`, turnData)
}

/**
 * 删除场景轮次
 * DELETE /atc-scenarios/{scenarioId}/turns/{turnId}
 */
export const deleteScenarioTurn = (scenarioId, turnId) => {
  return request.delete(`/atc-scenarios/${scenarioId}/turns/${turnId}`)
}

// ==================== 批量操作接口 ====================

/**
 * 批量删除场景
 * DELETE /atc-scenarios/batch
 */
export const batchDeleteScenarios = (scenarioIds) => {
  return request.delete('/atc-scenarios/batch', { data: scenarioIds })
}

/**
 * 批量激活场景
 * PUT /atc-scenarios/batch-activate
 */
export const batchActivateScenarios = (scenarioIds) => {
  return request.put('/atc-scenarios/batch-activate', scenarioIds)
}

/**
 * 批量停用场景
 * PUT /atc-scenarios/batch-deactivate
 */
export const batchDeactivateScenarios = (scenarioIds) => {
  return request.put('/atc-scenarios/batch-deactivate', scenarioIds)
}

/**
 * 批量操作场景
 * POST /atc-scenarios/batch-operation
 */
export const batchOperateScenarios = (operation, scenarioIds, data = {}) => {
  return request.post('/atc-scenarios/batch-operation', {
    operation,
    scenarioIds,
    data
  })
}

// ==================== 统计相关接口 ====================

/**
 * 获取场景统计信息
 * GET /atc-scenarios/statistics
 */
export const getScenarioStatistics = () => {
  return request.get('/atc-scenarios/statistics')
}

/**
 * 获取机场场景统计
 * GET /atc-scenarios/statistics/by-airport
 */
export const getAirportScenarioStatistics = () => {
  return request.get('/atc-scenarios/statistics/by-airport')
}

/**
 * 获取场景类型统计
 * GET /atc-scenarios/statistics/by-type
 */
export const getScenarioTypeStatistics = () => {
  return request.get('/atc-scenarios/statistics/by-type')
}

/**
 * 获取难度等级统计
 * GET /atc-scenarios/statistics/by-difficulty
 */
export const getDifficultyStatistics = () => {
  return request.get('/atc-scenarios/statistics/by-difficulty')
}

// ==================== 导入导出接口 ====================

/**
 * 导出场景数据
 * GET /atc-scenarios/export
 */
export const exportScenarios = (params = {}) => {
  return request.get('/atc-scenarios/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 批量导入场景
 * POST /atc-scenarios/import
 */
export const importScenarios = (file, options = {}) => {
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
  
  return request.post('/atc-scenarios/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出场景模板
 * GET /atc-scenarios/export/template
 */
export const exportScenarioTemplate = () => {
  return request.get('/atc-scenarios/export/template', {
    responseType: 'blob'
  })
}

/**
 * 验证导入文件
 * POST /atc-scenarios/import/validate
 */
export const validateImportFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/atc-scenarios/import/validate', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ==================== 高级搜索接口 ====================

/**
 * 高级搜索场景
 * POST /atc-scenarios/advanced-search
 */
export const advancedSearchScenarios = (criteria) => {
  return request.post('/atc-scenarios/advanced-search', criteria)
}

/**
 * 全文搜索场景
 * GET /atc-scenarios/search/fulltext
 */
export const fullTextSearchScenarios = (query, params = {}) => {
  return request.get('/atc-scenarios/search/fulltext', {
    params: { query, ...params }
  })
}

/**
 * 获取相似场景推荐
 * GET /atc-scenarios/{scenarioId}/similar
 */
export const getSimilarScenarios = (scenarioId, limit = 10) => {
  return request.get(`/atc-scenarios/${scenarioId}/similar`, {
    params: { limit }
  })
}

// ==================== 工具函数 ====================

/**
 * 验证场景数据
 */
export const validateScenarioData = (scenario) => {
  const errors = []
  
  if (!scenario.title || scenario.title.trim() === '') {
    errors.push('场景标题不能为空')
  }
  
  if (scenario.title && scenario.title.length > 255) {
    errors.push('场景标题长度不能超过255个字符')
  }
  
  if (scenario.description && scenario.description.length > 2000) {
    errors.push('场景描述长度不能超过2000个字符')
  }
  
  if (!scenario.airportId) {
    errors.push('必须选择机场')
  }
  
  if (!scenario.moduleId) {
    errors.push('必须选择模块')
  }
  
  if (scenario.scenarioType && scenario.scenarioType.length > 50) {
    errors.push('场景类型长度不能超过50个字符')
  }
  
  if (scenario.difficultyLevel && (scenario.difficultyLevel < 1 || scenario.difficultyLevel > 5)) {
    errors.push('难度等级必须在1-5之间')
  }
  
  if (scenario.estimatedDuration && (scenario.estimatedDuration < 1 || scenario.estimatedDuration > 180)) {
    errors.push('预计持续时间必须在1-180分钟之间')
  }
  
  return errors
}

/**
 * 生成场景模板
 */
export const generateScenarioTemplate = () => {
  return {
    id: null,
    airportId: null,
    moduleId: null,
    title: '',
    description: '',
    scenarioType: '',
    difficultyLevel: 1,
    estimatedDuration: 30,
    isActive: true,
    createdAt: null,
    updatedAt: null
  }
}

/**
 * 格式化持续时间显示
 */
export const formatDuration = (minutes) => {
  if (!minutes) return '未设置'
  
  if (minutes < 60) {
    return `${minutes}分钟`
  } else {
    const hours = Math.floor(minutes / 60)
    const remainingMinutes = minutes % 60
    if (remainingMinutes === 0) {
      return `${hours}小时`
    } else {
      return `${hours}小时${remainingMinutes}分钟`
    }
  }
}

/**
 * 获取难度等级描述
 */
export const getDifficultyDescription = (difficultyLevel) => {
  if (!difficultyLevel) return '未设置'
  
  switch (difficultyLevel) {
    case 1: return '初级'
    case 2: return '初中级'
    case 3: return '中级'
    case 4: return '中高级'
    case 5: return '高级'
    default: return '未知'
  }
}

/**
 * 获取场景类型描述
 */
export const getScenarioTypeDescription = (scenarioType) => {
  if (!scenarioType) return '未设置'
  
  const typeMap = {
    'APPROACH': '进近',
    'DEPARTURE': '离场',
    'GROUND': '地面',
    'TOWER': '塔台',
    'RADAR': '雷达',
    'EMERGENCY': '紧急情况',
    'COMPLEX': '复杂场景',
    'TRAINING': '训练场景'
  }
  
  return typeMap[scenarioType.toUpperCase()] || scenarioType
}

/**
 * 获取场景类型选项
 */
export const getScenarioTypeOptions = () => {
  return [
    { value: 'APPROACH', label: '进近' },
    { value: 'DEPARTURE', label: '离场' },
    { value: 'GROUND', label: '地面' },
    { value: 'TOWER', label: '塔台' },
    { value: 'RADAR', label: '雷达' },
    { value: 'EMERGENCY', label: '紧急情况' },
    { value: 'COMPLEX', label: '复杂场景' },
    { value: 'TRAINING', label: '训练场景' }
  ]
}

/**
 * 获取难度等级选项
 */
export const getDifficultyLevelOptions = () => {
  return [
    { value: 1, label: '初级' },
    { value: 2, label: '初中级' },
    { value: 3, label: '中级' },
    { value: 4, label: '中高级' },
    { value: 5, label: '高级' }
  ]
}

/**
 * 获取持续时间预设选项
 */
export const getDurationPresets = () => {
  return [
    { value: 15, label: '15分钟' },
    { value: 30, label: '30分钟' },
    { value: 45, label: '45分钟' },
    { value: 60, label: '1小时' },
    { value: 90, label: '1.5小时' },
    { value: 120, label: '2小时' },
    { value: 180, label: '3小时' }
  ]
}

/**
 * 检查场景是否完整
 */
export const isScenarioComplete = (scenario) => {
  return scenario.title && scenario.title.trim() !== '' &&
         scenario.scenarioType && scenario.scenarioType.trim() !== '' &&
         scenario.difficultyLevel && scenario.difficultyLevel >= 1 && scenario.difficultyLevel <= 5 &&
         scenario.estimatedDuration && scenario.estimatedDuration > 0 &&
         scenario.airportId && scenario.moduleId
}

/**
 * 获取场景复杂度评级
 */
export const getScenarioComplexity = (scenario) => {
  let score = 0
  
  // 基于难度等级
  if (scenario.difficultyLevel) {
    score += scenario.difficultyLevel * 20
  }
  
  // 基于持续时间
  if (scenario.estimatedDuration) {
    if (scenario.estimatedDuration > 120) score += 20
    else if (scenario.estimatedDuration > 60) score += 15
    else if (scenario.estimatedDuration > 30) score += 10
    else score += 5
  }
  
  // 基于场景类型
  const complexTypes = ['EMERGENCY', 'COMPLEX', 'RADAR']
  if (scenario.scenarioType && complexTypes.includes(scenario.scenarioType.toUpperCase())) {
    score += 15
  }
  
  if (score >= 80) return { level: 'high', label: '高复杂度', color: '#f56c6c' }
  if (score >= 60) return { level: 'medium', label: '中复杂度', color: '#e6a23c' }
  return { level: 'low', label: '低复杂度', color: '#67c23a' }
}

/**
 * 格式化场景摘要
 */
export const formatScenarioSummary = (scenario) => {
  const parts = []
  
  if (scenario.title) parts.push(scenario.title)
  if (scenario.scenarioType) parts.push(getScenarioTypeDescription(scenario.scenarioType))
  if (scenario.difficultyLevel) parts.push(getDifficultyDescription(scenario.difficultyLevel))
  if (scenario.estimatedDuration) parts.push(formatDuration(scenario.estimatedDuration))
  
  return parts.join(' | ')
}

/**
 * 生成排序选项
 */
export const getSortOptions = () => {
  return [
    { value: 'title', label: '场景标题' },
    { value: 'scenarioType', label: '场景类型' },
    { value: 'difficultyLevel', label: '难度等级' },
    { value: 'estimatedDuration', label: '持续时间' },
    { value: 'createdAt', label: '创建时间' },
    { value: 'updatedAt', label: '更新时间' }
  ]
}

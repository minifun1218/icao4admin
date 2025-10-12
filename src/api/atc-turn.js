import request from '@/utils/request'

/**
 * 空中交通管制轮次管理API
 * 基于 AtcTurn.java 实体类
 */

// ==================== 轮次基础CRUD操作 ====================

/**
 * 获取所有轮次（分页）
 * GET /atc-turns
 */
export const getTurns = (params = {}) => {
  return request.get('/atc-turns', { params })
}

/**
 * 根据ID获取轮次详情
 * GET /atc-turns/{id}
 */
export const getTurnById = (id) => {
  return request.get(`/atc-turns/${id}`)
}

/**
 * 创建轮次
 * POST /atc-turns
 */
export const createTurn = (turnData) => {
  return request.post('/atc-turns', turnData)
}

/**
 * 更新轮次
 * PUT /atc-turns/{id}
 */
export const updateTurn = (id, turnData) => {
  return request.put(`/atc-turns/${id}`, turnData)
}

/**
 * 删除轮次
 * DELETE /atc-turns/{id}
 */
export const deleteTurn = (id) => {
  return request.delete(`/atc-turns/${id}`)
}

// ==================== 轮次扩展功能 ====================

/**
 * 根据场景ID获取轮次列表
 * GET /atc-turns/by-scenario/{scenarioId}
 */
export const getTurnsByScenarioId = (scenarioId, params = {}) => {
  return request.get(`/atc-turns/by-scenario/${scenarioId}`, { params })
}

/**
 * 根据说话者类型获取轮次列表
 * GET /atc-turns/by-speaker/{speakerType}
 */
export const getTurnsBySpeakerType = (speakerType, params = {}) => {
  return request.get(`/atc-turns/by-speaker/${speakerType}`, { params })
}

/**
 * 根据难度等级获取轮次列表
 * GET /atc-turns/by-difficulty/{difficultyLevel}
 */
export const getTurnsByDifficulty = (difficultyLevel, params = {}) => {
  return request.get(`/atc-turns/by-difficulty/${difficultyLevel}`, { params })
}

/**
 * 搜索轮次
 * GET /atc-turns/search
 */
export const searchTurns = (keyword, params = {}) => {
  return request.get('/atc-turns/search', {
    params: { keyword, ...params }
  })
}

/**
 * 激活/停用轮次
 * PUT /atc-turns/{id}/toggle-active
 */
export const toggleTurnActive = (id) => {
  return request.put(`/atc-turns/${id}/toggle-active`)
}

/**
 * 复制轮次
 * POST /atc-turns/{id}/copy
 */
export const copyTurn = (id) => {
  return request.post(`/atc-turns/${id}/copy`)
}

/**
 * 重置轮次数据
 * PUT /atc-turns/{id}/reset
 */
export const resetTurn = (id) => {
  return request.put(`/atc-turns/${id}/reset`)
}

// ==================== 音频文件管理 ====================

/**
 * 上传音频文件
 * POST /atc-turns/{id}/upload-audio
 */
export const uploadAudioFile = (turnId, file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post(`/atc-turns/${turnId}/upload-audio`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除音频文件
 * DELETE /atc-turns/{id}/audio
 */
export const deleteAudioFile = (turnId) => {
  return request.delete(`/atc-turns/${turnId}/audio`)
}

/**
 * 获取音频文件URL
 * GET /atc-turns/{id}/audio-url
 */
export const getAudioFileUrl = (turnId) => {
  return request.get(`/atc-turns/${turnId}/audio-url`)
}

// ==================== 轮次回答管理 ====================

/**
 * 获取轮次的回答列表
 * GET /atc-turns/{turnId}/responses
 */
export const getTurnResponses = (turnId, params = {}) => {
  return request.get(`/atc-turns/${turnId}/responses`, { params })
}

/**
 * 创建轮次回答
 * POST /atc-turns/{turnId}/responses
 */
export const createTurnResponse = (turnId, responseData) => {
  return request.post(`/atc-turns/${turnId}/responses`, responseData)
}

/**
 * 评分轮次回答
 * PUT /atc-turns/{turnId}/responses/{responseId}/score
 */
export const scoreTurnResponse = (turnId, responseId, scoreData) => {
  return request.put(`/atc-turns/${turnId}/responses/${responseId}/score`, scoreData)
}

// ==================== 批量操作接口 ====================

/**
 * 批量删除轮次
 * DELETE /atc-turns/batch
 */
export const batchDeleteTurns = (turnIds) => {
  return request.delete('/atc-turns/batch', { data: turnIds })
}

/**
 * 批量激活轮次
 * PUT /atc-turns/batch-activate
 */
export const batchActivateTurns = (turnIds) => {
  return request.put('/atc-turns/batch-activate', turnIds)
}

/**
 * 批量停用轮次
 * PUT /atc-turns/batch-deactivate
 */
export const batchDeactivateTurns = (turnIds) => {
  return request.put('/atc-turns/batch-deactivate', turnIds)
}

/**
 * 批量操作轮次
 * POST /atc-turns/batch-operation
 */
export const batchOperateTurns = (operation, turnIds, data = {}) => {
  return request.post('/atc-turns/batch-operation', {
    operation,
    turnIds,
    data
  })
}

/**
 * 批量更新轮次顺序
 * PUT /atc-turns/batch-reorder
 */
export const batchReorderTurns = (turnOrders) => {
  return request.put('/atc-turns/batch-reorder', turnOrders)
}

// ==================== 统计相关接口 ====================

/**
 * 获取轮次统计信息
 * GET /atc-turns/statistics
 */
export const getTurnStatistics = () => {
  return request.get('/atc-turns/statistics')
}

/**
 * 获取场景轮次统计
 * GET /atc-turns/statistics/by-scenario/{scenarioId}
 */
export const getScenarioTurnStatistics = (scenarioId) => {
  return request.get(`/atc-turns/statistics/by-scenario/${scenarioId}`)
}

/**
 * 获取说话者类型统计
 * GET /atc-turns/statistics/by-speaker
 */
export const getSpeakerTypeStatistics = () => {
  return request.get('/atc-turns/statistics/by-speaker')
}

/**
 * 获取难度等级统计
 * GET /atc-turns/statistics/by-difficulty
 */
export const getTurnDifficultyStatistics = () => {
  return request.get('/atc-turns/statistics/by-difficulty')
}

// ==================== 导入导出接口 ====================

/**
 * 导出轮次数据
 * GET /atc-turns/export
 */
export const exportTurns = (params = {}) => {
  return request.get('/atc-turns/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 批量导入轮次
 * POST /atc-turns/import
 */
export const importTurns = (file, options = {}) => {
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
  
  return request.post('/atc-turns/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出轮次模板
 * GET /atc-turns/export/template
 */
export const exportTurnTemplate = () => {
  return request.get('/atc-turns/export/template', {
    responseType: 'blob'
  })
}

/**
 * 验证导入文件
 * POST /atc-turns/import/validate
 */
export const validateTurnImportFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/atc-turns/import/validate', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ==================== 高级搜索接口 ====================

/**
 * 高级搜索轮次
 * POST /atc-turns/advanced-search
 */
export const advancedSearchTurns = (criteria) => {
  return request.post('/atc-turns/advanced-search', criteria)
}

/**
 * 全文搜索轮次
 * GET /atc-turns/search/fulltext
 */
export const fullTextSearchTurns = (query, params = {}) => {
  return request.get('/atc-turns/search/fulltext', {
    params: { query, ...params }
  })
}

// ==================== 工具函数 ====================

/**
 * 验证轮次数据
 */
export const validateTurnData = (turn) => {
  const errors = []
  
  if (!turn.scenarioId) {
    errors.push('必须选择场景')
  }
  
  if (!turn.turnNumber || turn.turnNumber < 1) {
    errors.push('轮次序号必须大于0')
  }
  
  if (!turn.speakerType || turn.speakerType.trim() === '') {
    errors.push('说话者类型不能为空')
  }
  
  if (turn.speakerType && turn.speakerType.length > 20) {
    errors.push('说话者类型长度不能超过20个字符')
  }
  
  if (turn.audioFilePath && turn.audioFilePath.length > 500) {
    errors.push('音频文件路径长度不能超过500个字符')
  }
  
  if (turn.maxScore && turn.maxScore < 0) {
    errors.push('最大分数不能小于0')
  }
  
  if (turn.audioDuration && turn.audioDuration < 0) {
    errors.push('音频时长不能小于0')
  }
  
  if (turn.difficultyLevel && (turn.difficultyLevel < 1 || turn.difficultyLevel > 5)) {
    errors.push('难度等级必须在1-5之间')
  }
  
  return errors
}

/**
 * 生成轮次模板
 */
export const generateTurnTemplate = () => {
  return {
    id: null,
    scenarioId: null,
    turnNumber: 1,
    speakerType: 'pilot',
    audioFilePath: '',
    transcriptText: '',
    expectedResponse: '',
    scoringCriteria: '',
    maxScore: 10.0,
    audioDuration: null,
    difficultyLevel: 3,
    isRequired: true,
    isActive: true,
    createdAt: null,
    updatedAt: null
  }
}

/**
 * 格式化音频时长显示
 */
export const formatAudioDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '未知'
  
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  
  if (minutes > 0) {
    return `${minutes}分${remainingSeconds}秒`
  } else {
    return `${seconds}秒`
  }
}

/**
 * 获取说话者类型描述
 */
export const getSpeakerTypeDescription = (speakerType) => {
  if (!speakerType) return '未知'
  
  switch (speakerType.toLowerCase()) {
    case 'pilot':
      return '飞行员'
    case 'controller':
      return '管制员'
    default:
      return speakerType
  }
}

/**
 * 获取难度等级描述
 */
export const getTurnDifficultyDescription = (difficultyLevel) => {
  if (!difficultyLevel) return '未设置'
  
  switch (difficultyLevel) {
    case 1: return '非常简单'
    case 2: return '简单'
    case 3: return '中等'
    case 4: return '困难'
    case 5: return '非常困难'
    default: return '未知难度'
  }
}

/**
 * 获取说话者类型选项
 */
export const getSpeakerTypeOptions = () => {
  return [
    { value: 'pilot', label: '飞行员' },
    { value: 'controller', label: '管制员' }
  ]
}

/**
 * 获取难度等级选项
 */
export const getTurnDifficultyLevelOptions = () => {
  return [
    { value: 1, label: '非常简单' },
    { value: 2, label: '简单' },
    { value: 3, label: '中等' },
    { value: 4, label: '困难' },
    { value: 5, label: '非常困难' }
  ]
}

/**
 * 检查轮次完整性
 */
export const isTurnComplete = (turn) => {
  return turn.scenarioId &&
         turn.turnNumber &&
         turn.speakerType &&
         turn.speakerType.trim() !== '' &&
         turn.audioFilePath &&
         turn.audioFilePath.trim() !== '' &&
         turn.transcriptText &&
         turn.transcriptText.trim() !== ''
}

/**
 * 检查是否可以评分
 */
export const canTurnBeScored = (turn) => {
  return isTurnComplete(turn) &&
         turn.expectedResponse &&
         turn.expectedResponse.trim() !== '' &&
         turn.scoringCriteria &&
         turn.scoringCriteria.trim() !== '' &&
         turn.maxScore &&
         turn.maxScore > 0
}

/**
 * 获取轮次状态描述
 */
export const getTurnStatusDescription = (turn) => {
  if (!turn.isActive) {
    return '未激活'
  }
  
  if (!isTurnComplete(turn)) {
    return '不完整'
  }
  
  if (canTurnBeScored(turn)) {
    return '可评分'
  }
  
  return '已激活'
}

/**
 * 获取说话者类型标签类型
 */
export const getSpeakerTypeTagType = (speakerType) => {
  switch (speakerType?.toLowerCase()) {
    case 'pilot':
      return 'primary'
    case 'controller':
      return 'success'
    default:
      return 'info'
  }
}

/**
 * 获取难度标签类型
 */
export const getTurnDifficultyTagType = (difficultyLevel) => {
  switch (difficultyLevel) {
    case 1:
    case 2:
      return 'success'
    case 3:
      return 'warning'
    case 4:
    case 5:
      return 'danger'
    default:
      return 'info'
  }
}

/**
 * 获取状态标签类型
 */
export const getTurnStatusTagType = (turn) => {
  if (!turn.isActive) return 'danger'
  if (!isTurnComplete(turn)) return 'warning'
  if (canTurnBeScored(turn)) return 'success'
  return 'primary'
}

/**
 * 生成轮次摘要
 */
export const generateTurnSummary = (turn) => {
  const parts = []
  
  if (turn.turnNumber) parts.push(`第${turn.turnNumber}轮`)
  if (turn.speakerType) parts.push(getSpeakerTypeDescription(turn.speakerType))
  if (turn.difficultyLevel) parts.push(getTurnDifficultyDescription(turn.difficultyLevel))
  if (turn.audioDuration) parts.push(formatAudioDuration(turn.audioDuration))
  if (turn.maxScore) parts.push(`${turn.maxScore}分`)
  
  return parts.join(' | ')
}

/**
 * 计算轮次完成度
 */
export const calculateTurnCompleteness = (turn) => {
  let score = 0
  const totalFields = 8
  
  if (turn.speakerType) score += 1
  if (turn.audioFilePath) score += 1
  if (turn.transcriptText) score += 1
  if (turn.expectedResponse) score += 1
  if (turn.scoringCriteria) score += 1
  if (turn.maxScore) score += 1
  if (turn.audioDuration) score += 1
  if (turn.difficultyLevel) score += 1
  
  return Math.round((score / totalFields) * 100)
}

/**
 * 获取完成度颜色
 */
export const getCompletenessColor = (percentage) => {
  if (percentage >= 90) return '#67c23a'
  if (percentage >= 70) return '#e6a23c'
  if (percentage >= 50) return '#f56c6c'
  return '#909399'
}

/**
 * 生成排序选项
 */
export const getTurnSortOptions = () => {
  return [
    { value: 'turnNumber', label: '轮次序号' },
    { value: 'speakerType', label: '说话者类型' },
    { value: 'difficultyLevel', label: '难度等级' },
    { value: 'audioDuration', label: '音频时长' },
    { value: 'maxScore', label: '最大分数' },
    { value: 'createdAt', label: '创建时间' },
    { value: 'updatedAt', label: '更新时间' }
  ]
}

/**
 * 验证音频文件
 */
export const validateAudioFile = (file) => {
  const errors = []
  const allowedTypes = ['audio/mp3', 'audio/wav', 'audio/m4a', 'audio/ogg']
  const maxSize = 10 * 1024 * 1024 // 10MB
  
  if (!allowedTypes.includes(file.type)) {
    errors.push('只支持MP3、WAV、M4A、OGG格式的音频文件')
  }
  
  if (file.size > maxSize) {
    errors.push('音频文件大小不能超过10MB')
  }
  
  return errors
}

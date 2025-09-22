import api from './index'

// 考试结果管理相关API
export const resultApi = {
  // ==================== 考试结果基础管理 ====================
  
  // 获取考试结果列表
  getResults(params = {}) {
    return api.get('/app/result', { params })
  },

  // 根据ID获取考试结果详情
  getResultById(id) {
    return api.get(`/app/result/${id}`)
  },

  // 根据考试ID获取结果列表
  getResultsByExamId(examId, params = {}) {
    return api.get(`/app/result/exam/${examId}`, { params })
  },

  // 根据用户ID获取结果列表
  getResultsByUserId(userId, params = {}) {
    return api.get(`/app/result/user/${userId}`, { params })
  },

  // 创建考试结果
  createResult(data) {
    return api.post('/app/result', data)
  },

  // 更新考试结果
  updateResult(id, data) {
    return api.put(`/app/result/${id}`, data)
  },

  // 删除考试结果
  deleteResult(id) {
    return api.delete(`/app/result/${id}`)
  },

  // 批量删除考试结果
  batchDeleteResults(resultIds) {
    return api.delete('/app/result/batch', { data: { resultIds } })
  },

  // ==================== 考试结果评分管理 ====================
  
  // 自动评分
  autoScore(resultId) {
    return api.post(`/app/result/${resultId}/auto-score`)
  },

  // 人工评分
  manualScore(resultId, scoreData) {
    return api.post(`/app/result/${resultId}/manual-score`, scoreData)
  },

  // 重新评分
  reScore(resultId, options = {}) {
    return api.post(`/app/result/${resultId}/re-score`, options)
  },

  // 批量重新评分
  batchReScore(resultIds, options = {}) {
    return api.post('/app/result/batch/re-score', { resultIds, ...options })
  },

  // 锁定评分（防止修改）
  lockScore(resultId) {
    return api.put(`/app/result/${resultId}/lock-score`)
  },

  // 解锁评分
  unlockScore(resultId) {
    return api.put(`/app/result/${resultId}/unlock-score`)
  },

  // ==================== 音频数据评分管理 ====================
  
  // 获取音频答题数据
  getAudioAnswers(resultId) {
    return api.get(`/app/result/${resultId}/audio-answers`)
  },

  // 上传音频评分
  scoreAudioAnswer(resultId, questionId, scoreData) {
    return api.post(`/app/result/${resultId}/audio-answers/${questionId}/score`, scoreData)
  },

  // 批量音频评分
  batchScoreAudioAnswers(resultId, audioScores) {
    return api.post(`/app/result/${resultId}/audio-answers/batch-score`, { audioScores })
  },

  // 获取音频评分标准
  getAudioScoringCriteria(questionType) {
    return api.get(`/app/result/audio-scoring-criteria/${questionType}`)
  },

  // 更新音频评分标准
  updateAudioScoringCriteria(questionType, criteria) {
    return api.put(`/app/result/audio-scoring-criteria/${questionType}`, criteria)
  },

  // AI辅助音频评分
  aiScoreAudio(resultId, questionId, options = {}) {
    return api.post(`/app/result/${resultId}/audio-answers/${questionId}/ai-score`, options)
  },

  // 获取音频评分历史
  getAudioScoringHistory(resultId, questionId) {
    return api.get(`/app/result/${resultId}/audio-answers/${questionId}/scoring-history`)
  },

  // 音频质量检测
  checkAudioQuality(audioUrl) {
    return api.post('/app/result/audio/quality-check', { audioUrl })
  },

  // 音频转文本
  audioToText(audioUrl, options = {}) {
    return api.post('/app/result/audio/transcribe', { audioUrl, ...options })
  },

  // ==================== 口语评分专项管理 ====================
  
  // 口语流利度评分
  scoreSpeakingFluency(resultId, questionId, scoreData) {
    return api.post(`/app/result/${resultId}/speaking/${questionId}/fluency-score`, scoreData)
  },

  // 口语发音评分
  scoreSpeakingPronunciation(resultId, questionId, scoreData) {
    return api.post(`/app/result/${resultId}/speaking/${questionId}/pronunciation-score`, scoreData)
  },

  // 口语语法评分
  scoreSpeakingGrammar(resultId, questionId, scoreData) {
    return api.post(`/app/result/${resultId}/speaking/${questionId}/grammar-score`, scoreData)
  },

  // 口语词汇评分
  scoreSpeakingVocabulary(resultId, questionId, scoreData) {
    return api.post(`/app/result/${resultId}/speaking/${questionId}/vocabulary-score`, scoreData)
  },

  // 综合口语评分
  scoreSpeakingOverall(resultId, questionId, scoreData) {
    return api.post(`/app/result/${resultId}/speaking/${questionId}/overall-score`, scoreData)
  },

  // 获取口语评分详情
  getSpeakingScoreDetails(resultId, questionId) {
    return api.get(`/app/result/${resultId}/speaking/${questionId}/score-details`)
  },

  // ==================== 考试结果分析 ====================
  
  // 获取结果统计分析
  getResultAnalysis(resultId) {
    return api.get(`/app/result/${resultId}/analysis`)
  },

  // 获取题目答题分析
  getQuestionAnalysis(resultId) {
    return api.get(`/app/result/${resultId}/question-analysis`)
  },

  // 获取能力分析报告
  getAbilityAnalysis(resultId) {
    return api.get(`/app/result/${resultId}/ability-analysis`)
  },

  // 生成学习建议
  generateLearningAdvice(resultId) {
    return api.post(`/app/result/${resultId}/learning-advice`)
  },

  // 获取同类考试对比
  getComparativeAnalysis(resultId, params = {}) {
    return api.get(`/app/result/${resultId}/comparative-analysis`, { params })
  },

  // ==================== 考试结果搜索筛选 ====================
  
  // 搜索考试结果
  searchResults(params = {}) {
    return api.get('/app/result/search', { params })
  },

  // 高级搜索
  advancedSearchResults(criteria) {
    return api.post('/app/result/search/advanced', criteria)
  },

  // 按分数范围筛选
  getResultsByScoreRange(minScore, maxScore, params = {}) {
    return api.get('/app/result/score-range', {
      params: { minScore, maxScore, ...params }
    })
  },

  // 按时间范围筛选
  getResultsByTimeRange(startTime, endTime, params = {}) {
    return api.get('/app/result/time-range', {
      params: { startTime, endTime, ...params }
    })
  },

  // 按通过状态筛选
  getResultsByPassStatus(isPassed, params = {}) {
    return api.get('/app/result/pass-status', {
      params: { isPassed, ...params }
    })
  },

  // ==================== 考试结果导出 ====================
  
  // 导出单个考试结果
  exportResult(resultId, format = 'pdf') {
    return api.get(`/app/result/${resultId}/export`, {
      params: { format },
      responseType: 'blob'
    })
  },

  // 批量导出考试结果
  batchExportResults(resultIds, format = 'excel') {
    return api.post('/app/result/export/batch', {
      resultIds,
      format
    }, {
      responseType: 'blob'
    })
  },

  // 导出考试统计报告
  exportStatisticsReport(examId, format = 'pdf') {
    return api.get(`/app/result/exam/${examId}/statistics-report`, {
      params: { format },
      responseType: 'blob'
    })
  },

  // 导出成绩单
  exportGradeSheet(resultIds, template = 'standard') {
    return api.post('/app/result/export/grade-sheet', {
      resultIds,
      template
    }, {
      responseType: 'blob'
    })
  },

  // ==================== 证书管理 ====================
  
  // 生成考试证书
  generateCertificate(resultId, certificateData = {}) {
    return api.post(`/app/result/${resultId}/certificate`, certificateData)
  },

  // 下载证书
  downloadCertificate(resultId) {
    return api.get(`/app/result/${resultId}/certificate/download`, {
      responseType: 'blob'
    })
  },

  // 批量生成证书
  batchGenerateCertificates(resultIds, certificateData = {}) {
    return api.post('/app/result/certificates/batch', {
      resultIds,
      ...certificateData
    })
  },

  // 验证证书
  verifyCertificate(certificateCode) {
    return api.get(`/app/result/certificate/verify/${certificateCode}`)
  },

  // 获取证书模板
  getCertificateTemplates() {
    return api.get('/app/result/certificate/templates')
  },

  // ==================== 考试结果统计 ====================
  
  // 获取整体统计
  getOverallStatistics(params = {}) {
    return api.get('/app/result/statistics/overall', { params })
  },

  // 获取考试统计
  getExamStatistics(examId) {
    return api.get(`/app/result/statistics/exam/${examId}`)
  },

  // 获取用户统计
  getUserStatistics(userId) {
    return api.get(`/app/result/statistics/user/${userId}`)
  },

  // 获取时间段统计
  getPeriodStatistics(startDate, endDate, params = {}) {
    return api.get('/app/result/statistics/period', {
      params: { startDate, endDate, ...params }
    })
  },

  // 获取分数分布统计
  getScoreDistribution(examId) {
    return api.get(`/app/result/statistics/score-distribution/${examId}`)
  },

  // 获取通过率统计
  getPassRateStatistics(params = {}) {
    return api.get('/app/result/statistics/pass-rate', { params })
  },

  // 获取题目正确率统计
  getQuestionAccuracyStats(examId) {
    return api.get(`/app/result/statistics/question-accuracy/${examId}`)
  },

  // ==================== 考试结果审核 ====================
  
  // 提交审核
  submitForReview(resultId, reviewData = {}) {
    return api.post(`/app/result/${resultId}/submit-review`, reviewData)
  },

  // 审核通过
  approveResult(resultId, reviewComment = '') {
    return api.post(`/app/result/${resultId}/approve`, { reviewComment })
  },

  // 审核拒绝
  rejectResult(resultId, reviewComment = '') {
    return api.post(`/app/result/${resultId}/reject`, { reviewComment })
  },

  // 获取待审核结果
  getPendingReviewResults(params = {}) {
    return api.get('/app/result/pending-review', { params })
  },

  // 批量审核
  batchReviewResults(resultIds, action, reviewComment = '') {
    return api.post('/app/result/batch-review', {
      resultIds,
      action,
      reviewComment
    })
  },

  // ==================== 考试结果备注和标记 ====================
  
  // 添加备注
  addResultNote(resultId, note) {
    return api.post(`/app/result/${resultId}/notes`, { note })
  },

  // 更新备注
  updateResultNote(resultId, noteId, note) {
    return api.put(`/app/result/${resultId}/notes/${noteId}`, { note })
  },

  // 删除备注
  deleteResultNote(resultId, noteId) {
    return api.delete(`/app/result/${resultId}/notes/${noteId}`)
  },

  // 获取备注列表
  getResultNotes(resultId) {
    return api.get(`/app/result/${resultId}/notes`)
  },

  // 标记为重点关注
  markAsImportant(resultId) {
    return api.put(`/app/result/${resultId}/mark-important`)
  },

  // 取消重点关注
  unmarkAsImportant(resultId) {
    return api.put(`/app/result/${resultId}/unmark-important`)
  },

  // 添加标签
  addResultTags(resultId, tags) {
    return api.post(`/app/result/${resultId}/tags`, { tags })
  },

  // 移除标签
  removeResultTags(resultId, tags) {
    return api.delete(`/app/result/${resultId}/tags`, { data: { tags } })
  },

  // ==================== 工具方法 ====================
  
  // 获取结果状态选项
  getResultStatusOptions() {
    return [
      { value: 'pending', label: '待评分', color: '#E6A23C' },
      { value: 'scoring', label: '评分中', color: '#409EFF' },
      { value: 'completed', label: '已完成', color: '#67C23A' },
      { value: 'reviewed', label: '已审核', color: '#67C23A' },
      { value: 'rejected', label: '已拒绝', color: '#F56C6C' },
      { value: 'archived', label: '已归档', color: '#909399' }
    ]
  },

  // 获取评分类型选项
  getScoringTypeOptions() {
    return [
      { value: 'auto', label: '自动评分' },
      { value: 'manual', label: '人工评分' },
      { value: 'mixed', label: '混合评分' },
      { value: 'ai_assisted', label: 'AI辅助评分' }
    ]
  },

  // 获取音频评分维度
  getAudioScoringDimensions() {
    return [
      { value: 'fluency', label: '流利度', weight: 25 },
      { value: 'pronunciation', label: '发音', weight: 25 },
      { value: 'grammar', label: '语法', weight: 25 },
      { value: 'vocabulary', label: '词汇', weight: 25 }
    ]
  },

  // 获取证书模板选项
  getCertificateTemplateOptions() {
    return [
      { value: 'standard', label: '标准模板' },
      { value: 'professional', label: '专业模板' },
      { value: 'academic', label: '学术模板' },
      { value: 'corporate', label: '企业模板' }
    ]
  },

  // 验证结果数据
  validateResultData(resultData) {
    const errors = []
    
    if (!resultData.examId) {
      errors.push('考试ID不能为空')
    }
    
    if (!resultData.userId) {
      errors.push('用户ID不能为空')
    }
    
    if (resultData.score !== undefined && (resultData.score < 0 || resultData.score > 100)) {
      errors.push('分数必须在0-100之间')
    }
    
    return {
      isValid: errors.length === 0,
      errors
    }
  },

  // 格式化结果状态
  formatResultStatus(status) {
    const statusMap = {
      'pending': '待评分',
      'scoring': '评分中',
      'completed': '已完成',
      'reviewed': '已审核',
      'rejected': '已拒绝',
      'archived': '已归档'
    }
    return statusMap[status] || status
  },

  // 格式化评分类型
  formatScoringType(type) {
    const typeMap = {
      'auto': '自动评分',
      'manual': '人工评分',
      'mixed': '混合评分',
      'ai_assisted': 'AI辅助评分'
    }
    return typeMap[type] || type
  },

  // 计算通过状态
  calculatePassStatus(score, passingScore = 60) {
    return score >= passingScore
  },

  // 计算等级
  calculateGrade(score) {
    if (score >= 90) return 'A'
    if (score >= 80) return 'B'
    if (score >= 70) return 'C'
    if (score >= 60) return 'D'
    return 'F'
  },

  // 格式化用时
  formatDuration(seconds) {
    if (!seconds) return '0秒'
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const remainingSeconds = seconds % 60
    
    if (hours > 0) {
      return `${hours}小时${minutes}分${remainingSeconds}秒`
    } else if (minutes > 0) {
      return `${minutes}分${remainingSeconds}秒`
    } else {
      return `${remainingSeconds}秒`
    }
  },

  // 生成结果摘要
  generateResultSummary(result) {
    return {
      score: result.score || 0,
      grade: this.calculateGrade(result.score || 0),
      isPassed: this.calculatePassStatus(result.score || 0, result.passingScore || 60),
      duration: this.formatDuration(result.duration),
      status: this.formatResultStatus(result.status),
      accuracy: result.correctAnswers && result.totalQuestions 
        ? Math.round((result.correctAnswers / result.totalQuestions) * 100) 
        : 0
    }
  },

  // 音频文件验证
  validateAudioFile(file) {
    const allowedTypes = [
      'audio/mpeg', 'audio/mp3', 'audio/wav', 
      'audio/ogg', 'audio/m4a', 'audio/aac'
    ]
    const maxSize = 100 * 1024 * 1024 // 100MB

    if (!file) {
      return { valid: false, message: '请选择音频文件' }
    }

    if (!allowedTypes.includes(file.type)) {
      return { 
        valid: false, 
        message: '只支持 MP3、WAV、OGG、M4A、AAC 格式的音频文件' 
      }
    }

    if (file.size > maxSize) {
      return { 
        valid: false, 
        message: '音频文件大小不能超过 100MB' 
      }
    }

    return { valid: true }
  }
}

export default resultApi

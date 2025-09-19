import api from './index'

// 考试管理相关API
export const examApi = {
  // ==================== 试卷管理 ====================
  
  // 获取试卷列表
  getExamPapers(params = {}) {
    return api.get('/exam/papers', { params })
  },

  // 根据ID获取试卷详情
  getExamPaperById(id) {
    return api.get(`/exam/papers/${id}`)
  },

  // 创建试卷
  createExamPaper(data) {
    return api.post('/exam/papers', data)
  },

  // 更新试卷
  updateExamPaper(id, data) {
    return api.put(`/exam/papers/${id}`, data)
  },

  // 删除试卷
  deleteExamPaper(id) {
    return api.delete(`/exam/papers/${id}`)
  },

  // 批量删除试卷
  batchDeleteExamPapers(paperIds) {
    return api.delete('/exam/papers/batch', { data: { paperIds } })
  },

  // 复制试卷
  copyExamPaper(id) {
    return api.post(`/exam/papers/${id}/copy`)
  },

  // 发布试卷
  publishExamPaper(id) {
    return api.put(`/exam/papers/${id}/publish`)
  },

  // 取消发布试卷
  unpublishExamPaper(id) {
    return api.put(`/exam/papers/${id}/unpublish`)
  },

  // 预览试卷
  previewExamPaper(id) {
    return api.get(`/exam/papers/${id}/preview`)
  },

  // 导出试卷
  exportExamPaper(id) {
    return api.get(`/exam/papers/${id}/export`, {
      responseType: 'blob'
    })
  },

  // ==================== 试卷题目管理 ====================
  
  // 获取试卷题目列表
  getExamPaperQuestions(paperId, params = {}) {
    return api.get(`/exam/papers/${paperId}/questions`, { params })
  },

  // 添加题目到试卷
  addQuestionToPaper(paperId, questionData) {
    return api.post(`/exam/papers/${paperId}/questions`, questionData)
  },

  // 批量添加题目到试卷
  batchAddQuestionsToPaper(paperId, questionIds) {
    return api.post(`/exam/papers/${paperId}/questions/batch`, { questionIds })
  },

  // 从试卷移除题目
  removeQuestionFromPaper(paperId, questionId) {
    return api.delete(`/exam/papers/${paperId}/questions/${questionId}`)
  },

  // 更新试卷中题目的顺序和分值
  updatePaperQuestion(paperId, questionId, data) {
    return api.put(`/exam/papers/${paperId}/questions/${questionId}`, data)
  },

  // 自动组卷（根据条件自动选择题目）
  autoGeneratePaper(paperId, criteria) {
    return api.post(`/exam/papers/${paperId}/auto-generate`, criteria)
  },

  // ==================== 考试信息管理 ====================
  
  // 获取考试列表
  getExams(params = {}) {
    return api.get('/exam/exams', { params })
  },

  // 根据ID获取考试详情
  getExamById(id) {
    return api.get(`/exam/exams/${id}`)
  },

  // 创建考试
  createExam(data) {
    return api.post('/exam/exams', data)
  },

  // 更新考试信息
  updateExam(id, data) {
    return api.put(`/exam/exams/${id}`, data)
  },

  // 删除考试
  deleteExam(id) {
    return api.delete(`/exam/exams/${id}`)
  },

  // 批量删除考试
  batchDeleteExams(examIds) {
    return api.delete('/exam/exams/batch', { data: { examIds } })
  },

  // 开始考试
  startExam(id) {
    return api.put(`/exam/exams/${id}/start`)
  },

  // 结束考试
  endExam(id) {
    return api.put(`/exam/exams/${id}/end`)
  },

  // 暂停考试
  pauseExam(id) {
    return api.put(`/exam/exams/${id}/pause`)
  },

  // 恢复考试
  resumeExam(id) {
    return api.put(`/exam/exams/${id}/resume`)
  },

  // 取消考试
  cancelExam(id) {
    return api.put(`/exam/exams/${id}/cancel`)
  },

  // ==================== 考试参与者管理 ====================
  
  // 获取考试参与者列表
  getExamParticipants(examId, params = {}) {
    return api.get(`/exam/exams/${examId}/participants`, { params })
  },

  // 添加参与者到考试
  addParticipantToExam(examId, userData) {
    return api.post(`/exam/exams/${examId}/participants`, userData)
  },

  // 批量添加参与者
  batchAddParticipants(examId, userIds) {
    return api.post(`/exam/exams/${examId}/participants/batch`, { userIds })
  },

  // 从考试中移除参与者
  removeParticipantFromExam(examId, userId) {
    return api.delete(`/exam/exams/${examId}/participants/${userId}`)
  },

  // 导入参与者名单
  importParticipants(examId, file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`/exam/exams/${examId}/participants/import`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // ==================== 考试结果管理 ====================
  
  // 获取考试结果列表
  getExamResults(examId, params = {}) {
    return api.get(`/exam/exams/${examId}/results`, { params })
  },

  // 根据ID获取考试结果详情
  getExamResultById(resultId) {
    return api.get(`/exam/results/${resultId}`)
  },

  // 获取用户的考试结果
  getUserExamResults(userId, params = {}) {
    return api.get(`/exam/users/${userId}/results`, { params })
  },

  // 重新评分
  reScoreExamResult(resultId) {
    return api.post(`/exam/results/${resultId}/re-score`)
  },

  // 批量重新评分
  batchReScoreResults(resultIds) {
    return api.post('/exam/results/batch-re-score', { resultIds })
  },

  // 导出考试结果
  exportExamResults(examId, params = {}) {
    return api.get(`/exam/exams/${examId}/results/export`, {
      params,
      responseType: 'blob'
    })
  },

  // 导出成绩单
  exportGradeReport(examId, format = 'excel') {
    return api.get(`/exam/exams/${examId}/grade-report`, {
      params: { format },
      responseType: 'blob'
    })
  },

  // ==================== 考试统计分析 ====================
  
  // 获取考试统计概览
  getExamStatistics(examId) {
    return api.get(`/exam/exams/${examId}/statistics`)
  },

  // 获取考试成绩分布
  getScoreDistribution(examId) {
    return api.get(`/exam/exams/${examId}/score-distribution`)
  },

  // 获取题目正确率统计
  getQuestionAccuracy(examId) {
    return api.get(`/exam/exams/${examId}/question-accuracy`)
  },

  // 获取考试时间分析
  getExamTimeAnalysis(examId) {
    return api.get(`/exam/exams/${examId}/time-analysis`)
  },

  // 获取考试排名
  getExamRanking(examId, params = {}) {
    return api.get(`/exam/exams/${examId}/ranking`, { params })
  },

  // 获取系统考试统计
  getSystemExamStats(params = {}) {
    return api.get('/exam/stats', { params })
  },

  // ==================== 考试监控 ====================
  
  // 获取正在进行的考试列表
  getOngoingExams(params = {}) {
    return api.get('/exam/ongoing', { params })
  },

  // 获取考试实时状态
  getExamRealTimeStatus(examId) {
    return api.get(`/exam/exams/${examId}/real-time-status`)
  },

  // 获取考生答题进度
  getParticipantProgress(examId, userId) {
    return api.get(`/exam/exams/${examId}/participants/${userId}/progress`)
  },

  // 强制提交考试
  forceSubmitExam(examId, userId) {
    return api.post(`/exam/exams/${examId}/participants/${userId}/force-submit`)
  },

  // ==================== 工具方法 ====================
  
  // 获取考试状态选项
  getExamStatusOptions() {
    return [
      { value: 'draft', label: '草稿', color: 'info' },
      { value: 'published', label: '已发布', color: 'success' },
      { value: 'ongoing', label: '进行中', color: 'warning' },
      { value: 'ended', label: '已结束', color: 'primary' },
      { value: 'cancelled', label: '已取消', color: 'danger' }
    ]
  },

  // 获取试卷类型选项
  getPaperTypeOptions() {
    return [
      { value: 'practice', label: '练习试卷', icon: 'Edit' },
      { value: 'mock', label: '模拟考试', icon: 'Clock' },
      { value: 'formal', label: '正式考试', icon: 'Medal' },
      { value: 'placement', label: '分级考试', icon: 'TrendCharts' }
    ]
  },

  // 获取评分方式选项
  getScoringMethodOptions() {
    return [
      { value: 'auto', label: '自动评分' },
      { value: 'manual', label: '人工评分' },
      { value: 'mixed', label: '混合评分' }
    ]
  },

  // 格式化考试状态
  formatExamStatus(status) {
    const statusMap = {
      'draft': '草稿',
      'published': '已发布',
      'ongoing': '进行中',
      'ended': '已结束',
      'cancelled': '已取消'
    }
    return statusMap[status] || status
  },

  // 格式化试卷类型
  formatPaperType(type) {
    const typeMap = {
      'practice': '练习试卷',
      'mock': '模拟考试',
      'formal': '正式考试',
      'placement': '分级考试'
    }
    return typeMap[type] || type
  },

  // 计算考试时长（分钟）
  calculateExamDuration(startTime, endTime) {
    if (!startTime || !endTime) return 0
    const start = new Date(startTime)
    const end = new Date(endTime)
    return Math.floor((end - start) / (1000 * 60))
  },

  // 格式化分数
  formatScore(score, maxScore = 100) {
    if (score === null || score === undefined) return '-'
    return `${score}/${maxScore}`
  },

  // 计算通过率
  calculatePassRate(results, passingScore = 60) {
    if (!results || results.length === 0) return 0
    const passedCount = results.filter(result => result.score >= passingScore).length
    return Math.round((passedCount / results.length) * 100)
  }
}

export default examApi

import { paperApi } from './paper'
import { resultApi } from './result'

/**
 * 考试管理统一API接口
 * 整合试卷管理(paperApi)和考试结果管理(resultApi)功能
 */
export const examApi = {
  // ==================== 试卷相关API (委托给paperApi) ====================
  
  // 获取试卷列表 (作为考试试卷)
  getExamPapers(params = {}) {
    return paperApi.getPapers(params)
  },

  // 根据ID获取试卷详情
  getExamPaperById(id) {
    return paperApi.getPaperById(id)
  },

  // 创建试卷
  createExamPaper(data) {
    return paperApi.createPaper(data)
  },

  // 更新试卷
  updateExamPaper(id, data) {
    return paperApi.updatePaper(id, data)
  },

  // 删除试卷
  deleteExamPaper(id) {
    return paperApi.deletePaper(id)
  },

  // 批量删除试卷
  batchDeleteExamPapers(paperIds) {
    return paperApi.batchDeletePapers(paperIds)
  },

  // 复制试卷
  copyExamPaper(id) {
    return paperApi.copyPaper(id)
  },

  // 发布试卷
  publishExamPaper(id) {
    return paperApi.publishPaper(id)
  },

  // 取消发布试卷
  unpublishExamPaper(id) {
    return paperApi.unpublishPaper(id)
  },

  // 预览试卷
  previewExamPaper(id) {
    return paperApi.previewPaper(id)
  },

  // 获取试卷统计信息
  getExamPaperStatistics(id) {
    return paperApi.getPaperStatistics(id)
  },

  // ==================== 考试管理API (基于试卷的考试实例) ====================
  
  // 获取考试列表 (实际上是获取已发布的试卷作为考试)
  getExams(params = {}) {
    // 获取已发布状态的试卷作为考试
    return paperApi.getPapers({ 
      ...params, 
      status: params.status || 'published' 
    })
  },

  // 根据ID获取考试详情
  getExamById(id) {
    return paperApi.getPaperById(id)
  },

  // 创建考试 (实际上是创建试卷)
  createExam(data) {
    // 将考试数据转换为试卷数据格式
    const paperData = {
      title: data.name || data.title,
      type: data.type,
      description: data.description,
      duration: data.duration,
      passingScore: data.passingScore,
      status: data.status || 'draft',
      settings: data.settings,
      startTime: data.startTime,
      endTime: data.endTime,
      maxParticipants: data.maxParticipants,
      retakeLimit: data.retakeLimit,
      scoringMethod: data.scoringMethod,
      remark: data.remark
    }
    return paperApi.createPaper(paperData)
  },

  // 更新考试
  updateExam(id, data) {
    const paperData = {
      title: data.name || data.title,
      type: data.type,
      description: data.description,
      duration: data.duration,
      passingScore: data.passingScore,
      status: data.status,
      settings: data.settings,
      startTime: data.startTime,
      endTime: data.endTime,
      maxParticipants: data.maxParticipants,
      retakeLimit: data.retakeLimit,
      scoringMethod: data.scoringMethod,
      remark: data.remark
    }
    return paperApi.updatePaper(id, paperData)
  },

  // 删除考试
  deleteExam(id) {
    return paperApi.deletePaper(id)
  },

  // 开始考试 (发布试卷)
  startExam(id) {
    return paperApi.publishPaper(id)
  },

  // 结束考试 (取消发布试卷)
  endExam(id) {
    return paperApi.unpublishPaper(id)
  },

  // ==================== 考试结果相关API (委托给resultApi) ====================
  
  // 获取所有考试结果
  getAllExamResults(params = {}) {
    return resultApi.getResults(params)
  },

  // 根据考试ID获取结果
  getExamResults(examId, params = {}) {
    return resultApi.getResultsByExamId(examId, params)
  },

  // 根据用户ID获取结果
  getUserExamResults(userId, params = {}) {
    return resultApi.getResultsByUserId(userId, params)
  },

  // 获取考试结果详情
  getExamResultById(id) {
    return resultApi.getResultById(id)
  },

  // 创建考试结果
  createExamResult(data) {
    return resultApi.createResult(data)
  },

  // 更新考试结果
  updateExamResult(id, data) {
    return resultApi.updateResult(id, data)
  },

  // 删除考试结果
  deleteExamResult(id) {
    return resultApi.deleteResult(id)
  },

  // 重新评分
  reScoreExamResult(id, options = {}) {
    return resultApi.reScore(id, options)
  },

  // 导出考试结果
  exportExamResults(examId = null, format = 'excel') {
    if (examId) {
      return resultApi.exportStatisticsReport(examId, format)
    } else {
      return resultApi.batchExportResults([], format)
    }
  },

  // 获取考试统计
  getExamStatistics(examId) {
    return resultApi.getExamStatistics(examId)
  },

  // ==================== 组合API方法 ====================
  
  // 获取考试完整信息 (试卷 + 结果统计)
  async getExamFullInfo(examId) {
    try {
      const [examInfo, examStats] = await Promise.all([
        this.getExamById(examId),
        this.getExamStatistics(examId).catch(() => ({ data: {} }))
      ])
      
      return {
        data: {
          ...examInfo.data,
          statistics: examStats.data
        }
      }
    } catch (error) {
      console.error('获取考试完整信息失败:', error)
      throw error
    }
  },

  // 获取考试参与者列表
  async getExamParticipants(examId, params = {}) {
    try {
      const response = await this.getExamResults(examId, params)
      return {
        data: {
          content: response.data?.content || [],
          total: response.data?.total || 0
        }
      }
    } catch (error) {
      console.error('获取考试参与者失败:', error)
      throw error
    }
  },

  // ==================== 工具方法 ====================
  
  // 获取试卷类型选项
  getPaperTypeOptions() {
    return paperApi.getPaperTypeOptions()
  },

  // 获取试卷状态选项 (作为考试状态)
  getExamStatusOptions() {
    return [
      { value: 'draft', label: '草稿', color: '#909399' },
      { value: 'published', label: '已发布', color: '#67C23A' },
      { value: 'ongoing', label: '进行中', color: '#E6A23C' },
      { value: 'ended', label: '已结束', color: '#409EFF' },
      { value: 'cancelled', label: '已取消', color: '#F56C6C' },
      { value: 'archived', label: '已归档', color: '#606266' }
    ]
  },

  // 获取评分方法选项
  getScoringMethodOptions() {
    return [
      { value: 'auto', label: '自动评分' },
      { value: 'manual', label: '人工评分' },
      { value: 'mixed', label: '混合评分' },
      { value: 'ai_assisted', label: 'AI辅助评分' }
    ]
  },

  // 获取考试结果状态选项
  getExamResultStatusOptions() {
    return resultApi.getResultStatusOptions()
  },

  // 格式化考试状态
  formatExamStatus(status) {
    const statusMap = {
      'draft': '草稿',
      'published': '已发布',
      'ongoing': '进行中',
      'ended': '已结束',
      'cancelled': '已取消',
      'archived': '已归档'
    }
    return statusMap[status] || status
  },

  // 格式化试卷类型
  formatPaperType(type) {
    return paperApi.formatPaperType(type)
  },

  // 格式化考试结果状态
  formatExamResultStatus(status) {
    return resultApi.formatResultStatus(status)
  },

  // 验证考试数据
  validateExamData(examData) {
    const errors = []
    
    if (!examData.name && !examData.title) {
      errors.push('考试名称不能为空')
    }
    
    if (!examData.type) {
      errors.push('请选择考试类型')
    }
    
    if (!examData.duration || examData.duration <= 0) {
      errors.push('考试时长必须大于0分钟')
    }
    
    if (examData.startTime && examData.endTime) {
      if (new Date(examData.startTime) >= new Date(examData.endTime)) {
        errors.push('开始时间必须早于结束时间')
      }
    }
    
    if (examData.passingScore !== undefined) {
      if (examData.passingScore < 0 || examData.passingScore > 100) {
        errors.push('及格分数必须在0-100之间')
      }
    }
    
    if (examData.maxParticipants !== undefined && examData.maxParticipants <= 0) {
      errors.push('最大参与人数必须大于0')
    }
    
    return {
      isValid: errors.length === 0,
      errors
    }
  },

  // 计算考试统计信息
  calculateExamStats(exams = [], results = []) {
    const stats = {
      totalExams: exams.length,
      draftExams: 0,
      publishedExams: 0,
      ongoingExams: 0,
      endedExams: 0,
      cancelledExams: 0,
      totalResults: results.length,
      completedResults: 0,
      averageScore: 0,
      passRate: 0
    }

    // 统计考试状态
    exams.forEach(exam => {
      switch (exam.status) {
        case 'draft':
          stats.draftExams++
          break
        case 'published':
          stats.publishedExams++
          break
        case 'ongoing':
          stats.ongoingExams++
          break
        case 'ended':
          stats.endedExams++
          break
        case 'cancelled':
          stats.cancelledExams++
          break
      }
    })

    // 统计考试结果
    if (results.length > 0) {
      const completedResults = results.filter(r => r.status === 'completed')
      stats.completedResults = completedResults.length

      if (completedResults.length > 0) {
        const totalScore = completedResults.reduce((sum, r) => sum + (r.score || 0), 0)
        stats.averageScore = Math.round(totalScore / completedResults.length)
        
        const passedResults = completedResults.filter(r => 
          resultApi.calculatePassStatus(r.score, r.passingScore || 60)
        )
        stats.passRate = Math.round((passedResults.length / completedResults.length) * 100)
      }
    }

    return stats
  },

  // ==================== 批量操作方法 ====================
  
  // 批量发布考试
  async batchPublishExams(examIds) {
    try {
      const promises = examIds.map(id => this.startExam(id))
      await Promise.all(promises)
      return { success: true, message: `成功发布 ${examIds.length} 个考试` }
    } catch (error) {
      console.error('批量发布考试失败:', error)
      throw error
    }
  },

  // 批量结束考试
  async batchEndExams(examIds) {
    try {
      const promises = examIds.map(id => this.endExam(id))
      await Promise.all(promises)
      return { success: true, message: `成功结束 ${examIds.length} 个考试` }
    } catch (error) {
      console.error('批量结束考试失败:', error)
      throw error
    }
  },

  // 批量删除考试
  async batchDeleteExams(examIds) {
    try {
      const promises = examIds.map(id => this.deleteExam(id))
      await Promise.all(promises)
      return { success: true, message: `成功删除 ${examIds.length} 个考试` }
    } catch (error) {
      console.error('批量删除考试失败:', error)
      throw error
    }
  },

  // ==================== 高级搜索方法 ====================
  
  // 搜索考试
  searchExams(params = {}) {
    return paperApi.searchPapers(params)
  },

  // 高级搜索考试
  advancedSearchExams(criteria) {
    return paperApi.advancedSearchPapers(criteria)
  },

  // 搜索考试结果
  searchExamResults(params = {}) {
    return resultApi.searchResults(params)
  },

  // ==================== 数据导入导出方法 ====================
  
  // 导出考试数据
  async exportExamData(examIds = [], format = 'excel') {
    try {
      if (examIds.length === 0) {
        // 导出所有考试数据
        return await paperApi.batchExportPapers(examIds, format)
      } else {
        // 导出指定考试数据
        return await paperApi.batchExportPapers(examIds, format)
      }
    } catch (error) {
      console.error('导出考试数据失败:', error)
      throw error
    }
  },

  // 导入考试数据
  async importExamData(file, options = {}) {
    try {
      return await paperApi.importPaper(file, options)
    } catch (error) {
      console.error('导入考试数据失败:', error)
      throw error
    }
  },

  // ==================== 权限和安全方法 ====================
  
  // 检查考试操作权限
  checkExamPermission(exam, operation) {
    // 基础权限检查逻辑
    const permissions = {
      edit: exam.status !== 'ongoing',
      delete: exam.status !== 'ongoing',
      start: ['draft', 'published'].includes(exam.status),
      end: exam.status === 'ongoing',
      view: true,
      export: true
    }
    
    return permissions[operation] || false
  },

  // 获取考试权限信息
  getExamPermissions(examId) {
    return paperApi.getPaperPermissions(examId)
  },

  // 设置考试权限
  setExamPermissions(examId, permissions) {
    return paperApi.setPaperPermissions(examId, permissions)
  }
}

// 兼容性导出
export default examApi

// 重新导出paperApi和resultApi以便直接使用
export { paperApi, resultApi }

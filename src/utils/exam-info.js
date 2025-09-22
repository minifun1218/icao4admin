import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { examApi, paperApi, resultApi } from '@/api/exam'

/**
 * 考试信息管理工具类
 * 用于管理考试信息的状态、数据处理和业务逻辑
 * 关联 ExamInfoPanel.vue 组件和其他考试相关组件
 */
export class ExamInfoManager {
  constructor(options = {}) {
    // 基础配置
    this.options = {
      autoRefresh: false,
      refreshInterval: 30000, // 30秒
      enableCache: true,
      cacheTimeout: 300000, // 5分钟
      ...options
    }

    // 响应式数据状态
    this.state = reactive({
      // 考试列表数据
      examList: [],
      filteredExams: [],
      
      // 加载状态
      loading: false,
      saving: false,
      
      // 分页信息
      pagination: {
        page: 1,
        size: 20,
        total: 0
      },
      
      // 搜索条件
      searchParams: {
        keyword: '',
        status: '',
        type: '',
        dateRange: [],
        creatorId: null,
        paperId: null
      },
      
      // 选中的考试
      selectedExams: [],
      currentExam: null,
      
      // 统计信息
      statistics: {
        totalExams: 0,
        draftExams: 0,
        publishedExams: 0,
        ongoingExams: 0,
        endedExams: 0,
        cancelledExams: 0
      },
      
      // 缓存数据
      cache: new Map(),
      lastRefreshTime: null
    })

    // 自动刷新定时器
    this.refreshTimer = null
    
    // 初始化
    this.init()
  }

  /**
   * 初始化
   */
  async init() {
    if (this.options.autoRefresh) {
      this.startAutoRefresh()
    }
    await this.loadExamData()
  }

  /**
   * 销毁实例
   */
  destroy() {
    this.stopAutoRefresh()
    this.state.cache.clear()
  }

  // ==================== 数据加载方法 ====================

  /**
   * 加载考试数据
   */
  async loadExamData(forceRefresh = false) {
    if (this.state.loading && !forceRefresh) return

    try {
      this.state.loading = true
      
      // 检查缓存
      const cacheKey = this.generateCacheKey('examList', this.state.searchParams)
      if (!forceRefresh && this.options.enableCache && this.state.cache.has(cacheKey)) {
        const cached = this.state.cache.get(cacheKey)
        if (Date.now() - cached.timestamp < this.options.cacheTimeout) {
          this.state.examList = cached.data
          this.updateFilteredExams()
          this.updateStatistics()
          return cached.data
        }
      }

      // 构建查询参数
      const params = this.buildQueryParams()
      
      // 调用API获取数据
      const response = await examApi.getExams(params)
      const examData = response.data?.content || []
      
      // 更新状态
      this.state.examList = examData
      this.state.pagination.total = response.data?.total || examData.length
      this.state.lastRefreshTime = new Date()
      
      // 缓存数据
      if (this.options.enableCache) {
        this.state.cache.set(cacheKey, {
          data: examData,
          timestamp: Date.now()
        })
      }
      
      // 更新过滤和统计
      this.updateFilteredExams()
      this.updateStatistics()
      
      return examData
    } catch (error) {
      console.error('加载考试数据失败:', error)
      ElMessage.error('加载考试数据失败')
      throw error
    } finally {
      this.state.loading = false
    }
  }

  /**
   * 根据ID获取考试详情
   */
  async getExamById(examId, forceRefresh = false) {
    if (!examId) return null

    try {
      const cacheKey = `exam_${examId}`
      
      // 检查缓存
      if (!forceRefresh && this.options.enableCache && this.state.cache.has(cacheKey)) {
        const cached = this.state.cache.get(cacheKey)
        if (Date.now() - cached.timestamp < this.options.cacheTimeout) {
          return cached.data
        }
      }

      // 调用API获取详情
      const response = await examApi.getExamById(examId)
      const examData = response.data
      
      // 缓存数据
      if (this.options.enableCache) {
        this.state.cache.set(cacheKey, {
          data: examData,
          timestamp: Date.now()
        })
      }
      
      return examData
    } catch (error) {
      console.error('获取考试详情失败:', error)
      ElMessage.error('获取考试详情失败')
      throw error
    }
  }

  /**
   * 获取考试统计数据
   */
  async loadExamStatistics() {
    try {
      // 计算本地统计数据
      const stats = examApi.calculateExamStats(this.state.examList, [])
      this.state.statistics = {
        ...this.state.statistics,
        ...stats
      }
      return this.state.statistics
    } catch (error) {
      console.error('获取统计数据失败:', error)
      return this.state.statistics
    }
  }

  // ==================== 考试CRUD操作 ====================

  /**
   * 创建考试
   */
  async createExam(examData) {
    try {
      this.state.saving = true
      
      // 验证数据
      const validation = examApi.validateExamData(examData)
      if (!validation.isValid) {
        ElMessage.error(validation.errors[0])
        return false
      }
      
      // 调用API创建
      const response = await examApi.createExam(examData)
      const newExam = response.data
      
      // 更新本地状态
      this.state.examList.unshift(newExam)
      this.updateFilteredExams()
      this.updateStatistics()
      
      // 清除相关缓存
      this.clearCache('examList')
      
      ElMessage.success('考试创建成功')
      return newExam
    } catch (error) {
      console.error('创建考试失败:', error)
      ElMessage.error('创建考试失败')
      throw error
    } finally {
      this.state.saving = false
    }
  }

  /**
   * 更新考试
   */
  async updateExam(examId, examData) {
    try {
      this.state.saving = true
      
      // 验证数据
      const validation = examApi.validateExamData(examData)
      if (!validation.isValid) {
        ElMessage.error(validation.errors[0])
        return false
      }
      
      // 调用API更新
      const response = await examApi.updateExam(examId, examData)
      const updatedExam = response.data
      
      // 更新本地状态
      const index = this.state.examList.findIndex(exam => exam.id === examId)
      if (index !== -1) {
        this.state.examList[index] = updatedExam
        this.updateFilteredExams()
        this.updateStatistics()
      }
      
      // 清除相关缓存
      this.clearCache(['examList', `exam_${examId}`])
      
      ElMessage.success('考试更新成功')
      return updatedExam
    } catch (error) {
      console.error('更新考试失败:', error)
      ElMessage.error('更新考试失败')
      throw error
    } finally {
      this.state.saving = false
    }
  }

  /**
   * 删除考试
   */
  async deleteExam(examId, examName = '') {
    try {
      // 确认删除
      await ElMessageBox.confirm(
        `确定要删除考试"${examName}"吗？此操作不可恢复。`,
        '确认删除',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      
      // 调用API删除
      await examApi.deleteExam(examId)
      
      // 更新本地状态
      this.state.examList = this.state.examList.filter(exam => exam.id !== examId)
      this.state.selectedExams = this.state.selectedExams.filter(id => id !== examId)
      this.updateFilteredExams()
      this.updateStatistics()
      
      // 清除相关缓存
      this.clearCache(['examList', `exam_${examId}`])
      
      ElMessage.success('删除成功')
      return true
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除考试失败:', error)
        ElMessage.error('删除考试失败')
      }
      return false
    }
  }

  /**
   * 批量删除考试
   */
  async batchDeleteExams(examIds) {
    try {
      if (!examIds || examIds.length === 0) {
        ElMessage.warning('请选择要删除的考试')
        return false
      }

      // 确认删除
      await ElMessageBox.confirm(
        `确定要删除选中的 ${examIds.length} 个考试吗？此操作不可恢复。`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      
      // 调用API批量删除
      await examApi.batchDeleteExams(examIds)
      
      // 更新本地状态
      this.state.examList = this.state.examList.filter(exam => !examIds.includes(exam.id))
      this.state.selectedExams = []
      this.updateFilteredExams()
      this.updateStatistics()
      
      // 清除相关缓存
      this.clearCache(['examList', ...examIds.map(id => `exam_${id}`)])
      
      ElMessage.success(`成功删除 ${examIds.length} 个考试`)
      return true
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除考试失败:', error)
        ElMessage.error('批量删除考试失败')
      }
      return false
    }
  }

  // ==================== 考试状态管理 ====================

  /**
   * 开始考试
   */
  async startExam(examId) {
    try {
      await examApi.startExam(examId)
      
      // 更新本地状态
      const exam = this.state.examList.find(e => e.id === examId)
      if (exam) {
        exam.status = 'ongoing'
        exam.startTime = new Date().toISOString()
        this.updateFilteredExams()
        this.updateStatistics()
      }
      
      // 清除相关缓存
      this.clearCache(['examList', `exam_${examId}`])
      
      ElMessage.success('考试已开始')
      return true
    } catch (error) {
      console.error('开始考试失败:', error)
      ElMessage.error('开始考试失败')
      return false
    }
  }

  /**
   * 结束考试
   */
  async endExam(examId, examName = '') {
    try {
      // 确认结束
      await ElMessageBox.confirm(
        `确定要结束考试"${examName}"吗？考试结束后将无法继续答题。`,
        '确认结束考试',
        {
          type: 'warning',
          confirmButtonText: '确定结束',
          cancelButtonText: '取消'
        }
      )
      
      await examApi.endExam(examId)
      
      // 更新本地状态
      const exam = this.state.examList.find(e => e.id === examId)
      if (exam) {
        exam.status = 'ended'
        exam.endTime = new Date().toISOString()
        this.updateFilteredExams()
        this.updateStatistics()
      }
      
      // 清除相关缓存
      this.clearCache(['examList', `exam_${examId}`])
      
      ElMessage.success('考试已结束')
      return true
    } catch (error) {
      if (error !== 'cancel') {
        console.error('结束考试失败:', error)
        ElMessage.error('结束考试失败')
      }
      return false
    }
  }

  /**
   * 发布考试
   */
  async publishExam(examId) {
    try {
      await examApi.startExam(examId)
      
      // 更新本地状态
      const exam = this.state.examList.find(e => e.id === examId)
      if (exam) {
        exam.status = 'published'
        this.updateFilteredExams()
        this.updateStatistics()
      }
      
      // 清除相关缓存
      this.clearCache(['examList', `exam_${examId}`])
      
      ElMessage.success('考试发布成功')
      return true
    } catch (error) {
      console.error('发布考试失败:', error)
      ElMessage.error('发布考试失败')
      return false
    }
  }

  /**
   * 取消发布考试
   */
  async unpublishExam(examId) {
    try {
      await examApi.endExam(examId)
      
      // 更新本地状态
      const exam = this.state.examList.find(e => e.id === examId)
      if (exam) {
        exam.status = 'draft'
        this.updateFilteredExams()
        this.updateStatistics()
      }
      
      // 清除相关缓存
      this.clearCache(['examList', `exam_${examId}`])
      
      ElMessage.success('已取消发布')
      return true
    } catch (error) {
      console.error('取消发布失败:', error)
      ElMessage.error('取消发布失败')
      return false
    }
  }

  // ==================== 搜索和筛选 ====================

  /**
   * 设置搜索参数
   */
  setSearchParams(params) {
    Object.assign(this.state.searchParams, params)
    this.state.pagination.page = 1
    this.updateFilteredExams()
  }

  /**
   * 重置搜索参数
   */
  resetSearchParams() {
    Object.assign(this.state.searchParams, {
      keyword: '',
      status: '',
      type: '',
      dateRange: [],
      creatorId: null,
      paperId: null
    })
    this.state.pagination.page = 1
    this.updateFilteredExams()
  }

  /**
   * 执行搜索
   */
  async search(params = {}) {
    this.setSearchParams(params)
    await this.loadExamData(true)
  }

  /**
   * 更新过滤后的考试列表
   */
  updateFilteredExams() {
    let filtered = [...this.state.examList]
    const params = this.state.searchParams

    // 关键词搜索
    if (params.keyword) {
      const keyword = params.keyword.toLowerCase()
      filtered = filtered.filter(exam => 
        exam.name?.toLowerCase().includes(keyword) ||
        exam.title?.toLowerCase().includes(keyword) ||
        exam.description?.toLowerCase().includes(keyword)
      )
    }

    // 状态筛选
    if (params.status) {
      filtered = filtered.filter(exam => exam.status === params.status)
    }

    // 类型筛选
    if (params.type) {
      filtered = filtered.filter(exam => exam.type === params.type)
    }

    // 日期范围筛选
    if (params.dateRange && params.dateRange.length === 2) {
      const startDate = new Date(params.dateRange[0])
      const endDate = new Date(params.dateRange[1])
      filtered = filtered.filter(exam => {
        const examDate = new Date(exam.createdAt || exam.startTime)
        return examDate >= startDate && examDate <= endDate
      })
    }

    // 创建者筛选
    if (params.creatorId) {
      filtered = filtered.filter(exam => exam.creatorId === params.creatorId)
    }

    // 试卷筛选
    if (params.paperId) {
      filtered = filtered.filter(exam => exam.paperId === params.paperId)
    }

    // 更新分页总数
    this.state.pagination.total = filtered.length

    // 分页处理
    const { page, size } = this.state.pagination
    const start = (page - 1) * size
    const end = start + size
    
    this.state.filteredExams = filtered.slice(start, end)
  }

  // ==================== 分页管理 ====================

  /**
   * 设置分页参数
   */
  setPagination(pagination) {
    Object.assign(this.state.pagination, pagination)
    this.updateFilteredExams()
  }

  /**
   * 跳转到指定页面
   */
  goToPage(page) {
    this.state.pagination.page = page
    this.updateFilteredExams()
  }

  /**
   * 改变每页大小
   */
  changePageSize(size) {
    this.state.pagination.size = size
    this.state.pagination.page = 1
    this.updateFilteredExams()
  }

  // ==================== 选择管理 ====================

  /**
   * 选择考试
   */
  selectExams(examIds) {
    this.state.selectedExams = Array.isArray(examIds) ? examIds : [examIds]
  }

  /**
   * 切换考试选择状态
   */
  toggleExamSelection(examId) {
    const index = this.state.selectedExams.indexOf(examId)
    if (index > -1) {
      this.state.selectedExams.splice(index, 1)
    } else {
      this.state.selectedExams.push(examId)
    }
  }

  /**
   * 全选/取消全选
   */
  toggleSelectAll() {
    if (this.state.selectedExams.length === this.state.filteredExams.length) {
      this.state.selectedExams = []
    } else {
      this.state.selectedExams = this.state.filteredExams.map(exam => exam.id)
    }
  }

  /**
   * 清空选择
   */
  clearSelection() {
    this.state.selectedExams = []
  }

  /**
   * 设置当前考试
   */
  setCurrentExam(exam) {
    this.state.currentExam = exam
  }

  // ==================== 统计和分析 ====================

  /**
   * 更新统计信息
   */
  updateStatistics() {
    const exams = this.state.examList
    this.state.statistics = {
      totalExams: exams.length,
      draftExams: exams.filter(e => e.status === 'draft').length,
      publishedExams: exams.filter(e => e.status === 'published').length,
      ongoingExams: exams.filter(e => e.status === 'ongoing').length,
      endedExams: exams.filter(e => e.status === 'ended').length,
      cancelledExams: exams.filter(e => e.status === 'cancelled').length
    }
  }

  /**
   * 获取考试分析数据
   */
  getExamAnalysis(examId) {
    const exam = this.state.examList.find(e => e.id === examId)
    if (!exam) return null

    return {
      id: exam.id,
      name: exam.name || exam.title,
      status: exam.status,
      type: exam.type,
      participantCount: exam.participantCount || 0,
      completedCount: exam.completedCount || 0,
      completionRate: exam.participantCount > 0 
        ? Math.round((exam.completedCount / exam.participantCount) * 100) 
        : 0,
      averageScore: exam.averageScore || 0,
      passRate: exam.passRate || 0,
      duration: exam.duration,
      questionCount: exam.questionCount || 0
    }
  }

  // ==================== 导入导出 ====================

  /**
   * 导出考试数据
   */
  async exportExamData(examIds = null, format = 'excel') {
    try {
      const idsToExport = examIds || this.state.selectedExams
      if (!idsToExport || idsToExport.length === 0) {
        ElMessage.warning('请选择要导出的考试')
        return false
      }

      const blob = await examApi.exportExamData(idsToExport, format)
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `考试数据_${new Date().toISOString().slice(0, 10)}.${format === 'excel' ? 'xlsx' : 'pdf'}`
      link.click()
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('导出成功')
      return true
    } catch (error) {
      console.error('导出失败:', error)
      ElMessage.error('导出失败')
      return false
    }
  }

  /**
   * 导入考试数据
   */
  async importExamData(file, options = {}) {
    try {
      await examApi.importExamData(file, options)
      ElMessage.success('导入成功')
      await this.loadExamData(true)
      return true
    } catch (error) {
      console.error('导入失败:', error)
      ElMessage.error('导入失败')
      return false
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 构建查询参数
   */
  buildQueryParams() {
    const params = {
      page: this.state.pagination.page - 1, // API使用0基索引
      size: this.state.pagination.size,
      ...this.state.searchParams
    }

    // 处理日期范围
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0]
      params.endDate = params.dateRange[1]
      delete params.dateRange
    }

    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    return params
  }

  /**
   * 生成缓存键
   */
  generateCacheKey(prefix, params = {}) {
    const key = Object.keys(params)
      .sort()
      .map(k => `${k}:${params[k]}`)
      .join('|')
    return `${prefix}_${key}`
  }

  /**
   * 清除缓存
   */
  clearCache(keys = null) {
    if (!keys) {
      this.state.cache.clear()
      return
    }

    const keysToDelete = Array.isArray(keys) ? keys : [keys]
    keysToDelete.forEach(key => {
      // 支持模糊匹配
      if (key.includes('*')) {
        const pattern = key.replace('*', '')
        for (const cacheKey of this.state.cache.keys()) {
          if (cacheKey.includes(pattern)) {
            this.state.cache.delete(cacheKey)
          }
        }
      } else {
        this.state.cache.delete(key)
      }
    })
  }

  /**
   * 验证考试数据
   */
  validateExamData(examData) {
    return examApi.validateExamData(examData)
  }

  /**
   * 开始自动刷新
   */
  startAutoRefresh() {
    if (this.refreshTimer) return
    
    this.refreshTimer = setInterval(() => {
      this.loadExamData(true)
    }, this.options.refreshInterval)
  }

  /**
   * 停止自动刷新
   */
  stopAutoRefresh() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
  }

  // ==================== 计算属性 ====================

  /**
   * 获取考试状态选项
   */
  get examStatusOptions() {
    return examApi.getExamStatusOptions()
  }

  /**
   * 获取考试类型选项
   */
  get examTypeOptions() {
    return examApi.getPaperTypeOptions()
  }

  /**
   * 格式化考试状态
   */
  formatExamStatus(status) {
    return examApi.formatExamStatus(status)
  }

  /**
   * 格式化考试类型
   */
  formatExamType(type) {
    return examApi.formatPaperType(type)
  }

  /**
   * 获取状态标签类型
   */
  getStatusTagType(status) {
    const tagTypes = {
      draft: 'info',
      published: 'success',
      ongoing: 'warning',
      ended: 'primary',
      cancelled: 'danger'
    }
    return tagTypes[status] || 'info'
  }

  /**
   * 获取类型标签类型
   */
  getTypeTagType(type) {
    const tagTypes = {
      practice: 'success',
      mock: 'warning',
      formal: 'danger',
      placement: 'primary',
      diagnostic: 'info'
    }
    return tagTypes[type] || 'info'
  }
}

/**
 * 创建考试信息管理实例
 */
export function createExamInfoManager(options = {}) {
  return new ExamInfoManager(options)
}

/**
 * 全局考试信息管理实例（单例模式）
 */
let globalExamInfoManager = null

export function useExamInfoManager(options = {}) {
  if (!globalExamInfoManager) {
    globalExamInfoManager = new ExamInfoManager(options)
  }
  return globalExamInfoManager
}

/**
 * 考试信息组合式API
 * 用于在Vue组件中使用
 */
export function useExamInfo(options = {}) {
  const manager = useExamInfoManager(options)
  
  // 响应式引用
  const examList = computed(() => manager.state.examList)
  const filteredExams = computed(() => manager.state.filteredExams)
  const loading = computed(() => manager.state.loading)
  const saving = computed(() => manager.state.saving)
  const pagination = computed(() => manager.state.pagination)
  const searchParams = computed(() => manager.state.searchParams)
  const selectedExams = computed(() => manager.state.selectedExams)
  const currentExam = computed(() => manager.state.currentExam)
  const statistics = computed(() => manager.state.statistics)

  // 返回管理器方法和响应式数据
  return {
    // 响应式数据
    examList,
    filteredExams,
    loading,
    saving,
    pagination,
    searchParams,
    selectedExams,
    currentExam,
    statistics,
    
    // 数据操作方法
    loadExamData: manager.loadExamData.bind(manager),
    getExamById: manager.getExamById.bind(manager),
    createExam: manager.createExam.bind(manager),
    updateExam: manager.updateExam.bind(manager),
    deleteExam: manager.deleteExam.bind(manager),
    batchDeleteExams: manager.batchDeleteExams.bind(manager),
    
    // 状态管理方法
    startExam: manager.startExam.bind(manager),
    endExam: manager.endExam.bind(manager),
    publishExam: manager.publishExam.bind(manager),
    unpublishExam: manager.unpublishExam.bind(manager),
    
    // 搜索筛选方法
    search: manager.search.bind(manager),
    setSearchParams: manager.setSearchParams.bind(manager),
    resetSearchParams: manager.resetSearchParams.bind(manager),
    
    // 分页方法
    setPagination: manager.setPagination.bind(manager),
    goToPage: manager.goToPage.bind(manager),
    changePageSize: manager.changePageSize.bind(manager),
    
    // 选择方法
    selectExams: manager.selectExams.bind(manager),
    toggleExamSelection: manager.toggleExamSelection.bind(manager),
    toggleSelectAll: manager.toggleSelectAll.bind(manager),
    clearSelection: manager.clearSelection.bind(manager),
    setCurrentExam: manager.setCurrentExam.bind(manager),
    
    // 导入导出方法
    exportExamData: manager.exportExamData.bind(manager),
    importExamData: manager.importExamData.bind(manager),
    
    // 工具方法
    getExamAnalysis: manager.getExamAnalysis.bind(manager),
    formatExamStatus: manager.formatExamStatus.bind(manager),
    formatExamType: manager.formatExamType.bind(manager),
    getStatusTagType: manager.getStatusTagType.bind(manager),
    getTypeTagType: manager.getTypeTagType.bind(manager),
    
    // 选项数据
    examStatusOptions: manager.examStatusOptions,
    examTypeOptions: manager.examTypeOptions,
    
    // 管理器实例（用于高级用法）
    manager
  }
}

export default ExamInfoManager

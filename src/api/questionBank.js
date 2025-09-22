import api from './index'

// 题库管理相关API (适配Java后台接口)
export const questionBankApi = {
  // ==================== 听力理解选择题管理 (ListeningMcqController) ====================
  
  // 获取听力选择题列表
  getListeningMCQQuestions(params = {}) {
    const { page = 0, size = 20, ...otherParams } = params
    return api.get('/listening-mcq/questions', { 
      params: { page, size, ...otherParams }
    })
  },

  // 根据ID获取听力选择题
  getListeningMCQQuestionById(id) {
    return api.get(`/listening-mcq/questions/${id}`)
  },

  // 根据模块ID获取听力选择题
  getListeningMCQQuestionsByModule(moduleId, params = {}) {
    const { page = 0, size = 20 } = params
    return api.get(`/listening-mcq/questions/module/${moduleId}`, {
      params: { page, size }
    })
  },

  // 创建听力选择题
  createListeningMCQQuestion(data) {
    return api.post('/listening-mcq/questions', data)
  },

  // 更新听力选择题
  updateListeningMCQQuestion(id, data) {
    return api.put(`/listening-mcq/questions/${id}`, data)
  },

  // 删除听力选择题
  deleteListeningMCQQuestion(id) {
    return api.delete(`/listening-mcq/questions/${id}`)
  },

  // 搜索听力选择题
  searchListeningMCQQuestions(params = {}) {
    return api.get('/listening-mcq/questions/search', { params })
  },

  // 复制听力选择题
  copyListeningMCQQuestion(id) {
    return api.post(`/listening-mcq/questions/${id}/copy`)
  },

  // 获取题目选项
  getListeningMCQChoices(questionId) {
    return api.get(`/listening-mcq/questions/${questionId}/choices`)
  },

  // 创建选项
  createListeningMCQChoice(data) {
    return api.post('/listening-mcq/choices', data)
  },

  // 创建标准选项
  createStandardListeningMCQChoices(questionId, data) {
    return api.post(`/listening-mcq/questions/${questionId}/choices/standard`, data)
  },

  // 更新选项
  updateListeningMCQChoice(id, data) {
    return api.put(`/listening-mcq/choices/${id}`, data)
  },

  // 删除选项
  deleteListeningMCQChoice(id) {
    return api.delete(`/listening-mcq/choices/${id}`)
  },

  // 设置正确答案
  setCorrectChoice(id) {
    return api.put(`/listening-mcq/choices/${id}/correct`)
  },

  // 批量删除听力选择题
  batchDeleteListeningMCQQuestions(questionIds) {
    return api.delete('/listening-mcq/questions/batch', { data: questionIds })
  },

  // 获取听力选择题统计
  getListeningMCQStats(questionId) {
    return api.get(`/listening-mcq/questions/${questionId}/stats`)
  },

  // 题库管理 - 听力选择题
  getQuestionBankMCQ(params = {}) {
    return api.get('/listening-mcq/question-bank/mcq', { params })
  },

  createQuestionBankMCQ(data) {
    return api.post('/listening-mcq/question-bank/mcq', data)
  },

  updateQuestionBankMCQ(id, data) {
    return api.put(`/listening-mcq/question-bank/mcq/${id}`, data)
  },

  deleteQuestionBankMCQ(id) {
    return api.delete(`/listening-mcq/question-bank/mcq/${id}`)
  },

  // ==================== 故事复述题管理 (StoryRetellController) ====================
  
  // 获取故事复述题列表
  getStoryRetellItems(params = {}) {
    const { page = 0, size = 20, sort = ['createdAt', 'desc'] } = params
    return api.get('/story-retell/items', { 
      params: { page, size, sort }
    })
  },

  // 根据ID获取故事复述题
  getStoryRetellItemById(id) {
    return api.get(`/story-retell/items/${id}`)
  },

  // 根据模块ID获取故事复述题
  getStoryRetellItemsByModule(moduleId, params = {}) {
    const { page = 0, size = 20, sort = ['createdAt', 'desc'] } = params
    return api.get(`/story-retell/items/module/${moduleId}`, {
      params: { page, size, sort }
    })
  },

  // 创建故事复述题
  createStoryRetellItem(data) {
    return api.post('/story-retell/items', data)
  },

  // 更新故事复述题
  updateStoryRetellItem(id, data) {
    return api.put(`/story-retell/items/${id}`, data)
  },

  // 删除故事复述题
  deleteStoryRetellItem(id) {
    return api.delete(`/story-retell/items/${id}`)
  },

  // 复制故事复述题
  copyStoryRetellItem(id) {
    return api.post(`/story-retell/items/${id}/copy`)
  },

  // 搜索故事复述题
  searchStoryRetellItems(params = {}) {
    return api.get('/story-retell/items/search', { params })
  },

  // 获取故事复述题统计
  getStoryRetellStats(itemId) {
    return api.get(`/story-retell/items/${itemId}/stats`)
  },

  // 批量删除故事复述题
  batchDeleteStoryRetellItems(itemIds) {
    return api.delete('/story-retell/items/batch', { data: itemIds })
  },

  // 批量更新故事复述题状态
  batchUpdateStoryRetellItemsStatus(itemIds, enabled) {
    return api.put('/story-retell/items/batch/status', { itemIds, enabled })
  },

  // 验证故事复述题数据
  validateStoryRetellItem(data) {
    const errors = []
    
    if (!data.title || data.title.trim().length === 0) {
      errors.push('故事标题不能为空')
    }
    
    if (!data.content || data.content.trim().length === 0) {
      errors.push('故事内容不能为空')
    }
    
    if (data.content && (data.content.length < 50 || data.content.length > 500)) {
      errors.push('故事内容长度应在50-500个字符之间')
    }
    
    if (!data.audioUrl) {
      errors.push('必须上传音频文件')
    }
    
    if (!data.difficulty || data.difficulty < 1 || data.difficulty > 6) {
      errors.push('请选择有效的难度等级')
    }
    
    // 验证考试参数
    if (data.preparationTime && (data.preparationTime < 60 || data.preparationTime > 600)) {
      errors.push('准备时间应在60-600秒之间')
    }
    
    if (data.responseTime && (data.responseTime < 120 || data.responseTime > 600)) {
      errors.push('回答时间应在120-600秒之间')
    }
    
    return {
      isValid: errors.length === 0,
      errors
    }
  },

  // 获取故事复述题默认配置
  getStoryRetellDefaultConfig() {
    return {
      preparationTime: 300, // 准备时间300秒
      responseTime: 300,    // 回答时间300秒
      playCount: 2,         // 播放次数2次
      playInterval: 5,      // 播放间隔5秒
      wordCountMin: 100,    // 最小词数100词
      wordCountMax: 120,    // 最大词数120词
      autoNext: true,       // 时间结束自动跳转
      allowPause: false,    // 不允许暂停
      showTimer: true       // 显示计时器
    }
  },

  // 获取故事复述题评分标准
  getStoryRetellScoringCriteria() {
    return [
      {
        criteria: 'content_accuracy',
        label: '内容准确性',
        weight: 30,
        description: '复述内容与原文的符合程度'
      },
      {
        criteria: 'language_fluency',
        label: '语言流利性',
        weight: 25,
        description: '表达的流畅程度和连贯性'
      },
      {
        criteria: 'pronunciation',
        label: '发音准确性',
        weight: 20,
        description: '发音的清晰度和准确性'
      },
      {
        criteria: 'grammar_usage',
        label: '语法运用',
        weight: 15,
        description: '语法结构的正确使用'
      },
      {
        criteria: 'vocabulary_usage',
        label: '词汇运用',
        weight: 10,
        description: '词汇的准确性和丰富性'
      }
    ]
  },

  // ==================== 听力理解对话管理 (LsaDialogsController - 听力简答) ====================
  
  // 获取听力对话列表
  getListeningDialogs(params = {}) {
    const { page = 0, size = 20, sort = ['displayOrder', 'asc'] } = params
    return api.get('/lsa-dialogs/dialogs', { 
      params: { page, size, sort }
    })
  },

  // 根据ID获取听力对话
  getListeningDialogById(id) {
    return api.get(`/lsa-dialogs/dialogs/${id}`)
  },

  // 根据模块ID获取听力对话
  getListeningDialogsByModule(moduleId, params = {}) {
    const { page = 0, size = 20, sort = ['displayOrder', 'asc'] } = params
    return api.get(`/lsa-dialogs/dialogs/module/${moduleId}`, {
      params: { page, size, sort }
    })
  },

  // 创建听力对话
  createListeningDialog(data) {
    return api.post('/lsa-dialogs/dialogs', data)
  },

  // 更新听力对话
  updateListeningDialog(id, data) {
    return api.put(`/lsa-dialogs/dialogs/${id}`, data)
  },

  // 删除听力对话
  deleteListeningDialog(id) {
    return api.delete(`/lsa-dialogs/dialogs/${id}`)
  },

  // 获取对话问题
  getListeningDialogQuestions(dialogId, params = {}) {
    const { page = 0, size = 20 } = params
    return api.get(`/lsa-dialogs/dialogs/${dialogId}/questions`, {
      params: { page, size }
    })
  },

  // 创建对话问题
  createListeningDialogQuestion(data) {
    return api.post('/lsa-dialogs/questions', data)
  },

  // 更新对话问题
  updateListeningDialogQuestion(id, data) {
    return api.put(`/lsa-dialogs/questions/${id}`, data)
  },

  // 删除对话问题
  deleteListeningDialogQuestion(id) {
    return api.delete(`/lsa-dialogs/questions/${id}`)
  },

  // 获取问题选项
  getListeningDialogQuestionChoices(questionId) {
    return api.get(`/lsa-dialogs/questions/${questionId}/choices`)
  },

  // 创建问题选项
  createListeningDialogQuestionChoice(data) {
    return api.post('/lsa-dialogs/question-choices', data)
  },

  // 更新问题选项
  updateListeningDialogQuestionChoice(id, data) {
    return api.put(`/lsa-dialogs/question-choices/${id}`, data)
  },

  // 删除问题选项
  deleteListeningDialogQuestionChoice(id) {
    return api.delete(`/lsa-dialogs/question-choices/${id}`)
  },

  // 批量创建问题选项
  batchCreateListeningDialogQuestionChoices(questionId, choices) {
    return api.post(`/lsa-dialogs/questions/${questionId}/choices/batch`, { choices })
  },

  // 设置正确选项
  setListeningDialogCorrectChoice(choiceId) {
    return api.put(`/lsa-dialogs/question-choices/${choiceId}/correct`)
  },

  // 搜索听力对话
  searchListeningDialogs(params = {}) {
    return api.get('/lsa-dialogs/dialogs/search', { params })
  },

  // 批量删除听力对话
  batchDeleteListeningDialogs(dialogIds) {
    return api.delete('/lsa-dialogs/dialogs/batch', { data: dialogIds })
  },

  // 批量删除对话问题
  batchDeleteListeningDialogQuestions(questionIds) {
    return api.delete('/lsa-dialogs/questions/batch', { data: questionIds })
  },

  // 批量删除问题选项
  batchDeleteListeningDialogQuestionChoices(choiceIds) {
    return api.delete('/lsa-dialogs/question-choices/batch', { data: choiceIds })
  },

  // ==================== 听力简答考试流程管理 ====================
  
  // 获取听力简答考试配置
  getListeningComprehensionExamConfig() {
    return {
      totalDialogs: 2,           // 总对话数：2段
      questionsPerDialog: 3,     // 每段对话问题数：3个
      totalQuestions: 6,         // 总问题数：6道
      totalExamTime: 300,        // 总考试时间：5分钟（300秒）
      dialogWordCount: {         // 对话词数要求
        min: 100,
        max: 120
      },
      questionWordLimit: 15,     // 问题字数限制：15词以内
      answerTimePerQuestion: 20, // 每个问题回答时间：20秒
      playCount: 1,              // 音频播放次数：仅1次
      autoNext: true,            // 超时自动跳转
      recordResponse: true,      // 录音回答
      showTimer: true            // 显示计时器
    }
  },

  // 验证听力简答考试数据
  validateListeningComprehensionExam(examData) {
    const errors = []
    const config = this.getListeningComprehensionExamConfig()
    
    if (!examData.dialogs || examData.dialogs.length !== config.totalDialogs) {
      errors.push(`必须包含${config.totalDialogs}段对话`)
    }
    
    examData.dialogs?.forEach((dialog, index) => {
      if (!dialog.content || dialog.content.trim().length === 0) {
        errors.push(`第${index + 1}段对话内容不能为空`)
      }
      
      const wordCount = this.getWordCount(dialog.content)
      if (wordCount < config.dialogWordCount.min || wordCount > config.dialogWordCount.max) {
        errors.push(`第${index + 1}段对话词数应在${config.dialogWordCount.min}-${config.dialogWordCount.max}词之间`)
      }
      
      if (!dialog.audioUrl) {
        errors.push(`第${index + 1}段对话必须上传音频文件`)
      }
      
      if (!dialog.questions || dialog.questions.length !== config.questionsPerDialog) {
        errors.push(`第${index + 1}段对话必须包含${config.questionsPerDialog}个问题`)
      }
      
      dialog.questions?.forEach((question, qIndex) => {
        const questionWordCount = this.getWordCount(question.questionText)
        if (questionWordCount > config.questionWordLimit) {
          errors.push(`第${index + 1}段对话第${qIndex + 1}个问题字数不能超过${config.questionWordLimit}词`)
        }
      })
    })
    
    return {
      isValid: errors.length === 0,
      errors
    }
  },

  // 获取听力简答评分标准
  getListeningComprehensionScoringCriteria() {
    return [
      {
        criteria: 'content_accuracy',
        label: '内容准确性',
        weight: 35,
        description: '回答内容与问题的相关性和准确性'
      },
      {
        criteria: 'language_fluency',
        label: '语言流利性',
        weight: 25,
        description: '表达的流畅程度和自然性'
      },
      {
        criteria: 'pronunciation',
        label: '发音清晰度',
        weight: 20,
        description: '发音的清晰度和可理解性'
      },
      {
        criteria: 'grammar_usage',
        label: '语法正确性',
        weight: 12,
        description: '语法结构的正确使用'
      },
      {
        criteria: 'response_time',
        label: '回答时效性',
        weight: 8,
        description: '在规定时间内完成回答的能力'
      }
    ]
  },

  // 计算听力简答考试统计
  calculateListeningComprehensionStats(examData, results) {
    const config = this.getListeningComprehensionExamConfig()
    
    return {
      totalDialogs: examData.dialogs?.length || 0,
      totalQuestions: examData.dialogs?.reduce((sum, dialog) => sum + (dialog.questions?.length || 0), 0) || 0,
      averageDialogWordCount: examData.dialogs?.reduce((sum, dialog) => sum + this.getWordCount(dialog.content), 0) / (examData.dialogs?.length || 1) || 0,
      averageQuestionWordCount: this.calculateAverageQuestionWordCount(examData.dialogs),
      totalExamDuration: this.calculateTotalExamDuration(examData.dialogs, config),
      completionRate: results?.filter(r => r.isCompleted).length / (results?.length || 1) * 100 || 0,
      averageScore: results?.reduce((sum, r) => sum + (r.score || 0), 0) / (results?.length || 1) || 0
    }
  },

  // 辅助方法：计算平均问题词数
  calculateAverageQuestionWordCount(dialogs) {
    if (!dialogs || dialogs.length === 0) return 0
    
    const totalWords = dialogs.reduce((sum, dialog) => {
      return sum + (dialog.questions?.reduce((qSum, question) => {
        return qSum + this.getWordCount(question.questionText)
      }, 0) || 0)
    }, 0)
    
    const totalQuestions = dialogs.reduce((sum, dialog) => sum + (dialog.questions?.length || 0), 0)
    
    return totalQuestions > 0 ? totalWords / totalQuestions : 0
  },

  // 辅助方法：计算总考试时长
  calculateTotalExamDuration(dialogs, config) {
    if (!dialogs || dialogs.length === 0) return 0
    
    // 音频播放时间 + 回答时间
    const audioTime = dialogs.reduce((sum, dialog) => sum + (dialog.audioDuration || 60), 0) // 默认60秒每段
    const answerTime = dialogs.reduce((sum, dialog) => sum + (dialog.questions?.length || 0) * config.answerTimePerQuestion, 0)
    
    return audioTime + answerTime
  },

  // 辅助方法：计算词数
  getWordCount(text) {
    if (!text) return 0
    return text.trim().split(/\s+/).filter(word => word.length > 0).length
  },

  // ==================== ATC模拟管理 (AtcSimController - 模拟通话) ====================
  
  // 获取机场列表
  getATCAirports(params = {}) {
    const { page = 0, size = 20, sortBy = 'id', sortDir = 'ASC' } = params
    return api.get('/atc-sim/airports', { 
      params: { page, size, sortBy, sortDir }
    })
  },

  // 根据ID获取机场
  getATCAirportById(id) {
    return api.get(`/atc-sim/airports/${id}`)
  },

  // 创建机场
  createATCAirport(data) {
    return api.post('/atc-sim/airports', data)
  },

  // 更新机场
  updateATCAirport(id, data) {
    return api.put(`/atc-sim/airports/${id}`, data)
  },

  // 删除机场
  deleteATCAirport(id) {
    return api.delete(`/atc-sim/airports/${id}`)
  },

  // 获取ATC场景列表
  getATCScenarios(params = {}) {
    const { page = 0, size = 20, sortBy = 'id', sortDir = 'ASC' } = params
    return api.get('/atc-sim/scenarios', { 
      params: { page, size, sortBy, sortDir }
    })
  },

  // 根据ID获取ATC场景
  getATCScenarioById(id) {
    return api.get(`/atc-sim/scenarios/${id}`)
  },

  // 根据机场ID获取ATC场景
  getATCScenariosByAirport(airportId, params = {}) {
    const { page = 0, size = 20 } = params
    return api.get(`/atc-sim/scenarios/airport/${airportId}`, {
      params: { page, size }
    })
  },

  // 创建ATC场景
  createATCScenario(data) {
    return api.post('/atc-sim/scenarios', data)
  },

  // 更新ATC场景
  updateATCScenario(id, data) {
    return api.put(`/atc-sim/scenarios/${id}`, data)
  },

  // 删除ATC场景
  deleteATCScenario(id) {
    return api.delete(`/atc-sim/scenarios/${id}`)
  },

  // 获取ATC轮次
  getATCTurns(scenarioId, params = {}) {
    const { page = 0, size = 20 } = params
    return api.get(`/atc-sim/scenarios/${scenarioId}/turns`, {
      params: { page, size }
    })
  },

  // 根据ID获取ATC轮次
  getATCTurnById(id) {
    return api.get(`/atc-sim/turns/${id}`)
  },

  // 创建ATC轮次
  createATCTurn(data) {
    return api.post('/atc-sim/turns', data)
  },

  // 更新ATC轮次
  updateATCTurn(id, data) {
    return api.put(`/atc-sim/turns/${id}`, data)
  },

  // 删除ATC轮次
  deleteATCTurn(id) {
    return api.delete(`/atc-sim/turns/${id}`)
  },

  // 搜索ATC场景
  searchATCScenarios(params = {}) {
    return api.get('/atc-sim/scenarios/search', { params })
  },

  // 批量删除ATC场景
  batchDeleteATCScenarios(scenarioIds) {
    return api.delete('/atc-sim/scenarios/batch', { data: scenarioIds })
  },

  // ==================== OPI口语面试管理 (OpiController) ====================
  
  // 获取OPI话题列表
  getOPITopics(params = {}) {
    const { page = 0, size = 20, sort = ['order', 'asc'] } = params
    return api.get('/opi/topics', { 
      params: { page, size, sort }
    })
  },

  // 根据ID获取OPI话题
  getOPITopicById(id) {
    return api.get(`/opi/topics/${id}`)
  },

  // 根据模块ID获取OPI话题
  getOPITopicsByModule(moduleId, params = {}) {
    const { page = 0, size = 20, sort = ['order', 'asc'] } = params
    return api.get(`/opi/topics/module/${moduleId}`, {
      params: { page, size, sort }
    })
  },

  // 创建OPI话题
  createOPITopic(data) {
    return api.post('/opi/topics', data)
  },

  // 更新OPI话题
  updateOPITopic(id, data) {
    return api.put(`/opi/topics/${id}`, data)
  },

  // 删除OPI话题
  deleteOPITopic(id) {
    return api.delete(`/opi/topics/${id}`)
  },

  // 获取OPI问题列表
  getOPIQuestions(params = {}) {
    const { page = 0, size = 20, sort = ['order', 'asc'] } = params
    return api.get('/opi/questions', { 
      params: { page, size, sort }
    })
  },

  // 根据ID获取OPI问题
  getOPIQuestionById(id) {
    return api.get(`/opi/questions/${id}`)
  },

  // 根据话题ID获取OPI问题
  getOPIQuestionsByTopic(topicId, params = {}) {
    const { page = 0, size = 20 } = params
    return api.get(`/opi/questions/topic/${topicId}`, {
      params: { page, size }
    })
  },

  // 创建OPI问题
  createOPIQuestion(data) {
    return api.post('/opi/questions', data)
  },

  // 更新OPI问题
  updateOPIQuestion(id, data) {
    return api.put(`/opi/questions/${id}`, data)
  },

  // 删除OPI问题
  deleteOPIQuestion(id) {
    return api.delete(`/opi/questions/${id}`)
  },

  // 搜索OPI话题
  searchOPITopics(params = {}) {
    return api.get('/opi/topics/search', { params })
  },

  // 批量删除OPI话题
  batchDeleteOPITopics(topicIds) {
    return api.delete('/opi/topics/batch', { data: topicIds })
  },

  // 批量删除OPI问题
  batchDeleteOPIQuestions(questionIds) {
    return api.delete('/opi/questions/batch', { data: questionIds })
  },

  // ==================== 文件上传管理 ====================

  // 上传音频文件 (听力选择题)
  uploadListeningMCQAudio(file, options = {}) {
    const formData = new FormData()
    formData.append('file', file)
    Object.keys(options).forEach(key => {
      formData.append(key, options[key])
    })
    return api.post('/listening-mcq/question-bank/upload/audio', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    })
  },

  // 批量上传音频文件
  batchUploadListeningMCQAudio(files, options = {}) {
    const formData = new FormData()
    files.forEach((file, index) => {
      formData.append('files', file)
    })
    Object.keys(options).forEach(key => {
      formData.append(key, options[key])
    })
    return api.post('/listening-mcq/question-bank/upload/audio/batch', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    })
  },

  // 上传图片文件
  uploadQuestionImage(file, options = {}) {
    const formData = new FormData()
    formData.append('file', file)
    Object.keys(options).forEach(key => {
      formData.append(key, options[key])
    })
    return api.post('/listening-mcq/question-bank/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 30000
    })
  },

  // 删除图片文件
  deleteQuestionImage(imageUrl) {
    return api.delete('/listening-mcq/question-bank/upload/image', { 
      data: { imageUrl } 
    })
  },

  // ==================== 数据导入导出 ====================

  // 导入题目
  importQuestions(file, options = {}) {
    const formData = new FormData()
    formData.append('file', file)
    Object.keys(options).forEach(key => {
      formData.append(key, options[key])
    })
    return api.post('/listening-mcq/question-bank/questions/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 180000
    })
  },

  // 导出题目
  exportQuestions(params = {}) {
    return api.get('/listening-mcq/question-bank/questions/export', {
      params,
      responseType: 'blob',
      timeout: 120000
    })
  },

  // 导出题目模板
  exportQuestionTemplate(type = 'all') {
    return api.get('/listening-mcq/question-bank/questions/export/template', {
      params: { type },
      responseType: 'blob'
    })
  },

  // 验证导入文件
  validateImportFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/listening-mcq/question-bank/questions/import/validate', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 根据模块导出
  exportByModule(moduleId, params = {}) {
    return api.get(`/listening-mcq/modules/${moduleId}/export`, {
      params,
      responseType: 'blob'
    })
  },

  // 根据模块导入
  importByModule(moduleId, file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`/listening-mcq/modules/${moduleId}/import`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // ==================== 统计分析 ====================
  
  // 获取模块统计
  getModuleStats(moduleId) {
    return api.get(`/listening-mcq/modules/${moduleId}/stats`)
  },

  // 获取整体统计
  getOverallStats() {
    return api.get('/listening-mcq/stats/overall')
  },

  // 获取题库统计 (通用)
  getQuestionBankStats() {
    return this.getOverallStats()
  },

  // ==================== 工具方法 ====================
  
  // 验证音频文件
  validateAudioFile(file) {
    const allowedTypes = [
      'audio/mpeg', 
      'audio/mp3', 
      'audio/wav', 
      'audio/ogg', 
      'audio/m4a',
      'audio/aac'
    ]
    const maxSize = 50 * 1024 * 1024 // 50MB

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
        message: '音频文件大小不能超过 50MB' 
      }
    }

    return { valid: true }
  },

  // 验证图片文件
  validateImageFile(file) {
    const allowedTypes = [
      'image/jpeg', 
      'image/jpg', 
      'image/png', 
      'image/gif', 
      'image/webp'
    ]
    const maxSize = 10 * 1024 * 1024 // 10MB

    if (!file) {
      return { valid: false, message: '请选择图片文件' }
    }

    if (!allowedTypes.includes(file.type)) {
      return { 
        valid: false, 
        message: '只支持 JPG、PNG、GIF、WebP 格式的图片文件' 
      }
    }

    if (file.size > maxSize) {
      return { 
        valid: false, 
        message: '图片文件大小不能超过 10MB' 
      }
    }

    return { valid: true }
  },

  // 验证导入文件格式
  validateImportFileFormat(file) {
    const allowedTypes = [
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
      'application/vnd.ms-excel', // .xls
      'text/csv', // .csv
      'application/json' // .json
    ]
    const maxSize = 100 * 1024 * 1024 // 100MB

    if (!file) {
      return { valid: false, message: '请选择要导入的文件' }
    }

    if (!allowedTypes.includes(file.type)) {
      return { 
        valid: false, 
        message: '只支持 Excel (.xlsx, .xls)、CSV 和 JSON 格式的文件' 
      }
    }

    if (file.size > maxSize) {
      return { 
        valid: false, 
        message: '导入文件大小不能超过 100MB' 
      }
    }

    return { valid: true }
  },

  // 获取题目类型选项
  getQuestionTypes() {
    return [
      { 
        value: 'listening_comprehension', 
        label: '听力理解', 
        icon: 'Service', 
        color: '#409EFF',
        description: '基于对话的听力理解问答',
        endpoint: 'lsa-dialogs'
      },
      { 
        value: 'listening_mcq', 
        label: '听力选择题', 
        icon: 'List', 
        color: '#67C23A',
        description: '基于音频内容的多选题',
        endpoint: 'listening-mcq'
      },
      { 
        value: 'story_retell', 
        label: '故事复述', 
        icon: 'ChatDotRound', 
        color: '#F56C6C',
        description: '听后复述能力测试',
        endpoint: 'story-retell'
      },
      { 
        value: 'atc_simulation', 
        label: '模拟通话', 
        icon: 'Connection', 
        color: '#909399',
        description: '航空交通管制模拟训练',
        endpoint: 'atc-sim'
      },
      { 
        value: 'opi', 
        label: '口语能力面试', 
        icon: 'Microphone', 
        color: '#E6A23C',
        description: '口语面试和评估',
        endpoint: 'opi'
      }
    ]
  },

  // 获取难度等级选项
  getDifficultyLevels() {
    return [
      { value: 1, label: '入门', color: '#67C23A', description: '适合初学者' },
      { value: 2, label: '基础', color: '#95D475', description: '基础水平学员' },
      { value: 3, label: '中级', color: '#E6A23C', description: '中等水平学员' },
      { value: 4, label: '中高级', color: '#F78989', description: '中高级水平学员' },
      { value: 5, label: '高级', color: '#F56C6C', description: '高级水平学员' },
      { value: 6, label: '专家级', color: '#909399', description: '专业级水平' }
    ]
  },

  // 获取ATC场景类型
  getATCScenarioTypes() {
    return [
      { value: 'departure', label: '离场', icon: 'Top', color: '#67C23A' },
      { value: 'approach', label: '进场', icon: 'Bottom', color: '#409EFF' },
      { value: 'ground', label: '地面', icon: 'MapLocation', color: '#909399' },
      { value: 'tower', label: '塔台', icon: 'OfficeBuilding', color: '#E6A23C' },
      { value: 'emergency', label: '紧急情况', icon: 'Warning', color: '#F56C6C' },
      { value: 'weather', label: '天气相关', icon: 'Cloudy', color: '#409EFF' }
    ]
  },

  // 格式化题目类型
  formatQuestionType(type) {
    const typeMap = {
      'listening_comprehension': '听力理解',
      'listening_mcq': '听力选择题',
      'story_retell': '故事复述',
      'atc_simulation': '模拟通话',
      'opi': '口语能力面试'
    }
    return typeMap[type] || type
  },

  // 格式化难度等级
  formatDifficultyLevel(level) {
    const levelMap = {
      1: '入门',
      2: '基础',
      3: '中级',
      4: '中高级',
      5: '高级',
      6: '专家级'
    }
    return levelMap[level] || `等级${level}`
  },

  // 格式化文件大小
  formatFileSize(bytes) {
    if (!bytes) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  },

  // 生成题目URL
  generateQuestionUrl(questionId, type = 'preview') {
    return `/question-bank/${type}/${questionId}`
  },

  // 生成音频URL
  generateAudioUrl(audioPath, quality = 'medium') {
    if (!audioPath) return ''
    return `${audioPath}?quality=${quality}&t=${Date.now()}`
  },

  // ==================== 兼容性方法 (保持向后兼容) ====================

  // 兼容旧的方法名
  getListeningQuestions(params = {}) {
    return this.getListeningDialogs(params)
  },

  getMCQQuestions(params = {}) {
    return this.getListeningMCQQuestions(params)
  },

  getRetellQuestions(params = {}) {
    return this.getStoryRetellItems(params)
  },

  getATCQuestions(params = {}) {
    return this.getATCScenarios(params)
  },

  getOPIQuestions(params = {}) {
    return this.getOPITopics(params)
  },

  // 兼容创建方法
  createListeningQuestion(data) {
    return this.createListeningDialog(data)
  },

  createMCQQuestion(data) {
    return this.createListeningMCQQuestion(data)
  },

  createRetellQuestion(data) {
    return this.createStoryRetellItem(data)
  },

  createATCQuestion(data) {
    return this.createATCScenario(data)
  },

  createOPIQuestion(data) {
    return this.createOPITopic(data)
  },

  // 兼容更新方法
  updateListeningQuestion(id, data) {
    return this.updateListeningDialog(id, data)
  },

  updateMCQQuestion(id, data) {
    return this.updateListeningMCQQuestion(id, data)
  },

  updateRetellQuestion(id, data) {
    return this.updateStoryRetellItem(id, data)
  },

  updateATCQuestion(id, data) {
    return this.updateATCScenario(id, data)
  },

  updateOPIQuestion(id, data) {
    return this.updateOPITopic(id, data)
  },

  // 兼容删除方法
  deleteListeningQuestion(id) {
    return this.deleteListeningDialog(id)
  },

  deleteMCQQuestion(id) {
    return this.deleteListeningMCQQuestion(id)
  },

  deleteRetellQuestion(id) {
    return this.deleteStoryRetellItem(id)
  },

  deleteATCQuestion(id) {
    return this.deleteATCScenario(id)
  },

  deleteOPIQuestion(id) {
    return this.deleteOPITopic(id)
  },

  // 兼容批量删除方法
  batchDeleteQuestions(questionIds) {
    // 需要根据具体类型调用相应的批量删除方法
    return this.batchDeleteListeningMCQQuestions(questionIds)
  }
}

export default questionBankApi
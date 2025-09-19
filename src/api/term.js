import api from './index'

// 术语管理相关API
export const termApi = {
  // 获取术语列表
  getTerms(params = {}) {
    return api.get('/terms', { params })
  },

  // 获取术语详情
  getTermById(id) {
    return api.get(`/terms/${id}`)
  },

  // 创建术语
  createTerm(data) {
    return api.post('/terms', data)
  },

  // 批量操作术语
  batchOperateTerms(data) {
    return api.post('/terms/batch', data)
  },

  // 更新术语
  updateTerm(id, data) {
    return api.put(`/terms/${id}`, data)
  },

  // 删除术语
  deleteTerm(id) {
    return api.delete(`/terms/${id}`)
  },

  // 术语主题映射
  associateTermTopic(termId, topicId, data) {
    return api.post(`/terms/${termId}/topics/${topicId}`, data)
  },

  // 取消术语主题映射
  disassociateTermTopic(termId, topicId) {
    return api.delete(`/terms/${termId}/topics/${topicId}`)
  },

  // 获取术语分类
  getTermCategories() {
    return [
      'COMMUNICATION',
      'NAVIGATION',
      'METEOROLOGY',
      'EMERGENCY',
      'AIRCRAFT',
      'AIRPORT',
      'PROCEDURES',
      'EQUIPMENT',
      'GENERAL'
    ]
  },

  // 获取术语来源
  getTermSources() {
    return [
      'ICAO DOC4444',
      'ICAO DOC8168',
      'ICAO DOC9432',
      'ICAO Annex 10',
      'ICAO Annex 11',
      'ICAO Annex 14',
      'IATA',
      'FAA',
      'EUROCONTROL',
      'Custom'
    ]
  },

  // 导出术语
  exportTerms(params = {}) {
    return api.get('/terms/export', { 
      params,
      responseType: 'blob'
    })
  },

  // 导入术语
  importTerms(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/terms/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

export default termApi

import api from './index'

// 用户管理相关API (管理员端，不包含微信登录)
export const userApi = {
  
  // 获取RSA公钥（用于混合加密）
  getRSAPublicKey() {
    return api.get('/security/public-key')
  },
  
  // ==================== 普通用户管理 ====================
  
  // 根据ID获取用户信息
  getUserById(id) {
    return api.get(`/users/${id}`)
  },

  // 根据OpenID获取用户信息
  getUserByOpenId(openid) {
    return api.get(`/users/openid/${openid}`)
  },

  // 更新用户信息
  updateUser(id, data) {
    return api.put(`/users/${id}`, data)
  },

  // 删除用户（软删除）
  deleteUser(id) {
    return api.delete(`/users/${id}`)
  },

  // 物理删除用户
  hardDeleteUser(id) {
    return api.delete(`/users/${id}/hard`)
  },

  // 获取用户列表（分页）
  getUsers(params = {}) {
    return api.get('/users', { params })
  },

  // 更新用户状态
  updateUserStatus(id, status) {
    return api.put(`/users/${id}/status`, { status })
  },

  // 批量删除用户
  batchDeleteUsers(userIds) {
    return api.delete('/users/batch', { data: { userIds } })
  },

  // 重置用户密码
  resetUserPassword(id) {
    return api.post(`/users/${id}/reset-password`)
  },

  // 导出用户数据
  exportUsers(params = {}) {
    return api.get('/users/export', { 
      params,
      responseType: 'blob'
    })
  },


  // ==================== 用户标记管理 ====================
  
  // 获取用户词汇标记
  getUserVocabFlags(params = {}) {
    return api.get('/user-flags/vocab', { params })
  },

  // 添加词汇标记
  addVocabFlag(data) {
    return api.post('/user-flags/vocab', data)
  },

  // 删除词汇标记
  removeVocabFlag(vocabId, flagType) {
    return api.delete(`/user-flags/vocab/${vocabId}/flag/${flagType}`)
  },

  // 获取用户术语标记
  getUserTermFlags(params = {}) {
    return api.get('/user-flags/terms', { params })
  },

  // 添加术语标记
  addTermFlag(data) {
    return api.post('/user-flags/terms', data)
  },

  // 删除术语标记
  removeTermFlag(termId, flagType) {
    return api.delete(`/user-flags/terms/${termId}/flag/${flagType}`)
  },

  // ==================== 工具方法 ====================
  
  // 获取用户标记类型选项
  getFlagTypes() {
    return [
      { value: 'BOOKMARKED', label: '已收藏' },
      { value: 'LEARNED', label: '已学习' },
      { value: 'DIFFICULT', label: '困难' }
    ]
  }
}

export default userApi

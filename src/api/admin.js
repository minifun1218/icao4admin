import api from './index'

// 系统管理员相关API
export const adminApi = {
  // ==================== RSA加密相关 ====================
  
  // 获取RSA公钥（用于混合加密）
  getRSAPublicKey() {
    return api.get('/security/public-key')
  },

  // ==================== 管理员认证相关 ====================
  
  // 管理员注册 (需要SUPER_ADMIN权限)
  registerAdmin(data) {
    return api.post('/admin/register', data)
  },

  // 管理员登录
  loginAdmin(credentials) {
    return api.post('/admin/login', credentials)
  },

  // 获取当前管理员信息
  getCurrentAdmin() {
    return api.get('/admin/me')
  },

  // 更新当前管理员信息
  updateCurrentAdmin(data) {
    return api.put('/admin/me', data)
  },

  // 修改密码
  changePassword(data) {
    return api.put('/admin/password', data)
  },

  // 管理员登出
  logoutAdmin() {
    return api.post('/admin/logout')
  },

  // 刷新Token
  refreshToken(refreshToken) {
    return api.post('/admin/refresh', { refreshToken })
  },

  // ==================== 管理员管理 (CRUD) ====================
  
  // 获取管理员列表（分页）
  getAdmins(params = {}) {
    return api.get('/admin', { params })
  },

  // 根据ID获取管理员详情
  getAdminById(id) {
    return api.get(`/admin/${id}`)
  },

  // 创建管理员
  createAdmin(data) {
    return api.post('/admin', data)
  },

  // 更新管理员信息
  updateAdmin(id, data) {
    return api.put(`/admin/${id}`, data)
  },

  // 删除管理员
  deleteAdmin(id) {
    return api.delete(`/admin/${id}`)
  },

  // 批量删除管理员
  batchDeleteAdmins(adminIds) {
    return api.delete('/admin/batch', { data: { adminIds } })
  },

  // 更新管理员状态
  updateAdminStatus(id, status) {
    return api.put(`/admin/${id}/status`, { status })
  },

  // 重置管理员密码
  resetAdminPassword(id) {
    return api.post(`/admin/${id}/reset-password`)
  },

  // 导出管理员数据
  exportAdmins(params = {}) {
    return api.get('/admin/export', { 
      params,
      responseType: 'blob'
    })
  },

  // ==================== 角色权限管理 ====================
  
  // 获取角色列表
  getRoles() {
    return api.get('/admin/roles')
  },

  // 获取权限列表
  getPermissions() {
    return api.get('/admin/permissions')
  },

  // 更新管理员角色
  updateAdminRole(id, roleData) {
    return api.put(`/admin/${id}/role`, roleData)
  },

  // 获取管理员权限
  getAdminPermissions(id) {
    return api.get(`/admin/${id}/permissions`)
  },

  // ==================== 系统管理 ====================
  
  // 获取系统统计信息
  getSystemStats() {
    return api.get('/admin/stats')
  },

  // 获取操作日志
  getOperationLogs(params = {}) {
    return api.get('/admin/logs', { params })
  },

  // 清理系统缓存
  clearSystemCache() {
    return api.post('/admin/cache/clear')
  },

  // 获取系统配置
  getSystemConfig() {
    return api.get('/admin/config')
  },

  // 更新系统配置
  updateSystemConfig(config) {
    return api.put('/admin/config', config)
  },

  // ==================== 数据备份与恢复 ====================
  
  // 创建数据备份
  createBackup() {
    return api.post('/admin/backup')
  },

  // 获取备份列表
  getBackups(params = {}) {
    return api.get('/admin/backup', { params })
  },

  // 下载备份文件
  downloadBackup(backupId) {
    return api.get(`/admin/backup/${backupId}/download`, {
      responseType: 'blob'
    })
  },

  // 恢复数据
  restoreBackup(backupId) {
    return api.post(`/admin/backup/${backupId}/restore`)
  },

  // 删除备份
  deleteBackup(backupId) {
    return api.delete(`/admin/backup/${backupId}`)
  },

  // ==================== 工具方法 ====================
  
  // 获取管理员角色选项
  getRoleOptions() {
    return [
      { value: 'SUPER_ADMIN', label: '超级管理员', color: 'danger' },
      { value: 'ADMIN', label: '管理员', color: 'warning' },
      { value: 'EDITOR', label: '编辑员', color: 'info' },
      { value: 'VIEWER', label: '查看员', color: 'success' }
    ]
  },

  // 获取管理员状态选项
  getStatusOptions() {
    return [
      { value: 1, label: '启用', color: 'success' },
      { value: 0, label: '禁用', color: 'danger' }
    ]
  },

  // 验证管理员权限
  hasPermission(permission) {
    // 这里可以根据当前登录用户的权限进行验证
    // 实际实现可能需要从store或其他地方获取当前用户权限
    return true // 临时返回true，实际应该进行权限验证
  },

  // 格式化角色显示
  formatRole(role) {
    const roleMap = {
      'SUPER_ADMIN': '超级管理员',
      'ADMIN': '管理员',
      'EDITOR': '编辑员',
      'VIEWER': '查看员'
    }
    return roleMap[role] || role
  },

  // 格式化状态显示
  formatStatus(status) {
    return status === 1 ? '启用' : '禁用'
  }
}

export default adminApi

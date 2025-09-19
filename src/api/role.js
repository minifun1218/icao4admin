import api from './index'

// 角色权限管理相关API (根据API文档更新)
export const roleApi = {
  // ==================== 基础CRUD操作 ====================
  
  // 获取所有角色 (需要ADMIN权限)
  getRoles(params = {}) {
    return api.get('/roles', { params })
  },

  // 根据ID获取角色 (需要ADMIN权限)
  getRoleById(id) {
    return api.get(`/roles/${id}`)
  },

  // 创建角色 (需要SUPER_ADMIN权限)
  createRole(data) {
    return api.post('/roles', data)
  },

  // 更新角色 (需要SUPER_ADMIN权限)
  updateRole(id, data) {
    return api.put(`/roles/${id}`, data)
  },

  // 删除角色 (需要SUPER_ADMIN权限)
  deleteRole(id) {
    return api.delete(`/roles/${id}`)
  },

  // 更新角色状态 (需要SUPER_ADMIN权限)
  updateRoleStatus(id, status) {
    return api.put(`/roles/${id}/status`, null, { params: { status } })
  },

  // 获取预定义权限
  getPredefinedPermissions() {
    return [
      { id: 'USER_READ', name: '用户查看', category: 'USER' },
      { id: 'USER_WRITE', name: '用户管理', category: 'USER' },
      { id: 'USER_DELETE', name: '用户删除', category: 'USER' },
      { id: 'VOCAB_READ', name: '词汇查看', category: 'CONTENT' },
      { id: 'VOCAB_WRITE', name: '词汇管理', category: 'CONTENT' },
      { id: 'VOCAB_DELETE', name: '词汇删除', category: 'CONTENT' },
      { id: 'TERM_READ', name: '术语查看', category: 'CONTENT' },
      { id: 'TERM_WRITE', name: '术语管理', category: 'CONTENT' },
      { id: 'TERM_DELETE', name: '术语删除', category: 'CONTENT' },
      { id: 'EXERCISE_READ', name: '练习查看', category: 'EXERCISE' },
      { id: 'EXERCISE_WRITE', name: '练习管理', category: 'EXERCISE' },
      { id: 'EXERCISE_DELETE', name: '练习删除', category: 'EXERCISE' },
      { id: 'MEDIA_READ', name: '媒体查看', category: 'MEDIA' },
      { id: 'MEDIA_WRITE', name: '媒体管理', category: 'MEDIA' },
      { id: 'MEDIA_DELETE', name: '媒体删除', category: 'MEDIA' },
      { id: 'ROLE_READ', name: '角色查看', category: 'SYSTEM' },
      { id: 'ROLE_WRITE', name: '角色管理', category: 'SYSTEM' },
      { id: 'ROLE_DELETE', name: '角色删除', category: 'SYSTEM' },
      { id: 'SYSTEM_CONFIG', name: '系统配置', category: 'SYSTEM' },
      { id: 'SYSTEM_LOG', name: '系统日志', category: 'SYSTEM' }
    ]
  },

  // 获取权限分类
  getPermissionCategories() {
    return [
      { id: 'USER', name: '用户管理' },
      { id: 'CONTENT', name: '内容管理' },
      { id: 'EXERCISE', name: '练习管理' },
      { id: 'MEDIA', name: '媒体管理' },
      { id: 'SYSTEM', name: '系统管理' }
    ]
  }
}

export default roleApi

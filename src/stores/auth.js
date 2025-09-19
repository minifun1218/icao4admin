import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'
import { AuthUtils } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  // 从本地存储初始化状态
  const token = ref(AuthUtils.getToken() || '')
  const refreshToken = ref(AuthUtils.getRefreshToken() || '')
  const adminUserInfo = ref(AuthUtils.getUserInfo())

  // 计算属性
  const isAuthenticated = computed(() => {
    return !!token.value && !AuthUtils.isTokenExpired(token.value)
  })
  
  const userRoles = computed(() => adminUserInfo.value?.roles || [])
  const userName = computed(() => adminUserInfo.value?.username || '')
  const userEmail = computed(() => adminUserInfo.value?.email || '')
  const userAvatar = computed(() => adminUserInfo.value?.avatar || '')
  
  // 检查是否有指定角色
  const hasRole = (role) => userRoles.value.includes(role)
  
  // 检查是否有任意一个角色
  const hasAnyRole = (roles) => roles.some(role => userRoles.value.includes(role))
  
  // 检查是否有所有角色
  const hasAllRoles = (roles) => roles.every(role => userRoles.value.includes(role))

  // 登录
  const login = async (credentials) => {
    try {
      // 清除旧的认证信息
      logout()
      
      const response = await api.post('/admin/login', credentials, {
        showLoading: false // 登录页面有自己的loading
      })
      
      // 更新状态
      token.value = response.accessToken
      refreshToken.value = response.refreshToken
      adminUserInfo.value = response.adminUserInfo
      
      // 保存到本地存储
      AuthUtils.setToken(token.value)
      AuthUtils.setRefreshToken(refreshToken.value)
      AuthUtils.setUserInfo(adminUserInfo.value)
      
      console.log('✅ 登录成功:', {
        username: adminUserInfo.value?.username,
        roles: adminUserInfo.value?.roles,
        tokenExpiration: AuthUtils.getTokenExpiration(token.value)
      })
      
      return response
    } catch (error) {
      console.error('❌ 登录失败:', error)
      throw error
    }
  }

  // 登出
  const logout = () => {
    console.log('🚪 用户退出登录')
    
    // 清除状态
    token.value = ''
    refreshToken.value = ''
    adminUserInfo.value = null
    
    // 清除本地存储
    AuthUtils.clearAuth()
  }

  // 更新token
  const updateToken = (newToken, newRefreshToken) => {
    token.value = newToken
    if (newRefreshToken) {
      refreshToken.value = newRefreshToken
    }
    
    AuthUtils.setToken(newToken)
    if (newRefreshToken) {
      AuthUtils.setRefreshToken(newRefreshToken)
    }
    
    console.log('🔄 Token已更新:', {
      tokenExpiration: AuthUtils.getTokenExpiration(newToken)
    })
  }

  // 更新用户信息
  const updateUserInfo = (userInfo) => {
    adminUserInfo.value = { ...adminUserInfo.value, ...userInfo }
    AuthUtils.setUserInfo(adminUserInfo.value)
    
    console.log('👤 用户信息已更新:', userInfo)
  }

  // 注册
  const register = async (registerData) => {
    try {
      const response = await api.post('/admin/register', registerData, {
        showLoading: false // 注册页面有自己的loading
      })
      
      console.log('✅ 注册成功:', {
        username: registerData.username,
        email: registerData.email
      })
      
      return response
    } catch (error) {
      console.error('❌ 注册失败:', error)
      throw error
    }
  }

  // 修改密码
  const changePassword = async (passwordData) => {
    try {
      await api.put('/admin/password', passwordData)
      console.log('🔐 密码修改成功')
    } catch (error) {
      console.error('❌ 密码修改失败:', error)
      throw error
    }
  }

  // 检查token有效性
  const checkTokenValidity = () => {
    if (!token.value) {
      return false
    }
    
    if (AuthUtils.isTokenExpired(token.value)) {
      console.warn('⚠️ Token已过期')
      logout()
      return false
    }
    
    return true
  }

  // 获取用户权限信息
  const getPermissions = () => {
    return {
      roles: userRoles.value,
      isAdmin: hasRole('ADMIN'),
      isSuperAdmin: hasRole('SUPER_ADMIN'),
      isTeacher: hasRole('TEACHER'),
      canManageUsers: hasAnyRole(['ADMIN', 'SUPER_ADMIN']),
      canManageContent: hasAnyRole(['ADMIN', 'SUPER_ADMIN', 'TEACHER'])
    }
  }

  // 初始化时检查token有效性
  if (token.value && AuthUtils.isTokenExpired(token.value)) {
    console.warn('⚠️ 本地token已过期，清除认证信息')
    logout()
  }

  return {
    // 状态
    token,
    refreshToken,
    adminUserInfo,
    
    // 计算属性
    isAuthenticated,
    userRoles,
    userName,
    userEmail,
    userAvatar,
    
    // 方法
    hasRole,
    hasAnyRole,
    hasAllRoles,
    login,
    register,
    logout,
    updateToken,
    updateUserInfo,
    changePassword,
    checkTokenValidity,
    getPermissions
  }
})

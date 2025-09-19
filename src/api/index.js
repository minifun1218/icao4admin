import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// 请求队列，用于管理并发请求
let requestQueue = []
let isRefreshing = false
let loadingInstance = null
let requestCount = 0

// 创建 axios 实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000, // 增加超时时间
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 显示全局加载
const showLoading = () => {
  if (requestCount === 0) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '请求中...',
      background: 'rgba(0, 0, 0, 0.7)',
      customClass: 'global-loading'
    })
  }
  requestCount++
}

// 隐藏全局加载
const hideLoading = () => {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }
}

// Token刷新函数
const refreshToken = async () => {
  const authStore = useAuthStore()
  if (!authStore.refreshToken) {
    throw new Error('No refresh token available')
  }
  
  try {
    const response = await axios.post('http://localhost:8080/api/admin/refresh', {
      refreshToken: authStore.refreshToken
    })
    
    const { accessToken, refreshToken: newRefreshToken } = response.data
    authStore.token = accessToken
    if (newRefreshToken) {
      authStore.refreshToken = newRefreshToken
    }
    
    // 更新本地存储
    localStorage.setItem('token', accessToken)
    if (newRefreshToken) {
      localStorage.setItem('refreshToken', newRefreshToken)
    }
    
    return accessToken
  } catch (error) {
    authStore.logout()
    router.push('/login')
    throw error
  }
}

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 显示加载动画（可选，根据需要开启）
    if (config.showLoading !== false) {
      showLoading()
    }
    
    // 添加请求时间戳，防止缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    // 设置认证token
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    // 设置请求ID，便于调试
    config.headers['X-Request-ID'] = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    
    // 添加客户端信息（使用自定义头部，避免User-Agent限制）
    config.headers['X-Client-Info'] = `ICAO4-Admin/1.0.0`
    config.headers['X-Client-Platform'] = 'Web'
    
    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`, {
      headers: config.headers,
      params: config.params,
      data: config.data
    })
    
    return config
  },
  error => {
    hideLoading()
    console.error('❌ Request Error:', error)
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    hideLoading()
    
    console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url}`, {
      status: response.status,
      data: response.data
    })
    
    // 处理不同的响应格式
    const responseData = response.data
    
    // 如果响应数据包含code字段，说明是标准格式
    if (responseData && typeof responseData.code !== 'undefined') {
      const { code, message, data } = responseData
      
      // 成功响应
      if (code === 200 || code === 201) {
        // 对于公钥请求，返回完整的响应数据以保留code、message、data结构
        if (response.config.url?.includes('/security/public-key')) {
          return responseData
        }
        // 其他请求返回data部分
        return data || responseData
      }
      
      // 业务错误
      if (code && code !== 200 && code !== 201) {
        const errorMsg = message || '请求失败'
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      }
    }
    
    // HTTP状态码成功的响应
    if (response.status === 200 || response.status === 201) {
      return responseData
    }
    
    // 默认返回数据
    return responseData
  },
  async error => {
    hideLoading()
    
    const { config, response } = error
    console.error(`❌ API Error: ${config?.method?.toUpperCase()} ${config?.url}`, {
      status: response?.status,
      data: response?.data,
      error: error.message
    })
    
    // 网络错误
    if (!response) {
      ElMessage.error('网络连接失败，请检查网络设置')
      return Promise.reject(error)
    }
    
    const { status, data } = response
    
    // Token过期，尝试刷新
    if (status === 401) {
      const authStore = useAuthStore()
      
      // 如果是刷新token的请求失败，直接登出
      if (config.url?.includes('/admin/refresh')) {
        ElMessage.error('登录已过期，请重新登录')
        authStore.logout()
        router.push('/login')
        return Promise.reject(error)
      }
      
      // 如果已经在刷新token，将请求加入队列
      if (isRefreshing) {
        return new Promise((resolve) => {
          requestQueue.push(() => {
            config.headers.Authorization = `Bearer ${authStore.token}`
            resolve(api.request(config))
          })
        })
      }
      
      // 开始刷新token
      if (authStore.refreshToken && !isRefreshing) {
        isRefreshing = true
        
        try {
          await refreshToken()
          
          // 重试原请求
          config.headers.Authorization = `Bearer ${authStore.token}`
          
          // 执行队列中的请求
          requestQueue.forEach(callback => callback())
          requestQueue = []
          
          return api.request(config)
        } catch (refreshError) {
          ElMessage.error('登录已过期，请重新登录')
          authStore.logout()
          router.push('/login')
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      } else {
        ElMessage.error('登录已过期，请重新登录')
        authStore.logout()
        router.push('/login')
      }
    }
    
    // 其他HTTP错误
    const errorMessages = {
      400: '请求参数错误',
      403: '权限不足，无法访问',
      404: '请求的资源不存在',
      405: '请求方法不允许',
      408: '请求超时',
      409: '资源冲突',
      422: '数据验证失败',
      429: '请求过于频繁，请稍后再试',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务暂不可用',
      504: '网关超时'
    }
    
    const errorMsg = errorMessages[status] || data?.message || `请求失败 (${status})`
    ElMessage.error(errorMsg)
    
    return Promise.reject(error)
  }
)

// 导出API实例和工具函数
export default api

// 导出工具函数
export { RequestUtils, withAuth, withUpload, withDownload } from '@/utils/request'
export { AuthUtils } from '@/utils/auth'

// 创建带认证的API方法快捷方式
export const authApi = {
  get: (url, params, config = {}) => api.get(url, { ...config, params }),
  post: (url, data, config = {}) => api.post(url, data, config),
  put: (url, data, config = {}) => api.put(url, data, config),
  patch: (url, data, config = {}) => api.patch(url, data, config),
  delete: (url, config = {}) => api.delete(url, config)
}

// 文件上传API
export const uploadApi = {
  upload: (url, formData, config = {}) => api.post(url, formData, {
    ...config,
    headers: {
      'Content-Type': 'multipart/form-data',
      ...config.headers
    }
  })
}

// 文件下载API
export const downloadApi = {
  download: (url, params = {}, config = {}) => api.get(url, {
    ...config,
    params,
    responseType: 'blob'
  })
}

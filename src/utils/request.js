import { AuthUtils } from './auth'

// 请求配置工具类
export class RequestUtils {
  // 获取默认请求头
  static getDefaultHeaders() {
    return {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    }
  }
  
  // 获取认证请求头
  static getAuthHeaders() {
    const token = AuthUtils.getToken()
    const headers = this.getDefaultHeaders()
    
    if (token) {
      headers.Authorization = AuthUtils.formatAuthHeader(token)
    }
    
    return headers
  }
  
  // 获取文件上传请求头
  static getUploadHeaders() {
    const token = AuthUtils.getToken()
    const headers = {
      'Accept': 'application/json'
      // 不设置 Content-Type，让浏览器自动设置 multipart/form-data
    }
    
    if (token) {
      headers.Authorization = AuthUtils.formatAuthHeader(token)
    }
    
    return headers
  }
  
  // 获取下载请求头
  static getDownloadHeaders() {
    const token = AuthUtils.getToken()
    const headers = {
      'Accept': 'application/octet-stream, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    }
    
    if (token) {
      headers.Authorization = AuthUtils.formatAuthHeader(token)
    }
    
    return headers
  }
  
  // 创建请求配置
  static createConfig(options = {}) {
    const config = {
      timeout: 30000,
      ...options
    }
    
    // 根据请求类型设置不同的头部
    if (options.upload) {
      config.headers = { ...this.getUploadHeaders(), ...options.headers }
    } else if (options.download) {
      config.headers = { ...this.getDownloadHeaders(), ...options.headers }
      config.responseType = 'blob'
    } else {
      config.headers = { ...this.getAuthHeaders(), ...options.headers }
    }
    
    return config
  }
  
  // 创建分页参数
  static createPaginationParams(page = 0, size = 20, sort = 'createdAt,desc') {
    return {
      page,
      size,
      sort
    }
  }
  
  // 创建搜索参数
  static createSearchParams(searchForm = {}, pagination = {}) {
    const params = {
      ...this.createPaginationParams(
        pagination.page - 1 || 0,
        pagination.size || 20,
        pagination.sort
      )
    }
    
    // 过滤空值参数
    Object.keys(searchForm).forEach(key => {
      const value = searchForm[key]
      if (value !== null && value !== undefined && value !== '') {
        params[key] = value
      }
    })
    
    return params
  }
  
  // 处理API响应
  static handleResponse(response) {
    const { data, status } = response
    
    // 直接返回blob数据（用于文件下载）
    if (response.config.responseType === 'blob') {
      return data
    }
    
    // 处理标准API响应
    if (data && typeof data === 'object') {
      const { code, message, data: responseData } = data
      
      if (code === 200 || code === 201 || status === 200 || status === 201) {
        return responseData || data
      }
      
      throw new Error(message || '请求失败')
    }
    
    return data
  }
  
  // 处理API错误
  static handleError(error) {
    if (error.response) {
      const { status, data } = error.response
      const message = data?.message || `请求失败 (${status})`
      
      const apiError = new Error(message)
      apiError.status = status
      apiError.code = data?.code
      apiError.details = data?.details
      
      throw apiError
    }
    
    throw new Error(error.message || '网络连接失败')
  }
}

// 请求装饰器
export const withAuth = (config = {}) => {
  return RequestUtils.createConfig(config)
}

export const withUpload = (config = {}) => {
  return RequestUtils.createConfig({ ...config, upload: true })
}

export const withDownload = (config = {}) => {
  return RequestUtils.createConfig({ ...config, download: true })
}

// 常用请求方法封装
export const createApiMethod = (method, url, options = {}) => {
  return async (data, config = {}) => {
    const finalConfig = RequestUtils.createConfig({ ...options, ...config })
    
    try {
      let response
      if (method.toLowerCase() === 'get') {
        response = await api[method](url, { ...finalConfig, params: data })
      } else {
        response = await api[method](url, data, finalConfig)
      }
      
      return RequestUtils.handleResponse(response)
    } catch (error) {
      RequestUtils.handleError(error)
    }
  }
}

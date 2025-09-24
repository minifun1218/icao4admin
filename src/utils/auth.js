// 认证工具类
export class AuthUtils {
  static TOKEN_KEY = 'token'
  static REFRESH_TOKEN_KEY = 'refreshToken'
  static USER_INFO_KEY = 'adminUserInfo'
  
  // 获取token
  static getToken() {
    return localStorage.getItem(this.TOKEN_KEY)
  }
  
  // 设置token
  static setToken(token) {
    if (token) {
      localStorage.setItem(this.TOKEN_KEY, token)
    } else {
      localStorage.removeItem(this.TOKEN_KEY)
    }
  }
  
  // 获取刷新token
  static getRefreshToken() {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY)
  }
  
  // 设置刷新token
  static setRefreshToken(refreshToken) {
    if (refreshToken) {
      localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken)
    } else {
      localStorage.removeItem(this.REFRESH_TOKEN_KEY)
    }
  }
  
  // 获取用户信息
  static getUserInfo() {
    const userInfo = localStorage.getItem(this.USER_INFO_KEY)
    return userInfo ? JSON.parse(userInfo) : null
  }
  
  // 设置用户信息
  static setUserInfo(userInfo) {
    if (userInfo) {
      localStorage.setItem(this.USER_INFO_KEY, JSON.stringify(userInfo))
    } else {
      localStorage.removeItem(this.USER_INFO_KEY)
    }
  }
  
  // 清除所有认证信息
  static clearAuth() {
    localStorage.removeItem(this.TOKEN_KEY)
    localStorage.removeItem(this.REFRESH_TOKEN_KEY)
    localStorage.removeItem(this.USER_INFO_KEY)
  }
  
  // 清除token（兼容性方法）
  static clearToken() {
    this.clearAuth()
  }
  
  // 检查token是否过期
  static isTokenExpired(token) {
    if (!token) return true
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      const currentTime = Date.now() / 1000
      return payload.exp < currentTime
    } catch (error) {
      return true
    }
  }
  
  // 检查是否已登录
  static isAuthenticated() {
    const token = this.getToken()
    return token && !this.isTokenExpired(token)
  }
  
  // 获取token过期时间
  static getTokenExpiration(token) {
    if (!token) return null
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return new Date(payload.exp * 1000)
    } catch (error) {
      return null
    }
  }
  
  // 格式化Authorization header
  static formatAuthHeader(token) {
    return token ? `Bearer ${token}` : ''
  }
}

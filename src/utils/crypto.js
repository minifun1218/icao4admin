import CryptoJS from 'crypto-js'
import { JSEncrypt } from 'jsencrypt'

// 加密密钥 (生产环境应该从环境变量获取)
const SECRET_KEY = 'ICAO4-ADMIN-SECRET-KEY-2024'

// RSA加密实例
let rsaEncrypt = null

/**
 * 密码加密工具类
 */
export class CryptoUtils {
  /**
   * RSA公钥存储
   * 实际应该从后端动态获取
   */
  static RSA_PUBLIC_KEY = null
  static RSA_KEY_ID = null
  static RSA_KEY_EXPIRES_IN = null

  /**
   * 使用AES加密密码
   * @param {string} password - 原始密码
   * @param {string} timestamp - 时间戳（可选，增加安全性）
   * @returns {string} 加密后的密码
   */
  static encryptPassword(password, timestamp = null) {
    try {
      const ts = timestamp || Date.now().toString()
      const data = `${password}|${ts}`
      
      // 使用AES加密
      const encrypted = CryptoJS.AES.encrypt(data, SECRET_KEY).toString()
      return encrypted
    } catch (error) {
      console.error('密码加密失败:', error)
      throw new Error('密码加密失败')
    }
  }

  /**
   * 使用SHA-256对密码进行哈希
   * @param {string} password - 原始密码
   * @param {string} salt - 盐值
   * @returns {string} 哈希后的密码
   */
  static hashPassword(password, salt = 'ICAO4_SALT') {
    try {
      const hash = CryptoJS.SHA256(password + salt).toString()
      return hash
    } catch (error) {
      console.error('密码哈希失败:', error)
      throw new Error('密码哈希失败')
    }
  }

  /**
   * 生成客户端签名（用于验证请求完整性）
   * @param {string} username - 用户名
   * @param {string} encryptedPassword - 加密后的密码
   * @param {string} timestamp - 时间戳
   * @returns {string} 签名
   */
  static generateSignature(username, encryptedPassword, timestamp) {
    try {
      const data = `${username}${encryptedPassword}${timestamp}`
      const signature = CryptoJS.HmacSHA256(data, SECRET_KEY).toString()
      return signature
    } catch (error) {
      console.error('签名生成失败:', error)
      throw new Error('签名生成失败')
    }
  }

  /**
   * 生成随机盐值
   * @param {number} length - 盐值长度
   * @returns {string} 随机盐值
   */
  static generateSalt(length = 32) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let salt = ''
    for (let i = 0; i < length; i++) {
      salt += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return salt
  }

  /**
   * 生成安全的登录请求数据
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @param {boolean} includeSaltAndSignature - 是否包含salt和signature（默认false，由后端处理）
   * @returns {object} 加密后的登录数据
   */
  static generateSecureLoginData(username, password, includeSaltAndSignature = false) {
    try {
      const timestamp = Date.now().toString()
      
      // 加密密码
      const encryptedPassword = this.encryptPassword(password, timestamp)
      
      const loginData = {
        username,
        password: encryptedPassword, // 传输加密后的密码
        timestamp,
        encryptionType: 'AES-256'
      }
      
      // 如果需要包含salt和signature（不推荐，降低安全性）
      if (includeSaltAndSignature) {
        const salt = this.generateSalt(32)
        const signature = this.generateSignature(username, encryptedPassword, timestamp)
        
        loginData.salt = salt
        loginData.signature = signature
        
        console.warn('⚠️ 警告：在前端生成salt和signature会降低安全性')
      }
      
      return loginData
    } catch (error) {
      console.error('生成安全登录数据失败:', error)
      throw new Error('登录数据加密失败')
    }
  }

  /**
   * 用于开发和调试的解密方法（仅客户端使用）
   * @param {string} encryptedPassword - 加密的密码
   * @returns {string} 解密后的密码
   */
  static decryptPassword(encryptedPassword) {
    try {
      const decrypted = CryptoJS.AES.decrypt(encryptedPassword, SECRET_KEY)
      const decryptedText = decrypted.toString(CryptoJS.enc.Utf8)
      return decryptedText.split('|')[0] // 移除时间戳
    } catch (error) {
      console.error('密码解密失败:', error)
      return null
    }
  }

  /**
   * 后端解密完整数据的示例方法（供参考）
   * @param {string} encryptedPassword - 加密的密码
   * @returns {object} 解密后的完整数据
   */
  static decryptPasswordWithTimestamp(encryptedPassword) {
    try {
      const decrypted = CryptoJS.AES.decrypt(encryptedPassword, SECRET_KEY)
      const decryptedText = decrypted.toString(CryptoJS.enc.Utf8)
      const [password, timestamp] = decryptedText.split('|')
      
      return {
        password,
        timestamp: parseInt(timestamp),
        isValid: Date.now() - parseInt(timestamp) < 300000 // 5分钟有效期
      }
    } catch (error) {
      console.error('密码解密失败:', error)
      return null
    }
  }

  /**
   * 验证密码强度
   * @param {string} password - 密码
   * @returns {object} 密码强度信息
   */
  static validatePasswordStrength(password) {
    const result = {
      isValid: false,
      score: 0,
      feedback: []
    }

    if (password.length < 6) {
      result.feedback.push('密码长度至少6位')
      return result
    }

    if (password.length >= 8) result.score += 20
    if (/[a-z]/.test(password)) result.score += 20
    if (/[A-Z]/.test(password)) result.score += 20
    if (/\d/.test(password)) result.score += 20
    if (/[^a-zA-Z\d]/.test(password)) result.score += 20

    if (result.score >= 80) {
      result.isValid = true
      result.feedback.push('密码强度: 强')
    } else if (result.score >= 60) {
      result.isValid = true
      result.feedback.push('密码强度: 中等')
    } else if (result.score >= 40) {
      result.isValid = true
      result.feedback.push('密码强度: 弱')
    } else {
      result.feedback.push('密码强度过弱，请包含大小写字母、数字和特殊字符')
    }

    return result
  }

  // ==================== RSA+AES 混合加密方案 ====================

  /**
   * 从后端获取RSA公钥
   * @returns {Promise<string>} RSA公钥
   */
  static async fetchRSAPublicKey() {
    try {
      // 动态导入API以避免循环依赖
      const { userApi } = await import('@/api/user')
      
      console.log('🔑 正在从后端获取RSA公钥: http://127.0.0.1:8080/api/security/public-key')
      const response = await userApi.getRSAPublicKey()
      
        // 处理新的响应格式: {"code":200,"message":"success","data":{"keyId":"...","publicKey":"...","expiresIn":1800}}
        if (response && response.code === 200 && response.data) {
          const keyId = response.data.keyId;
          const publicKey = response.data.publicKey;
          const expiresIn = response.data.expiresIn;
          
          console.log('🔍 解析后端响应数据:')
          console.log('  - KeyID:', keyId)
          console.log('  - PublicKey长度:', publicKey?.length)
          console.log('  - ExpiresIn:', expiresIn)
          
          // 验证必要字段
          if (!keyId || !publicKey) {
            throw new Error('后端返回的数据缺少必要字段: keyId=' + keyId + ', publicKey=' + (publicKey ? '存在' : '缺失'))
          }
          
          // 格式化公钥为PEM格式
          const formattedPublicKey = this.formatPublicKeyToPEM(publicKey)
        
        // 存储公钥信息
        this.RSA_PUBLIC_KEY = formattedPublicKey
        this.RSA_KEY_ID = keyId
        this.RSA_KEY_EXPIRES_IN = expiresIn
        
        console.log('✅ RSA公钥获取成功:')
        console.log('  - KeyID:', keyId)
        console.log('  - 公钥长度:', formattedPublicKey.length, '字符')
        console.log('  - 过期时间:', expiresIn, '秒')
        
        return formattedPublicKey
      }
      // 兼容旧格式
      else if (response && response.publicKey) {
        this.RSA_PUBLIC_KEY = response.publicKey
        console.log('✅ RSA公钥获取成功（旧格式），长度:', response.publicKey.length)
        return response.publicKey
      } else if (response && response.data && response.data.publicKey) {
        // 处理可能的嵌套响应结构
        this.RSA_PUBLIC_KEY = response.data.publicKey
        console.log('✅ RSA公钥获取成功（嵌套结构），长度:', response.data.publicKey.length)
        return response.data.publicKey
      } else {
        throw new Error('后端返回的公钥格式不正确: ' + JSON.stringify(response))
      }
    } catch (error) {
      console.warn('⚠️ 从后端获取RSA公钥失败，使用模拟公钥:', error.message)
      
      // 回退到模拟公钥（开发环境）
      const simulatedPublicKey = `-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7QWzQ8VgG5J9h8Ry
7KjGCYvJ4nC2dK3mH8pL9oN1qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3
oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4
cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4
qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6
eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5
sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7
gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7
uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9
iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8
wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0
kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0
yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1
mN3oP4qR5sT7uV8wX0yZ2aB4cD6eF7gH9iJ0kL1mN3oP4qR5sT7uV8wIDAQAB
-----END PUBLIC KEY-----`
      
      this.RSA_PUBLIC_KEY = simulatedPublicKey
      this.RSA_KEY_ID = 'simulated_key_id'
      console.log('🔑 使用模拟RSA公钥（开发环境）')
      return simulatedPublicKey
    }
  }

  /**
   * 将Base64格式的公钥转换为PEM格式
   * @param {string} publicKeyBase64 - Base64格式的公钥
   * @returns {string} PEM格式的公钥
   */
  static formatPublicKeyToPEM(publicKeyBase64) {
    try {
      // 如果已经是PEM格式，直接返回
      if (publicKeyBase64.includes('BEGIN PUBLIC KEY')) {
        return publicKeyBase64
      }
      
      // 将Base64字符串按每64字符换行
      const formattedKey = publicKeyBase64.match(/.{1,64}/g).join('\n')
      
      // 添加PEM头部和尾部
      return `-----BEGIN PUBLIC KEY-----\n${formattedKey}\n-----END PUBLIC KEY-----`
    } catch (error) {
      console.error('❌ 格式化公钥失败:', error)
      // 如果格式化失败，返回原始字符串
      return publicKeyBase64
    }
  }

  /**
   * 生成随机AES密钥
   * @param {number} length - 密钥长度（字节）
   * @returns {string} AES密钥
   */
  static generateAESKey(length = 32) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let key = ''
    for (let i = 0; i < length; i++) {
      key += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return key
  }

  /**
   * 检查RSA密钥是否过期
   * @returns {boolean} 是否过期
   */
  static isRSAKeyExpired() {
    if (!this.RSA_KEY_EXPIRES_IN || !this.RSA_KEY_ID) {
      return true // 如果没有过期时间信息，认为已过期
    }
    
    // 这里简化处理，实际应该基于获取时间计算
    // 为了简单起见，暂时返回false，实际项目中应该记录获取时间
    return false
  }

  /**
   * 获取RSA密钥信息
   * @returns {object} 密钥信息
   */
  static getRSAKeyInfo() {
    return {
      keyId: this.RSA_KEY_ID,
      hasPublicKey: !!this.RSA_PUBLIC_KEY,
      expiresIn: this.RSA_KEY_EXPIRES_IN,
      isExpired: this.isRSAKeyExpired()
    }
  }

  /**
   * 调试方法：打印当前密钥状态
   */
  static debugKeyStatus() {
    console.log('🔍 当前RSA密钥状态:')
    console.log('  - RSA_KEY_ID:', this.RSA_KEY_ID)
    console.log('  - RSA_PUBLIC_KEY存在:', !!this.RSA_PUBLIC_KEY)
    console.log('  - RSA_KEY_EXPIRES_IN:', this.RSA_KEY_EXPIRES_IN)
    console.log('  - 是否过期:', this.isRSAKeyExpired())
    
    if (this.RSA_PUBLIC_KEY) {
      console.log('  - 公钥长度:', this.RSA_PUBLIC_KEY.length)
      console.log('  - 公钥格式:', this.RSA_PUBLIC_KEY.includes('BEGIN PUBLIC KEY') ? 'PEM' : 'Base64')
    }
  }

  /**
   * 使用RSA公钥加密AES密钥
   * @param {string} aesKey - AES密钥
   * @param {string} publicKey - RSA公钥
   * @returns {string} 加密后的AES密钥
   */
  static encryptAESKeyWithRSA(aesKey, publicKey) {
    try {
      if (!rsaEncrypt) {
        rsaEncrypt = new JSEncrypt()
      }
      
      rsaEncrypt.setPublicKey(publicKey)
      const encryptedAESKey = rsaEncrypt.encrypt(aesKey)
      
      if (!encryptedAESKey) {
        throw new Error('RSA加密失败')
      }
      
      return encryptedAESKey
    } catch (error) {
      console.error('❌ RSA加密AES密钥失败:', error)
      throw new Error('RSA加密失败')
    }
  }

  /**
   * 使用AES密钥加密密码
   * @param {string} password - 原始密码
   * @param {string} aesKey - AES密钥
   * @param {string} timestamp - 时间戳
   * @returns {string} 加密后的密码
   */
  static encryptPasswordWithAES(password, aesKey, timestamp) {
    try {
      const data = `${password}|${timestamp}`
      const encrypted = CryptoJS.AES.encrypt(data, aesKey).toString()
      return encrypted
    } catch (error) {
      console.error('❌ AES加密密码失败:', error)
      throw new Error('AES加密失败')
    }
  }

  /**
   * RSA+AES混合加密登录数据（推荐方案）
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @returns {Promise<object>} 混合加密后的登录数据
   */
  static async generateHybridEncryptedLoginData(username, password) {
    try {
      console.log('🔐 开始RSA+AES混合加密流程...')
      
      // 调试：打印当前密钥状态
      this.debugKeyStatus()
      
      // 步骤1: 先请求后端获取RSA公钥
      console.log('📋 步骤1: 从后端获取RSA公钥')
      let publicKey = this.RSA_PUBLIC_KEY
      
      // 检查是否需要重新获取公钥
      if (!publicKey || this.isRSAKeyExpired()) {
        if (!publicKey) {
          console.log('🌐 首次获取RSA公钥: http://127.0.0.1:8080/api/security/public-key')
        } else {
          console.log('🔄 RSA密钥已过期，重新获取: http://127.0.0.1:8080/api/security/public-key')
        }
        publicKey = await this.fetchRSAPublicKey()
      } else {
        console.log('✅ 使用已缓存的RSA公钥 (KeyID: ' + this.RSA_KEY_ID + ')')
      }
      
      // 步骤2: 在本地生成一次性AES密钥（256位随机字符串）
      console.log('📋 步骤2: 生成一次性AES-256密钥')
      const aesKey = this.generateAESKey(32) // 32字节 = 256位
      console.log('🔑 生成256位AES密钥:', aesKey.substring(0, 12) + '...' + aesKey.substring(aesKey.length - 4))
      console.log('🔑 AES密钥长度:', aesKey.length, '字符 (', aesKey.length * 8, '位)')
      
      // 步骤3: 使用RSA公钥加密这个一次性AES密钥
      console.log('📋 步骤3: 使用RSA公钥加密AES密钥')
      const encryptedAESKey = this.encryptAESKeyWithRSA(aesKey, publicKey)
      console.log('🔒 RSA加密AES密钥完成，密文长度:', encryptedAESKey.length)
      
      // 步骤4: 使用这个一次性AES密钥对用户明文密码进行AES-256加密
      console.log('📋 步骤4: 使用AES密钥加密用户密码')
      const timestamp = Date.now().toString()
      const encryptedPassword = this.encryptPasswordWithAES(password, aesKey, timestamp)
      console.log('🔐 AES-256加密密码完成，密文长度:', encryptedPassword.length)
      
      // 步骤5: 将两个密文和其他登录信息组合发送给后端
      console.log('📋 步骤5: 组合加密数据准备发送')
      
      // 验证keyId是否存在
      if (!this.RSA_KEY_ID) {
        console.error('❌ RSA密钥ID为空，无法完成混合加密')
        throw new Error('RSA密钥ID为空，请重新获取公钥')
      }
      
      const hybridData = {
        "username": username,                    // 用户名（明文）
        "password": encryptedPassword,          // AES-256加密的密码
        "encryptedAESKey": encryptedAESKey,     // RSA加密的AES密钥
        "keyId": this.RSA_KEY_ID,               // RSA密钥ID
        "timestamp": timestamp,                  // 时间戳
        encryptionType: 'RSA+AES-256',
        keySize: aesKey.length
      }
      
      console.log('✅ RSA+AES混合加密完成，准备发送数据:')
      console.log('  - 用户名:', hybridData.username)
      console.log('  - 加密密码长度:', hybridData.password.length, '字符')
      console.log('  - 加密AES密钥长度:', hybridData.encryptedAESKey.length, '字符')
      console.log('  - RSA密钥ID:', hybridData.keyId)
      console.log('  - 时间戳:', hybridData.timestamp)
      console.log('  - 加密类型:', hybridData.encryptionType)
      console.log('  - AES密钥尺寸:', hybridData.keySize, '字节')
      
      return hybridData
    } catch (error) {
      console.error('❌ RSA+AES混合加密失败:', error)
      throw new Error('混合加密失败: ' + error.message)
    }
  }

  /**
   * 后端解密示例方法（仅供参考，实际应在后端实现）
   * @param {object} hybridData - 混合加密数据
   * @param {string} privateKey - RSA私钥（后端持有）
   * @returns {object} 解密结果
   */
  static simulateBackendDecryption(hybridData, privateKey) {
    try {
      console.log('🔧 模拟后端解密过程...')
      
      // 注意：这只是演示，实际解密应在后端进行
      // 步骤1: 使用RSA私钥解密AES密钥
      // const decryptedAESKey = rsaDecrypt(hybridData.encryptedAESKey, privateKey)
      
      // 步骤2: 使用解密的AES密钥解密密码
      // const decryptedPassword = aesDecrypt(hybridData.encryptedPassword, decryptedAESKey)
      
      // 步骤3: 验证时间戳
      const currentTime = Date.now()
      const requestTime = parseInt(hybridData.timestamp)
      const timeDiff = currentTime - requestTime
      const isValid = timeDiff < 300000 // 5分钟内有效
      
      return {
        success: true,
        message: '解密成功（模拟）',
        isTimestampValid: isValid,
        timeDifference: timeDiff,
        decryptionSteps: [
          '1. 使用RSA私钥解密AES密钥',
          '2. 使用AES密钥解密密码',
          '3. 验证时间戳有效性'
        ]
      }
    } catch (error) {
      console.error('❌ 后端解密失败:', error)
      return {
        success: false,
        message: '解密失败: ' + error.message
      }
    }
  }
}

export default CryptoUtils

/**
 * 密码加密工具使用示例
 * 演示如何在不同场景下使用CryptoUtils
 */

import { CryptoUtils } from './crypto'

console.log('🔐 密码加密工具使用示例\n')

// ==================== 示例1: 默认登录（推荐） ====================
console.log('📋 示例1: 默认登录模式（后端处理salt和signature）')

const username = 'admin001'
const password = 'MySecurePassword123!'

// 生成默认的安全登录数据
const defaultLoginData = CryptoUtils.generateSecureLoginData(username, password)

console.log('发送给后端的数据:', {
  username: defaultLoginData.username,
  passwordLength: defaultLoginData.password.length,
  timestamp: defaultLoginData.timestamp,
  encryptionType: defaultLoginData.encryptionType
})

console.log('完整数据结构:', defaultLoginData)
console.log('')

// ==================== 示例2: 包含salt和signature（不推荐） ====================
console.log('📋 示例2: 包含salt和signature模式（降低安全性）')

// 生成包含salt和signature的登录数据
const fullLoginData = CryptoUtils.generateSecureLoginData(username, password, true)

console.log('发送给后端的数据:', {
  username: fullLoginData.username,
  passwordLength: fullLoginData.password.length,
  timestamp: fullLoginData.timestamp,
  encryptionType: fullLoginData.encryptionType,
  saltLength: fullLoginData.salt?.length,
  signatureLength: fullLoginData.signature?.length
})

console.log('完整数据结构:', fullLoginData)
console.log('')

// ==================== 示例3: 后端解密演示 ====================
console.log('📋 示例3: 后端解密演示')

// 模拟后端解密过程
const encryptedPassword = defaultLoginData.password
console.log('加密密码:', encryptedPassword)

// 后端解密（仅用于演示，实际应在后端进行）
const decryptedData = CryptoUtils.decryptPasswordWithTimestamp(encryptedPassword)
console.log('解密结果:', decryptedData)

// 验证时间戳有效性
if (decryptedData && decryptedData.isValid) {
  console.log('✅ 时间戳验证通过')
} else {
  console.log('❌ 时间戳验证失败或解密失败')
}

console.log('')

// ==================== 示例4: 密码强度验证 ====================
console.log('📋 示例4: 密码强度验证')

const passwords = [
  '123456',           // 弱密码
  'password',         // 弱密码
  'Password123',      // 中等密码
  'MySecure123!',     // 强密码
  'V3ry$tr0ng!P@ssw0rd'  // 超强密码
]

passwords.forEach(pwd => {
  const strength = CryptoUtils.validatePasswordStrength(pwd)
  console.log(`密码: "${pwd}" -> 分数: ${strength.score}, 反馈: ${strength.feedback.join(', ')}`)
})

console.log('')

// ==================== 示例5: 单独功能演示 ====================
console.log('📋 示例5: 单独功能演示')

// 生成盐值
const salt = CryptoUtils.generateSalt(32)
console.log('生成的盐值:', salt)

// 密码哈希
const hashedPassword = CryptoUtils.hashPassword(password, salt)
console.log('哈希后的密码:', hashedPassword)

// 生成签名
const timestamp = Date.now().toString()
const signature = CryptoUtils.generateSignature(username, encryptedPassword, timestamp)
console.log('生成的签名:', signature)

console.log('')

// ==================== 前端使用建议 ====================
console.log('💡 前端使用建议:')
console.log('1. 默认使用 generateSecureLoginData(username, password) - 不传第三个参数')
console.log('2. 让后端处理salt和signature的生成和验证')
console.log('3. 前端只负责密码加密和强度验证')
console.log('4. 生产环境中将SECRET_KEY移至环境变量')

console.log('')

// ==================== 后端处理建议 ====================
console.log('🔧 后端处理建议:')
console.log('1. 使用相同的SECRET_KEY解密前端发送的密码')
console.log('2. 验证时间戳的有效性（建议5分钟内有效）')
console.log('3. 在后端生成salt和signature用于存储')
console.log('4. 使用BCrypt或类似算法对解密后的密码进行最终哈希存储')

export {
  defaultLoginData,
  fullLoginData,
  decryptedData
}

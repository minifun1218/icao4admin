import { CryptoUtils } from './crypto.js'

/**
 * 密码加密功能演示
 * 这个文件用于演示和测试密码加密功能
 */

console.log('🔐 密码加密功能演示开始...\n')

// 测试数据
const testUsername = 'admin001'
const testPassword = 'AdminPassword123!'

console.log('📝 原始登录数据:')
console.log(`用户名: ${testUsername}`)
console.log(`密码: ${testPassword}`)
console.log(`密码长度: ${testPassword.length} 字符\n`)

// 1. 测试密码强度验证
console.log('💪 密码强度验证:')
const passwordStrength = CryptoUtils.validatePasswordStrength(testPassword)
console.log(`是否有效: ${passwordStrength.isValid}`)
console.log(`安全评分: ${passwordStrength.score}/100`)
console.log(`反馈信息: ${passwordStrength.feedback.join(', ')}\n`)

// 2. 测试密码加密
console.log('🔒 密码加密测试:')
const timestamp = Date.now().toString()
const encryptedPassword = CryptoUtils.encryptPassword(testPassword, timestamp)
console.log(`加密后密码: ${encryptedPassword}`)
console.log(`加密后长度: ${encryptedPassword.length} 字符\n`)

// 3. 测试密码哈希
console.log('🏷️ 密码哈希测试:')
const hashedPassword = CryptoUtils.hashPassword(testPassword)
console.log(`哈希后密码: ${hashedPassword}`)
console.log(`哈希长度: ${hashedPassword.length} 字符\n`)

// 4. 测试签名生成
console.log('✍️ 数字签名测试:')
const signature = CryptoUtils.generateSignature(testUsername, encryptedPassword, timestamp)
console.log(`数字签名: ${signature}`)
console.log(`签名长度: ${signature.length} 字符\n`)

// 5. 测试完整的安全登录数据生成
console.log('🛡️ 安全登录数据生成:')
const secureLoginData = CryptoUtils.generateSecureLoginData(testUsername, testPassword)
console.log('生成的安全数据结构:')
console.log(JSON.stringify({
  username: secureLoginData.username,
  passwordLength: secureLoginData.password.length,
  timestamp: secureLoginData.timestamp,
  saltLength: secureLoginData.salt.length,
  signatureLength: secureLoginData.signature.length,
  encryptionType: secureLoginData.encryptionType
}, null, 2))

// 6. 测试解密功能（仅用于验证）
console.log('\n🔓 解密验证测试:')
const decryptedPassword = CryptoUtils.decryptPassword(encryptedPassword)
const isDecryptionCorrect = decryptedPassword === testPassword
console.log(`解密结果: ${decryptedPassword}`)
console.log(`解密正确: ${isDecryptionCorrect ? '✅' : '❌'}\n`)

// 7. 测试不同强度的密码
console.log('🎯 不同密码强度测试:')
const testPasswords = [
  '123',           // 非常弱
  '123456',        // 弱
  'password',      // 弱
  'Password123',   // 中等
  'MyP@ssw0rd!',   // 强
  'AdminPassword123!' // 强
]

testPasswords.forEach((pwd, index) => {
  const strength = CryptoUtils.validatePasswordStrength(pwd)
  console.log(`${index + 1}. "${pwd}" - 评分: ${strength.score}/100, 有效: ${strength.isValid ? '✅' : '❌'}`)
})

console.log('\n🎉 密码加密功能演示完成!')

// 导出测试结果供其他文件使用
export const demoResults = {
  originalPassword: testPassword,
  encryptedPassword,
  hashedPassword,
  signature,
  secureLoginData,
  decryptedPassword,
  isDecryptionCorrect,
  passwordStrength
}

export default CryptoUtils

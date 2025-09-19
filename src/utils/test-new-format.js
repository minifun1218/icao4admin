/**
 * 测试新的后端响应格式
 * 响应格式: {"code":200,"message":"success","data":{"keyId":"rsa_1758179500197_5db2","publicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7t0C6B+NwjM7WPIqIXSLAAWZeX5JLOHXxnwSeoMj1KBdzou+h2F7BiLgLuXHao5YBWA/u/fKW7uAzvsIsoiTTCynDVd0JIv+7Dx5wAseFy+cNsEpppbinHLtnTGssSK9UZk56nAjryjdYqL1KzcYzuGI66Gizt93UWDNgAOCrMNCAN4CosPvT45RO0AStAfrBATkhhm1d+mWUcpgEmzjcZa8rb1NxEbihqkwfJsbStQgy2IcNNJD87cutYhfahO4UMRcWuSyHE2NsWbEsDjgRfBmKqFWghWv+UOrcjFqvsewDtQ3ST61j/mn1cNnqZcrbosjohskHZvt3wCqgZAHFQIDAQAB","expiresIn":1800}}
 */

import { CryptoUtils } from './crypto'

/**
 * 模拟后端响应数据
 */
const mockBackendResponse = {
  "code": 200,
  "message": "success",
  "data": {
    "keyId": "rsa_1758179500197_5db2",
    "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7t0C6B+NwjM7WPIqIXSLAAWZeX5JLOHXxnwSeoMj1KBdzou+h2F7BiLgLuXHao5YBWA/u/fKW7uAzvsIsoiTTCynDVd0JIv+7Dx5wAseFy+cNsEpppbinHLtnTGssSK9UZk56nAjryjdYqL1KzcYzuGI66Gizt93UWDNgAOCrMNCAN4CosPvT45RO0AStAfrBATkhhm1d+mWUcpgEmzjcZa8rb1NxEbihqkwfJsbStQgy2IcNNJD87cutYhfahO4UMRcWuSyHE2NsWbEsDjgRfBmKqFWghWv+UOrcjFqvsewDtQ3ST61j/mn1cNnqZcrbosjohskHZvt3wCqgZAHFQIDAQAB",
    "expiresIn": 1800
  }
}

/**
 * 测试公钥格式化功能
 */
export function testPublicKeyFormatting() {
  console.log('🧪 测试公钥格式化功能')
  console.log('=' .repeat(50))
  
  const { publicKey } = mockBackendResponse.data
  console.log('原始Base64公钥长度:', publicKey.length)
  console.log('原始Base64公钥 (前50字符):', publicKey.substring(0, 50) + '...')
  
  const formattedKey = CryptoUtils.formatPublicKeyToPEM(publicKey)
  console.log('\n格式化后的PEM公钥:')
  console.log(formattedKey)
  
  console.log('\n格式化后长度:', formattedKey.length)
  console.log('是否包含PEM头部:', formattedKey.includes('BEGIN PUBLIC KEY'))
  console.log('是否包含PEM尾部:', formattedKey.includes('END PUBLIC KEY'))
  
  return {
    original: publicKey,
    formatted: formattedKey,
    success: formattedKey.includes('BEGIN PUBLIC KEY') && formattedKey.includes('END PUBLIC KEY')
  }
}

/**
 * 测试新响应格式解析
 */
export function testResponseParsing() {
  console.log('🧪 测试新响应格式解析')
  console.log('=' .repeat(50))
  
  console.log('模拟后端响应:')
  console.log(JSON.stringify(mockBackendResponse, null, 2))
  
  // 模拟解析过程
  if (mockBackendResponse && mockBackendResponse.code === 200 && mockBackendResponse.data) {
    const { keyId, publicKey, expiresIn } = mockBackendResponse.data
    
    console.log('\n✅ 响应解析成功:')
    console.log('  - KeyID:', keyId)
    console.log('  - 公钥长度:', publicKey.length, '字符')
    console.log('  - 过期时间:', expiresIn, '秒 (', expiresIn / 60, '分钟)')
    
    const formattedKey = CryptoUtils.formatPublicKeyToPEM(publicKey)
    console.log('  - 格式化公钥长度:', formattedKey.length, '字符')
    
    return {
      success: true,
      keyId,
      publicKey: formattedKey,
      expiresIn,
      originalLength: publicKey.length,
      formattedLength: formattedKey.length
    }
  } else {
    console.log('❌ 响应格式不正确')
    return { success: false }
  }
}

/**
 * 测试完整的混合加密流程（使用模拟数据）
 */
export async function testFullEncryptionWithMockData() {
  console.log('🧪 测试完整混合加密流程（模拟数据）')
  console.log('=' .repeat(60))
  
  try {
    // 模拟设置公钥数据
    const { keyId, publicKey, expiresIn } = mockBackendResponse.data
    const formattedKey = CryptoUtils.formatPublicKeyToPEM(publicKey)
    
    // 手动设置公钥信息（模拟从后端获取）
    CryptoUtils.RSA_PUBLIC_KEY = formattedKey
    CryptoUtils.RSA_KEY_ID = keyId
    CryptoUtils.RSA_KEY_EXPIRES_IN = expiresIn
    
    console.log('📝 设置模拟公钥信息:')
    console.log('  - KeyID:', keyId)
    console.log('  - 过期时间:', expiresIn, '秒')
    console.log('  - 公钥长度:', formattedKey.length, '字符')
    
    // 测试数据
    const testUsername = 'testuser'
    const testPassword = 'TestPassword123!'
    
    console.log('\n🔐 开始混合加密测试...')
    const startTime = Date.now()
    
    // 执行混合加密（应该使用缓存的公钥）
    const encryptedData = await CryptoUtils.generateHybridEncryptedLoginData(
      testUsername,
      testPassword
    )
    
    const endTime = Date.now()
    const duration = endTime - startTime
    
    console.log('\n✅ 混合加密完成!')
    console.log('⏱️  耗时:', duration, 'ms')
    
    console.log('\n📊 加密结果验证:')
    console.log('  - 用户名正确:', encryptedData.username === testUsername)
    console.log('  - 包含加密密码:', !!encryptedData.password)
    console.log('  - 包含加密AES密钥:', !!encryptedData.encryptedAESKey)
    console.log('  - 包含密钥ID:', !!encryptedData.keyId)
    console.log('  - 密钥ID正确:', encryptedData.keyId === keyId)
    console.log('  - 包含时间戳:', !!encryptedData.timestamp)
    console.log('  - 加密类型正确:', encryptedData.encryptionType === 'RSA+AES-256')
    
    console.log('\n📤 最终发送数据:')
    console.log(JSON.stringify(encryptedData, null, 2))
    
    return {
      success: true,
      data: encryptedData,
      duration,
      validations: {
        usernameCorrect: encryptedData.username === testUsername,
        hasEncryptedPassword: !!encryptedData.password,
        hasEncryptedAESKey: !!encryptedData.encryptedAESKey,
        hasKeyId: !!encryptedData.keyId,
        keyIdCorrect: encryptedData.keyId === keyId,
        hasTimestamp: !!encryptedData.timestamp,
        encryptionTypeCorrect: encryptedData.encryptionType === 'RSA+AES-256'
      }
    }
    
  } catch (error) {
    console.error('❌ 混合加密测试失败:', error)
    return {
      success: false,
      error: error.message
    }
  }
}

/**
 * 测试密钥信息获取
 */
export function testKeyInfo() {
  console.log('🧪 测试密钥信息获取')
  console.log('=' .repeat(40))
  
  const keyInfo = CryptoUtils.getRSAKeyInfo()
  
  console.log('密钥信息:')
  console.log('  - KeyID:', keyInfo.keyId)
  console.log('  - 是否有公钥:', keyInfo.hasPublicKey)
  console.log('  - 过期时间:', keyInfo.expiresIn, '秒')
  console.log('  - 是否过期:', keyInfo.isExpired)
  
  return keyInfo
}

/**
 * 运行所有测试
 */
export async function runAllNewFormatTests() {
  console.log('🚀 运行所有新格式测试')
  console.log('=' .repeat(70))
  console.log('')
  
  // 测试1: 公钥格式化
  const formatTest = testPublicKeyFormatting()
  console.log('')
  
  // 测试2: 响应解析
  const parseTest = testResponseParsing()
  console.log('')
  
  // 测试3: 完整混合加密
  const encryptionTest = await testFullEncryptionWithMockData()
  console.log('')
  
  // 测试4: 密钥信息
  const keyInfoTest = testKeyInfo()
  console.log('')
  
  console.log('🎯 所有新格式测试完成!')
  console.log('=' .repeat(70))
  
  const allTestsPass = formatTest.success && 
                       parseTest.success && 
                       encryptionTest.success
  
  console.log('总体结果:', allTestsPass ? '✅ 全部通过' : '❌ 部分失败')
  
  return {
    formatTest,
    parseTest,
    encryptionTest,
    keyInfoTest,
    allTestsPass
  }
}

// 浏览器环境下的便捷调用
if (typeof window !== 'undefined') {
  window.testNewFormat = {
    testPublicKeyFormatting,
    testResponseParsing,
    testFullEncryptionWithMockData,
    testKeyInfo,
    runAllNewFormatTests
  }
  
  console.log('🌐 新格式测试函数已加载到 window.testNewFormat')
  console.log('可以调用以下函数:')
  console.log('  - window.testNewFormat.testPublicKeyFormatting()')
  console.log('  - window.testNewFormat.testResponseParsing()')
  console.log('  - window.testNewFormat.testFullEncryptionWithMockData()')
  console.log('  - window.testNewFormat.runAllNewFormatTests()')
}

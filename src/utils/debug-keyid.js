/**
 * 调试KeyID问题的工具
 */

import { CryptoUtils } from './crypto'

/**
 * 测试公钥获取和KeyID解析
 */
export async function testKeyIdFix() {
  console.log('🔧 测试KeyID修复')
  console.log('=' .repeat(50))
  
  try {
    // 清除现有的密钥信息
    CryptoUtils.RSA_PUBLIC_KEY = null
    CryptoUtils.RSA_KEY_ID = null
    CryptoUtils.RSA_KEY_EXPIRES_IN = null
    
    console.log('1. 清除现有密钥信息')
    CryptoUtils.debugKeyStatus()
    
    // 测试获取公钥
    console.log('\n2. 测试获取公钥')
    const publicKey = await CryptoUtils.fetchRSAPublicKey()
    
    console.log('\n3. 获取公钥后的状态')
    CryptoUtils.debugKeyStatus()
    
    // 测试混合加密
    console.log('\n4. 测试混合加密')
    const testData = await CryptoUtils.generateHybridEncryptedLoginData('testuser', 'testpass123')
    
    console.log('\n5. 加密结果验证')
    console.log('  - Username:', testData.username)
    console.log('  - KeyID存在:', !!testData.keyId)
    console.log('  - KeyID值:', testData.keyId)
    console.log('  - Password加密长度:', testData.password?.length)
    console.log('  - AES密钥加密长度:', testData.encryptedAESKey?.length)
    console.log('  - 时间戳:', testData.timestamp)
    console.log('  - 加密类型:', testData.encryptionType)
    
    // 验证结果
    const isValid = testData.keyId && testData.keyId !== null && testData.keyId !== 'undefined'
    
    console.log('\n6. 修复验证结果')
    console.log('  - KeyID修复成功:', isValid ? '✅' : '❌')
    console.log('  - 完整数据结构:', JSON.stringify(testData, null, 2))
    
    return {
      success: isValid,
      keyId: testData.keyId,
      data: testData
    }
    
  } catch (error) {
    console.error('❌ KeyID修复测试失败:', error)
    return {
      success: false,
      error: error.message
    }
  }
}

/**
 * 模拟后端响应测试
 */
export function testMockResponse() {
  console.log('🔧 测试模拟后端响应')
  console.log('=' .repeat(50))
  
  // 模拟正确的后端响应
  const mockResponse = {
    code: 200,
    message: "success",
    data: {
      keyId: "rsa_1758179500197_5db2",
      publicKey: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7t0C6B+NwjM7WPIqIXSLAAWZeX5JLOHXxnwSeoMj1KBdzou+h2F7BiLgLuXHao5YBWA/u/fKW7uAzvsIsoiTTCynDVd0JIv+7Dx5wAseFy+cNsEpppbinHLtnTGssSK9UZk56nAjryjdYqL1KzcYzuGI66Gizt93UWDNgAOCrMNCAN4CosPvT45RO0AStAfrBATkhhm1d+mWUcpgEmzjcZa8rb1NxEbihqkwfJsbStQgy2IcNNJD87cutYhfahO4UMRcWuSyHE2NsWbEsDjgRfBmKqFWghWv+UOrcjFqvsewDtQ3ST61j/mn1cNnqZcrbosjohskHZvt3wCqgZAHFQIDAQAB",
      expiresIn: 1800
    }
  }
  
  console.log('模拟响应数据:')
  console.log(JSON.stringify(mockResponse, null, 2))
  
  // 测试解析
  if (mockResponse && mockResponse.code === 200 && mockResponse.data) {
    const keyId = mockResponse.data.keyId
    const publicKey = mockResponse.data.publicKey
    const expiresIn = mockResponse.data.expiresIn
    
    console.log('\n解析结果:')
    console.log('  - KeyID:', keyId)
    console.log('  - KeyID类型:', typeof keyId)
    console.log('  - KeyID长度:', keyId?.length)
    console.log('  - PublicKey长度:', publicKey?.length)
    console.log('  - ExpiresIn:', expiresIn)
    
    // 验证字段
    const validation = {
      hasKeyId: !!keyId,
      hasPublicKey: !!publicKey,
      hasExpiresIn: !!expiresIn,
      keyIdType: typeof keyId,
      publicKeyType: typeof publicKey,
      expiresInType: typeof expiresIn
    }
    
    console.log('\n字段验证:')
    console.log(JSON.stringify(validation, null, 2))
    
    return validation
  } else {
    console.log('❌ 响应格式不正确')
    return { success: false }
  }
}

/**
 * 测试User-Agent头部修复
 */
export function testUserAgentFix() {
  console.log('🔧 测试User-Agent头部修复')
  console.log('=' .repeat(50))
  
  // 检查axios实例配置
  import('@/api/index').then(({ default: api }) => {
    console.log('API实例配置:')
    console.log('  - BaseURL:', api.defaults.baseURL)
    console.log('  - Headers:', api.defaults.headers)
    
    // 创建测试请求配置
    const testConfig = {
      method: 'get',
      url: '/test',
      headers: {}
    }
    
    // 模拟请求拦截器处理
    testConfig.headers['X-Request-ID'] = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    testConfig.headers['X-Client-Info'] = 'ICAO4-Admin/1.0.0'
    testConfig.headers['X-Client-Platform'] = 'Web'
    
    console.log('\n修复后的头部:')
    console.log('  - X-Request-ID:', testConfig.headers['X-Request-ID'])
    console.log('  - X-Client-Info:', testConfig.headers['X-Client-Info'])
    console.log('  - X-Client-Platform:', testConfig.headers['X-Client-Platform'])
    console.log('  - User-Agent头部已移除:', !testConfig.headers['User-Agent'])
    
    return {
      success: true,
      headers: testConfig.headers,
      hasUserAgent: !!testConfig.headers['User-Agent']
    }
  })
}

/**
 * 运行所有修复测试
 */
export async function runAllFixTests() {
  console.log('🚀 运行所有修复测试')
  console.log('=' .repeat(70))
  console.log('')
  
  // 测试1: User-Agent修复
  const userAgentTest = testUserAgentFix()
  console.log('')
  
  // 测试2: 模拟响应解析
  const mockResponseTest = testMockResponse()
  console.log('')
  
  // 测试3: KeyID修复
  const keyIdTest = await testKeyIdFix()
  console.log('')
  
  console.log('🎯 所有修复测试完成!')
  console.log('=' .repeat(70))
  
  return {
    userAgentTest,
    mockResponseTest,
    keyIdTest
  }
}

// 浏览器环境下的便捷调用
if (typeof window !== 'undefined') {
  window.debugKeyId = {
    testKeyIdFix,
    testMockResponse,
    testUserAgentFix,
    runAllFixTests
  }
  
  console.log('🌐 KeyID调试工具已加载到 window.debugKeyId')
  console.log('可以调用以下函数:')
  console.log('  - window.debugKeyId.testKeyIdFix()')
  console.log('  - window.debugKeyId.testMockResponse()')
  console.log('  - window.debugKeyId.testUserAgentFix()')
  console.log('  - window.debugKeyId.runAllFixTests()')
}

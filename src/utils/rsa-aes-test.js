/**
 * RSA+AES混合加密测试
 * 测试完整的加密流程
 */

import { CryptoUtils } from './crypto'

/**
 * 测试RSA+AES混合加密完整流程
 */
export async function testHybridEncryption() {
  console.log('🧪 开始测试RSA+AES混合加密流程')
  console.log('=' .repeat(60))
  
  try {
    // 测试数据
    const testUsername = 'testuser001'
    const testPassword = 'TestPassword123!'
    
    console.log('📝 测试数据:')
    console.log('  用户名:', testUsername)
    console.log('  密码长度:', testPassword.length, '字符')
    console.log('')
    
    // 执行混合加密
    console.log('🚀 开始执行混合加密...')
    const startTime = Date.now()
    
    const hybridData = await CryptoUtils.generateHybridEncryptedLoginData(
      testUsername,
      testPassword
    )
    
    const endTime = Date.now()
    const duration = endTime - startTime
    
    console.log('')
    console.log('✅ 混合加密测试完成!')
    console.log('⏱️  加密耗时:', duration, 'ms')
    console.log('')
    
    // 验证结果
    console.log('🔍 验证加密结果:')
    console.log('  用户名:', hybridData.username === testUsername ? '✅' : '❌')
    console.log('  加密密码存在:', hybridData.encryptedPassword ? '✅' : '❌')
    console.log('  加密AES密钥存在:', hybridData.encryptedAESKey ? '✅' : '❌')
    console.log('  时间戳有效:', hybridData.timestamp ? '✅' : '❌')
    console.log('  加密类型正确:', hybridData.encryptionType === 'RSA+AES-256' ? '✅' : '❌')
    
    // 显示详细信息
    console.log('')
    console.log('📊 加密数据详情:')
    console.log('  用户名:', hybridData.username)
    console.log('  加密密码 (前20字符):', hybridData.encryptedPassword.substring(0, 20) + '...')
    console.log('  加密密码总长度:', hybridData.encryptedPassword.length, '字符')
    console.log('  加密AES密钥 (前20字符):', hybridData.encryptedAESKey.substring(0, 20) + '...')
    console.log('  加密AES密钥总长度:', hybridData.encryptedAESKey.length, '字符')
    console.log('  时间戳:', hybridData.timestamp)
    console.log('  加密类型:', hybridData.encryptionType)
    console.log('  AES密钥尺寸:', hybridData.keySize, '字节')
    
    // 生成发送给后端的JSON
    console.log('')
    console.log('📤 将发送给后端的JSON数据:')
    console.log(JSON.stringify(hybridData, null, 2))
    
    // 验证时间戳
    const currentTime = Date.now()
    const requestTime = parseInt(hybridData.timestamp)
    const timeDiff = currentTime - requestTime
    
    console.log('')
    console.log('⏰ 时间戳验证:')
    console.log('  当前时间:', currentTime)
    console.log('  请求时间:', requestTime)
    console.log('  时间差:', timeDiff, 'ms')
    console.log('  5分钟内有效:', timeDiff < 300000 ? '✅' : '❌')
    
    return {
      success: true,
      data: hybridData,
      duration: duration,
      tests: {
        usernameCorrect: hybridData.username === testUsername,
        passwordEncrypted: !!hybridData.encryptedPassword,
        aesKeyEncrypted: !!hybridData.encryptedAESKey,
        timestampValid: !!hybridData.timestamp,
        encryptionTypeCorrect: hybridData.encryptionType === 'RSA+AES-256'
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
 * 测试公钥获取
 */
export async function testPublicKeyFetch() {
  console.log('🧪 测试公钥获取')
  console.log('=' .repeat(40))
  
  try {
    const startTime = Date.now()
    const publicKey = await CryptoUtils.fetchRSAPublicKey()
    const endTime = Date.now()
    
    console.log('✅ 公钥获取成功')
    console.log('⏱️  获取耗时:', endTime - startTime, 'ms')
    console.log('📏 公钥长度:', publicKey.length, '字符')
    console.log('🔑 公钥格式:', publicKey.includes('BEGIN PUBLIC KEY') ? 'PEM格式 ✅' : '其他格式')
    console.log('')
    console.log('📝 公钥内容 (前100字符):')
    console.log(publicKey.substring(0, 100) + '...')
    
    return {
      success: true,
      publicKey: publicKey,
      length: publicKey.length,
      format: publicKey.includes('BEGIN PUBLIC KEY') ? 'PEM' : 'Unknown'
    }
    
  } catch (error) {
    console.error('❌ 公钥获取测试失败:', error)
    return {
      success: false,
      error: error.message
    }
  }
}

/**
 * 性能测试
 */
export async function performanceTest(iterations = 5) {
  console.log('🧪 性能测试 - 执行', iterations, '次加密')
  console.log('=' .repeat(50))
  
  const results = []
  const testData = {
    username: 'perftest',
    password: 'PerformanceTest123!'
  }
  
  for (let i = 0; i < iterations; i++) {
    console.log(`🔄 第 ${i + 1}/${iterations} 次测试`)
    
    const startTime = Date.now()
    
    try {
      const result = await CryptoUtils.generateHybridEncryptedLoginData(
        testData.username,
        testData.password
      )
      
      const endTime = Date.now()
      const duration = endTime - startTime
      
      results.push({
        iteration: i + 1,
        success: true,
        duration: duration,
        encryptedPasswordLength: result.encryptedPassword.length,
        encryptedAESKeyLength: result.encryptedAESKey.length
      })
      
      console.log(`  ✅ 耗时: ${duration}ms`)
      
    } catch (error) {
      const endTime = Date.now()
      const duration = endTime - startTime
      
      results.push({
        iteration: i + 1,
        success: false,
        duration: duration,
        error: error.message
      })
      
      console.log(`  ❌ 失败: ${error.message}`)
    }
  }
  
  // 统计结果
  const successCount = results.filter(r => r.success).length
  const failCount = results.filter(r => !r.success).length
  const durations = results.filter(r => r.success).map(r => r.duration)
  
  if (durations.length > 0) {
    const avgDuration = durations.reduce((a, b) => a + b, 0) / durations.length
    const minDuration = Math.min(...durations)
    const maxDuration = Math.max(...durations)
    
    console.log('')
    console.log('📊 性能测试结果:')
    console.log('  成功次数:', successCount)
    console.log('  失败次数:', failCount)
    console.log('  成功率:', (successCount / iterations * 100).toFixed(1) + '%')
    console.log('  平均耗时:', avgDuration.toFixed(1) + 'ms')
    console.log('  最快耗时:', minDuration + 'ms')
    console.log('  最慢耗时:', maxDuration + 'ms')
  }
  
  return {
    iterations,
    successCount,
    failCount,
    successRate: successCount / iterations * 100,
    results
  }
}

/**
 * 运行所有测试
 */
export async function runAllTests() {
  console.log('🚀 运行所有RSA+AES混合加密测试')
  console.log('=' .repeat(70))
  console.log('')
  
  // 测试1: 公钥获取
  const publicKeyTest = await testPublicKeyFetch()
  console.log('')
  
  // 测试2: 混合加密
  const encryptionTest = await testHybridEncryption()
  console.log('')
  
  // 测试3: 性能测试
  const performanceResult = await performanceTest(3)
  console.log('')
  
  console.log('🎯 所有测试完成!')
  console.log('=' .repeat(70))
  
  return {
    publicKeyTest,
    encryptionTest,
    performanceResult
  }
}

// 浏览器环境下的便捷调用
if (typeof window !== 'undefined') {
  window.testRSAAES = {
    testHybridEncryption,
    testPublicKeyFetch,
    performanceTest,
    runAllTests
  }
  
  console.log('🌐 RSA+AES测试函数已加载到 window.testRSAAES')
  console.log('可以调用以下函数:')
  console.log('  - window.testRSAAES.testHybridEncryption()')
  console.log('  - window.testRSAAES.testPublicKeyFetch()')
  console.log('  - window.testRSAAES.performanceTest(5)')
  console.log('  - window.testRSAAES.runAllTests()')
}

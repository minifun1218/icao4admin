/**
 * RSA+AES混合加密演示
 * 展示完整的加密和解密流程
 */

import { CryptoUtils } from './crypto'

console.log('🔐 RSA+AES混合加密演示开始\n')

// 演示用户信息
const demoUser = {
  username: 'admin001',
  password: 'MySecurePassword123!'
}

/**
 * 演示完整的RSA+AES混合加密流程
 */
async function demonstrateHybridEncryption() {
  try {
    console.log('=' .repeat(60))
    console.log('🚀 RSA+AES混合加密流程演示')
    console.log('=' .repeat(60))
    
    // 步骤1: 前端获取公钥（登录前）
    console.log('\n📋 步骤1: 前端获取RSA公钥')
    console.log('- 后端生成RSA密钥对，私钥安全保存')
    console.log('- 前端向后端请求公钥')
    console.log('- 公钥可以安全传输，即使被拦截也无影响')
    
    const publicKey = await CryptoUtils.fetchRSAPublicKey()
    console.log('✅ 公钥获取成功')
    
    // 步骤2: 前端混合加密（登录时）
    console.log('\n📋 步骤2: 前端混合加密过程')
    console.log('- 生成一次性AES密钥（256位随机字符串）')
    console.log('- 使用RSA公钥加密AES密钥')
    console.log('- 使用AES密钥加密用户密码')
    console.log('- 将两个密文一起发送给后端')
    
    const hybridData = await CryptoUtils.generateHybridEncryptedLoginData(
      demoUser.username,
      demoUser.password
    )
    
    console.log('\n📦 前端发送给后端的数据:')
    console.log(JSON.stringify({
      username: hybridData.username,
      password: hybridData.encryptedPassword.substring(0, 50) + '...',
      encryptedAESKey: hybridData.encryptedAESKey.substring(0, 50) + '...',
      timestamp: hybridData.timestamp,
      encryptionType: hybridData.encryptionType,
      keySize: hybridData.keySize
    }, null, 2))
    
    // 步骤3: 后端解密（登录时）
    console.log('\n📋 步骤3: 后端解密过程（模拟）')
    console.log('- 使用RSA私钥解密AES密钥')
    console.log('- 使用解密的AES密钥解密密码')
    console.log('- 验证时间戳有效性')
    console.log('- 与数据库中的密码哈希值比对')
    
    const decryptionResult = CryptoUtils.simulateBackendDecryption(hybridData, 'private-key')
    
    console.log('\n🔧 后端解密结果（模拟）:')
    console.log(JSON.stringify(decryptionResult, null, 2))
    
    // 安全性分析
    console.log('\n' + '=' .repeat(60))
    console.log('🛡️ 安全性分析')
    console.log('=' .repeat(60))
    
    console.log('\n✅ 安全优势:')
    console.log('1. 每次登录都使用不同的AES密钥（一次性密钥）')
    console.log('2. AES密钥使用RSA公钥加密，只有后端私钥能解密')
    console.log('3. 用户密码使用强AES-256加密')
    console.log('4. 时间戳防止重放攻击')
    console.log('5. 即使网络被监听，攻击者也无法获取密码')
    
    console.log('\n🔒 加密层级:')
    console.log('- 第一层: 用户密码 → AES-256加密 → 密文1')
    console.log('- 第二层: AES密钥 → RSA-2048加密 → 密文2')
    console.log('- 传输: {密文1, 密文2, 用户名, 时间戳}')
    
    console.log('\n⚡ 性能特点:')
    console.log('- RSA只加密小数据（AES密钥），性能好')
    console.log('- AES加密大数据（密码），速度快')
    console.log('- 结合了对称和非对称加密的优势')
    
    return hybridData
    
  } catch (error) {
    console.error('❌ 混合加密演示失败:', error)
    throw error
  }
}

/**
 * 比较不同加密方案
 */
async function compareEncryptionMethods() {
  console.log('\n' + '=' .repeat(60))
  console.log('📊 加密方案对比')
  console.log('=' .repeat(60))
  
  try {
    // 传统AES加密
    console.log('\n🔒 传统AES加密:')
    const aesData = CryptoUtils.generateSecureLoginData(demoUser.username, demoUser.password)
    console.log('- 加密类型:', aesData.encryptionType)
    console.log('- 密文长度:', aesData.password.length)
    console.log('- 安全性: 中等（密钥固定）')
    
    // RSA+AES混合加密
    console.log('\n🚀 RSA+AES混合加密:')
    const hybridData = await CryptoUtils.generateHybridEncryptedLoginData(demoUser.username, demoUser.password)
    console.log('- 加密类型:', hybridData.encryptionType)
    console.log('- 密码密文长度:', hybridData.encryptedPassword.length)
    console.log('- AES密钥密文长度:', hybridData.encryptedAESKey.length)
    console.log('- 安全性: 高（密钥动态生成）')
    
    console.log('\n📈 推荐使用: RSA+AES混合加密')
    console.log('- 更高的安全性')
    console.log('- 动态密钥管理')
    console.log('- 防止密钥泄露风险')
    
  } catch (error) {
    console.error('❌ 加密方案对比失败:', error)
  }
}

/**
 * 后端实现建议
 */
function backendImplementationGuidelines() {
  console.log('\n' + '=' .repeat(60))
  console.log('🔧 后端实现建议')
  console.log('=' .repeat(60))
  
  console.log('\n📋 Java后端实现步骤:')
  console.log(`
1. 生成RSA密钥对:
   KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
   keyGen.initialize(2048);
   KeyPair keyPair = keyGen.generateKeyPair();

2. 提供公钥接口:
   @GetMapping("/admin/public-key")
   public ResponseEntity<Map<String, String>> getPublicKey() {
       String publicKey = Base64.encode(keyPair.getPublic().getEncoded());
       return ResponseEntity.ok(Map.of("publicKey", publicKey));
   }

3. 登录时解密:
   @PostMapping("/admin/login")
   public ResponseEntity<?> login(@RequestBody HybridLoginRequest request) {
       // 使用私钥解密AES密钥
       String aesKey = decryptWithRSA(request.getEncryptedAESKey(), privateKey);
       
       // 使用AES密钥解密密码
       String password = decryptWithAES(request.getEncryptedPassword(), aesKey);
       
       // 验证用户...
   }
`)
  
  console.log('\n⚠️ 安全注意事项:')
  console.log('- RSA私钥必须安全存储，不能泄露')
  console.log('- 验证时间戳，防止重放攻击')
  console.log('- 解密后立即清除内存中的AES密钥')
  console.log('- 使用HTTPS传输所有数据')
  console.log('- 定期轮换RSA密钥对')
}

// 执行演示
async function runDemo() {
  try {
    await demonstrateHybridEncryption()
    await compareEncryptionMethods()
    backendImplementationGuidelines()
    
    console.log('\n' + '=' .repeat(60))
    console.log('✅ RSA+AES混合加密演示完成')
    console.log('=' .repeat(60))
    
  } catch (error) {
    console.error('❌ 演示执行失败:', error)
  }
}

// 导出演示函数
export {
  demonstrateHybridEncryption,
  compareEncryptionMethods,
  backendImplementationGuidelines,
  runDemo
}

// 如果直接运行此文件，执行演示
if (typeof window !== 'undefined') {
  // 浏览器环境
  console.log('🌐 在浏览器控制台中运行演示')
  console.log('可以调用 runDemo() 函数开始演示')
} else {
  // Node.js环境
  runDemo()
}

<template>
  <div class="rsa-test-page">
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <h2>🔐 RSA+AES混合加密测试</h2>
          <p>测试完整的RSA+AES混合加密流程</p>
        </div>
      </template>

      <!-- 测试表单 -->
      <el-form :model="testForm" label-width="120px" class="test-form">
        <el-form-item label="用户名:">
          <el-input v-model="testForm.username" placeholder="请输入测试用户名" />
        </el-form-item>
        
        <el-form-item label="密码:">
          <el-input 
            v-model="testForm.password" 
            type="password" 
            placeholder="请输入测试密码"
            show-password 
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="testHybridEncryption" 
            :loading="testing"
            icon="Lock"
          >
            测试RSA+AES混合加密
          </el-button>
          
          <el-button 
            type="success" 
            @click="testPublicKey" 
            :loading="fetchingKey"
            icon="Key"
          >
            测试获取公钥
          </el-button>
          
          <el-button 
            type="warning" 
            @click="runPerformanceTest" 
            :loading="performanceTesting"
            icon="Timer"
          >
            性能测试
          </el-button>
          
          <el-button 
            type="info" 
            @click="debugKeyStatus" 
            icon="Bug"
          >
            调试KeyID
          </el-button>
        </el-form-item>
      </el-form>

      <!-- API端点信息 -->
      <el-alert
        title="API端点信息"
        type="info"
        :closable="false"
        class="api-info"
      >
        <p><strong>公钥获取端点:</strong> http://127.0.0.1:8080/api/security/public-key</p>
        <p><strong>登录端点:</strong> http://127.0.0.1:8080/api/admin/login</p>
      </el-alert>

      <!-- 测试结果 -->
      <div v-if="testResult" class="test-result">
        <h3>🧪 测试结果</h3>
        
        <el-alert
          :title="testResult.success ? '✅ 测试成功' : '❌ 测试失败'"
          :type="testResult.success ? 'success' : 'error'"
          :closable="false"
          class="result-alert"
        >
          <p v-if="testResult.duration">⏱️ 耗时: {{ testResult.duration }}ms</p>
          <p v-if="testResult.error">错误: {{ testResult.error }}</p>
        </el-alert>

        <!-- 加密数据详情 -->
        <div v-if="testResult.data" class="encryption-details">
          <h4>📊 加密数据详情</h4>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">
              {{ testResult.data.username }}
            </el-descriptions-item>
            <el-descriptions-item label="加密类型">
              <el-tag :type="testResult.data.encryptionType === 'RSA+AES-256' ? 'success' : 'warning'">
                {{ testResult.data.encryptionType }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="时间戳">
              {{ new Date(parseInt(testResult.data.timestamp)).toLocaleString() }}
            </el-descriptions-item>
            <el-descriptions-item label="AES密钥尺寸">
              {{ testResult.data.keySize }} 字节 ({{ testResult.data.keySize * 8 }} 位)
            </el-descriptions-item>
            <el-descriptions-item label="RSA密钥ID" v-if="testResult.data.keyId">
              <el-tag type="info">{{ testResult.data.keyId }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="加密密码长度">
              {{ testResult.data.encryptedPassword?.length }} 字符
            </el-descriptions-item>
            <el-descriptions-item label="加密AES密钥长度">
              {{ testResult.data.encryptedAESKey?.length }} 字符
            </el-descriptions-item>
          </el-descriptions>

          <!-- JSON数据预览 -->
          <div class="json-preview">
            <h4>📤 发送给后端的JSON数据</h4>
            <el-input
              v-model="jsonData"
              type="textarea"
              :rows="12"
              readonly
              class="json-textarea"
            />
            <el-button 
              @click="copyToClipboard" 
              type="primary" 
              size="small" 
              class="copy-btn"
              icon="CopyDocument"
            >
              复制JSON
            </el-button>
          </div>
        </div>
      </div>

      <!-- 公钥测试结果 -->
      <div v-if="publicKeyResult" class="public-key-result">
        <h3>🔑 公钥测试结果</h3>
        
        <el-alert
          :title="publicKeyResult.success ? '✅ 公钥获取成功' : '❌ 公钥获取失败'"
          :type="publicKeyResult.success ? 'success' : 'error'"
          :closable="false"
        >
          <p v-if="publicKeyResult.length">📏 公钥长度: {{ publicKeyResult.length }} 字符</p>
          <p v-if="publicKeyResult.format">🔖 公钥格式: {{ publicKeyResult.format }}</p>
          <p v-if="publicKeyResult.error">❌ 错误: {{ publicKeyResult.error }}</p>
        </el-alert>

        <div v-if="publicKeyResult.publicKey" class="public-key-display">
          <h4>📝 公钥内容 (前200字符)</h4>
          <el-input
            :value="publicKeyResult.publicKey.substring(0, 200) + '...'"
            type="textarea"
            :rows="6"
            readonly
          />
        </div>
      </div>

      <!-- 性能测试结果 -->
      <div v-if="performanceResult" class="performance-result">
        <h3>⚡ 性能测试结果</h3>
        
        <el-descriptions :column="3" border>
          <el-descriptions-item label="测试次数">
            {{ performanceResult.iterations }}
          </el-descriptions-item>
          <el-descriptions-item label="成功次数">
            <el-tag type="success">{{ performanceResult.successCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="失败次数">
            <el-tag type="danger">{{ performanceResult.failCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成功率">
            <el-tag :type="performanceResult.successRate === 100 ? 'success' : 'warning'">
              {{ performanceResult.successRate.toFixed(1) }}%
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="平均耗时" v-if="performanceResult.avgDuration">
            {{ performanceResult.avgDuration.toFixed(1) }}ms
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { CryptoUtils } from '@/utils/crypto'

// 测试表单数据
const testForm = reactive({
  username: 'testuser001',
  password: 'TestPassword123!'
})

// 状态管理
const testing = ref(false)
const fetchingKey = ref(false)
const performanceTesting = ref(false)

// 测试结果
const testResult = ref(null)
const publicKeyResult = ref(null)
const performanceResult = ref(null)
const jsonData = ref('')

/**
 * 测试RSA+AES混合加密
 */
const testHybridEncryption = async () => {
  if (!testForm.username || !testForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  testing.value = true
  testResult.value = null

  try {
    console.log('🧪 开始测试RSA+AES混合加密...')
    
    const startTime = Date.now()
    
    const hybridData = await CryptoUtils.generateHybridEncryptedLoginData(
      testForm.username,
      testForm.password
    )
    
    const endTime = Date.now()
    const duration = endTime - startTime

    testResult.value = {
      success: true,
      data: hybridData,
      duration: duration
    }

    // 格式化JSON数据
    jsonData.value = JSON.stringify(hybridData, null, 2)

    ElMessage.success(`RSA+AES混合加密测试成功！耗时: ${duration}ms`)
    
  } catch (error) {
    console.error('❌ 测试失败:', error)
    
    testResult.value = {
      success: false,
      error: error.message
    }

    ElMessage.error('RSA+AES混合加密测试失败: ' + error.message)
  } finally {
    testing.value = false
  }
}

/**
 * 测试获取公钥
 */
const testPublicKey = async () => {
  fetchingKey.value = true
  publicKeyResult.value = null

  try {
    console.log('🧪 开始测试公钥获取...')
    
    const startTime = Date.now()
    const publicKey = await CryptoUtils.fetchRSAPublicKey()
    const endTime = Date.now()

    publicKeyResult.value = {
      success: true,
      publicKey: publicKey,
      length: publicKey.length,
      format: publicKey.includes('BEGIN PUBLIC KEY') ? 'PEM' : 'Unknown',
      duration: endTime - startTime
    }

    ElMessage.success('公钥获取测试成功！')
    
  } catch (error) {
    console.error('❌ 公钥获取测试失败:', error)
    
    publicKeyResult.value = {
      success: false,
      error: error.message
    }

    ElMessage.error('公钥获取测试失败: ' + error.message)
  } finally {
    fetchingKey.value = false
  }
}

/**
 * 运行性能测试
 */
const runPerformanceTest = async () => {
  performanceTesting.value = true
  performanceResult.value = null

  try {
    console.log('🧪 开始性能测试...')
    
    const iterations = 3
    const results = []
    
    for (let i = 0; i < iterations; i++) {
      const startTime = Date.now()
      
      try {
        await CryptoUtils.generateHybridEncryptedLoginData(
          testForm.username,
          testForm.password
        )
        
        const endTime = Date.now()
        const duration = endTime - startTime
        
        results.push({
          success: true,
          duration: duration
        })
        
      } catch (error) {
        const endTime = Date.now()
        const duration = endTime - startTime
        
        results.push({
          success: false,
          duration: duration,
          error: error.message
        })
      }
    }

    // 计算统计信息
    const successCount = results.filter(r => r.success).length
    const failCount = results.filter(r => !r.success).length
    const durations = results.filter(r => r.success).map(r => r.duration)
    const avgDuration = durations.length > 0 ? 
      durations.reduce((a, b) => a + b, 0) / durations.length : 0

    performanceResult.value = {
      iterations,
      successCount,
      failCount,
      successRate: (successCount / iterations) * 100,
      avgDuration,
      results
    }

    ElMessage.success(`性能测试完成！平均耗时: ${avgDuration.toFixed(1)}ms`)
    
  } catch (error) {
    console.error('❌ 性能测试失败:', error)
    ElMessage.error('性能测试失败: ' + error.message)
  } finally {
    performanceTesting.value = false
  }
}

/**
 * 调试KeyID状态
 */
const debugKeyStatus = () => {
  console.log('🔧 调试KeyID状态')
  CryptoUtils.debugKeyStatus()
  
  const keyInfo = CryptoUtils.getRSAKeyInfo()
  console.log('密钥信息:', keyInfo)
  
  ElMessage.info('KeyID调试信息已输出到控制台，请查看')
}

/**
 * 复制JSON到剪贴板
 */
const copyToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(jsonData.value)
    ElMessage.success('JSON数据已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.rsa-test-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-card {
  margin-bottom: 20px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #409eff;
}

.card-header p {
  margin: 0;
  color: #666;
}

.test-form {
  margin-bottom: 20px;
}

.api-info {
  margin: 20px 0;
}

.api-info p {
  margin: 5px 0;
}

.test-result,
.public-key-result,
.performance-result {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.result-alert {
  margin-bottom: 20px;
}

.encryption-details {
  margin-top: 20px;
}

.json-preview {
  margin-top: 20px;
}

.json-textarea {
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.copy-btn {
  margin-top: 10px;
}

.public-key-display {
  margin-top: 15px;
}

.public-key-display .el-textarea {
  font-family: 'Courier New', monospace;
  font-size: 12px;
}
</style>


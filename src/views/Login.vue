<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="/favicon.ico" alt="Logo" class="logo" />
        <h1>ICAO4 管理系统</h1>
        <p>International Civil Aviation Organization English Training Admin</p>
      </div>
      
      <el-form
        ref="loginForm"
        :model="loginData"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginData.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginData.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <div class="register-link">
            还没有账号？
            <router-link to="/register" class="link">立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>&copy; 2024 ICAO4 English Training System. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { CryptoUtils } from '@/utils/crypto'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = ref(null)
const loading = ref(false)

const loginData = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为6-100个字符', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value) {
          const strength = CryptoUtils.validatePasswordStrength(value)
          if (!strength.isValid) {
            callback(new Error(strength.feedback.join(', ')))
          } else {
            callback()
          }
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

const handleLogin = async () => {
  if (!loginForm.value) return
  
  try {
    await loginForm.value.validate()
    loading.value = true
    
    console.log('开始加密登录数据...')
    
    // 优先使用RSA+AES混合加密方案
    let secureLoginData
    
    try {
      console.log('🚀 使用RSA+AES混合加密方案')
      console.log('📍 API端点: http://127.0.0.1:8080/api/security/public-key')
      
      // 使用RSA+AES混合加密（推荐方案）
      secureLoginData = await CryptoUtils.generateHybridEncryptedLoginData(
        loginData.username,
        loginData.password
      )
      
      console.log('✅ RSA+AES混合加密成功')
      
    } catch (hybridError) {
      console.warn('⚠️ RSA+AES混合加密失败，回退到传统AES加密:', hybridError.message)
      
      // 回退到传统AES加密
      console.log('🔒 使用传统AES加密方案（回退）')
      secureLoginData = CryptoUtils.generateSecureLoginData(
        loginData.username,
        loginData.password
      )
      
      console.log('✅ 传统AES加密成功（回退方案）')
    }
    
    console.log('📦 加密后的登录数据:', {
      username: secureLoginData.username,
      encryptionType: secureLoginData.encryptionType,
      timestamp: secureLoginData.timestamp,
      ...(secureLoginData.encryptedPassword && {
        encryptedPasswordLength: secureLoginData.encryptedPassword.length
      }),
      ...(secureLoginData.encryptedAESKey && {
        encryptedAESKeyLength: secureLoginData.encryptedAESKey.length
      }),
      ...(secureLoginData.password && {
        passwordLength: secureLoginData.password.length
      })
    })
    
    // 使用加密数据进行登录
    await authStore.login(secureLoginData)
    
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('❌ 登录失败:', error)
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 检查是否已登录
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/')
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
}

.login-header h1 {
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.login-header p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  color: #95a5a6;
  font-size: 12px;
}

.login-footer p {
  margin: 0;
}

.register-link {
  text-align: center;
  color: #7f8c8d;
  font-size: 14px;
}

.register-link .link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
}

.register-link .link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 10px;
  }
  
  .login-box {
    padding: 30px 20px;
  }
  
  .login-header h1 {
    font-size: 20px;
  }
}
</style>

<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <img src="/favicon.ico" alt="Logo" class="logo" />
        <h1>ICAO4 管理系统</h1>
        <p>International Civil Aviation Organization English Training Admin</p>
        <div class="register-subtitle">
          <h2>管理员注册</h2>
        </div>
      </div>
      
      <el-form
        ref="registerForm"
        :model="registerData"
        :rules="registerRules"
        class="register-form"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerData.username"
            placeholder="请输入用户名（3-50字符）"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="registerData.email"
            type="email"
            placeholder="请输入邮箱地址"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input
            v-model="registerData.phone"
            placeholder="请输入手机号码"
            size="large"
            prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerData.password"
            type="password"
            placeholder="请输入密码（6-100字符）"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerData.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="avatar" label="头像">
          <div class="avatar-upload">
            <div class="avatar-preview" @click="$refs.avatarUpload.click()">
              <img v-if="avatarPreview" :src="avatarPreview" alt="头像预览" class="avatar-image" />
              <div v-else class="avatar-placeholder">
                <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传头像</div>
              </div>
            </div>
            <input
              ref="avatarUpload"
              type="file"
              accept="image/*"
              @change="handleAvatarChange"
              style="display: none"
            />
            <div class="avatar-upload-tips">
              <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
              <el-button v-if="avatarPreview" @click="removeAvatar" size="small" type="danger" plain>
                移除头像
              </el-button>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading || uploading"
            @click="handleRegister"
            class="register-btn"
          >
            {{ uploading ? '上传头像中...' : loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <div class="login-link">
            已有账号？
            <router-link to="/login" class="link">立即登录</router-link>
          </div>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <p>&copy; 2024 ICAO4 English Training System. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { uploadApi } from '@/api'
import { CryptoUtils } from '@/utils/crypto'

const router = useRouter()
const authStore = useAuthStore()

const registerForm = ref(null)
const avatarUpload = ref(null)
const loading = ref(false)
const uploading = ref(false)
const avatarPreview = ref('')
const avatarFile = ref(null)

const registerData = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  avatar: ''
})

// 自定义验证器
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 100) {
    callback(new Error('密码长度为6-100个字符'))
  } else {
    // 验证密码强度
    const strength = CryptoUtils.validatePasswordStrength(value)
    if (!strength.isValid) {
      callback(new Error(strength.feedback.join(', ')))
    } else {
      if (registerData.confirmPassword !== '') {
        registerForm.value.validateField('confirmPassword')
      }
      callback()
    }
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== registerData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入手机号码'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入邮箱地址'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 处理头像文件选择
const handleAvatarChange = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('请选择 JPG、PNG 或 GIF 格式的图片')
    return
  }
  
  // 验证文件大小（2MB）
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }
  
  avatarFile.value = file
  
  // 创建预览
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 移除头像
const removeAvatar = () => {
  avatarFile.value = null
  avatarPreview.value = ''
  registerData.avatar = ''
  if (avatarUpload.value) {
    avatarUpload.value.value = ''
  }
}

// 上传头像到服务器
const uploadAvatar = async () => {
  if (!avatarFile.value) return null
  
  try {
    uploading.value = true
    const formData = new FormData()
    formData.append('file', avatarFile.value)
    formData.append('description', '用户头像')
    formData.append('category', 'AVATAR')
    
    const response = await uploadApi.upload('/media/upload', formData)
    return response.uri || response.url
  } catch (error) {
    ElMessage.error('头像上传失败: ' + (error.message || '未知错误'))
    throw error
  } finally {
    uploading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value) return
  
  try {
    await registerForm.value.validate()
    loading.value = true
    
    console.log('🔐 开始加密注册数据...')
    
    // 准备注册数据，排除确认密码
    const { confirmPassword, password, ...baseRegistrationData } = registerData
    
    // 如果有头像文件，先上传
    if (avatarFile.value) {
      try {
        const avatarUrl = await uploadAvatar()
        baseRegistrationData.avatar = avatarUrl
      } catch (error) {
        // 头像上传失败，但不阻止注册
        ElMessage.warning('头像上传失败，将使用默认头像')
        delete baseRegistrationData.avatar
      }
    } else {
      // 如果没有头像文件，则不传递avatar字段
      delete baseRegistrationData.avatar
    }
    
    // 生成加密的注册数据
    const securePasswordData = CryptoUtils.generateSecureLoginData(
      baseRegistrationData.username,
      password
    )
    
    // 组合最终的注册数据
    const finalRegistrationData = {
      ...baseRegistrationData,
      password: securePasswordData.password, // 使用加密密码
      confirmPassword: securePasswordData.password, // 确认密码也使用加密
      timestamp: securePasswordData.timestamp,
      encryptionType: securePasswordData.encryptionType
    }
    
    console.log('📦 加密后的注册数据:', {
      username: finalRegistrationData.username,
      email: finalRegistrationData.email,
      passwordLength: finalRegistrationData.password.length,
      encryptionType: finalRegistrationData.encryptionType,
      timestamp: finalRegistrationData.timestamp
    })
    
    await authStore.register(finalRegistrationData)
    
    ElMessage.success('注册成功！请登录')
    
    // 跳转到登录页面
    router.push('/login')
  } catch (error) {
    console.error('❌ 注册失败:', error)
    if (error.message) {
      ElMessage.error(error.message)
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (registerForm.value) {
    registerForm.value.resetFields()
  }
  removeAvatar() // 同时清除头像
}

// 暴露方法给父组件（如果需要）
defineExpose({
  resetForm
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-box {
  background: white;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 480px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
}

.register-header h1 {
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.register-header p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

.register-subtitle {
  margin-top: 20px;
  margin-bottom: 10px;
}

.register-subtitle h2 {
  color: #34495e;
  font-size: 20px;
  font-weight: 500;
  margin: 0;
}

.register-form {
  margin-bottom: 20px;
}

.register-form .el-form-item {
  margin-bottom: 20px;
}

.register-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 500;
}

.login-link {
  text-align: center;
  color: #7f8c8d;
  font-size: 14px;
}

.login-link .link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
}

.login-link .link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.register-footer {
  text-align: center;
  color: #95a5a6;
  font-size: 12px;
}

.register-footer p {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    padding: 10px;
  }
  
  .register-box {
    padding: 30px 20px;
    max-width: 100%;
  }
  
  .register-header h1 {
    font-size: 20px;
  }
  
  .register-subtitle h2 {
    font-size: 18px;
  }
}

/* 表单验证错误样式优化 */
.el-form-item.is-error .el-input__inner {
  border-color: #f56c6c;
}

.el-form-item.is-error .el-input__prefix {
  color: #f56c6c;
}

/* 加载状态样式 */
.register-btn.is-loading {
  pointer-events: none;
}

/* 输入框焦点样式 */
.el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 头像上传样式 */
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.avatar-preview:hover {
  border-color: #409EFF;
  background-color: #f5f7fa;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  font-size: 14px;
}

.avatar-uploader-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 12px;
  text-align: center;
}

.avatar-upload-tips {
  text-align: center;
}

.avatar-upload-tips p {
  color: #909399;
  font-size: 12px;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

/* 表单标签样式优化 */
.register-form .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

/* 自定义滚动条 */
.register-container::-webkit-scrollbar {
  width: 8px;
}

.register-container::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.register-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

.register-container::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.5);
}
</style>

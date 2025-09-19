<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-illustration">
        <div class="error-number">404</div>
        <div class="error-icon">
          <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
            <circle cx="50" cy="40" r="3" fill="#409EFF" />
            <circle cx="50" cy="55" r="3" fill="#409EFF" />
            <path d="M25 75 Q50 90 75 75" stroke="#409EFF" stroke-width="3" fill="none" stroke-linecap="round" />
          </svg>
        </div>
      </div>
      
      <div class="error-message">
        <h1>页面未找到</h1>
        <p>抱歉，您访问的页面不存在或已被移除</p>
        <div class="error-details">
          <p>可能的原因：</p>
          <ul>
            <li>页面地址输入错误</li>
            <li>页面已被移动或删除</li>
            <li>您没有访问该页面的权限</li>
          </ul>
        </div>
      </div>
      
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
        <el-button size="large" @click="refresh">
          <el-icon><Refresh /></el-icon>
          刷新页面
        </el-button>
      </div>
      
      <div class="help-info">
        <p>如果问题持续存在，请联系系统管理员</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { House, ArrowLeft, Refresh } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}

const refresh = () => {
  window.location.reload()
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.not-found-content {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  padding: 60px 40px;
  text-align: center;
  max-width: 600px;
  width: 100%;
}

.error-illustration {
  margin-bottom: 40px;
  position: relative;
}

.error-number {
  font-size: 120px;
  font-weight: 700;
  color: #409EFF;
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 0 4px 20px rgba(64, 158, 255, 0.3);
  background: linear-gradient(45deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.error-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  opacity: 0.8;
}

.error-icon svg {
  width: 100%;
  height: 100%;
}

.error-message {
  margin-bottom: 40px;
}

.error-message h1 {
  font-size: 32px;
  color: #2c3e50;
  margin-bottom: 16px;
  font-weight: 600;
}

.error-message > p {
  font-size: 18px;
  color: #7f8c8d;
  margin-bottom: 24px;
  line-height: 1.6;
}

.error-details {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  margin: 24px 0;
  text-align: left;
}

.error-details p {
  font-size: 16px;
  color: #5a6c7d;
  margin-bottom: 12px;
  font-weight: 500;
}

.error-details ul {
  margin: 0;
  padding-left: 20px;
}

.error-details li {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 8px;
  line-height: 1.5;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.error-actions .el-button {
  min-width: 120px;
  height: 45px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.error-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.help-info {
  font-size: 14px;
  color: #95a5a6;
  padding-top: 20px;
  border-top: 1px solid #ecf0f1;
}

.help-info p {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .not-found-container {
    padding: 20px 10px;
  }
  
  .not-found-content {
    padding: 40px 20px;
  }
  
  .error-number {
    font-size: 80px;
  }
  
  .error-message h1 {
    font-size: 24px;
  }
  
  .error-message > p {
    font-size: 16px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .error-actions .el-button {
    width: 100%;
    max-width: 240px;
  }
}

@media (max-width: 480px) {
  .error-number {
    font-size: 60px;
  }
  
  .error-message h1 {
    font-size: 20px;
  }
  
  .error-details {
    padding: 16px;
    text-align: center;
  }
  
  .error-details ul {
    padding-left: 0;
    list-style: none;
  }
  
  .error-details li {
    margin-bottom: 6px;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.not-found-content {
  animation: fadeInUp 0.6s ease-out;
}

.error-number {
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.error-message {
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.error-actions {
  animation: fadeInUp 0.8s ease-out 0.6s both;
}
</style>

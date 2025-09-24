<template>
  <div class="rsa-test-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>RSA+AES加密测试</h2>
      <p>测试RSA和AES加密解密功能</p>
    </div>

    <!-- 功能开发中提示 -->
    <div class="development-notice">
      <el-result
        icon="warning"
        title="功能开发中"
        sub-title="RSA+AES加密测试功能正在开发中，敬请期待"
      >
        <template #extra>
          <el-button type="primary" @click="goBack">返回首页</el-button>
        </template>
      </el-result>
    </div>

    <!-- 加密测试说明 -->
    <div class="encryption-info">
      <el-card header="加密方案说明">
        <el-row :gutter="20">
          <el-col :span="12">
            <h4>RSA加密</h4>
            <p>用于加密AES密钥，确保密钥传输安全</p>
            <ul>
              <li>非对称加密算法</li>
              <li>适用于小数据量加密</li>
              <li>密钥长度通常为2048位或更高</li>
              <li>计算复杂度较高</li>
            </ul>
          </el-col>
          <el-col :span="12">
            <h4>AES加密</h4>
            <p>用于加密实际数据，提供高效的数据保护</p>
            <ul>
              <li>对称加密算法</li>
              <li>适用于大数据量加密</li>
              <li>密钥长度支持128、192、256位</li>
              <li>加密速度快，安全性高</li>
            </ul>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 测试功能预览 -->
    <div class="test-features">
      <el-card header="测试功能">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="test-item">
              <h5>密钥生成测试</h5>
              <p>测试RSA密钥对和AES密钥的生成</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="test-item">
              <h5>数据加密测试</h5>
              <p>测试文本和文件的加密功能</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="test-item">
              <h5>数据解密测试</h5>
              <p>测试加密数据的解密和验证</p>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="8">
            <div class="test-item">
              <h5>性能测试</h5>
              <p>测试加解密操作的性能表现</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="test-item">
              <h5>兼容性测试</h5>
              <p>测试与后端加密的兼容性</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="test-item">
              <h5>安全性验证</h5>
              <p>验证加密强度和安全性</p>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.rsa-test-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.development-notice {
  margin-bottom: 30px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.encryption-info {
  margin-bottom: 30px;
}

.encryption-info h4 {
  color: #303133;
  margin-bottom: 12px;
}

.encryption-info p {
  color: #606266;
  margin-bottom: 16px;
}

.encryption-info ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.encryption-info li {
  padding: 4px 0;
  color: #909399;
  font-size: 14px;
}

.encryption-info li:before {
  content: "•";
  color: #409eff;
  font-weight: bold;
  margin-right: 8px;
}

.test-features {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.test-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  height: 120px;
}

.test-item h5 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.test-item p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}
</style>

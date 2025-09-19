<template>
  <div class="listening-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>听力理解管理</span>
          <el-button type="primary">
            <el-icon><Plus /></el-icon>
            添加对话
          </el-button>
        </div>
      </template>
      
      <div class="coming-soon">
        <el-icon size="60" color="#dcdfe6"><Headset /></el-icon>
        <h3>听力理解管理</h3>
        <p>此模块正在开发中，敬请期待...</p>
        <p>功能包括：对话管理、问题管理、回答统计等</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { Plus, Headset } from '@element-plus/icons-vue'
</script>

<style scoped>
.listening-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coming-soon {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.coming-soon h3 {
  margin: 20px 0 10px 0;
  color: #606266;
}

.coming-soon p {
  margin: 5px 0;
  font-size: 14px;
}
</style>

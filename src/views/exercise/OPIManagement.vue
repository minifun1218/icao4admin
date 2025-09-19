<template>
  <div class="opi-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>口语模仿管理</span>
          <el-button type="primary">
            <el-icon><Plus /></el-icon>
            添加题目
          </el-button>
        </div>
      </template>
      
      <div class="coming-soon">
        <el-icon size="60" color="#dcdfe6"><Microphone /></el-icon>
        <h3>口语模仿管理</h3>
        <p>此模块正在开发中，敬请期待...</p>
        <p>功能包括：OPI题目管理、音频管理、评估标准设置等</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { Plus, Microphone } from '@element-plus/icons-vue'
</script>

<style scoped>
.opi-management {
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

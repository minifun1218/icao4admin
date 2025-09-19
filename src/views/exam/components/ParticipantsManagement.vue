<template>
  <div class="participants-management">
    <div class="coming-soon">
      <el-icon size="60" color="#dcdfe6"><User /></el-icon>
      <h3>参与者管理</h3>
      <p>此模块正在开发中，敬请期待...</p>
      <p>功能包括：参与者添加、名单导入、权限设置、通知发送等</p>
    </div>
  </div>
</template>

<script setup>
import { User } from '@element-plus/icons-vue'

// Props
defineProps({
  exam: {
    type: Object,
    default: () => ({})
  }
})

// Emits
defineEmits(['update'])
</script>

<style scoped>
.participants-management {
  padding: 0;
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

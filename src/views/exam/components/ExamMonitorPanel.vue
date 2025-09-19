<template>
  <div class="exam-monitor-panel">
    <div class="coming-soon">
      <el-icon size="60" color="#dcdfe6"><Monitor /></el-icon>
      <h3>考试监控</h3>
      <p>此模块正在开发中，敬请期待...</p>
      <p>功能包括：实时监控、强制提交、进度查看、异常检测等</p>
    </div>
  </div>
</template>

<script setup>
import { Monitor } from '@element-plus/icons-vue'

// Props
defineProps({
  ongoingExams: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
defineEmits(['view-real-time', 'force-submit', 'view-progress'])
</script>

<style scoped>
.exam-monitor-panel {
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

<template>
  <div class="exam-result-panel">
    <div class="coming-soon">
      <el-icon size="60" color="#dcdfe6"><TrendCharts /></el-icon>
      <h3>考试结果管理</h3>
      <p>此模块正在开发中，敬请期待...</p>
      <p>功能包括：成绩查看、统计分析、重新评分、结果导出等</p>
    </div>
  </div>
</template>

<script setup>
import { TrendCharts } from '@element-plus/icons-vue'

// Props
defineProps({
  results: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
defineEmits(['view-detail', 're-score', 'export', 'view-statistics'])
</script>

<style scoped>
.exam-result-panel {
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

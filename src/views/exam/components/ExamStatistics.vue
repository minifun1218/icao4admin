<template>
  <div class="exam-statistics">
    <div class="coming-soon">
      <el-icon size="60" color="#dcdfe6"><DataAnalysis /></el-icon>
      <h3>考试统计分析</h3>
      <p>此模块正在开发中，敬请期待...</p>
      <p>功能包括：成绩分布、题目分析、时间统计、通过率分析等</p>
    </div>
  </div>
</template>

<script setup>
import { DataAnalysis } from '@element-plus/icons-vue'

// Props
defineProps({
  exam: {
    type: Object,
    default: () => ({})
  }
})
</script>

<style scoped>
.exam-statistics {
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

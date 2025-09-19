<template>
  <div class="banner-statistics">
    <div class="coming-soon">
      <el-icon size="60" color="#dcdfe6"><DataAnalysis /></el-icon>
      <h3>轮播图统计分析</h3>
      <p>此模块正在开发中，敬请期待...</p>
      <p>功能包括：点击统计、展示统计、转化率分析、热力图等</p>
      
      <div class="feature-preview">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="feature-card">
              <el-icon size="32" color="#409EFF"><TrendCharts /></el-icon>
              <h4>点击统计</h4>
              <p>统计轮播图的点击次数和点击率</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="feature-card">
              <el-icon size="32" color="#67C23A"><View /></el-icon>
              <h4>展示统计</h4>
              <p>分析轮播图的展示次数和展示效果</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="feature-card">
              <el-icon size="32" color="#E6A23C"><Histogram /></el-icon>
              <h4>转化分析</h4>
              <p>评估轮播图的转化率和用户行为</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { DataAnalysis, TrendCharts, View, Histogram } from '@element-plus/icons-vue'

// Props
defineProps({
  clickStats: {
    type: Array,
    default: () => []
  },
  viewStats: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.banner-statistics {
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
  font-size: 24px;
}

.coming-soon p {
  margin: 5px 0;
  font-size: 16px;
}

.feature-preview {
  margin-top: 40px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.feature-card h4 {
  margin: 16px 0 8px 0;
  color: #303133;
  font-size: 18px;
}

.feature-card p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .feature-preview .el-col {
    margin-bottom: 20px;
  }
  
  .feature-card {
    padding: 20px 15px;
  }
  
  .feature-card h4 {
    font-size: 16px;
  }
  
  .feature-card p {
    font-size: 13px;
  }
}
</style>

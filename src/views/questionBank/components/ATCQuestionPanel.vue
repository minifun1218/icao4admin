<template>
  <div class="atc-question-panel">
    <!-- 标签页：ATC题目和场景管理 -->
    <el-tabs v-model="activeSubTab" class="atc-tabs">
      <!-- ATC题目管理 -->
      <el-tab-pane label="ATC题目" name="questions">
        <div class="panel-header">
          <div class="search-section">
            <el-form :model="questionSearchForm" inline class="search-form">
              <el-form-item>
                <el-input
                  v-model="questionSearchForm.keyword"
                  placeholder="搜索题目内容"
                  clearable
                  @clear="handleQuestionSearch"
                  @keyup.enter="handleQuestionSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-select v-model="questionSearchForm.scenario" placeholder="选择场景" clearable>
                  <el-option
                    v-for="scenario in scenarios"
                    :key="scenario.id"
                    :label="scenario.name"
                    :value="scenario.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="questionSearchForm.difficulty" placeholder="难度等级" clearable>
                  <el-option
                    v-for="level in difficultyLevels"
                    :key="level.value"
                    :label="level.label"
                    :value="level.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleQuestionSearch" :loading="loading">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="resetQuestionSearch">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="action-section">
            <el-button type="success" @click="$emit('add')">
              <el-icon><Plus /></el-icon>
              添加ATC题目
            </el-button>
            <el-button type="danger" @click="batchDeleteQuestions" :disabled="!hasQuestionSelection">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>

        <!-- ATC题目列表 -->
        <el-table
          v-loading="loading"
          :data="filteredQuestions"
          @selection-change="handleQuestionSelectionChange"
          stripe
          class="atc-table"
          empty-text="暂无ATC题目"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column label="题目内容" min-width="300">
            <template #default="{ row }">
              <div class="question-content">
                <div class="content-text">{{ row.content }}</div>
                <div class="content-meta">
                  <el-tag 
                    v-if="row.scenario" 
                    size="small" 
                    :type="getScenarioTagType(row.scenario.type)"
                  >
                    <el-icon><component :is="getScenarioIcon(row.scenario.type)" /></el-icon>
                    {{ row.scenario.name }}
                  </el-tag>
                  <el-tag v-if="row.audioUrl" size="small" type="info">
                    <el-icon><Headset /></el-icon>
                    音频
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="场景类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                v-if="row.scenario"
                size="small"
                :type="getScenarioTagType(row.scenario.type)"
              >
                {{ getScenarioTypeLabel(row.scenario.type) }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="难度" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :color="getDifficultyColor(row.difficultyLevel)"
                size="small"
                effect="light"
              >
                {{ getDifficultyLabel(row.difficultyLevel) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="分值" width="80" align="center">
            <template #default="{ row }">
              <span class="points-text">{{ row.points }}</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.isActive"
                @change="handleQuestionStatusChange(row)"
                size="small"
              />
            </template>
          </el-table-column>

          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="$emit('edit', row)"
                  link
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button 
                  type="info" 
                  size="small" 
                  @click="previewATCQuestion(row)"
                  link
                >
                  <el-icon><View /></el-icon>
                  预览
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="$emit('delete', row)"
                  link
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="questionPagination.page"
            v-model:page-size="questionPagination.size"
            :total="questionPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleQuestionSizeChange"
            @current-change="handleQuestionCurrentChange"
          />
        </div>
      </el-tab-pane>

      <!-- ATC场景管理 -->
      <el-tab-pane label="场景管理" name="scenarios">
        <div class="panel-header">
          <div class="search-section">
            <el-form :model="scenarioSearchForm" inline class="search-form">
              <el-form-item>
                <el-input
                  v-model="scenarioSearchForm.keyword"
                  placeholder="搜索场景名称"
                  clearable
                  @clear="handleScenarioSearch"
                  @keyup.enter="handleScenarioSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-select v-model="scenarioSearchForm.type" placeholder="场景类型" clearable>
                  <el-option
                    v-for="type in scenarioTypes"
                    :key="type.value"
                    :label="type.label"
                    :value="type.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleScenarioSearch" :loading="loading">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="resetScenarioSearch">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="action-section">
            <el-button type="success" @click="$emit('add-scenario')">
              <el-icon><Plus /></el-icon>
              添加场景
            </el-button>
            <el-button type="danger" @click="batchDeleteScenarios" :disabled="!hasScenarioSelection">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>

        <!-- 场景卡片列表 -->
        <div class="scenarios-grid">
          <el-row :gutter="20">
            <el-col :span="8" v-for="scenario in filteredScenarios" :key="scenario.id">
              <el-card class="scenario-card" shadow="hover">
                <template #header>
                  <div class="scenario-header">
                    <div class="scenario-title">
                      <el-icon :color="getScenarioColor(scenario.type)">
                        <component :is="getScenarioIcon(scenario.type)" />
                      </el-icon>
                      <span>{{ scenario.name }}</span>
                    </div>
                    <el-checkbox 
                      v-model="selectedScenarios"
                      :label="scenario.id"
                      @change="handleScenarioSelectionChange"
                    />
                  </div>
                </template>
                
                <div class="scenario-content">
                  <div class="scenario-type">
                    <el-tag :type="getScenarioTagType(scenario.type)" size="small">
                      {{ getScenarioTypeLabel(scenario.type) }}
                    </el-tag>
                  </div>
                  
                  <div class="scenario-description">
                    {{ scenario.description }}
                  </div>
                  
                  <div class="scenario-stats">
                    <div class="stat-item">
                      <span class="stat-label">题目数量:</span>
                      <span class="stat-value">{{ scenario.questionCount || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="stat-label">创建时间:</span>
                      <span class="stat-value">{{ formatDate(scenario.createdAt) }}</span>
                    </div>
                  </div>
                </div>
                
                <template #footer>
                  <div class="scenario-actions">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="$emit('edit-scenario', scenario)"
                    >
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button 
                      type="info" 
                      size="small" 
                      @click="viewScenarioQuestions(scenario)"
                    >
                      <el-icon><View /></el-icon>
                      查看题目
                    </el-button>
                    <el-button 
                      type="danger" 
                      size="small" 
                      @click="$emit('delete-scenario', scenario)"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </template>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 场景分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="scenarioPagination.page"
            v-model:page-size="scenarioPagination.size"
            :total="scenarioPagination.total"
            :page-sizes="[6, 12, 18, 24]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleScenarioSizeChange"
            @current-change="handleScenarioCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ATC题目预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="ATC题目预览"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="previewQuestionData" class="atc-preview-content">
        <div class="preview-header">
          <h3>{{ previewQuestionData.content }}</h3>
          <div class="preview-meta">
            <el-tag 
              v-if="previewQuestionData.scenario"
              :type="getScenarioTagType(previewQuestionData.scenario.type)"
              size="small"
            >
              <el-icon><component :is="getScenarioIcon(previewQuestionData.scenario.type)" /></el-icon>
              {{ previewQuestionData.scenario.name }}
            </el-tag>
            <el-tag :color="getDifficultyColor(previewQuestionData.difficultyLevel)" size="small">
              {{ getDifficultyLabel(previewQuestionData.difficultyLevel) }}
            </el-tag>
            <span class="points">{{ previewQuestionData.points }}分</span>
          </div>
        </div>

        <!-- 场景信息 -->
        <div v-if="previewQuestionData.scenario" class="scenario-info">
          <h4>场景信息</h4>
          <div class="scenario-details">
            <div class="detail-item">
              <span class="label">场景名称:</span>
              <span class="value">{{ previewQuestionData.scenario.name }}</span>
            </div>
            <div class="detail-item">
              <span class="label">场景类型:</span>
              <span class="value">{{ getScenarioTypeLabel(previewQuestionData.scenario.type) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">场景描述:</span>
              <span class="value">{{ previewQuestionData.scenario.description }}</span>
            </div>
          </div>
        </div>

        <!-- 音频播放 -->
        <div v-if="previewQuestionData.audioUrl" class="audio-section">
          <h4>通讯音频</h4>
          <audio :src="previewQuestionData.audioUrl" controls class="audio-player"></audio>
        </div>

        <!-- ATC通讯内容 -->
        <div v-if="previewQuestionData.communicationContent" class="communication-section">
          <h4>通讯内容</h4>
          <div class="communication-content">
            {{ previewQuestionData.communicationContent }}
          </div>
        </div>

        <!-- 评分标准 -->
        <div v-if="previewQuestionData.scoringCriteria" class="scoring-section">
          <h4>评分标准</h4>
          <div class="scoring-criteria">
            <div 
              v-for="(criterion, index) in previewQuestionData.scoringCriteria" 
              :key="index"
              class="criterion-item"
            >
              <span class="criterion-name">{{ criterion.name }}</span>
              <span class="criterion-weight">权重: {{ criterion.weight }}%</span>
              <div class="criterion-description">{{ criterion.description }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionBankApi } from '@/api/questionBank'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  View,
  Headset,
  Top,
  Bottom,
  MapLocation,
  OfficeBuilding,
  Warning,
  Cloudy
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  questions: {
    type: Array,
    default: () => []
  },
  scenarios: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits([
  'add', 'edit', 'delete', 'selection-change',
  'add-scenario', 'edit-scenario', 'delete-scenario'
])

// 响应式数据
const activeSubTab = ref('questions')
const previewDialogVisible = ref(false)
const previewQuestionData = ref(null)

const selectedQuestions = ref([])
const selectedScenarios = ref([])

// 搜索表单
const questionSearchForm = reactive({
  keyword: '',
  scenario: '',
  difficulty: ''
})

const scenarioSearchForm = reactive({
  keyword: '',
  type: ''
})

// 分页数据
const questionPagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const scenarioPagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

// 计算属性
const difficultyLevels = computed(() => questionBankApi.getDifficultyLevels())
const scenarioTypes = computed(() => questionBankApi.getATCScenarioTypes())

const filteredQuestions = computed(() => {
  let filtered = props.questions

  // 关键词搜索
  if (questionSearchForm.keyword) {
    const keyword = questionSearchForm.keyword.toLowerCase()
    filtered = filtered.filter(q => 
      q.content.toLowerCase().includes(keyword)
    )
  }

  // 场景筛选
  if (questionSearchForm.scenario) {
    filtered = filtered.filter(q => q.scenarioId === questionSearchForm.scenario)
  }

  // 难度筛选
  if (questionSearchForm.difficulty !== '') {
    filtered = filtered.filter(q => q.difficultyLevel === questionSearchForm.difficulty)
  }

  questionPagination.total = filtered.length

  const start = (questionPagination.page - 1) * questionPagination.size
  const end = start + questionPagination.size
  return filtered.slice(start, end)
})

const filteredScenarios = computed(() => {
  let filtered = props.scenarios

  // 关键词搜索
  if (scenarioSearchForm.keyword) {
    const keyword = scenarioSearchForm.keyword.toLowerCase()
    filtered = filtered.filter(s => 
      s.name.toLowerCase().includes(keyword) ||
      s.description.toLowerCase().includes(keyword)
    )
  }

  // 类型筛选
  if (scenarioSearchForm.type) {
    filtered = filtered.filter(s => s.type === scenarioSearchForm.type)
  }

  scenarioPagination.total = filtered.length

  const start = (scenarioPagination.page - 1) * scenarioPagination.size
  const end = start + scenarioPagination.size
  return filtered.slice(start, end)
})

const hasQuestionSelection = computed(() => selectedQuestions.value.length > 0)
const hasScenarioSelection = computed(() => selectedScenarios.value.length > 0)

// 方法
const getDifficultyLabel = (level) => {
  return questionBankApi.formatDifficultyLevel(level)
}

const getDifficultyColor = (level) => {
  const colors = {
    1: '#67C23A',
    2: '#95D475', 
    3: '#E6A23C',
    4: '#F78989',
    5: '#F56C6C',
    6: '#909399'
  }
  return colors[level] || '#909399'
}

const getScenarioTypeLabel = (type) => {
  const typeLabels = {
    departure: '离场',
    approach: '进场',
    ground: '地面',
    tower: '塔台',
    emergency: '紧急情况',
    weather: '天气相关'
  }
  return typeLabels[type] || type
}

const getScenarioTagType = (type) => {
  const tagTypes = {
    departure: 'success',
    approach: 'primary',
    ground: 'info',
    tower: 'warning',
    emergency: 'danger',
    weather: ''
  }
  return tagTypes[type] || 'info'
}

const getScenarioIcon = (type) => {
  const icons = {
    departure: 'Top',
    approach: 'Bottom',
    ground: 'MapLocation',
    tower: 'OfficeBuilding',
    emergency: 'Warning',
    weather: 'Cloudy'
  }
  return icons[type] || 'OfficeBuilding'
}

const getScenarioColor = (type) => {
  const colors = {
    departure: '#67C23A',
    approach: '#409EFF',
    ground: '#909399',
    tower: '#E6A23C',
    emergency: '#F56C6C',
    weather: '#409EFF'
  }
  return colors[type] || '#909399'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 题目相关方法
const handleQuestionSearch = () => {
  questionPagination.page = 1
}

const resetQuestionSearch = () => {
  Object.assign(questionSearchForm, {
    keyword: '',
    scenario: '',
    difficulty: ''
  })
  questionPagination.page = 1
}

const handleQuestionSelectionChange = (selection) => {
  selectedQuestions.value = selection
  emit('selection-change', selection)
}

const handleQuestionStatusChange = async (question) => {
  try {
    ElMessage.success('状态更新成功')
  } catch (error) {
    question.isActive = !question.isActive
    ElMessage.error('状态更新失败')
  }
}

const handleQuestionSizeChange = (size) => {
  questionPagination.size = size
  questionPagination.page = 1
}

const handleQuestionCurrentChange = (page) => {
  questionPagination.page = page
}

const previewATCQuestion = (question) => {
  previewQuestionData.value = question
  previewDialogVisible.value = true
}

const batchDeleteQuestions = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要删除的题目')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedQuestions.value.length} 道ATC题目吗？`,
      '确认批量删除',
      { type: 'warning' }
    )
    
    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 场景相关方法
const handleScenarioSearch = () => {
  scenarioPagination.page = 1
}

const resetScenarioSearch = () => {
  Object.assign(scenarioSearchForm, {
    keyword: '',
    type: ''
  })
  scenarioPagination.page = 1
}

const handleScenarioSelectionChange = () => {
  // 场景选择变化处理
}

const handleScenarioSizeChange = (size) => {
  scenarioPagination.size = size
  scenarioPagination.page = 1
}

const handleScenarioCurrentChange = (page) => {
  scenarioPagination.page = page
}

const viewScenarioQuestions = (scenario) => {
  // 切换到题目标签页并筛选该场景的题目
  activeSubTab.value = 'questions'
  questionSearchForm.scenario = scenario.id
  handleQuestionSearch()
}

const batchDeleteScenarios = async () => {
  if (selectedScenarios.value.length === 0) {
    ElMessage.warning('请选择要删除的场景')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedScenarios.value.length} 个场景吗？`,
      '确认批量删除',
      { type: 'warning' }
    )
    
    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}
</script>

<style scoped>
.atc-question-panel {
  padding: 0;
}

.atc-tabs {
  margin-top: 0;
}

.panel-header {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.search-section {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.action-section {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.atc-table {
  border-radius: 8px;
  overflow: hidden;
}

.question-content {
  padding: 8px 0;
}

.content-text {
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.content-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.points-text {
  font-weight: 600;
  color: #E6A23C;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

/* 场景卡片样式 */
.scenarios-grid {
  margin-bottom: 20px;
}

.scenario-card {
  margin-bottom: 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.scenario-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.scenario-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scenario-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.scenario-content {
  padding: 16px 0;
}

.scenario-type {
  margin-bottom: 12px;
}

.scenario-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.scenario-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.stat-label {
  color: #909399;
}

.stat-value {
  color: #303133;
  font-weight: 500;
}

.scenario-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 预览对话框样式 */
.atc-preview-content {
  max-height: 70vh;
  overflow-y: auto;
}

.preview-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.preview-header h3 {
  margin: 0 0 12px 0;
  color: #303133;
  line-height: 1.5;
}

.preview-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.preview-meta .points {
  font-weight: 600;
  color: #E6A23C;
}

.scenario-info,
.audio-section,
.communication-section,
.scoring-section {
  margin-bottom: 20px;
}

.scenario-info h4,
.audio-section h4,
.communication-section h4,
.scoring-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.scenario-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.detail-item {
  display: flex;
  margin-bottom: 8px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item .label {
  min-width: 80px;
  color: #909399;
  font-weight: 500;
}

.detail-item .value {
  color: #303133;
  flex: 1;
}

.audio-player {
  width: 100%;
  margin-top: 8px;
}

.communication-content {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  color: #303133;
  line-height: 1.6;
  white-space: pre-line;
}

.scoring-criteria {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.criterion-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.criterion-item .criterion-name {
  font-weight: 600;
  color: #303133;
  margin-right: 12px;
}

.criterion-item .criterion-weight {
  color: #E6A23C;
  font-size: 12px;
}

.criterion-item .criterion-description {
  color: #606266;
  margin-top: 8px;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .action-section {
    flex-direction: column;
  }
  
  .scenarios-grid .el-col {
    margin-bottom: 16px;
  }
  
  .scenario-actions {
    flex-direction: column;
  }
  
  .preview-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .detail-item {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-item .label {
    min-width: auto;
  }
}
</style>

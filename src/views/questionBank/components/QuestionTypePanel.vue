<template>
  <div class="question-type-panel">
    <!-- 操作栏 -->
    <div class="panel-header">
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索题目内容"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.difficulty" placeholder="难度等级" clearable>
              <el-option
                v-for="level in difficultyLevels"
                :key="level.value"
                :label="level.label"
                :value="level.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.status" placeholder="状态" clearable>
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="action-section">
        <el-button type="success" @click="$emit('add', type)">
          <el-icon><Plus /></el-icon>
          添加{{ getTypeLabel() }}
        </el-button>
        <el-button type="warning" @click="batchEdit" :disabled="!hasSelection">
          <el-icon><Edit /></el-icon>
          批量编辑
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="!hasSelection">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="question-list">
      <el-table
        v-loading="loading"
        :data="filteredQuestions"
        @selection-change="handleSelectionChange"
        stripe
        class="question-table"
        empty-text="暂无题目数据"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="题目内容" min-width="300">
          <template #default="{ row }">
            <div class="question-content">
              <div class="content-text">{{ row.content }}</div>
              <div class="content-meta">
                <el-tag v-if="row.audioUrl" size="small" type="info">
                  <el-icon><Service /></el-icon>
                  音频
                </el-tag>
                <el-tag 
                  v-for="tag in row.tags" 
                  :key="tag" 
                  size="small" 
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
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

        <el-table-column label="预计用时" width="100" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.estimatedTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="handleStatusChange(row)"
              size="small"
            />
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" :width="type === 'listening_comprehension' ? 300 : 200" fixed="right">
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
              
              <!-- 听力理解特殊操作 -->
              <el-button 
                v-if="type === 'listening_comprehension'" 
                type="success" 
                size="small" 
                @click="manageQuestions(row)"
                link
              >
                <el-icon><Plus /></el-icon>
                题干管理
              </el-button>
              
              <el-button 
                type="info" 
                size="small" 
                @click="previewQuestion(row)"
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
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="题目预览"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="previewQuestion" class="preview-content">
        <div class="preview-header">
          <h3>{{ previewQuestion.content }}</h3>
          <div class="preview-meta">
            <el-tag :color="getDifficultyColor(previewQuestion.difficultyLevel)" size="small">
              {{ getDifficultyLabel(previewQuestion.difficultyLevel) }}
            </el-tag>
            <span class="points">{{ previewQuestion.points }}分</span>
            <span class="time">{{ formatTime(previewQuestion.estimatedTime) }}</span>
          </div>
        </div>

        <!-- 音频播放 -->
        <div v-if="previewQuestion.audioUrl" class="audio-section">
          <h4>音频内容</h4>
          <audio :src="previewQuestion.audioUrl" controls class="audio-player"></audio>
        </div>

        <!-- 选择题选项 -->
        <div v-if="previewQuestion.choices && previewQuestion.choices.length" class="choices-section">
          <h4>选项</h4>
          <div class="choices-list">
            <div 
              v-for="(choice, index) in previewQuestion.choices" 
              :key="index"
              class="choice-item"
              :class="{ 'correct-choice': index === previewQuestion.correctAnswer }"
            >
              <span class="choice-label">{{ String.fromCharCode(65 + index) }}.</span>
              <span class="choice-text">{{ choice.text }}</span>
              <el-icon v-if="index === previewQuestion.correctAnswer" class="correct-icon" color="#67C23A">
                <Check />
              </el-icon>
            </div>
          </div>
        </div>

        <!-- 标签 -->
        <div v-if="previewQuestion.tags && previewQuestion.tags.length" class="tags-section">
          <h4>标签</h4>
          <el-tag 
            v-for="tag in previewQuestion.tags" 
            :key="tag" 
            size="small"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
        </div>

        <!-- 备注 -->
        <div v-if="previewQuestion.remark" class="remark-section">
          <h4>备注</h4>
          <p>{{ previewQuestion.remark }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionBankApi } from '@/api/questionBank'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  View,
  Service,
  Check
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  type: {
    type: String,
    required: true
  },
  questions: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['add', 'edit', 'delete', 'selection-change', 'manage-questions'])

// 响应式数据
const previewDialogVisible = ref(false)
const previewQuestionData = ref(null)
const selectedQuestions = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  difficulty: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 计算属性
const difficultyLevels = computed(() => questionBankApi.getDifficultyLevels())

const filteredQuestions = computed(() => {
  let filtered = props.questions

  // 关键词搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filtered = filtered.filter(q => 
      q.content.toLowerCase().includes(keyword) ||
      (q.tags && q.tags.some(tag => tag.toLowerCase().includes(keyword)))
    )
  }

  // 难度筛选
  if (searchForm.difficulty !== '') {
    filtered = filtered.filter(q => q.difficultyLevel === searchForm.difficulty)
  }

  // 状态筛选
  if (searchForm.status !== '') {
    filtered = filtered.filter(q => q.isActive === searchForm.status)
  }

  // 更新分页总数
  pagination.total = filtered.length

  // 分页
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filtered.slice(start, end)
})

const hasSelection = computed(() => selectedQuestions.value.length > 0)

// 方法
const getTypeLabel = () => {
  const typeLabels = {
    listening: '听力理解题',
    mcq: '听力选择题',
    opi: '口语模仿题',
    retell: '故事复述题'
  }
  return typeLabels[props.type] || '题目'
}

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

const formatTime = (seconds) => {
  if (!seconds) return '-'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return minutes > 0 ? `${minutes}分${remainingSeconds}秒` : `${remainingSeconds}秒`
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const handleSearch = () => {
  pagination.page = 1
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    difficulty: '',
    status: ''
  })
  pagination.page = 1
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
  emit('selection-change', selection)
}

const handleStatusChange = async (question) => {
  try {
    // 这里应该调用API更新状态
    // await questionBankApi.updateQuestionStatus(question.id, question.isActive)
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 恢复原状态
    question.isActive = !question.isActive
    ElMessage.error('状态更新失败')
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
}

const handleCurrentChange = (page) => {
  pagination.page = page
}

const previewQuestion = (question) => {
  previewQuestionData.value = question
  previewDialogVisible.value = true
}

const manageQuestions = (dialog) => {
  emit('manage-questions', dialog)
}

const batchEdit = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要编辑的题目')
    return
  }
  
  // 批量编辑逻辑
  ElMessage.info('批量编辑功能开发中...')
}

const batchDelete = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要删除的题目')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedQuestions.value.length} 道题目吗？`,
      '确认批量删除',
      { type: 'warning' }
    )
    
    // 这里应该调用批量删除API
    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 监听题目列表变化，重置分页
watch(() => props.questions, () => {
  pagination.page = 1
}, { deep: true })
</script>

<style scoped>
.question-type-panel {
  padding: 0;
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

.question-table {
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
  line-clamp: 2;
  overflow: hidden;
}

.content-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  margin: 0;
}

.points-text {
  font-weight: 600;
  color: #E6A23C;
}

.time-text {
  font-size: 12px;
  color: #909399;
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

/* 预览对话框样式 */
.preview-content {
  max-height: 60vh;
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

.preview-meta .time {
  color: #909399;
}

.audio-section,
.choices-section,
.tags-section,
.remark-section {
  margin-bottom: 20px;
}

.audio-section h4,
.choices-section h4,
.tags-section h4,
.remark-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.audio-player {
  width: 100%;
  margin-top: 8px;
}

.choices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.choice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.choice-item.correct-choice {
  background: rgba(103, 194, 58, 0.1);
  border: 1px solid #67C23A;
}

.choice-label {
  font-weight: 600;
  color: #606266;
  min-width: 20px;
}

.choice-text {
  flex: 1;
  color: #303133;
}

.correct-icon {
  margin-left: auto;
}

.remark-section p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .action-section {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .preview-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .choice-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .choice-label {
    min-width: auto;
  }
}
</style>

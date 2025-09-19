<template>
  <div class="mcq-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>听力选择题管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="createQuestion">
              <el-icon><Plus /></el-icon>
              添加题目
            </el-button>
            <el-button type="danger" @click="batchDelete" :disabled="!hasSelection">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="题目内容">
            <el-input
              v-model="searchForm.questionText"
              placeholder="搜索题目内容"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="题目类型">
            <el-select v-model="searchForm.questionType" placeholder="选择类型" clearable>
              <el-option
                v-for="type in questionTypes"
                :key="type"
                :label="getQuestionTypeLabel(type)"
                :value="type"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="难度等级">
            <el-select v-model="searchForm.difficultyLevel" placeholder="选择难度" clearable>
              <el-option
                v-for="level in difficultyLevels"
                :key="level.value"
                :label="level.label"
                :value="level.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.isActive" placeholder="选择状态" clearable>
              <el-option label="激活" :value="true" />
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
      
      <!-- 题目列表 -->
      <el-table
        v-loading="loading"
        :data="questionList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="questionText" label="题目内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题目类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getQuestionTypeType(row.questionType)">
              {{ getQuestionTypeLabel(row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficultyLevel" label="难度" width="100">
          <template #default="{ row }">
            <el-rate
              v-model="row.difficultyLevel"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column prop="points" label="分数" width="80" />
        <el-table-column label="音频" width="80">
          <template #default="{ row }">
            <el-button
              v-if="row.audioFilePath"
              type="text"
              @click="playAudio(row.audioFilePath)"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <span v-else class="text-gray">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="choiceCount" label="选项数" width="80" />
        <el-table-column prop="correctAnswerCount" label="正确答案数" width="100" />
        <el-table-column label="正确率" width="100">
          <template #default="{ row }">
            <span v-if="row.stats">{{ (row.stats.correctRate * 100).toFixed(1) }}%</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="viewStats(row)">
              <el-icon><DataAnalysis /></el-icon>
              统计
            </el-button>
            <el-button type="primary" size="small" @click="editQuestion(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteQuestion(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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
    </el-card>
    
    <!-- 编辑题目对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editForm.id ? '编辑题目' : '新增题目'"
      width="900px"
      top="5vh"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模块ID" prop="moduleId">
              <el-input-number v-model="editForm.moduleId" :min="1" placeholder="请输入模块ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="题目类型" prop="questionType">
              <el-select v-model="editForm.questionType" placeholder="选择题目类型">
                <el-option
                  v-for="type in questionTypes"
                  :key="type"
                  :label="getQuestionTypeLabel(type)"
                  :value="type"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="题目内容" prop="questionText">
          <el-input
            v-model="editForm.questionText"
            type="textarea"
            :rows="3"
            placeholder="请输入题目内容"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-rate
                v-model="editForm.difficultyLevel"
                :max="5"
                show-text
                text-color="#ff9900"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分数" prop="points">
              <el-input-number v-model="editForm.points" :min="0" :precision="1" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="题目序号" prop="questionOrder">
              <el-input-number v-model="editForm.questionOrder" :min="1" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="音频文件路径" prop="audioFilePath">
              <el-input v-model="editForm.audioFilePath" placeholder="请输入音频文件路径" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="音频时长(秒)" prop="audioDuration">
              <el-input-number v-model="editForm.audioDuration" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="是否激活" prop="isActive">
          <el-switch v-model="editForm.isActive" />
        </el-form-item>
        
        <!-- 选项列表 -->
        <el-divider>答案选项</el-divider>
        <div class="choices-section">
          <div v-for="(choice, index) in editForm.choices" :key="index" class="choice-item">
            <el-row :gutter="10" align="middle">
              <el-col :span="3">
                <el-form-item :label="`选项${choice.choiceKey}`">
                  <el-input v-model="choice.choiceKey" placeholder="A" maxlength="1" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="选项内容">
                  <el-input v-model="choice.choiceText" placeholder="请输入选项内容" />
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-form-item label="正确答案">
                  <el-checkbox v-model="choice.isCorrect" />
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-form-item label="序号">
                  <el-input-number v-model="choice.choiceOrder" :min="1" />
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-button type="danger" @click="removeChoice(index)" :disabled="editForm.choices.length <= 2">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-col>
            </el-row>
          </div>
          
          <el-button type="primary" @click="addChoice" :disabled="editForm.choices.length >= 6">
            <el-icon><Plus /></el-icon>
            添加选项
          </el-button>
        </div>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 统计信息对话框 -->
    <el-dialog
      v-model="statsDialogVisible"
      title="题目统计"
      width="600px"
    >
      <div v-if="currentStats" class="stats-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-statistic title="总回答数" :value="currentStats.totalResponses" />
          </el-col>
          <el-col :span="12">
            <el-statistic title="正确回答数" :value="currentStats.correctResponses" />
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-statistic title="正确率" :value="(currentStats.correctRate * 100).toFixed(1)" suffix="%" />
          </el-col>
          <el-col :span="12">
            <el-statistic title="平均用时" :value="currentStats.averageResponseTime" suffix="秒" />
          </el-col>
        </el-row>
        
        <el-divider>各选项统计</el-divider>
        <el-table :data="currentStats.choiceStats" stripe>
          <el-table-column prop="choiceKey" label="选项" width="80" />
          <el-table-column prop="choiceText" label="内容" min-width="200" />
          <el-table-column prop="selectedCount" label="选择次数" width="100" />
          <el-table-column prop="selectionRate" label="选择率" width="100">
            <template #default="{ row }">
              {{ (row.selectionRate * 100).toFixed(1) }}%
            </template>
          </el-table-column>
          <el-table-column prop="isCorrect" label="正确答案" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isCorrect" type="success">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { exerciseApi } from '@/api/exercise'
import {
  Search,
  Refresh,
  Plus,
  Delete,
  Edit,
  VideoPlay,
  DataAnalysis
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const editDialogVisible = ref(false)
const statsDialogVisible = ref(false)
const editFormRef = ref(null)

const questionList = ref([])
const selectedQuestions = ref([])
const currentStats = ref(null)

// 选项数据
const questionTypes = exerciseApi.getQuestionTypes()
const difficultyLevels = exerciseApi.getDifficultyLevels()

// 搜索表单
const searchForm = reactive({
  questionText: '',
  questionType: '',
  difficultyLevel: '',
  isActive: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 编辑表单
const editForm = reactive({
  id: null,
  moduleId: 1,
  questionText: '',
  questionType: 'single_choice',
  difficultyLevel: 3,
  points: 1.0,
  audioFilePath: '',
  audioDuration: 0,
  questionOrder: 1,
  isActive: true,
  choices: [
    { choiceKey: 'A', choiceText: '', isCorrect: false, choiceOrder: 1 },
    { choiceKey: 'B', choiceText: '', isCorrect: false, choiceOrder: 2 }
  ]
})

// 表单验证规则
const editRules = {
  moduleId: [
    { required: true, message: '请输入模块ID', trigger: 'blur' }
  ],
  questionText: [
    { required: true, message: '请输入题目内容', trigger: 'blur' }
  ],
  questionType: [
    { required: true, message: '请选择题目类型', trigger: 'change' }
  ],
  choices: [
    {
      validator: (rule, value, callback) => {
        if (value.length < 2) {
          callback(new Error('至少需要2个选项'))
        } else if (!value.some(choice => choice.isCorrect)) {
          callback(new Error('至少需要一个正确答案'))
        } else if (value.some(choice => !choice.choiceText.trim())) {
          callback(new Error('选项内容不能为空'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 计算属性
const hasSelection = computed(() => selectedQuestions.value.length > 0)

// 加载题目列表
const loadQuestions = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: 'questionOrder,asc',
      ...searchForm
    }
    
    const response = await exerciseApi.getMCQQuestions(params)
    
    questionList.value = response.content || []
    pagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载题目列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadQuestions()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    questionText: '',
    questionType: '',
    difficultyLevel: '',
    isActive: ''
  })
  handleSearch()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadQuestions()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadQuestions()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

// 状态切换
const handleStatusChange = async (question) => {
  try {
    await exerciseApi.updateMCQQuestion(question.id, { isActive: question.isActive })
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 恢复原状态
    question.isActive = !question.isActive
    ElMessage.error('状态更新失败')
  }
}

// 创建题目
const createQuestion = () => {
  Object.assign(editForm, {
    id: null,
    moduleId: 1,
    questionText: '',
    questionType: 'single_choice',
    difficultyLevel: 3,
    points: 1.0,
    audioFilePath: '',
    audioDuration: 0,
    questionOrder: 1,
    isActive: true,
    choices: [
      { choiceKey: 'A', choiceText: '', isCorrect: false, choiceOrder: 1 },
      { choiceKey: 'B', choiceText: '', isCorrect: false, choiceOrder: 2 }
    ]
  })
  editDialogVisible.value = true
}

// 编辑题目
const editQuestion = (question) => {
  Object.assign(editForm, {
    id: question.id,
    moduleId: question.moduleId,
    questionText: question.questionText,
    questionType: question.questionType,
    difficultyLevel: question.difficultyLevel,
    points: question.points,
    audioFilePath: question.audioFilePath || '',
    audioDuration: question.audioDuration || 0,
    questionOrder: question.questionOrder,
    isActive: question.isActive,
    choices: question.choices ? [...question.choices] : [
      { choiceKey: 'A', choiceText: '', isCorrect: false, choiceOrder: 1 },
      { choiceKey: 'B', choiceText: '', isCorrect: false, choiceOrder: 2 }
    ]
  })
  editDialogVisible.value = true
}

// 保存题目
const saveQuestion = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    const data = { ...editForm }
    
    if (editForm.id) {
      await exerciseApi.updateMCQQuestion(editForm.id, data)
      ElMessage.success('题目更新成功')
    } else {
      await exerciseApi.createMCQQuestion(data)
      ElMessage.success('题目创建成功')
    }
    
    editDialogVisible.value = false
    loadQuestions()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除题目
const deleteQuestion = (question) => {
  ElMessageBox.confirm(
    `确定要删除题目 "${question.questionText.substring(0, 30)}..." 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await exerciseApi.deleteMCQQuestion(question.id)
      ElMessage.success('删除成功')
      loadQuestions()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const batchDelete = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要删除的题目')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedQuestions.value.length} 个题目吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const questionIds = selectedQuestions.value.map(q => q.id)
      await exerciseApi.batchDeleteMCQQuestions(questionIds)
      ElMessage.success('批量删除成功')
      loadQuestions()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 查看统计
const viewStats = async (question) => {
  try {
    const stats = await exerciseApi.getMCQStats(question.id)
    currentStats.value = {
      ...stats,
      choiceStats: question.choices || []
    }
    statsDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

// 添加选项
const addChoice = () => {
  const nextKey = String.fromCharCode(65 + editForm.choices.length) // A, B, C, D...
  editForm.choices.push({
    choiceKey: nextKey,
    choiceText: '',
    isCorrect: false,
    choiceOrder: editForm.choices.length + 1
  })
}

// 删除选项
const removeChoice = (index) => {
  editForm.choices.splice(index, 1)
  // 重新排序
  editForm.choices.forEach((choice, i) => {
    choice.choiceOrder = i + 1
  })
}

// 播放音频
const playAudio = (audioPath) => {
  const audio = new Audio(audioPath)
  audio.play().catch(() => {
    ElMessage.error('音频播放失败')
  })
}

// 获取题目类型标签
const getQuestionTypeLabel = (type) => {
  const labels = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    true_false: '判断题',
    fill_blank: '填空题',
    short_answer: '简答题'
  }
  return labels[type] || type
}

// 获取题目类型标签类型
const getQuestionTypeType = (type) => {
  const types = {
    single_choice: 'primary',
    multiple_choice: 'success',
    true_false: 'warning',
    fill_blank: 'info',
    short_answer: 'danger'
  }
  return types[type] || ''
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadQuestions()
})
</script>

<style scoped>
.mcq-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.choices-section {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background-color: #fafafa;
}

.choice-item {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: white;
}

.choice-item:last-child {
  margin-bottom: 0;
}

.stats-content .el-statistic {
  text-align: center;
}

.text-gray {
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
  }
  
  .search-form .el-form--inline .el-form-item {
    display: block;
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .el-table {
    font-size: 12px;
  }
}
</style>

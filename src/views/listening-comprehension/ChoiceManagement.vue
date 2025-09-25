<template>
  <div class="choice-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>听力理解 - 选项管理</h2>
      <p>管理听力理解选择题的选项内容，设置正确答案</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加选项
        </el-button>
        <el-button 
          type="success" 
          @click="showBatchCreateDialog"
        >
          <el-icon><DocumentAdd /></el-icon>
          批量创建
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedChoices.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="refreshList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="showCopyDialog">
          <el-icon><CopyDocument /></el-icon>
          复制选项
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-select
          v-model="filterQuestion"
          placeholder="选择题目"
          clearable
          filterable
          style="width: 200px; margin-right: 12px"
          @change="handleFilterChange"
        >
          <el-option label="全部题目" value="" />
          <el-option 
            v-for="question in questions" 
            :key="question.id" 
            :label="`${question.id} - ${question.textStem ? question.textStem.substring(0, 30) + '...' : '无题干'}`" 
            :value="question.id" 
          />
        </el-select>
        <el-select
          v-model="filterLabel"
          placeholder="选项标签"
          clearable
          style="width: 120px; margin-right: 12px"
          @change="handleFilterChange"
        >
          <el-option label="全部标签" value="" />
          <el-option 
            v-for="label in choiceLabels" 
            :key="label.value" 
            :label="label.label" 
            :value="label.value" 
          />
        </el-select>
        <el-select
          v-model="filterCorrect"
          placeholder="答案类型"
          clearable
          style="width: 120px; margin-right: 12px"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="正确答案" value="true" />
          <el-option label="错误答案" value="false" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索选项内容..."
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>



    <!-- 选项列表 -->
    <div class="choice-list">
      <el-table
        v-loading="loading"
        :data="choiceList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="questionId" label="题目ID" width="100" align="center">
          <template #default="scope">
            <el-link type="primary" @click="viewQuestion(scope.row.questionId)">
              {{ scope.row.questionId }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="label" label="标签" width="80" align="center">
          <template #default="scope">
            <el-tag :color="getChoiceLabelColor(scope.row.label)" effect="dark">
              {{ scope.row.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="选项内容" min-width="300">
          <template #default="scope">
            <div class="choice-content">
              <span class="content-text">{{ scope.row.content }}</span>
              <el-tag 
                v-if="scope.row.isCorrect" 
                type="success" 
                size="small" 
                class="correct-tag"
              >
                正确答案
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="isCorrect" label="是否正确" width="120" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isCorrect"
              @change="handleToggleCorrect(scope.row)"
              :loading="scope.row.switching"
            />
          </template>
        </el-table-column>
        <el-table-column label="题目信息" width="200">
          <template #default="scope">
            <div class="question-info">
              <el-link type="primary" @click="showQuestionDetail(scope.row.questionId)">
                题目ID: {{ scope.row.questionId }}
              </el-link>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="scope.row.isCorrect ? 'warning' : 'success'"
              @click="handleToggleCorrect(scope.row)"
            >
              {{ scope.row.isCorrect ? '取消正确' : '设为正确' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
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
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑选项对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑选项' : '添加选项'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="choiceFormRef"
        :model="currentChoice"
        :rules="choiceRules"
        label-width="120px"
      >
        <el-form-item label="关联题目" prop="questionId">
          <el-select 
            v-model="currentChoice.questionId" 
            placeholder="请选择题目" 
            style="width: 100%"
            filterable
          >
            <el-option 
              v-for="question in questions" 
              :key="question.id" 
              :label="`${question.id} - ${question.textStem ? question.textStem.substring(0, 50) + '...' : '无题干'}`" 
              :value="question.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选项标签" prop="label">
          <el-select v-model="currentChoice.label" placeholder="请选择标签" style="width: 200px">
            <el-option 
              v-for="label in choiceLabels" 
              :key="label.value" 
              :label="label.label" 
              :value="label.value" 
            >
              <span :style="{ color: label.color }">{{ label.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="选项内容" prop="content">
          <el-input
            v-model="currentChoice.content"
            type="textarea"
            :rows="4"
            placeholder="请输入选项内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="是否正确答案">
          <el-switch
            v-model="currentChoice.isCorrect"
            active-text="正确答案"
            inactive-text="错误答案"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveChoice" :loading="saveLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看选项详情对话框 -->
    <el-dialog v-model="detailVisible" title="选项详情" width="600px">
      <div v-if="currentChoice" class="choice-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="选项ID">{{ currentChoice.id }}</el-descriptions-item>
          <el-descriptions-item label="题目ID">
            <el-link type="primary" @click="viewQuestion(currentChoice.questionId)">
              {{ currentChoice.questionId }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="选项标签">
            <el-tag :color="getChoiceLabelColor(currentChoice.label)" effect="dark">
              {{ currentChoice.label }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="是否正确">
            <el-tag :type="currentChoice.isCorrect ? 'success' : 'info'">
              {{ currentChoice.isCorrect ? '正确答案' : '错误答案' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="选项内容" :span="2">
            <div class="content-display">{{ currentChoice.content }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="question-info-section">
          <h4>关联题目信息</h4>
          <div class="question-detail">
            <p><strong>题干:</strong> {{ getQuestionStem(currentChoice.questionId) }}</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 批量创建选项对话框 -->
    <el-dialog
      v-model="batchCreateVisible"
      title="批量创建选项"
      width="800px"
      @close="resetBatchForm"
    >
      <div class="batch-create-section">
        <el-form
          ref="batchFormRef"
          :model="batchForm"
          label-width="120px"
        >
          <el-form-item label="选择题目" required>
            <el-select 
              v-model="batchForm.questionId" 
              placeholder="请选择题目" 
              style="width: 100%"
              filterable
            >
              <el-option 
                v-for="question in questions" 
                :key="question.id" 
                :label="`${question.id} - ${question.textStem ? question.textStem.substring(0, 50) + '...' : '无题干'}`" 
                :value="question.id" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="创建方式">
            <el-radio-group v-model="batchForm.createMode">
              <el-radio value="standard">标准四选项 (A、B、C、D)</el-radio>
              <el-radio value="custom">自定义选项</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <!-- 标准四选项模式 -->
        <div v-if="batchForm.createMode === 'standard'" class="standard-choices">
          <h4>标准四选项设置</h4>
          <div class="choice-inputs">
            <div 
              v-for="(choice, index) in batchForm.standardChoices" 
              :key="choice.label" 
              class="choice-input-item"
            >
              <div class="choice-header">
                <el-tag :color="getChoiceLabelColor(choice.label)" effect="dark">
                  选项 {{ choice.label }}
                </el-tag>
                <el-checkbox v-model="choice.isCorrect">设为正确答案</el-checkbox>
              </div>
              <el-input
                v-model="choice.content"
                type="textarea"
                :rows="2"
                :placeholder="`请输入选项${choice.label}的内容`"
                maxlength="1000"
              />
            </div>
          </div>
        </div>

        <!-- 自定义选项模式 -->
        <div v-if="batchForm.createMode === 'custom'" class="custom-choices">
          <div class="custom-header">
            <h4>自定义选项</h4>
            <el-button size="small" @click="addCustomChoice">
              <el-icon><Plus /></el-icon>
              添加选项
            </el-button>
          </div>
          <div class="choice-inputs">
            <div 
              v-for="(choice, index) in batchForm.customChoices" 
              :key="index" 
              class="choice-input-item"
            >
              <div class="choice-header">
                <el-select v-model="choice.label" placeholder="标签" style="width: 80px">
                  <el-option 
                    v-for="label in availableLabels" 
                    :key="label.value" 
                    :label="label.label" 
                    :value="label.value" 
                  />
                </el-select>
                <el-checkbox v-model="choice.isCorrect">设为正确答案</el-checkbox>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="removeCustomChoice(index)"
                  :disabled="batchForm.customChoices.length <= 1"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-input
                v-model="choice.content"
                type="textarea"
                :rows="2"
                :placeholder="`请输入选项${choice.label}的内容`"
                maxlength="1000"
              />
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchCreateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchCreate" :loading="batchSaveLoading">
          批量创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 复制选项对话框 -->
    <el-dialog v-model="copyVisible" title="复制选项" width="500px">
      <el-form label-width="120px">
        <el-form-item label="源题目">
          <el-select v-model="copyForm.fromQuestionId" placeholder="选择源题目" style="width: 100%">
            <el-option 
              v-for="question in questions" 
              :key="question.id" 
              :label="`${question.id} - ${question.textStem ? question.textStem.substring(0, 30) + '...' : '无题干'}`" 
              :value="question.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标题目">
          <el-select v-model="copyForm.toQuestionId" placeholder="选择目标题目" style="width: 100%">
            <el-option 
              v-for="question in questions" 
              :key="question.id" 
              :label="`${question.id} - ${question.textStem ? question.textStem.substring(0, 30) + '...' : '无题干'}`" 
              :value="question.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="copyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCopyChoices" :loading="copyLoading">
          复制
        </el-button>
      </template>
    </el-dialog>

    <!-- 题目详情对话框 -->
    <el-dialog v-model="questionDetailVisible" title="题目详情" width="800px">
      <div v-if="currentQuestionDetail" class="question-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目ID">{{ currentQuestionDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="模块ID">{{ currentQuestionDetail.moduleId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="音频ID">{{ currentQuestionDetail.audioId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="题目类型">{{ currentQuestionDetail.questionType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="难度等级">{{ currentQuestionDetail.difficultyLevel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分值">{{ currentQuestionDetail.points || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(currentQuestionDetail.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="question-content-section">
          <h4>题目内容</h4>
          <div class="content-block">
            <div class="content-item">
              <strong>题干：</strong>
              <div class="content-text">{{ currentQuestionDetail.textStem || '无题干' }}</div>
            </div>
            <div v-if="currentQuestionDetail.audioStem" class="content-item">
              <strong>音频题干：</strong>
              <div class="content-text">{{ currentQuestionDetail.audioStem }}</div>
            </div>
            <div v-if="currentQuestionDetail.imageStem" class="content-item">
              <strong>图片题干：</strong>
              <div class="content-text">{{ currentQuestionDetail.imageStem }}</div>
            </div>
          </div>
        </div>

        <div v-if="currentQuestionDetail.choices && currentQuestionDetail.choices.length > 0" class="choices-section">
          <h4>选项列表</h4>
          <div class="choices-list">
            <div 
              v-for="choice in currentQuestionDetail.choices" 
              :key="choice.id" 
              class="choice-item"
              :class="{ 'correct-choice': choice.isCorrect }"
            >
              <div class="choice-header">
                <el-tag :color="getChoiceLabelColor(choice.label)" effect="dark">
                  {{ choice.label }}
                </el-tag>
                <el-tag v-if="choice.isCorrect" type="success" size="small">
                  正确答案
                </el-tag>
              </div>
              <div class="choice-content-text">{{ choice.content }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else-if="questionDetailLoading" class="loading-container">
        <el-loading-text>加载题目详情中...</el-loading-text>
      </div>
      
      <div v-else class="error-container">
        <el-text type="danger">加载题目详情失败</el-text>
      </div>

      <template #footer>
        <el-button @click="questionDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="editQuestion(currentQuestionDetail.id)">
          编辑题目
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  Refresh,
  Search,
  View,
  Edit,
  CopyDocument,
  DocumentAdd
} from '@element-plus/icons-vue'
import {
  getAllChoices,
  getChoiceById,
  getChoicesByQuestionId,
  createChoice,
  updateChoice,
  deleteChoice,
  batchDeleteChoices,
  batchCreateChoices,
  setChoiceAsCorrect,
  toggleChoiceCorrectness,
  copyChoicesToQuestion,
  searchChoices,
  getChoiceStatistics,
  getChoiceLabels,
  getChoiceLabelColor,
  validateChoiceData,
  generateStandardChoiceTemplate,
  validateChoiceConfiguration,
  getQuestions,
  getQuestionById
} from '@/api/listening-mcq'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const batchSaveLoading = ref(false)
const copyLoading = ref(false)
const questionDetailLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const batchCreateVisible = ref(false)
const copyVisible = ref(false)
const questionDetailVisible = ref(false)
const isEditing = ref(false)

const choiceList = ref([])
const selectedChoices = ref([])
const questions = ref([])
const statistics = ref({})
const currentQuestionDetail = ref(null)

const searchKeyword = ref('')
const filterQuestion = ref('')
const filterLabel = ref('')
const filterCorrect = ref('')

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const currentChoice = reactive({
  id: null,
  questionId: null,
  label: '',
  content: '',
  isCorrect: false
})

const batchForm = reactive({
  questionId: null,
  createMode: 'standard',
  standardChoices: [],
  customChoices: []
})

const copyForm = reactive({
  fromQuestionId: null,
  toQuestionId: null
})

// 表单引用
const choiceFormRef = ref(null)
const batchFormRef = ref(null)

// 计算属性
const choiceLabels = computed(() => getChoiceLabels())

const availableLabels = computed(() => {
  const usedLabels = batchForm.customChoices.map(c => c.label).filter(Boolean)
  return choiceLabels.value.filter(label => !usedLabels.includes(label.value))
})

const formatDateTime = computed(() => {
  return (dateTime) => {
    if (!dateTime) return '-'
    return new Date(dateTime).toLocaleString('zh-CN')
  }
})

// 表单验证规则
const choiceRules = {
  questionId: [
    { required: true, message: '请选择关联的题目', trigger: 'change' }
  ],
  label: [
    { required: true, message: '请选择选项标签', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入选项内容', trigger: 'blur' },
    { max: 1000, message: '选项内容不能超过1000个字符', trigger: 'blur' }
  ]
}

// 方法
const loadChoiceList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    
    if (filterQuestion.value) {
      params.questionId = filterQuestion.value
    }
    if (filterLabel.value) {
      params.label = filterLabel.value
    }
    if (filterCorrect.value !== '') {
      params.isCorrect = filterCorrect.value === 'true'
    }
    
    const response = await getAllChoices(params)
    
    if (response && response.data) {
      choiceList.value = response.data.data.content || []
      pagination.total = response.data.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载选项列表失败')
    console.error('加载选项列表错误:', error)
  } finally {
    loading.value = false
  }
}

const loadQuestions = async () => {
  try {
    const response = await getQuestions({ page: 0, size: 1000 })
    console.log('题目API响应:', response) // 调试日志
    if (response && response.data) {
      // 根据API响应结构调整数据提取
      if (response.data.data && response.data.data.content) {
        questions.value = response.data.data.content || []
      } else if (response.data.content) {
        questions.value = response.data.content || []
      } else if (Array.isArray(response.data)) {
        questions.value = response.data
      } else {
        questions.value = []
        console.warn('未知的题目数据结构:', response.data)
      }
      console.log('加载的题目列表:', questions.value) // 调试日志
    }
  } catch (error) {
    console.error('加载题目列表错误:', error)
    ElMessage.error('加载题目列表失败')
  }
}

const loadStatistics = async () => {
  try {
    const response = await getChoiceStatistics()
    if (response && response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('加载统计信息错误:', error)
  }
}

const getQuestionStem = (questionId) => {
  const question = questions.value.find(q => q.id === questionId)
  return question ? (question.textStem || '无题干') : '未找到题目'
}

const showCreateDialog = async () => {
  isEditing.value = false
  resetCurrentChoice()
  
  // 确保题目列表是最新的
  if (questions.value.length === 0) {
    await loadQuestions()
  }
  
  dialogVisible.value = true
}

const showBatchCreateDialog = async () => {
  resetBatchForm()
  
  // 确保题目列表是最新的
  if (questions.value.length === 0) {
    await loadQuestions()
  }
  
  batchCreateVisible.value = true
}

const showCopyDialog = async () => {
  copyForm.fromQuestionId = null
  copyForm.toQuestionId = null
  
  // 确保题目列表是最新的
  if (questions.value.length === 0) {
    await loadQuestions()
  }
  
  copyVisible.value = true
}

const handleView = (choice) => {
  Object.assign(currentChoice, choice)
  detailVisible.value = true
}

const handleEdit = (choice) => {
  isEditing.value = true
  Object.assign(currentChoice, choice)
  dialogVisible.value = true
}

const handleDelete = (choice) => {
  ElMessageBox.confirm(
    `确定要删除选项"${choice.label}. ${choice.content.substring(0, 20)}..."吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteChoice(choice.id)
      ElMessage.success('选项删除成功')
      refreshList()
    } catch (error) {
      ElMessage.error('选项删除失败')
      console.error('删除选项错误:', error)
    }
  })
}

const handleBatchDelete = () => {
  if (selectedChoices.value.length === 0) {
    ElMessage.warning('请选择要删除的选项')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedChoices.value.length} 个选项吗？`,
    '批量删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const choiceIds = selectedChoices.value.map(c => c.id)
      await batchDeleteChoices(choiceIds)
      ElMessage.success('批量删除成功')
      refreshList()
    } catch (error) {
      ElMessage.error('批量删除失败')
      console.error('批量删除错误:', error)
    }
  })
}

const handleToggleCorrect = async (choice) => {
  try {
    choice.switching = true
    await toggleChoiceCorrectness(choice.id)
    choice.isCorrect = !choice.isCorrect
    ElMessage.success('设置成功')
    refreshList()
  } catch (error) {
    ElMessage.error('设置失败')
    console.error('切换正确性错误:', error)
  } finally {
    choice.switching = false
  }
}

const handleSaveChoice = async () => {
  if (!choiceFormRef.value) return
  
  try {
    await choiceFormRef.value.validate()
    
    // 验证选项数据
    const errors = validateChoiceData(currentChoice)
    if (errors.length > 0) {
      ElMessage.error(errors[0])
      return
    }
    
    saveLoading.value = true
    
    if (isEditing.value) {
      await updateChoice(currentChoice.id, currentChoice)
      ElMessage.success('选项更新成功')
    } else {
      await createChoice(currentChoice)
      ElMessage.success('选项创建成功')
    }
    
    dialogVisible.value = false
    refreshList()
  } catch (error) {
    ElMessage.error(isEditing.value ? '选项更新失败' : '选项创建失败')
    console.error('保存选项错误:', error)
  } finally {
    saveLoading.value = false
  }
}

const handleBatchCreate = async () => {
  if (!batchForm.questionId) {
    ElMessage.error('请选择题目')
    return
  }
  
  try {
    let choicesToCreate = []
    
    if (batchForm.createMode === 'standard') {
      choicesToCreate = batchForm.standardChoices.filter(c => c.content.trim())
    } else {
      choicesToCreate = batchForm.customChoices.filter(c => c.content.trim() && c.label)
    }
    
    if (choicesToCreate.length === 0) {
      ElMessage.error('请至少填写一个选项内容')
      return
    }
    
    // 验证选项配置
    const configErrors = validateChoiceConfiguration(choicesToCreate)
    if (configErrors.length > 0) {
      ElMessage.error(configErrors[0])
      return
    }
    
    batchSaveLoading.value = true
    
    // 设置questionId
    choicesToCreate.forEach(choice => {
      choice.questionId = batchForm.questionId
    })
    
    await batchCreateChoices(choicesToCreate)
    ElMessage.success(`批量创建成功，共创建 ${choicesToCreate.length} 个选项`)
    
    batchCreateVisible.value = false
    refreshList()
  } catch (error) {
    ElMessage.error('批量创建失败')
    console.error('批量创建错误:', error)
  } finally {
    batchSaveLoading.value = false
  }
}

const handleCopyChoices = async () => {
  if (!copyForm.fromQuestionId || !copyForm.toQuestionId) {
    ElMessage.error('请选择源题目和目标题目')
    return
  }
  
  if (copyForm.fromQuestionId === copyForm.toQuestionId) {
    ElMessage.error('源题目和目标题目不能相同')
    return
  }
  
  try {
    copyLoading.value = true
    await copyChoicesToQuestion(copyForm.fromQuestionId, copyForm.toQuestionId)
    ElMessage.success('选项复制成功')
    copyVisible.value = false
    refreshList()
  } catch (error) {
    ElMessage.error('选项复制失败')
    console.error('复制选项错误:', error)
  } finally {
    copyLoading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    refreshList()
    return
  }
  
  try {
    loading.value = true
    const params = {
      page: 0,
      size: pagination.size
    }
    
    const response = await searchChoices(searchKeyword.value, params)
    
    if (response && response.data) {
      choiceList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
      pagination.page = 1
    }
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error('搜索错误:', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.page = 1
  loadChoiceList()
}

const handleSelectionChange = (selection) => {
  selectedChoices.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadChoiceList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadChoiceList()
}

const refreshList = () => {
  loadChoiceList()
}

const showQuestionDetail = async (questionId) => {
  try {
    questionDetailLoading.value = true
    questionDetailVisible.value = true
    currentQuestionDetail.value = null
    
      const response = await getQuestionById(questionId)
      if (response && response.data) {
        currentQuestionDetail.value = response.data.data
      
      // 同时加载该题目的选项
      try {
        const choicesResponse = await getChoicesByQuestionId(questionId)
        if (choicesResponse && choicesResponse.data) {
          currentQuestionDetail.value.choices = choicesResponse.data
        }
      } catch (error) {
        console.error('加载选项列表错误:', error)
        currentQuestionDetail.value.choices = []
      }
    }
  } catch (error) {
    ElMessage.error('加载题目详情失败')
    console.error('加载题目详情错误:', error)
  } finally {
    questionDetailLoading.value = false
  }
}

const editQuestion = (questionId) => {
  // TODO: 跳转到题目编辑页面
  ElMessage.info(`编辑题目 ${questionId}`)
  questionDetailVisible.value = false
}

const viewQuestion = (questionId) => {
  showQuestionDetail(questionId)
}

const resetForm = () => {
  if (choiceFormRef.value) {
    choiceFormRef.value.resetFields()
  }
  resetCurrentChoice()
}

const resetCurrentChoice = () => {
  Object.assign(currentChoice, {
    id: null,
    questionId: null,
    label: '',
    content: '',
    isCorrect: false
  })
}

const resetBatchForm = () => {
  Object.assign(batchForm, {
    questionId: null,
    createMode: 'standard',
    standardChoices: generateStandardChoiceTemplate(null),
    customChoices: [{ label: 'A', content: '', isCorrect: false }]
  })
}

const addCustomChoice = () => {
  if (batchForm.customChoices.length >= 4) {
    ElMessage.warning('最多只能添加4个选项')
    return
  }
  
  const availableLabel = availableLabels.value[0]
  if (availableLabel) {
    batchForm.customChoices.push({
      label: availableLabel.value,
      content: '',
      isCorrect: false
    })
  }
}

const removeCustomChoice = (index) => {
  batchForm.customChoices.splice(index, 1)
}

// 生命周期
onMounted(async () => {
  loadChoiceList()
  await loadQuestions()
  
  // 调试：检查题目列表是否正确加载
  setTimeout(() => {
    console.log('onMounted后的题目列表状态:', questions.value)
    if (questions.value.length === 0) {
      console.warn('题目列表为空，可能是API接口问题或数据结构不匹配')
    }
  }, 1000)
  
  resetBatchForm()
})
</script>

<style scoped>
.choice-management {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.toolbar {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.statistics {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.choice-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.choice-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.correct-tag {
  flex-shrink: 0;
}

.question-info {
  font-size: 12px;
  color: #606266;
}

.question-stem {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.choice-detail {
  padding: 16px 0;
}

.content-display {
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

.question-info-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.question-info-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.question-detail {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.batch-create-section {
  padding: 16px 0;
}

.standard-choices,
.custom-choices {
  margin-top: 20px;
}

.custom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.custom-header h4 {
  margin: 0;
}

.choice-inputs {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.choice-input-item {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 16px;
  background: #fafafa;
}

.choice-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

/* 题目详情样式 */
.question-detail-content {
  padding: 16px 0;
}

.question-content-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.question-content-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.content-block {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.content-item {
  margin-bottom: 16px;
}

.content-item:last-child {
  margin-bottom: 0;
}

.content-item strong {
  color: #495057;
  font-weight: 600;
  margin-bottom: 8px;
  display: block;
}

.content-text {
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  background: white;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #dee2e6;
  min-height: 40px;
}

.choices-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.choices-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.choices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.choice-item {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
}

.choice-item.correct-choice {
  background: #f0f9ff;
  border-color: #67c23a;
  box-shadow: 0 2px 4px rgba(103, 194, 58, 0.1);
}

.choice-item .choice-header {
  margin-bottom: 12px;
}

.choice-content-text {
  color: #303133;
  line-height: 1.6;
  background: white;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #dee2e6;
}

.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
  color: #606266;
}
</style>

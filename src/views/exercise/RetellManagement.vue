<template>
  <div class="retell-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>故事复述管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="createItem">
              <el-icon><Plus /></el-icon>
              添加故事
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
          <el-form-item label="故事标题">
            <el-input
              v-model="searchForm.title"
              placeholder="搜索故事标题"
              clearable
              @clear="handleSearch"
            />
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
      
      <!-- 故事列表 -->
      <el-table
        v-loading="loading"
        :data="itemList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="故事标题" min-width="200" />
        <el-table-column prop="storyText" label="故事内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.storyText?.substring(0, 100) }}{{ row.storyText?.length > 100 ? '...' : '' }}
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
        <el-table-column prop="expectedDuration" label="预期时长" width="100">
          <template #default="{ row }">
            {{ row.expectedDuration }}秒
          </template>
        </el-table-column>
        <el-table-column label="音频" width="80">
          <template #default="{ row }">
            <el-button
              v-if="row.storyAudioPath"
              type="text"
              @click="playAudio(row.storyAudioPath)"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <span v-else class="text-gray">无</span>
          </template>
        </el-table-column>
        <el-table-column label="关键点数量" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.keyPoints?.length || 0 }}个
            </el-tag>
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
            <el-button type="info" size="small" @click="viewResponses(row)">
              <el-icon><DataAnalysis /></el-icon>
              回答
            </el-button>
            <el-button type="primary" size="small" @click="editItem(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteItem(row)">
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
    
    <!-- 编辑故事对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editForm.id ? '编辑故事' : '新增故事'"
      width="800px"
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
            <el-form-item label="故事标题" prop="title">
              <el-input v-model="editForm.title" placeholder="请输入故事标题" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="故事内容" prop="storyText">
          <el-input
            v-model="editForm.storyText"
            type="textarea"
            :rows="6"
            placeholder="请输入故事内容"
            show-word-limit
            maxlength="2000"
          />
        </el-form-item>
        
        <el-form-item label="指导语" prop="instructions">
          <el-input
            v-model="editForm.instructions"
            type="textarea"
            :rows="3"
            placeholder="请输入指导语，如：Listen carefully and retell the story"
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
            <el-form-item label="预期时长(秒)" prop="expectedDuration">
              <el-input-number v-model="editForm.expectedDuration" :min="30" :max="600" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否激活" prop="isActive">
              <el-switch v-model="editForm.isActive" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="音频路径" prop="storyAudioPath">
          <el-input v-model="editForm.storyAudioPath" placeholder="请输入故事音频文件路径" />
        </el-form-item>
        
        <!-- 关键点管理 -->
        <el-divider>关键点设置</el-divider>
        <div class="key-points-section">
          <div v-for="(point, index) in editForm.keyPoints" :key="index" class="key-point-item">
            <el-row :gutter="10" align="middle">
              <el-col :span="20">
                <el-input
                  v-model="editForm.keyPoints[index]"
                  placeholder="请输入关键点"
                />
              </el-col>
              <el-col :span="4">
                <el-button type="danger" @click="removeKeyPoint(index)" :disabled="editForm.keyPoints.length <= 1">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-col>
            </el-row>
          </div>
          
          <el-button type="primary" @click="addKeyPoint" :disabled="editForm.keyPoints.length >= 10">
            <el-icon><Plus /></el-icon>
            添加关键点
          </el-button>
          <p class="hint-text">关键点用于评估学生复述的完整性，建议设置3-8个</p>
        </div>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 回答列表对话框 -->
    <el-dialog
      v-model="responsesDialogVisible"
      title="学生回答记录"
      width="1000px"
    >
      <el-table
        v-loading="responsesLoading"
        :data="responsesList"
        stripe
        border
      >
        <el-table-column prop="id" label="回答ID" width="80" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="asrText" label="识别文本" min-width="300" show-overflow-tooltip />
        <el-table-column prop="elapsedMs" label="用时" width="100">
          <template #default="{ row }">
            {{ Math.round(row.elapsedMs / 1000) }}秒
          </template>
        </el-table-column>
        <el-table-column label="音频" width="80">
          <template #default="{ row }">
            <el-button
              v-if="row.answerAudioId"
              type="text"
              @click="playAnswerAudio(row.answerAudioId)"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <span v-else class="text-gray">无</span>
          </template>
        </el-table-column>
        <el-table-column label="回答时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.answeredAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="evaluateResponse(row)">
              评估
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container" style="margin-top: 20px;">
        <el-pagination
          v-model:current-page="responsesPagination.page"
          v-model:page-size="responsesPagination.size"
          :total="responsesPagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleResponsesSizeChange"
          @current-change="handleResponsesCurrentChange"
        />
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
const responsesLoading = ref(false)
const editDialogVisible = ref(false)
const responsesDialogVisible = ref(false)
const editFormRef = ref(null)

const itemList = ref([])
const selectedItems = ref([])
const responsesList = ref([])
const currentItem = ref(null)

// 选项数据
const difficultyLevels = exerciseApi.getDifficultyLevels()

// 搜索表单
const searchForm = reactive({
  title: '',
  difficultyLevel: '',
  isActive: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const responsesPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 编辑表单
const editForm = reactive({
  id: null,
  moduleId: 1,
  title: '',
  storyText: '',
  storyAudioPath: '',
  difficultyLevel: 3,
  expectedDuration: 180,
  keyPoints: ['emergency', 'landing', 'procedure'],
  instructions: 'Listen carefully and retell the story in your own words.',
  isActive: true
})

// 表单验证规则
const editRules = {
  moduleId: [
    { required: true, message: '请输入模块ID', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入故事标题', trigger: 'blur' }
  ],
  storyText: [
    { required: true, message: '请输入故事内容', trigger: 'blur' }
  ],
  keyPoints: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('至少需要一个关键点'))
        } else if (value.some(point => !point.trim())) {
          callback(new Error('关键点不能为空'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 计算属性
const hasSelection = computed(() => selectedItems.value.length > 0)

// 加载故事列表
const loadItems = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: 'createdAt,desc',
      ...searchForm
    }
    
    const response = await exerciseApi.getRetellItems(params)
    
    itemList.value = response.content || []
    pagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载故事列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadItems()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    title: '',
    difficultyLevel: '',
    isActive: ''
  })
  handleSearch()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadItems()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadItems()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

// 状态切换
const handleStatusChange = async (item) => {
  try {
    await exerciseApi.updateRetellItem(item.id, { isActive: item.isActive })
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 恢复原状态
    item.isActive = !item.isActive
    ElMessage.error('状态更新失败')
  }
}

// 创建故事
const createItem = () => {
  Object.assign(editForm, {
    id: null,
    moduleId: 1,
    title: '',
    storyText: '',
    storyAudioPath: '',
    difficultyLevel: 3,
    expectedDuration: 180,
    keyPoints: ['emergency', 'landing', 'procedure'],
    instructions: 'Listen carefully and retell the story in your own words.',
    isActive: true
  })
  editDialogVisible.value = true
}

// 编辑故事
const editItem = (item) => {
  Object.assign(editForm, {
    id: item.id,
    moduleId: item.moduleId,
    title: item.title,
    storyText: item.storyText,
    storyAudioPath: item.storyAudioPath || '',
    difficultyLevel: item.difficultyLevel,
    expectedDuration: item.expectedDuration,
    keyPoints: item.keyPoints ? [...item.keyPoints] : ['emergency'],
    instructions: item.instructions || 'Listen carefully and retell the story in your own words.',
    isActive: item.isActive
  })
  editDialogVisible.value = true
}

// 保存故事
const saveItem = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    const data = { ...editForm }
    
    if (editForm.id) {
      await exerciseApi.updateRetellItem(editForm.id, data)
      ElMessage.success('故事更新成功')
    } else {
      await exerciseApi.createRetellItem(data)
      ElMessage.success('故事创建成功')
    }
    
    editDialogVisible.value = false
    loadItems()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除故事
const deleteItem = (item) => {
  ElMessageBox.confirm(
    `确定要删除故事 "${item.title}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await exerciseApi.deleteRetellItem(item.id)
      ElMessage.success('删除成功')
      loadItems()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const batchDelete = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要删除的故事')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedItems.value.length} 个故事吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      for (const item of selectedItems.value) {
        await exerciseApi.deleteRetellItem(item.id)
      }
      ElMessage.success('批量删除成功')
      loadItems()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 查看回答记录
const viewResponses = async (item) => {
  currentItem.value = item
  responsesDialogVisible.value = true
  await loadResponses()
}

// 加载回答记录
const loadResponses = async () => {
  if (!currentItem.value) return
  
  try {
    responsesLoading.value = true
    
    // 模拟数据，实际应该调用API
    responsesList.value = [
      {
        id: 1,
        studentName: '张三',
        asrText: 'It was a stormy night and the pilot had to make an emergency landing...',
        elapsedMs: 125000,
        answerAudioId: 123,
        answeredAt: new Date(Date.now() - 1000 * 60 * 30)
      },
      {
        id: 2,
        studentName: '李四',
        asrText: 'The story is about emergency procedures during bad weather...',
        elapsedMs: 180000,
        answerAudioId: 124,
        answeredAt: new Date(Date.now() - 1000 * 60 * 60)
      }
    ]
    responsesPagination.total = 2
  } catch (error) {
    ElMessage.error('加载回答记录失败')
  } finally {
    responsesLoading.value = false
  }
}

// 回答记录分页处理
const handleResponsesSizeChange = (size) => {
  responsesPagination.size = size
  responsesPagination.page = 1
  loadResponses()
}

const handleResponsesCurrentChange = (page) => {
  responsesPagination.page = page
  loadResponses()
}

// 添加关键点
const addKeyPoint = () => {
  editForm.keyPoints.push('')
}

// 删除关键点
const removeKeyPoint = (index) => {
  editForm.keyPoints.splice(index, 1)
}

// 播放音频
const playAudio = (audioPath) => {
  const audio = new Audio(audioPath)
  audio.play().catch(() => {
    ElMessage.error('音频播放失败')
  })
}

// 播放回答音频
const playAnswerAudio = (audioId) => {
  // 这里应该根据audioId获取音频URL
  const audioUrl = `/api/media/${audioId}/audio`
  playAudio(audioUrl)
}

// 评估回答
const evaluateResponse = (response) => {
  ElMessage.info('评估功能待开发')
  // TODO: 实现回答评估功能
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.retell-management {
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

.key-points-section {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background-color: #fafafa;
}

.key-point-item {
  margin-bottom: 10px;
}

.key-point-item:last-child {
  margin-bottom: 15px;
}

.hint-text {
  font-size: 12px;
  color: #909399;
  margin: 10px 0 0 0;
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

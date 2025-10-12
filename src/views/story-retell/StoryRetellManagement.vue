<template>
  <div class="story-retell-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>故事复述管理</h2>
      <p>管理故事复述题目，包括音频内容、参考文本和评分标准</p>
    </div>

    <!-- 统计信息 -->
    <div class="statistics" v-if="statistics.totalItems !== undefined">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalItems || 0 }}</div>
              <div class="stat-label">总题目数</div>
            </div>
            <el-icon class="stat-icon"><Document /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.withReferenceText || 0 }}</div>
              <div class="stat-label">有参考文本</div>
            </div>
            <el-icon class="stat-icon"><EditPen /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalResponses || 0 }}</div>
              <div class="stat-label">总回答数</div>
            </div>
            <el-icon class="stat-icon"><ChatDotRound /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ (statistics.averageScore || 0).toFixed(1) }}</div>
              <div class="stat-label">平均分</div>
            </div>
            <el-icon class="stat-icon"><TrendCharts /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加题目
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedItems.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="refreshList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="exportItems">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-select
          v-model="filterModule"
          placeholder="选择模块"
          clearable
          style="width: 150px; margin-right: 12px"
          @change="handleFilterChange"
        >
          <el-option label="全部模块" value="" />
          <el-option 
            v-for="module in modules" 
            :key="module.id" 
            :label="module.name" 
            :value="module.id" 
          />
        </el-select>
        <el-select
          v-model="filterReferenceText"
          placeholder="参考文本"
          clearable
          style="width: 120px; margin-right: 12px"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="有参考文本" value="true" />
          <el-option label="无参考文本" value="false" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索题目标题..."
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

    <!-- 题目列表 -->
    <div class="item-list">
      <el-table
        v-loading="loading"
        :data="itemList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题目标题" min-width="200">
          <template #default="scope">
            <el-link type="primary" @click="handleView(scope.row)">
              {{ scope.row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="moduleId" label="模块" width="120" align="center">
          <template #default="scope">
            <el-tag type="info">
              {{ getModuleName(scope.row.moduleId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="音频时长" width="100" align="center">
          <template #default="scope">
            {{ formatAudioDuration(scope.row.audioDurationSec) }}
          </template>
        </el-table-column>
        <el-table-column label="答题时长" width="100" align="center">
          <template #default="scope">
            {{ formatAnswerTime(scope.row.answerSeconds) }}
          </template>
        </el-table-column>
        <el-table-column label="参考文本" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.referenceText ? 'success' : 'info'" size="small">
              {{ scope.row.referenceText ? '有' : '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-tooltip content="查看" placement="top">
                <el-button size="small" circle @click="handleView(scope.row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button size="small" circle type="primary" @click="handleEdit(scope.row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="复制" placement="top">
                <el-button size="small" circle type="success" @click="handleCopy(scope.row)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button size="small" circle type="danger" @click="handleDelete(scope.row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
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

    <!-- 创建/编辑题目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑题目' : '添加题目'"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="itemFormRef"
        :model="currentItem"
        :rules="itemRules"
        label-width="120px"
      >
        <el-form-item label="题目标题" prop="title">
          <el-input
            v-model="currentItem.title"
            placeholder="请输入题目标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="音频时长(秒)" prop="audioDurationSec">
              <el-input-number
                v-model="currentItem.audioDurationSec"
                :min="1"
                :max="3600"
                style="width: 100%"
                placeholder="音频时长"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="答题时长(秒)" prop="answerSeconds">
              <el-input-number
                v-model="currentItem.answerSeconds"
                :min="30"
                :max="1800"
                style="width: 100%"
                placeholder="答题时长"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="音频文件" prop="audioAssetId">
          <div class="audio-upload">
            <el-upload
              ref="audioUploadRef"
              :auto-upload="false"
              :on-change="handleAudioChange"
              :before-upload="beforeAudioUpload"
              :file-list="audioFileList"
              v-model:file-list="audioFileList"
              :limit="1"
              accept="audio/*"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择音频文件
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持mp3、wav格式，文件大小不超过50MB（选择后点击创建按钮时上传）
                </div>
              </template>
            </el-upload>
            
            <!-- 音频预览 -->
            <div v-if="currentAudioUrl || currentItem.audioAssetId" class="audio-preview">
              <div class="audio-info">
                <el-icon color="#67c23a"><VideoPlay /></el-icon>
                <span class="audio-label">音频文件</span>
              </div>
              <audio 
                :src="currentAudioUrl || getAudioUrl(currentItem.audioAssetId)" 
                controls 
                style="width: 100%; margin-top: 10px"
              >
                您的浏览器不支持音频播放
              </audio>
              <div class="audio-actions">
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="removeAudio"
                  style="margin-top: 8px;"
                >
                  移除音频
                </el-button>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="参考文本">
          <el-input
            v-model="currentItem.referenceText"
            type="textarea"
            :rows="6"
            placeholder="请输入参考文本（可选）"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveItem" :loading="saveLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看题目详情对话框 -->
    <el-dialog v-model="detailVisible" title="题目详情" width="800px">
      <div v-if="currentItem" class="item-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目ID">{{ currentItem.id }}</el-descriptions-item>
          <el-descriptions-item label="所属模块">
            {{ getModuleName(currentItem.moduleId) }}
          </el-descriptions-item>
          <el-descriptions-item label="题目标题" :span="2">
            {{ currentItem.title }}
          </el-descriptions-item>
          <el-descriptions-item label="音频时长">
            {{ formatAudioDuration(currentItem.audioDurationSec) }}
          </el-descriptions-item>
          <el-descriptions-item label="答题时长">
            {{ formatAnswerTime(currentItem.answerSeconds) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(currentItem.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="audio-section">
          <h4>音频内容</h4>
          <div v-if="currentItem.audioAssetId" class="audio-player">
            <audio controls style="width: 100%">
              <source :src="getAudioUrl(currentItem.audioAssetId)" type="audio/mpeg">
              您的浏览器不支持音频播放
            </audio>
          </div>
          <div v-else class="no-audio">
            <el-text type="info">暂无音频文件</el-text>
          </div>
        </div>

        <div v-if="currentItem.referenceText" class="reference-section">
          <h4>参考文本</h4>
          <div class="reference-text">
            {{ currentItem.referenceText }}
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="importVisible" title="批量导入题目" width="600px">
      <div class="import-section">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #default>
            <p>1. 请下载并填写模板文件</p>
            <p>2. 支持Excel和CSV格式</p>
            <p>3. 音频文件需要单独上传</p>
          </template>
        </el-alert>

        <div class="template-download">
          <el-button @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
        </div>

        <el-upload
          ref="importUploadRef"
          :before-upload="beforeImportUpload"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          :file-list="importFileList"
          :limit="1"
          accept=".xlsx,.xls,.csv"
          action="#"
          :http-request="uploadImportFile"
          style="margin-top: 20px"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持xlsx、xls、csv格式文件
            </div>
          </template>
        </el-upload>

        <div class="import-options" style="margin-top: 20px">
          <el-checkbox v-model="importOptions.skipDuplicates">跳过重复项</el-checkbox>
          <el-checkbox v-model="importOptions.updateExisting">更新已存在项</el-checkbox>
          <el-checkbox v-model="importOptions.validateData">验证数据</el-checkbox>
        </div>
      </div>

      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importLoading">
          开始导入
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
  Upload,
  Download,
  CopyDocument,
  Document,
  EditPen,
  ChatDotRound,
  TrendCharts
} from '@element-plus/icons-vue'
import {
  getRetellItems,
  getRetellItemById,
  createRetellItem,
  updateRetellItem,
  deleteRetellItem,
  copyRetellItem,
  searchRetellItems,
  batchDeleteItems,
  getOverallStats,
  uploadQuestionAudio,
  importRetellQuestions,
  exportRetellQuestions,
  exportRetellQuestionTemplate,
  formatAudioDuration,
  formatAnswerTime,
  validateRetellItemData,
  generateRetellItemTemplate
} from '@/api/story-retell.js'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const importLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const importVisible = ref(false)
const isEditing = ref(false)

const itemList = ref([])
const selectedItems = ref([])
const modules = ref([])
const statistics = ref({})

const searchKeyword = ref('')
const filterModule = ref('')
const filterReferenceText = ref('')

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const currentItem = reactive(generateRetellItemTemplate())

const audioFileList = ref([])
const importFileList = ref([])
const currentAudioUrl = ref('')
const currentAudioFile = ref(null)

const importOptions = reactive({
  skipDuplicates: true,
  updateExisting: false,
  validateData: true
})

// 表单引用
const itemFormRef = ref(null)
const audioUploadRef = ref(null)
const importUploadRef = ref(null)

// 表单验证规则
const itemRules = {
  title: [
    { required: true, message: '请输入题目标题', trigger: 'blur' },
    { max: 200, message: '题目标题不能超过200个字符', trigger: 'blur' }
  ],
  audioDurationSec: [
    { required: true, message: '请输入音频时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 3600, message: '音频时长应在1-3600秒之间', trigger: 'blur' }
  ],
  answerSeconds: [
    { required: true, message: '请输入答题时长', trigger: 'blur' },
    { type: 'number', min: 30, max: 1800, message: '答题时长应在30-1800秒之间', trigger: 'blur' }
  ]
}

// 计算属性
const formatDateTime = computed(() => {
  return (dateTime) => {
    if (!dateTime) return '-'
    return new Date(dateTime).toLocaleString('zh-CN')
  }
})

// 方法
const loadItemList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    
    if (filterModule.value) {
      params.moduleId = filterModule.value
    }
    if (filterReferenceText.value !== '') {
      params.hasReferenceText = filterReferenceText.value === 'true'
    }
    
    const response = await getRetellItems(params)
    
    if (response && response.data) {
      itemList.value = response.data.data.content || []
      pagination.total = response.data.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载题目列表失败')
    console.error('加载题目列表错误:', error)
  } finally {
    loading.value = false
  }
}

const loadModules = async () => {
  try {
    // TODO: 实现加载模块列表的API
    // 临时模拟数据
    modules.value = [
      { id: 1, name: '日常对话' },
      { id: 2, name: '工作场景' },
      { id: 3, name: '紧急情况' }
    ]
  } catch (error) {
    console.error('加载模块列表错误:', error)
  }
}

const loadStatistics = async () => {
  try {
    const response = await getOverallStats()
    if (response && response.data) {
      statistics.value = response.data.items || {}
    }
  } catch (error) {
    console.error('加载统计信息错误:', error)
  }
}

const getModuleName = (moduleId) => {
  const module = modules.value.find(m => m.id === moduleId)
  return module ? module.name : `模块${moduleId}`
}

const getAudioUrl = (audioAssetId) => {
  // TODO: 根据实际的媒体资源API返回音频URL
  return `/api/media/audio/${audioAssetId}`
}

const showCreateDialog = async () => {
  isEditing.value = false
  Object.assign(currentItem, generateRetellItemTemplate())
  audioFileList.value = []
  currentAudioFile.value = null
  currentAudioUrl.value = ''
  
  // 确保模块列表是最新的
  if (modules.value.length === 0) {
    await loadModules()
  }
  
  dialogVisible.value = true
}

const handleView = async (item) => {
  try {
    const response = await getRetellItemById(item.id)
    if (response && response.data) {
      Object.assign(currentItem, response.data.data)
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载题目详情失败')
    console.error('加载题目详情错误:', error)
  }
}

const handleEdit = async (item) => {
  try {
    isEditing.value = true
    const response = await getRetellItemById(item.id)
    if (response && response.data) {
      Object.assign(currentItem, response.data.data)
      
      // 清空新上传的音频
      currentAudioFile.value = null
      currentAudioUrl.value = ''
      
      // 如果有现有音频，显示但不作为新上传文件
      if (currentItem.audioAssetId) {
        audioFileList.value = []
      } else {
        audioFileList.value = []
      }
      
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载题目信息失败')
    console.error('加载题目信息错误:', error)
  }
}

const handleCopy = async (item) => {
  try {
    await copyRetellItem(item.id)
    ElMessage.success('题目复制成功')
    refreshList()
  } catch (error) {
    ElMessage.error('题目复制失败')
    console.error('复制题目错误:', error)
  }
}

const handleDelete = (item) => {
  ElMessageBox.confirm(
    `确定要删除题目"${item.title}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteRetellItem(item.id)
      ElMessage.success('题目删除成功')
      refreshList()
    } catch (error) {
      ElMessage.error('题目删除失败')
      console.error('删除题目错误:', error)
    }
  })
}

const handleBatchDelete = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要删除的题目')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedItems.value.length} 个题目吗？`,
    '批量删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const itemIds = selectedItems.value.map(item => item.id)
      await batchDeleteItems(itemIds)
      ElMessage.success('批量删除成功')
      refreshList()
    } catch (error) {
      ElMessage.error('批量删除失败')
      console.error('批量删除错误:', error)
    }
  })
}

const handleSaveItem = async () => {
  if (!itemFormRef.value) return
  
  try {
    await itemFormRef.value.validate()
    
    saveLoading.value = true
    
    // 创建新题目时必须有音频文件
    if (!isEditing.value && !currentAudioFile.value) {
      ElMessage.error('请选择音频文件')
      saveLoading.value = false
      return
    }
    
    console.log('准备保存题目，数据:', currentItem)
    
    if (isEditing.value) {
      // 编辑模式：如果有新音频文件，先上传再更新
      if (currentAudioFile.value) {
        console.log('编辑模式：上传新音频文件...')
        const audioId = await uploadAudioFile()
        
        if (audioId) {
          currentItem.audioAssetId = audioId
          console.log('新音频ID已设置:', audioId)
        } else {
          ElMessage.error('音频上传失败，请重试')
          saveLoading.value = false
          return
        }
      } else if (!currentItem.audioAssetId) {
        ElMessage.error('请选择音频文件')
        saveLoading.value = false
        return
      }
      
      // 更新题目
      await updateRetellItem(currentItem.id, currentItem)
      ElMessage.success('题目更新成功')
    } else {
      // 创建模式：先上传音频获取ID，再创建题目
      console.log('创建模式：开始上传音频文件...')
      
      let uploadedAudioId = null
      
      try {
        // 先上传音频文件
        uploadedAudioId = await uploadAudioFile()
        
        if (!uploadedAudioId) {
          ElMessage.error('音频上传失败，请重试')
          saveLoading.value = false
          return
        }
        
        console.log('音频上传成功，ID:', uploadedAudioId)
        
        // 设置音频ID
        currentItem.audioAssetId = uploadedAudioId
        
        // 创建题目记录
        console.log('创建题目记录，包含音频ID:', uploadedAudioId)
        const createResponse = await createRetellItem(currentItem)
        console.log('题目创建响应:', createResponse)
        
        ElMessage.success('题目创建成功')
        
      } catch (createError) {
        console.error('创建题目失败:', createError)
        
        // 如果题目创建失败但音频已上传
        if (uploadedAudioId) {
          ElMessage.error('题目创建失败。音频已上传到媒体库（ID: ' + uploadedAudioId + '），但未关联到题目')
        }
        
        throw createError
      }
    }
    
    dialogVisible.value = false
    refreshList()
  } catch (error) {
    ElMessage.error(isEditing.value ? '题目更新失败' : '题目创建失败')
    console.error('保存题目错误:', error)
  } finally {
    saveLoading.value = false
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
    
    const response = await searchRetellItems(searchKeyword.value, params)
    
    if (response && response.data) {
      itemList.value = response.data.content || []
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
  loadItemList()
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadItemList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadItemList()
}

const refreshList = () => {
  loadItemList()
  loadStatistics()
}

const viewResponses = (itemId) => {
  // TODO: 跳转到回答记录页面
  ElMessage.info(`查看题目 ${itemId} 的回答记录`)
}

// 音频上传相关
const handleAudioChange = (file) => {
  console.log('音频文件选择:', file)
  currentAudioFile.value = file.raw
  if (file.raw) {
    currentAudioUrl.value = URL.createObjectURL(file.raw)
  }
  // 更新文件列表显示
  audioFileList.value = [file]
}

const beforeAudioUpload = (file) => {
  const isAudio = file.type.startsWith('audio/')
  const isLt50M = file.size / 1024 / 1024 < 50

  if (!isAudio) {
    ElMessage.error('只能上传音频文件!')
    return false
  }
  if (!isLt50M) {
    ElMessage.error('音频文件大小不能超过 50MB!')
    return false
  }
  return true
}

const uploadAudioFile = async () => {
  if (!currentAudioFile.value) {
    return null
  }
  
  try {
    console.log('开始上传音频文件:', currentAudioFile.value.name)
    
    const formData = new FormData()
    formData.append('file', currentAudioFile.value)
    formData.append('type', 'audio')
    formData.append('category', 'story-retell')
    formData.append('title', `${currentItem.title} 音频`)
    formData.append('description', `故事复述题目 "${currentItem.title}" 的音频文件`)
    
    // 使用媒体上传API
    const { uploadMediaFile } = await import('@/api/media')
    const response = await uploadMediaFile(formData)
    
    console.log('音频上传响应:', response)
    
    // 检查多种可能的响应格式
    let mediaData = null
    if (response && response.data) {
      if (response.data.data && response.data.data.id) {
        mediaData = response.data.data
      } else if (response.data.id) {
        mediaData = response.data
      }
    }
    
    if (mediaData && mediaData.id) {
      console.log('音频上传成功，ID:', mediaData.id)
      ElMessage.success('音频上传成功')
      return mediaData.id
    } else {
      console.error('音频上传响应格式异常:', response)
      ElMessage.error('音频上传失败：响应格式异常')
      return null
    }
  } catch (error) {
    console.error('音频上传失败:', error)
    ElMessage.error('音频上传失败: ' + (error.message || error))
    return null
  }
}

const removeAudio = () => {
  currentAudioFile.value = null
  currentAudioUrl.value = ''
  audioFileList.value = []
  currentItem.audioAssetId = null
  
  // 清空音频上传组件
  if (audioUploadRef.value) {
    audioUploadRef.value.clearFiles()
  }
}

// 导入相关
const showImportDialog = () => {
  importFileList.value = []
  importVisible.value = true
}

const beforeImportUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                 file.type === 'application/vnd.ms-excel' ||
                 file.type === 'text/csv'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传Excel或CSV文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

const uploadImportFile = async (options) => {
  // 暂存文件，等待用户点击导入按钮
  importFileList.value = [options.file]
  options.onSuccess()
}

const handleImport = async () => {
  if (importFileList.value.length === 0) {
    ElMessage.error('请选择要导入的文件')
    return
  }
  
  try {
    importLoading.value = true
    const response = await importRetellQuestions(importFileList.value[0], importOptions)
    
    if (response && response.data) {
      ElMessage.success(`导入完成: 成功${response.data.successCount}条，失败${response.data.failureCount}条`)
      importVisible.value = false
      refreshList()
    }
  } catch (error) {
    ElMessage.error('导入失败')
    console.error('导入错误:', error)
  } finally {
    importLoading.value = false
  }
}

const handleImportSuccess = () => {
  // 文件上传成功，但不自动导入
}

const handleImportError = () => {
  ElMessage.error('文件上传失败')
}

const downloadTemplate = async () => {
  try {
    const response = await exportRetellQuestionTemplate()
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'retell_template.csv'
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
    console.error('下载模板错误:', error)
  }
}

const exportItems = async () => {
  try {
    const params = {}
    if (filterModule.value) {
      params.moduleId = filterModule.value
    }
    if (filterReferenceText.value !== '') {
      params.hasReferenceText = filterReferenceText.value === 'true'
    }
    
    const response = await exportRetellQuestions(params)
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'retell_questions.csv'
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error('导出错误:', error)
  }
}

const resetForm = () => {
  if (itemFormRef.value) {
    itemFormRef.value.resetFields()
  }
  Object.assign(currentItem, generateRetellItemTemplate())
  audioFileList.value = []
  currentAudioFile.value = null
  currentAudioUrl.value = ''
  
  // 清空音频上传组件
  if (audioUploadRef.value) {
    audioUploadRef.value.clearFiles()
  }
}

// 生命周期
onMounted(() => {
  loadItemList()
  loadModules()
  loadStatistics()
})
</script>

<style scoped>
.story-retell-management {
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

.statistics {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  position: relative;
}

.stat-content {
  position: relative;
  z-index: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  color: rgba(64, 158, 255, 0.1);
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

.item-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.item-detail {
  padding: 16px 0;
}

.audio-section,
.reference-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.audio-section h4,
.reference-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.audio-player {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.no-audio {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.reference-text {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  line-height: 1.6;
  white-space: pre-wrap;
}

.audio-upload {
  width: 100%;
}

.audio-preview {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.audio-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #67c23a;
  font-weight: 500;
}

.audio-label {
  color: #303133;
}

.audio-actions {
  display: flex;
  justify-content: flex-start;
}

.import-section {
  padding: 16px 0;
}

.template-download {
  margin-bottom: 20px;
}

.import-options {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: nowrap;
  align-items: center;
  justify-content: center;
}

.action-buttons .el-button.is-circle {
  padding: 6px;
}
</style>

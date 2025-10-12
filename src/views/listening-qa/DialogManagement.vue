<template>
  <div class="dialog-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>对话管理</h2>
      <p>管理听力问答对话，设置音频、时间限制和相关问题</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <!-- 增 - 新建 -->
        <el-button type="primary" @click="showCreateDialog = true" v-if="hasPermission('ADMIN')">
          <el-icon><Plus /></el-icon>
          新建对话
        </el-button>
        
        <!-- 删 - 批量删除 -->
        <el-button 
          type="danger" 
          :disabled="selectedDialogs.length === 0"
          @click="handleBatchDelete"
          v-if="hasPermission('ADMIN')"
        >
          <el-icon><Delete /></el-icon>
          批量删除 ({{ selectedDialogs.length }})
        </el-button>
        
        <!-- 改 - 批量激活 -->
        <el-button 
          type="success" 
          :disabled="selectedDialogs.length === 0"
          @click="handleBatchActivate"
          v-if="hasPermission('ADMIN')"
        >
          <el-icon><Select /></el-icon>
          批量激活
        </el-button>
        
        <!-- 改 - 批量停用 -->
        <el-button 
          type="warning" 
          :disabled="selectedDialogs.length === 0"
          @click="handleBatchDeactivate"
          v-if="hasPermission('ADMIN')"
        >
          <el-icon><RemoveFilled /></el-icon>
          批量停用
        </el-button>
        
        <!-- 查 - 刷新 -->
        <el-button @click="refreshList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        
        <!-- 导出 -->
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-select
          v-model="filterParams.moduleId"
          placeholder="选择模块"
          clearable
          style="width: 150px; margin-right: 12px"
          @change="handleFilter"
        >
          <el-option label="全部模块" value="" />
          <el-option 
            v-for="module in moduleOptions" 
            :key="module.value" 
            :label="module.label" 
            :value="module.value" 
          />
        </el-select>
        <el-select
          v-model="filterParams.isActive"
          placeholder="状态"
          clearable
          style="width: 120px; margin-right: 12px"
          @change="handleFilter"
        >
          <el-option label="全部状态" value="" />
          <el-option label="激活" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索对话标题..."
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>


    <!-- 对话列表 -->
    <div class="table-section">
      <el-table
        :data="dialogList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        
        <el-table-column prop="title" label="对话标题" min-width="200" sortable="custom">
          <template #default="scope">
            <div class="dialog-title">
              <el-text class="title" @click="viewDialog(scope.row)" style="cursor: pointer; color: #409eff;">
                {{ scope.row.title }}
              </el-text>
              <el-tag v-if="scope.row.tags" size="small" class="mt-1">
                {{ scope.row.tags }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="moduleId" label="所属模块" width="120">
          <template #default="scope">
            <el-tag type="info">{{ getModuleName(scope.row.moduleId) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="音频" width="80" align="center">
          <template #default="scope">
            <template v-if="scope.row.audioId">
              <el-tooltip 
                :content="playingAudioId === scope.row.audioId ? '暂停音频' : '播放音频'" 
                placement="top"
              >
                <el-button 
                  :type="playingAudioId === scope.row.audioId ? 'success' : 'primary'"
                  :icon="playingAudioId === scope.row.audioId ? VideoPause : VideoPlay"
                  circle
                  :class="{ 'playing-audio-btn': playingAudioId === scope.row.audioId }"
                  @click="toggleAudio(scope.row.audioId)"
                />
              </el-tooltip>
            </template>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>

        <el-table-column label="问题数量" width="120" align="center">
          <template #default="scope">
            <div class="question-count-cell">
              <el-tooltip content="管理问题" placement="top">
                <div 
                  class="question-count-badge"
                  :class="getQuestionCountClass(scope.row.questionCount)"
                  @click="manageQuestions(scope.row)"
                >
                  <el-icon class="question-icon"><QuestionFilled /></el-icon>
                  <span class="count-number">{{ scope.row.questionCount || 0 }}</span>
                </div>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="displayOrder" label="显示顺序" width="100" align="center" sortable="custom" />

        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isActive"
              @change="toggleDialogStatus(scope.row)"
              :disabled="!hasPermission('ADMIN')"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-tooltip content="查看" placement="top">
                <el-button size="small" circle @click="viewDialog(scope.row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button size="small" circle type="primary" @click="editDialog(scope.row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="复制" placement="top" >
                <el-button size="small" circle type="success" @click="copyDialogAction(scope.row)">
                  <el-icon><DocumentCopy /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button size="small" circle type="danger" @click="deleteDialogAction(scope.row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
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

    <!-- 创建/编辑对话对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEdit ? '编辑对话' : '新建对话'"
      width="800px"
      @close="resetDialogForm"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="120px"
      >
        <el-form-item label="对话标题" prop="title">
          <el-input v-model="dialogForm.title" placeholder="请输入对话标题" />
        </el-form-item>

        <el-form-item label="所属模块" prop="moduleId">
          <el-select 
            v-model="dialogForm.moduleId" 
            placeholder="请选择所属模块" 
            style="width: 100%"
            clearable
          >
            <el-option 
              v-for="module in moduleOptions" 
              :key="module.value" 
              :label="module.label" 
              :value="module.value" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="对话描述">
          <el-input
            v-model="dialogForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入对话描述"
          />
        </el-form-item>

        <el-form-item label="对话文本">
          <el-input
            v-model="dialogForm.dialogText"
            type="textarea"
            :rows="5"
            placeholder="请输入对话文本内容"
          />
        </el-form-item>

        <el-form-item label="音频文件" prop="audioId">
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
            <div v-if="currentAudioUrl || dialogForm.audioId" class="audio-preview">
              <div class="audio-info">
                <el-icon color="#67c23a"><VideoPlay /></el-icon>
                <span class="audio-label">音频文件</span>
              </div>
              <audio 
                :src="currentAudioUrl || getAudioUrl(dialogForm.audioId)" 
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

        <el-form-item label="显示顺序" prop="displayOrder">
          <el-input-number
            v-model="dialogForm.displayOrder"
            :min="1"
            placeholder="显示顺序"
            style="width: 200px"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="dialogForm.tags" placeholder="多个标签用逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="dialogForm.isActive"
                active-text="激活"
                inactive-text="停用"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="元数据">
          <el-input
            v-model="dialogForm.metadata"
            type="textarea"
            :rows="2"
            placeholder="JSON格式的元数据信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="submitDialog" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看对话详情对话框 -->
    <el-dialog
      v-model="showViewDialog"
      title="对话详情"
      width="900px"
    >
      <div v-if="currentDialog" class="dialog-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="对话标题">
            {{ currentDialog.title }}
          </el-descriptions-item>
          <el-descriptions-item label="所属模块">
            {{ getModuleName(currentDialog.moduleId) }}
          </el-descriptions-item>
          <el-descriptions-item label="显示顺序">
            {{ currentDialog.displayOrder }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentDialog.isActive ? 'success' : 'danger'">
              {{ currentDialog.isActive ? '激活' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="音频">
            <el-tag v-if="currentDialog.audioId" type="success">
              <el-icon><VideoPlay /></el-icon>
              有音频
            </el-tag>
            <el-tag v-else type="info">无音频</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="问题数量">
            <el-tag type="info">{{ currentDialog.questionCount || 0 }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentDialog.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(currentDialog.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentDialog.description" class="mt-4">
          <h4>对话描述</h4>
          <el-text>{{ currentDialog.description }}</el-text>
        </div>

        <div v-if="currentDialog.dialogText" class="mt-4">
          <h4>对话文本</h4>
          <el-text class="dialog-text">{{ currentDialog.dialogText }}</el-text>
        </div>

        <div v-if="currentDialog.tags" class="mt-4">
          <h4>标签</h4>
          <el-tag v-for="tag in currentDialog.tags.split(',')" :key="tag" class="mr-1">
            {{ tag.trim() }}
          </el-tag>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showViewDialog = false">关闭</el-button>
          <el-button type="primary" @click="manageQuestions(currentDialog)">
            管理问题
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入对话对话框 -->
    <el-dialog
      v-model="showImportDialog"
      title="批量导入对话"
      width="600px"
    >
      <div class="import-section">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          class="mb-4"
        >
          <template #default>
            <p>支持Excel(.xlsx)和CSV(.csv)格式文件</p>
            <p>请先下载模板文件，按照格式填写数据后上传</p>
          </template>
        </el-alert>

        <div class="mb-4">
          <el-button @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
        </div>

        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.csv"
          :on-change="handleFileChange"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传xlsx/csv文件，且不超过10MB
            </div>
          </template>
        </el-upload>

        <div class="import-options mt-4">
          <el-checkbox v-model="importOptions.skipDuplicates">跳过重复数据</el-checkbox>
          <el-checkbox v-model="importOptions.updateExisting">更新已存在的数据</el-checkbox>
          <el-checkbox v-model="importOptions.validateData">验证数据格式</el-checkbox>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showImportDialog = false">取消</el-button>
          <el-button type="primary" @click="submitImport" :loading="importing">
            开始导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Plus,
  Search,
  Refresh,
  Upload,
  Download,
  View,
  Edit,
  Delete,
  DocumentCopy,
  VideoPlay,
  VideoPause,
  QuestionFilled,
  UploadFilled,
  Select,
  RemoveFilled
} from '@element-plus/icons-vue'

// API导入
import {
  getDialogs,
  getDialogById,
  createDialog,
  updateDialog,
  deleteDialog,
  toggleDialogActive,
  copyDialog,
  searchDialogs,
  batchDeleteDialogs,
  batchActivateDialogs,
  batchDeactivateDialogs,
  formatAudioDuration,
  formatTimeLimit,
  getDialogType,
  validateDialogData,
  generateDialogTemplate,
  exportListeningQuestionTemplate,
  importListeningQuestions
} from '@/api/lsa-dialogs'
import { getExamModules, getModulesByType } from '@/api/exam-module'
import request from '@/utils/request'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const dialogList = ref([])
const selectedDialogs = ref([])
const currentDialog = ref(null)

// 音频相关
const playingAudioId = ref(null)
const currentAudio = ref(null)
const audioFileList = ref([])
const currentAudioUrl = ref('')
const currentAudioFile = ref(null)
const audioUploadRef = ref(null)

// 搜索和筛选
const searchKeyword = ref('')
const filterParams = reactive({
  moduleId: null,
  isActive: null,
  dialogType: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 排序
const sortParams = reactive({
  prop: 'displayOrder',
  order: 'ascending'
})

// 对话框状态
const showCreateDialog = ref(false)
const showViewDialog = ref(false)
const showImportDialog = ref(false)
const isEdit = ref(false)

// 表单
const dialogFormRef = ref(null)
const dialogForm = reactive({
  ...generateDialogTemplate(),
  displayOrder: 1
})
const dialogRules = {
  title: [
    { required: true, message: '请输入对话标题', trigger: 'blur' },
    { max: 255, message: '标题长度不能超过255个字符', trigger: 'blur' }
  ],
  moduleId: [
    { required: true, message: '请选择所属模块', trigger: 'change' }
  ],
  displayOrder: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' },
    { type: 'number', min: 1, message: '显示顺序必须为正数', trigger: 'blur' }
  ]
}

// 导入选项
const importOptions = reactive({
  skipDuplicates: true,
  updateExisting: false,
  validateData: true
})

// 模块选项
const moduleOptions = ref([])

// 权限检查
const hasPermission = (role) => {
  return authStore.hasRole(role)
}

// 加载模块列表
const loadModules = async () => {
  try {
    console.log('📦 开始加载模块列表...')
    
    // 尝试获取听力相关的模块
    const response = await getModulesByType('LISTENING_QA', { page: 0, size: 100 })
    
    console.log('📦 模块列表响应:', response.data)
    
    const modules = response.data.content || response.data.data?.content || []
    
    moduleOptions.value = modules.map(module => ({
      value: module.id,
      label: module.moduleName || module.moduleType || `模块${module.id}`
    }))
    
    console.log('📦 模块选项:', moduleOptions.value)
    
    // 如果没有找到听力模块，尝试获取所有模块
    if (moduleOptions.value.length === 0) {
      console.log('📦 未找到听力模块，尝试获取所有模块...')
      const allModulesResponse = await getExamModules({ page: 0, size: 100 })
      const allModules = allModulesResponse.data.content || allModulesResponse.data.data?.content || []
      
      moduleOptions.value = allModules.map(module => ({
        value: module.id,
        label: module.moduleName || module.moduleType || `模块${module.id}`
      }))
    }
    
    // 如果还是没有数据，使用默认模块
    if (moduleOptions.value.length === 0) {
      console.log('📦 使用默认模块选项')
      moduleOptions.value = [
        { value: 1, label: '听力问答模块' },
        { value: 2, label: '听力理解模块' },
        { value: 3, label: '综合听力模块' }
      ]
    }
    
    console.log('✅ 模块列表加载完成，共', moduleOptions.value.length, '个模块')
  } catch (error) {
    console.error('❌ 加载模块列表失败:', error)
    // 使用默认模块选项
    moduleOptions.value = [
      { value: 1, label: '听力问答模块' },
      { value: 2, label: '听力理解模块' },
      { value: 3, label: '综合听力模块' }
    ]
    console.log('📦 使用默认模块选项')
  }
}

// 获取模块名称
const getModuleName = (moduleId) => {
  const module = moduleOptions.value.find(m => m.value === moduleId)
  return module ? module.label : `模块${moduleId}`
}

// 获取问题数量样式类
const getQuestionCountClass = (count) => {
  if (!count || count === 0) {
    return 'count-zero'
  } else if (count <= 3) {
    return 'count-low'
  } else if (count <= 6) {
    return 'count-medium'
  } else {
    return 'count-high'
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 加载对话列表
const loadDialogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: [sortParams.prop, sortParams.order === 'ascending' ? 'asc' : 'desc']
    }

    // 添加筛选参数
    if (filterParams.moduleId) params.moduleId = filterParams.moduleId
    if (filterParams.isActive !== null && filterParams.isActive !== '') {
      params.isActive = filterParams.isActive
    }

    let response
    if (searchKeyword.value) {
      response = await searchDialogs(searchKeyword.value, params)
    } else {
      response = await getDialogs(params)
    }

    console.log('📋 对话列表响应:', response.data)

    // 处理响应数据，确保状态字段正确
    const content = response.data.data?.content || response.data.content || []
    dialogList.value = content.map(dialog => ({
      ...dialog,
      // 确保 isActive 是布尔值
      isActive: dialog.isActive === true || dialog.isActive === 'true' || dialog.isActive === 1
    }))
    
    pagination.total = response.data.data?.totalElements || response.data.totalElements || 0
    
    console.log('📋 处理后的对话列表:', dialogList.value)
  } catch (error) {
    console.error('❌ 加载对话列表失败:', error)
    ElMessage.error('加载对话列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadDialogs()
}

// 筛选处理
const handleFilter = () => {
  pagination.page = 1
  loadDialogs()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  Object.assign(filterParams, {
    moduleId: null,
    isActive: null,
    dialogType: null
  })
  pagination.page = 1
  loadDialogs()
}

// 刷新列表
const refreshList = () => {
  searchKeyword.value = ''
  Object.assign(filterParams, {
    moduleId: null,
    isActive: null,
    dialogType: null
  })
  pagination.page = 1
  loadDialogs()
  ElMessage.success('列表已刷新')
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadDialogs()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadDialogs()
}

// 排序处理
const handleSortChange = ({ prop, order }) => {
  sortParams.prop = prop || 'displayOrder'
  sortParams.order = order || 'ascending'
  loadDialogs()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedDialogs.value = selection
}

// 查看对话
const viewDialog = async (dialog) => {
  try {
    const response = await getDialogById(dialog.id)
    currentDialog.value = response.data
    showViewDialog.value = true
  } catch (error) {
    ElMessage.error('获取对话详情失败：' + error.message)
  }
}

// 编辑对话
const editDialog = async (dialog) => {
  isEdit.value = true
  
  try {
    const response = await getDialogById(dialog.id)
    Object.assign(dialogForm, response.data.data)
    
    // 清空新上传的音频
    currentAudioFile.value = null
    currentAudioUrl.value = ''
    
    // 如果有现有音频，不作为新上传文件
    audioFileList.value = []
    
    showCreateDialog.value = true
  } catch (error) {
    ElMessage.error('加载对话信息失败：' + error.message)
  }
}

// 复制对话
const copyDialogAction = async (dialog) => {
  try {
    await copyDialog(dialog.id)
    ElMessage.success('对话复制成功')
    loadDialogs()
  } catch (error) {
    ElMessage.error('复制对话失败：' + error.message)
  }
}

// 删除对话
const deleteDialogAction = (dialog) => {
  ElMessageBox.confirm(
    `确定要删除对话"${dialog.title}"吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteDialog(dialog.id)
      ElMessage.success('删除成功')
      loadDialogs()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

// 切换对话状态
const toggleDialogStatus = async (dialog) => {
  const originalStatus = dialog.isActive
  const targetStatus = !originalStatus
  
  console.log(`🔄 切换对话状态 - ID: ${dialog.id}, 原状态: ${originalStatus}, 目标状态: ${targetStatus}`)
  
  try {
    // 先更新UI状态（乐观更新）
    dialog.isActive = targetStatus
    
    // 调用后端API
    const response = await toggleDialogActive(dialog.id)
    console.log('🔄 状态切换响应:', response.data)
    
    // 从后端获取最新状态
    const refreshedDialog = await getDialogById(dialog.id)
    const newStatus = refreshedDialog.data.isActive
    
    console.log(`✅ 状态切换成功 - 最新状态: ${newStatus}`)
    
    // 更新为后端返回的最新状态
    dialog.isActive = newStatus === true || newStatus === 'true' || newStatus === 1
    
    ElMessage.success(`对话已${dialog.isActive ? '激活' : '停用'}`)
    
    // 刷新列表以确保所有数据同步
    loadDialogs()
  } catch (error) {
    console.error('❌ 状态切换失败:', error)
    // 恢复原状态
    dialog.isActive = originalStatus
    ElMessage.error('状态切换失败：' + (error.message || '未知错误'))
  }
}

// 管理问题
const manageQuestions = (dialog) => {
  router.push({
    name: 'listening-qa-questions',
    params: { dialogId: dialog.id },
    query: { dialogTitle: dialog.title }
  })
}

// 批量操作
const handleBatchActivate = () => {
  if (selectedDialogs.value.length === 0) {
    ElMessage.warning('请选择要激活的对话')
    return
  }
  
  const count = selectedDialogs.value.length
  ElMessageBox.confirm(
    `确定要激活选中的 ${count} 个对话吗？`,
    '确认批量激活',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      const ids = selectedDialogs.value.map(d => d.id)
      console.log('🔄 批量激活对话 - IDs:', ids)
      
      await batchActivateDialogs(ids)
      
      ElMessage.success(`成功激活 ${count} 个对话`)
      selectedDialogs.value = []
      loadDialogs()
    } catch (error) {
      console.error('❌ 批量激活失败:', error)
      ElMessage.error('批量激活失败：' + (error.message || '未知错误'))
    }
  }).catch(() => {
    // 用户取消操作
  })
}

const handleBatchDeactivate = () => {
  if (selectedDialogs.value.length === 0) {
    ElMessage.warning('请选择要停用的对话')
    return
  }
  
  const count = selectedDialogs.value.length
  ElMessageBox.confirm(
    `确定要停用选中的 ${count} 个对话吗？`,
    '确认批量停用',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedDialogs.value.map(d => d.id)
      console.log('🔄 批量停用对话 - IDs:', ids)
      
      await batchDeactivateDialogs(ids)
      
      ElMessage.success(`成功停用 ${count} 个对话`)
      selectedDialogs.value = []
      loadDialogs()
    } catch (error) {
      console.error('❌ 批量停用失败:', error)
      ElMessage.error('批量停用失败：' + (error.message || '未知错误'))
    }
  }).catch(() => {
    // 用户取消操作
  })
}

const handleBatchDelete = () => {
  if (selectedDialogs.value.length === 0) {
    ElMessage.warning('请选择要删除的对话')
    return
  }
  
  const count = selectedDialogs.value.length
  ElMessageBox.confirm(
    `确定要删除选中的 ${count} 个对话吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedDialogs.value.map(d => d.id)
      await batchDeleteDialogs(ids)
      ElMessage.success('批量删除成功')
      selectedDialogs.value = []
      loadDialogs()
    } catch (error) {
      ElMessage.error('批量删除失败：' + error.message)
    }
  })
}

// 音频相关方法
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
    formData.append('category', 'lsa-dialog')
    formData.append('title', `${dialogForm.title} 音频`)
    formData.append('description', `听力问答对话 "${dialogForm.title}" 的音频文件`)
    
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
  dialogForm.audioId = null
  
  // 清空音频上传组件
  if (audioUploadRef.value) {
    audioUploadRef.value.clearFiles()
  }
}

const getAudioUrl = (audioId) => {
  if (!audioId) return null
  return `/api/media/audio/${audioId}`
}

// 切换音频播放/暂停
const toggleAudio = async (audioId) => {
  // 如果点击的是正在播放的音频，则暂停
  if (playingAudioId.value === audioId && currentAudio.value) {
    pauseAudio()
    return
  }
  
  // 否则播放新音频
  await playAudio(audioId)
}

// 暂停音频
const pauseAudio = () => {
  if (currentAudio.value) {
    currentAudio.value.pause()
    playingAudioId.value = null
    currentAudio.value = null
    console.log('🎵 音频已暂停')
  }
}

// 播放音频
const playAudio = async (audioId) => {
  console.log('🎵 尝试播放音频，音频ID:', audioId)
  
  if (!audioId) {
    ElMessage.warning('音频ID不存在')
    return
  }
  
  // 如果有正在播放的音频，先停止
  if (currentAudio.value) {
    currentAudio.value.pause()
    currentAudio.value = null
  }
  
  try {
    // 通过后端API获取媒体资源信息
    console.log('🎵 调用后端API获取媒体资源:', audioId)
    const response = await request.get(`/media/${audioId}`)

    console.log('🎵 后端媒体资源响应:', response.data)

    const previewUrl = response.data.data.previewUrl
    console.log('🎵 后端返回的预览URL:', previewUrl)

    // 将相对路径拼接成完整路径
    let audioUrl = null
    if (previewUrl) {
      if (previewUrl.startsWith('http')) {
        // 已经是完整URL
        audioUrl = previewUrl
      } else {
        // 相对路径，需要拼接API基础路径
        const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
        audioUrl = previewUrl.startsWith('/') ? `${baseURL}${previewUrl}` : `${baseURL}/${previewUrl}`
      }
    }
    
    console.log('🎵 构建的完整音频URL:', audioUrl)
    
    if (!audioUrl) {
      ElMessage.error('无法获取音频URL')
      return
    }
    
    // 创建音频元素
    const audio = new Audio(audioUrl)
    currentAudio.value = audio
    
    // 添加事件监听
    audio.addEventListener('error', (e) => {
      console.error('🎵 音频加载错误:', e)
      ElMessage.error('音频文件加载失败')
      playingAudioId.value = null
      currentAudio.value = null
    })
    
    // 音频播放结束时重置状态
    audio.addEventListener('ended', () => {
      console.log('🎵 音频播放完成')
      playingAudioId.value = null
      currentAudio.value = null
    })
    
    // 尝试播放
    audio.play().then(() => {
      console.log('🎵 音频播放开始')
      playingAudioId.value = audioId
    }).catch(error => {
      console.error('🎵 音频播放失败:', error)
      ElMessage.error('音频播放失败: ' + error.message)
      playingAudioId.value = null
      currentAudio.value = null
    })
    
  } catch (error) {
    console.error('🎵 调用后端API失败:', error)
    ElMessage.error('获取音频资源失败: ' + (error.message || error))
    playingAudioId.value = null
    currentAudio.value = null
  }
}

// 提交对话表单
const submitDialog = async () => {
  if (!dialogFormRef.value) return

  try {
    await dialogFormRef.value.validate()
    
    // 验证数据
    const errors = validateDialogData(dialogForm)
    if (errors.length > 0) {
      ElMessage.error('表单验证失败：' + errors[0])
      return
    }

    submitting.value = true

    if (isEdit.value) {
      // 编辑模式：如果有新音频文件，先上传再更新
      if (currentAudioFile.value) {
        console.log('编辑模式：上传新音频文件...')
        const audioId = await uploadAudioFile()
        
        if (audioId) {
          dialogForm.audioId = audioId
          console.log('新音频ID已设置:', audioId)
        } else {
          ElMessage.error('音频上传失败，请重试')
          submitting.value = false
          return
        }
      }
      
      await updateDialog(dialogForm.id, dialogForm)
      ElMessage.success('对话更新成功')
    } else {
      // 创建模式：先上传音频获取ID，再创建对话
      console.log('创建模式：开始上传音频文件...')
      
      let uploadedAudioId = null
      
      if (currentAudioFile.value) {
        try {
          // 先上传音频文件
          uploadedAudioId = await uploadAudioFile()
          
          if (!uploadedAudioId) {
            ElMessage.error('音频上传失败，请重试')
            submitting.value = false
            return
          }
          
          console.log('音频上传成功，ID:', uploadedAudioId)
          
          // 设置音频ID
          dialogForm.audioId = uploadedAudioId
        } catch (uploadError) {
          console.error('音频上传失败:', uploadError)
          ElMessage.error('音频上传失败，请重试')
          submitting.value = false
          return
        }
      }
      
      // 创建对话记录
      console.log('创建对话记录，包含音频ID:', uploadedAudioId)
      await createDialog(dialogForm)
      ElMessage.success('对话创建成功')
    }

    showCreateDialog.value = false
    loadDialogs()
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetDialogForm = () => {
  if (dialogFormRef.value) {
    dialogFormRef.value.resetFields()
  }
  
  // 生成新的表单模板并设置默认值
  const template = generateDialogTemplate()
  Object.assign(dialogForm, {
    ...template,
    displayOrder: 1
  })
  
  isEdit.value = false
  
  // 清空音频相关数据
  currentAudioFile.value = null
  currentAudioUrl.value = ''
  audioFileList.value = []
  
  // 清空音频上传组件
  if (audioUploadRef.value) {
    audioUploadRef.value.clearFiles()
  }
}

// 导出数据
const handleExport = async () => {
  try {
    const params = {}
    if (filterParams.moduleId) {
      params.moduleId = filterParams.moduleId
    }
    if (filterParams.isActive !== null) {
      params.isActive = filterParams.isActive
    }
    
    // 创建下载链接
    const timestamp = new Date().toISOString().slice(0, 10)
    ElMessage.success(`导出对话数据（${timestamp}）`)
    
    // TODO: 实际的导出API调用
    // const response = await exportDialogsAPI(params)
    // const blob = new Blob([response], { type: 'text/csv;charset=utf-8' })
    // const url = URL.createObjectURL(blob)
    // const link = document.createElement('a')
    // link.href = url
    // link.download = `dialogs_${timestamp}.csv`
    // link.click()
    // URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await exportListeningQuestionTemplate()
    // 创建下载链接
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'dialog_template.csv'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败：' + error.message)
  }
}

// 文件选择处理
const handleFileChange = (file) => {
  // 可以在这里添加文件预览或验证逻辑
}

// 提交导入
const submitImport = async () => {
  const uploadRef = document.querySelector('.el-upload')
  const fileList = uploadRef?.files
  
  if (!fileList || fileList.length === 0) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  importing.value = true
  try {
    const file = fileList[0]
    const response = await importListeningQuestions(file, importOptions)
    
    ElMessage.success('导入成功')
    showImportDialog.value = false
    loadDialogs()
  } catch (error) {
    ElMessage.error('导入失败：' + error.message)
  } finally {
    importing.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadModules()  // 加载模块列表
  loadDialogs()  // 加载对话列表
})
</script>

<style scoped>
.dialog-management {
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
  gap: 0;
}

.table-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.dialog-title .title {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
}

.dialog-detail {
  padding: 16px 0;
}

.dialog-detail h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.dialog-detail .dialog-text {
  white-space: pre-wrap;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  display: block;
  margin-top: 8px;
  line-height: 1.6;
  color: #303133;
}

.import-section .mb-4 {
  margin-bottom: 16px;
}

.import-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mt-1 {
  margin-top: 4px;
}

.mt-4 {
  margin-top: 16px;
}

.mr-1 {
  margin-right: 4px;
}

/* 操作按钮样式 */
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

/* 文本样式 */
.text-muted {
  color: #909399;
  font-size: 12px;
}

/* 音频上传样式 */
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

/* 正在播放的音频按钮样式 */
.playing-audio-btn {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

/* 问题数量样式 */
.question-count-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.question-count-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  min-width: 60px;
  justify-content: center;
}

.question-icon {
  font-size: 16px;
}

.count-number {
  font-size: 14px;
  line-height: 1;
}

/* 0个问题 - 灰色 */
.question-count-badge.count-zero {
  background: #f5f7fa;
  color: #909399;
  border: 1px solid #e4e7ed;
}

.question-count-badge.count-zero:hover {
  background: #ebeef5;
  border-color: #d3d6dd;
  transform: translateY(-1px);
}

/* 1-3个问题 - 蓝色 */
.question-count-badge.count-low {
  background: #ecf5ff;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.question-count-badge.count-low:hover {
  background: #d9ecff;
  border-color: #b3d8ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

/* 4-6个问题 - 橙色 */
.question-count-badge.count-medium {
  background: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #f5dab1;
}

.question-count-badge.count-medium:hover {
  background: #f5dab1;
  border-color: #ebb563;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
}

/* 7+个问题 - 绿色 */
.question-count-badge.count-high {
  background: #f0f9ff;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

.question-count-badge.count-high:hover {
  background: #c2e7b0;
  border-color: #95d475;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
}
</style>

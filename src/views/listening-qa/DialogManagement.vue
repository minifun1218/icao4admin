<template>
  <div class="dialog-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>听力简答 - 对话管理</h1>
      <p>管理听力简答考试的对话内容和相关音频资源</p>
    </div>

    <!-- 搜索和操作区域 -->
    <el-card class="search-card" shadow="never">
      <div class="search-section">
        <div class="search-left">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索对话标题或内容"
            style="width: 300px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select
            v-model="searchForm.moduleId"
            placeholder="选择模块"
            style="width: 200px; margin-left: 10px"
            clearable
          >
            <el-option label="所有模块" :value="null" />
            <el-option label="模块1" :value="1" />
            <el-option label="模块2" :value="2" />
          </el-select>
          
          <el-select
            v-model="searchForm.isActive"
            placeholder="状态"
            style="width: 120px; margin-left: 10px"
            clearable
          >
            <el-option label="全部" :value="null" />
            <el-option label="激活" :value="true" />
            <el-option label="未激活" :value="false" />
          </el-select>
          
          <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          
          <el-button @click="resetSearch" style="margin-left: 10px">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
        
        <div class="search-right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增对话
          </el-button>
          
          <el-dropdown @command="handleBatchAction" style="margin-left: 10px">
            <el-button>
              批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="activate" :disabled="selectedRows.length === 0">
                  批量激活
                </el-dropdown-item>
                <el-dropdown-item command="deactivate" :disabled="selectedRows.length === 0">
                  批量停用
                </el-dropdown-item>
                <el-dropdown-item command="delete" :disabled="selectedRows.length === 0" divided>
                  批量删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <el-button @click="handleExport" style="margin-left: 10px">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          
          <el-button @click="handleImport" style="margin-left: 10px">
            <el-icon><Upload /></el-icon>
            导入
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        
        <el-table-column prop="title" label="对话标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        
        <el-table-column label="音频信息" width="150">
          <template #default="{ row }">
            <div v-if="row.audioId">
              <el-tag size="small" type="success">有音频</el-tag>
              <div class="audio-duration">{{ row.audioDurationFormatted || '未知时长' }}</div>
            </div>
            <el-tag v-else size="small" type="info">无音频</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="dialogType" label="对话类型" width="100">
          <template #default="{ row }">
            <el-tag 
              size="small" 
              :type="getDialogTypeTagType(row.dialogType)"
            >
              {{ row.dialogType || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="difficultyLevel" label="难度等级" width="100">
          <template #default="{ row }">
            <el-tag 
              size="small" 
              :type="getDifficultyTagType(row.difficultyLevel)"
            >
              {{ row.difficultyLevel || '未设置' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="questionCount" label="问题数" width="80" />
        
        <el-table-column prop="displayOrder" label="排序" width="80" sortable="custom" />
        
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="handleToggleStatus(row)"
              :loading="row.statusLoading"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="160" sortable="custom">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="text" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="text" size="small" @click="handleCopy(row)">
              复制
            </el-button>
            <el-button type="text" size="small" @click="handleViewQuestions(row)">
              问题
            </el-button>
            <el-popconfirm
              title="确定删除这个对话吗？"
              @confirm="handleDelete(row)"
              width="200"
            >
              <template #reference>
                <el-button type="text" size="small" style="color: #f56c6c">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增对话' : '编辑对话'"
      width="800px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="对话标题" prop="title">
              <el-input v-model="dialogForm.title" placeholder="请输入对话标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模块ID" prop="moduleId">
              <el-select v-model="dialogForm.moduleId" placeholder="请选择模块" style="width: 100%">
                <el-option label="模块1" :value="1" />
                <el-option label="模块2" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="对话描述" prop="description">
          <el-input
            v-model="dialogForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入对话描述"
          />
        </el-form-item>
        
        <el-form-item label="对话文本" prop="dialogText">
          <el-input
            v-model="dialogForm.dialogText"
            type="textarea"
            :rows="5"
            placeholder="请输入对话文本内容"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="音频资源">
              <div class="audio-upload-wrapper">
                <!-- 音频上传区域 -->
                <div v-if="!currentAudioInfo" class="audio-upload-zone">
                  <el-upload
                    ref="audioUploadRef"
                    class="audio-upload"
                    :action="uploadAction"
                    :headers="uploadHeaders"
                    :data="uploadData"
                    :before-upload="beforeAudioUpload"
                    :on-success="handleAudioUploadSuccess"
                    :on-error="handleAudioUploadError"
                    :show-file-list="false"
                    accept="audio/*,.mp3,.wav,.ogg,.m4a,.aac"
                    drag
                  >
                    <div class="upload-content">
                      <el-icon class="upload-icon"><Upload /></el-icon>
                      <div class="upload-text">点击或拖拽音频文件到此处上传</div>
                      <div class="upload-hint">支持 MP3、WAV、OGG、M4A、AAC 格式，最大 100MB</div>
                    </div>
                  </el-upload>
                </div>
                
                <!-- 已上传音频信息 -->
                <div v-else class="audio-info-card">
                  <div class="audio-info-content">
                    <el-icon class="audio-icon"><VideoPlay /></el-icon>
                    <div class="audio-details">
                      <div class="audio-name">{{ currentAudioInfo.originalFilename }}</div>
                      <div class="audio-meta">
                        <span class="audio-duration">时长: {{ formatDuration(currentAudioInfo.durationSeconds) }}</span>
                        <span class="audio-size">大小: {{ formatFileSize(currentAudioInfo.fileSize) }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="audio-actions">
                    <el-button size="small" type="primary" plain @click="playAudio">
                      <el-icon><VideoPlay /></el-icon>
                      播放
                    </el-button>
                    <el-button size="small" type="danger" plain @click="removeAudio">
                      <el-icon><Delete /></el-icon>
                      移除
                    </el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="音频时长(秒)">
              <el-input 
                :value="dialogForm.audioDurationSeconds || '未解析'" 
                readonly
                placeholder="音频上传后自动解析"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="答题时长(秒)" prop="timeLimitSeconds">
              <el-input-number 
                v-model="dialogForm.timeLimitSeconds" 
                :min="0" 
                placeholder="秒"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="显示顺序" prop="displayOrder">
              <el-input-number 
                v-model="dialogForm.displayOrder" 
                :min="0" 
                placeholder="显示顺序"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否激活">
              <el-switch v-model="dialogForm.isActive" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="标签" prop="tags">
          <el-input 
            v-model="dialogForm.tags" 
            placeholder="JSON格式标签，如：{&quot;level&quot;: &quot;intermediate&quot;}"
          />
        </el-form-item>
        
        <el-form-item label="元数据" prop="metadata">
          <el-input 
            v-model="dialogForm.metadata" 
            type="textarea"
            :rows="2"
            placeholder="JSON格式元数据"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleDialogSubmit" :loading="dialogSubmitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看对话详情弹窗 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="对话详情"
      width="900px"
    >
      <div v-if="currentDialog" class="dialog-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="对话ID">{{ currentDialog.id }}</el-descriptions-item>
          <el-descriptions-item label="模块ID">{{ currentDialog.moduleId }}</el-descriptions-item>
          <el-descriptions-item label="对话标题">{{ currentDialog.title }}</el-descriptions-item>
          <el-descriptions-item label="对话类型">
            <el-tag :type="getDialogTypeTagType(currentDialog.dialogType)">
              {{ currentDialog.dialogType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="难度等级">
            <el-tag :type="getDifficultyTagType(currentDialog.difficultyLevel)">
              {{ currentDialog.difficultyLevel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentDialog.isActive ? 'success' : 'info'">
              {{ currentDialog.isActive ? '激活' : '未激活' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="音频时长">{{ currentDialog.audioDurationFormatted || '无' }}</el-descriptions-item>
          <el-descriptions-item label="答题时长">{{ currentDialog.timeLimitFormatted || '无限制' }}</el-descriptions-item>
          <el-descriptions-item label="问题数量">{{ currentDialog.questionCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="显示顺序">{{ currentDialog.displayOrder }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentDialog.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(currentDialog.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section" v-if="currentDialog.description">
          <h4>对话描述</h4>
          <p>{{ currentDialog.description }}</p>
        </div>
        
        <div class="detail-section" v-if="currentDialog.dialogText">
          <h4>对话文本</h4>
          <div class="dialog-text">{{ currentDialog.dialogText }}</div>
        </div>
        
        <div class="detail-section" v-if="currentDialog.tags">
          <h4>标签</h4>
          <pre>{{ currentDialog.tags }}</pre>
        </div>
        
        <div class="detail-section" v-if="currentDialog.metadata">
          <h4>元数据</h4>
          <pre>{{ currentDialog.metadata }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Download,
  Upload,
  ArrowDown,
  VideoPlay,
  Delete
} from '@element-plus/icons-vue'
import {
  getAllDialogs,
  createDialog,
  updateDialog,
  deleteDialog,
  toggleDialogActive,
  copyDialog,
  searchDialogs,
  batchDeleteDialogs,
  batchActivateDialogs,
  batchDeactivateDialogs
} from '@/api/lsa-dialogs'
import { 
  uploadMediaFile, 
  getMediaById, 
  formatDuration as mediaFormatDuration,
  formatFileSize as mediaFormatFileSize,
  getPreviewUrl 
} from '@/api/media'
import { useAuthStore } from '@/stores/auth'

// 实例
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

// 音频上传相关
const audioUploadRef = ref(null)
const currentAudioInfo = ref(null)
const uploadAction = '/api/media/upload'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${authStore.token}`
}))
const uploadData = {
  type: 'audio',
  title: '听力对话音频',
  description: '用于听力简答的对话音频'
}

// 搜索表单
const searchForm = reactive({
  keyword: '',
  moduleId: null,
  isActive: null
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 排序数据
const sortData = reactive({
  prop: 'displayOrder',
  order: 'ascending'
})

// 对话弹窗相关
const dialogVisible = ref(false)
const dialogMode = ref('add') // 'add' | 'edit'
const dialogSubmitting = ref(false)
const dialogFormRef = ref(null)
const dialogForm = reactive({
  id: null,
  moduleId: null,
  title: '',
  description: '',
  audioId: null,
  dialogText: '',
  audioDurationSeconds: null,
  timeLimitSeconds: null,
  displayOrder: 0,
  isActive: true,
  tags: '',
  metadata: ''
})

// 表单验证规则
const dialogRules = {
  title: [
    { required: true, message: '请输入对话标题', trigger: 'blur' },
    { max: 255, message: '标题长度不能超过255个字符', trigger: 'blur' }
  ],
  moduleId: [
    { required: true, message: '请选择模块', trigger: 'change' }
  ],
  displayOrder: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' },
    { type: 'number', min: 0, message: '显示顺序不能为负数', trigger: 'blur' }
  ]
}

// 查看对话详情相关
const viewDialogVisible = ref(false)
const currentDialog = ref(null)

// 生命周期
onMounted(() => {
  loadTableData()
})

// 计算属性
const hasSelection = computed(() => selectedRows.value.length > 0)

// 方法
const loadTableData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      sort: [getSortString()]
    }
    
    // 添加搜索条件
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.moduleId !== null) {
      params.moduleId = searchForm.moduleId
    }
    if (searchForm.isActive !== null) {
      params.isActive = searchForm.isActive
    }
    
    const response = await (searchForm.keyword ? 
      searchDialogs(searchForm.keyword, params) : 
      getAllDialogs(params))
    
    if (response.data.code == 200) {
      tableData.value = response.data.data.content || []
      pagination.total = response.data.data.totalElements || 0
    } else {
      ElMessage.error(response.message || '加载数据失败')
    }
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const getSortString = () => {
  if (sortData.order === 'ascending') {
    return `${sortData.prop},asc`
  } else {
    return `${sortData.prop},desc`
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadTableData()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    moduleId: null,
    isActive: null
  })
  pagination.currentPage = 1
  loadTableData()
}

const handleAdd = () => {
  dialogMode.value = 'add'
  resetDialogForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogMode.value = 'edit'
  Object.assign(dialogForm, {
    ...row,
    tags: row.tags || '',
    metadata: row.metadata || ''
  })
  
  // 加载音频信息
  if (row.audioId) {
    await loadAudioInfo(row.audioId)
  } else {
    currentAudioInfo.value = null
  }
  
  dialogVisible.value = true
}

const handleView = (row) => {
  currentDialog.value = row
  viewDialogVisible.value = true
}

const handleCopy = async (row) => {
  try {
    const response = await copyDialog(row.id)
    if (response.success) {
      ElMessage.success('对话复制成功')
      loadTableData()
    } else {
      ElMessage.error(response.message || '复制失败')
    }
  } catch (error) {
    ElMessage.error('复制失败：' + error.message)
  }
}

const handleDelete = async (row) => {
  try {
    const response = await deleteDialog(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      loadTableData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败：' + error.message)
  }
}

const handleToggleStatus = async (row) => {
  row.statusLoading = true
  try {
    const response = await toggleDialogActive(row.id)
    if (response.success) {
      ElMessage.success('状态更新成功')
      // 更新行数据
      Object.assign(row, response.data)
    } else {
      // 恢复原状态
      row.isActive = !row.isActive
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error) {
    // 恢复原状态
    row.isActive = !row.isActive
    ElMessage.error('状态更新失败：' + error.message)
  } finally {
    row.statusLoading = false
  }
}

const handleViewQuestions = (row) => {
  // TODO: 跳转到问题管理页面
  ElMessage.info('问题管理功能待开发')
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleSortChange = ({ prop, order }) => {
  sortData.prop = prop
  sortData.order = order
  loadTableData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadTableData()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadTableData()
}

const handleBatchAction = async (command) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要操作的数据')
    return
  }
  
  const ids = selectedRows.value.map(row => row.id)
  
  try {
    let response
    switch (command) {
      case 'activate':
        await ElMessageBox.confirm(`确定要激活选中的 ${ids.length} 个对话吗？`, '确认操作')
        response = await batchActivateDialogs(ids)
        break
      case 'deactivate':
        await ElMessageBox.confirm(`确定要停用选中的 ${ids.length} 个对话吗？`, '确认操作')
        response = await batchDeactivateDialogs(ids)
        break
      case 'delete':
        await ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 个对话吗？此操作不可恢复！`, '确认删除', {
          type: 'warning'
        })
        response = await batchDeleteDialogs(ids)
        break
    }
    
    if (response.success) {
      ElMessage.success('批量操作成功')
      loadTableData()
    } else {
      ElMessage.error(response.message || '批量操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量操作失败：' + error.message)
    }
  }
}

const handleExport = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能待开发')
}

const handleImport = () => {
  // TODO: 实现导入功能
  ElMessage.info('导入功能待开发')
}

const resetDialogForm = () => {
  Object.assign(dialogForm, {
    id: null,
    moduleId: null,
    title: '',
    description: '',
    audioId: null,
    dialogText: '',
    audioDurationSeconds: null,
    timeLimitSeconds: null,
    displayOrder: 0,
    isActive: true,
    tags: '',
    metadata: ''
  })
  
  // 清理音频信息
  currentAudioInfo.value = null
  
  if (dialogFormRef.value) {
    dialogFormRef.value.clearValidate()
  }
}

const handleDialogClose = () => {
  dialogVisible.value = false
  resetDialogForm()
}

const handleDialogSubmit = async () => {
  if (!dialogFormRef.value) return
  
  try {
    await dialogFormRef.value.validate()
    dialogSubmitting.value = true
    
    const submitData = { ...dialogForm }
    // 清理空值
    if (!submitData.audioId) submitData.audioId = null
    if (!submitData.audioDurationSeconds) submitData.audioDurationSeconds = null
    if (!submitData.timeLimitSeconds) submitData.timeLimitSeconds = null
    if (!submitData.tags) submitData.tags = null
    if (!submitData.metadata) submitData.metadata = null
    
    let response
    if (dialogMode.value === 'add') {
      delete submitData.id
      response = await createDialog(submitData)
    } else {
      response = await updateDialog(submitData.id, submitData)
    }
    
    if (response.success) {
      ElMessage.success(dialogMode.value === 'add' ? '创建成功' : '更新成功')
      handleDialogClose()
      loadTableData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    dialogSubmitting.value = false
  }
}

// 音频上传相关方法
const beforeAudioUpload = (file) => {
  const isAudio = file.type.startsWith('audio/')
  const isLt100M = file.size / 1024 / 1024 < 100

  if (!isAudio) {
    ElMessage.error('请上传音频文件！')
    return false
  }
  if (!isLt100M) {
    ElMessage.error('音频文件大小不能超过 100MB！')
    return false
  }
  
  // 显示上传进度提示
  ElMessage.info('正在上传音频文件，请稍候...')
  return true
}

const handleAudioUploadSuccess = async (response) => {
  try {
    if (response.success && response.data) {
      const audioData = response.data
      
      // 设置音频ID和自动解析的时长
      dialogForm.audioId = audioData.id
      dialogForm.audioDurationSeconds = audioData.durationSeconds || null
      
      // 保存当前音频信息用于显示和播放
      currentAudioInfo.value = audioData
      
      ElMessage.success(`音频上传成功！${audioData.durationSeconds ? `时长: ${formatDuration(audioData.durationSeconds)}` : ''}`)
    } else {
      ElMessage.error(response.message || '音频上传失败')
    }
  } catch (error) {
    ElMessage.error('处理上传结果失败：' + error.message)
  }
}

const handleAudioUploadError = (error) => {
  console.error('音频上传错误:', error)
  ElMessage.error('音频上传失败，请重试')
}

const playAudio = () => {
  if (currentAudioInfo.value && currentAudioInfo.value.id) {
    const audioUrl = getPreviewUrl(currentAudioInfo.value)
    if (audioUrl) {
      const audio = new Audio(audioUrl)
      audio.play().catch(error => {
        console.error('音频播放失败:', error)
        ElMessage.error('音频播放失败')
      })
    } else {
      ElMessage.error('无法获取音频播放地址')
    }
  }
}

const removeAudio = () => {
  ElMessageBox.confirm('确定要移除当前音频吗？', '确认移除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    dialogForm.audioId = null
    dialogForm.audioDurationSeconds = null
    currentAudioInfo.value = null
    ElMessage.success('音频已移除')
  }).catch(() => {
    // 用户取消
  })
}

const loadAudioInfo = async (audioId) => {
  if (!audioId) {
    currentAudioInfo.value = null
    return
  }
  
  try {
    const response = await getMediaById(audioId)
    if (response.success && response.data) {
      currentAudioInfo.value = response.data
    }
  } catch (error) {
    console.error('加载音频信息失败:', error)
  }
}

// 工具方法
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}

const formatDuration = (duration) => {
  return mediaFormatDuration(duration)
}

const formatFileSize = (size) => {
  return mediaFormatFileSize(size)
}

const getDialogTypeTagType = (type) => {
  switch (type) {
    case '短对话': return 'success'
    case '中等对话': return 'warning'
    case '长对话': return 'danger'
    default: return 'info'
  }
}

const getDifficultyTagType = (level) => {
  switch (level) {
    case '简单': return 'success'
    case '中等': return 'warning'
    case '困难': return 'danger'
    default: return 'info'
  }
}
</script>

<style scoped>
.dialog-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.search-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-card {
  background: white;
}

.audio-duration {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 16px;
}

.detail-section p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

.dialog-text {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.detail-section pre {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  overflow-x: auto;
}

/* 音频上传相关样式 */
.audio-upload-wrapper {
  width: 100%;
}

/* 音频上传区域 */
.audio-upload-zone {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
  transition: all 0.3s ease;
  cursor: pointer;
}

.audio-upload-zone:hover {
  border-color: #409eff;
  background: #f7f9fc;
}

.audio-upload :deep(.el-upload-dragger) {
  border: none;
  background: transparent;
  width: 100%;
  height: auto;
  padding: 16px 12px;
  min-height: auto;
}

.upload-content {
  text-align: center;
}

.upload-icon {
  font-size: 28px;
  color: #c0c4cc;
  margin-bottom: 8px;
  display: block;
}

.upload-text {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.upload-hint {
  font-size: 11px;
  color: #999;
  line-height: 1.3;
}

/* 音频信息卡片 */
.audio-info-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 60px;
}

.audio-info-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.audio-icon {
  font-size: 20px;
  color: #67c23a;
  margin-right: 10px;
  flex-shrink: 0;
}

.audio-details {
  flex: 1;
  min-width: 0;
}

.audio-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.audio-meta {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: #909399;
  flex-wrap: wrap;
}

.audio-duration,
.audio-size {
  background: #f5f7fa;
  padding: 1px 4px;
  border-radius: 2px;
  white-space: nowrap;
}

.audio-actions {
  display: flex;
  gap: 6px;
  margin-left: 8px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .search-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-left,
  .search-right {
    justify-content: center;
  }
  
  .audio-info-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    min-height: auto;
  }
  
  .audio-info-content {
    width: 100%;
  }
  
  .audio-actions {
    margin-left: 0;
    justify-content: flex-start;
    width: 100%;
  }
  
  .upload-content {
    padding: 12px 8px;
  }
  
  .upload-icon {
    font-size: 24px;
    margin-bottom: 6px;
  }
  
  .upload-text {
    font-size: 12px;
  }
  
  .upload-hint {
    font-size: 10px;
  }
}
</style>

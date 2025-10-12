<template>
  <div class="dialog-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>对话管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建对话
        </el-button>
        <el-button @click="showImportDialog = true">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="exportDialogs">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索对话标题、内容..."
            @input="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.moduleId" placeholder="选择模块" clearable @change="handleFilter">
            <el-option
              v-for="module in moduleOptions"
              :key="module.value"
              :label="module.label"
              :value="module.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.isActive" placeholder="状态" clearable @change="handleFilter">
            <el-option label="激活" :value="true" />
            <el-option label="未激活" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.dialogType" placeholder="对话类型" clearable @change="handleFilter">
            <el-option label="短对话" value="short" />
            <el-option label="中等对话" value="medium" />
            <el-option label="长对话" value="long" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedDialogs.length > 0">
      <el-alert
        :title="`已选择 ${selectedDialogs.length} 项`"
        type="info"
        show-icon
        :closable="false"
      >
        <template #default>
          <el-button size="small" @click="batchActivate">批量激活</el-button>
          <el-button size="small" @click="batchDeactivate">批量停用</el-button>
          <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
        </template>
      </el-alert>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="dialogList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
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

        <el-table-column label="音频信息" width="150">
          <template #default="scope">
            <div v-if="scope.row.audioId">
              <el-icon><VideoPlay /></el-icon>
              {{ formatAudioDuration(scope.row.audioDurationSeconds) }}
              <br>
              <small>{{ getDialogType(scope.row.audioDurationSeconds) }}</small>
            </div>
            <el-text v-else type="info">无音频</el-text>
          </template>
        </el-table-column>

        <el-table-column label="问题数量" width="100" align="center">
          <template #default="scope">
            <el-badge :value="scope.row.questionCount || 0" class="item">
              <el-button size="small" text @click="manageQuestions(scope.row)">
                <el-icon><QuestionFilled /></el-icon>
              </el-button>
            </el-badge>
          </template>
        </el-table-column>

        <el-table-column label="时间限制" width="100" align="center">
          <template #default="scope">
            {{ formatTimeLimit(scope.row.timeLimitSeconds) }}
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

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDialog(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="editDialog(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              @click="copyDialogAction(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteDialogAction(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="对话标题" prop="title">
              <el-input v-model="dialogForm.title" placeholder="请输入对话标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属模块" prop="moduleId">
              <el-select v-model="dialogForm.moduleId" placeholder="请选择模块" style="width: 100%">
                <el-option
                  v-for="module in moduleOptions"
                  :key="module.value"
                  :label="module.label"
                  :value="module.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

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

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="音频时长(秒)">
              <el-input-number
                v-model="dialogForm.audioDurationSeconds"
                :min="0"
                :max="3600"
                placeholder="音频时长"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="答题时限(秒)">
              <el-input-number
                v-model="dialogForm.timeLimitSeconds"
                :min="0"
                :max="3600"
                placeholder="答题时间限制"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="显示顺序" prop="displayOrder">
              <el-input-number
                v-model="dialogForm.displayOrder"
                :min="1"
                placeholder="显示顺序"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

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
          <el-descriptions-item label="音频时长">
            {{ formatAudioDuration(currentDialog.audioDurationSeconds) }}
          </el-descriptions-item>
          <el-descriptions-item label="答题时限">
            {{ formatTimeLimit(currentDialog.timeLimitSeconds) }}
          </el-descriptions-item>
          <el-descriptions-item label="显示顺序">
            {{ currentDialog.displayOrder }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentDialog.isActive ? 'success' : 'danger'">
              {{ currentDialog.isActive ? '激活' : '停用' }}
            </el-tag>
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
  QuestionFilled,
  UploadFilled
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

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const dialogList = ref([])
const selectedDialogs = ref([])
const currentDialog = ref(null)

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
const dialogForm = reactive(generateDialogTemplate())
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

// 模块选项（模拟数据，实际应该从API获取）
const moduleOptions = ref([
  { value: 1, label: '听力理解模块1' },
  { value: 2, label: '听力理解模块2' },
  { value: 3, label: '听力理解模块3' }
])

// 权限检查
const hasPermission = (role) => {
  return authStore.hasRole(role)
}

// 获取模块名称
const getModuleName = (moduleId) => {
  const module = moduleOptions.value.find(m => m.value === moduleId)
  return module ? module.label : `模块${moduleId}`
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
    if (filterParams.isActive !== null) params.isActive = filterParams.isActive

    let response
    if (searchKeyword.value) {
      response = await searchDialogs(searchKeyword.value, params)
    } else {
      response = await getDialogs(params)
    }

    dialogList.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载对话列表失败：' + error.message)
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
const editDialog = (dialog) => {
  isEdit.value = true
  Object.assign(dialogForm, dialog)
  showCreateDialog.value = true
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
  try {
    await toggleDialogActive(dialog.id)
    ElMessage.success(`对话已${dialog.isActive ? '激活' : '停用'}`)
  } catch (error) {
    dialog.isActive = !dialog.isActive // 恢复状态
    ElMessage.error('状态切换失败：' + error.message)
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
const batchActivate = async () => {
  try {
    const ids = selectedDialogs.value.map(d => d.id)
    await batchActivateDialogs(ids)
    ElMessage.success('批量激活成功')
    loadDialogs()
  } catch (error) {
    ElMessage.error('批量激活失败：' + error.message)
  }
}

const batchDeactivate = async () => {
  try {
    const ids = selectedDialogs.value.map(d => d.id)
    await batchDeactivateDialogs(ids)
    ElMessage.success('批量停用成功')
    loadDialogs()
  } catch (error) {
    ElMessage.error('批量停用失败：' + error.message)
  }
}

const batchDelete = () => {
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
      await updateDialog(dialogForm.id, dialogForm)
      ElMessage.success('对话更新成功')
    } else {
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
  Object.assign(dialogForm, generateDialogTemplate())
  isEdit.value = false
}

// 导出数据
const exportDialogs = async () => {
  try {
    // 这里应该调用导出API，当前为模拟
    ElMessage.info('导出功能正在开发中')
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
  loadDialogs()
})
</script>

<style scoped>
.dialog-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.batch-actions {
  margin-bottom: 20px;
}

.table-section {
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-title .title {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
}

.dialog-detail .dialog-text {
  white-space: pre-wrap;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  display: block;
  margin-top: 8px;
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
</style>

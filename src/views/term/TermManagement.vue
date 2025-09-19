<template>
  <div class="term-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>术语管理</span>
          <div class="header-actions">
            <el-upload
              ref="uploadRef"
              :show-file-list="false"
              :before-upload="beforeUpload"
              accept=".xlsx,.xls,.csv"
              style="display: inline-block; margin-right: 10px;"
            >
              <el-button type="success" :loading="importing">
                <el-icon><Upload /></el-icon>
                导入术语
              </el-button>
            </el-upload>
            <el-button type="info" @click="exportTerms" :loading="exporting">
              <el-icon><Download /></el-icon>
              导出术语
            </el-button>
            <el-button type="primary" @click="createTerm">
              <el-icon><Plus /></el-icon>
              添加术语
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
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索术语、解释"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.category" placeholder="选择分类" clearable>
              <el-option
                v-for="category in categories"
                :key="category"
                :label="category"
                :value="category"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="searchForm.source" placeholder="选择来源" clearable>
              <el-option
                v-for="source in sources"
                :key="source"
                :label="source"
                :value="source"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="难度等级">
            <el-select v-model="searchForm.difficultyLevel" placeholder="选择难度" clearable>
              <el-option
                v-for="i in 5"
                :key="i"
                :label="`${i}级`"
                :value="i"
              />
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
      
      <!-- 术语列表 -->
      <el-table
        v-loading="loading"
        :data="termList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="termText" label="英文术语" min-width="150" />
        <el-table-column prop="termTextZh" label="中文术语" min-width="150" />
        <el-table-column prop="explanation" label="英文解释" min-width="200" show-overflow-tooltip />
        <el-table-column prop="explanationZh" label="中文解释" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">
              {{ row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="difficultyLevel" label="难度" width="80">
          <template #default="{ row }">
            <el-rate
              v-model="row.difficultyLevel"
              disabled
              show-score
              text-color="#ff9900"
              score-template="难度 {value}"
            />
          </template>
        </el-table-column>
        <el-table-column label="启用状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.isEnabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editTerm(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteTerm(row)">
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
    
    <!-- 编辑术语对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editForm.id ? '编辑术语' : '新增术语'"
      width="700px"
      top="5vh"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="英文术语" prop="termText">
              <el-input v-model="editForm.termText" placeholder="请输入英文术语" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中文术语" prop="termTextZh">
              <el-input v-model="editForm.termTextZh" placeholder="请输入中文术语" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="英文解释" prop="explanation">
          <el-input
            v-model="editForm.explanation"
            type="textarea"
            :rows="3"
            placeholder="请输入英文解释"
          />
        </el-form-item>
        
        <el-form-item label="中文解释" prop="explanationZh">
          <el-input
            v-model="editForm.explanationZh"
            type="textarea"
            :rows="3"
            placeholder="请输入中文解释"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="editForm.category" placeholder="选择分类">
                <el-option
                  v-for="category in categories"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-select v-model="editForm.source" placeholder="选择来源" allow-create>
                <el-option
                  v-for="source in sources"
                  :key="source"
                  :label="source"
                  :value="source"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-rate
                v-model="editForm.difficultyLevel"
                :max="5"
                show-text
                text-color="#ff9900"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="isEnabled">
              <el-switch v-model="editForm.isEnabled" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="标签" prop="tags">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        
        <el-form-item label="使用示例" prop="usageExample">
          <el-input
            v-model="editForm.usageExample"
            type="textarea"
            :rows="3"
            placeholder="请输入使用示例"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTerm" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 批量操作对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量操作"
      width="500px"
    >
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="batchForm.action">
            <el-radio label="updateCategory">更新分类</el-radio>
            <el-radio label="updateSource">更新来源</el-radio>
            <el-radio label="updateStatus">更新状态</el-radio>
            <el-radio label="delete">删除</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item
          v-if="batchForm.action === 'updateCategory'"
          label="新分类"
        >
          <el-select v-model="batchForm.category" placeholder="选择分类">
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item
          v-if="batchForm.action === 'updateSource'"
          label="新来源"
        >
          <el-select v-model="batchForm.source" placeholder="选择来源">
            <el-option
              v-for="source in sources"
              :key="source"
              :label="source"
              :value="source"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item
          v-if="batchForm.action === 'updateStatus'"
          label="新状态"
        >
          <el-radio-group v-model="batchForm.isEnabled">
            <el-radio :label="true">启用</el-radio>
            <el-radio :label="false">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="executeBatchOperation" :loading="batchLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { termApi } from '@/api/term'
import {
  Search,
  Refresh,
  Upload,
  Download,
  Plus,
  Delete,
  Edit
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const importing = ref(false)
const exporting = ref(false)
const saving = ref(false)
const batchLoading = ref(false)
const editDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const editFormRef = ref(null)
const uploadRef = ref(null)

const termList = ref([])
const selectedTerms = ref([])

// 选项数据
const categories = termApi.getTermCategories()
const sources = termApi.getTermSources()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  source: '',
  difficultyLevel: ''
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
  termText: '',
  termTextZh: '',
  explanation: '',
  explanationZh: '',
  category: '',
  source: '',
  tags: '',
  difficultyLevel: 2,
  usageExample: '',
  isEnabled: true
})

// 批量操作表单
const batchForm = reactive({
  action: 'updateCategory',
  category: '',
  source: '',
  isEnabled: true
})

// 表单验证规则
const editRules = {
  termText: [
    { required: true, message: '请输入英文术语', trigger: 'blur' }
  ],
  explanation: [
    { required: true, message: '请输入英文解释', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// 计算属性
const hasSelection = computed(() => selectedTerms.value.length > 0)

// 加载术语列表
const loadTerms = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: 'termText,asc',
      ...searchForm
    }
    
    const response = await termApi.getTerms(params)
    
    termList.value = response.content || []
    pagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载术语列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadTerms()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    category: '',
    source: '',
    difficultyLevel: ''
  })
  handleSearch()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadTerms()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadTerms()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedTerms.value = selection
}

// 状态切换
const handleStatusChange = async (term) => {
  try {
    await termApi.updateTerm(term.id, { isEnabled: term.isEnabled })
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 恢复原状态
    term.isEnabled = !term.isEnabled
    ElMessage.error('状态更新失败')
  }
}

// 创建术语
const createTerm = () => {
  Object.assign(editForm, {
    id: null,
    termText: '',
    termTextZh: '',
    explanation: '',
    explanationZh: '',
    category: '',
    source: '',
    tags: '',
    difficultyLevel: 2,
    usageExample: '',
    isEnabled: true
  })
  editDialogVisible.value = true
}

// 编辑术语
const editTerm = (term) => {
  Object.assign(editForm, {
    id: term.id,
    termText: term.termText,
    termTextZh: term.termTextZh,
    explanation: term.explanation,
    explanationZh: term.explanationZh,
    category: term.category,
    source: term.source,
    tags: term.tags || '',
    difficultyLevel: term.difficultyLevel || 2,
    usageExample: term.usageExample || '',
    isEnabled: term.isEnabled
  })
  editDialogVisible.value = true
}

// 保存术语
const saveTerm = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    if (editForm.id) {
      await termApi.updateTerm(editForm.id, editForm)
      ElMessage.success('术语更新成功')
    } else {
      await termApi.createTerm(editForm)
      ElMessage.success('术语创建成功')
    }
    
    editDialogVisible.value = false
    loadTerms()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除术语
const deleteTerm = (term) => {
  ElMessageBox.confirm(
    `确定要删除术语 "${term.termText}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await termApi.deleteTerm(term.id)
      ElMessage.success('删除成功')
      loadTerms()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const batchDelete = () => {
  if (selectedTerms.value.length === 0) {
    ElMessage.warning('请选择要操作的术语')
    return
  }
  batchDialogVisible.value = true
}

// 执行批量操作
const executeBatchOperation = async () => {
  try {
    batchLoading.value = true
    
    const termIds = selectedTerms.value.map(term => term.id)
    const data = {
      action: batchForm.action,
      termIds
    }
    
    if (batchForm.action === 'updateCategory') {
      data.category = batchForm.category
    } else if (batchForm.action === 'updateSource') {
      data.source = batchForm.source
    } else if (batchForm.action === 'updateStatus') {
      data.isEnabled = batchForm.isEnabled
    }
    
    await termApi.batchOperateTerms(data)
    
    ElMessage.success('批量操作成功')
    batchDialogVisible.value = false
    loadTerms()
  } catch (error) {
    ElMessage.error('批量操作失败')
  } finally {
    batchLoading.value = false
  }
}

// 导入术语
const beforeUpload = async (file) => {
  try {
    importing.value = true
    await termApi.importTerms(file)
    ElMessage.success('导入成功')
    loadTerms()
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
  return false
}

// 导出术语
const exportTerms = async () => {
  try {
    exporting.value = true
    
    const blob = await termApi.exportTerms(searchForm)
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `terms_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 获取分类类型
const getCategoryType = (category) => {
  const typeMap = {
    COMMUNICATION: 'primary',
    NAVIGATION: 'success',
    METEOROLOGY: 'info',
    EMERGENCY: 'danger',
    AIRCRAFT: 'warning',
    AIRPORT: '',
    PROCEDURES: 'success',
    EQUIPMENT: 'info',
    GENERAL: ''
  }
  return typeMap[category] || ''
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadTerms()
})
</script>

<style scoped>
.term-management {
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
  flex-wrap: wrap;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .header-actions .el-button {
    width: 100%;
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

<template>
  <div class="terminology-type-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>术语类型管理</h2>
      <p>管理术语主题分类和层级结构</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加主题
        </el-button>
        <el-button @click="showCreateDialog(true)">
          <el-icon><FolderAdd /></el-icon>
          添加子主题
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedTopics.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="expandAll">
          <el-icon><DCaret /></el-icon>
          展开全部
        </el-button>
        <el-button @click="collapseAll">
          <el-icon><CaretRight /></el-icon>
          收起全部
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索主题..."
          style="width: 200px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadTopics">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="statistics">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="总主题数" :value="statistics.totalCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="根主题数" :value="statistics.rootCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已关联术语" :value="statistics.mappedTermCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="最大层级" :value="statistics.maxDepth || 0" />
        </el-col>
      </el-row>
    </div>

    <!-- 主题数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="filteredTopicList"
        @selection-change="handleSelectionChange"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column 
          prop="nameZh" 
          label="中文名称" 
          width="200"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div class="topic-name">
              <span class="name-zh">{{ scope.row.nameZh }}</span>
              <span v-if="scope.row.nameEn" class="name-en">({{ scope.row.nameEn }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column 
          prop="code" 
          label="主题代码" 
          width="150"
          show-overflow-tooltip
        >
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          prop="description" 
          label="描述" 
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column 
          prop="displayOrder" 
          label="显示顺序" 
          width="100"
          align="center"
        />
        <el-table-column 
          label="层级" 
          width="80"
          align="center"
        >
          <template #default="scope">
            <el-tag size="small" :type="getDepthType(scope.row.depth)">
              {{ scope.row.depth || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          label="术语数量" 
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-badge :value="scope.row.termCount || 0" type="primary" />
          </template>
        </el-table-column>
        <el-table-column 
          prop="createdAt" 
          label="创建时间" 
          width="180"
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleAddChild(scope.row)"
            >
              添加子项
            </el-button>
            <el-button 
              size="small" 
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              @click="handleViewTerms(scope.row)"
            >
              查看术语
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 主题详情/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="getDialogTitle()"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="topicForm"
        :model="currentTopic"
        :rules="topicRules"
        label-width="120px"
        :disabled="dialogMode === 'view'"
      >
        <el-form-item label="父级主题" v-if="currentTopic.parentId">
          <el-tree-select
            v-model="currentTopic.parentId"
            :data="topicSelectOptions"
            :props="{ label: 'nameZh', value: 'id' }"
            placeholder="选择父级主题"
            clearable
            check-strictly
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>

        <el-form-item label="主题代码" prop="code">
          <el-input 
            v-model="currentTopic.code" 
            placeholder="输入主题代码，如：ATC_CLEARANCE"
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="中文名称" prop="nameZh">
              <el-input v-model="currentTopic.nameZh" placeholder="输入中文名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文名称">
              <el-input v-model="currentTopic.nameEn" placeholder="输入英文名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="显示顺序" prop="displayOrder">
          <el-input-number 
            v-model="currentTopic.displayOrder" 
            :min="0" 
            :max="9999"
            placeholder="显示顺序"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="主题描述">
          <el-input
            v-model="currentTopic.description"
            type="textarea"
            :rows="4"
            placeholder="输入主题描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          v-if="dialogMode !== 'view'" 
          type="primary" 
          @click="handleSaveTopic"
          :loading="saveLoading"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 关联术语对话框 -->
    <el-dialog
      v-model="termMappingVisible"
      title="关联术语管理"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="mapping-section">
        <div class="section-header">
          <h4>主题：{{ selectedTopicForMapping?.nameZh }}</h4>
          <div class="header-actions">
            <el-button type="primary" @click="showAddTermDialog">
              <el-icon><Plus /></el-icon>
              添加术语
            </el-button>
            <el-button @click="loadMappedTerms(selectedTopicForMapping?.id)">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
        
        <el-table
          :data="mappedTerms"
          v-loading="mappingLoading"
          @selection-change="handleTermSelection"
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="headword" label="术语" width="150">
            <template #default="scope">
              <div class="term-display">
                <span class="headword">{{ scope.row.headword }}</span>
                <span v-if="scope.row.ipa" class="ipa">[{{ scope.row.ipa }}]</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="definitionZh" label="中文释义" min-width="200" />
          <el-table-column prop="pos" label="词性" width="80" />
          <el-table-column prop="cefrLevel" label="CEFR" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.cefrLevel" size="small">
                {{ scope.row.cefrLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="主要归属" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.isPrimary" type="primary" size="small">主要</el-tag>
              <el-tag v-else type="info" size="small">次要</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button 
                size="small" 
                @click="togglePrimaryMapping(scope.row)"
              >
                {{ scope.row.isPrimary ? '设为次要' : '设为主要' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mapping-actions">
          <el-button 
            type="danger" 
            :disabled="selectedMappedTerms.length === 0"
            @click="handleRemoveTermMapping"
          >
            移除选中术语
          </el-button>
        </div>
      </div>

      <template #footer>
        <el-button @click="termMappingVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加术语到主题对话框 -->
    <el-dialog v-model="addTermVisible" title="添加术语到主题" width="800px">
      <div class="add-term-section">
        <div class="search-section">
          <el-input
            v-model="termSearchKeyword"
            placeholder="搜索术语..."
            @keyup.enter="searchAvailableTerms"
          >
            <template #suffix>
              <el-button @click="searchAvailableTerms">搜索</el-button>
            </template>
          </el-input>
        </div>

        <el-table
          :data="availableTerms"
          v-loading="availableTermsLoading"
          @selection-change="handleAvailableTermSelection"
          max-height="400"
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="headword" label="术语" width="150" />
          <el-table-column prop="definitionZh" label="中文释义" min-width="200" />
          <el-table-column prop="pos" label="词性" width="80" />
          <el-table-column prop="cefrLevel" label="CEFR" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.cefrLevel" size="small">
                {{ scope.row.cefrLevel }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="addTermVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleAddTermsToTopic"
          :loading="addTermLoading"
          :disabled="selectedAvailableTerms.length === 0"
        >
          添加选中术语
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
  Search,
  Delete,
  Refresh,
  FolderAdd,
  DCaret,
  CaretRight
} from '@element-plus/icons-vue'
import {
  getTermTopics,
  createTermTopic,
  updateTermTopic,
  deleteTermTopic,
  getTermsByTopicId,
  batchAddToTopic,
  batchRemoveFromTopic,
  setPrimaryTopic,
  getTerms
} from '@/api/term'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const mappingLoading = ref(false)
const availableTermsLoading = ref(false)
const addTermLoading = ref(false)
const dialogVisible = ref(false)
const termMappingVisible = ref(false)
const addTermVisible = ref(false)
const dialogMode = ref('view') // view, edit, create
const searchKeyword = ref('')
const termSearchKeyword = ref('')

const topicList = ref([])
const filteredTopicList = ref([])
const selectedTopics = ref([])
const statistics = ref({})
const topicSelectOptions = ref([])
const mappedTerms = ref([])
const selectedMappedTerms = ref([])
const availableTerms = ref([])
const selectedAvailableTerms = ref([])
const selectedTopicForMapping = ref(null)

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 当前主题
const currentTopic = reactive({
  parentId: null,
  code: '',
  nameZh: '',
  nameEn: '',
  description: '',
  displayOrder: 0
})

// 表单验证规则
const topicRules = {
  code: [
    { required: true, message: '请输入主题代码', trigger: 'blur' },
    { max: 100, message: '主题代码不能超过100个字符', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '主题代码只能包含大写字母、数字和下划线', trigger: 'blur' }
  ],
  nameZh: [
    { required: true, message: '请输入中文名称', trigger: 'blur' },
    { max: 200, message: '中文名称不能超过200个字符', trigger: 'blur' }
  ],
  displayOrder: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' },
    { type: 'number', min: 0, message: '显示顺序不能小于0', trigger: 'blur' }
  ]
}

// 计算属性
const getDialogTitle = computed(() => {
  return () => {
    const modeMap = {
      create: '添加主题',
      edit: '编辑主题',
      view: '查看主题'
    }
    return modeMap[dialogMode.value] || '主题详情'
  }
})

const formatDateTime = computed(() => {
  return (dateTime) => {
    if (!dateTime) return '-'
    return new Date(dateTime).toLocaleString('zh-CN')
  }
})

// 获取深度标签类型
const getDepthType = (depth) => {
  if (depth === 0) return 'primary'
  if (depth === 1) return 'success'
  if (depth === 2) return 'warning'
  return 'danger'
}

// 模板引用
const topicForm = ref(null)

// 方法
const loadTopics = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: 'displayOrder',
      direction: 'ASC'
    }
    
    const response = await getTermTopics(params)
    topicList.value = response.data.content
    pagination.total = response.data.totalElements
    
    // 构建层级结构和选项数据
    buildTopicHierarchy()
    topicSelectOptions.value = topicList.value
  } catch (error) {
    ElMessage.error('加载主题列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const buildTopicHierarchy = () => {
  // 简化处理，假设数据已经是扁平结构
  // 实际项目中可能需要根据parentId构建树形结构
  filteredTopicList.value = topicList.value.map(topic => ({
    ...topic,
    depth: topic.parentId ? 1 : 0,
    termCount: 0 // 需要从后端获取
  }))
}

const loadStatistics = async () => {
  try {
    // 计算统计信息
    const totalCount = topicList.value.length
    const rootCount = topicList.value.filter(topic => !topic.parentId).length
    
    statistics.value = {
      totalCount,
      rootCount,
      mappedTermCount: 0, // 需要从后端获取
      maxDepth: Math.max(...filteredTopicList.value.map(topic => topic.depth || 0))
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const showCreateDialog = (isChild = false) => {
  dialogMode.value = 'create'
  resetCurrentTopic()
  
  if (isChild && selectedTopics.value.length > 0) {
    currentTopic.parentId = selectedTopics.value[0].id
  }
  
  dialogVisible.value = true
}

const handleAddChild = (parent) => {
  dialogMode.value = 'create'
  resetCurrentTopic()
  currentTopic.parentId = parent.id
  dialogVisible.value = true
}

const handleEdit = (topic) => {
  dialogMode.value = 'edit'
  Object.assign(currentTopic, topic)
  dialogVisible.value = true
}

const handleDelete = async (topic) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除主题"${topic.nameZh}"吗？\n注意：删除主题会同时删除其所有子主题和相关映射关系。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTermTopic(topic.id)
    ElMessage.success('删除成功')
    loadTopics()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedTopics.value.length === 0) {
    ElMessage.warning('请先选择要删除的主题')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedTopics.value.length} 个主题吗？\n注意：删除主题会同时删除其所有子主题和相关映射关系。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 批量删除逻辑
    for (const topic of selectedTopics.value) {
      await deleteTermTopic(topic.id)
    }
    
    ElMessage.success('批量删除成功')
    selectedTopics.value = []
    loadTopics()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error(error)
    }
  }
}

const handleSaveTopic = async () => {
  try {
    await topicForm.value.validate()
    saveLoading.value = true
    
    if (dialogMode.value === 'create') {
      await createTermTopic(currentTopic)
      ElMessage.success('创建成功')
    } else {
      await updateTermTopic(currentTopic.id, currentTopic)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    loadTopics()
    loadStatistics()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('保存失败')
    }
    console.error(error)
  } finally {
    saveLoading.value = false
  }
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    filteredTopicList.value = topicList.value
    return
  }
  
  filteredTopicList.value = topicList.value.filter(topic => 
    topic.nameZh.includes(searchKeyword.value) ||
    topic.nameEn?.includes(searchKeyword.value) ||
    topic.code.includes(searchKeyword.value)
  )
}

const handleSelectionChange = (selection) => {
  selectedTopics.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadTopics()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadTopics()
}

const expandAll = () => {
  // 展开所有节点的逻辑
  ElMessage.info('展开全部功能待实现')
}

const collapseAll = () => {
  // 收起所有节点的逻辑
  ElMessage.info('收起全部功能待实现')
}

const resetCurrentTopic = () => {
  Object.assign(currentTopic, {
    parentId: null,
    code: '',
    nameZh: '',
    nameEn: '',
    description: '',
    displayOrder: 0
  })
}

// 术语映射相关方法
const handleViewTerms = async (topic) => {
  selectedTopicForMapping.value = topic
  await loadMappedTerms(topic.id)
  termMappingVisible.value = true
}

const loadMappedTerms = async (topicId) => {
  try {
    mappingLoading.value = true
    const response = await getTermsByTopicId(topicId, { page: 0, size: 1000 })
    mappedTerms.value = response.data.content.map(term => ({
      ...term,
      isPrimary: false // 需要从映射关系中获取
    }))
  } catch (error) {
    ElMessage.error('加载关联术语失败')
    console.error(error)
  } finally {
    mappingLoading.value = false
  }
}

const handleTermSelection = (selection) => {
  selectedMappedTerms.value = selection
}

const togglePrimaryMapping = async (term) => {
  try {
    if (!term.isPrimary) {
      await setPrimaryTopic(term.id, selectedTopicForMapping.value.id)
      ElMessage.success('已设为主要归属')
    } else {
      ElMessage.info('取消主要归属功能待实现')
    }
    
    await loadMappedTerms(selectedTopicForMapping.value.id)
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

const handleRemoveTermMapping = async () => {
  if (selectedMappedTerms.value.length === 0) {
    ElMessage.warning('请先选择要移除的术语')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要移除选中的 ${selectedMappedTerms.value.length} 个术语的关联关系吗？`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const termIds = selectedMappedTerms.value.map(term => term.id)
    await batchRemoveFromTopic(termIds, selectedTopicForMapping.value.id)
    ElMessage.success('移除成功')
    
    selectedMappedTerms.value = []
    await loadMappedTerms(selectedTopicForMapping.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
      console.error(error)
    }
  }
}

const showAddTermDialog = () => {
  termSearchKeyword.value = ''
  availableTerms.value = []
  selectedAvailableTerms.value = []
  addTermVisible.value = true
}

const searchAvailableTerms = async () => {
  try {
    availableTermsLoading.value = true
    const params = {
      page: 0,
      size: 100
    }
    
    if (termSearchKeyword.value.trim()) {
      params.headword = termSearchKeyword.value
    }
    
    const response = await getTerms(params)
    availableTerms.value = response.data.content
  } catch (error) {
    ElMessage.error('搜索术语失败')
    console.error(error)
  } finally {
    availableTermsLoading.value = false
  }
}

const handleAvailableTermSelection = (selection) => {
  selectedAvailableTerms.value = selection
}

const handleAddTermsToTopic = async () => {
  try {
    addTermLoading.value = true
    const termIds = selectedAvailableTerms.value.map(term => term.id)
    
    await batchAddToTopic(termIds, selectedTopicForMapping.value.id, false)
    ElMessage.success('添加术语成功')
    
    addTermVisible.value = false
    await loadMappedTerms(selectedTopicForMapping.value.id)
  } catch (error) {
    ElMessage.error('添加术语失败')
    console.error(error)
  } finally {
    addTermLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadTopics()
  loadStatistics()
})
</script>

<style scoped>
.terminology-type-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.statistics {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.topic-name {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-zh {
  font-weight: 500;
  color: #303133;
}

.name-en {
  color: #909399;
  font-size: 12px;
}

.pagination {
  padding: 20px;
  text-align: center;
  border-top: 1px solid #e9ecef;
}

.mapping-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.section-header h4 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.mapping-actions {
  margin-top: 16px;
  text-align: center;
}

.term-display {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.headword {
  font-weight: 500;
  color: #303133;
}

.ipa {
  font-size: 12px;
  color: #909399;
  font-style: italic;
}

.add-term-section {
  margin-bottom: 20px;
}

.search-section {
  margin-bottom: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .toolbar-left,
  .toolbar-right {
    justify-content: center;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
}
</style>

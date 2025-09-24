<template>
  <div class="vocabulary-type-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>词汇类型管理</h2>
      <p>管理词汇主题分类和层级结构</p>
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
          <el-statistic title="已关联词汇" :value="statistics.mappedVocabCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="最大层级" :value="statistics.maxDepth || 0" />
        </el-col>
      </el-row>
    </div>

    <!-- 主题树形结构 -->
    <div class="tree-container">
      <el-tree
        ref="topicTree"
        v-loading="loading"
        :data="topicTreeData"
        :props="treeProps"
        :expand-on-click-node="false"
        :check-on-click-node="false"
        show-checkbox
        node-key="id"
        @check-change="handleNodeCheck"
        @node-click="handleNodeClick"
        class="topic-tree"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-content">
              <div class="node-info">
                <span class="node-title">{{ data.nameZh }}</span>
                <span v-if="data.nameEn" class="node-subtitle">({{ data.nameEn }})</span>
                <el-tag size="small" class="node-code">{{ data.code }}</el-tag>
              </div>
              <div class="node-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click.stop="handleAddChild(data)"
                >
                  添加子项
                </el-button>
                <el-button 
                  size="small" 
                  @click.stop="handleEdit(data)"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click.stop="handleDelete(data)"
                >
                  删除
                </el-button>
              </div>
            </div>
            <div v-if="data.description" class="node-description">
              {{ data.description }}
            </div>
          </div>
        </template>
      </el-tree>

      <!-- 空状态 -->
      <div v-if="!loading && topicTreeData.length === 0" class="empty-state">
        <el-empty description="暂无主题数据">
          <el-button type="primary" @click="showCreateDialog">添加第一个主题</el-button>
        </el-empty>
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

    <!-- 关联词汇对话框 -->
    <el-dialog
      v-model="vocabMappingVisible"
      title="关联词汇管理"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="mapping-section">
        <div class="section-header">
          <h4>主题：{{ selectedTopicForMapping?.nameZh }}</h4>
          <el-button type="primary" @click="showAddVocabDialog">
            <el-icon><Plus /></el-icon>
            添加词汇
          </el-button>
        </div>
        
        <el-table
          :data="mappedVocabs"
          v-loading="mappingLoading"
          @selection-change="handleVocabSelection"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="headword" label="词汇" width="150" />
          <el-table-column prop="definitionZh" label="中文释义" />
          <el-table-column label="主要归属" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.isPrimary" type="primary" size="small">主要</el-tag>
              <el-tag v-else type="info" size="small">次要</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
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
            :disabled="selectedMappedVocabs.length === 0"
            @click="handleRemoveVocabMapping"
          >
            移除选中词汇
          </el-button>
        </div>
      </div>

      <template #footer>
        <el-button @click="vocabMappingVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Edit,
  Delete,
  Refresh,
  FolderAdd,
  DCaret,
  CaretRight
} from '@element-plus/icons-vue'
import {
  getVocabTopics,
  createVocabTopic,
  updateVocabTopic,
  deleteVocabTopic,
  deleteVocabTopicsBatch,
  getRootVocabTopics,
  getVocabTopicHierarchy,
  searchVocabTopics,
  getVocabTopicStatistics,
  getVocabTopicMappingsByTopicId,
  addVocabsToTopic,
  removeVocabsFromTopic,
  setPrimaryTopic
} from '@/api/vocab'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const mappingLoading = ref(false)
const dialogVisible = ref(false)
const vocabMappingVisible = ref(false)
const dialogMode = ref('view') // view, edit, create
const searchKeyword = ref('')

const topicTreeData = ref([])
const selectedTopics = ref([])
const statistics = ref({})
const topicSelectOptions = ref([])
const mappedVocabs = ref([])
const selectedMappedVocabs = ref([])
const selectedTopicForMapping = ref(null)

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'nameZh',
  disabled: false
}

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

// 模板引用
const topicForm = ref(null)
const topicTree = ref(null)

// 方法
const loadTopics = async () => {
  try {
    loading.value = true
    const response = await getVocabTopicHierarchy()
    topicTreeData.value = response.data
    
    // 同时加载平铺的选项数据（用于父级选择）
    const flatResponse = await getVocabTopics({ page: 0, size: 1000 })
    topicSelectOptions.value = flatResponse.data.content
  } catch (error) {
    ElMessage.error('加载主题列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await getVocabTopicStatistics()
    statistics.value = response.data
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
    
    await deleteVocabTopic(topic.id)
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
    
    const ids = selectedTopics.value.map(topic => topic.id)
    await deleteVocabTopicsBatch(ids)
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
      await createVocabTopic(currentTopic)
      ElMessage.success('创建成功')
    } else {
      await updateVocabTopic(currentTopic.id, currentTopic)
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

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadTopics()
    return
  }
  
  try {
    loading.value = true
    const response = await searchVocabTopics({
      keyword: searchKeyword.value,
      page: 0,
      size: 1000
    })
    
    // 将搜索结果转换为树形结构
    topicTreeData.value = response.data.content.map(topic => ({
      ...topic,
      children: []
    }))
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleNodeCheck = (data, checked, indeterminate) => {
  // 处理节点选择
  const checkedNodes = topicTree.value.getCheckedNodes()
  selectedTopics.value = checkedNodes
}

const handleNodeClick = (data) => {
  // 点击节点时的处理
  console.log('点击节点:', data)
}

const expandAll = () => {
  // 展开所有节点
  const expandAllNodes = (nodes) => {
    nodes.forEach(node => {
      topicTree.value.store.nodesMap[node.id].expanded = true
      if (node.children && node.children.length > 0) {
        expandAllNodes(node.children)
      }
    })
  }
  expandAllNodes(topicTreeData.value)
}

const collapseAll = () => {
  // 收起所有节点
  const collapseAllNodes = (nodes) => {
    nodes.forEach(node => {
      topicTree.value.store.nodesMap[node.id].expanded = false
      if (node.children && node.children.length > 0) {
        collapseAllNodes(node.children)
      }
    })
  }
  collapseAllNodes(topicTreeData.value)
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

// 词汇映射相关方法
const showVocabMapping = async (topic) => {
  selectedTopicForMapping.value = topic
  await loadMappedVocabs(topic.id)
  vocabMappingVisible.value = true
}

const loadMappedVocabs = async (topicId) => {
  try {
    mappingLoading.value = true
    const response = await getVocabTopicMappingsByTopicId(topicId)
    mappedVocabs.value = response.data.map(mapping => ({
      ...mapping.vocab,
      isPrimary: mapping.isPrimary,
      mappingId: mapping.id
    }))
  } catch (error) {
    ElMessage.error('加载关联词汇失败')
    console.error(error)
  } finally {
    mappingLoading.value = false
  }
}

const handleVocabSelection = (selection) => {
  selectedMappedVocabs.value = selection
}

const togglePrimaryMapping = async (vocab) => {
  try {
    if (!vocab.isPrimary) {
      await setPrimaryTopic(vocab.id, selectedTopicForMapping.value.id)
      ElMessage.success('已设为主要归属')
    } else {
      // 取消主要归属的逻辑需要在后端实现
      ElMessage.info('取消主要归属功能待实现')
    }
    
    // 重新加载数据
    await loadMappedVocabs(selectedTopicForMapping.value.id)
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

const handleRemoveVocabMapping = async () => {
  if (selectedMappedVocabs.value.length === 0) {
    ElMessage.warning('请先选择要移除的词汇')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要移除选中的 ${selectedMappedVocabs.value.length} 个词汇的关联关系吗？`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const vocabIds = selectedMappedVocabs.value.map(vocab => vocab.id)
    await removeVocabsFromTopic(selectedTopicForMapping.value.id, vocabIds)
    ElMessage.success('移除成功')
    
    selectedMappedVocabs.value = []
    await loadMappedVocabs(selectedTopicForMapping.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
      console.error(error)
    }
  }
}

const showAddVocabDialog = () => {
  // 显示添加词汇到主题的对话框
  ElMessage.info('添加词汇功能待实现')
}

// 生命周期
onMounted(() => {
  loadTopics()
  loadStatistics()
})
</script>

<style scoped>
.vocabulary-type-management {
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

.tree-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  min-height: 400px;
}

.topic-tree {
  padding: 20px;
}

.tree-node {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.node-title {
  font-weight: 500;
  color: #303133;
}

.node-subtitle {
  color: #909399;
  font-size: 12px;
}

.node-code {
  font-family: 'Courier New', monospace;
  font-size: 11px;
}

.node-actions {
  display: flex;
  gap: 8px;
}

.node-description {
  color: #606266;
  font-size: 12px;
  line-height: 1.4;
  padding-left: 20px;
  border-left: 2px solid #e4e7ed;
  margin-left: 10px;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
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

.mapping-actions {
  margin-top: 16px;
  text-align: center;
}

/* 自定义树形组件样式 */
:deep(.el-tree-node__content) {
  height: auto;
  min-height: 40px;
  padding: 8px 0;
}

:deep(.el-tree-node__expand-icon) {
  margin-right: 8px;
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

  .node-content {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .node-actions {
    justify-content: center;
  }
}
</style>

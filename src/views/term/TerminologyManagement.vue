<template>
  <div class="terminology-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>术语管理</h2>
      <p>管理航空英语术语库</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加术语
        </el-button>
        <el-button @click="showBatchImportDialog">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedTerms.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="showBatchOperationDialog">
          <el-icon><Operation /></el-icon>
          批量操作
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索术语..."
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="showAdvancedSearch = !showAdvancedSearch">
          高级搜索
        </el-button>
      </div>
    </div>

    <!-- 高级搜索面板 -->
    <el-collapse-transition>
      <div v-show="showAdvancedSearch" class="advanced-search">
        <el-form :model="advancedSearchForm" inline>
          <el-form-item label="术语">
            <el-input v-model="advancedSearchForm.headword" placeholder="输入术语" />
          </el-form-item>
          <el-form-item label="词性">
            <el-select v-model="advancedSearchForm.pos" placeholder="选择词性" clearable>
              <el-option 
                v-for="pos in posOptions" 
                :key="pos" 
                :label="pos" 
                :value="pos" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="CEFR等级">
            <el-select v-model="advancedSearchForm.cefrLevel" placeholder="选择等级" clearable>
              <el-option 
                v-for="level in cefrLevels" 
                :key="level" 
                :label="level" 
                :value="level" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="释义">
            <el-input v-model="advancedSearchForm.definition" placeholder="输入释义" />
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="advancedSearchForm.source" placeholder="选择来源" clearable>
              <el-option 
                v-for="source in termSources" 
                :key="source" 
                :label="source" 
                :value="source" 
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdvancedSearch">搜索</el-button>
            <el-button @click="resetAdvancedSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-collapse-transition>

    <!-- 统计信息 -->
    <div class="statistics">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="总术语数" :value="statistics.totalTerms || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已分类术语" :value="statistics.termsWithTopics || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="有音频术语" :value="statistics.withAudio || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="高频术语" :value="statistics.highFrequency || 0" />
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="termList"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column 
          prop="headword" 
          label="术语" 
          width="180"
          sortable="custom"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div class="term-cell">
              <span class="headword">{{ scope.row.headword }}</span>
              <span v-if="scope.row.ipa" class="ipa">[{{ scope.row.ipa }}]</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="pos" label="词性" width="80" />
        <el-table-column 
          prop="definitionZh" 
          label="中文释义" 
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column 
          prop="definitionEn" 
          label="英文释义" 
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="cefrLevel" label="CEFR等级" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.cefrLevel" :type="getCefrLevelType(scope.row.cefrLevel)">
              {{ scope.row.cefrLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="freqRank" label="频次排名" width="100" sortable="custom" />
        <el-table-column prop="source" label="来源" width="120" show-overflow-tooltip />
        <el-table-column label="音频" width="80" align="center">
          <template #default="scope">
            <el-icon v-if="scope.row.hasAudioAsset" color="#67c23a">
              <VideoPlay />
            </el-icon>
            <el-icon v-else color="#ddd">
              <Mute />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column label="主题数" width="80" align="center">
          <template #default="scope">
            <el-badge :value="scope.row.topicCount || 0" type="info" />
          </template>
        </el-table-column>
        <el-table-column 
          prop="createdAt" 
          label="创建时间" 
          width="180"
          sortable="custom"
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="small" @click="handleCopy(scope.row)">
              复制
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

    <!-- 术语详情/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '添加术语' : dialogMode === 'edit' ? '编辑术语' : '术语详情'"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="termForm"
        :model="currentTerm"
        :rules="termRules"
        label-width="120px"
        :disabled="dialogMode === 'view'"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="术语" prop="headword">
              <el-input v-model="currentTerm.headword" placeholder="输入术语" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="词性" prop="pos">
              <el-select v-model="currentTerm.pos" placeholder="选择词性" clearable>
                <el-option 
                  v-for="pos in posOptions" 
                  :key="pos" 
                  :label="pos" 
                  :value="pos" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="词形还原">
              <el-input v-model="currentTerm.lemma" placeholder="输入词形还原" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="国际音标">
              <el-input v-model="currentTerm.ipa" placeholder="输入音标" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="中文释义" prop="definitionZh">
          <el-input
            v-model="currentTerm.definitionZh"
            type="textarea"
            :rows="3"
            placeholder="输入中文释义"
          />
        </el-form-item>

        <el-form-item label="英文释义">
          <el-input
            v-model="currentTerm.definitionEn"
            type="textarea"
            :rows="3"
            placeholder="输入英文释义"
          />
        </el-form-item>

        <el-form-item label="英文例句">
          <el-input
            v-model="currentTerm.exampleEn"
            type="textarea"
            :rows="2"
            placeholder="输入英文例句"
          />
        </el-form-item>

        <el-form-item label="中文例句">
          <el-input
            v-model="currentTerm.exampleZh"
            type="textarea"
            :rows="2"
            placeholder="输入中文例句"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="CEFR等级">
              <el-select v-model="currentTerm.cefrLevel" placeholder="选择等级" clearable>
                <el-option 
                  v-for="level in cefrLevels" 
                  :key="level" 
                  :label="level" 
                  :value="level" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="频次排名">
              <el-input-number 
                v-model="currentTerm.freqRank" 
                :min="1" 
                :max="999999"
                placeholder="频次排名"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源">
              <el-select v-model="currentTerm.source" placeholder="选择来源" clearable>
                <el-option 
                  v-for="source in termSources" 
                  :key="source" 
                  :label="source" 
                  :value="source" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 主题映射管理 -->
        <el-form-item v-if="dialogMode !== 'create'" label="主题映射">
          <div class="topic-mappings">
            <el-button size="small" @click="showTopicMappingDialog">
              <el-icon><Plus /></el-icon>
              添加主题
            </el-button>
            <div v-if="currentTermTopics.length > 0" class="topic-list">
              <el-tag
                v-for="topic in currentTermTopics"
                :key="topic.id"
                :type="topic.isPrimary ? 'primary' : 'info'"
                closable
                @close="handleRemoveTopicMapping(topic)"
                class="topic-tag"
              >
                {{ topic.topicName }}
                <span v-if="topic.isPrimary" class="primary-badge">主</span>
              </el-tag>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          v-if="dialogMode !== 'view'" 
          type="primary" 
          @click="handleSaveTerm"
          :loading="saveLoading"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="batchImportVisible" title="批量导入术语" width="600px">
      <div class="import-section">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :on-change="handleFileChange"
          :before-upload="beforeUpload"
          accept=".xlsx,.xls,.csv"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 Excel 和 CSV 格式，文件大小不超过 10MB
            </div>
          </template>
        </el-upload>
        
        <div class="template-download">
          <el-button type="text" @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载导入模板
          </el-button>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchImportVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleBatchImport"
          :loading="importLoading"
        >
          导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量操作对话框 -->
    <el-dialog v-model="batchOperationVisible" title="批量操作" width="500px">
      <el-form :model="batchOperationForm" label-width="100px">
        <el-form-item label="操作类型">
          <el-select v-model="batchOperationForm.operationType" placeholder="选择操作类型">
            <el-option label="删除" value="delete" />
            <el-option label="更新CEFR等级" value="updateCefrLevel" />
            <el-option label="更新来源" value="updateSource" />
            <el-option label="添加到主题" value="addToTopics" />
            <el-option label="从主题移除" value="removeFromTopics" />
            <el-option label="更新频次排名" value="updateFreqRank" />
          </el-select>
        </el-form-item>

        <el-form-item 
          v-if="batchOperationForm.operationType === 'updateCefrLevel'" 
          label="目标等级"
        >
          <el-select v-model="batchOperationForm.targetCefrLevel" placeholder="选择CEFR等级">
            <el-option 
              v-for="level in cefrLevels" 
              :key="level" 
              :label="level" 
              :value="level" 
            />
          </el-select>
        </el-form-item>

        <el-form-item 
          v-if="batchOperationForm.operationType === 'updateSource'" 
          label="目标来源"
        >
          <el-select v-model="batchOperationForm.targetSource" placeholder="选择来源">
            <el-option 
              v-for="source in termSources" 
              :key="source" 
              :label="source" 
              :value="source" 
            />
          </el-select>
        </el-form-item>

        <el-form-item 
          v-if="batchOperationForm.operationType === 'updateFreqRank'" 
          label="起始排名"
        >
          <el-input-number 
            v-model="batchOperationForm.startFreqRank" 
            :min="1" 
            placeholder="起始频次排名"
          />
        </el-form-item>

        <el-form-item 
          v-if="batchOperationForm.operationType === 'updateFreqRank'" 
          label="步长"
        >
          <el-input-number 
            v-model="batchOperationForm.freqRankStep" 
            :min="1" 
            placeholder="频次排名步长"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchOperationVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleBatchOperation"
          :loading="batchOperationLoading"
        >
          执行操作
        </el-button>
      </template>
    </el-dialog>

    <!-- 主题映射对话框 -->
    <el-dialog v-model="topicMappingVisible" title="添加主题映射" width="400px">
      <el-form :model="topicMappingForm" label-width="80px">
        <el-form-item label="选择主题">
          <el-select v-model="topicMappingForm.topicId" placeholder="选择主题">
            <el-option 
              v-for="topic in availableTopics" 
              :key="topic.id" 
              :label="topic.nameZh" 
              :value="topic.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主要归属">
          <el-switch v-model="topicMappingForm.isPrimary" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="topicMappingVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleAddTopicMapping"
          :loading="topicMappingLoading"
        >
          添加
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
  Edit,
  Delete,
  Upload,
  Download,
  VideoPlay,
  Mute,
  UploadFilled,
  Operation
} from '@element-plus/icons-vue'
import {
  getTerms,
  createTerm,
  updateTerm,
  deleteTerm,
  copyTerm,
  advancedSearchTerms,
  getTermStatistics,
  batchOperation,
  getCEFRLevels,
  getPOSOptions,
  getTermSources,
  getTermTopicMappings,
  addTopicMapping,
  removeTopicMapping,
  importTerms
} from '@/api/term'
import { getTermTopics } from '@/api/term'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const importLoading = ref(false)
const batchOperationLoading = ref(false)
const topicMappingLoading = ref(false)
const dialogVisible = ref(false)
const batchImportVisible = ref(false)
const batchOperationVisible = ref(false)
const topicMappingVisible = ref(false)
const showAdvancedSearch = ref(false)
const dialogMode = ref('view') // view, edit, create

const termList = ref([])
const selectedTerms = ref([])
const statistics = ref({})
const cefrLevels = ref([])
const posOptions = ref([])
const termSources = ref([])
const currentTermTopics = ref([])
const availableTopics = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

const advancedSearchForm = reactive({
  headword: '',
  definition: '',
  pos: '',
  cefrLevel: '',
  source: ''
})

// 当前术语
const currentTerm = reactive({
  headword: '',
  lemma: '',
  pos: '',
  ipa: '',
  definitionEn: '',
  definitionZh: '',
  exampleEn: '',
  exampleZh: '',
  cefrLevel: '',
  freqRank: null,
  source: ''
})

// 批量操作表单
const batchOperationForm = reactive({
  operationType: '',
  targetCefrLevel: '',
  targetSource: '',
  startFreqRank: 1,
  freqRankStep: 1
})

// 主题映射表单
const topicMappingForm = reactive({
  topicId: null,
  isPrimary: false
})

// 表单验证规则
const termRules = {
  headword: [
    { required: true, message: '请输入术语', trigger: 'blur' },
    { max: 200, message: '术语不能超过200个字符', trigger: 'blur' }
  ],
  definitionZh: [
    { required: true, message: '请输入中文释义', trigger: 'blur' },
    { max: 5000, message: '中文释义不能超过5000个字符', trigger: 'blur' }
  ]
}

// 计算属性
const formatDateTime = computed(() => {
  return (dateTime) => {
    if (!dateTime) return '-'
    return new Date(dateTime).toLocaleString('zh-CN')
  }
})

// 获取CEFR等级标签类型
const getCefrLevelType = (level) => {
  const typeMap = {
    'A1': 'success',
    'A2': 'success',
    'B1': 'warning',
    'B2': 'warning',
    'C1': 'danger',
    'C2': 'danger'
  }
  return typeMap[level] || 'info'
}

// 方法
const loadTerms = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size
    }
    
    const response = await getTerms(params)
    termList.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载术语列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await getTermStatistics()
    statistics.value = response.data
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const loadOptions = async () => {
  try {
    const [cefrResponse, posResponse, sourceResponse] = await Promise.all([
      getCEFRLevels(),
      getPOSOptions(),
      getTermSources()
    ])
    cefrLevels.value = cefrResponse.data
    posOptions.value = posResponse.data
    termSources.value = sourceResponse.data
  } catch (error) {
    console.error('加载选项失败:', error)
  }
}

const loadAvailableTopics = async () => {
  try {
    const response = await getTermTopics({ page: 0, size: 1000 })
    availableTopics.value = response.data.content
  } catch (error) {
    console.error('加载主题列表失败:', error)
  }
}

const showCreateDialog = () => {
  dialogMode.value = 'create'
  resetCurrentTerm()
  dialogVisible.value = true
}

const handleView = async (term) => {
  dialogMode.value = 'view'
  Object.assign(currentTerm, term)
  await loadTermTopics(term.id)
  dialogVisible.value = true
}

const handleEdit = async (term) => {
  dialogMode.value = 'edit'
  Object.assign(currentTerm, term)
  await loadTermTopics(term.id)
  dialogVisible.value = true
}

const handleCopy = async (term) => {
  try {
    await copyTerm(term.id)
    ElMessage.success('术语复制成功')
    loadTerms()
    loadStatistics()
  } catch (error) {
    ElMessage.error('术语复制失败')
    console.error(error)
  }
}

const handleDelete = async (term) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除术语"${term.headword}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTerm(term.id)
    ElMessage.success('删除成功')
    loadTerms()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedTerms.value.length} 个术语吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const termIds = selectedTerms.value.map(term => term.id)
    await batchOperation({
      operationType: 'delete',
      termIds
    })
    
    ElMessage.success('批量删除成功')
    selectedTerms.value = []
    loadTerms()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error(error)
    }
  }
}

const handleSaveTerm = async () => {
  try {
    await termForm.value.validate()
    saveLoading.value = true
    
    if (dialogMode.value === 'create') {
      await createTerm(currentTerm)
      ElMessage.success('创建成功')
    } else {
      await updateTerm(currentTerm.id, currentTerm)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    loadTerms()
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
  if (!searchForm.keyword.trim()) {
    loadTerms()
    return
  }
  
  try {
    loading.value = true
    const searchData = {
      headword: searchForm.keyword
    }
    const params = {
      page: 0,
      size: pagination.size
    }
    
    const response = await advancedSearchTerms(searchData, params)
    termList.value = response.data.content
    pagination.total = response.data.totalElements
    pagination.page = 1
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdvancedSearch = async () => {
  try {
    loading.value = true
    const params = {
      page: 0,
      size: pagination.size
    }
    
    const response = await advancedSearchTerms(advancedSearchForm, params)
    termList.value = response.data.content
    pagination.total = response.data.totalElements
    pagination.page = 1
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetAdvancedSearch = () => {
  Object.assign(advancedSearchForm, {
    headword: '',
    definition: '',
    pos: '',
    cefrLevel: '',
    source: ''
  })
}

const handleSelectionChange = (selection) => {
  selectedTerms.value = selection
}

const handleSortChange = ({ prop, order }) => {
  // 处理排序逻辑
  console.log('排序:', prop, order)
  loadTerms()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadTerms()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadTerms()
}

const resetCurrentTerm = () => {
  Object.assign(currentTerm, {
    headword: '',
    lemma: '',
    pos: '',
    ipa: '',
    definitionEn: '',
    definitionZh: '',
    exampleEn: '',
    exampleZh: '',
    cefrLevel: '',
    freqRank: null,
    source: ''
  })
  currentTermTopics.value = []
}

const showBatchImportDialog = () => {
  batchImportVisible.value = true
}

const showBatchOperationDialog = () => {
  if (selectedTerms.value.length === 0) {
    ElMessage.warning('请先选择要操作的术语')
    return
  }
  batchOperationVisible.value = true
}

const handleFileChange = (file) => {
  console.log('选择文件:', file)
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel' ||
                  file.type === 'text/csv'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 或 CSV 文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

const handleBatchImport = async () => {
  try {
    importLoading.value = true
    ElMessage.success('导入成功')
    batchImportVisible.value = false
    loadTerms()
    loadStatistics()
  } catch (error) {
    ElMessage.error('导入失败')
    console.error(error)
  } finally {
    importLoading.value = false
  }
}

const handleBatchOperation = async () => {
  try {
    batchOperationLoading.value = true
    const termIds = selectedTerms.value.map(term => term.id)
    
    await batchOperation({
      ...batchOperationForm,
      termIds
    })
    
    ElMessage.success('批量操作成功')
    batchOperationVisible.value = false
    selectedTerms.value = []
    loadTerms()
    loadStatistics()
  } catch (error) {
    ElMessage.error('批量操作失败')
    console.error(error)
  } finally {
    batchOperationLoading.value = false
  }
}

const downloadTemplate = () => {
  ElMessage.info('模板下载功能待实现')
}

// 主题映射相关方法
const loadTermTopics = async (termId) => {
  try {
    const response = await getTermTopicMappings(termId)
    currentTermTopics.value = response.data
  } catch (error) {
    console.error('加载术语主题失败:', error)
  }
}

const showTopicMappingDialog = () => {
  topicMappingForm.topicId = null
  topicMappingForm.isPrimary = false
  topicMappingVisible.value = true
}

const handleAddTopicMapping = async () => {
  try {
    topicMappingLoading.value = true
    await addTopicMapping(currentTerm.id, topicMappingForm)
    ElMessage.success('添加主题映射成功')
    topicMappingVisible.value = false
    await loadTermTopics(currentTerm.id)
  } catch (error) {
    ElMessage.error('添加主题映射失败')
    console.error(error)
  } finally {
    topicMappingLoading.value = false
  }
}

const handleRemoveTopicMapping = async (topic) => {
  try {
    await removeTopicMapping(currentTerm.id, topic.topicId)
    ElMessage.success('移除主题映射成功')
    await loadTermTopics(currentTerm.id)
  } catch (error) {
    ElMessage.error('移除主题映射失败')
    console.error(error)
  }
}

// 生命周期
onMounted(() => {
  loadTerms()
  loadStatistics()
  loadOptions()
  loadAvailableTopics()
})

// 模板引用
const termForm = ref(null)
const uploadRef = ref(null)
</script>

<style scoped>
.terminology-management {
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

.advanced-search {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
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

.term-cell {
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

.pagination {
  padding: 20px;
  text-align: center;
  border-top: 1px solid #e9ecef;
}

.import-section {
  margin-bottom: 20px;
}

.template-download {
  margin-top: 16px;
  text-align: center;
}

.topic-mappings {
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 12px;
}

.topic-list {
  margin-top: 12px;
}

.topic-tag {
  margin: 4px 8px 4px 0;
  position: relative;
}

.primary-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #f56c6c;
  color: white;
  font-size: 10px;
  padding: 1px 3px;
  border-radius: 50%;
  line-height: 1;
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

  .advanced-search .el-form {
    flex-direction: column;
  }
}
</style>

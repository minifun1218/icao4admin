<template>
  <div class="vocabulary-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>词汇管理</h2>
      <p>管理航空英语词汇库</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加词汇
        </el-button>
        <el-button @click="showBatchImportDialog">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedVocabs.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索词汇..."
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
          <el-form-item label="词汇">
            <el-input v-model="advancedSearchForm.word" placeholder="输入词汇" />
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
          <el-form-item label="例句">
            <el-input v-model="advancedSearchForm.example" placeholder="输入例句" />
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
          <el-statistic title="总词汇数" :value="statistics.totalCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已分类词汇" :value="statistics.categorizedCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="有音频词汇" :value="statistics.withAudioCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="高频词汇" :value="statistics.highFreqCount || 0" />
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="vocabList"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column 
          prop="headword" 
          label="词汇" 
          width="150"
          sortable="custom"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div class="vocab-cell">
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
            <el-icon v-if="scope.row.audioAssetId" color="#67c23a">
              <VideoPlay />
            </el-icon>
            <el-icon v-else color="#ddd">
              <Mute />
            </el-icon>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="handleEdit(scope.row)">
              编辑
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

    <!-- 词汇详情/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '添加词汇' : dialogMode === 'edit' ? '编辑词汇' : '词汇详情'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="vocabForm"
        :model="currentVocab"
        :rules="vocabRules"
        label-width="120px"
        :disabled="dialogMode === 'view'"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="词汇" prop="headword">
              <el-input v-model="currentVocab.headword" placeholder="输入词汇" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="词性" prop="pos">
              <el-select v-model="currentVocab.pos" placeholder="选择词性" clearable>
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
              <el-input v-model="currentVocab.lemma" placeholder="输入词形还原" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="国际音标">
              <el-input v-model="currentVocab.ipa" placeholder="输入音标" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="中文释义" prop="definitionZh">
          <el-input
            v-model="currentVocab.definitionZh"
            type="textarea"
            :rows="3"
            placeholder="输入中文释义"
          />
        </el-form-item>

        <el-form-item label="英文释义">
          <el-input
            v-model="currentVocab.definitionEn"
            type="textarea"
            :rows="3"
            placeholder="输入英文释义"
          />
        </el-form-item>

        <el-form-item label="英文例句">
          <el-input
            v-model="currentVocab.exampleEn"
            type="textarea"
            :rows="2"
            placeholder="输入英文例句"
          />
        </el-form-item>

        <el-form-item label="中文例句">
          <el-input
            v-model="currentVocab.exampleZh"
            type="textarea"
            :rows="2"
            placeholder="输入中文例句"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="CEFR等级">
              <el-select v-model="currentVocab.cefrLevel" placeholder="选择等级" clearable>
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
                v-model="currentVocab.freqRank" 
                :min="1" 
                :max="999999"
                placeholder="频次排名"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源">
              <el-input v-model="currentVocab.source" placeholder="输入来源" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 音频上传 -->
        <el-form-item label="发音音频">
          <div class="audio-upload-section">
            <el-upload
              ref="audioUpload"
              :auto-upload="false"
              :on-change="handleAudioChange"
              :before-upload="beforeAudioUpload"
              :file-list="audioFileList"
              :limit="1"
              accept="audio/*"
              class="audio-uploader"
            >
              <el-button type="primary" :icon="Upload">
                选择音频文件
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持 MP3、WAV、OGG 格式，文件大小不超过 50MB
                </div>
              </template>
            </el-upload>
            
            <!-- 音频预览播放器 -->
            <div v-if="currentAudioUrl || currentVocab.audioAssetId" class="audio-player">
              <audio 
                ref="audioPlayer"
                :src="currentAudioUrl"
                controls
                preload="metadata"
                style="width: 100%; margin-top: 10px;"
              >
                您的浏览器不支持音频播放
              </audio>
              <div class="audio-actions">
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="removeAudio"
                  style="margin-top: 5px;"
                >
                  删除音频
                </el-button>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- 扩展信息 (JSON字段) -->
        <el-form-item label="同义词">
          <el-input
            v-model="synonymsText"
            placeholder="输入同义词，用逗号分隔"
            @blur="updateExtraJson"
          />
        </el-form-item>

        <el-form-item label="反义词">
          <el-input
            v-model="antonymsText"
            placeholder="输入反义词，用逗号分隔"
            @blur="updateExtraJson"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="extraNotes"
            type="textarea"
            :rows="2"
            placeholder="输入其他备注信息"
            @blur="updateExtraJson"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          v-if="dialogMode !== 'view'" 
          type="primary" 
          @click="handleSaveVocab"
          :loading="saveLoading"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="batchImportVisible" title="批量导入词汇" width="600px">
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
  UploadFilled
} from '@element-plus/icons-vue'
import {
  getVocabs,
  createVocab,
  updateVocab,
  deleteVocab,
  deleteVocabsBatch,
  searchVocabs,
  comprehensiveSearchVocabs,
  getVocabStatistics,
  getCEFRLevels,
  getPOSOptions
} from '@/api/vocab'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const importLoading = ref(false)
const dialogVisible = ref(false)
const batchImportVisible = ref(false)
const showAdvancedSearch = ref(false)
const dialogMode = ref('view') // view, edit, create

const vocabList = ref([])
const selectedVocabs = ref([])
const statistics = ref({})
const cefrLevels = ref([])
const posOptions = ref([])

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
  word: '',
  definition: '',
  example: '',
  pos: '',
  cefrLevel: ''
})

// 当前词汇
const currentVocab = reactive({
  id: null,
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
  source: '',
  audioAssetId: null,
  extraJson: {}
})

// 音频相关
const audioFileList = ref([])
const currentAudioUrl = ref('')
const currentAudioFile = ref(null)

// 扩展信息字段
const synonymsText = ref('')
const antonymsText = ref('')
const extraNotes = ref('')

// 表单验证规则
const vocabRules = {
  headword: [
    { required: true, message: '请输入词汇', trigger: 'blur' },
    { max: 200, message: '词汇不能超过200个字符', trigger: 'blur' }
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
const loadVocabs = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size
    }
    
    const response = await getVocabs(params)
    vocabList.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载词汇列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await getVocabStatistics()
    statistics.value = response.data
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const loadOptions = async () => {
  try {
    const [cefrResponse, posResponse] = await Promise.all([
      getCEFRLevels(),
      getPOSOptions()
    ])
    cefrLevels.value = cefrResponse.data
    posOptions.value = posResponse.data
  } catch (error) {
    console.error('加载选项失败:', error)
  }
}

const showCreateDialog = () => {
  dialogMode.value = 'create'
  resetCurrentVocab()
  dialogVisible.value = true
}

const handleView = (vocab) => {
  dialogMode.value = 'view'
  Object.assign(currentVocab, vocab)
  populateExtraFields(vocab)
  loadAudioForVocab(vocab)
  dialogVisible.value = true
}

const handleEdit = (vocab) => {
  dialogMode.value = 'edit'
  Object.assign(currentVocab, vocab)
  populateExtraFields(vocab)
  loadAudioForVocab(vocab)
  dialogVisible.value = true
}

const handleDelete = async (vocab) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除词汇"${vocab.headword}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteVocab(vocab.id)
    ElMessage.success('删除成功')
    loadVocabs()
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
      `确定要删除选中的 ${selectedVocabs.value.length} 个词汇吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedVocabs.value.map(vocab => vocab.id)
    await deleteVocabsBatch(ids)
    ElMessage.success('批量删除成功')
    selectedVocabs.value = []
    loadVocabs()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error(error)
    }
  }
}

const handleSaveVocab = async () => {
  try {
    await vocabForm.value.validate()
    saveLoading.value = true
    
    // 更新扩展JSON字段
    updateExtraJson()
    
    // 如果有新的音频文件，先上传音频
    if (currentAudioFile.value) {
      const audioAssetId = await uploadAudioFile()
      if (audioAssetId) {
        currentVocab.audioAssetId = audioAssetId
      }
    }
    
    if (dialogMode.value === 'create') {
      await createVocab(currentVocab)
      ElMessage.success('创建成功')
    } else {
      await updateVocab(currentVocab.id, currentVocab)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    loadVocabs()
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
    loadVocabs()
    return
  }
  
  try {
    loading.value = true
    const params = {
      keyword: searchForm.keyword,
      page: 0,
      size: pagination.size
    }
    
    const response = await searchVocabs(params)
    vocabList.value = response.data.content
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
      ...advancedSearchForm,
      page: 0,
      size: pagination.size
    }
    
    const response = await comprehensiveSearchVocabs(params)
    vocabList.value = response.data.content
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
    word: '',
    definition: '',
    example: '',
    pos: '',
    cefrLevel: ''
  })
}

const handleSelectionChange = (selection) => {
  selectedVocabs.value = selection
}

const handleSortChange = ({ prop, order }) => {
  // 处理排序逻辑
  console.log('排序:', prop, order)
  loadVocabs()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadVocabs()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadVocabs()
}

const resetCurrentVocab = () => {
  Object.assign(currentVocab, {
    id: null,
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
    source: '',
    audioAssetId: null,
    extraJson: {}
  })
  
  // 重置音频相关状态
  audioFileList.value = []
  currentAudioUrl.value = ''
  currentAudioFile.value = null
  
  // 重置扩展信息字段
  synonymsText.value = ''
  antonymsText.value = ''
  extraNotes.value = ''
}

const showBatchImportDialog = () => {
  batchImportVisible.value = true
}

const handleFileChange = (file) => {
  // 处理文件选择
  console.log('选择文件:', file)
}

const beforeUpload = (file) => {
  // 文件上传前验证
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
    // 实现批量导入逻辑
    ElMessage.success('导入成功')
    batchImportVisible.value = false
    loadVocabs()
    loadStatistics()
  } catch (error) {
    ElMessage.error('导入失败')
    console.error(error)
  } finally {
    importLoading.value = false
  }
}

const downloadTemplate = () => {
  // 下载导入模板
  ElMessage.info('模板下载功能待实现')
}

// 音频处理方法
const handleAudioChange = (file) => {
  currentAudioFile.value = file.raw
  currentAudioUrl.value = URL.createObjectURL(file.raw)
  
  // 更新文件列表显示
  audioFileList.value = [file]
}

const beforeAudioUpload = (file) => {
  const isAudio = file.type.startsWith('audio/')
  const isLt50M = file.size / 1024 / 1024 < 50

  if (!isAudio) {
    ElMessage.error('只能上传音频文件')
    return false
  }
  if (!isLt50M) {
    ElMessage.error('音频文件大小不能超过 50MB')
    return false
  }
  return true
}

const removeAudio = () => {
  currentAudioFile.value = null
  currentAudioUrl.value = ''
  audioFileList.value = []
  currentVocab.audioAssetId = null
  
  // 清空音频上传组件
  if (audioUpload.value) {
    audioUpload.value.clearFiles()
  }
}

const loadAudioForVocab = async (vocab) => {
  if (vocab.audioAssetId) {
    // 如果有音频资源ID，构建音频URL
    // 这里需要根据实际的媒体资源服务来构建URL
    currentAudioUrl.value = `/api/media/audio/${vocab.audioAssetId}`
    audioFileList.value = []
  } else {
    currentAudioUrl.value = ''
    audioFileList.value = []
  }
}

// 扩展信息处理方法
const populateExtraFields = (vocab) => {
  if (vocab.extraJson) {
    // 填充同义词
    if (vocab.extraJson.synonyms && Array.isArray(vocab.extraJson.synonyms)) {
      synonymsText.value = vocab.extraJson.synonyms.join(', ')
    }
    
    // 填充反义词
    if (vocab.extraJson.antonyms && Array.isArray(vocab.extraJson.antonyms)) {
      antonymsText.value = vocab.extraJson.antonyms.join(', ')
    }
    
    // 填充备注
    if (vocab.extraJson.notes) {
      extraNotes.value = vocab.extraJson.notes
    }
  }
}

const updateExtraJson = () => {
  if (!currentVocab.extraJson) {
    currentVocab.extraJson = {}
  }
  
  // 更新同义词
  if (synonymsText.value.trim()) {
    currentVocab.extraJson.synonyms = synonymsText.value
      .split(',')
      .map(word => word.trim())
      .filter(word => word.length > 0)
  } else {
    delete currentVocab.extraJson.synonyms
  }
  
  // 更新反义词
  if (antonymsText.value.trim()) {
    currentVocab.extraJson.antonyms = antonymsText.value
      .split(',')
      .map(word => word.trim())
      .filter(word => word.length > 0)
  } else {
    delete currentVocab.extraJson.antonyms
  }
  
  // 更新备注
  if (extraNotes.value.trim()) {
    currentVocab.extraJson.notes = extraNotes.value.trim()
  } else {
    delete currentVocab.extraJson.notes
  }
}

// 上传音频文件
const uploadAudioFile = async () => {
  if (!currentAudioFile.value) {
    return null
  }
  
  try {
    const formData = new FormData()
    formData.append('file', currentAudioFile.value)
    formData.append('type', 'audio')
    formData.append('category', 'vocabulary')
    
    // 这里需要调用媒体上传API
    // const response = await uploadMediaFile(formData)
    // return response.data.id
    
    // 暂时返回一个模拟的ID
    ElMessage.info('音频上传功能待后端媒体服务完善')
    return null
  } catch (error) {
    console.error('音频上传失败:', error)
    ElMessage.error('音频上传失败')
    return null
  }
}

// 生命周期
onMounted(() => {
  loadVocabs()
  loadStatistics()
  loadOptions()
})

// 模板引用
const vocabForm = ref(null)
const uploadRef = ref(null)
const audioUpload = ref(null)
const audioPlayer = ref(null)
</script>

<style scoped>
.vocabulary-management {
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

.vocab-cell {
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

/* 音频上传相关样式 */
.audio-upload-section {
  width: 100%;
}

.audio-uploader {
  width: 100%;
}

.audio-player {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.audio-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

/* 音频预览播放器样式 */
audio {
  width: 100%;
  height: 40px;
}

/* 表单字段样式优化 */
.el-form-item {
  margin-bottom: 20px;
}

.el-form-item .el-input,
.el-form-item .el-select,
.el-form-item .el-input-number {
  width: 100%;
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
  
  .audio-player {
    padding: 12px;
  }
}
</style>

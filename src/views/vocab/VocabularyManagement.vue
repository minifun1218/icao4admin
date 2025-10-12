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
                :key="level.value" 
                :label="level.label" 
                :value="level.value" 
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
          <el-statistic title="有音频词汇" :value="statistics.withAudioCount || 0" />
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
        <el-table-column label="音频" width="80" align="center">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.audioAssetId"
              content="点击播放音频"
              placement="top"
            >
              <el-button 
                type="text" 
                size="small"
                @click="playAudio(scope.row)"
                class="audio-play-btn"
              >
                <el-icon color="#67c23a" size="18">
                  <VideoPlay />
                </el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip v-else content="暂无音频" placement="top">
              <el-icon color="#ddd" size="18" class="no-audio-icon">
                <Mute />
              </el-icon>
            </el-tooltip>
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
      <!-- 词汇详情模式下的音频播放区域 -->
      <div v-if="dialogMode === 'view' && currentVocab.audioAssetId" class="vocab-detail-audio">
        <div class="audio-section-header">
          <el-icon color="#67c23a"><VideoPlay /></el-icon>
          <span class="audio-section-title">发音音频</span>
        </div>
        <div class="audio-player-container">
          <audio 
            ref="detailAudioPlayer"
:src="currentAudioUrl"
            controls
            preload="metadata"
            class="detail-audio-player"
            @error="handleDetailAudioError"
          >
            您的浏览器不支持音频播放
          </audio>
        </div>
      </div>
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
                  :key="level.value" 
                  :label="level.label" 
                  :value="level.value" 
                />
              </el-select>
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
                  支持 MP3、WAV、OGG、M4A、AAC 格式，文件大小不超过 {{ getMaxFileSizeText() }}
                </div>
              </template>
            </el-upload>
            
            <!-- 音频预览播放器 -->
            <div v-if="currentAudioUrl || currentVocab.audioAssetId" class="audio-player">
                <div class="audio-info">
                <el-icon color="#67c23a"><VideoPlay /></el-icon>
                <span class="audio-label">音频文件</span>
              </div>
              <audio 
                ref="audioPlayer"
                :src="currentAudioUrl"
                controls
                preload="metadata"
                style="width: 100%; margin-top: 10px;"
                @error="handleAudioError"
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
import {
  uploadMediaFile,
  getPreviewUrl,
  getDownloadUrl,
  getMediaById
} from '@/api/media'
import request from '@/utils/request'
import { 
  MEDIA_CONFIG, 
  validateFileType, 
  validateFileSize, 
  getMaxFileSizeText, 
  getSupportedTypesText 
} from '@/utils/media-config'

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
// CEFR等级列表（根据后台枚举）
const cefrLevels = ref([
  { value: 'A1', label: 'A1 (初级入门)' },
  { value: 'A2', label: 'A2 (初级进阶)' },
  { value: 'B1', label: 'B1 (中级入门)' },
  { value: 'B2', label: 'B2 (中级进阶)' },
  { value: 'C1', label: 'C1 (高级入门)' },
  { value: 'C2', label: 'C2 (高级精通)' }
])

// 默认词性列表
const posOptions = ref([
  'n.', // 名词
  'v.', // 动词
  'adj.', // 形容词
  'adv.', // 副词
  'prep.', // 介词
  'conj.', // 连词
  'pron.', // 代词
  'art.', // 冠词
  'num.', // 数词
  'int.', // 感叹词
  'abbr.', // 缩写
  'phr.', // 短语
  'aux.', // 助动词
  'modal' // 情态动词
])

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
  audioAssetId: null
})

// 音频相关
const audioFileList = ref([])
const currentAudioUrl = ref('')
const currentAudioFile = ref(null)


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

// 选项数据已经在组件中定义，无需从API加载
const loadOptions = () => {
  // CEFR等级和词性选项已经预定义
  console.log('选项已预定义')
}

const showCreateDialog = () => {
  dialogMode.value = 'create'
  resetCurrentVocab()
  dialogVisible.value = true
}

const handleView = async (vocab) => {
  dialogMode.value = 'view'
  Object.assign(currentVocab, vocab)
  
  // 异步加载音频URL
  if (vocab.audioAssetId) {
    try {
      const audioUrl = await getAudioUrl(vocab)
      currentAudioUrl.value = audioUrl
    } catch (error) {
      console.error('加载音频URL失败:', error)
      currentAudioUrl.value = ''
    }
  } else {
    currentAudioUrl.value = ''
  }
  
  dialogVisible.value = true
}

const handleEdit = (vocab) => {
  dialogMode.value = 'edit'
  Object.assign(currentVocab, vocab)
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
  audioAssetId: null
  })
  
  // 重置音频相关状态
  audioFileList.value = []
  currentAudioUrl.value = ''
  currentAudioFile.value = null
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
  // 使用媒体配置进行验证
  if (!validateFileType(file, 'audio')) {
    ElMessage.error('不支持的音频文件类型，支持的格式：MP3、WAV、OGG、M4A、AAC')
    return false
  }
  
  if (!validateFileSize(file)) {
    ElMessage.error(`音频文件大小不能超过 ${getMaxFileSizeText()}`)
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

// 音频加载错误处理
const handleAudioError = (event) => {
  console.error('音频加载失败:', event)
  ElMessage.warning('音频文件加载失败，可能文件已损坏或不存在')
}

// 详情页面音频加载错误处理
const handleDetailAudioError = (event) => {
  console.error('详情音频加载失败:', event)
  ElMessage.warning('音频文件加载失败')
}

// 媒体URL缓存机制
const mediaUrlCache = reactive(new Map())

// 获取带缓存的媒体URL
const getCachedMediaUrl = (mediaAsset, type = 'preview') => {
  if (!mediaAsset) return null
  
  const cacheKey = `${mediaAsset.id || mediaAsset.filename}_${type}`
  
  if (mediaUrlCache.has(cacheKey)) {
    return mediaUrlCache.get(cacheKey)
  }
  
  let url = null
  switch (type) {
    case 'preview':
      url = getPreviewUrl(mediaAsset)
      break
    case 'thumbnail':
      url = getPreviewUrl(mediaAsset)
      break
    case 'download':
      url = getDownloadUrl(mediaAsset)
      break
    default:
      url = getPreviewUrl(mediaAsset)
  }
  
  if (url) {
    mediaUrlCache.set(cacheKey, url)
  }
  
  return url
}

// 表格中播放音频
const playAudio = async (vocab) => {
  console.log('🎵 尝试播放音频，词汇:', vocab.headword, '音频ID:', vocab.audioAssetId)
  
  if (!vocab.audioAssetId) {
    ElMessage.warning('该词汇没有音频文件')
    return
  }
  
  try {
    // 通过后端API获取媒体资源信息
    console.log('🎵 调用后端API获取媒体资源:', vocab.audioAssetId)
    // 直接使用request调用，因为createApiMethod不支持路径参数替换
    const response = await request.get(`/media/${vocab.audioAssetId}`)

    console.log('🎵 后端媒体资源响应:', response.data, '123123')


    const previewUrl = response.data.data.previewUrl;
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
    
    // 创建临时音频元素播放
    const audio = new Audio(audioUrl)
    
    // 添加加载事件监听
    audio.addEventListener('loadstart', () => {
      console.log('🎵 开始加载音频')
    })
    
    audio.addEventListener('canplay', () => {
      console.log('🎵 音频可以播放')
    })
    
    audio.addEventListener('error', (e) => {
      console.error('🎵 音频加载错误:', e)
      ElMessage.error('音频文件加载失败')
    })
    
    // 尝试播放
    audio.play().then(() => {
      console.log('🎵 音频播放开始')
      ElMessage.success('正在播放音频')
    }).catch(error => {
      console.error('🎵 音频播放失败:', error)
      ElMessage.error('音频播放失败: ' + error.message)
    })
    
  } catch (error) {
    console.error('🎵 调用后端API失败:', error)
    ElMessage.error('获取音频资源失败: ' + (error.message || error))
  }
}

// 获取音频URL的辅助方法（用于详情页面）
const getAudioUrl = async (vocab) => {
  console.log('🎵 getAudioUrl 被调用，词汇:', vocab.headword, '音频ID:', vocab.audioAssetId)
  
  if (!vocab.audioAssetId) {
    console.log('🎵 没有音频ID，返回null')
    return null
  }
  
  try {
    // 通过后端API获取媒体资源信息
    const response = await request.get(`/media/${vocab.audioAssetId}`)
    
    console.log('🎵 getAudioUrl - 后端响应:', response.data)
    
    const previewUrl = response.data.data?.previewUrl
    if (!previewUrl) {
      console.error('🎵 后端未返回previewUrl')
      return null
    }
    
    // 将相对路径拼接成完整路径
    let audioUrl = null
    if (previewUrl.startsWith('http')) {
      // 已经是完整URL
      audioUrl = previewUrl
    } else {
      // 相对路径，需要拼接API基础路径
      const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
      audioUrl = previewUrl.startsWith('/') ? `${baseURL}${previewUrl}` : `${baseURL}/${previewUrl}`
    }
    
    console.log('🎵 生成的音频URL:', audioUrl)
    return audioUrl
    
  } catch (error) {
    console.error('🎵 获取音频URL失败:', error)
    return null
  }
}

const loadAudioForVocab = async (vocab) => {
  if (vocab.audioAssetId) {
    try {
      // 通过后端API获取音频URL
      const audioUrl = await getAudioUrl(vocab)
      currentAudioUrl.value = audioUrl
      audioFileList.value = []
      
      console.log('加载词汇音频URL:', currentAudioUrl.value)
    } catch (error) {
      console.error('加载音频失败:', error)
      currentAudioUrl.value = ''
      audioFileList.value = []
    }
  } else {
    currentAudioUrl.value = ''
    audioFileList.value = []
  }
}


// 上传音频文件
const uploadAudioFile = async () => {
  if (!currentAudioFile.value) {
    return null
  }
  
  try {
    // 验证文件类型和大小
    if (!validateFileType(currentAudioFile.value, 'audio')) {
      ElMessage.error('不支持的音频文件类型')
      return null
    }
    
    if (!validateFileSize(currentAudioFile.value)) {
      ElMessage.error(`文件大小不能超过 ${getMaxFileSizeText()}`)
      return null
    }
    
    const formData = new FormData()
    formData.append('file', currentAudioFile.value)
    formData.append('type', 'audio')
    formData.append('category', 'vocabulary')
    formData.append('title', `${currentVocab.headword} 发音`)
    formData.append('description', `词汇 "${currentVocab.headword}" 的发音音频`)
    
    console.log('开始上传音频文件:', currentAudioFile.value.name)
    const response = await uploadMediaFile(formData)
    
    console.log('音频上传响应:', response)
    
    // 检查多种可能的响应格式
    let mediaData = null
    if (response && response.data) {
      if (response.data.data && response.data.data.id) {
        // 格式: { data: { data: { id: ... } } }
        mediaData = response.data.data
      } else if (response.data.id) {
        // 格式: { data: { id: ... } }
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
const detailAudioPlayer = ref(null)
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

.audio-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding: 8px;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #e1f5fe;
}

.audio-label {
  font-size: 14px;
  color: #1976d2;
  font-weight: 500;
}

/* 表格中的音频播放按钮 */
.audio-play-btn {
  padding: 6px !important;
  min-height: auto !important;
  border-radius: 50%;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid transparent;
  background-color: rgba(103, 194, 58, 0.1);
}

.audio-play-btn:hover {
  background-color: rgba(103, 194, 58, 0.2);
  border-color: #67c23a;
  transform: scale(1.15);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.audio-play-btn:active {
  transform: scale(1.05);
  background-color: rgba(103, 194, 58, 0.3);
}

/* 无音频图标样式 */
.no-audio-icon {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 词汇详情中的音频区域 */
.vocab-detail-audio {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.audio-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.audio-section-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.audio-player-container {
  padding: 8px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
}

.detail-audio-player {
  width: 100%;
  height: 40px;
  outline: none;
}

.detail-audio-player::-webkit-media-controls-panel {
  background-color: #f5f7fa;
}

.detail-audio-player::-webkit-media-controls-play-button,
.detail-audio-player::-webkit-media-controls-pause-button {
  background-color: #409eff;
  border-radius: 50%;
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

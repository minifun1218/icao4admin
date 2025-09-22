<template>
  <div class="story-retell-panel">
    <!-- 操作栏 -->
    <div class="panel-header">
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索故事标题或内容"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.difficulty" placeholder="难度等级" clearable>
              <el-option
                v-for="level in difficultyLevels"
                :key="level.value"
                :label="level.label"
                :value="level.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.status" placeholder="状态" clearable>
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
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
      
      <div class="action-section">
        <el-button type="success" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加故事复述题
        </el-button>
        <el-button type="warning" @click="batchEdit" :disabled="!hasSelection">
          <el-icon><Edit /></el-icon>
          批量编辑
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="!hasSelection">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 故事列表 -->
    <div class="story-list">
      <el-table
        v-loading="loading"
        :data="filteredStories"
        @selection-change="handleSelectionChange"
        stripe
        class="story-table"
        empty-text="暂无故事复述题数据"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="expand" width="30">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="story-details">
                <div class="detail-section">
                  <h4>故事内容</h4>
                  <div class="story-content">{{ row.content }}</div>
                </div>
                
                <div class="detail-section">
                  <h4>考试配置</h4>
                  <el-row :gutter="20">
                    <el-col :span="6">
                      <div class="config-item">
                        <span class="label">准备时间:</span>
                        <span class="value">{{ row.preparationTime || 300 }}秒</span>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="config-item">
                        <span class="label">回答时间:</span>
                        <span class="value">{{ row.responseTime || 300 }}秒</span>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="config-item">
                        <span class="label">播放次数:</span>
                        <span class="value">{{ row.playCount || 2 }}次</span>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="config-item">
                        <span class="label">播放间隔:</span>
                        <span class="value">{{ row.playInterval || 5 }}秒</span>
                      </div>
                    </el-col>
                  </el-row>
                </div>
                
                <div class="detail-section" v-if="row.audioUrl">
                  <h4>音频文件</h4>
                  <audio :src="row.audioUrl" controls style="width: 100%;" />
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="故事信息" min-width="300">
          <template #default="{ row }">
            <div class="story-info">
              <div class="story-title">{{ row.title }}</div>
              <div class="story-meta">
                <el-tag v-if="row.audioUrl" size="small" type="success">
                  <el-icon><Service /></el-icon>
                  音频
                </el-tag>
                <el-tag 
                  :type="getDifficultyTagType(row.difficulty)"
                  size="small"
                >
                  {{ formatDifficultyLevel(row.difficulty) }}
                </el-tag>
                <span class="word-count">{{ getWordCount(row.content) }}词</span>
              </div>
              <div class="story-preview">
                {{ row.content ? row.content.substring(0, 100) + '...' : '暂无内容' }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="考试时长" width="120" align="center">
          <template #default="{ row }">
            <div class="time-info">
              <div>准备: {{ formatTime(row.preparationTime || 300) }}</div>
              <div>回答: {{ formatTime(row.responseTime || 300) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="播放设置" width="100" align="center">
          <template #default="{ row }">
            <div class="play-info">
              <el-tag size="small" type="info">
                {{ row.playCount || 2 }}次
              </el-tag>
              <div style="font-size: 12px; color: #909399; margin-top: 2px;">
                间隔{{ row.playInterval || 5 }}秒
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="toggleStatus(row)"
              active-color="#67C23A"
              inactive-color="#F56C6C"
            />
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="editStory(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="info" size="small" @click="previewStory(row)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button type="success" size="small" @click="copyStory(row)">
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
              <el-button type="danger" size="small" @click="deleteStory(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

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

    <!-- 创建/编辑故事对话框 -->
    <el-dialog
      v-model="storyFormVisible"
      :title="storyForm.id ? '编辑故事复述题' : '创建故事复述题'"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="storyFormRef"
        :model="storyForm"
        :rules="storyRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="故事标题" prop="title">
              <el-input v-model="storyForm.title" placeholder="请输入故事标题" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="故事内容" prop="content">
              <el-input
                v-model="storyForm.content"
                type="textarea"
                :rows="6"
                placeholder="请输入故事内容（100-120词，与民航管制工作相关的非正常或特情情景）"
                maxlength="500"
                show-word-limit
                @input="updateWordCount"
              />
              <div class="word-count-info">
                <span :class="{ 'warning': wordCount < 100 || wordCount > 120 }">
                  当前词数: {{ wordCount }} 词 (建议: 100-120词)
                </span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficulty">
              <el-select v-model="storyForm.difficulty" placeholder="选择难度等级">
                <el-option
                  v-for="level in difficultyLevels"
                  :key="level.value"
                  :label="level.label"
                  :value="level.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示顺序">
              <el-input-number
                v-model="storyForm.displayOrder"
                :min="1"
                :max="9999"
                placeholder="显示顺序"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="音频文件" prop="audioUrl">
              <div class="audio-upload-section">
                <el-upload
                  ref="audioUploadRef"
                  :action="uploadUrl"
                  :before-upload="beforeAudioUpload"
                  :on-success="handleAudioUploadSuccess"
                  :on-error="handleAudioUploadError"
                  :show-file-list="false"
                  accept="audio/*"
                >
                  <el-button type="primary">
                    <el-icon><Upload /></el-icon>
                    上传音频
                  </el-button>
                </el-upload>
                
                <div v-if="storyForm.audioUrl" class="audio-preview">
                  <audio :src="storyForm.audioUrl" controls style="width: 100%; margin-top: 10px;" />
                  <el-button type="danger" size="small" @click="removeAudio" style="margin-top: 10px;">
                    移除音频
                  </el-button>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 考试配置 -->
        <el-divider content-position="left">
          <el-icon><Setting /></el-icon>
          考试配置
        </el-divider>
        
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="准备时间">
              <el-input-number
                v-model="storyForm.preparationTime"
                :min="60"
                :max="600"
                :step="30"
                placeholder="秒"
              />
              <div class="help-text">考生听录音后的准备时间</div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="回答时间">
              <el-input-number
                v-model="storyForm.responseTime"
                :min="120"
                :max="600"
                :step="30"
                placeholder="秒"
              />
              <div class="help-text">考生复述的最长时间</div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="播放次数">
              <el-input-number
                v-model="storyForm.playCount"
                :min="1"
                :max="5"
                placeholder="次"
              />
              <div class="help-text">音频播放次数</div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="播放间隔">
              <el-input-number
                v-model="storyForm.playInterval"
                :min="0"
                :max="30"
                placeholder="秒"
              />
              <div class="help-text">两次播放之间的间隔</div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="自动跳转">
              <el-switch
                v-model="storyForm.autoNext"
                active-text="是"
                inactive-text="否"
              />
              <div class="help-text">时间结束后自动跳转</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="显示计时器">
              <el-switch
                v-model="storyForm.showTimer"
                active-text="是"
                inactive-text="否"
              />
              <div class="help-text">向考生显示倒计时</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch
                v-model="storyForm.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="storyForm.remark"
                type="textarea"
                :rows="2"
                placeholder="备注信息"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="storyFormVisible = false">取消</el-button>
        <el-button @click="resetToDefaults">恢复默认</el-button>
        <el-button type="primary" @click="saveStory" :loading="saving">
          {{ storyForm.id ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <StoryRetellPreview
      v-if="previewVisible"
      :visible="previewVisible"
      :story="previewStory"
      @close="previewVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionBankApi } from '@/api/questionBank'
import StoryRetellPreview from './StoryRetellPreview.vue'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  View,
  Upload,
  Service,
  Setting,
  CopyDocument
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['selection-change', 'refresh'])

// 响应式数据
const storyFormVisible = ref(false)
const previewVisible = ref(false)
const saving = ref(false)

const storyFormRef = ref(null)
const audioUploadRef = ref(null)

const stories = ref([])
const selectedStories = ref([])
const previewStory = ref(null)
const wordCount = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  difficulty: null,
  status: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 故事表单
const storyForm = reactive({
  id: null,
  title: '',
  content: '',
  audioUrl: '',
  difficulty: 3,
  displayOrder: 1,
  preparationTime: 300,
  responseTime: 300,
  playCount: 2,
  playInterval: 5,
  autoNext: true,
  showTimer: true,
  enabled: true,
  remark: ''
})

// 计算属性
const difficultyLevels = computed(() => questionBankApi.getDifficultyLevels())

const filteredStories = computed(() => {
  let filtered = stories.value

  // 关键词搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filtered = filtered.filter(story => 
      story.title?.toLowerCase().includes(keyword) ||
      story.content?.toLowerCase().includes(keyword)
    )
  }

  // 难度筛选
  if (searchForm.difficulty !== null) {
    filtered = filtered.filter(story => story.difficulty === searchForm.difficulty)
  }

  // 状态筛选
  if (searchForm.status !== null) {
    filtered = filtered.filter(story => story.enabled === searchForm.status)
  }

  // 更新分页总数
  pagination.total = filtered.length

  // 分页
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filtered.slice(start, end)
})

const hasSelection = computed(() => selectedStories.value.length > 0)

const uploadUrl = computed(() => '/api/listening-mcq/question-bank/upload/audio')

// 表单验证规则
const storyRules = {
  title: [
    { required: true, message: '请输入故事标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度为2-200个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入故事内容', trigger: 'blur' },
    { min: 50, max: 500, message: '内容长度为50-500个字符', trigger: 'blur' }
  ],
  difficulty: [
    { required: true, message: '请选择难度等级', trigger: 'change' }
  ],
  audioUrl: [
    { required: true, message: '请上传音频文件', trigger: 'change' }
  ]
}

// 方法
const loadStories = async () => {
  try {
    const response = await questionBankApi.getStoryRetellItems({
      page: pagination.page - 1,
      size: pagination.size
    })
    
    stories.value = response.data?.content || []
    pagination.total = response.data?.totalElements || 0
    
  } catch (error) {
    console.error('加载故事列表失败:', error)
    ElMessage.error('加载故事列表失败')
  }
}

const showCreateDialog = () => {
  resetStoryForm()
  storyFormVisible.value = true
}

const editStory = (story) => {
  Object.assign(storyForm, {
    ...story,
    enabled: story.enabled !== false
  })
  updateWordCount()
  storyFormVisible.value = true
}

const saveStory = async () => {
  if (!storyFormRef.value) return
  
  try {
    await storyFormRef.value.validate()
    
    // 验证数据
    const validation = questionBankApi.validateStoryRetellItem(storyForm)
    if (!validation.isValid) {
      ElMessage.error(validation.errors[0])
      return
    }
    
    saving.value = true
    
    const data = { ...storyForm }
    
    if (storyForm.id) {
      await questionBankApi.updateStoryRetellItem(storyForm.id, data)
      ElMessage.success('故事更新成功')
    } else {
      await questionBankApi.createStoryRetellItem(data)
      ElMessage.success('故事创建成功')
    }
    
    storyFormVisible.value = false
    await loadStories()
    emit('refresh')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const deleteStory = async (story) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除故事"${story.title}"吗？`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await questionBankApi.deleteStoryRetellItem(story.id)
    ElMessage.success('删除成功')
    await loadStories()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const copyStory = async (story) => {
  try {
    await questionBankApi.copyStoryRetellItem(story.id)
    ElMessage.success('复制成功')
    await loadStories()
    emit('refresh')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const previewStory = (story) => {
  previewStory.value = story
  previewVisible.value = true
}

const toggleStatus = async (story) => {
  try {
    await questionBankApi.updateStoryRetellItem(story.id, { enabled: story.enabled })
    ElMessage.success(`已${story.enabled ? '启用' : '禁用'}故事`)
  } catch (error) {
    story.enabled = !story.enabled // 回滚状态
    ElMessage.error('状态更新失败')
  }
}

const batchEdit = () => {
  if (selectedStories.value.length === 0) {
    ElMessage.warning('请选择要编辑的故事')
    return
  }
  ElMessage.info('批量编辑功能开发中...')
}

const batchDelete = async () => {
  if (selectedStories.value.length === 0) {
    ElMessage.warning('请选择要删除的故事')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedStories.value.length} 个故事吗？`,
      '批量删除确认',
      { type: 'warning' }
    )
    
    const storyIds = selectedStories.value.map(story => story.id)
    await questionBankApi.batchDeleteStoryRetellItems(storyIds)
    
    ElMessage.success(`成功删除 ${selectedStories.value.length} 个故事`)
    selectedStories.value = []
    await loadStories()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const resetToDefaults = () => {
  const defaults = questionBankApi.getStoryRetellDefaultConfig()
  Object.assign(storyForm, defaults)
}

const handleSearch = () => {
  pagination.page = 1
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    difficulty: null,
    status: null
  })
  pagination.page = 1
}

const handleSelectionChange = (selection) => {
  selectedStories.value = selection
  emit('selection-change', selection)
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadStories()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadStories()
}

const resetStoryForm = () => {
  const defaults = questionBankApi.getStoryRetellDefaultConfig()
  Object.assign(storyForm, {
    id: null,
    title: '',
    content: '',
    audioUrl: '',
    difficulty: 3,
    displayOrder: 1,
    enabled: true,
    remark: '',
    ...defaults
  })
  
  wordCount.value = 0
  
  if (storyFormRef.value) {
    storyFormRef.value.clearValidate()
  }
}

// 音频上传相关方法
const beforeAudioUpload = (file) => {
  const validation = questionBankApi.validateAudioFile(file)
  if (!validation.valid) {
    ElMessage.error(validation.message)
    return false
  }
  return true
}

const handleAudioUploadSuccess = (response) => {
  if (response.success) {
    storyForm.audioUrl = response.data.url
    ElMessage.success('音频上传成功')
  } else {
    ElMessage.error('音频上传失败')
  }
}

const handleAudioUploadError = () => {
  ElMessage.error('音频上传失败')
}

const removeAudio = () => {
  storyForm.audioUrl = ''
  ElMessage.success('音频已移除')
}

// 工具方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatTime = (seconds) => {
  if (!seconds) return '-'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return minutes > 0 ? `${minutes}分${remainingSeconds}秒` : `${remainingSeconds}秒`
}

const formatDifficultyLevel = (level) => {
  return questionBankApi.formatDifficultyLevel(level)
}

const getDifficultyTagType = (level) => {
  const typeMap = {
    1: 'success',
    2: 'success',
    3: 'warning',
    4: 'warning',
    5: 'danger',
    6: 'info'
  }
  return typeMap[level] || 'info'
}

const getWordCount = (text) => {
  if (!text) return 0
  return text.trim().split(/\s+/).length
}

const updateWordCount = () => {
  wordCount.value = getWordCount(storyForm.content)
}

// 生命周期
onMounted(() => {
  loadStories()
})
</script>

<style scoped>
.story-retell-panel {
  padding: 0;
}

.panel-header {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.search-section {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.action-section {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.story-table {
  border-radius: 8px;
  overflow: hidden;
}

.story-info {
  padding: 8px 0;
}

.story-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.story-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.word-count {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.story-preview {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.time-info {
  font-size: 12px;
  color: #606266;
}

.play-info {
  text-align: center;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.expand-content {
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-section h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.story-content {
  background: #fff;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  line-height: 1.6;
}

.config-item {
  display: flex;
  margin-bottom: 4px;
}

.config-item .label {
  font-weight: 600;
  width: 80px;
  color: #606266;
}

.config-item .value {
  color: #303133;
}

.audio-upload-section {
  width: 100%;
}

.audio-preview {
  margin-top: 10px;
}

.word-count-info {
  margin-top: 8px;
  font-size: 12px;
}

.word-count-info .warning {
  color: #E6A23C;
  font-weight: 600;
}

.help-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .action-section {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .story-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>

<template>
  <div class="vocab-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>词汇管理</span>
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
                导入词汇
              </el-button>
            </el-upload>
            <el-button type="info" @click="exportVocabs" :loading="exporting">
              <el-icon><Download /></el-icon>
              导出词汇
            </el-button>
            <el-button type="primary" @click="createVocab">
              <el-icon><Plus /></el-icon>
              添加词汇
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
              placeholder="搜索词汇、释义"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="CEFR等级">
            <el-select v-model="searchForm.cefrLevel" placeholder="选择等级" clearable>
              <el-option
                v-for="level in cefrLevels"
                :key="level"
                :label="level"
                :value="level"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="难度等级">
            <el-select v-model="searchForm.difficultyLevel" placeholder="选择难度" clearable>
              <el-option
                v-for="i in 10"
                :key="i"
                :label="`${i}级`"
                :value="i"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="词性">
            <el-select v-model="searchForm.pos" placeholder="选择词性" clearable>
              <el-option
                v-for="pos in posOptions"
                :key="pos"
                :label="pos"
                :value="pos"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="有音频">
            <el-select v-model="searchForm.hasAudio" placeholder="是否有音频" clearable>
              <el-option label="是" :value="true" />
              <el-option label="否" :value="false" />
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
      
      <!-- 词汇列表 -->
      <el-table
        v-loading="loading"
        :data="vocabList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="headword" label="词汇" min-width="120" />
        <el-table-column prop="lemma" label="词形还原" width="120" />
        <el-table-column prop="pos" label="词性" width="100" />
        <el-table-column prop="ipa" label="音标" width="120" />
        <el-table-column prop="definitionEn" label="英文释义" min-width="200" show-overflow-tooltip />
        <el-table-column prop="definitionZh" label="中文释义" min-width="150" show-overflow-tooltip />
        <el-table-column prop="cefrLevel" label="CEFR等级" width="100" />
        <el-table-column prop="difficultyLevel" label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficultyLevel)">
              {{ row.difficultyLevel }}级
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="音频" width="80">
          <template #default="{ row }">
            <el-button
              v-if="row.audioAsset"
              type="text"
              @click="playAudio(row.audioAsset.url)"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <span v-else class="text-gray">无</span>
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
            <el-button type="primary" size="small" @click="editVocab(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteVocab(row)">
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
    
    <!-- 编辑词汇对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editForm.id ? '编辑词汇' : '新增词汇'"
      width="800px"
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
            <el-form-item label="词汇" prop="headword">
              <el-input v-model="editForm.headword" placeholder="请输入词汇" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="词形还原" prop="lemma">
              <el-input v-model="editForm.lemma" placeholder="请输入词形还原" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="词性" prop="pos">
              <el-select v-model="editForm.pos" placeholder="选择词性">
                <el-option
                  v-for="pos in posOptions"
                  :key="pos"
                  :label="pos"
                  :value="pos"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="音标" prop="ipa">
              <el-input v-model="editForm.ipa" placeholder="请输入国际音标" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="英文释义" prop="definitionEn">
          <el-input
            v-model="editForm.definitionEn"
            type="textarea"
            :rows="2"
            placeholder="请输入英文释义"
          />
        </el-form-item>
        
        <el-form-item label="中文释义" prop="definitionCn">
          <el-input
            v-model="editForm.definitionCn"
            type="textarea"
            :rows="2"
            placeholder="请输入中文释义"
          />
        </el-form-item>
        
        <el-form-item label="例句" prop="exampleSentence">
          <el-input
            v-model="editForm.exampleSentence"
            type="textarea"
            :rows="2"
            placeholder="请输入例句"
          />
        </el-form-item>
        
        <el-form-item label="例句翻译" prop="exampleTranslation">
          <el-input
            v-model="editForm.exampleTranslation"
            type="textarea"
            :rows="2"
            placeholder="请输入例句翻译"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="CEFR等级" prop="cefrLevel">
              <el-select v-model="editForm.cefrLevel" placeholder="选择等级">
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
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-slider
                v-model="editForm.difficultyLevel"
                :min="1"
                :max="10"
                show-stops
                show-input
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="频率等级" prop="frequencyLevel">
              <el-slider
                v-model="editForm.frequencyLevel"
                :min="1"
                :max="10"
                show-stops
                show-input
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="音频路径" prop="audioPath">
              <el-input v-model="editForm.audioPath" placeholder="请输入音频文件路径" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图片路径" prop="imagePath">
              <el-input v-model="editForm.imagePath" placeholder="请输入图片路径" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="标签" prop="tags">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        
        <el-form-item label="备注" prop="notes">
          <el-input
            v-model="editForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="显示顺序" prop="displayOrder">
              <el-input-number v-model="editForm.displayOrder" :min="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="isEnabled">
              <el-switch v-model="editForm.isEnabled" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveVocab" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { vocabApi } from '@/api/vocab'
import {
  Search,
  Refresh,
  Upload,
  Download,
  Plus,
  Delete,
  Edit,
  VideoPlay
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const importing = ref(false)
const exporting = ref(false)
const saving = ref(false)
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const uploadRef = ref(null)

const vocabList = ref([])
const selectedVocabs = ref([])

// 选项数据
const cefrLevels = vocabApi.getCEFRLevels()
const posOptions = vocabApi.getPOSOptions()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  cefrLevel: '',
  difficultyLevel: '',
  pos: '',
  hasAudio: ''
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
  headword: '',
  lemma: '',
  pos: '',
  ipa: '',
  definitionEn: '',
  definitionCn: '',
  exampleSentence: '',
  exampleTranslation: '',
  audioPath: '',
  imagePath: '',
  difficultyLevel: 3,
  cefrLevel: '',
  frequencyLevel: 4,
  tags: '',
  notes: '',
  isEnabled: true,
  displayOrder: 1
})

// 表单验证规则
const editRules = {
  headword: [
    { required: true, message: '请输入词汇', trigger: 'blur' }
  ],
  definitionEn: [
    { required: true, message: '请输入英文释义', trigger: 'blur' }
  ],
  definitionCn: [
    { required: true, message: '请输入中文释义', trigger: 'blur' }
  ]
}

// 计算属性
const hasSelection = computed(() => selectedVocabs.value.length > 0)

// 加载词汇列表
const loadVocabs = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: 'headword,asc',
      ...searchForm
    }
    
    const response = await vocabApi.getVocabs(params)
    
    vocabList.value = response.content || []
    pagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载词汇列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadVocabs()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    cefrLevel: '',
    difficultyLevel: '',
    pos: '',
    hasAudio: ''
  })
  handleSearch()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadVocabs()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadVocabs()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedVocabs.value = selection
}

// 状态切换
const handleStatusChange = async (vocab) => {
  try {
    await vocabApi.updateVocab(vocab.id, { isEnabled: vocab.isEnabled })
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 恢复原状态
    vocab.isEnabled = !vocab.isEnabled
    ElMessage.error('状态更新失败')
  }
}

// 创建词汇
const createVocab = () => {
  Object.assign(editForm, {
    id: null,
    headword: '',
    lemma: '',
    pos: '',
    ipa: '',
    definitionEn: '',
    definitionCn: '',
    exampleSentence: '',
    exampleTranslation: '',
    audioPath: '',
    imagePath: '',
    difficultyLevel: 3,
    cefrLevel: '',
    frequencyLevel: 4,
    tags: '',
    notes: '',
    isEnabled: true,
    displayOrder: 1
  })
  editDialogVisible.value = true
}

// 编辑词汇
const editVocab = (vocab) => {
  Object.assign(editForm, {
    id: vocab.id,
    headword: vocab.headword,
    lemma: vocab.lemma,
    pos: vocab.pos,
    ipa: vocab.ipa,
    definitionEn: vocab.definitionEn,
    definitionCn: vocab.definitionZh,
    exampleSentence: vocab.exampleEn,
    exampleTranslation: vocab.exampleZh,
    audioPath: vocab.audioAsset?.fileName || '',
    imagePath: vocab.imagePath || '',
    difficultyLevel: vocab.difficultyLevel || 3,
    cefrLevel: vocab.cefrLevel,
    frequencyLevel: vocab.freqRank || 4,
    tags: vocab.tags || '',
    notes: vocab.notes || '',
    isEnabled: vocab.isEnabled,
    displayOrder: vocab.displayOrder || 1
  })
  editDialogVisible.value = true
}

// 保存词汇
const saveVocab = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    const data = {
      ...editForm,
      definitionCn: editForm.definitionCn,
      exampleSentence: editForm.exampleSentence,
      exampleTranslation: editForm.exampleTranslation
    }
    
    if (editForm.id) {
      await vocabApi.updateVocab(editForm.id, data)
      ElMessage.success('词汇更新成功')
    } else {
      await vocabApi.createVocab(data)
      ElMessage.success('词汇创建成功')
    }
    
    editDialogVisible.value = false
    loadVocabs()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除词汇
const deleteVocab = (vocab) => {
  ElMessageBox.confirm(
    `确定要删除词汇 "${vocab.headword}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await vocabApi.deleteVocab(vocab.id)
      ElMessage.success('删除成功')
      loadVocabs()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const batchDelete = () => {
  if (selectedVocabs.value.length === 0) {
    ElMessage.warning('请选择要删除的词汇')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedVocabs.value.length} 个词汇吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const vocabIds = selectedVocabs.value.map(vocab => vocab.id)
      await vocabApi.batchDeleteVocabs(vocabIds)
      ElMessage.success('批量删除成功')
      loadVocabs()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 导入词汇
const beforeUpload = async (file) => {
  try {
    importing.value = true
    await vocabApi.importVocabs(file)
    ElMessage.success('导入成功')
    loadVocabs()
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
  return false // 阻止自动上传
}

// 导出词汇
const exportVocabs = async () => {
  try {
    exporting.value = true
    
    const blob = await vocabApi.exportVocabs(searchForm)
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `vocabs_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 播放音频
const playAudio = (audioUrl) => {
  const audio = new Audio(audioUrl)
  audio.play().catch(() => {
    ElMessage.error('音频播放失败')
  })
}

// 获取难度类型
const getDifficultyType = (level) => {
  if (level <= 3) return 'success'
  if (level <= 6) return 'warning'
  return 'danger'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadVocabs()
})
</script>

<style scoped>
.vocab-management {
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

.text-gray {
  color: #909399;
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

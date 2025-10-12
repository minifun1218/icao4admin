<template>
  <div class="paper-management">
    <el-card class="box-card">
      <!-- 页面标题和操作按钮 -->
      <template #header>
        <div class="card-header">
          <span class="card-title">试卷管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建试卷
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入试卷名称"
            clearable
            style="width: 300px"
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 试卷列表 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="code" label="试卷代码" width="180" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="试卷名称" min-width="200">
          <template #default="{ row }">
            <div class="paper-name">{{ row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="totalDurationMin" label="考试时长" width="120" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.totalDurationMin) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="350" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handlePreview(row)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="handleCopy(row)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 创建/编辑试卷对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form
        ref="paperFormRef"
        :model="paperForm"
        :rules="paperRules"
        label-width="120px"
      >
        <el-form-item label="试卷代码" prop="code">
          <el-input
            v-model="paperForm.code"
            placeholder="请输入试卷代码"
            :disabled="isEdit"
          >
            <template #append>
              <el-button v-if="!isEdit" @click="handleGenerateCode">
                <el-icon><Refresh /></el-icon>
                生成
              </el-button>
            </template>
          </el-input>
          <div class="form-item-tip">试卷唯一标识，创建后不可修改</div>
        </el-form-item>
        
        <el-form-item label="试卷名称" prop="name">
          <el-input
            v-model="paperForm.name"
            placeholder="请输入试卷名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="考试时长" prop="totalDurationMin">
          <el-input-number
            v-model="paperForm.totalDurationMin"
            :min="1"
            :max="1440"
            :step="30"
            placeholder="请输入考试时长（分钟）"
            style="width: 200px"
          />
          <span class="ml-10">分钟</span>
          <el-select
            v-model="paperForm.totalDurationMin"
            placeholder="快速选择"
            class="ml-10"
            style="width: 150px"
          >
            <el-option
              v-for="preset in durationPresets"
              :key="preset.value"
              :label="preset.label"
              :value="preset.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="paperForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入试卷描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 复制试卷对话框 -->
    <el-dialog
      v-model="copyDialogVisible"
      title="复制试卷"
      width="600px"
    >
      <el-form
        ref="copyFormRef"
        :model="copyForm"
        :rules="copyRules"
        label-width="120px"
      >
        <el-form-item label="原试卷">
          <el-input :value="currentPaper?.name" disabled />
        </el-form-item>
        <el-form-item label="新试卷代码" prop="code">
          <el-input
            v-model="copyForm.code"
            placeholder="请输入新试卷代码"
          >
            <template #append>
              <el-button @click="handleGenerateCopyCode">
                <el-icon><Refresh /></el-icon>
                生成
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="新试卷名称" prop="name">
          <el-input
            v-model="copyForm.name"
            placeholder="请输入新试卷名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="copyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="copyLoading" @click="handleConfirmCopy">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 试卷预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="试卷预览"
      width="90%"
      top="5vh"
    >
      <div v-loading="previewLoading" class="preview-container">
        <!-- 试卷基本信息 -->
        <el-descriptions :column="2" border class="paper-info">
          <el-descriptions-item label="试卷代码">
            <el-tag type="info">{{ currentPaper?.code }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="试卷名称">
            {{ currentPaper?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="考试时长">
            {{ formatDuration(currentPaper?.totalDurationMin) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(currentPaper?.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentPaper?.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 试卷模块列表 -->
        <el-divider content-position="left">
          <span class="divider-title">试卷模块（共 {{ previewModules.length }} 个）</span>
        </el-divider>

        <el-empty v-if="previewModules.length === 0" description="该试卷暂无模块" />
        
        <div v-else class="modules-container">
          <el-card
            v-for="(module, index) in previewModules"
            :key="module.id"
            class="module-card"
            shadow="hover"
          >
            <template #header>
              <div class="module-header">
                <span class="module-title">
                  <el-tag type="primary" class="mr-10">模块 {{ index + 1 }}</el-tag>
                  {{ getModuleTypeName(module.moduleType) }}
                </span>
                <div class="module-meta">
                  <el-tag size="small" type="info" class="mr-10">顺序: {{ module.displayOrder }}</el-tag>
                  <el-tag size="small" type="success">分数: {{ module.score || 0 }}</el-tag>
                </div>
              </div>
            </template>

            <!-- 模块题目列表 -->
            <div class="module-questions">
              <div v-if="getModuleQuestions(module).length === 0" class="no-questions">
                暂无题目
              </div>
              <div v-else>
                <div
                  v-for="(question, qIndex) in getModuleQuestions(module)"
                  :key="question.id"
                  class="question-item"
                >
                  <div class="question-number">题目 {{ qIndex + 1 }}</div>
                  <div class="question-content" v-html="formatQuestionContent(question, module.moduleType)"></div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePrint">
          <el-icon><Printer /></el-icon>
          打印
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  View,
  CopyDocument,
  Search,
  Refresh,
  Printer
} from '@element-plus/icons-vue'
import {
  getExamPapers,
  createExamPaper,
  updateExamPaper,
  deleteExamPaper,
  copyExamPaper,
  searchExamPapers,
  generateExamPaperCode,
  formatDuration,
  getDurationPresets
} from '@/api/exam-paper'
import { getModulesByExamPaperId } from '@/api/exam-module'

// 引入题库API
import * as listeningMcqApi from '@/api/listening-mcq'
import * as storyRetellApi from '@/api/story-retell'
import * as lsaDialogsApi from '@/api/lsa-dialogs'
import * as opiQuestionApi from '@/api/opi-question'
import * as atcScenarioApi from '@/api/atc-scenario'

// 模块类型映射
const moduleTypes = {
  'LISTENING_MCQ': { label: '听力理解（多选题）', tag: 'primary' },
  'STORY_RETELL': { label: '故事复述', tag: 'success' },
  'LISTENING_SA': { label: '听力简答题', tag: 'warning' },
  'ATC_SIM': { label: '模拟通话', tag: 'danger' },
  'OPI': { label: '口语能力面试', tag: 'info' }
}

// 数据状态
const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')

// 时长预设
const durationPresets = getDurationPresets()

// 分页
const pagination = reactive({
  page: 0,
  size: 20,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑试卷' : '新建试卷')
const isEdit = ref(false)
const submitLoading = ref(false)
const paperFormRef = ref(null)

// 表单数据
const paperForm = reactive({
  id: null,
  code: '',
  name: '',
  totalDurationMin: 120,
  description: ''
})

// 表单验证规则
const paperRules = {
  code: [
    { required: true, message: '请输入试卷代码', trigger: 'blur' },
    { max: 100, message: '试卷代码长度不能超过100个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入试卷名称', trigger: 'blur' },
    { max: 200, message: '试卷名称长度不能超过200个字符', trigger: 'blur' }
  ],
  totalDurationMin: [
    { required: true, message: '请输入考试时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 1440, message: '考试时长必须在1-1440分钟之间', trigger: 'blur' }
  ]
}

// 复制对话框
const copyDialogVisible = ref(false)
const copyLoading = ref(false)
const copyFormRef = ref(null)
const currentPaper = ref(null)
const copyForm = reactive({
  code: '',
  name: ''
})

const copyRules = {
  code: [
    { required: true, message: '请输入新试卷代码', trigger: 'blur' },
    { max: 100, message: '试卷代码长度不能超过100个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入新试卷名称', trigger: 'blur' },
    { max: 200, message: '试卷名称长度不能超过200个字符', trigger: 'blur' }
  ]
}

// 预览对话框
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewModules = ref([])
const previewQuestionsMap = ref({})

// ==================== 生命周期 ====================
onMounted(() => {
  loadPapers()
})

// ==================== 数据加载 ====================
const loadPapers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      sortBy: 'createdAt',
      sortDir: 'desc'
    }

    const response = await getExamPapers(params)
    if (response.data.code === 200 && response.data) {
      tableData.value = response.data.data.content || []
      pagination.total = response.data.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载试卷列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================
const handleSearch = async () => {
  if (!searchKeyword.value || searchKeyword.value.trim() === '') {
    loadPapers()
    return
  }

  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    const response = await searchExamPapers(searchKeyword.value, params)
    if (response.code === 200 && response.data) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('搜索失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchKeyword.value = ''
  pagination.page = 0
  loadPapers()
}

const handleSizeChange = () => {
  loadPapers()
}

const handleCurrentChange = () => {
  loadPapers()
}

const showCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleGenerateCode = async () => {
  try {
    const response = await generateExamPaperCode()
    console.log('生成代码响应:', response)
    
    if (response && response.data.code === 200) {
      // 处理不同的返回数据格式
      if (response.data && response.data.code) {
        paperForm.code = response.data.data.code
      }
    } else {
      ElMessage.error('生成代码失败：' + (response?.message || '未知错误'))
    }
  } catch (error) {
    console.error('生成代码异常:', error)
    ElMessage.error('生成代码失败：' + (error.message || error.response?.data?.message || '请求失败，可能是权限不足'))
  }
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(paperForm, {
    id: row.id,
    code: row.code,
    name: row.name,
    totalDurationMin: row.totalDurationMin,
    description: row.description
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个试卷吗？删除后将无法恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteExamPaper(row.id)
    ElMessage.success('删除成功')
    loadPapers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

const handleCopy = (row) => {
  currentPaper.value = row
  copyForm.code = ''
  copyForm.name = `${row.name} - 副本`
  copyDialogVisible.value = true
}

const handleGenerateCopyCode = async () => {
  try {
    const response = await generateExamPaperCode()
    console.log('生成复制代码响应:', response)
    
    if (response && response.data.code === 200) {
      // 处理不同的返回数据格式
      if (response.data && response.data.code) {
        copyForm.code = response.data.data.code
      }
    } else {
      ElMessage.error('生成代码失败：' + (response?.message || '未知错误'))
    }
  } catch (error) {
    console.error('生成复制代码异常:', error)
    ElMessage.error('生成代码失败：' + (error.message || error.response?.data?.message || '请求失败，可能是权限不足'))
  }
}

const handleConfirmCopy = async () => {
  if (!copyFormRef.value) return

  try {
    await copyFormRef.value.validate()
    copyLoading.value = true

    await copyExamPaper(currentPaper.value.id, copyForm.code, copyForm.name)
    ElMessage.success('复制成功')
    copyDialogVisible.value = false
    loadPapers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('复制失败：' + error.message)
    }
  } finally {
    copyLoading.value = false
  }
}

const handlePreview = async (row) => {
  currentPaper.value = row
  previewVisible.value = true
  previewLoading.value = true
  previewModules.value = []
  previewQuestionsMap.value = {}

  try {
    // 加载试卷的模块列表
    const response = await getModulesByExamPaperId(row.id, true)
    if (response.code === 200 && response.data) {
      previewModules.value = response.data || []

      // 加载每个模块的题目
      for (const module of previewModules.value) {
        await loadModuleQuestions(module)
      }
    }
  } catch (error) {
    ElMessage.error('加载试卷预览失败：' + error.message)
  } finally {
    previewLoading.value = false
  }
}

const loadModuleQuestions = async (module) => {
  try {
    // 解析配置获取题目ID列表
    let questionIds = []
    if (module.configJson) {
      const config = JSON.parse(module.configJson)
      questionIds = config.questionIds || []
    }

    if (questionIds.length === 0) {
      previewQuestionsMap.value[module.id] = []
      return
    }

    // 根据模块类型加载对应的题目详情
    const questions = []
    for (const id of questionIds) {
      try {
        let response
        switch (module.moduleType) {
          case 'LISTENING_MCQ':
            response = await listeningMcqApi.getQuestionById(id)
            break
          case 'STORY_RETELL':
            response = await storyRetellApi.getRetellItemById(id)
            break
          case 'LISTENING_SA':
            response = await lsaDialogsApi.getDialogById(id)
            break
          case 'ATC_SIM':
            response = await atcScenarioApi.getScenarioById(id)
            break
          case 'OPI':
            response = await opiQuestionApi.getQuestionById(id)
            break
        }
        if (response && response.code === 200 && response.data) {
          questions.push(response.data)
        }
      } catch (e) {
        console.error(`加载题目${id}失败`, e)
      }
    }

    previewQuestionsMap.value[module.id] = questions
  } catch (error) {
    console.error('解析模块配置失败', error)
    previewQuestionsMap.value[module.id] = []
  }
}

const handleSubmit = async () => {
  if (!paperFormRef.value) return

  try {
    await paperFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      // 更新试卷
      const updateData = {
        name: paperForm.name,
        totalDurationMin: paperForm.totalDurationMin,
        description: paperForm.description
      }
      await updateExamPaper(paperForm.id, updateData)
      ElMessage.success('更新成功')
    } else {
      // 创建试卷
      await createExamPaper({
        code: paperForm.code,
        name: paperForm.name,
        totalDurationMin: paperForm.totalDurationMin,
        description: paperForm.description
      })
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadPapers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  Object.assign(paperForm, {
    id: null,
    code: '',
    name: '',
    totalDurationMin: 120,
    description: ''
  })
  if (paperFormRef.value) {
    paperFormRef.value.resetFields()
  }
}

const handlePrint = () => {
  window.print()
}

// ==================== 辅助方法 ====================
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const getModuleTypeName = (type) => {
  return moduleTypes[type]?.label || type
}

const getModuleQuestions = (module) => {
  return previewQuestionsMap.value[module.id] || []
}

const formatQuestionContent = (question, moduleType) => {
  if (!question) return ''
  
  let html = '<div class="question-detail">'
  
  // 根据不同类型格式化内容
  if (moduleType === 'LISTENING_MCQ') {
    html += `<p><strong>题干：</strong>${question.textStem || ''}</p>`
    if (question.choices && question.choices.length > 0) {
      html += '<p><strong>选项：</strong></p><ul>'
      question.choices.forEach(choice => {
        const correct = choice.isCorrect ? ' <span class="correct-mark">✓</span>' : ''
        html += `<li>${choice.label}. ${choice.content}${correct}</li>`
      })
      html += '</ul>'
    }
    html += `<p class="meta"><strong>答题时间：</strong>${question.answerSeconds || 0}秒</p>`
  } else if (moduleType === 'STORY_RETELL') {
    html += `<p><strong>标题：</strong>${question.title || ''}</p>`
    html += `<p class="meta"><strong>音频时长：</strong>${question.audioDurationSec || 0}秒 | <strong>答题时间：</strong>${question.answerSeconds || 0}秒</p>`
    if (question.referenceText) {
      html += `<p><strong>参考文本：</strong>${question.referenceText}</p>`
    }
  } else if (moduleType === 'LISTENING_SA') {
    html += `<p><strong>对话：</strong>${question.title || ''}</p>`
    if (question.dialogText) {
      html += `<p><strong>对话内容：</strong></p><div class="dialog-text">${question.dialogText}</div>`
    }
    html += `<p class="meta"><strong>音频时长：</strong>${question.audioDurationSeconds || 0}秒</p>`
  } else if (moduleType === 'ATC_SIM') {
    html += `<p><strong>场景：</strong>${question.title || ''}</p>`
    html += `<p><strong>描述：</strong>${question.description || ''}</p>`
    html += `<p class="meta"><strong>场景类型：</strong>${question.scenarioType || ''} | <strong>难度：</strong>${question.difficultyLevel || ''}</p>`
  } else if (moduleType === 'OPI') {
    html += `<p><strong>问题：</strong>${question.questionText || ''}</p>`
    html += `<p class="meta"><strong>答题时间：</strong>${question.answerSeconds || 0}秒</p>`
    if (question.promptText) {
      html += `<p><strong>提示文本：</strong>${question.promptText}</p>`
    }
  }
  
  html += '</div>'
  return html
}
</script>

<style scoped>
.paper-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.ml-10 {
  margin-left: 10px;
}

.mr-10 {
  margin-right: 10px;
}

.paper-name {
  font-weight: 500;
  color: #303133;
}

/* 预览样式 */
.preview-container {
  min-height: 400px;
}

.paper-info {
  margin-bottom: 20px;
}

.divider-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.modules-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.module-card {
  border: 1px solid #e4e7ed;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.module-meta {
  display: flex;
  align-items: center;
}

.module-questions {
  padding: 10px 0;
}

.no-questions {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.question-item {
  padding: 12px;
  margin-bottom: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.question-item:last-child {
  margin-bottom: 0;
}

.question-number {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.question-content {
  color: #606266;
}

.question-detail :deep(p) {
  margin: 8px 0;
  line-height: 1.6;
}

.question-detail :deep(ul) {
  margin: 8px 0;
  padding-left: 20px;
}

.question-detail :deep(li) {
  margin: 4px 0;
  line-height: 1.6;
}

.question-detail :deep(strong) {
  color: #303133;
  font-weight: 600;
}

.question-detail :deep(.correct-mark) {
  color: #67c23a;
  font-weight: bold;
  margin-left: 8px;
}

.question-detail :deep(.meta) {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}

.question-detail :deep(.dialog-text) {
  padding: 10px;
  background-color: white;
  border-radius: 4px;
  margin: 8px 0;
  white-space: pre-wrap;
}

/* 打印样式 */
@media print {
  .el-dialog__header,
  .el-dialog__footer,
  .el-button {
    display: none !important;
  }
  
  .preview-container {
    padding: 0;
  }
  
  .module-card {
    page-break-inside: avoid;
  }
}
</style>

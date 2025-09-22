<template>
  <el-dialog
    v-model="visible"
    title="管理单选题题干"
    width="1000px"
    :close-on-click-modal="false"
    @close="$emit('close')"
  >
    <div class="question-panel">
      <!-- 操作栏 -->
      <div class="panel-header">
        <div class="header-info">
          <el-icon><List /></el-icon>
          <span>单选题题干管理</span>
          <el-tag type="info" size="small">{{ questions.length }}道题干</el-tag>
        </div>
        
        <div class="header-actions">
          <el-button type="success" @click="showCreateForm">
            <el-icon><Plus /></el-icon>
            添加题干
          </el-button>
        </div>
      </div>

      <!-- 题干列表 -->
      <div class="question-list">
        <el-table
          :data="questions"
          stripe
          class="question-table"
          empty-text="暂无题干数据"
        >
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column label="题干内容" min-width="300">
            <template #default="{ row }">
              <div class="question-content">
                <div class="content-text">{{ row.questionText }}</div>
                <div class="content-meta">
                  <el-tag v-if="row.hasImage" size="small" type="warning">
                    <el-icon><Picture /></el-icon>
                    图片
                  </el-tag>
                  <span class="choice-count">{{ row.choiceCount || 0 }}个选项</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="选项数" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getChoiceCountTagType(row.choiceCount)">
                {{ row.choiceCount || 0 }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="正确答案" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.correctChoice" type="success" size="small">
                {{ row.correctChoice }}
              </el-tag>
              <el-tag v-else type="danger" size="small">未设置</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="editQuestion(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="success" size="small" @click="manageChoices(row)">
                  <el-icon><Setting /></el-icon>
                  管理选项
                </el-button>
                <el-button type="danger" size="small" @click="deleteQuestion(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 创建/编辑题干表单 -->
      <el-dialog
        v-model="questionFormVisible"
        :title="questionForm.id ? '编辑题干' : '创建题干'"
        width="600px"
        :close-on-click-modal="false"
        append-to-body
      >
        <el-form
          ref="questionFormRef"
          :model="questionForm"
          :rules="questionRules"
          label-width="120px"
        >
          <el-form-item label="题干内容" prop="questionText">
            <el-input
              v-model="questionForm.questionText"
              type="textarea"
              :rows="4"
              placeholder="请输入题干内容"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="题目图片">
            <div class="image-upload-section">
              <el-upload
                ref="imageUploadRef"
                :action="imageUploadUrl"
                :before-upload="beforeImageUpload"
                :on-success="handleImageUploadSuccess"
                :on-error="handleImageUploadError"
                :show-file-list="false"
                accept="image/*"
              >
                <el-button type="primary">
                  <el-icon><Upload /></el-icon>
                  上传图片
                </el-button>
              </el-upload>
              
              <div v-if="questionForm.imageUrl" class="image-preview">
                <img :src="questionForm.imageUrl" alt="题目图片" style="max-width: 200px; max-height: 150px;" />
                <el-button type="danger" size="small" @click="removeImage" style="margin-top: 10px;">
                  移除图片
                </el-button>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="题目顺序">
            <el-input-number
              v-model="questionForm.questionOrder"
              :min="1"
              :max="99"
              placeholder="题目顺序"
            />
          </el-form-item>
          
          <el-form-item label="备注">
            <el-input
              v-model="questionForm.remark"
              type="textarea"
              :rows="2"
              placeholder="备注信息"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="questionFormVisible = false">取消</el-button>
          <el-button type="primary" @click="saveQuestion" :loading="saving">
            {{ questionForm.id ? '更新' : '创建' }}
          </el-button>
        </template>
      </el-dialog>
    </div>

    <template #footer>
      <el-button @click="$emit('close')">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionBankApi } from '@/api/questionBank'
import {
  List,
  Plus,
  Edit,
  Delete,
  Setting,
  Upload,
  Picture
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  dialogId: {
    type: [String, Number],
    required: true
  },
  questions: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['close', 'refresh', 'manage-choices'])

// 响应式数据
const questionFormVisible = ref(false)
const saving = ref(false)

const questionFormRef = ref(null)
const imageUploadRef = ref(null)

const localQuestions = ref([])

// 题干表单
const questionForm = reactive({
  id: null,
  dialogId: null,
  questionText: '',
  imageUrl: '',
  questionOrder: 1,
  remark: ''
})

// 计算属性
const imageUploadUrl = computed(() => '/api/listening-mcq/question-bank/upload/image')

// 表单验证规则
const questionRules = {
  questionText: [
    { required: true, message: '请输入题干内容', trigger: 'blur' },
    { min: 5, max: 1000, message: '题干内容长度为5-1000个字符', trigger: 'blur' }
  ]
}

// 监听props变化
watch(() => props.questions, (newQuestions) => {
  localQuestions.value = [...newQuestions]
  loadQuestionDetails()
}, { immediate: true })

watch(() => props.visible, (visible) => {
  if (visible && props.dialogId) {
    loadQuestions()
  }
})

// 方法
const loadQuestions = async () => {
  try {
    const response = await questionBankApi.getListeningDialogQuestions(props.dialogId)
    localQuestions.value = response.data?.content || []
    await loadQuestionDetails()
  } catch (error) {
    console.error('加载题干失败:', error)
    ElMessage.error('加载题干失败')
  }
}

const loadQuestionDetails = async () => {
  // 为每个问题加载选项数量和正确答案
  await Promise.all(localQuestions.value.map(async (question) => {
    try {
      const choicesResponse = await questionBankApi.getListeningDialogQuestionChoices(question.id)
      const choices = choicesResponse.data || []
      question.choiceCount = choices.length
      
      const correctChoice = choices.find(choice => choice.isCorrect)
      question.correctChoice = correctChoice ? correctChoice.choiceText : null
    } catch (error) {
      question.choiceCount = 0
      question.correctChoice = null
    }
  }))
}

const showCreateForm = () => {
  resetQuestionForm()
  questionForm.dialogId = props.dialogId
  questionFormVisible.value = true
}

const editQuestion = (question) => {
  Object.assign(questionForm, {
    ...question,
    dialogId: props.dialogId
  })
  questionFormVisible.value = true
}

const saveQuestion = async () => {
  if (!questionFormRef.value) return
  
  try {
    await questionFormRef.value.validate()
    saving.value = true
    
    const data = { ...questionForm }
    
    if (questionForm.id) {
      await questionBankApi.updateListeningDialogQuestion(questionForm.id, data)
      ElMessage.success('题干更新成功')
    } else {
      await questionBankApi.createListeningDialogQuestion(data)
      ElMessage.success('题干创建成功')
    }
    
    questionFormVisible.value = false
    await loadQuestions()
    emit('refresh', props.dialogId)
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const deleteQuestion = async (question) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除题干"${question.questionText.substring(0, 50)}..."吗？此操作将同时删除相关的所有选项。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await questionBankApi.deleteListeningDialogQuestion(question.id)
    ElMessage.success('删除成功')
    await loadQuestions()
    emit('refresh', props.dialogId)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const manageChoices = (question) => {
  emit('manage-choices', question)
}

const resetQuestionForm = () => {
  Object.assign(questionForm, {
    id: null,
    dialogId: null,
    questionText: '',
    imageUrl: '',
    questionOrder: 1,
    remark: ''
  })
  
  if (questionFormRef.value) {
    questionFormRef.value.clearValidate()
  }
}

// 图片上传相关方法
const beforeImageUpload = (file) => {
  const validation = questionBankApi.validateImageFile(file)
  if (!validation.valid) {
    ElMessage.error(validation.message)
    return false
  }
  return true
}

const handleImageUploadSuccess = (response) => {
  if (response.success) {
    questionForm.imageUrl = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleImageUploadError = () => {
  ElMessage.error('图片上传失败')
}

const removeImage = () => {
  questionForm.imageUrl = ''
  ElMessage.success('图片已移除')
}

// 工具方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getChoiceCountTagType = (count) => {
  if (!count || count === 0) return 'danger'
  if (count < 2) return 'warning'
  if (count < 4) return 'primary'
  return 'success'
}
</script>

<style scoped>
.question-panel {
  padding: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.question-table {
  border-radius: 8px;
  overflow: hidden;
}

.question-content {
  padding: 8px 0;
}

.content-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.choice-count {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.image-upload-section {
  width: 100%;
}

.image-preview {
  margin-top: 10px;
  text-align: center;
}

.image-preview img {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: block;
  margin: 0 auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .panel-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .content-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>

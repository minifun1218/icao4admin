<template>
  <el-dialog
    v-model="visible"
    title="管理单选题选项"
    width="800px"
    :close-on-click-modal="false"
    @close="$emit('close')"
  >
    <div class="choice-panel">
      <!-- 操作栏 -->
      <div class="panel-header">
        <div class="header-info">
          <el-icon><Menu /></el-icon>
          <span>选项管理</span>
          <el-tag type="info" size="small">{{ choices.length }}个选项</el-tag>
        </div>
        
        <div class="header-actions">
          <el-button type="success" @click="showCreateForm">
            <el-icon><Plus /></el-icon>
            添加选项
          </el-button>
          <el-button type="primary" @click="batchCreateChoices">
            <el-icon><DocumentAdd /></el-icon>
            批量添加
          </el-button>
        </div>
      </div>

      <!-- 选项列表 -->
      <div class="choice-list">
        <el-table
          :data="choices"
          stripe
          class="choice-table"
          empty-text="暂无选项数据"
        >
          <el-table-column label="选项" width="80" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="row.isCorrect ? 'success' : 'info'"
                :effect="row.isCorrect ? 'dark' : 'plain'"
                size="large"
              >
                {{ row.choiceLabel || getChoiceLabel(row._index) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="选项内容" min-width="300">
            <template #default="{ row }">
              <div class="choice-content">
                <div class="content-text">{{ row.choiceText }}</div>
                <div class="content-meta" v-if="row.explanation">
                  <el-tag size="small" type="warning">
                    <el-icon><InfoFilled /></el-icon>
                    有解释
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="正确答案" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.isCorrect"
                @change="setCorrectChoice(row)"
                active-color="#67C23A"
                inactive-color="#dcdfe6"
              />
            </template>
          </el-table-column>

          <el-table-column label="顺序" width="80" align="center">
            <template #default="{ row }">
              {{ row.choiceOrder || row._index + 1 }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="editChoice(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="deleteChoice(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 创建/编辑选项表单 -->
      <el-dialog
        v-model="choiceFormVisible"
        :title="choiceForm.id ? '编辑选项' : '创建选项'"
        width="500px"
        :close-on-click-modal="false"
        append-to-body
      >
        <el-form
          ref="choiceFormRef"
          :model="choiceForm"
          :rules="choiceRules"
          label-width="100px"
        >
          <el-form-item label="选项标签">
            <el-select v-model="choiceForm.choiceLabel" placeholder="选择选项标签">
              <el-option label="A" value="A" />
              <el-option label="B" value="B" />
              <el-option label="C" value="C" />
              <el-option label="D" value="D" />
              <el-option label="E" value="E" />
              <el-option label="F" value="F" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="选项内容" prop="choiceText">
            <el-input
              v-model="choiceForm.choiceText"
              type="textarea"
              :rows="3"
              placeholder="请输入选项内容"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="选项顺序">
            <el-input-number
              v-model="choiceForm.choiceOrder"
              :min="1"
              :max="10"
              placeholder="显示顺序"
            />
          </el-form-item>
          
          <el-form-item label="正确答案">
            <el-switch
              v-model="choiceForm.isCorrect"
              active-text="是"
              inactive-text="否"
            />
          </el-form-item>
          
          <el-form-item label="解释说明">
            <el-input
              v-model="choiceForm.explanation"
              type="textarea"
              :rows="2"
              placeholder="选项解释（可选）"
              maxlength="300"
              show-word-limit
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="choiceFormVisible = false">取消</el-button>
          <el-button type="primary" @click="saveChoice" :loading="saving">
            {{ choiceForm.id ? '更新' : '创建' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 批量创建选项对话框 -->
      <el-dialog
        v-model="batchCreateVisible"
        title="批量创建选项"
        width="600px"
        :close-on-click-modal="false"
        append-to-body
      >
        <div class="batch-create-form">
          <el-alert
            title="批量创建说明"
            type="info"
            :closable="false"
            style="margin-bottom: 16px;"
          >
            <template #default>
              <p>每行一个选项，格式：选项内容</p>
              <p>在选项内容前加 * 表示正确答案，例如：*正确选项内容</p>
            </template>
          </el-alert>
          
          <el-form label-width="100px">
            <el-form-item label="选项内容">
              <el-input
                v-model="batchChoicesText"
                type="textarea"
                :rows="8"
                placeholder="请输入选项内容，每行一个选项&#10;例如：&#10;选项A内容&#10;*选项B内容（正确答案）&#10;选项C内容&#10;选项D内容"
              />
            </el-form-item>
          </el-form>
          
          <div class="preview-section" v-if="parsedBatchChoices.length > 0">
            <h4>预览 ({{ parsedBatchChoices.length }}个选项)</h4>
            <el-table :data="parsedBatchChoices" size="small">
              <el-table-column label="标签" width="60" align="center">
                <template #default="{ $index }">
                  {{ getChoiceLabel($index) }}
                </template>
              </el-table-column>
              <el-table-column prop="text" label="内容" />
              <el-table-column label="正确答案" width="80" align="center">
                <template #default="{ row }">
                  <el-icon v-if="row.isCorrect" color="#67C23A"><Check /></el-icon>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <template #footer>
          <el-button @click="batchCreateVisible = false">取消</el-button>
          <el-button type="primary" @click="saveBatchChoices" :loading="saving">
            创建选项
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
  Menu,
  Plus,
  Edit,
  Delete,
  DocumentAdd,
  InfoFilled,
  Check
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  questionId: {
    type: [String, Number],
    required: true
  },
  choices: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['close', 'refresh'])

// 响应式数据
const choiceFormVisible = ref(false)
const batchCreateVisible = ref(false)
const saving = ref(false)

const choiceFormRef = ref(null)

const localChoices = ref([])
const batchChoicesText = ref('')

// 选项表单
const choiceForm = reactive({
  id: null,
  questionId: null,
  choiceLabel: 'A',
  choiceText: '',
  choiceOrder: 1,
  isCorrect: false,
  explanation: ''
})

// 计算属性
const parsedBatchChoices = computed(() => {
  if (!batchChoicesText.value.trim()) return []
  
  const lines = batchChoicesText.value.split('\n').filter(line => line.trim())
  return lines.map(line => {
    const isCorrect = line.startsWith('*')
    const text = isCorrect ? line.substring(1).trim() : line.trim()
    return { text, isCorrect }
  })
})

// 表单验证规则
const choiceRules = {
  choiceText: [
    { required: true, message: '请输入选项内容', trigger: 'blur' },
    { min: 1, max: 500, message: '选项内容长度为1-500个字符', trigger: 'blur' }
  ]
}

// 监听props变化
watch(() => props.choices, (newChoices) => {
  localChoices.value = newChoices.map((choice, index) => ({
    ...choice,
    _index: index
  }))
}, { immediate: true })

watch(() => props.visible, (visible) => {
  if (visible && props.questionId) {
    loadChoices()
  }
})

// 方法
const loadChoices = async () => {
  try {
    const response = await questionBankApi.getListeningDialogQuestionChoices(props.questionId)
    localChoices.value = (response.data || []).map((choice, index) => ({
      ...choice,
      _index: index
    }))
  } catch (error) {
    console.error('加载选项失败:', error)
    ElMessage.error('加载选项失败')
  }
}

const showCreateForm = () => {
  resetChoiceForm()
  choiceForm.questionId = props.questionId
  choiceForm.choiceLabel = getNextChoiceLabel()
  choiceForm.choiceOrder = localChoices.value.length + 1
  choiceFormVisible.value = true
}

const editChoice = (choice) => {
  Object.assign(choiceForm, {
    ...choice,
    questionId: props.questionId
  })
  choiceFormVisible.value = true
}

const saveChoice = async () => {
  if (!choiceFormRef.value) return
  
  try {
    await choiceFormRef.value.validate()
    saving.value = true
    
    const data = { ...choiceForm }
    
    if (choiceForm.id) {
      await questionBankApi.updateListeningDialogQuestionChoice(choiceForm.id, data)
      ElMessage.success('选项更新成功')
    } else {
      await questionBankApi.createListeningDialogQuestionChoice(data)
      ElMessage.success('选项创建成功')
    }
    
    choiceFormVisible.value = false
    await loadChoices()
    emit('refresh', props.questionId)
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const deleteChoice = async (choice) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选项"${choice.choiceText.substring(0, 30)}..."吗？`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await questionBankApi.deleteListeningDialogQuestionChoice(choice.id)
    ElMessage.success('删除成功')
    await loadChoices()
    emit('refresh', props.questionId)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const setCorrectChoice = async (choice) => {
  try {
    if (choice.isCorrect) {
      // 设置为正确答案时，需要取消其他选项的正确状态
      localChoices.value.forEach(c => {
        if (c.id !== choice.id) {
          c.isCorrect = false
        }
      })
      
      await questionBankApi.setListeningDialogCorrectChoice(choice.id)
      ElMessage.success('已设置为正确答案')
    } else {
      // 取消正确答案
      await questionBankApi.updateListeningDialogQuestionChoice(choice.id, { isCorrect: false })
      ElMessage.success('已取消正确答案')
    }
    
    await loadChoices()
    emit('refresh', props.questionId)
  } catch (error) {
    // 回滚状态
    choice.isCorrect = !choice.isCorrect
    ElMessage.error('设置失败')
  }
}

const batchCreateChoices = () => {
  batchChoicesText.value = ''
  batchCreateVisible.value = true
}

const saveBatchChoices = async () => {
  if (parsedBatchChoices.value.length === 0) {
    ElMessage.warning('请输入选项内容')
    return
  }
  
  try {
    saving.value = true
    
    const choices = parsedBatchChoices.value.map((choice, index) => ({
      questionId: props.questionId,
      choiceLabel: getChoiceLabel(localChoices.value.length + index),
      choiceText: choice.text,
      choiceOrder: localChoices.value.length + index + 1,
      isCorrect: choice.isCorrect
    }))
    
    await questionBankApi.batchCreateListeningDialogQuestionChoices(props.questionId, choices)
    ElMessage.success(`成功创建 ${choices.length} 个选项`)
    
    batchCreateVisible.value = false
    await loadChoices()
    emit('refresh', props.questionId)
  } catch (error) {
    ElMessage.error('批量创建失败')
  } finally {
    saving.value = false
  }
}

const resetChoiceForm = () => {
  Object.assign(choiceForm, {
    id: null,
    questionId: null,
    choiceLabel: 'A',
    choiceText: '',
    choiceOrder: 1,
    isCorrect: false,
    explanation: ''
  })
  
  if (choiceFormRef.value) {
    choiceFormRef.value.clearValidate()
  }
}

// 工具方法
const getChoiceLabel = (index) => {
  const labels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J']
  return labels[index] || `${index + 1}`
}

const getNextChoiceLabel = () => {
  const existingLabels = localChoices.value.map(c => c.choiceLabel).filter(Boolean)
  const allLabels = ['A', 'B', 'C', 'D', 'E', 'F']
  
  for (const label of allLabels) {
    if (!existingLabels.includes(label)) {
      return label
    }
  }
  
  return 'A'
}
</script>

<style scoped>
.choice-panel {
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

.choice-table {
  border-radius: 8px;
  overflow: hidden;
}

.choice-content {
  padding: 8px 0;
}

.content-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.5;
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.batch-create-form {
  padding: 0;
}

.preview-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.preview-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
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
}
</style>

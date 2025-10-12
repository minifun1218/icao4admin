<template>
  <div class="turn-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>
            <el-button text @click="goBackToScenarios">
              <el-icon><ArrowLeft /></el-icon>
              返回场景列表
            </el-button>
          </el-breadcrumb-item>
        </el-breadcrumb>
        <h1>轮次管理</h1>
        <el-tag v-if="currentScenarioTitle" type="info" class="ml-2">
          {{ currentScenarioTitle }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateTurn = true">
          <el-icon><Plus /></el-icon>
          新建轮次
        </el-button>
        <el-button @click="showImportTurn = true">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="exportTurnsAction">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
        <el-button @click="reorderTurns" v-if="turnList.length > 1">
          <el-icon><Sort /></el-icon>
          排序
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索转录文本、预期回答..."
            @input="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.speakerType" placeholder="说话者类型" clearable @change="handleFilter">
            <el-option
              v-for="type in speakerTypeOptions"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.difficultyLevel" placeholder="难度等级" clearable @change="handleFilter">
            <el-option
              v-for="level in difficultyLevelOptions"
              :key="level.value"
              :label="level.label"
              :value="level.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.isActive" placeholder="状态" clearable @change="handleFilter">
            <el-option label="激活" :value="true" />
            <el-option label="未激活" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedTurns.length > 0">
      <el-alert
        :title="`已选择 ${selectedTurns.length} 项`"
        type="info"
        show-icon
        :closable="false"
      >
        <template #default>
          <el-button size="small" @click="batchActivate">批量激活</el-button>
          <el-button size="small" @click="batchDeactivate">批量停用</el-button>
          <el-button size="small" @click="batchUpdateDifficulty">批量设置难度</el-button>
          <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
        </template>
      </el-alert>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="turnList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="turnNumber" label="序号" width="80" sortable="custom" align="center">
          <template #default="scope">
            <el-tag type="primary" class="turn-number">{{ scope.row.turnNumber }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="说话者" width="100" sortable="custom">
          <template #default="scope">
            <el-tag 
              :type="getSpeakerTypeTagType(scope.row.speakerType)" 
              class="speaker-tag"
            >
              {{ getSpeakerTypeDescription(scope.row.speakerType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="转录内容" min-width="250">
          <template #default="scope">
            <div class="transcript-content">
              <el-text 
                class="transcript-text" 
                @click="viewTurn(scope.row)" 
                style="cursor: pointer; color: #409eff;"
              >
                {{ scope.row.transcriptText ? 
                    (scope.row.transcriptText.substring(0, 80) + 
                     (scope.row.transcriptText.length > 80 ? '...' : '')) : 
                    '无转录文本' }}
              </el-text>
              <div class="audio-info mt-1" v-if="scope.row.audioFilePath || scope.row.audioDuration">
                <el-icon class="audio-icon"><VideoPlay /></el-icon>
                <el-text type="info" size="small">
                  {{ formatAudioDuration(scope.row.audioDuration) }}
                </el-text>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="预期回答" width="200">
          <template #default="scope">
            <el-text v-if="scope.row.expectedResponse" class="expected-response">
              {{ scope.row.expectedResponse.substring(0, 50) }}{{ scope.row.expectedResponse.length > 50 ? '...' : '' }}
            </el-text>
            <el-text v-else type="info">无预期回答</el-text>
          </template>
        </el-table-column>

        <el-table-column label="分数/难度" width="120" align="center">
          <template #default="scope">
            <div class="score-difficulty">
              <el-text class="max-score">{{ scope.row.maxScore || 10 }}分</el-text>
              <el-tag 
                :type="getTurnDifficultyTagType(scope.row.difficultyLevel)" 
                size="small"
                class="difficulty-tag"
              >
                {{ getTurnDifficultyDescription(scope.row.difficultyLevel) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="完成度" width="120" align="center">
          <template #default="scope">
            <div class="completeness-indicator">
              <el-progress
                :percentage="calculateTurnCompleteness(scope.row)"
                :color="getCompletenessColor(calculateTurnCompleteness(scope.row))"
                :show-text="false"
                stroke-width="8"
              />
              <el-text size="small" class="completeness-text">
                {{ calculateTurnCompleteness(scope.row) }}%
              </el-text>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <div class="status-column">
              <el-switch
                v-model="scope.row.isActive"
                @change="toggleTurnStatus(scope.row)"
                :disabled="!hasPermission('ADMIN')"
                size="small"
              />
              <el-tag 
                :type="getTurnStatusTagType(scope.row)"
                size="small"
                class="status-tag"
              >
                {{ getTurnStatusDescription(scope.row) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="音频" width="80" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.audioFilePath" 
              size="small" 
              text 
              @click="playAudio(scope.row)"
              :loading="playingAudio === scope.row.id"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <el-text v-else type="info" size="small">无音频</el-text>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewTurn(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="editTurn(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              @click="copyTurnAction(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteTurnAction(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑轮次对话框 -->
    <el-dialog
      v-model="showCreateTurn"
      :title="isEdit ? '编辑轮次' : '新建轮次'"
      width="900px"
      @close="resetTurnForm"
    >
      <el-form
        ref="turnFormRef"
        :model="turnForm"
        :rules="turnRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="轮次序号" prop="turnNumber">
              <el-input-number
                v-model="turnForm.turnNumber"
                :min="1"
                :max="100"
                placeholder="轮次序号"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="说话者类型" prop="speakerType">
              <el-select 
                v-model="turnForm.speakerType" 
                placeholder="请选择说话者" 
                style="width: 100%"
              >
                <el-option
                  v-for="type in speakerTypeOptions"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-select 
                v-model="turnForm.difficultyLevel" 
                placeholder="请选择难度" 
                style="width: 100%"
              >
                <el-option
                  v-for="level in difficultyLevelOptions"
                  :key="level.value"
                  :label="level.label"
                  :value="level.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="转录文本" prop="transcriptText">
          <el-input
            v-model="turnForm.transcriptText"
            type="textarea"
            :rows="4"
            placeholder="请输入转录文本"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="预期回答">
          <el-input
            v-model="turnForm.expectedResponse"
            type="textarea"
            :rows="3"
            placeholder="请输入预期回答"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="评分标准">
          <el-input
            v-model="turnForm.scoringCriteria"
            type="textarea"
            :rows="3"
            placeholder="请输入评分标准"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最大分数" prop="maxScore">
              <el-input-number
                v-model="turnForm.maxScore"
                :min="0"
                :max="100"
                :precision="1"
                placeholder="最大分数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="音频时长(秒)">
              <el-input-number
                v-model="turnForm.audioDuration"
                :min="0"
                :max="3600"
                placeholder="音频时长"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否必答">
              <el-switch
                v-model="turnForm.isRequired"
                active-text="必答"
                inactive-text="选答"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="音频文件">
          <div class="audio-upload-section">
            <el-upload
              ref="audioUploadRef"
              :auto-upload="false"
              :limit="1"
              accept="audio/*"
              :on-change="handleAudioFileChange"
              :on-remove="handleAudioFileRemove"
              :file-list="audioFileList"
              drag
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将音频文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持MP3、WAV、M4A、OGG格式，文件大小不超过10MB
                </div>
              </template>
            </el-upload>
            
            <div v-if="turnForm.audioFilePath" class="current-audio mt-2">
              <el-text type="primary">当前音频：</el-text>
              <el-button size="small" @click="playCurrentAudio" :loading="playingCurrentAudio">
                <el-icon><VideoPlay /></el-icon>
                播放
              </el-button>
              <el-button size="small" type="danger" @click="removeCurrentAudio">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="turnForm.isActive"
                active-text="激活"
                inactive-text="停用"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <div class="turn-preview">
              <el-text class="preview-label">完成度预览：</el-text>
              <el-progress
                :percentage="calculateTurnCompleteness(turnForm)"
                :color="getCompletenessColor(calculateTurnCompleteness(turnForm))"
                :show-text="false"
                stroke-width="6"
                style="width: 100px"
              />
              <el-text size="small">{{ calculateTurnCompleteness(turnForm) }}%</el-text>
            </div>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateTurn = false">取消</el-button>
          <el-button type="primary" @click="submitTurn" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看轮次详情对话框 -->
    <el-dialog
      v-model="showViewTurn"
      title="轮次详情"
      width="800px"
    >
      <div v-if="currentTurn" class="turn-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="轮次序号">
            <el-tag type="primary">第 {{ currentTurn.turnNumber }} 轮</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="说话者类型">
            <el-tag :type="getSpeakerTypeTagType(currentTurn.speakerType)">
              {{ getSpeakerTypeDescription(currentTurn.speakerType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="难度等级">
            <el-tag :type="getTurnDifficultyTagType(currentTurn.difficultyLevel)">
              {{ getTurnDifficultyDescription(currentTurn.difficultyLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最大分数">
            {{ currentTurn.maxScore || 10 }}分
          </el-descriptions-item>
          <el-descriptions-item label="音频时长">
            {{ formatAudioDuration(currentTurn.audioDuration) }}
          </el-descriptions-item>
          <el-descriptions-item label="是否必答">
            <el-tag :type="currentTurn.isRequired ? 'success' : 'info'">
              {{ currentTurn.isRequired ? '必答' : '选答' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getTurnStatusTagType(currentTurn)">
              {{ getTurnStatusDescription(currentTurn) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="完成度">
            {{ calculateTurnCompleteness(currentTurn) }}%
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentTurn.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(currentTurn.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentTurn.transcriptText" class="mt-4">
          <h4>转录文本</h4>
          <el-text class="transcript-text">{{ currentTurn.transcriptText }}</el-text>
        </div>

        <div v-if="currentTurn.expectedResponse" class="mt-4">
          <h4>预期回答</h4>
          <el-text class="expected-response">{{ currentTurn.expectedResponse }}</el-text>
        </div>

        <div v-if="currentTurn.scoringCriteria" class="mt-4">
          <h4>评分标准</h4>
          <el-text class="scoring-criteria">{{ currentTurn.scoringCriteria }}</el-text>
        </div>

        <div v-if="currentTurn.audioFilePath" class="mt-4">
          <h4>音频文件</h4>
          <div class="audio-player">
            <el-button @click="playDetailAudio" :loading="playingDetailAudio">
              <el-icon><VideoPlay /></el-icon>
              播放音频
            </el-button>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showViewTurn = false">关闭</el-button>
          <el-button type="primary" @click="editTurn(currentTurn)" v-if="hasPermission('ADMIN')">
            编辑轮次
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量设置难度对话框 -->
    <el-dialog
      v-model="showBatchDifficulty"
      title="批量设置难度"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="难度等级">
          <el-select 
            v-model="batchDifficultyLevel" 
            placeholder="请选择难度等级" 
            style="width: 100%"
          >
            <el-option
              v-for="level in difficultyLevelOptions"
              :key="level.value"
              :label="level.label"
              :value="level.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchDifficulty = false">取消</el-button>
          <el-button type="primary" @click="submitBatchDifficulty" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入轮次对话框 -->
    <el-dialog
      v-model="showImportTurn"
      title="批量导入轮次"
      width="600px"
    >
      <div class="import-section">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          class="mb-4"
        >
          <template #default>
            <p>支持Excel(.xlsx)和CSV(.csv)格式文件</p>
            <p>请先下载模板文件，按照格式填写数据后上传</p>
            <p>必填字段：轮次序号、说话者类型、转录文本</p>
          </template>
        </el-alert>

        <div class="mb-4">
          <el-button @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
        </div>

        <el-upload
          ref="importUploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.csv"
          :on-change="handleImportFileChange"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传xlsx/csv文件，且不超过10MB
            </div>
          </template>
        </el-upload>

        <div class="import-options mt-4">
          <el-checkbox v-model="importOptions.skipDuplicates">跳过重复数据</el-checkbox>
          <el-checkbox v-model="importOptions.updateExisting">更新已存在的数据</el-checkbox>
          <el-checkbox v-model="importOptions.validateData">验证数据格式</el-checkbox>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showImportTurn = false">取消</el-button>
          <el-button type="primary" @click="submitImport" :loading="importing">
            开始导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  ArrowLeft,
  Plus,
  Search,
  Refresh,
  Upload,
  Download,
  Sort,
  View,
  Edit,
  Delete,
  DocumentCopy,
  VideoPlay,
  UploadFilled
} from '@element-plus/icons-vue'

// API导入
import {
  getTurnsByScenarioId,
  getTurnById,
  createTurn,
  updateTurn,
  deleteTurn,
  copyTurn,
  searchTurns,
  toggleTurnActive,
  batchDeleteTurns,
  batchActivateTurns,
  batchDeactivateTurns,
  batchOperateTurns,
  uploadAudioFile,
  deleteAudioFile,
  getAudioFileUrl,
  exportTurnTemplate,
  importTurns,
  validateTurnData,
  generateTurnTemplate,
  formatAudioDuration,
  getSpeakerTypeDescription,
  getTurnDifficultyDescription,
  getSpeakerTypeOptions,
  getTurnDifficultyLevelOptions,
  isTurnComplete,
  canTurnBeScored,
  getTurnStatusDescription,
  getSpeakerTypeTagType,
  getTurnDifficultyTagType,
  getTurnStatusTagType,
  calculateTurnCompleteness,
  getCompletenessColor,
  validateAudioFile
} from '@/api/atc-turn'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 获取场景ID
const scenarioId = computed(() => route.params.scenarioId)
const currentScenarioTitle = computed(() => route.query.scenarioTitle)

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const playingAudio = ref(null)
const playingCurrentAudio = ref(false)
const playingDetailAudio = ref(false)
const turnList = ref([])
const selectedTurns = ref([])
const currentTurn = ref(null)

// 搜索和筛选
const searchKeyword = ref('')
const filterParams = reactive({
  speakerType: null,
  difficultyLevel: null,
  isActive: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 排序
const sortParams = reactive({
  prop: 'turnNumber',
  order: 'ascending'
})

// 对话框状态
const showCreateTurn = ref(false)
const showViewTurn = ref(false)
const showImportTurn = ref(false)
const showBatchDifficulty = ref(false)
const isEdit = ref(false)

// 表单
const turnFormRef = ref(null)
const turnForm = reactive(generateTurnTemplate())
const turnRules = {
  turnNumber: [
    { required: true, message: '请输入轮次序号', trigger: 'blur' },
    { type: 'number', min: 1, message: '轮次序号必须大于0', trigger: 'blur' }
  ],
  speakerType: [
    { required: true, message: '请选择说话者类型', trigger: 'change' }
  ],
  transcriptText: [
    { required: true, message: '请输入转录文本', trigger: 'blur' },
    { max: 2000, message: '转录文本长度不能超过2000个字符', trigger: 'blur' }
  ],
  maxScore: [
    { required: true, message: '请设置最大分数', trigger: 'blur' },
    { type: 'number', min: 0, message: '最大分数不能小于0', trigger: 'blur' }
  ]
}

// 音频文件
const audioFileList = ref([])
const audioUploadRef = ref(null)

// 批量操作
const batchDifficultyLevel = ref(null)

// 导入选项
const importOptions = reactive({
  skipDuplicates: true,
  updateExisting: false,
  validateData: true
})

// 选项数据
const speakerTypeOptions = ref(getSpeakerTypeOptions())
const difficultyLevelOptions = ref(getTurnDifficultyLevelOptions())

// 权限检查
const hasPermission = (role) => {
  return authStore.hasRole(role)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 返回场景列表
const goBackToScenarios = () => {
  router.push('/atc-simulation/scenarios')
}

// 加载轮次列表
const loadTurns = async () => {
  if (!scenarioId.value) return
  
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: [sortParams.prop, sortParams.order === 'ascending' ? 'asc' : 'desc']
    }

    // 添加筛选参数
    if (filterParams.speakerType) params.speakerType = filterParams.speakerType
    if (filterParams.difficultyLevel) params.difficultyLevel = filterParams.difficultyLevel
    if (filterParams.isActive !== null) params.isActive = filterParams.isActive

    let response
    if (searchKeyword.value) {
      response = await searchTurns(searchKeyword.value, { ...params, scenarioId: scenarioId.value })
    } else {
      response = await getTurnsByScenarioId(scenarioId.value, params)
    }

    turnList.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载轮次列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadTurns()
}

// 筛选处理
const handleFilter = () => {
  pagination.page = 1
  loadTurns()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  Object.assign(filterParams, {
    speakerType: null,
    difficultyLevel: null,
    isActive: null
  })
  pagination.page = 1
  loadTurns()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadTurns()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadTurns()
}

// 排序处理
const handleSortChange = ({ prop, order }) => {
  sortParams.prop = prop || 'turnNumber'
  sortParams.order = order || 'ascending'
  loadTurns()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedTurns.value = selection
}

// 查看轮次
const viewTurn = async (turn) => {
  try {
    const response = await getTurnById(turn.id)
    currentTurn.value = response.data
    showViewTurn.value = true
  } catch (error) {
    ElMessage.error('获取轮次详情失败：' + error.message)
  }
}

// 编辑轮次
const editTurn = (turn) => {
  isEdit.value = true
  Object.assign(turnForm, turn)
  showCreateTurn.value = true
}

// 复制轮次
const copyTurnAction = async (turn) => {
  try {
    await copyTurn(turn.id)
    ElMessage.success('轮次复制成功')
    loadTurns()
  } catch (error) {
    ElMessage.error('复制轮次失败：' + error.message)
  }
}

// 删除轮次
const deleteTurnAction = (turn) => {
  ElMessageBox.confirm(
    `确定要删除第${turn.turnNumber}轮次吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteTurn(turn.id)
      ElMessage.success('删除成功')
      loadTurns()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

// 切换轮次状态
const toggleTurnStatus = async (turn) => {
  try {
    await toggleTurnActive(turn.id)
    ElMessage.success(`轮次已${turn.isActive ? '激活' : '停用'}`)
  } catch (error) {
    turn.isActive = !turn.isActive // 恢复状态
    ElMessage.error('状态切换失败：' + error.message)
  }
}

// 播放音频
const playAudio = async (turn) => {
  try {
    playingAudio.value = turn.id
    // 这里应该调用实际的音频播放逻辑
    ElMessage.info('音频播放功能正在开发中')
  } catch (error) {
    ElMessage.error('播放音频失败：' + error.message)
  } finally {
    playingAudio.value = null
  }
}

// 播放当前音频
const playCurrentAudio = async () => {
  try {
    playingCurrentAudio.value = true
    // 这里应该调用实际的音频播放逻辑
    ElMessage.info('音频播放功能正在开发中')
  } catch (error) {
    ElMessage.error('播放音频失败：' + error.message)
  } finally {
    playingCurrentAudio.value = false
  }
}

// 播放详情音频
const playDetailAudio = async () => {
  try {
    playingDetailAudio.value = true
    // 这里应该调用实际的音频播放逻辑
    ElMessage.info('音频播放功能正在开发中')
  } catch (error) {
    ElMessage.error('播放音频失败：' + error.message)
  } finally {
    playingDetailAudio.value = false
  }
}

// 删除当前音频
const removeCurrentAudio = () => {
  ElMessageBox.confirm('确定要删除当前音频文件吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    turnForm.audioFilePath = ''
    turnForm.audioDuration = null
    ElMessage.success('音频文件已删除')
  })
}

// 音频文件变化处理
const handleAudioFileChange = (file) => {
  const errors = validateAudioFile(file.raw)
  if (errors.length > 0) {
    ElMessage.error(errors[0])
    audioFileList.value = []
    return
  }
  audioFileList.value = [file]
}

// 音频文件移除处理
const handleAudioFileRemove = () => {
  audioFileList.value = []
}

// 批量操作
const batchActivate = async () => {
  try {
    const ids = selectedTurns.value.map(t => t.id)
    await batchActivateTurns(ids)
    ElMessage.success('批量激活成功')
    loadTurns()
  } catch (error) {
    ElMessage.error('批量激活失败：' + error.message)
  }
}

const batchDeactivate = async () => {
  try {
    const ids = selectedTurns.value.map(t => t.id)
    await batchDeactivateTurns(ids)
    ElMessage.success('批量停用成功')
    loadTurns()
  } catch (error) {
    ElMessage.error('批量停用失败：' + error.message)
  }
}

const batchUpdateDifficulty = () => {
  batchDifficultyLevel.value = null
  showBatchDifficulty.value = true
}

const submitBatchDifficulty = async () => {
  if (!batchDifficultyLevel.value) {
    ElMessage.warning('请选择难度等级')
    return
  }

  try {
    submitting.value = true
    const ids = selectedTurns.value.map(t => t.id)
    await batchOperateTurns('updateDifficulty', ids, { difficultyLevel: batchDifficultyLevel.value })
    ElMessage.success('批量设置难度成功')
    showBatchDifficulty.value = false
    loadTurns()
  } catch (error) {
    ElMessage.error('批量设置难度失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

const batchDelete = () => {
  const count = selectedTurns.value.length
  ElMessageBox.confirm(
    `确定要删除选中的 ${count} 个轮次吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedTurns.value.map(t => t.id)
      await batchDeleteTurns(ids)
      ElMessage.success('批量删除成功')
      selectedTurns.value = []
      loadTurns()
    } catch (error) {
      ElMessage.error('批量删除失败：' + error.message)
    }
  })
}

// 轮次排序
const reorderTurns = () => {
  ElMessage.info('轮次排序功能正在开发中')
}

// 提交轮次表单
const submitTurn = async () => {
  if (!turnFormRef.value) return

  try {
    await turnFormRef.value.validate()
    
    // 设置场景ID
    turnForm.scenarioId = scenarioId.value
    
    // 验证数据
    const errors = validateTurnData(turnForm)
    if (errors.length > 0) {
      ElMessage.error('表单验证失败：' + errors[0])
      return
    }

    submitting.value = true

    // 如果有音频文件需要上传
    if (audioFileList.value.length > 0) {
      try {
        const uploadResponse = await uploadAudioFile(turnForm.id || 'temp', audioFileList.value[0].raw)
        turnForm.audioFilePath = uploadResponse.data.filePath
        turnForm.audioDuration = uploadResponse.data.duration
      } catch (uploadError) {
        ElMessage.warning('音频上传失败，但轮次数据将保存')
      }
    }

    if (isEdit.value) {
      await updateTurn(turnForm.id, turnForm)
      ElMessage.success('轮次更新成功')
    } else {
      await createTurn(turnForm)
      ElMessage.success('轮次创建成功')
    }

    showCreateTurn.value = false
    loadTurns()
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetTurnForm = () => {
  if (turnFormRef.value) {
    turnFormRef.value.resetFields()
  }
  Object.assign(turnForm, generateTurnTemplate())
  audioFileList.value = []
  isEdit.value = false
}

// 导出数据
const exportTurnsAction = async () => {
  try {
    ElMessage.info('导出功能正在开发中')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await exportTurnTemplate()
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'turn_template.csv'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败：' + error.message)
  }
}

// 导入文件选择处理
const handleImportFileChange = (file) => {
  // 可以在这里添加文件预览或验证逻辑
}

// 提交导入
const submitImport = async () => {
  const uploadRef = document.querySelector('.el-upload')
  const fileList = uploadRef?.files
  
  if (!fileList || fileList.length === 0) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  importing.value = true
  try {
    const file = fileList[0]
    const response = await importTurns(file, importOptions)
    
    ElMessage.success('导入成功')
    showImportTurn.value = false
    loadTurns()
  } catch (error) {
    ElMessage.error('导入失败：' + error.message)
  } finally {
    importing.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  if (!scenarioId.value) {
    ElMessage.error('缺少场景ID参数')
    router.push('/atc-simulation/scenarios')
    return
  }
  loadTurns()
})
</script>

<style scoped>
.turn-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.header-left h1 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.batch-actions {
  margin-bottom: 20px;
}

.table-section {
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.turn-number {
  font-weight: bold;
}

.speaker-tag {
  font-size: 12px;
}

.transcript-content .transcript-text {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
}

.audio-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.audio-icon {
  font-size: 14px;
}

.expected-response {
  display: block;
  color: #606266;
}

.score-difficulty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.max-score {
  font-weight: bold;
}

.difficulty-tag {
  font-size: 11px;
}

.completeness-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.completeness-text {
  font-size: 12px;
  color: #606266;
}

.status-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.status-tag {
  font-size: 11px;
}

.turn-detail .transcript-text,
.turn-detail .expected-response,
.turn-detail .scoring-criteria {
  white-space: pre-wrap;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  display: block;
  margin-top: 8px;
}

.audio-upload-section {
  width: 100%;
}

.current-audio {
  display: flex;
  align-items: center;
  gap: 8px;
}

.turn-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
}

.preview-label {
  color: #606266;
  font-size: 14px;
}

.audio-player {
  display: flex;
  align-items: center;
  gap: 8px;
}

.import-section .mb-4 {
  margin-bottom: 16px;
}

.import-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ml-2 {
  margin-left: 8px;
}

.mt-1 {
  margin-top: 4px;
}

.mt-2 {
  margin-top: 8px;
}

.mt-4 {
  margin-top: 16px;
}
</style>

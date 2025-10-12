<template>
  <div class="scenario-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>场景管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateScenario = true">
          <el-icon><Plus /></el-icon>
          新建场景
        </el-button>
        <el-button @click="showImportScenario = true">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="exportScenariosAction">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索场景标题、描述..."
            @input="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.scenarioType" placeholder="场景类型" clearable @change="handleFilter">
            <el-option
              v-for="type in scenarioTypeOptions"
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
    <div class="batch-actions" v-if="selectedScenarios.length > 0">
      <el-alert
        :title="`已选择 ${selectedScenarios.length} 项`"
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
        :data="scenarioList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        
        <el-table-column label="场景信息" min-width="300" sortable="custom">
          <template #default="scope">
            <div class="scenario-info">
              <el-text class="title" @click="viewScenario(scope.row)" style="cursor: pointer; color: #409eff;">
                {{ scope.row.title }}
              </el-text>
              <div class="scenario-meta mt-1">
                <el-tag 
                  size="small" 
                  :type="getScenarioTypeTagType(scope.row.scenarioType)"
                  class="mr-1"
                >
                  {{ getScenarioTypeDescription(scope.row.scenarioType) }}
                </el-tag>
                <el-tag 
                  size="small" 
                  :type="getDifficultyTagType(scope.row.difficultyLevel)"
                  class="mr-1"
                >
                  {{ getDifficultyDescription(scope.row.difficultyLevel) }}
                </el-tag>
              </div>
              <div v-if="scope.row.description" class="description mt-1">
                <el-text type="info" size="small">
                  {{ scope.row.description.substring(0, 100) }}{{ scope.row.description.length > 100 ? '...' : '' }}
                </el-text>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="机场" width="120">
          <template #default="scope">
            <el-text>{{ getAirportName(scope.row.airportId) }}</el-text>
          </template>
        </el-table-column>

        <el-table-column label="持续时间" width="100" align="center" sortable="custom">
          <template #default="scope">
            <el-text>{{ formatDuration(scope.row.estimatedDuration) }}</el-text>
          </template>
        </el-table-column>

        <el-table-column label="轮次数量" width="100" align="center">
          <template #default="scope">
            <el-badge :value="scope.row.turnCount || 0" class="item">
              <el-button size="small" text @click="manageTurns(scope.row)">
                <el-icon><Collection /></el-icon>
              </el-button>
            </el-badge>
          </template>
        </el-table-column>

        <el-table-column label="复杂度" width="120" align="center">
          <template #default="scope">
            <div class="complexity-indicator">
              <el-progress
                :percentage="getComplexityPercentage(scope.row)"
                :color="getScenarioComplexity(scope.row).color"
                :show-text="false"
                stroke-width="8"
              />
              <el-text size="small" class="complexity-text">
                {{ getScenarioComplexity(scope.row).label }}
              </el-text>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isActive"
              @change="toggleScenarioStatus(scope.row)"
              :disabled="!hasPermission('ADMIN')"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewScenario(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="editScenario(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              @click="copyScenarioAction(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteScenarioAction(scope.row)"
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

    <!-- 创建/编辑场景对话框 -->
    <el-dialog
      v-model="showCreateScenario"
      :title="isEdit ? '编辑场景' : '新建场景'"
      width="800px"
      @close="resetScenarioForm"
    >
      <el-form
        ref="scenarioFormRef"
        :model="scenarioForm"
        :rules="scenarioRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="机场" prop="airportId">
              <el-select 
                v-model="scenarioForm.airportId" 
                placeholder="请选择机场" 
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="airport in airportOptions"
                  :key="airport.id"
                  :label="formatAirportDisplayName(airport)"
                  :value="airport.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模块" prop="moduleId">
              <el-select 
                v-model="scenarioForm.moduleId" 
                placeholder="请选择模块" 
                style="width: 100%"
              >
                <el-option
                  v-for="module in moduleOptions"
                  :key="module.id"
                  :label="module.name"
                  :value="module.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="场景标题" prop="title">
          <el-input
            v-model="scenarioForm.title"
            placeholder="请输入场景标题"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="场景描述">
          <el-input
            v-model="scenarioForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入场景描述"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="场景类型" prop="scenarioType">
              <el-select 
                v-model="scenarioForm.scenarioType" 
                placeholder="请选择类型" 
                style="width: 100%"
              >
                <el-option
                  v-for="type in scenarioTypeOptions"
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
                v-model="scenarioForm.difficultyLevel" 
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
          <el-col :span="8">
            <el-form-item label="预计时长(分)" prop="estimatedDuration">
              <el-input-number
                v-model="scenarioForm.estimatedDuration"
                :min="1"
                :max="180"
                placeholder="持续时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="duration-presets mb-4">
          <el-text class="preset-label">快速设置时长：</el-text>
          <el-button-group>
            <el-button 
              v-for="preset in durationPresets" 
              :key="preset.value"
              size="small"
              @click="scenarioForm.estimatedDuration = preset.value"
            >
              {{ preset.label }}
            </el-button>
          </el-button-group>
        </div>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="scenarioForm.isActive"
                active-text="激活"
                inactive-text="停用"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <div class="scenario-preview">
              <el-text class="preview-label">复杂度预览：</el-text>
              <el-tag :type="getScenarioComplexity(scenarioForm).level" size="small">
                {{ getScenarioComplexity(scenarioForm).label }}
              </el-tag>
            </div>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateScenario = false">取消</el-button>
          <el-button type="primary" @click="submitScenario" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看场景详情对话框 -->
    <el-dialog
      v-model="showViewScenario"
      title="场景详情"
      width="900px"
    >
      <div v-if="currentScenario" class="scenario-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="场景标题" span="2">
            {{ currentScenario.title }}
          </el-descriptions-item>
          <el-descriptions-item label="机场">
            {{ getAirportName(currentScenario.airportId) }}
          </el-descriptions-item>
          <el-descriptions-item label="模块">
            {{ getModuleName(currentScenario.moduleId) }}
          </el-descriptions-item>
          <el-descriptions-item label="场景类型">
            <el-tag :type="getScenarioTypeTagType(currentScenario.scenarioType)">
              {{ getScenarioTypeDescription(currentScenario.scenarioType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="难度等级">
            <el-tag :type="getDifficultyTagType(currentScenario.difficultyLevel)">
              {{ getDifficultyDescription(currentScenario.difficultyLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预计时长">
            {{ formatDuration(currentScenario.estimatedDuration) }}
          </el-descriptions-item>
          <el-descriptions-item label="轮次数量">
            {{ currentScenario.turnCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentScenario.isActive ? 'success' : 'danger'">
              {{ currentScenario.isActive ? '激活' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="复杂度">
            <el-tag :type="getScenarioComplexity(currentScenario).level">
              {{ getScenarioComplexity(currentScenario).label }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentScenario.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(currentScenario.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentScenario.description" class="mt-4">
          <h4>场景描述</h4>
          <el-text class="scenario-description">{{ currentScenario.description }}</el-text>
        </div>

        <div class="mt-4">
          <h4>复杂度分析</h4>
          <div class="complexity-analysis">
            <el-progress
              :percentage="getComplexityPercentage(currentScenario)"
              :color="getScenarioComplexity(currentScenario).color"
              stroke-width="12"
            >
              <template #default="{ percentage }">
                <span class="percentage-text">{{ percentage }}%</span>
              </template>
            </el-progress>
            <div class="complexity-factors mt-2">
              <el-tag size="small" class="mr-1">
                难度: {{ getDifficultyDescription(currentScenario.difficultyLevel) }}
              </el-tag>
              <el-tag size="small" class="mr-1">
                时长: {{ formatDuration(currentScenario.estimatedDuration) }}
              </el-tag>
              <el-tag size="small" class="mr-1">
                类型: {{ getScenarioTypeDescription(currentScenario.scenarioType) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showViewScenario = false">关闭</el-button>
          <el-button type="primary" @click="manageTurns(currentScenario)">
            管理轮次
          </el-button>
          <el-button type="primary" @click="editScenario(currentScenario)" v-if="hasPermission('ADMIN')">
            编辑场景
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

    <!-- 导入场景对话框 -->
    <el-dialog
      v-model="showImportScenario"
      title="批量导入场景"
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
            <p>必填字段：场景标题、机场ID、模块ID</p>
          </template>
        </el-alert>

        <div class="mb-4">
          <el-button @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
        </div>

        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.csv"
          :on-change="handleFileChange"
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
          <el-button @click="showImportScenario = false">取消</el-button>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Plus,
  Search,
  Refresh,
  Upload,
  Download,
  View,
  Edit,
  Delete,
  DocumentCopy,
  Collection,
  UploadFilled
} from '@element-plus/icons-vue'

// API导入
import {
  getScenarios,
  getScenarioById,
  createScenario,
  updateScenario,
  deleteScenario,
  copyScenario,
  searchScenarios,
  toggleScenarioActive,
  batchDeleteScenarios,
  batchActivateScenarios,
  batchDeactivateScenarios,
  batchOperateScenarios,
  exportScenarioTemplate,
  importScenarios,
  validateScenarioData,
  generateScenarioTemplate,
  formatDuration,
  getDifficultyDescription,
  getScenarioTypeDescription,
  getScenarioTypeOptions,
  getDifficultyLevelOptions,
  getDurationPresets,
  getScenarioComplexity
} from '@/api/atc-scenario'

// 机场API导入
import { getAirports, formatAirportDisplayName } from '@/api/airport'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const scenarioList = ref([])
const selectedScenarios = ref([])
const currentScenario = ref(null)

// 搜索和筛选
const searchKeyword = ref('')
const filterParams = reactive({
  scenarioType: null,
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
  prop: 'title',
  order: 'ascending'
})

// 对话框状态
const showCreateScenario = ref(false)
const showViewScenario = ref(false)
const showImportScenario = ref(false)
const showBatchDifficulty = ref(false)
const isEdit = ref(false)

// 表单
const scenarioFormRef = ref(null)
const scenarioForm = reactive(generateScenarioTemplate())
const scenarioRules = {
  title: [
    { required: true, message: '请输入场景标题', trigger: 'blur' },
    { max: 255, message: '标题长度不能超过255个字符', trigger: 'blur' }
  ],
  airportId: [
    { required: true, message: '请选择机场', trigger: 'change' }
  ],
  moduleId: [
    { required: true, message: '请选择模块', trigger: 'change' }
  ],
  scenarioType: [
    { required: true, message: '请选择场景类型', trigger: 'change' }
  ],
  difficultyLevel: [
    { required: true, message: '请选择难度等级', trigger: 'change' }
  ],
  estimatedDuration: [
    { required: true, message: '请设置预计时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 180, message: '时长必须在1-180分钟之间', trigger: 'blur' }
  ]
}

// 批量操作
const batchDifficultyLevel = ref(null)

// 导入选项
const importOptions = reactive({
  skipDuplicates: true,
  updateExisting: false,
  validateData: true
})

// 选项数据
const scenarioTypeOptions = ref(getScenarioTypeOptions())
const difficultyLevelOptions = ref(getDifficultyLevelOptions())
const durationPresets = ref(getDurationPresets())
const airportOptions = ref([])
const moduleOptions = ref([
  { id: 1, name: '模拟通话模块1' },
  { id: 2, name: '模拟通话模块2' },
  { id: 3, name: '模拟通话模块3' }
])

// 权限检查
const hasPermission = (role) => {
  return authStore.hasRole(role)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取机场名称
const getAirportName = (airportId) => {
  const airport = airportOptions.value.find(a => a.id === airportId)
  return airport ? formatAirportDisplayName(airport) : `机场${airportId}`
}

// 获取模块名称
const getModuleName = (moduleId) => {
  const module = moduleOptions.value.find(m => m.id === moduleId)
  return module ? module.name : `模块${moduleId}`
}

// 获取场景类型标签类型
const getScenarioTypeTagType = (scenarioType) => {
  const typeMap = {
    'APPROACH': 'primary',
    'DEPARTURE': 'success',
    'GROUND': 'info',
    'TOWER': 'warning',
    'RADAR': 'danger',
    'EMERGENCY': 'danger',
    'COMPLEX': 'warning',
    'TRAINING': 'info'
  }
  return typeMap[scenarioType] || 'default'
}

// 获取难度标签类型
const getDifficultyTagType = (difficultyLevel) => {
  switch (difficultyLevel) {
    case 1:
    case 2:
      return 'success'
    case 3:
      return 'warning'
    case 4:
    case 5:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取复杂度百分比
const getComplexityPercentage = (scenario) => {
  const complexity = getScenarioComplexity(scenario)
  if (complexity.level === 'high') return 100
  if (complexity.level === 'medium') return 70
  return 40
}

// 加载场景列表
const loadScenarios = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: [sortParams.prop, sortParams.order === 'ascending' ? 'asc' : 'desc']
    }

    // 添加筛选参数
    if (filterParams.scenarioType) params.scenarioType = filterParams.scenarioType
    if (filterParams.difficultyLevel) params.difficultyLevel = filterParams.difficultyLevel
    if (filterParams.isActive !== null) params.isActive = filterParams.isActive

    let response
    if (searchKeyword.value) {
      response = await searchScenarios(searchKeyword.value, params)
    } else {
      response = await getScenarios(params)
    }

    scenarioList.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载场景列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载机场选项
const loadAirportOptions = async () => {
  try {
    const response = await getAirports({ size: 1000 })
    airportOptions.value = response.data.content || []
  } catch (error) {
    console.error('加载机场选项失败：', error)
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadScenarios()
}

// 筛选处理
const handleFilter = () => {
  pagination.page = 1
  loadScenarios()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  Object.assign(filterParams, {
    scenarioType: null,
    difficultyLevel: null,
    isActive: null
  })
  pagination.page = 1
  loadScenarios()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadScenarios()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadScenarios()
}

// 排序处理
const handleSortChange = ({ prop, order }) => {
  sortParams.prop = prop || 'title'
  sortParams.order = order || 'ascending'
  loadScenarios()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedScenarios.value = selection
}

// 查看场景
const viewScenario = async (scenario) => {
  try {
    const response = await getScenarioById(scenario.id)
    currentScenario.value = response.data
    showViewScenario.value = true
  } catch (error) {
    ElMessage.error('获取场景详情失败：' + error.message)
  }
}

// 编辑场景
const editScenario = (scenario) => {
  isEdit.value = true
  Object.assign(scenarioForm, scenario)
  showCreateScenario.value = true
}

// 复制场景
const copyScenarioAction = async (scenario) => {
  try {
    await copyScenario(scenario.id)
    ElMessage.success('场景复制成功')
    loadScenarios()
  } catch (error) {
    ElMessage.error('复制场景失败：' + error.message)
  }
}

// 删除场景
const deleteScenarioAction = (scenario) => {
  ElMessageBox.confirm(
    `确定要删除场景"${scenario.title}"吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteScenario(scenario.id)
      ElMessage.success('删除成功')
      loadScenarios()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

// 切换场景状态
const toggleScenarioStatus = async (scenario) => {
  try {
    await toggleScenarioActive(scenario.id)
    ElMessage.success(`场景已${scenario.isActive ? '激活' : '停用'}`)
  } catch (error) {
    scenario.isActive = !scenario.isActive // 恢复状态
    ElMessage.error('状态切换失败：' + error.message)
  }
}

// 管理轮次
const manageTurns = (scenario) => {
  router.push({
    name: 'atc-rounds',
    params: { scenarioId: scenario.id },
    query: { scenarioTitle: scenario.title }
  })
}

// 批量操作
const batchActivate = async () => {
  try {
    const ids = selectedScenarios.value.map(s => s.id)
    await batchActivateScenarios(ids)
    ElMessage.success('批量激活成功')
    loadScenarios()
  } catch (error) {
    ElMessage.error('批量激活失败：' + error.message)
  }
}

const batchDeactivate = async () => {
  try {
    const ids = selectedScenarios.value.map(s => s.id)
    await batchDeactivateScenarios(ids)
    ElMessage.success('批量停用成功')
    loadScenarios()
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
    const ids = selectedScenarios.value.map(s => s.id)
    await batchOperateScenarios('updateDifficulty', ids, { difficultyLevel: batchDifficultyLevel.value })
    ElMessage.success('批量设置难度成功')
    showBatchDifficulty.value = false
    loadScenarios()
  } catch (error) {
    ElMessage.error('批量设置难度失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

const batchDelete = () => {
  const count = selectedScenarios.value.length
  ElMessageBox.confirm(
    `确定要删除选中的 ${count} 个场景吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedScenarios.value.map(s => s.id)
      await batchDeleteScenarios(ids)
      ElMessage.success('批量删除成功')
      selectedScenarios.value = []
      loadScenarios()
    } catch (error) {
      ElMessage.error('批量删除失败：' + error.message)
    }
  })
}

// 提交场景表单
const submitScenario = async () => {
  if (!scenarioFormRef.value) return

  try {
    await scenarioFormRef.value.validate()
    
    // 验证数据
    const errors = validateScenarioData(scenarioForm)
    if (errors.length > 0) {
      ElMessage.error('表单验证失败：' + errors[0])
      return
    }

    submitting.value = true

    if (isEdit.value) {
      await updateScenario(scenarioForm.id, scenarioForm)
      ElMessage.success('场景更新成功')
    } else {
      await createScenario(scenarioForm)
      ElMessage.success('场景创建成功')
    }

    showCreateScenario.value = false
    loadScenarios()
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetScenarioForm = () => {
  if (scenarioFormRef.value) {
    scenarioFormRef.value.resetFields()
  }
  Object.assign(scenarioForm, generateScenarioTemplate())
  isEdit.value = false
}

// 导出数据
const exportScenariosAction = async () => {
  try {
    ElMessage.info('导出功能正在开发中')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await exportScenarioTemplate()
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'scenario_template.csv'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败：' + error.message)
  }
}

// 文件选择处理
const handleFileChange = (file) => {
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
    const response = await importScenarios(file, importOptions)
    
    ElMessage.success('导入成功')
    showImportScenario.value = false
    loadScenarios()
  } catch (error) {
    ElMessage.error('导入失败：' + error.message)
  } finally {
    importing.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadScenarios()
  loadAirportOptions()
})
</script>

<style scoped>
.scenario-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
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

.scenario-info .title {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
}

.scenario-meta {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.description {
  font-size: 12px;
  color: #909399;
}

.complexity-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.complexity-text {
  font-size: 12px;
  color: #606266;
}

.scenario-detail .scenario-description {
  white-space: pre-wrap;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  display: block;
  margin-top: 8px;
}

.complexity-analysis {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
}

.complexity-factors {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.percentage-text {
  font-size: 14px;
  font-weight: bold;
}

.duration-presets {
  margin-bottom: 16px;
}

.preset-label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.scenario-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
}

.preview-label {
  color: #606266;
  font-size: 14px;
}

.import-section .mb-4 {
  margin-bottom: 16px;
}

.import-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
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

.mr-1 {
  margin-right: 4px;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>

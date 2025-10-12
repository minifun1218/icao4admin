<template>
  <div class="airport-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>机场管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateAirport = true">
          <el-icon><Plus /></el-icon>
          新建机场
        </el-button>
        <el-button @click="showImportAirport = true">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="exportAirports">
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
            placeholder="搜索机场名称、ICAO、IATA代码..."
            @input="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.country" placeholder="选择国家" clearable @change="handleFilter">
            <el-option
              v-for="country in countryOptions"
              :key="country.value"
              :label="country.label"
              :value="country.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-input
            v-model="filterParams.city"
            placeholder="城市"
            clearable
            @input="handleFilter"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.hasIata" placeholder="IATA代码" clearable @change="handleFilter">
            <el-option label="有IATA代码" :value="true" />
            <el-option label="无IATA代码" :value="false" />
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
    <div class="batch-actions" v-if="selectedAirports.length > 0">
      <el-alert
        :title="`已选择 ${selectedAirports.length} 项`"
        type="info"
        show-icon
        :closable="false"
      >
        <template #default>
          <el-button size="small" @click="batchUpdateCountry">批量设置国家</el-button>
          <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
        </template>
      </el-alert>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="airportList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        
        <el-table-column prop="icao" label="ICAO代码" width="100" sortable="custom">
          <template #default="scope">
            <el-tag type="primary" class="code-tag">{{ scope.row.icao }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="iata" label="IATA代码" width="100" sortable="custom">
          <template #default="scope">
            <el-tag v-if="scope.row.iata" type="success" class="code-tag">
              {{ scope.row.iata }}
            </el-tag>
            <el-text v-else type="info">-</el-text>
          </template>
        </el-table-column>
        
        <el-table-column prop="name" label="机场名称" min-width="200" sortable="custom">
          <template #default="scope">
            <div class="airport-name">
              <el-text class="name" @click="viewAirport(scope.row)" style="cursor: pointer; color: #409eff;">
                {{ scope.row.name }}
              </el-text>
              <div class="airport-info mt-1">
                <el-text type="info" size="small">
                  {{ getAirportFullInfo(scope.row) }}
                </el-text>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="city" label="城市" width="120" sortable="custom">
          <template #default="scope">
            <el-text>{{ scope.row.city || '-' }}</el-text>
          </template>
        </el-table-column>

        <el-table-column prop="country" label="国家" width="120" sortable="custom">
          <template #default="scope">
            <el-text>{{ scope.row.country || '-' }}</el-text>
          </template>
        </el-table-column>

        <el-table-column label="代码完整性" width="120" align="center">
          <template #default="scope">
            <div class="code-completeness">
              <el-progress
                :percentage="getCodeCompleteness(scope.row)"
                :color="getCompletenessColor(getCodeCompleteness(scope.row))"
                :show-text="false"
                stroke-width="8"
              />
              <el-text size="small" class="completeness-text">
                {{ getCodeCompleteness(scope.row) }}%
              </el-text>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewAirport(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="editAirport(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              @click="copyAirportAction(scope.row)"
              v-if="hasPermission('ADMIN')"
            >
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteAirportAction(scope.row)"
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

    <!-- 创建/编辑机场对话框 -->
    <el-dialog
      v-model="showCreateAirport"
      :title="isEdit ? '编辑机场' : '新建机场'"
      width="600px"
      @close="resetAirportForm"
    >
      <el-form
        ref="airportFormRef"
        :model="airportForm"
        :rules="airportRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ICAO代码" prop="icao">
              <el-input 
                v-model="airportForm.icao" 
                placeholder="4位字母代码"
                maxlength="4"
                @input="handleIcaoInput"
                @blur="checkIcaoAvailability"
              />
              <div v-if="icaoCheckResult" class="validation-result">
                <el-text :type="icaoCheckResult.available ? 'success' : 'danger'" size="small">
                  {{ icaoCheckResult.message }}
                </el-text>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="IATA代码" prop="iata">
              <el-input 
                v-model="airportForm.iata" 
                placeholder="3位字母代码（可选）"
                maxlength="3"
                @input="handleIataInput"
                @blur="checkIataAvailability"
              />
              <div v-if="iataCheckResult" class="validation-result">
                <el-text :type="iataCheckResult.available ? 'success' : 'danger'" size="small">
                  {{ iataCheckResult.message }}
                </el-text>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="机场名称" prop="name">
          <el-input
            v-model="airportForm.name"
            placeholder="请输入机场名称"
            maxlength="200"
            @input="handleNameInput"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="城市" prop="city">
              <el-input
                v-model="airportForm.city"
                placeholder="请输入城市名称"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="国家" prop="country">
              <el-select 
                v-model="airportForm.country" 
                placeholder="请选择国家" 
                style="width: 100%"
                filterable
                allow-create
              >
                <el-option
                  v-for="country in countryOptions"
                  :key="country.value"
                  :label="country.label"
                  :value="country.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="code-suggestion" v-if="!isEdit && airportForm.name">
          <el-alert
            title="代码建议"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="suggestion-content">
                <p>基于机场名称生成的代码建议：</p>
                <div class="suggested-codes">
                  <el-button size="small" @click="applySuggestedCodes">
                    ICAO: {{ suggestedCodes.icao }} | IATA: {{ suggestedCodes.iata }}
                  </el-button>
                </div>
              </div>
            </template>
          </el-alert>
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateAirport = false">取消</el-button>
          <el-button type="primary" @click="submitAirport" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看机场详情对话框 -->
    <el-dialog
      v-model="showViewAirport"
      title="机场详情"
      width="700px"
    >
      <div v-if="currentAirport" class="airport-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ICAO代码">
            <el-tag type="primary">{{ currentAirport.icao }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IATA代码">
            <el-tag v-if="currentAirport.iata" type="success">{{ currentAirport.iata }}</el-tag>
            <el-text v-else type="info">无</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="机场名称" span="2">
            {{ currentAirport.name }}
          </el-descriptions-item>
          <el-descriptions-item label="城市">
            {{ currentAirport.city || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="国家">
            {{ currentAirport.country || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" span="2">
            {{ formatDateTime(currentAirport.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="mt-4">
          <h4>代码完整性分析</h4>
          <div class="completeness-analysis">
            <el-progress
              :percentage="getCodeCompleteness(currentAirport)"
              :color="getCompletenessColor(getCodeCompleteness(currentAirport))"
              stroke-width="12"
            >
              <template #default="{ percentage }">
                <span class="percentage-text">{{ percentage }}%</span>
              </template>
            </el-progress>
            <div class="completeness-details mt-2">
              <el-tag type="primary" size="small">
                ICAO: {{ currentAirport.icao ? '已设置' : '未设置' }}
              </el-tag>
              <el-tag type="success" size="small" class="ml-1">
                IATA: {{ currentAirport.iata ? '已设置' : '未设置' }}
              </el-tag>
              <el-tag type="info" size="small" class="ml-1">
                位置: {{ (currentAirport.city && currentAirport.country) ? '完整' : '部分' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="mt-4">
          <h4>完整标识</h4>
          <el-text class="full-identifier">{{ getAirportFullInfo(currentAirport) }}</el-text>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showViewAirport = false">关闭</el-button>
          <el-button type="primary" @click="editAirport(currentAirport)" v-if="hasPermission('ADMIN')">
            编辑机场
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量设置国家对话框 -->
    <el-dialog
      v-model="showBatchCountry"
      title="批量设置国家"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="国家">
          <el-select 
            v-model="batchCountry" 
            placeholder="请选择国家" 
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="country in countryOptions"
              :key="country.value"
              :label="country.label"
              :value="country.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchCountry = false">取消</el-button>
          <el-button type="primary" @click="submitBatchCountry" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入机场对话框 -->
    <el-dialog
      v-model="showImportAirport"
      title="批量导入机场"
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
            <p>必填字段：ICAO代码、机场名称</p>
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
          <el-button @click="showImportAirport = false">取消</el-button>
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
  UploadFilled
} from '@element-plus/icons-vue'

// API导入
import {
  getAirports,
  getAirportById,
  createAirport,
  updateAirport,
  deleteAirport,
  copyAirport,
  searchAirports,
  batchDeleteAirports,
  batchOperateAirports,
  checkIcaoAvailability,
  checkIataAvailability,
  exportAirportTemplate,
  importAirports,
  validateAirportData,
  generateAirportTemplate,
  formatAirportDisplayName,
  getAirportFullInfo,
  getCountryOptions,
  suggestAirportCodes,
  validateIcaoCode,
  validateIataCode
} from '@/api/airport'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const airportList = ref([])
const selectedAirports = ref([])
const currentAirport = ref(null)

// 搜索和筛选
const searchKeyword = ref('')
const filterParams = reactive({
  country: null,
  city: '',
  hasIata: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 排序
const sortParams = reactive({
  prop: 'icao',
  order: 'ascending'
})

// 对话框状态
const showCreateAirport = ref(false)
const showViewAirport = ref(false)
const showImportAirport = ref(false)
const showBatchCountry = ref(false)
const isEdit = ref(false)

// 表单
const airportFormRef = ref(null)
const airportForm = reactive(generateAirportTemplate())
const airportRules = {
  icao: [
    { required: true, message: '请输入ICAO代码', trigger: 'blur' },
    { len: 4, message: 'ICAO代码必须为4位字符', trigger: 'blur' },
    { pattern: /^[A-Z]{4}$/, message: 'ICAO代码必须为4个大写字母', trigger: 'blur' }
  ],
  iata: [
    { pattern: /^[A-Z]{3}$|^$/, message: 'IATA代码必须为3个大写字母或留空', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入机场名称', trigger: 'blur' },
    { max: 200, message: '机场名称长度不能超过200个字符', trigger: 'blur' }
  ],
  city: [
    { max: 100, message: '城市名称长度不能超过100个字符', trigger: 'blur' }
  ],
  country: [
    { max: 100, message: '国家名称长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 代码检查结果
const icaoCheckResult = ref(null)
const iataCheckResult = ref(null)

// 代码建议
const suggestedCodes = ref({ icao: '', iata: '' })

// 批量操作
const batchCountry = ref('')

// 导入选项
const importOptions = reactive({
  skipDuplicates: true,
  updateExisting: false,
  validateData: true
})

// 选项数据
const countryOptions = ref(getCountryOptions())

// 权限检查
const hasPermission = (role) => {
  return authStore.hasRole(role)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取代码完整性百分比
const getCodeCompleteness = (airport) => {
  let score = 0
  if (airport.icao) score += 40 // ICAO代码权重40%
  if (airport.iata) score += 30 // IATA代码权重30%
  if (airport.city) score += 15 // 城市权重15%
  if (airport.country) score += 15 // 国家权重15%
  return score
}

// 获取完整性颜色
const getCompletenessColor = (percentage) => {
  if (percentage >= 85) return '#67c23a'
  if (percentage >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 加载机场列表
const loadAirports = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: [sortParams.prop, sortParams.order === 'ascending' ? 'asc' : 'desc']
    }

    // 添加筛选参数
    if (filterParams.country) params.country = filterParams.country
    if (filterParams.city) params.city = filterParams.city
    if (filterParams.hasIata !== null) params.hasIata = filterParams.hasIata

    let response
    if (searchKeyword.value) {
      response = await searchAirports(searchKeyword.value, params)
    } else {
      response = await getAirports(params)
    }

    airportList.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载机场列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadAirports()
}

// 筛选处理
const handleFilter = () => {
  pagination.page = 1
  loadAirports()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  Object.assign(filterParams, {
    country: null,
    city: '',
    hasIata: null
  })
  pagination.page = 1
  loadAirports()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadAirports()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadAirports()
}

// 排序处理
const handleSortChange = ({ prop, order }) => {
  sortParams.prop = prop || 'icao'
  sortParams.order = order || 'ascending'
  loadAirports()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedAirports.value = selection
}

// ICAO输入处理
const handleIcaoInput = (value) => {
  airportForm.icao = value.toUpperCase()
  icaoCheckResult.value = null
}

// IATA输入处理
const handleIataInput = (value) => {
  airportForm.iata = value.toUpperCase()
  iataCheckResult.value = null
}

// 机场名称输入处理
const handleNameInput = (value) => {
  if (!isEdit.value && value) {
    suggestedCodes.value = suggestAirportCodes(value, airportForm.city)
  }
}

// 检查ICAO代码可用性
const checkIcaoAvailability = async () => {
  if (!airportForm.icao || !validateIcaoCode(airportForm.icao)) {
    icaoCheckResult.value = null
    return
  }

  try {
    const response = await checkIcaoAvailability(airportForm.icao)
    icaoCheckResult.value = {
      available: response.data.available,
      message: response.data.available ? 'ICAO代码可用' : 'ICAO代码已存在'
    }
  } catch (error) {
    icaoCheckResult.value = {
      available: false,
      message: '检查失败'
    }
  }
}

// 检查IATA代码可用性
const checkIataAvailability = async () => {
  if (!airportForm.iata || !validateIataCode(airportForm.iata)) {
    iataCheckResult.value = null
    return
  }

  try {
    const response = await checkIataAvailability(airportForm.iata)
    iataCheckResult.value = {
      available: response.data.available,
      message: response.data.available ? 'IATA代码可用' : 'IATA代码已存在'
    }
  } catch (error) {
    iataCheckResult.value = {
      available: false,
      message: '检查失败'
    }
  }
}

// 应用建议的代码
const applySuggestedCodes = () => {
  airportForm.icao = suggestedCodes.value.icao
  airportForm.iata = suggestedCodes.value.iata
  checkIcaoAvailability()
  checkIataAvailability()
}

// 查看机场
const viewAirport = async (airport) => {
  try {
    const response = await getAirportById(airport.id)
    currentAirport.value = response.data
    showViewAirport.value = true
  } catch (error) {
    ElMessage.error('获取机场详情失败：' + error.message)
  }
}

// 编辑机场
const editAirport = (airport) => {
  isEdit.value = true
  Object.assign(airportForm, airport)
  showCreateAirport.value = true
}

// 复制机场
const copyAirportAction = async (airport) => {
  try {
    await copyAirport(airport.id)
    ElMessage.success('机场复制成功')
    loadAirports()
  } catch (error) {
    ElMessage.error('复制机场失败：' + error.message)
  }
}

// 删除机场
const deleteAirportAction = (airport) => {
  ElMessageBox.confirm(
    `确定要删除机场"${airport.name} (${airport.icao})"吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteAirport(airport.id)
      ElMessage.success('删除成功')
      loadAirports()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

// 批量设置国家
const batchUpdateCountry = () => {
  batchCountry.value = ''
  showBatchCountry.value = true
}

const submitBatchCountry = async () => {
  if (!batchCountry.value) {
    ElMessage.warning('请选择国家')
    return
  }

  try {
    submitting.value = true
    const ids = selectedAirports.value.map(a => a.id)
    await batchOperateAirports('updateCountry', ids, { country: batchCountry.value })
    ElMessage.success('批量设置国家成功')
    showBatchCountry.value = false
    loadAirports()
  } catch (error) {
    ElMessage.error('批量设置国家失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 批量删除
const batchDelete = () => {
  const count = selectedAirports.value.length
  ElMessageBox.confirm(
    `确定要删除选中的 ${count} 个机场吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedAirports.value.map(a => a.id)
      await batchDeleteAirports(ids)
      ElMessage.success('批量删除成功')
      selectedAirports.value = []
      loadAirports()
    } catch (error) {
      ElMessage.error('批量删除失败：' + error.message)
    }
  })
}

// 提交机场表单
const submitAirport = async () => {
  if (!airportFormRef.value) return

  try {
    await airportFormRef.value.validate()
    
    // 验证数据
    const errors = validateAirportData(airportForm)
    if (errors.length > 0) {
      ElMessage.error('表单验证失败：' + errors[0])
      return
    }

    submitting.value = true

    if (isEdit.value) {
      await updateAirport(airportForm.id, airportForm)
      ElMessage.success('机场更新成功')
    } else {
      await createAirport(airportForm)
      ElMessage.success('机场创建成功')
    }

    showCreateAirport.value = false
    loadAirports()
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetAirportForm = () => {
  if (airportFormRef.value) {
    airportFormRef.value.resetFields()
  }
  Object.assign(airportForm, generateAirportTemplate())
  isEdit.value = false
  icaoCheckResult.value = null
  iataCheckResult.value = null
  suggestedCodes.value = { icao: '', iata: '' }
}

// 导出数据
const exportAirports = async () => {
  try {
    ElMessage.info('导出功能正在开发中')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await exportAirportTemplate()
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'airport_template.csv'
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
    const response = await importAirports(file, importOptions)
    
    ElMessage.success('导入成功')
    showImportAirport.value = false
    loadAirports()
  } catch (error) {
    ElMessage.error('导入失败：' + error.message)
  } finally {
    importing.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadAirports()
})
</script>

<style scoped>
.airport-management {
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

.code-tag {
  font-family: 'Courier New', monospace;
  font-weight: bold;
}

.airport-name .name {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
}

.airport-info {
  font-size: 12px;
  color: #909399;
}

.code-completeness {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.completeness-text {
  font-size: 12px;
  color: #606266;
}

.validation-result {
  margin-top: 4px;
}

.code-suggestion {
  margin-top: 16px;
}

.suggestion-content p {
  margin: 0 0 8px 0;
}

.suggested-codes {
  margin-top: 8px;
}

.airport-detail .completeness-analysis {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
}

.completeness-details {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.percentage-text {
  font-size: 14px;
  font-weight: bold;
}

.full-identifier {
  font-family: 'Courier New', monospace;
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  display: block;
  margin-top: 8px;
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

.ml-1 {
  margin-left: 4px;
}
</style>

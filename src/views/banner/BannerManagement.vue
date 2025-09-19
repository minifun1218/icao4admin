<template>
  <div class="banner-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon" size="24" color="#409EFF"><Picture /></el-icon>
            <span class="header-title">轮播图管理</span>
            <el-tag type="info" size="small">{{ totalBanners }}个轮播图</el-tag>
          </div>
          <div class="header-actions">
            <el-button type="success" @click="showImportDialog">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button type="primary" @click="exportBanners">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button type="danger" @click="batchDelete" :disabled="!hasSelection">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :span="6" v-for="stat in bannerStats" :key="stat.type">
            <el-card class="stat-card" :class="`stat-${stat.type}`" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon :size="32" :color="stat.color">
                    <component :is="stat.icon" />
                  </el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ stat.count }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="banner-tabs">
        <!-- 轮播图组 -->
        <el-tab-pane name="groups">
          <template #label>
            <span class="tab-label">
              <el-icon><FolderOpened /></el-icon>
              轮播图组
            </span>
          </template>
          <BannerGroupPanel 
            :groups="bannerGroups"
            :loading="loading"
            @add="addGroup"
            @edit="editGroup"
            @delete="deleteGroup"
            @preview="previewGroup"
            @publish="publishGroup"
            @selection-change="handleSelectionChange"
          />
        </el-tab-pane>

        <!-- 轮播图项目 -->
        <el-tab-pane name="items">
          <template #label>
            <span class="tab-label">
              <el-icon><Picture /></el-icon>
              轮播图项目
            </span>
          </template>
          <BannerItemPanel 
            :items="bannerItems"
            :groups="bannerGroups"
            :loading="loading"
            @add="addItem"
            @edit="editItem"
            @delete="deleteItem"
            @upload="uploadImage"
            @selection-change="handleSelectionChange"
          />
        </el-tab-pane>

        <!-- 统计分析 -->
        <el-tab-pane name="statistics">
          <template #label>
            <span class="tab-label">
              <el-icon><DataAnalysis /></el-icon>
              统计分析
            </span>
          </template>
          <BannerStatistics 
            :click-stats="clickStats"
            :view-stats="viewStats"
            :loading="loading"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 添加/编辑轮播图组对话框 -->
    <el-dialog
      v-model="groupDialogVisible"
      :title="groupForm.id ? '编辑轮播图组' : '创建轮播图组'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupRules"
        label-width="120px"
        class="group-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组名称" prop="name">
              <el-input v-model="groupForm.name" placeholder="请输入轮播图组名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="groupForm.type" placeholder="选择类型">
                <el-option
                  v-for="type in bannerTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                >
                  <span class="option-item">
                    <el-icon :color="type.color"><component :is="type.icon" /></el-icon>
                    {{ type.label }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="groupForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入轮播图组描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="显示位置" prop="displayPosition">
              <el-select v-model="groupForm.displayPosition" placeholder="选择显示位置">
                <el-option
                  v-for="position in displayPositions"
                  :key="position.value"
                  :label="position.label"
                  :value="position.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="displayOrder">
              <el-input-number
                v-model="groupForm.displayOrder"
                :min="1"
                :max="999"
                :step="1"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="isActive">
              <el-switch
                v-model="groupForm.isActive"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="自动播放">
              <el-switch
                v-model="groupForm.autoPlay"
                active-text="开启"
                inactive-text="关闭"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="播放间隔" v-if="groupForm.autoPlay">
              <el-input-number
                v-model="groupForm.playInterval"
                :min="1000"
                :max="10000"
                :step="1000"
                placeholder="毫秒"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="groupForm.remark"
            type="textarea"
            :rows="2"
            placeholder="轮播图组备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="groupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGroup" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑轮播图项目对话框 -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="itemForm.id ? '编辑轮播图项目' : '添加轮播图项目'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="itemFormRef"
        :model="itemForm"
        :rules="itemRules"
        label-width="120px"
        class="item-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="itemForm.title" placeholder="请输入标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属组" prop="groupId">
              <el-select v-model="itemForm.groupId" placeholder="选择轮播图组">
                <el-option
                  v-for="group in bannerGroups"
                  :key="group.id"
                  :label="group.name"
                  :value="group.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="图片上传" prop="imageUrl">
          <div class="image-upload">
            <el-upload
              ref="imageUploadRef"
              :file-list="imageFileList"
              :before-upload="beforeImageUpload"
              :on-success="handleImageSuccess"
              :on-remove="handleImageRemove"
              accept="image/*"
              :limit="1"
              action="/api/banners/upload/image"
              list-type="picture-card"
            >
              <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">
              <p>支持 JPG、PNG、GIF、WebP 格式，文件大小不超过 5MB</p>
              <p>建议尺寸：1920x1080 或 16:9 比例</p>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="itemForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入图片描述"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="链接类型" prop="linkType">
              <el-select v-model="itemForm.linkType" placeholder="选择链接类型">
                <el-option
                  v-for="type in linkTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item 
              v-if="itemForm.linkType !== 'none'"
              label="链接地址" 
              prop="linkUrl"
            >
              <el-input 
                v-model="itemForm.linkUrl" 
                placeholder="请输入链接地址"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="排序" prop="displayOrder">
              <el-input-number
                v-model="itemForm.displayOrder"
                :min="1"
                :max="999"
                :step="1"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="isActive">
              <el-switch
                v-model="itemForm.isActive"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="新窗口打开" v-if="itemForm.linkType !== 'none'">
              <el-switch
                v-model="itemForm.openInNewWindow"
                active-text="是"
                inactive-text="否"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="itemForm.remark"
            type="textarea"
            :rows="2"
            placeholder="轮播图项目备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入轮播图"
      width="600px"
    >
      <div class="import-content">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          class="import-alert"
        >
          <template #default>
            <div>支持Excel格式文件，请按照模板格式准备数据</div>
            <div>
              <el-link type="primary" @click="downloadTemplate">
                <el-icon><Download /></el-icon>
                下载导入模板
              </el-link>
            </div>
          </template>
        </el-alert>

        <el-upload
          ref="importUploadRef"
          :file-list="importFileList"
          :before-upload="beforeImportUpload"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          accept=".xlsx,.xls"
          :limit="1"
          action="/api/banners/import"
          class="import-upload"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              只能上传xlsx/xls文件，且不超过10MB
            </div>
          </template>
        </el-upload>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startImport" :loading="importing">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bannerApi } from '@/api/banner'
import BannerGroupPanel from './components/BannerGroupPanel.vue'
import BannerItemPanel from './components/BannerItemPanel.vue'
import BannerStatistics from './components/BannerStatistics.vue'
import {
  Picture,
  Upload,
  Download,
  Delete,
  FolderOpened,
  DataAnalysis,
  Plus
} from '@element-plus/icons-vue'

// 响应式数据
const activeTab = ref('groups')
const loading = ref(false)
const saving = ref(false)
const importing = ref(false)

const groupDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const importDialogVisible = ref(false)

const groupFormRef = ref(null)
const itemFormRef = ref(null)
const imageUploadRef = ref(null)
const importUploadRef = ref(null)

const imageFileList = ref([])
const importFileList = ref([])

// 数据
const bannerGroups = ref([])
const bannerItems = ref([])
const clickStats = ref([])
const viewStats = ref([])
const selectedItems = ref([])

// 表单数据
const groupForm = reactive({
  id: null,
  name: '',
  type: 'carousel',
  description: '',
  displayPosition: '',
  displayOrder: 1,
  autoPlay: true,
  playInterval: 3000,
  isActive: true,
  remark: ''
})

const itemForm = reactive({
  id: null,
  groupId: null,
  title: '',
  description: '',
  imageUrl: '',
  linkType: 'none',
  linkUrl: '',
  displayOrder: 1,
  openInNewWindow: false,
  isActive: true,
  remark: ''
})

// 统计数据
const bannerStats = ref([
  { type: 'groups', label: '轮播图组', count: 0, icon: 'FolderOpened', color: '#409EFF' },
  { type: 'items', label: '轮播图项目', count: 0, icon: 'Picture', color: '#67C23A' },
  { type: 'active', label: '已启用', count: 0, icon: 'Check', color: '#E6A23C' },
  { type: 'views', label: '总浏览量', count: 0, icon: 'View', color: '#909399' }
])

// 选项数据
const bannerTypes = computed(() => bannerApi.getBannerTypes())
const linkTypes = computed(() => bannerApi.getLinkTypes())
const displayPositions = computed(() => bannerApi.getDisplayPositions())

// 计算属性
const totalBanners = computed(() => {
  return bannerStats.value.reduce((total, stat) => total + stat.count, 0)
})

const hasSelection = computed(() => selectedItems.value.length > 0)

// 表单验证规则
const groupRules = {
  name: [
    { required: true, message: '请输入轮播图组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度为2-50个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  displayPosition: [
    { required: true, message: '请选择显示位置', trigger: 'change' }
  ]
}

const itemRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度为2-100个字符', trigger: 'blur' }
  ],
  groupId: [
    { required: true, message: '请选择所属组', trigger: 'change' }
  ],
  imageUrl: [
    { required: true, message: '请上传图片', trigger: 'blur' }
  ]
}

// 方法
const loadBannerData = async () => {
  try {
    loading.value = true
    
    const [groupsRes, itemsRes, statsRes] = await Promise.all([
      bannerApi.getBannerGroups(),
      bannerApi.getBannerItems(),
      bannerApi.getBannerStats()
    ])
    
    bannerGroups.value = groupsRes.content || []
    bannerItems.value = itemsRes.content || []
    
    // 更新统计数据
    if (statsRes) {
      bannerStats.value[0].count = statsRes.groupCount || 0
      bannerStats.value[1].count = statsRes.itemCount || 0
      bannerStats.value[2].count = statsRes.activeCount || 0
      bannerStats.value[3].count = statsRes.viewCount || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleTabClick = (tab) => {
  activeTab.value = tab.name
  if (tab.name === 'statistics') {
    loadStatistics()
  }
}

const loadStatistics = async () => {
  try {
    const [clickRes, viewRes] = await Promise.all([
      bannerApi.getBannerClickStats(),
      bannerApi.getBannerViewStats()
    ])
    
    clickStats.value = clickRes || []
    viewStats.value = viewRes || []
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

// 轮播图组管理
const addGroup = () => {
  resetGroupForm()
  groupDialogVisible.value = true
}

const editGroup = (group) => {
  Object.assign(groupForm, {
    ...group
  })
  groupDialogVisible.value = true
}

const saveGroup = async () => {
  if (!groupFormRef.value) return
  
  try {
    await groupFormRef.value.validate()
    saving.value = true
    
    if (groupForm.id) {
      await bannerApi.updateBannerGroup(groupForm.id, groupForm)
      ElMessage.success('轮播图组更新成功')
    } else {
      await bannerApi.createBannerGroup(groupForm)
      ElMessage.success('轮播图组创建成功')
    }
    
    groupDialogVisible.value = false
    loadBannerData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const deleteGroup = async (group) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除轮播图组"${group.name}"吗？此操作不可恢复。`,
      '确认删除',
      { type: 'warning' }
    )
    
    await bannerApi.deleteBannerGroup(group.id)
    ElMessage.success('删除成功')
    loadBannerData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const previewGroup = (group) => {
  // 预览轮播图组
  ElMessage.info('预览功能开发中...')
}

const publishGroup = async (group) => {
  try {
    await bannerApi.publishBannerGroup(group.id)
    ElMessage.success('发布成功')
    loadBannerData()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

// 轮播图项目管理
const addItem = () => {
  resetItemForm()
  itemDialogVisible.value = true
}

const editItem = (item) => {
  Object.assign(itemForm, {
    ...item
  })
  
  if (item.imageUrl) {
    imageFileList.value = [{
      name: 'image.jpg',
      url: item.imageUrl
    }]
  }
  
  itemDialogVisible.value = true
}

const saveItem = async () => {
  if (!itemFormRef.value) return
  
  try {
    await itemFormRef.value.validate()
    saving.value = true
    
    if (itemForm.id) {
      await bannerApi.updateBannerItem(itemForm.id, itemForm)
      ElMessage.success('轮播图项目更新成功')
    } else {
      await bannerApi.createBannerItem(itemForm)
      ElMessage.success('轮播图项目创建成功')
    }
    
    itemDialogVisible.value = false
    loadBannerData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const deleteItem = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除轮播图项目"${item.title}"吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await bannerApi.deleteBannerItem(item.id)
    ElMessage.success('删除成功')
    loadBannerData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const uploadImage = (file) => {
  return bannerApi.uploadBannerImage(file)
}

// 文件上传
const beforeImageUpload = (file) => {
  const validation = bannerApi.validateImageFile(file)
  if (!validation.valid) {
    ElMessage.error(validation.message)
    return false
  }
  return true
}

const handleImageSuccess = (response, file) => {
  itemForm.imageUrl = response.url
  ElMessage.success('图片上传成功')
}

const handleImageRemove = () => {
  itemForm.imageUrl = ''
}

// 导入导出
const exportBanners = async () => {
  try {
    const blob = await bannerApi.exportBanners()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `轮播图数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const showImportDialog = () => {
  importDialogVisible.value = true
  importFileList.value = []
}

const downloadTemplate = () => {
  const link = document.createElement('a')
  link.href = '/templates/banner-import-template.xlsx'
  link.download = '轮播图导入模板.xlsx'
  link.click()
}

const beforeImportUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                  file.type === 'application/vnd.ms-excel'
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!')
    return false
  }
  return true
}

const startImport = async () => {
  if (importFileList.value.length === 0) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  try {
    importing.value = true
    await bannerApi.importBanners(importFileList.value[0])
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    loadBannerData()
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

const handleImportSuccess = (response) => {
  ElMessage.success('文件上传成功')
}

const handleImportError = (error) => {
  ElMessage.error('文件上传失败')
}

// 批量操作
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

const batchDelete = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要删除的项目')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedItems.value.length} 个项目吗？`,
      '确认批量删除',
      { type: 'warning' }
    )
    
    const ids = selectedItems.value.map(item => item.id)
    if (activeTab.value === 'groups') {
      await bannerApi.batchDeleteBannerGroups(ids)
    } else {
      await bannerApi.batchDeleteBannerItems(ids)
    }
    
    ElMessage.success('批量删除成功')
    loadBannerData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 重置表单
const resetGroupForm = () => {
  Object.assign(groupForm, {
    id: null,
    name: '',
    type: 'carousel',
    description: '',
    displayPosition: '',
    displayOrder: 1,
    autoPlay: true,
    playInterval: 3000,
    isActive: true,
    remark: ''
  })
  
  if (groupFormRef.value) {
    groupFormRef.value.clearValidate()
  }
}

const resetItemForm = () => {
  Object.assign(itemForm, {
    id: null,
    groupId: null,
    title: '',
    description: '',
    imageUrl: '',
    linkType: 'none',
    linkUrl: '',
    displayOrder: 1,
    openInNewWindow: false,
    isActive: true,
    remark: ''
  })
  
  imageFileList.value = []
  
  if (itemFormRef.value) {
    itemFormRef.value.clearValidate()
  }
}

// 生命周期
onMounted(() => {
  loadBannerData()
})
</script>

<style scoped>
.banner-management {
  padding: 0;
}

.main-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  background: linear-gradient(135deg, #409EFF, #67C23A);
  border-radius: 8px;
  padding: 8px;
  color: white;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-cards {
  margin: 20px 0;
}

.stat-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.1));
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.banner-tabs {
  margin-top: 20px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.group-form,
.item-form {
  max-height: 70vh;
  overflow-y: auto;
}

.image-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}

.upload-tip p {
  margin: 0;
}

.import-content {
  padding: 20px 0;
}

.import-alert {
  margin-bottom: 20px;
}

.import-upload {
  text-align: center;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 40px 20px;
  background: #fafafa;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .stats-cards .el-col {
    margin-bottom: 16px;
  }
  
  .group-form,
  .item-form {
    padding: 0 10px;
  }
}
</style>
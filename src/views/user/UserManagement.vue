<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>
      
      <!-- 标签页 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 微信用户管理 -->
        <el-tab-pane label="微信用户" name="wechat">
          <div class="tab-content">
            <div class="tab-header">
              <div class="header-actions">
                <el-button type="primary" @click="exportWechatUsers" :loading="wechatExporting">
                  <el-icon><Download /></el-icon>
                  导出用户
                </el-button>
                <el-button type="danger" @click="batchDeleteWechatUsers" :disabled="!hasWechatSelection">
                  <el-icon><Delete /></el-icon>
                  批量删除
                </el-button>
              </div>
            </div>
            
            <!-- 微信用户搜索表单 -->
            <div class="search-form">
              <el-form :model="wechatSearchForm" inline>
                <el-form-item label="用户名">
                  <el-input
                    v-model="wechatSearchForm.username"
                    placeholder="请输入用户名"
                    clearable
                    @clear="handleWechatSearch"
                  />
                </el-form-item>
                <el-form-item label="微信昵称">
                  <el-input
                    v-model="wechatSearchForm.nickname"
                    placeholder="请输入微信昵称"
                    clearable
                    @clear="handleWechatSearch"
                  />
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="wechatSearchForm.status" placeholder="选择状态" clearable>
                    <el-option label="活跃" :value="1" />
                    <el-option label="禁用" :value="0" />
                  </el-select>
                </el-form-item>
                <el-form-item label="注册时间">
                  <el-date-picker
                    v-model="wechatSearchForm.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleWechatSearch" :loading="wechatLoading">
                    <el-icon><Search /></el-icon>
                    搜索
                  </el-button>
                  <el-button @click="resetWechatSearch">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 微信用户列表 -->
            <el-table
              v-loading="wechatLoading"
              :data="wechatUserList"
              @selection-change="handleWechatSelectionChange"
              stripe
              border
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="头像" width="80">
                <template #default="{ row }">
                  <el-avatar :size="40" :src="row.avatar || row.avatarUrl">
                    {{ (row.username || row.nickname)?.charAt(0).toUpperCase() }}
                  </el-avatar>
                </template>
              </el-table-column>
              <el-table-column prop="username" label="用户名" min-width="120" />
              <el-table-column prop="nickname" label="微信昵称" min-width="120" />
              <el-table-column prop="openid" label="OpenID" min-width="200" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.status"
                    :active-value="1"
                    :inactive-value="0"
                    @change="handleWechatStatusChange(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="注册时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="最后更新" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.updatedAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="editWechatUser(row)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteWechatUser(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 微信用户分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="wechatPagination.page"
                v-model:page-size="wechatPagination.size"
                :total="wechatPagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleWechatSizeChange"
                @current-change="handleWechatCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 系统用户管理 -->
        <el-tab-pane label="系统用户" name="admin">
          <div class="tab-content">
            <div class="tab-header">
              <div class="header-actions">
                <el-button type="success" @click="addAdmin">
                  <el-icon><Plus /></el-icon>
                  新增管理员
                </el-button>
                <el-button type="primary" @click="exportAdmins" :loading="adminExporting">
                  <el-icon><Download /></el-icon>
                  导出用户
                </el-button>
                <el-button type="danger" @click="batchDeleteAdmins" :disabled="!hasAdminSelection">
                  <el-icon><Delete /></el-icon>
                  批量删除
                </el-button>
              </div>
            </div>
            
            <!-- 系统用户搜索表单 -->
            <div class="search-form">
              <el-form :model="adminSearchForm" inline>
                <el-form-item label="用户名">
                  <el-input
                    v-model="adminSearchForm.username"
                    placeholder="请输入用户名"
                    clearable
                    @clear="handleAdminSearch"
                  />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input
                    v-model="adminSearchForm.email"
                    placeholder="请输入邮箱"
                    clearable
                    @clear="handleAdminSearch"
                  />
                </el-form-item>
                <el-form-item label="角色">
                  <el-select v-model="adminSearchForm.role" placeholder="选择角色" clearable>
                    <el-option label="超级管理员" value="SUPER_ADMIN" />
                    <el-option label="管理员" value="ADMIN" />
                    <el-option label="编辑员" value="EDITOR" />
                  </el-select>
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="adminSearchForm.status" placeholder="选择状态" clearable>
                    <el-option label="活跃" :value="1" />
                    <el-option label="禁用" :value="0" />
                  </el-select>
                </el-form-item>
                <el-form-item label="创建时间">
                  <el-date-picker
                    v-model="adminSearchForm.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleAdminSearch" :loading="adminLoading">
                    <el-icon><Search /></el-icon>
                    搜索
                  </el-button>
                  <el-button @click="resetAdminSearch">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 系统用户列表 -->
            <el-table
              v-loading="adminLoading"
              :data="adminList"
              @selection-change="handleAdminSelectionChange"
              stripe
              border
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="头像" width="80">
                <template #default="{ row }">
                  <el-avatar :size="40" :src="row.avatar">
                    {{ row.username?.charAt(0).toUpperCase() }}
                  </el-avatar>
                </template>
              </el-table-column>
              <el-table-column prop="username" label="用户名" min-width="120" />
              <el-table-column prop="email" label="邮箱" min-width="180" />
              <el-table-column prop="phone" label="手机号" min-width="130" />
              <el-table-column label="角色" width="120">
                <template #default="{ row }">
                  <el-tag :type="getRoleTagType(row.role)">
                    {{ getRoleLabel(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.status"
                    :active-value="1"
                    :inactive-value="0"
                    @change="handleAdminStatusChange(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="最后更新" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.updatedAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="240" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="editAdmin(row)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button type="warning" size="small" @click="resetAdminPassword(row)">
                    <el-icon><Key /></el-icon>
                    重置密码
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteAdmin(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 系统用户分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="adminPagination.page"
                v-model:page-size="adminPagination.size"
                :total="adminPagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleAdminSizeChange"
                @current-change="handleAdminCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 编辑微信用户对话框 -->
    <el-dialog
      v-model="wechatEditDialogVisible"
      title="编辑微信用户"
      width="500px"
    >
      <el-form
        ref="wechatEditFormRef"
        :model="wechatEditForm"
        :rules="wechatEditRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="wechatEditForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="微信昵称" prop="nickname">
          <el-input v-model="wechatEditForm.nickname" placeholder="请输入微信昵称" />
        </el-form-item>
        <el-form-item label="头像URL" prop="avatar">
          <el-input v-model="wechatEditForm.avatar" placeholder="请输入头像URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="wechatEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveWechatUser" :loading="wechatSaving">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑系统用户对话框 -->
    <el-dialog
      v-model="adminEditDialogVisible"
      :title="adminEditForm.id ? '编辑管理员' : '新增管理员'"
      width="500px"
    >
      <el-form
        ref="adminEditFormRef"
        :model="adminEditForm"
        :rules="adminEditRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminEditForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="adminEditForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="adminEditForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="adminEditForm.role" placeholder="选择角色">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="编辑员" value="EDITOR" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!adminEditForm.id" label="密码" prop="password">
          <el-input 
            v-model="adminEditForm.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="头像URL" prop="avatar">
          <el-input v-model="adminEditForm.avatar" placeholder="请输入头像URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAdmin" :loading="adminSaving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import { adminApi } from '@/api/admin'
import {
  Search,
  Refresh,
  Download,
  Delete,
  Edit,
  Key,
  Plus
} from '@element-plus/icons-vue'

// 当前激活的标签页
const activeTab = ref('wechat')

// ==================== 微信用户相关 ====================
const wechatLoading = ref(false)
const wechatExporting = ref(false)
const wechatSaving = ref(false)
const wechatEditDialogVisible = ref(false)
const wechatEditFormRef = ref(null)

const wechatUserList = ref([])
const selectedWechatUsers = ref([])

// 微信用户搜索表单
const wechatSearchForm = reactive({
  username: '',
  nickname: '',
  status: '',
  dateRange: []
})

// 微信用户分页信息
const wechatPagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 微信用户编辑表单
const wechatEditForm = reactive({
  id: null,
  username: '',
  nickname: '',
  avatar: ''
})

// 微信用户表单验证规则
const wechatEditRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ]
}

// ==================== 系统用户相关 ====================
const adminLoading = ref(false)
const adminExporting = ref(false)
const adminSaving = ref(false)
const adminEditDialogVisible = ref(false)
const adminEditFormRef = ref(null)

const adminList = ref([])
const selectedAdmins = ref([])

// 系统用户搜索表单
const adminSearchForm = reactive({
  username: '',
  email: '',
  role: '',
  status: '',
  dateRange: []
})

// 系统用户分页信息
const adminPagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 系统用户编辑表单
const adminEditForm = reactive({
  id: null,
  username: '',
  email: '',
  phone: '',
  role: '',
  password: '',
  avatar: ''
})

// 系统用户表单验证规则
const adminEditRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}

// 计算属性
const hasWechatSelection = computed(() => selectedWechatUsers.value.length > 0)
const hasAdminSelection = computed(() => selectedAdmins.value.length > 0)

// ==================== 标签页切换 ====================
const handleTabClick = (tab) => {
  // activeTab的变化会通过watch自动触发数据加载，这里不需要重复调用
  activeTab.value = tab.name
}

// 根据当前标签页加载对应数据
const loadCurrentTabData = () => {
  if (activeTab.value === 'wechat') {
    loadWechatUsers()
  } else if (activeTab.value === 'admin') {
    loadAdmins()
  }
}

// ==================== 微信用户管理方法 ====================

// 加载微信用户列表
const loadWechatUsers = async () => {
  try {
    wechatLoading.value = true
    
    const params = {
      page: wechatPagination.page - 1,
      size: wechatPagination.size,
      ...wechatSearchForm
    }
    
    // 处理日期范围
    if (wechatSearchForm.dateRange && wechatSearchForm.dateRange.length === 2) {
      params.startDate = wechatSearchForm.dateRange[0]
      params.endDate = wechatSearchForm.dateRange[1]
    }
    delete params.dateRange
    
    const response = await userApi.getUsers(params)
    
    wechatUserList.value = response.content || []
    wechatPagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载微信用户列表失败')
  } finally {
    wechatLoading.value = false
  }
}

// 微信用户搜索处理
const handleWechatSearch = () => {
  wechatPagination.page = 1
  loadWechatUsers()
}

// 重置微信用户搜索
const resetWechatSearch = () => {
  Object.assign(wechatSearchForm, {
    username: '',
    nickname: '',
    status: '',
    dateRange: []
  })
  handleWechatSearch()
}

// 微信用户分页处理
const handleWechatSizeChange = (size) => {
  wechatPagination.size = size
  wechatPagination.page = 1
  loadWechatUsers()
}

const handleWechatCurrentChange = (page) => {
  wechatPagination.page = page
  loadWechatUsers()
}

// 微信用户选择处理
const handleWechatSelectionChange = (selection) => {
  selectedWechatUsers.value = selection
}

// 微信用户状态切换
const handleWechatStatusChange = async (user) => {
  try {
    await userApi.updateUserStatus(user.id, user.status)
    ElMessage.success('用户状态更新成功')
  } catch (error) {
    // 恢复原状态
    user.status = user.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 编辑微信用户
const editWechatUser = (user) => {
  Object.assign(wechatEditForm, {
    id: user.id,
    username: user.username,
    nickname: user.nickname,
    avatar: user.avatar || user.avatarUrl
  })
  wechatEditDialogVisible.value = true
}

// 保存微信用户
const saveWechatUser = async () => {
  if (!wechatEditFormRef.value) return
  
  try {
    await wechatEditFormRef.value.validate()
    wechatSaving.value = true
    
    await userApi.updateUser(wechatEditForm.id, wechatEditForm)
    ElMessage.success('用户更新成功')
    
    wechatEditDialogVisible.value = false
    loadWechatUsers()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    wechatSaving.value = false
  }
}

// 删除微信用户
const deleteWechatUser = (user) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${user.username || user.nickname}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await userApi.deleteUser(user.id)
      ElMessage.success('删除成功')
      loadWechatUsers()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除微信用户
const batchDeleteWechatUsers = () => {
  if (selectedWechatUsers.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedWechatUsers.value.length} 个用户吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const userIds = selectedWechatUsers.value.map(user => user.id)
      await userApi.batchDeleteUsers(userIds)
      ElMessage.success('批量删除成功')
      loadWechatUsers()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 导出微信用户
const exportWechatUsers = async () => {
  try {
    wechatExporting.value = true
    
    const blob = await userApi.exportUsers(wechatSearchForm)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `wechat_users_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    wechatExporting.value = false
  }
}

// ==================== 系统用户管理方法 ====================

// 加载系统用户列表
const loadAdmins = async () => {
  try {
    adminLoading.value = true
    
    const params = {
      page: adminPagination.page - 1,
      size: adminPagination.size,
      ...adminSearchForm
    }
    
    // 处理日期范围
    if (adminSearchForm.dateRange && adminSearchForm.dateRange.length === 2) {
      params.startDate = adminSearchForm.dateRange[0]
      params.endDate = adminSearchForm.dateRange[1]
    }
    delete params.dateRange
    
    const response = await adminApi.getAdmins(params)
    
    adminList.value = response.content || []
    adminPagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载系统用户列表失败')
  } finally {
    adminLoading.value = false
  }
}

// 系统用户搜索处理
const handleAdminSearch = () => {
  adminPagination.page = 1
  loadAdmins()
}

// 重置系统用户搜索
const resetAdminSearch = () => {
  Object.assign(adminSearchForm, {
    username: '',
    email: '',
    role: '',
    status: '',
    dateRange: []
  })
  handleAdminSearch()
}

// 系统用户分页处理
const handleAdminSizeChange = (size) => {
  adminPagination.size = size
  adminPagination.page = 1
  loadAdmins()
}

const handleAdminCurrentChange = (page) => {
  adminPagination.page = page
  loadAdmins()
}

// 系统用户选择处理
const handleAdminSelectionChange = (selection) => {
  selectedAdmins.value = selection
}

// 系统用户状态切换
const handleAdminStatusChange = async (admin) => {
  try {
    await adminApi.updateAdminStatus(admin.id, admin.status)
    ElMessage.success('管理员状态更新成功')
  } catch (error) {
    // 恢复原状态
    admin.status = admin.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 新增管理员
const addAdmin = () => {
  Object.assign(adminEditForm, {
    id: null,
    username: '',
    email: '',
    phone: '',
    role: '',
    password: '',
    avatar: ''
  })
  adminEditDialogVisible.value = true
}

// 编辑系统用户
const editAdmin = (admin) => {
  Object.assign(adminEditForm, {
    id: admin.id,
    username: admin.username,
    email: admin.email,
    phone: admin.phone,
    role: admin.role,
    password: '', // 编辑时不显示密码
    avatar: admin.avatar
  })
  adminEditDialogVisible.value = true
}

// 保存系统用户
const saveAdmin = async () => {
  if (!adminEditFormRef.value) return
  
  try {
    await adminEditFormRef.value.validate()
    adminSaving.value = true
    
    if (adminEditForm.id) {
      // 编辑时不传递密码字段
      const { password, ...updateData } = adminEditForm
      await adminApi.updateAdmin(adminEditForm.id, updateData)
      ElMessage.success('管理员更新成功')
    } else {
      await adminApi.createAdmin(adminEditForm)
      ElMessage.success('管理员创建成功')
    }
    
    adminEditDialogVisible.value = false
    loadAdmins()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    adminSaving.value = false
  }
}

// 删除系统用户
const deleteAdmin = (admin) => {
  ElMessageBox.confirm(
    `确定要删除管理员 "${admin.username}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await adminApi.deleteAdmin(admin.id)
      ElMessage.success('删除成功')
      loadAdmins()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除系统用户
const batchDeleteAdmins = () => {
  if (selectedAdmins.value.length === 0) {
    ElMessage.warning('请选择要删除的管理员')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedAdmins.value.length} 个管理员吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const adminIds = selectedAdmins.value.map(admin => admin.id)
      await adminApi.batchDeleteAdmins(adminIds)
      ElMessage.success('批量删除成功')
      loadAdmins()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 重置系统用户密码
const resetAdminPassword = (admin) => {
  ElMessageBox.confirm(
    `确定要重置管理员 "${admin.username}" 的密码吗？`,
    '确认重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const result = await adminApi.resetAdminPassword(admin.id)
      ElMessageBox.alert(
        `密码重置成功！新密码为：${result.newPassword}`,
        '密码重置成功',
        {
          confirmButtonText: '确定',
          type: 'success'
        }
      )
    } catch (error) {
      ElMessage.error('密码重置失败')
    }
  })
}

// 导出系统用户
const exportAdmins = async () => {
  try {
    adminExporting.value = true
    
    const blob = await adminApi.exportAdmins(adminSearchForm)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `admins_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    adminExporting.value = false
  }
}

// ==================== 工具方法 ====================

// 获取角色标签类型
const getRoleTagType = (role) => {
  const roleTypes = {
    'SUPER_ADMIN': 'danger',
    'ADMIN': 'warning',
    'EDITOR': 'info'
  }
  return roleTypes[role] || 'info'
}

// 获取角色标签文本
const getRoleLabel = (role) => {
  const roleLabels = {
    'SUPER_ADMIN': '超级管理员',
    'ADMIN': '管理员',
    'EDITOR': '编辑员'
  }
  return roleLabels[role] || role
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'wechat') {
    loadWechatUsers()
  } else if (newTab === 'admin') {
    loadAdmins()
  }
}, { immediate: false })

onMounted(() => {
  loadCurrentTabData()
})
</script>

<style scoped>
.user-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-content {
  margin-top: 20px;
}

.tab-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
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
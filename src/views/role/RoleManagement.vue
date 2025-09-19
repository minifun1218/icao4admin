<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色权限管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="createRole">
              <el-icon><Plus /></el-icon>
              添加角色
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
          <el-form-item label="角色名称">
            <el-input
              v-model="searchForm.name"
              placeholder="搜索角色名称"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="显示名称">
            <el-input
              v-model="searchForm.displayName"
              placeholder="搜索显示名称"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.isEnabled" placeholder="选择状态" clearable>
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
      
      <!-- 角色列表 -->
      <el-table
        v-loading="loading"
        :data="roleList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" min-width="120" />
        <el-table-column prop="displayName" label="显示名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="权限数量" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.permissions?.length || 0 }}个
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户数量" width="100">
          <template #default="{ row }">
            <el-tag type="success" size="small">
              {{ row.userCount || 0 }}人
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
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
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="managePermissions(row)">
              <el-icon><Key /></el-icon>
              权限
            </el-button>
            <el-button type="primary" size="small" @click="editRole(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteRole(row)">
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
    
    <!-- 编辑角色对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editForm.id ? '编辑角色' : '新增角色'"
      width="600px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入角色名称（英文）" />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="editForm.displayName" placeholder="请输入显示名称（中文）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="是否启用" prop="isEnabled">
          <el-switch v-model="editForm.isEnabled" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 权限管理对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="权限管理"
      width="800px"
    >
      <div class="permission-management">
        <div class="role-info">
          <h4>角色：{{ currentRole?.displayName }} ({{ currentRole?.name }})</h4>
          <p>{{ currentRole?.description }}</p>
        </div>
        
        <el-divider />
        
        <div class="permission-list">
          <div v-for="category in permissionCategories" :key="category.id" class="permission-category">
            <div class="category-header">
              <el-checkbox
                v-model="categoryChecked[category.id]"
                :indeterminate="categoryIndeterminate[category.id]"
                @change="handleCategoryChange(category.id)"
              >
                {{ category.name }}
              </el-checkbox>
            </div>
            <div class="category-permissions">
              <el-checkbox-group v-model="selectedPermissions">
                <el-checkbox
                  v-for="permission in getPermissionsByCategory(category.id)"
                  :key="permission.id"
                  :label="permission.id"
                >
                  {{ permission.name }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions" :loading="permissionSaving">
          保存权限
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi } from '@/api/role'
import {
  Search,
  Refresh,
  Plus,
  Delete,
  Edit,
  Key
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const permissionSaving = ref(false)
const editDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const editFormRef = ref(null)

const roleList = ref([])
const selectedRoles = ref([])
const currentRole = ref(null)
const allPermissions = ref([])
const permissionCategories = ref([])
const selectedPermissions = ref([])

// 权限分类状态
const categoryChecked = ref({})
const categoryIndeterminate = ref({})

// 搜索表单
const searchForm = reactive({
  name: '',
  displayName: '',
  isEnabled: ''
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
  name: '',
  displayName: '',
  description: '',
  isEnabled: true
})

// 表单验证规则
const editRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色名称只能包含大写字母和下划线', trigger: 'blur' }
  ],
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' }
  ]
}

// 计算属性
const hasSelection = computed(() => selectedRoles.value.length > 0)

// 根据分类获取权限
const getPermissionsByCategory = (categoryId) => {
  return allPermissions.value.filter(p => p.category === categoryId)
}

// 加载角色列表
const loadRoles = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sort: 'createdAt,desc',
      ...searchForm
    }
    
    const response = await roleApi.getRoles(params)
    
    roleList.value = response.content || []
    pagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// 加载权限数据
const loadPermissions = async () => {
  try {
    allPermissions.value = roleApi.getPredefinedPermissions()
    permissionCategories.value = roleApi.getPermissionCategories()
  } catch (error) {
    ElMessage.error('加载权限数据失败')
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadRoles()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    displayName: '',
    isEnabled: ''
  })
  handleSearch()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadRoles()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadRoles()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedRoles.value = selection
}

// 状态切换
const handleStatusChange = async (role) => {
  try {
    await roleApi.updateRole(role.id, { isEnabled: role.isEnabled })
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 恢复原状态
    role.isEnabled = !role.isEnabled
    ElMessage.error('状态更新失败')
  }
}

// 创建角色
const createRole = () => {
  Object.assign(editForm, {
    id: null,
    name: '',
    displayName: '',
    description: '',
    isEnabled: true
  })
  editDialogVisible.value = true
}

// 编辑角色
const editRole = (role) => {
  Object.assign(editForm, {
    id: role.id,
    name: role.name,
    displayName: role.displayName,
    description: role.description,
    isEnabled: role.isEnabled
  })
  editDialogVisible.value = true
}

// 保存角色
const saveRole = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    if (editForm.id) {
      await roleApi.updateRole(editForm.id, editForm)
      ElMessage.success('角色更新成功')
    } else {
      await roleApi.createRole(editForm)
      ElMessage.success('角色创建成功')
    }
    
    editDialogVisible.value = false
    loadRoles()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除角色
const deleteRole = (role) => {
  ElMessageBox.confirm(
    `确定要删除角色 "${role.displayName}" 吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await roleApi.deleteRole(role.id)
      ElMessage.success('删除成功')
      loadRoles()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const batchDelete = () => {
  if (selectedRoles.value.length === 0) {
    ElMessage.warning('请选择要删除的角色')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRoles.value.length} 个角色吗？此操作不可恢复。`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const roleIds = selectedRoles.value.map(role => role.id)
      await roleApi.batchDeleteRoles(roleIds)
      ElMessage.success('批量删除成功')
      loadRoles()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 权限管理
const managePermissions = async (role) => {
  currentRole.value = role
  
  try {
    // 获取角色的权限
    const permissions = await roleApi.getRolePermissions(role.id)
    selectedPermissions.value = permissions.map(p => p.id)
    
    // 更新分类状态
    updateCategoryStates()
    
    permissionDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取角色权限失败')
  }
}

// 更新分类状态
const updateCategoryStates = () => {
  permissionCategories.value.forEach(category => {
    const categoryPermissions = getPermissionsByCategory(category.id)
    const selectedInCategory = categoryPermissions.filter(p => 
      selectedPermissions.value.includes(p.id)
    )
    
    if (selectedInCategory.length === 0) {
      categoryChecked.value[category.id] = false
      categoryIndeterminate.value[category.id] = false
    } else if (selectedInCategory.length === categoryPermissions.length) {
      categoryChecked.value[category.id] = true
      categoryIndeterminate.value[category.id] = false
    } else {
      categoryChecked.value[category.id] = false
      categoryIndeterminate.value[category.id] = true
    }
  })
}

// 分类选择变化
const handleCategoryChange = (categoryId) => {
  const categoryPermissions = getPermissionsByCategory(categoryId)
  const isChecked = categoryChecked.value[categoryId]
  
  if (isChecked) {
    // 添加该分类下所有权限
    categoryPermissions.forEach(p => {
      if (!selectedPermissions.value.includes(p.id)) {
        selectedPermissions.value.push(p.id)
      }
    })
  } else {
    // 移除该分类下所有权限
    selectedPermissions.value = selectedPermissions.value.filter(id => 
      !categoryPermissions.some(p => p.id === id)
    )
  }
  
  updateCategoryStates()
}

// 监听权限选择变化
watch(selectedPermissions, () => {
  updateCategoryStates()
}, { deep: true })

// 保存权限
const savePermissions = async () => {
  try {
    permissionSaving.value = true
    
    await roleApi.assignRolePermissions(currentRole.value.id, selectedPermissions.value)
    
    ElMessage.success('权限保存成功')
    permissionDialogVisible.value = false
    loadRoles()
  } catch (error) {
    ElMessage.error('权限保存失败')
  } finally {
    permissionSaving.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadRoles()
  loadPermissions()
})
</script>

<style scoped>
.role-management {
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

.permission-management {
  max-height: 600px;
  overflow-y: auto;
}

.role-info {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.role-info h4 {
  margin: 0 0 5px 0;
  color: #409eff;
}

.role-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.permission-category {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.category-header {
  background-color: #f5f7fa;
  padding: 10px 15px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
}

.category-permissions {
  padding: 15px;
}

.category-permissions .el-checkbox {
  margin-right: 20px;
  margin-bottom: 10px;
  min-width: 120px;
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
  
  .category-permissions .el-checkbox {
    display: block;
    margin-right: 0;
    margin-bottom: 8px;
  }
}
</style>

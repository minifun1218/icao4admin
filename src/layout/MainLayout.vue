<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="sidebar-header">
        <img src="/favicon.ico" alt="Logo" class="logo" />
        <h2 v-if="!isCollapse">ICAO4 管理系统</h2>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        
        <el-sub-menu index="vocabs">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>词汇管理</span>
          </template>
          <el-menu-item index="/vocabs">词汇管理</el-menu-item>
          <el-menu-item index="/vocab-types">词汇类型管理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="terms">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>术语管理</span>
          </template>
          <el-menu-item index="/terms">术语管理</el-menu-item>
          <el-menu-item index="/term-types">术语类型管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/media">
          <el-icon><PictureFilled /></el-icon>
          <span>媒体资源</span>
        </el-menu-item>
        <!-- 轮播图管理 - 功能开发中 -->
        <!-- 
        <el-menu-item index="/banners">
          <el-icon><PictureFilled /></el-icon>
          <span>轮播图管理</span>
        </el-menu-item>
        -->
        
        <!-- 题库管理 - 功能开发中 -->

        <el-sub-menu index="question-bank">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>题库管理</span>
          </template>
          <el-menu-item index="/listening-comprehension">听力理解</el-menu-item>
          <el-menu-item index="/story-retell">故事复述</el-menu-item>
          <el-menu-item index="/listening-qa">听力简答</el-menu-item>
          <el-menu-item index="/atc-simulation">模拟通话</el-menu-item>
          <el-menu-item index="/oral-interview">口语能力面试</el-menu-item>
        </el-sub-menu>

        
        <!-- 考试管理 - 功能开发中 -->
        <!-- 
        <el-sub-menu index="exams">
          <template #title>
            <el-icon><Medal /></el-icon>
            <span>考试管理</span>
          </template>
          <el-menu-item index="/exam-modules">考试模块管理</el-menu-item>
          <el-menu-item index="/exam-papers">试卷管理</el-menu-item>
          <el-menu-item index="/exam-info">考试信息管理</el-menu-item>
          <el-menu-item index="/exam-results">成绩管理</el-menu-item>
        </el-sub-menu>
        -->
        
        <!-- 媒体资源 - 功能开发中 -->
        <!-- 

        -->
        
        <el-menu-item index="/roles">
          <el-icon><Key /></el-icon>
          <span>角色权限</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleSidebar"
            class="collapse-btn"
          >
            <el-icon size="20">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </el-button>
          
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteMeta.title">
              {{ currentRouteMeta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 用户信息下拉菜单 -->
          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <el-avatar :size="30" :src="userInfo.avatar">
                {{ userInfo.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
  
  <!-- 修改密码对话框 -->
  <el-dialog v-model="changePasswordVisible" title="修改密码" width="400px">
    <el-form
      ref="passwordForm"
      :model="passwordData"
      :rules="passwordRules"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="passwordData.oldPassword"
          type="password"
          show-password
          placeholder="请输入原密码"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordData.newPassword"
          type="password"
          show-password
          placeholder="请输入新密码"
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="passwordData.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="changePasswordVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPasswordChange" :loading="passwordLoading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  User,
  Document,
  Collection,
  Medal,
  PictureFilled,
  Key,
  Fold,
  Expand,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 侧边栏状态
const isCollapse = ref(false)
const sidebarWidth = computed(() => isCollapse.value ? '64px' : '250px')

// 当前路由信息
const activeMenu = computed(() => route.path)
const currentRouteMeta = computed(() => route.meta || {})

// 用户信息
const userInfo = computed(() => authStore.adminUserInfo || {})

// 修改密码相关
const changePasswordVisible = ref(false)
const passwordLoading = ref(false)
const passwordForm = ref(null)
const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为6-100个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordData.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理用户菜单命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      // TODO: 打开个人资料页面
      ElMessage.info('个人资料功能待开发')
      break
    case 'changePassword':
      changePasswordVisible.value = true
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 提交密码修改
const submitPasswordChange = async () => {
  if (!passwordForm.value) return
  
  try {
    await passwordForm.value.validate()
    passwordLoading.value = true
    
    await authStore.changePassword(passwordData)
    
    ElMessage.success('密码修改成功')
    changePasswordVisible.value = false
    
    // 重置表单
    Object.assign(passwordData, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    authStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  })
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 20px;
  color: white;
  border-bottom: 1px solid #434a54;
}

.logo {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.sidebar-menu {
  border: none;
  background-color: transparent;
}

/* 菜单项基础样式 */
.sidebar-menu .el-menu-item,
.sidebar-menu :deep(.el-sub-menu__title) {
  color: #bfcbd9;
}

/* 菜单项悬停效果 */
.sidebar-menu .el-menu-item:hover,
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: #434a54 !important;
  color: #ffffff;
}

/* 菜单项激活状态 */
.sidebar-menu .el-menu-item.is-active {
  background-color: #409eff;
  color: #ffffff;
}

/* 子菜单项样式 */
.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  color: #bfcbd9;
  padding-left: 50px !important;
  background-color: #434a54;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background-color: #434a54 !important;
  color: #ffffff;
}

.sidebar-menu .el-sub-menu .el-menu-item.is-active {
  background-color: #409eff !important;
  color: #ffffff;
}

.header {
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  margin-right: 20px;
  color: #606266;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  margin: 0 8px 0 12px;
  font-size: 14px;
  color: #606266;
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
}
</style>

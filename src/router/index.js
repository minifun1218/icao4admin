import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/layout/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '仪表盘' }
        },
        {
          path: '/users',
          name: 'UserManagement',
          component: () => import('@/views/user/UserManagement.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: '/vocabs',
          name: 'VocabularyManagement',
          component: () => import('@/views/vocab/VocabularyManagement.vue'),
          meta: { title: '词汇管理' }
        },
        {
          path: '/vocab-types',
          name: 'VocabularyTypeManagement',
          component: () => import('@/views/vocab/VocabularyTypeManagement.vue'),
          meta: { title: '词汇类型管理' }
        },
        {
          path: '/terms',
          name: 'TerminologyManagement',
          component: () => import('@/views/term/TerminologyManagement.vue'),
          meta: { title: '术语管理' }
        },
        {
          path: '/term-types',
          name: 'TerminologyTypeManagement',
          component: () => import('@/views/term/TerminologyTypeManagement.vue'),
          meta: { title: '术语类型管理' }
        },
        {
          path: '/media',
          name: 'MediaManagement',
          component: () => import('@/views/media/MediaManagement.vue'),
          meta: { title: '媒体资源管理' }
        },
        {
          path: '/roles',
          name: 'RoleManagement',
          component: () => import('@/views/role/RoleManagement.vue'),
          meta: { title: '角色权限管理' }
        },
        {
          path: '/rsa-test',
          name: 'RSATestPage',
          component: () => import('@/views/RSATestPage.vue'),
          meta: { title: 'RSA+AES加密测试' }
        }
      ]
    },
    // 404页面 - 必须放在所有路由的最后
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: { 
        requiresAuth: false,
        title: '页面未找到'
      }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  console.log('🧭 路由导航:', {
    from: from.path,
    to: to.path,
    requiresAuth: to.meta.requiresAuth,
    isAuthenticated: authStore.isAuthenticated
  })
  
  // 检查token有效性
  if (authStore.token && !authStore.checkTokenValidity()) {
    console.warn('⚠️ Token无效，跳转到登录页')
    next('/login')
    return
  }
  
  // 需要认证的路由
  if (to.meta.requiresAuth !== false && !authStore.isAuthenticated) {
    console.log('🔒 需要登录，跳转到登录页')
    next('/login')
    return
  }
  
  // 已登录用户访问登录页或注册页，跳转到首页
  if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
    console.log('✅ 已登录，跳转到首页')
    next('/')
    return
  }
  
  // 角色权限检查
  if (to.meta.roles && to.meta.roles.length > 0) {
    const hasPermission = authStore.hasAnyRole(to.meta.roles)
    if (!hasPermission) {
      console.warn('❌ 权限不足:', {
        requiredRoles: to.meta.roles,
        userRoles: authStore.userRoles
      })
      next('/403') // 需要创建403页面
      return
    }
  }
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - ICAO4 管理系统`
  } else {
    document.title = 'ICAO4 管理系统'
  }
  
  next()
})

// 路由后置守卫
router.afterEach((to, from) => {
  console.log('✅ 路由完成:', to.path)
})

export default router

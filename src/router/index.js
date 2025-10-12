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
            // 听力理解管理
            {
              path: '/listening-comprehension/questions',
              name: 'ListeningQuestionManagement',
              component: () => import('@/views/listening-comprehension/QuestionManagement.vue'),
              meta: { title: '听力理解 - 问题管理' }
            },
            {
              path: '/listening-comprehension/options',
              name: 'ListeningChoiceManagement',
              component: () => import('@/views/listening-comprehension/ChoiceManagement.vue'),
              meta: { title: '听力理解 - 选项管理' }
            },
            // 故事复述管理
            {
              path: '/story-retell',
              name: 'StoryRetellManagement',
              component: () => import('@/views/story-retell/StoryRetellManagement.vue'),
              meta: { title: '故事复述管理' }
            },
            // 听力简答管理
            {
              path: '/listening-qa/dialogs',
              name: 'listening-qa-dialogs',
              component: () => import('@/views/listening-qa/DialogManagement.vue'),
              meta: { title: '听力简答 - 对话管理' }
            },
            {
              path: '/listening-qa/questions/:dialogId?',
              name: 'listening-qa-questions',
              component: () => import('@/views/listening-qa/QuestionManagement.vue'),
              meta: { title: '听力简答 - 问题管理' }
            },
            // 模拟通话管理
            {
              path: '/atc-simulation/airports',
              name: 'atc-airports',
              component: () => import('@/views/atc-simulation/AirportManagement.vue'),
              meta: { title: '模拟通话 - 机场管理' }
            },
            {
              path: '/atc-simulation/scenarios',
              name: 'atc-scenarios',
              component: () => import('@/views/atc-simulation/ScenarioManagement.vue'),
              meta: { title: '模拟通话 - 场景管理' }
            },
            {
              path: '/atc-simulation/scenarios/:scenarioId/turns',
              name: 'atc-turns',
              component: () => import('@/views/atc-simulation/TurnManagement.vue'),
              meta: { title: '模拟通话 - 轮次管理' }
            },
            // 口语能力面试管理
            {
              path: '/oral-interview/topics',
              name: 'opi-topics',
              component: () => import('@/views/opi/TopicManagement.vue'),
              meta: { title: '口语能力面试 - 话题管理' }
            },
            {
              path: '/oral-interview/questions',
              name: 'opi-questions',
              component: () => import('@/views/opi/QuestionManagement.vue'),
              meta: { title: '口语能力面试 - 问题管理' }
            },
            // 考试管理
            {
              path: '/exam-modules',
              name: 'ExamModuleManagement',
              component: () => import('@/views/exam/ModuleManagement.vue'),
              meta: { title: '考试管理 - 模块管理' }
            },
            {
              path: '/exam-papers',
              name: 'ExamPaperManagement',
              component: () => import('@/views/exam/PaperManagement.vue'),
              meta: { title: '考试管理 - 试卷管理' }
            },
            {
              path: '/exam-results',
              name: 'ExamResultManagement',
              component: () => import('@/views/exam/ResultManagement.vue'),
              meta: { title: '考试管理 - 成绩管理' }
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

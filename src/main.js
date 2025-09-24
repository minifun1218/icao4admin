import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/main.css'

const app = createApp(App)

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  // 忽略浏览器扩展相关错误
  if (err.message && err.message.includes('Extension context invalidated')) {
    return
  }
  if (err.message && err.message.includes('Could not establish connection')) {
    return
  }
  
  console.error('应用错误:', err, info)
}

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局处理未捕获的Promise拒绝
window.addEventListener('unhandledrejection', event => {
  // 忽略扩展相关错误
  if (event.reason && event.reason.message && 
      (event.reason.message.includes('Extension context invalidated') ||
       event.reason.message.includes('Could not establish connection'))) {
    event.preventDefault()
    return
  }
  
  console.error('未处理的Promise拒绝:', event.reason)
})

// 处理运行时错误
window.addEventListener('error', event => {
  // 忽略扩展相关错误
  if (event.message && 
      (event.message.includes('Extension context invalidated') ||
       event.message.includes('Could not establish connection') ||
       event.message.includes('runtime.lastError'))) {
    return
  }
  
  console.error('运行时错误:', event.error)
})

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { size: 'default' })

app.mount('#app')
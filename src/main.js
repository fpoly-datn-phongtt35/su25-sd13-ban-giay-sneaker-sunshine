import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import '@fortawesome/fontawesome-free/css/all.min.css'

import { createApp, reactive } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import { getCurrentUser } from './utils/auth'  

const app = createApp(App)

// Khởi tạo global state reactive để lưu user
const globalState = reactive({
  currentUser: null
})

// Khôi phục user từ localStorage
const user = getCurrentUser()
if (user) {
  globalState.currentUser = user
  console.log('✅ Khôi phục người dùng:', user)
}

// Cung cấp globalState cho toàn app
app.provide('globalState', globalState)

app.use(router)
app.use(ElementPlus)
app.mount('#app')

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-icons/font/bootstrap-icons.css';

import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import '@fortawesome/fontawesome-free/css/all.min.css';

import { createApp } from 'vue' 
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
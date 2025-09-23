<template>
  <el-dialog
    v-model="dialogVisible"
    :title="mode === 'login' ? 'Đăng nhập' : 'Quên mật khẩu'"
    width="450px"
    destroy-on-close
    :close-on-click-modal="false"
  >
    <!-- Form Đăng nhập -->
    <el-form
      v-if="mode === 'login'"
      :model="loginForm"
      ref="loginFormRef"
      :rules="rules"
      @submit.prevent="handleLogin"
      label-position="top"
    >
      <el-form-item label="Tên đăng nhập" prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="Nhập tên đăng nhập"
          :prefix-icon="User"
        />
      </el-form-item>

      <el-form-item label="Mật khẩu" prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="Nhập mật khẩu"
          :prefix-icon="Lock"
          show-password
          @keyup.enter="handleLogin"
        />
      </el-form-item>

      <div style="text-align: right; margin-bottom: 10px;">
        <el-button type="text" @click="openForgotModal">Quên mật khẩu?</el-button>
      </div>

      <el-form-item style="text-align: center">
        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          :icon="Right"
        >
          Đăng nhập
        </el-button>
      </el-form-item>
    </el-form>

    <!-- Form Quên mật khẩu -->
    <el-form
      v-else
      :model="forgotForm"
      ref="forgotFormRef"
      :rules="forgotRules"
      @submit.prevent="handleForgot"
      label-position="top"
    >
      <el-form-item label="Số điện thoại" prop="phone">
        <el-input v-model="forgotForm.phone" placeholder="Nhập số điện thoại" />
      </el-form-item>

      <el-form-item label="Email" prop="email">
        <el-input v-model="forgotForm.email" placeholder="Nhập email" />
      </el-form-item>

      <el-form-item style="text-align: center">
        <el-button type="primary" native-type="submit" :loading="loading">
          Gửi yêu cầu
        </el-button>
        <el-button type="text" @click="mode = 'login'">← Quay lại đăng nhập</el-button>
      </el-form-item>
    </el-form>

    <template #footer>
      <div v-if="mode === 'login'" style="text-align: center">
        <span>Chưa có tài khoản?</span>
        <el-button type="text" @click="openRegisterModal">Đăng ký ngay</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Right } from '@element-plus/icons-vue'
import apiClient from '../utils/axiosInstance.js'
import axios from 'axios'

// Props và Emits
const props = defineProps({
  isVisible: Boolean
})
const emit = defineEmits(['update:isVisible', 'loggedIn', 'openRegister'])

// Điều khiển dialog
const dialogVisible = ref(props.isVisible)
watch(() => props.isVisible, val => dialogVisible.value = val)
watch(dialogVisible, val => emit('update:isVisible', val))

// State: login / forgot
const mode = ref('login')

// Dữ liệu form login
const loginForm = ref({ username: '', password: '' })
const loginFormRef = ref(null)
const loading = ref(false)

// Rules login
const rules = {
  username: [{ required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' }],
  password: [{ required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' }]
}

// Hàm đăng nhập
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    const res = await apiClient.post('/auth/login-user', loginForm.value)
    const { token, employeeName, customerName, id, customerId } = res.data

    localStorage.setItem('token', token)
    localStorage.setItem('employeeName', employeeName || '')
    localStorage.setItem('customerName', customerName || '')
    localStorage.setItem('userId', id)
    localStorage.setItem('customerId', customerId)

    const guestCart = localStorage.getItem('cart_guest')
    if (guestCart) {
      localStorage.setItem(`cart_${id}`, guestCart)
      localStorage.removeItem('cart_guest')
    }

    emit('loggedIn', { username: loginForm.value.username, employeeName, customerName, id, token })
    dialogVisible.value = false
  } catch (error) {
    const msg = error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng thử lại.'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

// Dữ liệu form forgot password
const forgotForm = ref({ phone: '', email: '' })
const forgotFormRef = ref(null)

// Rules forgot
const forgotRules = {
  phone: [{ required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' }],
  email: [
    { required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
    { type: 'email', message: 'Email không hợp lệ', trigger: ['blur', 'change'] }
  ]
}

const handleForgot = async () => {
  try {
    await forgotFormRef.value.validate()
    loading.value = true
    await axios.post('http://localhost:8080/api/auth/change-pass', forgotForm.value)
    ElMessage.success('Yêu cầu đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra email.')
    mode.value = 'login'
  } catch (error) {
    const msg = error.response?.data?.message || 'Không thể gửi yêu cầu. Thử lại.'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const openForgotModal = () => {
  mode.value = 'forgot'
}

const openRegisterModal = () => {
  dialogVisible.value = false
  emit('openRegister')
}
</script>

<style scoped>
.el-dialog__title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  width: 100%;
}
</style>

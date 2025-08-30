<template>
  <el-dialog
    v-model="dialogVisible"
    title="Đăng nhập"
    width="450px"
    destroy-on-close
    :close-on-click-modal="false"
  >
    <el-form
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

    <template #footer>
      <div style="text-align: center">
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

// Props và Emits
const props = defineProps({
  isVisible: Boolean
})
const emit = defineEmits(['update:isVisible', 'loggedIn', 'openRegister'])

// Điều khiển dialog
const dialogVisible = ref(props.isVisible)
watch(() => props.isVisible, val => dialogVisible.value = val)
watch(dialogVisible, val => emit('update:isVisible', val))

// Dữ liệu form
const loginForm = ref({
  username: '',
  password: ''
})
const loginFormRef = ref(null)
const loading = ref(false)

// Validation rules
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

    console.log('id:', id)

    // Lưu thông tin user vào localStorage
    localStorage.setItem('token', token)
    localStorage.setItem('employeeName', employeeName || '')
    localStorage.setItem('customerName', customerName || '')
    localStorage.setItem('userId', id)
    localStorage.setItem('customerId', customerId)

    // Giả sử giỏ hàng khách vãng lai lưu ở 'cart_guest'
    const guestCart = localStorage.getItem('cart_guest')
    if (guestCart) {
      localStorage.setItem(`cart_${id}`, guestCart) // chuyển giỏ hàng guest sang user
      localStorage.removeItem('cart_guest') // xoá giỏ hàng guest
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

// Mở form đăng ký
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

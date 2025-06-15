<template>
  <el-dialog
    v-model="dialogVisible"
    title="Đăng nhập"
    width="450px"
    destroy-on-close
    :close-on-click-modal="false"
    class="login-modal"
  >
    <el-form :model="loginForm" ref="loginFormRef" :rules="rules" @submit.prevent="handleLogin" label-position="top">
      <el-form-item label="Tên đăng nhập" prop="username">
        <el-input v-model="loginForm.username" placeholder="Nhập tên đăng nhập" :prefix-icon="User" />
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
      <el-form-item class="login-button-container">
        <el-button type="primary" native-type="submit" :loading="loading" :icon="Right" size="large">
          Đăng nhập
        </el-button>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer login-footer">
        <p>
          Chưa có tài khoản?
          <el-button type="text" @click="openRegisterModal" class="register-link">
            Đăng ký ngay
          </el-button>
        </p>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Lock, Right } from '@element-plus/icons-vue';
// import axios from 'axios'; // Bỏ comment nếu bạn muốn gọi API thật sự

const props = defineProps({
  isVisible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:isVisible', 'loggedIn', 'openRegister']);

const dialogVisible = ref(props.isVisible);
const loginFormRef = ref(null);
const loading = ref(false);

const loginForm = ref({
  username: '',
  password: ''
});

// Watch for changes in prop.isVisible to update dialogVisible
watch(() => props.isVisible, (newVal) => {
  dialogVisible.value = newVal;
  if (newVal) {
    resetForm(); // Reset form when dialog opens
  }
});

// Watch for changes in dialogVisible to emit update event
watch(dialogVisible, (newVal) => {
  emit('update:isVisible', newVal);
});

const closeModal = () => {
  dialogVisible.value = false;
};

const openRegisterModal = () => {
  closeModal(); // Đóng modal đăng nhập
  emit('openRegister'); // Báo cho component cha mở modal đăng ký
};

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate();
    loading.value = true;

    // Simulate API call for login
    console.log('Đang đăng nhập với:', loginForm.value);

    // Replace with your actual API call
    // const response = await axios.post('http://localhost:8080/api/auth/login', loginForm.value);
    // console.log('Login successful:', response.data);

    // Simulate success after a delay
    await new Promise(resolve => setTimeout(resolve, 1000));

    if (loginForm.value.username === 'test' && loginForm.value.password === '123456') {
      ElMessage.success('Đăng nhập thành công!');
      emit('loggedIn', { username: loginForm.value.username }); // Gửi thông tin người dùng
      closeModal(); // Đóng modal
    } else {
      ElMessage.error('Tên đăng nhập hoặc mật khẩu không đúng!');
    }

  } catch (error) {
    loading.value = false;
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(`Lỗi: ${error.response.data.message}`);
    } else {
      // Validation error handled by Element Plus automatically with ElMessage.error
      // Only show a generic error if it's not a validation error and not an API error message
      if (!error.message || error.message.includes('Validation Failed')) {
         // Do nothing, validation messages appear next to fields
      } else {
        ElMessage.error('Có lỗi xảy ra khi đăng nhập. Vui lòng thử lại.');
      }
    }
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  if (loginFormRef.value) {
    loginFormRef.value.resetFields();
  }
};

const rules = ref({
  username: [
    { required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' },
    { min: 3, message: 'Tên đăng nhập phải có ít nhất 3 ký tự', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' },
    { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự', trigger: 'blur' }
  ]
});
</script>

<style>
.login-modal .el-dialog__header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 20px;
  margin-right: 0;
}

.login-modal .el-dialog__title {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  text-align: center; /* Center the title */
  width: 100%; /* Ensure title takes full width */
}

.login-modal .el-dialog__body {
  padding: 30px;
}

.login-modal .el-form-item {
  margin-bottom: 22px;
}

.login-modal .el-form-item__label {
  font-weight: 600;
  color: #606266;
  margin-bottom: 5px;
}

.login-modal .el-input,
.login-modal .el-select {
  width: 100%;
}

.login-modal .el-input__wrapper {
  height: 40px;
  line-height: 40px;
  border-radius: 6px;
}

.login-modal .el-input__inner:focus,
.login-modal .el-select__wrapper.is-focused {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.login-modal .login-button-container {
  margin-top: 30px;
  text-align: center;
}

.login-modal .login-button-container .el-button {
  width: 180px;
  padding: 12px 20px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 8px;
}

.login-modal .el-dialog__footer {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
  text-align: center; /* Center the footer content */
}

.login-modal .login-footer p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.login-modal .login-footer .register-link {
  color: #409EFF; /* Element Plus primary color */
  font-weight: bold;
  margin-left: 5px;
  padding: 0; /* Remove default button padding for text style */
  height: auto; /* Remove default button height */
  line-height: inherit; /* Inherit line height */
}

.login-modal .login-footer .register-link:hover {
  text-decoration: underline;
}
</style>
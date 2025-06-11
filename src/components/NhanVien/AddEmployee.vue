<template>
  <div class="add-employee p-4">
    <el-card shadow="hover">
      <!-- Nút quay lại -->
      <el-button type="info" icon="ArrowLeft" @click="goBack" class="mb-4"> Quay lại </el-button>

      <h2 class="mb-4">
        <el-icon><User /></el-icon> Thêm nhân viên mới
      </h2>

      <el-form :model="form" :rules="rules" ref="employeeForm" label-width="150px">
        <el-form-item label="Họ tên" prop="employeeName">
          <el-input v-model="form.employeeName" />
        </el-form-item>

        <el-form-item label="Tên đăng nhập" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="Mật khẩu" prop="password">
  <el-input
    v-model="form.password"
    type="password"
    show-password
  />
</el-form-item>


        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item label="Điện thoại" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>

        <el-form-item label="Giới tính" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">Nam</el-radio>
            <el-radio :label="0">Nữ</el-radio>
            <el-radio :label="2">Khác</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="Ngày sinh" prop="dateOfBirth">
          <el-date-picker
            v-model="form.dateOfBirth"
            type="date"
            placeholder="Chọn ngày sinh"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="Ngày tuyển dụng" prop="hireDate">
          <el-date-picker
            v-model="form.hireDate"
            type="date"
            placeholder="Chọn ngày tuyển dụng"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="Lương" prop="salary">
          <el-input
            v-model="form.salary"
            type="text"
            placeholder="Nhập lương"
            :formatter="formatSalary"
            :parser="parseSalary"
          />
        </el-form-item>

        <el-form-item label="Quốc gia" prop="country">
          <el-input v-model="form.country" />
        </el-form-item>

        <el-form-item label="Tỉnh/Thành phố" prop="province">
          <el-input v-model="form.province" />
        </el-form-item>

        <el-form-item label="Quận/Huyện" prop="district">
          <el-input v-model="form.district" />
        </el-form-item>

        <el-form-item label="Phường/Xã" prop="ward">
          <el-input v-model="form.ward" />
        </el-form-item>

        <el-form-item label="Số nhà" prop="houseName">
          <el-input v-model="form.houseName" />
        </el-form-item>

        <el-form-item label="Vai trò" prop="role">
          <el-select v-model="form.role" placeholder="Chọn vai trò">
            <el-option :label="'Quản trị'" :value="1" />
            <el-option :label="'Nhân viên'" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">Thêm nhân viên</el-button>
          <el-button @click="resetForm">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const form = ref({
  employeeName: '',
  username: '',
  password: '',
  email: '',
  phone: '',
  gender: 1,
  dateOfBirth: '',
  country: '',
  province: '',
  district: '',
  ward: '',
  houseName: '',
  role: 1,
  hireDate: new Date().toISOString().split('T')[0], // mặc định là ngày hôm nay
  salary: 0,
})

const router = useRouter()

const goBack = () => {
  router.push('/employee')
}

const formatSalary = (value) => {
  if (!value) return ''
  return Number(value).toLocaleString('vi-VN')
}

const parseSalary = (value) => {
  return value.replace(/[^\d]/g, '')
}

const rules = {
  employeeName: [{ required: true, message: 'Vui lòng nhập họ tên', trigger: 'blur' }],
  username: [{ required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' }],
  password: [{ required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' }],
  email: [{ type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }],
  phone: [{ required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' }],
  role: [{ required: true, message: 'Vui lòng chọn vai trò', trigger: 'change' }],
  gender: [{ required: true, message: 'Vui lòng chọn giới tính', trigger: 'change' }],
  hireDate: [{ required: true, message: 'Vui lòng chọn ngày tuyển dụng', trigger: 'change' }],
  salary: [
    { required: true, message: 'Vui lòng nhập lương', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const parsed = parseInt(value.toString().replace(/[^\d]/g, ''), 10)
        if (isNaN(parsed) || parsed < 0) {
          callback(new Error('Lương phải là số và lớn hơn hoặc bằng 0'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const employeeForm = ref(null)

const submitForm = () => {
  employeeForm.value.validate(async (valid) => {
    if (valid) {
      try {
        await ElMessageBox.confirm('Bạn có chắc chắn muốn thêm nhân viên này?', 'Xác nhận', {
          confirmButtonText: 'Xác nhận',
          cancelButtonText: 'Hủy',
          type: 'warning',
        })
        await axios.post('http://localhost:8080/api/admin/employees', form.value)
        ElMessage.success('Thêm nhân viên thành công!')
        resetForm()
      } catch (error) {
        console.error(error)
        ElMessage.error('Lỗi khi thêm nhân viên.')
      }
    }
  })
}

const resetForm = () => {
  employeeForm.value.resetFields()
}
</script>

<style scoped>
.add-employee {
  max-width: 800px;
  margin: auto;
}
</style>
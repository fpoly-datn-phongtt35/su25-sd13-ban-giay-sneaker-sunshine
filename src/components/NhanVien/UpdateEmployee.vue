<template>
  <div class="update-employee p-4">
    <el-card shadow="hover">
      <el-button type="info" icon="ArrowLeft" @click="goBack" class="mb-4"> Quay lại </el-button>
      <h2 class="mb-4">
        <el-icon><User /></el-icon> Cập nhật nhân viên
      </h2>

      <el-form :model="employee" label-width="120px">
        <el-form-item label="Họ tên">
          <el-input v-model="employee.employeeName" />
        </el-form-item>

        <el-form-item label="Tên đăng nhập">
          <el-input v-model="employee.username" />
        </el-form-item>

        <el-form-item label="Mật khẩu">
          <el-input v-model="employee.password" type="password" show-password />
        </el-form-item>

        <el-form-item label="Email">
          <el-input v-model="employee.email" />
        </el-form-item>

        <el-form-item label="Số điện thoại">
          <el-input v-model="employee.phone" />
        </el-form-item>

        <el-form-item label="Giới tính">
          <el-select v-model="employee.gender" placeholder="Chọn giới tính">
            <el-option label="Nam" :value="1" />
            <el-option label="Nữ" :value="0" />
            <el-option label="Khác" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="Ngày sinh">
          <el-date-picker
            v-model="employee.dateOfBirth"
            type="date"
            placeholder="Chọn ngày sinh"
            format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="Ngày vào làm">
          <el-date-picker
            v-model="employee.hireDate"
            type="date"
            placeholder="Chọn ngày vào làm"
            format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="Lương">
          <el-input
            v-model="employee.salary"
            placeholder="Nhập lương"
            :formatter="formatSalary"
            :parser="parseSalary"
          />
        </el-form-item>

        <el-form-item label="Quốc gia">
          <el-input v-model="employee.country" />
        </el-form-item>

        <el-form-item label="Tỉnh/TP">
          <el-input v-model="employee.province" />
        </el-form-item>

        <el-form-item label="Quận/Huyện">
          <el-input v-model="employee.district" />
        </el-form-item>

        <el-form-item label="Phường/Xã">
          <el-input v-model="employee.ward" />
        </el-form-item>

        <el-form-item label="Số nhà">
          <el-input v-model="employee.houseName" />
        </el-form-item>

        <el-form-item label="Vai trò">
          <el-select v-model="employee.role" placeholder="Chọn vai trò">
            <el-option label="Quản trị" :value="1" />
            <el-option label="Nhân viên" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="update">Cập nhật</el-button>
          <el-button @click="resetForm" type="warning">Đặt lại</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const employeeId = route.params.id

const employee = ref({
  employeeName: '',
  username: '',
  password: '',
  email: '',
  phone: '',
  gender: 1,
  dateOfBirth: '',
  hireDate: '',
  salary: '',
  country: '',
  province: '',
  district: '',
  ward: '',
  houseName: '',
  role: 1,
})

const originalEmployee = ref({})

const fetchEmployee = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/admin/employees/${employeeId}`)
    employee.value = res.data
    originalEmployee.value = { ...res.data }
  } catch (error) {
    ElMessage.error('Không thể tải dữ liệu nhân viên.')
  }
}

const formatSalary = (value) => {
  if (!value) return ''
  return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

// Bỏ dấu chấm khi submit
const parseSalary = (value) => {
  return value.replace(/\./g, '')
}

const update = async () => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn cập nhật thông tin nhân viên này?',
      'Xác nhận',
      {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning',
      },
    )

    await axios.put(`http://localhost:8080/api/admin/employees/${employeeId}`, employee.value)
    ElMessage.success('Cập nhật nhân viên thành công!')
    router.push('/employee')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Cập nhật thất bại.')
    } else {
      ElMessage.info('Đã hủy cập nhật.')
    }
  }
}

const resetForm = () => {
  employee.value = { ...originalEmployee.value }
  ElMessage.success('Form đã được đặt lại.')
}

const goBack = () => {
  router.push('/employee')
}

onMounted(fetchEmployee)
</script>

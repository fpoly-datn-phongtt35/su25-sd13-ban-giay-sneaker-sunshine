<template>
  <div class="employee-list p-6">
    <el-card shadow="always" class="rounded-2xl shadow-md">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-semibold flex items-center gap-2">
          <el-icon :size="24" class="text-blue-500"><Avatar /></el-icon>
          Danh sách nhân viên
        </h2>
        <el-button type="primary" round plain @click="addEmployee">
          <el-icon :size="16" class="mr-1"><CirclePlus /></el-icon>
          Thêm nhân viên
        </el-button>
      </div>

      <el-table
        :data="employees"
        style="width: 100%"
        stripe
        border
        highlight-current-row
        class="rounded-lg"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="employeeCode" label="Mã NV" />
        <el-table-column prop="employeeName" label="Họ tên" />
        <el-table-column prop="username" label="Tên đăng nhập" />
        <el-table-column prop="password" label="Mật khẩu" />
        <el-table-column prop="email" label="Email" />
        <el-table-column prop="phone" label="Điện thoại" />
        <el-table-column label="Giới tính">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? 'success' : row.gender === 0 ? 'danger' : 'info'" effect="plain">
              {{ row.gender === 1 ? 'Nam' : row.gender === 0 ? 'Nữ' : 'Khác' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Ngày sinh">
          <template #default="{ row }">{{ formatDate(row.dateOfBirth) }}</template>
        </el-table-column>
        <el-table-column prop="country" label="Quốc gia" />
        <el-table-column prop="province" label="Tỉnh/TP" />
        <el-table-column prop="district" label="Quận/Huyện" />
        <el-table-column prop="ward" label="Phường/Xã" />
        <el-table-column prop="houseName" label="Số nhà" />
        <el-table-column prop="createdBy" label="Người tạo" />
        <el-table-column label="Ngày tạo">
          <template #default="{ row }">{{ formatDate(row.createdDate) }}</template>
        </el-table-column>
        <el-table-column label="Vai trò">
          <template #default="{ row }">
            <el-tag :type="row.role === 0 ? 'warning' : 'primary'" effect="plain">
              {{ mapRole(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- Hành động -->
        <el-table-column label="Hành động" width="160">
          <template #default="{ row }">
            <div class="flex gap-2">
              <el-button
                type="primary"
                size="small"
                @click="updateEmployee(row.id)"
                circle
              >
                <el-icon :size="16"><EditPen /></el-icon>
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="deleteEmployee(row.id)"
                circle
              >
                <el-icon :size="16"><DeleteFilled /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-center mt-6">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="size"
          :total="totalElements"
          layout="prev, pager, next"
          @current-change="handlePageChange"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, CirclePlus, EditPen, DeleteFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const employees = ref([])
const currentPage = ref(1)
const size = ref(10)
const totalElements = ref(0)

const fetchEmployees = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/employees', {
      params: {
        page: currentPage.value - 1,
        size: size.value
      }
    })
    employees.value = response.data.content
    totalElements.value = response.data.totalElements
  } catch (error) {
    console.error(error)
    ElMessage.error('Không thể tải dữ liệu nhân viên.')
  }
}

const handlePageChange = (val) => {
  currentPage.value = val
  fetchEmployees()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('vi-VN')
}

const mapRole = (role) => {
  switch (role) {
    case 0: return 'Nhân viên'
    case 1: return 'Quản trị'
    default: return 'Khác'
  }
}

const addEmployee = () => {
  router.push('/employee/add')
}

const updateEmployee = (id) => {
  router.push(`/employee/update/${id}`)
}

const deleteEmployee = async (id) => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn xóa nhân viên này không?',
      'Xác nhận',
      {
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }
    )
    await axios.delete(`http://localhost:8080/api/admin/employees/${id}`)
    ElMessage.success('Xóa nhân viên thành công.')
    fetchEmployees()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Xóa nhân viên thất bại.')
    }
  }
}

onMounted(fetchEmployees)
</script>

<style scoped>
.employee-list {
  max-width: 100%;
}

.el-card {
  border-radius: 12px;
}
</style>
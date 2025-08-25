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
        :loading="loading"
        style="width: 100%"
        stripe
        border
        highlight-current-row
        class="rounded-lg"
        element-loading-text="Đang tải dữ liệu..."
      >
        <el-table-column label="STT" width="80" align="center">
          <template #default="{ $index }">
            {{ pageStartIndex + $index + 1 }}
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="employeeCode" label="Mã NV" min-width="120" />
        <el-table-column prop="employeeName" label="Họ tên" min-width="160" show-overflow-tooltip />
        <el-table-column prop="username" label="Tên đăng nhập" min-width="140" show-overflow-tooltip />
        <el-table-column prop="password" label="Mật khẩu" min-width="140" show-overflow-tooltip />
        <el-table-column prop="email" label="Email" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="Điện thoại" min-width="130" />
        <el-table-column label="Giới tính" width="120">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? 'success' : row.gender === 0 ? 'danger' : 'info'" effect="plain">
              {{ row.gender === 1 ? 'Nam' : row.gender === 0 ? 'Nữ' : 'Khác' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Ngày sinh" width="140" align="center">
          <template #default="{ row }">{{ formatDate(row.dateOfBirth) }}</template>
        </el-table-column>
        <el-table-column label="Ngày tạo" width="140" align="center">
          <template #default="{ row }">{{ formatDate(row.createdDate) }}</template>
        </el-table-column>
        <el-table-column label="Vai trò" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 0 ? 'warning' : 'primary'" effect="plain">
              {{ mapRole(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Hành động" width="160" fixed="right">
          <template #default="{ row }">
            <div class="flex gap-2 justify-center">
              <el-button type="primary" size="small" @click="updateEmployee(row.id)" circle>
                <el-icon :size="16"><EditPen /></el-icon>
              </el-button>
              <el-button type="danger" size="small" @click="deleteEmployee(row.id)" circle>
                <el-icon :size="16"><DeleteFilled /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mt-6">
        <div class="text-gray-600">
          Tổng: <strong>{{ totalElements }}</strong> bản ghi
        </div>

        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50, 100]"
          :total="totalElements"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import apiClient from '@/utils/axiosInstance'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, CirclePlus, EditPen, DeleteFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// ====== State ======
const employees = ref([])
const loading = ref(false)

const currentPage = ref(1)        // 1-based cho UI
const pageSize = ref(10)          // số bản ghi/trang
const totalElements = ref(0)      // tổng số bản ghi từ backend

// Tính chỉ số bắt đầu của trang hiện tại (để hiển thị STT)
const pageStartIndex = computed(() => (currentPage.value - 1) * pageSize.value)

// ====== API ======
const fetchEmployees = async () => {
  loading.value = true
  try {
    const res = await apiClient.get('/admin/employees', {
      params: {
        page: currentPage.value - 1, // backend 0-based
        size: pageSize.value
      }
    })

    // Chuẩn hoá dữ liệu về dạng Spring Data Page
    const data = res?.data || {}
    console.log('data: ',data)
    employees.value = data.content ?? []
    totalElements.value = Number(data.page.totalElements ?? 0)

    if (employees.value.length === 0 && currentPage.value > 1) {
      currentPage.value = currentPage.value - 1
      await fetchEmployees()
    }
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu nhân viên:', error)
    if (error?.response?.status === 403) {
      router.push('/error')
    } else {
      ElMessage.error('Không thể tải dữ liệu nhân viên. Vui lòng thử lại sau.')
    }
    employees.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// ====== Pagination handlers ======
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchEmployees()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // quay về trang 1 khi đổi page size
  fetchEmployees()
}

// ====== Utils ======
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return d.toLocaleDateString('vi-VN')
}

const mapRole = (role) => {
  switch (role) {
    case 0: return 'Nhân viên'
    case 1: return 'Quản trị'
    default: return 'Khác'
  }
}

// ====== Actions ======
const addEmployee = () => router.push('/employee/add')
const updateEmployee = (id) => router.push(`/employee/update/${id}`)

const deleteEmployee = async (id) => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn xóa nhân viên này không?',
      'Xác nhận',
      { confirmButtonText: 'Xóa', cancelButtonText: 'Hủy', type: 'warning' }
    )
    await apiClient.delete(`/admin/employees/${id}`)
    ElMessage.success('Xóa nhân viên thành công.')
    // Sau khi xoá: nạp lại trang hiện tại (logic trong fetchEmployees tự xử lý lùi trang nếu cần)
    fetchEmployees()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác xóa.')
    } else {
      console.error('Lỗi khi xóa nhân viên:', error)
      ElMessage.error('Xóa nhân viên thất bại. Vui lòng thử lại.')
    }
  }
}

// ====== Lifecycle ======
onMounted(fetchEmployees)
</script>

<style scoped>
.employee-list {
  max-width: 1400px;
  margin: 0 auto;
}

.el-card {
  border-radius: 12px;
}
</style>

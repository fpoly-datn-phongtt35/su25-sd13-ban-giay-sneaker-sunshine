<template>
  <div class="employee-list p-6">
    <el-card shadow="always" class="rounded-2xl shadow-md">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-semibold flex items-center gap-2">
          <el-icon :size="24" class="text-blue-500"><Avatar /></el-icon>
          Danh sách nhân viên
        </h2>
        <div class="flex gap-2">
          <el-button type="success" plain round @click="exportExcel" :loading="exporting">
            Xuất Excel
          </el-button>
          <el-button type="primary" round plain @click="addEmployee">
            <el-icon :size="16" class="mr-1"><CirclePlus /></el-icon>
            Thêm nhân viên
          </el-button>
        </div>
      </div>

      <!-- Search filters -->
      <el-form :inline="true" class="mb-4">
        <el-form-item label="Mã NV">
          <el-input v-model.trim="filters.employeeCode" placeholder="VD: NV001" clearable @keyup.enter.native="handleSearch"/>
        </el-form-item>
        <el-form-item label="Họ tên">
          <el-input v-model.trim="filters.employeeName" placeholder="VD: Nguyễn Văn A" clearable @keyup.enter.native="handleSearch"/>
        </el-form-item>
        <el-form-item label="Email">
          <el-input v-model.trim="filters.email" placeholder="VD: a@gmail.com" clearable @keyup.enter.native="handleSearch"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Tìm kiếm</el-button>
          <el-button @click="handleReset" :disabled="!isSearching && !hasAnyFilter">Xóa lọc</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="tableData"
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
            {{ isSearching ? $index + 1 : pageStartIndex + $index + 1 }}
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

      <!-- Pagination: ẩn khi đang ở chế độ search -->
      <div v-if="!isSearching" class="flex flex-col md:flex-row md:items-center justify-between gap-4 mt-6">
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

      <!-- Thông tin kết quả tìm kiếm -->
      <div v-else class="flex items-center justify-between mt-4 text-gray-600">
        <div>
          Kết quả tìm thấy: <strong>{{ employees.length }}</strong> bản ghi
        </div>
        <el-button link type="primary" @click="handleReset">Quay lại danh sách phân trang</el-button>
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
const employees = ref([])          // dữ liệu hiện trong bảng
const loading = ref(false)
const exporting = ref(false)

const currentPage = ref(1)        // 1-based cho UI
const pageSize = ref(10)          // số bản ghi/trang
const totalElements = ref(0)      // tổng số bản ghi từ backend

const isSearching = ref(false)    // đang ở chế độ search (dùng /search, không phân trang)
const filters = ref({
  employeeCode: '',
  employeeName: '',
  email: ''
})

const hasAnyFilter = computed(() =>
  !!(filters.value.employeeCode || filters.value.employeeName || filters.value.email)
)

// Tính chỉ số bắt đầu của trang hiện tại (để hiển thị STT)
const pageStartIndex = computed(() => (currentPage.value - 1) * pageSize.value)

// Dữ liệu đưa vào bảng: nếu đang search thì dùng employees trực tiếp,
// nếu không thì cũng dùng employees (đã là data trang hiện tại)
const tableData = computed(() => employees.value)

// ====== API: danh sách mặc định (phân trang) ======
const fetchEmployees = async () => {
  loading.value = true
  isSearching.value = false
  try {
    const res = await apiClient.get('/admin/employees', {
      params: {
        page: currentPage.value - 1, // backend 0-based
        size: pageSize.value
      }
    })

    const data = res?.data || {}
    employees.value = data.content ?? []
    totalElements.value = Number(data.page?.totalElements ?? 0)

    if (employees.value.length === 0 && currentPage.value > 1 && totalElements.value > 0) {
      // nếu trang rỗng -> lùi 1 trang và gọi lại
      currentPage.value = Math.max(1, currentPage.value - 1)
      await fetchEmployees()
    }
  } catch (error) {
    if (error?.response?.status === 403) {
      router.push('/error')
      return
    }
    console.error('Lỗi khi tải dữ liệu nhân viên:', error)
    ElMessage.error('Không thể tải dữ liệu nhân viên. Vui lòng thử lại sau.')
    employees.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// ====== API: tìm kiếm theo Mã NV / Họ tên / Email (không phân trang BE) ======
const handleSearch = async () => {
  // Nếu không có filter -> quay về danh sách phân trang
  if (!hasAnyFilter.value) {
    currentPage.value = 1
    await fetchEmployees()
    return
  }

  loading.value = true
  try {
    const res = await apiClient.get('/admin/employees/search', {
      params: {
        employeeCode: filters.value.employeeCode || null,
        employeeName: filters.value.employeeName || null,
        email: filters.value.email || null
      }
    })
    employees.value = res?.data ?? []
    isSearching.value = true
  } catch (error) {
    if (error?.response?.status === 403) {
      router.push('/error')
      return
    }
    console.error('Lỗi tìm kiếm nhân viên:', error)
    ElMessage.error('Tìm kiếm thất bại. Vui lòng thử lại.')
    employees.value = []
    isSearching.value = true
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  filters.value = { employeeCode: '', employeeName: '', email: '' }
  currentPage.value = 1
  await fetchEmployees()
}

// ====== Export Excel theo filter hiện tại ======
const exportExcel = async () => {
  try {
    exporting.value = true
    const res = await apiClient.get('/admin/employees/export-excel', {
      params: {
        employeeCode: filters.value.employeeCode || null,
        employeeName: filters.value.employeeName || null,
        email: filters.value.email || null
      },
      responseType: 'blob'
    })

    // Tạo file tải về
    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    const ts = new Date().toISOString().replace(/[:.]/g, '-')
    a.href = url
    a.download = `employees-${ts}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)

    ElMessage.success('Xuất Excel thành công!')
  } catch (error) {
    if (error?.response?.status === 403) {
      router.push('/error')
      return
    }
    console.error('Lỗi xuất Excel:', error)
    ElMessage.error('Xuất Excel thất bại.')
  } finally {
    exporting.value = false
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
    if (isSearching.value) {
      // nếu đang ở chế độ tìm kiếm -> tìm lại theo filter hiện tại
      await handleSearch()
    } else {
      await fetchEmployees()
    }
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác xóa.')
    } else if (error?.response?.status === 403) {
      router.push('/error')
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

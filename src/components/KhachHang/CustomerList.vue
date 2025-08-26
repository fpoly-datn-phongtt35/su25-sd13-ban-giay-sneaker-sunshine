<template>
  <div class="customer-list">
    <el-card shadow="always" class="card">
      <!-- Header -->
      <template #header>
        <div class="card-header">
          <div class="title-wrap">
            <h3 class="title">Danh sách khách hàng</h3>
          </div>
          <el-button type="success" :icon="Plus" @click="goToAddCustomer">Thêm khách hàng</el-button>
        </div>
      </template>

      <!-- Search / Filters -->
      <el-form :inline="true" class="search-bar" @submit.prevent>
        <el-form-item label="Từ khóa">
          <el-input
            v-model="searchKeyword"
            placeholder="Tên, mã, email, SĐT…"
            clearable
            :prefix-icon="Search"
            @clear="onImmediateSearch"
            @input="onDebouncedSearch"
            class="search-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onImmediateSearch">Tìm kiếm</el-button>
          <el-button :icon="Refresh" @click="resetSearch">Reset</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table
        :data="customers"
        v-loading="loading"
        element-loading-text="Đang tải dữ liệu…"
        empty-text="Chưa có khách hàng."
        border
        stripe
        class="table"
        :row-class-name="tableRowClassName"
        size="default"
      >
        <el-table-column type="index" label="#" width="64" :index="indexMethod" />

        <el-table-column prop="customerName" label="Tên khách hàng" min-width="200" sortable show-overflow-tooltip />

        <el-table-column prop="email" label="Email" min-width="220" show-overflow-tooltip />

        <el-table-column prop="phone" label="Số điện thoại" width="150" />

        <el-table-column label="Ngày tạo" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createdDate) }}
          </template>
        </el-table-column>

        <el-table-column label="Cảnh báo" min-width="260">
          <template #default="{ row }">
            <div class="row-warning">
              <el-tag v-if="row.isBlacklisted" type="danger" effect="light" size="small" class="mr-1">
                Đang bị cấm
              </el-tag>
              <template v-if="row.blacklistReason && row.blacklistReason.trim()">
                <span class="reason">Lý do: {{ row.blacklistReason }}</span>
                <el-tag v-if="row.blacklistEndDate" type="danger" effect="plain" size="small" class="ml-1">
                  Đến {{ formatDate(row.blacklistEndDate) }}
                </el-tag>
              </template>
              <span v-else class="reason-muted">Không có lý do cụ thể</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Hành động" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-tooltip content="Sửa" :show-after="300">
              <el-button type="primary" :icon="Edit" size="small" circle @click="goToEditCustomer(row.id)" />
            </el-tooltip>

            <el-tooltip content="Xóa" :show-after="300">
              <el-button type="danger" :icon="Delete" size="small" circle @click="confirmDeleteCustomer(row.id)" />
            </el-tooltip>

            <template v-if="!row.isBlacklisted">
              <el-tooltip content="Cấm khách hàng" :show-after="300">
                <el-button
                  type="warning"
                  :icon="CircleClose"
                  size="small"
                  circle
                  @click="confirmBlacklistCustomer(row.id)"
                />
              </el-tooltip>
            </template>
            <template v-else>
              <el-tooltip content="Bỏ cấm khách hàng" :show-after="300">
                <el-button
                  type="success"
                  :icon="CircleCheck"
                  size="small"
                  circle
                  @click="confirmUnblacklistCustomer(row.id)"
                />
              </el-tooltip>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="size"
          :page-sizes="[5, 10, 20, 50]"
          :total="totalElements"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import apiClient from '@/utils/axiosInstance'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, CircleClose, CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()

// State
const customers = ref([])
const currentPage = ref(1)
const size = ref(10)
const totalElements = ref(0)
const loading = ref(false)
const searchKeyword = ref('')

// Debounce timer
let typingTimer = null
const DEBOUNCE_MS = 350

// API
const fetchCustomers = async () => {
  loading.value = true
  try {
    const res = await apiClient.get('/admin/customers/phan-trang', {
      params: {
        page: currentPage.value - 1,
        size: size.value,
        keyword: searchKeyword.value?.trim() || null,
      },
    })
    const data = res.data || {}
    customers.value = data.content || []
    totalElements.value = data?.page?.totalElements ?? 0

    // Điều chỉnh nếu trang trống
    if (customers.value.length === 0 && currentPage.value > 1 && totalElements.value > 0) {
      currentPage.value = Math.max(1, Math.ceil(totalElements.value / size.value))
      await fetchCustomers()
    } else if (totalElements.value === 0) {
      currentPage.value = 1
    }
  } catch (err) {
    if (err?.response?.status === 403) {
      router.push('/error')
      return
    }
    ElMessage.error('Không thể tải dữ liệu khách hàng. Vui lòng thử lại sau.')
    customers.value = []
    totalElements.value = 0
    currentPage.value = 1
  } finally {
    loading.value = false
  }
}

// Pagination
const handleSizeChange = (newSize) => {
  size.value = newSize
  currentPage.value = 1
  fetchCustomers()
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  fetchCustomers()
}

// Utilities
const indexMethod = (index) => (currentPage.value - 1) * size.value + index + 1

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return isNaN(d.getTime()) ? dateStr : d.toLocaleDateString('vi-VN')
}

const tableRowClassName = ({ row }) => (row.isBlacklisted ? 'danger-row' : '')

// Nav
const goToAddCustomer = () => router.push({ name: 'AddCustomer' })
const goToEditCustomer = (id) => router.push({ name: 'UpdateCustomer', params: { id } })

// Search
const onImmediateSearch = () => {
  if (typingTimer) clearTimeout(typingTimer)
  currentPage.value = 1
  fetchCustomers()
}
const onDebouncedSearch = () => {
  if (typingTimer) clearTimeout(typingTimer)
  typingTimer = setTimeout(() => {
    currentPage.value = 1
    fetchCustomers()
  }, DEBOUNCE_MS)
}

const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  fetchCustomers()
}

// Delete
const confirmDeleteCustomer = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc chắn muốn xóa khách hàng này?', 'Cảnh báo', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning',
    })
    await apiClient.delete(`/admin/customers/${id}`)
    ElMessage.success('Xóa khách hàng thành công!')
    if (customers.value.length === 1 && currentPage.value > 1) currentPage.value--
    await fetchCustomers()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác xóa.')
    } else {
      ElMessage.error('Không thể xóa khách hàng. Vui lòng thử lại.')
    }
  }
}

// Blacklist
const confirmBlacklistCustomer = async (id) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('Nhập lý do cấm khách hàng:', 'Cấm khách hàng', {
      confirmButtonText: 'Cấm',
      cancelButtonText: 'Hủy',
      inputType: 'textarea',
      inputPlaceholder: 'Ví dụ: Vi phạm chính sách…',
      inputValidator: (v) => (!!v && v.trim() !== '') || 'Lý do không được để trống.',
      showClose: false,
    })
    const { value: duration } = await ElMessageBox.prompt(
      'Nhập số ngày cấm (để trống hoặc 0 nếu vĩnh viễn):',
      'Thời gian cấm',
      {
        confirmButtonText: 'Cấm',
        cancelButtonText: 'Hủy',
        inputType: 'number',
        inputPlaceholder: 'Ví dụ: 30',
        inputValidator: (v) => {
          if (v === null || String(v).trim() === '') return true
          const num = parseInt(v, 10)
          return (!isNaN(num) && num >= 0) || 'Số ngày không hợp lệ.'
        },
        showClose: false,
      }
    )
    const durationInDays = duration === null || String(duration).trim() === '' ? null : parseInt(duration, 10)
    await apiClient.put(`/admin/customers/${id}/blacklist`, { reason, durationInDays })
    ElMessage.success('Đã cấm khách hàng!')
    await fetchCustomers()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác cấm.')
    } else {
      ElMessage.error('Không thể cấm khách hàng. Vui lòng thử lại.')
    }
  }
}

const confirmUnblacklistCustomer = async (id) => {
  try {
    await ElMessageBox.confirm('Bỏ cấm khách hàng này?', 'Xác nhận', {
      confirmButtonText: 'Bỏ cấm',
      cancelButtonText: 'Hủy',
      type: 'info',
    })
    await apiClient.put(`/admin/customers/${id}/unblacklist`)
    ElMessage.success('Đã bỏ cấm!')
    await fetchCustomers()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác.')
    } else {
      ElMessage.error('Không thể bỏ cấm. Vui lòng thử lại.')
    }
  }
}

// Lifecycle
onMounted(fetchCustomers)
</script>

<style scoped>
.customer-list {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 16px;
}

.card {
  border-radius: 12px;
}

/* Header */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.title-wrap {
  display: flex;
  flex-direction: column;
}
.title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
.subtitle {
  margin: 2px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* Search bar */
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-bottom: 12px;
}
.search-input {
  width: 360px;
  max-width: 100%;
}

/* Table */
.table :deep(.el-table__header-wrapper th) {
  background: var(--el-fill-color-light);
  font-weight: 600;
  color: var(--el-text-color-regular);
}
.table :deep(.el-table__cell) {
  padding: 10px 8px;
}

/* Row highlight for blacklisted */
.danger-row :deep(td) {
  background-color: var(--el-color-error-light-9) !important;
}
.danger-row:hover :deep(td) {
  background-color: var(--el-color-error-light-8) !important;
}

/* Warning column */
.row-warning {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.mr-1 { margin-right: 6px; }
.ml-1 { margin-left: 6px; }
.reason {
  color: var(--el-color-error);
  font-weight: 500;
}
.reason-muted {
  color: var(--el-text-color-secondary);
}

/* Pagination */
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* Responsive */
@media (max-width: 768px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }
  .title { font-size: 18px; }
}
</style>

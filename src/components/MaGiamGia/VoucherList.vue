<template>
  <div class="voucher-wrapper">
    <div class="card shadow-lg">
      <div class="card-body p-4">
        <div class="search-section mb-4 p-3 bg-light rounded">
          <div class="row g-3">
            <div class="col-md-3 col-sm-6">
              <input
                v-model="searchVoucher.keyword"
                class="form-control form-control-sm"
                placeholder="Tìm mã, tên voucher, tên sản phẩm"
              />
            </div>
            <div class="col-md-2 col-sm-6">
              <select v-model="searchVoucher.status" class="form-select form-select-sm">
                <option :value="null">Tất cả trạng thái</option>
                <option :value="1">Đang diễn ra</option>
                <option :value="0">Ngừng hoạt động</option>
                <option :value="2">Sắp diễn ra</option>
              </select>
            </div>
            <div class="col-md-2 col-sm-6">
              <select v-model="searchVoucher.orderType" class="form-select form-select-sm">
                <option :value="null">Tất cả loại đơn hàng</option>
                <option :value="1">Tại quầy</option>
                <option :value="0">Online</option>
              </select>
            </div>
            <div class="col-md-2 col-sm-6">
              <select v-model="searchVoucher.voucherType" class="form-select form-select-sm">
                <option :value="null">Tất cả loại</option>
                <option :value="1">Công khai</option>
                <option :value="0">Cá nhân</option>
              </select>
            </div>
            <div class="col-md-3 col-sm-12">
              <select
                id="category"
                v-model="searchVoucher.categoryIds"
                class="form-select form-select-sm"
              >
                <option :value="null">-- Chọn danh mục --</option>
                <option v-for="cat in categoryList" :key="cat.id" :value="cat.id">
                  {{ cat.categoryName }}
                </option>
              </select>
            </div>
            <div class="col-md-3 col-sm-12 d-flex gap-2 align-items-end">
              <el-button type="primary" size="small" @click="fetchVoucher(0)">Tìm</el-button>
              <el-button type="default" size="small" @click="resetForm">Xóa bộ lọc</el-button>
            </div>
          </div>
        </div>

        <div class="voucher-header d-flex align-items-center mb-4">
          <el-icon class="me-2 text-primary" size="24"><ticket /></el-icon>
          <h2 class="flex-grow-1 mb-0 fs-5 fw-bold text-dark">Danh sách Voucher</h2>
          <el-button type="primary" :icon="Plus" @click="onAddVoucher">Thêm Voucher</el-button>
          <el-button type="success" size="small" @click="exportExcel">Xuất Excel</el-button>
        </div>

        <div class="table-responsive">
          <table class="table table-hover table-sm table-bordered align-middle">
            <thead class="table-light">
              <tr>
                <th scope="col" class="text-center" style="width: 50px">
                  <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
                </th>
                <th scope="col" class="text-center" style="width: 80px">Mã</th>
                <th scope="col" class="text-wrap">Tên</th>
                <th scope="col" class="text-wrap">Sản phẩm</th>
                <th scope="col" class="text-center" style="width: 70px">Giảm (%)</th>
                <th scope="col" class="text-end" style="width: 90px">Tiền giảm</th>
                <th scope="col" class="text-end" style="width: 90px">Tối thiểu</th>
                <th scope="col" class="text-end" style="width: 90px">Tối đa</th>
                <th scope="col" class="text-center" style="width: 100px">Bắt đầu</th>
                <th scope="col" class="text-center" style="width: 100px">Kết thúc</th>
                <th scope="col" class="text-center" style="width: 90px">Trạng thái</th>
                <th scope="col" class="text-center d-none d-md-table-cell" style="width: 90px">KH</th>
                <th scope="col" class="text-center d-none d-md-table-cell" style="width: 90px">NV</th>
                <th scope="col" class="text-center" style="width: 80px">Loại đơn hàng</th>
                <th scope="col" class="text-center" style="width: 80px">Loại</th>
                <th scope="col" class="text-center" style="width: 80px">Số lượng</th> <th scope="col" class="text-center d-none d-md-table-cell" style="width: 100px">Danh mục</th>
                <th scope="col" class="text-center" style="width: 100px">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in vouchers" :key="row.id">
                <td class="text-center">
                  <input type="checkbox" v-model="selectedVouchers" :value="row.id" />
                </td>
                <td class="text-center">{{ row.voucherCode || '-' }}</td>
                <td class="text-wrap">{{ row.voucherName || '-' }}</td>
                <td class="text-wrap">{{ row.productName || '-' }}</td>
                <td class="text-center">{{ formatDiscountPercent(row) }}</td>
                <td class="text-end">{{ formatDiscountAmount(row) }}</td>
                <td class="text-end">{{ formatCurrency(row, null, row.minOrderValue) }}</td>
                <td class="text-end">{{ formatCurrency(row, null, row.maxDiscountValue) }}</td>
                <td class="text-center">{{ formatDate(row, null, row.startDate) }}</td>
                <td class="text-center">{{ formatDate(row, null, row.endDate) }}</td>
                <td class="text-center">
                  <span :class="{
                    'badge bg-success': row.status === 1,
                    'badge bg-danger': row.status === 0,
                    'badge bg-warning': row.status === 2
                  }">
                    {{ formatStatus(row) }}
                  </span>
                </td>
                <td class="text-center d-none d-md-table-cell">{{ row.customerName || '-' }}</td>
                <td class="text-center d-none d-md-table-cell">{{ row.employeeName || '-' }}</td>
                <td class="text-center">{{ formatOrderType(row) }}</td>
                <td class="text-center">{{ formatVoucherType(row) }}</td>
                <td class="text-center">{{ row.quantity || '-' }}</td> <td class="text-center d-none d-md-table-cell">{{ row.categoryName || '-' }}</td>
                <td class="text-center">
                  <el-tooltip content="Sửa" placement="top">
                    <el-button
                      type="primary"
                      :icon="Edit"
                      size="small"
                      circle
                      @click="onEditVoucher(row)"
                    />
                  </el-tooltip>
                  <el-tooltip content="Xóa" placement="top">
                    <el-button
                      type="danger"
                      :icon="Delete"
                      size="small"
                      circle
                      @click="onDeleteVoucher(row)"
                    />
                  </el-tooltip>
                </td>
              </tr>
              <tr v-if="!vouchers.length">
                <td colspan="18" class="text-center text-muted">Không có dữ liệu</td> </tr>
            </tbody>
          </table>
        </div>

        <nav aria-label="Page navigation" class="mt-4" v-if="totalPages > 1">
          <ul class="pagination pagination-sm justify-content-center">
            <li class="page-item" :class="{ disabled: !hasPrevious }">
              <button class="page-link" @click="fetchVoucher(page - 1)" :disabled="!hasPrevious">Trước</button>
            </li>

            <li
              class="page-item"
              v-for="p in totalPages"
              :key="p"
              :class="{ active: page === p - 1 }"
            >
              <button class="page-link" @click="fetchVoucher(p - 1)">{{ p }}</button>
            </li>

            <li class="page-item" :class="{ disabled: !hasNext }">
              <button class="page-link" @click="fetchVoucher(page + 1)" :disabled="!hasNext">Sau</button>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessageBox, ElButton, ElIcon, ElMessage } from 'element-plus'
import { Ticket, Edit, Delete, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const vouchers = ref([])
const totalPages = ref(0)
const totalElements = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)
const page = ref(0)
const size = ref(8)
const categoryList = ref([])
const selectedVouchers = ref([])
const selectAll = ref(false)

const router = useRouter()

const searchVoucher = ref({
  keyword: null,
  status: null,
  orderType: null,
  voucherType: null,
  categoryIds: null,
  productId: null,
})

// Reset form tìm kiếm
const resetForm = async () => {
  searchVoucher.value = {
    keyword: null,
    status: null,
    orderType: null,
    voucherType: null,
    categoryIds: null,
    productId: null,
  }
  page.value = 0
  selectedVouchers.value = []
  selectAll.value = false
  await fetchVoucher()
}

// Lấy danh mục
const fetchCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/categories/hien-thi')
    categoryList.value = response.data || []
  } catch (error) {
    console.error('Lỗi lấy danh mục:', error)
    ElMessage.error(`Lấy danh mục thất bại: ${error.message}`)
    categoryList.value = []
  }
}

// Lấy danh sách voucher
const fetchVoucher = async (newPage = 0) => {
  try {
    const searchParams = {
      keyword: searchVoucher.value.keyword?.trim() || null,
      status:
        searchVoucher.value.status !== '' && searchVoucher.value.status != null
          ? Number(searchVoucher.value.status)
          : null,
      orderType:
        searchVoucher.value.orderType !== '' && searchVoucher.value.orderType != null
          ? Number(searchVoucher.value.orderType)
          : null,
      voucherType:
        searchVoucher.value.voucherType !== '' && searchVoucher.value.voucherType != null
          ? Number(searchVoucher.value.voucherType)
          : null,
      categoryId: searchVoucher.value.categoryIds ?? null,
      page: newPage,
      size: size.value,
    }

    const requestBody = Object.keys(searchParams).every(
      (key) => searchParams[key] === null || searchParams[key].length === 0,
    )
      ? {}
      : searchParams

    const response = await axios.post('http://localhost:8080/api/admin/vouchers/search', requestBody)
    vouchers.value = response.data.data || []
    totalPages.value = response.data.pagination?.totalPages || 0
    totalElements.value = response.data.pagination?.totalElements || 0
    hasNext.value = response.data.pagination?.hasNext || false
    hasPrevious.value = newPage > 0
    page.value = newPage

    // Reset selected vouchers when fetching new page
    selectedVouchers.value = []
    selectAll.value = false

    if (!vouchers.value.length) {
      ElMessage.warning('Không tìm thấy voucher nào!')
    }
  } catch (error) {
    console.error('Lỗi tải danh sách voucher:', error)
    ElMessage.error(`Tải danh sách voucher thất bại: ${error.message}`)
    vouchers.value = []
    totalPages.value = 0
    totalElements.value = 0
    hasGonext.value = false
    hasPrevious.value = false
  }
}

// Chọn tất cả checkbox
const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedVouchers.value = vouchers.value.map(voucher => voucher.id)
  } else {
    selectedVouchers.value = []
  }
}

// Xuất Excel
const exportExcel = async () => {
  if (selectedVouchers.value.length === 0) {
    ElMessage.warning('Vui lòng chọn ít nhất một voucher để xuất!')
    return
  }

  try {
    const response = await axios.post('http://localhost:8080/api/vouchers/vcollect', {
      voucherIds: selectedVouchers.value
    }, {
      responseType: 'blob' // Expect binary data (Excel file)
    })

    // Create a URL for the blob and trigger download
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'vouchers.xlsx') // File name from backend or default
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('Xuất Excel thành công!')
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    ElMessage.error(`Xuất Excel thất bại: ${error.message}`)
  }
}

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchVoucher()])
})

const formatCurrency = (row, column, value) =>
  value ? Number(value).toLocaleString('vi-VN') + ' ₫' : '-'
const formatDate = (row, column, value) => {
  if (!value) return '-'
  const date = new Date(value)
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

const formatStatus = (row) => {
  switch (row.status) {
    case 1:
      return 'Đang diễn ra'
    case 0:
      return 'Ngừng hoạt động'
    case 2:
      return 'Sắp diễn ra'
    default:
      return '-'
  }
}

const formatDiscountPercent = (row) =>
  row.discountPercentage ? `${Number(row.discountPercentage)}%` : '-'
const formatDiscountAmount = (row) =>
  row.discountAmount ? row.discountAmount.toLocaleString('vi-VN') + ' ₫' : '-'
const formatOrderType = (row) =>
  row.orderType === 1 ? 'Tại quầy' : row.orderType === 0 ? 'Online' : '-'
const formatVoucherType = (row) =>
  row.voucherType === 1 ? 'Công khai' : row.voucherType === 0 ? 'Cá nhân' : '-'

const onAddVoucher = () => {
  router.push({ name: 'AddVoucher' })
  ElMessage.success('Chuyển đến trang thêm voucher!')
}

const onEditVoucher = (row) => {
  router.push({ name: 'UpdateVoucher', params: { id: row.id } })
  ElMessage.success('Chuyển đến trang sửa voucher!')
}

const onDeleteVoucher = (row) => {
  ElMessageBox.confirm(`Xác nhận xóa voucher ${row.voucherCode}?`, 'Xác nhận xóa', {
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
    type: 'warning',
  })
    .then(async () => {
      try {
        await axios.delete(`http://localhost:8080/api/admin/vouchers/${row.id}`)
        ElMessage.success('Xóa voucher thành công!')
        await fetchVoucher(page.value)
      } catch (error) {
        console.error('Lỗi khi xóa:', error)
        ElMessage.error(`Xóa voucher thất bại: ${error.message}`)
      }
    })
    .catch(() => {
      ElMessage.info('Đã hủy xóa voucher')
    })
}
</script>

<style scoped>
.voucher-wrapper {
  max-width: 100%;
  padding: 1rem;
  box-sizing: border-box;
}

.card {
  background: #fff;
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.search-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
}

.voucher-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #2c3e50;
}

.table {
  font-size: 0.875rem;
  border-collapse: separate;
  border-spacing: 0;
  border-radius: 10px;
  overflow: hidden;
}

.table th,
.table td {
  padding: 0.75rem;
  vertical-align: middle;
}

.table th {
  background: #e9ecef;
  font-weight: 600;
  color: #34495e;
}

.table tbody tr:hover {
  background: #f1f3f5;
}

.text-wrap {
  white-space: normal !important;
  word-break: break-word;
  max-width: 150px;
}

.table-responsive {
  border-radius: 10px;
  overflow-x: auto;
}

.table-responsive::-webkit-scrollbar {
  height: 8px;
}

.table-responsive::-webkit-scrollbar-thumb {
  background-color: #adb5bd;
  border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-track {
  background: #f1f3f5;
  border-radius: 4px;
}

.form-control-sm,
.form-select-sm {
  border-radius: 6px;
  font-size: 0.875rem;
  padding: 0.5rem;
}

.pagination .page-link {
  border-radius: 5px;
  margin: 0 3px;
  color: #2980b9;
}

.pagination .page-item.active .page-link {
  background-color: #2980b9;
  border-color: #2980b9;
  color: #fff;
}

.badge {
  padding: 0.4em 0.8em;
  border-radius: 4px;
  font-size: 0.75rem;
}

@media (max-width: 768px) {
  .voucher-wrapper {
    padding: 0.5rem;
  }

  .search-section {
    padding: 1rem;
  }

  .voucher-header h2 {
    font-size: 1rem;
  }

  .table {
    font-size: 0.75rem;
  }

  .table th,
  .table td {
    padding: 0.5rem;
  }
}

@media (max-width: 576px) {
  .search-section {
    padding: 0.75rem;
  }

  .form-control-sm,
  .form-select-sm {
    font-size: 0.8rem;
  }

  .table {
    font-size: 0.7rem;
  }

  .table th,
  .table td {
    padding: 0.4rem;
  }
}
</style>
<template>
  <div class="voucher-wrapper p-4">
    <el-card shadow="hover" class="mb-4">
      <div class="row g-3">
        <div class="col-md-4 col-sm-6 mb-2">
          <el-input
            v-model="searchVoucher.keyword"
            placeholder="Tìm mã, tên voucher"
            size="small"
            clearable
          />
        </div>
        <div class="col-md-2 col-sm-6 mb-2">
          <el-select v-model="searchVoucher.status" placeholder="Tất cả trạng thái" size="small" clearable>
            <el-option :label="'Đang diễn ra'" :value="1" />
            <el-option :label="'Ngừng hoạt động'" :value="0" />
            <el-option :label="'Sắp diễn ra'" :value="2" />
          </el-select>
        </div>
        <div class="col-md-2 col-sm-6 mb-2">
          <el-select v-model="searchVoucher.orderType" placeholder="Tất cả loại đơn hàng" size="small" clearable>
            <el-option :label="'Tại quầy'" :value="1" />
            <el-option :label="'Online'" :value="2" />
          </el-select>
        </div>
        <div class="col-md-4 col-sm-12 mb-2 d-flex gap-2">
          <el-button type="primary" size="small" @click="fetchVoucher(0)">Tìm</el-button>
          <el-button type="default" size="small" @click="resetForm">Xóa bộ lọc</el-button>
          <el-button type="success" size="small" @click="exportExcel">Xuất Excel</el-button>
          <el-button type="primary" size="small" @click="onAddVoucher">Thêm Voucher</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover">
      <el-table
        :data="vouchers"
        stripe
        border
        style="width: 100%"
        :row-key="row => row.id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="voucherCode" label="Mã" width="200" />
        <el-table-column prop="voucherName" label="Tên" width="200" />
        <el-table-column
          label="Giảm (%)"
          width="200"
          :formatter="row => formatDiscountPercent(row)"
        />
        <el-table-column
          label="Tiền giảm"
          width="200"
          :formatter="row => formatDiscountAmount(row)"
        />
        <el-table-column
          label="Tối thiểu"
          width="170"
          :formatter="row => formatCurrency(row, null, row.minOrderValue)"
        />
        <el-table-column
          label="Tối đa"
          width="170"
          :formatter="row => formatCurrency(row, null, row.maxDiscountValue)"
        />
        <el-table-column
          label="Bắt đầu"
          width="200"
          :formatter="row => formatDate(row, null, row.startDate)"
        />
        <el-table-column
          label="Kết thúc"
          width="200"
          :formatter="row => formatDate(row, null, row.endDate)"
        />
        <el-table-column label="Thao tác" width="120">
          <template #default="scope">
            <el-tooltip content="Sửa" placement="top">
              <el-button
                type="primary"
                :icon="Edit"
                size="small"
                circle
                @click="onEditVoucher(scope.row)"
              />
            </el-tooltip>
            <el-tooltip content="Xóa" placement="top">
              <el-button
                type="danger"
                :icon="Delete"
                size="small"
                circle
                @click="onDeleteVoucher(scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="d-flex justify-content-end mt-3">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="page + 1"
          :page-size="size"
          :total="totalElements"
          :page-sizes="[5, 10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import apiClient from '@/utils/axiosInstance'
import { ElMessageBox, ElButton, ElIcon, ElMessage } from 'element-plus'
import { Ticket, Edit, Delete, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const vouchers = ref([])
const totalElements = ref(0) // Đã đổi tên từ totalPages sang totalElements
const page = ref(0)
const size = ref(10) // Mặc định 10 phần tử mỗi trang
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
    const response = await apiClient.get('/admin/categories/hien-thi')
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
      status: searchVoucher.value.status ?? null,
      orderType: searchVoucher.value.orderType ?? null,
      voucherType: searchVoucher.value.voucherType ?? null,
      categoryId: searchVoucher.value.categoryIds ?? null,
      page: newPage,
      size: size.value,
    }

    const requestBody = Object.keys(searchParams).every(
      (key) => searchParams[key] === null || searchParams[key].length === 0,
    )
      ? {}
      : searchParams

    const response = await apiClient.post('/admin/vouchers/search', requestBody)
    vouchers.value = response.data.data || []
    totalElements.value = response.data.pagination?.totalElements || 0
    page.value = newPage

    selectedVouchers.value = []
    selectAll.value = false

    if (!vouchers.value.length) {
      ElMessage.warning('Không tìm thấy voucher nào!')
    }
  } catch (error) {
    console.error('Lỗi tải danh sách voucher:', error)
    ElMessage.error(`Tải danh sách voucher thất bại: ${error.message}`)
    vouchers.value = []
    totalElements.value = 0
  }
}

// Cập nhật danh sách các voucher được chọn
const handleSelectionChange = (val) => {
  selectedVouchers.value = val.map(voucher => voucher.id);
};

// Xử lý khi người dùng chuyển trang
const handlePageChange = (newPage) => {
  fetchVoucher(newPage - 1); // Trừ 1 vì API bắt đầu từ trang 0
};

// Xử lý khi người dùng thay đổi số lượng mục trên mỗi trang
const handlePageSizeChange = (newSize) => {
  size.value = newSize;
  fetchVoucher(0); // Chuyển về trang 1 khi thay đổi kích thước
};

// Xuất Excel
const exportExcel = async () => {
  if (selectedVouchers.value.length === 0) {
    ElMessage.warning('Vui lòng chọn ít nhất một voucher để xuất!')
    return
  }

  try {
    const response = await apiClient.post('/admin/vouchers/vcollect', {
      voucherIds: selectedVouchers.value
    }, {
      responseType: 'blob' // Expect binary data (Excel file)
    })

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
  row.voucherType === 1 ? 'Công khai' : row.voucherType === 2 ? 'Cá nhân' : '-'

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
        await apiClient.delete(`/admin/vouchers/${row.id}`)
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

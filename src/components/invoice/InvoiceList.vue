<template>
  <div class="p-4">
    <el-row justify="space-between" align="middle" class="mb-4">
      <el-col :span="12">
        <h3 class="mb-0">Danh sách hóa đơn</h3>
      </el-col>
      <el-col :span="12" class="text-end">
        <el-dropdown @command="handleExportCommand" type="primary">
          <el-button type="primary">
            Xuất Excel<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="selected" :disabled="selectedRows.length === 0">
                Xuất các hóa đơn đã chọn
              </el-dropdown-item>
              <el-dropdown-item command="currentPage" :disabled="invoices.length === 0">
                Xuất hóa đơn trang này
              </el-dropdown-item>
              <el-dropdown-item command="all"> Xuất tất cả hóa đơn </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
    </el-row>

    <!-- <div class="mb-4">
      <InvoiceSearch @search="onSearch" @clear="onClear" />
    </div> -->

<!--  -->

    <el-table
      :data="invoices"
      style="width: 100%"
      v-loading="loading"
      border
      ref="invoiceTable"
      @selection-change="handleRowSelection"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="index" label="#" width="50" align="center" :index="tableIndex" />
      <el-table-column prop="invoiceCode" label="Mã hóa đơn" width="150">
        <template #default="scope">
          {{ getField(scope.row, 'invoiceCode') }}
        </template>
      </el-table-column>
      <el-table-column prop="customerName" label="Khách hàng">
        <template #default="scope">
          {{ getField(scope.row, 'customerName') || 'Khách lẻ' }}
        </template>
      </el-table-column>
      <!-- <el-table-column prop="employeeName" label="Nhân viên">
        <template #default="scope">
          {{ getField(scope.row, 'employeeName') || '---' }}
        </template>
      </el-table-column> -->
      <!-- <el-table-column prop="totalAmount" label="Tổng tiền" align="right" width="150">
        <template #default="scope">
          {{ formatCurrency(getField(scope.row, 'totalAmount')) }}
        </template>
      </el-table-column> -->
      <!-- <el-table-column prop="discountAmount" label="Giảm giá" align="right" width="150">
        <template #default="scope">
          {{ formatCurrency(getField(scope.row, 'discountAmount')) }}
        </template>
      </el-table-column> -->
      <el-table-column prop="finalAmount" label="Thành tiền" align="right" width="150">
        <template #default="scope">
          {{ formatCurrency(getField(scope.row, 'finalAmount')) }}
        </template>
      </el-table-column>

      <el-table-column prop="orderType" label="Loại đơn hàng" align="center">
        <template #default="scope">
          <el-tag type="info" disable-transitions>
            {{ getField(scope.row, 'orderType') === 0 ? 'Tại quầy' : 'Online' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="status" label="Trạng thái" align="center">
        <template #default="scope">
          <el-tag
            :type="statusClass(getField(scope.row, 'status'), getField(scope.row, 'statusDetail'), getField(scope.row, 'orderType'))"
            disable-transitions
          >
            {{ statusText(getField(scope.row, 'status'), getField(scope.row, 'statusDetail'), getField(scope.row, 'orderType')) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createdDate" label="Ngày tạo" align="center" width="180">
        <template #default="scope">
          {{ formatDate(getField(scope.row, 'createdDate')) }}
        </template>
      </el-table-column>
      <!-- <el-table-column prop="description" label="Ghi chú">
        <template #default="scope">
          {{ getField(scope.row, 'description') || '---' }}
        </template>
      </el-table-column> -->
      <el-table-column label="Thao tác" width="120" align="center" fixed="right">
        <template #default="scope">
          <el-button-group>
            <el-button
              :icon="View"
              type="primary"
              size="small"
              @click="viewInvoiceDetails(getField(scope.row, 'id'))"
              title="Xem chi tiết"
            />
            <el-button
              :icon="Printer"
              type="success"
              size="small"
              @click="printInvoice(getField(scope.row, 'id'))"
              title="In hóa đơn"
            />
          </el-button-group>
        </template>
      </el-table-column>
      <template #empty>
        <p class="text-center text-muted m-4">Không có hóa đơn nào để hiển thị.</p>
      </template>
    </el-table>

    <el-pagination
      v-if="totalPages > 0"
      class="mt-4 justify-content-end"
      background
      layout="total, prev, pager, next, sizes"
      :total="totalItems"
      :current-page="page + 1"
      :page-size="size"
      :page-sizes="[5, 10, 20, 50, 100]"
      :pager-count="5"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="dialogVisible" title="Chi tiết hóa đơn" width="60%" destroy-on-close>
      <template #header="{ close, titleId, titleClass }">
        <div class="my-header">
          <h4 :id="titleId" :class="titleClass">
            Chi tiết hóa đơn #{{ selectedInvoice?.invoiceCode || selectedInvoice?.id }}
          </h4>
        </div>
      </template>

      <div v-if="selectedInvoice">
        <el-row :gutter="20" class="mb-2">
          <el-col :span="12">
            <strong>Khách hàng:</strong> {{ selectedInvoice.customerName || 'Khách lẻ' }}
          </el-col>
          <el-col :span="12">
            <strong>Nhân viên:</strong> {{ selectedInvoice.employeeName || '---' }}
          </el-col>
        </el-row>

        <el-row :gutter="20" class="mb-3">
          <el-col :span="12">
            <strong>Ngày tạo:</strong> {{ formatDate(selectedInvoice.createdDate) }}
          </el-col>
          <el-col :span="12">
            <strong>Ghi chú:</strong> {{ selectedInvoice.description || '---' }}
          </el-col>
        </el-row>

        <el-table :data="invoiceDetails" border size="small">
          <el-table-column property="productName" label="Sản phẩm" />
          <el-table-column property="quantity" label="Số lượng" align="right" />
          <el-table-column label="Giá bán" align="right">
            <template #default="scope">
              <template v-if="scope.row.discountedPrice !== null">
                <del class="text-gray-500 me-1">{{ formatCurrency(scope.row.sellPrice) }}</del>
                <span class="text-danger">{{ formatCurrency(scope.row.discountedPrice) }}</span>
              </template>
              <template v-else>
                {{ formatCurrency(scope.row.sellPrice) }}
              </template>
            </template>
          </el-table-column>
          <el-table-column label="Thành tiền" align="right">
            <template #default="scope">
              {{
                formatCurrency(
                  (scope.row.discountedPrice !== null ? scope.row.discountedPrice : scope.row.sellPrice) * scope.row.quantity
                )
              }}
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 text-end">
          <p><strong>Tổng tiền:</strong> {{ formatCurrency(selectedInvoice.totalAmount) }}</p>
          <p><strong>Giảm giá:</strong> {{ formatCurrency(selectedInvoice.discountAmount) }}</p>
          <h4 class="mt-2">
            <strong>Thành tiền:</strong> {{ formatCurrency(selectedInvoice.finalAmount) }}
          </h4>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Đóng</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import apiClient from '../../utils/axiosInstance.js'
import {
  ElTable,
  ElTableColumn,
  ElButton,
  ElButtonGroup,
  ElTag,
  ElDialog,
  ElPagination,
  ElRow,
  ElCol,
  ElLoading,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElIcon,
} from 'element-plus'
import { View, Printer, ArrowDown } from '@element-plus/icons-vue'
import InvoiceSearch from './InvoiceSearch.vue'
import Swal from 'sweetalert2'
import { ElMessageBox, ElMessage } from 'element-plus'

// State refs
const invoices = ref([])
const selectedInvoice = ref(null)
const invoiceDetails = ref([])
const page = ref(0)
const size = ref(5)
const totalPages = ref(0)
const totalItems = ref(0)
const isSearching = ref(false)
const loading = ref(true)
const selectedRows = ref([])
// Dialog visibility
const dialogVisible = ref(false)

// Giữ lại các giá trị tìm kiếm hiện tại để phân trang
let currentKeyword = ''
let currentStatus = null
let currentCreatedDate = null

const handleRowSelection = (selection) => {
  selectedRows.value = selection
}

const getField = (inv, field) => inv[field] ?? inv?.invoice?.[field]

const tableIndex = (index) => {
  return page.value * size.value + index + 1
}

const formatDateToYYYYMMDD = (date) => {
  if (!date) return null
  if (typeof date === 'string') return date
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

const fetchOrSearch = async () => {
  loading.value = true
  const endpoint = isSearching.value ? '/admin/invoices/search' : '/admin/invoices'
  const params = {
    page: page.value,
    size: size.value,
  }

  if (isSearching.value) {
    if (currentKeyword) params.keyword = currentKeyword
    if (currentStatus !== null) params.status = currentStatus
    if (currentCreatedDate) params.createdDate = currentCreatedDate
  }

  try {
    const res = await apiClient.get(endpoint, { params })
    invoices.value = res.data.content.map(item => item.invoice);
    console.log('data: ',invoices.value)
    totalPages.value = res.data.page.totalPages
    totalItems.value = res.data.page.totalElements
    page.value = res.data.page.number
    size.value = res.data.page.size
  } catch (err) {
    console.error('Lỗi khi tải hóa đơn:', err)
    Swal.fire('Lỗi', 'Không thể tải danh sách hóa đơn.', 'error')
  } finally {
    loading.value = false
  }
}

const onSearch = ({ keyword, status, createdDate }) => {
  page.value = 0

  let newStatus = null
  if (status !== null && status !== undefined && status !== '') {
    const numStatus = Number(status)
    if (!isNaN(numStatus)) newStatus = numStatus
  }

  const newKeyword = keyword?.trim() || ''
  const newCreatedDate = createdDate ? formatDateToYYYYMMDD(createdDate) : null

  if (newKeyword || newStatus !== null || newCreatedDate) {
    isSearching.value = true
    currentKeyword = newKeyword
    currentStatus = newStatus
    currentCreatedDate = newCreatedDate
  } else {
    isSearching.value = false
    currentKeyword = ''
    currentStatus = null
    currentCreatedDate = null
  }

  fetchOrSearch()
}

const onClear = () => {
  isSearching.value = false
  currentKeyword = ''
  currentStatus = null
  currentCreatedDate = null
  page.value = 0
  fetchOrSearch()
}

const viewInvoiceDetails = async (invoiceId) => {
  if (!invoiceId) return
  try {
    const res = await apiClient.get(`/admin/invoices/${invoiceId}/detail`)
    selectedInvoice.value = res.data.invoice || res.data
    // ✅ Lấy từ invoice.invoiceDetails thay vì res.data.details
    invoiceDetails.value = res.data.invoice?.invoiceDetails || []
    dialogVisible.value = true
  } catch (err) {
    console.error('Lỗi khi xem chi tiết hóa đơn:', err)
    Swal.fire('Lỗi', 'Không thể tải chi tiết hóa đơn.', 'error')
  }
}


const printInvoice = (invoiceId) => {
  if (!invoiceId) {
    console.warn('Cần có ID hóa đơn để in.')
    return
  }

  ElMessageBox.confirm('Bạn có chắc muốn in hóa đơn?', 'Xác nhận', {
    confirmButtonText: 'Có, In ngay!',
    cancelButtonText: 'Hủy',
    type: 'warning',
  })
    .then(() => {
      const url = `/admin/invoices/${invoiceId}/export-id`

      apiClient
        .get(url, { responseType: 'blob' })
        .then((response) => {
          const file = new Blob([response.data], { type: 'application/pdf' })
          const fileURL = URL.createObjectURL(file)
          window.open(fileURL)
        })
        .catch((error) => {
          console.error('Lỗi khi in hóa đơn:', error)
          ElMessage.error('Không thể in hóa đơn. Vui lòng thử lại.')
        })
    })
    .catch(() => {
      // Người dùng bấm hủy, không làm gì cả
    })
}

const handleExportCommand = (command) => {
  const url = '/admin/invoices/export-excel'
  const params = {}
  let exportFileName = `hoa_don_${new Date().toLocaleDateString('vi-VN').replace(/\//g, '-')}.xlsx`
  let actionDescription = ''

  switch (command) {
    case 'selected':
      if (selectedRows.value.length === 0) {
        ElMessage.warning('Vui lòng chọn ít nhất một hóa đơn để xuất.')
        return
      }
      const selectedIds = selectedRows.value.map((row) => getField(row, 'id'))
      params.invoiceIds = selectedIds.join(',')
      actionDescription = `Xuất ${selectedIds.length} hóa đơn đã chọn...`
      break

    case 'currentPage':
      if (invoices.value.length === 0) {
        ElMessage.warning('Không có hóa đơn nào ở trang hiện tại để xuất.')
        return
      }
      const currentPageIds = invoices.value.map((row) => getField(row, 'id'))
      params.invoiceIds = currentPageIds.join(',')
      actionDescription = 'Xuất các hóa đơn trên trang hiện tại...'
      break

    case 'all':
      // Gửi các tham số rỗng để báo cho backend rằng chúng ta muốn xuất tất cả,
      // mô phỏng một tìm kiếm "trống". Điều này giúp tránh lỗi 500 do
      // gọi API mà không có tham số bắt buộc.
      params.keyword = ''
      params.status = null
      params.createdDate = null
      exportFileName = `tat_ca_hoa_don_${new Date().toLocaleDateString('vi-VN').replace(/\//g, '-')}.xlsx`
      actionDescription = 'Xuất tất cả hóa đơn...'
      break

    default:
      ElMessage.error('Hành động xuất không hợp lệ.')
      return
  }

  ElMessage.info(actionDescription || 'Đang chuẩn bị tệp Excel, vui lòng chờ...')

  apiClient
    .get(url, { params, responseType: 'blob' })
    .then((response) => {
      const file = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      const fileURL = URL.createObjectURL(file)
      const link = document.createElement('a')
      link.href = fileURL
      link.download = exportFileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(fileURL)
      ElMessage.success('Xuất Excel thành công!')
    })
    .catch((error) => {
      console.error('Lỗi khi xuất Excel:', error)
      ElMessage.error('Không thể xuất Excel. Vui lòng thử lại.')
    })
}

const handlePageChange = (newPage) => {
  page.value = newPage - 1
  fetchOrSearch()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  page.value = 0
  fetchOrSearch()
}

const formatCurrency = (val) => {
  if (val == null) return '---'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(val)
}

const formatDate = (val) => {
  if (!val) return '---'
  const d = new Date(val)
  return d.toLocaleDateString('vi-VN') + ' ' + d.toLocaleTimeString('vi-VN')
}

// Hàm mới để hiển thị text trạng thái
const statusText = (status, statusDetail, orderType) => {
  if (orderType === 0) { // Đơn tại quầy
    switch (status) {
      case 'HUY_GIAO_DICH': return 'Hủy giao dịch'
      case 'DANG_XU_LY': return 'Đang xử lý'
      case 'THANH_CONG': return 'Thành công'
      case 'DA_HUY': return 'Đã hủy'
      case 'TRA_HANG': return 'Trả hàng'
      case 'KHIEU_NAI': return 'Khiếu nại'
      default: return 'Không xác định'
    }
  } else if (orderType === 1) { // Đơn online
    switch (statusDetail) {
      case 'DANG_GIAO_DICH': return 'Đang giao dịch'
      case 'HUY_DON': return 'Hủy đơn hàng'
      case 'HUY_GIAO_DICH': return 'Hủy giao dịch'
      case 'CHO_XU_LY': return 'Chờ xử lý'
      case 'DA_XU_LY': return 'Đã xử lý'
      case 'CHO_GIAO_HANG': return 'Chờ giao hàng'
      case 'DANG_GIAO_HANG': return 'Đang giao hàng'
      case 'GIAO_THANH_CONG': return 'Giao hàng thành công'
      case 'GIAO_THAT_BAI': return 'Giao hàng thất bại'
      case 'MAT_HANG': return 'Mất hàng'
      case 'DA_HOAN_TIEN': return 'Đã hoàn tiền'
      case 'DA_HOAN_THANH': return 'Đã hoàn thành'
      default: return 'Không xác định'
    }
  }
  return 'Không xác định'
}

// Hàm mới để gán class cho tag trạng thái
const statusClass = (status, statusDetail, orderType) => {
  if (orderType === 0) { // Đơn tại quầy
    switch (status) {
      case 'HUY_GIAO_DICH': return 'danger'
      case 'DANG_XU_LY': return 'warning'
      case 'THANH_CONG': return 'success'
      case 'DA_HUY': return 'danger'
      case 'TRA_HANG': return 'info'
      case 'KHIEU_NAI': return 'info'
      default: return 'info'
    }
  } else if (orderType === 1) { // Đơn online
    switch (statusDetail) {
      case 'DANG_GIAO_DICH': return 'primary'
      case 'HUY_DON': return 'danger'
      case 'HUY_GIAO_DICH': return 'danger'
      case 'CHO_XU_LY': return 'warning'
      case 'DA_XU_LY': return 'primary'
      case 'CHO_GIAO_HANG': return 'info'
      case 'DANG_GIAO_HANG': return 'primary'
      case 'GIAO_THANH_CONG': return 'success'
      case 'GIAO_THAT_BAI': return 'danger'
      case 'MAT_HANG': return 'danger'
      case 'DA_HOAN_TIEN': return 'info'
      case 'DA_HOAN_THANH': return 'success'
      default: return 'info'
    }
  }
  return 'info'
}


onMounted(() => {
  fetchOrSearch()
})
</script>

<style scoped>
.mb-4 {
  margin-bottom: 20px;
}
.mt-4 {
  margin-top: 20px;
}
.p-4 {
  padding: 20px;
}
.text-end {
  text-align: right;
}
.justify-content-end {
  justify-content: flex-end;
}
.my-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}
/* Style for dropdown icon */
.el-icon--right {
  margin-left: 8px;
}
</style>

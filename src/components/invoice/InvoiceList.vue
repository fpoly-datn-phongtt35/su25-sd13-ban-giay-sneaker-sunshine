<template>
  <div class="p-4">
    <el-row justify="space-between" align="middle" class="mb-4">
      <el-col :span="12">
        <h3 class="mb-0">Danh sách hóa đơn</h3>
      </el-col>
    </el-row>

    <div class="mb-4">
      <InvoiceSearch @search="onSearch" @clear="onClear" />
    </div>

    <el-table :data="invoices" style="width: 100%" v-loading="loading" border>
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
      <el-table-column prop="employeeName" label="Nhân viên">
        <template #default="scope">
          {{ getField(scope.row, 'employeeName') || '---' }}
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="Tổng tiền" align="right" width="150">
        <template #default="scope">
          {{ formatCurrency(getField(scope.row, 'totalAmount')) }}
        </template>
      </el-table-column>
      <el-table-column prop="discountAmount" label="Giảm giá" align="right" width="150">
        <template #default="scope">
          {{ formatCurrency(getField(scope.row, 'discountAmount')) }}
        </template>
      </el-table-column>
      <el-table-column prop="finalAmount" label="Thành tiền" align="right" width="150">
        <template #default="scope">
          {{ formatCurrency(getField(scope.row, 'finalAmount')) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Trạng thái" align="center">
        <template #default="scope">
          <el-tag :type="statusClass(getField(scope.row, 'status'))" disable-transitions>
            {{ statusText(getField(scope.row, 'status')) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdDate" label="Ngày tạo" align="center" width="180">
        <template #default="scope">
          {{ formatDate(getField(scope.row, 'createdDate')) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="Ghi chú">
        <template #default="scope">
          {{ getField(scope.row, 'description') || '---' }}
        </template>
      </el-table-column>
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
          <el-col :span="12"
            ><strong>Khách hàng:</strong> {{ selectedInvoice.customerName || 'Khách lẻ' }}</el-col
          >
          <el-col :span="12"
            ><strong>Nhân viên:</strong> {{ selectedInvoice.employeeName || '---' }}</el-col
          >
        </el-row>
        <el-row :gutter="20" class="mb-3">
          <el-col :span="12"
            ><strong>Ngày tạo:</strong> {{ formatDate(selectedInvoice.createdDate) }}</el-col
          >
          <el-col :span="12"
            ><strong>Ghi chú:</strong> {{ selectedInvoice.description || '---' }}</el-col
          >
        </el-row>

        <el-table :data="invoiceDetails" border size="small">
          <el-table-column property="productName" label="Sản phẩm" />
          <el-table-column property="quantity" label="Số lượng" align="right" />
          <el-table-column label="Giá bán" align="right">
            <template #default="scope">{{ formatCurrency(scope.row.price) }}</template>
          </el-table-column>
          <el-table-column label="Thành tiền" align="right">
            <template #default="scope">{{
              formatCurrency(scope.row.price * scope.row.quantity)
            }}</template>
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
} from 'element-plus'
import { View, Printer } from '@element-plus/icons-vue'
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

// Dialog visibility
const dialogVisible = ref(false)

// Giữ lại các giá trị tìm kiếm hiện tại để phân trang
let currentKeyword = ''
let currentStatus = null
let currentCreatedDate = null

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
    invoices.value = res.data.content
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
    invoiceDetails.value = res.data.details || []
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

  ElMessageBox.confirm(
    'Bạn có chắc muốn in hóa đơn?', 
    'Xác nhận', 
    {
      confirmButtonText: 'Có, In ngay!',
      cancelButtonText: 'Hủy',
      type: 'warning',
    }
  ).then(() => {
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
  }).catch(() => {
    // Người dùng bấm hủy, không làm gì cả
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

const statusText = (status) => {
  switch (status) {
    case 0:
      return 'Chờ xử lý'
    case 1:
      return 'Đã thanh toán'
    case 2:
      return 'Đã hủy'
    default:
      return 'Không xác định'
  }
}

const statusClass = (status) => {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'danger'
    default:
      return 'info'
  }
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
</style>

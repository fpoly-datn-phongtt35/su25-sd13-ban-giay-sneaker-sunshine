<template>
  <div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="mb-0">Danh sách hóa đơn</h3>
    </div>

    <!-- Component tìm kiếm -->
    <InvoiceSearch @search="onSearch" @clear="onClear" class="mb-3" />

    <!-- Table -->
    <div class="table-responsive" v-if="invoices.length > 0">
      <table class="table table-hover table-borderless align-middle">
        <thead class="table-light">
          <tr class="align-middle text-center">
            <th><input type="checkbox" v-model="selectAll" @change="toggleAll" /></th>
            <th>#</th>
            <th>Mã hóa đơn</th>
            <th>Khách hàng</th>
            <th>Nhân viên</th>
            <th>Tổng tiền</th>
            <th>Giảm giá</th>
            <th>Thành tiền</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th>Ghi chú</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(inv, index) in invoices" :key="getField(inv, 'id')" class="text-center">
            <td><input type="checkbox" v-model="inv.selected" /></td>
            <td>{{ page * size + index + 1 }}</td>
            <td>{{ getField(inv, 'invoiceCode') }}</td>
            <td>{{ getField(inv, 'customerName') || 'Khách lẻ' }}</td>
            <td>{{ getField(inv, 'employeeName') || '---' }}</td>
            <td>{{ formatCurrency(getField(inv, 'totalAmount')) }}</td>
            <td>{{ formatCurrency(getField(inv, 'discountAmount')) }}</td>
            <td>{{ formatCurrency(getField(inv, 'finalAmount')) }}</td>
            <td>
              <span :class="['badge', statusClass(getField(inv, 'status'))]">
                {{ statusText(getField(inv, 'status')) }}
              </span>
            </td>
            <td>{{ formatDate(getField(inv, 'createdDate')) }}</td>
            <td>{{ getField(inv, 'description') || '---' }}</td>
            <td>
              <div class="btn-group btn-group-sm">
                <button
                  class="btn btn-outline-primary"
                  @click="viewInvoiceDetails(getField(inv, 'id'))"
                  title="Xem chi tiết hóa đơn"
                >
                  <i class="bi bi-eye"></i>
                </button>
                <button
                  class="btn btn-outline-success"
                  @click="printInvoice(getField(inv, 'invoiceCode'))"
                  title="In hóa đơn"
                >
                  <i class="bi bi-printer"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Nếu không có hóa đơn -->
    <div v-else>
      <p class="text-center text-muted mt-4">Không có hóa đơn nào để hiển thị.</p>
    </div>

    <!-- Pagination -->
    <nav
      aria-label="Pagination"
      v-if="totalPages > 0"
      class="d-flex justify-content-between align-items-center mt-3"
    >
      <div class="ms-2 text-muted"><strong>Trang:</strong> {{ page + 1 }} / {{ totalPages }}</div>
      <div class="btn-group">
        <button
          class="btn btn-outline-secondary"
          :disabled="page === 0"
          @click="changePage(page - 1)"
        >
          ← Trước
        </button>
        <button
          class="btn btn-outline-secondary"
          :disabled="page >= totalPages - 1"
          @click="changePage(page + 1)"
        >
          Tiếp →
        </button>
      </div>
    </nav>

    <!-- Modal chi tiết -->
    <div class="modal fade" tabindex="-1" ref="modalEl" aria-hidden="true">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title">
              🧾 Chi tiết hóa đơn #{{ selectedInvoice?.invoiceCode || selectedInvoice?.id }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
          </div>
          <div class="modal-body" v-if="selectedInvoice">
            <div class="row mb-2">
              <div class="col-md-6"><strong>Khách hàng:</strong> {{ selectedInvoice.customerName || 'Khách lẻ' }}</div>
              <div class="col-md-6"><strong>Nhân viên:</strong> {{ selectedInvoice.employeeName || '---' }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-6"><strong>Ngày tạo:</strong> {{ formatDate(selectedInvoice.createdDate) }}</div>
              <div class="col-md-6"><strong>Ghi chú:</strong> {{ selectedInvoice.description || '---' }}</div>
            </div>

            <table class="table table-sm table-bordered mt-3">
              <thead class="table-light">
                <tr>
                  <th>Sản phẩm</th>
                  <th class="text-end">Số lượng</th>
                  <th class="text-end">Giá bán</th>
                  <th class="text-end">Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="detail in invoiceDetails" :key="detail.id">
                  <td>{{ detail.productName }}</td>
                  <td class="text-end">{{ detail.quantity }}</td>
                  <td class="text-end">{{ formatCurrency(detail.price) }}</td>
                  <td class="text-end">{{ formatCurrency(detail.price * detail.quantity) }}</td>
                </tr>
              </tbody>
            </table>

            <div class="mt-4 text-end">
              <p><strong>Tổng tiền:</strong> {{ formatCurrency(selectedInvoice.totalAmount) }}</p>
              <p><strong>Giảm giá:</strong> {{ formatCurrency(selectedInvoice.discountAmount) }}</p>
              <p><strong>Thành tiền:</strong> {{ formatCurrency(selectedInvoice.finalAmount) }}</p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeModal">Đóng</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import { Modal } from 'bootstrap'
import InvoiceSearch from './InvoiceSearch.vue'

const invoices = ref([])
const selectedInvoice = ref(null)
const invoiceDetails = ref([])
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)
const isSearching = ref(false)
const modalInstance = ref(null)
const modalEl = ref(null)

const selectAll = ref(false)

// Theo dõi khi selectAll thay đổi để cập nhật tất cả checkbox
const toggleAll = () => {
  invoices.value.forEach((inv) => {
    inv.selected = selectAll.value
  })
}

// Tự động cập nhật trạng thái của checkbox "select all"
watch(
  invoices,
  (newVal) => {
    if (!newVal.length) {
      selectAll.value = false
      return
    }
    selectAll.value = newVal.every((inv) => inv.selected)
  },
  { deep: true },
)

// Lưu giữ giá trị tìm kiếm hiện tại (để phân trang vẫn giữ filter)
let currentKeyword = ''
let currentStatus = null
let currentCreatedDate = null

const getField = (inv, field) => inv[field] ?? inv?.invoice?.[field]

const formatDateToYYYYMMDD = (date) => {
  if (!date) return null
  if (typeof date === 'string') return date
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

const fetchInvoices = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/invoices', {
      params: { page: page.value, size: size.value },
    })
    invoices.value = res.data.content.map((inv) => ({ ...inv, selected: false }))
    totalPages.value = res.data.totalPages
  } catch (err) {
    console.error('Lỗi tải hóa đơn:', err)
  }
}

const searchInvoices = async (keyword, status, createdDate) => {
  isSearching.value = true
  currentKeyword = keyword || ''
  currentStatus = status == null || status === '' ? null : Number(status)
  currentCreatedDate = createdDate ? formatDateToYYYYMMDD(createdDate) : null

  try {
    const res = await axios.get('http://localhost:8080/api/invoices/search', {
      params: {
        keyword: currentKeyword || undefined,
        status: currentStatus !== null ? currentStatus : undefined,
        createdDate: currentCreatedDate || undefined,
        page: page.value,
        size: size.value,
      },
    })
    invoices.value = res.data.content.map((inv) => ({ ...inv, selected: false }))
    totalPages.value = res.data.totalPages
  } catch (err) {
    console.error('Lỗi tìm kiếm hóa đơn:', err)
  }
}

const onSearch = ({ keyword, status, createdDate }) => {
  page.value = 0
  if (
    (!keyword || keyword.trim() === '') &&
    (status === '' || status === null || status === undefined) &&
    (!createdDate || createdDate === null)
  ) {
    isSearching.value = false
    currentKeyword = ''
    currentStatus = null
    currentCreatedDate = null
    fetchInvoices()
  } else {
    searchInvoices(keyword, status, createdDate)
  }
}

const onClear = () => {
  isSearching.value = false
  currentKeyword = ''
  currentStatus = null
  currentCreatedDate = null
  page.value = 0
  fetchInvoices()
}

const viewInvoiceDetails = async (invoiceId) => {
  if (!invoiceId) return
  try {
    const res = await axios.get(`http://localhost:8080/api/invoices/${invoiceId}/detail`)
    selectedInvoice.value = res.data.invoice || res.data
    invoiceDetails.value = res.data.details || []
    modalInstance.value.show()
  } catch (err) {
    console.error('Lỗi chi tiết hóa đơn:', err)
  }
}

const printInvoice = (invoiceCode) => {
  if (!invoiceCode) {
    console.warn('Invoice code is required để in hóa đơn.')
    return
  }
  const printUrl = `http://localhost:8080/api/invoices/${invoiceCode}/export`
  window.open(printUrl, '_blank', 'noopener,noreferrer')
}

const closeModal = () => {
  modalInstance.value.hide()
  selectedInvoice.value = null
  invoiceDetails.value = []
}

const changePage = async (newPage) => {
  if (newPage < 0 || newPage >= totalPages.value) return
  page.value = newPage
  if (isSearching.value) {
    await searchInvoices(currentKeyword, currentStatus, currentCreatedDate)
  } else {
    await fetchInvoices()
  }
}

const formatCurrency = (val) => {
  if (val == null) return '---'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
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
      return 'text-warning'
    case 1:
      return 'text-success'
    case 2:
      return 'text-danger'
    default:
      return 'text-muted'
  }
}

onMounted(() => {
  modalInstance.value = new Modal(modalEl.value)
  fetchInvoices()
})
</script>

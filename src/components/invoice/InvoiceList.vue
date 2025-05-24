<template>
  <div class="container-fluid mt-4">
    <h3 class="mb-3">Danh sách hóa đơn</h3>

    <!-- Component tìm kiếm -->
    <InvoiceSearch @search="onSearch" @clear="onClear" />

    <div class="table-responsive" v-if="invoices.length > 0">
      <table class="table table-striped table-bordered">
        <thead>
          <tr>
            <th>STT</th>
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
          <tr v-for="(inv, index) in invoices" :key="getField(inv, 'id')">
            <td>{{ page * size + index + 1 }}</td>
            <td>{{ getField(inv, 'invoiceCode') }}</td>
            <td>{{ getField(inv, 'customerName') || 'Khách lẻ' }}</td>
            <td>{{ getField(inv, 'employeeName') || '---' }}</td>
            <td>{{ formatCurrency(getField(inv, 'totalAmount')) }}</td>
            <td>{{ formatCurrency(getField(inv, 'discountAmount')) }}</td>
            <td>{{ formatCurrency(getField(inv, 'finalAmount')) }}</td>
            <td>
              <span :class="statusClass(getField(inv, 'status'))">
                {{ statusText(getField(inv, 'status')) }}
              </span>
            </td>
            <td>{{ formatDate(getField(inv, 'createdDate')) }}</td>
            <td>{{ getField(inv, 'description') || '---' }}</td>
            <td>
              <button class="btn btn-primary btn-sm" @click="viewInvoiceDetails(getField(inv, 'id'))">
                Xem chi tiết
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else>
      <p class="text-center">Không có hóa đơn nào để hiển thị.</p>
    </div>

    <!-- Pagination -->
    <nav aria-label="Page navigation" v-if="totalPages > 1">
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{ disabled: page === 0 }" @click="changePage(page - 1)">
          <a class="page-link" href="#">Previous</a>
        </li>
        <li
          class="page-item"
          v-for="p in totalPages"
          :key="p"
          :class="{ active: page === p - 1 }"
          @click="changePage(p - 1)"
        >
          <a class="page-link" href="#">{{ p }}</a>
        </li>
        <li class="page-item" :class="{ disabled: page === totalPages - 1 }" @click="changePage(page + 1)">
          <a class="page-link" href="#">Next</a>
        </li>
      </ul>
    </nav>

    <!-- Modal chi tiết -->
    <div class="modal fade" tabindex="-1" ref="modalEl" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Chi tiết hóa đơn #{{ selectedInvoice?.id }}</h5>
            <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
          </div>
          <div class="modal-body" v-if="selectedInvoice">
            <p><strong>Khách hàng:</strong> {{ selectedInvoice.customerName || 'Khách lẻ' }}</p>
            <p><strong>Nhân viên:</strong> {{ selectedInvoice.employeeName || '---' }}</p>
            <p><strong>Ngày tạo:</strong> {{ formatDate(selectedInvoice.createdDate) }}</p>
            <p><strong>Ghi chú:</strong> {{ selectedInvoice.description || '---' }}</p>

            <table class="table table-bordered mt-3">
              <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Giá bán</th>
                  <th>Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="detail in invoiceDetails" :key="detail.id">
                  <td>{{ detail.productName }}</td>
                  <td>{{ detail.quantity }}</td>
                  <td>{{ formatCurrency(detail.price) }}</td>
                  <td>{{ formatCurrency(detail.price * detail.quantity) }}</td>
                </tr>
              </tbody>
            </table>

            <p class="mt-3"><strong>Tổng tiền:</strong> {{ formatCurrency(selectedInvoice.totalAmount) }}</p>
            <p><strong>Giảm giá:</strong> {{ formatCurrency(selectedInvoice.discountAmount) }}</p>
            <p><strong>Thành tiền:</strong> {{ formatCurrency(selectedInvoice.finalAmount) }}</p>
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
import { ref, onMounted } from 'vue'
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
let currentKeyword = ''
let currentStatus = null // sửa lại null để phân biệt với 0

const getField = (inv, field) => inv[field] ?? inv?.invoice?.[field]

const fetchInvoices = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/invoices', {
      params: { page: page.value, size: size.value },
    })
    invoices.value = res.data.content
    totalPages.value = res.data.totalPages
  } catch (err) {
    console.error('Lỗi tải hóa đơn:', err)
  }
}

const searchInvoices = async (keyword, status) => {
  isSearching.value = true
  currentKeyword = keyword ?? ''
  currentStatus = status

  try {
    const res = await axios.get('http://localhost:8080/api/invoices/search', {
      params: {
        keyword: currentKeyword,
        status: currentStatus,
        page: page.value,
        size: size.value,
      },
    })
    invoices.value = res.data.content
    totalPages.value = res.data.totalPages
  } catch (err) {
    console.error('Lỗi tìm kiếm hóa đơn:', err)
  }
}

const onSearch = ({ keyword, status }) => {
  page.value = 0
  if ((keyword === '' || keyword == null) && (status === '' || status == null)) {
    isSearching.value = false
    currentKeyword = ''
    currentStatus = null
    fetchInvoices()
  } else {
    searchInvoices(keyword, status)
  }
}

const onClear = () => {
  isSearching.value = false
  currentKeyword = ''
  currentStatus = null
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

const closeModal = () => {
  modalInstance.value.hide()
  selectedInvoice.value = null
  invoiceDetails.value = []
}

const changePage = async (newPage) => {
  if (newPage < 0 || newPage >= totalPages.value) return
  page.value = newPage
  isSearching.value
    ? await searchInvoices(currentKeyword, currentStatus)
    : await fetchInvoices()
}

const formatCurrency = (val) => {
  if (val == null) return '---'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const formatDate = (val) => {
  if (!val) return '---'
  const d = new Date(val)
  return d.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
}

const statusClass = (s) => {
  return {
    0: 'badge bg-danger',
    1: 'badge bg-success',
    2: 'badge bg-secondary',
  }[s] || 'badge bg-warning'
}

const statusText = (s) => {
  return {
    0: 'Chưa thanh toán',
    1: 'Đã thanh toán',
    2: 'Đơn hàng bị hủy',
  }[s] || 'Không xác định'
}

onMounted(() => {
  modalInstance.value = new Modal(modalEl.value)
  fetchInvoices()
})
</script>

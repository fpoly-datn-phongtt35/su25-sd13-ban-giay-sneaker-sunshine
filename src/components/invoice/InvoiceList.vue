<template>
  <div class="container-fluid mt-4">
    <h3 class="mb-3">Danh sách hóa đơn</h3>

    <div class="table-responsive">
      <table class="table table-striped table-bordered">
        <thead>
          <tr>
            <th>STT</th>
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
          <tr v-for="(inv, index) in invoices" :key="inv.id">
            <td>{{ index + 1 }}</td>
            <td>{{ inv.customerName || 'Khách lẻ' }}</td>
            <td>{{ inv.employeeName || '---' }}</td>
            <td>{{ formatCurrency(inv.totalAmount) }}</td>
            <td>{{ formatCurrency(inv.discountAmount) }}</td>
            <td>{{ formatCurrency(inv.finalAmount) }}</td>
            <td>
              <span :class="statusClass(inv.status)">
                {{ statusText(inv.status) }}
              </span>
            </td>
            <td>{{ formatDate(inv.createdDate) }}</td>
            <td>{{ inv.description || '---' }}</td>
            <td>
              <button class="btn btn-primary btn-sm" @click="viewInvoiceDetails(inv.id)">
                Xem chi tiết
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal chi tiết hóa đơn -->
    <div
      class="modal fade"
      tabindex="-1"
      ref="modalEl"
      aria-hidden="true"
    >
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
import { Modal } from 'bootstrap'  // nhớ đã cài và import bootstrap JS rồi nhé

const invoices = ref([])
const selectedInvoice = ref(null)
const invoiceDetails = ref([])
const modalInstance = ref(null)
const modalEl = ref(null)

const fetchInvoices = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/invoices')
    invoices.value = res.data
  } catch (error) {
    console.error('Lỗi khi tải danh sách hóa đơn:', error)
  }
}

const viewInvoiceDetails = async (invoiceId) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/invoices/${invoiceId}/detail`)
    selectedInvoice.value = res.data.invoice
    invoiceDetails.value = res.data.details
    modalInstance.value.show()
  } catch (error) {
    console.error('Lỗi khi tải chi tiết hóa đơn:', error)
  }
}

const closeModal = () => {
  modalInstance.value.hide()
  selectedInvoice.value = null
  invoiceDetails.value = []
}

const formatCurrency = (value) => {
  if (value == null) return '---'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const formatDate = (dateString) => {
  if (!dateString) return '---'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const statusClass = (status) => {
  switch (status) {
    case 0:
      return 'badge bg-danger'    // Chưa thanh toán - đỏ
    case 1:
      return 'badge bg-success'   // Đã thanh toán - xanh
    case 2:
      return 'badge bg-secondary' // Đơn hàng bị hủy - xám
    default:
      return 'badge bg-warning'   // Trạng thái khác
  }
}

const statusText = (status) => {
  switch (status) {
    case 0:
      return 'Chưa thanh toán'
    case 1:
      return 'Đã thanh toán'
    case 2:
      return 'Đơn hàng bị hủy'
    default:
      return 'Không xác định'
  }
}

onMounted(() => {
  modalInstance.value = new Modal(modalEl.value)
  fetchInvoices()
})
</script>

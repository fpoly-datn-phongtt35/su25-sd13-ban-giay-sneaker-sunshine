<template>
  <div class="container py-4">
    <!-- Tạo hóa đơn mới -->
    <div class="mb-4">
      <button @click="createInvoice" class="btn btn-primary">Tạo hóa đơn mới</button>
    </div>

    <!-- Danh sách hóa đơn -->
    <div class="mb-5">
      <h4>Danh sách hóa đơn</h4>
      <table class="table table-bordered table-hover mt-3" v-if="invoices.length">
        <thead class="table-light">
          <tr>
            <th>Mã hóa đơn</th>
            <th>Ngày tạo</th>
            <th>Nhân viên</th>
            <th>Khách hàng</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="invoice in invoices" :key="invoice.id">
            <td>{{ invoice.invoiceCode }}</td>
            <td>{{ formatDate(invoice.createdDate) }}</td>
            <td>{{ invoice.employeeName || 'N/A' }}</td>
            <td>{{ invoice.customerName }}</td>
            <td>
              <button class="btn btn-success btn-sm" @click="viewInvoiceDetails(invoice.id)">
                Xem
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else>Không có hóa đơn.</p>

      <!-- Phân trang -->
      <div
        v-if="pagination.totalPages > 1"
        class="d-flex justify-content-between align-items-center mt-3"
      >
        <div>Trang hiện tại: {{ pagination.currentPage }} / {{ pagination.totalPages }}</div>
        <div>
          <button
            class="btn btn-secondary me-2"
            :disabled="!pagination.hasPrevious"
            @click="changePage(pagination.currentPage - 1)"
          >
            Trước
          </button>
          <button
            class="btn btn-secondary"
            :disabled="!pagination.hasNext"
            @click="changePage(pagination.currentPage + 1)"
          >
            Tiếp
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'

const invoices = ref([])
const page = ref(0) // page bắt đầu từ 0, theo chuẩn API
const size = ref(5)

const pagination = reactive({
  currentPage: 1,  // 1-based page để hiển thị cho người dùng
  totalPages: 0,
  hasPrevious: false,
  hasNext: false,
})

const employeeId = 1
const router = useRouter()
const toast = useToast()

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('vi-VN')
}

const fetchInvoices = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/counter-sales/invoices', {
      params: {
        status: 0,
        page: page.value,
        size: size.value,
      },
    })
    invoices.value = res.data.content
    pagination.totalPages = res.data.totalPages
    pagination.currentPage = res.data.number + 1 // API trả page từ 0 nên +1
    pagination.hasPrevious = !res.data.first ? true : false
    pagination.hasNext = !res.data.last ? true : false
  } catch (error) {
    console.error('Lỗi khi lấy danh sách hóa đơn:', error)
    toast.error('Lỗi khi lấy danh sách hóa đơn')
  }
}

const changePage = (newPage) => {
  // newPage là 1-based, đổi về 0-based trước khi gọi API
  if (newPage >= 1 && newPage <= pagination.totalPages) {
    page.value = newPage - 1
    fetchInvoices()
  }
}

const createInvoice = async () => {
  try {
    const res = await axios.post(`http://localhost:8080/api/counter-sales/create-empty?employeeId=${employeeId}`)
    toast.success(`Tạo hóa đơn mới thành công! Mã hóa đơn: ${res.data.invoiceCode || res.data.id}`)
    fetchInvoices()
  } catch (error) {
    console.error('Lỗi khi tạo hóa đơn:', error)
    toast.error('Tạo hóa đơn thất bại!')
  }
}

const viewInvoiceDetails = (id) => {
  router.push({ name: 'CounterSalesDisplay', params: { id } })
}

onMounted(() => {
  fetchInvoices()
})
</script>

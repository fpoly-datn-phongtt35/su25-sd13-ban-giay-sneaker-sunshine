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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'

const invoices = ref([])
const employeeId = 1
const router = useRouter()
const toast = useToast()

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('vi-VN')
}

const fetchInvoices = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/counter-sales/invoices')
    invoices.value = res.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách hóa đơn:', error)
    toast.error('Lỗi khi lấy danh sách hóa đơn')
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



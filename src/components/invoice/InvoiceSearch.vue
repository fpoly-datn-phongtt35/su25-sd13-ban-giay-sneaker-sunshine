<template>
  <div class="d-flex mb-3 gap-2 align-items-center">
    <input
      type="text"
      class="form-control"
      placeholder="Tìm kiếm mã hóa đơn, tên khách hàng, số điện thoại..."
      v-model="keyword"
      @input="onInput"
      autofocus
    />

    <select class="form-select w-auto" v-model="status" @change="onStatusChange">
      <option value="">Tất cả trạng thái</option>
      <option value="0">Chờ xử lý</option>
      <option value="1">Đã thanh toán</option>
      <option value="2">Đã hủy</option>
    </select>

    <input
      type="date"
      class="form-control w-auto"
      v-model="createdDate"
      @change="onCreatedDateChange"
      placeholder="Ngày tạo"
    />

    <button class="btn btn-success btn-sm" @click="exportExcel">
      Xuất Excel
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Swal from 'sweetalert2'

const keyword = ref('')
const status = ref('')
const createdDate = ref('')

const emit = defineEmits(['search', 'clear'])

let debounceTimer = null

function emitSearch() {
  const statusValue = status.value === '' ? undefined : Number(status.value)
  const dateValue = createdDate.value ? new Date(createdDate.value) : undefined

  emit('search', { keyword: keyword.value.trim(), status: statusValue, createdDate: dateValue })
}

function onInput() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    emitSearch()
  }, 300)
}

function onStatusChange() {
  emitSearch()
}

function onCreatedDateChange() {
  emitSearch()
}

function onClearClick() {
  keyword.value = ''
  status.value = ''
  createdDate.value = ''
  emit('clear')
}

async function exportExcel() {
  const result = await Swal.fire({
    title: 'Bạn có chắc muốn xuất toàn bộ danh sách hóa đơn ra file Excel?',
    showCancelButton: true,
    confirmButtonText: 'Có',
    cancelButtonText: 'Hủy',
    icon: undefined  // Không hiển thị icon
  })

  if (!result.isConfirmed) return

  try {
    const response = await fetch('http://localhost:8080/api/invoices/export-all-excel', {
      method: 'GET',
      headers: {
        'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      },
    })
    if (!response.ok) throw new Error('Lỗi tải file Excel')

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_hoa_don.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    Swal.fire('Lỗi', 'Xuất Excel thất bại: ' + error.message, 'error')
  }
}
</script>

<style scoped>
.btn-sm {
  height: 42px;
  padding: 0 10px;
  font-size: 0.875rem;
}
</style>

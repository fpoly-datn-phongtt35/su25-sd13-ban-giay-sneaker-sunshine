<template>
  <!-- Dialog chỉ chứa form -->
  <dialog ref="dialogRef" class="p-4" style="max-width: 500px; border-radius: 8px;">
    <form @submit.prevent="submitForm" novalidate>
      <h3 class="mb-4">Nhập thông tin khách hàng</h3>

      <div class="mb-3">
        <label for="phoneInput" class="form-label">Số điện thoại (bắt buộc):</label>
        <input
          id="phoneInput"
          v-model="phone"
          type="text"
          class="form-control"
          :class="{ 'is-invalid': error && !phone.trim() }"
          required
          autocomplete="off"
        />
        <div class="invalid-feedback" v-if="error && !phone.trim()">
          Số điện thoại là bắt buộc.
        </div>
      </div>

      <div class="mb-3">
        <label for="nameInput" class="form-label">Tên khách (tùy chọn):</label>
        <input
          id="nameInput"
          v-model="name"
          type="text"
          class="form-control"
          autocomplete="off"
        />
      </div>

      <div class="d-flex justify-content-between align-items-center mb-3">
        <button type="submit" class="btn btn-primary" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          {{ loading ? 'Đang gửi...' : 'Tạo' }}
        </button>
        <button type="button" class="btn btn-secondary" @click="closeDialog" :disabled="loading">Hủy</button>
      </div>

      <div v-if="error && phone.trim()" class="alert alert-danger py-2">
        {{ error }}
      </div>
    </form>
  </dialog>

  <!-- Phần hiển thị khách hàng mới tạo bên ngoài dialog -->
  <div v-if="createdCustomer" class="mt-4 p-3 border rounded" style="max-width: 500px;">
    <h5>Khách hàng mới tạo:</h5>
    <p><strong>Số điện thoại:</strong> {{ createdCustomer.phone }}</p>
    <p><strong>Tên khách:</strong> {{ createdCustomer.customerName || '(Chưa có tên)' }}</p>
    <button class="btn btn-success" @click="selectCreatedCustomer">Chọn khách hàng này</button>
  </div>
</template>

<script setup>
import { ref, defineExpose } from 'vue'
import axios from 'axios'

const phone = ref('')
const name = ref('')
const loading = ref(false)
const error = ref(null)
const dialogRef = ref(null)
const createdCustomer = ref(null)

const emit = defineEmits(['created', 'select-customer'])

function openDialog() {
  error.value = null
  phone.value = ''
  name.value = ''
  createdCustomer.value = null
  dialogRef.value.showModal()
}

function closeDialog() {
  dialogRef.value.close()
}

async function submitForm() {
  error.value = null

  if (!phone.value.trim()) {
    error.value = 'Số điện thoại là bắt buộc.'
    return
  }

  loading.value = true

  try {
    const params = new URLSearchParams()
    params.append('phone', phone.value)
    if (name.value.trim()) {
      params.append('name', name.value)
    }

    const res = await axios.post(`http://localhost:8080/api/counter-sales/quick-create-customer?${params.toString()}`)

    createdCustomer.value = res.data
    emit('created', res.data)
    closeDialog() // Đóng dialog khi tạo xong

  } catch (e) {
    error.value = e.response?.data?.message || e.message || 'Lỗi khi gọi API.'
  } finally {
    loading.value = false
  }
}

function selectCreatedCustomer() {
  emit('select-customer', createdCustomer.value)
  closeDialog()
  createdCustomer.value = null // Ẩn phần hiển thị khách hàng
}


defineExpose({ openDialog })
</script>
<style>
button.btn-primary, button.btn-secondary {
  height: 38px;
  min-height: 38px;
  line-height: 1.5;
  padding: 0 1rem;
  font-size: 1rem;
}
</style>


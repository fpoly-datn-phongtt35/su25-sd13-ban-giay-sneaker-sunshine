<template>
  <dialog ref="dialogRef" class="p-0 customer-dialog shadow-lg">
    <div class="p-4"> <form @submit.prevent="submitForm" novalidate>
        <h3 class="mb-4 text-center fw-bold">Thông tin Khách hàng Mới</h3>

        <div class="mb-3">
          <label for="phoneInput" class="form-label">
            Số điện thoại <span class="text-danger">*</span>
          </label>
          <input
            id="phoneInput"
            v-model.trim="phone" type="tel" class="form-control form-control-lg" :class="{ 'is-invalid': error && !phone }"
            placeholder="Ví dụ: 09xxxxxxxx"
            required
            autocomplete="tel"
          />
          <div class="invalid-feedback" v-if="error && !phone">
            Số điện thoại không được để trống.
          </div>
        </div>

        <div class="mb-3">
          <label for="nameInput" class="form-label">Tên khách hàng (tùy chọn)</label>
          <input
            id="nameInput"
            v-model="name"
            type="text"
            class="form-control form-control-lg"
            placeholder="Nhập tên khách hàng"
            autocomplete="name"
          />
        </div>

        <div v-if="error && phone" class="alert alert-danger py-2 mb-3">
          {{ error }}
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-secondary" @click="closeDialog" :disabled="loading">
            <i class="fas fa-times me-1"></i>Hủy
          </button>
          <button type="submit" class="btn btn-primary" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            <i v-else class="fas fa-save me-1"></i>
            {{ loading ? 'Đang xử lý...' : 'Lưu Khách Hàng' }}
          </button>
        </div>
      </form>
    </div>
  </dialog>

<div v-if="createdCustomer" class="mt-4 p-3 border rounded created-customer-info bg-light shadow-sm">
    <div class="d-flex align-items-center text-success mb-2">
      <i class="fas fa-check-circle me-2"></i> <span class="fw-semibold">Khách hàng đã được tạo:</span>
    </div>
    <p class="mb-1">Số điện thoại: {{ createdCustomer.phone }}</p>
    <p class="mb-3">Tên khách hàng: {{ createdCustomer.customerName || '(Chưa cung cấp tên)' }}</p>
    <button class="btn btn-success" @click="selectCreatedCustomer">
      <i class="fas fa-user-check me-2"></i> Sử dụng khách hàng này
    </button>
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

.customer-dialog {
  max-width: 500px;
  width: 90%; /* Chiếm 90% chiều rộng cho màn hình nhỏ */
  border-radius: 0.5rem; /* Tăng border-radius */
  border: none; /* Loại bỏ border mặc định của dialog HTML */
}

/* Tùy chọn: Thêm backdrop mờ khi dialog hiển thị (nếu bạn không dùng showModal()) */
dialog::backdrop {
  background-color: rgba(0, 0, 0, 0.4);
}

.created-customer-info {
  max-width: 500px;
  margin-left: auto; /* Căn giữa nếu container cha không giới hạn chiều rộng */
  margin-right: auto;
}

/* Làm cho input lớn hơn một chút để dễ tương tác hơn trên mobile */
.form-control-lg {
  min-height: calc(1.5em + 1rem + 2px);
  padding: 0.5rem 1rem;
  font-size: 1.1rem; /* Điều chỉnh kích thước font */
}
</style>


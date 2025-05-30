<template>
  <div>
    <h4>Tìm kiếm khách hàng</h4>
    <div class="input-group">
      <input 
        v-model="searchPhone" 
        class="form-control" 
        placeholder="Nhập số điện thoại..." 
        @input="onInputChange"
      />
      <button @click="searchCustomers" class="btn btn-primary">Tìm kiếm</button>
    </div>

    <!-- Danh sách khách hàng tìm được -->
    <div v-if="customers.length" class="mt-3">
      <div
        v-for="cus in customers"
        :key="cus.id"
        class="border p-2 rounded bg-light mb-2"
        style="cursor: pointer;"
        @click="handleSelectCustomer(cus)"
      >
        <p><strong>Tên:</strong> {{ cus.customerName }}</p>
        <p><strong>SĐT:</strong> {{ cus.phone }}</p>
      </div>
    </div>

    <!-- Nếu không tìm thấy -->
    <div v-else-if="searched" class="mt-2 text-danger">
      Không tìm thấy khách hàng nào.
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'

const toast = useToast()
const searchPhone = ref('')
const customers = ref([])
const searched = ref(false)

// Tạo emit để gửi sự kiện lên cha
const emit = defineEmits(['select-customer'])

const searchCustomers = async () => {
  const trimmed = searchPhone.value.trim()
  searched.value = false
  customers.value = []

  if (!trimmed) {
    toast.warning('Vui lòng nhập số điện thoại.')
    return
  }

  if (trimmed.length < 6) {
    toast.warning('Vui lòng nhập ít nhất 6 chữ số để tìm kiếm.')
    return
  }

  try {
    const response = await axios.get(`http://localhost:8080/api/counter-sales/search-by-phone-prefix?phone=${trimmed}`)
    customers.value = response.data || []
    searched.value = true
  } catch (error) {
    customers.value = []
    searched.value = true
    toast.error('Không thể lấy dữ liệu khách hàng.')
  }
}


// Hàm gọi khi click chọn khách hàng
const handleSelectCustomer = (cus) => {
  toast.success(`Đã chọn khách hàng: ${cus.customerName} - ${cus.phone}`)
  emit('select-customer', cus)

  // Ẩn danh sách sau khi chọn
  customers.value = []
  searched.value = false
}

const onInputChange = () => {
  if (!searchPhone.value.trim()) {
    customers.value = []
    searched.value = false
  }
}
</script>

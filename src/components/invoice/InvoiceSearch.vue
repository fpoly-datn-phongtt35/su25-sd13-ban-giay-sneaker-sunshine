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
  </div>
</template>

<script setup>
import { ref } from 'vue'

const keyword = ref('')
const status = ref('')

const emit = defineEmits(['search', 'clear'])

let debounceTimer = null

function emitSearch() {
  // Nếu status là chuỗi rỗng, gửi undefined để backend hiểu không lọc trạng thái
  const statusValue = status.value === '' ? undefined : Number(status.value)
  emit('search', { keyword: keyword.value.trim(), status: statusValue })
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

function onClearClick() {
  keyword.value = ''
  status.value = ''
  emit('clear')
}
</script>

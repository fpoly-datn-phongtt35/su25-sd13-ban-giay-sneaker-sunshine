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
      <option value="0">Chưa thanh toán</option>
      <option value="1">Đã thanh toán</option>
      <option value="2">Đơn hàng bị hủy</option>
    </select>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { defineEmits } from 'vue'

const emits = defineEmits(['search', 'clear'])

const keyword = ref('')
const status = ref('')

let debounceTimeout = null

const emitSearch = () => {
  emits('search', { keyword: keyword.value.trim(), status: status.value })
}

const onInput = () => {
  clearTimeout(debounceTimeout)
  debounceTimeout = setTimeout(() => {
    emitSearch()
  }, 300)
}

const onStatusChange = () => {
  emitSearch()
}

const onClearClick = () => {
  keyword.value = ''
  status.value = ''
  emits('clear')
}
</script>

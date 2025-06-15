<template>
  <div>
    <h4 class="mb-3">Tìm kiếm khách hàng</h4>
    <el-autocomplete
      v-model="searchPhone"
      :fetch-suggestions="searchCustomers"
      placeholder="Nhập SĐT khách hàng để tìm..."
      :trigger-on-focus="false"
      @select="handleSelectCustomer"
      class="w-100"
      size="large"
      clearable
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <template #default="{ item }">
        <div>
          <div class="fw-bold">{{ item.customerName || 'Khách lẻ' }}</div>
          <span class="text-muted small">{{ item.phone }}</span>
        </div>
      </template>
      <template #empty>
         <div class="p-3 text-muted">Không tìm thấy khách hàng nào.</div>
      </template>
    </el-autocomplete>
  </div>
</template>

<script setup>
import { ref, defineEmits } from 'vue';
// KHÔNG import axios trực tiếp ở đây nữa

// THAY ĐỔI: Import instance apiClient đã được cấu hình sẵn
// Hãy đảm bảo đường dẫn này chính xác với cấu trúc project của bạn
import apiClient from '../../utils/axiosInstance.js';

import { ElMessage, ElAutocomplete, ElIcon } from 'element-plus';
import { Search } from '@element-plus/icons-vue';

// --- State ---
const searchPhone = ref('');

// --- Emits ---
const emit = defineEmits(['select-customer']);

/**
 * Debounce function to delay execution.
 * @param {Function} func - The function to debounce.
 * @param {number} delay - The delay in milliseconds.
 * @returns {Function} - The debounced function.
 */
const debounce = (func, delay) => {
  let timeout;
  return (...args) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), delay);
  };
};

/**
 * Fetches customer suggestions from the API based on the query string.
 * This is used by the el-autocomplete component.
 * @param {string} queryString - The phone number prefix to search for.
 * @param {Function} cb - The callback function to return the suggestions.
 */
const searchCustomers = debounce(async (queryString, cb) => {
  if (!queryString || queryString.trim().length < 1) {
    cb([]); // Return empty if query is too short
    return;
  }

  try {
    // THAY ĐỔI: Sử dụng apiClient và rút gọn URL
    const response = await apiClient.get(`/admin/counter-sales/search-by-phone-prefix?phone=${queryString.trim()}`);
    cb(response.data || []); // Pass the results to the autocomplete callback
  } catch (error) {
    console.error("Error fetching customers:", error);
    ElMessage.error('Không thể tải danh sách khách hàng.');
    cb([]); // Return empty on error
  }
}, 300); // 300ms delay

/**
 * Handles the selection of a customer from the autocomplete list.
 * @param {object} customer - The selected customer object.
 */
const handleSelectCustomer = (customer) => {
  if (customer) {
    ElMessage.success(`Đã chọn khách hàng: ${customer.customerName || customer.phone}`);
    emit('select-customer', customer);
    searchPhone.value = ''; // Clear input after selection
  }
};
</script>

<style scoped>
.w-100 {
  width: 100%;
}
.mb-3 {
  margin-bottom: 1rem;
}
.p-3 {
    padding: 1rem;
}
.fw-bold {
    font-weight: 600;
}
.text-muted {
    color: var(--el-text-color-secondary);
}
.small {
    font-size: 0.875em;
}
</style>

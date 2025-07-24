<template>
  <div class="voucher-list p-4">
    <h2 class="text-lg font-semibold mb-4">Voucher của bạn</h2>

    <!-- Ưu đãi cá nhân hóa -->
    <el-card v-if="promotionSuggestion" class="mb-4">
      <template #header>
        <span class="font-bold text-base">Ưu đãi dành riêng cho bạn</span>
      </template>

      <p><strong>Hạng khách hàng:</strong> 
        <el-tag type="warning" class="ml-2">{{ promotionSuggestion.tier }}</el-tag>
      </p>
      <p class="mt-2"><strong>Thông điệp:</strong> {{ promotionSuggestion.message }}</p>
      <p><strong>Đơn hàng đã mua:</strong> {{ promotionSuggestion.invoiceCount }}</p>
    </el-card>

    <!-- Danh sách voucher -->
    <el-table v-if="vouchers.length" :data="vouchers" border style="width: 100%">
      <el-table-column prop="voucherCode" label="Mã" />
      <el-table-column prop="voucherName" label="Tên voucher" />
      <el-table-column prop="discountAmount" label="Giảm giá (VNĐ)">
        <template #default="{ row }">
          {{ formatCurrency(row.discountAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="minOrderValue" label="Tối thiểu đơn hàng">
        <template #default="{ row }">
          {{ formatCurrency(row.minOrderValue) }}
        </template>
      </el-table-column>
      <el-table-column prop="maxDiscountValue" label="Giảm tối đa">
        <template #default="{ row }">
          {{ formatCurrency(row.maxDiscountValue) }}
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="Bắt đầu" />
      <el-table-column prop="endDate" label="Kết thúc" />
      <el-table-column prop="status" label="Trạng thái">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? 'Hoạt động' : 'Hết hạn' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <p v-else class="text-gray-500 mt-4">Bạn hiện không có voucher nào.</p>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const vouchers = ref([])
const promotionSuggestion = ref(null)

const formatCurrency = (value) => {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}

const fetchVouchers = async () => {
  const customerId = localStorage.getItem('userId')
  if (!customerId) {
    ElMessage.warning('Không tìm thấy thông tin người dùng.')
    return
  }

  try {
    const res = await axios.get(`http://localhost:8080/api/admin/vouchers/by-customer/${customerId}`)
    vouchers.value = res.data
  } catch (err) {
    ElMessage.error('Không thể tải danh sách voucher.')
  }
}

const fetchPromotionSuggestion = async () => {
  const customerId = localStorage.getItem('userId')
  if (!customerId) return

  try {
    const res = await axios.get(`http://localhost:8080/api/online-sale/suggest-promotion/${customerId}`)
    promotionSuggestion.value = res.data
  } catch (err) {
    console.error('Lỗi khi lấy gợi ý khuyến mãi:', err)
  }
}

onMounted(() => {
  fetchVouchers()
  fetchPromotionSuggestion()
})
</script>

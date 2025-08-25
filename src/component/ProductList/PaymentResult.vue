<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const status = ref(null)
const message = ref('⏳ Đang kiểm tra trạng thái thanh toán...')

// Enum chuỗi trạng thái trả về từ backend
const STATUS = {
  DANG_XU_LY: 'DANG_XU_LY',
  HUY_DON: 'HUY_DON',
  THAT_BAI: 'THAT_BAI',
  HUY_GIAO_DICH: 'HUY_GIAO_DICH'
}

// Thông báo tương ứng theo trạng thái
const STATUS_MESSAGES = {
  [STATUS.THAT_BAI]: '❌ Thanh toán thất bại!',
  [STATUS.HUY_DON]: '🚫 Đơn hàng đã bị hủy!',
  [STATUS.DANG_XU_LY]: '✅ Thanh toán thành công!',
  [STATUS.HUY_GIAO_DICH]: '⛔ Giao dịch đã bị hủy!'
}

onMounted(async () => {
  const appTransId = route.query.app_trans_id

  if (!appTransId) {
    message.value = '❌ Không tìm thấy mã giao dịch trong URL'
    ElMessage.error(message.value)
    return
  }

  try {
    // Bước 1: Đồng bộ trạng thái mới nhất từ ZaloPay
    await axios.get('http://localhost:8080/api/payment/zalo/status-check', {
      params: { appTransId }
    })

    // Bước 2: Lấy trạng thái đơn hàng từ DB
    const res = await axios.get('http://localhost:8080/api/payment/zalo/invoice/status', {
      params: { appTransId }
    })

    status.value = res.data?.status
    console.log('🧾 Trạng thái hóa đơn từ server:', status.value)

    if (status.value && STATUS_MESSAGES[status.value]) {
      message.value = STATUS_MESSAGES[status.value]

      // Phân loại hiển thị
      if ([STATUS.DANG_XU_LY].includes(status.value)) {
        ElMessage.success(message.value)
      } else if ([STATUS.THAT_BAI, STATUS.HUY_DON, STATUS.HUY_GIAO_DICH].includes(status.value)) {
        ElMessage.error(message.value)
      } else {
        ElMessage.warning(message.value)
      }

    } else {
      message.value = `❓ Không xác định trạng thái đơn hàng: ${status.value || 'null'}`
      console.warn('⚠️ Trạng thái không xác định:', res.data)
      ElMessage.warning(message.value)
    }

  } catch (err) {
    console.error('❌ Lỗi khi kiểm tra trạng thái:', err)
    message.value = '❌ Không thể kiểm tra trạng thái đơn hàng'
    ElMessage.error(message.value)
  }
})
</script>

<template>
  <div class="p-6 text-center">
    <h2 class="text-2xl font-bold mb-4">Kết quả thanh toán</h2>
    <p class="text-lg">{{ message }}</p>
  </div>
</template>

<style scoped>
p {
  font-size: 18px;
}
</style>

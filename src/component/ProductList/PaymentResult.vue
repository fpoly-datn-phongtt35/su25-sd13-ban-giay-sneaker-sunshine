<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const status = ref(null)
const message = ref('⏳ Đang kiểm tra trạng thái thanh toán...')

onMounted(async () => {
  const appTransId = route.query.app_trans_id

  if (!appTransId) {
    message.value = '❌ Không tìm thấy mã giao dịch trong URL'
    ElMessage.error(message.value)
    return
  }

  try {
    // ✅ Gọi API để cập nhật trạng thái mới nhất từ ZaloPay
    await axios.get('http://localhost:8080/api/payment/zalo/status-check', {
      params: { appTransId }
    })

    // ✅ Gọi API lấy trạng thái hóa đơn từ DB
    const res = await axios.get('http://localhost:8080/api/payment/zalo/invoice/status', {
      params: { appTransId }
    })

    status.value = res.data?.status

    if (status.value === 1) {
      message.value = '✅ Thanh toán thành công!'
      ElMessage.success(message.value)
    } else if (status.value === 11) {
      message.value = '❌ Thanh toán thất bại hoặc bị hủy!'
      ElMessage.error(message.value)
    } else {
      message.value = '⏳ Đơn hàng đang xử lý...'
      ElMessage.warning(message.value)
    }
  } catch (err) {
    console.error(err)
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

<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()

onMounted(async () => {
  const appTransId = route.query.app_trans_id

  if (!appTransId) {
    ElMessage.error('Không tìm thấy mã giao dịch')
    return
  }

  try {
    const res = await axios.get(`http://localhost:8080/api/payment/zalo/invoice/status?appTransId=${appTransId}`)
    const status = res.data?.status

    if (status === 1) {
      ElMessage.success('✅ Thanh toán thành công!')
    } else if (status === 11) {
      ElMessage.error('❌ Thanh toán thất bại hoặc bị hủy!')
    } else {
      ElMessage.warning('⏳ Đơn hàng đang xử lý...')
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('❌ Không thể kiểm tra trạng thái đơn hàng')
  }
})
</script>

<template>
  <div class="p-6">
    <h2 class="text-xl font-bold mb-4">Kết quả thanh toán</h2>
    <p>Vui lòng chờ vài giây...</p>
  </div>
</template>

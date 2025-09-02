<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
// Import các icon cần thiết từ Element Plus
import { InfoFilled } from '@element-plus/icons-vue'

// --- BIẾN & TRẠNG THÁI (GIỮ NGUYÊN) ---
const route = useRoute()
const isLoading = ref(true)
const orderStatus = ref(null)
const appTransId = ref(route.query.app_trans_id)

// --- CẤU HÌNH TRẠNG THÁI (GIỮ NGUYÊN) ---
const STATUS_KEYS = {
  SUCCESS: 'DANG_XU_LY',
  FAILED: 'THAT_BAI',
  ORDER_CANCELLED: 'HUY_DON',
  TRANSACTION_CANCELLED: 'HUY_GIAO_DICH',
  PENDING: 'CHO_XU_LY'
}

const STATUS_CONFIG = {
  [STATUS_KEYS.SUCCESS]: {
    title: 'Thanh toán thành công!',
    message: 'Cảm ơn bạn đã mua hàng. Đơn hàng của bạn đang được chuẩn bị để giao đi sớm nhất.',
    // icon của ElResult chấp nhận: 'success', 'warning', 'error', 'info'
    icon: 'success',
  },
  [STATUS_KEYS.FAILED]: {
    title: 'Thanh toán thất bại',
    message: 'Đã có lỗi xảy ra trong quá trình thanh toán. Vui lòng kiểm tra lại thông tin hoặc thử lại sau.',
    icon: 'error',
  },
  [STATUS_KEYS.ORDER_CANCELLED]: {
    title: 'Đơn hàng đã bị hủy',
    message: 'Đơn hàng này đã được hủy trước đó. Vui lòng liên hệ hỗ trợ nếu bạn cần giúp đỡ.',
    icon: 'warning',
  },
  [STATUS_KEYS.TRANSACTION_CANCELLED]: {
    title: 'Giao dịch đã bị hủy',
    message: 'Bạn đã hủy giao dịch thanh toán. Bạn có thể quay lại giỏ hàng để thực hiện lại.',
    icon: 'warning',
  },
  [STATUS_KEYS.PENDING]: {
    title: 'Đang chờ xử lý',
    message: 'Hệ thống đang xử lý giao dịch của bạn. Vui lòng chờ trong giây lát.',
    // Map 'pending' của chúng ta sang 'info' của Element Plus
    icon: 'info',
  },
  defaultError: {
    title: 'Đã xảy ra lỗi',
    message: 'Không thể kiểm tra trạng thái đơn hàng. Vui lòng liên hệ bộ phận hỗ trợ khách hàng.',
    icon: 'error',
  }
}

// Computed property để lấy thông tin hiển thị (GIỮ NGUYÊN)
const currentStatusInfo = computed(() => {
  if (isLoading.value) return {} // Trả về object rỗng để không lỗi
  return STATUS_CONFIG[orderStatus.value] || STATUS_CONFIG.defaultError
})

// --- LOGIC XỬ LÝ (GIỮ NGUYÊN) ---
onMounted(async () => {
  if (!appTransId.value) {
    orderStatus.value = 'ERROR_NO_ID'
    isLoading.value = false
    return
  }
  try {
    await axios.get('http://localhost:8080/api/payment/zalo/status-check', {
      params: { appTransId: appTransId.value }
    })
    const res = await axios.get('http://localhost:8080/api/payment/zalo/invoice/status', {
      params: { appTransId: appTransId.value }
    })
    orderStatus.value = res.data?.status
  } catch (err) {
    console.error('❌ Lỗi khi kiểm tra trạng thái:', err)
    orderStatus.value = 'ERROR_API_CALL'
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="payment-result-page">
    <el-card
      class="result-card"
      shadow="hover"
      v-loading="isLoading"
      element-loading-text="Đang kiểm tra thanh toán, vui lòng chờ..."
    >
      <el-result
        v-if="!isLoading"
        :icon="currentStatusInfo.icon"
        :title="currentStatusInfo.title"
        :sub-title="currentStatusInfo.message"
      >
        <template #icon>
            <el-icon v-if="currentStatusInfo.icon === 'info'" :size="80" color="#409eff">
                <InfoFilled />
            </el-icon>
        </template>
        

        <template #extra>
          <div class="actions">
            <template v-if="orderStatus === STATUS_KEYS.SUCCESS">
              <RouterLink to="/don-hang">
                <el-button type="primary" size="large">Xem đơn hàng</el-button>
              </RouterLink>
              <RouterLink to="/">
                <el-button size="large">Tiếp tục mua sắm</el-button>
              </RouterLink>
            </template>

            <template v-else-if="[STATUS_KEYS.FAILED, STATUS_KEYS.TRANSACTION_CANCELLED].includes(orderStatus)">
              <RouterLink to="/gio-hang">
                <el-button type="primary" size="large">Thử lại thanh toán</el-button>
              </RouterLink>
              <RouterLink to="/">
                <el-button size="large">Quay về trang chủ</el-button>
              </RouterLink>
            </template>

            <template v-else>
              <RouterLink to="/">
                <el-button type="primary" size="large">Quay về trang chủ</el-button>
              </RouterLink>
            </template>
          </div>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<style scoped>
/* Giữ lại style cho layout chính */
.payment-result-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  background-color: #f7f8fa;
  padding: 20px;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.result-card {
  max-width: 550px;
  width: 100%;
  min-height: 400px; /* Đảm bảo card có chiều cao tối thiểu khi loading */
  border-radius: 16px;
  /* ElCard đã có box-shadow, không cần custom */
}

/* Tùy chỉnh một chút cho các component của Element Plus nếu cần */
.el-result {
    padding: 40px 20px;
}

.message {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 24px;
}

/* Giữ lại style cho phần order-details */
.order-details {
  background-color: #f7f8fa;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 0 auto 32px; /* Canh giữa và tạo khoảng cách */
  display: inline-flex;
  gap: 8px;
  font-size: 16px;
}
.order-details span {
  color: #909399;
}
.order-details strong {
  color: #303133;
  font-weight: 600;
}

/* Giữ lại style cho các nút actions */
.actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* Responsive adjustments */
@media (max-width: 600px) {
  .result-card {
    padding: 0;
  }
  .actions {
    flex-direction: column;
    gap: 12px;
  }
  .actions .el-button {
    width: 100%;
  }
}
</style>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'

// ===== Router & Query =====
const route = useRoute()
const router = useRouter()
const appTransId = ref(route.query.app_trans_id) // ZaloPay online
const invoiceCode = ref(route.query.invoiceCode) // Shipcode

// ===== Trạng thái trang =====
const isLoading = ref(true)
const orderStatus = ref(null) // server trả về, ví dụ: DANG_XU_LY, THAT_BAI,...

// ===== Hằng số trạng thái hiển thị =====
const STATUS_KEYS = {
  SUCCESS: 'DANG_XU_LY',            // tuỳ theo backend bạn map trường hợp "thanh toán thành công" -> đọc status service
  FAILED: 'THAT_BAI',
  ORDER_CANCELLED: 'HUY_DON',
  TRANSACTION_CANCELLED: 'HUY_GIAO_DICH',
  PENDING: 'CHO_XU_LY',
}

const STATUS_CONFIG = {
  [STATUS_KEYS.SUCCESS]: {
    title: 'Thanh toán thành công!',
    message: 'Cảm ơn bạn đã mua hàng. Đơn hàng đang được chuẩn bị để giao đi sớm nhất.',
    icon: 'success',
  },
  [STATUS_KEYS.FAILED]: {
    title: 'Thanh toán thất bại',
    message: 'Đã có lỗi trong quá trình thanh toán. Vui lòng thử lại.',
    icon: 'error',
  },
  [STATUS_KEYS.ORDER_CANCELLED]: {
    title: 'Đơn hàng đã bị hủy',
    message: 'Đơn hàng này đã được hủy trước đó. Liên hệ hỗ trợ nếu cần giúp đỡ.',
    icon: 'warning',
  },
  [STATUS_KEYS.TRANSACTION_CANCELLED]: {
    title: 'Giao dịch đã bị hủy',
    message: 'Bạn đã hủy giao dịch thanh toán. Có thể quay lại giỏ hàng để thực hiện lại.',
    icon: 'warning',
  },
  [STATUS_KEYS.PENDING]: {
    title: 'Đang chờ xử lý',
    message: 'Hệ thống đang xử lý giao dịch của bạn. Vui lòng chờ trong giây lát.',
    icon: 'info',
  },
  defaultError: {
    title: 'Đã xảy ra lỗi',
    message: 'Không thể kiểm tra trạng thái đơn hàng. Vui lòng liên hệ CSKH.',
    icon: 'error',
  },
}

// ===== Tính thông tin hiển thị theo status =====
const currentStatusInfo = computed(() => {
  if (isLoading.value) return {}
  return STATUS_CONFIG[orderStatus.value] || STATUS_CONFIG.defaultError
})

// ===== Helper đăng nhập =====
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const handleViewOrders = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('Vui lòng đăng nhập để sử dụng tính năng này.')
    router.push({ path: '/dang-nhap', query: { redirect: '/don-hang' } })
    return
  }
  router.push('/don-hang')
}

// ===== Gọi API kiểm tra kết quả thanh toán =====
onMounted(async () => {
  try {
    if (appTransId.value) {
      // ZaloPay online
      await axios.get('http://localhost:8080/api/payment/zalo/status-check', {
        params: { appTransId: appTransId.value },
      })
      const res = await axios.get('http://localhost:8080/api/payment/zalo/invoice/status', {
        params: { appTransId: appTransId.value },
      })
      orderStatus.value = res.data?.status
    } else if (invoiceCode.value) {
      // Shipcode
      const res = await axios.get('http://localhost:8080/api/payment/zalo/invoice/shipcode/status', {
        params: { invoiceCode: invoiceCode.value },
      })
      orderStatus.value = res.data?.status
    } else {
      orderStatus.value = 'ERROR_NO_ID'
    }
  } catch (e) {
    console.error('Lỗi khi kiểm tra trạng thái:', e)
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
          <el-icon v-if="currentStatusInfo.icon === 'info'" :size="80">
            <InfoFilled />
          </el-icon>
        </template>

        <template #extra>
          <div class="actions">
            <!-- Thành công -> cho xem đơn hàng (yêu cầu đăng nhập) -->
            <template v-if="orderStatus === STATUS_KEYS.SUCCESS">
              <el-button type="primary" size="large" @click="handleViewOrders">
                Xem đơn hàng
              </el-button>
              <router-link to="/">
                <el-button size="large">Tiếp tục mua sắm</el-button>
              </router-link>
            </template>

            <!-- Thất bại / hủy giao dịch -->
            <template v-else-if="[STATUS_KEYS.FAILED, STATUS_KEYS.TRANSACTION_CANCELLED].includes(orderStatus)">
              <router-link to="/gio-hang">
                <el-button type="primary" size="large">Thử lại thanh toán</el-button>
              </router-link>
              <router-link to="/">
                <el-button size="large">Quay về trang chủ</el-button>
              </router-link>
            </template>

            <!-- Mặc định -->
            <template v-else>
              <router-link to="/">
                <el-button type="primary" size="large">Quay về trang chủ</el-button>
              </router-link>
            </template>
          </div>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<style scoped>
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
  min-height: 400px;
  border-radius: 16px;
}

.el-result {
  padding: 40px 20px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

@media (max-width: 600px) {
  .result-card { padding: 0; }
  .actions { flex-direction: column; gap: 12px; }
  .actions .el-button { width: 100%; }
}
</style>

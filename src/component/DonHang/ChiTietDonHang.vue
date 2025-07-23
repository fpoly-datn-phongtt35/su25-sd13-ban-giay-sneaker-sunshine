<template>
  <div class="p-4 full-screen">
    <el-button type="primary" icon="el-icon-arrow-left" @click="goBack" class="mb-4">
      Quay lại
    </el-button>

    <el-card>
      <h3>Tiến trình đơn hàng</h3>

      <el-steps
        v-if="['HUY_DON'].includes(invoice.statusDetail)"
        :active="0"
        align-center
        finish-status="error"
      >
        <el-step
          :title="'Đã hủy'"
          status="error"
        />
      </el-steps>

      <el-steps
        v-else
        :active="getActiveStep(invoice.statusDetail)"
        finish-status="success"
        align-center
      >
        <el-step
          v-for="step in mainSteps"
          :key="step.key"
          :title="step.label"
        />
      </el-steps>

      <div class="mt-4 flex flex-wrap gap-2">
        <el-button type="danger" @click="showCancelDialog" v-if="canCancel">Hủy đơn hàng</el-button>
      </div>

      <el-divider />

      <h2>Thông tin đơn hàng</h2>
      <p><strong>Mã hóa đơn:</strong> {{ invoice.invoiceCode }}</p>
      <p><strong>Khách hàng:</strong> {{ invoice.customerName }}</p>
      <p><strong>Ngày tạo:</strong> {{ formatDate(invoice.createdDate) }}</p>
      <p><strong>Tổng tiền:</strong> {{ formatCurrency(invoice.totalAmount) }}</p>
      <p><strong>Giảm giá:</strong> {{ formatCurrency(invoice.discountAmount) }}</p>
      <p><strong>Phí vận chuyển:</strong> {{ formatCurrency(invoice.shippingFee) }}</p>
      <p><strong>Thành tiền:</strong> {{ formatCurrency(invoice.finalAmount) }}</p>

      <el-divider />

      <h3>Lịch sử thanh toán</h3>
      <el-table :data="invoice.invoiceTransactionResponses || []" border stripe>
        <el-table-column label="STT" width="70" type="index" />
        <el-table-column prop="transactionCode" label="Mã giao dịch" />
        <el-table-column prop="transactionType" label="Loại giao dịch" />
        <el-table-column prop="paymentMethod" label="PT Thanh toán" />
        <el-table-column prop="paymentTime" label="Thời gian" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="transactionNote" label="Ghi chú" />
      </el-table>

      <el-divider />

      <h3>Sản phẩm đã mua</h3>
      <el-table :data="invoice.invoiceDetailResponses || []" border stripe>
        <el-table-column label="STT" width="70" type="index" />
        <el-table-column prop="productDetailName" label="Tên sản phẩm" />
        <el-table-column prop="sizeName" label="Kích thước" />
        <el-table-column prop="colorName" label="Màu sắc" />
        <el-table-column prop="quantity" label="Số lượng" />
      </el-table>
    </el-card>

    <el-dialog title="Lý do hủy đơn hàng" v-model="cancelDialogVisible" width="400px">
      <el-form label-position="top">
        <el-form-item label="Lý do hủy đơn">
          <el-input type="textarea" v-model="cancelNote" placeholder="Nhập lý do hủy..." rows="3" />
        </el-form-item>
        <el-form-item label="Phương thức hoàn tiền">
          <el-select v-model="selectedPaymentMethod" placeholder="Chọn phương thức hoàn tiền">
            <el-option label="Tiền mặt" value="TIEN_MAT" />
            <el-option label="ZaloPay" value="ZALOPAY" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">Hủy</el-button>
        <el-button type="danger" @click="cancelOrder">Xác nhận hủy đơn</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiClient from '@/utils/axiosInstance'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const invoiceId = route.params.id
const invoice = ref({})
const cancelDialogVisible = ref(false)
const cancelNote = ref('')
const selectedPaymentMethod = ref('TIEN_MAT')

const mainSteps = [
  { key: 'CHO_XU_LY', label: 'Chờ xử lý' },
  { key: 'DA_XU_LY', label: 'Đã xử lý' },
  { key: 'CHO_GIAO_HANG', label: 'Chờ giao hàng' },
  { key: 'DANG_GIAO_HANG', label: 'Đang giao hàng' },
  { key: 'GIAO_THANH_CONG', label: 'Giao thành công' },
  { key: 'DA_HOAN_THANH', label: 'Hoàn tất' }
]
const mainStepKeys = mainSteps.map(s => s.key)

const getActiveStep = (statusKey) => {
  return mainStepKeys.indexOf(statusKey || '')
}

const canCancel = computed(() => {
  const status = invoice.value?.statusDetail
  return mainStepKeys.includes(status) && mainStepKeys.indexOf(status) < mainStepKeys.indexOf('DANG_GIAO_HANG')
})

const fetchInvoice = async () => {
  try {
    const res = await apiClient.get(`/admin/online-sales/get-order-customer-detail`, {
      params: { invoiceId: invoiceId }
    })
    invoice.value = res.data
  } catch (err) {
    ElMessage.error('Lỗi khi tải thông tin đơn hàng.')
    console.error(err)
  }
}

const showCancelDialog = () => {
  cancelNote.value = ''
  selectedPaymentMethod.value = 'TIEN_MAT'
  cancelDialogVisible.value = true
}

const cancelOrder = async () => {
  try {
    if (!cancelNote.value.trim()) {
      ElMessage.warning('Vui lòng nhập lý do hủy đơn!')
      return
    }
    if (!selectedPaymentMethod.value) {
      ElMessage.warning('Vui lòng chọn phương thức hoàn tiền!')
      return
    }

    await apiClient.put(`/admin/online-sales/huy-don-va-hoan-tien`, null, {
      params: {
        invoiceId,
        statusDetail: 'HUY_DON',
        note: cancelNote.value,
        paymentMethod: selectedPaymentMethod.value
      }
    })

    ElMessage.success('Hủy đơn hàng và hoàn tiền thành công!')
    cancelDialogVisible.value = false
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi hủy đơn hàng.')
    console.error(err)
  }
}

const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('vi-VN') : ''
const formatCurrency = (val) => `${Number(val || 0).toLocaleString('vi-VN')} ₫`
const goBack = () => router.back()

onMounted(fetchInvoice)
</script>

<style scoped>
.full-screen {
  max-width: 100%;
}
.mt-4 {
  margin-top: 20px;
}
.el-card {
  max-width: 100%;
  margin: auto;
}
.el-dialog {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.el-dialog__header {
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.el-dialog__title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
}
.el-form-item__label {
  font-size: 0.95rem;
  font-weight: 500;
  color: #555;
}
</style>

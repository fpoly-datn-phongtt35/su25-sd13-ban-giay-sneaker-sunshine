<template>
  <div class="p-4 full-screen">
    <el-button type="primary" icon="el-icon-arrow-left" @click="goBack" class="mb-4">
      Quay lại
    </el-button>

    <el-card>
      <h3>Tiến trình đơn hàng</h3>

      <el-steps
        v-if="['HUY_DON', 'GIAO_THAT_BAI'].includes(invoice.statusDetail)"
        :active="0"
        align-center
        finish-status="error"
      >
        <el-step
          :title="invoice.statusDetail === 'HUY_DON' ? 'Đã hủy' : 'Giao hàng thất bại'"
          status="error"
        />
      </el-steps>

      <el-steps
        v-else
        :active="getActiveStep(invoice.statusDetail)"
        finish-status="success"
        align-center
      >
        <el-step v-for="step in mainSteps" :key="step.key" :title="step.label" />
      </el-steps>

      <div class="mt-4 flex flex-wrap gap-2">
        <el-button type="success" @click="confirmAdvance" v-if="canAdvance"
          >Chuyển trạng thái tiếp theo</el-button
        >
        <el-button type="warning" @click="confirmRevert" v-if="canRevert"
          >Quay lại trạng thái trước</el-button
        >
        <el-button type="danger" @click="showCancelDialog" v-if="canCancel">Hủy đơn hàng</el-button>
        <el-button
          type="danger"
          @click="showFailDialog"
          v-if="invoice.statusDetail === 'DANG_GIAO_HANG'"
          >Giao hàng thất bại</el-button
        >
        <el-button @click="showActionHistoryDialog">Lịch sử tác động</el-button>
      </div>

      <el-divider />

      <h2>Thông tin đơn hàng</h2>
      <p><strong>Mã hóa đơn:</strong> {{ invoice.invoiceCode }}</p>
      <p><strong>Khách hàng:</strong> {{ invoice.customerName }}</p>
      <p><strong>Số điện thoại:</strong> {{ invoice.phone }}</p>
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

        <!-- Cột giá (hiển thị giá gốc & giảm nếu có) -->
        <el-table-column label="Giá" align="center">
          <template #default="{ row }">
            <div>
              <span
                v-if="row.sellPrice !== row.discountedPrice"
                style="text-decoration: line-through; color: gray; margin-right: 6px"
              >
                {{ formatCurrency(row.sellPrice) }}
              </span>
              <span style="color: red; font-weight: bold">
                {{ formatCurrency(row.discountedPrice) }}
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="Hủy đơn hàng" v-model="cancelDialogVisible" width="500px">
      <!-- Ghi chú -->
      <div class="mb-3">
        <label class="font-medium">Lý do hủy đơn hàng</label>
        <el-input
          type="textarea"
          v-model="cancelNote"
          placeholder="Nhập lý do hủy đơn hàng..."
          rows="3"
        />
      </div>

      <!-- Phương thức hoàn tiền (chỉ hiện khi đã thanh toán) -->
      <div v-if="isPaid">
        <label class="font-medium">Phương thức hoàn tiền</label>
        <el-select
          v-model="selectedPaymentMethod"
          placeholder="Chọn phương thức hoàn tiền"
          class="w-full"
        >
          <el-option label="Tiền mặt" value="TIEN_MAT" />
          <el-option label="ZaloPay" value="ZALO_PAY" />
        </el-select>
      </div>

      <template #footer>
        <el-button @click="cancelDialogVisible = false">Hủy</el-button>
        <el-button type="danger" @click="cancelOrder">Xác nhận hủy đơn</el-button>
      </template>
    </el-dialog>

    <el-dialog title="Giao hàng thất bại" v-model="failDialogVisible" width="500px">
      <div class="mb-3">
        <label class="font-medium">Lý do giao hàng thất bại</label>
        <el-input
          type="textarea"
          v-model="failNote"
          placeholder="Nhập lý do giao hàng thất bại..."
          rows="3"
        />
      </div>

      <!-- Phương thức thanh toán - chỉ hiện khi đơn đã thanh toán -->
      <div v-if="isPaid">
        <label class="font-medium">Phương thức thanh toán</label>
        <el-select
          v-model="selectedPaymentMethod"
          placeholder="Chọn phương thức thanh toán"
          class="w-full"
        >
          <el-option label="Tiền mặt" value="TIEN_MAT" />
          <el-option label="ZaloPay" value="ZALO_PAY" />
        </el-select>
      </div>

      <template #footer>
        <el-button @click="failDialogVisible = false">Hủy</el-button>
        <el-button type="danger" @click="markAsFailedDelivery">Xác nhận</el-button>
      </template>
    </el-dialog>

    <el-dialog title="Lịch sử tác động đơn hàng" v-model="actionHistoryDialogVisible" width="800px">
      <el-table :data="actionHistory" border stripe>
        <el-table-column label="STT" width="70" type="index" />
        <el-table-column prop="oldStatus" label="Trạng thái cũ">
          <template #default="scope">
            {{ getStatusLabelFromInt(scope.row.oldStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="newStatus" label="Trạng thái mới">
          <template #default="scope">
            {{ getStatusLabelFromInt(scope.row.newStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="changedAt" label="Thời gian" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.changedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="note" label="Ghi chú" />
        <el-table-column prop="employeeName" label="Người thực hiện" />
      </el-table>
      <template #footer>
        <el-button @click="actionHistoryDialogVisible = false">Đóng</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiClient from '@/utils/axiosInstance'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const invoiceId = route.params.invoiceId
const invoice = ref({})
const cancelDialogVisible = ref(false)
const cancelNote = ref('')
const selectedPaymentMethod = ref('')

const failDialogVisible = ref(false)
const failNote = ref('')

const actionHistoryDialogVisible = ref(false)
const actionHistory = ref([])

const isPaid = ref(false)

const mainSteps = [
  { key: 'CHO_XU_LY', label: 'Chờ xử lý' },
  { key: 'DA_XU_LY', label: 'Đã xử lý' },
  { key: 'CHO_GIAO_HANG', label: 'Chờ giao hàng' },
  { key: 'DANG_GIAO_HANG', label: 'Đang giao hàng' },
  { key: 'GIAO_THANH_CONG', label: 'Giao thành công' },
]
const mainStepKeys = mainSteps.map((s) => s.key)

const getActiveStep = (statusKey) => {
  return mainStepKeys.indexOf(statusKey || '')
}

const getStatusLabelFromInt = (statusInt) => {
  if (statusInt === -1) {
    return 'Đã hủy'
  }

  if (typeof statusInt === 'number' && statusInt >= 0 && statusInt < mainSteps.length) {
    return mainSteps[statusInt].label
  }
  return `Không xác định (${statusInt})`
}

const canAdvance = computed(() => {
  const status = invoice.value?.statusDetail
  return mainStepKeys.includes(status) && status !== 'DA_HOAN_THANH'
})

const canRevert = computed(() => {
  const status = invoice.value?.statusDetail
  const idx = mainStepKeys.indexOf(status)
  return idx > 0 && idx < mainStepKeys.indexOf('DANG_GIAO_HANG') && mainStepKeys.includes(status)
})

const canCancel = computed(() => {
  const status = invoice.value?.statusDetail
  return (
    mainStepKeys.includes(status) &&
    mainStepKeys.indexOf(status) < mainStepKeys.indexOf('DANG_GIAO_HANG')
  )
})

const fetchInvoice = async () => {
  try {
    const res = await apiClient.get(`/admin/online-sales/get-order`, {
      params: { invoiceId },
    })
    invoice.value = res.data
    isPaid.value = res.data.isPaid
    console.log('data: ', invoice.value)
    console.log('ispaid: ', isPaid.value)
  } catch (err) {
    ElMessage.error('Lỗi tải đơn hàng')
    console.error(err)
  }
}

const confirmAdvance = () => {
  ElMessageBox.confirm('Bạn có chắc muốn chuyển sang trạng thái tiếp theo?', 'Xác nhận', {
    type: 'warning',
  }).then(advanceStatus)
}

const confirmRevert = () => {
  ElMessageBox.confirm('Bạn có chắc muốn quay lại trạng thái trước?', 'Xác nhận', {
    type: 'warning',
  }).then(revertStatus)
}

const showCancelDialog = () => {
  cancelNote.value = ''
  cancelDialogVisible.value = true
}
const showFailDialog = () => {
  failNote.value = ''
  failDialogVisible.value = true
}

const showActionHistoryDialog = async () => {
  try {
    const res = await apiClient.get(`/admin/online-sales/get-order-history`, {
      params: { invoiceId },
    })
    actionHistory.value = res.data
    console.log('data his: ',res.data)
    actionHistoryDialogVisible.value = true
  } catch (err) {
    ElMessage.error('Lỗi tải lịch sử tác động')
    console.error(err)
  }
}

const advanceStatus = async () => {
  const currentKey = invoice.value?.statusDetail
  const currentIndex = mainStepKeys.indexOf(currentKey)
  const nextKey = mainStepKeys[currentIndex + 1]

  console.log(`Chuyển từ ${currentKey} → ${nextKey}`)

  if (!nextKey) {
    ElMessage.info('Không có trạng thái tiếp theo để chuyển.')
    return
  }

  try {
    await apiClient.put(`/admin/online-sales/chuyen-trang-thai`, null, {
      params: { invoiceId, statusDetail: nextKey },
    })
    ElMessage.success('Chuyển trạng thái thành công!')
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi chuyển trạng thái')
    console.error(err)
  }
}

const revertStatus = async () => {
  const currentKey = invoice.value?.statusDetail
  const currentIndex = mainStepKeys.indexOf(currentKey)
  const prevKey = mainStepKeys[currentIndex - 1]

  if (!prevKey) {
    ElMessage.info('Không có trạng thái trước đó để quay lại.')
    return
  }

  try {
    await apiClient.put(`/admin/online-sales/chuyen-trang-thai`, null, {
      params: { invoiceId, statusDetail: prevKey },
    })
    ElMessage.success('Quay lại trạng thái trước thành công!')
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi quay lại trạng thái')
    console.error(err)
  }
}

const cancelOrder = async () => {
  try {
    if (!cancelNote.value.trim()) {
      ElMessage.warning('Vui lòng nhập lý do hủy đơn!')
      return
    }
     if (isPaid.value && !selectedPaymentMethod.value) {
    ElMessage.warning('Vui lòng chọn phương thức hoàn tiền!')
    return
  }

    await apiClient.put(`/admin/online-sales/huy-don-va-hoan-tien`, null, {
      params: {
        invoiceId,
        statusDetail: 'HUY_DON',
        note: cancelNote.value,
        paymentMethod: selectedPaymentMethod.value || '',
        isPaid: isPaid.value
      },
    })

    ElMessage.success('Hủy đơn hàng và hoàn tiền thành công!')
    cancelDialogVisible.value = false
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi hủy đơn hàng')
    console.error(err)
  }
}

const markAsFailedDelivery = async () => {
  try {
    if (!failNote.value.trim()) {
      ElMessage.warning('Vui lòng nhập lý do giao hàng thất bại!')
      return
    }
    if (isPaid.value && !selectedPaymentMethod.value) {
      ElMessage.warning('Vui lòng chọn phương thức thanh toán!')
      return
    }

    await apiClient.put(`/admin/online-sales/failed-shipping`, null, {
      params: {
        invoiceId,
        statusDetail: 'GIAO_THAT_BAI',
        note: failNote.value,
        paymentMethod: isPaid.value ? selectedPaymentMethod.value : 'UNKNOWN',
        isPaid: invoice.value?.isPaid,
      },
    })

    ElMessage.success('Cập nhật trạng thái giao hàng thất bại thành công!')
    failDialogVisible.value = false
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi cập nhật trạng thái thất bại')
    console.error(err)
  }
}

const formatDate = (dateStr) => (dateStr ? new Date(dateStr).toLocaleString('vi-VN') : '')
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
</style>

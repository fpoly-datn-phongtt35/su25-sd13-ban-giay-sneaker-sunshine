<template>
  <div class="p-4 full-screen">
    <el-button type="primary" icon="el-icon-arrow-left" @click="goBack" class="mb-4">
      Quay lại
    </el-button>

    <el-card>
      <h3>Tiến trình đơn hàng</h3>

      <!-- Hủy -->
      <el-steps
        v-if="['HUY_DON','HUY_GIAO_DICH'].includes(invoice.statusDetail)"
        :active="0"
        align-center
        finish-status="error"
      >
        <el-step :title="'Đã hủy'" status="error" />
      </el-steps>

      <!-- Bình thường -->
      <el-steps
        v-else
        :active="getActiveStep(invoice.statusDetail)"
        finish-status="success"
        align-center
      >
        <el-step v-for="step in mainSteps" :key="step.key" :title="step.label" />
      </el-steps>

      <div class="mt-4 flex flex-wrap gap-2">
        <el-tooltip v-if="!canCancel" content="Đơn đã đến giai đoạn không thể hủy" placement="top">
          <span>
            <el-button type="danger" :disabled="true">Hủy đơn hàng</el-button>
          </span>
        </el-tooltip>
        <el-button v-else type="danger" @click="showCancelDialog">Hủy đơn hàng</el-button>
      </div>

      <el-divider />

      <h2>Thông tin đơn hàng</h2>
      <p><strong>Mã hóa đơn:</strong> {{ invoice.invoiceCode }}</p>
      <p><strong>Khách hàng:</strong> {{ invoice.customerName }}</p>

      <p class="flex items-center gap-2">
        <strong>Số điện thoại:</strong> {{ invoice.phone }}
        <el-button
          v-if="canEditContact"
          type="primary"
          size="small"
          class="ml-2"
          @click="openPhoneDialog"
        >Cập nhật</el-button>
      </p>

      <p><strong>Ngày tạo:</strong> {{ formatDate(invoice.createdDate) }}</p>
      <p><strong>Tổng tiền:</strong> {{ formatCurrency(invoice.totalAmount) }}</p>
      <p><strong>Giảm giá:</strong> {{ formatCurrency(invoice.discountAmount) }}</p>
      <p><strong>Phí vận chuyển:</strong> {{ formatCurrency(invoice.shippingFee) }}</p>
      <p><strong>Thành tiền:</strong> {{ formatCurrency(invoice.finalAmount) }}</p>

      <p class="flex items-center gap-2">
        <strong>Địa chỉ giao hàng:</strong> {{ invoice.deliveryAddress }}
        <el-button
          v-if="canEditContact"
          type="primary"
          size="small"
          class="ml-2"

          @click="openAddressDialog"
        >Cập nhật</el-button>
      </p>

      <el-divider />

      <!-- Dialog cập nhật SỐ ĐIỆN THOẠI -->
      <el-dialog v-model="phoneDialogVisible" title="Cập nhật số điện thoại" width="420px" :close-on-click-modal="false">
        <el-form label-position="top">
          <el-form-item label="Số điện thoại mới">
            <el-input v-model.trim="phoneForm.phone" placeholder="Nhập số điện thoại" maxlength="20" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="phoneDialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="submitPhoneUpdate">Cập nhật</el-button>
        </template>
      </el-dialog>

      <!-- Dialog cập nhật ĐỊA CHỈ -->
      <el-dialog v-model="addressDialogVisible" title="Cập nhật địa chỉ giao hàng" width="600px" :close-on-click-modal="false">
        <div class="flex gap-4 mb-4 flex-wrap">
          <el-select
            v-model="addressForm.provinceCode"
            placeholder="Chọn tỉnh/thành"
            class="flex-1 min-w-[180px]"
            filterable
            @change="handleProvinceChangeForAddress"
          >
            <el-option v-for="p in provinces" :key="p.ProvinceID" :label="p.ProvinceName" :value="p.ProvinceID" />
          </el-select>

          <el-select
            v-model="addressForm.districtCode"
            placeholder="Chọn quận/huyện"
            class="flex-1 min-w-[180px]"
            filterable
            @change="handleDistrictChangeForAddress"
            :disabled="!addressForm.provinceCode"
          >
            <el-option v-for="d in districts" :key="d.DistrictID" :label="d.DistrictName" :value="d.DistrictID" />
          </el-select>

          <el-select
            v-model="addressForm.wardCode"
            placeholder="Chọn phường/xã"
            class="flex-1 min-w-[180px]"
            filterable
            @change="handleWardChangeForAddress"
            :disabled="!addressForm.districtCode"
          >
            <el-option v-for="w in wards" :key="w.WardCode" :label="w.WardName" :value="w.WardCode" />
          </el-select>
        </div>

        <div class="mb-4">
          <el-input v-model="addressForm.houseNumber" placeholder="Số nhà, tên đường" />
        </div>

        <div class="text-right">
          <el-button @click="addressDialogVisible = false">Đóng</el-button>
          <el-button type="primary" @click="submitAddressUpdate">Cập nhật</el-button>
        </div>
      </el-dialog>

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
        <el-table-column label="Giá" min-width="180" align="right">
          <template #default="{ row }">
            <div class="price">
              <span v-if="row.sellPrice !== row.discountedPrice" class="old">
                {{ formatCurrency(row.sellPrice) }}
              </span>
              <span class="new" :class="{ danger: row.discountedPrice < row.sellPrice }">
                {{ formatCurrency(row.discountedPrice) }}
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Hủy đơn -->
    <el-dialog title="Lý do hủy đơn hàng" v-model="cancelDialogVisible" width="400px">
      <el-form label-position="top">
        <el-form-item label="Lý do hủy đơn">
          <el-input type="textarea" v-model="cancelNote" placeholder="Nhập lý do hủy..." rows="3" />
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
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const invoiceId = route.params.id
const invoice = ref({})
const cancelDialogVisible = ref(false)
const cancelNote = ref('')
const selectedPaymentMethod = ref('TIEN_MAT')

/* ====== PHONE dialog ====== */
const phoneDialogVisible = ref(false)
const phoneForm = ref({ phone: '' })

/* ====== ADDRESS dialog ====== */
const addressDialogVisible = ref(false)

const mainSteps = [
  { key: 'CHO_XU_LY', label: 'Chờ xử lý' },
  { key: 'DA_XU_LY', label: 'Đã xử lý' },
  { key: 'CHO_GIAO_HANG', label: 'Chờ giao hàng' },
  { key: 'DANG_GIAO_HANG', label: 'Đang giao hàng' },
  { key: 'GIAO_THANH_CONG', label: 'Giao thành công' },

]
const mainStepKeys = mainSteps.map(s => s.key)

// Những trạng thái KHÓA chỉnh sửa liên hệ (địa chỉ/điện thoại)
const LOCKED_STATUSES = new Set(['DANG_GIAO_HANG', 'GIAO_THANH_CONG', 'HUY_DON', 'HUY_GIAO_DICH'])

const getActiveStep = (statusKey) => {
  const idx = mainStepKeys.indexOf(statusKey || '')
  return idx >= 0 ? idx : 0
}

// CHỈ cho sửa liên hệ khi còn TRƯỚC DANG_GIAO_HANG
const canEditContact = computed(() => {
  const s = invoice.value?.statusDetail
  if (!s || LOCKED_STATUSES.has(s)) return false
  const idx = mainStepKeys.indexOf(s)
  const lockFromIdx = mainStepKeys.indexOf('DANG_GIAO_HANG')
  return idx >= 0 && idx < lockFromIdx
})

// Cho phép hủy: chỉ trước "DANG_GIAO_HANG" và không thuộc locked hủy/giao xong
const canCancel = computed(() => {
  const s = invoice.value?.statusDetail
  if (!s) return false
  if (['GIAO_THANH_CONG', 'HUY_DON', 'HUY_GIAO_DICH'].includes(s)) return false
  const idx = mainStepKeys.indexOf(s)
  const lockFromIdx = mainStepKeys.indexOf('DANG_GIAO_HANG')
  return idx >= 0 && idx < lockFromIdx
})

/* ===== GHN master data ===== */
const provinces = ref([])
const districts = ref([])
const wards = ref([])

const addressForm = ref({
  houseNumber: '',
  provinceCode: null,
  provinceName: '',
  districtCode: null,
  districtName: '',
  wardCode: null,
  wardName: '',
})

const GHN_TOKEN = '847c9bb7-6e42-11ee-a59f-a260851ba65c' // thay bằng token của bạn

const loadProvincesForAddress = async () => {
  try {
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/province',
      {},
      { headers: { Token: GHN_TOKEN } }
    )
    provinces.value = res.data.data
  } catch (err) {
    console.error(err)
    ElMessage.error('Không thể tải danh sách tỉnh/thành phố.')
  }
}

const loadDistrictsForAddress = async () => {
  addressForm.value.districtCode = null
  addressForm.value.districtName = ''
  addressForm.value.wardCode = null
  addressForm.value.wardName = ''
  districts.value = []
  wards.value = []

  if (!addressForm.value.provinceCode) return
  try {
    const res = await axios.get(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/district',
      { headers: { Token: GHN_TOKEN }, params: { province_id: addressForm.value.provinceCode } }
    )
    districts.value = res.data.data
  } catch (err) {
    console.error(err)
    ElMessage.error('Không thể tải danh sách quận/huyện.')
  }
}

const loadWardsForAddress = async () => {
  addressForm.value.wardCode = null
  addressForm.value.wardName = ''
  wards.value = []

  if (!addressForm.value.districtCode) return
  try {
    const res = await axios.get(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/ward',
      { headers: { Token: GHN_TOKEN }, params: { district_id: addressForm.value.districtCode } }
    )
    wards.value = res.data.data
  } catch (err) {
    console.error(err)
    ElMessage.error('Không thể tải danh sách phường/xã.')
  }
}

const handleProvinceChangeForAddress = () => {
  const selected = provinces.value.find(p => p.ProvinceID === addressForm.value.provinceCode)
  addressForm.value.provinceName = selected?.ProvinceName || ''
  loadDistrictsForAddress()
}

const handleDistrictChangeForAddress = () => {
  const selected = districts.value.find(d => d.DistrictID === addressForm.value.districtCode)
  addressForm.value.districtName = selected?.DistrictName || ''
  loadWardsForAddress()
}

const handleWardChangeForAddress = () => {
  const selected = wards.value.find(w => w.WardCode === addressForm.value.wardCode)
  addressForm.value.wardName = selected?.WardName || ''
}

/* ===== Tính phí ship (trả về NUMBER) ===== */
const shippingFee = ref(null)
const calculateShippingFee = async () => {
  if (!addressForm.value.districtCode || !addressForm.value.wardCode) {
    ElMessage.warning('Vui lòng chọn đầy đủ Tỉnh/Thành, Quận/Huyện, Phường/Xã')
    return 0
  }

  const FROM_DISTRICT_ID = 1483
  const FROM_WARD_CODE = '21108'
  const SHOP_ID = 5851480
  const TOKEN_FEE = '741f1c91-4f42-11f0-8cf5-d2552bfd31d8' // token fee

  try {
    const totalWeight = (invoice.value?.invoiceDetailResponses || []).reduce(
      (sum, item) => sum + (item.weight || 200) * item.quantity, 0
    )

    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee',
      {
        from_district_id: FROM_DISTRICT_ID,
        from_ward_code: FROM_WARD_CODE,
        to_district_id: addressForm.value.districtCode,
        to_ward_code: addressForm.value.wardCode,
        weight: Math.max(totalWeight, 100),
        insurance_value: Number(invoice.value?.totalAmount || 0),
        service_type_id: 2,
      },
      {
        headers: {
          'Content-Type': 'application/json',
          Token: TOKEN_FEE,
          ShopId: SHOP_ID,
        },
      }
    )

    const fee = Number(res.data?.data?.total || 0)
    shippingFee.value = fee
    ElMessage.success(`Phí vận chuyển dự kiến: ${formatCurrency(fee)}`)
    return fee
  } catch (err) {
    console.error('❌ Lỗi tính phí ship:', err)
    ElMessage.error('Không thể tính phí vận chuyển. Vui lòng thử lại.')
    return 0
  }
}

/* ====== PHONE: mở & submit ====== */
const openPhoneDialog = () => {
  if (!canEditContact.value) return
  phoneForm.value.phone = String(invoice.value?.phone || '')
  phoneDialogVisible.value = true
}

const VN_PHONE_REGEX = /^(0|\+84)(\d){9,10}$/

const submitPhoneUpdate = async () => {
  if (!canEditContact.value) {
    ElMessage.warning('Đơn đã ở trạng thái không cho phép chỉnh sửa số điện thoại.')
    return
  }
  const phone = (phoneForm.value.phone || '').trim()
  if (!phone) {
    ElMessage.warning('Vui lòng nhập số điện thoại.')
    return
  }
  if (!VN_PHONE_REGEX.test(phone.replace(/\s+/g, ''))) {
    ElMessage.warning('Số điện thoại không hợp lệ.')
    return
  }

  try {
    await apiClient.put(
  '/online-sale/update-phone',
  null,
  { params: { invoiceId, phone } }
)
    ElMessage.success('Cập nhật số điện thoại thành công!')
    phoneDialogVisible.value = false
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi cập nhật số điện thoại!')
    console.error(err)
  }
}

/* ====== ADDRESS: mở & submit ====== */
const openAddressDialog = async () => {
  if (!canEditContact.value) return

  addressDialogVisible.value = true
  await loadProvincesForAddress()

  // Reset form
  addressForm.value = {
    houseNumber: '',
    provinceCode: null,
    provinceName: '',
    districtCode: null,
    districtName: '',
    wardCode: null,
    wardName: '',
  }
  districts.value = []
  wards.value = []

  // Parse địa chỉ hiện tại
  const currentAddress = invoice.value?.deliveryAddress || ''
  const parts = currentAddress.split(' - ').map(s => s.trim())
  if (parts.length >= 4) {
    addressForm.value.houseNumber = parts[0]
    const [wardName, districtName, provinceName] = [parts[1], parts[2], parts[3]]

    const foundProvince = provinces.value.find(p => p.ProvinceName === provinceName)
    if (foundProvince) {
      addressForm.value.provinceCode = foundProvince.ProvinceID
      addressForm.value.provinceName = foundProvince.ProvinceName
      await loadDistrictsForAddress()

      const foundDistrict = districts.value.find(d => d.DistrictName === districtName)
      if (foundDistrict) {
        addressForm.value.districtCode = foundDistrict.DistrictID
        addressForm.value.districtName = foundDistrict.DistrictName
        await loadWardsForAddress()

        const foundWard = wards.value.find(w => w.WardName === wardName)
        if (foundWard) {
          addressForm.value.wardCode = foundWard.WardCode
          addressForm.value.wardName = foundWard.WardName
        }
      }
    }
  }
}

const submitAddressUpdate = async () => {
  if (!canEditContact.value) {
    ElMessage.warning('Đơn đã ở trạng thái không cho phép chỉnh sửa địa chỉ.')
    return
  }

  const { houseNumber, provinceName, districtName, wardName } = addressForm.value
  if (!houseNumber || !provinceName || !districtName || !wardName) {
    ElMessage.warning('Vui lòng nhập đầy đủ địa chỉ.')
    return
  }

  const fullAddress = `${houseNumber} - ${wardName} - ${districtName} - ${provinceName}`
  const newShippingFee = await calculateShippingFee()

  const totalAmount = Number(invoice.value.totalAmount) || 0
  const discountAmount = Number(invoice.value.discountAmount) || 0
  const finalAmount = totalAmount + Number(newShippingFee) - discountAmount

  try {
    await apiClient.put(`/admin/online-sales/update-address`, {
      invoiceId,
      newAddress: fullAddress,
      shippingFee: Number(newShippingFee),
      finalAmount: finalAmount
    })

    ElMessage.success('Cập nhật địa chỉ thành công!')
    addressDialogVisible.value = false
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi cập nhật địa chỉ!')
    console.error(err)
  }
}

/* ===== Hủy đơn ===== */
const showCancelDialog = () => {
  if (!canCancel.value) {
    ElMessage.warning('Đơn đã ở trạng thái không thể hủy.')
    return
  }
  cancelNote.value = ''
  selectedPaymentMethod.value = 'TIEN_MAT'
  cancelDialogVisible.value = true
}

const cancelOrder = async () => {
  try {
    if (!canCancel.value) {
      ElMessage.warning('Đơn đã ở trạng thái không thể hủy.')
      return
    }
    if (!cancelNote.value.trim()) {
      ElMessage.warning('Vui lòng nhập lý do hủy đơn!')
      return
    }

    await apiClient.put(`/online-sale/huy-don-va-hoan-tien`, null, {
      params: {
        invoiceId,
        statusDetail: 'HUY_DON',
        note: cancelNote.value,
        paymentMethod: selectedPaymentMethod.value,
        request: invoice.value.isPaid ? 1 : null,
        isPaid: invoice.value.isPaid
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

/* ===== Fetch invoice ===== */
const fetchInvoice = async () => {
  try {
    const res = await apiClient.get(`/online-sale/get-order-customer-detail`, {
      params: { invoiceId }
    })
    invoice.value = res.data
    console.log('data: ', invoice.value)
  } catch (err) {
    ElMessage.error('Lỗi khi tải thông tin đơn hàng.')
    console.error(err)
  }
}

const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('vi-VN') : ''
const formatCurrency = (val) => `${Number(val || 0).toLocaleString('vi-VN')} ₫`
const goBack = () => router.back()

onMounted(() => {
  fetchInvoice()
})
</script>

<style scoped>
.full-screen { max-width: 100%; }
.mt-4 { margin-top: 20px; }
.el-card { max-width: 100%; margin: auto; }
.el-dialog {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.el-dialog__header { padding-bottom: 10px; border-bottom: 1px solid #eee; }
.el-dialog__title { font-size: 1.25rem; font-weight: 600; color: #333; }
.el-form-item__label { font-size: 0.95rem; font-weight: 500; color: #555; }
.price { display: flex; gap: 8px; justify-content: flex-end; align-items: baseline; }
.price .old { text-decoration: line-through; color: var(--el-text-color-placeholder); }
.price .new.danger { color: var(--el-color-danger); font-weight: 700; }
</style>

<template>
  <div class="checkout-page">
    <el-row :gutter="30" class="checkout-content">
      <!-- LEFT: Shipping + Payment -->
      <el-col :span="14" :xs="24" :sm="24" :md="14">
        <div class="shipping-info-section">
          <div class="section-header">THÔNG TIN GIAO HÀNG</div>

          <el-form
            :model="form"
            :rules="rules"
            ref="formRef"
            label-position="top"
            class="delivery-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="HỌ VÀ TÊN" prop="customerName">
                  <el-input v-model="form.customerName" placeholder="Nhập họ tên" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="SỐ ĐIỆN THOẠI" prop="phone">
                  <el-input v-model="form.phone" placeholder="Số điện thoại" />
                  <div class="phone-hint"></div>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="ĐỊA CHỈ" prop="address.houseName">
                  <el-input v-model="form.address.houseName" placeholder="Nhập địa chỉ cụ thể" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="Email" prop="email">
                  <el-input v-model="form.email" placeholder="Email" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="TỈNH / THÀNH" prop="address.provinceCode">
                  <el-select
                    v-model.number="form.address.provinceCode"
                    placeholder="Chọn tỉnh / thành"
                    @change="onProvinceChange"
                    filterable
                  >
                    <el-option
                      v-for="item in provinces"
                      :key="item.ProvinceID"
                      :label="item.ProvinceName"
                      :value="item.ProvinceID"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="QUẬN / HUYỆN" prop="address.districtCode">
                  <el-select
                    v-model.number="form.address.districtCode"
                    placeholder="Chọn quận / huyện"
                    @change="onDistrictChange"
                    :disabled="!form.address.provinceCode"
                    filterable
                  >
                    <el-option
                      v-for="item in districts"
                      :key="item.DistrictID"
                      :label="item.DistrictName"
                      :value="item.DistrictID"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="PHƯỜNG / XÃ" prop="address.wardCode">
                  <el-select
                    v-model="form.address.wardCode"
                    placeholder="Chọn phường / xã"
                    @change="onWardChange"
                    :disabled="!form.address.districtCode"
                    filterable
                  >
                    <el-option
                      v-for="item in wards"
                      :key="item.WardCode"
                      :label="item.WardName"
                      :value="item.WardCode"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="GHI CHÚ" prop="description">
                  <el-input
                    v-model="form.description"
                    type="textarea"
                    :rows="3"
                    placeholder="Ghi chú cho đơn hàng (không bắt buộc)..."
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <div class="shipping-method-section">
            <div class="section-header">PHƯƠNG THỨC THANH TOÁN</div>
            <el-radio-group v-model="paymentMethod" class="payment-method-radio">
              <el-radio :label="1" border class="payment-option">
                <div class="payment-content">
                  <img
                    src="https://file.hstatic.net/1000284478/file/zalo_pay-39_a15d1d155b814bf38a06e52e1fff5cad.svg"
                    alt="ZaloPay"
                    class="payment-icon"
                  />
                  <span>Thanh toán qua ZaloPay</span>
                </div>
              </el-radio>
              <el-radio :label="0" border class="payment-option">
                <div class="payment-content">
                  <img
                    src="https://file.hstatic.net/1000284478/file/cod_icon-47_a8768752c1a445da90d600ca0a94675c.svg"
                    alt="COD"
                    class="payment-icon"
                  />
                  <span>Thanh toán khi nhận hàng (COD)</span>
                </div>
              </el-radio>
            </el-radio-group>

            <!-- native-type="button" để không submit form HTML / tránh reload -->
            <el-button
              native-type="button"
              type="primary"
              size="large"
              :loading="isSubmitting"
              :disabled="isSubmitting"
              @click="handleSubmit"
              class="submit-button"
            >
              {{ isSubmitting ? 'Đang xử lý...' : 'ĐẶT HÀNG' }}
            </el-button>
          </div>
        </div>
      </el-col>

      <!-- RIGHT: Order Summary -->
      <el-col :span="10" :xs="24" :sm="24" :md="10">
        <div class="order-summary-section">
          <div class="summary-header">
            <span class="summary-title">TÓM TẮT ĐƠN HÀNG</span>
            <span class="total-price-header">{{ formatPrice(finalTotal) }}</span>
          </div>

          <div class="order-items-list">
            <div v-for="item in cartItems" :key="item.productDetailId" class="order-item">
              <div class="item-details">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-variant">
                  <span v-if="item.color">{{ item.color }}</span>
                  <span v-if="item.color && item.size"> / </span>
                  <span v-if="item.size">{{ item.size }}</span>
                </div>
              </div>
              <div class="item-quantity">SL: {{ item.quantity }}</div>
              <div class="item-price">{{ formatPrice(unitPriceOf(item)) }}</div>
            </div>
          </div>

          <!-- Discount area -->
          <div class="discount-code-section">
            <el-input
              v-model="discountCode"
              placeholder="Nhập Mã Giảm Giá"
              class="discount-input"
            />
            <el-button type="primary" class="apply-discount-button" @click="applyDiscountCode"
              >SỬ DỤNG</el-button
            >
            <el-button class="view-voucher-button" @click="openVoucherDialog"
              >XEM VOUCHER</el-button
            >
            <el-button
              v-if="appliedVoucher"
              type="danger"
              class="cancel-discount-button"
              @click="cancelVoucher"
              >HỦY BỎ</el-button
            >
          </div>

          <div v-if="appliedVoucher" class="applied-voucher-note">
            <el-tag type="success"
              >Đang áp dụng: {{ appliedVoucher.voucherName }} ({{
                appliedVoucher.voucherCode
              }})</el-tag
            >
          </div>

          <div class="loyal-customer-text">Khách hàng thân thiết</div>

          <div class="summary-totals">
            <div class="total-row">
              <span class="label">Tạm tính</span>
              <span class="value">{{ formatPrice(orderTotal) }}</span>
            </div>
            <div class="total-row">
              <span class="label">Giảm giá</span>
              <span class="value discount-value">-{{ formatPrice(discountAmount) }}</span>
            </div>
            <div class="total-row">
              <span class="label">Phí vận chuyển</span>
              <span class="value shipping-value">{{
                shippingFee > 0 ? formatPrice(shippingFee) : 'Miễn phí'
              }}</span>
            </div>
            <div class="total-row final-total-row">
              <span class="label">THÀNH TIỀN</span>
              <span class="value final-value">{{ formatPrice(finalTotal) }}</span>
            </div>
          </div>

          <div class="delivery-time-warning">
            Do lượng đơn hàng bên vận chuyển đang cao, thời gian giao hàng dự kiến sẽ tăng thêm 2-3
            ngày.
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- VOUCHER DIALOG -->
    <el-dialog v-model="voucherDialog.visible" width="900px" :close-on-click-modal="false">
      <div v-if="voucherDialog.loading" class="text-center py-6 text-gray-500">
        Đang tải voucher...
      </div>

      <div v-else>
        <el-empty v-if="!voucherDialog.filtered.length" description="Không có voucher phù hợp" />
        <el-table v-else :data="voucherDialog.filtered" border style="width: 100%">
          <el-table-column prop="voucherCode" label="Mã" min-width="130" />
          <el-table-column
            prop="voucherName"
            label="Tên voucher"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column label="Ưu đãi" min-width="160">
            <template #default="{ row }">
              <span v-if="row.discountPercentage"
                >-{{ row.discountPercentage }}% (tối đa
                {{ formatPrice(row.maxDiscountValue || 0) }})</span
              >
              <span v-else>{{ formatPrice(row.discountAmount || 0) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="ĐK tối thiểu" min-width="140">
            <template #default="{ row }">{{ formatPrice(row.minOrderValue || 0) }}</template>
          </el-table-column>
          <el-table-column prop="endDate" label="Hết hạn" min-width="130" />
          <el-table-column label="Trạng thái" min-width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{
                row.status === 1 ? 'Hoạt động' : 'Hết hạn'
              }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Thao tác" fixed="right" width="200">
            <template #default="{ row }">
              <el-space v-if="isCurrentApplied(row)">
                <el-button size="small" type="danger" @click="cancelVoucherFromDialog"
                  >Hủy bỏ</el-button
                >
                <el-tag size="small" type="success">Đang áp dụng</el-tag>
              </el-space>
              <template v-else>
                <el-tooltip
                  v-if="!isVoucherUsable(row)"
                  placement="top"
                  :content="voucherDisableReason(row)"
                >
                  <span
                    ><el-button size="small" type="primary" plain disabled>Sử dụng</el-button></span
                  >
                </el-tooltip>
                <el-button v-else size="small" type="primary" @click="applyVoucherFromDialog(row)"
                  >Sử dụng</el-button
                >
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="voucherDialog.visible = false">Đóng</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCart, clearCart } from '@/utils/cart'

// ===== GHN constants (đổi qua .env khi production)
const GHN_TOKEN = '741f1c91-4f42-11f0-8cf5-d2552bfd31d8'
const SHOP_ID = 5851480
const FROM_DISTRICT_ID = 1483
const FROM_WARD_CODE = '21108'

const router = useRouter()

// ===== state
const formRef = ref(null)
const cartItems = ref([])
const provinces = ref([])
const districts = ref([])
const wards = ref([])

const shippingFee = ref(0)
const discountAmount = ref(0)
const finalTotal = ref(0)
const paymentAmount = ref(0)
const isSubmitting = ref(false)
const paymentMethod = ref(0) // 0 COD, 1 ZaloPay
const discountCode = ref('')
const appliedVoucher = ref(null)

const vouchers = ref([])
const voucherDialog = ref({ visible: false, loading: false, search: '', filtered: [] })

// ===== form
const form = ref({
  customerId: null,
  customerName: '',
  phone: '',
  email: '',
  description: '',
  address: {
    country: 'Việt Nam',
    provinceCode: null,
    provinceName: '',
    districtCode: null,
    districtName: '',
    wardCode: '',
    wardName: '',
    houseName: '',
    fullAddress: '',
  },
})

// ===== rules
const rules = {
  email: [{ required: true, message: 'Vui lòng nhập email', trigger: 'blur' }],
  customerName: [{ required: true, message: 'Vui lòng nhập họ và tên', trigger: 'blur' }],
  phone: [
    { required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' },
    {
      pattern: /^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$/,
      message: 'Số điện thoại không hợp lệ',
      trigger: 'blur',
    },
  ],
  'address.houseName': [{ required: true, message: 'Vui lòng nhập địa chỉ', trigger: 'blur' }],
  'address.provinceCode': [
    { required: true, message: 'Vui lòng chọn tỉnh/thành phố', trigger: 'change' },
  ],
  'address.districtCode': [
    { required: true, message: 'Vui lòng chọn quận/huyện', trigger: 'change' },
  ],
  'address.wardCode': [{ required: true, message: 'Vui lòng chọn phường/xã', trigger: 'change' }],
}

// ===== helpers
const toInt = (n) => Math.round(Number(n || 0))

const unitPriceOf = (it) => toInt(it.discountedPrice ?? it.sellPrice ?? it.price ?? 0)

const orderTotal = computed(() =>
  cartItems.value.reduce((s, it) => {
    const price = unitPriceOf(it)
    const qty = toInt(it.quantity)
    return s + (Number.isFinite(price) && Number.isFinite(qty) ? price * qty : 0)
  }, 0),
)

const formatPrice = (val) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(val || 0))

// voucher helpers
const isVoucherUsable = (voucher) => {
  if (!voucher || voucher.status !== 1) return false
  if ((voucher.minOrderValue || 0) > orderTotal.value) return false
  return true
}
const voucherDisableReason = (voucher) => {
  if (!voucher) return ''
  if (voucher.status !== 1) return 'Voucher đã hết hạn hoặc không hoạt động'
  if ((voucher.minOrderValue || 0) > orderTotal.value) {
    const delta = (voucher.minOrderValue || 0) - orderTotal.value
    return `Cần mua thêm ${formatPrice(delta)} để dùng voucher này`
  }
  return 'Không đủ điều kiện'
}
const computeDiscount = (ot, voucher) => {
  if (!voucher) return 0
  const order = toInt(ot)
  let discount = 0
  if (voucher.discountPercentage) {
    discount = Math.floor((order * Number(voucher.discountPercentage || 0)) / 100)
    if (voucher.maxDiscountValue) discount = Math.min(discount, toInt(voucher.maxDiscountValue))
  } else if (voucher.discountAmount) {
    discount = toInt(voucher.discountAmount)
  }
  discount = Math.max(0, Math.min(discount, order))
  return discount
}

// recalc final totals
const recalcFinal = () => {
  const ot = toInt(orderTotal.value)
  const ship = toInt(shippingFee.value)
  const disc = appliedVoucher.value ? computeDiscount(ot, appliedVoucher.value) : 0
  discountAmount.value = disc
  const total = Math.max(0, ot - disc + ship)
  finalTotal.value = toInt(total)
  paymentAmount.value = toInt(total)
}

// voucher actions
const cancelVoucher = () => {
  appliedVoucher.value = null
  discountCode.value = ''
  recalcFinal()
  ElMessage.info('Đã hủy mã giảm giá.')
}
const cancelVoucherFromDialog = () => cancelVoucher()
const applyVoucherFromDialog = (voucher) => {
  if (!isVoucherUsable(voucher)) {
    ElMessage.warning('Voucher không hợp lệ')
    return
  }
  appliedVoucher.value = voucher
  discountCode.value = voucher.voucherCode || ''
  recalcFinal()
  ElMessage.success(`Áp dụng voucher: ${voucher.voucherName}`)
}
const applyDiscountCode = async () => {
  const code = (discountCode.value || '').trim()
  if (!code) {
    ElMessage.warning('Vui lòng nhập mã giảm giá!')
    return
  }
  try {
    const res = await axios.get('http://localhost:8080/api/online-sale/vouchers/apply', {
      params: {
        customerId: form.value.customerId || 0,
        voucherCode: code,
        orderTotal: orderTotal.value,
      },
    })
    appliedVoucher.value = res.data || null
    discountCode.value = appliedVoucher.value?.voucherCode || code
    recalcFinal()
    ElMessage.success('Áp dụng voucher thành công')
  } catch (err) {
    console.error('Lỗi áp voucher:', err)
    ElMessage.error(err?.response?.data?.message || 'Không áp dụng được voucher.')
  }
}

// voucher dialog
const openVoucherDialog = async () => {
  voucherDialog.value.visible = true
  voucherDialog.value.loading = true
  voucherDialog.value.search = ''
  try {
    const customerId = form.value.customerId || localStorage.getItem('userId')
    if (!customerId) {
      ElMessage.warning('Không tìm thấy thông tin người dùng.')
      voucherDialog.value.loading = false
      return
    }
    const res = await axios.get(`http://localhost:8080/api/online-sale/by-customer/${customerId}`)
    vouchers.value = Array.isArray(res.data) ? res.data : res.data?.data || []
    voucherDialog.value.filtered = [...vouchers.value]
  } catch (err) {
    console.error('Lỗi load voucher:', err)
    vouchers.value = []
    voucherDialog.value.filtered = []
    ElMessage.error('Không thể tải voucher.')
  } finally {
    voucherDialog.value.loading = false
  }
}
const filterVouchers = () => {
  const q = (voucherDialog.value.search || '').trim().toLowerCase()
  if (!q) {
    voucherDialog.value.filtered = [...vouchers.value]
    return
  }
  voucherDialog.value.filtered = vouchers.value.filter(
    (v) =>
      (v.voucherCode || '').toLowerCase().includes(q) ||
      (v.voucherName || '').toLowerCase().includes(q),
  )
}
const isCurrentApplied = (row) =>
  !!(appliedVoucher.value && (row?.voucherCode || '') === (appliedVoucher.value?.voucherCode || ''))

// auto apply best

// ===== GHN helpers
const loadProvinces = async () => {
  try {
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/province',
      {},
      { headers: { Token: GHN_TOKEN } },
    )
    provinces.value = res.data?.data || []
  } catch (err) {
    console.error('Lỗi load provinces', err)
  }
}
const loadDistricts = async () => {
  if (!form.value.address.provinceCode) return
  try {
    const res = await axios.get(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/district',
      {
        headers: { Token: GHN_TOKEN },
        params: { province_id: form.value.address.provinceCode },
      },
    )
    districts.value = res.data?.data || []
  } catch (err) {
    console.error('Lỗi load districts', err)
  }
}
const loadWards = async () => {
  if (!form.value.address.districtCode) return
  try {
    const res = await axios.get('https://online-gateway.ghn.vn/shiip/public-api/master-data/ward', {
      headers: { Token: GHN_TOKEN },
      params: { district_id: form.value.address.districtCode },
    })
    wards.value = res.data?.data || []
  } catch (err) {
    console.error('Lỗi load wards', err)
  }
}

// calculate shipping
const calculateShippingFee = async () => {
  if (!form.value.address.districtCode || !form.value.address.wardCode) {
    shippingFee.value = 0
    recalcFinal()
    return
  }
  try {
    const totalWeight = cartItems.value.reduce((s, it) => {
      const w = toInt(it.weight || 200)
      const q = toInt(it.quantity || 0)
      return s + (Number.isFinite(w) && Number.isFinite(q) ? w * q : 0)
    }, 0)
    const weightToSend = Math.max(totalWeight, 100)
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee',
      {
        from_district_id: FROM_DISTRICT_ID,
        from_ward_code: FROM_WARD_CODE,
        to_district_id: Number(form.value.address.districtCode),
        to_ward_code: form.value.address.wardCode,
        weight: weightToSend,
        service_type_id: 2,
      },
      { headers: { 'Content-Type': 'application/json', Token: GHN_TOKEN, ShopId: SHOP_ID } },
    )
    shippingFee.value = Math.max(0, toInt(res.data?.data?.total || 0))
    ElMessage.info(`Phí vận chuyển: ${formatPrice(shippingFee.value)}`)
    recalcFinal()
  } catch (err) {
    console.error('Lỗi tính phí ship', err)
    shippingFee.value = 0
    recalcFinal()
    ElMessage.error('Không thể tính phí vận chuyển.')
  }
}

// address handlers
const onProvinceChange = async () => {
  const sel = provinces.value.find((p) => p.ProvinceID === form.value.address.provinceCode)
  form.value.address.provinceName = sel?.ProvinceName || ''
  form.value.address.districtCode = null
  form.value.address.districtName = ''
  form.value.address.wardCode = null
  form.value.address.wardName = ''
  districts.value = []
  wards.value = []
  shippingFee.value = 0
  await loadDistricts()
}
const onDistrictChange = async () => {
  const sel = districts.value.find((d) => d.DistrictID === form.value.address.districtCode)
  form.value.address.districtName = sel?.DistrictName || ''
  form.value.address.wardCode = null
  form.value.address.wardName = ''
  wards.value = []
  shippingFee.value = 0
  await loadWards()
}
const onWardChange = async () => {
  const sel = wards.value.find((w) => w.WardCode === form.value.address.wardCode)
  form.value.address.wardName = sel?.WardName || ''
  await calculateShippingFee()
}

const productDetailIds = ref(null)
const handleSubmit = async () => {
  // ===== Helpers =====
  const failEarly = (msg, type = 'error') => {
    if (type === 'warning') ElMessage.warning(msg)
    else if (type === 'success') ElMessage.success(msg)
    else ElMessage.error(msg)
    throw { __isAbort: true }
  }
  const vndInt = (n) => toInt(n ?? 0) // chuẩn hóa VND

  // ===== Validate =====
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('Vui lòng điền đầy đủ thông tin giao hàng.')
    return
  }
  if (isSubmitting.value) {
    ElMessage.warning('Không thể thanh toán')
    return
  }
  if (!cartItems.value.length) {
    ElMessage.warning('Giỏ hàng trống.')
    router.push('/')
    return
  }

  isSubmitting.value = true
  let loadingInstance = null

  try {
    loadingInstance = ElLoading.service({ fullscreen: true, text: 'Đang đặt hàng...' })

    // ===== 1) Chuẩn hóa items (snapshot giá FE) =====
    const itemsPayload = cartItems.value.map((it) => {
      const unitPrice = vndInt(unitPriceOf(it)) // giá gốc hiển thị (tham khảo)
      const quantity  = Math.max(1, vndInt(it.quantity || 0))

      // Bắt buộc gửi sellPrice & discountedPrice
      const sellPrice = vndInt(it.sellPrice ?? it.price ?? unitPrice)
      const discountedPrice =
        it.discountedPrice != null ? vndInt(it.discountedPrice) : sellPrice // nếu không giảm = sell

      // Tự tính lại % để nhất quán
      const discountPercentage =
        sellPrice > 0 ? Math.round(((sellPrice - discountedPrice) * 100) / sellPrice) : 0

      // Chỉ giữ campaignId khi user thực sự chọn 1 campaign (ví dụ bạn có biến userPickedCampaignId)
      const discountCampaignId = it.userPickedCampaignId ?? null

      return {
        productDetailId: it.productDetailId,
        productName: it.productName || it.name || null,
        quantity,

        // snapshot giá FE để verify-prices
        sellPrice,
        discountedPrice,
        discountPercentage,
        discountCampaignId,

        // phụ trợ
        unitPrice,
        lineTotal: unitPrice * quantity,
      }
    })

    // ===== 2) VERIFY tồn kho / status nhanh =====
    const idsParam = itemsPayload.map((x) => x.productDetailId).join(',')
    try {
      const res = await axios.get(`http://localhost:8080/api/online-sale/verify-list-pdDetail/${idsParam}`)
      let payload = res.data
      if (typeof payload === 'string') {
        try { payload = JSON.parse(payload) } catch (_) {}
      }
      if (!Array.isArray(payload) || payload.length === 0) {
        failEarly('Không có dữ liệu xác thực sản phẩm từ server.')
      }

      const getStatus = (p) => p?.status ?? p?.active ?? p?.isAvailable ?? p?.enabled
      const getAvailable = (p) => p?.quantity ?? null

      const byId = new Map()
      payload.forEach((p) => {
        const id = p?.id ?? p?.productDetailId ?? p?.product_detail_id ?? null
        if (id != null) byId.set(String(id), p)
      })

      const invalid = []
      for (const item of itemsPayload) {
        const idStr = String(item.productDetailId)
        const serverItem = byId.get(idStr)
        const name = item.productName || serverItem?.productName || serverItem?.name || idStr
        if (!serverItem) { invalid.push({ id: idStr, name, reason: 'Không tìm thấy trên server' }); continue }

        if (!Number.isFinite(item.quantity) || item.quantity <= 0) {
          invalid.push({ id: idStr, name, reason: 'Số lượng phải > 0' }); continue
        }

        // status
        const s = getStatus(serverItem)
        const okStatus = (typeof s === 'boolean') ? s === true
          : (Number.isFinite(Number(s)) ? Number(s) === 1
          : (typeof s === 'string' && ['active','enabled','available','true','1'].includes(s.trim().toLowerCase())))
        if (!okStatus) { invalid.push({ id: idStr, name, reason: 'Sản phẩm không hợp lệ (status)' }); continue }

        // qty
        const availRaw = getAvailable(serverItem)
        if (availRaw == null) { invalid.push({ id: idStr, name, reason: 'Thiếu tồn kho (quantity)' }); continue }
        const availNum = Number(availRaw)
        if (!Number.isFinite(availNum)) { invalid.push({ id: idStr, name, reason: `Tồn kho không hợp lệ: ${availRaw}` }); continue }
        if (availNum <= 0) { invalid.push({ id: idStr, name, reason: `Đã hết hàng (tồn = ${availNum})` }); continue }
        if (item.quantity > availNum) { invalid.push({ id: idStr, name, reason: `Yêu cầu ${item.quantity} > tồn ${availNum}` }); continue }
      }

      if (invalid.length > 0) {
        const details = invalid.map(i => `${i.name || i.id}: ${i.reason}`).join('; ')
        failEarly(`Có sản phẩm không hợp lệ: ${details}`)
      }
    } catch (e) {
      console.error('verify-list-pdDetail error:', e)
      failEarly('Không thể kiểm tra trạng thái sản phẩm. Vui lòng thử lại sau.')
    }

    // ===== 3) Tính tổng & build payload =====
    const itemsSubtotal = itemsPayload.reduce((s, x) => s + (x.lineTotal ?? x.unitPrice * x.quantity), 0)
    const expectedTotal = Math.max(0, vndInt(itemsSubtotal) - vndInt(discountAmount.value) + vndInt(shippingFee.value))
    finalTotal.value = expectedTotal
    paymentAmount.value = expectedTotal

    const payload = {
      customerInfo: {
        ...form.value,
        address: {
          ...form.value.address,
          provinceName: (provinces.value.find(p => p.ProvinceID === form.value.address.provinceCode)?.ProvinceName) || '',
          districtName: (districts.value.find(d => d.DistrictID === form.value.address.districtCode)?.DistrictName) || '',
          wardName:     (wards.value.find(w => w.WardCode   === form.value.address.wardCode)?.WardName) || '',
        },
      },
      items: itemsPayload, // chứa snapshot giá FE
      orderTotal: vndInt(orderTotal.value),
      discountAmount: vndInt(discountAmount.value),
      shippingFee: vndInt(shippingFee.value),
      amount: vndInt(paymentAmount.value),
      amountBreakdown: {
        itemsSubtotal: vndInt(itemsSubtotal),
        orderTotal: vndInt(orderTotal.value),
        discountAmount: vndInt(discountAmount.value),
        shippingFee: vndInt(shippingFee.value),
      },
      voucherCode: appliedVoucher.value?.voucherCode || null,
      // CHÚ Ý: Đừng ép campaignId toàn giỏ ở đây, để null nếu không có lựa chọn cố định
      discountCampaignId: null,
      description: form.value.description || '',
      orderType: 1,
      status: 1,
      employeeId: null,
    }

    // ===== 4) (Optional) verify voucher =====
    if (payload.voucherCode) {
      try {
        const res = await axios.get('http://localhost:8080/api/online-sale/get-code-voucher', {
          params: { code: payload.voucherCode },
        })
        if (res.data !== 1) failEarly('Voucher không tồn tại hoặc đã bị xóa')
      } catch (err) {
        console.error('verify-voucher error:', err)
        failEarly('Lỗi khi kiểm tra voucher. Vui lòng thử lại.')
      }
    }

    // ===== 5) VERIFY-PRICES =====
    try {
      await axios.post('http://localhost:8080/api/online-sale/verify-prices', payload)
      // 200 OK -> khớp giá, tiếp tục
    } catch (err) {
      const data = err?.response?.data
      if (err?.response?.status === 409) {
        // In rõ cặp FE vs SV để biết lệch
        if (Array.isArray(data?.diffs) && data.diffs.length) {
          console.table(data.diffs[0]) // {feSellPrice, svSellPrice, ...}
        }
        failEarly(data?.message || 'Giá đã được cập nhật, vui lòng làm mới giỏ hàng.', 'warning')
      } else {
        console.error('verify-prices error:', err)
        failEarly('Không thể xác thực giá. Vui lòng thử lại.')
      }
    }

    // ===== 6) PAYMENT FLOW =====
    if (paymentMethod.value === 1) {
      // ---- ZaloPay ----
      try {
        const res = await axios.post('http://localhost:8080/api/payment/zalo/create', payload)
        const zaloPay = res.data?.zaloPay
        const invoice = res.data?.invoiceData?.invoice || null
        const code = invoice?.invoiceCode

        if (code) {
          try {
            const res2 = await axios.get('http://localhost:8080/api/online-sale/verify-invoice-status', { params: { code } })
            const data = res2?.data
            const nums = Array.isArray(data)
              ? data.map(item => {
                  if (typeof item === 'number') return item
                  if (typeof item === 'string' && item.trim() !== '') return Number(item)
                  if (item && typeof item === 'object') return Number(item.status ?? item.code ?? item.value ?? item)
                  return NaN
                }).filter(Number.isFinite)
              : [Number(data)]
            if (nums.includes(2)) failEarly('Đợt giảm giá không tồn tại hoặc đã xóa')
          } catch (err) {
            console.error('verify-invoice error:', err?.response?.data || err)
            failEarly('Không thể kiểm tra trạng thái hoá đơn. Vui lòng thử lại.')
          }
        }

        const customerId = invoice?.customerId
        if (customerId) localStorage.setItem('userId', String(customerId))

        if (zaloPay?.orderUrl && zaloPay?.appTransId) {
          const pending = {
            invoiceId: invoice?.id || null,
            appTransId: zaloPay.appTransId,
            amount: payload.amount,
          }
          localStorage.setItem('appTransId', zaloPay.appTransId)
          localStorage.setItem('pendingOrder', JSON.stringify(pending))
          ElMessage.success('Đang mở ZaloPay ở tab mới. Sau khi thanh toán xong, tab này sẽ tự động đóng.')

          let newTab = null
          try { newTab = window.open(zaloPay.orderUrl, '_blank') } catch { newTab = null }
          if (!newTab) {
            console.warn('Popup bị chặn, chuyển cùng tab')
            window.location.replace(zaloPay.orderUrl)
            return
          }

          const messageHandler = (e) => {
            const data = e.data || {}
            if (data && (data.status === 'paid' || data.status === 'failed' || data.status === 'cancel')) {
              try { if (newTab && !newTab.closed) newTab.close() } catch {}
              window.removeEventListener('message', messageHandler)
              if (router?.replace) router.replace('/payment-result')
              else window.location.replace('/payment-result')
            }
          }
          window.addEventListener('message', messageHandler)

          const watcher = setInterval(() => {
            try {
              if (!newTab || newTab.closed) {
                clearInterval(watcher)
                window.removeEventListener('message', messageHandler)
                if (router?.replace) router.replace('/payment-result')
                else window.location.replace('/payment-result')
              }
            } catch {}
          }, 1000)

          clearCart()
          return
        } else {
          failEarly('Không nhận được URL thanh toán từ ZaloPay. Vui lòng thử lại.')
        }
      } catch (err) {
        console.error('Khởi tạo ZaloPay lỗi:', err)
        failEarly(err?.response?.data?.message || 'Lỗi khi khởi tạo thanh toán ZaloPay. Vui lòng thử lại.')
      }
    } else {
      // ---- COD ----
      try {
        const res = await axios.post('http://localhost:8080/api/online-sale/checkout', payload)

        const customerId = res.data?.invoice?.customerId
        if (customerId) localStorage.setItem('userId', String(customerId))

        const invoiceCode =
          res.data?.invoice?.invoiceCode ??
          res.data?.invoiceCode ??
          res.data?.data?.invoiceCode ??
          null

        if (invoiceCode) {
          try {
            const res2 = await axios.get('http://localhost:8080/api/online-sale/verify-invoice-status', { params: { code: invoiceCode } })
            if (Number(res2.data) === 2) failEarly('Đợt giảm giá không tồn tại hoặc đã xóa')
          } catch (err) {
            console.error('verify-invoice error (COD):', err?.response?.data || err)
            failEarly('Không thể kiểm tra trạng thái hoá đơn. Vui lòng thử lại.')
          }
        }

        clearCart()
        cartItems.value = []
        ElMessage.success('Đặt hàng thành công! Đơn hàng sẽ sớm được giao.')
        if (invoiceCode) router.push({ path: '/payment-result', query: { invoiceCode } })
        else router.push('/payment-result')
        return
      } catch (codErr) {
        console.error('Checkout COD lỗi:', codErr)
        failEarly(codErr?.response?.data?.message || 'Lỗi khi đặt hàng COD. Vui lòng thử lại.')
      }
    }
  } catch (err) {
    if (!err?.__isAbort) {
      console.error('Lỗi đặt hàng:', err)
      ElMessage.error(err?.response?.data?.message || 'Đặt hàng thất bại, vui lòng thử lại.')
    }
  } finally {
    isSubmitting.value = false
    try { if (loadingInstance) loadingInstance.close() } catch {}
  }
}


// ===== watchers
watch(
  [orderTotal, shippingFee, appliedVoucher],
  () => {
    recalcFinal()
  },
  { immediate: true },
)

// ===== onMounted
onMounted(async () => {
  // load cart
  try {
    const res = await getCart()
    if (Array.isArray(res)) cartItems.value = res
    else if (res && Array.isArray(res.cart)) {
      cartItems.value = res.cart
      if (res.removed && res.removed.length) ElMessage.warning('Một số sản phẩm đã bị gỡ khỏi giỏ.')
    } else cartItems.value = []
  } catch (err) {
    console.error('Không load được giỏ hàng:', err)
    cartItems.value = []
  }

  if (!cartItems.value.length) {
    ElMessage.warning('Giỏ hàng trống! Bạn sẽ được chuyển hướng về trang chủ.')
    router.push('/')
    return
  }

  await loadProvinces()

  // load user info nếu đã có
  const userId = localStorage.getItem('userId')
  if (userId) {
    const loadingInstance = ElLoading.service({ fullscreen: true, text: 'Đang tải thông tin...' })
    try {
      const res = await axios.get(`http://localhost:8080/api/online-sale/customers/${userId}`)
      const customer = res.data
      form.value = {
        ...form.value,
        customerId: customer.id,
        customerName: customer.customerName,
        phone: customer.phone,
        email: customer.email,
        address: {
          country: 'Việt Nam',
          provinceCode: Number(customer.provinceCode) || null,
          provinceName: customer.provinceName || '',
          districtCode: Number(customer.districtCode) || null,
          districtName: customer.districtName || '',
          wardCode: customer.wardCode || '',
          wardName: customer.wardName || '',
          houseName: customer.houseName || '',
          fullAddress:
            customer.houseName &&
            customer.wardName &&
            customer.districtName &&
            customer.provinceName
              ? `${customer.houseName}, ${customer.wardName}, ${customer.districtName}, ${customer.provinceName}`
              : '',
        },
      }
      if (form.value.address.provinceCode) await loadDistricts()
      if (form.value.address.districtCode) await loadWards()
      if (form.value.address.districtCode && form.value.address.wardCode)
        await calculateShippingFee()
    } catch (err) {
      console.error('Không lấy được thông tin khách hàng:', err)
    } finally {
      try {
        loadingInstance.close()
      } catch {}
    }
  }

  recalcFinal()
})
</script>

<style scoped>
.checkout-page {
  padding: 30px;
  max-width: 1400px;
  margin: 20px auto;
  font-family: Arial, sans-serif;
}
.shipping-info-section {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.section-header {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.delivery-form .el-form-item {
  margin-bottom: 15px;
}
.phone-hint {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 5px;
}
.shipping-method-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #eee;
}
.payment-option {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.payment-content {
  display: flex;
  align-items: center;
  gap: 10px;
}
.payment-icon {
  height: 24px;
  width: auto;
  object-fit: contain;
}
.submit-button {
  width: 100%;
  margin-top: 30px;
  padding: 14px 0;
  font-size: 18px;
  font-weight: 700;
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
}
.order-summary-section {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.summary-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}
.total-price-header {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}
.order-items-list {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 15px;
}
.order-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  font-size: 14px;
}
.item-details {
  flex-grow: 1;
}
.item-name {
  font-weight: 500;
  color: #333;
  line-height: 1.4;
}
.item-variant {
  color: #999;
  font-size: 12px;
}
.item-quantity {
  width: 60px;
  text-align: center;
  color: #666;
  flex-shrink: 0;
}
.item-price {
  font-weight: 500;
  color: #333;
  width: 100px;
  text-align: right;
  flex-shrink: 0;
}
.discount-code-section {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  align-items: center;
}
.discount-input {
  flex: 1 1 220px;
  min-width: 220px;
}
.apply-discount-button {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
  font-weight: 700;
}
.applied-voucher-note {
  margin-bottom: 10px;
}
.loyal-customer-text {
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
  text-align: right;
}
.summary-totals {
  padding-top: 15px;
  border-top: 1px dashed #e0e0e0;
  margin-bottom: 20px;
}
.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 15px;
  color: #555;
}
.total-row .value {
  font-weight: 700;
  color: #333;
}
.discount-value,
.shipping-value {
  font-weight: normal;
}
.total-row.final-total-row {
  margin-top: 20px;
  font-size: 20px;
}
.final-total-row .label {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}
.final-total-row .value.final-value {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}
.delivery-time-warning {
  font-size: 12px;
  color: #f56c6c;
  text-align: center;
  margin-top: 20px;
}
@media (max-width: 991px) {
  .checkout-page {
    padding: 15px;
  }
  .order-summary-section {
    margin-top: 30px;
  }
}
</style>

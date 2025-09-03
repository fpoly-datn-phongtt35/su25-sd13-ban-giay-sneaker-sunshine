<template>
  <div class="checkout-page">
    <el-row :gutter="30" class="checkout-content">
      <!-- LEFT: Shipping + Payment -->
      <el-col :span="14" :xs="24" :sm="24" :md="14">
        <div class="shipping-info-section">
          <div class="section-header">THÔNG TIN GIAO HÀNG</div>

          <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="delivery-form">
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
                  <el-select v-model.number="form.address.provinceCode" placeholder="Chọn tỉnh / thành" @change="onProvinceChange" filterable>
                    <el-option v-for="item in provinces" :key="item.ProvinceID" :label="item.ProvinceName" :value="item.ProvinceID" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="QUẬN / HUYỆN" prop="address.districtCode">
                  <el-select v-model.number="form.address.districtCode" placeholder="Chọn quận / huyện" @change="onDistrictChange" :disabled="!form.address.provinceCode" filterable>
                    <el-option v-for="item in districts" :key="item.DistrictID" :label="item.DistrictName" :value="item.DistrictID" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="PHƯỜNG / XÃ" prop="address.wardCode">
                  <el-select v-model="form.address.wardCode" placeholder="Chọn phường / xã" @change="onWardChange" :disabled="!form.address.districtCode" filterable>
                    <el-option v-for="item in wards" :key="item.WardCode" :label="item.WardName" :value="item.WardCode" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <div class="shipping-method-section">
            <div class="section-header">PHƯƠNG THỨC THANH TOÁN</div>
            <el-radio-group v-model="paymentMethod" class="payment-method-radio">
              <el-radio :label="1" border class="payment-option">
                <div class="payment-content">
                  <img src="https://file.hstatic.net/1000284478/file/zalo_pay-39_a15d1d155b814bf38a06e52e1fff5cad.svg" alt="ZaloPay" class="payment-icon" />
                  <span>Thanh toán qua ZaloPay</span>
                </div>
              </el-radio>
              <el-radio :label="0" border class="payment-option">
                <div class="payment-content">
                  <img src="https://file.hstatic.net/1000284478/file/cod_icon-47_a8768752c1a445da90d600ca0a94675c.svg" alt="COD" class="payment-icon" />
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
            <el-input v-model="discountCode" placeholder="Nhập Mã Giảm Giá" class="discount-input" />
            <el-button type="primary" class="apply-discount-button" @click="applyDiscountCode">SỬ DỤNG</el-button>
            <el-button class="view-voucher-button" @click="openVoucherDialog">XEM VOUCHER</el-button>
            <el-button v-if="appliedVoucher" type="danger" class="cancel-discount-button" @click="cancelVoucher">HỦY BỎ</el-button>
          </div>

          <div v-if="appliedVoucher" class="applied-voucher-note">
            <el-tag type="success">Đang áp dụng: {{ appliedVoucher.voucherName }} ({{ appliedVoucher.voucherCode }})</el-tag>
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
              <span class="value shipping-value">{{ shippingFee > 0 ? formatPrice(shippingFee) : 'Miễn phí' }}</span>
            </div>
            <div class="total-row final-total-row">
              <span class="label">THÀNH TIỀN</span>
              <span class="value final-value">{{ formatPrice(finalTotal) }}</span>
            </div>
          </div>

          <div class="delivery-time-warning">Do lượng đơn hàng bên vận chuyển đang cao, thời gian giao hàng dự kiến sẽ tăng thêm 2-3 ngày.</div>
        </div>
      </el-col>
    </el-row>

    <!-- VOUCHER DIALOG -->
    <el-dialog v-model="voucherDialog.visible" width="900px" :close-on-click-modal="false">
      <template #header>
        <div class="flex items-center justify-between w-full">
          <span class="font-bold text-base">Voucher của bạn</span>
          <el-input v-model="voucherDialog.search" placeholder="Tìm theo mã hoặc tên voucher" clearable style="width:260px" @input="filterVouchers" />
        </div>
      </template>

      <div v-if="voucherDialog.loading" class="text-center py-6 text-gray-500">Đang tải voucher...</div>

      <div v-else>
        <el-empty v-if="!voucherDialog.filtered.length" description="Không có voucher phù hợp" />
        <el-table v-else :data="voucherDialog.filtered" border style="width:100%">
          <el-table-column prop="voucherCode" label="Mã" min-width="130" />
          <el-table-column prop="voucherName" label="Tên voucher" min-width="220" show-overflow-tooltip />
          <el-table-column label="Ưu đãi" min-width="160">
            <template #default="{ row }">
              <span v-if="row.discountPercentage">-{{ row.discountPercentage }}% (tối đa {{ formatPrice(row.maxDiscountValue || 0) }})</span>
              <span v-else>{{ formatPrice(row.discountAmount || 0) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="ĐK tối thiểu" min-width="140">
            <template #default="{ row }">{{ formatPrice(row.minOrderValue || 0) }}</template>
          </el-table-column>
          <el-table-column prop="endDate" label="Hết hạn" min-width="130" />
          <el-table-column label="Trạng thái" min-width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? 'Hoạt động' : 'Hết hạn' }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Thao tác" fixed="right" width="200">
            <template #default="{ row }">
              <el-space v-if="isCurrentApplied(row)">
                <el-button size="small" type="danger" @click="cancelVoucherFromDialog">Hủy bỏ</el-button>
                <el-tag size="small" type="success">Đang áp dụng</el-tag>
              </el-space>
              <template v-else>
                <el-tooltip v-if="!isVoucherUsable(row)" placement="top" :content="voucherDisableReason(row)">
                  <span><el-button size="small" type="primary" plain disabled>Sử dụng</el-button></span>
                </el-tooltip>
                <el-button v-else size="small" type="primary" @click="applyVoucherFromDialog(row)">Sử dụng</el-button>
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
    { pattern: /^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$/, message: 'Số điện thoại không hợp lệ', trigger: 'blur' },
  ],
  'address.houseName': [{ required: true, message: 'Vui lòng nhập địa chỉ', trigger: 'blur' }],
  'address.provinceCode': [{ required: true, message: 'Vui lòng chọn tỉnh/thành phố', trigger: 'change' }],
  'address.districtCode': [{ required: true, message: 'Vui lòng chọn quận/huyện', trigger: 'change' }],
  'address.wardCode': [{ required: true, message: 'Vui lòng chọn phường/xã', trigger: 'change' }],
}

// ===== helpers
const toInt = (n) => Math.round(Number(n || 0))

// Lấy đơn giá thanh toán nhất quán
const unitPriceOf = (it) => toInt(it.discountedPrice ?? it.sellPrice ?? it.price ?? 0)

// orderTotal luôn dựa trên unitPrice
const orderTotal = computed(() =>
  cartItems.value.reduce((s, it) => {
    const price = unitPriceOf(it)
    const qty = toInt(it.quantity)
    return s + (Number.isFinite(price) && Number.isFinite(qty) ? price * qty : 0)
  }, 0)
)

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(val || 0))

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
  if (!isVoucherUsable(voucher)) { ElMessage.warning('Voucher không hợp lệ'); return }
  appliedVoucher.value = voucher
  discountCode.value = voucher.voucherCode || ''
  recalcFinal()
  ElMessage.success(`Áp dụng voucher: ${voucher.voucherName}`)
}
const applyDiscountCode = async () => {
  const code = (discountCode.value || '').trim()
  if (!code) { ElMessage.warning('Vui lòng nhập mã giảm giá!'); return }
  try {
    const res = await axios.get('http://localhost:8080/api/online-sale/vouchers/apply', {
      params: { customerId: form.value.customerId || 0, voucherCode: code, orderTotal: orderTotal.value }
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
    const res = await axios.get(`http://localhost:8080/api/admin/vouchers/by-customer/${customerId}`)
    vouchers.value = Array.isArray(res.data) ? res.data : (res.data?.data || [])
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
  if (!q) { voucherDialog.value.filtered = [...vouchers.value]; return }
  voucherDialog.value.filtered = vouchers.value.filter(v =>
    (v.voucherCode || '').toLowerCase().includes(q) || (v.voucherName || '').toLowerCase().includes(q)
  )
}
const isCurrentApplied = (row) => !!(appliedVoucher.value && (row?.voucherCode || '') === (appliedVoucher.value?.voucherCode || ''))

// auto apply best


// ===== GHN helpers
const loadProvinces = async () => {
  try {
    const res = await axios.post('https://online-gateway.ghn.vn/shiip/public-api/master-data/province', {}, { headers: { Token: GHN_TOKEN }})
    provinces.value = res.data?.data || []
  } catch (err) { console.error('Lỗi load provinces', err) }
}
const loadDistricts = async () => {
  if (!form.value.address.provinceCode) return
  try {
    const res = await axios.get('https://online-gateway.ghn.vn/shiip/public-api/master-data/district', {
      headers: { Token: GHN_TOKEN }, params: { province_id: form.value.address.provinceCode }
    })
    districts.value = res.data?.data || []
  } catch (err) { console.error('Lỗi load districts', err) }
}
const loadWards = async () => {
  if (!form.value.address.districtCode) return
  try {
    const res = await axios.get('https://online-gateway.ghn.vn/shiip/public-api/master-data/ward', {
      headers: { Token: GHN_TOKEN }, params: { district_id: form.value.address.districtCode }
    })
    wards.value = res.data?.data || []
  } catch (err) { console.error('Lỗi load wards', err) }
}

// calculate shipping
const calculateShippingFee = async () => {
  if (!form.value.address.districtCode || !form.value.address.wardCode) { shippingFee.value = 0; recalcFinal(); return }
  try {
    const totalWeight = cartItems.value.reduce((s, it) => {
      const w = toInt(it.weight || 200)
      const q = toInt(it.quantity || 0)
      return s + (Number.isFinite(w) && Number.isFinite(q) ? w * q : 0)
    }, 0)
    const weightToSend = Math.max(totalWeight, 100)
    const res = await axios.post('https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee', {
      from_district_id: FROM_DISTRICT_ID,
      from_ward_code: FROM_WARD_CODE,
      to_district_id: Number(form.value.address.districtCode),
      to_ward_code: form.value.address.wardCode,
      weight: weightToSend,
      service_type_id: 2,
    }, { headers: { 'Content-Type': 'application/json', Token: GHN_TOKEN, ShopId: SHOP_ID }})
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
  const sel = provinces.value.find(p => p.ProvinceID === form.value.address.provinceCode)
  form.value.address.provinceName = sel?.ProvinceName || ''
  form.value.address.districtCode = null; form.value.address.districtName = ''
  form.value.address.wardCode = null; form.value.address.wardName = ''
  districts.value = []; wards.value = []
  shippingFee.value = 0
  await loadDistricts()
}
const onDistrictChange = async () => {
  const sel = districts.value.find(d => d.DistrictID === form.value.address.districtCode)
  form.value.address.districtName = sel?.DistrictName || ''
  form.value.address.wardCode = null; form.value.address.wardName = ''
  wards.value = []; shippingFee.value = 0
  await loadWards()
}
const onWardChange = async () => {
  const sel = wards.value.find(w => w.WardCode === form.value.address.wardCode)
  form.value.address.wardName = sel?.WardName || ''
  await calculateShippingFee()
}

const productDetailIds = ref(null);

const handleSubmit = async () => {
  try {
    // Validate form
    await formRef.value.validate();
  } catch (err) {
    ElMessage.warning('Vui lòng điền đầy đủ thông tin giao hàng.');
    return;
  }

  if (!cartItems.value.length) {
    ElMessage.warning('Giỏ hàng trống.');
    router.push('/');
    return;
  }

  isSubmitting.value = true;
  const loadingInstance = ElLoading.service({ fullscreen: true, text: 'Đang đặt hàng...' });

  try {
    // ------------- Chuẩn hóa items -------------
    const itemsPayload = cartItems.value.map(it => {
      const unitPrice = unitPriceOf(it);
      const quantity = toInt(it.quantity || 0);
      const sellPrice = toInt(it.sellPrice ?? it.price ?? unitPrice);
      const discounted = (it.discountedPrice != null) ? toInt(it.discountedPrice) : null;
      const lineTotal = unitPrice * quantity;
      return {
        productDetailId: it.productDetailId,
        productName: it.productName || it.name || null,
        quantity,
        sellPrice,
        discountedPrice: discounted,
        unitPrice,
        discountPercentage: Number(it.discountPercentage || 0),
        discountCampaignId: it.discountCampaignId || null,
        lineTotal,
      };
    });

    // IDs để gọi verify
    productDetailIds.value = itemsPayload.map(x => x.productDetailId);

    // ------------- VERIFY: status + tồn kho (field `quantity` ở backend) -------------
    try {
      const idsParam = Array.isArray(productDetailIds.value) ? productDetailIds.value.join(',') : productDetailIds.value;
      const res = await axios.get(`http://localhost:8080/api/online-sale/verify-list-pdDetail/${idsParam}`);
      let payload = res.data;
      if (typeof payload === 'string') {
        try { payload = JSON.parse(payload); } catch (e) { /* ignore parse error */ }
      }

      if (!Array.isArray(payload) || payload.length === 0) {
        ElMessage.error('Không có dữ liệu xác thực sản phẩm từ server.');
        return;
      }

      const getStatus = (p) => p?.status ?? p?.active ?? p?.isAvailable ?? p?.enabled;
      // CHỈ SỬ DỤNG 'quantity' LÀ TỒN KHO (theo yêu cầu)
      const getAvailable = (p) => p?.quantity ?? null;

      // map by id
      const byId = new Map();
      payload.forEach(p => {
        const id = p?.id ?? p?.productDetailId ?? p?.product_detail_id ?? null;
        if (id != null) byId.set(String(id), p);
      });

      const invalidItems = [];

      for (const item of itemsPayload) {
        const idStr = String(item.productDetailId);
        const serverItem = byId.get(idStr);
        const displayName = item.productName || (serverItem && (serverItem.productName || serverItem.name)) || idStr;

        if (!serverItem) {
          invalidItems.push({ id: idStr, name: displayName, reason: 'Không tìm thấy sản phẩm trên server' });
          continue;
        }

        // kiểm tra quantity request > 0
        if (!Number.isFinite(item.quantity) || item.quantity <= 0) {
          invalidItems.push({ id: idStr, name: displayName, reason: 'Số lượng phải > 0' });
          continue;
        }

        // kiểm tra status (chấp nhận numeric=1 / boolean true / string active)
        const s = getStatus(serverItem);
        let okStatus = false;
        if (typeof s === 'boolean') okStatus = s === true;
        else {
          const n = Number(s);
          if (Number.isFinite(n)) okStatus = n === 1;
          else if (typeof s === 'string') {
            const t = s.trim().toLowerCase();
            okStatus = ['active', 'enabled', 'available', 'true', '1'].includes(t);
          }
        }
        if (!okStatus) {
          invalidItems.push({ id: idStr, name: displayName, reason: 'Sản phẩm không hợp lệ (status)' });
          continue;
        }

        // CHECK tồn kho dùng field `quantity`
        const availRaw = getAvailable(serverItem);
        if (availRaw == null) {
          invalidItems.push({ id: idStr, name: displayName, reason: 'Backend không trả thông tin tồn kho (quantity)' });
          continue;
        }

        const availNum = Number(availRaw);
        if (!Number.isFinite(availNum)) {
          invalidItems.push({ id: idStr, name: displayName, reason: `Tồn kho trả về không hợp lệ: ${availRaw}` });
          continue;
        }

        if (availNum <= 0) {
          invalidItems.push({ id: idStr, name: displayName, reason: `Sản phẩm đã hết hàng (tồn kho = ${availNum})` });
          continue;
        }

        if (item.quantity > availNum) {
          invalidItems.push({ id: idStr, name: displayName, reason: `Số lượng yêu cầu (${item.quantity}) vượt quá tồn kho (${availNum})` });
          continue;
        }
      } // end for

      if (invalidItems.length > 0) {
        const details = invalidItems.map(i => `${i.name || i.id}: ${i.reason}`).join('; ');
        ElMessage.error(`Có sản phẩm không hợp lệ: ${details}`);
        return;
      }
    } catch (verifyErr) {
      console.error('Lỗi khi kiểm tra trạng thái productDetail:', verifyErr);
      ElMessage.error('Không thể kiểm tra trạng thái sản phẩm. Vui lòng thử lại sau.');
      return;
    }

    // ------------- Tính tiền và build payload -------------
    const itemsSubtotal = itemsPayload.reduce((s, x) => s + (x.lineTotal ?? (x.unitPrice * x.quantity)), 0);
    const expectedTotal = Math.max(0, toInt(itemsSubtotal) - toInt(discountAmount.value) + toInt(shippingFee.value));
    finalTotal.value = expectedTotal;
    paymentAmount.value = expectedTotal;

    const payload = {
      customerInfo: {
        ...form.value,
        address: {
          ...form.value.address,
          provinceName: provinces.value.find(p => p.ProvinceID === form.value.address.provinceCode)?.ProvinceName || '',
          districtName: districts.value.find(d => d.DistrictID === form.value.address.districtCode)?.DistrictName || '',
          wardName: wards.value.find(w => w.WardCode === form.value.address.wardCode)?.WardName || '',
        }
      },
      items: itemsPayload,
      orderTotal: toInt(orderTotal.value),
      discountAmount: toInt(discountAmount.value),
      shippingFee: toInt(shippingFee.value),
      amount: toInt(paymentAmount.value), // BE dùng trường này gửi ZaloPay
      amountBreakdown: {
        itemsSubtotal: toInt(itemsSubtotal),
        orderTotal: toInt(orderTotal.value),
        discountAmount: toInt(discountAmount.value),
        shippingFee: toInt(shippingFee.value),
      },
      voucherCode: appliedVoucher.value?.voucherCode || null,
      discountCampaignId: cartItems.value[0]?.discountCampaignId || null,
      description: form.value.description || '',
      orderType: 1,
      status: 1,
      employeeId: null,
    };

    // ------------- Payment flow -------------
    if (paymentMethod.value === 1) {
      // ZaloPay
      // bên trong branch nếu (paymentMethod.value === 1) { ... }
      try {
        const res = await axios.post('http://localhost:8080/api/payment/zalo/create', payload);
        console.log('data invoice v2: ',res)
        const zaloPay = res.data?.zaloPay;
        const invoice = res.data?.invoiceData?.invoice || null;
        console.log('data invoice: ',invoice?.invoiceCode)
        const code = invoice?.invoiceCode;
        if (!code) {
          console.warn('Không có invoiceCode để verify');
        } else {
          try {
            const res2 = await axios.get('http://localhost:8080/api/online-sale/verify-invoice', {
              params: { code }
            });
            console.log('data invoice v4: ', res2.data);
          } catch (error) {
            console.error('verify-invoice error:', error?.response?.data || error);
          }
        }
        const customerId = invoice?.customerId;
        if (customerId) localStorage.setItem('userId', String(customerId));

        if (zaloPay?.orderUrl && zaloPay?.appTransId) {
          const pending = {
            invoiceId: invoice?.id || null,
            appTransId: zaloPay.appTransId,
            amount: payload.amount
          };

          localStorage.setItem('appTransId', zaloPay.appTransId);
          localStorage.setItem('pendingOrder', JSON.stringify(pending));

          ElMessage.success('Đang mở ZaloPay ở tab mới. Sau khi thanh toán xong, tab này sẽ tự động đóng.');

          // Thử mở popup
          let newTab = null;
          try { newTab = window.open(zaloPay.orderUrl, '_blank'); } catch (e) { newTab = null; }

          // Nếu popup bị chặn -> fallback: mở cùng tab
          if (!newTab) {
            console.warn('Popup bị chặn, chuyển cùng tab');
            window.location.replace(zaloPay.orderUrl);
            return;
          }

          // Lắng nghe message từ popup (payment-result sẽ gửi message khi hoàn tất)
          const messageHandler = (e) => {
            // Bảo mật: kiểm tra origin (nếu bạn muốn chặt chẽ: e.origin === window.location.origin)
            const data = e.data || {};
            if (data && (data.status === 'paid' || data.status === 'failed' || data.status === 'cancel')) {
              // đóng popup nếu còn mở
              try { if (newTab && !newTab.closed) newTab.close(); } catch (err) { /* ignore */ }

              window.removeEventListener('message', messageHandler);
              // chuyển trang chính sang trang kết quả (không lưu lịch sử)
              if (typeof router !== 'undefined' && router && typeof router.replace === 'function') {
                router.replace('/payment-result');
              } else {
                window.location.replace('/payment-result');
              }
            }
          };
          window.addEventListener('message', messageHandler);

          // watcher: nếu user đóng popup thủ công -> redirect về payment-result
          const watcher = setInterval(() => {
            try {
              if (!newTab || newTab.closed) {
                clearInterval(watcher);
                window.removeEventListener('message', messageHandler);
                if (typeof router !== 'undefined' && router && typeof router.replace === 'function') {
                  router.replace('/payment-result');
                } else {
                  window.location.replace('/payment-result');
                }
              }
            } catch (err) { /* ignore cross-origin check errors */ }
          }, 1000);

          // optional: sau khi mở popup, vẫn để tab gốc ở trạng thái hiện tại (hoặc show loading)
          return;
        } else {
          ElMessage.error('Không nhận được URL thanh toán từ ZaloPay. Vui lòng thử lại.');
        }
      } catch (err) {
        console.error('Lỗi khi khởi tạo thanh toán ZaloPay:', err);
        ElMessage.error(err?.response?.data?.message || 'Lỗi khi khởi tạo thanh toán ZaloPay. Vui lòng thử lại.');
      }
    } else {
      // COD flow (CHỈ THÊM bắn status theo invoiceCode khi chuyển trang)
      try {
        const res = await axios.post('http://localhost:8080/api/online-sale/checkout', payload);

        const customerId = res.data?.invoice?.customerId;
        if (customerId) localStorage.setItem('userId', String(customerId));

        // Lấy invoiceCode để trang payment-result tự check trạng thái ShipCode
        const invoiceCode =
          res.data?.invoice?.invoiceCode ??
          res.data?.invoiceCode ??
          res.data?.data?.invoiceCode ??
          null;

        clearCart();
        cartItems.value = [];
        ElMessage.success('Đặt hàng thành công! Đơn hàng sẽ sớm được giao.');

        // 👉 Bắn status qua query nếu có invoiceCode
        if (invoiceCode) {
          router.push({ path: '/payment-result', query: { invoiceCode } });
        } else {
          router.push('/payment-result');
        }
      } catch (codErr) {
        console.error('Lỗi khi checkout COD:', codErr);
        ElMessage.error(codErr?.response?.data?.message || 'Lỗi khi đặt hàng COD. Vui lòng thử lại.');
      }
    }

  } catch (err) {
    console.error('Lỗi đặt hàng:', err);
    ElMessage.error(err?.response?.data?.message || 'Đặt hàng thất bại, vui lòng thử lại.');
  } finally {
    isSubmitting.value = false;
    try { loadingInstance.close(); } catch (e) { /* ignore */ }
  }
};

// ===== watchers
watch([orderTotal, shippingFee, appliedVoucher], () => { recalcFinal() }, { immediate: true })

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
          fullAddress: customer.houseName && customer.wardName && customer.districtName && customer.provinceName
            ? `${customer.houseName}, ${customer.wardName}, ${customer.districtName}, ${customer.provinceName}` : '',
        }
      }
      if (form.value.address.provinceCode) await loadDistricts()
      if (form.value.address.districtCode) await loadWards()
      if (form.value.address.districtCode && form.value.address.wardCode) await calculateShippingFee()
    } catch (err) {
      console.error('Không lấy được thông tin khách hàng:', err)
    } finally {
      try { loadingInstance.close() } catch {}
    }
  }

  recalcFinal()
})
</script>

<style scoped>
.checkout-page { padding: 30px; max-width: 1400px; margin: 20px auto; font-family: Arial, sans-serif; }
.shipping-info-section { padding:20px; border:1px solid #e0e0e0; border-radius:8px; background:#fff; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.section-header { font-size:16px; font-weight:700; color:#333; margin-bottom:20px; padding-bottom:10px; border-bottom:1px solid #eee; }
.delivery-form .el-form-item { margin-bottom: 15px; }
.phone-hint { font-size:12px; color:#f56c6c; margin-top:5px; }
.shipping-method-section { margin-top:30px; padding-top:20px; border-top:1px dashed #eee; }
.payment-option { border:1px solid #dcdfe6; border-radius:4px; padding:10px 15px; display:flex; align-items:center; gap:10px; width:100%; }
.payment-content { display:flex; align-items:center; gap:10px; }
.payment-icon { height:24px; width:auto; object-fit:contain; }
.submit-button { width:100%; margin-top:30px; padding:14px 0; font-size:18px; font-weight:700; background-color:#f56c6c; border-color:#f56c6c; color:#fff; }
.order-summary-section { padding:20px; border:1px solid #e0e0e0; border-radius:8px; background:#f9f9f9; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.summary-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; padding-bottom:10px; border-bottom:1px solid #eee; }
.summary-title { font-size:16px; font-weight:700; color:#333; }
.total-price-header { font-size:20px; font-weight:700; color:#f56c6c; }
.order-items-list { border-bottom:1px solid #eee; padding-bottom:15px; margin-bottom:15px; }
.order-item { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:10px; font-size:14px; }
.item-details { flex-grow:1; }
.item-name { font-weight:500; color:#333; line-height:1.4; }
.item-variant { color:#999; font-size:12px; }
.item-quantity { width:60px; text-align:center; color:#666; flex-shrink:0; }
.item-price { font-weight:500; color:#333; width:100px; text-align:right; flex-shrink:0; }
.discount-code-section { display:flex; gap:10px; margin-bottom:12px; flex-wrap:wrap; align-items:center; }
.discount-input { flex:1 1 220px; min-width:220px; }
.apply-discount-button { background-color:#409EFF; border-color:#409EFF; color:#fff; font-weight:700; }
.applied-voucher-note { margin-bottom:10px; }
.loyal-customer-text { font-size:13px; color:#999; margin-bottom:20px; text-align:right; }
.summary-totals { padding-top:15px; border-top:1px dashed #e0e0e0; margin-bottom:20px; }
.total-row { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; font-size:15px; color:#555; }
.total-row .value { font-weight:700; color:#333; }
.discount-value, .shipping-value { font-weight:normal; }
.total-row.final-total-row { margin-top:20px; font-size:20px; }
.final-total-row .label { font-size:18px; font-weight:700; color:#333; }
.final-total-row .value.final-value { font-size:24px; font-weight:700; color:#f56c6c; }
.delivery-time-warning { font-size:12px; color:#f56c6c; text-align:center; margin-top:20px; }
@media (max-width:991px) { .checkout-page { padding:15px } .order-summary-section { margin-top:30px } }
</style>

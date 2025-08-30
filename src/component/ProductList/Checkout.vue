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
                <el-form-item label="Gmail" prop="email">
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
            <el-button type="primary" size="large" :loading="isSubmitting" @click="handleSubmit" class="submit-button">
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
              <div class="item-price">{{ formatPrice(item.price) }}</div>
            </div>
          </div>

          <!-- Discount area (luôn có: Input + SỬ DỤNG + XEM VOUCHER; nếu đã áp dụng thì thêm HỦY BỎ) -->
          <div class="discount-code-section">
            <el-input
              v-model="discountCode"
              placeholder="Nhập Mã Giảm Giá"
              class="discount-input"
            />
            <el-button
              type="primary"
              class="apply-discount-button"
              @click="applyDiscountCode"
            >
              SỬ DỤNG
            </el-button>

            <el-button
              class="view-voucher-button"
              @click="openVoucherDialog"
            >
              XEM VOUCHER
            </el-button>

            <el-button
              v-if="appliedVoucher"
              type="danger"
              class="cancel-discount-button"
              @click="cancelVoucher"
            >
              HỦY BỎ
            </el-button>
          </div>

          <!-- Tag hiển thị voucher đang áp dụng -->
          <div v-if="appliedVoucher" class="applied-voucher-note">
            <el-tag type="success">
              Đang áp dụng: {{ appliedVoucher.voucherName }} ({{ appliedVoucher.voucherCode }})
            </el-tag>
          </div>

          <div class="loyal-customer-text">Khách hàng thân thiết</div>

          <div class="summary-totals">
            <div class="total-row">
              <span class="label">Tạm tính</span>
              <span class="value">{{ formatPrice(totalPrice) }}</span>
            </div>
            <div class="total-row">
              <span class="label">Giảm giá</span>
              <span class="value discount-value">-{{ formatPrice(discountAmount) }}</span>
            </div>
            <div class="total-row">
              <span class="label">Phí vận chuyển</span>
              <span class="value shipping-value">{{ shippingFee > 0 ? formatPrice(shippingFee) : '+' }}</span>
            </div>
            <div class="total-row final-total-row">
              <span class="label">THÀNH TIỀN</span>
              <span class="value final-value">{{ formatPrice(finalTotal) }}</span>
            </div>
          </div>

          <div class="delivery-time-warning">
            Do lượng đơn hàng bên vận chuyển đang cao, thời gian giao hàng dự kiến sẽ tăng thêm 2-3 ngày. Rất mong Quý Khách thông cảm.
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- =========================
         VOUCHER DIALOG
    ========================== -->
    <el-dialog v-model="voucherDialog.visible" width="900px" :close-on-click-modal="false">
      <template #header>
        <div class="flex items-center justify-between w-full">
          <span class="font-bold text-base">Voucher của bạn</span>
          <el-input
            v-model="voucherDialog.search"
            placeholder="Tìm theo mã hoặc tên voucher"
            clearable
            style="width: 260px"
            @input="filterVouchers"
          />
        </div>
      </template>

      <div v-if="voucherDialog.loading" class="text-center py-6 text-gray-500">
        Đang tải voucher...
      </div>

      <div v-else>
        <el-empty v-if="!voucherDialog.filtered.length" description="Không có voucher phù hợp" />
        <el-table v-else :data="voucherDialog.filtered" border style="width: 100%">
          <el-table-column prop="voucherCode" label="Mã" min-width="130" />
          <el-table-column prop="voucherName" label="Tên voucher" min-width="220" show-overflow-tooltip />
          <el-table-column label="Ưu đãi" min-width="140">
            <template #default="{ row }">
              <span v-if="row.discountPercentage">
                -{{ row.discountPercentage }}% (tối đa {{ formatPrice(row.maxDiscountValue || 0) }})
              </span>
              <span v-else>{{ formatCurrency(row.discountAmount || 0) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="ĐK tối thiểu" min-width="140">
            <template #default="{ row }">
              {{ formatCurrency(row.minOrderValue || 0) }}
            </template>
          </el-table-column>
          <el-table-column prop="endDate" label="Hết hạn" min-width="130" />
          <el-table-column label="Trạng thái" min-width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? 'Hoạt động' : 'Hết hạn' }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- Hành động -->
          <el-table-column label="Thao tác" fixed="right" width="180">
            <template #default="{ row }">
              <!-- Nếu đang áp dụng chính voucher này -->
              <el-space v-if="isCurrentApplied(row)">
                <el-button size="small" type="danger" @click="cancelVoucherFromDialog">
                  Hủy bỏ
                </el-button>
                <el-tag size="small" type="success">Đang áp dụng</el-tag>
              </el-space>

              <!-- Nếu chưa áp dụng -->
              <template v-else>
                <el-tooltip
                  v-if="!isVoucherUsable(row)"
                  placement="top"
                  :content="voucherDisableReason(row)"
                >
                  <span>
                    <el-button size="small" type="primary" plain disabled> Sử dụng </el-button>
                  </span>
                </el-tooltip>
                <el-button
                  v-else
                  size="small"
                  type="primary"
                  @click="applyVoucherFromDialog(row)"
                >
                  Sử dụng
                </el-button>
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
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCart, clearCart } from '@/utils/cart'

/** ====== Constants (GHN) ====== */
const GHN_TOKEN = '741f1c91-4f42-11f0-8cf5-d2552bfd31d8'
const SHOP_ID = 5851480
const FROM_DISTRICT_ID = 1483
const FROM_WARD_CODE = '21108'

const router = useRouter()

/** ====== Refs/State ====== */
const formRef = ref(null)
const cartItems = ref([])
const provinces = ref([])
const districts = ref([])
const wards = ref([])

const shippingFee = ref(0)
const finalTotal = ref(0)
const isSubmitting = ref(false)
const paymentMethod = ref(0) // 0 COD, 1 ZaloPay
const discountCode = ref('')
const appliedVoucher = ref(null)
const discountAmount = ref(0)

<<<<<<< HEAD
const cancelVoucher = () => {
  appliedVoucher.value = null
  discountCode.value = ''           // Xóa code trong input
  discountAmount.value = 0
  // Cập nhật lại finalTotal
  finalTotal.value = totalPrice.value + shippingFee.value
  ElMessage.info('Đã hủy bỏ mã giảm giá.')
}

const applyDiscountCode = async () => {
  if (!discountCode.value) {
    ElMessage.warning('Vui lòng nhập mã giảm giá!')
    return
  }
  try {
    const res = await axios.get('http://localhost:8080/api/online-sale/vouchers/apply', {
      params: {
        customerId: form.value.customerId || 0,
        voucherCode: discountCode.value,
        orderTotal: totalPrice.value
      }
    })
    appliedVoucher.value = res.data
    ElMessage.success(`Áp dụng voucher thành công: ${appliedVoucher.value.voucherName}`)

    // 👉 Tính tiền giảm giá
    let discount = 0
    if (appliedVoucher.value.discountPercentage) {
      discount = Math.min(
        (totalPrice.value * appliedVoucher.value.discountPercentage) / 100,
        appliedVoucher.value.maxDiscountValue || Infinity
      )
    } else if (appliedVoucher.value.discountAmount) {
      discount = appliedVoucher.value.discountAmount
    }

    discountAmount.value = discount
    finalTotal.value = totalPrice.value - discountAmount.value + shippingFee.value

  } catch (err) {
    console.error('❌ Lỗi áp dụng voucher:', err)
    ElMessage.error(err?.response?.data?.message || 'Không áp dụng được voucher, vui lòng kiểm tra lại.')
    // Reset discount nếu fail
    discountAmount.value = 0
    finalTotal.value = totalPrice.value + shippingFee.value
  }
}

// ✨ NEW FUNCTION: Automatic Best Voucher Application
const applyBestVoucherAutomatically = async () => {
  const customerId = form.value.customerId;
  const orderTotal = totalPrice.value;

  // Only attempt to apply if customerId is known and orderTotal is greater than 0
  if (customerId && orderTotal > 0) {
    try {
      // Clear any manually entered discount code
      discountCode.value = '';

      const res = await axios.get('http://localhost:8080/api/online-sale/vouchers/apply-best', { // Corrected line
        params: {
          customerId: customerId,
          orderTotal: orderTotal
        }
      });

      // If a best voucher is found
      if (res.data) {
        appliedVoucher.value = res.data;
        // Corrected variable name: appliedVoucher.value.voucherName instead of appliedVulatedVoucher.value.voucherName
        ElMessage.success(`Tự động áp dụng voucher tốt nhất: ${appliedVoucher.value.voucherName}`);

        let discount = 0;
        if (appliedVoucher.value.discountPercentage) {
          discount = Math.min(
            (orderTotal * appliedVoucher.value.discountPercentage) / 100,
            appliedVoucher.value.maxDiscountValue || Infinity
          );
        } else if (appliedVoucher.value.discountAmount) {
          discount = appliedVoucher.value.discountAmount;
        }

        discountAmount.value = discount;
        finalTotal.value = orderTotal - discountAmount.value + shippingFee.value;
      } else {
        // No suitable voucher found, just keep current state
        ElMessage.info('Không tìm thấy voucher phù hợp nào để tự động áp dụng.');
        discountAmount.value = 0;
        finalTotal.value = orderTotal + shippingFee.value;
        appliedVoucher.value = null; // Ensure no voucher is considered applied
      }

    } catch (err) {
      console.error('❌ Lỗi tự động áp dụng voucher:', err);
      // If no best voucher is found or an error occurs, simply don't apply one automatically.
      // The backend should return a 404 or handle null gracefully.
      ElMessage.info('Không tìm thấy voucher phù hợp để tự động áp dụng.');
      discountAmount.value = 0;
      finalTotal.value = orderTotal + shippingFee.value;
      appliedVoucher.value = null; // Ensure no voucher is considered applied
    }
  } else {
    // If customerId is not available or orderTotal is zero, ensure no discount is applied.
    discountAmount.value = 0;
    appliedVoucher.value = null;
    finalTotal.value = orderTotal + shippingFee.value;
  }
};

=======
const vouchers = ref([])
const voucherDialog = ref({
  visible: false,
  loading: false,
  search: '',
  filtered: [],
})
>>>>>>> c2ada23183d58b8b9798e6d957e75e644f7755fe

/** ====== Form ====== */
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
    fullAddress: '70000, Vietnam',
  },
})

/** ====== Rules ====== */
const rules = {
  email: [{ required: true, message: 'Vui lòng nhập email ', trigger: 'blur' }],
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

/** ====== Computed ====== */
const totalPrice = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
)

/** ====== Format helpers ====== */
const formatPrice = (val) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)

const formatCurrency = (val) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)

/** ====== Voucher helpers ====== */
const isVoucherUsable = (voucher) => {
  if (!voucher || voucher.status !== 1) return false
  if ((voucher.minOrderValue || 0) > totalPrice.value) return false
  return true
}

const voucherDisableReason = (voucher) => {
  if (voucher.status !== 1) return 'Voucher đã hết hạn hoặc không hoạt động'
  if ((voucher.minOrderValue || 0) > totalPrice.value) {
    const delta = (voucher.minOrderValue || 0) - totalPrice.value
    return `Cần mua thêm ${formatCurrency(delta)} để dùng voucher này`
  }
  return 'Không đủ điều kiện'
}

const computeDiscount = (orderTotal, voucher) => {
  let discount = 0
  if (voucher?.discountPercentage) {
    discount = Math.floor((orderTotal * voucher.discountPercentage) / 100)
    if (voucher.maxDiscountValue) {
      discount = Math.min(discount, voucher.maxDiscountValue)
    }
  } else if (voucher?.discountAmount) {
    discount = voucher.discountAmount
  }
  return Math.max(0, Math.min(discount, orderTotal))
}

const recalcFinal = () => {
  const orderTotal = totalPrice.value
  const ship = shippingFee.value
  const disc = appliedVoucher.value ? computeDiscount(orderTotal, appliedVoucher.value) : 0
  discountAmount.value = disc
  finalTotal.value = orderTotal - disc + ship
}

/** ====== Actions: Apply/Cancel Voucher ====== */
const cancelVoucher = () => {
  appliedVoucher.value = null
  // không xoá discountCode để user có thể nhấn SỬ DỤNG áp lại nếu muốn
  recalcFinal()
  ElMessage.info('Đã hủy bỏ mã giảm giá.')
}

const cancelVoucherFromDialog = () => {
  cancelVoucher()
}

const applyVoucherFromDialog = (voucher) => {
  if (!isVoucherUsable(voucher)) return
  appliedVoucher.value = voucher
  discountCode.value = voucher.voucherCode || ''
  recalcFinal()
  // để người dùng thấy rõ thay đổi, mình KHÔNG auto đóng dialog
  ElMessage.success(`Áp dụng voucher: ${voucher.voucherName}`)
}

const applyDiscountCode = async () => {
  if (!discountCode.value?.trim()) {
    ElMessage.warning('Vui lòng nhập mã giảm giá!')
    return
  }
  try {
    const res = await axios.get('http://localhost:8080/api/online-sale/vouchers/apply', {
      params: {
        customerId: form.value.customerId || 0,
        voucherCode: discountCode.value.trim(),
        orderTotal: totalPrice.value,
      },
    })
    appliedVoucher.value = res.data
    // đảm bảo đồng bộ input với voucher server trả về (tránh case nhập chữ thường/hoa)
    discountCode.value = appliedVoucher.value?.voucherCode || discountCode.value
    recalcFinal()
    ElMessage.success(`Áp dụng voucher thành công: ${appliedVoucher.value.voucherName}`)
  } catch (err) {
    console.error('❌ Lỗi áp dụng voucher:', err)
    ElMessage.error(err?.response?.data?.message || 'Không áp dụng được voucher, vui lòng kiểm tra lại.')
  }
}

/** ====== Voucher dialog open/fetch/filter ====== */
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
    console.error('❌ Lỗi tải voucher:', err)
    ElMessage.error('Không thể tải danh sách voucher.')
    vouchers.value = []
    voucherDialog.value.filtered = []
  } finally {
    voucherDialog.value.loading = false
  }
}

const filterVouchers = () => {
  const q = voucherDialog.value.search?.trim().toLowerCase()
  if (!q) {
    voucherDialog.value.filtered = [...vouchers.value]
    return
  }
  voucherDialog.value.filtered = vouchers.value.filter(v =>
    (v.voucherCode || '').toLowerCase().includes(q) ||
    (v.voucherName || '').toLowerCase().includes(q)
  )
}

const isCurrentApplied = (row) => {
  if (!appliedVoucher.value) return false
  return (row?.voucherCode || '') === (appliedVoucher.value?.voucherCode || '')
}

/** ====== Auto-apply best voucher (optional) ====== */
const applyBestVoucherAutomatically = async () => {
  const customerId = form.value.customerId
  const orderTotal = totalPrice.value
  if (customerId && orderTotal > 0) {
    try {
      const res = await axios.get('http://localhost:8080/api/online-sale/vouchers/apply-best', {
        params: { customerId, orderTotal },
      })
      if (res.data) {
        appliedVoucher.value = res.data
        discountCode.value = res.data.voucherCode || ''
        recalcFinal()
        ElMessage.success(`Tự động áp dụng voucher tốt nhất: ${appliedVoucher.value.voucherName}`)
        return
      }
      if (!appliedVoucher.value) recalcFinal()
      ElMessage.info('Không tìm thấy voucher phù hợp để tự động áp dụng.')
    } catch (err) {
      console.error('❌ Lỗi tự động áp dụng voucher:', err)
      if (!appliedVoucher.value) recalcFinal()
    }
  } else {
    if (!appliedVoucher.value) recalcFinal()
  }
}

/** ====== GHN loads ====== */
const loadProvinces = async () => {
  try {
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/province',
      {},
      { headers: { Token: GHN_TOKEN } }
    )
    provinces.value = res.data.data
  } catch (err) {
    console.error('❌ Lỗi load tỉnh:', err)
    ElMessage.error('Không thể tải danh sách tỉnh/thành phố.')
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
      }
    )
    districts.value = res.data.data
  } catch (err) {
    console.error('❌ Lỗi load quận:', err)
    ElMessage.error('Không thể tải danh sách quận/huyện.')
  }
}

const loadWards = async () => {
  if (!form.value.address.districtCode) return
  try {
    const res = await axios.get('https://online-gateway.ghn.vn/shiip/public-api/master-data/ward', {
      headers: { Token: GHN_TOKEN },
      params: { district_id: form.value.address.districtCode },
    })
    wards.value = res.data.data
  } catch (err) {
    console.error('❌ Lỗi load phường:', err)
    ElMessage.error('Không thể tải danh sách phường/xã.')
  }
}

/** ====== Shipping fee ====== */
const calculateShippingFee = async () => {
  if (!form.value.address.districtCode || !form.value.address.wardCode) {
    shippingFee.value = 0
    recalcFinal()
    return
  }
  try {
    const totalWeight = cartItems.value.reduce((sum, item) => sum + (item.weight || 200) * item.quantity, 0)
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee',
      {
        from_district_id: FROM_DISTRICT_ID,
        from_ward_code: FROM_WARD_CODE,
        to_district_id: form.value.address.districtCode,
        to_ward_code: form.value.address.wardCode,
        weight: Math.max(totalWeight, 100),
        service_type_id: 2,
      },
      {
        headers: {
          'Content-Type': 'application/json',
          Token: GHN_TOKEN,
          ShopId: SHOP_ID,
        },
      }
    )
    shippingFee.value = res.data.data.total || 0
    ElMessage.success(`Phí vận chuyển: ${formatPrice(shippingFee.value)}`)
    recalcFinal()
  } catch (err) {
    console.error('❌ Lỗi tính phí ship:', err)
    ElMessage.error('Không thể tính phí vận chuyển. Vui lòng thử lại.')
    shippingFee.value = 0
    recalcFinal()
  }
}

/** ====== Address change handlers ====== */
const onProvinceChange = async () => {
  const selected = provinces.value.find((p) => p.ProvinceID === form.value.address.provinceCode)
  form.value.address.provinceName = selected?.ProvinceName || ''
  form.value.address.districtCode = null
  form.value.address.districtName = ''
  form.value.address.wardCode = ''
  form.value.address.wardName = ''
  districts.value = []
  wards.value = []
  shippingFee.value = 0
  await loadDistricts()
}

const onDistrictChange = async () => {
  const selected = districts.value.find((d) => d.DistrictID === form.value.address.districtCode)
  form.value.address.districtName = selected?.DistrictName || ''
  form.value.address.wardCode = ''
  form.value.address.wardName = ''
  wards.value = []
  shippingFee.value = 0
  await loadWards()
}

const onWardChange = async () => {
  const selected = wards.value.find((w) => w.WardCode === form.value.address.wardCode)
  form.value.address.wardName = selected?.WardName || ''
  await calculateShippingFee()
}

/** ====== Submit ====== */
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('Vui lòng điền đầy đủ và chính xác thông tin giao hàng.')
      return
    }
    if (!cartItems.value.length) {
      ElMessage.warning('Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm trước khi đặt.')
      router.push('/')
      return
    }

    isSubmitting.value = true
    const loadingInstance = ElLoading.service({ fullscreen: true, text: 'Đang đặt hàng...' })
    try {
      const payload = {
        customerInfo: {
          ...form.value,
          address: {
            ...form.value.address,
            provinceName: provinces.value.find(p => p.ProvinceID === form.value.address.provinceCode)?.ProvinceName || '',
            districtName: districts.value.find(d => d.DistrictID === form.value.address.districtCode)?.DistrictName || '',
            wardName: wards.value.find(w => w.WardCode === form.value.address.wardCode)?.WardName || '',
          },
        },
        items: cartItems.value.map(item => ({
          productDetailId: item.productDetailId,
          quantity: item.quantity,
          sellPrice: item.sellPrice,
          discountedPrice: item.discountedPrice,
          discountPercentage: item.discountPercentage,
          discountCampaignId: item.discountCampaignId || null,
        })),
        discountAmount: discountAmount.value || 0,
        voucherCode: appliedVoucher.value?.voucherCode || null,
        discountCampaignId: cartItems.value[0]?.discountCampaignId || null,
        description: form.value.description,
        orderType: 1,
        status: 1,
        employeeId: null,
        shippingFee: shippingFee.value,
      }

      if (paymentMethod.value === 1) {
        const res = await axios.post('http://localhost:8080/api/payment/zalo/create', payload)
        const zaloPay = res.data?.zaloPay
        const customerId = res.data?.invoice?.customerId
        if (customerId) localStorage.setItem('userId', String(customerId))
        if (zaloPay?.orderUrl && zaloPay?.appTransId) {
          localStorage.setItem('appTransId', zaloPay.appTransId)
          clearCart()
          cartItems.value = []
          ElMessage.success('Đang chuyển hướng đến ZaloPay để thanh toán...')
          window.location.href = zaloPay.orderUrl
        } else {
          ElMessage.error('Không nhận được URL thanh toán từ ZaloPay. Vui lòng thử lại.')
        }
      } else {
        const res = await axios.post('http://localhost:8080/api/online-sale/checkout', payload)
        const customerId = res.data?.invoice?.customerId
        if (customerId) localStorage.setItem('userId', String(customerId))
        clearCart()
        cartItems.value = []
        ElMessage.success('Đặt hàng thành công! Đơn hàng của bạn sẽ sớm được giao.')
        router.push('/payment-result')
      }
    } catch (err) {
      console.error('❌ Lỗi đặt hàng:', err)
      ElMessage.error(`Đặt hàng thất bại: ${err?.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.'}`)
    } finally {
      isSubmitting.value = false
      loadingInstance.close()
    }
  })
}

/** ====== Watchers ====== */
watch([totalPrice, shippingFee], recalcFinal)

watch([() => form.value.customerId, totalPrice], async ([cid, total]) => {
  if (cid && total > 0 && !appliedVoucher.value) {
    await applyBestVoucherAutomatically()
  } else {
    recalcFinal()
  }
})

/** ====== Mounted ====== */
onMounted(async () => {
  cartItems.value = getCart()
  if (cartItems.value.length === 0) {
    ElMessage.warning('Giỏ hàng trống! Bạn sẽ được chuyển hướng về trang chủ.')
    router.push('/')
    return
  }

  await loadProvinces()

  const userId = localStorage.getItem('userId')
  if (userId) {
    let loadingInstance = ElLoading.service({ fullscreen: true, text: 'Đang tải thông tin...' })
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
            ? `${customer.houseName}, ${customer.wardName}, ${customer.districtName}, ${customer.provinceName}`
            : '70000, Vietnam',
        },
      }
      if (form.value.address.provinceCode) await loadDistricts()
      if (form.value.address.districtCode) await loadWards()
      if (form.value.address.districtCode && form.value.address.wardCode) {
        await calculateShippingFee()
      }
    } catch (err) {
      console.error('❌ Không lấy được thông tin khách hàng:', err)
      ElMessage.error('Không thể tải thông tin khách hàng tự động.')
    } finally {
      loadingInstance.close()
    }
  }

  recalcFinal()
  await applyBestVoucherAutomatically()
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
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,.05);
}
.section-header {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.delivery-form .el-form-item { margin-bottom: 15px; }
.phone-hint { font-size: 12px; color: #f56c6c; margin-top: 5px; }
.shipping-method-section { margin-top: 30px; padding-top: 20px; border-top: 1px dashed #eee; }

.payment-option {
  border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px 15px;
  display: flex; align-items: center; justify-content: flex-start; width: 100%;
}
.payment-option.is-checked { border-color: #409eff; background-color: #ecf5ff; }
.payment-content { display: flex; align-items: center; gap: 10px; }
.payment-icon { height: 24px; width: auto; object-fit: contain; }
.submit-button {
  width: 100%; margin-top: 30px; padding: 14px 0; font-size: 18px; font-weight: bold;
  background-color: #f56c6c; border-color: #f56c6c;
}
.submit-button:hover, .submit-button:focus { background-color: #f78989; border-color: #f78989; }

/* Right column */
.order-summary-section {
  padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;
  background-color: #f9f9f9; box-shadow: 0 2px 8px rgba(0,0,0,.05);
}
.summary-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #eee;
}
.summary-title { font-size: 16px; font-weight: bold; color: #333; }
.total-price-header { font-size: 20px; font-weight: bold; color: #f56c6c; }
.order-items-list { border-bottom: 1px solid #eee; padding-bottom: 15px; margin-bottom: 15px; }
.order-item { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; font-size: 14px; }
.item-details { flex-grow: 1; }
.item-name { font-weight: 500; color: #333; line-height: 1.4; }
.item-variant { color: #999; font-size: 12px; }
.item-quantity { width: 60px; text-align: center; color: #666; flex-shrink: 0; }
.item-price { font-weight: 500; color: #333; width: 100px; text-align: right; flex-shrink: 0; }

/* Discount row */
.discount-code-section {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  align-items: center;
}
.discount-input { flex: 1 1 220px; min-width: 220px; }
.apply-discount-button { background-color: #409EFF; border-color: #409EFF; color: #fff; font-weight: bold; height: auto; }
.view-voucher-button { height: auto; }
.cancel-discount-button { height: auto; }

.applied-voucher-note { margin-bottom: 10px; }

.loyal-customer-text { font-size: 13px; color: #999; margin-bottom: 20px; text-align: right; }
.summary-totals { padding-top: 15px; border-top: 1px dashed #e0e0e0; margin-bottom: 20px; }
.total-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; font-size: 15px; color: #555; }
.total-row .value { font-weight: bold; color: #333; }
.discount-value, .shipping-value { font-weight: normal; }
.total-row.final-total-row { margin-top: 20px; font-size: 20px; }
.final-total-row .label { font-size: 18px; font-weight: bold; color: #333; }
.final-total-row .value.final-value { font-size: 24px; font-weight: bold; color: #f56c6c; }

.delivery-time-warning { font-size: 12px; color: #f56c6c; text-align: center; margin-top: 20px; }

@media (max-width: 991px) {
  .checkout-page { padding: 15px; }
  .el-col-md-14, .el-col-md-10 { width: 100%; }
  .order-summary-section { margin-top: 30px; }
}
</style>

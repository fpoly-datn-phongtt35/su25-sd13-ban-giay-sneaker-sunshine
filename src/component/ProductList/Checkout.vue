<template>
  <div class="checkout-page">
    <el-row :gutter="30" class="checkout-content">
      <el-col :span="14" :xs="24" :sm="24" :md="14">
        <div class="shipping-info-section">
          <div class="section-header">THÔNG TIN GIAO HÀNG</div>

          <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="delivery-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="HỌ VÀ TÊN" prop="customerName">
                  <el-input v-model="form.customerName" placeholder="phan tuan anh"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="SỐ ĐIỆN THOẠI" prop="phone">
                  <el-input v-model="form.phone" placeholder="Số điện thoại"></el-input>
                  <div class="phone-hint"></div>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="ĐỊA CHỈ" prop="address.houseName">
                  <el-input v-model="form.address.houseName" placeholder="Địa chỉ"></el-input>
                </el-form-item>
              </el-col>
            </el-row>

             <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="Gmail" prop="address.houseName">
                  <el-input v-model="form.address.houseName" placeholder="Địa chỉ"></el-input>
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

          <div class="discount-code-section">
  <el-input v-model="discountCode" placeholder="Nhập Mã Giảm Giá" class="discount-input"></el-input>
  <el-button type="primary" class="apply-discount-button" @click="applyDiscountCode">SỬ DỤNG</el-button>
  <el-button
    v-if="appliedVoucher"
    type="danger"
    class="cancel-discount-button"
    @click="cancelVoucher">
    HỦY BỎ
  </el-button>
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
            Do lượng đơn hàng cao, thời gian giao hàng dự kiến sẽ tăng thêm 2-3 ngày. Rất mong Quý Khách thông cảm.
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { ElMessage, ElLoading, ElAvatar } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCart, clearCart } from '@/utils/cart'
import { UserFilled } from '@element-plus/icons-vue'

const GHN_TOKEN = '741f1c91-4f42-11f0-8cf5-d2552bfd31d8'
const SHOP_ID = 5851480
const FROM_DISTRICT_ID = 1483
const FROM_WARD_CODE = '21108'
const router = useRouter()

const formRef = ref(null)
const cartItems = ref([])
const provinces = ref([])
const districts = ref([])
const wards = ref([])

const shippingFee = ref(0)
const finalTotal = ref(0)
const isSubmitting = ref(false)
const paymentMethod = ref(0) // Default to COD (0), ZaloPay (1)
const deliveryType = ref(0); // 0 for Giao tận nơi, 1 for Nhận tại cửa hàng
const addressType = ref(0); // 0 for Nhà, 1 for Công ty
const discountCode = ref(''); // For the discount code input
const appliedVoucher = ref(null)
const discountAmount = ref(0)

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
    const res = await axios.get('http://localhost:8080/api/admin/vouchers/apply', {
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


const form = ref({
  customerId: null,
  customerName: 'phan tuan anh', // Pre-fill from image
  phone: '',
  email: 'phantuananh@gmail.com', // Pre-fill from image
  description: '', // Not shown in image, but kept for backend
  address: {
    country: 'Việt Nam',
    provinceCode: null,
    provinceName: '',
    districtCode: null,
    districtName: '',
    wardCode: '',
    wardName: '',
    houseName: '',
    fullAddress: '70000, Vietnam' // Placeholder for the address selection box
  },
})

// Validation Rules - Adjusted based on image's visible fields and hints
const rules = {
  customerName: [{ required: true, message: 'Vui lòng nhập họ và tên', trigger: 'blur' }],
  phone: [
    { required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' },
    { pattern: /^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$/, message: 'Số điện thoại không hợp lệ', trigger: 'blur' }
  ],
  'address.houseName': [{ required: true, message: 'Vui lòng nhập địa chỉ', trigger: 'blur' }],
  'address.provinceCode': [{ required: true, message: 'Vui lòng chọn tỉnh/thành phố', trigger: 'change' }],
  'address.districtCode': [{ required: true, message: 'Vui lòng chọn quận/huyện', trigger: 'change' }],
  'address.wardCode': [{ required: true, message: 'Vui lòng chọn phường/xã', trigger: 'change' }],
}

const totalPrice = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0),
)

// Update finalTotal whenever totalPrice or shippingFee changes
watch([totalPrice, shippingFee], () => {
  finalTotal.value = totalPrice.value + shippingFee.value;
});

const formatPrice = (val) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)

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
    let loadingInstance = ElLoading.service({ fullscreen: true, text: 'Đang tải thông tin...' });
    try {
      const res = await axios.get(`http://localhost:8080/api/admin/customers/${userId}`)
      const customer = res.data

      // Pre-fill form with customer data
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
            : '70000, Vietnam' // Fallback or computed
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
      loadingInstance.close();
    }
  }

  // Initial calculation of finalTotal
  finalTotal.value = totalPrice.value + shippingFee.value;
})

const loadProvinces = async () => {
  try {
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/province',
      {},
      { headers: { Token: GHN_TOKEN } },
    )
    provinces.value = res.data.data
  } catch (err) {
    console.error('❌ Lỗi load tỉnh:', err)
    ElMessage.error('Không thể tải danh sách tỉnh/thành phố.')
  }
}

const loadDistricts = async () => {
  if (!form.value.address.provinceCode) return;
  try {
    const res = await axios.get(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/district',
      {
        headers: { Token: GHN_TOKEN },
        params: { province_id: form.value.address.provinceCode },
      },
    )
    districts.value = res.data.data
  } catch (err) {
    console.error('❌ Lỗi load quận:', err)
    ElMessage.error('Không thể tải danh sách quận/huyện.')
  }
}

const loadWards = async () => {
  if (!form.value.address.districtCode) return;
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

const calculateShippingFee = async () => {
  if (!form.value.address.districtCode || !form.value.address.wardCode) {
    shippingFee.value = 0;
    return;
  }
  try {
    const totalWeight = cartItems.value.reduce((sum, item) => sum + (item.weight || 200) * item.quantity, 0);

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
      },
    )
    shippingFee.value = res.data.data.total || 0
    ElMessage.success(`Phí vận chuyển: ${formatPrice(shippingFee.value)}`);
  } catch (err) {
    console.error('❌ Lỗi tính phí ship:', err)
    ElMessage.error('Không thể tính phí vận chuyển. Vui lòng thử lại.')
    shippingFee.value = 0
  }
}

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

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('🛑 Vui lòng điền đầy đủ và chính xác thông tin giao hàng.')
      return
    }

    if (!cartItems.value.length) {
      ElMessage.warning('🛑 Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm vào giỏ hàng trước khi đặt.')
      router.push('/')
      return
    }

    isSubmitting.value = true
    const loadingInstance = ElLoading.service({ fullscreen: true, text: 'Đang đặt hàng...' })

    try {
      // Chuẩn bị dữ liệu payload gửi lên server
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
        items: cartItems.value.map(item => ({
          productDetailId: item.productDetailId,
          quantity: item.quantity,
          sellPrice: item.sellPrice,
          discountedPrice: item.discountedPrice,
          discountPercentage: item.discountPercentage,
        })),
        discountAmount: discountAmount.value || 0,                  // ✅ trừ giảm giá
        voucherCode: appliedVoucher.value?.voucherCode || null,     // ✅ gửi mã voucher (nếu có)
        description: form.value.description,
        orderType: 1,        // Mặc định: đơn online
        status: 1,           // Mặc định
        employeeId: null,
        shippingFee: shippingFee.value
      }

      console.log(' Payload gửi lên server:', JSON.stringify(payload, null, 2))

      if (paymentMethod.value === 1) {
        // 👉 ZaloPay
        const res = await axios.post('http://localhost:8080/api/payment/zalo/create', payload)
        const zaloPay = res.data?.zaloPay

        if (zaloPay?.orderUrl && zaloPay?.appTransId) {
          localStorage.setItem('appTransId', zaloPay.appTransId)
          clearCart()
          cartItems.value = []
          ElMessage.success('Đang chuyển hướng đến ZaloPay để thanh toán...')
          window.location.href = zaloPay.orderUrl
        } else {
          ElMessage.error(' Không nhận được URL thanh toán từ ZaloPay. Vui lòng thử lại.')
        }

      } else {
        //  Thanh toán COD
        await axios.post('http://localhost:8080/api/online-sale/checkout', payload)
        clearCart()
        cartItems.value = []
        ElMessage.success(' Đặt hàng thành công! Đơn hàng của bạn sẽ sớm được giao.')
        router.push('/don-hang')
      }

    } catch (err) {
      console.error('❌ Lỗi đặt hàng:', err)
      ElMessage.error(`❌ Đặt hàng thất bại: ${err?.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.'}`)
    } finally {
      isSubmitting.value = false
      loadingInstance.close()
    }
  })
}

</script>

<style scoped>
.checkout-page {
  padding: 30px;
  max-width: 1400px;
  margin: 20px auto;
  font-family: Arial, sans-serif; /* Consistent font */
}

/* Custom Styles to match the image */

/* Left Column - Shipping Information */
.shipping-info-section {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.user-profile-summary {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
}

.user-avatar {
  background-color: #f0f2f5;
  color: #c0c4cc;
}

.user-details {
  flex-grow: 1;
}

.user-name {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.user-email {
  color: #666;
  font-size: 14px;
}

.logout-button {
  color: #409EFF;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  height: auto;
}

.address-selection-box {
  margin-bottom: 25px;
}

.input-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
  display: block;
  font-weight: bold;
}

.full-address-select {
  width: 100%;
}

/* Form inputs */
.delivery-form .el-form-item {
  margin-bottom: 15px; /* Tighter spacing */
}

.delivery-form .el-input__inner,
.delivery-form .el-select__inner,
.delivery-form .el-textarea__inner {
  border: 1px solid #ccc; /* Lighter border */
  border-radius: 4px;
  padding: 10px 15px;
}

.delivery-form .el-input {
  height: 40px; /* Standard height */
}

.phone-hint {
  font-size: 12px;
  color: #f56c6c; /* Red color for warning */
  margin-top: 5px;
}

.delivery-type-radio-group,
.address-type-radio-group {
  margin-bottom: 20px;
}

.delivery-type-radio-group .el-radio,
.address-type-radio-group .el-radio {
  margin-right: 20px; /* Spacing between radios */
  padding: 0; /* Remove default padding */
  height: auto;
  line-height: normal;
}

.delivery-type-radio-group .el-radio__label,
.address-type-radio-group .el-radio__label {
  font-size: 14px;
}

.shipping-method-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #eee; /* Dashed line as in image */
}


.payment-option {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
}

.payment-option.is-checked {
  border-color: #409eff;
  background-color: #ecf5ff; /* Light blue background for selected */
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
  font-weight: bold;
  background-color: #f56c6c; /* Red color as in the image's "Thành Tiền" */
  border-color: #f56c6c;
}
.submit-button:hover, .submit-button:focus {
  background-color: #f78989;
  border-color: #f78989;
}

/* Right Column - Order Summary */
.order-summary-section {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #f9f9f9; /* Light grey background */
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
  font-weight: bold;
  color: #333;
}

.total-price-header {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c; /* Red color */
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
  width: 60px; /* Fixed width for quantity */
  text-align: center;
  color: #666;
  flex-shrink: 0;
}

.item-price {
  font-weight: 500;
  color: #333;
  width: 100px; /* Fixed width for price */
  text-align: right;
  flex-shrink: 0;
}

.discount-code-section {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.discount-input {
  flex-grow: 1;
}

.discount-input .el-input__inner {
  border-color: #dcdfe6;
}

.apply-discount-button {
  background-color: #409EFF;
  border-color: #409EFF;
  color: #fff;
  font-weight: bold;
  padding: 8px 15px;
  height: auto;
}

.loyal-customer-text {
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
  text-align: right;
}

.summary-totals {
  padding-top: 15px;
  border-top: 1px dashed #e0e0e0; /* Dashed line */
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

.total-row .label {
  font-weight: normal; /* As in image */
}

.total-row .value {
  font-weight: bold;
  color: #333;
}

.discount-value, .shipping-value {
  font-weight: normal; /* To match image, '...' and '+' are not bold */
}

.total-row.final-total-row {
  border-top: none; /* No extra border for final total row */
  margin-top: 20px;
  padding-top: 0;
  font-size: 20px;
}

.final-total-row .label {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.final-total-row .value.final-value {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c; /* Red color for final total */
}

.delivery-time-warning {
  font-size: 12px;
  color: #f56c6c; /* Red warning text */
  text-align: center;
  margin-top: 20px;
}

/* Responsive adjustments */
@media (max-width: 991px) {
  .checkout-page {
    padding: 15px;
  }
  .el-col-md-14, .el-col-md-10 {
    width: 100%;
  }
  .order-summary-section {
    margin-top: 30px;
  }
}

@media (max-width: 767px) {
  .section-header {
    font-size: 14px;
  }
  .user-profile-summary {
    flex-wrap: wrap;
  }
  .user-avatar {
    margin-right: 0;
  }
  .logout-button {
    width: 100%;
    text-align: right;
    margin-top: 5px;
  }
  .delivery-form .el-col-sm-12 {
    width: 100%;
  }
  .payment-option {
    padding: 8px 12px;
  }
  .submit-button {
    font-size: 16px;
    padding: 12px 0;
  }
  .summary-header {
    font-size: 14px;
  }
  .total-price-header {
    font-size: 18px;
  }
  .order-item {
    flex-wrap: wrap;
  }
  .item-quantity, .item-price {
    width: auto;
    flex-basis: 50%;
    text-align: left;
    margin-top: 5px;
  }
  .item-price {
    text-align: right;
  }
  .total-row {
    font-size: 14px;
  }
  .final-total-row .label {
    font-size: 16px;
  }
  .final-total-row .value.final-value {
    font-size: 20px;
  }
  .delivery-time-warning {
    font-size: 11px;
  }
}
</style>

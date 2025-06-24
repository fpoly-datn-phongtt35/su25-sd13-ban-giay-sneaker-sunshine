<template>
  <div class="checkout-container">
    <h2>🗳️ Thông tin thanh toán</h2>

    <el-row :gutter="30">
      <!-- Form thông tin khách hàng -->
      <el-col :span="14">
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Họ và tên" prop="customerName">
                <el-input v-model="form.customerName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số điện thoại" prop="phone">
                <el-input v-model="form.phone" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Email" prop="email">
                <el-input v-model="form.email" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số nhà, đường" prop="address.houseName">
                <el-input v-model="form.address.houseName" />
              </el-form-item>
            </el-col>

            <!-- Tỉnh -->
            <el-col :span="8">
              <el-form-item label="Tỉnh / Thành phố" prop="address.provinceName">
                <el-select
                  v-model.number="form.address.provinceCode"
                  placeholder="Chọn tỉnh"
                  @change="onProvinceChange"
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

            <!-- Quận -->
            <el-col :span="8">
              <el-form-item label="Quận / Huyện" prop="address.districtName">
                <el-select
                  v-model.number="form.address.districtCode"
                  placeholder="Chọn quận"
                  @change="onDistrictChange"
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

            <!-- Phường -->
            <el-col :span="8">
              <el-form-item label="Phường / Xã" prop="address.wardName">
                <el-select
                  v-model="form.address.wardCode"
                  placeholder="Chọn phường"
                  @change="onWardChange"
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

            <!-- Ghi chú -->
            <el-col :span="24">
              <el-form-item label="Ghi chú đơn hàng">
                <el-input type="textarea" v-model="form.description" :rows="3" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit">🛍️ Đặt hàng</el-button>
          </el-form-item>
        </el-form>
      </el-col>

      <!-- Giỏ hàng -->
      <el-col :span="10">
        <h3>🚲 Giỏ hàng</h3>
        <el-table :data="cartItems" border size="small">
          <el-table-column prop="productName" label="Sản phẩm" />
          <el-table-column label="SL" width="60">
            <template #default="{ row }">{{ row.quantity }}</template>
          </el-table-column>
          <el-table-column label="Giá" width="100">
            <template #default="{ row }">{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column label="Tổng" width="100">
            <template #default="{ row }">{{ formatPrice(row.price * row.quantity) }}</template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 10px">
          <strong>Phí vận chuyển:</strong> {{ formatPrice(shippingFee) }}<br />
          <strong>Tổng tiền:</strong> {{ formatPrice(finalTotal) }}
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCart } from '@/utils/cart'

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
  },
})

const rules = {
  email: [{ type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }],
  'address.houseName': [{ required: true, message: 'Nhập số nhà, đường', trigger: 'blur' }],
  'address.provinceName': [{ required: true, message: 'Chọn tỉnh', trigger: 'blur' }],
  'address.districtName': [{ required: true, message: 'Chọn quận', trigger: 'blur' }],
  'address.wardName': [{ required: true, message: 'Chọn phường', trigger: 'blur' }],
}

const totalPrice = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0),
)

const formatPrice = (val) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)

onMounted(async () => {
  cartItems.value = getCart()
  await loadProvinces()

  const userId = localStorage.getItem('userId')
  if (userId) {
    try {
      const res = await axios.get(`http://localhost:8080/api/admin/customers/${userId}`)
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
        },
      }

      if (form.value.address.provinceCode) await loadDistricts()
      if (form.value.address.districtCode) await loadWards()
      if (form.value.address.districtCode && form.value.address.wardCode) {
        await calculateShippingFee()
      }
    } catch (err) {
      console.error('❌ Không lấy được thông tin khách hàng:', err)
    }
  }
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
  }
}

const loadDistricts = async () => {
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
  }
}

const loadWards = async () => {
  try {
    const res = await axios.get('https://online-gateway.ghn.vn/shiip/public-api/master-data/ward', {
      headers: { Token: GHN_TOKEN },
      params: { district_id: form.value.address.districtCode },
    })
    wards.value = res.data.data
  } catch (err) {
    console.error('❌ Lỗi load phường:', err)
  }
}

const calculateShippingFee = async () => {
  if (!form.value.address.districtCode || !form.value.address.wardCode) return
  try {
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee',
      {
        from_district_id: FROM_DISTRICT_ID,
        from_ward_code: FROM_WARD_CODE,
        to_district_id: form.value.address.districtCode,
        to_ward_code: form.value.address.wardCode,
        weight: 200,
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
    finalTotal.value = totalPrice.value + shippingFee.value
  } catch (err) {
    console.error('❌ Lỗi tính phí ship:', err)
    shippingFee.value = 0
    finalTotal.value = totalPrice.value
  }
}

const onProvinceChange = async () => {
  const selected = provinces.value.find((p) => p.ProvinceID === form.value.address.provinceCode)
  form.value.address.provinceName = selected?.ProvinceName || ''
  form.value.address.districtCode = null
  form.value.address.districtName = ''
  form.value.address.wardCode = ''
  form.value.address.wardName = ''
  shippingFee.value = 0
  finalTotal.value = totalPrice.value
  await loadDistricts()
}

const onDistrictChange = async () => {
  const selected = districts.value.find((d) => d.DistrictID === form.value.address.districtCode)
  form.value.address.districtName = selected?.DistrictName || ''
  form.value.address.wardCode = ''
  form.value.address.wardName = ''
  shippingFee.value = 0
  finalTotal.value = totalPrice.value
  await loadWards()
}

const onWardChange = async () => {
  const selected = wards.value.find((w) => w.WardCode === form.value.address.wardCode)
  form.value.address.wardName = selected?.WardName || ''
  await calculateShippingFee()
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    if (!cartItems.value.length) {
      ElMessage.warning('🛑 Giỏ hàng trống!')
      return
    }

    const payload = {
      customerInfo: form.value,
      items: cartItems.value.map((item) => ({
        productDetailId: item.productDetailId,
        quantity: item.quantity,
      })),
      discountAmount: 0,
      description: form.value.description,
      orderType: 1,
      status: 1,
      employeeId: null,
      shippingFee: shippingFee.value,
    }

    try {
      const res = await axios.post('http://localhost:8080/api/payment/zalo/create', payload)
      const invoiceData = res.data?.invoiceData
      const zaloPay = res.data?.zaloPay

      const appTransId = zaloPay?.appTransId
      const orderUrl = zaloPay?.orderUrl

      if (orderUrl && appTransId) {
        // 👉 Lưu appTransId để tra cứu sau thanh toán
        localStorage.setItem('appTransId', appTransId)

        // 👉 Redirect user đến trang thanh toán
        window.location.href = orderUrl
      } else {
        ElMessage.error('❌ Không nhận được orderUrl từ ZaloPay')
      }
    } catch (err) {
      console.error('❌ Lỗi đặt hàng:', err)
      ElMessage.error(`❌ ${err?.response?.data?.message || 'Có lỗi xảy ra'}`)
    }
  })
}

</script>

<style scoped>
.checkout-container {
  padding: 20px;
}
</style>

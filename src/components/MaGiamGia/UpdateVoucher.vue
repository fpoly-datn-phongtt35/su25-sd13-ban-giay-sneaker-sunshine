<template>
  <el-container class="min-h-screen bg-gray-100">
    <el-main class="p-6">
      <el-card class="max-w-4xl mx-auto" shadow="always">
        <template #header>
          <div class="flex items-center gap-4">
            <el-button type="info" circle @click="goBack" title="Quay lại">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <h2 class="text-2xl font-bold text-gray-800">
              <el-icon class="mr-2 text-orange-500"><Ticket /></el-icon>
              Cập nhật Voucher
            </h2>
          </div>
        </template>

        <el-form
          ref="voucherForm"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="onSubmit"
        >
          <!-- Tên -->
          <el-form-item label="Tên voucher" prop="voucherName">
            <el-input v-model="form.voucherName" placeholder="Nhập tên voucher" clearable>
              <template #prefix><el-icon><PriceTag /></el-icon></template>
            </el-input>
          </el-form-item>

          <!-- Giảm giá -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Phần trăm giảm (%)" prop="discountPercentage">
                <el-input-number
                  v-model="form.discountPercentage"
                  :min="0"
                  :max="100"
                  :step="0.1"
                  :disabled="!!form.discountAmount"
                  :formatter="formatPercent"
                  :parser="parsePercent"
                  class="w-full"
                  placeholder="VD: 9.5"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Số tiền giảm (VNĐ)" prop="discountAmount">
                <el-input-number
                  v-model="form.discountAmount"
                  :min="0"
                  :disabled="!!form.discountPercentage"
                  :formatter="formatMoney"
                  :parser="parseMoney"
                  class="w-full"
                  placeholder="VD: 50000"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- Điều kiện đơn & giảm tối đa -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Giá trị đơn tối thiểu (VNĐ)" prop="minOrderValue">
                <el-input-number
                  v-model="form.minOrderValue"
                  :min="0"
                  :formatter="formatMoney"
                  :parser="parseMoney"
                  class="w-full"
                  placeholder="VD: 200000"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Giảm tối đa (VNĐ)" prop="maxDiscountValue">
                <el-input-number
                  v-model="form.maxDiscountValue"
                  :min="0"
                  :formatter="formatMoney"
                  :parser="parseMoney"
                  class="w-full"
                  placeholder="Có thể để trống"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- NEW: minOrderToReceive -->
          <el-form-item
            label="Giá trị đơn tối thiểu để NHẬN voucher (VNĐ) - có thể để trống"
            prop="minOrderToReceive"
          >
            <el-input-number
              v-model="form.minOrderToReceive"
              :min="0"
              :formatter="formatMoney"
              :parser="parseMoney"
              class="w-full"
              placeholder="Bỏ trống nếu không áp dụng"
            />
          </el-form-item>

          <!-- Ngày -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Ngày bắt đầu" prop="startDate">
                <el-date-picker
                  v-model="form.startDate"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  format="YYYY-MM-DD HH:mm:ss"
                  class="w-full"
                  placeholder="Chọn ngày bắt đầu"
                >
                  <template #prefix><el-icon><Calendar /></el-icon></template>
                </el-date-picker>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Ngày kết thúc" prop="endDate">
                <el-date-picker
                  v-model="form.endDate"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  format="YYYY-MM-DD HH:mm:ss"
                  class="w-full"
                  placeholder="Chọn ngày kết thúc"
                >
                  <template #prefix><el-icon><Calendar /></el-icon></template>
                </el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- Mô tả -->
          <el-form-item label="Mô tả" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="Nhập mô tả">
              <template #prefix><el-icon><InfoFilled /></el-icon></template>
            </el-input>
          </el-form-item>

          <!-- Loại đơn, loại voucher, khách hàng -->
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="Loại đơn hàng" prop="orderType">
                <el-select v-model="form.orderType" placeholder="Chọn loại" class="w-full">
                  <el-option :value="0" label="Bán tại quầy" />
                  <el-option :value="1" label="Bán online" />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- <el-col :span="8">
              <el-form-item label="Loại voucher" prop="voucherType">
                <el-select v-model="form.voucherType" placeholder="Chọn loại" class="w-full">
                  <el-option :value="1" label="Công khai" />
                  <el-option :value="2" label="Riêng tư" />
                </el-select>
              </el-form-item>
            </el-col> -->

            <el-col :span="8" v-if="form.voucherType === 2">
              <el-form-item label="Khách hàng áp dụng" prop="customerId">
                <el-select
                  v-model="form.customerId"
                  placeholder="Chọn khách hàng"
                  class="w-full"
                  clearable
                  filterable
                  :filter-method="filterCustomers"
                >
                  <template #prefix><el-icon><User /></el-icon></template>
                  <el-option
                    v-for="c in customers"
                    :key="c.id"
                    :label="c.customerName || `ID: ${c.id}`"
                    :value="c.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- Sản phẩm / Danh mục (chọn 1 trong 2) -->
          <!-- <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Sản phẩm áp dụng (nếu có)" prop="productId">
                <el-select
                  v-model="form.productId"
                  placeholder="Chọn sản phẩm"
                  class="w-full"
                  :disabled="!!form.categoryId"
                  clearable
                  filterable
                  :filter-method="filterProducts"
                >
                  <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.id" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="Danh mục áp dụng (nếu có)" prop="categoryId">
                <el-select
                  v-model="form.categoryId"
                  placeholder="Chọn danh mục"
                  class="w-full"
                  :disabled="!!form.productId"
                  clearable
                  filterable
                  :filter-method="filterCategories"
                >
                  <el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row> -->

          <!-- Số lượng -->
          <el-form-item label="Số lượng" prop="quantity">
            <el-input-number v-model="form.quantity" :min="0" class="w-full" />
          </el-form-item>

          <!-- Actions -->
          <div class="flex gap-4 mt-6">
            <el-button type="primary" @click="onSubmit">
              <el-icon class="mr-2"><Check /></el-icon> Cập nhật Voucher
            </el-button>
            <el-button @click="onReset">
              <el-icon class="mr-2"><Refresh /></el-icon> Đặt lại
            </el-button>
          </div>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import apiClient from '@/utils/axiosInstance'
import { Check, Refresh, ArrowLeft, Ticket, PriceTag, InfoFilled, Calendar, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const voucherId = route.params.id

const voucherForm = ref(null)
const original = ref({})
const products = ref([])
const categories = ref([])
const customers = ref([])

/** ===== Form state ===== */
const form = reactive({
  voucherName: '',
  discountPercentage: null, // BigDecimal
  discountAmount: null,     // Integer
  minOrderValue: null,      // BigDecimal
  maxDiscountValue: null,   // BigDecimal
  minOrderToReceive: null,  // BigDecimal (NEW)
  startDate: null,          // 'YYYY-MM-DD HH:mm:ss'
  endDate: null,
  description: '',
  orderType: 1,
  voucherType: 1,           // 1: công khai, 2: riêng tư
  customerId: null,
  productId: null,
  categoryId: null,
  quantity: null,
  status: 1,
})

/** ===== Format/Parser helpers ===== */
const moneyNF = new Intl.NumberFormat('vi-VN', { style: 'decimal' })
const formatMoney = (v) => (v == null ? '' : `${moneyNF.format(v)} VNĐ`)
const parseMoney  = (v) => v ? Number(String(v).replace(/[^\d]/g, '')) : 0
const formatPercent = (v) => (v == null ? '' : `${Number(v).toFixed(1).replace(/\.0$/, '')} %`)
const parsePercent  = (v) => {
  if (!v) return 0
  const num = parseFloat(String(v).replace(',', '.').replace(/[^\d.]/g, ''))
  if (Number.isNaN(num)) return 0
  return Math.min(Math.max(num, 0), 100)
}

/** ===== Validate rules ===== */
const rules = {
  voucherName: [{ required: true, message: 'Vui lòng nhập tên voucher', trigger: 'blur' }],
  discountPercentage: [{
    validator: (r, val, cb) => {
      if ((val == null || val === '') && (form.discountAmount == null || form.discountAmount === '')) {
        cb(new Error('Vui lòng nhập phần trăm giảm hoặc số tiền giảm'))
      } else if (val != null && val !== '' && (val < 0 || val > 100)) {
        cb(new Error('Phần trăm giảm phải từ 0 đến 100'))
      } else cb()
    },
    trigger: 'change',
  }],
  discountAmount: [{
    validator: (r, val, cb) => {
      if ((val == null || val === '') && (form.discountPercentage == null || form.discountPercentage === '')) {
        cb(new Error('Vui lòng nhập số tiền giảm hoặc phần trăm giảm'))
      } else if (val != null && val < 0) {
        cb(new Error('Số tiền giảm không thể âm'))
      } else cb()
    },
    trigger: 'change',
  }],
  minOrderValue: [{
    validator: (r, val, cb) => {
      if (val != null && val < 0) cb(new Error('Giá trị đơn tối thiểu không thể âm'))
      else cb()
    }, trigger: 'change',
  }],
  maxDiscountValue: [{
    validator: (r, val, cb) => {
      if (val != null && val < 0) cb(new Error('Số tiền giảm tối đa không thể âm'))
      else cb()
    }, trigger: 'change',
  }],
  minOrderToReceive: [{
    validator: (r, val, cb) => {
      if (val == null || val === '') return cb()
      if (Number(val) < 0) cb(new Error('Giá trị không thể âm'))
      else cb()
    }, trigger: 'change',
  }],
  startDate: [{ required: true, message: 'Vui lòng chọn ngày bắt đầu', trigger: 'change' }],
  endDate: [{
    validator: (r, val, cb) => {
      if (!val) return cb(new Error('Vui lòng chọn ngày kết thúc'))
      if (form.startDate && new Date(val) <= new Date(form.startDate)) {
        return cb(new Error('Ngày kết thúc phải sau ngày bắt đầu'))
      }
      cb()
    }, trigger: 'change',
  }],
  quantity: [
    { required: true, message: 'Vui lòng nhập số lượng voucher', trigger: 'change' },
    { validator: (r, val, cb) => (val != null && val < 1) ? cb(new Error('Số lượng phải ≥ 1')) : cb(), trigger: 'change' },
  ],
}

/** ===== Mutual exclusivity bằng watch (không dùng @change) ===== */
watch(() => form.discountPercentage, (val) => {
  if (val != null && val !== '') form.discountAmount = null
})
watch(() => form.discountAmount, (val) => {
  if (val != null && val !== '') form.discountPercentage = null
})
watch(() => form.productId, (val) => {
  if (val) form.categoryId = null
})
watch(() => form.categoryId, (val) => {
  if (val) form.productId = null
})

/** ===== Load detail ===== */
const fetchVoucher = async () => {
  const { data } = await apiClient.get(`/admin/vouchers/${voucherId}`)
  original.value = { ...(data || {}) }

  form.voucherName        = data.voucherName ?? ''
  form.discountPercentage = data.discountPercentage ?? null
  form.discountAmount     = data.discountAmount ?? null
  form.minOrderValue      = data.minOrderValue ?? null
  form.maxDiscountValue   = data.maxDiscountValue ?? null
  form.minOrderToReceive  = data.minOrderToReceive ?? null   // ✅ prefill

  form.startDate = data.startDate ? String(data.startDate).replace('T',' ').slice(0,19) : null
  form.endDate   = data.endDate   ? String(data.endDate).replace('T',' ').slice(0,19)   : null

  form.description = data.description ?? ''
  form.orderType   = data.orderType ?? 1
  form.voucherType = data.voucherType ?? 1
  form.customerId  = data.customerId ? Number(data.customerId) : null
  form.productId   = data.productId  ? Number(data.productId)  : null
  form.categoryId  = data.categoryId ? Number(data.categoryId) : null
  form.quantity    = data.quantity ?? null
  form.status      = data.status ?? 1
}

/** ===== Options ===== */
const fetchProducts = async () => {
  const { data } = await apiClient.get('/admin/products/hien-thi')
  products.value = data || []
}
const fetchCategories = async () => {
  const { data } = await apiClient.get('/admin/categories/hien-thi')
  categories.value = data || []
}
const fetchCustomers = async () => {
  const { data } = await apiClient.get('/admin/customers')
  customers.value = data || []
}
const filterProducts = async (q) => {
  const { data } = await apiClient.get('/admin/products/hien-thi', { params: { search: q } })
  products.value = data || []
}
const filterCategories = async (q) => {
  const { data } = await apiClient.get('/admin/categories/hien-thi', { params: { search: q } })
  categories.value = data || []
}
const filterCustomers = async (q) => {
  const { data } = await apiClient.get('/admin/customers', { params: { search: q } })
  customers.value = data || []
}

/** ===== Submit ===== */
const onSubmit = async () => {
  try {
    await voucherForm.value.validate()

    // Check số lượng
    if (!form.quantity || form.quantity === 0) {
      ElMessage.error('Số lượng voucher phải lớn hơn 0')
      return
    }

    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn cập nhật voucher này không?',
      'Xác nhận',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Hủy',
        type: 'warning',
      }
    )

    // chỉ giữ 1 loại giảm
    const discountPercentage = form.discountAmount ? null : form.discountPercentage
    const discountAmount     = form.discountPercentage ? null : form.discountAmount

    const payload = {
      voucherName: form.voucherName,
      discountPercentage: (discountPercentage == null || discountPercentage === 0) ? null : discountPercentage,
      discountAmount: (discountAmount == null || discountAmount === 0) ? null : discountAmount,
      minOrderValue: (form.minOrderValue == null || form.minOrderValue === 0) ? null : form.minOrderValue,
      maxDiscountValue: (form.maxDiscountValue == null || form.maxDiscountValue === 0) ? null : form.maxDiscountValue,
      minOrderToReceive: (form.minOrderToReceive == null || form.minOrderToReceive === 0)
        ? null : form.minOrderToReceive,
      startDate: form.startDate,
      endDate: form.endDate,
      description: form.description,
      orderType: form.orderType,
      voucherType: form.voucherType,
      customerId: form.voucherType === 2 ? form.customerId : null,
      productId: form.productId,
      categoryId: form.categoryId,
      quantity: form.quantity,
      status: form.status,
    }

    const res = await apiClient.put(`/admin/vouchers/update/${voucherId}`, payload)
    if (res.status !== 200) throw new Error(res.data?.message || 'Cập nhật thất bại')
    ElMessage.success('Cập nhật voucher thành công!')
    router.push('/voucher')
  } catch (err) {
    if (err === 'cancel') {
      ElMessage.info('Đã hủy cập nhật')
      return
    }
    console.error('Lỗi cập nhật voucher:', err)
    ElMessage.error(err?.response?.data?.message || err?.message || 'Cập nhật thất bại, vui lòng kiểm tra lại.')
  }
}


/** ===== Reset & Back ===== */
const onReset = () => {
  if (!original.value) return
  form.voucherName        = original.value.voucherName ?? ''
  form.discountPercentage = original.value.discountPercentage ?? null
  form.discountAmount     = original.value.discountAmount ?? null
  form.minOrderValue      = original.value.minOrderValue ?? null
  form.maxDiscountValue   = original.value.maxDiscountValue ?? null
  form.minOrderToReceive  = original.value.minOrderToReceive ?? null
  form.startDate = original.value.startDate ? String(original.value.startDate).replace('T',' ').slice(0,19) : null
  form.endDate   = original.value.endDate   ? String(original.value.endDate).replace('T',' ').slice(0,19)   : null
  form.description = original.value.description ?? ''
  form.orderType   = original.value.orderType ?? 1
  form.voucherType = original.value.voucherType ?? 1
  form.customerId  = original.value.customerId ? Number(original.value.customerId) : null
  form.productId   = original.value.productId  ? Number(original.value.productId)  : null
  form.categoryId  = original.value.categoryId ? Number(original.value.categoryId) : null
  form.quantity    = original.value.quantity ?? null
  form.status      = original.value.status ?? 1
}
const goBack = () => router.back()

onMounted(async () => {
  await Promise.all([fetchVoucher(), fetchProducts(), fetchCategories(), fetchCustomers()])
})
</script>

<style scoped>
.el-card { border-radius: 12px; }
:deep(.el-form-item__label){ @apply font-semibold text-gray-700; }
:deep(.el-input-number){ @apply w-full; }
:deep(.el-select){ @apply w-full; }
:deep(.el-date-picker){ @apply w-full; }
</style>

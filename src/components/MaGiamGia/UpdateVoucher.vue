<template>
  <el-container class="min-h-screen bg-gray-100">
    <el-main class="p-6">
      <el-card class="max-w-4xl mx-auto" shadow="always">
        <template #header>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-4">
              <el-button
                type="info"
                circle
                @click="goBack"
                title="Quay lại"
              >
                <i class="fas fa-arrow-left"></i>
              </el-button>
              <h2 class="text-2xl font-bold text-gray-800">
                <i class="fas fa-edit mr-2 text-orange-500"></i>
                Cập nhật Voucher
              </h2>
            </div>
          </div>
        </template>

        <el-form
          ref="voucherForm"
          :model="voucher"
          :rules="rules"
          label-position="top"
          @submit.prevent="updateVoucher"
        >
          <el-form-item label="Tên voucher" prop="voucherName">
            <el-input v-model="voucher.voucherName" placeholder="Nhập tên voucher" clearable>
              <template #prefix><i class="fas fa-tag text-gray-500"></i></template>
            </el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Phần trăm giảm (%)" prop="discountPercentage">
                <el-input-number
                  v-model="voucher.discountPercentage"
                  :min="0"
                  :max="100"
                  :disabled="voucher.discountAmount !== null && voucher.discountAmount > 0"
                  class="w-full"
                  :formatter="(value) => value ? `${value}%` : ''"
                  :parser="(value) => value.replace('%', '')"
                  placeholder="Nhập phần trăm giảm"
                  @change="clearDiscountAmount"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số tiền giảm (VNĐ)" prop="discountAmount">
                <el-input-number
                  v-model="voucher.discountAmount"
                  :min="0"
                  :disabled="voucher.discountPercentage !== null && voucher.discountPercentage > 0"
                  class="w-full"
                  :formatter="(value) => value ? `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',') : ''"
                  :parser="(value) => value.replace(/,/g, '')"
                  placeholder="Nhập số tiền giảm"
                  @change="clearDiscountPercentage"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Giá trị đơn tối thiểu" prop="minOrderValue">
                <el-input-number
                  v-model="voucher.minOrderValue"
                  :min="0"
                  class="w-full"
                  :formatter="(value) => value ? `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',') : ''"
                  :parser="(value) => value.replace(/,/g, '')"
                  placeholder="Nhập giá trị tối thiểu"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Giảm tối đa" prop="maxDiscountValue">
                <el-input-number
                  v-model="voucher.maxDiscountValue"
                  :min="0"
                  class="w-full"
                  :formatter="(value) => value ? `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',') : ''"
                  :parser="(value) => value.replace(/,/g, '')"
                  placeholder="Nhập số tiền giảm tối đa"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Ngày bắt đầu" prop="startDate">
                <el-date-picker
                  v-model="voucher.startDate"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  @change="updateStatus"
                  class="w-full"
                  :disabled-date="disablePastDates"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Ngày kết thúc" prop="endDate">
                <el-date-picker
                  v-model="voucher.endDate"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  class="w-full"
                  :disabled-date="disableEndDateBeforeStart"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Mô tả" prop="description">
            <el-input
              v-model="voucher.description"
              type="textarea"
              rows="3"
              placeholder="Nhập mô tả"
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="Loại đơn hàng" prop="orderType">
                <el-select v-model="voucher.orderType" placeholder="Chọn loại">
                  <el-option label="Tại quầy" :value="1" />
                  <el-option label="Online" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
            <!-- <el-col :span="8">
              <el-form-item label="Loại voucher" prop="voucherType">
                <el-select v-model="voucher.voucherType" @change="onVoucherTypeChange">
                  <el-option label="Công khai" :value="1" />
                  <el-option label="Riêng tư" :value="0" />
                </el-select>
              </el-form-item>
            </el-col> -->
            <el-col :span="8" v-if="voucher.voucherType === 0">
              <el-form-item label="Khách hàng áp dụng" prop="customerId">
                <el-select
                  v-model="voucher.customerId"
                  placeholder="Chọn hoặc tìm khách hàng"
                  class="w-full"
                  clearable
                  filterable
                  :filter-method="filterCustomers"
                >
                  <template #prefix>
                    <i class="fas fa-user text-gray-500"></i>
                  </template>
                  <el-option
                    v-for="customer in customers"
                    :key="customer.id"
                    :value="customer.id"
                    :label="customer.customerName || `ID: ${customer.id}`"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Chọn sản phẩm (nếu có)" prop="productId">
                <el-select
                  v-model="voucher.productId"
                  placeholder="Chọn hoặc nhập để tìm sản phẩm"
                  class="w-full"
                  :disabled="isProductIdDisabled"
                  clearable
                  filterable
                  :filter-method="filterProducts"
                >
                  <el-option
                    v-for="product in products"
                    :key="product.id"
                    :value="product.id"
                    :label="product.productName"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Chọn danh mục (nếu có)" prop="categoryId">
                <el-select
                  v-model="voucher.categoryId"
                  placeholder="Chọn hoặc nhập để tìm danh mục"
                  class="w-full"
                  :disabled="isCategoryIdDisabled"
                  clearable
                  filterable
                  :filter-method="filterCategories"
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :value="category.id"
                    :label="category.categoryName"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Số lượng" prop="quantity">
            <el-input-number
              v-model="voucher.quantity"
              :min="1"
              placeholder="Nhập số lượng voucher"
              class="w-full"
            />
          </el-form-item>

          <div class="flex gap-4 mt-6">
            <el-button type="primary" @click="updateVoucher">
              <i class="fas fa-save mr-2" /> Cập nhật Voucher
            </el-button>
            <el-button @click="resetForm">
              <i class="fas fa-undo mr-2" /> Đặt lại
            </el-button>
          </div>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import apiClient from '@/utils/axiosInstance' // Import the configured axios instance

const route = useRoute()
const router = useRouter()

const voucherId = route.params.id
const originalVoucher = ref({}) // Store original voucher data for reset

// Computed properties to disable product/category selection
const isProductIdDisabled = computed(() => !!voucher.categoryId)
const isCategoryIdDisabled = computed(() => !!voucher.productId)

const voucherForm = ref(null)
const voucher = reactive({
  voucherName: '',
  discountPercentage: null,
  discountAmount: null,
  minOrderValue: null,
  maxDiscountValue: null,
  startDate: null,
  endDate: null,
  description: '',
  orderType: null,
  voucherType: 1,
  customerId: null,
  productId: null,
  categoryId: null,
  quantity: null,
  status: 1,
})

const parseDate = (str) => {
  // "YYYY-MM-DD HH:mm:ss" -> "YYYY-MM-DDTHH:mm:ssZ" để JS parse đúng UTC
  return new Date(str.replace(' ', 'T') + 'Z')
}

const rules = {
  voucherName: [{ required: true, message: 'Vui lòng nhập tên voucher', trigger: 'blur' }],
startDate: [
  { required: true, message: 'Chọn ngày bắt đầu', trigger: 'change' },
  {
    validator: (rule, value, callback) => {
      console.log('Validator startDate called')
      console.log('value (raw from date picker):', value)
      const selected = parseDate(value)
      console.log('selected (parsed Date):', selected)

      const now = new Date()
      now.setHours(0, 0, 0, 0)
      console.log('now (today start):', now)

      if (selected <= now) {
        console.warn('Ngày bắt đầu không hợp lệ: <= ngày hiện tại')
        return callback(new Error('Ngày bắt đầu phải lớn hơn ngày hiện tại'))
      }

      if (voucher.endDate) {
        const end = parseDate(voucher.endDate)
        console.log('voucher.endDate:', voucher.endDate, 'parsed end:', end)
        if (selected >= end) {
          console.warn('Ngày bắt đầu không hợp lệ: >= ngày kết thúc')
          return callback(new Error('Ngày bắt đầu phải trước ngày kết thúc'))
        }
      }

      callback()
    },
    trigger: 'change',
  },
],
endDate: [
  { required: true, message: 'Chọn ngày kết thúc', trigger: 'change' },
  {
    validator: (rule, value, callback) => {
      const now = new Date()
      if (!value) {
        callback(new Error('Chọn ngày kết thúc'))
      } else if (new Date(value) <= now) {
        callback(new Error('Ngày kết thúc phải lớn hơn hiện tại'))
      } else if (voucher.startDate && new Date(value) <= new Date(voucher.startDate)) {
        callback(new Error('Ngày kết thúc phải sau ngày bắt đầu'))
      } else {
        callback()
      }
    },
    trigger: 'change',
  },
],
  discountPercentage: [
    {
      validator: (rule, value, callback) => {
        if (voucher.discountAmount === null && (value === null || value === '')) {
          callback(new Error('Vui lòng nhập phần trăm giảm hoặc số tiền giảm'))
        } else if (value !== null && (value < 0 || value > 100)) {
          callback(new Error('Phần trăm giảm phải từ 0 đến 100'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  discountAmount: [
    {
      validator: (rule, value, callback) => {
        if (voucher.discountPercentage === null && (value === null || value === '')) {
          callback(new Error('Vui lòng nhập số tiền giảm hoặc phần trăm giảm'))
        } else if (value !== null && value < 0) {
          callback(new Error('Số tiền giảm phải lớn hơn hoặc bằng 0'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  quantity: [
    { required: true, message: 'Vui lòng nhập số lượng voucher', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value !== null && value < 1) {
          callback(new Error('Số lượng phải lớn hơn hoặc bằng 1'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

const products = ref([])
const categories = ref([])
const customers = ref([])

const fetchVoucher = async () => {
  try {
    // Use apiClient
    const res = await apiClient.get(`/admin/vouchers/${voucherId}`)

    console.log('Dữ liệu voucher từ API:', res.data) // <-- log dữ liệu ở đây

    Object.assign(voucher, res.data)
    Object.assign(originalVoucher.value, res.data) // Store original data
    if (res.data.startDate) {
      voucher.startDate = res.data.startDate ? res.data.startDate.replace('T', ' ') : null
    }
    if (res.data.endDate) {
      voucher.endDate = res.data.endDate ? res.data.endDate.replace('T', ' ') : null
    }
    voucher.discountPercentage = res.data.discountPercentage ?? null
    voucher.discountAmount = res.data.discountAmount ?? null
    voucher.minOrderValue = res.data.minOrderValue ?? null
    voucher.maxDiscountValue = res.data.maxDiscountValue ?? null
    voucher.quantity = res.data.quantity ?? null
  } catch (err) {
    console.error('Lỗi khi tải dữ liệu voucher:', err)
    ElMessage.error('Không thể tải dữ liệu voucher!')
  }
}


const filterProducts = async (query) => {
  try {
    // Use apiClient
    const res = await apiClient.get('/admin/products/hien-thi', {
      params: { search: query },
    })
    products.value = res.data
  } catch (error) {
    console.error('Lỗi khi tìm kiếm sản phẩm:', error)
    ElMessage.error('Không thể tìm kiếm sản phẩm!')
  }
}

const filterCategories = async (query) => {
  try {
    // Use apiClient
    const res = await apiClient.get('/admin/categories/hien-thi', {
      params: { search: query },
    })
    categories.value = res.data
  } catch (error) {
    console.error('Lỗi khi tìm kiếm danh mục:', error)
    ElMessage.error('Không thể tìm kiếm danh mục!')
  }
}

const filterCustomers = async (query) => {
  try {
    // Use apiClient
    const res = await apiClient.get('/admin/customers', {
      params: { search: query },
    })
    customers.value = res.data
  } catch (error) {
    console.error('Lỗi khi tìm kiếm khách hàng:', error)
    ElMessage.error('Không thể tìm kiếm khách hàng!')
  }
}

const updateStatus = () => {
  if (!voucher.startDate) {
    voucher.status = 1
    return
  }

  console.log('Ngày bắt đầu selected:', voucher.startDate)
  const now = new Date()
  const start = new Date(voucher.startDate)
  voucher.status = start > now ? 2 : 1
}

const fetchProducts = async () => {
  try {
    // Use apiClient
    const res = await apiClient.get('/admin/products/hien-thi')
    products.value = res.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm:', error)
    ElMessage.error('Không thể tải danh sách sản phẩm!')
  }
}

const fetchCategories = async () => {
  try {
    // Use apiClient
    const res = await apiClient.get('/admin/categories/hien-thi')
    categories.value = res.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách danh mục:', error)
    ElMessage.error('Không thể tải danh sách danh mục!')
  }
}

const fetchCustomers = async () => {
  try {
    // Use apiClient
    const res = await apiClient.get('/admin/customers')
    customers.value = res.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách khách hàng:', error)
    ElMessage.error('Không thể tải danh sách khách hàng!')
  }
}

const formatDateForBackend = (date) => {
  if (!date) return null
  const d = new Date(date)
  if (isNaN(d.getTime())) {
    console.warn('Invalid date:', date)
    return null
  }
  // Ensure the time is included if needed by backend, otherwise just date part.
  // The backend seems to expect 'YYYY-MM-DD HH:mm:ss'
  return d.toISOString().replace('T', ' ').slice(0, 19)
}

const clearDiscountAmount = () => {
  if (voucher.discountPercentage !== null && voucher.discountPercentage > 0) {
    voucher.discountAmount = null
  }
}

const clearDiscountPercentage = () => {
  if (voucher.discountAmount !== null && voucher.discountAmount > 0) {
    voucher.discountPercentage = null
  }
}

const disablePastDates = (time) => {
  return time.getTime() < new Date().setHours(0, 0, 0, 0)
}

const disableEndDateBeforeStart = (time) => {
  if (!voucher.startDate) return false
  return time.getTime() <= new Date(voucher.startDate).getTime()
}

const updateVoucher = async () => {
  try {
    await voucherForm.value.validate()
    const confirmed = await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn cập nhật voucher này không?',
      'Xác nhận cập nhật',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Hủy',
        type: 'warning',
      }
    )

    if (!confirmed) {
      ElMessage.info('Hủy cập nhật voucher')
      return
    }

    const payload = {
      ...voucher,
      startDate: formatDateForBackend(voucher.startDate),
      endDate: formatDateForBackend(voucher.endDate),
      // Ensure only one of discountPercentage or discountAmount is sent
      discountPercentage: voucher.discountAmount > 0 ? null : voucher.discountPercentage,
      discountAmount: voucher.discountPercentage > 0 ? null : voucher.discountAmount,
      minOrderValue: voucher.minOrderValue === 0 || voucher.minOrderValue === '' ? null : voucher.minOrderValue,
      maxDiscountValue: voucher.maxDiscountValue === 0 || voucher.maxDiscountValue === '' ? null : voucher.maxDiscountValue,
    }

    // Set fields to null if they are 0 or empty strings, as per backend expectation
    if (payload.minOrderValue === 0 || payload.minOrderValue === '') payload.minOrderValue = null;
    if (payload.maxDiscountValue === 0 || payload.maxDiscountValue === '') payload.maxDiscountValue = null;
    if (payload.discountPercentage === 0 || payload.discountPercentage === '') payload.discountPercentage = null;
    if (payload.discountAmount === 0 || payload.discountAmount === '') payload.discountAmount = null;

    console.log('Payload sent:', payload); // Log the payload for debugging

    const res = await apiClient.put(`/admin/vouchers/update/${voucherId}`, payload);

    if (res.status !== 200) {
      throw new Error(res.data.message || 'Cập nhật voucher thất bại')
    }
    ElMessage.success('Cập nhật voucher thành công!')
    router.push('/voucher')
  } catch (err) {
    if (err === 'cancel') {
      ElMessage.info('Hủy cập nhật voucher')
    } else {
      console.error('Lỗi trong quá trình cập nhật voucher:', err)
      const errorMessage = err.response?.data?.message || 'Cập nhật thất bại, kiểm tra lại thông tin.'
      ElMessage.error(errorMessage)
    }
  }
}

const resetForm = () => {
  if (voucherForm.value) {
    voucherForm.value.resetFields()
    Object.assign(voucher, originalVoucher.value)
    if (originalVoucher.value.startDate) {
      voucher.startDate = new Date(originalVoucher.value.startDate)
    }
    if (originalVoucher.value.endDate) {
      voucher.endDate = new Date(originalVoucher.value.endDate)
    }
  }
}

const goBack = () => {
  router.back()
}

const onVoucherTypeChange = () => {
  // Assuming voucherType 0 means private (for specific customer)
  // And other values mean public (no specific customer)
  if (voucher.voucherType !== 0) {
    voucher.customerId = null
    if (voucherForm.value) {
      voucherForm.value.clearValidate('customerId')
    }
  }
}

onMounted(async () => {
  await Promise.all([
    fetchVoucher(),
    fetchProducts(),
    fetchCategories(),
    fetchCustomers(),
  ])
})
</script>

<style scoped>
.el-card {
  border-radius: 12px;
}

:deep(.el-form-item__label) {
  @apply font-semibold text-gray-700;
}
:deep(.el-input-number) {
  @apply w-full;
}
:deep(.el-select) {
  @apply w-full;
}
:deep(.el-date-picker) {
  @apply w-full;
}
</style>

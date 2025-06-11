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
                class="hover:bg-gray-600 hover:text-white transition-colors"
                title="Quay lại"
              >
                <i class="fas fa-arrow-left text-lg"></i>
              </el-button>
              <h2 class="text-2xl font-bold text-gray-800">
                <i class="fas fa-ticket-alt mr-2 text-blue-500"></i> Thêm Voucher
              </h2>
            </div>
          </div>
        </template>
        <el-form
          ref="voucherForm"
          :model="voucher"
          :rules="rules"
          label-position="top"
          @submit.prevent="submitForm"
        >
          <el-form-item label="Tên voucher" prop="voucherName">
            <el-input v-model="voucher.voucherName" placeholder="Nhập tên voucher" clearable>
              <template #prefix>
                <i class="fas fa-tag text-gray-500"></i>
              </template>
            </el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Phần trăm giảm (%)" prop="discountPercentage">
                <el-input-number
                  v-model="voucher.discountPercentage"
                  :min="0"
                  :max="100"
                  :disabled="isDiscountPercentageDisabled"
                  placeholder="Nhập phần trăm giảm"
                  class="w-full"
                >
                  <template #prefix>
                    <i class="fas fa-percentage text-gray-500"></i>
                  </template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số tiền giảm (VNĐ)" prop="discountAmount">
                <el-input-number
                  v-model="voucher.discountAmount"
                  :min="0"
                  :disabled="isDiscountAmountDisabled"
                  placeholder="Nhập số tiền giảm"
                  class="w-full"
                >
                  <template #prefix>
                    <i class="fas fa-money-bill-wave text-gray-500"></i>
                  </template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Giá trị đơn tối thiểu (VNĐ)" prop="minOrderValue">
                <el-input-number
                  v-model="voucher.minOrderValue"
                  :min="0"
                  placeholder="Nhập giá trị tối thiểu"
                  class="w-full"
                >
                  <template #prefix>
                    <i class="fas fa-shopping-cart text-gray-500"></i>
                  </template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Giảm tối đa (VNĐ)" prop="maxDiscountValue">
                <el-input-number
                  v-model="voucher.maxDiscountValue"
                  :min="0"
                  placeholder="Nhập số tiền giảm tối đa"
                  class="w-full"
                >
                  <template #prefix>
                    <i class="fas fa-hand-holding-usd text-gray-500"></i>
                  </template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Ngày bắt đầu" prop="startDate">
                <el-date-picker
                  v-model="voucher.startDate"
                  type="datetime"
                  placeholder="Chọn ngày bắt đầu"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  @change="updateStatus"
                  class="w-full"
                >
                  <template #prefix>
                    <i class="fas fa-calendar-alt text-gray-500"></i>
                  </template>
                </el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Ngày kết thúc" prop="endDate">
                <el-date-picker
                  v-model="voucher.endDate"
                  type="datetime"
                  placeholder="Chọn ngày kết thúc"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  class="w-full"
                >
                  <template #prefix>
                    <i class="fas fa-calendar-alt text-gray-500"></i>
                  </template>
                </el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Mô tả" prop="description">
            <el-input
              v-model="voucher.description"
              type="textarea"
              :rows="3"
              placeholder="Nhập mô tả voucher"
            >
              <template #prefix>
                <i class="fas fa-info-circle text-gray-500"></i>
              </template>
            </el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="Loại đơn hàng" prop="orderType">
                <el-select v-model="voucher.orderType" placeholder="Chọn loại đơn hàng" class="w-full">
                  <el-option :value="1" label="Bán tại quầy" />
                  <el-option :value="2" label="Bán online" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Loại voucher" prop="voucherType">
                <el-select
                  v-model="voucher.voucherType"
                  @change="handleVoucherTypeChange"
                  placeholder="Chọn loại voucher"
                  class="w-full"
                >
                  <el-option :value="1" label="Công khai" />
                  <el-option :value="2" label="Riêng tư" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="voucher.voucherType === 2">
              <el-form-item label="ID Khách hàng" prop="customerId">
                <el-input-number
                  v-model="voucher.customerId"
                  :min="1"
                  placeholder="Nhập ID khách hàng"
                  class="w-full"
                >
                  <template #prefix>
                    <i class="fas fa-user text-gray-500"></i>
                  </template>
                </el-input-number>
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

          <div class="flex gap-4 mt-4">
            <el-button type="primary" native-type="submit" :icon="Check">
              <i class="fas fa-save mr-2"></i> Thêm Voucher
            </el-button>
            <el-button type="info" @click="resetForm" :icon="Refresh">
              <i class="fas fa-undo mr-2"></i> Reset Form
            </el-button>
          </div>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Refresh } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const voucherForm = ref(null)
const products = ref([])
const categories = ref([])

const voucher = reactive({
  customerId: null,
  employeeId: null,
  voucherName: '',
  discountPercentage: null,
  discountAmount: null,
  minOrderValue: null,
  maxDiscountValue: null,
  startDate: null,
  endDate: null,
  status: 1,
  description: '',
  createdDate: new Date().toISOString(),
  updatedDate: new Date().toISOString(),
  createdBy: 'admin',
  updatedBy: 'admin',
  orderType: 1,
  voucherType: 1,
  productId: null,
  categoryId: null,
})

const rules = reactive({
  voucherName: [{ required: true, message: 'Vui lòng nhập tên voucher', trigger: 'blur' }],
  discountPercentage: [
    {
      validator: (rule, value, callback) => {
        if (!value && !voucher.discountAmount) {
          callback(new Error('Vui lòng nhập phần trăm giảm hoặc số tiền giảm'))
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
        if (!value && !voucher.discountPercentage) {
          callback(new Error('Vui lòng nhập số tiền giảm hoặc phần trăm giảm'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  startDate: [{ required: true, message: 'Vui lòng chọn ngày bắt đầu', trigger: 'change' }],
  endDate: [
    {
      validator: (rule, value, callback) => {
        if (value && new Date(value) <= new Date(voucher.startDate)) {
          callback(new Error('Ngày kết thúc phải sau ngày bắt đầu'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  customerId: [
    {
      validator: (rule, value, callback) => {
        if (voucher.voucherType === 2 && !value) {
          callback(new Error('Vui lòng nhập ID khách hàng cho voucher riêng tư'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
})

const isProductIdDisabled = computed(() => !!voucher.categoryId)
const isCategoryIdDisabled = computed(() => !!voucher.productId)
const isDiscountAmountDisabled = computed(() => !!voucher.discountPercentage)
const isDiscountPercentageDisabled = computed(() => !!voucher.discountAmount)

const filterProducts = (query) => {
  if (query) {
    products.value = products.value.filter((product) =>
      product.productName.toLowerCase().includes(query.toLowerCase())
    )
  } else {
    fetchProducts()
  }
}

const filterCategories = (query) => {
  if (query) {
    categories.value = categories.value.filter((category) =>
      category.categoryName.toLowerCase().includes(query.toLowerCase())
    )
  } else {
    fetchCategories()
  }
}

const resetForm = () => {
  voucherForm.value.resetFields()
  Object.assign(voucher, {
    customerId: null,
    employeeId: null,
    voucherName: '',
    discountPercentage: null,
    discountAmount: null,
    minOrderValue: null,
    maxDiscountValue: null,
    startDate: null,
    endDate: null,
    status: 1,
    description: '',
    createdDate: new Date().toISOString(),
    updatedDate: new Date().toISOString(),
    createdBy: 'admin',
    updatedBy: 'admin',
    orderType: 1,
    voucherType: 1,
    productId: null,
    categoryId: null,
  })
  ElMessage.success('Form đã được reset!')
}

const updateStatus = () => {
  if (!voucher.startDate) {
    voucher.status = 1
    return
  }
  const now = new Date()
  const start = new Date(voucher.startDate)
  voucher.status = start > now ? 2 : 1
}

const handleVoucherTypeChange = () => {
  if (voucher.voucherType === 2 && !voucher.customerId) {
    ElMessage.warning('Vui lòng nhập ID khách hàng cho voucher riêng tư!')
  }
}

const fetchProducts = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/products/hien-thi')
    products.value = response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm:', error)
    ElMessage.error('Không thể tải danh sách sản phẩm!')
  }
}

const fetchCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/categories/hien-thi')
    categories.value = response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách danh mục:', error)
    ElMessage.error('Không thể tải danh sách danh mục!')
  }
}

const submitForm = async () => {
  try {
    await voucherForm.value.validate()
    ElMessageBox.confirm('Bạn có chắc chắn muốn thêm voucher này?', 'Xác nhận', {
      confirmButtonText: 'Thêm',
      cancelButtonText: 'Hủy',
      type: 'warning',
    })
      .then(async () => {
        updateStatus()
        try {
          await axios.post('http://localhost:8080/api/vouchers/create', voucher)
          ElMessage.success('Thêm voucher thành công!')
          router.push('/voucher')
          resetForm()
        } catch (error) {
          console.error('Lỗi:', error)
          ElMessage.error('Thêm voucher thất bại!')
        }
      })
      .catch(() => {
        ElMessage.info('Hủy thêm voucher')
      })
  } catch (error) {
    ElMessage.error('Vui lòng kiểm tra lại thông tin!')
  }
}

const goBack = () => {
  router.push('/voucher')
  ElMessage.info('Đã quay lại trang trước!')
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<style scoped>
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
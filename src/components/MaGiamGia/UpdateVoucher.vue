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
          <!-- Tên voucher -->
          <el-form-item label="Tên voucher" prop="voucherName">
            <el-input v-model="voucher.voucherName" placeholder="Nhập tên voucher" clearable>
              <template #prefix><i class="fas fa-tag text-gray-500"></i></template>
            </el-input>
          </el-form-item>

          <!-- Phần trăm / số tiền giảm -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Phần trăm giảm (%)" prop="discountPercentage">
                <el-input-number
                  v-model="voucher.discountPercentage"
                  :min="0"
                  :max="100"
                  :disabled="voucher.discountAmount > 0"
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số tiền giảm (VNĐ)" prop="discountAmount">
                <el-input-number
                  v-model="voucher.discountAmount"
                  :min="0"
                  :disabled="voucher.discountPercentage > 0"
                  class="w-full"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- Đơn hàng tối thiểu & giảm tối đa -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Giá trị đơn tối thiểu" prop="minOrderValue">
                <el-input-number v-model="voucher.minOrderValue" :min="0" class="w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Giảm tối đa" prop="maxDiscountValue">
                <el-input-number v-model="voucher.maxDiscountValue" :min="0" class="w-full" />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- Ngày bắt đầu / kết thúc -->
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
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- Mô tả -->
          <el-form-item label="Mô tả" prop="description">
            <el-input
              v-model="voucher.description"
              type="textarea"
              rows="3"
              placeholder="Nhập mô tả"
            />
          </el-form-item>

          <!-- Loại đơn hàng / loại voucher -->
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="Loại đơn hàng" prop="orderType">
                <el-select v-model="voucher.orderType" placeholder="Chọn loại">
                  <el-option label="Tại quầy" :value="1" />
                  <el-option label="Online" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Loại voucher" prop="voucherType">
                <el-select v-model="voucher.voucherType" @change="onVoucherTypeChange">
                  <el-option label="Công khai" :value="1" />
                  <el-option label="Riêng tư" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="voucher.voucherType === 2">
              <el-form-item label="ID khách hàng" prop="customerId">
                <el-input-number v-model="voucher.customerId" :min="1" class="w-full" />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- Sản phẩm & danh mục -->
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

          <!-- Nút -->
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

const route = useRoute()
const router = useRouter()

const voucherId = route.params.id

const isProductIdDisabled = computed(() => !!voucher.categoryId)
const isCategoryIdDisabled = computed(() => !!voucher.productId)

const voucherForm = ref(null)
const voucher = reactive({
  voucherName: '',
  discountPercentage: 0,
  discountAmount: 0,
  minOrderValue: 0,
  maxDiscountValue: 0,
  startDate: null,
  endDate: null,
  description: '',
  orderType: 1,
  voucherType: 1,
  customerId: null,
  productId: null,
  categoryId: null
})

const rules = {
  voucherName: [{ required: true, message: 'Vui lòng nhập tên voucher', trigger: 'blur' }],
  startDate: [{ required: true, message: 'Chọn ngày bắt đầu', trigger: 'change' }],
  endDate: [{ required: true, message: 'Chọn ngày kết thúc', trigger: 'change' }],
  voucherType: [{ required: true, message: 'Chọn loại voucher', trigger: 'change' }],
  orderType: [{ required: true, message: 'Chọn loại đơn hàng', trigger: 'change' }],
}

const products = ref([])
const categories = ref([])

const fetchVoucher = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/vouchers/${voucherId}`)
    const data = await res.json()
    Object.assign(voucher, data)
  } catch (err) {
    ElMessage.error('Không thể tải dữ liệu voucher!')
  }
}

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

const updateStatus = () => {
  if (!voucher.startDate) {
    voucher.status = 1
    return
  }
  const now = new Date()
  const start = new Date(voucher.startDate)
  voucher.status = start > now ? 2 : 1
}

const fetchProducts = async () => {
  const res = await fetch('http://localhost:8080/api/admin/products/hien-thi')
  products.value = await res.json()
}

const fetchCategories = async () => {
  const res = await fetch('http://localhost:8080/api/admin/categories/hien-thi')
  categories.value = await res.json()
}

const formatDate = (date) => {
  if (!date) return null
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const updateVoucher = async () => {
  try {
    await voucherForm.value.validate()

    // Hiển thị hộp thoại xác nhận
    const confirmed = await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn cập nhật voucher này không?',
      'Xác nhận cập nhật',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }
    )

    // Nếu người dùng nhấn OK, tiếp tục gửi request
    const payload = {
      ...voucher,
      startDate: voucher.startDate ? formatDate(voucher.startDate) : null,
      endDate: voucher.endDate ? formatDate(voucher.endDate) : null
    }

    console.log('startDate:', payload.startDate)
    console.log('endDate:', payload.endDate)

    const res = await fetch(`http://localhost:8080/api/vouchers/update/${voucherId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) throw new Error()
    ElMessage.success('Cập nhật voucher thành công!')
    router.push('/voucher') // Sửa '/voucher' thành '/vouchers' cho đúng với route trước đó
  } catch (err) {
    // Nếu người dùng hủy xác nhận, không hiển thị lỗi
    if (err === 'cancel') return
    ElMessage.error('Cập nhật thất bại, kiểm tra lại thông tin.')
  }
}

const resetForm = () => {
  voucherForm.value.resetFields()
}

const goBack = () => {
  router.back()
}

const onVoucherTypeChange = () => {
  if (voucher.voucherType !== 2) {
    voucher.customerId = null
  }
}

onMounted(() => {
  fetchVoucher()
  fetchProducts()
  fetchCategories()
})
</script>

<style scoped>
.el-card {
  border-radius: 12px;
}
</style>

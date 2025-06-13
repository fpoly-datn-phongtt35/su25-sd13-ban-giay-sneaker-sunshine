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
                  :formatter="(value) => `${value}%`"
                  :parser="(value) => value.replace('%', '')"
                  placeholder="Nhập phần trăm giảm"
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
                  :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="(value) => value.replace(/,/g, '')"
                  placeholder="Nhập số tiền giảm"
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
                  :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
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
                  :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
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
                  <el-option label="Online" :value="0" /> </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Loại voucher" prop="voucherType">
                <el-select v-model="voucher.voucherType" @change="onVoucherTypeChange">
                  <el-option label="Công khai" :value="1" />
                  <el-option label="Riêng tư" :value="0" /> </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="voucher.voucherType === 0"> <el-form-item label="Khách hàng áp dụng" prop="customerId">
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
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const voucherId = route.params.id

// Computed properties to disable product/category selection if one is already chosen
const isProductIdDisabled = computed(() => !!voucher.categoryId)
const isCategoryIdDisabled = computed(() => !!voucher.productId)

const voucherForm = ref(null) // Reference to the ElForm component for validation
const voucher = reactive({
  voucherName: '',
  discountPercentage: null,
  discountAmount: null,
  minOrderValue: null,
  maxDiscountValue: null,
  startDate: null,
  endDate: null,
  description: '',
  orderType: 1, // Default to 'Tại quầy'
  voucherType: 1, // Default to 'Công khai'
  customerId: null,
  productId: null,
  categoryId: null,
  quantity: null, // New quantity field
  status: 1, // Default status
})

// Validation rules for the form fields
const rules = {
  voucherName: [{ required: true, message: 'Vui lòng nhập tên voucher', trigger: 'blur' }],
  startDate: [{ required: true, message: 'Chọn ngày bắt đầu', trigger: 'change' }],
  endDate: [{ required: true, message: 'Chọn ngày kết thúc', trigger: 'change' }],
  voucherType: [{ required: true, message: 'Chọn loại voucher', trigger: 'change' }],
  orderType: [{ required: true, message: 'Chọn loại đơn hàng', trigger: 'change' }],
  // Custom validation for customerId if voucherType is 'Riêng tư'
  customerId: [
    {
      validator: (rule, value, callback) => {
        if (voucher.voucherType === 0 && !value) { // Use 0 for "Riêng tư"
          callback(new Error('Vui lòng chọn khách hàng cho voucher riêng tư'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change'],
    },
  ],
  // Custom validation for discountPercentage and discountAmount
  discountPercentage: [
    {
      validator: (rule, value, callback) => {
        // If discountAmount is not set, then discountPercentage is required
        if (voucher.discountAmount === null && (value === null || value === '')) {
          callback(new Error('Vui lòng nhập phần trăm giảm hoặc số tiền giảm'));
        } else if (value !== null && (value < 0 || value > 100)) {
          callback(new Error('Phần trăm giảm phải từ 0 đến 100'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
  discountAmount: [
    {
      validator: (rule, value, callback) => {
        // If discountPercentage is not set, then discountAmount is required
        if (voucher.discountPercentage === null && (value === null || value === '')) {
          callback(new Error('Vui lòng nhập số tiền giảm hoặc phần trăm giảm'));
        } else if (value !== null && value < 0) {
          callback(new Error('Số tiền giảm phải lớn hơn hoặc bằng 0'));
        } else {
          callback();
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
          callback(new Error('Số lượng phải lớn hơn hoặc bằng 1'));
        } else {
          callback();
        }
      },
      trigger: 'change',
    },
  ],
}

const products = ref([])
const categories = ref([])
const customers = ref([])

// Fetches voucher data from the API based on the voucherId
const fetchVoucher = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/admin/vouchers/${voucherId}`)
    // Directly assign fetched data, ensuring nulls are handled for input-number fields
    Object.assign(voucher, res.data)

    // Convert date strings from API to Date objects for el-date-picker to display correctly
    if (res.data.startDate) {
        voucher.startDate = new Date(res.data.startDate);
    }
    if (res.data.endDate) {
        voucher.endDate = new Date(res.data.endDate);
    }

    // Ensure numeric fields default to null if they come as 0 or undefined from API
    voucher.discountPercentage = res.data.discountPercentage ?? null;
    voucher.discountAmount = res.data.discountAmount ?? null;
    voucher.minOrderValue = res.data.minOrderValue ?? null;
    voucher.maxDiscountValue = res.data.maxDiscountValue ?? null;
    voucher.quantity = res.data.quantity ?? null;
  } catch (err) {
    console.error('Lỗi khi tải dữ liệu voucher:', err)
    ElMessage.error('Không thể tải dữ liệu voucher!')
  }
}

// Client-side filtering for products (consider server-side for large datasets)
const filterProducts = (query) => {
  if (query) {
    products.value = products.value.filter((product) =>
      product.productName.toLowerCase().includes(query.toLowerCase())
    )
  } else {
    fetchProducts() // Re-fetch all products if search query is cleared
  }
}

// Client-side filtering for categories
const filterCategories = (query) => {
  if (query) {
    categories.value = categories.value.filter((category) =>
      category.categoryName.toLowerCase().includes(query.toLowerCase())
    )
  } else {
    fetchCategories()
  }
}

// Client-side filtering for customers by name or ID
const filterCustomers = (query) => {
  if (query) {
    customers.value = customers.value.filter((customer) =>
      (customer.customerName?.toLowerCase() || '').includes(query.toLowerCase()) ||
      String(customer.id).includes(query)
    )
  } else {
    fetchCustomers()
  }
}

// Updates voucher status based on start date
const updateStatus = () => {
  if (!voucher.startDate) {
    voucher.status = 1 // Default or a safe status
    return
  }
  const now = new Date()
  const start = new Date(voucher.startDate)
  // Status 1: Đang diễn ra, 2: Sắp diễn ra
  voucher.status = start > now ? 2 : 1
}

// Fetches list of products
const fetchProducts = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/products/hien-thi')
    products.value = res.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm:', error)
    ElMessage.error('Không thể tải danh sách sản phẩm!')
  }
}

// Fetches list of categories
const fetchCategories = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/categories/hien-thi')
    categories.value = res.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách danh mục:', error)
    ElMessage.error('Không thể tải danh sách danh mục!')
  }
}

// Fetches list of customers
const fetchCustomers = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/customers')
    customers.value = response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách khách hàng:', error)
    ElMessage.error('Không thể tải danh sách khách hàng!')
  }
}

// Formats a Date object into a 'YYYY-MM-DD HH:mm:ss' string
const formatDateForBackend = (date) => {
  if (!date) return null;
  const d = new Date(date);
  // Check for invalid date to prevent errors
  if (isNaN(d.getTime())) {
    console.warn("Invalid date object provided to formatDateForBackend:", date);
    return null;
  }
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// Handles the voucher update process
const updateVoucher = async () => {
  try {
    // Validate form fields
    await voucherForm.value.validate();

    const confirmed = await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn cập nhật voucher này không?',
      'Xác nhận cập nhật',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Hủy',
        type: 'warning',
      }
    );

    // If confirmation is canceled, stop the process
    if (!confirmed) {
      ElMessage.info('Hủy cập nhật voucher');
      return;
    }

    // Prepare payload, converting Date objects to string format required by backend
    const payload = {
      ...voucher,
      startDate: voucher.startDate ? formatDateForBackend(voucher.startDate) : null,
      endDate: voucher.endDate ? formatDateForBackend(voucher.endDate) : null,
      // Logic to ensure only one of discountPercentage or discountAmount is sent
      discountPercentage: voucher.discountAmount > 0 ? null : voucher.discountPercentage,
      discountAmount: voucher.discountPercentage > 0 ? null : voucher.discountAmount,
    };

    // Set fields to null if they are 0 or empty strings, as per backend expectation
    if (payload.minOrderValue === 0 || payload.minOrderValue === '') payload.minOrderValue = null;
    if (payload.maxDiscountValue === 0 || payload.maxDiscountValue === '') payload.maxDiscountValue = null;
    if (payload.discountPercentage === 0 || payload.discountPercentage === '') payload.discountPercentage = null;
    if (payload.discountAmount === 0 || payload.discountAmount === '') payload.discountAmount = null;

    console.log('Payload sent:', payload); // Log the payload for debugging

    const res = await axios.put(`http://localhost:8080/api/admin/vouchers/update/${voucherId}`, payload);

    if (res.status !== 200) {
      throw new Error('Cập nhật voucher thất bại');
    }
    ElMessage.success('Cập nhật voucher thành công!');
    router.push('/voucher'); // Navigate back to the voucher list
  } catch (err) {
    if (err === 'cancel') {
      ElMessage.info('Hủy cập nhật voucher');
    } else {
      console.error('Lỗi trong quá trình cập nhật voucher:', err);
      // Provide more specific error messages if possible from backend
      ElMessage.error('Cập nhật thất bại, kiểm tra lại thông tin hoặc thử lại sau.');
    }
  }
};

// Resets the form to its initial state
const resetForm = () => {
  if (voucherForm.value) {
    voucherForm.value.resetFields();
  }
  // Manually clear quantity if resetFields doesn't cover it fully
  voucher.quantity = null;
  // Re-fetch the original voucher data to revert changes
  fetchVoucher();
};

// Navigates back to the previous page
const goBack = () => {
  router.back();
};

// Handles change in voucherType to clear customerId if not 'Riêng tư'
const onVoucherTypeChange = () => {
  if (voucher.voucherType !== 0) { // If not "Riêng tư" (0)
    voucher.customerId = null;
    if (voucherForm.value) {
      voucherForm.value.clearValidate('customerId');
    }
  }
};

// Fetch initial data when the component is mounted
onMounted(async () => {
  await Promise.all([
    fetchVoucher(),
    fetchProducts(),
    fetchCategories(),
    fetchCustomers()
  ]);
});
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
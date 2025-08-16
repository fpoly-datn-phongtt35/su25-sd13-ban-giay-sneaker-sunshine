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
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <h2 class="text-2xl font-bold text-gray-800">
                <el-icon class="mr-2 text-blue-500"><Ticket /></el-icon> Thêm Voucher
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
                <el-icon><PriceTag /></el-icon>
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
                  :step="0.1"
                  :disabled="isDiscountPercentageDisabled"
                  :formatter="formatPercentage"
                  :parser="parsePercentage"
                  placeholder="Nhập phần trăm giảm (VD: 9.3)"
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Số tiền giảm (VNĐ)" prop="discountAmount">
                <el-input-number
                  v-model="voucher.discountAmount"
                  :min="0"
                  :disabled="isDiscountAmountDisabled"
                  :formatter="formatCurrency"
                  :parser="parseCurrency"
                  placeholder="Nhập số tiền giảm"
                  class="w-full"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Giá trị đơn tối thiểu (VNĐ)" prop="minOrderValue">
                <el-input-number
                  v-model="voucher.minOrderValue"
                  :min="0"
                  :formatter="formatCurrency"
                  :parser="parseCurrency"
                  placeholder="Nhập giá trị tối thiểu"
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Giảm tối đa (VNĐ)" prop="maxDiscountValue">
                <el-input-number
                  v-model="voucher.maxDiscountValue"
                  :min="0"
                  :formatter="formatCurrency"
                  :parser="parseCurrency"
                  placeholder="Nhập số tiền giảm tối đa"
                  class="w-full"
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
                  placeholder="Chọn ngày bắt đầu"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  @change="updateStatus"
                  class="w-full"
                >
                  <template #prefix>
                    <el-icon><Calendar /></el-icon>
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
                    <el-icon><Calendar /></el-icon>
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
                <el-icon><InfoFilled /></el-icon>
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
            <!-- <el-col :span="8">
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
            </el-col> -->
            <el-col :span="8" v-if="voucher.voucherType === 2">
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
                    <el-icon><User /></el-icon>
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

          <div class="flex gap-4 mt-4">
            <el-button type="primary" @click="submitForm" >
              <el-icon class="mr-2"><Check /></el-icon> Thêm Voucher
            </el-button>
            <el-button type="info" @click="resetForm">
              <el-icon class="mr-2"><Refresh /></el-icon> Reset Form
            </el-button>
          </div>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import apiClient from '@/utils/axiosInstance'; // Import the configured axios instance
import { ElMessage, ElMessageBox } from 'element-plus';
import { Check, Refresh, ArrowLeft, Ticket, PriceTag, InfoFilled, Calendar, User } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const voucherForm = ref(null);
const products = ref([]);
const categories = ref([]);
const customers = ref([]);

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
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  createdBy: 'admin',
  updatedBy: 'admin',
  orderType: null,
  voucherType: 1,
  productId: null,
  categoryId: null,
  quantity: null, // Added quantity field
});

const rules = reactive({
  voucherName: [{ required: true, message: 'Vui lòng nhập tên voucher', trigger: 'blur' }],
  discountPercentage: [
    {
      validator: (rule, value, callback) => {
        if (!value && !voucher.discountAmount) {
          callback(new Error('Vui lòng nhập phần trăm giảm hoặc số tiền giảm'));
        } else if (value !== null && (value < 0 || value > 100)) {
          callback(new Error('Phần trăm giảm phải từ 0 đến 100'));
        } else {
          callback();
        }
      },
      trigger: 'change',
    },
  ],
  discountAmount: [
    {
      validator: (rule, value, callback) => {
        if (!value && !voucher.discountPercentage) {
          callback(new Error('Vui lòng nhập số tiền giảm hoặc phần trăm giảm'));
        } else if (value !== null && value < 0) {
          callback(new Error('Số tiền giảm không thể âm'));
        } else {
          callback();
        }
      },
      trigger: 'change',
    },
  ],
  minOrderValue: [
    { required: true, message: 'Vui lòng nhập giá trị đơn tối thiểu', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value !== null && value < 0) {
          callback(new Error('Giá trị đơn tối thiểu không thể âm'));
        } else {
          callback();
        }
      },
      trigger: 'change',
    },
  ],
  maxDiscountValue: [
    {
      validator: (rule, value, callback) => {
        if (value !== null && value < 0) {
          callback(new Error('Số tiền giảm tối đa không thể âm'));
        } else {
          callback();
        }
      },
      trigger: 'change',
    },
  ],
startDate: [
  {
    validator: (rule, value, callback) => {
      if (!value) {
        callback(new Error('Vui lòng chọn ngày bắt đầu'));
      } else if (new Date(value) < new Date()) {
        callback(new Error('Ngày bắt đầu phải lớn hơn hoặc bằng ngày hiện tại'));
      } else {
        callback();
      }
    },
    trigger: 'change',
  },
],
endDate: [
  {
    validator: (rule, value, callback) => {
      if (!value) {
        callback(new Error('Vui lòng chọn ngày kết thúc'));
      } else if (new Date(value) < new Date()) {
        callback(new Error('Ngày kết thúc phải lớn hơn hoặc bằng ngày hiện tại'));
      } else if (voucher.startDate && new Date(value) <= new Date(voucher.startDate)) {
        callback(new Error('Ngày kết thúc phải sau ngày bắt đầu'));
      } else {
        callback();
      }
    },
    trigger: 'change',
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
});

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '';
  return new Intl.NumberFormat('vi-VN', { style: 'decimal' }).format(value) + ' VNĐ';
};

const parseCurrency = (value) => {
  if (!value) return 0;
  return parseInt(value.replace(/[^\d]/g, '')) || 0;
};

const formatPercentage = (value) => {
  if (value === null || value === undefined) return '';
  return `${value.toFixed(1).replace(/\.0$/, '')} %`;
};

const parsePercentage = (value) => {
  if (!value) return 0;
  // Replace comma with dot and parse as float
  const cleaned = value.replace(',', '.').replace(/[^\d.]/g, '');
  const parsed = parseFloat(cleaned);
  return isNaN(parsed) ? 0 : Math.min(parsed, 100);
};

const isProductIdDisabled = computed(() => !!voucher.categoryId);
const isCategoryIdDisabled = computed(() => !!voucher.productId);
const isDiscountAmountDisabled = computed(() => !!voucher.discountPercentage);
const isDiscountPercentageDisabled = computed(() => !!voucher.discountAmount);

const filterProducts = (query) => {
  if (query) {
    products.value = products.value.filter((product) =>
      product.productName.toLowerCase().includes(query.toLowerCase())
    );
  } else {
    fetchProducts();
  }
};

const filterCategories = (query) => {
  if (query) {
    categories.value = categories.value.filter((category) =>
      category.categoryName.toLowerCase().includes(query.toLowerCase())
    );
  } else {
    fetchCategories();
  }
};

const filterCustomers = (query) => {
  if (query) {
    customers.value = customers.value.filter((customer) =>
      (customer.customerName?.toLowerCase() || '').includes(query.toLowerCase()) ||
      String(customer.id).includes(query)
    );
  } else {
    fetchCustomers();
  }
};

const resetForm = () => {
  voucherForm.value.resetFields();
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
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    createdBy: 'admin',
    updatedBy: 'admin',
    orderType: 1,
    voucherType: 1,
    productId: null,
    categoryId: null,
    quantity: null, // Reset quantity
  });
};

const updateStatus = () => {
  if (!voucher.startDate) {
    voucher.status = 1;
    return;
  }
  const now = new Date();
  const start = new Date(voucher.startDate);
  voucher.status = start > now ? 2 : 1;
};

const handleVoucherTypeChange = () => {
  if (voucher.voucherType === 1) {
    voucher.customerId = null;
    voucherForm.value?.clearValidate('customerId');
  } else if (voucher.voucherType === 2 && !voucher.customerId) {
    ElMessage.warning('Vui lòng chọn khách hàng cho voucher riêng tư.');
  }
};

const fetchProducts = async () => {
  try {
    // Use apiClient instead of axios
    const response = await apiClient.get('/admin/products/hien-thi');
    products.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải danh sách sản phẩm:', error);
    ElMessage.error('Không thể tải danh sách sản phẩm!');
  }
};

const fetchCategories = async () => {
  try {
    // Use apiClient instead of axios
    const response = await apiClient.get('/admin/categories/hien-thi');
    categories.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải danh sách danh mục:', error);
    ElMessage.error('Không thể tải danh sách danh mục!');
  }
};

const fetchCustomers = async () => {
  try {
    // Use apiClient instead of axios
    const response = await apiClient.get('/admin/customers');
    customers.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải danh sách khách hàng:', error);
    ElMessage.error('Không thể tải danh sách khách hàng!');
  }
};

const submitForm = async () => {
  try {
    await voucherForm.value.validate();
    console.log('Voucher data before submission:', { ...voucher });
    ElMessageBox.confirm('Bạn có chắc chắn muốn thêm voucher này?', 'Xác nhận', {
      confirmButtonText: 'Thêm',
      cancelButtonText: 'Hủy',
      type: 'warning',
    }).then(async () => {
      updateStatus();
      try {
        // Use apiClient instead of axios
        await apiClient.post('/admin/vouchers/create', voucher);
        ElMessage.success('Thêm voucher thành công!');
        router.push('/voucher');
        resetForm();
      } catch (error) {
        console.error('Lỗi khi thêm voucher:', error);
        ElMessage.error('Thêm voucher thất bại! Vui lòng kiểm tra lại.');
      }
    }).catch(() => {
      ElMessage.info('Hủy thêm voucher.');
    });
  } catch (error) {
    console.error('Validation error:', error);
    ElMessage.error('Vui lòng kiểm tra lại thông tin!');
  }
};

const goBack = () => {
  router.push('/voucher');
  ElMessage.info('Đã quay lại trang trước.');
};

onMounted(() => {
  fetchProducts();
  fetchCategories();
  fetchCustomers();
});
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

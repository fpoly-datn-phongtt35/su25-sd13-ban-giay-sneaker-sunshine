<template>
  <el-container class="min-h-screen bg-gray-100 p-4 sm:p-6 lg:p-8 font-inter">
    <el-main class="w-full max-w-6xl mx-auto">
      <el-card class="rounded-lg shadow-xl p-6 sm:p-8">
        <template #header>
          <h1 class="text-2xl sm:text-3xl font-bold text-gray-800 text-center">Tạo đợt giảm giá mới</h1>
        </template>

        <el-form :model="form" @submit.prevent="createCampaign" label-width="150px" label-position="top">
          <el-row :gutter="20" class="mb-6">
            <el-col :span="24" :md="12">
              <el-form-item label="Tên đợt giảm giá" class="mb-4">
                <el-input v-model="form.name" placeholder="Nhập tên đợt giảm giá" class="rounded-md"></el-input>
              </el-form-item>

              <el-form-item label="Giá trị giảm (%)" class="mb-4">
                <el-input-number
                  v-model="form.discountPercentage"
                  :min="0"
                  :max="100"
                  controls-position="right"
                  placeholder="%"
                  class="w-full rounded-md"
                ></el-input-number>
              </el-form-item>

              <el-form-item label="Ghi chú" class="mb-4">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="3"
                  placeholder="Nhập ghi chú..."
                  class="rounded-md"
                ></el-input>
              </el-form-item>

              <el-form-item label="Thời gian" class="mb-4">
                <div class="flex flex-col md:flex-row gap-4 w-full">
                  <el-date-picker
                    v-model="form.startDate"
                    type="datetime"
                    placeholder="Ngày bắt đầu"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    class="flex-1 rounded-md"
                  ></el-date-picker>
                  <el-date-picker
                    v-model="form.endDate"
                    type="datetime"
                    placeholder="Ngày kết thúc"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    class="flex-1 rounded-md"
                  ></el-date-picker>
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="24" :md="12">
              <el-form-item label="Sản phẩm áp dụng" class="mb-4">
                <div class="w-full bg-gray-50 p-4 rounded-lg border border-gray-200 min-h-[200px]">
                  <h3 class="text-lg font-semibold mb-3 text-gray-700">Chọn sản phẩm</h3>
                  <div v-if="loadingProducts" class="text-gray-500 text-center py-10">Đang tải sản phẩm...</div>
                  <div v-else-if="products.length === 0" class="text-gray-500 text-center py-10">
                    Không tìm thấy sản phẩm nào.
                  </div>
                  <div v-else class="w-full">
                    <el-table
                      :data="products"
                      style="width: 100%"
                      border
                      @selection-change="handleProductSelectionChange"
                      class="rounded-lg overflow-hidden"
                    >
                      <el-table-column type="selection" width="55"></el-table-column>
                      <el-table-column type="index" label="STT" width="80" align="center"></el-table-column>
                      <el-table-column prop="productName" label="Tên sản phẩm"></el-table-column>
                      <el-table-column prop="quantity" label="Số lượng" width="100" align="center"></el-table-column>
                    </el-table>
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item class="flex justify-center mt-6">
            <el-button
              type="primary"
              @click="createCampaign"
              class="w-full sm:w-auto px-8 py-3 text-lg rounded-full shadow-lg hover:shadow-xl transition-all duration-300"
            >
              Thêm mới
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import axios from 'axios';
import {
  ElContainer,
  ElHeader, // Not directly used in template, but good to keep if header is part of overall layout
  ElMain,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElDatePicker,
  ElTable,
  ElTableColumn,
  ElButton,
  ElMessage,
  ElRow,
  ElCol,
  ElCard // Added ElCard
} from 'element-plus';

// Load Tailwind CSS
const loadTailwind = () => {
  const script = document.createElement('script');
  script.src = 'https://cdn.tailwindcss.com';
  document.head.appendChild(script);
};

// Load Inter font
const loadInterFont = () => {
  const link = document.createElement('link');
  link.href = 'https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap';
  link.rel = 'stylesheet';
  document.head.appendChild(link);
};

onMounted(() => {
  loadTailwind();
  loadInterFont();
});

const form = reactive({
  name: '',
  discountPercentage: null,
  description: '',
  startDate: '',
  endDate: '',
  products: [] // This will store the selected product IDs
});

const products = ref([]);
const loadingProducts = ref(true);
const selectedProducts = ref([]);

onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/products/hien-thi');
    products.value = res.data;
  } catch (error) {
    console.error('Lỗi tải sản phẩm:', error);
    ElMessage.error('Không thể tải danh sách sản phẩm.');
  } finally {
    loadingProducts.value = false;
  }
});

const handleProductSelectionChange = (selection) => {
  selectedProducts.value = selection;
};

const createCampaign = async () => {
  try {
    form.products = selectedProducts.value.map(p => ({
      productId: p.id
    }));

    if (!form.name || form.discountPercentage === null || !form.startDate || !form.endDate || form.products.length === 0) {
      ElMessage.warning('Vui lòng điền đầy đủ thông tin và chọn ít nhất một sản phẩm.');
      return;
    }

    // Basic date validation: ensure start date is before end date
    if (new Date(form.startDate) >= new Date(form.endDate)) {
      ElMessage.error('Ngày bắt đầu phải trước ngày kết thúc.');
      return;
    }

    await axios.post('http://localhost:8080/api/admin/campaigns', form);
    ElMessage.success('Tạo đợt giảm giá thành công!');

    // Reset form after successful submission
    form.name = '';
    form.discountPercentage = null;
    form.description = '';
    form.startDate = '';
    form.endDate = '';
    form.products = [];
    selectedProducts.value = [];
  } catch (error) {
    console.error('Lỗi tạo đợt giảm giá:', error);
    // More specific error message if available from backend
    const errorMessage = error.response?.data?.message || 'Lỗi khi tạo đợt giảm giá!';
    ElMessage.error(errorMessage);
  }
};
</script>

<style scoped>
/* Base styles for Element Plus components, overridden by Tailwind where applicable */
.font-inter {
  font-family: 'Inter', sans-serif;
}

/* Custom styles for rounded corners on Element Plus components */
/* These target internal Element Plus elements to ensure consistent rounding */
:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-input-number),
:deep(.el-date-editor),
:deep(.el-table),
:deep(.el-button) {
  border-radius: 0.5rem !important; /* rounded-lg */
}

/* Specific adjustments for date pickers to ensure flex-1 works well */
:deep(.el-date-editor.el-input) {
  width: 100%; /* Ensure it takes full width within its flex item */
}

/* Adjust table header background and text color for better aesthetics */
:deep(.el-table th.el-table__cell) {
  background-color: #f8f8f8 !important; /* Light gray background for headers */
  color: #333 !important; /* Darker text for headers */
  font-weight: 600;
}

/* Add a subtle hover effect to table rows */
:deep(.el-table__row:hover) {
  background-color: #f5f5f5 !important;
}

/* Ensure form item labels are styled correctly for 'top' position */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem; /* Space between label and input */
}

/* Style for the product selection box */
.product-selection-box {
  background-color: #f9fafb; /* Light background */
  border: 1px solid #e5e7eb; /* Light border */
  border-radius: 0.75rem; /* Rounded corners */
  padding: 1.5rem; /* Padding inside the box */
  min-height: 250px; /* Minimum height for visual consistency */
}

/* Responsive adjustments for the main container padding */
@media (max-width: 640px) {
  .el-container {
    padding: 1rem; /* Smaller padding on small screens */
  }
  .el-card {
    padding: 1.5rem; /* Smaller card padding on small screens */
  }
}
</style>

<template>
  <el-container class="page-container">
    <el-main>
      <el-card class="form-card" shadow="lg">
        <template #header>
          <div class="card-header">
            <h1>Tạo đợt giảm giá mới</h1>
          </div>
        </template>

        <el-form :model="form" @submit.prevent="createCampaign" label-position="top">
          <el-row :gutter="40" class="flex-row">
            <el-col :span="24" :md="12">
              <el-form-item label="Tên đợt giảm giá" required>
                <el-input v-model="form.name" placeholder="Ví dụ: Khuyến mãi Black Friday"></el-input>
              </el-form-item>

              <el-form-item label="Giá trị giảm (%)" required>
                <el-input-number
                  v-model="form.discountPercentage"
                  :min="0"
                  :max="100"
                  controls-position="right"
                  placeholder="%"
                  style="width: 100%;"
                ></el-input-number>
              </el-form-item>

              <el-form-item label="Thời gian diễn ra" required>
                 <el-date-picker
                    v-model="dateRange"
                    type="datetimerange"
                    range-separator="Đến"
                    start-placeholder="Ngày giờ bắt đầu"
                    end-placeholder="Ngày giờ kết thúc"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    style="width: 100%;"
                  />
              </el-form-item>
              
              <el-form-item label="Ghi chú">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="4"
                  placeholder="Nhập ghi chú hoặc mô tả cho đợt giảm giá..."
                ></el-input>
              </el-form-item>
            </el-col>

            <el-col :span="24" :md="12" class="stretch-col">
              <el-form-item label="Sản phẩm áp dụng" required class="stretch-form-item">
                <div class="product-selection-container">
                  <el-table
                    v-loading="loadingProducts"
                    :data="products"
                    @selection-change="handleProductSelectionChange"
                    border
                    style="width: 100%"
                    height="100%"
                  >
                    <el-table-column type="selection" width="55" align="center"></el-table-column>
                    <el-table-column type="index" label="STT" width="60" align="center"></el-table-column>
                    <el-table-column prop="productName" label="Tên sản phẩm" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="quantity" label="Số lượng tồn" width="120" align="center"></el-table-column>
                  </el-table>
                   <div v-if="!loadingProducts && products.length === 0" class="empty-state">
                     Không có sản phẩm để hiển thị.
                   </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
             <el-col :span="24" class="form-actions">
                <el-button @click="goBack" size="large"> Quay lại</el-button>
                <el-button type="primary" @click="createCampaign" size="large">
                  Tạo mới đợt giảm giá
                </el-button>
             </el-col>
          </el-row>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import {
  ElContainer,
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
  ElCard,
  ElLoading
} from 'element-plus';

const vLoading = ElLoading.directive;
const router = useRouter();

const form = reactive({
  name: '',
  discountPercentage: null,
  description: '',
  startDate: '',
  endDate: '',
  products: []
});

const products = ref([]);
const loadingProducts = ref(true);

const dateRange = computed({
  get() {
    return form.startDate && form.endDate ? [form.startDate, form.endDate] : [];
  },
  set(val) {
    if (val && val.length === 2) {
      [form.startDate, form.endDate] = val;
    } else {
      form.startDate = '';
      form.endDate = '';
    }
  }
});

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
  form.products = selection.map(p => ({ productId: p.id }));
};

const goBack = () => {
  router.back();
};

const createCampaign = async () => {
  if (!form.name || form.discountPercentage === null || !form.startDate || !form.endDate) {
    ElMessage.warning('Vui lòng điền đầy đủ các trường thông tin bắt buộc.');
    return;
  }
  if (form.products.length === 0) {
    ElMessage.warning('Vui lòng chọn ít nhất một sản phẩm để áp dụng giảm giá.');
    return;
  }
  if (new Date(form.startDate) >= new Date(form.endDate)) {
    ElMessage.error('Ngày bắt đầu phải trước ngày kết thúc.');
    return;
  }

  try {
    await axios.post('http://localhost:8080/api/admin/campaigns', form);
    ElMessage.success('Tạo đợt giảm giá thành công!');
    goBack();
  } catch (error) {
    console.error('Lỗi tạo đợt giảm giá:', error);
    const errorMessage = error.response?.data?.message || 'Đã xảy ra lỗi khi tạo đợt giảm giá!';
    ElMessage.error(errorMessage);
  }
};
</script>

<style scoped>
.page-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.form-card {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 8px;
}

:deep(.el-card__body) {
  padding: 30px;
}

.card-header {
  text-align: center;
}

.card-header h1 {
  margin: 0 0 10px 0;
  font-size: 26px;
  color: #303133;
  font-weight: 600;
}

/* Các thay đổi chính để căn chỉnh */
.flex-row {
  display: flex;
  flex-wrap: wrap;
}

.stretch-col {
  display: flex;
  flex-direction: column;
}

.stretch-form-item {
  display: flex;
  flex-direction: column;
  flex-grow: 1; /* Quan trọng: cho phép item này lớn lên */
  margin-bottom: 0; /* Xóa margin cuối cùng để vừa vặn */
}

:deep(.stretch-form-item .el-form-item__content) {
  flex-grow: 1;
}

.product-selection-container {
  flex-grow: 1; /* Quan trọng: cho phép container này lớn lên */
  display: flex;
  flex-direction: column;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  position: relative;
}
/* Kết thúc thay đổi */

.empty-state {
  margin: auto;
  color: #909399;
  font-size: 14px;
}

.form-actions {
  text-align: right;
  margin-top: 30px;
}

.form-actions .el-button + .el-button {
  margin-left: 12px;
}
</style>
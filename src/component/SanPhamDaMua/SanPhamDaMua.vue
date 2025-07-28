<template>
  <div class="container py-5">
    <h2 class="mb-4 fw-bold text-primary">🛒 Sản phẩm đã mua</h2>

    <!-- Loading -->
    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải...</span>
      </div>
    </div>

    <!-- Empty -->
    <div v-else-if="products.length === 0" class="alert alert-info text-center">
      😔 Chưa có sản phẩm nào được mua
    </div>

    <!-- Product Table -->
    <div v-else class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-primary">
          <tr>
            <th scope="col" class="text-center">Ảnh</th>
            <th scope="col">Tên sản phẩm</th>
            <th scope="col" class="text-center">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(product, index) in products" :key="index">
            <td class="text-center">
              <img
                :src="'data:image/jpeg;base64,' + product.image"
                alt="Product Image"
                class="rounded shadow-sm"
                style="width: 64px; height: 64px; object-fit: cover"
              />
            </td>
            <td class="fw-semibold">{{ product.productName }}</td>
            <td class="text-center">
               <p v-if="product.isRated">Đã đánh giá</p>
               <p v-else>Chưa đánh giá</p>
               <el-button v-if="product.isRated" @click="openRating(product)" type="primary" size="small">
              Đánh giá lại
                </el-button>
              <button v-else class="btn btn-outline-primary btn-sm" @click="openReviewDialog(product)">
                <i class="bi bi-star-fill me-1" ></i> Đánh giá
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Dialog đánh giá -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="Đánh giá sản phẩm"
      width="600px"
    >
      <div class="mb-4">
        <div class="fw-bold mb-2">
          {{ selectedProduct.productName }} (ID: {{ selectedProduct.productId }})
        </div>
        <el-rate
          v-model="selectedProduct.rating"
          :max="5"
          show-score
          score-template="{value} sao"
        />
        <el-input
          v-model="selectedProduct.comment"
          type="textarea"
          :rows="4"
          placeholder="Nhập nhận xét của bạn"
          class="mt-3"
        />
      </div>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="submitReview">Gửi đánh giá</el-button>
      </template>
    </el-dialog>

  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import apiClient from '@/utils/axiosInstance'

const products = ref([])
const loading = ref(false)

const reviewDialogVisible = ref(false)
const selectedProduct = ref({
  productId: null,
  productName: '',
  rating: 0,
  comment: '',
})

// Gọi API và xử lý ảnh base64
const fetchPurchasedProducts = async () => {
  try {
    loading.value = true
    const res = await apiClient.get('/favorites/san-pham-da-mua')
    products.value = res.data;
  } catch (err) {
    console.error(err)
    ElMessage.error('Lỗi khi tải sản phẩm đã mua')
  } finally {
    loading.value = false
  }
}

const openReviewDialog = (product) => {
  selectedProduct.value = {
    productId: product.productId,
    productName: product.productName,
    rating: 0,
    comment: '',
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  const product = selectedProduct.value;

  if (!product.rating || !product.comment) {
    ElMessage.warning('Vui lòng nhập đủ đánh giá và bình luận.');
    return;
  }

  const dataToSubmit = {
    productId: product.productId,
    rate: product.rating,
    comment: product.comment
  };

  try {
    await apiClient.post('/favorites', dataToSubmit);
    ElMessage.success('Đã gửi đánh giá thành công!');
    reviewDialogVisible.value = false;
  } catch (error) {
    console.error('Lỗi khi gửi đánh giá:', error);
    ElMessage.error('Gửi đánh giá thất bại.');
  }
};


onMounted(() => {
  fetchPurchasedProducts()
})
</script>

<style scoped>
</style>

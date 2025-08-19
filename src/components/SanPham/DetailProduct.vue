<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>Chi tiết sản phẩm: {{ product?.productName || 'Đang tải...' }}</span>
        <el-button type="primary" @click="goBack" size="small" plain>Quay lại</el-button>
      </div>
    </template>

    <el-empty v-if="loading" description="Đang tải dữ liệu..." />
    <el-empty v-else-if="error" description="Lỗi khi tải dữ liệu" />

    <template v-else-if="product">
      <el-descriptions title="Thông tin chung" :column="3" border>
        <el-descriptions-item label="Mã sản phẩm">{{ product.productCode || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Tên sản phẩm">{{ product.productName || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Thương hiệu">{{ product.brandName || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Chất liệu">{{ product.materialName || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Cổ giày">{{ product.styleName || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Giới tính">{{ product.genderName || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Giá bán">{{ formatCurrency(product.sellPrice) }}</el-descriptions-item>
        <el-descriptions-item label="Số lượng tồn kho">{{ product.quantity ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="Trạng thái">{{ product.status === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}</el-descriptions-item>
        <el-descriptions-item label="Mô tả">{{ product.description || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Nhà cung cấp">{{ product.supplierName || 'Không có' }}</el-descriptions-item>
        <el-descriptions-item label="Ngày tạo">{{ formatDate(product.createdDate) }}</el-descriptions-item>
        <el-descriptions-item label="Ngày cập nhật">{{ formatDate(product.updatedDate) }}</el-descriptions-item>
        <el-descriptions-item label="QR Code">
          <el-button type="success" size="small" @click="openQrDialog()">Xem QR</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="Đánh giá">
          <el-button type="info" size="small" @click="openReviewsDialog(product.id)">Xem đánh giá</el-button>
        </el-descriptions-item>
      </el-descriptions>

      <el-table
        v-if="product.productDetails && product.productDetails.length"
        :data="product.productDetails"
        style="margin-top: 30px"
        border
        stripe
      >
        <el-table-column prop="sizeName" label="Size" width="200" align="center"/>
        <el-table-column prop="colorName" label="Màu sắc" width="200" align="center"/>
        <el-table-column label="Giá bán" width="200" align="center" >
          <template #default="{ row }">{{ formatCurrency(row.sellPrice) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="Số lượng" width="200" align="center" />
        <el-table-column label="Trạng thái" width="400" align="center">
          <template #default="{ row }">{{ row.status === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}</template>
        </el-table-column>
      </el-table>

      <div v-if="product.productImages && product.productImages.length" style="margin-top: 30px">
        <h3>Ảnh sản phẩm</h3>
        <el-carousel height="200px" trigger="click" arrow="always">
          <el-carousel-item v-for="img in product.productImages" :key="img.id">
            <img
              :src="`data:image/png;base64,${img.image}`"
              alt="product image"
              style="max-height: 180px; margin: auto; display: block"
            />
          </el-carousel-item>
        </el-carousel>
      </div>
    </template>

    <el-dialog v-model="qrDialogVisible" title="QR Code" width="300px">
      <div style="text-align: center;">
        <img
          :src="qrImageUrl"
          alt="QR Code"
          style="width: 200px; height: 200px; margin-bottom: 10px;"
        />
        <br />
        <el-button type="primary" plain @click="downloadQrCode">Tải về QR</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="reviewsDialogVisible" title="Đánh giá sản phẩm" width="600px">
      <el-empty v-if="productReviews.length === 0" description="Chưa có đánh giá nào." />
      <el-table v-else :data="productReviews" border style="width: 100%">
        <el-table-column label="Người đánh giá" prop="reviewerName" width="180">
          <template #default="{ row }">
            {{ row.customerName || 'Khách hàng ẩn danh' }}
          </template>
        </el-table-column>
        <el-table-column label="Số sao" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.rate" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="Nội dung đánh giá" prop="comment" />
        <el-table-column label="Ngày đánh giá" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElNotification } from 'element-plus' // Import ElNotification
import apiClient from '@/utils/axiosInstance'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const loading = ref(false)
const error = ref(false)

const qrDialogVisible = ref(false)
const qrImageUrl = ref('')
const currentProductCode = ref('')

const reviewsDialogVisible = ref(false) // Biến điều khiển dialog đánh giá
const productReviews = ref([]) // Dữ liệu đánh giá sản phẩm

function goBack() {
  router.push('/product')
}

function formatCurrency(value) {
  if (value == null || isNaN(value)) return '0 ₫'
  return value.toLocaleString('vi-VN', {
    style: 'currency',
    currency: 'VND',
  })
}

function formatDate(dateString) {
  if (!dateString) return 'Không có'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

function openQrDialog() {
  currentProductCode.value = product.value?.productCode || ''
  qrImageUrl.value = `http://localhost:8080/api/admin/products/qrcode/${currentProductCode.value}`
  qrDialogVisible.value = true
}

async function downloadQrCode() {
  try {
    const response = await fetch(qrImageUrl.value)
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `qrcode_${currentProductCode.value}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Lỗi khi tải QR code:', error)
    ElNotification({
      title: 'Lỗi',
      message: 'Không thể tải QR code. Vui lòng thử lại.',
      type: 'error',
    })
  }
}

async function fetchProductReviews(productId) {
  try {
    const response = await apiClient.get(`/admin/products/reviews-product/${productId}`);
    productReviews.value = response.data;
    return true; // Trả về true nếu thành công
  } catch (e) {
    console.error('Lỗi khi fetch đánh giá sản phẩm:', e);
    ElNotification({
      title: 'Lỗi',
      message: 'Không thể tải đánh giá sản phẩm. Vui lòng thử lại.',
      type: 'error',
    });
    return false; // Trả về false nếu có lỗi
  }
}

async function openReviewsDialog() {
  if (product.value && product.value.id) {
    console.log('product id: ',product.value.id)
    const isSuccess = await fetchProductReviews(product.value.id);
    if (isSuccess) {
      reviewsDialogVisible.value = true;
    }
  } else {
    ElNotification({
      title: 'Thông báo',
      message: 'Không tìm thấy ID sản phẩm để xem đánh giá.',
      type: 'warning',
    });
  }
}

async function fetchProduct(id) {
  loading.value = true
  error.value = false
  try {
    const response = await apiClient.get(`/admin/products/${id}`)
    product.value = response.data
  } catch (e) {
    console.error('Lỗi khi fetch sản phẩm:', e)
    error.value = true
    ElNotification({
      title: 'Lỗi',
      message: 'Không thể tải thông tin sản phẩm. Vui lòng thử lại.',
      type: 'error',
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const id = route.params.id
  if (id) {
    fetchProduct(id)
  } else {
    error.value = true
    ElNotification({
      title: 'Lỗi',
      message: 'Không tìm thấy ID sản phẩm trong URL.',
      type: 'error',
    })
  }
})
</script>

<style scoped>
.box-card {
  max-width: 1200px;
  margin: 20px auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

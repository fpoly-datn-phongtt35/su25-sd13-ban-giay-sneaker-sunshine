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
      <!-- Thông tin chung -->
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
        <el-descriptions-item label="Đánh giá">
          <el-button type="info" size="small" @click="openReviewsDialog()">Xem đánh giá</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="Lịch sử tác động">
          <el-button type="warning" size="small" @click="openHistoryDialog()">Xem lịch sử tác động</el-button>
        </el-descriptions-item>
      </el-descriptions>

      <!-- Chi tiết sản phẩm -->
      <el-table
        v-if="product.productDetails && product.productDetails.length"
        :data="product.productDetails"
        style="margin-top: 30px"
        border
        stripe
      >
        <el-table-column prop="sizeName" label="Size" width="150" align="center"/>
        <el-table-column prop="colorName" label="Màu sắc" width="150" align="center"/>
        <el-table-column label="Giá bán" width="150" align="center">
          <template #default="{ row }">{{ formatCurrency(row.sellPrice) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="Số lượng" width="120" align="center"/>
        <el-table-column label="Trạng thái" width="150" align="center">
          <template #default="{ row }">{{ row.status === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}</template>
        </el-table-column>
        <el-table-column label="QR Code" width="120" align="center">
          <template #default="{ row }">
            <el-button size="mini" type="success" @click="openQrDialog(row.productDetailCode)">QR</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Ảnh sản phẩm -->
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

    <!-- Dialog QR Code -->
    <el-dialog v-model="qrDialogVisible" title="QR Code" width="300px">
      <div style="text-align: center;">
        <img :src="qrImageUrl" alt="QR Code" style="width: 200px; height: 200px; margin-bottom: 10px;" />
        <br />
        <el-button type="primary" plain @click="downloadQrCode">Tải về QR</el-button>
      </div>
    </el-dialog>

    <!-- Dialog Đánh giá -->
    <el-dialog v-model="reviewsDialogVisible" title="Đánh giá sản phẩm" width="600px">
      <el-empty v-if="productReviews.length === 0" description="Chưa có đánh giá nào." />
      <el-table v-else :data="productReviews" border style="width: 100%">
        <el-table-column label="Người đánh giá" prop="reviewerName" width="180">
          <template #default="{ row }">{{ row.customerName || 'Khách hàng ẩn danh' }}</template>
        </el-table-column>
        <el-table-column label="Số sao" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.rate" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="Nội dung đánh giá" prop="comment"/>
        <el-table-column label="Ngày đánh giá" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- Dialog Lịch sử -->
    <el-dialog v-model="historyDialogVisible" title="Lịch sử tác động sản phẩm" width="800px">
      <el-empty v-if="productHistory.length === 0" description="Không có lịch sử tác động." />
      <el-table v-else :data="productHistory" border style="width: 100%">
        <el-table-column label="Người tạo" prop="createdBy" width="180"/>
        <el-table-column label="Loại tác động" width="150">
          <template #default="{ row }">{{ getActionType(row.actionType) }}</template>
        </el-table-column>
        <el-table-column label="Mô tả" prop="description"/>
        <el-table-column label="Thời gian" width="200">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import apiClient from '@/utils/axiosInstance'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const loading = ref(false)
const error = ref(false)

const qrDialogVisible = ref(false)
const qrImageUrl = ref('')
const currentSpctCode = ref('')

const reviewsDialogVisible = ref(false)
const productReviews = ref([])

const historyDialogVisible = ref(false)
const productHistory = ref([])

function goBack() { router.push('/product') }
function formatCurrency(value) { return value == null || isNaN(value) ? '0 ₫' : value.toLocaleString('vi-VN', { style:'currency', currency:'VND' }) }
function formatDate(dateString) { return dateString ? new Date(dateString).toLocaleDateString('vi-VN') : 'Không có' }
function formatDateTime(dateString) { return dateString ? new Date(dateString).toLocaleString('vi-VN') : 'Không có' }
function getActionType(type) { const map = { CREATE: 'Tạo mới', UPDATE: 'Cập nhật', DELETE: 'Xóa' }; return map[type] || type }

function openQrDialog(productDetailCode) {
  qrDialogVisible.value = true
  if (!productDetailCode) return
  console.log('spct code: ',productDetailCode)
  currentSpctCode.value = productDetailCode
  qrImageUrl.value = `http://localhost:8080/api/admin/products/qrcode/${productDetailCode}`
}

async function downloadQrCode() {
  try {
    const response = await fetch(qrImageUrl.value)
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `qrcode_${currentSpctCode.value}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Lỗi khi tải QR code:', error)
    ElNotification({ title: 'Lỗi', message: 'Không thể tải QR code.', type: 'error' })
  }
}

// ---------------- Reviews -----------------
async function fetchProductReviews(productId) {
  try {
    const response = await apiClient.get(`/admin/products/reviews-product/${productId}`)
    productReviews.value = response.data
    return true
  } catch (e) {
    console.error('Lỗi fetch reviews:', e)
    ElNotification({ title: 'Lỗi', message: 'Không thể tải đánh giá sản phẩm.', type: 'error' })
    return false
  }
}

async function openReviewsDialog() {
  if (product.value && product.value.id) {
    const success = await fetchProductReviews(product.value.id)
    if (success) reviewsDialogVisible.value = true
  }
}

// ---------------- History -----------------
async function fetchProductHistory(productId) {
  try {
    const response = await apiClient.get(`/admin/product-histories/get-by-productId?productId=${productId}`)
    productHistory.value = response.data
    return true
  } catch (e) {
    console.error('Lỗi fetch history:', e)
    ElNotification({ title: 'Lỗi', message: 'Không thể tải lịch sử sản phẩm.', type: 'error' })
    return false
  }
}

async function openHistoryDialog() {
  if (product.value && product.value.id) {
    const success = await fetchProductHistory(product.value.id)
    if (success) historyDialogVisible.value = true
  }
}

// ---------------- Fetch product -----------------
async function fetchProduct(id) {
  loading.value = true
  error.value = false
  try {
    const response = await apiClient.get(`/admin/products/${id}`)
    product.value = response.data
    console.log('produc: ',product.value)
  } catch (e) {
    console.error('Lỗi fetch product:', e)
    error.value = true
    ElNotification({ title: 'Lỗi', message: 'Không thể tải thông tin sản phẩm.', type: 'error' })
  } finally { loading.value = false }
}

onMounted(() => {
  const id = route.params.id
  if (id) fetchProduct(id)
  else {
    error.value = true
    ElNotification({ title: 'Lỗi', message: 'Không tìm thấy ID sản phẩm trong URL.', type: 'error' })
  }
})
</script>

<style scoped>
.box-card { max-width: 1200px; margin: 20px auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

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
        <el-descriptions-item label="QR Code">
          <el-button type="success" size="small" @click="openQrDialog()">Xem QR</el-button>
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
        <el-table-column prop="sizeName" label="Size" width="80" />
        <el-table-column prop="colorName" label="Màu sắc" width="100" />
        <el-table-column prop="productDetailCode" label="Mã chi tiết" />
        <el-table-column label="Giá bán" width="120">
          <template #default="{ row }">{{ formatCurrency(row.sellPrice) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="Số lượng" width="100" />
        <el-table-column label="Trạng thái" width="120">
          <template #default="{ row }">{{ row.status === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}</template>
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

    <!-- Dialog hiển thị QR Code -->
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
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const loading = ref(false)
const error = ref(false)

const qrDialogVisible = ref(false)
const qrImageUrl = ref('')
const currentProductCode = ref('')

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

// Mở dialog QR, dùng mã sản phẩm
function openQrDialog() {
  currentProductCode.value = product.value?.productCode || ''
  qrImageUrl.value = `http://localhost:8080/api/admin/products/qrcode/${currentProductCode.value}`
  qrDialogVisible.value = true
}

// Tải file QR code với tên chứa mã sản phẩm
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
  }
}

async function fetchProduct(id) {
  loading.value = true
  error.value = false
  try {
    const response = await axios.get(`http://localhost:8080/api/admin/products/${id}`)
    product.value = response.data
  } catch (e) {
    console.error('Lỗi khi fetch sản phẩm:', e)
    error.value = true
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

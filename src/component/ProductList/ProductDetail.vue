<template>
  <el-row :gutter="20" class="product-detail">
    <!-- Ảnh phụ -->
    <el-col :span="3">
      <div class="thumbnail-list">
        <img
          v-for="(img, index) in product.productImages"
          :key="index"
          :src="getImage(img)"
          class="thumbnail"
          @click="setMainImage(img)"
        />
      </div>
    </el-col>

    <!-- Ảnh chính -->
    <el-col :span="9">
      <div class="main-image">
        <img :src="mainImage" alt="Ảnh chính" />
      </div>
    </el-col>

    <!-- Thông tin -->
    <el-col :span="12">
      <h2 class="product-name">{{ product.productName }}</h2>
      <p class="product-code">Mã SP: {{ product.productCode }}</p>
      <p class="product-price">{{ formatPrice(product.sellPrice) }}</p>

      <!-- Màu sắc -->
      <div v-if="uniqueColors.length">
        <span>Màu sắc:</span>
        <div>
          <el-tag
            v-for="color in uniqueColors"
            :key="color"
            :type="selectedColor === color ? 'success' : 'info'"
            @click="selectColor(color)"
            style="cursor: pointer; margin-right: 8px;"
          >
            {{ color }}
          </el-tag>
        </div>
      </div>

      <!-- Kích thước -->
      <div v-if="filteredSizes.length" style="margin-top: 10px;">
        <span>Kích thước:</span>
        <div>
          <el-button
            v-for="size in filteredSizes"
            :key="size"
            size="small"
            :type="selectedSize === size ? 'primary' : ''"
            @click="selectSize(size)"
            style="margin-right: 8px;"
          >
            {{ size }}
          </el-button>
        </div>
      </div>

      <!-- Số lượng -->
      <div style="margin-top: 12px;">
        <span>Số lượng:</span>
        <el-input-number
          v-model="quantity"
          :min="1"
          :max="100"
          size="small"
          style="margin-left: 10px;"
        />
      </div>

      <!-- Nút hành động -->
      <div class="action-buttons" style="display: flex; margin-top: 20px;">
        <el-button
          style="flex: 1; background-color: black; color: white; border: none;"
          @click="handleAddToCart"
        >
          THÊM VÀO GIỎ
        </el-button>
        <el-button
          style="flex: 1; background-color: #b81c23; color: white; border: none;"
          @click="handleBuyNow"
        >
          MUA NGAY
        </el-button>
      </div>

      <el-divider />
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { addToCart } from '@/utils/cart'

const route = useRoute()
const router = useRouter()
const product = ref({})
const mainImage = ref('/no-image.jpg')

const selectedColor = ref(null)
const selectedSize = ref(null)
const quantity = ref(1)

onMounted(async () => {
  try {
    const id = route.params.id
    const res = await axios.get(`http://localhost:8080/api/admin/products/${id}`)
    product.value = res.data

    if (product.value.productImages?.length > 0) {
      mainImage.value = getImage(product.value.productImages[0])
    }

    if (uniqueColors.value.length > 0) {
      selectedColor.value = uniqueColors.value[0]
    }
  } catch (err) {
    console.error('❌ Lỗi tải sản phẩm:', err)
  }
})

// Lấy ảnh base64
const getImage = (img) =>
  img?.image ? `data:image/jpeg;base64,${img.image}` : '/no-image.jpg'

// Cập nhật ảnh chính
const setMainImage = (img) => {
  mainImage.value = getImage(img)
}

// Format giá tiền
const formatPrice = (price) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price || 0)

// Lấy danh sách màu duy nhất
const uniqueColors = computed(() => {
  const details = product.value.productDetails || []
  return [...new Set(details.map((d) => d.colorName))]
})

const filteredSizes = computed(() => {
  const details = product.value.productDetails || []
  return details
    .filter((d) => d.colorName === selectedColor.value)
    .map((d) => d.sizeName)
})

const findProductDetail = () => {
  const details = product.value.productDetails || []
  return details.find(
    (d) =>
      d.colorName === selectedColor.value &&
      d.sizeName === selectedSize.value
  )
}

const selectColor = (color) => {
  selectedColor.value = color
  selectedSize.value = null
}

const selectSize = (size) => {
  selectedSize.value = size
}

const handleAddToCart = () => {
  if (!selectedColor.value || !selectedSize.value) {
    ElMessage.warning('Vui lòng chọn màu sắc và kích thước!')
    return false
  }

  const detail = findProductDetail()
  if (!detail) {
    ElMessage.error('Không tìm thấy sản phẩm với màu và kích thước đã chọn!')
    return false
  }

  const item = {
    productDetailId: detail.id,
    productId: product.value.id,
    productName: product.value.productName,
    productCode: product.value.productCode,
    image: mainImage.value,
    color: selectedColor.value,
    size: selectedSize.value,
    price: detail.sellPrice || product.value.sellPrice,
    quantity: quantity.value,
  }

  addToCart(item)
  ElMessage.success('Đã thêm vào giỏ hàng!')
  quantity.value = 1
  return true
}

const handleBuyNow = () => {
  const added = handleAddToCart()
  if (added) {
    router.push('/cart')
  }
}

</script>

<style scoped>
.thumbnail {
  width: 100%;
  cursor: pointer;
  margin-bottom: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.main-image img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}
.product-name {
  font-size: 24px;
  font-weight: bold;
}
.product-price {
  font-size: 20px;
  color: #c00;
  margin-bottom: 10px;
}
</style>

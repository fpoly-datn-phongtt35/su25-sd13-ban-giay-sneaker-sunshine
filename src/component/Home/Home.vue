<template>
  <div>
    <el-carousel :interval="4000" height="500px" class="banner-carousel">
      <el-carousel-item v-for="item in carouselImages" :key="item.id">
        <img :src="item.src" class="carousel-image" alt="Banner Image" />
      </el-carousel-item>
    </el-carousel>

    <div class="product-collection-container">
      <div class="collection-header">
        <h2 class="collection-title">Sản phẩm mới</h2>
      </div>
      <el-row :gutter="30">
        <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
          <div class="product-card">
            <div class="product-card__image-wrapper">
              <span v-if="product.discountPercentage > 0" class="product-card__discount-badge">
                -{{ product.discountPercentage }}%
              </span>

              <img
                :src="product.activeImage"
                class="product-card__image"
                alt="Ảnh sản phẩm"
                @click="goToDetail(product.id)"
              />

              <el-button
                :icon="ShoppingCart"
                circle
                size="large"
                class="product-card__quick-view-btn"
                @click.stop="openQuickViewModal(product)"
              ></el-button>
            </div>

            <div class="product-card__info">
              <p class="product-card__name" @click="goToDetail(product.id)">
                {{ product.productName }}
              </p>

              <div class="product-card__price-container">
                <template v-if="product.discountPercentage > 0 && product.discountedPrice > 0">
                  <span class="discounted-price">{{ formatPrice(product.discountedPrice) }}</span>
                  <del class="original-price">{{ formatPrice(product.sellPrice) }}</del>
                </template>
                <template v-else>
                  <span class="normal-price">{{ formatPrice(product.sellPrice) }}</span>
                </template>
              </div>

              <div
                class="product-card__colors"
                v-if="product.variants && product.variants.length > 0"
              >
                <span
                  v-for="variant in product.variants"
                  :key="variant.colorId"
                  class="product-card__color-dot"
                  :style="{ backgroundColor: variant.colorCode }"
                  @click.stop="changeProductImage(product, variant.image)"
                ></span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-dialog
      v-model="quickViewVisible"
      width="850px"
      class="quick-view-dialog"
      @close="resetQuickView"
      :show-close="true"
    >
      <div v-if="selectedProduct" class="quick-view">
        <el-row :gutter="40">
          <el-col :span="12">
            <img :src="quickViewActiveImage" class="quick-view__main-image" />
          </el-col>
          <el-col :span="12" class="quick-view__info">
            <h3 class="quick-view__title">{{ selectedProduct.productName }}</h3>
            <div class="quick-view__color-display" v-if="quickViewSelectedColor">
              Màu: <strong>{{ quickViewSelectedColor.colorName }}</strong>
            </div>
            <div class="quick-view__color-selector">
              <div
                v-for="variant in selectedProduct.variants"
                :key="variant.colorId"
                class="color-thumbnail"
                :class="{ 'is-selected': quickViewSelectedColorId === variant.colorId }"
                @click="selectQuickViewColor(variant)"
              >
                <img :src="variant.image" :alt="variant.colorName" />
              </div>
            </div>
            <div class="quick-view__size-selector">
              <p class="selector-label">Kích thước:</p>
              <el-button
                v-for="size in availableSizesForQuickView"
                :key="size"
                :class="{ 'is-selected': quickViewSelectedSize === size }"
                @click="selectSize(size)"
                class="size-button"
              >
                {{ size }}
              </el-button>
            </div>
            <div class="quick-view__quantity-selector">
              <p class="selector-label">Số lượng:</p>
              <el-input-number v-model="quickViewQuantity" :min="1" :max="selectedVariantStock" />
              <span v-if="quickViewSelectedSize" class="stock-info">
                (Còn lại: {{ selectedVariantStock }})</span
              >
            </div>
            <div class="quick-view__actions">
              <el-button class="add-to-cart-btn" @click="handleAddToCartInModal"
                >Thêm vào giỏ</el-button
              >
              <el-button class="buy-now-btn" @click="handleBuyNowInModal">Mua ngay</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { addToCart } from '@/utils/cart.js'

// --- KHỞI TẠO & DỮ LIỆU TĨNH ---
const router = useRouter()
const products = ref([])
import banner1 from '@/img/1.jpg'
import banner2 from '@/img/2.jpg'
const carouselImages = ref([
  { id: 1, src: banner1 },
  { id: 2, src: banner2 },
])

// --- STATE CHO QUICK VIEW MODAL ---
const quickViewVisible = ref(false)
const selectedProduct = ref(null)
const quickViewSelectedColorId = ref(null)
const quickViewSelectedColor = ref(null)
const quickViewSelectedSize = ref(null)
const quickViewQuantity = ref(1)
const quickViewActiveImage = ref('')

// --- LẤY DỮ LIỆU & XỬ LÝ ---
onMounted(() => {
  fetchFeaturedProducts()
})

async function fetchFeaturedProducts() {
  try {
    const response = await axios.get('http://localhost:8080/api/online-sale/online-home')
    const uniqueProducts = [...new Map(response.data.map((item) => [item.id, item])).values()]
    products.value = uniqueProducts.map(processProductData)
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm:', error)
    ElMessage.error('Không thể tải danh sách sản phẩm.')
  }
}

function processProductData(product) {
  const uniqueColors = [
    ...new Map(product.productDetails.map((item) => [item.colorId, item])).values(),
  ]
  const variants = uniqueColors
    .map((colorInfo) => {
      const imageInfo = product.productImages.find((img) => img.colorId === colorInfo.colorId)
      return {
        colorId: colorInfo.colorId,
        colorName: colorInfo.colorName,
        colorCode: getColorCode(colorInfo.colorName),
        image: imageInfo ? `data:image/jpeg;base64,${imageInfo.image}` : null,
      }
    })
    .filter((v) => v.image)
  return {
    ...product,
    variants,
    activeImage: variants.length > 0 ? variants[0].image : 'https://via.placeholder.com/400',
  }
}

// --- LOGIC CHO QUICK VIEW MODAL ---
function openQuickViewModal(product) {
  selectedProduct.value = product
  if (product.variants.length > 0) {
    selectQuickViewColor(product.variants[0]) // Dùng hàm select để khởi tạo
  }
  quickViewVisible.value = true
}

const availableSizesForQuickView = computed(() => {
  if (!selectedProduct.value || quickViewSelectedColorId.value === null) return []
  return [
    ...new Set(
      selectedProduct.value.productDetails
        .filter((detail) => detail.colorId === quickViewSelectedColorId.value)
        .map((detail) => detail.sizeName),
    ),
  ].sort((a, b) => a - b)
})

const selectedVariantStock = computed(() => {
  if (!quickViewSelectedSize.value) return 10 // Giả định
  const detail = findSelectedProductDetail()
  return detail ? detail.quantity : 0
})

function selectQuickViewColor(variant) {
  quickViewSelectedColorId.value = variant.colorId
  quickViewSelectedColor.value = variant
  quickViewActiveImage.value = variant.image
  quickViewSelectedSize.value = null
}

function selectSize(size) {
  quickViewSelectedSize.value = size
  quickViewQuantity.value = 1 // Reset số lượng về 1 khi chọn size mới
}

function findSelectedProductDetail() {
  if (!selectedProduct.value || !quickViewSelectedColorId.value || !quickViewSelectedSize.value)
    return null
  return selectedProduct.value.productDetails.find(
    (d) =>
      d.colorId === quickViewSelectedColorId.value && d.sizeName === quickViewSelectedSize.value,
  )
}

function resetQuickView() {
  quickViewVisible.value = false
  setTimeout(() => {
    selectedProduct.value = null
    quickViewSelectedColorId.value = null
    quickViewSelectedColor.value = null
    quickViewSelectedSize.value = null
    quickViewQuantity.value = 1
    quickViewActiveImage.value = ''
  }, 300)
}

function handleAddToCartInModal() {
  if (!quickViewSelectedSize.value) {
    ElMessage.warning('Vui lòng chọn kích thước')
    return false
  }
  const detail = findSelectedProductDetail()
  if (!detail) {
    ElMessage.error('Lỗi: không tìm thấy chi tiết sản phẩm')
    return false
  }
  if (quickViewQuantity.value > detail.quantity) {
    ElMessage.warning(`Số lượng tồn kho không đủ, chỉ còn ${detail.quantity} sản phẩm.`)
    return false
  }
  const item = {
    productDetailId: detail.id,
    productId: selectedProduct.value.id,
    productName: selectedProduct.value.productName,
    image: quickViewActiveImage.value,
    color: quickViewSelectedColor.value.colorName,
    size: quickViewSelectedSize.value,
    price: detail.discountedPrice > 0 ? detail.discountedPrice : detail.sellPrice,
    quantity: quickViewQuantity.value,
  }
  addToCart(item)
  ElMessage.success('Đã thêm vào giỏ hàng!')
  resetQuickView()
  return true
}

function handleBuyNowInModal() {
  const added = handleAddToCartInModal()
  if (added) {
    router.push('/cart')
  }
}

// --- HÀM HỖ TRỢ & ĐIỀU HƯỚNG ---
function changeProductImage(product, newImage) {
  product.activeImage = newImage
}
function goToDetail(id) {
  router.push(`/product/${id}`)
}
function getColorCode(colorName) {
  const colorMap = {
    black: '#000000',
    white: '#FFFFFF',
    red: '#FF0000',
    blue: '#0000FF',
    green: '#008000',
    grey: '#808080',
    silver: '#C0C0C0',
  }
  return colorMap[colorName?.toLowerCase()] || '#CCCCCC'
}
const formatPrice = (price) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
</script>

<style scoped>
.product-card__discount-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background-color: #d9534f;
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  z-index: 2;
}

/* Container cho giá */
.product-card__price-container {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 0 0 10px 0;
  height: 24px;
}

/* Giá sau khi giảm */
.discounted-price {
  font-size: 16px;
  font-weight: 600;
  color: #d9534f;
}

/* Giá gốc (bị gạch ngang) */
.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

/* Giá bình thường (khi không giảm) */
.normal-price {
  font-size: 16px;
  font-weight: 600;
  color: #000;
}
/* --- STYLES CHUNG --- */
.product-collection-container {
  max-width: 1400px;
  margin: 60px auto;
  padding: 0 20px;
}
.collection-header {
  text-align: center;
  margin-bottom: 40px;
}
.collection-title {
  font-size: 32px;
  font-weight: 600;
}
.banner-carousel {
  max-width: 100%;
}
.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.selector-label {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 10px 0;
}
.stock-info {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

/* --- PRODUCT CARD --- */
.product-card {
  margin-bottom: 20px;
}
.product-card__image-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  background-color: #f5f5f5;
  overflow: hidden;
  cursor: pointer;
}
.product-card__image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.product-card:hover .product-card__image {
  transform: scale(1.05);
}
.product-card__quick-view-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 2;
  background-color: rgba(255, 255, 255, 0.9) !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}
.product-card__info {
  padding-top: 15px;
  text-align: left;
}
.product-card__name {
  font-size: 15px;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  cursor: pointer;
}
.product-card__price {
  font-size: 16px;
  font-weight: 600;
  color: #000;
  margin: 0 0 10px 0;
}
.product-card__colors {
  display: flex;
  gap: 6px;
  height: 14px;
}
.product-card__color-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid #e0e0e0;
  cursor: pointer;
}

/* --- QUICK VIEW MODAL --- */
:deep(.el-dialog.quick-view-dialog) {
  border-radius: 8px;
}
:deep(.quick-view-dialog .el-dialog__header) {
  padding: 0;
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 1;
}
:deep(.quick-view-dialog .el-dialog__headerbtn .el-icon) {
  font-size: 24px;
  color: #555;
}
:deep(.quick-view-dialog .el-dialog__body) {
  padding: 40px;
}
.quick-view__main-image {
  width: 100%;
  height: auto;
  aspect-ratio: 1/1;
  object-fit: cover;
  border-radius: 4px;
}
.quick-view__info {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.quick-view__title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px 0;
  line-height: 1.3;
}
.quick-view__color-display {
  font-size: 14px;
  margin-bottom: 15px;
  color: #555;
  height: 20px;
}
.quick-view__color-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.color-thumbnail {
  cursor: pointer;
  border: 2px solid #e0e0e0;
  padding: 2px;
  transition: border-color 0.2s;
  width: 60px;
  height: 60px;
}
.color-thumbnail.is-selected {
  border-color: #000;
}
.color-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.quick-view__size-selector {
  margin-bottom: 20px;
}
.size-button {
  margin: 0 8px 8px 0 !important;
  border-radius: 50px;
  border: 1px solid #ccc;
  font-weight: 600;
  min-width: 50px;
}
.size-button.is-selected {
  background-color: #000 !important;
  color: #fff !important;
  border-color: #000 !important;
}
.quick-view__quantity-selector {
  margin-bottom: 25px;
}
.quick-view__actions {
  display: flex;
  gap: 10px;
  margin-top: auto;
}
.quick-view__actions .el-button {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 5px;
  border: 1px solid #000 !important;
}
.add-to-cart-btn {
  background-color: #fff !important;
  color: #000 !important;
}
.buy-now-btn {
  background-color: #000 !important;
  color: #fff !important;
}
</style>

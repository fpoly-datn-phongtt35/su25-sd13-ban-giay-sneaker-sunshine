<template>
  <el-row :gutter="20" class="product-detail">
    <!-- Thumbnails bên trái -->
    <el-col :span="3">
      <div class="thumbnail-list">
        <img
          v-for="(img, index) in colorSpecificImages"
          :key="img.id"
          :src="getImage(img)"
          class="thumbnail"
          :class="{ active: index === currentImageIndex }"
          @click="setMainImageByIndex(index)"
        />
      </div>
    </el-col>

    <!-- Ảnh chính -->
    <el-col :span="9">
      <div class="main-image-container">
        <img :src="mainImage" alt="Ảnh chính" />
        <el-button
          v-if="colorSpecificImages.length > 1"
          class="nav-button prev"
          @click="prevImage"
          circle
        >
          &lt;
        </el-button>
        <el-button
          v-if="colorSpecificImages.length > 1"
          class="nav-button next"
          @click="nextImage"
          circle
        >
          &gt;
        </el-button>
      </div>
    </el-col>

    <!-- Thông tin sản phẩm -->
    <el-col :span="12">
      <h1 class="product-name">{{ product.productName }}</h1>
      <p class="product-code">Style Code: {{ product.productCode }}</p>

      <div class="product-price">
        <template v-if="product.discountedPrice && product.discountedPrice < product.sellPrice">
          <span class="original-price">{{ formatPrice(product.sellPrice) }}</span>
          <span class="discounted-price">{{ formatPrice(product.discountedPrice) }}</span>
        </template>
        <template v-else>
          <span class="discounted-price">{{ formatPrice(product.sellPrice) }}</span>
        </template>
      </div>

      <!-- Chọn màu -->
      <div class="color-selector" v-if="uniqueColors.length">
        <div
          v-for="color in uniqueColors"
          :key="color.id"
          class="color-swatch"
          :style="{ backgroundColor: color.name.toLowerCase() }"
          :class="{ selected: selectedColor === color.name }"
          @click="selectColor(color)"
        ></div>
      </div>

      <!-- Chọn size -->
      <div class="size-selector" v-if="filteredSizes.length">
        <div class="size-header">
          <!-- Liên kết mở dialog -->
          <a href="#" class="size-guide-link" @click.prevent="isSizeGuideVisible = true">
            Hướng dẫn kích thước
          </a>

          <!-- Dialog hiện bảng size -->
          <el-dialog
            v-model="isSizeGuideVisible"
            width="1000px"
            title="Hướng dẫn chọn kích thước"
            :close-on-click-modal="false"
          >
            <img
              src="https://file.hstatic.net/1000284478/file/mlb_new_unisex_-_chunky_-_desktop_e90f62fd3ddf4139bd3c0a3cae52ebd5.jpg"
              alt="Bảng size"
              style="width: 100%"
            />
          </el-dialog>
        </div>

        <div class="size-buttons">
          <el-button
            v-for="size in filteredSizes"
            :key="size"
            :class="{ selected: selectedSize === size }"
            @click="selectSize(size)"
          >
            {{ size }}
          </el-button>
        </div>
      </div>

      <!-- Nút hành động -->
      <div class="action-buttons">
        <el-button class="add-to-cart-btn" @click="handleAddToCart"> THÊM VÀO GIỎ </el-button>
        <el-button class="buy-now-btn" @click="handleBuyNow"> MUA NGAY </el-button>
      </div>

      <div class="promotions-section">
        <div class="promotion-item">
          <p class="promotion-title"><span class="dot">●</span> SunShine Chào bạn mới</p>
          <p class="promotion-text">
            Nhận ngay ưu đãi 10% khi đăng ký thành viên và mua đơn hàng nguyên giá đầu tiên tại
            website*
          </p>
          <p class="promotion-text">Nhập mã: MLBWELCOME</p>
          <p class="promotion-text">Ưu đãi không áp dụng cùng với các CTKM khác</p>
        </div>
        <div class="promotion-item">
          <p class="promotion-title"><span class="dot">●</span> BLACK TUESDAY REWARDS</p>
          <p class="promotion-text">
            Hoàn ngay 10% điểm Loyalty theo giá trị hóa đơn vào mỗi Thứ 3 hàng tuần
          </p>
          <p class="promotion-text">Áp dụng từ: 01/04/2025</p>
          <p class="promotion-text">
            Hạn sử dụng điểm: Cuối tháng kế tiếp (VD: Điểm ngày 10/3 thì điểm hết hạn 30/4).
          </p>
          <p class="promotion-text">
            Điểm Loyalty được cộng thêm bên cạnh quyền lợi tích điểm thành viên thông thường
          </p>
          <p class="promotion-note">*Chỉ áp dụng vào mỗi Thứ 3 hàng tuần</p>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { addToCart } from '@/utils/cart'

// --- Router + Route ---
const route = useRoute()
const router = useRouter()
const isSizeGuideVisible = ref(false)

// --- State chính ---
const product = ref({
  productName: 'Đang tải sản phẩm...',
  productCode: '...',
  sellPrice: 0,
  productDetails: [],
})

const colorSpecificImages = ref([])
const currentImageIndex = ref(0)
const selectedColor = ref(null)
const selectedSize = ref(null)
const quantity = ref(1)
const discountPercentage = ref(0)
const isLoadingImages = ref(false)

// --- Computed ---
const mainImage = computed(() => {
  if (isLoadingImages.value) return '/loading-placeholder.gif'
  if (!colorSpecificImages.value.length) return '/no-image.jpg'
  return getImage(colorSpecificImages.value[currentImageIndex.value])
})

const finalDiscountedPrice = computed(() => {
  const price = product.value.discountedPrice
  return price && price > 0 ? price : product.value.sellPrice
})

const uniqueColors = computed(() => {
  const map = new Map()
  product.value.productDetails?.forEach((d) => {
    if (d.colorId && !map.has(d.colorId)) {
      map.set(d.colorId, { id: d.colorId, name: d.colorName })
    }
  })
  return Array.from(map.values())
})

const filteredSizes = computed(() => {
  if (!selectedColor.value) return []
  return product.value.productDetails
    ?.filter((d) => d.colorName === selectedColor.value)
    .map((d) => d.sizeName)
    .sort((a, b) => a - b)
})

// --- Methods ---
const getImage = (img) => (img?.image ? `data:image/jpeg;base64,${img.image}` : '/no-image.jpg')

const formatPrice = (price) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' })
    .format(price || 0)
    .replace(/\s/g, '')

const fetchImagesForColor = async (colorId) => {
  if (!colorId) return
  isLoadingImages.value = true
  try {
    const productId = route.params.id
    const res = await axios.get(
      `http://localhost:8080/api/admin/product-images?productId=${productId}&colorId=${colorId}`,
    )
    colorSpecificImages.value = res.data
    currentImageIndex.value = 0
  } catch (err) {
    console.error('Lỗi khi tải ảnh:', err)
    ElMessage.error('Không thể tải ảnh cho màu này.')
    colorSpecificImages.value = []
  } finally {
    isLoadingImages.value = false
  }
}

const nextImage = () => {
  if (colorSpecificImages.value.length > 1) {
    currentImageIndex.value = (currentImageIndex.value + 1) % colorSpecificImages.value.length
  }
}

const prevImage = () => {
  if (colorSpecificImages.value.length > 1) {
    currentImageIndex.value =
      (currentImageIndex.value - 1 + colorSpecificImages.value.length) %
      colorSpecificImages.value.length
  }
}

const setMainImageByIndex = (index) => {
  currentImageIndex.value = index
}

const selectColor = (color) => {
  selectedColor.value = color.name
  selectedSize.value = null
  fetchImagesForColor(color.id)
}

const selectSize = (size) => {
  selectedSize.value = size
}

const findProductDetail = () => {
  return product.value.productDetails.find(
    (d) => d.colorName === selectedColor.value && d.sizeName === selectedSize.value,
  )
}

const handleAddToCart = () => {
  if (!selectedColor.value || !selectedSize.value) {
    ElMessage.warning('Vui lòng chọn màu sắc và kích thước!')
    return false
  }

  const detail = findProductDetail()
  if (!detail) {
    ElMessage.error('Không tìm thấy sản phẩm phù hợp!')
    return false
  }

  console.log('✅ Chi tiết sản phẩm tìm được:', detail)

  if (quantity.value > detail.quantity) {
    ElMessage.warning(`Số lượng bạn chọn vượt quá tồn kho. Bạn chỉ có thể đặt tối đa ${detail.quantity} sản phẩm.`)
    return false
  }

  // Tính giá cuối cùng (giá sau giảm nếu có, ngược lại là giá gốc)
  const finalPrice =
    detail.discountedPrice && detail.discountedPrice > 0
      ? detail.discountedPrice
      : detail.sellPrice || finalDiscountedPrice.value

  // Tạo item để thêm vào giỏ hàng
  const item = {
    productDetailId: detail.id,
    productId: product.value.id,
    productName: product.value.productName,
    productCode: product.value.productCode,
    image: mainImage.value,
    color: selectedColor.value,
    size: selectedSize.value,
    price: finalPrice,
    sellPrice: detail.sellPrice || product.value.sellPrice,
    discountedPrice: detail.discountedPrice || product.value.discountedPrice,
    discountPercentage: detail.discountPercentage || discountPercentage.value || 0,
    discountCampaignId: detail.discountCampaignId || null, // ✅ Thêm campaign ID nếu có
    quantity: quantity.value,
  }

  // ✅ Log dữ liệu thêm vào giỏ hàng
  console.log('🛒 Sản phẩm thêm vào giỏ hàng:', item)

  addToCart(item)
  ElMessage.success('Đã thêm vào giỏ hàng!')
  return true
}


const handleBuyNow = () => {
  if (handleAddToCart()) {
    router.push('/cart')
  }
}

// --- onMounted ---
onMounted(async () => {
  try {
    const id = route.params.id
    const res = await axios.get(`http://localhost:8080/api/online-sale/${id}`)
    product.value = res.data
    discountPercentage.value = res.data.discountPercentage || 0

    // Tự động chọn màu đầu tiên nếu có
    if (uniqueColors.value.length > 0) {
      selectColor(uniqueColors.value[0])
    }
  } catch (err) {
    console.error('❌ Lỗi khi tải sản phẩm:', err)
  }
})
</script>

<style scoped>

/* --- General Layout and Typography --- */
.product-detail {
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  color: #333;
  line-height: 1.4;
  /* Removed min-height: calc(100vh - 40px) as it might interfere and is better managed globally */
  box-sizing: border-box;
  padding-top: 15px;
  padding-bottom: 15px;
}

/* --- Thumbnails --- */
.thumbnail-list {
  padding-right: 10px;
}

.thumbnail {
  width: 100%;
  margin-bottom: 6px;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  transition:
    border-color 0.2s ease,
    transform 0.2s ease;
  object-fit: cover;
  max-height: 100px; /* Limit thumbnail height */
}

.thumbnail:hover {
  border-color: #a0a0a0;
  transform: translateY(-1px);
}

.thumbnail.active {
  border: 2px solid #000;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.08);
}

/* --- Main Image --- */
.main-image-container {
  position: relative;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.main-image-container img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 6px;
}

.nav-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transition:
    background-color 0.3s ease,
    transform 0.3s ease;
  width: 36px;
  height: 36px;
  font-size: 18px;
  color: #666;
}

.nav-button:hover {
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  transform: translateY(-50%) scale(1.03);
}

.nav-button.prev {
  left: 10px;
}

.nav-button.next {
  right: 10px;
}

/* --- Product Information --- */
.product-name {
  font-size: 24px;
  font-weight: 700;
  margin-top: 0;
  margin-bottom: 8px;
  color: #222;
  line-height: 1.2;
}

.product-code {
  color: #777;
  font-size: 14px;
  margin-bottom: 20px;
}

.product-price {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
}

.original-price {
  text-decoration: line-through;
  color: #a0a0a0;
  margin-right: 10px;
  font-size: 20px;
}

.discounted-price {
  color: #e60000;
  font-size: 26px;
}

/* --- Color Selector --- */
.color-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.color-swatch {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.color-swatch:hover {
  border-color: #a0a0a0;
  transform: scale(1.03);
}

.color-swatch.selected {
  border: 2px solid #000;
  box-shadow:
    0 0 0 1px #fff,
    0 0 0 3px #000;
}

/* --- Size Selector --- */
.size-selector {
  margin-bottom: 20px;
}

.size-header {
  font-size: 14px;
  margin-bottom: 10px;
}

.size-guide-link {
  font-size: 14px;
}

.size-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.size-buttons .el-button {
  min-width: 50px;
  height: 36px;
  padding: 0 12px;
  font-size: 14px;
  border-radius: 4px; /* Giữ bo tròn cho nút size */
}

/* --- Action Buttons (Thêm vào giỏ & Mua ngay) --- */
.action-buttons {
  display: flex;
  gap: 0px; /* Sát nhau */
  margin: 20px 0; /* Margin dọc */
  width: 100%;
}

.action-buttons .el-button {
  flex: 1; /* Chia đều không gian */
  height: 55px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 0px; /* KHÔNG BO TRÒN */
  text-transform: uppercase;
  transition:
    background-color 0.3s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.add-to-cart-btn {
  background-color: #000; /* Màu đen */
  color: #fff;
  border: 1px solid #000;
}

.add-to-cart-btn:hover {
  background-color: #333; /* Đen nhạt hơn khi hover */
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.buy-now-btn {
  background-color: #b81c23; /* Màu đỏ */
  color: #fff;
  border: 1px solid #b81c23;
}

.buy-now-btn:hover {
  background-color: #d02a31; /* Đỏ nhạt hơn khi hover */
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(184, 28, 35, 0.3);
}

/* --- Promotions Section --- */
.promotions-section {
  background-color: #fcfcfc;
  border-radius: 6px;
  padding: 15px;
  border: 1px solid #f0f0f0;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.02);
  /* Consider overflow-y: auto; max-height: X; here if promotions are long */
}

.promotion-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #e5e5e5;
}

.promotion-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.promotion-title {
  font-size: 16px;
  margin-bottom: 8px;
  display: flex; /* Ensure dot aligns with text */
  align-items: center;
}

.promotion-title .dot {
  color: #b81c23; /* Red dot */
  font-size: 20px;
  line-height: 1;
  margin-right: 6px;
  transform: translateY(-2px);
}

.promotion-text {
  font-size: 13px;
  color: #555; /* Ensure color is defined */
  margin-bottom: 3px;
}

.promotion-note {
  font-size: 12px;
  color: #888; /* Ensure color is defined */
  font-style: italic;
  margin-top: 8px;
}

/* --- Responsive Adjustments --- */
@media (max-width: 768px) {
  .product-detail {
    padding: 10px;
  }
  .el-col {
    padding-left: 5px !important;
    padding-right: 5px !important;
  }
  /* Example: Stack main image and product info on small screens */
  .el-col:nth-child(2), /* Main Image */
  .el-col:nth-child(3) /* Product Info */ {
    flex: 0 0 100%; /* Make them full width */
    max-width: 100%;
  }
  .el-col:nth-child(1) {
    /* Hide thumbnails on very small screens */
    display: none;
  }
}

/* --- GLOBAL STYLES (MOVE TO A SEPARATE FILE OR NON-SCOPED <style> BLOCK IN APP.VUE) --- */

html,
body {
  overflow-x: hidden;
  width: 100%;
  margin: 0;
  padding: 0;
}

.product-detail {
  max-width: 100%;
  box-sizing: border-box;
}

.size-selector {
  margin-bottom: 20px;
}

.size-header {
  font-size: 14px;
  margin-bottom: 10px;
}

.size-guide-link {
  font-size: 14px;
}

.size-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.size-buttons .el-button {
  min-width: 50px;
  height: 36px;
  padding: 0 12px;
  font-size: 14px;
  border-radius: 4px; /* Giữ bo tròn cho nút size */
  background-color: #f8f8f8; /* Off-white for default */
  border: 1px solid #dcdcdc; /* Softer border */
  color: #555;
  font-weight: 500;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease,
    transform 0.1s ease,
    box-shadow 0.2s ease; /* Add box-shadow to transition */
}

.size-buttons .el-button:hover:not(.selected) {
  background-color: #eaeaea; /* Light grey on hover */
  border-color: #b0b0b0;
  transform: translateY(-1px); /* Slight lift */
}

.size-buttons .el-button.selected {
  background-color: #000; /* Màu nền đen */
  color: #fff; /* Chữ trắng */
  border-color: #000; /* Viền đen */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3); /* Stronger, more prominent shadow */
  transform: translateY(-2px) scale(1.03); /* Slight lift and scale up */
  z-index: 1; /* Đảm bảo nút nổi lên trên các nút khác nếu có overlap bóng */
}
</style>

<template>
  <div class="product-collection-container">
    <div class="collection-header">
      <h2 class="collection-title">Bộ sưu tập</h2>
    </div>

    <div v-if="isLoading" class="loading-state">Đang tải sản phẩm...</div>
    <div v-if="error" class="error-state">{{ error }}</div>

    <el-main v-if="!isLoading && !error" class="product-list-main">
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
              <el-button
                size="small"
                :type="isFavorite(product.id) ? 'primary' : 'success'"
                :icon="Star"
                @click="toggleFavorite(product.id, product.productName)"
                v-if="product.id"
              >
                {{ isFavorite(product.id) ? 'Đã yêu thích' : 'Thêm vào Yêu thích' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <div class="pagination-container">
        <el-pagination
          v-if="totalItems > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          :total="totalItems"
          layout="total, prev, pager, next, jumper"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-main>

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
            <div class="quick-view__description" v-if="selectedProduct.description">
              <p>{{ selectedProduct.description }}</p>
            </div>
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
        <div class="pre-order-text">
          Trường hợp hết hàng, bạn có thể đặt trước
          <a href="#" @click.prevent="openPreOrderDialog" style="color: black">click vào đây</a>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="preOrderDialogVisible" title="Đặt trước sản phẩm" width="500px">
      <template v-if="preOrderItem">
        <p>
          <strong>{{ preOrderItem.productName }}</strong>
        </p>

        <div class="quick-view__color-selector" style="margin-top: 20px">
          <p class="selector-label">Chọn màu:</p>
          <div
            v-for="variant in preOrderAvailableColors"
            :key="variant.colorId"
            class="color-thumbnail"
            :class="{ 'is-selected': preOrderSelectedColorId === variant.colorId }"
            @click="preOrderSelectedColorId = variant.colorId"
          >
            <img
              :src="variant.image"
              :alt="variant.colorName"
              style="object-fit: cover; width: 100%; height: 100%"
            />
          </div>
        </div>

        <div class="quick-view__size-selector" style="margin-top: 20px">
          <p class="selector-label">Chọn kích thước:</p>
          <el-button
            v-for="size in preOrderAvailableSizes"
            :key="size"
            :class="{ 'is-selected': preOrderSelectedSize === size }"
            @click="preOrderSelectedSize = size"
            class="size-button"
          >
            {{ size }}
          </el-button>
        </div>

        <div class="quick-view__quantity-selector" style="margin-top: 20px">
          <p class="selector-label">Số lượng đặt trước:</p>
          <el-input-number v-model="preOrderQuantity" :min="1" />
        </div>

      </template>

      <template #footer>
        <el-button @click="preOrderDialogVisible = false">Huỷ</el-button>
        <el-button type="primary" @click="handlePreOrderConfirm">Đặt trước</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import { addToCart } from '@/utils/cart.js' // Giả định path này đúng
import apiClient from '@/utils/axiosInstance'

const router = useRouter()

// --- STATE QUẢN LÝ TRẠNG THÁI CHUNG ---
const isLoading = ref(true)
const error = ref(null)

// --- STATE DANH SÁCH SẢN PHẨM & PHÂN TRANG ---
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const totalItems = ref(0)

// --- STATE CHO QUICK VIEW MODAL ---
const quickViewVisible = ref(false)
const selectedProduct = ref(null) // Sản phẩm đang được xem nhanh
const quickViewSelectedColorId = ref(null)
const quickViewSelectedColor = ref(null) // Đối tượng màu sắc đã chọn
const quickViewSelectedSize = ref(null)
const quickViewQuantity = ref(1)
const quickViewActiveImage = ref('') // Ảnh hiển thị lớn trong quick view

// --- STATE CHO PRE-ORDER MODAL ---
const preOrderDialogVisible = ref(false)
const preOrderItem = ref(null)
const preOrderSelectedColorId = ref(null)
const preOrderSelectedSize = ref(null)
const preOrderQuantity = ref(1) // BỔ SUNG: Số lượng đặt trước

onMounted(() => {
  fetchProducts();
   loadFavoritesFromStorage();
})

function loadFavoritesFromStorage() {
  const storedFavorites = localStorage.getItem('favorites');
  if (storedFavorites) {
    favorites.value = JSON.parse(storedFavorites);
  }
}

const customerId = localStorage.getItem('userId')
const favorites = ref([])
function isFavorite(productId) {
  return favorites.value.some((fav) => fav.customerId === customerId && fav.productId === productId)
}

const isLoggedIn = computed(() => !!customerId)


function toggleFavorite(productId, productName) {
  if (!isLoggedIn.value) {
    ElMessage.warning('Vui lòng đăng nhập để sử dụng tính năng này.')
    return
  }
  if (isFavorite(productId)) {
    favorites.value = favorites.value.filter(
      (fav) => !(fav.customerId === customerId && fav.productId === productId),
    )
    ElMessage({
      message: `Đã bỏ yêu thích sản phẩm "${productName}"`,
      type: 'warning',
    })
  } else {
    favorites.value.push({ customerId, productId })
    ElMessage({
      message: `Đã thêm sản phẩm "${productName}" vào yêu thích`,
      type: 'success',
    })
  }
  localStorage.setItem('favorites', JSON.stringify(favorites.value))
}

// Đồng bộ preOrderItem với selectedProduct khi mở dialog đặt trước
watch(preOrderDialogVisible, (newValue) => {
  if (newValue && selectedProduct.value) {
    preOrderItem.value = JSON.parse(JSON.stringify(selectedProduct.value)) // Tạo bản sao sâu
    // Reset lựa chọn màu/size mặc định cho pre-order modal
    if (preOrderItem.value.variants && preOrderItem.value.variants.length > 0) {
      preOrderSelectedColorId.value = preOrderItem.value.variants[0].colorId
      // Cố gắng giữ lại size đã chọn từ quick view nếu có, nếu không thì chọn size đầu tiên của màu đã chọn
      if (
        quickViewSelectedSize.value &&
        preOrderAvailableSizes.value.includes(quickViewSelectedSize.value)
      ) {
        preOrderSelectedSize.value = quickViewSelectedSize.value
      } else {
        preOrderSelectedSize.value =
          preOrderAvailableSizes.value.length > 0 ? preOrderAvailableSizes.value[0] : null
      }
    } else {
      preOrderSelectedColorId.value = null
      preOrderSelectedSize.value = null
    }
    preOrderQuantity.value = 1 // BỔ SUNG: Reset số lượng khi mở modal
  } else if (!newValue) {
    // Reset khi đóng modal đặt trước
    preOrderItem.value = null
    preOrderSelectedColorId.value = null
    preOrderSelectedSize.value = null
    preOrderQuantity.value = 1 // BỔ SUNG: Reset số lượng khi đóng modal
  }
})

// Reset preOrderSelectedSize khi preOrderSelectedColorId thay đổi
watch(preOrderSelectedColorId, (newColorId, oldColorId) => {
  if (
    newColorId !== oldColorId &&
    preOrderDialogVisible.value &&
    preOrderAvailableSizes.value.length > 0
  ) {
    preOrderSelectedSize.value = preOrderAvailableSizes.value[0]
  } else if (preOrderDialogVisible.value && preOrderAvailableSizes.value.length === 0) {
    preOrderSelectedSize.value = null
  }
})

// --- HÀM XỬ LÝ DỮ LIỆU ---
async function fetchProducts() {
  isLoading.value = true
  error.value = null
  try {
    const response = await axios.get('http://localhost:8080/api/online-sale', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
      },
    })

    if (response.data && Array.isArray(response.data.content) && response.data.page) {
      const responseData = response.data

      // Lọc các sản phẩm trùng lặp và xử lý dữ liệu
      const uniqueProducts = [
        ...new Map(responseData.content.map((item) => [item.id, item])).values(),
      ]
      products.value = uniqueProducts.map(processProductData)

      totalItems.value = responseData.page.totalElements
    } else {
      throw new Error('Định dạng dữ liệu API không đúng hoặc không có dữ liệu.')
    }
  } catch (err) {
    console.error('Lỗi khi tải sản phẩm:', err)
    error.value = 'Không thể tải sản phẩm. Vui lòng thử lại sau.'
  } finally {
    isLoading.value = false
  }
}

// Hàm xử lý dữ liệu thô từ API thành dạng mà component cần
function processProductData(product) {
  // Lấy danh sách màu sắc duy nhất và liên kết với hình ảnh
  const variants = product.productDetails
    .filter((detail, index, self) => index === self.findIndex((d) => d.colorId === detail.colorId))
    .map((colorInfo) => {
      const imageInfo = product.productImages.find((img) => img.colorId === colorInfo.colorId)
      return {
        colorId: colorInfo.colorId,
        colorName: colorInfo.colorName,
        colorCode: getColorCode(colorInfo.colorName),
        image: imageInfo ? `data:image/jpeg;base64,${imageInfo.image}` : null,
      }
    })
    .filter((v) => v.image) // Chỉ giữ lại các variant có ảnh

  return {
    ...product,
    variants,
    activeImage: variants.length > 0 ? variants[0].image : 'https://via.placeholder.com/400', // Ảnh mặc định
  }
}

// --- HÀM XỬ LÝ SỰ KIỆN PHÂN TRANG ---
function handlePageChange() {
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// --- HÀM XỬ LÝ QUICK VIEW MODAL ---
function openQuickViewModal(product) {
  selectedProduct.value = product
  // Mặc định chọn màu đầu tiên nếu có
  if (product.variants && product.variants.length > 0) {
    selectQuickViewColor(product.variants[0])
  } else {
    quickViewSelectedColorId.value = null
    quickViewSelectedColor.value = null
    quickViewActiveImage.value = product.activeImage // Sử dụng ảnh chính của sản phẩm
    quickViewSelectedSize.value =
      product.productDetails.length > 0 ? product.productDetails[0].sizeName : null
  }
  quickViewVisible.value = true
}

function selectQuickViewColor(variant) {
  quickViewSelectedColorId.value = variant.colorId
  quickViewSelectedColor.value = variant
  quickViewActiveImage.value = variant.image
  quickViewSelectedSize.value = null
  quickViewQuantity.value = 1
  if (availableSizesForQuickView.value.length > 0) {
    quickViewSelectedSize.value = availableSizesForQuickView.value[0]
  }
}

function selectSize(size) {
  quickViewSelectedSize.value = size
  quickViewQuantity.value = 1 // Reset số lượng về 1 khi chọn size mới
}

function resetQuickView() {
  setTimeout(() => {
    selectedProduct.value = null
    quickViewSelectedColorId.value = null
    quickViewSelectedColor.value = null
    quickViewSelectedSize.value = null
    quickViewQuantity.value = 1
    quickViewActiveImage.value = ''
  }, 300)
}

function findSelectedProductDetail() {
  if (!selectedProduct.value) return null
  if (
    selectedProduct.value.productDetails.length === 1 &&
    !selectedProduct.value.productDetails[0].colorId &&
    !selectedProduct.value.productDetails[0].sizeName
  ) {
    return selectedProduct.value.productDetails[0]
  }

  if (quickViewSelectedColorId.value && !quickViewSelectedSize.value) {
    return selectedProduct.value.productDetails.find(
      (d) => d.colorId === quickViewSelectedColorId.value,
    )
  }

  return selectedProduct.value.productDetails.find(
    (d) =>
      d.colorId === quickViewSelectedColorId.value && d.sizeName === quickViewSelectedSize.value,
  )
}

// --- COMPUTED PROPERTIES CHO QUICK VIEW ---
const availableSizesForQuickView = computed(() => {
  if (!selectedProduct.value || quickViewSelectedColorId.value === null) return []
  const sizes = selectedProduct.value.productDetails
    .filter((detail) => detail.colorId === quickViewSelectedColorId.value)
    .map((detail) => detail.sizeName)
  return [...new Set(sizes)].sort((a, b) => {
    // Sắp xếp size theo thứ tự: S, M, L, XL, XXL, ...
    const sizeOrder = { S: 1, M: 2, L: 3, XL: 4, XXL: 5 }
    return (sizeOrder[a] || 99) - (sizeOrder[b] || 99)
  })
})

const selectedVariantStock = computed(() => {
  const detail = findSelectedProductDetail()
  // Nếu chưa chọn size hoặc không tìm thấy chi tiết, có thể trả về 0 hoặc một giá trị mặc định phù hợp
  return detail ? detail.quantity : 0
})

// --- HÀM XỬ LÝ GIỎ HÀNG VÀ MUA NGAY ---
function handleAddToCartInModal() {
  const detail = findSelectedProductDetail()

  // Kiểm tra nếu sản phẩm có biến thể theo size mà người dùng chưa chọn size
  // Lấy ra tất cả các size name duy nhất từ productDetails của selectedProduct
  const allSizes = [...new Set(selectedProduct.value.productDetails.map((d) => d.sizeName))]

  // Nếu có bất kỳ size nào không null/undefined và người dùng chưa chọn size
  if (
    allSizes.some((size) => size !== null && size !== undefined) &&
    !quickViewSelectedSize.value
  ) {
    ElMessage.warning('Vui lòng chọn kích thước sản phẩm trước khi thêm vào giỏ hàng.')
    return false
  }

  if (!detail) {
    ElMessage.error(
      'Lỗi: Không tìm thấy chi tiết sản phẩm phù hợp. Vui lòng chọn lại màu sắc và kích thước.',
    )
    return false
  }

  if (quickViewQuantity.value <= 0) {
    ElMessage.warning('Số lượng phải lớn hơn 0.')
    return false
  }

  if (quickViewQuantity.value > detail.quantity) {
    ElMessage.warning(
      `Số lượng tồn kho không đủ, chỉ còn ${detail.quantity} sản phẩm. Vui lòng đặt trước nếu muốn mua nhiều hơn.`,
    )
    // Tùy chọn: Mở dialog đặt trước ngay tại đây nếu bạn muốn
    openPreOrderDialog()
    return false
  }

  const item = {
    productDetailId: detail.id,
    productId: selectedProduct.value.id,
    productName: selectedProduct.value.productName,
    image: quickViewActiveImage.value,
    color: quickViewSelectedColor.value ? quickViewSelectedColor.value.colorName : 'N/A', // Đảm bảo có màu
    size: quickViewSelectedSize.value || 'N/A', // Đảm bảo có size
    price: detail.discountedPrice > 0 ? detail.discountedPrice : detail.sellPrice,
    quantity: quickViewQuantity.value,
    maxQuantity: detail.quantity, // Thêm maxQuantity để giỏ hàng biết giới hạn
  }
  addToCart(item)
  ElMessage.success('Đã thêm vào giỏ hàng!')
  quickViewVisible.value = false // Đóng modal sau khi thêm thành công
  return true
}

function handleBuyNowInModal() {
  const addedToCartSuccessfully = handleAddToCartInModal()
  if (addedToCartSuccessfully) {
    router.push('/cart')
  }
}

// --- HÀM HỖ TRỢ HIỂN THỊ ---
function changeProductImage(product, newImage) {
  if (product && newImage) {
    const productIndex = products.value.findIndex((p) => p.id === product.id)
    if (productIndex !== -1) {
      products.value[productIndex].activeImage = newImage
    }
  }
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
    pink: '#FFC0CB',
    yellow: '#FFFF00',
    purple: '#800080',
    orange: '#FFA500',
    brown: '#A52A2A',
    navy: '#000080',
    beige: '#F5F5DC',
    gold: '#FFD700',
    // Thêm các màu khác nếu cần
  }
  return colorMap[String(colorName).toLowerCase()] || '#CCCCCC' // Mặc định màu xám nhạt nếu không tìm thấy
}

const formatPrice = (price) => {
  if (typeof price !== 'number' || isNaN(price)) return 'N/A'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}

// --- HÀM XỬ LÝ PRE-ORDER MODAL ---
function openPreOrderDialog() {
  preOrderDialogVisible.value = true
  // preOrderItem đã được set trong watcher khi preOrderDialogVisible thay đổi
}

const preOrderAvailableColors = computed(() => {
  if (!preOrderItem.value) return []
  // Sử dụng trực tiếp variants đã được xử lý và có sẵn ảnh
  return preOrderItem.value.variants
})

const preOrderAvailableSizes = computed(() => {
  if (!preOrderItem.value || preOrderSelectedColorId.value === null) return []
  const sizes = preOrderItem.value.productDetails
    .filter((d) => d.colorId === preOrderSelectedColorId.value)
    .map((d) => d.sizeName)
  return [...new Set(sizes)].sort((a, b) => {
    const sizeOrder = { S: 1, M: 2, L: 3, XL: 4, XXL: 5 }
    return (sizeOrder[a] || 99) - (sizeOrder[b] || 99)
  })
})

const preOrderStock = computed(() => {
  // Đối với đặt trước, số lượng trong kho có thể là 0, hoặc số lượng hiện có nếu muốn đặt thêm
  // Ở đây, chúng ta sẽ hiển thị số lượng thực tế trong kho (có thể là 0)
  if (!preOrderItem.value || preOrderSelectedColorId.value === null || !preOrderSelectedSize.value)
    return 0
  const detail = preOrderItem.value.productDetails.find(
    (d) => d.colorId === preOrderSelectedColorId.value && d.sizeName === preOrderSelectedSize.value,
  )
  return detail?.quantity || 0
})

function handlePreOrderConfirm() {
  if (!preOrderSelectedColorId.value || !preOrderSelectedSize.value) {
    ElMessage.warning('Vui lòng chọn đầy đủ màu sắc và kích thước trước khi đặt trước.')
    return
  }

  // BỔ SUNG: Kiểm tra số lượng đặt trước
  if (preOrderQuantity.value <= 0) {
    ElMessage.warning('Số lượng đặt trước phải lớn hơn 0.')
    return
  }

  // Lấy chi tiết sản phẩm cho đơn đặt trước
  const preOrderProductDetail = preOrderItem.value.productDetails.find(
    (d) => d.colorId === preOrderSelectedColorId.value && d.sizeName === preOrderSelectedSize.value,
  )

  if (!preOrderProductDetail) {
    ElMessage.error('Lỗi: Không tìm thấy chi tiết sản phẩm để đặt trước.')
    return
  }

  apiClient
    .post('/reservations', {
      productDetailId: preOrderProductDetail.id,
      quantity: preOrderQuantity.value,
    })
    .then((response) => {
      ElMessage.success(
        'Yêu cầu đặt trước đã được gửi thành công! Chúng tôi sẽ liên hệ lại với bạn sớm nhất.',
      )
      preOrderDialogVisible.value = false
    })
    .catch((error) => {
      console.error('Lỗi khi đặt trước:', error)
      ElMessage.error('Có lỗi xảy ra khi đặt trước. Vui lòng thử lại.')
    })

  ElMessage.success(
    `Đã đặt trước ${preOrderQuantity.value} sản phẩm ${preOrderItem.value.productName} (Màu: ${preOrderProductDetail.colorName}, Size: ${preOrderProductDetail.sizeName})! Chúng tôi sẽ liên hệ lại với bạn sớm nhất.`,
  )
  preOrderDialogVisible.value = false
}
</script>

<style scoped>
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

.loading-state,
.error-state {
  text-align: center;
  padding: 20px;
  font-size: 18px;
  color: #555;
}

.product-list-main {
  max-width: 100%;
  overflow-x: hidden;
  padding: 0;
}

/* --- PRODUCT CARD --- */
.product-card {
  margin-bottom: 20px;
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease-in-out;
}

.product-card:hover {
  transform: translateY(-5px);
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

.product-card__quick-view-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 2;
  background-color: rgba(255, 255, 255, 0.9) !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-card__quick-view-btn {
  opacity: 1;
}

.product-card__info {
  padding: 15px;
  text-align: left;
}

.product-card__name {
  font-size: 15px;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  cursor: pointer;
  transition: color 0.2s;
}

.product-card__name:hover {
  color: #007bff; /* Thay đổi màu khi hover */
}

.product-card__price-container {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 0 0 10px 0;
  height: 24px;
  /* Đảm bảo không gian đủ cho giá */
}

.discounted-price {
  font-size: 16px;
  font-weight: 600;
  color: #d9534f;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.normal-price {
  font-size: 16px;
  font-weight: 600;
  color: #000;
}

.product-card__colors {
  display: flex;
  gap: 6px;
  height: 14px;
  margin-top: 10px; /* Adjust as needed for spacing */
}

.product-card__color-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: transform 0.2s;
}

.product-card__color-dot:hover {
  transform: scale(1.2);
}

/* --- QUICK VIEW MODAL --- */
:deep(.el-dialog.quick-view-dialog) {
  border-radius: 8px;
  /* Đảm bảo dialog có màu nền nếu không có */
  background-color: #fff;
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
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 10px 0;
  line-height: 1.3;
  color: #333;
}

/* BỔ SUNG: Style cho mô tả sản phẩm trong Quick View */
.quick-view__description {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
  max-height: 80px; /* Giới hạn chiều cao để không bị quá dài */
  overflow-y: auto; /* Thêm thanh cuộn nếu mô tả dài */
}
/* HẾT BỔ SUNG */

.quick-view__color-display {
  font-size: 16px;
  margin-bottom: 15px;
  color: #555;
  height: 20px;
  font-weight: 500;
}

.quick-view__color-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.selector-label {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.color-thumbnail {
  cursor: pointer;
  border: 2px solid #e0e0e0;
  padding: 2px;
  transition:
    border-color 0.2s,
    transform 0.2s;
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.color-thumbnail:hover {
  transform: translateY(-2px);
}

.color-thumbnail.is-selected {
  border-color: #000;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.2);
}

.color-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 2px;
}

.quick-view__size-selector {
  margin-bottom: 20px;
}

.size-button {
  margin: 0 8px 8px 0 !important;
  border-radius: 5px;
  border: 1px solid #ccc;
  font-weight: 600;
  min-width: 50px;
  padding: 8px 15px;
  transition:
    background-color 0.2s,
    color 0.2s,
    border-color 0.2s;
}

.size-button.is-selected {
  background-color: #000 !important;
  color: #fff !important;
  border-color: #000 !important;
}

.quick-view__quantity-selector {
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.stock-info {
  font-size: 14px;
  color: #999;
  margin-left: 0px;
}

.quick-view__actions {
  display: flex;
  gap: 10px;
  margin-top: 30px; /* Tăng khoảng cách để tách biệt */
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

.add-to-cart-btn:hover {
  background-color: #f5f5f5 !important;
}

.buy-now-btn {
  background-color: #000 !important;
  color: #fff !important;
}

.buy-now-btn:hover {
  background-color: #333 !important;
}

/* Style cho dòng chữ "Trường hợp hết hàng..." */
.pre-order-text {
  color: #555; /* Màu chữ mặc định */
  font-size: 13px;
  margin-top: 20px; /* Đặt khoảng cách với các nút bên trên */
  text-align: center; /* Căn giữa */
  padding: 5px; /* Thêm chút padding */
  border: 1px dashed #ccc; /* Viền mờ để dễ nhận biết */
  border-radius: 4px;
  background-color: #f9f9f9; /* Nền nhẹ */
}

.pre-order-text a {
  color: #007bff; /* Màu xanh nổi bật cho link */
  text-decoration: underline;
  font-weight: 600;
  transition: color 0.2s;
}

.pre-order-text a:hover {
  color: #0056b3;
}

/* --- PHÂN TRANG --- */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>

<template>
  <div class="page-wrapper">
    <!-- Banner -->
    <el-carousel :interval="4000" height="500px" class="banner-carousel">
      <el-carousel-item v-for="item in carouselImages" :key="item.id">
        <img :src="item.src" class="carousel-image" alt="Banner Image" />
      </el-carousel-item>
    </el-carousel>

    <!-- Sản phẩm mới -->
    <div class="product-collection-container">
      <div class="collection-header">
        <h2 class="collection-title">Sản phẩm mới</h2>
      </div>

      <!-- Trạng thái -->
      <div v-if="isLoading" class="loading-state">Đang tải sản phẩm…</div>
      <div v-else-if="error" class="error-state">{{ error }}</div>

      <!-- Grid -->
      <el-row v-else :gutter="30">
        <el-col
          v-for="product in products"
          :key="product.id"
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <div class="product-card">
            <div class="product-card__image-wrapper">
              <span
                v-if="product.discountPercentage > 0"
                class="product-card__discount-badge"
              >
                -{{ product.discountPercentage }}%
              </span>

              <img
                :src="product.activeImage"
                class="product-card__image"
                :alt="`Ảnh ${product.productName}`"
                loading="lazy"
                decoding="async"
                @click="goToDetail(product.id)"
              />

              <el-button
                :icon="ShoppingCart"
                circle
                size="large"
                class="product-card__quick-view-btn"
                aria-label="Xem nhanh"
                @click.stop="openQuickViewModal(product)"
              />

              <!-- ĐÃ BỔ SUNG: Badge ĐÃ BÁN (góc dưới phải) -->
              <div class="product-card__sold-badge">
                Đã bán: <strong>{{ soldCounts[product.id] ?? '...' }}</strong>
              </div>
            </div>

            <div class="product-card__info">
              <p class="product-card__name" @click="goToDetail(product.id)">
                {{ product.productName }}
              </p>

              <div class="product-card__price-container">
                <template v-if="product.discountPercentage > 0 && Number(product.discountedPrice) > 0">
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
                  :title="variant.colorName"
                  @mouseenter="changeProductImage(product, variant.image)"
                  @mouseleave="restoreProductImage(product)"
                />
              </div>

              <!-- ĐÃ BỔ SUNG: Rating (trung bình & tổng lượt) -->
              <div
                class="rating-row"
                v-if="ratingSummaries[product.id]"
              >
                <el-rate
                  :model-value="(ratingSummaries[product.id]?.avg ?? 0)"
                  disabled
                  allow-half
                  :max="5"
                  size="small"
                />
                <span class="rating-text">
                  {{ (ratingSummaries[product.id]?.avg ?? 0).toFixed(1) }} ⭐
                  ({{ ratingSummaries[product.id]?.count ?? 0 }})
                </span>
              </div>

              <el-button
                v-if="product.id"
                size="small"
                :type="isFavorite(product.id) ? 'primary' : 'success'"
                :icon="Star"
                @click="toggleFavorite(product.id, product.productName)"
              >
                {{ isFavorite(product.id) ? 'Đã yêu thích' : 'Thêm vào Yêu thích' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Quick View Dialog -->
    <el-dialog
      v-model="quickViewVisible"
      width="850px"
      class="quick-view-dialog"
      :show-close="true"
      @close="resetQuickView"
    >
      <div v-if="selectedProduct" class="quick-view">
        <el-row :gutter="40">
          <el-col :span="12">
            <img :src="quickViewActiveImage" class="quick-view__main-image" alt="Ảnh lớn" />
          </el-col>

          <el-col :span="12" class="quick-view__info">
            <h3 class="quick-view__title">{{ selectedProduct.productName }}</h3>

            <div class="quick-view__color-display" v-if="quickViewSelectedColor">
              Màu: <strong>{{ quickViewSelectedColor.colorName }}</strong>
            </div>

            <div
              class="quick-view__color-selector"
              v-if="selectedProduct.variants?.length"
            >
              <div
                v-for="v in selectedProduct.variants"
                :key="v.colorId"
                class="color-thumbnail"
                :class="{ 'is-selected': quickViewSelectedColorId === v.colorId }"
                :title="v.colorName"
                @click="selectQuickViewColor(v)"
              >
                <img :src="v.image" :alt="v.colorName" />
              </div>
            </div>

            <div
              class="quick-view__size-selector"
              v-if="availableSizesForQuickView.length"
            >
              <p class="selector-label">Kích thước:</p>
              <el-button
                v-for="s in availableSizesForQuickView"
                :key="s"
                class="size-button"
                :class="{ 'is-selected': quickViewSelectedSize === s }"
                :disabled="getStockForSize(s) === 0"
                @click="selectSize(s)"
              >
                {{ s }}
              </el-button>
            </div>

            <div class="quick-view__quantity-selector">
              <p class="selector-label">Số lượng:</p>
              <template v-if="!needSize || quickViewSelectedSize">
                <el-input-number
                  v-model="quickViewQuantity"
                  :min="minQty"
                  :max="maxQty"
                  :disabled="maxQty === 0"
                />
                <span class="stock-info">(Còn lại: {{ selectedVariantStock }})</span>
              </template>
              <template v-else>
                <el-input-number :model-value="0" :min="0" :max="0" disabled />
                <span class="stock-info">Chọn size để xem tồn</span>
              </template>
            </div>

            <div class="quick-view__actions">
              <el-button class="add-to-cart-btn" @click="handleAddToCartInModal">Thêm vào giỏ</el-button>
              <el-button class="buy-now-btn" @click="handleBuyNowInModal">Mua ngay</el-button>
            </div>
          </el-col>
        </el-row>

        <!-- Link đặt trước -->
        <div class="pre-order-text" style="margin-top: 12px; color:#333">
          Trường hợp hết hàng, bạn có thể đặt trước
          <a href="#" @click.prevent="openPreOrderDialog" style="color: inherit; font-weight: 600; text-decoration: underline;">
            click vào đây
          </a>
        </div>
      </div>
    </el-dialog>

    <!-- Dialog Đặt trước -->
    <el-dialog v-model="preOrderDialogVisible" title="Đặt trước sản phẩm" width="500px">
      <template v-if="preOrderItem">
        <p><strong>{{ preOrderItem.productName }}</strong></p>

        <div class="quick-view__color-selector" style="margin-top: 20px">
          <p class="selector-label">Chọn màu:</p>
          <div
            v-for="variant in preOrderAvailableColors"
            :key="variant.colorId"
            class="color-thumbnail"
            :class="{ 'is-selected': preOrderSelectedColorId === variant.colorId }"
            @click="preOrderSelectedColorId = variant.colorId"
            style="width:60px;height:60px;border-radius:4px"
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
            class="size-button"
            :class="{ 'is-selected': preOrderSelectedSize === size }"
            @click="preOrderSelectedSize = size"
          >
            {{ size }}
          </el-button>
          <div class="stock-info" v-if="preOrderSelectedSize">
            (Tồn hiện tại: {{ preOrderStock }})
          </div>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import apiClient from '@/utils/axiosInstance'
import { addToCart } from '@/utils/cart.js'

// ====== Banner images (thay bằng ảnh bạn) ======
import banner1 from '@/img/1.jpg'
import banner2 from '@/img/2.jpg'
import axios from 'axios'

/* =======================
 * State chung
 * ======================= */
const router = useRouter()
const isLoading = ref(true)
const error = ref(null)
const products = ref([])

const carouselImages = ref([
  { id: 1, src: banner1 },
  { id: 2, src: banner2 },
])

/* =======================
 * Helpers
 * ======================= */
const PLACEHOLDER_IMG = 'https://via.placeholder.com/400'
const formatPrice = (price) => {
  const n = Number(price)
  if (!Number.isFinite(n)) return 'N/A'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}
const isHexColor = (str) => /^#([0-9A-F]{3}){1,2}$/i.test(String(str || ''))
const colorCodeOf = (colorName) => {
  if (!colorName) return '#CCCCCC'
  const name = String(colorName).trim().toLowerCase()
  if (isHexColor(name)) return name
  const map = {
    'đen':'#000000','trắng':'#FFFFFF','đỏ':'#FF0000','xanh dương':'#0000FF','xanh lá':'#008000','xám':'#808080',
    'bạc':'#C0C0C0','hồng':'#FFC0CB','vàng':'#FFFF00','tím':'#800080','cam':'#FFA500','nâu':'#A52A2A',
    'xanh navy':'#000080','be':'#F5F5DC','vàng gold':'#FFD700',
    'black':'#000000','white':'#FFFFFF','red':'#FF0000','blue':'#0000FF','green':'#008000','grey':'#808080','gray':'#808080',
    'silver':'#C0C0C0','pink':'#FFC0CB','yellow':'#FFFF00','purple':'#800080','orange':'#FFA500','brown':'#A52A2A',
    'navy':'#000080','beige':'#F5F5DC','gold':'#FFD700',
  }
  return map[name] || '#CCCCCC'
}
const sizeComparator = (a, b) => {
  const order = { XS:0, S:1, M:2, L:3, XL:4, XXL:5, XXXL:6 }
  const A = String(a).toUpperCase()
  const B = String(b).toUpperCase()
  const numA = Number(A), numB = Number(B)
  if (Number.isFinite(numA) && Number.isFinite(numB)) return numA - numB
  if (Number.isFinite(numA)) return -1
  if (Number.isFinite(numB)) return 1
  const wa = order[A] ?? 999
  const wb = order[B] ?? 999
  return wa - wb || A.localeCompare(B)
}

/* =======================
 * Yêu thích (localStorage)
 * ======================= */
const customerId = localStorage.getItem('userId') || null
const favorites = ref([])
const loadFavorites = () => {
  try {
    const saved = JSON.parse(localStorage.getItem('favorites') || '[]')
    favorites.value = Array.isArray(saved) ? saved : []
  } catch { favorites.value = [] }
}
const isFavorite = (productId) => {
  const pid = Number(productId)
  return favorites.value.some(
    (f) => String(f.customerId) === String(customerId) && Number(f.productId) === pid
  )
}
const toggleFavorite = (productId, name) => {
  if (!customerId) {
    ElMessage.warning('Vui lòng đăng nhập để thêm vào yêu thích.')
    return
  }
  const pid = Number(productId)
  if (isFavorite(pid)) {
    favorites.value = favorites.value.filter(
      (f) => !(String(f.customerId) === String(customerId) && Number(f.productId) === pid)
    )
    ElMessage.success(`Đã bỏ "${name}" khỏi yêu thích.`)
  } else {
    favorites.value.push({ customerId, productId: pid })
    ElMessage.success('Đã thêm vào danh sách yêu thích.')
  }
}
watch(favorites, (val) => localStorage.setItem('favorites', JSON.stringify(val)), { deep: true })

/* =======================
 * Chuẩn hoá product
 * ======================= */
const normalizeProduct = (p) => {
  const details = Array.isArray(p.productDetails) ? p.productDetails : []
  const images = Array.isArray(p.productImages) ? p.productImages : []

  const seen = new Set()
  const variants = []
  for (const d of details) {
    const cid = d?.colorId
    if (cid == null || seen.has(cid)) continue
    seen.add(cid)
    const img = images.find((i) => i?.colorId === cid)
    variants.push({
      colorId: cid,
      colorName: d?.colorName || 'Không rõ',
      colorCode: colorCodeOf(d?.colorName),
      image: img?.image ? `data:image/jpeg;base64,${img.image}` : (img?.url || null),
    })
  }

  const activeImage = variants[0]?.image || images[0]?.url || PLACEHOLDER_IMG
  return { ...p, variants, activeImage, __originalImage: activeImage }
}

/* =======================
 * Fetch sản phẩm
 * ======================= */
const fetchFeaturedProducts = async () => {
  isLoading.value = true
  error.value = null
  try {
    // Dùng apiClient để tận dụng baseURL "/api"
    const { data } = await apiClient.get('/online-sale/online-home')
    const unique = new Map((Array.isArray(data) ? data : []).map((it) => [it.id, it]))
    products.value = Array.from(unique.values()).map(normalizeProduct)

    // Gọi dữ liệu phụ: sold count + rating
    for (const p of products.value) {
      fetchSoldCount(p.id)
      fetchRatingSingle(p.id)
    }
  } catch (e) {
    console.error('Lỗi khi tải sản phẩm:', e)
    error.value = 'Không thể tải danh sách sản phẩm.'
    ElMessage.error(error.value)
  } finally {
    isLoading.value = false
  }
}

/* =======================
 * Sold count
 * ======================= */
const soldCounts = ref({})
const soldLoading = new Set()
const fetchSoldCount = async (productId) => {
  const pid = Number(productId)
  if (!Number.isFinite(pid)) return
  if (soldCounts.value[pid] != null || soldLoading.has(pid)) return
  soldLoading.add(pid)
  try {
    const { data } = await apiClient.get(`/online-sale/sold-quantity/product/${pid}`)
    const total = Number(data?.totalSoldQuantity ?? 0)
    soldCounts.value[pid] = Number.isFinite(total) ? total : 0
  } catch (e) {
    console.error('Fetch sold count error', pid, e)
    soldCounts.value[pid] = 0
  } finally {
    soldLoading.delete(pid)
  }
}

/* =======================
 * Ratings (avg & count)
 * ======================= */
const ratingSummaries = ref({}) // { [productId]: { avg, count } }
const ratingLoading = new Set()

const fetchRatingSingle = async (pid) => {
  const id = Number(pid)
  if (!Number.isFinite(id)) return
  if (ratingSummaries.value[id] || ratingLoading.has(id)) return
  ratingLoading.add(id)
  try {
    const { data } = await apiClient.get(`/online-sale/ratings/product/${id}`)
    ratingSummaries.value[id] = {
      avg: Number(data?.avgRating ?? 0),
      count: Number(data?.totalReviews ?? 0),
    }
  } catch (e) {
    console.error('rating single error:', id, e)
    ratingSummaries.value[id] = { avg: 0, count: 0 }
  } finally {
    ratingLoading.delete(id)
  }
}

/* =======================
 * Quick view
 * ======================= */
const quickViewVisible = ref(false)
const selectedProduct = ref(null)
const quickViewSelectedColorId = ref(null)
const quickViewSelectedColor = ref(null)
const quickViewSelectedSize = ref(null)
const quickViewQuantity = ref(1)
const quickViewActiveImage = ref(PLACEHOLDER_IMG)

const openQuickViewModal = (p) => {
  selectedProduct.value = p
  quickViewVisible.value = true
  quickViewQuantity.value = 1

  if (p.variants?.length) {
    selectQuickViewColor(p.variants[0])
  } else {
    quickViewSelectedColorId.value = null
    quickViewSelectedColor.value = null
    quickViewActiveImage.value = p.activeImage || PLACEHOLDER_IMG
    quickViewSelectedSize.value = null
  }
}
const resetQuickView = () => {
  setTimeout(() => {
    selectedProduct.value = null
    quickViewSelectedColorId.value = null
    quickViewSelectedColor.value = null
    quickViewSelectedSize.value = null
    quickViewQuantity.value = 1
    quickViewActiveImage.value = PLACEHOLDER_IMG
  }, 200)
}
const selectQuickViewColor = (variant) => {
  quickViewSelectedColorId.value = variant.colorId
  quickViewSelectedColor.value = variant
  quickViewActiveImage.value = variant.image || PLACEHOLDER_IMG
  quickViewSelectedSize.value = null
  quickViewQuantity.value = 1
  if (availableSizesForQuickView.value.length > 0) {
    quickViewSelectedSize.value = availableSizesForQuickView.value[0]
  }
}
const selectSize = (s) => {
  quickViewSelectedSize.value = s
  quickViewQuantity.value = 1
}
const findSelectedProductDetail = () => {
  const sp = selectedProduct.value
  if (!sp || !Array.isArray(sp.productDetails)) return null
  if (sp.productDetails.length === 1 && !sp.productDetails[0].colorId && !sp.productDetails[0].sizeName) {
    return sp.productDetails[0]
  }
  if (quickViewSelectedColorId.value && quickViewSelectedSize.value) {
    return sp.productDetails.find(
      (d) => d.colorId === quickViewSelectedColorId.value && d.sizeName === quickViewSelectedSize.value
    )
  }
  return null
}
const getStockForSize = (size) => {
  const sp = selectedProduct.value
  if (!sp) return 0
  const d = sp.productDetails?.find((x) => x.colorId === quickViewSelectedColorId.value && x.sizeName === size)
  return Number(d?.quantity || 0)
}
const availableSizesForQuickView = computed(() => {
  const sp = selectedProduct.value
  if (!sp || quickViewSelectedColorId.value == null) return []
  const sizes = (sp.productDetails || [])
    .filter((d) => d.colorId === quickViewSelectedColorId.value)
    .map((d) => d.sizeName)
    .filter(Boolean)
  return Array.from(new Set(sizes)).sort(sizeComparator)
})
const selectedVariantStock = computed(() => {
  const d = findSelectedProductDetail()
  return d ? Number(d.quantity || 0) : 0
})
const needSize = computed(() => {
  const sp = selectedProduct.value
  if (!sp) return false
  const named = new Set((sp.productDetails || []).map((d) => d.sizeName).filter(Boolean))
  return named.size > 0
})
const minQty = computed(() => (selectedVariantStock.value > 0 ? 1 : 0))
const maxQty = computed(() => (selectedVariantStock.value > 0 ? selectedVariantStock.value : 0))
watch([selectedVariantStock, quickViewSelectedSize, quickViewVisible], () => {
  if (!quickViewVisible.value) return
  const min = minQty.value
  const max = maxQty.value
  if (max === 0) quickViewQuantity.value = 0
  else {
    if (quickViewQuantity.value < min) quickViewQuantity.value = min
    if (quickViewQuantity.value > max) quickViewQuantity.value = max
  }
})

/* =======================
 * Cart / Buy now
 * ======================= */
const requiresSizeSelection = (sp) => {
  const named = new Set((sp.productDetails || []).map((d) => d.sizeName).filter(Boolean))
  return named.size > 0
}
const handleAddToCartInModal = async () => {
  const sp = selectedProduct.value
  if (!sp) return false

  if (requiresSizeSelection(sp) && !quickViewSelectedSize.value) {
    ElMessage.warning('Vui lòng chọn kích thước sản phẩm trước khi thêm vào giỏ hàng.')
    return false
  }

  const detail = findSelectedProductDetail()

  const productDetailId = detail.id;
  try {
  const res = await axios.get(`http://localhost:8080/api/online-sale/verify-pdDetail/${productDetailId}`)
  console.log('res hihi: ',res)
  const status = res?.data?.status;
  const statusNum = Number(status)
  if (!Number.isFinite(statusNum) || statusNum !== 1) {
    ElMessage.error('Sản phẩm hiện không hợp lệ để mua (đã bị vô hiệu hóa hoặc không còn bán).')
    return false
  }
} catch (e) {
  console.error('Lỗi khi kiểm tra trạng thái productDetail:', e)
  ElMessage.error('Không thể kiểm tra trạng thái sản phẩm. Vui lòng thử lại sau.')
  return false
}

  if (!detail) {
    ElMessage.error('Không tìm thấy chi tiết phù hợp. Vui lòng chọn lại màu và kích thước.')
    return false
  }

  if (quickViewQuantity.value <= 0) {
    ElMessage.warning('Số lượng phải lớn hơn 0.')
    return false
  }
  if (quickViewQuantity.value > Number(detail.quantity || 0)) {
    ElMessage.warning(`Tồn kho không đủ (còn ${detail.quantity}).`)
    return false
  }

  const price = Number(detail.discountedPrice) > 0 ? Number(detail.discountedPrice) : Number(detail.sellPrice)
  const item = {
    productDetailId: detail.id,
    productId: sp.id,
    productName: sp.productName,
    image: quickViewActiveImage.value,
    color: quickViewSelectedColor.value ? quickViewSelectedColor.value.colorName : 'N/A',
    size: quickViewSelectedSize.value || 'N/A',
    price,
    quantity: quickViewQuantity.value,
    maxQuantity: Number(detail.quantity || 0),
    discountCampaignId: detail.discountCampaignId || null,
  }

  addToCart(item)
  ElMessage.success('Đã thêm vào giỏ hàng!')
  quickViewVisible.value = false
  return true
}
const handleBuyNowInModal = () => {
  if (handleAddToCartInModal()) router.push('/cart')
}

/* =======================
 * Pre-order
 * ======================= */
const preOrderDialogVisible = ref(false)
const preOrderItem = ref(null)
const preOrderSelectedColorId = ref(null)
const preOrderSelectedSize = ref(null)
const preOrderQuantity = ref(1)

const openPreOrderDialog = () => { preOrderDialogVisible.value = true }

watch(preOrderDialogVisible, (val) => {
  if (val && selectedProduct.value) {
    preOrderItem.value = JSON.parse(JSON.stringify(selectedProduct.value))
    if (preOrderItem.value.variants?.length) {
      preOrderSelectedColorId.value = preOrderItem.value.variants[0].colorId
    } else {
      preOrderSelectedColorId.value = null
    }
    if (quickViewSelectedSize.value && preOrderAvailableSizes.value.includes(quickViewSelectedSize.value)) {
      preOrderSelectedSize.value = quickViewSelectedSize.value
    } else {
      preOrderSelectedSize.value = preOrderAvailableSizes.value[0] || null
    }
    preOrderQuantity.value = 1
  } else if (!val) {
    preOrderItem.value = null
    preOrderSelectedColorId.value = null
    preOrderSelectedSize.value = null
    preOrderQuantity.value = 1
  }
})
watch(preOrderSelectedColorId, (n, o) => {
  if (n !== o && preOrderDialogVisible.value) {
    preOrderSelectedSize.value = preOrderAvailableSizes.value[0] || null
  }
})

const preOrderAvailableColors = computed(() => preOrderItem.value?.variants || [])
const preOrderAvailableSizes = computed(() => {
  if (!preOrderItem.value || preOrderSelectedColorId.value == null) return []
  const sizes = (preOrderItem.value.productDetails || [])
    .filter((d) => d.colorId === preOrderSelectedColorId.value)
    .map((d) => d.sizeName)
    .filter(Boolean)
  return Array.from(new Set(sizes)).sort(sizeComparator)
})
const preOrderStock = computed(() => {
  if (!preOrderItem.value || preOrderSelectedColorId.value == null || !preOrderSelectedSize.value) return 0
  const d = preOrderItem.value.productDetails?.find(
    (x) => x.colorId === preOrderSelectedColorId.value && x.sizeName === preOrderSelectedSize.value
  )
  return Number(d?.quantity || 0)
})

const handlePreOrderConfirm = async () => {
  if (!preOrderSelectedColorId.value || !preOrderSelectedSize.value) {
    ElMessage.warning('Vui lòng chọn đầy đủ màu sắc và kích thước trước khi đặt trước.')
    return
  }
  if (preOrderQuantity.value <= 0) {
    ElMessage.warning('Số lượng đặt trước phải lớn hơn 0.')
    return
  }
  const detail = preOrderItem.value?.productDetails?.find(
    (d) => d.colorId === preOrderSelectedColorId.value && d.sizeName === preOrderSelectedSize.value
  )
  if (!detail) {
    ElMessage.error('Không tìm thấy chi tiết sản phẩm để đặt trước.')
    return
  }
  try {
    await apiClient.post('/reservations', { productDetailId: detail.id, quantity: preOrderQuantity.value })
    ElMessage.success('Yêu cầu đặt trước đã được gửi. Chúng tôi sẽ liên hệ lại sớm nhất!')
    preOrderDialogVisible.value = false
  } catch (err) {
    console.error('Lỗi đặt trước:', err)
    ElMessage.error('Có lỗi xảy ra khi đặt trước. Vui lòng thử lại.')
  }
}

/* =======================
 * Minor helpers
 * ======================= */
const changeProductImage = (p, img) => {
  if (!p || !img) return
  p.__hoverImage = img
  p.activeImage = img
}
const restoreProductImage = (p) => {
  if (!p) return
  p.activeImage = p.__originalImage || p.activeImage
}
const goToDetail = (id) => router.push(`/product/${id}`)

/* =======================
 * Lifecycle
 * ======================= */
onMounted(() => {
  loadFavorites()
  fetchFeaturedProducts()
})
</script>

<style scoped>
/* ========= Design tokens ========= */
:root{
  --container-w: 1360px;
  --gap: 20px;

  --radius: 14px;
  --radius-sm: 10px;

  --shadow-1: 0 2px 10px rgba(0,0,0,.06);
  --shadow-2: 0 8px 24px rgba(0,0,0,.08);

  --border: 1px solid #e9e9ef;
  --bg-card: #ffffff;
  --bg-soft: #f7f8fb;

  --text-1: #17181c;
  --text-2: #4d5160;
  --text-3: #949ab0;

  --primary: #111111;
  --accent: #0ea5e9;
  --danger: #ff0000; /* đỏ tươi cho % giảm */
}

/* ========= Common ========= */
.page-wrapper{ max-width:100%; }
.loading-state, .error-state{ text-align:center; padding:20px; font-size:18px; color:#555; }

/* ========= Banner ========= */
.banner-carousel{ max-width:100%; }
.carousel-image{
  width:100%; height:auto; display:block;
  object-fit:cover; /* giữ tỉ lệ ảnh, lấp đầy khung nếu có chiều cao cố định */
}

/* ========= Collection ========= */
.product-collection-container{ max-width: var(--container-w); margin: 48px auto 60px; padding: 0 16px; }
.collection-header{ text-align:center; margin-bottom: 22px; }
.collection-title{ font-size: 28px; font-weight: 600; line-height: 1.2; letter-spacing: .2px; color: var(--text-1); }

/* ========= Card ========= */
.product-card{
  margin-bottom: var(--gap);
  background: var(--bg-card);
  border: var(--border);
  border-radius: var(--radius);
  overflow:hidden;
  box-shadow: var(--shadow-1);
  transition: transform .18s ease, box-shadow .18s ease, border-color .2s ease;
}
.product-card:hover{ transform: translateY(-4px); box-shadow: var(--shadow-2); }

.product-card__image-wrapper{
  position:relative; width:100%;
  aspect-ratio: 1/1; /* ảnh vuông, cân */
  background: var(--bg-soft);
  overflow:hidden; cursor:pointer;
}
.product-card__image{
  position:absolute; inset:0; width:100%; height:100%;
  object-fit:cover; transition: transform .28s ease;
}
.product-card:hover .product-card__image{ transform: scale(1.035); }

/* Badge % giảm: đỏ tươi nền đặc, chữ trắng */
.product-card__discount-badge{
  position:absolute; top:10px; left:10px;
  background-color:#ff0000 !important; color:#fff !important;
  border:0 !important; padding:4px 8px; border-radius:999px;
  font-size:12px; font-weight:600; z-index:2;
  box-shadow:none !important; filter:none !important;
  mix-blend-mode:normal !important; backdrop-filter:none !important; -webkit-backdrop-filter:none !important;
}

/* Quick view button */
.product-card__quick-view-btn{
  position:absolute; top:12px; right:12px; z-index:2;
  background:rgba(255,255,255,.96)!important;
  border:1px solid #f0f0f5 !important;
  box-shadow: var(--shadow-1);
  opacity:0; transition: opacity .22s ease;
}
.product-card:hover .product-card__quick-view-btn{ opacity:1; }

/* Badge đã bán */
.product-card__sold-badge{
  position:absolute; right:10px; bottom:10px; z-index:2;
  background: rgba(20,22,26,.75); color:#fff; font-weight:600;
  padding:6px 10px; border-radius:10px; font-size:12px; line-height:1;
  backdrop-filter: blur(2px);
}

/* Info */
.product-card__info{ padding:15px; text-align:left; }
.product-card__name{
  font-size:15px; font-weight:600; color:var(--text-1);
  margin:0 0 6px 0; line-height:1.35; height:42px; overflow:hidden;
  text-overflow:ellipsis; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical;
  transition: color .15s ease; cursor:pointer;
}
.product-card__name:hover{ color: var(--accent); }

.product-card__price-container{ display:flex; align-items:baseline; gap:8px; margin:0 0 10px 0; height:24px; }
.discounted-price{ font-size:16px; font-weight:600; color: var(--danger); }
.original-price{ font-size:13px; color:#9aa0b4; text-decoration:line-through; font-weight:400; }
.normal-price{ font-size:16px; font-weight:600; color:#000; }

/* Color variants */
.product-card__colors{ display:flex; gap:6px; height:14px; margin-top:10px; }
.product-card__color-dot{
  width:14px; height:14px; border-radius:50%;
  border:1px solid #e0e0e0; cursor:pointer; transition: transform .2s ease, box-shadow .2s ease;
}
.product-card__color-dot:hover{ transform: scale(1.12); box-shadow:0 2px 6px rgba(0,0,0,.12); }

/* Rating */
.rating-row{ margin-top:8px; display:flex; justify-content:space-between; align-items:center; }
.rating-text{ font-size:12px; color:#666; white-space:nowrap; }

/* ========= Quick view ========= */
:deep(.el-dialog.quick-view-dialog){ border-radius: 12px; background:#fff; box-shadow: var(--shadow-2); }
:deep(.quick-view-dialog .el-dialog__header){ padding:0; position:absolute; top:15px; right:15px; z-index:1; }
:deep(.quick-view-dialog .el-dialog__headerbtn .el-icon){ font-size:24px; color:#555; }
:deep(.quick-view-dialog .el-dialog__body){ padding: 34px 36px; }

.quick-view__main-image{
  width:100%; height:auto; aspect-ratio:1/1; object-fit:cover;
  border-radius:8px; border: var(--border);
}
.quick-view__info{ display:flex; flex-direction:column; height:100%; }
.quick-view__title{ font-size:24px; font-weight:700; margin:0 0 10px; line-height:1.3; color:var(--text-1); }
.quick-view__color-display{ font-size:14px; margin-bottom:12px; color:var(--text-2); height:20px; font-weight:600; }

.quick-view__color-selector{ display:flex; flex-wrap:wrap; gap:10px; margin-bottom:16px; }
.selector-label{ font-size:13px; font-weight:600; margin:0 0 10px; color:var(--text-2); text-transform:uppercase; letter-spacing:.4px; }
.color-thumbnail{
  cursor:pointer; border:2px solid #e0e0e0; padding:2px; width:60px; height:60px; border-radius:8px;
  transition: border-color .18s ease, transform .18s ease;
}
.color-thumbnail:hover{ transform: translateY(-2px); }
.color-thumbnail.is-selected{ border-color:#000; box-shadow:0 0 0 3px rgba(0,0,0,.06); }
.color-thumbnail img{ width:100%; height:100%; object-fit:cover; display:block; border-radius:4px; }

.quick-view__size-selector{ margin-bottom:18px; }
.size-button{
  margin:0 8px 8px 0 !important; border-radius:8px;
  border:1px solid #ccc; font-weight:600; min-width:50px; padding:8px 15px;
  transition: background-color .18s ease, color .18s ease, border-color .18s ease;
}
.size-button.is-selected{ background:#000 !important; color:#fff !important; border-color:#000 !important; }

.quick-view__quantity-selector{ margin-bottom:22px; display:flex; align-items:center; gap:14px; }
.stock-info{ font-size:13px; color:#999; margin-left:0; }
.quick-view__actions{ display:flex; gap:10px; margin-top:24px; }
.quick-view__actions .el-button{
  flex:1; height:46px; font-size:15px; font-weight:600; border-radius:8px; border:1px solid #000 !important;
}
.add-to-cart-btn{ background:#fff !important; color:#000 !important; }
.add-to-cart-btn:hover{ background:#f5f5f5 !important; }
.buy-now-btn{ background:#000 !important; color:#fff !important; }
.buy-now-btn:hover{ background:#333 !important; }

/* ========= Misc ========= */
.pre-order-text a:hover{ opacity:.85; }

/* ========= Responsive ========= */
@media (max-width: 992px){
  .product-collection-container{ padding: 0 12px; }
  .collection-title{ font-size:24px; }
}
</style>


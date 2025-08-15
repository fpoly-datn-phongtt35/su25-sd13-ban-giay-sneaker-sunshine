<template>
  <div class="product-collection-container">
    <!-- Header -->
    <div class="collection-header">
      <h2 class="collection-title">
        <!-- THÊM: Ưu tiên hiển thị danh mục -->
        <template v-if="categoryId">
          Danh mục: {{ categoryName || ('#' + categoryId) }}
        </template>
        <template v-else-if="brandId">
          Thương hiệu: {{ brandName || ('#' + brandId) }}
        </template>
        <template v-else>
          Bộ sưu tập
        </template>
      </h2>
    </div>

    <!-- States -->
    <div v-if="isLoading" class="loading-state">Đang tải sản phẩm…</div>
    <div v-else-if="error" class="error-state">{{ error }}</div>

    <!-- Product grid -->
    <el-main v-else class="product-list-main">
      <el-row :gutter="30">
        <el-col
          v-for="p in products"
          :key="p.id"
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <div class="product-card">
            <div class="product-card__image-wrapper">
              <span v-if="p.discountPercentage > 0" class="product-card__discount-badge">
                -{{ p.discountPercentage }}%
              </span>

              <img
                :src="p.activeImage"
                class="product-card__image"
                :alt="`Ảnh ${p.productName}`"
                loading="lazy"
                decoding="async"
                @click="goToDetail(p.id)"
              />

              <el-button
                :icon="ShoppingCart"
                circle
                size="large"
                class="product-card__quick-view-btn"
                aria-label="Xem nhanh"
                @click.stop="openQuickViewModal(p)"
              />
            </div>

            <div class="product-card__info">
              <p class="product-card__name" @click="goToDetail(p.id)">
                {{ p.productName }}
              </p>

              <div class="product-card__price-container">
                <template v-if="p.discountPercentage > 0 && Number(p.discountedPrice) > 0">
                  <span class="discounted-price">{{ formatPrice(p.discountedPrice) }}</span>
                  <del class="original-price">{{ formatPrice(p.sellPrice) }}</del>
                </template>
                <template v-else>
                  <span class="normal-price">{{ formatPrice(p.sellPrice) }}</span>
                </template>
              </div>

              <div class="product-card__colors" v-if="p.variants?.length">
                <span
                  v-for="v in p.variants"
                  :key="v.colorId"
                  class="product-card__color-dot"
                  :style="{ backgroundColor: v.colorCode }"
                  :title="v.colorName"
                  @mouseenter="changeProductImage(p, v.image)"
                  @mouseleave="restoreProductImage(p)"
                />
              </div>

              <el-button
                v-if="p.id"
                size="small"
                :type="isFavorite(p.id) ? 'primary' : 'success'"
                :icon="Star"
                @click="toggleFavorite(p.id, p.productName)"
              >
                {{ isFavorite(p.id) ? 'Đã yêu thích' : 'Thêm vào Yêu thích' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-if="totalItems > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          :total="totalItems"
          layout="total, prev, pager, next, jumper, sizes"
          @size-change="onPageUiChanged"
          @current-change="onPageUiChanged"
        />
      </div>
    </el-main>

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

            <div class="quick-view__color-selector" v-if="selectedProduct.variants?.length">
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

            <div class="quick-view__size-selector" v-if="availableSizesForQuickView.length">
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import apiClient from '@/utils/axiosInstance'   // baseURL nên là http://localhost:8080/api
import { addToCart } from '@/utils/cart.js'

/* =========================
 * Router & Brand
 * ========================= */
const router = useRouter()
const route = useRoute()

const brandId = computed(() => {
  const id = route.query.brandId ?? route.params.brandId
  const n = Number(id)
  return Number.isFinite(n) ? n : null
})
const brandName = ref(String(route.query.brandName || '').trim())

/* =========================
 * Category (THÊM MỚI)
 * ========================= */
const categoryId = computed(() => {
  const id = route.query.categoryId ?? route.params.categoryId
  const n = Number(id)
  return Number.isFinite(n) ? n : null
})
const categoryName = ref(String(route.query.categoryName || '').trim())

/* =========================
 * Common helpers
 * ========================= */
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

/* =========================
 * States
 * ========================= */
const isLoading = ref(true)
const error = ref(null)
const products = ref([])
const totalItems = ref(0)

/* Đồng bộ page/size với URL */
const currentPage = computed({
  get: () => Math.max(1, Number(route.query.page ?? 1)),
  set: (val) => router.push({ query: { ...route.query, page: val } })
})
const pageSize = computed({
  get: () => {
    const s = Number(route.query.size ?? 12)
    return [12,24,36,48].includes(s) ? s : 12
  },
  set: (val) => router.push({ query: { ...route.query, size: val, page: 1 } })
})

/* =========================
 * Favorites (localStorage)
 * ========================= */
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

/* =========================
 * Normalize product
 * ========================= */
const normalizeProduct = (p) => {
  const details = Array.isArray(p.productDetails) ? p.productDetails : []
  const images = Array.isArray(p.productImages) ? p.productImages : []

  // Variants theo color (unique colorId)
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

  // Active image ưu tiên biến thể đầu → ảnh đầu → placeholder
  const activeImage = variants[0]?.image || images[0]?.url || PLACEHOLDER_IMG
  return { ...p, variants, activeImage, __originalImage: activeImage }
}

/* =========================
 * Fetch theo DANH MỤC (THÊM MỚI)
 * ========================= */
const fetchProductsByCategory = async (params) => {
  const endpoints = [
    `/admin/categories/${categoryId.value}/products`,
    `/admin/category/${categoryId.value}/products`,
    `/categories/${categoryId.value}/products`,
    `/online-sale?categoryId=${categoryId.value}`, // fallback query
  ]
  let data = null, lastErr = null
  for (const ep of endpoints) {
    try {
      const res = await apiClient.get(ep, { params })
      data = res?.data
      if (data) break
    } catch (e) {
      lastErr = e
    }
  }
  if (!data) throw lastErr || new Error('Không thể tải sản phẩm theo danh mục.')

  const payload = data ?? {}
  const content =
    Array.isArray(payload.content) ? payload.content :
    Array.isArray(payload.data?.content) ? payload.data.content :
    Array.isArray(payload) ? payload : []

  if (!Array.isArray(content)) throw new Error('Định dạng dữ liệu API không hợp lệ.')

  const map = new Map()
  for (const item of content) if (item && item.id != null) map.set(item.id, item)
  products.value = Array.from(map.values()).map(normalizeProduct)

  totalItems.value =
    Number(payload.totalElements) ||
    Number(payload.data?.totalElements) ||
    Number(payload.page?.totalElements) ||
    content.length
}

/* =========================
 * Fetch products (brand-aware + category-aware)
 * ========================= */
const fetchProducts = async () => {
  isLoading.value = true
  error.value = null
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }

    // THÊM: Ưu tiên danh mục
    if (categoryId.value) {
      await fetchProductsByCategory(params)
      isLoading.value = false
      return
    }

    // Giữ nguyên logic BRAND / mặc định
    let url
    if (brandId.value) {
      url = `/admin/brand/${brandId.value}/products`
    } else {
      url = `/online-sale`
      // hoặc url = `/online-sale/online-home` nếu bạn muốn feed khác
    }

    const { data } = await apiClient.get(url, { params })

    // Chuẩn hóa payload & content
    const payload = data ?? {}
    const content =
      Array.isArray(payload.content) ? payload.content :
      Array.isArray(payload.data?.content) ? payload.data.content :
      Array.isArray(payload) ? payload : []

    if (!Array.isArray(content)) throw new Error('Định dạng dữ liệu API không hợp lệ.')

    // Unique by id và normalize
    const map = new Map()
    for (const item of content) if (item && item.id != null) map.set(item.id, item)
    products.value = Array.from(map.values()).map(normalizeProduct)

    totalItems.value =
      Number(payload.totalElements) ||
      Number(payload.data?.totalElements) ||
      Number(payload.page?.totalElements) ||
      content.length
  } catch (e) {
    console.error(e)
    error.value = 'Không thể tải sản phẩm. Vui lòng thử lại sau.'
    ElMessage.error(error.value)
  } finally {
    isLoading.value = false
  }
}

/* =========================
 * Pagination handlers
 * ========================= */
const onPageUiChanged = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/* =========================
 * Quick view
 * ========================= */
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

/* =========================
 * Cart / Buy now
 * ========================= */
const requiresSizeSelection = (sp) => {
  const named = new Set((sp.productDetails || []).map((d) => d.sizeName).filter(Boolean))
  return named.size > 0
}
const handleAddToCartInModal = () => {
  const sp = selectedProduct.value
  if (!sp) return false

  if (requiresSizeSelection(sp) && !quickViewSelectedSize.value) {
    ElMessage.warning('Vui lòng chọn kích thước sản phẩm trước khi thêm vào giỏ hàng.')
    return false
  }

  const detail = findSelectedProductDetail()
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

/* =========================
 * Pre-order
 * ========================= */
const preOrderDialogVisible = ref(false)
const preOrderItem = ref(null)
const preOrderSelectedColorId = ref(null)
const preOrderSelectedSize = ref(null)
const preOrderQuantity = ref(1)

const openPreOrderDialog = () => {
  preOrderDialogVisible.value = true
}

watch(preOrderDialogVisible, (val) => {
  if (val && selectedProduct.value) {
    preOrderItem.value = JSON.parse(JSON.stringify(selectedProduct.value))
    if (preOrderItem.value.variants?.length) {
      preOrderSelectedColorId.value = preOrderItem.value.variants[0].colorId
    } else {
      preOrderSelectedColorId.value = null
    }
    if (
      quickViewSelectedSize.value &&
      preOrderAvailableSizes.value.includes(quickViewSelectedSize.value)
    ) {
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

const preOrderAvailableColors = computed(() => {
  if (!preOrderItem.value) return []
  return preOrderItem.value.variants || []
})
const preOrderAvailableSizes = computed(() => {
  if (!preOrderItem.value || preOrderSelectedColorId.value == null) return []
  const sizes = (preOrderItem.value.productDetails || [])
    .filter((d) => d.colorId === preOrderSelectedColorId.value)
    .map((d) => d.sizeName)
    .filter(Boolean)
  return Array.from(new Set(sizes)).sort(sizeComparator)
})
const preOrderStock = computed(() => {
  if (!preOrderItem.value || preOrderSelectedColorId.value == null || !preOrderSelectedSize.value)
    return 0
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
    await apiClient.post('/reservations', {
      productDetailId: detail.id,
      quantity: preOrderQuantity.value,
    })
    ElMessage.success('Yêu cầu đặt trước đã được gửi. Chúng tôi sẽ liên hệ lại sớm nhất!')
    preOrderDialogVisible.value = false
  } catch (err) {
    console.error('Lỗi đặt trước:', err)
    ElMessage.error('Có lỗi xảy ra khi đặt trước. Vui lòng thử lại.')
  }
}

/* =========================
 * Minor helpers
 * ========================= */
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

/* =========================
 * Lifecycle & watchers
 * ========================= */
onMounted(() => {
  loadFavorites()
  fetchProducts()
})
watch(() => route.fullPath, () => {
  // GIỮ NGUYÊN: brandName
  brandName.value = String(route.query.brandName || '').trim()
  // THÊM: đồng bộ categoryName
  categoryName.value = String(route.query.categoryName || '').trim()
  fetchProducts()
})
watch(brandId, (n, o) => {
  if (n !== o) {
    router.push({ query: { ...route.query, page: 1 } })
  }
})
// THÊM: reset trang khi đổi danh mục
watch(categoryId, (n, o) => {
  if (n !== o) {
    router.push({ query: { ...route.query, page: 1 } })
  }
})
</script>

<style scoped>
/* Container */
.product-collection-container { max-width: 1400px; margin: 60px auto; padding: 0 20px; }
.collection-header { text-align: center; margin-bottom: 40px; }
.collection-title { font-size: 32px; font-weight: 600; }
.loading-state, .error-state { text-align: center; padding: 20px; font-size: 18px; color: #555; }
.product-list-main { max-width: 100%; overflow-x: hidden; padding: 0; }

/* Card */
.product-card { margin-bottom: 20px; background-color: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: transform .2s ease-in-out; }
.product-card:hover { transform: translateY(-5px); }
.product-card__image-wrapper { position: relative; width: 100%; padding-bottom: 100%; background-color: #f5f5f5; overflow: hidden; cursor: pointer; }
.product-card__image { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; transition: transform .3s ease; }
.product-card:hover .product-card__image { transform: scale(1.05); }
.product-card__discount-badge { position: absolute; top: 10px; left: 10px; background-color: #d9534f; color: #fff; padding: 3px 8px; border-radius: 4px; font-size: 12px; font-weight: 700; z-index: 2; }
.product-card__quick-view-btn { position: absolute; top: 15px; right: 15px; z-index: 2; background-color: rgba(255,255,255,.9) !important; box-shadow: 0 2px 10px rgba(0,0,0,.1); opacity: 0; transition: opacity .3s ease; }
.product-card:hover .product-card__quick-view-btn { opacity: 1; }
.product-card__info { padding: 15px; text-align: left; }
.product-card__name { font-size: 15px; color: #333; margin: 0 0 8px 0; line-height: 1.4; height: 42px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; cursor: pointer; transition: color .2s; }
.product-card__name:hover { color: #007bff; }
.product-card__price-container { display: flex; align-items: baseline; gap: 8px; margin: 0 0 10px 0; height: 24px; }
.discounted-price { font-size: 16px; font-weight: 600; color: #d9534f; }
.original-price { font-size: 14px; color: #999; text-decoration: line-through; }
.normal-price { font-size: 16px; font-weight: 600; color: #000; }
.product-card__colors { display: flex; gap: 6px; height: 14px; margin-top: 10px; }
.product-card__color-dot { width: 14px; height: 14px; border-radius: 50%; border: 1px solid #e0e0e0; cursor: pointer; transition: transform .2s; }
.product-card__color-dot:hover { transform: scale(1.2); }

/* Quick view */
:deep(.el-dialog.quick-view-dialog) { border-radius: 8px; background-color: #fff; }
:deep(.quick-view-dialog .el-dialog__header) { padding: 0; position: absolute; top: 15px; right: 15px; z-index: 1; }
:deep(.quick-view-dialog .el-dialog__headerbtn .el-icon) { font-size: 24px; color: #555; }
:deep(.quick-view-dialog .el-dialog__body) { padding: 40px; }
.quick-view__main-image { width: 100%; height: auto; aspect-ratio: 1/1; object-fit: cover; border-radius: 4px; }
.quick-view__info { display: flex; flex-direction: column; height: 100%; }
.quick-view__title { font-size: 28px; font-weight: 700; margin: 0 0 10px 0; line-height: 1.3; color: #333; }
.quick-view__color-display { font-size: 16px; margin-bottom: 15px; color: #555; height: 20px; font-weight: 500; }
.quick-view__color-selector { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.selector-label { font-size: 14px; font-weight: 600; margin: 0 0 10px 0; }
.color-thumbnail { cursor: pointer; border: 2px solid #e0e0e0; padding: 2px; transition: border-color .2s, transform .2s; width: 60px; height: 60px; border-radius: 4px; }
.color-thumbnail:hover { transform: translateY(-2px); }
.color-thumbnail.is-selected { border-color: #000; box-shadow: 0 0 0 2px rgba(0,0,0,.2); }
.color-thumbnail img { width: 100%; height: 100%; object-fit: cover; display: block; border-radius: 2px; }
.quick-view__size-selector { margin-bottom: 20px; }
.size-button { margin: 0 8px 8px 0 !important; border-radius: 5px; border: 1px solid #ccc; font-weight: 600; min-width: 50px; padding: 8px 15px; transition: background-color .2s, color .2s, border-color .2s; }
.size-button.is-selected { background-color: #000 !important; color: #fff !important; border-color: #000 !important; }
.quick-view__quantity-selector { margin-bottom: 25px; display: flex; align-items: center; gap: 15px; }
.stock-info { font-size: 14px; color: #999; margin-left: 0; }
.quick-view__actions { display: flex; gap: 10px; margin-top: 30px; }
.quick-view__actions .el-button { flex: 1; height: 48px; font-size: 16px; font-weight: 600; border-radius: 5px; border: 1px solid #000 !important; }
.add-to-cart-btn { background-color: #fff !important; color: #000 !important; }
.add-to-cart-btn:hover { background-color: #f5f5f5 !important; }
.buy-now-btn { background-color: #000 !important; color: #fff !important; }
.buy-now-btn:hover { background-color: #333 !important; }

/* Pagination */
.pagination-container { display: flex; justify-content: center; margin-top: 40px; }

/* Pre-order link hover */
.pre-order-text a:hover { opacity: .8; }
</style>

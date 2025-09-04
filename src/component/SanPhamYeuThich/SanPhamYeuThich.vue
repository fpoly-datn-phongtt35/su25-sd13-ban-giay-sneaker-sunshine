<template>
  <div class="product-collection-container">
    <!-- Header -->
    <div class="collection-header">
      <h2 class="collection-title">Sản phẩm yêu thích</h2>
    </div>

    <!-- States -->
    <div v-if="isLoading" class="loading-state">Đang tải sản phẩm yêu thích...</div>
    <div v-else-if="error" class="error-state">{{ error }}</div>

    <!-- List -->
    <el-main v-else class="product-list-main">
      <template v-if="products.length">
        <el-row :gutter="30">
          <el-col
            v-for="p in products"
            :key="p.id"
            :xs="24" :sm="12" :md="8" :lg="6"
          >
            <div class="product-card">
              <div class="product-card__image-wrapper">
                <span
                  v-if="Number(p.discountPercentage) > 0"
                  class="product-card__discount-badge"
                >
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
                  circle size="large"
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
                  <template v-if="Number(p.discountPercentage) > 0 && Number(p.discountedPrice) > 0">
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
              </div>
            </div>
          </el-col>
        </el-row>
      </template>

      <div v-else class="loading-state">
        Bạn chưa có sản phẩm yêu thích nào.
      </div>
    </el-main>

    <!-- QUICK VIEW -->
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
                <el-input-number v-model="quickViewQuantity" :min="minQty" :max="maxQty" :disabled="maxQty===0" />
                <span class="stock-info">(Còn lại: {{ selectedVariantStock }})</span>
              </template>
              <template v-else>
                <el-input-number :model-value="0" :min="0" :max="0" disabled />
                <span class="stock-info">Chọn size để xem tồn</span>
              </template>
            </div>

            <div class="quick-view__actions">
              <el-button
                class="add-to-cart-btn"
                :loading="isAdding"
                :disabled="isAdding"
                @click="handleAddToCartInModal"
              >
                Thêm vào giỏ
              </el-button>

              <el-button
                class="buy-now-btn"
                :loading="isAdding"
                :disabled="isAdding"
                @click="handleBuyNowInModal"
              >
                Mua ngay
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- CONTACT DIALOG (SL lớn) -->
    <el-dialog
      v-model="contactDialogVisible"
      title="Liên hệ nhân viên để đặt số lượng lớn"
      width="520px"
    >
      <div class="contact-block">
        <p>Đơn online không hỗ trợ <strong>Mua ngay/Thêm giỏ</strong> khi số lượng từ
          <strong>{{ NGAY_MAX_QTY }}</strong> đôi trở lên.</p>
        <p>Vui lòng liên hệ nhân viên để được hỗ trợ:</p>
        <ul class="contact-list">
          <li>Hotline: <a :href="`tel:${hotline}`">{{ hotline }}</a></li>
          <li>Zalo/WhatsApp: <a :href="zaloLink" target="_blank" rel="noopener">Chat ngay</a></li>
          <li>Facebook: <a :href="facebookLink" target="_blank" rel="noopener">Fanpage</a></li>
        </ul>
      </div>

      <template #footer>
        <el-button @click="contactDialogVisible = false">Đóng</el-button>
        <el-button type="primary" @click="contactDialogVisible = false">Đã hiểu</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import axios from 'axios'
import apiClient from '@/utils/axiosInstance'
import { addToCart } from '@/utils/cart.js'

/* ====== Config giống Collection ====== */
const NGAY_MAX_QTY = 10
const hotline = '0346771322'
const zaloLink = 'https://zalo.me/0346771322'
const facebookLink = 'https://www.facebook.com/phantuananh181205/'
const PLACEHOLDER_IMG = 'https://via.placeholder.com/400'

const router = useRouter()

/* ====== States ====== */
const isLoading = ref(true)
const error = ref(null)
const products = ref([])

const quickViewVisible = ref(false)
const selectedProduct = ref(null)
const quickViewSelectedColorId = ref(null)
const quickViewSelectedColor = ref(null)
const quickViewSelectedSize = ref(null)
const quickViewQuantity = ref(1)
const quickViewActiveImage = ref(PLACEHOLDER_IMG)

const isAdding = ref(false)
const contactDialogVisible = ref(false)

/* ====== Favorites storage ====== */
const customerId = localStorage.getItem('userId') || null
const favorites = ref([])

const loadFavorites = () => {
  try { favorites.value = JSON.parse(localStorage.getItem('favorites') || '[]') || [] }
  catch { favorites.value = [] }
}

/* ====== Helpers ====== */
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

/* ====== Fetch favorites ====== */
const fetchFavoriteProducts = async () => {
  isLoading.value = true
  error.value = null
  try {
    if (!customerId) {
      error.value = 'Bạn chưa đăng nhập. Vui lòng đăng nhập để xem sản phẩm yêu thích.'
      products.value = []
      return
    }
    const productIds = favorites.value
      .filter(f => String(f.customerId) === String(customerId))
      .map(f => Number(f.productId))
      .filter(Number.isFinite)

    if (!productIds.length) {
      products.value = []
      return
    }

    // dùng apiClient cho đồng nhất
    const { data } = await apiClient.post('/online-sale/favorites', { productIds })
    const list = Array.isArray(data) ? data : []
    products.value = list
      .filter(Boolean)
      .map(normalizeProduct)
  } catch (e) {
    console.error('Lỗi khi tải sản phẩm yêu thích:', e)
    error.value = 'Không thể tải sản phẩm yêu thích. Vui lòng thử lại sau.'
  } finally {
    isLoading.value = false
  }
}

/* ====== UI actions ====== */
const changeProductImage = (p, img) => { if (p && img) { p.__hoverImage = img; p.activeImage = img } }
const restoreProductImage = (p) => { if (p) p.activeImage = p.__originalImage || p.activeImage }
const goToDetail = (id) => router.push(`/product/${id}`)

/* ====== Quick View ====== */
const openQuickViewModal = (p) => {
  selectedProduct.value = p
  quickViewVisible.value = true
  quickViewQuantity.value = 1
  if (p.variants?.length) selectQuickViewColor(p.variants[0])
  else {
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
  if (availableSizesForQuickView.value.length) quickViewSelectedSize.value = availableSizesForQuickView.value[0]
}
const selectSize = (s) => { quickViewSelectedSize.value = s; quickViewQuantity.value = 1 }

const findSelectedProductDetail = () => {
  const sp = selectedProduct.value
  if (!sp || !Array.isArray(sp.productDetails)) return null
  if (sp.productDetails.length===1 && !sp.productDetails[0].colorId && !sp.productDetails[0].sizeName) return sp.productDetails[0]
  if (quickViewSelectedColorId.value && quickViewSelectedSize.value) {
    return sp.productDetails.find(d => d.colorId===quickViewSelectedColorId.value && d.sizeName===quickViewSelectedSize.value)
  }
  return null
}

const getStockForSize = (size) => {
  const sp = selectedProduct.value
  if (!sp) return 0
  const d = sp.productDetails?.find(x => x.colorId===quickViewSelectedColorId.value && x.sizeName===size)
  return Number(d?.quantity || 0)
}

const availableSizesForQuickView = computed(() => {
  const sp = selectedProduct.value
  if (!sp || quickViewSelectedColorId.value==null) return []
  const sizes = (sp.productDetails || []).filter(d => d.colorId===quickViewSelectedColorId.value).map(d => d.sizeName).filter(Boolean)
  // sắp xếp hợp lý (S/M/L/XL/XXL… hoặc số)
  const order = { XS:0, S:1, M:2, L:3, XL:4, XXL:5, XXXL:6 }
  return Array.from(new Set(sizes)).sort((a,b) => {
    const A = String(a).toUpperCase(), B = String(b).toUpperCase()
    const na = Number(A), nb = Number(B)
    if (Number.isFinite(na) && Number.isFinite(nb)) return na - nb
    if (Number.isFinite(na)) return -1
    if (Number.isFinite(nb)) return 1
    return (order[A] ?? 999) - (order[B] ?? 999) || A.localeCompare(B)
  })
})

const selectedVariantStock = computed(() => {
  const d = findSelectedProductDetail()
  return d ? Number(d.quantity || 0) : 0
})
const needSize = computed(() => {
  const sp = selectedProduct.value
  if (!sp) return false
  const named = new Set((sp.productDetails || []).map(d => d.sizeName).filter(Boolean))
  return named.size > 0
})
const minQty = computed(() => (selectedVariantStock.value > 0 ? 1 : 0))
const maxQty = computed(() => (selectedVariantStock.value > 0 ? selectedVariantStock.value : 0))

/* ====== Cart / BuyNow logic (y hệt Collection) ====== */
const requiresSizeSelection = (sp) => {
  const named = new Set((sp.productDetails || []).map(d => d.sizeName).filter(Boolean))
  return named.size > 0
}
const showBulkDialog = () => {
  contactDialogVisible.value = true
  ElMessage.warning(`Số lượng lớn (≥ ${NGAY_MAX_QTY} đôi). Vui lòng liên hệ nhân viên để đặt hàng.`)
}

const handleAddToCartInModal = async () => {
  if (isAdding.value) return false
  isAdding.value = true

  try {
    const sp = selectedProduct.value
    if (!sp) { ElMessage.error('Sản phẩm không hợp lệ.'); return false }

    if (Number(quickViewQuantity.value) >= NGAY_MAX_QTY) {
      showBulkDialog()
      return false
    }
    if (requiresSizeSelection(sp) && !quickViewSelectedSize.value) {
      ElMessage.warning('Vui lòng chọn kích thước sản phẩm trước khi thêm vào giỏ hàng.')
      return false
    }

    const detail = findSelectedProductDetail()
    if (!detail) {
      ElMessage.error('Không tìm thấy chi tiết phù hợp.')
      return false
    }

    if (quickViewQuantity.value <= 0) {
      ElMessage.warning('Số lượng phải lớn hơn 0.')
      return false
    }

    // Verify trạng thái + tồn kho trên BE (giống Collection)
    try {
      const productDetailId = detail.id
      const res = await axios.get(`http://localhost:8080/api/online-sale/verify-pdDetail/${productDetailId}`)
      let data = res?.data
      if (Array.isArray(data) && data.length > 0) data = data[0]

      const statusVal = data?.status ?? data?.active ?? data?.isAvailable ?? data?.enabled
      const qtyVal = Number(data?.quantity ?? detail.quantity ?? 0)

      let okStatus = false
      if (typeof statusVal === 'boolean') okStatus = statusVal === true
      else {
        const n = Number(statusVal)
        if (Number.isFinite(n)) okStatus = n === 1
        else if (typeof statusVal === 'string') okStatus = ['active','enabled','available','true'].includes(statusVal.trim().toLowerCase())
      }
      if (!okStatus) {
        ElMessage.error('Sản phẩm hiện không hợp lệ để mua (đã bị vô hiệu hoặc ngừng bán).')
        return false
      }

      if (!Number.isFinite(qtyVal) || qtyVal <= 0) {
        ElMessage.warning(`Tồn kho không đủ (còn ${qtyVal}).`)
        return false
      }
      if (quickViewQuantity.value > qtyVal) {
        ElMessage.warning(`Số lượng yêu cầu (${quickViewQuantity.value}) vượt quá tồn kho (${qtyVal}).`)
        return false
      }
    } catch (verifyErr) {
      console.error('Lỗi verify productDetail:', verifyErr)
      ElMessage.error('Không thể kiểm tra trạng thái sản phẩm. Vui lòng thử lại sau.')
      return false
    }

    // Pass verify -> addToCart
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
      status: detail.status
    }
    addToCart(item)
    ElMessage.success('Đã thêm vào giỏ hàng!')
    quickViewVisible.value = false
    return true
  } finally {
    isAdding.value = false
  }
}

const handleBuyNowInModal = async () => {
  if (isAdding.value) return
  if (Number(quickViewQuantity.value) >= NGAY_MAX_QTY) {
    showBulkDialog()
    return
  }
  const ok = await handleAddToCartInModal()
  if (ok) router.push('/cart')
}

/* ====== Lifecycle ====== */
onMounted(async () => {
  loadFavorites()
  await fetchFavoriteProducts()
})
</script>

<style scoped>
/* Giữ style đồng bộ với Collection để look & feel nhất quán */
.product-collection-container{
  max-width: 1360px;
  margin: 48px auto 60px;
  padding: 0 16px;
}
.collection-header{ display:flex; align-items:center; justify-content:center; margin-bottom: 18px; }
.collection-title{ font-size: 28px; line-height:1.2; font-weight:600; letter-spacing:.2px; }
.loading-state,.error-state{ text-align:center; padding: 20px; font-size:16px; color:#555; }
.product-list-main{ max-width:100%; padding:0; }

.product-card{
  margin-bottom: 20px;
  background:#fff; border:1px solid #e9e9ef; border-radius:14px;
  box-shadow: 0 2px 10px rgba(0,0,0,.06);
  transition: transform .18s ease, box-shadow .18s ease, border-color .2s ease;
}
.product-card:hover{ transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,.08); }

.product-card__image-wrapper{
  position:relative; width:100%; aspect-ratio:1/1;
  background:#f7f8fb; overflow:hidden; cursor:pointer;
}
.product-card__image{
  position:absolute; inset:0; width:100%; height:100%; object-fit:cover;
  transition: transform .28s ease;
}
.product-card:hover .product-card__image{ transform: scale(1.035); }

.product-card__discount-badge{
  position:absolute; top:10px; left:10px;
  background-color:#ff0000; color:#fff; border:0;
  padding:4px 8px; border-radius:999px; font-size:12px; font-weight:600; z-index:2;
}

.product-card__quick-view-btn{
  position:absolute; top:12px; right:12px; z-index:2;
  background:rgba(255,255,255,.96)!important;
  border:1px solid #f0f0f5 !important;
  box-shadow: 0 2px 10px rgba(0,0,0,.06);
  opacity:0; transition: opacity .22s ease;
}
.product-card:hover .product-card__quick-view-btn{ opacity:1; }

.product-card__info{ padding:14px 14px 16px; text-align:left; }
.product-card__name{
  font-size:15px; font-weight:600; color:#17181c;
  margin:0 0 6px 0; line-height:1.35; height:42px; overflow:hidden;
  text-overflow:ellipsis; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical;
  cursor:pointer; transition:color .15s ease;
}
.product-card__name:hover{ color:#0ea5e9; }

.product-card__price-container{ display:flex; align-items:baseline; gap:10px; height:24px; margin-bottom:8px; }
.discounted-price{ font-size:16px; font-weight:600; color:#ff0000; }
.original-price{ font-size:13px; color:#9aa0b4; text-decoration:line-through; font-weight:400; }
.normal-price{ font-size:16px; font-weight:600; color:#17181c; }

.product-card__colors{ display:flex; gap:7px; height:16px; margin-top:10px; }
.product-card__color-dot{
  width:16px; height:16px; border-radius:50%;
  border:1px solid #e6e8f0; cursor:pointer;
  transition: transform .15s ease, box-shadow .15s ease;
}
.product-card__color-dot:hover{ transform: translateY(-1px) scale(1.06); box-shadow:0 2px 6px rgba(0,0,0,.12); }

/* Dialog */
:deep(.el-dialog.quick-view-dialog){ border-radius:16px; background:#fff; box-shadow: 0 8px 24px rgba(0,0,0,.08); }
:deep(.quick-view-dialog .el-dialog__header){ padding:0; position:absolute; top:14px; right:14px; z-index:1; }
:deep(.quick-view-dialog .el-dialog__headerbtn .el-icon){ font-size:22px; color:#6b7280; }
:deep(.quick-view-dialog .el-dialog__body){ padding:32px 34px; }

.quick-view__main-image{
  width:100%; height:auto; aspect-ratio:1/1; object-fit:cover;
  border-radius:12px; border:1px solid #eceef6;
}
.quick-view__info{ display:flex; flex-direction:column; height:100%; }
.quick-view__title{ font-size:24px; font-weight:700; margin:0 0 8px; color:#17181c; }
.quick-view__color-display{ font-size:14px; margin-bottom:10px; color:#4d5160; height:20px; font-weight:600; }

.quick-view__color-selector{ display:flex; flex-wrap:wrap; gap:10px; margin-bottom:16px; }
.selector-label{ font-size:13px; font-weight:600; margin:0 0 8px; color:#4d5160; text-transform:uppercase; letter-spacing:.4px; }

.color-thumbnail{
  cursor:pointer; border:2px solid #eceef6; padding:2px;
  width:58px; height:58px; border-radius:10px; background:#fff;
  transition: border-color .18s ease, transform .18s ease;
}
.color-thumbnail:hover{ transform: translateY(-2px); }
.color-thumbnail.is-selected{ border-color:#111; box-shadow:0 0 0 3px rgba(0,0,0,.06); }
.color-thumbnail img{ width:100%; height:100%; object-fit:cover; display:block; border-radius:8px; }

.quick-view__size-selector{ margin-bottom:16px; }
.size-button{
  margin:0 8px 8px 0 !important; border-radius:10px;
  border:1px solid #dfe3ec; font-weight:600; min-width:50px; padding:9px 14px;
  transition: background-color .18s ease, color .18s ease, border-color .18s ease;
}
.size-button.is-selected{ background:#000 !important; color:#fff !important; border-color:#000 !important; }

.quick-view__quantity-selector{ margin-bottom:18px; display:flex; align-items:center; gap:12px; }
.stock-info{ font-size:13px; color:#8b90a3; }
.quick-view__actions{ display:flex; gap:10px; margin-top:18px; }
.quick-view__actions :deep(.el-button){
  flex:1; height:46px; font-size:15px; font-weight:600; border-radius:12px;
  border:1px solid #111 !important;
}
.add-to-cart-btn{ background:#fff !important; color:#111 !important; }
.add-to-cart-btn:hover{ background:#f6f7fb !important; }
.buy-now-btn{ background:#111 !important; color:#fff !important; }
.buy-now-btn:hover{ background:#222 !important; }

/* Contact dialog */
.contact-block { font-size:14px; color:#17181c; }
.contact-list { margin:10px 0 0; padding-left:18px; }
.contact-list li { margin:4px 0; }
.contact-list a { color: #0ea5e9; text-decoration: none; }
.contact-list a:hover { text-decoration: underline; }
</style>

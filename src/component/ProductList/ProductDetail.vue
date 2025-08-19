<template>
  <div class="pd-wrapper" :class="{ 'has-variant-selected': !!selectedDetail }">
    <!-- Breadcrumb -->
    <div class="pd-breadcrumb">
      <RouterLink to="/">Trang chủ</RouterLink>
      <span class="sep">/</span>
      <RouterLink
        v-if="brandId && brandName"
        :to="{ path: '/collections', query: { brandId: String(brandId), brandName } }"
        :title="`Xem tất cả của ${brandName}`"
      >
        {{ brandName }}
      </RouterLink>
      <span v-else>Thương hiệu</span>
      <span class="sep">/</span>
      <span class="current">{{ product.productName || 'Sản phẩm' }}</span>
    </div>

    <el-row :gutter="24" class="product-detail">
      <!-- Thumbnails -->
      <el-col :span="3" class="pd-thumbs-col">
        <div class="thumbnail-list">
          <img
            v-for="(img, index) in colorSpecificImages"
            :key="img.id || index"
            :src="imgSrc(img)"
            class="thumbnail"
            :class="{ active: index === currentImageIndex }"
            @click="setMainImageByIndex(index)"
            loading="lazy"
            decoding="async"
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
            aria-label="Ảnh trước"
          >&lt;</el-button>
          <el-button
            v-if="colorSpecificImages.length > 1"
            class="nav-button next"
            @click="nextImage"
            circle
            aria-label="Ảnh sau"
          >&gt;</el-button>
        </div>
      </el-col>

      <!-- Thông tin sản phẩm -->
      <el-col :span="12">
        <div class="pd-head">
          <h1 class="product-name">{{ product.productName }}</h1>

          <!-- Badge thương hiệu -->
          <RouterLink
            v-if="brandId && brandName"
            class="brand-badge"
            :to="{ path: '/collections', query: { brandId: String(brandId), brandName } }"
          >
            {{ brandName }}
          </RouterLink>
        </div>

        <p class="product-code">Style Code: {{ product.productCode }}</p>

        <!-- GIÁ THEO BIẾN THỂ (ưu tiên) -->
        <div class="product-price variant" v-if="selectedDetail">
          <template v-if="Number(displayPrice.original) > 0">
            <span class="original-price">{{ money(displayPrice.original) }}</span>
            <span class="discounted-price">{{ money(displayPrice.discounted) }}</span>
          </template>
          <template v-else>
            <span class="discounted-price">{{ money(displayPrice.discounted) }}</span>
          </template>
        </div>

        <!-- GIÁ MẶC ĐỊNH (fallback) -->
        <div class="product-price">
          <template v-if="Number(product.discountedPrice) > 0 && product.discountedPrice < product.sellPrice">
            <span class="original-price">{{ money(product.sellPrice) }}</span>
            <span class="discounted-price">{{ money(product.discountedPrice) }}</span>
          </template>
          <template v-else>
            <span class="discounted-price">{{ money(product.sellPrice) }}</span>
          </template>
        </div>

        <!-- Màu -->
        <div class="color-selector" v-if="uniqueColors.length">
          <div class="selector-title">Màu sắc</div>
          <div class="color-swatches">
            <div
              v-for="c in uniqueColors"
              :key="c.id"
              class="color-swatch"
              :style="{ backgroundColor: (c.name || '').toLowerCase() }"
              :class="{ selected: selectedColor === c.name }"
              @click="selectColor(c)"
              :title="c.name"
            />
          </div>
        </div>

        <!-- Size -->
        <div class="size-selector" v-if="availableSizes.length">
          <div class="size-header">
            <div class="selector-title">Kích thước</div>
            <a href="#" class="size-guide-link" @click.prevent="isSizeGuideVisible = true">
              Hướng dẫn kích thước
            </a>
          </div>

          <el-dialog
            v-model="isSizeGuideVisible"
            width="900px"
            title="Hướng dẫn chọn kích thước"
            :close-on-click-modal="false"
          >
            <img
              src="https://file.hstatic.net/1000284478/file/mlb_new_unisex_-_chunky_-_desktop_e90f62fd3ddf4139bd3c0a3cae52ebd5.jpg"
              alt="Bảng size" style="width:100%"
            />
          </el-dialog>

          <div class="size-buttons">
            <el-button
              v-for="s in availableSizes"
              :key="s"
              :class="{ selected: selectedSize === s }"
              @click="selectSize(s)"
            >{{ s }}</el-button>
          </div>
        </div>

        <!-- Số lượng -->
        <div class="qty-row" v-if="selectedDetail">
          <span class="stock" :class="{ low: Number(selectedDetail.quantity) <= 5 }">
            Còn {{ selectedDetail.quantity }} sản phẩm
          </span>
          <div class="qty-actions">
            <el-input-number v-model="quantity" :min="1" :max="Number(selectedDetail.quantity) || 1" size="small" />
          </div>
        </div>

        <!-- Actions -->
        <div class="action-buttons">
          <el-button class="add-to-cart-btn" @click="handleAddToCart" :disabled="!selectedDetail">THÊM VÀO GIỎ</el-button>
          <el-button class="buy-now-btn" @click="handleBuyNow" :disabled="!selectedDetail">MUA NGAY</el-button>
        </div>

        <!-- Khuyến mãi -->
        <div class="promotions-section">
          <div class="promotion-item">
            <p class="promotion-title"><span class="dot">●</span> SunShine Chào bạn mới</p>
            <p class="promotion-text">Ưu đãi 10% cho đơn nguyên giá đầu tiên*</p>
            <p class="promotion-text">Nhập mã: MLBWELCOME</p>
            <p class="promotion-text">Không áp dụng cùng CTKM khác</p>
          </div>
          <div class="promotion-item">
            <p class="promotion-title"><span class="dot">●</span> BLACK TUESDAY REWARDS</p>
            <p class="promotion-text">Hoàn 10% điểm Loyalty mỗi Thứ 3</p>
            <p class="promotion-text">Áp dụng từ: 01/04/2025</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Liên quan -->
    <div class="related-wrapper" v-if="related.items.length">
      <div class="related-header">
        <div class="related-title">
          <h3>Sản phẩm liên quan</h3>
          <span v-if="brandName" class="related-brand">thuộc <strong>{{ brandName }}</strong></span>
        </div>
        <RouterLink
          v-if="brandId && brandName"
          :to="{ path: '/collections', query: { brandId: String(brandId), brandName } }"
          class="see-all-link"
        >
          Xem tất cả {{ brandName }}
        </RouterLink>
      </div>

      <el-row :gutter="20">
        <el-col
          v-for="rp in related.items"
          :key="rp.id"
          :xs="12" :sm="8" :md="6" :lg="6"
        >
          <div class="related-card" @click="goToDetail(rp.id)">
            <div class="related-image">
              <img :src="rp.activeImage" :alt="rp.productName" loading="lazy" decoding="async" />
              <span v-if="rp.discountPercentage>0" class="badge">-{{ rp.discountPercentage }}%</span>
            </div>
            <div class="related-info">
              <p class="name" :title="rp.productName">{{ rp.productName }}</p>
              <div class="price">
                <template v-if="Number(rp.discountedPrice)>0">
                  <span class="new">{{ money(rp.discountedPrice) }}</span>
                  <del class="old">{{ money(rp.sellPrice) }}</del>
                </template>
                <template v-else>
                  <span class="new">{{ money(rp.sellPrice) }}</span>
                </template>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { addToCart } from '@/utils/cart'

/* API base */
const API = axios.create({ baseURL: 'http://localhost:8080/api' })

/* Router */
const route = useRoute()
const router = useRouter()

/* State */
const product = ref({
  id: null,
  productName: 'Đang tải…',
  productCode: '…',
  sellPrice: 0,
  discountedPrice: 0,
  brandId: null,
  brandName: '',
  brand: null,
  productDetails: [],
})
const colorSpecificImages = ref([])
const currentImageIndex = ref(0)
const isLoadingImages = ref(false)
const selectedColor = ref(null)
const selectedSize = ref(null)
const quantity = ref(1)
const isSizeGuideVisible = ref(false)

/* Helpers */
const money = (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(n || 0))
const imgSrc = (img) => (img?.image ? `data:image/jpeg;base64,${img.image}` : (img?.url || '/no-image.jpg'))

/* Brand */
const brandId = computed(() => product.value?.brandId ?? product.value?.brand?.id ?? null)
const brandName = computed(() =>
  product.value?.brandName
  ?? product.value?.brand?.brandName
  ?? product.value?.brand?.name
  ?? ''
)

/* Ảnh chính */
const mainImage = computed(() => {
  if (isLoadingImages.value) return '/loading-placeholder.gif'
  if (!colorSpecificImages.value.length) return '/no-image.jpg'
  return imgSrc(colorSpecificImages.value[currentImageIndex.value])
})

/* Màu & size */
const uniqueColors = computed(() => {
  const m = new Map()
  ;(product.value.productDetails || []).forEach(d => {
    if (d?.colorId && !m.has(d.colorId)) m.set(d.colorId, { id: d.colorId, name: d.colorName })
  })
  return Array.from(m.values())
})

const availableSizes = computed(() => {
  if (!selectedColor.value) return []
  return Array.from(
    new Set(
      (product.value.productDetails || [])
        .filter(d => d.colorName === selectedColor.value)
        .map(d => d.sizeName)
    )
  ).sort((a, b) => {
    const na = Number(a), nb = Number(b)
    if (Number.isFinite(na) && Number.isFinite(nb)) return na - nb
    return String(a).localeCompare(String(b))
  })
})

/* Biến thể & giá hiển thị */
const findSelectedDetail = () =>
  (product.value.productDetails || []).find(d => d.colorName === selectedColor.value && d.sizeName === selectedSize.value)

const selectedDetail = computed(() => findSelectedDetail() || null)

const displayPrice = computed(() => {
  if (selectedDetail.value) {
    const d = selectedDetail.value
    if (Number(d.discountedPrice) > 0 && d.discountedPrice < d.sellPrice) {
      return { original: d.sellPrice, discounted: d.discountedPrice }
    }
    return { original: null, discounted: d.sellPrice }
  }
  // fallback khi chưa chọn biến thể
  if (Number(product.value.discountedPrice) > 0 && product.value.discountedPrice < product.value.sellPrice) {
    return { original: product.value.sellPrice, discounted: product.value.discountedPrice }
  }
  return { original: null, discounted: product.value.sellPrice }
})

/* Liên quan */
const related = ref({ items: [], pageSize: 8 })
const normalizeProduct = (p) => {
  const images = Array.isArray(p.productImages) ? p.productImages : []
  const details = Array.isArray(p.productDetails) ? p.productDetails : []
  const firstImg = images[0]?.url || (images[0]?.image ? `data:image/jpeg;base64,${images[0].image}` : null)
  const seen = new Set()
  const variants = []
  for (const d of details) {
    if (!d?.colorId || seen.has(d.colorId)) continue
    seen.add(d.colorId)
    const im = images.find(i => i.colorId === d.colorId)
    variants.push({
      colorId: d.colorId,
      colorName: d.colorName,
      image: im?.url || (im?.image ? `data:image/jpeg;base64,${im.image}` : null)
    })
  }
  return { ...p, variants, activeImage: variants[0]?.image || firstImg || '/no-image.jpg' }
}

/* Methods */
const fetchProduct = async () => {
  const id = route.params.id
  const { data } = await API.get(`/online-sale/${id}`)
  product.value = data
}

const fetchImagesForColor = async (colorId) => {
  if (!colorId) return
  isLoadingImages.value = true
  try {
    const productId = route.params.id
    const { data } = await API.get(`/admin/product-images`, { params: { productId, colorId } })
    colorSpecificImages.value = Array.isArray(data) ? data : []
    currentImageIndex.value = 0
  } finally {
    isLoadingImages.value = false
  }
}

const selectColor = (c) => {
  selectedColor.value = c.name
  selectedSize.value = null
  quantity.value = 1
  fetchImagesForColor(c.id)
}
const selectSize = (s) => { selectedSize.value = s; quantity.value = 1 }

const setMainImageByIndex = (i) => { currentImageIndex.value = i }
const nextImage = () => { if (colorSpecificImages.value.length) currentImageIndex.value = (currentImageIndex.value + 1) % colorSpecificImages.value.length }
const prevImage = () => { if (colorSpecificImages.value.length) currentImageIndex.value = (currentImageIndex.value - 1 + colorSpecificImages.value.length) % colorSpecificImages.value.length }

const handleAddToCart = () => {
  if (!selectedColor.value || !selectedSize.value) {
    return ElMessage.warning('Vui lòng chọn màu & kích thước!')
  }

  const d = findSelectedDetail()
  if (!d) return ElMessage.error('Không tìm thấy biến thể phù hợp!')
  if (quantity.value > Number(d.quantity || 0)) {
    return ElMessage.warning(`Chỉ còn ${d.quantity} sản phẩm.`)
  }

  const price = Number(d.discountedPrice) > 0 ? d.discountedPrice : (d.sellPrice || product.value.sellPrice)

  const cartItem = {
    productDetailId: d.id,
    productId: product.value.id,
    productName: product.value.productName,
    productCode: product.value.productCode,
    image: mainImage.value,
    color: selectedColor.value,
    size: selectedSize.value,
    price,
    quantity: quantity.value,
    discountCampaignId: d.discountCampaignId || null,
  }

  // Thêm giỏ
  addToCart(cartItem)

  // Log ra console để kiểm tra
  console.log('✅ Đã thêm vào giỏ:', cartItem)

  // Hoặc log chi tiết từng field
  console.table(cartItem)

  ElMessage.success('Đã thêm vào giỏ hàng!')
}

const handleBuyNow = () => {
  handleAddToCart()
  if (selectedDetail.value) router.push('/cart')
}

const fetchRelated = async () => {
  related.value.items = []
  const id = route.params.id

  // Ưu tiên: endpoint liên quan mới
  try {
    const { data } = await API.get(`/online-sale/${id}/related`, { params: { limit: related.value.pageSize } })
    const list = Array.isArray(data) ? data : (Array.isArray(data?.content) ? data.content : [])
    related.value.items = list.map(normalizeProduct)
    if (related.value.items.length) return
  } catch (_) {}

  // Fallback: theo brand
  if (!brandId.value) return
  try {
    const { data } = await API.get(`/admin/brand/${brandId.value}/products`, { params: { page: 0, size: related.value.pageSize + 1 } })
    const payload = data ?? {}
    const list = Array.isArray(payload.content) ? payload.content
               : Array.isArray(payload.data?.content) ? payload.data.content
               : Array.isArray(payload) ? payload : []
    related.value.items = list
      .filter(p => p && p.id !== product.value.id)
      .slice(0, related.value.pageSize)
      .map(normalizeProduct)
  } catch (_) {}
}

const goToDetail = (id) => router.push(`/product/${id}`)

/* Lifecycle */
const initPage = async () => {
  await fetchProduct()
  // Auto chọn màu/size đầu tiên
  const colors = uniqueColors.value
  if (colors.length) {
    selectColor(colors[0])
    const sizes = availableSizes.value
    if (sizes.length) selectSize(sizes[0])
  }
  await fetchRelated()
}

onMounted(initPage)
watch(() => route.params.id, () => {
  selectedColor.value = null
  selectedSize.value = null
  colorSpecificImages.value = []
  currentImageIndex.value = 0
  quantity.value = 1
  initPage()
})
watch(brandId, fetchRelated)
</script>

<style scoped>
/* Wrapper & breadcrumb */
.pd-wrapper { max-width: 1400px; margin: 20px auto 60px; padding: 0 20px; }
.pd-breadcrumb { display:flex; align-items:center; gap:8px; font-size:13px; color:#666; margin-bottom: 12px; }
.pd-breadcrumb a { color:#666; text-decoration: none; }
.pd-breadcrumb a:hover { color:#000; text-decoration: underline; }
.pd-breadcrumb .sep { opacity:.6; }
.pd-breadcrumb .current { color:#222; }

/* Layout */
.product-detail { font-family: 'Helvetica Neue', Arial, sans-serif; color: #333; line-height: 1.45; }

/* Thumbnails */
.pd-thumbs-col { padding-right: 6px; }
.thumbnail-list { display: flex; flex-direction: column; gap: 8px; }
.thumbnail {
  width: 100%; aspect-ratio: 1/1; object-fit: cover; display: block;
  border: 1px solid #e6e6e6; border-radius: 6px; cursor: pointer; transition: .2s;
  background: #fff;
}
.thumbnail:hover { border-color: #bbb; transform: translateY(-1px); box-shadow: 0 4px 10px rgba(0,0,0,.06); }
.thumbnail.active { border: 2px solid #111; box-shadow: 0 6px 16px rgba(0,0,0,.08); }

/* Ảnh chính */
.main-image-container { position: relative; border-radius: 10px; overflow: hidden; background:#fafafa; box-shadow: 0 4px 14px rgba(0,0,0,.06); }
.main-image-container img { width: 100%; height: auto; display: block; }
.nav-button { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(255,255,255,.95); border: 1px solid rgba(0,0,0,.08); box-shadow: 0 2px 10px rgba(0,0,0,.08); width: 38px; height: 38px; font-size: 18px; color: #555; }
.nav-button.prev { left: 10px; } .nav-button.next { right: 10px; }
.nav-button:hover { background:#111; color:#fff; }

/* Head + Brand badge */
.pd-head { display:flex; align-items:center; gap: 10px; flex-wrap: wrap; }
.product-name { font-size: 28px; font-weight: 800; margin: 0; color:#222; letter-spacing:.2px; }
.brand-badge {
  background:#111; color:#fff; border-radius: 999px; padding: 6px 12px;
  font-size: 12px; text-transform: uppercase; letter-spacing: .6px;
  border: 1px solid #111; transition: .2s;
}
.brand-badge:hover { background:#fff; color:#111; }

/* Code & giá */
.product-code { color:#777; font-size:14px; margin: 6px 0 16px; }
.product-price { font-size:26px; font-weight:700; margin-bottom: 18px; display:flex; gap:12px; align-items:baseline; }
.original-price { text-decoration: line-through; color:#9e9e9e; font-size:20px; }
.discounted-price { color:#d32f2f; font-size:28px; }

/* Ẩn giá mặc định khi đã chọn biến thể */
.has-variant-selected .product-price:not(.variant) { display: none; }

/* Màu/Size */
.selector-title { font-weight: 600; margin-bottom: 8px; }
.color-selector { margin: 14px 0; }
.color-swatches { display: flex; gap: 8px; flex-wrap: wrap; }
.color-swatch {
  width: 30px; height: 30px; border-radius: 50%; border: 1px solid #dedede; cursor: pointer; transition: .15s;
  box-shadow: 0 1px 2px rgba(0,0,0,.06);
}
.color-swatch:hover { transform: scale(1.05); border-color:#bbb; }
.color-swatch.selected { border: 2px solid #000; box-shadow: 0 0 0 2px #fff, 0 0 0 4px #000; }

.size-selector { margin: 14px 0; }
.size-header { display: flex; justify-content: space-between; align-items: center; }
.size-guide-link { color: #409eff; font-size: 13px; }
.size-buttons { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
.size-buttons .el-button { min-width: 52px; height: 38px; padding: 0 14px; font-size: 14px; border-radius: 8px; }

/* Size đang chọn -> nền đen, chữ trắng */
.size-buttons .el-button.selected,
.size-buttons .el-button.selected:hover,
.size-buttons .el-button.selected:focus {
  background: #111 !important;
  color: #fff !important;
  border-color: #111 !important;
}

/* Số lượng */
.qty-row { display: flex; justify-content: space-between; align-items: center; margin: 12px 0 4px; }
.stock { font-size: 13px; color: #16a34a; }
.stock.low { color: #d97706; }

/* Actions */
.action-buttons { display:flex; gap:12px; margin:18px 0 22px; width:100%; }
.action-buttons .el-button { flex:1; height:52px; font-size:16px; font-weight:800; border-radius:10px; text-transform:uppercase; letter-spacing:.5px; }
.add-to-cart-btn { background:#fff; color:#111; border:2px solid #111; }
.add-to-cart-btn:hover { background:#111; color:#fff; }
.buy-now-btn { background:#b81c23; color:#fff; border:2px solid #b81c23; }
.buy-now-btn:hover { background:#d02a31; }

/* Liên quan */
.related-wrapper { margin-top: 28px; }
.related-header { display:flex; justify-content:space-between; align-items:end; margin-bottom: 14px; }
.related-title { display:flex; align-items:baseline; gap:10px; }
.related-header h3 { font-size:22px; margin:0; }
.related-brand { font-size:14px; color:#666; }
.see-all-link {
  font-size:13px; padding:8px 14px; border:1px solid #ddd; border-radius:999px; text-decoration:none; color:#111; background:#fff;
}
.related-card { background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,.06); cursor:pointer; transition: .18s; }
.related-card:hover { transform: translateY(-4px); box-shadow:0 8px 24px rgba(0,0,0,.1); }
.related-image { position:relative; width:100%; padding-bottom:100%; background:#f5f5f5; overflow:hidden; }
.related-image img { position:absolute; inset:0; width:100%; height:100%; object-fit:cover; }
.related-image .badge { position:absolute; top:8px; left:8px; background:#d9534f; color:#fff; font-size:12px; padding:3px 6px; border-radius:4px; }
.related-info { padding:12px; }
.related-info .name { font-size:14px; line-height:1.4; height:40px; overflow:hidden; margin:0 0 6px; }
.related-info .price { display:flex; gap:8px; align-items: baseline; }
.related-info .price .new { font-weight:800; }
.related-info .price .old { color:#999; }

/* Responsive */
@media (max-width: 992px) { .product-name { font-size: 24px; } }
@media (max-width: 768px) { .pd-thumbs-col { display:none; } }
</style>

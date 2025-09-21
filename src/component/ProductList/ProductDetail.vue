<template>
  <div class="pd-wrapper" :class="{ 'has-variant-selected': !!selectedDetail }">
    <!-- ===== Breadcrumb ===== -->
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
      <!-- ===== Thumbnails ===== -->
      <el-col :span="3" class="pd-thumbs-col">
        <div class="thumbnail-list">
          <img
            v-for="(img, index) in colorSpecificImages"
            :key="img.id || index"
            :src="imgSrc(img)"
            class="thumbnail"
            :class="{ active: index === currentImageIndex }"
            @click="setMainImageByIndex(index)"
            alt="Ảnh nhỏ"
            loading="lazy"
            decoding="async"
          />
        </div>
      </el-col>

      <!-- ===== Ảnh chính ===== -->
      <el-col :span="9">
        <div class="main-image-container">
          <img :src="mainImage" alt="Ảnh chính" class="main-image" />
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

      <!-- ===== Thông tin sản phẩm ===== -->
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

        <!-- ===== Giá ===== -->
        <div v-if="selectedDetail" class="product-price variant">
          <template v-if="Number(displayPrice.discounted) > 0 && Number(displayPrice.discounted) < Number(displayPrice.original)">
            <span class="original-price">{{ money(displayPrice.original) }}</span>
            <span class="discounted-price">{{ money(displayPrice.discounted) }}</span>
          </template>
          <template v-else>
            <span class="only-price">{{ money(displayPrice.original || displayPrice.discounted) }}</span>
          </template>
        </div>

        <div v-else class="product-price">
          <template v-if="Number(product.discountedPrice) > 0 && Number(product.discountedPrice) < Number(product.sellPrice)">
            <span class="original-price">{{ money(product.sellPrice) }}</span>
            <span class="discounted-price">{{ money(product.discountedPrice) }}</span>
          </template>
          <template v-else>
            <span class="only-price">{{ money(product.sellPrice) }}</span>
          </template>
        </div>

        <!-- ===== Màu ===== -->
        <div class="color-selector" v-if="uniqueColors.length">
          <div class="selector-title">Màu sắc</div>
          <div class="color-swatches">
            <div
              v-for="c in uniqueColors"
              :key="c.id"
              class="color-swatch"
              :class="{
                selected: selectedColor === c.name,
                'is-light': isLightHex(c.name)
              }"
              :style="swatchStyle(c.name)"
              @click="selectColor(c)"
              :title="c.name"
            />
          </div>
        </div>

        <!-- ===== Size ===== -->
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

        <!-- ===== Số lượng & tồn ===== -->
        <div class="qty-row" v-if="selectedDetail">
          <span class="stock" :class="{ low: Number(selectedDetail.quantity) <= 5 }">
            Còn {{ selectedDetail.quantity }} sản phẩm
          </span>
          <div class="qty-actions">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="Number(selectedDetail.quantity) || 1"
              size="small"
            />
          </div>
        </div>

        <!-- ===== Action buttons ===== -->
        <div class="action-buttons">
          <el-button
            class="add-to-cart-btn"
            @click="handleAddToCart"
            :disabled="!selectedDetail"
          >
            THÊM VÀO GIỎ
          </el-button>
          <el-button
            class="buy-now-btn"
            @click="handleBuyNow"
            :disabled="!selectedDetail"
          >
            MUA NGAY
          </el-button>
        </div>

        <!-- ===== Ghi chú / mô tả ===== -->
        <div class="promotions-section">
          <div class="promotion-item">
            <p class="promotion-title"><span class="dot">●</span> Ghi chú sản phẩm</p>
            <p class="promotion-text">{{ product.description || 'Không có ghi chú' }}</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== Liên quan ===== -->
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
              <img :src="rp.activeImage" :alt="rp.productName" class="related-img" loading="lazy" decoding="async" />
              <span
                v-if="hasDiscount(rp)"
                class="badge"
              >
                -{{ discountPercent(rp) }}%
              </span>
            </div>
            <div class="related-info">
              <p class="name" :title="rp.productName">{{ rp.productName }}</p>
              <div class="price">
                <template v-if="hasDiscount(rp)">
                  <span class="new">{{ money(bestPrice(rp).discounted) }}</span>
                  <del class="old">{{ money(bestPrice(rp).sell) }}</del>
                </template>
                <template v-else>
                  <span class="new">{{ money(bestPrice(rp).sell) }}</span>
                </template>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- ===== CONTACT STAFF (SL ≥ NGAY_MAX_QTY) ===== -->
    <el-dialog
      v-model="contactDialogVisible"
      title="Liên hệ nhân viên để đặt số lượng lớn"
      width="520px"
    >
      <div class="contact-block">
        <p>Đơn online không hỗ trợ đặt <strong>số lượng từ {{ NGAY_MAX_QTY }}</strong> đôi trở lên.</p>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addToCart } from '@/utils/cart'
import apiClient from '@/utils/axiosInstance'
import axios from 'axios'

/* ========== Cấu hình giới hạn & liên hệ ========== */
const NGAY_MAX_QTY = 10 // ngưỡng chặn mua/giỏ
const hotline = '09xx xxx xxx'                 // TODO: thay số thật
const zaloLink = 'https://zalo.me/09xxxxxxxx'  // TODO
const facebookLink = 'https://facebook.com/yourpage' // TODO
const contactDialogVisible = ref(false)
const showBulkDialog = () => {
  contactDialogVisible.value = true
  ElMessage.warning(`Số lượng lớn (≥ ${NGAY_MAX_QTY} đôi). Vui lòng liên hệ nhân viên để đặt hàng.`)
}

/* ========== Router ========== */
const route = useRoute()
const router = useRouter()

/* ========== State sản phẩm ========== */
const product = ref({
  id: null,
  productName: 'Đang tải…',
  productCode: '…',
  sellPrice: 0,
  discountedPrice: 0,
  brandId: null,
  brandName: '',
  brand: null,
  description: '',
  productDetails: [],
})

/* ========== Ảnh theo màu ========== */
const colorSpecificImages = ref([])
const currentImageIndex = ref(0)
const isLoadingImages = ref(false)

/* ========== Lựa chọn ========== */
const selectedColor = ref(null)
const selectedSize = ref(null)
const quantity = ref(1)
const isSizeGuideVisible = ref(false)

/* =========================
 * Helpers chung
 * ========================= */
const money = (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(n || 0))
const imgSrc = (img) => (img?.image ? `data:image/jpeg;base64,${img.image}` : (img?.url || '/no-image.jpg'))

const toNum = (v) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}
const isDiscountedPrice = (sell, disc) => {
  const s = toNum(sell), d = toNum(disc)
  return s > 0 && d > 0 && d < s
}

/* =========================
 * Color helpers (đã thay đổi như bộ sưu tập)
 * ========================= */
const COLOR_MAP = {
  // --- VN cơ bản ---
  'đen':'#000000','trắng':'#FFFFFF','đỏ':'#FF0000','xanh dương':'#0066FF','xanh biển':'#0077FF',
  'xanh nước biển':'#0077FF','xanh lam':'#1E90FF','xanh lá':'#008000','xanh lục':'#008000',
  'xanh rêu':'#556B2F','xanh ngọc':'#00CED1','xanh mint':'#98FF98','xanh pastel':'#A7E6FF',
  'xanh navy':'#000080','xanh coban':'#0047AB','xanh dạ quang':'#39FF14',
  'vàng':'#FFD700','vàng gold':'#FFD700','vàng chanh':'#FFF44F','vàng nhạt':'#FFFACD',
  'cam':'#FFA500','cam đất':'#D2691E','nâu':'#8B4513','nâu đất':'#7B3F00',
  'tím':'#800080','tím pastel':'#C3B1E1','tím than':'#3D2B56',
  'hồng':'#FFC0CB','hồng nhạt':'#FFDEE2','hồng đất':'#D18888','hồng cánh sen':'#FF6FAD',
  'be':'#F5F5DC','beige':'#F5F5DC','kem':'#FFFDD0','ivory':'#FFFFF0',
  'xám':'#808080','xám nhạt':'#D3D3D3','xám đậm':'#4B4B4B','ghi':'#9E9E9E',
  'bạc':'#C0C0C0','bạch kim':'#E5E4E2',
  'bordeuax':'#800020','đỏ rượu':'#800020','đỏ đô':'#8B0000',
  'than':'#111111','than đen':'#0F0F0F',

  // alias 1 từ
  'xanh':'#0066FF',

  // --- EN ---
  'black':'#000000','white':'#FFFFFF','red':'#FF0000','blue':'#0066FF','navy':'#000080',
  'green':'#008000','olive':'#808000','teal':'#008080','aqua':'#00FFFF','cyan':'#00FFFF',
  'lime':'#00FF00','yellow':'#FFFF00','orange':'#FFA500','brown':'#8B4513','chocolate':'#D2691E',
  'purple':'#800080','violet':'#8A2BE2','indigo':'#4B0082','magenta':'#FF00FF','pink':'#FFC0CB',
  'silver':'#C0C0C0','gold':'#FFD700','grey':'#808080','gray':'#808080','lightgray':'#D3D3D3',
  'ivory':'#FFFFF0','maroon':'#800000','burgundy':'#800020','khaki':'#C3B091','tan':'#D2B48C',
  'coral':'#FF7F50','salmon':'#FA8072',
}

const isHexColor = (str) => /^#([0-9A-F]{3}){1,2}$/i.test(String(str || '').trim())
const norm = (s) => String(s || '').trim().toLowerCase()
const hexFromName = (name) => {
  const k = norm(name)
  if (!k) return '#CCCCCC'
  if (isHexColor(k)) return k
  return COLOR_MAP[k] || '#CCCCCC'
}
/** Ưu tiên các bigram phổ biến (xanh dương, vàng gold, ...) rồi mới tách */
const parseCompositeColors = (rawName) => {
  const s = norm(rawName)
  if (!s) return []
  const bigrams = [
    'xanh dương','xanh biển','xanh nước biển','xanh lam','xanh lá','xanh lục','xanh rêu',
    'xanh navy','vàng gold','hồng nhạt','hồng đất','xám nhạt','xám đậm','đỏ rượu','đỏ đô',
  ]
  for (const bi of bigrams) {
    if (s.includes(bi)) {
      const token = bi.replace(/\s+/g, '__')
      const replaced = s.replace(new RegExp(bi, 'g'), token)
      return replaced
        .split(/[\/,\-\+&]+|\s+/g)
        .map(x => x.replace(/__/g, ' '))
        .filter(Boolean)
    }
  }
  return s.split(/[\/,\-\+,&]+|\s+/g).filter(Boolean)
}
/** Tính sáng để thêm viền nếu rất nhạt */
const isLightHex = (nameOrHex) => {
  const parts = parseCompositeColors(nameOrHex)
  const h = hexFromName(parts[0] || nameOrHex)
  try {
    const r = parseInt(h.slice(1,3),16), g = parseInt(h.slice(3,5),16), b = parseInt(h.slice(5,7),16)
    const L = 0.2126*r + 0.7152*g + 0.0722*b
    return L > 210
  } catch { return false }
}
/** Style cho swatch: 1 màu => backgroundColor; 2+ màu => linear-gradient chia đều */
const swatchStyle = (name) => {
  if (!name) return { backgroundColor: '#CCCCCC' }
  if (isHexColor(name)) return { backgroundColor: name }

  const parts = parseCompositeColors(name).map(hexFromName).filter(Boolean)
  if (parts.length <= 1) {
    const single = parts[0] || hexFromName(name)
    const style = { backgroundColor: single }
    if (isLightHex(single)) style.border = '1px solid #d1d5db'
    return style
  }
  const n = parts.length
  const stops = parts.map((c, i) => {
    const from = Math.round((100 * i) / n)
    const to   = Math.round((100 * (i + 1)) / n)
    return `${c} ${from}% ${to}%`
  }).join(', ')
  const style = { background: `linear-gradient(90deg, ${stops})` }
  if (parts.some(isLightHex)) style.border = '1px solid #d1d5db'
  return style
}

/* ========== Brand computed ========== */
const brandId = computed(() => product.value?.brandId ?? product.value?.brand?.id ?? null)
const brandName = computed(() =>
  product.value?.brandName
  ?? product.value?.brand?.brandName
  ?? product.value?.brand?.name
  ?? ''
)

/* ========== Ảnh chính ========== */
const mainImage = computed(() => {
  if (isLoadingImages.value) return '/loading-placeholder.gif'
  if (!colorSpecificImages.value.length) return '/no-image.jpg'
  return imgSrc(colorSpecificImages.value[currentImageIndex.value])
})

/* ========== Màu & size ========== */
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
        .filter(Boolean)
    )
  ).sort((a, b) => {
    const na = Number(a), nb = Number(b)
    if (Number.isFinite(na) && Number.isFinite(nb)) return na - nb
    return String(a).localeCompare(String(b))
  })
})

/* ========== Biến thể & giá hiển thị ========== */
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
  if (Number(product.value.discountedPrice) > 0 && product.value.discountedPrice < product.value.sellPrice) {
    return { original: product.value.sellPrice, discounted: product.value.discountedPrice }
  }
  return { original: null, discounted: product.value.sellPrice }
})

/* ========== Related products + normalize (với giá nhỏ nhất) ========== */
const related = ref({ items: [], pageSize: 8 })

const computeBestPriceForProduct = (p) => {
  // trả về { sell, discounted, percent, minVariant }
  const details = Array.isArray(p.productDetails) ? p.productDetails : []
  let minSell = Number.POSITIVE_INFINITY
  let minDiscounted = Number.POSITIVE_INFINITY
  let detailForMinSell = null
  let detailForMinDisc = null

  for (const d of details) {
    const sp = toNum(d?.sellPrice ?? p.sellPrice)
    if (sp > 0 && sp < minSell) {
      minSell = sp
      detailForMinSell = d
    }

    // discounted priority
    let dp = null
    if (d?.discountedPrice != null && d.discountedPrice !== '') {
      dp = toNum(d.discountedPrice)
    } else if (d?.discountPercentage != null && !isNaN(Number(d.discountPercentage)) && sp > 0) {
      dp = Math.round(sp * (100 - Number(d.discountPercentage)) / 100)
    }

    if (dp != null && dp > 0 && dp < sp) {
      if (dp < minDiscounted) {
        minDiscounted = dp
        detailForMinDisc = d
      }
    }
  }

  if (minSell === Number.POSITIVE_INFINITY) minSell = null
  if (minDiscounted === Number.POSITIVE_INFINITY) minDiscounted = null

  let percent = 0
  const baseSell = minSell ?? toNum(p.sellPrice)
  if (minDiscounted != null && baseSell > 0) {
    percent = Math.round((1 - (minDiscounted / baseSell)) * 100)
  } else if (p.discountPercentage) {
    percent = toNum(p.discountPercentage)
  }

  const minVariant = (detailForMinDisc || detailForMinSell) ? {
    productDetailId: (detailForMinDisc || detailForMinSell)?.id ?? null,
    sizeName: (detailForMinDisc || detailForMinSell)?.sizeName ?? null,
    colorName: (detailForMinDisc || detailForMinSell)?.colorName ?? null,
    colorId: (detailForMinDisc || detailForMinSell)?.colorId ?? null
  } : null

  return {
    sell: baseSell ?? toNum(p.sellPrice),
    discounted: minDiscounted != null ? minDiscounted : (isDiscountedPrice(p.sellPrice, p.discountedPrice) ? toNum(p.discountedPrice) : (baseSell ?? toNum(p.sellPrice))),
    percent: Math.round(percent),
    minVariant
  }
}

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

  const activeImage = variants[0]?.image || firstImg || '/no-image.jpg'
  const best = computeBestPriceForProduct(p)

  // attach min prices to product object so template can use if needed
  return {
    ...p,
    variants,
    activeImage,
    minSellPrice: best.sell,
    minDiscountedPrice: best.discounted,
    minDiscountPercentage: best.percent,
    minVariant: best.minVariant
  }
}

/* helper to check discount on any product object */
const hasDiscount = (p) => {
  if (!p) return false
  // if we attached minDiscountedPrice/minSellPrice use them
  if (p.minSellPrice != null && p.minDiscountedPrice != null && p.minDiscountedPrice < p.minSellPrice) return true
  // fallback to top-level
  if (isDiscountedPrice(p.sellPrice, p.discountedPrice)) return true
  // fallback check details
  const details = Array.isArray(p.productDetails) ? p.productDetails : []
  return details.some(d => toNum(d?.discountedPrice) > 0 && toNum(d?.discountedPrice) < toNum(d?.sellPrice ?? p.sellPrice))
}
const bestPrice = (p) => {
  if (!p) return { sell: 0, discounted: 0, percent: 0 }
  if (p.minSellPrice != null || p.minDiscountedPrice != null) {
    return {
      sell: p.minSellPrice ?? toNum(p.sellPrice),
      discounted: p.minDiscountedPrice != null ? p.minDiscountedPrice : (isDiscountedPrice(p.sellPrice, p.discountedPrice) ? toNum(p.discountedPrice) : (p.minSellPrice ?? toNum(p.sellPrice))),
      percent: p.minDiscountPercentage ?? 0
    }
  }
  // fallback compute on the fly
  return computeBestPriceForProduct(p)
}
const discountPercent = (p) => bestPrice(p).percent

/* ========== API calls ========== */
const fetchProduct = async () => {
  const id = route.params.id
  try {
    const { data } = await apiClient.get(`/online-sale/${id}`)
    product.value = data || product.value
  } catch (e) {
    console.error('fetchProduct', e)
  }
}

const fetchImagesForColor = async (colorId) => {
  if (!colorId) return
  isLoadingImages.value = true
  try {
    const productId = route.params.id
    const { data } = await apiClient.get(`/online-sale/product-images`, { params: { productId, colorId } })
    colorSpecificImages.value = Array.isArray(data) ? data : []
    currentImageIndex.value = 0
  } finally {
    isLoadingImages.value = false
  }
}

const fetchRelated = async () => {
  related.value.items = []
  if (!brandId.value) return
  try {
    const { data } = await apiClient.get(`/online-sale/brands/${brandId.value}/products`, { params: { page: 0, size: related.value.pageSize + 1 } })
    const payload = data ?? {}
    const list = Array.isArray(payload.content) ? payload.content
      : Array.isArray(payload.data?.content) ? payload.data.content
        : Array.isArray(payload) ? payload : []
    related.value.items = list
      .filter(p => p && p.id !== product.value.id)
      .slice(0, related.value.pageSize)
      .map(normalizeProduct)
  } catch (e){
    console.log(e);
  }
}

/* ========== UI events ========== */
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

// === Trả về boolean, chỉ toast success khi thêm OK ===
const handleAddToCart = async () => {
  if (!selectedColor.value || !selectedSize.value) {
    ElMessage.warning('Vui lòng chọn màu & kích thước!')
    return false
  }

  const d = findSelectedDetail()
  if (!d) {
    ElMessage.error('Không tìm thấy biến thể phù hợp!')
    return false
  }

  const productDetailId = d.id
  try {
    const res = await axios.get(`http://localhost:8080/api/online-sale/verify-pdDetail/${productDetailId}`)
    const status = res?.data?.status ?? res?.data?.active ?? res?.data?.enabled
    let ok = false
    if (typeof status === 'boolean') ok = status === true
    else {
      const n = Number(status)
      ok = Number.isFinite(n) ? n === 1 : ['active','enabled','available','true'].includes(String(status).trim().toLowerCase())
    }
    if (!ok) {
      ElMessage.error('Sản phẩm hiện không hợp lệ để mua (đã bị vô hiệu hóa hoặc không còn bán).')
      return false
    }
  } catch (e) {
    console.error('Lỗi khi kiểm tra trạng thái productDetail:', e)
    ElMessage.error('Không thể kiểm tra trạng thái sản phẩm. Vui lòng thử lại sau.')
    return false
  }

  // Chặn SL lớn
  if (Number(quantity.value) >= NGAY_MAX_QTY) {
    showBulkDialog()
    return false
  }

  // Chặn vượt tồn kho
  if (Number(quantity.value) > Number(d.quantity || 0)) {
    ElMessage.warning(`Chỉ còn ${d.quantity} sản phẩm.`)
    return false
  }

  // ======= Lưu cả giá gốc & giá sau giảm vào giỏ (giữ trường price cũ) =======
  const unitPriceOriginal = Number(d.sellPrice) > 0 ? Number(d.sellPrice) : Number(product.value.sellPrice) || 0
  const hasDisc = Number(d.discountedPrice) > 0 && Number(d.discountedPrice) < unitPriceOriginal
  const unitPriceDiscounted = hasDisc ? Number(d.discountedPrice) : null
  const unitPriceFinal = hasDisc ? Number(d.discountedPrice) : unitPriceOriginal
  const price = unitPriceFinal

  const cartItem = {
    productDetailId: d.id,
    productId: product.value.id,
    productName: product.value.productName,
    productCode: product.value.productCode,
    image: mainImage.value,
    color: selectedColor.value,
    size: selectedSize.value,

    unitPriceOriginal,
    unitPriceDiscounted,
    unitPriceFinal,
    price,

    quantity: Number(quantity.value || 1),
    discountCampaignId: d.discountCampaignId || null,
    status: d.status
  }

  const result = addToCart(cartItem)
  if (result?.ok) {
    ElMessage.success('Đã thêm vào giỏ hàng!')
    return true
  }
  ElMessage.error('Không thể thêm vào giỏ hàng.')
  return false
}

// === PHẢI async + await, chỉ push khi thêm OK ===
const handleBuyNow = async () => {
  if (Number(quantity.value) >= NGAY_MAX_QTY) {
    showBulkDialog()
    return
  }
  const ok = await handleAddToCart()
  if (ok) {
    await router.push('/cart')
  }
}


/* ========== Điều hướng chi tiết sp liên quan ========== */
const goToDetail = (id) => router.push(`/product/${id}`)

/* ========== Lifecycle ========== */
const initPage = async () => {
  await fetchProduct()
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
/* ==== Wrapper & breadcrumb ==== */
.pd-wrapper {
  max-width: 1400px;
  margin: 20px auto 60px;
  padding: 0 20px;
}
.pd-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}
.pd-breadcrumb a { color: #666; text-decoration: none; }
.pd-breadcrumb a:hover { color: #000; text-decoration: underline; }
.pd-breadcrumb .sep { opacity: .6; }
.pd-breadcrumb .current { color: #222; }

/* ==== Layout & font ==== */
.product-detail {
  color: #333;
  line-height: 1.5;
  font-size: 15px;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

/* ==== Thumbnails ==== */
.pd-thumbs-col { padding-right: 6px; }
.thumbnail-list { display: flex; flex-direction: column; gap: 8px; }
.thumbnail {
  width: 100%;
  aspect-ratio: 1/1;
  object-fit: cover;
  display: block;
  border: none;
  border-radius: 0;
  cursor: pointer;
}
.thumbnail.active {
  outline: 2px solid #000;
  outline-offset: -2px;
}
.thumbnail:hover { opacity: 0.9; }

/* ==== Ảnh chính ==== */
.main-image-container {
  position: relative;
  border: none;
  border-radius: 0;
  background: #fafafa;
  overflow: hidden;
}
.main-image {
  width: 100%;
  height: auto;
  display: block;
}
.nav-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: #fff;
  border: 1px solid #ccc;
  width: 36px;
  height: 36px;
  font-size: 18px;
  color: #444;
}
.nav-button.prev { left: 10px; }
.nav-button.next { right: 10px; }
.nav-button:hover { background: #111; color: #fff; }

/* ==== Head + brand badge ==== */
.pd-head { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.product-name { font-size: 28px; font-weight: 700; margin: 0; color: #222; }
.brand-badge {
  background: #111; color: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  text-transform: uppercase;
  border: 1px solid #111;
}
.brand-badge:hover { background: #fff; color: #111; }

/* ==== Code & giá ==== */
.product-code { color: #777; font-size: 14px; margin: 6px 0 16px; }
.product-price { font-size: 26px; font-weight: 700; margin-bottom: 18px; display: flex; gap: 12px; }
.original-price { text-decoration: line-through; color: #9e9e9e; font-size: 20px; }
.discounted-price { color: #d32f2f; font-size: 28px; }
.only-price { font-size: 28px; }

/* ==== Màu & size ==== */
.selector-title { font-weight: 600; margin-bottom: 8px; }
.color-selector { margin: 14px 0; }
.color-swatches { display: flex; gap: 10px; flex-wrap: wrap; }
.color-swatch {
  width: 32px; height: 32px;
  border-radius: 50%;
  border: 1px solid #ccc;
  cursor: pointer;
}
.color-swatch.selected { border: 2px solid #000; }
.color-swatch.is-light { border-color: #333; }

.size-selector { margin: 14px 0; }
.size-header { display: flex; justify-content: space-between; align-items: center; }
.size-guide-link { color: #409eff; font-size: 13px; }
.size-buttons { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
.size-buttons .el-button { min-width: 52px; height: 38px; font-size: 14px; border-radius: 4px; }
.size-buttons .el-button.selected {
  background: #111 !important; color: #fff !important; border-color: #111 !important;
}

/* ==== Số lượng ==== */
.qty-row { display: flex; justify-content: space-between; align-items: center; margin: 12px 0 4px; }
.stock { font-size: 13px; color: #16a34a; }
.stock.low { color: #d97706; }

/* ==== Actions ==== */
.action-buttons { display: flex; gap: 12px; margin: 18px 0 22px; }
.action-buttons .el-button {
  flex: 1; height: 48px; font-size: 15px; font-weight: 700; border-radius: 6px;
}
.add-to-cart-btn { background: #fff; color: #111; border: 2px solid #111; }
.add-to-cart-btn:hover { background: #111; color: #fff; }
.buy-now-btn { background: #b81c23; color: #fff; border: 2px solid #b81c23; }
.buy-now-btn:hover { background: #d02a31; }

/* ==== Liên quan ==== */
.related-wrapper { margin-top: 28px; }
.related-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 14px; }
.related-title { display: flex; align-items: baseline; gap: 10px; }
.related-header h3 { font-size: 20px; margin: 0; }
.related-card {
  background: #fff;
  border: none;
  border-radius: 0;
  cursor: pointer;
}
.related-image { position: relative; width: 100%; padding-bottom: 100%; background: #f5f5f5; }
.related-image img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.related-image .badge {
  position: absolute; top: 8px; left: 8px;
  background: #d32f2f; color: #fff; font-size: 12px; padding: 2px 6px; border-radius: 4px;
}
.related-info { padding: 10px; }
.related-info .name { font-size: 14px; height: 40px; overflow: hidden; margin: 0 0 6px; }
.related-info .price { display: flex; gap: 8px; align-items: baseline; }
.related-info .price .new { font-weight: 700; }
.related-info .price .old { color: #999; text-decoration: line-through; }

/* ==== Contact dialog ==== */
.contact-block { font-size:14px; color:#333; }
.contact-list { margin:10px 0 0; padding-left:18px; }
.contact-list li { margin:4px 0; }
.contact-list a { color:#409eff; text-decoration:none; }
.contact-list a:hover { text-decoration:underline; }

/* ==== Responsive ==== */
@media (max-width: 992px) { .product-name { font-size: 24px; } }
@media (max-width: 768px) { .pd-thumbs-col { display: none; } }
</style>

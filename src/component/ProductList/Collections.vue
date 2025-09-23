<template>
  <div class="product-collection-container">
    <el-container class="two-columns">
      <!-- ===== ASIDE: FILTERS ===== -->
      <el-aside class="filter-aside">
        <!-- ===== BRAND FILTER ===== -->
        <div class="filter-card">
          <button class="filter-accordion" @click="brandOpen = !brandOpen">
            <span>THƯƠNG HIỆU</span>
            <el-icon :class="['chevron', { 'is-open': brandOpen }]">
              <i class="el-icon-arrow-down"></i>
            </el-icon>
          </button>

          <transition name="slide-fade">
            <div v-show="brandOpen" class="filter-body">
              <el-input v-model="brandSearch" placeholder="Tùy chọn tìm kiếm." clearable size="large" class="filter-search"/>
              <div class="filter-list">
                <label v-for="b in filteredBrands" :key="b.id" class="filter-item">
                  <input
                    type="checkbox"
                    class="filter-checkbox"
                    :checked="selectedBrandIds.includes(b.id)"
                    @change="onBrandToggle(b.id)"
                  />
                  <span class="filter-name">{{ b.name }}</span>
                </label>
                <div v-if="!isLoadingBrands && filteredBrands.length===0" class="filter-empty">Không tìm thấy thương hiệu</div>
                <div v-if="isLoadingBrands" class="filter-loading">Đang tải thương hiệu…</div>
              </div>
            </div>
          </transition>
        </div>

        <!-- ===== COLOR FILTER ===== -->
        <div class="filter-card space-top">
          <button class="filter-accordion" @click="colorOpen = !colorOpen">
            <span>MÀU SẮC</span>
            <el-icon :class="['chevron', { 'is-open': colorOpen }]">
              <i class="el-icon-arrow-down"></i>
            </el-icon>
          </button>

          <transition name="slide-fade">
            <div v-show="colorOpen" class="filter-body">
              <el-input v-model="colorSearch" placeholder="Tùy chọn tìm kiếm." clearable size="large" class="filter-search"/>
              <div class="filter-list">
                <label v-for="c in filteredColors" :key="c.id" class="filter-item">
                  <input
                    type="checkbox"
                    class="filter-checkbox"
                    :checked="selectedColorIds.includes(c.id)"
                    @change="onColorToggle(c)"
                  />
                  <!-- CHỈ ĐỔI DÒNG NÀY: dùng swatchStyle để hỗ trợ “màu ghép” -->
                  <span class="swatch" :style="swatchStyle(c.name)"></span>
                  <span class="filter-name">{{ c.name }}</span>
                </label>
                <div v-if="!isLoadingColors && filteredColors.length===0" class="filter-empty">Không tìm thấy màu</div>
                <div v-if="isLoadingColors" class="filter-loading">Đang tải màu sắc…</div>
              </div>
            </div>
          </transition>
        </div>

        <!-- ===== GENDER FILTER ===== -->
        <div class="filter-card space-top">
          <button class="filter-accordion" @click="genderOpen = !genderOpen">
            <span>GIỚI TÍNH</span>
            <el-icon :class="['chevron', { 'is-open': genderOpen }]">
              <i class="el-icon-arrow-down"></i>
            </el-icon>
          </button>

          <transition name="slide-fade">
            <div v-show="genderOpen" class="filter-body">
              <el-input v-model="genderSearch" placeholder="Tùy chọn tìm kiếm." clearable size="large" class="filter-search"/>
              <div class="filter-list">
                <label v-for="g in filteredGenders" :key="g.id" class="filter-item">
                  <input
                    type="checkbox"
                    class="filter-checkbox"
                    :checked="selectedGenderIds.includes(g.id)"
                    @change="onGenderToggle(g)"
                  />
                  <span class="filter-name">{{ g.name }}</span>
                </label>
                <div v-if="!isLoadingGenders && filteredGenders.length===0" class="filter-empty">Không tìm thấy giới tính</div>
                <div v-if="isLoadingGenders" class="filter-loading">Đang tải giới tính…</div>
              </div>
            </div>
          </transition>
        </div>

        <!-- ===== SIZE FILTER ===== -->
        <div class="filter-card space-top">
          <button class="filter-accordion" @click="sizeOpen = !sizeOpen">
            <span>KÍCH THƯỚC</span>
            <el-icon :class="['chevron', { 'is-open': sizeOpen }]">
              <i class="el-icon-arrow-down"></i>
            </el-icon>
          </button>

          <transition name="slide-fade">
            <div v-show="sizeOpen" class="filter-body">
              <el-input v-model="sizeSearch" placeholder="Tùy chọn tìm kiếm." clearable size="large" class="filter-search"/>
              <div class="filter-list">
                <label v-for="s in filteredSizes" :key="s.id" class="filter-item">
                  <input
                    type="checkbox"
                    class="filter-checkbox"
                    :checked="selectedSizeIds.includes(s.id)"
                    @change="onSizeToggle(s)"
                  />
                  <span class="filter-name">{{ s.name }}</span>
                </label>
                <div v-if="!isLoadingSizes && filteredSizes.length===0" class="filter-empty">Không tìm thấy size</div>
                <div v-if="isLoadingSizes" class="filter-loading">Đang tải size…</div>
              </div>
            </div>
          </transition>
        </div>
      </el-aside>

      <!-- ===== MAIN: PRODUCTS ===== -->
      <el-main class="product-list-main">
        <div class="collection-header">
          <h2 class="collection-title">
            <template v-if="categoryId">Danh mục: {{ categoryName || ('#' + categoryId) }}</template>
            <template v-else-if="effectiveGenderId">Giới tính: {{ genderName || ('#' + effectiveGenderId) }}</template>
            <template v-else-if="effectiveSizeId">Kích thước: {{ sizeName || ('#' + effectiveSizeId) }}</template>
            <template v-else-if="effectiveColorId">Màu sắc: {{ colorName || ('#' + effectiveColorId) }}</template>
            <template v-else-if="effectiveBrandId">Thương hiệu: {{ brandName || ('#' + effectiveBrandId) }}</template>
            <template v-else>Bộ sưu tập</template>
          </h2>
        </div>

        <div v-if="isLoading" class="loading-state">Đang tải sản phẩm…</div>
        <div v-else-if="error" class="error-state">{{ error }}</div>

        <template v-else>
          <el-row :gutter="30">
            <el-col v-for="p in products" :key="p.id" :xs="24" :sm="12" :md="8" :lg="6">
              <div class="product-card">
                <div class="product-card__image-wrapper">
                  <span v-if="p.minDiscountPercentage > 0" class="product-card__discount-badge">-{{ p.minDiscountPercentage }}%</span>

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

                  <div class="product-card__sold-badge">
                    Đã bán: <strong>{{ soldCounts[p.id] ?? '...' }}</strong>
                  </div>
                </div>

                <div class="product-card__info">
                  <p class="product-card__name" @click="goToDetail(p.id)">{{ p.productName }}</p>

                  <div class="product-card__price-container">
                    <template v-if="p.minDiscountedPrice != null && p.minSellPrice != null && p.minDiscountedPrice < p.minSellPrice">
                      <span class="discounted-price">{{ formatPrice(p.minDiscountedPrice) }}</span>
                      <del class="original-price">{{ formatPrice(p.minSellPrice) }}</del>
                    </template>
                    <template v-else-if="p.minSellPrice != null">
                      <span class="normal-price">{{ formatPrice(p.minSellPrice) }}</span>
                    </template>
                    <template v-else>
                      <span class="normal-price">{{ formatPrice(p.sellPrice) }}</span>
                    </template>
                  </div>

                  <div class="product-card__colors" v-if="p.variants?.length">
                    <!-- CHỈ ĐỔI DÒNG NÀY: dùng swatchStyle theo tên màu, hỗ trợ 1–n màu -->
                    <span
                      v-for="v in p.variants"
                      :key="v.colorId"
                      class="product-card__color-dot"
                      :style="swatchStyle(v.colorName)"
                      :title="v.colorName"
                      @mouseenter="changeProductImage(p, v.image)"
                      @mouseleave="restoreProductImage(p)"
                    />
                  </div>

                  <div class="rating-row">
                    <el-rate :model-value="(ratingSummaries[p.id]?.avg ?? 0)" disabled allow-half :max="5" size="small"/>
                    <span class="rating-text">{{ (ratingSummaries[p.id]?.avg ?? 0).toFixed(1) }} ⭐ ({{ ratingSummaries[p.id]?.count ?? 0 }})</span>
                  </div>

                  <el-button
                    v-if="p.id"
                    size="small"
                    :type="isFavorite(p.id) ? 'primary' : 'success'"
                    @click="toggleFavorite(p.id, p.productName)"
                  >
                    {{ isFavorite(p.id) ? 'Đã yêu thích' : 'Thêm vào Yêu thích' }}
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
              layout="total, prev, pager, next, jumper, sizes"
              @size-change="onPageUiChanged"
              @current-change="onPageUiChanged"
            />
          </div>
        </template>
      </el-main>
    </el-container>

    <!-- ===== QUICK VIEW ===== -->
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

    <!-- ===== PRE-ORDER ===== -->
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
          <div class="stock-info" v-if="preOrderSelectedSize">(Tồn hiện tại: {{ preOrderStock }})</div>
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

    <!-- ===== CONTACT STAFF (SL ≥ NGAY_MAX_QTY) ===== -->
    <el-dialog
      v-model="contactDialogVisible"
      title="Liên hệ nhân viên để đặt số lượng lớn"
      width="520px"
    >
      <div class="contact-block">
        <p>Đơn online không hỗ trợ <strong>Mua ngay/Thêm giỏ</strong> khi số lượng từ <strong>{{ NGAY_MAX_QTY }}</strong> đôi trở lên.</p>
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
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import apiClient from '@/utils/axiosInstance'
import { addToCart } from '@/utils/cart.js'
import axios from 'axios'

/* ========== Config (có thể bind từ .env hoặc API) ========== */
const NGAY_MAX_QTY = 10 // ngưỡng chặn Mua ngay/Thêm giỏ
const hotline = '0346771322'
const zaloLink = 'https://zalo.me/0346771322'
const facebookLink = 'https://www.facebook.com/phantuananh181205/'

/* ========== Router & Params ========== */
const router = useRouter()
const route = useRoute()

// Brand
const brandName = ref(String(route.query.brandName || '').trim())
const brandIdFromQuery = computed(() => {
  const n = Number(route.query.brandId ?? route.params.brandId)
  return Number.isFinite(n) ? n : null
})

// Color
const colorName = ref(String(route.query.colorName || '').trim())
const colorIdFromQuery = computed(() => {
  const n = Number(route.query.colorId ?? route.params.colorId)
  return Number.isFinite(n) ? n : null
})

// Gender
const genderName = ref(String(route.query.genderName || '').trim())
const genderIdFromQuery = computed(() => {
  const n = Number(route.query.genderId ?? route.params.genderId)
  return Number.isFinite(n) ? n : null
})

// Size
const sizeName = ref(String(route.query.sizeName || '').trim())
const sizeIdFromQuery = computed(() => {
  const n = Number(route.query.sizeId ?? route.params.sizeId)
  return Number.isFinite(n) ? n : null
})

// Category (optional)
const categoryId = computed(() => {
  const n = Number(route.query.categoryId ?? route.params.categoryId)
  return Number.isFinite(n) ? n : null
})
const categoryName = ref(String(route.query.categoryName || '').trim())

/* ========== Helpers ========== */
const PLACEHOLDER_IMG = 'https://via.placeholder.com/400'
const formatPrice = (price) => {
  const n = Number(price)
  if (!Number.isFinite(n)) return 'N/A'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

/* =========================
 * COLOR: map & helpers
 * ========================= */

/** Bảng màu mở rộng (VN + EN + alias) */
const COLOR_MAP = {
  // --- Cơ bản (VN) ---
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

  // --- Alias 1 từ (VN) ---
  'xanh':'#0066FF', // nếu muốn mặc định xanh lá: đổi '#008000'
  'ghi sáng':'#B0B0B0','ghi đậm':'#6C6C6C',

  // --- Tiếng Anh ---
  'black':'#000000','white':'#FFFFFF','red':'#FF0000','blue':'#0066FF','navy':'#000080',
  'green':'#008000','olive':'#808000','teal':'#008080','aqua':'#00FFFF','cyan':'#00FFFF',
  'lime':'#00FF00','yellow':'#FFFF00','orange':'#FFA500','brown':'#8B4513','chocolate':'#D2691E',
  'purple':'#800080','violet':'#8A2BE2','indigo':'#4B0082','magenta':'#FF00FF','pink':'#FFC0CB',
  'silver':'#C0C0C0','gold':'#FFD700','grey':'#808080','gray':'#808080','lightgray':'#D3D3D3',
  'beige':'#F5F5DC','ivory':'#FFFFF0','maroon':'#800000','burgundy':'#800020',
  'khaki':'#C3B091','tan':'#D2B48C','coral':'#FF7F50','salmon':'#FA8072',
}

/** Nhận diện mã hex #RGB/#RRGGBB */
const isHexColor = (str) => /^#([0-9A-F]{3}){1,2}$/i.test(String(str || '').trim())
/** Chuẩn hóa khóa */
const norm = (name) => String(name || '').trim().toLowerCase()
/** Lấy hex từ tên (nếu là hex thì trả thẳng) */
const hexFromName = (name) => {
  const key = norm(name)
  if (!key) return '#CCCCCC'
  if (isHexColor(key)) return key
  return COLOR_MAP[key] || '#CCCCCC'
}
/** Parse “màu ghép”: ưu tiên các bigram phổ biến rồi mới tách */
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
/** Độ sáng để thêm viền với màu rất nhạt */
const isLightHex = (hex) => {
  try {
    const h = hexFromName(hex)
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
/** Lấy 1 mã hex đại diện (màu đầu tiên) — dùng lưu DB nếu cần */
const colorCodeOf = (colorName) => {
  const parts = parseCompositeColors(colorName)
  return hexFromName(parts[0] || colorName)
}

/* ========== Size helpers / others (GIỮ NGUYÊN) ========== */
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

const isLoading = ref(true)
const error = ref(null)
const products = ref([])
const totalItems = ref(0)
const isAdding = ref(false)

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

/* ========== Favorites (GIỮ NGUYÊN) ========== */
const customerId = localStorage.getItem('userId') || null
const favorites = ref([])
const loadFavorites = () => {
  try { favorites.value = JSON.parse(localStorage.getItem('favorites') || '[]') || [] }
  catch { favorites.value = [] }
}
const isFavorite = (productId) =>
  favorites.value.some(
    (f) => String(f.customerId) === String(customerId) && Number(f.productId) === Number(productId)
  )
const toggleFavorite = (productId, name) => {
  if (!customerId) { ElMessage.warning('Vui lòng đăng nhập để thêm vào yêu thích.'); return }
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

/* ========== Normalize product (CHỈ GIỮ, có dùng colorCodeOf đại diện) ========= */
const normalizeProduct = (p) => {
  const details = Array.isArray(p.productDetails) ? p.productDetails : []
  const images = Array.isArray(p.productImages) ? p.productImages : []

  const seen = new Set()
  const variants = []
  for (const d of details) {
    const cid = d?.colorId
    if (cid == null) continue
    if (seen.has(cid)) continue
    seen.add(cid)
    const img = images.find((i) => Number(i?.colorId) === Number(cid))
    variants.push({
      colorId: cid,
      colorName: d?.colorName || 'Không rõ',
      colorCode: colorCodeOf(d?.colorName), // vẫn lưu 1 mã đại diện nếu cần
      image: img?.image ? `data:image/jpeg;base64,${img.image}` : (img?.url || null),
    })
  }
  const activeImage = variants[0]?.image || images[0]?.url || PLACEHOLDER_IMG

  let minSell = Number.POSITIVE_INFINITY
  let minDiscounted = Number.POSITIVE_INFINITY
  let minDetailForDiscount = null
  let minDetailForOriginal = null

  for (const d of details) {
    const sp = Number(d?.sellPrice ?? NaN)
    if (!isNaN(sp)) {
      if (sp < minSell) {
        minSell = sp
        minDetailForOriginal = d
      }
    }
    let dp = null
    if (d?.discountedPrice != null && d.discountedPrice !== '') {
      const v = Number(d.discountedPrice)
      if (!isNaN(v)) dp = v
    } else if (d?.discountPercentage != null && !isNaN(Number(d.discountPercentage)) && !isNaN(sp)) {
      const pct = Number(d.discountPercentage)
      dp = Math.round(sp * (100 - pct) / 100)
    }
    if (dp != null && !isNaN(dp) && !isNaN(sp) && dp < sp) {
      if (dp < minDiscounted) {
        minDiscounted = dp
        minDetailForDiscount = d
      }
    }
  }

  if (minSell === Number.POSITIVE_INFINITY) minSell = null
  if (minDiscounted === Number.POSITIVE_INFINITY) minDiscounted = null

  let minDiscountPct = 0
  let chosenDetail = minDetailForDiscount || minDetailForOriginal || (details[0] ?? null)
  if (chosenDetail) {
    if (chosenDetail.discountPercentage != null) minDiscountPct = Number(chosenDetail.discountPercentage) || 0
    else if (minDiscounted != null && minSell != null) {
      minDiscountPct = Math.round((1 - (minDiscounted / minSell)) * 100)
    }
  }

  const minVariant = chosenDetail ? {
    productDetailId: chosenDetail.id ?? null,
    sizeName: chosenDetail.sizeName ?? null,
    colorName: chosenDetail.colorName ?? null,
    colorId: chosenDetail.colorId ?? null
  } : null

  return {
    ...p,
    variants,
    activeImage,
    __originalImage: activeImage,
    minSellPrice: minSell,
    minDiscountedPrice: minDiscounted,
    minDiscountPercentage: minDiscountPct,
    minVariant
  }
}

/* ========== BRAND/COLOR/GENDER/SIZE filters (GIỮ NGUYÊN) ========== */
const brandOpen = ref(true)
const brandSearch = ref('')
const brands = ref([])
const isLoadingBrands = ref(false)
const selectedBrandIds = ref([])

const effectiveBrandId = computed(() => {
  const picked = Number(selectedBrandIds.value[0])
  return Number.isFinite(picked) ? picked : brandIdFromQuery.value
})

const fetchBrands = async () => {
  isLoadingBrands.value = true
  const endpoints = ['online-sale/brand/hien-thi']
  let data = null
  for (const ep of endpoints) {
    try {
      const res = await apiClient.get(ep)
      if (Array.isArray(res?.data)) { data = res.data; break }
    } catch {}
  }
  brands.value = Array.isArray(data)
    ? data.map(x => ({ id: x.id, name: x.name || x.ten || x.brandName || ('#' + x.id) }))
    : []
  isLoadingBrands.value = false
}
const filteredBrands = computed(() => {
  const q = brandSearch.value.trim().toLowerCase()
  return q ? brands.value.filter(b => String(b.name).toLowerCase().includes(q)) : brands.value
})
const onBrandToggle = (id) => {
  const target = Number(id)
  if (!Number.isFinite(target)) return
  if (selectedBrandIds.value.includes(target)) {
    selectedBrandIds.value = []
    const q = { ...route.query, page: 1 }; delete q.brandId; delete q.brandName
    router.push({ query: q })
    return
  }
  selectedBrandIds.value = [target]
  const b = brands.value.find(x => Number(x.id) === target)
  router.push({ query: { ...route.query, brandId: String(target), brandName: b?.name || undefined, page: 1 } })
}

/* ========== COLOR ========== */
const colorOpen = ref(true)
const colorSearch = ref('')
const colors = ref([])
const isLoadingColors = ref(false)
const selectedColorIds = ref([])

const effectiveColorId = computed(() => {
  const picked = Number(selectedColorIds.value[0])
  return Number.isFinite(picked) ? picked : colorIdFromQuery.value
})

const fetchColors = async () => {
  isLoadingColors.value = true
  const endpoints = ['online-sale/color/hien-thi']
  let data = null
  for (const ep of endpoints) {
    try {
      const res = await apiClient.get(ep)
      if (Array.isArray(res?.data)) { data = res.data; break }
    } catch {}
  }
  colors.value = Array.isArray(data)
    ? data.map(x => ({
      id: x.id,
      name: x.name || x.ten || x.colorName || ('#' + x.id),
      code: x.code || x.ma || colorCodeOf(x.name || x.ten || x.colorName),
    }))
    : []
  isLoadingColors.value = false
}
const filteredColors = computed(() => {
  const q = colorSearch.value.trim().toLowerCase()
  return q ? colors.value.filter(c => String(c.name).toLowerCase().includes(q)) : colors.value
})
const onColorToggle = (c) => {
  const target = Number(c.id)
  if (!Number.isFinite(target)) return
  if (selectedColorIds.value.includes(target)) {
    selectedColorIds.value = []
    const q = { ...route.query, page: 1 }; delete q.colorId; delete q.colorName
    router.push({ query: q })
    return
  }
  selectedColorIds.value = [target]
  router.push({ query: { ...route.query, colorId: String(target), colorName: c?.name || undefined, page: 1 } })
}

/* ========== GENDER ========== */
const genderOpen = ref(true)
const genderSearch = ref('')
const genders = ref([])
const isLoadingGenders = ref(false)
const selectedGenderIds = ref([])

const effectiveGenderId = computed(() => {
  const picked = Number(selectedGenderIds.value[0])
  return Number.isFinite(picked) ? picked : genderIdFromQuery.value
})

const fetchGenders = async () => {
  isLoadingGenders.value = true
  const endpoints = ['online-sale/gender/hien-thi']
  let data = null
  for (const ep of endpoints) {
    try {
      const res = await apiClient.get(ep)
      if (Array.isArray(res?.data)) { data = res.data; break }
    } catch {}
  }
  genders.value = Array.isArray(data)
    ? data.map(x => ({ id: x.id, name: x.name || x.ten || x.genderName || ('#' + x.id) }))
    : []
  isLoadingGenders.value = false
}
const filteredGenders = computed(() => {
  const q = genderSearch.value.trim().toLowerCase()
  return q ? genders.value.filter(g => String(g.name).toLowerCase().includes(q)) : genders.value
})
const onGenderToggle = (g) => {
  const target = Number(g.id)
  if (!Number.isFinite(target)) return
  if (selectedGenderIds.value.includes(target)) {
    selectedGenderIds.value = []
    const q = { ...route.query, page: 1 }; delete q.genderId; delete q.genderName
    router.push({ query: q })
    return
  }
  selectedGenderIds.value = [target]
  router.push({ query: { ...route.query, genderId: String(target), genderName: g?.name || undefined, page: 1 } })
}

/* ========== SIZE ========== */
const sizeOpen = ref(true)
const sizeSearch = ref('')
const sizes = ref([])
const isLoadingSizes = ref(false)
const selectedSizeIds = ref([])

const effectiveSizeId = computed(() => {
  const picked = Number(selectedSizeIds.value[0])
  return Number.isFinite(picked) ? picked : sizeIdFromQuery.value
})

const fetchSizes = async () => {
  isLoadingSizes.value = true
  const endpoints = ['online-sale/size/hien-thi']
  let data = null
  for (const ep of endpoints) {
    try {
      const res = await apiClient.get(ep)
      if (Array.isArray(res?.data)) { data = res.data; break }
    } catch {}
  }
  sizes.value = Array.isArray(data)
    ? data.map(x => ({ id: x.id, name: x.name || x.ten || x.sizeName || ('#' + x.id) }))
    : []
  sizes.value.sort((a,b) => sizeComparator(a.name, b.name))
  isLoadingSizes.value = false
}
const filteredSizes = computed(() => {
  const q = sizeSearch.value.trim().toLowerCase()
  return q ? sizes.value.filter(s => String(s.name).toLowerCase().includes(q)) : sizes.value
})
const onSizeToggle = (s) => {
  const target = Number(s.id)
  if (!Number.isFinite(target)) return
  if (selectedSizeIds.value.includes(target)) {
    selectedSizeIds.value = []
    const q = { ...route.query, page: 1 }; delete q.sizeId; delete q.sizeName
    router.push({ query: q })
    return
  }
  selectedSizeIds.value = [target]
  router.push({ query: { ...route.query, sizeId: String(target), sizeName: s?.name || undefined, page: 1 } })
}

const fetchSoldCountsForPage = async (items) => {
  const ids = (items || []).map(x => x?.id).filter(id => Number.isFinite(Number(id)))
  for (const id of ids) fetchSoldCount(id)
}
const fetchRatingsForPage = async (items) => {
  const ids = (items || []).map(x => Number(x?.id)).filter(Number.isFinite)
  const need = ids.filter(id => !ratingSummaries.value[id])
  if (!need.length) return
  try {
    const { data } = await apiClient.get('/online-sale/ratings/products', { params: { ids: need.join(',') } })
    const map = {}
    for (const r of (Array.isArray(data) ? data : [])) {
      const pid = Number(r?.productId)
      if (!Number.isFinite(pid)) continue
      map[pid] = { avg: Number(r?.avgRating ?? 0), count: Number(r?.totalReviews ?? 0) }
    }
    ratingSummaries.value = { ...ratingSummaries.value, ...map }
    const missing = need.filter(id => !map[id])
    for (const id of missing) await fetchRatingSingle(id)
  } catch { for (const id of need) await fetchRatingSingle(id) }
}

const fetchProductsByCategory = async (params) => {
  const endpoints = [
    `/online-sale/categories/${categoryId.value}/products`,
    `/categories/${categoryId.value}/products`,
    `/online-sale?categoryId=${categoryId.value}`,
  ]
  let data = null
  for (const ep of endpoints) {
    try { const res = await apiClient.get(ep, { params }); data = res?.data; if (data) break } catch {}
  }
  if (!data) throw new Error('Không thể tải sản phẩm theo danh mục.')

  const payload = data ?? {}
  const content =
    Array.isArray(payload.content) ? payload.content :
      Array.isArray(payload.data?.content) ? payload.data.content :
        Array.isArray(payload) ? payload : []

  if (!Array.isArray(content)) throw new Error('Định dạng dữ liệu API không hợp lệ.')
  const map = new Map()
  for (const item of content) if (item && item.id != null) map.set(item.id, item)
  products.value = Array.from(map.values()).map(normalizeProduct)

  totalItems.value = Number(payload.totalElements) || Number(payload.data?.totalElements) || Number(payload.page?.totalElements) || content.length
  await fetchSoldCountsForPage(products.value); await fetchRatingsForPage(products.value)
}

const fetchProductsByGender = async (genderId, params) => {
  const endpoints = ['/online-sale/by-gender']
  let data = null
  for (const ep of endpoints) {
    try { const res = await apiClient.get(ep, { params: { ...params, genderId } }); data = res?.data; if (data) break } catch {}
  }
  if (!data) throw new Error('Không thể tải sản phẩm theo giới tính.')

  const payload = data ?? {}
  const content =
    Array.isArray(payload.content) ? payload.content :
      Array.isArray(payload.data?.content) ? payload.data.content :
        Array.isArray(payload) ? payload : []
  if (!Array.isArray(content)) throw new Error('Định dạng dữ liệu API không hợp lệ.')
  const map = new Map()
  for (const item of content) if (item && item.id != null) map.set(item.id, item)
  products.value = Array.from(map.values()).map(normalizeProduct)
  totalItems.value = Number(payload.totalElements) || Number(payload.data?.totalElements) || Number(payload.page?.totalElements) || content.length
  await fetchSoldCountsForPage(products.value); await fetchRatingsForPage(products.value)
}

const fetchProductsBySize = async (sizeId, params) => {
  const endpoints = ['/online-sale/by-size']
  let data = null
  for (const ep of endpoints) {
    try { const res = await apiClient.get(ep, { params: { ...params, sizeId } }); data = res?.data; if (data) break } catch {}
  }
  if (!data) throw new Error('Không thể tải sản phẩm theo size.')

  const payload = data ?? {}
  const content =
    Array.isArray(payload.content) ? payload.content :
      Array.isArray(payload.data?.content) ? payload.data.content :
        Array.isArray(payload) ? payload : []
  if (!Array.isArray(content)) throw new Error('Định dạng dữ liệu API không hợp lệ.')
  const map = new Map()
  for (const item of content) if (item && item.id != null) map.set(item.id, item)
  products.value = Array.from(map.values()).map(normalizeProduct)
  totalItems.value = Number(payload.totalElements) || Number(payload.data?.totalElements) || Number(payload.page?.totalElements) || content.length
  await fetchSoldCountsForPage(products.value); await fetchRatingsForPage(products.value)
}

const fetchProductsByColor = async (colorId, params) => {
  const endpoints = ['/online-sale/by-color']
  let data = null
  for (const ep of endpoints) {
    try { const res = await apiClient.get(ep, { params: { ...params, colorId } }); data = res?.data; if (data) break } catch {}
  }
  if (!data) throw new Error('Không thể tải sản phẩm theo màu.')

  const payload = data ?? {}
  const content =
    Array.isArray(payload.content) ? payload.content :
      Array.isArray(payload.data?.content) ? payload.data.content :
        Array.isArray(payload) ? payload : []
  if (!Array.isArray(content)) throw new Error('Định dạng dữ liệu API không hợp lệ.')
  const map = new Map()
  for (const item of content) if (item && item.id != null) map.set(item.id, item)
  products.value = Array.from(map.values()).map(normalizeProduct)
  totalItems.value = Number(payload.totalElements) || Number(payload.data?.totalElements) || Number(payload.page?.totalElements) || content.length
  await fetchSoldCountsForPage(products.value); await fetchRatingsForPage(products.value)
}

const fetchProductsByBrand = async (brandId, params) => {
  const { data } = await apiClient.get(`/online-sale/brands/${brandId}/products`, { params })
  const payload = data ?? {}
  const content =
    Array.isArray(payload.content) ? payload.content :
      Array.isArray(payload.data?.content) ? payload.data.content :
        Array.isArray(payload) ? payload : []
  if (!Array.isArray(content)) throw new Error('Định dạng dữ liệu API không hợp lệ.')
  const map = new Map()
  for (const item of content) if (item && item.id != null) map.set(item.id, item)
  products.value = Array.from(map.values()).map(normalizeProduct)
  totalItems.value = Number(payload.totalElements) || Number(payload.data?.totalElements) || Number(payload.page?.totalElements) || content.length
  await fetchSoldCountsForPage(products.value); await fetchRatingsForPage(products.value)
}

const fetchProducts = async () => {
  isLoading.value = true
  error.value = null
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }

    if (categoryId.value) { await fetchProductsByCategory(params); return }
    if (Number.isFinite(effectiveGenderId.value)) { await fetchProductsByGender(effectiveGenderId.value, params); return }
    if (Number.isFinite(effectiveSizeId.value))   { await fetchProductsBySize(effectiveSizeId.value, params);   return }
    if (Number.isFinite(effectiveColorId.value))  { await fetchProductsByColor(effectiveColorId.value, params);  return }
    if (Number.isFinite(effectiveBrandId.value))  { await fetchProductsByBrand(effectiveBrandId.value, params);  return }

    const { data } = await apiClient.get('/online-sale', { params })
    const payload = data ?? {}
    console.log('dataa: ',payload)
    const content =
      Array.isArray(payload.content) ? payload.content :
        Array.isArray(payload.data?.content) ? payload.data.content :
          Array.isArray(payload) ? payload : []
    if (!Array.isArray(content)) throw new Error('Định dạng dữ liệu API không hợp lệ.')
    const map = new Map()
    for (const item of content) if (item && item.id != null) map.set(item.id, item)
    products.value = Array.from(map.values()).map(normalizeProduct)
    totalItems.value = Number(payload.totalElements) || Number(payload.data?.totalElements) || Number(payload.page?.totalElements) || content.length
    await fetchSoldCountsForPage(products.value); await fetchRatingsForPage(products.value)
  } catch (e) {
    console.error(e)
    error.value = 'Không thể tải sản phẩm. Vui lòng thử lại sau.'
    ElMessage.error(error.value)
  } finally {
    isLoading.value = false
  }
}

/* ========== Pagination ========== */
const onPageUiChanged = () => window.scrollTo({ top: 0, behavior: 'smooth' })

/* ========== Quick view (GIỮ NGUYÊN) ========== */
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
  if (quickViewSelectedColorId.value) {
    return sp.productDetails.find(d => d.colorId===quickViewSelectedColorId.value)
  }
  return sp.productDetails[0] || null
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
  return Array.from(new Set(sizes)).sort(sizeComparator)
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

/* ========== Cart / Buy now (GIỮ NGUYÊN) ========== */
const requiresSizeSelection = (sp) => {
  const named = new Set((sp.productDetails || []).map(d => d.sizeName).filter(Boolean))
  return named.size > 0
}
const showBulkDialog = () => {
  contactDialogVisible.value = true
  ElMessage.warning(`Số lượng lớn (≥ ${NGAY_MAX_QTY} đôi). Vui lòng liên hệ nhân viên để đặt hàng.`)
}

/* Verify & Add to cart — GIỮ NGUYÊN */
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

    // Verify server-side
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
        ElMessage.error('Sản phẩm hiện không hợp lệ để mua (đã bị vô hiệu hóa hoặc không còn bán).')
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
      console.error('Lỗi khi kiểm tra trạng thái productDetail:', verifyErr)
      ElMessage.error('Không thể kiểm tra trạng thái sản phẩm. Vui lòng thử lại sau.')
      return false
    }

    // Chọn giá (giữ nguyên như bản của bạn: final price là discounted nếu hợp lệ)
    const originalPrice = Number(detail.sellPrice) || 0
    let discountedPrice = null
    if (detail.discountedPrice != null && detail.discountedPrice !== '') {
      const v = Number(detail.discountedPrice)
      discountedPrice = Number.isFinite(v) ? v : null
    } else if (detail.discountPercentage != null && Number.isFinite(Number(detail.discountPercentage))) {
      const pct = Number(detail.discountPercentage)
      discountedPrice = Math.round(originalPrice * (100 - pct) / 100)
    }
    const finalPrice = (Number.isFinite(discountedPrice) && discountedPrice > 0 && discountedPrice < originalPrice)
      ? discountedPrice
      : originalPrice

    const discountPercent = finalPrice < originalPrice && originalPrice > 0
      ? Math.round((1 - (finalPrice / originalPrice)) * 100)
      : 0

    const qty = Number(quickViewQuantity.value)
    const lineSubtotalOriginal = originalPrice * qty
    const lineSubtotalFinal    = finalPrice * qty
    const lineDiscountAmount   = lineSubtotalOriginal - lineSubtotalFinal

    const item = {
      productDetailId: detail.id,
      productId: sp.id,
      productName: sp.productName,
      image: quickViewActiveImage.value,
      color: quickViewSelectedColor.value ? quickViewSelectedColor.value.colorName : 'N/A',
      size: quickViewSelectedSize.value || 'N/A',

      quantity: qty,
      maxQuantity: Number(detail.quantity || 0),

      price: finalPrice, // giữ để tương thích

      // bổ sung các field giá (giữ nguyên như yêu cầu trước đó của bạn)
      unitPriceOriginal: originalPrice,
      unitPriceDiscounted: discountedPrice,
      unitPriceFinal: finalPrice,
      discountPercent,
      lineSubtotalOriginal,
      lineSubtotalFinal,
      lineDiscountAmount,

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
  if (Number(quickViewQuantity.value) >= NGAY_MAX_QTY) { showBulkDialog(); return }
  const ok = await handleAddToCartInModal()
  if (ok) router.push('/cart')
}

/* ========== Minor & stats (GIỮ NGUYÊN) ========== */
const changeProductImage = (p, img) => { if (p && img) { p.__hoverImage = img; p.activeImage = img } }
const restoreProductImage = (p) => { if (p) p.activeImage = p.__originalImage || p.activeImage }
const goToDetail = (id) => router.push(`/product/${id}`)

const soldCounts = ref({})
const loadingSet = new Set()
const fetchSoldCount = async (productId) => {
  const pid = Number(productId)
  if (!Number.isFinite(pid) || soldCounts.value[pid] != null || loadingSet.has(pid)) return
  loadingSet.add(pid)
  try {
    const { data } = await apiClient.get(`/online-sale/sold-quantity/product/${pid}`)
    const total = Number(data?.totalSoldQuantity ?? 0)
    soldCounts.value[pid] = Number.isFinite(total) ? total : 0
  } catch { soldCounts.value[pid] = 0 } finally { loadingSet.delete(pid) }
}

const ratingSummaries = ref({})
const ratingLoading = new Set()
const fetchRatingSingle = async (pid) => {
  const id = Number(pid)
  if (!Number.isFinite(id) || ratingSummaries.value[id] || ratingLoading.has(id)) return
  ratingLoading.add(id)
  try {
    const { data } = await apiClient.get(`/online-sale/ratings/product/${id}`)
    ratingSummaries.value[id] = { avg: Number(data?.avgRating ?? 0), count: Number(data?.totalReviews ?? 0) }
  } catch { ratingSummaries.value[id] = { avg: 0, count: 0 } } finally { ratingLoading.delete(id) }
}

/* ========== Lifecycle (GIỮ NGUYÊN) ========== */
onMounted(async () => {
  loadFavorites()
  await Promise.all([fetchBrands(), fetchColors(), fetchGenders(), fetchSizes() ])
  selectedBrandIds.value  = brandIdFromQuery.value  ? [brandIdFromQuery.value]  : []
  selectedColorIds.value  = colorIdFromQuery.value  ? [colorIdFromQuery.value]  : []
  selectedGenderIds.value = genderIdFromQuery.value ? [genderIdFromQuery.value] : []
  selectedSizeIds.value   = sizeIdFromQuery.value   ? [sizeIdFromQuery.value]   : []
  await fetchProducts()
})
watch(() => route.fullPath, async () => {
  brandName.value  = String(route.query.brandName  || '').trim()
  colorName.value  = String(route.query.colorName  || '').trim()
  genderName.value = String(route.query.genderName || '').trim()
  sizeName.value   = String(route.query.sizeName   || '').trim()
  categoryName.value = String(route.query.categoryName || '').trim()

  selectedBrandIds.value  = brandIdFromQuery.value  ? [brandIdFromQuery.value]  : []
  selectedColorIds.value  = colorIdFromQuery.value  ? [colorIdFromQuery.value]  : []
  selectedGenderIds.value = genderIdFromQuery.value ? [genderIdFromQuery.value] : []
  selectedSizeIds.value   = sizeIdFromQuery.value   ? [sizeIdFromQuery.value]   : []
  await fetchProducts()
})
watch([favorites], () => localStorage.setItem('favorites', JSON.stringify(favorites.value)), { deep: true })

/* ====== Các biến dialog thêm/đặt trước để compile bình thường (GIỮ NGUYÊN) ====== */
const preOrderDialogVisible = ref(false)
const preOrderItem = ref(null)
const preOrderAvailableColors = ref([])
const preOrderAvailableSizes = ref([])
const preOrderSelectedColorId = ref(null)
const preOrderSelectedSize = ref(null)
const preOrderStock = ref(0)
const preOrderQuantity = ref(1)
const handlePreOrderConfirm = () => { preOrderDialogVisible.value = false }

const contactDialogVisible = ref(false)
</script>

<style scoped>
/* ========= Design tokens ========= */
:root{
  --container-w: 1360px;
  --aside-w: 320px;
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
  --danger: #ff0000;
}

/* ========= Layout ========= */
.product-collection-container{
  max-width: var(--container-w);
  margin: 48px auto 60px;
  padding: 0 16px;
}
.two-columns{ align-items:flex-start; gap: var(--gap); }
.filter-aside{
  width: var(--aside-w);
  position: sticky; top: 84px; align-self:flex-start;
  padding-right: 6px;
}
.product-list-main{ max-width:100%; padding:0; }

/* ========= Header ========= */
.collection-header{
  display:flex; align-items:center; justify-content:center;
  margin: 0 0 18px 0;
}
.collection-title{
  font-size: 28px; line-height:1.2;
  font-weight:600; letter-spacing:.2px; color: var(--text-1);
}

/* ========= Filter cards ========= */
.filter-card{
  background: var(--bg-card); border: var(--border);
  border-radius: var(--radius); overflow:hidden; box-shadow: var(--shadow-1);
}
.space-top{ margin-top: 14px; }

.filter-accordion{
  width:100%; display:flex; align-items:center; justify-content:space-between; gap:10px;
  padding:14px 16px;
  font-weight:600; font-size:14px; letter-spacing:.3px;
  background:linear-gradient(180deg,#fafbff,#f6f7fb);
  border:0; outline:none; cursor:pointer;
}
.filter-accordion .chevron{ transition: transform .18s ease; }
.filter-accordion .chevron.is-open{ transform: rotate(180deg); }

.filter-body{ padding: 12px 14px 14px; }
.filter-search :deep(.el-input__wrapper){ border-radius:999px; box-shadow:none!important; }
.filter-list{ margin-top:10px; max-height:420px; overflow:auto; padding-right:4px; }

.filter-item{
  display:flex; align-items:center; gap:10px;
  padding:10px 8px; border-radius:10px; cursor:pointer; user-select:none;
  transition: background .15s ease;
}
.filter-item:hover{ background:#f4f6fb; }
.filter-checkbox{ width:18px; height:18px; accent-color: var(--primary); }
.filter-name{ font-weight:600; color:var(--text-1); flex:1; }
.filter-loading,.filter-empty{ color:var(--text-3); font-size:13px; padding:6px 2px; }
.swatch{ width:14px; height:14px; border-radius:50%; border:1px solid #e7e7ef; }

/* ========= Product card ========= */
.product-card{
  margin-bottom: var(--gap);
  background: var(--bg-card); border: var(--border);
  border-radius: var(--radius); overflow:hidden;
  box-shadow: var(--shadow-1);
  transition: transform .18s ease, box-shadow .18s ease, border-color .2s ease;
}
.product-card:hover{ transform: translateY(-4px); box-shadow: var(--shadow-2); }

.product-card__image-wrapper{
  position:relative; width:100%; aspect-ratio:1/1;
  background: var(--bg-soft); overflow:hidden; cursor:pointer;
}
.product-card__image{
  position:absolute; inset:0; width:100%; height:100%; object-fit:cover;
  transition: transform .28s ease;
}
.product-card:hover .product-card__image{ transform: scale(1.035); }

/* ========= Badges ========= */
.product-card__discount-badge{
  position:absolute; top:10px; left:10px;
  background-color:#ff0000 !important;
  color:#ffffff !important;
  border:0 !important;
  padding:4px 8px; border-radius:999px;
  font-size:12px; font-weight:600;
  z-index:2;
  box-shadow:none !important;
  filter:none !important;
  mix-blend-mode:normal !important;
  backdrop-filter:none !important;
}

.product-card__quick-view-btn{
  position:absolute; top:12px; right:12px; z-index:2;
  background:rgba(255,255,255,.96)!important;
  border:1px solid #f0f0f5 !important;
  box-shadow: var(--shadow-1);
  opacity:0; transition: opacity .22s ease;
}
.product-card:hover .product-card__quick-view-btn{ opacity:1; }

/* ========= Info ========= */
.product-card__info{ padding:14px 14px 16px; text-align:left; }
.product-card__name{
  font-size:15px; font-weight:600; color:var(--text-1);
  margin:0 0 6px 0; line-height:1.35; height:42px; overflow:hidden;
  text-overflow:ellipsis; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical;
  transition:color .15s ease; cursor:pointer;
}
.product-card__name:hover{ color: var(--accent); }

.product-card__price-container{
  display:flex; align-items:baseline; gap:10px; height:24px; margin-bottom:8px;
}
.discounted-price{ font-size:16px; font-weight:600; color:var(--danger); }
.original-price{ font-size:13px; color:#9aa0b4; text-decoration:line-through; font-weight:400; }
.normal-price{ font-size:16px; font-weight:600; color:var(--text-1); }

/* ========= Color dots ========= */
.product-card__colors{ display:flex; gap:7px; height:16px; margin-top:10px; }
.product-card__color-dot{
  width:16px; height:16px; border-radius:50%;
  border:1px solid #e6e8f0; cursor:pointer;
  transition: transform .15s ease, box-shadow .15s ease;
}
.product-card__color-dot:hover{ transform: translateY(-1px) scale(1.06); box-shadow:0 2px 6px rgba(0,0,0,.12); }

/* ========= Sold badge ========= */
.product-card__sold-badge{
  position:absolute; right:10px; bottom:10px; z-index:2;
  background: rgba(20,22,26,.75); color:#fff; font-weight:600;
  padding:6px 10px; border-radius:10px; font-size:12px; line-height:1;
  backdrop-filter: blur(2px);
}

/* ========= Rating ========= */
.rating-row{ margin-top:10px; display:flex; align-items:center; justify-content:space-between; }
.rating-text{ font-size:12px; color:var(--text-2); white-space:nowrap; }

/* ========= Quick view dialog ========= */
:deep(.el-dialog.quick-view-dialog){
  border-radius:16px; background:#fff; box-shadow: var(--shadow-2);
}
:deep(.quick-view-dialog .el-dialog__header){ padding:0; position:absolute; top:14px; right:14px; z-index:1; }
:deep(.quick-view-dialog .el-dialog__headerbtn .el-icon){ font-size:22px; color:#6b7280; }
:deep(.quick-view-dialog .el-dialog__body){ padding:32px 34px; }

.quick-view__main-image{
  width:100%; height:auto; aspect-ratio:1/1; object-fit:cover;
  border-radius:12px; border: var(--border);
}
.quick-view__info{ display:flex; flex-direction:column; height:100%; }
.quick-view__title{ font-size:24px; font-weight:700; margin:0 0 8px; color:var(--text-1); }
.quick-view__color-display{ font-size:14px; margin-bottom:10px; color:var(--text-2); height:20px; font-weight:600; }

.quick-view__color-selector{ display:flex; flex-wrap:wrap; gap:10px; margin-bottom:16px; }
.selector-label{ font-size:13px; font-weight:600; margin:0 0 8px; color:var(--text-2); text-transform:uppercase; letter-spacing:.4px; }

.color-thumbnail{
  cursor:pointer; border:2px solid #eceef6; padding:2px;
  width:58px; height:58px; border-radius:10px;
  transition: border-color .18s ease, transform .18s ease; background:#fff;
}
.color-thumbnail:hover{ transform: translateY(-2px); }
.color-thumbnail.is-selected{ border-color:var(--primary); box-shadow:0 0 0 3px rgba(0,0,0,.06); }
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

/* ========= Contact dialog ========= */
.contact-block { font-size:14px; color:var(--text-1); }
.contact-list { margin:10px 0 0; padding-left:18px; }
.contact-list li { margin:4px 0; }
.contact-list a { color: var(--accent); text-decoration: none; }
.contact-list a:hover { text-decoration: underline; }

/* ========= Pagination ========= */
.pagination-container{ display:flex; justify-content:center; margin-top:22px; }
:deep(.el-pagination){
  padding:10px 14px; background:#fff; border: var(--border);
  border-radius:12px; box-shadow: var(--shadow-1);
}
:deep(.el-pagination.is-background .el-pager li.is-active){ font-weight:600; }

/* ========= Transitions ========= */
.slide-fade-enter-active,.slide-fade-leave-active{ transition:all .18s ease; }
.slide-fade-enter-from,.slide-fade-leave-to{ opacity:0; transform: translateY(-6px); }

@media (max-width:1400px){ :root{ --aside-w:300px; } }
@media (max-width:1200px){ :root{ --aside-w:280px; } }
@media (max-width:992px){
  .filter-aside{ display:none; }
  .product-collection-container{ padding:0 10px; }
  .collection-title{ font-size:24px; }
}
</style>

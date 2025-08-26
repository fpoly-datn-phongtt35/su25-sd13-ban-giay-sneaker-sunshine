<template>
  <div class="orders-review-page">
    <!-- Header / Filters -->
    <div class="toolbar">
      <div class="left">
        <el-input
          v-if="show"
          v-model="filters.keyword"
          placeholder="Tìm theo mã đơn / tên sản phẩm..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          class="w-72"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="đến"
          start-placeholder="Từ ngày"
          end-placeholder="Đến ngày"
          value-format="YYYY-MM-DD"
          :unlink-panels="true"
          @change="handleSearch"
          class="ml-3"
        />
      </div>

      <div class="right">
        <el-select
          v-model="filters.onlyUnrated"
          @change="handleSearch"
          class="w-56"
          clearable
        >
          <el-option :value="true" label="Chỉ hiển thị sản phẩm chưa đánh giá" />
          <el-option :value="false" label="Hiển thị tất cả sản phẩm" />
        </el-select>

        <el-button type="primary" class="ml-3" @click="handleSearch">
          <el-icon class="mr-1"><Filter /></el-icon>
          Lọc
        </el-button>
      </div>
    </div>

    <!-- Empty state -->
    <el-empty
      v-if="orders.length === 0 && !loading"
      description="Không có đơn hàng giao thành công phù hợp"
      :image-size="120"
    />

    <!-- Loading -->
    <div v-if="loading" class="loading-box">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- Orders list -->
    <div v-else class="orders-list">
      <el-card
        v-for="order in orders"
        :key="order.invoiceId"
        class="order-card"
        shadow="hover"
      >
        <div class="order-header">
          <div class="meta">
            <span class="code">
              Mã đơn: <b>{{ order.invoiceCode || order.code || order.invoiceId }}</b>
            </span>
            <span class="dot">•</span>
            <span class="date">
              Ngày: {{ formatDate(order.deliveredAt || order.completedAt || order.createdDate) || '—' }}
            </span>
            <span class="dot">•</span>
            <el-tag :type="order.deliveredAt ? 'success' : 'info'" size="small">
              {{ order.deliveredAt ? 'GIAO THÀNH CÔNG' : 'ĐƠN HOÀN TẤT' }}
            </el-tag>
          </div>
          <div class="total">
            Tổng: <b>{{ formatCurrency(order.finalAmount ?? order.totalPaid ?? order.totalAmount) }}</b>
          </div>
        </div>

        <div class="items">
          <div
            v-for="item in visibleItems(order)"
            :key="item.__uid"
            class="item-row"
          >
            <img :src="item.imageUrl" class="thumb" alt="product" />
            <div class="info">
              <div class="name" :title="item.productName">{{ item.productName }}</div>
              <div class="variant">
                {{ item.colorName || '—' }} • {{ item.sizeName || '—' }} • SL: {{ item.quantity ?? 1 }}
              </div>
              <div class="price">{{ formatCurrency(item.displayPrice) }}</div>
            </div>

            <div class="actions">
              <p class="status" :class="{ rated: item.isRated }">
                {{ item.isRated ? 'Đã đánh giá' : 'Chưa đánh giá' }}
              </p>

              <!-- ĐÃ ĐÁNH GIÁ: chỉ hiện tag, không có nút -->
              <el-tag v-if="item.isRated" type="success" effect="light" size="small">
                ĐÃ ĐÁNH GIÁ
              </el-tag>

              <!-- CHƯA ĐÁNH GIÁ: cho bấm mở dialog -->
              <el-button
                v-else
                type="primary"
                plain
                size="small"
                @click="openReviewDialog(item, order)"
              >
                <el-icon class="mr-1"><StarFilled /></el-icon> Đánh giá
              </el-button>
            </div>
          </div>

          <!-- Expand more -->
          <div v-if="(order.invoiceDetail?.length || 0) > maxItemsPerOrder" class="more">
            <el-link type="primary" :underline="false" @click="toggleExpand(order)">
              {{ order.__expanded ? 'Thu gọn' : `Xem thêm ${order.invoiceDetail.length - maxItemsPerOrder} sản phẩm` }}
            </el-link>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Dialog đánh giá -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="Đánh giá sản phẩm"
      width="600px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="mb-4">
        <div class="fw-bold mb-2">
          {{ selectedProduct.productName || 'Sản phẩm' }}
          <span v-if="selectedProduct.productId"> (PID: {{ selectedProduct.productId }})</span>
          <span v-if="selectedProduct.invoiceId" class="text-muted"> • Đơn: {{ selectedProduct.invoiceId }}</span>
        </div>

        <el-alert
          v-if="!selectedProduct.productId"
          type="warning"
          show-icon
          title="Thiếu productId"
          description="Bạn có thể nhập đánh giá, nhưng cần productId hợp lệ để gửi."
          class="mb-2"
        />

        <el-rate
          v-model="selectedProduct.rating"
          :max="5"
          show-score
          score-template="{value} sao"
        />
        <el-input
          v-model="selectedProduct.comment"
          type="textarea"
          :rows="4"
          placeholder="Nhập nhận xét của bạn"
          class="mt-3"
          maxlength="2000"
          show-word-limit
        />
      </div>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">Hủy</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          :disabled="!canSubmit || submitLoading"
          @click="submitReview"
        >
          Gửi đánh giá
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Filter, StarFilled } from '@element-plus/icons-vue'
import apiClient from '@/utils/axiosInstance'

/* ================== STATE ================== */
const loading = ref(false)
const submitLoading = ref(false)
const orders = ref([]) // mỗi đơn có invoiceDetail[]
const maxItemsPerOrder = 3

const filters = ref({
  keyword: '',
  dateRange: null, // ['YYYY-MM-DD','YYYY-MM-DD']
  onlyUnrated: false,
})

/* ===== Dialog & Selected Product ===== */
const reviewDialogVisible = ref(false)
const selectedProduct = ref({
  invoiceId: null,
  productId: null,
  productName: '',
  rating: 0,
  comment: '',
})

const show = ref(false)

/* ================== HELPERS ================== */
function formatDate(d) {
  try {
    if (!d) return ''
    const dt = typeof d === 'string' || typeof d === 'number' ? new Date(d) : d
    return dt.toLocaleDateString('vi-VN')
  } catch {
    return d ?? ''
  }
}
function formatCurrency(n) {
  return (n ?? 0).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' })
}
function asArray(payload) {
  if (!payload) return []
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload.data)) return payload.data
  if (Array.isArray(payload.content)) return payload.content
  return []
}

const PLACEHOLDER_IMG =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="72" height="72">
       <rect width="100%" height="100%" fill="#f2f3f5"/>
       <text x="50%" y="50%" font-size="10" fill="#999" text-anchor="middle" alignment-baseline="middle">No Image</text>
     </svg>`
  )

function bytesToBase64(arr) {
  try {
    const chunk = 0x8000
    let binary = ''
    for (let i = 0; i < arr.length; i += chunk) {
      const sub = arr.slice(i, i + chunk)
      binary += String.fromCharCode.apply(null, sub)
    }
    return btoa(binary)
  } catch {
    return null
  }
}

function toImageUrl(img) {
  if (!img) return PLACEHOLDER_IMG
  if (typeof img === 'string') {
    if (img.startsWith('http') || img.startsWith('data:image')) return img
    return `data:image/jpeg;base64,${img}`
  }
  if (Array.isArray(img)) {
    const base64 = bytesToBase64(img)
    return base64 ? `data:image/jpeg;base64,${base64}` : PLACEHOLDER_IMG
  }
  if (img?.url) return img.url
  if (img?.imageUrl) return img.imageUrl
  if (img?.data && Array.isArray(img.data)) {
    const base64 = bytesToBase64(img.data)
    return base64 ? `data:image/jpeg;base64,${base64}` : PLACEHOLDER_IMG
  }
  return PLACEHOLDER_IMG
}

/* ================== RENDER UTILS ================== */
function visibleItems(order) {
  const items = order?.invoiceDetail || []
  if (!order.__expanded && items.length > maxItemsPerOrder) {
    return items.slice(0, maxItemsPerOrder)
  }
  return items
}
function toggleExpand(order) {
  order.__expanded = !order.__expanded
}

/* ================== FETCH ================== */
async function fetchOrders() {
  loading.value = true
  try {
    const params = {
      onlyUnrated: filters.value.onlyUnrated ? 1 : 0,
      keyword: (filters.value.keyword || '').trim() || null,
      dateFrom: filters.value.dateRange?.[0] || null,
      dateTo:   filters.value.dateRange?.[1] || null,
    }

    const { data } = await apiClient.get('/online-sale/delivered', { params })
    const list = asArray(data)

    let uid = 0
    orders.value = list.map(inv => {
      const rawItems = inv.invoiceDetail || inv.items || []
      const items = rawItems.map(d => {
        const displayPrice =
          d.discountedPrice ?? d.sellPrice ?? d.finalTotalPrice ?? d.totalPrice ?? d.price ?? 0

        const isRated = (d.rated === true) || (d.rate != null)

        const productId =
          d.productId ??
          d.product_id ??
          d.product?.id ??
          d.product?.productId ??
          d.productDetail?.productId ??
          d.productDetail?.product?.id ??
          d.idProduct ??
          d.productID ??
          null

        const imageCandidate =
          d.imageUrl ?? d.image ?? d.thumbnail ?? d.product?.imageUrl ?? d.productDetail?.image

        return {
          __uid: `${inv.invoiceId ?? inv.id ?? 'inv'}_${uid++}`,
          ...d, // giữ gốc

          // chuẩn hoá cho UI
          productId,
          isRated,
          displayPrice,
          imageUrl: toImageUrl(imageCandidate),
          invoiceId: inv.invoiceId ?? inv.id ?? null,
          productName:
            d.productName ??
            d.name ??
            d.product?.productName ??
            d.productDetail?.product?.productName ??
            'Sản phẩm',
          colorName: d.colorName ?? d.productDetail?.colorName ?? null,
          sizeName:  d.sizeName  ?? d.productDetail?.sizeName  ?? null,
          quantity:  d.quantity ?? 1,
        }
      })

      return {
        ...inv,
        invoiceId: inv.invoiceId ?? inv.id ?? null,
        invoiceCode: inv.invoiceCode ?? inv.code ?? String(inv.invoiceId ?? ''),
        deliveredAt: inv.deliveredAt ?? inv.completedAt ?? inv.createdDate ?? null,
        finalAmount: inv.finalAmount ?? inv.totalAmount ?? inv.totalPaid ?? 0,
        invoiceDetail: items,
        __expanded: false,
      }
    })
  } catch (e) {
    console.error('[fetchOrders] error =', e?.response?.data || e)
    orders.value = []
    ElMessage.error('Không thể tải danh sách đơn hàng')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchOrders()
}

/* ================== DIALOG HANDLERS ================== */
const canSubmit = computed(() => {
  const p = selectedProduct.value
  return !!p.productId && !!p.invoiceId && !!p.rating && !!(p.comment || '').trim()
})

function openReviewDialog(item, order) {
  if (item.isRated) {
    ElMessage.info('Sản phẩm này đã được đánh giá. Mỗi sản phẩm chỉ được đánh giá 1 lần.')
    return
  }
  selectedProduct.value = {
    invoiceId: order?.invoiceId ?? item.invoiceId ?? null,
    productId: item.productId ?? null,
    productName: item.productName,
    rating: 0,
    comment: '',
  }
  reviewDialogVisible.value = true
  if (!selectedProduct.value.productId) {
    ElMessage.warning('Thiếu productId — bạn có thể nhập trước, cần productId hợp lệ để gửi.')
  }
}

/* ================== SUBMIT REVIEW ================== */
async function submitReview() {
  const p = selectedProduct.value

  // Phòng hờ: chặn nếu đã đánh giá (tránh double submit do race condition)
  const itemAlreadyRated = orders.value.some(o =>
    (o.invoiceId === p.invoiceId) &&
    o.invoiceDetail.some(it => it.productId === p.productId && (it.isRated || it.rate != null))
  )
  if (itemAlreadyRated) {
    ElMessage.info('Sản phẩm này đã được đánh giá. Mỗi sản phẩm chỉ được đánh giá 1 lần.')
    reviewDialogVisible.value = false
    return
  }

  if (!p.productId) return ElMessage.warning('Thiếu productId — không thể gửi đánh giá.')
  if (!p.invoiceId) return ElMessage.warning('Thiếu invoiceId — không thể gửi đánh giá.')
  if (!p.rating || !p.comment?.trim()) return ElMessage.warning('Vui lòng nhập đủ đánh giá và bình luận.')

  const payload = {
    invoiceId: p.invoiceId,
    productId: p.productId,
    rate: p.rating,
    comment: p.comment.trim(),
  }

  try {
    submitLoading.value = true
    await apiClient.post('/favorites', payload)
    ElMessage.success('Đã gửi đánh giá thành công!')
    reviewDialogVisible.value = false

    // đồng bộ local state -> đánh dấu đã đánh giá
    orders.value.forEach(o =>
      o.invoiceDetail.forEach(it => {
        if (it.productId === p.productId && it.invoiceId === p.invoiceId) {
          it.rate = p.rating
          it.comment = p.comment.trim()
          it.isRated = true
          it.rated = true
        }
      })
    )
  } catch (error) {
    console.error('[submitReview] error =', error?.response?.data || error)
    ElMessage.error('Gửi đánh giá thất bại.')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
/* Layout tổng thể */
.orders-review-page {
  max-width: 980px;
  margin: 16px auto 36px;
  padding: 0 12px;
  color: #222;
}

/* Toolbar */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.toolbar .left,
.toolbar .right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Loading */
.loading-box {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

/* Danh sách đơn */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-card {
  border-radius: 14px;
  overflow: hidden;
}

/* Header đơn */
.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.order-header .meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 13px;
}
.order-header .meta .code b,
.order-header .total b {
  color: #111;
}
.order-header .dot {
  opacity: .6;
}

/* Items */
.items {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 8px;
}
.item-row {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 10px 8px;
  border: 1px solid #eee;
  border-radius: 12px;
}
.item-row .thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 10px;
  display: block;
  background: #f7f8fa;
}
.item-row .info { min-width: 0; }
.item-row .info .name {
  font-weight: 600;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-row .info .variant { font-size: 13px; color: #666; }
.item-row .info .price { font-weight: 600; margin-top: 2px; }
.item-row .actions {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  flex-direction: column;
}
.item-row .actions .status {
  font-size: 12px;
  margin: 0 0 2px 0;
  color: #666;
}
.item-row .actions .status.rated {
  color: #2ecc71;
  font-weight: 600;
}

/* More link */
.more {
  display: flex;
  justify-content: center;
  margin-top: 6px;
}

/* Utilities */
.w-72 { width: 18rem; }
.ml-3 { margin-left: 12px; }
.mr-1 { margin-right: 4px; }
.mb-2 { margin-bottom: 8px; }
.mt-3 { margin-top: 12px; }
.fw-bold { font-weight: 600; }
.text-muted { color: #777; }
</style>

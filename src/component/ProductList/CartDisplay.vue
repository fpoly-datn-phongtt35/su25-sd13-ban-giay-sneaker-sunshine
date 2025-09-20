<template>
  <div class="cart-page-container">
    <div class="cart-header">
      <h2 class="cart-title">Giỏ hàng của bạn</h2>
      <span v-if="cartItems.length" class="item-count">({{ totalQty }} sản phẩm)</span>
    </div>

    <el-alert
      v-if="exceedsBulkRule && cartItems.length"
      type="warning"
      show-icon
      class="bulk-alert"
      :closable="false"
      title="Đơn online không hỗ trợ số lượng lớn"
      description="Số lượng mỗi sản phẩm tối đa 9. Nếu bạn muốn đặt từ 10 đôi trở lên, vui lòng liên hệ nhân viên để được hỗ trợ."
    />

    <div v-if="cartItems.length" class="cart-layout">
      <div class="cart-items-list">
        <div
          v-for="item in cartItems"
          :key="`${item.productDetailId ?? item.id ?? ''}-${item.color ?? ''}-${item.size ?? ''}`"
          class="cart-item-card"
        >
          <div class="item-image">
            <el-image :src="item.image || '/no-image.jpg'" fit="cover" class="cart-thumb" />
          </div>

          <div class="item-details">
            <div class="item-info">
              <span class="product-name">{{ item.productName }}</span>
              <span class="product-meta">
                <span v-if="item.color">Màu: {{ item.color }}</span>
                <span v-if="item.size"> | Size: {{ item.size }}</span>
                <span v-if="Number(item.maxQuantity) > 0" class="stock-hint"> | Tồn: {{ item.maxQuantity }}</span>
              </span>
            </div>

            <div class="item-price">
              <!-- Nếu có giá giảm hợp lệ thì hiển thị sellPrice (gạch) + discountedPrice (nổi bật) -->
              <template v-if="item.discountedPrice>0">
                <span class="original-price" aria-hidden="true">
                  {{ formatNumber(getSellPrice(item)) }} <span class="currency-small">đ</span>
                </span>

                <span class="final-price" aria-label="Giá sau giảm">
                  <span class="amount">{{ formatNumber(getDiscountedPrice(item)) }}</span>
                  <span class="currency">đ</span>
                </span>
              </template>

              <!-- Ngược lại chỉ hiển thị sellPrice -->
              <template v-else>
              <span>{{item.discountedPrice}}</span>
              </template>
            </div>
          </div>

          <div class="item-controls">
            <el-input-number
              v-model="item.quantity"
              :min="1"
              :max="getRowMax(item)"
              @change="() => updateQuantity(item)"
              size="small"
              class="quantity-input"
            />
            <div class="item-total-price">
              {{ formatPrice(getDisplayedPrice(item) * (Number(item.quantity) || 0)) }}
            </div>
          </div>

          <div class="item-actions">
            <el-button type="danger" :icon="Delete" text circle @click="removeItem(item)" />
          </div>
        </div>
      </div>

      <div class="cart-summary-card">
        <h3 class="summary-title">Tóm tắt đơn hàng</h3>
        <div class="summary-row">
          <span>Tạm tính ({{ totalQty }} sản phẩm)</span>
          <span>{{ formatPrice(calculateTotal) }}</span>
        </div>
        <div class="summary-row total-row">
          <span class="total-label">Tổng cộng</span>
          <span class="total-price-display">{{ formatPrice(calculateTotal) }}</span>
        </div>

        <div class="summary-actions">
          <el-tooltip
            v-if="exceedsBulkRule"
            effect="dark"
            content="Số lượng lớn (≥ 10). Vui lòng liên hệ nhân viên để đặt hàng."
            placement="top"
            class="full-width-tooltip"
          >
            <el-button type="success" :disabled="true" @click="goToCheckout" class="checkout-btn" size="large">
              Thanh toán
            </el-button>
          </el-tooltip>

          <el-button v-else type="success" @click="goToCheckout" class="checkout-btn" size="large">
            Thanh toán
          </el-button>
        </div>

        <div class="continue-shopping">
          <el-button type="info" link @click="router.push('/')">
            <el-icon><Back /></el-icon> Tiếp tục mua sắm
          </el-button>
          <el-button type="danger" link @click="confirmClearAll">
            <el-icon><Delete /></el-icon> Xóa tất cả
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="Giỏ hàng của bạn đang trống" image-size="200">
      <el-button type="primary" @click="router.push('/')" size="large">
        Bắt đầu mua sắm ngay!
      </el-button>
    </el-empty>

    <el-dialog v-model="contactDialogVisible" title="Liên hệ nhân viên để đặt số lượng lớn" width="520px">
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
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Back } from '@element-plus/icons-vue'
import { getCart, saveCart, clearCart } from '@/utils/cart'
import axios from 'axios'

/* cấu hình */
const NGAY_MAX_QTY = 10
const RULE_PER_LINE_MAX = NGAY_MAX_QTY - 1
const hotline = '09xx xxx xxx'
const zaloLink = 'https://zalo.me/09xxxxxxxx'
const facebookLink = 'https://facebook.com/yourpage'

const contactDialogVisible = ref(false)
const showBulkDialog = () => {
  contactDialogVisible.value = true
  ElMessage.warning(`Số lượng lớn (≥ ${NGAY_MAX_QTY} đôi). Vui lòng liên hệ nhân viên để đặt hàng.`)
}

const router = useRouter()
const cartItems = ref([])
const isApplyingRemote = ref(false)
const lastNetworkError = ref(false)

/* ---- Helpers giá ----
   parsePriceToNumber: an toàn với chuỗi đã format như "360.000 đ"
*/
const parsePriceToNumber = (v) => {
  if (v === null || v === undefined) return null
  if (typeof v === 'number') return Number.isFinite(v) ? Math.trunc(v) : null
  if (typeof v === 'string') {
    // loại bỏ mọi ký tự không phải số (giữ dấu - nếu có)
    const cleaned = v.replace(/[^\d\-]/g, '')
    if (cleaned === '') return null
    const n = Number(cleaned)
    return Number.isFinite(n) ? Math.trunc(n) : null
  }
  return null
}

/* Lấy sellPrice (giá gốc để hiển thị gạch khi có discount).
   Ưu tiên các trường thường dùng: sellPrice -> price -> listPrice -> msrp
*/
const getSellPrice = (item) => {
  if (!item) return 0
  return (
    parsePriceToNumber(item.sellPrice) ??
    parsePriceToNumber(item.price) ??
    parsePriceToNumber(item.listPrice) ??
    parsePriceToNumber(item.msrp) ??
    0
  )
}

/* Lấy discountedPrice (nếu backend có đặt tên khác, thêm vào đây)
   ưu tiên: discountedPrice -> discountPrice -> promoPrice -> salePrice
*/
const getDiscountedPrice = (item) => {
  if (!item) return null
  return (
    parsePriceToNumber(item.discountedPrice) ??
    parsePriceToNumber(item.discountPrice) ??
    parsePriceToNumber(item.promoPrice) ??
    parsePriceToNumber(item.salePrice) ??
    null
  )
}

/* Quy tắc hiển thị:
   - nếu discountedPrice tồn tại, > 0, và < sellPrice => show both (sellPrice gạch)
   - nếu discountedPrice === 0 || null || >= sellPrice => chỉ show sellPrice
*/
const shouldShowDiscount = (item) => {
  const sell = getSellPrice(item)
  const disc = getDiscountedPrice(item)
  if (disc === null) return false
  if (disc <= 0) return false
  if (sell <= 0) {
    // nếu không có sellPrice nhưng có discountedPrice > 0 -> vẫn hiển thị discountedPrice (business may vary)
    return true
  }
  return disc < sell
}

/* Giá hiển thị để tính tổng (nếu có discount hợp lệ dùng discounted, else sell) */
const getDisplayedPrice = (item) => {
  return shouldShowDiscount(item) ? getDiscountedPrice(item) : getSellPrice(item)
}

/* format helpers */
const formatNumber = (n) => {
  const num = Number(n) || 0
  return num.toLocaleString('vi-VN')
}
const formatPrice = (n) => {
  const num = Number(n) || 0
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num)
}

/* ===== Cart logic (load/save/normalize) - giữ nguyên logic trước đó ===== */
const safeInt = (v, def = 0) => {
  const n = Number(v)
  return Number.isFinite(n) ? Math.trunc(n) : def
}

const getRowMax = (row) => {
  const stock = Number.isFinite(Number(row?.maxQuantity)) && Number(row.maxQuantity) >= 0 ? Number(row.maxQuantity) : Infinity
  return Math.max(1, Math.min(RULE_PER_LINE_MAX, stock))
}

function makeKey(item) {
  return `${item?.productDetailId ?? item?.id ?? ''}::${item?.productId ?? ''}::${item?.color ?? ''}::${item?.size ?? ''}`
}
function isCartEqual(a, b) {
  if (!Array.isArray(a) || !Array.isArray(b)) return false
  if (a.length !== b.length) return false
  const mapA = new Map()
  for (const it of a) mapA.set(makeKey(it), safeInt(it.quantity, 0))
  for (const it of b) {
    const k = makeKey(it)
    if (!mapA.has(k)) return false
    if (mapA.get(k) !== safeInt(it.quantity, 0)) return false
  }
  return true
}

let isLoading = false
const loadCartFromService = async () => {

  if (isLoading) return
  isLoading = true
  try {
    const res = await getCart()
    console.log('res: ',res)
    let arr = []
    if (Array.isArray(res)) {
      arr = res
      lastNetworkError.value = false
    } else if (res && Array.isArray(res.cart)) {
      arr = res.cart
      lastNetworkError.value = !!res.networkError
    } else {
      arr = Array.isArray(res) ? res : []
      lastNetworkError.value = true
    }
    const normalized = (Array.isArray(arr) ? arr : []).map(it => ({
      ...it,
      quantity: Math.max(1, safeInt(it.quantity, 1)),
      status: Number(it.status ?? 1),
      productDetailId: it.productDetailId ?? (it.productDetail && it.productDetail.id)
    }))

    console.log('cart: ',cartItems.value)
    if (isCartEqual(normalized, cartItems.value)) {
      isLoading = false
      return
    }


    isApplyingRemote.value = true
    cartItems.value = normalized
    console.log('cart: ',cartItems.value)
    normalizeAllRows(false)
    await nextTick()
    isApplyingRemote.value = false

    if (lastNetworkError.value) {
      ElMessage.warning('Không thể xác thực sản phẩm với server — dùng dữ liệu cục bộ.')
    }
  } catch (err) {
    console.error('loadCartFromService error', err)
    ElMessage.error('Lỗi khi tải giỏ hàng.')
  } finally {
    isLoading = false
  }
}

const normalizeAllRows = (showDialog = true) => {
  if (!Array.isArray(cartItems.value)) cartItems.value = []
  let touched = false
  cartItems.value = cartItems.value.map((row) => {
    const qty0 = safeInt(row.quantity, 1)
    const rowMax = getRowMax(row)
    let qty = Math.max(1, qty0)
    if (qty >= NGAY_MAX_QTY) {
      qty = rowMax
      touched = true
    } else if (qty > rowMax) {
      qty = rowMax
      touched = true
    }
    return { ...row, quantity: qty }
  })
  if (touched) {
    if (showDialog) showBulkDialog()
    try { saveCart(cartItems.value) } catch (e) { console.error(e) }
  }
}

/* ===== Computed ===== */
const totalQty = computed(() =>
  Array.isArray(cartItems.value) ? cartItems.value.reduce((s, it) => s + safeInt(it.quantity, 0), 0) : 0
)

const exceedsBulkRule = computed(() =>
  totalQty.value >= NGAY_MAX_QTY ||
  (Array.isArray(cartItems.value) && cartItems.value.some(i => safeInt(i.quantity, 0) >= NGAY_MAX_QTY))
)

const calculateTotal = computed(() =>
  Array.isArray(cartItems.value)
    ? cartItems.value.reduce((sum, it) => sum + (getDisplayedPrice(it) * safeInt(it.quantity, 0)), 0)
    : 0
)

/* ===== Actions ===== */
const updateQuantity = (item) => {
  if (!item) return
  item.quantity = Math.max(1, safeInt(item.quantity, 1))
  const rowMax = getRowMax(item)
  if (item.quantity > rowMax) {
    item.quantity = rowMax
    ElMessage.warning(`Số lượng tối đa cho phép là ${rowMax}.`)
  }
  if (item.quantity >= NGAY_MAX_QTY) {
    item.quantity = getRowMax(item)
    showBulkDialog()
  }
  if (!isApplyingRemote.value) {
    try { saveCart(cartItems.value) } catch (e) { console.error(e) }
  }
}

const removeItem = (item) => {
  if (!item) return
  ElMessageBox.confirm(`Xóa "${item.productName}" khỏi giỏ?`, 'Xác nhận', {
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
    type: 'warning'
  }).then(() => {
    cartItems.value = cartItems.value.filter(i =>
      !(i.productId === item.productId && i.color === item.color && i.size === item.size)
    )
    if (!isApplyingRemote.value) try { saveCart(cartItems.value) } catch (e) { console.error(e) }
    ElMessage.success('Đã xóa sản phẩm!')
  }).catch(() => {})
}

const confirmClearAll = () => {
  ElMessageBox.confirm('Xóa tất cả sản phẩm khỏi giỏ?', 'Xác nhận', {
    confirmButtonText: 'Xóa tất cả',
    cancelButtonText: 'Hủy',
    type: 'warning'
  }).then(() => {
    clearCart()
    cartItems.value = []
    ElMessage.success('Đã xóa tất cả!')
  }).catch(() => {})
}

const goToCheckout = async () => {
  if (!Array.isArray(cartItems.value) || cartItems.value.length === 0) {
    ElMessage.warning('Giỏ hàng đang trống!')
    return
  }
  if (exceedsBulkRule.value) {
    showBulkDialog()
    return
  }

  const ids = Array.from(new Set(
    cartItems.value
      .map(it => Number(it?.productDetailId ?? it?.productDetail?.id ?? it?.productDetailId))
      .filter(n => Number.isFinite(n) && n > 0)
  ))

  if (ids.length === 0) {
    ElMessage.error('Không tìm thấy productDetail id để kiểm tra. Vui lòng kiểm tra lại giỏ hàng.')
    window.location.reload()
    return
  }

  const idsCsv = ids.join(',')
  const url = `http://localhost:8080/api/online-sale/verify-list-pdDetail/${idsCsv}`

  try {
    const res = await axios.get(url, { timeout: 10000 })
    const payload = res?.data
    const arr = Array.isArray(payload)
      ? payload
      : (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload?.content) ? payload.content : null))

    if (!Array.isArray(arr)) {
      console.error('verify API returned unexpected shape', payload)
      window.location.reload()
      ElMessage.error('Máy chủ trả về dữ liệu không đúng. Vui lòng thử lại sau.')
      return
    }

    const statusMap = {}
    for (const pd of arr) {
      const id = Number(pd?.id ?? pd?.productDetailId ?? (pd?.productDetail && pd.productDetail.id))
      const status = pd?.status ?? pd?.statusCode ?? pd?.active ?? undefined
      if (Number.isFinite(id)) statusMap[id] = (Number.isFinite(Number(status)) ? Number(status) : undefined)
    }

    const invalidItems = []
    for (const it of cartItems.value) {
      const pid = Number(it?.productDetailId ?? it?.productDetail?.id ?? it?.productDetailId)
      const s = statusMap[pid]
      if (!Number.isFinite(s) || Number(s) !== 1) {
        invalidItems.push({
          productName: it.productName ?? `(id:${pid})`,
          productDetailId: pid,
          serverStatus: s
        })
      }
    }

    if (invalidItems.length > 0) {
      const names = invalidItems.map(i => `${i.productName || ''} (id:${i.productDetailId})`).join(', ')
      ElMessage.error(`Một hoặc nhiều sản phẩm không hợp lệ để mua: ${names}. Vui lòng kiểm tra giỏ hàng.`)
      console.warn('Invalid items for checkout:', invalidItems)
      window.location.reload()
      return
    }

    router.push('/checkout')
  } catch (err) {
    console.error('Lỗi khi kiểm tra trạng thái productDetail:', err)
    ElMessage.error('Không thể kiểm tra trạng thái sản phẩm. Vui lòng thử lại sau.')
    window.location.reload()
    return
  }
}

/* watchers & events */
const stopWatch = watch(cartItems, (newVal) => {
  if (isApplyingRemote.value) return
  try { saveCart(Array.isArray(newVal) ? newVal : []) } catch (e) { console.error(e) }
}, { deep: true })

function onCartUpdatedHandler(e) {
  try {
    const incoming = e?.detail?.cart
    if (!Array.isArray(incoming)) {
      isApplyingRemote.value = true
      loadCartFromService().finally(() => { isApplyingRemote.value = false })
      return
    }

    const normalized = incoming.map(it => ({
      ...it,
      quantity: Math.max(1, safeInt(it.quantity, 1)),
      productDetailId: it.productDetailId ?? (it.productDetail && it.productDetail.id)
    }))

    if (isCartEqual(normalized, cartItems.value)) return

    isApplyingRemote.value = true
    cartItems.value = normalized
    normalizeAllRows(false)
    nextTick(() => { isApplyingRemote.value = false })
  } catch (err) {
    console.error('onCartUpdatedHandler', err)
  }
}

function onCartRemovedHandler(e) {
  const removed = e?.detail?.removed ?? []
  if (Array.isArray(removed) && removed.length > 0) {
    ElMessage.info(`${removed.length} sản phẩm đã bị gỡ khỏi giỏ (không còn tồn/không hợp lệ).`)
    loadCartFromService()
  }
}

function onStorageEvent(e) {
  if (e.key === 'cart') loadCartFromService()
}

onMounted(() => {
  loadCartFromService()
  window.addEventListener('cart-updated', onCartUpdatedHandler)
  window.addEventListener('cart-removed', onCartRemovedHandler)
  window.addEventListener('storage', onStorageEvent)
})

onBeforeUnmount(() => {
  window.removeEventListener('cart-updated', onCartUpdatedHandler)
  window.removeEventListener('cart-removed', onCartRemovedHandler)
  window.removeEventListener('storage', onStorageEvent)
  stopWatch()
})
</script>

<style scoped>
/* tổng thể */
.cart-page-container { max-width: 1280px; margin: 20px auto; padding: 20px; background-color: #f8f9fa; }

/* header */
.cart-header { display:flex; align-items:baseline; gap:12px; margin-bottom:20px; }
.cart-title { font-size:28px; font-weight:700; color:#333; margin:0; }
.item-count { font-size:16px; color:#666; }
.bulk-alert { margin-bottom:20px; }

/* layout */
.cart-layout { display:grid; grid-template-columns:2fr 1fr; gap:24px; align-items:flex-start; }
.cart-items-list { display:flex; flex-direction:column; gap:1px; background:#e9ecef; border-radius:8px; overflow:hidden; }

/* item card */
.cart-item-card { display:grid; grid-template-columns:auto 1fr auto auto; gap:16px; align-items:center; padding:16px; background:#fff; }
.cart-thumb { width:80px; height:80px; border-radius:6px; border:1px solid #eee; object-fit:cover; }

/* details */
.item-details { display:flex; flex-direction:column; gap:4px; text-align:left; }
.product-name { font-weight:600; color:#333; font-size:16px; line-height:1.4; }
.product-meta { font-size:13px; color:#6c757d; }
.stock-hint { color:#28a745; }

/* price area */
.item-price { display:flex; align-items:flex-end; gap:12px; }

/* original price - gray struck-through */
.original-price {
  color: #9ca3af;
  font-size: 18px;
  text-decoration: line-through;
  text-decoration-thickness: 2px;
  text-decoration-color: #e2e8f0;
  opacity: 0.95;
  margin-right: 6px;
  display:flex;
  align-items:center;
}
.original-price .currency-small { font-size: 14px; margin-left:4px; color:#9ca3af; }

/* final price - big red with underline accent */
.final-price { display:inline-flex; align-items:baseline; gap:6px; }

.final-price .amount {
  color: #b91c1c; /* đỏ đậm */
  font-weight: 800;
  font-size: 22px;
  line-height: 1;
  padding-bottom: 6px;
  position: relative;
}

/* gạch nền dưới số giống mock: a subtle rectangle block */
.final-price .amount::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -4px;
  height: 8px;
  background: rgba(185,28,28,0.12);
  border-radius: 2px;
  z-index: -1;
}

/* currency small, aligned */
.final-price .currency {
  color: #b91c1c;
  font-weight: 800;
  font-size: 18px;
  line-height: 1;
  padding-bottom: 6px;
  position: relative;
}
.final-price .currency::after {
  content: '';
  position: absolute;
  left: -2px;
  right: -2px;
  bottom: -4px;
  height: 8px;
  background: rgba(185,28,28,0.12);
  border-radius: 2px;
  z-index: -1;
}

/* controls */
.item-controls { display:flex; flex-direction:column; align-items:center; gap:8px; }
.quantity-input { width:100px; }
.item-total-price { font-size:16px; font-weight:700; color:#d9534f; }
.item-actions .el-button { font-size:18px; }

/* summary */
.cart-summary-card { position: sticky; top:20px; background:#fff; border:1px solid #e9ecef; border-radius:8px; padding:24px; box-shadow:0 4px 12px rgba(0,0,0,0.05); }
.summary-title { font-size:20px; font-weight:600; margin:0 0 20px 0; text-align:left; }
.summary-row { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; font-size:15px; color:#555; }
.summary-row.total-row { margin-top:20px; padding-top:20px; border-top:1px dashed #ced4da; }
.total-label { font-size:18px; font-weight:700; }
.total-price-display { font-size:22px; font-weight:700; color:#d9534f; }
.summary-actions { margin-top:24px; }
.checkout-btn { width:100%; font-weight:600; }
.full-width-tooltip { width:100%; }
.continue-shopping { margin-top:16px; display:flex; justify-content:space-between; }

/* dialog */
.contact-block { font-size:14px; color:#333; }
.contact-list { margin:10px 0 0; padding-left:18px; }
.contact-list li { margin:4px 0; }
.contact-list a { color:#409eff; text-decoration:none; }
.contact-list a:hover { text-decoration:underline; }

/* responsive */
@media (max-width: 992px) {
  .cart-layout { grid-template-columns:1fr; }
  .cart-summary-card { position: static; margin-top:20px; }
}
@media (max-width: 768px) {
  .cart-title { font-size:24px; }
  .cart-item-card { grid-template-columns:80px 1fr; grid-template-rows:auto auto auto; row-gap:12px; column-gap:12px; }
  .item-image { grid-row:1 / 4; }
  .item-details { grid-column:2 / 3; grid-row:1 / 2; }
  .item-controls { grid-column:2 / 3; grid-row:2 / 3; flex-direction:row; justify-content:space-between; align-items:center; }
  .item-actions { grid-column:2 / 3; grid-row:3 / 4; justify-self:end; }
  .product-name { font-size:15px; }
}
@media (max-width: 480px) {
  .cart-header { flex-direction:column; align-items:flex-start; gap:4px; }
}
</style>

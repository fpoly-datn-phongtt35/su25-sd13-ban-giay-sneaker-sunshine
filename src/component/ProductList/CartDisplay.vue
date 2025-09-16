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
        <div v-for="item in cartItems" :key="`${item.productDetailId}-${item.color}-${item.size}`" class="cart-item-card">
          <div class="item-image">
            <el-image :src="item.image || '/no-image.jpg'" fit="cover" class="cart-thumb" />
          </div>

          <div class="item-details">
            <div class="item-info">
              <span class="product-name">{{ item.productName }}</span>
              <span class="product-meta">
                Màu: {{ item.color }} | Size: {{ item.size }}
                <span v-if="Number(item.maxQuantity) > 0" class="stock-hint">| Tồn: {{ item.maxQuantity }}</span>
              </span>
            </div>

            <div class="item-price">
               <span v-if="item.sellPrice > item.price" class="original-price">
                {{ formatPrice(item.sellPrice) }}
              </span>
              <span class="final-price">{{ formatPrice(item.price) }}</span>
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
              {{ formatPrice((Number(item.price) || 0) * (Number(item.quantity) || 0)) }}
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
// ===== SCRIPT SETUP KHÔNG THAY ĐỔI =====
// (Dán toàn bộ phần <script setup> của bạn vào đây)
import { ref, onMounted, onBeforeUnmount, watch, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Back, Shop } from '@element-plus/icons-vue'
import { getCart, saveCart, clearCart } from '@/utils/cart'
import axios from 'axios'

/* ===== Cấu hình giới hạn & liên hệ ===== */
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

// FLAG: khi đang áp dụng cập nhật từ remote/event -> watcher sẽ bỏ qua (tránh loop)
const isApplyingRemote = ref(false)
const lastNetworkError = ref(false)

const safeInt = (v, def = 0) => {
  const n = Number(v)
  return Number.isFinite(n) ? Math.trunc(n) : def
}
const formatPrice = (value) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(value) || 0)

const getRowMax = (row) => {
  const stock = Number.isFinite(Number(row?.maxQuantity)) && Number(row.maxQuantity) >= 0 ? Number(row.maxQuantity) : Infinity
  return Math.max(1, Math.min(RULE_PER_LINE_MAX, stock))
}

/* ===== So sánh cart (để tránh phản hồi thừa) =====
   So sánh bằng key sản phẩm (productDetailId|productId|color|size) + quantity để xác định identical
*/
function makeKey(item) {
  return `${item?.productDetailId ?? item?.productDetail?.id ?? ''}::${item?.productId ?? ''}::${item?.color ?? ''}::${item?.size ?? ''}`
}
function isCartEqual(a, b) {
  if (!Array.isArray(a) || !Array.isArray(b)) return false
  if (a.length !== b.length) return false
  const mapA = new Map()
  for (const it of a) {
    mapA.set(makeKey(it), safeInt(it.quantity, 0))
  }
  for (const it of b) {
    const k = makeKey(it)
    if (!mapA.has(k)) return false
    if (mapA.get(k) !== safeInt(it.quantity, 0)) return false
  }
  return true
}

/* ===== Load cart từ service (async) ===== */
let isLoading = false
async function loadCartFromService() {
  if (isLoading) return
  isLoading = true
  try {
    const res = await getCart()
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

    // nếu incoming giống current thì bỏ qua (tránh loop)
    if (isCartEqual(normalized, cartItems.value)) {
      // update network error indicator only
      lastNetworkError.value = lastNetworkError.value
      isLoading = false
      return
    }

    isApplyingRemote.value = true
    cartItems.value = normalized
    normalizeAllRows(false)
    await nextTick()
    isApplyingRemote.value = false

    if (lastNetworkError.value) {
      ElMessage.warning('Không thể xác thực sản phẩm với server — dùng dữ liệu cục bộ.')
    }
  } catch (e) {
    console.error('loadCartFromService error', e)
    ElMessage.error('Lỗi khi tải giỏ hàng.')
  } finally {
    isLoading = false
  }
}

/* ===== Đồng bộ mọi dòng về mức hợp lệ ===== */
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
  Array.isArray(cartItems.value) ? cartItems.value.reduce((sum, it) => sum + (safeInt(it.price, 0) * safeInt(it.quantity, 0)), 0) : 0
)

/* ===== Cập nhật số lượng ===== */
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
  // Nếu đang apply remote update thì không lưu (tránh ghi lại chính update từ remote)
  if (!isApplyingRemote.value) {
    try { saveCart(cartItems.value) } catch (e) { console.error(e) }
  }
}

/* ===== Xóa 1 item ===== */
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
    // only save if not applying remote
    if (!isApplyingRemote.value) try { saveCart(cartItems.value) } catch (e) { console.error(e) }
    ElMessage.success('Đã xóa sản phẩm!')
  }).catch(() => {})
}

/* ===== Xóa tất cả ===== */
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
  // 1) kiểm tra cơ bản trước khi gọi API
  if (!Array.isArray(cartItems.value) || cartItems.value.length === 0) {
    ElMessage.warning('Giỏ hàng đang trống!')
    return
  }
  if (exceedsBulkRule.value) {
    showBulkDialog()
    return
  }

  // 2) gom ids productDetail hợp lệ
  const ids = Array.from(new Set(
    cartItems.value
      .map(it => Number(it?.productDetailId ?? it?.productDetail?.id ?? it?.productDetailId))
      .filter(n => Number.isFinite(n) && n > 0)
  ))

  if (ids.length === 0) {
    // không có id hợp lệ -> không thể verify, chặn lại
    ElMessage.error('Không tìm thấy productDetail id để kiểm tra. Vui lòng kiểm tra lại giỏ hàng.')
     window.location.reload()
    return
  }

  const idsCsv = ids.join(',')
  const url = `http://localhost:8080/api/online-sale/verify-list-pdDetail/${idsCsv}`

  try {
    // optional: show a small loading indicator if bạn có
    const res = await axios.get(url, { timeout: 10000 }) // timeout 10s
    // trạng thái body có thể là mảng trực tiếp hoặc { data: [...]} hoặc { content: [...] }
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

    // build map id -> status
    const statusMap = {}
    for (const pd of arr) {
      const id = Number(pd?.id ?? pd?.productDetailId ?? (pd?.productDetail && pd.productDetail.id))
      const status = pd?.status ?? pd?.statusCode ?? pd?.active ?? undefined
      if (Number.isFinite(id)) statusMap[id] = (Number.isFinite(Number(status)) ? Number(status) : undefined)
    }

    // tìm item không hợp lệ
    const invalidItems = []
    for (const it of cartItems.value) {
      const pid = Number(it?.productDetailId ?? it?.productDetail?.id ?? it?.productDetailId)
      const s = statusMap[pid]
      // điều kiện: nếu server không trả thông tin cho id này -> coi là KHÔNG HỢP LỆ (bảo mật checkout)
      // nếu bạn muốn giữ id thiếu info là "giữ" thay vì "không hợp lệ", đổi điều kiện tương ứng
      if (!Number.isFinite(s) || Number(s) !== 1) {
        invalidItems.push({
          productName: it.productName ?? `(id:${pid})`,
          productDetailId: pid,
          serverStatus: s
        })
      }
    }

    if (invalidItems.length > 0) {
      // hiển thị tên/ID các sản phẩm bị lỗi
      const names = invalidItems.map(i => `${i.productName || ''} (id:${i.productDetailId})`).join(', ')
      ElMessage.error(`Một hoặc nhiều sản phẩm không hợp lệ để mua: ${names}. Vui lòng kiểm tra giỏ hàng.`)
      console.warn('Invalid items for checkout:', invalidItems)
       window.location.reload()
      return
    }

    // mọi thứ OK -> chuyển trang checkout
    router.push('/checkout')
  } catch (err) {
    console.error('Lỗi khi kiểm tra trạng thái productDetail:', err)
    ElMessage.error('Không thể kiểm tra trạng thái sản phẩm. Vui lòng thử lại sau.')
     window.location.reload()
    return
  }
}


/* ===== Watchers & event listeners ===== */
const stopWatch = watch(cartItems, (newVal) => {
  // Nếu đang apply remote update, không lưu (đã lưu ở nơi phát event)
  if (isApplyingRemote.value) return
  try { saveCart(Array.isArray(newVal) ? newVal : []) } catch (e) { console.error(e) }
}, { deep: true })

function onCartUpdatedHandler(e) {
  try {
    const incoming = e?.detail?.cart
    if (!Array.isArray(incoming)) {
      // nếu event không payload cart, reload toàn bộ từ service
      isApplyingRemote.value = true
      loadCartFromService().finally(() => { isApplyingRemote.value = false })
      return
    }

    // nếu incoming giống cart hiện tại -> bỏ qua
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
  if (e.key === 'cart') {
    loadCartFromService()
  }
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
/* ===== GIAO DIỆN TỔNG THỂ ===== */
.cart-page-container {
  max-width: 1280px;
  margin: 20px auto;
  padding: 20px;
  background-color: #f8f9fa; /* Nền xám nhạt cho toàn trang */
}

.cart-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}

.cart-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.item-count {
  font-size: 16px;
  color: #666;
}

.bulk-alert {
  margin-bottom: 20px;
}

/* ===== BỐ CỤC CHÍNH (2 CỘT) ===== */
.cart-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  align-items: flex-start;
}

.cart-items-list {
  display: flex;
  flex-direction: column;
  gap: 1px; /* Tạo đường kẻ mảnh giữa các item */
  background-color: #e9ecef; /* Màu đường kẻ */
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
}

/* ===== THẺ SẢN PHẨM (CARD) ===== */
.cart-item-card {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 16px;
  align-items: center;
  padding: 16px;
  background: #fff;
}

.item-image .cart-thumb {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  border: 1px solid #eee;
}

.item-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
}

.product-name {
  font-weight: 600;
  color: #333;
  font-size: 16px;
  line-height: 1.4;
}

.product-meta {
  font-size: 13px;
  color: #6c757d;
}

.stock-hint {
  color: #28a745;
}

.item-price {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
}

.original-price {
  text-decoration: line-through;
  color: #999;
  font-size: 13px;
}

.final-price {
  color: #333;
  font-weight: 600;
  font-size: 15px;
}

.item-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.quantity-input {
  width: 100px;
}
.item-total-price {
  font-size: 16px;
  font-weight: 700;
  color: #d9534f;
}

.item-actions .el-button {
  font-size: 18px;
}

/* ===== THẺ TÓM TẮT ĐƠN HÀNG ===== */
.cart-summary-card {
  position: sticky;
  top: 20px;
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.summary-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px 0;
  text-align: left;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 15px;
  color: #555;
}

.summary-row.total-row {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #ced4da;
}

.total-label {
  font-size: 18px;
  font-weight: 700;
}

.total-price-display {
  font-size: 22px;
  font-weight: 700;
  color: #d9534f;
}

.summary-actions {
  margin-top: 24px;
}
.checkout-btn {
  width: 100%;
  font-weight: 600;
}
.full-width-tooltip {
  width: 100%;
}
.continue-shopping {
    margin-top: 16px;
    display: flex;
    justify-content: space-between;
}

/* ===== DIALOG LIÊN HỆ ===== */
.contact-block { font-size: 14px; color: #333; }
.contact-list { margin: 10px 0 0; padding-left: 18px; }
.contact-list li { margin: 4px 0; }
.contact-list a { color: #409eff; text-decoration: none; }
.contact-list a:hover { text-decoration: underline; }

/* ===== RESPONSIVE DESIGN ===== */
@media (max-width: 992px) {
  .cart-layout {
    grid-template-columns: 1fr; /* Chuyển thành 1 cột */
  }

  .cart-summary-card {
    position: static; /* Gỡ sticky */
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .cart-title {
    font-size: 24px;
  }
  
  .cart-item-card {
    grid-template-columns: 80px 1fr; /* Ảnh và phần còn lại */
    grid-template-rows: auto auto auto;
    row-gap: 12px;
    column-gap: 12px;
  }

  .item-image {
    grid-row: 1 / 4; /* Ảnh chiếm cả 3 hàng */
  }

  .item-details { grid-column: 2 / 3; grid-row: 1 / 2; }
  .item-controls { 
    grid-column: 2 / 3; 
    grid-row: 2 / 3;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  .item-actions { 
    grid-column: 2 / 3;
    grid-row: 3 / 4; 
    justify-self: end;
  }
  .product-name { font-size: 15px; }
}

@media (max-width: 480px) {
    .cart-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
    }
}
</style>
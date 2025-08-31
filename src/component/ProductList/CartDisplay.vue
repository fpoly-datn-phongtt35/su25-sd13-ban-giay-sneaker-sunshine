<template>
  <div class="cart-container">
    <h2 class="cart-title">Giỏ hàng của bạn</h2>

    <el-alert
      v-if="exceedsBulkRule && cartItems.length"
      type="warning"
      show-icon
      class="bulk-alert"
      :closable="false"
      title="Đơn online không hỗ trợ số lượng lớn"
      description="Số lượng mỗi sản phẩm tối đa 9. Nếu bạn muốn đặt từ 10 đôi trở lên, vui lòng liên hệ nhân viên để được hỗ trợ."
    />

    <el-table v-if="cartItems.length" :data="cartItems" border class="cart-table">
      <el-table-column label="Ảnh" width="90" align="center">
        <template #default="{ row }">
          <el-image :src="row.image || '/no-image.jpg'" fit="cover" class="cart-thumb" />
        </template>
      </el-table-column>

      <el-table-column label="Sản phẩm" min-width="200">
        <template #default="{ row }">
          <div class="product-info">
            <span class="product-name">{{ row.productName }}</span>
            <span class="product-details">
              Màu: {{ row.color }} | Size: {{ row.size }}
              <span v-if="Number(row.maxQuantity) > 0" class="stock-hint">| Tồn: {{ row.maxQuantity }}</span>
            </span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Số lượng" width="160" align="center">
        <template #default="{ row }">
          <el-input-number
            v-model="row.quantity"
            :min="1"
            :max="getRowMax(row)"
            @change="() => updateQuantity(row)"
            size="small"
          />
        </template>
      </el-table-column>

      <el-table-column label="Đơn giá" width="160" align="right">
        <template #default="{ row }">
          <div class="price-block">
            <span v-if="row.sellPrice > row.price" class="original-price">
              {{ formatPrice(row.sellPrice) }}
            </span>
            <span class="final-price">{{ formatPrice(row.price) }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Thành tiền" width="150" align="right">
        <template #default="{ row }">
          <span class="total-price">{{ formatPrice((Number(row.price) || 0) * (Number(row.quantity) || 0)) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="Hành động" width="100" align="center">
        <template #default="{ row }">
          <el-button type="danger" :icon="Delete" circle size="small" @click="removeItem(row)" />
        </template>
      </el-table-column>
    </el-table>

    <div v-if="cartItems.length" class="cart-summary">
      <div class="total-amount">
        Tổng cộng ({{ totalQty }} đôi): <span class="total-price-display">{{ formatPrice(calculateTotal) }}</span>
      </div>
      <div class="cart-actions">
        <el-button type="info" @click="router.push('/')">
          <el-icon><Back /></el-icon> Tiếp tục mua sắm
        </el-button>
        <el-button type="danger" @click="confirmClearAll">Xóa tất cả</el-button>

        <el-tooltip
          v-if="exceedsBulkRule"
          effect="dark"
          content="Số lượng lớn (≥ 10). Vui lòng liên hệ nhân viên để đặt hàng."
          placement="top"
        >
          <el-button type="success" :disabled="true" @click="goToCheckout">Thanh toán</el-button>
        </el-tooltip>

        <el-button v-else type="success" @click="goToCheckout">Thanh toán</el-button>
      </div>
    </div>

    <el-empty v-else description="Giỏ hàng của bạn đang trống" image-size="200">
      <el-button type="primary" @click="router.push('/')">
        <el-icon><Shop /></el-icon> Bắt đầu mua sắm ngay!
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
/* (giữ nguyên CSS từ bản trước) */
.cart-container { max-width: 1200px; margin: 20px auto; padding: 20px; background: #fff; border-radius: 8px; }
.cart-title { font-size: 24px; font-weight: 700; margin-bottom: 20px; text-align: center; }
.bulk-alert { margin-bottom: 12px; }
.cart-table { margin-bottom: 20px; }
.cart-thumb { width: 70px; height: 70px; border: 1px solid #eee; border-radius: 6px; }
.product-info { text-align: left; }
.product-name { font-weight: 600; color: #333; }
.product-details { font-size: 13px; color: #666; }
.stock-hint { color: #999; }
.price-block { display: flex; flex-direction: column; align-items: flex-end; }
.original-price { text-decoration: line-through; color: #999; font-size: 12px; }
.final-price { color: #e6a23c; font-weight: 600; }
.total-price { color: #f56c6c; font-weight: 700; }
.cart-summary { text-align: right; border-top: 1px solid #eee; padding-top: 15px; }
.total-amount { font-size: 20px; font-weight: 700; margin-bottom: 10px; }
.total-price-display { color: #f56c6c; font-size: 24px; }
.cart-actions { display: flex; gap: 10px; justify-content: flex-end; flex-wrap: wrap; }

.contact-block { font-size:14px; color:#333; }
.contact-list { margin:10px 0 0; padding-left:18px; }
.contact-list li { margin:4px 0; }
.contact-list a { color:#409eff; text-decoration:none; }
.contact-list a:hover { text-decoration:underline; }
</style>

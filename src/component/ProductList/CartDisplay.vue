<template>
  <div class="cart-container">
    <h2 class="cart-title">Giỏ hàng của bạn</h2>

    <!-- Cảnh báo nếu tổng SL hoặc bất kỳ dòng nào ≥ NGAY_MAX_QTY -->
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
          <span class="total-price">{{ formatPrice(row.price * row.quantity) }}</span>
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
        Tổng cộng ({{ totalQty }} đôi): <span class="total-price-display">{{ formatPrice(calculateTotal()) }}</span>
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
        <el-button
          v-else
          type="success"
          @click="goToCheckout"
        >
          Thanh toán
        </el-button>
      </div>
    </div>

    <el-empty v-else description="Giỏ hàng của bạn đang trống" image-size="200">
      <el-button type="primary" @click="router.push('/')">
        <el-icon><Shop /></el-icon> Bắt đầu mua sắm ngay!
      </el-button>
    </el-empty>

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
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Back, Shop } from '@element-plus/icons-vue'
import { getCart, saveCart, clearCart } from '@/utils/cart'

/* ===== Cấu hình giới hạn & liên hệ ===== */
const NGAY_MAX_QTY = 10                   // Ngưỡng chặn (từ 10 trở lên)
const hotline = '09xx xxx xxx'            // TODO: thay số thật
const zaloLink = 'https://zalo.me/09xxxxxxxx'  // TODO
const facebookLink = 'https://facebook.com/yourpage' // TODO
const contactDialogVisible = ref(false)
const showBulkDialog = () => {
  contactDialogVisible.value = true
  ElMessage.warning(`Số lượng lớn (≥ ${NGAY_MAX_QTY} đôi). Vui lòng liên hệ nhân viên để đặt hàng.`)
}

const router = useRouter()
const cartItems = ref([])

onMounted(() => {
  cartItems.value = getCart()
  // Ép lại những item nào vượt quá luật (≥10) về mức hợp lệ
  normalizeAllRows()
})

// Lưu giỏ khi thay đổi
watch(cartItems, (newVal) => saveCart(newVal), { deep: true })

/* ===== Helpers ===== */
const RULE_PER_LINE_MAX = NGAY_MAX_QTY - 1 // 9
const safeInt = (v, def=0) => Number.isFinite(Number(v)) ? Number(v) : def

// max cho InputNumber = min(9, tồn kho nếu có)
const getRowMax = (row) => {
  const stock = safeInt(row.maxQuantity, Infinity)
  return Math.max(1, Math.min(RULE_PER_LINE_MAX, stock))
}

// Đồng bộ mọi dòng về mức hợp lệ
const normalizeAllRows = () => {
  let touched = false
  cartItems.value = cartItems.value.map((row) => {
    let qty = Math.max(1, safeInt(row.quantity, 1))
    const rowMax = getRowMax(row)
    if (qty >= NGAY_MAX_QTY) {
      qty = rowMax // ép về tối đa cho phép
      touched = true
    } else if (qty > rowMax) {
      qty = rowMax
      touched = true
    }
    return { ...row, quantity: qty }
  })
  if (touched) {
    showBulkDialog()
    saveCart(cartItems.value)
  }
}

// Định dạng giá tiền
const formatPrice = (value) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)

// Tổng số lượng & điều kiện chặn
const totalQty = computed(() =>
  cartItems.value.reduce((sum, item) => sum + safeInt(item.quantity, 0), 0)
)

// chặn nếu tổng ≥ 10 hoặc bất kỳ dòng nào ≥ 10
const exceedsBulkRule = computed(() =>
  totalQty.value >= NGAY_MAX_QTY ||
  cartItems.value.some(i => safeInt(i.quantity, 0) >= NGAY_MAX_QTY)
)

/* ===== Cập nhật số lượng ===== */
const updateQuantity = (item) => {
  // đảm bảo tối thiểu 1
  if (safeInt(item.quantity, 0) < 1) {
    item.quantity = 1
    ElMessage.warning('Số lượng tối thiểu là 1.')
  }

  // chặn theo tồn kho nếu có
  const stock = getRowMax(item)
  if (safeInt(item.quantity, 0) > stock) {
    item.quantity = stock
    ElMessage.warning(`Số lượng tối đa cho phép là ${stock}.`)
  }

  // chặn theo luật 10+
  if (safeInt(item.quantity, 0) >= NGAY_MAX_QTY) {
    item.quantity = stock // stock đã ≤ 9
    showBulkDialog()
  }

  saveCart(cartItems.value)
}

/* ===== Xóa 1 sản phẩm ===== */
const removeItem = (item) => {
  ElMessageBox.confirm(`Xóa "${item.productName}" khỏi giỏ?`, 'Xác nhận', {
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
    type: 'warning'
  }).then(() => {
    cartItems.value = cartItems.value.filter(i =>
      !(i.productId === item.productId && i.color === item.color && i.size === item.size)
    )
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

/* ===== Tính tổng tiền ===== */
const calculateTotal = () =>
  cartItems.value.reduce((sum, item) => sum + (safeInt(item.price, 0) * safeInt(item.quantity, 0)), 0)

/* ===== Đi tới thanh toán ===== */
const goToCheckout = () => {
  if (!cartItems.value.length) {
    ElMessage.warning('Giỏ hàng đang trống!')
    return
  }
  if (exceedsBulkRule.value) {
    showBulkDialog()
    return
  }
  router.push('/checkout')
}
</script>

<style scoped>
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

/* Contact dialog */
.contact-block { font-size:14px; color:#333; }
.contact-list { margin:10px 0 0; padding-left:18px; }
.contact-list li { margin:4px 0; }
.contact-list a { color:#409eff; text-decoration:none; }
.contact-list a:hover { text-decoration:underline; }
</style>

<template>
  <div class="cart-container">
    <h2 class="cart-title">Giỏ hàng của bạn</h2>

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
            <span class="product-details">Màu: {{ row.color }} | Size: {{ row.size }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Số lượng" width="140" align="center">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="1" :max="100" @change="updateQuantity(row)" size="small" />
        </template>
      </el-table-column>

      <el-table-column label="Đơn giá" width="160" align="right">
        <template #default="{ row }">
          <div class="price-block">
            <span 
              v-if="row.sellPrice > row.price" 
              class="original-price">
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
        Tổng cộng: <span class="total-price-display">{{ formatPrice(calculateTotal()) }}</span>
      </div>
      <div class="cart-actions">
        <el-button type="info" @click="router.push('/')">
          <el-icon><Back /></el-icon> Tiếp tục mua sắm
        </el-button>
        <el-button type="danger" @click="confirmClearAll">Xóa tất cả</el-button>
        <el-button type="success" @click="goToCheckout">Thanh toán</el-button>
      </div>
    </div>

    <el-empty v-else description="Giỏ hàng của bạn đang trống" image-size="200">
      <el-button type="primary" @click="router.push('/')">
        <el-icon><Shop /></el-icon> Bắt đầu mua sắm ngay!
      </el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Back, Shop } from '@element-plus/icons-vue'
import { getCart, saveCart, clearCart } from '@/utils/cart'

const router = useRouter()
const cartItems = ref([])

onMounted(() => {
  cartItems.value = getCart()
})

// Tự động lưu giỏ khi thay đổi
watch(cartItems, (newVal) => saveCart(newVal), { deep: true })

// Định dạng giá tiền
const formatPrice = (value) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)

// Cập nhật số lượng
const updateQuantity = (item) => {
  if (item.quantity < 1) {
    item.quantity = 1
    ElMessage.warning('Số lượng tối thiểu là 1.')
  }
  saveCart(cartItems.value)
}

// Xóa 1 sản phẩm
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

// Xóa tất cả
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

// Tính tổng tiền
const calculateTotal = () =>
  cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)

// Đi tới trang thanh toán
const goToCheckout = () => {
  if (!cartItems.value.length) {
    ElMessage.warning('Giỏ hàng đang trống!')
    return
  }
  router.push('/checkout')
}
</script>

<style scoped>
.cart-container { max-width: 1200px; margin: 20px auto; padding: 20px; background: #fff; border-radius: 8px; }
.cart-title { font-size: 24px; font-weight: 700; margin-bottom: 20px; text-align: center; }
.cart-table { margin-bottom: 20px; }
.cart-thumb { width: 70px; height: 70px; border: 1px solid #eee; border-radius: 6px; }
.product-info { text-align: left; }
.product-name { font-weight: 600; color: #333; }
.product-details { font-size: 13px; color: #666; }
.price-block { display: flex; flex-direction: column; align-items: flex-end; }
.original-price { text-decoration: line-through; color: #999; font-size: 12px; }
.final-price { color: #e6a23c; font-weight: 600; }
.total-price { color: #f56c6c; font-weight: 700; }
.cart-summary { text-align: right; border-top: 1px solid #eee; padding-top: 15px; }
.total-amount { font-size: 20px; font-weight: 700; margin-bottom: 10px; }
.total-price-display { color: #f56c6c; font-size: 24px; }
.cart-actions { display: flex; gap: 10px; justify-content: flex-end; flex-wrap: wrap; }
</style>

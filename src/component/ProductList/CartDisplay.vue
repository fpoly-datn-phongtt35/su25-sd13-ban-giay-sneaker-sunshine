<template>
  <div class="cart-container">
    <h2 class="cart-title">🛒 Giỏ hàng của bạn</h2>

    <div v-if="cartItems.length" class="cart-content">
      <el-table :data="cartItems" style="width: 100%">
        <el-table-column label="Ảnh" width="80">
          <template #default="{ row }">
            <img
              :src="row.image || '/no-image.jpg'"
              class="cart-product-thumbnail"
              alt="Ảnh sản phẩm"
            />
          </template>
        </el-table-column>

        <el-table-column prop="productName" label="Sản phẩm" min-width="180" />
        <el-table-column prop="color" label="Màu" width="100" />
        <el-table-column prop="size" label="Size" width="100" />
        <el-table-column prop="quantity" label="SL" width="80" />
        
        <el-table-column label="Đơn giá" width="120">
          <template #default="{ row }">
            {{ formatPrice(row.price) }}
          </template>
        </el-table-column>

        <el-table-column label="Thành tiền" width="120">
          <template #default="{ row }">
            {{ formatPrice(row.price * row.quantity) }}
          </template>
        </el-table-column>

        <el-table-column label="Xóa" width="80">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              icon="Delete"
              circle
              @click="removeItem(row)"
              :aria-label="`Xóa sản phẩm ${row.productName}`"
            />
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-actions" style="margin-top: 20px; text-align: right;">
        <el-button type="danger" @click="clearAll">🗑️ Xóa tất cả</el-button>
        <el-button type="success" @click="goToCheckout" style="margin-left: 12px;">💰 Thanh toán</el-button>
      </div>
    </div>

    <div v-else class="empty-cart" style="text-align: center; margin-top: 40px;">
      <el-empty description="Giỏ hàng trống" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, saveCart, clearCart } from '@/utils/cart'
import { ElMessage } from 'element-plus'

// Khởi tạo router và trạng thái giỏ hàng
const router = useRouter()
const cartItems = ref([])

// Load giỏ hàng khi component mount
onMounted(() => {
  cartItems.value = getCart()
  console.log('📦 Giỏ hàng khi load:', cartItems.value)
})

// Hàm định dạng tiền tệ VND
const formatPrice = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value || 0)
}

// Xóa 1 sản phẩm trong giỏ hàng theo productId, color, size
const removeItem = (item) => {
  const updatedCart = cartItems.value.filter(cartItem =>
    !(
      cartItem.productId === item.productId &&
      cartItem.color === item.color &&
      cartItem.size === item.size
    )
  )
  cartItems.value = updatedCart
  saveCart(updatedCart)
  ElMessage.success('Đã xóa sản phẩm!')
}

// Xóa tất cả giỏ hàng
const clearAll = () => {
  clearCart()
  cartItems.value = []
  ElMessage.success('Đã xóa tất cả sản phẩm!')
}

// Chuyển sang trang thanh toán, kiểm tra giỏ hàng trước
const goToCheckout = () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('Giỏ hàng đang trống!')
    return
  }
  router.push('/checkout')
}
</script>

<style scoped>
.cart-product-thumbnail {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}
.cart-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 16px;
}
.cart-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.empty-cart {
  margin-top: 40px;
}
</style>

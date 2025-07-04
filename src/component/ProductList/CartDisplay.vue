<template>
  <div class="cart-container">
    <h2 class="cart-title"> Giỏ hàng của bạn</h2>

    <div v-if="cartItems.length" class="cart-content">
      <el-table :data="cartItems" style="width: 100%" class="cart-table" border>
        <el-table-column label="Ảnh" width="90" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.image || '/no-image.jpg'"
              class="cart-product-thumbnail"
              fit="cover"
              :alt="`Ảnh sản phẩm ${row.productName}`"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column label="Sản phẩm" min-width="200" header-align="left">
          <template #default="{ row }">
            <div class="product-info">
              <span class="product-name">{{ row.productName }}</span>
              <p class="product-details">
                <span v-if="row.color">Màu: {{ row.color }}</span>
                <span v-if="row.size"> | Size: {{ row.size }}</span>
              </p>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Số lượng" width="150" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="100"
              @change="updateQuantity(row)"
              size="small"
              controls-position="right"
            />
          </template>
        </el-table-column>

        <el-table-column label="Đơn giá" width="130" align="right">
          <template #default="{ row }">
            <span class="price-text">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Thành tiền" width="150" align="right">
          <template #default="{ row }">
            <span class="price-text total-price">{{ formatPrice(row.price * row.quantity) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Hành động" width="100" align="center">
          <template #default="{ row }">
            <el-button
              type="danger"
              :icon="Delete"
              circle
              @click="removeItem(row)"
              size="small"
              :aria-label="`Xóa sản phẩm ${row.productName}`"
            />
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-summary">
        <div class="total-amount">
          Tổng cộng: <span class="total-price-display">{{ formatPrice(calculateTotal()) }}</span>
        </div>
        <div class="cart-actions">
          <el-button type="info" @click="router.push('/')">
            <el-icon><Back /></el-icon> Tiếp tục mua sắm
          </el-button>
          <el-button type="danger" @click="confirmClearAll">
             Xóa tất cả
          </el-button>
          <el-button type="success" @click="goToCheckout">
             Thanh toán
          </el-button>
        </div>
      </div>
    </div>

    <div v-else class="empty-cart">
      <el-empty description="Giỏ hàng của bạn đang trống" image-size="200">
        <el-button type="primary" @click="router.push('/')">
          <el-icon><Shop /></el-icon> Bắt đầu mua sắm ngay!
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, saveCart, clearCart } from '@/utils/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Picture, Back, Wallet, Shop } from '@element-plus/icons-vue'

const router = useRouter()
const cartItems = ref([])

onMounted(() => {
  cartItems.value = getCart()
  console.log(' Giỏ hàng khi load:', cartItems.value)
})

// Watch for changes in cartItems and save to local storage
watch(cartItems, (newCart) => {
  saveCart(newCart)
}, { deep: true }) // Deep watch to detect changes within objects in the array

const formatPrice = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value || 0)
}

// Update quantity and save cart
const updateQuantity = (item) => {
  if (item.quantity < 1) {
    ElMessage.warning('Số lượng không thể nhỏ hơn 1.')
    item.quantity = 1; // Reset to 1 if user tries to go below
  }
  saveCart(cartItems.value)
  ElMessage.success('Cập nhật số lượng thành công!')
}

// Remove an item from the cart by productId, color, and size
const removeItem = (item) => {
  ElMessageBox.confirm(
    `Bạn có chắc chắn muốn xóa sản phẩm "${item.productName}" khỏi giỏ hàng?`,
    'Xác nhận xóa',
    {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning',
    }
  )
    .then(() => {
      const updatedCart = cartItems.value.filter(
        (cartItem) =>
          !(
            cartItem.productId === item.productId &&
            cartItem.color === item.color &&
            cartItem.size === item.size
          )
      )
      cartItems.value = updatedCart
      ElMessage.success('Đã xóa sản phẩm khỏi giỏ hàng!')
    })
    .catch(() => {
      ElMessage.info('Hủy bỏ thao tác xóa.')
    })
}

const confirmClearAll = () => {
  ElMessageBox.confirm(
    'Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng?',
    'Xác nhận xóa tất cả',
    {
      confirmButtonText: 'Xóa tất cả',
      cancelButtonText: 'Hủy',
      type: 'warning',
    }
  )
    .then(() => {
      clearCart()
      cartItems.value = []
      ElMessage.success('Đã xóa tất cả sản phẩm khỏi giỏ hàng!')
    })
    .catch(() => {
      ElMessage.info('Hủy bỏ thao tác xóa tất cả.')
    })
}

const calculateTotal = () => {
  return cartItems.value.reduce((total, item) => total + item.price * item.quantity, 0)
}

const goToCheckout = () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('Giỏ hàng của bạn đang trống! Vui lòng thêm sản phẩm để thanh toán.')
    return
  }
  router.push('/checkout')
}
</script>

<style scoped>
.cart-container {
  padding: 20px;
  max-width: 1200px;
  margin: 20px auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.cart-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 25px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.cart-table {
  margin-bottom: 25px;
  border-radius: 6px;
  overflow: hidden; 
}

.cart-product-thumbnail {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  border: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9f9f9;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f0f0f0;
  color: #909399;
  font-size: 20px;
}

.product-info {
  text-align: left;
}

.product-name {
  font-weight: 600;
  color: #444;
  font-size: 16px;
  display: block; 
  margin-bottom: 4px;
}

.product-details {
  font-size: 13px;
  color: #666;
}

.price-text {
  font-weight: 600;
  color: #e6a23c; /* A nice amber for prices */
}

.total-price {
  color: #f56c6c; /* A striking red for final total */
  font-size: 15px;
}

/* Cart Summary and Actions */
.cart-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 20px; /* Space between total and buttons */
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.total-amount {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-price-display {
  font-size: 26px;
  color: #f56c6c;
  font-weight: 800;
}

.cart-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  flex-wrap: wrap; /* Allows buttons to wrap on smaller screens */
}

/* Empty Cart Styles */
.empty-cart {
  text-align: center;
  margin-top: 50px;
  padding: 30px;
  background-color: #fcfcfc;
  border-radius: 8px;
  border: 1px dashed #e0e0e0;
}

.el-empty {
  padding: 0; /* Remove default padding from el-empty if it causes issues */
}
</style>
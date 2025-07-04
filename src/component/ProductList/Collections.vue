<template>
  <el-container class="modern-product-collection-container">
    <el-header class="modern-collection-header">
      <h2 class="modern-collection-title">Bộ sưu tập</h2>
    </el-header>
    <el-main class="modern-product-list-main">
      <el-row :gutter="20">
        <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card
            class="modern-product-card"
            shadow="hover"
            :body-style="{ padding: '0px' }"
            @click="goToDetail(product.id)"
          >
            <div class="modern-image-container">
              <img
                :src="getBase64Image(product)"
                class="modern-product-image"
                alt="Ảnh sản phẩm"
              />
              <div class="modern-cart-overlay">
                <el-button
                  type="primary"
                  :icon="ShoppingCart"
                  circle
                  size="large"
                  class="modern-add-to-cart-btn"
                ></el-button>
              </div>
            </div>
            <div class="modern-product-info">
              <div class="modern-product-name">{{ product.productName }}</div>
              <div class="modern-product-prices">
                <span
                  v-if="product.sellPrice !== product.discountedPrice"
                  class="modern-original-price"
                >
                  {{ formatPrice(product.sellPrice) }}
                </span>
                <span class="modern-discounted-price">
                  {{ formatPrice(product.discountedPrice) }}
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ShoppingCart } from '@element-plus/icons-vue'

const products = ref([])
const router = useRouter()

onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/online-sale/hien-thi')
    products.value = response.data
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm:', error)
  }
})

// Xử lý ảnh base64
const getBase64Image = (product) => {
  if (product.productImages?.length > 0 && product.productImages[0].image) {
    return `data:image/jpeg;base64,${product.productImages[0].image}`
  }
  return '/no-image.jpg' // fallback ảnh mặc định
}

// Định dạng giá tiền VND
const formatPrice = (price) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)

// Điều hướng đến trang chi tiết
const goToDetail = (id) => {
  router.push({ name: 'ProductDetail', params: { id } })
}
</script>

<style scoped>
.modern-product-collection-container {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
}

.modern-collection-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.modern-collection-title {
  font-size: 2.2em;
  color: #333;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  position: relative;
  display: inline-block;
}

.modern-collection-title::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -5px;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background-color: #409eff;
  border-radius: 2px;
}

.modern-product-list-main {
  padding: 0;
}

.modern-product-card {
  margin-bottom: 25px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.modern-product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.modern-image-container {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
  background-color: #fcfcfc;
}

.modern-product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.2s ease-in-out;
}

.modern-product-card:hover .modern-product-image {
  transform: scale(1.02);
}

.modern-cart-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
}

.modern-product-card:hover .modern-cart-overlay {
  opacity: 1;
}

.modern-add-to-cart-btn {
  font-size: 24px;
  color: #fff;
  border-color: #fff;
  background-color: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(2px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: all 0.2s ease-in-out;
}

.modern-add-to-cart-btn:hover {
  background-color: #409eff;
  border-color: #409eff;
}

.modern-product-info {
  padding: 15px;
  text-align: left;
  border-top: 1px solid #f5f5f5;
}

.modern-product-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 6px;
  min-height: 40px;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: #444;
}

.modern-product-prices {
  display: flex;
  align-items: center;
  gap: 8px;
}

.modern-original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 14px;
}

.modern-discounted-price {
  color: #e53935;
  font-weight: 600;
  font-size: 16px;
}
</style>

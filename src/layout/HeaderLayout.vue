<template>
  <header class="bg-white text-gray-800 py-4 shadow-md">
    <div class="container d-flex align-items-center justify-content-between">
      <!-- Logo -->
      <div class="d-flex align-items-center gap-1 flex-shrink-0">
        <img :src="logoSrc" alt="Logo" class="h-8 w-8" />
        <h1 class="m-0 fw-bold text-xl text-nowrap">Sunshine Shop</h1>
      </div>

      <!-- Thanh tìm kiếm -->
      <div class="search-bar d-flex flex-grow-1 mx-4">
        <div class="input-group w-100">
          <input
            type="text"
            class="form-control custom-search-input"
            placeholder="Tìm kiếm sản phẩm..."
            v-model.trim="searchQuery"
            @keyup.enter="performSearch"
          />
          <button class="btn btn-primary custom-search-button" @click="performSearch">
            <i class="fas fa-search"></i>
          </button>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="flex-grow-1 d-flex justify-content-center px-4">
        <ul class="nav gap-3 m-0 p-0 flex-nowrap">
          <li class="nav-item" v-for="link in navLinks" :key="link.path">
            <RouterLink
              :to="link.path"
              class="nav-link text-gray-700 fw-semibold d-flex align-items-center gap-1"
              active-class="text-primary"
            >
              <i :class="link.icon"></i> {{ link.label }}
            </RouterLink>
          </li>
        </ul>
      </nav>

      <!-- Tài khoản và giỏ hàng -->
      <div class="d-flex align-items-center gap-2 flex-nowrap flex-shrink-0">
        <template v-if="user">
          <span class="fw-semibold text-sm text-gray-700 d-flex align-items-center gap-1">
            <i class="fas fa-user-circle"></i> 👋 {{ user.customerName || user.employeeName }}
          </span>
          <button class="btn btn-outline-secondary btn-sm" @click="logout" type="button">
            <i class="fas fa-sign-out-alt"></i> Đăng xuất
          </button>
        </template>
        <template v-else>
          <button class="btn btn-outline-primary btn-sm" @click="showRegister = true" type="button">
            <i class="fas fa-user-plus"></i> Đăng ký
          </button>
          <button class="btn btn-primary btn-sm" @click="showLogin = true" type="button">
            <i class="fas fa-sign-in-alt"></i> Đăng nhập
          </button>
        </template>

        <RouterLink to="/cart" class="btn btn-outline-primary btn-sm position-relative" aria-label="Giỏ hàng">
          <i class="fas fa-shopping-cart"></i>
          <span
            v-if="cartCount > 0"
            class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
            >{{ cartCount }}</span
          >
        </RouterLink>
      </div>
    </div>

    <!-- Modal đăng nhập -->
    <LoginModal
      :isVisible="showLogin"
      @update:isVisible="showLogin = $event"
      @loggedIn="handleLoggedIn"
      @openRegister="showRegister = true"
    />

    <!-- Modal đăng ký -->
    <RegisterCustomerModal
      :isVisible="showRegister"
      @update:isVisible="showRegister = $event"
      @customerAdded="handleRegisterSuccess"
      @openLogin="showLogin = true"
    />
  </header>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import LoginModal from '@/component/LoginModal.vue'
import RegisterCustomerModal from '@/component/RegisterCustomerModal.vue'

// Router instance
const router = useRouter()

// State
const logoSrc = ref('https://img.icons8.com/?size=100&id=juRF5DiUGr4p&format=png&color=000000')
const user = ref(null)
const cartCount = ref(0)
const searchQuery = ref('')
const showLogin = ref(false)
const showRegister = ref(false)

// Navigation links
const navLinks = [
  { path: '/', label: 'Trang chủ', icon: 'fas fa-home' },
  { path: '/collections', label: 'Sản phẩm', icon: 'fas fa-box-open' },
  { path: '/chinh-sach', label: 'Sản phẩm bán chạy', icon: 'fas fa-scroll' },
  { path: '/gioi-thieu', label: 'Giới thiệu', icon: 'fas fa-info-circle' },
  { path: '/lien-he', label: 'Liên hệ', icon: 'fas fa-phone-alt' },
  { path: '/don-hang', label: 'Đơn hàng', icon: 'fas fa-receipt' },
]

// Load user & cart count from localStorage on mount
onMounted(() => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) user.value = JSON.parse(storedUser)

  updateCartCount()
})

// Optional: Watch localStorage changes if your app updates cart count dynamically
// You can implement a custom event or state management for better sync
// Here just a simple watcher on user changes cart count (if needed)
watch(user, () => {
  updateCartCount()
})

// Function update cartCount from localStorage or global store
function updateCartCount() {
  try {
    // Giả sử bạn lưu giỏ hàng theo key 'cart_userId' hoặc 'cart_guest'
    const userId = user.value?.id || 'guest'
    const cartKey = `cart_${userId}`
    const cart = JSON.parse(localStorage.getItem(cartKey) || '[]')
    cartCount.value = Array.isArray(cart) ? cart.reduce((acc, item) => acc + (item.quantity || 0), 0) : 0
  } catch {
    cartCount.value = 0
  }
}

// Thực hiện tìm kiếm
const performSearch = () => {
  const keyword = searchQuery.value.trim()
  if (!keyword) {
    ElMessage.info('Vui lòng nhập từ khóa tìm kiếm.')
    return
  }
  router.push({ path: '/search-results', query: { q: keyword } })
  ElMessage.success(`Đang tìm kiếm: "${keyword}"`)
}

// Xử lý khi đăng nhập thành công
const handleLoggedIn = (userData) => {
  localStorage.setItem('user', JSON.stringify(userData))
  user.value = userData
  updateCartCount()
  ElMessage.success('Đăng nhập thành công!')
}

// Xử lý khi đăng ký thành công
const handleRegisterSuccess = () => {
  ElMessage.success('Đăng ký thành công! Vui lòng đăng nhập.')
  showLogin.value = true
}

// Đăng xuất
const logout = () => {
  const userId = user.value?.id || 'guest'

  // Xóa toàn bộ dữ liệu liên quan đến user
  localStorage.removeItem('user')
  localStorage.removeItem('userId')
  localStorage.removeItem(`cart_${userId}`)  // Xóa giỏ hàng

  user.value = null
  updateCartCount()

  ElMessage.success('Đăng xuất thành công!')
  router.push('/') // Optional: điều hướng về trang chủ sau khi logout
}

</script>

<style scoped>
/* Bạn có thể thêm CSS tùy chỉnh tại đây nếu cần */
</style>

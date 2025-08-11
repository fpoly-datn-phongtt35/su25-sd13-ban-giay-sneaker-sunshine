<template>
  <div class="header-wrapper">
    <div class="top-announcement-bar">
      <p class="scrolling-text">
        <span
          >Chào mừng đến với cửa hàng giày Sunshine! Mua sắm ngay hôm nay để nhận ưu đãi đặc
          biệt!</span
        >
        <span>Miễn phí vận chuyển cho đơn hàng trên 500.000đ!</span>
        <span>Giảm giá 20% cho đơn hàng đầu tiên khi đăng ký tài khoản!</span>
      </p>
    </div>

    <header class="main-header" :class="{ 'is-scrolled': isScrolled }">
      <div class="container d-flex align-items-center justify-content-between">
        <div class="header-logo-container">
          <RouterLink to="/">
            <img :src="logoSrc" alt="Logo" class="site-logo" />
          </RouterLink>
        </div>

        <nav class="header-nav">
          <ul class="nav-list">
            <li class="nav-item" v-for="link in navLinks" :key="link.path">
              <RouterLink
                :to="link.path"
                class="nav-link"
                active-class="is-active"
                :class="{ 'text-red-sale': link.isSale }"
              >
                <i :class="link.icon"></i> {{ link.label }}
              </RouterLink>
            </li>
          </ul>
        </nav>

        <div class="header-actions">
          <div class="search-input-container">
            <el-input
              v-model.trim="searchQuery"
              placeholder="Tìm kiếm..."
              :prefix-icon="Search"
              @keyup.enter="performSearch"
              class="header-search-input"
            />
          </div>

          <div class="user-menu-container">
            <button
              class="header-icon-btn"
              @click="toggleUserDropdown"
              type="button"
              aria-label="Tài khoản"
            >
              <el-icon :size="24"><User /></el-icon>
            </button>
            <Transition name="fade">
              <div v-if="showUserOptions" class="user-dropdown-menu">
                <template v-if="user">
                  <span class="dropdown-greeting"
                    >👋 {{ user.customerName || user.employeeName }}</span
                  >
                  <RouterLink to="/don-hang" class="dropdown-item" @click="closeDropdown"
                    ><i class="fas fa-receipt"></i> Đơn hàng</RouterLink
                  >
                  <RouterLink to="/san-pham-da-mua" class="dropdown-item" @click="closeDropdown">
                    <i class="fas fa-box-open"></i> Sản phẩm đã mua
                  </RouterLink>
                  <RouterLink to="/thong-tin-ca-nhan" class="dropdown-item" @click="closeDropdown"
                    ><i class="fas fa-user-circle"></i> Thông tin cá nhân</RouterLink
                  >
                  <RouterLink to="/ma-giam-gia" class="dropdown-item" @click="closeDropdown"
                    ><i class="fas fa-tags"></i> Mã giảm giá</RouterLink
                  >
                  <button class="btn-logout" @click="logout">
                    <i class="fas fa-sign-out-alt"></i> Đăng xuất
                  </button>
                </template>
                <template v-else>
                  <button class="btn-register" @click="openRegisterModal">Đăng ký</button>
                  <button class="btn-login" @click="openLoginModal">Đăng nhập</button>
                </template>
              </div>
            </Transition>
          </div>

          <RouterLink to="/cart" class="header-icon-btn cart-icon-container" aria-label="Giỏ hàng">
            <el-icon :size="24"><ShoppingCart /></el-icon>
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </RouterLink>

          <RouterLink to="/san-pham-yeu-thich" class="header-icon-btn cart-icon-container" aria-label="Giỏ hàng">
            <i class="bi bi-heart heart-icon"></i>
          </RouterLink>
        </div>
      </div>
    </header>

    <LoginModal
      :isVisible="showLogin"
      @update:isVisible="showLogin = $event"
      @loggedIn="handleLoggedIn"
      @openRegister="openRegisterModal"
    />
    <RegisterCustomerModal
      :isVisible="showRegister"
      @update:isVisible="showRegister = $event"
      @customerAdded="handleRegisterSuccess"
      @openLogin="openLoginModal"
    />

    <Transition name="fade">
      <el-button
        v-if="showScrollTopButton"
        class="scroll-to-top-btn"
        :icon="ArrowUp"
        circle
        size="large"
        type="primary"
        @click="scrollToTop"
      />
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElIcon, ElInput, ElButton } from 'element-plus'
import { User, Search, ShoppingCart, ArrowUp } from '@element-plus/icons-vue'

// Import các component con và tài nguyên
import LoginModal from '@/component/LoginModal.vue'
import RegisterCustomerModal from '@/component/RegisterCustomerModal.vue'
import logoSrc from '@/img/logo.png'

// Khởi tạo
const router = useRouter()

// --- STATE ---
const user = ref(null)
const cartCount = ref(0)
const searchQuery = ref('')
const showLogin = ref(false)
const showRegister = ref(false)
const showUserOptions = ref(false)
const isScrolled = ref(false)
const showScrollTopButton = ref(false)

const navLinks = [
  { path: '/', label: 'TRANG CHỦ' },
  { path: '/collections', label: 'SẢN PHẨM' },
  { path: '/', label: 'SẢN PHẨM' },
  { path: '/', label: 'SẢN PHẨM' },
  { path: '/', label: 'GIẢM GIÁ', isSale: true },
  { path: '/', label: 'STEAL KARINA STYLE' },
]

// --- LIFECYCLE HOOKS ---
onMounted(() => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    user.value = JSON.parse(storedUser)
  }
  updateCartCount()

  window.addEventListener('scroll', handleScroll)
  window.addEventListener('cart-updated', updateCartCount)
  document.addEventListener('click', handleClickOutsideUserMenu)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('cart-updated', updateCartCount)
  document.removeEventListener('click', handleClickOutsideUserMenu)
})

// --- WATCHERS ---
watch(user, () => {
  updateCartCount()
})

// --- METHODS ---
function updateCartCount() {
  try {
    const userId = user.value?.id || 'guest'
    const cartKey = `cart_${userId}`
    const cart = JSON.parse(localStorage.getItem(cartKey) || '[]')
    cartCount.value = Array.isArray(cart)
      ? cart.reduce((acc, item) => acc + (item.quantity || 0), 0)
      : 0
  } catch (error) {
    console.error('Lỗi khi cập nhật giỏ hàng:', error)
    cartCount.value = 0
  }
}

const handleLoggedIn = (userData) => {
  localStorage.setItem('user', JSON.stringify(userData))
  user.value = userData
  ElMessage.success('Đăng nhập thành công!')
  showLogin.value = false
}

const handleRegisterSuccess = () => {
  ElMessage.success('Đăng ký thành công! Vui lòng đăng nhập.')
  showRegister.value = false
  showLogin.value = true
}

const logout = () => {
  const userId = user.value?.id // Lấy userId trước khi user.value bị xóa

  // --- BẮT ĐẦU XÓA TẤT CẢ CÁC MỤC LIÊN QUAN ĐẾN PHIÊN ---

  // 1. Xóa thông tin người dùng chính (đối tượng JSON được lưu dưới khóa 'user')
  localStorage.removeItem('user')

  // 2. Xóa các mục riêng lẻ nếu chúng vẫn còn tồn tại (do cách lưu trữ cũ hoặc lỗi/thiếu đồng bộ)
  localStorage.removeItem('token')
  localStorage.removeItem('employeeName')
  localStorage.removeItem('customerName')
  localStorage.removeItem('userId')

  // 3. Xóa các mục giỏ hàng
  if (userId) {
    localStorage.removeItem(`cart_${userId}`) // Giỏ hàng của người dùng đã đăng nhập
  }
  localStorage.removeItem('cart_guest') // Giỏ hàng của khách vãng lai
  localStorage.removeItem('cart') // Xóa key 'cart' chung nếu nó không phải là giỏ hàng guest/user cụ thể

  // 4. Nếu bạn có bất kỳ key nào khác liên quan đến phiên đăng nhập được lưu riêng lẻ,
  // hãy thêm chúng vào đây. Ví dụ:
  // localStorage.removeItem('my_access_token');
  // localStorage.removeItem('my_refresh_token');

  // --- KẾT THÚC XÓA TẤT CẢ CÁC MỤC LIÊN QUAN ĐẾN PHIÊN ---

  // Đặt lại trạng thái trong ứng dụng sau khi đăng xuất
  user.value = null
  cartCount.value = 0
  showUserOptions.value = false // Đóng dropdown menu
  ElMessage.success('Đăng xuất thành công!')
  router.push('/') // Chuyển hướng về trang chủ hoặc trang đăng nhập
}
const handleScroll = () => {
  const scrollPosition = window.scrollY
  isScrolled.value = scrollPosition > 50
  showScrollTopButton.value = scrollPosition > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const performSearch = () => {
  if (!searchQuery.value) return
  router.push({ path: '/search-results', query: { q: searchQuery.value } })
}

const toggleUserDropdown = (event) => {
  event.stopPropagation()
  showUserOptions.value = !showUserOptions.value
}

const closeDropdown = () => {
  showUserOptions.value = false
}

const openLoginModal = () => {
  closeDropdown()
  showLogin.value = true
}

const openRegisterModal = () => {
  closeDropdown()
  showRegister.value = true
}

const handleClickOutsideUserMenu = (event) => {
  if (showUserOptions.value && !event.target.closest('.user-menu-container')) {
    showUserOptions.value = false
  }
}
</script>

<style scoped>
/* --- FONT & GLOBAL --- */
:root {
  --primary-color: #007bff;
  --danger-color: #dc3545;
  --text-dark: #343a40;
  --text-light: #6c757d;
  --border-color: #dee2e6;
}

.header-wrapper {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: #fff;
}

/* --- THANH THÔNG BÁO CHẠY CHỮ --- */
@keyframes scroll-text {
  0% {
    transform: translateX(0%);
  }
  100% {
    transform: translateX(-50%);
  }
}
.top-announcement-bar {
  background-color: #000;
  color: #fff;
  padding: 10px 0;
  font-size: 14px;
  overflow: hidden;
  white-space: nowrap;
}
.scrolling-text {
  margin: 0;
  animation: scroll-text 30s linear infinite;
  display: inline-block;
}
.scrolling-text span {
  padding: 0 2rem;
}

/* --- HEADER CHÍNH --- */
.main-header {
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease-in-out;
  padding: 10px 0;
}
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}
.main-header.is-scrolled {
  padding: 5px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* Logo */
.site-logo {
  height: 80px;
  transition: height 0.3s ease-in-out;
}
.main-header.is-scrolled .site-logo {
  height: 60px;
}

/* Navigation */
.header-nav {
  flex-grow: 1;
  display: flex;
  justify-content: center;
}
.nav-list {
  display: flex;
  gap: 1rem;
  list-style: none;
  margin: 0;
  padding: 0;
}
.nav-link {
  font-family: 'Product Sans', sans-serif;
  font-size: 16px;
  padding: 10px 15px;
  color: var(--text-dark);
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
  transition: color 0.2s ease;
  border-radius: 5px;
}
.nav-link:hover,
.nav-link.is-active {
  color: var(--primary-color);
}
.nav-link.text-red-sale {
  color: var(--danger-color) !important;
}

/* Header Actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}
.header-search-input {
  width: 220px;
}
.header-icon-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  color: var(--text-dark);
  position: relative;
}
.header-icon-btn:hover {
  color: var(--primary-color);
}

/* Cart Badge */
.cart-icon-container {
  position: relative;
}
.cart-badge {
  position: absolute;
  top: -5px;
  right: -10px;
  background-color: var(--danger-color);
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

/* User Dropdown */
.user-menu-container {
  position: relative;
}
.user-dropdown-menu {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  background-color: #fff;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  padding: 10px;
  width: 200px;
  z-index: 1010;
}
.dropdown-greeting {
  display: block;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
}
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  text-decoration: none;
  color: var(--text-dark);
  font-size: 15px;
  border-radius: 5px;
  transition: background-color 0.2s;
}
.dropdown-item:hover {
  background-color: #f8f9fa;
}
.user-dropdown-menu .btn-logout,
.user-dropdown-menu .btn-login,
.user-dropdown-menu .btn-register {
  width: 100%;
  padding: 8px;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 600;
  margin-top: 5px;
  border: 1px solid var(--border-color);
}
.user-dropdown-menu .btn-login {
  background-color: var(--primary-color);
  color: black;
  border-color: var(--primary-color);
}
.user-dropdown-menu .btn-logout {
  background-color: #f8f9fa;
  color: var(--danger-color);
  border-color: var(--danger-color);
}

/* Scroll-to-top Button */
.scroll-to-top-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1050;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

/* Transition Fades */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 992px) {
  .header-nav,
  .search-input-container {
    display: none;
  }
}

.heart-icon {
  font-size: 24px;
  color: black;
  transition: color 0.2s ease;
}

.cart-icon-container:hover .heart-icon {
  color: red;
}

</style>

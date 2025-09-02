<template>
  <div class="header-wrapper">
    <!-- Top marquee -->
    <div class="top-announcement-bar">
      <p class="scrolling-text">
        <span>Chào mừng đến với cửa hàng giày Sunshine! Mua sắm ngay hôm nay để nhận ưu đãi đặc biệt!</span>
      </p>
    </div>

    <!-- Header -->
    <header class="main-header" :class="{ 'is-scrolled': isScrolled }">
      <div class="container d-flex align-items-center justify-content-between">
        <!-- Logo -->
        <div class="header-logo-container">
          <RouterLink to="/">
            <img :src="logoSrc" alt="Logo" class="site-logo" />
          </RouterLink>
        </div>

        <!-- NAV -->
        <nav class="header-nav">
          <ul class="nav-list">
            <li
              v-for="link in navLinks"
              :key="link.path + link.label"
              class="nav-item"
              :class="{ 'brand-item': link.label === 'THƯƠNG HIỆU' || link.label === 'DANH MỤC' }"
              @mouseenter="
                (link.label === 'THƯƠNG HIỆU' && openBrandMenu()) ||
                (link.label === 'DANH MỤC' && openCategoryMenu())
              "
              @mouseleave="
                (link.label === 'THƯƠNG HIỆU' && delayCloseBrandMenu()) ||
                (link.label === 'DANH MỤC' && delayCloseCategoryMenu())
              "
            >
              <RouterLink
                :to="link.label === 'THƯƠNG HIỆU' || link.label === 'DANH MỤC' ? '' : link.path"
                class="nav-link"
                active-class="is-active"
                :class="{ 'text-red-sale': link.isSale }"
                @click.prevent="link.label === 'THƯƠNG HIỆU' || link.label === 'DANH MỤC'"
              >
                <i :class="link.icon"></i> {{ link.label }}
              </RouterLink>

              <!-- BRAND MEGA -->
              <Transition name="fade">
                <div
                  v-if="showBrandMenu && link.label === 'THƯƠNG HIỆU'"
                  class="brand-mega"
                  @mouseenter="cancelCloseTimer()"
                  @mouseleave="delayCloseBrandMenu"
                >
                  <div class="brand-mega__content">
                    <div v-if="brandLoading" class="brand-state">Đang tải thương hiệu…</div>
                    <div v-else-if="brandError" class="brand-state brand-state--error">
                      {{ brandError }}
                    </div>

                    <template v-else>
                      <div class="brand-col">
                        <div class="brand-col__title">A - G</div>
                        <ul class="brand-list">
                          <li v-for="b in brandGroups.AG" :key="'AG-' + b.id">
                            <a href="#" @click.prevent="gotoBrand(b)">{{ b.name }}</a>
                          </li>
                        </ul>
                      </div>
                      <div class="brand-col">
                        <div class="brand-col__title">H - R</div>
                        <ul class="brand-list">
                          <li v-for="b in brandGroups.HR" :key="'HR-' + b.id">
                            <a href="#" @click.prevent="gotoBrand(b)">{{ b.name }}</a>
                          </li>
                        </ul>
                      </div>
                      <div class="brand-col">
                        <div class="brand-col__title">S - Z</div>
                        <ul class="brand-list">
                          <li v-for="b in brandGroups.SZ" :key="'SZ-' + b.id">
                            <a href="#" @click.prevent="gotoBrand(b)">{{ b.name }}</a>
                          </li>
                        </ul>
                      </div>
                    </template>
                  </div>
                </div>
              </Transition>

              <!-- CATEGORY MEGA -->
              <Transition name="fade">
                <div
                  v-if="showCategoryMenu && link.label === 'DANH MỤC'"
                  class="category-mega"
                  @mouseenter="cancelCloseCategoryTimer"
                  @mouseleave="delayCloseCategoryMenu"
                >
                  <div class="category-mega__content">
                    <div v-if="categoryLoading" class="brand-state">Đang tải danh mục…</div>
                    <div v-else-if="categoryError" class="brand-state brand-state--error">
                      {{ categoryError }}
                    </div>

                    <template v-else>
                      <ul class="category-grid">
                        <li v-for="c in categoryListSorted" :key="c.id">
                          <a href="#" @click.prevent="gotoCategory(c)">{{ c.name }}</a>
                        </li>
                      </ul>
                    </template>
                  </div>
                </div>
              </Transition>
            </li>
          </ul>
        </nav>

        <!-- Actions -->
        <div class="header-actions">
          <!-- SEARCH -->
          <div class="search-input-container" ref="searchContainerRef">
            <el-input
              v-model.trim="searchQuery"
              placeholder="Nhập tên sản phẩm muốn tìm..."
              :prefix-icon="Search"
              class="header-search-input"
              @input="performSearchSuggestions()"
              @focus="handleSearchFocus"
              @keydown.down.prevent="moveSelection(1)"
              @keydown.up.prevent="moveSelection(-1)"
              @keyup.enter.prevent="handleEnter"
              @keydown.esc.prevent="hideSearchDropdown"
            />
            <el-button
              type="primary"
              :icon="Search"
              class="search-btn"
              @click="performSearchSuggestions(true)"
            >
              Tìm
            </el-button>

            <Transition name="fade">
              <div
                v-if="showSearchResults"
                class="search-results-dropdown"
                role="listbox"
                :aria-busy="searchLoading"
              >
                <div v-if="searchLoading" class="dropdown-state">Đang tìm kiếm...</div>
                <div v-else-if="searchError" class="dropdown-state dropdown-state--error">
                  {{ searchError }}
                </div>
                <div v-else-if="searchResults.length === 0" class="dropdown-state">
                  Không tìm thấy sản phẩm nào.
                </div>

                <div v-else class="product-list">
                  <RouterLink
                    v-for="(product, idx) in searchResults"
                    :key="product.id"
                    :to="`/product/${product.id}`"
                    class="product-item"
                    :class="{ 'is-active': idx === selectedIndex }"
                    role="option"
                    @mousedown.prevent="selectProduct(product)"
                  >
                    <img :src="getImageSrc(product)" alt="" class="product-item-image" />
                    <div class="product-item-info">
                      <div class="product-item-name">{{ product.productName }}</div>
                      <div class="product-item-price">{{ formatPrice(product.sellPrice) }}</div>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </Transition>
          </div>

          <!-- User -->
          <div class="user-menu-container">
            <button class="header-icon-btn" @click="toggleUserDropdown" type="button" aria-label="Tài khoản">
              <el-icon :size="24"><User /></el-icon>
            </button>

            <Transition name="fade">
              <div v-if="showUserOptions" class="user-dropdown-menu">
                <template v-if="user">
                  <span class="dropdown-greeting">Welcome: {{ user.customerName || user.employeeName }}</span>

                  <RouterLink to="/don-hang" class="dropdown-item" @click="closeDropdown">
                    <el-icon><Document /></el-icon> Đơn hàng
                  </RouterLink>

                   <RouterLink to="/don-hang-da-mua" class="dropdown-item" @click="closeDropdown">
                    <el-icon><Box /></el-icon> Đánh giá sản phẩm
                  </RouterLink>

                  <RouterLink to="/thong-tin-ca-nhan" class="dropdown-item" @click="closeDropdown">
                    <el-icon><User /></el-icon> Thông tin cá nhân
                  </RouterLink>

                  <RouterLink to="/ma-giam-gia" class="dropdown-item" @click="closeDropdown">
                    <el-icon><Tickets /></el-icon> Mã giảm giá
                  </RouterLink>

                  <button class="btn-logout" @click="logout">
                    <el-icon><SwitchButton /></el-icon> Đăng xuất
                  </button>
                </template>

                <template v-else>
                  <button class="btn-register" @click="openRegisterModal">Đăng ký</button>
                  <button class="btn-login" @click="openLoginModal">Đăng nhập</button>
                </template>
              </div>
            </Transition>
          </div>

          <!-- Cart -->
          <RouterLink to="/cart" class="header-icon-btn cart-icon-container" aria-label="Giỏ hàng">
            <el-icon :size="24"><ShoppingCart /></el-icon>
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </RouterLink>

          <!-- Favorite -->
          <RouterLink
            to="/san-pham-yeu-thich"
            class="header-icon-btn cart-icon-container"
            aria-label="Yêu thích"
          >
            <i class="bi bi-heart heart-icon"></i>
          </RouterLink>
        </div>
      </div>
    </header>

    <!-- Modals -->
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

    <!-- Scroll to top -->
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, onBeforeRouteUpdate, RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, Search, ShoppingCart, ArrowUp, Tickets, Box, Document, SwitchButton,
} from '@element-plus/icons-vue'

import LoginModal from '@/component/LoginModal.vue'
import RegisterCustomerModal from '@/component/RegisterCustomerModal.vue'
import logoSrc from '@/img/logo.png'
import apiClient from '@/utils/axiosInstance'

const router = useRouter()

/* ================= CORE STATE ================= */
const user = ref(null)
const cartCount = ref(0)
const searchQuery = ref('')
const showLogin = ref(false)
const showRegister = ref(false)
const showUserOptions = ref(false)
const isScrolled = ref(false)
const showScrollTopButton = ref(false)

/* ================= NAV ================= */
const navLinks = [
  { path: '/', label: 'TRANG CHỦ' },
  { path: '/gioi-thieu', label: 'GIỚI THIỆU' },
  { path: '/collections', label: 'SẢN PHẨM' },
  { path: '/san-pham-uu-dai', label: 'ƯU ĐÃI', isSale: true },
  { path: '/', label: 'THƯƠNG HIỆU' },
  { path: '/', label: 'DANH MỤC' }
]

/* ================= BRAND MENU ================= */
const showBrandMenu = ref(false)
const brandLoading = ref(false)
const brandError = ref('')
const brandList = ref([])
let closeTimer = null

const brandGroups = computed(() => {
  const groups = { AG: [], HR: [], SZ: [] }
  const list = Array.isArray(brandList.value) ? brandList.value : []

  const normalize = (s) => (s || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim()
  list.forEach((raw) => {
    const id = raw.id ?? raw.brandId ?? raw.code ?? raw._id ?? String(Math.random())
    const name = raw.brandName ?? raw.name ?? raw.brand_name ?? ''
    const clean = normalize(name)
    if (!clean) return
    const first = (clean[0] || 'Z').toUpperCase()
    const push = (bucket) => bucket.push({ id, name: name.trim() })
    if ('ABCDEFG'.includes(first)) push(groups.AG)
    else if ('HIJKLMNOPQR'.includes(first)) push(groups.HR)
    else push(groups.SZ)
  })
  const byName = (a, b) => a.name.localeCompare(b.name, 'vi', { sensitivity: 'base' })
  groups.AG.sort(byName); groups.HR.sort(byName); groups.SZ.sort(byName)
  return groups
})

async function fetchBrands() {
  try {
    brandLoading.value = true
    brandError.value = ''
    const res = await fetch('http://localhost:8080/api/online-sale/brand/hien-thi')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    brandList.value = Array.isArray(data) ? data : data?.data || data?.content || []
  } catch (e) {
    console.error(e)
    brandError.value = 'Không tải được danh sách thương hiệu.'
  } finally {
    brandLoading.value = false
  }
}
function openBrandMenu() { cancelCloseTimer(); showBrandMenu.value = true; if (!brandList.value.length && !brandLoading.value) fetchBrands() }
function delayCloseBrandMenu() { cancelCloseTimer(); closeTimer = setTimeout(() => (showBrandMenu.value = false), 150) }
function cancelCloseTimer() { if (closeTimer) { clearTimeout(closeTimer); closeTimer = null } }
function gotoBrand(b) {
  showBrandMenu.value = false
  const idNum = Number(b.id ?? b.brandId ?? b.code ?? b._id)
  router.push({
    path: '/collections',
    query: { brandId: Number.isFinite(idNum) ? idNum : b.id, brandName: b.name, page: 1, size: 12 },
  })
}

/* ================= CATEGORY MENU ================= */
const showCategoryMenu = ref(false)
const categoryLoading = ref(false)
const categoryError = ref('')
const categoryList = ref([])
let catTimer = null

async function fetchCategories() {
  if (categoryLoading.value || categoryList.value.length) return
  try {
    categoryLoading.value = true
    categoryError.value = ''
    const res = await fetch('http://localhost:8080/api/online-sale/categories/hien-thi')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    categoryList.value = Array.isArray(data) ? data : data?.data || data?.content || []
  } catch (e) {
    console.error(e)
    categoryError.value = 'Không tải được danh mục.'
  } finally {
    categoryLoading.value = false
  }
}
const categoryListSorted = computed(() => {
  const src = Array.isArray(categoryList.value) ? categoryList.value : []
  const rows = src
    .map((x) => ({ id: x.id ?? x.categoryId ?? x._id ?? x.code ?? String(Math.random()), name: x.categoryName ?? x.name ?? x.title ?? 'Danh mục' }))
    .filter((x) => x.id && x.name)
  rows.sort((a, b) => a.name.localeCompare(b.name, 'vi', { sensitivity: 'base' }))
  return rows
})

function openCategoryMenu() { cancelCloseCategoryTimer(); showCategoryMenu.value = true; fetchCategories() }
function delayCloseCategoryMenu() { cancelCloseCategoryTimer(); catTimer = setTimeout(() => (showCategoryMenu.value = false), 150) }
function cancelCloseCategoryTimer() { if (catTimer) { clearTimeout(catTimer); catTimer = null } }
function gotoCategory(c) { showCategoryMenu.value = false; router.push({ path: '/collections', query: { categoryId: c.id, categoryName: c.name, page: 1, size: 12 } }) }

/* ================= CART & USER ================= */
function updateCartCount() {
  try {
    const userId = user.value?.id || 'guest'
    const cartKey = `cart_${userId}`
    const cart = JSON.parse(localStorage.getItem(cartKey) || '[]')
    cartCount.value = Array.isArray(cart) ? cart.reduce((acc, item) => acc + (item.quantity || 0), 0) : 0
  } catch { cartCount.value = 0 }
}
const handleLoggedIn = (userData) => { localStorage.setItem('user', JSON.stringify(userData)); user.value = userData; ElMessage.success('Đăng nhập thành công!'); showLogin.value = false }
const handleRegisterSuccess = () => { ElMessage.success('Đăng ký thành công! Vui lòng đăng nhập.'); showRegister.value = false; showLogin.value = true }
const logout = () => {
  const userId = user.value?.id
   localStorage.removeItem('user'); localStorage.removeItem('token'); localStorage.removeItem('employeeName'); localStorage.removeItem('customerName'); localStorage.removeItem('userId') ;  localStorage.removeItem('customerId')
   if (userId) localStorage.removeItem(`cart_${userId}`)
   localStorage.removeItem('cart_guest'); localStorage.removeItem('cart') 
   user.value = null; cartCount.value = 0; showUserOptions.value = false

  ElMessage.success('Đăng xuất thành công!'); router.push('/')
}

/* ================= SEARCH DROPDOWN ================= */
const searchResults = ref([])
const showSearchResults = ref(false)
const searchLoading = ref(false)
const searchError = ref('')
let searchTimer = null
const searchContainerRef = ref(null)
const selectedIndex = ref(-1)

const PLACEHOLDER_IMG = '/placeholder.png' // đổi sang đường dẫn ảnh mặc định của bạn

function getImageSrc(p) {
  // Lấy ảnh đầu tiên từ danh sách productImages
  const first = Array.isArray(p?.productImages) && p.productImages.length > 0 ? p.productImages[0] : null
  if (!first || !first.image) return PLACEHOLDER_IMG
  // Nếu là dataURL sẵn thì trả về luôn, nếu là base64 byte[] thì wrap
  if (typeof first.image === 'string' && first.image.startsWith('data:image')) return first.image
  return `data:image/png;base64,${first.image}`
}

function handleSearchFocus() { if (searchResults.value.length > 0) showSearchResults.value = true }
function hideSearchDropdown() { showSearchResults.value = false; selectedIndex.value = -1 }
function selectProduct(product) { clearSearch(); router.push(`/product/${product.id}`) }

async function handleEnter() {
  if (!searchQuery.value) return
  // Bấm Enter: gọi quick-search NOW để đảm bảo dữ liệu mới nhất (không phân trang)
  await performSearchNow()

  if (selectedIndex.value >= 0 && selectedIndex.value < searchResults.value.length) {
    const p = searchResults.value[selectedIndex.value]
    selectProduct(p)
  } else {
    // Nếu muốn Enter điều hướng tới trang kết quả tổng:
    router.push({ path: '/search-results', query: { q: searchQuery.value } })
    clearSearch()
  }
}

function moveSelection(step) {
  if (searchResults.value.length === 0) return
  showSearchResults.value = true
  const len = searchResults.value.length
  if (selectedIndex.value === -1) selectedIndex.value = step > 0 ? 0 : len - 1
  else selectedIndex.value = (selectedIndex.value + step + len) % len
}

/* Gợi ý khi gõ (nếu muốn dùng API gợi ý khác). Có thể bỏ nếu chỉ dùng quick-search. */
async function performSearchSuggestions(force = false) {
  if (!searchQuery.value && !force) {
    searchResults.value = []; showSearchResults.value = false; selectedIndex.value = -1
    return
  }
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    searchLoading.value = true; searchError.value = ''; showSearchResults.value = true
    try {
      // Nếu bạn có API gợi ý riêng: thay thế đoạn này
      // Ở đây mình vẫn có thể gọi quick-search để hiển thị 5 sản phẩm ngay dưới dropdown
      const response = await apiClient.get('/online-sale/quick-search', { params: { productName: searchQuery.value } })
      const items = response?.data ?? []
      searchResults.value = Array.isArray(items) ? items.slice(0, 5) : []
      selectedIndex.value = searchResults.value.length ? 0 : -1
    } catch (e) {
      console.error('Lỗi tìm kiếm:', e)
      searchError.value = 'Không thể tìm kiếm sản phẩm.'
      searchResults.value = []; selectedIndex.value = -1
    } finally {
      searchLoading.value = false
    }
  }, 300)
}

/* Quick-search NOW (Enter) – GET + query param, không phân trang */
async function performSearchNow() {
  searchLoading.value = true
  searchError.value = ''
  showSearchResults.value = true
  try {
    const response = await apiClient.get('/online-sale/quick-search', { params: { productName: searchQuery.value } })
    const items = response?.data ?? []
    searchResults.value = Array.isArray(items) ? items : []
    selectedIndex.value = searchResults.value.length ? 0 : -1
  } catch (e) {
    console.error('Lỗi tìm kiếm (NOW):', e)
    searchError.value = 'Không thể tìm kiếm sản phẩm.'
    searchResults.value = []; selectedIndex.value = -1
  } finally {
    searchLoading.value = false
  }
}

function clearSearch() { searchQuery.value = ''; showSearchResults.value = false; searchResults.value = []; selectedIndex.value = -1 }
function handleClickOutsideSearch(event) { const el = searchContainerRef.value; if (el && !el.contains(event.target)) hideSearchDropdown() }

/* ================= UI HANDLERS ================= */
function handleScroll() {
  const y = window.scrollY
  isScrolled.value = y > 50
  showScrollTopButton.value = y > 300
  if (showBrandMenu.value) showBrandMenu.value = false
  if (showCategoryMenu.value) showCategoryMenu.value = false
}
function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }
function toggleUserDropdown(e) { e.stopPropagation(); showUserOptions.value = !showUserOptions.value }
function closeDropdown() { showUserOptions.value = false }
function openLoginModal() { closeDropdown(); showLogin.value = true }
function openRegisterModal() { closeDropdown(); showRegister.value = true }
function handleClickOutsideUserMenu(event) { if (showUserOptions.value && !event.target.closest('.user-menu-container')) showUserOptions.value = false }

/* ================= LIFECYCLE ================= */
onMounted(() => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) user.value = JSON.parse(storedUser)
  updateCartCount()
  window.addEventListener('scroll', handleScroll)
  window.addEventListener('cart-updated', updateCartCount)
  document.addEventListener('click', handleClickOutsideUserMenu)
  document.addEventListener('click', handleClickOutsideSearch)
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('cart-updated', updateCartCount)
  document.removeEventListener('click', handleClickOutsideUserMenu)
  document.removeEventListener('click', handleClickOutsideSearch)
})
watch(user, updateCartCount)
onBeforeRouteUpdate(() => hideSearchDropdown())

/* ================= UTIL ================= */
function formatPrice(v) {
  if (v == null) return ''
  try { return Number(v).toLocaleString('vi-VN') + ' ₫' } catch { return v }
}
</script>

<style scoped>
:root {
  --primary-color: #007bff;
  --danger-color: #dc3545;
  --text-dark: #343a40;
  --text-light: #6c757d;
  --border-color: #dee2e6;
}

.header-wrapper { position: sticky; top: 0; z-index: 1000; background-color: #fff; }

/* Marquee */
@keyframes scroll-text { 0% { transform: translateX(0%); } 100% { transform: translateX(-50%); } }
.top-announcement-bar { background-color: #000; color: #fff; padding: 10px 0; font-size: 14px; overflow: hidden; white-space: nowrap; }
.scrolling-text { margin: 0; animation: scroll-text 30s linear infinite; display: inline-block; }
.scrolling-text span { padding: 0 2rem; }

/* Header */
.main-header { border-bottom: 1px solid var(--border-color); transition: all 0.3s ease-in-out; padding: 10px 0; }
.container { max-width: 1400px; margin: 0 auto; padding: 0 20px; }
.main-header.is-scrolled { padding: 5px 0; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); }

/* Logo */
.site-logo { height: 80px; transition: height 0.3s ease-in-out; }
.main-header.is-scrolled .site-logo { height: 60px; }

/* Nav */
.header-nav { flex-grow: 1; display: flex; justify-content: center; }
.nav-list { display: flex; gap: 1rem; list-style: none; margin: 0; padding: 0; }
.nav-link { font-family: 'Product Sans', sans-serif; font-size: 16px; padding: 10px 15px; color: var(--text-dark); font-weight: 700; text-decoration: none; white-space: nowrap; transition: color 0.2s ease; border-radius: 5px; }
.nav-link:hover, .nav-link.is-active { color: var(--primary-color); }
.nav-link.text-red-sale { color: var(--danger-color) !important; }

/* Actions */
.header-actions { display: flex; align-items: center; gap: 1.5rem; }
.header-search-input { width: 260px; }
.search-input-container { position: relative; }
.search-btn { margin-left: 8px; }
.search-results-dropdown {
  position: absolute; top: calc(100% + 8px); left: 0; width: 480px; max-height: 420px;
  overflow: auto; background: #fff; border: 1px solid var(--border-color); border-radius: 10px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12); padding: 8px; z-index: 2000;
}
.dropdown-state { padding: 18px 8px; text-align: center; color: var(--text-light); }
.dropdown-state--error { color: var(--danger-color); }

/* Product list */
.product-list { display: flex; flex-direction: column; gap: 6px; }
.product-item { display: flex; gap: 10px; padding: 8px; border-radius: 8px; text-decoration: none; }
.product-item:hover, .product-item.is-active { background: #f6f7f9; }
.product-item-image { width: 56px; height: 56px; object-fit: cover; border-radius: 6px; border: 1px solid #eee; }
.product-item-info { display: flex; flex-direction: column; justify-content: center; }
.product-item-name { font-weight: 600; color: #111; line-height: 1.2; }
.product-item-price { color: #0b61ff; font-weight: 700; }

/* Icons */
.header-icon-btn { background: none; border: none; padding: 0; cursor: pointer; color: var(--text-dark); position: relative; }
.header-icon-btn:hover { color: var(--primary-color); }

/* Cart badge */
.cart-icon-container { position: relative; }
.cart-badge {
  position: absolute; top: -5px; right: -10px; background-color: var(--danger-color); color: #fff;
  border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: bold;
}

/* User dropdown */
.user-menu-container { position: relative; }
.user-dropdown-menu {
  position: absolute; top: calc(100% + 10px); right: 0; background: #fff; border: 1px solid var(--border-color);
  border-radius: 8px; box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1); padding: 10px; width: 220px; z-index: 1010;
}
.dropdown-greeting { display: block; text-align: center; font-size: 14px; font-weight: 600; color: var(--text-dark);
  margin-bottom: 10px; padding-bottom: 10px; border-bottom: 1px solid var(--border-color);
}
.dropdown-item { display: flex; align-items: center; gap: 10px; padding: 8px 10px; text-decoration: none; color: var(--text-dark); font-size: 15px; border-radius: 5px; transition: background-color 0.2s; }
.dropdown-item:hover { background-color: #f8f9fa; }
.user-dropdown-menu .btn-logout,
.user-dropdown-menu .btn-login,
.user-dropdown-menu .btn-register { width: 100%; padding: 8px; border-radius: 5px; cursor: pointer; font-weight: 600; margin-top: 5px; border: 1px solid var(--border-color); }
.user-dropdown-menu .btn-login { background-color: var(--primary-color); color: #000; border-color: var(--primary-color); }
.user-dropdown-menu .btn-logout { background-color: #f8f9fa; color: var(--danger-color); border-color: var(--danger-color); }

/* BRAND mega */
.brand-item { position: relative; }
.brand-mega {
  position: absolute; left: 0; top: calc(100% + 10px); width: 960px; background: #fff; border: 1px solid var(--border-color);
  border-radius: 10px; box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12); padding: 18px 22px; z-index: 1200;
}
.brand-mega__content { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 28px; }
.brand-col__title { font-weight: 700; color: var(--text-dark); margin-bottom: 10px; padding-bottom: 8px; border-bottom: 1px solid var(--border-color); }
.brand-list { list-style: none; margin: 0; padding: 0; max-height: 420px; overflow: auto; }
.brand-list li a { display: block; padding: 6px 2px; text-decoration: none; color: var(--text-dark); border-radius: 6px; line-height: 1.2; }
.brand-list li a:hover { background: #f6f7f9; color: var(--primary-color); }
.brand-state { grid-column: 1 / -1; text-align: center; padding: 24px 0; color: var(--text-light); }
.brand-state--error { color: var(--danger-color); }

/* CATEGORY mega */
.category-mega {
  position: absolute; left: 0; top: calc(100% + 10px); width: 900px; background: #fff; border: 1px solid var(--border-color);
  border-radius: 10px; box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12); padding: 18px 22px; z-index: 1200;
}
.category-mega__content { max-height: 420px; overflow: auto; }
.category-grid { list-style: none; margin: 0; padding: 0; columns: 4; column-gap: 24px; }
.category-grid li { break-inside: avoid; }
.category-grid li a { display: block; padding: 6px 2px; color: var(--text-dark); text-decoration: none; border-radius: 6px; }
.category-grid li a:hover { background: #f6f7f9; color: var(--primary-color); }

/* Scroll to top */
.scroll-to-top-btn { position: fixed; bottom: 30px; right: 30px; z-index: 1050; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2); }

/* Fade */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* Responsive */
@media (max-width: 992px) {
  .header-nav, .search-input-container, .brand-mega, .category-mega { display: none; }
}

.heart-icon { font-size: 24px; color: black; transition: color .2s ease; }
.cart-icon-container:hover .heart-icon { color: red; }
</style>

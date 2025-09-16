<template>
  <div class="header-wrapper">
    <!-- ===== Marquee ===== -->
    <div class="top-announcement-bar">
      <p class="scrolling-text">
        <span>Chào mừng đến với Sunshine! Mua sắm ngay hôm nay để nhận ưu đãi đặc biệt!</span>
      </p>
    </div>

    <!-- ===== Header ===== -->
    <header class="main-header" :class="{ 'is-scrolled': isScrolled }">
      <div class="container d-flex align-items-center justify-content-between">
        <!-- Logo -->
        <RouterLink to="/" class="header-logo-container">
          <img :src="logoSrc" alt="Logo" class="site-logo" />
        </RouterLink>

        <!-- ===== NAV ===== -->
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
                    <div v-else-if="brandError" class="brand-state brand-state--error">{{ brandError }}</div>
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
                    <div v-else-if="categoryError" class="brand-state brand-state--error">{{ categoryError }}</div>
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

        <!-- ===== Actions ===== -->
        <div class="header-actions">
          <!-- ===== SEARCH with suggestions ===== -->
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
            <el-button type="primary" :icon="Search" class="search-btn" @click="performSearchSuggestions(true)">Tìm</el-button>

            <Transition name="fade">
              <div v-if="showSearchResults" class="search-results-dropdown" role="listbox" :aria-busy="searchLoading">
                <div v-if="searchLoading" class="dropdown-state">Đang tìm kiếm...</div>
                <div v-else-if="searchError" class="dropdown-state dropdown-state--error">{{ searchError }}</div>
                <div v-else-if="searchResults.length === 0" class="dropdown-state">Không tìm thấy sản phẩm nào.</div>
                <div v-else class="product-list">
                  <RouterLink
                    v-for="(p, idx) in searchResults"
                    :key="p.id"
                    :to="`/product/${p.id}`"
                    class="product-item"
                    :class="{ 'is-active': idx === selectedIndex }"
                    role="option"
                    @mousedown.prevent="selectProduct(p)"
                  >
                    <img :src="getImageSrc(p)" alt="" class="product-item-image" />
                    <div class="product-item-info">
                      <div class="product-item-name">{{ p.productName }}</div>
                      <div class="product-item-price">{{ formatPrice(p.sellPrice) }}</div>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </Transition>
          </div>

          <!-- ===== USER MENU ===== -->
          <div class="user-menu-container">
            <button class="header-icon-btn" @click="toggleUserDropdown" type="button" aria-label="Tài khoản">
              <el-icon :size="24"><User /></el-icon>
            </button>

            <Transition name="fade">
              <div v-if="showUserOptions" class="user-dropdown-menu" @click.stop>
                <template v-if="user">
                  <span class="dropdown-greeting">Welcome: {{ user.customerName || user.employeeName }}</span>

                  <RouterLink to="/don-hang" class="dropdown-item" @click="closeUserDropdown">
                    <el-icon><Document /></el-icon> Đơn hàng
                  </RouterLink>
                  <RouterLink to="/don-hang-da-mua" class="dropdown-item" @click="closeUserDropdown">
                    <el-icon><Box /></el-icon> Đánh giá sản phẩm
                  </RouterLink>
                  <RouterLink to="/thong-tin-ca-nhan" class="dropdown-item" @click="closeUserDropdown">
                    <el-icon><User /></el-icon> Thông tin cá nhân
                  </RouterLink>
                  <RouterLink to="/ma-giam-gia" class="dropdown-item" @click="closeUserDropdown">
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

          <!-- ===== CART ===== -->
          <RouterLink to="/cart" class="header-icon-btn cart-icon-container" aria-label="Giỏ hàng">
            <el-icon :size="24"><ShoppingCart /></el-icon>
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </RouterLink>

          <!-- ===== FAVORITE ===== -->
          <RouterLink to="/san-pham-yeu-thich" class="header-icon-btn cart-icon-container" aria-label="Yêu thích">
            <i class="bi bi-heart heart-icon"></i>
            <span v-if="favoriteCount > 0" class="fav-badge">{{ favoriteCount }}</span>
          </RouterLink>

          <!-- ===== BELL (tách riêng) ===== -->
          <div class="notify-menu-container" ref="notifyContainerRef">
            <button class="header-icon-btn relative" @click="toggleNotifyDropdown" type="button" aria-label="Thông báo">
              <el-icon :size="24"><Bell /></el-icon>
              <span v-if="unread > 0" class="notify-badge">{{ unread }}</span>
            </button>

            <Transition name="fade">
              <div v-if="showNotify" class="notify-dropdown" @click.stop>
                <div class="notify-header">
                  <span class="title">Thông báo</span>
                  <div class="tools">
                    <button class="link" @click.stop="markAllRead">Đánh dấu đã đọc</button>
                    <button class="link" @click.stop="clearNotifications">Xoá</button>
                  </div>
                </div>

                <div v-if="notifications.length===0" class="notify-empty">Chưa có thông báo</div>

                <div v-else class="notify-list">
                  <div v-for="(n, idx) in notifications" :key="idx" class="notify-item" @click="goNotify(n)">
                    <div class="row1">
                      <span class="tit">{{ n.title || 'Thông báo' }}</span>
                    </div>
                    <div v-if="n.content" class="content">{{ n.content }}</div>
                    <div class="meta">
                      <span v-if="n.at">{{ formatTime(n.at) }}</span>
                      <span v-if="n.invoiceCode">• Đơn: {{ n.invoiceCode }}</span>
                      <span v-if="n.nextStatus">• {{ n.nextStatus }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </header>

    <!-- ===== Modals ===== -->
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

    <!-- ===== Scroll to top ===== -->
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
import { RouterLink, useRouter, onBeforeRouteUpdate } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Search, ShoppingCart, ArrowUp, Tickets, Box, Document, SwitchButton, Bell } from '@element-plus/icons-vue'

import LoginModal from '@/component/LoginModal.vue'
import RegisterCustomerModal from '@/component/RegisterCustomerModal.vue'
import logoSrc from '@/img/logo.png'

import apiClient from '@/utils/axiosInstance'
import { WsClient } from '@/utils/ws'
import cartService from '@/utils/cart.js'

// ===== Core =====
const router = useRouter()
const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
const getToken = () => localStorage.getItem('access_token') || localStorage.getItem('token') || ''

const user = ref(null)
const cartCount = ref(0)
const favoriteCount = ref(0)
const searchQuery = ref('')
const showLogin = ref(false)
const showRegister = ref(false)
const showUserOptions = ref(false)
const isScrolled = ref(false)
const showScrollTopButton = ref(false)

// ===== Nav =====
const navLinks = [
  { path: '/', label: 'TRANG CHỦ' },
  { path: '/collections', label: 'SẢN PHẨM' },
  { path: '/san-pham-uu-dai', label: 'ƯU ĐÃI', isSale: true },
  { path: '/', label: 'THƯƠNG HIỆU' },
  { path: '/', label: 'DANH MỤC' }
]

// ===== Brand menu =====
const showBrandMenu = ref(false)
const brandLoading = ref(false)
const brandError = ref('')
const brandList = ref([])
let closeTimer = null

const brandGroups = computed(() => {
  const groups = { AG: [], HR: [], SZ: [] }
  const list = Array.isArray(brandList.value) ? brandList.value : []
  const norm = (s) => (s || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim()
  list.forEach((raw) => {
    const id = raw.id ?? raw.brandId ?? raw.code ?? raw._id ?? String(Math.random())
    const name = raw.brandName ?? raw.name ?? raw.brand_name ?? ''
    const clean = norm(name)
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
    const res = await fetch(`${API_BASE}/api/online-sale/brand/hien-thi`)
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
  router.push({ path: '/collections', query: { brandId: Number.isFinite(idNum) ? idNum : b.id, brandName: b.name, page: 1, size: 12 } })
}

// ===== Category menu =====
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
    const res = await fetch(`${API_BASE}/api/online-sale/categories/hien-thi`)
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
  const rows = src.map((x) => ({
    id: x.id ?? x.categoryId ?? x._id ?? x.code ?? String(Math.random()),
    name: x.categoryName ?? x.name ?? x.title ?? 'Danh mục'
  })).filter((x) => x.id && x.name)
  rows.sort((a, b) => a.name.localeCompare(b.name, 'vi', { sensitivity: 'base' }))
  return rows
})
function openCategoryMenu() { cancelCloseCategoryTimer(); showCategoryMenu.value = true; fetchCategories() }
function delayCloseCategoryMenu() { cancelCloseCategoryTimer(); catTimer = setTimeout(() => (showCategoryMenu.value = false), 150) }
function cancelCloseCategoryTimer() { if (catTimer) { clearTimeout(catTimer); catTimer = null } }
function gotoCategory(c) { showCategoryMenu.value = false; router.push({ path: '/collections', query: { categoryId: c.id, categoryName: c.name, page: 1, size: 12 } }) }

// ===== Search =====
const searchResults = ref([])
const showSearchResults = ref(false)
const searchLoading = ref(false)
const searchError = ref('')
let searchTimer = null
const searchContainerRef = ref(null)
const selectedIndex = ref(-1)

const PLACEHOLDER_IMG = '/placeholder.png'
function getImageSrc(p) {
  const first = Array.isArray(p?.productImages) && p.productImages.length > 0 ? p.productImages[0] : null
  if (!first || !first.image) return PLACEHOLDER_IMG
  if (typeof first.image === 'string' && first.image.startsWith('data:image')) return first.image
  return `data:image/png;base64,${first.image}`
}
function handleSearchFocus() { if (searchResults.value.length > 0) showSearchResults.value = true }
function hideSearchDropdown() { showSearchResults.value = false; selectedIndex.value = -1 }
function selectProduct(product) { clearSearch(); router.push(`/product/${product.id}`) }
async function handleEnter() {
  if (!searchQuery.value) return
  await performSearchNow()
  if (selectedIndex.value >= 0 && selectedIndex.value < searchResults.value.length) {
    const p = searchResults.value[selectedIndex.value]; selectProduct(p)
  } else {
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
async function performSearchSuggestions(force = false) {
  if (!searchQuery.value && !force) {
    searchResults.value = []; showSearchResults.value = false; selectedIndex.value = -1
    return
  }
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    searchLoading.value = true; searchError.value = ''; showSearchResults.value = true
    try {
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

// ===== UI handlers =====
function handleScroll() {
  const y = window.scrollY
  isScrolled.value = y > 50
  showScrollTopButton.value = y > 300
  if (showBrandMenu.value) showBrandMenu.value = false
  if (showCategoryMenu.value) showCategoryMenu.value = false
}
function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }
function toggleUserDropdown(e) { e.stopPropagation(); showUserOptions.value = !showUserOptions.value; showNotify.value = false }
function closeUserDropdown() { showUserOptions.value = false }
function openLoginModal() { closeUserDropdown(); showLogin.value = true }
function openRegisterModal() { closeUserDropdown(); showRegister.value = true }
function handleClickOutsideUserMenu(event) { if (showUserOptions.value && !event.target.closest('.user-menu-container')) showUserOptions.value = false }

// ===== Cart & Favorite =====
function currentUserId() {
  try { return user.value?.id || JSON.parse(localStorage.getItem('user')||'{}')?.id || null } catch { return null }
}
async function updateCartCount() {
  const uid = currentUserId()
  cartCount.value = await cartService.getCartCount(uid)
}
async function updateFavoriteCount() {
  try {
    const cid = user.value?.customerId || JSON.parse(localStorage.getItem('user')||'{}')?.customerId
    if (!cid) { favoriteCount.value = 0; return }
    const fav = await cartService.getFavoritesByCustomer(cid)
    favoriteCount.value = Array.isArray(fav) ? fav.length : 0
  } catch { favoriteCount.value = 0 }
}

// ===== Auth =====
const handleLoggedIn = async (userData) => {
  if (userData?.token) localStorage.setItem('access_token', userData.token)
  localStorage.setItem('user', JSON.stringify(userData))
  user.value = userData
  await cartService.migrateGuestCartToUser(userData.id)
  await updateCartCount()
  await updateFavoriteCount()

  ElMessage.success('Đăng nhập thành công!')
  showLogin.value = false

  ws.deactivate()
  ws.activate()
  subscribeCustomerTopic()
}
const handleRegisterSuccess = () => {
  ElMessage.success('Đăng ký thành công! Vui lòng đăng nhập.')
  showRegister.value = false
  showLogin.value = true
}
const logout = async () => {
  const uid = user.value?.id
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  localStorage.removeItem('access_token')
  localStorage.removeItem('employeeName')
  localStorage.removeItem('customerName')
  localStorage.removeItem('userId')
  localStorage.removeItem('customerId')
  if (uid) localStorage.removeItem(`cart_${uid}`)
  localStorage.removeItem('cart_guest')

  user.value = null
  cartCount.value = await cartService.getCartCount(null)
  favoriteCount.value = 0
  showUserOptions.value = false

  ws.deactivate()

  ElMessage.success('Đăng xuất thành công!')
  router.push('/')
}

// ===== Notifications (BELL) =====
const notifications = ref([]) // [{title,content,invoiceCode,nextStatus,at,customerId}]
const unread = ref(0)
const showNotify = ref(false)
const notifyContainerRef = ref(null)

function toggleNotifyDropdown(e) {
  e.stopPropagation()
  showNotify.value = !showNotify.value
  showUserOptions.value = false
}
function handleClickOutsideNotify(event) {
  const el = notifyContainerRef.value
  if (showNotify.value && el && !el.contains(event.target)) showNotify.value = false
}
function goNotify(n) {
  if (n?.invoiceCode) router.push({ path: '/don-hang', query: { code: n.invoiceCode } })
}

const ws = new WsClient({
  baseUrl: API_BASE,
  endpoint: '/ws',
  getToken,
  onError: (err) => console.error('[WS] error', err),
  onConnect: () => { subscribeCustomerTopic() }
})

ws.onUserQueue((data) => { if (!allowMessageForCurrentUser(data)) return; notifications.value.unshift(data); unread.value += 1 })
ws.onCustomerTopic((data) => { if (!allowMessageForCurrentUser(data)) return; notifications.value.unshift(data); unread.value += 1 })

function allowMessageForCurrentUser(data) {
  const payloadCid = data?.customerId
  const myCid = user.value?.customerId || JSON.parse(localStorage.getItem('user') || '{}')?.customerId
  if (payloadCid == null || myCid == null) return true
  return String(payloadCid) === String(myCid)
}
function subscribeCustomerTopic() {
  const cid = user.value?.customerId || JSON.parse(localStorage.getItem('user') || '{}')?.customerId
  ws.subscribeCustomerTopic(cid)
}
const markAllRead = () => {
  unread.value = 0
  try {
    // TODO: gọi API mark-read nếu backend có
  } catch {}
}
const clearNotifications = () => { notifications.value = []; unread.value = 0 }
const formatTime = (iso) => { if (!iso) return ''; try { return new Date(iso).toLocaleString() } catch { return iso } }

// ===== Lifecycle =====
onMounted(async () => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) user.value = JSON.parse(storedUser)

  await updateCartCount()
  await updateFavoriteCount()

  window.addEventListener('scroll', handleScroll)
  window.addEventListener('cart-updated', async (e) => {
    // cartService đã phát event kèm totalQty
    cartCount.value = Number(e?.detail?.totalQty ?? (await cartService.getCartCount(currentUserId())))
  })
  window.addEventListener('cart-cleared', async () => { cartCount.value = await cartService.getCartCount(currentUserId()) })
  window.addEventListener('favorite-updated', updateFavoriteCount)
  document.addEventListener('click', handleClickOutsideUserMenu)
  document.addEventListener('click', handleClickOutsideSearch)
  document.addEventListener('click', handleClickOutsideNotify)

  ws.activate()
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('favorite-updated', updateFavoriteCount)
  document.removeEventListener('click', handleClickOutsideUserMenu)
  document.removeEventListener('click', handleClickOutsideSearch)
  document.removeEventListener('click', handleClickOutsideNotify)
  ws.deactivate()
})
watch(user, async () => { await updateCartCount(); await updateFavoriteCount(); subscribeCustomerTopic() })
onBeforeRouteUpdate(() => hideSearchDropdown())

// ===== Utils =====
function formatPrice(v) { if (v == null) return ''; try { return Number(v).toLocaleString('vi-VN') + ' ₫' } catch { return v } }
</script>

<style scoped>
:root { --primary-color:#007bff; --danger-color:#dc3545; --text-dark:#343a40; --text-light:#6c757d; --border-color:#dee2e6 }
.header-wrapper{position:sticky;top:0;z-index:1000;background:#fff}

/* Marquee */
@keyframes scroll-text{0%{transform:translateX(0%)}100%{transform:translateX(-50%)}}
.top-announcement-bar{background:#000;color:#fff;padding:10px 0;font-size:14px;overflow:hidden;white-space:nowrap}
.scrolling-text{margin:0;animation:scroll-text 30s linear infinite;display:inline-block}
.scrolling-text span{padding:0 2rem}

/* Header */
.main-header{border-bottom:1px solid var(--border-color);transition:all .3s ease-in-out;padding:10px 0}
.container{max-width:1400px;margin:0 auto;padding:0 20px}
.main-header.is-scrolled{padding:5px 0;box-shadow:0 4px 12px rgba(0,0,0,.08)}
.site-logo{height:80px;transition:height .3s ease-in-out}
.main-header.is-scrolled .site-logo{height:60px}

/* Nav */
.header-nav{flex-grow:1;display:flex;justify-content:center}
.nav-list{display:flex;gap:1rem;list-style:none;margin:0;padding:0}
.nav-link{font-family:'Product Sans',sans-serif;font-size:16px;padding:10px 15px;color:var(--text-dark);font-weight:700;text-decoration:none;white-space:nowrap;transition:color .2s;border-radius:5px}
.nav-link:hover,.nav-link.is-active{color:var(--primary-color)}
.nav-link.text-red-sale{color:var(--danger-color)!important}

/* Actions */
.header-actions{display:flex;align-items:center;gap:1.2rem}
.header-search-input{width:260px}
.search-input-container{position:relative}
.search-btn{margin-left:8px}
.search-results-dropdown{position:absolute;top:calc(100% + 8px);left:0;width:480px;max-height:420px;overflow:auto;background:#fff;border:1px solid var(--border-color);border-radius:10px;box-shadow:0 12px 30px rgba(0,0,0,.12);padding:8px;z-index:2000}
.dropdown-state{padding:18px 8px;text-align:center;color:var(--text-light)}
.dropdown-state--error{color:var(--danger-color)}
.product-list{display:flex;flex-direction:column;gap:6px}
.product-item{display:flex;gap:10px;padding:8px;border-radius:8px;text-decoration:none}
.product-item:hover,.product-item.is-active{background:#f6f7f9}
.product-item-image{width:56px;height:56px;object-fit:cover;border-radius:6px;border:1px solid #eee}
.product-item-info{display:flex;flex-direction:column;justify-content:center}
.product-item-name{font-weight:600;color:#111;line-height:1.2}
.product-item-price{color:#0b61ff;font-weight:700}

/* Icons */
.header-icon-btn{background:none;border:none;padding:0;cursor:pointer;color:var(--text-dark);position:relative}
.header-icon-btn:hover{color:var(--primary-color)}

/* Badges */
.notify-badge{position:absolute;top:-5px;right:-8px;min-width:18px;height:18px;padding:0 6px;border-radius:999px;background:#dc3545;color:#fff;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:700}
.cart-icon-container{position:relative}
.cart-badge{position:absolute;top:-5px;right:-10px;background:var(--danger-color);color:#da2d2d;border-radius:50%;width:20px;height:20px;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700}
.fav-badge{position:absolute;top:-5px;right:-10px;background:#0b61ff;color:#d31111;border-radius:50%;min-width:18px;height:18px;display:flex;align-items:center;justify-content:center;padding:0 5px;font-size:11px;font-weight:700}

/* User dropdown */
.user-menu-container{position:relative}
.user-dropdown-menu{position:absolute;top:calc(100% + 10px);right:0;background:#fff;border:1px solid var(--border-color);border-radius:8px;box-shadow:0 6px 20px rgba(0,0,0,.1);padding:10px;width:260px;z-index:1010}
.dropdown-greeting{display:block;text-align:center;font-size:14px;font-weight:600;color:var(--text-dark);margin-bottom:10px;padding-bottom:10px;border-bottom:1px solid var(--border-color)}
.dropdown-item{display:flex;align-items:center;gap:10px;padding:8px 10px;text-decoration:none;color:var(--text-dark);font-size:15px;border-radius:5px;transition:background-color .2s}
.dropdown-item:hover{background:#f8f9fa}
.user-dropdown-menu .btn-logout,.user-dropdown-menu .btn-login,.user-dropdown-menu .btn-register{width:100%;padding:8px;border-radius:5px;cursor:pointer;font-weight:600;margin-top:5px;border:1px solid var(--border-color)}
.user-dropdown-menu .btn-login{background:var(--primary-color);color:#000;border-color:var(--primary-color)}
.user-dropdown-menu .btn-logout{background:#f8f9fa;color:var(--danger-color);border-color:var(--danger-color)}

/* Notify dropdown (bell) */
.notify-menu-container{position:relative}
.notify-dropdown{position:absolute;top:calc(100% + 10px);right:0;background:#fff;border:1px solid var(--border-color);border-radius:10px;box-shadow:0 12px 30px rgba(0,0,0,.12);padding:10px;width:360px;z-index:1050}
.notify-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:6px}
.notify-header .title{font-weight:600}
.notify-header .tools .link{font-size:12px;color:#2563eb;background:none;border:none;margin-left:8px;cursor:pointer}
.notify-empty{font-size:12px;color:#6b7280}
.notify-list{max-height:320px;overflow:auto}
.notify-item{padding:8px;border:1px solid #f1f1f1;border-radius:8px;margin-bottom:6px;background:#fff}
.notify-item .row1{display:flex;gap:8px;align-items:center;justify-content:space-between}
.notify-item .tit{font-size:13px;font-weight:600}
.notify-item .content{font-size:12px;color:#4b5563;margin-top:2px}
.notify-item .meta{font-size:11px;color:#9ca3af;margin-top:3px}

/* BRAND mega */
.brand-item{position:relative}
.brand-mega{position:absolute;left:0;top:calc(100% + 10px);width:960px;background:#fff;border:1px solid var(--border-color);border-radius:10px;box-shadow:0 12px 30px rgba(0,0,0,.12);padding:18px 22px;z-index:1200}
.brand-mega__content{display:grid;grid-template-columns:1fr 1fr 1fr;gap:28px}
.brand-col__title{font-weight:700;color:var(--text-dark);margin-bottom:10px;padding-bottom:8px;border-bottom:1px solid var(--border-color)}
.brand-list{list-style:none;margin:0;padding:0;max-height:420px;overflow:auto}
.brand-list li a{display:block;padding:6px 2px;text-decoration:none;color:var(--text-dark);border-radius:6px;line-height:1.2}
.brand-list li a:hover{background:#f6f7f9;color:#var(--primary-color)}
.brand-state{grid-column:1/-1;text-align:center;padding:24px 0;color:#var(--text-light)}
.brand-state--error{color:var(--danger-color)}

/* CATEGORY mega */
.category-mega{position:absolute;left:0;top:calc(100% + 10px);width:900px;background:#fff;border:1px solid var(--border-color);border-radius:10px;box-shadow:0 12px 30px rgba(0,0,0,.12);padding:18px 22px;z-index:1200}
.category-mega__content{max-height:420px;overflow:auto}
.category-grid{list-style:none;margin:0;padding:0;columns:4;column-gap:24px}
.category-grid li{break-inside:avoid}
.category-grid li a{display:block;padding:6px 2px;color:var(--text-dark);text-decoration:none;border-radius:6px}
.category-grid li a:hover{background:#f6f7f9;color:#var(--primary-color)}

/* Scroll to top */
.scroll-to-top-btn{position:fixed;bottom:30px;right:30px;z-index:1050;box-shadow:0 2px 10px rgba(0,0,0,.2)}

/* Fade */
.fade-enter-active,.fade-leave-active{transition:opacity .2s ease}
.fade-enter-from,.fade-leave-to{opacity:0}

/* Responsive */
@media (max-width: 992px){.header-nav,.search-input-container,.brand-mega,.category-mega{display:none}}

.heart-icon{font-size:24px;color:black;transition:color .2s}
.cart-icon-container:hover .heart-icon{color:red}
</style>

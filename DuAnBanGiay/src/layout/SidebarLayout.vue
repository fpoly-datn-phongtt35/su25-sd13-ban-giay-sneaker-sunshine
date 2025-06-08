<template>
  <nav class="d-flex flex-column bg-white p-3 min-vh-100 border-end" style="width: 250px">
    <!-- Logo -->
    <div class="d-flex align-items-center mb-4">
      <img
        :src="logoSrc"
        alt="Logo"
        class="rounded-circle me-3"
        style="width: 50px; height: 50px; object-fit: cover"
      />
      <span class="h4 text-dark">Clothing Store</span>
    </div>

    <!-- Navigation -->
    <ul class="nav flex-column">
      <!-- Trang chủ -->
      <li class="nav-item">
        <RouterLink
          class="nav-link py-3"
          :class="{ active: selected === 'home' }"
          to="/home"
          @click="selectMenu('home')"
        >
          <i class="bi bi-house-door me-2"></i> Trang chủ
        </RouterLink>
      </li>

      <!-- Bán hàng -->
      <li class="nav-item">
        <div
          class="nav-link py-3 d-flex justify-content-between align-items-center"
          @click="toggleCollapse('sales')"
        >
          <span><i class="bi bi-cart-fill me-2"></i> Bán hàng</span>
          <i class="bi" :class="isSalesOpen ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <ul v-if="isSalesOpen" class="nav flex-column ms-3">
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'sales-counter' }"
              to="/sales-counter"
              @click="selectMenu('sales-counter')"
            >
              <i class="bi bi-cash me-2"></i> Bán hàng tại quầy
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'sales-online' }"
              to="/sales-online"
              @click="selectMenu('sales-online')"
            >
              <i class="bi bi-laptop me-2"></i> Bán hàng online
            </RouterLink>
          </li>
        </ul>
      </li>

      <!-- Quản lý sản phẩm -->
      <li class="nav-item">
        <div
          class="nav-link py-3 d-flex justify-content-between align-items-center"
          @click="toggleCollapse('product')"
        >
          <span><i class="bi bi-box-seam me-2"></i> Quản lý sản phẩm</span>
          <i class="bi" :class="isProductOpen ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <ul v-if="isProductOpen" class="nav flex-column ms-3">
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'products' }"
              to="/product"
              @click="selectMenu('products')"
            >
              <i class="bi bi-box me-2"></i> Sản phẩm
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'discounts' }"
              to="/voucher"
              @click="selectMenu('discounts')"
            >
              <i class="bi bi-tag me-2"></i> Mã giảm giá
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'suppliers' }"
              to="/supplier"
              @click="selectMenu('suppliers')"
            >
              <i class="bi bi-truck me-2"></i> Nhà cung cấp
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'soles' }"
              to="/sole"
              @click="selectMenu('soles')"
            >
              <i class="bi bi-grid me-2"></i> Đế giày
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'collars' }"
              to="/style"
              @click="selectMenu('collars')"
            >
              <i class="bi bi-circle-half me-2"></i> Cổ giày
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'materials' }"
              to="/material"
              @click="selectMenu('materials')"
            >
              <i class="bi bi-layers me-2"></i> Chất liệu
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'categories' }"
              to="/categories"
              @click="selectMenu('categories')"
            >
              <i class="bi bi-list me-2"></i> Danh mục
            </RouterLink>
          </li>

          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'sizes' }"
              to="/size"
              @click="selectMenu('sizes')"
            >
              <i class="bi bi-arrows-angle-contract me-2"></i> Kích thước
            </RouterLink>
          </li>

          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'colors' }"
              to="/color"
              @click="selectMenu('colors')"
            >
              <i class="bi bi-palette me-2"></i> Màu sắc
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'brands' }"
              to="/brand"
              @click="selectMenu('brands')"
            >
              <i class="bi bi-tags me-2"></i> Thương hiệu
            </RouterLink>
          </li>
        </ul>
      </li>

      <!-- Quản lý người dùng -->
      <li class="nav-item">
        <RouterLink
          class="nav-link py-3"
          :class="{ active: selected === 'user' }"
          to="/employee"
          @click="selectMenu('user')"
        >
          <i class="bi bi-person me-2"></i> Quản lý người dùng
        </RouterLink>
      </li>

      <!-- Quản lý khách hàng -->
      <li class="nav-item">
        <RouterLink
          class="nav-link py-3"
          :class="{ active: selected === 'customers' }"
          to="/customer"
          @click="selectMenu('customers')"
        >
          <i class="bi bi-person-fill me-2"></i> Quản lý khách hàng
        </RouterLink>
      </li>

      <!-- Quản lý hóa đơn -->
      <li class="nav-item">
        <RouterLink
          class="nav-link py-3"
          :class="{ active: selected === 'invoices' }"
          to="/invoice"
          @click="selectMenu('invoices')"
        >
          <i class="bi bi-file-earmark me-2"></i> Quản lý hóa đơn
        </RouterLink>
      </li>

      <!-- Thống kê -->
      <li class="nav-item">
        <div
          class="nav-link py-3 d-flex justify-content-between align-items-center"
          @click="toggleCollapse('statistics')"
        >
          <span><i class="bi bi-graph-up me-2"></i> Thống kê</span>
          <i class="bi" :class="isStatisticsOpen ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <ul v-if="isStatisticsOpen" class="nav flex-column ms-3">
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'statistics-counter' }"
              to="/statistics/counter"
              @click="selectMenu('statistics-counter')"
            >
              <i class="bi bi-bar-chart me-2"></i> Tại quầy
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'statistics-online' }"
              to="/statistics/online"
              @click="selectMenu('statistics-online')"
            >
              <i class="bi bi-bar-chart-line me-2"></i> Online
            </RouterLink>
          </li>
        </ul>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { ref } from 'vue'

const selected = ref('home')
const selectMenu = (menu) => {
  selected.value = menu
}

const logoSrc = ref('https://img.icons8.com/?size=100&id=juRF5DiUGr4p&format=png&color=000000')

// Collapse states
const isSalesOpen = ref(true)
const isProductOpen = ref(true)
const isStatisticsOpen = ref(true)

const toggleCollapse = (section) => {
  if (section === 'sales') isSalesOpen.value = !isSalesOpen.value
  if (section === 'product') isProductOpen.value = !isProductOpen.value
  if (section === 'statistics') isStatisticsOpen.value = !isStatisticsOpen.value
}
</script>

<style scoped>
nav {
  background-color: #ffffff;
  color: #333;
}

.nav-link {
  color: black;
  cursor: pointer;
}

.nav-link:hover {
  background-color: #f1f1f1;
  color: black;
}

.nav-link.active {
  background-color: #007bff;
  color: white;
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
}

.nav-link i {
  font-size: 1.3rem;
}

.ms-3 {
  margin-left: 1rem;
}
</style>

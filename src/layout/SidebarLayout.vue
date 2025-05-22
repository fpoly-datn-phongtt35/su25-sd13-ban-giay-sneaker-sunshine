<template>
  <nav class="d-flex flex-column bg-white p-3 min-vh-100 border-end" style="width: 250px;">
    <!-- Logo Section -->
    <div class="d-flex align-items-center mb-4">
      <div class="bg-primary p-3 rounded-circle me-3">
        <i class="bi bi-house-door-fill text-white"></i>
      </div>
      <span class="h4 text-dark">Clothing Store</span>
    </div>

    <!-- Navigation Menu -->
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
        <div class="nav-link py-3">
          <i class="bi bi-cart-fill me-2"></i> Bán hàng
        </div>
        <ul class="nav flex-column ms-3">
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

      <!-- Quản lý -->
      <li class="nav-item">
        <div class="nav-link py-3">
          <i class="bi bi-gear-fill me-2"></i> Quản lý
        </div>
        <ul class="nav flex-column ms-3">
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
              to="/nha-cung-cap"
              @click="selectMenu('suppliers')"
            >
              <i class="bi bi-truck me-2"></i> Nhà cung cấp
            </RouterLink>
          </li>
          <!-- <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'stock' }"
              to="/management/stock"
              @click="selectMenu('stock')"
            >
              <i class="bi bi-boxes me-2"></i> Kho hàng
            </RouterLink>
          </li> -->
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
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'user' }"
              to="/employee"
              @click="selectMenu('user')"
            >
              <i class="bi bi-person me-2"></i> Người Dùng
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'customers' }"
              to="/customer"
              @click="selectMenu('customers')"
            >
              <i class="bi bi-person-fill me-2"></i> Khách hàng
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link py-2"
              :class="{ active: selected === 'invoices' }"
              to="/invoice"
              @click="selectMenu('invoices')"
            >
              <i class="bi bi-file-earmark me-2"></i> Hóa đơn
            </RouterLink>
          </li>
        </ul>
      </li>

      <!-- Thống kê -->
      <li class="nav-item">
        <div class="nav-link py-3">
          <i class="bi bi-graph-up me-2"></i> Thống kê
        </div>
        <ul class="nav flex-column ms-3">
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
import { ref } from 'vue';

const selected = ref('home');

const selectMenu = (menu) => {
  selected.value = menu;
};
</script>

<style scoped>
nav {
  background-color: #ffffff;
  color: #333;
}

.nav-link {
  color: black;
}

.nav-link:hover {
  background-color: #f1f1f1;
  color: black; /* Đảm bảo chữ vẫn đen khi hover */
}

.nav-link.active {
  background-color: #007bff;
  color: white; /* Giữ chữ trắng khi active nếu cần */
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
}

.nav-link i {
  font-size: 1.3rem;
}

.ms-3 {
  margin-left: 1rem;
}
</style>

<template>
  <nav class="sidebar-container">
    <el-menu
      :default-active="route.path"
      class="el-menu-vertical"
      router
    >
      <div class="logo-container">
        <img
          :src="logoSrc"
          alt="Logo"
          class="logo-img"
        />
        <span class="logo-text">SunShine</span>
      </div>

      <el-menu-item index="/home">
        <el-icon><i class="bi bi-house-door"></i></el-icon>
        <span>Trang chủ</span>
      </el-menu-item>

      <el-sub-menu index="sales">
        <template #title>
          <el-icon><i class="bi bi-cart-fill"></i></el-icon>
          <span>Bán hàng</span>
        </template>
        <el-menu-item index="/sales-counter/list">
          <el-icon><i class="bi bi-cash"></i></el-icon>
          <span>Bán hàng tại quầy</span>
        </el-menu-item>
        <el-menu-item index="/sales-online">
          <el-icon><i class="bi bi-laptop"></i></el-icon>
          <span>Bán hàng online</span>
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="product-management">
        <template #title>
          <el-icon><i class="bi bi-box-seam"></i></el-icon>
          <span>Quản lý sản phẩm</span>
        </template>
        <el-menu-item index="/product">
          <el-icon><i class="bi bi-box"></i></el-icon>
          <span>Sản phẩm</span>
        </el-menu-item>
        <el-menu-item index="/voucher">
          <el-icon><i class="bi bi-tag"></i></el-icon>
          <span>Mã giảm giá</span>
        </el-menu-item>
        <el-menu-item index="/supplier">
          <el-icon><i class="bi bi-truck"></i></el-icon>
          <span>Nhà cung cấp</span>
        </el-menu-item>
         <el-menu-item index="/sole">
          <el-icon><i class="bi bi-grid"></i></el-icon>
          <span>Đế giày</span>
        </el-menu-item>
        <el-menu-item index="/style">
          <el-icon><i class="bi bi-circle-half"></i></el-icon>
          <span>Cổ giày</span>
        </el-menu-item>
        <el-menu-item index="/material">
          <el-icon><i class="bi bi-layers"></i></el-icon>
          <span>Chất liệu</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><i class="bi bi-list"></i></el-icon>
          <span>Danh mục</span>
        </el-menu-item>
        <el-menu-item index="/size">
          <el-icon><i class="bi bi-arrows-angle-contract"></i></el-icon>
          <span>Kích thước</span>
        </el-menu-item>
        <el-menu-item index="/color">
          <el-icon><i class="bi bi-palette"></i></el-icon>
          <span>Màu sắc</span>
        </el-menu-item>
        <el-menu-item index="/brand">
          <el-icon><i class="bi bi-tags"></i></el-icon>
          <span>Thương hiệu</span>
        </el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/employee">
        <el-icon><i class="bi bi-person"></i></el-icon>
        <span>Quản lý người dùng</span>
      </el-menu-item>

      <el-menu-item index="/customer">
        <el-icon><i class="bi bi-person-fill"></i></el-icon>
        <span>Quản lý khách hàng</span>
      </el-menu-item>

      <el-menu-item index="/invoices">
        <el-icon><i class="bi bi-file-earmark"></i></el-icon>
        <span>Quản lý hóa đơn</span>
      </el-menu-item>

      <el-menu-item index="/discount-campaigns">
        <el-icon><i class="bi bi-file-earmark"></i></el-icon>
        <span>Quản lý đợt giảm giá</span>
      </el-menu-item>

      <el-sub-menu index="">
        <template #title>
          <el-icon><i class="bi bi-graph-up"></i></el-icon>
          <span>Thống kê</span>
        </template>
        <el-menu-item index="/statistics">
          <el-icon><i class="bi bi-bar-chart"></i></el-icon>
          <span>Thống kê chi tiết</span>
        </el-menu-item>

      </el-sub-menu>
    </el-menu>
  </nav>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  ElMenu,
  ElMenuItem,
  ElSubMenu,
  ElIcon
} from 'element-plus'

// Lấy thông tin route hiện tại để xác định menu active
const route = useRoute()

const logoSrc = ref('https://img.icons8.com/?size=100&id=juRF5DiUGr4p&format=png&color=000000')

// Các biến state và hàm để quản lý đóng/mở menu không còn cần thiết nữa.
// Element Plus sẽ tự động xử lý.
</script>

<style scoped>
.sidebar-container {
  background-color: #ffffff;
  width: 250px;
  min-height: 100vh;
  border-right: 1px solid #e6e6e6;
}

.el-menu-vertical {
  width: 100%;
  border-right: none; /* Bỏ border mặc định của el-menu */
}

.logo-container {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  margin-bottom: 10px;
}

.logo-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 50%;
  margin-right: 12px;
}

.logo-text {
  font-size: 1.25rem; /* Tương đương h4 */
  font-weight: 600;
  color: #303133;
}

/* Căn chỉnh lại icon */
.el-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem; /* Giữ kích thước icon như cũ */
  margin-right: 8px;
}

/* Ghi đè style của el-menu-item để trông đẹp hơn */
.el-menu-item,
.el-sub-menu__title {
    padding-left: 20px !important; /* Đồng bộ khoảng cách lề trái */
}

/* Style cho các mục con trong menu */
.el-menu-item-group .el-menu-item {
    padding-left: 40px !important;
}
</style>
<template>
  <header class="bg-white text-gray-800 py-4 shadow-md">
    <div class="container d-flex align-items-center justify-content-between">
      <div class="d-flex align-items-center gap-1 flex-shrink-0">
        <img :src="logoSrc" alt="Logo" class="h-10 w-10" /> 
        <h1 class="m-0 fw-bold text-2xl text-nowrap">Sunshine Shop</h1> 
      </div>

      <nav class="flex-grow-1 d-flex justify-content-start"> 
        <ul class="nav gap-1 m-0 p-0 flex-nowrap align-items-center"> 
          <li class="nav-item" v-for="link in navLinks" :key="link.path">
            <RouterLink
              :to="link.path"
              class="nav-link text-gray-700 fw-semibold d-flex align-items-center gap-1 text-nowrap"
              active-class="text-primary"
            >
              <i :class="link.icon"></i> {{ link.label }}
            </RouterLink>
          </li>
        </ul>
      </nav>

      <div class="d-flex align-items-center gap-4 flex-nowrap flex-shrink-0"> 
        <div class="search-input-container">
          <el-input
            v-model.trim="searchQuery"
            placeholder="Tìm kiếm..."
            :prefix-icon="Search"
            @keyup.enter="performSearchAndCloseModal"
            class="header-search-input"
          />
        </div>

        <div class="position-relative">
          <button class="btn btn-link p-0 border-0 text-gray-700 header-icon-btn" @click="toggleUserDropdown" type="button" aria-label="Tài khoản">
            <el-icon :size="24"><User /></el-icon>
          </button>
          <div
            v-if="showUserOptions"
            class="user-dropdown-menu position-absolute bg-white shadow-lg rounded py-2 px-3"
          >
            <template v-if="user">
              <span class="d-block text-center text-sm fw-semibold text-gray-700 mb-2">👋 {{ user.customerName || user.employeeName }}</span>
              <RouterLink to="/don-hang" class="dropdown-item d-flex align-items-center gap-2 py-2 mb-1" @click="showUserOptions = false">
                <i class="fas fa-receipt"></i> Đơn hàng
              </RouterLink>
              <RouterLink to="/thong-tin-ca-nhan" class="dropdown-item d-flex align-items-center gap-2 py-2 mb-1" @click="showUserOptions = false">
                <i class="fas fa-user-circle"></i> Thông tin cá nhân
              </RouterLink>
              <RouterLink to="/ma-giam-gia" class="dropdown-item d-flex align-items-center gap-2 py-2 mb-1" @click="showUserOptions = false">
                <i class="fas fa-tags"></i> Mã giảm giá
              </RouterLink>
              <button class="btn btn-outline-danger btn-sm w-100 mt-2" @click="logout" type="button">
                <i class="fas fa-sign-out-alt"></i> Đăng xuất
              </button>
            </template>
            <template v-else>
              <button class="btn btn-outline-primary btn-sm w-100 mb-2" @click="openRegisterModal" type="button">
                <i class="fas fa-user-plus"></i> Đăng ký
              </button>
              <button class="btn btn-primary btn-sm w-100" @click="openLoginModal" type="button">
                <i class="fas fa-sign-in-alt"></i> Đăng nhập
              </button>
            </template>
          </div>
        </div>

        <RouterLink to="/cart" class="btn btn-link p-0 border-0 text-gray-700 position-relative header-icon-btn" aria-label="Giỏ hàng">
          <el-icon :size="24"><ShoppingCart /></el-icon>
          <span
            v-if="cartCount > 0"
            class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
            >{{ cartCount }}</span
          >
        </RouterLink>
      </div>
    </div>

    <LoginModal
      :isVisible="showLogin"
      @update:isVisible="showLogin = $event"
      @loggedIn="handleLoggedIn"
      @openRegister="showRegister = true"
    />

    <RegisterCustomerModal
      :isVisible="showRegister"
      @update:isVisible="showRegister = $event"
      @customerAdded="handleRegisterSuccess"
      @openLogin="showLogin = true"
    />

    <el-dialog
      v-model="showSearchModal"
      title="Tìm kiếm sản phẩm"
      width="30%"
      @close="searchQuery = ''"
    >
      <el-input
        v-model.trim="searchQuery"
        placeholder="Nhập từ khóa tìm kiếm..."
        @keyup.enter="performSearchAndCloseModal"
      />
      <template #footer>
        <el-button @click="showSearchModal = false">Hủy</el-button>
        <el-button type="primary" @click="performSearchAndCloseModal">Tìm kiếm</el-button>
      </template>
    </el-dialog>

  </header>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Search, ShoppingCart } from '@element-plus/icons-vue'

import LoginModal from '@/component/LoginModal.vue'
import RegisterCustomerModal from '@/component/RegisterCustomerModal.vue'

const router = useRouter()

const logoSrc = ref('https://img.icons8.com/?size=100&id=juRF5DiUGr4p&format=png&color=000000')
const user = ref(null)
const cartCount = ref(0)
const searchQuery = ref('')
const showLogin = ref(false)
const showRegister = ref(false)
const showUserOptions = ref(false)
const showSearchModal = ref(false);

const navLinks = [
  { path: '/', label: 'Trang chủ', icon: 'fas fa-home' },
  { path: '/collections', label: 'Sản phẩm', icon: 'fas fa-box-open' },
  { path: '/chinh-sach', label: 'Sản phẩm bán chạy', icon: 'fas fa-scroll' },
  { path: '/gioi-thieu', label: 'Giới thiệu', icon: 'fas fa-info-circle' },
  { path: '/lien-he', label: 'Liên hệ', icon: 'fas fa-phone-alt' },
  // { path: '/don-hang', label: 'Đơn hàng', icon: 'fas fa-receipt' },
]

onMounted(() => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) user.value = JSON.parse(storedUser)
  updateCartCount()
  document.addEventListener('click', closeUserDropdownOnClickOutside);
})

onUnmounted(() => {
  document.removeEventListener('click', closeUserDropdownOnClickOutside);
});

watch(user, () => {
  updateCartCount()
})

function updateCartCount() {
  try {
    const userId = user.value?.id || 'guest'
    const cartKey = `cart_${userId}`
    const cart = JSON.parse(localStorage.getItem(cartKey) || '[]')
    cartCount.value = Array.isArray(cart) ? cart.reduce((acc, item) => acc + (item.quantity || 0), 0) : 0
  } catch {
    cartCount.value = 0
  }
}

const toggleUserDropdown = (event) => {
  event.stopPropagation();
  showUserOptions.value = !showUserOptions.value;
};

const openLoginModal = () => {
  showUserOptions.value = false;
  showLogin.value = true;
};

const openRegisterModal = () => {
  showUserOptions.value = false;
  showRegister.value = true;
};

const closeUserDropdownOnClickOutside = (event) => {
  // Kiểm tra nếu click bên ngoài dropdown và không phải nút kích hoạt dropdown
  if (showUserOptions.value &&
      !event.target.closest('.user-dropdown-menu') &&
      !event.target.closest('.header-icon-btn')
  ) {
    showUserOptions.value = false;
  }
};

const openSearchModal = () => {
  showSearchModal.value = true;
};

const performSearch = () => {
  const keyword = searchQuery.value.trim()
  if (!keyword) {
    ElMessage.info('Vui lòng nhập từ khóa tìm kiếm.')
    return
  }
  router.push({ path: '/search-results', query: { q: keyword } })
};

const performSearchAndCloseModal = () => {
  performSearch();
};

const handleLoggedIn = (userData) => {
  localStorage.setItem('user', JSON.stringify(userData))
  user.value = userData
  updateCartCount()
  ElMessage.success('Đăng nhập thành công!')
  showLogin.value = false; // Đóng modal đăng nhập
}

const handleRegisterSuccess = () => {
  ElMessage.success('Đăng ký thành công! Vui lòng đăng nhập.')
  showLogin.value = true
  showRegister.value = false; // Đóng modal đăng ký
}

const logout = () => {
  const userId = user.value?.id || 'guest'

  localStorage.removeItem('user')
  localStorage.removeItem('userId')
  localStorage.removeItem(`cart_${userId}`)

  user.value = null
  updateCartCount()
  showUserOptions.value = false; // Đóng dropdown sau khi đăng xuất

  ElMessage.success('Đăng xuất thành công!')
  router.push('/')
}
</script>

<style scoped>
/* Đảm bảo container chính của header căn giữa các mục con theo chiều dọc và có một chút khoảng cách từ lề */
.container.d-flex {
  align-items: center;
  height: 70px; /* Chiều cao cố định của header */
  padding: 0 1rem;
  max-width: none;
  width: 100%;
}

/* Logo và Tên Shop */
.d-flex.align-items-center.gap-1.flex-shrink-0 {
  /* No specific vertical alignment needed as parent container handles it */
}

/* Navigation Links (Menu) */
nav.flex-grow-1 {
  flex-grow: 1;
  display: flex;
  justify-content: flex-start; /* Căn các mục menu về phía trái của nav */
  align-items: center;
  padding: 0 1.5rem; /* Khoảng cách giữa logo và menu */
}

/* Danh sách Navigation (ul) */
nav ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 0.25rem; /* Khoảng cách giữa các mục menu */
}

/* Các Mục Link Navigation riêng lẻ (li) */
.nav-item {
  display: flex;
  align-items: center;
}

/* RouterLink bên trong Mục Nav - Điều chỉnh padding để to hơn */
.nav-link {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.75rem 1rem; /* Padding của các menu link */
  text-decoration: none;
  white-space: nowrap;
  color: #4a5568; /* Màu chữ mặc định */
  transition: color 0.2s ease;
}

.nav-link:hover {
  color: #007bff; /* Màu chữ khi hover */
}

.nav-link.text-primary {
  color: #007bff !important; /* Màu chữ khi active */
}

/* Các Icon và ô tìm kiếm bên phải */
.d-flex.align-items-center.gap-4.flex-nowrap.flex-shrink-0 {
  display: flex;
  align-items: center;
  gap: 2rem; /* Khoảng cách giữa ô tìm kiếm, tài khoản, giỏ hàng */
  flex-shrink: 0;
}

/* Ô input tìm kiếm trên header - Tăng chiều rộng */
.header-search-input {
  width: 250px; /* Chiều rộng của ô input */
  height: 36px; /* Chiều cao của ô input */
  --el-input-border-radius: 20px; /* Bo tròn góc input */
  --el-input-border-color: #dcdfe6;
  --el-input-hover-border-color: #409eff;
  --el-input-focus-border-color: #409eff;
}

/* Đảm bảo icon bên trong input không bị lệch */
.header-search-input .el-input__prefix {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Styling chung cho các nút icon (Tài khoản, Giỏ hàng) */
.header-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  min-height: 24px;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
}

/* Icon Element Plus bên trong các nút */
.header-icon-btn .el-icon {
  vertical-align: middle;
  color: #4a5568;
  transition: color 0.2s ease;
  font-size: 24px;
}

.header-icon-btn:hover .el-icon {
  color: #007bff;
}

/* Menu thả xuống của người dùng */
.user-dropdown-menu {
  top: calc(100% + 5px); /* Khoảng cách từ nút kích hoạt */
  right: 0; /* Căn lề phải với nút kích hoạt */
  z-index: 1000;
  min-width: 180px; /* Tăng chiều rộng tối thiểu cho menu dropdown */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid #e2e8f0; /* Thêm đường viền nhẹ */
}

/* Kiểu cho các mục trong dropdown menu */
.user-dropdown-menu .dropdown-item {
  display: flex;
  align-items: center;
  padding: 0.5rem 1rem;
  text-decoration: none;
  color: #333;
  transition: background-color 0.2s ease, color 0.2s ease;
  border-radius: 0.25rem; /* Bo tròn nhẹ các góc của mục */
}

.user-dropdown-menu .dropdown-item:hover {
  background-color: #f0f0f0;
  color: #007bff;
}

.user-dropdown-menu .dropdown-item i {
  margin-right: 0.5rem; /* Khoảng cách giữa icon và chữ */
}

/* Điều chỉnh cho màn hình nhỏ (responsive) */
@media (max-width: 992px) {
  .container.d-flex {
    flex-wrap: wrap;
    justify-content: center;
    padding: 0 1rem;
    height: auto;
  }

  .d-flex.align-items-center.gap-1.flex-shrink-0 { /* Logo */
    width: 100%;
    justify-content: center;
    margin-bottom: 1rem;
    order: 1;
  }

  nav.flex-grow-1 { /* Menu Navigation */
    width: 100%;
    order: 2;
    margin-bottom: 1rem;
    padding: 0;
    justify-content: center;
  }

  nav ul {
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.5rem;
  }

  .nav-link {
    padding: 0.4rem 0.6rem;
  }

  .d-flex.align-items-center.gap-3.flex-nowrap.flex-shrink-0 { /* Các icon bên phải */
    width: 100%;
    order: 3;
    justify-content: center;
    gap: 1rem;
    margin-top: 0.5rem;
  }

  /* Ẩn ô input tìm kiếm trên mobile, chỉ hiển thị icon */
  .search-input-container {
    display: none;
  }
}
</style>
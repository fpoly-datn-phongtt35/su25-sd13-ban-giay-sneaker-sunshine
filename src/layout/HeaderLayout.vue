<template>
  <header class="bg-white text-gray-800 py-4 shadow-md">
    <div class="container d-flex align-items-center justify-content-between">
      <div class="d-flex align-items-center gap-1 flex-shrink-0">
        <img :src="logoSrc" alt="Sunshine Shop Logo" class="h-8 w-8" />
        <h1 class="m-0 fw-bold text-xl text-nowrap">Sunshine Shop</h1>
      </div>

      <div class="search-bar d-flex flex-grow-1 mx-4">
        <div class="input-group">
          <input
            type="text"
            class="form-control custom-search-input"
            placeholder="Tìm kiếm sản phẩm..."
            aria-label="Tìm kiếm sản phẩm"
            v-model="searchQuery"
            @keyup.enter="performSearch"
          />
          <button class="btn btn-primary custom-search-button" type="button" @click="performSearch">
            <i class="fas fa-search"></i>
          </button>
        </div>
      </div>

      <nav class="flex-grow-1 d-flex justify-content-center px-4">
        <ul class="nav gap-3 m-0 p-0 flex-nowrap">
          <li class="nav-item">
            <RouterLink to="/" class="nav-link text-gray-700 hover:text-blue-600 fw-semibold d-flex align-items-center gap-1">
              <i class="fas fa-home"></i> Trang chủ
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink to="/product-list" class="nav-link text-gray-700 hover:text-blue-600 fw-semibold d-flex align-items-center gap-1">
              <i class="fas fa-box-open"></i> Sản phẩm
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink to="/chinh-sach" class="nav-link text-gray-700 hover:text-blue-600 fw-semibold d-flex align-items-center gap-1">
              <i class="fas fa-scroll"></i> Sản phẩm bán chạy
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink to="/gioi-thieu" class="nav-link text-gray-700 hover:text-blue-600 fw-semibold d-flex align-items-center gap-1">
              <i class="fas fa-info-circle"></i> Giới thiệu
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink to="/lien-he" class="nav-link text-gray-700 hover:text-blue-600 fw-semibold d-flex align-items-center gap-1">
              <i class="fas fa-phone-alt"></i> Liên hệ
            </RouterLink>
          </li>
          <li class="nav-item">
            <a href="#" class="nav-link text-gray-700 hover:text-blue-600 fw-semibold d-flex align-items-center gap-1" @click.prevent="handleOrderClick">
              <i class="fas fa-receipt"></i> Đơn hàng
            </a>
          </li>
        </ul>
      </nav>

      <div class="d-flex align-items-center gap-1 flex-nowrap flex-shrink-0">
        <template v-if="customer">
          <span class="fw-semibold text-nowrap text-sm text-gray-700 d-flex align-items-center gap-1">
            <i class="fas fa-user-circle"></i> 👋 {{ customer.customerName }}
          </span>
          <button class="btn btn-outline-secondary btn-sm" @click="logout">
            <i class="fas fa-sign-out-alt"></i> Đăng xuất
          </button>
        </template>
        <template v-else>
          <button class="btn btn-outline-primary btn-sm text-nowrap d-flex align-items-center gap-1" @click="showRegisterCustomerModal = true">
            <i class="fas fa-user-plus"></i> Đăng ký
          </button>
          <button class="btn btn-primary btn-sm text-nowrap d-flex align-items-center gap-1" @click="showLoginModal = true">
            <i class="fas fa-sign-in-alt"></i> Đăng nhập
          </button>
        </template>

        <router-link to="/gio-hang" class="btn btn-outline-primary btn-sm position-relative flex-shrink-0">
          <i class="fas fa-shopping-cart"></i>
          <span
            v-if="cartCount > 0"
            class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
          >
            {{ cartCount }}
          </span>
        </router-link>
      </div>
    </div>
  </header>

  <LoginModal
    :isVisible="showLoginModal"
    @update:isVisible="showLoginModal = $event"
    @loggedIn="handleLoggedIn"
    @openRegister="showRegisterCustomerModal = true"
  />

  <RegisterCustomerModal
    :isVisible="showRegisterCustomerModal"
    @update:isVisible="showRegisterCustomerModal = $event"
    @customerAdded="handleCustomerAdded"
    @openLogin="showLoginModal = true"
  />
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus'; // Import ElMessage for toast notifications
import RegisterCustomerModal from "@/component/RegisterCustomerModal.vue";
import LoginModal from "@/component/LoginModal.vue";

const customer = ref(null);
const cartCount = ref(0);
const router = useRouter();

// State cho input tìm kiếm
const searchQuery = ref('');

// States để điều khiển hiển thị các modal
const showLoginModal = ref(false);
const showRegisterCustomerModal = ref(false);

const logoSrc = ref('https://img.icons8.com/?size=100&id=juRF5DiUGr4p&format=png&color=000000'); // Hello Kitty logo

const logout = () => {
  customer.value = null; // Clear customer data
  ElMessage.success('Đăng xuất thành công!');
  console.log("Logged out");
  // Thêm logic xóa token, reset state của giỏ hàng nếu cần
};

const handleOrderClick = () => {
  if (customer.value) {
    router.push('/don-hang');
  } else {
    // Nếu chưa đăng nhập, mở modal đăng nhập
    showLoginModal.value = true;
    ElMessage.info('Vui lòng đăng nhập để xem đơn hàng của bạn.');
  }
};

// Hàm xử lý tìm kiếm
const performSearch = () => {
  if (searchQuery.value.trim()) {
    console.log("Tìm kiếm sản phẩm:", searchQuery.value);
    // Điều hướng đến trang kết quả tìm kiếm với query
    router.push({ path: '/search-results', query: { q: searchQuery.value.trim() } });
    ElMessage.success(`Đang tìm kiếm: "${searchQuery.value}"`);
  } else {
    ElMessage.info('Vui lòng nhập từ khóa tìm kiếm.');
  }
};

// Xử lý khi đăng nhập thành công từ LoginModal
const handleLoggedIn = (userData) => {
  customer.value = { customerName: userData.username }; // Cập nhật thông tin khách hàng
  console.log('Đăng nhập thành công, user:', userData.username);
  // Có thể tải dữ liệu giỏ hàng, thông tin cá nhân khác ở đây
};

// Xử lý khi đăng ký khách hàng thành công từ RegisterCustomerModal
const handleCustomerAdded = (userData) => {
  // Sau khi đăng ký thành công, thông báo và có thể tự động mở lại LoginModal
  ElMessage.success('Tài khoản đã được đăng ký thành công! Vui lòng đăng nhập.');
  showLoginModal.value = true; // Mở lại modal đăng nhập
  // Không tự động gán customer ở đây vì họ cần đăng nhập để xác thực
};
</script>

<style scoped>
/* Bootstrap-like utility classes */
.container {
  max-width: 100%;
  margin: 0 auto;
  padding: 0 10px;
  box-sizing: border-box;
}
.d-flex { display: flex; }
.justify-content-between { justify-content: space-between; }
.justify-content-center { justify-content: center; }
.align-items-center { align-items: center; }

.gap-1 { gap: 0.25rem; }
.gap-3 { gap: 0.75rem; }
.flex-grow-1 { flex-grow: 1; }
.flex-shrink-0 { flex-shrink: 0; }
.flex-nowrap { flex-wrap: nowrap; }
.text-nowrap { white-space: nowrap; }

.m-0 { margin: 0; }
.p-0 { padding: 0; }
.mx-4 { margin-left: 1rem; margin-right: 1rem; } /* Margin cho thanh search */
.px-4 { padding-left: 1rem; padding-right: 1rem; }
.fw-bold { font-weight: 700; }
.fw-semibold { font-weight: 600; }
.text-xl { font-size: 1.25rem; }
.text-sm { font-size: 0.875rem; }

.bg-white { background-color: #fff; }
.text-gray-800 { color: #2d3748; }
.text-gray-700 { color: #4a5568; }
.hover\:text-blue-600:hover { color: #3182ce; }
.shadow-md { box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); }
.py-4 { padding-top: 1rem; padding-bottom: 1rem; }

/* Navigation specific styles */
.nav { list-style: none; }
.nav-link {
  display: block;
  padding: 0.5rem 0.75rem;
  text-decoration: none;
  transition: color 0.2s ease-in-out;
}
.nav-link:hover { text-decoration: none; }

.nav-link.d-flex i {
  vertical-align: middle;
  font-size: 1rem;
  line-height: 1;
}
.nav-link.d-flex {
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

/* Button styles */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  line-height: 1.5;
  border-radius: 0.25rem;
  cursor: pointer;
  transition: all 0.15s ease-in-out;
  text-decoration: none;
}
.btn-primary { color: #fff; background-color: #4299e1; border-color: #4299e1; }
.btn-primary:hover { background-color: #3182ce; border-color: #3182ce; }
.btn-outline-primary { color: #4299e1; border: 1px solid #4299e1; background-color: transparent; }
.btn-outline-primary:hover { color: #fff; background-color: #4299e1; }
.btn-outline-secondary { color: #718096; border: 1px solid #718096; background-color: transparent; }
.btn-outline-secondary:hover { color: #fff; background-color: #718096; }
.btn-sm { padding: 0.25rem 0.5rem; font-size: 0.75rem; }

/* Badge styling */
.position-relative { position: relative; }
.position-absolute { position: absolute; }
.top-0 { top: 0; }
.start-100 { left: 100%; }
.translate-middle { transform: translate(-50%, -50%); }
.badge {
  padding: 0.35em 0.65em;
  font-size: 0.75em;
  font-weight: 700;
  line-height: 1;
  text-align: center;
  white-space: nowrap;
  vertical-align: baseline;
  border-radius: 0.25rem;
}
.rounded-pill { border-radius: 50rem; }
.bg-danger { background-color: #e53e3e; color: #fff; }

/* Custom search bar styles */
.search-bar {
  max-width: 400px; /* Giới hạn chiều rộng thanh tìm kiếm */
}

.input-group {
  display: flex;
  width: 100%;
}

.form-control {
  display: block;
  width: 100%;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  font-weight: 400;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  border-radius: 0.25rem;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.form-control:focus {
  color: #495057;
  background-color: #fff;
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.custom-search-input {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-right: none; /* Loại bỏ viền phải để liền với nút */
}

.custom-search-button {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  padding: 0.375rem 0.75rem; /* Giữ padding như các btn khác */
  font-size: 1rem; /* Giữ kích thước icon */
}

/* Responsive adjustments */
@media (max-width: 992px) {
  .container {
    flex-wrap: wrap;
    justify-content: center;
  }
  .search-bar {
    order: 3; /* Đẩy search bar xuống hàng mới trên màn hình nhỏ */
    width: 100%;
    margin: 10px 0; /* Thêm margin trên dưới */
    max-width: none; /* Bỏ giới hạn chiều rộng trên màn hình nhỏ */
  }
  nav {
    order: 2; /* Đẩy nav xuống hàng thứ 2 */
    flex-basis: 100%; /* Chiếm toàn bộ chiều rộng */
    justify-content: center !important;
    padding: 0 !important;
  }
  nav ul {
    flex-wrap: wrap; /* Cho phép các mục nav xuống dòng */
    justify-content: center;
  }
  nav .nav-item {
    margin: 5px; /* Thêm khoảng cách giữa các mục nav khi xuống dòng */
  }
  .d-flex.flex-shrink-0:first-child,
  .d-flex.flex-shrink-0:last-child {
    flex-basis: auto; /* Trở lại kích thước ban đầu */
    width: auto;
  }
  .d-flex.flex-nowrap {
    flex-wrap: wrap;
    justify-content: center;
  }
  .text-nowrap {
    white-space: normal; /* Cho phép text xuống dòng trên màn hình nhỏ */
  }
}

@media (max-width: 768px) {
  .h-8 { height: 2rem; } /* Điều chỉnh kích thước logo nhỏ hơn */
  .w-8 { width: 2rem; }
  .text-xl { font-size: 1.1rem; } /* Điều chỉnh font chữ tiêu đề */
  .btn-sm { font-size: 0.8rem; padding: 0.2rem 0.4rem; }
  .nav-link { padding: 0.4rem 0.6rem; font-size: 0.9rem; }
  .input-group .form-control, .input-group .btn {
    font-size: 0.9rem;
    padding: 0.3rem 0.6rem;
  }
}

</style>
<template>
  <footer class="main-footer py-5">
    <div class="container">
      <div class="row">
        <div class="col-md-3 col-12 mb-4 mb-md-0 text-center text-md-start">
          <h4 class="footer-brand mb-3">SPRINTX</h4>
          <p class="footer-description mb-3">
            Chúng tôi cung cấp những sản phẩm giày thể thao chất lượng cao, mang đến
            sự thoải mái và phong cách cho khách hàng.
          </p>
          <div class="footer-social-icons">
            <a href="#" class="social-icon me-3"><i class="bi bi-facebook"></i></a>
            <a href="#" class="social-icon me-3"><i class="bi bi-instagram"></i></a>
            <a href="#" class="social-icon"><i class="bi bi-youtube"></i></a>
          </div>
        </div>

        <div class="col-md-3 col-12 mb-4 mb-md-0 text-center text-md-start">
          <h5 class="footer-heading mb-3">Liên kết nhanh</h5>
          <ul class="list-unstyled footer-links">
            <li><a href="#">Trang chủ</a></li>
            <li><a href="#">Sản phẩm</a></li>
            <li><a href="#">Về chúng tôi</a></li>
            <li><a href="#">Liên hệ</a></li>
            <li><a href="#">Blog</a></li>
          </ul>
        </div>

        <div class="col-md-3 col-12 mb-4 mb-md-0 text-center text-md-start">
          <h5 class="footer-heading mb-3">Liên hệ</h5>
          <ul class="list-unstyled footer-contact-info">
            <li><i class="bi bi-geo-alt-fill me-2"></i>123 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh</li>
            <li><i class="bi bi-telephone-fill me-2"></i>+84 123 456 789</li>
            <li><i class="bi bi-envelope-fill me-2"></i>info@sprintx.com</li>
          </ul>
        </div>

        <div class="col-md-3 col-12 text-center text-md-start">
          <h5 class="footer-heading mb-3">Đăng ký nhận tin</h5>
          <p class="mb-3">
            Đăng ký để nhận thông tin về sản phẩm mới và khuyến mại đặc biệt.
          </p>
          <div class="input-group mb-3">
            <input type="email" class="form-control footer-email-input" placeholder="Email của bạn" aria-label="Email của bạn">
            <button class="btn btn-danger footer-signup-btn" type="button">Đăng ký</button>
          </div>
        </div>
      </div>

      <hr class="footer-divider my-4">

      <div class="row align-items-center">
        <div class="col-md-6 col-12 text-center text-md-start mb-2 mb-md-0">
          <p class="copyright-text">&copy; 2025 SPRINTX. Tất cả quyền được bảo lưu.</p>
        </div>
        <div class="col-md-6 col-12 text-center text-md-end">
          <ul class="list-inline footer-policy-links mb-0">
            <li class="list-inline-item"><a href="#">Điều khoản sử dụng</a></li>
            <li class="list-inline-item"><a href="#">Chính sách bảo mật</a></li>
          </ul>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
// Không cần script cụ thể cho footer này trừ khi có tương tác động (ví dụ: gửi form đăng ký)
</script>

<style scoped>
.main-footer {
  background-color: #212529; /* Màu nền tối tương tự ảnh */
  color: #dee2e6; /* Màu chữ sáng */
  font-size: 0.95rem;
}

.footer-brand {
  font-size: 1.8rem;
  font-weight: bold;
  color: #fff; /* Màu trắng cho thương hiệu */
  text-transform: uppercase;
  letter-spacing: 1px;
}

.footer-description {
  color: #adb5bd; /* Màu xám nhạt hơn */
  line-height: 1.6;
}

.footer-social-icons .social-icon {
  color: #adb5bd;
  font-size: 1.5rem;
  transition: color 0.3s ease;
}

.footer-social-icons .social-icon:hover {
  color: #fff; /* Trắng khi hover */
}

.footer-heading {
  font-size: 1.1rem;
  font-weight: bold;
  color: #fff;
  margin-bottom: 1rem;
  position: relative;
}

/* Gạch chân nhỏ dưới tiêu đề nếu muốn (tùy chọn) */
/*
.footer-heading::after {
  content: '';
  display: block;
  width: 40px;
  height: 3px;
  background-color: #e60023;
  margin-top: 8px;
  margin-left: 0;
  text-align: left;
}
*/

.footer-links li a,
.footer-contact-info li,
.footer-policy-links li a {
  color: #adb5bd;
  text-decoration: none;
  transition: color 0.3s ease;
  line-height: 2; /* Khoảng cách dòng cho các liên kết */
}

.footer-links li a:hover,
.footer-policy-links li a:hover {
  color: #fff; /* Trắng khi hover */
}

.footer-contact-info li i {
  color: #e60023; /* Màu đỏ cho icon */
  font-size: 1.1rem;
}

.footer-email-input {
  border-radius: 5px 0 0 5px !important;
  border: 1px solid #495057; /* Viền màu xám đậm hơn */
  background-color: #343a40; /* Nền tối hơn cho input */
  color: #fff; /* Màu chữ trong input */
}

.footer-email-input::placeholder {
  color: #adb5bd; /* Màu placeholder */
}

.footer-signup-btn {
  background-color: #e60023; /* Màu đỏ */
  border-color: #e60023;
  color: white;
  border-radius: 0 5px 5px 0 !important;
  font-weight: 600;
}

.footer-signup-btn:hover {
  background-color: #c7001a; /* Màu đỏ đậm hơn khi hover */
  border-color: #c7001a;
}

.footer-divider {
  border-top: 1px solid #495057; /* Đường phân cách màu xám đậm */
}

.copyright-text {
  font-size: 0.85rem;
  color: #adb5bd;
  margin-bottom: 0;
}

.footer-policy-links .list-inline-item {
  margin-right: 15px;
}

.footer-policy-links .list-inline-item:last-child {
  margin-right: 0;
}

.footer-policy-links li a {
  font-size: 0.85rem;
}

/* Responsive adjustments */
@media (max-width: 767.98px) {
  .main-footer {
    text-align: center !important;
  }
  .footer-brand,
  .footer-heading {
    text-align: center !important;
  }
  .footer-links,
  .footer-contact-info,
  .footer-policy-links {
    padding-left: 0; /* Xóa padding mặc định của list-unstyled */
  }
  .footer-links li,
  .footer-contact-info li,
  .footer-policy-links li {
    margin-bottom: 5px; /* Khoảng cách giữa các mục trên mobile */
  }
  .footer-social-icons {
    margin-top: 15px;
    margin-bottom: 20px;
  }
  .footer-email-input,
  .footer-signup-btn {
    width: 100%; /* Đảm bảo input và button chiếm toàn bộ chiều rộng */
    border-radius: 5px !important; /* Tròn cả 4 góc trên mobile */
  }
  .input-group {
    flex-wrap: wrap; /* Cho phép các phần tử xuống dòng */
  }
  .input-group > .form-control,
  .input-group > .btn {
    flex: 1 1 auto;
  }
  .input-group .footer-email-input {
    margin-bottom: 10px; /* Khoảng cách giữa input và button */
  }
  .input-group .footer-signup-btn {
    margin-left: 0; /* Đảm bảo button không bị đẩy sang phải */
  }
  .footer-policy-links .list-inline-item {
    margin-right: 0;
    display: block; /* Mỗi liên kết một dòng */
  }
  .footer-policy-links .list-inline-item:first-child {
    margin-bottom: 5px;
  }
}
</style>
<template>
  <footer class="mlb-footer py-5">
    <div class="container">
      <div class="footer-sections row">
        <div class="col-md-4 col-12 mb-4 mb-md-0 text-start">
          <h5 class="footer-heading mb-3">TRỢ GIÚP</h5>
          <ul class="list-unstyled footer-links">
            <li><a href="#">Hệ thống cửa hàng</a></li>
            <li><a href="#">Liên hệ</a></li>
            <li><a href="#">Chính sách Loyalty</a></li>
            <li><a href="#">Chính sách bảo mật</a></li>
            <li><a href="#">Điều khoản sử dụng</a></li>
            <li><a href="#">Chính sách vận chuyển, giao hàng</a></li>
            <li><a href="#">Chính sách thanh toán</a></li>
            <li><a href="#">Chính sách đổi trả</a></li>
            <li><a href="#">Câu hỏi thường gặp</a></li>
          </ul>
        </div>

        <div class="col-md-4 col-12 mb-4 mb-md-0 text-start">
          <h5 class="footer-heading mb-3">THANH TOÁN</h5>
          <ul class="list-unstyled footer-links">
            <li><a href="#">Visa / Mastercard / JCB</a></li>
            <li><a href="#">ATM / Internet Banking</a></li>
            <li><a href="#">Quét mã QR</a></li>
            <li><a href="#">Mua trước trả sau</a></li>
            <li><a href="#">Ví điện tử</a></li>
            <li><a href="#">Thanh toán khi nhận hàng (COD)</a></li>
          </ul>
        </div>

        <div class="col-md-4 col-12 mb-4 mb-md-0 text-start">
          <h5 class="footer-heading mb-3">GIAO HÀNG</h5>
          <ul class="list-unstyled footer-links">
            <li><a href="#">Giao hàng tiêu chuẩn</a></li>
            <li><a href="#">Maison NOW</a></li>
            <li>
            </li>
          </ul>
        </div>
      </div>

      <hr class="footer-divider my-5">

      <div class="footer-bottom text-center">
        <p class="mb-1">Bản quyền thuộc về MLB Korea được phân phối độc quyền tại Việt Nam bởi CÔNG TY CỔ PHẦN MAISON RETAIL MANAGEMENT INTERNATIONAL</p>
        <p class="mb-1">Địa chỉ: 189 - 197, Đường Ba Trại, Phường Rạch Ông, Quận 8, TP. Hồ Chí Minh | MST: 031375103</p>
        <p class="mb-0">Hotline: 1900 299268 | Email: <a href="mailto:customercare@maisonrmi.com" class="footer-email-address">customercare@maisonrmi.com</a></p>
        <p class="text-xs mt-3">
          <a href="https://online.gov.vn/Home/WebDetails/105121" target="_blank" class="bocongthuong-gov-link">online.gov.vn/Home/WebDetails/105121</a>
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup>
// No specific script needed for this static footer
</script>

<style scoped>
.mlb-footer {
  background-color: #262626; /* Dark background from the image */
  color: #a0a0a0; /* Light gray text for general content */
  font-family: Arial, sans-serif; /* A sans-serif font similar to the image */
  font-size: 0.875rem; /* Smaller base font size */
  line-height: 1.6;
}

.container {
  max-width: 1200px; /* Adjust container width to match image's content width */
  margin: 0 auto;
  padding: 0 20px;
}

.footer-sections {
  margin-bottom: 3rem; /* Space before the divider */
}

.footer-heading {
  color: #ffffff; /* White color for headings */
  font-size: 0.9rem; /* Smaller, uppercase heading */
  font-weight: bold;
  text-transform: uppercase;
  margin-bottom: 1rem; /* Space below heading */
  letter-spacing: 0.05em; /* A bit more letter spacing for headings */
}

.footer-links {
  padding-left: 0; /* Remove default ul padding */
  list-style: none; /* Remove list bullets */
}

.footer-links li {
  margin-bottom: 0.5rem; /* Space between list items */
}

.footer-links li a {
  color: #a0a0a0; /* Link color */
  text-decoration: none;
  transition: color 0.2s ease-in-out;
  line-height: 1.4; /* Tighter line height for links */
}

.footer-links li a:hover {
  color: #ffffff; /* White on hover */
}

/* Specific styling for the 'Bo Cong Thuong' logo */
.bocongthuong-link {
  display: block; /* Ensures the image doesn't affect line height of surrounding text */
  width: 140px; /* Specific width as seen in the image */
  height: auto;
}

.bocongthuong-logo {
  max-width: 100%;
  height: auto;
  display: block; /* Remove extra space below image */
}

.footer-divider {
  border-color: #444444; /* Darker gray for the divider */
  margin-top: 2rem;
  margin-bottom: 2rem;
}

.footer-bottom {
  font-size: 0.8rem; /* Smaller font size for bottom text */
  color: #888888; /* Even lighter gray for copyright/address */
}

.footer-bottom p {
  margin-bottom: 0.4rem; /* Small margin between paragraphs */
}

.footer-email-address {
  color: #888888; /* Same color as surrounding text */
  text-decoration: none;
}

.footer-email-address:hover {
  color: #ffffff; /* White on hover */
  text-decoration: underline; /* Underline on hover */
}

.bocongthuong-gov-link {
  color: #888888; /* Same color */
  text-decoration: none;
  font-size: 0.75rem; /* Even smaller for this specific link */
}

.bocongthuong-gov-link:hover {
  color: #ffffff;
  text-decoration: underline;
}

/* Responsive adjustments */
@media (max-width: 767.98px) {
  .footer-sections .col-12 {
    text-align: center; /* Center align columns on small screens */
  }
  .footer-heading {
    text-align: center; /* Center heading on small screens */
    margin-bottom: 0.8rem;
  }
  .footer-links {
    padding-left: 0; /* Ensure no padding */
  }
  .footer-links li {
    margin-bottom: 0.3rem; /* Less space between links on mobile */
  }
  .bocongthuong-link {
    margin: 1.5rem auto 0 auto; /* Center logo on small screens and add top margin */
  }
  .footer-bottom {
    font-size: 0.75rem;
  }
  .footer-bottom p {
    margin-bottom: 0.3rem;
  }
  .mlb-footer .py-5 {
    padding-top: 2rem !important;
    padding-bottom: 2rem !important;
  }
  .footer-sections {
    margin-bottom: 2rem;
  }
  .footer-divider {
    margin-top: 1.5rem;
    margin-bottom: 1.5rem;
  }
}

@media (max-width: 575.98px) {
  .footer-links li a {
    font-size: 0.85rem;
  }
  .footer-heading {
    font-size: 0.85rem;
  }
  .footer-bottom {
    font-size: 0.7rem;
  }
}
</style>
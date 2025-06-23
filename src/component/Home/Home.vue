<template>
  <div class="hero-section">
    <div class="container hero-content">
      <div class="hero-header-badge mb-3">
        Bộ sưu tập mới 2025
      </div>
      <h2 class="hero-title">
        Nâng tầm phong cách <br>
        <span class="text-danger">Vượt mọi giới hạn</span>
      </h2>
      <p class="hero-description mb-4">
        Khám phá bộ sưu tập giày thể thao mới nhất với <br>
        công nghệ tiên tiến, thiết kế hiện đại và sự thoải mái <br>
        vượt trội.
      </p>
      <div class="hero-buttons mb-5">
        <button @click="buyNow" class="btn btn-danger hero-btn-buy me-3">
          Mua ngay <i class="bi bi-arrow-right"></i>
        </button>
        <button @click="exploreCollection" class="btn btn-outline-dark hero-btn-explore">
          Khám phá bộ sưu tập
        </button>
      </div>
      <div class="hero-stats d-flex justify-content-start">
        <div class="stat-item me-5">
          <h3 class="stat-number">500+</h3>
          <p class="stat-label">Mẫu giày</p>
        </div>
        <div class="stat-item me-5">
          <h3 class="stat-number">100+</h3>
          <p class="stat-label">Thương hiệu</p>
        </div>
        <div class="stat-item">
          <h3 class="stat-number">50K+</h3>
          <p class="stat-label">Khách hàng</p>
        </div>
      </div>
    </div>
  </div>

  <div class="container my-5">
    <h1 class="text-center mb-2 featured-products-title">Sản phẩm nổi bật</h1>
    <p class="text-center text-muted mb-5">Khám phá những mẫu giày thể thao được yêu thích nhất hiện nay</p>

    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Đang tải sản phẩm...</p>
    </div>

    <div v-else-if="error" class="alert alert-danger text-center" role="alert">
      Không thể tải sản phẩm. Vui lòng thử lại sau.
    </div>

    <div v-else class="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4">
      <div class="col" v-for="product in featuredProducts" :key="product.id">
        <div class="card product-card h-100 shadow-sm">
          <span v-if="product.discountPercentage" class="badge bg-danger discount-badge">
            Giảm {{ product.discountPercentage }}%
          </span>
          <span v-if="product.isNew" class="badge bg-info new-badge">MỚI</span>

          <img :src="product.imageUrl || '/placeholder-shoe.jpg'" class="card-img-top product-image" :alt="product.productName">
          <div class="card-body d-flex flex-column">
            <h5 class="card-title product-name">{{ product.productName }}</h5>
            <p class="card-text product-price">
              <span v-if="product.oldPrice" class="text-decoration-line-through text-muted me-2 small">
                {{ formatCurrency(product.oldPrice) }}
              </span>
              <span class="fw-bold text-danger">{{ formatCurrency(product.sellPrice) }}</span>
            </p>
            <div class="mt-auto">
              <button @click="addToCart(product.id)" class="btn btn-primary w-100 add-to-cart-btn">
                <i class="bi bi-cart-plus-fill me-1"></i> Thêm vào giỏ
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="text-center mt-5">
      <button @click="viewAllProducts" class="btn btn-outline-secondary btn-lg view-all-btn">
        Xem tất cả sản phẩm
      </button>
    </div>
  </div>

  <div class="why-choose-us-section py-5 bg-light">
    <div class="container">
      <h2 class="text-center mb-2 why-choose-us-title">Tại sao chọn chúng tôi?</h2>
      <p class="text-center text-muted mb-5 why-choose-us-subtitle">Chúng tôi cam kết mang đến trải nghiệm mua sắm tốt nhất cho bạn</p>

      <div class="row g-4 justify-content-center">
        <div class="col-12 col-md-6 col-lg-3">
          <div class="why-choose-us-item text-center p-4">
            <div class="icon-wrapper mx-auto mb-3">
              <i class="bi bi-truck icon-shipping"></i>
            </div>
            <h5 class="item-title">Giao hàng miễn phí</h5>
            <p class="item-description">Miễn phí giao hàng cho tất cả đơn hàng trên 1 triệu đồng trên toàn quốc.</p>
          </div>
        </div>
        <div class="col-12 col-md-6 col-lg-3">
          <div class="why-choose-us-item text-center p-4">
            <div class="icon-wrapper mx-auto mb-3">
              <i class="bi bi-shield-check icon-warranty"></i>
            </div>
            <h5 class="item-title">Bảo hành chính hãng</h5>
            <p class="item-description">Tất cả sản phẩm đều được bảo hành chính hãng từ 12 đến 24 tháng.</p>
          </div>
        </div>
        <div class="col-12 col-md-6 col-lg-3">
          <div class="why-choose-us-item text-center p-4">
            <div class="icon-wrapper mx-auto mb-3">
              <i class="bi bi-arrow-counterclockwise icon-return"></i>
            </div>
            <h5 class="item-title">Đổi trả dễ dàng</h5>
            <p class="item-description">Đổi trả sản phẩm trong vòng 30 ngày nếu không vừa ý.</p>
          </div>
        </div>
        <div class="col-12 col-md-6 col-lg-3">
          <div class="why-choose-us-item text-center p-4">
            <div class="icon-wrapper mx-auto mb-3">
              <i class="bi bi-clock icon-support"></i>
            </div>
            <h5 class="item-title">Hỗ trợ 24/7</h5>
            <p class="item-description">Đội ngũ tư vấn viên luôn sẵn sàng hỗ trợ bạn mọi lúc mọi nơi.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus'; // Đảm bảo bạn đã cài đặt Element Plus

const router = useRouter();
const featuredProducts = ref([]);
const loading = ref(true);
const error = ref(false);

const fetchFeaturedProducts = async () => {
  loading.value = true;
  error.value = false;
  try {
    const response = await axios.get('http://localhost:8080/api/admin/products/online-home');
    featuredProducts.value = response.data.map(p => ({
      id: p.id,
      productName: p.productName,
      sellPrice: p.sellPrice,
      oldPrice: p.originPrice > p.sellPrice ? p.originPrice : null,
      discountPercentage: p.originPrice > p.sellPrice ? Math.round(((p.originPrice - p.sellPrice) / p.originPrice) * 100) : null,
      imageUrl: p.productImages && p.productImages.length > 0 ? `data:image/png;base64,${p.productImages[0].image}` : '/placeholder-shoe.jpg',
      isNew: p.createdDate ? new Date(p.createdDate).getFullYear() === 2025 : false
    }));
  } catch (err) {
    console.error('Lỗi khi tải sản phẩm nổi bật:', err);
    error.value = true;
    ElMessage.error('Không thể tải sản phẩm nổi bật. Vui lòng thử lại.');
  } finally {
    loading.value = false;
  }
};

const formatCurrency = (value) => {
  if (!value) return '0₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const addToCart = (productId) => {
  ElMessage.success(`Đã thêm sản phẩm ${productId} vào giỏ hàng! (Đây là tính năng giả lập)`);
  console.log('Thêm vào giỏ:', productId);
};

const viewAllProducts = () => {
  router.push('/products-list');
  console.log('Xem tất cả sản phẩm');
};

const buyNow = () => {
  router.push('/products-list');
  ElMessage.info('Chuyển hướng đến trang mua sắm!');
  console.log('Mua ngay!');
};

const exploreCollection = () => {
  router.push('/collections/new-arrivals');
  ElMessage.info('Chuyển hướng đến trang khám phá bộ sưu tập!');
  console.log('Khám phá bộ sưu tập!');
};

onMounted(() => {
  fetchFeaturedProducts();
});
</script>

<style scoped>
.hero-section {
  background-color: #f8f8f8;
  padding: 100px 0;
  min-height: 500px;
  display: flex;
  align-items: center;
  text-align: left;
}

.hero-content {
  max-width: 600px;
}

.hero-header-badge {
  background-color: #ffe0e6;
  color: #e60023;
  font-weight: bold;
  padding: 8px 15px;
  border-radius: 50px;
  display: inline-block;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 900;
  line-height: 1.1;
  color: #212529;
  margin-bottom: 20px;
}

.hero-title .text-danger {
  color: #e60023 !important;
}

.hero-description {
  font-size: 1.1rem;
  color: #6c757d;
  line-height: 1.6;
  margin-bottom: 30px;
}

.hero-btn-buy {
  background-color: #e60023;
  border-color: #e60023;
  color: white;
  padding: 12px 25px;
  font-size: 1.05rem;
  font-weight: 600;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.hero-btn-buy:hover {
  background-color: #c7001a;
  border-color: #c7001a;
}

.hero-btn-explore {
  border: 2px solid #212529;
  color: #212529;
  padding: 12px 25px;
  font-size: 1.05rem;
  font-weight: 600;
  border-radius: 8px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.hero-btn-explore:hover {
  background-color: #212529;
  color: white;
}

.hero-stats {
  margin-top: 50px;
}

.stat-item {
  text-align: left;
}

.stat-number {
  font-size: 2.2rem;
  font-weight: 800;
  color: #212529;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 1rem;
  color: #6c757d;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 991.98px) {
  .hero-section {
    padding: 80px 0;
  }
  .hero-title {
    font-size: 2.8rem;
  }
  .hero-description {
    font-size: 1rem;
  }
  .hero-buttons .btn {
    padding: 10px 20px;
    font-size: 1rem;
  }
  .hero-stats {
    justify-content: center !important;
    flex-wrap: wrap;
  }
  .stat-item {
    margin-bottom: 20px;
    margin-right: 30px !important;
  }
}

@media (max-width: 767.98px) {
  .hero-section {
    padding: 60px 0;
  }
  .hero-title {
    font-size: 2.2rem;
    text-align: center;
  }
  .hero-description {
    font-size: 0.9rem;
    text-align: center;
  }
  .hero-buttons {
    flex-direction: column;
    align-items: center;
    margin-bottom: 30px !important;
  }
  .hero-buttons .btn {
    width: 80%;
    max-width: 250px;
    margin-bottom: 10px !important;
    margin-right: 0 !important;
  }
  .hero-stats {
    flex-direction: column;
    align-items: center;
  }
  .stat-item {
    margin-right: 0 !important;
    margin-bottom: 15px;
  }
  .stat-number {
    font-size: 1.8rem;
  }
  .stat-label {
    font-size: 0.9rem;
  }
}

.container {
  max-width: 1200px;
}

.featured-products-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
}

.product-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  position: relative;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.product-image {
  height: 200px;
  object-fit: contain;
  padding: 15px;
  background-color: #f8f8f8;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  min-height: 50px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  font-size: 1.25rem;
  margin-top: auto;
}

.add-to-cart-btn {
  font-size: 0.95rem;
  padding: 8px 15px;
  border-radius: 5px;
}

.discount-badge, .new-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
  z-index: 10;
}

.new-badge {
  background-color: #17a2b8 !important;
  right: auto;
  left: 10px;
}

.view-all-btn {
  border-radius: 50px;
  padding: 12px 30px;
  font-size: 1.1rem;
  font-weight: 600;
}

@media (max-width: 767.98px) {
  .product-image {
    height: 180px;
  }
  .product-name {
    font-size: 1rem;
    min-height: 48px;
  }
  .product-price {
    font-size: 1.1rem;
  }
  .add-to-cart-btn {
    font-size: 0.85rem;
    padding: 6px 12px;
  }
  .view-all-btn {
    padding: 10px 25px;
    font-size: 1rem;
  }
}

/* CSS cho phần "Tại sao chọn chúng tôi?" */
.why-choose-us-section {
  background-color: #f8f8f8; /* Gần với màu nền trong ảnh */
  padding: 80px 0; /* Khoảng cách trên dưới */
}

.why-choose-us-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #333;
}

.why-choose-us-subtitle {
  font-size: 1.05rem;
  color: #6c757d;
  margin-bottom: 40px;
}

.why-choose-us-item {
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  height: 100%; /* Đảm bảo các item có chiều cao bằng nhau */
  display: flex;
  flex-direction: column;
  justify-content: center; /* Căn giữa nội dung theo chiều dọc */
}

.why-choose-us-item .icon-wrapper {
  width: 70px;
  height: 70px;
  background-color: #ffe0e6; /* Màu hồng nhạt */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.why-choose-us-item .icon-wrapper i {
  font-size: 2.5rem;
  color: #e60023; /* Màu đỏ */
}

.why-choose-us-item .item-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}

.why-choose-us-item .item-description {
  font-size: 0.95rem;
  color: #6c757d;
  line-height: 1.6;
  flex-grow: 1; /* Cho phép mô tả chiếm không gian còn lại */
}

/* Responsive adjustments for why-choose-us section */
@media (max-width: 991.98px) {
  .why-choose-us-section {
    padding: 60px 0;
  }
  .why-choose-us-title {
    font-size: 2rem;
  }
  .why-choose-us-subtitle {
    font-size: 1rem;
  }
  .why-choose-us-item .icon-wrapper {
    width: 60px;
    height: 60px;
  }
  .why-choose-us-item .icon-wrapper i {
    font-size: 2rem;
  }
  .why-choose-us-item .item-title {
    font-size: 1.15rem;
  }
  .why-choose-us-item .item-description {
    font-size: 0.9rem;
  }
}

@media (max-width: 767.98px) {
  .why-choose-us-section {
    padding: 40px 0;
  }
  .why-choose-us-title {
    font-size: 1.8rem;
  }
  .why-choose-us-subtitle {
    font-size: 0.9rem;
  }
  .why-choose-us-item {
    padding: 30px 20px;
  }
  .why-choose-us-item .icon-wrapper {
    width: 55px;
    height: 55px;
  }
  .why-choose-us-item .icon-wrapper i {
    font-size: 1.8rem;
  }
  .why-choose-us-item .item-title {
    font-size: 1.1rem;
  }
  .why-choose-us-item .item-description {
    font-size: 0.85rem;
  }
}
</style>
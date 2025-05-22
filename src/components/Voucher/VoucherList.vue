<template>
    <div class="container-fluid mt-4">
      <h3 class="mb-3">Danh sách mã giảm giá</h3>
  
      <div class="table-responsive">
        <table class="table table-striped table-bordered">
          <thead>
            <tr>
              <th>STT</th>
              <th>Mã giảm giá</th>
              <th>Số tiền giảm</th>
              <th>Giá trị đơn hàng tối thiểu</th>
              <th>Ngày hết hạn</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(voucher, index) in vouchers" :key="voucher.id">
              <td>{{ index + 1 }}</td>
              <td>{{ voucher.code || '---' }}</td>
              <td>{{ formatDiscount(voucher) }}</td>
              <td>{{ formatCurrency(voucher.minOrderValue) }}</td>
              <td>{{ formatDate(voucher.expriryDate) }}</td>
              <td>
                <span :class="voucher.voucherStatus === 1 ? 'badge bg-success' : 'badge bg-danger'">
                  {{ voucher.voucherStatus === 1 ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import axios from 'axios';
  
  const vouchers = ref([]);
  
  const fetchVouchers = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/admin/voucher');
      vouchers.value = response.data;
    } catch (error) {
      console.error('Lỗi khi tải danh sách mã giảm giá:', error);
    }
  };
  
  onMounted(() => {
    fetchVouchers();
  });
  
  // Hàm định dạng ngày tháng
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' });
  };
  
  // Hàm định dạng tiền tệ
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
  };
  
  // Hàm định dạng số tiền giảm
  const formatDiscount = (voucher) => {
    if (voucher.discountAmount === 0) {
      return 'Miễn phí vận chuyển';
    }
    return `${voucher.discountAmount}%`;
  };
  </script>
  
  <style scoped>
  /* Bạn có thể thêm các kiểu tùy chỉnh ở đây nếu cần */
  </style>
  
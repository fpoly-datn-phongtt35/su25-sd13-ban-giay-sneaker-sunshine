<template>
    <div class="container-fluid mt-4">
      <h3 class="mb-3">Danh sách thương hiệu</h3>
    
      <div class="table-responsive">
        <table class="table table-striped table-bordered">
          <thead>
            <tr>
              <th>STT</th>
              <th>Mã thương hiệu</th>
              <th>Tên thương hiệu</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(brand, index) in brands" :key="brand.id">
              <td>{{ index + 1 }}</td>
              <td>{{ brand.brandCode || '---' }}</td>
              <td>{{ brand.brandName || '---' }}</td>
              <td>
                <span :class="brand.brandStatus === 1 ? 'badge bg-success' : 'badge bg-danger'">
                  {{ brand.brandStatus === 1 ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
              </td>
              <td>{{ formatDate(brand.dateCreated) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import axios from 'axios';
  
  const brands = ref([]);
  
  const fetchBrands = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/admin/brand');
      brands.value = response.data;
    } catch (error) {
      console.error('Lỗi khi tải danh sách thương hiệu:', error);
    }
  };
  
  onMounted(() => {
    fetchBrands();
  });
  
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' });
  };
  </script>
  
  <style scoped>
  /* Bạn có thể thêm các kiểu tùy chỉnh ở đây nếu cần */
  </style>
  
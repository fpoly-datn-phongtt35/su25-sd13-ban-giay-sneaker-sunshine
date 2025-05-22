<template>
  <div class="container-fluid mt-4">
    <h3 class="mb-3">Danh sách nhà cung cấp</h3>
  
    <div class="table-responsive">
      <table class="table table-striped table-bordered">
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã nhà cung cấp</th>
            <th>Tên nhà cung cấp</th>
            <th>Quốc gia</th>
            <th>Tỉnh/Thành phố</th>
            <th>Quận/Huyện</th>
            <th>Phường/Xã</th>
            <th>Địa chỉ</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(supplier, index) in suppliers" :key="supplier.id">
            <td>{{ index + 1 }}</td>
            <td>{{ supplier.supplierCode || '---' }}</td>
            <td>{{ supplier.supplierName || '---' }}</td>
            <td>{{ supplier.country || '---' }}</td>
            <td>{{ supplier.province || '---' }}</td>
            <td>{{ supplier.district || '---' }}</td>
            <td>{{ supplier.ward || '---' }}</td>
            <td>{{ supplier.houseName || '---' }}</td>
            <td>
              <span :class="supplier.supplierStatus === 1 ? 'badge bg-success' : 'badge bg-danger'">
                {{ supplier.supplierStatus === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <router-link :to="'/suppliers/update/' + supplier.id" class="btn btn-warning btn-sm">Cập nhật</router-link>
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

const suppliers = ref([]);

const fetchSuppliers = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/supplier');
    suppliers.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải danh sách nhà cung cấp:', error);
  }
};

onMounted(() => {
  fetchSuppliers();
});
</script>

<style scoped>
/* Bạn có thể thêm các kiểu tùy chỉnh ở đây nếu cần */
</style>

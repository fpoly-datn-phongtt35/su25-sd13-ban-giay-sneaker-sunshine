<template>
    <div class="container-fluid mt-4">
      <h3 class="mb-3">Danh sách khách hàng</h3>
  
      <div class="table-responsive">
        <table class="table table-striped table-bordered">
          <thead>
            <tr>
              <th>STT</th>
              <th>Mã khách hàng</th>
              <th>Tên khách hàng</th>
              <th>Email</th>
              <th>Số điện thoại</th>
              <th>Địa chỉ</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th>Ngày cập nhật</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(customer, index) in customers" :key="customer.id">
              <td>{{ index + 1 }}</td>
              <td>{{ customer.customerCode || '---' }}</td>
              <td>{{ customer.customerName || '---' }}</td>
              <td>{{ customer.email || '---' }}</td>
              <td>{{ customer.phone || '---' }}</td>
              <td>
                {{ customer.houseName || '---' }} 
                {{ customer.ward || '' }}
                {{ customer.district || '' }}
                {{ customer.province || '' }}
                {{ customer.country || '' }}
              </td>
              <td>
                <span :class="customer.customerStatus === 1 ? 'badge bg-success' : 'badge bg-danger'">
                  {{ customer.customerStatus === 1 ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
              </td>
              <td>{{ formatDate(customer.dateCreated) }}</td>
              <td>{{ formatDate(customer.dateUpdated) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import axios from 'axios'
  
  const customers = ref([])
  
  const fetchCustomers = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/admin/customer')
      customers.value = response.data
    } catch (error) {
      console.error('Lỗi khi tải danh sách khách hàng:', error)
    }
  }
  
  onMounted(() => {
    fetchCustomers()
  })
  
  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' })
  }
  </script>
  
  <style scoped>
  /* Thêm các kiểu tùy chỉnh ở đây nếu cần */
  </style>
  
<template>
    <div class="container-fluid mt-4">
      <h3 class="mb-3">Danh sách màu</h3>
  
      <div class="table-responsive">
        <table class="table table-striped table-bordered">
          <thead>
            <tr>
              <th>STT</th>
              <th>Tên màu</th>
              <th>Mã màu</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(color, index) in colors" :key="color.id">
              <td>{{ index + 1 }}</td>
              <td>{{ color.colorName || '---' }}</td>
              <td>{{ color.colorCode || '---' }}</td>
              <td>
                <span :class="color.colorStatus === 1 ? 'badge bg-success' : 'badge bg-danger'">
                  {{ color.colorStatus === 1 ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
              </td>
              <td>{{ formatDate(color.dateCreated) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import axios from 'axios'
  
  const colors = ref([])
  
  const fetchColors = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/admin/color')
      colors.value = response.data
    } catch (error) {
      console.error('Lỗi khi tải danh sách màu:', error)
    }
  }
  
  onMounted(() => {
    fetchColors()
  })
  
  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' })
  }
  </script>
  
  <style scoped>
  /* Thêm các kiểu tùy chỉnh ở đây nếu cần */
  </style>
  
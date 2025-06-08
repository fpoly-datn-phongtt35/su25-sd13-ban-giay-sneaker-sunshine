<template>
  <div class="container mt-4">
    <h2 class="mb-3">Danh sách sản phẩm đã xóa</h2>

    <table class="table table-striped">
      <thead class="table-dark">
        <tr>
          <th>STT</th>
          <th>Mã SP</th>
          <th>Tên SP</th>
          <th>Thương hiệu</th>
          <th>Chất liệu</th>
          <th>Giá bán</th>
          <th>Số lượng</th>
          <th>Ngày tạo</th>
          <th>Danh mục</th>
          <th>Trạng thái</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(product, index) in products" :key="product.id">
          <td>{{ page * size + index + 1 }}</td>
          <td>{{ product.productCode }}</td>
          <td>{{ product.productName }}</td>
          <td>{{ product.brandName }}</td>
          <td>{{ product.materialName }}</td>
          <td>{{ formatPrice(product.sellPrice) }}</td>
          <td>{{ product.quantity }}</td>
          <td>{{ formatDate(product.createdDate) }}</td>
          <td>
            <ul class="mb-0 ps-3">
              <li v-for="cat in product.categoryNames" :key="cat">{{ cat }}</li>
            </ul>
          </td>
          <td>
            <span class="badge bg-danger" v-if="product.status === 0">Đã xóa</span>
            <span class="badge bg-success" v-else>Hoạt động</span>
          </td>
          <td>
            <button class="btn btn-sm btn-primary" @click="restoreProduct(product.id)">Khôi phục</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="d-flex justify-content-between align-items-center mt-4">
      <button class="btn btn-secondary" :disabled="page === 0" @click="prevPage">Trang trước</button>
      <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
      <button class="btn btn-secondary" :disabled="page + 1 >= totalPages" @click="nextPage">Trang sau</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const products = ref([])
const page = ref(0)
const size = 8
const totalPages = ref(0)

const fetchData = async () => {
  try {
    const requestBody = {
      page: page.value,
      size: size
    }

    const response = await axios.post('http://localhost:8080/api/admin/products/inactive', requestBody, {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    products.value = response.data.data
    totalPages.value = response.data.totalPages
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu:', error)
  }
}

const restoreProduct = async (productId) => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn khôi phục sản phẩm này không?',
      'Xác nhận khôi phục',
      {
        confirmButtonText: 'Khôi phục',
        cancelButtonText: 'Hủy',
        type: 'warning',
      }
    )

    await axios.put(`http://localhost:8080/api/admin/products/restore/${productId}`)
    ElMessage({
      type: 'success',
      message: 'Khôi phục sản phẩm thành công!',
    })
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Lỗi khi khôi phục sản phẩm:', error)
      ElMessage({
        type: 'error',
        message: 'Khôi phục sản phẩm thất bại!',
      })
    }
  }
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('vi-VN')
}

const nextPage = () => {
  if (page.value + 1 < totalPages.value) {
    page.value++
    fetchData()
  }
}

const prevPage = () => {
  if (page.value > 0) {
    page.value--
    fetchData()
  }
}

onMounted(fetchData)
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle;
}
</style>

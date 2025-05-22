<template>
  <div class="container-fluid mt-4">
    <!-- Nút Thêm sản phẩm -->
    <div class="mb-3">
      <button @click="goToAdd" class="btn btn-primary">
        Thêm sản phẩm
      </button>
    </div>

    <!-- Bảng danh sách sản phẩm -->
    <div class="table-responsive">
      <table class="table table-striped table-bordered">
        <thead>
          <tr>
            <th>STT</th>
            <th>Danh mục con</th>
            <th>Mã sản phẩm</th>
            <th>Tên sản phẩm</th>
            <th>Giá gốc</th>
            <th>Giá bán</th>
            <th>Số lượng</th>
            <th>Ghi chú</th>
            <th>Ngày tạo</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
            <th>Chuyển trạng thái</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(pr, index) in productList" :key="pr.id">
            <td>{{ page * size + index + 1 }}</td>
            <td>{{ pr.subCateName }}</td>
            <td>{{ pr.productCode }}</td>
            <td>{{ pr.productName }}</td>
            <td>{{ pr.originPrice }}</td>
            <td>{{ pr.sellPrice }}</td>
            <td>{{ pr.quantity }}</td>
            <td>{{ pr.note }}</td>
            <td>{{ formatDate(pr.dateCreated) }}</td>
            <td>
              <span class="badge" :class="pr.productStatus === 1 ? 'bg-success' : 'bg-danger'">
                {{ pr.productStatus === 1 ? 'Đang hoạt động' : 'Ngừng Hoạt Động' }}
              </span>
            </td>
            <td>
              <button class="btn btn-info btn-sm me-2" @click="goToUpdate(pr.id)">
                <i class="fas fa-edit"></i> 
              </button>
              <button class="btn btn-secondary btn-sm me-2" @click="goToDetail(pr.id)">
                <i class="fas fa-info-circle"></i> 
              </button>
            </td>
            <td>
              <button class="btn btn-warning btn-sm" @click="openDeleteModal(pr)">
                <i class="fas fa-exchange-alt"></i> {{ pr.productStatus === 1 ? 'Ngừng hoạt động' : 'Kích hoạt' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Điều Hướng Trang -->
    <div class="d-flex justify-content-between align-items-center mt-3">
      <p class="mb-0">Trang Hiện Tại: {{ page + 1 }} / {{ totalPages }}</p>
      <div>
        <button class="btn btn-secondary me-2" :disabled="page === 0" @click="previousPage">
          Trước
        </button>
        <button class="btn btn-secondary" :disabled="page >= totalPages - 1" @click="nextPage">
          Tiếp
        </button>
      </div>
    </div>

    <!-- Modal xác nhận chuyển trạng thái -->
    <div
      v-if="showModal"
      class="modal fade show"
      tabindex="-1"
      aria-labelledby="deleteModalLabel"
      aria-hidden="true"
      style="display: block"
    >
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="deleteModalLabel">Xác nhận thay đổi trạng thái</h5>
            <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <p>
              Bạn có chắc chắn muốn
              {{ selectedProduct?.productStatus === 1 ? 'ngừng hoạt động' : 'kích hoạt' }} sản phẩm
              này không?
            </p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
            <button
              type="button"
              class="btn btn-warning"
              @click="stateChange(selectedProduct?.id)"
            >
              {{ selectedProduct?.productStatus === 1 ? 'Ngừng hoạt động' : 'Kích hoạt' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const productList = ref([])
const page = ref(0);
const size = ref(5);
const totalPages = ref(0)

const showModal = ref(false)

const selectedProduct = ref(null)

const openDeleteModal = (id) => {
  selectedProduct.value = id
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const goToUpdate = (id) => {
  router.push({ name: 'UpdateProduct', params: { id } })
}

const goToAdd = () => {
  router.push(`/product/add`)
}

const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } })
}

const fetchProduct = async () => {
  console.log("Fetching data for page:", page.value); // Kiểm tra giá trị page
  try {
    const response = await axios.get(`http://localhost:8080/api/admin/product`, {
      params: {
        page: page.value,
        size: size.value,
      },
    })

    console.log("API response:", response.data); // Kiểm tra API trả về đúng dữ liệu chưa
    productList.value = response.data.content
    totalPages.value = response.data.totalPages
  } catch (error) {
    console.error('Có lỗi xảy ra khi tải danh sách sản phẩm: ', error)
  }
}

const stateChange = async () => {
  if (!selectedProduct.value) return

  try {
    const response = await axios.put(`http://localhost:8080/api/admin/product/state-change/${selectedProduct.value.id}`)
    const statusChangeProduct = response.data

    const productIndex = productList.value.findIndex((p) => p.id === selectedProduct.value.id)
    if (productIndex !== -1) {
      productList.value[productIndex].productStatus = statusChangeProduct.productStatus
    }

    closeModal()
  } catch (error) {
    console.error('Lỗi chuyển trạng thái: ', error)
  }
}

onMounted(() => {
  fetchProduct()
})

const formatDate = (dateString) => {
  const options = { year: 'numeric', month: '2-digit', day: '2-digit' }
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', options)
}

const previousPage = () => {
  if (page.value > 0) {
    page.value--;
    fetchProduct();
  }
};

const nextPage = () => {
  if (page.value < totalPages.value - 1) {
    page.value++;
    fetchProduct();
  }
};

</script>

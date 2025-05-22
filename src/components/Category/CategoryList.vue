<template>
  <div>
    <h1>Quản lí danh mục</h1>
    <div>
      <button @click="goToAdd">Thêm Danh Mục Mới</button>
    </div>
    <div>
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã danh mục</th>
            <th>Tên Danh Mục</th>
            <th>Ngày tạo</th>
            <th>Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(ct, index) in category" :key="ct.id">
            <td>{{ index + 1 + page * size }}</td>
            <td>{{ ct.cateCode }}</td>
            <td>{{ ct.cateName }}</td>
            <td>{{ ct.dateCreated }}</td>
            <td>
              <button class="btn btn-danger btn-sm me-2" @click="openDeleteModal(ct.id)">
                <i class="fas fa-trash-alt"></i>
              </button>
              <button class="btn btn-info btn-sm me-2" @click="goToUpdate(ct.id)">
                <i class="fas fa-edit"></i>
              </button>
              <button class="btn btn-secondary btn-sm" @click="goToDetail(ct.id)">
                <i class="fas fa-info-circle"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div>
      <p>Trang hiện tại: {{ page + 1 }} / {{ totalPages }}</p>
      <div>
        <button :disabled="page == 0" @click="previousPage">Trước</button>
        <button :disabled="page >= totalPages - 1" @click="nextPage">Tiếp</button>
      </div>
    </div>
  </div>

  <div
    v-if="showModal"
    class="modal fade show"
    tabindex="-1"
    aria-labelledby="deleteModalLabel"
    aria-hidden="true"
    style="display: block"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="deleteModalLabel">Xác nhận xóa</h5>
          <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p>Bạn có chắc chắn muốn xóa nhân viên này không?</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
          <button type="button" class="btn btn-danger" @click="deleteCategory">Xóa</button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'


const category = ref([])
const page = ref(0)
const size = ref(5)
const totalPages = ref(0)

const router = useRouter()

const showModal = ref(false)
const cateToDelete = ref(null)

const goToAdd = () => {
    router.push(`/categories/add`)
}

const goToUpdate = (id) => {
    router.push({name: 'UpdateCategory',params: {id}});
}

const goToDetail = (id) => {
    router.push({name: 'DetailCategory',params: {id}});
}

const openDeleteModal = (id) => {
  cateToDelete.value = id
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const fetchCatgories = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/admin/categories`, {
      params: {
        page: page.value,
        size: size.value,
      },
    })
    category.value = response.data.content
    totalPages.value = response.data.totalPages
  } catch (error) {
    console.error('Có lỗi xảy ra khi tải danh sách danh mục:', error)
  }
}

const previousPage = () => {
  if (page.value > 0) {
    page.value--
    fetchCatgories()
  }
}

const nextPage = () => {
  if (page.value < totalPages.value - 1) {
    page.value++
    fetchCatgories()
  }
}
onMounted(() => {
  fetchCatgories()
})

const deleteCategory = async () => {
  try {
    await axios.put(`http://localhost:8080/api/admin/categories/soft-delete/${cateToDelete.value}`)
    closeModal();
    fetchCatgories();
    console.log('xóa thành công ',cateToDelete.value)
  } catch (error) {
    console.error('Lỗi xóa danh mục: ', error)
  }
}
</script>

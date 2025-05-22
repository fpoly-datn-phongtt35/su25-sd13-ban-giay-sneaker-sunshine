<template>
  <div>
    <h1>Sửa tên danh mục</h1>
    <button @click="goBack">Quay lại</button>
    <form @submit.prevent="openConfirmDialog">
      <div>Category Name: <input type="text" placeholder="Nhập tên danh mục" v-model="updateCategory.cateName"/></div>
      <div>Category Description <input type="text" v-model="updateCategory.cateDescription"/></div>
      <button type="submit">Cập nhật</button>
    </form>
    <!-- Thông báo -->
    <div v-if="err" class="alert alert-danger alert-dismissible mt-3">
      {{ err }}
      <button type="button" class="btn-close" @click="err = null"></button>
    </div>
    <div v-if="success" class="alert alert-success alert-dismissible mt-3">
      {{ success }}
      <button type="button" class="btn-close" @click="success = null"></button>
    </div>

    <!-- Modal -->
    <div
      v-if="isModalVisible"
      class="modal fade show d-block"
      tabindex="-1"
      role="dialog"
      aria-labelledby="confirmDialogTitle"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Xác nhận</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <p>Bạn có chắc chắn muốn lưu danh mục này không?</p>
          </div>
          <div class="modal-footer">
            <button @click="editingCategory" class="btn btn-success">Xác nhận</button>
            <button @click="closeModal" class="btn btn-danger">Hủy</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import axios from 'axios';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import UpdateEmployee from '../Employee/UpdateEmployee.vue';

const router = useRouter();
const route = useRoute();
const isModalVisible = ref(false);

const err = ref(null);
const success = ref(null);

const updateCategory = ref({});

const openConfirmDialog = () => {
    isModalVisible.value = true
}

const closeModal = () => {
    isModalVisible.value = false
}


const editingCategory = async() =>{
    err.value = null;
    success.value = null;

    const id = route.params.id;

    try {
        await axios.put(`http://localhost:8080/api/admin/categories/${id}`,updateCategory.value);
        success.value = 'Sửa danh mục thành công';
        setTimeout(() =>{
            router.push('/categories');
        },2000)
    } catch (error) {
        err.value = error.response?.data?.err || 'Đã xảy ra lỗi khi sửa danh mục';
        console.error(error);
    }finally{
        closeModal();
    }
}

onMounted(() => {
    const id = route.params.id;
    console.log('Id là: ',id);

    axios.get(`http://localhost:8080/api/admin/categories/${id}`)
    .then((response) => {
        updateCategory.value = response.data;
    })
    .catch((error) => {
        err.value = 'Lỗi khi lấy danh mục';
        console.error(error);
    })
})

const goBack = () => {
    router.push(`/categories`);
}
</script>

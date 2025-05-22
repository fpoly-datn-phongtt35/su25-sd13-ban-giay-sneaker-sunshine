<template>
  <div>
    <div>
      <h1>Chi tiết danh mục</h1>
    </div>
    <div>
      <button @click="goBack">Quay lại</button>
    </div>
    <div>
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã danh mục con</th>
            <th>Tên danh mục con</th>
            <th>Mô tả danh mục con</th>
            <th>Ngày tạo</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(sct, index) in DetailCategory" :key="sct.id">
            <td>{{ index + 1 }}</td>
            <td>{{ sct.subCateCode }}</td>
            <td>{{ sct.subCateName }}</td>
            <td>{{ sct.subCateDescription }}</td>
            <td>{{ sct.dateCreated }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();

// Khởi tạo DetailCategory là một mảng
const DetailCategory = ref([]);

// Hàm lấy dữ liệu danh mục
const getCategories = async () => {
  try {
    const id = route.params.id;
    const response = await axios.get(`http://localhost:8080/api/admin/subcategories/detail-categories/${id}`);

    // Đảm bảo response.data là một mảng
    DetailCategory.value = Array.isArray(response.data)
      ? response.data.map((item) => ({
          ...item,
          dateCreated: item.dateCreated
            ? new Date(item.dateCreated).toISOString().split('T')[0]
            : null,
        }))
      : [];
  } catch (error) {
    console.error('Lỗi khi lấy danh mục con: ', error);
  }
};

// Tự động gọi hàm khi component được mount
onMounted(() => {
  getCategories();
});

// Hàm quay lại
const goBack = () => {
  router.push('/categories');
};
</script>

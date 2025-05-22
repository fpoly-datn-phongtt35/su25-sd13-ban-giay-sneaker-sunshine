<template>
    <div class="container mt-3">
      <div>
        <button class="btn btn-secondary mb-3" @click="goBack">Quay lại</button>
      </div>
  
      <div class="table-responsive">
        <table class="table table-bordered">
          <thead class="table-dark">
            <tr>
              <th>STT</th>
              <th>Mã Sản Phẩm Chi Tiết</th>
              <th>Tên Sản Phẩm</th>
              <th>Kích thước</th>
              <th>Màu Sắc</th>
              <th>Giá</th>
              <th>Số Lượng</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(dt, index) in detailProduct" :key="dt.id">
              <td>{{ index + 1 }}</td>
              <td>{{ dt.productDetailCode }}</td>
              <td>{{ dt.productName }}</td>
              <td>{{ dt.sizeName }}</td>
              <td>{{ dt.colorName }}</td>
              <td>{{ dt.sellPrice }}</td>
              <td>{{ dt.quantity }}</td>
              <td>
                <span class="badge" :class="dt.productDetailStatus === 1 ? 'bg-success' : 'bg-danger'">
                  {{ dt.productDetailStatus === 1 ? 'Đang Bán' : 'Ngừng Bán' }}
                </span>
              </td>
              <td>
                <button class="btn btn-warning btn-sm" @click="openConfirmDialog(dt)">
                  {{ dt.productDetailStatus === 1 ? 'Ngừng bán' : 'Kích hoạt' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
  
      <!-- Modal xác nhận -->
      <div class="modal fade" :class="{ show: showModal }" tabindex="-1" style="display: block" v-if="showModal">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Xác nhận thay đổi trạng thái</h5>
              <button type="button" class="btn-close" @click="closeModal"></button>
            </div>
            <div class="modal-body">
              <p>
                Bạn có chắc chắn muốn
                <strong>{{ selectedProductDetail?.productDetailStatus === 1 ? 'ngừng bán' : 'kích hoạt' }}</strong>
                sản phẩm này không?
              </p>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
              <button type="button" class="btn btn-warning" @click="stateChangeDetail">
                {{ selectedProductDetail?.productDetailStatus === 1 ? 'Ngừng bán' : 'Kích hoạt' }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show" v-if="showModal"></div>
    </div>
  </template>
  
  <script setup>
  import axios from 'axios';
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  
  const router = useRouter();
  const route = useRoute();
  const detailProduct = ref([]);
  const showModal = ref(false);
  const selectedProductDetail = ref(null);
  
  // Mở modal xác nhận
  const openConfirmDialog = (productDetails) => {
    selectedProductDetail.value = { ...productDetails };
    showModal.value = true;
  };
  
  // Đóng modal
  const closeModal = () => {
    showModal.value = false;
  };
  
  const stateChangeDetail = async () => {
  if (!selectedProductDetail.value) return;

  console.log('Đang cập nhật trạng thái cho sản phẩm: ', selectedProductDetail.value);

  try {
    const id = selectedProductDetail.value.id;
    const response = await axios.put(`http://localhost:8080/api/admin/product/state-change/detail/${id}`);
    console.log('Phản hồi từ API: ', response);

    if (response.status === 200) {
      const newStatus = response.data.productDetailStatus; // Lấy trạng thái từ phản hồi

      // Tìm và cập nhật trạng thái trong danh sách
      const index = detailProduct.value.findIndex((p) => p.id === id);
      if (index !== -1) {
        console.log('Cập nhật trạng thái cho sản phẩm tại index:', index);
        
        // Thay vì chỉ cập nhật trực tiếp, tạo một bản sao mảng mới
        detailProduct.value = [
          ...detailProduct.value.slice(0, index),
          { ...detailProduct.value[index], productDetailStatus: newStatus },
          ...detailProduct.value.slice(index + 1)
        ];

        // Debug cập nhật trong mảng
        console.log('Danh sách sản phẩm sau khi cập nhật:', detailProduct.value);
      }
    }
    closeModal();
  } catch (error) {
    console.error('Lỗi khi chuyển trạng thái:', error);
  }
};

  
  // Quay lại trang sản phẩm
  const goBack = () => {
    router.push('/product');
  };
  
  // Lấy danh sách sản phẩm chi tiết
  const fetchProductDetail = async () => {
    const id = route.params.id;
    try {
      const response = await axios.get(`http://localhost:8080/api/admin/product/${id}`);
      console.log('Danh sách sản phẩm chi tiết: ', response.data.productDetails);
      detailProduct.value = response.data.productDetails;
    } catch (error) {
      console.error('Lỗi khi lấy dữ liệu sản phẩm:', error);
    }
  };
  
  onMounted(fetchProductDetail);
  </script>
  
<template>
  <div>
    <button @click="goBack">Quay lại</button>
  </div>

  <div>
    <div>
      <h2>Chi tiết sản phẩm</h2>
    </div>

    <div>
      <form @submit.prevent="openConfirmDialog">
        <!-- Select Category -->
        <div class="mb-3">
          <label for="subcategory" class="form-label text-primary">Chọn danh mục sản phẩm</label>
          <select id="subcategory" v-model="product.subCateId" class="form-select">
            <option v-for="st in subcateList" :key="st.id" :value="st.id">
              {{ st.subCateName }}
            </option>
          </select>
        </div>

        <!-- Product Name -->
        <div class="mb-3">
          <label for="productName" class="form-label text-primary">Tên sản phẩm</label>
          <input
            type="text"
            id="productName"
            placeholder="Nhập tên sản phẩm"
            v-model="product.productName"
            class="form-control shadow-sm"
          />
        </div>

        <!-- Material -->
        <div class="mb-3">
          <label for="material" class="form-label text-primary">Chọn Chất Liệu</label>
          <select v-model="product.materialId" class="form-select shadow-sm">
            <option v-for="mt in materialList" :key="mt.id" :value="mt.id">
              {{ mt.materialName }}
            </option>
          </select>
        </div>

        <!-- Supplier -->
        <div class="mb-3">
          <label for="supplier" class="form-label text-primary">Chọn nhà cung cấp</label>
          <select v-model="product.supplierId" class="form-select shadow-sm">
            <option v-for="sp in supplierList" :key="sp.id" :value="sp.id">
              {{ sp.supplierName }}
            </option>
          </select>
        </div>

        <!-- Brand -->
        <div class="mb-3">
          <label for="brand" class="form-label text-primary">Chọn thương hiệu</label>
          <select v-model="product.brandId" class="form-select shadow-sm">
            <option v-for="br in brandList" :key="br.id" :value="br.id">
              {{ br.brandName }}
            </option>
          </select>
        </div>

        <!-- Origin Price -->
        <div class="mb-3">
          <label for="originPrice" class="form-label text-primary">Giá gốc sản phẩm</label>
          <input
            type="number"
            id="originPrice"
            placeholder="Nhập giá gốc sản phẩm"
            v-model="product.originPrice"
            class="form-control shadow-sm"
          />
        </div>

        <!-- Sell Price -->
        <div class="mb-3">
          <label for="sellPrice" class="form-label text-primary">Giá bán sản phẩm</label>
          <input
            type="number"
            id="sellPrice"
            placeholder="Nhập giá bán sản phẩm"
            v-model="product.sellPrice"
            class="form-control shadow-sm"
            required
          />
        </div>

        <!-- Note -->
        <div class="mb-3">
          <label for="note" class="form-label text-primary">Ghi chú sản phẩm</label>
          <textarea
            id="note"
            placeholder="Ghi chú sản phẩm"
            v-model="product.note"
            rows="3"
            class="form-control shadow-sm"
          ></textarea>
        </div>

        <!-- Display current product images -->
        <div v-if="product.images && product.images.length">
          <h5>Ảnh hiện tại:</h5>
          <div class="d-flex">
            <img
              v-for="(img, index) in product.images"
              :key="index"
              :src="img.imageUrl.startsWith('data:image') ? img.imageUrl : 'data:image/png;base64,' + img.imageUrl"
              class="img-thumbnail me-2"
              width="100"
              alt="Ảnh sản phẩm"
            />
          </div>
        </div>

        <!-- Image Upload -->
        <div class="mb-3">
          <label for="product-images" class="form-label text-primary">Chọn hình ảnh:</label>
          <input
            id="product-images"
            type="file"
            multiple
            @change="handleFileChange"
            class="form-control"
          />
        </div>

        <div>
          <button type="submit">Cập nhật sản phẩm</button>
        </div>
      </form>
    </div>

    <!-- Error or success notification -->
    <div v-if="err" class="alert alert-danger mt-3 d-flex justify-content-between align-items-center">
      <span>{{ err }}</span>
      <button type="button" class="btn-close" @click="err = null"></button>
    </div>

    <div v-if="success" class="alert alert-success mt-3 d-flex justify-content-between align-items-center">
      <span>{{ success }}</span>
      <button type="button" class="btn-close" @click="success = null"></button>
    </div>

    <!-- Confirmation Modal -->
    <div v-if="isModalVisible" class="modal fade show d-block" tabindex="-1" role="dialog" aria-labelledby="confirmDialogTitle" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title">Xác nhận</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <p>Bạn có chắc chắn muốn cập nhật sản phẩm này không?</p>
          </div>
          <div class="modal-footer">
            <button @click="updatedProduct" class="btn btn-success">Xác nhận</button>
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

const router = useRouter();
const route = useRoute();

const subcateList = ref([]);
const brandList = ref([]);
const materialList = ref([]);
const supplierList = ref([]);
const success = ref(null);
const err = ref(null);
const isModalVisible = ref(false);

const openConfirmDialog = () => {
  isModalVisible.value = true;
};

const closeModal = () => {
  isModalVisible.value = false;
};

const product = ref({
  subCateId: null,
  productName: '',
  materialId: null,
  supplierId: null,
  brandId: null,
  originPrice: null,
  sellPrice: null,
  note: '',
  images: [], // Lưu các URL của ảnh
  productDetails: [],
});

const previewImages = ref([]);
const base64Images = ref([]);

const fetchProduct = async () => {
  const id = route.params.id;
  try {
    const response = await axios.get(`http://localhost:8080/api/admin/product/${id}`);
    product.value = response.data;

    // Nếu có ảnh, xử lý ảnh Base64
    if (product.value.images && Array.isArray(product.value.images)) {
      product.value.images = product.value.images.map((img) => ({
        imageUrl: img.imageUrl ? (img.imageUrl.startsWith('data:image') ? img.imageUrl : `data:image/png;base64,${img.imageUrl}`) : '',
      }));
    } else {
      product.value.images = [];
    }
  } catch (error) {
    console.error('Lỗi khi lấy dữ liệu sản phẩm: ', error);
  }
};

// Lấy danh mục sản phẩm
const fetchSubcate = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/subcategories');
    subcateList.value = response.data;
  } catch (error) {
    console.error('Lỗi lấy danh mục con: ', error);
  }
};

// Lấy material sản phẩm
const fecthMaterial = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/material');
    materialList.value = response.data;
  } catch (error) {
    console.error('Lỗi lấy material: ', error);
  }
};

// Lấy nhà cung cấp sản phẩm
const fetchSupplier = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/supplier');
    supplierList.value = response.data;
  } catch (error) {
    console.error('Lỗi lấy nhà cung cấp: ', error);
  }
};

// Lấy thương hiệu sản phẩm
const fetchBrand = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/brand');
    brandList.value = response.data;
  } catch (error) {
    console.error('Lỗi lấy thương hiệu sản phẩm: ', error);
  }
};

const goBack = () => {
  router.push('/product');
};

// Xử lý cập nhật sản phẩm
const updatedProduct = async () => {
  err.value = null;
  success.value = null;
  const id = route.params.id;

  try {
    const updateProduct = { ...product.value, images: base64Images.value };
    await axios.put(`http://localhost:8080/api/admin/product/update/${id}`, updateProduct);
    success.value = 'Sửa sản phẩm thành công';
    setTimeout(() => {
      router.push('/product');
    }, 2000);
  } catch (error) {
    err.value = error.response?.data?.err || 'Đã xảy ra lỗi khi sửa sản phẩm';
    console.error(error);
  } finally {
    closeModal();
  }
};

// Xử lý khi chọn ảnh
const handleFileChange = (event) => {
  const files = event.target.files;
  const newImages = [];

  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    const reader = new FileReader();
    reader.onloadend = () => {
      newImages.push(reader.result.split(',')[1]);
      if (newImages.length === files.length) {
        base64Images.value = newImages;
      }
    };
    reader.readAsDataURL(file);
  }
};

// Fetch dữ liệu khi component mounted
onMounted(() => {
  fetchProduct();
  fetchSubcate();
  fetchBrand();
  fetchSupplier();
  fecthMaterial();
});
</script>

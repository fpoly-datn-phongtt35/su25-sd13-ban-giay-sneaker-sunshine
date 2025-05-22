<template>
  <div class="container mt-5">
    <!-- Button for going back -->
    <div class="mb-3">
      <button @click="goBack" class="btn btn-outline-secondary rounded-pill shadow-sm">
        <i class="bi bi-arrow-left-circle"></i> Quay lại
      </button>
    </div>

    <!-- Add product form -->
    <form @submit.prevent="openConfirmDialog" class="bg-light p-4 rounded shadow-lg">
      <h2 class="text-center mb-4">Thêm Sản Phẩm Mới</h2>

      <!-- Select Category -->
      <div class="mb-3">
        <label for="subcategory" class="form-label text-primary">Chọn danh mục sản phẩm</label>
        <select id="subcategory" v-model="newProduct.subCateId" class="form-select">
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
          v-model="newProduct.productName"
          class="form-control shadow-sm"
        />
      </div>

      <!-- Material -->
      <div class="mb-3">
        <label for="material" class="form-label text-primary">Chọn Chất Liệu</label>
        <select v-model="newProduct.materialId" class="form-select shadow-sm">
          <option v-for="mt in materialList" :key="mt.id" :value="mt.id">
            {{ mt.materialName }}
          </option>
        </select>
      </div>

      <!-- Supplier -->
      <div class="mb-3">
        <label for="supplier" class="form-label text-primary">Chọn nhà cung cấp</label>
        <select v-model="newProduct.supplierId" class="form-select shadow-sm">
          <option v-for="sp in supplierList" :key="sp.id" :value="sp.id">
            {{ sp.supplierName }}
          </option>
        </select>
      </div>

      <!-- Brand -->
      <div class="mb-3">
        <label for="brand" class="form-label text-primary">Chọn thương hiệu</label>
        <select v-model="newProduct.brandId" class="form-select shadow-sm">
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
          v-model="newProduct.originPrice"
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
          v-model="newProduct.sellPrice"
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
          v-model="newProduct.note"
          rows="3"
          class="form-control shadow-sm"
        ></textarea>
      </div>

      <!-- Select Sizes -->
      <div class="mb-3">
        <label class="form-label text-primary">Kích thước</label>
        <div v-for="size in sizeList" :key="size.id" class="form-check form-check-inline">
          <input
            type="checkbox"
            class="form-check-input"
            :value="size.id"
            v-model="selectedSizes"
            @change="generateProductDetails"
          />
          <label class="form-check-label">{{ size.sizeName }}</label>
        </div>
      </div>

      <!-- Select Colors -->
      <div class="mb-3">
        <label class="form-label text-primary">Màu sắc</label>
        <div v-for="color in colorList" :key="color.id" class="form-check form-check-inline">
          <input
            type="checkbox"
            class="form-check-input"
            :value="color.id"
            v-model="selectedColors"
            @change="generateProductDetails"
          />
          <label class="form-check-label">{{ color.colorName }}</label>
        </div>
      </div>

      <!-- Product Details -->
      <div v-for="(detail, index) in productDetails" :key="index" class="card mb-4 shadow-sm">
        <div class="card-header bg-info text-white">
          <strong>Chi Tiết Sản Phẩm {{ index + 1 }}</strong>
        </div>
        <div class="card-body">
          <div class="mb-3">
            <label class="form-label">Kích Thước</label>
            <input type="text" :value="detail.sizeName" readonly class="form-control" />
          </div>
          <div class="mb-3">
            <label class="form-label">Màu Sắc</label>
            <input type="text" :value="detail.colorName" readonly class="form-control" />
          </div>
          <div class="mb-3">
            <label class="form-label">Giá Bán</label>
            <input type="number" v-model="detail.sellPrice" class="form-control" required />
          </div>
          <div class="mb-3">
            <label class="form-label">Số Lượng</label>
            <input type="number" v-model="detail.quantity" class="form-control" required />
          </div>
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


      <!-- Submit Button -->
      <div class="text-center">
        <button type="submit" class="btn btn-success btn-lg shadow-sm">Thêm sản phẩm</button>
      </div>
    </form>

    <!-- Error or success notification -->
    <div
      v-if="err"
      class="alert alert-danger mt-3 d-flex justify-content-between align-items-center"
    >
      <span>{{ err }}</span>
      <button type="button" class="btn-close" @click="err = null"></button>
    </div>
    <div
      v-if="success"
      class="alert alert-success mt-3 d-flex justify-content-between align-items-center"
    >
      <span>{{ success }}</span>
      <button type="button" class="btn-close" @click="success = null"></button>
    </div>

    <!-- Confirmation Modal -->
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
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title">Xác nhận</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <p>Bạn có chắc chắn muốn lưu sản phẩm này không?</p>
          </div>
          <div class="modal-footer">
            <button @click="saveProduct" class="btn btn-success">Xác nhận</button>
            <button @click="closeModal" class="btn btn-danger">Hủy</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const subcateList = ref([])
const brandList = ref([])
const materialList = ref([])
const supplierList = ref([])
const sizeList = ref([])
const colorList = ref([])

const success = ref(null)
const err = ref(null)
const isModalVisible = ref(false)

// Dữ liệu sản phẩm mới
const newProduct = ref({
  subCateId: null,
  productName: '',
  materialId: null,
  supplierId: null,
  brandId: null,
  originPrice: null,
  sellPrice: null,
  note: '',
})

// Dữ liệu tệp hình ảnh
const selectedFiles = ref([])

const selectedSizes = ref([])
const selectedColors = ref([])
const productDetails = ref([])

// Hiển thị modal xác nhận
const openConfirmDialog = () => {
  isModalVisible.value = true
}

// Đóng modal
const closeModal = () => {
  isModalVisible.value = false
}

// Lấy danh mục sản phẩm
const fetchSubcate = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/subcategories')
    subcateList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy danh mục con: ', error)
  }
}

//lấy material sản phẩm
const fecthMaterial = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/material')
    materialList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy material: ', error)
  }
}

//lấy nhà cung cấp sản phẩm
const fetchSupplier = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/supplier')
    supplierList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy nhà cung cấp: ', error)
  }
}

//lấy thương hiệu sản phẩm
const fetchBrand = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/brand')
    brandList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy thương hiệu sản phẩm: ', error)
  }
}

// kết hợp lấy cả kích thước với cả màu
const fetchSizesAndColors = async () => {
  try {
    const [sizesResponse, colorsResponse] = await Promise.all([
      axios.get('http://localhost:8080/api/admin/size'),
      axios.get('http://localhost:8080/api/admin/color'),
    ])
    sizeList.value = sizesResponse.data
    colorList.value = colorsResponse.data
  } catch (error) {
    console.error('Lỗi lấy kích thước hoặc màu sắc: ', error)
  }
}

const generateProductDetails = () => {
  productDetails.value = []
  for (const sizeId of selectedSizes.value) {
    for (const colorId of selectedColors.value) {
      const size = sizeList.value.find((s) => s.id === sizeId)
      const color = colorList.value.find((c) => c.id === colorId)
      productDetails.value.push({
        sizeId: sizeId,
        colorId: colorId,
        sizeName: size.sizeName,
        colorName: color.colorName,
        sellPrice: newProduct.value.sellPrice,
        quantity: 0,
      })
    }
  }
}

// Quay lại trang trước
const goBack = () => {
  router.push(`/product`)
}


const handleFileChange = async (event) => {
  const files = event.target.files
  if (!files || files.length === 0) {
    console.log('❌ Không có tệp nào được chọn.')
    return
  }

  // Giữ lại danh sách ảnh đã chọn thay vì ghi đè
  selectedFiles.value = [...selectedFiles.value, ...files]

  console.log('🟢 Số lượng file đã chọn:', selectedFiles.value.length)

  for (const file of files) {
    try {
      const storageRef = ref(storage, `images/${file.name}`)
      const snapshot = await uploadBytes(storageRef, file)
      const downloadURL = await getDownloadURL(snapshot.ref)

      console.log(`✅ Tải lên thành công: ${file.name}`)
      console.log(`🔗 URL tải xuống: ${downloadURL}`)
    } catch (error) {
      console.error(`❌ Lỗi khi tải lên ${file.name}:`, error)
    }
  }
}

const saveProduct = async () => {
  err.value = null
  success.value = null

  try {
    const formData = new FormData()

    // 1. Dữ liệu sản phẩm chính
    const productData = {
      subCateId: newProduct.value.subCateId,
      productName: newProduct.value.productName,
      materialId: newProduct.value.materialId,
      supplierId: newProduct.value.supplierId,
      brandId: newProduct.value.brandId,
      originPrice: newProduct.value.originPrice,
      sellPrice: newProduct.value.sellPrice,
      note: newProduct.value.note,
    }

    // 2. Dữ liệu chi tiết sản phẩm
    const productDetailsData = productDetails.value.map((detail) => ({
      sizeId: detail.sizeId,
      colorId: detail.colorId,
      sellPrice: detail.sellPrice,
      quantity: detail.quantity,
    }))

    // 3. Gửi dữ liệu JSON vào form
    formData.append('product', JSON.stringify(productData))
    formData.append('productDetails', JSON.stringify(productDetailsData))

    // 4. Thêm các file ảnh (nếu có)
    selectedFiles.value.forEach((file) => {
      formData.append('images', file)
    })

    // 5. Gửi lên server
    const response = await axios.post('http://localhost:8080/api/admin/product', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    success.value = 'Thêm sản phẩm thành công!'
    console.log('✅ Server phản hồi:', response.data)

    // Reset form
    newProduct.value = {
      subCateId: null,
      productName: '',
      materialId: null,
      supplierId: null,
      brandId: null,
      originPrice: null,
      sellPrice: null,
      note: '',
    }
    selectedFiles.value = []
    productDetails.value = []

    setTimeout(() => {
      router.push('/product')
    }, 2000)
  } catch (error) {
    console.error('❌ Lỗi:', error)
    err.value = error.response?.data?.error || 'Đã xảy ra lỗi khi thêm sản phẩm.'
  }
}


onMounted(() => {
  fetchSubcate()
  fetchBrand()
  fetchSupplier()
  fecthMaterial()
  fetchSizesAndColors()
})
</script>

<style scoped>
/* Thêm CSS tùy chỉnh nếu cần */
.modal-content {
  max-width: 500px;
  margin: auto;
}
</style>

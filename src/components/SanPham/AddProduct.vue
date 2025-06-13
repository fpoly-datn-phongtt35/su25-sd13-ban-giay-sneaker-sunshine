```vue
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

      <div class="mb-3">
        <label class="form-label text-primary">Chọn danh mục sản phẩm</label>
        <multiselect
          v-model="newProduct.categoryIds"
          :options="categoryList"
          :multiple="true"
          :close-on-select="false"
          placeholder="Chọn danh mục"
          label="categoryName"
          track-by="id"
          class="form-control"
        />
        <p class="mt-2">
          Danh mục đã chọn:
          <span v-if="newProduct.categoryIds.length === 0">Chưa chọn</span>
          <span v-else>{{ newProduct.categoryIds.map((c) => c.categoryName).join(', ') }}</span>
        </p>
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

      <!-- Gender (Target Audience) -->
      <div class="mb-3">
        <label class="form-label text-primary">Dành cho: </label>
        <div class="form-check form-check-inline">
          <input
            class="form-check-input"
            type="radio"
            id="genderMale"
            value="1"
            v-model="newProduct.genderId"
          />
          <label class="form-check-label" for="genderMale">Nam</label>
        </div>
        <div class="form-check form-check-inline">
          <input
            class="form-check-input"
            type="radio"
            id="genderFemale"
            value="2"
            v-model="newProduct.genderId"
          />
          <label class="form-check-label" for="genderFemale">Nữ</label>
        </div>
        <div class="form-check form-check-inline">
          <input
            class="form-check-input"
            type="radio"
            id="genderBoth"
            value="3"
            v-model="newProduct.genderId"
          />
          <label class="form-check-label" for="genderBoth">Cả nam và nữ</label>
        </div>
      </div>

      <div class="mb-3">
        <label for="supplier" class="form-label text-primary">Chọn nhà cung cấp</label>
        <select v-model="newProduct.supplierId" class="form-select shadow-sm">
          <option v-for="sp in supplierList" :key="sp.id" :value="sp.id">
            {{ sp.supplierName }}
          </option>
        </select>
      </div>

      <!-- Sole -->
      <div class="mb-3">
        <label for="sole" class="form-label text-primary">Chọn loại đế</label>
        <select v-model="newProduct.soleId" class="form-select shadow-sm">
          <option v-for="sp in soleList" :key="sp.id" :value="sp.id">
            {{ sp.soleName }}
          </option>
        </select>
      </div>

      <!-- Style -->
      <div class="mb-3">
        <label for="style" class="form-label text-primary">Chọn cổ giày</label>
        <select v-model="newProduct.styleId" class="form-select shadow-sm">
          <option v-for="sp in styleList" :key="sp.id" :value="sp.id">
            {{ sp.styleName }}
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

      <div class="mb-3">
        <label for="weight" class="form-label text-primary">Cân nặng (gram)</label>
        <input
          type="number"
          id="weight"
          v-model="newProduct.weight"
          class="form-control shadow-sm"
          placeholder="Nhập cân nặng sản phẩm (gram)"
          min="0"
        />
      </div>

      <!-- Origin Price -->
      <div class="mb-3">
        <label for="originPrice" class="form-label text-primary">Giá bán buôn</label>
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
        <label for="sellPrice" class="form-label text-primary">Giá bán lẻ</label>
        <input
          type="number"
          id="sellPrice"
          placeholder="Nhập giá bán sản phẩm"
          v-model="newProduct.sellPrice"
          class="form-control shadow-sm"
        />
      </div>

      <!-- Note -->
      <div class="mb-3">
        <label for="note" class="form-label text-primary">Ghi chú sản phẩm</label>
        <textarea
          id="note"
          placeholder="Ghi chú sản phẩm"
          v-model="newProduct.description"
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

      <!-- Image Upload and Display -->
      <el-upload
        class="upload-demo"
        action="#"
        :on-preview="handlePreview"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        list-type="picture-card"
        :limit="10"
        :file-list="selectedFiles"
        :auto-upload="false"
        multiple
        accept="image/*"
      >
        <el-button type="primary">Tải lên hình ảnh</el-button>
      </el-upload>

      <!-- Submit Button -->
      <div class="text-center">
        <button type="submit" class="btn btn-success btn-lg shadow-sm">Thêm sản phẩm</button>
      </div>
    </form>

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
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.min.css'
import { ElNotification } from 'element-plus'

const router = useRouter()

const brandList = ref([])
const materialList = ref([])
const categoryList = ref([])
const sizeList = ref([])
const colorList = ref([])
const soleList = ref([])
const supplierList = ref([])
const styleList = ref([])

const isModalVisible = ref(false)
const selectedFiles = ref([])
const selectedSizes = ref([])
const selectedColors = ref([])
const productDetails = ref([])

const newProduct = ref({
  categoryIds: [],
  productName: '',
  materialId: null,
  styleId: null,
  supplierId: null,
  genderId: null,
  soleId: null,
  brandId: null,
  originPrice: null,
  weight: null,
  sellPrice: null,
  description: '',
})

// Hiển thị thông báo thành công
const showSuccess = (message) => {
  ElNotification({
    title: 'Thành công',
    message,
    type: 'success',
    duration: 3000,
    position: 'top-right',
  })
}

// Hiển thị thông báo lỗi
const showError = (message) => {
  ElNotification({
    title: 'Lỗi',
    message,
    type: 'error',
    duration: 3000,
    position: 'top-right',
  })
}

// Mở dialog xác nhận
const openConfirmDialog = () => {
  if (selectedFiles.value.length === 0) {
    showError('Vui lòng chọn ít nhất một ảnh cho sản phẩm!')
    return
  }
  isModalVisible.value = true
}

// Đóng dialog
const closeModal = () => {
  isModalVisible.value = false
}

// Lấy danh mục sản phẩm
const fetchCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/categories/hien-thi')
    categoryList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy danh mục:', error)
    showError('Lỗi lấy danh mục sản phẩm!')
  }
}

// Lấy chất liệu sản phẩm
const fetchMaterial = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/material/hien-thi')
    materialList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy chất liệu:', error)
    showError('Lỗi lấy chất liệu sản phẩm!')
  }
}

// Lấy nhà cung cấp
const fetchSupplier = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/supplier/hien-thi')
    supplierList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy nhà cung cấp:', error)
    showError('Lỗi lấy nhà cung cấp!')
  }
}

// Lấy thương hiệu sản phẩm
const fetchBrand = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/brand/hien-thi')
    brandList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy thương hiệu:', error)
    showError('Lỗi lấy thương hiệu sản phẩm!')
  }
}

// Lấy đế giày sản phẩm
const fetchSole = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/sole/hien-thi')
    soleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy đế giày:', error)
    showError('Lỗi lấy đế giày sản phẩm!')
  }
}

// Lấy phong cách sản phẩm
const fetchStyle = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/style/hien-thi')
    styleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy phong cách:', error)
    showError('Lỗi lấy phong cách sản phẩm!')
  }
}

// Lấy kích thước và màu sắc
const fetchSizesAndColors = async () => {
  try {
    const [sizesResponse, colorsResponse] = await Promise.all([
      axios.get('http://localhost:8080/api/admin/size/hien-thi'),
      axios.get('http://localhost:8080/api/admin/color/hien-thi'),
    ])
    sizeList.value = sizesResponse.data
    colorList.value = colorsResponse.data
  } catch (error) {
    console.error('Lỗi lấy kích thước hoặc màu sắc:', error)
    showError('Lỗi lấy kích thước hoặc màu sắc!')
  }
}

// Tạo chi tiết sản phẩm
const generateProductDetails = () => {
  productDetails.value = []
  for (const sizeId of selectedSizes.value) {
    for (const colorId of selectedColors.value) {
      const size = sizeList.value.find((s) => s.id === sizeId)
      const color = colorList.value.find((c) => c.id === colorId)
      productDetails.value.push({
        sizeId: sizeId,
        colorId: colorId,
        sizeName: size?.sizeName || '',
        colorName: color?.colorName || '',
        sellPrice: newProduct.value.sellPrice || 0,
        quantity: 0,
      })
    }
  }
}

// Quay lại trang trước
const goBack = () => {
  router.push('/product')
}

// Xử lý khi chọn file trong el-upload
const handleFileChange = (file, fileList) => {
  // Kiểm tra kích thước file (giới hạn 5MB)
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    showError(`Ảnh ${file.name} vượt quá kích thước tối đa (5MB)!`)
    fileList.splice(fileList.indexOf(file), 1) // Xóa file khỏi fileList
    return
  }

  // Kiểm tra file trùng (dựa trên tên và kích thước)
  const isDuplicate = selectedFiles.value.some(
    (f) => f.name === file.name && f.file?.size === file.size
  )
  if (isDuplicate) {
    showError(`Ảnh ${file.name} đã được chọn!`)
    fileList.splice(fileList.indexOf(file), 1) // Xóa file trùng
    return
  }

  console.log('File mới:', { name: file.name, size: file.size, totalFiles: fileList.length })
  selectedFiles.value = fileList.map((item) => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null,
  }))
  console.log('selectedFiles sau khi thêm:', selectedFiles.value.map(f => ({ name: f.name, size: f.file?.size })))
}

// Xử lý khi xóa file
const handleFileRemove = (file, fileList) => {
  console.log('File xóa:', { name: file.name, totalFiles: fileList.length })
  selectedFiles.value = fileList.map((item) => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null,
  }))
  console.log('selectedFiles sau khi xóa:', selectedFiles.value.map(f => ({ name: f.name, size: f.file?.size })))
}

// Xử lý xem trước ảnh
const handlePreview = (file) => {
  console.log('Xem trước:', file.name)
  window.open(file.url, '_blank')
}

// Lưu sản phẩm
const saveProduct = async () => {
  try {
    console.log('selectedFiles:', selectedFiles.value.map(f => ({ name: f.name, size: f.file?.size })))
    console.log('productDetails:', productDetails.value)

    const totalQuantity = productDetails.value.reduce((sum, detail) => sum + Number(detail.quantity), 0)
    if (totalQuantity <= 0) {
      showError('Tổng số lượng của các biến thể sản phẩm phải lớn hơn 0!')
      isModalVisible.value = false
      return
    }

    const newImages = selectedFiles.value.filter((fileObj) => fileObj.file)
    if (newImages.length === 0) {
      showError('Vui lòng chọn ít nhất một ảnh cho sản phẩm!')
      isModalVisible.value = false
      return
    }

    console.log('newImages:', { count: newImages.length, names: newImages.map(f => f.name) })

    const formData = new FormData()
    formData.append('productName', newProduct.value.productName || '')
    formData.append('materialId', newProduct.value.materialId || '')
    formData.append('supplierId', newProduct.value.supplierId || '')
    formData.append('brandId', newProduct.value.brandId || '')
    formData.append('soleId', newProduct.value.soleId || '')
    formData.append('styleId', newProduct.value.styleId || '')
    formData.append('genderId', newProduct.value.genderId || '')
    formData.append('weight', newProduct.value.weight || 0)
    formData.append('originPrice', newProduct.value.originPrice || 0)
    formData.append('sellPrice', newProduct.value.sellPrice || 0)
    formData.append('quantity', totalQuantity)
    formData.append('description', newProduct.value.description || '')

    const categories = Array.isArray(newProduct.value.categoryIds)
      ? newProduct.value.categoryIds
      : []
    categories.forEach((cat, index) => {
      const categoryId = typeof cat === 'object' && cat !== null ? cat.id : cat
      formData.append(`categoryIds[${index}]`, categoryId)
    })

    productDetails.value.forEach((detail, index) => {
      formData.append(`productDetails[${index}].sizeId`, detail.sizeId)
      formData.append(`productDetails[${index}].colorId`, detail.colorId)
      formData.append(`productDetails[${index}].sellPrice`, detail.sellPrice)
      formData.append(`productDetails[${index}].quantity`, detail.quantity)
    })

    // Thêm ảnh mới với cùng key 'productImages' (giống Postman)
    newImages.forEach((fileObj) => {
      formData.append('productImages[]', fileObj.file)
    })

    // Log nội dung FormData
    console.log('FormData Contents:')
    for (let [key, value] of formData.entries()) {
      console.log(`FormData: ${key} =`, value instanceof File ? value.name : value)
    }

    console.log('Gửi FormData tới server...')
    const response = await axios.post('http://localhost:8080/api/admin/products', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    console.log('Phản hồi server:', JSON.stringify(response.data, null, 2))
    showSuccess('Thêm sản phẩm thành công!')
    isModalVisible.value = false

    // Reset form
    newProduct.value = {
      categoryIds: [],
      productName: '',
      materialId: null,
      styleId: null,
      supplierId: null,
      genderId: null,
      soleId: null,
      brandId: null,
      originPrice: null,
      weight: null,
      sellPrice: null,
      description: '',
    }
    selectedFiles.value = []
    productDetails.value = []
    selectedSizes.value = []
    selectedColors.value = []

    setTimeout(() => {
      router.push('/product')
    }, 3000)
  } catch (error) {
    console.error('Lỗi thêm sản phẩm:', error)
    console.error('Phản hồi lỗi server:', error.response?.data)
    const errorMessage = error.response?.data?.error || 'Đã xảy ra lỗi khi thêm sản phẩm.'
    showError(errorMessage)
    isModalVisible.value = false
  }
}

onMounted(() => {
  fetchBrand()
  fetchMaterial()
  fetchSizesAndColors()
  fetchSole()
  fetchStyle()
  fetchCategories()
  fetchSupplier()
})
</script>

<style scoped>
.container {
  max-width: 1200px;
}

.bg-light {
  background-color: #f8f9fa;
}

.form-control,
.form-select {
  border-radius: 0.5rem;
}

.btn-success {
  background-color: #28a745;
  border-color: #28a745;
}

.btn-success:hover {
  background-color: #218838;
  border-color: #1e7e34;
}

.modal-content {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.modal-header {
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}
</style>
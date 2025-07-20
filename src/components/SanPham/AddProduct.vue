<template>
  <div class="container mt-5">
    <div class="mb-3">
      <button @click="goBack" class="btn btn-outline-secondary rounded-pill shadow-sm">
        <i class="bi bi-arrow-left-circle"></i> Quay lại
      </button>
    </div>

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
          :class="{ 'is-invalid': errors.categoryIds }"
        />
        <p class="mt-2">
          Danh mục đã chọn:
          <span v-if="newProduct.categoryIds.length === 0">Chưa chọn</span>
          <span v-else>{{ newProduct.categoryIds.map((c) => c.categoryName).join(', ') }}</span>
        </p>
        <div v-if="errors.categoryIds" class="text-danger">{{ errors.categoryIds }}</div>
      </div>

      <div class="mb-3">
        <label for="productName" class="form-label text-primary">Tên sản phẩm</label>
        <input
          type="text"
          id="productName"
          placeholder="Nhập tên sản phẩm"
          v-model="newProduct.productName"
          class="form-control shadow-sm"
          :class="{ 'is-invalid': errors.productName }"
        />
        <div v-if="errors.productName" class="text-danger">{{ errors.productName }}</div>
      </div>

      <div class="mb-3">
        <label for="material" class="form-label text-primary">Chọn Chất Liệu</label>
        <select
          v-model="newProduct.materialId"
          class="form-select shadow-sm"
          :class="{ 'is-invalid': errors.materialId }"
        >
          <option :value="null" disabled>-- Chọn --</option>
          <option v-for="mt in materialList" :key="mt.id" :value="mt.id">
            {{ mt.materialName }}
          </option>
        </select>
        <div v-if="errors.materialId" class="text-danger">{{ errors.materialId }}</div>
      </div>

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
        <div v-if="errors.genderId" class="text-danger mt-2">{{ errors.genderId }}</div>
      </div>

      <div class="mb-3">
        <label for="supplier" class="form-label text-primary">Chọn nhà cung cấp</label>
        <select
          v-model="newProduct.supplierId"
          class="form-select shadow-sm"
          :class="{ 'is-invalid': errors.supplierId }"
        >
          <option :value="null" disabled>-- Chọn --</option>
          <option v-for="sp in supplierList" :key="sp.id" :value="sp.id">
            {{ sp.supplierName }}
          </option>
        </select>
        <div v-if="errors.supplierId" class="text-danger">{{ errors.supplierId }}</div>
      </div>

      <div class="mb-3">
        <label for="sole" class="form-label text-primary">Chọn loại đế</label>
        <select
          v-model="newProduct.soleId"
          class="form-select shadow-sm"
          :class="{ 'is-invalid': errors.soleId }"
        >
          <option :value="null" disabled>-- Chọn --</option>
          <option v-for="sp in soleList" :key="sp.id" :value="sp.id">
            {{ sp.soleName }}
          </option>
        </select>
        <div v-if="errors.soleId" class="text-danger">{{ errors.soleId }}</div>
      </div>

      <div class="mb-3">
        <label for="style" class="form-label text-primary">Chọn cổ giày</label>
        <select
          v-model="newProduct.styleId"
          class="form-select shadow-sm"
          :class="{ 'is-invalid': errors.styleId }"
        >
          <option :value="null" disabled>-- Chọn --</option>
          <option v-for="sp in styleList" :key="sp.id" :value="sp.id">
            {{ sp.styleName }}
          </option>
        </select>
        <div v-if="errors.styleId" class="text-danger">{{ errors.styleId }}</div>
      </div>

      <div class="mb-3">
        <label for="brand" class="form-label text-primary">Chọn thương hiệu</label>
        <select
          v-model="newProduct.brandId"
          class="form-select shadow-sm"
          :class="{ 'is-invalid': errors.brandId }"
        >
          <option :value="null" disabled>-- Chọn --</option>
          <option v-for="br in brandList" :key="br.id" :value="br.id">
            {{ br.brandName }}
          </option>
        </select>
        <div v-if="errors.brandId" class="text-danger">{{ errors.brandId }}</div>
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
          :class="{ 'is-invalid': errors.weight }"
        />
        <div v-if="errors.weight" class="text-danger">{{ errors.weight }}</div>
      </div>

      <div class="mb-3">
        <label for="sellPrice" class="form-label text-primary">Giá bán</label>
        <input
          type="number"
          id="sellPrice"
          placeholder="Nhập giá bán sản phẩm"
          v-model="newProduct.sellPrice"
          class="form-control shadow-sm"
          :class="{ 'is-invalid': errors.sellPrice }"
        />
        <div v-if="errors.sellPrice" class="text-danger">{{ errors.sellPrice }}</div>
      </div>

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

      <div class="mb-3">
        <label class="form-label text-primary">Kích thước</label>
        <div
          v-for="size in sizeList"
          :key="size.id"
          class="form-check form-check-inline"
        >
          <input
            type="checkbox"
            class="form-check-input"
            :value="size.id"
            v-model="selectedSizes"
            @change="generateProductDetails"
          />
          <label class="form-check-label">{{ size.sizeName }}</label>
        </div>
        <div v-if="errors.selectedSizes" class="text-danger">{{ errors.selectedSizes }}</div>
      </div>

      <div class="mb-3">
        <label class="form-label text-primary">Màu sắc</label>
        <div
          v-for="color in colorList"
          :key="color.id"
          class="form-check form-check-inline"
        >
          <input
            type="checkbox"
            class="form-check-input"
            :value="color.id"
            v-model="selectedColors"
            @change="generateProductDetails"
          />
          <label class="form-check-label">{{ color.colorName }}</label>
        </div>
        <div v-if="errors.selectedColors" class="text-danger">{{ errors.selectedColors }}</div>
      </div>

      <div v-if="selectedColors.length > 0" class="mb-3">
        <label class="form-label text-primary">Hình ảnh theo màu sắc</label>
        <div v-for="colorId in selectedColors" :key="colorId" class="card mb-3 shadow-sm">
          <div class="card-header bg-info text-white">
            <strong>Hình ảnh cho màu: {{ getColorName(colorId) }}</strong>
          </div>
          <div class="card-body">
            <el-upload
              class="upload-demo"
              action="#"
              :on-preview="handlePreview"
              :on-change="(file, fileList) => handleFileChange(file, fileList, colorId)"
              :on-remove="(file, fileList) => handleFileRemove(file, fileList, colorId)"
              list-type="picture-card"
              :limit="10"
              :file-list="colorImages[colorId] || []"
              :auto-upload="false"
              multiple
              accept="image/*"
            >
              <el-button type="primary">Tải lên hình ảnh</el-button>
            </el-upload>
            <div v-if="errors[`colorImage_${colorId}`]" class="text-danger mt-2">
              {{ errors[`colorImage_${colorId}`] }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="productDetails.length > 0">
        <div v-if="errors.productDetails" class="text-danger mb-3">{{ errors.productDetails }}</div>
        <div
          v-for="(detail, index) in productDetails"
          :key="index"
          class="card mb-4 shadow-sm"
        >
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
              <input
                type="number"
                v-model="detail.sellPrice"
                class="form-control"
                required
                :class="{ 'is-invalid': errors[`productDetail_${index}_sellPrice`] }"
              />
              <div v-if="errors[`productDetail_${index}_sellPrice`]" class="text-danger">
                {{ errors[`productDetail_${index}_sellPrice`] }}
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">Số Lượng</label>
              <input
                type="number"
                v-model="detail.quantity"
                class="form-control"
                required
                :class="{ 'is-invalid': errors[`productDetail_${index}_quantity`] }"
              />
              <div v-if="errors[`productDetail_${index}_quantity`]" class="text-danger">
                {{ errors[`productDetail_${index}_quantity`] }}
              </div>
            </div>
          </div>
        </div>
      </div>


      <div class="text-center">
        <button type="submit" class="btn btn-success btn-lg shadow-sm">Thêm sản phẩm</button>
      </div>
    </form>

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
import { ref, onMounted, watch, computed } from 'vue'
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
const colorImages = ref({}) // Store images per colorId
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
  originPrice: null, // This is not used in the form's inputs, but is in the formData
  weight: null,
  sellPrice: null,
  description: '',
})

// Validation error messages
const errors = ref({})

// Computed property to check if the form is valid
const isFormValid = computed(() => {
  // Reset errors before re-evaluating
  errors.value = {}

  // Basic required fields
  if (!newProduct.value.productName) errors.value.productName = 'Tên sản phẩm không được để trống.'
  if (!newProduct.value.materialId) errors.value.materialId = 'Vui lòng chọn chất liệu.'
  if (!newProduct.value.genderId) errors.value.genderId = 'Vui lòng chọn đối tượng dành cho.'
  if (!newProduct.value.supplierId) errors.value.supplierId = 'Vui lòng chọn nhà cung cấp.'
  if (!newProduct.value.soleId) errors.value.soleId = 'Vui lòng chọn loại đế.'
  if (!newProduct.value.styleId) errors.value.styleId = 'Vui lòng chọn cổ giày.'
  if (!newProduct.value.brandId) errors.value.brandId = 'Vui lòng chọn thương hiệu.'

  // Category selection
  if (newProduct.value.categoryIds.length === 0) errors.value.categoryIds = 'Vui lòng chọn ít nhất một danh mục.'

  // Numeric validations
  if (newProduct.value.weight === null || newProduct.value.weight < 0) errors.value.weight = 'Cân nặng phải là số dương.'
  if (newProduct.value.sellPrice === null || newProduct.value.sellPrice <= 0) errors.value.sellPrice = 'Giá bán phải lớn hơn 0.'

  // Size and Color selection
  if (selectedSizes.value.length === 0) errors.value.selectedSizes = 'Vui lòng chọn ít nhất một kích thước.'
  if (selectedColors.value.length === 0) errors.value.selectedColors = 'Vui lòng chọn ít nhất một màu sắc.'

  // Product Details validation (generated combinations of size and color)
  if (selectedSizes.value.length > 0 && selectedColors.value.length > 0 && productDetails.value.length === 0) {
    errors.value.productDetails = 'Không có chi tiết sản phẩm nào được tạo. Vui lòng chọn kích thước và màu sắc hợp lệ.'
  } else {
    productDetails.value.forEach((detail, index) => {
      if (detail.sellPrice === null || detail.sellPrice <= 0) {
        errors.value[`productDetail_${index}_sellPrice`] = `Giá bán của chi tiết sản phẩm ${index + 1} phải lớn hơn 0.`
      }
      if (detail.quantity === null || detail.quantity < 0) {
        errors.value[`productDetail_${index}_quantity`] = `Số lượng của chi tiết sản phẩm ${index + 1} phải là số dương.`
      }
    })
  }

  // Image validation for each selected color
  for (const colorId of selectedColors.value) {
    if (!colorImages.value[colorId] || colorImages.value[colorId].length === 0) {
      errors.value[`colorImage_${colorId}`] = `Vui lòng tải lên ít nhất một ảnh cho màu ${getColorName(colorId)}.`
    }
  }

  return Object.keys(errors.value).length === 0
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
  if (!isFormValid.value) {
    showError('Vui lòng điền đầy đủ và chính xác tất cả các trường bắt buộc.')
    // Scroll to the first error if needed
    setTimeout(() => {
      const firstErrorElement = document.querySelector('.text-danger')
      if (firstErrorElement) {
        firstErrorElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    }, 100); // Small delay to allow DOM to update
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

// Lấy chất liệu
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

// Lấy thương hiệu
const fetchBrand = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/brand/hien-thi')
    brandList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy thương hiệu:', error)
    showError('Lỗi lấy thương hiệu sản phẩm!')
  }
}

// Lấy đế giày
const fetchSole = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/sole/hien-thi')
    soleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy đế giày:', error)
    showError('Lỗi lấy đế giày sản phẩm!')
  }
}

// Lấy phong cách
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

// Lấy tên màu từ colorId
const getColorName = (colorId) => {
  const color = colorList.value.find((c) => c.id === colorId)
  return color?.colorName || 'Không xác định'
}

// Tạo chi tiết sản phẩm
const generateProductDetails = () => {
  const newDetails = []
  for (const sizeId of selectedSizes.value) {
    for (const colorId of selectedColors.value) {
      const size = sizeList.value.find((s) => s.id === sizeId)
      const color = colorList.value.find((c) => c.id === colorId)

      // Check if this detail combination already exists
      const existingDetail = productDetails.value.find(
        (d) => d.sizeId === sizeId && d.colorId === colorId
      )

      if (existingDetail) {
        // If it exists, keep its current sellPrice and quantity
        newDetails.push(existingDetail)
      } else {
        // Otherwise, create a new one with default values
        newDetails.push({
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
  // Sort the newDetails to maintain consistent order
  productDetails.value = newDetails.sort((a, b) => {
    if (a.sizeId === b.sizeId) {
      return a.colorId - b.colorId;
    }
    return a.sizeId - b.sizeId;
  });
}

// Quay lại trang trước
const goBack = () => {
  router.push('/product')
}

// Xử lý khi chọn file trong el-upload cho màu cụ thể
const handleFileChange = (file, fileList, colorId) => {
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    showError(`Ảnh ${file.name} vượt quá kích thước tối đa (5MB)!`)
    fileList.splice(fileList.indexOf(file), 1)
    return
  }

  const isDuplicate = (colorImages.value[colorId] || []).some(
    (f) => f.name === file.name && f.file?.size === file.size
  )
  if (isDuplicate) {
    showError(`Ảnh ${file.name} đã được chọn cho màu này!`)
    fileList.splice(fileList.indexOf(file), 1)
    return
  }

  // Create new image object
  const newFile = {
    name: file.name,
    url: file.url || (file.raw ? URL.createObjectURL(file.raw) : ''),
    file: file.raw || null,
    uid: file.uid // Element Plus uses uid for internal tracking
  }

  // Update image list for the specific color
  if (!colorImages.value[colorId]) {
    colorImages.value[colorId] = []
  }
  colorImages.value[colorId].push(newFile)
  colorImages.value = { ...colorImages.value } // Trigger reactivity

  // Clear any previous image validation error for this color
  if (errors.value[`colorImage_${colorId}`]) {
    delete errors.value[`colorImage_${colorId}`]
  }

  console.log(`selectedFiles for color ${getColorName(colorId)}:`, colorImages.value[colorId].map((f) => ({
    name: f.name,
    size: f.file?.size
  })))
}

// Xử lý khi xóa file cho màu cụ thể
const handleFileRemove = (file, fileList, colorId) => {
  console.log(`File removed for color ${getColorName(colorId)}:`, { name: file.name, totalFiles: fileList.length })
  colorImages.value[colorId] = fileList.map((item) => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null,
    uid: item.uid
  }))
  colorImages.value = { ...colorImages.value } // Trigger reactivity
  console.log(`selectedFiles after removal for color ${getColorName(colorId)}:`, colorImages.value[colorId].map((f) => ({
    name: f.name,
    size: f.file?.size
  })))
}

// Xử lý xem trước ảnh
const handlePreview = (file) => {
  console.log('Preview:', file.name)
  window.open(file.url, '_blank')
}

// Lưu sản phẩm
const saveProduct = async () => {
  try {
    console.log('colorImages:', Object.entries(colorImages.value).map(([colorId, files]) => ({
      color: getColorName(colorId),
      files: files.map((f) => ({ name: f.name, size: f.file?.size }))
    })))
    console.log('productDetails:', productDetails.value)

    const totalQuantity = productDetails.value.reduce((sum, detail) => sum + Number(detail.quantity), 0)
    if (totalQuantity <= 0) {
      showError('Tổng số lượng của các biến thể sản phẩm phải lớn hơn 0!')
      isModalVisible.value = false
      return
    }

    // This check is already part of isFormValid, but kept for redundancy before API call
    for (const colorId of selectedColors.value) {
      if (!colorImages.value[colorId] || colorImages.value[colorId].length === 0) {
        showError(`Vui lòng chọn ít nhất một ảnh cho màu ${getColorName(colorId)}!`)
        isModalVisible.value = false
        return
      }
    }

    const formData = new FormData()
    formData.append('productName', newProduct.value.productName || '')
    formData.append('materialId', newProduct.value.materialId || '')
    formData.append('supplierId', newProduct.value.supplierId || '')
    formData.append('brandId', newProduct.value.brandId || '')
    formData.append('soleId', newProduct.value.soleId || '')
    formData.append('styleId', newProduct.value.styleId || '')
    formData.append('genderId', newProduct.value.genderId || '')
    formData.append('weight', newProduct.value.weight || 0)
    formData.append('originPrice', newProduct.value.originPrice || 0) // Ensure this is handled correctly if it's always 0 or derived
    formData.append('sellPrice', newProduct.value.sellPrice || 0)
    formData.append('quantity', totalQuantity)
    formData.append('description', newProduct.value.description || '')

    // Append categoryIds
    const categories = Array.isArray(newProduct.value.categoryIds)
      ? newProduct.value.categoryIds
      : []
    categories.forEach((cat, index) => {
      const categoryId = typeof cat === 'object' && cat !== null ? cat.id : cat
      formData.append(`categoryIds[${index}]`, categoryId)
    })

    // Append productDetails
    productDetails.value.forEach((detail, index) => {
      formData.append(`productDetails[${index}].sizeId`, detail.sizeId)
      formData.append(`productDetails[${index}].colorId`, detail.colorId)
      formData.append(`productDetails[${index}].sellPrice`, detail.sellPrice)
      formData.append(`productDetails[${index}].quantity`, detail.quantity)
    })

    // Append productImages with colorId
    let imageIndex = 0
    Object.entries(colorImages.value).forEach(([colorId, files]) => {
      files
        .filter((fileObj) => fileObj.file) // Only append files that actually exist (not just URL placeholders)
        .forEach((fileObj) => {
          formData.append(`productImages[${imageIndex}].productImages`, fileObj.file)
          formData.append(`productImages[${imageIndex}].colorId`, colorId)
          imageIndex++
        })
    })

    // Log FormData contents for debugging
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
    colorImages.value = {}
    productDetails.value = []
    selectedSizes.value = []
    selectedColors.value = []
    errors.value = {} // Clear errors on successful submission


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

// Xóa ảnh của màu khi màu bị bỏ chọn
watch(selectedColors, (newColors, oldColors) => {
  const removedColors = oldColors.filter((colorId) => !newColors.includes(colorId))
  removedColors.forEach((colorId) => {
    delete colorImages.value[colorId]
    colorImages.value = { ...colorImages.value } // Trigger reactivity
    // Also remove the error for this color if it exists
    if (errors.value[`colorImage_${colorId}`]) {
      delete errors.value[`colorImage_${colorId}`]
    }
  })
  generateProductDetails() // Recalculate product details based on new selections
})

// Watch selectedSizes to regenerate product details
watch(selectedSizes, () => {
  generateProductDetails()
})

// Watch newProduct.sellPrice to update productDetails sellPrice
watch(() => newProduct.value.sellPrice, (newSellPrice) => {
  if (newSellPrice !== null) {
    productDetails.value.forEach(detail => {
      detail.sellPrice = Number(newSellPrice);
    });
  }
});


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

/* Custom style for invalid fields from Bootstrap */
.is-invalid {
  border-color: #dc3545 !important;
}

.text-danger {
  color: #dc3545 !important;
  font-size: 0.875em; /* Smaller font for error messages */
  margin-top: 0.25rem;
}
</style>

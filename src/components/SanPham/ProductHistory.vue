<template>
  <div class="p-4">
    <el-card shadow="hover" class="card-container">
      <div class="d-flex justify-between align-center mb-3">
        <h2 class="title">Danh sách sản phẩm đã xóa</h2>
        <el-button type="info" @click="goBack" round>
          <el-icon><Back /></el-icon> Quay lại
        </el-button>
      </div>

      <el-table
        :data="products"
        style="width: 100%"
        border
        stripe
        v-loading="loading"
        empty-text="Không có sản phẩm đã xóa nào."
      >
        <el-table-column type="index" label="STT" width="60" :index="indexMethod" />
        <el-table-column prop="productCode" label="Mã SP" width="120" sortable />
        <el-table-column prop="productName" label="Tên SP" sortable />
        <el-table-column prop="brandName" label="Thương hiệu" width="150" />
        <el-table-column prop="materialName" label="Chất liệu" width="150" />
        <el-table-column label="Giá bán" width="120">
          <template #default="scope">
            {{ formatPrice(scope.row.sellPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="Số lượng" width="100" />
        <el-table-column label="Ngày tạo" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdDate) }}
          </template>
        </el-table-column>
        <el-table-column label="Danh mục" width="180">
          <template #default="scope">
            <div class="category-tags-container">
              <el-tag
                v-for="(cat, idx) in scope.row.categoryNames"
                :key="idx"
                size="small"
                class="mr-1 mb-1"
                type="info"
              >
                {{ cat }}
              </el-tag>
              <span v-if="!scope.row.categoryNames || scope.row.categoryNames.length === 0"
                >N/A</span
              >
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Trạng thái" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">
              {{ scope.row.status === 0 ? 'Đã xóa' : 'Hoạt động' }}
            </el-tag>
          </template>
          </el-table-column>
        <el-table-column label="Hành động" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openRestoreDialog(scope.row)">
              Khôi phục
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="d-flex justify-end mt-4">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="page + 1"
          :page-sizes="[5, 8, 10, 20, 50]"
          :page-size="size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalElements"
          background
        />
      </div>
    </el-card>

    <el-dialog
      title="Khôi phục và Cập nhật Sản Phẩm"
      v-model="isRestoreDialogVisible"
      width="80%"
      :before-close="handleCloseDialog"
      center
      class="elegant-dialog"
    >
      <div class="dialog-content">
        <el-form
          :model="updateProduct"
          ref="productForm"
          class="form-container"
          @submit.prevent="openConfirmDialog"
          label-position="top"
        >
          <h3 class="section-title">Thông tin cơ bản</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Tên sản phẩm" prop="productName">
                <el-input v-model="updateProduct.productName" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Danh mục sản phẩm" prop="categoryNames">
                <el-input
                  :value="updateProduct.categoryNames?.join(', ') || 'Chưa chọn'"
                  disabled
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Chất liệu" prop="materialName">
                <el-input :value="updateProduct.materialName || ''" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Dành cho" prop="genderId">
                <el-input :value="getGenderName(updateProduct.genderId)" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Nhà cung cấp" prop="supplierName">
                <el-input :value="updateProduct.supplierName || ''" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Loại đế" prop="soleName">
                <el-input :value="updateProduct.soleName || ''" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Cổ giày" prop="styleName">
                <el-input :value="updateProduct.styleName || ''" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Thương hiệu" prop="brandName">
                <el-input :value="updateProduct.brandName || ''" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Cân nặng (gram)" prop="weight">
                <el-input :value="updateProduct.weight || 0" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Giá bán buôn" prop="originPrice">
                <el-input :value="formatPrice(updateProduct.originPrice) || '0 ₫'" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Giá bán lẻ" prop="sellPrice">
                <el-input :value="formatPrice(updateProduct.sellPrice) || '0 ₫'" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="Ghi chú sản phẩm" prop="description">
                <el-input
                  type="textarea"
                  :value="updateProduct.description || ''"
                  :rows="3"
                  disabled
                />
              </el-form-item>
            </el-col>
          </el-row>

          <h3 class="section-title">Thông tin chi tiết</h3>
          <el-form-item label="Kích thước" prop="selectedSizes">
            <el-checkbox-group v-model="selectedSizes" @change="generateProductDetails">
              <el-checkbox
                v-for="size in sizeList"
                :key="size.id"
                :label="size.id"
                class="checkbox-item"
              >
                {{ size.sizeName }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="Màu sắc" prop="selectedColors">
            <el-checkbox-group v-model="selectedColors" @change="generateProductDetails">
              <el-checkbox
                v-for="color in colorList"
                :key="color.id"
                :label="color.id"
                class="checkbox-item"
              >
                {{ color.colorName }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <div v-for="(detail, index) in productDetails" :key="index" class="detail-card">
            <div class="card-header">
              <strong>Chi Tiết Sản Phẩm {{ index + 1 }}</strong>
            </div>
            <div class="card-body">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="Kích Thước">
                    <el-input :value="detail.sizeName" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="Màu Sắc">
                    <el-input :value="detail.colorName" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item
                    label="Số Lượng"
                    :prop="'productDetails.' + index + '.quantity'"
                    :rules="[
                      { required: true, message: 'Số lượng là bắt buộc', trigger: 'blur' },
                      {
                        type: 'number',
                        min: 0,
                        message: 'Số lượng phải lớn hơn hoặc bằng 0',
                        trigger: 'blur',
                      },
                    ]"
                  >
                    <el-input-number v-model="detail.quantity" :min="0" class="w-full" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </div>

          <el-form-item label="Hình ảnh sản phẩm">
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
              <el-button type="primary" round>Tải lên hình ảnh</el-button>
            </el-upload>
          </el-form-item>

          <div class="form-actions">
            <el-button type="success" native-type="submit" size="large" round>
              Khôi phục và Cập nhật
            </el-button>
          </div>
        </el-form>
      </div>
    </el-dialog>

    <el-dialog
      title="Xác nhận"
      v-model="isConfirmDialogVisible"
      width="30%"
      center
      class="confirm-dialog"
    >
      <p>Bạn có chắc chắn muốn khôi phục và cập nhật sản phẩm này không?</p>
      <template #footer>
        <el-button type="success" @click="saveProduct" round>Xác nhận</el-button>
        <el-button type="danger" @click="isConfirmDialogVisible = false" round>Hủy</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import {
  ElMessage,
  ElMessageBox,
  ElTable,
  ElTableColumn,
  ElPagination,
  ElButton,
  ElCard,
  ElTag,
  ElIcon,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElCheckboxGroup,
  ElCheckbox,
  ElUpload,
  ElDialog,
  ElRow,
  ElCol,
  // ElRadioGroup, // Không sử dụng trực tiếp nếu chỉ dùng để hiển thị text
  // ElRadio, // Không sử dụng trực tiếp nếu chỉ dùng để hiển thị text
} from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Helper function để hiển thị tên giới tính từ ID
function getGenderName(id) {
  return {
    1: 'Nam',
    2: 'Nữ',
    3: 'Cả nam và nữ'
  }[id] || ''
}

// State Variables
const products = ref([])
const page = ref(0)
const size = ref(8)
const totalElements = ref(0)
const loading = ref(false)
const isRestoreDialogVisible = ref(false)
const isConfirmDialogVisible = ref(false)

// Lists for dropdowns/checkboxes in the form (fetched from API)
const brandList = ref([])
const materialList = ref([])
const categoryList = ref([]) // Dùng cho checkbox group nếu cần lựa chọn lại danh mục
const sizeList = ref([])
const colorList = ref([])
const soleList = ref([])
const supplierList = ref([])
const styleList = ref([])

const deletedImageIds = ref([]) // Store IDs of images marked for deletion
const selectedFiles = ref([]) // Files currently selected for upload/display
const selectedSizes = ref([]) // IDs of selected sizes for product details
const selectedColors = ref([]) // IDs of selected colors for product details
const productDetails = ref([]) // Array of product detail objects (size, color, quantity)
const currentProductId = ref(null) // ID of the product being restored/updated

// Form model for the product to be updated
const updateProduct = ref({
  productId: null, // Add productId here to explicitly store it
  categoryIds: [],
  categoryNames: [], // This will directly hold the List<String> from the API
  productName: '',
  materialId: null,
  materialName: '',
  styleId: null,
  styleName: '',
  supplierId: null,
  supplierName: '',
  genderId: null,
  soleId: null,
  soleName: '',
  brandId: null,
  brandName: '',
  originPrice: null,
  weight: null,
  sellPrice: null,
  description: '',
})

// Navigation Function
const goBack = () => {
  router.push('/product')
}

// --- API Data Fetching Functions ---

const fetchData = async () => {
  loading.value = true
  try {
    const requestBody = {
      page: page.value,
      size: size.value,
    }
    const response = await axios.post(
      'http://localhost:8080/api/admin/products/inactive',
      requestBody,
      {
        headers: { 'Content-Type': 'application/json' },
      },
    )
    products.value = response.data.data
    totalElements.value = response.data.pagination.totalElements
  } catch (error) {
    console.error('Lỗi khi tải danh sách sản phẩm đã xóa:', error)
    ElMessage.error('Không thể tải danh sách sản phẩm đã xóa. Vui lòng thử lại.')
  } finally {
    loading.value = false
  }
}

// Fetch lists for form selections
const fetchCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/categories/hien-thi')
    categoryList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy danh mục:', error)
    ElMessage.error('Lỗi lấy danh mục sản phẩm!')
  }
}

const fetchMaterial = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/material/hien-thi')
    materialList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy chất liệu:', error)
    ElMessage.error('Lỗi lấy chất liệu sản phẩm!')
  }
}

const fetchSupplier = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/supplier/hien-thi')
    supplierList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy nhà cung cấp:', error)
    ElMessage.error('Lỗi lấy nhà cung cấp!')
  }
}

const fetchBrand = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/brand/hien-thi')
    brandList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy thương hiệu:', error)
    ElMessage.error('Lỗi lấy thương hiệu sản phẩm!')
  }
}

const fetchSole = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/sole/hien-thi')
    soleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy đế giày:', error)
    ElMessage.error('Lỗi lấy đế giày sản phẩm!')
  }
}

const fetchStyle = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/style/hien-thi')
    styleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy phong cách:', error)
    ElMessage.error('Lỗi lấy phong cách sản phẩm!')
  }
}

const fetchSizesAndColors = async () => {
  try {
    const [sizesResponse, colorsResponse] = await Promise.all([
      axios.get('http://localhost:8080/api/size/hien-thi'),
      axios.get('http://localhost:8080/api/color/hien-thi'),
    ])
    sizeList.value = sizesResponse.data
    colorList.value = colorsResponse.data
  } catch (error) {
    console.error('Lỗi lấy kích thước hoặc màu sắc:', error)
    ElMessage.error('Lỗi lấy kích thước hoặc màu sắc!')
  }
}

// Fetch specific product data for the restore dialog
const fetchProduct = async (productId) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/admin/products/${productId}`)
    const product = response.data

    // Map fetched data to the updateProduct form model
    updateProduct.value = {
      productId: product.id, // Store the product ID
      // Assuming API now directly returns categoryNames as List<String>
      categoryNames: product.categoryNames || [],
      // If your API also returns categoryIds as List<Integer>, map it here too:
      categoryIds: product.categoryIds || [],

      productName: product.productName || '',
      materialId: product.materialId || null,
      materialName: product.materialName || '',
      styleId: product.styleId || null,
      styleName: product.styleName || '',
      supplierId: product.supplierId || null,
      supplierName: product.supplierName || '',
      genderId: product.genderId || null,
      soleId: product.soleId || null,
      soleName: product.soleName || '',
      brandId: product.brandId || null,
      brandName: product.brandName || '',
      originPrice: product.originPrice || null,
      weight: product.weight || null,
      sellPrice: product.sellPrice || null,
      description: product.description || '',
    }

    // Process product details (sizes, colors, quantities)
    productDetails.value =
      product.productDetails?.map((detail) => {
        const size = sizeList.value.find((s) => s.id === detail.sizeId)
        const color = colorList.value.find((c) => c.id === detail.colorId)
        return {
          sizeId: detail.sizeId,
          colorId: detail.colorId,
          sizeName: size?.sizeName || '',
          colorName: color?.colorName || '',
          sellPrice: detail.sellPrice,
          quantity: detail.quantity,
          id: detail.id, // Keep existing detail ID
        }
      }) || []

    // Pre-select checkboxes based on existing product details
    selectedSizes.value = [...new Set(productDetails.value.map((detail) => detail.sizeId))]
    selectedColors.value = [...new Set(productDetails.value.map((detail) => detail.colorId))]

    // Populate selectedFiles for image display
    selectedFiles.value =
      product.productImages?.map((img, index) => ({
        name: `image-${index + 1}`,
        url: `data:image/png;base64,${img.image}`, // Assume base64 image data
        isOld: true, // Mark as an existing image
        id: img.id, // Store image ID for deletion tracking
        file: null, // No raw file for old images
      })) || []

    deletedImageIds.value = [] // Reset deleted image IDs when opening dialog
  } catch (error) {
    console.error('Lỗi lấy dữ liệu sản phẩm để khôi phục:', error)
    ElMessage.error('Lỗi lấy dữ liệu sản phẩm để khôi phục!')
  }
}

// --- Dialog Management ---

const openRestoreDialog = async (product) => {
  currentProductId.value = product.id
  // Fetch all necessary supporting data first
  await Promise.all([
    fetchCategories(),
    fetchMaterial(),
    fetchSupplier(),
    fetchBrand(),
    fetchSole(),
    fetchStyle(),
    fetchSizesAndColors(),
  ]);
  // Then fetch the specific product data
  await fetchProduct(product.id)
  isRestoreDialogVisible.value = true
}

const handleCloseDialog = (done) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn đóng biểu mẫu này không?', 'Xác nhận', {
    confirmButtonText: 'Đóng',
    cancelButtonText: 'Hủy',
    type: 'warning',
  })
    .then(() => {
      resetForm()
      done()
    })
    .catch(() => {
      // User cancelled closing the dialog
    })
}

// Resets the form model and related state
const resetForm = () => {
  updateProduct.value = {
    productId: null,
    categoryIds: [],
    categoryNames: [],
    productName: '',
    materialId: null,
    materialName: '',
    styleId: null,
    styleName: '',
    supplierId: null,
    supplierName: '',
    genderId: null,
    soleId: null,
    soleName: '',
    brandId: null,
    brandName: '',
    originPrice: null,
    weight: null,
    sellPrice: null,
    description: '',
  }
  productDetails.value = []
  selectedSizes.value = []
  selectedColors.value = []
  selectedFiles.value = []
  deletedImageIds.value = []
  currentProductId.value = null
}

const openConfirmDialog = () => {
  // You might want to add form validation here before opening the confirm dialog
  isConfirmDialogVisible.value = true
}

// --- Product Details Logic ---

const generateProductDetails = () => {
  const newProductDetails = [];
  const existingDetailsMap = new Map();

  // Populate map with existing details for easy lookup by sizeId-colorId
  productDetails.value.forEach(detail => {
    existingDetailsMap.set(`${detail.sizeId}-${detail.colorId}`, detail);
  });

  // Generate new product details based on selected sizes and colors
  for (const sizeId of selectedSizes.value) {
    for (const colorId of selectedColors.value) {
      const size = sizeList.value.find((s) => s.id === sizeId);
      const color = colorList.value.find((c) => c.id === colorId);
      const key = `${sizeId}-${colorId}`;
      const existingDetail = existingDetailsMap.get(key); // Get existing detail if available

      newProductDetails.push({
        sizeId: sizeId,
        colorId: colorId,
        sizeName: size?.sizeName || '',
        colorName: color?.colorName || '',
        // Keep existing sellPrice and quantity if detail existed, otherwise default
        sellPrice: existingDetail?.sellPrice || updateProduct.value.sellPrice || 0,
        quantity: existingDetail?.quantity || 0,
        id: existingDetail?.id || null, // Preserve ID for existing details
      });
    }
  }
  productDetails.value = newProductDetails;
}


// --- Image Upload/Management Handlers ---

const handleFileChange = (file, fileList) => {
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    ElMessage.error(`Ảnh ${file.name} vượt quá kích thước tối đa (5MB)!`)
    // Remove the oversized file from the list
    fileList.splice(fileList.indexOf(file), 1)
    selectedFiles.value = fileList.map((item) => ({
      name: item.name,
      url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
      file: item.raw || null,
      isOld: item.isOld || false,
      id: item.id || null,
    }))
    return
  }

  // Check for duplicate files (by name and size, for new files)
  const isDuplicate = selectedFiles.value.some(
    (f) => f.name === file.name && f.file?.size === file.size && !f.isOld,
  )
  if (isDuplicate) {
    ElMessage.error(`Ảnh ${file.name} đã được chọn!`)
    fileList.splice(fileList.indexOf(file), 1)
    selectedFiles.value = fileList.map((item) => ({
      name: item.name,
      url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
      file: item.raw || null,
      isOld: item.isOld || false,
      id: item.id || null,
    }))
    return
  }

  // Update selectedFiles with new and existing files
  selectedFiles.value = fileList.map((item) => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null, // raw file is available for newly added files
    isOld: item.isOld || false,
    id: item.id || null, // ID for old images
  }))
}

const handleFileRemove = (file, fileList) => {
  // If an old image is removed, add its ID to deletedImageIds
  if (file.isOld && file.id) {
    if (!deletedImageIds.value.includes(file.id)) {
      deletedImageIds.value.push(file.id)
    }
  }
  // Update the remaining selected files
  selectedFiles.value = fileList.map((item) => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null,
    isOld: item.isOld || false,
    id: item.id || null,
  }))
}

const handlePreview = (file) => {
  // Open image in a new tab for preview
  window.open(file.url, '_blank')
}

// --- Save Product Function ---

const saveProduct = async () => {
  try {
    // Calculate total quantity from product details
    const totalQuantity = productDetails.value.reduce((sum, detail) => sum + Number(detail.quantity), 0)

    if (totalQuantity <= 0) {
      ElMessage.error('Tổng số lượng của các biến thể sản phẩm phải lớn hơn 0!')
      isConfirmDialogVisible.value = false
      return
    }

    const formData = new FormData()
    // Append basic product information
    formData.append('productName', updateProduct.value.productName || '')
    formData.append('materialId', updateProduct.value.materialId || '')
    formData.append('supplierId', updateProduct.value.supplierId || '')
    formData.append('brandId', updateProduct.value.brandId || '')
    formData.append('soleId', updateProduct.value.soleId || '')
    formData.append('styleId', updateProduct.value.styleId || '')
    formData.append('genderId', updateProduct.value.genderId || '')
    formData.append('weight', updateProduct.value.weight || 0)
    formData.append('originPrice', updateProduct.value.originPrice || 0)
    formData.append('sellPrice', updateProduct.value.sellPrice || 0)
    formData.append('quantity', totalQuantity) // Total quantity for the product
    formData.append('description', updateProduct.value.description || '')

    // Append category IDs (assuming backend expects them as a list of integers)
    // You need to make sure updateProduct.value.categoryIds is populated correctly
    // If your backend now only expects categoryNames for display, you might adjust this
    updateProduct.value.categoryIds.forEach((catId, index) => {
      formData.append(`categoryIds[${index}]`, catId)
    })

    // Append product details (variants)
    productDetails.value.forEach((detail, index) => {
      if (detail.id) { // Include ID if it's an existing detail
        formData.append(`productDetails[${index}].id`, detail.id)
      }
      formData.append(`productDetails[${index}].sizeId`, detail.sizeId)
      formData.append(`productDetails[${index}].colorId`, detail.colorId)
      formData.append(`productDetails[${index}].sellPrice`, detail.sellPrice)
      formData.append(`productDetails[${index}].quantity`, detail.quantity)
    })

    // Append IDs of images to be deleted
    if (deletedImageIds.value.length > 0) {
      deletedImageIds.value.forEach((id, index) => {
        formData.append(`oldImageIds[${index}]`, id)
      })
    }

    // Append new image files
    const newImages = selectedFiles.value.filter((fileObj) => fileObj.file && !fileObj.isOld)
    newImages.forEach((fileObj) => {
      formData.append('productImages', fileObj.file)
    })

    // Send the PUT request to restore and update the product
    await axios.put(
      `http://localhost:8080/api/admin/products/restore/${currentProductId.value}`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' }, // Important for FormData
      },
    )

    ElMessage.success('Khôi phục và cập nhật sản phẩm thành công!')
    isConfirmDialogVisible.value = false
    isRestoreDialogVisible.value = false
    resetForm() 
    fetchData()
  } catch (error) {
    console.error('Lỗi khi khôi phục và cập nhật sản phẩm:', error)
    const errorMessage = error.response?.data?.error || 'Đã xảy ra lỗi khi cập nhật sản phẩm.'
    ElMessage.error(errorMessage)
    isConfirmDialogVisible.value = false
  }
}

const formatPrice = (price) => {
  if (price === null || price === undefined) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('vi-VN')
}


// --- Pagination Handlers ---

const handleSizeChange = (newSize) => {
  size.value = newSize
  page.value = 0 // Reset to first page when size changes
  fetchData()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage - 1 // page is 0-indexed in backend, but 1-indexed in Element Plus pagination
  fetchData()
}

const indexMethod = (index) => {
  return page.value * size.value + index + 1
}


onMounted(() => {
  fetchData() // Fetch initial list of deleted products
  // Pre-fetch all supporting lists needed for the restore/update form
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
/* Base padding and background for the main container */
.p-4 {
  padding: 2rem;
  background-color: #f5f7fa;
}

/* Styling for the main card holding the table */
.card-container {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  background: linear-gradient(145deg, #ffffff, #f8f9fa);
}

/* Title styling */
.title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

/* Flexbox utilities */
.d-flex {
  display: flex;
}
.justify-between {
  justify-content: space-between;
}
.justify-end {
  justify-content: flex-end;
}
.align-center {
  align-items: center;
}

/* Margin utilities */
.mb-3 {
  margin-bottom: 1rem;
}
.mt-4 {
  margin-top: 1.5rem;
}
.mr-1 {
  margin-right: 0.25rem;
}
.mb-1 {
  margin-bottom: 0.25rem;
}

/* Category tags display */
.category-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px; /* Space between tags */
}
.el-tag {
  border-radius: 12px;
  font-size: 0.85rem;
}

/* Styling for the restore dialog (main dialog) */
.elegant-dialog {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden; /* Ensures border-radius applies to content */
}
/* Header of the elegant dialog */
.elegant-dialog :deep(.el-dialog__header) {
  background: linear-gradient(90deg, #3498db, #2ecc71); /* Gradient background */
  color: #fff;
  padding: 1.5rem;
  font-size: 1.25rem;
  font-weight: 500;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
}
/* Body of the elegant dialog */
.elegant-dialog :deep(.el-dialog__body) {
  padding: 2rem;
  background: #f9fafc;
}

/* Content area within the dialog, with scrollbar */
.dialog-content {
  max-height: 70vh; /* Max height to enable scrolling */
  overflow-y: auto;
  padding-right: 1rem; /* Space for scrollbar */
}
.dialog-content::-webkit-scrollbar {
  width: 8px;
}
.dialog-content::-webkit-scrollbar-thumb {
  background: #bdc3c7;
  border-radius: 4px;
}
.dialog-content::-webkit-scrollbar-track {
  background: #f1f3f5;
}

/* Styling for the form container inside the dialog */
.form-container {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

/* Section titles within the form */
.section-title {
  font-size: 1.4rem;
  font-weight: 500;
  color: #34495e;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #3498db;
  padding-bottom: 0.5rem;
}

/* Styling for individual checkbox items */
.checkbox-item {
  margin-right: 1.5rem;
  margin-bottom: 0.5rem;
}

/* Card styling for product details sections */
.detail-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
  background: #fff;
}
.card-header {
  background: linear-gradient(90deg, #3498db, #2980b9);
  color: #fff;
  padding: 1rem;
  font-size: 1.1rem;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}
.card-body {
  padding: 1.5rem;
}

/* Form action buttons container */
.form-actions {
  text-align: center;
  margin-top: 2rem;
}

/* Full width utility class */
.w-full {
  width: 100%;
}

/* Styling for the confirmation dialog */
.confirm-dialog :deep(.el-dialog__header) {
  background: #3498db;
  color: #fff;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}
.confirm-dialog :deep(.el-dialog__body) {
  font-size: 1rem;
  color: #2c3e50;
}
</style>
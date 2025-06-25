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
              <el-form-item label="Giá bán" prop="sellPrice">
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
            <el-checkbox-group v-model="selectedColors" @change="handleSelectedColorsChange">
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

          <h3 class="section-title">Hình ảnh sản phẩm</h3>
          <el-tabs v-model="activeImageTab" type="card" class="image-tabs">
            <el-tab-pane label="Tất cả ảnh" name="all">
              <el-upload
                action="#"
                :on-preview="handlePreview"
                :on-remove="handleFileRemove"
                list-type="picture-card"
                :limit="10"
                :file-list="filteredFiles(null)"
                :auto-upload="false"
                multiple
                accept="image/*"
              >
                <template #trigger>
                  <el-button type="primary" round>Tải ảnh mới</el-button>
                </template>
                <template #file="{ file }">
                  <div>
                    <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
                    <span class="el-upload-list__item-actions">
                      <span
                        class="el-upload-list__item-preview"
                        @click="handlePreview(file)"
                      >
                        <el-icon><zoom-in /></el-icon>
                      </span>
                      <span
                        class="el-upload-list__item-delete"
                        @click="handleFileRemove(file)"
                      >
                        <el-icon><Delete /></el-icon>
                      </span>
                    </span>
                    <el-select
                      v-model="file.colorId"
                      placeholder="Chọn màu"
                      size="small"
                      style="width: 100px; margin-top: 5px;"
                      class="image-color-select"
                      :disabled="file.isOld"
                    >
                      <el-option
                        v-for="color in colorList"
                        :key="color.id"
                        :label="color.colorName"
                        :value="color.id"
                      />
                    </el-select>
                  </div>
                </template>
              </el-upload>
            </el-tab-pane>

            <el-tab-pane label="Ảnh chưa gán màu" name="unassigned">
              <el-upload
                action="#"
                :on-preview="handlePreview"
                :on-change="(file, fileList) => handleFileChange(file, fileList, null)"
                :on-remove="handleFileRemove"
                list-type="picture-card"
                :limit="10"
                :file-list="filteredFiles(null)"
                :auto-upload="false"
                multiple
                accept="image/*"
              >
                <template #trigger>
                  <el-button type="primary" round>Tải ảnh mới</el-button>
                </template>
                <template #file="{ file }">
                  <div>
                    <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
                    <span class="el-upload-list__item-actions">
                      <span
                        class="el-upload-list__item-preview"
                        @click="handlePreview(file)"
                      >
                        <el-icon><zoom-in /></el-icon>
                      </span>
                      <span
                        class="el-upload-list__item-delete"
                        @click="handleFileRemove(file)"
                      >
                        <el-icon><Delete /></el-icon>
                      </span>
                    </span>
                    <el-select
                      v-model="file.colorId"
                      placeholder="Chọn màu"
                      size="small"
                      style="width: 100px; margin-top: 5px;"
                      class="image-color-select"
                    >
                      <el-option
                        v-for="color in colorList"
                        :key="color.id"
                        :label="color.colorName"
                        :value="color.id"
                      />
                    </el-select>
                  </div>
                </template>
              </el-upload>
            </el-tab-pane>

            <el-tab-pane
              v-for="colorId in selectedColors"
              :key="colorId"
              :label="getColorName(colorId)"
              :name="colorId.toString()"
            >
              <el-upload
                action="#"
                :on-preview="handlePreview"
                :on-change="(file, fileList) => handleFileChange(file, fileList, colorId)"
                :on-remove="handleFileRemove"
                list-type="picture-card"
                :limit="10"
                :file-list="filteredFiles(colorId)"
                :auto-upload="false"
                multiple
                accept="image/*"
              >
                <template #trigger>
                  <el-button type="primary" round>Tải ảnh cho {{ getColorName(colorId) }}</el-button>
                </template>
                <template #file="{ file }">
                  <div>
                    <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
                    <span class="el-upload-list__item-actions">
                      <span
                        class="el-upload-list__item-preview"
                        @click="handlePreview(file)"
                      >
                        <el-icon><zoom-in /></el-icon>
                      </span>
                      <span
                        class="el-upload-list__item-delete"
                        @click="handleFileRemove(file)"
                      >
                        <el-icon><Delete /></el-icon>
                      </span>
                    </span>
                  </div>
                </template>
              </el-upload>
            </el-tab-pane>
          </el-tabs>

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

    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
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
  ElSelect,
  ElOption,
  ElTabs,
  ElTabPane
} from 'element-plus'
import { Back, ZoomIn, Delete } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// --- Helper Functions ---
const getGenderName = (id) => {
  return {
    1: 'Nam',
    2: 'Nữ',
    3: 'Cả nam và nữ'
  }[id] || ''
}

const formatPrice = (price) => {
  if (price === null || price === undefined) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${day}/${month}/${year} ${hours}:${minutes}`;
};

const indexMethod = (index) => {
  return page.value * size.value + index + 1;
};

const getColorName = (colorId) => {
  const color = colorList.value.find(c => c.id === colorId);
  return color ? color.colorName : 'Không xác định';
};


// --- State Variables ---
const products = ref([])
const page = ref(0)
const size = ref(8)
const totalElements = ref(0)
const loading = ref(false)
const isRestoreDialogVisible = ref(false)
const isConfirmDialogVisible = ref(false)
const dialogImageUrl = ref('') // For image preview
const dialogVisible = ref(false) // For image preview dialog

// Lists for dropdowns/checkboxes in the form (fetched from API)
const brandList = ref([])
const materialList = ref([])
const categoryList = ref([])
const sizeList = ref([])
const colorList = ref([])
const soleList = ref([])
const supplierList = ref([])
const styleList = ref([])

// All files selected/existing for the product
// This array will hold both original images (with isOld: true) and new uploads (with isNew: true)
const allProductImages = ref([])

const selectedSizes = ref([])
const selectedColors = ref([]) // IDs of selected colors for product details
const activeImageTab = ref('all') // Default to 'all' tab to see all images

const productDetails = ref([])
const originalProductDetails = ref([]) // Store initial details from API
const originalProductImages = ref([]) // Store initial images from API

const currentProductId = ref(null)

const updateProduct = ref({
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
})

// --- Navigation Function ---
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
    const response = await axios.get('http://localhost:8080/api/admin/material/hien-thi')
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
    const response = await axios.get('http://localhost:8080/api/admin/brand/hien-thi')
    brandList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy thương hiệu:', error)
    ElMessage.error('Lỗi lấy thương hiệu sản phẩm!')
  }
}

const fetchSole = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/sole/hien-thi')
    soleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy đế giày:', error)
    ElMessage.error('Lỗi lấy đế giày sản phẩm!')
  }
}

const fetchStyle = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/style/hien-thi')
    styleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy phong cách:', error)
    ElMessage.error('Lỗi lấy phong cách sản phẩm!')
  }
}

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
    ElMessage.error('Lỗi lấy kích thước hoặc màu sắc!')
  }
}

// Fetch specific product data for the restore dialog
const fetchProduct = async (productId) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/admin/products/${productId}`)
    const product = response.data

    updateProduct.value = {
      productId: product.id,
      categoryNames: product.categoryNames || [],
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

    originalProductDetails.value = product.productDetails || []
    originalProductImages.value = product.productImages || []

    // Pre-select sizes and colors based on existing details
    selectedSizes.value = [...new Set(originalProductDetails.value.map((detail) => detail.sizeId))];
    selectedColors.value = [...new Set(originalProductDetails.value.map((detail) => detail.colorId))];
    
    // Generate product details for the form
    generateProductDetails();

    // Populate allProductImages with all existing images (active ones)
    // Add a unique identifier (uid) to each file for proper tracking by el-upload
    allProductImages.value =
      originalProductImages.value.filter(img => img.status === 1).map((img) => ({
        uid: img.id || Math.random().toString(36).substring(2, 11), // Use ID if available, else generate
        name: img.imageName || `image-${img.id || 'new'}`,
        url: `data:image/png;base64,${img.image}`, // Assume base64 encoded image
        isOld: true, // Mark as an existing image from DB
        isNew: false, // Not a newly uploaded file
        id: img.id, // Keep original ID
        colorId: img.colorId, // Preserve existing colorId
      })) || []

    activeImageTab.value = 'all'; // Set initial active tab to 'all'
  } catch (error) {
    console.error('Lỗi lấy dữ liệu sản phẩm để khôi phục:', error)
    ElMessage.error('Lỗi lấy dữ liệu sản phẩm để khôi phục!')
  }
}

// --- Dialog Management ---

const openRestoreDialog = async (product) => {
  currentProductId.value = product.id
  await Promise.all([
    fetchCategories(),
    fetchMaterial(),
    fetchSupplier(),
    fetchBrand(),
    fetchSole(),
    fetchStyle(),
    fetchSizesAndColors(),
  ]);
  await fetchProduct(product.id)
  isRestoreDialogVisible.value = true
}

const handleCloseDialog = (done) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn đóng biểu mẫu này không? Mọi thay đổi sẽ không được lưu.', 'Xác nhận', {
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
  originalProductDetails.value = []
  originalProductImages.value = []
  selectedSizes.value = []
  selectedColors.value = []
  allProductImages.value = [] // Reset all images
  currentProductId.value = null
  activeImageTab.value = 'all'; // Reset to 'all' tab after close
}

const openConfirmDialog = () => {
  // Validate if all new or kept old images have a color assigned
  const imagesWithoutColor = allProductImages.value.filter(file => !file.colorId);

  if (imagesWithoutColor.length > 0) {
    ElMessage.error('Vui lòng chọn màu cho tất cả các hình ảnh đang hoạt động hoặc mới tải lên!');
    return;
  }
  isConfirmDialogVisible.value = true
}

// --- Product Details Logic ---

const generateProductDetails = () => {
  const newProductDetails = [];
  const existingDetailsMap = new Map();

  originalProductDetails.value.forEach(detail => {
    // Only consider active details for pre-filling
    if (detail.status === 1) {
      const key = `${detail.sizeId}-${detail.colorId}`;
      existingDetailsMap.set(key, detail);
    }
  });

  for (const sizeId of selectedSizes.value) {
    // Filter out invalid colorIds (null/undefined)
    for (const colorId of selectedColors.value.filter(id => id !== null && id !== undefined)) { 
      const size = sizeList.value.find((s) => s.id === sizeId);
      const color = colorList.value.find((c) => c.id === colorId);
      const key = `${sizeId}-${colorId}`;

      const detailToUse = existingDetailsMap.get(key);

      newProductDetails.push({
        sizeId: sizeId,
        colorId: colorId,
        sizeName: size?.sizeName || '',
        colorName: color?.colorName || '',
        sellPrice: detailToUse?.sellPrice || updateProduct.value.sellPrice || 0,
        quantity: detailToUse?.quantity || 0,
        id: detailToUse?.id || null, // Keep existing ID for backend to update/restore
        status: detailToUse ? detailToUse.status : 1, // Default new details to active (1)
      });
    }
  }
  productDetails.value = newProductDetails;
}

const handleSelectedColorsChange = (newSelectedColors) => {
    generateProductDetails(); // Re-generate product details first based on new color selection

    // Update image color assignments: If an image's assigned color is no longer selected, nullify its colorId
    allProductImages.value.forEach(img => {
        if (img.colorId && !newSelectedColors.includes(img.colorId)) {
            img.colorId = null; // Unlink image from a deselected color
        }
    });

    // Reset active tab if the currently active color tab is no longer selected
    if (activeImageTab.value !== 'all' && activeImageTab.value !== 'unassigned' && !newSelectedColors.includes(parseInt(activeImageTab.value))) {
        activeImageTab.value = 'all'; // Change to 'all' if current tab's color is removed
    }
}

// --- Image Handling Logic ---

// Computed property to filter files for the active tab
const filteredFiles = computed(() => (tabColorId) => {
  if (tabColorId === null) { // This handles 'unassigned' tab
    return allProductImages.value.filter(file => file.colorId === null || file.colorId === undefined);
  } else if (tabColorId === undefined) { // This handles 'all' tab (no specific colorId filter for the tab itself)
    return allProductImages.value;
  } else { // Specific color tab
    return allProductImages.value.filter(file => file.colorId === tabColorId);
  }
});


const handlePreview = (file) => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

// Handles both new file uploads and updates to existing files (e.g., changing colorId in 'all' tab)
const handleFileChange = (file, fileList, currentColorId = undefined) => {
  const maxSize = 5 * 1024 * 1024 // 5MB

  if (file.size > maxSize) {
    ElMessage.error(`Ảnh ${file.name} vượt quá kích thước tối đa (5MB)!`)
    // Remove the oversized file directly from the allProductImages array
    allProductImages.value = allProductImages.value.filter(f => f.uid !== file.uid);
    // Also remove from el-upload's internal fileList if it's not already filtered by the v-model
    const uploadComp = document.querySelector('.el-upload'); // Find the specific upload component if necessary
    if (uploadComp && uploadComp.__vue__) { // Access Vue instance if available
      // This is a bit hacky, directly manipulating internal state, but sometimes necessary for complex scenarios
      // A cleaner approach might involve re-assigning fileList prop if available.
      // For now, relying on v-model for filteredFiles to naturally update.
    }
    return;
  }

  // Find the file in the overall list by uid
  const existingFileIndex = allProductImages.value.findIndex(f => f.uid === file.uid);

  if (existingFileIndex > -1) {
    // If it's an existing file being updated (e.g., colorId changed or it's an old image being re-added by EL-Upload internally), update it
    allProductImages.value[existingFileIndex] = {
      ...allProductImages.value[existingFileIndex],
      // If currentColorId is provided (from a specific color tab), use it.
      // Otherwise, if it's from 'unassigned' tab, it's null.
      // If from 'all' tab, the select dropdown handles its own model (file.colorId is already bound).
      colorId: currentColorId !== undefined ? currentColorId : allProductImages.value[existingFileIndex].colorId,
      url: file.url || (file.raw ? URL.createObjectURL(file.raw) : allProductImages.value[existingFileIndex].url),
      file: file.raw || allProductImages.value[existingFileIndex].file, // Preserve raw file if exists
    };
  } else {
    // It's a new file being added
    // Check for exact duplicates (same raw file content/size) only for new files
    const isDuplicateNewFile = allProductImages.value.some(
        (f) => f.isNew && f.file && file.raw && f.file.size === file.raw.size && f.file.name === file.raw.name
    );

    if (file.raw && isDuplicateNewFile) {
        ElMessage.error(`Ảnh ${file.name} đã được chọn!`);
        // Remove the duplicate from the internal fileList of el-upload if necessary
        // This relies on el-upload's internal mechanism. The v-model for filteredFiles will take care of our state.
        return;
    }

    allProductImages.value.push({
      uid: file.uid || Math.random().toString(36).substring(2, 11), // Ensure uid
      name: file.name,
      url: file.url || URL.createObjectURL(file.raw),
      file: file.raw,
      isOld: false, // Mark as a new file
      isNew: true, // Mark as a new file
      id: null, // New file, no ID yet
      colorId: currentColorId, // Automatically assign colorId if from a specific tab (will be null for 'unassigned' tab)
    });
  }
};


const handleFileRemove = (fileToRemove) => {
  // Remove the file from the allProductImages array
  allProductImages.value = allProductImages.value.filter(
    (f) => f.uid !== fileToRemove.uid
  );
  // No need to track removedImageIds for this API call, as per simplified backend
};

// --- Save Product Logic ---

const saveProduct = async () => {
  isConfirmDialogVisible.value = false;
  try {
    const productData = {
      ...updateProduct.value,
      productDetails: productDetails.value.map((detail) => ({
        id: detail.id, // Pass ID to allow backend to update/restore existing details
        sizeId: detail.sizeId,
        colorId: detail.colorId,
        quantity: detail.quantity,
        sellPrice: detail.sellPrice,
        status: detail.status,
      })),
      // No removedColorIds or removedImageIds needed here as per simplified backend
    };

    const formData = new FormData();

    // Append JSON part of productData
    formData.append('productRequest', new Blob([JSON.stringify(productData)], { type: 'application/json' }));

    // Append new product images
    const newImages = allProductImages.value.filter(
      (file) => file.isNew && file.file // Only new files with actual raw file data
    );

    newImages.forEach((file, index) => {
      if (file.file) {
        // Backend expects ProductImageRequest with 'productImages' (MultipartFile) and 'colorId'
        // The list index must be part of the field name to correctly bind to List<ProductImageRequest>
        formData.append(`productImages[${index}].productImages`, file.file);
        formData.append(`productImages[${index}].colorId`, file.colorId);
        // productId is handled by the overall ProductRequest
      }
    });

    // For debugging: Log FormData entries
    for (const pair of formData.entries()) {
      console.log(`${pair[0]}, ${pair[1]}`);
    }

    const response = await axios.put(
      `http://localhost:8080/api/admin/products/restore/${currentProductId.value}`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data', // Essential for FormData
        },
      }
    );

    ElMessage.success('Sản phẩm đã được khôi phục và cập nhật thành công!');
    isRestoreDialogVisible.value = false;
    resetForm();
    fetchData(); // Reload product list
  } catch (error) {
    console.error('Lỗi khi khôi phục hoặc cập nhật sản phẩm:', error);
    ElMessage.error(
      'Không thể khôi phục hoặc cập nhật sản phẩm. Vui lòng kiểm tra lại.'
    );
  }
};

// --- Pagination Handlers ---
const handleSizeChange = (newSize) => {
  size.value = newSize
  page.value = 0 // Reset to first page
  fetchData()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage - 1 // Convert to 0-indexed
  fetchData()
}

// Initial data fetch on component mount
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.p-4 {
  padding: 1rem;
}

.card-container {
  border-radius: 8px;
  overflow: hidden;
}

.title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
}

.d-flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.align-center {
  align-items: center;
}

.mb-3 {
  margin-bottom: 1rem;
}

.mt-4 {
  margin-top: 1.5rem;
}

/* El-dialog styles */
.elegant-dialog :deep(.el-dialog__header) {
  background-color: #f5f7fa;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.elegant-dialog :deep(.el-dialog__title) {
  font-weight: bold;
  color: #303133;
}

.elegant-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.dialog-content {
  max-height: 70vh; /* Limits height of dialog content */
  overflow-y: auto; /* Enables scrolling for overflowing content */
  padding-right: 10px; /* Add some padding for the scrollbar */
}

.form-container {
  padding: 10px;
}

.section-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: #409eff;
  margin-top: 20px;
  margin-bottom: 15px;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 5px;
}

.checkbox-item {
  margin-right: 15px;
  margin-bottom: 10px;
}

.detail-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 15px;
  overflow: hidden;
}

.card-header {
  background-color: #f5f7fa;
  padding: 10px 15px;
  border-bottom: 1px solid #e4e7ed;
  font-size: 1rem;
  color: #606266;
}

.card-body {
  padding: 15px;
}

.form-actions {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
  margin-top: 20px;
}

.confirm-dialog :deep(.el-dialog__body) {
  text-align: center;
  font-size: 1.1rem;
}

.confirm-dialog :deep(.el-dialog__footer) {
  text-align: center;
}

/* Custom styles for el-upload */
.image-tabs :deep(.el-upload-list__item) {
  width: 148px; /* Standard size for picture-card */
  height: 148px; /* Standard size for picture-card */
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column; /* Allow content to stack */
  align-items: center;
  justify-content: center;
}

.image-tabs :deep(.el-upload-list__item-thumbnail) {
  max-width: 100%;
  max-height: 100px; /* Limit image height to make space for select */
  object-fit: contain;
  display: block;
}

.image-tabs .image-color-select {
  position: absolute; /* Position the select over the image space */
  bottom: 5px; /* Adjust as needed */
  left: 50%;
  transform: translateX(-50%);
  width: 90% !important; /* Ensure it fits */
}

.image-tabs :deep(.el-upload--picture-card) {
  width: 148px;
  height: 148px;
}

.category-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px; /* Space between tags */
}
</style>
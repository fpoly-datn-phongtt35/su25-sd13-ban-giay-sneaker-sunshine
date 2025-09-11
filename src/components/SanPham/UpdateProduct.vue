<template>
  <div class="ssn-wrap">
    <!-- Topbar -->
    <div class="ssn-topbar">
      <div class="left">
        <el-button link type="primary" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span class="ml-1">Quay lại</span>
        </el-button>
        <span class="divider"></span>
        <h2 class="title">Cập nhật Sản phẩm</h2>
      </div>

      <div class="right">
        <el-button @click="openConfirmDialog" type="success" round size="large">
          Cập nhật
        </el-button>
      </div>
    </div>

    <div class="ssn-container">
      <el-form
        ref="productForm"
        :model="updateProduct"
        :rules="rules"
        label-position="top"
        class="ssn-form"
      >
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
          <!-- LEFT: INFO + VARIANTS -->
          <div class="xl:col-span-2 space-y-6">
            <!-- General Info -->
            <el-card shadow="never" class="ssn-card">
              <template #header>
                <div class="card-header">
                  <h3>Thông tin chung</h3>
                </div>
              </template>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <el-form-item label="Tên sản phẩm" prop="productName">
                  <el-input
                    v-model="updateProduct.productName"
                    placeholder="Nhập tên sản phẩm"
                    clearable
                  />
                </el-form-item>

                <el-form-item label="Thương hiệu" prop="brandId">
                  <el-select v-model="updateProduct.brandId" placeholder="Chọn thương hiệu" filterable>
                    <el-option v-for="br in brandList" :key="br.id" :label="br.brandName" :value="br.id" />
                  </el-select>
                </el-form-item>

                <el-form-item label="Nhà cung cấp" prop="supplierId">
                  <el-select v-model="updateProduct.supplierId" placeholder="Chọn nhà cung cấp" filterable>
                    <el-option v-for="sp in supplierList" :key="sp.id" :label="sp.supplierName" :value="sp.id" />
                  </el-select>
                </el-form-item>

                <el-form-item label="Chất liệu" prop="materialId">
                  <el-select v-model="updateProduct.materialId" placeholder="Chọn chất liệu" filterable>
                    <el-option v-for="mt in materialList" :key="mt.id" :label="mt.materialName" :value="mt.id" />
                  </el-select>
                </el-form-item>

                <el-form-item label="Loại đế" prop="soleId">
                  <el-select v-model="updateProduct.soleId" placeholder="Chọn loại đế" filterable>
                    <el-option v-for="s in soleList" :key="s.id" :label="s.soleName" :value="s.id" />
                  </el-select>
                </el-form-item>

                <el-form-item label="Cổ giày" prop="styleId">
                  <el-select v-model="updateProduct.styleId" placeholder="Chọn cổ giày" filterable>
                    <el-option v-for="st in styleList" :key="st.id" :label="st.styleName" :value="st.id" />
                  </el-select>
                </el-form-item>

                <el-form-item label="Dành cho" prop="genderId">
                  <el-radio-group v-model="updateProduct.genderId" class="radio-inline">
                    <el-radio :label="'1'">Nam</el-radio>
                    <el-radio :label="'2'">Nữ</el-radio>
                    <el-radio :label="'3'">Unisex</el-radio>
                  </el-radio-group>
                </el-form-item>

                <div class="grid grid-cols-2 gap-4">
                  <el-form-item label="Cân nặng (gram)" prop="weight">
                    <el-input
                      type="number"
                      v-model.number="updateProduct.weight"
                      placeholder="0"
                      clearable
                      :min="0"
                    />
                  </el-form-item>

                  <el-form-item label="Giá bán mặc định" prop="sellPrice">
                    <div class="flex items-center gap-3">
                      <el-input
                        type="number"
                        v-model.number="updateProduct.sellPrice"
                        placeholder="0"
                        clearable
                        :min="0"
                        style="max-width:220px"
                      />
                    </div>
                  </el-form-item>
                </div>
              </div>

              <el-form-item label="Danh mục" prop="categoryIds" class="mt-2">
                <Multiselect
                  v-model="updateProduct.categoryIds"
                  :options="categoryList"
                  :multiple="true"
                  :close-on-select="false"
                  placeholder="Chọn danh mục"
                  label="categoryName"
                  track-by="id"
                  class="w-full"
                />
                <div class="chips" v-if="updateProduct.categoryIds?.length">
                  <span class="chip" v-for="c in updateProduct.categoryIds" :key="typeof c === 'object' ? c.id : c">
                    {{ typeof c === 'object' ? c.categoryName : getCategoryName(c) }}
                  </span>
                </div>
              </el-form-item>

              <el-form-item label="Mô tả" prop="description">
                <el-input
                  v-model="updateProduct.description"
                  type="textarea"
                  :rows="4"
                  placeholder="Mô tả ngắn về sản phẩm..."
                  maxlength="1000"
                  show-word-limit
                />
              </el-form-item>
            </el-card>

            <!-- Variants -->
            <el-card shadow="never" class="ssn-card">
              <template #header>
                <div class="card-header">
                  <h3>Biến thể (Size & Màu)</h3>
                </div>
              </template>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <el-form-item label="Kích thước" prop="selectedSizes">
                  <el-checkbox-group v-model="updateProduct.selectedSizes" @change="generateProductDetails">
                    <div class="check-grid">
                      <el-checkbox
                        v-for="size in sizeList"
                        :key="size.id"
                        :label="size.id"
                        class="check-item"
                      >
                        {{ size.sizeName }}
                      </el-checkbox>
                    </div>
                  </el-checkbox-group>
                </el-form-item>

                <el-form-item label="Màu sắc" prop="selectedColors">
                  <el-checkbox-group v-model="updateProduct.selectedColors" @change="generateProductDetails">
                    <div class="check-grid">
                      <el-checkbox
                        v-for="color in colorList"
                        :key="color.id"
                        :label="color.id"
                        class="check-item"
                      >
                        {{ color.colorName }}
                      </el-checkbox>
                    </div>
                  </el-checkbox-group>
                </el-form-item>
              </div>

              <el-divider />

              <div class="variants">
                <el-empty
                  v-if="!productDetails.length"
                  description="Chọn Size và Màu để tạo biến thể"
                />
                <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <el-card
                    v-for="(detail, index) in productDetails"
                    :key="detail.sizeId + '-' + detail.colorId"
                    shadow="hover"
                    class="variant-card"
                  >
                    <div class="variant-head">
                      <span class="badge">{{ detail.sizeName }}</span>
                      <span class="sep">×</span>
                      <span class="badge badge-blue">{{ detail.colorName }}</span>
                      <span class="price-badge">{{ formatCurrency(updateProduct.sellPrice) }}</span>
                    </div>
                    <div class="grid grid-cols-2 gap-3 mt-3">
                      <!-- Giá bán chi tiết đã bỏ input, hiển thị giá chung ở trên -->
                      <el-form-item
                        :prop="'productDetails.' + index + '.quantity'"
                        :rules="rules.productDetailQuantity"
                      >
                        <label class="form-label">Số lượng</label>
                        <el-input type="number" v-model.number="detail.quantity" :min="0" clearable />
                      </el-form-item>
                    </div>
                  </el-card>
                </div>
              </div>
            </el-card>

            <!-- Confirm Summary -->
            <el-card shadow="never" class="ssn-card">
              <template #header>
                <div class="card-header">
                  <h3>Xác nhận cập nhật</h3>
                </div>
              </template>
              <div class="flex items-center justify-between">
                <div class="meta">
                  <div class="meta-line">
                    Tổng biến thể: <b>{{ productDetails.length || 0 }}</b>
                  </div>
                  <div class="meta-line">
                    Tổng số lượng: <b>{{ totalQuantity }}</b>
                  </div>
                </div>
                <el-button type="success" size="large" @click="openConfirmDialog">
                  Cập nhật sản phẩm
                </el-button>
              </div>
            </el-card>
          </div>

          <!-- RIGHT: IMAGES BY COLOR -->
          <div class="xl:col-span-1 space-y-6">
            <el-card shadow="never" class="ssn-card">
              <template #header>
                <div class="card-header">
                  <h3>Hình ảnh theo màu</h3>
                </div>
              </template>

              <el-alert
                v-if="!updateProduct.selectedColors?.length"
                type="info"
                :closable="false"
                show-icon
                class="mb-3"
                description="Chọn màu để tải ảnh tương ứng."
              />

              <div v-for="colorId in updateProduct.selectedColors" :key="colorId" class="mb-6">
                <div class="color-heading">
                  <div class="dot" />
                  <div class="name">{{ getColorName(colorId) }}</div>
                  <div class="count">({{ (colorImages[colorId] || []).length }}/10)</div>
                </div>

                <el-upload
                  action="#"
                  list-type="picture-card"
                  :limit="10"
                  multiple
                  accept="image/*"
                  :file-list="colorImages[colorId] || []"
                  :auto-upload="false"
                  :on-preview="handlePreview"
                  :on-change="(file, fileList) => handleFileChange(file, fileList, colorId)"
                  :on-remove="(file, fileList) => handleFileRemove(file, fileList, colorId)"
                >
                  <template #default>
                    <div class="upload-slot">
                      <el-icon><Plus /></el-icon>
                      <span>Thêm ảnh</span>
                    </div>
                  </template>
                </el-upload>
              </div>

              <!-- hidden prop for validation trigger -->
              <el-form-item v-if="updateProduct.selectedColors.length > 0" prop="images">
                <div style="display:none;"></div>
              </el-form-item>
            </el-card>
          </div>
        </div>
      </el-form>
    </div>

    <!-- Confirm Dialog -->
    <el-dialog
      title="Xác nhận"
      v-model="isModalVisible"
      width="420px"
      center
      :before-close="closeModal"
      class="ssn-dialog"
    >
      <p>Bạn có chắc chắn muốn cập nhật sản phẩm này không?</p>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeModal">Hủy</el-button>
          <el-button type="success" @click="saveProduct">Xác nhận</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'
import { ElNotification } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import apiClient from '@/utils/axiosInstance'

/* Router */
const router = useRouter()
const route = useRoute()

/* Refs & state */
const productForm = ref(null)
const brandList = ref([])
const materialList = ref([])
const categoryList = ref([])
const sizeList = ref([])
const colorList = ref([])
const soleList = ref([])
const supplierList = ref([])
const styleList = ref([])
const isModalVisible = ref(false)
const colorImages = ref({}) // { colorId: [{ name, url, file, isOld, id }] }
const deletedImageIds = ref([])
const oldColorIds = ref([])
const productDetails = ref([]) // array of { id, sizeId, colorId, sizeName, colorName, quantity }

/* Reactive product model */
const updateProduct = reactive({
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
  selectedSizes: [],
  selectedColors: []
})

/* Computed total quantity */
const totalQuantity = computed(() =>
  productDetails.value.reduce((s, d) => s + Number(d.quantity || 0), 0)
)

/* Notification helpers */
const showSuccess = (message) => {
  ElNotification({ title: 'Thành công', message, type: 'success', duration: 3000, position: 'top-right' })
}
const showError = (message) => {
  ElNotification({ title: 'Lỗi', message, type: 'error', duration: 3000, position: 'top-right' })
}

/* Validation rules (note: productDetailSellPrice removed since sellPrice is global) */
const rules = {
  categoryIds: [
    {
      required: true,
      message: 'Vui lòng chọn ít nhất một danh mục',
      trigger: 'change',
      validator: (rule, value, cb) => (!value || value.length === 0) ? cb(new Error('Vui lòng chọn ít nhất một danh mục')) : cb()
    }
  ],
  productName: [{ required: true, message: 'Vui lòng nhập tên sản phẩm', trigger: 'blur' }],
  materialId: [{ required: true, message: 'Vui lòng chọn chất liệu', trigger: 'change' }],
  supplierId: [{ required: true, message: 'Vui lòng chọn nhà cung cấp', trigger: 'change' }],
  soleId: [{ required: true, message: 'Vui lòng chọn loại đế', trigger: 'change' }],
  styleId: [{ required: true, message: 'Vui lòng chọn cổ giày', trigger: 'change' }],
  brandId: [{ required: true, message: 'Vui lòng chọn thương hiệu', trigger: 'change' }],
  genderId: [{ required: true, message: 'Vui lòng chọn đối tượng', trigger: 'change' }],
  weight: [
    { required: true, message: 'Vui lòng nhập cân nặng', trigger: 'blur' },
    { type: 'number', min: 0, message: 'Cân nặng phải là số không âm', trigger: 'blur' }
  ],
  sellPrice: [
    { required: true, message: 'Vui lòng nhập giá bán', trigger: 'blur' },
    { type: 'number', min: 1, message: 'Giá bán phải là số lớn hơn 0', trigger: 'blur' }
  ],
  description: [{ required: true, message: 'Vui lòng nhập mô tả sản phẩm', trigger: 'blur' }],
  selectedSizes: [
    {
      required: true,
      message: 'Vui lòng chọn ít nhất một kích thước',
      trigger: 'change',
      validator: (rule, value, cb) => (!value || value.length === 0) ? cb(new Error('Vui lòng chọn ít nhất một kích thước')) : cb()
    }
  ],
  selectedColors: [
    {
      required: true,
      message: 'Vui lòng chọn ít nhất một màu sắc',
      trigger: 'change',
      validator: (rule, value, cb) => (!value || value.length === 0) ? cb(new Error('Vui lòng chọn ít nhất một màu sắc')) : cb()
    }
  ],
  images: [
    {
      validator: (rule, value, cb) => {
        for (const colorId of updateProduct.selectedColors) {
          const files = colorImages.value[colorId]
          if (!files || files.length === 0) {
            cb(new Error(`Vui lòng tải lên ít nhất một ảnh cho màu ${getColorName(colorId)}`))
            return
          }
        }
        cb()
      },
      trigger: 'change'
    }
  ],
  productDetailQuantity: [
    { type: 'number', min: 0, message: 'Số lượng phải là số không âm', trigger: 'blur' }
  ]
}

/* Helper to get names */
const getColorName = (colorId) => colorList.value.find(c => c.id === colorId)?.colorName || 'Không xác định'
const getCategoryName = (id) => categoryList.value.find(c => c.id === id)?.categoryName || ''

/* Format currency */
const formatCurrency = (v) => {
  if (v == null) return '0 ₫'
  const n = Number(v) || 0
  return n.toLocaleString('vi-VN') + ' ₫'
}

/* Fetchers */
const fetchCategories = async () => {
  try { categoryList.value = (await apiClient.get('/admin/categories/hien-thi')).data }
  catch (e) { console.error(e); showError('Lỗi lấy danh mục sản phẩm!') }
}
const fetchMaterial = async () => {
  try { materialList.value = (await apiClient.get('/admin/material/hien-thi')).data }
  catch (e) { console.error(e); showError('Lỗi lấy chất liệu sản phẩm!') }
}
const fetchSupplier = async () => {
  try { supplierList.value = (await apiClient.get('/admin/supplier/hien-thi')).data }
  catch (e) { console.error(e); showError('Lỗi lấy nhà cung cấp!') }
}
const fetchBrand = async () => {
  try { brandList.value = (await apiClient.get('/admin/brand/hien-thi')).data }
  catch (e) { console.error(e); showError('Lỗi lấy thương hiệu sản phẩm!') }
}
const fetchSole = async () => {
  try { soleList.value = (await apiClient.get('/admin/sole/hien-thi')).data }
  catch (e) { console.error(e); showError('Lỗi lấy đế giày sản phẩm!') }
}
const fetchStyle = async () => {
  try { styleList.value = (await apiClient.get('/admin/style/hien-thi')).data }
  catch (e) { console.error(e); showError('Lỗi lấy phong cách sản phẩm!') }
}
const fetchSizesAndColors = async () => {
  try {
    const [sizesRes, colorsRes] = await Promise.all([
      apiClient.get('/admin/size/hien-thi'),
      apiClient.get('/admin/color/hien-thi')
    ])
    sizeList.value = sizesRes.data
    colorList.value = colorsRes.data
  } catch (e) {
    console.error(e); showError('Lỗi lấy kích thước hoặc màu sắc!')
  }
}

/* Load product detail */
const fetchProduct = async () => {
  const id = route.params.id
  try {
    const product = (await apiClient.get(`/admin/products/${id}`)).data
    // map categories to objects if necessary
    updateProduct.categoryIds = product.categories?.map(c => ({ id: c.id, categoryName: c.categoryName })) || []
    updateProduct.productName = product.productName || ''
    updateProduct.materialId = product.materialId || null
    updateProduct.styleId = product.styleId || null
    updateProduct.supplierId = product.supplierId || null
    updateProduct.genderId = product.genderId ? product.genderId.toString() : null
    updateProduct.soleId = product.soleId || null
    updateProduct.brandId = product.brandId || null
    updateProduct.originPrice = product.originPrice || null
    updateProduct.weight = product.weight || null
    updateProduct.sellPrice = product.sellPrice || null
    updateProduct.description = product.description || ''
    updateProduct.selectedSizes = [...new Set(product.productDetails?.map(d => d.sizeId) || [])]
    updateProduct.selectedColors = [...new Set(product.productDetails?.map(d => d.colorId) || [])]

    productDetails.value = product.productDetails?.map(d => ({
      id: d.id,
      sizeId: d.sizeId,
      colorId: d.colorId,
      sizeName: d.sizeName,
      colorName: d.colorName,
      quantity: d.quantity
    })) || []

    // images
    colorImages.value = {}
    product.productImages?.forEach(img => {
      if (img.status === '1') {
        const colorId = img.colorId
        if (!colorImages.value[colorId]) colorImages.value[colorId] = []
        colorImages.value[colorId].push({
          name: img.imageName,
          url: img.image?.startsWith('data:image') ? img.image : `data:image/png;base64,${img.image}`,
          isOld: true,
          id: img.id,
          file: null
        })
      }
    })
    colorImages.value = { ...colorImages.value }
  } catch (e) {
    console.error(e); showError('Lỗi lấy dữ liệu sản phẩm!')
  }
}

/* Generate productDetails from selectedSizes x selectedColors while preserving existing entries */
const generateProductDetails = () => {
  const existed = [...productDetails.value] // shallow copy
  const out = []

  for (const sizeId of updateProduct.selectedSizes) {
    for (const colorId of updateProduct.selectedColors) {
      const size = sizeList.value.find(s => s.id === sizeId)
      const color = colorList.value.find(c => c.id === colorId)
      const old = existed.find(d => d.sizeId === sizeId && d.colorId === colorId)

      const baseQty = old?.quantity != null ? old.quantity : 0

      out.push({
        id: old?.id || null,
        sizeId,
        colorId,
        sizeName: size?.sizeName || '',
        colorName: color?.colorName || '',
        quantity: baseQty
      })
    }
  }
  productDetails.value = out
}

/* Watchers */

// khi sellPrice thay đổi -> luôn cập nhật giá biến thể trong payload khi save (displayed via formatCurrency)
watch(() => updateProduct.sellPrice, (val) => {
  // nothing to update on productDetails objects because we removed per-variant price,
  // but keep this watcher in case you want to enforce something later
})

// when removing a color, mark its images for deletion and remember old color
watch(() => updateProduct.selectedColors, (newColors, oldColors) => {
  const removed = oldColors?.filter(cid => !newColors.includes(cid)) || []
  removed.forEach(cid => {
    if (!oldColorIds.value.includes(cid)) oldColorIds.value.push(cid)
    if (colorImages.value[cid]) {
      colorImages.value[cid].forEach(img => {
        if (img.isOld && img.id && !deletedImageIds.value.includes(img.id)) deletedImageIds.value.push(img.id)
      })
      delete colorImages.value[cid]
    }
  })
  colorImages.value = { ...colorImages.value }
  productForm.value?.validateField('images')
}, { deep: true })

// when sizes change -> regenerate details
watch(() => updateProduct.selectedSizes, () => generateProductDetails())

/* Upload handlers */
const handleFileChange = (file, fileList, colorId) => {
  const max = 5 * 1024 * 1024
  if (file.size > max) {
    showError(`Ảnh ${file.name} vượt quá 5MB!`)
    colorImages.value[colorId] = fileList.filter(f => f.raw !== file.raw).map(item => ({
      name: item.name,
      url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
      file: item.raw || null,
      isOld: item.isOld || false,
      id: item.id || null
    }))
    colorImages.value = { ...colorImages.value }
    return
  }

  const existed = colorImages.value[colorId] || []
  const isDup = existed.some(f => f.name === file.name && (!f.file || f.file.size === file.size))
  if (isDup) {
    showError(`Ảnh ${file.name} đã được chọn cho màu này!`)
    colorImages.value[colorId] = fileList.filter(f => f.raw !== file.raw).map(item => ({
      name: item.name,
      url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
      file: item.raw || null,
      isOld: item.isOld || false,
      id: item.id || null
    }))
    colorImages.value = { ...colorImages.value }
    return
  }

  colorImages.value[colorId] = fileList.map(item => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null,
    isOld: item.isOld || false,
    id: item.id || null
  }))
  colorImages.value = { ...colorImages.value }
  productForm.value?.validateField('images')
}
const handleFileRemove = (file, fileList, colorId) => {
  if (file.isOld && file.id && !deletedImageIds.value.includes(file.id)) deletedImageIds.value.push(file.id)
  colorImages.value[colorId] = fileList.map(item => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null,
    isOld: item.isOld || false,
    id: item.id || null
  }))
  colorImages.value = { ...colorImages.value }
  productForm.value?.validateField('images')
}
const handlePreview = (file) => window.open(file.url, '_blank')

/* Dialog controls */
const openConfirmDialog = () => {
  // validate form + custom checks for productDetails
  productForm.value.validate(async (valid) => {
    if (valid) {
      // validate each variant has quantity non-negative
      for (const d of productDetails.value) {
        if (d.quantity === null || d.quantity === undefined || d.quantity < 0) {
          showError('Vui lòng nhập số lượng hợp lệ cho tất cả biến thể (>= 0).')
          return
        }
      }
      const totalQty = productDetails.value.reduce((s, d) => s + Number(d.quantity || 0), 0)
      if (totalQty <= 0) {
        showError('Tổng số lượng của các biến thể phải lớn hơn 0!')
        return
      }
      // validate images presence
      for (const colorId of updateProduct.selectedColors) {
        if (!colorImages.value[colorId] || colorImages.value[colorId].length === 0) {
          showError(`Vui lòng tải lên ít nhất 1 ảnh cho màu ${getColorName(colorId)}`)
          return
        }
      }

      isModalVisible.value = true
    } else {
      showError('Vui lòng điền đầy đủ các trường bắt buộc hoặc sửa lỗi!')
      setTimeout(() => {
        const el = document.querySelector('.is-error')
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }, 100)
    }
  })
}
const closeModal = () => { isModalVisible.value = false }
const goBack = () => { router.push('/product') }

/* Save handler */
const saveProduct = async () => {
  try {
    // prepare merged details (only keep combos currently selected)
    const mergedDetails = productDetails.value.filter(d =>
      updateProduct.selectedSizes.includes(d.sizeId) && updateProduct.selectedColors.includes(d.colorId)
    )

    const formData = new FormData()
    formData.append('productName', updateProduct.productName || '')
    formData.append('materialId', updateProduct.materialId || '')
    formData.append('supplierId', updateProduct.supplierId || '')
    formData.append('brandId', updateProduct.brandId || '')
    formData.append('soleId', updateProduct.soleId || '')
    formData.append('styleId', updateProduct.styleId || '')
    formData.append('genderId', updateProduct.genderId || '')
    formData.append('weight', updateProduct.weight ?? 0)
    formData.append('originPrice', updateProduct.originPrice ?? 0)
    // sellPrice is global and will be used as the price for each product detail on server
    formData.append('sellPrice', updateProduct.sellPrice ?? 0)
    formData.append('quantity', mergedDetails.reduce((s, d) => s + Number(d.quantity || 0), 0))
    formData.append('description', updateProduct.description || '')

    // categories (support object list or primitive ids)
    updateProduct.categoryIds.forEach((cat, idx) => {
      const id = typeof cat === 'object' ? cat.id : cat
      formData.append(`categoryIds[${idx}]`, id)
    })

    // product details: send id, sizeId, colorId, quantity
    mergedDetails.forEach((d, idx) => {
      if (d.id) formData.append(`productDetails[${idx}].id`, d.id)
      formData.append(`productDetails[${idx}].sizeId`, d.sizeId)
      formData.append(`productDetails[${idx}].colorId`, d.colorId)
      // we still send sellPrice for compatibility (server may expect it) — use global price
      formData.append(`productDetails[${idx}].sellPrice`, updateProduct.sellPrice ?? 0)
      formData.append(`productDetails[${idx}].quantity`, d.quantity ?? 0)
    })

    // deleted images (existing image ids user removed)
    if (deletedImageIds.value.length > 0) {
      deletedImageIds.value.forEach((id, idx) => formData.append(`oldImageIds[${idx}]`, id))
    }
    // removed old color ids
    if (oldColorIds.value.length > 0) {
      oldColorIds.value.forEach((id, idx) => formData.append(`removedColorIds[${idx}]`, id))
    }

    // append new images grouped with colorId
    let imageIndex = 0
    Object.entries(colorImages.value).forEach(([colorId, files]) => {
      files.filter(f => f.file && !f.isOld).forEach(f => {
        formData.append(`productImages[${imageIndex}].productImages`, f.file)
        formData.append(`productImages[${imageIndex}].colorId`, colorId)
        imageIndex++
      })
    })

    const id = route.params.id
    await apiClient.put(`/admin/products/${id}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })

    showSuccess('Cập nhật sản phẩm thành công!')
    isModalVisible.value = false

    // reset local state then redirect
    updateProduct.productName = ''
    updateProduct.categoryIds = []
    updateProduct.materialId = null
    updateProduct.styleId = null
    updateProduct.supplierId = null
    updateProduct.genderId = null
    updateProduct.soleId = null
    updateProduct.brandId = null
    updateProduct.originPrice = null
    updateProduct.weight = null
    updateProduct.sellPrice = null
    updateProduct.description = ''
    updateProduct.selectedSizes = []
    updateProduct.selectedColors = []
    colorImages.value = {}
    productDetails.value = []
    deletedImageIds.value = []
    oldColorIds.value = []

    setTimeout(() => router.push('/product'), 900)
  } catch (e) {
    console.error('Error updating product:', e)
    const msg = e.response?.data?.error || e.response?.data?.message || 'Đã xảy ra lỗi khi cập nhật sản phẩm.'
    showError(msg)
    isModalVisible.value = false
  }
}

/* Mount */
onMounted(() => {
  fetchBrand()
  fetchMaterial()
  fetchSizesAndColors()
  fetchSole()
  fetchStyle()
  fetchCategories()
  fetchSupplier()
  fetchProduct()
})
</script>

<style scoped>
/* ===== Colors & layout ===== */
.ssn-wrap {
  --bg: #f6f9ff;
  --card: #fff;
  --text: #0f172a;
  --muted: #64748b;
  --primary: #2563eb;
  --ring: rgba(37,99,235,.12);
  background: var(--bg);
  min-height: 100vh;
}

/* Topbar */
.ssn-topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  backdrop-filter: saturate(180%) blur(6px);
  background: linear-gradient(180deg, rgba(255,255,255,.9), rgba(255,255,255,.75));
  border-bottom: 1px solid #e5e9f2;
}
.ssn-topbar .left { display: flex; align-items: center; }
.ssn-topbar .divider { width: 1px; height: 22px; background: #e5e9f2; margin: 0 12px; }
.ssn-topbar .title { font-size: 18px; font-weight: 700; color: var(--text); }

.ssn-container { max-width: 1200px; margin: 18px auto 40px; padding: 0 16px; }
.ssn-form :deep(.el-form-item__label) { font-weight: 600; color: #334155; }

/* Card */
.ssn-card { border: 1px solid #eef2ff; border-radius: 14px; overflow: hidden; }
.ssn-card :deep(.el-card__header) { background: #fff; border-bottom: 1px solid #eef2ff; padding: 14px 18px; }
.card-header h3 { font-weight: 700; color: #0f172a; font-size: 16px; }

/* Chips */
.chips { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.chip {
  background: #eff6ff; border: 1px solid #dbeafe; color: #1e40af;
  padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 600;
}

/* Checkbox grid */
.check-grid { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 8px 12px; }
.check-item :deep(.el-checkbox__label) { color: #334155; }

/* Variant cards */
.variant-card { border-radius: 12px; border: 1px solid #eef2ff; padding: 12px; }
.variant-head { display: flex; align-items: center; gap: 8px; }
.badge { background: #f1f5f9; color: #0f172a; font-weight: 700; font-size: 12px; padding: 4px 8px; border-radius: 8px; }
.badge-blue { background: #e8f1ff; color: #1d4ed8; }
.sep { color: #94a3b8; }
.price-badge {
  margin-left: 8px;
  background: #fff7ed;
  border: 1px solid #ffedd5;
  color: #92400e;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
}

/* Images by color */
.color-heading { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.color-heading .dot { width: 8px; height: 8px; border-radius: 999px; background: var(--primary); box-shadow: 0 0 0 3px var(--ring); }
.color-heading .name { font-weight: 700; color: #0f172a; }
.color-heading .count { color: var(--muted); font-size: 12px; }
.upload-slot { display: flex; flex-direction: column; align-items: center; padding: 6px 0; font-size: 12px; color: #475569; }
.upload-slot :deep(.el-icon) { margin-bottom: 6px; }

/* Dialog */
.ssn-dialog :deep(.el-dialog__header) { border-bottom: 1px solid #eef2ff; }
.dialog-footer { display: flex; gap: 10px; justify-content: flex-end; }

/* Inputs rounding */
:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-textarea__inner) { border-radius: 10px !important; }

/* Minimal grid helpers */
.grid { display: grid; }
.grid-cols-1 { grid-template-columns: repeat(1,minmax(0,1fr)); }
.grid-cols-2 { grid-template-columns: repeat(2,minmax(0,1fr)); }
@media (min-width: 1280px) {
  .xl\:grid-cols-3 { grid-template-columns: repeat(3,minmax(0,1fr)); }
  .xl\:col-span-2 { grid-column: span 2 / span 2; }
  .xl\:col-span-1 { grid-column: span 1 / span 1; }
}
.gap-3 { gap: .75rem; } .gap-4 { gap: 1rem; } .gap-6 { gap: 1.5rem; }
.mt-2 { margin-top: .5rem; } .mt-3 { margin-top: .75rem; }
.mb-3 { margin-bottom: .75rem; } .mb-6 { margin-bottom: 1.5rem; }
.ml-1 { margin-left: .25rem; }
.flex { display: flex; } .items-center { align-items: center; } .justify-between { justify-content: space-between; }
.space-y-6 > * + * { margin-top: 1.5rem; }
.form-label { display:block; font-weight:600; margin-bottom:6px; color:#334155; }
</style>

<template>
  <div class="page">
    <!-- Header -->
    <div class="page-header">
      <el-button @click="goBack" round>
        <el-icon><arrow-left /></el-icon>
        Quay lại
      </el-button>
      <h2>Thêm sản phẩm mới</h2>
    </div>

    <!-- Form -->
    <el-form label-position="top" class="card">
      <!-- Danh mục -->
      <el-form-item label="Chọn danh mục sản phẩm" :error="errors.categoryIds">
        <el-select
          v-model="newProduct.categoryIds"
          multiple
          filterable
          collapse-tags
          collapse-tags-tooltip
          placeholder="Chọn danh mục"
          value-key="id"
          style="width: 100%"
        >
          <el-option
            v-for="c in categoryList"
            :key="c.id"
            :label="c.categoryName"
            :value="c"
          />
        </el-select>
        <div class="hint">
          <span>Đã chọn: </span>
          <template v-if="newProduct.categoryIds.length === 0">
            <el-text type="info">Chưa chọn</el-text>
          </template>
          <template v-else>
            <el-space wrap>
              <el-tag
                v-for="c in newProduct.categoryIds"
                :key="c.id"
                size="small"
                type="success"
              >{{ c.categoryName }}</el-tag>
            </el-space>
          </template>
        </div>
      </el-form-item>

      <!-- Tên sp -->
      <el-form-item label="Tên sản phẩm" :error="errors.productName">
        <el-input
          v-model="newProduct.productName"
          placeholder="Nhập tên sản phẩm"
          clearable
        />
      </el-form-item>

      <!-- 2 cột thông tin chọn lựa -->
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-form-item label="Chất liệu" :error="errors.materialId">
            <el-select v-model="newProduct.materialId" placeholder="Chọn chất liệu" style="width:100%">
              <el-option v-for="m in materialList" :key="m.id" :label="m.materialName" :value="m.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="Nhà cung cấp" :error="errors.supplierId">
            <el-select v-model="newProduct.supplierId" placeholder="Chọn nhà cung cấp" style="width:100%">
              <el-option v-for="s in supplierList" :key="s.id" :label="s.supplierName" :value="s.id" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :xs="24" :sm="12">
          <el-form-item label="Đế giày" :error="errors.soleId">
            <el-select v-model="newProduct.soleId" placeholder="Chọn loại đế" style="width:100%">
              <el-option v-for="s in soleList" :key="s.id" :label="s.soleName" :value="s.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="Cổ giày" :error="errors.styleId">
            <el-select v-model="newProduct.styleId" placeholder="Chọn cổ giày" style="width:100%">
              <el-option v-for="s in styleList" :key="s.id" :label="s.styleName" :value="s.id" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :xs="24" :sm="12">
          <el-form-item label="Thương hiệu" :error="errors.brandId">
            <el-select v-model="newProduct.brandId" placeholder="Chọn thương hiệu" style="width:100%">
              <el-option v-for="b in brandList" :key="b.id" :label="b.brandName" :value="b.id" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :xs="24" :sm="12">
          <el-form-item label="Dành cho" :error="errors.genderId">
            <el-radio-group v-model="newProduct.genderId">
              <el-radio :label="1">Nam</el-radio>
              <el-radio :label="2">Nữ</el-radio>
              <el-radio :label="3">Cả nam và nữ</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- Trọng lượng + Giá bán -->
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-form-item label="Cân nặng (gram)" :error="errors.weight">
            <el-input-number v-model="newProduct.weight" :min="0" :step="10" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="Giá bán" :error="errors.sellPrice">
            <el-input-number v-model="newProduct.sellPrice" :min="0" :step="1000" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- Ghi chú -->
      <el-form-item label="Ghi chú sản phẩm">
        <el-input
          v-model="newProduct.description"
          type="textarea"
          :rows="3"
          placeholder="Ghi chú sản phẩm"
        />
      </el-form-item>

      <!-- Kích thước -->
      <el-form-item label="Kích thước" :error="errors.selectedSizes">
        <el-checkbox-group v-model="selectedSizes" @change="generateProductDetails">
          <el-space wrap>
            <el-checkbox
              v-for="sz in sizeList"
              :key="sz.id"
              :label="sz.id"
            >{{ sz.sizeName }}</el-checkbox>
          </el-space>
        </el-checkbox-group>
      </el-form-item>

      <!-- Màu sắc -->
      <el-form-item label="Màu sắc" :error="errors.selectedColors">
        <el-checkbox-group v-model="selectedColors" @change="generateProductDetails">
          <el-space wrap>
            <el-checkbox
              v-for="cl in colorList"
              :key="cl.id"
              :label="cl.id"
            >{{ cl.colorName }}</el-checkbox>
          </el-space>
        </el-checkbox-group>
      </el-form-item>

      <!-- Upload ảnh theo màu -->
      <div v-if="selectedColors.length > 0" class="block">
        <el-alert
          title="Tải ảnh theo từng màu. Mỗi màu nên có ít nhất 1 ảnh."
          type="info"
          :closable="false"
          show-icon
          class="mb-12"
        />
        <el-row :gutter="16">
          <el-col v-for="cid in selectedColors" :key="cid" :xs="24" :md="12">
            <el-card shadow="never" class="color-card">
              <template #header>
                <div class="card-header">
                  <span>Ảnh cho màu: <b>{{ getColorName(cid) }}</b></span>
                </div>
              </template>

              <el-upload
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                multiple
                :limit="10"
                accept="image/*"
                :file-list="colorImages[cid] || []"
                :on-change="(file, list) => handleFileChange(file, list, cid)"
                :on-remove="(file, list) => handleFileRemove(file, list, cid)"
                :on-preview="handlePreview"
              >
                <el-icon><plus /></el-icon>
              </el-upload>

              <div v-if="errors[`colorImage_${cid}`]" class="el-form-item__error mt-6">
                {{ errors[`colorImage_${cid}`] }}
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- Bảng chi tiết biến thể -->
      <div v-if="productDetails.length > 0" class="block">
        <div v-if="errors.productDetails" class="el-form-item__error mb-8">{{ errors.productDetails }}</div>

        <el-table :data="productDetails" border style="width: 100%">
          <el-table-column prop="sizeName" label="Kích thước" width="140" />
          <el-table-column prop="colorName" label="Màu sắc" width="180" />
          <el-table-column label="Số lượng" width="200">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="0"
                :step="1"
                style="width: 100%"
                :class="{ 'is-invalid': !!errors[`productDetail_${$index}_quantity`] }"
              />
              <div v-if="errors[`productDetail_${$index}_quantity`]" class="el-form-item__error">
                {{ errors[`productDetail_${$index}_quantity`] }}
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Actions -->
      <div class="actions">
        <el-button size="large" @click="goBack">Huỷ</el-button>
        <el-button size="large" type="primary" @click="openConfirmDialog">Thêm sản phẩm</el-button>
      </div>
    </el-form>

    <!-- Confirm Dialog -->
    <el-dialog v-model="isModalVisible" title="Xác nhận" width="420px">
      <span>Bạn có chắc chắn muốn lưu sản phẩm này không?</span>
      <template #footer>
        <el-button @click="closeModal">Hủy</el-button>
        <el-button type="primary" @click="saveProduct">Xác nhận</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import axios from 'axios'
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'

const router = useRouter()

// Data lists
const brandList = ref([])
const materialList = ref([])
const categoryList = ref([])
const sizeList = ref([])
const colorList = ref([])
const soleList = ref([])
const supplierList = ref([])
const styleList = ref([])

// UI states
const isModalVisible = ref(false)

// Images by colorId
const colorImages = ref({}) // { [colorId]: [{name,url,file,uid}] }

// selections
const selectedSizes = ref([])   // array of sizeId
const selectedColors = ref([])  // array of colorId

// generated details
const productDetails = ref([])

// main form model (giữ nguyên cấu trúc)
const newProduct = ref({
  categoryIds: [],   // giữ nguyên dạng mảng object (id, categoryName)
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

// errors
const errors = ref({})

// computed validate (giữ logic)
const isFormValid = computed(() => {
  errors.value = {}

  if (!newProduct.value.productName) errors.value.productName = 'Tên sản phẩm không được để trống.'
  if (!newProduct.value.materialId) errors.value.materialId = 'Vui lòng chọn chất liệu.'
  if (!newProduct.value.genderId) errors.value.genderId = 'Vui lòng chọn đối tượng dành cho.'
  if (!newProduct.value.supplierId) errors.value.supplierId = 'Vui lòng chọn nhà cung cấp.'
  if (!newProduct.value.soleId) errors.value.soleId = 'Vui lòng chọn loại đế.'
  if (!newProduct.value.styleId) errors.value.styleId = 'Vui lòng chọn cổ giày.'
  if (!newProduct.value.brandId) errors.value.brandId = 'Vui lòng chọn thương hiệu.'

  if (newProduct.value.categoryIds.length === 0) errors.value.categoryIds = 'Vui lòng chọn ít nhất một danh mục.'

  if (newProduct.value.weight === null || Number(newProduct.value.weight) < 0) {
    errors.value.weight = 'Cân nặng phải là số dương.'
  }
  if (newProduct.value.sellPrice === null || Number(newProduct.value.sellPrice) <= 0) {
    errors.value.sellPrice = 'Giá bán phải lớn hơn 0.'
  }

  if (selectedSizes.value.length === 0) errors.value.selectedSizes = 'Vui lòng chọn ít nhất một kích thước.'
  if (selectedColors.value.length === 0) errors.value.selectedColors = 'Vui lòng chọn ít nhất một màu sắc.'

  if (selectedSizes.value.length > 0 && selectedColors.value.length > 0 && productDetails.value.length === 0) {
    errors.value.productDetails = 'Không có chi tiết sản phẩm nào được tạo.'
  } else {
    productDetails.value.forEach((d, i) => {
      if (d.sellPrice === null || Number(d.sellPrice) <= 0) {
        errors.value[`productDetail_${i}_sellPrice`] = `Giá bán của chi tiết ${i + 1} phải lớn hơn 0.`
      }
      if (d.quantity === null || Number(d.quantity) < 0) {
        errors.value[`productDetail_${i}_quantity`] = `Số lượng của chi tiết ${i + 1} phải là số không âm.`
      }
    })
  }

  // mỗi màu ít nhất một ảnh
  for (const cid of selectedColors.value) {
    if (!colorImages.value[cid] || colorImages.value[cid].length === 0) {
      errors.value[`colorImage_${cid}`] = `Vui lòng tải lên ít nhất một ảnh cho màu ${getColorName(cid)}.`
    }
  }

  return Object.keys(errors.value).length === 0
})

// Notifications
const notify = (message, type = 'success') => {
  ElNotification({ title: type === 'success' ? 'Thành công' : 'Lỗi', message, type, duration: 2500 })
}

// Confirm dialog handlers
const openConfirmDialog = () => {
  if (!isFormValid.value) {
    notify('Vui lòng điền đầy đủ và chính xác các trường bắt buộc.', 'error')
    // scroll to first error
    requestAnimationFrame(() => {
      const errEl = document.querySelector('.el-form-item__error')
      if (errEl) errEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
    })
    return
  }
  isModalVisible.value = true
}
const closeModal = () => { isModalVisible.value = false }

// fetchers
const fetchCategories = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/admin/categories/hien-thi')
    categoryList.value = data
  } catch (e) { notify('Lỗi lấy danh mục!', 'error') }
}
const fetchMaterial = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/admin/material/hien-thi')
    materialList.value = data
  } catch (e) { notify('Lỗi lấy chất liệu!', 'error') }
}
const fetchSupplier = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/admin/supplier/hien-thi')
    supplierList.value = data
  } catch (e) { notify('Lỗi lấy nhà cung cấp!', 'error') }
}
const fetchBrand = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/admin/brand/hien-thi')
    brandList.value = data
  } catch (e) { notify('Lỗi lấy thương hiệu!', 'error') }
}
const fetchSole = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/admin/sole/hien-thi')
    soleList.value = data
  } catch (e) { notify('Lỗi lấy đế giày!', 'error') }
}
const fetchStyle = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/admin/style/hien-thi')
    styleList.value = data
  } catch (e) { notify('Lỗi lấy cổ giày!', 'error') }
}
const fetchSizesAndColors = async () => {
  try {
    const [sRes, cRes] = await Promise.all([
      axios.get('http://localhost:8080/api/admin/size/hien-thi'),
      axios.get('http://localhost:8080/api/admin/color/hien-thi'),
    ])
    sizeList.value = sRes.data
    colorList.value = cRes.data
  } catch (e) { notify('Lỗi lấy kích thước hoặc màu sắc!', 'error') }
}

// helpers
const getColorName = (colorId) => colorList.value.find(c => c.id === colorId)?.colorName || 'Không xác định'

// generate details size×color (giữ nguyên logic)
const generateProductDetails = () => {
  const next = []
  for (const sizeId of selectedSizes.value) {
    for (const colorId of selectedColors.value) {
      const size = sizeList.value.find(s => s.id === sizeId)
      const color = colorList.value.find(c => c.id === colorId)
      const existed = productDetails.value.find(d => d.sizeId === sizeId && d.colorId === colorId)
      if (existed) {
        next.push(existed)
      } else {
        next.push({
          sizeId,
          colorId,
          sizeName: size?.sizeName || '',
          colorName: color?.colorName || '',
          sellPrice: newProduct.value.sellPrice || 0,
          quantity: 0,
        })
      }
    }
  }
  // sort cho đẹp
  next.sort((a, b) => (a.sizeId - b.sizeId) || (a.colorId - b.colorId))
  productDetails.value = next
}

// Back
const goBack = () => router.push('/product')

// upload handlers
const handleFileChange = (file, fileList, colorId) => {
  const max = 5 * 1024 * 1024
  if (file.size > max) {
    notify(`Ảnh ${file.name} vượt quá 5MB!`, 'error')
    fileList.splice(fileList.indexOf(file), 1)
    return
  }
  const dup = (colorImages.value[colorId] || []).some(f => f.name === file.name && f.file?.size === file.size)
  if (dup) {
    notify(`Ảnh ${file.name} đã được chọn cho màu này!`, 'error')
    fileList.splice(fileList.indexOf(file), 1)
    return
  }

  const f = {
    name: file.name,
    url: file.url || (file.raw ? URL.createObjectURL(file.raw) : ''),
    file: file.raw || null,
    uid: file.uid,
  }
  if (!colorImages.value[colorId]) colorImages.value[colorId] = []
  colorImages.value[colorId].push(f)
  colorImages.value = { ...colorImages.value }

  if (errors.value[`colorImage_${colorId}`]) delete errors.value[`colorImage_${colorId}`]
}

const handleFileRemove = (file, fileList, colorId) => {
  colorImages.value[colorId] = fileList.map(item => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null,
    uid: item.uid,
  }))
  colorImages.value = { ...colorImages.value }
}

const handlePreview = (file) => {
  if (file.url) window.open(file.url, '_blank')
}

// save
const saveProduct = async () => {
  try {
    // total qty > 0
    const totalQty = productDetails.value.reduce((s, d) => s + Number(d.quantity || 0), 0)
    if (totalQty <= 0) {
      notify('Tổng số lượng các biến thể phải lớn hơn 0!', 'error')
      isModalVisible.value = false
      return
    }

    // check ảnh theo màu
    for (const cid of selectedColors.value) {
      if (!colorImages.value[cid] || colorImages.value[cid].length === 0) {
        notify(`Vui lòng chọn ít nhất một ảnh cho màu ${getColorName(cid)}!`, 'error')
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
    formData.append('originPrice', newProduct.value.originPrice || 0)
    formData.append('sellPrice', newProduct.value.sellPrice || 0)
    formData.append('quantity', totalQty)
    formData.append('description', newProduct.value.description || '')

    // categoryIds (giữ nguyên dạng object ⇒ gửi id)
    const cats = Array.isArray(newProduct.value.categoryIds) ? newProduct.value.categoryIds : []
    cats.forEach((cat, idx) => formData.append(`categoryIds[${idx}]`, cat?.id ?? cat))

    // productDetails
    productDetails.value.forEach((d, i) => {
      formData.append(`productDetails[${i}].sizeId`, d.sizeId)
      formData.append(`productDetails[${i}].colorId`, d.colorId)
      formData.append(`productDetails[${i}].sellPrice`, newProduct.value.sellPrice || 0)
      formData.append(`productDetails[${i}].quantity`, d.quantity)
    })

    // productImages by color
    let imgIdx = 0
    Object.entries(colorImages.value).forEach(([cid, files]) => {
      files.filter(f => f.file).forEach(f => {
        formData.append(`productImages[${imgIdx}].productImages`, f.file)
        formData.append(`productImages[${imgIdx}].colorId`, cid)
        imgIdx++
      })
    })

    // debug (tuỳ ý bật)
    // for (const [k, v] of formData.entries()) console.log(k, v instanceof File ? v.name : v)

    const res = await axios.post('http://localhost:8080/api/admin/products', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    notify('Thêm sản phẩm thành công!')
    isModalVisible.value = false

    // reset
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
    errors.value = {}

    setTimeout(() => router.push('/product'), 1200)
  } catch (e) {
    console.error('Lỗi thêm sản phẩm:', e)
    const m = e.response?.data?.error || 'Đã xảy ra lỗi khi thêm sản phẩm.'
    notify(m, 'error')
    isModalVisible.value = false
  }
}

// watches
watch(selectedColors, (n, o) => {
  const removed = (o || []).filter(id => !n.includes(id))
  removed.forEach(id => {
    delete colorImages.value[id]
    colorImages.value = { ...colorImages.value }
    if (errors.value[`colorImage_${id}`]) delete errors.value[`colorImage_${id}`]
  })
  generateProductDetails()
})
watch(selectedSizes, () => generateProductDetails())

// đồng bộ giá bán chung xuống từng biến thể khi thay đổi
watch(() => newProduct.value.sellPrice, (val) => {
  if (val !== null && val !== undefined) {
    productDetails.value.forEach(d => d.sellPrice = Number(val))
  }
})

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
.page {
  max-width: 1100px;
  margin: 32px auto;
  padding: 0 12px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.page-header h2 { margin: 0 0 0 8px; font-weight: 700; }
.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
.hint { margin-top: 6px; }
.block { margin-top: 16px; }
.mb-8 { margin-bottom: 8px; }
.mb-12 { margin-bottom: 12px; }
.mt-6 { margin-top: 6px; }
.color-card { margin-bottom: 12px; }
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
.is-invalid :deep(.el-input__wrapper),
.is-invalid :deep(.el-input-number__decrease),
.is-invalid :deep(.el-input-number__increase) {
  border-color: var(--el-color-danger) !important;
}
</style>

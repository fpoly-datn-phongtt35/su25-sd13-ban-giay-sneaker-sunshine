<template>
  <div class="page">
    <!-- Header actions -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="goToAdd">Thêm sản phẩm</el-button>

      <div class="toolbar__right">
        <el-button type="success" :icon="Document" @click="downloadExcel">Xuất Excel</el-button>
        <el-button
          v-if="selectedIds.size"
          type="danger"
          plain
          :icon="CircleClose"
          @click="clearAllSelections"
        >
          Bỏ chọn ({{ selectedIds.size }})
        </el-button>
        <el-button :icon="Clock" @click="goToHistory">Lịch sử sản phẩm</el-button>
      </div>
    </div>

    <!-- Compact filter (sticky) -->
    <el-card shadow="never" class="filter-card" body-class="filter-card__body">
      <template #header>
        <div class="filter-card__head">
          <div class="filter-title">Tìm kiếm</div>
          <el-link type="primary" :underline="false" @click="showAdvanced = !showAdvanced">
            {{ showAdvanced ? 'Thu gọn' : 'Mở rộng' }}
          </el-link>
        </div>
      </template>

      <!-- Hàng filter chính (inline, nhỏ gọn) -->
      <el-form :model="filters" :inline="true" size="small" class="filter-inline" @submit.prevent>
        <el-form-item label="Tên/Mã" class="w-64">
          <el-input
            v-model="filters.keyword"
            clearable
            :prefix-icon="Search"
            placeholder="Nhập tên hoặc mã"
            @keyup.enter="onSearch"
          />
        </el-form-item>

        <el-form-item label="Danh mục" class="w-60">
          <el-select
            v-model="filters.categoryIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            filterable
            clearable
            placeholder="Chọn"
          >
            <el-option v-for="c in categoryList" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="Thương hiệu" class="w-56">
          <el-select v-model="filters.brandId" filterable clearable placeholder="Chọn">
            <el-option v-for="b in brandList" :key="b.id" :label="b.brandName" :value="b.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="Giá" class="w-64">
          <div class="price-range">
            <el-input-number v-model="filters.priceMin" :min="0" :step="1000" :precision="0" placeholder="Min" />
            <span class="sep">—</span>
            <el-input-number v-model="filters.priceMax" :min="0" :step="1000" :precision="0" placeholder="Max" />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">Tìm kiếm</el-button>
          <el-button :icon="Refresh" @click="onReset">Đặt lại</el-button>
        </el-form-item>
      </el-form>

      <!-- Nâng cao -->
      <el-collapse-transition>
        <div v-show="showAdvanced" class="advanced">
          <el-form :model="filters" label-position="top" size="small" class="advanced-grid">
            <el-form-item label="Chất liệu">
              <el-select v-model="filters.materialId" clearable filterable placeholder="Chọn">
                <el-option v-for="m in materialList" :key="m.id" :label="m.materialName" :value="m.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="Loại đế">
              <el-select v-model="filters.soleId" clearable filterable placeholder="Chọn">
                <el-option v-for="s in soleList" :key="s.id" :label="s.soleName" :value="s.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="Cổ giày">
              <el-select v-model="filters.styleId" clearable filterable placeholder="Chọn">
                <el-option v-for="s in styleList" :key="s.id" :label="s.styleName" :value="s.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="Dành cho" class="col-span-3">
              <el-radio-group v-model="filters.genderId">
                <el-radio-button :label="1">Nam</el-radio-button>
                <el-radio-button :label="2">Nữ</el-radio-button>
                <el-radio-button :label="3">Cả hai</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="Ngày tạo (Từ - Đến)" class="col-span-3">
              <div class="date-range">
                <el-date-picker
                  v-model="filters.createdFrom"
                  type="datetime"
                  placeholder="Từ ngày"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  clearable
                />
                <el-date-picker
                  v-model="filters.createdTo"
                  type="datetime"
                  placeholder="Đến ngày"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  clearable
                />
              </div>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- Summary -->
    <div class="summary">
      Đã chọn: <b>{{ selectedIds.size }}</b> / Tổng: <b>{{ totalElements }}</b> sản phẩm
    </div>

    <!-- Table -->
    <el-card shadow="never">
      <el-table
        v-loading="tableLoading"
        :data="productList"
        :row-key="rowKey"
        border
        stripe
        height="540"
        @selection-change="onSelectionChange"
        :default-sort="{ prop: 'productCode', order: 'ascending' }"
      >
        <el-table-column type="selection" width="50" :reserve-selection="true" />

        <el-table-column label="STT" width="80">
          <template #default="{ $index }">
            {{ page * size + $index + 1 }}
          </template>
        </el-table-column>

        <el-table-column prop="productCode" label="Mã" min-width="120" show-overflow-tooltip />
        <el-table-column prop="productName" label="Tên sản phẩm" min-width="220" show-overflow-tooltip />
        <el-table-column prop="brandName" label="Thương hiệu" min-width="140" />
        <el-table-column prop="styleName" label="Cổ giày" min-width="120" />

        <el-table-column label="Giá bán" min-width="180">
          <template #default="{ row }">
            <template v-if="row.discountPercentage > 0 || row.discountedPrice < row.sellPrice">
              <span class="line-through mr-2 text-muted">{{ formatCurrency(row.sellPrice) }}</span>
              <span class="price-sale">{{ formatCurrency(row.discountedPrice) }}</span>
            </template>
            <template v-else>
              <span class="price">{{ formatCurrency(row.sellPrice) }}</span>
            </template>
          </template>
        </el-table-column>

        <el-table-column prop="quantity" label="Số lượng" width="110">
          <template #default="{ row }">
            <el-tag :type="row.quantity < 10 ? 'danger' : 'success'" effect="light">
              {{ row.quantity }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Hành động" fixed="right" width="170">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="goToUpdate(row.id)" />
            <el-button size="small" :icon="InfoFilled" @click="goToDetail(row.id)" />
            <el-popconfirm
              title="Chuyển sản phẩm vào thùng rác?"
              confirm-button-text="Xóa"
              cancel-button-text="Hủy"
              confirm-button-type="danger"
              @confirm="deleteProduct(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger" :icon="Delete" />
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalElements"
          :current-page="page + 1"
          :page-size="size"
          :page-sizes="[5, 8, 10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Document,
  Clock,
  CircleClose,
  Edit,
  InfoFilled,
  Delete,
} from '@element-plus/icons-vue'
import apiClient from '@/utils/axiosInstance'

type ProductRow = {
  id: number
  productCode: string
  productName: string
  brandName: string
  styleName: string
  sellPrice: number
  discountedPrice: number
  discountPercentage: number
  quantity: number
}

const router = useRouter()

/* ===== UI State ===== */
const showAdvanced = ref(false)
const tableLoading = ref(false)

/* ===== Filters ===== */
const filters = ref({
  keyword: '',
  brandId: null as number | null,
  genderId: null as number | null,
  styleId: null as number | null,
  soleId: null as number | null,
  materialId: null as number | null,
  createdFrom: null as string | null,
  createdTo: null as string | null,
  categoryIds: [] as number[],
  priceMin: null as number | null,
  priceMax: null as number | null,
  status: 1,
})

/* ===== Reference Data ===== */
const brandList = ref<any[]>([])
const materialList = ref<any[]>([])
const categoryList = ref<any[]>([])
const soleList = ref<any[]>([])
const styleList = ref<any[]>([])

/* ===== Table & Pagination ===== */
const productList = ref<ProductRow[]>([])
const totalElements = ref(0)
const totalPages = ref(0)
const page = ref(0)
const size = ref(8)

/* ===== Selection (reserve across pages) ===== */
const selectedIds = ref<Set<number>>(new Set<number>())
const rowKey = (row: ProductRow) => row.id
const onSelectionChange = (rows: ProductRow[]) => {
  const currentPageIds = productList.value.map(r => r.id)
  currentPageIds.forEach(id => selectedIds.value.delete(id))
  rows.forEach(r => selectedIds.value.add(r.id))
}
const clearAllSelections = () => {
  selectedIds.value.clear()
  ElMessage.info('Đã bỏ chọn tất cả sản phẩm.')
}

/* ===== Fetch reference data ===== */
const fetchCategories = async () => {
  try {
    const res = await apiClient.get('/admin/categories/hien-thi')
    categoryList.value = res.data
  } catch { ElMessage.error('Lấy danh mục thất bại!') }
}
const fetchMaterial = async () => {
  try {
    const res = await apiClient.get('/admin/material/hien-thi')
    materialList.value = res.data
  } catch { ElMessage.error('Lấy chất liệu thất bại!') }
}
const fetchBrand = async () => {
  try {
    const res = await apiClient.get('/admin/brand/hien-thi')
    brandList.value = res.data
  } catch { ElMessage.error('Lấy thương hiệu thất bại!') }
}
const fetchSole = async () => {
  try {
    const res = await apiClient.get('/admin/sole/hien-thi')
    soleList.value = res.data
  } catch { ElMessage.error('Lấy đế giày thất bại!') }
}
const fetchStyle = async () => {
  try {
    const res = await apiClient.get('/admin/style/hien-thi')
    styleList.value = res.data
  } catch { ElMessage.error('Lấy phong cách thất bại!') }
}

/* ===== Validation ===== */
const validateRanges = (): boolean => {
  const { priceMin, priceMax, createdFrom, createdTo } = filters.value
  if (priceMin != null && priceMax != null && priceMin > priceMax) {
    ElMessage.warning('Giá Min không được lớn hơn Giá Max.')
    return false
  }
  if (createdFrom && createdTo && createdFrom > createdTo) {
    ElMessage.warning('Ngày bắt đầu không được lớn hơn ngày kết thúc.')
    return false
  }
  return true
}

/* ===== Fetch products ===== */
const fetchProduct = async () => {
  if (!validateRanges()) return
  tableLoading.value = true
  try {
    const payload = {
      keyword: filters.value.keyword || null,
      brandId: filters.value.brandId || null,
      genderId: filters.value.genderId || null,
      styleId: filters.value.styleId || null,
      soleId: filters.value.soleId || null,
      materialId: filters.value.materialId || null,
      createdFrom: filters.value.createdFrom || null,
      createdTo: filters.value.createdTo || null,
      categoryIds: filters.value.categoryIds || [],
      priceMin: filters.value.priceMin ?? null,
      priceMax: filters.value.priceMax ?? null,
      page: page.value,
      size: size.value,
      status: 1,
    }
    const res = await apiClient.post('/admin/products/search', payload)
    productList.value = res.data.data || []
    totalElements.value = res.data.pagination?.totalElements || 0
    totalPages.value = res.data.pagination?.totalPages || 0
  } catch {
    ElMessage.error('Tải danh sách sản phẩm thất bại!')
  } finally {
    tableLoading.value = false
  }
}

/* ===== Actions ===== */
const onSearch = () => { page.value = 0; fetchProduct() }
const onReset = () => {
  filters.value = {
    keyword: '',
    brandId: null, genderId: null, styleId: null, soleId: null, materialId: null,
    createdFrom: null, createdTo: null,
    categoryIds: [],
    priceMin: null, priceMax: null,
    status: 1,
  }
  page.value = 0
  size.value = 8
  selectedIds.value.clear()
  fetchProduct()
}

const handleSizeChange = (newSize: number) => { size.value = newSize; page.value = 0; fetchProduct() }
const handleCurrentChange = (newPage: number) => { page.value = newPage - 1; fetchProduct() }

const downloadExcel = async () => {
  try {
    const hasIds = selectedIds.value.size > 0
    const msg = hasIds
      ? `Xuất Excel cho ${selectedIds.value.size} sản phẩm đã chọn?`
      : 'Xuất Excel toàn bộ sản phẩm theo bộ lọc hiện tại?'
    await ElMessageBox.confirm(msg, 'Xác nhận', {
      type: 'warning', confirmButtonText: 'Xuất', cancelButtonText: 'Hủy',
    })

    let url = '', data: any, filename = ''
    if (hasIds) {
      url = '/admin/products/export-excel/by-ids'
      data = Array.from(selectedIds.value)
      filename = 'products-by-ids.xlsx'
    } else {
      const { keyword, brandId, genderId, styleId, soleId, materialId, createdFrom, createdTo, categoryIds, priceMin, priceMax } =
        filters.value
      url = '/admin/products/export-excel/by-filter'
      data = {
        keyword: keyword || null, brandId: brandId || null, genderId: genderId || null,
        styleId: styleId || null, soleId: soleId || null, materialId: materialId || null,
        createdFrom: createdFrom || null, createdTo: createdTo || null,
        categoryIds: categoryIds || [], priceMin: priceMin ?? null, priceMax: priceMax ?? null,
        status: 1,
      }
      filename = 'products-by-filter.xlsx'
    }

    const res = await apiClient.post(url, data, { responseType: 'blob' })
    const blobUrl = window.URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a'); a.href = blobUrl; a.download = filename
    document.body.appendChild(a); a.click(); document.body.removeChild(a)
    window.URL.revokeObjectURL(blobUrl)
    ElMessage.success('Xuất Excel thành công!')
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error('Xuất Excel thất bại!')
  }
}

const deleteProduct = async (id: number) => {
  try {
    await apiClient.delete(`/admin/products/${id}`)
    ElMessage.success('Xóa sản phẩm thành công!')
    selectedIds.value.delete(id)
    fetchProduct()
  } catch {
    ElMessage.error('Xóa sản phẩm thất bại!')
  }
}

/* ===== Navigation ===== */
const goToAdd = () => router.push('/product/add')
const goToHistory = () => router.push('/product/history')
const goToUpdate = (id: number) => router.push({ name: 'UpdateProduct', params: { id } })
const goToDetail = (id: number) => router.push({ name: 'DetailProduct', params: { id } })

/* ===== Helpers ===== */
const formatCurrency = (val: number) => {
  if (!val) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', minimumFractionDigits: 0 }).format(val)
}

/* ===== Init ===== */
onMounted(() => {
  fetchBrand(); fetchCategories(); fetchMaterial(); fetchSole(); fetchStyle(); fetchProduct()
})
</script>

<style scoped>
/* Layout tổng thể */
.page { padding: 16px; }

/* Toolbar */
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.toolbar__right { display: flex; gap: 8px; }

/* Filter card (sticky để luôn thấy bảng) */
.filter-card { position: sticky; top: 0; z-index: 12; background: #fff; margin-bottom: 10px; }
.filter-card__head { display: flex; align-items: center; justify-content: space-between; }
.filter-title { font-weight: 600; }
.filter-card__body { padding-top: 10px; padding-bottom: 8px; }

/* Inline form gọn */
.filter-inline :deep(.el-form-item) { margin-right: 10px; margin-bottom: 8px; }
.w-56 { width: 224px; }
.w-60 { width: 240px; }
.w-64 { width: 256px; }
.price-range { display: flex; align-items: center; gap: 4px; }
.sep { color: #c0c4cc; padding: 0 2px; }

/* Advanced area */
.advanced { margin-top: 8px; }
.advanced-grid { display: grid; grid-template-columns: repeat(1, minmax(0,1fr)); gap: 10px; }
@media (min-width: 768px) { .advanced-grid { grid-template-columns: repeat(3, minmax(0,1fr)); } }
.col-span-3 { grid-column: span 3 / span 3; }
.date-range { display: grid; grid-template-columns: 1fr; gap: 8px; }
@media (min-width: 768px) { .date-range { grid-template-columns: 1fr 1fr; } }

/* Summary */
.summary { color: #6b7280; margin: 6px 0 10px; }

/* Prices */
.text-muted { color: #9ca3af; }
.line-through { text-decoration: line-through; }
.mr-2 { margin-right: .5rem; }
.price { font-weight: 600; }
.price-sale { color: #d03050; font-weight: 600; }

/* Pager */
.pager { display: flex; justify-content: end; margin-top: 12px; }
</style>

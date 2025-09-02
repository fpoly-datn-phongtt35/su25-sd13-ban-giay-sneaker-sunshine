<template>
  <el-container class="page-container">
    <el-main>
      <el-card class="form-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h1>Tạo đợt giảm giá mới</h1>
            <p class="subtitle">Chọn sản phẩm/SPCT và áp dụng phần trăm giảm</p>
          </div>
        </template>

        <el-form :model="form" label-position="top" @submit.prevent="createCampaign">
          <el-row :gutter="24" class="flex-row">
            <!-- ====== Cột trái: thông tin chung ====== -->
            <el-col :span="24" :md="10">
              <el-card class="section-card" shadow="hover">
                <template #header><div class="section-title">Thông tin chiến dịch</div></template>

                <el-form-item label="Tên đợt giảm giá" required>
                  <el-input v-model="form.name" placeholder="Ví dụ: Black Friday 2025" />
                </el-form-item>

                <el-form-item label="Giá trị giảm (%) (mặc định cho toàn chiến dịch)" required>
                  <el-input-number
                    v-model="form.discountPercentage"
                    :min="0"
                    :max="100"
                    :precision="2"
                    :step="0.5"
                    controls-position="right"
                    placeholder="%"
                    style="width: 100%"
                  />
                </el-form-item>

                <el-form-item label="Thời gian diễn ra" required>
                  <el-date-picker
                    v-model="dateRange"
                    type="datetimerange"
                    range-separator="Đến"
                    start-placeholder="Bắt đầu"
                    end-placeholder="Kết thúc"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    style="width: 100%"
                  />
                  <!-- FIX: định dạng ISO đúng -->
                </el-form-item>

                <el-form-item label="Ghi chú">
                  <el-input
                    v-model="form.description"
                    type="textarea"
                    :rows="5"
                    placeholder="Mô tả..."
                  />
                </el-form-item>

                <el-alert
                  type="info"
                  :closable="false"
                  show-icon
                  class="mt-2"
                  title="Mẹo"
                  description="Để trống % ở từng SPCT sẽ dùng % mặc định của chiến dịch."
                />
              </el-card>
            </el-col>

            <!-- ====== Cột phải: SẢN PHẨM ====== -->
            <el-col :span="24" :md="14">
              <el-card class="section-card" shadow="hover">
                <template #header><div class="section-title">Chọn Sản phẩm</div></template>

                <el-table
                  ref="productTableRef"
                  v-loading="loadingProducts"
                  :data="products"
                  border
                  stripe
                  size="small"
                  :row-key="(row) => row.id"
                  :height="320"
                  @selection-change="onProductSelectionChange"
                >
                  <el-table-column
                    type="selection"
                    width="44"
                    align="center"
                    :reserve-selection="true"
                  >
                    <template #header><span class="empty-header"></span></template>
                  </el-table-column>
                  <el-table-column type="index" label="#" width="56" align="center" />
                  <el-table-column
                    prop="productCode"
                    label="Mã"
                    width="140"
                    show-overflow-tooltip
                  />
                  <el-table-column
                    prop="productText"
                    label="Tên sản phẩm"
                    min-width="200"
                    show-overflow-tooltip
                  />
                  <el-table-column
                    prop="brandText"
                    label="Thương hiệu"
                    width="160"
                    show-overflow-tooltip
                  />
                  <el-table-column prop="quantity" label="Tồn" width="90" align="center" />
                </el-table>

                <div class="pagination-container">
                  <el-pagination
                    v-if="totalItems > 0"
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    :total="totalItems"
                    :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next"
                    @current-change="fetchProducts"
                    @size-change="onSizeChangeProducts"
                  />
                </div>
              </el-card>
            </el-col>

            <!-- ====== SPCT: full width ====== -->
            <el-col :span="24">
              <el-card class="section-card" shadow="hover">
                <template #header>
                  <div class="section-title">
                    Chọn Sản phẩm chi tiết (SPCT)
                    <span class="muted">— chọn từng dòng, không có “chọn tất cả”</span>
                  </div>
                </template>

                <el-table
                  ref="spctTableRef"
                  v-loading="loadingDetails"
                  :data="details"
                  border
                  stripe
                  size="small"
                  :row-key="(r) => r.id"
                  :height="520"
                  class="wide-table"
                  @selection-change="onDetailSelectionChange"
                >
                  <el-table-column
                    type="selection"
                    width="44"
                    align="center"
                    :reserve-selection="true"
                    :selectable="(r) => Number(r.quantity) > 0"
                  >
                    <template #header><span class="empty-header"></span></template>
                  </el-table-column>

                  <el-table-column type="index" label="#" width="56" align="center" />
                  <el-table-column
                    prop="productDetailCode"
                    label="Mã SPCT"
                    width="160"
                    show-overflow-tooltip
                  />
                  <el-table-column
                    prop="productText"
                    label="Sản phẩm"
                    min-width="220"
                    show-overflow-tooltip
                  />
                  <el-table-column
                    prop="brandText"
                    label="Thương hiệu"
                    width="160"
                    show-overflow-tooltip
                  />
                  <el-table-column prop="colorText" label="Màu" width="120" show-overflow-tooltip />
                  <el-table-column prop="sizeText" label="Size" width="90" align="center" />
                  <el-table-column prop="sellPrice" label="Giá bán" width="140">
                    <template #default="{ row }">{{ formatCurrency(row.sellPrice) }}</template>
                  </el-table-column>
                  <el-table-column prop="quantity" label="Tồn" width="90" align="center" />
                  <el-table-column label="% riêng" width="160" header-align="center">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="spctExtraPercent[row.id]"
                        :min="0"
                        :max="100"
                        :precision="2"
                        :step="0.5"
                        placeholder="%"
                        controls-position="right"
                        style="width: 140px"
                      />
                    </template>
                  </el-table-column>
                </el-table>

                <div class="pagination-container">
                  <el-pagination
                    v-if="detailsTotal > 0"
                    v-model:current-page="detailsPage"
                    v-model:page-size="detailsSize"
                    :total="detailsTotal"
                    :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next"
                    @current-change="fetchDetails"
                    @size-change="onSizeChangeDetails"
                  />
                </div>
              </el-card>
            </el-col>
          </el-row>

          <div class="footer-actions">
            <div class="left">
              <el-tag type="info" effect="plain"
                >SP đã chọn: <b>{{ totalSelectedProducts }}</b></el-tag
              >
              <el-tag type="warning" effect="plain"
                >SPCT đã chọn: <b>{{ totalSelectedDetails }}</b></el-tag
              >
            </div>
            <div class="right">
              <el-button @click="goBack" size="large">Quay lại</el-button>
              <el-button type="primary" @click="createCampaign" size="large"
                >Tạo mới đợt giảm giá</el-button
              >
            </div>
          </div>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>
<script setup>
import { reactive, ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import apiClient from '@/utils/axiosInstance'

const router = useRouter()

/** ======== STATE FORM ======== */
const form = reactive({
  campaignCode: '',
  name: '',
  discountPercentage: null,
  description: '',
  status: 1,
  startDate: '',
  endDate: '',
  products: [],
  productDetails: [],
})

/** ======== DATE RANGE (ISO "YYYY-MM-DDTHH:mm:ss") ======== */
const dateRange = computed({
  get() {
    return form.startDate && form.endDate ? [form.startDate, form.endDate] : []
  },
  set(val) {
    if (Array.isArray(val) && val.length === 2) {
      form.startDate = val[0]
      form.endDate = val[1]
    } else {
      form.startDate = ''
      form.endDate = ''
    }
  },
})

/** ========= Helpers ========= */
const fallbackId = (id) => (id !== undefined && id !== null ? `#${id}` : '')
const textOf = (obj, keys) => {
  for (const k of keys) {
    const v = k
      .split('.')
      .reduce((acc, kk) => (acc && acc[kk] !== undefined ? acc[kk] : undefined), obj)
    if (v !== undefined && v !== null && String(v).trim() !== '') return String(v)
  }
  return ''
}
const normalizeProduct = (row) => {
  const productText =
    textOf(row, ['productName', 'product.productName', 'name', 'product.name']) ||
    fallbackId(row.productId ?? row.id)
  const brandText =
    textOf(row, ['brandName', 'brand.brandName', 'brand.name']) || fallbackId(row.brandId)
  return { ...row, productText, brandText }
}

const normalizeDetail = (row) => {
  const productText =
    textOf(row, ['productName', 'product.productName', 'product.name']) || fallbackId(row.productId)
  const brandText =
    textOf(row, ['brandName', 'brand.brandName', 'brand.name']) || fallbackId(row.brandId)
  const colorText =
    textOf(row, ['colorName', 'color.colorName', 'color.name']) || fallbackId(row.colorId)
  const sizeText =
    textOf(row, ['sizeName', 'size.sizeName', 'size.name', 'sizeValue']) || fallbackId(row.sizeId)
  return { ...row, productText, brandText, colorText, sizeText }
}

/** ======== PRODUCTS ======== */
const productTableRef = ref()
const products = ref([])
const loadingProducts = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)
const selectedProductIds = ref(new Set())

const fetchProducts = async () => {
  loadingProducts.value = true
  try {
    const { data } = await apiClient.get('/admin/products', {
      params: { page: currentPage.value - 1, size: pageSize.value },
    })
    const content = data?.content || []
    products.value = content.map(normalizeProduct)
    totalItems.value = data?.page?.totalElements ?? data?.totalElements ?? content.length

    // giữ selection qua trang
    await nextTick()
    productTableRef.value?.clearSelection?.()
    products.value.forEach((row) => {
      if (selectedProductIds.value.has(row.id))
        productTableRef.value?.toggleRowSelection?.(row, true)
    })
  } catch (e) {
    console.error(e)
    ElMessage.error('Không thể tải danh sách sản phẩm.')
  } finally {
    loadingProducts.value = false
  }
}
const onSizeChangeProducts = () => {
  currentPage.value = 1
  fetchProducts()
}
const onProductSelectionChange = (rows) => {
  const currentIds = products.value.map((r) => r.id)
  currentIds.forEach((id) => selectedProductIds.value.delete(id))
  rows.forEach((r) => selectedProductIds.value.add(r.id))
}
const totalSelectedProducts = computed(() => selectedProductIds.value.size)

/** ======== SPCT ======== */
const spctTableRef = ref()
const details = ref([])
const loadingDetails = ref(false)
const detailsPage = ref(1)
const detailsSize = ref(10)
const detailsTotal = ref(0)
const selectedDetailIds = ref(new Set())
const spctExtraPercent = reactive({})

const fetchDetails = async () => {
  loadingDetails.value = true
  try {
    const { data } = await apiClient.get('/admin/products/details', {
      params: { page: detailsPage.value - 1, size: detailsSize.value },
    })
    const content = data?.content || []
    details.value = content.map(normalizeDetail)
    detailsTotal.value = data?.page?.totalElements ?? data?.totalElements ?? content.length

    // giữ selection qua trang
    await nextTick()
    spctTableRef.value?.clearSelection?.()
    details.value.forEach((row) => {
      if (selectedDetailIds.value.has(row.id) && Number(row.quantity) > 0) {
        spctTableRef.value?.toggleRowSelection?.(row, true)
      }
    })
  } catch (e) {
    console.error(e)
    ElMessage.error('Không thể tải SPCT.')
    details.value = []
    detailsTotal.value = 0
  } finally {
    loadingDetails.value = false
  }
}
const onSizeChangeDetails = () => {
  detailsPage.value = 1
  fetchDetails()
}
const onDetailSelectionChange = (rows) => {
  const currentIds = details.value.map((r) => r.id)
  currentIds.forEach((id) => selectedDetailIds.value.delete(id))
  rows.forEach((r) => selectedDetailIds.value.add(r.id))
}
const totalSelectedDetails = computed(() => selectedDetailIds.value.size)

/** ======== VALIDATE + SUBMIT ======== */
const validateBeforeSubmit = () => {
  if (
    !form.name?.trim() ||
    form.discountPercentage === null ||
    form.discountPercentage === '' ||
    !form.startDate ||
    !form.endDate
  ) {
    ElMessage.warning('Vui lòng điền đủ Tên, %, và Thời gian diễn ra.')
    return false
  }
  if (isNaN(Number(form.discountPercentage))) {
    ElMessage.warning('Giá trị % giảm không hợp lệ.')
    return false
  }
  if (new Date(form.startDate) >= new Date(form.endDate)) {
    ElMessage.error('Ngày bắt đầu phải trước ngày kết thúc.')
    return false
  }
  if (selectedProductIds.value.size === 0 && selectedDetailIds.value.size === 0) {
    ElMessage.warning('Hãy chọn ít nhất 1 Sản phẩm hoặc 1 SPCT.')
    return false
  }
  return true
}

const buildProductPayload = () =>
  Array.from(selectedProductIds.value).map((id) => ({ productId: id }))
const buildProductDetailsPayload = () => {
  const list = []
  selectedDetailIds.value.forEach((id) => {
    const percent = spctExtraPercent[id]
    if (percent !== undefined && percent !== null && percent !== '') {
      list.push({ productDetailId: id, discountPercentage: Number(percent) })
    } else {
      list.push({ productDetailId: id })
    }
  })
  return list
}

const sanitize = (obj) => {
  const out = {}
  Object.entries(obj).forEach(([k, v]) => {
    if (v === '' || v === undefined) return
    out[k] = v
  })
  return out
}

const createCampaign = async () => {
  if (!validateBeforeSubmit()) return
  const payload = sanitize({
    campaignCode: form.campaignCode?.trim() || null,
    name: form.name.trim(),
    discountPercentage: Number(form.discountPercentage),
    description: form.description?.trim() || null,
    status: form.status ?? 1,
    startDate: form.startDate, // "YYYY-MM-DDTHH:mm:ss"
    endDate: form.endDate,
    products: buildProductPayload(),
    productDetails: buildProductDetailsPayload(),
  })

  try {
    await apiClient.post('/admin/campaigns', payload, {
      headers: { 'Content-Type': 'application/json' },
    })
    ElMessage.success('Tạo đợt giảm giá thành công!')
    router.back()
  } catch (e) {
    console.error('Create campaign error:', {
      url: '/admin/campaigns',
      status: e?.response?.status,
      data: e?.response?.data,
      payload,
    })
    ElMessage.error(
      e?.response?.data?.message ||
        `Không thể tạo đợt giảm giá (HTTP ${e?.response?.status || 'ERR'}).`,
    )
  }
}

/** ======== INIT ======== */
onMounted(() => {
  fetchProducts()
  fetchDetails()
})

const goBack = () => router.back()
const formatCurrency = (v) =>
  v === null || v === undefined ? '' : Number(v).toLocaleString('vi-VN')
</script>

<style scoped>
.page-container {
  padding: 20px;
  background-color: #f5f7fb;
  min-height: 100vh;
}
.form-card {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  border-radius: 14px;
  overflow: hidden;
}
:deep(.el-card__header) {
  background: linear-gradient(90deg, #f8fafc, #eef2ff);
}
.card-header {
  text-align: left;
}
.card-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}
.subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.section-card {
  border-radius: 12px;
}
.section-title {
  font-weight: 600;
  color: #111827;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title .muted {
  color: #6b7280;
  font-weight: 400;
  font-size: 12px;
}

.flex-row {
  display: flex;
  flex-wrap: wrap;
}

:deep(.el-form-item) {
  margin-bottom: 14px;
}
:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 10px;
}

.empty-header {
  display: inline-block;
  width: 12px;
  height: 12px;
} /* che checkbox header */

.wide-table :deep(.el-table__inner-wrapper) {
  border-radius: 8px;
}
:deep(.el-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-tr-bg-color: #fff;
}
:deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 2;
}
:deep(.el-table .cell) {
  line-height: 20px;
}

.pagination-container {
  padding-top: 10px;
  display: flex;
  justify-content: center;
}

.footer-actions {
  position: sticky;
  bottom: 0;
  z-index: 5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  padding: 12px 16px;
  margin-top: 16px;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}
.footer-actions .left .el-tag {
  margin-right: 8px;
}
</style>

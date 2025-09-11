<template>
  <el-container class="page-container">
    <el-main>
      <el-card class="wizard-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h1>Cập nhật Đợt Giảm Giá</h1>
            <p class="subtitle">Chỉnh sửa thông tin, chọn sản phẩm/SPCT, xem lại và lưu cập nhật</p>
          </div>
        </template>

        <!-- ====== STEPS ====== -->
        <el-steps :active="activeStep" finish-status="success" align-center class="wizard-steps">
          <el-step title="Thiết lập thông tin" />
          <el-step title="Chọn sản phẩm & SPCT" />
          <el-step title="Xem lại & Hoàn tất" />
        </el-steps>

        <el-form :model="form" label-position="top" @submit.prevent>
          <!-- ====== STEP 1 ====== -->
          <section v-show="activeStep === 0" class="step-content">
            <el-row :gutter="24" justify="center">
              <el-col :span="24" :md="16">
                <h3 class="step-title">1) Thông tin chung</h3>

                <el-skeleton v-if="loadingCampaign" :rows="6" animated />

                <template v-else>
                  <el-form-item label="Tên đợt giảm giá" required>
                    <el-input v-model="form.name" placeholder="VD: Summer Sale 2025" size="large" />
                  </el-form-item>

                  <el-row :gutter="16">
                    <el-col :span="24" :sm="12">
                      <el-form-item label="Giá trị giảm (%)" required>
                        <el-input-number
                          v-model="form.discountPercentage"
                          :min="1"
                          :max="99"
                          :step="1"
                          controls-position="right"
                          size="large"
                          style="width: 100%"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-form-item label="Thời gian diễn ra" required>
                    <el-date-picker
                      v-model="dateRange"
                      type="datetimerange"
                      range-separator="Đến"
                      start-placeholder="Bắt đầu"
                      end-placeholder="Kết thúc"
                      format="YYYY-MM-DD HH:mm:ss"
                      value-format="YYYY-MM-DDTHH:mm:ss"
                      size="large"
                      style="width: 100%"
                    />
                  </el-form-item>

                  <el-form-item label="Mô tả / Ghi chú">
                    <el-input v-model="form.description" type="textarea" :rows="4" placeholder="Ghi chú quản trị..." />
                  </el-form-item>
                </template>
              </el-col>
            </el-row>
          </section>

          <!-- ====== STEP 2 ====== -->
          <section v-show="activeStep === 1" class="step-content">
            <h3 class="step-title">2) Chọn sản phẩm & SPCT</h3>
            <p class="step-description">Tìm kiếm sản phẩm, chọn các SP cần áp dụng. SPCT sẽ lọc theo SP đã chọn.</p>

            <!-- PRODUCTS -->
            <el-card class="table-card" shadow="none">
              <template #header>
                <div class="table-card-header">
                  <div class="title">Danh sách sản phẩm</div>
                  <el-input
                    v-model="productSearch"
                    placeholder="Tìm theo tên hoặc mã sản phẩm..."
                    clearable
                    @input="debouncedFetchProducts"
                    style="max-width: 340px"
                  />
                </div>
              </template>

              <el-table
                ref="productTableRef"
                v-loading="loadingProducts"
                :data="products"
                border
                stripe
                height="320"
                :row-key="row => row.id"
                @row-click="onProductRowClick"
                @selection-change="onProductSelectionChange"
              >
                <el-table-column type="selection" width="50" align="center" :reserve-selection="true" />
                <el-table-column type="index" label="#" width="60" align="center" />
                <el-table-column label="Sản phẩm" min-width="280">
                  <template #default="{ row }">
                    <div class="product-cell">
                      <div class="product-name">{{ row.productText }}</div>
                      <div class="product-code">Mã: {{ row.productCode || '-' }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="brandText" label="Thương hiệu" width="180" show-overflow-tooltip />
                <el-table-column prop="quantity" label="Tổng tồn" width="110" align="center" />
              </el-table>

              <el-pagination
                v-if="totalItems > 0"
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="totalItems"
                :page-sizes="[10,20,50,100]"
                layout="total, sizes, prev, pager, next"
                @current-change="fetchProducts"
                @size-change="onSizeChangeProducts"
                class="table-pagination"
              />
            </el-card>

            <!-- DETAILS -->
            <el-card class="table-card" shadow="none" style="margin-top: 16px">
              <template #header>
                <div class="table-card-header">
                  <div class="title">Sản phẩm chi tiết (SPCT)
                    <span class="muted-text">— chỉ hiển thị theo SP đã chọn</span>
                  </div>
                </div>
              </template>

              <el-alert
                v-if="selectedProductIds.length === 0"
                type="info"
                :closable="false"
                show-icon
                class="mb-2"
                title="Hãy chọn ít nhất 1 sản phẩm ở bảng trên để hiển thị SPCT."
              />

              <div v-else>
                <el-table
                  ref="spctTableRef"
                  v-loading="loadingDetails"
                  :data="details"
                  border
                  stripe
                  height="420"
                  :row-key="r => r.id"
                  @selection-change="onDetailSelectionChange"
                >
                  <el-table-column
                    type="selection"
                    width="50"
                    align="center"
                    :reserve-selection="true"
                    :selectable="r => Number(r.quantity) > 0"
                  />
                  <el-table-column type="index" width="60" align="center" label="#" />
                  <el-table-column prop="productDetailCode" label="Mã SPCT" width="160" show-overflow-tooltip />
                  <el-table-column prop="productText" label="Sản phẩm" min-width="220" show-overflow-tooltip />
                  <el-table-column prop="brandText" label="Thương hiệu" width="180" show-overflow-tooltip />
                  <el-table-column label="Thuộc tính" width="200">
                    <template #default="{ row }">
                      <el-tag size="small" effect="plain" class="attribute-tag">Màu: {{ row.colorText }}</el-tag>
                      <el-tag size="small" effect="plain" class="attribute-tag">Size: {{ row.sizeText }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="Giá bán" width="140" align="right">
                    <template #default="{ row }">{{ formatCurrency(row.sellPrice) }}</template>
                  </el-table-column>
                  <el-table-column prop="quantity" label="Tồn" width="90" align="center" />
                  <el-table-column label="% riêng" width="160" header-align="center">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="spctExtraPercent[row.id]"
                        :min="1"
                        :max="99"
                        :step="1"
                        placeholder="Mặc định"
                        controls-position="right"
                        style="width: 130px"
                      />
                    </template>
                  </el-table-column>
                </el-table>

                <el-pagination
                  v-if="detailsTotal > 0"
                  v-model:current-page="detailsPage"
                  v-model:page-size="detailsSize"
                  :total="detailsTotal"
                  :page-sizes="[10,20,50,100]"
                  layout="total, sizes, prev, pager, next"
                  @current-change="fetchDetails"
                  @size-change="onSizeChangeDetails"
                  class="table-pagination"
                />
              </div>
            </el-card>
          </section>

          <!-- ====== STEP 3 ====== -->
          <section v-show="activeStep === 2" class="step-content">
            <h3 class="step-title">3) Xem lại & Hoàn tất</h3>
            <p class="step-description">Bạn có thể chỉnh <b>thời gian diễn ra</b> ngay tại bước này nếu lỡ nhập sai.</p>

            <!-- Quick edit time range on Step 3 -->
            <el-card class="table-card" shadow="none" style="margin-bottom: 16px;">
              <template #header>
                <div class="table-card-header">
                  <div class="title">Chỉnh sửa nhanh thời gian diễn ra</div>
                </div>
              </template>
              <el-row :gutter="16">
                <el-col :span="24" :md="16">
                  <el-form-item label="Thời gian diễn ra" required>
                    <el-date-picker
                      v-model="dateRangeStep3"
                      type="datetimerange"
                      range-separator="Đến"
                      start-placeholder="Bắt đầu"
                      end-placeholder="Kết thúc"
                      format="YYYY-MM-DD HH:mm:ss"
                      value-format="YYYY-MM-DDTHH:mm:ss"
                      size="large"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-alert v-if="dateError" :title="dateError" type="error" show-icon :closable="false" />
            </el-card>

            <el-descriptions :column="2" border class="review-descriptions">
              <el-descriptions-item label="Tên chiến dịch">{{ form.name }}</el-descriptions-item>
              <el-descriptions-item label="Trạng thái">
                <el-tag :type="form.status === 1 ? 'success' : 'warning'">
                  {{ form.status === 1 ? 'Đang hoạt động' : 'Tạm ngưng' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Giảm mặc định">
                <el-tag type="danger">{{ form.discountPercentage }}%</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Mô tả">{{ form.description || 'Không có' }}</el-descriptions-item>
              <el-descriptions-item label="Bắt đầu" :span="1">{{ form.startDate }}</el-descriptions-item>
              <el-descriptions-item label="Kết thúc" :span="1">{{ form.endDate }}</el-descriptions-item>
            </el-descriptions>

            <el-divider />

            <div class="selection-summary">
              <h4>Đối tượng áp dụng</h4>
              <div class="summary-items">
                <div class="summary-item">
                  <div class="count">{{ totalSelectedProducts }}</div>
                  <div class="label">Sản phẩm</div>
                </div>
                <div class="summary-item">
                  <div class="count">{{ totalSelectedDetails }}</div>
                  <div class="label">SPCT</div>
                </div>
              </div>
              <el-alert type="info" :closable="false" show-icon class="mt-4"
                        :title="`SPCT không đặt % riêng sẽ tự áp dụng ${form.discountPercentage}%`" />
            </div>
          </section>
        </el-form>

        <!-- ====== FOOTER ====== -->
        <div class="footer-actions">
          <div>
            <el-button v-if="activeStep > 0" @click="prevStep" size="large">Quay lại</el-button>
          </div>
          <div>
            <el-button v-if="activeStep < 2" type="primary" @click="nextStep" size="large">Tiếp theo</el-button>
            <el-button v-if="activeStep === 2" type="success" @click="updateCampaign" size="large">Lưu cập nhật</el-button>
          </div>
        </div>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { reactive, ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import apiClient from '@/utils/axiosInstance'
import { debounce } from 'lodash-es'

const router = useRouter()
const route = useRoute()
const campaignId = route.params.id

/* ====== Steps ====== */
const activeStep = ref(0)
const nextStep = () => {
  if (activeStep.value === 0) {
    if (!form.name?.trim() || form.discountPercentage === '' || form.discountPercentage === null || !form.startDate || !form.endDate) {
      ElMessage.warning('Vui lòng điền đủ thông tin ở bước 1.')
      return
    }
  }
  if (activeStep.value === 1 && selectedProductIds.value.length === 0) {
    ElMessage.warning('Bạn phải chọn ít nhất 1 sản phẩm.')
    return
  }
  if (activeStep.value < 2) activeStep.value++
}
const prevStep = () => { if (activeStep.value > 0) activeStep.value-- }

/* ====== Form ====== */
const form = reactive({
  name: '',
  discountPercentage: '',
  description: '',
  status: 1,
  startDate: '',
  endDate: ''
})

/* ====== Date range binding ====== */
const dateRange = computed({
  get() { return form.startDate && form.endDate ? [form.startDate, form.endDate] : [] },
  set(v) {
    if (Array.isArray(v) && v.length === 2) { [form.startDate, form.endDate] = v }
    else { form.startDate = ''; form.endDate = '' }
  },
})
const dateRangeStep3 = computed({
  get() { return dateRange.value },
  set(v) { dateRange.value = v },
})
const dateError = ref('')

/* ====== Helpers ====== */
const fallbackId = (id) => (id !== undefined && id !== null ? `#${id}` : '')
const textOf = (obj, keys) => {
  for (const k of keys) {
    const v = k.split('.').reduce((acc, kk) => (acc && acc[kk] !== undefined ? acc[kk] : undefined), obj)
    if (v !== undefined && v !== null && String(v).trim() !== '') return String(v)
  }
  return ''
}
const normalizeProduct = (row) => {
  const productText =
    textOf(row, ['productText']) ||
    textOf(row, ['productName','name']) ||
    textOf(row, ['product.productName','product.name']) ||
    fallbackId(row.id)
  const brandText =
    textOf(row, ['brandText']) ||
    textOf(row, ['brandName']) ||
    textOf(row, ['brand.brandName','brand.name']) ||
    textOf(row, ['product.brand.brandName','product.brand.name']) ||
    fallbackId(row.brandId)
  return { ...row, productText, brandText }
}
const normalizeDetail = (row) => {
  const productText =
    textOf(row, ['productText']) ||
    textOf(row, ['productName','product.productName','product.name']) ||
    fallbackId(row.productId)
  const brandText =
    textOf(row, ['brandText']) ||
    textOf(row, ['brandName']) ||
    textOf(row, ['brand.brandName','brand.name']) ||
    textOf(row, ['product.brand.brandName','product.brand.name']) ||
    fallbackId(row.brandId)
  const colorText =
    textOf(row, ['colorText']) ||
    textOf(row, ['colorName','color.colorName','color.name']) ||
    fallbackId(row.colorId)
  const sizeText =
    textOf(row, ['sizeText']) ||
    textOf(row, ['sizeName','size.sizeName','size.name','sizeValue']) ||
    fallbackId(row.sizeId)
  return { ...row, productText, brandText, colorText, sizeText }
}

/* ====== Products (SP) ====== */
const productTableRef = ref()
const products = ref([])
const loadingProducts = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)
const selectedProductIds = ref([]) // array for reactivity with table
const productSearch = ref('')

const totalFrom = (data, contentLength) => data?.page?.totalElements ?? data?.totalElements ?? contentLength ?? 0

const fetchProducts = async () => {
  loadingProducts.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value, keyword: productSearch.value || undefined }
    const { data } = await apiClient.get('/admin/products', { params })
    const content = data?.content || []
    products.value = content.map(normalizeProduct)
    totalItems.value = totalFrom(data, content.length)

    await nextTick()
    productTableRef.value?.clearSelection?.()
    products.value.forEach(row => {
      if (selectedProductIds.value.includes(row.id)) {
        productTableRef.value?.toggleRowSelection?.(row, true)
      }
    })
  } catch (e) {
    console.error(e)
    ElMessage.error('Không thể tải danh sách sản phẩm.')
    products.value = []
    totalItems.value = 0
  } finally {
    loadingProducts.value = false
  }
}
const debouncedFetchProducts = debounce(() => { currentPage.value = 1; fetchProducts() }, 400)
const onSizeChangeProducts = () => { currentPage.value = 1; fetchProducts() }
const onProductRowClick = (row) => { productTableRef.value?.toggleRowSelection?.(row) }
const onProductSelectionChange = (rows) => {
  selectedProductIds.value = rows.map(r => r.id)
  detailsPage.value = 1
  fetchDetails()
}
const totalSelectedProducts = computed(() => selectedProductIds.value.length)

/* ====== Product Details (SPCT) ====== */
const spctTableRef = ref()
const details = ref([])
const loadingDetails = ref(false)
const detailsPage = ref(1)
const detailsSize = ref(10)
const detailsTotal = ref(0)
const selectedDetailIds = ref([])
const spctExtraPercent = reactive({})

const paramsSerializerRepeat = (params) => {
  const usp = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (Array.isArray(v)) v.forEach(x => usp.append(k, x))
    else if (v !== undefined && v !== null && v !== '') usp.append(k, v)
  })
  return usp.toString()
}

const fetchDetails = async () => {
  const ids = selectedProductIds.value
  if (!ids.length) { details.value = []; detailsTotal.value = 0; return }

  loadingDetails.value = true
  try {
    const { data } = await apiClient.get('/admin/products/details', {
      params: { page: detailsPage.value - 1, size: detailsSize.value, productIds: ids },
      paramsSerializer: { serialize: paramsSerializerRepeat },
    })
    const content = data?.content || []
    details.value = content.map(normalizeDetail)
    detailsTotal.value = totalFrom(data, content.length)

    await nextTick()
    spctTableRef.value?.clearSelection?.()
    details.value.forEach(row => {
      if (selectedDetailIds.value.includes(row.id) && Number(row.quantity) > 0) {
        spctTableRef.value?.toggleRowSelection?.(row, true)
      }
      // giữ lại % riêng đã nạp sẵn
      if (spctExtraPercent[row.id] !== undefined && spctExtraPercent[row.id] !== null) {
        spctExtraPercent[row.id] = Number(spctExtraPercent[row.id])
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
const onSizeChangeDetails = () => { detailsPage.value = 1; fetchDetails() }
const onDetailSelectionChange = (rows) => { selectedDetailIds.value = rows.map(r => r.id) }
const totalSelectedDetails = computed(() => selectedDetailIds.value.length)

/* ====== LOAD CAMPAIGN ====== */
const loadingCampaign = ref(true)
const normalizeIso = (s) => (!s ? '' : (typeof s === 'string' ? s.replace(' ', 'T') : s))

const fetchCampaign = async () => {
  loadingCampaign.value = true
  try {
    const { data } = await apiClient.get(`/admin/campaigns/${campaignId}`)
    form.name               = data?.name ?? ''
    form.discountPercentage = data?.discountPercentage ?? ''
    form.description        = data?.description ?? ''
    form.status             = data?.status ?? 1
    form.startDate          = normalizeIso(data?.startDate) || ''
    form.endDate            = normalizeIso(data?.endDate) || ''

    // preselect products
    selectedProductIds.value = []
    const prodLinks = data?.products || data?.productLinks || []
    prodLinks.forEach(link => {
      const pid = link?.product?.id ?? link?.productId
      if (pid != null) selectedProductIds.value.push(pid)
    })

    // preselect product details + per-item %
    selectedDetailIds.value = []
    Object.keys(spctExtraPercent).forEach(k => delete spctExtraPercent[k])
    const pdLinks = data?.productDetails || data?.productDetailLinks || []
    pdLinks.forEach(link => {
      const pdId = link?.productDetail?.id ?? link?.productDetailId
      if (pdId != null) {
        selectedDetailIds.value.push(pdId)
        const pct = link?.discountPercentage
        if (pct !== null && pct !== undefined && pct !== '') spctExtraPercent[pdId] = Number(pct)
      }
    })
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.response?.data?.message || 'Không thể tải thông tin đợt giảm giá.')
    router.back()
  } finally {
    loadingCampaign.value = false
  }
}

/* ====== VALIDATE & SUBMIT ====== */
const validateFinal = () => {
  dateError.value = ''
  if (!form.name?.trim() || form.discountPercentage === '' || form.discountPercentage === null) {
    ElMessage.error('Thiếu tên chiến dịch hoặc % giảm.')
    if (activeStep.value === 0) return false
  }
  if (!form.startDate || !form.endDate) {
    dateError.value = 'Vui lòng chọn đầy đủ thời gian bắt đầu và kết thúc.'
    return false
  }
  const now = new Date()
  const start = new Date(form.startDate)
  const end = new Date(form.endDate)
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  if (start < today) { dateError.value = 'Ngày bắt đầu không được ở quá khứ.'; return false }
  if (start >= end) { dateError.value = 'Ngày kết thúc phải sau ngày bắt đầu.'; return false }
  if (selectedProductIds.value.length === 0 && selectedDetailIds.value.length === 0) {
    ElMessage.warning('Chọn ít nhất 1 SP hoặc 1 SPCT.')
    if (activeStep.value === 1) return false
  }
  return true
}

const buildPayload = () => {
  const productDetailsPayload = selectedDetailIds.value.map(id => {
    const percent = spctExtraPercent[id]
    return {
      productDetailId: id,
      ...(percent !== undefined && percent !== null && percent !== '' && { discountPercentage: Number(percent) }),
    }
  })
  return {
    name: form.name.trim(),
    discountPercentage: Number(form.discountPercentage),
    description: form.description?.trim() || null,
    status: form.status,
    startDate: form.startDate,
    endDate: form.endDate,
    products: selectedProductIds.value.map(id => ({ productId: id })),
    productDetails: productDetailsPayload,
  }
}

const updateCampaign = async () => {
  if (!validateFinal()) return
  const payload = buildPayload()
  try {
    await apiClient.put(`/admin/campaigns/${campaignId}`, payload, { headers: { 'Content-Type': 'application/json' } })
    ElMessage.success('Cập nhật đợt giảm giá thành công!')
    router.push('/discount-campaigns')
  } catch (e) {
    console.error('Update campaign error:', { status: e?.response?.status, data: e?.response?.data, payload })
    ElMessage.error(e?.response?.data?.message || `Không thể cập nhật đợt giảm giá (HTTP ${e?.response?.status || 'ERR'}).`)
  }
}

/* ====== Init ====== */
onMounted(async () => {
  await fetchCampaign()
  fetchProducts()
  // nạp SPCT theo SP đã preselect
  if (selectedProductIds.value.length) fetchDetails()
})

/* ====== Format ====== */
const formatCurrency = (v) => (v === null || v === undefined ? '' : Number(v).toLocaleString('vi-VN'))
</script>

<style scoped>
/* Layout */
.page-container { background: #f5f7fb; min-height: 100vh; padding: 20px; }
.wizard-card { width: 100%; max-width: 1440px; margin: 0 auto; border-radius: 12px; }
:deep(.el-card__header) { background: #fff; }
.card-header h1 { margin: 0; font-size: 20px; font-weight: 700; }
.card-header .subtitle { margin: 4px 0 0; color: var(--el-text-color-secondary); font-size: 13px; }

/* Steps */
.wizard-steps { padding: 16px 0; margin: 0 16px 12px; border-bottom: 1px solid var(--el-border-color-lighter); }
:deep(.el-step__title) { font-size: 14px; }

/* Content */
.step-content { padding: 10px 16px; }
.step-title { font-size: 18px; font-weight: 600; margin-bottom: 6px; color: var(--el-text-color-primary); }
.step-description { font-size: 13px; color: var(--el-text-color-secondary); margin-bottom: 16px; }

/* Tables */
.table-card { border: 1px solid var(--el-border-color-lighter); border-radius: 12px; }
:deep(.table-card .el-card__header) { background: #fcfcfd; padding: 12px 16px; }
:deep(.table-card .el-card__body) { padding: 8px; }
.table-card-header { display: flex; justify-content: space-between; align-items: center; }
.table-card-header .title { font-weight: 600; font-size: 16px; }
.muted-text { font-weight: 400; font-size: 13px; color: var(--el-text-color-secondary); margin-left: 6px; }
.product-cell { display: flex; flex-direction: column; }
.product-name { font-weight: 500; color: var(--el-text-color-primary); }
.product-code { font-size: 12px; color: var(--el-text-color-secondary); }
.attribute-tag { margin-right: 6px; margin-top: 4px; }
.table-pagination { padding: 10px 0 0; display: flex; justify-content: center; }

/* Review */
.review-descriptions { border-radius: 8px; }
.selection-summary { padding: 16px; background: #f9fafb; border-radius: 12px; }
.selection-summary h4 { margin: 0 0 12px; font-size: 16px; font-weight: 600; }
.summary-items { display: flex; gap: 24px; text-align: center; }
.summary-item .count { font-size: 26px; font-weight: 700; color: var(--el-color-primary); }
.summary-item .label { font-size: 13px; color: var(--el-text-color-secondary); }
.mt-4 { margin-top: 16px; }

/* Footer */
.footer-actions { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; border-top: 1px solid var(--el-border-color-lighter); background: rgba(255,255,255,0.9); position: sticky; bottom: 0; z-index: 10; }
.summary-tags { display: flex; gap: 10px; }
</style>

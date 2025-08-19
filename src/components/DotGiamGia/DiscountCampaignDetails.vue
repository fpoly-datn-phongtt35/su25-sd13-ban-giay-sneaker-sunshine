<template>
  <el-container class="campaign-details-container">
    <!-- Header / breadcrumb -->
    <el-header class="cd-header">
      <div class="cd-header-left">
        <el-button link @click="goBack">← Quay lại</el-button>
        <h1 class="cd-title">Chi tiết đợt giảm giá</h1>
      </div>
      <div class="cd-header-right">
        <el-tag v-if="campaign" :type="statusTagType(campaign.status)">
          {{ statusText(campaign.status) }}
        </el-tag>
        <el-button type="primary" @click="refresh">Làm mới</el-button>
      </div>
    </el-header>

    <el-main>
      <!-- Loading -->
      <el-skeleton v-if="loading" animated :rows="6" />

      <!-- Error -->
      <el-alert v-else-if="error" :title="error" type="error" show-icon class="mb-4" />

      <!-- Content -->
      <div v-else-if="campaign">
        <!-- Thông tin tổng quan -->
        <el-card class="mb-4" shadow="never">
          <template #header>
            <div class="card-header">
              <span>Thông tin đợt giảm giá</span>
              <el-tag type="info">ID: {{ campaign.id }}</el-tag>
            </div>
          </template>

          <el-descriptions :column="3" border>
            <el-descriptions-item label="Tên">{{ campaign.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Mã">{{ campaign.campaignCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Giảm giá">
              <el-tag type="success">{{ (campaign.discountPercentage ?? 0) }}%</el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="Thời gian" :span="2">
              {{ formatDateTime(campaign.startDate) }} → {{ formatDateTime(campaign.endDate) }}
            </el-descriptions-item>
            <el-descriptions-item label="Trạng thái">
              <el-tag :type="statusTagType(campaign.status)">{{ statusText(campaign.status) }}</el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="Mô tả" :span="3">
              {{ campaign.description || '—' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Thống kê -->
        <el-card v-if="stats" class="mb-4" shadow="never">
          <template #header><div class="card-header">Thống kê</div></template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-label">Số hóa đơn</div>
              <div class="stat-value">{{ stats.totalInvoices ?? '-' }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">Doanh thu sau giảm</div>
              <div class="stat-value">
                {{ stats.totalAfterDiscount != null ? formatCurrency(stats.totalAfterDiscount) : '-' }}
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-label">Sản phẩm bán ra</div>
              <div class="stat-value">{{ stats.totalProductsSold ?? '-' }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">Tỉ lệ giảm TB</div>
              <div class="stat-value">
                {{ stats.averageDiscountRate != null ? stats.averageDiscountRate + '%' : '-' }}
              </div>
            </div>
          </div>
        </el-card>

        <!-- Danh sách SP & SPCT -->
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>Dữ liệu đợt giảm giá</span>
            </div>
          </template>

          <el-tabs v-model="activeTab" class="cd-tabs">
            <!-- TAB SẢN PHẨM -->
            <el-tab-pane name="products" label="Sản phẩm">
              <div class="toolbar">
         
              </div>

              <el-table
                :data="pagedProducts"
                border
                stripe
                highlight-current-row
                :fit="true"
                empty-text="Chưa có sản phẩm nào trong đợt này"
              >
                <el-table-column type="index" label="STT" width="70" align="center" />
                <el-table-column prop="productId" label="Mã SP" width="120" />
                <el-table-column prop="productName" label="Tên sản phẩm" min-width="240" show-overflow-tooltip />
              </el-table>

              <div class="table-pagination">
                <el-pagination
                  background
                  layout="total, prev, pager, next, sizes"
                  :total="filteredProducts.length"
                  :current-page="prodPage"
                  :page-size="prodPageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  @current-change="(p)=>{ prodPage = p; }"
                  @size-change="(s)=>{ prodPageSize = s; prodPage = 1; }"
                />
              </div>
            </el-tab-pane>

            <!-- TAB SẢN PHẨM CHI TIẾT -->
            <el-tab-pane name="productDetails" label="Sản phẩm chi tiết">
              <div class="toolbar">

              </div>

              <el-table
                :data="pagedDetails"
                border
                stripe
                highlight-current-row
                :fit="true"
                empty-text="Chưa có SPCT nào trong đợt này"
              >
                <el-table-column type="index" label="STT" width="70" align="center" />
                <el-table-column prop="id" label="ID SPCT" width="120" />
                <el-table-column prop="productName" label="Tên sản phẩm" min-width="220" show-overflow-tooltip />
                <el-table-column prop="sku" label="SKU / Mã biến thể" min-width="160" show-overflow-tooltip />
                <el-table-column prop="colorName" label="Màu" width="110" />
                <el-table-column prop="sizeName" label="Size" width="90" align="center" />
                <el-table-column label="Giá bán" width="140" align="right">
                  <template #default="{ row }">
                    {{ formatCurrency(row.sellPrice ?? row.price ?? 0) }}
                  </template>
                </el-table-column>
                <el-table-column label="Giảm (%)" width="110" align="center">
                  <template #default="{ row }">
                    {{ row.discountPercentage ?? campaign.discountPercentage ?? 0 }}%
                  </template>
                </el-table-column>
                <el-table-column label="Tồn kho" width="110" align="center">
                  <template #default="{ row }">{{ row.quantity ?? '-' }}</template>
                </el-table-column>
              </el-table>

              <div class="table-pagination">
                <el-pagination
                  background
                  layout="total, prev, pager, next, sizes"
                  :total="filteredDetails.length"
                  :current-page="detailPage"
                  :page-size="detailPageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  @current-change="(p)=>{ detailPage = p; }"
                  @size-change="(s)=>{ detailPageSize = s; detailPage = 1; }"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const campaign = ref(null)
const stats = ref(null)
const loading = ref(true)
const error = ref('')
const activeTab = ref('products')

/* =========================
   SP – tìm kiếm & phân trang
   ========================= */
const kwProduct = ref('')
let prodPage = ref(1)
let prodPageSize = ref(10)

/* =========================
   SPCT – tìm kiếm & phân trang
   ========================= */
const kwDetail = ref('')
let detailPage = ref(1)
let detailPageSize = ref(10)

/* ===== Helpers ===== */
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const safe = typeof dateStr === 'string' ? dateStr.replace(' ', 'T') : dateStr
  const date = new Date(safe)
  if (isNaN(date.getTime())) return String(dateStr)
  return date.toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
const formatCurrency = (val) => {
  try { return Number(val ?? 0).toLocaleString('vi-VN') + ' đ' } catch { return '-' }
}
const statusText = (s) => (s===0?'Sắp diễn ra':s===1?'Đang hoạt động':s===2?'Đã kết thúc':'Không xác định')
const statusTagType = (s) => (s===0?'warning':s===1?'success':s===2?'info':'')

/* ===== Điều hướng / hành động ===== */
const goBack = () => router.back()
const goToProduct = (productId) => { if (productId) router.push({ path: `/products/${productId}` }) }
const goToDetail = (row) => {
  // Nếu bạn có route riêng cho SPCT, điều hướng tại đây. Ví dụ:
  // router.push({ path: `/product-details/${row.id}` })
  ElMessage.info('Mở chi tiết SPCT (hãy gắn route thực tế của bạn).')
}
const refresh = () => { loadCampaign(); loadStats() }
const printItem = (type, row) => {
  // Placeholder: thay bằng in report / modal tùy nhu cầu
  window.print()
}

/* ===== Nguồn dữ liệu: ưu tiên từ campaign =====
   BE của bạn có thể trả về:
   campaign.products = [{ productId, productName, ... }]
   campaign.productDetails = [{ id, sku, productName, colorName, sizeName, price/sellPrice, discountPercentage, quantity, ... }]
*/
const products = computed(() => {
  if (!campaign.value) return []
  const list = campaign.value.products || []
  return Array.isArray(list) ? list : []
})
const productDetails = computed(() => {
  if (!campaign.value) return []
  const list = campaign.value.productDetails || campaign.value.details || []
  return Array.isArray(list) ? list : []
})

/* ===== Filter SP ===== */
const filteredProducts = computed(() => {
  const kw = kwProduct.value.trim().toLowerCase()
  if (!kw) return products.value
  return products.value.filter(p => (p.productName || '').toLowerCase().includes(kw))
})
const pagedProducts = computed(() => {
  const start = (prodPage.value - 1) * prodPageSize.value
  return filteredProducts.value.slice(start, start + prodPageSize.value)
})
const applyProductFilter = () => { prodPage.value = 1 }

/* ===== Filter SPCT ===== */
const filteredDetails = computed(() => {
  const kw = kwDetail.value.trim().toLowerCase()
  if (!kw) return productDetails.value
  return productDetails.value.filter(d =>
    ((d.productName || '').toLowerCase().includes(kw)) ||
    ((d.sku || '').toLowerCase().includes(kw))
  )
})
const pagedDetails = computed(() => {
  const start = (detailPage.value - 1) * detailPageSize.value
  return filteredDetails.value.slice(start, start + detailPageSize.value)
})
const applyDetailFilter = () => { detailPage.value = 1 }

/* ===== API ===== */
const loadCampaign = async () => {
  loading.value = true
  error.value = ''
  try {
    const id = route.params.id
    const res = await axios.get(`http://localhost:8080/api/admin/campaigns/${id}`)
    campaign.value = res.data
    // reset phân trang khi đổi dữ liệu
    prodPage.value = 1
    detailPage.value = 1
  } catch (e) {
    console.error(e)
    error.value = 'Không thể tải chi tiết đợt giảm giá.'
  } finally {
    loading.value = false
  }
}
const loadStats = async () => {
  try {
    const id = route.params.id
    const res = await axios.get(`http://localhost:8080/api/admin/campaigns/${id}/statistics`)
    stats.value = res.data
  } catch {
    stats.value = null
  }
}

/* ===== Lifecycle ===== */
onMounted(() => { loadCampaign(); loadStats() })
watch(() => route.params.id, () => { refresh() })
</script>

<style scoped>
/* Header */
.cd-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin-bottom: 8px;
}
.cd-header-left { display: flex; align-items: center; gap: 10px; }
.cd-title { margin: 0; font-size: 22px; font-weight: 700; }

/* Card header */
.card-header {
  display: flex; justify-content: space-between; align-items: center;
  font-weight: 600;
}

/* Stats */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
}
.stat-item {
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-fill-color-blank);
}
.stat-label { font-size: 12px; color: var(--el-text-color-secondary); margin-bottom: 6px; }
.stat-value { font-size: 18px; font-weight: 700; }
@media (max-width: 1200px) { .stats-grid { grid-template-columns: repeat(2, minmax(160px, 1fr)); } }

/* Tabs & toolbars */
.cd-tabs { --el-tabs-header-height: 42px; }
.toolbar { display: flex; justify-content: flex-end; padding-bottom: 10px; }

/* Action buttons (giống style ảnh) */
.action-group { display: inline-flex; border-radius: 8px; overflow: hidden; }
.action-btn {
  border: none !important; width: 40px; height: 34px;
  display: inline-flex; align-items: center; justify-content: center; padding: 0;
}
.action-btn :deep(.el-icon) { color: #fff; font-size: 16px; }
.action-btn.view { background: #3b82f6; }        /* blue-500 */
.action-btn.view:hover { background: #2563eb; }  /* blue-600 */
.action-btn.print { background: #22c55e; }       /* green-500 */
.action-btn.print:hover { background: #16a34a; } /* green-600 */

.table-pagination { display: flex; justify-content: flex-end; padding-top: 12px; }

/* Spacing helpers */
.mb-4 { margin-bottom: 16px; }
</style>

<template>
  <el-container class="campaign-details-container">
    <!-- Header / breadcrumb -->
    <el-header class="flex justify-between items-center mb-4">
      <div class="flex items-center gap-2">
        <el-button link @click="goBack">
          ← Quay lại
        </el-button>
        <h1 class="text-2xl font-bold">Chi tiết đợt giảm giá</h1>
      </div>
      <div class="flex items-center gap-2">
        <el-tag :type="statusTagType(campaign?.status)" v-if="campaign">
          {{ statusText(campaign.status) }}
        </el-tag>
        <el-button type="primary" @click="refresh">Làm mới</el-button>
      </div>
    </el-header>

    <el-main>
      <!-- Loading -->
      <el-skeleton v-if="loading" animated :rows="6" />

      <!-- Error -->
      <el-alert
        v-else-if="error"
        :title="error"
        type="error"
        show-icon
        class="mb-4"
      />

      <!-- Content -->
      <div v-else-if="campaign">
        <!-- Thông tin tổng quan -->
        <el-card class="mb-4" shadow="hover">
          <template #header>
            <div class="card-header flex justify-between items-center">
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

        <!-- Thống kê (nếu có endpoint /statistics) -->
        <el-card v-if="stats" class="mb-4" shadow="hover">
          <template #header>
            <div class="card-header">Thống kê</div>
          </template>
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

        <!-- Danh sách sản phẩm thuộc đợt -->
        <el-card shadow="hover">
          <template #header>
            <div class="card-header flex justify-between items-center">
              <span>Sản phẩm thuộc đợt giảm giá</span>
              <el-input
                v-model="keyword"
                placeholder="Tìm theo tên sản phẩm…"
                clearable
                style="max-width: 320px"
                @clear="applyFilter"
                @keyup.enter.native="applyFilter"
              >
                <template #append>
                  <el-button @click="applyFilter">Tìm</el-button>
                </template>
              </el-input>
            </div>
          </template>

          <el-table
            :data="pagedProducts"
            border
            style="width: 100%; table-layout: auto;"
            :fit="true"
            empty-text="Chưa có sản phẩm nào trong đợt này"
          >
            <el-table-column type="index" label="STT" width="70" align="center" />

            <el-table-column prop="productId" label="Mã SP" width="120" />

            <el-table-column prop="productName" label="Tên sản phẩm" min-width="220" show-overflow-tooltip />

            <el-table-column label="Xem nhanh" width="130" align="center">
              <template #default="{ row }">
                <el-button size="small" @click="goToProduct(row.productId)">Chi tiết SP</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="mt-4 flex justify-end">
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
        </el-card>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute();

const campaign = ref(null);
const stats = ref(null);
const loading = ref(true);
const error = ref('');

// tìm kiếm trong bảng sản phẩm
const keyword = ref('');

// phân trang bảng sản phẩm
let prodPage = ref(1);
let prodPageSize = ref(10);

const formatDateTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const formatCurrency = (val) => {
  try {
    return Number(val).toLocaleString('vi-VN') + ' đ';
  } catch {
    return '-';
  }
};

const statusText = (status) => {
  switch (status) {
    case 0: return 'Sắp diễn ra';
    case 1: return 'Đang hoạt động';
    case 2: return 'Đã kết thúc';
    default: return 'Không xác định';
  }
};
const statusTagType = (status) => {
  switch (status) {
    case 0: return 'warning';
    case 1: return 'success';
    case 2: return 'info';
    default: return '';
  }
};

const goBack = () => router.back();
const goToProduct = (productId) => {
  if (!productId) return;
  // Điều hướng sang trang chi tiết sản phẩm của bạn
  // Ví dụ: /products/123
  router.push({ path: `/products/${productId}` });
};

const refresh = () => {
  loadCampaign();
  loadStats();
};

// dữ liệu sản phẩm từ campaign.products (API bạn trả về)
const products = computed(() => {
  // Mặc định ưu tiên campaign.products; nếu hệ thống của bạn để ở campaign.productDetails thì có thể map lại tại đây
  if (!campaign.value) return [];
  // campaign.products = [{ id, productId, productName }]
  return Array.isArray(campaign.value.products) ? campaign.value.products : [];
});

// filter/tìm kiếm theo tên
const filteredProducts = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  if (!kw) return products.value;
  return products.value.filter(p => (p.productName || '').toLowerCase().includes(kw));
});

// trang hiện tại
const pagedProducts = computed(() => {
  const start = (prodPage.value - 1) * prodPageSize.value;
  return filteredProducts.value.slice(start, start + prodPage.value * prodPageSize.value - start);
});

const applyFilter = () => {
  prodPage.value = 1;
};

const loadCampaign = async () => {
  loading.value = true;
  error.value = '';
  try {
    const id = route.params.id;
    const res = await axios.get(`http://localhost:8080/api/admin/campaigns/${id}`);
    campaign.value = res.data;
    // reset phân trang khi dữ liệu đổi
    prodPage.value = 1;
  } catch (e) {
    console.error(e);
    error.value = 'Không thể tải chi tiết đợt giảm giá.';
  } finally {
    loading.value = false;
  }
};

const loadStats = async () => {
  try {
    const id = route.params.id;
    const res = await axios.get(`http://localhost:8080/api/admin/campaigns/${id}/statistics`);
    stats.value = res.data;
  } catch (e) {
    // Không chặn render nếu không có thống kê
    stats.value = null;
  }
};

onMounted(() => {
  loadCampaign();
  loadStats();
});

// khi đổi id trong route (điều hướng nội bộ), tự reload
watch(() => route.params.id, () => {
  refresh();
});
</script>

<style scoped>
.campaign-details-container {
  padding-bottom: 20px;
}

.card-header {
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
}
.stat-item {
  padding: 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  background: var(--el-fill-color-blank);
}
.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}
.stat-value {
  font-size: 18px;
  font-weight: 700;
}
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(160px, 1fr));
  }
}
</style>

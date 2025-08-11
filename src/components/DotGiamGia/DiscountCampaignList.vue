<template>
  <el-container class="discount-campaigns-container">
    <!-- Header -->
    <el-header class="flex justify-between items-center mb-4">
      <h1 class="text-2xl font-bold">Danh sách đợt giảm giá</h1>
      <el-button type="primary" @click="goToAddPage">Thêm đợt giảm giá</el-button>
    </el-header>

    <el-main>
      <!-- Loading -->
      <div v-if="loading" class="text-gray-500">Đang tải dữ liệu...</div>

      <!-- Table -->
      <div v-else>
        <el-table
          :data="campaigns"
          border
          style="width: 100%; table-layout: auto;"
          :fit="true"
        >
          <!-- STT -->
          <el-table-column type="index" label="STT" width="60" align="center" />

          <!-- Tên -->
          <el-table-column prop="name" label="Tên đợt giảm giá" min-width="180" />

          <!-- Mã -->
          <el-table-column prop="campaignCode" label="Mã" min-width="100" />

          <!-- Giảm giá -->
          <el-table-column label="Giảm giá" width="100" align="center">
            <template #default="{ row }">
              {{ row.discountPercentage }}%
            </template>
          </el-table-column>

          <!-- Mô tả -->
          <el-table-column prop="description" label="Mô tả" min-width="200" show-overflow-tooltip />

          <!-- Thời gian -->
          <el-table-column label="Thời gian" min-width="200">
            <template #default="{ row }">
              {{ formatDateTime(row.startDate) }} → {{ formatDateTime(row.endDate) }}
            </template>
          </el-table-column>

          <!-- Trạng thái -->
          <el-table-column label="Trạng thái" width="130" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- Thống kê gộp -->
          <el-table-column label="Thống kê" min-width="200" align="left">
            <template #default="{ row }">
              <div class="stats-cell">
                <div><strong>Hóa đơn:</strong> {{ row.totalInvoices ?? '-' }}</div>
                <div><strong>Doanh thu:</strong> {{ row.totalAfterDiscount ? row.totalAfterDiscount.toLocaleString('vi-VN') + ' đ' : '-' }}</div>
                <div><strong>SP bán ra:</strong> {{ row.totalProductsSold ?? '-' }}</div>
                <div><strong>Giảm TB:</strong> {{ row.averageDiscountRate ? row.averageDiscountRate + '%' : '-' }}</div>
              </div>
            </template>
          </el-table-column>

          <!-- Hành động -->
          <el-table-column label="Hành động" min-width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="warning" @click="changeStatus(row)">Chuyển trạng thái</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="mt-4 flex justify-end">
          <el-pagination
            background
            layout="total, prev, pager, next, sizes"
            :total="totalItems"
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const router = useRouter();

const campaigns = ref([]);
const loading = ref(true);

// Pagination
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);

// Format datetime
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

// Status text
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

// Change status
const changeStatus = async (campaign) => {
  try {
    await axios.post(`http://localhost:8080/api/admin/campaigns/${campaign.id}/delete`);
    ElMessage.success('Đã chuyển trạng thái thành công!');
    loadCampaigns();
  } catch (error) {
    console.error('Lỗi khi chuyển trạng thái:', error);
    ElMessage.error('Không thể chuyển trạng thái!');
  }
};

// Pagination handlers
const handlePageChange = (page) => {
  currentPage.value = page;
  loadCampaigns();
};
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1;
  loadCampaigns();
};

// Load campaigns with statistics
const loadCampaigns = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/api/admin/campaigns', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    });

    let data = res.data.content;

    // Fetch statistics for each campaign
    const statsPromises = data.map(async (campaign) => {
      try {
        const statsRes = await axios.get(`http://localhost:8080/api/admin/campaigns/${campaign.id}/statistics`);
        return { ...campaign, ...statsRes.data };
      } catch (err) {
        console.error(`Lỗi load thống kê cho campaign ${campaign.id}`, err);
        return campaign;
      }
    });

    campaigns.value = await Promise.all(statsPromises);

    totalItems.value = res.data.page.totalElements;
    pageSize.value = res.data.page.size;
    currentPage.value = res.data.page.number + 1;
  } catch (error) {
    console.error('Lỗi tải đợt giảm giá:', error);
    ElMessage.error('Không thể tải danh sách đợt giảm giá.');
  } finally {
    loading.value = false;
  }
};

// Go to add page
const goToAddPage = () => {
  router.push('/discount-campaigns/add');
};

onMounted(() => {
  loadCampaigns();
});
</script>
<style scoped>
.stats-cell-compact {
  display: flex;
  flex-direction: column;
  gap: 4px; /* Tạo khoảng cách giữa các dòng */
}

/* Tùy chọn: Tối ưu hiển thị trên màn hình nhỏ */
@media (max-width: 1200px) {
  .el-table-column {
    min-width: 100px !important;
  }
}
</style>


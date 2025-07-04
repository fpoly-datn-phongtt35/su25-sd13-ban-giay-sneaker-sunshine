<template>
  <el-container class="discount-campaigns-container">
    <el-header class="flex justify-between items-center mb-4">
      <h1 class="text-2xl font-bold">Danh sách đợt giảm giá</h1>
      <el-button type="primary" @click="goToAddPage">
        + Thêm đợt giảm giá
      </el-button>
    </el-header>

    <el-main>
      <div v-if="loading" class="text-gray-500">Đang tải dữ liệu...</div>

      <div v-else>
        <el-table :data="campaigns" style="width: 100%" border>
          <el-table-column type="index" label="STT" width="80" align="center"></el-table-column>
          <el-table-column prop="name" label="Tên đợt giảm giá"></el-table-column>
          <el-table-column prop="campaignCode" label="Mã"></el-table-column>
          <el-table-column prop="discountPercentage" label="Giảm giá" width="120">
            <template #default="{ row }"> {{ row.discountPercentage }}% </template>
          </el-table-column>
          <el-table-column prop="description" label="Mô tả"></el-table-column>
          <el-table-column label="Thời gian" width="240">
            <template #default="{ row }">
              {{ formatDate(row.startDate) }} → {{ formatDate(row.endDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="Trạng thái" width="150">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- ✅ Thêm cột hành động -->
          <el-table-column label="Hành động" width="180">
            <template #default="{ row }">
              <el-button 
                size="small" 
                type="warning" 
                @click="changeStatus(row)">
                Chuyển trạng thái
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-main>
  </el-container>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const campaigns = ref([]);
const loading = ref(true);
const router = useRouter();

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('vi-VN');
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

const goToAddPage = () => {
  router.push('/discount-campaigns/add');
};

const loadCampaigns = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/api/admin/campaigns');
    campaigns.value = res.data;
  } catch (error) {
    console.error('Lỗi tải đợt giảm giá:', error);
    ElMessage.error('Không thể tải danh sách đợt giảm giá.');
  } finally {
    loading.value = false;
  }
};

// ✅ Hàm đổi trạng thái
const changeStatus = async (campaign) => {
  try {
    await axios.post(`http://localhost:8080/api/admin/campaigns/${campaign.id}/delete`);
    ElMessage.success('Đã chuyển trạng thái thành công!');
    await loadCampaigns(); // reload lại danh sách sau khi đổi trạng thái
  } catch (error) {
    console.error('Lỗi khi chuyển trạng thái:', error);
    ElMessage.error('Không thể chuyển trạng thái!');
  }
};

onMounted(() => {
  loadCampaigns();
});
</script>

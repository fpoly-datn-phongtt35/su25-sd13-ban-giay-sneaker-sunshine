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
        <el-table :data="campaigns" border style="width: 100%" :fit="true" stripe>
          <!-- STT -->
          <el-table-column type="index" label="STT" width="60" align="center" />

          <!-- Tên -->
          <el-table-column prop="name" label="Tên đợt giảm giá" min-width="180" show-overflow-tooltip />

          <!-- Mã -->
          <el-table-column prop="campaignCode" label="Mã" min-width="120" />

          <!-- Giảm giá -->
          <el-table-column label="Giảm giá" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="success" effect="plain">{{ row.discountPercentage }}%</el-tag>
            </template>
          </el-table-column>

          <!-- Mô tả -->
          <el-table-column prop="description" label="Mô tả" min-width="200" show-overflow-tooltip />

          <!-- Trạng thái -->
          <el-table-column label="Trạng thái" width="140" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" round>{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>

          <!-- Hành động -->
          <el-table-column label="Hành động" width="160" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-group">
                <!-- Nút xem -->
                <el-button class="action-btn view" size="small"
                  @click="$router.push(`/discount-campaigns/detail/${row.id}`)">
                  <el-icon><View /></el-icon>
                </el-button>

                <!-- Nút in -->
                <!-- <el-button class="action-btn print" size="small" @click="printCampaign(row)">
                  <el-icon><Printer /></el-icon>
                </el-button> -->

                <!-- Menu thêm -->
                <el-dropdown trigger="click">
                  <span class="el-dropdown-link more-btn">
                    <el-icon><MoreFilled /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="goToUpdate(row.id)">Cập nhật</el-dropdown-item>
                      <el-dropdown-item @click="changeStatus(row)">Chuyển trạng thái</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { View, Printer, MoreFilled } from '@element-plus/icons-vue'

const router = useRouter()

const campaigns = ref([])
const loading = ref(true)

// Pagination
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const safe = typeof dateStr === 'string' ? dateStr.replace(' ', 'T') : dateStr
  const date = new Date(safe)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const statusText = (status) => {
  switch (status) {
    case 0: return 'Sắp diễn ra'
    case 1: return 'Đang hoạt động'
    case 2: return 'Đã kết thúc'
    default: return 'Không xác định'
  }
}
const statusTagType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    default: return ''
  }
}

const changeStatus = async (campaign) => {
  try {
    await axios.post(`http://localhost:8080/api/admin/campaigns/${campaign.id}/delete`)
    ElMessage.success('Đã chuyển trạng thái thành công!')
    loadCampaigns()
  } catch (error) {
    console.error('Lỗi khi chuyển trạng thái:', error)
    ElMessage.error('Không thể chuyển trạng thái!')
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadCampaigns()
}
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadCampaigns()
}

const loadCampaigns = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/admin/campaigns', {
      params: { page: currentPage.value - 1, size: pageSize.value },
    })

    const data = res.data?.content ?? []
    const statsPromises = data.map(async (c) => {
      try {
        const statsRes = await axios.get(`http://localhost:8080/api/admin/campaigns/${c.id}/statistics`)
        return { ...c, ...statsRes.data }
      } catch {
        return c
      }
    })
    campaigns.value = await Promise.all(statsPromises)

    const pageMeta = res.data?.page ?? {}
    totalItems.value = pageMeta.totalElements ?? 0
    pageSize.value = pageMeta.size ?? pageSize.value
    currentPage.value = (pageMeta.number ?? 0) + 1
  } catch (error) {
    console.error('Lỗi tải đợt giảm giá:', error)
    ElMessage.error('Không thể tải danh sách đợt giảm giá.')
  } finally {
    loading.value = false
  }
}

const goToAddPage = () => router.push('/discount-campaigns/add')
const goToUpdate = (id) => router.push(`/discount-campaigns/update/${id}`)

const printCampaign = (row) => {
  // Tạm in toàn trang, bạn thay bằng in report theo yêu cầu
  window.print()
}

onMounted(() => {
  loadCampaigns()
})
</script>

<style scoped>
.action-group {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.action-btn {
  border: none !important;
  width: 40px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}
.action-btn :deep(.el-icon) {
  font-size: 16px;
  color: #fff;
}
.action-btn.view { background: #3b82f6; }
.action-btn.view:hover { background: #2563eb; }
.action-btn.print { background: #22c55e; }
.action-btn.print:hover { background: #16a34a; }

/* More menu */
.more-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
}
.more-btn:hover {
  background: #f3f4f6;
  color: #111827;
}
</style>

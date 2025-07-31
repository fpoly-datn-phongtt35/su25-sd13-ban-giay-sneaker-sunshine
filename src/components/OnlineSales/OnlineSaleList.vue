<template>
  <div class="p-4 bg-light">
    <!-- Bộ lọc tìm kiếm -->
    <el-card class="mb-4 shadow-sm card-filter">
      <el-form
        :inline="true"
        :model="filters"
        class="form-bar flex flex-wrap items-end gap-4"
        label-position="top"
      >
        <el-form-item label="Tìm kiếm (Mã / Khách hàng)" class="w-64">
          <el-input
            v-model="filters.search"
            placeholder="Nhập mã hoặc tên khách hàng"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-form-item>

        <el-form-item label="Khoảng thời gian tạo" class="w-96">
          <el-date-picker
            v-model="filters.dateRange"
            type="datetimerange"
            range-separator="→"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="DD/MM/YYYY HH:mm:ss"
            clearable
            class="w-full"
            unlink-panels
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="search">Tìm kiếm</el-button>
          <el-button icon="el-icon-refresh" @click="resetFilters">Làm mới</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Tabs trạng thái -->
    <el-tabs
      v-model="currentTab"
      @tab-change="search"
      type="border-card"
      class="mb-4 shadow-sm"
    >
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.key"
        :label="`${tab.label} (${tab.count})`"
        :name="tab.key"
      />
    </el-tabs>

    <!-- Bảng dữ liệu -->
    <el-table
      :data="invoices"
      v-loading="loading"
      border
      stripe
      class="shadow-sm"
      row-class-name="table-row"
      height="auto"
    >
      <el-table-column label="#" type="index" width="60" align="center" />
      <el-table-column prop="invoiceCode" label="Mã hóa đơn" width="160" />
      <el-table-column prop="customerName" label="Khách hàng" />
      <el-table-column prop="phone" label="SĐT" width="130" />
      <el-table-column label="Ngày tạo" width="220">
        <template #default="scope">
          <el-tooltip
            effect="dark"
            :content="formatDateTime(scope.row.createdDate)"
            placement="top"
          >
            <span>{{ formatDateTime(scope.row.createdDate) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="Tổng tiền" width="150" align="right">
        <template #default="scope">
          {{ scope.row.totalAmount.toLocaleString() }} ₫
        </template>
      </el-table-column>
      <el-table-column label="Hành động" width="160" align="center">
        <template #default="scope">
          <el-button
            size="small"
            type="primary"
            icon="el-icon-view"
            @click="goToStatusPage(scope.row.id)"
          >
            Chi tiết
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Phân trang -->
    <div class="d-flex justify-end mt-4">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import apiClient from '@/utils/axiosInstance'

const router = useRouter()

const filters = ref({
  search: '',
  dateRange: []
})

const currentTab = ref('CHO_XU_LY')
const page = ref(1)
const pageSize = ref(100)
const total = ref(0)
const invoices = ref([])
const loading = ref(false)
const statusCounts = ref([])

const tabs = ref([
  { key: 'CHO_XU_LY', label: 'Chờ xử lý', count: 0 },
  { key: 'DA_XU_LY', label: 'Đã xử lý', count: 0 },
  { key: 'CHO_GIAO_HANG', label: 'Chờ giao hàng', count: 0 },
  { key: 'DANG_GIAO_HANG', label: 'Đang giao hàng', count: 0 },
  { key: 'GIAO_THANH_CONG', label: 'Giao hàng thành công', count: 0 },
  { key: 'GIAO_THAT_BAI', label: 'Giao hàng thất bại', count: 0 },
  { key: 'HUY_DON', label: 'Đơn hàng hủy', count: 0 }
])

const statusLabelToCode = (label) => {
  const map = {
    'DANG_GIAO_DICH': -3,
    'HUY_DON': -2,
    'HUY_GIAO_DICH': -1,
    'CHO_XU_LY': 0,
    'DA_XU_LY': 1,
    'CHO_GIAO_HANG': 2,
    'DANG_GIAO_HANG': 3,
    'GIAO_THANH_CONG': 4,
    'GIAO_THAT_BAI': 5,
    'MAT_HANG': 6,
    'DA_HOAN_TIEN': 7,
  }
  return map[label] ?? null
}

const fetchStatusCounts = async () => {
  try {
    const res = await apiClient.get('/admin/online-sales/count-by-status')
    statusCounts.value = res.data || []
    tabs.value = tabs.value.map(tab => {
      const found = statusCounts.value.find(i => i.statusDetail === tab.key)
      return { ...tab, count: found ? found.count : 0 }
    })
  } catch (err) {
    console.error('Lỗi lấy số lượng trạng thái:', err)
  }
}

const search = async () => {
  loading.value = true
  try {
    const params = {
      status: statusLabelToCode(currentTab.value),
      phone: filters.value.search || null,
      code: filters.value.search || null,
      createdFrom: filters.value.dateRange[0] || null,
      createdTo: filters.value.dateRange[1] || null,
      page: page.value - 1,
      size: pageSize.value
    }
    const res = await apiClient.get('/admin/online-sales/search', { params })
    invoices.value = res.data.content
    total.value = res.data.totalElements
    await fetchStatusCounts()
  } catch (err) {
    console.error('Lỗi tìm kiếm:', err)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = { search: '', dateRange: [] }
  page.value = 1
  search()
}

const handlePageChange = (val) => {
  page.value = val
  search()
}

const goToStatusPage = (invoiceId) => {
  router.push({ name: 'InvoiceStatus', params: { invoiceId } })
}

const formatDateTime = (dateStr) => {
  return dayjs(dateStr).format('DD/MM/YYYY HH:mm:ss')
}

onMounted(() => {
  search()
})
</script>

<style scoped>
.form-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.card-filter {
  border-radius: 12px;
  background-color: #fff;
  padding: 16px;
}

.el-table .table-row:hover {
  background-color: #f5f7fa;
}

.justify-end {
  display: flex;
  justify-content: flex-end;
}

.bg-light {
  background-color: #fafafa;
  min-height: 100vh;
}

.w-64 {
  width: 256px;
}

.w-96 {
  width: 384px;
}

.w-full {
  width: 100%;
}
</style>

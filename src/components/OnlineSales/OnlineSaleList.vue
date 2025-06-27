<template>
  <div class="p-4">
    <!-- Bộ lọc -->
    <el-card class="mb-4">
      <el-form :inline="true" :model="filters" class="demo-form-inline">
        <el-form-item label="Tìm kiếm">
          <el-input v-model="filters.search" placeholder="Mã hóa đơn / khách hàng" clearable />
        </el-form-item>
        <el-form-item label="Loại đơn">
          <el-select v-model="filters.orderType" placeholder="Chọn loại">
            <el-option label="Tất cả" value="" />
            <el-option label="ONLINE" value="1" />
            <el-option label="OFFLINE" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="Ngày">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="Đến"
            start-placeholder="Từ"
            end-placeholder="Đến"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">Tìm kiếm</el-button>
          <el-button @click="resetFilters">Làm mới</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Tabs trạng thái -->
    <el-tabs v-model="currentTab" @tab-change="search" type="border-card" class="mb-4">
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.key"
        :label="`${tab.label}`"
        :name="tab.key"
      />
    </el-tabs>

    <!-- Bảng hóa đơn -->
    <el-table :data="invoices" v-loading="loading" border stripe>
      <el-table-column label="STT" type="index" width="60" />
      <el-table-column prop="invoiceCode" label="Mã hóa đơn" />
      <el-table-column prop="customerName" label="Khách hàng" />
      <el-table-column prop="phone" label="SĐT" />
      <el-table-column prop="orderType" label="Loại đơn">
        <template #default="scope">
          <el-tag :type="scope.row.orderType === 1 ? 'success' : 'info'" effect="plain">
            {{ scope.row.orderType === 1 ? 'ONLINE' : 'OFFLINE' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdDate" label="Ngày tạo" />
      <el-table-column prop="totalAmount" label="Tổng tiền">
        <template #default="scope">
          {{ scope.row.totalAmount.toLocaleString() }} ₫
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Trạng thái">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">
            {{ statusCodeToLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- Phân trang -->
    <div class="d-flex justify-content-end mt-4">
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
import axios from 'axios'
import apiClient from '@/utils/axiosInstance'

// Filters & Tabs
const filters = ref({
  search: '',
  orderType: '',
  dateRange: []
})

const currentTab = ref('ALL')
const tabs = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'WAITING', label: 'Chờ xác nhận' },
  { key: 'CONFIRMED', label: 'Đã xác nhận' },
  { key: 'DELIVERING', label: 'Đang giao hàng' },
  { key: 'COMPLETED', label: 'Đã hoàn thành' },
  { key: 'CANCELLED', label: 'Đã hủy' }
]

// Data & Pagination
const invoices = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// Status mapping
const statusLabelToCode = (label) => ({
  WAITING: 0,
  CONFIRMED: 1,
  DELIVERING: 2,
  COMPLETED: 3,
  CANCELLED: 4
})[label] ?? null

const statusCodeToLabel = (code) => ({
  0: 'Chờ xác nhận',
  1: 'Đã xác nhận',
  2: 'Đang giao hàng',
  3: 'Đã hoàn thành',
  4: 'Đã hủy'
})[code] ?? 'Không rõ'

const statusTag = (code) => ({
  0: 'warning',
  1: '',
  2: 'info',
  3: 'success',
  4: 'danger'
})[code] ?? ''

// Search API
const search = async () => {
  loading.value = true
  try {
    const params = {
      status: currentTab.value !== 'ALL' ? statusLabelToCode(currentTab.value) : null,
      orderType: filters.value.orderType || null,
      phone: filters.value.search || null,
      code: filters.value.search || null,
      createdFrom: filters.value.dateRange[0] || null,
      createdTo: filters.value.dateRange[1] || null,
      page: page.value - 1,
      size: pageSize.value
    }
    const response = await apiClient.get('/admin/online-sales/search', { params })
    invoices.value = response.data.content
    total.value = response.data.totalElements
  } catch (e) {
    console.error('Lỗi khi tải dữ liệu:', e)
  } finally {
    loading.value = false
  }
}

// Pagination handler
const handlePageChange = (val) => {
  page.value = val
  search()
}

// Reset
const resetFilters = () => {
  filters.value = {
    search: '',
    orderType: '',
    dateRange: []
  }
  page.value = 1
  search()
}

// Initial load
onMounted(() => {
  search()
})
</script>

<style scoped>
.el-card {
  background-color: #fdfdfd;
}
</style>

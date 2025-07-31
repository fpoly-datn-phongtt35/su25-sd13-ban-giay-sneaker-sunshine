<template>
  <div class="container mt-4">
    <h4 class="mb-3">Báo cáo đơn hàng theo nhân viên tạo</h4>

    <!-- Bộ lọc -->
    <div class="row mb-3">
      <div class="col-md-2">
        <input type="date" v-model="filters.startDate" class="form-control" />
      </div>
      <div class="col-md-2">
        <input type="date" v-model="filters.endDate" class="form-control" />
      </div>
      <div class="col-md-2">
        <select v-model="filters.store" class="form-control">
          <option value="">- Cửa hàng -</option>
          <option value="store1">Store 1</option>
          <option value="store2">Store 2</option>
        </select>
      </div>
      <div class="col-md-2">
        <select v-model="filters.createdBy" class="form-control">
          <option value="creator">Theo người tạo</option>
          <option value="handler">Theo người xử lý</option>
        </select>
      </div>
      <div class="col-md-2">
        <button @click="applyFilters" class="btn btn-success">Lọc</button>
      </div>
    </div>

    <!-- Bảng báo cáo -->
    <div class="table-responsive">
      <table class="table table-bordered table-sm text-center align-middle">
        <thead class="table-light">
          <tr>
            <th rowspan="2">Nhân viên</th>
            <th colspan="3">Tổng đơn</th>
            <th colspan="3">Thành công</th>
            <th colspan="3">Chuyển hoàn</th>
            <th colspan="3">Huỷ</th>
            <th colspan="6">Chi phí</th>
            <th>Chiết khấu</th>
            <th>Đặt cọc</th>
            <th>Chuyển khoản</th>
          </tr>
          <tr>
            <th>Đơn</th><th>SLSP</th><th>Doanh thu</th>
            <th>Đơn</th><th>SLSP</th><th>Doanh thu</th>
            <th>Đơn</th><th>SLSP</th><th>Doanh thu</th>
            <th>Đơn</th><th>SLSP</th><th>Doanh thu</th>
            <th>Vận chuyển</th><th>Thu hộ</th><th>Bảo hiểm</th>
            <th>Khách</th><th>Chuyển hoàn</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in reportData" :key="row.employee">
            <td>{{ row.employee }}</td>
            <td>{{ row.totalOrders }}</td>
            <td>{{ row.totalProducts }}</td>
            <td>{{ format(row.totalRevenue) }}</td>

            <td>{{ row.successOrders }}</td>
            <td>{{ row.successProducts }}</td>
            <td>{{ format(row.successRevenue) }}</td>

            <td>{{ row.returnOrders }}</td>
            <td>{{ row.returnProducts }}</td>
            <td>{{ format(row.returnRevenue) }}</td>

            <td>{{ row.cancelOrders }}</td>
            <td>{{ row.cancelProducts }}</td>
            <td>{{ format(row.cancelRevenue) }}</td>

            <td>{{ format(row.shippingFee) }}</td>
            <td>{{ format(row.codFee) }}</td>
            <td>{{ format(row.insuranceFee) }}</td>
            <td>{{ format(row.customerFee) }}</td>
            <td>{{ format(row.returnFee) }}</td>

            <td>{{ format(row.discount) }}</td>
            <td>{{ format(row.deposit) }}</td>
            <td>{{ format(row.bankTransfer) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const filters = ref({
  startDate: '2023-02-01',
  endDate: '2023-02-22',
  store: '',
  createdBy: 'creator',
})

const reportData = ref([
  {
    employee: 'Lam Diệp',
    totalOrders: 206,
    totalProducts: 211,
    totalRevenue: 51607000,
    successOrders: 9,
    successProducts: 9,
    successRevenue: 2655000,
    returnOrders: 1,
    returnProducts: 1,
    returnRevenue: 15000,
    cancelOrders: 3,
    cancelProducts: 4,
    cancelRevenue: 942000,
    shippingFee: 42000,
    codFee: 58000,
    insuranceFee: 0,
    customerFee: 0,
    returnFee: 0,
    discount: 25000,
    deposit: 0,
    bankTransfer: 4930000,
  },
  {
    employee: 'DVKH nhanh',
    totalOrders: 32,
    totalProducts: 59,
    totalRevenue: 67206000,
    successOrders: 8,
    successProducts: 8,
    successRevenue: 535000,
    returnOrders: 1,
    returnProducts: 1,
    returnRevenue: 15000,
    cancelOrders: 3,
    cancelProducts: 3,
    cancelRevenue: 600000,
    shippingFee: 204700,
    codFee: 184600,
    insuranceFee: 190520,
    customerFee: 0,
    returnFee: 0,
    discount: 0,
    deposit: 0,
    bankTransfer: 0,
  },
])

const applyFilters = () => {
  console.log('Lọc theo', filters.value)
  // Gọi API để fetch dữ liệu mới
}

const format = (value) => {
  return new Intl.NumberFormat('vi-VN').format(value)
}
</script>

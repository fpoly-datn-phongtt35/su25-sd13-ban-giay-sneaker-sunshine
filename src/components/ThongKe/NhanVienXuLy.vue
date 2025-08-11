<template>
  <div class="container mt-4">
    <h4 class="mb-3">Báo cáo đơn hàng theo nhân viên</h4>

    <div class="row mb-3 g-2">
      <div class="col-md-3">
        <label for="startDate" class="form-label visually-hidden">Ngày bắt đầu</label>
        <input type="date" id="startDate" v-model="filters.startDate" class="form-control" />
      </div>
      <div class="col-md-3">
        <label for="endDate" class="form-label visually-hidden">Ngày kết thúc</label>
        <input type="date" id="endDate" v-model="filters.endDate" class="form-control" />
      </div>
      <div class="col-md-3">
        <label for="employee" class="form-label visually-hidden">Nhân viên</label>
        <select id="employee" v-model="filters.employee" class="form-control">
          <option value="">- Tất cả nhân viên -</option>
          <option v-for="employee in employeeList" :key="employee" :value="employee">
            {{ employee }}
          </option>
        </select>
      </div>
      <div class="col-md-3 d-flex">
        <button @click="applyFilters" class="btn btn-success me-2">Lọc</button>
        <button @click="resetFilters" class="btn btn-outline-secondary">Đặt lại</button>
      </div>
    </div>

    <div class="table-responsive">
      <table class="table table-bordered table-sm text-center align-middle">
        <thead class="table-light">
          <tr>
            <th rowspan="2">Nhân viên</th>
            <th colspan="3">Tổng đơn</th>
            <th colspan="3">Thành công</th>
            <th colspan="3">Huỷ</th>
          </tr>
          <tr>
            <th>Đơn</th><th>SLSP</th><th>Doanh thu</th>
            <th>Đơn</th><th>SLSP</th><th>Doanh thu</th>
            <th>Đơn</th><th>SLSP</th><th>Doanh thu</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredData.length === 0">
            <td colspan="10">Không tìm thấy dữ liệu.</td>
          </tr>
          <tr v-else v-for="row in filteredData" :key="row.employee">
            <td>{{ row.employee }}</td>
            <td>{{ row.totalOrders }}</td>
            <td>{{ row.totalProducts }}</td>
            <td>{{ format(row.totalRevenue) }}</td>
            <td>{{ row.successOrders }}</td>
            <td>{{ row.successProducts }}</td>
            <td>{{ format(row.successRevenue) }}</td>
            <td>{{ row.cancelOrders }}</td>
            <td>{{ row.cancelProducts }}</td>
            <td>{{ format(row.cancelRevenue) }}</td>
          </tr>
          <tr class="table-info fw-bold">
            <td>Tổng cộng</td>
            <td>{{ total.totalOrders }}</td>
            <td>{{ total.totalProducts }}</td>
            <td>{{ format(total.totalRevenue) }}</td>
            <td>{{ total.successOrders }}</td>
            <td>{{ total.successProducts }}</td>
            <td>{{ format(total.successRevenue) }}</td>
            <td>{{ total.cancelOrders }}</td>
            <td>{{ total.cancelProducts }}</td>
            <td>{{ format(total.cancelRevenue) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

// Dữ liệu mẫu (mock data) đã được tinh gọn
const mockReportData = [
  {
    employee: 'Lam Diệp',
    createdDate: '2023-02-05',
    totalOrders: 206, totalProducts: 211, totalRevenue: 51607000,
    successOrders: 9, successProducts: 9, successRevenue: 2655000,
    cancelOrders: 3, cancelProducts: 4, cancelRevenue: 942000,
  },
  {
    employee: 'DVKH nhanh',
    createdDate: '2023-02-08',
    totalOrders: 32, totalProducts: 59, totalRevenue: 67206000,
    successOrders: 8, successProducts: 8, successRevenue: 535000,
    cancelOrders: 3, cancelProducts: 3, cancelRevenue: 600000,
  },
  {
    employee: 'Nguyễn Văn A',
    createdDate: '2023-02-10',
    totalOrders: 50, totalProducts: 120, totalRevenue: 15000000,
    successOrders: 40, successProducts: 100, successRevenue: 12000000,
    cancelOrders: 5, cancelProducts: 10, cancelRevenue: 250000,
  },
  {
    employee: 'Trần Thị B',
    createdDate: '2023-02-15',
    totalOrders: 15, totalProducts: 30, totalRevenue: 7500000,
    successOrders: 10, successProducts: 20, successRevenue: 6000000,
    cancelOrders: 3, cancelProducts: 6, cancelRevenue: 150000,
  },
  {
    employee: 'Lê Văn C',
    createdDate: '2023-02-20',
    totalOrders: 100, totalProducts: 150, totalRevenue: 25000000,
    successOrders: 80, successProducts: 120, successRevenue: 20000000,
    cancelOrders: 10, cancelProducts: 15, cancelRevenue: 500000,
  },
];

// Lấy danh sách tên nhân viên duy nhất từ dữ liệu mẫu
const employeeList = computed(() => {
  const employees = new Set(mockReportData.map(item => item.employee));
  return Array.from(employees);
});

const filters = ref({
  startDate: '2023-02-01',
  endDate: '2023-02-28',
  employee: '',
});

const filteredData = computed(() => {
  return mockReportData.filter(row => {
    // Lọc theo ngày
    const rowDate = new Date(row.createdDate);
    const start = new Date(filters.value.startDate);
    const end = new Date(filters.value.endDate);
    end.setHours(23, 59, 59, 999);
    if (rowDate < start || rowDate > end) {
      return false;
    }

    // Lọc theo nhân viên
    if (filters.value.employee && row.employee !== filters.value.employee) {
      return false;
    }

    return true;
  });
});

const total = computed(() => {
  return filteredData.value.reduce((acc, row) => {
    acc.totalOrders += row.totalOrders;
    acc.totalProducts += row.totalProducts;
    acc.totalRevenue += row.totalRevenue;
    acc.successOrders += row.successOrders;
    acc.successProducts += row.successProducts;
    acc.successRevenue += row.successRevenue;
    acc.cancelOrders += row.cancelOrders;
    acc.cancelProducts += row.cancelProducts;
    acc.cancelRevenue += row.cancelRevenue;
    return acc;
  }, {
    totalOrders: 0, totalProducts: 0, totalRevenue: 0,
    successOrders: 0, successProducts: 0, successRevenue: 0,
    cancelOrders: 0, cancelProducts: 0, cancelRevenue: 0,
  });
});

const applyFilters = () => {
  console.log('Đã áp dụng bộ lọc', filters.value);
};

const resetFilters = () => {
  filters.value = {
    startDate: '2023-02-01',
    endDate: '2023-02-28',
    employee: '',
  };
  console.log('Đã đặt lại bộ lọc');
};

const format = (value) => {
  return new Intl.NumberFormat('vi-VN').format(value);
};
</script>

<style scoped>
.table-info th, .table-info td {
  border-top: 2px solid #dee2e6;
}
</style>

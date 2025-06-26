<template>
  <div class="customer-list-container">
    <el-card class="box-card">
      <div class="card-header">
        <h3 class="title">Danh sách khách hàng</h3>
        <el-button type="success" :icon="Plus" @click="goToAddCustomer" size="default">
          Thêm khách hàng
        </el-button>
      </div>

      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="Tìm kiếm theo tên, mã, email, SĐT..."
          :prefix-icon="Search"
          clearable
          @clear="fetchCustomers"
          @keyup.enter="handleSearch"
          class="search-input"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">Tìm kiếm</el-button>
        <el-button :icon="Refresh" @click="resetSearch">Reset</el-button>
      </div>

      <el-table
        :data="customers"
        border
        stripe
        v-loading="loading"
        element-loading-text="Đang tải dữ liệu..."
        empty-text="Không có dữ liệu khách hàng nào."
        class="customer-table"
      >
        <el-table-column type="index" label="#" width="60" :index="indexMethod" />
        <el-table-column prop="customerCode" label="Mã khách hàng" width="150" sortable />
        <el-table-column prop="customerName" label="Tên khách hàng" sortable />
        <el-table-column prop="email" label="Email" />
        <el-table-column prop="phone" label="Số điện thoại" width="140" />
        <el-table-column label="Giới Tính" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.gender === 1 ? 'info' : ''" effect="plain">
              {{ scope.row.gender === 1 ? 'Nam' : 'Nữ' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Ngày sinh" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.dateOfBirth) }}
          </template>
        </el-table-column>
        <el-table-column label="Trạng thái" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" effect="dark">
              {{ scope.row.status === 1 ? 'Hoạt động' : 'Không hoạt động' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Ngày tạo" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.createdDate) }}
          </template>
        </el-table-column>
        <el-table-column label="Hành động" width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              :icon="Edit"
              size="small"
              circle
              @click="goToEditCustomer(scope.row.id)"
              title="Sửa"
            />
            <el-button
              type="danger"
              :icon="Delete"
              size="small"
              circle
              @click="confirmDeleteCustomer(scope.row.id)"
              title="Xóa"
            />
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          v-model:current-page="currentPage"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalElements"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// Import the pre-configured apiClient instance
import apiClient from '@/utils/axiosInstance'; 
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue';

const router = useRouter();

// --- State Variables ---
const customers = ref([]);
const currentPage = ref(1); // Current page (1-indexed for el-pagination)
const size = ref(10); // Items per page
const totalElements = ref(0); // Total number of items
const loading = ref(false); // Loading state for the table
const searchKeyword = ref(''); // Search keyword

// --- Data Fetching ---
const fetchCustomers = async () => {
  loading.value = true;
  try {
    const res = await apiClient.get(`/admin/customers/phan-trang`, {
      params: {
        page: currentPage.value - 1,
        size: size.value,
        keyword: searchKeyword.value.trim() !== '' ? searchKeyword.value.trim() : null,
      },
    });

    console.log('API Response:', res.data);

    customers.value = res.data?.content || [];
    totalElements.value = res.data?.page?.totalElements ?? 0;

    if (customers.value.length === 0 && currentPage.value > 1 && totalElements.value > 0) {
      console.log('No data on current page, adjusting to last valid page');
      currentPage.value = Math.max(1, Math.ceil(totalElements.value / size.value));
      await fetchCustomers();
    } else if (totalElements.value === 0) {
      console.log('No data available, resetting to page 1');
      currentPage.value = 1;
    }

  } catch (err) {
    console.error('Lỗi tải danh sách khách hàng:', err);

    // 👉 Nếu lỗi là 403 thì chuyển hướng
    if (err.response && err.response.status === 403) {
      router.push('/error');
      return;
    }

    ElMessage.error('Không thể tải dữ liệu khách hàng. Vui lòng thử lại sau.');
    customers.value = [];
    totalElements.value = 0;
    currentPage.value = 1;
  } finally {
    loading.value = false;
  }
};

// --- Pagination Handlers ---
const handleSizeChange = (newSize) => {
  console.log('Page size changed to:', newSize);
  size.value = newSize;
  currentPage.value = 1; // Reset to first page
  fetchCustomers();
};

const handleCurrentChange = (newPage) => {
  console.log('Page changed to:', newPage);
  currentPage.value = newPage;
  fetchCustomers();
};

// --- Table Utilities ---
const indexMethod = (index) => {
  return (currentPage.value - 1) * size.value + index + 1;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) {
    return dateStr;
  }
  return date.toLocaleDateString('vi-VN');
};

// --- Navigation ---
const goToAddCustomer = () => {
  router.push({ name: 'AddCustomer' });
};

const goToEditCustomer = (id) => {
  router.push({ name: 'UpdateCustomer', params: { id: id } });
};

// --- Search Functionality ---
const handleSearch = () => {
  console.log('Search triggered with keyword:', searchKeyword.value);
  currentPage.value = 1; // Reset to first page
  fetchCustomers();
};

const resetSearch = () => {
  console.log('Search reset');
  searchKeyword.value = '';
  currentPage.value = 1; // Reset to first page
  fetchCustomers();
};

// --- Customer Actions (Delete) ---
const confirmDeleteCustomer = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc chắn muốn xóa khách hàng này?', 'Cảnh báo', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning',
    });
    await deleteCustomer(id);
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác xóa.');
    } else {
      console.error('Lỗi xác nhận xóa:', error);
      ElMessage.error('Có lỗi xảy ra khi xác nhận xóa.');
    }
  }
};

const deleteCustomer = async (id) => {
  try {
    // Use apiClient instead of axios directly
    await apiClient.delete(`/admin/customers/${id}`);
    ElMessage.success('Xóa khách hàng thành công!');
    if (customers.value.length === 1 && currentPage.value > 1) {
      currentPage.value--;
    }
    await fetchCustomers();
  } catch (err) {
    console.error('Lỗi khi xóa khách hàng:', err);
    ElMessage.error('Không thể xóa khách hàng. Vui lòng thử lại.');
  }
};

// --- Lifecycle Hook ---
onMounted(() => {
  console.log('Component mounted, fetching customers');
  fetchCustomers();
});
</script>

<style scoped>
.customer-list-container {
  max-width: 1300px;
  margin: 40px auto;
  padding: 20px;
}

.box-card {
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 18px;
  border-bottom: 1px solid #ebeef5;
}

.title {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: bold;
}

.search-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  max-width: 400px;
}

.customer-table {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  margin-top: 0;
}

.customer-table .el-table__header-wrapper th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
}

.customer-table .el-table__cell {
  padding: 10px 0;
}

.el-tag {
  font-weight: 500;
  border-radius: 4px;
}

.el-tag.el-tag--success {
  background-color: #e6f7ff;
  color: #1890ff;
  border-color: #91d5ff;
}

.el-tag.el-tag--danger {
  background-color: #fff1f0;
  color: #f5222d;
  border-color: #ffa39e;
}

.el-tag.el-tag--info {
  background-color: #f0f2f5;
  color: #606266;
  border-color: #d9d9d9;
}

.el-button.el-button--small.is-circle {
  padding: 6px;
  font-size: 14px;
}

.pagination-container {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
}

.el-pagination {
  --el-pagination-font-size: 0.875rem;
  --el-pagination-button-width: 36px;
  --el-pagination-button-height: 36px;
}

.el-pagination.is-background .el-pager li,
.el-pagination.is-background .btn-prev,
.el-pagination.is-background .btn-next {
  background-color: #f0f2f5;
  border-radius: 6px;
  transition: background-color 0.3s ease;
}

.el-pagination.is-background .el-pager li:not(.is-disabled):hover {
  background-color: #e0e6ed;
}

.el-pagination.is-background .el-pager li:not(.is-disabled).is-active {
  background-color: #409eff;
  color: #fff;
  font-weight: bold;
}

.el-pagination__total,
.el-pagination__sizes,
.el-pagination__jump {
  font-size: 0.9rem;
  color: #606266;
}

@media (max-width: 768px) {
  .customer-list-container {
    padding: 10px;
    margin: 20px auto;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .title {
    font-size: 24px;
  }
  .search-section {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input {
    max-width: 100%;
  }
  .el-table {
    font-size: 12px;
  }
}
</style>
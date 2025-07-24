<template>
  <div class="p-8 bg-gray-50 min-h-screen font-sans">
    <el-tabs
      v-model="currentTab"
      @tab-change="search"
      type="card"
      class="mb-8 bg-white rounded-xl shadow-lg"
    >
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.key"
        :label="`${tab.label} (${tab.count})`"
        :name="tab.key"
      />
    </el-tabs>

    <el-card class="shadow-lg rounded-xl overflow-hidden">
      <el-table
        :data="invoices"
        v-loading="loading"
        border
        stripe
        class="w-full text-lg"
        row-class-name="hover:bg-gray-100 transition-colors duration-200"
        header-cell-class-name="bg-blue-50 text-gray-700 font-semibold"
      >
        <el-table-column label="STT" type="index" width="70" align="center" />
        <el-table-column prop="invoiceCode" label="Mã hóa đơn" width="160" />
        <el-table-column prop="totalAmount" label="Tổng tiền" width="140" align="right">
          <template #default="scope">{{ formatCurrency(scope.row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="shippingFee" label="Tiền ship" width="120" align="right">
          <template #default="scope">{{ formatCurrency(scope.row.shippingFee) }}</template>
        </el-table-column>
        <el-table-column prop="amountToPay" label="Tiền phải trả" width="140" align="right">
          <template #default="scope">{{ formatCurrency(scope.row.amountToPay) }}</template>
        </el-table-column>
        <el-table-column prop="createdDate" label="Ngày tạo" width="200">
          <template #default="scope">
            <el-tooltip :content="formatDate(scope.row.createdDate)" placement="top">
              <span>{{ formatDate(scope.row.createdDate) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
<el-table-column label="Hành động" width="220" align="center" fixed="right">
  <template #default="scope">
    <el-button
      type="primary"
      :icon="View"
      size="default"
      @click="goToStatusPage(scope.row.invoiceId)"
    >
      Chi tiết
    </el-button>

    <!--  Chuyển v-if trực tiếp vào el-button -->
    <el-button
      v-if="currentTab === 'GIAO_THANH_CONG'"
      type="danger"
      :icon="Refresh"
      size="default"
      class="ml-2"
      @click="openReturnDialog(scope.row)"
    >
      Trả hàng/Hoàn tiền
    </el-button>

<el-button
  v-if="currentTab === 'GIAO_THANH_CONG'"
  type="danger"
  :icon="Refresh"
  size="default"
  class="ml-2"
  @click="openReviewDialog(scope.row)"
>
  Đánh giá sản phẩm
</el-button>

  </template>
</el-table-column>


      </el-table>
    </el-card>

<el-dialog
      v-model="returnDialogVisible"
      title="Bạn muốn trả hàng vì lý do gì?"
      width="35%"
      class="return-dialog-custom"
      destroy-on-close
    >
      <div class="dialog-content-wrapper text-left space-y-4">
        <p class="dialog-description-text">Vui lòng chọn lý do phù hợp nhất để chúng tôi xử lý yêu cầu trả hàng/hoàn tiền của bạn:</p>
        <el-button
          type="warning"
          plain
          class="w-full return-reason-button"
          @click="handleReturnChoice('ISSUE')"
        >
          <div class="button-content">
            <el-icon><WarningFilled /></el-icon>
            <span class="button-text">Đã nhận hàng nhưng lỗi, khác mô tả, không giống mẫu đặt</span>
          </div>
        </el-button>
        <el-button
          type="info"
          plain
          class="w-full return-reason-button"
          @click="handleReturnChoice('MISSING')"
        >
          <div class="button-content">
            <el-icon><QuestionFilled /></el-icon>
            <span class="button-text">Không nhận được hàng hoặc nhận thiếu hàng</span>
          </div>
        </el-button>
      </div>
    </el-dialog>

 <el-dialog
  v-model="reviewDialogVisible"
  title="Đánh giá sản phẩm"
  width="600px"
>
  <div
    v-for="(item, index) in selectedOrderProducts"
    :key="item.productId"
    class="mb-6 border-b pb-4"
  >
    <div class="flex items-center justify-between mb-2">
      <div class="font-medium text-gray-700">
       Product Name: {{ item.productName }} (ID: {{ item.productId }})
      </div>
      <el-rate
        v-model="item.rating"
        :max="5"
        allow-half
        show-score
        score-template="{value} sao"
      />
    </div>

    <el-input
      v-model="item.comment"
      type="textarea"
      :rows="3"
      placeholder="Nhập nhận xét của bạn"
    />
  </div>

  <template #footer>
    <el-button @click="reviewDialogVisible = false">Hủy</el-button>
    <el-button type="primary" @click="submitReview">Gửi đánh giá</el-button>
  </template>
</el-dialog>

  </div>
</template>

  <script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import apiClient from '@/utils/axiosInstance'
import { View, Refresh } from '@element-plus/icons-vue' // Nhập icon View
import { ElMessage } from 'element-plus'

// Router để điều hướng
const router = useRouter()

// Trạng thái reactive
const filters = ref({ search: '', dateRange: [] })
const currentTab = ref('CHO_XU_LY') // Tab khởi tạo là 'Chờ xử lý'
const invoices = ref([])
const loading = ref(false)

// Tabs trạng thái đơn hàng
const tabs = ref([
  { key: 'CHO_XU_LY', label: 'Chờ xử lý', count: 0 },
  { key: 'DA_XU_LY', label: 'Đã xử lý', count: 0 },
  { key: 'CHO_GIAO_HANG', label: 'Chờ giao hàng', count: 0 },
  { key: 'DANG_GIAO_HANG', label: 'Đang giao hàng', count: 0 },
  { key: 'GIAO_THANH_CONG', label: 'Giao hàng thành công', count: 0 },
  { key: 'GIAO_THAT_BAI', label: 'Giao hàng thất bại', count: 0 },
  { key: 'HUY_DON', label: 'Đơn hàng hủy', count: 0 }
])

// Ánh xạ tên tab -> mã trạng thái
const statusLabelToCode = (label) => {
  const map = {
    'CHO_XU_LY': 0,
    'DA_XU_LY': 1,
    'CHO_GIAO_HANG': 3,
    'DANG_GIAO_HANG': 4,
    'GIAO_THANH_CONG': 5,
    'GIAO_THAT_BAI': 6,
    'DA_HOAN_THANH': 9,
    'HUY_DON': -2
  }
  return map[label] ?? null
}

// Gọi API tìm kiếm hóa đơn
const search = async () => {
  loading.value = true
  try {
    const statusDetailCode = statusLabelToCode(currentTab.value)

    if (statusDetailCode !== null) {
      console.log('Gửi yêu cầu API với statusDetail:', statusDetailCode) // Log debug
      const res = await apiClient.get('/admin/online-sales/get-order-customer', {
        params: {
          statusDetail: statusDetailCode
        }
      })
      console.log('Phản hồi API:', res.data) // Log debug
      if (res.data && Array.isArray(res.data)) {
        invoices.value = res.data
        // Cập nhật số lượng cho tab hiện tại
        tabs.value = tabs.value.map(tab =>
          tab.key === currentTab.value ? { ...tab, count: res.data.length } : tab
        )
      } else {
        console.warn('Dữ liệu trả về không phải mảng:', res.data)
        invoices.value = []
      }
    } else {
      console.warn('Không tìm thấy mã trạng thái cho tab hiện tại:', currentTab.value)
      invoices.value = []
    }
  } catch (err) {
    console.error('Lỗi khi gọi API tìm kiếm:', err.message)
    if (err.response) {
      console.error('Phản hồi lỗi từ server:', err.response.data)
    }
    invoices.value = []
  } finally {
    loading.value = false
  }
}

const fetchProductsByOrderId = async (orderId) => {
  console.log('id: ',orderId)
  try {
    const res = await apiClient.get(`/favorites/rating-product`, {
      params: { invoiceId: orderId }
    })
    return res.data
  } catch (err) {
    ElMessage.error('Không thể tải sản phẩm của đơn hàng')
    return []
  }
}



const selectedOrderProducts = ref([])
const reviewDialogVisible = ref(false)

const openReviewDialog = async (order) => {
  const res = await fetchProductsByOrderId(order.invoiceId); // gọi API thật
  selectedOrderProducts.value = res.map(product => ({
    productId: product.productId,
    productName: product.productName,
    rating: 0,
    comment: ''
  }));
  reviewDialogVisible.value = true;
};




// Gửi đánh giá
// const submitReview = async () => {
//   const dataToSubmit = selectedOrderProducts.value.map(item => ({
//     productId: item.productId,   // Đã có từ bước openReviewDialog
//     rate: item.rating,
//     comment: item.comment
//   }));
//   console.log('data submit: ',dataToSubmit)
//   try {
//     await apiClient.post('/favorites', dataToSubmit); // ✅ Sửa chỗ post()
//     ElMessage.success('Đã gửi đánh giá thành công!');
//     reviewDialogVisible.value = false;
//   } catch (error) {
//     console.error('Gửi đánh giá lỗi:', error);
//     ElMessage.error('Gửi đánh giá thất bại.');
//   }
// };

const submitReview = async () => {
  const firstProduct = selectedOrderProducts.value[0];

  const dataToSubmit = {
    productId: firstProduct.productId,
    rate: firstProduct.rating,
    comment: firstProduct.comment
  };

  console.log('data submit:', dataToSubmit);

  try {
    await apiClient.post('/favorites', dataToSubmit); // Gửi object, không phải array
    ElMessage.success('Đã gửi đánh giá thành công!');
    reviewDialogVisible.value = false;
  } catch (error) {
    console.error('Gửi đánh giá lỗi:', error);
    ElMessage.error('Gửi đánh giá thất bại.');
  }
};





// Đặt lại bộ lọc và tìm kiếm lại
const resetFilters = () => {
  filters.value = { search: '', dateRange: [] }
  search()
}

// Điều hướng đến trang chi tiết hóa đơn
const goToStatusPage = (invoiceId) => {
  console.log('Điều hướng đến ChiTietDonHang với id:', invoiceId) // Log debug
  router.push({ name: 'ChiTietDonHang', params: { id: invoiceId } })
}

const selectedInvoice = ref(null)
const returnDialogVisible = ref(false)

const openReturnDialog = (invoice) => {
  console.log('invoice id: ',invoice.id)
  selectedInvoice.value = invoice
  returnDialogVisible.value = true
}

const handleReturnChoice = (type) => {
  returnDialogVisible.value = false
  const invoiceId = selectedInvoice.value?.invoiceId

  if (!invoiceId) return

  if (type === 'ISSUE') {
    router.push({ name: 'DonHangVanDe', params: { invoiceId } })
  } else if (type === 'MISSING') {
    router.push({ name: 'DonHangChuaNhan', params: { invoiceId } })
  }
}


// Format tiền VND
const formatCurrency = (val) => (val || 0).toLocaleString('vi-VN') + ' ₫'

// Format ngày
const formatDate = (val) => val ? dayjs(val).format('DD/MM/YYYY HH:mm:ss') : ''


// Gọi tìm kiếm khi component được mount
onMounted(() => {
  search()
})
</script>

<style scoped>
/* Padding cơ bản và chiều cao tối thiểu */
.min-h-screen {
  min-height: 100vh;
}

/* Tăng kích thước phông chữ cho toàn bộ component */
.font-sans {
  font-family: 'Roboto', sans-serif; /* Ví dụ: sử dụng Roboto, bạn có thể cần import */
  font-size: 16px; /* Kích thước phông chữ cơ bản, sẽ được điều chỉnh cho nội dung bảng */
}

/* Tùy chỉnh kiểu cho El-Tabs để có giao diện hiện đại hơn */
.el-tabs--card > .el-tabs__header {
  border-bottom: none;
}

.el-tabs--card > .el-tabs__header .el-tabs__nav {
  border: none;
  background-color: transparent;
}

.el-tabs--card > .el-tabs__header .el-tabs__item {
  border: none;
  background-color: #f0f4f8; /* Xám xanh nhạt cho tab không hoạt động */
  color: #555;
  margin-right: 8px;
  border-radius: 8px;
  padding: 12px 20px; /* Padding lớn hơn cho tab */
  font-weight: 600;
  transition: all 0.3s ease;
  font-size: 1.1rem; /* Kích thước phông chữ lớn hơn cho nhãn tab */
}

.el-tabs--card > .el-tabs__header .el-tabs__item.is-active {
  background-color: #409eff; /* Màu xanh primary của Element Plus cho tab hoạt động */
  color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  font-size: 1.1rem;
}

.el-tabs--card > .el-tabs__header .el-tabs__item:hover:not(.is-active) {
  background-color: #e0e6ed;
}

/* Định kiểu riêng cho bảng */
.el-table {
  font-size: 1rem; /* Kích thước phông chữ tiêu chuẩn cho nội dung bảng */
}

.el-table th.el-table__cell {
  background-color: #e3f2fd !important; /* Màu xanh nhạt hơn cho tiêu đề bảng */
  color: #333;
  font-weight: bold;
  font-size: 1.05rem; /* Kích thước phông chữ lớn hơn một chút cho tiêu đề bảng */
  padding: 14px 0; /* Nhiều padding hơn cho tiêu đề */
}

.el-table .el-table__cell {
  padding: 12px 0; /* Nhiều padding hơn cho ô bảng */
}

/* Hiệu ứng hover cho hàng */
.el-table .hover\:bg-gray-100:hover {
  background-color: #f0f0f0 !important;
}

/* Định kiểu nút trong bảng */
.el-button {
  font-size: 0.95rem; /* Phông chữ nút lớn hơn một chút */
  padding: 8px 15px; /* Điều chỉnh padding nút */
}

/* Đảm bảo bảng vừa với phần tử cha và cung cấp thanh cuộn ngang nếu cần */
.el-card {
  overflow-x: auto; /* Đảm bảo cuộn ngang cho bảng nếu nội dung quá rộng */
}
</style>

<template>
  <div class="customer-list-container">
    <el-card class="box-card">
      <div class="card-header">
        <h3 class="title">Danh sách khách hàng</h3>

        <div class="actions">
          <el-button type="primary" :icon="Download" :loading="exporting" @click="exportExcel" size="default">
            Xuất Excel
          </el-button>

          <el-button type="success" :icon="Plus" @click="goToAddCustomer" size="default">
            Thêm khách hàng
          </el-button>
        </div>
      </div>


      <div class="search-section">
        <el-input v-model="searchKeyword" placeholder="Tìm kiếm theo tên, mã, email, SĐT..." :prefix-icon="Search"
          clearable @clear="fetchCustomers" @keyup.enter="handleSearch" class="search-input" />
        <el-button type="primary" :icon="Search" @click="handleSearch">Tìm kiếm</el-button>
        <el-button :icon="Refresh" @click="resetSearch">Reset</el-button>
      </div>

      <el-table :data="customers" border stripe v-loading="loading" element-loading-text="Đang tải dữ liệu..."
        empty-text="Không có dữ liệu khách hàng nào." class="customer-table" :row-class-name="tableRowClassName">
        <el-table-column type="index" label="#" width="60" :index="indexMethod" />
        <el-table-column prop="customerName" label="Tên khách hàng" sortable />
        <el-table-column prop="email" label="Email" />
        <el-table-column prop="phone" label="Số điện thoại" width="140" />
        <el-table-column label="Ngày tạo" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.createdDate) }}
          </template>
        </el-table-column>

        <!-- <el-table-column prop="trustScore" label="Điểm tin cậy" width="130" sortable>
          <template #default="scope">
            <el-tag :type="getTrustScoreTagType(scope.row.trustScore)" effect="light">
              {{ scope.row.trustScore }}
            </el-tag>
          </template>
        </el-table-column> -->

        <el-table-column label="Cảnh báo khách hàng" min-width="200">
          <template #default="scope">
            <div class="flex items-center space-x-2">
              <!-- <el-tag :type="scope.row.isBlacklisted ? 'danger' : 'success'" effect="dark" size="small">
        {{ scope.row.isBlacklisted ? 'Đang bị cấm' : 'Hoạt động' }}
      </el-tag> -->

              <template v-if="scope.row.blacklistReason && scope.row.blacklistReason.trim()">
                <span class="text-reason">Lý do: {{ scope.row.blacklistReason }}</span>
                <el-tag v-if="scope.row.blacklistEndDate" type="danger" size="small" effect="plain">
                  Đến {{ formatDate(scope.row.blacklistEndDate) }}
                </el-tag>
              </template>

              <template v-else>
                <div class="text-muted-reason"></div>
              </template>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Hành động" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" :icon="Edit" size="small" circle @click="goToEditCustomer(scope.row.id)"
              title="Sửa" />
            <el-button type="danger" :icon="Delete" size="small" circle @click="confirmDeleteCustomer(scope.row.id)"
              title="Xóa" />
            <el-button v-if="!scope.row.isBlacklisted" type="warning" :icon="CircleClose" size="small" circle
              @click="confirmBlacklistCustomer(scope.row.id)" title="Cấm khách hàng" />
            <el-button v-else type="success" :icon="CircleCheck" size="small" circle
              @click="confirmUnblacklistCustomer(scope.row.id)" title="Bỏ cấm khách hàng" />
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
          v-model:current-page="currentPage" :page-sizes="[5, 10, 20, 50]" :page-size="size"
          layout="total, sizes, prev, pager, next, jumper" :total="totalElements" background />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import apiClient from '@/utils/axiosInstance' // Đảm bảo đường dẫn này đúng trong dự án của bạn
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Search,
  Refresh,
  CircleClose,
  CircleCheck,
  Download
} from '@element-plus/icons-vue'

const router = useRouter()

// --- State Variables ---
const exporting = ref(false)
const customers = ref([])
const currentPage = ref(1)
const size = ref(10)
const totalElements = ref(0)
const loading = ref(false)
const searchKeyword = ref('')

const toNull = (v) => (v && v.trim() !== '' ? v.trim() : null)
const isPhoneLike = (v) => !!v && /^[0-9+\s-]{8,15}$/.test(v.trim())

// Từ searchKeyword suy ra tham số gọi /search
const normalizeDigits = (v) => (v || '').replace(/\D/g, '')
const deriveAdvancedFromKeyword = (kw) => {
  const k = (kw || '').trim()
  if (!k) return { name: null, phone: null, phoneSuffix: null }

  // Nếu đúng 5 chữ số -> coi là phoneSuffix
  if (/^\d{5}$/.test(k)) {
    return { name: null, phone: null, phoneSuffix: k }
  }

  // Nếu có ít nhất 3 chữ số -> coi là phone (contains)
  if (isPhoneLike(k)) {
    return { name: null, phone: k, phoneSuffix: null }
  }

  // còn lại coi là tên
  return { name: k, phone: null, phoneSuffix: null }
}


// --- Data Fetching ---
const fetchCustomers = async () => {
  loading.value = true
  try {
    const hasKeyword = (searchKeyword.value || '').trim() !== ''
    let res

    if (hasKeyword) {
      // Gọi endpoint tìm kiếm nâng cao
      const { name, phone, phoneSuffix } = deriveAdvancedFromKeyword(searchKeyword.value)
      res = await apiClient.get(`/admin/customers/search`, {
        params: {
          name: toNull(name),
          phone: toNull(phone),
          phoneSuffix: toNull(phoneSuffix),
          page: currentPage.value - 1,
          size: size.value,
          sort: 'createdDate,desc'
        }
      })
    } else {
      // Gọi endpoint phân trang mặc định
      res = await apiClient.get(`/admin/customers/phan-trang`, {
        params: {
          page: currentPage.value - 1,
          size: size.value
          // KHÔNG GỬI keyword vì BE không xử lý ở endpoint này
        },
      })
    }

    // Chuẩn hoá đọc dữ liệu
    customers.value = res.data?.content || []
    totalElements.value = res.data?.totalElements ?? res.data?.page?.totalElements ?? 0

    // Giữ hành vi chỉnh trang như cũ
    if (customers.value.length === 0 && currentPage.value > 1 && totalElements.value > 0) {
      currentPage.value = Math.max(1, Math.ceil(totalElements.value / size.value))
      await fetchCustomers()
    } else if (totalElements.value === 0) {
      currentPage.value = 1
    }
  } catch (err) {
    console.error('Lỗi tải danh sách khách hàng:', err)
    if (err.response && err.response.status === 403) {
      router.push('/error')
      return
    }
    ElMessage.error('Không thể tải dữ liệu khách hàng. Vui lòng thử lại sau.')
    customers.value = []
    totalElements.value = 0
    currentPage.value = 1
  } finally {
    loading.value = false
  }
}
// Lấy filename từ header Content-Disposition (nếu có)
const parseFilename = (cd) => {
  if (!cd) return 'customers.xlsx'
  const m = /filename\*=UTF-8''([^;]+)|filename="?([^\";]+)"?/i.exec(cd)
  return decodeURIComponent(m?.[1] || m?.[2] || 'customers.xlsx')
}

const exportExcel = async () => {
  exporting.value = true
  try {
    // Dùng đúng logic suy diễn name/phone theo keyword hiện tại
    const { name, phone } = deriveAdvancedFromKeyword(searchKeyword.value)
    const res = await apiClient.get(`/admin/customers/export`, {
      params: {
        name: toNull(name),
        phone: toNull(phone),
      },
      responseType: 'blob', // Bắt buộc để tải file nhị phân
    })

    const fileName = parseFilename(res.headers['content-disposition'])
    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName || 'customers.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)

    ElMessage.success('Đang tải file Excel…')
  } catch (err) {
    console.error('Export Excel error:', err)
    ElMessage.error('Xuất Excel thất bại. Vui lòng thử lại.')
  } finally {
    exporting.value = false
  }
}



// --- Pagination Handlers ---
const handleSizeChange = (newSize) => {
  size.value = newSize
  currentPage.value = 1 // Reset về trang đầu tiên khi thay đổi kích thước trang
  fetchCustomers()
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  fetchCustomers()
}

// --- Table Utilities ---
const indexMethod = (index) => {
  return (currentPage.value - 1) * size.value + index + 1
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) {
    return dateStr // Trả về chuỗi gốc nếu không phải định dạng ngày hợp lệ
  }
  // Định dạng ngày theo chuẩn Việt Nam (DD/MM/YYYY)
  return date.toLocaleDateString('vi-VN')
}

const getTrustScoreTagType = (score) => {
  if (score >= 80) return 'success' // Điểm cao
  if (score >= 50) return 'warning' // Điểm trung bình
  return 'danger' // Điểm thấp (nguy hiểm)
}

// Hàm này quyết định lớp CSS cho mỗi hàng trong bảng
const tableRowClassName = ({ row }) => {
  // Tô đỏ hàng nếu khách hàng bị đánh dấu là "isBlacklisted: true"
  // Điều này yêu cầu backend phải cung cấp trường 'isBlacklisted'
  if (row.isBlacklisted) {
    return 'danger-row' // Áp dụng lớp CSS 'danger-row' để tô đỏ
  }
  return '' // Không áp dụng lớp nào khác
}

// --- Navigation ---
const goToAddCustomer = () => {
  router.push({ name: 'AddCustomer' })
}

const goToEditCustomer = (id) => {
  router.push({ name: 'UpdateCustomer', params: { id: id } })
}

// --- Search Functionality ---
const handleSearch = () => {
  currentPage.value = 1 // Luôn tìm kiếm từ trang đầu tiên
  fetchCustomers()
}

const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  fetchCustomers()
}

// --- Customer Actions (Delete, Blacklist, Unblacklist) ---

// Xác nhận xóa khách hàng
const confirmDeleteCustomer = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc chắn muốn xóa khách hàng này?', 'Cảnh báo', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning',
    })
    await deleteCustomer(id)
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác xóa.')
    } else {
      console.error('Lỗi xác nhận xóa:', error)
      ElMessage.error('Có lỗi xảy ra khi xác nhận xóa.')
    }
  }
}

// Gửi yêu cầu xóa khách hàng đến API
const deleteCustomer = async (id) => {
  try {
    await apiClient.delete(`/admin/customers/${id}`)
    ElMessage.success('Xóa khách hàng thành công!')
    // Điều chỉnh trang nếu item cuối cùng của trang bị xóa
    if (customers.value.length === 1 && currentPage.value > 1) {
      currentPage.value--
    }
    await fetchCustomers() // Tải lại danh sách để cập nhật
  } catch (err) {
    console.error('Lỗi khi xóa khách hàng:', err)
    ElMessage.error('Không thể xóa khách hàng. Vui lòng thử lại.')
  }
}

// Xác nhận cấm khách hàng (hiển thị 2 hộp thoại: lý do và thời gian)
const confirmBlacklistCustomer = async (id) => {
  try {
    // Bước 1: Nhập lý do cấm
    const { value: reason } = await ElMessageBox.prompt(
      'Vui lòng nhập lý do cấm khách hàng:',
      'Cấm khách hàng',
      {
        confirmButtonText: 'Cấm',
        cancelButtonText: 'Hủy',
        inputType: 'textarea', // Cho phép nhập nhiều dòng
        inputPlaceholder: 'Lý do cấm (ví dụ: Vi phạm chính sách, hành vi không phù hợp)',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return 'Lý do cấm không được để trống.'
          }
          return true
        },
        inputErrorMessage: 'Lý do không hợp lệ.',
        showClose: false, // Không cho phép đóng bằng nút X
      },
    )

    if (reason) {
      // Nếu người dùng đã nhập lý do
      // Bước 2: Nhập số ngày cấm
      const { value: duration } = await ElMessageBox.prompt(
        'Nhập **số ngày cấm** khách hàng (để trống hoặc 0 nếu cấm vĩnh viễn):',
        'Thời gian cấm',
        {
          confirmButtonText: 'Cấm',
          cancelButtonText: 'Hủy',
          inputType: 'number', // Cho phép nhập số
          inputPlaceholder: 'Ví dụ: 30 (ngày)',
          inputValidator: (value) => {
            // Cho phép để trống (null) hoặc chuỗi rỗng để cấm vĩnh viễn
            if (value === null || value.trim() === '') {
              return true
            }
            const num = parseInt(value, 10)
            // Kiểm tra phải là số và không âm
            if (isNaN(num) || num < 0) {
              return 'Số ngày không hợp lệ. Vui lòng nhập số dương hoặc để trống.'
            }
            return true
          },
          inputErrorMessage: 'Số ngày không hợp lệ.',
          showClose: false,
        },
      )

      // Chuyển đổi duration sang số nguyên. Nếu là null/rỗng, giữ nguyên null để backend xử lý là vĩnh viễn.
      const durationInDays =
        duration === null || duration.trim() === '' ? null : parseInt(duration, 10)

      await blacklistCustomer(id, reason, durationInDays)
    }
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác cấm khách hàng.')
    } else {
      console.error('Lỗi xác nhận cấm khách hàng:', error)
      ElMessage.error('Có lỗi xảy ra khi xác nhận cấm khách hàng.')
    }
  }
}

// Gửi yêu cầu cấm khách hàng đến API
const blacklistCustomer = async (id, reason, durationInDays) => {
  try {
    // API call với lý do và số ngày cấm (durationInDays có thể là null)
    await apiClient.put(`/admin/customers/${id}/blacklist`, {
      reason: reason,
      durationInDays: durationInDays,
    })

    ElMessage.success('Đã cấm khách hàng thành công!')
    await fetchCustomers() // Tải lại danh sách để cập nhật trạng thái
  } catch (err) {
    console.error('Lỗi khi cấm khách hàng:', err)
    ElMessage.error('Không thể cấm khách hàng. Vui lòng thử lại.')
  }
}

// Xác nhận bỏ cấm khách hàng
const confirmUnblacklistCustomer = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc chắn muốn bỏ cấm khách hàng này?', 'Xác nhận', {
      confirmButtonText: 'Bỏ cấm',
      cancelButtonText: 'Hủy',
      type: 'info',
    })
    await unblacklistCustomer(id)
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác bỏ cấm khách hàng.')
    } else {
      console.error('Lỗi xác nhận bỏ cấm:', error)
      ElMessage.error('Có lỗi xảy ra khi xác nhận bỏ cấm.')
    }
  }
}

// Gửi yêu cầu bỏ cấm khách hàng đến API
const unblacklistCustomer = async (id) => {
  try {
    await apiClient.put(`/admin/customers/${id}/unblacklist`)
    ElMessage.success('Đã bỏ cấm khách hàng thành công!')
    await fetchCustomers() // Tải lại danh sách để cập nhật trạng thái
  } catch (err) {
    console.error('Lỗi khi bỏ cấm khách hàng:', err)
    ElMessage.error('Không thể bỏ cấm khách hàng. Vui lòng thử lại.')
  }
}

// --- Lifecycle Hook ---
onMounted(() => {
  fetchCustomers() // Tải dữ liệu khi component được mount
})
</script>

<style scoped>
/* Container chính cho toàn bộ trang */
.customer-list-container {
  max-width: 1300px;
  margin: 40px auto;
  padding: 20px;
}

/* Thẻ bao quanh nội dung */
.box-card {
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  /* Đổ bóng nhẹ nhàng */
}

/* Header của thẻ, chứa tiêu đề và nút thêm */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 18px;
  border-bottom: 1px solid #ebeef5;
  /* Đường kẻ dưới */
}

/* Tiêu đề chính */
.title {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: bold;
}

/* Phần tìm kiếm và lọc */
.search-section {
  display: flex;
  gap: 10px;
  /* Khoảng cách giữa các phần tử */
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  max-width: 400px;
}

/* Bảng hiển thị danh sách khách hàng */
.customer-table {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  /* Đảm bảo góc bo tròn */
  margin-top: 0;
}

/* Header của bảng */
.customer-table .el-table__header-wrapper th {
  background-color: #f5f7fa;
  /* Màu nền header */
  color: #606266;
  font-weight: bold;
}

.customer-table .el-table__cell {
  padding: 10px 0;
}

/* --- ĐIỂM QUAN TRỌNG: STYLE CHO DÒNG BỊ CẤM --- */
.el-table .danger-row {
  background-color: #fef0f0 !important;
  /* Màu nền đỏ nhạt cho hàng bị cấm */
  border-left: 8px solid #f56c6c;
  /* Tăng độ dày viền đỏ bên trái */
}

/* Đảm bảo tất cả các văn bản và phần tử trong ô của hàng này có màu đỏ đậm và in đậm */
.el-table .danger-row .el-table__cell {
  color: #a30000 !important;
  /* Màu chữ đỏ đậm cho toàn bộ nội dung ô */
  font-weight: bold !important;
  /* In đậm chữ trong toàn bộ nội dung ô */
}

.el-table .danger-row:hover>td {
  background-color: #fce7e7 !important;
  /* Đảm bảo màu hover cũng theo màu đỏ nhạt */
}

/* --- STYLE CỦA CÁC THẺ TAG --- */
.el-tag {
  font-weight: 500;
  border-radius: 4px;
}

/* Định nghĩa màu sắc cho các loại tag (nếu cần tùy chỉnh so với default của Element Plus) */
/* Cập nhật màu tag cho trạng thái Blacklisted/Active */
.el-tag.el-tag--success[effect='dark'] {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #fff;
}

.el-tag.el-tag--danger[effect='dark'] {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
}

/* Màu tag cho lý do cấm (plain effect) */
.el-tag.el-tag--danger[effect='plain'] {
  background-color: #fef0f0;
  /* Màu nền nhẹ nhàng hơn */
  color: #f56c6c;
  /* Màu chữ đỏ */
  border-color: #fde2e2;
}

/* Styles cho tag 'light' effect (cho điểm tin cậy) */
.el-tag.el-tag--success[effect='light'] {
  background-color: #f0f9eb;
  /* Light green */
  color: #67c23a;
  border-color: #e1f3d8;
}

.el-tag.el-tag--warning[effect='light'] {
  background-color: #fdf6ec;
  /* Light orange */
  color: #e6a23c;
  border-color: #faecd8;
}

.el-tag.el-tag--danger[effect='light'] {
  background-color: #fef0f0;
  /* Light red */
  color: #f56c6c;
  border-color: #fde2e2;
}

/* --- STYLE CỦA CÁC NÚT HÀNH ĐỘNG --- */
.el-button.el-button--small.is-circle {
  padding: 6px;
  font-size: 14px;
  margin-left: 5px;
}

/* Tùy chỉnh màu nút warning (ví dụ cho nút "Cấm khách hàng") */
.el-button--warning {
  --el-button-bg-color: var(--el-color-warning);
  --el-button-border-color: var(--el-color-warning);
  --el-button-hover-bg-color: var(--el-color-warning-light-3);
  --el-button-hover-border-color: var(--el-color-warning-light-3);
  --el-button-active-bg-color: var(--el-color-warning-dark-2);
  --el-button-active-border-color: var(--el-color-warning-dark-2);
}

/* --- PHÂN TRANG --- */
.pagination-container {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
  /* Căn phải */
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
  /* Màu xanh nổi bật cho trang hiện tại */
  color: #fff;
  font-weight: bold;
}

.el-pagination__total,
.el-pagination__sizes,
.el-pagination__jump {
  font-size: 0.9rem;
  color: #606266;
}

/* --- UTILITIES --- */
.mt-1 {
  margin-top: 4px;
}

.ml-1 {
  margin-left: 4px;
}

.ml-2 {
  margin-left: 8px;
}

/* Để đảm bảo màu chữ trong phần lý do cấm cũng là màu đỏ đã chọn */
.text-reason {
  color: #a30000;
  /* Màu chữ cho "Lý do:" */
}

.text-muted-reason {
  color: #a30000;
  /* Màu chữ cho "Không có lý do cụ thể" */
}

/* --- RESPONSIVE DESIGN --- */
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

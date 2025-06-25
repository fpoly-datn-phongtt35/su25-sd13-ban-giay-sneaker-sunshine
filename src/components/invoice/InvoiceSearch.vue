<template>
  <div>
    <div class="search-bar-container">
      <el-input
        v-model="keyword"
        placeholder="Tìm mã hóa đơn, tên khách hàng..."
        @input="onInput"
        clearable
        :prefix-icon="Search"
      />

      <el-select v-model="status" @change="onStatusChange" placeholder="Tất cả trạng thái" clearable>
        <el-option label="Tất cả trạng thái" value="" />
        <el-option label="Chờ xử lý" value="0" />
        <el-option label="Đã thanh toán" value="1" />
        <el-option label="Đã hủy" value="2" />
      </el-select>

      <el-date-picker
        v-model="createdDate"
        type="date"
        placeholder="Chọn ngày tạo"
        @change="onCreatedDateChange"
        clearable
        format="DD/MM/YYYY"
        value-format="YYYY-MM-DD"
      />

      <el-button
        type="primary"
        @click="startScan"
        :disabled="scanning"
        :loading="scanning"
        :icon="Camera"
      >
        Quét QR
      </el-button>
    </div>

    <el-card v-if="scanning" class="qr-scanner-container mt-3" shadow="never">
       <template #header>
        <div class="d-flex justify-content-between align-items-center">
            <span>Đưa mã QR vào vùng camera</span>
            <el-button type="danger" @click="stopScan" :icon="CircleClose" circle/>
        </div>
      </template>
      <div id="qr-reader" style="width: 100%;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, Camera, CircleClose } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'
// THAY ĐỔI: Import instance apiClient đã được cấu hình sẵn
import apiClient from '@/utils/axiosInstance.js' // <-- Điều chỉnh đường dẫn nếu cần

// State
const keyword = ref('')
const status = ref('')
const createdDate = ref('')
const scanning = ref(false)
let html5QrCode = null

// Emits
const emit = defineEmits(['search', 'clear'])

// --- Search & Filter Logic ---

let debounceTimer = null

function emitSearch() {
  const statusValue = status.value === '' ? undefined : Number(status.value)
  // createdDate đã là string YYYY-MM-DD, không cần convert
  emit('search', { keyword: keyword.value.trim(), status: statusValue, createdDate: createdDate.value })
}

function onInput() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    emitSearch()
  }, 300)
}

function onStatusChange() {
  emitSearch()
}

function onCreatedDateChange() {
  emitSearch()
}

// Function này có thể được gọi từ component cha nếu cần nút "Clear"
function onClearClick() {
  keyword.value = ''
  status.value = ''
  createdDate.value = ''
  emit('clear')
}

// --- QR Code Scanning ---

async function startScan() {
  scanning.value = true;
  await nextTick(); // Đợi div #qr-reader hiển thị

  html5QrCode = new Html5Qrcode("qr-reader");

  try {
    await html5QrCode.start(
      { facingMode: "environment" }, // camera sau
      { fps: 60, qrbox: { width: 250, height: 250 } },
      qrCodeMessage => {
        // Khi quét thành công
        stopScan();
        sendQRCodeToServer(qrCodeMessage);
      },
      errorMessage => {
        // Lỗi có thể bỏ qua, không cần thông báo
      }
    );
  } catch (err) {
    ElMessage.error('Không thể truy cập camera: ' + err);
    scanning.value = false;
  }
}

async function stopScan() {
  if (html5QrCode && scanning.value) {
    try {
      await html5QrCode.stop();
    } catch (err) {
      console.warn('Lỗi khi dừng camera:', err);
    }
  }
  scanning.value = false;
}

async function sendQRCodeToServer(qrCode) {
  try {
    // THAY ĐỔI: Sử dụng apiClient và rút gọn URL
    const response = await apiClient.post('/admin/invoices/qr-scan', {
        invoiceCode: qrCode
    });

    const data = response.data;
    const invoice = data.invoice;
    const details = data.details;

    // Tạo nội dung HTML để hiển thị
    let productsHtml = '<ul style="text-align:left; padding-left: 20px; margin:0;">';
    details.forEach(item => {
      productsHtml += `
        <li>
          <b>${item.productName}</b> (Size: ${item.size.sizeName}, Màu: ${item.color.colorName})<br>
          <small>${item.quantity} x ${item.price.toLocaleString('vi-VN')} VND</small>
        </li>`;
    });
    productsHtml += '</ul>';

    const htmlContent = `
      <div style="text-align:left; font-size: 1rem;">
        <p><strong>Mã HĐ:</strong> ${invoice.invoiceCode}</p>
        <p><strong>Khách hàng:</strong> ${invoice.customerName || 'Khách lẻ'}</p>
        <p><strong>Trạng thái:</strong> ${invoice.status === 1 ? 'Đã thanh toán' : 'Chờ xử lý'}</p>
        <hr>
        <p><strong>Chi tiết sản phẩm:</strong></p>
        ${productsHtml}
        <hr>
        <p style="text-align:right; font-size: 1.1rem;"><strong>Tổng tiền: ${invoice.finalAmount.toLocaleString('vi-VN')} VND</strong></p>
      </div>`;

    ElMessageBox.alert(htmlContent, 'Thông tin hóa đơn', {
      dangerouslyUseHTMLString: true,
      width: '500px',
    });

  } catch (error) {
    const errorMessage = error.response?.data || error.message;
    ElMessage.error(`Lỗi: ${errorMessage}`);
  }
}
</script>

<style scoped>
.search-bar-container {
  display: flex;
  gap: 10px;
  align-items: center;
}
.qr-scanner-container {
  max-width: 400px;
}
.mt-3 {
    margin-top: 1rem;
}
.d-flex {
    display: flex;
}
.justify-content-between {
    justify-content: space-between;
}
.align-items-center {
    align-items: center;
}
/* Tùy chỉnh chiều rộng cho select và date-picker nếu cần */
.el-select, .el-date-editor {
  width: 200px;
}
</style>
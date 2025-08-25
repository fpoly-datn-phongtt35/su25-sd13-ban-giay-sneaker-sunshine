<template>
  <div>
    <div class="search-bar-container">
      <el-input
        v-model="keyword"
        placeholder="Tìm mã hóa đơn, tên/SĐT khách hàng..."
        @input="emitSearch"
        clearable
        :prefix-icon="Search"
      />

      <!-- Trạng thái TẠI QUẦY (orderType = 0) -->
      <el-select
        v-model="counterStatusKey"
        @change="emitSearch"
        placeholder="Trạng thái tại quầy"
        clearable
      >
        <el-option
          v-for="opt in COUNTER_STATUS_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>

      <!-- Trạng thái ONLINE (orderType = 1) -->
      <el-select
        v-model="onlineStatusKey"
        @change="emitSearch"
        placeholder="Trạng thái online"
        clearable
      >
        <el-option
          v-for="opt in ONLINE_STATUS_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>

      <el-date-picker
        v-model="createdDate"
        type="date"
        placeholder="Chọn ngày tạo"
        @change="emitSearch"
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

      <el-button class="ms-2" @click="onClearClick">
        Xoá lọc
      </el-button>
    </div>

    <!-- QR scanner -->
    <el-card v-if="scanning" class="qr-scanner-container mt-3" shadow="never">
      <template #header>
        <div class="d-flex justify-content-between align-items-center">
          <span>Đưa mã QR vào vùng camera</span>
          <el-button type="danger" @click="stopScan" :icon="CircleClose" circle />
        </div>
      </template>
      <div id="qr-reader" style="width: 100%;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Camera, CircleClose } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'
import apiClient from '@/utils/axiosInstance.js'

const emit = defineEmits(['search', 'clear'])

const keyword = ref('')
const counterStatusKey = ref('')   // trạng thái tại quầy (TrangThaiTong)
const onlineStatusKey  = ref('')   // trạng thái online (TrangThaiChiTiet)
const createdDate = ref('')        // 'YYYY-MM-DD'

// TẠI QUẦY: chỉ 3 trạng thái
const COUNTER_STATUS_OPTIONS = [
  { label: 'Tất cả (tại quầy)', value: '' },
  { label: 'Chờ xử lý',         value: 'DANG_XU_LY' },
  { label: 'Thành công',        value: 'THANH_CONG' },
  { label: 'Đã hủy',            value: 'DA_HUY' },
]

// ONLINE: đúng như ảnh bạn gửi
const ONLINE_STATUS_OPTIONS = [
  { label: 'Tất cả (online)',     value: '' },
  { label: 'Chờ xử lý',           value: 'CHO_XU_LY' },
  { label: 'Đã xử lý',            value: 'DA_XU_LY' },
  { label: 'Chờ giao hàng',       value: 'CHO_GIAO_HANG' },
  { label: 'Đang giao hàng',      value: 'DANG_GIAO_HANG' },
  { label: 'Giao hàng thành công', value: 'GIAO_THANH_CONG' },
  { label: 'Giao hàng thất bại',  value: 'GIAO_THAT_BAI' },
  { label: 'Hủy đơn',             value: 'HUY_DON' },
]

let debounceTimer = null
function emitSearch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    emit('search', {
      keyword: (keyword.value || '').trim() || undefined,
      counterStatusKey: counterStatusKey.value || undefined,
      onlineStatusKey:  onlineStatusKey.value  || undefined,
      createdDate: createdDate.value || undefined,
    })
  }, 250)
}

function onClearClick() {
  keyword.value = ''
  counterStatusKey.value = ''
  onlineStatusKey.value = ''
  createdDate.value = ''
  emit('clear')
}

/* ===== QR Scan (tuỳ chọn) ===== */
const scanning = ref(false)
let html5QrCode = null

async function startScan () {
  scanning.value = true
  await nextTick()
  html5QrCode = new Html5Qrcode('qr-reader')
  try {
    await html5QrCode.start(
      { facingMode: 'environment' },
      { fps: 60, qrbox: { width: 250, height: 250 } },
      msg => { stopScan(); sendQRCodeToServer(msg) },
      _ => {}
    )
  } catch (err) {
    ElMessage.error('Không thể truy cập camera: ' + err)
    scanning.value = false
  }
}
async function stopScan () {
  if (html5QrCode && scanning.value) {
    try { await html5QrCode.stop() } catch (e) { console.warn(e) }
  }
  scanning.value = false
}
async function sendQRCodeToServer (qrCode) {
  try {
    const { data } = await apiClient.post('/admin/invoices/qr-scan', { invoiceCode: qrCode })
    const invoice = data.invoice
    const details = data.details || []

    let productsHtml = '<ul style="text-align:left; padding-left: 20px; margin:0;">'
    details.forEach(item => {
      const qty = item.quantity ?? 0
      const price = (item.price ?? 0).toLocaleString('vi-VN')
      const sizeName = item.size?.sizeName ?? '—'
      const colorName = item.color?.colorName ?? '—'
      productsHtml += `
        <li>
          <b>${item.productName}</b> (Size: ${sizeName}, Màu: ${colorName})<br>
          <small>${qty} x ${price} VND</small>
        </li>`
    })
    productsHtml += '</ul>'

    const total = (invoice.finalAmount ?? 0).toLocaleString('vi-VN')
    const kh = invoice.customerName || 'Khách lẻ'
    const statusLabel = invoice.status === 1 ? 'Đã thanh toán' : 'Chờ xử lý'

    const htmlContent = `
      <div style="text-align:left; font-size: 1rem;">
        <p><strong>Mã HĐ:</strong> ${invoice.invoiceCode}</p>
        <p><strong>Khách hàng:</strong> ${kh}</p>
        <p><strong>Trạng thái:</strong> ${statusLabel}</p>
        <hr>
        <p><strong>Chi tiết sản phẩm:</strong></p>
        ${productsHtml}
        <hr>
        <p style="text-align:right; font-size: 1.1rem;"><strong>Tổng tiền: ${total} VND</strong></p>
      </div>`
    ElMessageBox.alert(htmlContent, 'Thông tin hóa đơn', { dangerouslyUseHTMLString: true, width: '500px' })
  } catch (error) {
    ElMessage.error(`Lỗi: ${error?.response?.data || error?.message}`)
  }
}
</script>

<style scoped>
.search-bar-container { display: flex; gap: 10px; align-items: center; }
.qr-scanner-container { max-width: 400px; }
.mt-3 { margin-top: 1rem; }
.ms-2 { margin-left: 0.5rem; }
.d-flex { display: flex; }
.justify-content-between { justify-content: space-between; }
.align-items-center { align-items: center; }
.el-select, .el-date-editor { width: 220px; }
</style>

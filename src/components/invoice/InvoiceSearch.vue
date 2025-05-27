<template>
  <div class="d-flex mb-3 gap-2 align-items-center">
    <input
      type="text"
      class="form-control"
      placeholder="Tìm kiếm mã hóa đơn, tên khách hàng, số điện thoại..."
      v-model="keyword"
      @input="onInput"
      autofocus
    />

    <select class="form-select w-auto" v-model="status" @change="onStatusChange">
      <option value="">Tất cả trạng thái</option>
      <option value="0">Chờ xử lý</option>
      <option value="1">Đã thanh toán</option>
      <option value="2">Đã hủy</option>
    </select>

    <input
      type="date"
      class="form-control w-auto"
      v-model="createdDate"
      @change="onCreatedDateChange"
      placeholder="Ngày tạo"
    />

    <button class="btn btn-success btn-sm" @click="exportExcel">
      Xuất Excel
    </button>

    <button class="btn btn-primary btn-sm" @click="startScan" :disabled="scanning">
      Quét QR
    </button>
  </div>

  <!-- Khu vực hiển thị camera khi quét QR -->
  <div v-if="scanning" class="qr-scanner-container mt-3">
    <div id="qr-reader" style="width: 300px;"></div>
    <button class="btn btn-danger btn-sm mt-2" @click="stopScan">Dừng quét</button>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import Swal from 'sweetalert2'
import { Html5Qrcode } from 'html5-qrcode'

const keyword = ref('')
const status = ref('')
const createdDate = ref('')

const scanning = ref(false)
let html5QrCode = null

const emit = defineEmits(['search', 'clear'])

let debounceTimer = null

function emitSearch() {
  const statusValue = status.value === '' ? undefined : Number(status.value)
  const dateValue = createdDate.value ? new Date(createdDate.value) : undefined

  emit('search', { keyword: keyword.value.trim(), status: statusValue, createdDate: dateValue })
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

function onClearClick() {
  keyword.value = ''
  status.value = ''
  createdDate.value = ''
  emit('clear')
}

async function exportExcel() {
  const result = await Swal.fire({
    title: 'Bạn có chắc muốn xuất toàn bộ danh sách hóa đơn ra file Excel?',
    showCancelButton: true,
    confirmButtonText: 'Có',
    cancelButtonText: 'Hủy',
    icon: undefined  // Không hiển thị icon
  })

  if (!result.isConfirmed) return

  try {
    const response = await fetch('http://localhost:8080/api/invoices/export-all-excel', {
      method: 'GET',
      headers: {
        'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      },
    })
    if (!response.ok) throw new Error('Lỗi tải file Excel')

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_hoa_don.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    Swal.fire('Lỗi', 'Xuất Excel thất bại: ' + error.message, 'error')
  }
}

// --- Quét QR Code ---

async function startScan() {
  scanning.value = true
  await nextTick() // Đợi div #qr-reader hiển thị

  html5QrCode = new Html5Qrcode("qr-reader")

  html5QrCode.start(
    { facingMode: "environment" }, // camera sau
    {
      fps: 10,
      qrbox: 250
    },
    qrCodeMessage => {
      // Khi quét thành công
      stopScan()
      sendQRCodeToServer(qrCodeMessage)
    },
    errorMessage => {
      // Có thể log lỗi nhỏ khi quét, không cần báo người dùng
      // console.log('QR scan error:', errorMessage)
    }
  ).catch(err => {
    Swal.fire('Lỗi', 'Không thể truy cập camera: ' + err, 'error')
    scanning.value = false
  })
}

async function stopScan() {
  if (html5QrCode) {
    try {
      await html5QrCode.stop()
      html5QrCode.clear()
    } catch (err) {
      console.warn('Lỗi khi dừng camera:', err)
    }
  }
  scanning.value = false
}

async function sendQRCodeToServer(qrCode) {
  try {
    const response = await fetch('http://localhost:8080/api/invoices/qr-scan', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ invoiceCode: qrCode }),
    })

    if (!response.ok) {
      const errorText = await response.text()
      Swal.fire('Lỗi', `Không tìm thấy hóa đơn: ${errorText}`, 'error')
      return
    }

    const data = await response.json()

    // Tạo nội dung html hiển thị thông tin hóa đơn và chi tiết
    const invoice = data.invoice
    const details = data.details

    // format ngày tạo
    const createdDate = new Date(invoice.createdDate).toLocaleString()

    // Tạo html danh sách sản phẩm
    let productsHtml = '<ul style="text-align:left; padding-left: 20px;">'
    details.forEach(item => {
      productsHtml += `
        <li>
          <b>${item.productName}</b> (${item.categoryName})<br>
          Size: ${item.size.sizeName}, Màu: ${item.color.colorName}<br>
          Số lượng: ${item.quantity}, Giá: ${item.price.toLocaleString()} VND
        </li>
      `
    })
    productsHtml += '</ul>'

    const htmlContent = `
      <div style="text-align:left;">
        <p><b>Mã hóa đơn:</b> ${invoice.invoiceCode}</p>
        <p><b>Khách hàng:</b> ${invoice.customerName}</p>
        <p><b>Nhân viên:</b> ${invoice.employeeName}</p>
        <p><b>Ngày tạo:</b> ${createdDate}</p>
        <p><b>Trạng thái:</b> ${invoice.status === 0 ? 'Chờ xử lý' : invoice.status === 1 ? 'Đã thanh toán' : 'Đã hủy'}</p>
        <p><b>Tổng tiền:</b> ${invoice.totalAmount.toLocaleString()} VND</p>
        <p><b>Chi tiết sản phẩm:</b></p>
        ${productsHtml}
      </div>
    `

    Swal.fire({
      title: 'Thông tin hóa đơn',
      html: htmlContent,
      icon: 'info',
      width: '600px',
      customClass: {
        popup: 'swal-wide',
      },
    })

  } catch (error) {
    Swal.fire('Lỗi', 'Gửi mã QR lên server thất bại: ' + error.message, 'error')
  }
}

</script>

<style scoped>
.btn-sm {
  height: 42px;
  padding: 0 10px;
  font-size: 0.875rem;
}

.qr-scanner-container {
  border: 1px solid #ccc;
  padding: 10px;
  width: 320px;
}
</style>

<template>
  <div class="pos-container">
    <!-- ====== TABS HÓA ĐƠN + NÚT TẠO NHANH ====== -->
    <div class="invoice-tabs">
      <el-tabs v-model="activeInvoiceId" type="card" class="tabs-flat">
        <el-tab-pane v-for="(t, idx) in openInvoices" :key="t.id" :name="String(t.id)">
          <template #label>
            <span class="tab-label">
              Đơn {{ idx + 1 }}<span v-if="t.code" class="tab-code"> · {{ t.code }}</span>
            </span>
            <el-tooltip content="Đóng (vẫn giữ lại trên hệ thống)" placement="top">
              <el-icon class="tab-close" @click.stop="closeInvoiceTab(t)"><CircleClose /></el-icon>
            </el-tooltip>
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-tooltip content="Tạo hóa đơn mới" placement="top">
        <el-button type="primary" :icon="Plus" circle @click="createInvoiceTab" />
      </el-tooltip>
    </div>

    <!-- ====== THANH HÀNH ĐỘNG NHANH ====== -->
    <div class="top-actions">
      <el-button @click="$router.back()" :icon="ArrowLeft">Quay lại</el-button>
      <div class="grow"></div>
      <el-button
        type="danger"
        plain
        :icon="CircleClose"
        @click="cancelInvoice"
        :disabled="!invoiceId"
      >
        Hủy hóa đơn
      </el-button>
      <el-button
        type="primary"
        :disabled="!invoiceDetails?.details?.length"
        :loading="isLoading"
        @click="checkoutInvoice"
      >
        <el-icon class="mr-6"><Select /></el-icon>Thanh toán
      </el-button>
    </div>

    <!-- ====== LAYOUT 2 CỘT ====== -->
    <el-row :gutter="16">
      <!-- ==== CỘT TRÁI: SẢN PHẨM + GIỎ ==== -->
      <el-col :xs="24" :lg="14">
        <!-- TÌM SẢN PHẨM -->
        <el-card shadow="never" class="card">
          <template #header>
            <div class="card-title">Tìm kiếm sản phẩm</div>
          </template>

          <el-input
            v-model="searchTerm"
            placeholder="Nhập tên hoặc mã sản phẩm…"
            clearable
            size="large"
            :prefix-icon="Search"
            aria-label="Tìm kiếm sản phẩm"
          />

          <!-- Nút mở camera để quét QR (nhanh) -->
          <div class="mt-12 flex items-center gap-8">
            <el-button type="success" @click="openQrDialog"> Quét QR bằng camera </el-button>
          </div>

          <el-table
            :data="products"
            stripe
            v-loading="productLoading"
            class="mt-12"
            :header-cell-style="{ background: '#fafafa' }"
          >
            <el-table-column prop="productCode" label="Mã SP" width="110" />
            <el-table-column prop="productName" label="Tên SP" min-width="180" />
            <el-table-column label="Giá" width="140">
              <template #default="{ row }">
                <div class="price-cell">
                  <template v-if="row.discountedPrice && row.discountedPrice !== row.sellPrice">
                    <span class="price-old">{{ formatCurrency(row.sellPrice) }}</span>
                    <span class="price-new">{{ formatCurrency(row.discountedPrice) }}</span>
                  </template>
                  <template v-else>
                    <span class="price-normal">{{ formatCurrency(row.sellPrice) }}</span>
                  </template>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="Kho" width="80" align="center" />
            <el-table-column label="Thêm" width="90" align="center">
              <template #default="{ row }">
                <el-button type="primary" :icon="Plus" circle @click="openProductDialog(row)" />
              </template>
            </el-table-column>

            <template #empty>
              <el-empty description="Không tìm thấy sản phẩm." />
            </template>
          </el-table>

          <el-pagination
            v-if="pagination.totalPages > 1"
            class="mt-12 justify-center"
            background
            layout="prev, pager, next"
            :total="pagination.totalElements"
            :page-size="pagination.pageSize"
            :current-page="pagination.currentPage"
            @current-change="changePage"
          />
        </el-card>

        <!-- GIỎ HÀNG -->
        <el-card shadow="never" class="card mt-16">
          <template #header>
            <div class="card-title">
              {{ invoiceDetails ? `Giỏ hàng — ${invoiceDetails.invoice.invoiceCode}` : 'Giỏ hàng' }}
            </div>
          </template>

          <div v-if="invoiceDetails">
            <el-table
              :data="invoiceDetails.details"
              stripe
              :summary-method="tableSummary"
              show-summary
              :header-cell-style="{ background: '#fafafa' }"
            >
              <el-table-column prop="productName" label="Sản phẩm" min-width="180" />
              <el-table-column prop="size.sizeName" label="Size" width="80" />
              <el-table-column prop="color.colorName" label="Màu" width="90" />

              <el-table-column label="SL" width="120" align="center">
                <template #default="{ row }">
                  <div class="qty-inline">
                    <el-button
                      size="small"
                      :icon="Minus"
                      circle
                      @click="decreaseQuantity(row)"
                      :disabled="row.quantity <= 1"
                    />
                    <span class="qty-number">{{ row.quantity }}</span>
                    <el-button size="small" :icon="Plus" circle @click="increaseQuantity(row)" />
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="Đơn giá" width="140" align="right">
                <template #default="{ row }">
                  <div class="price-cell">
                    <template v-if="row.discountedPrice && row.discountedPrice !== row.sellPrice">
                      <span class="price-old">{{ formatCurrency(row.sellPrice) }}</span>
                      <span class="price-new">{{ formatCurrency(row.discountedPrice) }}</span>
                    </template>
                    <template v-else>
                      <span class="price-normal">{{ formatCurrency(row.sellPrice) }}</span>
                    </template>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="Thành tiền" width="160" align="right">
                <template #default="{ row }">
                  {{
                    formatCurrency(
                      row.discountedPrice && row.discountedPrice !== row.sellPrice
                        ? row.discountedPrice * row.quantity
                        : row.sellPrice * row.quantity,
                    )
                  }}
                </template>
              </el-table-column>

              <el-table-column label="" width="64" align="center">
                <template #default="{ row }">
                  <el-tooltip content="Xóa khỏi giỏ" placement="top">
                    <el-button
                      type="danger"
                      :icon="Delete"
                      circle
                      @click="deleteCartItem(row.id)"
                    />
                  </el-tooltip>
                </template>
              </el-table-column>

              <template #empty>
                <el-empty description="Giỏ hàng trống." />
              </template>
            </el-table>
          </div>
          <el-empty v-else description="Chưa có hóa đơn được chọn." />
        </el-card>
      </el-col>

      <!-- ==== CỘT PHẢI: KHÁCH HÀNG + THANH TOÁN + VOUCHER ==== -->
      <el-col :xs="24" :lg="10">
        <div class="stack">
          <!-- KHÁCH HÀNG -->
          <el-card shadow="never" class="card">
            <template #header><div class="card-title">Thông tin khách hàng</div></template>
            <CustomerSearch @select-customer="selectCustomer" />
            <el-button
              type="success"
              class="w-100 mt-12"
              :icon="User"
              @click="openCreateCustomerDialog"
            >
              Tạo khách hàng mới
            </el-button>
          </el-card>

          <!-- THANH TOÁN -->
          <el-card shadow="never" class="card">
            <template #header><div class="card-title">Thanh toán</div></template>

            <div v-if="invoiceDetails">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="Khách hàng">
                  {{ invoiceDetails.invoice.customerName || 'Khách lẻ' }}
                </el-descriptions-item>
                <el-descriptions-item label="Số SP">{{ getTotalQuantity }}</el-descriptions-item>
                <el-descriptions-item label="Tổng tiền">
                  <el-tag type="info" size="large">
                    {{ formatCurrency(invoiceDetails.invoice.totalAmount) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Giảm giá">
                  <el-tag type="warning" size="large">
                    - {{ formatCurrency(invoiceDetails.invoice.discountAmount || 0) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Khách phải trả">
                  <el-tag type="danger" size="large">
                    {{ formatCurrency(invoiceDetails.invoice.finalAmount || 0) }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <el-divider />

              <el-form-item label="Tiền khách đưa">
                <el-input
                  v-model="customerPaidInput"
                  @input="onCustomerPaidInput"
                  placeholder="Nhập số tiền"
                  size="large"
                  :formatter="(value) => `₫ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="(value) => value.replace(/₫\\s?|(,*)/g, '')"
                />
              </el-form-item>

              <div class="change-line">
                <span>Tiền thừa:</span>
                <b class="text-success">{{ formatCurrency(changeAmount) }}</b>
              </div>
              <p v-if="errorMessage" class="text-danger small mt-6">{{ errorMessage }}</p>
            </div>

            <el-empty v-else description="Chưa có thông tin hóa đơn." />
          </el-card>

          <!-- VOUCHER -->
          <el-card shadow="never" class="card">
            <template #header><div class="card-title">Voucher khuyến mãi</div></template>

            <div v-loading="voucherLoading">
              <div v-if="voucherError" class="alert alert-danger p-8 small">{{ voucherError }}</div>

              <template v-else>
                <el-empty
                  v-if="vouchers.length === 0"
                  description="Không có voucher phù hợp."
                  :image-size="60"
                />
                <el-scrollbar v-else max-height="180px">
                  <div v-for="vc in vouchers" :key="vc.id" class="voucher-row">
                    <div class="voucher-info">
                      <div>
                        Mã: <b>{{ vc.voucherCode }}</b>
                      </div>
                      <div class="text-muted small">
                        Giảm:
                        <template v-if="vc.discountPercentage">
                          {{ vc.discountPercentage }}%
                          <template v-if="vc.maxDiscountValue">
                            (tối đa {{ formatCurrency(vc.maxDiscountValue) }})
                          </template>
                        </template>
                        <template v-else>
                          {{ formatCurrency(vc.discountAmount) }}
                        </template>
                      </div>
                    </div>

                    <div>
                      <el-button
                        v-if="appliedVoucher?.voucherCode === vc.voucherCode"
                        type="danger"
                        round
                        size="small"
                        :loading="removeLoading"
                        @click="removeVoucher"
                        >Bỏ chọn</el-button
                      >

                      <el-button
                        v-else
                        type="primary"
                        round
                        size="small"
                        :loading="applyLoading && applyingVoucherCode === vc.voucherCode"
                        :disabled="applyLoading"
                        @click="applyVoucher(vc.voucherCode)"
                        >Áp dụng</el-button
                      >
                    </div>
                  </div>
                </el-scrollbar>
              </template>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <!-- ====== CHỌN THUỘC TÍNH SP ====== -->
    <el-dialog
      v-model="productDialogVisible"
      :title="currentProduct ? `Chọn thuộc tính: ${currentProduct.productName}` : 'Chọn thuộc tính'"
      width="520px"
      destroy-on-close
      @close="closeProductDialog"
    >
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Size">
              <el-select
                v-model="selectedSizeId"
                placeholder="Chọn size"
                class="w-100"
                :disabled="sizes.length === 0"
              >
                <el-option v-for="s in sizes" :key="s.id" :label="s.sizeName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Màu sắc">
              <el-select
                v-model="selectedColorId"
                placeholder="Chọn màu"
                class="w-100"
                :disabled="colors.length === 0"
              >
                <el-option v-for="c in colors" :key="c.id" :label="c.colorName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Số lượng">
          <el-input-number
            v-model="selectedQuantity"
            :min="1"
            :max="Math.max(1, maxQuantity)"
            :disabled="maxQuantity === 0"
          />
          <small v-if="maxQuantity > 0" class="muted ms-10">Tối đa: {{ maxQuantity }}</small>
          <small v-else class="text-danger ms-10">Hết hàng biến thể này</small>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeProductDialog">Hủy</el-button>
        <el-button
          type="primary"
          :disabled="
            !selectedSizeId ||
            !selectedColorId ||
            selectedQuantity < 1 ||
            selectedQuantity > maxQuantity ||
            maxQuantity === 0
          "
          @click="confirmAddProduct"
        >
          <el-icon class="mr-6"><Check /></el-icon>Xác nhận
        </el-button>
      </template>
    </el-dialog>

    <!-- ====== DIALOG QUÉT QR BẰNG CAMERA (NHANH) ====== -->
    <el-dialog
      v-model="qrDialogVisible"
      title="Quét QR sản phẩm"
      width="560px"
      @closed="stopQrScan"
    >
      <div class="flex flex-col gap-3">
        <div class="w-full aspect-video border rounded overflow-hidden relative">
          <video
            ref="qrVideoRef"
            class="w-full h-full object-cover"
            autoplay
            muted
            playsinline
          ></video>

          <!-- khung ngắm -->
          <div class="pointer-events-none absolute inset-0 grid place-items-center">
            <div class="qr-frame"></div>
          </div>
        </div>

        <div class="flex gap-8 items-center">
          <el-select
            v-model="selectedDeviceId"
            placeholder="Chọn camera"
            style="flex: 1"
            @change="restartQrScan"
          >
            <el-option
              v-for="d in qrDevices"
              :key="d.deviceId"
              :label="d.label || 'Camera'"
              :value="d.deviceId"
            />
          </el-select>

          <el-button
            @click="toggleTorch"
            :disabled="!canTorch"
            :type="torchOn ? 'warning' : 'default'"
          >
            {{ torchOn ? 'Tắt đèn' : 'Bật đèn' }}
          </el-button>

          <el-button @click="restartQrScan" :loading="qrScanning">Bắt đầu</el-button>
          <el-button @click="stopQrScan" :disabled="!qrScanning">Dừng</el-button>
        </div>

        <el-alert v-if="qrMessage" :title="qrMessage" type="info" show-icon />
        <el-alert
          v-if="useNativeDetector"
          title="Đang dùng BarcodeDetector (nhanh)"
          type="success"
          show-icon
        />
        <el-alert v-else title="Đang dùng ZXing fallback" type="warning" show-icon />
      </div>
    </el-dialog>

    <!-- MODAL TẠO KHÁCH HÀNG -->
    <CounterSalesCreateCustomer
      ref="createCustomerDialog"
      @created="handleCustomerCreated"
      @select-customer="selectCreatedCustomer"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiClient from '../../utils/axiosInstance.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Plus,
  Minus,
  Delete,
  Check,
  Search,
  User,
  CircleClose,
  Select,
} from '@element-plus/icons-vue'
import CustomerSearch from './CustomerSearch.vue'
import CounterSalesCreateCustomer from './CounterSalesCreateCustomer.vue'
import { BrowserMultiFormatReader } from '@zxing/browser' // npm i @zxing/browser @zxing/library
import { BarcodeFormat, DecodeHintType } from '@zxing/library' // import enums từ library

/* ----------------- Tabs Hóa đơn ----------------- */
const route = useRoute()
const router = useRouter()
const employeeId = 1

const openInvoices = ref([]) // [{id, code}]
const activeInvoiceId = ref(route.params.id ? String(route.params.id) : '')
const invoiceId = computed(() => Number(activeInvoiceId.value || 0))

// tạo mới tab + hóa đơn
const createInvoiceTab = async () => {
  try {
    const { data } = await apiClient.post(
      `/admin/counter-sales/create-empty?employeeId=${employeeId}`,
      {},
    )
    if (!openInvoices.value.some((t) => t.id === data.id)) {
      openInvoices.value.push({ id: data.id, code: data.invoiceCode || '' })
    }
    activeInvoiceId.value = String(data.id)
    router.replace({ name: 'CounterSalesDisplay', params: { id: activeInvoiceId.value } })
    await loadActiveInvoice()
    ElMessage.success(`Đã tạo ${data.invoiceCode}`)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Tạo hóa đơn thất bại!')
  }
}

// đóng tab (không hủy server, hỏi tùy chọn)
const closeInvoiceTab = async (tab) => {
  const justClose = await ElMessageBox.confirm(
    'Đóng tab này? (Hóa đơn trên hệ thống sẽ giữ nguyên)',
    'Đóng tab',
    { type: 'warning', confirmButtonText: 'Đóng tab', cancelButtonText: 'Hủy' },
  )
    .then(() => true)
    .catch(() => false)
  if (!justClose) return

  const closingActive = String(tab.id) === activeInvoiceId.value

  // Xóa tab
  openInvoices.value = openInvoices.value.filter((t) => t.id !== tab.id)

  // Nếu không còn tab nào → về danh sách
  if (openInvoices.value.length === 0) {
    activeInvoiceId.value = ''
    invoiceDetails.value = null
    vouchers.value = []
    router.push('/sales-counter/list')
    return
  }

  // Nếu đóng tab đang active → kích hoạt tab cuối cùng còn lại
  if (closingActive) {
    const last = openInvoices.value.at(-1)
    activeInvoiceId.value = String(last.id)
    router.replace({ name: 'CounterSalesDisplay', params: { id: activeInvoiceId.value } })
    await loadActiveInvoice()
  }
}

// chuyển tab mượt
watch(activeInvoiceId, async (newId, oldId) => {
  if (!newId || newId === oldId) return
  if (route.params.id !== newId) {
    router.replace({ name: 'CounterSalesDisplay', params: { id: newId } })
  }
  await loadActiveInvoice()
})

// sync theo route
watch(
  () => route.params.id,
  async (nid) => {
    if (!nid) return
    const num = Number(nid)
    if (!openInvoices.value.some((t) => t.id === num)) {
      openInvoices.value.push({ id: num, code: '' })
    }
    activeInvoiceId.value = String(num)
    await loadActiveInvoice()
  },
)

/* ----------------- State chính ----------------- */
const invoiceDetails = ref(null)

const searchTerm = ref('')
const products = ref([])
const productLoading = ref(false)

const productDialogVisible = ref(false)
const currentProduct = ref(null)
const attributes = ref([])
const sizes = ref([])
const colors = ref([])
const selectedSizeId = ref('')
const selectedColorId = ref('')
const selectedQuantity = ref(1)

const changeAmount = ref(0)
const customerPaidInput = ref('')
const customerPaid = ref(0)
const errorMessage = ref('')

const appliedVoucher = ref(null)
const voucherLoading = ref(false)
const voucherError = ref('')
const vouchers = ref([])
const applyLoading = ref(false)
const applyingVoucherCode = ref(null)
const removeLoading = ref(false)

const isLoading = ref(false)


const addingProduct = ref(false)
const selectingCustomer = ref(false)

/* ----------------- Helpers ----------------- */
const formatCurrency = (v) =>
  v == null ? '' : Number(v).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' })

const debounce = (fn, delay) => {
  let t
  return (...args) => {
    clearTimeout(t)
    t = setTimeout(() => fn(...args), delay)
  }
}

/* ----------------- API ----------------- */
const fetchInvoiceDetails = async (id) => {
  if (!id) return
  try {
    const { data } = await apiClient.get(`/admin/counter-sales/${id}/details`)
    invoiceDetails.value = data
    appliedVoucher.value = data.invoice?.voucher || null
    const t = openInvoices.value.find((x) => x.id === id)
    if (t) t.code = data.invoice?.invoiceCode || t.code
  } catch {
    ElMessage.error('Lỗi khi tải chi tiết hóa đơn.')
    invoiceDetails.value = null
  }
}

const fetchVoucherByInvoiceId = async (id) => {
  if (!id) return
  voucherLoading.value = true
  voucherError.value = ''
  vouchers.value = []
  try {
    const res = await apiClient.get(`/admin/vouchers/by-invoice/${id}`)
    vouchers.value = res.data || []
  } catch {
    voucherError.value = 'Chưa chọn khách hàng hoặc không có voucher phù hợp.'
  } finally {
    voucherLoading.value = false
  }
}

const loadActiveInvoice = async () => {
  if (!invoiceId.value) return
  await Promise.all([
    fetchInvoiceDetails(invoiceId.value),
    fetchVoucherByInvoiceId(invoiceId.value),
  ])
}

/* ----------------- Sản phẩm ----------------- */
const pagination = ref({ currentPage: 1, pageSize: 5, totalPages: 1, totalElements: 0 })
const fetchProducts = async (page = 1) => {
  productLoading.value = true
  try {
    const { data } = await apiClient.post('/admin/products/search', {
      keyword: searchTerm.value.trim(),
      page: page - 1,
      size: pagination.value.pageSize,
    })
    products.value = data.data || []
    pagination.value = {
      ...data.pagination,
      currentPage: data.pagination.currentPage,
      totalElements: data.pagination.totalElements,
    }
  } catch {
    ElMessage.error('Lỗi khi tải sản phẩm.')
  } finally {
    productLoading.value = false
  }
}
const changePage = (p) => {
  pagination.value.currentPage = p
  fetchProducts(p)
}

watch(
  searchTerm,
  debounce(() => fetchProducts(1), 250),
)

/* ====== QUÉT QR BẰNG CAMERA (NHANH) ====== */
const qrDialogVisible = ref(false)
const qrVideoRef = ref(null)
const qrDevices = ref([])
const selectedDeviceId = ref('')
const qrScanning = ref(false)
const qrMessage = ref('')

// Torch & stream
const torchOn = ref(false)
const canTorch = ref(false)
let mediaStream = null
let rafId = null

// engine flags
const useNativeDetector = 'BarcodeDetector' in window
let barcodeDetector = null
let zxingReader = null

const openQrDialog = async () => {
  qrDialogVisible.value = true
  await initQrDevices()
  await startQrScan()
}

async function initQrDevices() {
  try {
    const devices = await navigator.mediaDevices.enumerateDevices()
    const videos = devices.filter((d) => d.kind === 'videoinput')
    qrDevices.value = videos
    if (!selectedDeviceId.value) {
      const back = videos.find((d) => /back|trailing|environment/i.test(d.label))
      selectedDeviceId.value = (back || videos[0])?.deviceId || ''
    }
  } catch {
    qrMessage.value = 'Không truy cập được danh sách camera.'
  }
}

function buildConstraints() {
  const base = {
    width: { ideal: 1280 },
    height: { ideal: 720 },
    frameRate: { ideal: 30, max: 60 },
  }
  if (selectedDeviceId.value) {
    return {
      video: {
        deviceId: { exact: selectedDeviceId.value },
        ...base,
        advanced: [{ focusMode: 'continuous' }],
      },
      audio: false,
    }
  }
  return {
    video: {
      facingMode: { ideal: 'environment' },
      ...base,
      advanced: [{ focusMode: 'continuous' }],
    },
    audio: false,
  }
}

async function startQrScan() {
  stopQrScan()
  qrMessage.value = 'Đang mở camera...'
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia(buildConstraints())
    const video = qrVideoRef.value
    video.srcObject = mediaStream
    await video.play()

    // Torch capability
    canTorch.value = false
    torchOn.value = false
    const track = mediaStream.getVideoTracks()[0]
    if (track && track.getCapabilities) {
      const caps = track.getCapabilities()
      canTorch.value = !!caps.torch
    }

    if (useNativeDetector) {
      // NATIVE BarcodeDetector (rất nhanh)
      try {
        // một số trình duyệt không expose list, vẫn detect được
        await window.BarcodeDetector.getSupportedFormats?.()
      } catch {}
      barcodeDetector = new window.BarcodeDetector({ formats: ['qr_code'] })
      qrScanning.value = true
      qrMessage.value = 'Đang quét (native)...'
      nativeLoop()
    } else {
      // ZXING fallback: chỉ decode QR_CODE, giảm interval
      const hints = new Map()
      hints.set(DecodeHintType.POSSIBLE_FORMATS, [BarcodeFormat.QR_CODE])
      zxingReader = new BrowserMultiFormatReader(hints, /*timeBetweenScansMillis*/ 80)
      qrScanning.value = true
      qrMessage.value = 'Đang quét (ZXing)...'
      await zxingReader.decodeFromConstraints(buildConstraints(), video, async (result, err) => {
        if (result) {
          stopQrScan()
          const code = result.getText().trim()
          await onQrDecoded(code)
        }
      })
    }
  } catch (e) {
    qrMessage.value = 'Không thể mở camera. Kiểm tra quyền hoặc kết nối thiết bị.'
    qrScanning.value = false
  }
}

async function nativeLoop() {
  if (!qrScanning.value || !barcodeDetector) return
  try {
    const codes = await barcodeDetector.detect(qrVideoRef.value)
    if (codes && codes.length) {
      stopQrScan()
      const code = (codes[0]?.rawValue || '').trim()
      if (code) {
        await onQrDecoded(code)
        return
      }
    }
  } catch {
    // bỏ qua, tiếp tục vòng lặp
  }
  rafId = requestAnimationFrame(nativeLoop)
}

async function toggleTorch() {
  if (!mediaStream) return
  const track = mediaStream.getVideoTracks()[0]
  if (!track?.applyConstraints) return
  try {
    torchOn.value = !torchOn.value
    await track.applyConstraints({ advanced: [{ torch: torchOn.value }] })
  } catch {
    torchOn.value = false
  }
}

async function restartQrScan() {
  await startQrScan()
}

function stopQrScan() {
  qrScanning.value = false
  if (rafId) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
  try {
    zxingReader?.reset()
  } catch {}
  zxingReader = null
  barcodeDetector = null
  if (mediaStream) {
    mediaStream.getTracks().forEach((t) => t.stop())
    mediaStream = null
  }
}

onBeforeUnmount(() => stopQrScan())

// Khi đọc xong → gọi BE rồi mở dialog chọn size/màu
async function onQrDecoded(code) {
  try {
    const { data: resp } = await apiClient.get('/admin/products/scan', { params: { code } })
    qrDialogVisible.value = false
    await openProductDialogFromScan(resp)
  } catch (e) {
    qrMessage.value = e?.response?.data?.message || 'Không tìm thấy sản phẩm từ QR.'
    // người dùng có thể bấm "Bắt đầu" để quét lại
  }
}

/* ====== Sau khi quét → mở dialog thuộc tính ====== */
async function openProductDialogFromScan(resp) {
  const row = { id: resp.id, productName: resp.productName }
  currentProduct.value = row
  try {
    const { data: attrs } = await apiClient.get(`/admin/counter-sales/${resp.id}/attributes`)
    attributes.value = attrs
    sizes.value = [...new Map(attrs.filter((a) => a.size).map((a) => [a.size.id, a.size])).values()]
    colors.value = [
      ...new Map(attrs.filter((a) => a.color).map((a) => [a.color.id, a.color])).values(),
    ]

    // Auto chọn nếu chỉ có 1 lựa chọn
    selectedSizeId.value = sizes.value.length === 1 ? sizes.value[0].id : ''
    selectedColorId.value = colors.value.length === 1 ? colors.value[0].id : ''

    selectedQuantity.value = 1
    productDialogVisible.value = true
  } catch {
    ElMessage.error('Không thể lấy thuộc tính sản phẩm.')
  }
}

/* ----------------- Modal chọn biến thể (dùng chung) ----------------- */
const maxQuantity = computed(() => {
  if (!currentProduct.value || !selectedSizeId.value || !selectedColorId.value) return 0
  const attr = attributes.value.find(
    (a) => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value,
  )
  return Number(attr?.quantity || 0)
})

const openProductDialog = async (product) => {
  currentProduct.value = product
  try {
    const { data: attrs } = await apiClient.get(`/admin/counter-sales/${product.id}/attributes`)
    attributes.value = attrs
    sizes.value = [...new Map(attrs.filter((a) => a.size).map((a) => [a.size.id, a.size])).values()]
    colors.value = [
      ...new Map(attrs.filter((a) => a.color).map((a) => [a.color.id, a.color])).values(),
    ]
    selectedSizeId.value = ''
    selectedColorId.value = ''
    selectedQuantity.value = 1
    productDialogVisible.value = true
  } catch {
    ElMessage.error('Không thể lấy thuộc tính sản phẩm.')
  }
}
const closeProductDialog = () => {
  productDialogVisible.value = false
  currentProduct.value = null
  attributes.value = []
  sizes.value = []
  colors.value = []
}

const confirmAddProduct = async () => {
  const id = invoiceDetails.value?.invoice?.id
  if (!id) return ElMessage.warning('Hóa đơn không hợp lệ.')

  const matched = attributes.value.find(
    (a) => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value
  )
  if (!matched) return ElMessage.warning('Vui lòng chọn đủ size & màu.')

  if (addingProduct.value) return
  addingProduct.value = true

  try {
    await apiClient.post(`/admin/counter-sales/${id}/details`, {
      productDetailId: matched.id,
      quantity: selectedQuantity.value,
      discountCampaignId: matched.discountCampaignId ?? null,
    })

    ElMessage.success(`Đã thêm "${currentProduct.value.productName}" vào giỏ.`)
    closeProductDialog()

    // Áp voucher tự động sau khi thêm sp
    const { applied, message } = await autoApplyBestVoucher(id)
    if (message) {
      applied ? ElMessage.success(message) : ElMessage.info(message)
    }

    // Refresh UI đồng thời
    await Promise.all([fetchInvoiceDetails(id), fetchVoucherByInvoiceId(id)])
  } catch (e) {
    ElMessage.error(e?.response?.data || 'Thêm sản phẩm thất bại.')
  } finally {
    addingProduct.value = false
  }
}

/* ----------------- Sửa số lượng giỏ ----------------- */
const updateInvoiceDetailQuantity = async (detailId, newQty) => {
  const { data } = await apiClient.put(
    `/admin/counter-sales/invoice-details/${detailId}/quantity`,
    null,
    { params: { quantity: newQty } },
  )
  return data
}
const increaseQuantity = async (row) => {
  try {
    invoiceDetails.value = await updateInvoiceDetailQuantity(row.id, row.quantity + 1)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Không thể tăng số lượng.')
  }
}
const decreaseQuantity = async (row) => {
  if (row.quantity <= 1) return
  try {
    invoiceDetails.value = await updateInvoiceDetailQuantity(row.id, row.quantity - 1)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Không thể giảm số lượng.')
  }
}
const deleteCartItem = async (detailId) => {
  try {
    await ElMessageBox.confirm('Xóa sản phẩm này khỏi giỏ?', 'Xác nhận', { type: 'warning' })
    await apiClient.delete(`/admin/counter-sales/cart-item/${detailId}`)
    await fetchInvoiceDetails(invoiceId.value)
    ElMessage.success('Đã xóa sản phẩm.')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data || 'Xóa thất bại.')
  }
}

/* ----------------- Khách hàng ----------------- */
const selectCustomer = async (customer) => {
  if (!invoiceId.value) return ElMessage.error('Chưa chọn hóa đơn.')
  if (!customer?.id)   return ElMessage.warning('Khách hàng không hợp lệ.')

  if (selectingCustomer.value) return
  selectingCustomer.value = true

  try {
    // 1) Gán khách cho hóa đơn
    await apiClient.put(`/admin/counter-sales/${invoiceId.value}/assign-customer`, {
      customerId: customer.id,
    })

    // 2) Tự động áp voucher tốt nhất (không làm vỡ luồng nếu lỗi)
    const { applied, message } = await autoApplyBestVoucher(invoiceId.value)
    if (message) {
      applied ? ElMessage.success(message) : ElMessage.info(message)
    }

    // 3) Refresh dữ liệu hiển thị
    await Promise.all([
      fetchInvoiceDetails(invoiceId.value),
      fetchVoucherByInvoiceId(invoiceId.value),
    ])

    ElMessage.success(`Đã chọn khách: ${customer.customerName || customer.phone}`)
  } catch (e) {
    const msg = e?.response?.data || e?.response?.data?.message || 'Không thể cập nhật khách hàng.'
    ElMessage.error(msg)
  } finally {
    selectingCustomer.value = false
  }
}
const createCustomerDialog = ref(null)
const openCreateCustomerDialog = () => createCustomerDialog.value?.openDialog()
const handleCustomerCreated = (c) => (createdCustomer.value = c)
const createdCustomer = ref(null)
const selectCreatedCustomer = async () => {
  if (!createdCustomer.value) return
  await selectCustomer(createdCustomer.value)
  createdCustomer.value = null
}

/* ---- Voucher apply/remove (để tránh lỗi nút bấm) ---- */
const applyVoucher = async (code) => {
  if (!invoiceId.value) return

  try {
    applyLoading.value = true
    applyingVoucherCode.value = code

    const { data } = await apiClient.post(
      `/admin/counter-sales/${invoiceId.value}/apply-voucher?voucherCode=${encodeURIComponent(code)}`
    )

    // BE trả object có voucher + finalAmount
    appliedVoucher.value = data?.voucher || null

    await fetchInvoiceDetails(invoiceId.value)
    ElMessage.success('Đã áp dụng voucher.')
  } catch (e) {
    // Lấy message trực tiếp từ backend
    const errMsg = e?.response?.data || 'Không áp dụng được voucher.'
    ElMessage.warning(errMsg) // warning thay vì error nếu muốn hiển thị nhẹ nhàng hơn
  } finally {
    applyLoading.value = false
    applyingVoucherCode.value = null
  }
}

const removeVoucher = async () => {
  if (!invoiceId.value) return
  try {
    removeLoading.value = true
    await apiClient.put(`/admin/counter-sales/${invoiceId.value}/remove-voucher`)
    appliedVoucher.value = null
    await fetchInvoiceDetails(invoiceId.value)
    ElMessage.success('Đã bỏ voucher.')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Không bỏ được voucher.')
  } finally {
    removeLoading.value = false
  }
}

const autoApplyBestVoucher = async (invoiceId) => {
  try {
    const { data } = await apiClient.post(`/admin/counter-sales/${invoiceId}/apply-best-voucher`)

    // Trường hợp BE trả về string
    if (typeof data === 'string') {
      const msg = data.trim()
      const noVoucher = /không có voucher/i.test(msg)
      if (noVoucher) {
        appliedVoucher.value = null
        return { applied: false, message: msg }
      }
      // Fallback: có thể BE trả message thành công nhưng không có DTO
      await fetchVoucherByInvoiceId(invoiceId)
      return { applied: true, message: msg || 'Đã áp dụng voucher tốt nhất.' }
    }

    // Trường hợp BE trả object:
    // - kiểu { voucher: {...}, finalAmount, discountAmount }
    // - hoặc trả thẳng DTO voucher {...}
    if (data?.voucher || data?.id) {
      appliedVoucher.value = data.voucher || data
      return { applied: true, message: 'Đã áp dụng voucher tốt nhất.' }
    }

    // Shape lạ -> refetch để đồng bộ UI
    await fetchVoucherByInvoiceId(invoiceId)
    return { applied: true, message: 'Đã áp dụng voucher tốt nhất.' }
  } catch {
    // nuốt lỗi để không ảnh hưởng UX
    return { applied: false, message: null }
  }
}

/* ----------------- Thanh toán ----------------- */
function onCustomerPaidInput() {
  const numeric = customerPaidInput.value.replace(/[^\d]/g, '')
  customerPaid.value = Number(numeric)
  customerPaidInput.value = numeric.replace(/\B(?=(\d{3})+(?!\d))/g, '.')
  calculateChange()
}
function calculateChange() {
  const finalAmount = invoiceDetails.value?.invoice?.finalAmount || 0
  changeAmount.value =
    customerPaid.value > 0 && finalAmount > 0 ? customerPaid.value - finalAmount : 0
  errorMessage.value =
    customerPaid.value > 0 && customerPaid.value < finalAmount ? 'Tiền khách đưa chưa đủ.' : ''
}
const checkoutInvoice = async () => {
  if (!invoiceId.value) return ElMessage.error('Hóa đơn không hợp lệ!')
  if (!invoiceDetails.value?.details?.length) return ElMessage.error('Giỏ hàng trống!')

  isLoading.value = true
  try {
    const res = await apiClient.post(`/admin/counter-sales/${invoiceId.value}/checkout`)
    const print = await ElMessageBox.confirm(
      res.data?.message || 'Thanh toán thành công! In hóa đơn PDF?',
      'Thành công',
      { type: 'success', confirmButtonText: 'In hóa đơn', cancelButtonText: 'Đóng' },
    )
      .then(() => true)
      .catch(() => false)

    if (print) {
      const pdf = await apiClient.get(`/admin/invoices/${invoiceId.value}/export-id`, {
        responseType: 'blob',
      })
      const blob = new Blob([pdf.data], { type: 'application/pdf' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `HoaDon-${invoiceId.value}.pdf`
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)
    }

    openInvoices.value = openInvoices.value.filter((t) => t.id !== invoiceId.value)
    const last = openInvoices.value.at(-1)
    activeInvoiceId.value = last ? String(last.id) : ''
    if (activeInvoiceId.value) await loadActiveInvoice()
    else router.push('/sales-counter/list')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Có lỗi khi thanh toán.')
  } finally {
    isLoading.value = false
  }
}
const cancelInvoice = async () => {
  if (!invoiceId.value) return
  try {
    await ElMessageBox.confirm('Hủy hóa đơn này? Hành động không thể hoàn tác.', 'Xác nhận', {
      type: 'warning',
    })
    await apiClient.post(`/admin/counter-sales/${invoiceId.value}/cancel`)
    ElMessage.success('Đã hủy hóa đơn.')

    openInvoices.value = openInvoices.value.filter((t) => t.id !== invoiceId.value)
    const last = openInvoices.value.at(-1)
    activeInvoiceId.value = last ? String(last.id) : ''
    if (activeInvoiceId.value) await loadActiveInvoice()
    else router.push('/sales-counter/list')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data || 'Không thể hủy hóa đơn.')
  }
}

/* ----------------- Tổng số & Lifecycle ----------------- */
const getTotalQuantity = computed(() =>
  Array.isArray(invoiceDetails.value?.details)
    ? invoiceDetails.value.details.reduce((s, it) => s + Number(it.quantity || 0), 0)
    : 0,
)
const tableSummary = ({ data, columns }) => {
  const sums = []
  columns.forEach((col, idx) => {
    if (idx === 0) sums[idx] = 'Tổng cộng'
    else if (col.label === 'Thành tiền') {
      const total = data.reduce((acc, r) => {
        const price =
          r.discountedPrice && r.discountedPrice !== r.sellPrice ? r.discountedPrice : r.sellPrice
        return acc + Number(price || 0) * Number(r.quantity || 0)
      }, 0)
      sums[idx] = formatCurrency(total)
    } else {
      sums[idx] = ''
    }
  })
  return sums
}

onMounted(async () => {
  if (route.params.id) {
    const idNum = Number(route.params.id)
    if (!openInvoices.value.some((t) => t.id === idNum))
      openInvoices.value.push({ id: idNum, code: '' })
    activeInvoiceId.value = String(idNum)
    await loadActiveInvoice()
  }
  fetchProducts()
})
</script>

<style scoped>
/* Layout tổng thể */
.pos-container {
  padding: 12px 16px 24px;
  background: #f5f7fb;
}

/* Tabs hoá đơn */
.invoice-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(90deg, #1976d2, #1565c0);
  padding: 8px 12px;
  border-radius: 10px;
  margin-bottom: 12px;
}
.tabs-flat :deep(.el-tabs__header) {
  margin: 0;
}
.tabs-flat :deep(.el-tabs__nav) {
  border: 0;
}
.tabs-flat :deep(.el-tabs__item) {
  color: #fff;
}
.tabs-flat :deep(.el-tabs__item.is-active) {
  background: rgba(255, 255, 255, 0.18);
  border-radius: 6px;
  color: #fff;
}
.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.tab-code {
  opacity: 0.85;
  font-weight: 500;
}
.tab-close {
  margin-left: 8px;
  opacity: 0.85;
  cursor: pointer;
}
.tab-close:hover {
  opacity: 1;
}

/* Thanh hành động */
.top-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0 14px;
}
.grow {
  flex: 1;
}
.mr-6 {
  margin-right: 6px;
}

/* Card */
.card {
  border-radius: 12px;
  overflow: hidden;
}
.card-title {
  font-weight: 700;
  font-size: 16px;
}
.mt-12 {
  margin-top: 12px;
}
.mt-16 {
  margin-top: 16px;
}
.w-100 {
  width: 100%;
}

/* Table cells */
.price-cell {
  display: flex;
  gap: 4px;
  flex-direction: column;
  align-items: flex-end;
}
.price-old {
  text-decoration: line-through;
  color: #999;
  font-size: 12px;
}
.price-new {
  color: #f56c6c;
  font-weight: 700;
}
.price-normal {
  font-weight: 600;
}

.qty-inline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.qty-number {
  width: 28px;
  text-align: center;
  font-weight: 600;
}

/* Voucher list */
.voucher-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 6px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.voucher-info {
  line-height: 1.3;
}

/* QR frame */
.qr-frame {
  width: 12rem;
  height: 12rem;
  border: 2px dashed rgba(255, 255, 255, 0.85);
  border-radius: 10px;
}

/* Misc */
.justify-center {
  display: flex;
  justify-content: center;
}
.text-success {
  color: var(--el-color-success);
}
.text-danger {
  color: var(--el-color-danger);
}
.small {
  font-size: 12px;
}
.ms-10 {
  margin-left: 10px;
}
.mt-6 {
  margin-top: 6px;
}
.p-8 {
  padding: 8px;
}
.stack {
  display: grid;
  gap: 12px;
}
.change-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}
</style>

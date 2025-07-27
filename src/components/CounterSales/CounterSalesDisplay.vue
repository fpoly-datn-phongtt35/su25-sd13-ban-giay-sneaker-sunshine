<template>
  <div class="pos-full-screen-container">
    <el-button @click="$router.back()" :icon="ArrowLeft" class="mb-3"> Quay lại </el-button>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="mb-4">
          <template #header>
            <div class="card-header">
              <span>Tìm kiếm sản phẩm</span>
            </div>
          </template>
          <el-input
            v-model="searchTerm"
            placeholder="Nhập tên hoặc mã sản phẩm..."
            clearable
            size="large"
            :prefix-icon="Search"
            aria-label="Tìm kiếm sản phẩm"
          />

          <el-table :data="products" stripe v-loading="productLoading" class="mt-3">
            <el-table-column prop="productCode" label="Mã SP" width="100" />
            <el-table-column prop="productName" label="Tên SP" />
            <el-table-column label="Giá" width="120">
              <template #default="{ row }">
                <div>
                  <span v-if="row.discountedPrice && row.discountedPrice !== row.sellPrice">
                    <span style="text-decoration: line-through; color: #888; font-size: 12px">
                      {{ formatCurrency(row.sellPrice) }}
                    </span>
                    <br />
                    <span style="color: #f56c6c; font-weight: 600">
                      {{ formatCurrency(row.discountedPrice) }}
                    </span>
                  </span>
                  <span v-else>
                    {{ formatCurrency(row.sellPrice) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="Kho" width="80" />
            <el-table-column label="Hành động" width="90" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  :icon="Plus"
                  circle
                  @click="openProductDialog(row)"
                  title="Thêm vào giỏ"
                />
              </template>
            </el-table-column>

            <template #empty>
              <el-empty description="Không tìm thấy sản phẩm nào." />
            </template>
          </el-table>

          <el-pagination
            v-if="pagination.totalPages > 1"
            class="mt-4 justify-content-center"
            background
            layout="prev, pager, next"
            :total="pagination.totalElements"
            :page-size="pagination.pageSize"
            :current-page="pagination.currentPage"
            @current-change="changePage"
          />
        </el-card>

        <el-card shadow="never" class="mb-4">
  <template #header>
    <div class="card-header">
      <span v-if="invoiceDetails">Giỏ hàng: {{ invoiceDetails.invoice.invoiceCode }}</span>
      <span v-else>Giỏ hàng</span>
    </div>
  </template>

  <div v-if="invoiceDetails">
    <el-table :data="invoiceDetails.details" stripe class="mt-3">
      
      <el-table-column prop="productName" label="Tên SP" />
      <el-table-column prop="size.sizeName" label="Size" width="80" />
      <el-table-column prop="color.colorName" label="Màu" width="90" />

      <!-- Cột số lượng có nút + - -->
      <el-table-column label="SL" width="110">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; justify-content: center;">
            <el-button
              size="small"
              :icon="Minus"
              circle
              @click="decreaseQuantity(row)"
              :disabled="row.quantity <= 1"
            />
            <span style="margin: 0 8px;">{{ row.quantity }}</span>
            <el-button
              size="small"
              :icon="Plus"
              circle
              @click="increaseQuantity(row)"
            />
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Giá" width="120">
        <template #default="{ row }">
          <div>
            <template v-if="row.discountedPrice && row.discountedPrice !== row.sellPrice">
              <span style="text-decoration: line-through; color: #999; font-size: 12px;">
                {{ formatCurrency(row.sellPrice) }}
              </span>
              <br />
              <span style="color: #f56c6c; font-weight: 600;">
                {{ formatCurrency(row.discountedPrice) }}
              </span>
            </template>
            <template v-else>
              <span>{{ formatCurrency(row.sellPrice) }}</span>
            </template>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Hành động" width="90" align="center">
        <template #default="{ row }">
          <el-button
            type="danger"
            :icon="Delete"
            circle
            @click="deleteCartItem(row.id)"
            title="Xóa sản phẩm"
          />
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="Giỏ hàng trống." />
      </template>
    </el-table>
  </div>

  <el-empty v-else description="Đang tải giỏ hàng hoặc chưa có hóa đơn..." />
</el-card>


      </el-col>

      <el-col :xs="24" :lg="10">
        <div class="d-flex flex-column gap-3">
          <el-card shadow="never">
            <template #header>
              <span>Thông tin khách hàng</span>
            </template>
            <CustomerSearch @select-customer="selectCustomer" />
            <el-button
              type="success"
              class="w-100 mt-3"
              :icon="User"
              @click="openCreateCustomerDialog"
            >
              Tạo khách hàng mới
            </el-button>
          </el-card>

          <el-card shadow="never">
            <template #header>
              <span>Thông tin thanh toán</span>
            </template>
            <div v-if="invoiceDetails">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="Khách hàng">
                  {{ invoiceDetails.invoice.customerName || 'Khách lẻ' }}
                </el-descriptions-item>
                <el-descriptions-item label="Tổng sản phẩm">
                  {{ getTotalQuantity }}
                </el-descriptions-item>
                <el-descriptions-item label="Tổng tiền">
                  <el-tag type="info" size="large">{{
                    formatCurrency(invoiceDetails.invoice.totalAmount)
                  }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Giảm giá (Voucher)">
                  <el-tag type="warning" size="large"
                    >- {{ formatCurrency(invoiceDetails.invoice.discountAmount) }}</el-tag
                  >
                </el-descriptions-item>
                <el-descriptions-item label="Khách phải trả">
                  <el-tag type="danger" size="large">{{
                    formatCurrency(invoiceDetails.invoice.finalAmount)
                  }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <el-divider />

              <el-form-item label="Tiền khách đưa:">
                <el-input
                  v-model="customerPaidInput"
                  @input="onCustomerPaidInput"
                  placeholder="Nhập số tiền khách đưa"
                  size="large"
                  :formatter="(value) => `₫ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="(value) => value.replace(/₫\s?|(,*)/g, '')"
                />
              </el-form-item>
              <div class="mt-2">
                <strong>Tiền thừa trả khách:</strong>
                <span class="fs-5 text-success ms-2 fw-bold">{{
                  formatCurrency(changeAmount)
                }}</span>
              </div>
              <p v-if="errorMessage" class="text-danger small mt-1">{{ errorMessage }}</p>
            </div>
            <el-empty v-else description="Chưa có thông tin hóa đơn." />
          </el-card>

          <el-card shadow="never">
            <template #header>
              <span>Voucher khuyến mãi</span>
            </template>

            <div v-loading="voucherLoading">
              <div v-if="voucherError" class="alert alert-danger p-2 small">{{ voucherError }}</div>

              <div v-else-if="vouchers.length > 0">
                <el-scrollbar max-height="150px">
                  <div
                    v-for="voucher in vouchers"
                    :key="voucher.id"
                    class="d-flex justify-between align-items-center p-2 border-bottom"
                  >
                    <div>
                      <div>
                        Mã: <strong>{{ voucher.voucherCode }}</strong>
                      </div>
                      <div class="text-muted small">
                        Giảm:
                        <template v-if="voucher.discountPercentage">
                          {{ voucher.discountPercentage }}%
                          <template v-if="voucher.maxDiscountValue">
                            (tối đa {{ formatCurrency(voucher.maxDiscountValue) }})
                          </template>
                        </template>
                        <template v-else>
                          {{ formatCurrency(voucher.discountAmount) }}
                        </template>
                      </div>
                    </div>

                    <!-- BỎ CHỌN -->
                    <el-button
                      v-if="appliedVoucher?.voucherCode === voucher.voucherCode"
                      @click="removeVoucher"
                      :loading="removeLoading"
                      type="danger"
                      size="small"
                      round
                    >
                      Bỏ chọn
                    </el-button>

                    <!-- ÁP DỤNG -->
                    <el-button
                      v-else
                      @click="applyVoucher(voucher.voucherCode)"
                      :loading="applyLoading && applyingVoucherCode === voucher.voucherCode"
                      :disabled="applyLoading"
                      type="primary"
                      size="small"
                      round
                    >
                      Áp dụng
                    </el-button>
                  </div>
                </el-scrollbar>
              </div>

              <el-empty v-else description="Không có voucher phù hợp." :image-size="60" />
            </div>
          </el-card>

          <div class="d-grid gap-2">
            <el-button
              type="primary"
              size="large"
              @click="checkoutInvoice"
              :loading="isLoading"
              :disabled="!invoiceDetails?.details?.length"
            >
              <i class="fas fa-credit-card me-2"></i> Thanh toán
            </el-button>
            <el-button type="danger" plain @click="cancelInvoice">
              <el-icon><CircleClose /></el-icon> Hủy hóa đơn
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-dialog
      v-model="productDialogVisible"
      :title="`Chọn thuộc tính: ${currentProduct?.productName}`"
      width="500px"
      @close="closeProductDialog"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Size:">
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
            <el-form-item label="Màu sắc:">
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
        <el-form-item label="Số lượng:">
          <el-input-number
            v-model="selectedQuantity"
            :min="1"
            :max="Math.max(1, maxQuantity)"
            :disabled="maxQuantity === 0"
          />
          <small v-if="maxQuantity > 0" class="form-text text-muted ms-3"
            >Tối đa có thể chọn: {{ maxQuantity }}</small
          >
          <small v-else class="form-text text-danger ms-3">Sản phẩm này hiện không còn hàng.</small>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeProductDialog">Hủy</el-button>
          <el-button
            type="primary"
            @click="confirmAddProduct"
            :disabled="
              !selectedSizeId ||
              !selectedColorId ||
              selectedQuantity < 1 ||
              selectedQuantity > maxQuantity ||
              maxQuantity === 0
            "
          >
            <el-icon class="me-1"><Check /></el-icon> Xác nhận
          </el-button>
        </span>
      </template>
    </el-dialog>

    <CounterSalesCreateCustomer
      ref="createCustomerDialog"
      @created="handleCustomerCreated"
      @select-customer="selectCreatedCustomer"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
// KHÔNG import axios trực tiếp ở đây nữa

// THAY ĐỔI: Import instance axios đã được cấu hình sẵn
import apiClient from '../../utils/axiosInstance.js'
import { useRoute, useRouter } from 'vue-router'
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

const route = useRoute()
const router = useRouter()

// State
const invoiceDetails = ref(null)
const searchTerm = ref('')
const products = ref([])
const productLoading = ref(false)

// Add Product Dialog State
const productDialogVisible = ref(false)
const currentProduct = ref(null)
const attributes = ref([])
const sizes = ref([])
const colors = ref([])
const selectedSizeId = ref('')
const selectedColorId = ref('')
const selectedQuantity = ref(1)

// Payment State
const changeAmount = ref(0)
const customerPaidInput = ref('')
const customerPaid = ref(0)
const errorMessage = ref('')

// Customer State
const selectedCustomerId = ref(null)
const createCustomerDialog = ref(null)
const createdCustomer = ref(null)

// Voucher State
const appliedVoucher = ref(null)
const voucherLoading = ref(false)
const voucherError = ref('')
const vouchers = ref([])
const applyLoading = ref(false)
const applyError = ref('') // Not directly shown, but used for logic
const applyingVoucherCode = ref(null) // To show loading on specific button
const removeLoading = ref(false) // <- THÊM DÒNG NÀY

// Action State
const isLoading = ref(false)
const invoiceId = Number(route.params.id)

// Gọi API
const updateInvoiceDetailQuantity = async (invoiceDetailId, newQuantity) => {
  const response = await apiClient.put(`/admin/counter-sales/invoice-details/${invoiceDetailId}/quantity`, null, {
    params: { quantity: newQuantity }
  })
  return response.data
}

// Tăng số lượng
const increaseQuantity = async (item) => {
  try {
    const data = await updateInvoiceDetailQuantity(item.id, item.quantity + 1)
    invoiceDetails.value = data
  } catch (e) {
    console.error('Lỗi tăng số lượng:', e)
    // ✅ Lấy message trả về từ backend và báo lên người dùng
    const message = e?.response?.data?.message || 'Đã có lỗi xảy ra'
    ElMessage.error(message)
  }
}

// Giảm số lượng
const decreaseQuantity = async (item) => {
  if (item.quantity > 1) {
    try {
      const data = await updateInvoiceDetailQuantity(item.id, item.quantity - 1)
      invoiceDetails.value = data
    } catch (e) {
      console.error('Lỗi giảm số lượng:', e)
      const message = e?.response?.data?.message || 'Đã có lỗi xảy ra'
      ElMessage.error(message)
    }
  }
}

// --- Computed Properties ---
const maxQuantity = computed(() => {
  if (!currentProduct.value || !selectedSizeId.value || !selectedColorId.value) return 0
  const attr = attributes.value.find(
    (a) => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value,
  )
  return attr ? attr.quantity : 0
})

const getTotalQuantity = computed(() => {
  if (!invoiceDetails.value || !Array.isArray(invoiceDetails.value.details)) return 0
  return invoiceDetails.value.details.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

// --- Methods ---
const formatCurrency = (val) =>
  val == null ? '' : val.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' })

const debounce = (fn, delay) => {
  let timer
  return (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

// API Calls & Logic - Tất cả đều đã được đổi sang apiClient
const fetchInvoiceDetails = async (id) => {
  try {
    const { data } = await apiClient.get(`/admin/counter-sales/${id}/details`)
    console.log(data.details)
    invoiceDetails.value = data

    if (data.invoice?.voucher) {
      appliedVoucher.value = data.invoice.voucher
    } else {
      appliedVoucher.value = null
    }
  } catch (e) {
    ElMessage.error('Lỗi khi tải chi tiết hóa đơn.')
    invoiceDetails.value = null
  }
}

const pagination = ref({
  currentPage: 1,
  pageSize: 5,
  totalPages: 1,
  totalElements: 0,
})

const fetchProducts = async (page = 1) => {
  productLoading.value = true
  try {
    const response = await apiClient.post('/admin/products/search', {
      keyword: searchTerm.value.trim(),
      page: page - 1,
      size: pagination.value.pageSize,
    })
    products.value = response.data.data || []
    pagination.value = {
      ...response.data.pagination,
      currentPage: response.data.pagination.currentPage,
      totalElements: response.data.pagination.totalElements,
    }
  } catch (err) {
    ElMessage.error('Lỗi khi tải sản phẩm.')
  } finally {
    productLoading.value = false
  }
}

const changePage = (page) => {
  pagination.value.currentPage = page
  fetchProducts(page)
}

// --- Product Modal Logic ---
const openProductDialog = async (product) => {
  currentProduct.value = product
  try {
    const { data: attrs } = await apiClient.get(
      // THAY ĐỔI
      `/admin/counter-sales/${product.id}/attributes`,
    )
    attributes.value = attrs
    sizes.value = [...new Map(attrs.filter((a) => a.size).map((a) => [a.size.id, a.size])).values()]
    colors.value = [
      ...new Map(attrs.filter((a) => a.color).map((a) => [a.color.id, a.color])).values(),
    ]
    productDialogVisible.value = true
  } catch (error) {
    ElMessage.error('Không thể lấy thuộc tính sản phẩm.')
  }
}

const closeProductDialog = () => {
  productDialogVisible.value = false
  currentProduct.value = null
  selectedSizeId.value = ''
  selectedColorId.value = ''
  selectedQuantity.value = 1
  attributes.value = []
}

const confirmAddProduct = async () => {
  const invoiceId = invoiceDetails.value?.invoice?.id
  if (!invoiceId) {
    ElMessage.warning('Hóa đơn không hợp lệ.')
    return
  }

  const matchedAttr = attributes.value.find(
    (a) => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value
  )

  if (!matchedAttr) {
    ElMessage.warning('Không tìm thấy biến thể sản phẩm phù hợp.')
    return
  }

  try {
    // 1. Thêm sản phẩm vào hóa đơn
    await apiClient.post(`/admin/counter-sales/${invoiceId}/details`, {
      productDetailId: matchedAttr.id,
      quantity: selectedQuantity.value,
    })

    ElMessage.success(`Đã thêm "${currentProduct.value.productName}" vào giỏ hàng.`)
    closeProductDialog()

    // 2. Làm mới danh sách sản phẩm & chi tiết hóa đơn
    await Promise.all([
      fetchProducts(pagination.value.currentPage),
      fetchInvoiceDetails(invoiceId),
    ])

    // 3. Luôn luôn áp dụng lại voucher tốt nhất sau khi thêm sản phẩm
    try {
      const res = await apiClient.post(`/admin/counter-sales/${invoiceId}/apply-best-voucher`)
      if (res.data && typeof res.data === 'object') {
        appliedVoucher.value = res.data
        ElMessage.success('Đã tự động áp dụng voucher tốt nhất.')
      } else {
        appliedVoucher.value = null
        ElMessage.info('Không có voucher phù hợp để áp dụng.')
      }
    } catch (err) {
      appliedVoucher.value = null
      console.warn('Không thể áp dụng voucher tốt nhất:', err)
    }

    // 4. Gọi lại fetchInvoiceDetails để cập nhật lại giảm giá và tổng tiền
    await fetchInvoiceDetails(invoiceId)

    // 5. Cập nhật danh sách voucher có thể áp dụng
    await fetchVoucherByInvoiceId(invoiceId)

  } catch (error) {
    const message = error.response?.data || 'Thêm sản phẩm thất bại.'
    ElMessage.error(message)
    console.error('Lỗi khi thêm sản phẩm vào hóa đơn:', error)
  }
}

// --- Cart Logic ---
const deleteCartItem = async (invoiceDetailId) => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?',
      'Xác nhận xóa',
      {
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy bỏ',
        type: 'warning',
      },
    )
    await apiClient.delete(`/admin/counter-sales/cart-item/${invoiceDetailId}`) // THAY ĐỔI
    ElMessage.success('Đã xóa sản phẩm khỏi giỏ hàng.')
    await fetchInvoiceDetails(invoiceId)
    await fetchProducts(pagination.value.currentPage)
  } catch (err) {
    if (err !== 'cancel') {
      const message = err.response?.data || 'Xóa sản phẩm thất bại.'
      ElMessage.error(message)
    }
  }
}

// --- Customer Logic ---
const selectCustomer = async (customer) => {
  if (!invoiceDetails.value?.invoice) {
    ElMessage.error('Hóa đơn chưa được tải hoặc không hợp lệ.')
    return
  }
  try {
    await apiClient.put(
      // THAY ĐỔI
      `/admin/counter-sales/${invoiceId}/assign-customer`,
      { customerId: customer.id },
    )
    selectedCustomerId.value = customer.id
    await fetchInvoiceDetails(invoiceId)
    await fetchVoucherByInvoiceId(invoiceId)
    ElMessage.success(`Đã chọn khách hàng: ${customer.customerName || customer.phone}`)
  } catch (error) {
    const message = error.response?.data?.message || 'Không thể cập nhật khách hàng.'
    ElMessage.error(message)
  }
}

const openCreateCustomerDialog = () => createCustomerDialog.value.openDialog()

const handleCustomerCreated = (customer) => (createdCustomer.value = customer)

const selectCreatedCustomer = async () => {
  if (!createdCustomer.value) return
  await selectCustomer(createdCustomer.value)
  createdCustomer.value = null // Reset after selection
}

// --- Payment & Checkout Logic ---
function onCustomerPaidInput() {
  const numericString = customerPaidInput.value.replace(/[^\d]/g, '')
  customerPaid.value = Number(numericString)
  customerPaidInput.value = numericString.replace(/\B(?=(\d{3})+(?!\d))/g, '.')
  calculateChange()
}

async function calculateChange() {
  const finalAmount = invoiceDetails.value?.invoice?.finalAmount || 0
  if (customerPaid.value > 0 && finalAmount > 0) {
    changeAmount.value = customerPaid.value - finalAmount
  } else {
    changeAmount.value = 0
  }

  if (customerPaid.value > 0 && customerPaid.value < finalAmount) {
    errorMessage.value = `Tiền khách đưa chưa đủ.`
  } else {
    errorMessage.value = ''
  }
}

const checkoutInvoice = async () => {
  // Kiểm tra hóa đơn hợp lệ
  if (!invoiceId) {
    ElMessage.error('Hóa đơn không hợp lệ!')
    return
  }

  // Kiểm tra giỏ hàng có sản phẩm
  if (!invoiceDetails.value?.details?.length) {
    ElMessage.error('Giỏ hàng trống, không thể thanh toán!')
    return
  }

  isLoading.value = true

  try {
    // Gửi yêu cầu thanh toán
    const response = await apiClient.post(`/admin/counter-sales/${invoiceId}/checkout`)

    // Hiển thị hộp thoại xác nhận sau khi thanh toán thành công
    const confirmed = await ElMessageBox.confirm(
      response.data?.message || 'Thanh toán thành công! Bạn có muốn in hóa đơn PDF không?',
      'Thành công',
      {
        confirmButtonText: 'Có, In hóa đơn',
        cancelButtonText: 'Không',
        type: 'success',
      }
    ).then(() => true).catch(() => false)

    // Nếu người dùng chọn in hóa đơn
    if (confirmed) {
      const res = await apiClient.get(`/admin/invoices/${invoiceId}/export-id`, {
        responseType: 'blob',
      })

      // Tạo blob và tự động tải về file PDF
      const blob = new Blob([res.data], { type: 'application/pdf' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `HoaDon-${invoiceId}.pdf`
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)
    }

    // Chuyển hướng về danh sách hóa đơn tại quầy
    router.push('/sales-counter/list')
  } catch (err) {
    // Ghi log chi tiết lỗi ra console
    console.error('%c🚨 LỖI THANH TOÁN:', 'color: red; font-weight: bold;', err)

    if (err.response) {
      console.error('↪️ Response status:', err.response.status)
      console.error('↪️ Response data:', err.response.data)
    } else if (err.request) {
      console.error('🛰️ Không nhận được phản hồi từ server:', err.request)
    } else {
      console.error('❌ Lỗi khác:', err.message)
    }

    // Hiển thị lỗi cho người dùng
    const message =
      err.response?.data?.message ||
      err.message ||
      'Có lỗi xảy ra khi thanh toán.'

    ElMessage.error(message)
  } finally {
    isLoading.value = false
  }
}

const cancelInvoice = async () => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn hủy hóa đơn này không? Hành động này không thể hoàn tác.',
      'Xác nhận hủy',
      {
        confirmButtonText: 'Đồng ý hủy',
        cancelButtonText: 'Hủy bỏ',
        type: 'warning',
      },
    )
    await apiClient.post(`/admin/counter-sales/${invoiceId}/cancel`) // THAY ĐỔI
    ElMessage.success('Hủy hóa đơn thành công.')
    router.push('/sales-counter/list')
  } catch (err) {
    if (err !== 'cancel') {
      const message = err.response?.data || 'Không thể hủy hóa đơn.'
      ElMessage.error(message)
    } else {
      ElMessage.info('Đã hủy thao tác.')
    }
  }
}

// --- Voucher Logic ---
const fetchVoucherByInvoiceId = async (id) => {
  voucherLoading.value = true
  voucherError.value = ''
  vouchers.value = []
  try {
    const res = await apiClient.get(`/admin/vouchers/by-invoice/${id}`) // THAY ĐỔI
    vouchers.value = res.data || []
  } catch (error) {
    voucherError.value = 'Chưa chọn khách hàng hoặc không có voucher nào.'
  } finally {
    voucherLoading.value = false
  }
}

const applyVoucher = async (voucherCode) => {
  applyLoading.value = true
  applyingVoucherCode.value = voucherCode
  try {
    const response = await apiClient.post(
      // THAY ĐỔI
      `/admin/counter-sales/${invoiceId}/apply-voucher`,
      null,
      { params: { voucherCode } },
    )
    appliedVoucher.value = response.data
    await fetchInvoiceDetails(invoiceId)
    ElMessage.success(`Voucher ${voucherCode} đã được áp dụng.`)
  } catch (error) {
    const message = error.response?.data || 'Lỗi khi áp dụng voucher.'
    ElMessage.error(message)
    appliedVoucher.value = null // Clear if applying failed
  } finally {
    applyLoading.value = false
    applyingVoucherCode.value = null
  }
}

const removeVoucher = async () => {
  try {
    removeLoading.value = true

    await apiClient.put(`/admin/counter-sales/${invoiceId}/remove-voucher`)

    appliedVoucher.value = null // Xóa voucher đã chọn
    await fetchInvoiceDetails(invoiceId) // Cập nhật chi tiết hóa đơn

    ElMessage.success('Đã bỏ áp dụng voucher.')
  } catch (error) {
    const message = error.response?.data || 'Lỗi khi bỏ voucher.'
    ElMessage.error(message)
  } finally {
    removeLoading.value = false
  }
}

// --- Lifecycle & Watchers ---
onMounted(() => {
  if (invoiceId) {
    fetchInvoiceDetails(invoiceId)
    fetchVoucherByInvoiceId(invoiceId)
  }
  fetchProducts()
})

watch(
  searchTerm,
  debounce(() => fetchProducts(1), 300),
)

watch(maxQuantity, (newMax) => {
  if (newMax === 0) {
    selectedQuantity.value = 1
  } else if (selectedQuantity.value > newMax) {
    selectedQuantity.value = newMax
  }
})
</script>

<style scoped>
/* Added padding here to compensate for removing it from the root container */
.el-container {
  padding: 1.5rem;
}
.mb-3 {
  margin-bottom: 1rem;
}
.mb-4 {
  margin-bottom: 1.5rem;
}
.mt-3 {
  margin-top: 1rem;
}
.mt-4 {
  margin-top: 1.5rem;
}
.w-100 {
  width: 100%;
}
.justify-content-center {
  justify-content: center;
}
.d-flex {
  display: flex;
}
.flex-column {
  flex-direction: column;
}
.gap-3 {
  gap: 1rem;
}
.d-grid {
  display: grid;
}
.gap-2 {
  gap: 0.5rem;
}
.align-items-center {
  align-items: center;
}
.justify-content-between {
  justify-content: space-between;
}
.p-2 {
  padding: 0.5rem;
}
.border-bottom {
  border-bottom: 1px solid var(--el-border-color-light);
}
.me-1 {
  margin-right: 0.25rem;
}
.me-2 {
  margin-right: 0.5rem;
}
.ms-2 {
  margin-left: 0.5rem;
}
.ms-3 {
  margin-left: 1rem;
}
.text-danger {
  color: var(--el-color-danger);
}
.text-success {
  color: var(--el-color-success);
}
.fw-bold {
  font-weight: bold;
}
.fs-5 {
  font-size: 1.25rem;
}
.text-muted {
  color: var(--el-text-color-secondary);
}
.small {
  font-size: 0.875em;
}

/* Ensure El-Card header has no bottom padding for a tighter look */
:deep(.el-card__header) {
  padding-bottom: 10px;
}
</style>

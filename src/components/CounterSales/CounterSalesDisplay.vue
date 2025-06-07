<template>
  <div class="container py-4">
    <button class="btn btn-secondary mb-3" @click="$router.back()">
      <i class="fas fa-arrow-left me-2"></i>Quay lại
    </button>

    <div class="row">
      <div class="col-12 col-lg-8">
        <section class="mb-4 card shadow-sm">
          <div class="card-body">
            <h4 class="card-title">Tìm kiếm sản phẩm</h4>
            <div class="mb-3">
              <label for="productSearchInput" class="form-label visually-hidden"
                >Nhập tên hoặc mã sản phẩm</label
              >
              <input
                id="productSearchInput"
                v-model="searchTerm"
                type="text"
                class="form-control"
                placeholder="Nhập tên hoặc mã sản phẩm..."
                aria-label="Tìm kiếm sản phẩm"
              />
            </div>

            <div class="table-responsive">
              <table class="table table-bordered table-hover table-striped">
                <thead class="table-light">
                  <tr>
                    <th>Mã SP</th>
                    <th>Tên SP</th>
                    <th>Giá</th>
                    <th>Kho</th>
                    <th>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="products.length === 0">
                    <td colspan="5" class="text-center">Không tìm thấy sản phẩm nào.</td>
                  </tr>
                  <tr v-for="product in products" :key="product.id" v-else>
                    <td>{{ product.productCode }}</td>
                    <td>{{ product.productName }}</td>
                    <td>{{ formatCurrency(product.sellPrice) }}</td>
                    <td>{{ product.quantity }}</td>
                    <td>
                      <button class="btn btn-sm btn-primary" @click="openModal(product)">
                        <i class="fas fa-plus me-1"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div
              v-if="pagination.totalPages > 1"
              class="d-flex flex-column flex-sm-row justify-content-between align-items-center mt-3"
            >
              <div class="mb-2 mb-sm-0">
                Trang:
                <strong>{{ pagination.currentPage }} / {{ pagination.totalPages }}</strong> (Tổng:
                {{ pagination.totalElements }} sản phẩm)
              </div>
              <nav aria-label="Product navigation">
                <ul class="pagination mb-0">
                  <li class="page-item" :class="{ disabled: !pagination.hasPrevious }">
                    <a
                      class="page-link"
                      href="#"
                      @click.prevent="changePage(pagination.currentPage - 1)"
                      >Trước</a
                    >
                  </li>
                  <li class="page-item" :class="{ disabled: !pagination.hasNext }">
                    <a
                      class="page-link"
                      href="#"
                      @click.prevent="changePage(pagination.currentPage + 1)"
                      >Tiếp</a
                    >
                  </li>
                </ul>
              </nav>
            </div>
          </div>
        </section>

        <section v-if="invoiceDetails" class="mb-4 card shadow-sm">
          <div class="card-body">
            <h4 class="card-title">Giỏ hàng: {{ invoiceDetails.invoice.invoiceCode }}</h4>
            <div class="table-responsive">
              <table class="table table-bordered table-striped table-hover">
                <thead class="table-light">
                  <tr>
                    <th>Mã CT</th>
                    <th>Tên SP (Chi tiết)</th>
                    <th>Size</th>
                    <th>Màu</th>
                    <th>SL</th>
                    <th>Giá</th>
                    <th>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!invoiceDetails.details || invoiceDetails.details.length === 0">
                    <td colspan="7" class="text-center">Giỏ hàng trống.</td>
                  </tr>
                  <tr v-for="detail in invoiceDetails.details" :key="detail.id" v-else>
                    <td>{{ detail.id }}</td>
                    <td>{{ detail.productName || 'Sản phẩm không xác định' }}</td>
                    <td>{{ detail.size?.sizeName || 'N/A' }}</td>
                    <td>{{ detail.color?.colorName || 'N/A' }}</td>
                    <td>{{ detail.quantity }}</td>
                    <td>{{ formatCurrency(detail.price) }}</td>
                    <td>
                      <button class="btn btn-danger btn-sm" @click="deleteCartItem(detail.id)">
                        <i class="fas fa-trash-alt me-1"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>
        <section v-else class="mb-4 card shadow-sm">
          <div class="card-body">
            <p class="text-center mb-0">Đang tải chi tiết hóa đơn hoặc chưa có hóa đơn...</p>
          </div>
        </section>
      </div>

      <div class="col-12 col-lg-4">
        <div class="d-flex flex-column gap-3">
          <div class="card shadow-sm">
            <div class="card-body">
              <CustomerSearch @select-customer="selectCustomer" />
              <button
                class="btn btn-success w-100 mt-3"
                @click="openCreateCustomerDialog"
                title="Tạo khách hàng mới"
              >
                <i class="fas fa-user-plus me-2"></i>Tạo khách hàng mới
              </button>
            </div>
          </div>

          <CounterSalesCreateCustomer
            ref="createCustomerDialog"
            @created="handleCustomerCreated"
            @select-customer="selectCreatedCustomer"
          />

          <div class="card shadow-sm">
            <div class="card-body">
              <h5 class="card-title">Thông tin hóa đơn</h5>
              <template v-if="invoiceDetails">
                <p>
                  <strong>Tổng tiền:</strong> {{ formatCurrency(invoiceDetails.invoice.finalAmount)
                  }}<br />
                  <strong>Số lượng sản phẩm:</strong> {{ getTotalQuantity }}<br />
                  <strong>Tên khách hàng: {{ invoiceDetails.invoice.customerName }} </strong><br />
                  <strong>VAT (0%):</strong> 0<br />
                  <strong>Chiết khấu:</strong> 0<br />
                  <strong>Khách phải trả:</strong>
                  {{ formatCurrency(invoiceDetails.invoice.finalAmount) }}
                </p>

                <div class="mt-2">
                  <label for="customerPaid"><strong>Tiền khách đưa:</strong></label>
                  <input
                    type="text"
                    id="customerPaid"
                    v-model="customerPaidInput"
                    @input="onCustomerPaidInput"
                    class="form-control"
                    placeholder="Nhập số tiền khách đưa"
                  />
                  <p v-if="errorMessage" class="text-danger mt-1">{{ errorMessage }}</p>
                </div>

                <div class="mt-2">
                  <strong>Tiền thừa trả khách:</strong> {{ formatCurrency(changeAmount) }}
                </div>
              </template>
              <p v-else class="text-muted">Chưa có thông tin hóa đơn.</p>
            </div>
          </div>

          <div class="container mt-3">
            <!-- Loading voucher -->
            <div v-if="voucherLoading" class="small text-muted mb-2 d-flex align-items-center">
              <div
                class="spinner-border spinner-border-sm me-2"
                role="status"
                aria-hidden="true"
              ></div>
              Đang tải voucher...
            </div>

            <!-- Error khi tải voucher -->
            <div v-else-if="voucherError" class="alert alert-danger p-2 py-1 small mb-2">
              {{ voucherError }}
            </div>

            <!-- Danh sách voucher -->
            <ul v-else-if="vouchers.length > 0" class="list-group mb-2">
              <li
                v-for="voucher in vouchers"
                :key="voucher.id"
                class="list-group-item d-flex justify-content-between align-items-center p-2"
              >
                <span class="small"
                  >Mã: <strong>{{ voucher.voucherCode }}</strong></span
                >
                <button
                  @click="applyVoucher(voucher.voucherCode)"
                  :disabled="applyLoading || appliedVoucher?.voucherCode === voucher.voucherCode"
                  class="btn btn-sm"
                  :class="
                    appliedVoucher?.voucherCode === voucher.voucherCode
                      ? 'btn-success'
                      : 'btn-primary'
                  "
                >
                  {{
                    appliedVoucher?.voucherCode === voucher.voucherCode ? 'Đã áp dụng' : 'Áp dụng'
                  }}
                </button>
              </li>
            </ul>

            <!-- Không có voucher -->
            <div v-else class="small text-muted">Không có voucher nào.</div>

            <!-- Loading áp dụng voucher -->
            <div v-if="applyLoading" class="small text-muted d-flex align-items-center mt-2">
              <div
                class="spinner-border spinner-border-sm me-2"
                role="status"
                aria-hidden="true"
              ></div>
              Đang áp dụng...
            </div>

            <!-- Lỗi khi áp dụng -->
            <div v-if="applyError" class="alert alert-danger p-2 py-1 small mt-2">
              {{ applyError }}
            </div>
          </div>
        </div>
      </div>

      <div class="container mt-4">
        <button class="btn btn-primary" @click="checkoutInvoice" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ isLoading ? 'Đang xử lý...' : 'Thanh toán' }}
        </button>

        <div v-if="message" class="alert alert-success mt-3">
          {{ message }}
        </div>

        <div v-if="error" class="alert alert-danger mt-3">
          {{ error }}
        </div>
      </div>

      <div class="mt-3">
        <button @click="cancelInvoice" class="btn btn-danger">
          <i class="bi bi-x-circle"></i> Hủy hóa đơn
        </button>

        <div v-if="message" class="alert alert-success mt-2">
          {{ message }}
        </div>

        <div v-if="error" class="alert alert-danger mt-2">
          {{ error }}
        </div>
      </div>
    </div>

    <div
      v-if="showModal"
      class="modal fade show d-block"
      tabindex="-1"
      role="dialog"
      aria-labelledby="productVariantModalTitle"
      aria-modal="true"
      @click.self="closeModal"
      style="background-color: rgba(0, 0, 0, 0.5)"
    >
      <div class="modal-dialog modal-dialog-centered" role="document" @click.stop>
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="productVariantModalTitle">
              Chọn thuộc tính: {{ currentProduct?.productName }}
            </h5>
            <button type="button" class="btn-close" @click="closeModal" aria-label="Đóng"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="size-select" class="form-label">Size:</label>
              <select
                id="size-select"
                class="form-select"
                v-model="selectedSizeId"
                :disabled="sizes.length === 0"
              >
                <option value="" disabled>
                  -- {{ sizes.length === 0 ? 'Không có size' : 'Chọn size' }} --
                </option>
                <option v-for="s in sizes" :key="s.id" :value="s.id">{{ s.sizeName }}</option>
              </select>
            </div>

            <div class="mb-3">
              <label for="color-select" class="form-label">Màu sắc:</label>
              <select
                id="color-select"
                class="form-select"
                v-model="selectedColorId"
                :disabled="colors.length === 0"
              >
                <option value="" disabled>
                  -- {{ colors.length === 0 ? 'Không có màu' : 'Chọn màu' }} --
                </option>
                <option v-for="c in colors" :key="c.id" :value="c.id">{{ c.colorName }}</option>
              </select>
            </div>

            <div class="mb-3">
              <label for="quantity-input" class="form-label">Số lượng:</label>
              <div class="input-group">
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="selectedQuantity > 1 ? selectedQuantity-- : 1"
                >
                  -
                </button>
                <input
                  id="quantity-input"
                  type="number"
                  min="1"
                  :max="maxQuantity"
                  class="form-control text-center"
                  v-model.number="selectedQuantity"
                  :disabled="maxQuantity === 0"
                />
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="selectedQuantity < maxQuantity ? selectedQuantity++ : maxQuantity"
                >
                  +
                </button>
              </div>
              <small v-if="maxQuantity > 0" class="form-text text-muted"
                >Tối đa có thể chọn: {{ maxQuantity }}</small
              >
              <small v-else class="form-text text-danger"
                >Sản phẩm này hiện không còn hàng với lựa chọn này.</small
              >
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
            <button
              type="button"
              class="btn btn-primary"
              :disabled="
                !selectedSizeId ||
                !selectedColorId ||
                selectedQuantity < 1 ||
                selectedQuantity > maxQuantity ||
                maxQuantity === 0
              "
              @click="confirmAddProduct"
            >
              <i class="fas fa-check me-1"></i>Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import { useToast } from 'vue-toastification'
import CustomerSearch from './CustomerSearch.vue'
import CounterSalesCreateCustomer from './CounterSalesCreateCustomer.vue'
import Swal from 'sweetalert2'
import router from '@/router'
import { ElMessage, ElMessageBox } from 'element-plus'

const toast = useToast()
const route = useRoute()

const invoiceDetails = ref(null)
const searchTerm = ref('')
const products = ref([])
const quantities = ref({})
const attributes = ref([]) // danh sách biến thể sản phẩm (size + màu + số lượng)
const selectedQuantity = ref(1)

const showModal = ref(false)
const currentProduct = ref(null)
const sizes = ref([])
const colors = ref([])
const selectedSizeId = ref('')
const selectedColorId = ref('')

const changeAmount = ref(0)
const customerPaidInput = ref('') // dùng string để xử lý định dạng
const customerPaid = ref(0) // giá trị số thực sự dùng tính toán

const errorMessage = ref('') // khai báo reactive biến errorMessage

const selectedCustomerId = ref(null)

const appliedVoucher = ref(null)

const voucherLoading = ref(false)
const voucherError = ref('')
const vouchers = ref([]) // mảng voucher

const invoiceId = Number(route.params.id)

const invoiceIdCheckout = ref(Number(route.params.id))

const applyLoading = ref(false)
const applyError = ref('')

const message = ref('')
const success = ref(false)

// Trạng thái loading và lỗi
const isLoading = ref(false)
const error = ref('')

const cancelInvoice = async () => {
  try {
    await ElMessageBox.confirm('Bạn có chắc chắn muốn hủy hóa đơn này?', 'Xác nhận hủy', {
      confirmButtonText: 'Đồng ý',
      cancelButtonText: 'Hủy bỏ',
      type: 'warning',
    })

    const response = await axios.post(`http://localhost:8080/api/counter-sales/${invoiceId}/cancel`)
    message.value = response.data // "Hủy hóa đơn thành công"
    error.value = ''
    ElMessage.success('Hủy hóa đơn thành công')
    router.push('/sales-counter/list')
  } catch (err) {
    if (err === 'cancel' || err === 'close') {
      ElMessage.info('Đã hủy thao tác')
    } else if (err.response) {
      error.value = err.response.data
      ElMessage.error(error.value)
    } else {
      error.value = 'Không thể kết nối đến server'
      ElMessage.error(error.value)
    }
    message.value = ''
  }
}

const checkoutInvoice = async () => {
  if (!invoiceDetails.value?.details?.length) {
    toast.error('Giỏ hàng trống, không thể thanh toán!')
    return // Dừng thanh toán nếu giỏ hàng trống
  }

  isLoading.value = true
  error.value = ''

  try {
    const response = await axios.post(
      `http://localhost:8080/api/counter-sales/${invoiceIdCheckout.value}/checkout`,
    )
    message.value = response.data
    success.value = true

    await ElMessageBox.confirm(
      'Thanh toán thành công! Bạn có muốn in hóa đơn không?',
      'In hóa đơn',
      {
        confirmButtonText: 'Có',
        cancelButtonText: 'Không',
        type: 'success',
      },
    )

    const exportResponse = await axios.get(
      `http://localhost:8080/api/invoices/${invoiceIdCheckout.value}/export-id`,
      { responseType: 'blob' },
    )

    const blob = new Blob([exportResponse.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `HoaDon-${invoiceIdCheckout.value}.pdf`
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (err) {
    if (err !== 'cancel') {
      success.value = false
      error.value = err.response?.data || 'Có lỗi xảy ra'
    }
  } finally {
    isLoading.value = false
    router.push('/sales-counter/list')
  }
}

const applyVoucher = async (voucherCode) => {
  applyLoading.value = true
  applyError.value = ''

  try {
    const response = await axios.post(
      `http://localhost:8080/api/counter-sales/${invoiceId}/apply-voucher`,
      null,
      { params: { voucherCode } },
    )

    appliedVoucher.value = response.data

    // Load lại chi tiết hóa đơn để cập nhật thông tin mới
    await fetchInvoiceDetails(invoiceId)

    // Hiển thị thông báo thành công
    Swal.fire({
      icon: 'success',
      title: 'Áp dụng voucher thành công!',
      text: `Voucher ${voucherCode} đã được áp dụng.`,
      timer: 2000,
      showConfirmButton: false,
    })
  } catch (error) {
    const message = error.response?.data || 'Lỗi khi áp dụng voucher.'

    // Set lỗi cho hiển thị trong UI (nếu vẫn muốn)
    applyError.value = message

    // Hiển thị thông báo lỗi
    Swal.fire({
      icon: 'error',
      title: 'Áp dụng voucher thất bại',
      text: message,
    })
  } finally {
    applyLoading.value = false
  }
}

// Hàm fetch voucher theo invoiceId
const fetchVoucherByInvoiceId = async (invoiceId) => {
  voucherLoading.value = true
  voucherError.value = ''
  vouchers.value = []

  try {
    const res = await axios.get(`http://localhost:8080/api/vouchers/by-invoice/${invoiceId}`)
    vouchers.value = res.data || []
  } catch (error) {
    voucherError.value = 'Chưa có khách hàng.'
  } finally {
    voucherLoading.value = false
  }
}

// Watch selectedCustomerId hoặc invoiceDetails.invoice.id để reload voucher
watch(
  () => invoiceDetails.value?.invoice?.id,
  (newInvoiceId) => {
    if (newInvoiceId) {
      fetchVoucherByInvoiceId(newInvoiceId)
    }
  },
  { immediate: true },
)

// Xử lý input tiền khách đưa: bỏ tất cả ký tự không phải số
function onCustomerPaidInput() {
  // Loại bỏ dấu không phải số
  const numericString = customerPaidInput.value.replace(/[^\d]/g, '')
  customerPaidInput.value = numericString.replace(/\B(?=(\d{3})+(?!\d))/g, '.') // format với dấu chấm mỗi 3 số

  // Chuyển về số để tính toán
  customerPaid.value = Number(numericString)
  calculateChange()
}

async function calculateChange() {
  const invoiceId = invoiceDetails.value?.invoice?.id
  const amountGiven = Number(customerPaid.value)
  const totalAmount = invoiceDetails.value?.invoice?.totalAmount || 0

  errorMessage.value = '' // reset lỗi mỗi lần gọi
  changeAmount.value = 0 // reset tiền thừa

  if (!invoiceId) {
    errorMessage.value = 'Hóa đơn không hợp lệ hoặc chưa có dữ liệu.'
    return { success: false, message: errorMessage.value }
  }

  if (amountGiven <= 0) {
    errorMessage.value = 'Vui lòng nhập số tiền khách đưa.'
    return { success: false, message: errorMessage.value }
  }

  if (amountGiven < totalAmount) {
    errorMessage.value = `Tiền khách đưa phải lớn hơn hoặc bằng tổng tiền phải trả (${formatCurrency(totalAmount)}).`
    return { success: false, message: errorMessage.value }
  }

  try {
    const response = await axios.get(
      `http://localhost:8080/api/counter-sales/${invoiceId}/calculate-payment`,
      {
        params: { amountGiven },
      },
    )
    changeAmount.value = response.data.change || 0
    return { success: true, change: changeAmount.value }
  } catch (error) {
    console.error('Lỗi tính tiền thừa:', error)
    errorMessage.value = 'Lỗi máy chủ khi tính tiền thừa. Vui lòng thử lại sau.'
    return { success: false, message: errorMessage.value }
  }
}

const maxQuantity = computed(() => {
  if (!currentProduct.value) return 0
  const attr = attributes.value.find(
    (a) => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value,
  )
  return attr ? attr.quantity : 0
})

const debounce = (fn, delay) => {
  let timer
  return (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

const formatCurrency = (val) =>
  val == null ? '' : val.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' })

const fetchInvoiceDetails = async (id) => {
  try {
    const { data } = await axios.get(`http://localhost:8080/api/counter-sales/${id}/details`)
    invoiceDetails.value = data
  } catch (e) {
    toast.error('Lỗi khi tải chi tiết hóa đơn.')
    invoiceDetails.value = null
  }
}

const pagination = ref({
  currentPage: 1,
  pageSize: 5,
  totalPages: 1,
  hasNext: false,
  hasPrevious: false,
})

const fetchProducts = async (page = 1) => {
  try {
    const response = await axios.post('http://localhost:8080/api/admin/products/search', {
      keyword: searchTerm.value.trim(),
      page: page - 1, // gửi page 0-based lên server
      size: pagination.value.pageSize,
    })

    products.value = response.data.data || []
    pagination.value = {
      ...response.data.pagination,
      currentPage: response.data.pagination.currentPage, // giữ nguyên 1-based để hiển thị
    }

    // Khởi tạo số lượng mặc định cho sản phẩm
    products.value.forEach((p) => {
      if (!quantities.value[p.id]) quantities.value[p.id] = 1
    })
  } catch (err) {
    toast.error('Lỗi khi tải sản phẩm.')
  }
}

const changePage = (targetPage) => {
  if (targetPage >= 1 && targetPage <= pagination.value.totalPages) {
    fetchProducts(targetPage)
  }
}

watch(searchTerm, debounce(fetchProducts, 300))

const openModal = async (product) => {
  currentProduct.value = product
  selectedSizeId.value = ''
  selectedColorId.value = ''
  selectedQuantity.value = 1
  sizes.value = []
  colors.value = []
  attributes.value = []

  try {
    const { data: attrs } = await axios.get(
      `http://localhost:8080/api/counter-sales/${product.id}/attributes`,
    )
    attributes.value = attrs

    // Lấy size/color duy nhất từ attributes
    sizes.value = [...new Map(attrs.filter((a) => a.size).map((a) => [a.size.id, a.size])).values()]
    colors.value = [
      ...new Map(attrs.filter((a) => a.color).map((a) => [a.color.id, a.color])).values(),
    ]

    showModal.value = true
    document.body.style.overflow = 'hidden'
  } catch (error) {
    toast.error('Không thể lấy thuộc tính sản phẩm')
  }
}

const closeModal = () => {
  showModal.value = false
  currentProduct.value = null
  selectedSizeId.value = ''
  selectedColorId.value = ''
  document.body.style.overflow = ''
}

const confirmAddProduct = async () => {
  // Kiểm tra bắt buộc chọn size, màu và có hóa đơn
  if (!invoiceDetails.value || !selectedSizeId.value || !selectedColorId.value) {
    toast.warning('Vui lòng chọn size và màu.')
    return
  }

  try {
    // Lấy danh sách attributes từ API
    const { data: attrs } = await axios.get(
      `http://localhost:8080/api/counter-sales/${currentProduct.value.id}/attributes`,
    )

    // Tìm biến thể phù hợp dựa trên sizeId và colorId được chọn
    const matchedAttr = attrs.find(
      (a) => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value,
    )

    if (!matchedAttr) {
      toast.warning('Không tìm thấy biến thể phù hợp.')
      return
    }

    // Lấy số lượng đã chọn, mặc định 1 nếu không có
    const quantity = selectedQuantity.value || 1

    // Gửi yêu cầu thêm chi tiết hóa đơn (chi tiết sản phẩm)
    await axios.post(
      `http://localhost:8080/api/counter-sales/${invoiceDetails.value.invoice.id}/details`,
      {
        productDetailId: matchedAttr.id,
        quantity,
      },
    )

    toast.success(`Đã thêm "${currentProduct.value.productName}"`)

    // Đóng modal và cập nhật lại dữ liệu sau khi thêm
    closeModal()
    await fetchInvoiceDetails(invoiceDetails.value.invoice.id)
    await fetchProducts() // Cập nhật lại kho hàng
  } catch (error) {
    toast.error('Thêm sản phẩm thất bại.')
    console.error('Error adding product to invoice:', error)
  }
}

onMounted(() => {
  if (route.params.id) fetchInvoiceDetails(route.params.id)
})

onMounted(() => {
  fetchProducts()
})

const selectCustomer = async (customer) => {
  if (!invoiceDetails.value?.invoice) {
    toast.error('Hóa đơn chưa được tải hoặc không hợp lệ.')
    return
  }

  console.log('ID khách hàng được chọn (trước khi gán):', customer.id)

  try {
    await axios.put(
      `http://localhost:8080/api/counter-sales/${invoiceDetails.value.invoice.id}/assign-customer`,
      { customerId: customer.id },
    )

    selectedCustomerId.value = customer.id
    console.log('selectedCustomerId sau khi gán:', selectedCustomerId.value)

    await fetchInvoiceDetails(invoiceDetails.value.invoice.id)

    console.log('Thông tin hóa đơn sau khi tải lại:', invoiceDetails.value.invoice)
    console.log(
      'ID khách hàng gắn với hóa đơn sau khi tải:',
      invoiceDetails.value.invoice.customerId,
    )

    // Gọi lại fetchVoucherByInvoiceId để load voucher mới
    await fetchVoucherByInvoiceId(invoiceDetails.value.invoice.id)
  } catch (error) {
    console.error('Lỗi khi gán khách hàng:', error)
    const message = error.response?.data?.message || 'Không thể cập nhật khách hàng cho hóa đơn.'
    toast.error(message)
  }
}

const deleteCartItem = async (invoiceDetailId) => {
  if (!invoiceDetails.value || !invoiceDetails.value.details) {
    toast.error('Không có hóa đơn để xóa sản phẩm.')
    return
  }

  const deletedItem = invoiceDetails.value.details.find((item) => item.id === invoiceDetailId)
  if (!deletedItem) {
    toast.warning('Không tìm thấy sản phẩm cần xóa trong giỏ hàng.')
    return
  }

  try {
    const response = await axios.delete(
      `http://localhost:8080/api/counter-sales/cart-item/${invoiceDetailId}`,
    )

    if (response.status === 200 || response.status === 204) {
      toast.success('Đã xóa sản phẩm trong giỏ.')

      // 1. Cập nhật lại chi tiết hóa đơn (giỏ hàng) sau khi xóa
      await fetchInvoiceDetails(invoiceDetails.value.invoice.id)

      await fetchProducts() // Hoặc await fetchProducts(1);
    } else {
      toast.error('Xóa sản phẩm thất bại. Mã phản hồi: ' + response.status)
    }
  } catch (error) {
    console.error('Lỗi khi xóa sản phẩm:', error)
    toast.error('Xóa sản phẩm thất bại: ' + (error.response?.data || error.message))
  }
}

const createCustomerDialog = ref(null)
const createdCustomer = ref(null)

function openCreateCustomerDialog() {
  createCustomerDialog.value.openDialog()
}

// Khi tạo khách xong, chỉ lưu tạm customer đã tạo
function handleCustomerCreated(customer) {
  createdCustomer.value = customer
}

// Khi người dùng nhấn chọn khách hàng mới tạo, gọi API gán khách hàng
async function selectCreatedCustomer() {
  if (!createdCustomer.value) {
    toast.error('Chưa có khách hàng để chọn.')
    return
  }
  if (!invoiceDetails.value || !invoiceDetails.value.invoice) {
    toast.error('Hóa đơn chưa được tải hoặc không hợp lệ.')
    return
  }

  try {
    const invoiceId = invoiceDetails.value.invoice.id
    const customerId = createdCustomer.value.id

    await axios.put(`http://localhost:8080/api/counter-sales/${invoiceId}/assign-customer`, {
      customerId,
    })

    toast.success(
      `Thêm khách hàng "${createdCustomer.value.customerName || createdCustomer.value.phone}" thành công!`,
    )
    await fetchInvoiceDetails(invoiceId)
    await fetchVoucherByInvoiceId(invoiceDetails.value.invoice.id)

    // Gán customerId để truyền cho component con
    selectedCustomerId.value = customerId

    // Có thể reset createdCustomer để ẩn phần tạo khách
    createdCustomer.value = null
  } catch (error) {
    console.error('Lỗi khi gán khách hàng:', error)
    toast.error(
      'Không thể cập nhật khách hàng cho hóa đơn: ' + (error.response?.data || error.message),
    )
  }
}

// Watch để tự động cập nhật khi có thay đổi trong invoiceDetails
watch(
  () => invoiceDetails.value,
  (newVal) => {
    if (newVal && newVal.invoice) {
      fetchProducts() // Cập nhật lại danh sách sản phẩm khi có hóa đơn mới
    }
  },
  { immediate: true },
)

// Watch để tự động cập nhật khi có thay đổi trong searchTerm
watch(
  searchTerm,
  (newVal) => {
    if (newVal.trim() === '') {
      products.value = [] // Xóa sản phẩm khi không có từ khóa tìm kiếm
    } else {
      fetchProducts(1) // Tìm kiếm lại với trang đầu tiên
    }
  },
  { immediate: true },
)

const getTotalQuantity = computed(() => {
  if (!invoiceDetails.value || !Array.isArray(invoiceDetails.value.details)) return 0
  return invoiceDetails.value.details.reduce((sum, item) => sum + (item.quantity || 0), 0)
})
</script>

<style scoped>
.modal {
  background: rgba(0, 0, 0, 0.5);
  display: flex !important;
  justify-content: center;
  align-items: center;
  overflow-y: auto;
}
.customer-search {
  display: flex;
  align-items: center;
}

.customer-search input {
  height: 38px;
  padding: 6px 12px;
  box-sizing: border-box;
}
</style>

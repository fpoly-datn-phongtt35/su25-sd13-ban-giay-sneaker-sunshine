<template>
  <div class="container py-4">
    <!-- Nút quay lại -->
    <button class="btn btn-secondary mb-3" @click="$router.back()">Quay lại</button>

    <!-- Bố cục 2 cột: trái (sản phẩm, hóa đơn), phải (khách hàng) -->
    <div class="row">
      <!-- Bên trái: chức năng sản phẩm và hóa đơn -->
      <div class="col-12 col-lg-8">
        <!-- Tìm kiếm sản phẩm -->
        <section class="mt-4">
          <h4>Tìm kiếm sản phẩm</h4>
          <input
            v-model="searchTerm"
            type="text"
            class="form-control"
            placeholder="Nhập tên hoặc mã sản phẩm..."
          />

     <table class="table table-bordered">
      <thead>
        <tr>
          <th>Mã SP</th>
          <th>Tên SP</th>
          <th>Giá</th>
          <th>Kho</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="product in products" :key="product.id">
          <td>{{ product.productCode }}</td>
          <td>{{ product.productName }}</td>
          <td>{{ formatCurrency(product.sellPrice) }}</td>
          <td>{{ product.quantity }}</td>
          <td>
            <button class="btn btn-sm btn-primary" @click="openModal(product)">Chọn</button>
          </td>
        </tr>
      </tbody>
    </table>

          <!-- Phân trang -->
          <div
            v-if="pagination.totalPages > 1"
            class="d-flex justify-content-between align-items-center mt-3"
          >
            <div>Trang Hiện Tại: {{ pagination.currentPage }} / {{ pagination.totalPages }}</div>
            <div>
              <button
                class="btn btn-secondary me-2"
                :disabled="!pagination.hasPrevious"
                @click="changePage(pagination.currentPage - 1)"
              >
                Trước
              </button>
              <button
                class="btn btn-secondary"
                :disabled="!pagination.hasNext"
                @click="changePage(pagination.currentPage + 1)"
              >
                Tiếp
              </button>
            </div>
          </div>
        </section>

        <!-- Chi tiết hóa đơn -->
        <section v-if="invoiceDetails" class="mt-4">
          <h4>Giỏ hàng: {{ invoiceDetails.invoice.invoiceCode }}</h4>

          <table class="table table-bordered table-striped table-hover">
            <thead class="table-light">
              <tr>
                <th>ID</th>
                <th>Mã chi tiết</th>
                <th>Size</th>
                <th>Màu</th>
                <th>Số lượng</th>
                <th>Giá</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="detail in invoiceDetails.details" :key="detail.id">
                <td>{{ detail.id }}</td>
                <td>{{ detail.invoiceCodeDetail || 'NULL' }}</td>
                <td>{{ detail.size?.sizeName || 'N/A' }}</td>
                <td>{{ detail.color?.colorName || 'N/A' }}</td>
                <td>{{ detail.quantity }}</td>
                <td>{{ formatCurrency(detail.price) }}</td>
                <td>
                  <button class="btn btn-danger btn-sm" @click="deleteCartItem(detail.id)">
                    Xóa
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <p v-else>Đang tải chi tiết hóa đơn...</p>
      </div>

      <!-- Bên phải: tìm + tạo khách hàng -->
      <div class="col-12 col-lg-4">
        <div class="d-flex flex-column gap-3">
          <CustomerSearch @select-customer="selectCustomer" />

          <button class="btn btn-primary" @click="openCreateCustomerDialog" title="Tạo khách hàng">
            <i class="me-1"></i>
            Tạo khách hàng
          </button>

          <!-- Di chuyển form tạo khách hàng vào đây -->
          <CounterSalesCreateCustomer
            ref="createCustomerDialog"
            @created="handleCustomerCreated"
            @select-customer="selectCreatedCustomer"
          />
        </div>
      </div>
    </div>

    <!-- Modal chọn Size và Màu -->
  <div v-if="showModal" class="modal d-block" @click.self="closeModal" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog" @click.stop>
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Chọn size & màu & số lượng: {{ currentProduct?.productName }}</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="size-select" class="form-label">Size</label>
              <select id="size-select" class="form-select" v-model="selectedSizeId">
                <option value="" disabled>-- Chọn size --</option>
                <option v-for="s in sizes" :key="s.id" :value="s.id">{{ s.sizeName }}</option>
              </select>
            </div>

            <div class="mb-3">
              <label for="color-select" class="form-label">Màu</label>
              <select id="color-select" class="form-select" v-model="selectedColorId">
                <option value="" disabled>-- Chọn màu --</option>
                <option v-for="c in colors" :key="c.id" :value="c.id">{{ c.colorName }}</option>
              </select>
            </div>

            <div class="mb-3">
              <label for="quantity-input" class="form-label">Số lượng</label>
              <input
                id="quantity-input"
                type="number"
                min="1"
                :max="maxQuantity"
                class="form-control"
                v-model.number="selectedQuantity"
              />
              <small class="text-muted">Tối đa: {{ maxQuantity }}</small>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeModal">Hủy</button>
            <button
              class="btn btn-primary"
              :disabled="!selectedSizeId || !selectedColorId || selectedQuantity < 1 || selectedQuantity > maxQuantity"
              @click="confirmAddProduct"
            >
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed} from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import { useToast } from 'vue-toastification'
import CustomerSearch from './CustomerSearch.vue'
import { eventBus } from '@/components/CounterSales/eventBus'
import CounterSalesCreateCustomer from './CounterSalesCreateCustomer.vue'

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

const maxQuantity = computed(() => {
  if (!currentProduct.value) return 0
  const attr = attributes.value.find(a => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value)
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
    const { data: attrs } = await axios.get(`http://localhost:8080/api/counter-sales/${product.id}/attributes`)
    attributes.value = attrs

    // Lấy size/color duy nhất từ attributes
    sizes.value = [...new Map(attrs.filter(a => a.size).map(a => [a.size.id, a.size])).values()]
    colors.value = [...new Map(attrs.filter(a => a.color).map(a => [a.color.id, a.color])).values()]

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
      `http://localhost:8080/api/counter-sales/${currentProduct.value.id}/attributes`
    )

    // Tìm biến thể phù hợp dựa trên sizeId và colorId được chọn
    const matchedAttr = attrs.find(
      (a) => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value
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
      }
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
  if (!invoiceDetails.value || !invoiceDetails.value.invoice) {
    toast.error('Hóa đơn chưa được tải hoặc không hợp lệ.')
    return
  }

  console.log('Đang gán khách hàng:', customer)
  console.log('Invoice ID:', invoiceDetails.value.invoice.id)

  try {
    const response = await axios.put(
      `http://localhost:8080/api/counter-sales/${invoiceDetails.value.invoice.id}/assign-customer`,
      { customerId: customer.id },
    )
    console.log('Phản hồi từ server khi gán khách hàng:', response.data)

    // Cập nhật lại invoiceDetails sau khi gán khách hàng
    await fetchInvoiceDetails(invoiceDetails.value.invoice.id)
    console.log('InvoiceDetails sau khi cập nhật:', invoiceDetails.value)
  } catch (error) {
    console.error('Lỗi khi gán khách hàng:', error)
    toast.error(
      'Không thể cập nhật khách hàng cho hóa đơn: ' + (error.response?.data || error.message),
    )
  }
}

const deleteCartItem = async (invoiceDetailId) => {
  // 1. Kiểm tra hóa đơn có tồn tại không
  if (!invoiceDetails.value || !invoiceDetails.value.details) {
    toast.error('Không có hóa đơn để xóa sản phẩm.')
    return
  }

  // 2. Tìm sản phẩm trong giỏ để lấy productId và quantity
  const deletedItem = invoiceDetails.value.details.find((item) => item.id === invoiceDetailId)
  if (!deletedItem) {
    toast.warning('Không tìm thấy sản phẩm cần xóa trong giỏ hàng.')
    return
  }

  try {
    // 3. Gọi API xóa sản phẩm khỏi giỏ
    const response = await axios.delete(
      `http://localhost:8080/api/counter-sales/cart-item/${invoiceDetailId}`,
    )

    // 4. Kiểm tra phản hồi thành công (204 hoặc 200)
    if (response.status === 200 || response.status === 204) {
      toast.success('Đã xóa sản phẩm trong giỏ.')

      // 5. Cập nhật lại danh sách chi tiết hóa đơn
      await fetchInvoiceDetails(invoiceDetails.value.invoice.id)

      // 6. Gửi sự kiện để component con cập nhật kho sản phẩm
      eventBus.emit('restore-product-quantity', {
        productId: deletedItem.productId,
        quantity: deletedItem.quantity,
      })
    } else {
      toast.error('Xóa sản phẩm thất bại. Mã phản hồi: ' + response.status)
    }
  } catch (error) {
    // 7. Xử lý lỗi nếu API thất bại
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

    const response = await axios.put(
      `http://localhost:8080/api/counter-sales/${invoiceId}/assign-customer`,
      { customerId },
    )

    toast.success(
      `Thêm khách hàng "${createdCustomer.value.customerName || createdCustomer.value.phone}" thành công!`,
    )
    await fetchInvoiceDetails(invoiceId)

    // Sau khi gán xong có thể xóa createdCustomer để ẩn phần chọn
    createdCustomer.value = null
  } catch (error) {
    console.error('Lỗi khi gán khách hàng:', error)
    toast.error(
      'Không thể cập nhật khách hàng cho hóa đơn: ' + (error.response?.data || error.message),
    )
  }
}
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

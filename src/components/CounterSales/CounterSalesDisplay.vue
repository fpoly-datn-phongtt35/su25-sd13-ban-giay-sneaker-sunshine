<template>
  <div class="container mt-4">
    <h2>Bán hàng - Quầy thu ngân</h2>

    <div class="mb-3">
      <button class="btn btn-primary" @click="createInvoice" :disabled="creatingInvoice">
        {{ creatingInvoice ? 'Đang tạo...' : 'Tạo hóa đơn' }}
      </button>
    </div>

    <div v-if="errorMsg" class="alert alert-danger">{{ errorMsg }}</div>
    <div v-if="successMsg" class="alert alert-success">{{ successMsg }}</div>

    <div v-if="invoices.length" class="mb-4">
      <h5>Hóa đơn đang hoạt động</h5>
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nhân viên</th>
            <th>Khách hàng</th>
            <th>Ngày tạo</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inv in invoices" :key="inv.id">
            <td>{{ inv.id }}</td>
            <td>{{ inv.employeeName }}</td>
            <td>{{ inv.customerName || 'Khách lẻ' }}</td>
            <td>{{ formatDate(inv.createdDate) }}</td>
            <td>
              <button class="btn btn-info btn-sm" @click="viewInvoice(inv.id)">Xem</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="selectedInvoiceId" class="card p-3 shadow-sm mb-4">
      <h5>Chi tiết hóa đơn #{{ selectedInvoiceId }}</h5>
      <table class="table table-striped">
        <thead>
          <tr>
            <th>Sản phẩm</th>
            <th>Size</th>
            <th>Màu</th>
            <th>Số lượng</th>
            <th>Giá bán</th>
            <th>Thành tiền</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in invoiceDetails" :key="item.id">
            <td>{{ item.productName }}</td>
            <td>{{ item.size?.sizeName || '—' }}</td>
            <td>{{ item.color?.colorName || '—' }}</td>
            <td>{{ item.quantity }}</td>
            <td>{{ formatMoney(item.price) }}</td>
            <td>{{ formatMoney(item.quantity * item.price) }}</td>
          </tr>
        </tbody>
      </table>

      <p><strong>Tổng tiền:</strong> {{ formatMoney(totalAmount) }}</p>

      <div class="mb-2">
        <label>Phương thức thanh toán:</label>
        <select v-model="selectedPaymentMethodId" class="form-select w-auto d-inline-block ms-2">
          <option :value="1">Tiền mặt</option>
          <option :value="2">Chuyển khoản</option>
        </select>
      </div>

      <div class="d-flex gap-2 mt-2">
        <button class="btn btn-success" @click="checkoutInvoice">Thanh toán</button>
        <button class="btn btn-danger" @click="cancelInvoice">Hủy hóa đơn</button>
        <button class="btn btn-warning" @click="clearCart" :disabled="clearingCart">
          {{ clearingCart ? 'Đang xóa...' : 'Xóa giỏ hàng' }}
        </button>
      </div>
    </div>

    <div v-if="selectedInvoiceId">
      <h5>Thêm sản phẩm</h5>
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>Sản phẩm</th>
            <th>Giá</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td>{{ p.productName }}</td>
            <td>{{ formatMoney(p.sellPrice) }}</td>
            <td>
              <button class="btn btn-success btn-sm" @click="openAttributeDialog(p)">
                Chọn thuộc tính
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showDialog" class="modal-backdrop">
      <div class="modal-content p-4">
        <h5>Chọn thuộc tính cho {{ selectedProduct.productName }}</h5>
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>Size</th>
              <th>Màu</th>
              <th>SL còn</th>
              <th>Chọn SL</th>
              <th>Thêm</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="attr in productAttributes" :key="attr.id">
              <td>{{ attr.size.sizeName }}</td>
              <td>{{ attr.color.colorName }}</td>
              <td>{{ attr.availableQuantity }}</td>
              <td>
                <input
                  type="number"
                  v-model.number="attr.selectedQty"
                  min="1"
                  :max="attr.availableQuantity"
                />
              </td>
              <td>
                <button
                  class="btn btn-primary btn-sm"
                  :disabled="!canAdd(attr)"
                  @click="addToInvoice(attr)"
                >
                  Thêm
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <button class="btn btn-secondary" @click="showDialog = false">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const API = 'http://localhost:8080/api/counter-sales'

const invoices = ref([])
const invoiceDetails = ref([])
const selectedInvoiceId = ref(null)
const selectedPaymentMethodId = ref(1)

const products = ref([])
const productAttributes = ref([])
const selectedProduct = ref(null)

const creatingInvoice = ref(false)
const showDialog = ref(false)
const clearingCart = ref(false)

const successMsg = ref(null)
const errorMsg = ref(null)

const fetchInvoices = async () => {
  try {
    const res = await axios.get(`${API}/invoices`)
    invoices.value = res.data
  } catch {
    errorMsg.value = 'Không thể tải danh sách hóa đơn.'
  }
}

const fetchProducts = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/products')
    products.value = res.data
  } catch {
    errorMsg.value = 'Không thể tải danh sách sản phẩm.'
  }
}

const createInvoice = async () => {
  successMsg.value = null
  errorMsg.value = null

  // Giới hạn số lượng hóa đơn tạo cùng lúc
  if (invoices.value.length >= 5) {
    errorMsg.value = 'Bạn chỉ được tạo tối đa 5 hóa đơn cùng lúc.'
    return
  }

  creatingInvoice.value = true
  try {
    const res = await axios.post(`${API}/invoices`, { employeeId: 1, customerId: null })
    selectedInvoiceId.value = res.data.id
    await fetchInvoices()
    await viewInvoice(res.data.id)
    successMsg.value = 'Đã tạo hóa đơn mới.'
  } catch {
    errorMsg.value = 'Không thể tạo hóa đơn.'
  } finally {
    creatingInvoice.value = false
  }
}

const viewInvoice = async (id) => {
  successMsg.value = null
  errorMsg.value = null
  try {
    const res = await axios.get(`${API}/invoices/${id}/details`)
    invoiceDetails.value = res.data
    selectedInvoiceId.value = id
  } catch {
    errorMsg.value = 'Không thể tải chi tiết hóa đơn.'
  }
}

const openAttributeDialog = async (product) => {
  selectedProduct.value = product
  successMsg.value = null
  errorMsg.value = null
  try {
    const res = await axios.get(`${API}/products/${product.id}/attributes`)
    productAttributes.value = res.data.map(attr => ({ ...attr, selectedQty: 1 }))
    showDialog.value = true
  } catch {
    errorMsg.value = 'Không thể tải thuộc tính sản phẩm.'
  }
}

const addToInvoice = async (attr) => {
  successMsg.value = null
  errorMsg.value = null
  try {
    await axios.post(`${API}/invoices/${selectedInvoiceId.value}/cart`, null, {
      params: { productDetailId: attr.id, quantity: attr.selectedQty }
    })
    successMsg.value = 'Đã thêm sản phẩm vào hóa đơn.'
    await viewInvoice(selectedInvoiceId.value)
  } catch {
    errorMsg.value = 'Không thể thêm sản phẩm.'
  }
}

const canAdd = (attr) => {
  return attr.selectedQty > 0 && attr.selectedQty <= attr.availableQuantity
}

const checkoutInvoice = async () => {
  if (!confirm('Xác nhận thanh toán hóa đơn?')) return
  successMsg.value = null
  errorMsg.value = null
  try {
    await axios.post(`${API}/invoices/${selectedInvoiceId.value}/checkout`, null, {
      params: { paymentMethodId: selectedPaymentMethodId.value }
    })
    successMsg.value = 'Thanh toán thành công.'
    await fetchInvoices()
    selectedInvoiceId.value = null
    invoiceDetails.value = []
  } catch {
    errorMsg.value = 'Thanh toán thất bại.'
  }
}

const cancelInvoice = async () => {
  if (!confirm('Xác nhận hủy hóa đơn?')) return
  successMsg.value = null
  errorMsg.value = null
  try {
    await axios.post(`${API}/invoices/${selectedInvoiceId.value}/cancel`)
    successMsg.value = 'Đã hủy hóa đơn.'
    await fetchInvoices()
    selectedInvoiceId.value = null
    invoiceDetails.value = []
  } catch {
    errorMsg.value = 'Không thể hủy hóa đơn.'
  }
}

const clearCart = async () => {
  if (!confirm('Xác nhận xóa giỏ hàng?')) return
  clearingCart.value = true
  errorMsg.value = null
  successMsg.value = null
  try {
    await axios.delete(`${API}/invoices/${selectedInvoiceId.value}/cart`)
    successMsg.value = 'Đã xóa giỏ hàng.'
    await viewInvoice(selectedInvoiceId.value)
  } catch {
    errorMsg.value = 'Không thể xóa giỏ hàng.'
  } finally {
    clearingCart.value = false
  }
}

const formatMoney = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const formatDate = (dateStr) => {
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN')
}

const totalAmount = computed(() => {
  return invoiceDetails.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

onMounted(() => {
  fetchInvoices()
  fetchProducts()
})
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background: white;
  max-width: 800px;
  width: 90%;
  border-radius: 8px;
}
</style>

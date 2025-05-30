<template>
  <div>
    <h4>Danh sách sản phẩm</h4>

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
          <td>{{ quantities[product.id] ?? product.quantity }}</td>
          <td>
            <button class="btn btn-sm btn-primary" @click="openModal(product)">Chọn</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Phân trang -->
    <nav aria-label="Page navigation">
      <ul class="pagination justify-content-center">
        <li :class="['page-item', { disabled: currentPage === 0 }]" @click.prevent="prevPage">
          <a class="page-link" href="#">Trước</a>
        </li>

        <li
          v-for="page in totalPages"
          :key="page"
          :class="['page-item', { active: currentPage === page - 1 }]"
          @click.prevent="goToPage(page - 1)"
        >
          <a class="page-link" href="#">{{ page }}</a>
        </li>

        <li :class="['page-item', { disabled: currentPage === totalPages - 1 }]" @click.prevent="nextPage">
          <a class="page-link" href="#">Tiếp</a>
        </li>
      </ul>
    </nav>

    <!-- Modal chọn size, màu, số lượng -->
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
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'

import { eventBus } from './eventBus'

const emit = defineEmits(['product-selected'])
const toast = useToast()

// Dữ liệu
const products = ref([])
const quantities = ref({}) // Số lượng tồn kho đã cập nhật sau chọn

// Phân trang
const currentPage = ref(0)
const pageSize = 10
const totalPages = ref(1)

// Modal và chọn thuộc tính
const showModal = ref(false)
const currentProduct = ref(null)
const sizes = ref([])
const colors = ref([])
const attributes = ref([]) // danh sách biến thể sản phẩm (size + màu + số lượng)
const selectedSizeId = ref('')
const selectedColorId = ref('')
const selectedQuantity = ref(1)

// Định dạng tiền VNĐ
const formatCurrency = (val) =>
  val == null ? '' : val.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' })

// Lấy danh sách sản phẩm theo trang
const fetchProducts = async (page = 0) => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/admin/products', {
      params: { page: page, size: pageSize }
    })
    products.value = data.content || []
    totalPages.value = data.totalPages || 1
    currentPage.value = page

    // Khởi tạo số lượng tồn kho hiển thị
    products.value.forEach(p => {
      if (quantities.value[p.id] == null) quantities.value[p.id] = p.quantity || 0
    })
  } catch (err) {
    console.error(err)
    toast.error('Lỗi khi tải danh sách sản phẩm.')
  }
}

// Mở modal chọn thuộc tính
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

// Đóng modal
const closeModal = () => {
  showModal.value = false
  currentProduct.value = null
  selectedSizeId.value = ''
  selectedColorId.value = ''
  selectedQuantity.value = 1
  document.body.style.overflow = ''
}

// Tính số lượng tối đa có thể chọn dựa trên size + màu đã chọn
const maxQuantity = computed(() => {
  if (!currentProduct.value) return 0
  const attr = attributes.value.find(a => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value)
  return attr ? attr.quantity : 0
})

// Xác nhận thêm sản phẩm
const confirmAddProduct = () => {
  if (!selectedSizeId.value || !selectedColorId.value) {
    toast.warning('Vui lòng chọn size và màu.')
    return
  }
  if (selectedQuantity.value < 1) {
    toast.warning('Số lượng phải lớn hơn 0.')
    return
  }

  const matchedAttr = attributes.value.find(
    a => a.size?.id === selectedSizeId.value && a.color?.id === selectedColorId.value
  )
  if (!matchedAttr) {
    toast.warning('Không tìm thấy biến thể phù hợp.')
    return
  }

  if (selectedQuantity.value > matchedAttr.quantity) {
    toast.warning(`Số lượng vượt quá tồn kho (${matchedAttr.quantity}).`)
    return
  }

  // Cập nhật tồn kho trong client
  quantities.value[currentProduct.value.id] = (quantities.value[currentProduct.value.id] ?? 0) - selectedQuantity.value
  if (quantities.value[currentProduct.value.id] < 0) quantities.value[currentProduct.value.id] = 0

  const payload = {
    productDetailId: matchedAttr.id,
    productId: currentProduct.value.id,
    productName: currentProduct.value.productName,
    quantity: selectedQuantity.value,
    price: currentProduct.value.sellPrice,
    size: matchedAttr.size,
    color: matchedAttr.color,
  }

  emit('product-selected', payload)

  toast.success(`Đã chọn "${currentProduct.value.productName}" (${selectedQuantity.value} cái)`)
  closeModal()
}

// Hàm chuyển trang
const goToPage = (page) => {
  if (page < 0 || page >= totalPages.value) return
  fetchProducts(page)
}

const prevPage = () => {
  if (currentPage.value > 0) fetchProducts(currentPage.value - 1)
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) fetchProducts(currentPage.value + 1)
}

// Khởi tạo dữ liệu lần đầu
onMounted(() => {
  fetchProducts()
})

onMounted(() => {
  eventBus.on('reload-products', () => {
    fetchProducts(currentPage.value)
  })
})

onMounted(() => {
  eventBus.on('restore-product-quantity', ({ productId, quantity }) => {
    if (quantities.value[productId] != null) {
      quantities.value[productId] += quantity
    }
  })
})
</script>

<style scoped>
.modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
}
</style>

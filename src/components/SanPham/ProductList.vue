<template>
  <div class="container-fluid mt-4">
    <div class="mb-2">
      <button @click="goToAdd" class="btn btn-primary btn-sm">
        <i class="bi bi-bag-plus-fill me-1"></i> Thêm sản phẩm
      </button>
    </div>

    <div class="card shadow-sm mb-2">
      <div class="card-body p-2">
        <h5 class="text-center mb-2">Tìm kiếm nâng cao</h5>
        <form @submit.prevent="search">
          <div class="row g-2">
            <div class="col-md-6">
              <label class="form-label fw-bold text-primary small">Tên/Mã</label>
              <div class="input-group input-group-sm">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input
                  type="text"
                  v-model="searchProduct.keyWord"
                  placeholder="Tên/Mã sản phẩm"
                  class="form-control"
                />
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold text-primary small">Danh mục</label>
              <Multiselect
                v-model="searchProduct.categoryIds"
                :options="categoryList"
                :multiple="true"
                :close-on-select="false"
                placeholder="Chọn danh mục"
                label="categoryName"
                track-by="id"
                class="form-select form-select-sm"
              />
              <small class="text-muted">
                Đã chọn:
                <span v-if="searchProduct.categoryIds.length === 0">Chưa chọn</span>
                <span v-else>{{
                  searchProduct.categoryIds.map((c) => c.categoryName).join(', ')
                }}</span>
              </small>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold text-primary small">Chất liệu</label>
              <select v-model="searchProduct.materialId" class="form-select form-select-sm">
                <option value="" disabled selected>Chọn</option>
                <option v-for="mt in materialList" :key="mt.id" :value="mt.id">
                  {{ mt.materialName }}
                </option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold text-primary small">Dành cho</label>
              <div class="d-flex gap-1">
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="genderMale"
                    :value="1"
                    v-model="searchProduct.genderId"
                  />
                  <label class="form-check-label small" for="genderMale">Nam</label>
                </div>
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="genderFemale"
                    :value="2"
                    v-model="searchProduct.genderId"
                  />
                  <label class="form-check-label small" for="genderFemale">Nữ</label>
                </div>
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="genderBoth"
                    :value="3"
                    v-model="searchProduct.genderId"
                  />
                  <label class="form-check-label small" for="genderBoth">Cả hai</label>
                </div>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold text-primary small">Loại đế</label>
              <select v-model="searchProduct.soleId" class="form-select form-select-sm">
                <option value="" disabled selected>Chọn</option>
                <option v-for="sp in soleList" :key="sp.id" :value="sp.id">
                  {{ sp.soleName }}
                </option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold text-primary small">Cổ giày</label>
              <select v-model="searchProduct.styleId" class="form-select form-select-sm">
                <option value="" disabled selected>Chọn</option>
                <option v-for="sp in styleList" :key="sp.id" :value="sp.id">
                  {{ sp.styleName }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold text-primary small">Thương hiệu</label>
              <select v-model="searchProduct.brandId" class="form-select form-select-sm">
                <option value="" disabled selected>Chọn</option>
                <option v-for="br in brandList" :key="br.id" :value="br.id">
                  {{ br.brandName }}
                </option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold text-primary small">Giá</label>
              <div class="input-group input-group-sm">
                <input
                  type="number"
                  v-model="searchProduct.priceMin"
                  placeholder="Min"
                  class="form-control"
                />
                <span class="input-group-text"><i class="bi bi-currency-dollar"></i></span>
                <input
                  type="number"
                  v-model="searchProduct.priceMax"
                  placeholder="Max"
                  class="form-control"
                />
              </div>
            </div>

            <div class="demo-datetime-picker row">
              <label class="form-label fw-bold text-primary small">Ngày tạo (Từ - Đến)</label>
              <div class="col-md-6">
                <el-date-picker
                  v-model="searchProduct.createdFrom"
                  type="datetime"
                  placeholder="Từ ngày"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%"
                />
              </div>
              <div class="col-md-6">
                <el-date-picker
                  v-model="searchProduct.createdTo"
                  type="datetime"
                  placeholder="Đến ngày"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%"
                />
              </div>
            </div>
          </div>

          <div class="text-center mt-3 d-flex justify-content-center gap-2">
            <button type="submit" class="btn btn-primary btn-sm">
              <i class="bi bi-search me-1"></i> Tìm kiếm
            </button>
            <button type="button" @click="resetForm" class="btn btn-outline-secondary btn-sm">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
            </button>
          </div>
        </form>
      </div>
    </div>

    <div class="text-muted small mb-2">Đã chọn: {{ allSelectedProducts.length }} sản phẩm</div>

    <div class="mb-2 d-flex justify-content-between align-items-center">
      <div>
        <button type="button" class="btn btn-success btn-sm" @click="downloadExcel">
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel
        </button>
        <button
          v-if="allSelectedProducts.length > 0"
          type="button"
          class="btn btn-outline-danger btn-sm ms-2"
          @click="clearAllSelections"
        >
          <i class="bi bi-x-circle me-1"></i> Bỏ chọn tất cả
        </button>
      </div>

      <div>
        <button @click="goToHistory" class="btn btn-outline-secondary btn-sm">
          <i class="bi bi-clock-history me-1"></i> Lịch sử sản phẩm
        </button>
      </div>
    </div>

    <div class="card shadow-sm">
      <div class="card-body table-responsive p-2">
        <table class="table table-striped table-hover table-bordered">
          <thead class="table-primary">
            <tr>
              <th scope="col">
                <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
              </th>
              <th scope="col">STT</th>
              <th scope="col">Danh mục</th>
              <th scope="col">Mã sản phẩm</th>
              <th scope="col">Tên sản phẩm</th>
              <th scope="col">Thương hiệu</th>
              <th scope="col">Cổ giày</th>
              <th scope="col">Dành cho</th>
              <th scope="col">Giá bán buôn</th>
              <th scope="col">Giá bán lẻ</th>
              <th scope="col">Số lượng</th>
              <th scope="col">Ghi chú</th>
              <th scope="col">Ngày tạo</th>
              <th scope="col">Người tạo</th>
              <th scope="col">Trạng thái</th>
              <th scope="col">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(pr, index) in productList" :key="pr.id">
              <td>
                <input type="checkbox" v-model="selectedProducts" :value="pr.id" />
              </td>
              <td>{{ page * size + index + 1 }}</td>
              <td>
                <span v-for="(cat, i) in pr.categoryNames" :key="i" class="badge bg-primary me-1">
                  {{ cat }}
                </span>
              </td>
              <td>{{ pr.productCode }}</td>
              <td>{{ pr.productName }}</td>
              <td>{{ pr.brandName }}</td>
              <td>{{ pr.styleName }}</td>
              <td>{{ pr.genderName }}</td>
              <td>{{ pr.originPrice }}</td>
              <td>{{ pr.sellPrice }}</td>
              <td>{{ pr.quantity }}</td>
              <td>{{ pr.description }}</td>
              <td>{{ formatDate(pr.createdDate) }}</td>
              <td>{{ pr.createdBy }}</td>
              <td>
                <span class="badge" :class="pr.status === 1 ? 'bg-success' : 'bg-danger'">
                  {{ pr.status === 1 ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </td>
              <td>
                <div class="btn-group" role="group">
                  <button class="btn btn-info btn-sm" @click="goToUpdate(pr.id)" title="Chỉnh sửa">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <button
                    class="btn btn-secondary btn-sm"
                    @click="goToDetail(pr.id)"
                    title="Chi tiết"
                  >
                    <i class="bi bi-info-circle"></i>
                  </button>
                  <button class="btn btn-sm btn-danger" @click="deleteProduct(pr.id)" title="Xóa">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="d-flex justify-content-end mt-4">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page + 1"
        :page-sizes="[5, 8, 10, 20, 50]"
        :page-size="size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalElements"
        background
      />
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref, watch } from 'vue'
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'
import { useRouter } from 'vue-router'

const router = useRouter()
const productList = ref([])
const totalPages = ref(0) // Giá trị này không còn dùng trực tiếp cho hasNext/hasPrevious với el-pagination
const totalElements = ref(0) // Tổng số phần tử, cần thiết cho el-pagination
const page = ref(0)
const size = ref(8)
const selectedProducts = ref([]) // ID sản phẩm trên trang hiện tại
const allSelectedProducts = ref([]) // Tất cả ID sản phẩm đã chọn qua các trang
const selectAll = ref(false)

const searchProduct = ref({
  keyWord: '',
  brandId: null,
  genderId: null,
  styleId: null,
  soleId: null,
  materialId: null,
  createdFrom: null,
  createdTo: null,
  categoryIds: [],
  priceMin: null,
  priceMax: null,
})

const brandList = ref([])
const materialList = ref([])
const categoryList = ref([])
const soleList = ref([])
const styleList = ref([])

// Reset form tìm kiếm và lựa chọn
const resetForm = async () => {
  searchProduct.value = {
    keyWord: '',
    categoryIds: [],
    materialId: null,
    genderId: null,
    soleId: null,
    styleId: null,
    brandId: null,
    priceMin: null,
    priceMax: null,
    createdFrom: null,
    createdTo: null,
  }
  page.value = 0 // Reset trang về 0
  size.value = 8 // Reset kích thước trang về mặc định
  allSelectedProducts.value = [] // Xóa tất cả lựa chọn
  selectedProducts.value = [] // Xóa lựa chọn trên trang hiện tại
  selectAll.value = false // Bỏ chọn "Chọn tất cả"
  await fetchProduct()
}

// Bỏ chọn tất cả sản phẩm
const clearAllSelections = () => {
  allSelectedProducts.value = []
  selectedProducts.value = [] // Đảm bảo checkbox trên trang hiện tại cũng bỏ chọn
  selectAll.value = false
  ElMessage.info('Đã bỏ chọn tất cả sản phẩm.');
}

// Xử lý checkbox "Chọn tất cả"
const toggleSelectAll = () => {
  if (selectAll.value) {
    const newIds = productList.value
      .map((p) => p.id)
      .filter((id) => !allSelectedProducts.value.includes(id))
    allSelectedProducts.value.push(...newIds)
    selectedProducts.value = [...productList.value.map((p) => p.id)]
  } else {
    // Chỉ loại bỏ các sản phẩm của trang hiện tại khỏi allSelectedProducts
    allSelectedProducts.value = allSelectedProducts.value.filter(
      (id) => !productList.value.some((p) => p.id === id),
    )
    selectedProducts.value = []
  }
}

// Đồng bộ selectedProducts với allSelectedProducts
// Hàm này chạy sau khi productList thay đổi (ví dụ: chuyển trang)
const syncSelectedProducts = () => {
  selectedProducts.value = productList.value
    .filter((p) => allSelectedProducts.value.includes(p.id))
    .map((p) => p.id)
  // Kiểm tra nếu tất cả sản phẩm trên trang hiện tại được chọn
  selectAll.value = productList.value.length > 0 &&
                    selectedProducts.value.length === productList.value.length
}

// Theo dõi selectedProducts để cập nhật allSelectedProducts
watch(selectedProducts, (newSelectedIds, oldSelectedIds) => {
  const currentPageIds = productList.value.map((p) => p.id);

  // Loại bỏ các ID cũ của trang hiện tại khỏi allSelectedProducts
  oldSelectedIds.forEach(id => {
    if (currentPageIds.includes(id) && !newSelectedIds.includes(id)) {
      const index = allSelectedProducts.value.indexOf(id);
      if (index > -1) {
        allSelectedProducts.value.splice(index, 1);
      }
    }
  });

  // Thêm các ID mới của trang hiện tại vào allSelectedProducts
  newSelectedIds.forEach(id => {
    if (!allSelectedProducts.value.includes(id)) {
      allSelectedProducts.value.push(id);
    }
  });

  // Cập nhật trạng thái của checkbox "Chọn tất cả" trên trang hiện tại
  selectAll.value = productList.value.length > 0 &&
                    selectedProducts.value.length === productList.value.length;
}, { deep: true });


// Lấy danh mục sản phẩm
const fetchCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/categories/hien-thi')
    categoryList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy danh mục:', error)
    ElMessage.error('Lấy danh mục thất bại!')
  }
}

// Lấy chất liệu sản phẩm
const fetchMaterial = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/material/hien-thi')
    materialList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy chất liệu:', error)
    ElMessage.error('Lấy chất liệu thất bại!')
  }
}

// Lấy thương hiệu sản phẩm
const fetchBrand = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/brand/hien-thi')
    brandList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy thương hiệu:', error)
    ElMessage.error('Lấy thương hiệu thất bại!')
  }
}

// Lấy đế giày sản phẩm
const fetchSole = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/sole/hien-thi')
    soleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy đế giày:', error)
    ElMessage.error('Lấy đế giày thất bại!')
  }
}

// Lấy phong cách sản phẩm
const fetchStyle = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/style/hien-thi')
    styleList.value = response.data
  } catch (error) {
    console.error('Lỗi lấy phong cách:', error)
    ElMessage.error('Lấy phong cách thất bại!')
  }
}

// Lấy danh sách sản phẩm
const fetchProduct = async () => {
  const searchParams = {
    keyword: searchProduct.value.keyWord || null,
    brandId: searchProduct.value.brandId || null,
    genderId: searchProduct.value.genderId || null,
    styleId: searchProduct.value.styleId || null,
    soleId: searchProduct.value.soleId || null,
    materialId: searchProduct.value.materialId || null,
    createdFrom: searchProduct.value.createdFrom || null,
    createdTo: searchProduct.value.createdTo || null,
    categoryIds: searchProduct.value.categoryIds.map((c) => c.id) || [],
    priceMin: searchProduct.value.priceMin || null,
    priceMax: searchProduct.value.priceMax || null,
    page: page.value,
    size: size.value,
    status: 1 // Chỉ lấy sản phẩm đang hoạt động
  }

  try {
    const response = await axios.post(
      'http://localhost:8080/api/admin/products/search',
      searchParams,
    )
    productList.value = response.data.data
    totalElements.value = response.data.pagination?.totalElements || 0 // Cập nhật tổng số phần tử
    totalPages.value = response.data.pagination?.totalPages || 0 // Giữ lại nếu bạn vẫn muốn hiển thị
    syncSelectedProducts() // Đồng bộ hóa các checkbox trên trang hiện tại
  } catch (error) {
    console.error('Lỗi tải danh sách sản phẩm:', error)
    ElMessage.error('Tải danh sách sản phẩm thất bại!')
  }
}

// Điều hướng
const goToAdd = () => {
  router.push('/product/add')
}

const goToHistory = () => {
  router.push('/product/history')
}

const goToTrash = () => {
  router.push('/product/trash') // Thêm điều hướng tới trang sản phẩm đã xóa
}

const goToUpdate = (id) => {
  router.push({ name: 'UpdateProduct', params: { id } })
}

const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } })
}

// Định dạng ngày
const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('vi-VN', { hour12: false })
}


// --- Phân trang Element Plus handlers ---
const handleSizeChange = (newSize) => {
  size.value = newSize
  page.value = 0 // Reset về trang đầu tiên khi thay đổi số lượng mục trên trang
  fetchProduct()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage - 1 // el-pagination là 1-indexed, API của bạn là 0-indexed
  fetchProduct()
}

// Xuất Excel
const downloadExcel = async () => {
  try {
    const message =
      allSelectedProducts.value.length > 0
        ? `Xuất Excel cho ${allSelectedProducts.value.length} sản phẩm đã chọn?`
        : 'Xuất Excel toàn bộ sản phẩm theo bộ lọc hiện tại?'; // Thay đổi thông báo

    await ElMessageBox.confirm(message, 'Xác nhận', {
      type: 'warning',
      confirmButtonText: 'Xuất',
      cancelButtonText: 'Hủy',
    })

    let url, data, filename
    if (allSelectedProducts.value.length > 0) {
      url = 'http://localhost:8080/api/admin/products/export-excel/by-ids'
      data = allSelectedProducts.value
      filename = 'products-by-ids.xlsx'
    } else {
      const searchParamsForExport = {
        keyword: searchProduct.value.keyWord || null,
        brandId: searchProduct.value.brandId || null,
        genderId: searchProduct.value.genderId || null,
        styleId: searchProduct.value.styleId || null,
        soleId: searchProduct.value.soleId || null,
        materialId: searchProduct.value.materialId || null,
        createdFrom: searchProduct.value.createdFrom || null, // value-format đã xử lý
        createdTo: searchProduct.value.createdTo || null,
        categoryIds: searchProduct.value.categoryIds.map((c) => c.id) || [],
        priceMin: searchProduct.value.priceMin === '' ? null : searchProduct.value.priceMin,
        priceMax: searchProduct.value.priceMax === '' ? null : searchProduct.value.priceMax,
        status: 1 // Chỉ xuất các sản phẩm đang hoạt động
      }
      url = 'http://localhost:8080/api/admin/products/export-excel/by-filter'
      data = searchParamsForExport
      filename = 'products-by-filter.xlsx'
    }

    const response = await axios.post(url, data, { responseType: 'blob' })
    const responseUrl = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = responseUrl
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(responseUrl) // Giải phóng bộ nhớ

    ElMessage.success('Xuất Excel thành công!')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('Lỗi khi xuất Excel:', error)
      ElMessage.error('Xuất Excel thất bại!')
    }
  }
}

// Xóa sản phẩm
const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc chắn muốn xóa sản phẩm này? Sản phẩm sẽ được chuyển vào thùng rác.', 'Xác nhận', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning',
    })
    await axios.delete(`http://localhost:8080/api/admin/products/${id}`)
    ElMessage.success('Xóa sản phẩm thành công!')
    // Cập nhật lại danh sách sau khi xóa
    await fetchProduct()
    // Xóa ID khỏi allSelectedProducts nếu nó bị xóa
    allSelectedProducts.value = allSelectedProducts.value.filter((pId) => pId !== id)
    // Xóa ID khỏi selectedProducts nếu nó nằm trên trang hiện tại
    selectedProducts.value = selectedProducts.value.filter((pId) => pId !== id)
    syncSelectedProducts() // Đồng bộ lại checkbox "Chọn tất cả"
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('Lỗi khi xóa:', error)
    ElMessage.error('Xóa sản phẩm thất bại!')
  }
}

// Tìm kiếm
const search = async () => {
  page.value = 0 // Reset về trang đầu tiên khi tìm kiếm mới
  allSelectedProducts.value = [] // Xóa tất cả các lựa chọn khi tìm kiếm mới
  await fetchProduct()
}

// Khởi tạo dữ liệu
onMounted(() => {
  fetchBrand()
  fetchCategories()
  fetchMaterial()
  fetchSole()
  fetchStyle()
  fetchProduct()
})
</script>

<style scoped>
.container-fluid {
  padding: 15px;
}

.card {
  border-radius: 6px;
  border: none;
}

.card-body {
  padding: 1rem;
}

.table {
  border-radius: 6px;
  overflow: hidden;
}

.table-primary {
  background-color: #007bff;
  color: white;
}

.table-hover tbody tr:hover {
  background-color: #f8f9fa;
}

.btn-group .btn {
  margin-right: 3px;
}

.btn-sm {
  padding: 0.15rem 0.3rem;
}

.form-label {
  font-size: 0.85rem;
  margin-bottom: 0.2rem;
}

.form-control,
.form-select {
  border-radius: 4px;
  font-size: 0.85rem;
  padding: 0.2rem 0.5rem;
}

.input-group-text {
  background-color: #f8f9fa;
  border-radius: 4px 0 0 4px;
  font-size: 0.85rem;
  padding: 0.2rem 0.5rem;
}

.btn {
  font-size: 0.875rem;
  padding: 0.3rem 0.6rem;
}

/* Multiselect specific styles to make it match Bootstrap sm */
.multiselect {
  min-height: auto;
  font-size: 0.85rem;
  min-height: 32px; /* Match Bootstrap form-control-sm height */
}
.multiselect__select {
  height: 32px;
  padding: 0 8px;
}
.multiselect__tags {
  min-height: 32px;
  padding: 4px 40px 0 8px; /* Adjust padding to leave space for the arrow */
}
.multiselect__tag {
  margin-bottom: 4px;
}
.multiselect__input, .multiselect__single {
  height: 24px;
  line-height: 24px;
  font-size: 0.85rem;
  margin-bottom: 4px;
}


/* El-date-picker styles for better integration */
.demo-datetime-picker .el-date-editor {
  height: 32px; /* Match Bootstrap form-control-sm height */
  --el-date-editor-font-size: 0.85rem; /* Adjust font size */
}
.demo-datetime-picker .el-input__wrapper {
  padding: 0.2rem 0.5rem; /* Match Bootstrap form-control-sm padding */
}

/* El-pagination styling for better visual */
.el-pagination {
  --el-pagination-font-size: 0.875rem; /* Slightly smaller font */
  --el-pagination-button-width: 28px; /* Smaller buttons */
  --el-pagination-button-height: 28px; /* Smaller buttons */
  padding: 8px 0;
}
.el-pagination.is-background .el-pager li,
.el-pagination.is-background .btn-prev,
.el-pagination.is-background .btn-next {
    background-color: #f0f2f5; /* Light grey background for pages */
    border-radius: 4px;
}

.el-pagination.is-background .el-pager li:not(.is-disabled).is-active {
    background-color: #007bff; /* Primary color for active page */
    color: #fff;
}

.el-pagination__total,
.el-pagination__sizes,
.el-pagination__jump {
    font-size: 0.875rem; /* Match other pagination elements */
    margin-right: 8px;
}

/* Generic styling for Element Plus components if needed, or rely on defaults */
.text-muted {
  font-size: 0.875rem;
}

.small {
  font-size: 0.8rem;
}
</style>
<template>
  <el-card class="box-card">
    <template #header>
      <span>Quản lý kích thước</span>
    </template>

    <!-- Form Thêm / Sửa -->
    <div class="form-section">
      <el-form :model="form" ref="formRef" label-width="120px">
        <el-form-item
          label="Tên kích thước"
          :rules="[{ required: true, message: 'Tên không được để trống', trigger: 'blur' }]"
        >
          <el-input v-model="form.name" placeholder="Nhập tên kích thước..." />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">{{
            isEditing ? 'Cập nhật' : 'Thêm mới'
          }}</el-button>
          <el-button @click="resetForm">Làm mới</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Bảng hiển thị kích thước -->
    <el-table :data="sizes" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sizeCode" label="Mã kích thước" />
      <el-table-column prop="sizeName" label="Tên kích thước" />
      <el-table-column prop="status" label="Trạng thái">
        <template #default="scope">
          <span :style="{ color: scope.row.status === 1 ? 'green' : 'red' }">
            {{ scope.row.status === 1 ? 'Hoạt động' : 'Không hoạt động' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="Ngày tạo">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdDate) }}
        </template>
      </el-table-column>

      <el-table-column label="Hành động" width="200">
        <template #default="scope">
          <el-button size="small" @click="editSize(scope.row)">Sửa</el-button>
          <el-button size="small" type="danger" @click="confirmDelete(scope.row.id)">Xóa</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const sizes = ref([])
const form = ref({ id: null, name: '' })
const isEditing = ref(false)
const formRef = ref(null)

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('vi-VN', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

const fetchSizes = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/size/hien-thi')
    sizes.value = response.data
  } catch (error) {
    ElMessage.error('Lỗi khi tải dữ liệu kích thước')
  }
}

const handleSubmit = async () => {
  if (!form.value.name || form.value.name.trim() === '') {
    ElMessage.warning('Vui lòng nhập tên kích thước')
    return
  }

  try {
    if (isEditing.value) {
      await axios.put(`http://localhost:8080/api/admin/size/${form.value.id}`, null, {
        params: { name: form.value.name },
      })
      ElMessage.success('Cập nhật thành công')
      await fetchSizes()
      resetForm()
    } else {
      ElMessageBox.confirm('Bạn có chắc chắn muốn thêm mới kích thước?', 'Xác nhận', {
        confirmButtonText: 'Thêm',
        cancelButtonText: 'Hủy',
        type: 'info',
      })
        .then(async () => {
          await axios.post('http://localhost:8080/api/admin/size', null, {
            params: { name: form.value.name },
          })
          ElMessage.success('Thêm mới thành công')
          await fetchSizes()
          resetForm()
        })
        .catch(() => {
          ElMessage.info('Đã hủy thao tác thêm')
        })
    }
  } catch (error) {
    ElMessage.error('Có lỗi xảy ra')
  }
}

const editSize = (size) => {
  form.value = { id: size.id, name: size.sizeName }
  isEditing.value = true
}

const resetForm = () => {
  form.value = { id: null, name: '' }
  isEditing.value = false
  formRef.value?.clearValidate()
}

const confirmDelete = (id) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn xóa?', 'Xác nhận', {
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
    type: 'warning',
  })
    .then(async () => {
      await axios.delete(`http://localhost:8080/api/admin/size/${id}`)
      ElMessage.success('Đã xóa thành công')
      await fetchSizes()
    })
    .catch(() => {})
}

onMounted(fetchSizes)
</script>

<style scoped>
.box-card {
  max-width: 800px;
  margin: 30px auto;
}
.form-section {
  margin-bottom: 20px;
}
</style>

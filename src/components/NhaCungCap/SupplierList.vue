<template>
  <el-card class="box-card">
    <template #header>
      <span>Quản lý nhà cung cấp</span>
    </template>

    <!-- Form Thêm / Sửa -->
    <div class="form-section">
      <el-form :model="form" ref="formRef" label-width="140px" :rules="rules">
        <el-form-item label="Tên nhà cung cấp" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="Nhập tên nhà cung cấp..." />
        </el-form-item>

        <el-form-item label="Quốc gia" prop="country">
          <el-input v-model="form.country" placeholder="Nhập quốc gia..." />
        </el-form-item>

        <el-form-item label="Tỉnh/Thành phố" prop="province">
          <el-input v-model="form.province" placeholder="Nhập tỉnh/thành phố..." />
        </el-form-item>

        <el-form-item label="Quận/Huyện" prop="district">
          <el-input v-model="form.district" placeholder="Nhập quận/huyện..." />
        </el-form-item>

        <el-form-item label="Phường/Xã" prop="ward">
          <el-input v-model="form.ward" placeholder="Nhập phường/xã..." />
        </el-form-item>

        <el-form-item label="Tên nhà/địa chỉ cụ thể" prop="houseName">
          <el-input v-model="form.houseName" placeholder="Nhập tên nhà/địa chỉ cụ thể..." />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">
            {{ isEditing ? 'Cập nhật' : 'Thêm mới' }}
          </el-button>
          <el-button @click="resetForm">Làm mới</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Bảng hiển thị nhà cung cấp -->
    <el-table :data="suppliers" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="supplierName" label="Tên nhà cung cấp" />
      <el-table-column prop="country" label="Quốc gia" />
      <el-table-column prop="province" label="Tỉnh/Thành phố" />
      <el-table-column prop="district" label="Quận/Huyện" />
      <el-table-column prop="ward" label="Phường/Xã" />
      <el-table-column prop="houseName" label="Địa chỉ cụ thể" />
      <el-table-column label="Hành động" width="200">
        <template #default="scope">
          <el-button size="small" @click="editSupplier(scope.row)">Sửa</el-button>
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

const suppliers = ref([])
const form = ref({
  id: null,
  supplierName: '',
  country: '',
  province: '',
  district: '',
  ward: '',
  houseName: '',
})
const isEditing = ref(false)
const formRef = ref(null)

const rules = {
  supplierName: [
    { required: true, message: 'Tên nhà cung cấp là bắt buộc', trigger: 'blur' },
    { max: 250, message: 'Tên nhà cung cấp tối đa 250 ký tự', trigger: 'blur' },
  ],
  country: [
    { required: true, message: 'Quốc gia là bắt buộc', trigger: 'blur' },
    { max: 50, message: 'Quốc gia tối đa 50 ký tự', trigger: 'blur' },
  ],
  province: [
    { required: true, message: 'Tỉnh/Thành phố là bắt buộc', trigger: 'blur' },
    { max: 100, message: 'Tỉnh/Thành phố tối đa 100 ký tự', trigger: 'blur' },
  ],
  district: [
    { required: true, message: 'Quận/Huyện là bắt buộc', trigger: 'blur' },
    { max: 100, message: 'Quận/Huyện tối đa 100 ký tự', trigger: 'blur' },
  ],
  ward: [
    { required: true, message: 'Phường/Xã là bắt buộc', trigger: 'blur' },
    { max: 100, message: 'Phường/Xã tối đa 100 ký tự', trigger: 'blur' },
  ],
  houseName: [
    { required: true, message: 'Tên nhà/địa chỉ cụ thể là bắt buộc', trigger: 'blur' },
    { max: 250, message: 'Tên nhà/địa chỉ tối đa 250 ký tự', trigger: 'blur' },
  ],
}

const fetchSuppliers = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/supplier/hien-thi')
    suppliers.value = res.data
  } catch (error) {
    ElMessage.error('Lỗi khi tải danh sách nhà cung cấp')
  }
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    const confirmMsg = isEditing.value
      ? `Bạn có chắc chắn muốn cập nhật nhà cung cấp "${form.value.supplierName}"?`
      : `Bạn có chắc chắn muốn thêm mới nhà cung cấp "${form.value.supplierName}"?`

    try {
      await ElMessageBox.confirm(confirmMsg, 'Xác nhận', {
        confirmButtonText: isEditing.value ? 'Cập nhật' : 'Thêm mới',
        cancelButtonText: 'Hủy',
        type: 'info',
      })

      if (isEditing.value) {
        await axios.put(`http://localhost:8080/api/admin/supplier/${form.value.id}`, form.value)
        ElMessage.success('Cập nhật thành công')
      } else {
        await axios.post('http://localhost:8080/api/admin/supplier', form.value)
        ElMessage.success('Thêm mới thành công')
      }

      await fetchSuppliers()
      resetForm()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('Có lỗi xảy ra khi gửi dữ liệu!')
      } else {
        ElMessage.info('Đã hủy thao tác')
      }
    }
  })
}

const editSupplier = (supplier) => {
  form.value = { ...supplier }
  isEditing.value = true
}

const resetForm = () => {
  form.value = {
    id: null,
    supplierName: '',
    country: '',
    province: '',
    district: '',
    ward: '',
    houseName: '',
  }
  isEditing.value = false
  formRef.value?.clearValidate()
}

const confirmDelete = (id) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn xóa nhà cung cấp này?', 'Cảnh báo', {
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
    type: 'warning',
  }).then(async () => {
    try {
      await axios.delete(`http://localhost:8080/api/admin/supplier/${id}`)
      ElMessage.success('Xóa thành công')
      await fetchSuppliers()
    } catch (error) {
      ElMessage.error('Lỗi khi xóa nhà cung cấp')
    }
  }).catch(() => {})
}

onMounted(fetchSuppliers)
</script>

<style scoped>
.box-card {
  max-width: 1000px;
  margin: 30px auto;
}
.form-section {
  margin-bottom: 20px;
}
</style>

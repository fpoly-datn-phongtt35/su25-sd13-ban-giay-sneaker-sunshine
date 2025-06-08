<template>
  <div class="container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Brush /></el-icon>
          <span>Quản lý thương hiệu</span>
        </div>
      </template>

      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-position="top"
        label-width="100%"
      >
        <el-row :gutter="20">
          <el-col :xs="24" :sm="18" :md="12" :lg="10">
            <el-form-item label="Tên thương hiệu" prop="brandName">
              <el-input
                v-model="form.brandName"
                placeholder="Nhập tên thương hiệu"
                maxlength="50"
                show-word-limit
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item class="form-actions">
          <el-button
            type="primary"
            :icon="isEditing ? Edit : CirclePlus"
            @click="handleSubmit"
            :loading="loading"
          >
            {{ isEditing ? 'Cập nhật' : 'Thêm mới' }}
          </el-button>
          <el-button type="warning" icon="RefreshRight" @click="resetForm">Làm mới</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-divider content-position="left">Danh sách thương hiệu</el-divider>

    <el-table :data="brands" style="margin-top: 10px" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="brandName" label="Tên thương hiệu" />
      <el-table-column label="Hành động" width="200">
        <template #default="scope">
          <el-button
            type="primary"
            icon="Edit"
            size="small"
            @click="editBrand(scope.row)"
          >Sửa</el-button>
          <el-button
            type="danger"
            icon="Delete"
            size="small"
            @click="deleteBrand(scope.row.id)"
          >Xóa</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Brush, Edit, CirclePlus, RefreshRight } from '@element-plus/icons-vue'

const brands = ref([])
const form = ref({ id: null, brandName: '' })
const isEditing = ref(false)
const formRef = ref()
const loading = ref(false)

const rules = {
  brandName: [
    { required: true, message: 'Tên thương hiệu không được để trống', trigger: 'blur' },
    { min: 2, message: 'Tên thương hiệu tối thiểu 2 ký tự', trigger: 'blur' },
    { max: 50, message: 'Tên thương hiệu tối đa 50 ký tự', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const pattern = /^[\p{L}\d\s]+$/u
        if (!pattern.test(value)) {
          callback(new Error('Tên thương hiệu không chứa ký tự đặc biệt'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const fetchBrands = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/brand/hien-thi')
    brands.value = res.data
  } catch (err) {
    ElMessage.error('Không thể tải danh sách thương hiệu')
  }
}

const resetForm = () => {
  form.value = { id: null, brandName: '' }
  isEditing.value = false
  formRef.value?.resetFields()
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    const brandNameTrimmed = form.value.brandName.trim().toLowerCase()
    const existed = brands.value.some(
      (b) => b.brandName.trim().toLowerCase() === brandNameTrimmed && b.id !== form.value.id
    )
    if (existed) {
      ElMessage.warning('Tên thương hiệu đã tồn tại')
      return
    }

    loading.value = true
    try {
      if (isEditing.value) {
        await axios.put(`http://localhost:8080/api/brand/${form.value.id}`, null, {
          params: { name: form.value.brandName }
        })
        ElMessage.success('Cập nhật thành công')
      } else {
        await axios.post('http://localhost:8080/api/brand', null, {
          params: { name: form.value.brandName }
        })
        ElMessage.success('Thêm mới thành công')
      }
      await fetchBrands()
      resetForm()
    } catch (err) {
      ElMessage.error('Lỗi khi lưu dữ liệu')
    } finally {
      loading.value = false
    }
  })
}

const editBrand = (brand) => {
  form.value = { ...brand }
  isEditing.value = true
}

const deleteBrand = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc muốn xóa thương hiệu này?', 'Xác nhận', {
      type: 'warning'
    })
    await axios.delete(`http://localhost:8080/api/brand/${id}`)
    ElMessage.success('Xóa thành công')
    await fetchBrands()
  } catch (err) {
    ElMessage.error('Không thể xóa')
  }
}

onMounted(() => {
  fetchBrands()
})
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 18px;
}
.form-actions {
  margin-top: 10px;
}
</style>

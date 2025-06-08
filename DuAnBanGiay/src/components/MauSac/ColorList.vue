<template>
  <div class="container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Brush /></el-icon>
          <span>Quản lý màu sắc</span>
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
            <el-form-item label="Tên màu sắc" prop="colorName">
              <el-input
                v-model="form.colorName"
                placeholder="Nhập tên màu sắc"
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

    <el-divider content-position="left">Danh sách màu</el-divider>

    <el-table :data="colors" style="margin-top: 10px" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="colorName" label="Tên màu sắc" />
      <el-table-column label="Hành động" width="200">
        <template #default="scope">
          <el-button
            type="primary"
            icon="Edit"
            size="small"
            @click="editColor(scope.row)"
          >Sửa</el-button>
          <el-button
            type="danger"
            icon="Delete"
            size="small"
            @click="deleteColor(scope.row.id)"
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

const colors = ref([])
const form = ref({ id: null, colorName: '' })
const isEditing = ref(false)
const formRef = ref()
const loading = ref(false)

const rules = {
  colorName: [
    { required: true, message: 'Tên màu không được để trống', trigger: 'blur' },
    { min: 2, message: 'Tên màu tối thiểu 2 ký tự', trigger: 'blur' },
    { max: 50, message: 'Tên màu tối đa 50 ký tự', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const pattern = /^[\p{L}\d\s]+$/u
        if (!pattern.test(value)) {
          callback(new Error('Tên màu không chứa ký tự đặc biệt'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const fetchColors = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/color/hien-thi')
    colors.value = res.data
  } catch (err) {
    ElMessage.error('Không thể tải danh sách màu')
  }
}

const resetForm = () => {
  form.value = { id: null, colorName: '' }
  isEditing.value = false
  formRef.value?.resetFields()
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    const nameTrimmed = form.value.colorName.trim().toLowerCase()
    const existed = colors.value.some(
      (c) => c.colorName.trim().toLowerCase() === nameTrimmed && c.id !== form.value.id
    )
    if (existed) {
      ElMessage.warning('Tên màu đã tồn tại')
      return
    }

    loading.value = true
    try {
      if (isEditing.value) {
        await axios.put(`http://localhost:8080/api/color/${form.value.id}`, null, {
          params: { colorName: form.value.colorName }
        })
        ElMessage.success('Cập nhật thành công')
      } else {
        await axios.post('http://localhost:8080/api/color', null, {
          params: { colorName: form.value.colorName }
        })
        ElMessage.success('Thêm mới thành công')
      }
      await fetchColors()
      resetForm()
    } catch (err) {
      ElMessage.error('Lỗi khi lưu dữ liệu')
    } finally {
      loading.value = false
    }
  })
}

const editColor = (color) => {
  form.value = { ...color }
  isEditing.value = true
}

const deleteColor = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc muốn xóa màu này?', 'Xác nhận', {
      type: 'warning'
    })
    await axios.delete(`http://localhost:8080/api/color/${id}`)
    ElMessage.success('Xóa thành công')
    await fetchColors()
  } catch (err) {
    ElMessage.error('Không thể xóa')
  }
}

onMounted(() => {
  fetchColors()
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

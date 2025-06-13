<template>
  <div class="container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Brush /></el-icon>
          <span>Quản lý Cổ Giày</span>
        </div>
      </template>

      <!-- Form -->
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-position="top"
        label-width="100%"
      >
        <el-row :gutter="20">
          <el-col :xs="24" :sm="18" :md="12" :lg="10">
            <el-form-item label="Tên cổ giày" prop="styleName">
              <el-input
                v-model="form.styleName"
                placeholder="Nhập tên cổ giày"
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
            @click="confirmSubmit"
            :loading="loading"
          >
            {{ isEditing ? 'Cập nhật' : 'Thêm mới' }}
          </el-button>
          <el-button type="warning" :icon="RefreshRight" @click="resetForm">
            Làm mới
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-divider content-position="left">Danh sách kiểu dáng</el-divider>

    <!-- Table -->
    <el-table :data="styles" style="margin-top: 10px" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="styleName" label="Tên kiểu dáng" />
      <el-table-column label="Hành động" width="200">
        <template #default="{ row }">
          <el-button
            type="primary"
            :icon="Edit"
            size="small"
            @click="editStyle(row)"
          >
            Sửa
          </el-button>
          <el-button
            type="danger"
            :icon="Delete"
            size="small"
            @click="confirmDelete(row.id)"
          >
            Xóa
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Brush, Edit, CirclePlus, RefreshRight, Delete } from '@element-plus/icons-vue'

// Trạng thái
const styles = ref([])
const form = ref({ id: null, styleName: '' })
const isEditing = ref(false)
const formRef = ref()
const loading = ref(false)

// Rule validate động
const rules = {
  styleName: [
    { required: true, message: 'Tên kiểu dáng không được để trống', trigger: 'blur' },
    { min: 2, message: 'Tối thiểu 2 ký tự', trigger: 'blur' },
    { max: 50, message: 'Tối đa 50 ký tự', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const trimmed = value.trim().toLowerCase()
        const exists = styles.value.some(
          (s) => s.styleName.trim().toLowerCase() === trimmed &&
            (!isEditing.value || s.id !== form.value.id)
        )
        if (exists) {
          callback(new Error('Tên kiểu dáng đã tồn tại'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// Gọi API lấy danh sách
const fetchStyles = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/style/hien-thi')
    styles.value = res.data
  } catch {
    ElMessage.error('Không thể tải danh sách kiểu dáng')
  }
}

// Reset form
const resetForm = () => {
  form.value = { id: null, styleName: '' }
  isEditing.value = false
  formRef.value?.resetFields()
}

// Xác nhận thêm/sửa
const confirmSubmit = () => {
  const action = isEditing.value ? 'Cập nhật' : 'Thêm mới'
  ElMessageBox.confirm(
    `Bạn có chắc chắn muốn ${action.toLowerCase()} kiểu dáng này?`,
    'Xác nhận',
    {
      confirmButtonText: action,
      cancelButtonText: 'Hủy',
      type: 'info'
    }
  ).then(() => {
    handleSubmit()
  }).catch(() => {})
}

// Gửi dữ liệu form
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEditing.value) {
        await axios.put(`http://localhost:8080/api/admin/style/${form.value.id}`, null, {
          params: { name: form.value.styleName }
        })
        ElMessage.success('Cập nhật thành công')
      } else {
        await axios.post('http://localhost:8080/api/admin/style', null, {
          params: { name: form.value.styleName }
        })
        ElMessage.success('Thêm mới thành công')
      }
      await fetchStyles()
      resetForm()
    } catch {
      ElMessage.error('Lỗi khi lưu dữ liệu')
    } finally {
      loading.value = false
    }
  })
}

// Gán dữ liệu khi nhấn "Sửa"
const editStyle = (style) => {
  form.value = { ...style }
  isEditing.value = true
}

// Xác nhận và xóa
const confirmDelete = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc muốn xóa kiểu dáng này?', 'Cảnh báo', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning'
    })
    await axios.delete(`http://localhost:8080/api/admin/style/${id}`)
    ElMessage.success('Xóa thành công')
    await fetchStyles()
  } catch {
    ElMessage.error('Xóa thất bại')
  }
}

// Load dữ liệu ban đầu
onMounted(() => {
  fetchStyles()
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

<template>
  <div class="container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Brush /></el-icon>
          <span>Quản lý kiểu dáng</span>
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
            <el-form-item label="Tên kiểu dáng" prop="styleName">
              <el-input
                v-model="form.styleName"
                placeholder="Nhập tên kiểu dáng"
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

    <el-divider content-position="left">Danh sách kiểu dáng</el-divider>

    <el-table :data="styles" style="margin-top: 10px" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="styleName" label="Tên kiểu dáng" />
      <el-table-column label="Hành động" width="200">
        <template #default="scope">
          <el-button
            type="primary"
            icon="Edit"
            size="small"
            @click="editStyle(scope.row)"
          >Sửa</el-button>
          <el-button
            type="danger"
            icon="Delete"
            size="small"
            @click="deleteStyle(scope.row.id)"
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

const styles = ref([])
const form = ref({ id: null, styleName: '' })
const isEditing = ref(false)
const formRef = ref()
const loading = ref(false)

const rules = {
  styleName: [
    { required: true, message: 'Tên kiểu dáng không được để trống', trigger: 'blur' },
    { min: 2, message: 'Tên kiểu dáng tối thiểu 2 ký tự', trigger: 'blur' },
    { max: 50, message: 'Tên kiểu dáng tối đa 50 ký tự', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const pattern = /^[\p{L}\d\s]+$/u
        if (!pattern.test(value)) {
          callback(new Error('Tên kiểu dáng không chứa ký tự đặc biệt'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const fetchStyles = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/style/hien-thi')
    styles.value = res.data
  } catch (err) {
    ElMessage.error('Không thể tải danh sách kiểu dáng')
  }
}

const resetForm = () => {
  form.value = { id: null, styleName: '' }
  isEditing.value = false
  formRef.value?.resetFields()
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    const styleNameTrimmed = form.value.styleName.trim().toLowerCase()
    const existed = styles.value.some(
      (s) => s.styleName.trim().toLowerCase() === styleNameTrimmed && s.id !== form.value.id
    )
    if (existed) {
      ElMessage.warning('Tên kiểu dáng đã tồn tại')
      return
    }

    loading.value = true
    try {
      if (isEditing.value) {
        await axios.put(`http://localhost:8080/api/style/${form.value.id}`, null, {
          params: { name: form.value.styleName }
        })
        ElMessage.success('Cập nhật thành công')
      } else {
        await axios.post('http://localhost:8080/api/style', null, {
          params: { name: form.value.styleName }
        })
        ElMessage.success('Thêm mới thành công')
      }
      await fetchStyles()
      resetForm()
    } catch (err) {
      ElMessage.error('Lỗi khi lưu dữ liệu')
    } finally {
      loading.value = false
    }
  })
}

const editStyle = (style) => {
  form.value = { ...style }
  isEditing.value = true
}

const deleteStyle = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc muốn xóa kiểu dáng này?', 'Xác nhận', {
      type: 'warning'
    })
    await axios.delete(`http://localhost:8080/api/style/${id}`)
    ElMessage.success('Xóa thành công')
    await fetchStyles()
  } catch (err) {
    ElMessage.error('Không thể xóa')
  }
}

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

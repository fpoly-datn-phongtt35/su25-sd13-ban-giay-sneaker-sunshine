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
// Import your pre-configured API client
import apiClient from '@/utils/axiosInstance'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Brush, Edit, CirclePlus, RefreshRight, Delete } from '@element-plus/icons-vue' // Ensure all used icons are imported

const styles = ref([])
const form = ref({ id: null, styleName: '' }) // 'styleName' for the form input
const isEditing = ref(false)
const formRef = ref()
const loading = ref(false) // Added loading state for better UX

const rules = {
  styleName: [
    { required: true, message: 'Tên kiểu dáng không được để trống', trigger: 'blur' },
    { min: 2, message: 'Tên kiểu dáng tối thiểu 2 ký tự', trigger: 'blur' },
    { max: 50, message: 'Tên kiểu dáng tối đa 50 ký tự', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        // Unicode property escape \p{L} matches any kind of letter from any language.
        // \d matches digits, \s matches whitespace.
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
  loading.value = true // Set loading state to true
  try {
    // Use apiClient for the GET request
    const res = await apiClient.get('/admin/style/hien-thi')
    styles.value = res.data
    ElMessage.success('Tải danh sách kiểu dáng thành công.')
  } catch (err) {
    console.error('Lỗi khi tải danh sách kiểu dáng:', err);
    ElMessage.error('Không thể tải danh sách kiểu dáng');
  } finally {
    loading.value = false; // Set loading state to false
  }
}

const resetForm = () => {
  form.value = { id: null, styleName: '' }
  isEditing.value = false
  formRef.value?.resetFields() // Safely reset form fields and validation status
  ElMessage.info('Form đã được đặt lại.');
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('Vui lòng kiểm tra lại thông tin form.');
      return;
    }

    const styleNameTrimmed = form.value.styleName.trim().toLowerCase()
    // Check for existing style name, excluding the current style if editing
    const existed = styles.value.some(
      (s) => s.styleName.trim().toLowerCase() === styleNameTrimmed && s.id !== form.value.id
    )
    if (existed) {
      ElMessage.warning('Tên kiểu dáng đã tồn tại');
      return;
    }

    loading.value = true // Set loading state for submission
    try {
      if (isEditing.value) {
        // Use apiClient for the PUT request
        await apiClient.put(`/admin/style/${form.value.id}`, null, {
          params: { name: form.value.styleName }
        })
        ElMessage.success('Cập nhật thành công')
      } else {
        // Use apiClient for the POST request
        await apiClient.post('/admin/style', null, {
          params: { name: form.value.styleName }
        })
        ElMessage.success('Thêm mới thành công')
      }
      await fetchStyles() // Refresh the list after successful operation
      resetForm() // Clear the form
    } catch (err) {
      console.error('Lỗi khi lưu dữ liệu kiểu dáng:', err);
      // Provide more specific error if available from backend
      if (err.response && err.response.data && err.response.data.message) {
        ElMessage.error(`Lỗi: ${err.response.data.message}`);
      } else {
        ElMessage.error('Lỗi khi lưu dữ liệu');
      }
    } finally {
      loading.value = false
    }
  })
}

const editStyle = (style) => {
  form.value = { ...style } // Populate form with existing style data
  isEditing.value = true // Set editing mode
  ElMessage.info(`Đang chỉnh sửa: ${style.styleName}`);
}

const deleteStyle = async (id) => {
  try {
    // Show confirmation dialog before deleting
    await ElMessageBox.confirm('Bạn có chắc muốn xóa kiểu dáng này?', 'Xác nhận', {
      type: 'warning'
    })
    // Use apiClient for the DELETE request
    await apiClient.delete(`/admin/style/${id}`)
    ElMessage.success('Xóa thành công')
    await fetchStyles() // Refresh the list after deletion
    // If the deleted item was the one being edited, reset the form
    if (form.value.id === id) {
      resetForm();
    }
  } catch (err) {
    console.error('Lỗi khi xóa kiểu dáng:', err);
    // Handle user cancellation or API errors
    if (err === 'cancel' || err === 'close') {
      ElMessage.info('Đã hủy thao tác xóa.');
    } else if (err.response && err.response.data && err.response.data.message) {
      ElMessage.error(`Không thể xóa: ${err.response.data.message}`);
    } else {
      ElMessage.error('Không thể xóa');
    }
  }
}

// Fetch styles when the component is mounted
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

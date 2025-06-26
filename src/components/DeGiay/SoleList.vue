<template>
  <el-card class="box-card">
    <template #header>
      <span>Quản lý loại đế</span>
    </template>

    <!-- Form Thêm / Sửa -->
    <div class="form-section">
      <el-form :model="form" ref="formRef" label-width="120px">
        <el-form-item
          label="Tên loại đế"
          :rules="[{ required: true, message: 'Tên không được để trống', trigger: 'blur' }]"
        >
          <el-input v-model="form.name" placeholder="Nhập tên loại đế..." />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">{{
            isEditing ? 'Cập nhật' : 'Thêm mới'
          }}</el-button>
          <el-button @click="resetForm">Làm mới</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Bảng hiển thị loại đế -->
    <el-table :data="soles" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="soleCode" label="Mã loại đế" />
      <el-table-column prop="soleName" label="Tên loại đế" />
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
          <el-button size="small" @click="editSole(scope.row)">Sửa</el-button>
          <el-button size="small" type="danger" @click="confirmDelete(scope.row.id)">Xóa</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// Import your pre-configured API client
import apiClient from '@/utils/axiosInstance'
import { ElMessage, ElMessageBox } from 'element-plus'
// You might want to add icons here, for example:
import { Plus, Edit, Delete, RefreshRight, CirclePlus } from '@element-plus/icons-vue'

const soles = ref([])
const form = ref({ id: null, name: '' }) // 'name' for the form input
const isEditing = ref(false)
const formRef = ref(null)
const loading = ref(false) // Added loading state for better UX

// You might want to add validation rules for 'name' here, similar to other components
// const rules = {
//   name: [
//     { required: true, message: 'Tên loại đế không được để trống', trigger: 'blur' },
//     { min: 2, message: 'Tên loại đế tối thiểu 2 ký tự', trigger: 'blur' },
//     { max: 50, message: 'Tên loại đế tối đa 50 ký tự', trigger: 'blur' },
//     {
//       validator: (_, value, callback) => {
//         const pattern = /^[\p{L}\d\s]+$/u
//         if (!pattern.test(value)) {
//           callback(new Error('Tên loại đế không chứa ký tự đặc biệt'))
//         } else {
//           callback()
//         }
//       },
//       trigger: 'blur'
//     }
//   ]
// }

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  // Check if date is valid before formatting
  if (isNaN(date.getTime())) {
    return dateStr;
  }
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

const fetchSoles = async () => {
  loading.value = true // Set loading state to true
  try {
    // Use apiClient for the GET request
    const response = await apiClient.get('/admin/sole/hien-thi')
    soles.value = response.data
    ElMessage.success('Tải dữ liệu loại đế thành công.')
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu loại đế:', error)
    ElMessage.error('Lỗi khi tải dữ liệu loại đế.')
    soles.value = [] // Clear data on error
  } finally {
    loading.value = false // Set loading state to false
  }
}

const handleSubmit = async () => {
  // If you add rules and use formRef, uncomment the validation block below
  // if (formRef.value) {
  //   const valid = await formRef.value.validate();
  //   if (!valid) {
  //     ElMessage.error('Vui lòng kiểm tra lại thông tin form.');
  //     return;
  //   }
  // }

  if (!form.value.name || form.value.name.trim() === '') {
    ElMessage.warning('Vui lòng nhập tên loại đế')
    return
  }

  // Optional: Check for duplicate names (case-insensitive, exclude current item if editing)
  const nameTrimmed = form.value.name.trim().toLowerCase();
  const existed = soles.value.some(
    (s) => s.soleName?.trim().toLowerCase() === nameTrimmed && s.id !== form.value.id
  );
  if (existed) {
    ElMessage.warning('Tên loại đế đã tồn tại');
    return;
  }

  loading.value = true // Set loading state for submission
  try {
    if (isEditing.value) {
      // Use apiClient for the PUT request (no confirm for update as per original logic)
      await apiClient.put(`/admin/sole/${form.value.id}`, null, {
        params: { name: form.value.name },
      })
      ElMessage.success('Cập nhật thành công')
    } else {
      // Use ElMessageBox.confirm for new entry confirmation
      await ElMessageBox.confirm('Bạn có chắc chắn muốn thêm mới loại đế?', 'Xác nhận', {
        confirmButtonText: 'Thêm',
        cancelButtonText: 'Hủy',
        type: 'info',
      });
      // Use apiClient for the POST request
      await apiClient.post('/admin/sole', null, {
        params: { name: form.value.name },
      })
      ElMessage.success('Thêm mới thành công')
    }
    await fetchSoles() // Refresh the list after successful operation
    resetForm() // Clear the form
  } catch (error) {
    console.error('Lỗi khi lưu dữ liệu loại đế:', error)
    // Handle user cancellation (from ElMessageBox.confirm) or API errors
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác.')
    } else if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(`Lỗi: ${error.response.data.message}`);
    } else {
      ElMessage.error('Có lỗi xảy ra khi lưu dữ liệu.')
    }
  } finally {
    loading.value = false // Reset loading state
  }
}

const editSole = (sole) => {
  // Map fetched 'soleName' to form's 'name'
  form.value = { id: sole.id, name: sole.soleName }
  isEditing.value = true
  ElMessage.info(`Đang chỉnh sửa: ${sole.soleName}`);
}

const resetForm = () => {
  form.value = { id: null, name: '' }
  isEditing.value = false
  formRef.value?.clearValidate() // Uncomment if you add rules and formRef to <el-form>
  ElMessage.info('Form đã được đặt lại.');
}

const confirmDelete = async (id) => {
  try {
    await ElMessageBox.confirm('Bạn có chắc chắn muốn xóa loại đế này?', 'Xác nhận', {
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      type: 'warning',
    })
    // Use apiClient for the DELETE request
    await apiClient.delete(`/admin/sole/${id}`)
    ElMessage.success('Đã xóa thành công')
    await fetchSoles() // Refresh the list
    // If the deleted item was the one being edited, reset the form
    if (form.value.id === id) {
      resetForm();
    }
  } catch (error) {
    console.error('Lỗi khi xóa loại đế:', error)
    // Handle user cancellation or API errors
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('Đã hủy thao tác xóa.')
    } else if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(`Lỗi: ${error.response.data.message}`);
    } else {
      ElMessage.error('Lỗi khi xóa loại đế.')
    }
  }
}

onMounted(fetchSoles)
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

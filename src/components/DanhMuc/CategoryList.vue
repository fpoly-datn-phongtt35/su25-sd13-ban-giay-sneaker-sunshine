<template>
  <div class="p-6">
    <el-card shadow="hover">
      <!-- Tiêu đề và nút thêm -->
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-semibold">Quản lý danh mục</h2>
        <el-button type="primary" @click="openDialog">
          <el-icon><Plus /></el-icon> Thêm danh mục
        </el-button>
      </div>

      <!-- Bảng danh sách -->
      <el-table :data="categories" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="Tên danh mục" />
        <el-table-column prop="categoryCode" label="Mã danh mục" />
        <el-table-column prop="description" label="Mô tả" />
        <el-table-column prop="status" label="Trạng thái" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? 'Hoạt động' : 'Đã xóa' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Hành động" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editCategory(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button size="small" type="danger" @click="confirmDelete(row.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Form thêm/sửa -->
      <el-dialog :title="isEdit ? 'Cập nhật danh mục' : 'Thêm danh mục'" v-model="dialogVisible" width="500px">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="Tên danh mục" prop="categoryName">
            <el-input v-model="form.categoryName" />
          </el-form-item>
          <el-form-item label="Mô tả">
            <el-input type="textarea" v-model="form.description" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="submitForm">Lưu</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

// State
const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref()

// Form data
const form = ref({
  categoryName: '',
  description: ''
})

// Rules
const rules = {
  categoryName: [
    { required: true, message: 'Vui lòng nhập tên danh mục', trigger: 'blur' },
    { min: 3, message: 'Tên danh mục phải có ít nhất 3 ký tự', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const trimmed = value?.trim().toLowerCase()
        const exists = categories.value.some(c =>
          c.categoryName?.trim().toLowerCase() === trimmed &&
          (!isEdit.value || c.id !== editingId.value)
        )
        if (exists) {
          callback(new Error('Tên danh mục đã tồn tại'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// API
const fetchCategories = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/categories/hien-thi')
    categories.value = res.data
  } catch {
    ElMessage.error('Không thể tải danh sách danh mục')
  }
}

const openDialog = () => {
  isEdit.value = false
  form.value = {
    categoryName: '',
    description: ''
  }
  dialogVisible.value = true
}

const editCategory = (category) => {
  isEdit.value = true
  editingId.value = category.id
  form.value = {
    categoryName: category.categoryName,
    description: category.description
  }
  dialogVisible.value = true
}

const formatDateToISOString = (date) => new Date(date).toISOString()

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    const now = formatDateToISOString(new Date())
    const payload = {
      ...form.value,
      updatedDate: now
    }
    if (!isEdit.value) {
      payload.createdDate = now
    }

    try {
      await ElMessageBox.confirm(
        isEdit.value
          ? 'Bạn có chắc chắn muốn cập nhật danh mục này?'
          : 'Bạn có chắc chắn muốn thêm mới danh mục này?',
        'Xác nhận',
        {
          confirmButtonText: isEdit.value ? 'Cập nhật' : 'Thêm',
          cancelButtonText: 'Hủy',
          type: 'info'
        }
      )

      if (isEdit.value) {
        await axios.put(`http://localhost:8080/api/admin/categories/${editingId.value}`, payload)
        ElMessage.success('Cập nhật thành công')
      } else {
        await axios.post('http://localhost:8080/api/admin/categories', payload)
        ElMessage.success('Thêm mới thành công')
      }

      dialogVisible.value = false
      fetchCategories()
    } catch (err) {
      if (err !== 'cancel') ElMessage.error('Lưu thất bại')
    }
  })
}

const confirmDelete = (id) => {
  ElMessageBox.confirm('Bạn có chắc chắn muốn xóa danh mục này?', 'Cảnh báo', {
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`http://localhost:8080/api/admin/categories/${id}`)
      ElMessage.success('Đã xóa thành công')
      fetchCategories()
    } catch {
      ElMessage.error('Xóa thất bại')
    }
  })
}

onMounted(fetchCategories)
</script>

<style scoped>
.text-xl {
  font-size: 1.25rem;
}
</style>

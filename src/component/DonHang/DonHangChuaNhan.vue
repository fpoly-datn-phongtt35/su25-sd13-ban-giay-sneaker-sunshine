<template>
  <div class="p-6 bg-white rounded shadow max-w-3xl mx-auto">
    <h2 class="text-xl font-semibold mb-4">Báo cáo chưa nhận/thiếu hàng ({{ invoiceId }})</h2>

    <el-form :model="form" label-width="150px">
      <el-form-item label="Vấn đề gặp phải">
        <el-select v-model="form.reason" placeholder="Chọn vấn đề">
          <el-option label="Chưa nhận được đơn hàng" value="Chưa nhận được đơn hàng" />
          <el-option label="Thiếu sản phẩm trong đơn" value="Thiếu sản phẩm trong đơn" />
        </el-select>
      </el-form-item>

      <el-form-item label="Ghi chú">
        <el-input type="textarea" v-model="form.description" rows="3" placeholder="Mô tả tình huống..." />
      </el-form-item>
    </el-form>

    <el-form-item v-if="form.reason === 'Thiếu sản phẩm trong đơn'" label="Ảnh/Video nhận hàng">
      <el-upload
        class="upload-demo"
        action=""
        :auto-upload="false"
        :file-list="fileList"
        :on-change="handleFileChange"
        :on-remove="handleRemove"
        list-type="picture-card"
        accept="image/*,video/*"
      >
        <el-icon><Plus /></el-icon>
        <template #file="{ file }">
          <div class="el-upload-list__item is-success">
            <video v-if="file.raw && file.raw.type.startsWith('video/')" controls style="width: 100%; height: 100%; object-fit: cover;">
              <source :src="file.url" />
            </video>
            <img v-else :src="file.url" class="el-upload-list__item-thumbnail" />

            <span class="el-upload-list__item-actions">
              <span
                class="el-upload-list__item-preview"
                @click="handlePreview(file)"
              >
                <el-icon><ZoomIn /></el-icon>
              </span>
              <span
                class="el-upload-list__item-delete"
                @click="handleRemove(file)"
              >
                <el-icon><Delete /></el-icon>
              </span>
            </span>
          </div>
        </template>
      </el-upload>
    </el-form-item>


    <div class="text-right mt-6">
      <el-button @click="goBack">Quay lại</el-button>
      <el-button type="primary" @click="submitReport">Gửi báo cáo</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import apiClient from '@/utils/axiosInstance'
import { Plus, Delete } from '@element-plus/icons-vue' // Import Delete icon

const route = useRoute()
const router = useRouter()
const invoiceId = route.params.invoiceId
const fileList = ref([])

const form = ref({
  reason: '',
  description: ''
})

const handleFileChange = (file, fileListRaw) => {
  // Khi có file mới hoặc file bị thay đổi, cập nhật fileList
  fileList.value = fileListRaw.map(item => ({
    ...item,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : '') // Đảm bảo có URL cho cả file mới
  }))
}

// Chỉnh sửa hàm handleRemove để loại bỏ file cụ thể
const handleRemove = (fileToRemove) => {
  // Lọc bỏ file khỏi danh sách dựa trên uid hoặc một thuộc tính định danh khác
  fileList.value = fileList.value.filter(file => file.uid !== fileToRemove.uid)
  // Nếu bạn đã tạo URL.createObjectURL, hãy thu hồi nó để tránh rò rỉ bộ nhớ
  if (fileToRemove.url && fileToRemove.url.startsWith('blob:')) {
    URL.revokeObjectURL(fileToRemove.url)
  }
}

// Xử lý xem trước ảnh/video
const handlePreview = (file) => {
  if (file.url) {
    window.open(file.url, '_blank')
  } else {
    ElMessage.warning('Không có URL để xem trước file này.')
  }
}

const submitReport = async () => {
  if (!form.value.reason) {
    ElMessage.warning('Vui lòng chọn vấn đề gặp phải')
    return
  }

  const formData = new FormData()
  formData.append('invoiceId', invoiceId)
  formData.append('reason', form.value.reason)
  formData.append('description', form.value.description || '')

  fileList.value.forEach((fileObj) => {
    formData.append('files', fileObj.raw)
  })

  try {
    await apiClient.post('/complaints', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success('Đã gửi thông tin khiếu nại')

    form.value = {
      reason: '',
      description: ''
    }
    fileList.value = []

    router.push({ name: 'OrderList' })
  } catch (e) {
    ElMessage.error('Lỗi khi gửi báo cáo')
  }
}

const goBack = () => router.back()
</script>

<style scoped>
.el-upload-list__item {
  position: relative;
}
</style>

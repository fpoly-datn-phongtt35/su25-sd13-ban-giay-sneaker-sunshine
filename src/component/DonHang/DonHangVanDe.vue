<template>
  <div class="p-6 bg-white rounded shadow max-w-3xl mx-auto">
    <h2 class="text-xl font-semibold mb-4">Trả hàng do có vấn đề ({{ invoiceId }})</h2>

    <el-form :model="form" label-width="140px">
      <el-form-item label="Lý do trả hàng">
        <el-select v-model="form.reason" placeholder="Chọn lý do">
          <el-option label="Sản phẩm bị hỏng" value="Sản phẩm bị hỏng" />
          <el-option label="Không đúng mô tả" value="Không đúng mô tả" />
          <el-option label="Khác" value="Khác" />
        </el-select>
      </el-form-item>

      <el-form-item label="Ghi chú thêm">
        <el-input type="textarea" v-model="form.note" rows="3" placeholder="Nhập mô tả chi tiết..." />
      </el-form-item>

      <el-form-item label="Ảnh minh chứng">
        <el-upload
          action="#"
          list-type="picture-card"
          :auto-upload="false"
          :on-preview="handlePreview"
          :on-remove="handleRemove"
          :file-list="form.images"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>

    <div class="text-right mt-6">
      <el-button @click="goBack">Quay lại</el-button>
      <el-button type="primary" @click="submitReturn">Gửi yêu cầu trả hàng</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const invoiceId = route.params.invoiceId

const form = ref({
  reason: '',
  note: '',
  images: []
})

const handlePreview = (file) => {
  window.open(file.url, '_blank')
}
const handleRemove = (file, fileList) => {
  form.value.images = fileList
}

const submitReturn = async () => {
  if (!form.value.reason) {
    ElMessage.warning('Vui lòng chọn lý do trả hàng')
    return
  }
  // TODO: Gửi API xử lý trả hàng
  ElMessage.success('Đã gửi yêu cầu trả hàng')
  router.push({ name: 'OrderList' }) // hoặc quay lại trang trước
}

const goBack = () => router.back()
</script>

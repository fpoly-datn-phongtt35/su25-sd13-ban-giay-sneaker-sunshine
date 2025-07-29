<template>
  <div class="p-4 full-screen">
    <el-button type="primary" icon="el-icon-arrow-left" @click="goBack" class="mb-4">
      Quay lại
    </el-button>

    <el-card>
      <h3>Tiến trình đơn hàng</h3>

      <el-steps
        v-if="['HUY_DON'].includes(invoice.statusDetail)"
        :active="0"
        align-center
        finish-status="error"
      >
        <el-step
          :title="'Đã hủy'"
          status="error"
        />
      </el-steps>

      <el-steps
        v-else
        :active="getActiveStep(invoice.statusDetail)"
        finish-status="success"
        align-center
      >
        <el-step
          v-for="step in mainSteps"
          :key="step.key"
          :title="step.label"
        />
      </el-steps>

      <div class="mt-4 flex flex-wrap gap-2">
        <el-button type="danger" @click="showCancelDialog" v-if="canCancel">Hủy đơn hàng</el-button>
      </div>

      <el-divider />

      <h2>Thông tin đơn hàng</h2>
      <p><strong>Mã hóa đơn:</strong> {{ invoice.invoiceCode }}</p>
      <p><strong>Khách hàng:</strong> {{ invoice.customerName }}</p>
      <p><strong>Số điện thoại:</strong> {{ invoice.phone }}</p>
      <p><strong>Ngày tạo:</strong> {{ formatDate(invoice.createdDate) }}</p>
      <p><strong>Tổng tiền:</strong> {{ formatCurrency(invoice.totalAmount) }}</p>
      <p><strong>Giảm giá:</strong> {{ formatCurrency(invoice.discountAmount) }}</p>
      <p><strong>Phí vận chuyển:</strong> {{ formatCurrency(invoice.shippingFee) }}</p>
      <p><strong>Thành tiền:</strong> {{ formatCurrency(invoice.finalAmount) }}</p>
      <p><strong>Địa chỉ giao hàng:</strong> {{ invoice.deliveryAddress}}</p>
      <el-button
          v-if="canEditAddress"
          type="primary"
          size="small"
          class="ml-2"
          @click="openAddressDialog"
        >Cập nhật</el-button>
      <el-divider />

    <el-dialog v-model="addressDialogVisible" title="Cập nhật địa chỉ giao hàng" width="600px">

      <div class="flex gap-4 mb-4">
        <el-select v-model="addressForm.provinceCode" placeholder="Chọn tỉnh/thành"
                         class="flex-1" filterable @change="handleProvinceChangeForAddress">
          <el-option v-for="p in provinces" :key="p.ProvinceID" :label="p.ProvinceName" :value="p.ProvinceID" />
        </el-select>

        <el-select v-model="addressForm.districtCode" placeholder="Chọn quận/huyện"
                         class="flex-1" filterable @change="handleDistrictChangeForAddress">
          <el-option v-for="d in districts" :key="d.DistrictID" :label="d.DistrictName" :value="d.DistrictID" />
        </el-select>

        <el-select v-model="addressForm.wardCode" placeholder="Chọn phường/xã"
                         class="flex-1" filterable @change="handleWardChangeForAddress ">
          <el-option v-for="w in wards" :key="w.WardCode" :label="w.WardName" :value="w.WardCode" />
        </el-select>
              <div class="mb-4">
        <el-input v-model="addressForm.houseNumber" placeholder="Số nhà, tên đường" />
      </div>

      </div>

      <div class="text-right">
        <el-button @click="addressDialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="submitAddressUpdate">Cập nhật</el-button>
      </div>
    </el-dialog>

      <h3>Lịch sử thanh toán</h3>
      <el-table :data="invoice.invoiceTransactionResponses || []" border stripe>
        <el-table-column label="STT" width="70" type="index" />
        <el-table-column prop="transactionCode" label="Mã giao dịch" />
        <el-table-column prop="transactionType" label="Loại giao dịch" />
        <el-table-column prop="paymentMethod" label="PT Thanh toán" />
        <el-table-column prop="paymentTime" label="Thời gian" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="transactionNote" label="Ghi chú" />
      </el-table>

      <el-divider />

      <h3>Sản phẩm đã mua</h3>
      <el-table :data="invoice.invoiceDetailResponses || []" border stripe>
        <el-table-column label="STT" width="70" type="index" />
        <el-table-column prop="productDetailName" label="Tên sản phẩm" />
        <el-table-column prop="sizeName" label="Kích thước" />
        <el-table-column prop="colorName" label="Màu sắc" />
        <el-table-column prop="quantity" label="Số lượng" />
      </el-table>
    </el-card>

    <el-dialog title="Lý do hủy đơn hàng" v-model="cancelDialogVisible" width="400px">
      <el-form label-position="top">
        <el-form-item label="Lý do hủy đơn">
          <el-input type="textarea" v-model="cancelNote" placeholder="Nhập lý do hủy..." rows="3" />
        </el-form-item>
        <el-form-item v-if="invoice && invoice.isPaid" label="Phương thức hoàn tiền">
          <el-select v-model="selectedPaymentMethod" placeholder="Chọn phương thức hoàn tiền">
            <el-option label="Tiền mặt" value="TIEN_MAT" />
            <el-option label="ZaloPay" value="ZALOPAY" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">Hủy</el-button>
        <el-button type="danger" @click="cancelOrder">Xác nhận hủy đơn</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiClient from '@/utils/axiosInstance' // Giả sử đây là axios instance của bạn
import { ElMessage } from 'element-plus'
import axios from 'axios' // Import axios cho GHN API

const route = useRoute()
const router = useRouter()
const invoiceId = route.params.id
const invoice = ref({})
const cancelDialogVisible = ref(false)
const cancelNote = ref('')
const selectedPaymentMethod = ref('TIEN_MAT')
const addressDialogVisible = ref(false)


const canEditAddress = computed(() => {
  const status = invoice.value?.statusDetail
  return mainStepKeys.includes(status) &&
    mainStepKeys.indexOf(status) < mainStepKeys.indexOf('CHO_GIAO_HANG')
})


const provinces = ref([])
const districts = ref([])
const wards = ref([])

const addressForm = ref({
  houseNumber: '',
  provinceCode: null,
  provinceName: '',
  districtCode: null,
  districtName: '',
  wardCode: null,
  wardName: '',
})

const GHN_TOKEN = '847c9bb7-6e42-11ee-a59f-a260851ba65c' // Thay bằng token GHN thực của bạn

const loadProvincesForAddress = async () => {
  try {
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/province',
      {},
      { headers: { Token: GHN_TOKEN } }
    )
    provinces.value = res.data.data
  } catch (err) {
    console.error(err)
    ElMessage.error('Không thể tải danh sách tỉnh/thành phố.')
  }
}

const loadDistrictsForAddress = async () => {
  addressForm.value.districtCode = null
  addressForm.value.districtName = ''
  addressForm.value.wardCode = null
  addressForm.value.wardName = ''
  districts.value = []
  wards.value = []

  if (!addressForm.value.provinceCode) return

  try {
    const res = await axios.get(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/district',
      {
        headers: { Token: GHN_TOKEN },
        params: { province_id: addressForm.value.provinceCode }
      }
    )
    districts.value = res.data.data
  } catch (err) {
    console.error(err)
    ElMessage.error('Không thể tải danh sách quận/huyện.')
  }
}

const loadWardsForAddress = async () => {
  addressForm.value.wardCode = null
  addressForm.value.wardName = ''
  wards.value = []

  if (!addressForm.value.districtCode) return

  try {
    const res = await axios.get(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/ward',
      {
        headers: { Token: GHN_TOKEN },
        params: { district_id: addressForm.value.districtCode }
      }
    )
    wards.value = res.data.data
  } catch (err) {
    console.error(err)
    ElMessage.error('Không thể tải danh sách phường/xã.')
  }
}

const handleProvinceChangeForAddress = () => {
  const selected = provinces.value.find(p => p.ProvinceID === addressForm.value.provinceCode)
  addressForm.value.provinceName = selected?.ProvinceName || ''
  loadDistrictsForAddress()
}

const handleDistrictChangeForAddress = () => {
  const selected = districts.value.find(d => d.DistrictID === addressForm.value.districtCode)
  addressForm.value.districtName = selected?.DistrictName || ''
  loadWardsForAddress()
}

const handleWardChangeForAddress = () => {
  const selected = wards.value.find(w => w.WardCode === addressForm.value.wardCode)
  addressForm.value.wardName = selected?.WardName || ''
}


// Cập nhật địa chỉ mới
const submitAddressUpdate = async () => {
  const {
    houseNumber,
    provinceName,
    districtName,
    wardName
  } = addressForm.value

  // if (!houseNumber || !provinceName || !districtName || !wardName) {
  //   ElMessage.warning('Vui lòng nhập đầy đủ thông tin địa chỉ.')
  //   return
  // }

  console.log('tỉnh: ',addressForm.value);

  const fullAddress = `${houseNumber} - ${wardName} - ${districtName} - ${provinceName} - Việt Nam`

  try {
    await apiClient.put(`/online-sale/cap-nhat-dia-chi`, null, {
      params: {
        invoiceId,
        newAddress: fullAddress
      }
    })

    ElMessage.success('Cập nhật địa chỉ thành công!')
    addressDialogVisible.value = false
    fetchInvoice() // Cập nhật lại thông tin hóa đơn sau khi đổi địa chỉ
  } catch (err) {
    ElMessage.error('Lỗi cập nhật địa chỉ!')
    console.error(err)
  }
}

const openAddressDialog = async () => {
  addressDialogVisible.value = true;

  // Đảm bảo danh sách tỉnh/thành phố được tải trước khi xử lý
  await loadProvincesForAddress();

  // Reset form địa chỉ để tránh dữ liệu cũ
  addressForm.value = {
    houseNumber: '',
    provinceCode: null,
    provinceName: '',
    districtCode: null,
    districtName: '',
    wardCode: null,
    wardName: '',
  };
  districts.value = [];
  wards.value = [];

  // Tách địa chỉ hiện tại từ invoice.value.deliveryAddress
  const currentAddress = invoice.value.deliveryAddress || '';
  // Ví dụ địa chỉ: "123 Đường ABC - Phường XYZ - Quận 1 - TP.HCM - Việt Nam"
  const addressParts = currentAddress.split(' - ').map(part => part.trim());

  if (addressParts.length >= 4) { // Đảm bảo có đủ các phần địa chỉ
    addressForm.value.houseNumber = addressParts[0];
    const wardName = addressParts[1];
    const districtName = addressParts[2];
    const provinceName = addressParts[3];

    // Gán provinceCode và provinceName
    const foundProvince = provinces.value.find(p => p.ProvinceName === provinceName);
    if (foundProvince) {
      addressForm.value.provinceCode = foundProvince.ProvinceID;
      addressForm.value.provinceName = foundProvince.ProvinceName;

      // Sau khi gán province, tải danh sách quận/huyện
      await loadDistrictsForAddress();

      // Gán districtCode và districtName
      const foundDistrict = districts.value.find(d => d.DistrictName === districtName);
      if (foundDistrict) {
        addressForm.value.districtCode = foundDistrict.DistrictID;
        addressForm.value.districtName = foundDistrict.DistrictName;

        // Sau khi gán district, tải danh sách phường/xã
        await loadWardsForAddress();

        // Gán wardCode và wardName
        const foundWard = wards.value.find(w => w.WardName === wardName);
        if (foundWard) {
          addressForm.value.wardCode = foundWard.WardCode;
          addressForm.value.wardName = foundWard.WardName;
        }
      }
    }
  }
};


const mainSteps = [
  { key: 'CHO_XU_LY', label: 'Chờ xử lý' },
  { key: 'DA_XU_LY', label: 'Đã xử lý' },
  { key: 'CHO_GIAO_HANG', label: 'Chờ giao hàng' },
  { key: 'DANG_GIAO_HANG', label: 'Đang giao hàng' },
  { key: 'GIAO_THANH_CONG', label: 'Giao thành công' },
]
const mainStepKeys = mainSteps.map(s => s.key)

const getActiveStep = (statusKey) => {
  return mainStepKeys.indexOf(statusKey || '')
}

const canCancel = computed(() => {
  const status = invoice.value?.statusDetail
  return mainStepKeys.includes(status) && mainStepKeys.indexOf(status) < mainStepKeys.indexOf('DANG_GIAO_HANG')
})

const fetchInvoice = async () => {
  try {
    const res = await apiClient.get(`/admin/online-sales/get-order-customer-detail`, {
      params: { invoiceId: invoiceId }
    })
    invoice.value = res.data
    console.log('data: ',res.data)
  } catch (err) {
    ElMessage.error('Lỗi khi tải thông tin đơn hàng.')
    console.error(err)
  }
}

const showCancelDialog = () => {
  cancelNote.value = ''
  selectedPaymentMethod.value = 'TIEN_MAT'
  cancelDialogVisible.value = true
}

const cancelOrder = async () => {
  try {
    if (!cancelNote.value.trim()) {
      ElMessage.warning('Vui lòng nhập lý do hủy đơn!')
      return
    }
    // Dòng đã sửa: Thêm kiểm tra invoice.value
    if (invoice.value && invoice.value.isPaid && !selectedPaymentMethod.value) {
      ElMessage.warning('Vui lòng chọn phương thức hoàn tiền!')
      return
    }

    await apiClient.put(`/online-sale/huy-don-va-hoan-tien`, null, {
      params: {
        invoiceId,
        statusDetail: 'HUY_DON',
        note: cancelNote.value,
        paymentMethod: selectedPaymentMethod.value,
        isPaid: invoice.value.isPaid
      }
    })

    ElMessage.success('Hủy đơn hàng và hoàn tiền thành công!')
    cancelDialogVisible.value = false
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi hủy đơn hàng.')
    console.error(err)
  }
}

const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('vi-VN') : ''
const formatCurrency = (val) => `${Number(val || 0).toLocaleString('vi-VN')} ₫`
const goBack = () => router.back()


onMounted(() => {
  fetchInvoice()
})
</script>

<style scoped>
.full-screen {
  max-width: 100%;
}
.mt-4 {
  margin-top: 20px;
}
.el-card {
  max-width: 100%;
  margin: auto;
}
.el-dialog {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba
  (0, 0, 0, 0.15);
}
.el-dialog__header {
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.el-dialog__title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
}
.el-form-item__label {
  font-size: 0.95rem;
  font-weight: 500;
  color: #555;
}
</style>

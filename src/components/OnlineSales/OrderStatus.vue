<template>
  <div class="p-4 full-screen">
    <el-button type="primary" icon="el-icon-arrow-left" @click="goBack" class="mb-4">
      Quay lại
    </el-button>

    <el-card>
      <h3>Tiến trình đơn hàng</h3>

      <el-steps
        v-if="['HUY_DON', 'GIAO_THAT_BAI'].includes(invoice.statusDetail)"
        :active="0"
        align-center
        finish-status="error"
      >
        <el-step
          :title="invoice.statusDetail === 'HUY_DON' ? 'Đã hủy' : 'Giao hàng thất bại'"
          status="error"
        />
      </el-steps>

      <el-steps
        v-else
        :active="getActiveStep(invoice.statusDetail)"
        finish-status="success"
        align-center
      >
        <el-step v-for="step in mainSteps" :key="step.key" :title="step.label" />
      </el-steps>

      <div class="mt-4 flex flex-wrap gap-2">
        <el-button type="success" @click="confirmAdvance" v-if="canAdvance">Chuyển trạng thái tiếp theo</el-button>
        <el-button type="warning" @click="confirmRevert" v-if="canRevert">Quay lại trạng thái trước</el-button>
        <el-button type="danger" @click="showCancelDialog" v-if="canCancel">Hủy đơn hàng</el-button>
        <el-button
          type="danger"
          @click="showFailDialog"
          v-if="invoice.statusDetail === 'DANG_GIAO_HANG'"
        >Giao hàng thất bại</el-button>
        <el-button @click="showActionHistoryDialog">Lịch sử tác động</el-button>
      </div>

      <el-divider />

      <h2>Thông tin đơn hàng</h2>
      <p><strong>Mã hóa đơn:</strong> {{ invoice.invoiceCode }}</p>
      <p><strong>Khách hàng:</strong> {{ invoice.customerName }}</p>
      <p>
        <strong>Số điện thoại:</strong> {{ invoice.phone }}
        <el-button type="text" @click="openDialog('phone')">Sửa</el-button>
      </p>
      <p><strong>Ngày tạo:</strong> {{ formatDate(invoice.createdDate) }}</p>
      <p><strong>Tổng tiền:</strong> {{ formatCurrency(invoice.totalAmount) }}</p>
      <p><strong>Giảm giá:</strong> {{ formatCurrency(invoice.discountAmount) }}</p>
      <p>
        <strong>Địa chỉ giao hàng:</strong> {{ invoice.deliveryAddress }}
        <el-button type="text" @click="openAddressDialog">Sửa</el-button>
      </p>
      <p><strong>Phí vận chuyển:</strong> {{ formatCurrency(invoice.shippingFee) }}</p>
      <p><strong>Thành tiền:</strong> {{ formatCurrency(invoice.finalAmount) }}</p>

      <el-divider />

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

        <el-table-column label="Giá" align="center">
          <template #default="{ row }">
            <div>
              <span
                v-if="row.sellPrice !== row.discountedPrice"
                style="text-decoration: line-through; color: gray; margin-right: 6px"
              >
                {{ formatCurrency(row.sellPrice) }}
              </span>
              <span style="color: red; font-weight: bold">
                {{ formatCurrency(row.discountedPrice) }}
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-input v-model="dialogValue" :placeholder="dialogTitle" />
      <template #footer>
        <el-button @click="dialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="saveChange">Lưu</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addressDialogVisible" title="Cập nhật địa chỉ giao hàng" width="600px">
      <el-form :model="addressForm" label-position="top">
        <el-form-item label="Tỉnh/Thành phố">
          <el-select
            v-model="addressForm.provinceCode"
            placeholder="Chọn tỉnh/thành"
            class="w-full"
            filterable
            @change="handleProvinceChangeForAddress">
            <el-option
              v-for="p in provinces"
              :key="p.ProvinceID"
              :label="p.ProvinceName"
              :value="p.ProvinceID"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Quận/Huyện">
          <el-select
            v-model="addressForm.districtCode"
            placeholder="Chọn quận/huyện"
            class="w-full"
            filterable
            @change="handleDistrictChangeForAddress">
            <el-option
              v-for="d in districts"
              :key="d.DistrictID"
              :label="d.DistrictName"
              :value="d.DistrictID"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Phường/Xã">
          <el-select
            v-model="addressForm.wardCode"
            placeholder="Chọn phường/xã"
            class="w-full"
            filterable
            @change="handleWardChangeForAddress">
            <el-option
              v-for="w in wards"
              :key="w.WardCode"
              :label="w.WardName"
              :value="w.WardCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Số nhà, tên đường">
          <el-input v-model="addressForm.houseNumber" placeholder="Nhập số nhà, tên đường" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addressDialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="submitAddressUpdate">Cập nhật</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="Hủy đơn hàng" v-model="cancelDialogVisible" width="500px">
      <div class="mb-3">
        <label class="font-medium">Lý do hủy đơn hàng</label>
        <el-input
          type="textarea"
          v-model="cancelNote"
          placeholder="Nhập lý do hủy đơn hàng..."
          rows="3"
        />
      </div>

      <div v-if="isPaid">
        <label class="font-medium">Phương thức hoàn tiền</label>
        <el-select
          v-model="selectedPaymentMethod"
          placeholder="Chọn phương thức hoàn tiền"
          class="w-full"
        >
          <el-option label="ZaloPay" value="ZALO_PAY" />
          <el-option label="Ngân hàng khác" value="NGAN_HANG_KHAC" />
        </el-select>
        <div v-if="selectedPaymentMethod === 'NGAN_HANG_KHAC'" class="mt-3">
          <label class="font-medium">Tên ngân hàng</label>
          <el-input
            v-model="bankName"
            placeholder="Nhập tên ngân hàng"
            class="w-full"
          />
        </div>
        <label class="font-medium">Mã giao dịch</label>
        <el-input
          v-model="transactionCode"
          placeholder="Nhập mã giao dịch"
          class="w-full"
        />
      </div>

      <template #footer>
        <el-button @click="cancelDialogVisible = false">Hủy</el-button>
        <el-button type="danger" @click="cancelOrder">Xác nhận hủy đơn</el-button>
      </template>
    </el-dialog>

    <el-dialog title="Giao hàng thất bại" v-model="failDialogVisible" width="500px">
      <div class="mb-3">
        <label class="font-medium">Lý do giao hàng thất bại</label>
        <el-input
          type="textarea"
          v-model="failNote"
          placeholder="Nhập lý do giao hàng thất bại..."
          rows="3"
        />
      </div>

      <div v-if="isPaid">
        <label class="font-medium">Phương thức thanh toán</label>
        <el-select
          v-model="selectedPaymentMethod"
          placeholder="Chọn phương thức thanh toán"
          class="w-full"
        >
          <el-option label="Tiền mặt" value="TIEN_MAT" />
          <el-option label="ZaloPay" value="ZALO_PAY" />
        </el-select>
      </div>

      <template #footer>
        <el-button @click="failDialogVisible = false">Hủy</el-button>
        <el-button type="danger" @click="markAsFailedDelivery">Xác nhận</el-button>
      </template>
    </el-dialog>

    <el-dialog title="Lịch sử tác động đơn hàng" v-model="actionHistoryDialogVisible" width="800px">
      <el-table :data="actionHistory" border stripe>
        <el-table-column label="STT" width="70" type="index" />
        <el-table-column prop="oldStatus" label="Trạng thái cũ">
          <template #default="scope">
            {{ getStatusLabelFromInt(scope.row.oldStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="newStatus" label="Trạng thái mới">
          <template #default="scope">
            {{ getStatusLabelFromInt(scope.row.newStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="changedAt" label="Thời gian" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.changedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="note" label="Ghi chú" />
        <el-table-column prop="employeeName" label="Người thực hiện" />
      </el-table>
      <template #footer>
        <el-button @click="actionHistoryDialogVisible = false">Đóng</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiClient from '@/utils/axiosInstance'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const invoiceId = route.params.invoiceId
const invoice = ref({})
const cancelDialogVisible = ref(false)
const cancelNote = ref('')
const selectedPaymentMethod = ref('')

const failDialogVisible = ref(false)
const failNote = ref('')

const actionHistoryDialogVisible = ref(false)
const actionHistory = ref([])

const isPaid = ref(false)
const transactionCode = ref('')
const bankName = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref("")
const dialogValue = ref("")
const editingField = ref("")

const provinces = ref([])
const districts = ref([])
const wards = ref([])
const addressDialogVisible = ref(false)
const addressForm = ref({
  houseNumber: '',
  provinceCode: null,
  provinceName: '',
  districtCode: null,
  districtName: '',
  wardCode: null,
  wardName: '',
})

const GHN_TOKEN = '847c9bb7-6e42-11ee-a59f-a260851ba65c'

// --- Hàm xử lý chính ---
const fetchInvoice = async () => {
  try {
    const res = await apiClient.get(`/admin/online-sales/get-order`, {
      params: { invoiceId },
    })
    invoice.value = res.data
    isPaid.value = res.data.isPaid
  } catch (err) {
    ElMessage.error('Lỗi tải đơn hàng')
    console.error(err)
  }
}

const goBack = () => router.back()

// --- Chức năng chuyển đổi trạng thái đơn hàng ---
const mainSteps = [
  { key: 'CHO_XU_LY', label: 'Chờ xác nhận' },
  { key: 'DA_XU_LY', label: 'Đã xác nhận' },
  { key: 'CHO_GIAO_HANG', label: 'Chờ giao hàng' },
  { key: 'DANG_GIAO_HANG', label: 'Đang giao hàng' },
  { key: 'GIAO_THANH_CONG', label: 'Giao thành công' },
]
const mainStepKeys = mainSteps.map((s) => s.key)

const getActiveStep = (statusKey) => mainStepKeys.indexOf(statusKey || '')
const getStatusLabelFromInt = (statusInt) => {
  if (statusInt === -1) return 'Đã hủy'
  if (typeof statusInt === 'number' && statusInt >= 0 && statusInt < mainSteps.length) {
    return mainSteps[statusInt].label
  }
  return `Không xác định (${statusInt})`
}

const canAdvance = computed(() => {
  const status = invoice.value?.statusDetail
  return mainStepKeys.includes(status) && status !== 'GIAO_THANH_CONG'
})

const canRevert = computed(() => {
  const status = invoice.value?.statusDetail
  const idx = mainStepKeys.indexOf(status)
  return idx > 0 && idx < mainStepKeys.indexOf('DANG_GIAO_HANG') && mainStepKeys.includes(status)
})

const canCancel = computed(() => {
  const status = invoice.value?.statusDetail
  return mainStepKeys.includes(status) && mainStepKeys.indexOf(status) < mainStepKeys.indexOf('DANG_GIAO_HANG')
})

const confirmAdvance = () => {
  ElMessageBox.confirm('Bạn có chắc muốn chuyển sang trạng thái tiếp theo?', 'Xác nhận', {
    type: 'warning',
  }).then(advanceStatus)
}

const confirmRevert = () => {
  ElMessageBox.confirm('Bạn có chắc muốn quay lại trạng thái trước?', 'Xác nhận', {
    type: 'warning',
  }).then(revertStatus)
}

const advanceStatus = async () => {
  const currentKey = invoice.value?.statusDetail
  const currentIndex = mainStepKeys.indexOf(currentKey)
  const nextKey = mainStepKeys[currentIndex + 1]

  if (!nextKey) {
    ElMessage.info('Không có trạng thái tiếp theo để chuyển.')
    return
  }

  try {
    await apiClient.put(`/admin/online-sales/chuyen-trang-thai`, null, {
      params: { invoiceId, statusDetail: nextKey },
    })
    ElMessage.success('Chuyển trạng thái thành công!')
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi chuyển trạng thái')
    console.error(err)
  }
}

const revertStatus = async () => {
  const currentKey = invoice.value?.statusDetail
  const currentIndex = mainStepKeys.indexOf(currentKey)
  const prevKey = mainStepKeys[currentIndex - 1]

  if (!prevKey) {
    ElMessage.info('Không có trạng thái trước đó để quay lại.')
    return
  }

  try {
    await apiClient.put(`/admin/online-sales/chuyen-trang-thai`, null, {
      params: { invoiceId, statusDetail: prevKey },
    })
    ElMessage.success('Quay lại trạng thái trước thành công!')
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi quay lại trạng thái')
    console.error(err)
  }
}

// --- Chức năng hủy đơn hàng ---
const showCancelDialog = () => {
  cancelNote.value = ''
  selectedPaymentMethod.value = ''
  transactionCode.value = ''
  bankName.value = ''
  cancelDialogVisible.value = true
}

const cancelOrder = async () => {
  try {
    if (!cancelNote.value.trim()) {
      ElMessage.warning('Vui lòng nhập lý do hủy đơn!')
      return
    }
    if (isPaid.value && !selectedPaymentMethod.value) {
      ElMessage.warning('Vui lòng chọn phương thức hoàn tiền!')
      return
    }

    await apiClient.put(`/admin/online-sales/huy-don-va-hoan-tien`, null, {
      params: {
        invoiceId,
        statusDetail: 'HUY_DON',
        note: cancelNote.value,
        paymentMethod: selectedPaymentMethod.value || '',
        tradeCode: transactionCode.value || '',
        bankName: bankName.value || '',
        isPaid: isPaid.value
      },
    })

    ElMessage.success('Hủy đơn hàng và hoàn tiền thành công!')
    cancelDialogVisible.value = false
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi hủy đơn hàng')
    console.error(err)
  }
}

// --- Chức năng giao hàng thất bại ---
const showFailDialog = () => {
  failNote.value = ''
  selectedPaymentMethod.value = ''
  failDialogVisible.value = true
}

const markAsFailedDelivery = async () => {
  try {
    if (!failNote.value.trim()) {
      ElMessage.warning('Vui lòng nhập lý do giao hàng thất bại!')
      return
    }
    if (isPaid.value && !selectedPaymentMethod.value) {
      ElMessage.warning('Vui lòng chọn phương thức thanh toán!')
      return
    }

    await apiClient.put(`/admin/online-sales/failed-shipping`, null, {
      params: {
        invoiceId,
        statusDetail: 'GIAO_THAT_BAI',
        note: failNote.value,
        paymentMethod: isPaid.value ? selectedPaymentMethod.value : 'UNKNOWN',
      },
    })

    ElMessage.success('Cập nhật trạng thái giao hàng thất bại thành công!')
    failDialogVisible.value = false
    fetchInvoice()
  } catch (err) {
    ElMessage.error('Lỗi cập nhật trạng thái thất bại')
    console.error(err)
  }
}

// --- Lịch sử tác động ---
const showActionHistoryDialog = async () => {
  try {
    const res = await apiClient.get(`/admin/online-sales/get-order-history`, {
      params: { invoiceId },
    })
    actionHistory.value = res.data
    actionHistoryDialogVisible.value = true
  } catch (err) {
    ElMessage.error('Lỗi tải lịch sử tác động')
    console.error(err)
  }
}

// --- Cập nhật số điện thoại ---
const openDialog = (field) => {
  editingField.value = field
  dialogVisible.value = true
  dialogTitle.value = "Sửa số điện thoại"
  dialogValue.value = invoice.value.phone
}

const saveChange = async () => {
  if (editingField.value === "phone") {
    if (!dialogValue.value.trim()) {
      ElMessage.warning('Số điện thoại không được để trống!')
      return
    }
    try {
      await apiClient.put('/admin/online-sales/update-phone', null, {
        params: {
          invoiceId,
          newPhone: dialogValue.value
        }
      })
      ElMessage.success('Cập nhật số điện thoại thành công!')
      dialogVisible.value = false
      fetchInvoice()
    } catch (err) {
      ElMessage.error('Lỗi cập nhật số điện thoại!')
      console.error(err)
    }
  }
}

// --- Cập nhật địa chỉ giao hàng ---
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

const handleWardChangeForAddress = async  () => {
  const selected = wards.value.find(w => w.WardCode === addressForm.value.wardCode)
  addressForm.value.wardName = selected?.WardName || ''

  shippingFee.value = await calculateShippingFee();
}

const openAddressDialog = async () => {
  addressDialogVisible.value = true;
  await loadProvincesForAddress();

  const currentAddress = invoice.value.deliveryAddress || '';

  console.log('ahihih: ',currentAddress)
  const addressParts = currentAddress.split(' - ').map(part => part.trim());

if (addressParts.length >= 4) {
    addressForm.value.houseNumber = addressParts[0];
    const wardName = addressParts[1];
    const districtName = addressParts[2];
    const provinceName = addressParts[3];

    const foundProvince = provinces.value.find(p => p.ProvinceName === provinceName);
    if (foundProvince) {
      addressForm.value.provinceCode = foundProvince.ProvinceID;
      addressForm.value.provinceName = foundProvince.ProvinceName;
      await loadDistrictsForAddress();

      const foundDistrict = districts.value.find(d => d.DistrictName === districtName);
      if (foundDistrict) {
        addressForm.value.districtCode = foundDistrict.DistrictID;
        addressForm.value.districtName = foundDistrict.DistrictName;
        await loadWardsForAddress();

        const foundWard = wards.value.find(w => w.WardName === wardName);
        if (foundWard) {
          addressForm.value.wardCode = foundWard.WardCode;
          addressForm.value.wardName = foundWard.WardName;
        }
      }
    }
  }
};

const shippingFee = ref(null);
const calculateShippingFee = async () => {
  // Kiểm tra nếu chưa chọn Quận/Huyện hoặc Phường/Xã thì không tính phí
  if (!addressForm.value.districtCode || !addressForm.value.wardCode) {
    ElMessage.warning('Vui lòng chọn đầy đủ Tỉnh/Thành, Quận/Huyện, Phường/Xã');
    return 0; // Trả về 0 nếu thông tin chưa đủ
  }

  const FROM_DISTRICT_ID = 1483; // ID Quận/Huyện của kho hàng
  const FROM_WARD_CODE = "21108"; // Mã Phường/Xã của kho hàng
  const SHOP_ID = 5851480; // ID của shop bạn trên GHN
  const GHN_TOKEN = '741f1c91-4f42-11f0-8cf5-d2552bfd31d8';

  try {

    const totalWeight = invoice.value.invoiceDetailResponses.reduce(
      (sum, item) => sum + (item.weight || 200) * item.quantity, 0
    );

    // 2. Gọi API tính phí của GHN
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee',
      {
        from_district_id: FROM_DISTRICT_ID,
        from_ward_code: FROM_WARD_CODE,
        to_district_id: addressForm.value.districtCode,
        to_ward_code: addressForm.value.wardCode,
        weight: Math.max(totalWeight, 100), // GHN yêu cầu tối thiểu 100g
        insurance_value: invoice.value.totalAmount, // Giá trị đơn hàng
        service_type_id: 2, // 2: Chuyển phát nhanh (Fast Delivery)
      },
      {
        headers: {
          'Content-Type': 'application/json',
          Token: GHN_TOKEN,
          ShopId: SHOP_ID,
        },
      }
    );

    shippingFee.value = res.data.data.total || 0;
    ElMessage.success(`Phí vận chuyển dự kiến: ${formatCurrency(shippingFee.value)}`);
    return shippingFee;

  } catch (err) {
    console.error('❌ Lỗi tính phí ship:', err);
    ElMessage.error('Không thể tính phí vận chuyển. Vui lòng thử lại.');
    return 0; // Trả về 0 khi có lỗi
  }
};

const submitAddressUpdate = async () => {
  const { houseNumber, provinceName, districtName, wardName } = addressForm.value;

  if (!houseNumber || !provinceName || !districtName || !wardName) {
    ElMessage.warning('Vui lòng nhập đầy đủ thông tin địa chỉ.');
    return;
  }

  const fullAddress = `${houseNumber} - ${wardName} - ${districtName} - ${provinceName}`;
  const newShippingFee = await calculateShippingFee();
   const totalAmount = Number(invoice.value.totalAmount) || 0;
    const discountAmount = Number(invoice.value.discountAmount) || 0;
    const finalAmount = totalAmount + newShippingFee.value - discountAmount;

  try {
    await apiClient.put(`/admin/online-sales/update-address`, {
  invoiceId,
  newAddress: fullAddress,
  shippingFee: newShippingFee.value,
  finalAmount: finalAmount
});

    ElMessage.success('Cập nhật địa chỉ thành công!');
    addressDialogVisible.value = false;
    fetchInvoice();
  } catch (err) {
    ElMessage.error('Lỗi cập nhật địa chỉ!');
    console.error(err);
  }
};

// --- Tiện ích ---
const formatDate = (dateStr) => (dateStr ? new Date(dateStr).toLocaleString('vi-VN') : '')
const formatCurrency = (val) => `${Number(val || 0).toLocaleString('vi-VN')} ₫`

// --- Lifecycle Hook ---
onMounted(() => {
  fetchInvoice();
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
</style>

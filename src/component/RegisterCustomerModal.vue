<template>
  <el-dialog
    v-model="dialogVisible"
    title="Đăng ký tài khoản"
    width="800px"
    destroy-on-close
    :close-on-click-modal="false"
    class="register-customer-dialog"
  >
    <el-form
      :model="form"
      label-position="top"
      @submit.prevent="submitForm"
      class="customer-form"
      ref="customerFormRef"
      :rules="rules"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Họ tên" prop="customerName">
            <el-input v-model="form.customerName" placeholder="Nhập họ tên" :prefix-icon="User" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Tên đăng nhập" prop="username">
            <el-input v-model="form.username" placeholder="Tên đăng nhập" :prefix-icon="UserFilled" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Mật khẩu" prop="password">
            <el-input v-model="form.password" type="password" placeholder="Mật khẩu" :prefix-icon="Lock" show-password />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Số điện thoại" prop="phone">
            <el-input v-model="form.phone" placeholder="Số điện thoại" :prefix-icon="Phone" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Email" prop="email">
            <el-input v-model="form.email" type="email" placeholder="Email" :prefix-icon="Message" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Ngày sinh" prop="dateOfBirth">
            <el-date-picker
              v-model="form.dateOfBirth"
              type="date"
              placeholder="Chọn ngày sinh"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%;"
              :prefix-icon="Calendar"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Giới tính" prop="gender">
            <el-select v-model="form.gender" placeholder="Chọn giới tính" style="width: 100%;">
              <el-option label="Nam" :value="1" />
              <el-option label="Nữ" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Tỉnh/Thành phố" prop="provinceCode">
            <el-select v-model="form.provinceCode" placeholder="Chọn Tỉnh/Thành" @change="handleProvinceChange" filterable style="width: 100%;">
              <el-option
                v-for="item in provinces"
                :key="item.ProvinceID"
                :label="item.ProvinceName"
                :value="item.ProvinceID"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Quận/Huyện" prop="districtCode">
            <el-select
              v-model="form.districtCode"
              placeholder="Chọn Quận/Huyện"
              :disabled="!districts.length"
              @change="handleDistrictChange"
              filterable
              style="width: 100%;"
            >
              <el-option
                v-for="item in districts"
                :key="item.DistrictID"
                :label="item.DistrictName"
                :value="item.DistrictID"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Phường/Xã" prop="wardCode">
            <el-select
              v-model="form.wardCode"
              placeholder="Chọn Phường/Xã"
              :disabled="!wards.length"
              @change="handleWardChange"
              filterable
              style="width: 100%;"
            >
              <el-option
                v-for="item in wards"
                :key="item.WardCode"
                :label="item.WardName"
                :value="item.WardCode"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="Số nhà, tên đường" prop="houseName">
        <el-input v-model="form.houseName" placeholder="Số nhà, tên đường" :prefix-icon="HomeFilled" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer register-footer-custom">
        <div class="switch-mode">
          Đã có tài khoản?
          <el-button type="text" @click="openLoginModal" class="login-link">
            Đăng nhập ngay
          </el-button>
        </div>
        <div class="button-group">
          <el-button @click="resetForm" :icon="Refresh">Reset</el-button>
          <el-button type="primary" @click="submitForm" :icon="CirclePlus">
            Đăng ký
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { User, UserFilled, Lock, Phone, Message, Calendar, HomeFilled, CirclePlus, Refresh } from '@element-plus/icons-vue';
import axios from 'axios';

const props = defineProps({
  isVisible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:isVisible', 'customerRegistered', 'openLogin']);

const dialogVisible = ref(props.isVisible);
const customerFormRef = ref(null);

const form = ref({
  customerName: '',
  username: '',
  password: '',
  phone: '',
  dateOfBirth: null,
  country: 'Việt Nam',
  email: '',
  gender: 1, // 1: Nam, 0: Nữ
  provinceCode: null,
  provinceName: '',
  districtCode: null,
  districtName: '',
  wardCode: null,
  wardName: '',
  houseName: '',
});

const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

const GHN_TOKEN = '847c9bb7-6e42-11ee-a59f-a260851ba65c'; // Your GHN Token

// Watch for changes in prop.isVisible to update dialogVisible
watch(() => props.isVisible, (newVal) => {
  dialogVisible.value = newVal;
  if (newVal) {
    resetForm(); // Reset form when dialog opens
    loadProvinces(); // Load provinces when dialog opens
  }
});

// Watch for changes in dialogVisible to emit update event
watch(dialogVisible, (newVal) => {
  emit('update:isVisible', newVal);
});

const closeModal = () => {
  dialogVisible.value = false;
};

// Function to open the login modal
const openLoginModal = () => {
  closeModal(); // Close registration modal
  emit('openLogin'); // Emit event to parent component to open login modal
};

// Function to load provinces
const loadProvinces = async () => {
  try {
    const res = await axios.post(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/province',
      {},
      { headers: { Token: GHN_TOKEN } }
    );
    provinces.value = res.data.data;
  } catch (error) {
    console.error('Error loading provinces:', error);
    ElMessage.error('Failed to load provinces.');
  }
};

// Function to load districts
const loadDistricts = async () => {
  form.value.districtCode = null;
  form.value.districtName = '';
  form.value.wardCode = null;
  form.value.wardName = '';
  districts.value = [];
  wards.value = [];

  if (!form.value.provinceCode) return;

  try {
    const res = await axios.get(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/district',
      {
        headers: { Token: GHN_TOKEN },
        params: { province_id: form.value.provinceCode },
      }
    );
    districts.value = res.data.data;
  } catch (error) {
    console.error('Error loading districts:', error);
    ElMessage.error('Failed to load districts.');
  }
};

// Function to load wards
const loadWards = async () => {
  form.value.wardCode = null;
  form.value.wardName = '';
  wards.value = [];

  if (!form.value.districtCode) return;

  try {
    const res = await axios.get(
      'https://online-gateway.ghn.vn/shiip/public-api/master-data/ward',
      {
        headers: { Token: GHN_TOKEN },
        params: { district_id: form.value.districtCode },
      }
    );
    wards.value = res.data.data;
  } catch (error) {
    console.error('Error loading wards:', error);
    ElMessage.error('Failed to load wards.');
  }
};

const handleProvinceChange = () => {
  const selected = provinces.value.find(p => p.ProvinceID === form.value.provinceCode);
  form.value.provinceName = selected?.ProvinceName || '';
  loadDistricts();
};

const handleDistrictChange = () => {
  const selected = districts.value.find(d => d.DistrictID === form.value.districtCode);
  form.value.districtName = selected?.DistrictName || '';
  loadWards();
};

const handleWardChange = () => {
  const selected = wards.value.find(w => w.WardCode === form.value.wardCode);
  form.value.wardName = selected?.WardName || '';
};

// Submit form (register account)
const submitForm = async () => {
  try {
    await customerFormRef.value.validate();

    let formattedDateOfBirth = null;
    if (form.value.dateOfBirth instanceof Date) {
      const year = form.value.dateOfBirth.getFullYear();
      const month = String(form.value.dateOfBirth.getMonth() + 1).padStart(2, '0');
      const day = String(form.value.dateOfBirth.getDate()).padStart(2, '0');
      formattedDateOfBirth = `${year}-${month}-${day}`;
    } else if (typeof form.value.dateOfBirth === 'string' && form.value.dateOfBirth) {
      formattedDateOfBirth = form.value.dateOfBirth;
    }

    const customerData = {
      customerName: form.value.customerName,
      username: form.value.username,
      password: form.value.password,
      phone: form.value.phone,
      email: form.value.email,
      dateOfBirth: formattedDateOfBirth,
      gender: form.value.gender,
      provinceCode: form.value.provinceCode || null,
      provinceName: form.value.provinceName || '',
      districtCode: form.value.districtCode || null,
      districtName: form.value.districtName || '',
      wardCode: form.value.wardCode || '',
      wardName: form.value.wardName || '',
      houseName: form.value.houseName || '',
      country: form.value.country || 'Việt Nam',
    };

    console.log("Registration data submitted:", customerData);

    // Change endpoint if this is for customer registration API
    const res = await axios.post('http://localhost:8080/api/auth/register', customerData); // Example endpoint
    ElMessage.success('Account registered successfully!');
    console.log('Account registered:', res.data);
    emit('customerRegistered', { username: form.value.username }); // Emit event on successful registration
    closeModal();
  } catch (error) {
    console.error('Error during account registration:', error);
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(`Error: ${error.response.data.message}`);
    } else if (error.message && error.message.includes('Network Error')) {
      ElMessage.error('Cannot connect to server. Please check your network connection or try again later.');
    } else {
      ElMessage.error('An error occurred during account registration. Please check your information.');
    }
  }
};

const resetForm = () => {
  if (customerFormRef.value) {
    customerFormRef.value.resetFields();
  }
  form.value.provinceCode = null;
  form.value.provinceName = '';
  form.value.districtCode = null;
  form.value.districtName = '';
  form.value.wardCode = null;
  form.value.wardName = '';
  form.value.houseName = '';
  districts.value = [];
  wards.value = [];
};

// Rules for validation
const rules = ref({
  customerName: [{ required: true, message: 'Vui lòng nhập họ tên của bạn', trigger: 'blur' }],
  username: [
    { required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' },
    { min: 3, message: 'Tên đăng nhập phải có ít nhất 3 ký tự', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' },
    { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' },
    { pattern: /^(0|\+84)[3|5|7|8|9][0-9]{8}$/, message: 'Số điện thoại không hợp lệ', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
    { type: 'email', message: 'Email không đúng định dạng', trigger: ['blur', 'change'] }
  ],
  dateOfBirth: [{ required: true, message: 'Vui lòng chọn ngày sinh', trigger: 'change' }],
  gender: [{ required: true, message: 'Vui lòng chọn giới tính', trigger: 'change' }],
  provinceCode: [{ required: true, message: 'Vui lòng chọn Tỉnh/Thành phố', trigger: 'change' }],
  districtCode: [{ required: true, message: 'Vui lòng chọn Quận/Huyện', trigger: 'change' }],
  wardCode: [{ required: true, message: 'Vui lòng chọn Phường/Xã', trigger: 'change' }],
  houseName: [{ required: true, message: 'Vui lòng nhập số nhà, tên đường', trigger: 'blur' }]
});
</script>

<style>
/* Global Element Plus overrides and custom styles for a modern, clean look */
.register-customer-dialog {
  font-family: 'Roboto', 'Helvetica Neue', Helvetica, Arial, sans-serif; /* Modern, clean font for consistency */
}

.register-customer-dialog .el-dialog {
  border-radius: 12px; /* Softer, more modern rounded corners for the dialog */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1); /* Subtle, diffused shadow for depth */
}

.register-customer-dialog .el-dialog__header {
  border-bottom: 1px solid #e0e0e0; /* Lighter, subtle border for separation */
  padding: 25px 30px 20px; /* Adjusted padding for better visual balance */
  margin-right: 0; /* Ensures the close button aligns correctly */
  position: relative;
}

.register-customer-dialog .el-dialog__title {
  font-size: 28px; /* Larger, more prominent title */
  font-weight: 700; /* Bolder title for emphasis */
  color: #212121; /* Darker, more striking title color */
  text-align: center;
  width: 100%;
}

.register-customer-dialog .el-dialog__body {
  padding: 30px; /* Consistent padding around the form content */
}

.register-customer-dialog .el-form-item {
  margin-bottom: 24px; /* Slightly more space between form items for readability */
}

.register-customer-dialog .el-form-item__label {
  font-weight: 600; /* Semi-bold labels */
  color: #424242; /* Darker grey for better contrast and readability */
  margin-bottom: 8px; /* More space between label and input field */
  font-size: 15px; /* Slightly larger label font */
}

.register-customer-dialog .el-input,
.register-customer-dialog .el-select,
.register-customer-dialog .el-date-editor {
  width: 100%; /* Ensures all input-like components take full width */
}

.register-customer-dialog .el-input__wrapper,
.register-customer-dialog .el-select__wrapper,
.register-customer-dialog .el-date-editor .el-input__wrapper {
  height: 44px; /* Taller input fields for a more substantial feel */
  line-height: 44px;
  border-radius: 8px; /* More rounded input corners */
  box-shadow: none !important; /* Removes Element Plus default inner shadow */
  background-color: #f5f5f5; /* Light grey background for input fields */
  border: 1px solid #e0e0e0; /* Subtle border for definition */
}

.register-customer-dialog .el-input__inner,
.register-customer-dialog .el-select__placeholder {
  color: #616161; /* Consistent color for input text and placeholders */
}

.register-customer-dialog .el-input__inner:focus,
.register-customer-dialog .el-select__wrapper.is-focused,
.register-customer-dialog .el-date-editor.el-input__wrapper.is-focused {
  border-color: #1976D2; /* Deeper blue border on focus */
  background-color: #ffffff; /* White background on focus for clarity */
}

.register-customer-dialog .el-input__prefix {
  color: #757575; /* Subtle grey for input icons */
  font-size: 16px;
  padding-right: 8px; /* Space between icon and input text */
}

.register-customer-dialog .el-dialog__footer {
  border-top: 1px solid #e0e0e0; /* Lighter border at the top of the footer */
  padding: 20px 30px 25px; /* Adjusted padding for better spacing */
  display: flex;
  justify-content: space-between; /* Aligns "Already have account" to left, buttons to right */
  align-items: center;
}

.register-customer-dialog .dialog-footer.register-footer-custom {
  width: 100%; /* Ensures the footer content spans the full width */
}

.register-customer-dialog .dialog-footer .button-group {
  display: flex;
  gap: 15px; /* Increased spacing between buttons */
}

.register-customer-dialog .dialog-footer .el-button {
  min-width: 130px; /* Ensures buttons have a consistent, minimum width */
  font-size: 16px;
  font-weight: 600; /* Semi-bold button text */
  border-radius: 8px; /* More rounded button corners */
  padding: 12px 20px; /* More generous vertical padding for touch targets */
  transition: all 0.3s ease; /* Smooth transitions for hover effects */
}

.register-customer-dialog .dialog-footer .el-button--primary {
  background-color: #1976D2; /* Deep blue primary button color */
  border-color: #1976D2;
  color: #ffffff; /* White text on primary button */
}
.register-customer-dialog .dialog-footer .el-button--primary:hover {
  background-color: #1565C0; /* Darker blue on hover */
  border-color: #1565C0;
  box-shadow: 0 4px 10px rgba(25, 118, 210, 0.2); /* Subtle shadow on hover for depth */
}

.register-customer-dialog .dialog-footer .el-button--default {
  color: #424242; /* Dark grey text for default button */
  border-color: #bdbdbd; /* Softer border for default button */
  background-color: #ffffff;
}
.register-customer-dialog .dialog-footer .el-button--default:hover {
  color: #1976D2; /* Primary blue text on hover */
  border-color: #1976D2; /* Primary blue border on hover */
  background-color: #E3F2FD; /* Light blue background on hover */
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05); /* Subtle shadow on hover */
}

.register-customer-dialog .switch-mode {
  color: #616161; /* Medium grey for the "already have account" text */
  font-size: 15px; /* Slightly larger font size */
  display: flex;
  align-items: center;
  gap: 8px; /* Space between text and link button */
}

.register-customer-dialog .switch-mode .login-link {
  color: #1976D2; /* Deep blue link color */
  font-weight: 700; /* Bolder link text */
  text-decoration: none; /* No underline by default */
  padding: 0;
  height: auto;
  line-height: inherit;
}

.register-customer-dialog .switch-mode .login-link:hover {
  text-decoration: underline; /* Underline on hover for interactivity */
  color: #1565C0; /* Darker blue on hover */
}
</style>
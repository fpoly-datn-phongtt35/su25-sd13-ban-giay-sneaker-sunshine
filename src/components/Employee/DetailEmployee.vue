<template>
  <div class="container mt-5">
    <!-- Nút quay lại -->
    <div class="mb-4">
      <button class="btn btn-outline-secondary" @click="goBackEmployee">
        <i class="bi bi-arrow-left"></i> Quay lại
      </button>
    </div>

    <!-- Card chứa toàn bộ form -->
    <div class="card shadow-lg border-0">
      <div class="card-header bg-primary text-white text-center">
        <h3 class="fw-bold">Chi Tiết Thông Tin Nhân Viên</h3>
      </div>
      <div class="card-body p-4">
        <div class="row g-4">
          <!-- Tên Nhân Viên -->
          <div class="col-md-6">
            <label for="employeeName" class="form-label fw-semibold">Tên Nhân Viên</label>
            <input
              type="text"
              class="form-control"
              id="employeeName"
              placeholder="Nhập Tên Nhân Viên"
              v-model="UpdateEmployee.employeeName"
              readonly
            />
          </div>
          <!-- Chức vụ -->
          <div class="col-md-6">
            <label for="roleId" class="form-label fw-semibold">Chức vụ</label>
            <select id="roleId" class="form-select" v-model="UpdateEmployee.roleId" disabled>
              <option value="" disabled>Chọn Chức Vụ</option>
              <option v-for="rl in roleList" :key="rl.id" :value="rl.id">
                {{ rl.roleName }}
              </option>
            </select>
          </div>
          <!-- Ngày sinh -->
          <div class="col-md-6">
            <label for="dateOfBirth" class="form-label fw-semibold">Ngày Sinh</label>
            <input
              type="date"
              class="form-control"
              id="dateOfBirth"
              v-model="UpdateEmployee.dateOfBirth"
              readonly
            />
          </div>
          <!-- Giới tính -->
          <div class="col-md-6">
            <label class="form-label fw-semibold">Giới Tính</label>
            <div class="d-flex gap-3 mt-2">
              <div class="form-check">
                <input
                  type="radio"
                  class="form-check-input"
                  name="gender"
                  value="1"
                  id="genderMale"
                  v-model="UpdateEmployee.gender"
                  disabled
                />
                <label class="form-check-label" for="genderMale">Nam</label>
              </div>
              <div class="form-check">
                <input
                  type="radio"
                  class="form-check-input"
                  name="gender"
                  value="0"
                  id="genderFemale"
                  v-model="UpdateEmployee.gender"
                  disabled
                />
                <label class="form-check-label" for="genderFemale">Nữ</label>
              </div>
            </div>
          </div>
          <!-- Số điện thoại -->
          <div class="col-md-6">
            <label for="phone" class="form-label fw-semibold">Số Điện Thoại</label>
            <input
              type="text"
              class="form-control"
              id="phone"
              placeholder="Nhập số điện thoại"
              v-model="UpdateEmployee.phone"
              readonly
            />
          </div>
          <!-- Email -->
          <div class="col-md-6">
            <label for="email" class="form-label fw-semibold">Email</label>
            <input
              type="email"
              class="form-control"
              id="email"
              placeholder="Nhập email"
              v-model="UpdateEmployee.email"
              readonly
            />
          </div>
          <!-- Lương -->
          <div class="col-md-6">
            <label for="salary" class="form-label fw-semibold">Lương</label>
            <input
              type="text"
              class="form-control"
              id="salary"
              placeholder="Nhập Lương"
              v-model="UpdateEmployee.salary"
              readonly
            />
          </div>
          <!-- Ngày vào làm -->
          <div class="col-md-6">
            <label for="hireDate" class="form-label fw-semibold">Ngày vào làm</label>
            <input
              type="date"
              class="form-control"
              id="hireDate"
              v-model="UpdateEmployee.hireDate"
              readonly
            />
          </div>
          <!-- Mật khẩu -->
          <div class="col-md-12">
            <label for="password" class="form-label fw-semibold">Mật khẩu</label>
            <input
              type="text"
              class="form-control"
              id="password"
              placeholder="Nhập mật khẩu"
              v-model="UpdateEmployee.passWord"
              readonly
            />
          </div>
          <!-- Địa chỉ -->
          <div class="col-md-6">
            <label for="province" class="form-label fw-semibold">Tỉnh/Thành Phố</label>
            <input
              type="text"
              class="form-control"
              id="province"
              placeholder="Nhập tỉnh/thành phố"
              v-model="UpdateEmployee.province"
              readonly
            />
          </div>
          <div class="col-md-6">
            <label for="district" class="form-label fw-semibold">Quận/Huyện</label>
            <input
              type="text"
              class="form-control"
              id="district"
              placeholder="Nhập quận/huyện"
              v-model="UpdateEmployee.district"
              readonly
            />
          </div>
          <div class="col-md-6">
            <label for="ward" class="form-label fw-semibold">Phường/Xã</label>
            <input
              type="text"
              class="form-control"
              id="ward"
              placeholder="Nhập phường/xã"
              v-model="UpdateEmployee.ward"
              readonly
            />
          </div>
          <div class="col-md-6">
            <label for="houseName" class="form-label fw-semibold">Địa chỉ cụ thể</label>
            <input
              type="text"
              class="form-control"
              id="houseName"
              placeholder="Nhập địa chỉ cụ thể"
              v-model="UpdateEmployee.houseName"
              readonly
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios';
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();
const roleList = ref([]);
const UpdateEmployee = ref({});

const getEmployeeById = async () => {
  try {
    const { id } = route.params;
    const response = await axios.get(`http://localhost:8080/api/admin/employee/${id}`);
    UpdateEmployee.value = {
      ...response.data,
      dateOfBirth: response.data.dateOfBirth
        ? new Date(response.data.dateOfBirth).toISOString().split('T')[0]
        : null,
      hireDate: response.data.hireDate
        ? new Date(response.data.hireDate).toISOString().split('T')[0]
        : null,
    };
  } catch (error) {
    console.error('Lỗi khi lấy thông tin nhân viên:', error);
  }
};

const fetchRoleList = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/role');
    roleList.value = response.data;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách chức vụ:', error);
  }
};

onMounted(() => {
  fetchRoleList();
  getEmployeeById();
});

const goBackEmployee = () => {
  router.push('/employee');
};
</script>

<style scoped>
label {
  font-weight: 600;
}
</style>

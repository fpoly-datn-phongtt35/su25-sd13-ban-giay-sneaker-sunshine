<template>
    <div class="container mt-5">
      <!-- Back Button -->
      <div class="mb-4">
        <button class="btn btn-outline-secondary" @click="goBackEmployee">
          <i class="bi bi-arrow-left"></i> Quay lại
        </button>
      </div>
  
      <!-- Card Container -->
      <div class="card shadow border-0">
        <div class="card-header bg-primary text-white text-center">
          <h3 class="fw-bold">Sửa Thông Tin Nhân Viên</h3>
        </div>
  
        <div class="card-body p-4">
          <form @submit.prevent="openConfirmDialog">
            <div class="row g-4">
              <!-- Employee Name -->
              <div class="col-md-6">
                <label for="employeeName" class="form-label fw-semibold">Tên Nhân Viên</label>
                <input
                  type="text"
                  class="form-control"
                  id="employeeName"
                  placeholder="Nhập Tên Nhân Viên"
                  v-model="UpdateEmployee.employeeName"
                />
              </div>
  
              <!-- Role -->
              <div class="col-md-6">
                <label for="roleId" class="form-label fw-semibold">Chức vụ</label>
                <select id="roleId" class="form-select" v-model="UpdateEmployee.roleId">
                  <option value="" disabled>Chọn Chức Vụ</option>
                  <option v-for="rl in roleList" :key="rl.id" :value="rl.id">
                    {{ rl.roleName }}
                  </option>
                </select>
                <small class="form-text text-muted">Vui lòng chọn chức vụ phù hợp.</small>
              </div>
  
              <!-- Date of Birth -->
              <div class="col-md-6">
                <label for="dateOfBirth" class="form-label fw-semibold">Ngày Sinh</label>
                <input
                  type="date"
                  class="form-control"
                  id="dateOfBirth"
                  v-model="UpdateEmployee.dateOfBirth"
                />
              </div>
  
              <!-- Gender -->
              <div class="col-md-6">
                <label class="form-label fw-semibold">Giới Tính</label>
                <div class="d-flex align-items-center gap-3 mt-2">
                  <div class="form-check">
                    <input
                      type="radio"
                      class="form-check-input"
                      name="gender"
                      value="1"
                      id="genderMale"
                      v-model="UpdateEmployee.gender"
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
                    />
                    <label class="form-check-label" for="genderFemale">Nữ</label>
                  </div>
                </div>
              </div>
  
              <!-- Address Fields -->
              <div class="col-md-6">
                <label for="province" class="form-label fw-semibold">Tỉnh/Thành Phố</label>
                <input type="text" class="form-control" id="province" placeholder="Nhập tỉnh/thành phố" v-model="UpdateEmployee.province" />
              </div>
  
              <div class="col-md-6">
                <label for="district" class="form-label fw-semibold">Quận/Huyện</label>
                <input type="text" class="form-control" id="district" placeholder="Nhập quận/huyện" v-model="UpdateEmployee.district" />
              </div>
  
              <div class="col-md-6">
                <label for="ward" class="form-label fw-semibold">Phường/Xã</label>
                <input type="text" class="form-control" id="ward" placeholder="Nhập phường/xã" v-model="UpdateEmployee.ward" />
              </div>
  
              <div class="col-md-6">
                <label for="houseName" class="form-label fw-semibold">Địa chỉ cụ thể</label>
                <input
                  type="text"
                  class="form-control"
                  id="houseName"
                  placeholder="Nhập địa chỉ cụ thể (bao gồm: số nhà, ngõ, ngách, đường)"
                  v-model="UpdateEmployee.houseName"
                />
              </div>
  
              <!-- Contact Details -->
              <div class="col-md-6">
                <label for="phone" class="form-label fw-semibold">Số Điện Thoại</label>
                <input
                  type="text"
                  class="form-control"
                  id="phone"
                  placeholder="Nhập số điện thoại"
                  v-model="UpdateEmployee.phone"
                />
              </div>
  
              <div class="col-md-6">
                <label for="email" class="form-label fw-semibold">Email</label>
                <input
                  type="email"
                  class="form-control"
                  id="email"
                  placeholder="Nhập email"
                  v-model="UpdateEmployee.email"
                />
              </div>
  
              <!-- Salary and Hire Date -->
              <div class="col-md-6">
                <label for="salary" class="form-label fw-semibold">Lương</label>
                <input
                  type="text"
                  class="form-control"
                  id="salary"
                  placeholder="Nhập Lương"
                  v-model="UpdateEmployee.salary"
                />
              </div>
  
              <div class="col-md-6">
                <label for="hireDate" class="form-label fw-semibold">Ngày vào làm</label>
                <input
                  type="date"
                  class="form-control"
                  id="hireDate"
                  v-model="UpdateEmployee.hireDate"
                />
              </div>
  
              <!-- Password -->
              <div class="col-md-12">
                <label for="password" class="form-label fw-semibold">Mật khẩu</label>
                <input
                  type="text"
                  class="form-control"
                  id="password"
                  placeholder="Nhập mật khẩu"
                  v-model="UpdateEmployee.passWord"
                />
              </div>
  
              <!-- Submit Button -->
              <div class="col-12 text-center">
                <button type="submit" class="btn btn-primary btn-lg px-5">Cập nhật</button>
              </div>
            </div>
          </form>
  
          <!-- Error & Success Messages -->
          <div v-if="err" class="alert alert-danger mt-4">
            {{ err }}
          </div>
          <div v-if="success" class="alert alert-success mt-4">
            {{ success }}
          </div>
        </div>
      </div>
  
      <!-- Confirmation Modal -->
      <div
        v-if="isModalVisible"
        class="modal fade show d-block"
        tabindex="-1"
        role="dialog"
        aria-labelledby="confirmDialogTitle"
        aria-hidden="true"
      >
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="confirmDialogTitle">Xác nhận</h5>
              <button type="button" class="btn-close" @click="closeModal"></button>
            </div>
            <div class="modal-body">
              <p>Bạn có chắc chắn muốn lưu nhân viên này không?</p>
            </div>
            <div class="modal-footer">
              <button @click="editingEmployee" class="btn btn-success">Xác nhận</button>
              <button @click="closeModal" class="btn btn-danger">Hủy</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import axios from 'axios';
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  
  const router = useRouter();
  const route = useRoute();
  const roleList = ref([]);
  const success = ref(null);
  const err = ref(null);
  const isModalVisible = ref(false);
  const UpdateEmployee = ref({});
  
  const openConfirmDialog = () => {
    isModalVisible.value = true;
  };
  
  const closeModal = () => {
    isModalVisible.value = false;
  };
  
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
      err.value = 'Lỗi khi lấy thông tin nhân viên';
    }
  };
  
  const editingEmployee = async () => {
    err.value = null;
    success.value = null;
  
    const { id } = route.params;
  
    try {
      await axios.put(`http://localhost:8080/api/admin/employee/${id}`, UpdateEmployee.value);
      success.value = 'Sửa nhân viên thành công';
      setTimeout(() => {
        router.push('/employee');
      }, 2000);
    } catch (error) {
      err.value = error.response?.data?.err || 'Đã xảy ra lỗi khi sửa nhân viên';
    } finally {
      closeModal();
    }
  };
  
  const fetchRoleList = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/admin/role');
      roleList.value = response.data;
    } catch (error) {
      console.error('Lỗi khi lấy danh sách chức vụ', error);
    }
  };
  
  onMounted(() => {
    fetchRoleList();
    getEmployeeById();
  });
  
  const goBackEmployee = () => {
    router.push(`/employee`);
  };
  </script>
  
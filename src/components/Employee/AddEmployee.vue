<template>
  <div class="container mt-5">
    <h1 class="mb-4 text-center text-primary">Thêm Nhân Viên</h1>
    <button type="button" class="btn btn-secondary mb-4" @click="goBackEmployee">
      <i class="fas fa-arrow-left me-2"></i>Quay lại
    </button>
    <form @submit.prevent="openConfirmDialog" class="border p-4 rounded shadow-lg bg-light">
      <!-- Hàng 1: Tên nhân viên và Chức vụ -->
      <div class="row mb-3">
        <div class="col-md-6">
          <label for="employeeName" class="form-label">Tên Nhân Viên</label>
          <input
            type="text"
            class="form-control"
            id="employeeName"
            placeholder="Nhập Tên Nhân Viên"
            v-model="newEmployee.employeeName"
          />
        </div>
        <div class="col-md-6">
          <label for="roleId" class="form-label">Chọn Chức Vụ</label>
          <select id="roleId" class="form-select" v-model="newEmployee.roleId">
            <option value="" disabled>Chọn chức vụ</option>
            <option v-for="rl in roleList" :key="rl.id" :value="rl.id">
              {{ rl.roleName }}
            </option>
          </select>
        </div>
      </div>

      <!-- Hàng 2: Ngày sinh và Giới tính -->
      <div class="row mb-3">
        <div class="col-md-6">
          <label for="birthDate" class="form-label">Ngày Sinh</label>
          <input type="date" class="form-control" id="birthDate" v-model="newEmployee.dateOfBirth" />
        </div>
        <div class="col-md-6">
          <label class="form-label">Giới Tính</label>
          <div class="d-flex align-items-center">
            <div class="form-check me-3">
              <input
                type="radio"
                id="male"
                name="gender"
                value="1"
                class="form-check-input"
                v-model="newEmployee.gender"
              />
              <label for="male" class="form-check-label">Nam</label>
            </div>
            <div class="form-check">
              <input
                type="radio"
                id="female"
                name="gender"
                value="0"
                class="form-check-input"
                v-model="newEmployee.gender"
              />
              <label for="female" class="form-check-label">Nữ</label>
            </div>
          </div>
        </div>
      </div>

      <!-- Hàng 3: Địa chỉ -->
      <div class="row mb-3">
        <div class="col-md-3">
          <label for="province" class="form-label">Tỉnh/Thành Phố</label>
          <input type="text" class="form-control" id="province" placeholder="Nhập tỉnh" v-model="newEmployee.province" />
        </div>
        <div class="col-md-3">
          <label for="district" class="form-label">Quận/Huyện</label>
          <input type="text" class="form-control" id="district" placeholder="Nhập quận" v-model="newEmployee.district" />
        </div>
        <div class="col-md-3">
          <label for="ward" class="form-label">Phường/Xã</label>
          <input type="text" class="form-control" id="ward" placeholder="Nhập phường" v-model="newEmployee.ward" />
        </div>
        <div class="col-md-3">
          <label for="houseName" class="form-label">Địa Chỉ Cụ Thể</label>
          <input
            type="text"
            class="form-control"
            id="houseName"
            placeholder="Nhập địa chỉ cụ thể"
            v-model="newEmployee.houseName"
          />
        </div>
      </div>

      <!-- Hàng 4: Số điện thoại, Email -->
      <div class="row mb-3">
        <div class="col-md-6">
          <label for="phone" class="form-label">Số Điện Thoại</label>
          <input type="text" class="form-control" id="phone" placeholder="Nhập số điện thoại" v-model="newEmployee.phone" />
        </div>
        <div class="col-md-6">
          <label for="email" class="form-label">Email</label>
          <input type="email" class="form-control" id="email" placeholder="Nhập email" v-model="newEmployee.email" />
        </div>
      </div>

      <!-- Hàng 5: Lương và Ngày vào làm -->
      <div class="row mb-3">
        <div class="col-md-6">
          <label for="salary" class="form-label">Lương</label>
          <input type="number" class="form-control" id="salary" placeholder="Nhập lương" v-model="newEmployee.salary" />
        </div>
        <div class="col-md-6">
          <label for="hireDate" class="form-label">Ngày Vào Làm</label>
          <input type="date" class="form-control" id="hireDate" v-model="newEmployee.hireDate" />
        </div>
      </div>

      <!-- Hàng 6: Mật khẩu -->
      <div class="mb-3">
        <label for="password" class="form-label">Mật Khẩu</label>
        <input
          type="password"
          class="form-control"
          id="password"
          placeholder="Nhập mật khẩu"
          v-model="newEmployee.passWord"
        />
      </div>

      <!-- Nút Lưu -->
      <div class="d-flex justify-content-between">
        <button type="submit" class="btn btn-primary px-4">Lưu</button>
      </div>
    </form>

    <!-- Thông báo -->
    <div v-if="err" class="alert alert-danger alert-dismissible mt-3">
      {{ err }}
      <button type="button" class="btn-close" @click="err = null"></button>
    </div>
    <div v-if="success" class="alert alert-success alert-dismissible mt-3">
      {{ success }}
      <button type="button" class="btn-close" @click="success = null"></button>
    </div>

    <!-- Modal -->
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
            <h5 class="modal-title">Xác nhận</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <p>Bạn có chắc chắn muốn lưu nhân viên này không?</p>
          </div>
          <div class="modal-footer">
            <button @click="createEmployee" class="btn btn-success">Xác nhận</button>
            <button @click="closeModal" class="btn btn-danger">Hủy</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const roleList = ref([])

const success = ref(null)
const err = ref(null)
const isModalVisible = ref(false)

const newEmployee = ref({
  roleId: null,
  employeeName: '',
  dateOfBirth: '',
  gender: null,
  phone: '',
  email: '',
  salary: null,
  hireDate: '',
  passWord: '',
  province: '',
  district: '',
  ward: '',
  houseName: '',
})

const openConfirmDialog = () => {
  isModalVisible.value = true
}

const closeModal = () => {
  isModalVisible.value = false
}

const createEmployee = async () => {
  err.value = null
  success.value = null
  try {
    await axios.post('http://localhost:8080/api/admin/employee', newEmployee.value)
    success.value = 'Thêm Nhân Viên Thành Công'
    newEmployee.value = {
      roleId: null,
      employeeName: '',
      dateOfBirth: '',
      gender: null,
      phone: '',
      email: '',
      salary: null,
      hireDate: '',
      passWord: '',
      province: '',
      district: '',
      ward: '',
      houseName: '',
    }
    setTimeout(() => {
      router.push('/employee')
    }, 2000)
  } catch (error) {
    err.value = error.response?.data?.error || 'Đã xảy ra lỗi khi thêm nhân viên'
  } finally {
    closeModal()
  }
}

const goBackEmployee = () => {
  router.push('/employee')
}

const fetchRoleList = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/role')
    roleList.value = response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách quyền hạn', error)
  }
}

onMounted(() => {
  fetchRoleList()
})
</script>

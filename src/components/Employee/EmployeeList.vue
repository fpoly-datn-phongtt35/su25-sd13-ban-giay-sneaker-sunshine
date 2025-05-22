<template>
  <div class="container-fluid mt-4">
    <h1>Quản Lí Nhân Viên</h1>

    <!-- Nút Thêm Mới -->
    <div class="d-flex justify-content-end mb-3">
      <button class="btn btn-primary" @click="goToAdd">Thêm Mới</button>
    </div>

    <!-- Bảng Danh Sách Nhân Viên -->
    <div class="table-responsive">
      <table class="table table-bordered table-striped table-sm">
        <thead class="table-dark text-center">
          <tr>
            <th style="width: 50px">#</th>
            <th>Mã NV</th>
            <th>Tên NV</th>
            <th>Chức Vụ</th>
            <th>Ngày Sinh</th>
            <th>Giới Tính</th>
            <th>SĐT</th>
            <th>Email</th>
            <th>Ngày Vào Làm</th>
            <th>Ngày Tạo</th>
            <th>Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(empl, index) in employee" :key="empl.id" class="text-center align-middle">
            <td>{{ index + 1 + page * size }}</td>
            <td>{{ empl.employeeCode }}</td>
            <td>{{ empl.employeeName }}</td>
            <td>{{ empl.roleName }}</td>
            <td>{{ formatDate(empl.dateOfBirth) }}</td>
            <td>{{ empl.gender == 1 ? 'Nam' : 'Nữ' }}</td>
            <td>{{ empl.phone }}</td>
            <td>{{ empl.email }}</td>
            <td>{{ formatDate(empl.hireDate) }}</td>
            <td>{{ empl.dateCreated }}</td>
            <td>
              <button class="btn btn-danger btn-sm me-2" @click="openDeleteModal(empl.id)">
                <i class="fas fa-trash-alt"></i>
              </button>
              <button class="btn btn-info btn-sm me-2" @click="goToUpdate(empl.id)">
                <i class="fas fa-edit"></i>
              </button>
              <button class="btn btn-secondary btn-sm" @click="goToDetail(empl.id)">
                <i class="fas fa-info-circle"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Điều Hướng Trang -->
    <div class="d-flex justify-content-between align-items-center mt-3">
      <p class="mb-0">Trang Hiện Tại: {{ page + 1 }} / {{ totalPages }}</p>
      <div>
        <button class="btn btn-secondary me-2" :disabled="page === 0" @click="previousPage">
          Trước
        </button>
        <button class="btn btn-secondary" :disabled="page >= totalPages - 1" @click="nextPage">
          Tiếp
        </button>
      </div>
    </div>
  </div>

      <!-- Modal xác nhận xóa -->
      <div v-if="showModal" class="modal fade show" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true" style="display: block;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="deleteModalLabel">Xác nhận xóa</h5>
            <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <p>Bạn có chắc chắn muốn xóa nhân viên này không?</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
            <button type="button" class="btn btn-danger" @click="deleteEmployee">Xóa</button>
          </div>
        </div>
      </div>
    </div>

</template>

<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const employee = ref([])
const page = ref(0)
const size = ref(5)
const totalPages = ref(0)

const router = useRouter();

const showModal = ref(false);
const employeeToDelete = ref(null);

const openDeleteModal = (id) => {
  employeeToDelete.value = id;
  showModal.value = true;
}

const closeModal = () => {
  showModal.value = false;
}

const deleteEmployee = async() => {
  try {
    await axios.put(`http://localhost:8080/api/admin/employee/soft-delete/${employeeToDelete.value}`);
    closeModal();
    fetchEmployee();
  } catch (error) {
    console.error('Lỗi khi xóa nhân viên',error);
  }
}

const goToUpdate = (id) => {
  router.push({ name: 'UpdateEmployee', params: { id } })
}

const goToAdd = () => {
  router.push(`/employee/add`)
}

const goToDetail = (id) => {
  router.push({name: 'DetailEmployee',params: {id}})
}

const fetchEmployee = async () => {
  console.log("Fetching data for page:", page.value); 
  try {
    const response = await axios.get(`http://localhost:8080/api/admin/employee`, {
      params: {
        page: page.value,
        size: size.value,
      },
    })
    employee.value = response.data.content
    totalPages.value = response.data.totalPages
  } catch (error) {
    console.error('Có lỗi xảy ra khi tải danh sách nhân viên: ', error)
  }
}

const previousPage = () => {
  if (page.value > 0) {
    page.value--
    fetchEmployee()
  }
}

const nextPage = () => {
  if (page.value < totalPages.value - 1) {
    page.value++
    fetchEmployee()
  }
}

onMounted(() => {
  fetchEmployee()
})

const formatDate = (dateString) => {
  const options = { year: 'numeric', month: '2-digit', day: '2-digit' };
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', options);  // Định dạng theo kiểu ngày tháng năm
};

</script>

<style>
/* Đảm bảo bảng chiếm hết chiều rộng có sẵn */
.table-responsive {
  max-height: 70vh; /* Tùy chỉnh chiều cao của bảng */
  overflow-y: auto; /* Thêm cuộn dọc nếu quá cao */
}

.table {
  width: 100%; /* Bảng chiếm hết không gian ngang */
  table-layout: fixed; /* Điều chỉnh độ rộng cột đồng đều */
}

.table td,
.table th {
  word-wrap: break-word; /* Cho phép xuống dòng khi dữ liệu dài */
  white-space: normal; /* Không bị giới hạn chiều dài chữ */
  overflow: hidden; /* Ẩn văn bản nếu quá dài */
  text-overflow: ellipsis; /* Hiển thị "..." nếu quá dài */
}
</style>

<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card">
          <div class="card-body">
            <h2 class="text-center mb-4">Đăng nhập</h2>
            <form @submit.prevent="handleLogin">
              <div class="mb-3">
                <label for="phone" class="form-label">Số điện thoại:</label>
                <input
                  v-model.trim="phone"
                  type="text"
                  id="phone"
                  class="form-control"
                  placeholder="Nhập số điện thoại"
                  required
                />
              </div>
              <div class="mb-3">
                <label for="password" class="form-label">Mật khẩu:</label>
                <input
                  v-model.trim="password"
                  type="password"
                  id="password"
                  class="form-control"
                  placeholder="Nhập mật khẩu"
                  required
                />
              </div>
              <div class="d-grid">
                <button type="submit" class="btn btn-primary" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm"></span>
                  <span v-else>Đăng nhập</span>
                </button>
              </div>
            </form>
            <div v-if="errorMessage" class="alert alert-danger mt-3">
              {{ errorMessage }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

// Khai báo biến
const phone = ref("");
const password = ref(""); // Vue sử dụng biến này, nhưng khi gửi thì đặt key là "passWord"
const loading = ref(false);
const errorMessage = ref("");

// Lấy router để điều hướng
const router = useRouter();

// Hàm xử lý đăng nhập
const handleLogin = async () => {
  if (!phone.value || !password.value) {
    errorMessage.value = "Vui lòng nhập số điện thoại và mật khẩu!";
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await axios.post("http://localhost:8080/api/auth/login", {
      phone: phone.value,  
      passWord: password.value, // ✅ Đặt key là "passWord" như backend yêu cầu
    });

    const { token, name,employeeId } = response.data;

    console.log("JWT Token sau khi đăng nhập:",token);

    // Lưu token vào localStorage
    localStorage.setItem("jwtToken", token);
    localStorage.setItem("name", name);
    localStorage.setItem("employeeId", employeeId);

     // 🔥 Xóa toàn bộ lịch sử trước đó để không thể Back
     window.history.pushState(null, "", "/home");
    window.history.replaceState(null, "", "/home");

    // 🔥 Chặn cả nút Forward
    router.replace("/home"); 
  } catch (error) {
    console.error("Lỗi khi đăng nhập:", error);
    errorMessage.value = error.response?.data?.message || "Đăng nhập thất bại! Kiểm tra lại thông tin.";
  } finally {
    loading.value = false;
  }
};
</script>

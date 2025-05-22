<template>
  <div>
    <template v-if="isAuthenticated">
      <RouterView />
    </template>
    <template v-else>
      <Login />
    </template>
  </div>
</template>

<script setup>
import { ref, watchEffect } from "vue";
import { useRouter } from "vue-router";
import Login from "@/components/Login/Login.vue";

const router = useRouter();
const isAuthenticated = ref(!!localStorage.getItem("jwtToken"));

// Theo dõi thay đổi của localStorage
watchEffect(() => {
  isAuthenticated.value = !!localStorage.getItem("jwtToken");

  if (!isAuthenticated.value && router.currentRoute.value.path !== "/login") {
    router.push("/login"); // Chỉ đẩy về login nếu chưa đăng nhập
  }
});


</script>
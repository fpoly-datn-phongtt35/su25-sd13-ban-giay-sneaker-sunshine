<template>
    <div>
      <h1>Trang chủ</h1>
      <p>Chào mừng bạn đến với cửa hàng thời trang của chúng tôi!</p>
    </div>
  </template>
  
  <script setup>
  import { onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  
  onMounted(() => {
    const user = JSON.parse(localStorage.getItem('user')); // Lấy thông tin người dùng từ localStorage
    if (!user) {
      router.push('/login'); // Nếu không có người dùng, điều hướng về trang đăng nhập
    }
  });
  </script>
  
<template>
  <div>
    <HeaderLayout />
    <ContentLayout />
    <FooterLayout />

    <div class="floating-social-buttons">
      <div class="icon-wrapper">
        <span class="ping"></span>
        <a href="https://zalo.me/1234567890" target="_blank">
          <img src="/zalo.svg" alt="Zalo" class="shake-strong" />
        </a>
      </div>

      <div class="icon-wrapper">
        <span class="ping"></span>
        <a href="https://m.me/yourpage" target="_blank">
          <img src="/messenger.svg" alt="Messenger" class="shake-strong" />
        </a>
      </div>

      <div class="icon-wrapper">
        <span class="ping"></span>
        <a href="tel:0987654321">
          <img src="/phone.svg" alt="Phone" class="shake-strong" />
        </a>
      </div>

      <div class="icon-wrapper">
        <span class="ping"></span>
        <a href="https://instagram.com/yourpage" target="_blank">
          <img src="/instagram.svg" alt="Instagram" class="shake-strong" />
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import HeaderLayout from './layout/HeaderLayout.vue';
import ContentLayout from './layout/ContentLayout.vue';
import FooterLayout from './layout/FooterLayout.vue';
</script>

<style scoped>
.floating-social-buttons {
  position: fixed;
  right: 10px;
  bottom: 100px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 999;
}

/* 🌀 Gói icon với hiệu ứng sóng */
.icon-wrapper {
  position: relative;
  width: 66px;
  height: 66px;
}

/* 🌊 Sóng ping */
.ping {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 66px;
  height: 66px;
  background-color: rgba(0, 136, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: ping 1.8s infinite;
  z-index: 0;
}

/* 🧍 Icon chính */
.icon-wrapper a img {
  position: relative;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: white;
  padding: 8px;
  box-shadow: 0 0 12px rgba(255, 0, 0, 0.4);
  transition: transform 0.3s ease-in-out;
  z-index: 1;
}

/* 💥 Rung mạnh */
@keyframes shakeStrong {
  0%   { transform: rotate(0deg); }
  10%  { transform: rotate(10deg); }
  20%  { transform: rotate(-10deg); }
  30%  { transform: rotate(8deg); }
  40%  { transform: rotate(-8deg); }
  50%  { transform: rotate(6deg); }
  60%  { transform: rotate(-6deg); }
  70%  { transform: rotate(4deg); }
  80%  { transform: rotate(-4deg); }
  90%  { transform: rotate(2deg); }
  100% { transform: rotate(0deg); }
}

.shake-strong {
  animation: shakeStrong 1.5s infinite ease-in-out;
}

@keyframes ping {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(2.2);
    opacity: 0;
  }
}
</style>

<template>
  <RouterView />
</template>

<script setup>
  import { useAuthStore } from '@/store/auth';
  import { onMounted, onUnmounted } from 'vue';
  import { disconnectWebSocket } from '@/features/chat/api/webSocketService.js';

  const authStore = useAuthStore();

  // 페이지 종료 시 웹소켓 연결 해제
  const handleBeforeUnload = () => {
    if (authStore.isAuthenticated) {
      disconnectWebSocket();
      console.log('[App] 페이지 종료 시 웹소켓 연결 해제');
    }
  };

  onMounted(() => {
    authStore.initAuth();

    // beforeunload 이벤트 리스너 추가
    window.addEventListener('beforeunload', handleBeforeUnload);
  });

  onUnmounted(() => {
    // 컴포넌트 언마운트 시 이벤트 리스너 제거
    window.removeEventListener('beforeunload', handleBeforeUnload);

    // 웹소켓 연결 해제
    if (authStore.isAuthenticated) {
      disconnectWebSocket();
    }
  });
</script>

<style scoped></style>

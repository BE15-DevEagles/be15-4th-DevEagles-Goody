<template>
  <RouterView />
</template>

<script setup>
  import { useAuthStore } from '@/store/auth';
  import { onMounted, onUnmounted } from 'vue';
  import {
    disconnectWebSocket,
    initializeWebSocket,
    isWebSocketConnected,
  } from '@/features/chat/api/webSocketService.js';

  const authStore = useAuthStore();
  let isPageHidden = false;

  // 페이지 종료 시 웹소켓 연결 해제
  const handleBeforeUnload = event => {
    if (authStore.isAuthenticated) {
      console.log('[App] 페이지 종료 시 웹소켓 연결 해제');
      disconnectWebSocket();

      // 브라우저가 페이지를 즉시 닫지 않도록 약간의 지연
      const start = Date.now();
      while (Date.now() - start < 100) {
        // 100ms 동안 대기
      }
    }
  };

  // 페이지 가시성 변경 시 처리
  const handleVisibilityChange = async () => {
    if (document.hidden) {
      // 페이지가 숨겨질 때
      isPageHidden = true;
      console.log('[App] 페이지 숨김 - 웹소켓 상태 유지');
      // 즉시 연결을 해제하지 않고 상태만 기록
    } else {
      // 페이지가 다시 보일 때
      if (isPageHidden && authStore.isAuthenticated) {
        console.log('[App] 페이지 복귀 - 웹소켓 연결 상태 확인');

        // 웹소켓 연결 상태 확인 후 필요시 재연결
        if (!isWebSocketConnected()) {
          console.log('[App] 웹소켓 연결이 끊어짐, 재연결 시도');
          try {
            await initializeWebSocket();
            console.log('[App] 웹소켓 재연결 완료');
          } catch (error) {
            console.error('[App] 웹소켓 재연결 실패:', error);
          }
        } else {
          console.log('[App] 웹소켓 연결 정상');
        }
      }
      isPageHidden = false;
    }
  };

  onMounted(() => {
    authStore.initAuth();

    // 이벤트 리스너 추가
    window.addEventListener('beforeunload', handleBeforeUnload);
    document.addEventListener('visibilitychange', handleVisibilityChange);
  });

  onUnmounted(() => {
    // 이벤트 리스너 제거
    window.removeEventListener('beforeunload', handleBeforeUnload);
    document.removeEventListener('visibilitychange', handleVisibilityChange);

    // 웹소켓 연결 해제
    if (authStore.isAuthenticated) {
      disconnectWebSocket();
    }
  });
</script>

<style scoped></style>

<template>
  <RouterView />

  <!-- 타임캡슐 오픈 알림 모달 -->
  <BaseModal v-model="showOpenModal" title="타임캡슐">
    <template #default> 오픈할 타임캡슐이 있습니다. 타임캡슐을 오픈해주세요! </template>
    <template #footer>
      <BaseButton type="primary" @click="onOpenConfirm">확인</BaseButton>
    </template>
  </BaseModal>

  <!-- 타임캡슐 내용 모달 (여러 개 연속 표시) -->
  <BaseModal v-model="showContentModal" title="타임캡슐">
    <template #default>
      <div v-if="currentCapsule">
        <div class="mb-2">
          <strong>생성 일시:</strong>
          {{ formatDateTime(currentCapsule.createdAt) }}
        </div>
        <div><strong>내용:</strong> {{ currentCapsule.timecapsuleContent }}</div>
        <div class="mt-2 text-right text-xs text-gray-400">
          {{ currentCapsuleIndex + 1 }} / {{ openCapsules.length }}
        </div>
      </div>
      <div v-else>타임캡슐 데이터가 없습니다.</div>
    </template>
    <template #footer>
      <BaseButton type="primary" @click="onNextCapsule">확인</BaseButton>
    </template>
  </BaseModal>
</template>

<script setup>
  import { useAuthStore } from '@/store/auth';
  import { onMounted, onUnmounted, ref, watch, computed } from 'vue';
  import {
    disconnectWebSocket,
    initializeWebSocket,
    isWebSocketConnected,
  } from '@/features/chat/api/webSocketService.js';

  // 타임캡슐 관련 import
  import { useTeamStore } from '@/store/team';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { fetchOpenTeamTimecapsules } from '@/features/timecapsule/api/timecapsuleApi';

  const authStore = useAuthStore();
  let isPageHidden = false;

  // --- 타임캡슐 오픈 모달 상태 ---
  const teamStore = useTeamStore();
  const showOpenModal = ref(false);
  const showContentModal = ref(false);

  // 여러 개 오픈 타임캡슐
  const openCapsules = ref([]);
  const currentCapsuleIndex = ref(0);

  // 현재 보여줄 타임캡슐
  const currentCapsule = computed(() => openCapsules.value[currentCapsuleIndex.value] || null);

  // 날짜/시간 포맷 함수
  function formatDateTime(isoString) {
    if (!isoString) return '';
    const dt = new Date(isoString);
    const pad = n => n.toString().padStart(2, '0');
    return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`;
  }

  // 팀 클릭 후(혹은 currentTeamId 변경 시) 오픈 타임캡슐 조회
  async function checkOpenTimecapsule() {
    const teamId = teamStore.currentTeamId;
    if (!teamId) return;

    try {
      const capsules = await fetchOpenTeamTimecapsules(teamId);
      if (capsules.length > 0) {
        openCapsules.value = capsules;
        currentCapsuleIndex.value = 0;
        showOpenModal.value = true;
      }
    } catch (e) {
      console.error('타임캡슐 오픈 체크 실패:', e);
    }
  }

  function onOpenConfirm() {
    showOpenModal.value = false;
    showContentModal.value = true;
  }

  function onNextCapsule() {
    if (currentCapsuleIndex.value < openCapsules.value.length - 1) {
      currentCapsuleIndex.value += 1;
    } else {
      showContentModal.value = false;
    }
  }

  // 팀 변경 시마다 체크 (로그인 후, 팀 클릭 후 모두 동작)
  watch(
    () => teamStore.currentTeamId,
    () => {
      checkOpenTimecapsule();
    },
    { immediate: true }
  );

  // --- 기존 App.vue 코드 (건드리지 않음) ---
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

<style scoped>
  .mb-2 {
    margin-bottom: 0.5rem;
  }
  .mt-1 {
    margin-top: 0.25rem;
  }
  .whitespace-pre-line {
    white-space: pre-line;
  }
  .text-gray-400 {
    color: #bdbdbd;
  }
  .text-xs {
    font-size: 0.75rem;
  }
  .text-right {
    text-align: right;
  }
</style>

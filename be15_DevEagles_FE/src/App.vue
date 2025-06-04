<template>
  <RouterView />

  <!-- 타임캡슐 오픈 알림 모달 -->
  <BaseModal v-model="showOpenModal" title="타임캡슐" animation-class="magical-appear">
    <template #default>
      <div class="timecapsule-alert">
        <div class="timecapsule-icon">🕰️</div>
        <div class="alert-message">
          오픈할 타임캡슐이 있습니다.<br />
          <span class="highlight-text">타임캡슐을 오픈해주세요!</span>
        </div>
        <div class="excitement-emojis">
          <span v-for="i in 5" :key="i" class="excitement-emoji" :style="getExcitementStyle(i)">
            {{ ['🎁', '💫', '🌟', '✨', '🎊'][i % 5] }}
          </span>
        </div>
      </div>
    </template>
    <template #footer>
      <BaseButton type="primary" class="open-btn magical-btn" @click="onOpenConfirm">
        <span class="btn-icon">🔓</span>
        타임캡슐 열기
      </BaseButton>
    </template>
  </BaseModal>

  <!-- 타임캡슐 내용 모달 (여러 개 연속 표시) -->
  <BaseModal v-model="showContentModal" title="타임캡슐" animation-class="back-in-left">
    <template #default>
      <div v-if="currentCapsule" class="timecapsule-content">
        <div class="mystical-bg">
          <div v-for="i in 20" :key="i" class="floating-star" :style="getFloatingStarStyle(i)">
            ⭐
          </div>
        </div>

        <div class="capsule-reveal" :class="{ revealed: showContent }">
          <div class="capsule-container">
            <div class="capsule-lid">🎁</div>
            <div class="capsule-body">
              <div class="content-glow">
                <div class="date-info">
                  <div class="date-label">📅 생성 일시</div>
                  <div class="date-value">{{ formatDateTimeKST(currentCapsule.createdAt) }}</div>
                </div>
                <div class="content-info">
                  <div class="content-label">💌 타임캡슐 내용</div>
                  <div class="content-value">{{ currentCapsule.timecapsuleContent }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="capsule-counter">
          <span class="counter-icon">📦</span>
          {{ currentCapsuleIndex + 1 }} / {{ openCapsules.length }}
        </div>
      </div>
      <div v-else class="no-data">타임캡슐 데이터가 없습니다.</div>
    </template>
    <template #footer>
      <BaseButton type="primary" class="next-btn magical-btn" @click="onNextCapsule">
        <span class="btn-icon">{{
          currentCapsuleIndex < openCapsules.length - 1 ? '➡️' : '✅'
        }}</span>
        {{ currentCapsuleIndex < openCapsules.length - 1 ? '다음 타임캡슐' : '완료' }}
      </BaseButton>
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
  const showContent = ref(false);

  // 여러 개 오픈 타임캡슐
  const openCapsules = ref([]);
  const currentCapsuleIndex = ref(0);

  // 현재 보여줄 타임캡슐
  const currentCapsule = computed(() => openCapsules.value[currentCapsuleIndex.value] || null);

  function getExcitementStyle(index) {
    const delay = index * 0.3;
    return {
      animationDelay: `${delay}s`,
    };
  }

  function getFloatingStarStyle(index) {
    const x = Math.random() * 100;
    const y = Math.random() * 100;
    const delay = index * 0.1;
    const duration = 4 + Math.random() * 3;
    return {
      left: `${x}%`,
      top: `${y}%`,
      animationDelay: `${delay}s`,
      animationDuration: `${duration}s`,
    };
  }

  function formatDateTimeKST(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    const pad = n => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
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
    // ★ 타임캡슐 오픈 애니메이션 시작
    setTimeout(() => {
      showContent.value = true;
    }, 500);
  }

  function onNextCapsule() {
    if (currentCapsuleIndex.value < openCapsules.value.length - 1) {
      showContent.value = false;
      setTimeout(() => {
        currentCapsuleIndex.value += 1;
        showContent.value = true;
      }, 300);
    } else {
      showContentModal.value = false;
      showContent.value = false;
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
  .timecapsule-alert {
    position: relative;
    text-align: center;
    padding: 20px;
    overflow: hidden;
  }

  .timecapsule-icon {
    font-size: 4rem;
    margin-bottom: 16px;
    animation: icon-pulse 2s ease-in-out infinite;
  }

  .alert-message {
    font-size: 1.1rem;
    line-height: 1.6;
    margin-bottom: 20px;
    color: #333;
  }

  .highlight-text {
    color: #e74c3c;
    font-weight: 700;
    text-shadow: 0 0 10px rgba(231, 76, 60, 0.3);
    animation: text-shimmer 2s ease-in-out infinite;
  }

  .excitement-emojis {
    margin-top: 16px;
  }

  .excitement-emoji {
    display: inline-block;
    font-size: 1.5rem;
    margin: 0 8px;
    animation: excitement-bounce 1.5s ease-in-out infinite;
  }

  .timecapsule-content {
    position: relative;
    padding: 20px;
    min-height: 300px;
    overflow: hidden;
  }

  .mystical-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    background: linear-gradient(135deg, rgba(37, 113, 128, 0.05) 0%, rgba(255, 215, 0, 0.05) 100%);
  }

  .floating-star {
    position: absolute;
    font-size: 0.8rem;
    animation: float-twinkle 4s ease-in-out infinite;
    opacity: 0.7;
  }

  .capsule-reveal {
    position: relative;
    z-index: 2;
    text-align: center;
  }

  .capsule-container {
    position: relative;
    margin: 20px 0;
  }

  .capsule-lid {
    font-size: 3rem;
    margin-bottom: 16px;
    animation: lid-shake 0.5s ease-in-out;
    transform-origin: center bottom;
  }

  .capsule-reveal.revealed .capsule-lid {
    animation: lid-open 1s ease-out forwards;
  }

  .capsule-body {
    opacity: 0;
    transform: scale(0.8) translateY(20px);
    transition: all 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }

  .capsule-reveal.revealed .capsule-body {
    opacity: 1;
    transform: scale(1) translateY(0);
  }

  .content-glow {
    background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
    border-radius: 12px;
    padding: 20px;
    box-shadow:
      0 8px 32px rgba(0, 0, 0, 0.1),
      0 0 20px rgba(255, 215, 0, 0.2);
    border: 2px solid rgba(255, 215, 0, 0.3);
    animation: content-glow 2s ease-in-out infinite;
  }

  .date-info,
  .content-info {
    margin-bottom: 16px;
    text-align: left;
  }

  .date-label,
  .content-label {
    font-size: 0.9rem;
    color: #666;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .date-value,
  .content-value {
    font-size: 1.1rem;
    color: #333;
    font-weight: 500;
    line-height: 1.5;
  }

  .capsule-counter {
    margin-top: 20px;
    text-align: center;
    font-size: 0.9rem;
    color: #888;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .counter-icon {
    font-size: 1.2rem;
  }

  .magical-btn {
    position: relative;
    overflow: hidden;
    background: linear-gradient(135deg, #257180 0%, #4db6ac 100%);
    box-shadow: 0 4px 15px rgba(37, 113, 128, 0.3);
    transition: all 0.3s ease;
  }

  .magical-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(37, 113, 128, 0.4);
  }

  .magical-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.5s;
  }

  .magical-btn:hover::before {
    left: 100%;
  }

  .btn-icon {
    margin-right: 8px;
    font-size: 1.2rem;
  }

  .no-data {
    text-align: center;
    color: #888;
    padding: 40px 20px;
  }

  @keyframes icon-pulse {
    0%,
    100% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.1);
    }
  }

  @keyframes text-shimmer {
    0%,
    100% {
      text-shadow: 0 0 10px rgba(231, 76, 60, 0.3);
    }
    50% {
      text-shadow:
        0 0 20px rgba(231, 76, 60, 0.6),
        0 0 30px rgba(255, 215, 0, 0.4);
    }
  }

  @keyframes excitement-bounce {
    0%,
    100% {
      transform: translateY(0) rotate(0deg);
    }
    50% {
      transform: translateY(-10px) rotate(10deg);
    }
  }

  @keyframes float-twinkle {
    0%,
    100% {
      transform: translateY(0) scale(1);
      opacity: 0.7;
    }
    50% {
      transform: translateY(-15px) scale(1.2);
      opacity: 1;
    }
  }

  @keyframes lid-shake {
    0%,
    100% {
      transform: rotate(0deg);
    }
    25% {
      transform: rotate(-2deg);
    }
    75% {
      transform: rotate(2deg);
    }
  }

  @keyframes lid-open {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(-45deg) translateY(-10px);
      opacity: 0.3;
    }
  }

  @keyframes content-glow {
    0%,
    100% {
      box-shadow:
        0 8px 32px rgba(0, 0, 0, 0.1),
        0 0 20px rgba(255, 215, 0, 0.2);
    }
    50% {
      box-shadow:
        0 8px 32px rgba(0, 0, 0, 0.15),
        0 0 30px rgba(255, 215, 0, 0.4);
    }
  }

  .magical-appear {
    animation: magical-appear 1s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }

  @keyframes magical-appear {
    0% {
      opacity: 0;
      transform: scale(0.3) rotate(-10deg) translateY(50px);
    }
    50% {
      opacity: 0.8;
      transform: scale(1.1) rotate(5deg) translateY(-10px);
    }
    100% {
      opacity: 1;
      transform: scale(1) rotate(0deg) translateY(0);
    }
  }

  .mb-2 {
    margin-bottom: 0.5rem;
  }
  .mt-2 {
    margin-top: 0.5rem;
  }
  .text-right {
    text-align: right;
  }
  .text-xs {
    font-size: 0.75rem;
  }
  .text-gray-400 {
    color: #bdbdbd;
  }
</style>

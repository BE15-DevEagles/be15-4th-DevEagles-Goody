<template>
  <header class="bg-[var(--color-gray-800)] h-14 w-full flex items-center px-3 shadow-drop z-50">
    <div class="flex items-center justify-between w-full">
      <div class="w-30 h-10">
        <img
          src="/assets/image/logo-goody-with-text.png"
          alt="Goody Logo"
          class="w-full h-full object-contain"
        />
      </div>

      <div class="flex items-center space-x-4">
        <!-- 검색 -->
        <div class="relative">
          <input
            type="text"
            placeholder="검색"
            class="bg-[var(--color-primary-400)] text-white placeholder-[var(--color-gray-300)] rounded-md px-3 py-1.5 pl-9 font-small outline-none focus:ring-1 focus:ring-white w-48"
          />
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-4 w-4 text-[var(--color-gray-300)] absolute left-3 top-1/2 transform -translate-y-1/2"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </div>

        <!-- 프로필 -->
        <div class="relative group mr-4">
          <button
            class="flex items-center space-x-2 text-white hover:opacity-80 transition-opacity"
          >
            <div
              class="rounded-full bg-white h-8 w-8 overflow-hidden flex items-center justify-center border-2 border-white"
            >
              <img
                v-if="userThumbnailUrl"
                :src="userThumbnailUrl"
                :alt="name"
                class="w-full h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center bg-[var(--color-primary-300)] text-white font-one-liner-semibold"
              >
                {{ name ? name.charAt(0) : '?' }}
              </div>
            </div>
            <span class="font-one-liner-semibold">{{ name }}</span>
          </button>

          <!-- 드롭다운 메뉴 -->
          <div
            class="absolute right-0 top-full mt-2 bg-white rounded-xl shadow-xl border border-[var(--color-gray-200)] overflow-hidden invisible opacity-0 group-hover:visible group-hover:opacity-100 transition-all duration-300 ease-out transform scale-95 group-hover:scale-100 w-52 z-[9999] backdrop-blur-sm"
          >
            <!-- 사용자 정보 헤더 -->
            <div
              class="px-4 py-3 bg-gradient-to-r from-[var(--color-primary-50)] to-[var(--color-primary-100)] border-b border-[var(--color-gray-200)]"
            >
              <div class="flex items-center space-x-3">
                <div class="w-8 h-8 rounded-full overflow-hidden flex-shrink-0">
                  <img
                    v-if="userThumbnailUrl"
                    :src="userThumbnailUrl"
                    :alt="name"
                    class="w-full h-full object-cover"
                  />
                  <div
                    v-else
                    class="w-full h-full flex items-center justify-center bg-[var(--color-primary-300)] text-white font-one-liner-semibold text-sm"
                  >
                    {{ name ? name.charAt(0) : '?' }}
                  </div>
                </div>
                <div class="flex-grow min-w-0">
                  <p class="font-one-liner-semibold text-[var(--color-gray-800)] truncate">
                    {{ name }}
                  </p>
                  <p class="text-xs text-[var(--color-gray-500)]">온라인</p>
                </div>
              </div>
            </div>

            <!-- 메뉴 아이템들 -->
            <div class="py-2">
              <a
                href="#"
                class="flex items-center px-4 py-3 text-[var(--color-gray-700)] hover:bg-[var(--color-primary-50)] hover:text-[var(--color-primary-600)] font-one-liner transition-all duration-200 group/item"
                @click.prevent="handleMyPage"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-4 w-4 mr-3 text-[var(--color-gray-500)] group-hover/item:text-[var(--color-primary-500)] transition-colors"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                  />
                </svg>
                나의 정보
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-4 w-4 ml-auto opacity-0 group-hover/item:opacity-100 transition-opacity"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </a>

              <div
                class="mx-4 my-2 h-px bg-gradient-to-r from-transparent via-[var(--color-gray-200)] to-transparent"
              ></div>

              <a
                href="#"
                class="flex items-center px-4 py-3 text-[var(--color-error-400)] hover:bg-[var(--color-error-50)] hover:text-[var(--color-error-500)] font-one-liner transition-all duration-200 group/item"
                @click.prevent="handleLogout"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-4 w-4 mr-3 text-[var(--color-error-400)] group-hover/item:text-[var(--color-error-500)] transition-colors"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                  />
                </svg>
                로그아웃
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-4 w-4 ml-auto opacity-0 group-hover/item:opacity-100 transition-opacity"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
  import { logout } from '@/features/user/api/user.js';
  import { useRouter } from 'vue-router';
  import { useAuthStore } from '@/store/auth.js';
  import { disconnectWebSocket } from '@/features/chat/api/webSocketService.js';
  import { useUserStatusStore } from '@/store/userStatus.js';
  import { logoutUserStatus } from '@/features/chat/api/userStatusService.js';
  import { storeToRefs } from 'pinia';

  const router = useRouter();
  const authStore = useAuthStore();
  const userStatusStore = useUserStatusStore();

  const { name, userThumbnailUrl } = storeToRefs(authStore);

  const handleLogout = async () => {
    console.log('[Header] 로그아웃 시작');

    let logoutUserStatusSuccess = false;

    try {
      // 1. 먼저 사용자 상태 스토어 리셋 (웹소켓 구독 해제 포함)
      userStatusStore.reset();
      console.log('[Header] 사용자 상태 스토어 리셋 완료');

      // 2. 백엔드에 오프라인 상태 알림 (Redis에서 사용자 제거) - JWT 토큰이 유효할 때 먼저 실행
      try {
        logoutUserStatusSuccess = await logoutUserStatus();
        console.log(
          '[Header] 사용자 오프라인 상태 변경:',
          logoutUserStatusSuccess ? '성공' : '실패'
        );
      } catch (statusError) {
        console.warn('[Header] 사용자 오프라인 상태 변경 실패:', statusError);
      }

      // 3. 웹소켓 연결 해제
      disconnectWebSocket();
      console.log('[Header] 웹소켓 연결 해제 완료');

      // 4. 서버에 로그아웃 요청 (JWT 토큰 무효화)
      try {
        await logout();
        console.log('[Header] 서버 로그아웃 완료');
      } catch (logoutError) {
        console.warn('[Header] 서버 로그아웃 실패:', logoutError);
      }
    } catch (error) {
      console.error('[Header] 로그아웃 처리 중 오류:', error);
    } finally {
      // 5. 항상 실행되는 클라이언트 정리
      authStore.clearAuth();
      console.log('[Header] 인증 정보 삭제 완료');

      // 6. 로그인 페이지로 이동
      router.push('/login');

      // 7. 오프라인 상태 처리가 실패했다면 강제로 한 번 더 시도 (토큰 없이)
      if (!logoutUserStatusSuccess) {
        console.log('[Header] 오프라인 상태 처리 재시도');
        setTimeout(async () => {
          try {
            // 토큰 없이 직접 스토어에서 현재 사용자 제거
            const currentUserId = authStore.userId;
            if (currentUserId) {
              userStatusStore.updateUserStatus(currentUserId, false);
            }
          } catch (retryError) {
            console.warn('[Header] 오프라인 상태 재시도 실패:', retryError);
          }
        }, 1000);
      }
    }
  };

  const handleMyPage = () => {
    router.push('/mypage');
  };
</script>

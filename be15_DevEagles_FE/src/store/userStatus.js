import { defineStore } from 'pinia';
import { subscribeToUserStatus } from '@/features/chat/api/webSocketService';
import { getOnlineUsers } from '@/features/chat/api/userStatusService';

export const useUserStatusStore = defineStore('userStatus', {
  state: () => ({
    onlineUsers: new Set(), // 온라인 사용자 ID 집합
    isInitialized: false,
  }),

  getters: {
    isUserOnline: state => {
      return userId => {
        if (!userId) return false;

        // 다양한 타입의 userId를 안전하게 문자열로 변환
        const userIdStr = String(userId).trim();
        const isOnline = state.onlineUsers.has(userIdStr);

        // 디버깅을 위한 로그 (개발 환경에서만)
        if (import.meta.env.DEV) {
          console.log(`[UserStatusStore] 사용자 ${userIdStr} 온라인 상태 확인: ${isOnline}`, {
            originalUserId: userId,
            convertedUserId: userIdStr,
            onlineUsers: Array.from(state.onlineUsers),
          });
        }

        return isOnline;
      };
    },

    getOnlineUserCount: state => {
      return state.onlineUsers.size;
    },

    getOnlineUserIds: state => {
      return Array.from(state.onlineUsers);
    },
  },

  actions: {
    // 사용자 상태 업데이트
    updateUserStatus(userId, isOnline) {
      const userIdStr = String(userId);
      const wasOnline = this.onlineUsers.has(userIdStr);

      if (isOnline) {
        this.onlineUsers.add(userIdStr);
        if (!wasOnline) {
          console.log(`[UserStatusStore] 사용자 ${userId} 온라인됨`);
        }
      } else {
        this.onlineUsers.delete(userIdStr);
        if (wasOnline) {
          console.log(`[UserStatusStore] 사용자 ${userId} 오프라인됨`);
        }
      }

      console.log(
        `[UserStatusStore] 현재 온라인 사용자: ${this.onlineUsers.size}명`,
        Array.from(this.onlineUsers)
      );
    },

    // 초기 온라인 사용자 목록 로드
    async loadInitialOnlineUsers() {
      try {
        console.log('[UserStatusStore] 초기 온라인 사용자 목록 로드 시작');
        const onlineUserIds = await getOnlineUsers();

        // 기존 온라인 사용자 목록 초기화
        this.onlineUsers.clear();

        // 새로운 온라인 사용자 목록 설정
        onlineUserIds.forEach(userId => {
          this.onlineUsers.add(String(userId));
        });

        console.log(`[UserStatusStore] 초기 온라인 사용자 ${this.onlineUsers.size}명 로드 완료`);
      } catch (error) {
        console.error('[UserStatusStore] 초기 온라인 사용자 목록 로드 실패:', error);
      }
    },

    // 웹소켓 구독 초기화
    async initializeUserStatusSubscription() {
      if (this.isInitialized) {
        console.log('[UserStatusStore] 이미 초기화됨, 온라인 사용자 목록만 새로고침');
        await this.loadInitialOnlineUsers();
        return;
      }

      console.log('[UserStatusStore] 사용자 상태 구독 시작');

      try {
        // 1. 먼저 현재 온라인 사용자 목록을 REST API로 가져오기
        await this.loadInitialOnlineUsers();

        // 2. 현재 로그인한 사용자를 온라인 상태로 추가 (자기 자신은 항상 온라인)
        const { useAuthStore } = await import('@/store/auth');
        const authStore = useAuthStore();
        if (authStore.userId) {
          this.onlineUsers.add(String(authStore.userId));
          console.log(`[UserStatusStore] 현재 사용자 ${authStore.userId} 온라인 상태로 추가`);
        }

        // 3. 웹소켓 연결 확인 및 초기화
        const { initializeWebSocket } = await import('@/features/chat/api/webSocketService');

        console.log('[UserStatusStore] 웹소켓 초기화 시작');
        await initializeWebSocket();
        console.log('[UserStatusStore] 웹소켓 초기화 완료');

        // 4. 웹소켓 구독 시작
        const { subscribeToUserStatus } = await import('@/features/chat/api/webSocketService');
        subscribeToUserStatus(statusMessage => {
          console.log('[UserStatusStore] 상태 메시지 수신:', statusMessage);

          if (
            statusMessage &&
            statusMessage.userId !== undefined &&
            statusMessage.online !== undefined
          ) {
            this.updateUserStatus(statusMessage.userId, statusMessage.online);
          } else {
            console.warn('[UserStatusStore] 잘못된 상태 메시지 형식:', statusMessage);
          }
        });

        this.isInitialized = true;
        console.log('[UserStatusStore] 사용자 상태 구독 초기화 완료');
      } catch (error) {
        console.error('[UserStatusStore] 사용자 상태 구독 초기화 실패:', error);
        // 실패해도 REST API로 가져온 데이터는 유지
      }
    },

    // 온라인 사용자 목록 새로고침
    async refreshOnlineUsers() {
      await this.loadInitialOnlineUsers();
    },

    // 초기화 상태 리셋
    reset() {
      this.onlineUsers.clear();
      this.isInitialized = false;
      console.log('[UserStatusStore] 상태 초기화됨');
    },
  },
});

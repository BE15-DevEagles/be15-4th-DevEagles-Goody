import { defineStore } from 'pinia';
import { subscribeToUserStatus, unsubscribe } from '@/features/chat/api/webSocketService';
import { getOnlineUsers } from '@/features/chat/api/userStatusService';

export const useUserStatusStore = defineStore('userStatus', {
  state: () => ({
    onlineUsers: new Set(),
    isInitialized: false,
  }),

  getters: {
    isUserOnline: state => {
      return userId => {
        return state.onlineUsers.has(String(userId));
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
    updateUserStatus(userId, isOnline) {
      const userIdStr = String(userId).trim();

      if (
        !userIdStr ||
        userIdStr === 'null' ||
        userIdStr === 'undefined' ||
        userIdStr.includes('@')
      ) {
        console.warn(`[UserStatusStore] 유효하지 않은 사용자 ID 무시: "${userIdStr}"`);
        return;
      }

      const currentlyOnline = this.onlineUsers.has(userIdStr);
      if (currentlyOnline === isOnline) {
        console.log(
          `[UserStatusStore] 사용자 ${userIdStr} 상태 변경 없음 (이미 ${isOnline ? '온라인' : '오프라인'})`
        );
        return;
      }

      if (isOnline) {
        this.onlineUsers.add(userIdStr);
        console.log(`[UserStatusStore] 사용자 ${userIdStr} 온라인됨`);
      } else {
        this.onlineUsers.delete(userIdStr);
        console.log(`[UserStatusStore] 사용자 ${userIdStr} 오프라인됨`);
      }

      console.log(
        `[UserStatusStore] 현재 온라인 사용자: ${this.onlineUsers.size}명`,
        Array.from(this.onlineUsers)
      );
    },

    async loadInitialOnlineUsers() {
      try {
        console.log('[UserStatusStore] 초기 온라인 사용자 목록 로드 시작');
        const onlineUserIds = await getOnlineUsers();

        this.onlineUsers.clear();

        onlineUserIds.forEach(userId => {
          const userIdStr = String(userId).trim();
          if (userIdStr && userIdStr !== 'null' && userIdStr !== 'undefined') {
            if (!userIdStr.includes('@')) {
              this.onlineUsers.add(userIdStr);
            }
          }
        });

        console.log(
          `[UserStatusStore] 초기 온라인 사용자 ${this.onlineUsers.size}명 로드 완료:`,
          Array.from(this.onlineUsers)
        );
      } catch (error) {
        console.error('[UserStatusStore] 초기 온라인 사용자 목록 로드 실패:', error);
      }
    },

    // 웹소켓 구독 초기화
    async initializeUserStatusSubscription() {
      if (this.isInitialized) {
        console.log('[UserStatusStore] 이미 초기화됨');
        return;
      }

      console.log('[UserStatusStore] 사용자 상태 구독 시작');

      // 1. 먼저 현재 온라인 사용자 목록을 REST API로 가져오기
      await this.loadInitialOnlineUsers();

      // 2. 웹소켓 구독 시작
      subscribeToUserStatus(statusMessage => {
        console.log('[UserStatusStore] 상태 메시지 수신:', statusMessage);

        if (
          statusMessage &&
          statusMessage.userId !== undefined &&
          statusMessage.online !== undefined
        ) {
          const userIdStr = String(statusMessage.userId).trim();
          // 이메일 형태가 아닌 숫자 ID만 처리
          if (userIdStr && !userIdStr.includes('@')) {
            this.updateUserStatus(userIdStr, statusMessage.online);
          } else {
            console.log('[UserStatusStore] 이메일 형태 사용자 ID 무시:', userIdStr);
          }
        } else {
          console.warn('[UserStatusStore] 잘못된 상태 메시지 형식:', statusMessage);
        }
      });

      this.isInitialized = true;
    },

    // 온라인 사용자 목록 새로고침
    async refreshOnlineUsers() {
      await this.loadInitialOnlineUsers();
    },

    // 초기화 상태 리셋
    reset() {
      // 웹소켓 사용자 상태 구독 해제
      try {
        unsubscribe('user.status');
        console.log('[UserStatusStore] 사용자 상태 웹소켓 구독 해제 완료');
      } catch (error) {
        console.error('[UserStatusStore] 사용자 상태 구독 해제 실패:', error);
      }

      this.onlineUsers.clear();
      this.isInitialized = false;
      console.log('[UserStatusStore] 상태 초기화됨');
    },
  },
});

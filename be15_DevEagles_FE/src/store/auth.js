import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

function decodeJwtPayload(token) {
  const payload = token.split('.')[1];
  const decoded = atob(payload);
  const utf8Payload = new TextDecoder().decode(Uint8Array.from(decoded, c => c.charCodeAt(0)));
  return JSON.parse(utf8Payload);
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(null);
  const userId = ref(null);
  const expirationTime = ref(null);

  // ✅ 추가 상태
  const name = ref(null);
  const userThumbnailUrl = ref(null);
  const userStatus = ref(null);
  const returnUser = ref(null);

  const isAuthenticated = computed(
    () =>
      !!accessToken.value &&
      Date.now() < (expirationTime.value || 0) &&
      userStatus.value === 'ENABLED'
  );

  async function setAuth(at) {
    accessToken.value = at;
    try {
      const payload = decodeJwtPayload(at);
      console.log('payload', payload);
      userId.value = payload.userId;
      name.value = payload.name || null;
      userThumbnailUrl.value = payload.userThumbnailUrl || null;
      expirationTime.value = payload.exp * 1000;
      userStatus.value = payload.userStatus;
      returnUser.value = payload.returnUser;

      localStorage.setItem('accessToken', at);

      // 인증 완료 후 사용자 상태 초기화
      if (userStatus.value === 'ENABLED') {
        try {
          const { useUserStatusStore } = await import('@/store/userStatus');
          const userStatusStore = useUserStatusStore();
          await userStatusStore.initializeUserStatusSubscription();
        } catch (error) {
          console.error('사용자 상태 초기화 실패:', error);
        }
      }
    } catch (e) {
      clearAuth();
    }
  }

  function clearAuth() {
    // 인증 정보 삭제
    accessToken.value = null;
    userId.value = null;
    name.value = null;
    userThumbnailUrl.value = null;
    expirationTime.value = null;
    returnUser.value = null;

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');

    console.log('[Auth] 인증 정보 삭제 완료');
  }

  async function initAuth() {
    const token = localStorage.getItem('accessToken');
    if (token) {
      await setAuth(token);
    }
  }

  function updateAuth(up) {
    name.value = up.name;
    userThumbnailUrl.value = up.thumbnail;
  }

  return {
    accessToken,
    userId,
    name,
    userThumbnailUrl,
    expirationTime,
    isAuthenticated,
    returnUser,
    setAuth,
    clearAuth,
    initAuth,
    updateAuth,
  };
});

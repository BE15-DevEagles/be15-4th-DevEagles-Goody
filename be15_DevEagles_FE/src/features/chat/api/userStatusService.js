import api from '@/api/axios';

/**
 * 현재 온라인 사용자 목록 조회
 * @returns {Promise<string[]>} 온라인 사용자 ID 배열
 */
export async function getOnlineUsers() {
  try {
    const response = await api.get('/user-status/online-users');

    if (response.data?.success && response.data?.data) {
      const onlineUsers = Array.from(response.data.data);
      console.log('[userStatusService] 온라인 사용자 목록 조회 성공:', onlineUsers.length, '명');
      return onlineUsers;
    }

    console.warn('[userStatusService] 온라인 사용자 목록 조회 응답이 비어있음');
    return [];
  } catch (error) {
    console.error('[userStatusService] 온라인 사용자 목록 조회 실패:', error);
    return [];
  }
}

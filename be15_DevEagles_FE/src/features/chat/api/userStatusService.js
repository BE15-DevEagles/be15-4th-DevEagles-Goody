import api from '@/api/axios';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('userStatusService');

export async function getOnlineUsers() {
  try {
    const response = await api.get('/user-status/online-users');

    if (response.data?.success && response.data?.data) {
      const onlineUsers = Array.from(response.data.data);
      logger.info('[userStatusService] 온라인 사용자 목록 조회 성공:', onlineUsers.length, '명');
      return onlineUsers;
    }

    logger.warn('[userStatusService] 온라인 사용자 목록 조회 응답이 비어있음');
    return [];
  } catch (error) {
    logger.error('[userStatusService] 온라인 사용자 목록 조회 실패:', error);
    return [];
  }
}

export async function logoutUserStatus() {
  try {
    const response = await api.delete('/user-status/logout');

    if (response.data?.success) {
      logger.info('[userStatusService] 사용자 오프라인 상태 변경 성공');
      return true;
    }

    logger.warn('[userStatusService] 사용자 오프라인 상태 변경 실패:', response.data);
    return false;
  } catch (error) {
    logger.error('[userStatusService] 사용자 오프라인 상태 변경 실패:', error);
    return false;
  }
}

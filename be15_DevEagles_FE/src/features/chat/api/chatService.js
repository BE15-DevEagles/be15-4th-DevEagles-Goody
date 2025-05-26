import api from '@/api/axios';
import { sendWebSocketMessage } from './webSocketService';
import { createOrGetAiChatRoom } from './aiChatService';
import { useAuthStore } from '@/store/auth.js';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('chatService');

export async function getChatRooms(teamId = null) {
  try {
    const params = {};
    if (teamId) {
      params.teamId = teamId;
    }

    const response = await api.get('/chatrooms', { params });
    return response.data.data.chatrooms;
  } catch (error) {
    logger.error('채팅방 목록 조회 실패:', error);
    throw error;
  }
}

export async function createChatRoom(request) {
  try {
    logger.info('[chatService] 채팅방 생성 요청:', request);
    logger.debug('[chatService] JSON 직렬화 테스트:', JSON.stringify(request));

    const response = await api.post('/chatrooms', request, {
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
      },
      transformRequest: [
        function (data) {
          return JSON.stringify(data);
        },
      ],
    });

    logger.info('[chatService] 채팅방 생성 응답:', response.data);
    return response.data;
  } catch (error) {
    logger.error('채팅방 생성 실패:', error);
    logger.error('요청 데이터:', request);
    if (error.response) {
      logger.error('응답 데이터:', error.response.data);
      logger.error('요청 설정:', error.config);
    }
    throw error;
  }
}

export async function getChatHistory(chatRoomId, before = null, limit = 20) {
  try {
    logger.info('[chatService] 채팅 히스토리 요청:', {
      chatRoomId,
      before,
      limit,
    });

    const params = {};
    if (before) {
      params.before = before;
    }
    params.limit = limit;

    const response = await api.get(`/chatrooms/${chatRoomId}/messages`, {
      params,
    });

    if (response.data.data && response.data.data.messages) {
      const messages = response.data.data.messages;
      logger.info(`[chatService] 히스토리 로드: ${messages.length}개 메시지`);

      const noTimestampCount = messages.filter(msg => !msg.timestamp && !msg.createdAt).length;
      if (noTimestampCount > 0) {
        logger.warn(`[chatService] 타임스탬프 없는 메시지: ${noTimestampCount}개`);
      }

      const normalizedMessages = messages.map(msg => {
        if (!msg.timestamp && msg.createdAt) {
          msg.timestamp = msg.createdAt;
        }
        return msg;
      });

      return {
        ...response.data.data,
        messages: normalizedMessages,
      };
    }

    return response.data.data;
  } catch (error) {
    logger.error(`채팅 이력 조회 실패:`, error);
    throw error;
  }
}

export function sendMessage(chatRoomId, message) {
  let authStore;
  try {
    authStore = useAuthStore();
  } catch (error) {
    logger.error('Auth 스토어 접근 실패:', error);
  }

  return sendWebSocketMessage(chatRoomId, message, authStore);
}

export async function markAsRead(chatRoomId, messageId = null) {
  try {
    const params = {};
    if (messageId) {
      params.messageId = messageId;
    }

    await api.put(`/chatrooms/${chatRoomId}/read`, null, { params });
    logger.info(`[chatService] 읽음 처리 완료: chatRoomId=${chatRoomId}, messageId=${messageId}`);
    return true;
  } catch (error) {
    logger.error('읽음 표시 실패:', error);
    return false;
  }
}

export async function getMessageReadStatus(chatroomId, messageId) {
  try {
    const response = await api.get(`/chatrooms/${chatroomId}/messages/${messageId}/read-status`);
    return response.data.data;
  } catch (error) {
    logger.error('메시지 읽음 상태 조회 실패:', error);
    throw error;
  }
}

export async function getChatNotificationSetting(chatRoomId) {
  try {
    const response = await api.get(`/chatrooms/${chatRoomId}/notification`);
    return response.data.data;
  } catch (error) {
    logger.error('알림 설정 조회 실패:', error);
    throw error;
  }
}

export async function toggleChatNotification(chatRoomId) {
  try {
    const response = await api.put(`/chatrooms/${chatRoomId}/notification/toggle`);
    return response.data.data;
  } catch (error) {
    logger.error('알림 설정 변경 실패:', error);
    throw error;
  }
}

export async function getAllNotificationSettings() {
  try {
    const response = await api.get('/chatrooms/notifications');
    return response.data.data;
  } catch (error) {
    logger.error('전체 알림 설정 조회 실패:', error);
    throw error;
  }
}

export { createOrGetAiChatRoom };

import { ref } from 'vue';
import { getChatHistory, markAsRead } from '@/features/chat/api/chatService.js';
import { sendWebSocketMessage } from '@/features/chat/api/webSocketService.js';
import { useAuthStore } from '@/store/auth.js';
import { useChatStore } from '@/store/chat.js';
import { createTempMessage } from '@/features/chat/utils/messageUtils.js';
import { normalizeTimestamp } from '@/features/chat/utils/timeUtils.js';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('useChatRoom');

export function useChatRoom() {
  const isLoading = ref(false);
  const isSending = ref(false);
  const error = ref(null);

  const authStore = useAuthStore();
  const chatStore = useChatStore();

  const loadChatHistory = async (chatRoomId, before = null, limit = 30) => {
    if (!chatRoomId) {
      logger.warn('[useChatRoom] 채팅방 ID가 없습니다.');
      return [];
    }

    try {
      isLoading.value = true;
      error.value = null;

      logger.info('[useChatRoom] 채팅 히스토리 로드 시작:', {
        chatRoomId,
        before,
        limit,
      });

      const response = await getChatHistory(chatRoomId, before, limit);

      let messages = [];

      if (response && Array.isArray(response.messages)) {
        messages = response.messages;
      } else if (response && Array.isArray(response)) {
        messages = response;
      } else if (response && response.data) {
        if (Array.isArray(response.data.messages)) {
          messages = response.data.messages;
        } else if (Array.isArray(response.data)) {
          messages = response.data;
        }
      }

      messages = messages.map(msg => {
        if (!msg.timestamp && !msg.createdAt) {
          logger.warn('[useChatRoom] 메시지에 타임스탬프 없음:', msg.id);
        }
        msg.timestamp = normalizeTimestamp(msg.timestamp || msg.createdAt);
        return msg;
      });

      messages.reverse();

      logger.info('[useChatRoom] 로드된 메시지 수:', messages.length);
      return messages;
    } catch (err) {
      logger.error('[useChatRoom] 채팅 히스토리 로드 실패:', err);
      error.value = '채팅 이력을 불러오는데 실패했습니다.';
      return [];
    } finally {
      isLoading.value = false;
    }
  };

  const sendMessage = async (chatRoomId, content) => {
    if (!chatRoomId || !content?.trim()) {
      logger.warn('[useChatRoom] 메시지 전송 실패 - 필수 정보 부족');
      return null;
    }

    if (isSending.value) {
      logger.warn('[useChatRoom] 이미 메시지 전송 중입니다.');
      return null;
    }

    try {
      isSending.value = true;
      error.value = null;

      logger.info('[useChatRoom] 메시지 전송 시작:', { chatRoomId, content });

      const success = sendWebSocketMessage(
        chatRoomId,
        content.trim(),
        authStore.userId,
        authStore.name
      );

      if (success) {
        logger.info('[useChatRoom] 메시지 전송 성공');

        const tempMessage = {
          ...createTempMessage(content.trim(), authStore.userId, authStore.name),
          chatroomId: chatRoomId,
        };

        return tempMessage;
      } else {
        throw new Error('WebSocket 메시지 전송 실패');
      }
    } catch (err) {
      logger.error('[useChatRoom] 메시지 전송 실패:', err);
      error.value = '메시지 전송에 실패했습니다.';
      return null;
    } finally {
      isSending.value = false;
    }
  };

  const markChatAsRead = async chatRoomId => {
    if (!chatRoomId) {
      logger.warn('[useChatRoom] 읽음 처리 실패 - 채팅방 ID 없음');
      return false;
    }

    try {
      logger.info('[useChatRoom] 채팅방 읽음 처리:', chatRoomId);
      await markAsRead(chatRoomId);

      if (chatStore.markChatAsRead) {
        chatStore.markChatAsRead(chatRoomId);
      }

      logger.info('[useChatRoom] 읽음 처리 완료');
      return true;
    } catch (err) {
      logger.error('[useChatRoom] 읽음 처리 실패:', err);
      return false;
    }
  };

  const updateChatInfo = (chatRoomId, lastMessage) => {
    if (!chatRoomId || !lastMessage) return;

    try {
      if (chatStore.updateChatAfterSending) {
        chatStore.updateChatAfterSending(chatRoomId, lastMessage.content);
      }
    } catch (err) {
      logger.error('[useChatRoom] 채팅방 정보 업데이트 실패:', err);
    }
  };

  const clearError = () => {
    error.value = null;
  };

  return {
    isLoading,
    isSending,
    error,
    loadChatHistory,
    sendMessage,
    markChatAsRead,
    updateChatInfo,
    clearError,
  };
}

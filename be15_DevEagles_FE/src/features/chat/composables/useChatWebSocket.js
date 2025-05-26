import { ref, onUnmounted } from 'vue';
import {
  subscribeToChatRoom,
  subscribeToReadStatus,
  sendReadMessage,
  sendAiChatInit,
  unsubscribe,
  isWebSocketConnected,
} from '@/features/chat/api/webSocketService.js';
import { useAuthStore } from '@/store/auth.js';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('useChatWebSocket');

export function useChatWebSocket() {
  const currentSubscription = ref(null);
  const isConnected = ref(false);
  const authStore = useAuthStore();

  const updateConnectionStatus = () => {
    isConnected.value = isWebSocketConnected();
  };

  const subscribeToChat = (chatRoomId, messageHandler, readStatusHandler) => {
    if (!chatRoomId) {
      logger.warn('[useChatWebSocket] 채팅방 ID가 없습니다.');
      return;
    }

    if (currentSubscription.value === chatRoomId) {
      logger.info('[useChatWebSocket] 이미 같은 채팅방을 구독 중입니다:', chatRoomId);
      return;
    }

    unsubscribeFromChat();

    logger.info('[useChatWebSocket] 채팅방 구독 시작:', chatRoomId);

    try {
      subscribeToChatRoom(chatRoomId, message => {
        logger.info('[useChatWebSocket] 메시지 수신:', {
          id: message.id,
          chatroomId: message.chatroomId,
          content: message.content?.substring(0, 20),
          senderId: message.senderId,
        });

        if (messageHandler && message.chatroomId === chatRoomId) {
          messageHandler(message);

          const currentUserId = authStore.user?.id?.toString();
          if (message.senderId !== currentUserId && message.id) {
            logger.info('[useChatWebSocket] 자동 읽음 처리:', {
              messageId: message.id,
              senderId: message.senderId,
              currentUserId: currentUserId,
            });
            markMessageAsRead(chatRoomId, message.id);
          }
        } else {
          logger.warn('[useChatWebSocket] 메시지 채팅방 ID 불일치:', {
            expected: chatRoomId,
            received: message.chatroomId,
          });
        }
      });

      subscribeToReadStatus(chatRoomId, readStatus => {
        logger.info('[useChatWebSocket] 읽음 상태 수신:', readStatus);
        if (readStatusHandler && readStatus.chatroomId === chatRoomId) {
          readStatusHandler(readStatus);
        }
      });

      currentSubscription.value = chatRoomId;
      updateConnectionStatus();

      logger.info('[useChatWebSocket] 채팅방 구독 완료:', chatRoomId);
    } catch (error) {
      logger.error('[useChatWebSocket] 구독 중 오류 발생:', error);
      currentSubscription.value = null;
    }
  };

  const unsubscribeFromChat = () => {
    if (currentSubscription.value) {
      logger.info('[useChatWebSocket] 구독 해제 시작:', currentSubscription.value);

      try {
        unsubscribe(`/topic/chatroom.${currentSubscription.value}`);
        unsubscribe(`/topic/chatroom.${currentSubscription.value}.read`);
        logger.info('[useChatWebSocket] 구독 해제 완료');
      } catch (error) {
        logger.error('[useChatWebSocket] 구독 해제 중 오류:', error);
      }

      currentSubscription.value = null;
    }
    updateConnectionStatus();
  };

  const markMessageAsRead = (chatRoomId, messageId) => {
    if (!chatRoomId || !messageId) {
      logger.warn('[useChatWebSocket] 읽음 처리 실패 - 필수 정보 부족:', {
        chatRoomId,
        messageId,
      });
      return false;
    }

    try {
      const success = sendReadMessage(chatRoomId, messageId);
      if (success) {
        logger.info('[useChatWebSocket] 읽음 상태 전송 성공:', { chatRoomId, messageId });
      }
      return success;
    } catch (error) {
      logger.error('[useChatWebSocket] 읽음 상태 전송 실패:', error);
      return false;
    }
  };

  const markLatestMessageAsRead = (chatRoomId, messages) => {
    if (!chatRoomId || !messages || messages.length === 0) return;

    const latestMessage = messages[messages.length - 1];
    if (latestMessage && !latestMessage.isMe) {
      markMessageAsRead(chatRoomId, latestMessage.id);
    }
  };

  const initializeAiChat = chatRoomId => {
    if (!chatRoomId) {
      logger.warn('[useChatWebSocket] AI 초기화 실패 - 채팅방 ID 없음');
      return false;
    }

    try {
      const success = sendAiChatInit(chatRoomId);
      if (success) {
        logger.info('[useChatWebSocket] AI 채팅 초기화 성공:', chatRoomId);
      }
      return success;
    } catch (error) {
      logger.error('[useChatWebSocket] AI 초기화 실패:', error);
      return false;
    }
  };

  onUnmounted(() => {
    unsubscribeFromChat();
  });

  return {
    currentSubscription,
    isConnected,
    subscribeToChat,
    unsubscribeFromChat,
    markMessageAsRead,
    markLatestMessageAsRead,
    updateConnectionStatus,
    initializeAiChat,
  };
}

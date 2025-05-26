import { ref, nextTick } from 'vue';
import { useAuthStore } from '@/store/auth';
import { useChatStore } from '@/store/chat';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('useChatWindow');

export function useChatWindow() {
  const authStore = useAuthStore();
  const chatStore = useChatStore();
  const currentChatMessageHandler = ref(null);

  const initializeChat = async (
    chatId,
    { loadChatHistory, setMessages, markChatAsRead, clearError, onReady }
  ) => {
    if (!chatId) {
      logger.warn('[useChatWindow] 채팅방 ID가 없습니다.');
      return;
    }

    logger.info('[useChatWindow] 채팅 초기화 시작:', chatId);

    try {
      clearError();

      const historyMessages = await loadChatHistory(chatId, null, 15);

      if (historyMessages && historyMessages.length > 0) {
        logger.info('[useChatWindow] 초기 메시지 로드 완료:', historyMessages.length);
        setMessages(historyMessages, authStore.userId);
      } else {
        logger.info('[useChatWindow] 초기 메시지 없음');
      }

      await markChatAsRead(chatId);

      if (onReady) {
        onReady();
      }

      logger.info('[useChatWindow] 채팅 초기화 완료 (전역 웹소켓 구독 + ChatWindow 핸들러)');
    } catch (err) {
      logger.error('[useChatWindow] 채팅 초기화 실패:', err);
      throw err;
    }
  };

  const handleIncomingMessage = (
    message,
    { currentChatId, addMessage, updateChatInfo, markMessageAsRead, scrollToBottom }
  ) => {
    logger.info('[useChatWindow] 새 메시지 수신:', {
      id: message.id,
      chatroomId: message.chatroomId,
      content: message.content?.substring(0, 20),
    });

    if (message.chatroomId !== currentChatId) {
      logger.info('[useChatWindow] 다른 채팅방 메시지 무시');
      return;
    }

    if (!message.timestamp && !message.createdAt) {
      message.timestamp = new Date().toISOString();
      logger.warn('[useChatWindow] 메시지에 타임스탬프 없어 현재 시간 설정:', message.id);
    }

    const messageWithUserId = {
      ...message,
      currentUserId: authStore.userId,
    };

    const success = addMessage(messageWithUserId);

    if (success) {
      logger.info('[useChatWindow] 메시지 추가 성공');
      scrollToBottom();

      if (message.senderId !== authStore.userId && message.id) {
        markMessageAsRead(currentChatId, message.id);
      }
      updateChatInfo(currentChatId, message);
    }
  };

  const registerMessageHandler = (handler, chatId) => {
    logger.info('[useChatWindow] 메시지 핸들러 등록 요청:', {
      hasHandler: !!handler,
      handlerType: typeof handler,
      chatId,
    });

    currentChatMessageHandler.value = handler;
    chatStore.registerChatWindowHandler(chatId, handler);

    logger.info('[useChatWindow] 메시지 핸들러 등록 완료:', {
      isRegistered: !!currentChatMessageHandler.value,
    });
  };

  const unregisterMessageHandler = () => {
    logger.info('[useChatWindow] 메시지 핸들러 해제 요청');
    chatStore.unregisterChatWindowHandler();
    currentChatMessageHandler.value = null;
    logger.info('[useChatWindow] 메시지 핸들러 해제 완료');
  };

  const loadMoreMessages = async (
    chatId,
    { getOldestMessage, loadChatHistory, setMessages, messages, maintainScrollPosition }
  ) => {
    if (!chatId) return { hasMore: false };

    const oldestMessage = getOldestMessage();
    const beforeId = oldestMessage?.id;

    logger.info('[useChatWindow] 이전 메시지 로드 시작:', {
      beforeId,
      currentCount: messages.value.length,
    });

    return await maintainScrollPosition(async () => {
      const olderMessages = await loadChatHistory(chatId, beforeId, 15);

      if (olderMessages && olderMessages.length > 0) {
        setMessages([...olderMessages, ...messages.value], authStore.userId);
        logger.info('[useChatWindow] 이전 메시지 추가 완료:', olderMessages.length);
        return { hasMore: olderMessages.length >= 15 };
      }

      return { hasMore: false };
    });
  };

  return {
    initializeChat,
    handleIncomingMessage,
    loadMoreMessages,
    registerMessageHandler,
    unregisterMessageHandler,
  };
}

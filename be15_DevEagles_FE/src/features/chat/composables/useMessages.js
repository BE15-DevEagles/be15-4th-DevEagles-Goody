import { ref, computed } from 'vue';
import { formatMessageTime, formatDate } from '../utils/timeUtils';
import {
  createTempMessage,
  normalizeMessage,
  isDuplicateMessage,
  findTempMessageIndex,
  groupMessagesByDate,
  isTempMessage,
} from '../utils/messageUtils';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('useMessages');

export function useMessages() {
  const messages = ref([]);

  const addMessage = message => {
    if (!message) return false;

    if (isDuplicateMessage(messages.value, message)) {
      logger.info('[useMessages] 중복 메시지 무시:', message.id);
      return false;
    }

    if (!isTempMessage(message.id)) {
      const tempIndex = findTempMessageIndex(messages.value, message);
      if (tempIndex !== -1) {
        logger.info('[useMessages] 임시 메시지를 실제 메시지로 교체:', {
          tempId: messages.value[tempIndex].id,
          realId: message.id,
        });

        const normalizedMessage = normalizeMessage(message, message.currentUserId);
        messages.value[tempIndex] = normalizedMessage;
        return true;
      }
    }

    const formattedMessage = normalizeMessage(message, message.currentUserId);
    messages.value.push(formattedMessage);

    logger.info('[useMessages] 메시지 추가:', message.id);
    return true;
  };

  const addTemporaryMessage = (content, senderId, senderName) => {
    const tempMessage = createTempMessage(content, senderId, senderName);
    messages.value.push(tempMessage);

    logger.info('[useMessages] 임시 메시지 추가:', tempMessage.id);
    return tempMessage;
  };

  const groupedMessages = computed(() => groupMessagesByDate(messages.value));

  const clearMessages = () => {
    logger.info('[useMessages] 메시지 초기화');
    messages.value = [];
  };

  const setMessages = (newMessages, currentUserId) => {
    logger.info('[useMessages] 메시지 목록 설정, 개수:', newMessages?.length || 0);

    if (!newMessages || !Array.isArray(newMessages)) {
      logger.warn('[useMessages] 유효하지 않은 메시지 배열:', newMessages);
      return;
    }

    const normalizedMessages = newMessages.map(msg => {
      const formattedMessage = {
        ...normalizeMessage(msg, currentUserId),
        currentUserId,
      };
      return formattedMessage;
    });

    const uniqueMessages = [];
    const seenIds = new Set();

    normalizedMessages.forEach(msg => {
      if (!seenIds.has(msg.id)) {
        seenIds.add(msg.id);
        uniqueMessages.push(msg);
      }
    });

    uniqueMessages.sort((a, b) => {
      const timeA = new Date(a.timestamp).getTime();
      const timeB = new Date(b.timestamp).getTime();
      return timeA - timeB;
    });

    messages.value = uniqueMessages;

    logger.info('[useMessages] 최종 메시지 수:', messages.value.length);
  };

  const getMessageCount = () => messages.value.length;

  const getLastMessage = () => {
    if (messages.value.length === 0) return null;
    const sorted = [...messages.value].sort(
      (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
    return sorted[0];
  };

  const getOldestMessage = () => {
    if (messages.value.length === 0) return null;
    const sorted = [...messages.value].sort(
      (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
    );
    return sorted[0];
  };

  const removeTempMessage = tempId => {
    const index = messages.value.findIndex(msg => msg.id === tempId);
    if (index !== -1) {
      messages.value.splice(index, 1);
      logger.info('[useMessages] 임시 메시지 제거:', tempId);
      return true;
    }
    return false;
  };

  const updateMessage = (messageId, updates) => {
    const index = messages.value.findIndex(msg => msg.id === messageId);
    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], ...updates };
      logger.info('[useMessages] 메시지 업데이트:', messageId);
      return true;
    }
    return false;
  };

  return {
    messages,
    groupedMessages,
    addMessage,
    addTemporaryMessage,
    clearMessages,
    setMessages,
    formatMessageTime,
    formatDate,
    getMessageCount,
    getLastMessage,
    getOldestMessage,
    removeTempMessage,
    updateMessage,
  };
}

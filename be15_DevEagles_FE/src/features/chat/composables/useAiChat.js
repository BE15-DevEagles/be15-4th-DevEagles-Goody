import { ref, computed } from 'vue';
import {
  createOrGetAiChatRoom,
  generateMoodInquiry,
  saveMoodAnswer,
  getUserMoodHistory,
} from '@/features/chat/api/aiChatService';
import {
  isAiChatRoom,
  isAiMessage,
  isMoodInquiryMessage,
  findPendingMoodInquiry,
  isWaitingForMoodResponse,
  getAiMessageType,
  AI_CONSTANTS,
} from '@/features/chat/utils/aiMessageUtils';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('useAiChat');

export function useAiChat() {
  const isAiThinking = ref(false);
  const isProcessingMood = ref(false);
  const error = ref(null);
  const moodHistory = ref([]);

  const initializeAiChat = async (userId, aiName = '수리AI') => {
    try {
      const chatRoom = await createOrGetAiChatRoom(userId, aiName);
      return chatRoom;
    } catch (err) {
      error.value = 'AI 채팅방 초기화 실패';
      logger.error('[useAiChat] 초기화 실패:', err);
      throw err;
    }
  };

  const requestMoodInquiry = async userId => {
    try {
      isProcessingMood.value = true;
      error.value = null;

      const inquiry = await generateMoodInquiry(userId);

      if (inquiry) {
        logger.info('[useAiChat] 기분 질문 생성됨:', inquiry);
        return inquiry;
      } else {
        logger.info('[useAiChat] 오늘 이미 기분 질문 완료');
        return null;
      }
    } catch (err) {
      error.value = '기분 질문을 생성할 수 없습니다. 잠시 후 다시 시도해 주세요.';
      logger.error('[useAiChat] 기분 질문 실패:', err);
      setAiThinking(false);
      throw err;
    } finally {
      isProcessingMood.value = false;
    }
  };

  const processMoodAnswer = async (inquiryId, answer) => {
    try {
      isProcessingMood.value = true;
      error.value = null;

      const result = await saveMoodAnswer(inquiryId, answer);
      logger.info('[useAiChat] 기분 답변 저장됨:', result);

      return result;
    } catch (err) {
      error.value = '답변을 저장할 수 없습니다. 네트워크를 확인해 주세요.';
      logger.error('[useAiChat] 기분 답변 실패:', err);
      setAiThinking(false);
      throw err;
    } finally {
      isProcessingMood.value = false;
    }
  };

  const loadMoodHistory = async userId => {
    try {
      const history = await getUserMoodHistory(userId);
      moodHistory.value = history;
      return history;
    } catch (err) {
      error.value = '기분 히스토리 로드 실패';
      logger.error('[useAiChat] 히스토리 로드 실패:', err);
      throw err;
    }
  };

  const setAiThinking = thinking => {
    isAiThinking.value = thinking;
  };

  const handleUserMessageForMood = async (message, messages) => {
    if (!message || !messages) return false;

    const pendingInquiry = findPendingMoodInquiry(messages);

    if (pendingInquiry) {
      try {
        const inquiryId = pendingInquiry.metadata?.inquiryId;
        if (inquiryId) {
          logger.info('[useAiChat] 기분 질문에 대한 답변 감지:', {
            inquiryId,
            answer: message.content,
          });

          await processMoodAnswer(inquiryId, message.content);
          return true;
        }
      } catch (err) {
        logger.error('[useAiChat] 기분 답변 처리 오류:', err);
        error.value = 'AI 응답을 처리할 수 없습니다. 잠시 후 다시 시도해 주세요.';
        setAiThinking(false);
        return false;
      }
    }

    return false;
  };

  const handleAiMessage = message => {
    if (!isAiMessage(message)) return;

    const messageType = getAiMessageType(message);

    switch (messageType) {
      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_INQUIRY:
        logger.info('[useAiChat] 기분 질문 수신:', message.content);
        break;

      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_FEEDBACK:
        logger.info('[useAiChat] 기분 피드백 수신:', message.content);
        break;

      case AI_CONSTANTS.MESSAGE_TYPES.GENERAL_RESPONSE:
        logger.info('[useAiChat] 일반 AI 응답 수신:', message.content);
        break;
    }

    setAiThinking(false);
  };

  const clearError = () => {
    error.value = null;
  };

  const hasError = computed(() => !!error.value);
  const isProcessing = computed(() => isProcessingMood.value || isAiThinking.value);

  return {
    isAiThinking,
    isProcessingMood,
    error,
    moodHistory,

    hasError,
    isProcessing,

    initializeAiChat,
    requestMoodInquiry,
    processMoodAnswer,
    loadMoodHistory,
    setAiThinking,
    handleUserMessageForMood,
    handleAiMessage,
    clearError,

    isAiChatRoom,
    isAiMessage,
    isMoodInquiryMessage,
    isWaitingForMoodResponse,
    findPendingMoodInquiry,
  };
}

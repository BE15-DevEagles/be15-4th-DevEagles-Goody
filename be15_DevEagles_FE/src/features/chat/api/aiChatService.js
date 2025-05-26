import api from '@/api/axios';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('aiChatService');

export async function createOrGetAiChatRoom(userId, aiName = '수리AI') {
  try {
    const response = await api.post('/ai-chat', null, {
      params: { userId, aiName },
    });
    return response.data.data;
  } catch (error) {
    logger.error('AI 채팅방 생성 실패:', error);
    throw error;
  }
}

export async function getUserAiChatRooms(userId) {
  try {
    const response = await api.get('/ai-chat/me', {
      params: { userId },
    });
    return response.data.data;
  } catch (error) {
    logger.error('AI 채팅방 목록 조회 실패:', error);
    return [];
  }
}

export async function getUserAiChatRoom(userId) {
  try {
    const aiChatRooms = await getUserAiChatRooms(userId);
    return aiChatRooms.length > 0 ? aiChatRooms[0] : null;
  } catch (error) {
    logger.error('AI 채팅방 조회 실패:', error);
    return null;
  }
}

export async function getAiChatMessages(chatroomId, page = 0, size = 20) {
  try {
    const response = await api.get(`/ai-chat/${chatroomId}/messages`, {
      params: { page, size },
    });
    return response.data.data;
  } catch (error) {
    logger.error('AI 채팅 메시지 조회 실패:', error);
    throw error;
  }
}

export async function generateMoodInquiry(userId) {
  try {
    const response = await api.post('/ai-chat/mood-inquiry', null, {
      params: { userId },
    });
    return response.data.data;
  } catch (error) {
    if (error.response?.status === 400) {
      return null;
    }
    logger.error('기분 질문 생성 실패:', error);
    throw error;
  }
}

export async function saveMoodAnswer(inquiryId, answer) {
  try {
    const response = await api.post('/ai-chat/mood-answer', {
      inquiryId,
      answer,
    });
    return response.data.data;
  } catch (error) {
    logger.error('기분 답변 저장 실패:', error);
    throw error;
  }
}

export async function getUserMoodHistory(userId) {
  try {
    const response = await api.get('/ai-chat/mood-history', {
      params: { userId },
    });
    return response.data.data;
  } catch (error) {
    logger.error('기분 히스토리 조회 실패:', error);
    throw error;
  }
}

import api from '@/api/axios';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('rouletteService');

export async function sendRouletteResultToChat(teamId, result) {
  try {
    const response = await api.post('/roulette/send-result', {
      teamId,
      result,
    });
    logger.info('룰렛 결과 전송 성공:', { teamId, result });
    return response.data;
  } catch (error) {
    logger.error('룰렛 결과 전송 실패:', error);
    throw error;
  }
}

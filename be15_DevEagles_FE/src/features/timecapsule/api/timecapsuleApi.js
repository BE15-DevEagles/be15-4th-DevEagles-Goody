import api from '@/api/axios';

export async function createTimecapsule({ timecapsuleContent, openDate, teamId }) {
  try {
    const response = await api.post('/api/v1/timecapsules', {
      timecapsuleContent,
      openDate,
      teamId,
    });
    return response.data;
  } catch (error) {
    console.error('타임캡슐 생성 실패:', error);
    throw error;
  }
}

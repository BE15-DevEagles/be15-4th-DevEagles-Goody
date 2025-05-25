import api from '@/api/axios';

export async function createTimecapsule({ timecapsuleContent, openDate, teamId }) {
  try {
    const response = await api.post('/timecapsules', {
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

export async function fetchOpenTeamTimecapsules(teamId) {
  try {
    const res = await api.get(`/timecapsules/team/${teamId}/open`);
    return res.data.data || [];
  } catch (error) {
    console.error('타임캡슐 오픈 목록 조회 실패:', error);
    return [];
  }
}

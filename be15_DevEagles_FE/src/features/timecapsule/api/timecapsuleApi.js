import api from '@/api/axios';

// 타임캡슐 생성
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

// 특정 팀의 오늘 오픈할 타임캡슐 목록 조회 (기존)
export async function fetchOpenTeamTimecapsules(teamId) {
  try {
    const res = await api.get(`/timecapsules/team/${teamId}/open`);
    return res.data.data || [];
  } catch (error) {
    console.error('타임캡슐 오픈 목록 조회 실패:', error);
    return [];
  }
}

// INACTIVE(오픈 가능한) 타임캡슐 전체 조회 (조회 페이지에서 사용)
export async function fetchInactiveTimecapsules() {
  try {
    // 백엔드 컨트롤러에서 /api/v1/timecapsules/opened 가 INACTIVE만 반환한다고 가정
    const res = await api.get('/timecapsules/opened');
    return res.data.data || [];
  } catch (error) {
    console.error('INACTIVE 타임캡슐 목록 조회 실패:', error);
    return [];
  }
}

// 타임캡슐 삭제
export async function deleteTimecapsule(timecapsuleId) {
  try {
    await api.delete(`/timecapsules/${timecapsuleId}`);
    return true;
  } catch (error) {
    console.error('타임캡슐 삭제 실패:', error);
    throw error;
  }
}

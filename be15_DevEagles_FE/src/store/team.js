import { defineStore } from 'pinia';
import api from '@/api/axios';

export const useTeamStore = defineStore('team', {
  state: () => ({
    currentTeam: null,
    teams: [],
    teamMembers: [],
    loading: false,
    error: null,
  }),

  getters: {
    currentTeamId: state => state.currentTeam?.teamId,
    teamChannels: state => state.currentTeam?.channels || [],
  },

  actions: {
    // 팀 목록 로드
    async fetchTeams() {
      this.loading = true;
      try {
        const response = await api.get('teams/my');
        this.teams = response.data.data;

        // 팀이 없는 경우 초기화
        if (this.teams.length === 0) {
          this.currentTeam = null;
          this.teamMembers = [];
          localStorage.removeItem('lastSelectedTeam');
          console.log('소속된 팀이 없습니다.');
          return;
        }

        const lastSelectedTeam = localStorage.getItem('lastSelectedTeam');
        const teamToSelect =
          lastSelectedTeam && this.teams.find(team => team.teamId === Number(lastSelectedTeam))
            ? Number(lastSelectedTeam)
            : this.teams[0]?.teamId;

        if (teamToSelect) {
          await this.setCurrentTeam(teamToSelect);
        }
      } catch (err) {
        this.error = err.message;
        console.error('팀 목록 로드 실패:', err);
      } finally {
        this.loading = false;
      }
    },

    // 현재 팀 설정
    async setCurrentTeam(teamId) {
      // teamId가 유효하지 않은 경우 처리
      if (!teamId || teamId === 'undefined' || teamId === 'null') {
        console.warn('유효하지 않은 teamId:', teamId);
        this.currentTeam = null;
        this.teamMembers = [];
        return;
      }

      this.loading = true;
      try {
        // 팀 상세 정보 조회
        const teamRes = await api.get(`teams/teams/${teamId}`);
        this.currentTeam = teamRes.data.data;

        // 팀 멤버 목록 조회 (감정 분석 포함)
        const memberRes = await api.get(`teams/${teamId}/members/with-mood`);
        this.teamMembers = Array.isArray(memberRes.data.data) ? memberRes.data.data : [];

        console.log('📦 멤버 API 응답 (감정 포함):', memberRes.data.data);

        localStorage.setItem('lastSelectedTeam', teamId);

        // 팀 변경 후 채팅방 목록 새로고침
        try {
          const { useChatStore } = await import('./chat');
          const chatStore = useChatStore();
          await chatStore.loadChatRooms();
          console.log('[setCurrentTeam] 채팅방 목록 새로고침 완료');
        } catch (chatError) {
          console.warn('[setCurrentTeam] 채팅방 목록 새로고침 실패:', chatError);
        }
      } catch (err) {
        this.error = err.message;
        console.error('팀 설정 실패:', err);
        // 에러 발생 시 상태 초기화
        this.currentTeam = null;
        this.teamMembers = [];
      } finally {
        this.loading = false;
      }
    },
  },
});

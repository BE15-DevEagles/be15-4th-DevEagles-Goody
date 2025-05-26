<template>
  <aside class="bg-[var(--color-gray-800)] h-full w-24 flex flex-col items-center py-3">
    <!-- 팀 썸네일 목록 -->
    <div class="flex flex-col items-center space-y-3 overflow-y-auto flex-grow min-h-0 py-2 px-2">
      <div
        v-for="team in teamStore.teams"
        :key="team.teamId"
        class="w-10 h-10 rounded-xl flex items-center justify-center cursor-pointer relative overflow-visible group transition-all duration-300 ease-out transform hover:scale-110 flex-shrink-0"
        :class="[
          team.teamId === teamStore.currentTeamId
            ? team.teamThumbnailUrl
              ? 'ring-2 ring-[var(--color-primary-400)] ring-offset-2 ring-offset-[var(--color-gray-800)] shadow-lg shadow-[var(--color-primary-400)]/40 scale-105'
              : 'bg-[var(--color-primary-400)] ring-2 ring-[var(--color-primary-300)] ring-offset-2 ring-offset-[var(--color-gray-800)] shadow-lg shadow-[var(--color-primary-400)]/40 scale-105'
            : team.teamThumbnailUrl
              ? 'hover:ring-2 hover:ring-[var(--color-gray-500)] hover:ring-offset-2 hover:ring-offset-[var(--color-gray-800)] hover:shadow-lg'
              : 'bg-[var(--color-gray-600)] hover:bg-[var(--color-gray-500)] hover:ring-2 hover:ring-[var(--color-gray-400)] hover:ring-offset-2 hover:ring-offset-[var(--color-gray-800)] hover:shadow-lg',
        ]"
        @click="switchTeam(team.teamId)"
      >
        <!-- 썸네일 이미지 or 팀 이름 두 글자 -->
        <img
          v-if="team.teamThumbnailUrl"
          :src="team.teamThumbnailUrl"
          :alt="team.teamName"
          class="w-full h-full object-cover transition-all duration-300 rounded-xl"
          :class="
            team.teamId === teamStore.currentTeamId
              ? 'brightness-110'
              : 'group-hover:brightness-110'
          "
        />
        <span
          v-else
          class="text-white font-xs-semibold transition-all duration-300"
          :class="
            team.teamId === teamStore.currentTeamId
              ? 'text-white scale-110'
              : 'group-hover:scale-110'
          "
        >
          {{ team.teamName?.charAt(0) + team.teamName?.charAt(1) || '?' }}
        </span>

        <!-- 활성 팀 표시기 - 더 세련되게 -->
        <div
          v-if="team.teamId === teamStore.currentTeamId"
          class="absolute -left-4 top-1/2 transform -translate-y-1/2 w-2 h-6 bg-white rounded-r-lg shadow-md transition-all duration-300"
        ></div>

        <!-- 선택된 팀 글로우 효과 -->
        <div
          v-if="team.teamId === teamStore.currentTeamId"
          class="absolute inset-0 rounded-xl bg-gradient-to-r from-[var(--color-primary-400)]/20 to-[var(--color-primary-500)]/20 animate-pulse"
        ></div>

        <!-- 호버 툴팁 -->
        <div
          class="absolute left-16 top-1/2 transform -translate-y-1/2 z-20 invisible opacity-0 group-hover:visible group-hover:opacity-100 transition-all duration-300 bg-[var(--color-gray-900)] text-white px-3 py-2 rounded-lg font-small shadow-xl border border-[var(--color-gray-600)] backdrop-blur-sm whitespace-nowrap"
        >
          {{ team.teamName || '팀 이름 없음' }}
          <!-- 툴팁 화살표 -->
          <div
            class="absolute left-0 top-1/2 transform -translate-x-1 -translate-y-1/2 w-0 h-0 border-t-4 border-b-4 border-r-4 border-transparent border-r-[var(--color-gray-900)]"
          ></div>
        </div>
      </div>
    </div>

    <!-- 구분선 -->
    <div
      class="w-12 h-px bg-gradient-to-r from-transparent via-[var(--color-gray-600)] to-transparent my-3 flex-shrink-0"
    ></div>

    <!-- 팀 생성 버튼 -->
    <div
      class="w-10 h-10 rounded-xl bg-[var(--color-gray-600)] flex items-center justify-center cursor-pointer hover:bg-[var(--color-primary-400)] group relative transition-all duration-300 ease-out transform hover:scale-110 hover:ring-2 hover:ring-[var(--color-primary-300)] hover:ring-offset-2 hover:ring-offset-[var(--color-gray-800)] hover:shadow-lg hover:shadow-[var(--color-primary-400)]/30 flex-shrink-0 mb-2"
      @click="createTeam"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-5 w-5 text-white transition-all duration-300 group-hover:scale-110 group-hover:rotate-90"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M12 6v6m0 0v6m0-6h6m-6 0H6"
        />
      </svg>

      <!-- 호버 툴팁 -->
      <div
        class="absolute left-16 top-1/2 transform -translate-y-1/2 z-20 invisible opacity-0 group-hover:visible group-hover:opacity-100 transition-all duration-300 bg-[var(--color-gray-900)] text-white px-3 py-2 rounded-lg font-small shadow-xl border border-[var(--color-gray-600)] backdrop-blur-sm whitespace-nowrap"
      >
        New Team
        <!-- 툴팁 화살표 -->
        <div
          class="absolute left-0 top-1/2 transform -translate-x-1 -translate-y-1/2 w-0 h-0 border-t-4 border-b-4 border-r-4 border-transparent border-r-[var(--color-gray-900)]"
        ></div>
      </div>
    </div>
  </aside>

  <!-- 팀 생성 모달 -->
  <CreateTeamModal v-model="showModal" @submit="handleTeamCreate" />
</template>

<script setup>
  import { ref, onMounted, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import { useTeamStore } from '@/store/team';
  import { useChatStore } from '@/store/chat';
  import CreateTeamModal from '@/features/team/components/CreateTeamModal.vue';

  const router = useRouter(); // ✅ router 인스턴스
  const teamStore = useTeamStore();
  const chatStore = useChatStore();

  const showModal = ref(false);

  // 팀 데이터 로드
  onMounted(async () => {
    await teamStore.fetchTeams();
  });

  // 팀 전환 처리 + 홈 이동
  function switchTeam(teamId) {
    teamStore.setCurrentTeam(teamId);
    router.push('/'); // ✅ 홈 페이지로 이동
  }

  // 팀 변경 감지 및 관련 데이터 갱신
  watch(
    () => teamStore.currentTeamId,
    async newTeamId => {
      if (newTeamId) {
        try {
          // 채팅방 목록 새로고침 (AI 채팅방 포함)
          await chatStore.loadChatRooms();
          console.log(`팀 변경: ${newTeamId}, 팀원 수: ${teamStore.teamMembers.length}`);
        } catch (err) {
          console.error('팀 관련 데이터 갱신 실패:', err);
        }
      } else {
        // 팀이 선택되지 않은 경우에도 AI 채팅방은 로드
        try {
          await chatStore.loadChatRooms();
          console.log('팀 선택 해제 - AI 채팅방만 로드');
        } catch (err) {
          console.error('AI 채팅방 로드 실패:', err);
        }
      }
    },
    { immediate: true }
  );

  // 팀 생성 모달 제출
  const handleTeamCreate = async ({ teamName, description }) => {
    try {
      await teamStore.createTeam({ teamName, description });
      await teamStore.fetchTeams();
    } catch (e) {
      console.error('팀 생성 실패:', e);
    }
  };

  const createTeam = () => {
    showModal.value = true;
  };
</script>

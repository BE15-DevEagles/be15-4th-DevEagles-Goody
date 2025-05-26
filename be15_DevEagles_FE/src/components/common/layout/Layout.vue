<template>
  <div class="flex flex-col h-screen bg-[var(--color-gray-800)]">
    <Header />
    <div class="flex flex-1 overflow-hidden min-w-0">
      <TeamSidebar />
      <Sidebar />
      <Content :current-page="currentPage" :page-description="pageDescription" />
      <RightSidebar
        :is-collapsed="isSidebarCollapsed"
        @update:is-collapsed="handleSidebarCollapse"
      />
    </div>
    <Footer />
  </div>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { useTeamStore } from '@/store/team';
  import Header from './Header.vue';
  import Sidebar from './Sidebar.vue';
  import TeamSidebar from './TeamSidebar.vue';
  import Content from './Content.vue';
  import RightSidebar from './RightSidebar.vue';
  import Footer from './Footer.vue';
  import { useRoute } from 'vue-router';

  const teamStore = useTeamStore();
  const isSidebarCollapsed = ref(false);
  const handleSidebarCollapse = val => {
    console.log('[Sidebar 상태] isSidebarCollapsed 변경됨 →', val);
    isSidebarCollapsed.value = val;
  };

  const route = useRoute();
  const isMyPage = computed(() => route.path.startsWith('/mypage'));
  const isHomePage = computed(() => route.path === '/');

  const currentPage = computed(() => {
    if (isHomePage.value) return '홈';
    if (isMyPage.value) return '마이페이지';
    if (route.path === '/calendar/my') return '내 캘린더';
    if (route.path === '/calendar/team') return '팀 캘린더';
    if (route.path === '/worklog/my') return '업무일지 현황';
    if (route.path === '/worklog/create') return '업무일지 작성';
    if (route.path.includes('/worklog/detail')) return '업무일지 상세';
    if (route.path === '/todos') return 'Todo 목록';
    if (route.path.includes('/timecapsule')) return '타임캡슐';
    if (route.path === '/roulette') return '룰렛';
    if (route.path.includes('/team/info')) return '팀 정보';
    return teamStore.currentTeam?.name || '일반';
  });

  const pageDescription = computed(() => {
    if (isHomePage.value) return '협업 플랫폼 Goody';
    if (isMyPage.value) return '';
    if (route.path === '/calendar/my') return '개인 일정 관리';
    if (route.path === '/calendar/team') return '팀 일정 공유 및 관리';
    if (route.path === '/worklog/my') return '이번 달 현황';
    if (route.path === '/worklog/create') return '오늘의 업무 성과를 체계적으로 기록하세요';
    if (route.path.includes('/worklog/detail')) return '업무일지 상세 내용';
    if (route.path === '/todos') return '할 일 관리 및 우선순위 설정';
    if (route.path.includes('/timecapsule')) return '미래를 위한 메시지 보관';
    if (route.path === '/roulette') return '팀 활동 및 의사결정 도구';
    if (route.path.includes('/team/info')) return '팀원 정보 및 역할 관리';
    return teamStore.currentTeam?.description || '팀 채널 소통 공간';
  });
</script>

<style>
  html,
  body {
    height: 100%;
    margin: 0;
    padding: 0;
    font-family: 'Noto Sans KR', sans-serif;
    color: var(--color-gray-800);
    background-color: var(--color-gray-100);
  }

  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }

  ::-webkit-scrollbar-track {
    background: transparent;
  }

  ::-webkit-scrollbar-thumb {
    background: var(--color-gray-400);
    border-radius: 4px;
  }

  ::-webkit-scrollbar-thumb:hover {
    background: var(--color-gray-500);
  }

  /* 모든 버튼의 기본 스타일 */
  button {
    transition: all 0.2s;
  }

  /* 모든 인풋의 기본 스타일 */
  input,
  textarea,
  select {
    outline: none;
    transition: all 0.2s;
  }

  /* 기본 포커스 스타일 */
  *:focus-visible {
    outline: 2px solid var(--color-primary-300);
    outline-offset: 2px;
  }
</style>

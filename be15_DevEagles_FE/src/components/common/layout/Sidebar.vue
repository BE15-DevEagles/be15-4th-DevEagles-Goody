<template>
  <aside
    class="bg-[var(--color-gray-700)] h-full w-[190px] flex flex-col shadow-drop rounded-l-2xl"
  >
    <!-- 워크스페이스 헤더 -->
    <div class="p-3 flex items-center border-b border-[var(--color-gray-600)]">
      <h1 class="text-white font-section-inner">{{ currentTeamName || '팀 이름' }}</h1>
    </div>

    <div class="overflow-y-auto flex-grow">
      <!-- 홈 영역 -->
      <div class="px-3 py-4">
        <h2 class="text-[var(--color-gray-400)] font-small-semibold uppercase px-2 mb-2">
          워크스페이스
        </h2>
        <ul>
          <li
            v-for="(item, index) in workspaceItems"
            :key="index"
            class="flex items-center px-3 py-2 rounded-lg mb-1 cursor-pointer transition-all duration-300 ease-out relative group"
            :class="
              isActiveRoute(item.route)
                ? 'bg-gradient-to-r from-[var(--color-primary-400)] to-[var(--color-primary-500)] text-white shadow-lg transform scale-[1.02]'
                : 'text-[var(--color-gray-300)] hover:bg-[var(--color-gray-600)] hover:text-white hover:transform hover:scale-[1.01] hover:shadow-md'
            "
            @click="handleWorkspaceClick(item)"
          >
            <!-- 활성 상태 표시기 -->
            <div
              v-if="isActiveRoute(item.route)"
              class="absolute left-0 top-1/2 transform -translate-y-1/2 w-1 h-6 bg-white rounded-r-full shadow-lg"
            ></div>

            <span
              class="w-5 h-5 mr-3 flex-shrink-0 transition-all duration-300"
              :class="isActiveRoute(item.route) ? 'scale-110' : 'group-hover:scale-110'"
              v-html="item.icon"
            ></span>
            <span class="font-one-liner truncate">{{ item.name }}</span>

            <!-- 호버 글로우 효과 -->
            <div
              v-if="!isActiveRoute(item.route)"
              class="absolute inset-0 rounded-lg bg-gradient-to-r from-[var(--color-primary-400)]/0 to-[var(--color-primary-500)]/0 group-hover:from-[var(--color-primary-400)]/10 group-hover:to-[var(--color-primary-500)]/10 transition-all duration-300"
            ></div>
          </li>
        </ul>
      </div>

      <!-- 팀 채널 영역 -->
      <div class="px-3 py-2">
        <div class="flex items-center px-2 mb-2">
          <h2 class="text-[var(--color-gray-400)] font-small-semibold uppercase">팀 채널</h2>
        </div>
        <ul>
          <li v-for="(channel, index) in channels" :key="index" class="flex flex-col">
            <div
              class="flex items-center px-3 py-2 rounded-lg mb-1 cursor-pointer transition-all duration-300 ease-out relative group"
              :class="
                isActiveChannel(channel)
                  ? 'bg-gradient-to-r from-[var(--color-primary-400)] to-[var(--color-primary-500)] text-white shadow-lg transform scale-[1.02]'
                  : 'text-[var(--color-gray-300)] hover:bg-[var(--color-gray-600)] hover:text-white hover:transform hover:scale-[1.01] hover:shadow-md'
              "
              @click="
                channel.name === '타임캡슐' ? toggleTimecapsuleMenu() : handleChannelClick(channel)
              "
            >
              <!-- 활성 상태 표시기 -->
              <div
                v-if="isActiveChannel(channel)"
                class="absolute left-0 top-1/2 transform -translate-y-1/2 w-1 h-6 bg-white rounded-r-full shadow-lg"
              ></div>

              <span
                class="mr-3 font-one-liner transition-all duration-300"
                :class="isActiveChannel(channel) ? 'scale-110' : 'group-hover:scale-110'"
                >#</span
              >
              <span class="font-one-liner truncate">{{ channel.name }}</span>
              <span
                v-if="channel.name === '타임캡슐'"
                class="ml-auto transition-all duration-300"
                :class="timecapsuleOpen ? 'rotate-180' : 'group-hover:scale-110'"
              >
                <svg width="16" height="16" fill="currentColor">
                  <path d="M4 6l4 4 4-4" />
                </svg>
              </span>

              <!-- 호버 글로우 효과 -->
              <div
                v-if="!isActiveChannel(channel)"
                class="absolute inset-0 rounded-lg bg-gradient-to-r from-[var(--color-primary-400)]/0 to-[var(--color-primary-500)]/0 group-hover:from-[var(--color-primary-400)]/10 group-hover:to-[var(--color-primary-500)]/10 transition-all duration-300"
              ></div>
            </div>
            <!-- 타임캡슐 하위 메뉴 -->
            <ul
              v-if="channel.name === '타임캡슐' && timecapsuleOpen"
              class="ml-6 overflow-hidden transition-all duration-300 ease-out"
            >
              <li
                class="flex items-center px-3 py-2 rounded-lg mb-1 cursor-pointer transition-all duration-300 ease-out font-one-liner truncate relative group"
                :class="
                  isActiveRoute('/timecapsule/create')
                    ? 'bg-gradient-to-r from-[var(--color-primary-300)] to-[var(--color-primary-400)] text-white shadow-md transform scale-[1.01]'
                    : 'text-[var(--color-gray-300)] hover:bg-[var(--color-gray-600)] hover:text-white hover:transform hover:scale-[1.01]'
                "
                @click.stop="goRoute('/timecapsule/create')"
              >
                <div
                  v-if="isActiveRoute('/timecapsule/create')"
                  class="absolute left-0 top-1/2 transform -translate-y-1/2 w-0.5 h-4 bg-white rounded-r-full"
                ></div>
                타임캡슐 생성
              </li>
              <li
                class="flex items-center px-3 py-2 rounded-lg mb-1 cursor-pointer transition-all duration-300 ease-out font-one-liner truncate relative group"
                :class="
                  isActiveRoute('/timecapsule/list')
                    ? 'bg-gradient-to-r from-[var(--color-primary-300)] to-[var(--color-primary-400)] text-white shadow-md transform scale-[1.01]'
                    : 'text-[var(--color-gray-300)] hover:bg-[var(--color-gray-600)] hover:text-white hover:transform hover:scale-[1.01]'
                "
                @click.stop="goRoute('/timecapsule/list')"
              >
                <div
                  v-if="isActiveRoute('/timecapsule/list')"
                  class="absolute left-0 top-1/2 transform -translate-y-1/2 w-0.5 h-4 bg-white rounded-r-full"
                ></div>
                타임캡슐 조회
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </aside>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useTeamStore } from '@/store/team.js';

  const router = useRouter();
  const route = useRoute();
  const teamStore = useTeamStore();
  const teamId = computed(() => teamStore.currentTeamId);
  const currentTeamName = computed(() => {
    const currentTeam = teamStore.teams.find(team => team.teamId === teamStore.currentTeamId);
    return currentTeam?.teamName;
  });

  // 아이콘 SVG 문자열
  const homeIcon = `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
  </svg>`;

  const projectIcon = `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
  </svg>`;

  const calendarIcon = `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
  </svg>`;

  // Mock 데이터
  const workspaceItems = ref([
    { name: '홈', icon: homeIcon, route: '/', active: false },
    { name: '내 캘린더', icon: calendarIcon, route: '/calendar/my', active: false },
  ]);

  function handleWorkspaceClick(item) {
    if (item.route) {
      router.push(item.route);
    }
  }

  function handleChannelClick(channel) {
    switch (channel.name) {
      case '팀 정보':
        router.push(`/team/info/${teamId.value}`);
        break;
      case '캘린더':
        router.push('/calendar/team');
        break;
      case '업무일지':
        router.push('/worklog/my');
        break;
      case 'Todo 목록':
        router.push({ path: '/todos', query: { status: 'all' } });
        break;
      case '타임캡슐':
        router.push('/timecapsule');
        break;
      case '룰렛':
        router.push('/roulette');
        break;
      default:
        console.warn('❓ 알 수 없는 채널:', channel.name);
    }
  }

  const channels = ref([
    { name: '팀 정보', icon: projectIcon, active: false },
    { name: '캘린더', icon: calendarIcon, active: false },
    { name: '업무일지', active: false },
    { name: 'Todo 목록', active: false },
    { name: '타임캡슐', active: false },
    { name: '룰렛', active: false },
  ]);

  const timecapsuleOpen = ref(false);
  function toggleTimecapsuleMenu() {
    timecapsuleOpen.value = !timecapsuleOpen.value;
  }

  function goRoute(route) {
    router.push(route);
  }

  // 현재 라우트가 활성 상태인지 확인
  function isActiveRoute(targetRoute) {
    if (!targetRoute) return false;

    // 홈 경로는 정확히 일치할 때만 활성화
    if (targetRoute === '/') {
      return route.path === '/';
    }

    // 다른 경로는 시작 문자열로 매칭
    return route.path === targetRoute || route.path.startsWith(targetRoute + '/');
  }

  // 채널이 활성 상태인지 확인
  function isActiveChannel(channel) {
    switch (channel.name) {
      case '팀 정보':
        return route.path.includes('/team/info');
      case '캘린더':
        return route.path.includes('/calendar/team');
      case '업무일지':
        return route.path.includes('/worklog');
      case 'Todo 목록':
        return route.path.includes('/todos');
      case '타임캡슐':
        return route.path.includes('/timecapsule');
      case '룰렛':
        return route.path.includes('/roulette');
      default:
        return false;
    }
  }
</script>

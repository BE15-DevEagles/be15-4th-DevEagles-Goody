<script setup>
  import { ref, onMounted, watch, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import WorkLog from '@/features/worklog/components/WorkLog.vue';
  import Pagination from '@/components/common/components/Pagaination.vue';
  import { searchWorklogs, fetchMyWorklogs } from '@/features/worklog/api/worklog.js';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { useAuthStore } from '@/store/auth.js';
  import { useTeamStore } from '@/store/team.js';
  import { format } from 'date-fns';
  import { DatePicker } from 'v-calendar';
  import 'v-calendar/style.css';

  const teamStore = useTeamStore();
  const authStore = useAuthStore();
  const router = useRouter();

  const worklogs = ref([]);
  const searchType = ref('all');
  const searchInput = ref('');
  const sortType = ref('latest');
  const dateRange = ref({ start: new Date(2020, 0, 1), end: new Date() });
  const currentPage = ref(1);
  const totalPages = ref(0);
  const worklogScope = ref('mine');
  const pageSize = 10;
  const teamId = teamStore.currentTeamId;
  const showDatePicker = ref(false);

  const displayDateRange = computed(() => {
    const { start, end } = dateRange.value;
    if (!start || !end) return '';
    return `${format(start, 'yyyy.MM.dd')} ~ ${format(end, 'yyyy.MM.dd')}`;
  });

  function goToCreatePage() {
    router.push({
      name: 'WorklogCreate',
      query: { username: authStore.name, teamId },
    });
  }

  function formatDateTime(date) {
    return date ? format(date, 'yyyy-MM-dd HH:mm:ss') : null;
  }

  function isSearchMode() {
    return (
      searchInput.value.trim() !== '' ||
      (dateRange.value.start && dateRange.value.end) ||
      searchType.value !== 'all'
    );
  }

  function clearDates() {
    dateRange.value = { start: new Date(2020, 0, 1), end: new Date() };
    triggerSearch();
  }

  function switchScope(scope) {
    if (worklogScope.value !== scope) {
      worklogScope.value = scope;
      currentPage.value = 1;
      fetchWorklogs();
    }
  }

  function triggerSearch() {
    currentPage.value = 1;
    fetchWorklogs();
  }

  function onPageChange(page) {
    if (page !== currentPage.value) {
      currentPage.value = page;
      fetchWorklogs();
    }
  }

  function goToDetail(log) {
    router.push({ name: 'WorklogDetail', params: { id: log.worklogId } });
  }

  async function fetchWorklogs() {
    try {
      const commonParams = {
        teamId,
        page: currentPage.value,
        size: pageSize,
        sort: sortType.value,
      };

      if (isSearchMode()) {
        const request = {
          ...commonParams,
          searchType: searchType.value.toUpperCase(),
          keyword: searchInput.value,
          startDate: formatDateTime(dateRange.value.start),
          endDate: formatDateTime(dateRange.value.end),
        };
        const response = await searchWorklogs(request);
        const data = response?.data?.data;
        if (!data) throw new Error('응답 데이터 없음');
        const { content, pagination } = data;
        worklogs.value = content;
        totalPages.value = Math.ceil(pagination.totalItems / pageSize);
      } else {
        const url = worklogScope.value === 'mine' ? '/myworklog' : '/team';
        const response = await fetchMyWorklogs('/worklog' + url, commonParams);
        const data = response?.data?.data;
        if (!data) throw new Error('응답 데이터 없음');
        const { content, pagination } = data;
        worklogs.value = content;
        totalPages.value = Math.ceil(pagination.totalItems / pageSize);
      }
    } catch (error) {
      console.error('업무일지 로드 실패:', error);
    }
  }

  onMounted(() => {
    dateRange.value = { start: new Date(2020, 0, 1), end: new Date() };
    fetchWorklogs();
  });

  watch(sortType, () => {
    currentPage.value = 1;
    fetchWorklogs();
  });

  watch(dateRange, newVal => {
    if (newVal.start && newVal.end) {
      if (newVal.start > newVal.end) {
        const tmp = newVal.start;
        dateRange.value.start = newVal.end;
        dateRange.value.end = tmp;
      }
      triggerSearch();
    }
  });
</script>

<template>
  <section class="p-4">
    <div class="d-flex gap-2 mb-3 justify-content-between align-items-center">
      <BaseButton class="btn btn-accent" @click="goToCreatePage">업무일지 작성</BaseButton>
      <div class="d-flex gap-2">
        <BaseButton
          class="btn tab-toggle"
          :class="{ selected: worklogScope === 'team' }"
          @click="switchScope('team')"
          >팀별 업무일지</BaseButton
        >
        <BaseButton
          class="btn tab-toggle"
          :class="{ selected: worklogScope === 'mine' }"
          @click="switchScope('mine')"
          >내 업무일지</BaseButton
        >
      </div>
    </div>

    <div class="d-flex align-items-center search-box">
      <select v-model="searchType" class="input select-type">
        <option value="all">전체</option>
        <option value="author">작성자</option>
        <option value="keyword">키워드</option>
      </select>
      <div class="position-relative w-100" style="height: 100%">
        <input
          v-model.trim="searchInput"
          type="text"
          class="input w-100"
          placeholder="검색어 입력"
          style="border: none; border-radius: 0; height: 100%; padding-right: 2.5rem"
          @keyup.enter="triggerSearch"
        />
        <span class="search-icon" @click="triggerSearch">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            style="width: 16px; height: 16px"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </span>
      </div>
    </div>

    <div class="d-flex align-items-center justify-content-between flex-wrap">
      <!-- 왼쪽: 최신순 정렬 -->
      <div class="d-flex align-items-center">
        <select v-model="sortType" class="input" style="width: 7.2rem; height: 42px">
          <option value="latest">최신순</option>
          <option value="created">등록순</option>
        </select>
      </div>

      <!-- 가운데: 날짜 범위 표시 -->
      <div class="d-flex justify-content-center flex-grow-1">
        <div class="selected-range-label text-nowrap">{{ displayDateRange }}</div>
      </div>

      <!-- 오른쪽: 날짜 선택 및 초기화 버튼 -->
      <div class="d-flex align-items-center gap-2">
        <BaseButton
          class="btn-secondary btn-date-picker-toggle"
          @click="showDatePicker = !showDatePicker"
        >
          날짜 선택
        </BaseButton>
        <BaseButton class="btn-initialize" @click="clearDates">초기화</BaseButton>
      </div>
    </div>

    <div v-if="showDatePicker" class="calendar-container mt-2">
      <DatePicker
        v-model="dateRange"
        is-range
        :columns="2"
        :min-date="new Date(2020, 0, 1)"
        :max-date="new Date()"
        :masks="{ input: 'YYYY-MM-DD' }"
        :popover="{ placement: 'bottom-start' }"
      />
    </div>

    <div class="table-responsive">
      <table class="table table-striped text-center w-100" style="table-layout: fixed">
        <thead>
          <tr>
            <th style="width: 20%">작성자</th>
            <th style="width: 60%">제목</th>
            <th style="width: 20%">작성일자</th>
          </tr>
        </thead>
        <tbody>
          <WorkLog
            v-for="log in worklogs"
            :key="log.worklogId"
            :log="log"
            @click="goToDetail(log)"
          />
          <tr v-if="worklogs.length === 0">
            <td colspan="3" class="text-center text-gray">조회된 업무일지가 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="mt-5 d-flex justify-content-center">
      <Pagination
        :key="`${totalPages}-${currentPage}`"
        :current-page="currentPage"
        :total-pages="totalPages"
        @update:current-page="onPageChange"
      />
    </div>
  </section>
</template>

<style scoped>
  .btn-date-picker-toggle {
    height: 32px;
    font-size: 14px;
    padding: 0 12px;
    min-width: 8rem;
  }

  .selected-range-label {
    font-weight: 500;
    font-size: 14px;
    width: 14rem;
    color: var(--color-gray-600);
  }

  .tab-toggle {
    border: 1px solid var(--color-primary-main);
    background: transparent;
    color: var(--color-primary-main);
  }
  .tab-toggle.selected {
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
  }

  .search-box {
    border: 1px solid var(--color-gray-300);
    border-radius: 0.5rem;
    overflow: hidden;
    height: 42px;
    min-width: 400px;
    flex-grow: 1;
  }

  .select-type {
    border: none;
    border-right: 1px solid var(--color-gray-300);
    border-radius: 0;
    height: 100%;
    max-width: 7rem;
  }

  .search-icon {
    position: absolute;
    right: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: var(--color-gray-400);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .btn-accent {
    background-color: var(--color-primary-500);
    color: var(--color-neutral-white);
    border: none;
  }

  .btn-accent:hover {
    background-color: var(--color-primary-400);
  }

  .btn-initialize {
    background-color: var(--color-primary-main);
    color: var(--color-neutral-white);
    padding: 8px 36px;
    font-size: 13px;
    font-weight: 700;
    border-radius: 0.5rem;
    white-space: nowrap;
  }
</style>

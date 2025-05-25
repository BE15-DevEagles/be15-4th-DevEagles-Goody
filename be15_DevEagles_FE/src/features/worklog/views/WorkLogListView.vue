<script setup>
  import { ref, onMounted, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import WorkLog from '@/features/worklog/components/WorkLog.vue';
  import Pagination from '@/components/common/components/Pagaination.vue';
  import { searchWorklogs, fetchMyWorklogs } from '@/features/worklog/api/worklog.js';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { useAuthStore } from '@/store/auth.js';
  import { useTeamStore } from '@/store/team.js';
  import VueDatePicker from '@vuepic/vue-datepicker';
  import '@vuepic/vue-datepicker/dist/main.css';
  import { format } from 'date-fns';

  const teamStore = useTeamStore();
  const authStore = useAuthStore();
  const router = useRouter();

  const worklogs = ref([]);
  const searchType = ref('all');
  const searchInput = ref('');
  const sortType = ref('latest');
  const dateRange = ref([null, null]);
  const currentPage = ref(1);
  const totalPages = ref(0);
  const worklogScope = ref('mine');
  const pageSize = 10;
  const teamId = teamStore.currentTeamId;

  function goToCreatePage() {
    router.push({
      name: 'WorklogCreate',
      query: {
        username: authStore.name,
        teamId: teamId,
      },
    });
  }

  function formatDateTime(date) {
    return date ? format(date, 'yyyy-MM-dd HH:mm:ss') : null;
  }

  function isSearchMode() {
    return (
      searchInput.value.trim() !== '' ||
      (dateRange.value[0] && dateRange.value[1]) ||
      searchType.value !== 'all'
    );
  }

  function clearDates() {
    dateRange.value = [new Date(2020, 0, 1), new Date()];
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
        const [start, end] = dateRange.value;
        const request = {
          ...commonParams,
          searchType: searchType.value.toUpperCase(),
          keyword: searchInput.value,
          startDate: formatDateTime(start),
          endDate: formatDateTime(end),
        };
        const response = await searchWorklogs(request);
        const { content, pagination } = response.data.data;
        worklogs.value = content;
        totalPages.value = Math.ceil(pagination.totalItems / pageSize);
      } else {
        const url = worklogScope.value === 'mine' ? '/myworklog' : '/team';
        const response = await fetchMyWorklogs('/worklog' + url, commonParams);
        const { content, pagination } = response.data.data;
        worklogs.value = content;
        totalPages.value = Math.ceil(pagination.totalItems / pageSize);
      }
    } catch (error) {
      console.error('업무일지 로드 실패:', error);
    }
  }

  // ✅ 초기 로딩 시 오늘 날짜로 설정 & 호출
  onMounted(() => {
    dateRange.value = [new Date(2020, 0, 1), new Date()];
    fetchWorklogs();
  });

  // ✅ 정렬 바뀔 때
  watch(sortType, () => {
    currentPage.value = 1;
    fetchWorklogs();
  });

  // ✅ 날짜 선택 시 자동 검색 & 순서 정리
  watch(dateRange, newVal => {
    if (newVal[0] && newVal[1]) {
      if (newVal[0] > newVal[1]) {
        dateRange.value = [newVal[1], newVal[0]];
      }
      triggerSearch();
    }
  });
</script>

<template>
  <section class="p-4">
    <!-- 작성 + 탭 -->
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

    <!-- 검색 + 정렬 + 달력 -->
    <div class="d-flex flex-column gap-2 mb-3">
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

      <div class="d-flex align-items-center justify-content-between gap-2">
        <div class="d-flex align-items-center gap-2">
          <select v-model="sortType" class="input" style="min-width: 7rem; height: 42px">
            <option value="latest">최신순</option>
            <option value="created">등록순</option>
          </select>

          <VueDatePicker
            v-model="dateRange"
            range
            :clearable="true"
            :min-date="new Date(2020, 0, 1)"
            :max-date="new Date()"
            :enable-time-picker="false"
            :start-date="new Date()"
            placeholder="날짜 선택"
            class="datepicker-input"
            :format="'yyyy-MM-dd'"
            :preview-format="'yyyy-MM-dd'"
            teleport="body"
          />
        </div>

        <BaseButton class="btn-initialize" @click="clearDates">초기화</BaseButton>
      </div>
    </div>

    <!-- 테이블 -->
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

  .datepicker-input {
    height: 42px;
    width: 100%;
    min-width: 300px;
    padding: 0 12px;
    font-size: 14px;
    font-weight: 400;
    box-sizing: border-box;
    background-color: transparent;
    border: 1px solid var(--color-gray-300);
    border-radius: 0.5rem;
  }

  .btn-initialize {
    background-color: var(--color-primary-main);
    color: var(--color-neutral-white);
    padding: 6px 12px;
    font-size: 13px;
    font-weight: 700;
    border-radius: 0.5rem;
    white-space: nowrap;
  }

  ::v-deep(.dp__main) {
    border: none !important;
    box-shadow: none !important;
    background-color: transparent !important;
    padding: 0 !important;
  }

  /* 아이콘 padding 없애기 */
  ::v-deep(.dp__input_wrap) {
    border: 1px solid var(--color-gray-300) !important;
    border-radius: 0.5rem !important;
    background-color: var(--color-neutral-white) !important;
    padding: 8px 12px 8px 60px !important;
    display: flex;
    align-items: center;
    height: 42px;
    box-sizing: border-box;
    position: relative;
  }

  ::v-deep(.dp__input) {
    border: none !important;
    background: transparent !important;
    padding: 0 !important;
    font-size: 14px;
    font-weight: 500;
    color: var(--color-neutral-dark);
    width: 100%;
    padding-left: 100px !important; /* 아이콘과 날짜 텍스트 간 미세 간격 확보 */
  }

  ::v-deep(.dp__input_icon_pad) {
    padding-left: 0 !important;
  }

  ::v-deep(.dp__input_icon) {
    position: absolute !important;
    z-index: 1;
    pointer-events: none;
    color: var(--color-gray-500);
    display: flex;
    align-items: center;
    font-size: 16px;
  }
</style>

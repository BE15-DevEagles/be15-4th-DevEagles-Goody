<script setup>
  import { ref, onMounted, watch, computed } from 'vue';
  import { useRouter } from 'vue-router';
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

  // 통계 데이터
  const stats = computed(() => ({
    total: worklogs.value.length,
    thisWeek: worklogs.value.filter(log => {
      const logDate = new Date(log.writtenAt);
      const weekAgo = new Date();
      weekAgo.setDate(weekAgo.getDate() - 7);
      return logDate >= weekAgo;
    }).length,
    thisMonth: worklogs.value.filter(log => {
      const logDate = new Date(log.writtenAt);
      const monthAgo = new Date();
      monthAgo.setMonth(monthAgo.getMonth() - 1);
      return logDate >= monthAgo;
    }).length,
  }));

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
  <div class="worklog-container">
    <!-- 헤더 섹션 -->
    <div class="header-section">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">업무일지 현황</h1>
          <p class="page-subtitle">이번 달 현황</p>
        </div>
        <div class="header-actions">
          <BaseButton class="btn-create" @click="goToCreatePage">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 4v16m8-8H4"
              ></path>
            </svg>
            업무일지 작성
          </BaseButton>
        </div>
      </div>

      <!-- 통계 카드 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon total">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              ></path>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.total }}</div>
            <div class="stat-label">전체 업무일지</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon week">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              ></path>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.thisWeek }}</div>
            <div class="stat-label">이번 주</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon month">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"
              ></path>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.thisMonth }}</div>
            <div class="stat-label">이번 달</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 필터 및 검색 섹션 -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="scope-tabs">
          <button
            class="scope-tab"
            :class="{ active: worklogScope === 'mine' }"
            @click="switchScope('mine')"
          >
            내 업무일지
          </button>
          <button
            class="scope-tab"
            :class="{ active: worklogScope === 'team' }"
            @click="switchScope('team')"
          >
            팀 업무일지
          </button>
        </div>

        <div class="view-controls">
          <select v-model="sortType" class="sort-select">
            <option value="latest">최신순</option>
            <option value="created">등록순</option>
          </select>
        </div>
      </div>

      <div class="search-controls">
        <div class="search-box">
          <select v-model="searchType" class="search-type">
            <option value="all">전체</option>
            <option value="author">작성자</option>
            <option value="keyword">키워드</option>
          </select>
          <div class="search-input-wrapper">
            <input
              v-model.trim="searchInput"
              type="text"
              class="search-input"
              placeholder="검색어를 입력하세요"
              @keyup.enter="triggerSearch"
            />
            <button class="search-btn" @click="triggerSearch">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                ></path>
              </svg>
            </button>
          </div>
        </div>

        <div class="date-controls">
          <div class="date-range-display">{{ displayDateRange }}</div>
          <div class="date-actions">
            <button class="date-btn" @click="showDatePicker = !showDatePicker">
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                ></path>
              </svg>
              날짜 선택
            </button>
            <button class="date-btn secondary" @click="clearDates">초기화</button>
          </div>
        </div>
      </div>

      <!-- 날짜 선택 팝업 -->
      <div v-if="showDatePicker" class="date-picker-popup">
        <DatePicker
          v-model="dateRange"
          is-range
          :columns="2"
          :min-date="new Date(2020, 0, 1)"
          :max-date="new Date()"
          :masks="{ input: 'YYYY-MM-DD' }"
        />
      </div>
    </div>

    <!-- 컨텐츠 섹션 -->
    <div class="content-section">
      <!-- 리스트 뷰 -->
      <div class="list-container">
        <div class="list-header">
          <div class="header-col author">작성자</div>
          <div class="header-col title">제목</div>
          <div class="header-col date">작성일자</div>
          <div class="header-col action">액션</div>
        </div>
        <div class="list-body">
          <div
            v-for="log in worklogs"
            :key="log.worklogId"
            class="list-item"
            @click="goToDetail(log)"
          >
            <div class="item-col author">
              <div class="author-info">
                <div class="author-avatar">{{ log.userName.charAt(0) }}</div>
                <span class="author-name">{{ log.userName }}</span>
              </div>
            </div>
            <div class="item-col title">
              <div class="worklog-title">{{ log.summary }}</div>
              <div class="worklog-preview">{{ log.workContent?.substring(0, 60) }}...</div>
            </div>
            <div class="item-col date">
              <div class="date-primary">{{ log.writtenAt?.slice(0, 10) }}</div>
              <div class="date-time">{{ log.writtenAt?.slice(11, 16) }}</div>
            </div>
            <div class="item-col action">
              <button class="action-btn view-btn" title="상세보기" @click.stop="goToDetail(log)">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  ></path>
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                  ></path>
                </svg>
              </button>
            </div>
          </div>
          <div v-if="worklogs.length === 0" class="empty-list">
            <div class="empty-icon">
              <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="1"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                ></path>
              </svg>
            </div>
            <h3 class="empty-title">업무일지가 없습니다</h3>
            <p class="empty-description">첫 번째 업무일지를 작성해보세요</p>
            <BaseButton class="btn-create" @click="goToCreatePage">업무일지 작성</BaseButton>
          </div>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="pagination-container">
        <Pagination
          :key="`${totalPages}-${currentPage}`"
          :current-page="currentPage"
          :total-pages="totalPages"
          @update:current-page="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
  .worklog-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
    background-color: var(--color-neutral-white);
    min-height: 100vh;
  }

  /* 헤더 섹션 */
  .header-section {
    margin-bottom: 32px;
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
  }

  .title-section {
    flex: 1;
  }

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--color-neutral-dark);
    margin: 0 0 8px 0;
  }

  .page-subtitle {
    font-size: 16px;
    color: var(--color-gray-500);
    margin: 0;
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }

  .btn-create {
    display: flex;
    align-items: center;
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    border: none;
    padding: 12px 24px;
    border-radius: 12px;
    font-weight: 600;
    transition: all 0.2s ease;
    box-shadow: 0 4px 12px rgba(37, 113, 128, 0.3);
  }

  .btn-create:hover {
    transform: translateY(-2px);
    background: var(--color-primary-300);
    box-shadow: 0 6px 20px rgba(37, 113, 128, 0.4);
  }

  /* 통계 카드 */
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 20px;
    margin-bottom: 32px;
  }

  .stat-card {
    background: var(--color-neutral-white);
    border-radius: 16px;
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--color-gray-200);
    transition: all 0.2s ease;
  }

  .stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-neutral-white);
  }

  .stat-icon.total {
    background: var(--color-primary-main);
  }
  .stat-icon.week {
    background: var(--color-secondary-main);
  }
  .stat-icon.month {
    background: var(--color-info-500);
  }

  .stat-content {
    flex: 1;
  }

  .stat-number {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-neutral-dark);
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 14px;
    color: var(--color-gray-500);
  }

  /* 필터 섹션 */
  .filter-section {
    background: var(--color-neutral-white);
    border-radius: 16px;
    padding: 24px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--color-gray-200);
  }

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .scope-tabs {
    display: flex;
    background: var(--color-gray-100);
    border-radius: 12px;
    padding: 4px;
  }

  .scope-tab {
    padding: 8px 16px;
    border: none;
    background: transparent;
    border-radius: 8px;
    font-weight: 500;
    color: var(--color-gray-500);
    transition: all 0.2s ease;
    cursor: pointer;
  }

  .scope-tab.active {
    background: var(--color-neutral-white);
    color: var(--color-primary-main);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .view-controls {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .sort-select {
    padding: 8px 12px;
    border: 1px solid var(--color-gray-200);
    border-radius: 8px;
    background: var(--color-neutral-white);
    color: var(--color-gray-700);
    font-size: 14px;
  }

  .search-controls {
    display: flex;
    gap: 16px;
    align-items: center;
    flex-wrap: wrap;
  }

  .search-box {
    display: flex;
    flex: 1;
    min-width: 300px;
    border: 1px solid var(--color-gray-200);
    border-radius: 12px;
    overflow: hidden;
    background: var(--color-neutral-white);
  }

  .search-type {
    padding: 12px 16px;
    border: none;
    border-right: 1px solid var(--color-gray-200);
    background: var(--color-gray-50);
    color: var(--color-gray-700);
    font-size: 14px;
    min-width: 100px;
  }

  .search-input-wrapper {
    display: flex;
    flex: 1;
    position: relative;
  }

  .search-input {
    flex: 1;
    padding: 12px 16px;
    border: none;
    outline: none;
    font-size: 14px;
    color: var(--color-gray-700);
  }

  .search-btn {
    padding: 12px;
    border: none;
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    cursor: pointer;
    transition: background 0.2s ease;
  }

  .search-btn:hover {
    background: var(--color-primary-300);
  }

  .date-controls {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .date-range-display {
    font-size: 14px;
    color: var(--color-gray-700);
    font-weight: 500;
    min-width: 200px;
  }

  .date-actions {
    display: flex;
    gap: 8px;
  }

  .date-btn {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    border: 1px solid var(--color-primary-main);
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .date-btn:hover {
    background: var(--color-primary-300);
    border-color: var(--color-primary-300);
  }

  .date-btn.secondary {
    background: var(--color-neutral-white);
    color: var(--color-primary-main);
  }

  .date-btn.secondary:hover {
    background: var(--color-gray-50);
  }

  .date-picker-popup {
    margin-top: 16px;
    padding: 16px;
    background: var(--color-neutral-white);
    border: 1px solid var(--color-gray-200);
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }

  /* 컨텐츠 섹션 */
  .content-section {
    background: var(--color-neutral-white);
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--color-gray-200);
  }

  /* 리스트 뷰 */
  .list-container {
    border: 1px solid var(--color-gray-200);
    border-radius: 12px;
    overflow: hidden;
  }

  .list-header {
    display: grid;
    grid-template-columns: 20% 50% 20% 10%;
    background: var(--color-gray-50);
    border-bottom: 2px solid var(--color-gray-200);
  }

  .header-col {
    padding: 16px;
    font-weight: 600;
    color: var(--color-gray-700);
    font-size: 14px;
    text-align: center;
  }

  .header-col.title {
    text-align: left;
  }

  .list-body {
    background: var(--color-neutral-white);
  }

  .list-item {
    display: grid;
    grid-template-columns: 20% 50% 20% 10%;
    border-bottom: 1px solid var(--color-gray-200);
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .list-item:hover {
    background: var(--color-gray-50);
  }

  .list-item:last-child {
    border-bottom: none;
  }

  .item-col {
    padding: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .item-col.title {
    justify-content: flex-start;
    flex-direction: column;
    align-items: flex-start;
  }

  .item-col.author {
    justify-content: flex-start;
  }

  .author-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .author-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
  }

  .author-name {
    font-weight: 600;
    color: var(--color-neutral-dark);
    font-size: 14px;
  }

  .worklog-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-neutral-dark);
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 100%;
  }

  .worklog-preview {
    font-size: 12px;
    color: var(--color-gray-500);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 100%;
  }

  .date-primary {
    font-weight: 600;
    color: var(--color-gray-700);
    font-size: 14px;
    margin-bottom: 2px;
  }

  .date-time {
    font-size: 12px;
    color: var(--color-gray-500);
  }

  .action-btn {
    width: 32px;
    height: 32px;
    border: none;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .view-btn {
    background: var(--color-primary-50);
    color: var(--color-primary-main);
  }

  .view-btn:hover {
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
  }

  .empty-list {
    text-align: center;
    padding: 60px 20px;
  }

  .empty-list .empty-icon {
    color: var(--color-gray-300);
    margin-bottom: 16px;
  }

  .empty-list .empty-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-gray-600);
    margin: 0 0 8px 0;
  }

  .empty-list .empty-description {
    font-size: 14px;
    color: var(--color-gray-500);
    margin: 0 0 24px 0;
  }

  /* 페이지네이션 */
  .pagination-container {
    margin-top: 32px;
    display: flex;
    justify-content: center;
  }

  /* 반응형 */
  @media (max-width: 768px) {
    .worklog-container {
      padding: 16px;
    }

    .header-content {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;
    }

    .stats-grid {
      grid-template-columns: 1fr;
    }

    .filter-header {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;
    }

    .search-controls {
      flex-direction: column;
      align-items: stretch;
    }

    .list-header,
    .list-item {
      grid-template-columns: 1fr;
      gap: 8px;
    }

    .header-col,
    .item-col {
      padding: 8px 16px;
      text-align: left;
      justify-content: flex-start;
    }

    .item-col.title {
      order: 1;
    }

    .item-col.author {
      order: 2;
    }

    .item-col.date {
      order: 3;
    }

    .item-col.action {
      order: 4;
      justify-content: flex-end;
    }
  }
</style>

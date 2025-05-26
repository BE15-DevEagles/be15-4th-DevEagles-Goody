<template>
  <div class="page-container">
    <div class="card">
      <h1 class="font-screen-title mb-4">TodoList</h1>

      <!-- 필터 탭 -->
      <div class="filter-section">
        <div class="filter-tabs">
          <BaseButton
            v-for="type in ['전체', '미완료', '완료']"
            :key="type"
            :type="selectedStatus === mapTabToStatus(type) ? 'primary' : 'secondary'"
            :outline="selectedStatus !== mapTabToStatus(type)"
            size="sm"
            @click="selectStatus(type)"
          >
            {{ type }}
          </BaseButton>
        </div>

        <!-- 팀 멤버 필터 -->
        <div class="member-filter">
          <span class="font-small-semibold text-gray">멤버 필터:</span>
          <div class="member-avatars">
            <div
              v-for="member in teamMembers"
              :key="member.userId"
              class="member-avatar"
              :class="{ selected: selectedUserId === Number(member.userId) }"
              :title="member.userName"
              @click="toggleUser(Number(member.userId))"
            >
              <template v-if="member.userThumbnailUrl">
                <div
                  class="avatar-image"
                  :style="{ backgroundImage: `url(${member.userThumbnailUrl})` }"
                ></div>
              </template>
              <template v-else>
                <div class="avatar-initial">
                  {{ member.userName?.charAt(0) || '?' }}
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 목록 테이블 -->
      <div class="table-container">
        <table class="table">
          <colgroup>
            <col style="width: 15%" />
            <col style="width: 70%" />
            <col style="width: 15%" />
          </colgroup>
          <thead>
            <tr>
              <th class="font-small-semibold">작성자</th>
              <th class="font-small-semibold">내용</th>
              <th class="font-small-semibold">디데이</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="todo in todos" :key="todo.todoId" class="table-row">
              <td class="font-one-liner">{{ todo.userName }}</td>
              <td class="font-one-liner todo-content">{{ todo.content }}</td>
              <td>
                <span class="badge" :class="getDdayBadgeClass(todo.dueDate)">
                  {{ getDday(todo.dueDate) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-wrapper">
        <BasePagination
          :current-page="currentPage"
          :total-pages="totalPages"
          @update:current-page="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { storeToRefs } from 'pinia';
  import { fetchTeamTodos } from '@/features/todolist/api/api.js';
  import { useTeamStore } from '@/store/team';
  import BasePagination from '@/components/common/components/Pagaination.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const route = useRoute();
  const teamStore = useTeamStore();
  const { currentTeamId } = storeToRefs(teamStore);

  const todos = ref([]);
  const selectedUserId = ref([]);

  const teamMembers = computed(() => teamStore.teamMembers);
  const validStatuses = ['all', 'completed', 'incomplete'];
  const selectedStatus = ref(
    validStatuses.includes(route.query.status) ? route.query.status : 'all'
  );

  const currentPage = ref(1);
  const totalPages = ref(1);

  const onPageChange = page => {
    currentPage.value = page;
    fetchTodos();
  };

  const fetchTodos = async () => {
    if (!currentTeamId.value) return;
    const { data } = await fetchTeamTodos({
      teamId: currentTeamId.value,
      userId: selectedUserId.value,
      status: selectedStatus.value,
      page: currentPage.value,
      size: 10,
    });
    todos.value = data.data.content;
    totalPages.value = data.data.pagination.totalPages;
  };

  const getDday = dueDateStr => {
    const today = new Date();
    const due = new Date(dueDateStr);
    const diff = Math.ceil((due - today) / (1000 * 60 * 60 * 24));
    if (diff === 0) return 'D-DAY';
    return diff > 0 ? `D - ${diff}` : `D + ${Math.abs(diff)}`;
  };

  const getDdayBadgeClass = dueDateStr => {
    const today = new Date();
    const due = new Date(dueDateStr);
    const diff = Math.ceil((due - today) / (1000 * 60 * 60 * 24));

    if (diff === 0) return 'badge-error';
    if (diff < 0) return 'badge-neutral';
    if (diff <= 3) return 'badge-warning';
    return 'badge-primary';
  };

  const toggleUser = userId => {
    selectedUserId.value = selectedUserId.value === userId ? null : userId;
    fetchTodos();
  };

  const selectStatus = label => {
    selectedStatus.value = mapTabToStatus(label);
    fetchTodos();
  };

  const mapTabToStatus = label => {
    if (label === '전체') return 'all';
    if (label === '완료') return 'completed';
    if (label === '미완료') return 'incomplete';
    return 'all';
  };
  watch(teamMembers, val => {
    console.log('[팀 멤버 변경 감지됨]', val);
  });
  watch(
    currentTeamId,
    async newId => {
      if (newId) {
        await fetchTodos();
      }
    },
    { immediate: true }
  );
</script>

<style scoped>
  /* 페이지 컨테이너 */
  .page-container {
    padding: 1.5rem;
    font-family: 'Noto Sans KR', sans-serif;
  }

  /* 카드 스타일 */
  .card {
    background: var(--color-neutral-white);
    border: 1px solid var(--color-gray-200);
    border-radius: 0.75rem;
    padding: 2rem;
    box-shadow: 0 8px 40px -10px rgba(0, 0, 0, 0.08);
  }

  /* 필터 섹션 */
  .filter-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid var(--color-gray-200);
  }

  .filter-tabs {
    display: flex;
    gap: 0.75rem;
  }

  /* 멤버 필터 */
  .member-filter {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .member-avatars {
    display: flex;
    gap: 0.5rem;
  }

  .member-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.2s ease;
    border: 2px solid transparent;
  }

  .member-avatar:hover {
    transform: scale(1.1);
    border-color: var(--color-primary-200);
  }

  .member-avatar.selected {
    border-color: var(--color-primary-main);
    box-shadow: 0 0 0 2px rgba(37, 113, 128, 0.2);
  }

  .avatar-image {
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    border-radius: 50%;
  }

  .avatar-initial {
    width: 100%;
    height: 100%;
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 16px;
    border-radius: 50%;
  }

  /* 테이블 컨테이너 */
  .table-container {
    margin-bottom: 2rem;
    border-radius: 0.5rem;
    overflow: hidden;
    border: 1px solid var(--color-gray-200);
  }

  /* 테이블 스타일 */
  .table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
    line-height: 21px;
  }

  .table th {
    font-weight: 700;
    text-align: center;
    padding: 1rem;
    background: var(--color-gray-50);
    color: var(--color-gray-800);
    border-bottom: 2px solid var(--color-gray-300);
  }

  .table td {
    padding: 1rem;
    text-align: center;
    color: var(--color-gray-700);
    border-bottom: 1px solid var(--color-gray-200);
  }

  .table-row:hover {
    background: var(--color-gray-50);
  }

  .table tr:last-child td {
    border-bottom: none;
  }

  /* 투두 내용 */
  .todo-content {
    text-align: left !important;
    max-width: 0;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  /* 페이지네이션 */
  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 1rem;
  }

  /* 반응형 */
  @media (max-width: 768px) {
    .filter-section {
      flex-direction: column;
      gap: 1rem;
      align-items: flex-start;
    }

    .member-filter {
      align-self: stretch;
      justify-content: space-between;
    }

    .table th,
    .table td {
      padding: 0.75rem 0.5rem;
      font-size: 13px;
    }
  }
</style>

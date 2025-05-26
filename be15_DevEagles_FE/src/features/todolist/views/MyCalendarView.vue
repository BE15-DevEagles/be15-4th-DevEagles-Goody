<script setup>
  import { ref, onMounted, watch } from 'vue';
  import TodoCalendar from '@/features/todolist/components/TodoCalendar.vue';
  import TodoDetailModal from '@/features/todolist/components/TodoDetailModal.vue';
  import {
    completeTodo,
    fetchMyCalendarEvents,
    fetchMyDdayTodos,
  } from '@/features/todolist/api/api.js';
  import dayjs from 'dayjs';
  import BasePagination from '@/components/common/components/Pagaination.vue';
  import { useToast } from 'vue-toastification';

  const myEvents = ref([]);
  const ddayTodoList = ref([]);
  const currentPage = ref(1);
  const totalPages = ref(1);
  const pageSize = 10;
  const selectedTodoId = ref(null);
  const isDetailModalOpen = ref(false);
  const toast = useToast();

  const openDetailModal = todoId => {
    selectedTodoId.value = todoId;
    isDetailModalOpen.value = true;
  };

  const onEditTodo = () => {
    isDetailModalOpen.value = false;
    fetchDdayTodos();
  };

  const onDeleteTodo = () => {
    isDetailModalOpen.value = false;
    fetchDdayTodos();
  };

  const props = defineProps({
    isSidebarCollapsed: Boolean,
  });

  const handleComplete = async todoId => {
    try {
      await completeTodo(todoId);
      location.reload();
    } catch (err) {
      toast.error('완료 처리에 실패했습니다.');
      console.error('❌ 완료 처리 실패:', err);
    }
  };

  function formatDday(dday) {
    if (dday > 0) return `D - ${dday}`;
    if (dday === 0) return 'D - DAY';
    return `D + ${Math.abs(dday)}`;
  }

  const fetchDdayTodos = async () => {
    try {
      const res = await fetchMyDdayTodos({ page: currentPage.value, size: pageSize });
      ddayTodoList.value = res.data.data.content;
      totalPages.value = res.data.data.pagination.totalPages;
    } catch (err) {
      console.error('❌ D-day 목록 불러오기 실패:', err);
    }
  };

  onMounted(async () => {
    const calendarRes = await fetchMyCalendarEvents();
    myEvents.value = calendarRes.data.data.map(todo => ({
      id: todo.todoId,
      title: todo.content,
      start: todo.startDate,
      end: dayjs(todo.dueDate).add(1, 'day').format('YYYY-MM-DD'),
      extendedProps: { teamId: todo.teamId },
    }));

    await fetchDdayTodos();
  });

  watch(currentPage, fetchDdayTodos);
</script>

<template>
  <div class="page">
    <div class="calendar-page">
      <div :class="['calendar-section', props.isSidebarCollapsed ? 'wide' : 'narrow']">
        <div class="card">
          <TodoCalendar :events="myEvents" type="my" />
        </div>
      </div>

      <div :class="['todolist-section', props.isSidebarCollapsed ? 'compact' : 'expanded']">
        <div class="card">
          <div class="todolist-top-section">
            <h2 class="font-section-title">TodoList</h2>
          </div>

          <div class="todolist-header">
            <span class="font-small-semibold">완료</span>
            <span class="font-small-semibold">할 일</span>
            <span class="font-small-semibold">디데이</span>
          </div>

          <div class="todolist-container">
            <div
              v-for="todo in ddayTodoList"
              :key="todo.todoId"
              class="todolist-item"
              @click="openDetailModal(todo.todoId)"
            >
              <div class="checkbox-wrapper">
                <input
                  type="checkbox"
                  :checked="false"
                  @click.stop
                  @change.stop="handleComplete(todo.todoId)"
                />
              </div>
              <div class="todo-content font-one-liner">{{ todo.content }}</div>
              <div class="todo-dday">
                <span
                  class="badge"
                  :class="
                    todo.dday === 0
                      ? 'badge-error'
                      : todo.dday < 0
                        ? 'badge-neutral'
                        : 'badge-warning'
                  "
                >
                  {{ formatDday(todo.dday) }}
                </span>
              </div>
            </div>
          </div>

          <div class="pagination-wrapper">
            <BasePagination
              :current-page="currentPage"
              :total-pages="totalPages"
              @update:current-page="page => (currentPage = page)"
            />
          </div>

          <TodoDetailModal
            v-model="isDetailModalOpen"
            :todo-id="selectedTodoId"
            @edit="onEditTodo"
            @delete="onDeleteTodo"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  /* 페이지 레이아웃 */
  .page {
    display: flex;
    justify-content: center;
    width: 100%;
    font-family: 'Noto Sans KR', sans-serif;
  }

  .calendar-page {
    display: flex;
    align-items: stretch;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    justify-content: flex-start;
    gap: 1.5rem;
    padding: 1.5rem;
    overflow-x: auto;
  }

  /* 캘린더 섹션 */
  .calendar-section {
    width: 600px;
    min-width: 600px;
    max-width: 600px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
  }

  .calendar-section.wide,
  .calendar-section.narrow {
    width: 600px;
    min-width: 600px;
    max-width: 600px;
  }

  /* 투두리스트 섹션 */
  .todolist-section {
    display: flex;
    flex-direction: column;
    transition: all 0.3s ease;
  }

  .todolist-section.expanded {
    flex: 1;
    min-width: 280px;
    max-width: 380px;
  }

  .todolist-section.compact {
    flex: 0 0 220px;
    min-width: 220px;
  }

  /* 카드 스타일 */
  .card {
    background: var(--color-neutral-white);
    border: 1px solid var(--color-gray-200);
    border-radius: 0.75rem;
    padding: 1.5rem;
    box-shadow: 0 8px 40px -10px rgba(0, 0, 0, 0.08);
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  /* 투두리스트 상단 섹션 */
  .todolist-top-section {
    margin-bottom: 1.5rem;
  }

  .action-buttons-top {
    display: flex;
    gap: 0.5rem;
    margin-top: 0.75rem;
  }

  /* 투두리스트 헤더 */
  .todolist-header {
    display: grid;
    grid-template-columns: 50px 1fr 70px;
    gap: 0.5rem;
    padding: 0.625rem 0.875rem;
    background: var(--color-gray-50);
    border-radius: 0.5rem;
    margin-bottom: 1rem;
    align-items: center;
    text-align: center;
    font-size: 12px;
  }

  /* 투두리스트 컨테이너 */
  .todolist-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-bottom: 1rem;
    min-height: 200px;
  }

  /* 투두리스트 아이템 */
  .todolist-item {
    display: grid;
    grid-template-columns: 50px 1fr 70px;
    gap: 0.5rem;
    padding: 0.75rem 0.875rem;
    border: 1px solid var(--color-gray-200);
    border-radius: 0.5rem;
    background: var(--color-neutral-white);
    transition: all 0.2s ease;
    cursor: pointer;
    align-items: center;
    font-size: 13px;
  }

  .todolist-item:hover {
    background: var(--color-gray-50);
    border-color: var(--color-primary-300);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  /* 체크박스 래퍼 */
  .checkbox-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .checkbox-wrapper input[type='checkbox'] {
    width: 16px;
    height: 16px;
    accent-color: var(--color-primary-main);
    cursor: pointer;
  }

  /* 투두 내용 */
  .todo-content {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    text-align: left;
    color: var(--color-gray-700);
  }

  /* 디데이 */
  .todo-dday {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  /* 페이지네이션 */
  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin: 1rem 0;
  }

  /* 알림 스타일 커스터마이징 */
  .alert {
    margin-bottom: 1.5rem;
    font-size: 14px;
    font-weight: 600;
  }

  /* 반응형 디자인 */
  @media (max-width: 1200px) {
    .calendar-page {
      flex-direction: column;
      gap: 1rem;
    }

    .calendar-section {
      width: 100%;
      min-width: auto;
      max-width: none;
    }

    .todolist-section.expanded,
    .todolist-section.compact {
      flex: none;
      min-width: auto;
      max-width: none;
    }
  }
</style>

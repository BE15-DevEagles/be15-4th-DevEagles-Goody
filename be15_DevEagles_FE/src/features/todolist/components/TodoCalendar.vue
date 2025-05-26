<!-- file: TodoCalendar.vue -->
<script setup>
  import { onMounted, ref, watch } from 'vue';
  import { Calendar } from '@fullcalendar/core';
  import dayGridPlugin from '@fullcalendar/daygrid';
  import timeGridPlugin from '@fullcalendar/timegrid';
  import interactionPlugin from '@fullcalendar/interaction';
  import tippy from 'tippy.js';
  import 'tippy.js/dist/tippy.css';

  import { fetchTodoDetail, fetchTeamTodoDetail } from '@/features/todolist/api/api.js';

  // 유저별 색상 맵
  const userColorMap = {};
  const userColors = [
    '#DA5D77',
    '#E89056',
    '#AF7AC5',
    '#F3C146',
    '#FFAA5C',
    '#B9A26D',
    '#5C87C5',
    '#6AA6AC',
    '#8F8FBF',
    '#62B292',
  ];

  function getColorForUser(userName) {
    if (!userColorMap[userName]) {
      const nextColor = userColors[Object.keys(userColorMap).length % userColors.length];
      userColorMap[userName] = nextColor;
    }
    return userColorMap[userName];
  }

  // 팀별 색상 맵
  const teamColorMap = {
    1: { bg: '#DA5D77', fg: '#000' },
    2: { bg: '#E89056', fg: '#000' },
    3: { bg: '#AF7AC5', fg: '#000' },
    4: { bg: '#F3C146', fg: '#000' },
    5: { bg: '#FFAA5C', fg: '#000' },
    6: { bg: '#B9A26D', fg: '#000' },
    7: { bg: '#5C87C5', fg: '#000' },
    8: { bg: '#6AA6AC', fg: '#000' },
    9: { bg: '#8F8FBF', fg: '#000' },
    10: { bg: '#62B292', fg: '#000' },
  };

  const props = defineProps({
    events: Array,
    type: {
      type: String,
      default: 'my',
      validator: value => ['my', 'team'].includes(value),
    },
  });

  const calendarRef = ref(null);
  let calendarInstance = null;

  watch(
    () => props.events,
    newEvents => {
      if (calendarInstance) {
        calendarInstance.removeAllEvents();
        calendarInstance.addEventSource(newEvents);
      }
    },
    { immediate: true }
  );

  onMounted(() => {
    calendarInstance = new Calendar(calendarRef.value, {
      plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
      initialView: 'dayGridMonth',
      selectable: true,
      editable: false,
      eventStartEditable: false,
      eventDurationEditable: false,
      height: 'auto',
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay',
      },
      titleFormat: dateInfo => {
        const year = dateInfo.date.year;
        const month = String(dateInfo.date.month + 1).padStart(2, '0');
        return `${year}년 ${month}월`;
      },
      eventDidMount: async info => {
        const todoId = Number(info.event.id);
        const fetchFn = props.type === 'team' ? fetchTeamTodoDetail : fetchTodoDetail;

        try {
          const res = await fetchFn(todoId);
          const todo = props.type === 'team' ? res.data.data : res.data;

          if (!todo) {
            console.warn('❗ 상세조회 실패 또는 응답 없음:', res);
            return;
          }

          // ✅ 배경 색상 처리
          if (props.type === 'team') {
            // 팀 캘린더 → 유저별 색상
            const userColor = getColorForUser(todo.userName);
            info.el.style.backgroundColor = userColor;
            info.el.style.color = '#fff';
          } else {
            // 내 캘린더 → 팀별 색상
            const teamColor = teamColorMap[todo.teamId];
            if (teamColor) {
              info.el.style.backgroundColor = teamColor.bg;
              info.el.style.color = teamColor.fg;
            } else {
              info.el.style.backgroundColor = '#257180';
              info.el.style.color = '#fff';
            }
          }

          // ✅ 툴팁 렌더링
          const content = `
          <div style="font-size: 13px; line-height: 1.5;">
            <div><strong>${todo.isCompleted ? '✅ 완료' : '🕓 진행 중'}</strong></div>
            <div>${todo.startDate.slice(0, 10)} ~ ${todo.dueDate.slice(0, 10)}</div>
            <div>작성자: ${todo.userName}</div>
            <div>팀명: ${todo.teamName}</div>
            <div style="margin-top: 4px;">${todo.content}</div>
          </div>
        `;

          tippy(info.el, {
            content,
            allowHTML: true,
            theme: 'light-border',
            placement: 'top',
            offset: [0, 10],
            delay: [100, 0],
          });
        } catch (err) {
          console.error('❌ 툴팁 상세조회 실패:', err);
        }
      },
    });

    calendarInstance.render();
  });
</script>

<template>
  <div>
    <div ref="calendarRef"></div>
  </div>
</template>

<style>
  /* 툴팁 스타일 */
  .tippy-box[data-theme~='light-border'] {
    background-color: var(--color-neutral-white);
    color: var(--color-neutral-dark);
    border: 1px solid var(--color-gray-300);
    font-size: 12px;
    font-family: 'Noto Sans KR', sans-serif;
    border-radius: 0.5rem;
    padding: 0.75rem;
    box-shadow: 0 8px 40px -10px rgba(0, 0, 0, 0.15);
    line-height: 1.4;
  }

  .tippy-box[data-theme~='light-border'][data-placement^='top'] > .tippy-arrow::before {
    border-top-color: var(--color-gray-300);
  }
  .tippy-box[data-theme~='light-border'][data-placement^='bottom'] > .tippy-arrow::before {
    border-bottom-color: var(--color-gray-300);
  }
  .tippy-box[data-theme~='light-border'][data-placement^='left'] > .tippy-arrow::before {
    border-left-color: var(--color-gray-300);
  }
  .tippy-box[data-theme~='light-border'][data-placement^='right'] > .tippy-arrow::before {
    border-right-color: var(--color-gray-300);
  }

  /* FullCalendar 전역 스타일 개선 */
  .fc {
    font-family: 'Noto Sans KR', sans-serif !important;
    font-size: 13px !important;
  }

  /* 캘린더 헤더 툴바 */
  .fc-toolbar {
    margin-bottom: 1rem !important;
  }

  .fc-toolbar-title {
    font-family: 'Pretendard', 'Noto Sans KR', sans-serif !important;
    font-size: 20px !important;
    font-weight: 700 !important;
    color: var(--color-neutral-dark) !important;
    letter-spacing: -0.02em !important;
  }

  /* 캘린더 버튼들 */
  .fc .fc-button {
    background: var(--color-primary-main) !important;
    color: var(--color-neutral-white) !important;
    border: none !important;
    font-family: 'Pretendard', 'Noto Sans KR', sans-serif !important;
    font-weight: 600 !important;
    font-size: 12px !important;
    border-radius: 0.375rem !important;
    padding: 0.375rem 0.75rem !important;
    transition: all 0.2s ease !important;
  }

  .fc .fc-button:hover {
    background: var(--color-primary-400) !important;
    transform: translateY(-1px) !important;
  }

  .fc .fc-button:active {
    background: var(--color-primary-500) !important;
    transform: translateY(0) !important;
  }

  .fc .fc-button:disabled {
    background: var(--color-gray-200) !important;
    color: var(--color-gray-500) !important;
    cursor: not-allowed !important;
    transform: none !important;
  }

  /* 요일 헤더 */
  .fc-col-header-cell {
    font-family: 'Pretendard', 'Noto Sans KR', sans-serif !important;
    font-weight: 600 !important;
    font-size: 12px !important;
    background: var(--color-gray-50) !important;
    color: var(--color-gray-700) !important;
    padding: 0.5rem !important;
  }

  .fc-col-header-cell-cushion {
    padding: 0.25rem !important;
  }

  /* 날짜 숫자 */
  .fc-daygrid-day-number {
    font-family: 'Pretendard', 'Noto Sans KR', sans-serif !important;
    font-weight: 500 !important;
    font-size: 13px !important;
    color: var(--color-gray-700) !important;
    padding: 0.25rem !important;
  }

  /* 오늘 날짜 강조 */
  .fc-day-today {
    background-color: var(--color-primary-50) !important;
  }

  .fc-day-today .fc-daygrid-day-number {
    background: var(--color-primary-main) !important;
    color: var(--color-neutral-white) !important;
    border-radius: 50% !important;
    width: 24px !important;
    height: 24px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    font-weight: 700 !important;
  }

  /* 이벤트 스타일 */
  .fc-event {
    border-radius: 4px !important;
    border: none !important;
    font-weight: 600 !important;
    font-size: 11px !important;
    padding: 1px 4px !important;
    font-family: 'Noto Sans KR', sans-serif !important;
    line-height: 1.2 !important;
  }

  .fc-event-title {
    font-weight: 600 !important;
    font-size: 11px !important;
  }

  .fc-daygrid-event {
    margin: 1px 1px !important;
    min-height: 18px !important;
  }

  /* 캘린더 셀 */
  .fc-daygrid-day {
    min-height: 32px !important;
  }

  .fc-daygrid-day-frame {
    min-height: 32px !important;
  }

  /* 더보기 링크 */
  .fc-daygrid-more-link {
    font-family: 'Noto Sans KR', sans-serif !important;
    font-size: 10px !important;
    color: var(--color-primary-main) !important;
    font-weight: 600 !important;
  }

  /* 주말 스타일 */
  .fc-day-sat .fc-daygrid-day-number {
    color: var(--color-primary-main) !important;
  }

  .fc-day-sun .fc-daygrid-day-number {
    color: var(--color-error-300) !important;
  }
</style>

export const calendarRoutes = [
  {
    path: '/calendar/my',
    name: 'MyCalendar',
    component: () => import('@/features/todolist/views/MyCalendarView.vue'),
    meta: {
      layout: 'default',
      requiresAuth: true,
      title: '나의 캘린더',
      description: '나의 모든 할 일을 확인할 수 있어요.',
    },
  },
  {
    path: '/calendar/team',
    name: 'TeamCalendar',
    component: () => import('@/features/todolist/views/TeamCalendarView.vue'),
    meta: {
      layout: 'default',
      requiresAuth: true,
      title: '팀 캘린더',
      description: '내가 속한 팀의 모든 할 일을 확인할 수 있어요.',
    },
  },
  {
    path: '/todos',
    name: 'TeamTodoList',
    component: () => import('@/features/todolist/views/TeamTodoListView.vue'),
    meta: {
      layout: 'default',
      requiresAuth: true,
      title: '팀 TodoList',
      description: '내가 속한 팀의 모든 TodoList를 확인할 수 있어요.',
    },
  },
];

export const timecapsuleRoute = [
  {
    path: 'timecapsule/create',
    name: 'TimecapsuleCreate',
    component: () => import('@/features/timecapsule/views/TimecapsuleCreatePage.vue'),
  },
  {
    path: 'timecapsule/list',
    name: 'TimecapsuleList',
    component: () => import('@/features/timecapsule/views/TimecapsuleList.vue'),
  },
];

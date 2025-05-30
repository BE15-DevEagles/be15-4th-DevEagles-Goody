import { createRouter, createWebHistory } from 'vue-router';
import { Layout, ErrorPage } from '@/components/common/layout';
import { userRoutes } from '@/features/user/userRouter.js';
import { useAuthStore } from '@/store/auth';
import { calendarRoutes } from '@/features/todolist/router.js';
import { myPageRoutes } from '@/features/user/mypageRouter.js';
import { teamRoutes } from '@/features/team/router.js';
import { workRoutes } from '@/features/worklog/router.js';
import { timecapsuleRoute } from '@/features/timecapsule/router.js';
import { rouletteRoute } from '@/features/roulette/views/router.js';

const routes = [
  ...userRoutes,
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
      },
      // 여기 Layout 하위 라우트 계속 추가 가능
      ...calendarRoutes,
      ...myPageRoutes,
      ...teamRoutes,
      ...workRoutes,
      ...timecapsuleRoute,
      ...rouletteRoute,
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: ErrorPage,
    props: {
      errorCode: '404',
      errorTitle: '페이지를 찾을 수 없습니다',
      errorMessage: '요청하신 페이지가 존재하지 않습니다. URL을 확인해 주세요.',
    },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();

  const publicPages = ['/login', '/signup', '/email-verify', '/email-pwd', '/findpwd']; // 인증 없이 접근 가능한 경로들
  const authRequired = !publicPages.includes(to.path);

  // 인증이 필요한 페이지인데 로그인 안 했으면 로그인으로 이동
  if (authRequired && !authStore.isAuthenticated) {
    return next('/login');
  }

  next();
});

export default router;

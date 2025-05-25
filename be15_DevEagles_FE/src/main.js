import { createApp } from 'vue';
import { createPinia } from 'pinia';
import router from './router';
import Toast, { POSITION } from 'vue-toastification';
import 'vue-toastification/dist/index.css';
import './assets/css/toast.css';
import App from './App.vue';
import './assets/css/index.css';
import { useAuthStore } from '@/store/auth.js';
import { setupChat } from './features/chat/config/chatConfig';
import { setupCalendar, Calendar, DatePicker } from 'v-calendar';
import 'v-calendar/style.css';

const app = createApp(App);
const pinia = createPinia();

const toastOptions = {
  position: POSITION.TOP_RIGHT,
  timeout: 4000,
  closeOnClick: true,
  pauseOnFocusLoss: true,
  pauseOnHover: true,
  draggable: true,
  draggablePercent: 0.6,
  showCloseButtonOnHover: false,
  hideProgressBar: false,
  closeButton: 'button',
  icon: true,
  rtl: false,
};

setupCalendar(app, {
  componentPrefix: 'V',
});
app.component('VCalendar', Calendar);
app.component('VDatePicker', DatePicker);
app.use(pinia);
app.use(router);
app.use(Toast, toastOptions);

app.mount('#app');

// 라우터 가드 설정 (필요시 활성화)
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();

  // 인증이 필요한 페이지 확인
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!authStore.isAuthenticated) {
      console.log('인증이 필요한 페이지입니다. 로그인 페이지로 이동합니다.');
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      });
    } else {
      next();
    }
  } else {
    next();
  }
});

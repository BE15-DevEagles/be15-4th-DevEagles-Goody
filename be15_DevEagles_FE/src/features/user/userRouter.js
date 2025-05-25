export const userRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/features/user/views/LoginView.vue'),
  },
  {
    path: '/signup',
    name: 'SignUp',
    component: () => import('@/features/user/views/SignUpView.vue'),
  },
  {
    path: '/email-verify',
    name: 'EmailVerify',
    component: () => import('@/features/user/views/EmailVerifyView.vue'),
  },
  {
    path: '/email-pwd',
    name: 'EmailVerify',
    component: () => import('@/features/user/views/EmailVerifyPwdView.vue'),
  },
  {
    path: '/findpwd',
    name: 'EmailPwdEdit',
    component: () => import('@/features/user/views/EmailPwdEditView.vue'),
  },
];

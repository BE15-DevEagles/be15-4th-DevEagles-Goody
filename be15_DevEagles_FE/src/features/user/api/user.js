import api from '@/api/axios';
import { useAuthStore } from '@/store/auth.js';

const exceptToken = [
  { method: 'post', url: '/auth/login' },
  { method: 'post', url: '/auth/refresh' },
  { method: 'post', url: '/users$' },
  { method: 'post', url: '/users/duplcheck' },
  { method: 'patch', url: '/users/email/pwd' },
  { method: 'post', url: '/auth/sendauth' },
  { method: 'post', url: '/auth/findid' },
  { method: 'post', url: '/auth/findpwd' },
  { method: 'post', url: '/auth/verify' },
];

api.interceptors.request.use(
  config => {
    const authStore = useAuthStore();
    const token = authStore.accessToken;

    const shouldSkipToken = exceptToken.some(
      pattern => pattern.method === config.method && new RegExp(pattern.url).test(config.url)
    );

    if (token && !shouldSkipToken) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(p => {
    if (error) p.reject(error);
    else p.resolve(token);
  });
  failedQueue = [];
};

api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    const authStore = useAuthStore();

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers['Authorization'] = `Bearer ${token}`;
          return api(originalRequest);
        });
      }

      isRefreshing = true;

      try {
        const res = await api.post('/auth/refresh');
        const newAccessToken = res.data.data.accessToken;

        // accessToken 갱신
        await authStore.setAuth(newAccessToken);
        api.defaults.headers.common['Authorization'] = `Bearer ${newAccessToken}`;
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;

        processQueue(null, newAccessToken);
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        authStore.clearAuth();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    console.log('[INTERCEPTOR] accessToken 갱신 성공');

    return Promise.reject(error);
  }
);

export const login = params => api.post(`/auth/login`, params);

export const logout = () => api.post(`/auth/logout`);

export const validUserStatus = () => api.post(`/auth/valid`);

export const findUserId = params => api.post(`/auth/findid`, params);

export const findUserPwd = params => api.post(`/auth/findpwd`, params);

export const emailEditPassword = params => api.patch(`/users/email/pwd`, params);

export const sendAuth = params => api.post(`/auth/sendauth`, params);

export const verifyEmailCode = params => api.post(`/auth/verify`, params);

export const signUp = formData =>
  api.post(`/users`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

export const checkDuplicate = params => api.post(`/users/duplcheck`, params);

export const mypage = () => api.get(`/users/me`);

export const updateUserInfo = formData =>
  api.patch(`/users/mod`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

export const verifyPassword = password => api.post(`/users/valid`, password);

export const editPassword = password =>
  api.patch(
    '/users/mod/pwd',
    { password },
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

export const withdrawUser = () => api.delete(`/users`);

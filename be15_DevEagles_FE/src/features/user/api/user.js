import axios from 'axios';
import { useAuthStore } from '@/store/auth.js';

const api = axios.create({
  baseURL: `http://localhost:8080/api/v1`,
  withCredentials: true,
});

const exceptToken = ['/auth/login', '/users', '/users/duplcheck', '/auth/sendauth', '/auth/verify'];

api.interceptors.request.use(
  config => {
    const authStore = useAuthStore();
    const token = authStore.accessToken;

    const shouldSkipToken = exceptToken.some(url => config.url?.includes(url));

    if (token && !shouldSkipToken) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

export const login = params => api.post(`/auth/login`, params);

export const logout = () => api.post(`/auth/logout`);

export const validUserStatus = () => api.post(`/auth/valid`);

export const signUp = formData =>
  api.post(`/users`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

export const checkDuplicate = params => api.post(`/users/duplcheck`, params);

export const sendAuth = params => api.post(`/auth/sendauth`, params);

export const verifyEmailCode = params => api.post(`/auth/verify`, params);

export const mypage = () => api.post(`/users/me`);

import api from '@/api/axios.js';

export const fetchComments = worklogId => {
  return api.get(`/comments/${worklogId}`);
};

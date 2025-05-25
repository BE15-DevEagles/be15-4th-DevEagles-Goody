import api from '@/api/axios.js';

export const fetchComments = worklogId => {
  return api.get(`/comments/${worklogId}`);
};

export const postComment = (worklogId, commentContent) => {
  const requestBody = {
    worklogId,
    commentContent,
  };
  return api.post('/comments', requestBody);
};

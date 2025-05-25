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
export const updateComment = (commentId, commentContent) => {
  return api.put(`/comments/${commentId}`, {
    commentContent,
  });
};

export const deleteComment = commentId => {
  return api.delete(`comments/${commentId}`);
};

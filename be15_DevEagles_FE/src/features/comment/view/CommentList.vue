<script setup>
  import { onMounted, ref } from 'vue';
  import { fetchComments } from '@/features/comment/api/comment.js';
  import CommentForm from '@/features/comment/components/CommentForm.vue';

  const props = defineProps({
    worklogId: {
      type: Number,
      required: true,
    },
  });

  const comments = ref([]);
  const loading = ref(true);

  async function loadComments() {
    loading.value = true;
    try {
      const res = await fetchComments(props.worklogId);
      console.log(res);
      comments.value = res.data.data;
    } catch (error) {
      console.error('댓글 조회 실패:', error);
    } finally {
      loading.value = false;
    }
  }

  function handleCommentAdded() {
    loadComments();
  }

  onMounted(() => {
    loadComments();
  });
</script>

<template>
  <div class="space-y-4">
    <!-- 댓글 작성 폼 -->
    <CommentForm :worklog-id="props.worklogId" @comment-added="handleCommentAdded" />

    <!-- 로딩 중 -->
    <div v-if="loading" class="text-sm text-gray-500">💬 댓글을 불러오는 중...</div>

    <!-- 댓글 리스트 -->
    <ul v-else-if="comments.length" class="space-y-4">
      <li
        v-for="comment in comments"
        :key="comment.commentId"
        class="p-3 border border-gray-200 rounded bg-gray-50 shadow-sm"
      >
        <div class="text-sm text-gray-800">{{ comment.commentContent }}</div>
        <div class="text-xs text-gray-500 mt-1 flex justify-between">
          <span>👤 {{ comment.username }}</span>
          <span>
            {{ new Date(comment.time).toLocaleString() }}
            <span v-if="comment.isEdited">(수정됨)</span>
          </span>
        </div>
      </li>
    </ul>

    <!-- 댓글 없음 -->
    <div v-else class="text-sm text-gray-400">아직 댓글이 없습니다.</div>
  </div>
</template>

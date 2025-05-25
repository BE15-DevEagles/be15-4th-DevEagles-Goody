<script setup>
  import { ref } from 'vue';
  import { postComment } from '@/features/comment/api/comment.js';

  const props = defineProps({
    worklogId: {
      type: [String, Number],
      required: true,
    },
  });

  const emit = defineEmits(['comment-added']);
  const content = ref('');
  const loading = ref(false);

  async function submitComment() {
    if (!content.value.trim()) return;

    try {
      loading.value = true;
      const response = await postComment(props.worklogId, content.value.trim());
      emit('comment-added', response.data.data); // ✅ 등록된 댓글 객체 전달
      content.value = '';
    } catch (error) {
      console.error('댓글 등록 실패:', error);
    } finally {
      loading.value = false;
    }
  }
</script>

<template>
  <div class="mb-4">
    <textarea
      v-model="content"
      class="input w-full resize-none"
      rows="3"
      placeholder="댓글을 입력하세요"
    ></textarea>
    <div class="flex justify-end mt-2">
      <button class="btn btn-primary" :disabled="loading || !content.trim()" @click="submitComment">
        등록
      </button>
    </div>
  </div>
</template>

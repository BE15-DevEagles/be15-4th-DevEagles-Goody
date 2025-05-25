<script setup>
  import { nextTick, onMounted, ref } from 'vue';
  import { fetchComments, updateComment } from '@/features/comment/api/comment.js';
  import CommentForm from '@/features/comment/components/CommentForm.vue';
  import ConfirmModal from '@/features/worklog/components/ConfirmModal.vue';

  const props = defineProps({
    worklogId: {
      type: Number,
      required: true,
    },
  });

  const comments = ref([]);
  const loading = ref(true);
  const editingId = ref(null);
  const editContent = ref('');
  const showRegisterModal = ref(false);
  const showEditModal = ref(false);

  async function loadComments() {
    loading.value = true;
    try {
      const res = await fetchComments(props.worklogId);
      comments.value = res.data.data;
    } catch (error) {
      console.error('댓글 조회 실패:', error);
    } finally {
      loading.value = false;
    }
  }

  function handleCommentAdded(newComment) {
    showRegisterModal.value = true;
    pendingNewComment.value = newComment;
  }

  const pendingNewComment = ref(null);
  function confirmAddComment() {
    if (pendingNewComment.value) {
      comments.value.unshift(pendingNewComment.value);
      pendingNewComment.value = null;
    }
    showRegisterModal.value = false;
  }

  function cancelAddComment() {
    pendingNewComment.value = null;
    showRegisterModal.value = false;
  }

  function startEdit(comment) {
    editingId.value = comment.commentId;
    editContent.value = comment.commentContent;
  }

  function cancelEdit() {
    editingId.value = null;
    editContent.value = '';
  }

  function openEditModal() {
    showEditModal.value = true;
  }

  async function saveEdit() {
    try {
      await updateComment(editingId.value, editContent.value.trim());

      // ✅ 수정 성공 후 댓글 목록 새로고침!
      await loadComments();
    } catch (error) {
      console.error('댓글 수정 실패:', error);
    } finally {
      editingId.value = null;
      editContent.value = '';
      showEditModal.value = false;
    }
  }

  onMounted(() => {
    loadComments();
  });
</script>

<template>
  <div class="space-y-4">
    <!-- 댓글 작성 폼 -->
    <CommentForm :worklog-id="props.worklogId" @comment-added="handleCommentAdded" />

    <!-- 등록 확인 모달 -->
    <ConfirmModal
      v-model="showRegisterModal"
      title="댓글 등록"
      message="댓글을 등록하시겠습니까?"
      @confirm="confirmAddComment"
      @update:model-value="cancelAddComment"
    />

    <!-- 수정 확인 모달 -->
    <ConfirmModal
      v-model="showEditModal"
      title="댓글 수정"
      message="정말 수정하시겠습니까?"
      @confirm="saveEdit"
      @update:model-value="cancelEdit"
    />

    <!-- 로딩 중 -->
    <div v-if="loading" class="text-sm text-gray-500">💬 댓글을 불러오는 중...</div>

    <!-- 댓글 리스트 -->
    <ul v-else-if="comments.length" class="space-y-4">
      <li
        v-for="comment in comments"
        :key="comment.commentId"
        class="p-3 border border-gray-200 rounded bg-gray-50 shadow-sm"
      >
        <div v-if="editingId === comment.commentId">
          <textarea v-model="editContent" class="input w-full resize-none mb-2" rows="3"></textarea>
          <div class="flex justify-end gap-2">
            <button class="btn btn-secondary" @click="cancelEdit">취소</button>
            <button class="btn btn-primary" @click="openEditModal">저장</button>
          </div>
        </div>
        <div v-else>
          <div class="text-sm text-gray-800">{{ comment.commentContent }}</div>
          <div class="text-xs text-gray-500 mt-1 flex justify-between">
            <span>👤 {{ comment.username }}</span>
            <span>
              {{ new Date(comment.time).toLocaleString() }}
              <span v-if="comment.isEdited">(수정됨)</span>
            </span>
          </div>
          <div class="text-right mt-1">
            <button class="text-blue-500 text-xs hover:underline" @click="startEdit(comment)">
              ✏️ 수정
            </button>
          </div>
        </div>
      </li>
    </ul>

    <!-- 댓글 없음 -->
    <div v-else class="text-sm text-gray-400">아직 댓글이 없습니다.</div>
  </div>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import api from '@/api/axios.js';

  // 댓글 컴포넌트 준비
  import CommentForm from '@/features/comment/components/CommentForm.vue';
  import CommentList from '@/features/comment/view/CommentList.vue';

  const route = useRoute();
  const router = useRouter();
  const worklogId = route.params.id;

  const full = ref(null);
  const commentListKey = ref(0);

  function formatDate(dateStr) {
    return dateStr ? new Date(dateStr).toLocaleDateString('ko-KR') : '-';
  }

  function refreshComments() {
    commentListKey.value++;
  }

  onMounted(async () => {
    window.scrollTo(0, 0);
    try {
      const res = await api.get(`/worklog/${worklogId}`);
      full.value = res.data.data;
    } catch (err) {
      console.error('상세 조회 실패:', err);
    }
  });
</script>

<template>
  <div class="worklog-detail-container">
    <!-- 로딩 중 메시지 -->
    <div v-if="!full" class="loading-state">
      <div class="loading-spinner"></div>
      <p class="loading-text">업무일지를 불러오는 중입니다...</p>
    </div>

    <!-- 업무일지 문서 -->
    <div v-else class="worklog-document">
      <!-- 문서 헤더 -->
      <div class="document-header">
        <h1 class="document-title">{{ full.summary }}</h1>
        <div class="document-meta">
          <span class="meta-item"> <strong>작성자:</strong> {{ full.userName }} </span>
          <span class="meta-divider">|</span>
          <span class="meta-item"> <strong>소속팀:</strong> {{ full.teamName }} </span>
          <span class="meta-divider">|</span>
          <span class="meta-item"> <strong>작성일:</strong> {{ formatDate(full.writtenAt) }} </span>
        </div>
      </div>

      <!-- 문서 본문 -->
      <div class="document-body">
        <!-- 업무 내용 섹션 -->
        <section class="document-section">
          <h2 class="section-title">1. 업무 내용</h2>
          <div class="section-content">
            {{ full.workContent || '작성된 내용이 없습니다.' }}
          </div>
        </section>

        <!-- 특이사항 섹션 -->
        <section class="document-section">
          <h2 class="section-title">2. 특이사항</h2>
          <div class="section-content">
            {{ full.note || '특이사항이 없습니다.' }}
          </div>
        </section>

        <!-- 익일 계획 섹션 -->
        <section class="document-section">
          <h2 class="section-title">3. 익일 업무계획</h2>
          <div class="section-content">
            {{ full.planContent || '계획된 업무가 없습니다.' }}
          </div>
        </section>

        <!-- 댓글 섹션 -->
        <section class="document-section comments-section">
          <h2 class="section-title">
            <svg
              class="w-5 h-5 inline-block mr-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
              ></path>
            </svg>
            댓글
          </h2>
          <div class="comments-wrapper">
            <CommentList :worklog-id="Number(worklogId)" />
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .worklog-detail-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 32px 24px;
    background-color: var(--color-neutral-white);
    min-height: 100vh;
    font-family: 'Noto Sans KR', sans-serif;
  }

  /* 로딩 상태 */
  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    text-align: center;
  }

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid var(--color-gray-200);
    border-top: 3px solid var(--color-primary-main);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
  }

  .loading-text {
    color: var(--color-gray-500);
    font-size: 16px;
    margin: 0;
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  /* 문서 스타일 */
  .worklog-document {
    background: var(--color-neutral-white);
    border: 1px solid var(--color-gray-200);
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    overflow: hidden;
  }

  /* 문서 헤더 */
  .document-header {
    padding: 32px 40px 24px;
    border-bottom: 2px solid var(--color-gray-200);
    background: var(--color-gray-50);
  }

  .document-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--color-neutral-dark);
    margin: 0 0 16px 0;
    line-height: 1.3;
    text-align: center;
  }

  .document-meta {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    font-size: 14px;
    color: var(--color-gray-600);
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .meta-item strong {
    color: var(--color-gray-700);
    font-weight: 600;
  }

  .meta-divider {
    color: var(--color-gray-400);
    font-weight: 300;
  }

  /* 문서 본문 */
  .document-body {
    padding: 0;
  }

  .document-section {
    padding: 32px 40px;
    border-bottom: 1px solid var(--color-gray-100);
  }

  .document-section:last-child {
    border-bottom: none;
  }

  .comments-section {
    background: var(--color-neutral-white);
    border-top: 3px solid var(--color-primary-main);
  }

  .comments-wrapper {
    background: var(--color-neutral-white);
    border-radius: 12px;
    padding: 20px;
    border: 1px solid var(--color-gray-200);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .section-title {
    font-size: 20px;
    font-weight: 700;
    color: var(--color-neutral-dark);
    margin: 0 0 20px 0;
    padding-bottom: 8px;
    border-bottom: 2px solid var(--color-primary-main);
    display: inline-flex;
    align-items: center;
  }

  .comments-section .section-title {
    color: var(--color-primary-main);
    border-bottom: 2px solid var(--color-primary-main);
  }

  .section-content {
    font-size: 16px;
    line-height: 1.8;
    color: var(--color-gray-700);
    white-space: pre-line;
    word-break: break-word;
  }

  /* 반응형 */
  @media (max-width: 768px) {
    .worklog-detail-container {
      padding: 20px 16px;
    }

    .document-header {
      padding: 24px 20px 20px;
    }

    .document-title {
      font-size: 24px;
    }

    .document-meta {
      flex-direction: column;
      gap: 4px;
      text-align: center;
    }

    .meta-divider {
      display: none;
    }

    .document-section {
      padding: 24px 20px;
    }

    .section-title {
      font-size: 18px;
    }

    .section-content {
      font-size: 15px;
      line-height: 1.7;
    }

    .comments-wrapper {
      padding: 16px;
    }
  }

  @media (max-width: 480px) {
    .worklog-detail-container {
      padding: 16px 12px;
    }

    .document-header {
      padding: 20px 16px;
    }

    .document-title {
      font-size: 22px;
    }

    .document-section {
      padding: 20px 16px;
    }

    .section-title {
      font-size: 16px;
    }

    .section-content {
      font-size: 14px;
    }

    .comments-wrapper {
      padding: 12px;
    }
  }
</style>

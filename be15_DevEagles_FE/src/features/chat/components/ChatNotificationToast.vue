<template>
  <div class="chat-notification-toast">
    <div class="flex items-center space-x-3 p-4">
      <!-- 채팅 아이콘 -->
      <div class="flex-shrink-0">
        <div
          class="w-8 h-8 rounded-full flex items-center justify-center shadow-md"
          style="background: var(--color-primary-main)"
        >
          <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path
              fill-rule="evenodd"
              d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z"
              clip-rule="evenodd"
            />
          </svg>
        </div>
      </div>

      <!-- 메시지 내용 -->
      <div class="flex-1 min-w-0">
        <div class="flex items-center space-x-2 mb-1">
          <p class="text-sm font-semibold text-gray-900 truncate">
            {{ senderName }}
          </p>
          <span class="text-xs text-gray-500">새 메시지</span>
        </div>
        <p class="text-sm text-gray-700 line-clamp-2">
          {{ content }}
        </p>
      </div>

      <!-- 닫기 버튼 -->
      <div class="flex-shrink-0">
        <button
          class="text-gray-400 hover:text-gray-600 transition-colors duration-200 p-1 rounded-full hover:bg-gray-100"
          @click="closeToast"
        >
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path
              fill-rule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clip-rule="evenodd"
            />
          </svg>
        </button>
      </div>
    </div>

    <!-- 진행 바 -->
    <div class="h-1 bg-gray-200">
      <div
        class="h-full transition-all duration-100 ease-linear"
        :style="{
          width: `${progress}%`,
          background: 'var(--color-primary-main)',
        }"
      ></div>
    </div>
  </div>
</template>

<script setup>
  import { ref, onMounted, onUnmounted } from 'vue';

  const props = defineProps({
    senderName: {
      type: String,
      required: true,
    },
    content: {
      type: String,
      required: true,
    },
    timeout: {
      type: Number,
      default: 8000,
    },
    closeToast: {
      type: Function,
      required: true,
    },
  });

  const progress = ref(100);
  let interval = null;

  onMounted(() => {
    const step = 100 / (props.timeout / 100);

    interval = setInterval(() => {
      progress.value -= step;
      if (progress.value <= 0) {
        clearInterval(interval);
        props.closeToast();
      }
    }, 100);
  });

  onUnmounted(() => {
    if (interval) {
      clearInterval(interval);
    }
  });
</script>

<style scoped>
  .chat-notification-toast {
    @apply bg-white rounded-lg shadow-lg overflow-hidden;
    min-width: 300px;
    max-width: 380px;
    border: 1px solid #e5e7eb;
  }

  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 호버 효과 */
  .chat-notification-toast:hover .h-1 > div {
    animation-play-state: paused;
  }

  /* Vue Toastification 기본 스타일 오버라이드 */
  :deep(.Vue-Toastification__toast) {
    border: none !important;
    box-shadow:
      0 10px 15px -3px rgba(0, 0, 0, 0.1),
      0 4px 6px -2px rgba(0, 0, 0, 0.05) !important;
  }

  :deep(.Vue-Toastification__toast-body) {
    padding: 0 !important;
    margin: 0 !important;
  }
</style>

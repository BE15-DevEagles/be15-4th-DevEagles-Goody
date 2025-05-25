<template>
  <div v-if="showReadStatus" class="text-xs text-[var(--color-gray-400)] mt-1">
    <div v-if="isLoading" class="flex items-center gap-1">
      <div class="w-2 h-2 bg-gray-300 rounded-full animate-pulse"></div>
      <span>읽음 상태 확인 중...</span>
    </div>
    <div v-else-if="readStatus" class="flex items-center gap-1">
      <!-- 모든 사람이 읽음 -->
      <div v-if="readStatus.unreadCount === 0" class="flex items-center gap-1 text-blue-500">
        <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path
            fill-rule="evenodd"
            d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
            clip-rule="evenodd"
          />
        </svg>
        <span>읽음</span>
      </div>
      <!-- 일부만 읽음 -->
      <div v-else-if="readStatus.readCount > 0" class="flex items-center gap-1">
        <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path
            fill-rule="evenodd"
            d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
            clip-rule="evenodd"
          />
        </svg>
        <span>{{ readStatus.readCount }}/{{ readStatus.totalParticipants }}</span>
      </div>
      <!-- 아무도 읽지 않음 -->
      <div v-else class="flex items-center gap-1">
        <div class="w-3 h-3 border border-gray-300 rounded-full"></div>
        <span>안읽음</span>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
  import { getMessageReadStatus } from '@/features/chat/api/chatService.js';

  const props = defineProps({
    message: {
      type: Object,
      required: true,
    },
    chatroomId: {
      type: String,
      required: true,
    },
    showForMyMessages: {
      type: Boolean,
      default: true,
    },
    onReadStatusUpdate: {
      type: Function,
      default: null,
    },
  });

  const readStatus = ref(null);
  const isLoading = ref(false);

  // 내 메시지이고 showForMyMessages가 true일 때만 표시
  const showReadStatus = computed(() => {
    return (
      props.message.isMe &&
      props.showForMyMessages &&
      props.message.id &&
      !props.message.id.startsWith('temp_')
    );
  });

  const loadReadStatus = async () => {
    if (!showReadStatus.value) return;

    try {
      isLoading.value = true;
      const status = await getMessageReadStatus(props.chatroomId, props.message.id);
      readStatus.value = status;

      // 부모 컴포넌트에 읽음 상태 업데이트 알림
      if (props.onReadStatusUpdate) {
        props.onReadStatusUpdate(props.message.id, status);
      }
    } catch (error) {
      console.error('읽음 상태 조회 실패:', error);
      readStatus.value = null;
    } finally {
      isLoading.value = false;
    }
  };

  // 외부에서 읽음 상태 업데이트를 요청할 수 있도록 expose
  const refreshReadStatus = () => {
    loadReadStatus();
  };

  defineExpose({
    refreshReadStatus,
  });

  // 메시지 ID가 변경될 때만 읽음 상태 로드 (immediate: true로 마운트 시에도 실행)
  watch(() => props.message.id, loadReadStatus, { immediate: true });
</script>

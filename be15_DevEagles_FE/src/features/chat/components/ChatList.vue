<template>
  <div
    class="flex flex-col border-t border-[var(--color-gray-200)] h-full overflow-hidden flex-grow-0"
  >
    <div class="p-3 border-b border-[var(--color-gray-200)] flex-shrink-0">
      <h2 class="font-section-inner">채팅 목록</h2>
    </div>

    <div class="flex-grow overflow-y-auto h-full" style="height: calc(100% - 49px)">
      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="p-3 text-center">
        <p class="text-[var(--color-gray-500)] font-small">채팅 목록을 불러오는 중...</p>
      </div>

      <!-- 에러 상태 -->
      <div v-else-if="error" class="p-3 text-center">
        <p class="text-[var(--color-error-300)] font-small">{{ error }}</p>
        <button
          class="mt-2 text-[var(--color-primary-300)] hover:text-[var(--color-primary-400)] text-sm"
          @click="$emit('retry-load')"
        >
          다시 시도
        </button>
      </div>

      <!-- 채팅 목록이 비어있는 경우 -->
      <div v-else-if="chats.length === 0" class="p-3 text-center">
        <p class="text-[var(--color-gray-500)] font-small">아직 채팅이 없습니다.</p>
      </div>

      <!-- 채팅 목록 -->
      <div v-else>
        <div
          v-for="chat in chatsWithStatus"
          :key="chat.id"
          class="p-3 border-b border-[var(--color-gray-200)] hover:bg-[var(--color-gray-100)] cursor-pointer transition-colors"
          @click="handleChatSelect(chat)"
        >
          <div class="flex items-start">
            <div class="relative mr-3 flex-shrink-0">
              <!-- 썸네일 이미지 또는 기본 아바타 -->
              <div class="w-10 h-10 rounded-md overflow-hidden">
                <img
                  v-if="chat.thumbnail"
                  :src="chat.thumbnail"
                  :alt="chat.name"
                  class="w-full h-full object-cover"
                />
                <div
                  v-else
                  class="w-full h-full flex items-center justify-center text-white font-one-liner-semibold"
                  :class="getChatTypeClass(chat.type)"
                >
                  {{ getChatDisplayChar(chat) }}
                </div>
              </div>

              <!-- 온라인 상태 표시 (1:1 채팅만) -->
              <div
                v-if="chat.type === 'DIRECT' && chat.isOnline !== undefined"
                class="absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white"
                :class="chat.isOnline ? 'bg-green-500' : 'bg-gray-400'"
              ></div>

              <!-- AI 채팅 표시 -->
              <div
                v-else-if="chat.type === 'AI'"
                class="absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white bg-[var(--color-primary-400)] flex items-center justify-center"
              >
                <span class="text-white text-[8px] leading-none">🤖</span>
              </div>

              <!-- 팀 채팅방 표시 -->
              <div
                v-else-if="chat.type === 'TEAM'"
                class="absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white bg-[var(--color-success-400)] flex items-center justify-center"
              >
                <span class="text-white text-[8px] leading-none">👥</span>
              </div>

              <!-- 그룹 채팅방 표시 -->
              <div
                v-else-if="chat.type === 'GROUP'"
                class="absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white bg-[var(--color-info-400)] flex items-center justify-center"
              >
                <span class="text-white text-[8px] leading-none">👥</span>
              </div>
            </div>

            <div class="flex-grow overflow-hidden">
              <div class="flex items-center justify-between">
                <h3 class="font-one-liner-semibold truncate flex items-center">
                  {{ chat.name }}
                  <!-- 채팅방 타입별 참가자 수 표시 -->
                  <span
                    v-if="chat.type === 'GROUP' || chat.type === 'TEAM'"
                    class="ml-1 text-xs text-[var(--color-gray-400)]"
                  >
                    ({{ chat.participants?.length || 0 }}명)
                  </span>
                  <!-- 알림 끔 상태 표시 -->
                  <div
                    v-if="!isNotificationEnabled(chat.id)"
                    class="ml-1 relative"
                    title="알림 꺼짐"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-3 w-3 text-[var(--color-gray-500)]"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                    >
                      <path
                        d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z"
                      />
                    </svg>
                    <!-- 슬래시 표시 -->
                    <div class="absolute inset-0 flex items-center justify-center">
                      <div class="w-4 h-0.5 bg-[var(--color-gray-500)] transform rotate-45"></div>
                    </div>
                  </div>
                </h3>
                <span class="text-xs text-[var(--color-gray-500)] whitespace-nowrap">
                  {{ chat.lastMessageTime }}
                </span>
              </div>
              <p class="text-[var(--color-gray-500)] font-small truncate">
                {{ getLastMessageDisplay(chat) }}
              </p>
            </div>

            <!-- 읽지 않은 메시지 카운트 -->
            <div v-if="chat.unreadCount && chat.unreadCount > 0" class="ml-2 flex-shrink-0">
              <div
                class="bg-[var(--color-error-300)] text-white rounded-full text-xs min-w-[20px] h-5 flex items-center justify-center font-xs-semibold px-1"
              >
                {{ chat.unreadCount > 99 ? '99+' : chat.unreadCount }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { defineProps, defineEmits, computed } from 'vue';
  import { useNotifications } from '@/features/chat/composables/useNotifications';
  import { useUserStatusStore } from '@/store/userStatus';
  import {
    getChatTypeClass,
    getChatDisplayChar,
    getLastMessageDisplay,
  } from '@/features/chat/utils/chatUtils';

  const { isNotificationEnabled } = useNotifications();
  const userStatusStore = useUserStatusStore();

  /**
   * Props:
   * chats - 채팅 목록 배열
   * isLoading - 로딩 상태
   * error - 에러 메시지
   *
   * 예시:
   * [
   *   {
   *     id: 1,
   *     name: '김경록',
   *     isOnline: true,
   *     userThumbnail: null,
   *     lastMessage: '안녕하세요! 오늘 회의 자료 확인했습니다.',
   *     lastMessageTime: '14:12 전',
   *     unreadCount: 0,
   *     messages: [
   *       { text: '안녕하세요!', time: '14:12', isMe: false },
   *       { text: '네, 확인했습니다.', time: '14:15', isMe: true },
   *     ],
   *   },
   *   ...
   * ]
   */
  const props = defineProps({
    chats: {
      type: Array,
      required: true,
    },
    isLoading: {
      type: Boolean,
      default: false,
    },
    error: {
      type: String,
      default: null,
    },
  });

  /**
   * Emits:
   * select-chat - 채팅을 클릭했을 때 발생, 인자로 선택한 채팅 객체 전달
   * retry-load - 다시 로드 버튼을 클릭했을 때 발생
   *
   * 예시:
   * this.$emit('select-chat', { id: 1, name: '김경록', ... })
   */
  const emit = defineEmits(['select-chat', 'retry-load']);

  // 실제 온라인 상태를 반영한 채팅 목록
  const chatsWithStatus = computed(() => {
    return props.chats.map(chat => {
      if (chat.type === 'DIRECT' && chat.participants && chat.participants.length > 0) {
        // 1:1 채팅에서 상대방의 온라인 상태 확인
        const otherParticipant = chat.participants.find(p => p.userId !== chat.currentUserId);
        if (otherParticipant) {
          return {
            ...chat,
            isOnline: userStatusStore.isUserOnline(otherParticipant.userId),
          };
        }
      }
      return chat;
    });
  });

  // 채팅 선택 처리
  function handleChatSelect(chat) {
    emit('select-chat', chat);
  }
</script>

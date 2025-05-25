<template>
  <div
    class="border-b border-[var(--color-gray-200)] flex-shrink-0 flex-grow-0 overflow-hidden h-full"
  >
    <div class="p-3 border-b border-[var(--color-gray-200)] flex-shrink-0">
      <h2 class="font-section-inner">팀원</h2>
    </div>

    <div class="overflow-y-auto h-full" style="height: calc(100% - 49px)">
      <div
        v-for="(member, idx) in teamMembersWithStatus"
        :key="member.userId || idx"
        class="p-3 border-b border-[var(--color-gray-200)] hover:bg-[var(--color-gray-100)] transition-colors"
      >
        <div class="flex items-center">
          <div class="relative mr-3 flex-shrink-0">
            <div
              class="w-10 h-10 rounded-md overflow-hidden flex items-center justify-center text-white font-one-liner-semibold"
              :class="
                member.profileImageUrl || member.userThumbnailUrl
                  ? ''
                  : 'bg-[var(--color-primary-300)]'
              "
            >
              <!-- 썸네일 이미지가 있으면 표시, 없으면 첫 글자 -->
              <img
                v-if="member.profileImageUrl || member.userThumbnailUrl"
                :src="member.profileImageUrl || member.userThumbnailUrl"
                :alt="member.userName"
                class="w-full h-full object-cover"
              />
              <span v-else>{{ member.userName?.charAt(0) || '?' }}</span>
            </div>
            <div
              class="absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white"
              :class="member.isOnline ? 'bg-green-500' : 'bg-gray-400'"
            ></div>
          </div>

          <div class="flex-grow mr-2">
            <div class="flex items-center gap-2">
              <h3 class="font-one-liner-semibold">{{ member.userName || '이름 없음' }}</h3>

              <!-- 감정 상태 표시 -->
              <div v-if="member.latestMoodType" class="flex items-center gap-2">
                <!-- 감정 아이콘 (배경색 포함) -->
                <div
                  class="relative flex items-center justify-center w-7 h-7 rounded-full text-white text-sm font-semibold select-none cursor-help"
                  :style="{
                    backgroundColor: getMoodColor(member.latestMoodType),
                    opacity: getMoodOpacity(member.latestMoodIntensity),
                  }"
                  :title="`${getMoodLabel(member.latestMoodType)} (강도: ${member.latestMoodIntensity || 0}/100)`"
                >
                  {{ getMoodIcon(member.latestMoodType) }}
                </div>

                <!-- 감정 라벨 텍스트 -->
                <span
                  class="text-xs font-medium select-none cursor-help"
                  :style="{ color: getMoodColor(member.latestMoodType) }"
                  :title="`감정 강도: ${member.latestMoodIntensity}/100`"
                >
                  {{ getMoodLabel(member.latestMoodType) }}
                </span>
              </div>
            </div>
          </div>

          <!-- 액션 버튼들 -->
          <div class="flex items-center gap-1">
            <!-- 업무일지 보기 버튼 -->
            <button
              class="p-2 rounded-md hover:bg-[var(--color-gray-200)] transition-all duration-200 group flex items-center justify-center"
              title="업무일지 보기"
              @click="handleViewWorklog(member)"
            >
              <svg
                class="w-5 h-5 text-[var(--color-gray-500)] group-hover:text-[var(--color-primary-500)] transition-colors"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </button>

            <!-- 채팅하기 버튼 -->
            <button
              :disabled="isCurrentUser(member.userId)"
              :class="[
                'p-2 rounded-md transition-all duration-200 group flex items-center justify-center',
                isCurrentUser(member.userId)
                  ? 'opacity-40 cursor-not-allowed'
                  : 'hover:bg-[var(--color-gray-200)]',
              ]"
              :title="isCurrentUser(member.userId) ? '자신과는 채팅할 수 없습니다' : '채팅하기'"
              @click="handleStartChat(member)"
            >
              <svg
                :class="[
                  'w-5 h-5 transition-colors',
                  isCurrentUser(member.userId)
                    ? 'text-[var(--color-gray-400)]'
                    : 'text-[var(--color-gray-500)] group-hover:text-[var(--color-primary-500)]',
                ]"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                <!-- 동그란 말풍선 -->
                <path
                  d="M12 2C6.48 2 2 6.48 2 12c0 1.54.36 2.98.97 4.29L1 23l6.71-1.97C9.02 21.64 10.46 22 12 22c5.52 0 10-4.48 10-10S17.52 2 12 2z"
                />
                <!-- 점 3개 -->
                <circle cx="8" cy="12" r="1.5" fill="white" />
                <circle cx="12" cy="12" r="1.5" fill="white" />
                <circle cx="16" cy="12" r="1.5" fill="white" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { defineProps, defineEmits, watch, computed } from 'vue';
  import { useAuthStore } from '@/store/auth';
  import { useUserStatusStore } from '@/store/userStatus';

  const authStore = useAuthStore();
  const userStatusStore = useUserStatusStore();

  /**
   * Props:
   * teamMembers - 팀원 목록 배열
   *
   * 예시:
   * [
   *   {
   *     id: 1,
   *     name: '김경록',
   *     isOnline: true,
   *     userThumbnail: null,
   *     workLog: ['오늘 할 일 완료', '새로운 기능 구현', '회의 참석'],
   *   },
   *   ...
   * ]
   */
  const props = defineProps({
    teamMembers: {
      type: Array,
      required: true,
    },
  });

  // 실제 온라인 상태를 반영한 팀원 목록 (최적화)
  const teamMembersWithStatus = computed(() => {
    return props.teamMembers.map(member => {
      const isOnline = userStatusStore.isUserOnline(member.userId);

      // 온라인 상태가 변경되지 않았으면 원본 객체 반환 (참조 유지)
      if (member.isOnline === isOnline) {
        return member;
      }

      // 온라인 상태가 변경된 경우에만 새 객체 생성
      return {
        ...member,
        isOnline,
      };
    });
  });

  // 감정 타입별 아이콘 매핑
  const moodIcons = {
    JOY: '😊',
    SADNESS: '😢',
    ANGER: '😠',
    FEAR: '😨',
    SURPRISE: '😲',
    DISGUST: '🤢',
    NEUTRAL: '😐',
  };

  // 감정 타입별 라벨 매핑
  const moodLabels = {
    JOY: '기쁨',
    SADNESS: '슬픔',
    ANGER: '분노',
    FEAR: '두려움',
    SURPRISE: '놀람',
    DISGUST: '혐오',
    NEUTRAL: '중성',
  };

  // 감정 아이콘 가져오기
  const getMoodIcon = moodType => {
    return moodIcons[moodType] || '😐';
  };

  // 감정 라벨 가져오기
  const getMoodLabel = moodType => {
    return moodLabels[moodType] || '알 수 없음';
  };

  // 감정별 고유 색상
  const getMoodColor = moodType => {
    const colorMap = {
      JOY: '#FEA928', // 기쁨 - 밝은 오렌지
      SADNESS: '#3B82F6', // 슬픔 - 파란색
      ANGER: '#EF4444', // 분노 - 빨간색
      FEAR: '#8B5CF6', // 두려움 - 보라색
      SURPRISE: '#F59E0B', // 놀람 - 주황색
      DISGUST: '#10B981', // 혐오 - 초록색
      NEUTRAL: '#6B7280', // 중성 - 회색
    };
    return colorMap[moodType] || '#6B7280';
  };

  // 감정 강도에 따른 투명도 계산
  const getMoodOpacity = intensity => {
    if (!intensity) return 0.4;
    // 0-100 범위를 0.4-1.0 범위로 변환 (더 잘 보이도록)
    return Math.max(0.4, Math.min(1.0, 0.4 + (intensity / 100) * 0.6));
  };

  // 현재 사용자인지 확인
  const isCurrentUser = userId => {
    return Number(userId) === Number(authStore.userId);
  };

  // 업무일지 보기 핸들러
  const handleViewWorklog = member => {
    alert('업무일지 기능은 아직 구현되지 않았습니다.');
  };

  // 채팅 시작 핸들러
  const handleStartChat = async member => {
    if (isCurrentUser(member.userId)) {
      return;
    }

    // 부모 컴포넌트(RightSidebar)에서 처리하도록 emit
    emit('start-chat', member);
  };

  // 팀원 목록 변경 감지
  watch(
    () => props.teamMembers,
    newMembers => {
      console.log('팀원 목록 변경됨:', newMembers);
    },
    { deep: true }
  );

  /**
   * Emits:
   * view-worklog - 일지보기 버튼 클릭 시 발생, 인자로 팀원 객체 전달
   * start-chat - 대화하기 버튼 클릭 시 발생, 인자로 팀원 객체 전달
   *
   * 예시:
   * this.$emit('view-worklog', { id: 1, name: '김경록', ... })
   * this.$emit('start-chat', { id: 1, name: '김경록', ... })
   */
  const emit = defineEmits(['view-worklog', 'start-chat']);
</script>

<template>
  <div class="w-full max-w-4xl mx-auto">
    <div class="p-8">
      <!-- 팀 헤더 섹션 -->
      <div
        class="flex items-center justify-between mb-8 pb-6 border-b border-[var(--color-gray-200)]"
      >
        <div class="flex items-center space-x-6">
          <!-- 팀 썸네일 -->
          <div
            class="relative w-24 h-24 rounded-2xl overflow-hidden bg-gradient-to-br from-[var(--color-primary-main)] to-[var(--color-secondary-main)] flex items-center justify-center shadow-lg group cursor-pointer"
            :class="{ 'hover:scale-105 transition-transform duration-300': isTeamLeader }"
            @click="openThumbnailModal"
          >
            <img
              v-if="teamThumbnailUrl"
              :src="teamThumbnailUrl"
              :alt="teamName"
              class="w-full h-full object-cover"
            />
            <span v-else class="text-white text-2xl font-bold">
              {{ teamName?.charAt(0) || '?' }}
            </span>
            <!-- 편집 오버레이 -->
            <div
              v-if="isTeamLeader"
              class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center cursor-pointer"
              @click="isThumbnailModalOpen = true"
            >
              <span class="text-white font-one-liner-semibold">편집</span>
            </div>
          </div>

          <!-- 팀 정보 -->
          <div>
            <h2 class="text-2xl font-bold text-[var(--color-gray-900)] mb-1">
              {{ teamName || '팀 이름 없음' }}
            </h2>
            <div class="flex items-center space-x-2">
              <span
                class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-[var(--color-primary-100)] text-[var(--color-primary-main)]"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
                {{ members.length }}명
              </span>
            </div>
          </div>
        </div>

        <!-- 액션 버튼들 -->
        <div class="flex space-x-3">
          <BaseButton
            v-if="isTeamLeader"
            type="error"
            class="px-4 py-2 text-sm font-medium rounded-xl hover:scale-105 transition-transform duration-200"
            @click="isDeleteModalOpen = true"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
              />
            </svg>
            팀 삭제
          </BaseButton>
          <BaseButton
            type="secondary"
            class="px-4 py-2 text-sm font-medium rounded-xl hover:scale-105 transition-transform duration-200"
            @click="isWithdrawModalOpen = true"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
              />
            </svg>
            팀 탈퇴
          </BaseButton>
        </div>
      </div>

      <!-- 팀 소개 섹션 -->
      <div class="mb-8">
        <h3 class="text-lg font-semibold text-[var(--color-gray-900)] mb-4 flex items-center">
          <svg
            class="w-5 h-5 mr-2 text-[var(--color-primary-main)]"
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
          팀 소개
        </h3>
        <div
          class="bg-[var(--color-gray-50)] rounded-2xl p-6 border border-[var(--color-gray-200)]"
        >
          <p class="text-[var(--color-gray-700)] leading-relaxed whitespace-pre-wrap">
            {{ teamIntroduction || '팀 설명이 아직 없습니다.' }}
          </p>
        </div>
      </div>

      <!-- 팀원 목록 섹션 -->
      <div class="mb-8">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-lg font-semibold text-[var(--color-gray-900)] flex items-center">
            <svg
              class="w-5 h-5 mr-2 text-[var(--color-primary-main)]"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
              />
            </svg>
            팀원 목록
          </h3>
          <BaseButton
            v-if="isTeamLeader"
            type="primary"
            class="px-4 py-2 text-sm font-medium rounded-xl hover:scale-105 transition-transform duration-200"
            @click="isInviteModalOpen = true"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 6v6m0 0v6m0-6h6m-6 0H6"
              />
            </svg>
            팀원 초대
          </BaseButton>
        </div>

        <!-- 팀원 목록 테이블 -->
        <div
          class="bg-white rounded-2xl border border-[var(--color-gray-200)] shadow-drop overflow-hidden"
        >
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-[var(--color-gray-50)] border-b border-[var(--color-gray-200)]">
                <tr>
                  <th class="px-6 py-4 text-left font-section-inner text-[var(--color-gray-700)]">
                    이름
                  </th>
                  <th class="px-6 py-4 text-left font-section-inner text-[var(--color-gray-700)]">
                    이메일
                  </th>
                  <th
                    v-if="isTeamLeader"
                    class="px-6 py-4 text-right font-section-inner text-[var(--color-gray-700)]"
                  >
                    관리
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[var(--color-gray-200)]">
                <tr
                  v-for="member in members"
                  :key="member.userId"
                  class="group hover:bg-[var(--color-gray-50)] transition-colors duration-200"
                >
                  <td class="px-6 py-4">
                    <div class="flex items-center space-x-3">
                      <span class="font-one-liner-semibold text-[var(--color-gray-900)]">{{
                        member.userName
                      }}</span>
                      <span
                        v-if="member.userId === teamOwnerId"
                        class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-[var(--color-warning-100)] text-[var(--color-warning-600)]"
                      >
                        팀장
                      </span>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <span class="font-one-liner text-[var(--color-gray-600)]">{{
                      member.email
                    }}</span>
                  </td>
                  <td v-if="isTeamLeader" class="px-6 py-4 text-right">
                    <div
                      v-if="member.userId !== currentUserId"
                      class="flex justify-end space-x-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                    >
                      <button
                        class="px-3 py-1 text-xs font-medium text-[var(--color-error-600)] bg-[var(--color-error-100)] rounded-lg hover:bg-[var(--color-error-200)] transition-colors duration-200"
                        @click="openFireModalForUser(member.userId, member.email)"
                      >
                        추방
                      </button>
                      <button
                        class="px-3 py-1 text-xs font-medium text-[var(--color-secondary-600)] bg-[var(--color-secondary-100)] rounded-lg hover:bg-[var(--color-secondary-200)] transition-colors duration-200"
                        @click="openTransferModalForUser(member.userId, member.email)"
                      >
                        팀장 양도
                      </button>
                    </div>
                    <span v-else class="text-xs text-[var(--color-gray-400)]">본인</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 모달들 -->
  <TeamMemberInviteModal v-model="isInviteModalOpen" @success="fetchMembers" />
  <UpdateTeamThumbnailModal
    v-model="isThumbnailModalOpen"
    :team-id="teamId"
    :current-url="teamThumbnailUrl"
    @submit="fetchTeamInfo"
  />
  <FireMemberModal
    v-if="selectedUserId"
    v-model="showFireModal"
    :user-id="selectedUserId"
    :user-email="getSelectedEmail()"
    :team-id="teamId"
    @kicked="handleAfterKick"
  />
  <TransferLeadershipModal
    v-if="selectedUserId"
    v-model="isTransferModalOpen"
    :email="getSelectedEmail()"
    :team-id="teamId"
    @success="
      () => {
        fetchTeamInfo(teamId);
        selectedUserId = null;
      }
    "
  />
  <WithdrawTeamModal v-model="isWithdrawModalOpen" :team-id="teamId" />
  <DeleteTeamModal v-model="isDeleteModalOpen" :team-id="teamId" />
  <BaseModal v-model="showStatusModal" title="팀원 추방 완료">
    <p class="text-center py-4">팀원이 성공적으로 추방되었습니다.</p>
    <template #footer>
      <BaseButton type="primary" @click="showStatusModal = false">확인</BaseButton>
    </template>
  </BaseModal>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { useAuthStore } from '@/store/auth';
  import api from '@/api/axios';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import TeamMemberInviteModal from '@/features/team/components/TeamMemberInviteModal.vue';
  import UpdateTeamThumbnailModal from '@/features/team/components/UpdateTeamThumbnailModal.vue';
  import FireMemberModal from '@/features/team/components/FireMemberModal.vue';
  import TransferLeadershipModal from '@/features/team/components/TeamLeaderTransferModal.vue';
  import WithdrawTeamModal from '@/features/team/components/WithdrawTeamModal.vue';
  import DeleteTeamModal from '@/features/team/components/DeleteTeamModal.vue';
  import { getTeamMembers } from '@/features/team/api/team';

  const route = useRoute();
  const teamId = computed(() => Number(route.params.teamId));
  const authStore = useAuthStore();

  const currentUserId = ref(null);
  const teamOwnerId = ref(null);
  const teamName = ref('');
  const teamIntroduction = ref('');
  const teamThumbnailUrl = ref(null);

  const isInviteModalOpen = ref(false);
  const isThumbnailModalOpen = ref(false);
  const isTransferModalOpen = ref(false);
  const isWithdrawModalOpen = ref(false);
  const isDeleteModalOpen = ref(false);

  const isTeamLeader = computed(() => Number(teamOwnerId.value) === Number(currentUserId.value));

  const members = ref([]);
  const selectedUserId = ref(null);
  const showFireModal = ref(false);
  const showStatusModal = ref(false);

  onMounted(() => {
    authStore.initAuth();
    currentUserId.value = authStore.userId;
    if (!isNaN(teamId.value) && teamId.value > 0) {
      fetchTeamInfo(teamId.value);
      fetchMembers();
    }
  });

  async function fetchTeamInfo(id) {
    try {
      const res = await api.get(`/teams/teams/${id}`);
      const data = res.data.data;
      teamName.value = data.teamName || '';
      teamIntroduction.value = data.introduction || '';
      teamOwnerId.value = data.userId;
      teamThumbnailUrl.value = data.url || null;
    } catch (err) {
      console.error('팀 정보 불러오기 실패:', err);
    }
  }

  async function fetchMembers() {
    try {
      members.value = await getTeamMembers(teamId.value);
    } catch (e) {
      console.error('팀원 불러오기 실패:', e);
    }
  }

  function openThumbnailModal() {
    if (isTeamLeader.value) isThumbnailModalOpen.value = true;
  }

  function openFireModalForUser(userId, email) {
    selectedUserId.value = userId;
    showFireModal.value = true;
  }

  function openTransferModalForUser(userId, email) {
    selectedUserId.value = userId;
    isTransferModalOpen.value = true;
  }

  function getSelectedEmail() {
    const selected = members.value.find(m => m.userId === selectedUserId.value);
    return selected?.email || '';
  }

  async function handleAfterKick() {
    showFireModal.value = false;
    selectedUserId.value = null;
    await fetchMembers();
    showStatusModal.value = true;
  }
</script>

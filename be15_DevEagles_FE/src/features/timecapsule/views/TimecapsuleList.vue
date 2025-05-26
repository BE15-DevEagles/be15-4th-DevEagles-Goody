<template>
  <div class="page">
    <div class="timecapsule-page">
      <div class="card">
        <div class="timecapsule-top-section">
          <h2 class="font-section-title">타임캡슐 목록</h2>
          <div class="sort-select">
            <select v-model="sortOrder" class="sort-dropdown">
              <option value="desc">최신순</option>
              <option value="asc">오래된순</option>
            </select>
          </div>
        </div>

        <div v-if="loading" class="loading">로딩 중...</div>
        <div v-else-if="sortedCapsules.length === 0" class="empty">
          조회 가능한 타임캡슐이 없습니다.
        </div>
        <div v-else class="capsule-container">
          <div
            v-for="capsule in sortedCapsules"
            :key="capsule.timecapsuleId"
            class="capsule-item"
            @click="openDetailModal(capsule)"
          >
            <div class="capsule-header">
              <span class="capsule-date font-small-semibold"
                >오픈일: {{ formatLocalDate(capsule.openDate) }}</span
              >
              <span class="capsule-created font-small"
                >생성일시: {{ formatLocalDateTime(capsule.createdAt) }}</span
              >
            </div>
            <div class="capsule-content font-one-liner">
              {{
                capsule.timecapsuleContent.length > 50
                  ? capsule.timecapsuleContent.slice(0, 50) + '...'
                  : capsule.timecapsuleContent
              }}
            </div>
          </div>
        </div>

        <!-- 타임캡슐 상세 모달 -->
        <BaseModal v-model="showDetailModal" title="타임캡슐">
          <template #default>
            <div v-if="selectedCapsule" class="modal-content">
              <div class="modal-info">
                <strong>생성 일시:</strong>
                {{ formatLocalDateTime(selectedCapsule.createdAt) }}
              </div>
              <div class="modal-info">
                <strong>오픈 일시:</strong>
                {{ formatLocalDate(selectedCapsule.openDate) }}
              </div>
              <div class="modal-content-text">
                <strong>내용:</strong>
                <div class="content-text">{{ selectedCapsule.timecapsuleContent }}</div>
              </div>
            </div>
            <div v-else>타임캡슐 데이터가 없습니다.</div>
          </template>
          <template #footer>
            <BaseButton type="error" @click="showConfirmDelete = true">삭제</BaseButton>
            <BaseButton type="primary" @click="showDetailModal = false">확인</BaseButton>
          </template>
        </BaseModal>

        <!-- 삭제 확인 모달 -->
        <BaseModal v-model="showConfirmDelete" title="삭제 확인">
          <template #default> 정말로 삭제하시겠습니까? </template>
          <template #footer>
            <BaseButton type="error" @click="showConfirmDelete = false">취소</BaseButton>
            <BaseButton type="primary" @click="onDeleteCapsule">확인</BaseButton>
          </template>
        </BaseModal>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, onMounted, computed } from 'vue';
  import {
    fetchInactiveTimecapsules,
    deleteTimecapsule,
  } from '@/features/timecapsule/api/timecapsuleApi';
  import { useTeamStore } from '@/store/team';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const teamStore = useTeamStore();
  const teamId = computed(() => teamStore.currentTeamId);

  const capsules = ref([]);
  const loading = ref(true);
  const showDetailModal = ref(false);
  const selectedCapsule = ref(null);

  const sortOrder = ref('desc');
  const showConfirmDelete = ref(false);

  const filteredCapsules = computed(() =>
    capsules.value.filter(capsule => capsule.teamId === teamId.value)
  );

  const sortedCapsules = computed(() => {
    const arr = [...filteredCapsules.value];
    arr.sort((a, b) => {
      const t1 = new Date(a.createdAt).getTime();
      const t2 = new Date(b.createdAt).getTime();
      return sortOrder.value === 'desc' ? t2 - t1 : t1 - t2;
    });
    return arr;
  });

  function formatLocalDateTime(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    const pad = n => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }
  function formatLocalDate(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    const pad = n => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
  }

  function openDetailModal(capsule) {
    selectedCapsule.value = capsule;
    showDetailModal.value = true;
  }

  async function onDeleteCapsule() {
    if (!selectedCapsule.value) return;
    try {
      await deleteTimecapsule(selectedCapsule.value.timecapsuleId);
      capsules.value = capsules.value.filter(
        c => c.timecapsuleId !== selectedCapsule.value.timecapsuleId
      );
      showDetailModal.value = false;
      showConfirmDelete.value = false;
    } catch (e) {
      alert('삭제에 실패했습니다.');
    }
  }

  onMounted(async () => {
    loading.value = true;
    try {
      capsules.value = await fetchInactiveTimecapsules();
    } catch (e) {
      capsules.value = [];
    }
    loading.value = false;
  });
</script>

<style scoped>
  /* 페이지 레이아웃 */
  .page {
    display: flex;
    justify-content: center;
    width: 100%;
    font-family: 'Noto Sans KR', sans-serif;
  }

  .timecapsule-page {
    display: flex;
    width: 100%;
    max-width: 800px;
    box-sizing: border-box;
    padding: 1.5rem;
  }

  /* 카드 스타일 */
  .card {
    background: var(--color-neutral-white);
    border: 1px solid var(--color-gray-200);
    border-radius: 0.75rem;
    padding: 1.5rem;
    box-shadow: 0 8px 40px -10px rgba(0, 0, 0, 0.08);
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  /* 타임캡슐 상단 섹션 */
  .timecapsule-top-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
  }

  .sort-select {
    display: flex;
    align-items: center;
  }

  .sort-dropdown {
    border: 1px solid var(--color-gray-200);
    border-radius: 0.5rem;
    padding: 0.5rem 0.75rem;
    font-size: 0.875rem;
    color: var(--color-gray-700);
    background: var(--color-neutral-white);
    cursor: pointer;
  }

  .sort-dropdown:focus {
    outline: none;
    border-color: var(--color-primary-300);
  }

  /* 로딩 및 빈 상태 */
  .loading,
  .empty {
    text-align: center;
    color: var(--color-gray-500);
    padding: 2rem;
    font-size: 0.875rem;
  }

  /* 캡슐 컨테이너 */
  .capsule-container {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  /* 캡슐 아이템 */
  .capsule-item {
    padding: 1rem;
    border: 1px solid var(--color-gray-200);
    border-radius: 0.5rem;
    background: var(--color-neutral-white);
    transition: all 0.2s ease;
    cursor: pointer;
  }

  .capsule-item:hover {
    background: var(--color-gray-50);
    border-color: var(--color-primary-300);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .capsule-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
    font-size: 0.75rem;
  }

  .capsule-date {
    color: var(--color-primary-main);
  }

  .capsule-created {
    color: var(--color-gray-500);
  }

  .capsule-content {
    color: var(--color-gray-700);
    font-size: 0.875rem;
    line-height: 1.4;
  }

  /* 모달 스타일 */
  .modal-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .modal-info {
    font-size: 0.875rem;
    color: var(--color-gray-700);
  }

  .modal-content-text {
    font-size: 0.875rem;
    color: var(--color-gray-700);
  }

  .content-text {
    margin-top: 0.5rem;
    padding: 0.75rem;
    background: var(--color-gray-50);
    border-radius: 0.5rem;
    white-space: pre-line;
    line-height: 1.5;
  }
</style>

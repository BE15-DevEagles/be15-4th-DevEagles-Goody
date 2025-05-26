<template>
  <div class="timecapsule-list-container">
    <div class="list-header">
      <h2 class="list-title">타임캡슐 목록</h2>
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
    <ul v-else class="capsule-list">
      <li
        v-for="capsule in sortedCapsules"
        :key="capsule.timecapsuleId"
        class="capsule-item"
        @click="openDetailModal(capsule)"
      >
        <div class="capsule-header">
          <span class="capsule-date">오픈일: {{ formatLocalDate(capsule.openDate) }}</span>
          <span class="capsule-created"
            >생성일시: {{ formatLocalDateTime(capsule.createdAt) }}</span
          >
        </div>
        <div class="capsule-content">
          {{
            capsule.timecapsuleContent.length > 30
              ? capsule.timecapsuleContent.slice(0, 30) + '...'
              : capsule.timecapsuleContent
          }}
        </div>
      </li>
    </ul>

    <!-- 타임캡슐 상세 모달 -->
    <BaseModal v-model="showDetailModal" title="타임캡슐">
      <template #default>
        <div v-if="selectedCapsule">
          <div class="mb-2">
            <strong>생성 일시:</strong>
            {{ formatLocalDateTime(selectedCapsule.createdAt) }}
          </div>
          <div><strong>내용:</strong> {{ selectedCapsule.timecapsuleContent }}</div>
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
  .timecapsule-list-container {
    background: #fff;
    border-radius: 18px;
    box-shadow: 0 4px 32px rgba(0, 0, 0, 0.08);
    padding: 48px 40px;
    margin: 36px auto;
    max-width: 800px;
    min-width: 400px;
  }
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28px;
  }
  .list-title {
    font-size: 1.6rem;
    font-weight: bold;
    margin-bottom: 0;
    text-align: left;
    color: #222;
  }
  .sort-select {
    display: flex;
    align-items: center;
  }
  .sort-dropdown {
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    padding: 6px 10px;
    font-size: 1rem;
    color: #257180;
    background: #f8f8f8;
  }
  .loading,
  .empty {
    text-align: center;
    color: #999;
    margin: 32px 0;
  }
  .capsule-list {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  .capsule-item {
    border-bottom: 1px solid #e0e0e0;
    padding: 18px 0;
    cursor: pointer;
    border-radius: 10px;
    transition: background 0.2s;
  }
  .capsule-item:hover {
    background: var(--color-primary-100, #e6f1f3);
  }
  .capsule-header {
    font-size: 0.97rem;
    color: var(--color-primary-300, #257180);
    margin-bottom: 7px;
    display: flex;
    justify-content: space-between;
  }
  .capsule-content {
    font-size: 1.13rem;
    color: #222;
    white-space: pre-line;
  }
  .mb-2 {
    margin-bottom: 0.5rem;
  }
</style>

<template>
  <div class="page">
    <div class="timecapsule-create-page">
      <div class="card">
        <div class="timecapsule-top-section">
          <h2 class="font-section-title">타임캡슐 생성</h2>
          <div class="team-info">
            <span class="team-label font-small-semibold">팀:</span>
            <span class="team-name font-small">{{ teamName }}</span>
          </div>
        </div>

        <form class="form-container" @submit.prevent="openConfirmModal">
          <div class="form-group">
            <label for="openDate" class="form-label font-small-semibold">오픈할 날짜</label>
            <VDatePicker
              v-model="form.openDate"
              mode="date"
              :masks="{ input: 'YYYY-MM-DD' }"
              :popover="{ placement: 'bottom-start', visibility: 'click' }"
              color="primary"
              :min-date="new Date()"
            >
              <template #default="{ inputValue, inputEvents }">
                <input
                  class="form-input"
                  :value="inputValue"
                  readonly
                  placeholder="날짜를 선택하세요"
                  v-on="inputEvents"
                />
              </template>
            </VDatePicker>
            <div v-if="dateError" class="error-message">생성 날짜는 오늘 이후만 가능합니다.</div>
          </div>

          <div class="form-group">
            <label for="content" class="form-label font-small-semibold">타임캡슐 내용</label>
            <textarea
              id="content"
              v-model="form.timecapsuleContent"
              rows="8"
              placeholder="타임캡슐 내용을 입력하세요"
              required
              class="form-textarea"
            />
          </div>

          <div class="form-actions">
            <button
              type="submit"
              class="submit-btn"
              :disabled="!teamId || !form.openDate || !form.timecapsuleContent || dateError"
            >
              타임캡슐 생성하기
            </button>
            <div v-if="!teamId" class="error-message">팀을 먼저 선택해주세요.</div>
          </div>
        </form>

        <!-- 생성 전 확인 모달 -->
        <BaseModal v-model="showConfirm" title="타임캡슐 생성 확인">
          <template #default> 타임캡슐을 생성하시겠습니까? </template>
          <template #footer>
            <BaseButton type="error" @click="showConfirm = false">취소</BaseButton>
            <BaseButton type="primary" @click="onSubmitConfirm">확인</BaseButton>
          </template>
        </BaseModal>

        <!-- 생성 완료 안내 모달 -->
        <BaseModal v-model="showSuccess" title="알림">
          <template #default> 타임캡슐 생성이 완료되었습니다. </template>
          <template #footer>
            <BaseButton type="primary" @click="showSuccess = false">확인</BaseButton>
          </template>
        </BaseModal>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { reactive, computed, ref } from 'vue';
  import { useTimecapsule } from '../composables/useTimecapsule';
  import { useTeamStore } from '@/store/team';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const teamStore = useTeamStore();
  const teamId = computed(() => teamStore.currentTeamId);

  const teamName = computed(() => {
    const t = teamStore.teams.find(team => team.teamId === teamId.value);
    return t ? t.teamName : '';
  });

  const today = new Date().toISOString().split('T')[0];

  const form = reactive({
    timecapsuleContent: '',
    openDate: null,
  });

  const dateError = computed(() => {
    if (!form.openDate) return false;
    const selectedDate = new Date(form.openDate).toISOString().split('T')[0];
    return selectedDate <= today;
  });

  const { createTimecapsuleAction } = useTimecapsule();

  const showConfirm = ref(false);
  const showSuccess = ref(false);

  function openConfirmModal() {
    if (!teamId.value || dateError.value) {
      return;
    }
    showConfirm.value = true;
  }

  function resetForm() {
    form.timecapsuleContent = '';
    form.openDate = null;
  }

  async function onSubmitConfirm() {
    showConfirm.value = false;
    try {
      await createTimecapsuleAction({
        timecapsuleContent: form.timecapsuleContent,
        openDate: new Date(form.openDate).toISOString().split('T')[0],
        teamId: teamId.value,
      });
      resetForm();
      showSuccess.value = true;
      console.log('showSuccess:', showSuccess.value);
    } catch (e) {
      console.error(e);
    }
  }
</script>

<style scoped>
  /* 페이지 레이아웃 */
  .page {
    display: flex;
    justify-content: center;
    width: 100%;
    font-family: 'Noto Sans KR', sans-serif;
  }

  .timecapsule-create-page {
    display: flex;
    width: 100%;
    max-width: 600px;
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

  .team-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .team-label {
    color: var(--color-primary-main);
  }

  .team-name {
    color: var(--color-gray-700);
  }

  /* 폼 컨테이너 */
  .form-container {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  /* 폼 그룹 */
  .form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .form-label {
    color: var(--color-gray-700);
  }

  .form-input,
  .form-textarea {
    padding: 0.75rem;
    border: 1px solid var(--color-gray-200);
    border-radius: 0.5rem;
    font-size: 0.875rem;
    font-family: 'Noto Sans KR', sans-serif;
    transition: border-color 0.2s ease;
    cursor: pointer;
  }

  .form-input:focus,
  .form-textarea:focus {
    outline: none;
    border-color: var(--color-primary-300);
  }

  .form-textarea {
    resize: vertical;
    min-height: 120px;
    cursor: text;
  }

  /* 폼 액션 */
  .form-actions {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-top: 1rem;
  }

  .submit-btn {
    width: 100%;
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    border: none;
    padding: 0.875rem;
    border-radius: 0.5rem;
    font-weight: 600;
    font-size: 0.875rem;
    cursor: pointer;
    transition: all 0.2s ease;
    font-family: 'Noto Sans KR', sans-serif;
  }

  .submit-btn:hover:not(:disabled) {
    background: var(--color-primary-400);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .submit-btn:disabled {
    background: var(--color-gray-300);
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
  }

  /* 에러 메시지 */
  .error-message {
    color: var(--color-error-main);
    font-size: 0.75rem;
    margin-top: 0.25rem;
  }
</style>

<style>
  /* v-calendar 달력 스타일 커스터마이징 */
  :root {
    --vc-accent-500: var(--color-primary-main);
    --vc-accent-600: var(--color-primary-400);
    --vc-white: var(--color-neutral-white);
  }

  .vc-container {
    font-family: 'Noto Sans KR', sans-serif !important;
    font-size: 14px !important;
  }

  .vc-nav-title,
  .vc-nav-arrow,
  .vc-day {
    font-family: 'Noto Sans KR', sans-serif !important;
    font-size: 14px !important;
  }

  .vc-nav-title {
    font-weight: 700 !important;
  }

  .vc-weekday {
    font-weight: 600 !important;
  }
</style>

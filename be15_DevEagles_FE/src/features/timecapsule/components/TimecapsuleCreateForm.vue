<template>
  <div class="timecapsule-form-container">
    <form @submit.prevent="openConfirmModal">
      <!-- 현재 팀 이름 표시 -->
      <div class="team-name-row">
        <span class="team-name-label"></span>
        <span class="team-name-value">{{ teamName }}</span>
      </div>
      <div class="form-row">
        <label for="openDate">오픈할 날짜</label>
        <input id="openDate" v-model="form.openDate" type="date" :min="today" required />
      </div>
      <div class="form-row">
        <label for="content">타임캡슐 내용</label>
        <textarea
          id="content"
          v-model="form.timecapsuleContent"
          rows="8"
          placeholder="타임캡슐 내용을 입력하세요"
          required
        />
      </div>
      <button
        type="submit"
        class="submit-btn"
        :disabled="!teamId || !form.openDate || !form.timecapsuleContent || dateError"
      >
        타임캡슐 생성하기
      </button>
      <div v-if="!teamId" class="text-red-500 mt-2 text-sm">팀을 먼저 선택해주세요.</div>
      <div v-if="dateError" class="text-red-500 mt-2 text-sm">
        생성 날짜는 오늘 이후만 가능합니다.
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
</template>

<script setup>
  import { reactive, computed, ref } from 'vue';
  import { useTimecapsule } from '../composables/useTimecapsule';
  import { useTeamStore } from '@/store/team';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const teamStore = useTeamStore();
  const teamId = computed(() => teamStore.currentTeamId);

  // 현재 팀 이름 가져오기
  const teamName = computed(() => {
    const t = teamStore.teams.find(team => team.teamId === teamId.value);
    return t ? t.teamName : '';
  });

  const today = new Date().toISOString().split('T')[0];

  const form = reactive({
    timecapsuleContent: '',
    openDate: '',
  });

  const dateError = computed(() => {
    return form.openDate && form.openDate <= today;
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
    form.openDate = '';
  }

  async function onSubmitConfirm() {
    showConfirm.value = false;
    try {
      await createTimecapsuleAction({
        ...form,
        teamId: teamId.value,
      });
      resetForm();
      showSuccess.value = true;
      console.log('showSuccess:', showSuccess.value); // true가 찍히는지 확인
    } catch (e) {
      console.error(e);
    }
  }
</script>

<style scoped>
  .timecapsule-form-container {
    background: #fff;
    border-radius: 18px;
    box-shadow: 0 4px 32px rgba(0, 0, 0, 0.08);
    padding: 48px 40px;
    margin: 36px auto;
    max-width: 800px;
    min-width: 400px;
  }

  .team-name-row {
    margin-bottom: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.1rem;
  }
  .team-name-label {
    font-weight: 700;
    color: #257180;
    margin-right: 10px;
  }
  .team-name-value {
    font-weight: 500;
    color: #333;
  }

  .form-row {
    margin-bottom: 28px;
  }

  label {
    font-weight: 600;
    margin-bottom: 10px;
    display: block;
    font-size: 1.15rem;
  }

  input,
  textarea {
    width: 100%;
    padding: 16px 18px;
    border: 1px solid #e0e0e0;
    border-radius: 10px;
    font-size: 1.15rem;
    margin-top: 6px;
  }

  .submit-btn {
    width: 100%;
    background: var(--color-primary-300, #257180);
    color: #fff;
    border: none;
    padding: 18px 0;
    border-radius: 10px;
    font-weight: 700;
    font-size: 1.2rem;
    cursor: pointer;
    transition: background 0.2s;
  }
  .submit-btn:hover {
    background: var(--color-primary-400, #257180);
  }

  .text-red-500 {
    color: #e74c3c;
  }
</style>

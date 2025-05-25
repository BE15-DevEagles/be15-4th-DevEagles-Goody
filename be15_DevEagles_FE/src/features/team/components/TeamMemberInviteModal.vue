<template>
  <Teleport to="body">
    <!-- 팀원 초대 메인 모달 -->
    <BaseModal v-model="isOpen" title="팀원 초대">
      <form @submit.prevent="handleSubmit">
        <BaseForm v-model="email" label="팀원 이메일" />
      </form>

      <template #footer>
        <BaseButton type="error" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" :loading="loading" @click="handleSubmit">초대</BaseButton>
      </template>
    </BaseModal>

    <!-- 상태 메시지 모달 -->
    <BaseModal v-model="showStatusModal" :title="modalTitle">
      <p class="modal-text">
        {{ modalMessage }}
      </p>
      <template #footer>
        <div class="modal-footer-buttons">
          <BaseButton :type="isError ? 'error' : 'primary'" @click="closeStatusModal">
            확인
          </BaseButton>
        </div>
      </template>
    </BaseModal>
  </Teleport>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseForm from '@/components/common/components/BaseForm.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import api from '@/api/axios';
  import { useRoute } from 'vue-router';

  const props = defineProps({
    modelValue: {
      type: Boolean,
      required: true,
    },
  });

  const emit = defineEmits(['update:modelValue', 'success', 'fail']);

  const isOpen = computed({
    get: () => props.modelValue,
    set: val => emit('update:modelValue', val),
  });

  const email = ref('');
  const loading = ref(false);

  // 상태 모달
  const showStatusModal = ref(false);
  const modalTitle = ref('');
  const modalMessage = ref('');
  const isError = ref(false);

  const route = useRoute();
  const teamId = computed(() => Number(route.params.teamId));

  const closeModal = () => {
    isOpen.value = false;
    email.value = '';
  };

  const closeStatusModal = () => {
    showStatusModal.value = false;
    if (!isError.value) {
      closeModal();
      emit('success');
    }
  };

  const handleSubmit = async () => {
    if (!email.value.trim()) {
      isError.value = true;
      modalTitle.value = '입력 오류';
      modalMessage.value = '팀원 이메일을 입력해주세요.';
      showStatusModal.value = true;
      return;
    }

    loading.value = true;

    try {
      await api.post(`/team/members/${teamId.value}/invite`, { email: email.value });

      isError.value = false;
      modalTitle.value = '초대 완료';
      modalMessage.value = '팀원이 초대되었습니다.';
    } catch (err) {
      isError.value = true;
      modalTitle.value = '초대 실패';
      modalMessage.value =
        err.response?.data?.message || '팀원 초대에 실패했습니다. 다시 시도해주세요.';
      emit('fail', modalMessage.value);
    } finally {
      loading.value = false;
      showStatusModal.value = true;
    }
  };
</script>

<style scoped>
  .modal-text {
    text-align: center;
    line-height: 1.6;
    margin: 20px 0;
  }
  .modal-footer-buttons {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
  }
</style>

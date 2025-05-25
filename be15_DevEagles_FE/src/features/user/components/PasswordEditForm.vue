<template>
  <form class="form-box" @submit.prevent="handleSubmit">
    <BaseInput
      v-model="form.password"
      type="password"
      label="변경 비밀번호"
      placeholder="새로운 비밀번호를 입력해주세요"
      :error="errors.password"
    />
    <BaseInput
      v-model="form.confirmPassword"
      type="password"
      label="비밀번호 확인"
      placeholder="비밀번호를 다시 입력해주세요"
      :error="errors.confirmPassword"
    />

    <div class="button-group">
      <BaseButton type="primary">확인</BaseButton>
      <BaseButton type="secondary" outline @click="handleCancel">취소</BaseButton>
    </div>

    <BaseModal v-model="isSuccessModalOpen" title="">
      <template #default>
        <p style="text-align: center; font-weight: 600">비밀번호 변경이 완료되었습니다.</p>
      </template>
      <template #footer>
        <div style="display: flex; justify-content: center">
          <BaseButton type="primary" @click="handleSuccessConfirm">확인</BaseButton>
        </div>
      </template>
    </BaseModal>
  </form>
</template>

<script setup>
  import { computed, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import BaseInput from '@/components/common/components/BaseForm.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import { editPassword, emailEditPassword } from '@/features/user/api/user.js';

  const route = useRoute();
  const isFromEmail = computed(() => route.path.startsWith('/findpwd'));
  const email = ref('');
  if (isFromEmail.value) email.value = route.query.email;

  const props = defineProps({
    onCancel: {
      type: Function,
      default: () => {},
    },
    redirectPath: {
      type: String,
      default: '/mypage',
    },
  });

  const form = reactive({
    password: '',
    confirmPassword: '',
  });

  const errors = reactive({
    password: '',
    confirmPassword: '',
  });

  const isSuccessModalOpen = ref(false);
  const router = useRouter();

  const validate = () => {
    let valid = true;
    errors.password = '';
    errors.confirmPassword = '';

    if (!form.password) {
      errors.password = '비밀번호를 입력해주세요.';
      valid = false;
    } else if (
      /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()\-=+{}[\]|;:'",.<>?/]).{8,}$/.test(form.password)
    ) {
      errors.password = '비밀번호는 영문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.';
      valid = false;
    }

    if (!form.confirmPassword) {
      errors.confirmPassword = '비밀번호 확인을 입력해주세요.';
      valid = false;
    } else if (form.confirmPassword !== form.password) {
      errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
      valid = false;
    }

    return valid;
  };

  const handleSubmit = async () => {
    if (!validate()) return;

    try {
      if (isFromEmail.value)
        await emailEditPassword({
          email: email.value,
          password: form.password,
        });
      else await editPassword(form.password);
      isSuccessModalOpen.value = true;
    } catch (e) {
      console.error(e);
    }
  };

  const handleCancel = () => {
    props.onCancel?.();
  };

  const handleSuccessConfirm = () => {
    isSuccessModalOpen.value = false;
    router.push(props.redirectPath);
  };
</script>

<style scoped>
  .form-box {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .button-group {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 10px;
  }
</style>

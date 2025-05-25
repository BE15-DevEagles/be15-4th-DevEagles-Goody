<template>
  <div class="password-edit-view">
    <h1 class="page-title">비밀번호 변경</h1>
    <PasswordEditForm :redirect-path="'/mypage'" :on-cancel="handleCancel" />
  </div>
</template>

<script setup>
  import { useRouter } from 'vue-router';
  import PasswordEditForm from '@/features/user/components/PasswordEditForm.vue';
  const router = useRouter();
  const handleCancel = () => router.back();
</script>

<style scoped>
  .page-title {
    font-size: 22px;
    font-weight: bold;
    margin-bottom: 40px;
  }

  .password-edit-view {
    padding: 40px 20px;
    max-width: 440px;
    margin: 0 auto;
    background: white;
    border: 1px solid var(--color-gray-200);
    border-radius: 10px;
    margin-top: 90px;
  }

  .page-title {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 24px;
    text-align: center;
  }
</style>

<!-- src/components/common/components/ConfirmModal.vue -->
<script setup>
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const props = defineProps({
    modelValue: Boolean,
    title: String,
    message: String,
    confirmText: {
      type: String,
      default: '확인',
    },
    cancelText: {
      type: String,
      default: '취소',
    },
  });

  const emit = defineEmits(['update:modelValue', 'confirm']);

  function close() {
    emit('update:modelValue', false);
  }

  function confirm() {
    emit('confirm');
    close();
  }
</script>

<template>
  <BaseModal :model-value="modelValue" :title="title" @update:model-value="close">
    <template #default>
      {{ message }}
    </template>
    <template #footer>
      <BaseButton @click="confirm">{{ confirmText }}</BaseButton>
      <BaseButton @click="close">{{ cancelText }}</BaseButton>
    </template>
  </BaseModal>
</template>

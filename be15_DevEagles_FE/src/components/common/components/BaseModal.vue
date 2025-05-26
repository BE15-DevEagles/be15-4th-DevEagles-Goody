<template>
  <Teleport to="body">
    <transition name="modal-fade">
      <div v-if="modelValue" class="modal-backdrop" @click.self="closeModal">
        <div :class="['modal', animationClass]">
          <div class="modal-header">
            <h2 class="modal-title">{{ title }}</h2>
            <button class="modal-close" @click="closeModal">&times;</button>
          </div>
          <div class="modal-body">
            <slot></slot>
          </div>
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer">
              <BaseButton @click="closeModal">닫기</BaseButton>
            </slot>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script>
  import BaseButton from '@/components/common/components/BaseButton.vue';

  export default {
    name: 'BaseModal',
    components: { BaseButton },
    props: {
      modelValue: {
        type: Boolean,
        required: true,
      },
      title: {
        type: String,
        default: '',
      },
      animationClass: {
        type: String,
        default: '',
      },
    },
    emits: ['update:modelValue'],
    methods: {
      closeModal() {
        this.$emit('update:modelValue', false);
      },
    },
  };
</script>

<style>
  .modal-fade-enter-active,
  .modal-fade-leave-active {
    transition: opacity 0.3s ease;
  }

  .modal-fade-enter-from,
  .modal-fade-leave-to {
    opacity: 0;
  }

  .back-in-left {
    animation: backInLeft 0.6s both;
  }

  @keyframes backInLeft {
    0% {
      opacity: 0;
      transform: translateX(-800px) scale(0.7);
    }
    80% {
      opacity: 0.7;
      transform: translateX(0px) scale(1.05);
    }
    100% {
      opacity: 1;
      transform: translateX(0) scale(1);
    }
  }
</style>

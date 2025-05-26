<template>
  <BaseModal v-model="visible" title="할 일 작성">
    <div v-for="(todo, index) in todos" :key="index" class="todo-form-group">
      <BaseButton
        v-if="todos.length > 1"
        type="secondary"
        size="sm"
        class="remove-btn"
        @click="removeTodo(index)"
      >
        -
      </BaseButton>

      <div class="form-row">
        <label>시작 날짜</label>
        <VDatePicker
          v-model="todo.startDate"
          mode="dateTime"
          is24hr
          :masks="{ input: 'YYYY.MM.DD HH:mm' }"
          :popover="{ placement: 'bottom-start', visibility: 'click' }"
          color="primary"
        >
          <template #default="{ inputValue, inputEvents }">
            <input class="input" :value="inputValue" readonly v-on="inputEvents" />
          </template>
        </VDatePicker>
      </div>

      <div class="form-row">
        <label>마감 날짜</label>
        <VDatePicker
          v-model="todo.dueDate"
          mode="dateTime"
          is24hr
          :masks="{ input: 'YYYY.MM.DD HH:mm' }"
          :popover="{ placement: 'bottom-start', visibility: 'click' }"
          color="primary"
        >
          <template #default="{ inputValue, inputEvents }">
            <input class="input" :value="inputValue" readonly v-on="inputEvents" />
          </template>
        </VDatePicker>
      </div>

      <div class="form-row">
        <label>할 일</label>
        <input v-model="todo.content" class="input" />
      </div>
    </div>

    <div class="add-btn-wrapper">
      <BaseButton type="primary" size="sm" @click="addTodo">+</BaseButton>
    </div>

    <template #footer>
      <BaseButton type="error" @click="visible = false">취소</BaseButton>
      <BaseButton type="primary" :disabled="!canSubmit" @click="confirmVisible = true">
        저장
      </BaseButton>
    </template>
  </BaseModal>

  <BaseModal v-model="confirmVisible" title="확인">
    <div class="confirm-message">할 일을 저장하시겠습니까?</div>
    <template #footer>
      <BaseButton type="error" @click="confirmVisible = false">아니오</BaseButton>
      <BaseButton type="primary" @click="submitTodos">네</BaseButton>
    </template>
  </BaseModal>
</template>

<script setup>
  import { ref, watch, computed } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { useToast } from 'vue-toastification';
  import { createTodos } from '@/features/todolist/api/api';
  import { useTeamStore } from '@/store/team';

  const props = defineProps({
    modelValue: Boolean,
  });
  const emit = defineEmits(['update:modelValue', 'submitted']);

  const toast = useToast();
  const visible = ref(props.modelValue);
  watch(
    () => props.modelValue,
    val => (visible.value = val)
  );
  watch(visible, val => emit('update:modelValue', val));

  const confirmVisible = ref(false);

  const teamStore = useTeamStore();
  const currentTeamId = computed(() => teamStore.currentTeamId);

  const todos = ref([
    {
      content: '',
      startDate: new Date(),
      dueDate: new Date(),
    },
  ]);

  const addTodo = () => {
    todos.value.push({
      content: '',
      startDate: new Date(),
      dueDate: new Date(),
    });
  };

  const removeTodo = index => {
    todos.value.splice(index, 1);
  };

  const canSubmit = computed(() => todos.value.every(todo => todo.content.trim() !== ''));

  const submitTodos = async () => {
    if (!currentTeamId.value) {
      toast.error('⚠️ 팀이 선택되지 않았습니다.');
      return;
    }

    for (let i = 0; i < todos.value.length; i++) {
      const todo = todos.value[i];
      if (new Date(todo.startDate) > new Date(todo.dueDate)) {
        toast.error(`❌ ${i + 1}번째 할 일의 마감 날짜가 시작 날짜보다 빠릅니다.`);
        todo.startDate = new Date();
        todo.dueDate = new Date();
        confirmVisible.value = false;
        return;
      }
    }

    const payload = todos.value.map(todo => ({
      teamId: currentTeamId.value,
      content: todo.content.trim(),
      startDate: todo.startDate,
      dueDate: todo.dueDate,
    }));

    try {
      await createTodos(payload);
      emit('submitted');
      visible.value = false;
      confirmVisible.value = false;
      location.reload();
    } catch (err) {
      toast.error('등록에 실패했습니다.');
      console.error('❌ Todo 등록 실패:', err);
      confirmVisible.value = false;
    }
  };
</script>

<style scoped>
  /* 입력 필드 스타일 */
  .input {
    width: 100%;
    padding: 0.625rem 0.875rem;
    border: 1px solid var(--color-gray-300);
    border-radius: 0.5rem;
    background: var(--color-neutral-white);
    font-size: 14px;
    font-family: 'Noto Sans KR', sans-serif;
    line-height: 21px;
    color: var(--color-neutral-dark);
    transition:
      border-color 120ms ease,
      box-shadow 120ms ease;
  }

  .input:focus {
    outline: none;
    border-color: var(--color-primary-main);
    box-shadow: 0 0 0 2px rgba(37, 113, 128, 0.2);
  }

  /* 투두 폼 그룹 */
  .todo-form-group {
    border: 1px solid var(--color-gray-200);
    padding: 1.5rem;
    margin-bottom: 1rem;
    border-radius: 0.75rem;
    position: relative;
    background: var(--color-gray-50);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    font-family: 'Noto Sans KR', sans-serif;
  }

  /* 폼 행 */
  .form-row {
    display: flex;
    flex-direction: column;
    margin-bottom: 1rem;
  }

  .form-row:last-child {
    margin-bottom: 0;
  }

  /* 라벨 스타일 */
  .form-row label {
    display: block;
    margin-bottom: 0.5rem;
    font-size: 14px;
    font-weight: 700;
    line-height: 16px;
    color: var(--color-gray-700);
  }

  /* 제거 버튼 */
  .remove-btn {
    position: absolute;
    top: 0.75rem;
    right: 0.75rem;
    padding: 0;
    width: 32px;
    height: 32px;
    font-size: 18px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    border-radius: 50%;
    background: var(--color-error-300);
    color: var(--color-neutral-white);
    border: none;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .remove-btn:hover {
    background: var(--color-error-400);
    transform: scale(1.1);
  }

  /* 추가 버튼 래퍼 */
  .add-btn-wrapper {
    display: flex;
    justify-content: center;
    margin: 1rem 0 1.5rem;
  }

  .add-btn-wrapper button {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    font-size: 20px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
  }

  .add-btn-wrapper button:hover {
    transform: scale(1.1);
  }

  /* 확인 메시지 */
  .confirm-message {
    padding: 1.5rem;
    font-size: 16px;
    font-weight: 600;
    text-align: center;
    color: var(--color-gray-700);
    font-family: 'Noto Sans KR', sans-serif;
  }
</style>

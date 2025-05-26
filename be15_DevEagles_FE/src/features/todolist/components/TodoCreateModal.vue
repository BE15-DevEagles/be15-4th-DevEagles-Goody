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
  .input {
    padding: 10px 12px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 16px;
    height: 42px;
  }

  .todo-form-group {
    border: 1px solid #ccc;
    padding: 1rem;
    margin-bottom: 1rem;
    border-radius: 8px;
    position: relative;
    background-color: var(--color-neutral-white);
  }

  .form-row {
    display: flex;
    flex-direction: column;
    margin-bottom: 0.5rem;
  }

  .remove-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: 0;
    width: 28px;
    height: 28px;
    font-size: 1.1rem;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
  }

  .add-btn-wrapper {
    display: flex;
    justify-content: center;
    margin: 0.5rem 0 0.8rem;
    font-size: 1.1rem;
    font-weight: bold;
  }

  .confirm-message {
    padding: 1.5rem;
    font-size: 1rem;
    text-align: center;
  }
</style>

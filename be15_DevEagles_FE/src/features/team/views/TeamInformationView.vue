<template>
  <div class="min-h-screen bg-white p-6">
    <div class="max-w-4xl mx-auto">
      <!-- 팀 정보 카드 -->
      <div
        class="bg-white/90 backdrop-blur-sm rounded-3xl shadow-xl border border-[var(--color-primary-100)] overflow-hidden"
      >
        <TeamInfo />
      </div>
    </div>
  </div>
</template>

<script setup>
  import TeamInfo from '@/features/team/components/TeamInformationForm.vue';
</script>

<style scoped></style>

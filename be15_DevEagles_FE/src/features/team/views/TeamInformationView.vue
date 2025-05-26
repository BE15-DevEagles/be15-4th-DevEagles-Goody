<template>
  <div class="page">
    <div class="team-info-page">
      <div class="card">
        <TeamInfo />
      </div>
    </div>
  </div>
</template>

<script setup>
  import TeamInfo from '@/features/team/components/TeamInformationForm.vue';
</script>

<style scoped>
  /* 페이지 레이아웃 */
  .page {
    display: flex;
    justify-content: center;
    width: 100%;
    font-family: 'Noto Sans KR', sans-serif;
  }

  .team-info-page {
    display: flex;
    width: 100%;
    max-width: 1000px;
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
</style>

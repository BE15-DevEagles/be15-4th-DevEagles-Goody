<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import ConfirmModal from '@/features/worklog/components//ConfirmModal.vue';
  import { generateSummary } from '@/features/worklog/api/worklog.js';
  import api from '@/api/axios';
  import { useTeamStore } from '@/store/team.js';

  const route = useRoute();
  const router = useRouter();
  const teamStore = useTeamStore();

  const form = ref({
    summary: '',
    content: '',
    notice: '',
    plan: '',
    date: '',
    username: route.query.username,
    teamname: '',
    teamId: Number(route.query.teamId),
  });

  const showSubmitModal = ref(false);
  const showCancelModal = ref(false);
  const loading = ref(false);
  const showTooltip = ref(false);
  const currentStep = ref(1);

  const isFormComplete = computed(() => {
    return (
      form.value.summary &&
      form.value.content &&
      form.value.notice &&
      form.value.plan &&
      form.value.date &&
      form.value.username &&
      form.value.teamname
    );
  });

  const formProgress = computed(() => {
    const fields = ['summary', 'content', 'notice', 'plan', 'date'];
    const completed = fields.filter(field => form.value[field]).length;
    return Math.round((completed / fields.length) * 100);
  });

  const steps = [
    { id: 1, title: '기본 정보', description: '작성일자와 기본 정보를 입력하세요' },
    { id: 2, title: '업무 내용', description: '오늘 수행한 업무를 상세히 작성하세요' },
    { id: 3, title: '특이사항', description: '특별한 이슈나 문제점을 기록하세요' },
    { id: 4, title: '계획 수립', description: '내일 수행할 업무를 계획하세요' },
    { id: 5, title: '검토 및 제출', description: '작성한 내용을 검토하고 제출하세요' },
  ];

  onMounted(async () => {
    // 오늘 날짜로 기본 설정 (즉시 실행)
    const today = new Date().toISOString().split('T')[0];
    form.value.date = today;

    // 팀 정보가 이미 있는 경우 바로 사용
    if (form.value.teamId && teamStore.currentTeamId === form.value.teamId) {
      form.value.teamname = teamStore.currentTeam?.teamName || '가상팀';
      return;
    }

    if (form.value.teamId) {
      try {
        await teamStore.setCurrentTeamLite(form.value.teamId);
        form.value.teamname = teamStore.currentTeam?.teamName || '가상팀';
      } catch (error) {
        console.error('팀 설정 실패:', error);
        form.value.teamname = '가상팀';
      }
    }
  });

  function nextStep() {
    if (currentStep.value < steps.length) {
      currentStep.value++;
    }
  }

  function prevStep() {
    if (currentStep.value > 1) {
      currentStep.value--;
    }
  }

  function goToStep(step) {
    currentStep.value = step;
  }

  function openSubmitModal() {
    showSubmitModal.value = true;
  }

  function openCancelModal() {
    showCancelModal.value = true;
  }

  function formatDateTime(date) {
    return date ? `${date} 00:00:00` : null;
  }

  async function submit() {
    try {
      const payload = {
        summary: form.value.summary,
        workContent: form.value.content,
        note: form.value.notice,
        planContent: form.value.plan,
        writtenAt: formatDateTime(form.value.date),
      };

      await api.post(`/worklog/register/${form.value.teamId}`, payload);
      alert('등록이 완료되었습니다.');
      router.push('/worklog/my');
    } catch (error) {
      console.error('등록 실패:', error);
      alert('등록 중 오류가 발생했습니다.');
    } finally {
      showSubmitModal.value = false;
    }
  }

  function cancel() {
    showCancelModal.value = false;
    router.push('/worklog/my');
  }

  async function generateSummaryHandler() {
    try {
      loading.value = true;
      const response = await generateSummary({
        workContent: form.value.content,
        note: form.value.notice,
      });
      form.value.summary = response.data?.data?.summary || '';
    } catch (error) {
      console.error('요약 실패:', error);
      alert('AI 요약에 실패했습니다.');
    } finally {
      loading.value = false;
    }
  }

  function spellCheck() {
    // 추후 맞춤법 API 연동
    alert('맞춤법 검사 기능은 준비 중입니다.');
  }
</script>

<template>
  <div class="worklog-create-container">
    <!-- 헤더 섹션 -->
    <div class="header-section">
      <div class="header-content">
        <div class="progress-section">
          <div class="progress-info">
            <span class="progress-text">작성 진행률</span>
            <span class="progress-percentage">{{ formProgress }}%</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: formProgress + '%' }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 단계 네비게이션 -->
    <div class="steps-section">
      <div class="steps-container">
        <div
          v-for="(step, index) in steps"
          :key="step.id"
          class="step-item"
          :class="{
            active: currentStep === step.id,
            completed: currentStep > step.id,
            clickable: index < currentStep || formProgress > index * 20,
          }"
          @click="goToStep(step.id)"
        >
          <div class="step-circle">
            <svg
              v-if="currentStep > step.id"
              class="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 13l4 4L19 7"
              ></path>
            </svg>
            <span v-else>{{ step.id }}</span>
          </div>
          <div class="step-content">
            <div class="step-title">{{ step.title }}</div>
            <div class="step-description">{{ step.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 폼 섹션 -->
    <div class="form-section">
      <!-- 1단계: 기본 정보 -->
      <div v-if="currentStep === 1" class="form-step">
        <div class="step-header">
          <h2 class="step-title">기본 정보</h2>
          <p class="step-subtitle">업무일지의 기본 정보를 입력해주세요</p>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">작성일자 <span class="required">*</span></label>
            <input v-model="form.date" type="date" class="form-input" required />
          </div>

          <div class="form-group">
            <label class="form-label">작성자</label>
            <input v-model="form.username" type="text" class="form-input" disabled />
          </div>

          <div class="form-group">
            <label class="form-label">팀명</label>
            <input v-model="form.teamname" type="text" class="form-input" disabled />
          </div>
        </div>

        <div class="step-actions">
          <BaseButton class="btn-next" :disabled="!form.date" @click="nextStep">
            다음 단계
            <svg class="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              ></path>
            </svg>
          </BaseButton>
        </div>
      </div>

      <!-- 2단계: 업무 내용 -->
      <div v-if="currentStep === 2" class="form-step">
        <div class="step-header">
          <h2 class="step-title">업무 내용</h2>
          <p class="step-subtitle">오늘 수행한 업무를 구체적으로 작성해주세요</p>
        </div>

        <div class="form-group">
          <label class="form-label">업무 내용 <span class="required">*</span></label>
          <div class="textarea-container">
            <textarea
              v-model="form.content"
              class="form-textarea"
              placeholder="오늘 수행한 업무를 상세히 작성해주세요.&#10;&#10;예시:&#10;- 프로젝트 A 요구사항 분석&#10;- 데이터베이스 설계 및 구현&#10;- 팀 회의 참석 및 진행상황 공유"
              rows="8"
              required
            ></textarea>
            <div class="textarea-footer">
              <span class="char-count">{{ form.content.length }}/1000</span>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <BaseButton class="btn-secondary" @click="prevStep">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 19l-7-7 7-7"
              ></path>
            </svg>
            이전 단계
          </BaseButton>
          <BaseButton class="btn-next" :disabled="!form.content" @click="nextStep">
            다음 단계
            <svg class="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              ></path>
            </svg>
          </BaseButton>
        </div>
      </div>

      <!-- 3단계: 특이사항 -->
      <div v-if="currentStep === 3" class="form-step">
        <div class="step-header">
          <h2 class="step-title">특이사항</h2>
          <p class="step-subtitle">업무 중 발생한 이슈나 특별한 사항을 기록해주세요</p>
        </div>

        <div class="form-group">
          <label class="form-label">특이사항 <span class="required">*</span></label>
          <div class="textarea-container">
            <textarea
              v-model="form.notice"
              class="form-textarea"
              placeholder="업무 중 발생한 이슈, 문제점, 개선사항 등을 작성해주세요.&#10;&#10;예시:&#10;- 서버 성능 이슈로 인한 지연 발생&#10;- 클라이언트 요구사항 변경&#10;- 새로운 기술 도입 검토 필요"
              rows="6"
              required
            ></textarea>
            <div class="textarea-footer">
              <span class="char-count">{{ form.notice.length }}/500</span>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <BaseButton class="btn-secondary" @click="prevStep">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 19l-7-7 7-7"
              ></path>
            </svg>
            이전 단계
          </BaseButton>
          <BaseButton class="btn-next" :disabled="!form.notice" @click="nextStep">
            다음 단계
            <svg class="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              ></path>
            </svg>
          </BaseButton>
        </div>
      </div>

      <!-- 4단계: 익일 계획 -->
      <div v-if="currentStep === 4" class="form-step">
        <div class="step-header">
          <h2 class="step-title">익일 업무계획</h2>
          <p class="step-subtitle">내일 수행할 업무를 구체적으로 계획해주세요</p>
        </div>

        <div class="form-group">
          <label class="form-label">익일 업무계획 <span class="required">*</span></label>
          <div class="textarea-container">
            <textarea
              v-model="form.plan"
              class="form-textarea"
              placeholder="내일 수행할 업무를 우선순위별로 작성해주세요.&#10;&#10;예시:&#10;1. 프로젝트 B 설계 문서 작성&#10;2. 코드 리뷰 및 버그 수정&#10;3. 팀 스프린트 회의 준비"
              rows="6"
              required
            ></textarea>
            <div class="textarea-footer">
              <span class="char-count">{{ form.plan.length }}/500</span>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <BaseButton class="btn-secondary" @click="prevStep">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 19l-7-7 7-7"
              ></path>
            </svg>
            이전 단계
          </BaseButton>
          <BaseButton class="btn-next" :disabled="!form.plan" @click="nextStep">
            다음 단계
            <svg class="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              ></path>
            </svg>
          </BaseButton>
        </div>
      </div>

      <!-- 5단계: 검토 및 제출 -->
      <div v-if="currentStep === 5" class="form-step">
        <div class="step-header">
          <h2 class="step-title">검토 및 제출</h2>
          <p class="step-subtitle">작성한 내용을 검토하고 제목을 생성해주세요</p>
        </div>

        <div class="form-group">
          <label class="form-label">업무일지 제목 <span class="required">*</span></label>
          <div class="title-input-group">
            <input
              v-model="form.summary"
              class="form-input"
              placeholder="제목을 입력하거나 AI 요약을 사용하세요"
              required
            />
            <div class="ai-summary-section">
              <button
                class="btn-ai-summary"
                :disabled="!form.content || !form.notice || loading"
                @click="generateSummaryHandler"
              >
                <svg
                  v-if="loading"
                  class="w-4 h-4 mr-2 animate-spin"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    class="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                  ></circle>
                  <path
                    class="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                <svg
                  v-else
                  class="w-4 h-4 mr-2"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13 10V3L4 14h7v7l9-11h-7z"
                  ></path>
                </svg>
                {{ loading ? '생성 중...' : 'AI 요약' }}
              </button>
              <div class="ai-tooltip">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  ></path>
                </svg>
                <span class="tooltip-text"
                  >업무 내용과 특이사항을 기반으로 제목을 자동 생성합니다</span
                >
              </div>
            </div>
          </div>
        </div>

        <!-- 작성 내용 미리보기 -->
        <div class="preview-section">
          <h3 class="preview-title">작성 내용 미리보기</h3>
          <div class="preview-grid">
            <div class="preview-card">
              <h4>업무 내용</h4>
              <p>{{ form.content || '작성된 내용이 없습니다.' }}</p>
            </div>
            <div class="preview-card">
              <h4>특이사항</h4>
              <p>{{ form.notice || '작성된 내용이 없습니다.' }}</p>
            </div>
            <div class="preview-card">
              <h4>익일 계획</h4>
              <p>{{ form.plan || '작성된 내용이 없습니다.' }}</p>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <BaseButton class="btn-secondary" @click="prevStep">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 19l-7-7 7-7"
              ></path>
            </svg>
            이전 단계
          </BaseButton>

          <div class="final-actions">
            <BaseButton class="btn-spell-check" :disabled="!isFormComplete" @click="spellCheck">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                ></path>
              </svg>
              맞춤법 검사
            </BaseButton>

            <BaseButton class="btn-submit" :disabled="!isFormComplete" @click="openSubmitModal">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
                ></path>
              </svg>
              업무일지 제출
            </BaseButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 하단 액션 바 -->
    <div class="bottom-actions">
      <BaseButton class="btn-cancel" @click="openCancelModal">
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M6 18L18 6M6 6l12 12"
          ></path>
        </svg>
        작성 취소
      </BaseButton>

      <div class="step-indicator">{{ currentStep }} / {{ steps.length }}</div>
    </div>

    <!-- 모달들 -->
    <ConfirmModal
      v-model="showSubmitModal"
      title="업무일지 제출"
      message="작성한 업무일지를 제출하시겠습니까?"
      confirm-text="제출"
      cancel-text="취소"
      @confirm="submit"
    />

    <ConfirmModal
      v-model="showCancelModal"
      title="작성 취소"
      message="작성 중인 내용이 모두 삭제됩니다. 정말 취소하시겠습니까?"
      confirm-text="취소"
      cancel-text="계속 작성"
      @confirm="cancel"
    />

    <!-- 로딩 오버레이 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p class="loading-text">AI가 제목을 생성하고 있습니다...</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .worklog-create-container {
    max-width: 900px;
    margin: 0 auto;
    padding: 24px;
    background-color: var(--color-neutral-white);
    min-height: 100vh;
    box-sizing: border-box;
  }

  /* 헤더 섹션 */
  .header-section {
    margin-bottom: 20px;
  }

  .header-content {
    background: var(--color-neutral-white);
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--color-gray-200);
  }

  .title-section {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-neutral-dark);
    margin: 0 0 4px 0;
  }

  .page-subtitle {
    font-size: 14px;
    color: var(--color-gray-500);
    margin: 0;
  }

  .progress-section {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .progress-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .progress-text {
    font-size: 12px;
    color: var(--color-gray-700);
    font-weight: 500;
  }

  .progress-percentage {
    font-size: 12px;
    color: var(--color-primary-main);
    font-weight: 600;
  }

  .progress-bar {
    height: 4px;
    background: var(--color-gray-200);
    border-radius: 2px;
    overflow: hidden;
  }

  .progress-fill {
    height: 100%;
    background: var(--color-primary-main);
    border-radius: 2px;
    transition: width 0.3s ease;
  }

  /* 단계 네비게이션 */
  .steps-section {
    margin-bottom: 20px;
  }

  .steps-container {
    background: var(--color-neutral-white);
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--color-gray-200);
    display: flex;
    gap: 8px;
    overflow-x: auto;
  }

  .step-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    border-radius: 8px;
    transition: all 0.2s ease;
    min-width: 120px;
    cursor: default;
  }

  .step-item.clickable {
    cursor: pointer;
  }

  .step-item.clickable:hover {
    background: var(--color-gray-50);
  }

  .step-item.active {
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
  }

  .step-item.completed {
    background: var(--color-primary-50);
    border: 1px solid var(--color-primary-main);
  }

  .step-circle {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 10px;
    flex-shrink: 0;
  }

  .step-item:not(.active) .step-circle {
    background: var(--color-gray-200);
    color: var(--color-gray-500);
  }

  .step-item.active .step-circle {
    background: rgba(255, 255, 255, 0.2);
    color: var(--color-neutral-white);
  }

  .step-item.completed .step-circle {
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
  }

  .step-content {
    flex: 1;
  }

  .step-title {
    font-weight: 600;
    font-size: 12px;
    margin-bottom: 2px;
  }

  .step-description {
    font-size: 10px;
    opacity: 0.8;
    line-height: 1.2;
  }

  /* 폼 섹션 */
  .form-section {
    background: var(--color-neutral-white);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--color-gray-200);
  }

  .form-step {
    min-height: 300px;
  }

  .step-header {
    margin-bottom: 20px;
    text-align: center;
  }

  .step-header .step-title {
    font-size: 20px;
    font-weight: 700;
    color: var(--color-neutral-dark);
    margin: 0 0 4px 0;
  }

  .step-subtitle {
    font-size: 14px;
    color: var(--color-gray-500);
    margin: 0;
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 20px;
    width: 100%;
    box-sizing: border-box;
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: 0;
  }

  .form-label {
    font-weight: 600;
    color: var(--color-gray-700);
    font-size: 14px;
  }

  .required {
    color: var(--color-error-300);
  }

  .form-input {
    padding: 12px 16px;
    border: 1px solid var(--color-gray-200);
    border-radius: 8px;
    font-size: 14px;
    transition: all 0.2s ease;
    background: var(--color-neutral-white);
    width: 100%;
    box-sizing: border-box;
    min-width: 0;
  }

  .form-input:focus {
    outline: none;
    border-color: var(--color-primary-main);
    box-shadow: 0 0 0 3px rgba(37, 113, 128, 0.1);
  }

  .form-input:disabled {
    background: var(--color-gray-50);
    color: var(--color-gray-500);
  }

  .textarea-container {
    position: relative;
  }

  .form-textarea {
    width: 100%;
    padding: 16px;
    border: 1px solid var(--color-gray-200);
    border-radius: 8px;
    font-size: 14px;
    line-height: 1.6;
    resize: vertical;
    min-height: 120px;
    transition: all 0.2s ease;
    font-family: inherit;
    box-sizing: border-box;
  }

  .form-textarea:focus {
    outline: none;
    border-color: var(--color-primary-main);
    box-shadow: 0 0 0 3px rgba(37, 113, 128, 0.1);
  }

  .textarea-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
  }

  .char-count {
    font-size: 12px;
    color: var(--color-gray-500);
  }

  .title-input-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .ai-summary-section {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .btn-ai-summary {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    border: none;
    border-radius: 8px;
    font-weight: 500;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .btn-ai-summary:hover:not(:disabled) {
    transform: translateY(-1px);
    background: var(--color-primary-300);
    box-shadow: 0 4px 12px rgba(37, 113, 128, 0.3);
  }

  .btn-ai-summary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }

  .ai-tooltip {
    position: relative;
    display: flex;
    align-items: center;
    color: var(--color-gray-500);
    cursor: help;
  }

  .tooltip-text {
    position: absolute;
    bottom: 100%;
    left: 50%;
    transform: translateX(-50%);
    background: var(--color-neutral-dark);
    color: var(--color-neutral-white);
    padding: 8px 12px;
    border-radius: 6px;
    font-size: 12px;
    white-space: nowrap;
    opacity: 0;
    visibility: hidden;
    transition: all 0.2s ease;
    margin-bottom: 8px;
  }

  .ai-tooltip:hover .tooltip-text {
    opacity: 1;
    visibility: visible;
  }

  /* 미리보기 섹션 */
  .preview-section {
    margin-top: 32px;
    padding-top: 32px;
    border-top: 1px solid var(--color-gray-200);
  }

  .preview-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-neutral-dark);
    margin: 0 0 16px 0;
  }

  .preview-grid {
    display: grid;
    gap: 16px;
  }

  .preview-card {
    background: var(--color-gray-50);
    border: 1px solid var(--color-gray-200);
    border-radius: 8px;
    padding: 16px;
  }

  .preview-card h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-gray-700);
    margin: 0 0 8px 0;
  }

  .preview-card p {
    font-size: 14px;
    color: var(--color-gray-500);
    line-height: 1.5;
    margin: 0;
    max-height: 100px;
    overflow-y: auto;
  }

  /* 액션 버튼들 */
  .step-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid var(--color-gray-200);
  }

  .final-actions {
    display: flex;
    gap: 12px;
  }

  .btn-next,
  .btn-submit {
    display: flex;
    align-items: center;
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .btn-next:hover:not(:disabled),
  .btn-submit:hover:not(:disabled) {
    transform: translateY(-1px);
    background: var(--color-primary-300);
    box-shadow: 0 4px 12px rgba(37, 113, 128, 0.3);
  }

  .btn-secondary {
    display: flex;
    align-items: center;
    background: var(--color-neutral-white);
    color: var(--color-primary-main);
    border: 1px solid var(--color-primary-main);
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .btn-secondary:hover {
    background: var(--color-gray-50);
  }

  .btn-spell-check {
    display: flex;
    align-items: center;
    background: var(--color-warning-300);
    color: var(--color-neutral-white);
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .btn-spell-check:hover:not(:disabled) {
    background: var(--color-warning-400);
  }

  .btn-next:disabled,
  .btn-submit:disabled,
  .btn-spell-check:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }

  /* 하단 액션 바 */
  .bottom-actions {
    background: var(--color-neutral-white);
    border-radius: 12px;
    padding: 12px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--color-gray-200);
  }

  .btn-cancel {
    display: flex;
    align-items: center;
    background: var(--color-error-300);
    color: var(--color-neutral-white);
    border: none;
    padding: 10px 20px;
    border-radius: 8px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .btn-cancel:hover {
    background: var(--color-error-400);
  }

  .step-indicator {
    font-size: 14px;
    color: var(--color-gray-500);
    font-weight: 500;
  }

  /* 로딩 오버레이 */
  .loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
  }

  .loading-content {
    background: var(--color-neutral-white);
    border-radius: 16px;
    padding: 32px;
    text-align: center;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  }

  .loading-spinner {
    width: 48px;
    height: 48px;
    border: 4px solid var(--color-gray-200);
    border-top: 4px solid var(--color-primary-main);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 16px;
  }

  .loading-text {
    font-size: 16px;
    color: var(--color-gray-700);
    margin: 0;
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  .animate-spin {
    animation: spin 1s linear infinite;
  }

  /* 반응형 */
  @media (max-width: 768px) {
    .worklog-create-container {
      padding: 16px;
    }

    .header-content {
      padding: 24px;
    }

    .steps-container {
      flex-direction: column;
      gap: 12px;
    }

    .step-item {
      min-width: auto;
    }

    .form-section {
      padding: 24px;
    }

    .form-grid {
      grid-template-columns: 1fr;
    }

    .step-actions {
      flex-direction: column;
      gap: 16px;
    }

    .final-actions {
      flex-direction: column;
      width: 100%;
    }

    .bottom-actions {
      flex-direction: column;
      gap: 12px;
    }
  }
</style>

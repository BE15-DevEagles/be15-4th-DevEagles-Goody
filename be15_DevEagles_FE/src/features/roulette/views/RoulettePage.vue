<template>
  <div class="roulette-container">
    <h2 class="roulette-title">룰렛</h2>
    <form class="option-form" @submit.prevent="addOption">
      <input
        v-model="newOption"
        type="text"
        class="option-input"
        placeholder="룰렛 옵션을 입력하세요"
        :maxlength="20"
      />
      <button class="add-btn" :disabled="!canAddOption">추가</button>
    </form>
    <div class="option-list">
      <span v-for="(option, idx) in currentOptions" :key="idx" class="option-chip">
        {{ option }}
        <button class="delete-btn" @click="removeOption(idx)">×</button>
      </span>
    </div>

    <!-- ★ 초기화 버튼 추가 (옵션이 있을 때만 표시) -->
    <div v-if="currentOptions.length > 0" class="reset-section">
      <button class="reset-btn" @click="showConfirmModal = true">모든 옵션 초기화</button>
    </div>

    <button
      class="spin-btn"
      :disabled="currentOptions.length < 2 || currentOptions.length > 10 || spinning"
      @click="spinRoulette"
    >
      룰렛 돌리기
    </button>

    <div class="roulette-wheel-wrap">
      <svg
        v-if="currentOptions.length >= 2"
        class="roulette-wheel"
        :width="size"
        :height="size"
        :viewBox="`0 0 ${size} ${size}`"
      >
        <g :transform="`rotate(${rotation % 360}, ${center}, ${center})`">
          <path
            v-for="(option, idx) in currentOptions"
            :key="'path-' + idx"
            :d="getSlicePath(idx)"
            :fill="getColor(idx)"
            class="wheel-slice"
          />
          <!-- slice 경계선 -->
          <line
            v-for="(option, idx) in currentOptions"
            :key="'line-' + idx"
            :x1="center"
            :y1="center"
            :x2="center + radius * Math.cos(getSliceStartAngle(idx))"
            :y2="center + radius * Math.sin(getSliceStartAngle(idx))"
            stroke="black"
            stroke-width="1"
          />
          <text
            v-for="(option, idx) in currentOptions"
            :key="'text-' + idx"
            :x="getTextPos(idx).x"
            :y="getTextPos(idx).y"
            class="wheel-text"
          >
            {{ option }}
          </text>
        </g>
      </svg>
      <div v-if="currentOptions.length >= 2" class="pointer">▼</div>
    </div>

    <div v-if="result !== null" class="result-box">
      🎉 <span class="result-text">{{ currentOptions[result] }}</span>
      <div class="mt-3">
        <button class="chat-btn" @click="sendToChat">채팅방으로 전송하기</button>
      </div>
    </div>

    <!-- ★ 확인 모달 (BaseModal 사용) -->
    <BaseModal v-model="showConfirmModal" title="초기화 확인">
      <template #default>
        <p>모든 옵션을 초기화 하시겠습니까?</p>
        <p class="modal-info">(현재 {{ currentOptions.length }}개 옵션)</p>
      </template>
      <template #footer>
        <button class="modal-btn modal-btn-cancel" @click="showConfirmModal = false">취소</button>
        <button class="modal-btn modal-btn-confirm" @click="confirmReset">확인</button>
      </template>
    </BaseModal>

    <!-- ★ 완료 모달 (BaseModal 사용, 애니메이션 없음) -->
    <BaseModal v-model="showCompleteModal" title="초기화 완료">
      <template #default>
        <p>초기화 되었습니다</p>
      </template>
      <template #footer>
        <button class="modal-btn modal-btn-confirm" @click="showCompleteModal = false">확인</button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
  import { ref, computed, watch } from 'vue';
  import { useTeamStore } from '@/store/team';
  import BaseModal from '@/components/common/components/BaseModal.vue';

  const teamStore = useTeamStore();
  const teamId = computed(() => teamStore.currentTeamId);

  const size = 320;
  const center = size / 2;
  const radius = center - 12;
  const newOption = ref('');
  const optionsMap = ref({});
  const spinning = ref(false);
  const rotation = ref(0);
  const result = ref(null);

  // ★ 모달 상태 추가
  const showConfirmModal = ref(false);
  const showCompleteModal = ref(false);

  const colorList = [
    '#257180',
    '#4db6ac',
    '#ffb74d',
    '#e57373',
    '#9575cd',
    '#ffd54f',
    '#4fc3f7',
    '#f06292',
    '#aed581',
    '#90a4ae',
  ];

  const currentOptions = computed(() => optionsMap.value[teamId.value] || []);
  const canAddOption = computed(
    () => newOption.value.trim().length > 0 && currentOptions.value.length < 10
  );

  function addOption() {
    if (!canAddOption.value) return;
    if (!optionsMap.value[teamId.value]) optionsMap.value[teamId.value] = [];
    optionsMap.value[teamId.value].push(newOption.value.trim());
    newOption.value = '';
    result.value = null;
  }

  function removeOption(idx) {
    if (!optionsMap.value[teamId.value]) return;
    optionsMap.value[teamId.value].splice(idx, 1);
    result.value = null;
  }

  // ★ 초기화 확인 함수 (BaseModal 사용)
  function confirmReset() {
    if (!optionsMap.value[teamId.value] || optionsMap.value[teamId.value].length === 0) return;

    // 옵션 초기화
    optionsMap.value[teamId.value] = [];
    result.value = null;
    newOption.value = '';

    // 확인 모달 닫고 완료 모달 열기
    showConfirmModal.value = false;
    showCompleteModal.value = true;
  }

  // slice 시작 각도 (기본, 보정 없음)
  function getSliceStartAngle(idx) {
    const n = currentOptions.value.length;
    const angle = (2 * Math.PI) / n;
    return idx * angle - Math.PI / 2;
  }

  // slice path (기본, 보정 없음)
  function getSlicePath(idx) {
    const n = currentOptions.value.length;
    const angle = (2 * Math.PI) / n;
    const start = idx * angle - Math.PI / 2;
    const end = start + angle;
    const x1 = center + radius * Math.cos(start);
    const y1 = center + radius * Math.sin(start);
    const x2 = center + radius * Math.cos(end);
    const y2 = center + radius * Math.sin(end);
    const largeArc = angle > Math.PI ? 1 : 0;
    return `M ${center} ${center}
        L ${x1} ${y1}
        A ${radius} ${radius} 0 ${largeArc} 1 ${x2} ${y2}
        Z`;
  }

  function getColor(idx) {
    return colorList[idx % colorList.length];
  }

  function getTextPos(idx) {
    const n = currentOptions.value.length;
    const angle = ((idx + 0.5) * (2 * Math.PI)) / n - Math.PI / 2;
    const r = radius * 0.65;
    return {
      x: center + r * Math.cos(angle),
      y: center + r * Math.sin(angle) + 6,
    };
  }

  function getResultIndex(n, angle) {
    const normalizedAngle = (360 - (angle % 360)) % 360;
    const sliceAngle = 360 / n;
    return Math.floor(normalizedAngle / sliceAngle) % n;
  }

  function spinRoulette() {
    if (spinning.value || currentOptions.value.length < 2) return;
    spinning.value = true;
    result.value = null;

    const n = currentOptions.value.length;
    const randomAngle = Math.random() * 360;
    const baseRotation = 360 * 5;
    const finalAngle = baseRotation + randomAngle;
    const startRotation = rotation.value % 360;
    const diff = ((finalAngle - startRotation + 360 * 10) % 360) + baseRotation;

    const duration = 1500;
    const start = performance.now();

    function animate(now) {
      const elapsed = now - start;
      if (elapsed < duration) {
        const t = elapsed / duration;
        const eased = 1 - Math.pow(1 - t, 4);
        rotation.value = startRotation + eased * diff;
        requestAnimationFrame(animate);
      } else {
        rotation.value = startRotation + diff;
        spinning.value = false;
        const angle = ((rotation.value % 360) + 360) % 360;
        result.value = getResultIndex(n, angle);
      }
    }

    requestAnimationFrame(animate);
  }

  // 채팅방으로 전송하기 버튼 클릭 시
  function sendToChat() {
    if (result.value !== null) {
      alert(`채팅방으로 "${currentOptions.value[result.value]}" 전송!`);
      // 실제로는 emit, API 호출 등으로 연결
    }
  }

  watch(teamId, () => {
    result.value = null;
    newOption.value = '';
  });
</script>

<style scoped>
  .roulette-container {
    background: #fff;
    border-radius: 18px;
    box-shadow: 0 4px 32px rgba(0, 0, 0, 0.08);
    padding: 40px 32px;
    margin: 40px auto;
    max-width: 480px;
    min-width: 340px;
  }
  .roulette-title {
    font-size: 1.5rem;
    font-weight: bold;
    color: #000;
    margin-bottom: 24px;
    text-align: center;
  }
  .option-form {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }
  .option-input {
    flex: 1;
    padding: 10px 14px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    font-size: 1rem;
  }
  .add-btn {
    background: var(--color-primary-300, #257180);
    color: #fff;
    border: none;
    padding: 10px 18px;
    border-radius: 8px;
    font-weight: 600;
    font-size: 1rem;
    cursor: pointer;
    transition: background 0.2s;
  }
  .add-btn:disabled {
    background: #bdbdbd;
    cursor: not-allowed;
  }
  .option-list {
    margin-bottom: 18px;
  }
  .option-chip {
    display: inline-flex;
    align-items: center;
    background: #e6f1f3;
    color: #257180;
    border-radius: 16px;
    padding: 6px 14px;
    margin: 0 6px 6px 0;
    font-size: 1rem;
  }
  .delete-btn {
    background: none;
    border: none;
    color: #e74c3c;
    font-size: 1.1rem;
    margin-left: 4px;
    cursor: pointer;
  }

  /* ★ 초기화 버튼 스타일 */
  .reset-section {
    margin-bottom: 18px;
    text-align: center;
  }
  .reset-btn {
    background: #e74c3c;
    color: #fff;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
  }
  .reset-btn:hover {
    background: #c0392b;
  }

  .spin-btn {
    width: 100%;
    background: var(--color-primary-300, #257180);
    color: #fff;
    border: none;
    padding: 14px 0;
    border-radius: 10px;
    font-weight: 700;
    font-size: 1.2rem;
    cursor: pointer;
    margin-bottom: 24px;
    transition: background 0.2s;
  }
  .spin-btn:disabled {
    background: #bdbdbd;
    cursor: not-allowed;
  }
  .roulette-wheel-wrap {
    position: relative;
    width: 320px;
    height: 320px;
    margin: 0 auto 18px auto;
  }
  .roulette-wheel {
    border-radius: 50%;
    background: #f8f8f8;
    box-shadow: 0 2px 12px rgba(37, 113, 128, 0.08);
  }
  .pointer {
    position: absolute;
    left: 50%;
    top: -18px;
    transform: translateX(-50%);
    font-size: 2.2rem;
    color: #e74c3c;
    user-select: none;
    font-weight: bold;
    text-shadow: 0 2px 8px #fff;
  }
  .wheel-text {
    font-size: 1.03rem;
    fill: #222;
    text-anchor: middle;
    alignment-baseline: middle;
    font-weight: 600;
    pointer-events: none;
  }
  .result-box {
    text-align: center;
    font-size: 1.22rem;
    font-weight: bold;
    color: #257180;
    margin-top: 18px;
  }
  .result-text {
    color: #e74c3c;
    font-size: 1.25em;
    margin-left: 6px;
  }
  .chat-btn {
    margin-top: 10px;
    padding: 10px 20px;
    background: #257180;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
  }
  .chat-btn:hover {
    background: #1e5a6a;
  }

  /* ★ 모달 버튼 스타일 */
  .modal-info {
    font-size: 0.9rem;
    color: #888;
    margin-top: 8px;
  }
  .modal-btn {
    padding: 10px 20px;
    border: none;
    border-radius: 6px;
    font-size: 0.95rem;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
    margin-left: 8px;
  }
  .modal-btn-cancel {
    background: #e74c3c;
    color: #fff;
  }
  .modal-btn-cancel:hover {
    background: #c0392b;
  }
  .modal-btn-confirm {
    background: #257180;
    color: #fff;
  }
  .modal-btn-confirm:hover {
    background: #1e5a6a;
  }
</style>

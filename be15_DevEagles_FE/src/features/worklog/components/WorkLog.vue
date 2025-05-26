<template>
  <tr class="worklog-row" @click="$emit('click', log)">
    <td class="author-cell">
      <div class="author-info">
        <div class="author-avatar">{{ log.userName.charAt(0) }}</div>
        <div class="author-details">
          <div class="author-name">{{ log.userName }}</div>
          <div class="author-role">팀원</div>
        </div>
      </div>
    </td>
    <td class="title-cell">
      <div class="title-content">
        <h4 class="worklog-title">{{ log.summary }}</h4>
        <p class="worklog-preview">{{ log.workContent?.substring(0, 80) }}...</p>
      </div>
    </td>
    <td class="date-cell">
      <div class="date-info">
        <div class="date-primary">{{ formattedDate }}</div>
        <div class="date-time">{{ formattedTime }}</div>
      </div>
    </td>
    <td class="action-cell">
      <div class="action-buttons">
        <button class="action-btn view-btn" title="상세보기" @click.stop="$emit('click', log)">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            ></path>
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
            ></path>
          </svg>
        </button>
        <button class="action-btn menu-btn" title="더보기" @click.stop="toggleMenu">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"
            ></path>
          </svg>
        </button>
      </div>
    </td>
  </tr>
</template>

<script setup>
  import { computed } from 'vue';

  const props = defineProps({
    log: {
      type: Object,
      required: true,
    },
  });

  const emit = defineEmits(['click']);

  const formattedDate = computed(() => {
    return props.log.writtenAt?.slice(0, 10) ?? '';
  });

  const formattedTime = computed(() => {
    const time = props.log.writtenAt?.slice(11, 16);
    return time ? `${time}` : '';
  });

  function toggleMenu() {
    // 메뉴 토글 로직 (추후 구현)
    console.log('메뉴 토글');
  }
</script>

<style scoped>
  .worklog-row {
    cursor: pointer;
    transition: all 0.2s ease;
    border-bottom: 1px solid var(--color-gray-200);
  }

  .worklog-row:hover {
    background-color: var(--color-gray-50);
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .worklog-row:hover .action-buttons {
    opacity: 1;
  }

  .author-cell {
    padding: 16px;
    vertical-align: middle;
  }

  .author-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .author-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
  }

  .author-details {
    flex: 1;
  }

  .author-name {
    font-weight: 600;
    color: var(--color-neutral-dark);
    font-size: 14px;
    margin-bottom: 2px;
  }

  .author-role {
    font-size: 12px;
    color: var(--color-gray-500);
  }

  .title-cell {
    padding: 16px;
    vertical-align: middle;
  }

  .title-content {
    max-width: 400px;
  }

  .worklog-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--color-neutral-dark);
    margin: 0 0 4px 0;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .worklog-preview {
    font-size: 13px;
    color: var(--color-gray-500);
    line-height: 1.4;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .date-cell {
    padding: 16px;
    vertical-align: middle;
    text-align: center;
  }

  .date-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
  }

  .date-primary {
    font-weight: 600;
    color: var(--color-gray-700);
    font-size: 14px;
  }

  .date-time {
    font-size: 12px;
    color: var(--color-gray-500);
  }

  .action-cell {
    padding: 16px;
    vertical-align: middle;
    text-align: center;
  }

  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 8px;
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  .action-btn {
    width: 32px;
    height: 32px;
    border: none;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .view-btn {
    background: var(--color-primary-50);
    color: var(--color-primary-main);
  }

  .view-btn:hover {
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
  }

  .menu-btn {
    background: var(--color-gray-50);
    color: var(--color-gray-500);
  }

  .menu-btn:hover {
    background: var(--color-gray-200);
    color: var(--color-gray-700);
  }

  /* 모바일 반응형 */
  @media (max-width: 768px) {
    .author-info {
      gap: 8px;
    }

    .author-avatar {
      width: 32px;
      height: 32px;
      font-size: 12px;
    }

    .title-content {
      max-width: 200px;
    }

    .worklog-title {
      font-size: 14px;
    }

    .worklog-preview {
      font-size: 12px;
    }

    .action-buttons {
      opacity: 1;
    }
  }
</style>

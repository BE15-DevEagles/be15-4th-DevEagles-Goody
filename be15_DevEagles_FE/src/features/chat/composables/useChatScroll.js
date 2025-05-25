import { ref, nextTick } from 'vue';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('useChatScroll');

export function useChatScroll() {
  const shouldScrollToBottom = ref(true);
  const lastScrollTop = ref(0);
  let scrollContainer = null;

  const setScrollContainer = element => {
    scrollContainer = element;
    logger.info('[useChatScroll] 스크롤 컨테이너 설정됨');
  };

  const scrollToBottom = (force = false) => {
    if (!scrollContainer) return;

    if (force || shouldScrollToBottom.value) {
      nextTick(() => {
        if (scrollContainer) {
          scrollContainer.scrollTop = scrollContainer.scrollHeight;
          shouldScrollToBottom.value = true;
          logger.info('[useChatScroll] 하단으로 스크롤됨');
        }
      });
    }
  };

  const maintainScrollPosition = async callback => {
    if (!scrollContainer) return;

    const currentScrollTop = scrollContainer.scrollTop;
    const currentScrollHeight = scrollContainer.scrollHeight;

    const result = await callback();

    nextTick(() => {
      if (scrollContainer) {
        const newScrollHeight = scrollContainer.scrollHeight;
        const heightDifference = newScrollHeight - currentScrollHeight;
        scrollContainer.scrollTop = currentScrollTop + heightDifference;

        logger.info('[useChatScroll] 스크롤 위치 유지됨:', {
          이전높이: currentScrollHeight,
          새높이: newScrollHeight,
          높이차이: heightDifference,
          조정된스크롤: scrollContainer.scrollTop,
        });
      }
    });

    return result;
  };

  const handleScroll = (event, onScrollToTop) => {
    const { scrollTop, scrollHeight, clientHeight } = event.target;

    if (scrollTop <= 30 && onScrollToTop) {
      onScrollToTop(scrollTop);
    }

    const isNearBottom = scrollHeight - scrollTop - clientHeight < 100;

    if (scrollTop < lastScrollTop.value - 20) {
      shouldScrollToBottom.value = false;
    } else if (isNearBottom) {
      shouldScrollToBottom.value = true;
    }

    lastScrollTop.value = scrollTop;
  };

  const reset = () => {
    shouldScrollToBottom.value = true;
    lastScrollTop.value = 0;
    logger.info('[useChatScroll] 상태 초기화됨');
  };

  return {
    shouldScrollToBottom,
    setScrollContainer,
    scrollToBottom,
    maintainScrollPosition,
    handleScroll,
    reset,
  };
}

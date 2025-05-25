import { ref, computed } from 'vue';
import { useToast } from 'vue-toastification';
import {
  getChatNotificationSetting,
  toggleChatNotification,
  getAllNotificationSettings,
} from '@/features/chat/api/chatService.js';
import ChatNotificationToast from '@/features/chat/components/ChatNotificationToast.vue';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('useNotifications');

const notificationSettings = ref(new Map());
const isLoading = ref(false);

export function useNotifications() {
  const toast = useToast();

  const isNotificationEnabled = chatId => {
    const setting = notificationSettings.value.get(chatId);
    logger.info(`[useNotifications] 알림 설정 확인: chatId=${chatId}, setting=${setting}`);
    return setting !== false;
  };

  const toggleNotification = async chatId => {
    try {
      isLoading.value = true;
      logger.info(`[useNotifications] 알림 설정 토글 시작: chatId=${chatId}`);

      const result = await toggleChatNotification(chatId);
      logger.info(`[useNotifications] 백엔드 응답:`, result);

      const notificationEnabled = result.notificationEnabled;
      notificationSettings.value.set(chatId, notificationEnabled);

      logger.info(
        `[useNotifications] 알림 설정 업데이트 완료: chatId=${chatId}, enabled=${notificationEnabled}`
      );
      logger.debug(
        `[useNotifications] 현재 모든 설정:`,
        Object.fromEntries(notificationSettings.value)
      );

      return notificationEnabled;
    } catch (error) {
      logger.error('[useNotifications] 알림 설정 변경 실패:', error);
      toast.error('알림 설정 변경에 실패했습니다.');
      const currentSetting = notificationSettings.value.get(chatId);
      return currentSetting !== false;
    } finally {
      isLoading.value = false;
    }
  };

  const setNotificationEnabled = (chatId, enabled) => {
    logger.info(`[useNotifications] 알림 설정 직접 설정: chatId=${chatId}, enabled=${enabled}`);
    notificationSettings.value.set(chatId, enabled);
  };

  const initializeFromChatData = chats => {
    logger.info(`[useNotifications] 채팅방 데이터에서 알림 설정 초기화: ${chats.length}개 채팅방`);

    chats.forEach(chat => {
      if (chat.id && chat.notificationEnabled !== undefined) {
        notificationSettings.value.set(chat.id, chat.notificationEnabled);
        logger.info(
          `[useNotifications] 초기화: chatId=${chat.id}, enabled=${chat.notificationEnabled}`
        );
      }
    });

    logger.debug(
      `[useNotifications] 초기화 완료, 현재 설정:`,
      Object.fromEntries(notificationSettings.value)
    );
  };

  const loadNotificationSettings = async () => {
    try {
      isLoading.value = true;
      logger.info('[useNotifications] 백엔드에서 모든 알림 설정 로드 시작');

      const settings = await getAllNotificationSettings();
      logger.debug('[useNotifications] 백엔드 알림 설정:', settings);

      notificationSettings.value.clear();

      settings.forEach(setting => {
        notificationSettings.value.set(setting.chatRoomId, setting.notificationEnabled);
        logger.info(
          `[useNotifications] 로드: chatId=${setting.chatRoomId}, enabled=${setting.notificationEnabled}`
        );
      });

      logger.debug(
        `[useNotifications] 모든 설정 로드 완료:`,
        Object.fromEntries(notificationSettings.value)
      );
    } catch (error) {
      logger.error('[useNotifications] 알림 설정 로드 실패:', error);
    } finally {
      isLoading.value = false;
    }
  };

  const loadChatNotificationSetting = async chatId => {
    try {
      logger.info(`[useNotifications] 특정 채팅방 알림 설정 로드: chatId=${chatId}`);

      const setting = await getChatNotificationSetting(chatId);
      logger.info(`[useNotifications] 백엔드 응답: chatId=${chatId}, enabled=${setting}`);

      notificationSettings.value.set(chatId, setting);
      return setting;
    } catch (error) {
      logger.error(`[useNotifications] 채팅방 알림 설정 로드 실패: chatId=${chatId}`, error);
      notificationSettings.value.set(chatId, true);
      return true;
    }
  };

  const showChatNotification = message => {
    const { chatroomId, senderName, content } = message;

    logger.debug('[useNotifications] showChatNotification 호출됨:', {
      chatroomId,
      senderName,
      content: content?.substring(0, 30),
      messageObject: message,
    });

    const enabled = isNotificationEnabled(chatroomId);
    logger.info(
      `[useNotifications] 알림 표시 시도: chatId=${chatroomId}, enabled=${enabled}, sender=${senderName}`
    );

    if (!enabled) {
      logger.info(`[useNotifications] 알림 비활성화로 인해 표시하지 않음: chatId=${chatroomId}`);
      return;
    }

    logger.info('[useNotifications] 알림 활성화됨, 브라우저 알림 및 토스트 표시 시작');

    if ('Notification' in window && Notification.permission === 'granted') {
      logger.info('[useNotifications] 브라우저 알림 표시');
      new Notification(`${senderName}님이 메시지를 보냈습니다`, {
        body: content,
        icon: '/favicon.ico',
        tag: `chat-${chatroomId}`,
      });
    } else {
      logger.info('[useNotifications] 브라우저 알림 권한 없음 또는 지원 안함:', {
        hasNotification: 'Notification' in window,
        permission: 'Notification' in window ? Notification.permission : 'N/A',
      });
    }

    logger.info('[useNotifications] 토스트 알림 표시');
    toast(
      {
        component: ChatNotificationToast,
        props: {
          senderName: senderName,
          content: content,
          timeout: 8000,
        },
      },
      {
        type: 'default',
        timeout: 8000,
        closeOnClick: false,
        pauseOnFocusLoss: false,
        pauseOnHover: true,
        draggable: true,
        draggablePercent: 0.6,
        showCloseButtonOnHover: false,
        hideProgressBar: true,
        closeButton: false,
        icon: false,
        onClick: () => {
          window.focus();
        },
      }
    );

    logger.info(`[useNotifications] 알림 표시 완료: chatId=${chatroomId}`);
  };

  const requestNotificationPermission = async () => {
    if ('Notification' in window) {
      if (Notification.permission === 'default') {
        const permission = await Notification.requestPermission();
        return permission === 'granted';
      }
      return Notification.permission === 'granted';
    }
    return false;
  };

  const hasNotificationSupport = computed(() => {
    return 'Notification' in window;
  });

  const notificationPermission = computed(() => {
    return 'Notification' in window ? Notification.permission : 'denied';
  });

  return {
    isNotificationEnabled,
    toggleNotification,
    setNotificationEnabled,
    initializeFromChatData,
    loadNotificationSettings,
    loadChatNotificationSetting,
    showChatNotification,
    requestNotificationPermission,
    hasNotificationSupport,
    notificationPermission,
    isLoading,
  };
}

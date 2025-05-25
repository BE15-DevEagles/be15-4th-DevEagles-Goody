import { ref, computed } from 'vue';
import { useToast } from 'vue-toastification';
import {
  getChatNotificationSetting,
  toggleChatNotification,
  getAllNotificationSettings,
} from '@/features/chat/api/chatService.js';
import ChatNotificationToast from '@/features/chat/components/ChatNotificationToast.vue';

const notificationSettings = ref(new Map());
const isLoading = ref(false);

export function useNotifications() {
  const toast = useToast();

  const isNotificationEnabled = chatId => {
    const setting = notificationSettings.value.get(chatId);
    console.log(`[useNotifications] 알림 설정 확인: chatId=${chatId}, setting=${setting}`);

    // 설정이 명시적으로 false인 경우에만 false 반환, 그 외에는 true (기본값)
    return setting !== false;
  };

  const toggleNotification = async chatId => {
    try {
      isLoading.value = true;
      console.log(`[useNotifications] 알림 설정 토글 시작: chatId=${chatId}`);

      const result = await toggleChatNotification(chatId);
      console.log(`[useNotifications] 백엔드 응답:`, result);

      // 백엔드에서 반환된 설정값으로 업데이트
      const notificationEnabled = result.notificationEnabled;
      notificationSettings.value.set(chatId, notificationEnabled);

      console.log(
        `[useNotifications] 알림 설정 업데이트 완료: chatId=${chatId}, enabled=${notificationEnabled}`
      );
      console.log(
        `[useNotifications] 현재 모든 설정:`,
        Object.fromEntries(notificationSettings.value)
      );

      return notificationEnabled;
    } catch (error) {
      console.error('[useNotifications] 알림 설정 변경 실패:', error);
      toast.error('알림 설정 변경에 실패했습니다.');

      // 실패 시 현재 설정 반환
      const currentSetting = notificationSettings.value.get(chatId);
      return currentSetting !== false;
    } finally {
      isLoading.value = false;
    }
  };

  const setNotificationEnabled = (chatId, enabled) => {
    console.log(`[useNotifications] 알림 설정 직접 설정: chatId=${chatId}, enabled=${enabled}`);
    notificationSettings.value.set(chatId, enabled);
  };

  // 채팅방 데이터에서 알림 설정 초기화
  const initializeFromChatData = chats => {
    console.log(`[useNotifications] 채팅방 데이터에서 알림 설정 초기화: ${chats.length}개 채팅방`);

    chats.forEach(chat => {
      if (chat.id && chat.notificationEnabled !== undefined) {
        notificationSettings.value.set(chat.id, chat.notificationEnabled);
        console.log(
          `[useNotifications] 초기화: chatId=${chat.id}, enabled=${chat.notificationEnabled}`
        );
      }
    });

    console.log(
      `[useNotifications] 초기화 완료, 현재 설정:`,
      Object.fromEntries(notificationSettings.value)
    );
  };

  // 백엔드에서 모든 알림 설정 로드
  const loadNotificationSettings = async () => {
    try {
      isLoading.value = true;
      console.log('[useNotifications] 백엔드에서 모든 알림 설정 로드 시작');

      const settings = await getAllNotificationSettings();
      console.log('[useNotifications] 백엔드 알림 설정:', settings);

      // 기존 설정 클리어
      notificationSettings.value.clear();

      // 설정을 Map으로 변환
      settings.forEach(setting => {
        notificationSettings.value.set(setting.chatRoomId, setting.notificationEnabled);
        console.log(
          `[useNotifications] 로드: chatId=${setting.chatRoomId}, enabled=${setting.notificationEnabled}`
        );
      });

      console.log(
        `[useNotifications] 모든 설정 로드 완료:`,
        Object.fromEntries(notificationSettings.value)
      );
    } catch (error) {
      console.error('[useNotifications] 알림 설정 로드 실패:', error);
    } finally {
      isLoading.value = false;
    }
  };

  // 특정 채팅방 알림 설정 로드
  const loadChatNotificationSetting = async chatId => {
    try {
      console.log(`[useNotifications] 특정 채팅방 알림 설정 로드: chatId=${chatId}`);

      const setting = await getChatNotificationSetting(chatId);
      console.log(`[useNotifications] 백엔드 응답: chatId=${chatId}, enabled=${setting}`);

      notificationSettings.value.set(chatId, setting);
      return setting;
    } catch (error) {
      console.error(`[useNotifications] 채팅방 알림 설정 로드 실패: chatId=${chatId}`, error);
      // 실패 시 기본값(true) 반환
      notificationSettings.value.set(chatId, true);
      return true;
    }
  };

  const showChatNotification = message => {
    const { chatroomId, senderName, content } = message;

    console.log('[useNotifications] showChatNotification 호출됨:', {
      chatroomId,
      senderName,
      content: content?.substring(0, 30),
      messageObject: message,
    });

    const enabled = isNotificationEnabled(chatroomId);
    console.log(
      `[useNotifications] 알림 표시 시도: chatId=${chatroomId}, enabled=${enabled}, sender=${senderName}`
    );

    if (!enabled) {
      console.log(`[useNotifications] 알림 비활성화로 인해 표시하지 않음: chatId=${chatroomId}`);
      return;
    }

    console.log('[useNotifications] 알림 활성화됨, 브라우저 알림 및 토스트 표시 시작');

    // 브라우저 알림
    if ('Notification' in window && Notification.permission === 'granted') {
      console.log('[useNotifications] 브라우저 알림 표시');
      new Notification(`${senderName}님이 메시지를 보냈습니다`, {
        body: content,
        icon: '/favicon.ico',
        tag: `chat-${chatroomId}`,
      });
    } else {
      console.log('[useNotifications] 브라우저 알림 권한 없음 또는 지원 안함:', {
        hasNotification: 'Notification' in window,
        permission: 'Notification' in window ? Notification.permission : 'N/A',
      });
    }

    // 토스트 알림
    console.log('[useNotifications] 토스트 알림 표시');
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

    console.log(`[useNotifications] 알림 표시 완료: chatId=${chatroomId}`);
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

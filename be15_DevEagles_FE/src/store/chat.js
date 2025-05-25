import { defineStore } from 'pinia';
import { useAuthStore } from './auth';
import { getChatRooms, markAsRead, createChatRoom } from '@/features/chat/api/chatService';
import { getUserAiChatRooms } from '@/features/chat/api/aiChatService';
import {
  initializeWebSocket,
  subscribeToChatRoom,
  subscribeToReadStatus,
  sendWebSocketMessage,
  disconnectWebSocket,
  isWebSocketConnected,
} from '@/features/chat/api/webSocketService';
import { useNotifications } from '@/features/chat/composables/useNotifications';
import { transformChatRoom } from '@/features/chat/utils/chatUtils';
import { formatLastMessageTime } from '@/features/chat/utils/timeUtils';

export const useChatStore = defineStore('chat', {
  state: () => ({
    chats: [],
    isLoading: false,
    error: null,
    isWebSocketConnected: false,
    currentChatId: null,
    chatWindowHandler: null, // ChatWindow 전용 메시지 핸들러
    currentChatWindowId: null, // ChatWindow가 현재 보고 있는 채팅방 ID
  }),

  getters: {
    unreadCount: state => {
      return state.chats.reduce((total, chat) => total + (chat.unreadCount || 0), 0);
    },

    getCurrentChat: state => {
      return state.chats.find(chat => chat.id === state.currentChatId);
    },

    getChatById: state => {
      return chatId => state.chats.find(chat => chat.id === chatId);
    },
  },

  actions: {
    // 웹소켓 초기화
    async initializeWebSocketConnection() {
      if (!isWebSocketConnected()) {
        initializeWebSocket();

        // 연결 상태 모니터링
        const checkConnection = () => {
          this.isWebSocketConnected = isWebSocketConnected();
          if (!this.isWebSocketConnected) {
            setTimeout(checkConnection, 1000);
          } else {
            // 웹소켓 연결 완료 후 전역 메시지 수신 설정
            this.setupGlobalMessageHandling();
          }
        };

        setTimeout(checkConnection, 1000);
      } else {
        this.isWebSocketConnected = true;
        this.setupGlobalMessageHandling();
      }
    },

    // 전역 메시지 수신 처리 설정
    setupGlobalMessageHandling() {
      // 모든 채팅방에 대한 전역 구독 설정
      this.chats.forEach(chat => {
        if (chat.id) {
          subscribeToChatRoom(chat.id, message => {
            console.log('[setupGlobalMessageHandling] 전역 메시지 수신:', {
              chatId: chat.id,
              messageId: message.id,
              content: message.content?.substring(0, 20),
            });
            this.handleIncomingMessage(message);
          });

          subscribeToReadStatus(chat.id, readStatus => {
            console.log('[setupGlobalMessageHandling] 전역 읽음 상태 수신:', {
              chatId: chat.id,
              readStatus,
            });
            this.handleReadStatusUpdate(readStatus);
          });
        }
      });
    },

    // 채팅방 목록 로드
    async loadChatRooms() {
      try {
        this.isLoading = true;
        this.error = null;

        const { useTeamStore } = await import('./team');
        const teamStore = useTeamStore();
        const authStore = useAuthStore();

        // 현재 팀 ID가 없으면 AI 채팅방만 로드
        if (!teamStore.currentTeamId) {
          console.warn('[loadChatRooms] 현재 팀이 선택되지 않았습니다. AI 채팅방만 로드합니다.');

          // AI 채팅방만 로드
          try {
            const aiChatRooms = await getUserAiChatRooms(authStore.userId);
            const teamMembers = [];

            this.chats = aiChatRooms.map(room =>
              transformChatRoom(room, authStore.userId, teamMembers)
            );

            console.log('[loadChatRooms] AI 채팅방만 로드 완료:', {
              aiChatCount: this.chats.length,
            });
          } catch (error) {
            console.error('[loadChatRooms] AI 채팅방 로드 실패:', error);
            this.chats = [];
          }
          return;
        }

        // 팀 채팅방과 AI 채팅방을 병렬로 로드
        const [teamChatRooms, aiChatRooms] = await Promise.all([
          getChatRooms(teamStore.currentTeamId),
          getUserAiChatRooms(authStore.userId),
        ]);

        if (teamChatRooms && Array.isArray(teamChatRooms)) {
          // 팀원 정보 가져오기
          const teamMembers = teamStore.teamMembers || [];

          // 팀 채팅방 변환
          const transformedTeamChats = teamChatRooms.map(room =>
            transformChatRoom(room, authStore.userId, teamMembers)
          );

          // AI 채팅방 변환
          const transformedAiChats = (aiChatRooms || []).map(room =>
            transformChatRoom(room, authStore.userId, teamMembers)
          );

          // AI 채팅방을 맨 앞에 배치하고 팀 채팅방을 뒤에 배치
          this.chats = [...transformedAiChats, ...transformedTeamChats];

          console.log('[loadChatRooms] 채팅방 로드 완료:', {
            teamId: teamStore.currentTeamId,
            teamChatCount: transformedTeamChats.length,
            aiChatCount: transformedAiChats.length,
            totalChatCount: this.chats.length,
            teamMemberCount: teamMembers.length,
          });

          // 알림 설정 초기화 (채팅방 데이터에서)
          const { initializeFromChatData, loadNotificationSettings } = useNotifications();
          initializeFromChatData(this.chats);

          // 백엔드에서 최신 알림 설정 로드하여 동기화
          try {
            await loadNotificationSettings();
            console.log('[loadChatRooms] 백엔드 알림 설정 동기화 완료');
          } catch (error) {
            console.warn('[loadChatRooms] 백엔드 알림 설정 로드 실패, 기본값 사용:', error);
          }

          // 웹소켓이 연결되어 있으면 전역 메시지 수신 설정
          if (this.isWebSocketConnected) {
            this.setupGlobalMessageHandling();
          }
        } else {
          // 팀 채팅방이 없는 경우 AI 채팅방만 표시
          const aiChatRooms = await getUserAiChatRooms(authStore.userId);
          const teamMembers = teamStore.teamMembers || [];

          this.chats = (aiChatRooms || []).map(room =>
            transformChatRoom(room, authStore.userId, teamMembers)
          );

          console.log('[loadChatRooms] AI 채팅방만 로드 완료 (팀 채팅방 없음):', {
            aiChatCount: this.chats.length,
          });
        }
      } catch (err) {
        console.error('채팅방 목록 로드 실패:', err);
        this.error = '채팅방 목록을 불러올 수 없습니다.';
        this.chats = [];
      } finally {
        this.isLoading = false;
      }
    },

    // 채팅방 선택 및 구독
    async selectChat(chatId) {
      this.currentChatId = chatId;
      await this.markChatAsRead(chatId);

      const subscribeWithRetry = (retryCount = 0) => {
        if (isWebSocketConnected()) {
          subscribeToChatRoom(chatId, message => {
            this.handleIncomingMessage(message);
          });
          console.log(`[selectChat] 채팅방 ${chatId} 구독 완료`);
        } else if (retryCount < 3) {
          console.log(`[selectChat] 웹소켓 연결 대기 중... (${retryCount + 1}/3)`);
          setTimeout(() => subscribeWithRetry(retryCount + 1), 1000);
        } else {
          console.error('[selectChat] 웹소켓 연결 실패, 구독 불가');
        }
      };

      subscribeWithRetry();
    },

    // 채팅방 읽음 처리
    async markChatAsRead(chatId) {
      const chatIndex = this.chats.findIndex(c => c.id === chatId);

      if (chatIndex > -1) {
        this.chats[chatIndex].unreadCount = 0;

        try {
          await markAsRead(chatId);
        } catch (error) {
          console.error('읽음 표시 실패:', error);
        }
      }
    },

    // 읽음 상태 변경 처리 (WebSocket 이벤트용)
    handleReadStatusUpdate(readStatus) {
      const { chatroomId, userId } = readStatus;
      const authStore = useAuthStore();

      // 다른 사용자가 읽음 처리한 경우, 내가 현재 채팅방에 있다면 읽지 않은 메시지 수 감소
      if (userId !== authStore.userId && this.currentChatId === chatroomId) {
        const chatIndex = this.chats.findIndex(c => c.id === chatroomId);
        if (chatIndex > -1 && this.chats[chatIndex].unreadCount > 0) {
          this.chats[chatIndex].unreadCount = Math.max(0, this.chats[chatIndex].unreadCount - 1);
          console.log('[handleReadStatusUpdate] 읽지 않은 메시지 수 감소:', {
            chatroomId,
            newUnreadCount: this.chats[chatIndex].unreadCount,
          });
        }
      }
    },

    // 메시지 전송
    async sendMessage(chatId, content) {
      const authStore = useAuthStore();

      if (!isWebSocketConnected()) {
        console.error('웹소켓이 연결되지 않았습니다.');
        return false;
      }

      const success = sendWebSocketMessage(chatId, content, authStore.userId, authStore.name);

      if (success) {
        this.updateChatAfterSending(chatId, content);
      }

      return success;
    },

    // 메시지 전송 후 UI 업데이트
    updateChatAfterSending(chatId, content) {
      const chatIndex = this.chats.findIndex(c => c.id === chatId);

      if (chatIndex > -1) {
        const chat = this.chats[chatIndex];
        chat.lastMessage = content;
        chat.lastMessageTime = '방금';
        chat.lastMessageTimestamp = new Date().toISOString();

        // 해당 채팅을 목록 맨 위로 이동
        if (chatIndex > 0) {
          this.chats.splice(chatIndex, 1);
          this.chats.unshift(chat);
        }
      }
    },

    // 웹소켓 메시지 수신 처리
    handleIncomingMessage(message) {
      const authStore = useAuthStore();
      const { showChatNotification } = useNotifications();
      let chatIndex = this.chats.findIndex(c => c.id === message.chatroomId);

      // ChatWindow 핸들러가 등록되어 있고, 현재 ChatWindow가 보고 있는 채팅방의 메시지인 경우 핸들러 호출
      if (this.chatWindowHandler && this.currentChatWindowId === message.chatroomId) {
        console.log('[handleIncomingMessage] ChatWindow 핸들러 호출:', {
          messageId: message.id,
          chatroomId: message.chatroomId,
          hasHandler: !!this.chatWindowHandler,
        });

        try {
          this.chatWindowHandler(message);
        } catch (error) {
          console.error('[handleIncomingMessage] ChatWindow 핸들러 호출 중 오류:', error);
        }
      }

      // 채팅방이 목록에 없으면 새로고침하여 추가
      if (chatIndex === -1) {
        console.log('[handleIncomingMessage] 새 채팅방 감지, 목록 새로고침');
        this.loadChatRooms().then(() => {
          chatIndex = this.chats.findIndex(c => c.id === message.chatroomId);
          if (chatIndex !== -1) {
            this.processIncomingMessage(message, chatIndex, authStore, showChatNotification);
          }
        });
        return;
      }

      this.processIncomingMessage(message, chatIndex, authStore, showChatNotification);
    },

    // 메시지 처리 로직 분리
    processIncomingMessage(message, chatIndex, authStore, showChatNotification) {
      const chat = this.chats[chatIndex];

      console.log('[processIncomingMessage] 메시지 처리 시작:', {
        messageId: message.id,
        senderId: message.senderId,
        currentUserId: authStore.userId,
        currentChatId: this.currentChatId,
        messageChatId: message.chatroomId,
        isMyMessage: message.senderId === authStore.userId,
        isCurrentChat: this.currentChatId === message.chatroomId,
      });

      // 메시지가 내가 보낸 것이 아니면 처리
      if (message.senderId !== authStore.userId) {
        console.log('[processIncomingMessage] 다른 사용자의 메시지 처리');

        // 현재 채팅방이 아닌 경우 읽지 않은 메시지 증가
        if (this.currentChatId !== message.chatroomId) {
          chat.unreadCount = (chat.unreadCount || 0) + 1;
          console.log('[processIncomingMessage] 읽지 않은 메시지 증가:', chat.unreadCount);
        }

        // 알림 표시 조건 확인
        if (this.currentChatId !== message.chatroomId) {
          console.log('[processIncomingMessage] 알림 표시 조건 만족, 알림 준비 중');

          let senderName = message.senderName;

          if (!senderName || senderName.trim() === '') {
            if (chat.type === 'DIRECT') {
              // 1:1 채팅에서는 채팅방 이름(상대방 이름) 사용
              senderName = chat.name;
            } else if (chat.participants) {
              // 그룹 채팅에서는 참가자 목록에서 발신자 찾기
              const sender = chat.participants.find(
                p => String(p.userId) === String(message.senderId)
              );
              // 다양한 필드명 시도
              senderName =
                sender?.userName || sender?.name || sender?.nickname || '알 수 없는 사용자';

              console.log('[processIncomingMessage] 참가자에서 발신자 찾기:', {
                senderId: message.senderId,
                foundSender: sender,
                senderName: senderName,
                allParticipants: chat.participants.map(p => ({
                  userId: p.userId,
                  userName: p.userName,
                  name: p.name,
                  nickname: p.nickname,
                })),
              });
            } else {
              senderName = '알 수 없는 사용자';
            }
          }

          console.log('[processIncomingMessage] 알림 호출 직전:', {
            chatroomId: message.chatroomId,
            senderName: senderName,
            content: message.content?.substring(0, 30),
            originalSenderName: message.senderName,
            chatType: chat.type,
            chatName: chat.name,
          });

          showChatNotification({
            chatroomId: message.chatroomId,
            senderName: senderName,
            content: message.content,
          });

          console.log('[processIncomingMessage] showChatNotification 호출 완료');
        } else {
          console.log('[processIncomingMessage] 현재 채팅방이므로 알림 표시하지 않음');
        }
      } else {
        console.log('[processIncomingMessage] 내가 보낸 메시지이므로 알림 처리 안함');
      }

      // 마지막 메시지 업데이트
      chat.lastMessage = message.content;
      chat.lastMessageTime = formatLastMessageTime(message.timestamp);
      chat.lastMessageTimestamp = message.timestamp;

      // 해당 채팅을 목록 맨 위로 이동
      if (chatIndex > 0) {
        this.chats.splice(chatIndex, 1);
        this.chats.unshift(chat);
      }

      console.log('[processIncomingMessage] 메시지 처리 완료');
    },

    // 1:1 채팅 시작 또는 찾기
    async startDirectChat(userId, userName, userThumbnail) {
      const authStore = useAuthStore();
      const { useTeamStore } = await import('./team');
      const teamStore = useTeamStore();

      // 현재 팀이 설정되어 있는지 확인
      if (!teamStore.currentTeamId) {
        throw new Error('팀이 선택되지 않았습니다. 팀을 선택해주세요.');
      }

      // 필수 데이터 검증
      if (!authStore.userId || !userId) {
        throw new Error('사용자 정보가 없습니다.');
      }

      if (!authStore.name || !userName) {
        throw new Error('사용자 이름 정보가 없습니다.');
      }

      // 현재 팀의 채팅방 목록에서 기존 1:1 채팅 확인
      let existingChat = this.chats.find(chat => {
        if (chat.type !== 'DIRECT') return false;

        // 참가자가 정확히 2명이고, 현재 사용자와 대상 사용자가 포함되어 있는지 확인
        const participants = chat.participants || [];
        const activeParticipants = participants.filter(p => !p.deletedAt);

        if (activeParticipants.length !== 2) return false;

        const participantIds = activeParticipants.map(p => String(p.userId));
        return (
          participantIds.includes(String(authStore.userId)) &&
          participantIds.includes(String(userId))
        );
      });

      if (existingChat) {
        console.log('[startDirectChat] 기존 1:1 채팅방 발견:', existingChat.id);
        return existingChat;
      }

      // 새로운 1:1 채팅방 생성
      const requestData = {
        teamId: String(teamStore.currentTeamId),
        name: `1:1 채팅`, // 일반적인 이름으로 설정
        type: 'DIRECT',
        participantIds: [String(authStore.userId), String(userId)], // 현재 사용자와 대상 사용자 모두 포함
      };

      console.log('[startDirectChat] 요청 데이터:', requestData);
      console.log('[startDirectChat] 데이터 타입 확인:', {
        teamId: typeof requestData.teamId,
        name: typeof requestData.name,
        type: typeof requestData.type,
        participantIds: Array.isArray(requestData.participantIds),
        participantIdsTypes: requestData.participantIds.map(id => typeof id),
      });

      const response = await createChatRoom(requestData);

      if (response.success) {
        const newRoom = response.data;
        const teamMembers = teamStore.teamMembers || [];
        const transformedRoom = transformChatRoom(newRoom, authStore.userId, teamMembers);
        this.chats.unshift(transformedRoom);

        // 새 채팅방에 대한 전역 구독 추가
        if (this.isWebSocketConnected && newRoom.id) {
          subscribeToChatRoom(newRoom.id, message => {
            console.log('[startDirectChat] 새 채팅방 메시지 수신:', {
              chatId: newRoom.id,
              messageId: message.id,
              content: message.content?.substring(0, 20),
            });
            this.handleIncomingMessage(message);
          });
        }

        // 채팅방 목록 새로고침하여 최신 상태 동기화
        await this.loadChatRooms();

        // 새로 생성된 채팅방 찾기 (ID로 찾거나 최신 DIRECT 타입 채팅방)
        const createdChat = this.chats.find(
          chat =>
            chat.id === newRoom.id ||
            (chat.type === 'DIRECT' &&
              chat.participants?.some(p => String(p.userId) === String(userId)))
        );

        console.log('[startDirectChat] 채팅방 생성 완료:', {
          newRoomId: newRoom.id,
          foundChat: createdChat ? createdChat.id : 'not found',
          teamId: teamStore.currentTeamId,
        });

        return createdChat || transformedRoom;
      } else {
        throw new Error(response.message || '채팅방 생성 실패');
      }
    },

    // 초기화
    async initialize() {
      await this.initializeWebSocketConnection();
      await this.loadChatRooms();

      // 사용자 상태 구독 초기화
      try {
        const { useUserStatusStore } = await import('@/store/userStatus');
        const userStatusStore = useUserStatusStore();
        await userStatusStore.initializeUserStatusSubscription();
      } catch (error) {
        console.error('사용자 상태 구독 초기화 실패:', error);
      }
    },

    // 정리
    cleanup() {
      disconnectWebSocket();
      this.isWebSocketConnected = false;
      this.currentChatId = null;
      this.chatWindowHandler = null;
      this.currentChatWindowId = null;
    },

    // ChatWindow 메시지 핸들러 등록
    registerChatWindowHandler(chatId, handler) {
      console.log('[chatStore] ChatWindow 핸들러 등록:', {
        chatId,
        hasHandler: !!handler,
        handlerType: typeof handler,
      });

      this.currentChatWindowId = chatId;
      this.chatWindowHandler = handler;

      console.log('[chatStore] ChatWindow 핸들러 등록 완료:', {
        currentChatWindowId: this.currentChatWindowId,
        isRegistered: !!this.chatWindowHandler,
      });
    },

    // ChatWindow 메시지 핸들러 해제
    unregisterChatWindowHandler() {
      console.log('[chatStore] ChatWindow 핸들러 해제 요청');

      this.currentChatWindowId = null;
      this.chatWindowHandler = null;

      console.log('[chatStore] ChatWindow 핸들러 해제 완료');
    },

    // 채팅방 목록 조회
    async loadChats(teamId = null) {
      this.isLoading = true;
      this.error = null;

      try {
        const chatRooms = await getChatRooms(teamId);
        console.log('[chatStore] 채팅방 목록 조회 완료:', chatRooms);

        this.chats = chatRooms.map(room => {
          const transformedChat = transformChatRoom(room);

          // unreadCount가 있으면 사용, 없으면 0으로 설정
          transformedChat.unreadCount = room.unreadCount || 0;

          console.log('[chatStore] 채팅방 변환:', {
            id: transformedChat.id,
            name: transformedChat.name,
            unreadCount: transformedChat.unreadCount,
            lastMessage: transformedChat.lastMessage?.substring(0, 20),
          });

          return transformedChat;
        });

        console.log('[chatStore] 전체 읽지 않은 메시지 수:', this.unreadCount);
      } catch (error) {
        console.error('채팅방 목록 조회 실패:', error);
        this.error = '채팅방 목록을 불러오는데 실패했습니다.';
        this.chats = [];
      } finally {
        this.isLoading = false;
      }
    },
  },
});

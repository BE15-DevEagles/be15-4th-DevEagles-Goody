import { defineStore } from 'pinia';
import { useAuthStore } from './auth';
import { getChatRooms, markAsRead, createChatRoom } from '@/features/chat/api/chatService';
import { getUserAiChatRooms } from '@/features/chat/api/aiChatService';
import {
  initializeWebSocket,
  subscribeToChatRoom,
  sendWebSocketMessage,
  disconnectWebSocket,
  isWebSocketConnected,
} from '@/features/chat/api/webSocketService';
import { useNotifications } from '@/features/chat/composables/useNotifications';
import { transformChatRoom } from '@/features/chat/utils/chatUtils';
import { formatLastMessageTime } from '@/features/chat/utils/timeUtils';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('chatStore');

export const useChatStore = defineStore('chat', {
  state: () => ({
    chats: [],
    isLoading: false,
    error: null,
    isWebSocketConnected: false,
    currentChatId: null,
    chatWindowHandler: null,
    currentChatWindowId: null,
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
    async initializeWebSocketConnection() {
      if (!isWebSocketConnected()) {
        initializeWebSocket();
        const checkConnection = () => {
          this.isWebSocketConnected = isWebSocketConnected();
          if (!this.isWebSocketConnected) {
            setTimeout(checkConnection, 1000);
          } else {
            this.setupGlobalMessageHandling();
          }
        };
        setTimeout(checkConnection, 1000);
      } else {
        this.isWebSocketConnected = true;
        this.setupGlobalMessageHandling();
      }
    },

    setupGlobalMessageHandling() {
      this.chats.forEach(chat => {
        if (chat.id) {
          subscribeToChatRoom(chat.id, message => {
            logger.debug('[setupGlobalMessageHandling] 전역 메시지 수신:', {
              chatId: chat.id,
              messageId: message.id,
              content: message.content?.substring(0, 20),
            });
            this.handleIncomingMessage(message);
          });
        }
      });
    },

    async loadChatRooms() {
      try {
        this.isLoading = true;
        this.error = null;

        const { useTeamStore } = await import('./team');
        const teamStore = useTeamStore();
        const authStore = useAuthStore();

        if (!teamStore.currentTeamId) {
          logger.warn('[loadChatRooms] 현재 팀이 선택되지 않았습니다. AI 채팅방만 로드합니다.');
          try {
            const aiChatRooms = await getUserAiChatRooms(authStore.userId);
            const teamMembers = [];
            this.chats = aiChatRooms.map(room =>
              transformChatRoom(room, authStore.userId, teamMembers, null)
            );
            logger.info('[loadChatRooms] AI 채팅방만 로드 완료:', {
              aiChatCount: this.chats.length,
            });
          } catch (error) {
            logger.error('[loadChatRooms] AI 채팅방 로드 실패:', error);
            this.chats = [];
          }
          return;
        }

        const [teamChatRooms, aiChatRooms] = await Promise.all([
          getChatRooms(teamStore.currentTeamId),
          getUserAiChatRooms(authStore.userId),
        ]);

        if (teamChatRooms && Array.isArray(teamChatRooms)) {
          const teamMembers = teamStore.teamMembers || [];
          const currentTeamId = teamStore.currentTeamId;

          // 현재 팀에 속한 채팅방만 필터링
          const filteredTeamChats = teamChatRooms.filter(room => {
            // AI 채팅방은 항상 포함
            if (room.type === 'AI') {
              return true;
            }

            // 팀 채팅방과 그룹 채팅방은 teamId가 현재 팀과 일치하는 경우만
            if (room.type === 'TEAM' || room.type === 'GROUP') {
              return room.teamId === currentTeamId;
            }

            // 1:1 채팅방의 경우 상대방이 현재 팀 멤버인지 확인
            if (room.type === 'DIRECT') {
              const otherParticipants =
                room.participants?.filter(p => p.userId !== authStore.userId) || [];
              if (otherParticipants.length === 0) return false;

              const otherParticipant = otherParticipants[0];
              const isTeamMember = teamMembers.some(
                member => String(member.userId) === String(otherParticipant.userId)
              );

              if (!isTeamMember) {
                logger.info('[loadChatRooms] 다른 팀 사용자와의 1:1 채팅방 제외:', {
                  roomId: room.id,
                  otherUserId: otherParticipant.userId,
                  currentTeamId: currentTeamId,
                  teamMemberIds: teamMembers.map(m => m.userId),
                });
              }

              return isTeamMember;
            }

            return true;
          });

          const transformedTeamChats = filteredTeamChats.map(room =>
            transformChatRoom(room, authStore.userId, teamMembers, teamStore.currentTeam)
          );
          const transformedAiChats = (aiChatRooms || []).map(room =>
            transformChatRoom(room, authStore.userId, teamMembers, teamStore.currentTeam)
          );
          this.chats = [...transformedAiChats, ...transformedTeamChats];
          logger.info('[loadChatRooms] 채팅방 로드 완료:', {
            teamId: teamStore.currentTeamId,
            originalTeamChatCount: teamChatRooms.length,
            filteredTeamChatCount: transformedTeamChats.length,
            aiChatCount: transformedAiChats.length,
            totalChatCount: this.chats.length,
            teamMemberCount: teamMembers.length,
          });

          const { initializeFromChatData, loadNotificationSettings } = useNotifications();
          initializeFromChatData(this.chats);
          try {
            await loadNotificationSettings();
            logger.info('[loadChatRooms] 백엔드 알림 설정 동기화 완료');
          } catch (error) {
            logger.warn('[loadChatRooms] 백엔드 알림 설정 로드 실패, 기본값 사용:', error);
          }

          if (this.isWebSocketConnected) {
            this.setupGlobalMessageHandling();
          }
        } else {
          const aiChatRoomsData = await getUserAiChatRooms(authStore.userId);
          const teamMembers = teamStore.teamMembers || [];
          this.chats = (aiChatRoomsData || []).map(room =>
            transformChatRoom(room, authStore.userId, teamMembers, teamStore.currentTeam)
          );
          logger.info('[loadChatRooms] AI 채팅방만 로드 완료 (팀 채팅방 없음):', {
            aiChatCount: this.chats.length,
          });
        }
      } catch (err) {
        logger.error('채팅방 목록 로드 실패:', err);
        this.error = '채팅방 목록을 불러올 수 없습니다.';
        this.chats = [];
      } finally {
        this.isLoading = false;
      }
    },

    async selectChat(chatId) {
      this.currentChatId = chatId;
      await this.markChatAsRead(chatId);
      const subscribeWithRetry = (retryCount = 0) => {
        if (isWebSocketConnected()) {
          subscribeToChatRoom(chatId, message => {
            this.handleIncomingMessage(message);
          });
          logger.info(`[selectChat] 채팅방 ${chatId} 구독 완료`);
        } else if (retryCount < 3) {
          logger.info(`[selectChat] 웹소켓 연결 대기 중... (${retryCount + 1}/3)`);
          setTimeout(() => subscribeWithRetry(retryCount + 1), 1000);
        } else {
          logger.error('[selectChat] 웹소켓 연결 실패, 구독 불가');
        }
      };
      subscribeWithRetry();
    },

    async markChatAsRead(chatId) {
      const chatIndex = this.chats.findIndex(c => c.id === chatId);
      if (chatIndex > -1) {
        this.chats[chatIndex].unreadCount = 0;
        try {
          await markAsRead(chatId);
        } catch (error) {
          logger.error('읽음 표시 실패:', error);
        }
      }
    },

    async sendMessage(chatId, content) {
      const authStore = useAuthStore();
      if (!isWebSocketConnected()) {
        logger.error('웹소켓이 연결되지 않았습니다.');
        return false;
      }
      const success = sendWebSocketMessage(chatId, content, authStore.userId, authStore.name);
      if (success) {
        this.updateChatAfterSending(chatId, content);
      }
      return success;
    },

    updateChatAfterSending(chatId, content) {
      const chatIndex = this.chats.findIndex(c => c.id === chatId);
      if (chatIndex > -1) {
        const chat = this.chats[chatIndex];
        chat.lastMessage = content;
        chat.lastMessageTime = '방금';
        chat.lastMessageTimestamp = new Date().toISOString();
        if (chatIndex > 0) {
          this.chats.splice(chatIndex, 1);
          this.chats.unshift(chat);
        }
      }
    },

    handleIncomingMessage(message) {
      const authStore = useAuthStore();
      const { showChatNotification } = useNotifications();
      let chatIndex = this.chats.findIndex(c => c.id === message.chatroomId);

      if (this.chatWindowHandler && this.currentChatWindowId === message.chatroomId) {
        logger.debug('[handleIncomingMessage] ChatWindow 핸들러 호출:', {
          messageId: message.id,
          chatroomId: message.chatroomId,
          hasHandler: !!this.chatWindowHandler,
        });
        try {
          this.chatWindowHandler(message);
        } catch (error) {
          logger.error('[handleIncomingMessage] ChatWindow 핸들러 호출 중 오류:', error);
        }
      }

      if (chatIndex === -1) {
        logger.info('[handleIncomingMessage] 새 채팅방 감지, 목록 새로고침');
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

    processIncomingMessage(message, chatIndex, authStore, showChatNotification) {
      const chat = this.chats[chatIndex];
      logger.debug('[processIncomingMessage] 메시지 처리 시작:', {
        messageId: message.id,
        senderId: message.senderId,
        currentUserId: authStore.userId,
        currentChatId: this.currentChatId,
        messageChatId: message.chatroomId,
        isMyMessage: message.senderId === authStore.userId,
        isCurrentChat: this.currentChatId === message.chatroomId,
      });

      if (message.senderId !== authStore.userId) {
        logger.info('[processIncomingMessage] 다른 사용자의 메시지 처리');
        if (this.currentChatId !== message.chatroomId) {
          chat.unreadCount = (chat.unreadCount || 0) + 1;
          logger.info('[processIncomingMessage] 읽지 않은 메시지 증가:', chat.unreadCount);
        }

        if (this.currentChatId !== message.chatroomId) {
          logger.info('[processIncomingMessage] 알림 표시 조건 만족, 알림 준비 중');
          let senderName = message.senderName;
          if (!senderName || senderName.trim() === '') {
            if (chat.type === 'DIRECT') {
              senderName = chat.name;
            } else if (chat.participants) {
              const sender = chat.participants.find(
                p => String(p.userId) === String(message.senderId)
              );
              senderName =
                sender?.userName || sender?.name || sender?.nickname || '알 수 없는 사용자';
              logger.debug('[processIncomingMessage] 참가자에서 발신자 찾기:', {
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
          logger.debug('[processIncomingMessage] 알림 호출 직전:', {
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
          logger.info('[processIncomingMessage] showChatNotification 호출 완료');
        } else {
          logger.info('[processIncomingMessage] 현재 채팅방이므로 알림 표시하지 않음');
        }
      } else {
        logger.info('[processIncomingMessage] 내가 보낸 메시지이므로 알림 처리 안함');
      }

      chat.lastMessage = message.content;
      chat.lastMessageTime = formatLastMessageTime(message.timestamp);
      chat.lastMessageTimestamp = message.timestamp;

      if (chatIndex > 0) {
        this.chats.splice(chatIndex, 1);
        this.chats.unshift(chat);
      }
      logger.info('[processIncomingMessage] 메시지 처리 완료');
    },

    async startDirectChat(userId, userName, userThumbnail) {
      const authStore = useAuthStore();
      const { useTeamStore } = await import('./team');
      const teamStore = useTeamStore();

      if (!teamStore.currentTeamId) {
        throw new Error('팀이 선택되지 않았습니다. 팀을 선택해주세요.');
      }
      if (!authStore.userId || !userId) {
        throw new Error('사용자 정보가 없습니다.');
      }
      if (!authStore.name || !userName) {
        throw new Error('사용자 이름 정보가 없습니다.');
      }

      let existingChat = this.chats.find(chat => {
        if (chat.type !== 'DIRECT') return false;
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
        logger.info('[startDirectChat] 기존 1:1 채팅방 발견:', existingChat.id);
        return existingChat;
      }

      const requestData = {
        teamId: String(teamStore.currentTeamId),
        name: `1:1 채팅`,
        type: 'DIRECT',
        participantIds: [String(authStore.userId), String(userId)],
      };

      logger.info('[startDirectChat] 요청 데이터:', requestData);
      logger.debug('[startDirectChat] 데이터 타입 확인:', {
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
        const transformedRoom = transformChatRoom(
          newRoom,
          authStore.userId,
          teamMembers,
          teamStore.currentTeam
        );
        this.chats.unshift(transformedRoom);

        if (this.isWebSocketConnected && newRoom.id) {
          subscribeToChatRoom(newRoom.id, message => {
            logger.debug('[startDirectChat] 새 채팅방 메시지 수신:', {
              chatId: newRoom.id,
              messageId: message.id,
              content: message.content?.substring(0, 20),
            });
            this.handleIncomingMessage(message);
          });
        }

        await this.loadChatRooms();
        const createdChat = this.chats.find(
          chat =>
            chat.id === newRoom.id ||
            (chat.type === 'DIRECT' &&
              chat.participants?.some(p => String(p.userId) === String(userId)))
        );

        logger.info('[startDirectChat] 채팅방 생성 완료:', {
          newRoomId: newRoom.id,
          foundChat: createdChat ? createdChat.id : 'not found',
          teamId: teamStore.currentTeamId,
        });
        return createdChat || transformedRoom;
      } else {
        throw new Error(response.message || '채팅방 생성 실패');
      }
    },

    async initialize() {
      await this.initializeWebSocketConnection();
      await this.loadChatRooms();
      try {
        const { useUserStatusStore } = await import('@/store/userStatus');
        const userStatusStore = useUserStatusStore();
        await userStatusStore.initializeUserStatusSubscription();
      } catch (error) {
        logger.error('사용자 상태 구독 초기화 실패:', error);
      }
    },

    cleanup() {
      disconnectWebSocket();
      this.isWebSocketConnected = false;
      this.currentChatId = null;
      this.chatWindowHandler = null;
      this.currentChatWindowId = null;
    },

    registerChatWindowHandler(chatId, handler) {
      logger.info('[chatStore] ChatWindow 핸들러 등록:', {
        chatId,
        hasHandler: !!handler,
        handlerType: typeof handler,
      });
      this.currentChatWindowId = chatId;
      this.chatWindowHandler = handler;
      logger.info('[chatStore] ChatWindow 핸들러 등록 완료:', {
        currentChatWindowId: this.currentChatWindowId,
        isRegistered: !!this.chatWindowHandler,
      });
    },

    unregisterChatWindowHandler() {
      logger.info('[chatStore] ChatWindow 핸들러 해제 요청');
      this.currentChatWindowId = null;
      this.chatWindowHandler = null;
      logger.info('[chatStore] ChatWindow 핸들러 해제 완료');
    },

    async loadChats(teamId = null) {
      this.isLoading = true;
      this.error = null;
      try {
        const chatRooms = await getChatRooms(teamId);
        logger.info('[chatStore] 채팅방 목록 조회 완료:', chatRooms);
        this.chats = chatRooms.map(room => {
          const transformedChat = transformChatRoom(room, null, [], null);
          transformedChat.unreadCount = room.unreadCount || 0;
          logger.debug('[chatStore] 채팅방 변환:', {
            id: transformedChat.id,
            name: transformedChat.name,
            unreadCount: transformedChat.unreadCount,
            lastMessage: transformedChat.lastMessage?.substring(0, 20),
          });
          return transformedChat;
        });
        logger.info('[chatStore] 전체 읽지 않은 메시지 수:', this.unreadCount);
      } catch (error) {
        logger.error('채팅방 목록 조회 실패:', error);
        this.error = '채팅방 목록을 불러오는데 실패했습니다.';
        this.chats = [];
      } finally {
        this.isLoading = false;
      }
    },
  },
});

import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { normalizeTimestamp } from '@/features/chat/utils/timeUtils.js';
import { createLogger } from '@/utils/logger.js';

const logger = createLogger('webSocketService');

let stompClient = null;
let subscriptions = {};
let reconnectAttempts = 0;
let connectionState = 'disconnected';
let heartbeatInterval = null;
let connectionStateCallbacks = [];
let isManualDisconnect = false;
const MAX_RECONNECT_ATTEMPTS = 10;
const BASE_RECONNECT_DELAY = 1000;

export function onConnectionStateChange(callback) {
  connectionStateCallbacks.push(callback);
  callback(connectionState);
}

function notifyConnectionStateChange(newState) {
  connectionState = newState;
  connectionStateCallbacks.forEach(callback => {
    try {
      callback(newState);
    } catch (error) {
      logger.error('연결 상태 콜백 오류:', error);
    }
  });
}

function startHeartbeat() {
  if (heartbeatInterval) clearInterval(heartbeatInterval);

  heartbeatInterval = setInterval(() => {
    if (stompClient && stompClient.connected) {
      logger.debug('WebSocket 하트비트 확인됨');
    } else {
      logger.warn('WebSocket 연결이 끊어짐 - 재연결 시도');
      const token = localStorage.getItem('accessToken');
      if (!isManualDisconnect && token) {
        connectWebSocket();
      } else {
        logger.info('수동 연결 해제 상태이거나 인증 토큰이 없어 재연결하지 않습니다.');
        stopHeartbeat();
      }
    }
  }, 30000);
}

function stopHeartbeat() {
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval);
    heartbeatInterval = null;
  }
}

export function initializeWebSocket() {
  if (stompClient && stompClient.connected) {
    logger.info('이미 웹소켓이 연결되어 있습니다.');
    return;
  }
  isManualDisconnect = false;
  connectWebSocket();
}

function connectWebSocket() {
  if (connectionState === 'connecting') {
    logger.info('이미 연결 시도 중입니다.');
    return;
  }

  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS || isManualDisconnect) {
    if (isManualDisconnect) {
      logger.info('수동 연결 해제 상태로 재연결하지 않습니다.');
    } else {
      logger.error('최대 재연결 시도 횟수를 초과했습니다.');
      notifyConnectionStateChange('failed');
    }
    return;
  }

  const token = localStorage.getItem('accessToken');
  if (!token) {
    logger.warn('인증 토큰이 없어 웹소켓 연결을 시도하지 않습니다.');
    return;
  }

  notifyConnectionStateChange('connecting');

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
  const wsUrl = `${apiBaseUrl}/ws`;

  const socket = new SockJS(wsUrl);
  stompClient = Stomp.over(socket);

  stompClient.heartbeat.outgoing = 20000;
  stompClient.heartbeat.incoming = 20000;

  stompClient.debug = str => {
    logger.debug(`STOMP: ${str}`);
  };

  logger.info(`웹소켓 연결 시도... (${reconnectAttempts + 1}/${MAX_RECONNECT_ATTEMPTS})`, {
    url: wsUrl,
  });

  const connectHeaders = {};

  if (token) {
    connectHeaders['Authorization'] = `Bearer ${token}`;
    logger.info('웹소켓 연결에 JWT 토큰 포함');
  } else {
    logger.warn('JWT 토큰이 없어 웹소켓 인증 없이 연결');
  }

  stompClient.connect(
    connectHeaders,
    () => {
      logger.info('웹소켓 연결 성공');
      reconnectAttempts = 0;
      notifyConnectionStateChange('connected');
      startHeartbeat();

      const currentSubscriptions = { ...subscriptions };
      subscriptions = {};
      Object.keys(currentSubscriptions).forEach(destination => {
        const callback = currentSubscriptions[destination].callback;
        if (destination === 'user.status') {
          subscribeToUserStatus(callback);
        } else if (destination.includes('.read')) {
          const chatRoomId = destination.split('.')[1];
          subscribeToReadStatus(chatRoomId, callback);
        } else {
          const chatRoomId = destination.split('.')[1];
          subscribeToChatRoom(chatRoomId, callback);
        }
      });
    },
    error => {
      logger.error('웹소켓 연결 실패:', error?.message || '알 수 없는 오류');
      notifyConnectionStateChange('disconnected');
      stopHeartbeat();
      reconnectAttempts++;

      const currentToken = localStorage.getItem('accessToken');
      if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && !isManualDisconnect && currentToken) {
        const delay = Math.min(BASE_RECONNECT_DELAY * Math.pow(2, reconnectAttempts - 1), 30000);
        logger.info(
          `${delay}ms 후 재연결 시도... (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`
        );

        setTimeout(() => {
          connectWebSocket();
        }, delay);
      } else {
        if (isManualDisconnect) {
          logger.info('수동 연결 해제 상태로 재연결하지 않습니다.');
        } else if (!currentToken) {
          logger.warn('인증 토큰이 없어 재연결하지 않습니다.');
        } else {
          logger.error('최대 재연결 시도 횟수를 초과했습니다.');
          notifyConnectionStateChange('failed');
        }
      }
    }
  );

  socket.addEventListener('close', () => {
    logger.info('웹소켓 연결이 종료되었습니다.');
    notifyConnectionStateChange('disconnected');
    stopHeartbeat();

    const currentToken = localStorage.getItem('accessToken');
    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && !isManualDisconnect && currentToken) {
      setTimeout(() => {
        connectWebSocket();
      }, BASE_RECONNECT_DELAY);
    } else {
      if (isManualDisconnect) {
        logger.info('수동 연결 해제 상태로 재연결하지 않습니다.');
      } else if (!currentToken) {
        logger.warn('인증 토큰이 없어 재연결하지 않습니다.');
      }
    }
  });
}

export function subscribeToChatRoom(chatRoomId, callback) {
  if (!stompClient || !stompClient.connected) {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      logger.warn('[subscribeToChatRoom] 인증 토큰이 없어 구독하지 않습니다.');
      return;
    }

    logger.info('웹소켓 연결이 없습니다. 연결을 시도합니다.');
    initializeWebSocket();
    subscriptions[`chatroom.${chatRoomId}`] = { callback };
    setTimeout(() => subscribeToChatRoom(chatRoomId, callback), 1000);
    return;
  }

  const destination = `/topic/chatroom.${chatRoomId}`;

  if (subscriptions[destination]) {
    unsubscribe(destination);
  }

  subscriptions[destination] = {
    subscription: stompClient.subscribe(destination, message => {
      try {
        const receivedMessage = JSON.parse(message.body);

        logger.debug('[webSocketService] 받은 메시지 전체 구조:', {
          ...receivedMessage,
          hasTimestamp: !!receivedMessage.timestamp,
          hasCreatedAt: !!receivedMessage.createdAt,
          timestampValue: receivedMessage.timestamp,
          createdAtValue: receivedMessage.createdAt,
        });

        if (!receivedMessage.timestamp && !receivedMessage.createdAt) {
          receivedMessage.timestamp = normalizeTimestamp(null);
          logger.warn('[webSocketService] 웹소켓 메시지에 타임스탬프 없어 현재 시간 설정:', {
            messageId: receivedMessage.id,
            newTimestamp: receivedMessage.timestamp,
          });
        } else if (!receivedMessage.timestamp && receivedMessage.createdAt) {
          receivedMessage.timestamp = receivedMessage.createdAt;
          logger.info('[webSocketService] createdAt을 timestamp로 복사:', {
            messageId: receivedMessage.id,
            timestamp: receivedMessage.timestamp,
          });
        }

        callback(receivedMessage);
      } catch (error) {
        logger.error('메시지 처리 중 오류 발생:', error);
      }
    }),
    callback,
  };

  logger.info(`채팅방 ${chatRoomId} 구독 완료`);
}

export function subscribeToReadStatus(chatRoomId, callback) {
  if (!stompClient || !stompClient.connected) {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      logger.warn('[subscribeToReadStatus] 인증 토큰이 없어 구독하지 않습니다.');
      return;
    }

    logger.info('웹소켓 연결이 없습니다. 연결을 시도합니다.');
    initializeWebSocket();
    subscriptions[`chatroom.${chatRoomId}.read`] = { callback };
    setTimeout(() => subscribeToReadStatus(chatRoomId, callback), 1000);
    return;
  }

  const destination = `/topic/chatroom.${chatRoomId}.read`;

  if (subscriptions[destination]) {
    unsubscribe(destination);
  }

  subscriptions[destination] = {
    subscription: stompClient.subscribe(destination, message => {
      try {
        const readStatus = JSON.parse(message.body);
        callback(readStatus);
      } catch (error) {
        logger.error('읽음 상태 처리 중 오류 발생:', error);
      }
    }),
    callback,
  };

  logger.info(`채팅방 ${chatRoomId} 읽음 상태 구독 완료`);
}

export function sendWebSocketMessage(chatRoomId, message, userId, userName) {
  if (!stompClient || !stompClient.connected) {
    logger.error('웹소켓 연결이 없습니다. 메시지를 보낼 수 없습니다.');
    return false;
  }

  try {
    const messageData = {
      chatroomId: chatRoomId,
      content: message,
      senderId: String(userId),
      senderName: userName || '알 수 없는 사용자',
      messageType: 'TEXT',
    };

    logger.info('[webSocketService] 메시지 전송:', {
      content: messageData.content?.substring(0, 30),
      senderId: messageData.senderId,
      senderName: messageData.senderName,
      chatroomId: messageData.chatroomId,
    });

    stompClient.send(`/app/chat.send`, {}, JSON.stringify(messageData));
    return true;
  } catch (error) {
    logger.error('메시지 전송 중 오류 발생:', error);
    return false;
  }
}

export function sendReadMessage(chatRoomId, messageId) {
  if (!stompClient || !stompClient.connected) {
    logger.error('웹소켓 연결이 없습니다. 읽음 상태를 보낼 수 없습니다.');
    return false;
  }

  try {
    stompClient.send(
      `/app/chat.read`,
      {},
      JSON.stringify({
        chatroomId: chatRoomId,
        messageId: messageId,
      })
    );
    logger.info(`읽음 상태 전송: 채팅방=${chatRoomId}, 메시지=${messageId}`);
    return true;
  } catch (error) {
    logger.error('읽음 상태 전송 중 오류 발생:', error);
    return false;
  }
}

export function sendAiChatInit(chatRoomId) {
  if (!stompClient || !stompClient.connected) {
    logger.error('웹소켓 연결이 없습니다. AI 초기화를 보낼 수 없습니다.');
    return false;
  }

  try {
    stompClient.send(
      `/app/chat.ai.init`,
      {},
      JSON.stringify({
        chatroomId: chatRoomId,
      })
    );
    logger.info(`AI 채팅 초기화 전송: 채팅방=${chatRoomId}`);
    return true;
  } catch (error) {
    logger.error('AI 초기화 전송 중 오류 발생:', error);
    return false;
  }
}

export function unsubscribe(destination) {
  if (subscriptions[destination] && subscriptions[destination].subscription) {
    subscriptions[destination].subscription.unsubscribe();
    delete subscriptions[destination];
    logger.info(`${destination} 구독 해제 완료`);
  }
}

export function disconnectWebSocket() {
  logger.info('[webSocketService] 웹소켓 연결 해제 시작');

  isManualDisconnect = true;

  stopHeartbeat();

  if (stompClient && stompClient.connected) {
    Object.keys(subscriptions).forEach(destination => {
      if (subscriptions[destination].subscription) {
        subscriptions[destination].subscription.unsubscribe();
      }
    });
    subscriptions = {};

    stompClient.disconnect(() => {
      logger.info('[webSocketService] STOMP 연결 해제 콜백 실행됨');
    });
    stompClient = null;
    logger.info('[webSocketService] 웹소켓 연결 종료 완료');
  }

  reconnectAttempts = 0;
  connectionState = 'disconnected';
  notifyConnectionStateChange('disconnected');

  connectionStateCallbacks = [];
}

export function getWebSocketStatus() {
  if (!stompClient) {
    return 'NOT_INITIALIZED';
  }
  if (stompClient.connected) {
    return 'CONNECTED';
  }
  return 'DISCONNECTED';
}

export function isWebSocketConnected() {
  return stompClient && stompClient.connected;
}

export function forceReconnect() {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    logger.warn('[forceReconnect] 인증 토큰이 없어 재연결하지 않습니다.');
    return;
  }

  logger.info('강제 재연결 시도...');
  disconnectWebSocket();

  isManualDisconnect = false;
  reconnectAttempts = 0;

  setTimeout(() => {
    initializeWebSocket();
  }, 1000);
}

export function subscribeToUserStatus(callback) {
  if (!stompClient || !stompClient.connected) {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      logger.warn('[subscribeToUserStatus] 인증 토큰이 없어 구독하지 않습니다.');
      return;
    }

    logger.info('웹소켓 연결이 없습니다. 연결을 시도합니다.');
    initializeWebSocket();
    subscriptions['user.status'] = { callback };
    setTimeout(() => subscribeToUserStatus(callback), 1000);
    return;
  }

  const destination = '/topic/status';

  if (subscriptions['user.status']) {
    unsubscribe('user.status');
  }

  subscriptions['user.status'] = {
    subscription: stompClient.subscribe(destination, message => {
      try {
        const statusMessage = JSON.parse(message.body);
        logger.info('[webSocketService] 사용자 상태 변경:', statusMessage);
        callback(statusMessage);
      } catch (error) {
        logger.error('사용자 상태 메시지 처리 중 오류 발생:', error);
      }
    }),
    callback,
  };

  logger.info(`[webSocketService] 사용자 상태 구독 완료: ${destination}`);
}

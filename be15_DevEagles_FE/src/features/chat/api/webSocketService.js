import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { normalizeTimestamp } from '@/features/chat/utils/timeUtils.js';

let stompClient = null;
let subscriptions = {};
let reconnectAttempts = 0;
let connectionState = 'disconnected'; // 'connecting', 'connected', 'disconnected', 'failed'
let heartbeatInterval = null;
let connectionStateCallbacks = [];
let isManualDisconnect = false; // 수동 연결 해제 플래그 추가
const MAX_RECONNECT_ATTEMPTS = 10;
const BASE_RECONNECT_DELAY = 1000;

// 연결 상태 변경 이벤트 구독
export function onConnectionStateChange(callback) {
  connectionStateCallbacks.push(callback);
  callback(connectionState); // 현재 상태 즉시 전달
}

function notifyConnectionStateChange(newState) {
  connectionState = newState;
  connectionStateCallbacks.forEach(callback => {
    try {
      callback(newState);
    } catch (error) {
      console.error('연결 상태 콜백 오류:', error);
    }
  });
}

function startHeartbeat() {
  if (heartbeatInterval) clearInterval(heartbeatInterval);

  heartbeatInterval = setInterval(() => {
    if (stompClient && stompClient.connected) {
      // 하트비트 전송 (STOMP 자체 하트비트 사용)
      console.log('WebSocket 하트비트 확인됨');
    } else {
      console.warn('WebSocket 연결이 끊어짐 - 재연결 시도');
      // 수동 연결 해제나 인증 토큰이 없으면 재연결하지 않음
      const token = localStorage.getItem('accessToken');
      if (!isManualDisconnect && token) {
        connectWebSocket();
      } else {
        console.log('수동 연결 해제 상태이거나 인증 토큰이 없어 재연결하지 않습니다.');
        stopHeartbeat();
      }
    }
  }, 30000); // 30초마다 확인
}

function stopHeartbeat() {
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval);
    heartbeatInterval = null;
  }
}

export function initializeWebSocket() {
  if (stompClient && stompClient.connected) {
    console.log('이미 웹소켓이 연결되어 있습니다.');
    return;
  }

  // 수동 연결 해제 플래그 리셋
  isManualDisconnect = false;

  connectWebSocket();
}

function connectWebSocket() {
  if (connectionState === 'connecting') {
    console.log('이미 연결 시도 중입니다.');
    return;
  }

  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS || isManualDisconnect) {
    if (isManualDisconnect) {
      console.log('수동 연결 해제 상태로 재연결하지 않습니다.');
    } else {
      console.error('최대 재연결 시도 횟수를 초과했습니다.');
      notifyConnectionStateChange('failed');
    }
    return;
  }

  // 인증 토큰 확인
  const token = localStorage.getItem('accessToken');
  if (!token) {
    console.log('인증 토큰이 없어 웹소켓 연결을 시도하지 않습니다.');
    return;
  }

  notifyConnectionStateChange('connecting');

  // 기본 API URL 설정
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
  const wsUrl = `${apiBaseUrl}/ws`;

  const socket = new SockJS(wsUrl);
  stompClient = Stomp.over(socket);

  // 하트비트 설정
  stompClient.heartbeat.outgoing = 20000; // 20초마다 클라이언트 -> 서버
  stompClient.heartbeat.incoming = 20000; // 20초마다 서버 -> 클라이언트

  // 디버그 모드 활성화
  stompClient.debug = function (str) {
    console.log('STOMP: ' + str);
  };

  console.log(`웹소켓 연결 시도... (${reconnectAttempts + 1}/${MAX_RECONNECT_ATTEMPTS})`, {
    url: wsUrl,
  });

  const connectHeaders = {};

  if (token) {
    connectHeaders['Authorization'] = `Bearer ${token}`;
    console.log('웹소켓 연결에 JWT 토큰 포함');
  } else {
    console.warn('JWT 토큰이 없어 웹소켓 인증 없이 연결');
  }

  stompClient.connect(
    connectHeaders,
    () => {
      console.log('웹소켓 연결 성공');
      reconnectAttempts = 0;
      notifyConnectionStateChange('connected');
      startHeartbeat();

      // 기존 구독 복원
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
      console.error('웹소켓 연결 실패:', error?.message || '알 수 없는 오류');
      notifyConnectionStateChange('disconnected');
      stopHeartbeat();
      reconnectAttempts++;

      // 수동 연결 해제나 인증 토큰이 없으면 재연결하지 않음
      const token = localStorage.getItem('accessToken');
      if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && !isManualDisconnect && token) {
        // 지수 백오프 재연결
        const delay = Math.min(BASE_RECONNECT_DELAY * Math.pow(2, reconnectAttempts - 1), 30000);
        console.log(
          `${delay}ms 후 재연결 시도... (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`
        );

        setTimeout(() => {
          connectWebSocket();
        }, delay);
      } else {
        if (isManualDisconnect) {
          console.log('수동 연결 해제 상태로 재연결하지 않습니다.');
        } else if (!token) {
          console.log('인증 토큰이 없어 재연결하지 않습니다.');
        } else {
          console.error('최대 재연결 시도 횟수를 초과했습니다.');
          notifyConnectionStateChange('failed');
        }
      }
    }
  );

  // 연결 해제 이벤트 처리
  socket.addEventListener('close', () => {
    console.log('웹소켓 연결이 종료되었습니다.');
    notifyConnectionStateChange('disconnected');
    stopHeartbeat();

    // 수동 연결 해제가 아니고 인증 토큰이 있는 경우에만 자동 재연결 시도
    const token = localStorage.getItem('accessToken');
    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && !isManualDisconnect && token) {
      setTimeout(() => {
        connectWebSocket();
      }, BASE_RECONNECT_DELAY);
    } else {
      if (isManualDisconnect) {
        console.log('수동 연결 해제 상태로 재연결하지 않습니다.');
      } else if (!token) {
        console.log('인증 토큰이 없어 재연결하지 않습니다.');
      }
    }
  });
}

export function subscribeToChatRoom(chatRoomId, callback) {
  if (!stompClient || !stompClient.connected) {
    // 인증 토큰 확인
    const token = localStorage.getItem('accessToken');
    if (!token) {
      console.log('[subscribeToChatRoom] 인증 토큰이 없어 구독하지 않습니다.');
      return;
    }

    console.log('웹소켓 연결이 없습니다. 연결을 시도합니다.');
    initializeWebSocket();
    // 웹소켓 연결이 완료될 때까지 구독 정보 저장
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

        // 받은 메시지 구조를 상세히 로깅
        console.log('[webSocketService] 받은 메시지 전체 구조:', {
          ...receivedMessage,
          hasTimestamp: !!receivedMessage.timestamp,
          hasCreatedAt: !!receivedMessage.createdAt,
          timestampValue: receivedMessage.timestamp,
          createdAtValue: receivedMessage.createdAt,
        });

        // 타임스탬프 검증 및 자동 설정 (timeUtils 사용)
        if (!receivedMessage.timestamp && !receivedMessage.createdAt) {
          receivedMessage.timestamp = normalizeTimestamp(null);
          console.warn('[webSocketService] 웹소켓 메시지에 타임스탬프 없어 현재 시간 설정:', {
            messageId: receivedMessage.id,
            newTimestamp: receivedMessage.timestamp,
          });
        } else if (!receivedMessage.timestamp && receivedMessage.createdAt) {
          // createdAt이 있으면 timestamp로 복사
          receivedMessage.timestamp = receivedMessage.createdAt;
          console.log('[webSocketService] createdAt을 timestamp로 복사:', {
            messageId: receivedMessage.id,
            timestamp: receivedMessage.timestamp,
          });
        }

        callback(receivedMessage);
      } catch (error) {
        console.error('메시지 처리 중 오류 발생:', error);
      }
    }),
    callback,
  };

  console.log(`채팅방 ${chatRoomId} 구독 완료`);
}

export function subscribeToReadStatus(chatRoomId, callback) {
  if (!stompClient || !stompClient.connected) {
    // 인증 토큰 확인
    const token = localStorage.getItem('accessToken');
    if (!token) {
      console.log('[subscribeToReadStatus] 인증 토큰이 없어 구독하지 않습니다.');
      return;
    }

    console.log('웹소켓 연결이 없습니다. 연결을 시도합니다.');
    initializeWebSocket();
    // 웹소켓 연결이 완료될 때까지 구독 정보 저장
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
        console.error('읽음 상태 처리 중 오류 발생:', error);
      }
    }),
    callback,
  };

  console.log(`채팅방 ${chatRoomId} 읽음 상태 구독 완료`);
}

export function sendWebSocketMessage(chatRoomId, message, userId, userName) {
  if (!stompClient || !stompClient.connected) {
    console.error('웹소켓 연결이 없습니다. 메시지를 보낼 수 없습니다.');
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

    console.log('[webSocketService] 메시지 전송:', {
      content: messageData.content?.substring(0, 30),
      senderId: messageData.senderId,
      senderName: messageData.senderName,
      chatroomId: messageData.chatroomId,
    });

    stompClient.send(`/app/chat.send`, {}, JSON.stringify(messageData));
    return true;
  } catch (error) {
    console.error('메시지 전송 중 오류 발생:', error);
    return false;
  }
}

export function sendReadMessage(chatRoomId, messageId) {
  if (!stompClient || !stompClient.connected) {
    console.error('웹소켓 연결이 없습니다. 읽음 상태를 보낼 수 없습니다.');
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
    console.log(`읽음 상태 전송: 채팅방=${chatRoomId}, 메시지=${messageId}`);
    return true;
  } catch (error) {
    console.error('읽음 상태 전송 중 오류 발생:', error);
    return false;
  }
}

// AI 채팅 초기화 메시지 전송
export function sendAiChatInit(chatRoomId) {
  if (!stompClient || !stompClient.connected) {
    console.error('웹소켓 연결이 없습니다. AI 초기화를 보낼 수 없습니다.');
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
    console.log(`AI 채팅 초기화 전송: 채팅방=${chatRoomId}`);
    return true;
  } catch (error) {
    console.error('AI 초기화 전송 중 오류 발생:', error);
    return false;
  }
}

export function unsubscribe(destination) {
  if (subscriptions[destination] && subscriptions[destination].subscription) {
    subscriptions[destination].subscription.unsubscribe();
    delete subscriptions[destination];
    console.log(`${destination} 구독 해제 완료`);
  }
}

export function disconnectWebSocket() {
  console.log('[webSocketService] 웹소켓 연결 해제 시작');

  // 수동 연결 해제 플래그 설정
  isManualDisconnect = true;

  // 하트비트 중지
  stopHeartbeat();

  if (stompClient && stompClient.connected) {
    // 모든 구독 해제
    Object.keys(subscriptions).forEach(destination => {
      if (subscriptions[destination].subscription) {
        subscriptions[destination].subscription.unsubscribe();
      }
    });
    subscriptions = {};

    // STOMP 연결 해제
    stompClient.disconnect(() => {
      console.log('[webSocketService] STOMP 연결 해제 콜백 실행됨');
    });
    stompClient = null;
    console.log('[webSocketService] 웹소켓 연결 종료 완료');
  }

  // 상태 초기화
  reconnectAttempts = 0;
  connectionState = 'disconnected';
  notifyConnectionStateChange('disconnected');

  // 연결 상태 콜백 배열 초기화
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
  // 인증 토큰 확인
  const token = localStorage.getItem('accessToken');
  if (!token) {
    console.log('[forceReconnect] 인증 토큰이 없어 재연결하지 않습니다.');
    return;
  }

  console.log('강제 재연결 시도...');
  disconnectWebSocket();

  // 수동 연결 해제 플래그 리셋 (강제 재연결이므로)
  isManualDisconnect = false;
  reconnectAttempts = 0;

  setTimeout(() => {
    initializeWebSocket();
  }, 1000);
}

export function subscribeToUserStatus(callback) {
  if (!stompClient || !stompClient.connected) {
    // 인증 토큰 확인
    const token = localStorage.getItem('accessToken');
    if (!token) {
      console.log('[subscribeToUserStatus] 인증 토큰이 없어 구독하지 않습니다.');
      return;
    }

    console.log('웹소켓 연결이 없습니다. 연결을 시도합니다.');
    initializeWebSocket();
    // 웹소켓 연결이 완료될 때까지 구독 정보 저장
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
        console.log('[webSocketService] 사용자 상태 변경:', statusMessage);
        callback(statusMessage);
      } catch (error) {
        console.error('사용자 상태 메시지 처리 중 오류 발생:', error);
      }
    }),
    callback,
  };

  console.log(`[webSocketService] 사용자 상태 구독 완료: ${destination}`);
}

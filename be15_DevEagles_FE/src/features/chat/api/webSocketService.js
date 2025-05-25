import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { normalizeTimestamp } from '@/features/chat/utils/timeUtils.js';

let stompClient = null;
let subscriptions = {};
let reconnectAttempts = 0;
let connectionState = 'disconnected'; // 'connecting', 'connected', 'disconnected', 'failed'
let heartbeatInterval = null;
let connectionStateCallbacks = [];
let isLoggedOut = false; // 로그아웃 상태 추적
const MAX_RECONNECT_ATTEMPTS = 5;
const BASE_RECONNECT_DELAY = 1000;
const MAX_RECONNECT_DELAY = 30000; // 최대 30초

// 연결 상태 변경 이벤트 구독
export function onConnectionStateChange(callback) {
  connectionStateCallbacks.push(callback);
  callback(connectionState); // 현재 상태 즉시 전달
}

function notifyConnectionStateChange(newState) {
  connectionState = newState;
  connectionStateCallbacks.forEach(callbackItem => {
    try {
      // 콜백이 함수인지 확인
      const callback = typeof callbackItem === 'function' ? callbackItem : callbackItem?.callback;
      if (typeof callback === 'function') {
        callback(newState);
      } else {
        console.warn('[webSocketService] 유효하지 않은 콜백:', callbackItem);
      }
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
    } else if (!isLoggedOut) {
      // 로그아웃 상태가 아닐 때만 재연결 시도
      console.warn('WebSocket 연결이 끊어짐 - 재연결 시도');
      connectWebSocket();
    }
  }, 60000); // 30초에서 60초로 변경
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
    return Promise.resolve();
  }

  // 기존 연결이 있으면 먼저 정리
  if (stompClient) {
    console.log('[webSocketService] 기존 웹소켓 연결 정리 중...');
    try {
      stompClient.disconnect();
    } catch (error) {
      console.warn('[webSocketService] 기존 연결 정리 중 오류:', error);
    }
    stompClient = null;
  }

  if (connectionState === 'connecting') {
    console.log('이미 연결 시도 중입니다. 연결 완료까지 대기...');
    // 연결 완료까지 대기하는 Promise 반환
    return new Promise(resolve => {
      const checkConnection = () => {
        if (stompClient && stompClient.connected) {
          resolve();
        } else if (connectionState === 'failed' || connectionState === 'disconnected') {
          // 연결 실패 시 새로 시도
          connectWebSocket();
          setTimeout(checkConnection, 1000);
        } else {
          setTimeout(checkConnection, 500);
        }
      };
      checkConnection();
    });
  }

  return new Promise(resolve => {
    const originalCallback = connectionStateCallbacks.find(cb => cb.name === 'initResolve');
    if (!originalCallback) {
      connectionStateCallbacks.push({
        name: 'initResolve',
        callback: state => {
          if (state === 'connected') {
            resolve();
          }
        },
      });
    }
    connectWebSocket();
  });
}

function connectWebSocket() {
  if (connectionState === 'connecting') {
    console.log('이미 연결 시도 중입니다.');
    return;
  }

  // 로그아웃 상태에서는 재연결하지 않음
  if (isLoggedOut) {
    console.log('[webSocketService] 로그아웃 상태로 인해 재연결을 중단합니다.');
    return;
  }

  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    console.error('최대 재연결 시도 횟수를 초과했습니다.');
    notifyConnectionStateChange('failed');
    return;
  }

  notifyConnectionStateChange('connecting');

  // 기본 API URL 설정
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
  const wsUrl = `${apiBaseUrl}/ws`;

  const socket = new SockJS(wsUrl);
  stompClient = Stomp.over(socket);

  // 하트비트 설정 (간격 늘림)
  stompClient.heartbeat.outgoing = 60000; // 20초에서 60초로 변경
  stompClient.heartbeat.incoming = 60000; // 20초에서 60초로 변경

  // 디버그 모드 (개발 환경에서만)
  if (import.meta.env.DEV) {
    stompClient.debug = function (str) {
      console.log('STOMP: ' + str);
    };
  } else {
    stompClient.debug = null; // 프로덕션에서는 디버그 비활성화
  }

  console.log(`웹소켓 연결 시도... (${reconnectAttempts + 1}/${MAX_RECONNECT_ATTEMPTS})`, {
    url: wsUrl,
  });

  // JWT 토큰 가져오기
  const token = localStorage.getItem('accessToken');
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

      if (!isLoggedOut) {
        const delay = Math.min(
          BASE_RECONNECT_DELAY * Math.pow(2, reconnectAttempts - 1),
          MAX_RECONNECT_DELAY
        );
        console.log(
          `${delay}ms 후 재연결 시도... (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`
        );

        setTimeout(() => {
          connectWebSocket();
        }, delay);
      } else {
        console.log('[webSocketService] 로그아웃 상태로 인해 재연결을 중단합니다.');
      }
    }
  );

  // 연결 해제 이벤트 처리
  socket.addEventListener('close', () => {
    console.log('웹소켓 연결이 종료되었습니다.');
    notifyConnectionStateChange('disconnected');
    stopHeartbeat();

    // 로그아웃 상태가 아닐 때만 자동 재연결 시도
    if (!isLoggedOut && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      setTimeout(() => {
        connectWebSocket();
      }, BASE_RECONNECT_DELAY);
    }
  });
}

export function subscribeToChatRoom(chatRoomId, callback) {
  if (!stompClient || !stompClient.connected) {
    console.log('웹소켓 연결이 없습니다. 구독 정보를 저장하고 연결을 대기합니다.');
    // 웹소켓 연결이 완료될 때까지 구독 정보 저장
    subscriptions[`chatroom.${chatRoomId}`] = { callback };

    // 연결 시도 중이 아닐 때만 초기화 시도
    if (connectionState !== 'connecting') {
      initializeWebSocket()
        .then(() => {
          // 연결 완료 후 실제 구독 실행
          if (subscriptions[`chatroom.${chatRoomId}`]) {
            subscribeToChatRoom(chatRoomId, callback);
          }
        })
        .catch(error => {
          console.error('웹소켓 초기화 실패:', error);
        });
    }
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
    console.log('웹소켓 연결이 없습니다. 읽음 상태 구독 정보를 저장하고 연결을 대기합니다.');
    // 웹소켓 연결이 완료될 때까지 구독 정보 저장
    subscriptions[`chatroom.${chatRoomId}.read`] = { callback };

    // 연결 시도 중이 아닐 때만 초기화 시도
    if (connectionState !== 'connecting') {
      initializeWebSocket()
        .then(() => {
          // 연결 완료 후 실제 구독 실행
          if (subscriptions[`chatroom.${chatRoomId}.read`]) {
            subscribeToReadStatus(chatRoomId, callback);
          }
        })
        .catch(error => {
          console.error('웹소켓 초기화 실패:', error);
        });
    }
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

  // 로그아웃 상태로 설정하여 자동 재연결 방지
  isLoggedOut = true;

  // 하트비트 정지
  stopHeartbeat();

  if (stompClient && stompClient.connected) {
    console.log('[webSocketService] 연결된 웹소켓 해제 중...');

    // 모든 구독 해제
    Object.keys(subscriptions).forEach(destination => {
      if (subscriptions[destination].subscription) {
        subscriptions[destination].subscription.unsubscribe();
        console.log(`[webSocketService] 구독 해제: ${destination}`);
      }
    });
    subscriptions = {};

    // 웹소켓 연결 해제 (콜백으로 완료 확인)
    stompClient.disconnect(() => {
      console.log('[webSocketService] STOMP 연결 해제 완료');
    });

    // 연결 상태 변경 알림
    notifyConnectionStateChange('disconnected');
    console.log('[webSocketService] 웹소켓 연결 종료 처리 완료');
  } else {
    console.log('[webSocketService] 이미 연결이 해제된 상태');
  }

  // 콜백 배열 정리 (메모리 누수 방지)
  connectionStateCallbacks = [];

  // stompClient 초기화
  stompClient = null;
}

// 새로운 로그인 시 상태 리셋
export function resetWebSocketState() {
  console.log('[webSocketService] 웹소켓 상태 리셋');
  isLoggedOut = false;
  reconnectAttempts = 0;
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
  console.log('강제 재연결 시도...');
  disconnectWebSocket();
  reconnectAttempts = 0;
  setTimeout(() => {
    initializeWebSocket();
  }, 1000);
}

export function subscribeToUserStatus(callback) {
  if (!stompClient || !stompClient.connected) {
    console.log('웹소켓 연결이 없습니다. 사용자 상태 구독 정보를 저장하고 연결을 대기합니다.');
    // 웹소켓 연결이 완료될 때까지 구독 정보 저장
    subscriptions['user.status'] = { callback };

    // 연결 시도 중이 아닐 때만 초기화 시도
    if (connectionState !== 'connecting') {
      initializeWebSocket()
        .then(() => {
          // 연결 완료 후 실제 구독 실행
          if (subscriptions['user.status']) {
            subscribeToUserStatus(callback);
          }
        })
        .catch(error => {
          console.error('웹소켓 초기화 실패:', error);
        });
    }
    return;
  }

  const destination = '/topic/status';

  // 이미 구독 중인지 확인
  if (subscriptions['user.status'] && subscriptions['user.status'].subscription) {
    console.log('[webSocketService] 사용자 상태 이미 구독 중, 기존 구독 해제 후 재구독');
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

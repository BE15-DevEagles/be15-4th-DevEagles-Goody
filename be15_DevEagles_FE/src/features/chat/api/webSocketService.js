import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { normalizeTimestamp } from '@/features/chat/utils/timeUtils.js';

let stompClient = null;
let subscriptions = {};
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

export function initializeWebSocket() {
  if (stompClient && stompClient.connected) {
    console.log('이미 웹소켓이 연결되어 있습니다.');
    return;
  }

  connectWebSocket();
}

function connectWebSocket() {
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    console.error('최대 재연결 시도 횟수를 초과했습니다.');
    return;
  }

  // 기본 API URL 설정
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
  const wsUrl = `${apiBaseUrl}/ws`;

  const socket = new SockJS(wsUrl);
  stompClient = Stomp.over(socket);

  // 디버그 모드 활성화
  stompClient.debug = function (str) {
    console.log('STOMP: ' + str);
  };

  console.log('웹소켓 연결 시도...', { url: wsUrl });

  stompClient.connect(
    {}, // 헤더 없이 연결
    () => {
      console.log('웹소켓 연결 성공');
      reconnectAttempts = 0;

      // 기존 구독 복원
      const currentSubscriptions = { ...subscriptions };
      subscriptions = {};
      Object.keys(currentSubscriptions).forEach(destination => {
        const callback = currentSubscriptions[destination].callback;
        subscribeToChatRoom(destination.split('.')[1], callback);
      });
    },
    error => {
      console.error('웹소켓 연결 실패:', error?.message || '알 수 없는 오류');
      reconnectAttempts++;

      const delay = Math.min(1000 * (reconnectAttempts + 1), 10000);
      console.log(`${delay}ms 후 재연결 시도... (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);
      setTimeout(() => {
        connectWebSocket();
      }, delay);
    }
  );
}

export function subscribeToChatRoom(chatRoomId, callback) {
  if (!stompClient || !stompClient.connected) {
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
      senderId: userId,
      senderName: userName,
      messageType: 'TEXT',
    };

    console.log('[webSocketService] 메시지 전송:', messageData.content?.substring(0, 30));

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
  if (stompClient && stompClient.connected) {
    Object.keys(subscriptions).forEach(destination => {
      if (subscriptions[destination].subscription) {
        subscriptions[destination].subscription.unsubscribe();
      }
    });
    subscriptions = {};

    stompClient.disconnect();
    stompClient = null;
    console.log('웹소켓 연결 종료');
  }
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

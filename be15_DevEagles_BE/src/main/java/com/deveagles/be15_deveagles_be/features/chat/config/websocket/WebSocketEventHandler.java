package com.deveagles.be15_deveagles_be.features.chat.config.websocket;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.UserStatusMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@RequiredArgsConstructor
public class WebSocketEventHandler {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketEventHandler.class);
  private static final String USER_STATUS_TOPIC = "/topic/status";
  private static final String REDIS_KEY_ONLINE_USERS = "chat:online_users";

  private final SimpMessagingTemplate messagingTemplate;
  private final RedisTemplate<String, String> redisTemplate;
  private final Map<String, String> connectedUsers = new ConcurrentHashMap<>();

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();
    logger.info("웹소켓 연결 요청: 세션 ID={}", sessionId);

    if (headerAccessor.getUser() != null) {
      String username = headerAccessor.getUser().getName();
      logger.info("인증된 사용자 연결 요청: 사용자={}, 세션={}", username, sessionId);
    }
  }

  @EventListener
  public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();

    String userId = extractUserId(headerAccessor);
    if (userId != null) {
      logger.info("사용자 연결 완료: 사용자ID={}, 세션ID={}", userId, sessionId);

      try {
        redisTemplate.opsForSet().add(REDIS_KEY_ONLINE_USERS, userId);
        logger.info("User {} added to online users in Redis.", userId);

        connectedUsers.put(sessionId, userId);
        notifyUserStatusChange(userId, true);
        logger.info("User {} online status broadcasted to all users.", userId);
      } catch (Exception e) {
        logger.error(
            "Failed to add user {} to online_users in Redis: {}", userId, e.getMessage(), e);
      }
    } else {
      logger.warn("세션 ID {}에 대한 사용자 정보를 추출할 수 없음", sessionId);
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();
    logger.info("웹소켓 연결 해제: 세션 ID={}", sessionId);

    String userId = connectedUsers.remove(sessionId);
    if (userId != null) {
      logger.info("사용자 연결 종료: 사용자ID={}, 세션ID={}", userId, sessionId);

      // 현재 연결된 모든 세션 로그
      long userSessionCount =
          connectedUsers.values().stream().filter(id -> id.equals(userId)).count();

      logger.info("사용자 {} 남은 세션 수: {}", userId, userSessionCount);

      boolean userStillConnected = connectedUsers.values().contains(userId);

      if (!userStillConnected) {
        try {
          redisTemplate.opsForSet().remove(REDIS_KEY_ONLINE_USERS, userId);
          logger.info("User {} removed from online users in Redis.", userId);

          // 오프라인 상태 브로드캐스트
          notifyUserStatusChange(userId, false);
          logger.info("User {} offline status broadcasted to all users.", userId);
        } catch (Exception e) {
          logger.error(
              "Failed to remove user {} from online_users in Redis: {}", userId, e.getMessage(), e);
        }
      } else {
        logger.info(
            "User {} still has {} active sessions. Not removing from Redis online list.",
            userId,
            userSessionCount);
      }
    } else {
      logger.warn("세션 ID {}에 대한 사용자 정보를 찾을 수 없음", sessionId);
    }

    // 현재 전체 연결 상태 로그
    logger.info(
        "현재 총 연결된 세션 수: {}, 고유 사용자 수: {}",
        connectedUsers.size(),
        connectedUsers.values().stream().distinct().count());
  }

  private String extractUserId(StompHeaderAccessor headerAccessor) {
    try {
      if (headerAccessor.getUser() != null) {
        String userId = headerAccessor.getUser().getName();
        logger.debug("사용자 ID 추출 성공: {}", userId);
        return userId;
      } else {
        logger.warn("Principal이 null입니다. 인증되지 않은 연결일 수 있습니다.");
        return null;
      }
    } catch (Exception e) {
      logger.error("사용자 ID 추출 중 오류 발생: {}", e.getMessage(), e);
      return null;
    }
  }

  private void notifyUserStatusChange(String userId, boolean isOnline) {
    UserStatusMessage statusMessage = new UserStatusMessage(userId, isOnline);
    messagingTemplate.convertAndSend(USER_STATUS_TOPIC, statusMessage);
    logger.debug("사용자 상태 변경 알림 전송: {}", statusMessage);
  }
}

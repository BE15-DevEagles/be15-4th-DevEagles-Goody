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
        // Redis에 사용자 추가 (Set이므로 중복 자동 제거)
        Long addedCount = redisTemplate.opsForSet().add(REDIS_KEY_ONLINE_USERS, userId);
        boolean wasAdded = addedCount != null && addedCount > 0;
        logger.info("User {} added to online users in Redis. Was new: {}", userId, wasAdded);

        // 세션 매핑 저장
        connectedUsers.put(sessionId, userId);

        // 새로 추가된 사용자만 온라인 상태 브로드캐스트
        if (wasAdded) {
          notifyUserStatusChange(userId, true);
          logger.info("User {} online status broadcasted (new user).", userId);
        } else {
          logger.info("User {} was already online, skipping broadcast.", userId);
        }
      } catch (Exception e) {
        logger.error(
            "Failed to add user {} to online_users in Redis: {}", userId, e.getMessage(), e);
        // Redis 실패 시에도 세션 매핑은 저장
        connectedUsers.put(sessionId, userId);
      }
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

      // 해당 사용자의 다른 활성 세션이 있는지 확인
      boolean userStillConnected = connectedUsers.values().contains(userId);

      if (!userStillConnected) {
        // 짧은 지연 후 처리 (API 로그아웃과의 경합 조건 방지)
        try {
          Thread.sleep(500); // 500ms 대기
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        try {
          // Redis에서 사용자 상태 확인 후 제거
          Boolean isOnline = redisTemplate.opsForSet().isMember(REDIS_KEY_ONLINE_USERS, userId);

          if (Boolean.TRUE.equals(isOnline)) {
            Long removed = redisTemplate.opsForSet().remove(REDIS_KEY_ONLINE_USERS, userId);
            logger.info(
                "User {} removed from online users in Redis. Removed count: {}", userId, removed);

            // 실제로 제거된 경우에만 오프라인 상태 브로드캐스트
            if (removed != null && removed > 0) {
              notifyUserStatusChange(userId, false);
              logger.info("User {} offline status broadcasted to all users.", userId);
            }
          } else {
            logger.info(
                "User {} was already removed from Redis online users (likely by API logout).",
                userId);
          }
        } catch (Exception e) {
          logger.error(
              "Failed to remove user {} from online_users in Redis: {}", userId, e.getMessage(), e);

          // Redis 실패 시에도 오프라인 상태는 브로드캐스트 (중복 방지를 위해 한 번만)
          try {
            notifyUserStatusChange(userId, false);
            logger.info("User {} offline status broadcasted despite Redis error.", userId);
          } catch (Exception broadcastError) {
            logger.error(
                "Failed to broadcast offline status for user {}: {}",
                userId,
                broadcastError.getMessage());
          }
        }
      } else {
        logger.info(
            "User {} still has other active sessions. Not removing from Redis online list.",
            userId);
      }
    } else {
      logger.warn("세션 ID {}에 대한 사용자 정보를 찾을 수 없음", sessionId);
    }
  }

  private String extractUserId(StompHeaderAccessor headerAccessor) {
    if (headerAccessor.getUser() != null) {
      return headerAccessor.getUser().getName();
    }
    return null;
  }

  private void notifyUserStatusChange(String userId, boolean isOnline) {
    UserStatusMessage statusMessage = new UserStatusMessage(userId, isOnline);
    messagingTemplate.convertAndSend(USER_STATUS_TOPIC, statusMessage);
    logger.debug("사용자 상태 변경 알림 전송: {}", statusMessage);
  }
}

package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.UserStatusMessage;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/user-status")
@RequiredArgsConstructor
public class UserStatusController {

  private final RedisTemplate<String, String> redisTemplate;
  private final SimpMessagingTemplate messagingTemplate;
  private static final String REDIS_KEY_ONLINE_USERS = "chat:online_users";
  private static final String USER_STATUS_TOPIC = "/topic/status";

  @GetMapping("/online-users")
  public ResponseEntity<ApiResponse<Set<String>>> getOnlineUsers() {
    try {
      Set<String> onlineUsers = redisTemplate.opsForSet().members(REDIS_KEY_ONLINE_USERS);
      log.info("온라인 사용자 목록 조회: {}명", onlineUsers != null ? onlineUsers.size() : 0);
      return ResponseEntity.ok(ApiResponse.success(onlineUsers));
    } catch (Exception e) {
      log.error("온라인 사용자 목록 조회 실패", e);
      return ResponseEntity.ok(ApiResponse.success(Set.of()));
    }
  }

  @DeleteMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
    try {
      if (authentication != null && authentication.getName() != null) {
        String userId = authentication.getName();

        // Redis에서 사용자 제거
        Long removed = redisTemplate.opsForSet().remove(REDIS_KEY_ONLINE_USERS, userId);
        log.info("사용자 {} 로그아웃 처리: Redis에서 {}개 제거됨", userId, removed);

        // 오프라인 상태 브로드캐스트
        UserStatusMessage statusMessage = new UserStatusMessage(userId, false);
        messagingTemplate.convertAndSend(USER_STATUS_TOPIC, statusMessage);
        log.info("사용자 {} 오프라인 상태 브로드캐스트 완료", userId);

        return ResponseEntity.ok(ApiResponse.success(null));
      } else {
        log.warn("인증 정보가 없어 로그아웃 처리를 건너뜁니다");
        return ResponseEntity.ok(ApiResponse.success(null));
      }
    } catch (Exception e) {
      log.error("로그아웃 처리 실패", e);
      return ResponseEntity.ok(ApiResponse.success(null));
    }
  }
}

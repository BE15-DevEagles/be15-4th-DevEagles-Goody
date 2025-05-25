package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/user-status")
@RequiredArgsConstructor
public class UserStatusController {

  private final RedisTemplate<String, String> redisTemplate;
  private static final String REDIS_KEY_ONLINE_USERS = "chat:online_users";

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
}

package com.deveagles.be15_deveagles_be.features.chat.config.websocket;

import com.deveagles.be15_deveagles_be.common.jwt.JwtTokenProvider;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = extractTokenFromHeaders(accessor);

      if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
        try {
          String username = jwtTokenProvider.getUsernameFromJWT(token);
          CustomUser userDetails = (CustomUser) userDetailsService.loadUserByUsername(username);

          // Principal 설정 (userId를 name으로 사용)
          Principal principal = () -> String.valueOf(userDetails.getUserId());
          accessor.setUser(principal);

          log.info("웹소켓 인증 성공: userId={}", userDetails.getUserId());
        } catch (Exception e) {
          log.error("웹소켓 인증 실패: {}", e.getMessage());
        }
      } else {
        log.warn("웹소켓 연결 시 유효하지 않은 토큰");
      }
    }

    return message;
  }

  private String extractTokenFromHeaders(StompHeaderAccessor accessor) {
    // Authorization 헤더에서 토큰 추출
    String authHeader = accessor.getFirstNativeHeader("Authorization");
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    // 쿼리 파라미터에서 토큰 추출 (fallback)
    String tokenParam = accessor.getFirstNativeHeader("token");
    if (StringUtils.hasText(tokenParam)) {
      return tokenParam;
    }

    return null;
  }
}

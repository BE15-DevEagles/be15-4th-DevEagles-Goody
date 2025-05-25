package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import com.deveagles.be15_deveagles_be.common.jwt.JwtTokenProvider;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindIdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindPwdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.UserFindIdResponse;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.UserStatus;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import jakarta.mail.MessagingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final RedisTemplate<String, String> redisTemplate;
  private final AuthCodeService authCodeService;
  private final MailService mailService;

  @Override
  public TokenResponse login(LoginRequest request) {

    User user =
        userRepository
            .findValidUserForLogin(request.username(), LocalDateTime.now().minusMonths(1))
            .orElseThrow(() -> new BadCredentialsException("로그인할 수 없는 계정입니다."));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BadCredentialsException("올바르지 않은 비밀번호입니다.");
    }

    String accessToken = jwtTokenProvider.createToken(user.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

    refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

    return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  @Override
  public void logout(String refreshToken, String accessToken) {

    jwtTokenProvider.validateToken(refreshToken);
    String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);
    refreshTokenService.deleteRefreshToken(username);

    long remainTime = jwtTokenProvider.getRemainingExpiration(accessToken);
    redisTemplate.opsForValue().set("BL:" + accessToken, "logout", Duration.ofMillis(remainTime));
  }

  @Override
  public UserFindIdResponse findId(UserFindIdRequest request) {

    User validUser =
        userRepository
            .findValidUserForGetEmail(
                request.userName(), request.phoneNumber(), LocalDateTime.now().minusMonths(1))
            .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    return UserFindIdResponse.builder().email(validUser.getEmail()).build();
  }

  @Override
  public String sendFindPwdEmail(UserFindPwdRequest request) {

    userRepository
        .findValidUserForGetPwd(
            request.userName(), request.email(), LocalDateTime.now().minusMonths(1))
        .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    if (authCodeService.getAuthCode(request.email()) != null) {
      throw new UserBusinessException(UserErrorCode.DUPLICATE_SEND_AUTH_EXCEPTION);
    }

    String authCode = UUID.randomUUID().toString().substring(0, 6);
    authCodeService.saveAuthCode(request.email(), authCode);

    try {
      mailService.sendFindPwdEmail(request.email(), authCode);
    } catch (MessagingException e) {
      throw new UserBusinessException(UserErrorCode.SEND_EMAIL_FAILURE_EXCEPTION);
    }

    return authCode;
  }

  @Override
  public TokenResponse refreshToken(String refreshToken) {
    // 리프레시 토큰 유효성 검사
    jwtTokenProvider.validateToken(refreshToken);
    String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

    // Redis에서 저장된 refresh token 가져오기
    String redisKey = "RT:" + username;
    String storedToken = redisTemplate.opsForValue().get(redisKey);

    if (storedToken == null) {
      throw new BadCredentialsException("해당 유저로 저장된 리프레시 토큰 없음");
    }

    if (!storedToken.equals(refreshToken)) {
      throw new BadCredentialsException("리프레시 토큰 일치하지 않음");
    }

    User user =
        userRepository
            .findUserByEmail(username)
            .orElseThrow(() -> new BadCredentialsException("해당 유저 없음"));

    // 새로운 토큰 재발급
    String newAccessToken = jwtTokenProvider.createToken(user.getEmail());
    String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

    redisTemplate
        .opsForValue()
        .set(
            redisKey,
            newRefreshToken,
            jwtTokenProvider.getRefreshExpiration(),
            TimeUnit.MILLISECONDS);

    return TokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  @Override
  public String sendAuthEmail(String email) {
    userRepository
        .findUserByEmail(email)
        .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    if (authCodeService.getAuthCode(email) != null) {
      throw new UserBusinessException(UserErrorCode.DUPLICATE_SEND_AUTH_EXCEPTION);
    }

    String authCode = String.valueOf((int) (Math.random() * 900000) + 100000);
    // 최신화를 위해 삭제
    authCodeService.deleteAuthCode(email);
    authCodeService.saveAuthCode(email, authCode);
    try {
      mailService.sendAuthMail(email, authCode);
    } catch (MessagingException e) {
      throw new UserBusinessException(UserErrorCode.SEND_EMAIL_FAILURE_EXCEPTION);
    }

    return authCode;
  }

  @Override
  public void verifyAuthCode(String email, String authCode) {
    String savedCode = authCodeService.getAuthCode(email);

    if (savedCode == null || !savedCode.equals(authCode)) {
      throw new UserBusinessException(UserErrorCode.INVALID_AUTH_CODE);
    }

    User user =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    user.setEnabledUser(UserStatus.ENABLED);
    userRepository.save(user);

    authCodeService.deleteAuthCode(email);
  }
}

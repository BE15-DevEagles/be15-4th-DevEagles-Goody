package com.deveagles.be15_deveagles_be.features.auth.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.*;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.UserFindIdResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthCodeService;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthService;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.MailService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.UserStatus;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth 관련 API")
public class AuthController {

  private final AuthService authService;
  private final AuthCodeService authCodeService;
  private final MailService mailService;
  private final UserRepository userRepository;

  @Operation(summary = "로그인", description = "사용자가 사이트에 로그인합니다.")
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenResponse>> login(
      @RequestBody @Valid LoginRequest request) {

    TokenResponse response = authService.login(request);

    return buildTokenResponse(response);
  }

  @Operation(summary = "로그아웃", description = "사용자가 사이트에서 로그아웃합니다.")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken,
      @RequestHeader(name = "Authorization", required = false) String authHeader) {

    if (refreshToken != null && authHeader != null && authHeader.startsWith("Bearer ")) {
      String accessToken = authHeader.substring(7);
      authService.logout(refreshToken, accessToken);
    }

    ResponseCookie deleteCookie = createDeleteRefreshTokenCookie();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(ApiResponse.success(null));
  }

  @Operation(summary = "아이디 찾기", description = "사용자가 가입한 아이디 정보를 조회합니다.")
  @PostMapping("/findid")
  public ResponseEntity<ApiResponse<UserFindIdResponse>> findId(
      @RequestBody @Valid UserFindIdRequest request) {

    UserFindIdResponse response = authService.findId(request);

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @Operation(summary = "비밀번호 인증 요청", description = "사용자의 비밀번호 변경을 위한 인증을 요청합니다.")
  @PostMapping("/findpwd")
  public ResponseEntity<ApiResponse<Void>> findPwd(@RequestBody @Valid UserFindPwdRequest request) {

    String authCode = authService.sendFindPwdEmail(request);
    log.info("### 비밀번호 찾기 인증 요청 authCode: {}", authCode);

    return ResponseEntity.ok().body(ApiResponse.success(null));
  }

  @Operation(summary = "사용자 상태 확인", description = "사용자의 인증 상태를 확인합니다.")
  @PostMapping("/valid")
  public ResponseEntity<ApiResponse<Boolean>> validUserStatus(
      @AuthenticationPrincipal CustomUser customUser) {

    Boolean is_valid;

    if (customUser.getUserStatus().equals(UserStatus.PENDING)) is_valid = Boolean.FALSE;
    else if (customUser.getUserStatus().equals(UserStatus.ENABLED)) is_valid = Boolean.TRUE;
    else return null;

    return ResponseEntity.ok().body(ApiResponse.success(is_valid));
  }

  @Operation(summary = "사용자 인증 메일 전송", description = "사용자 인증을 위한 메일을 전송합니다.")
  @PostMapping("/sendauth")
  public ResponseEntity<ApiResponse<Void>> sendAuthEmail(
      @RequestBody @Valid SendAuthEmailRequest request) {

    try {
      String authCode = authService.sendAuthEmail(request.email());

      log.info("### 본인 인증 요청 authCode: {}", authCode);

      return ResponseEntity.ok().body(ApiResponse.success(null));
    } catch (UserBusinessException e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.failure(
                  e.getUserErrorCode().getCode(), e.getUserErrorCode().getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.failure(
                  UserErrorCode.SEND_EMAIL_FAILURE_EXCEPTION.getCode(),
                  UserErrorCode.SEND_EMAIL_FAILURE_EXCEPTION.getMessage()));
    }
  }

  @Operation(summary = "사용자 인증", description = "사용자가 전달 받은 인증코드를 통해 본인 인증을 합니다.")
  @PostMapping("/verify")
  public ResponseEntity<ApiResponse<Void>> verify(@RequestBody @Valid UserVerifyRequest request) {

    try {
      authService.verifyAuthCode(request.email(), request.authCode());
      return ResponseEntity.ok().body(ApiResponse.success(null));
    } catch (UserBusinessException e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.failure(
                  e.getUserErrorCode().getCode(), e.getUserErrorCode().getMessage()));
    }
  }

  @Operation(summary = "RefreshToken 재발급", description = "accessToken 만료 시 자동으로 Token을 재발급합니다.")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
      @CookieValue(name = "refreshToken", required = false) String refreshToken // HttpOnly 쿠키에서 읽어옴
      ) {
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // refreshToken이 없으면 401 반환
    }
    TokenResponse tokenResponse = authService.refreshToken(refreshToken);
    return buildTokenResponse(tokenResponse);
  }

  private ResponseEntity<ApiResponse<TokenResponse>> buildTokenResponse(TokenResponse response) {

    ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success(response));
  }

  private ResponseCookie createRefreshTokenCookie(String refreshToken) {

    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .path("/")
        .maxAge(Duration.ofDays(7))
        .sameSite("Strict")
        .build();
  }

  private ResponseCookie createDeleteRefreshTokenCookie() {

    return ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .build();
  }
}

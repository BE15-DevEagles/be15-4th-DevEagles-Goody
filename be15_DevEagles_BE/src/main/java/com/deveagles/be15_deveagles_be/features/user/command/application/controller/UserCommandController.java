package com.deveagles.be15_deveagles_be.features.user.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.*;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "회원", description = "회원 관련 API")
public class UserCommandController {

  private final UserCommandService userCommandService;
  private final UserRepository userRepository;

  @Operation(summary = "회원가입", description = "사용자가 사이트에 회원가입합니다.")
  @PostMapping("/users")
  public ResponseEntity<ApiResponse<Void>> userRegister(
      @RequestPart @Valid UserCreateRequest request,
      @RequestPart(required = false) MultipartFile profile) {

    userCommandService.userRegister(request, profile);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  @Operation(summary = "아이디, 전화번호 중복 확인", description = "사용자가 입력한 아이디와 전화번호의 중복 여부를 확인합니다..")
  @PostMapping("/users/duplcheck")
  public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestBody UserDuplRequest request) {

    Optional<User> user = Optional.empty();
    if (request.email() != null) user = userRepository.findUserByEmail(request.email());
    else if (request.phoneNumber() != null)
      user = userRepository.findUserByPhoneNumber(request.phoneNumber());

    Boolean checked;

    if (user.isEmpty()) checked = Boolean.TRUE;
    else checked = Boolean.FALSE;

    return ResponseEntity.ok().body(ApiResponse.success(checked));
  }

  @Operation(summary = "내 정보 조회", description = "사용자 개인의 정보를 조회합니다.")
  @GetMapping("/users/me")
  public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetails(
      @AuthenticationPrincipal CustomUser customUser) {

    UserDetailResponse response = userCommandService.getUserDetails(customUser.getUserId());

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @Operation(summary = "비밀번호 인증", description = "사용자가 회원 정보 수정을 요청할 시 비밀번호로 인증을 합니다.")
  @PostMapping("/users/valid")
  public ResponseEntity<ApiResponse<Boolean>> validUserPassword(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestBody @Valid UserPasswordRequest request) {

    Boolean is_Valid =
        userCommandService.validUserPassword(customUser.getUserId(), request.password());

    return ResponseEntity.ok().body(ApiResponse.success(is_Valid));
  }

  @Operation(summary = "회원 정보 수정", description = "사용자가 회원 정보를 수정합니다.")
  @PatchMapping("/users/mod")
  public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserDetails(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestPart @Valid UserUpdateRequest request,
      @RequestPart(required = false) MultipartFile profile) {

    log.info("🔥 profile == null ? {}", (profile == null));
    if (profile != null) {
      log.info("🔥 profile.isEmpty() ? {}", profile.isEmpty());
      log.info("🔥 profile original filename = {}", profile.getOriginalFilename());
      log.info("🔥 profile content type = {}", profile.getContentType());
    }
    UserDetailResponse response =
        userCommandService.updateUserDetails(customUser.getUserId(), request, profile);

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @Operation(summary = "비밀번호 변경", description = "사용자가 비밀번호를 변경합니다.")
  @PatchMapping("/users/mod/pwd")
  public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserPassword(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestBody @Valid UserPasswordRequest request) {

    UserDetailResponse response =
        userCommandService.updateUserPassword(customUser.getUserId(), request.password());

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @Operation(summary = "이메일 인증을 통한 비밀번호 변경", description = "사용자가 이메일 인증을 통해 비밀번호를 변경합니다.")
  @PatchMapping("/users/email/pwd")
  public ResponseEntity<ApiResponse<Void>> updateUserPasswordFromEmail(
      @RequestBody @Valid UserEmailPasswordRequest request) {

    userCommandService.updateUserPasswordFromEmail(request);

    return ResponseEntity.ok().body(ApiResponse.success(null));
  }

  @Operation(summary = "회원 탈퇴", description = "사용자가 사이트에서 탈퇴합니다.")
  @DeleteMapping("/users")
  public ResponseEntity<ApiResponse<Void>> withDrawUser(
      @AuthenticationPrincipal CustomUser customUser) {

    userCommandService.withDrawUser(customUser.getUserId());

    return ResponseEntity.ok().body(ApiResponse.success(null));
  }
}

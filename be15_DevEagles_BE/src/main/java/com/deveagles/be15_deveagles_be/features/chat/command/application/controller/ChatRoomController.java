package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.NotificationSettingResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.NotificationToggleResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ReadReceiptService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/chatrooms")
@Tag(name = "채팅방", description = "채팅방 관리 API")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;
  private final ReadReceiptService readReceiptService;

  public ChatRoomController(
      ChatRoomService chatRoomService, ReadReceiptService readReceiptService) {
    this.chatRoomService = chatRoomService;
    this.readReceiptService = readReceiptService;
  }

  @PostMapping
  @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "채팅방 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
      @RequestBody CreateChatRoomRequest request, @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.createChatRoom(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @PostMapping("/default")
  @Operation(summary = "기본 채팅방 생성", description = "팀의 기본 채팅방을 생성합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "기본 채팅방 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createDefaultChatRoom(
      @RequestParam String teamId,
      @RequestParam String name,
      @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.createDefaultChatRoom(teamId, name);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @PostMapping("/ai")
  @Operation(summary = "AI 채팅방 생성", description = "개인 AI 채팅방을 생성하거나 가져옵니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "AI 채팅방 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetPersonalAiChatRoom(
      @RequestParam String teamId,
      @RequestParam String userId,
      @RequestParam String name,
      @AuthenticationPrincipal CustomUser customUser) {
    // 인증된 사용자 ID를 사용
    if (customUser != null) {
      userId = String.valueOf(customUser.getUserId());
    }
    ChatRoomResponse chatRoom = chatRoomService.createOrGetPersonalAiChatRoom(teamId, userId, name);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @DeleteMapping("/{chatroomId}")
  @Operation(summary = "채팅방 삭제", description = "채팅방을 삭제합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "채팅방 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방을 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> deleteChatRoom(
      @PathVariable String chatroomId, @AuthenticationPrincipal CustomUser customUser) {
    Optional<ChatRoomResponse> deletedChatRoom = chatRoomService.deleteChatRoom(chatroomId);

    return deletedChatRoom
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
  }

  @PostMapping("/{chatroomId}/participants")
  @Operation(summary = "참가자 추가", description = "채팅방에 참가자를 추가합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "참가자 추가 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방을 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> addParticipant(
      @PathVariable String chatroomId,
      @RequestParam String userId,
      @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.addParticipantToChatRoom(chatroomId, userId);
    return ResponseEntity.ok(ApiResponse.success(chatRoom));
  }

  @DeleteMapping("/{chatroomId}/participants/{userId}")
  @Operation(summary = "참가자 제거", description = "채팅방에서 참가자를 제거합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "참가자 제거 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방 또는 참가자를 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> removeParticipant(
      @PathVariable String chatroomId,
      @PathVariable String userId,
      @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.removeParticipantFromChatRoom(chatroomId, userId);
    return ResponseEntity.ok(ApiResponse.success(chatRoom));
  }

  @PutMapping("/{chatroomId}/participants/{userId}/notification")
  @Operation(summary = "알림 설정 토글", description = "채팅방 참가자의 알림 설정을 토글합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "알림 설정 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방 또는 참가자를 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> toggleParticipantNotification(
      @PathVariable String chatroomId,
      @PathVariable String userId,
      @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.toggleParticipantNotification(chatroomId, userId);
    return ResponseEntity.ok(ApiResponse.success(chatRoom));
  }

  @GetMapping("/{chatroomId}/notification")
  @Operation(summary = "채팅방 알림 설정 조회", description = "현재 사용자의 채팅방 알림 설정을 조회합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "알림 설정 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방 또는 참가자를 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<Boolean>> getChatNotificationSetting(
      @PathVariable String chatroomId, @AuthenticationPrincipal CustomUser customUser) {
    String userId = String.valueOf(customUser.getUserId());
    Boolean notificationEnabled = chatRoomService.getChatNotificationSetting(chatroomId, userId);
    return ResponseEntity.ok(ApiResponse.success(notificationEnabled));
  }

  @PutMapping("/{chatroomId}/notification/toggle")
  @Operation(summary = "현재 사용자 알림 설정 토글", description = "현재 사용자의 채팅방 알림 설정을 토글합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "알림 설정 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방 또는 참가자를 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<NotificationToggleResponse>> toggleCurrentUserNotification(
      @PathVariable String chatroomId, @AuthenticationPrincipal CustomUser customUser) {
    String userId = String.valueOf(customUser.getUserId());
    NotificationToggleResponse response =
        chatRoomService.toggleCurrentUserNotification(chatroomId, userId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/notifications")
  @Operation(summary = "모든 채팅방 알림 설정 조회", description = "현재 사용자의 모든 채팅방 알림 설정을 조회합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "알림 설정 조회 성공")
      })
  public ResponseEntity<ApiResponse<List<NotificationSettingResponse>>> getAllNotificationSettings(
      @AuthenticationPrincipal CustomUser customUser) {
    String userId = String.valueOf(customUser.getUserId());
    List<NotificationSettingResponse> settings = chatRoomService.getAllNotificationSettings(userId);
    return ResponseEntity.ok(ApiResponse.success(settings));
  }

  @PutMapping("/{chatroomId}/read")
  @Operation(summary = "채팅방 읽음 처리", description = "채팅방의 마지막 메시지를 읽음 처리합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "읽음 처리 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방을 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<Void>> markChatAsRead(
      @PathVariable String chatroomId,
      @RequestParam(required = false) String messageId,
      @AuthenticationPrincipal CustomUser customUser) {
    String userId = String.valueOf(customUser.getUserId());

    try {
      if (messageId != null) {
        // 특정 메시지를 읽음 처리
        readReceiptService.markMessageAsRead(chatroomId, messageId, userId);
        log.info(
            "특정 메시지 읽음 처리 완료: chatroomId={}, messageId={}, userId={}",
            chatroomId,
            messageId,
            userId);
      } else {
        // 채팅방의 모든 메시지를 읽음 처리
        readReceiptService.markAllMessagesAsRead(chatroomId, userId);
        log.info("채팅방 모든 메시지 읽음 처리 완료: chatroomId={}, userId={}", chatroomId, userId);
      }

      return ResponseEntity.ok(ApiResponse.success(null));
    } catch (Exception e) {
      log.error(
          "읽음 처리 실패: chatroomId={}, messageId={}, userId={}, error={}",
          chatroomId,
          messageId,
          userId,
          e.getMessage(),
          e);
      return ResponseEntity.status(500).body(ApiResponse.failure("READ_FAILED", "읽음 처리에 실패했습니다."));
    }
  }
}

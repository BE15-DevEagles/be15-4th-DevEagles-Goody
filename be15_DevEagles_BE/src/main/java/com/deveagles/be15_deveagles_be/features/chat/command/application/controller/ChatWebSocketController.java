package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.AiChatService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.AutoEmotionAnalysisService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ReadReceiptService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ReadReceiptRepository;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import java.security.Principal;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

  private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);
  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
  private static final String CHATROOM_READ_TOPIC_FORMAT = "/topic/chatroom.%s.read";

  private final ChatMessageService chatMessageService;
  private final ChatRoomService chatRoomService;
  private final AiChatService aiChatService;
  private final AutoEmotionAnalysisService autoEmotionAnalysisService;
  private final ChatRoomRepository chatRoomRepository;
  private final ReadReceiptRepository readReceiptRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final RedisTemplate<String, String> redisTemplate;
  private final UserCommandService userCommandService;
  private final ReadReceiptService readReceiptService;

  public ChatWebSocketController(
      ChatMessageService chatMessageService,
      ChatRoomService chatRoomService,
      AiChatService aiChatService,
      AutoEmotionAnalysisService autoEmotionAnalysisService,
      ChatRoomRepository chatRoomRepository,
      ReadReceiptRepository readReceiptRepository,
      SimpMessagingTemplate messagingTemplate,
      RedisTemplate<String, String> redisTemplate,
      UserCommandService userCommandService,
      ReadReceiptService readReceiptService) {
    this.chatMessageService = chatMessageService;
    this.chatRoomService = chatRoomService;
    this.aiChatService = aiChatService;
    this.autoEmotionAnalysisService = autoEmotionAnalysisService;
    this.chatRoomRepository = chatRoomRepository;
    this.readReceiptRepository = readReceiptRepository;
    this.messagingTemplate = messagingTemplate;
    this.redisTemplate = redisTemplate;
    this.userCommandService = userCommandService;
    this.readReceiptService = readReceiptService;
  }

  @MessageMapping("/chat.send")
  public ChatMessageResponse sendMessage(
      @Payload ChatMessageRequest request,
      SimpMessageHeaderAccessor headerAccessor,
      Principal principal) {

    String senderId = principal != null ? principal.getName() : request.getSenderId();
    String senderName = request.getSenderName();

    // 사용자 이름이 없거나 "알 수 없는 사용자"인 경우 실제 사용자 정보 조회
    if (senderName == null || senderName.trim().isEmpty() || "알 수 없는 사용자".equals(senderName)) {
      try {
        Long userId = Long.parseLong(senderId);
        UserDetailResponse userDetail = userCommandService.getUserDetails(userId);
        if (userDetail != null && userDetail.getUserName() != null) {
          senderName = userDetail.getUserName();
          log.info("사용자 이름 조회 성공: userId={}, userName={}", userId, senderName);
        } else {
          log.warn("사용자 정보를 찾을 수 없음: userId={}", userId);
          senderName = "알 수 없는 사용자";
        }
      } catch (NumberFormatException e) {
        log.error("잘못된 사용자 ID 형식: {}", senderId);
        senderName = "알 수 없는 사용자";
      } catch (Exception e) {
        log.error("사용자 정보 조회 실패: userId={}, error={}", senderId, e.getMessage());
        senderName = "알 수 없는 사용자";
      }
    }

    ChatMessageRequest finalRequest =
        ChatMessageRequest.builder()
            .chatroomId(request.getChatroomId())
            .senderId(senderId)
            .senderName(senderName)
            .messageType(request.getMessageType())
            .content(request.getContent())
            .metadata(request.getMetadata())
            .build();

    log.info(
        "메시지 전송: chatroomId={}, senderId={}, senderName={}, content={}",
        finalRequest.getChatroomId(),
        finalRequest.getSenderId(),
        finalRequest.getSenderName(),
        finalRequest.getContent() != null
            ? finalRequest
                .getContent()
                .substring(0, Math.min(20, finalRequest.getContent().length()))
            : "null");

    ChatMessageResponse userMessageResponse = chatMessageService.sendMessage(finalRequest);

    final ChatMessageRequest aiRequest = finalRequest;
    chatRoomRepository
        .findById(finalRequest.getChatroomId())
        .ifPresent(
            chatRoom -> {
              if (chatRoom.getType() == ChatRoomType.AI) {
                log.info("AI 채팅방에 메시지 수신: {}", aiRequest.getContent());

                // AI 응답 생성
                CompletableFuture.runAsync(
                    () -> {
                      try {
                        aiChatService.processUserMessage(aiRequest);
                      } catch (Exception e) {
                        log.error("AI 응답 생성 중 오류 발생", e);
                      }
                    });

                // 자동 감정 분석 (5개 메시지마다)
                CompletableFuture.runAsync(
                    () -> {
                      try {
                        autoEmotionAnalysisService.processUserMessage(
                            aiRequest.getSenderId(),
                            aiRequest.getChatroomId(),
                            aiRequest.getContent());
                      } catch (Exception e) {
                        log.error("자동 감정 분석 중 오류 발생", e);
                      }
                    });
              }
            });

    return userMessageResponse;
  }

  @MessageMapping("/chat.read")
  public void markAsRead(@Payload ReadMessageRequest request, Principal principal) {
    if (principal == null || request.getChatroomId() == null || request.getMessageId() == null) {
      log.warn("Invalid markAsRead request: principal or chatroomId or messageId is null");
      return;
    }

    String userId = principal.getName();
    String chatroomId = request.getChatroomId();
    String messageId = request.getMessageId();

    boolean success = false;

    // 1. ReadReceiptService를 사용하여 DB에 읽음 상태 저장
    try {
      readReceiptService.markMessageAsRead(chatroomId, messageId, userId);
      log.info(
          "ReadReceipt 저장 성공: userId={}, messageId={}, chatroomId={}",
          userId,
          messageId,
          chatroomId);
      success = true;
    } catch (Exception e) {
      log.error(
          "ReadReceipt 저장 실패: userId={}, messageId={}, chatroomId={}, error={}",
          userId,
          messageId,
          chatroomId,
          e.getMessage(),
          e);
    }

    // 2. Redis에 읽음 상태 저장 (빠른 응답용, 폴백)
    if (success) {
      try {
        String redisKey = "chat:last_read_message:" + chatroomId;
        redisTemplate.opsForHash().put(redisKey, userId, messageId);
        log.debug(
            "Redis 읽음 상태 업데이트 성공: userId={}, messageId={}, chatroomId={}",
            userId,
            messageId,
            chatroomId);
      } catch (Exception e) {
        log.error(
            "Redis 읽음 상태 업데이트 실패: userId={}, messageId={}, chatroomId={}, error={}",
            userId,
            messageId,
            chatroomId,
            e.getMessage(),
            e);
      }
    }

    // 3. ChatRoom의 participant lastReadMessage 업데이트 (폴백)
    if (!success) {
      try {
        chatRoomService.updateLastReadMessage(chatroomId, userId, messageId);
        log.info(
            "ChatRoom 폴백 읽음 처리 성공: userId={}, messageId={}, chatroomId={}",
            userId,
            messageId,
            chatroomId);
        success = true;
      } catch (Exception e) {
        log.error(
            "ChatRoom 폴백 읽음 처리 실패: userId={}, messageId={}, chatroomId={}, error={}",
            userId,
            messageId,
            chatroomId,
            e.getMessage(),
            e);
        return;
      }
    }

    // 4. 읽음 상태 이벤트 전송
    if (success) {
      ReadStatusResponse readStatusResponse = new ReadStatusResponse(chatroomId, userId, messageId);
      String destination = String.format(CHATROOM_READ_TOPIC_FORMAT, chatroomId);
      messagingTemplate.convertAndSend(destination, readStatusResponse);
      log.debug(
          "읽음 상태 이벤트 전송: destination={}, userId={}, messageId={}", destination, userId, messageId);
    }
  }

  @MessageMapping("/chat.ai.init")
  public void initializeAiChat(@Payload AiChatInitRequest request, Principal principal) {
    if (principal == null) {
      return;
    }

    String userId = principal.getName();
    aiChatService.initializeAiChatSession(userId, request.getChatroomId());

    log.info("AI 채팅 세션 초기화 요청: 사용자={}, 채팅방={}", userId, request.getChatroomId());
  }

  public static class ReadMessageRequest {
    private String chatroomId;
    private String messageId;

    public String getChatroomId() {
      return chatroomId;
    }

    public String getMessageId() {
      return messageId;
    }
  }

  public static class ReadStatusResponse {
    private String chatroomId;
    private String userId;
    private String lastReadMessageId;

    public ReadStatusResponse(String chatroomId, String userId, String lastReadMessageId) {
      this.chatroomId = chatroomId;
      this.userId = userId;
      this.lastReadMessageId = lastReadMessageId;
    }

    public String getChatroomId() {
      return chatroomId;
    }

    public String getUserId() {
      return userId;
    }

    public String getLastReadMessageId() {
      return lastReadMessageId;
    }
  }

  public static class AiChatInitRequest {
    private String chatroomId;

    public String getChatroomId() {
      return chatroomId;
    }
  }
}

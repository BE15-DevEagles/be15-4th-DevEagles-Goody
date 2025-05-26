package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

  private static final Logger log = LoggerFactory.getLogger(ChatMessageServiceImpl.class);
  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
  private static final String CHATROOM_TOPIC_FORMAT = "/topic/chatroom.%s";
  private static final String CHATROOM_DELETE_TOPIC_FORMAT = "/topic/chatroom.%s.delete";

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public ChatMessageServiceImpl(
      ChatMessageRepository chatMessageRepository,
      ChatRoomRepository chatRoomRepository,
      SimpMessagingTemplate messagingTemplate,
      RedisTemplate<String, String> redisTemplate,
      ObjectMapper objectMapper) {
    this.chatMessageRepository = chatMessageRepository;
    this.chatRoomRepository = chatRoomRepository;
    this.messagingTemplate = messagingTemplate;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public ChatMessageResponse sendMessage(ChatMessageRequest request) {
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(request.getChatroomId())
            .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    // AI 사용자에 대한 특별 처리
    boolean isAiUser = "ai-assistant".equals(request.getSenderId());
    boolean isAiChatRoom = chatRoom.getType() == ChatRoom.ChatRoomType.AI;
    boolean isTeamChatRoom = chatRoom.getType() == ChatRoom.ChatRoomType.TEAM;

    // AI 사용자가 AI 채팅방 또는 팀 채팅방에서 메시지를 보내는 경우 권한 검증 생략
    if (!isAiUser || (!isAiChatRoom && !isTeamChatRoom)) {
      boolean isParticipant =
          chatRoom.getActiveParticipants().stream()
              .anyMatch(participant -> participant.getUserId().equals(request.getSenderId()));

      if (!isParticipant) {
        throw new ChatBusinessException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
      }
    }

    // UTC로 현재 시간 생성 (시간대 문제 해결)
    LocalDateTime utcTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

    ChatMessage message =
        ChatMessage.builder()
            .chatroomId(request.getChatroomId())
            .senderId(request.getSenderId())
            .senderName(request.getSenderName())
            .messageType(request.getMessageType())
            .content(request.getContent())
            .metadata(request.getMetadata())
            .createdAt(utcTime)
            .build();

    // MongoDB에 메시지 저장 (Primary Storage)
    ChatMessage savedMessage = chatMessageRepository.save(message);

    // 채팅방 마지막 메시지 정보 업데이트
    updateChatRoomLastMessage(chatRoom, savedMessage);

    ChatMessageResponse response = ChatMessageResponse.from(savedMessage);

    // 웹소켓으로 채팅방 메시지 전송
    String destination = String.format(CHATROOM_TOPIC_FORMAT, request.getChatroomId());
    messagingTemplate.convertAndSend(destination, response);
    log.debug("채팅방 메시지 전송 완료 -> 채팅방ID: {}, 메시지ID: {}", request.getChatroomId(), response.getId());

    try {
      String messageJson = objectMapper.writeValueAsString(response);
      String redisKey = "chat:messages:" + request.getChatroomId();

      // UTC 기준으로 score 계산 (시간대 문제 해결)
      double score = savedMessage.getCreatedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

      redisTemplate.opsForZSet().add(redisKey, messageJson, score);

      Long size = redisTemplate.opsForZSet().size(redisKey);
      if (size != null && size > 100) {
        redisTemplate.opsForZSet().removeRange(redisKey, 0, 0);
      }
    } catch (Exception e) {
      System.err.println("Failed to cache message in Redis: " + e.getMessage());
    }

    return response;
  }

  @Override
  public Optional<ChatMessageResponse> getMessage(String messageId) {
    return chatMessageRepository.findById(messageId).map(ChatMessageResponse::from);
  }

  @Override
  public List<ChatMessageResponse> getMessagesByChatroom(String chatroomId, int page, int size) {
    chatRoomRepository
        .findById(chatroomId)
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    PagedResult<ChatMessage> messages =
        chatMessageRepository.findMessagesByChatroomIdWithPagination(chatroomId, page, size);

    return messages.getContent().stream()
        .map(ChatMessageResponse::from)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<ChatMessageResponse> deleteMessage(String messageId) {
    Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);

    if (messageOpt.isEmpty()) {
      return Optional.empty();
    }

    ChatMessage message = messageOpt.get();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_DELETE_ACCESS_DENIED);
    }

    String currentUserId = authentication.getName();

    if (!message.getSenderId().equals(currentUserId)) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_DELETE_ACCESS_DENIED);
    }

    message.delete();
    ChatMessage updatedMessage = chatMessageRepository.save(message);

    ChatMessageResponse response = ChatMessageResponse.from(updatedMessage);

    // 웹소켓으로 메시지 삭제 이벤트 전송
    String destination = String.format(CHATROOM_DELETE_TOPIC_FORMAT, message.getChatroomId());
    messagingTemplate.convertAndSend(destination, response);
    log.debug(
        "메시지 삭제 이벤트 전송 완료 -> 채팅방ID: {}, 메시지ID: {}", message.getChatroomId(), response.getId());

    return Optional.of(response);
  }

  @Override
  public List<ChatMessageResponse> getMessagesByChatroomBefore(
      String chatroomId, String messageId, int limit) {
    chatRoomRepository
        .findById(chatroomId)
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
    if (messageOpt.isEmpty()) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_NOT_FOUND);
    }

    ChatMessage message = messageOpt.get();
    List<ChatMessage> messages =
        chatMessageRepository.findMessagesByChatroomIdBeforeTimestamp(
            chatroomId, message.getCreatedAt(), limit);

    return messages.stream().map(ChatMessageResponse::from).collect(Collectors.toList());
  }

  @Override
  public List<ChatMessageResponse> getMessagesByChatroomAfter(
      String chatroomId, String messageId, int limit) {
    chatRoomRepository
        .findById(chatroomId)
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
    if (messageOpt.isEmpty()) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_NOT_FOUND);
    }

    ChatMessage message = messageOpt.get();
    List<ChatMessage> messages =
        chatMessageRepository.findMessagesByChatroomIdAfterTimestamp(
            chatroomId, message.getCreatedAt(), limit);

    return messages.stream().map(ChatMessageResponse::from).collect(Collectors.toList());
  }

  private void updateChatRoomLastMessage(ChatRoom chatRoom, ChatMessage message) {
    ChatRoom.LastMessageInfo lastMessageInfo =
        ChatRoom.LastMessageInfo.builder()
            .id(message.getId())
            .content(message.getContent())
            .senderId(message.getSenderId())
            .senderName(message.getSenderName())
            .sentAt(message.getCreatedAt())
            .build();

    chatRoom.updateLastMessage(lastMessageInfo);
    chatRoomRepository.save(chatRoom);
  }

  @Override
  @Transactional
  public ChatMessageResponse sendRouletteResult(Long userId, String teamId, String result) {
    // 팀의 기본 채팅방 찾기
    ChatRoom defaultChatRoom =
        chatRoomRepository
            .findDefaultChatRoomByTeamId(teamId)
            .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    // AI 수리가 룰렛 결과 메시지 전송
    String content = String.format("🎲 룰렛 결과: %s", result);

    ChatMessageRequest aiMessageRequest =
        ChatMessageRequest.builder()
            .chatroomId(defaultChatRoom.getId())
            .senderId("ai-assistant")
            .senderName("수리 AI")
            .messageType(ChatMessage.MessageType.TEXT)
            .content(content)
            .build();

    return sendMessage(aiMessageRequest);
  }
}

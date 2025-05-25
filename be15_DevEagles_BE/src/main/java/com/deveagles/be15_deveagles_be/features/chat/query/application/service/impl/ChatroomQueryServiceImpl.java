package com.deveagles.be15_deveagles_be.features.chat.query.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ReadReceiptService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.Participant;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ReadReceiptRepository;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomReadSummaryResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomReadSummaryResponse.UnreadByUserDto;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.ChatroomQueryService;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.util.ChatroomResponseConverter;
import com.deveagles.be15_deveagles_be.features.chat.query.domain.repository.ChatroomQueryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatroomQueryServiceImpl implements ChatroomQueryService {

  private final ChatroomQueryRepository chatroomQueryRepository;
  private final ChatroomResponseConverter chatroomResponseConverter;
  private final ChatMessageRepository chatMessageRepository;
  private final ReadReceiptRepository readReceiptRepository;
  private final ReadReceiptService readReceiptService;

  @Override
  public ChatroomListResponse getChatrooms(Long userId, String teamId, int page, int size) {
    log.info("채팅방 목록 조회 서비스 -> 사용자ID: {}, 팀ID: {}", userId, teamId);

    List<ChatRoom> chatrooms =
        chatroomQueryRepository.findChatroomsByUserIdAndTeamId(userId, teamId, page, size);
    int totalCount = chatroomQueryRepository.countChatroomsByUserIdAndTeamId(userId, teamId);

    List<ChatroomResponse> chatroomResponses =
        chatrooms.stream()
            .map(
                chatroom -> {
                  ChatroomResponse response =
                      chatroomResponseConverter.convertToChatroomResponse(chatroom);
                  // ReadReceiptService를 사용하여 읽지 않은 메시지 수 계산
                  int unreadCount =
                      readReceiptService.getUnreadMessageCount(
                          chatroom.getId(), String.valueOf(userId));
                  return ChatroomResponse.builder()
                      .id(response.getId())
                      .teamId(response.getTeamId())
                      .name(response.getName())
                      .isDefault(response.getIsDefault())
                      .type(response.getType())
                      .lastMessage(response.getLastMessage())
                      .participants(response.getParticipants())
                      .createdAt(response.getCreatedAt())
                      .isDeleted(response.getIsDeleted())
                      .unreadCount(unreadCount)
                      .build();
                })
            .collect(Collectors.toList());

    return ChatroomListResponse.of(chatroomResponses, totalCount);
  }

  @Override
  public ChatroomResponse getChatroom(Long userId, String chatroomId) {
    log.info("채팅방 상세 조회 서비스 -> 사용자ID: {}, 채팅방ID: {}", userId, chatroomId);

    Optional<ChatRoom> chatroom = chatroomQueryRepository.findChatroomById(chatroomId);

    if (chatroom.isEmpty()) {
      return null;
    }

    ChatroomResponse response = chatroomResponseConverter.convertToChatroomResponse(chatroom.get());
    // ReadReceiptService를 사용하여 읽지 않은 메시지 수 계산
    int unreadCount = readReceiptService.getUnreadMessageCount(chatroomId, String.valueOf(userId));

    return ChatroomResponse.builder()
        .id(response.getId())
        .teamId(response.getTeamId())
        .name(response.getName())
        .isDefault(response.getIsDefault())
        .type(response.getType())
        .lastMessage(response.getLastMessage())
        .participants(response.getParticipants())
        .createdAt(response.getCreatedAt())
        .isDeleted(response.getIsDeleted())
        .unreadCount(unreadCount)
        .build();
  }

  @Override
  public ChatroomReadSummaryResponse getChatroomReadSummary(Long userId, String chatroomId) {
    log.info("채팅방 읽음 상태 요약 조회 서비스 -> 사용자ID: {}, 채팅방ID: {}", userId, chatroomId);

    Optional<ChatRoom> chatroom = chatroomQueryRepository.findChatroomById(chatroomId);

    if (chatroom.isEmpty()) {
      return null;
    }

    ChatRoom room = chatroom.get();
    ChatRoom.LastMessageInfo lastMessage = room.getLastMessage();

    int totalParticipants = room.getActiveParticipants().size();
    List<UnreadByUserDto> unreadByUsers = getUnreadUsers(room, lastMessage);
    boolean readByAll = unreadByUsers.isEmpty();

    return buildReadSummaryResponse(lastMessage, totalParticipants, unreadByUsers, readByAll);
  }

  private List<UnreadByUserDto> getUnreadUsers(
      ChatRoom room, ChatRoom.LastMessageInfo lastMessage) {
    return room.getActiveParticipants().stream()
        .filter(p -> isUnreadByUser(p, lastMessage))
        .map(this::createUnreadUserDto)
        .collect(Collectors.toList());
  }

  private boolean isUnreadByUser(Participant p, ChatRoom.LastMessageInfo lastMessage) {
    return p.getLastReadMessageId() == null
        || (lastMessage != null && !p.getLastReadMessageId().equals(lastMessage.getId()));
  }

  private UnreadByUserDto createUnreadUserDto(Participant participant) {
    return UnreadByUserDto.builder()
        .userId(participant.getUserId())
        .userName("User " + participant.getUserId()) // 실제로는 사용자 서비스에서 이름을 가져와야 함
        .build();
  }

  private ChatroomReadSummaryResponse buildReadSummaryResponse(
      ChatRoom.LastMessageInfo lastMessage,
      int totalParticipants,
      List<UnreadByUserDto> unreadByUsers,
      boolean readByAll) {

    return ChatroomReadSummaryResponse.builder()
        .lastMessageId(lastMessage != null ? lastMessage.getId() : null)
        .lastMessageTime(lastMessage != null ? lastMessage.getSentAt() : null)
        .readByAll(readByAll)
        .readCount(totalParticipants - unreadByUsers.size())
        .totalParticipants(totalParticipants)
        .unreadByUsers(unreadByUsers)
        .build();
  }
}

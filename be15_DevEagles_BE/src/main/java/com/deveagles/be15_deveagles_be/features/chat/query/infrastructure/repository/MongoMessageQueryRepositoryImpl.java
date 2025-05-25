package com.deveagles.be15_deveagles_be.features.chat.query.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ReadReceipt;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository.SpringDataMongoChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository.SpringDataMongoReadReceiptRepository;
import com.deveagles.be15_deveagles_be.features.chat.query.domain.repository.MessageQueryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoMessageQueryRepositoryImpl implements MessageQueryRepository {

  private final MongoTemplate mongoTemplate;
  private final SpringDataMongoChatMessageRepository chatMessageRepository;
  private final SpringDataMongoReadReceiptRepository readReceiptRepository;

  @Override
  public List<ChatMessage> findMessages(String chatroomId, String beforeMessageId, int limit) {
    if (chatroomId == null || chatroomId.isBlank()) {
      return new ArrayList<>();
    }

    if (beforeMessageId == null || beforeMessageId.isBlank()) {
      PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Direction.DESC, "createdAt"));
      return chatMessageRepository.findByChatroomIdAndDeletedAtIsNullOrderByCreatedAtDesc(
          chatroomId, pageRequest);
    } else {
      Optional<ChatMessage> optionalMessage = chatMessageRepository.findById(beforeMessageId);
      if (optionalMessage.isEmpty()) {
        return new ArrayList<>();
      }

      ChatMessage message = optionalMessage.get();
      LocalDateTime beforeTime = message.getCreatedAt();

      Query query = new Query();
      query.addCriteria(
          Criteria.where("chatroomId")
              .is(chatroomId)
              .and("createdAt")
              .lt(beforeTime)
              .and("deletedAt")
              .isNull());
      query.with(Sort.by(Direction.DESC, "createdAt"));
      query.limit(limit);

      return mongoTemplate.find(query, ChatMessage.class);
    }
  }

  @Override
  public Map<String, Object> getMessageReadStatus(String chatroomId, String messageId) {
    if (chatroomId == null || chatroomId.isBlank() || messageId == null || messageId.isBlank()) {
      return new HashMap<>();
    }

    Map<String, Object> result = new HashMap<>();

    List<ReadReceipt> readReceipts = readReceiptRepository.findByMessageId(messageId);

    Query chatroomQuery = new Query(Criteria.where("_id").is(chatroomId).and("deletedAt").isNull());
    Map chatroomInfo = mongoTemplate.findOne(chatroomQuery, Map.class, "chatroom");

    if (chatroomInfo == null) {
      return result;
    }

    List<Map<String, Object>> participants =
        (List<Map<String, Object>>) chatroomInfo.get("participants");

    if (participants == null || participants.isEmpty()) {
      return result;
    }

    List<String> readUserIds = readReceipts.stream().map(ReadReceipt::getUserId).toList();

    // 활성 참가자 필터링
    List<Map<String, Object>> activeParticipants =
        participants.stream().filter(p -> p.get("deletedAt") == null).toList();

    // 읽은 사용자와 읽지 않은 사용자 분리
    List<Map<String, Object>> readUsers = new ArrayList<>();
    List<Map<String, Object>> unreadUsers = new ArrayList<>();

    for (Map<String, Object> participant : activeParticipants) {
      String userId = (String) participant.get("userId");
      if (readUserIds.contains(userId)) {
        // 읽은 사용자 - ReadReceipt에서 readAt 정보 추가
        ReadReceipt receipt =
            readReceipts.stream()
                .filter(r -> r.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        Map<String, Object> readUser = new HashMap<>(participant);
        if (receipt != null) {
          readUser.put("readAt", receipt.getReadAt());
        }
        readUsers.add(readUser);
      } else {
        // 읽지 않은 사용자
        unreadUsers.add(participant);
      }
    }

    result.put("totalParticipants", activeParticipants.size());
    result.put("readCount", readUsers.size());
    result.put("unreadCount", unreadUsers.size());
    result.put("readUsers", readUsers);
    result.put("unreadUsers", unreadUsers);
    result.put("readReceipts", readReceipts);
    result.put("participants", participants);

    return result;
  }
}

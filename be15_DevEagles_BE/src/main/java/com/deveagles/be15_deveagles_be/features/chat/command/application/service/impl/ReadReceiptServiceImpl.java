package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ReadReceiptService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ReadReceipt;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ReadReceiptRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReadReceiptServiceImpl implements ReadReceiptService {

  private final ReadReceiptRepository readReceiptRepository;
  private final ChatMessageRepository chatMessageRepository;

  @Override
  public ReadReceipt markMessageAsRead(String chatroomId, String messageId, String userId) {
    log.debug("메시지 읽음 처리: chatroomId={}, messageId={}, userId={}", chatroomId, messageId, userId);

    // 중복 체크
    Optional<ReadReceipt> existing =
        readReceiptRepository.findByMessageIdAndUserId(messageId, userId);
    if (existing.isPresent()) {
      log.debug("이미 읽음 처리된 메시지: messageId={}, userId={}", messageId, userId);
      return existing.get();
    }

    // UTC 시간으로 생성
    LocalDateTime utcTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

    ReadReceipt readReceipt =
        ReadReceipt.builder()
            .messageId(messageId)
            .userId(userId)
            .chatroomId(chatroomId)
            .readAt(utcTime)
            .build();

    ReadReceipt saved = readReceiptRepository.save(readReceipt);
    log.info(
        "ReadReceipt 저장 완료: userId={}, messageId={}, chatroomId={}", userId, messageId, chatroomId);

    return saved;
  }

  @Override
  public void markAllMessagesAsRead(String chatroomId, String userId) {
    log.debug("채팅방 모든 메시지 읽음 처리: chatroomId={}, userId={}", chatroomId, userId);

    try {
      // 채팅방의 최신 메시지 조회
      List<ChatMessage> recentMessages =
          chatMessageRepository.findRecentMessagesByChatroomId(chatroomId, 1);

      if (!recentMessages.isEmpty()) {
        ChatMessage latestMessage = recentMessages.get(0);
        markMessageAsRead(chatroomId, latestMessage.getId(), userId);
        log.info(
            "채팅방 최신 메시지 읽음 처리 완료: chatroomId={}, userId={}, messageId={}",
            chatroomId,
            userId,
            latestMessage.getId());
      } else {
        log.debug("채팅방에 메시지가 없음: chatroomId={}", chatroomId);
      }
    } catch (Exception e) {
      log.error(
          "채팅방 모든 메시지 읽음 처리 실패: chatroomId={}, userId={}, error={}",
          chatroomId,
          userId,
          e.getMessage(),
          e);
      throw e;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public LocalDateTime getLastReadTime(String chatroomId, String userId) {
    log.debug("마지막 읽은 시간 조회: chatroomId={}, userId={}", chatroomId, userId);

    try {
      // 최근 1개월 내 ReadReceipt 조회
      List<ReadReceipt> readReceipts =
          readReceiptRepository.findByChatroomIdAndUserIdAndReadAtAfter(
              chatroomId, userId, LocalDateTime.now().minusMonths(1));

      if (!readReceipts.isEmpty()) {
        LocalDateTime lastReadTime =
            readReceipts.stream()
                .map(ReadReceipt::getReadAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        log.debug(
            "ReadReceipt에서 마지막 읽은 시간 조회: chatroomId={}, userId={}, time={}",
            chatroomId,
            userId,
            lastReadTime);
        return lastReadTime;
      }

      log.debug("ReadReceipt 없음: chatroomId={}, userId={}", chatroomId, userId);
      return null;
    } catch (Exception e) {
      log.error(
          "마지막 읽은 시간 조회 실패: chatroomId={}, userId={}, error={}",
          chatroomId,
          userId,
          e.getMessage(),
          e);
      return null;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public int getUnreadMessageCount(String chatroomId, String userId) {
    log.debug("읽지 않은 메시지 수 계산: chatroomId={}, userId={}", chatroomId, userId);

    try {
      LocalDateTime lastReadTime = getLastReadTime(chatroomId, userId);

      if (lastReadTime == null) {
        // 읽은 메시지가 없으면 모든 메시지가 읽지 않음
        long count =
            chatMessageRepository.countUnreadMessagesByChatroomIdAfterTimestamp(
                chatroomId, LocalDateTime.MIN);
        log.debug(
            "읽은 기록 없음, 전체 메시지 수: chatroomId={}, userId={}, count={}", chatroomId, userId, count);
        return (int) count;
      }

      // 마지막 읽은 시간 이후의 메시지 수 계산
      long count =
          chatMessageRepository.countUnreadMessagesByChatroomIdAfterTimestamp(
              chatroomId, lastReadTime);

      log.debug(
          "읽지 않은 메시지 수: chatroomId={}, userId={}, lastReadTime={}, count={}",
          chatroomId,
          userId,
          lastReadTime,
          count);
      return (int) count;

    } catch (Exception e) {
      log.error(
          "읽지 않은 메시지 수 계산 실패: chatroomId={}, userId={}, error={}",
          chatroomId,
          userId,
          e.getMessage(),
          e);
      return 0;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReadReceipt> getMessageReadReceipts(String messageId) {
    log.debug("메시지 읽음 상태 조회: messageId={}", messageId);

    try {
      List<ReadReceipt> receipts = readReceiptRepository.findByMessageId(messageId);
      log.debug("메시지 읽음 상태 조회 완료: messageId={}, count={}", messageId, receipts.size());
      return receipts;
    } catch (Exception e) {
      log.error("메시지 읽음 상태 조회 실패: messageId={}, error={}", messageId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public void deleteUserReadReceipts(String chatroomId, String userId) {
    log.debug("사용자 읽음 기록 삭제: chatroomId={}, userId={}", chatroomId, userId);

    try {
      // 해당 채팅방의 사용자 ReadReceipt 조회 후 삭제
      List<ReadReceipt> userReceipts =
          readReceiptRepository.findByChatroomIdAndUserIdAndReadAtAfter(
              chatroomId, userId, LocalDateTime.now().minusYears(1)); // 1년 내 모든 기록

      for (ReadReceipt receipt : userReceipts) {
        readReceiptRepository.deleteByMessageId(receipt.getMessageId());
      }

      log.info(
          "사용자 읽음 기록 삭제 완료: chatroomId={}, userId={}, count={}",
          chatroomId,
          userId,
          userReceipts.size());
    } catch (Exception e) {
      log.error(
          "사용자 읽음 기록 삭제 실패: chatroomId={}, userId={}, error={}",
          chatroomId,
          userId,
          e.getMessage(),
          e);
      throw e;
    }
  }
}

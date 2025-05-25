package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ReadReceipt;
import java.time.LocalDateTime;
import java.util.List;

public interface ReadReceiptService {

  /**
   * 메시지 읽음 처리
   *
   * @param chatroomId 채팅방 ID
   * @param messageId 메시지 ID
   * @param userId 사용자 ID
   * @return 저장된 ReadReceipt
   */
  ReadReceipt markMessageAsRead(String chatroomId, String messageId, String userId);

  /**
   * 채팅방의 모든 메시지를 읽음 처리 (최신 메시지까지)
   *
   * @param chatroomId 채팅방 ID
   * @param userId 사용자 ID
   */
  void markAllMessagesAsRead(String chatroomId, String userId);

  /**
   * 사용자의 마지막 읽은 메시지 시간 조회
   *
   * @param chatroomId 채팅방 ID
   * @param userId 사용자 ID
   * @return 마지막 읽은 시간
   */
  LocalDateTime getLastReadTime(String chatroomId, String userId);

  /**
   * 채팅방의 읽지 않은 메시지 수 계산
   *
   * @param chatroomId 채팅방 ID
   * @param userId 사용자 ID
   * @return 읽지 않은 메시지 수
   */
  int getUnreadMessageCount(String chatroomId, String userId);

  /**
   * 특정 메시지의 읽음 상태 조회
   *
   * @param messageId 메시지 ID
   * @return ReadReceipt 목록
   */
  List<ReadReceipt> getMessageReadReceipts(String messageId);

  /**
   * 사용자의 읽음 기록 삭제 (채팅방 나가기 시)
   *
   * @param chatroomId 채팅방 ID
   * @param userId 사용자 ID
   */
  void deleteUserReadReceipts(String chatroomId, String userId);
}

package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import java.util.Optional;

public interface AutoEmotionAnalysisService {
  // 시연용으로 임시 제작된 서비스
  Optional<UserMoodHistory> processUserMessage(String userId, String chatroomId, String content);

  int getUserMessageCount(String userId, String chatroomId);

  void resetUserMessageCount(String userId, String chatroomId);

  Optional<UserMoodHistory> analyzeRecentMessages(String userId, String chatroomId);
}

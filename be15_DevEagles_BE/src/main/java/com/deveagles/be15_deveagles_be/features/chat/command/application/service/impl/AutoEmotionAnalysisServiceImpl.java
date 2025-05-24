package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.service.AutoEmotionAnalysisService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.EmotionAnalysisService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.UserMoodHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoEmotionAnalysisServiceImpl implements AutoEmotionAnalysisService {

  private final ChatMessageRepository chatMessageRepository;
  private final UserMoodHistoryRepository userMoodHistoryRepository;
  private final EmotionAnalysisService emotionAnalysisService;
  private final RedisTemplate<String, String> redisTemplate;

  private static final String MESSAGE_COUNT_KEY_PREFIX = "chat:message_count:";
  private static final int ANALYSIS_TRIGGER_COUNT = 5;
  private static final String AI_SENDER_ID = "ai-assistant";

  @Override
  public Optional<UserMoodHistory> processUserMessage(
      String userId, String chatroomId, String content) {
    if (userId == null || chatroomId == null || content == null || content.trim().isEmpty()) {
      log.debug("유효하지 않은 입력으로 감정 분석을 건너뜀: userId={}, chatroomId={}", userId, chatroomId);
      return Optional.empty();
    }

    // 메시지 카운트 증가
    int currentCount = incrementMessageCount(userId, chatroomId);
    log.debug("사용자 {} 채팅방 {} 메시지 카운트: {}", userId, chatroomId, currentCount);

    // 5개 메시지마다 감정 분석 수행
    if (currentCount >= ANALYSIS_TRIGGER_COUNT) {
      log.info("사용자 {}의 메시지 {}개 달성, 감정 분석 시작", userId, ANALYSIS_TRIGGER_COUNT);

      // 카운트 리셋
      resetUserMessageCount(userId, chatroomId);

      return analyzeRecentMessages(userId, chatroomId);
    }

    return Optional.empty();
  }

  @Override
  public int getUserMessageCount(String userId, String chatroomId) {
    String key = buildCountKey(userId, chatroomId);
    String countStr = redisTemplate.opsForValue().get(key);
    return countStr != null ? Integer.parseInt(countStr) : 0;
  }

  @Override
  public void resetUserMessageCount(String userId, String chatroomId) {
    String key = buildCountKey(userId, chatroomId);
    redisTemplate.delete(key);
    log.debug("사용자 {} 채팅방 {} 메시지 카운트 초기화", userId, chatroomId);
  }

  @Override
  public Optional<UserMoodHistory> analyzeRecentMessages(String userId, String chatroomId) {
    try {
      // 사용자의 최근 5개 메시지 조회 (AI 메시지 제외)
      List<ChatMessage> recentMessages = getRecentUserMessages(userId, chatroomId);

      if (recentMessages.isEmpty()) {
        log.warn("사용자 {}의 최근 메시지가 없어 감정 분석을 건너뜀", userId);
        return Optional.empty();
      }

      // 메시지 내용을 하나의 텍스트로 결합
      String combinedText =
          recentMessages.stream().map(ChatMessage::getContent).collect(Collectors.joining(" "));

      log.info(
          "사용자 {}의 최근 {}개 메시지 감정 분석 수행, 텍스트 길이: {}",
          userId,
          recentMessages.size(),
          combinedText.length());

      // 감정 분석 수행
      EmotionAnalysisService.EmotionAnalysisResult analysisResult =
          emotionAnalysisService.analyzeEmotion(combinedText);

      if (analysisResult.isEmpty()) {
        log.warn("사용자 {}의 감정 분석 결과가 비어있음", userId);
        return Optional.empty();
      }

      // UserMoodHistory 생성 및 저장
      UserMoodHistory moodHistory =
          UserMoodHistory.builder()
              .userId(userId)
              .moodType(analysisResult.getMoodType())
              .intensity(analysisResult.getIntensity())
              .inquiry("자동 감정 분석")
              .inquiryId("auto_" + System.currentTimeMillis())
              .userAnswer(combinedText)
              .emotionAnalysis(analysisResult.getRawJson())
              .createdAt(LocalDateTime.now())
              .answeredAt(LocalDateTime.now())
              .build();

      UserMoodHistory savedHistory = userMoodHistoryRepository.save(moodHistory);

      log.info(
          "사용자 {}의 감정 분석 완료: 감정={}, 강도={}",
          userId,
          analysisResult.getMoodType(),
          analysisResult.getIntensity());

      return Optional.of(savedHistory);

    } catch (Exception e) {
      log.error("사용자 {}의 감정 분석 중 오류 발생: {}", userId, e.getMessage(), e);
      return Optional.empty();
    }
  }

  private int incrementMessageCount(String userId, String chatroomId) {
    String key = buildCountKey(userId, chatroomId);
    Long newCount = redisTemplate.opsForValue().increment(key);

    // TTL 설정 (24시간)
    if (newCount != null && newCount == 1) {
      redisTemplate.expire(key, java.time.Duration.ofHours(24));
    }

    return newCount != null ? newCount.intValue() : 0;
  }

  private List<ChatMessage> getRecentUserMessages(String userId, String chatroomId) {
    // 최근 메시지들을 조회 (AI 메시지 포함)
    List<ChatMessage> allMessages =
        chatMessageRepository.findRecentMessagesByChatroomId(
            chatroomId, ANALYSIS_TRIGGER_COUNT * 2);

    // AI 메시지 필터링 및 사용자 메시지만 추출
    return allMessages.stream()
        .filter(message -> !AI_SENDER_ID.equals(message.getSenderId()))
        .filter(message -> userId.equals(message.getSenderId()))
        .limit(ANALYSIS_TRIGGER_COUNT)
        .collect(Collectors.toList());
  }

  private String buildCountKey(String userId, String chatroomId) {
    return MESSAGE_COUNT_KEY_PREFIX + userId + ":" + chatroomId;
  }
}

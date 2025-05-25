package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import java.util.List;
import java.util.Optional;

public interface MoodInquiryService {

  UserMoodHistory generateMoodInquiry(String userId);

  UserMoodHistory saveMoodAnswer(String inquiryId, String userAnswer);

  List<UserMoodHistory> getUserMoodHistory(String userId);

  void sendMoodInquiryToAllUsers();

  Optional<UserMoodHistory> getTodayMoodInquiry(String userId);

  Optional<UserMoodHistory> getTodayUnansweredInquiry(String userId);

  boolean hasPendingMoodInquiry(String userId);

  Optional<String> getPendingInquiryId(String userId);

  Optional<UserMoodHistory> getLatestMoodHistory(String userId);
}

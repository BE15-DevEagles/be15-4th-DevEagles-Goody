package com.deveagles.be15_deveagles_be.features.timecapsule.query.application.service;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import com.deveagles.be15_deveagles_be.features.timecapsule.query.application.dto.response.TimecapsuleResponse;
import com.deveagles.be15_deveagles_be.features.timecapsule.query.repository.TimecapsuleQueryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimecapsuleQueryService {

  private final TimecapsuleQueryRepository repository;

  // INACTIVE(오픈 가능한) 타임캡슐 목록 조회
  public List<TimecapsuleResponse> getOpenedTimecapsulesByUser(Long userId) {
    return repository
        .findByUserIdAndTimecapsuleStatus(userId, Timecapsule.TimecapsuleStatus.INACTIVE)
        .stream()
        .map(
            tc ->
                TimecapsuleResponse.builder()
                    .timecapsuleId(tc.getTimecapsuleId())
                    .timecapsuleContent(tc.getTimecapsuleContent())
                    .openDate(tc.getOpenDate())
                    .timecapsuleStatus(tc.getTimecapsuleStatus().name())
                    .createdAt(tc.getCreatedAt())
                    .userId(tc.getUserId())
                    .teamId(tc.getTeamId())
                    .openedAt(tc.getOpenedAt())
                    .build())
        .collect(Collectors.toList());
  }

  // 타임캡슐 삭제
  public void deleteTimecapsule(Long timecapsuleId, Long userId) {
    Timecapsule tc =
        repository
            .findById(timecapsuleId)
            .orElseThrow(() -> new IllegalArgumentException("타임캡슐이 존재하지 않습니다."));
    if (!tc.getUserId().equals(userId)) {
      throw new IllegalArgumentException("삭제 권한이 없습니다.");
    }
    repository.deleteById(timecapsuleId);
  }
}

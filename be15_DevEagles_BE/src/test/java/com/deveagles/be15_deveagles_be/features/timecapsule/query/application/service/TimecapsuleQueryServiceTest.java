package com.deveagles.be15_deveagles_be.features.timecapsule.query.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import com.deveagles.be15_deveagles_be.features.timecapsule.query.application.dto.response.TimecapsuleResponse;
import com.deveagles.be15_deveagles_be.features.timecapsule.query.repository.TimecapsuleQueryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimecapsuleQueryServiceTest {

  private TimecapsuleQueryRepository timecapsuleQueryRepository;
  private TimecapsuleQueryService timecapsuleQueryService;

  @BeforeEach
  void setUp() {
    timecapsuleQueryRepository = mock(TimecapsuleQueryRepository.class);
    timecapsuleQueryService = new TimecapsuleQueryService(timecapsuleQueryRepository);
  }

  @Test
  @DisplayName("유저의 오픈된 타임캡슐 리스트 조회 - 정상 동작")
  void getOpenedTimecapsulesByUser_success() {
    // given
    Long userId = 1L;
    Timecapsule capsule1 =
        Timecapsule.builder()
            .timecapsuleId(100L)
            .timecapsuleContent("캡슐1")
            .openDate(LocalDate.now().minusDays(1))
            .timecapsuleStatus(Timecapsule.TimecapsuleStatus.INACTIVE)
            .createdAt(LocalDateTime.now().minusDays(2))
            .userId(userId)
            .teamId(10L)
            .openedAt(LocalDateTime.now().minusDays(1))
            .build();
    Timecapsule capsule2 =
        Timecapsule.builder()
            .timecapsuleId(101L)
            .timecapsuleContent("캡슐2")
            .openDate(LocalDate.now().minusDays(2))
            .timecapsuleStatus(Timecapsule.TimecapsuleStatus.INACTIVE)
            .createdAt(LocalDateTime.now().minusDays(3))
            .userId(userId)
            .teamId(10L)
            .openedAt(LocalDateTime.now().minusDays(2))
            .build();

    List<Timecapsule> mockList = Arrays.asList(capsule1, capsule2);

    when(timecapsuleQueryRepository.findByUserIdAndTimecapsuleStatus(
            eq(userId), eq(Timecapsule.TimecapsuleStatus.INACTIVE)))
        .thenReturn(mockList);

    // when
    List<TimecapsuleResponse> result = timecapsuleQueryService.getOpenedTimecapsulesByUser(userId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getTimecapsuleContent()).isEqualTo("캡슐1");
    assertThat(result.get(1).getTimecapsuleContent()).isEqualTo("캡슐2");
    assertThat(result.get(0).getTimecapsuleStatus()).isEqualTo("INACTIVE");
  }
}

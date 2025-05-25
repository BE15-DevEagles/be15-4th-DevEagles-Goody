package com.deveagles.be15_deveagles_be.features.timecapsule.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.repository.TimecapsuleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenTeamTimecapsulesTest {

  private TimecapsuleRepository timecapsuleRepository;
  private TimecapsuleService timecapsuleService;

  @BeforeEach
  void setUp() {
    timecapsuleRepository = mock(TimecapsuleRepository.class);
    timecapsuleService = new TimecapsuleService(timecapsuleRepository);
  }

  @Test
  @DisplayName("팀 타임캡슐 오픈 - 정상 동작")
  void openTeamTimecapsules_success() {
    // given
    Long teamId = 1L;
    Long userId = 2L;
    LocalDate today = LocalDate.now();

    Timecapsule capsule1 = mock(Timecapsule.class);
    Timecapsule capsule2 = mock(Timecapsule.class);
    List<Timecapsule> capsules = Arrays.asList(capsule1, capsule2);

    when(timecapsuleRepository
            .findByTeamIdAndUserIdAndOpenDateLessThanEqualAndTimecapsuleStatusAndOpenedAtIsNull(
                eq(teamId), eq(userId), eq(today), eq(Timecapsule.TimecapsuleStatus.ACTIVE)))
        .thenReturn(capsules);

    // when
    List<Timecapsule> result = timecapsuleService.openTeamTimecapsules(teamId, userId);

    // then
    assertThat(result).hasSize(2);
    verify(capsule1, times(1)).setOpenedAt(any(LocalDateTime.class));
    verify(capsule1, times(1)).setTimecapsuleStatus(Timecapsule.TimecapsuleStatus.INACTIVE);
    verify(capsule2, times(1)).setOpenedAt(any(LocalDateTime.class));
    verify(capsule2, times(1)).setTimecapsuleStatus(Timecapsule.TimecapsuleStatus.INACTIVE);
  }
}

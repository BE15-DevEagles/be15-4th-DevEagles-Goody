package com.deveagles.be15_deveagles_be.features.timecapsule.command.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.application.dto.request.CreateTimecapsuleRequest;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.repository.TimecapsuleRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateTimecapsuleTest {

  private TimecapsuleRepository timecapsuleRepository;
  private TimecapsuleService timecapsuleService;

  @BeforeEach
  void setUp() {
    timecapsuleRepository = mock(TimecapsuleRepository.class);
    timecapsuleService = new TimecapsuleService(timecapsuleRepository);
  }

  @Test
  @DisplayName("팀 미선택 시 예외 발생")
  void createTimecapsule_noTeamId_throwsException() {
    // given
    CreateTimecapsuleRequest req = new CreateTimecapsuleRequest();
    req.setTimecapsuleContent("내용");
    req.setOpenDate(LocalDate.now().plusDays(1));
    req.setTeamId(null);

    // when & then
    assertThatThrownBy(() -> timecapsuleService.createTimecapsule(req, 1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("팀을 선택해야 타임캡슐을 생성할 수 있습니다.");
  }

  @Test
  @DisplayName("과거 날짜로 생성 시 예외 발생")
  void createTimecapsule_pastDate_throwsException() {
    // given
    CreateTimecapsuleRequest req = new CreateTimecapsuleRequest();
    req.setTimecapsuleContent("내용");
    req.setOpenDate(LocalDate.now().minusDays(1));
    req.setTeamId(1L);

    // when & then
    assertThatThrownBy(() -> timecapsuleService.createTimecapsule(req, 1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("타임캡슐 오픈 날짜는 반드시 오늘 이후여야 합니다.");
  }

  @Test
  @DisplayName("정상 입력 시 저장 메서드 호출")
  void createTimecapsule_success() {
    // given
    CreateTimecapsuleRequest req = new CreateTimecapsuleRequest();
    req.setTimecapsuleContent("내용");
    req.setOpenDate(LocalDate.now().plusDays(1));
    req.setTeamId(1L);

    // when
    timecapsuleService.createTimecapsule(req, 1L);

    // then
    verify(timecapsuleRepository, times(1)).save(any(Timecapsule.class));
  }
}

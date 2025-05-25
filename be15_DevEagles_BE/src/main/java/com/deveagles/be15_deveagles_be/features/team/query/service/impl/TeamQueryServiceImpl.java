package com.deveagles.be15_deveagles_be.features.team.query.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.service.MoodInquiryService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.MyTeamListResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamInformationResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamMemberResponse;
import com.deveagles.be15_deveagles_be.features.team.query.mapper.TeamMapper;
import com.deveagles.be15_deveagles_be.features.team.query.service.TeamQueryService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamQueryServiceImpl implements TeamQueryService {

  private final TeamMapper teamMapper;
  private final TeamMemberRepository teamMemberRepository;
  private final MoodInquiryService moodInquiryService;
  private static final Logger log = LoggerFactory.getLogger(TeamQueryServiceImpl.class);

  @Override
  public List<MyTeamListResponse> getTeamsByUserId(Long userId) {
    return teamMapper.selectMyTeamList(userId);
  }

  @Override
  public List<TeamMemberResponse> getTeamMembers(Long userId, Long teamId) {
    // 1. 요청자 유효성 검증 (soft delete된 팀원이면 예외)
    boolean isMember =
        teamMemberRepository
            .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId)
            .isPresent();

    if (!isMember) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER);
    }

    // 2. 팀원 목록 조회 (soft delete된 팀원 제외)
    return teamMapper.selectTeamMembers(teamId);
  }

  @Override
  public List<TeamMemberResponse> getTeamMembersWithMood(Long userId, Long teamId) {
    // 1. 요청자 유효성 검증
    boolean isMember =
        teamMemberRepository
            .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId)
            .isPresent();

    if (!isMember) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER);
    }

    // 2. 기본 팀원 목록 조회
    List<TeamMemberResponse> members = teamMapper.selectTeamMembers(teamId);

    // 3. 각 팀원의 최근 감정 분석 결과를 추가
    return members.stream().map(this::enrichWithMoodData).collect(Collectors.toList());
  }

  @Override
  public TeamInformationResponse getTeamInformation(Long userId, Long teamId) {

    // 1. 요청자 유효성 검증 (soft delete된 팀원이면 예외)
    boolean isMember =
        teamMemberRepository
            .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId)
            .isPresent();

    if (!isMember) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER);
    }

    return teamMapper.selectTeamInformation(teamId);
  }

  private TeamMemberResponse enrichWithMoodData(TeamMemberResponse member) {
    try {
      log.debug("사용자 {}의 감정 데이터 조회 시작", member.getUserId());
      Optional<UserMoodHistory> latestMood =
          moodInquiryService.getLatestMoodHistory(String.valueOf(member.getUserId()));

      if (latestMood.isPresent()) {
        UserMoodHistory moodHistory = latestMood.get();
        log.debug("사용자 {}의 감정 데이터 조회 성공: {}", member.getUserId(), moodHistory.getMoodType());
        return TeamMemberResponse.builder()
            .userId(member.getUserId())
            .userName(member.getUserName())
            .email(member.getEmail())
            .profileImageUrl(member.getProfileImageUrl())
            .latestMoodType(
                moodHistory.getMoodType() != null ? moodHistory.getMoodType().name() : null)
            .latestMoodIntensity(moodHistory.getIntensity())
            .build();
      } else {
        log.debug("사용자 {}의 감정 데이터가 없음", member.getUserId());
      }
    } catch (Exception e) {
      // 감정 분석 데이터 조회 실패 시 로그 남기고 기본 데이터 반환
      log.error("사용자 {}의 감정 데이터 조회 실패: {}", member.getUserId(), e.getMessage(), e);
    }

    // 감정 데이터가 없거나 조회 실패 시 기본 데이터 반환
    return TeamMemberResponse.builder()
        .userId(member.getUserId())
        .userName(member.getUserName())
        .email(member.getEmail())
        .profileImageUrl(member.getProfileImageUrl())
        .latestMoodType(null)
        .latestMoodIntensity(null)
        .build();
  }
}

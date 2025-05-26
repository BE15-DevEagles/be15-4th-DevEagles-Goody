package com.deveagles.be15_deveagles_be.features.team.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.TransferLeaderRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.WithdrawTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamMemberResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamMemberCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMemberId;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberCommandServiceImpl implements TeamMemberCommandService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final ChatRoomService chatRoomService;
  private final ChatRoomRepository chatRoomRepository;

  @Override
  @Transactional
  public void inviteTeamMember(Long inviterId, Long teamId, String email) {
    // 1. 팀 존재 여부 확인
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));

    // 2. 초대한 사람이 팀장인지 검증
    if (!team.getUserId().equals(inviterId)) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_LEADER);
    }

    // 3. 이메일로 유저 조회
    User invitee =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.USER_NOT_FOUND));

    // 4. 이미 팀원인지 확인
    if (teamMemberRepository.existsByTeamTeamIdAndUserUserId(teamId, invitee.getUserId())) {
      throw new TeamBusinessException(TeamErrorCode.ALREADY_TEAM_MEMBER);
    }

    // 5. 팀원 등록
    TeamMember newMember =
        TeamMember.builder()
            .id(new TeamMemberId(invitee.getUserId(), teamId))
            .user(invitee)
            .team(team)
            .build();

    teamMemberRepository.save(newMember);

    // 6. 팀 기본 채팅방에 참가자 추가
    try {
      log.info("팀 기본 채팅방에 참가자 추가 시작 - 팀ID: {}, 사용자ID: {}", teamId, invitee.getUserId());

      Optional<ChatRoom> defaultChatRoom =
          chatRoomRepository.findDefaultChatRoomByTeamId(String.valueOf(teamId));
      if (defaultChatRoom.isPresent()) {
        log.info("기본 채팅방 발견 - 채팅방ID: {}", defaultChatRoom.get().getId());

        chatRoomService.addParticipantToChatRoom(
            defaultChatRoom.get().getId(), String.valueOf(invitee.getUserId()));

        log.info("팀 기본 채팅방에 참가자 추가 완료");
      } else {
        log.warn("팀 기본 채팅방을 찾을 수 없음 - 팀ID: {}", teamId);
      }
    } catch (Exception e) {
      log.error(
          "팀 기본 채팅방 참가자 추가 실패 - 팀ID: {}, 사용자ID: {}, 에러: {}",
          teamId,
          invitee.getUserId(),
          e.getMessage(),
          e);
    }
  }

  @Override
  @Transactional
  public void fireTeamMember(Long userId, Long teamId, String email) {
    // 1. 팀 조회
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));

    // 2. 팀장 권한 확인
    if (!team.getUserId().equals(userId)) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_LEADER);
    }

    // 3. 추방 대상 유저 조회
    User target =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.USER_NOT_FOUND));

    // 3-1. 팀장이 본인을 추방하려는 경우 예외
    if (target.getUserId().equals(userId)) {
      throw new TeamBusinessException(TeamErrorCode.CANNOT_FIRE_SELF);
    }

    // 4. 팀원 엔티티 조회
    TeamMember teamMember =
        teamMemberRepository
            .findByTeamTeamIdAndUserUserId(teamId, target.getUserId())
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER));

    // 5. 이미 삭제된 경우 체크
    if (teamMember.isDeleted()) {
      throw new TeamBusinessException(TeamErrorCode.ALREADY_DELETED_MEMBER);
    }

    // 6. soft delete 처리
    teamMember.softDelete();

    // 7. 팀 기본 채팅방에서 참가자 제거
    try {
      log.info("팀 기본 채팅방에서 참가자 제거 시작 - 팀ID: {}, 사용자ID: {}", teamId, target.getUserId());

      Optional<ChatRoom> defaultChatRoom =
          chatRoomRepository.findDefaultChatRoomByTeamId(String.valueOf(teamId));
      if (defaultChatRoom.isPresent()) {
        log.info("기본 채팅방 발견 - 채팅방ID: {}", defaultChatRoom.get().getId());

        chatRoomService.removeParticipantFromChatRoom(
            defaultChatRoom.get().getId(), String.valueOf(target.getUserId()));

        log.info("팀 기본 채팅방에서 참가자 제거 완료");
      } else {
        log.warn("팀 기본 채팅방을 찾을 수 없음 - 팀ID: {}", teamId);
      }
    } catch (Exception e) {
      log.error(
          "팀 기본 채팅방 참가자 제거 실패 - 팀ID: {}, 사용자ID: {}, 에러: {}",
          teamId,
          target.getUserId(),
          e.getMessage(),
          e);
    }
  }

  @Override
  @Transactional
  public void withdrawTeam(Long userId, WithdrawTeamRequest request) {
    Long teamId = request.getTeamId();

    // 1. 팀 존재 여부 확인
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));

    // 2. 팀장이 탈퇴하려는 경우 예외 발생
    if (userId.equals(team.getUserId())) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_LEADER_CANNOT_LEAVE);
    }

    // 3. 팀원인지 확인 및 soft delete 처리
    TeamMember teamMember =
        teamMemberRepository
            .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER));

    teamMember.softDelete();

    // 4. 팀 기본 채팅방에서 참가자 제거
    try {
      log.info("팀 탈퇴 시 기본 채팅방에서 참가자 제거 시작 - 팀ID: {}, 사용자ID: {}", teamId, userId);

      Optional<ChatRoom> defaultChatRoom =
          chatRoomRepository.findDefaultChatRoomByTeamId(String.valueOf(teamId));
      if (defaultChatRoom.isPresent()) {
        log.info("기본 채팅방 발견 - 채팅방ID: {}", defaultChatRoom.get().getId());

        chatRoomService.removeParticipantFromChatRoom(
            defaultChatRoom.get().getId(), String.valueOf(userId));

        log.info("팀 탈퇴 시 기본 채팅방에서 참가자 제거 완료");
      } else {
        log.warn("팀 기본 채팅방을 찾을 수 없음 - 팀ID: {}", teamId);
      }
    } catch (Exception e) {
      log.error(
          "팀 탈퇴 시 기본 채팅방 참가자 제거 실패 - 팀ID: {}, 사용자ID: {}, 에러: {}",
          teamId,
          userId,
          e.getMessage(),
          e);
    }
  }

  @Override
  @Transactional
  public void transferLeadership(Long currentLeaderId, Long teamId, TransferLeaderRequest request) {
    // 1. 팀 조회
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));

    // 2. 현재 유저가 팀장인지 확인
    if (!team.getUserId().equals(currentLeaderId)) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_LEADER);
    }

    // 3. 이메일로 양도 대상 유저 조회
    User targetUser =
        userRepository
            .findUserByEmail(request.getEmail())
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.USER_NOT_FOUND));

    // 4. 양도 대상 유저가 해당 팀의 팀원인지 확인
    TeamMember teamMember =
        teamMemberRepository
            .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(targetUser.getUserId(), teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER));

    // 5. 자기 자신에게 양도하려는 경우 방지
    if (currentLeaderId.equals(targetUser.getUserId())) {
      throw new TeamBusinessException(TeamErrorCode.CANNOT_TRANSFER_TO_SELF);
    }

    // 6. 리더십 양도 (team 테이블 user_id 변경)
    team.updateLeader(targetUser.getUserId());
  }

  @Override
  @Transactional
  public TeamMemberResponse findTeamMember(Long userId, Long teamId) {
    Optional<TeamMember> teamMember =
        teamMemberRepository.findByTeamTeamIdAndUserUserId(teamId, userId);
    if (teamMember.isEmpty()) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER);
    }
    return buildTeamMemberResponse(teamMember.get());
  }

  private TeamMemberResponse buildTeamMemberResponse(TeamMember teamMember) {

    return TeamMemberResponse.builder()
        .userId(teamMember.getUser().getUserId())
        .teamId(teamMember.getTeam().getTeamId())
        .build();
  }
}

package com.deveagles.be15_deveagles_be.features.team.command.application.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMemberId;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TeamCommandServiceImpl implements TeamCommandService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final ChatRoomService chatRoomService;
  private final AmazonS3 amazonS3;
  private static final Logger log = LoggerFactory.getLogger(TeamCommandServiceImpl.class);

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Override
  @Transactional
  public CreateTeamResponse createTeam(Long userId, CreateTeamRequest request) {
    // 1. 팀 이름 중복 검증
    if (teamRepository.existsByTeamName(request.getTeamName())) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_NAME_DUPLICATION);
    }

    // 2. 팀 생성
    Team team =
        Team.builder()
            .userId(userId)
            .teamName(request.getTeamName())
            .introduction(request.getIntroduction())
            .build();

    Team saved = teamRepository.save(team);

    // 3. 팀장을 팀원으로 등록
    User teamLeader =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.USER_NOT_FOUND));

    TeamMember teamMember =
        TeamMember.builder()
            .id(new TeamMemberId(userId, saved.getTeamId()))
            .user(teamLeader)
            .team(saved)
            .build();

    teamMemberRepository.save(teamMember);

    // 4. 팀 기본 채팅방 생성
    try {
      log.info("팀 기본 채팅방 생성 시작 - 팀ID: {}, 팀명: {}", saved.getTeamId(), saved.getTeamName());

      var chatRoom =
          chatRoomService.createDefaultChatRoom(
              String.valueOf(saved.getTeamId()), saved.getTeamName() + " 전체 채팅방");

      log.info("팀 기본 채팅방 생성 완료 - 채팅방ID: {}", chatRoom.getId());

      // 팀장을 기본 채팅방에 추가
      log.info("팀장을 기본 채팅방에 추가 시작 - 사용자ID: {}, 채팅방ID: {}", userId, chatRoom.getId());

      chatRoomService.addParticipantToChatRoom(chatRoom.getId(), String.valueOf(userId));

      log.info("팀장을 기본 채팅방에 추가 완료");

    } catch (Exception e) {
      log.error("팀 기본 채팅방 생성 실패 - 팀ID: {}, 에러: {}", saved.getTeamId(), e.getMessage(), e);
    }

    // 5. 응답 반환
    return CreateTeamResponse.builder()
        .teamId(saved.getTeamId())
        .teamName(saved.getTeamName())
        .introduction(saved.getIntroduction())
        .build();
  }

  @Override
  @Transactional
  public void deleteTeam(Long userId, Long teamId) {
    // 1. 팀 조회
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));

    // 2. 팀장인지 확인
    if (!team.getUserId().equals(userId)) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_LEADER);
    }

    // 3. 팀원 목록 조회 (삭제되지 않은)
    List<TeamMember> members = teamMemberRepository.findByTeamTeamIdAndDeletedAtIsNull(teamId);

    // 4. 팀장 외의 팀원이 존재하면 삭제 불가
    boolean hasOtherMembers =
        members.stream().anyMatch(member -> !member.getUser().getUserId().equals(userId));

    if (hasOtherMembers) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_HAS_MEMBERS);
    }

    // 5. soft delete 처리
    team.softDelete();
  }

  @Transactional
  @Override
  public TeamResponse getTeamDetail(Long teamId) {
    Team findTeam =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));
    return TeamResponse.builder()
        .teamId(findTeam.getTeamId())
        .teamName(findTeam.getTeamName())
        .build();
  }

  @Transactional
  public String uploadTeamThumbnail(Long userId, Long teamId, MultipartFile file)
      throws IOException {
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));

    if (!team.getUserId().equals(userId)) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_LEADER);
    }

    String fileName = "team/thumbnail_" + UUID.randomUUID() + "_" + file.getOriginalFilename();

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(file.getContentType());

    amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
    String fileUrl = amazonS3.getUrl(bucket, fileName).toString();

    team.setUrl(fileUrl);
    return fileUrl;
  }
}

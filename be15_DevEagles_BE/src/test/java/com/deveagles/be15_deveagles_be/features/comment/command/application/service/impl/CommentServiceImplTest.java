package com.deveagles.be15_deveagles_be.features.comment.command.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentCreateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentUpdateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.response.CommentResponse;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.aggregate.Comment;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.exception.CommentBusinessException;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.exception.CommentErrorCode;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.repository.CommentRepository;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamMemberCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogDetailResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

  @InjectMocks private CommentServiceImpl commentService;

  @Mock private WorklogService worklogService;
  @Mock private CommentRepository commentRepository;
  @Mock private UserCommandService userCommandService;
  @Mock private TeamMemberCommandService teamMemberCommandService;

  private Long userId;
  private Long otherUserId;
  private Long worklogId;
  private Long teamId;
  private Comment sampleComment;
  private CommentCreateRequest createRequest;
  private CommentUpdateRequest updateRequest;
  private WorklogDetailResponse worklogDetail;
  private UserDetailResponse userDetail;

  @BeforeEach
  void setUp() {
    userId = 1L;
    otherUserId = 999L;
    worklogId = 10L;
    teamId = 20L;

    sampleComment =
        Comment.builder().userId(userId).worklogId(worklogId).commentContent("기존 댓글").build();

    createRequest = new CommentCreateRequest(worklogId, "새 댓글");
    updateRequest = new CommentUpdateRequest("수정된 댓글");

    worklogDetail =
        WorklogDetailResponse.builder()
            .worklogId(worklogId)
            .summary("제목")
            .workContent("내용")
            .teamId(teamId)
            .teamName("팀이름")
            .userName("작성자")
            .writtenAt(null)
            .build();

    userDetail = UserDetailResponse.builder().userId(userId).userName("홍길동").build();
  }

  @Test
  @DisplayName("댓글 등록 성공")
  void registerComment_Success() {
    when(worklogService.getWorklogById(worklogId, userId)).thenReturn(worklogDetail);
    when(userCommandService.getUserDetails(userId)).thenReturn(userDetail);

    // save()에서 넘겨받은 객체에 ID 주입해주는 방식
    when(commentRepository.save(any(Comment.class)))
        .thenAnswer(
            invocation -> {
              Comment c = invocation.getArgument(0);
              ReflectionTestUtils.setField(c, "commentId", 1L);
              ReflectionTestUtils.setField(c, "createdAt", LocalDateTime.of(2024, 5, 26, 10, 0));
              return c;
            });

    // findById도 동일하게 대응
    when(commentRepository.findById(1L))
        .thenAnswer(
            invocation -> {
              Comment comment =
                  Comment.builder()
                      .worklogId(createRequest.getWorklogId())
                      .commentContent(createRequest.getCommentContent())
                      .userId(userId)
                      .build();
              ReflectionTestUtils.setField(comment, "commentId", 1L);
              ReflectionTestUtils.setField(
                  comment, "createdAt", LocalDateTime.of(2024, 5, 26, 10, 0));
              return Optional.of(comment);
            });

    // when
    CommentResponse response = commentService.registerComment(userId, createRequest);

    // then
    assertEquals("홍길동", response.getUsername());
    assertEquals("새 댓글", response.getCommentContent());
    assertNotNull(response.getTime());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 조회 성공")
  void getComments_Success() {
    when(worklogService.getWorklogById(worklogId, userId)).thenReturn(worklogDetail);
    when(commentRepository.findByWorklogIdAndDeletedAtIsNull(worklogId))
        .thenReturn(List.of(sampleComment));
    when(userCommandService.getUserDetails(userId)).thenReturn(userDetail);

    List<CommentResponse> result = commentService.getComments(worklogId, userId);

    assertEquals(1, result.size());
    assertEquals("홍길동", result.get(0).getUsername());
    assertEquals("기존 댓글", result.get(0).getCommentContent());
  }

  @Test
  @DisplayName("댓글 수정 성공")
  void updateComment_Success() {
    when(commentRepository.findById(any())).thenReturn(Optional.of(sampleComment));
    when(userCommandService.getUserDetails(userId)).thenReturn(userDetail);

    commentService.updateComment(1L, updateRequest, userId);

    assertEquals("수정된 댓글", sampleComment.getCommentContent());
  }

  @Test
  @DisplayName("댓글 삭제 성공")
  void removeComment_Success() {
    when(commentRepository.findById(any())).thenReturn(Optional.of(sampleComment));
    when(userCommandService.getUserDetails(userId)).thenReturn(userDetail);

    commentService.removeComment(1L, userId);

    assertNotNull(sampleComment.getDeletedAt());
  }

  @Test
  @DisplayName("댓글 등록 실패 - 잘못된 worklogId")
  void registerComment_InvalidWorklogId_ThrowsException() {
    when(userCommandService.getUserDetails(userId)).thenReturn(userDetail);
    when(worklogService.getWorklogById(worklogId, userId))
        .thenReturn(
            WorklogDetailResponse.builder()
                .worklogId(null)
                .summary(null)
                .workContent(null)
                .teamId(null)
                .teamName(null)
                .userName(null)
                .writtenAt(null)
                .build());

    assertThrows(
        CommentBusinessException.class,
        () -> {
          commentService.registerComment(userId, createRequest);
        });
  }

  @Test
  @DisplayName("댓글 조회 실패 - 팀 멤버 아님")
  void getComments_NotTeamMember_ThrowsException() {
    when(worklogService.getWorklogById(worklogId, userId))
        .thenThrow(new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER));

    CommentBusinessException ex =
        assertThrows(
            CommentBusinessException.class,
            () -> {
              commentService.getComments(worklogId, userId);
            });

    assertEquals(CommentErrorCode.NO_PERMISSION, ex.getErrorCode());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 본인이 아님")
  void updateComment_NotOwner_ThrowsException() {
    Comment someoneElsesComment =
        Comment.builder().userId(otherUserId).commentContent("다른 사람 댓글").build();
    when(userCommandService.getUserDetails(userId)).thenReturn(userDetail);
    when(commentRepository.findById(any())).thenReturn(Optional.of(someoneElsesComment));

    CommentBusinessException ex =
        assertThrows(
            CommentBusinessException.class,
            () -> {
              commentService.updateComment(1L, updateRequest, userId);
            });

    assertEquals(CommentErrorCode.NO_PERMISSION, ex.getErrorCode());
  }

  @Test
  @DisplayName("댓글 삭제 실패 - 본인이 아님")
  void removeComment_NotOwner_ThrowsException() {
    Comment someoneElsesComment =
        Comment.builder().userId(otherUserId).commentContent("삭제할 댓글").build();
    when(userCommandService.getUserDetails(userId)).thenReturn(userDetail);
    when(commentRepository.findById(any())).thenReturn(Optional.of(someoneElsesComment));

    CommentBusinessException ex =
        assertThrows(
            CommentBusinessException.class,
            () -> {
              commentService.removeComment(1L, userId);
            });

    assertEquals(CommentErrorCode.NO_PERMISSION, ex.getErrorCode());
  }
}

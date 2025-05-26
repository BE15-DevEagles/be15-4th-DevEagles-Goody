package com.deveagles.be15_deveagles_be.features.comment.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentCreateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentUpdateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.response.CommentResponse;
import com.deveagles.be15_deveagles_be.features.comment.command.application.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@Tag(name = "댓글", description = "업무일지 댓글 관련 API")
public class CommentCommandController {

  private final CommentService commentService;

  @PostMapping
  @Operation(summary = "댓글 작성", description = "업무일지에 댓글을 작성합니다.")
  public ResponseEntity<ApiResponse<CommentResponse>> createComment(
      @RequestBody CommentCreateRequest request, @AuthenticationPrincipal CustomUser customUser) {
    Long userId = customUser.getUserId();
    CommentResponse response = commentService.registerComment(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @GetMapping("/{worklogId}")
  @Operation(summary = "댓글 목록 조회", description = "특정 업무일지의 댓글 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByWorklog(
      @PathVariable Long worklogId, @AuthenticationPrincipal CustomUser customUser) {
    Long userId = customUser.getUserId();
    List<CommentResponse> comments = commentService.getComments(worklogId, userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(comments));
  }

  /*수정*/
  @Operation(summary = "댓글 수정", description = "특정 댓글의 내용을 수정합니다.")
  @PutMapping("/{commentId}")
  public ResponseEntity<Void> updateComment(
      @PathVariable Long commentId,
      @RequestBody CommentUpdateRequest request,
      @AuthenticationPrincipal CustomUser customUser) {

    Long userId = customUser.getUserId();
    commentService.updateComment(commentId, request, userId);
    return ResponseEntity.ok().build();
  }

  /*삭제*/
  @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable Long commentId, @AuthenticationPrincipal CustomUser customUser) {
    Long userId = customUser.getUserId();
    commentService.removeComment(commentId, userId);
    return ResponseEntity.noContent().build();
  }
}

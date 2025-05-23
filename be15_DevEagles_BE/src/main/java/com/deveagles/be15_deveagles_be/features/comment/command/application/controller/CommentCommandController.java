package com.deveagles.be15_deveagles_be.features.comment.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentCreateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentUpdateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.response.CommentResponse;
import com.deveagles.be15_deveagles_be.features.comment.command.application.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentCommandController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<ApiResponse<CommentResponse>> createComment(
      @RequestBody CommentCreateRequest request, @AuthenticationPrincipal CustomUser customUser) {
    Long userId = customUser.getUserId();
    CommentResponse response = commentService.registerComment(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @GetMapping("/{worklogId}")
  public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByWorklog(
      @PathVariable Long worklogId, @AuthenticationPrincipal CustomUser customUser) {
    Long userId = customUser.getUserId();
    List<CommentResponse> comments = commentService.getComments(worklogId, userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(comments));
  }

  /*수정*/
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
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable Long commentId, @AuthenticationPrincipal CustomUser customUser) {
    Long userId = customUser.getUserId();
    commentService.removeComment(commentId, userId);
    return ResponseEntity.noContent().build();
  }
}

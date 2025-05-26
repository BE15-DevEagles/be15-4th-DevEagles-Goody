package com.deveagles.be15_deveagles_be.features.worklog.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.SearchWorklogRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorklogCreateRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogDetailResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/worklog")
@RequiredArgsConstructor
public class WorklogCommandController {
  private final WorklogService worklogService;

  /*업무일지 등록*/
  @PostMapping("/register/{teamId}")
  @Operation(summary = "업무일지 등록", description = "사용자가 업무일지를 작성합니다.")
  public ResponseEntity<ApiResponse<WorklogDetailResponse>> registerWorklog(
      @RequestBody WorklogCreateRequest worklogCreateRequest,
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long teamId) {
    Long userId = customUser.getUserId();

    WorklogDetailResponse response =
        worklogService.createWorklog(userId, teamId, worklogCreateRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  /*내 업무일지 조회*/
  @Operation(summary = "내 업무일지 조회", description = "로그인한 사용자의 업무일지 목록을 검색 조건에 따라 조회합니다.")
  @PostMapping("/myworklog")
  public ResponseEntity<ApiResponse<PagedResponse<WorklogResponse>>> searchMyWorklog(
      @AuthenticationPrincipal CustomUser customUser, @RequestBody SearchWorklogRequest request) {
    Long userId = customUser.getUserId();

    PagedResponse<WorklogResponse> response = worklogService.findMyWorklog(userId, request);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
  }

  /*팀 업무일지 조회*/
  @Operation(summary = "팀 업무일지 조회", description = "소속된 팀의 모든 업무일지 목록을 검색 조건에 따라 조회합니다.")
  @PostMapping("/team")
  public ResponseEntity<ApiResponse<PagedResponse<WorklogResponse>>> getTeamWorklogs(
      @AuthenticationPrincipal CustomUser customUser, @RequestBody SearchWorklogRequest request) {

    Long userId = customUser.getUserId(); // 인증된 사용자 ID
    PagedResponse<WorklogResponse> response = worklogService.findTeamWorklogs(userId, request);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
  }

  /*업무일지 상세조회*/
  @Operation(summary = "업무일지 상세 조회", description = "특정 업무일지의 상세 정보를 조회합니다.")
  @GetMapping("/{worklogId}")
  public ResponseEntity<ApiResponse<WorklogDetailResponse>> getWorklog(
      @PathVariable Long worklogId, @AuthenticationPrincipal CustomUser customUser) {
    Long userId = customUser.getUserId();
    WorklogDetailResponse response = worklogService.getWorklogById(worklogId, userId);

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
  }
}

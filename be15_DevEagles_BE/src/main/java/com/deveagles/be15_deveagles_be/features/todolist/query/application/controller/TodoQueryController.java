package com.deveagles.be15_deveagles_be.features.todolist.query.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.*;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.service.TodoCalendarQueryService;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.service.TodoDdayQueryService;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.service.TodoQueryService;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.service.TodoTeamQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
@Tag(name = "할 일(Todo)", description = "할 일 조회 관련 API")
public class TodoQueryController {

  private final TodoCalendarQueryService calendarQueryService;
  private final TodoDdayQueryService ddayQueryService;
  private final TodoTeamQueryService teamQueryService;
  private final TodoQueryService todoQueryService;

  @Operation(summary = "내 캘린더 일정 조회", description = "내가 작성한 모든 할 일을 조회합니다.")
  @GetMapping("/calendar/my")
  public ResponseEntity<ApiResponse<List<MyCalendarTodoResponse>>> getMyCalendarTodos(
      @AuthenticationPrincipal CustomUser user) {
    List<MyCalendarTodoResponse> response =
        calendarQueryService.getMyCalendarTodos(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "내 미완료 Todo 목록 조회", description = "내 미완료 할 일 목록을 조회합니다.")
  @GetMapping("/dday/my")
  public ResponseEntity<ApiResponse<PagedResponse<MyDdayTodoResponse>>>
      getMyIncompleteTodosWithDday(
          @AuthenticationPrincipal CustomUser user,
          @RequestParam int page,
          @RequestParam int size) {
    PagedResponse<MyDdayTodoResponse> response =
        ddayQueryService.getMyIncompleteTodosWithDday(user.getUserId(), page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "팀 캘린더 일정 조회", description = "특정 팀의 전체 할 일을 조회합니다.")
  @GetMapping("/calendar/team/{teamId}")
  public ResponseEntity<ApiResponse<List<TeamCalendarTodoResponse>>> getTeamCalendarTodos(
      @PathVariable Long teamId) {
    List<TeamCalendarTodoResponse> response = calendarQueryService.getTeamCalendarTodos(teamId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "특정 팀의 내 미완료 Todo 목록 조회", description = "특정 팀에서 내가 작성한 미완료 할 일 목록을 조회합니다.")
  @GetMapping("/dday/team/{teamId}")
  public ResponseEntity<ApiResponse<PagedResponse<MyTeamDdayTodoResponse>>>
      getMyTeamIncompleteTodosWithDday(
          @AuthenticationPrincipal CustomUser user,
          @PathVariable Long teamId,
          @RequestParam int page,
          @RequestParam int size) {
    PagedResponse<MyTeamDdayTodoResponse> response =
        ddayQueryService.getMyTeamIncompleteTodosWithDday(user.getUserId(), teamId, page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(
      summary = "팀 Todo 목록 필터 조회",
      description = "특정 팀의 할 일 목록을 작성자와 상태별로(완료, 미완료) 필터링하여 조회합니다.")
  @GetMapping("/team/{teamId}")
  public ResponseEntity<ApiResponse<PagedResponse<TeamFilteredTodoResponse>>>
      getTeamTodosByCondition(
          @PathVariable Long teamId,
          @RequestParam(required = false) Long userId,
          @RequestParam(defaultValue = "all") String status,
          @RequestParam int page,
          @RequestParam int size) {

    PagedResponse<TeamFilteredTodoResponse> response =
        teamQueryService.getTeamTodosByCondition(teamId, userId, status, page, size);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "내 Todo 상세 조회", description = "내가 작성한 특정 할 일의 상세 정보를 조회합니다.")
  @GetMapping("/{todoId}")
  public ResponseEntity<TodoDetailResponse> getMyTodoDetail(
      @PathVariable Long todoId, @AuthenticationPrincipal CustomUser user) {
    Long userId = user.getUserId();
    TodoDetailResponse response = todoQueryService.getMyTodoDetail(userId, todoId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "팀 Todo 상세 조회", description = "팀원의 할 일의 상세 정보를 조회합니다.")
  @GetMapping("/team/detail/{todoId}")
  public ApiResponse<TodoDetailResponse> getTeamTodoDetail(@PathVariable Long todoId) {
    TodoDetailResponse response = todoQueryService.getTeamTodoDetail(todoId);
    return ApiResponse.success(response);
  }
}

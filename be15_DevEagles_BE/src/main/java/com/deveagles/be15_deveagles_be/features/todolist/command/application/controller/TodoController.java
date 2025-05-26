package com.deveagles.be15_deveagles_be.features.todolist.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.UpdateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.WorklogWrittenCheckResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.service.TodoService;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.service.WorklogQueryService;
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
@Tag(name = "할 일(Todo)", description = "할 일(Todo) 관련 API")
public class TodoController {

  private final TodoService todoService;
  private final WorklogQueryService worklogQueryService;

  @Operation(summary = "할 일 생성", description = "할 일 목록을 한 번에 여러 개 생성합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse<List<TodoResponse>>> createTodos(
      @AuthenticationPrincipal CustomUser user, @RequestBody List<CreateTodoRequest> requests) {
    List<TodoResponse> response = todoService.createTodos(user.getUserId(), requests);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "할 일 완료 처리", description = "특정 할 일을 완료 처리합니다.")
  @PutMapping("/{todoId}/complete")
  public ResponseEntity<ApiResponse<TodoResponse>> completeTodo(
      @AuthenticationPrincipal CustomUser user, @PathVariable Long todoId) {
    TodoResponse response = todoService.completeTodo(user.getUserId(), todoId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "할 일 수정", description = "특정 할 일을 수정합니다.")
  @PutMapping("/{todoId}")
  public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
      @AuthenticationPrincipal CustomUser user,
      @PathVariable Long todoId,
      @RequestBody UpdateTodoRequest request) {
    TodoResponse response = todoService.updateTodo(user.getUserId(), todoId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "할 일 삭제", description = "특정 할 일을 삭제합니다.")
  @DeleteMapping("/{todoId}")
  public ResponseEntity<ApiResponse<TodoResponse>> deleteTodo(
      @AuthenticationPrincipal CustomUser user, @PathVariable Long todoId) {
    TodoResponse response = todoService.deleteTodo(user.getUserId(), todoId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "작업일지 작성 여부 확인", description = "오늘 해당 팀에 대해 작업일지를 작성했는지 확인합니다.")
  @GetMapping("/worklog/written")
  public ResponseEntity<ApiResponse<WorklogWrittenCheckResponse>> checkWorklogWrittenToday(
      @AuthenticationPrincipal CustomUser user, @RequestParam Long teamId) {

    boolean written = worklogQueryService.hasWrittenToday(user.getUserId(), teamId);
    return ResponseEntity.ok(ApiResponse.success(WorklogWrittenCheckResponse.of(written)));
  }
}

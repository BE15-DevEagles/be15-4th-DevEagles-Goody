package com.deveagles.be15_deveagles_be.features.timecapsule.query.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.timecapsule.query.application.dto.response.TimecapsuleResponse;
import com.deveagles.be15_deveagles_be.features.timecapsule.query.application.service.TimecapsuleQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timecapsules")
@RequiredArgsConstructor
@Tag(name = "타임캡슐 조회", description = "타임캡슐 조회 및 삭제 API")
public class TimecapsuleQueryController {

  private final TimecapsuleQueryService queryService;

  // INACTIVE(오픈 가능한) 타임캡슐 목록 조회
  @GetMapping("/opened")
  @Operation(summary = "오픈된 타임캡슐 조회", description = "사용자가 오픈한 타임캡슐 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<List<TimecapsuleResponse>>> getOpenedTimecapsules(
      @AuthenticationPrincipal CustomUser user) {
    List<TimecapsuleResponse> result = queryService.getOpenedTimecapsulesByUser(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  // 타임캡슐 삭제
  @DeleteMapping("/{timecapsuleId}")
  @Operation(summary = "타임캡슐 삭제", description = "지정된 타임캡슐을 삭제합니다.")
  public ResponseEntity<ApiResponse<Void>> deleteTimecapsule(
      @PathVariable Long timecapsuleId, @AuthenticationPrincipal CustomUser user) {
    queryService.deleteTimecapsule(timecapsuleId, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}

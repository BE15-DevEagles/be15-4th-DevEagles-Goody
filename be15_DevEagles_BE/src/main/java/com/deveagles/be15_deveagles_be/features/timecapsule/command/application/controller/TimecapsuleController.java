package com.deveagles.be15_deveagles_be.features.timecapsule.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.application.dto.request.CreateTimecapsuleRequest;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.application.service.TimecapsuleService;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timecapsules")
@RequiredArgsConstructor
@Tag(name = "타임캡슐", description = "타임캡슐 관리 API")
public class TimecapsuleController {

  private final TimecapsuleService timecapsuleService;

  @PostMapping
  @Operation(summary = "타임캡슐 생성", description = "새로운 타임캡슐을 생성합니다.")
  public ResponseEntity<ApiResponse<Void>> createTimecapsule(
      @Valid @RequestBody CreateTimecapsuleRequest request,
      @AuthenticationPrincipal CustomUser user) {
    timecapsuleService.createTimecapsule(request, user.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  @GetMapping("/team/{teamId}/open")
  @Operation(
      summary = "팀 타임캡슐 오픈",
      description = "지정된 팀의 오픈 가능한 타임캡슐들을 조회하고 오픈합니다. 오픈 날짜가 지난 타임캡슐만 반환됩니다.")
  public ResponseEntity<ApiResponse<List<Timecapsule>>> openTeamTimecapsules(
      @PathVariable Long teamId, @AuthenticationPrincipal CustomUser user) {
    List<Timecapsule> result = timecapsuleService.openTeamTimecapsules(teamId, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}

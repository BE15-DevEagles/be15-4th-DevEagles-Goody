package com.deveagles.be15_deveagles_be.features.worklog.query.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.FindInfoRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;
import com.deveagles.be15_deveagles_be.features.worklog.query.service.WorklogSearchQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchInfoController {

  private final WorklogSearchQueryService worklogSearchQueryService;

  /* 검색 */
  @PostMapping("/info")
  public ResponseEntity<ApiResponse<PagedResponse<WorklogResponse>>> searchInfo(
      @RequestBody FindInfoRequest request, @AuthenticationPrincipal CustomUser customUser) {
    PagedResponse<WorklogResponse> pagedResult = worklogSearchQueryService.searchWorklogs(request);
    return ResponseEntity.ok(ApiResponse.success(pagedResult));
  }
}

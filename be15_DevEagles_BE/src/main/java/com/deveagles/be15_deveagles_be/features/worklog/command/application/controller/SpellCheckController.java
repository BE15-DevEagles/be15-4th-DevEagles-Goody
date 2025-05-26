package com.deveagles.be15_deveagles_be.features.worklog.command.application.controller;

import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.SpellCheckRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.SpellCheckResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spellcheck")
@RequiredArgsConstructor
public class SpellCheckController {
  private final WorklogService worklogService;

  @PostMapping
  public ResponseEntity<SpellCheckResponse> checkSpelling(@RequestBody SpellCheckRequest request) {
    SpellCheckResponse response = worklogService.checkSpelling(request);
    return ResponseEntity.ok(response);
  }
}

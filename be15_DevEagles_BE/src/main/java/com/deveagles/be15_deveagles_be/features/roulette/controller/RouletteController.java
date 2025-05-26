package com.deveagles.be15_deveagles_be.features.roulette.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roulette")
@Tag(name = "룰렛", description = "룰렛 관련 API")
public class RouletteController {

  private static final Logger log = LoggerFactory.getLogger(RouletteController.class);

  private final ChatMessageService chatMessageService;

  public RouletteController(ChatMessageService chatMessageService) {
    this.chatMessageService = chatMessageService;
  }

  @PostMapping("/send-result")
  @Operation(summary = "룰렛 결과 전송", description = "룰렛 결과를 팀의 기본 채팅방에 전송합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "전송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방을 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatMessageResponse>> sendRouletteResult(
      @AuthenticationPrincipal CustomUser customUser, @RequestBody RouletteResultRequest request) {
    log.info(
        "룰렛 결과 전송 요청 -> 사용자: {}, 팀ID: {}, 결과: {}",
        customUser.getUsername(),
        request.getTeamId(),
        request.getResult());

    ChatMessageResponse response =
        chatMessageService.sendRouletteResult(
            customUser.getUserId(), request.getTeamId(), request.getResult());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  public static class RouletteResultRequest {
    private String teamId;
    private String result;

    public String getTeamId() {
      return teamId;
    }

    public String getResult() {
      return result;
    }
  }
}

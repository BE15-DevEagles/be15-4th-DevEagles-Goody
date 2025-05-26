package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellCheckRequest {
  private String workContent;
  private String note;
  private String plan;
}

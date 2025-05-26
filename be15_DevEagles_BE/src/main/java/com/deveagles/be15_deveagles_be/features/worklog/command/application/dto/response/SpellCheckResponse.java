package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.SpellResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellCheckResponse {

  private SpellResult note;
  private SpellResult plan;

  @JsonProperty("workContent")
  private SpellResult workContent;
}

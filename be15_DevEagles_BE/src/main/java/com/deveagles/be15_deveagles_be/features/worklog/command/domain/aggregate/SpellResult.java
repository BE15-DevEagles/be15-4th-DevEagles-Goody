package com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellResult {
  private String original;
  private String corrected;
  private int errors;

  @JsonProperty("errorList")
  private List<SpellError> errorList; // 여기에 틀린 단어 정보들 추가
}

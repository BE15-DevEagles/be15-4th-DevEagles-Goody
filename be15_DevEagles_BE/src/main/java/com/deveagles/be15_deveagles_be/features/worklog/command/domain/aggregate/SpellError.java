package com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellError {
  private String token; // 틀린 단어
  private List<String> suggestions; // 추천 교정 단어
  private String type; // WRONG_SPELLING, WRONG_SPACING 등
}

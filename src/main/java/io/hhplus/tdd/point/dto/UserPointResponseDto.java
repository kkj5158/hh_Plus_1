package io.hhplus.tdd.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPointResponseDto {
  private long id;
  private long point;
  private long updateMillis;

}

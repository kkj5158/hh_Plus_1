package io.hhplus.tdd.point.dto;

import jakarta.validation.constraints.Positive;
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
public class UserPointRequestDto {
  @Positive(message = "Id값은 1이상의 값이여야합니다.")
  private long id;
  @Positive(message = "amount값은 1이상의 값이여야합니다.")
  private long amount;

}

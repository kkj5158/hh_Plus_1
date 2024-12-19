package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.record.TransactionType;
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
public class UserPointHistoryResponseDto {
  private long id;
  private long userId;
  private long amount;
  private TransactionType type;
  private long updateMillis;

}

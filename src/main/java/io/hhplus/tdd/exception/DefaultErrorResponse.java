package io.hhplus.tdd.exception;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class DefaultErrorResponse {
  private final int status;
  private final String error;
  private final String message;
  private final String timestamp;

}

package io.hhplus.tdd.point.unitTests.dto;

import static org.assertj.core.api.Assertions.assertThat;

import io.hhplus.tdd.point.dto.UserPointRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserPointRequestDtoTests {

  private static Validator validator;

  @BeforeAll
  static void setupValidator() {
    // ValidatorFactory를 통해 Validator 인스턴스 생성
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("1. 모든유효한값_통과")
  void 모든유효한값_통과() {
    // 유효한 DTO 생성
    UserPointRequestDto dto = UserPointRequestDto.builder()
                                                 .id(1L)
                                                 .amount(100L)
                                                 .build();

    // 유효성 검증 실행
    Set<ConstraintViolation<UserPointRequestDto>> violations = validator.validate(dto);

    // 검증 결과 확인
    assertThat(violations).isEmpty(); // 유효한 값이므로 violations는 비어 있어야 함
  }

  @Test
  @DisplayName("2. 유효하지않은id값_예외발생")
  void 유효하지않은id값_예외발생() {
    // 유효하지 않은 id 값 (-1) 설정
    UserPointRequestDto dto = UserPointRequestDto.builder()
                                                 .id(-1L)
                                                 .amount(100L)
                                                 .build();

    // 유효성 검증 실행
    Set<ConstraintViolation<UserPointRequestDto>> violations = validator.validate(dto);

    // 검증 결과 확인
    assertThat(violations).isNotEmpty(); // violations는 비어 있지 않아야 함
    assertThat(violations.iterator()
                         .next()
                         .getMessage()).isEqualTo("Id값은 1이상의 값이여야합니다.");
  }

  @Test
  @DisplayName("3. 유효하지않은amount값_예외발생")
  void 유효하지않은amount값_예외발생() {
    // 유효하지 않은 amount 값 (0) 설정
    UserPointRequestDto dto = UserPointRequestDto.builder()
                                                 .id(1L)
                                                 .amount(0L)
                                                 .build();

    // 유효성 검증 실행
    Set<ConstraintViolation<UserPointRequestDto>> violations = validator.validate(dto);

    // 검증 결과 확인
    assertThat(violations).isNotEmpty(); // violations는 비어 있지 않아야 함
    assertThat(violations.iterator()
                         .next()
                         .getMessage()).isEqualTo("amount값은 1이상의 값이여야합니다.");
  }
}
